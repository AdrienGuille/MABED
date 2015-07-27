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

package fr.ericlab.util;

import fr.ericlab.mabed.algo.MABED;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
public class Util {
    
    static public LinkedList<String> readStopWords(String pathToStopwordsFile){
        LinkedList<String> stopWords = new LinkedList<>();
        if(pathToStopwordsFile != null){
            LineIterator it = null;
            try {
                it = FileUtils.lineIterator(new File(pathToStopwordsFile), "UTF-8");
                while (it.hasNext()) {
                    stopWords.add(it.nextLine());
                }
            } catch (IOException ex) {
                Logger.getLogger(MABED.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                LineIterator.closeQuietly(it);
            }
        }
        return stopWords;
    }
       
    static public String getDate(){
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        String dateString = dateFormat.format(date); 
        return dateString;
    }
    
    static public long getTime(){
        Date date = new Date();
        return date.getTime();
    }
    
    static public int sum(short tab[], int a, int b){
        int sum = 0;
        for(int i = a; i <= b; i++){
            sum += tab[i];
        }
        return sum;
    }
    
    static public float sum(float tab[], int a, int b){
        float sum = 0;
        for(int i = a; i <= b; i++){
            sum += tab[i];
        }
        return sum;
    }
    
    static public float[] toFloatArray(short[] array){
        float[] newArray = new float[array.length];
        for(int i = 0; i < array.length; i++){
            newArray[i] = array[i];
        }
        return newArray;
    }
    
    static public float[] smoothArray(float array[], int windowSize){
        float[] smoothedArray = new float[array.length];
        for(int i = 0; i < array.length-1; i++){
            smoothedArray[i] = centeredMovingAverage(array, i, windowSize);
        }
        return smoothedArray;
    }
    
    static public float[] smoothArray(short array[], int windowSize){
        float[] smoothedArray = new float[array.length];
        for(int i = 0; i < array.length-1; i++){
            smoothedArray[i] = centeredMovingAverage(array, i, windowSize);
        }
        return smoothedArray;
    }
    
    static public double getMedian(double array[]){
        Arrays.sort(array);
        return array[array.length/2];
    }
    
    static public float centeredMovingAverage(float[] array, int index, int windowSize){
        int halfWindowSize = windowSize/2;
        int possibleLeftWindow = (index >= halfWindowSize)?halfWindowSize:index;
        int possibleRightWindow = (index+halfWindowSize < array.length-1)? halfWindowSize:array.length-2-index;
        int i1 = index - possibleLeftWindow, i2 = index + possibleRightWindow;
        float total = 0;
        for(int i = i1; i <= i2; i++){
            total += array[i];
        }
        return total/(float)(possibleLeftWindow+possibleRightWindow);
    }
    
    static public float centeredMovingAverage(short[] array, int index, int windowSize){
        int halfWindowSize = windowSize/2;
        int possibleLeftWindow = (index >= halfWindowSize)?halfWindowSize:index;
        int possibleRightWindow = (index+halfWindowSize < array.length-1)? halfWindowSize:array.length-2-index;
        int i1 = index - possibleLeftWindow, i2 = index + possibleRightWindow;
        float total = 0;
        for(int i = i1; i <= i2; i++){
            total += array[i];
        }
        return total/(float)(possibleLeftWindow+possibleRightWindow);
    }
}
