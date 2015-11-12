package fr.loria.twitterstream;

import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.HTMLLayout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.WriterAppender;

public class HTMLLogger {
	private static Logger log;
	private static FileOutputStream output;
	private static void createXMLLogger() {
		log = Logger.getLogger(HTMLLogger.class);
		WriterAppender appender = null;
	    try {
	    	System.out.println("Logger creation...");
	    	output = new FileOutputStream("tweets.html");
	    	HTMLLayout layout = new HTMLLayout();
	    	appender = new WriterAppender(layout, output);
			log.addAppender(appender);
			log.setAdditivity(false);
			log.setLevel(Level.ALL);
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
	public static void logTweet(String msg) {
		if (log == null) {
			createXMLLogger();
		}
		log.info(msg);
	}
	public static void close() throws IOException {
		output.close();
	}
}
