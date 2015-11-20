package fr.loria.twitterstream;

import java.io.IOException;

public class HookThread extends Thread {

	@Override
	public void run() {
		try {
			HTMLLogger.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			MabedWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
