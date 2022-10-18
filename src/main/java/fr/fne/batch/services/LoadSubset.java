package fr.fne.batch.services;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fr.fne.batch.services.util.bdd.DatabaseInsert;
import fr.fne.batch.services.util.entities.EntitiesJSOUP;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
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
    private EntitiesJSOUP entities;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Test with : http://fagonie-dev.v102.abes.fr:8181/
    public void go(String subsetFile) {




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
            String csrftoken = util.connexionWB();
            logger.info("The csrftoken is : " + csrftoken);

            // Création des propriétés : commenté ici car elles sont déjà présentes dans le wikibase
            //properties.createWithFile(csrftoken);

            //Get all properties (defined in resources/Properties.txt) needed, to know the corresponding WikiBase IDs
            Map<String,String> props;
            props = properties.get(csrftoken);
            logger.info("Number of properties loaded : "+props.size());



            //Test chargement direct en BDD
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/my_wiki?characterEncoding=utf-8",
                    "sqluser",
                    "change-this-sqlpassword");

            DatabaseInsert di = new DatabaseInsert(connection);
            final StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            di.startTransaction();


            //Connect to the database and get the records
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "select id, ppn, XMLROOT(data_xml,version '1.0\" encoding=\"UTF-8') as data_xml from notices where id in "
                            + "    (select id from aut_table_generale where typerecord!='d' and ppn in ("+ idOk+ ") ) ");

            for (Map<String, Object> row : rows) {
                recordNb++;
                String ppn = row.get("ppn").toString();
                logger.info("PPN à insérer : " + ppn);
                //DatabaseInsert di appelé avec createItem
                entities.insert(di, csrftoken,props,((XMLType) row.get("data_xml")).getStringVal());
            }

            di.commit();
            stopWatch.stop();

            logger.info("Created "+recordNb+" items in {} s." + stopWatch.getTime());
            logger.info("Speed is "+(int) (recordNb * 60 / (double) stopWatch.getTime())+" items/minute.");

        } catch (Exception e) {
            logger.error("LoadSubset pb : " + e.getMessage());
            e.printStackTrace();
        }

        long last = System.currentTimeMillis() - start;
        logger.info("LoadSubset ended, execution time : " + last + " | records processed : " + recordNb);
    }

}
