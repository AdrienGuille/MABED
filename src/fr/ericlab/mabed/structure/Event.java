////////////////////////////////////////////////////////////////////////////////
//  This file is part of MABED.                                               //
//                                                                            //
//  SONDY is free software: you can redistribute it and/or modify             //
//  it under the terms of the GNU General Public License as published by      //
//  the Free Software Foundation, either version 3 of the License, or         //
//  (at your option) any later version.                                       //
//                                                                            //
//  SONDY is distributed in the hope that it will be useful,                  //
//  but WITHOUT ANY WARRANTY; without even the implied warranty of            //
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
//  GNU General Public License for more details.                              //
//                                                                            //
//  You should have received a copy of the GNU General Public License         //
//  along with SONDY.  If not, see <http://www.gnu.org/licenses/>.            //
////////////////////////////////////////////////////////////////////////////////

package fr.ericlab.mabed.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
public class Event implements Comparable<Event> {
    public String mainTerm;
    public WeightedTermList relatedTerms;
    public TimeInterval I;
    public double score;
    public ArrayList<Double> anomaly;
    
    public Event(){
        mainTerm = "noMainTerm";
        score = 0;
        relatedTerms = new WeightedTermList();
        anomaly = new ArrayList<>();
    }
    
    public Event(String t, TimeInterval tI, double s){
        mainTerm = t;
        I = tI;
        score = s;
        relatedTerms = new WeightedTermList();
        anomaly = new ArrayList<>();
    }
    
    public Event(String t, TimeInterval tI, double s, ArrayList a){
        mainTerm = t;
        I = tI;
        score = s;
        relatedTerms = new WeightedTermList();
        anomaly = a;
    }
    
    public void setMainTerm(String t){
        mainTerm = t;
    }
    
    public Map<String,Object> getMainTermAttributes(){
        HashMap<String,Object> map = new HashMap<>();
        map.put("ui.class","mainTerm");
        map.put("ui.color",1);
        map.put("ui.color",1);
        map.put("I", I.timeSliceA+":"+I.timeSliceB);
        map.put("score",score);
        return map;
    }
    
    public Event merge(Event t){
        Event t1 = new Event(this.mainTerm+", "+t.mainTerm, this.I, this.score);
        for(WeightedTerm wt : this.relatedTerms.list){
            if(!t1.contains(wt.term)){
                t1.relatedTerms.add(wt);
            }
        }
        for(WeightedTerm wt : t.relatedTerms.list){
            if(!t1.contains(wt.term)){
                t1.relatedTerms.add(wt);
            }
        }
        return t1;
    }
    
    public Event merge(EventList tl){
        Event t1 = new Event("",this.I,this.score,this.anomaly);
        String mT = this.mainTerm;
        for(Event t : tl.list){
            mT += ", "+t.mainTerm;
        }
        t1.setMainTerm(mT);
        for(Event t : tl.list){
            for(WeightedTerm wt : t.relatedTerms.list){
                    if(!t1.contains(wt.term)){
                        t1.relatedTerms.add(wt);
                    }
                }
        }
        for(WeightedTerm wt : this.relatedTerms.list){
            if(!t1.contains(wt.term)){
                t1.relatedTerms.add(wt);
            }
        }
        return t1;
    }
    
    public String toString(boolean printI){
        String str = "";
        if(printI){
            str = "["+I.toString()+"] ";
        }
        str += mainTerm+"("+score+"): ";
        for(WeightedTerm wt : relatedTerms.list){
            str += wt.term+"("+wt.weight+") ";
        }
        return str;
    }
    
    public String intervalAsString(String lang){
        
        return "";
    }
    
    public boolean contains(String term){
        return this.mainTerm.contains(term) || containsrelatedTerm(term);
    }
    
    public boolean containsrelatedTerm(String term){
        for(WeightedTerm wt : relatedTerms.list){
            if(wt.term.equals(term)){
                return true;
            }
        }
        return false;
    }
    
    public String relatedTermAsList(){
        String st = "";
        for(WeightedTerm wt : relatedTerms.list){
            st += wt.term+" ";
        }
        return st;
    }
    
    public String anomalyToString(){
        String string = "[";
        for(double d : anomaly){
            string += d+",";
        }
        string = string.substring(0,string.length());
        return string+"]";
    }

    @Override
    public int compareTo(Event o) {
        if((o.score - this.score) == 0){
            return 0;
        }else{
            if(this.score > o.score){
                return -1;
            }else{
                return 1;
            }
        }
    }
}
