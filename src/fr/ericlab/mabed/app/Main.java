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

package fr.ericlab.mabed.app;

import fr.ericlab.mabed.structure.Corpus;
import fr.ericlab.util.Util;
import fr.loria.log.AppLogger;
import fr.ericlab.mabed.algo.MABED;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 *   Modifications : Nicolas Dugué
 */
public class Main {

   
    public static void main(String[] args) throws IOException {
    	Logger log = AppLogger.getInstance();
        Locale.setDefault(Locale.US);
        Configuration configuration = new Configuration();
        Corpus corpus = new Corpus(configuration);
        log.info("MABED: Mention-Anomaly-Based Event Detection");
        if(args.length == 0 || args[0].equals("-help")){
        	log.info("For more information on how to run MABED, see the README.txt file");
        }else{
            if(args[0].equals("-run") ){
                try {
                    if(configuration.numberOfThreads>1){
                    	log.info("Running the parallelized implementation with "+configuration.numberOfThreads+" threads (this computer has "+ Runtime.getRuntime().availableProcessors()+" available threads)");
                    }else{
                    	log.info("Running the centralized implementation");
                    }
                    corpus.loadCorpus(configuration.numberOfThreads > 1);
                    String output = "MABED: Mention-Anomaly-Based Event Detection\n"+corpus.output+"\n";
                    log.info("-------------------------\n"+Util.getDate()+" MABED is running\n-------------------------");
                    output += "-------------------------\n"+Util.getDate()+" MABED is running\n-------------------------\n";
                    System.out.println(Util.getDate()+" Reading parameters:\n   - k = "+configuration.k+", p = "+configuration.p+", theta = "+configuration.theta+", sigma = "+configuration.sigma);
                    MABED mabed = new MABED();
                    if(configuration.numberOfThreads>1){
                        output += mabed.applyParallelized(corpus,configuration);
                    }else{
                        output += mabed.applyCentralized(corpus,configuration);
                    }
                    log.info("--------------------\n"+Util.getDate()+" MABED ended\n--------------------");
                    output += "--------------------\n"+Util.getDate()+" MABED ended\n--------------------\n";
                    File textFile = new File(Configuration.getPathOutput()+"/MABED.tex");
                    FileUtils.writeStringToFile(textFile,mabed.events.toLatex(corpus),false);
                    File htmlFile = new File(Configuration.getPathOutput()+"/MABED.html");
                    FileUtils.writeStringToFile(htmlFile,mabed.events.toHtml(corpus),false);
                    textFile = new File(Configuration.getPathOutput()+"/MABED.log");
                    FileUtils.writeStringToFile(textFile,output,false);
                    //mabed.events.printLatex(corpus);
                    log.info("RUN OKAY : RESULTS IN " + Configuration.getPathOutput() +" -> MABED.html should be opened in a browser.");
                } catch (InterruptedException ex) {
                    log.fatal(ex);
                }
            }else{
                log.info("Unknown option '"+args[0]+"'\nType 'java -jar MABED.jar -help' for more information on how to run MABED");
            }
        }
    }
}
