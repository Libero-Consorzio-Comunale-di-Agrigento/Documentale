
package it.finmatica.gestionedocumenti.ws.smartdesktop;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Classe Java per task complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="task">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="descrizione" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="tooltipDescrizione" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="tipologia" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="tipologiaDescrizione" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="applicativoChiamante" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="dataAttivazione" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="dataScadenza" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="urlVisualizzazione" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="descrizioneUrlVisualizzazione" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="urlEsecuzione" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="descrizioneUrlEsecuzione" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="utente" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ente" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="idRiferimentoEsterno" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="taskTipoTODO" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="messaggioTaskTipoTODO" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="paramInitIter" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="nomeIter" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="descrizioneIter" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="colore" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ordinamento" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="categoria" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="desktop" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="stato" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dataArrivo" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="datiApplicativi1" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="datiApplicativi2" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="datiApplicativi3" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="livelloPriorita" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="note" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ordinamentoStringaLabel" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ordinamentoStringaValore" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ordinamentoDataLabel" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ordinamentoDataValore" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="ordinamentoNumeroLabel" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ordinamentoNumeroValore" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="urlAllegatiDinamici" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="taskDettaglio" type="{http://webservice.jworklist.finmatica.it/}taskDettaglio" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="taskAllegato" type="{http://webservice.jworklist.finmatica.it/}taskAllegato" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="taskBottone" type="{http://webservice.jworklist.finmatica.it/}taskBottone" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="taskUtenti" type="{http://webservice.jworklist.finmatica.it/}taskUtenti" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "task", propOrder = {
    "descrizione",
    "tooltipDescrizione",
    "tipologia",
    "tipologiaDescrizione",
    "applicativoChiamante",
    "dataAttivazione",
    "dataScadenza",
    "urlVisualizzazione",
    "descrizioneUrlVisualizzazione",
    "urlEsecuzione",
    "descrizioneUrlEsecuzione",
    "utente",
    "ente",
    "idRiferimentoEsterno",
    "taskTipoTODO",
    "messaggioTaskTipoTODO",
    "paramInitIter",
    "nomeIter",
    "descrizioneIter",
    "colore",
    "ordinamento",
    "categoria",
    "desktop",
    "stato",
    "dataArrivo",
    "datiApplicativi1",
    "datiApplicativi2",
    "datiApplicativi3",
    "livelloPriorita",
    "note",
    "ordinamentoStringaLabel",
    "ordinamentoStringaValore",
    "ordinamentoDataLabel",
    "ordinamentoDataValore",
    "ordinamentoNumeroLabel",
    "ordinamentoNumeroValore",
    "urlAllegatiDinamici",
    "taskDettaglio",
    "taskAllegato",
    "taskBottone",
    "taskUtenti"
})
public class Task {

    @XmlElement(required = true)
    protected String descrizione;
    protected String tooltipDescrizione;
    @XmlElement(required = true)
    protected String tipologia;
    @XmlElement(required = true)
    protected String tipologiaDescrizione;
    @XmlElement(required = true)
    protected String applicativoChiamante;
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dataAttivazione;
    @XmlElementRef(name = "dataScadenza", type = JAXBElement.class, required = false)
    protected JAXBElement<XMLGregorianCalendar> dataScadenza;
    protected String urlVisualizzazione;
    protected String descrizioneUrlVisualizzazione;
    protected String urlEsecuzione;
    protected String descrizioneUrlEsecuzione;
    @XmlElement(required = true)
    protected String utente;
    @XmlElement(required = true)
    protected String ente;
    protected String idRiferimentoEsterno;
    protected int taskTipoTODO;
    protected String messaggioTaskTipoTODO;
    protected String paramInitIter;
    protected String nomeIter;
    protected String descrizioneIter;
    protected String colore;
    protected String ordinamento;
    protected String categoria;
    protected String desktop;
    protected String stato;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dataArrivo;
    @XmlElement(required = true, nillable = true)
    protected String datiApplicativi1;
    @XmlElement(required = true, nillable = true)
    protected String datiApplicativi2;
    @XmlElement(required = true, nillable = true)
    protected String datiApplicativi3;
    @XmlElement(required = true, nillable = true)
    protected String livelloPriorita;
    @XmlElement(required = true, nillable = true)
    protected String note;
    @XmlElementRef(name = "ordinamentoStringaLabel", type = JAXBElement.class, required = false)
    protected JAXBElement<String> ordinamentoStringaLabel;
    protected String ordinamentoStringaValore;
    @XmlElementRef(name = "ordinamentoDataLabel", type = JAXBElement.class, required = false)
    protected JAXBElement<String> ordinamentoDataLabel;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar ordinamentoDataValore;
    @XmlElementRef(name = "ordinamentoNumeroLabel", type = JAXBElement.class, required = false)
    protected JAXBElement<String> ordinamentoNumeroLabel;
    protected int ordinamentoNumeroValore;
    @XmlElementRef(name = "urlAllegatiDinamici", type = JAXBElement.class, required = false)
    protected JAXBElement<String> urlAllegatiDinamici;
    protected List<TaskDettaglio> taskDettaglio;
    protected List<TaskAllegato> taskAllegato;
    protected List<TaskBottone> taskBottone;
    protected List<TaskUtenti> taskUtenti;

