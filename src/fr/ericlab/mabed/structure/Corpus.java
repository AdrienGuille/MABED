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

import fr.ericlab.mabed.app.Configuration;
import fr.ericlab.util.Util;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

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
    
    // Lucene
    HashMap<Integer,Integer> globalLuceneIds;
    HashMap<Integer,Integer> mentionLuceneIds;
    public IndexReader globalLuceneReader;
    IndexWriter globalLuceneWriter;
    public IndexReader mentionLuceneReader;
    IndexWriter mentionLuceneWriter;
    
    // Database
    static final int _BULK_SIZE_ = 100;
    Connection connection;
    Statement statement;
    
    public Corpus(Configuration conf){
        configuration = conf;
    }
       
    public void prepareCorpus(){
        try {
            System.out.println(Util.getDate()+" Preparing corpus...");
            // Connecting to DB
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection("jdbc:mysql://"+configuration.host, configuration.username, configuration.password);
            statement = connection.createStatement();
            statement.executeUpdate("USE "+configuration.database);
            ResultSet rs = statement.executeQuery("SHOW TABLES LIKE 'messages';");
            boolean tableExists = rs.next();
            if(tableExists){
                statement.executeUpdate("DROP TABLE "+configuration.database+".messages;");
            }
            statement.executeUpdate("CREATE TABLE "+configuration.database+".messages ( id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, time_slice INT, msg_post_time TIMESTAMP, msg_text VARCHAR(200) CHARACTER SET utf8 COLLATE utf8_unicode_ci) CHARACTER SET utf8 COLLATE utf8_unicode_ci ENGINE=myisam;");
            // Preparing time-slices
            nbTimeSlices = new File("input/").list().length/2;
            // Formatter for input files names
            NumberFormat formatter = new DecimalFormat("00000000");
            LineIterator it = null;
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S");
                it = FileUtils.lineIterator(new File("input/"+formatter.format(1)+".time"), "UTF-8");
                if(it.hasNext()) {
                    Date parsedDate = dateFormat.parse(it.nextLine());
                    startTimestamp = new java.sql.Timestamp(parsedDate.getTime());
                }
                it = FileUtils.lineIterator(new File("input/"+formatter.format(nbTimeSlices)+".time"), "UTF-8");
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
            // Preparing directories
            if(new File("index/global").exists()){
                FileUtils.deleteDirectory(new File("index/global"));
            }
            if(new File("index/mention").exists()){
                FileUtils.deleteDirectory(new File("index/mention"));
            }
            File globalIndexDirectory = new File("index/global");
            globalIndexDirectory.mkdir();
            File mentionIndexDirectory = new File("index/mention");
            mentionIndexDirectory.mkdir();
            // Creating Lucene indexes
            FSDirectory globalLuceneIndex = FSDirectory.open(globalIndexDirectory);
            FSDirectory mentionLuceneIndex = FSDirectory.open(mentionIndexDirectory);
            org.apache.lucene.analysis.StopwordAnalyzerBase analyzer;
            analyzer = new StandardAnalyzer(Version.LUCENE_36);
            IndexWriterConfig globalLuceneConfig = new IndexWriterConfig(Version.LUCENE_36, analyzer);
            IndexWriterConfig mentionLuceneConfig = new IndexWriterConfig(Version.LUCENE_36, analyzer);
            globalLuceneWriter = new IndexWriter(globalLuceneIndex, globalLuceneConfig);
            mentionLuceneWriter = new IndexWriter(mentionLuceneIndex, mentionLuceneConfig);
            LinkedList<Double> steps = new LinkedList<>();
            steps.add(0.75);
            steps.add(0.50);
            steps.add(0.25);
            statement = connection.createStatement();
            System.out.print("   - Scanning input");
            for(int i = 1; i <= nbTimeSlices; i++){
                if((steps.size() > 0) && ((double)i/(double)nbTimeSlices) > steps.getLast()){
                    System.out.print(", "+(int)(steps.getLast()*100)+"% done");
                    steps.removeLast();
                }
                String globalDocContent = new String();
                String mentionDocContent = new String();
                String message = "";
                LineIterator itText = null, itTime = null;
                try {
                    itText = FileUtils.lineIterator(new File("input/"+formatter.format(i)+".text"), "UTF-8");
                    itTime = FileUtils.lineIterator(new File("input/"+formatter.format(i)+".time"), "UTF-8");
                    int bulk = 0;
                    String bulkString = "";
                    while (itTime.hasNext()) {
                        message = itText.nextLine();
                        bulk++;
                        if(message.contains("@")){
                            mentionDocContent += message+"\n";
                        }
                        if(bulk < _BULK_SIZE_){
                            bulkString += " ("+i+",'"+itTime.nextLine()+"',\""+message+"\"),";
                        }else{
                            bulk = 0;
                            bulkString += " ("+i+",'"+itTime.nextLine()+"',\""+message+"\");";
                            statement.executeUpdate("INSERT INTO "+configuration.database+".messages (time_slice,msg_post_time,msg_text) VALUES"+bulkString);
                            bulkString = "";
                        }
                        globalDocContent += message+"\n";
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
                // add document to global index
                Document globalDoc = new Document();
                globalDoc.add(new Field("content", globalDocContent, Field.Store.YES, Field.Index.ANALYZED,Field.TermVector.YES));
                globalDoc.add(new Field("id", Integer.toString(i), Field.Store.YES, Field.Index.NOT_ANALYZED));
                globalLuceneWriter.addDocument(globalDoc);
                globalLuceneWriter.commit();
                // add document to mention index
                Document mentionDoc = new Document();
                mentionDoc.add(new Field("content", mentionDocContent, Field.Store.YES, Field.Index.ANALYZED,Field.TermVector.YES));
                mentionDoc.add(new Field("id", Integer.toString(i), Field.Store.YES, Field.Index.NOT_ANALYZED));
                mentionLuceneWriter.addDocument(mentionDoc);
                mentionLuceneWriter.commit();
            }
            System.out.println(", 100% done.");
            statement.executeUpdate("CREATE INDEX index_time_slice ON "+configuration.database+".messages (time_slice);");
            statement.close();
            connection.close();
            globalLuceneWriter.close();
            mentionLuceneWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
            Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void loadCorpus(){
        output = "";
        if(configuration.prepareCorpus){
            prepareCorpus();
        }
        try {
            // Connecting to DB
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection("jdbc:mysql://"+configuration.host, configuration.username, configuration.password);
            statement = connection.createStatement();
            // Getting basic properties
            nbTimeSlices = new File("input/").list().length/2;
            distribution = new int[nbTimeSlices+1];
            ResultSet rs = statement.executeQuery("SELECT time_slice, count(*) FROM "+configuration.database+".messages group by time_slice;");
            nbMessages = 0;
            while(rs.next()){
                distribution[rs.getInt(1)] = rs.getInt(2);
                nbMessages += rs.getInt(2);
            }
            // Formatter for input files names
            NumberFormat formatter = new DecimalFormat("00000000");
            LineIterator it = null;
            try {
                it = FileUtils.lineIterator(new File("input/"+formatter.format(1)+".time"), "UTF-8");
                if(it.hasNext()) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                    Date parsedDate = dateFormat.parse(it.nextLine());
                    startTimestamp = new java.sql.Timestamp(parsedDate.getTime());
                }
                it = FileUtils.lineIterator(new File("input/"+formatter.format(nbTimeSlices)+".time"), "UTF-8");
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
            // Preparing directories
            File globalIndexDirectory = new File("index/global");
            File mentionIndexDirectory = new File("index/mention");
            // Loading Lucene indexes
            FSDirectory globalLuceneIndex = FSDirectory.open(globalIndexDirectory);
            FSDirectory mentionLuceneIndex = FSDirectory.open(mentionIndexDirectory);
            org.apache.lucene.analysis.StopwordAnalyzerBase analyzer;
            analyzer = new StandardAnalyzer(Version.LUCENE_36);
            WhitespaceAnalyzer whitespaceAnalyzer = new WhitespaceAnalyzer(Version.LUCENE_36);
            IndexWriterConfig globalLuceneConfig = new IndexWriterConfig(Version.LUCENE_36, whitespaceAnalyzer);
            IndexWriterConfig mentionLuceneConfig = new IndexWriterConfig(Version.LUCENE_36, whitespaceAnalyzer);
            globalLuceneWriter = new IndexWriter(globalLuceneIndex, globalLuceneConfig);
            mentionLuceneWriter = new IndexWriter(mentionLuceneIndex, mentionLuceneConfig);
            mentionLuceneReader = IndexReader.open(mentionLuceneWriter, true);
            globalLuceneReader = IndexReader.open(globalLuceneWriter, true);
            loaded = true;
            nbTimeSlices = globalLuceneReader.numDocs();
            globalLuceneIds = new HashMap<>();
            mentionLuceneIds = new HashMap<>();
            for (int i = 0; i < globalLuceneReader.maxDoc() || i < mentionLuceneReader.maxDoc(); i++) {
                Document globalDoc = globalLuceneReader.document(i);
                Document mentionDoc = mentionLuceneReader.document(i);
                if(globalDoc != null) {
                    globalDoc.get("id");
                    globalLuceneIds.put(i,Integer.parseInt(globalDoc.get("id")));
                }
                if(mentionDoc != null) {
                    mentionDoc.get("id");
                    mentionLuceneIds.put(i,Integer.parseInt(mentionDoc.get("id")));
                }
            }
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
        } catch (IOException ex) {
            Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
            Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
        
    public double[] getMentionFrequency(TermDocs termDocs){
        try {
            double[] array = new double[nbTimeSlices];
            while(termDocs.next()){
                int freq = termDocs.freq();
                int doc = termDocs.doc();
                int id = mentionLuceneIds.get(doc);
                array[id-1] = freq;
            }
            return array;
        } catch (IOException ex) {
            Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public double[] getGlobalFrequency(TermDocs termDocs){
        try {
            double[] array = new double[nbTimeSlices];
            while(termDocs.next()){
                int freq = termDocs.freq();
                int doc = termDocs.doc();
                int id = globalLuceneIds.get(doc);
                array[id-1] = freq;
            }
            return array;
        } catch (IOException ex) {
            Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public double[] getMentionFrequency(String term){
        try {
            TermDocs termDocs = mentionLuceneReader.termDocs();
            int numDocs = mentionLuceneReader.numDocs();
            termDocs.seek(new Term("content", term));
                if(term!=null){
                    double[] array = new double[numDocs];
                    while(termDocs.next()){
                        int doc = termDocs.doc();
                        double freq = termDocs.freq();
                        int id = mentionLuceneIds.get(doc);
                        array[id-1] = freq;
                    }
                    return array;
            }else{
                return new double[numDocs];
            }
        } catch (IOException ex) {
            Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public double[] getGlobalFrequency(String term){
        try {
            TermDocs termDocs = globalLuceneReader.termDocs();
            int numDocs = globalLuceneReader.numDocs();
            termDocs.seek(new Term("content", term));
                if(term!=null){
                    double[] array = new double[numDocs+1];
                    while(termDocs.next()){
                        int doc = termDocs.doc();
                        double freq = termDocs.freq();
                        int id = globalLuceneIds.get(doc);
                        array[id-1] = freq;
                    }
                    return array;
            }else{
                return new double[numDocs];
            }
        } catch (IOException ex) {
            Logger.getLogger(Corpus.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public String getMessages(Event simpleEvent){
        try {
            statement = connection.createStatement();
            // Getting messages
            String messages = "";
            ResultSet rs = statement.executeQuery("select msg_text from "+configuration.database+".messages where time_slice>="+simpleEvent.I.timeSliceA+" and time_slice<="+simpleEvent.I.timeSliceB+" and msg_text like '% "+simpleEvent.mainTerm+" %';");
            while(rs.next()){
                messages += rs.getString(1)+"\n";
            }
            statement.close();
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
