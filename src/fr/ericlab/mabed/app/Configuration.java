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

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
public class Configuration {
    
    // Parallel
    public int numberOfThreads;
    
    // Corpus
    public boolean prepareCorpus;
    public int timeSliceLength;
    public String stopwords;
    
    // MABED
    public int k;
    public int p;
    public double theta;
    public double sigma;
    public double minSupport;
    public double maxSupport;
    
    // Database
    public String username;
    public String password;
    public String host;
    public String database;
    
    public Configuration() throws IOException{
        File inputFile = new File("parameters.txt");
        Properties prop = new Properties();
        if(!inputFile.exists()){
            System.out.println("Configuration file not found! See README.txt");
            System.exit(-1);
        }else{
            try (FileInputStream inputStream = new FileInputStream(inputFile)) {
                prop.load(inputStream);
                prepareCorpus = Boolean.parseBoolean(prop.getProperty("prepareCorpus"));
                timeSliceLength = Integer.parseInt(prop.getProperty("timeSliceLength"));
                k = Integer.parseInt(prop.getProperty("k"));
                p = Integer.parseInt(prop.getProperty("p"));
                theta = Double.parseDouble(prop.getProperty("theta"));
                sigma = Double.parseDouble(prop.getProperty("sigma"));
                minSupport = Double.parseDouble(prop.getProperty("minSupport"));
                maxSupport = Double.parseDouble(prop.getProperty("maxSupport"));
                stopwords = prop.getProperty("stopwords");
                username = prop.getProperty("username");
                password = prop.getProperty("password");
                host = prop.getProperty("host");
                database = prop.getProperty("database");
                numberOfThreads = Integer.parseInt(prop.getProperty("numberOfThreads"));
            }
        }
    }
}
