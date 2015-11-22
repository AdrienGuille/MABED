package fr.loria.orchestrator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class CopyFile {
	public static void copy(String path,String input, String output) throws IOException {
		Scanner sc = new Scanner(new File(path+"/"+input));
		FileWriter fw = new FileWriter(new File(path+"/"+output));
		while (sc.hasNextLine()) {
			fw.write(sc.nextLine()+"\n");
		}
		sc.close();
		fw.close();
	}
}
