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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import fr.loria.log.AppLogger;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 *   Modifications : Nicolas Dugué
 */
public class Configuration {
    
    // Parallel
    public int numberOfThreads;
    
    // Corpus
    public boolean prepareCorpus;
    public int timeSliceLength;
    public String stopwords;
    private static String path;
    private static String pathOutput;
    
    // MABED
    public int k;
    public int p;
    public double theta;
    public double sigma;
    public double minSupport;
    public double maxSupport;
    
    //HTMLReport
    private static boolean twitterFilled=false;
    private static String consumer;
    private static String secretConsumer;
    private static String token;
    private static String secretToken;
    
    //period
    private static int period = -1;
    
    private Logger log = AppLogger.getInstance();
    
    public Configuration(String file) throws IOException{
        File inputFile = new File(file);
        Properties prop = new Properties();
        if(!inputFile.exists()){
            log.fatal("Configuration file not found! See README.txt");
            System.exit(-1);
        }else{
            try (FileInputStream inputStream = new FileInputStream(inputFile)) {
                prop.load(inputStream);
                prepareCorpus = Boolean.parseBoolean(prop.getProperty("prepareCorpus"));
                path=prop.getProperty("exp");
                log.info("Input path : "+path);
                pathOutput=path+"_output";
                log.info("Ouput path " +pathOutput);
                File outputDir = new File(pathOutput);
                if(!outputDir.isDirectory()){
                	log.info("Output directory created");
                    outputDir.mkdir();
                }
                timeSliceLength = Integer.parseInt(prop.getProperty("timeSliceLength"));
                k = Integer.parseInt(prop.getProperty("k"));
                p = Integer.parseInt(prop.getProperty("p"));
                theta = Double.parseDouble(prop.getProperty("theta"));
                sigma = Double.parseDouble(prop.getProperty("sigma"));
                minSupport = Double.parseDouble(prop.getProperty("minSupport"));
                maxSupport = Double.parseDouble(prop.getProperty("maxSupport"));
                stopwords = prop.getProperty("stopwords");
                numberOfThreads = Integer.parseInt(prop.getProperty("numberOfThreads"));
                
                twitterFilled= Boolean.parseBoolean(prop.getProperty("twitter"));
                if (twitterFilled) {
                	consumer=prop.getProperty("consumerKey");
                	secretConsumer=prop.getProperty("secretConsumerKey");
                	token=prop.getProperty("token");
                	secretToken=prop.getProperty("secretToken");
                }
                else {
                	log.info("No Twitter properties found : HTML report won't contain pictures and tweet samples.");
                }
                if (prop.containsKey("period"))
                	period=Integer.parseInt(prop.getProperty("period"));
            }
        }
    }

	public static boolean isTwitterFilled() {
		return twitterFilled;
	}

	public static String getConsumer() {
		return consumer;
	}

	public static String getSecretConsumer() {
		return secretConsumer;
	}

	public static String getToken() {
		return token;
	}

	public static String getSecretToken() {
		return secretToken;
	}

	public static String getPath() {
		return path;
	}

	public static String getPathOutput() {
		return pathOutput;
	}

	public static int getPeriod() {
		return period;
	}
	

    
}
