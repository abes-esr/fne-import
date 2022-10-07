package fr.fne.batch.services.util.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class BasicHttp {

	@Value("${urlWikiBase}")
	private String urlWikiBase;

	@Value("${bot.login}")
    private String lgname;
	@Value("${bot.pwd}")
    private String lgpassword;
	//################## Version basique : sans librairie particuliere. Enfin, utilisation de RestTemplate qd meme :)
    
    // Basic : bas niveau
    public JSONObject getBasicJson(String url) throws Exception {    	
    	
		//Avec RestTemplate
		RestTemplate restTemplate = new RestTemplate();
		JSONObject json =  new JSONObject(restTemplate.getForObject(url, String.class));

		return json;

		/*
    	URL urlGetInfo = new URL(url);
        
		BufferedReader reader = new BufferedReader(new InputStreamReader(urlGetInfo.openStream(), "UTF-8"));
        String line = "";
        StringBuilder builder = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        reader.close();				  
        JSONObject json = new JSONObject(builder.toString());
        
        return json;
        */
    }

	// Basic : bas niveau : permet d'avoir une réponse Json à partir d'une requête Sparql
	public JSONObject getBasicSparqlJson(String url, String queryParam) throws Exception {

		//Avec RestTemplate
		RestTemplate restTemplate = new RestTemplate();
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url).queryParam("query",queryParam);
		JSONObject json =  new JSONObject(restTemplate.getForObject(builder.build().encode().toUri(), String.class));

		return json;
	}

    // Basic : bas niveau
    public JSONObject postBasicJson(String url, Map<String,String> params) throws Exception {    	
    	    	
		//Avec RestTemplate
    	RestTemplate restTemplate = new RestTemplate();
		HttpHeaders entete = new HttpHeaders();
		entete.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> paramE = new LinkedMultiValueMap<String, String>();
		for (Map.Entry<String,String> param : params.entrySet()) {
			paramE.add(param.getKey(),param.getValue());
		}

		HttpEntity<MultiValueMap<String, String>> requeteHttp = new HttpEntity<MultiValueMap<String, String>>(paramE, entete);		
		JSONObject json = new JSONObject(restTemplate.postForEntity(url, requeteHttp, String.class).getBody().toString());
        
        return json;
    	
    	/*URL urlPostData = new URL(url);
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String,String> param : params.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(param.getValue(), "UTF-8"));
        }
        byte[] postDataBytes = postData.toString().getBytes("UTF-8");

        HttpURLConnection connection = (HttpURLConnection)urlPostData.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        connection.setDoOutput(true);
        connection.getOutputStream().write(postDataBytes);

        Reader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));

        StringBuilder sb = new StringBuilder();
        for (int c; (c = in.read()) >= 0;)
            sb.append((char)c);
        JSONObject json = new JSONObject(sb.toString());
        
        return json;*/
    }
    
  //Connexion à WikiBase avec un compte Bot et cookie
    //oAuth vaudra forcément false, donc getJson et postJson utiliseront les versions "basiques" (sans OAuth)
    public void connexionBot() throws Exception {
		/* ## CONNECTION ## */
		// 1) Recuperer un login token
		// Le cookie est obligatoire sinon erreur
		CookieManager cookieManager = new CookieManager();
		CookieHandler.setDefault(cookieManager);
		/*
		 * Pour info, lecture des valeurs du cookie : List<HttpCookie> cookies =
		 * cookieManager.getCookieStore().getCookies(); for (HttpCookie cookie :
		 * cookies) { logger.info(cookie.getDomain()); logger.info(cookie); }
		 */

		JSONObject json = this.getBasicJson(urlWikiBase + "?action=query&meta=tokens&type=login&format=json");
		// logger.info(json.toString());
		String loginToken = json.optJSONObject("query").optJSONObject("tokens").optString("logintoken");
		// logger.info(loginToken);

		// 2) se logger avec un POST data
		Map<String, String> params = new LinkedHashMap<>();
		params.put("action", "login");
		params.put("lgname", lgname);
		params.put("lgpassword", lgpassword);
		params.put("lgtoken", loginToken);
		params.put("format", "json");
		json = this.postBasicJson(urlWikiBase, params);
		// logger.info(json.toString());
	}
    
    
}