    /**
     * Recupera il valore della propriet� descrizione.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescrizione() {
        return descrizione;
    }

    /**
     * Imposta il valore della propriet� descrizione.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescrizione(String value) {
        this.descrizione = value;
    }

    /**
     * Recupera il valore della propriet� tooltipDescrizione.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTooltipDescrizione() {
        return tooltipDescrizione;
    }

    /**
     * Imposta il valore della propriet� tooltipDescrizione.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTooltipDescrizione(String value) {
        this.tooltipDescrizione = value;
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
     * Recupera il valore della propriet� tipologiaDescrizione.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTipologiaDescrizione() {
        return tipologiaDescrizione;
    }

    /**
     * Imposta il valore della propriet� tipologiaDescrizione.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTipologiaDescrizione(String value) {
        this.tipologiaDescrizione = value;
    }

    /**
     * Recupera il valore della propriet� applicativoChiamante.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getApplicativoChiamante() {
        return applicativoChiamante;
    }

    /**
     * Imposta il valore della propriet� applicativoChiamante.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setApplicativoChiamante(String value) {
        this.applicativoChiamante = value;
    }

    /**
     * Recupera il valore della propriet� dataAttivazione.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDataAttivazione() {
        return dataAttivazione;
    }

    /**
     * Imposta il valore della propriet� dataAttivazione.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDataAttivazione(XMLGregorianCalendar value) {
        this.dataAttivazione = value;
    }

    /**
     * Recupera il valore della propriet� dataScadenza.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     *     
     */
    public JAXBElement<XMLGregorianCalendar> getDataScadenza() {
        return dataScadenza;
    }

    /**
     * Imposta il valore della propriet� dataScadenza.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     *     
     */
    public void setDataScadenza(JAXBElement<XMLGregorianCalendar> value) {
        this.dataScadenza = value;
    }

    /**
     * Recupera il valore della propriet� urlVisualizzazione.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUrlVisualizzazione() {
        return urlVisualizzazione;
    }

    /**
     * Imposta il valore della propriet� urlVisualizzazione.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUrlVisualizzazione(String value) {
        this.urlVisualizzazione = value;
    }

    /**
     * Recupera il valore della propriet� descrizioneUrlVisualizzazione.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescrizioneUrlVisualizzazione() {
        return descrizioneUrlVisualizzazione;
    }

    /**
     * Imposta il valore della propriet� descrizioneUrlVisualizzazione.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescrizioneUrlVisualizzazione(String value) {
        this.descrizioneUrlVisualizzazione = value;
    }

    /**
     * Recupera il valore della propriet� urlEsecuzione.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUrlEsecuzione() {
        return urlEsecuzione;
    }

    /**
     * Imposta il valore della propriet� urlEsecuzione.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUrlEsecuzione(String value) {
        this.urlEsecuzione = value;
    }

    /**
     * Recupera il valore della propriet� descrizioneUrlEsecuzione.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescrizioneUrlEsecuzione() {
        return descrizioneUrlEsecuzione;
    }

    /**
     * Imposta il valore della propriet� descrizioneUrlEsecuzione.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescrizioneUrlEsecuzione(String value) {
        this.descrizioneUrlEsecuzione = value;
    }

    /**
     * Recupera il valore della propriet� utente.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUtente() {
        return utente;
    }

    /**
     * Imposta il valore della propriet� utente.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUtente(String value) {
        this.utente = value;
    }

    /**
     * Recupera il valore della propriet� ente.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEnte() {
        return ente;
    }

    /**
     * Imposta il valore della propriet� ente.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEnte(String value) {
        this.ente = value;
    }

    /**
     * Recupera il valore della propriet� idRiferimentoEsterno.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdRiferimentoEsterno() {
        return idRiferimentoEsterno;
    }

    /**
     * Imposta il valore della propriet� idRiferimentoEsterno.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdRiferimentoEsterno(String value) {
        this.idRiferimentoEsterno = value;
    }

    /**
     * Recupera il valore della propriet� taskTipoTODO.
     * 
     */
    public int getTaskTipoTODO() {
        return taskTipoTODO;
    }

