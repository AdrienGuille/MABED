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

import fr.ericlab.mabed.algo.MABED;
import fr.loria.bingsearch.BingSearch;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
public class EventList {
    public LinkedList<Event> list;
    
    public EventList(){
        list = new LinkedList<>();
    }
    
    public void writeEventsToFile(Corpus dataset, String filename){
        try {
            File textFile = new File("output/"+filename+".txt");
            FileUtils.writeStringToFile(textFile,"",false);
            for(Event event : list){
                FileUtils.writeStringToFile(textFile,"   - ["+new SimpleDateFormat("yyyy-MM-dd hh:mm").format(dataset.toDate(event.I.timeSliceA))+"//"+new SimpleDateFormat("yyyy-MM-dd hh:mm").format(dataset.toDate(event.I.timeSliceB))+
                        "] "+event.toString(false)+"\n---------------------------------\n",true);
            }
        } catch (IOException ex) {
            Logger.getLogger(MABED.class.getName()).log(Level.SEVERE, null, ex);
        }
    } 
        
    public void scoreEvolution(){
        for(int i = 1; i <= list.size(); i++){
            System.out.print(i+",");
        }
        for(Event event : list){
            System.out.print(event.score+",");
        }
    }
    
    public void printLatex(Corpus corpus){
        System.out.println(toLatex(corpus));
    }
    
    public String toHtml(Corpus corpus) {
        String string = "<!DOCTYPE html>\n<html lang=\"en\"><head>\n<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n    <meta charset=\"utf-8\">\n    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n    <meta name=\"description\" content=\"\">\n    <meta name=\"author\" content=\"\">\n    <title>Mabed Output</title>\n    <link href=\"bootstrap.css\" rel=\"stylesheet\">\n    <link href=\"1-col-portfolio.css\" rel=\"stylesheet\">\n</head>\n<body>\n    <div class=\"container\">\n        <div class=\"row\">\n            <div class=\"col-lg-12\">\n                <h1 class=\"page-header\">Mabed output\n                    <small>Event detection results</small>\n                </h1>\n            </div>\n        </div>";
        LinkedList<String> images;
        Iterator<String> it;
        for(Event topic : list){
        	try {
				images=BingSearch.getPictures(topic.mainTerm.replace(",", " + "));
				it=images.iterator();
				string+=" <!-- /.row -->\n\n        <!-- Project One -->\n        <div class=\"row\">\n            <div class=\"col-md-7\">\n";
				for (int i = 0; i < 3 && it.hasNext(); i++)
					string+="<center><img class=\"img-responsive\" width='500px' src=\"" + it.next()+ "\" alt=\"\"><br /></center>\n";
				string+="</div>\n            <div class=\"col-md-5\">";
	            string +="<h3>" + topic.mainTerm+"</h3>";
	            string+="<h4>"+new SimpleDateFormat("dd/MM HH:mm").format(corpus.toDate(topic.I.timeSliceA)) + " - "+ new SimpleDateFormat("dd/MM HH:mm").format(corpus.toDate(topic.I.timeSliceB))+"</h4>";
	            string+="<p>"+topic.relatedTerms.toString().replace("related terms:","")+"</p></div></div><hr><hr><hr>";
			} catch (IOException e) {
				e.printStackTrace();
			}
        	
        }
        return string+"<hr>\n\n        <!-- Footer -->\n        <footer>\n            <div class=\"row\">\n                <div class=\"col-lg-12\">\n                    <p>Copyright © Mabed - Adrien Guille</p>\n                </div>\n            </div>\n            <!-- /.row -->\n        </footer>\n\n    </div>\n    <script src=\"jquery.js\"></script>\n    <script src=\"bootstrap.js\"></script>\n</body></html>";
    }
    
    public String toLatex(Corpus corpus){
        int rank = 1;
        String string = "";
        for(Event topic : list){
            string += rank+" & "+new SimpleDateFormat("dd/MM HH:mm").format(corpus.toDate(topic.I.timeSliceA))+" -- "+new SimpleDateFormat("dd/MM HH:mm").format(corpus.toDate(topic.I.timeSliceB))+" & "+topic.mainTerm+": "+topic.relatedTerms.toString().replace("related terms:","")+"\\\\ \\hline\n";
            rank++;
        }
        return string;
    }
    
    public void add(Event t){
        list.add(t);
    }
    
    public void sort(){
        Collections.sort(list);
    }
    
    public int size(){
        return list.size();
    }
    
    public Event get(int i){
        return list.get(i);
    }
    
    public void addAll(EventList tl){
        list.addAll(tl.list);
    }
    
    public void exportDetailledResults(Corpus corpus){
        File outputDir = new File("output/csv/");
        if(!outputDir.isDirectory()){
            outputDir.mkdir();
        }else{
            for(String filename : outputDir.list()){
                FileUtils.deleteQuietly(new File("output/csv/"+filename));
            }
        }
        NumberFormat formatter = new DecimalFormat("000");
        for(int i = 0; i < list.size(); i++){
            Event event = list.get(i);
            String mainTerm = event.mainTerm.replace(", ", "_");
            File descFile = new File("output/csv/"+formatter.format(i)+"-"+mainTerm+".desc");
            File wordsFile = new File("output/csv/"+formatter.format(i)+"-"+mainTerm+".words");
            File seriesFile = new File("output/csv/"+formatter.format(i)+"-"+mainTerm+".anomaly");            
            try {
                FileUtils.writeStringToFile(descFile,event.score+"\t"+event.I.timeSliceA+"\t"+event.I.timeSliceB+"\t"+new SimpleDateFormat("YYYY-MM-dd HH:mm").format(corpus.toDate(event.I.timeSliceA))+"\t"+new SimpleDateFormat("YYYY-MM-dd HH:mm").format(corpus.toDate(event.I.timeSliceB))+"\n",true);
            } catch (IOException ex) {
                Logger.getLogger(EventList.class.getName()).log(Level.SEVERE, null, ex);
            }
            for(WeightedTerm wt : event.relatedTerms.list){
                try {
                    FileUtils.writeStringToFile(wordsFile,wt.term+"\t"+wt.weight+"\n",true);
                } catch (IOException ex) {
                    Logger.getLogger(EventList.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            for(int j = 0; j < event.anomaly.size(); j++){
                try {
                    FileUtils.writeStringToFile(seriesFile,j+"\t"+event.anomaly.get(j)+"\n",true);
                } catch (IOException ex) {
                    Logger.getLogger(EventList.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
