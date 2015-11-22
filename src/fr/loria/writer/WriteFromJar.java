package fr.loria.html;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import fr.ericlab.mabed.app.Configuration;
import fr.loria.log.AppLogger;


/**
 * @author Nicolas Dugué
 *	Allows To write the HTML, JS and CSS files allowing the correct visualization of the HTML Report
 */
public class WriteHtml {
	private static WriteHtml m = new WriteHtml();
	private static Logger log = AppLogger.getInstance();
	public static void writeHtml() {
		log.info("Writing stylesheets for the html report");
		createFile("1-col-portfolio.css");
		createFile("bootstrap.css");
		log.info("Writing of the stylesheets ended");
	}
	private static String createFile(String s) {
		File file=null;
		 try {
	            InputStream input = m.getClass().getClassLoader().getResourceAsStream(s);
	            file= new File(Configuration.getPathOutput()+"/"+s);
	            OutputStream out = new FileOutputStream(file);
	            int read;
	            byte[] bytes = new byte[1024];

	            while ((read = input.read(bytes)) != -1) {
	                out.write(bytes, 0, read);
	            }
	            out.close();
	        } catch (IOException ex) {
	            log.fatal(ex);
	        }
		 return file.getAbsolutePath();
	}
}
