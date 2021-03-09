package fr.fne.batch.services.test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.scribejava.apis.MediaWikiApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;

@Service
public class OAuth1ThreeStepsConnexion {
	
	private static final Logger logger = Logger.getLogger(OAuth1ThreeStepsConnexion.class);


	// To get your consumer key/secret, see https://meta.wikimedia.org/wiki/Special:OAuthConsumerRegistration/propose
	// Pour supprimer un accès : http://fagonie-dev.v102.abes.fr:8181/wiki/Special:OAuthManageMyGrants
	
	//Compte : 
    private static final String CONSUMER_KEY = "3c46bf77c3929714e12296a751db6ec7";
    private static final String CONSUMER_SECRET = "a8ed920c0c9cccb9befa114c3e4e0e8f4a99adb3";
    
    private static final String API_USERINFO_URL = "http://fagonie-dev.v102.abes.fr:8181/w/api.php?action=query&format=json&meta=userinfo";

    
	// Connexion à OAuth1 avec demande à l'utilisateur
	public void go(String urlWikiBase) {
		try {
	    	MediaWikiApi mediaWiki = new MediaWikiApi("http://fagonie-dev.v102.abes.fr:8181/w/index.php", "http://fagonie-dev.v102.abes.fr:8181/wiki/");

			OAuth10aService service = new ServiceBuilder(CONSUMER_KEY).apiSecret(CONSUMER_SECRET).build(mediaWiki);
	        Scanner in = new Scanner(System.in);

	        logger.info("=== MediaWiki's OAuth Workflow ===");

	        // Obtain the Request Token
	        logger.info("Fetching the Request Token...");
	        OAuth1RequestToken requestToken = service.getRequestToken();
	        logger.info("Got the Request Token!");

	        logger.info("Now go and authorize ScribeJava here:");
	        logger.info(service.getAuthorizationUrl(requestToken));
	        logger.info("And paste the verifier here");
	        logger.info(">>");
	        final String oauthVerifier = in.nextLine();

	        // Trade the Request Token and Verifier for the Access Token
	        logger.info("Trading the Request Token for an Access Token...");
	        final OAuth1AccessToken accessToken = service.getAccessToken(requestToken, oauthVerifier);
	        logger.info("Got the Access Token!");
	        logger.info("(The raw response looks like this: " + accessToken.getRawResponse() + "')");

	        // Now let's go and ask for a protected resource!
	        logger.info("Now we're going to access a protected resource...");
	        final OAuthRequest request = new OAuthRequest(Verb.GET, API_USERINFO_URL);
	        service.signRequest(accessToken, request);
	        try (Response response = service.execute(request)) {
	            logger.info("Got it! Lets see what we found...");
	            logger.info(response.getBody());
	        }

	        logger.info("Thats it man! Go and build something awesome with MediaWiki and ScribeJava! :)");
		}
		catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}		
	}

}
