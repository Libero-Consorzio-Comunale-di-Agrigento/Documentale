
package it.finmatica.gestionedocumenti.ws.smartdesktop;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per taskBottone complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="taskBottone">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="etichetta" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="tooltip" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="icona" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="tipologia" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="multiplo" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="urlAzione" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="parametri" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="identificativoRiferimento" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "taskBottone", propOrder = {
    "etichetta",
    "tooltip",
    "icona",
    "tipologia",
    "multiplo",
    "urlAzione",
    "parametri",
    "identificativoRiferimento"
})
public class TaskBottone {

    @XmlElement(required = true)
    protected String etichetta;
    @XmlElement(required = true)
    protected String tooltip;
    @XmlElement(required = true)
    protected String icona;
    @XmlElement(required = true)
    protected String tipologia;
    protected int multiplo;
    @XmlElement(required = true)
    protected String urlAzione;
    @XmlElement(required = true)
    protected String parametri;
    @XmlElement(required = true)
    protected String identificativoRiferimento;

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
     * Recupera il valore della propriet� icona.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIcona() {
        return icona;
    }

    /**
     * Imposta il valore della propriet� icona.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIcona(String value) {
        this.icona = value;
    }

    /**
     * Recupera il valore della propriet� tipologia.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTipologia() {
        return tipologia;
    }

    /**
     * Imposta il valore della propriet� tipologia.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTipologia(String value) {
        this.tipologia = value;
    }

    /**
     * Recupera il valore della propriet� multiplo.
     * 
     */
    public int getMultiplo() {
        return multiplo;
    }

    /**
     * Imposta il valore della propriet� multiplo.
     * 
     */
    public void setMultiplo(int value) {
        this.multiplo = value;
    }

    /**
     * Recupera il valore della propriet� urlAzione.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUrlAzione() {
        return urlAzione;
    }

    /**
     * Imposta il valore della propriet� urlAzione.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUrlAzione(String value) {
        this.urlAzione = value;
    }

    /**
     * Recupera il valore della propriet� parametri.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParametri() {
        return parametri;
    }

    /**
     * Imposta il valore della propriet� parametri.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParametri(String value) {
        this.parametri = value;
    }

    /**
     * Recupera il valore della propriet� identificativoRiferimento.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdentificativoRiferimento() {
        return identificativoRiferimento;
    }

    /**
     * Imposta il valore della propriet� identificativoRiferimento.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdentificativoRiferimento(String value) {
        this.identificativoRiferimento = value;
    }

}
