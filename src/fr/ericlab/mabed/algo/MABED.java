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
import fr.ericlab.mabed.structure.TermInfoList;
import fr.ericlab.mabed.structure.WeightedTerm;
import fr.ericlab.mabed.structure.Event;
import fr.ericlab.mabed.structure.TimeInterval;
import fr.ericlab.util.Util;
import fr.ericlab.mabed.structure.TermInfo;
import fr.ericlab.mabed.structure.EventGraph;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
final public class MABED {
    // stopwords
    LinkedList<String> stopWords = new LinkedList<>();
    
    // dataset
    public Corpus dataset;
    
    // algo
    double maximumScore;
    static int _SMOOTH_ = 4;
    static int _MIN_RELATED_WORDS_ = 2;
    public String info;
    
    // results
    public EventList events;
    public EventGraph eventGraph;
    
    public String apply(Corpus d, Configuration configuration){
        dataset = d;
        info = " - minimum support for main terms: "+configuration.minSupport+"<br> - maximum support for main terms: "+configuration.maxSupport+"<br> - maximum number of related terms: "+configuration.p+"<br> - minimum weight for related terms: "+configuration.theta;
        String output = "   - min suppport = "+configuration.minSupport+", max support = "+configuration.maxSupport+", p = "+configuration.p+", theta = "+configuration.theta+", sigma = "+configuration.sigma+"\n";
        
        stopWords = Util.readStopWords(configuration.stopwords);
        System.out.println(Util.getDate()+" Loaded stopwords:\n   - filename: "+configuration.stopwords+"\n   - number of words: "+stopWords.size());
        
        // Get basic events
        long startP1 = Util.getTime();
        EventList basicEvents = getSimpleEvents(dataset, (int)(configuration.minSupport*dataset.nbMessages), (int)(configuration.maxSupport*dataset.nbMessages));
        basicEvents.sort();
        long endP1 = Util.getTime();
        
        // Get final events
        System.out.println(Util.getDate()+" Selecting related terms ("+configuration.k+" events with at most "+configuration.p+" related terms)");
        int nbFinalEvents = 0;
        int i = 0;
        long startP2 = Util.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long theTime = 0;
        if(basicEvents.size() > 0){
            eventGraph = new EventGraph(dataset, basicEvents.get(0).score, configuration.sigma);
            System.out.print("   - k: ");
            while(nbFinalEvents < configuration.k && i < basicEvents.size()){
                Event event = getRefinedEvent(dataset, basicEvents.get(i), configuration.p, configuration.theta);
                if(event.relatedTerms.size() >= _MIN_RELATED_WORDS_){
                    int previousNb = nbFinalEvents;
                    nbFinalEvents += eventGraph.addEvent(event);
                    if(nbFinalEvents > previousNb){
                        try {
                            Date startDate = dateFormat.parse("2009-11-01 00:00:00");
                            theTime = startDate.getTime();
                        } catch (ParseException ex) {
                            Logger.getLogger(MABED.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        System.out.print(" "+nbFinalEvents);
                    }
                }
                i++;
            }
            eventGraph.graph.display();
            long endP2 = Util.getTime();
            System.out.println();
            long startP3 = Util.getTime();
            eventGraph.redundancyGraph.display();
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
    
    double expectation(int timeSlice, double tmf){
        return dataset.distribution[timeSlice]*(tmf/dataset.nbMessages);
    }
    
    double anomaly(double expectation, double beta){
        return beta - expectation;
    }
    
    double getErdemCoefficient(double[] ref, double[] comp, int a, int b){
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
                        
    Event getRefinedEvent(Corpus dataset, Event basicEvent, int p, double theta){
        Event refinedEvent = new Event();
        String [] frequentTerms = new String[p];
        try {
            StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
            RAMDirectory temporaryIndex = new RAMDirectory();
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, analyzer);
            IndexWriter temporaryWriter = new IndexWriter(temporaryIndex, config);
            Document doc = new Document();
            doc.add(new Field("content", dataset.getMessages(basicEvent), Field.Store.YES, Field.Index.ANALYZED,Field.TermVector.YES));
            temporaryWriter.addDocument(doc);
            temporaryWriter.commit();
            IndexReader temporaryReader = IndexReader.open(temporaryWriter, true);
            TermEnum allTerms = temporaryReader.terms();
            int minFreq = 0;
            TermInfoList termList = new TermInfoList();
            HashSet<String> stopWordsSet = new HashSet<>();
            for(String stopWord : stopWords){
                stopWordsSet.add(stopWord);
            }
            stopWordsSet.add(basicEvent.mainTerm);
            while(allTerms.next()){
                String term = allTerms.term().text();
                if(term.length()>1 && !stopWordsSet.contains(term)){
                    float cf = Util.getTermOccurenceCount(temporaryReader, term);
                    if(cf>minFreq){
                        termList.addTermInfo(new TermInfo(term,(int)cf));
                        termList.sortList();
                        if(termList.size() > p){
                            termList.removeLast();
                        }
                        minFreq = termList.get(termList.size()-1).occurence;
                    }
                }
            }
            for(int i = 0; i < termList.size() && i < p; i++){
                frequentTerms[i] = termList.get(i).text;
            }
            temporaryWriter.close();
            temporaryReader.close();
            temporaryIndex.close();
            double ref[] = dataset.getGlobalFrequency(basicEvent.mainTerm);
            double comp[];
            refinedEvent = new Event(basicEvent.mainTerm, basicEvent.I, basicEvent.score, basicEvent.anomaly);
            for(int j = 0; j < p && frequentTerms[j] != null; j++){
                comp = dataset.getGlobalFrequency(frequentTerms[j]);
                double w = getErdemCoefficient(ref, comp, basicEvent.I.timeSliceA, basicEvent.I.timeSliceB);
                if(w >= theta){
                    refinedEvent.relatedTerms.add(new WeightedTerm(frequentTerms[j],w));
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(MABED.class.getName()).log(Level.SEVERE, null, ex);
        }
        return refinedEvent;
    }
        
    EventList getSimpleEvents(Corpus dataset, int minTermOccur, int maxTermOccur){
        System.out.println(Util.getDate()+" Scanning messages (minTermOccur = "+minTermOccur+", maxTermOccur = "+maxTermOccur+")...");
        EventList simpleEvents = new EventList();
        int m = dataset.globalLuceneReader.numDocs();
        try {
            TermEnum allTerms = dataset.mentionLuceneReader.terms();
            while(allTerms.next()){
                String term = allTerms.term().text();
                if(term.length()>2 && !stopWords.contains(term)){
                    TermDocs termDocs = dataset.mentionLuceneReader.termDocs(allTerms.term());
                    double[] gf, mf;
                    gf = dataset.getGlobalFrequency(term);
                    mf = dataset.getMentionFrequency(termDocs);
                    double tmf = Util.sum(mf,0,m-1);
                    double tgf = Util.sum(gf,0,m-1);
                    if(tgf>minTermOccur && tgf<maxTermOccur){
                        double expectation;
                        if(_SMOOTH_ > 0){
                            mf = Util.smoothArray(mf, _SMOOTH_);
                        }
                        double scoreSequence[] = new double[m];
                        for(int i = 0; i < m; i++){
                            expectation = expectation(i+1,tmf);
                            scoreSequence[i] = anomaly(expectation, mf[i]);
                        }
                        LinkedList<TimeInterval> I = new LinkedList<>();
                        LinkedList<Double> L = new LinkedList<>();
                        LinkedList<Double> R = new LinkedList<>();
                        ArrayList<Double> anomaly = new ArrayList<>();
                        for(int i = 0; i < m; i++){
                            anomaly.add(scoreSequence[i]>0.0?scoreSequence[i]:0.0);
                            if(scoreSequence[i]>0){
                                int k = I.size();
                                double Lk = 0, Rk = Util.sum(scoreSequence,0,i);
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
            }
        } catch (IOException ex) {
            Logger.getLogger(MABED.class.getName()).log(Level.SEVERE, null, ex);
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
