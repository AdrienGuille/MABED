package fr.ericlab.mabed.app;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

/**
 * @author Nicolas Dugué
 * 
 * Allows to store the Twitter parameters of a user and get it as a singleton
 *
 */
public class TwitterAccount {

	private static Twitter twitter;
	
	private static void createAccount() {
		twitter= TwitterFactory.getSingleton();
		twitter.setOAuthConsumer(Configuration.getConsumer(), Configuration.getSecretConsumer());
		AccessToken token = new AccessToken(Configuration.getToken(), Configuration.getSecretToken());
		twitter.setOAuthAccessToken(token);
	}
	/**
	 * @return a Twitter4j instance of a authorized account
	 */
	public static Twitter getSingleton() {
		if (twitter == null) {
			createAccount();
		}
		return twitter;
	}
	
}
