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

import fr.ericlab.mabed.structure.Corpus;
import fr.ericlab.mabed.structure.Event;
import fr.ericlab.mabed.structure.WeightedTerm;
import indexer.Indexer;
import java.util.ArrayList;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
public class Component2 extends Thread {
    Corpus corpus;
    Event basicEvent;
    int candidateWordSetSize;
    double theta;
    Event refinedEvent;
    int threadId;
    
    public Component2(int id, Corpus c, Event be, int p, double t){
        corpus = c;
        basicEvent = be;
        candidateWordSetSize = p;
        theta = t;
        threadId = id;
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
    
    @Override
    public void run(){
        refinedEvent = new Event();
        Indexer indexer = new Indexer();
        ArrayList<String> candidateWords = indexer.getMostFrequentWords(corpus.getMessages(basicEvent),basicEvent.mainTerm,candidateWordSetSize);
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
    }
}
