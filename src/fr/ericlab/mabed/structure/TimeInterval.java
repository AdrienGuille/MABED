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

import java.util.HashSet;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
public class TimeInterval implements Comparable {
    public int timeSliceA;
    public int timeSliceB;
           
    public TimeInterval(int a, int b){
        timeSliceA = a;
        timeSliceB = b;
    }
    
    public TimeInterval(String str){
        String[] split = str.split(":");
        timeSliceA = Integer.parseInt(split[0]);
        timeSliceB = Integer.parseInt(split[1]);
    }
    
    public String toString(){
        return timeSliceA+":"+timeSliceB;
    }
    
    public double intersection(TimeInterval ti){
        HashSet<Integer> set1 = new HashSet<>();
        HashSet<Integer> set2 = new HashSet<>();
        int intersectionSize = 0;
        for(int i = this.timeSliceA; i <= this.timeSliceB; i++){
            set1.add(i);
        }
        for(int j = ti.timeSliceA; j <= ti.timeSliceB; j++){
            set2.add(j);
        }
        for(int k : set1){
            if(set2.contains(k)){
                intersectionSize++;
            }
        }
        return intersectionSize;
    }
    
    public double intersectionProportion(TimeInterval ti){
        return intersection(ti)/(this.timeSliceB-this.timeSliceA+1);
    }
    
    @Override
    public int compareTo(Object o) {
        TimeInterval point = (TimeInterval)o;
        if(this.timeSliceA > point.timeSliceA){
            return 1;
        }else{
            if(this.timeSliceA == point.timeSliceA){
                return 0;
            }else{
                return -1;
            }
        }
    }
}
