package fr.fne.batch;

import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.fne.batch.services.util.api.Tool;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.process.ProcessCBS;
import fr.abes.cbs.utilitaire.Utilitaire;

import java.util.LinkedHashMap;
import java.util.Map;

@SpringBootTest
@Slf4j
class BatchApplicationTests {

	@Value("${urlWikiBase}")
	private String urlWikiBase;

	/*@Value("${cbs.url}")
	private String cbsUrl;
	@Value("${cbs.port}")
	private String cbsPort;
	@Value("${cbs.login}")
	private String cbsLogin;
	@Value("${cbs.pwd}")
	private String cbsPwd;
*/

	@Autowired
	private Tool util;

	@Test
    /*
    Création d'une propriété
     */
	void createProperty() {
		try {
			util.setOAuth(false);
			String csrftoken = util.connexionWB();

			Map<String, String> params = new LinkedHashMap<>();
			params = new LinkedHashMap<>();
			params.put("action", "wbeditentity");
			params.put("new", "property");
			params.put("token", csrftoken);
			params.put("format", "json");
			params.put("data", "{\"labels\":{\"fr\":{\"language\":\"fr\",\"value\":\"ordre3\"}},\"datatype\":\"string\"}");

			JSONObject json = util.postJson(urlWikiBase, params);
			log.info("==>" + json.getJSONObject("entity").optString("id"));
		}
		catch (Exception e){
			log.error("Erreur : "+e.getMessage());
		}
	}


    @Test
     /*
    Test ajout des qualificatifs : "regroup" (string) = P490 et ordre (string) = P492
    En suivant le schéma de données json décrit ici : https://www.mediawiki.org/wiki/Wikibase/DataModel/JSON#Qualifiers
    */
    void createItemWithQualifiers() {
        try {
            util.setOAuth(false);
            String csrftoken = util.connexionWB();

            Map<String, String> params = new LinkedHashMap<>();
            params = new LinkedHashMap<>();
            params.put("action", "wbeditentity");
            params.put("new", "item");
            params.put("token", csrftoken);
            params.put("format", "json");

            params.put("data",
                    "{\"claims\":[" +
                        "{\"mainsnak\":{\"datavalue\":{\"type\":\"string\",\"value\":\"026360608\"},\"property\":\"P114\",\"snaktype\":\"value\"},\"rank\":\"normal\",\"type\":\"statement\"," +
                            "\"qualifiers\":[" +
                                "{\"datavalue\":{\"type\":\"string\",\"value\":\"1\"},\"property\":\"P490\",\"snaktype\":\"value\",\"datatype\":\"string\"}," +
                                "{\"datavalue\":{\"type\":\"string\",\"value\":\"1\"},\"property\":\"P492\",\"snaktype\":\"value\",\"datatype\":\"string\"}" +
                            "]}," +
                        "{\"mainsnak\":{\"datavalue\":{\"type\":\"string\",\"value\":\"20200306101325.000\"},\"property\":\"P116\",\"snaktype\":\"value\"},\"rank\":\"normal\",\"type\":\"statement\"}," +
                        "{\"mainsnak\":{\"datavalue\":{\"type\":\"string\",\"value\":\"http://catalogue.bnf.fr/ark:/12148/cb11862380q\"},\"property\":\"P193\",\"snaktype\":\"value\"},\"rank\":\"normal\",\"type\":\"statement\"}," +
                        "{\"mainsnak\":{\"datavalue\":{\"type\":\"string\",\"value\":\"BNF\"},\"property\":\"P162\",\"snaktype\":\"value\"},\"rank\":\"normal\",\"type\":\"statement\"}," +
                        "{\"mainsnak\":{\"datavalue\":{\"type\":\"string\",\"value\":\"20150918\"},\"property\":\"P163\",\"snaktype\":\"value\"},\"rank\":\"normal\",\"type\":\"statement\"}," +
                        "{\"mainsnak\":{\"datavalue\":{\"type\":\"string\",\"value\":\"172396506\"},\"property\":\"P120\",\"snaktype\":\"value\"},\"rank\":\"normal\",\"type\":\"statement\"}," +
                        "{\"mainsnak\":{\"datavalue\":{\"type\":\"string\",\"value\":\"sudoc\"},\"property\":\"P121\",\"snaktype\":\"value\"},\"rank\":\"normal\",\"type\":\"statement\"}," +
                        "{\"mainsnak\":{\"datavalue\":{\"type\":\"string\",\"value\":\"frBN000003046\"},\"property\":\"P122\",\"snaktype\":\"value\"},\"rank\":\"normal\",\"type\":\"statement\"}," +
                        "{\"mainsnak\":{\"datavalue\":{\"type\":\"string\",\"value\":\"FRBNF118623803\"},\"property\":\"P123\",\"snaktype\":\"value\"},\"rank\":\"normal\",\"type\":\"statement\"}," +
                        "{\"mainsnak\":{\"datavalue\":{\"type\":\"string\",\"value\":\"http://viaf.org/viaf/144248059\"},\"property\":\"P124\",\"snaktype\":\"value\"},\"rank\":\"normal\",\"type\":\"statement\"}," +
                        "{\"mainsnak\":{\"datavalue\":{\"type\":\"string\",\"value\":\"19840116afrey50      ba0\"},\"property\":\"P126\",\"snaktype\":\"value\"},\"rank\":\"normal\",\"type\":\"statement\"}" +
                    "]," +
                    "\"labels\":{\"fr\":{\"language\":\"fr\",\"value\":\"Belgique\"}}}");

            //log.info("data : "+params.get("data"));
            JSONObject json = util.postJson(urlWikiBase, params);
            log.info("==>" + json.toString());
        }
        catch (Exception e){
            log.error("Erreur : "+e.getMessage());
        }
    }

	/*
	@Test
    void createAutoriteCBS(){
    	try {
			ProcessCBS leclient = new ProcessCBS();
//En test :
			leclient.authenticate(cbsUrl, cbsPort, cbsLogin, cbsPwd);

			String[] tabparams = {"-", "+", "AUT", "MTI", "K", "KOR", "UNU", "I", "UNM", "9999", "Y", "*", "*", "*", "*", "0", "0", "A", "SYS", "LAN", "YOP", "0", "A"};
			String ress2=leclient.setParams(tabparams);

			String leXml = "008 $aTp5\n" +
					"033 $aId033$2Ref2$CSourceC$d20200911\n" +
					"035 ##$aId035$2Ref2$CSourceC$dDated\n" +
					"101 ##$afre\n" +
					"102 ##$aFR\n" +
					"103 ##$a1980\n" +
					"106 ##$a0$b1$c0\n" +
					"120 ##$ab\n" +
					"200 #1$5f$90y$aTest$bAbes$f1980-....\n" +
					"340 ##$a340a\n" +
					"400 #1$9#y$aNomA$bPrenomB\n" +
					"500 ##$3149800681\n" +
					"810 ##$a810a";
			String resu = leclient.enregistrerNewAut(leXml);

			log.info("resu: "+resu);
			String Str1D = new String(new char[]{(char) 29});
			log.info("ppn : "+java.net.URLEncoder.encode(Utilitaire.recupEntre(resu, "03", Str1D), "UTF-8"));

		}
		catch (Exception e){
    		log.error("error:"+e.getMessage());
		}
	}*/
}
