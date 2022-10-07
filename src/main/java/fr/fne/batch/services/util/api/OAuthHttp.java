package fr.fne.batch.services.util.api;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.scribejava.apis.MediaWikiApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;

@Service
public class OAuthHttp {
	
	private final Logger logger = Logger.getLogger(OAuthHttp.class);

    @Value("${urlWikiBase}")
    private String urlWikiBase;

	@Value("${oauth.consumerToken}")
    private String consumerToken;
	@Value("${oauth.consumerSecret}")
    private String consumerSecret;
	@Value("${oauth.accessToken}")
    private String accessToken;
	@Value("${oauth.accessSecret}")
    private String accessSecret;
	
	//OAuth 1.0
    private String userAgent = ""; // https://meta.wikimedia.org/wiki/User-Agent_policy	
    private OAuth1AccessToken oAuthAccessToken;
    private OAuth10aService oAuthService;
    //########################### Version avec utilisation de Scribe pour authentification owner-only Connected App / OAuth 1.0 
    
    //OAUth
    public JSONObject httpOAuthGet(String url) throws Exception {
        return httpOAUthCall(Verb.GET, url, Collections.emptyMap(), Collections.emptyMap());
    }

    //OAUth
    public JSONObject httpOAuthPost(String url, Map<String, String> params) throws Exception {
    	Map<String, String> map1 = new HashMap<String, String>();
    	map1.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        return httpOAUthCall(Verb.POST, url, map1, params);
    }

    //OAuth
    public JSONObject httpOAUthCall(Verb verb, String url, Map<String, String> headers, Map<String, String> params) throws Exception {
        OAuthRequest request = new OAuthRequest(verb, url);
        request.setCharset(StandardCharsets.UTF_8.name());
        params.forEach(request::addParameter);
        headers.forEach(request::addHeader);
        request.addHeader("User-Agent", userAgent);
        oAuthService.signRequest(oAuthAccessToken, request);
        
        //logger.info("CompleteUrl :  "+request.getCompleteUrl());
        /*Collection<String>  ListofKeys = request.getHeaders().values().stream().collect(Collectors. 
                toCollection(ArrayList::new)); 
        logger.info(ListofKeys.toString());
        */
        
        return new JSONObject(oAuthService.execute(request).getBody());
    }
    
    //Connexion à WikiBase avec un compte OAuth
    //oAuth vaudra forcément true, donc getJson et postJson utiliseront les versions avec OAuth
    public void connexionOAuth() throws Exception {
    	oAuthService = new ServiceBuilder(consumerToken).apiSecret(consumerSecret).build(MediaWikiApi.instance());
    	//Sinon, avec debug : 
    	//oAuthService = new ServiceBuilder(consumerToken).debug().apiSecret(consumerSecret).build(MediaWikiApi.instance());
        oAuthAccessToken = new OAuth1AccessToken(accessToken, accessSecret);
        
        // Check authentication
        //logger.info(Utilitaire.getJson(urlWikiBase + "?action=query&meta=userinfo&uiprop=blockinfo|groups|rights|ratelimits&format=json"));
        // Fetch CSRF token, mandatory for upload using the Mediawiki API
	}
    
}