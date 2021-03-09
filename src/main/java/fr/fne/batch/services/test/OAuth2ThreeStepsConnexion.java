package fr.fne.batch.services.test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.scribejava.apis.MediaWikiApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.github.scribejava.core.oauth.OAuth20Service;

@Service
public class OAuth2ThreeStepsConnexion {
	
	private static final Logger logger = Logger.getLogger(OAuth2ThreeStepsConnexion.class);


	// To get your consumer key/secret, see https://meta.wikimedia.org/wiki/Special:OAuthConsumerRegistration/propose
	// Pour supprimer un accès : http://fagonie-dev.v102.abes.fr:8181/wiki/Special:OAuthManageMyGrants
		
	//Compte qui fonctionne en OAuth1 : 
    private static final String CONSUMER_KEY = "3c46bf77c3929714e12296a751db6ec7";
    private static final String CONSUMER_SECRET = "a8ed920c0c9cccb9befa114c3e4e0e8f4a99adb3";
    
    private static final String API_USERINFO_URL = "http://fagonie-dev.v102.abes.fr:8181/w/api.php?action=query&format=json&meta=userinfo";

    
    // Test OAuth 2
    // Partie de : https://github.com/scribejava/scribejava/blob/master/scribejava-apis/src/test/java/com/github/scribejava/apis/examples/LinkedIn20Example.java
	// https://www.mediawiki.org/wiki/Topic:Vmwiwu073xo33eh5
    // https://github.com/scribejava/scribejava/blob/7d128d31a4dfe3bee7e3edd5732cf4fa19d0c89e/scribejava-core/src/main/java/com/github/scribejava/core/builder/api/DefaultApi20.java#L33
    
    // https://www.mediawiki.org/wiki/OAuth/For_Developers#OAuth_2
    
    // https://www.mediawiki.org/wiki/Manual:$wgEnableRestAPI ) à partir de la 1.35 (14/07/2020) c'est true par défaut.
    // 
    
    public void go(String urlWikiBase) {
		try {
						
	        final OAuth20Service service = new ServiceBuilder(CONSUMER_KEY)
	                .apiSecret(CONSUMER_SECRET)
	                //.defaultScope("r_liteprofile r_emailaddress") // replace with desired scope
	                .callback("http://localhost/callback")
	                .build(MediaWiki20API.instance()); //MediaWiki20API : création de cette classe dans ce projet, pour essayer le OAuth2 avec Scribe. Donc url Ok pour fagonie
	        final Scanner in = new Scanner(System.in);

	        System.out.println("=== MediaWiki's OAuth Workflow ===");
	        System.out.println();

	        // Obtain the Authorization URL
	        System.out.println("Fetching the Authorization URL...");
	        final String secretState = "secret" + new Random().nextInt(999_999);
	        final String authorizationUrl = service.getAuthorizationUrl(secretState);
	        
	        //Message : 
	        //Set $wgEnableRestAPI to true to enable the experimental REST API
	        //Ok pr le paramètre, mais pb ensuite : on dirait que l'extension n'est pas présente sur fagonie-dev. Version 1.34. Pas les params OAuth2 dans le LocalSettings.php
	        //http://fagonie-dev.v102.abes.fr:8181/w/rest.php/oauth2/authorize?response_type=code&client_id=3c46bf77c3929714e12296a751db6ec7&redirect_uri=http%3A%2F%2Flocalhost%2Fcallback&scope=r_liteprofile%20r_emailaddress&state=secret80674
	        //Code 404 : The requested relative path (/oauth2/authorize) did not match any known handler
	        //
	        //Liste des extensions installées : http://fagonie-dev.v102.abes.fr:8181/wiki/Special:Version . Apparemment y'a pas le OAuth2 dans la version qu'on a ?
	        //https://www.mediawiki.org/wiki/Extension:OAuth
	        //Doc sur les groupes utilisateurs : https://www.mediawiki.org/wiki/Extension:OAuth#User_rights
	        //
	        
	        System.out.println("Got the Authorization URL!");
	        System.out.println("Now go and authorize ScribeJava here:");
	        System.out.println(authorizationUrl);
	        System.out.println("And paste the authorization code here");
	        System.out.print(">>");
	        final String code = in.nextLine();
	        System.out.println();

	        System.out.println("Trading the Authorization Code for an Access Token...");
	        final OAuth2AccessToken accessToken = service.getAccessToken(code);
	        System.out.println("Got the Access Token!");
	        System.out.println("(The raw response looks like this: " + accessToken.getRawResponse() + "')");
	        System.out.println();

	        /*System.out.println("Now we're going to get the email of the current user...");
	        final OAuthRequest emailRequest = new OAuthRequest(Verb.GET, PROTECTED_EMAIL_RESOURCE_URL);
	        emailRequest.addHeader("x-li-format", "json");
	        emailRequest.addHeader("Accept-Language", "ru-RU");
	        service.signRequest(accessToken, emailRequest);
	        System.out.println();
	        try (Response emailResponse = service.execute(emailRequest)) {
	            System.out.println(emailResponse.getCode());
	            System.out.println(emailResponse.getBody());
	        }
	        System.out.println();*/

	        System.out.println("Now we're going to access a protected profile resource...");

	        final OAuthRequest request = new OAuthRequest(Verb.GET, API_USERINFO_URL);
	        service.signRequest(accessToken, request);
	        System.out.println();
	        try (Response response = service.execute(request)) {
	            System.out.println(response.getCode());
	            System.out.println(response.getBody());
	        }

	        System.out.println();
		}
		catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}		
	}

}
