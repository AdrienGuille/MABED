package fr.loria.twitterstream;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import fr.loria.log.AppLogger;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;

/**
 * @author Nicolas Dugué
 *
 *	Main Class used to collect tweets
 */
public class Streaming {

	private static HelpFormatter formatter = new HelpFormatter();
	private static CommandLineParser parser = new DefaultParser();
	private static Options options = new Options();
	private static Logger log = AppLogger.getInstance();
	
	/**
	 * 	Allows to create the Streaming Command Line Interface
	 */
	public static void createCLI() {
		options.addOption("h", "help", false, "print this message");
		Option option = new Option("t", "token", true, "Twitter token");
		options.addOption(option);
		option = new Option("ts", "secrettoken", true, "Secret Twitter token");
		options.addOption(option);
		option = new Option("c", "consumer", true, "Consumer key");
		options.addOption(option);
		option = new Option("cs", "consumerkey", true, "Secret Consumer key");
		options.addOption(option);
		option = new Option("k", "keyword", true, "Keywords to use to filter the tweet stream");
		options.addOption(option);
		option = new Option("e", "exp", true, "Experiment Name : ONE WORD ONLY");
		options.addOption(option);
		option = new Option("m", "minutes", true, "Time interval in minutes. Default : 30.");
		options.addOption(option);
	}
	/**
	 * Allows to check whether or not the parameters necessary to run the stream collection were correctly filled 
	 * 
	 * @param line The Command Line interface with its options
	 * @return true if the parameters are correct
	 */
	public static boolean check(CommandLine line) {
		boolean okay=true;
		if (!line.hasOption("ts")) {
			log.warn("You need to provide a secret token");
			okay=false;
		}
		if (!line.hasOption("t")) {
			log.warn("You need to provide a token");
			okay=false;
		}
		if (!line.hasOption("c")) {
			log.warn("You need to provide a consumer key");
			okay=false;
		}
		if (!line.hasOption("cs")) {
			log.warn("You need to provide a secret consumer key");
			okay=false;
		}
		if (!line.hasOption("k")) {
			log.warn("You need to provide keywords : 5 at most");
			okay=false;
		}
		if (!line.hasOption("e")) {
			log.warn("You need to provide an experiment name : ONE WORD ONLY");
			okay=false;
		}
		if (!okay)
			printHelp();
		return okay;

	}
	/**
	 * Print the Command Line Interface Help
	 */
	public static void printHelp() {
		formatter.printHelp("Streaming API", options);
	}
	
	public static void main(String[] args) throws TwitterException, IOException, ParseException{
		//Create the Command Line Interface
		createCLI();
		//Parse the arguments filled by the user
		CommandLine line = parser.parse(options, args);
		//Check if the arguments were correctly filled
		boolean process = check(line);
		if (process) {
			//Get the arguments
			String consumer = line.getOptionValue("c");
			String consumerSecret = line.getOptionValue("cs");
			String token = line.getOptionValue("t");
			String tokenSecret = line.getOptionValue("ts");
			String keywords= line.getOptionValue("k");
			for (String s : line.getArgs())
				keywords += " " + s;
			final String exp = line.getOptionValue("e");
			final int minutes;
			if (line.hasOption("m"))
				minutes=Integer.parseInt(line.getOptionValue("m"));
			else
				minutes=30;
			
			//Used to log the stream
		    StatusListener listener = new StatusListener(){
		        public void onStatus(Status status) {
		        	String s = status.getText().replace("\n", " ");
					//HTMLLogger.logTweet(s, exp);
					try {
						MabedWriter.logTweet(s, exp);
					} catch (IOException e) {
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
		    //Used to close the files when the app is shut down
		    //A hook thread is a thred called just before the app shut down
		    Runtime.getRuntime().addShutdownHook(new HookThread());
		    TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
		    //Set the consumer keys and token
		    twitterStream.setOAuthConsumer(consumer, consumerSecret);
		    AccessToken act = new AccessToken(token,tokenSecret);
		    twitterStream.setOAuthAccessToken(act);
		    twitterStream.addListener(listener);
		    log.info("Stream opened on : " + keywords);
		    ChangeFileThread cf = new ChangeFileThread(minutes);
		    Thread t = new Thread(cf);
		    t.start();
		    twitterStream.filter(keywords);
		}
	}
}
