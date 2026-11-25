
package it.finmatica.gestionedocumenti.ws.smartdesktop;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per taskDettaglio complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="taskDettaglio">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="nomeDettaglio" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="valoreDettaglio" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="url" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "taskDettaglio", propOrder = {
    "nomeDettaglio",
    "valoreDettaglio",
    "url"
})
public class TaskDettaglio {

    @XmlElement(required = true)
    protected String nomeDettaglio;
    @XmlElement(required = true)
    protected String valoreDettaglio;
    protected int url;

    /**
     * Recupera il valore della propriet� nomeDettaglio.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNomeDettaglio() {
        return nomeDettaglio;
    }

    /**
     * Imposta il valore della propriet� nomeDettaglio.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNomeDettaglio(String value) {
        this.nomeDettaglio = value;
    }

    /**
     * Recupera il valore della propriet� valoreDettaglio.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValoreDettaglio() {
        return valoreDettaglio;
    }

    /**
     * Imposta il valore della propriet� valoreDettaglio.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValoreDettaglio(String value) {
        this.valoreDettaglio = value;
    }

    /**
     * Recupera il valore della propriet� url.
     * 
     */
    public int getUrl() {
        return url;
    }

    /**
     * Imposta il valore della propriet� url.
     * 
     */
    public void setUrl(int value) {
        this.url = value;
    }

}
