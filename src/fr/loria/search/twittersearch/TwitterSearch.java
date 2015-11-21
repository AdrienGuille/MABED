package fr.loria.search.twittersearch;

import java.util.HashSet;

import org.apache.log4j.Logger;

import fr.loria.log.AppLogger;
import fr.loria.search.ISearch;
import twitter4j.MediaEntity;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

/**
 * @author Nicolas Dugué
 * 
 * Provide a way to search in Twitter API
 *
 */
public class TwitterSearch implements ISearch {
	private Twitter twitter;
	private Query query;
	private QueryResult result;
	Logger log = AppLogger.getInstance();

	public TwitterSearch(Twitter twitter) {
		this.twitter = twitter;
	}

	/* (non-Javadoc)
	 * @see fr.loria.search.ISearch#getPictures(java.lang.String)
	 */
	@Override
	public HashSet<String> getPictures(String keywords) {
		HashSet<String> results = new HashSet<String>();
		try {
			search(keywords);
		} catch (TwitterException e) {
			log.debug(e);
			return results;
		}
		log.info("Pictures search on Twitter for request :" + keywords);
		for (Status status : result.getTweets()) {
			for (MediaEntity m : status.getMediaEntities()) {
				results.add(m.getMediaURL());
			}
		}
		return results;
	}

	/* (non-Javadoc)
	 * @see fr.loria.search.ISearch#getWebs(java.lang.String)
	 */
	@Override
	public HashSet<String> getWebs(String keywords) {
		HashSet<String> results = new HashSet<String>();
		try {
			search(keywords);
		} catch (TwitterException e) {
			log.debug(e);
			return results;
		}
		log.info("Twitter search for request :" + keywords+"|");
		for (Status status : result.getTweets()) {
			results.add(status.getText());
		}
		return results;
	}

	private void search(String keywords) throws TwitterException {
		if (!alreadyDone(keywords)) {
			query = new Query(keywords);
			query.setResultType(Query.MIXED);
			result = twitter.search(query);
		}
	}

	private boolean alreadyDone(String keywords) {
		for (String s : keywords.split(" ")) {
			if (query == null || !query.getQuery().contains(s))
				return false;
		}
		return true;
	}

	public static void main(String[] args) {
		Twitter twitter = TwitterFactory.getSingleton();
		twitter.setOAuthConsumer("KRqSTCdB7kmuB4KNlnhc1MAEA", "sw8XMo8HA4QLlv5oPbBWMMIrXF4ezSLI6XHjYWbSCydyEDGjG4");
		AccessToken token = new AccessToken("385008562-VqxMozAfS7YLqcFjp18BPVLt5wOOguKDwdUIKbAp", "OFGahvPFFVQG4fgjYpk4kk26oqX2kcjnyQjTxeEw3gdGk");
		twitter.setOAuthAccessToken(token);
		TwitterSearch search = new TwitterSearch(twitter);
		String keywords="amazing thereaibanksy 5kpbh0tptw supporting ";
		for (String s :search.getPictures(keywords))
			System.out.println(s);
		for (String s :search.getWebs(keywords))
			System.out.println(s);
	}

}
