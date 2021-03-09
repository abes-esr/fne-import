//
// Ce fichier a �t� g�n�r� par l'impl�mentation de r�f�rence JavaTM Architecture for XML Binding (JAXB), v2.2.8-b130911.1802 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apport�e � ce fichier sera perdue lors de la recompilation du sch�ma source. 
// G�n�r� le : 2020.10.08 � 02:06:55 PM CEST 
//


package fr.fne.batch.model.autorite;


import javax.xml.bind.annotation.*;
import java.math.BigInteger;
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
 *         &lt;element ref="{}subfield" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="ind1" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="ind2" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="tag" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "subfield"
})
@XmlRootElement(name = "datafield")
public class Datafield {

    @XmlElement(required = true)
    protected List<Subfield> subfield;
    @XmlAttribute(name = "ind1", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String ind1;
    @XmlAttribute(name = "ind2", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String ind2;
    @XmlAttribute(name = "tag", required = true)
    protected String tag;

    /**
     * Gets the value of the subfield property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the subfield property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSubfield().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Subfield }
     * 
     * 
     */
    public List<Subfield> getSubfield() {
        if (subfield == null) {
            subfield = new ArrayList<Subfield>();
        }
        return this.subfield;
    }

    /**
     * Obtient la valeur de la propri�t� ind1.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInd1() {
        return ind1;
    }

    /**
     * D�finit la valeur de la propri�t� ind1.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInd1(String value) {
        this.ind1 = value;
    }

    /**
     * Obtient la valeur de la propri�t� ind2.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInd2() {
        return ind2;
    }

    /**
     * D�finit la valeur de la propri�t� ind2.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInd2(String value) {
        this.ind2 = value;
    }

    /**
     * Obtient la valeur de la propri�t� tag.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public String getTag() {
        return tag;
    }

    /**
     * D�finit la valeur de la propri�t� tag.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTag(String value) {
        this.tag = value;
    }


    @Override
    public String toString() {
        return "Datafield{" +
                "subfield=" + subfield +
                ", ind1='" + ind1 + '\'' +
                ", ind2='" + ind2 + '\'' +
                ", tag=" + tag +
                '}';
    }
}
