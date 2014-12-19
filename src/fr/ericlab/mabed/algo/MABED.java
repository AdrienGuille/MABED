////////////////////////////////////////////////////////////////////////////////
//  This file is part of MABED.                                               //
//                                                                            //
//  MABED is free software: you can redistribute it and/or modify             //
//  it under the terms of the GNU General Public License as published by      //
//  the Free Software Foundation, either version 3 of the License, or         //
//  (at your option) any later version.                                       //
//                                                                            //
//  MABED is distributed in the hope that it will be useful,                  //
//  but WITHOUT ANY WARRANTY; without even the implied warranty of            //
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
//  GNU General Public License for more details.                              //
//                                                                            //
//  You should have received a copy of the GNU General Public License         //
//  along with MABED.  If not, see <http://www.gnu.org/licenses/>.            //
////////////////////////////////////////////////////////////////////////////////

package fr.ericlab.mabed.algo;

import fr.ericlab.mabed.app.Configuration;
import fr.ericlab.mabed.structure.EventList;
import fr.ericlab.mabed.structure.Corpus;
import fr.ericlab.mabed.structure.WeightedTerm;
import fr.ericlab.mabed.structure.Event;
import fr.ericlab.mabed.structure.TimeInterval;
import fr.ericlab.util.Util;
import fr.ericlab.mabed.structure.EventGraph;
import indexer.Indexer;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
final public class MABED {
    // stopwords
    LinkedList<String> stopWords = new LinkedList<>();
    
    // dataset
    public Corpus corpus;
    
    // algo
    double maximumScore;
    static int _SMOOTH_ = 4;
    static int _MIN_RELATED_WORDS_ = 2;
    public String info;
    
    // results
    public EventList events;
    public EventGraph eventGraph;
    
    public String applyCentralized(Corpus c, Configuration configuration){
        corpus = c;
        info = " - minimum support for main terms: "+configuration.minSupport+"<br> - maximum support for main terms: "+configuration.maxSupport+"<br> - maximum number of related terms: "+configuration.p+"<br> - minimum weight for related terms: "+configuration.theta;
        String output = "   - min suppport = "+configuration.minSupport+", max support = "+configuration.maxSupport+", p = "+configuration.p+", theta = "+configuration.theta+", sigma = "+configuration.sigma+"\n";
        
        stopWords = Util.readStopWords(configuration.stopwords);
        System.out.println(Util.getDate()+" Loaded stopwords:\n   - filename: "+configuration.stopwords+"\n   - number of words: "+stopWords.size());
        
        // Get basic events
        long startP1 = Util.getTime();
        EventList basicEvents = getSimpleEvents((int)(configuration.minSupport*corpus.nbMessages), (int)(configuration.maxSupport*corpus.nbMessages));
        basicEvents.sort();
        long endP1 = Util.getTime();
        
        // Get final events
        System.out.println(Util.getDate()+" Selecting related terms ("+configuration.k+" events with at most "+configuration.p+" related terms)");
        int nbFinalEvents = 0;
        int i = 0;
        long startP2 = Util.getTime();
        if(basicEvents.size() > 0){
            eventGraph = new EventGraph(corpus, basicEvents.get(0).score, configuration.sigma);
            System.out.print("   - k: ");
            while(nbFinalEvents < configuration.k && i < basicEvents.size()){
                Event event = getRefinedEvent(corpus, basicEvents.get(i), configuration.p, configuration.theta);
                if(event.relatedTerms.size() >= _MIN_RELATED_WORDS_){
                    int previousNb = nbFinalEvents;
                    nbFinalEvents += eventGraph.addEvent(event);
                    if(nbFinalEvents > previousNb){
                        System.out.print(" "+nbFinalEvents);
                    }
                }
                i++;
            }
            long endP2 = Util.getTime();
            System.out.println();
            long startP3 = Util.getTime();
            mergeRedundantEvents(eventGraph);
            events = eventGraph.toEventList();
            long endP3 = Util.getTime();
            double p1 = (double)(endP1-startP1)/(double)1000, p2 = (double)(endP2-startP2)/(double)1000, p3 = (double)(endP3-startP3)/(double)1000;
            DecimalFormat df = new DecimalFormat("#.00"); 
            System.out.println(Util.getDate()+" Computation time: "+df.format(p1)+"s + "+df.format(p2)+"s + "+df.format(p3)+"s = "+df.format(p1+p2+p3)+"s");
            output += "   - computation time: "+df.format(p1)+"s + "+df.format(p2)+"s + "+df.format(p3)+"s = "+df.format(p1+p2+p3)+"s\n";
        }
        return output;
    }
    
