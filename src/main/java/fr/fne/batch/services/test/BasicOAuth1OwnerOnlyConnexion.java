package fr.fne.batch.services.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.scribejava.core.java8.Base64;

@Service
public class BasicOAuth1OwnerOnlyConnexion {

	private static final Logger logger = Logger.getLogger(BasicOAuth1OwnerOnlyConnexion.class);

	@Value("${oauth.consumerToken}")
    private String consumerToken;
	@Value("${oauth.consumerSecret}")
    private String consumerSecret;
	@Value("${oauth.accessToken}")
    private String accessToken;
	@Value("${oauth.accessSecret}")
    private String accessSecret;

	
	// Juste pour bidouille la connexion à OAuth...
	// Au 01/09/20 : ça marche mais c'est compliqué... Plus simple avec services.util.OAuthHttp
	// https://www.mediawiki.org/wiki/OAuth/Owner-only_consumers
	public void go(String urlWikiBase) {
		
		try {
			
			//Utilitaire.connexionWB(Utilitaire.getProp("urlWikiBase"));
			//JSONObject json = Utilitaire.getJson(Utilitaire.getProp("urlWikiBase") + "?action=query&format=json&meta=tokens");
			
			JSONObject json = getBasicJson(urlWikiBase + "?action=query&format=json&meta=tokens");
			String csrftoken = json.optJSONObject("query").optJSONObject("tokens").optString("csrftoken");
			logger.info("CSRFTOKEN==>" + csrftoken);
			
			Map<String, String> params = new LinkedHashMap<>();
			params.put("action", "wbeditentity"); 
			params.put("data", "{\"labels\":{\"fr\":{\"language\":\"fr\",\"value\":\"Titre\"}},"+
			  		"\"descriptions\":{\"fr\":{\"language\":\"fr\",\"value\":\"Le titre de la notice\"}},\"datatype\":\"string\"}"
					); 
			params.put("format", "json");
			params.put("new", "property");
			params.put("token", csrftoken); 
			
			json = postBasicJson(urlWikiBase, params);
			//json = Utilitaire.postJson(Utilitaire.getProp("urlWikiBase"), params);
			
			logger.info("==>" + json.toString());
		}
		catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}		
	}
	
	// Basic : bas niveau
    public JSONObject getBasicJson(String url) throws Exception {
    	URL urlGetInfo = new URL(url);
    	HttpURLConnection conn = (HttpURLConnection) urlGetInfo.openConnection();
        conn.setRequestMethod("GET");
        conn.setDoOutput(true);
        
        //Test ajout header oAuth "manuellement"
        Random rand = new Random();
        long timer = System.currentTimeMillis()/1000;
        long nonce = timer + rand.nextInt();        		

        url+="&oauth_consumer_key="+consumerToken+"&oauth_nonce="+String.valueOf(nonce)+
        		"&oauth_signature_method=HMAC-SHA1&oauth_timestamp="+String.valueOf(timer)+"&oauth_token="+accessToken+"&oauth_version=1.0";
        
        String baseUrl = url.substring(0,url.indexOf("?"));
        String paramsUrl = url.substring(url.indexOf("?")+1);        
        String toSign = "GET&"+URLEncoder.encode(baseUrl, "UTF-8")+"&"+URLEncoder.encode(paramsUrl, "UTF-8");
        
        //logger.info("===>"+toSign);
        String keyString = URLEncoder.encode(consumerSecret, "UTF-8") + '&' + URLEncoder.encode(accessSecret, "UTF-8");
        final SecretKeySpec key = new SecretKeySpec(keyString.getBytes("UTF-8"), "HmacSHA1");
        final Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(key);
        final byte[] bytes = mac.doFinal( toSign.getBytes("UTF-8") );
        String signature = Base64.getEncoder().encodeToString(bytes).replace("\r\n", "");
        
        String header = "OAuth";
        header+=" oauth_nonce=\""+String.valueOf(nonce)+"\",";
        header+=" oauth_signature=\""+signature+"\",";
        header+=" oauth_token=\""+accessToken+"\",";
        header+=" oauth_consumer_key=\""+consumerToken+"\",";
        header+=" oauth_timestamp=\""+String.valueOf(timer)+"\",";
        header+=" oauth_signature_method=\"HMAC-SHA1\",";
        header+=" oauth_version=\"1.0\",";
        
        //https://stackoverflow.com/questions/29098500/how-to-add-header-to-httprequest-of-get-method-in-java
        conn.setRequestProperty("Authorization", header);
        conn.setRequestProperty("User-Agent", "");    
               
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        
		//BufferedReader reader = new BufferedReader(new InputStreamReader(urlGetInfo.openStream(), "UTF-8"));
        String line = "";
        StringBuilder builder = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        reader.close();				  
        JSONObject json = new JSONObject(builder.toString());
        
        return json;
    }
    
	 // Basic : bas niveau
    public JSONObject postBasicJson(String url, Map<String,String> params) throws Exception {    	
    	URL urlPostData = new URL(url);
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String,String> param : params.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(param.getValue(), "UTF-8"));
        }                               
        
        byte[] postDataBytes = postData.toString().getBytes("UTF-8");
        String paramsUrl = postData.toString();
        HttpURLConnection conn = (HttpURLConnection)urlPostData.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        //conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setDoOutput(true);
        
        
        //Test ajout header oAuth "manuellement"
        Random rand = new Random();
        long timer = System.currentTimeMillis()/1000;
        long nonce = timer + rand.nextInt();        		

        String token = "";
        if (paramsUrl.indexOf("&token=")>0) {
        	token = paramsUrl.substring(paramsUrl.indexOf("&token="));
        	paramsUrl = paramsUrl.substring(0,paramsUrl.indexOf("&token="));
        }
        paramsUrl+="&oauth_consumer_key="+consumerToken+"&oauth_nonce="+String.valueOf(nonce)+
        		"&oauth_signature_method=HMAC-SHA1&oauth_timestamp="+String.valueOf(timer)+"&oauth_token="+accessToken+"&oauth_version=1.0";
        //paramsUrl+="&token="+"8bc8d258f391fb58613d6660e3441b0e5f443644+\\";
        //paramsUrl+=URLDecoder.decode(token, "UTF-8");
        paramsUrl+=token;
        
        String baseUrl = url;   
        String toSign = "POST&"+URLEncoder.encode(baseUrl, "UTF-8")+"&"+URLEncoder.encode(paramsUrl.replaceAll("\\+", "%20"), "UTF-8");
        
        logger.info("===>"+toSign);
        String keyString = URLEncoder.encode(consumerSecret, "UTF-8") + '&' + URLEncoder.encode(accessSecret, "UTF-8");
        final SecretKeySpec key = new SecretKeySpec(keyString.getBytes("UTF-8"), "HmacSHA1");
        final Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(key);
        final byte[] bytes = mac.doFinal( toSign.getBytes("UTF-8") );
        String signature = Base64.getEncoder().encodeToString(bytes).replace("\r\n", "");
        
        logger.info("base string is : "+toSign);
        logger.info("signature : "+signature);
        //token : 8bc8d258f391fb58613d6660e3441b0e5f443644+\
        //base string is: POST&http%3A%2F%2Ffagonie-dev.v102.abes.fr%3A8181%2Fw%2Fapi.php&action%3Dwbeditentity%26data%3D%257B%2522labels%2522%253A%257B%2522fr%2522%253A%257B%2522language%2522%253A%2522fr%2522%252C%2522value%2522%253A%2522Titre%2522%257D%257D%252C%2522descriptions%2522%253A%257B%2522fr%2522%253A%257B%2522language%2522%253A%2522fr%2522%252C%2522value%2522%253A%2522Le%2520titre%2520de%2520la%2520notice%2522%257D%257D%252C%2522datatype%2522%253A%2522string%2522%257D%26format%3Djson%26new%3Dproperty%26oauth_consumer_key%3Db754aaed4712e64936c584c8c0a9c48d%26oauth_nonce%3D2995820181%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1598305890%26oauth_token%3De91e447c81cf157d2eb7253bc8822349%26oauth_version%3D1.0%26token%3D8bc8d258f391fb58613d6660e3441b0e5f443644%252B%255C
        //signature is: G8+0JZf1UfJ4M6t5sJHXYRcdy1U=
        
        
        String header = "OAuth";
        header+=" oauth_nonce=\""+String.valueOf(nonce)+"\",";
        header+=" oauth_signature=\""+signature+"\",";
        header+=" oauth_token=\""+accessToken+"\",";
        header+=" oauth_consumer_key=\""+consumerToken+"\",";
        header+=" oauth_timestamp=\""+String.valueOf(timer)+"\",";
        header+=" oauth_signature_method=\"HMAC-SHA1\",";
        header+=" oauth_version=\"1.0\",";
        
        //https://stackoverflow.com/questions/29098500/how-to-add-header-to-httprequest-of-get-method-in-java
        conn.setRequestProperty("Authorization", header);   
        conn.setRequestProperty("User-Agent", "");         
        
        conn.getOutputStream().write(postDataBytes);

        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

        StringBuilder sb = new StringBuilder();
        for (int c; (c = in.read()) >= 0;)
            sb.append((char)c);
        JSONObject json = new JSONObject(sb.toString());
        
        return json;
    }

    
}