    /**
     * Imposta il valore della propriet� taskTipoTODO.
     * 
     */
    public void setTaskTipoTODO(int value) {
        this.taskTipoTODO = value;
    }

    /**
     * Recupera il valore della propriet� messaggioTaskTipoTODO.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessaggioTaskTipoTODO() {
        return messaggioTaskTipoTODO;
    }

    /**
     * Imposta il valore della propriet� messaggioTaskTipoTODO.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessaggioTaskTipoTODO(String value) {
        this.messaggioTaskTipoTODO = value;
    }

    /**
     * Recupera il valore della propriet� paramInitIter.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParamInitIter() {
        return paramInitIter;
    }

    /**
     * Imposta il valore della propriet� paramInitIter.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParamInitIter(String value) {
        this.paramInitIter = value;
    }

    /**
     * Recupera il valore della propriet� nomeIter.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNomeIter() {
        return nomeIter;
    }

    /**
     * Imposta il valore della propriet� nomeIter.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNomeIter(String value) {
        this.nomeIter = value;
    }

    /**
     * Recupera il valore della propriet� descrizioneIter.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescrizioneIter() {
        return descrizioneIter;
    }

    /**
     * Imposta il valore della propriet� descrizioneIter.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescrizioneIter(String value) {
        this.descrizioneIter = value;
    }

    /**
     * Recupera il valore della propriet� colore.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getColore() {
        return colore;
    }

    /**
     * Imposta il valore della propriet� colore.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setColore(String value) {
        this.colore = value;
    }

    /**
     * Recupera il valore della propriet� ordinamento.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrdinamento() {
        return ordinamento;
    }

    /**
     * Imposta il valore della propriet� ordinamento.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrdinamento(String value) {
        this.ordinamento = value;
    }

    /**
     * Recupera il valore della propriet� categoria.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCategoria() {
        return categoria;
    }

    /**
     * Imposta il valore della propriet� categoria.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCategoria(String value) {
        this.categoria = value;
    }

    /**
     * Recupera il valore della propriet� desktop.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDesktop() {
        return desktop;
    }

    /**
     * Imposta il valore della propriet� desktop.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDesktop(String value) {
        this.desktop = value;
    }

    /**
     * Recupera il valore della propriet� stato.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStato() {
        return stato;
    }

    /**
     * Imposta il valore della propriet� stato.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStato(String value) {
        this.stato = value;
    }

    /**
     * Recupera il valore della propriet� dataArrivo.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDataArrivo() {
        return dataArrivo;
    }

    /**
     * Imposta il valore della propriet� dataArrivo.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDataArrivo(XMLGregorianCalendar value) {
        this.dataArrivo = value;
    }

    /**
     * Recupera il valore della propriet� datiApplicativi1.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDatiApplicativi1() {
        return datiApplicativi1;
    }

    /**
     * Imposta il valore della propriet� datiApplicativi1.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDatiApplicativi1(String value) {
        this.datiApplicativi1 = value;
    }

    /**
     * Recupera il valore della propriet� datiApplicativi2.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDatiApplicativi2() {
        return datiApplicativi2;
    }

    /**
     * Imposta il valore della propriet� datiApplicativi2.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDatiApplicativi2(String value) {
        this.datiApplicativi2 = value;
    }

    /**
     * Recupera il valore della propriet� datiApplicativi3.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDatiApplicativi3() {
        return datiApplicativi3;
    }

    /**
     * Imposta il valore della propriet� datiApplicativi3.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDatiApplicativi3(String value) {
        this.datiApplicativi3 = value;
    }

    /**
     * Recupera il valore della propriet� livelloPriorita.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLivelloPriorita() {
        return livelloPriorita;
    }

    /**
     * Imposta il valore della propriet� livelloPriorita.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLivelloPriorita(String value) {
        this.livelloPriorita = value;
    }

    /**
     * Recupera il valore della propriet� note.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNote() {
        return note;
    }

    /**
     * Imposta il valore della propriet� note.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNote(String value) {
        this.note = value;
    }

    /**
     * Recupera il valore della propriet� ordinamentoStringaLabel.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getOrdinamentoStringaLabel() {
        return ordinamentoStringaLabel;
    }

    /**
     * Imposta il valore della propriet� ordinamentoStringaLabel.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setOrdinamentoStringaLabel(JAXBElement<String> value) {
        this.ordinamentoStringaLabel = value;
    }

    /**
     * Recupera il valore della propriet� ordinamentoStringaValore.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrdinamentoStringaValore() {
        return ordinamentoStringaValore;
    }

    /**
     * Imposta il valore della propriet� ordinamentoStringaValore.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrdinamentoStringaValore(String value) {
        this.ordinamentoStringaValore = value;
    }

    /**
     * Recupera il valore della propriet� ordinamentoDataLabel.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getOrdinamentoDataLabel() {
        return ordinamentoDataLabel;
    }

    /**
     * Imposta il valore della propriet� ordinamentoDataLabel.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setOrdinamentoDataLabel(JAXBElement<String> value) {
        this.ordinamentoDataLabel = value;
    }

    /**
     * Recupera il valore della propriet� ordinamentoDataValore.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getOrdinamentoDataValore() {
        return ordinamentoDataValore;
    }

    /**
     * Imposta il valore della propriet� ordinamentoDataValore.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setOrdinamentoDataValore(XMLGregorianCalendar value) {
        this.ordinamentoDataValore = value;
    }

    /**
     * Recupera il valore della propriet� ordinamentoNumeroLabel.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getOrdinamentoNumeroLabel() {
        return ordinamentoNumeroLabel;
    }

    /**
     * Imposta il valore della propriet� ordinamentoNumeroLabel.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setOrdinamentoNumeroLabel(JAXBElement<String> value) {
        this.ordinamentoNumeroLabel = value;
    }

    /**
     * Recupera il valore della propriet� ordinamentoNumeroValore.
     * 
     */
    public int getOrdinamentoNumeroValore() {
        return ordinamentoNumeroValore;
    }

