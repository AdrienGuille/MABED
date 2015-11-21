package fr.loria.search.bingsearch;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.HashSet;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

import fr.loria.search.ISearch;

/**
 * @author Nicolas Dugué
 * 
 * Bing results are bad results : pretty useless class...
 *
 */
public class BingSearch implements ISearch{
	
	private static String urlStartPictures="https://api.datamarket.azure.com/Bing/Search/Image?Query=%27";
	private static String urlStartWebs="https://api.datamarket.azure.com/Bing/Search/Web?Query=%27";
	private static String urlEnd="%27&$format=JSON";
	
	//Need to be provided
	private static String accountKey = "";
	
	
	private static HashSet<String> getObjects(String keywords, String urlStart) throws IOException {
		HashSet<String> list = new HashSet<String>();
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
	    	if (urlStart.equals(BingSearch.urlStartPictures))
	    		list.add(objet.getString("MediaUrl"));
	    	else
	    		list.add(objet.getString("Description"));
	    }
	    return list; 	
	}
	
	/* (non-Javadoc)
	 * @see fr.loria.search.ISearch#getPictures(java.lang.String)
	 */
	public HashSet<String> getPictures(String keywords) {
		try {
			return BingSearch.getObjects(keywords, BingSearch.urlStartPictures);
		} catch (IOException e) {
		}
		return new HashSet<String>();
	}
	/* (non-Javadoc)
	 * @see fr.loria.search.ISearch#getWebs(java.lang.String)
	 */
	public HashSet<String> getWebs(String keywords){
		try {
			return BingSearch.getObjects(keywords, BingSearch.urlStartWebs);
		} catch (IOException e) {
		}
		return new HashSet<String>();
	}
	
}
