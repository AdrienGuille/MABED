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

import java.util.Collections;
import java.util.LinkedList;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
public class TermInfoList {
    public LinkedList<TermInfo> list;
    
    public TermInfoList(){
        list = new LinkedList<>();
    }
    
    public boolean addTermInfo(TermInfo termInfo){
        return list.add(termInfo);
    }
    
    public void sortList(){
        Collections.sort(list);
    }
    
    public int size(){
        return list.size();
    }
    
    public TermInfo get(int i){
        return list.get(i);
    }
    
    public void removeLast(){
        list.removeLast();
    }
    
    public void clearList(){
        list.clear();
    }
}
