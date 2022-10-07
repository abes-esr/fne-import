package fr.fne.batch.services.util.entities;

import fr.fne.batch.services.LoadAll;
import fr.fne.batch.services.util.api.Tool;
import oracle.xdb.XMLType;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
/*
CETTE CLASSE N'EST PLUS UTILISEE
POUR L'INSTANT ELLE EST CONSERVEE, MAIS SERA A SUPPRIMER
ELLE EST REMPLACEE PAR Entitites
 */
public class EntitiesJSOUP {
    private final Logger logger = Logger.getLogger(EntitiesJSOUP.class);

    /** The Constant STR009C. */
    public static final String STR009C = new String(new char[]{(char) 156});
    /** The Constant STR0098. */
    public static final String STR0098 = new String(new char[]{(char) 152});

    @Value("${urlWikiBase}")
    private String urlWikiBase;

    @Autowired
    private Tool util;

    @Autowired
    private Properties properties;

    /*
     * Insert in urlWikiBase, the record, with the properties defined (props)
     */
    public void insert(String csrftoken, Map<String,String> props, String record) {
        try {
            String noticeXML = record.replace(STR009C, "").replace(STR0098, "");
            //logger.info(noticeXML);
            Document theNotice = Jsoup.parse(noticeXML, "", Parser.xmlParser());
            theNotice.outputSettings(new Document.OutputSettings().prettyPrint(false));

            //title for the entity "label"
            String title = "";
            if (theNotice.getElementsByAttributeValueMatching("tag", "900|910|915|920|930|940|950|960|980").size() > 0) {
                title = theNotice
                        .getElementsByAttributeValueMatching("tag", "900|910|915|920|930|940|950|960|980")
                        .get(0).getElementsByAttributeValue("code", "a").text();
            }

            if (!title.isEmpty()) { // "Works" Tr case without title, example : id='5420922'

                // Doc how to create entites : item, prop, etc. => https://www.wikidata.org/w/api.php?action=help&modules=wbeditentity
                String data = "{\"labels\":{\"fr\":{\"language\":\"fr\",\"value\":\"" + title + "\"}},\"claims\":[";

                //For each known property (ex : 001,005,etc.) :
                //It's commented because there are too many properties on fagonie-dev, but it could be a good way to handle the definition of properties..
                //TODO : use a POJO to define and store record, and a DTO to transform data for wikibase
                /*for (Map.Entry<String, String> entry : props.entrySet()) {
                    data += addClaims(props, theNotice, entry.getKey().substring(0,3));
                }*/
                data += addClaims(csrftoken, props, theNotice);

                if (data.endsWith(",")){
                    data = data.substring(0,data.length()-1);
                }
                data+="]}";

                Map<String, String> params = new LinkedHashMap<>();
                params.put("action", "wbeditentity");
                params.put("new", "item");
                params.put("token", csrftoken);
                params.put("format", "json");
                params.put("data",data);

                logger.info("data : "+data);
                JSONObject json = util.postJson(urlWikiBase, params);
                logger.info("==>" + json.toString());
            } else {
                logger.info("==> no title for PPN : " + noticeXML);
            }

            // "claims" (déclarations) creation needed ? :
            // => https://www.wikidata.org/w/api.php?action=help&modules=wbcreateclaim

        } catch (Exception e) {
            logger.error("Error on the record : " + record + " :" + e.getMessage());
            e.printStackTrace();
        }
    }

    /*
     * Manage cases of :
     * controlfield (element with tag and no ind attributes) and
     * datafield (element with tag and ind attributes) which contains subfield
     */
    private String addClaims(String csrftoken, Map<String,String> props, Document theNotice) throws Exception{
        String data = "";

        //Leader
        //logger.info("LEADER:"+theNotice.getElementsByTag("leader").get(0).text());
        data += jsonClaim(csrftoken,props,"leader",theNotice.getElementsByTag("leader").get(0).text(),1,1);

        int regroupement = 1;

        //ControlField
        Elements zones = theNotice.getElementsByTag("controlfield");
        for (int i=0;i<zones.size();i++){
            Element zone = zones.get(i);
            regroupement++;
            data += jsonClaim(csrftoken,props,zone.attr("tag"),zone.text(),regroupement,1);
        //    logger.info("CONTROLFIELD:"+zone.attr("tag")+" texte:"+zone.text());
        }

        int ordre = 0;
        //DataField
        zones = theNotice.getElementsByTag("datafield");
        for (int i=0;i<zones.size();i++){
            Element zone = zones.get(i);
            regroupement++;
            ordre = 0;
            String tagEnCours = zone.attr("tag");

            //TODO : ajouter les indicateurs
            /*String ind1 = zone.attr("ind1");
            String ind2 = zone.attr("ind2");
            if ((ind1 != null && !ind1.trim().isEmpty()) || (ind2 != null && !ind2.trim().isEmpty()) ) {
                if (ind1 != null && !ind1.trim().isEmpty()) {
                    tagEnCours += ind1;
                }
                else {
                    tagEnCours += "#";
                }
                if (ind2 != null && !ind2.trim().isEmpty()) {
                    tagEnCours += ind2;
                }
                else {
                    tagEnCours += "#";
                }
            }*/


            Elements subZones = zone.getElementsByTag("subfield");
            for (int j=0;j<subZones.size();j++){
                String subZoneTag = tagEnCours+"$";
                Element subZone = subZones.get(j);
                String value = subZone.text();
                subZoneTag+=subZone.attr("code");
                ordre++;
//logger.info("SUBFIELD : tagEnCours: "+tagEnCours+" subZoneTag:"+subZoneTag+" Value:"+value+ "regroupement:"+regroupement+" ordre:"+ordre);
                data += jsonClaim(csrftoken,props,subZoneTag,value, regroupement, ordre);
            }

        }
        return data;
    }

    /*
     * Generate the json string to pass to the wikibase API
     */
    private String jsonClaim(String csrftoken, Map<String,String> props, String tag, String value, int regroupement, int ordre) throws Exception{
        //logger.info(tag);
        String claim = "";
        if (props.get(tag)==null){
            String idProp = properties.create(csrftoken,tag); // Création de la propriété
            props.put(tag, idProp); // Ajout dans la map
        }
        claim = "{\"mainsnak\":{\"snaktype\":\"value\",\"property\":\"" +
                props.get(tag) + "\",\"datavalue\":{\"value\":\"" + value +
                "\",\"type\":\"string\"}},\"type\":\"statement\",\"rank\":\"normal\"," +
                "\"qualifiers\":[" +
                    "{\"datavalue\":{\"type\":\"string\",\"value\":\""+regroupement+"\"},\"property\":\""+props.get("Regroupement")+"\",\"snaktype\":\"value\",\"datatype\":\"string\"}," +
                    "{\"datavalue\":{\"type\":\"string\",\"value\":\""+ordre+"\"},\"property\":\""+props.get("Ordre")+"\",\"snaktype\":\"value\",\"datatype\":\"string\"}" +
                "]},";
        return claim;
    };
}