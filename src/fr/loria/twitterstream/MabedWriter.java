package fr.loria.twitterstream;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;

import org.apache.log4j.Logger;

import fr.loria.date.MabedDateFormat;
import fr.loria.log.AppLogger;
import fr.loria.preparecorpus.FileNameFormatter;

/**
 * @author Nicolas Dugué
 *
 *	Used to write the tweet gathered by the streaming API
 */
public class MabedWriter {
	private static FileWriter fwtext;
	private static FileWriter fwtime;
	private static DateFormat dateFormat;
	private static Calendar c;
	private static int fileId=0; 
	private static String fileName;
	private static Logger logger;
	private static void createLogger(String filename) throws IOException {
		logger = AppLogger.getInstance();
		File theDir = new File(filename);
		// if the directory does not exist, create it
		if (!theDir.exists()) {
		    logger.info("Creating directory: " + filename);
		    boolean result = false;
		    try{
		        theDir.mkdir();
		        result = true;
		    } 
		    catch(SecurityException se){
		    	logger.fatal("Fatal error : directory cannot be created - you do not own it");
		    	System.exit(0);
		    }        
		    if(result) {    
		        logger.info("Directory " + filename+" created");  
		    }
		}

		MabedWriter.fileName=filename;
		dateFormat=MabedDateFormat.getDateFormat();
		
		createMabedFiles();
	}
	
	public static synchronized void logTweet(String msg, String filename) throws IOException{
		if (fwtext == null) {
			createLogger(filename);
		}
		fwtext.write(msg+"\n");
		c =Calendar.getInstance();
		fwtime.write(dateFormat.format(c.getTime())+"\n");
	}
	public static void close() throws IOException {
		fwtext.close();
		fwtime.close();
	}
	private static void createMabedFiles() throws IOException {
		String txtName = fileName+"/"+FileNameFormatter.getFormatter().format(fileId)+".text";
		String timeName=fileName+"/"+FileNameFormatter.getFormatter().format(fileId)+".time";
		fwtext = new FileWriter(new File(txtName));
		fwtime=new FileWriter(new File(timeName));
		logger.info(txtName + " created > records the textual content of your tweets");
		logger.info(timeName+ " created > records the timestamp of your tweets");
	}
	public static synchronized void closeAndReopen() throws IOException {
		close();
		fileId++;
		createMabedFiles();
	}
}
