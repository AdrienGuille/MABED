package fr.loria.twitterstream;

import java.io.IOException;

import org.apache.log4j.Logger;

import fr.loria.log.AppLogger;

/**
 * @author Nicolas Dugué
 * 
 * Used to slice the corpus in time interval files
 * Each {@link nbMinutes}, new ".text" and ".time" files are created to respect the corpus partition
 *
 */
public class ChangeFileThread implements Runnable {

	private int nbMinutes;
	private Logger log;
	public ChangeFileThread(int nbMinutes) {
		this.nbMinutes=nbMinutes;
		log=AppLogger.getInstance();
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while (true) {
			try {
				//Waiting nbMinutes *60000 milliseconds
				Thread.sleep(this.nbMinutes*60*1000);
			} catch (InterruptedException e) {
			}
			//Then change the fileName
			log.info(this.nbMinutes+ " minute(s) elapsed : new files have to be created to respect the corpus partition");
			try {
				MabedWriter.closeAndReopen();
			} catch (IOException e) {
				log.fatal("Files could not be created");
				System.exit(0);
			}
		}
		
	}

}
