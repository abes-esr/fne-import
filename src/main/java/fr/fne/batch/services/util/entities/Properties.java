package fr.fne.batch.services.util.entities;

import fr.fne.batch.services.util.api.Tool;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class Properties {
    private final Logger logger = Logger.getLogger(Properties.class);

    @Value("${urlWikiBase}")
    private String urlWikiBase;

    @Autowired
    private Tool util;

    /*
    * Insert into wikibase instance, properties found in the resources/Properties.txt file
     */
    public void createWithFile(String csrftoken) throws Exception {
        File file = new ClassPathResource("Properties.txt").getFile();
        List<String> lines = FileUtils.readLines(file, "UTF-8");
        Iterator line = lines.iterator();

        while (line.hasNext()){
            String prop = (String)line.next();
            String idProp = create(csrftoken,prop);
        }
    }

    /*
     * Creation de la propriété prop
     */
    public String create(String csrftoken, String prop) throws Exception {
            // Properties creation :
            Map<String, String> params = new LinkedHashMap<>();
            params = new LinkedHashMap<>();
            params.put("action", "wbeditentity");
            params.put("new", "property");
            params.put("token", csrftoken);
            params.put("format", "json");

            params.put("data", "{\"labels\":{\"fr\":{\"language\":\"fr\",\"value\":\"" + prop + "\"}},\"datatype\":\"string\"}");

            JSONObject json = util.postJson(urlWikiBase, params);
            //logger.info("==>" + json.toString());
            return json.getJSONObject("entity").optString("id");
    }

    /*
    * Return a map with the label and the wikibase ID (Pxx)
     */
    public Map get(String csrftoken) throws Exception {
        // Getting namespace Property id
        JSONObject json = util.getJson(urlWikiBase + "?action=query&format=json&meta=siteinfo&siprop=namespaces");
        Iterator<String> it = json.optJSONObject("query").optJSONObject("namespaces").keys();
        String idNamespaceProp = "";
        while (it.hasNext() && idNamespaceProp.isEmpty()) {
            String iterId = it.next();
            if (json.optJSONObject("query").optJSONObject("namespaces").optJSONObject(iterId).optString("canonical").equalsIgnoreCase("Property")) {
                idNamespaceProp = iterId;
            }
        }

        Map<String, String> properties = new LinkedHashMap<>();
        // Loading existing properties into a map
        // TODO : tester si au delà de 500, ça fonctionne :)
        String lastProp = "";
        while (lastProp != null) {
            json = util.getJson(urlWikiBase + "?action=query&format=json&list=allpages&apnamespace="
                    + idNamespaceProp + "&aplimit=500&apfrom=" + lastProp);
            JSONArray liste = json.optJSONObject("query").optJSONArray("allpages");
            for (int i = 0; i < liste.length(); i++) {
                JSONObject prop = liste.getJSONObject(i);
                // logger.info("Titre de la propriété : "+item.optString("title"));
                String title = prop.optString("title");
                String propertyId = title.replace("Property:", "");

                JSONObject property = util.getJson(urlWikiBase + "?action=wbgetentities&format=json&ids=" + propertyId);

                JSONObject theProperty =  property.optJSONObject("entities").optJSONObject(propertyId);
                if (theProperty!=null){
                    if (theProperty.optJSONObject("labels")!=null){
                        if (theProperty.optJSONObject("labels").optJSONObject("fr")!=null) {
                            if (theProperty.optJSONObject("labels").optJSONObject("fr").optString("value") != null) {
                                String propertyValue = theProperty.optJSONObject("labels").optJSONObject("fr").optString("value");
                                logger.info("Property : " + propertyId + " value : " + propertyValue);
                                properties.put(propertyValue, propertyId);
                            }
                        }
                    }
                }
            }

            // logger.info("to continue :"+json.optJSONObject("continue").optString("apcontinue"));
            if (json.optJSONObject("continue") != null) {
                lastProp = json.optJSONObject("continue").optString("apcontinue");
            } else {
                lastProp = null;
            }
        }

        //If nothing to load, create properties
        if (properties.size()==0){
            createWithFile(csrftoken);
            properties = get(csrftoken);
        }
        return properties;
    }
}
