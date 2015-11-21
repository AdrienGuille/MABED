package fr.loria.twitterstream;

import java.io.IOException;

/**
 * @author Nicolas Dugué
 * 
 * A Hook Thread is a thread that is runned when the program is shut down
 * Allows to close the file containing tweets while shutting down the Streaming app
 *
 */
public class HookThread extends Thread {

	@Override
	public void run() {
		try {
			MabedWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
