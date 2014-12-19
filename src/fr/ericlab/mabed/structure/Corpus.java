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

import com.mchange.v2.c3p0.ComboPooledDataSource;
import fr.ericlab.mabed.app.Configuration;
import fr.ericlab.util.Util;
import indexer.GlobalIndexer;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

/**
 *
 *   @author Adrien GUILLE, ERIC Lab, University of Lyon 2
 *   @email adrien.guille@univ-lyon2.fr
 */
public class Corpus {
    public Configuration configuration;
    
    public String info;
    public int nbMessages;
    public int nbAuthors;
    public int nbTimeSlices;
    public boolean loaded = false;
    public Timestamp startTimestamp;
    public Timestamp endTimestamp;
    public int[] distribution;
    public String output;
    
    // Indexes
    short[][] frequencyMatrix;
    public ArrayList<String> vocabulary;
    short[][] mentionFrequencyMatrix;
    public ArrayList<String> mentionVocabulary;
    
    // Database
    static final int _BULK_SIZE_ = 100;
    ComboPooledDataSource connectionPool;
    
    public Corpus(Configuration conf){
        configuration = conf;
        Properties p = new Properties(System.getProperties());
        p.put("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");
        p.put("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "OFF");
        System.setProperties(p);
    }
       
    public void prepareCorpus(){
        try {
            System.out.println(Util.getDate()+" Preparing corpus...");
            // Connecting to DB
            connectionPool = new ComboPooledDataSource();
            connectionPool.setLogWriter(null);
            connectionPool.setDriverClass("com.mysql.jdbc.Driver");
            connectionPool.setJdbcUrl("jdbc:mysql://"+configuration.host);
            connectionPool.setUser(configuration.username);
            connectionPool.setPassword(configuration.password);
            connectionPool.setMinPoolSize(1);
            connectionPool.setAcquireIncrement(1);
            connectionPool.setMaxPoolSize(configuration.numberOfThreads+1);
            Connection connection = connectionPool.getConnection();
            
            Statement statement = connection.createStatement();
            statement.executeUpdate("USE "+configuration.database);
            ResultSet rs = statement.executeQuery("SHOW TABLES LIKE 'messages';");
            boolean tableExists = rs.next();
            if(tableExists){
                statement.executeUpdate("DROP TABLE "+configuration.database+".messages;");
            }
            statement.executeUpdate("CREATE TABLE "+configuration.database+".messages ( id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, time_slice INT, msg_post_time TIMESTAMP, msg_text VARCHAR(200) CHARACTER SET utf8 COLLATE utf8_unicode_ci) CHARACTER SET utf8 COLLATE utf8_unicode_ci ENGINE=myisam;");
            // Preparing time-slices
            String[] fileArray = new File("input/").list();
            nbTimeSlices = 0;
            NumberFormat formatter = new DecimalFormat("00000000");
            ArrayList<Integer> list = new ArrayList<>();
            for(String filename : fileArray){
                if(filename.endsWith(".text")){
                    try {
                        list.add(formatter.parse(filename.substring(0, 8)).intValue());
                    } catch (ParseException ex) {
                        Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    nbTimeSlices++;
                }
            }
            int a = Collections.min(list), b = Collections.max(list);
            // Formatter for input files names
            LineIterator it = null;
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S");
                it = FileUtils.lineIterator(new File("input/"+formatter.format(a)+".time"), "UTF-8");
                if(it.hasNext()) {
                    Date parsedDate = dateFormat.parse(it.nextLine());
                    startTimestamp = new java.sql.Timestamp(parsedDate.getTime());
                }
                it = FileUtils.lineIterator(new File("input/"+formatter.format(b)+".time"), "UTF-8");
                String lastLine = "";
                while(it.hasNext()) {
                    lastLine = it.nextLine();
                }
                Date parsedDate = dateFormat.parse(lastLine);
                endTimestamp = new java.sql.Timestamp(parsedDate.getTime());
            } catch (IOException | ParseException ex) {
                Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                LineIterator.closeQuietly(it);
            }
            System.out.print("   - Scanning input");
            GlobalIndexer indexer = new GlobalIndexer(configuration.numberOfThreads,false);
            try {
                indexer.index("input/", configuration.stopwords);
            } catch (    InterruptedException | IOException ex) {
                Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
            }
            indexer = new GlobalIndexer(configuration.numberOfThreads,true);
            try {
                indexer.index("input/", configuration.stopwords);
            } catch (    InterruptedException | IOException ex) {
                Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
            }
            statement = connection.createStatement();
            for(int i = a; i <= b; i++){
                String message = "";
                LineIterator itText = null, itTime = null;
                try {
                    itText = FileUtils.lineIterator(new File("input/"+formatter.format(i)+".text"), "UTF-8");
                    itTime = FileUtils.lineIterator(new File("input/"+formatter.format(i)+".time"), "UTF-8");
                    int bulk = 0;
                    String bulkString = "";
                    while (itTime.hasNext()) {
                        message = itText.nextLine().replace("'"," ").replace("\""," ").replace("%"," ").replace("\\"," ");
                        bulk++;
                        if(bulk < _BULK_SIZE_){
                            bulkString += " ("+i+",'"+itTime.nextLine()+"',\""+message+"\"),";
                        }else{
                            bulk = 0;
                            bulkString += " ("+i+",'"+itTime.nextLine()+"',\""+message+"\");";
                            statement.executeUpdate("INSERT INTO "+configuration.database+".messages (time_slice,msg_post_time,msg_text) VALUES"+bulkString);
                            bulkString = "";
                        }
                    }
                    if(bulk > 0){
                        statement.executeUpdate("INSERT INTO "+configuration.database+".messages (time_slice,msg_post_time,msg_text) VALUES"+bulkString.substring(0,bulkString.length()-1)+";");
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    LineIterator.closeQuietly(itText);
                    LineIterator.closeQuietly(itTime);
                }
            }
            System.out.println(", 100% done.");
            statement.executeUpdate("CREATE INDEX index_time_slice ON "+configuration.database+".messages (time_slice);");
            statement.close();
            connection.close();
        } catch (SQLException | PropertyVetoException ex) {
            Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void loadCorpus(boolean parallelized){
        output = "";
        if(configuration.prepareCorpus){
            prepareCorpus();
        }
        try {
            // Connecting to DB
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            connectionPool = new ComboPooledDataSource();
            connectionPool.setLogWriter(null);
            connectionPool.setDriverClass("com.mysql.jdbc.Driver");
            connectionPool.setJdbcUrl("jdbc:mysql://"+configuration.host);
            connectionPool.setUser(configuration.username);
            connectionPool.setPassword(configuration.password);
            connectionPool.setMinPoolSize(1);
            connectionPool.setAcquireIncrement(1);
            connectionPool.setMaxPoolSize(configuration.numberOfThreads*2);
            Connection connection = connectionPool.getConnection();
            Statement statement = connection.createStatement();
            // Getting basic properties
            String[] fileArray = new File("input/").list();
            nbTimeSlices = 0;
            NumberFormat formatter = new DecimalFormat("00000000");
            ArrayList<Integer> list = new ArrayList<>();
            for(String filename : fileArray){
                if(filename.endsWith(".text")){
                    try {
                        list.add(formatter.parse(filename.substring(0, 8)).intValue());
                    } catch (ParseException ex) {
                        Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    nbTimeSlices++;
                }
            }
            int a = Collections.min(list), b = Collections.max(list);
            distribution = new int[nbTimeSlices];
            ResultSet rs = statement.executeQuery("SELECT time_slice, count(*) FROM "+configuration.database+".messages group by time_slice;");
            nbMessages = 0;
            while(rs.next()){
                distribution[rs.getInt(1)] = rs.getInt(2);
                nbMessages += rs.getInt(2);
            }
            // Formatter for input files names
            LineIterator it = null;
            try {
                it = FileUtils.lineIterator(new File("input/"+formatter.format(a)+".time"), "UTF-8");
                if(it.hasNext()) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                    Date parsedDate = dateFormat.parse(it.nextLine());
                    startTimestamp = new java.sql.Timestamp(parsedDate.getTime());
                }
                it = FileUtils.lineIterator(new File("input/"+formatter.format(b)+".time"), "UTF-8");
                String timestamp = "";
                while(it.hasNext()) {
                    timestamp = it.nextLine();
                }
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                Date parsedDate = dateFormat.parse(timestamp);
                endTimestamp = new java.sql.Timestamp(parsedDate.getTime());
            } catch (IOException | ParseException ex) {
                Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                LineIterator.closeQuietly(it);
            }
            
            // load indexes
            try {
                // Global index
                FileInputStream fisMatrix = new FileInputStream("input/indexes/frequencyMatrix.dat");
                ObjectInputStream oisMatrix = new ObjectInputStream(fisMatrix);
                frequencyMatrix = (short[][]) oisMatrix.readObject();
                FileInputStream fisVocabulary = new FileInputStream("input/indexes/vocabulary.dat");
                ObjectInputStream oisVocabulary = new ObjectInputStream(fisVocabulary);
                vocabulary = (ArrayList<String>) oisVocabulary.readObject();
                // Mention index
                FileInputStream fisMentionMatrix = new FileInputStream("input/indexes/mentionFrequencyMatrix.dat");
                ObjectInputStream oisMentionMatrix = new ObjectInputStream(fisMentionMatrix);
                mentionFrequencyMatrix = (short[][]) oisMentionMatrix.readObject();
                FileInputStream fisMentionVocabulary = new FileInputStream("input/indexes/mentionVocabulary.dat");
                ObjectInputStream oisMentionVocabulary = new ObjectInputStream(fisMentionVocabulary);
                mentionVocabulary = (ArrayList<String>) oisMentionVocabulary.readObject();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
            }
            statement.close();
            connection.close();
            DecimalFormat df = new DecimalFormat("#,###");
            System.out.println(Util.getDate()+" Loaded corpus:");
            output += Util.getDate()+" Loaded corpus:\n";
            info ="   - time-slices: "+df.format(nbTimeSlices)+" time-slices of "+configuration.timeSliceLength+" minutes each\n";
            info +="   - first message: "+startTimestamp+"\n";
            double datasetLength = (nbTimeSlices*configuration.timeSliceLength)/60/24;
            info +="   - last message: "+endTimestamp+" ("+datasetLength+" days)\n";
            info +="   - number of messages: "+df.format(nbMessages);
            output += info;
            System.out.println(info);
        } catch (SQLException | ClassNotFoundException | PropertyVetoException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public short[] getMentionFrequency(int i){
        return mentionFrequencyMatrix[i];
    }
    
    public short[] getGlobalFrequency(String term){
        int i = vocabulary.indexOf(term);
        if(i == -1){
            return new short[nbTimeSlices];
        }else{
            return frequencyMatrix[i];
        }
    }
    
    public String getMessages(Event simpleEvent){
        try {
            Connection connection = connectionPool.getConnection();
            Statement statement = connection.createStatement();
            // Getting messages
            String messages = "";
            String mainTerm = simpleEvent.mainTerm.replace("'"," ").replace("\""," ").replace("%"," ").replace("\\"," ");
            String query = "select msg_text from "+configuration.database+".messages where time_slice>="+simpleEvent.I.timeSliceA+" and time_slice<="+simpleEvent.I.timeSliceB+" and msg_text like '% "+mainTerm+" %';";
            ResultSet rs = statement.executeQuery(query);
            while(rs.next()){
                messages += rs.getString(1)+"\n";
            }
            statement.close();
            connection.close();
            return messages;
        } catch (SQLException ex) {
            Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
    public Timestamp toDate(int timeSlice){
        Timestamp date = startTimestamp;
        long dateLong = date.getTime() + timeSlice*configuration.timeSliceLength*60*1000L;
        return new Timestamp(dateLong);
    }
}
