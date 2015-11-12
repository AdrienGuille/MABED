package fr.loria.twitterstream;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MabedLogger {
	private static FileWriter fwtext;
	private static FileWriter fwtime;
	private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	private static Calendar c;
    Date date = new Date();
    String dateString = dateFormat.format(date); 
	
	private static void createLogger() throws IOException {
		fwtext = new FileWriter(new File("input/00000000.text"));
		fwtime=new FileWriter(new File("input/00000000.time"));
	}
	
	public static void logTweet(String msg) throws IOException{
		if (fwtext == null) {
			createLogger();
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
