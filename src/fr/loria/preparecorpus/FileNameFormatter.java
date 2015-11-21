package fr.loria.preparecorpus;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * @author Nicolas Dugué
 * 
 * Describe how the files are formatted
 *
 */
public class FileNameFormatter {
	public static NumberFormat formatter = new DecimalFormat("00000000");

	public static NumberFormat getFormatter() {
		return formatter;
	}

	public static void setFormatter(NumberFormat formatter) {
		FileNameFormatter.formatter = formatter;
	}
}
