package fr.loria.date;

import java.text.SimpleDateFormat;

/**
 * @author Nicolas Dugué
 *
 *	Allows to specify in one class how dates are supposed to be formatted
 */
public class MabedDateFormat {
	public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	public static SimpleDateFormat dateFormatResult = new SimpleDateFormat("MM-dd HH:mm");

	public static SimpleDateFormat getDateFormat() {
		return dateFormat;
	}
	public static SimpleDateFormat getDateFormatResult() {
		return dateFormatResult;
	}

	
}
