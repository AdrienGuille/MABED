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

package fr.ericlab.mabed.structure;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
public class WeightedTerm implements Comparable<WeightedTerm>{
    public String term;
    public double weight;
    
    public WeightedTerm(String t){
        term = t;
        weight = 0;
    }
    
    public WeightedTerm(String t, double w){
        term = t;
        weight = w;
    }
    
    @Override
    public int compareTo(WeightedTerm o) {
        if(o.weight-this.weight<0){
            return -1;
        }else{
            if(o.weight-this.weight>0){
                return 1;
            }else{
                return 0;
            }
        }
    }
}
