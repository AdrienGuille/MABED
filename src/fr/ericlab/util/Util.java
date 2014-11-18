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
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;

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
        String dateString = dateFormat.format(date).toString(); 
        return dateString;
    }
    
    static public long getTime(){
        Date date = new Date();
        return date.getTime();
    }
    
    static public double sum(double tab[], int a, int b){
        float sum = 0;
        for(int i = a; i <= b; i++){
            sum += tab[i];
        }
        return sum;
    }
    
    static public int getTermOccurenceCount(IndexReader reader, String term){
        try {
            int totalFreq = 0;          
            TermDocs termDocs = reader.termDocs();
            termDocs.seek(new Term("content", term));
            while(termDocs.next()){
                totalFreq += termDocs.freq();
            }
            return totalFreq;
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }
    
    static public double[] smoothArray(double array[], int windowSize){
        double[] smoothedArray = new double[array.length];
        for(int i = 0; i < array.length-1; i++){
            smoothedArray[i] = centeredMovingAverage(array, i, windowSize);
        }
        return smoothedArray;
    }
    
    static public double getMedian(double array[]){
        Arrays.sort(array);
        return array[array.length/2];
    }
    
    static public double centeredMovingAverage(double[] array, int index, int windowSize){
        int halfWindowSize = windowSize/2;
        int possibleLeftWindow = (index >= halfWindowSize)?halfWindowSize:index;
        int possibleRightWindow = (index+halfWindowSize < array.length-1)? halfWindowSize:array.length-2-index;
        int i1 = index - possibleLeftWindow, i2 = index + possibleRightWindow;
        float total = 0;
        for(int i = i1; i <= i2; i++){
            total += array[i];
        }
        return total/(double)(possibleLeftWindow+possibleRightWindow);
    }
}
