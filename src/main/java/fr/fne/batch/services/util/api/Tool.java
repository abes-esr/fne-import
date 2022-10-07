package fr.fne.batch.services.util.api;

import java.util.Map;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class Tool {
	private boolean oAuth = true; //Par défaut : utilisation de OAuth. Car préconisé : https://www.mediawiki.org/wiki/OAuth/For_Developers    
    private final Logger logger = Logger.getLogger(Tool.class);

	@Value("${urlWikiBase}")
	private String urlWikiBase;

	@Autowired
    private BasicHttp basicHttp;
	@Autowired
	private OAuthHttp oAuthHttp;
	
    /*
     * Permet de choisir comment se connecter à l'API : avec OAuth ou sans
     * Par défaut : connexion avec OAuth (utilisation de la librairie Scribe)
     */
    public void setOAuth(boolean val) {
    	oAuth = val;
    }
    
    /*
     * Connexion à WikiBase et renvoie du csrftoken
     */
    public String connexionWB() throws Exception{
    	if (oAuth) {
    		oAuthHttp.connexionOAuth();
    	}
    	else {
    		basicHttp.connexionBot();
    	}
    	
    	//Récupèration du token CSRF : csrftoken si connexion OK
		JSONObject json = this.getJson(urlWikiBase + "?action=query&meta=tokens&format=json");
		String csrftoken = json.optJSONObject("query").optJSONObject("tokens").optString("csrftoken");

		return csrftoken;
    }
    
    
    /*
     * Get sur url et renvoie un objet JSON
     */
    public JSONObject getJson(String url) throws Exception {
    	if (oAuth) {
    		return oAuthHttp.httpOAuthGet(url);
    	}
    	else {
    		return basicHttp.getBasicJson(url);
    	}
    }

	/*
	 * Get sur url et ajout d'un param query pour requête Sparql et renvoie un objet JSON
	 */
	public JSONObject getSparqlJson(String url, String queryParam) throws Exception {
		if (oAuth) { //TODO : je n'ai pas testé cet appel : peut être problème d'encodage sur le queryParam..
			return oAuthHttp.httpOAuthGet(url+"?query="+queryParam);
		}
		else {
			return basicHttp.getBasicSparqlJson(url, queryParam);
		}
	}
    
    /*
     * POST sur url avec les datas contenues dans params et renvoie un objet JSON
     */
    public JSONObject postJson(String url, Map<String,String> params) throws Exception {    
    	if (oAuth) {
    		return oAuthHttp.httpOAuthPost(url,params);
    	}
    	else {
    		return basicHttp.postBasicJson(url,params);
    	}
    }

}
