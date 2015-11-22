package fr.loria.orchestrator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import fr.loria.log.AppLogger;
import fr.loria.writer.WriteFromJar;

public class Orchestrator {
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
		option.setRequired(true);
		option.setType(String.class);
		options.addOption(option);
		option = new Option("ts", "secrettoken", true, "Secret Twitter token");
		option.setRequired(true);
		option.setType(String.class);
		options.addOption(option);
		option = new Option("c", "consumer", true, "Consumer key");
		option.setRequired(true);
		option.setType(String.class);
		options.addOption(option);
		option = new Option("cs", "consumerkey", true, "Secret Consumer key");
		option.setRequired(true);
		option.setType(String.class);
		options.addOption(option);
		option = new Option("keyword", "keyword", true, "Keywords to use to filter the tweet stream");
		option.setRequired(true);
		option.setType(String.class);
		options.addOption(option);
		option = new Option("e", "exp", true, "Experiment Name : ONE WORD ONLY");
		option.setRequired(true);
		option.setType(String.class);
		options.addOption(option);
		option = new Option("m", "minutes", true, "Time interval in minutes. Default : 30.");	
		option.setRequired(false);
		option.setType(Integer.class);
		options.addOption(option);
		option = new Option("period", "period", true, "How many time intervals make a period.");	
		option.setRequired(true);
		option.setType(Integer.class);
		options.addOption(option);
		option = new Option("nt", "thread", true, "Number of Threads");	
		option.setRequired(false);
		option.setType(Integer.class);
		options.addOption(option);
		option = new Option("k", "events", true, "Number of events to detect. Default to 20.");	
		option.setRequired(false);
		option.setType(Integer.class);
		options.addOption(option);
		option = new Option("p", "keywords", true, "Number of keywords per event. Default to 10.");	
		option.setRequired(false);
		option.setType(Integer.class);
		options.addOption(option);
		option = new Option("theta", "theta", true, "Parameter for keyword selection between 0 and 1. Default to 0.7");	
		option.setRequired(false);
		option.setType(Float.class);
		options.addOption(option);
		option = new Option("sigma", "sigma", true, "Parameter to control event redundancy between 0 and 1. Default to 0.5");	
		option.setRequired(false);
		option.setType(Float.class);
		options.addOption(option);
		option = new Option("ms", "minsupport", true, "Parameter for keyword selection between 0 and 1. Default to 0.01");	
		option.setRequired(false);
		option.setType(Float.class);
		options.addOption(option);
		option = new Option("Ms", "maxsupport", true, "Parameter for keyword selection between 0 and 1. Default to 0.1");	
		option.setRequired(false);
		option.setType(Float.class);
		options.addOption(option);
	}
	/**
	 * Print the Command Line Interface Help
	 */
	public static void printHelp() {
		formatter.printHelp("Streaming API", options);
	}
	
	public static void main(String[] args) throws ParseException, IOException {
		createCLI();
		
		try {
			CommandLine line = parser.parse(options, args);
			String ts = line.getOptionValue("ts");
			String t = line.getOptionValue("t");
			String cs = line.getOptionValue("cs");
			String c = line.getOptionValue("c");
			String e= line.getOptionValue("e");
			String m= line.getOptionValue("m");
			String period= line.getOptionValue("period");
			int nt=1;
			if (line.hasOption("nt"))
				nt = Integer.parseInt(line.getOptionValue("nt"));
			int k=20;
			if (line.hasOption("k"))
				k = Integer.parseInt(line.getOptionValue("k"));
			int p=10;
			if (line.hasOption("p"))
				p = Integer.parseInt(line.getOptionValue("p"));
			float theta=0.7f;
			if (line.hasOption("theta"))
				theta = Float.parseFloat(line.getOptionValue("theta"));
			float sigma=0.5f;
			if (line.hasOption("p"))
				sigma = Float.parseFloat(line.getOptionValue("p"));
			float ms=0.01f;
			if (line.hasOption("ms"))
				ms = Float.parseFloat(line.getOptionValue("ms"));
			float Ms=0.1f;
			if (line.hasOption("Ms"))
				Ms = Float.parseFloat(line.getOptionValue("Ms"));
			String keywords= line.getOptionValue("keyword");
			for (String s : line.getArgs())
				keywords += " " + s;
			
			//Creating the input directory
			File theDir = new File(e);
			// if the directory does not exist, create it
			if (!theDir.exists()) {
			    log.info("Creating directory: " + e);
			    boolean result = false;
			    try{
			        theDir.mkdir();
			        result = true;
			    } 
			    catch(SecurityException se){
			    	log.fatal("Fatal error : directory cannot be created - you do not own it");
			    	System.exit(0);
			    }        
			    if(result) {    
			        log.info("Directory " + e+" created");  
			    }
			}
			//Creating the parameters directory
			theDir = new File(e+"_parameters");
			// if the directory does not exist, create it
			if (!theDir.exists()) {
			    log.info("Creating directory: " + e+"_parameters");
			    boolean result = false;
			    try{
			        theDir.mkdir();
			        result = true;
			    } 
			    catch(SecurityException se){
			    	log.fatal("Fatal error : directory cannot be created - you do not own it");
			    	System.exit(0);
			    }        
			    if(result) {    
			        log.info("Directory " + e+"_parameters"+" created");  
			    }
			}
			//Put the stopwords into the parameters directory
			WriteFromJar.createFile("stopwords.txt", e+"_parameters");
			
			//Put the parameters into the parameters directory
			FileWriter fw = new FileWriter(new File(e+"_parameters/parameters.txt"));
			fw.write("exp = "+e+"\n");
			fw.write("prepareCorpus=true\n");
			fw.write("timeSliceLength="+m+"\n");
			fw.write("numberOfThreads = "+nt+"\n");
			fw.write("k = "+k+"\n");
			fw.write("p = "+p+"\n");
			fw.write("theta = "+theta+"\n");
			fw.write("sigma = "+sigma+"\n");
			fw.write("stopwords = "+e+"_parameters/stopwords.txt\n");
			fw.write("minSupport = "+ms+"\n");
			fw.write("maxSupport = "+Ms+"\n");
			fw.write("twitter = true\n");
			fw.write("consumerKey = "+c+"\n");
			fw.write("secretConsumerKey = "+cs+"\n");
			fw.write("token = "+t+"\n");
			fw.write("secretToken = "+ts+"\n");
			fw.write("keywords = "+keywords+"\n");
			fw.write("period = "+period+"\n");
			fw.close();
			log.info("File " + e+"_parameters/parameters.txt created : it sums up the experiment parameters");
			
			Runnable r = new StreamingThread(cs, c, ts, t, m, e, keywords);
			Thread streaming = new Thread (r);
			streaming.start();
			
			r = new MabedThread(Integer.parseInt(m), Integer.parseInt(period), e+"_parameters/parameters.txt", e);
			Thread mabed = new Thread(r);
			mabed.start();
			
			
			
			
		}
		catch (MissingOptionException e) {
			log.fatal("Missing parameters : " + e.getLocalizedMessage());
			printHelp();
		}
	}
}
