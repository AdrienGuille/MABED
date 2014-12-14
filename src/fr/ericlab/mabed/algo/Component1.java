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

import static fr.ericlab.mabed.algo.MABED._SMOOTH_;
import fr.ericlab.mabed.structure.Corpus;
import fr.ericlab.mabed.structure.Event;
import fr.ericlab.mabed.structure.EventList;
import fr.ericlab.mabed.structure.TimeInterval;
import fr.ericlab.util.Util;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
public class Component1 extends Thread {
    public EventList events = new EventList();
    Corpus corpus;
    int from;
    int to;
    int minTermOccur;
    int maxTermOccur;
    int threadId;
    
    public Component1(int id, Corpus c, int a, int b, int min, int max){
        corpus = c;
        from = a;
        to = b;
        minTermOccur = min;
        maxTermOccur = max;
        threadId = id;
    }
    
    float expectation(int timeSlice, float tmf){
        return corpus.distribution[timeSlice]*(tmf/corpus.nbMessages);
    }
    
    float anomaly(float expectation, float real){
        return real - expectation;
    }
    
    @Override
    public void run() {
        int m = corpus.nbTimeSlices;
        for(int t = from; t <= to; t++){
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
                    events.add(new Event(term,maxI,score,anomaly));
                }
            }
        }
        System.out.println("   - number of detected events (thread "+threadId+"): "+events.size());
    }
}
