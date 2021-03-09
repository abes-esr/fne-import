//
// Ce fichier a �t� g�n�r� par l'impl�mentation de r�f�rence JavaTM Architecture for XML Binding (JAXB), v2.2.8-b130911.1802 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apport�e � ce fichier sera perdue lors de la recompilation du sch�ma source. 
// G�n�r� le : 2020.10.08 � 02:06:55 PM CEST 
//


package fr.fne.batch.model.autorite;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Classe Java pour anonymous complex type.
 * 
 * <p>Le fragment de sch�ma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}leader"/>
 *         &lt;element ref="{}controlfield" maxOccurs="unbounded"/>
 *         &lt;element ref="{}datafield" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "leader",
    "controlfield",
    "datafield"
})
@XmlRootElement(name = "record")
public class Record {

    @XmlElement(required = true)
    protected String leader;
    @XmlElement(required = true)
    protected List<Controlfield> controlfield;
    @XmlElement(required = true)
    protected List<Datafield> datafield;

    /**
     * Obtient la valeur de la propri�t� leader.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getLeader() {
        return leader;
    }

    /**
     * D�finit la valeur de la propri�t� leader.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setLeader(String value) {
        this.leader = value;
    }

    /**
     * Gets the value of the controlfield property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the controlfield property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getControlfield().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Controlfield }
     *
     *
     */
    public List<Controlfield> getControlfield() {
        if (controlfield == null) {
            controlfield = new ArrayList<Controlfield>();
        }
        return this.controlfield;
    }

    /**
     * Gets the value of the datafield property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the datafield property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDatafield().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Datafield }
     *
     *
     */
    public List<Datafield> getDatafield() {
        if (datafield == null) {
            datafield = new ArrayList<Datafield>();
        }
        return this.datafield;
    }

    @Override
    public String toString() {
        return "Record{" +
                "leader='" + leader + '\'' +
                ", controlfield=" + controlfield +
                ", datafield=" + datafield +
                '}';
    }
}
