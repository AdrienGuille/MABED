package fr.loria.search;

import java.util.HashSet;

/**
 * @author Nicolas Dugué
 * 
 * If no search tool is provided by the user, this one is provided to the app.
 *
 */
public class EmptySearch implements ISearch{

	@Override
	public HashSet<String> getPictures(String keywords) {
		HashSet<String> l = new HashSet<String>();
		l.add("http://hellocomic.com/images/no_image_available.png");
		return l;
	}

	@Override
	public HashSet<String> getWebs(String keywords) {
		HashSet<String> l = new HashSet<String>();
		l.add("No more description. Fill your twitter Ids to get pictures and search results");
		return l;
		
	}

}
