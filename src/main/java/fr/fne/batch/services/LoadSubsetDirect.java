package fr.fne.batch.services;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fr.fne.batch.services.util.bdd.DatabaseInsert;
import fr.fne.batch.services.util.entities.EntitiesJSOUP;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import oracle.xdb.XMLType;

import fr.fne.batch.services.util.api.Tool;
import fr.fne.batch.services.util.entities.Properties;

/*
 * Batch used to load a subset find in a given file
 */
@Service
public class LoadSubsetDirect {

    private final Logger logger = Logger.getLogger(LoadSubsetDirect.class);

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

            // Création des propriétés :
            properties.createWithFile(csrftoken);

            //Get all properties (defined in resources/Properties.txt) needed, to know the corresponding WikiBase IDs
            Map<String,String> props;
            props = properties.get(csrftoken);
            logger.info("Number of properties loaded : "+props.size());



            //Test chargement direct en BDD
            //Supprimer l'option &rewriteBatchedStatements=true si pas d'executeBatch utilisé dans DatabaseInsert
            Connection connection = DriverManager.getConnection(
                    "jdbc:mariadb://localhost:3306/my_wiki?characterEncoding=utf-8&rewriteBatchedStatements=true",
                    "sqluser",
                    "change-this-sqlpassword");

            DatabaseInsert di = new DatabaseInsert(connection);
            final StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            di.startTransaction();


            String entity = null;

            //Utilisation d'un select sur la base XML avec les ppn de l'échantillon :
        /*    List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "select id, ppn, XMLROOT(data_xml,version '1.0\" encoding=\"UTF-8') as data_xml from notices where id in "
                            + "    (select id from aut_table_generale where typerecord!='d' and ppn in ("+ idOk+ ") ) ");

               for (Map<String, Object> row : rows) {

                    recordNb++;

                    //String ppn = row.get("ppn").toString();
                    //logger.info("PPN à insérer : " + ppn);

                    //=> Avec les executeBatch et l'option &rewriteBatchedStatements=true et la ligne de log désactivée ci-dessus)
                    //INFO  [main] fr.fne.batch.services.LoadSubsetDirect - Created 855 items in 31 s.
                    //INFO  [main] fr.fne.batch.services.LoadSubsetDirect - Speed is 1654 items/minute.

                    //=> Sans insertion SQL, juste le select et la transfo en JSON :
                    //INFO  [main] fr.fne.batch.services.LoadSubsetDirect - Created 855 items in 13 s.
                    //INFO  [main] fr.fne.batch.services.LoadSubsetDirect - Speed is 3946 items/minute.

                    entity = entities.get(csrftoken,props,((XMLType) row.get("data_xml")).getStringVal());

                    //Par Dump :
                    entity = entities.get(csrftoken, props, record.toString());

                    if (entity != null) {
                        di.createItem(entity);
                    }
                }
            }
         */


            //Utilisation d'un dump des notices :
            //Le dump est disponible ici : /applis/portail/SitemapNoticesSudoc/noticesautorites/dump
            File[] fichiers = new File("C:/dump/").listFiles();

            int lanceCommit = 0;

            for (int i=0;i<fichiers.length;i++) {
                Document collection = Jsoup.parse(new FileInputStream(fichiers[i]), "UTF-8", "", Parser.xmlParser());

                Elements records = collection.getElementsByTag("record");
                for (int j = 0; j < records.size(); j++) {
                    Element record = records.get(j);

                    recordNb++;

                    /*
                    lanceCommit++;

                    if (lanceCommit==1000){
                        di.commit();
                        lanceCommit=0;
                    }*/

                    //String ppn = row.get("ppn").toString();
                    //logger.info("PPN à insérer : " + ppn);

                    //Avec commit tous les 1000
//INFO  [main] fr.fne.batch.services.LoadSubsetDirect - Created 16672 items in 378 s.
//INFO  [main] fr.fne.batch.services.LoadSubsetDirect - Speed is 44 items/second.

                    //Sans commit tous les 1000
//INFO  [main] fr.fne.batch.services.LoadSubsetDirect - Created 16672 items in 354 s.
//INFO  [main] fr.fne.batch.services.LoadSubsetDirect - Speed is 47 items/second.

                    entity = entities.get(csrftoken, props, record.toString());

                    if (entity != null) {
                        di.createItem(entity);
                    }
                }
            }



            di.commit();
            stopWatch.stop();

            logger.info("Created "+recordNb+" items in " + stopWatch.getTime(TimeUnit.SECONDS)+" s.");
            logger.info("Speed is "+(int) (recordNb / (double) stopWatch.getTime(TimeUnit.SECONDS))+" items/second.");

        } catch (Exception e) {
            logger.error("LoadSubset pb : " + e.getMessage());
            e.printStackTrace();
        }

        long last = System.currentTimeMillis() - start;
        logger.info("LoadSubset ended, execution time : " + last + " | records processed : " + recordNb);
    }

}
