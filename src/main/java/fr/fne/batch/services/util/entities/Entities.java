package fr.fne.batch.services.util.entities;

import fr.fne.batch.services.DtoNoticeToFne;
import fr.fne.batch.services.LoadAll;
import fr.fne.batch.services.util.api.Tool;
import oracle.xdb.XMLType;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class Entities {
    private final Logger logger = Logger.getLogger(Entities.class);

    /** The Constant STR009C. */
    public static final String STR009C = new String(new char[]{(char) 156});
    /** The Constant STR0098. */
    public static final String STR0098 = new String(new char[]{(char) 152});

    @Autowired
    private Tool util;

    @Autowired
    private DtoNoticeToFne dto;


    /*
     * Crée un lien entre la 500$3_1 de l'entité correspondant au ppn en cours, et une autre entité dont le ppn est celui trouvé en 500$3_1
     */
    public void link(String urlWikiBase, String urlWdqs, String csrftoken, Map<String,String> props, String ppn) {
        try{
            // Pour retrouver l'id de l'entity à partir du PPN, utilisation du wiki query service (pas trouvé autrement...) :
            // Amélioration de la requête : récupération directe de l'id de l'entity correspondant à un PPN d'une 500$3_1
            // Filtre pour ne prendre en compte que les entity qui n'ont pas déjà une 500$3_1Liee
            // Source : https://stackoverflow.com/questions/27202263/how-to-find-a-wikidata-entity-by-property

            // Explications des espaces de noms :
            // http://baskauf.blogspot.com/2019/05/getting-data-out-of-wikidata-using.html
            // https://heardlibrary.github.io/digital-scholarship/lod/wikibase/#references
            String query =  "PREFIX wdt: <http://wikibase.svc/prop/direct/>\n" +
                    "PREFIX p: <http://wikibase.svc/prop/>\n" +
                    "\n" +
                    "SELECT ?item ?500d3_1value ?500d3_1ID ?itemAlier WHERE {\n" +
                    "  ?item wdt:"+props.get("001")+" \""+ppn+"\".\n" +
                    "  ?item wdt:"+props.get("500$3_1")+" ?500d3_1value.\n" +
                    "  ?item p:"+props.get("500$3_1")+" ?500d3_1ID.\n" +
                    "  ?itemAlier wdt:"+props.get("001")+" ?500d3_1value.\n" +
                    "        FILTER(\n" +
                    "  !EXISTS {?item wdt:"+props.get("500$3_1Liee")+" ?lien}\n" +
                    "          )\n" +
                    "}";

            //Utilisation d'un objet URI pour ce restTemplate à cause des "braces/accolades" :
            //https://stackoverflow.com/questions/20885521/spring-resttemplate-url-encoding
            //https://stackoverflow.com/questions/43917408/resttemplate-request-with-braces/44215493
            JSONObject json =  util.getSparqlJson(urlWdqs,query);

            JSONArray liste = json.optJSONObject("results").optJSONArray("bindings");
            if (liste.length()>0){
                if (liste.length()>1) {
                    logger.error("Problème, le ppn : " + ppn + " est en doublon");
                }

                JSONObject item = liste.getJSONObject(0);
                String idItem = item.optJSONObject("item").optString("value");
                //"http://wikibase.svc/entity/Q3569323"
                idItem = idItem.substring(idItem.lastIndexOf('/')+1);
                //logger.info("Q = "+idItem);

                String id500d3 = item.optJSONObject("500d3_1ID").optString("value");
                //"http://wikibase.svc/entity/statement/Q3569323-3E0930CE-8961-4661-9BF5-9FCD523CF650"
                id500d3 = id500d3.substring(id500d3.lastIndexOf('/')+1);

                String ppn500d3 = item.optJSONObject("500d3_1value").optString("value");
                //"029496462"

                String idItemAlier = item.optJSONObject("itemAlier").optString("value");
                //"http://wikibase.svc/entity/Q3568709"
                idItemAlier = idItemAlier.substring(idItemAlier.lastIndexOf('/')+1);


                //Il faut modifier la propriété GUID et mettre l'identifiant de l'entité qu'on vient de retrouver à partir de son PPN.
                //Modifier une propriété ?
                //Doc explicative, à la fin de cette page : http://fagonie-dev.v102.abes.fr:8181/w/api.php?action=help&modules=wbeditentity
                //Exemple : http://fagonie-dev.v102.abes.fr:8181/w/api.php?action=wbeditentity&id=Q42&data=%7B%22claims%22:%5B%7B%22id%22:%22Q42%24GH678DSA-01PQ-28XC-HJ90-DDFD9990126X%22,%22mainsnak%22:%7B%22snaktype%22:%22value%22,%22property%22:%22P56%22,%22datavalue%22:%7B%22value%22:%22ChangedString%22,%22type%22:%22string%22%7D%7D,%22type%22:%22statement%22,%22rank%22:%22normal%22%7D%5D%7D
                //En fait c'est pas modifier une propriété, c'est la remplacer :
                //Utiliser : http://fagonie-dev.v102.abes.fr:8181/wiki/Property:P224    libelle : 500$3Liee
                //Et : https://www.mediawiki.org/wiki/Wikibase/DataModel/JSON

                //Modifier la string
                //String data="{\"claims\":[{\"id\":\""+id500d3+"\",\"mainsnak\":{\"snaktype\":\"value\",\"property\":\"P132\",\"datavalue\":{\"value\":\"CHANGER!\",\"type\":\"string\"}},\"type\":\"statement\",\"rank\":\"normal\"}]}";
                //Supprimer la prop
                //String data="{\"claims\":[{\"id\":\""+id500d3+"\",\"remove\":\"\"}]}";
                //Ajout du idItemAlier
                String data="{\"claims\":[{\"mainsnak\":{\"snaktype\":\"value\",\"property\":\""+props.get("500$3_1Liee")+"\",\"datavalue\":{\"value\":{\"entity-type\": \"item\",\"id\": \""+idItemAlier+"\",\"numeric-id\": "+idItemAlier.replace("Q","")+"},\"type\":\"wikibase-entityid\"}},\"type\":\"statement\",\"rank\":\"normal\"}]}";
                //Remplacer la prop : pas possible..
                //String data="{\"claims\":[{\"id\":\""+id500d3+"\",\"mainsnak\":{\"snaktype\":\"value\",\"property\":\""+props.get("500$3_1Liee")+"\",\"datavalue\":{\"value\":{\"entity-type\": \"item\",\"id\": \""+idItemAlier+"\",\"numeric-id\": "+idItemAlier.replace("Q","")+"},\"type\":\"wikibase-entityid\"}},\"type\":\"statement\",\"rank\":\"normal\"}]}";

                Map<String, String> params = new LinkedHashMap<>();
                params.put("action", "wbeditentity");
                //params.put("id", "Q3567960");
                params.put("id", idItem);
                params.put("token", csrftoken);
                params.put("format", "json");
                params.put("data",data);

                json = util.postJson(urlWikiBase, params);
                logger.info("==>" + json.toString());
            }
        } catch (Exception e) {
            logger.error("Erreur sur le ppn : " + ppn + " :" + e.getMessage());
            e.printStackTrace();
        }
    }

    /*
     * Insert in urlWikiBase, the record, with the properties defined (props)
     */
    public void insert(String urlWikiBase,String csrftoken, Map<String,String> props, String record) {
        try {
            String noticeXML = record.replace(STR009C, "").replace(STR0098, "");
            String data = dto.unmarshallerNotice(noticeXML,props);

            Map<String, String> params = new LinkedHashMap<>();
            params.put("action", "wbeditentity");
            params.put("new", "item");
            params.put("token", csrftoken);
            params.put("format", "json");
            params.put("data",data);

            //logger.info("data : "+data);
            JSONObject json = util.postJson(urlWikiBase, params);
            logger.info("==>" + json.toString());

            // "claims" (déclarations) creation needed ? :
            // => https://www.wikidata.org/w/api.php?action=help&modules=wbcreateclaim

        } catch (Exception e) {
            logger.error("Error on the record : " + record + " :" + e.getMessage());
            e.printStackTrace();
        }
    }
}
