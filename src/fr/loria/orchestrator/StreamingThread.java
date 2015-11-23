package fr.loria.orchestrator;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;

import org.apache.log4j.Logger;

import fr.loria.log.AppLogger;

public class StreamingThread implements Runnable{

	private String cs;
	private String c;
	private String ts;
	private String t;
	private String nbMinutes;
	private String exp;
	private String keywords;
	private Logger log = AppLogger.getInstance();
	
	public StreamingThread(String cs, String c, String ts, String t, String nbMinutes, String exp, String keywords) {
		super();
		this.cs = cs;
		this.c = c;
		this.ts = ts;
		this.t = t;
		this.nbMinutes = nbMinutes;
		this.exp = exp;
		this.keywords = keywords;
	}

	@Override
	public void run() {
		Runtime rt = Runtime.getRuntime();
		try {
			ProcessBuilder pb = new ProcessBuilder("java","-jar","mabed-0.1-getTweets.jar","-c",c ,"-cs", cs,"-t", t,"-ts",ts,"-m",nbMinutes,"-e", exp,"-k",keywords);
			pb.redirectOutput(Redirect.INHERIT);
			pb.redirectError(Redirect.INHERIT);
			Process p = pb.start();
			log.info("Streaming running...");
		} catch (IOException e) {
			log.fatal(e);
		}
		
	}

}