    public String applyParallelized(Corpus d, Configuration configuration) throws InterruptedException{
        corpus = d;
        info = " - minimum support for main terms: "+configuration.minSupport+"<br> - maximum support for main terms: "+configuration.maxSupport+"<br> - maximum number of related terms: "+configuration.p+"<br> - minimum weight for related terms: "+configuration.theta;
        String output = "   - min suppport = "+configuration.minSupport+", max support = "+configuration.maxSupport+", p = "+configuration.p+", theta = "+configuration.theta+", sigma = "+configuration.sigma+"\n";
        
        stopWords = Util.readStopWords(configuration.stopwords);
        System.out.println(Util.getDate()+" Loaded stopwords:\n   - filename: "+configuration.stopwords+"\n   - number of words: "+stopWords.size());
        
        // Phase 1
        long startP1 = Util.getTime();
        System.out.println(Util.getDate()+" Detection of events based on mention anomaly...");
        LinkedList<Component1> c1Threads = new LinkedList<>();
        int numberOfWordsPerThread = corpus.mentionVocabulary.size()/configuration.numberOfThreads;                
        for(int i = 0; i < configuration.numberOfThreads; i++){
            int upperBound = (i==configuration.numberOfThreads-1)?corpus.mentionVocabulary.size()-1:numberOfWordsPerThread*(i+1);
            c1Threads.add(new Component1(i,corpus,numberOfWordsPerThread*i+1,upperBound,(int)(configuration.minSupport*corpus.nbMessages),(int)(configuration.maxSupport*corpus.nbMessages)));
            c1Threads.get(i).start();
        }
        for(Component1 c1 : c1Threads){
            c1.join();
        }
        EventList basicEvents = new EventList();
        for(Component1 c1 : c1Threads){
            basicEvents.addAll(c1.events);
        }
        basicEvents.sort();
        c1Threads.clear();
        System.out.println("   - number of detected events (total): "+basicEvents.size());
        long endP1 = Util.getTime();
        
        // Phase 2
        System.out.println(Util.getDate()+" Selecting related terms ("+configuration.k+" events with at most "+configuration.p+" related terms)");
        int nbFinalEvents = 0;
        int i = 0;
        long startP2 = Util.getTime();
        if(basicEvents.size() > 0){
            eventGraph = new EventGraph(corpus, basicEvents.get(0).score, configuration.sigma);
            System.out.print("   - k: ");
            while(nbFinalEvents < configuration.k && i < basicEvents.size()){
                int numberOfC2Threads = ((configuration.k - nbFinalEvents)<=configuration.numberOfThreads)?(configuration.k-nbFinalEvents):configuration.numberOfThreads;
                Event[] refinedEvents = new Event[numberOfC2Threads];
                LinkedList<Component2> c2Threads = new LinkedList<>();
                for(int j = 0; j < numberOfC2Threads; j++){
                    c2Threads.add(new Component2(j,corpus,basicEvents.get(i+j),configuration.p,configuration.theta));
                    c2Threads.get(j).start();
                }
                for(Component2 c2 : c2Threads){
                    c2.join();
                }
                for(Component2 c2 : c2Threads){
                    refinedEvents[c2.threadId] = c2.refinedEvent;
                }
                for(Event refinedEvent : refinedEvents){
                    if(refinedEvent.relatedTerms.size() >= _MIN_RELATED_WORDS_){
                        int previousNb = nbFinalEvents;
                        nbFinalEvents += eventGraph.addEvent(refinedEvent);
                        if(nbFinalEvents > previousNb){
                            System.out.print(" "+nbFinalEvents);
                        }
                    }
                    i++;
                }
            }
            long endP2 = Util.getTime();
            System.out.println();
            long startP3 = Util.getTime();
            mergeRedundantEvents(eventGraph);
            events = eventGraph.toEventList();
            long endP3 = Util.getTime();
            double p1 = (double)(endP1-startP1)/(double)1000, p2 = (double)(endP2-startP2)/(double)1000, p3 = (double)(endP3-startP3)/(double)1000;
            DecimalFormat df = new DecimalFormat("#.00"); 
            System.out.println(Util.getDate()+" Computation time: "+df.format(p1)+"s + "+df.format(p2)+"s + "+df.format(p3)+"s = "+df.format(p1+p2+p3)+"s");
            output += "   - computation time: "+df.format(p1)+"s + "+df.format(p2)+"s + "+df.format(p3)+"s = "+df.format(p1+p2+p3)+"s\n";
        }
        return output;
    }
    
    float expectation(int timeSlice, float tmf){
        return corpus.distribution[timeSlice]*(tmf/corpus.nbMessages);
    }
    
    float anomaly(float expectation, float real){
        return real - expectation;
    }
    
    double getErdemCoefficient(short[] ref, short[] comp, int a, int b){
        double scores1[] = new double[b-a+1], scores2[] = new double[b-a+1]; 
        for(int i = a; i <= b; i++){
            scores1[i-a] = ref[i];
            scores2[i-a] = comp[i];
        }
        double result;
        double A12 = 0, A1 = 0, A2 = 0;
        for(int i=2;i<scores1.length;i++){
            A12 += (scores1[i]-scores1[i-1])*(scores2[i]-scores2[i-1]);
            A1 += (scores1[i]-scores1[i-1])*(scores1[i]-scores1[i-1]);
            A2 += (scores2[i]-scores2[i-1])*(scores2[i]-scores2[i-1]);
        }
        A1 = Math.sqrt(A1/(scores1.length-1));
        A2 = Math.sqrt(A2/(scores1.length-1));
        result = A12/((scores1.length-1)*A1*A2);
        return (double) (result+1)/2;
    }
                        
