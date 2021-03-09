//
// Ce fichier a �t� g�n�r� par l'impl�mentation de r�f�rence JavaTM Architecture for XML Binding (JAXB), v2.2.8-b130911.1802 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apport�e � ce fichier sera perdue lors de la recompilation du sch�ma source. 
// G�n�r� le : 2020.10.08 � 02:06:55 PM CEST 
//


package fr.fne.batch.model.autorite;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the generated package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Leader_QNAME = new QName("", "leader");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: generated
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Subfield }
     * 
     */
    public Subfield createSubfield() {
        return new Subfield();
    }

    /**
     * Create an instance of {@link Datafield }
     *
     */
    public Datafield createDatafield() {
        return new Datafield();
    }

    /**
     * Create an instance of {@link Record }
     *
     */
    public Record createRecord() {
        return new Record();
    }

    /**
     * Create an instance of {@link Controlfield }
     * 
     */
    public Controlfield createControlfield() {
        return new Controlfield();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "leader")
    public JAXBElement<String> createLeader(String value) {
        return new JAXBElement<String>(_Leader_QNAME, String.class, null, value);
    }

}