    /**
     * Imposta il valore della propriet� ordinamentoNumeroValore.
     * 
     */
    public void setOrdinamentoNumeroValore(int value) {
        this.ordinamentoNumeroValore = value;
    }

    /**
     * Recupera il valore della propriet� urlAllegatiDinamici.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getUrlAllegatiDinamici() {
        return urlAllegatiDinamici;
    }

    /**
     * Imposta il valore della propriet� urlAllegatiDinamici.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setUrlAllegatiDinamici(JAXBElement<String> value) {
        this.urlAllegatiDinamici = value;
    }

    /**
     * Gets the value of the taskDettaglio property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the taskDettaglio property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTaskDettaglio().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TaskDettaglio }
     * 
     * 
     */
    public List<TaskDettaglio> getTaskDettaglio() {
        if (taskDettaglio == null) {
            taskDettaglio = new ArrayList<TaskDettaglio>();
        }
        return this.taskDettaglio;
    }

    /**
     * Gets the value of the taskAllegato property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the taskAllegato property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTaskAllegato().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TaskAllegato }
     * 
     * 
     */
    public List<TaskAllegato> getTaskAllegato() {
        if (taskAllegato == null) {
            taskAllegato = new ArrayList<TaskAllegato>();
        }
        return this.taskAllegato;
    }

    /**
     * Gets the value of the taskBottone property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the taskBottone property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTaskBottone().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TaskBottone }
     * 
     * 
     */
    public List<TaskBottone> getTaskBottone() {
        if (taskBottone == null) {
            taskBottone = new ArrayList<TaskBottone>();
        }
        return this.taskBottone;
    }

    /**
     * Gets the value of the taskUtenti property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the taskUtenti property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTaskUtenti().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TaskUtenti }
     * 
     * 
     */
    public List<TaskUtenti> getTaskUtenti() {
        if (taskUtenti == null) {
            taskUtenti = new ArrayList<TaskUtenti>();
        }
        return this.taskUtenti;
    }

}
