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

    @Autowired
    private Tool util;

    /*
     * Insert in urlWikiBase, the record, with the properties defined (props)
     */
    public void insert(String urlWikiBase,String csrftoken, Map<String,String> props, String record) {
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
                data += addClaims(props, theNotice, "001");
                data += addClaims(props, theNotice, "005");
                data += addClaims(props, theNotice, "033");
                data += addClaims(props, theNotice, "035");
                data += addClaims(props, theNotice, "100");
                data += addClaims(props, theNotice, "200");
                data += addClaims(props, theNotice, "400");
                data += addClaims(props, theNotice, "500");

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
    private String addClaims(Map<String,String> props, Document theNotice, String tag){
        String data = "";
        Elements zones = theNotice.getElementsByAttributeValue("tag", tag);

        for (int i=0;i<zones.size();i++){
            Element zone = zones.get(i);

            //By default, it"s supposed to be a controlfield
            String value = zone.text();
            if (!zone.hasAttr("ind1")) {
                data += jsonClaim(props,tag,value);
            }
            //If it's a datafield :
            else {
                String tagEnCours = tag;

                String ind1 = zone.attr("ind1");
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
                }

                Elements subZones = zone.getElementsByTag("subfield");
                for (int j=0;j<subZones.size();j++){
                    String subZoneTag = tagEnCours+"$";
                    Element subZone = subZones.get(j);
                    value = subZone.text();
                    subZoneTag+=subZone.attr("code");
                    //TODO : use something else to know if the tag/zone is repeatable (maybe in the Properties.txt file description ?)
                    //Maybe with a POJO ?
                    if (tag.startsWith("035") || tag.startsWith("400") || tag.startsWith("500")){
                        subZoneTag+="_"+(i+1);
                    }

                    data += jsonClaim(props,subZoneTag,value);
                }
            }
        }
        return data;
    }

    /*
     * Generate the json string to pass to the wikibase API
     */
    private String jsonClaim(Map<String,String> props, String tag, String value){
        logger.info(tag);
        String claim = "";
        //TODO : add the else case : if property is null, but should be created, then create it and reload properties.
        //Could be usefull for repeatable property : with position suffix : like 035$a_1, 035$a_2, etc.
        if (props.get(tag)!=null){
            claim = "{\"mainsnak\":{\"snaktype\":\"value\",\"property\":\"" +
                    props.get(tag) + "\",\"datavalue\":{\"value\":\"" + value +
                    "\",\"type\":\"string\"}},\"type\":\"statement\",\"rank\":\"normal\"},";
        }
        return claim;
    };
}
