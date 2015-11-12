package fr.loria.twitterstream;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

public class Streaming {

	public static void main(String[] args) throws TwitterException, IOException{
		//Twitter twitter = TwitterFactory.getSingleton();
	    StatusListener listener = new StatusListener(){
	        public void onStatus(Status status) {
	        	String s = status.getText().replace("\n", " ");
				HTMLLogger.logTweet(s);
				try {
					MabedLogger.logTweet(s);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
	        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
	        public void onException(Exception ex) {
	            ex.printStackTrace();
	        }
			public void onScrubGeo(long arg0, long arg1) {
			}
			public void onStallWarning(StallWarning arg0) {
			}
	    };
	    Runtime.getRuntime().addShutdownHook(new HookThread());
	    TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
	    twitterStream.addListener(listener);
	    // sample() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.
	    twitterStream.filter("#france");
	   /* Scanner sc = new Scanner(System.in);
	    String s="";
	    while (!s.equals("q")) {
	    	s=sc.next();
	    }
	    HTMLLogger.close();
	    MabedLogger.close();*/
	    
	}
}
