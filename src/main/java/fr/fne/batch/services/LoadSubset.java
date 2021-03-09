package fr.fne.batch.services;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import oracle.xdb.XMLType;

import fr.fne.batch.services.util.api.Tool;
import fr.fne.batch.services.util.entities.Properties;
import fr.fne.batch.services.util.entities.Entities;

/*
 * Batch used to load a subset find in a given file
 */
@Service
public class LoadSubset {

    private final Logger logger = Logger.getLogger(LoadSubset.class);

    @Autowired
    private Tool util;
    @Autowired
    private Properties properties;
    @Autowired
    private Entities entities;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Test with : http://fagonie-dev.v102.abes.fr:8181/
    public void go(String urlWikiBase, String subsetFile) {
        long start = System.currentTimeMillis();
        int recordNb = 0;

        logger.info("LoadSubset starts :");

        try {
            //Get id records (PPN) to be loaded in the WikiBase instance
            File file;
            if (subsetFile.isEmpty()){
                file = new ClassPathResource("EchantillonPPN.txt").getFile();
            }
            else {
                file = new File(subsetFile);
            }
            List<String> lines = FileUtils.readLines(file, StandardCharsets.UTF_8);
            Iterator<String> line = lines.iterator();
            StringBuilder idOk = new StringBuilder();
            while (line.hasNext()){
                idOk.append("'"+line.next()+"',");
            }
            if (idOk.length()>0) {
                idOk=new StringBuilder(idOk.substring(0,idOk.length()-1));
            }
            //logger.info("===>"+idOk);

            // Connextion à Wikibase et récupération du csrftoken
            util.setOAuth(false);
            String csrftoken = util.connexionWB(urlWikiBase);
            logger.info("The csrftoken is : " + csrftoken);

            // Création des propriétés : commenté ici car elles sont déjà présentes dans le wikibase
            //properties.create(urlWikiBase,csrftoken);

            //Get all properties (defined in resources/Properties.txt) needed, to know the corresponding WikiBase IDs
            Map<String,String> props;
            props = properties.get(urlWikiBase,csrftoken);
            logger.info("Number of properties loaded : "+props.size());

            //Connect to the database and get the records
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "select id, ppn, XMLROOT(data_xml,version '1.0\" encoding=\"UTF-8') as data_xml from notices where id in "
                            + "    (select id from aut_table_generale where typerecord!='d' and ppn in ("+ idOk+ ") ) ");

            for (Map<String, Object> row : rows) {
                recordNb++;
                String ppn = row.get("ppn").toString();
                logger.info("PPN à insérer : " + ppn);
                entities.insert(urlWikiBase,csrftoken,props,((XMLType) row.get("data_xml")).getStringVal());
            }

            //C'est moche, mais il faut laisser du temps pr que les notices créées soient présentes dans le WDQS
            //TODO : créer une nouvelle classe qui ne traitera que les liens ?
            Thread.sleep(20000);

            // Url du WDQS (WikiBase Query Service) :
            // TODO : le passer en paramètre ?
            String urlWdqs = urlWikiBase.substring(0,urlWikiBase.lastIndexOf(':'));
            urlWdqs += ":8282/proxy/wdqs/bigdata/namespace/wdq/sparql";

            //EN COURS Ajt lien 500$3 :
            //TODO : Ne conserver que le premier PPN de l'échantillon pour ce dév : 026726920
            //Pour chaque entité/notice qui vient d'être insérée, il faut :
            for (Map<String, Object> row : rows) {
                //TODO : requête sur chaque PPN qui vient d'être ajouté dans wikibase :
                String ppn = row.get("ppn").toString();
                logger.info("PPN à lier : " + ppn);
                entities.link(urlWikiBase,urlWdqs,csrftoken,props,ppn);
            }
            //FIN ENCOURS Ajt lien 500

        } catch (Exception e) {
            logger.error("LoadSubset pb : " + e.getMessage());
            e.printStackTrace();
        }

        long last = System.currentTimeMillis() - start;
        logger.info("LoadSubset ended, execution time : " + last + " | records processed : " + recordNb);
    }

}
