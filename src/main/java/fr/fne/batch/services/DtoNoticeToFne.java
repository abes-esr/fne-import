package fr.fne.batch.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.fne.batch.model.autorite.Controlfield;
import fr.fne.batch.model.autorite.Datafield;
import fr.fne.batch.model.autorite.Record;
import fr.fne.batch.model.autorite.Subfield;
import fr.fne.batch.model.fne.*;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;

@Service
public class DtoNoticeToFne {

    private final Logger logger = Logger.getLogger(DtoNoticeToFne.class);

    public static final List<String> TAGVALUE = Arrays.asList("900", "910", "915", "920", "930", "940", "950", "960", "980");

    /**
     * DTO to optimize next sprint .
     * Notice unimarc to RECORD objet and Data Transfert Objet to Fne Json DataValue Objet
     * @param noticeAutorite
     * @param props
     * @return
     */
    public String unmarshallerNotice(String noticeAutorite, Map<String,String> props)
    {

        String datanew = "";

        try {
            JAXBContext context = JAXBContext.newInstance(Record.class);
            Unmarshaller u = context.createUnmarshaller();

            StringReader reader = new StringReader(noticeAutorite);
            Record r = new Record();

            try {
                r = (Record) u.unmarshal(reader);
                //  Record r = (Record)u.unmarshal(new File("src/main/resources/notice.xml"));
                r.getLeader();

                //logger.info(r.toString());
            } catch (javax.xml.bind.UnmarshalException e) {
                logger.error("Main: " + e);
            }

            // DTO

            List<Claims> lcl = new ArrayList<Claims>();
            List<Claims> lcldatafield = new ArrayList<Claims>();
            List<LabelTag> llabel = new ArrayList<>();

            String dernierTag="";
            int i = 1;

            String labelValue = labelTag900CodeA(r);


            for (Datafield d : r.getDatafield()) {
                // Gestion correspondance tag - propertie : ind1 + ind2 + $ + code + _i (if start 035 or 400)

                //Gestion des indices : si les 2 sont vides, on ne met rien.
                //Sinon on remplace les vides par des #
                String indices = "";
                if (!d.getInd1().trim().equals("") || !d.getInd2().trim().equals("")){
                    if (d.getInd1().trim().equals("")){
                        indices +="#";
                    }
                    else {
                        indices +=d.getInd1().trim();
                    }
                    if (d.getInd2().trim().equals("")){
                        indices +="#";
                    }
                    else {
                        indices +=d.getInd2().trim();
                    }
                }

                String tagd = String.format("%03d", parseInt(d.getTag())) + indices + "$";

                //Si le nom du datafield change (alors remise à 1 de l'index), sinon +1
                //Exemple si passage de 120$ à 152$ i=1    par contre si passage du 1er 035$ au second, alors i+1, etc.
                if (dernierTag.equals(tagd)){
                    i++;
                }
                else {
                    i = 1;
                }

                for (Subfield s : d.getSubfield()) {
                    //logger.info(tagd+" "+s.getCode()+" "+i);


                    /** gestion label **/
                    // recuperation tag et code pour chaque subfield
                    LabelTag lt = new LabelTag();
                    lt.setTag(d.getTag().toString());
                    lt.setCode(s.getCode());
                    llabel.add(lt);



                    /** gestion tag **/
                    Boolean isPropsd = false;
                    // gestion tag
                    String tagcode = tagd + s.getCode();
                    //Zones répétables
                    if (tagcode.startsWith("035") || tagcode.startsWith("400") || tagcode.startsWith("500")) {
                        tagcode += "_" + i;
                    }

                    String tagPropsd = props.get(tagcode);

                    if (tagPropsd != null) {
                        isPropsd = true;
                    }
                    /** fin creation tag **/

                    Claims cld = new Claims();
                    Mainsnak sd = new Mainsnak();
                    Datavalue dd = new Datavalue();
                    dd.setType("string");
                    dd.setValue(s.getValue());
                    sd.setProperty(tagPropsd);
                    sd.setDatavalue(dd);
                    sd.setSnaktype("value");
                    cld.setMainsnak(sd);
                    cld.setType("statement");
                    cld.setRank("normal");

                    if (isPropsd) {
                        lcldatafield.add(cld);
                    }
                }
                //i++;
                dernierTag=tagd;
            }

            List<Claims> cld = lcldatafield
                    .stream()
                    .collect(Collectors.toList());

            ObjectMapper mapperd = new ObjectMapper();
            String jsonInStringdatafield = mapperd.writeValueAsString(cld);
            //logger.info(jsonInStringdatafield);

            for (Controlfield c : r.getControlfield()) {
                Boolean isProps = false;
                Claims cl = new Claims();
                Mainsnak s = new Mainsnak();
                Datavalue d = new Datavalue();

                d.setType("string");
                d.setValue(c.getValue());

                s.setSnaktype("value");

                // gestion tag => props
                String str = String.format("%03d", c.getTag());

                String tagControlfield = str;
                String tagProps = props.get(tagControlfield);

                if (tagProps != null) {
                    isProps = true;
                }
                //logger.info(tagProps);


                s.setProperty(tagProps);
                s.setDatavalue(d);

                cl.setType("statement");
                cl.setRank("normal");
                cl.setMainsnak(s);

                if (isProps) {
                    lcl.add(cl);
                }

            }

            // concat claims
            //  lcl.stream().forEachOrdered(lcldatafield::add);

            List<Claims> cl = lcl
                    .stream()
                    .collect(Collectors.toList());

            cl.addAll(cld);

            JSONObject labelval = new JSONObject();

            labelval.put("language", "fr");
            labelval.put("value", labelValue);

            JSONObject label = new JSONObject();
            label.put("fr", labelval);

            JSONObject jo = new JSONObject();
            jo.put("labels", label);
            jo.put("claims", cl);

           datanew = jo.toString();
        }
        catch (Exception e) {
            logger.error("Error on the record : " + noticeAutorite + " :" + e.getMessage());
            e.printStackTrace();
        }

        return datanew;
    }

    /**
     * Get Value Label from Datafield Tag = 900 .. 980 and Subfield Code = a
     * @param
     * @return
     */
    private String labelTag900CodeA(Record r) {

        String titreLabel =
                r.getDatafield()
                        .stream()
                        .filter( t ->  TAGVALUE.stream()
                                .anyMatch(v -> t.getTag().equals(v))

                        )
                        .limit(1)
                        .flatMap(d -> d.getSubfield().stream())
                        .filter(code -> code.getCode().equals("a"))
                        .map(Subfield::getValue)
                        .findFirst()
                        .orElseGet(() -> "default");

        //logger.info(titreLabel);

        return titreLabel;
    }

}
