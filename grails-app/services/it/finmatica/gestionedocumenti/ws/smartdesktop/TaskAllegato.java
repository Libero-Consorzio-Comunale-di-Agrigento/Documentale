
package it.finmatica.gestionedocumenti.ws.smartdesktop;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per taskAllegato complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="taskAllegato">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="etichetta" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="tooltip" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="nomeFile" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="url" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="mimetype" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="priorita" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="firmato" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "taskAllegato", propOrder = {
    "etichetta",
    "tooltip",
    "nomeFile",
    "url",
    "mimetype",
    "priorita",
    "firmato"
})
public class TaskAllegato {

    @XmlElement(required = true)
    protected String etichetta;
    protected String tooltip;
    @XmlElement(required = true)
    protected String nomeFile;
    @XmlElement(required = true)
    protected String url;
    protected String mimetype;
    protected int priorita;
    protected int firmato;

    /**
     * Recupera il valore della propriet� etichetta.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEtichetta() {
        return etichetta;
    }

    /**
     * Imposta il valore della propriet� etichetta.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEtichetta(String value) {
        this.etichetta = value;
    }

    /**
     * Recupera il valore della propriet� tooltip.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTooltip() {
        return tooltip;
    }

    /**
     * Imposta il valore della propriet� tooltip.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTooltip(String value) {
        this.tooltip = value;
    }

    /**
     * Recupera il valore della propriet� nomeFile.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNomeFile() {
        return nomeFile;
    }

    /**
     * Imposta il valore della propriet� nomeFile.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNomeFile(String value) {
        this.nomeFile = value;
    }

    /**
     * Recupera il valore della propriet� url.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUrl() {
        return url;
    }

    /**
     * Imposta il valore della propriet� url.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUrl(String value) {
        this.url = value;
    }

    /**
     * Recupera il valore della propriet� mimetype.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMimetype() {
        return mimetype;
    }

    /**
     * Imposta il valore della propriet� mimetype.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMimetype(String value) {
        this.mimetype = value;
    }

    /**
     * Recupera il valore della propriet� priorita.
     * 
     */
    public int getPriorita() {
        return priorita;
    }

    /**
     * Imposta il valore della propriet� priorita.
     * 
     */
    public void setPriorita(int value) {
        this.priorita = value;
    }

    /**
     * Recupera il valore della propriet� firmato.
     * 
     */
    public int getFirmato() {
        return firmato;
    }

    /**
     * Imposta il valore della propriet� firmato.
     * 
     */
    public void setFirmato(int value) {
        this.firmato = value;
    }

}
