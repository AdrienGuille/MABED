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
        private String coordinates;
        private String language;
	private Logger log = AppLogger.getInstance();
	
	public StreamingThread(String cs, String c, String ts, String t, String nbMinutes, String exp, String keys, String geo, String lang) {
		super();
		this.cs = cs;
		this.c = c;
		this.ts = ts;
		this.t = t;
		this.nbMinutes = nbMinutes;
		this.exp = exp;
                if(keys.length() > 0)
                    this.keywords = keys;
                else
                    this.keywords = null;
                if(geo.length() > 0){
                    this.coordinates = geo;
                }else{
                    this.coordinates = null;
                }
                this.language = lang;
	}

	@Override
	public void run() {
		Runtime rt = Runtime.getRuntime();
		try {
			ProcessBuilder pb = null;
                        if(this.keywords != null){
                                log.info("keywords: "+keywords);
                                pb = new ProcessBuilder("java","-jar","mabed-0.1-getTweets.jar","-c",c ,"-cs", cs,"-t", t,"-ts",ts,"-m",nbMinutes,"-e", exp,"-k",keywords,"-lang",language);
                        }else{
                            if(this.coordinates != null){
                                log.info("Coordinates: "+coordinates);
                                pb = new ProcessBuilder("java","-jar","mabed-0.1-getTweets.jar","-c",c ,"-cs", cs,"-t", t,"-ts",ts,"-m",nbMinutes,"-e", exp,"-geo",coordinates,"-lang",language);
                            }
                        }
			pb.redirectOutput(Redirect.INHERIT);
			pb.redirectError(Redirect.INHERIT);
			Process p = pb.start();
			log.info("Streaming running...");
		} catch (IOException e) {
			log.fatal(e);
		}
		
	}

}
