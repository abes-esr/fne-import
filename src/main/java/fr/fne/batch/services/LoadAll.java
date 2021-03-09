package fr.fne.batch.services;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import oracle.xdb.XMLType;

import fr.fne.batch.services.util.api.Tool;
import fr.fne.batch.services.util.entities.Entities;
import fr.fne.batch.services.util.entities.Properties;

/*
 * This batch class can load a whole wikibase instance
 *
 * Doc to the wikibase API used : http://fagonie-dev.v102.abes.fr:8181/w/api.php?action=help
 *
 * Go to see ? https://github.com/Wikidata/Wikidata-Toolkit
 * 
 */
@Service
public class LoadAll {

	private final Logger logger = Logger.getLogger(LoadAll.class);

    @Autowired
    private Tool util;
    @Autowired
    private Properties properties;
    @Autowired
    private Entities entities;
    @Autowired
    private JdbcTemplate jdbcTemplate;

	// Test avec : http://fagonie-dev.v102.abes.fr:8181/
	public void go(String urlWikiBase) {
		long start = System.currentTimeMillis();
		int recordNb = 0;
		int idx = 1;
		int jump = 1000;

		logger.info("LoadAll starts :");

		try {
			/*
			 * Inspired by :
			 * http://baskauf.blogspot.com/2019/06/putting-data-into-wikidata-using.html
			 * 
			 * Wikibase model explanation :
			 * https://edutechwiki.unige.ch/fr/Tutoriel_wikidata#Modules_MediaWiki_API
			 * 
			 * ex in python :
			 * https://github.com/HeardLibrary/digital-scholarship/blob/master/code/wikibase
			 * /api/load_csv.py
			 */

			// Connect to WikiBase and get the necessary csrftoken
			util.setOAuth(false);
			String csrftoken = util.connexionWB(urlWikiBase);
			logger.info("The csrftoken is : " + csrftoken);

            //You can force the creation of properties :
            //properties.create(urlWikiBase,csrftoken);

            //Get all properties needed (to know the wikibase ID)
            Map<String,String> props;
			props = properties.get(urlWikiBase,csrftoken);
			logger.info("Number of properties loaded : "+props.size());

			// jump (1000) x 10000 = 10 000 000 => this class can load up to 10 000 000 records
			// 36h = 1 000 000 loaded records (with 2 properties :) ) From a dev VM : pivoine-dev
			for (int i = 0; i < 10000; i++) { // By packets, because there is a slow down of the database connection
				List<Map<String, Object>> rows = jdbcTemplate.queryForList(
						"select id, XMLROOT(data_xml,version '1.0\" encoding=\"UTF-8') as data_xml from notices where id in "
								+ "    (select id from aut_table_generale where typerecord!='d' " +
									// startDate +
									// endDate +
								"and id>" + idx + " and id<=" + (idx + jump) + "    ) ");

				for (Map row : rows) {
                    recordNb++;
                    String id = ((BigDecimal) row.get("id")).toString();
                    logger.info("Id processed : " + id);
                    entities.insert(urlWikiBase,csrftoken,props,((XMLType) row.get("data_xml")).getStringVal());
				}

				idx = idx + jump;
			}
		} catch (Exception e) {
			logger.error("LoadAll pb : " + e.getMessage());
			e.printStackTrace();
		}

		long last = System.currentTimeMillis() - start;
		logger.info("LoadAll ended, execution time : " + last + " | records processed : " + recordNb);
	}

}
