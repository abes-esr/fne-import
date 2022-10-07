package fr.fne.batch.services;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.fne.batch.services.util.api.Tool;

@Service
public class DeleteWB {
	private final Logger logger = Logger.getLogger(DeleteWB.class);

	@Value("${urlWikiBase}")
	private String urlWikiBase;

	@Autowired
    private Tool util;
	
	public void go(String urlWikiBase) {	
		long debut = System.currentTimeMillis();
		int nbItem = 0;
		int nbProp = 0;

		logger.info("Début de DeleteWB :");
		//Pour voir toutes les pages Item : http://fagonie-dev.v102.abes.fr:8181/w/index.php?title=Special:AllPages&from=Q10&namespace=120
		//Pour voir tous les items sans description : http://fagonie-dev.v102.abes.fr:8181/wiki/Special:EntitiesWithoutDescription/fr/item?language=fr&type=item
		try {
			//Connexion au WikiBase et récupération du csrftoken nécessaire
			util.setOAuth(false);
			String csrftoken = util.connexionWB();
			Map<String, String> params = new LinkedHashMap<>();
			
			//Récupération de l'id des namespaces Item et Property
			JSONObject json = util.getJson(urlWikiBase+"?action=query&format=json&meta=siteinfo&siprop=namespaces");			
			Iterator<String> it = json.optJSONObject("query").optJSONObject("namespaces").keys();
			String idNamespaceItem = "";
			String idNamespaceProp = "";
	        while(it.hasNext() && (idNamespaceItem.isEmpty() || idNamespaceProp.isEmpty())){ 
	        	String iterId = it.next();	
	        	if (json.optJSONObject("query").optJSONObject("namespaces").optJSONObject(iterId).optString("canonical").equalsIgnoreCase("Item")) {
	        		idNamespaceItem=iterId; 
	        	}	
	        	else if (json.optJSONObject("query").optJSONObject("namespaces").optJSONObject(iterId).optString("canonical").equalsIgnoreCase("Property")) {
	        		idNamespaceProp=iterId; 
	        	}	        	
	        } 
	        
			//Suppression de tous les items (Q)
			String dernierItem = "";
			while (dernierItem!=null) {				
				//Retourne, par 500, les pages avec namespace:Item (120)
				//http://fagonie-dev.v102.abes.fr:8181/w/api.php?action=query&list=allpages&apnamespace=120&aplimit=500&apfrom=Q10448
				json = util.getJson(urlWikiBase+"?action=query&format=json&list=allpages&apnamespace="+idNamespaceItem+"&aplimit=500&apfrom="+dernierItem);
				//logger.info(json.toString());
				JSONArray liste = json.optJSONObject("query").optJSONArray("allpages");
				for (int i = 0;i<liste.length();i++) {
					JSONObject item = liste.getJSONObject(i);
					logger.info("Titre de la page : "+item.optString("title"));
					String pageId = item.optString("pageid");
					String title = item.optString("title");
					String itemId = title.replace("Item:", "");
					
					// Vider les props d'un Item :
					/*params.put("action", "wbeditentity");
					params.put("token", csrftoken);
					params.put("format", "json");
					params.put("clear", "true");
					params.put("id", itemId);
					params.put("data", "{}");

					json = Utilitaire.postData(urlWikiBase, params);
					logger.info("==>" + json.toString());
					*/
					
					// Supprimer une page Item :
					//http://fagonie-dev.v102.abes.fr:8181/w/index.php?title=Item:Q7698&action=delete
					params = new LinkedHashMap<>();
					params.put("action", "delete");
					params.put("token", csrftoken);
					params.put("format", "json");
					params.put("title", title);
					params.put("reason", "Réinitialisation");

					JSONObject jsonPost = util.postJson(urlWikiBase, params);
					logger.info("==>" + jsonPost.toString());
					
					nbItem++;
				}
				
				//logger.info("pour continuer : "+json.optJSONObject("continue").optString("apcontinue"));		
				if (json.optJSONObject("continue")!=null) {
					dernierItem = json.optJSONObject("continue").optString("apcontinue");
				}
				else {
					dernierItem = null;
				}
			}
			
			
			//Suppression de toutes les propriétés (P)
			String derniereProp = "";
			while (derniereProp!=null) {			
				//http://fagonie-dev.v102.abes.fr:8181/w/api.php?action=query&list=allpages&apnamespace=122&aplimit=500&apfrom=Q10448
				json = util.getJson(urlWikiBase+"?action=query&format=json&list=allpages&apnamespace="+idNamespaceProp+"&aplimit=500&apfrom="+derniereProp);
				//logger.info(json.toString());
				JSONArray liste = json.optJSONObject("query").optJSONArray("allpages");
				for (int i = 0;i<liste.length();i++) {
					JSONObject prop = liste.getJSONObject(i);
					logger.info("Titre de la page : "+prop.optString("title"));
					String title = prop.optString("title");
					
					params = new LinkedHashMap<>();
					params.put("action", "delete");
					params.put("token", csrftoken);
					params.put("format", "json");
					params.put("title", title);
					params.put("reason", "Réinitialisation");

					JSONObject jsonPost = util.postJson(urlWikiBase, params);
					logger.info("==>" + jsonPost.toString());
					
					nbProp++;
				}
				
				//logger.info("pour continuer : "+json.optJSONObject("continue").optString("apcontinue"));		
				if (json.optJSONObject("continue")!=null) {
					derniereProp = json.optJSONObject("continue").optString("apcontinue");
				}
				else {
					derniereProp = null;
				}
			}
						
		} catch (Exception e) {
			logger.error("DeleteWB pb : " + e.getMessage());
		}

		long duree = System.currentTimeMillis() - debut;
		logger.info("Fin de DeleteWB, tps d'execution : " + duree + " | items supprimés : " + nbItem+ " | propriétés supprimées : "+ nbProp);

	}

}
