package fr.loria.twitterstream;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import fr.loria.date.MabedDateFormat;
import fr.loria.log.AppLogger;

/**
 * @author nicolas
 *
 *	Used to write the tweet gathered by the streaming API
 */
public class MabedWriter {
	private static FileWriter fwtext;
	private static FileWriter fwtime;
	private static DateFormat dateFormat;
	private static Calendar c;
    Date date = new Date();
    String dateString = dateFormat.format(date); 
	
	private static void createLogger(String filename) throws IOException {
		fwtext = new FileWriter(new File(filename+".text"));
		fwtime=new FileWriter(new File(filename+".time"));
		dateFormat=MabedDateFormat.getDateFormat();
		Logger logger = AppLogger.getInstance();
		logger.info(filename+".text created > records the textual content of your tweets");
		logger.info(filename+".time created > records the timestamp of your tweets");
	}
	
	public static void logTweet(String msg, String filename) throws IOException{
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
}
