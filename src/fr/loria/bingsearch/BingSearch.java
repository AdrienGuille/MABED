package fr.loria.bingsearch;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

public class BingSearch {
	
	private static String urlStart="https://api.datamarket.azure.com/Bing/Search/Image?Query=%27";
	private static String urlEnd="%27&$format=JSON";
	private static String accountKey = "wAxAjiq/DYP5HeDuUgyIWz7m5ZEv/pRmpOVchDvxc7w";
	
	public static LinkedList<String> getPictures(String keywords) throws IOException {
		LinkedList<String> list = new LinkedList<String>();
		String bingUrl=urlStart+java.net.URLEncoder.encode(keywords)+urlEnd;

	    byte[] accountKeyBytes = Base64.encodeBase64((accountKey + ":" + accountKey).getBytes()); // code for encoding found on stackoverflow
	    String accountKeyEnc = new String(accountKeyBytes);

	    URL url = new URL(bingUrl);
	    URLConnection urlConnection = url.openConnection();
	    String s1 = "Basic " + accountKeyEnc;
	    urlConnection.setRequestProperty("Authorization", s1);
	    BufferedReader in = new BufferedReader(new InputStreamReader(
	        urlConnection.getInputStream()));
	    String inputLine;
	    StringBuffer sb = new StringBuffer();
	    while ((inputLine = in.readLine()) != null)
	      sb.append(inputLine);
	    in.close();
	    JSONObject json = new JSONObject(sb.toString());
	    json = (JSONObject) json.get("d");
	    JSONArray results = (JSONArray) json.get("results");
	    Iterator it = results.iterator();
	    JSONObject objet;
	    while (it.hasNext()) {
	    	objet=(JSONObject) it.next();
	    	list.add(objet.getString("MediaUrl"));
	    }
	    return list;
	    	
	}
	
	public static void main(String[] args) throws IOException {
		BingSearch.getPictures("caca");
	}
}
