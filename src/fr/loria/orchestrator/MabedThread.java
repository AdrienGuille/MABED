package fr.loria.orchestrator;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;

import org.apache.log4j.Logger;

import fr.loria.log.AppLogger;

public class MabedThread implements Runnable {

	private int nbMinutes;
	private int period;
	private int cptPeriod=0;
	private int nbPeriodElapsed=0;
	private String parameters;
	private String exp;
	private Logger log = AppLogger.getInstance();
	
	public MabedThread(int nbMinutes, int period, String parameters, String outputPath) {
		super();
		this.nbMinutes = nbMinutes;
		this.period = period;
		this.parameters = parameters;
		this.exp = outputPath;
	}
	
	@Override
	public void run() {
		Runtime rt = Runtime.getRuntime();
		try {
			while (true) {
				log.info("Mabed Waiting for enough tweets in the period...");
				while (cptPeriod < period) {
					log.info("Waiting " + (period -cptPeriod) +" * " + nbMinutes +" minutes");
					try {
						Thread.sleep(nbMinutes*60*1000);
					} catch (InterruptedException e) {
					}
					cptPeriod++;
				}
				log.info("Running mabed "+ parameters);
				ProcessBuilder pb = new ProcessBuilder("mabed", parameters);
				pb.redirectOutput(Redirect.INHERIT);
				pb.redirectError(Redirect.INHERIT);
				Process p = pb.start();
				try {
					p.waitFor();
				} catch (InterruptedException e) {
				}
				log.info("Execution terminated");
				cptPeriod=0;
				CopyFile.copy(exp+"_output", "MABED.html", "MABED-"+nbPeriodElapsed+".html");
				nbPeriodElapsed++;
			}
		} catch (IOException e) {
			log.fatal(e);
		}
		
	}



}
