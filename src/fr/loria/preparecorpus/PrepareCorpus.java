package fr.loria.preparecorpus;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Scanner;

import fr.loria.date.MabedDateFormat;

/**
 * @author Nicolas Dugué
 * 
 * Can be used to slice a corpus a posteriori, if the stream logged tweet in only one file
 *
 */
public class PrepareCorpus {
	private static String file;
	private static int nbMinutes;
	public static String getFile() {
		return file;
	}
	public static void setFile(String f) {
		if (f.contains(".text") || (f.contains(".time"))) {
			f=f.substring(0, f.lastIndexOf("."));
		}
		file = f;
	}
	public static int getNbMinutes() {
		return nbMinutes;
	}
	public static void setNbMinutes(int nbM) {
		nbMinutes = nbM;
	}
	
	
	/**
	 * Cut a corpus of tweets obtained via Streaming but gathered in only one file
	 * 
	 * @throws ParseException
	 * @throws IOException
	 */
	public static void cutCorpusInSlices() throws ParseException, IOException{
		Scanner scTxt= new Scanner(new File(file+".text"));
		Scanner scTime = new Scanner(new File(file+".time"));
		DateFormat dateFormat = MabedDateFormat.getDateFormat();
		NumberFormat formatter = FileNameFormatter.getFormatter();
		int fileId=0;
		Calendar start = Calendar.getInstance();
		FileWriter fwTxt, fwTime;
		String timeLine;
		if (scTime.hasNextLine()) {
			fwTxt = new FileWriter(new File(formatter.format(fileId)+".text"));
			fwTime = new FileWriter(new File(formatter.format(fileId)+".time"));
			timeLine=scTime.nextLine();
			start.setTime(dateFormat.parse(timeLine));
			Calendar others=Calendar.getInstance();
			others.setTime(dateFormat.parse(timeLine));
			start.add(Calendar.MINUTE, nbMinutes);
			fwTime.write(timeLine+"\n");
			fwTxt.write(scTxt.nextLine()+"\n");
			boolean ok=false;
			while (scTime.hasNextLine()) {
				timeLine=scTime.nextLine();
				others.setTime(dateFormat.parse(timeLine));
				
				ok=true;
				start.set(Calendar.HOUR_OF_DAY, start.get(Calendar.HOUR));
				others.set(Calendar.HOUR_OF_DAY, others.get(Calendar.HOUR));
				if (start.get(Calendar.HOUR_OF_DAY) == 0 && others.get(Calendar.HOUR_OF_DAY) == 11) {
					ok=false;
				}
				
				if (others.after(start) && ok) {
					fwTxt.close();
					fwTime.close();
					fileId++;
					fwTxt = new FileWriter(new File(formatter.format(fileId)+".text"));
					fwTime = new FileWriter(new File(formatter.format(fileId)+".time"));
					start.add(Calendar.MINUTE, nbMinutes);
				}
				fwTime.write(timeLine+"\n");
				fwTxt.write(scTxt.nextLine()+"\n");
			}
			try {
				fwTxt.close();
				fwTime.close();
			}
			catch (Exception e) {}
		}
		scTime.close();
		scTxt.close();
	}
	
	public static void main(String[] args) throws ParseException, IOException {
		PrepareCorpus.setFile(args[0]);
		PrepareCorpus.setNbMinutes(Integer.parseInt(args[1]));
		PrepareCorpus.cutCorpusInSlices();
	}
	

}
