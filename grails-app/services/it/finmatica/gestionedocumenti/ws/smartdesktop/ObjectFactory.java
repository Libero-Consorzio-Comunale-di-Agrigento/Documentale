
package it.finmatica.gestionedocumenti.ws.smartdesktop;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the it.finmatica.integrazioni.ws.smartdesktop package. 
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

    private final static QName _IdTask_QNAME = new QName("http://webservice.jworklist.finmatica.it/", "idTask");
    private final static QName _TaskEsterno_QNAME = new QName("http://webservice.jworklist.finmatica.it/", "taskEsterno");
    private final static QName _TaskBottone_QNAME = new QName("http://webservice.jworklist.finmatica.it/", "TaskBottone");
    private final static QName _TaskDettaglio_QNAME = new QName("http://webservice.jworklist.finmatica.it/", "TaskDettaglio");
    private final static QName _EliminaTaskResponse_QNAME = new QName("http://webservice.jworklist.finmatica.it/", "eliminaTaskResponse");
    private final static QName _TaskEsternoNew_QNAME = new QName("http://webservice.jworklist.finmatica.it/", "taskEsternoNew");
    private final static QName _IdRif_QNAME = new QName("http://webservice.jworklist.finmatica.it/", "idRif");
    private final static QName _TaskEsternoMultiplo_QNAME = new QName("http://webservice.jworklist.finmatica.it/", "taskEsternoMultiplo");
    private final static QName _TipoIdentificativo_QNAME = new QName("http://webservice.jworklist.finmatica.it/", "tipoIdentificativo");
    private final static QName _TaskResponse_QNAME = new QName("http://webservice.jworklist.finmatica.it/", "TaskResponse");
    private final static QName _InserisciTaskResponse_QNAME = new QName("http://webservice.jworklist.finmatica.it/", "inserisciTaskResponse");
    private final static QName _InserisciMultiTaskResponse_QNAME = new QName("http://webservice.jworklist.finmatica.it/", "inserisciMultiTaskResponse");
    private final static QName _TaskUtenti_QNAME = new QName("http://webservice.jworklist.finmatica.it/", "TaskUtenti");
    private final static QName _Task_QNAME = new QName("http://webservice.jworklist.finmatica.it/", "Task");
    private final static QName _SostituisciTaskResponse_QNAME = new QName("http://webservice.jworklist.finmatica.it/", "sostituisciTaskResponse");
    private final static QName _TaskResponseMultipla_QNAME = new QName("http://webservice.jworklist.finmatica.it/", "TaskResponseMultipla");
    private final static QName _IdAttivitaOrRiferimento_QNAME = new QName("http://webservice.jworklist.finmatica.it/", "idAttivitaOrRiferimento");
    private final static QName _TaskAllegato_QNAME = new QName("http://webservice.jworklist.finmatica.it/", "TaskAllegato");
    private final static QName _EliminaTaskByRifResponse_QNAME = new QName("http://webservice.jworklist.finmatica.it/", "eliminaTaskByRifResponse");
    private final static QName _TaskOrdinamentoStringaLabel_QNAME = new QName("", "ordinamentoStringaLabel");
    private final static QName _TaskOrdinamentoDataLabel_QNAME = new QName("", "ordinamentoDataLabel");
    private final static QName _TaskOrdinamentoNumeroLabel_QNAME = new QName("", "ordinamentoNumeroLabel");
    private final static QName _TaskUrlAllegatiDinamici_QNAME = new QName("", "urlAllegatiDinamici");
    private final static QName _TaskDataScadenza_QNAME = new QName("", "dataScadenza");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: it.finmatica.integrazioni.ws.smartdesktop
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Task }
     * 
     */
    public Task createTask() {
        return new Task();
    }

    /**
     * Create an instance of {@link TaskBottone }
     * 
     */
    public TaskBottone createTaskBottone() {
        return new TaskBottone();
    }

    /**
     * Create an instance of {@link TaskDettaglio }
     * 
     */
    public TaskDettaglio createTaskDettaglio() {
        return new TaskDettaglio();
    }

    /**
     * Create an instance of {@link TaskResponse }
     * 
     */
    public TaskResponse createTaskResponse() {
        return new TaskResponse();
    }

    /**
     * Create an instance of {@link TaskResponseMultipla }
     * 
     */
    public TaskResponseMultipla createTaskResponseMultipla() {
        return new TaskResponseMultipla();
    }

    /**
     * Create an instance of {@link TaskUtenti }
     * 
     */
    public TaskUtenti createTaskUtenti() {
        return new TaskUtenti();
    }

    /**
     * Create an instance of {@link TaskAllegato }
     * 
     */
    public TaskAllegato createTaskAllegato() {
        return new TaskAllegato();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Long }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.jworklist.finmatica.it/", name = "idTask")
    public JAXBElement<Long> createIdTask(Long value) {
        return new JAXBElement<Long>(_IdTask_QNAME, Long.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Task }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.jworklist.finmatica.it/", name = "taskEsterno")
    public JAXBElement<Task> createTaskEsterno(Task value) {
        return new JAXBElement<Task>(_TaskEsterno_QNAME, Task.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TaskBottone }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.jworklist.finmatica.it/", name = "TaskBottone")
    public JAXBElement<TaskBottone> createTaskBottone(TaskBottone value) {
        return new JAXBElement<TaskBottone>(_TaskBottone_QNAME, TaskBottone.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TaskDettaglio }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.jworklist.finmatica.it/", name = "TaskDettaglio")
    public JAXBElement<TaskDettaglio> createTaskDettaglio(TaskDettaglio value) {
        return new JAXBElement<TaskDettaglio>(_TaskDettaglio_QNAME, TaskDettaglio.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TaskResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.jworklist.finmatica.it/", name = "eliminaTaskResponse")
    public JAXBElement<TaskResponse> createEliminaTaskResponse(TaskResponse value) {
        return new JAXBElement<TaskResponse>(_EliminaTaskResponse_QNAME, TaskResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Task }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.jworklist.finmatica.it/", name = "taskEsternoNew")
    public JAXBElement<Task> createTaskEsternoNew(Task value) {
        return new JAXBElement<Task>(_TaskEsternoNew_QNAME, Task.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.jworklist.finmatica.it/", name = "idRif")
    public JAXBElement<String> createIdRif(String value) {
        return new JAXBElement<String>(_IdRif_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Task }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.jworklist.finmatica.it/", name = "taskEsternoMultiplo")
    public JAXBElement<Task> createTaskEsternoMultiplo(Task value) {
        return new JAXBElement<Task>(_TaskEsternoMultiplo_QNAME, Task.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.jworklist.finmatica.it/", name = "tipoIdentificativo")
    public JAXBElement<String> createTipoIdentificativo(String value) {
        return new JAXBElement<String>(_TipoIdentificativo_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TaskResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.jworklist.finmatica.it/", name = "TaskResponse")
    public JAXBElement<TaskResponse> createTaskResponse(TaskResponse value) {
        return new JAXBElement<TaskResponse>(_TaskResponse_QNAME, TaskResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TaskResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.jworklist.finmatica.it/", name = "inserisciTaskResponse")
    public JAXBElement<TaskResponse> createInserisciTaskResponse(TaskResponse value) {
        return new JAXBElement<TaskResponse>(_InserisciTaskResponse_QNAME, TaskResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TaskResponseMultipla }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.jworklist.finmatica.it/", name = "inserisciMultiTaskResponse")
    public JAXBElement<TaskResponseMultipla> createInserisciMultiTaskResponse(TaskResponseMultipla value) {
        return new JAXBElement<TaskResponseMultipla>(_InserisciMultiTaskResponse_QNAME, TaskResponseMultipla.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TaskUtenti }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.jworklist.finmatica.it/", name = "TaskUtenti")
    public JAXBElement<TaskUtenti> createTaskUtenti(TaskUtenti value) {
        return new JAXBElement<TaskUtenti>(_TaskUtenti_QNAME, TaskUtenti.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Task }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.jworklist.finmatica.it/", name = "Task")
    public JAXBElement<Task> createTask(Task value) {
        return new JAXBElement<Task>(_Task_QNAME, Task.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TaskResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.jworklist.finmatica.it/", name = "sostituisciTaskResponse")
    public JAXBElement<TaskResponse> createSostituisciTaskResponse(TaskResponse value) {
        return new JAXBElement<TaskResponse>(_SostituisciTaskResponse_QNAME, TaskResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TaskResponseMultipla }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.jworklist.finmatica.it/", name = "TaskResponseMultipla")
    public JAXBElement<TaskResponseMultipla> createTaskResponseMultipla(TaskResponseMultipla value) {
        return new JAXBElement<TaskResponseMultipla>(_TaskResponseMultipla_QNAME, TaskResponseMultipla.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.jworklist.finmatica.it/", name = "idAttivitaOrRiferimento")
    public JAXBElement<String> createIdAttivitaOrRiferimento(String value) {
        return new JAXBElement<String>(_IdAttivitaOrRiferimento_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TaskAllegato }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.jworklist.finmatica.it/", name = "TaskAllegato")
    public JAXBElement<TaskAllegato> createTaskAllegato(TaskAllegato value) {
        return new JAXBElement<TaskAllegato>(_TaskAllegato_QNAME, TaskAllegato.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TaskResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.jworklist.finmatica.it/", name = "eliminaTaskByRifResponse")
    public JAXBElement<TaskResponse> createEliminaTaskByRifResponse(TaskResponse value) {
        return new JAXBElement<TaskResponse>(_EliminaTaskByRifResponse_QNAME, TaskResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "ordinamentoStringaLabel", scope = Task.class)
    public JAXBElement<String> createTaskOrdinamentoStringaLabel(String value) {
        return new JAXBElement<String>(_TaskOrdinamentoStringaLabel_QNAME, String.class, Task.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "ordinamentoDataLabel", scope = Task.class)
    public JAXBElement<String> createTaskOrdinamentoDataLabel(String value) {
        return new JAXBElement<String>(_TaskOrdinamentoDataLabel_QNAME, String.class, Task.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "ordinamentoNumeroLabel", scope = Task.class)
    public JAXBElement<String> createTaskOrdinamentoNumeroLabel(String value) {
        return new JAXBElement<String>(_TaskOrdinamentoNumeroLabel_QNAME, String.class, Task.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "urlAllegatiDinamici", scope = Task.class)
    public JAXBElement<String> createTaskUrlAllegatiDinamici(String value) {
        return new JAXBElement<String>(_TaskUrlAllegatiDinamici_QNAME, String.class, Task.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "dataScadenza", scope = Task.class)
    public JAXBElement<XMLGregorianCalendar> createTaskDataScadenza(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_TaskDataScadenza_QNAME, XMLGregorianCalendar.class, Task.class, value);
    }

}
