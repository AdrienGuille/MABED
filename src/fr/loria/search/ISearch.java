package fr.loria.search;

import java.util.Set;

/**
 * @author Nicolas Dugué
 * 
 * Search abstraction
 *
 */
public interface ISearch {
	/**
	 * Get a list of pictures matching with the keywords provided
	 * @param keywords from an event
	 * @return a Set of url String
	 */
	public Set<String> getPictures(String keywords);
	
	/**
	 * Get a list of descriptions matching with the keywords provided
	 * @param keywords from an event
	 * @return a Set of String descriptions
	 */
	public Set<String> getWebs(String keywords);
	
}