    Event getRefinedEvent(Corpus corpus, Event basicEvent, int p, double theta){
        Event refinedEvent = new Event();
        Indexer indexer = new Indexer();
        ArrayList<String> candidateWords = indexer.getMostFrequentWords(corpus.getMessages(basicEvent),basicEvent.mainTerm,p);
        short ref[] = corpus.getGlobalFrequency(basicEvent.mainTerm);
        short comp[];
        refinedEvent = new Event(basicEvent.mainTerm, basicEvent.I, basicEvent.score, basicEvent.anomaly);
        for(String word : candidateWords){
            comp = corpus.getGlobalFrequency(word);
            double w = getErdemCoefficient(ref, comp, basicEvent.I.timeSliceA, basicEvent.I.timeSliceB);
            if(w >= theta){
                refinedEvent.relatedTerms.add(new WeightedTerm(word,w));
            }
        }
        return refinedEvent;
    }
        
    EventList getSimpleEvents(int minTermOccur, int maxTermOccur){
        System.out.println(Util.getDate()+" Scanning messages (minTermOccur = "+minTermOccur+", maxTermOccur = "+maxTermOccur+")...");
        EventList simpleEvents = new EventList();
        int m = corpus.nbTimeSlices;
        for(int t = 0; t < corpus.mentionVocabulary.size(); t++){
            String term = corpus.mentionVocabulary.get(t);
            float[] gf, mf;
            gf = Util.toFloatArray(corpus.getGlobalFrequency(term));
            mf = Util.toFloatArray(corpus.getMentionFrequency(t));
            int tmf = (int)Util.sum(mf,0,m-1);
            int tgf = (int)Util.sum(gf,0,m-1);
            if(tgf>minTermOccur && tgf<maxTermOccur){
                float expectation;
                if(_SMOOTH_ > 0){
                    mf = Util.smoothArray(mf, _SMOOTH_);
                }
                float scoreSequence[] = new float[m];
                for(int i = 0; i < m; i++){
                    expectation = expectation(i,tmf);
                    scoreSequence[i] = anomaly(expectation, mf[i]);
                }
                LinkedList<TimeInterval> I = new LinkedList<>();
                LinkedList<Float> L = new LinkedList<>();
                LinkedList<Float> R = new LinkedList<>();
                ArrayList<Float> anomaly = new ArrayList<>();
                for(int i = 0; i < m; i++){
                    anomaly.add(scoreSequence[i]>0?scoreSequence[i]:0);
                    if(scoreSequence[i]>0){
                        int k = I.size();
                        float Lk = 0, Rk = Util.sum(scoreSequence,0,i);
                        if(i>0){
                            Lk = Util.sum(scoreSequence,0,i-1);
                        }
                        int j = 0;
                        boolean foundJ = false;
                        for(int l=k-1; l>=0 && !foundJ; l--){
                            if(L.get(l)<Lk){
                                foundJ = true;
                                j = l;
                            }
                        }
                        if(foundJ && R.get(j)<Rk){
                            TimeInterval Ik = new TimeInterval(I.get(j).timeSliceA,i);
                            for(int p = j; p<k; p++){
                                I.removeLast();
                                L.removeLast();
                                R.removeLast();
                            }
                            k = j;
                            I.add(Ik);
                            L.add(Util.sum(scoreSequence,0,Ik.timeSliceA-1));
                            R.add(Util.sum(scoreSequence,0,Ik.timeSliceB));
                        }else{
                            I.add(new TimeInterval(i,i));
                            L.add(Lk);
                            R.add(Rk);
                        }
                    }
                }
                if(I.size()>0){
                    TimeInterval maxI = I.get(0);
                    for(TimeInterval Ii : I){
                        if(Util.sum(scoreSequence,Ii.timeSliceA,Ii.timeSliceB)>Util.sum(scoreSequence,maxI.timeSliceA,maxI.timeSliceB)){
                            maxI.timeSliceA = Ii.timeSliceA;
                            maxI.timeSliceB = Ii.timeSliceB;
                        }
                    }
                    double score = Util.sum(scoreSequence,I.get(0).timeSliceA,I.get(0).timeSliceB);
                    simpleEvents.add(new Event(term,maxI,score,anomaly));
                }
            }
        }
        System.out.println("   - number of detected events: "+simpleEvents.size());
        simpleEvents.sort();
        return simpleEvents;
    }
    
    void mergeRedundantEvents(EventGraph eventGraph){
        System.out.println(Util.getDate()+" Merging duplicated events...");
        eventGraph.identifyConnectedComponents();
    }
}
