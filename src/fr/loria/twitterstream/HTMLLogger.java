package fr.loria.twitterstream;

import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.HTMLLayout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.WriterAppender;

import fr.loria.log.AppLogger;

/**
 * @author nicolas
 *
 *	Was useful to visualize the tweet gathered
 *  However, it is hard to custom the html template of log4j... It thus makes it a bit useless.
 */
public class HTMLLogger {
	private static Logger log;
	private static FileOutputStream output;
	private static void createXMLLogger(String filename) {
		log = Logger.getLogger(HTMLLogger.class);
		WriterAppender appender = null;
	    try {
	    	System.out.println("Logger creation...");
	    	output = new FileOutputStream(filename+".html");
	    	HTMLLayout layout = new HTMLLayout();
	    	appender = new WriterAppender(layout, output);
			log.addAppender(appender);
			log.setAdditivity(false);
			log.setLevel(Level.ALL);
			Logger logger = AppLogger.getInstance();
			logger.info(filename+".html created > records all your tweets");
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
	public static void logTweet(String msg, String filename) {
		if (log == null) {
			createXMLLogger(filename);
		}
		log.info(msg);
	}
	public static void close() throws IOException {
		output.close();
	}
}
