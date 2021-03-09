package fr.fne.batch;

import fr.fne.batch.services.LoadSubset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import fr.fne.batch.services.DeleteWB;
import fr.fne.batch.services.LoadAll;
import fr.fne.batch.services.test.BasicOAuth1OwnerOnlyConnexion;
import fr.fne.batch.services.test.OAuth1ThreeStepsConnexion;
import fr.fne.batch.services.test.OAuth2ThreeStepsConnexion;

@SpringBootApplication
public class BatchApplication implements CommandLineRunner {
	@Autowired
    private LoadAll loadAll;
	@Autowired
	private LoadSubset loadSubset;
	@Autowired
    private DeleteWB deleteWB;
	
	@Autowired
    private BasicOAuth1OwnerOnlyConnexion basicOAuth1OwnerOnlyConnexion;
	@Autowired
	private OAuth1ThreeStepsConnexion oAuth1ThreeStepsConnexion;
	@Autowired
	private OAuth2ThreeStepsConnexion oAuth2ThreeStepsConnexion;
	
	@Value("${urlWikiBase}")
    private String urlWikiBase;	
	private final Logger logger = LoggerFactory.getLogger(BatchApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(BatchApplication.class, args);
	}

	public void run(String... args) throws Exception {		
		
		// To avoid application waiting forever if an url is not available
		System.setProperty("sun.net.client.defaultConnectTimeout", "20000");
		System.setProperty("sun.net.client.defaultReadTimeout", "20000");				

		// Get parameters
		if (args.length > 0) {						
			String action = args[0];
			if (action.equalsIgnoreCase("loadSubset")) {
				//The second parameter "subsetFile" can be something like "C:/[...]/something.txt"
                //If subsetFile, by default the file is : resources/EchantillonPPN.txt
				loadSubset.go(urlWikiBase,"");
			}
			else if (action.equalsIgnoreCase("loadAll")) {
				loadAll.go(urlWikiBase);
			}
			else if (action.equalsIgnoreCase("delete")) {
				deleteWB.go(urlWikiBase);
			}
			else if (action.equalsIgnoreCase("test")) {
				//basicOAuth1OwnerOnlyConnexion.go(urlWikiBase);
				//oAuth1ThreeStepsConnexion.go(urlWikiBase);
				oAuth2ThreeStepsConnexion.go(urlWikiBase);
			}
			
			if (args.length>1) {
				urlWikiBase = args[1];
			}
			
		} else {
			logger.info("BatchApplication : no parameter");
			logger.info("Choose : loadSubset|loadInit|delete|test and, optionnaly, urlWikiBase");
		}							
	}

}
