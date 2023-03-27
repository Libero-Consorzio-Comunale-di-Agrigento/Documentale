package it.finmatica.dmServer.management;

import java.io.*;
import java.sql.ResultSet;
import org.w3c.dom.*;
import java.util.Vector;
import java.util.HashMap;
import javax.xml.parsers.*;
import java.sql.Connection;
import it.finmatica.dmServer.*;
import org.xml.sax.InputSource;
import it.finmatica.jfc.dbUtil.*;
import it.finmatica.jfc.utility.DateUtility;
import it.finmatica.dmServer.util.*; 
import it.finmatica.dmServer.competenze.*; 
import it.finmatica.dmServer.motoreRicerca.*;
import it.finmatica.dmServer.jdms.*;
import it.finmatica.log4jsuite.LogDb;
import javax.servlet.http.*;
import java.lang.reflect.Constructor;

/** 
 * Gestione di una Query del documentale.
 * <BR>
 * Esempio di <B>INSERIMENTO:</B><BR>
 * <BR> 		
 * 		   // Creazione della Query passando<BR>
 * 		   // Area, Codice Modello, Radice (nell'esempio sistema),<BR>
 * 		   // nome della query<BR>
 * <BR>
 * 		   IQuery Iq = new IQuery("AD4","MODELLOQUERY_AD4",
                                  Global.ROOT_SYSTEM_FOLDER,
                                  "Prova Inserimento Query");<BR>
 * <BR>
 * 		   // Inizializzazione delle variabili<BR>
 * 		   // Viene passato User Ad4, Password AD4, Connection<BR>
 * 		   // in alternativa alla connection è possibile passare<BR>
 * 		   // il percorso del file di properties <BR>
 * 		   // es: "S:\\SI4\\GD4\\jGD4\\lib\\gd4dm.properties"<BR>
 * <BR>
 * 		   Iq.initVarEnv("GDM","GDM", conn);<BR>
 * <BR>
 * 		   Iq.addValue("ATTRIBUTO_QUERY_1","Valore di attributo 1");<BR>
 * <BR>
 * 		   //Impostazioni filtro di ricerca<BR>
 *         Iq.settArea("MIA_AREA");<BR>
 *         Iq.addCodiceModello("MIO_CM");<BR>
 * <BR>
 *         Iq.addCampo("CAMPO1","VALORE1");<BR>
 *         Iq.addCampo("CAMPO2","VALORE2",">");<BR>
 * <BR>
 * 		   try {<BR>
 * 		   &nbsp;&nbsp;Iq.insert();<BR>
 * 		   }<BR>
 * 		   catch (Exception e) {<BR>
 * 		   &nbsp;&nbsp;//GESTIONE ERRORE.....<BR>
 * 		   }
 * <BR>            
 * Esempio di <B>MODIFICA:</B><BR>              
 * <BR>		   
 * 		   // Accesso alla query con identificativo 678<BR>
 * 		   // In alternativa si potrebbe utilizzare il costruttore<BR>
 * 		   // con tre parametri (area,codMod,codRich)<BR>
 * 		   // Il codice richiesta di un profilo-Query<BR>
 * 		   // corrisponderà sempre all'indentificativo della query<BR>
 * 		   // preceduto dal simbolo "-"  	
 * <BR>
 * 		   IQuery Iq = new IQuery("678");<BR>
 * <BR>
 * 		   // Inizializzazione delle variabili<BR>
 * 		   // Viene passato User AD4, Password AD4, Connection<BR>
 * 		   // in alternativa alla connection è possibile passare<BR>
 * 		   // il percorso del file di properties <BR>
 * 		   // es: "S:\\SI4\\GD4\\jGD4\\lib\\gd4dm.properties"<BR>
 * <BR>
 * 		   Iq.initVarEnv("GDM","GDM", conn);<BR>
 * <BR>
 * 		   Iq.addValue("ATTRIBUTO_QUERY_1","Modifica Valore di attributo 1");<BR>
 * <BR>
 *  	   //Impostazioni filtro di ricerca<BR>
 *         Iq.settArea("MIA_AREA");<BR>
 *         Iq.addCodiceModello("MIO_CM");<BR>
 * <BR>
 *         Iq.addCampo("CAMPO1","VALORE1");<BR>
 *         Iq.addCampo("CAMPO2","VALORE2",">");<BR>
 * <BR>
 * 		   try {<BR>
 * 		   &nbsp;&nbsp;Iq.update();<BR>
 * 		   }<BR>
 * 		   catch (Exception e) {<BR>
 * 		   &nbsp;&nbsp;//GESTIONE ERRORE.....<BR>
 * 		   }
 * <BR>
 * Esempio di <B>RICERCA:</B><BR>              
 * <BR>		   
 * 		   // Costruttore che imposta la query in modalità ricerca<BR>
 * 		   // Eventualmente è possibile richiamare il costruttore<BR>
 * 		   // con due parametri (codice modello,area)<BR>
 * 		   // che preimposterà la ricerca su quell'area/codice modello<BR>
 * 		   // ( avrebbe lo stesso effetto del richiamo successivo di<BR> 
 * 		   //  Iq.settaArea("MIA_AREA"); Iq.addCodiceModello("MIO_MODELLO"); )
 * <BR>
 * 		   IQuery Iq = new IQuery();  <BR>
 * <BR>
 * 		   // Inizializzazione delle variabili<BR>
 * 		   // Viene passato User AD4, Password AD4, Connection<BR>
 * 		   // in alternativa alla connection è possibile passare<BR>
 * 		   // il percorso del file di properties <BR>
 * 		   // es: "S:\\SI4\\GD4\\jGD4\\lib\\gd4dm.properties"<BR>
 * <BR>
 * 		   Iq.initVarEnv("GDM","GDM", conn);<BR>
 * <BR>
 * 		   // Terminata la query, per ogni profilo trovato<BR>
 * 		   // NON effettuerò l'accesso<BR>
 * 		   Iq.setAccessProfile(false);<BR>
 * 		   // Imposto il timeout a 30 sec.<BR>
 *         Iq.setQueryTimeOut(30000);<BR>
 *         <BR>
 *         // Scelgo di ricercare per area AD4<BR>
 *         Iq.settaArea("AD4");<BR>
 *         <BR>
 *         // Restringo la ricerca ai modelli<BR>
 *         // sottoelencati<BR>
 *         Iq.addCodiceModello("MODELLOAD4");<BR>
 *         Iq.addCodiceModello("MODELLOAD4_2");<BR>
 *         // Aggiungo un ulteriore filtro sull'area<BR>
 *         // GC4 per il modello MODELLOGC4_2<BR>
 *         Iq.addCodiceModello("GC4","MODELLOGC4_2");<BR>
 *         <BR>
 *         // Ricerca puntuale su campo stringa senza intermedia<BR>
 *         Iq.addCampo("SOGGETTO","MANNELLA");<BR>
 *         Iq.addCampo("DATA_NASCITA","is null");<BR>
 *         // Ricerca su campo clob con Intermedia<BR>
 *         Iq.addCampoFT("FASCICOLO","44/56");<BR>
 *         // Ricerca su campo numerico con operatore<BR>
 *         Iq.addCampo("N_SOGGETTO","10",">");<BR>
 *         <BR>
 *         // Ricerca su tutti i possibili valori (senza<BR>
 *         // limite di campo) delle stringhe SAMOGGIA e 89BIS.<BR>
 *         // la ricerca avverrà su campo clob con Intermedia<BR>
 *         Iq.settaCondizioneAnd("SAMOGGIA 89BIS");<BR>
 *         <BR>
 *         // Effettuo la ricercaFT(), quindi sulla addCampoFT viene<BR>
 *         // attivata la ricerca intermedia sul campo "FASCICOLO".<BR>
 *         // In alternativa, richiamando la ricerca(), sul campo "FASCIOLO",<BR>
 *         // la addCampoFT si comporterà come una semplice addCampo (ricerca<BR>
 *         // puntuale su campo stringa senza intermedia)<BR> 
 *         if (Iq.ricercaFT().booleanValue()) {<BR>
 *         	   &nbsp;&nbsp; System.out.println("Ho trovato N° Documenti: "+Iq.getProfileNumber());<BR>
 *         }<BR>
 *         else {<BR>
 *         		&nbsp;&nbsp;if (Iq.isQueryTimeOut())<BR>
 *               &nbsp;&nbsp;&nbsp;&nbsp;System.out.println("Sono andato in timeout");<BR>
 *               &nbsp;&nbsp;else<BR>
 *               &nbsp;&nbsp;&nbsp;&nbsp;System.out.println("Non ho trovato nulla");<BR>
 *         }<BR> 
 * <B>Cenni sulle connessioni (gestione dei commit e dei rollback):</B><BR>
 * E' possibile inizializzare la cartella con il meotodo initVarEnv passando la connection
 * oppure il percorso del file di properties.<BR><BR>
 * Se siamo in presenza del file di properties, la connessione al database viene gestita 
 * internamente in maniera atomica: viene creata una sessione al richiamo dei metodi insert()/update()/delete()
 * che si premura di effettuare un commit o un rollback "interno" a seconda se le operazioni 
 * di registrazione della cartella sono andate rispettivamente a buon fine oppure sono fallite.<BR><BR> 
 * Se viene passata una connection, la gestione è tutta a carico dell'utente: sarà quest'ultimo
 * che dovrà preoccuparsi o meno di effettuare i commit o i rollback.
 * 
 * @author  D. Scandurra, G. Mannella
 * @version 2.8 
 *             
*/

public class IQuery 
{
   
   /**
    * Elenco dei profili restituiti dalla query
   */      
   private Vector profili;
  
   /**
    * Variabile di ambiente
   */  
   private Environment en;
  
   /**
    * Identificativo della Query 
   */
   private String identifierQuery;
  
   /**
    * Identificativo del profilo Query 
   */ 
   private String profileQuery;
    
   /**
    * Nome della query 
   */
   private String nameQuery;

   /**
    * Gestione dell'errore
   */    
   private String error;     
   
  /**
   * Codice Modello   di profilo-query
   * Area             di profilo-query
   * Codice Richiesta di profilo-query
  */   
   private String area;   
   private String crQuery;
   private String cmQuery;
   
   /**
    * Radice:   U = Utente,   S = Sistema
   */
   private String rootQuery;
   
   /**
    * ATTRIBUTI DI ACL 
   */
   private Vector ACLuser;
   private Vector ACLtype;
   

   /**
    * Filtro default
   */ 
   private String filtro = Global.FILTRO_STANDARD; 
   private boolean changefiltro = false;
   private boolean bModifyFilterInUpdate = true;
   
   /**
    * ATTRIBUTI DI RICERCA 
   */
   private Vector usersFilters;
   private Vector groupFilters;
   
   private String areaRicerca;
   private Vector cmRicerca;
   private Vector cmAreaRicerca;
   private Vector campiRicerca;  
   private Vector ogfiRicerca;
   private int joinCounter=1;
   private Vector campiOrdinamento;
   private String cAnd, cOr, cNot, cSingle;
   private String crSingleDocSearch = null;
   private boolean bIsRicercaPuntuale=true;
   private boolean bControllaPadre=false;
   private String extraConditionSearch="";
   
   private boolean bUseCaseNoSensitive=false;
   
   private String controlloCompetenzaQuery=Global.ABIL_LETT;
   
   private Vector vCampiReturnCursor;

   private String tipoRicercaDefault = null;
   
   /**
    * Se siamo in presenza di ricerca con più modelli o categorie divise
    * su più campi. Questi parametri mi aiutano a restituire gli idDocumento
    * del tipo che l'utente preferisce. 
   */
   private String areaIdRicercaReturn=null, cmIdRicercaReturn=null;
   private String categoriaIdRicercaReturn=null;
   
   /**
    * Record dal quale partire a fare la fetch
    * dei risultati sul vettore restituito dalla ricerca
   */
   private int fetchInit=0;

   /**
    * Dimensione di fetch dei risultati sul vettore
    * restituito dalla ricerca (-1=TUTTI)
   */
   private int fetchSize=-1;

   /**
	* True  - Resultset di ricerca esaurito
	* False - Esiste ancora almeno un record in ricerca 
	*         successivo all'ultimo elemento estratto
   */
   private boolean bIsLastRow = false;   
   
   private boolean bEscludiControlloCompetenze=false;
   private boolean bEscludiOrdinamento=false;

   private int queryTimeOut=60000;
   private int queryServiceLimit=0;
   private int stateExcecuteQuery;
   private boolean bAlive=false;         
   private boolean bQueryTimeOut=false;
   private boolean bIsMaster=false;
   private boolean abilitaQueryServiceLimit=false;
   private XMLFilter xmlf;
   
   public final static int STATEEX_QUERY_NORESULT = 1;
   public final static int STATEEX_QUERY_TIMEOUT  = 2;
 
   private boolean accessProfileAfterSearch=true;
   private boolean instanceProfileAfterSearch=true;
   
   /**
    * Serve per settare gli idDoc fissi per la ricerca (richiamato dal monoRecord di marco)
   */ 
   private Vector idDocToRicerca= new Vector();
   
   /**
    * Serve per settare direttamente l'sql della ricerca
   */    
   private String sqlSelect = null;
   
   /**
    * Se settata a false - i valori nulli del resultset sono restituiti come "null" (Stringa)
    * Se settata a true  - i valori nulli del resultset sono restituiti come null (Puntatore a null)
   */
   private boolean bResulsetIQuery_NullValueNullVariable=false;
   
   private ResultSetIQuery rst = null;
   
   /**
    * Parametro da usare per cambiare il tipo restituito 
    * dalla ricerca nel vettore (default: vettore di Profili) 
   */
   private String classnameExportRicerca="it.finmatica.dmServer.management.Profilo";

   /**
    * Variabile per la gestione dei log su DB
   */   
   private LogDb log4jSuiteDb = null;
   
   /**
    * Variabile per la gestione del recupero dei documenti anche in stato PREBOZZA
   */     
   private boolean bAnchePreBozza = false;
   
   private AbstractSearch ric=null;
   
   /**
    * Costruttore da utilizzare in modalità RICERCA</B>
   */
   public IQuery() {
	      
   }

   /**
    * Costruttore da utilizzare in modalità RICERCA</B>
    * 
    * @param cmProfileQuery codice modello sul quale preimpostare la ricerca
    * @param areaProfileQuery area sulla quale preimpostare la ricerca
   */
   public IQuery(String cmProfileQuery, String areaProfileQuery) throws Exception {
          this.area              = areaProfileQuery;
          this.cmQuery           = cmProfileQuery;         
   }
  
   /**
    * Costruttore da utilizzare esclusivamente in fase di
    * ricerca/modifica/cancellazione di una query conoscendone
    * la chiave primaria
    * 
    * @param idQuery identificativo della query
   */
   public IQuery(String idQuery) throws Exception {
          this.identifierQuery  =  idQuery;
   }

   /**
    * Costruttore da utilizzare esclusivamente in fase di
    * ricerca/modifica/cancellazione di una query conoscendone
    * la chiave primaria
    * 
    * @param areaProfileQuery area del profilo-query
    * @param cmProfileQuery codice modello del profilo-query
    * @param crProfileQuery codice richiesta del profilo-query
   */
   public IQuery(String areaProfileQuery, String cmProfileQuery, String crProfileQuery) throws Exception {         
          this.cmQuery           = cmProfileQuery;
          this.crQuery           = crProfileQuery;
          this.area              = areaProfileQuery;
   }

   /**
    * Costruttore da utilizzare esclusivamente in creazione
    * di una query passando area, codiceModello,
    * radice, nome
    * 
    * @param areaProfileQuery area del profilio-query
    * @param cmProfileQuery codice modello del profilio-query
    * @param rootFolder radice dell'albero. Possibili valori:<BR>
    * 		 Global.ROOT_SYSTEM_FOLDER<BR>
    * 		 Global.ROOT_USER_FOLDER
    * @param nameQuery nome della query
   */
   public IQuery(String areaProfileQuery, String cmProfileQuery,
                 String rootFolder, String nameQuery) throws Exception {
          this.area              = areaProfileQuery;
          this.cmQuery           = cmProfileQuery;
          this.rootQuery         = rootFolder;
          this.nameQuery         = nameQuery;         
   }

   /**
    * @param environment environment da cui leggere le
    *     			     variabili d'ambiente
   */
   public void initVarEnv(Environment environment) throws Exception  {
          try {           
            inizializza(environment);
          }
          catch (Exception e) {
            throw new Exception("IQuery::initVarEnv()\n"+e.getMessage());             
          }
   }   
  
   /**
    * @param user utente di AD4
    * @param passwd password di AD4
    * @param appl applicativo chiamante
    * @param ente ente chiamante
    * @param ini percorso del file di properties
   */ 
   public void initVarEnv(String user,String passwd,String appl,String ente, String ini) {
          try {
            Environment env = new Environment(user,passwd,appl, ente, "", ini );        
            inizializza(env);            
          }
          catch (Exception e) {
            error = "IQuery::initVarEnv()\n"+e.getMessage();             
          }              
   }
   
   /**
    * @param user utente di AD4
    * @param passwd password di AD4
    * @param appl applicativo chiamante
    * @param ente ente chiamante
    * @param cn connection
   */ 
   public void initVarEnv(String user,String passwd,String appl,String ente, Connection cn) {
          try {
            Environment env = new Environment(user,passwd,appl, ente, "", cn );        
            inizializza(env);            
          }
          catch (Exception e) {
            error = "IQuery::initVarEnv()\n"+e.getMessage();             
          }            
   }
   
   /**
    * Metodo da richiamare subito dopo il costruttore<BR>
    * user e password sono individuati da "AD4"<BR>
    * ini rappresenta il percorso del file di properties<BR>
    * in cui sono specificati i parametri di connessione<BR>
    * 
    * @param user utente di AD4
    * @param passwd password di AD4
    * @param ini percorso del file di properties
   */   
   public void initVarEnv(String user,String passwd, String ini) {
          initVarEnv( user, passwd, Global.APPL_STANDARD, Global.ENTE_STANDARD, ini);         
   }
   
   /**
    * Metodo da richiamare subito dopo il costruttore<BR>
    * user e password sono individuati da "AD4"<BR>
    * cn rappresenta la connection
    * 
    * @param user utente di AD4
    * @param passwd password di AD4
    * @param cn connection
   */
   public void initVarEnv(String user,String passwd, Connection cn) {
          initVarEnv( user, passwd, Global.APPL_STANDARD, Global.ENTE_STANDARD, cn);         
   }

   /**
    * Metodo che serve a settare il tipo restituito 
    * dalla ricerca nel vettore (default: vettore di Profili) 
    * 
    * @param newClassnameExportRicerca nome della classe da restituire nel vettore
    *                                  restituito dalla ricerca
    * @see <a href="IQuery.html#setClassReturnType(String)">setClassReturnType</a> 
   */   
   public void setClassReturnType(String newClassnameExportRicerca) {
          classnameExportRicerca = newClassnameExportRicerca;
   } 
   
   /**
    * Metodo che serve a settare l'identificativo
    * del profilo-query
    * 
    * @param idProfile identificativo del profilo-query
    * @see <a href="IQuery.html#getProfileQuery()">getProfileQuery</a> 
   */   
   public void setProfileQuery(String idProfile) {
         profileQuery = idProfile;
   }   
   
   /**
    * Metodo che serve a restituire l'identificativo
    * del profilo-query
    * 
    * @return identificativo del profilo-query
    * @see <a href="IQuery.html#setProfileQuery(java.lang.String)">setProfileQuery</a> 
   */      
   public String getProfileQuery() {
          return profileQuery;
   }
   
   public Vector getCampiOrdinamentoReturn() {
          return campiOrdinamento;
   }   
   
   public void setCampiOrdinamentoReturn(Vector vCampi) {
          campiOrdinamento=vCampi;
   }    
   
   public Vector getCampiRicerca() {
          return campiRicerca;
   }   
   
   public void setCampiRicerca(Vector vCampi) {
          campiRicerca=vCampi;
   }

    public int getQueryServiceLimit() {
        return queryServiceLimit;
    }

    public boolean isAbilitaQueryServiceLimit() {
        return abilitaQueryServiceLimit;
    }

    public void setAbilitaQueryServiceLimit(boolean abilitaQueryServiceLimit) {
        this.abilitaQueryServiceLimit = abilitaQueryServiceLimit;
    }

    /**
    * Metodo che serve a settare l'identificativo
    * della query
    * 
    * @param idQuery identificativo della query
    * @see <a href="IQuery.html#getIdentifierQuery()">getIdentifierQuery</a> 
   */
   public void setIdentifierQuery(String idQuery) {
          identifierQuery = idQuery;
   }
   
   /**
    * Metodo che serve a restituire l'identificativo
    * della query
    * 
    * @return identificativo della query
    * @see <a href="IQuery.html#setIdentifierQuery(java.lang.String)">setIdentifierQuery</a> 
   */   
   public String getIdentifierQuery() {
          return this.identifierQuery;
   }   
   
   /**
    * Metodo che serve a settare il filtro
    * della query
    * 
    * @param newFiltro Filtro XML della query
    * @see <a href="IQuery.html#getFiltro()">getFiltro</a> 
   */   
   public void setFiltro(String newFiltro) {
          changefiltro=true;
          filtro = newFiltro;
   }

   /**
    * Metodo che serve a restituire il filtro
    * della query
    * 
    * @return Filtro XML della query
    * @see <a href="IQuery.html#setFiltro(java.lang.String)">setFiltro</a> 
   */   
   public String getFiltro() {
          return filtro;
   }
   
   /**
    * Metodo che serve a settare l'aggiornamento del filtro
    * della query in fase di update
    * 
    * @param bFlag True:  in update il filtro verrà aggiornato
    * 			   False: in update il filtro non verrà aggiornato
   */    
   public void setUpdateFilter(boolean bFlag) {
	      bModifyFilterInUpdate=bFlag;
   }

   /**
    * Metodo che serve a settare il codice richesta
    * 
    * @param newCodRich Codice richiesta della query
    * @see <a href="IQuery.html#getCodiceRichiesta()">getCodiceRichiesta</a> 
   */
   public void settaCodiceRichiesta(String newCodRich) {
          crQuery = newCodRich;
   }
   
   /**
    * Metodo che serve a restituire il codice richesta
    * 
    * @return Codice richiesta della query
    * @see <a href="IQuery.html#settaCodiceRichiesta(java.lang.String)">settaCodiceRichiesta</a> 
   */
   public String getCodiceRichiesta() {
          return crQuery;
   }

   /**
    * Metodo che serve a settare l'area DI RICERCA della query
    * 
    * @param newArea area della query
    * @see <a href="IQuery.html#getArea()">getArea</a> 
   */   
   public void settaArea(String newArea) {
          this.areaRicerca = newArea;
   }

   /**
    * Metodo che serve a restituire l'area della query
    * 
    * @return area della query
    * @see <a href="IQuery.html#settaArea(java.lang.String)">settaArea</a> 
   */   
   public String getArea() {
          return this.area;
   }   

   /**
    * Metodo che serve a settare il codice modello della query
    * 
    * @param newTipoDocForm codice modello della query
    * @see <a href="IQuery.html#getTipoDocForm()">getTipoDocForm</a> 
   */   
   public void setTipoDocForm(String newTipoDocForm) {
          cmQuery = newTipoDocForm;
   }
   
   /**
    * Metodo che serve a restituire il codice modello della query
    * 
    * @return codice modello della query
    * @see <a href="IQuery.html#setTipoDocForm(java.lang.String)">setTipoDocForm</a> 
   */   
   public String getTipoDocForm() {
          return cmQuery;
   }   
   
   /**
    * Metodo che serve a determinare se è necessario 
    * effettuare l'accesso ai profili restituiti in seguito
    * ad una ricerca
    * 
    * @param bAccess Se true accede ai profili, se false non accede ai profili 
   */      
   public void setAccessProfile(boolean bAccess) {
         accessProfileAfterSearch=bAccess;
   }

   /**
    * Metodo che serve a determinare se è necessario 
    * effettuare la creazione dei Profili restituiti 
    * dalla ricerca
    * 
    * @param bInstance Se true istanzia profili, se false istanzia i profili ma
    *                  ne crea comunque tante variabili con puntatore nullo
   */
   public void setInstanceProfile(boolean bInstance) {
          instanceProfileAfterSearch=bInstance;
   }
   
   /**
    * Metodo che aggiunge alla lista delle ACL della cartella
    * la coppia (Utente,TipoACL).
    * Esempio:<BR>
    * <BR>
    * Iq.settaACL("AA4",Global.NORMAL_ACCESS);<BR>
    * <BR>
    * Verranno inserite le competenze di "NORMAL_ACCESS"
    * per l'utente AA4
    *
    * @param user utente a cui assegnare le ACL
    * @param type tipo di ACL. Tipi possibili:<BR>
    * 		 	  Global.NO_ACCESS -> Nega competenze di Lettura, Modifica, Cancellazione, Management<BR>
    *			  Global.COMPLETE_ACCESS -> Assegna competenze di Lettura, Modifica, Cancellazione, Management<BR>
    *			  Global.NORMAL_ACCESS -> Assegna competenze di Lettura, Modifica. Nega competenze di Cancellazione<BR>
    *			  Global.READONLY_ACCESS -> Assegna competenze di Lettura. Nega competenze di Modifica, Cancellazione<BR>
    *		      Global.INFOLDER_ACCESS -> Assegna competenze di inserimento oggetti in cartella
   */ 
   public void settaACL(String user, String type) {
          ACLuser.addElement(user);
          ACLtype.addElement(type);
   }
   
   public void settaIdDocumentoRicerca(String id) {
          idDocToRicerca.add(id);
   }  	   
   
   public void settaIdDocumentoRicerca(Vector idV) {
          idDocToRicerca=idV;
   }  	

   /**
    * Metodo che deve essere richiamato successivamente ad una ricerca,
    * se questa ha restituito false.
    * Restituisce:<BR>
    * True - la ricerca ha restituito false perché è andata in timeout<BR>
    * False - la ricerca ha restituito false ma non è andata in timeout.<BR>
    * Esempio:<BR>
    *         if (Iq.ricercaFT().booleanValue()) {<BR>
    *         	   &nbsp;&nbsp; System.out.println("Ho trovato N° Documenti: "+Iq.getProfileNumber());<BR>
    *         }<BR>
    *         else {<BR>
    *         		&nbsp;&nbsp;if (Iq.isQueryTimeOut())<BR>
    *               &nbsp;&nbsp;&nbsp;&nbsp;System.out.println("Sono andato in timeout");<BR>
    *               &nbsp;&nbsp;else<BR>
    *               &nbsp;&nbsp;&nbsp;&nbsp;System.out.println("Non ho trovato nulla");<BR>
    *         }<BR> 
    * @return True - la ricerca ha restituito false perché è andata in timeout<BR>
    * 		  False - la ricerca ha restituito false ma non è andata in timeout
   */  
   public boolean isQueryTimeOut() {
	      if (stateExcecuteQuery==STATEEX_QUERY_TIMEOUT) return true;
	     
	      return false;
   }

   /**
    * Gestione dell'errore
   */   
   public String getError() {
          return error;
   }
  
   /**
    * Metodo che fornisce il vettore di profili
    * restituito a partire da una ricerca.
    * Ogni oggetto del vettore è un Profilo.
    * 
    * @return Vettore di profili 
    * @see <a href="IQuery.html#getProfileNumber()">getProfileNumber</a>
   */   
   public Vector getProfili() {
          if (profili.get(0)==null)
             error="La ricerca non ha prodotto nessun risultato";    
         
          return profili;
   }
   
   /**
    * Metodo che fornisce il numero
    * di profili restituiti in seguito ad
    * una ricerca. Ottimo metodo per "ciclare"
    * sul vettore di profili restituito
    * dalla <a href="IQuery.html#getProfili()">getProfili</a>
    * 
    * @return Numero di profili
    * @see <a href="IQuery.html#getProfili()">getProfili</a>
   */      
   public int getProfileNumber() {
          return profili.size();
   }
      
   /**
    * Metodo che restituisce un Profilo
    * a partire dal vettore di profili
    * costruiti in seguito alla ricerca.
    * Il profilo viene scelto in funzione
    * dell'indice del vettore passato in input.
    * Il metodo va richiamato necessariamente dopo
    * aver lanciato la ricerca
    * 
    * @param index indice del vettore
    * @return Profilo con indice index
    * @see <a href="IQuery.html#getProfileFromDocNum(java.lang.String)">getProfileFromDocNum</a>
   */   
   public Profilo getProfileFromIndex(int index) throws Exception {
          try {
            return (Profilo)profili.get(index);
          }
          catch (Exception e) {
            error="Impossibile tornare il profilo n° "+index;
            throw new Exception(error);            
          }
   }

   /**
    * Metodo che restituisce un Profilo
    * a partire dal vettore di profili
    * costruiti in seguito alla ricerca.
    * Il profilo viene scelto in funzione
    * del suo identificativo.
    * Il metodo va richiamato necessariamente dopo
    * aver lanciato la ricerca
    * 
    * @param sDocNumber indentificativo del profilo
    * @return Profilo con identificativo sDocNumber
    * @see <a href="IQuery.html#getProfileFromIndex(int)">getProfileFromIndex</a>
   */   
   public Profilo getProfileFromDocNum(String sDocNumber) throws Exception {
          try {
            Vector v = profili;

            for(int i=0;i<v.size();i++) 
            {
                Profilo p = (Profilo)v.get(i);
                if (en.Global.DM.equals(en.Global.FINMATICA_DM)) {
                    if (p.getDocNumber().equals(sDocNumber)) return p;                
                }
                else {
                    if (p.getCampo("DOCNUM").equals(sDocNumber)) return p;
                }
            }

            error="Impossibile trovare il profilo con DocNum="+sDocNumber;
            throw new Exception(error);
          }
          catch (Exception e)
          {
            error="Impossibile tornare il profilo con DocNum="+sDocNumber;
            throw new Exception(error);
          }
   }

   /**
    * Metodo che restituisce la lista dei documenti
    * <B>a</B> cui un profilo è riferito. Il Profilo
    * è restituito a partire dal vettore di profili
    * costruiti in seguito alla ricerca.
    * Il profilo viene scelto in funzione
    * dell'indice del vettore passato in input.
    * Il metodo va richiamato necessariamente dopo
    * aver lanciato la ricerca.<BR>
    * Questo metodo va richiamato solo nel caso in
    * cui <a href="IQuery.html#setAccessProfile(boolean)">setAccessProfile</a>
    * viene richiamato passando true prima di effettuare la ricerca.
    * 
    * @param index indice del vettore
    * @return Stringa dei riferimenti nel seguente formato:<BR>
    * 		  idDocRif<B>1</B>,TipoRif<B>1</B>@idDocRif<B>2</B>,
    * 		  TipoRif<B>2</B>@.......@idDocRif<B>n</B>,TipoRif<B>n</B>
    * @see <a href="IQuery.html#getRiferimentiFromDocNum(java.lang.String)">getRiferimentiFromDocNum</a>
   */   
   public String getRiferimentiFromIndex(int index) {
          try {
            return getProfileFromIndex(index).getRiferimenti();
          }
          catch (Exception e) {
            error="Impossibile tornare i riferimenti del profilo n° "+index;
            return null;
          }
   }

   /**
    * Metodo che restituisce la lista dei documenti
    * <B>da</B> cui un profilo è riferito. Il Profilo
    * è restituito a partire dal vettore di profili
    * costruiti in seguito alla ricerca.
    * Il profilo viene scelto in funzione
    * dell'indetificativo passato in input.
    * Il metodo va richiamato necessariamente dopo
    * aver lanciato la ricerca.<BR>
    * Questo metodo va richiamato solo nel caso in
    * cui <a href="IQuery.html#setAccessProfile(boolean)">setAccessProfile</a>
    * viene richiamato passando true prima di effettuare la ricerca.
    * 
    * @param sDocNumber identificativo del profilo
    * @return Stringa dei riferimenti nel seguente formato:<BR>
    * 		  idDocRif<B>1</B>,TipoRif<B>1</B>@idDocRif<B>2</B>,
    * 		  TipoRif<B>2</B>@.......@idDocRif<B>n</B>,TipoRif<B>n</B>
    * @see <a href="IQuery.html#getRiferimentiFromIndex(int)">getRiferimentiFromIndex</a>
   */   
   public String getRiferimentiFromDocNum(String sDocNumber) {
          try {
            return getProfileFromDocNum(sDocNumber).getRiferimenti();
          }
          catch (Exception e) {
            error="Impossibile tornare i riferimenti del profilo con DocNum="+sDocNumber;
            return null;
          }
   }   

   /**
    * Metodo che a partire dal profilo, con indice index,
    * restituito dalla ricerca, esegue il metodo
    * <a href="IQuery.html#getFile(int)">getFile(index)</a>
    * della classe Profilo oppure 
    * <a href="IQuery.html#getFile(java.lang.String)">getFile(String)</a>
    * nel caso in cui venga passato come secondo parametro rispettivamente
    * l'indice o il nome del file.
    * 
    * @param index indice del profilo rispetto al vettore di Profili
    * @param indexOrName indice nel vettore di file del profilo oppure nome del file
    * @return percorso del download del file (es: c:\windows\temp\allegato.doc)
    * @see <a href="IQuery.html#getFileFromDocNum(java.lang.String, java.lang.String)">getFileFromDocNum()</a>  
   */   
   public String getFileFromIndex(int index,String indexOrName) {
          try {          
            Profilo pApp;
            pApp= getProfileFromIndex(index);
            if (pApp==null) return null;

            pApp.accedi("1");

            try {
              if (indexOrName.indexOf(".")==-1)
                  return pApp.getFile(Integer.parseInt(indexOrName)+1);
              else
                  return pApp.getFile(indexOrName);
            }
            catch(Exception e) {
              error="IQuery::getFileFromIndex(@,@)\n"+e.getMessage();
              return null;
            }
          }
          catch (Exception e) {
            error="Impossibile tornare il file del profilo n° "+index;
            return null;
          }
   }   

   /**
    * Metodo che a partire dal profilo, con identificativo sDocNumber,
    * restituito dalla ricerca, esegue il metodo
    * <a href="IQuery.html#getFile(int)">getFile(index)</a>
    * della classe Profilo oppure 
    * <a href="IQuery.html#getFile(java.lang.String)">getFile(String)</a>
    * nel caso in cui venga passato come secondo parametro rispettivamente
    * l'indice o il nome del file.
    * 
    * @param sDocNumber identificativo del profilo nel vettore di Profili
    * @param indexOrName indice nel vettore di file del profilo oppure nome del file
    * @return percorso del download del file (es: c:\windows\temp\allegato.doc)
    * @see <a href="IQuery.html#getFileFromIndex(java.lang.String, java.lang.String)">getFileFromIndex()</a>  
   */   
   public String getFileFromDocNum(String sDocNumber,String indexOrName) {
          try {
            Profilo pApp;
            pApp= getProfileFromDocNum(sDocNumber);
            if (pApp==null) return null;

            pApp.accedi("1");

            try {
              if (indexOrName.indexOf(".")==-1) 
                 return pApp.getFile(Integer.parseInt(indexOrName)+1);
              else 
                 return pApp.getFile(indexOrName);
              
            }
            catch(Exception e) {
              error="IQuery::getFileFromDocNum(@,@)\n"+e.getMessage();
              return null;
            }
          }
          catch (Exception e) {
            error="Impossibile tornare il file del profilo con DocNum="+sDocNumber;
            return null;
          }
   }

   /**
    * Metodo che a partire dal profilo, con indice index,
    * restituito dalla ricerca, esegue il metodo
    * <a href="IQuery.html#getListaFilesize()">getListaFilesize()</a>
    * della classe Profilo.
    * 
    * @param index indice del profilo rispetto al vettore di Profili
    * @return Numero di allegati del profilo. -1 se si verifica un errore
    * @see <a href="IQuery.html#getFileNumberFromDocNum(java.lang.String)">getFileNumberFromDocNum()</a>  
   */
   public long getFileNumberFromIndex(int index) {
          try {
            return getProfileFromIndex(index).getListaFilesize();
          }
          catch (Exception e) {
            error="Impossibile tornare il numero di allegati del profilo n° "+index;
            return -1;
          }
   }

   /**
    * Metodo che a partire dal profilo, con identificativo sDocNumber,
    * restituito dalla ricerca, esegue il metodo
    * <a href="Profilo.html#getListaFilesize()">getListaFilesize()</a>
    * della classe Profilo.
    * 
    * @param sDocNumber identificativo del profilo
    * @return Numero di allegati del profilo. -1 se si verifica un errore
    * @see <a href="IQuery.html#getFileNumberFromIndex(int)">getFileNumberFromIndex()</a>  
   */
   public long getFileNumberFromDocNum(String sDocNumber) {
          try {
            return getProfileFromDocNum(sDocNumber).getListaFilesize();
          }
          catch (Exception e) {
            error="Impossibile tornare il numero di allegati del profilo con DocNum="+sDocNumber;
            return -1;
          }
   }

   /**
    * Metodo che a partire dal profilo, con indice index,
    * restituito dalla ricerca, esegue il metodo
    * <a href="ProfiloBase.html#getCampo(java.lang.String)">getCampo()</a>
    * della classe Profilo.
    * 
    * @param index indice del profilo rispetto al vettore di Profili
    * @param campo nome del campo del profilo
    * @return Valore del campo del profilo
    * @see <a href="IQuery.html#getCampoFromDocNum(java.lang.String, java.lang.String)">getCampoFromDocNum()</a>  
   */   
   public String getCampoFromIndex(int index,String campo) {
          try {
            return getProfileFromIndex(index).getCampo(campo);
          }
          catch (Exception e) {
            error="Impossibile tornare campo "+campo+" del profilo n° "+index;
            return null;
          }
   }   
   
   /**
    * Metodo che a partire dal profilo, con identificativo sDocNumber,
    * restituito dalla ricerca, esegue il metodo
    * <a href="ProfiloBase.html#getCampo(java.lang.String)">getCampo()</a>
    * della classe Profilo.
    * 
    * @param sDocNumber identificativo del profilo
    * @param campo nome del campo del profilo
    * @return Valore del campo del profilo
    * @see <a href="IQuery.html#getCampoFromIndex(int, java.lang.String)">getCampoFromIndex()</a>  
   */
   public String getCampoFromDocNum(String sDocNumber,String campo) {
          try {
            return getProfileFromDocNum(sDocNumber).getCampo(campo);
          }
          catch (Exception e) {
            error="Impossibile tornare campo "+campo+" del profilo con DocNum="+sDocNumber;
            return null;
          }
   }
      
   /**
    * Metodo che a partire dal profilo, con indice index,
    * restituito dalla ricerca, esegue il metodo
    * <a href="IQuery.html#getDocNumber()">getDocNumber()</a>
    * della classe Profilo.
    * 
    * @param index indice del profilo rispetto al vettore di Profili
    * @return identificativo del profilo  
   */
   public String getDocNumFromIndex(int index) {
          try {
            return getProfileFromIndex(index).getDocNumber();
          }
          catch (Exception e) {
            error="Impossibile tornare il docNumber del profilo n° "+index;
            return null;
          }
   }     
   
   /**
    * Metodo che aggiunge, alla lista dei valori da inseire
    * nel profilo-query, la coppia (campo,valore).
    * 
    * @param key campo del profilo-query
    * @param val valore da aggiungere al campo del profilo-query
   */   
   public void addValue(String key,String val) {
          Valori v = new Valori(key,val);

          profili.add(v);
   }     

   /**
    * Metodo che setta il tempo (in millisecondi)
    * di timeout della query in ricerca. Es: 30000 = 30 sec.
    * Per default il timeout è impstato a 60 sec. 
    * 
    * @param time millisecondi di timeout 
   */
   public void setQueryTimeOut(int time) {	
	  	  queryTimeOut=time;
   }
   
   public void setSqlSelect(String sql) {
          sqlSelect = sql;
   }

   /**
    * Metodo che stabilisce se gli i profili restituiti
    * dalla query in seguito ad una ricerca devono
    * essere "padri" dei documenti trovati. Esempio:<BR>
    * viene trovato il profilo con id=1789 che ha padre
    * 1800. Se bMaster è settato a true viene restituito 
    * 1800, 1789 in caso contrario.
    * 
    * @param bMaster <BR>True - vengono restituiti i padri dei profili trovati<BR>
    * 				 False - la ricerca si comporta in maniera standard: restituisce ciò che trova    
   */   
   public void setQueryMaster(boolean bMaster) {	
	  	  bIsMaster=bMaster;
   } 
   
   /**
    * Metodo che aggiunge alla lista dei campi da ordinare
    * il campo passato in input.
    * L'ordinamento applicato al campo è ASC
    * 
    * @param campo nome del campo sul quale ordinare  
    * @see <a href="IQuery.html#addCampoOrdinamentoDesc(java.lang.String)">addCampoOrdinamentoDesc(String)</a> 
   */   
   public void addCampoOrdinamentoAsc(String campo) {	 
	      campiOrdinamento.add(campo+"@ASC");
   }   

   /**
    * Metodo che aggiunge alla lista dei campi da ordinare
    * il campo passato in input.
    * L'ordinamento applicato al campo è DESC
    * 
    * @param campo nome del campo sul quale ordinare  
    * @see <a href="IQuery.html#addCampoOrdinamentoAsc(java.lang.String)">addCampoOrdinamentoAsc(String)</a> 
   */   
   public void addCampoOrdinamentoDesc(String campo) {	 
	      campiOrdinamento.add(campo+"@DESC");	      	      	      
   }
   
   /**
    * Metodo che aggiunge alla lista dei campi da ordinare
    * il campo passato in input per il tipo documento dato da area e cm.
    * L'ordinamento applicato al campo è DESC
    *  
    * Verrà effettuato il mapping sul campo e sul cm passati
    *  
    * @param campo nome del campo sul quale ordinare
    * @param area Area sulla quale ricercare il campo
    * @param cm Codice modello sul quale ricercare il campo  
    * @see <a href="IQuery.html#addCampoOrdinamentoAsc(java.lang.String, java.lang.String, java.lang.String)">addCampoOrdinamentoAsc(String, String, String)</a> 
   */      
   public void addCampoOrdinamentoDesc(String campo, String area, String cm) {
	      addCampoOrdinamento("DESC",campo,area,cm);
   }

    public void addCampoOrdinamentoDesc(String campo, String area, String cm, String formatoCampo) {
        addCampoOrdinamento("DESC",campo,area,cm, formatoCampo);
    }
   
   /**
    * Metodo che aggiunge alla lista dei campi da ordinare
    * il campo passato in input per il tipo documento dato da area e cm.
    * L'ordinamento applicato al campo è ASC
    * 
    * Verrà effettuato il mapping sul campo e sul cm passati
    * 
    * @param campo nome del campo sul quale ordinare
    * @param area Area sulla quale ricercare il campo
    * @param cm Codice modello sul quale ricercare il campo  
    * @see <a href="IQuery.html#addCampoOrdinamentoDesc(java.lang.String, java.lang.String, java.lang.String)">addCampoOrdinamentoDesc(String, String, String)</a> 
   */   
   public void addCampoOrdinamentoAsc(String campo, String area, String cm) {
	      addCampoOrdinamento("ASC",campo,area,cm);
   }

    public void addCampoOrdinamentoAsc(String campo, String area, String cm, String formatoCampo) {
        addCampoOrdinamento("ASC",campo,area,cm, formatoCampo);
    }

    private void addCampoOrdinamento(String tipoOrd, String campo, String area, String cm) {
        addCampoOrdinamento(tipoOrd, campo, area, cm, null);
    }

    private void addCampoOrdinamento(String tipoOrd, String campo, String area, String cm, String formatoCampo) {

	      //Controllo se la chiave esiste già come Return.
	      //In caso positivo verrà aggiornata come campo
	      //ISCAMPO_ORDINAMENTO_AND_RETURN e si esce
	   	  if (controllaVettoreCampoOrdinamento(campo,area,cm,null,tipoOrd)) return;
	   
	      keyval k = new keyval();
	      
	      k.setKey(campo+"@"+tipoOrd);
	      k.setArea(area);
	      k.setCm(cm);
	      k.setCampoReturn(k.ISCAMPO_ORDINAMENTO);
	      k.setNoCaseSensitive(bUseCaseNoSensitive);
	      k.setFormatoCampo(formatoCampo);
	     	      
	      campiOrdinamento.add(k);	      	      	      
   }

   /**
    * Metodo che aggiunge alla lista dei campi da ordinare
    * il campo passato in input per la categoria "categoria"
    * L'ordinamento applicato al campo è DESC
    * 
    * @param campo nome del campo sul quale ordinare
    * @param categoria categoria sulla quale ricercare il campo  
    * @see <a href="IQuery.html#addCampoOrdinamentoAsc(java.lang.String, java.lang.String)">addCampoOrdinamentoAsc(String, String)</a> 
   */      
   public void addCampoOrdinamentoDesc(String campo, String categoria) {
	      addCampoOrdinamento("DESC",campo,categoria);
   }


   
   /**
    * Metodo che aggiunge alla lista dei campi da ordinare
    * il campo passato in input per la categoria "categoria"
    * L'ordinamento applicato al campo è ASC
    * 
    * @param campo nome del campo sul quale ordinare
    * @param categoria categoria sulla quale ricercare il campo  
    * @see <a href="IQuery.html#addCampoOrdinamentoDesc(java.lang.String, java.lang.String)">addCampoOrdinamentoDesc(String, String)</a> 
   */       
   public void addCampoOrdinamentoAsc(String campo, String categoria) {
	      addCampoOrdinamento("ASC",campo,categoria);
   }

    public void addCampoOrdinamentoAscConFormato(String campo, String categoria, String formatoCampo) {
        if (controllaVettoreCampoOrdinamento(campo,null,null,categoria,"ASC")) return;

        keyval k = new keyval();

        k.setKey(campo+"@ASC");
        k.setCategoria(categoria);
        k.setCampoReturn(k.ISCAMPO_ORDINAMENTO);
        k.setNoCaseSensitive(bUseCaseNoSensitive);
        k.setFormatoCampo(formatoCampo);

        campiOrdinamento.add(k);
    }

    public void addCampoOrdinamentoDescConFormato(String campo, String categoria, String formatoCampo) {
        addCampoOrdinamentoConFormato("DESC",campo,categoria,formatoCampo);
    }

    private void addCampoOrdinamentoConFormato(String tipoOrd, String campo, String categoria, String formatoCampo) {
//Controllo se la chiave esiste già come Return.
        //In caso positivo verrà aggiornata come campo
        //ISCAMPO_ORDINAMENTO_AND_RETURN e si esce
        if (controllaVettoreCampoOrdinamento(campo,null,null,categoria,tipoOrd)) return;

        keyval k = new keyval();

        k.setKey(campo+"@"+tipoOrd);
        k.setCategoria(categoria);
        k.setCampoReturn(k.ISCAMPO_ORDINAMENTO);
        k.setNoCaseSensitive(bUseCaseNoSensitive);
        k.setFormatoCampo(formatoCampo);

        campiOrdinamento.add(k);
    }


   private void addCampoOrdinamento(String tipoOrd, String campo, String categoria) {

	      //Controllo se la chiave esiste già come Return.
	      //In caso positivo verrà aggiornata come campo
	      //ISCAMPO_ORDINAMENTO_AND_RETURN e si esce
	   	  if (controllaVettoreCampoOrdinamento(campo,null,null,categoria,tipoOrd)) return;
	   
	      keyval k = new keyval();
	      
	      k.setKey(campo+"@"+tipoOrd);
	      k.setCategoria(categoria);
	      k.setCampoReturn(k.ISCAMPO_ORDINAMENTO);
	      k.setNoCaseSensitive(bUseCaseNoSensitive);
	      
	      campiOrdinamento.add(k);	      	      	      
   }

   /**
    * Metodo che aggiunge alla lista dei campi da restituire
    * il campo passato in input per il tipo documento dato da area e cm.
    *  
    * Verrà effettuato il mapping sul campo e sul cm passati
    * 
    * IL METODO FUNZIONA SOLO SUI CAMPI INSERITI ATTRAVERSO
    * IL METODO addCampo o addCampoFT DOVE VENGONO SPECIFICATI
    * AREA E CODICE_MODELLO
    *  
    * @param campo nome del campo da restituire
    * @param area Area sulla quale ricercare il campo
    * @param cm Codice modello sul quale ricercare il campo  
    * @see <a href="IQuery.html#addCampoReturn(java.lang.String, java.lang.String)">addCampoReturn(String, String)</a> 
   */     
   public void addCampoReturn(String campo, String area, String cm) {
	    
	      //Controllo se la chiave esiste già come Ordinamento.
	      //In caso positivo verrà aggiornata come campo
	      //ISCAMPO_ORDINAMENTO_AND_RETURN e si esce
	   	  if (controllaVettoreCampoOrdinamento(campo,area,cm,null,"DONTCARE")) return;
	   
	      keyval k = new keyval();
	      
	      k.setKey(campo+"@DONTCARE");
	      k.setArea(area);
	      k.setCm(cm);
	      k.setCampoReturn(k.ISCAMPO_RETURN);
	      k.setNoCaseSensitive(bUseCaseNoSensitive);
	      
	      campiOrdinamento.add(k);		      	     
   }

    public void addCampoReturnConFormato(String campo, String area, String cm, String formatoCampo) {

        //Controllo se la chiave esiste già come Ordinamento.
        //In caso positivo verrà aggiornata come campo
        //ISCAMPO_ORDINAMENTO_AND_RETURN e si esce
        if (controllaVettoreCampoOrdinamento(campo,area,cm,null,"DONTCARE")) return;

        keyval k = new keyval();

        k.setKey(campo+"@DONTCARE");
        k.setArea(area);
        k.setCm(cm);
        k.setCampoReturn(k.ISCAMPO_RETURN);
        k.setNoCaseSensitive(bUseCaseNoSensitive);
        k.setFormatoCampo(formatoCampo);

        campiOrdinamento.add(k);
    }

    /**
    * Metodo che aggiunge alla lista dei campi da restituire
    * il campo passato in input per la categoria "categoria"    
    * 
    * IL METODO FUNZIONA SOLO SUI CAMPI INSERITI ATTRAVERSO
    * IL METODO addCampoCategoria o addCampoCategoriaFT DOVE 
    * VIENE SPECIFICATA LA CATEGORIA
    * 
    * @param campo nome del campo da restituire
    * @param categoria categoria sulla quale ricercare il campo  
    * @see <a href="IQuery.html#addCampoReturn(java.lang.String, java.lang.String, java.lang.String)">addCampoReturn(String, String, String)</a> 
   */     
   public void addCampoReturn(String campo, String categoria) {

	      //Controllo se la chiave esiste già come Ordinamento.
	      //In caso positivo verrà aggiornata come campo
	      //ISCAMPO_ORDINAMENTO_AND_RETURN e si esce
	   	  if (controllaVettoreCampoOrdinamento(campo,null,null,categoria,"DONTCARE")) return;
	   
	      keyval k = new keyval();
	      
	      k.setKey(campo+"@DONTCARE");
	      k.setCategoria(categoria);
	      k.setCampoReturn(k.ISCAMPO_RETURN);
	      k.setNoCaseSensitive(bUseCaseNoSensitive);
	      
	      campiOrdinamento.add(k);	      	      	      
   }

    public void addCampoReturnConFormato(String campo, String categoria, String formatoCampo) {

        //Controllo se la chiave esiste già come Ordinamento.
        //In caso positivo verrà aggiornata come campo
        //ISCAMPO_ORDINAMENTO_AND_RETURN e si esce
        if (controllaVettoreCampoOrdinamento(campo,null,null,categoria,"DONTCARE")) return;

        keyval k = new keyval();

        k.setKey(campo+"@DONTCARE");
        k.setCategoria(categoria);
        k.setCampoReturn(k.ISCAMPO_RETURN);
        k.setNoCaseSensitive(bUseCaseNoSensitive);
        k.setFormatoCampo(formatoCampo);

        campiOrdinamento.add(k);
    }


    /**
    * Metodo che abilita il "no case sensitive" sui campi 
    * aggiunti in ricerca.
    * 
    * Lanciando la Iq.enableAddCampoNoCaseSensitive tutte le
    * successive addCampo che contengono dei campi di tipo stringa
    * saranno considerate "no case sensitive", di conseguenza alla
    * relativa frase di where verrà aggiunto un upper sia sul campo
    * che sulla condizione.
    * 
    * !!!!ABILITATA SOLO SU RICERCHE DI TIPO ORIZZONTALE!!!!
    * 
    * @see <a href="IQuery.html#disableAddCampoNoCaseSensitive()">disableAddCampoNoCaseSensitive()</a> 
   */
   public void enableAddCampoNoCaseSensitive() {
	      bUseCaseNoSensitive=true;
   }
   
   /**
    * Metodo che disabilita quanto impostato dalla 
    * Iq.enableAddCampoNoCaseSensitive
    * 
    * @see <a href="IQuery.html#enableAddCampoNoCaseSensitive()">enableAddCampoNoCaseSensitive()</a>
   */   
   public void disableAddCampoNoCaseSensitive() {
	      bUseCaseNoSensitive=false;
   }   
   
   /**
    * Metodo che aggiunge alla lista dei campi da ricercare
    * la coppia (campo,valore).
    * Verrà effettuata una ricerca puntuale sul campo valore_stringa
    * senza l'utilizzo di intermedia.
    * Se si sono scelti dei tipi documento su cui effettuare la ricerca
    * attraverso il metodo
    * <a href="IQuery.html#addCodiceModello(java.lang.String, java.lang.String)">addCodiceModello()</a>,
    * il valore verrà ricercato solo nei campi di questi modelli.
    * Nel secondo parametro è possibile passare la stringa "is null" oppure "is not null":
    * in tal caso verranno i relativi operatori di confronto con i valori nulli. Esempio:<BR>
    * <BR>
    * Iq.addCampo("SOGGETTO","SAMOGGIA") -> condizione sql: SOGGETTO='SAMOGGIA'<BR>
    * Iq.addCampo("SOGGETTO","is null") -> condizione sql: SOGGETTO is null 
    * 
    * @param campo nome del campo su cui effettuare la ricerca    
    * @param valore valore su cui effettuare la ricerca oppure "is null" oppure "is not null"
    * @see <a href="IQuery.html#addCampo(java.lang.String, java.lang.String, java.lang.String)">addCampo(String, String, String)</a> 
   */   
   public void addCampo(String campo, String valore) {
	      if (campo.equals("CR")) {crSingleDocSearch=valore;return;}
	   
	      keyval k = new keyval(campo,valore);
	      k.setNoCaseSensitive(bUseCaseNoSensitive);
	      
	      campiRicerca.add(k);
   }
   
   /**
    * Metodo che si comporta come
    * <a href="IQuery.html#addCampo(java.lang.String, java.lang.String)">addCampo(String, String)</a>
    * sostituendo campo con una NVL(campo,valoreOption)
   */   
   public void addCampoNvl(String campo, String valoreCampoOption, String valore) {	
	      keyval k = new keyval(campo,valore);	      
	      
	      k.setValueNvl(valoreCampoOption);
	      k.setNoCaseSensitive(bUseCaseNoSensitive);
	      
	      campiRicerca.add(k);
   }      

   /**
    * Metodo che aggiunge alla lista dei campi da ricercare
    * la coppia (campo,valore) per il modello cm sull'area area.
    * Verrà effettuata una ricerca puntuale sul campo valore_stringa
    * senza l'utilizzo di intermedia.
    * Utilizzando questo metodo NON saranno presi in considerazione i tipi documento 
    * su cui effettuare la ricerca attraverso il metodo
    * <a href="IQuery.html#addCodiceModello(java.lang.String, java.lang.String)">addCodiceModello()</a>,
    * il valore verrà ricercato solo nei campi del modello/area impostato su questo metodo.
    * Nel secondo parametro è possibile passare la stringa "is null" oppure "is not null":
    * in tal caso verranno i relativi operatori di confronto con i valori nulli. Esempio:<BR>
    * <BR>
    * Iq.addCampo("SOGGETTO","SAMOGGIOA","SEGRETERIA","M_SOGGETTO") -> condizione sql: SOGGETTO='SAMOGGIA'<BR>
    * Iq.addCampo("SOGGETTO","is null","SEGRETERIA","M_SOGGETTO") -> condizione sql: SOGGETTO is null 
    * 
    * Verrà effettuato il mapping sul campo e sul cm passati
    * 
    * @param campo nome del campo su cui effettuare la ricerca    
    * @param valore valore su cui effettuare la ricerca oppure "is null" oppure "is not null"
    * @param area Area sulla quale ricercare il campo
    * @param cm Codice modello sul quale ricercare il campo
    * @see <a href="IQuery.html#addCampo(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)">addCampo(String, String, String, String, String, String)</a> 
   */   
   public void addCampo(String campo, String valore, String area, String cm) {
	      addCampo(campo, valore, area, cm, true, null);
   }
   
   /**
    * Metodo che si comporta come
    * <a href="IQuery.html#addCampo(java.lang.String, java.lang.String, java.lang.String, java.lang.String)">addCampo(String, String, String, String)</a>
    * sostituendo campo con una NVL(campo,valoreOption)
   */    
   public void addCampoNvl(String campo, String valoreCampoOption, String valore, String area, String cm) {
	      addCampo(campo, valore, area, cm, true, valoreCampoOption);
   }   
   
   /**
    * Metodo che richiama il funzionamento di
    * <a href="IQuery.html#addCampo(java.lang.String, java.lang.String, java.lang.String, java.lang.String)">addCampo(String, String, String, String)</a>.
    * L'unica differenza risiede nel fatto che, solo nel caso in cui venga richiamata la
    * ricercaFT in luogo della ricerca, l'interrogazione per il campo specificato in input 
    * verrà effettuata mediante intermedia sul campo clob.
    * 
    * @param campo nome del campo su cui effettuare la ricerca    
    * @param valore valore su cui effettuare la ricerca oppure "is null" oppure "is not null"
    * @param area Area sulla quale ricercare il campo
    * @param cm Codice modello sul quale ricercare il campo
    * @see <a href="IQuery.html#addCampoFT(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)">addCampoFT(String, String, String, String, String, String)</a> 
   */    
   public void addCampoFT(String campo, String valore, String area, String cm) {
	      addCampo(campo, valore, area, cm, false, null);
   }   
   
   /**
    * Metodo che si comporta come
    * <a href="IQuery.html#addCampoFT(java.lang.String, java.lang.String, java.lang.String, java.lang.String)">addCampoFT(String, String, String, String)</a>
    * sostituendo campo con una NVL(campo,valoreOption)
   */    
   public void addCampoFTNvl(String campo, String valoreCampoOption, String valore, String area, String cm) {
	      addCampo(campo, valore, area, cm, false, valoreCampoOption);
   }    
   
   private void addCampo(String campo, String valore, String area, String cm, boolean ft, String valoreNvl) {
	       if (campo.equals("CR")) {crSingleDocSearch=valore;return;}
	   
	       keyval k = new keyval(campo,valore);
	       k.setArea(area);
	       k.setCm(cm);
	       k.setValueNvl(valoreNvl);
	       k.setIsRicercaPuntuale(ft);
	       k.setNoCaseSensitive(bUseCaseNoSensitive);
	      
	       campiRicerca.add(k);	
   }
   
   /**
    * Metodo che aggiunge alla lista dei campi da ricercare
    * la coppia (campo,valore) per la categoria "categoria"
    * Verrà effettuata una ricerca puntuale sul campo valore_stringa
    * senza l'utilizzo di intermedia.
    * Utilizzando questo metodo NON saranno presi in considerazione i tipi documento 
    * su cui effettuare la ricerca attraverso il metodo
    * <a href="IQuery.html#addCodiceModello(java.lang.String, java.lang.String)">addCodiceModello()</a>,
    * il valore verrà ricercato solo nei campi della categoria impostata su questo metodo.
    * Nel secondo parametro è possibile passare la stringa "is null" oppure "is not null":
    * in tal caso verranno i relativi operatori di confronto con i valori nulli. Esempio:<BR>
    * <BR>
    * Iq.addCampoCategoria("SOGGETTO","SAMOGGIOA","PROTO") -> condizione sql: SOGGETTO='SAMOGGIA'<BR>
    * Iq.addCampoCategoria("SOGGETTO","is null","PROTO") -> condizione sql: SOGGETTO is null 
    * 
    * @param campo nome del campo su cui effettuare la ricerca    
    * @param valore valore su cui effettuare la ricerca oppure "is null" oppure "is not null"
    * @param categoria Categoria sulla quale ricercare il campo
    * @see <a href="IQuery.html#addCampoCategoria(java.lang.String, java.lang.String, java.lang.String, java.lang.String)">addCampo(String, String, String, String)</a> 
   */   
   public void addCampoCategoria(String campo, String valore, String categoria) {
	      addCampoCategoria(campo,valore,categoria,true,null);
   }
   
   /**
    * Metodo che si comporta come
    * <a href="IQuery.html#addCampoCategoria(java.lang.String, java.lang.String, java.lang.String)">addCampoCategoria(String, String, String)</a>
    * sostituendo campo con una NVL(campo,valoreOption)
   */      
   public void addCampoCategoriaNvl(String campo, String valoreCampoOption, String valore, String categoria) {
	      addCampoCategoria(campo,valore,categoria,true,valoreCampoOption);
   }   
   
   /**
    * Metodo che richiama il funzionamento di
    * <a href="IQuery.html#addCampoCategoria(java.lang.String, java.lang.String, java.lang.String)">addCampo(String, String, String)</a>.
    * L'unica differenza risiede nel fatto che, solo nel caso in cui venga richiamata la
    * ricercaFT in luogo della ricerca, l'interrogazione per il campo specificato in input 
    * verrà effettuata mediante intermedia sul campo clob. 
    * 
    * @param campo nome del campo su cui effettuare la ricerca    
    * @param valore valore su cui effettuare la ricerca oppure "is null" oppure "is not null"
    * @param categoria Categoria sulla quale ricercare il campo
    * @see <a href="IQuery.html#addCampoCategoriaFT(java.lang.String, java.lang.String, java.lang.String, java.lang.String)">addCampoCategoriaFT(String, String, String, String)</a> 
   */   
   public void addCampoCategoriaFT(String campo, String valore, String categoria) {
	      addCampoCategoria(campo,valore,categoria,false,null);
   }   
   
   /**
    * Metodo che si comporta come
    * <a href="IQuery.html#addCampoCategoriaFT(java.lang.String, java.lang.String, java.lang.String)">addCampoCategoriaFT(String, String, String)</a>
    * sostituendo campo con una NVL(campo,valoreOption)
   */      
   public void addCampoCategoriaFTNvl(String campo, String valoreCampoOption, String valore, String categoria) {
	      addCampoCategoria(campo,valore,categoria,false,valoreCampoOption);
   }    
   
   private void addCampoCategoria(String campo, String valore, String categoria, boolean ft, String valoreNvl) {
	      if (campo.equals("CR")) {crSingleDocSearch=valore;return;}
	   
	      keyval k = new keyval(campo,valore);
	      k.setCategoria(categoria);
	      k.setIsRicercaPuntuale(ft);
	      k.setValueNvl(valoreNvl);
	      k.setNoCaseSensitive(bUseCaseNoSensitive);
	      
	      campiRicerca.add(k);	      
   }
   
   /**
    * Metodo che aggiunge una condizione di filtro
    * sugli oggetti file associati ai documenti
    * dei tipo specificati dalla
    * <a href="IQuery.html#addCodiceModello(java.lang.String, java.lang.String)">addCodiceModello()</a>, 
    * 
    * @param text testo da ricercare dentro il file    
    * @see <a href="IQuery.html#setOggettoFileCondition(java.lang.String, java.lang.String, java.lang.String)">setOggettoFileCondition(String, String, String)</a> 
    * @see <a href="IQuery.html#setOggettoFileCondition(java.lang.String, java.lang.String)">setOggettoFileCondition(String, String)</a>     
   */   
   public void setOggettoFileCondition(String text) {
	           setOggettoFileCondition(text,"","","",false);	      	      
   }
   
   /**
    * Metodo che aggiunge una condizione di filtro
    * sugli oggetti file associati ai documenti per la colonna OCR
    * dei tipo specificati dalla
    * <a href="IQuery.html#addCodiceModello(java.lang.String, java.lang.String)">addCodiceModello()</a>, 
    * 
    * @param text 	testo da ricercare dentro il file     
    * @see <a href="IQuery.html#setOggettoFileConditionOcr(java.lang.String, java.lang.String, java.lang.String)">setOggettoFileConditionOcr(String, String, String)</a> 
    * @see <a href="IQuery.html#setOggettoFileConditionOcr(java.lang.String, java.lang.String)">setOggettoFileConditionOcr(String, String)</a>     
   */   
   public void setOggettoFileConditionOcr(String text) {
	           setOggettoFileCondition(text,"","","",true);	      	      
   }

   /**
    * Metodo che aggiunge una condizione di filtro
    * sugli oggetti file associati ai documenti
    * dei tipo specificati da area e cm passati in input 
    * 
    * Verrà effettuato il mapping sul cm
    * 
    * @param text testo da ricercare dentro il file    
    * @param ar
    * @param cm
    * @see <a href="IQuery.html#setOggettoFileCondition(java.lang.String)">setOggettoFileCondition(String)</a> 
    * @see <a href="IQuery.html#setOggettoFileCondition(java.lang.String, java.lang.String)">setOggettoFileCondition(String, String)</a>     
   */    
   public void setOggettoFileCondition(String text, String ar, String cm) {
	           setOggettoFileCondition(text,ar,cm,"",false);	      	      
   }  
   
   /**
    * Metodo che aggiunge una condizione di filtro
    * sugli oggetti file associati ai documenti per la colonna OCR
    * dei tipo specificati da area e cm passati in input 
    * 
    * Verrà effettuato il mapping sul cm
    * 
    * @param text testo da ricercare dentro il file    
    * @param ar
    * @param cm
    * @see <a href="IQuery.html#setOggettoFileConditionOcr(java.lang.String)">setOggettoFileConditionOcr(String)</a> 
    * @see <a href="IQuery.html#setOggettoFileConditionOcr(java.lang.String, java.lang.String)">setOggettoFileConditionOcr(String, String)</a>     
   */    
   public void setOggettoFileConditionOcr(String text, String ar, String cm) {
	           setOggettoFileCondition(text,ar,cm,"",true);	      	      
   }

   /**
    * Metodo che aggiunge una condizione di filtro
    * sugli oggetti file associati ai documenti
    * dei tipo specificati dalla categoria passata in input 
    * 
    * @param text testo da ricercare dentro il file    
    * @param categoria
    * @see <a href="IQuery.html#setOggettoFileCondition(java.lang.String)">setOggettoFileCondition(String)</a> 
    * @see <a href="IQuery.html#setOggettoFileCondition(java.lang.String, java.lang.String, java.lang.String)">setOggettoFileCondition(String, String, String)</a>     
   */   
   public void setOggettoFileCondition(String text,String categoria) {
	           setOggettoFileCondition(text,"","",categoria,false);	      	      
   }   
   
   /**
    * Metodo che aggiunge una condizione di filtro
    * sugli oggetti file associati ai documenti per la colonna OCR
    * dei tipo specificati dalla categoria passata in input 
    * 
    * @param text testo da ricercare dentro il file    
    * @param categoria
    * @see <a href="IQuery.html#setOggettoFileConditionOcr(java.lang.String)">setOggettoFileConditionOcr(String)</a> 
    * @see <a href="IQuery.html#setOggettoFileConditionOcr(java.lang.String, java.lang.String, java.lang.String)">setOggettoFileConditionOcr(String, String, String)</a>     
   */   
   public void setOggettoFileConditionOcr(String text,String categoria) {
	           setOggettoFileCondition(text,"","",categoria,true);	      	      
   }   
   
   private void setOggettoFileCondition(String text, String ar, String cm, String categoria,boolean isOcr) {
	   			keyval k = new keyval();
	   			
	   			k.setKey(text);
	   			k.setCm(cm);
	   			k.setArea(ar);
	   			k.setCategoria(categoria);
	   			k.setNoCaseSensitive(bUseCaseNoSensitive);
	   			k.setIsOcr(isOcr);
	   
	   			addObjFileCondition(k);			   				   				
   }
   
   /**
    * Metodo che aggiunge una condizione di join fra i modelli
    * passati in input mediante un campo comune. Esempio:<BR>
    * <BR>
    * Iq.addJoinModel("SEGRETERIA.PROTOCOLLO","M_PROTOCOLLO","IDRIF",
    *                 "SEGRETERIA.PROTOCOLLO","M_SOGGETTO","IDRIF")<BR>
    * 
    * @param area1    
    * @param cm1 
    * @param campo1
    * @param area2   
    * @param cm2
    * @param campo2 
    * @see <a href="IQuery.html#addJoinClass(java.lang.String, java.lang.String, java.lang.String, java.lang.String)">addJoinClass(String, String, String, String)</a> 
    * @see <a href="IQuery.html#addJoinMix(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)">addJoinMix(String, String, String, String, String)</a>     
   */   
   public void addJoinModel(String area1, String cm1, String campo1, String area2, String cm2, String campo2) {   
	      addJoinModel(area1,cm1,null,campo1,area2,cm2,null,campo2);
   }

   /**
    * Metodo che aggiunge una condizione di join fra le
    * categorie passate in input mediante un campo comune. Esempio:<BR>
    * <BR>
    * addJoinClass("PROTO","IDRIF",
    *              "ALL_PROTO","IDRIF")<BR>
    * 
    * @param cat1   
    * @param campo1
    * @param cat2
    * @param campo2 
    * @see <a href="IQuery.html#addJoinModel(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)">addJoinClass(String, String, String, String, String, String)</a> 
    * @see <a href="IQuery.html#addJoinMix(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)">addJoinMix(String, String, String, String, String)</a>     
   */   
   public void addJoinClass(String cat1, String campo1, String cat2, String campo2) {   
	      addJoinModel(null,null,cat1,campo1,null,null,cat2,campo2);
   }

   /**
    * Metodo che aggiunge una condizione di join fra il modello e la
    * categoria passate in input mediante un campo comune. Esempio:<BR>
    * <BR>
    * addJoinMix(SEGRETERIA.PROTOCOLLO","M_PROTOCOLLO","IDRIF",
    *              "PROTO","IDRIF")<BR>
    * 
    * @param area1
    * @param cm1
    * @param campo1
    * @param cat2
    * @param campo2 
    * @see <a href="IQuery.html#addJoinModel(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)">addJoinClass(String, String, String, String, String, String)</a> 
    * @see <a href="IQuery.html#addJoinClass(java.lang.String, java.lang.String, java.lang.String, java.lang.String)">addJoinMix(String, String, String, String)</a>     
   */   
   public void addJoinMix(String area1, String cm1, String campo1, String cat2, String campo2) {   
	      addJoinModel(area1,cm1,null,campo1,null,null,cat2,campo2);
   }      
   
   private void addJoinModel(String area1, String cm1, String cat1, String campo1, String area2, String cm2, String cat2, String campo2) {   
	      keyval k = new keyval(campo1,"DONTCARE");
	      k.setArea(area1);
	      k.setCm(cm1);
	      k.setCategoria(cat1);
	      k.setIndexJoin(joinCounter);
	      k.setNoCaseSensitive(bUseCaseNoSensitive);
	      
	      campiRicerca.add(k);	
	      
	      keyval k2 = new keyval(campo2,"DONTCARE");
	      k2.setArea(area2);
	      k2.setCm(cm2);
	      k2.setCategoria(cat2);
	      k2.setIndexJoin(joinCounter);
	      k2.setNoCaseSensitive(bUseCaseNoSensitive);
	      
	      campiRicerca.add(k2);	
	      
	      joinCounter++;
   }      

   /**
    * Metodo che restituisce un vettore di oggetti
    * di classe keyval (it.finmatica.dmServer.util.keyval).
    * Gli oggetti contengono le terne (campo,operatore,valore)
    * corrispondenti al filtro impostato nella ricerca
    * 
    * @return Vettore di keyval
    * @see <a href="IQuery.html#getFiltroCampo(java.lang.String)">getFiltroCampo(String)</a> 
   */   
   public Vector getFiltroCampo() {
          return getFiltroCampo("");
   }
   
   /**
    * Metodo che restituisce un vettore di oggetti
    * di classe keyval (it.finmatica.dmServer.util.keyval).
    * Gli oggetti contengono le coppie (operatore,valore)
    * corrispondenti al filtro impostato per il campo
    * passato in input
    * 
    * @param campo nome del campo su cui filtrare le coppie (operatore,valore)
    * @return Vettore di keyval
    * @see <a href="IQuery.html#getFiltroCampo()">getFiltroCampo()</a> 
   */
   public Vector getFiltroCampo(String campo) {
	      Vector vCampi = new Vector();
	      
	      for(int i=0;i<campiRicerca.size();i++) {
	    	  keyval k = (keyval)campiRicerca.get(i);
	    	  
	    	  if (k.getKey().equals(campo) || campo.equals("")) {
    	    	  keyval kApp = new keyval(k.getKey(),k.getVal(),k.getTipoDoc(), k.getOperator());
	        	  kApp.setTipoDaClient(k.getTipoDaClient());

	    		  if (kApp.getTipoDaClient()!=null && kApp.getTipoDaClient().equals("Between")) {
	    			  kApp.setValue(kApp.getVal()+","+kApp.getOperator());
	    			  kApp.setOperator("Between");
	    		  }
	    		  if (kApp.getVal().equals("is null") || kApp.getVal().equals("is not null")) {
	    			  kApp.setOperator(kApp.getVal());
	    			  kApp.setValue("");	    			  
	    		  }	 
	    		  if (kApp.getOperator().equals("contains")) {
	    			  kApp.setOperator("=");
	    		  }	    			  
	    			  
	    		  vCampi.add(kApp);
	    	  }
	      }
	      
	      return vCampi;
   }

   /**
    * Se al metodo viene passato false - i valori nulli del resultset sono restituiti come "null" (Stringa)
    * Se al metodo viene passato true  - i valori nulli del resultset sono restituiti come null (Puntatore a null)
    * 
   */   
   public void setResultSetNullValue(boolean bFlag) {
	      bResulsetIQuery_NullValueNullVariable=bFlag;
   }
   
   /**
    * Meotodo che restituisce una classe ResultSet che contiene
    * campi e valori restituti dalla IQuery in funzione dell'utilizzo
    * del metodo addCampoReturn.
    * 
    * Il metodo va chiamato successivamente ad una ricerca
    * @return ResultSetIQuery
   */
   public ResultSetIQuery getResultSet() {
	      if (rst==null)
	    	  rst=new ResultSetIQuery(vCampiReturnCursor,bResulsetIQuery_NullValueNullVariable);
	      
	      return rst;
   }      
   
   /**
    * Metodo che aggiunge alla lista dei campi da ricercare
    * la coppia (campo,valore,operatore o secondo valore).
    * Verrà effettuata una ricerca su un campo stringa o numero
    * Se si sono scelti dei tipi documento su cui effettuare la ricerca
    * attraverso il metodo
    * <a href="IQuery.html#addCodiceModello(java.lang.String, java.lang.String)">addCodiceModello()</a>,
    * il valore verrà ricercato solo nei campi di questi modelli.
    * Se il valore passato contiene il token % , l'operatore = viene automaticamente convertito
    * in LIKE 
    * Il terzo parametro può essere utilizzato come operatore da applicare
    * alla ricerca sul campo oppure come secondo valore di confronto nel caso
    * in cui si voglia specificare un intervallo di preferenza. Esempio:<BR>
    * <BR>
    * Iq.addCampo("N_REG", "1", ">") ->  condizione sql: N_REG > 1<BR>
    * Iq.addCampo("N_REG", "1", "2") ->  condizione sql: N_REG between 1 and 2<BR>
    * 
    * @param campo nome del campo su cui effettuare la ricerca
    * @param valore valore su cui effettuare la ricerca
    * @param operatore operatore o secondo valore. Valori possibili come operatore:<BR>
    * 		 >, <, <>, =
    * 	     
    * @see <a href="IQuery.html#addCampo(java.lang.String, java.lang.String)">addCampo(String, String)</a> 
   */
   public void addCampo(String campo, String valore, String operatore) {	
	      keyval k = new keyval(campo,valore,null,operatore);	      
	      k.setNoCaseSensitive(bUseCaseNoSensitive);
	      
	      campiRicerca.add(k);
   }
   
   /**
    * Metodo che si comporta come
    * <a href="IQuery.html#addCampo(java.lang.String, java.lang.String, java.lang.String)">addCampo(String, String, String)</a>
    * sostituendo campo con una NVL(campo,valoreOption)
   */     
   public void addCampoNvl(String campo, String valoreCampoOption, String valore, String operatore) {	
	      keyval k = new keyval(campo,valore,null,operatore);	      
	      
	      k.setValueNvl(valoreCampoOption);
	      k.setNoCaseSensitive(bUseCaseNoSensitive);
	      
	      campiRicerca.add(k);
   }   
   
   /**
    * Metodo che aggiunge alla lista dei campi da ricercare
    * la coppia (campo,valore,operatore o secondo valore) per il modello 
    * cm sull'area area.
    * Verrà effettuata una ricerca su un campo stringa o numero
    * Utilizzando questo metodo NON saranno presi in considerazione i tipi documento 
    * su cui effettuare la ricerca attraverso il metodo
    * <a href="IQuery.html#addCodiceModello(java.lang.String, java.lang.String)">addCodiceModello()</a>,
    * il valore verrà ricercato solo nei campi del modello/area impostato su questo metodo.<BR>
    * Se il valore passato contiene il token % , l'operatore = viene automaticamente convertito
    * in LIKE
    * Il terzo parametro può essere utilizzato come operatore da applicare
    * alla ricerca sul campo oppure come secondo valore di confronto nel caso 
    * in cui si voglia specificare un intervallo di preferenza. Esempio:<BR>
    * <BR>
    * Iq.addCampo("N_REG", "1", ">", "SEGRETERIA", "M_PROTOCOLLO") ->  condizione sql: N_REG > 1<BR>
    * Iq.addCampo("N_REG", "1", "2", "SEGRETERIA", "M_PROTOCOLLO") ->  condizione sql: N_REG between 1 and 2<BR>
    * 
    * Verrà effettuato il mapping sul campo e sul cm passati
    * 
    * @param campo nome del campo su cui effettuare la ricerca
    * @param valore valore su cui effettuare la ricerca
    * @param operatore operatore o secondo valore. Valori possibili come operatore:<BR>
    * 		 >, <, <>, =
    * @param area Area sulla quale effettuare la ricerca
    * @param cm Codice Modello sul quale effettuare la ricerca
    * @see <a href="IQuery.html#addCampo(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)">addCampo(String, String, String, String, String)</a> 
   */   
   public void addCampo(String campo, String valore, String operatore, String area, String cm) {
  	      addCampoFT(campo,valore,operatore,area,cm,true,null);
   }
   
   /**
    * Metodo che si comporta come
    * <a href="IQuery.html#addCampo(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)">addCampo(String, String, String, String, String)</a>
    * sostituendo campo con una NVL(campo,valoreOption)
   */     
   public void addCampoNvl(String campo, String valoreCampoOption, String valore, String operatore, String area, String cm) {
  	      addCampoFT(campo,valore,operatore,area,cm,true,valoreCampoOption);
   }   
   
   /**
    * Metodo che richiama il funzionamento di
    * <a href="IQuery.html#addCampo(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)">addCampo(String, String, String, String, String)</a>.
    * L'unica differenza risiede nel fatto che, solo nel caso in cui venga richiamata la
    * ricercaFT in luogo della ricerca, l'interrogazione per il campo specificato in input 
    * verrà effettuata mediante intermedia sul campo clob.
    * 
    * @param campo nome del campo su cui effettuare la ricerca
    * @param valore valore su cui effettuare la ricerca
    * @param operatore operatore o secondo valore. Valori possibili come operatore:<BR>
    * 		 >, <, <>, =
    * @param area Area sulla quale effettuare la ricerca
    * @param cm Codice Modello sul quale effettuare la ricerca
    * @see <a href="IQuery.html#addCampoFT(java.lang.String, java.lang.String, java.lang.String, java.lang.String)">addCampo(String, String, String, String)</a> 
   */   
   public void addCampoFT(String campo, String valore, String operatore, String area, String cm) {
  	      addCampoFT(campo,valore,operatore,area,cm,false,null);
   }  
   
   /**
    * Metodo che si comporta come
    * <a href="IQuery.html#addCampoFT(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)">addCampoFT(String, String, String, String, String)</a>
    * sostituendo campo con una NVL(campo,valoreOption)
   */     
   public void addCampoFTNvl(String campo, String valoreCampoOption, String valore, String operatore, String area, String cm) {
  	      addCampoFT(campo,valore,operatore,area,cm,false,valoreCampoOption);
   }     
   
   private void addCampoFT(String campo, String valore, String operatore, String area, String cm, boolean ft, String valueNvl) {
	      keyval k = new keyval(campo,valore,null,operatore);

	      k.setArea(area);
	      k.setCm(cm);
	      k.setValueNvl(valueNvl);
	      k.setIsRicercaPuntuale(ft);
	      k.setNoCaseSensitive(bUseCaseNoSensitive);
	      
	      campiRicerca.add(k);  
   }
   
   /**
    * Metodo che aggiunge alla lista dei campi da ricercare
    * la coppia (campo,valore,operatore o secondo valore) per la categoria "categoria"
    * Verrà effettuata una ricerca su un campo stringa o numero
    * Utilizzando questo metodo NON saranno presi in considerazione i tipi documento 
    * su cui effettuare la ricerca attraverso il metodo
    * <a href="IQuery.html#addCodiceModello(java.lang.String, java.lang.String)">addCodiceModello()</a>,
    * il valore verrà ricercato solo nella categoria impostata su questo metodo.
    * Se il valore passato contiene il token % , l'operatore = viene automaticamente convertito
    * in LIKE 
    * Il terzo parametro può essere utilizzato come operatore da applicare
    * alla ricerca sul campo oppure come secondo valore di confronto nel caso
    * in cui si voglia specificare un intervallo di preferenza. Esempio:<BR>
    * <BR>
    * Iq.addCampo("N_REG", "1", ">", "PROT") ->  condizione sql: N_REG > 1<BR>
    * Iq.addCampo("N_REG", "1", "2", "PROTO") ->  condizione sql: N_REG between 1 and 2<BR>
    * 
    * @param campo nome del campo su cui effettuare la ricerca
    * @param valore valore su cui effettuare la ricerca
    * @param operatore operatore o secondo valore. Valori possibili come operatore:<BR>
    * 		 >, <, <>, =
    * @param categoria Categoria sulla quale effettuare la ricerca
    * @see <a href="IQuery.html#addCampoCategoria(java.lang.String, java.lang.String, java.lang.String)">addCampoCategoria(String, String, String)</a> 
   */   
   public void addCampoCategoria(String campo, String valore, String operatore, String categoria) {
	   	  addCampoCategoria(campo,valore,operatore,categoria,true,null);
   }
   
   /**
    * Metodo che si comporta come
    * <a href="IQuery.html#addCampoCategoria(java.lang.String, java.lang.String, java.lang.String, java.lang.String)">addCampoCategoria(String, String, String, String)</a>
    * sostituendo campo con una NVL(campo,valoreOption)
   */    
   public void addCampoCategoriaNvl(String campo, String valoreCampoOption, String valore, String operatore, String categoria) {
	   	  addCampoCategoria(campo,valore,operatore,categoria,true,valoreCampoOption);
   }   
   
   /**
    * Metodo che richiama il funzionamento di
    * <a href="IQuery.html#addCampoCategoria(java.lang.String, java.lang.String, java.lang.String, java.lang.String)">addCampo(String, String, String, String)</a>.
    * L'unica differenza risiede nel fatto che, solo nel caso in cui venga richiamata la
    * ricercaFT in luogo della ricerca, l'interrogazione per il campo specificato in input 
    * verrà effettuata mediante intermedia sul campo clob. 
    * 
    * @param campo nome del campo su cui effettuare la ricerca
    * @param valore valore su cui effettuare la ricerca
    * @param operatore operatore o secondo valore. Valori possibili come operatore:<BR>
    * 		 >, <, <>, =
    * @param categoria Categoria sulla quale effettuare la ricerca
    * @see <a href="IQuery.html#addCampoCategoria(java.lang.String, java.lang.String, java.lang.String)">addCampoCategoria(String, String, String)</a> 
   */   
   public void addCampoCategoriaFT(String campo, String valore, String operatore, String categoria) {
	   	  addCampoCategoria(campo,valore,operatore,categoria,false,null);
   }   
   
   /**
    * Metodo che si comporta come
    * <a href="IQuery.html#addCampoCategoriaFT(java.lang.String, java.lang.String, java.lang.String, java.lang.String)">addCampoCategoriaFT(String, String, String, String)</a>
    * sostituendo campo con una NVL(campo,valoreOption)
   */    
   public void addCampoCategoriaFTNvl(String campo, String valoreCampoOption, String valore, String operatore, String categoria) {
	   	  addCampoCategoria(campo,valore,operatore,categoria,true,valoreCampoOption);
   }   
   
   private void addCampoCategoria(String campo, String valore, String operatore, String categoria, boolean ft, String valueNvl) {
	      keyval k = new keyval(campo,valore,null,operatore);

	      k.setCategoria(categoria);	      
	      k.setIsRicercaPuntuale(ft);
	      k.setValueNvl(valueNvl);
	      k.setNoCaseSensitive(bUseCaseNoSensitive);
	      
	      campiRicerca.add(k);	
   }
   
   /**
    * Metodo che richiama il funzionamento di
    * <a href="IQuery.html#addCampo(java.lang.String, java.lang.String)">addCampo(String, String)</a>.
    * L'unica differenza risiede nel fatto che, solo nel caso in cui venga richiamata la
    * ricercaFT in luogo della ricerca, l'interrogazione per il campo specificato in input 
    * verrà effettuata mediante intermedia sul campo clob.
    * 
    * @param campo nome del campo su cui effettuare la ricerca    
    * @param valore valore su cui effettuare la ricerca oppure "is null" oppure "is not null"  
   */
   public void addCampoFT(String campo, String valore) {	   	  	   
	      keyval k = new keyval(campo,valore);
	      k.setIsRicercaPuntuale(false);
	      k.setNoCaseSensitive(bUseCaseNoSensitive);
	      
	      campiRicerca.add(k);
   }
   
   /**
    * Metodo che si comporta come
    * <a href="IQuery.html#addCampoFT(java.lang.String, java.lang.String)">addCampoFT(String, String)</a>
    * sostituendo campo con una NVL(campo,valoreOption)
   */   
   public void addCampoFTNvl(String campo, String valoreCampoOption, String valore) {	   	  	   
	      keyval k = new keyval(campo,valore);
	      k.setIsRicercaPuntuale(false);
	      k.setValueNvl(valoreCampoOption);
	      k.setNoCaseSensitive(bUseCaseNoSensitive);
	      
	      campiRicerca.add(k);
   }   
 
   /**
    * Metodo che richiama il funzionamento di
    * <a href="IQuery.html#addCampoFT(java.lang.String, java.lang.String, java.lang.String)">addCampo(String, String, String)</a>.
    * L'unica differenza risiede nel fatto che, solo nel caso in cui venga richiamata la
    * ricercaFT in luogo della ricerca, l'interrogazione per il campo specificato in input 
    * verrà effettuata mediante intermedia sul campo clob.
    * 
    * @param campo nome del campo su cui effettuare la ricerca    
    * @param valore valore su cui effettuare la ricerca oppure "is null" oppure "is not null"  
    * @param operatore l'unico operatore contemplato per adesso è il "<>" 
   */
   public void addCampoFT(String campo, String valore, String operatore) {	
	      keyval k = new keyval(campo,valore,null,operatore);	      
	      k.setIsRicercaPuntuale(false);
	      k.setNoCaseSensitive(bUseCaseNoSensitive);
	      
	      campiRicerca.add(k);
   }   
   
   /**
    * Metodo che si comporta come
    * <a href="IQuery.html#addCampoFT(java.lang.String, java.lang.String, java.lang.String)">addCampoFT(String, String, String)</a>
    * sostituendo campo con una NVL(campo,valoreOption)
   */    
   public void addCampoFTNvl(String campo, String valoreCampoOption, String valore, String operatore) {	
	      keyval k = new keyval(campo,valore,null,operatore);	      
	      k.setIsRicercaPuntuale(false);
	      k.setValueNvl(valoreCampoOption);
	      k.setNoCaseSensitive(bUseCaseNoSensitive);
	      
	      campiRicerca.add(k);
   }     

   /**
    * Metodo che aggiunge il codice modello passato in input
    * al filtro di ricerca.
    * 
    * Verrà effettuato il mapping sul cm
    * 
    * @param cm codice modello sul quale effettuare la ricerca  
    * @see <a href="IQuery.html#addCodiceModello(java.lang.String, java.lang.String)">addCodiceModello(String, String)</a>   
   */  
   public void addCodiceModello(String cm) {
	   	  addCodiceModello("",cm);
   }

   /**
    * Metodo che aggiunge la coppia (area,codice modello) passata in input
    * al filtro di ricerca.
    * 
    * Verrà effettuato il mapping sul cm
    * 
    * @param cm codice modello sul quale effettuare la ricerca  
    * @param area sulla quale effettuare la ricerca
    * @see <a href="IQuery.html#addCodiceModello(java.lang.String)">addCodiceModello(String)</a>   
   */     
   public void addCodiceModello(String area,String cm) {
	      cmRicerca.add(cm);
	      cmAreaRicerca.add(area);
   }

   /**
    * Metodo che aggiunge una condizione "and" alla ricerca. Esempio:<BR>
    * <BR> 
    * Iq.settaCondizioneAnd("SAMOGGIA MANNELLA");<BR>
    * In tal caso verrà eseguita una ricerca su TUTTI i valori (senza filtri sui campi)
    * utilizzando il motore di intermedia. Le parole cercate saranno "SAMOGGIA" e "MANNELLA"  
    * 
    * @param cond Condizione sulla quale effettuare la ricerca. Separare le parole con lo spazio!
    * @see <a href="IQuery.html#settaCondizioneOr(java.lang.String)">settaCondizioneOr(String)</a>   
   */   
   public void settaCondizioneAnd(String cond) {
          cAnd=cond;
   }

   /**
    * Metodo che aggiunge una condizione "or" alla ricerca. Esempio:<BR>
    * <BR> 
    * Iq.settaCondizioneOr("SAMOGGIA MANNELLA");<BR>
    * In tal caso verrà eseguita una ricerca su TUTTI i valori (senza filtri sui campi)
    * utilizzando il motore di intermedia. Le parole cercate saranno "SAMOGGIA" o "MANNELLA" 
    * 
    * @param cond Condizione sulla quale effettuare la ricerca. Separare le parole con lo spazio!
    * @see <a href="IQuery.html#settaCondizioneAnd(java.lang.String)">settaCondizioneAnd(String)</a>   
   */   
   public void settaCondizioneOr(String cond) {
          cOr=cond;
   }   

   /**
    * @deprecated
   */
   public void settaChiave(String key,String val) {
          //campiRicerca.add(new keyval(key,val));    
	   	  addCampo(key,val);
   }
   
   /**
    * @deprecated
   */
   public void settaChiave(String tipoDoc,String key,String val) {
          //campiRicerca.add(new keyval(key,val,tipoDoc));
	   	  addCampo(key,val);
	   	  addCodiceModello(tipoDoc);
   }  
   
   /**
    * @deprecated
   */
   public void settaChiave(String tipoDoc,String key,String val,String op) {
          //campiRicerca.add(new keyval(key,val,tipoDoc,op));
	      addCampo(key,val,op);
	      addCodiceModello(tipoDoc);
   }      

   /**
    * @deprecated
   */
   public void settaDato(String dato, String valore) {
          //datiRicerca.add(new keyval(dato,valore));
	      addCampo(dato,valore);
   }

   /**
    * @deprecated
   */   
   public void settaDato(String dato, String valore, String op) {
          //datiRicerca.add(new keyval(dato,valore,null,op));
	      addCampo(dato,valore,op);
   }

   /**
    * @deprecated
   */   
   public void settaCondizioneNot(String cond) {
          cNot=cond;
   }

   /**
    * @deprecated
   */   
   public void settaCondizioneSingle(String cond) {
          cSingle=cond;
   }
  
   /**
    * Metodo che aggiunge un utente o un gruppo al filtro di ricerca.<BR>
    * Metodo di integrazione con <B>HUMMINGBIRD</B> 
    * 
    * @param user Utente o Gruppo
    * @param isUserOrGroup Valori possibili:<BR>
    * 		 Global.IS_USER, Global.IS_GROUP
    * @see <a href="IQuery.html#settaUtente(java.lang.String)">settaUtente(String)</a>   
   */   
   public void settaUtente(String user,String isUserOrGroup) {          
          usersFilters.add(user);
          groupFilters.add(isUserOrGroup);
   }

   /**
    * Metodo che aggiunge un utente al filtro di ricerca.<BR>
    * Metodo di integrazione con <B>HUMMINGBIRD</B> 
    * 
    * @param user Utente
    * @see <a href="IQuery.html#settaUtente(java.lang.String, java.lang.String)">settaUtente(String, String)</a>   
   */   
   public void settaUtente(String user) {          
          usersFilters.add(user);
          groupFilters.add(Global.IS_USER);
   }

   /**
    * Scelta della dimensione di fetch dei risultati
    * della ricerca. Esempio:<BR><BR>
    * 
    * <B>Iq.setFetchSize(50);<BR><BR></B>
    * 
    * Restituirà solo i primi 50 risultati dalla ricerca 
    * 
    * @param size dimensione di fetch
    * @see <a href="IQuery.html#setFetchInit(int)">setFetchInit(int)</a>     
   */   
   public void setFetchSize(int size) {
          fetchSize=size;
   }

   /**
    * Scelta dei tipi di documento da restituire 
    * dopo una ricerca effettuata a partire da
    * categorie/modelli divisi su più campi (ricerca avanzata)
    * 
    * @param area
    * @param cm
    * @see <a href="IQuery.html#setTypeModelReturn(java.lang.String)">setTypeModelReturn(String)</a>     
   */   
   public void setTypeModelReturn(String area, String cm) {
	      areaIdRicercaReturn=area;
	      cmIdRicercaReturn=cm;
   }

   /**
    * Scelta dei tipi di documento da restituire 
    * dopo una ricerca effettuata a partire da
    * categorie/modelli divisi su più campi (ricerca avanzata)
    * 
    * @param categoria
    * @see <a href="IQuery.html#setTypeModelReturn(java.lang.String, java.lang.String)">setTypeModelReturn(String, String)</a>     
   */      
   public void setTypeModelReturn(String categoria) {
	      categoriaIdRicercaReturn=categoria;	      
   }   

   /**
    * Scelta del record da cui partire a restituire
    * i risultati della ricerca. Esempio:<BR><BR>
    * 
    * <B>Iq.setFetchInit(10);<BR><BR></B>
    * 
    * I risultati della ricerca partiranno dal 10° record
    * 
    * @param init Record da cui partire
    * @see <a href="IQuery.html#setFetchSize(int)">setFetchSize(int)</a>     
   */   
   public void setFetchInit(int init) {
          fetchInit=init;
   }
   
   public boolean isLastRowFetch() {
          return bIsLastRow;
   }
   
   public void setRicercaAnchePreBozza() {
	      bAnchePreBozza=true;
   }
   
   /**
    * Metodo svuota il vettore dei profili riempito
    * in seguito ad una ricerca  
   */   
   public void svuota() {
          profili.clear();
   }      
   
   /**
    * Meotodo che restituisce l'HTML della WorkArea del client
    * successiva ad una ricerca
   */
   public String getHtmlWorkArea(HttpServletRequest req,String pathImage,String NPAGE_SIZE,boolean icona) throws Exception {
          String idQuery=identifierQuery;
          String user=en.getUser();
          String WhereFullText="";
          String lista="";
          String parametro="";
          int PAGE_SIZE=Integer.parseInt(NPAGE_SIZE);
                 
          try {
            en.connect();
         
            // Preparazione della ServletModulistica      
            String url1=req.getServletPath();
            String url2=req.getRequestURL().toString();
            String urls=url2.substring(0,url2.indexOf(url1));
            // Bisogna cambiare e passare a common 
            //String url_servlet=urls+"/restrict/ServletModulisticaWindow.do?";
            String url_servlet=urls+"/restrict/ServletModulisticaDocumento.do?";
             
            //Ricostruzione dell'URL 
            String url=req.getRequestURL().toString();
            url+="?"+req.getQueryString();
            // Recupero parametro LinksPage
            parametro=req.getParameter("LINKSPage");
            if (parametro == null) {
               parametro="1";
               url+="&LINKSPage=1";
            }
            // Eliminazione del parametro LinksPage
            url=url.substring(0,url.indexOf("LINKSPage"));
       
            // Bisogna considerare il caso di query parametriche??? NO
     	    GD4_Gestione_Query q = new GD4_Gestione_Query(Integer.parseInt(idQuery),en);   
     	    
     	   
     	    //Non funziona con Enviroment ma soltanto passando il datasource fisso
          	//CCS_WorkArea workarea= new CCS_WorkArea(idQuery,(GD4_Gestione_Query)q,WhereFullText,parametro,pathImage,url,PAGE_SIZE,icona,url_servlet,new CCS_Common(en,user));
     	    CCS_WorkArea workarea= new CCS_WorkArea(idQuery,(GD4_Gestione_Query)q,WhereFullText,"",parametro,pathImage,url,PAGE_SIZE,icona,url_servlet,new CCS_Common("jdbc/gdm",user));
			
     	    lista = workarea._afterInitialize();
                     
            en.disconnectClose();
          
            return lista;
          }
          catch (Exception e) {   
        	  e.printStackTrace();
            try{en.disconnectClose();}catch(Exception ei){}
            throw new Exception("IQuery::getHtmlWorkArea()\n" + e.getMessage());
          }  
   }       
   
   /**
    * Metodo che restituisce lo stato di una query
    * i valori possibili sono:<BR>
    * 				  	Global.STATO_BOZZA<BR>
	*					Global.STATO_COMPLETO<BR>
	*					Global.STATO_ANNULLATO<BR>
	*					Global.STATO_CANCELLATO<BR>
    * 
   */   
   public String getStato() throws Exception {
           String sStato=null;
           IDbOperationSQL dbOp = null;         
          
           if (identifierQuery==null)
              throw new Exception("IQuery::getStato - "+
                                  "Impossibile recuperare lo stato della query se idQuery è nullo");
                                 
              try {
                StringBuffer sStm = new StringBuffer();
          
                en.connect();
                dbOp = en.getDbOp();

                sStm.append("select documenti.stato_documento ");
                sStm.append("from query,documenti ");
                sStm.append("where id_query= "+identifierQuery+" and id_documento_profilo=documenti.id_documento");

                dbOp.setStatement(sStm.toString());
                dbOp.execute();  

                ResultSet rst = dbOp.getRstSet();

                if ( rst.next() ) 
                   sStato = rst.getString(1);               
                else {
                   throw new Exception("IQuery::getStato - "+
                                       "Errore nel recupero dello stato della query da idQuery");     
                }
               
                en.disconnectClose();
                
                return sStato;
               
              }
              catch (Exception e) {               
                en.disconnectClose();
                throw new Exception("IQuery::getStato()\n" + e.getMessage());
              }               
   }      
   
   /**
    * Metodo che provvede alla cancellazione della query.
    * Prima di eliminare il metodo verifica se si possiedono
    * le competenze di cancellazione. 
   */
   public void delete() throws Exception {                 
         //0) Verifica della competenza di delete della cartella
         if (!verificaCompetenzaQuery(identifierQuery,Global.ABIL_CANC)) {
            String sMessaggio ="Non si possiedono le competenze ";
                   sMessaggio+="di delete sulla query";
            throw new Exception("IQuery::delete - "+sMessaggio);
         }
         
         //1) Procedo all'eliminazione attraverso l'apposita function
         //   F_ELIMINA_QUERY.
         IDbOperationSQL dbOpSQL = null;
                           
         try {
           en.connect();
           dbOpSQL = en.getDbOp();                                           
              
           dbOpSQL.setCallFunc("F_Elimina_Query("+identifierQuery+")");
           dbOpSQL.execute();
              
           if ( dbOpSQL.getCallSql().getInt(1)==0 ) {                  
                throw new Exception("IQuery::delete Errore function F_Elimina_Query ");
           }
              
           en.disconnectCommit();
         }
         catch (Exception e) {
              en.disconnectRollback();
              throw new Exception("IQuery::delete "+e.getMessage());
         }
   }

   /**
    * Inserisce la competenze ACL sulla IQuery
   */
   private void salvaACL(String tipoSalvataggio) throws Exception {
           //Ciclo sulle ACL
           int size = ACLuser.size();
          
           if (size==0) return;
          
           //Se sono in modifica del documento, prima di modificare le competenze
           //controllo se ho la comp. di manage
           if (tipoSalvataggio.equals("U")) {
              UtenteAbilitazione env = new UtenteAbilitazione( en.getUser(), en.getPwd(),null,null);								
              Abilitazioni ab = new Abilitazioni("QUERY",identifierQuery,Global.ABIL_GEST);				

              if ((new GDM_Competenze(en)).verifica_GDM_Compentenza(env,ab)==0) {
                 throw new Exception("ICartella::salvaACL() Impossibile aggiornare le competenze, non si possiede la competenza per modificarle");
              }
           }
          
           for(int i=0;i<size;i++) {  
              String user,key, sComp="", sCompDoc="", sCompNega="", sCompDocNega="";
                
              user=(String)ACLuser.elementAt(i);
              key=(String)ACLtype.elementAt(i);
             
              if (key.equals(Global.NO_ACCESS)) {
                 sComp="";
                 sCompDoc="";
                 sCompNega="LUCDM";
                 sCompDocNega="LUDM";
              }
              else if (key.equals(Global.COMPLETE_ACCESS)) {
                 sComp="LUCDM";
                 sCompDoc="LUDM";
                 sCompNega="";
                 sCompDocNega="";
              }
              else if (key.equals(Global.NORMAL_ACCESS)) {
                 sComp="LU";
                 sCompDoc="LU";
                 sCompNega="CD";
                 sCompDocNega="D";               
              }
              else if (key.equals(Global.READONLY_ACCESS)) {
                 sComp="L";
                 sCompDoc="L";
                 sCompNega="CUD";
                 sCompDocNega="UD"; 
              }    
              else if (key.equals(Global.INFOLDER_ACCESS)) {
                 sComp="C";
                 sCompDoc="";
                 sCompNega="";
                 sCompDocNega=""; 
              }   
                        
              try {
                UtenteAbilitazione env = new UtenteAbilitazione( user, user,null,null);								

                if (!sCompDoc.equals("")) {                
                   Abilitazioni ab = new Abilitazioni("DOCUMENTI",profileQuery,sCompDoc);				
                   (new GDM_Competenze(en)).assegnaCompentenza(env,ab);	
                }
               
                if (!sComp.equals("")) {
                   Abilitazioni ab = new Abilitazioni("QUERY",identifierQuery,sComp);							
                   (new GDM_Competenze(en)).assegnaCompentenza(env,ab);		
                }
               
                if (!sCompDocNega.equals("")) {                
                   Abilitazioni ab = new Abilitazioni("DOCUMENTI",profileQuery,sCompDocNega);				
                   ab.setAccesso("N");
                   (new GDM_Competenze(en)).assegnaCompentenza(env,ab);	
                }

                if (!sCompNega.equals("")) {                
                   Abilitazioni ab = new Abilitazioni("QUERY",identifierQuery,sCompNega);				
                   ab.setAccesso("N");
                   (new GDM_Competenze(en)).assegnaCompentenza(env,ab);	
               }               
             }
             catch (Exception e) {
               throw new Exception("ICartella::salvaACL() per (maschera,utente) ("+key+","+user+")\n" + e.getMessage());
             }             
          }
   }   
   
   /**
    * Metodo che provvede all'inserimento di una query.
    * Vengono eseguiti i seguenti passi:<BR> 
    *               1) Creazione del profilo-query, se questo già non esiste,
    *                  a partire dai valori passati in input<BR>
    *               2) Creazione della query<BR>
    *               3) Inserimento delle competenze al Profilo-Query ed alla Query  
   */  
   public void insert() throws Exception {                  
          IDbOperationSQL dbOp = null;
          try {
           
            en.connect();
            dbOp = en.getDbOp();

            //Inserimento del profilo (passo 1)
            if (profileQuery==null) 
               insertProfilo_Query(dbOp);            

            //Non vengo dal web caso ricerca MODULISTICA 
            if (filtro==null || (filtro.indexOf("RICERCAMODULISTICA_")==-1)) {
	            prepareXMLFilter();            
	            filtro=xmlf.doFilter();
            }
            
            //Inserimento della query (passo 2)     
            insertQuery(dbOp);

            //Assegnamento delle competenze a Profilo e Query (passo 3)   
            assegnaCompetenzeQuery(dbOp);
                       
            try {         
              salvaACL("I");
            }
            catch (Exception e) {
              throw new Exception("Insert - salvaACL\n"+e.getMessage());
            }                      

            en.disconnectCommit();           
          }
          catch (Exception e) {            
            try{en.disconnectRollback();}catch(Exception ei){}
            throw new Exception("IQuery::insert()\n" + e.getMessage());
          }
   }

   /**
    * Metodo che provvede all'aggiornamento di una query.
    * Vengono eseguiti i seguenti passi:<BR> 
    *               1) Verifica della competenza di modifica sulla query<BR>
    *               2) Aggiornamento dei valori del profilo<BR>
    *               3) Aggiornamento del nome query sulla tabella query in 
    *                  funzione del nuovo valore sul profilo  
   */
   public void update() throws Exception {      
          //1) Verifica della competenza di modifica della query
          if (!verificaCompetenzaQuery(identifierQuery,Global.ABIL_MODI)) {
             String sMessaggio ="Non si possiedono le competenze ";
                   sMessaggio+="di aggiornamento sulla Query";
             throw new Exception("IQuery::update - "+sMessaggio);
          }
         
          IDbOperationSQL dbOp = null;
         
          try {
            en.connect();
            dbOp = en.getDbOp();
            boolean wasNull=false;
             
            try {
               //Se la Global.CONNECTION è nulla,
               //significa che sto gestendo la connessione
               //sulla IQuery dall'interno.
               //Sull'AggiungiDocumento la gestirò comunque
               //esterna.....
               if (profili.size()!=0) {
	               if (en.Global.CONNECTION==null) {
	                   wasNull=true;
	                   en.setExternalConnection(dbOp.getConn());
	               }
	   
	               //Parte 2) - Aggiornamento dei valori del profilo
	               for(int i=0;i<profili.size();i++) {
	                    AggiornaDocumento ad = new AggiornaDocumento(profileQuery,en);
	               
	                    ad.aggiornaDati(((Valori)profili.get(i)).key,((Valori)profili.get(i)).value);
	      
	                    ad.salvaDocumento();
	               }
	               
	               //....qui la risetto a null nel caso in cui lo era
	               if (wasNull) {
	                  en.resetExternalConnection();
	                  en.setDbOp(dbOp);
	               }
               }

            }
            catch (Exception e) {
                        //....qui la risetto a null nel caso in cui lo era
               if (wasNull) {
                    en.resetExternalConnection();                    
                    en.setDbOp(dbOp);
               }   
               throw new Exception("AggiornaDocumento::aggiornaDati\n"+e.getMessage());
            }
           
            //Parte 3) Aggiornamento del nome query
            try {             
               for(int i=0;i<profili.size();i++) 
                   if ((((Valori)profili.get(i)).key).equals("NOME"))    
                       updateNameQuery(""+((Valori)profili.get(i)).value,dbOp);
            }
            catch (Exception e) {
               throw new Exception("insertDocumentInQuery\n"+e.getMessage());
            }      
      
            if (bModifyFilterInUpdate) {
	            if (!changefiltro) {
	              prepareXMLFilter();            
	              filtro=xmlf.doFilter();
	            }
	           
	            updateFiltroQuery(filtro,dbOp);            
            }
                                  
            try {         
              salvaACL("U");
            }
            catch (Exception e) {
              throw new Exception("Update - salvaACL\n"+e.getMessage());
            }
            
            en.disconnectCommit();      
         }
         catch (Exception e) {              	
           en.disconnectRollback();      
           throw new Exception("IQuery::update\n"+e.getMessage());
         }    
   }

   /**
    * Metodo che forza l'aggiornamento degli indici
    * di intermedia.
    * Da utilizzare prima di lanciare una ricercaFT
    * se non si è sicuri di aver indicizzato una serie
    * di valori precedentemente inseriti
   */   
   public void execRebuildIndex() throws Exception {
  	      try {
  		    en.connect();
  		    (new ManageConnection(en.Global)).rt_rebuild_index(en.getDbOp()); 
  		    try{en.disconnectClose();}catch(Exception ei){}
          }    
          catch (Exception e) {
    	    try{en.disconnectClose();}catch(Exception ei){}
            throw new Exception("IQuery::execRebuildIndex() "+ e.getMessage());
          }     
   }
   
   public void escludiControlloCompetenze(boolean bFlag) {
   		  bEscludiControlloCompetenze=bFlag;
   }   

   public void escludiOrdinamento(boolean bFlag) {
   		  bEscludiOrdinamento=bFlag;
   }  

   /**
    * Se il parametro è messo a true e nel caso in cui 
    * il documento ricercato non venga trovato
    * con le competenze su se stesso, viene lanciata la competenza 
    * sul suo id_documento_padre 
    * (se id_documento_padre risulta valorizzato)
    * 
    * @param bFlag
   */   
   public void controllaPadre(boolean bFlag) {
	   	  bControllaPadre=bFlag;
   }
      
   /**
    * Metodo che effettua la ricerca abilitando la ricerca intermedia
    * sui campi in cui si è richamata la 
    * <a href="IQuery.html#addCampoFT(java.lang.String, java.lang.String)">addCampoFT(String, String)</a>
    * 
    * @return true - La ricerca è andata a buon fine, false altrimenti
    * @see <a href="IQuery.html#ricerca()">ricerca()</a>  
   */   
   public Boolean ricercaFT() {
	      bIsRicercaPuntuale=false;
	      return ricerca();
   }
   

   public String getSqlQuery() throws Exception {
	      if (ric==null) return null;
	     
	      return ric.getLastQueryExecuted();
   }

   public String getSqlCountQuery() throws Exception {
	   	  if (ric==null) return null;
	   	  
	   	  if (ric.getLastQueryExecuted()==null) return null;
    
	   	  return "Select count(*) from ("+ric.getLastQueryExecuted()+")";
   }

   /**
    * Metodo che effettua la ricerca
    * 
    * @return true - La ricerca è andata a buon fine, false altrimenti
    * @see <a href="IQuery.html#ricercaFT()">ricercaFT()</a>  
   */   
   public Boolean ricerca() {            
          try {          
              //FINMATICA IMPLEMENTATION
              if (en.Global.DM.equals(en.Global.FINMATICA_DM)) {
                 try {                 	
                   int size;
                   Vector l;
                                                                           
                   if (sqlSelect!=null) {
                	   ric = new HorizontalSearch("",(new Vector()),(new Vector()),en);
                	   ric.setSqlCollectionIQuerySelect(sqlSelect);
                   }                
                   else {
                	   try {
                           ric=getRicercaFinmatica();
                           if (abilitaQueryServiceLimit && queryServiceLimit > 0) {
                               ric.setQueryServiceLimit(queryServiceLimit);
                           }
                       }
                       catch (Exception e) {
                  	       error=e.getMessage();
                  	       if (error.indexOf("alcun risultato")!=-1)
                  		   stateExcecuteQuery=STATEEX_QUERY_NORESULT;
                           return new Boolean(false);
                       }
                     
                       if (ric==null) return new Boolean(true);
                   }
                   
                   try {
                	 en.connect();
                	 if (ric instanceof HorizontalSearch)
	                	 log4jSuiteDb = new LogDb(en.getUser(),                   
	                			                  en.Global.CATEGO_RICERCA_SEMPLICE_HORIZ,
	                			                  en.getDbOp().getConn());
                	 else
	                	 log4jSuiteDb = new LogDb(en.getUser(),                   
	                			                  en.Global.CATEGO_RICERCA_SEMPLICE_VERT,
	                			                  en.getDbOp().getConn());                		                 	                 	                 	
                	 
                	 ric.setLog4JSuite(log4jSuiteDb);
                	 en.disconnectClose();
                   }                                       
                   catch(Exception e) {         
                	 throw new Exception("Errore creazione LOG\n"+e.getMessage());	 
                   }
                   
                   try {           
                	 
                	 ric.ricerca();
                
                   }
                   catch(Exception e) {                     	 
                	   if (e.getMessage().indexOf("ORA-01013")!=-1) {
                		   log4jSuiteDb.ScriviLog("Esecuzione Ricerca","Tempo max di esecuzione della query raggiunto!",
                				                  Global.TAG_RICERCA_SEMPLICE_TIMEOUTSQL,
                				                  LogDb.ERROR_LEVEL);
                		   error="Tempo max di esecuzione della query raggiunto!";
                    	   stateExcecuteQuery=STATEEX_QUERY_TIMEOUT;
                    	   e.printStackTrace();
                    	   return new Boolean(false);
                	   }
                	   try {en.disconnectClose();}catch(Exception ei){}  
                	   log4jSuiteDb.ScriviLog("Esecuzione Ricerca",e.getMessage(),
                				              Global.TAG_RICERCA_SEMPLICE_GENERRORSQL,
                				              LogDb.ERROR_LEVEL);                	   
                	   throw new Exception(e.getMessage());	 
                	 }
                
                	 l=ric.getDocumentList();
                	 vCampiReturnCursor=ric.getVAliasCampiReturn();                                                                                   
                		
                     size = l.size();
                     
                     bIsLastRow=ric.isLastRowFetch();
                     
                     //Testo se ha tornato almeno un profilo dalla ricerca
                     if (size==0) {   
                       if (bQueryTimeOut) {
                		   log4jSuiteDb.ScriviLog("Esecuzione Ricerca","Tempo max di esecuzione della query raggiunto!",
                				                  Global.TAG_RICERCA_SEMPLICE_TIMEOUTSQL,
                				                  LogDb.ERROR_LEVEL);                    	   
                    	   error="Tempo max di esecuzione della query raggiunto!";
                    	   stateExcecuteQuery=STATEEX_QUERY_TIMEOUT;
                       }
                       else {
                		   log4jSuiteDb.ScriviLog("Esecuzione Ricerca","La ricerca non ha prodotto alcun risultato",
                				                  Global.TAG_RICERCA_SEMPLICE_NOROWSSQL,
                				                  LogDb.ERROR_LEVEL);                    	   
                    	   error="La ricerca non ha prodotto alcun risultato";
                    	   stateExcecuteQuery=STATEEX_QUERY_NORESULT;
                       }

                       return new Boolean(false);
                     }                   
		                                  
                     for(int i=0;i<size;i++) {
                    	 addProfiloToRicercaVector(""+l.get(i));                   
                     }   
                                            
                 }
                 catch (Exception e) {
                	
                   error="IQuery::ricerca()\n"+e.getMessage();
                   return new Boolean(false);
                 }
              }
              //HUMM IMPLEMENTATION
              else {
                 RicercaDocumento rd = new RicercaDocumento(cmQuery, area,en);
  
                 int size = campiRicerca.size();
                 for(int i=0;i<size;i++) 
                     rd.settaChiavi(((keyval)campiRicerca.elementAt(i)).getKey(),
                                   ((keyval)campiRicerca.elementAt(i)).getVal());
  
                 size = usersFilters.size();
                 for(int i=0;i<size;i++) 
                     rd.aggiungiUtente( (String)(usersFilters.get(i)), (String)(groupFilters.get(i)) );
  
                 Vector l;
              
                 l = rd.ricerca();
              
                 String sXmlRet=(String)l.get(0);

                 parseXmlProfGet(sXmlRet);

                 //Testo se ha tornato almeno un profilo dalla ricerca
                 if (profili.size()==0) {
                    error="La ricerca non ha prodotto alcun risultato";    
                    return new Boolean(false);
                 }
              }              
              
              return new Boolean(true);
          }
          catch (Exception e) {
        	 
              error="IQuery::ricerca()\n"+e.getMessage();
              return new Boolean(false);
         }
   }

    public String getTipoRicercaDefault() {
        return tipoRicercaDefault;
    }

    public void setTipoRicercaDefault(String tipoRicercaDefault) {
        this.tipoRicercaDefault = tipoRicercaDefault;
    }

    public AbstractSearch getRicercaFinmatica() throws Exception {
	      AbstractSearch ric=null;
	      
          String miaArea;
           
          if (areaRicerca==null)
        	  miaArea=area;
          else
        	  miaArea=areaRicerca;

	       //Sto cercando con chiave CR
	       //restituisco lo stesso idDoc dentro il vettore
	       //di risultati (che stavolta contengono un int)
	       if (cmQuery!=null && miaArea!=null && crSingleDocSearch!=null) {                	                   	                   	                   	                   	  
	        	   if (profileQuery!=null)
        		  addProfiloToRicercaVector(""+profileQuery);
        	   else
        		  addProfiloToRicercaVector(""+(new DocUtil(en)).getIdDocumentoByAreaCmCr(miaArea, cmQuery, crSingleDocSearch));
        		   
        	   return null;//new Boolean(true);
           }
                             
           /*if (identifierQuery!=null) {
        	   leggiDBQuery();
           }*/                                      
           
           // Se è diverso da null nel caso in cui viene invocato
           // il costruttore IQuery(String cmProfileQuery, String areaProfileQuery)
           // viene aggiunto al vettore dei codici modelli
           if(cmQuery!=null)
              cmRicerca.add(cmQuery);           
           if (area!=null) 
        	  cmAreaRicerca.add(miaArea);                 
         
           if (bEscludiControlloCompetenze)
        	   en.byPassCompetenzeON();
                                        
           int tipoRicerca;
         
           if (idDocToRicerca.size()>0) {
        	   tipoRicerca=(new LookUpDMTable(en)).lookUpTipoRicercaHorV(miaArea,cmRicerca,cmAreaRicerca,campiRicerca,campiOrdinamento,areaIdRicercaReturn,cmIdRicercaReturn,categoriaIdRicercaReturn);
        	   if (miaArea==null && tipoRicerca==-2) tipoRicerca=1;
           }
           else {
               if (tipoRicercaDefault!=null) {
                   if (tipoRicercaDefault.equals("H"))
                        tipoRicerca=1;
                   else
                       tipoRicerca=(new LookUpDMTable(en)).lookUpTipoRicercaHorV(miaArea,cmRicerca,cmAreaRicerca,campiRicerca,campiOrdinamento,null,null,null);
               }
               else {
                   tipoRicerca=(new LookUpDMTable(en)).lookUpTipoRicercaHorV(miaArea,cmRicerca,cmAreaRicerca,campiRicerca,campiOrdinamento,null,null,null);
               }
           }

           if (tipoRicerca==1)
        	   	ric = new HorizontalSearch(miaArea,cmRicerca,cmAreaRicerca,en);
           else if (tipoRicerca==0)
        	    ric = new RicercaFinmatica(miaArea,cmRicerca,cmAreaRicerca,en);
           //Caso solo area e campi anonimi (non cm)
           else if (tipoRicerca==-2) {                	   
        	    //Mi calcolo i modelli dell'area e li inserisco
        	    Vector vCm = (new LookUpDMTable(en)).lookUpElencoCodiciModelliOrizzontaliByArea(miaArea);
        	    
        	    if (vCm.size()==0) {            	  
            	   throw new Exception("La ricerca non ha prodotto alcun risultato");            	   
        	    }
        	    
        	    for (int i=0;i<vCm.size();i++)
        	    	this.addCodiceModello(miaArea,""+vCm.get(i));
        	    
        	    ric = new HorizontalSearch(miaArea,cmRicerca,cmAreaRicerca,en);
           }
           else {        	                       	  
               throw new Exception("I modelli inseriti in ricerca non sono compatibili tra di loro.\nInserire o tutti modelli orizzontali o tutti verticali!");  
           }
        
           if (identifierQuery!=null) {
        	   ric.setRicercaWeb(true);
           }                  
           
           ric.setMaster(bIsMaster);
           
           ric.setFetchInit(fetchInit);
           ric.setFetchSize(fetchSize);
           
           ric.setCampi(campiRicerca);
           ric.setIsRicercaPuntuale(bIsRicercaPuntuale);
           ric.setControllaPadre(bControllaPadre);
           ric.setCampiOrdinamento(campiOrdinamento);
           ric.setCondizioneAnd(cAnd);
           ric.setCondizioneOr(cOr);
           ric.setCondizioneNot(cNot);
           ric.setCondizioneFullText(cSingle);                     
           ric.setTypeModelReturn(areaIdRicercaReturn, cmIdRicercaReturn);
           ric.setTypeModelReturn(categoriaIdRicercaReturn);
           ric.setIdDocumento(idDocToRicerca);
           ric.setObjFileCondition(ogfiRicerca);
           ric.setEscludiOrdinamento(bEscludiOrdinamento);
           ric.setExtraConditionSearch(extraConditionSearch);
           ric.setControlloCompetenzaQuery(controlloCompetenzaQuery);
           
           ric.setTimeOut((int)(queryTimeOut/1000));	 
           
           ric.setBTrovaAnchePreBozza(bAnchePreBozza);
	      
	       return ric;
   }
   
   private void addProfiloToRicercaVector(String id) throws Exception {	   
           try {
		   	   Profilo pApp=null;
	           
		       if (instanceProfileAfterSearch) {
			       pApp = createDocumento(id);//new Profilo(""+l.get(i));
		           pApp.setAccessCmCrArea(false);
		           
		           pApp.escludiControlloCompetenze(bEscludiControlloCompetenze);
		           pApp.initVarEnv(en);
		                          
		           if (accessProfileAfterSearch) pApp.accedi("0");
		       }
		       
	           profili.add(pApp);
           }
           catch(Exception e) {
        	   throw new Exception("IQuery::addProfiloToRicercaVector - "+e.getMessage());
           }
   }
    
   /**
    * Inserisce il profilo della query
    *
   */  
   private void insertProfilo_Query(IDbOperationSQL dbOp) throws Exception {
           // 1) Genero dalla sequence il numero di query che poi diventerà
           //    il codice richiesta del documento = - <valore_seq>
           //    ATTENZIONE: Identifichiamo il codice richiesta Query da quello
           //    delle Cartelle mediante il segno negativo.
           String cr;
           try {            
             identifierQuery=""+dbOp.getNextKeyFromSequence("QRY_SQ");
             cr="-"+identifierQuery;          
           }
           catch (Exception e) {            
             throw new Exception("IQuery::insertProfilo_Query()- Errore generazione sequence query\n" + e.getMessage());
           }         
   
           // 2) Inserisco il profilo a partire dai valori e obbligo ad inserire
           //    il nome della query         
           
           boolean wasNull=false;
           try {
             //Mi creo un environment di appoggio (privato) dove
             //specifico una connessione esterna dalla dbOp.
             //In questo modo evito che la AggiungiDocumento
             //faccia una propria commit.
             //Il commit invece lo gestisco io alla fine della
             //insert
            
             //Se la Global.CONNECTION è nulla,
             //significa che sto gestendo la connessione
             //sulla IQuery dall'interno.
             //Sull'AggiungiDocumento la gestirò comunque
             //esterna.....
                            
             if (en.Global.CONNECTION==null) {
                 wasNull=true;
                 en.setExternalConnection(dbOp.getConn());
             }
             
             AggiungiDocumento ad = new AggiungiDocumento(cmQuery,area,en);
           
             for(int i=0;i<profili.size();i++) {
                if ((((Valori)profili.get(i)).key).equals("NOME")) 
                    nameQuery=""+((Valori)profili.get(i)).value;      

                ad.aggiungiDati(((Valori)profili.get(i)).key,
                                ((Valori)profili.get(i)).value);
             }
         
             if (nameQuery==null) 
                throw new Exception("IQuery::insertProfilo_Query - Nome Query Obbligatorio");                     
           
             ad.settaCodiceRichiesta(cr);
             insertCodiceRichiesta(cr,dbOp);
            
             ad.salvaDocumentoBozza();
            
             profileQuery=ad.aDocumento.getIdDocumento();
            
             //....qui la risetto a null nel caso in cui lo era
             if (wasNull) {
                 en.resetExternalConnection();
                 en.setDbOp(dbOp);
             }                        
         
           }
           catch (Exception e) {       
        	   e.printStackTrace();
             //....qui la risetto a null nel caso in cui lo era
             if (wasNull) {
                en.resetExternalConnection();
                en.setDbOp(dbOp);
             }   
   
             throw new Exception("IQuery::insertProfilo_Query - AggiungiDocumento\n"+e.getMessage());
           }
   }  
 
   /**
    * Inserisce la query
    *
   */   
   private void insertQuery(IDbOperationSQL dbOp) throws Exception {         
           StringBuffer sStm = new StringBuffer();

           sStm.append("insert into query (ID_QUERY, NOME, TIPO, FILTRO, ID_DOCUMENTO_PROFILO");
           sStm.append(",DATA_AGGIORNAMENTO,UTENTE_AGGIORNAMENTO) ");
           sStm.append("values (" + identifierQuery + ",");
 		   sStm.append(" :NAME ,");
           sStm.append("'"+rootQuery+"',");         
		   sStm.append(" :FILTRO ,"+profileQuery+",");
		   sStm.append("sysdate,");
   		   sStm.append("'" + en.getUser() + "')");

           dbOp.setStatement(sStm.toString());           
           dbOp.setParameter(":NAME", nameQuery);  
           dbOp.setParameter(":FILTRO", filtro); 
 
           try {
  			 dbOp.execute();
           }
           catch (Exception e) {
             throw new Exception("IQuery::insertQuery - Insert Query\n"+e.getMessage());
           }
   }
  
   /**
    * Aggiorna il filtro della query
    * nella tabella query
   */  
   private void updateFiltroQuery(String name,IDbOperationSQL dbOp) throws Exception {         
           try {          

             StringBuffer sStm = new StringBuffer();

             sStm.append("UPDATE QUERY ");
 		     //sStm.append("SET FILTRO='" + name + "' "+ ",");
             sStm.append("SET FILTRO=:NAME, ");
			 sStm.append("DATA_AGGIORNAMENTO=sysdate,");
             sStm.append("UTENTE_AGGIORNAMENTO='"+en.getUser()+ "'");
			 sStm.append("where id_query=" + identifierQuery);

             dbOp.setStatement(sStm.toString());
             dbOp.setParameter(":NAME", name);
             dbOp.execute();
           
           }
           catch (Exception e) {          
        	   e.printStackTrace();
             throw new Exception("IQuery::updateFiltroQuery\n" + e.getMessage());           
           }
   }
   
   /**
   * Aggiorna il nome della query
   * nella tabella query
   */  
   private void updateNameQuery(String name,IDbOperationSQL dbOp)  throws Exception {         
           try {          

             StringBuffer sStm = new StringBuffer();

             sStm.append("UPDATE QUERY ");
 			 sStm.append("SET NOME='" + Global.replaceAll(name,"'","''") + "' "+ ",");
			 sStm.append("DATA_AGGIORNAMENTO=sysdate,");
             sStm.append("UTENTE_AGGIORNAMENTO='"+en.getUser()+ "'");
			 sStm.append("where id_query=" + identifierQuery);

             dbOp.setStatement(sStm.toString());
             dbOp.execute();
           
           }
           catch (Exception e) {           
             throw new Exception("IQuery::updateNameQuery\n" + e.getMessage());           
           }
   }

   /**
    * Restituisce il nome della cartella corrente   
   */
   private void getNomeQuery() throws Exception {
          
           IDbOperationSQL dbOp = null;         
          
           if (identifierQuery==null)
              throw new Exception("IQuery::getNomeQuery - "+
                                  "Impossibile recuperare nameQuery se idQuery è nullo");
                                 
              try {
                StringBuffer sStm = new StringBuffer();
          
                en.connect();
                dbOp = en.getDbOp();

                sStm.append("select nome ");
                sStm.append("from query ");
                sStm.append("where id_query= "+identifierQuery);

                dbOp.setStatement(sStm.toString());
                dbOp.execute();  

                ResultSet rst = dbOp.getRstSet();

                if ( rst.next() ) 
                   nameQuery = rst.getString(1);               
                else {
                   throw new Exception("IQuery::getNomeQuery - "+
                                       "Errore nel recupero del nameQuery da idQuery");     
                }
               
                en.disconnectClose();
               
              }
              catch (Exception e) {               
                en.disconnectClose();
                throw new Exception("IQuery::getNomeQuery()\n" + e.getMessage());
              }               
   }  

   /**
    * Metodo utilizzato in modalità "HUMMINGBIRD"
    * che serve ad effettuare il parse dell'XML
    * restituito in seguito alla chiamata del WS,
    * metodo getDocuments   
   */   
   private void parseXmlProfGet(String xml) {
       
           try {
              DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
              factory.setIgnoringElementContentWhitespace(true);
              DocumentBuilder builder =  factory.newDocumentBuilder();              
              InputSource inStream = new InputSource();
              inStream.setCharacterStream(new StringReader(xml));
              Document doc = builder.parse(inStream);
            
              NodeList nodesDescr,nodesChildsDescr,nodesInfo;

              nodesDescr = doc.getElementsByTagName("DOC_DESCR");     
              for (int i = 0; i < nodesDescr.getLength(); i++) {             
                  nodesChildsDescr=nodesDescr.item(i).getChildNodes();
                  Profilo pApp = new Profilo(cmQuery,area);
                  
                  pApp.initVarEnv(en);

                  for (int j = 0; j < nodesChildsDescr.getLength(); j++) {

                        nodesInfo=nodesChildsDescr.item(j).getChildNodes();
                        
                        for (int k = 0; k < nodesInfo.getLength(); k++) {                            
                            try {

                              String nomeNodo=nodesInfo.item(k).getNodeName();
                              if (nomeNodo.equals("PROFILE_INFO")||nomeNodo.equals("SECURITY_INFO")) {
                                 String name=nodesInfo.item(k).getAttributes().item(0).getNodeValue();
                                 String value=nodesInfo.item(k).getAttributes().item(1).getNodeValue();
                                 
                                 if (name.equals("")||value.equals("")) continue;

                                 if (nomeNodo.equals("PROFILE_INFO")) {                                    
                                    if (name.equals("DOCNUM")) pApp.setDocNumber(value);
                                    pApp.settaValore(name,value);      
                                 }
                                 else {
                                    pApp.settaACL(name,value);
                                 }
                              }
                              else if (nomeNodo.equals("item")) {
                                 String item=nodesInfo.item(k).getAttributes().item(0).getNodeValue();/*item(1).getNodeValue()*/;
                                 pApp.settaValore("RELATED_"+i+"_"+k,item);                                 
                              }
                            }
                            catch(Exception e){}
                        }
                   }
                   
                   pApp.accedi("NORETRIEVE");
                   profili.add(pApp);

              }

           }
           catch (Exception e) {
              error="IQuery::parseXmlProfGet()\n"+e.getMessage();
           }
   }

   /**
    * Assegna le competenze alla query ed al profilo
   */
   private void assegnaCompetenzeQuery(IDbOperationSQL dbOp) throws Exception {         
           try {           
             //Se sono sulla cartella di sistema 
             //devo ereditare le competenze dal tipo documento
             if (rootQuery.equals(Global.ROOT_SYSTEM_FOLDER))                
                 (new GDM_Competenze(en)).allineaCompetenzaCartQueryDoc(identifierQuery,profileQuery,en.getUser(),"Q");                
           
             //SONO SULLA WRKSP UTENTE E QUINDI ELIMINO QUELLI DEL DOC CHE LA
             //AggiungiDocumento HA MESSO DI DEFAULT E FORNISCO LE SEGUENTI STANDARD:
             //DOCUMENTO -> LUD    VIEW_CARTELLA -> LUCD		 
             else {               
                 StringBuffer sStm = new StringBuffer();
               
                 try {
                   sStm.append("DELETE FROM  SI4_COMPETENZE ");
                   sStm.append("WHERE OGGETTO='"+profileQuery+"' ");
                   sStm.append("AND id_abilitazione IN ");
                   sStm.append(" (SELECT id_abilitazione FROM SI4_ABILITAZIONI,SI4_TIPI_OGGETTO ");
                   sStm.append(" WHERE SI4_TIPI_OGGETTO.TIPO_OGGETTO='DOCUMENTI' ");
                   sStm.append(" AND SI4_TIPI_OGGETTO.ID_TIPO_OGGETTO=SI4_ABILITAZIONI.ID_TIPO_OGGETTO)");
                     
                   dbOp.setStatement(sStm.toString());
  
                   dbOp.execute();               
                 }
                 catch (Exception e) {
                   throw new Exception("IQuery::assegnaCompetenzeQuery - Delete SI4_COMPETENZE\n"+e.getMessage());
                 }
            
                 try {											
               
                   UtenteAbilitazione env = new UtenteAbilitazione( en.getUser(), en.getPwd(),null,null);								
                   Abilitazioni ab = new Abilitazioni("DOCUMENTI",profileQuery,"LUD");				
                   (new GDM_Competenze(en)).assegnaCompentenza(env,ab);	

                   Abilitazioni ab2;
                   ab2 = new Abilitazioni("QUERY",identifierQuery,"LUCD");							
                   (new GDM_Competenze(en)).assegnaCompentenza(env,ab2);				
  
                 }
                 catch (Exception e) {
                   throw new Exception("IQuery::assegnaCompetenzeQuery - Errore assegnamento competenze Documento-Query\n"+e.getMessage());
                 }
             
             }
           }
           catch (Exception e) {
             throw new Exception("IQuery::assegnaCompetenzeQuery\n"+e.getMessage());
           }         
   }
  
  
   /**
    * Verifica la competenza "tipoCompetenza" sulla query idQuery   
   */
   private boolean verificaCompetenzaQuery(String idQuery,String tipoCompetenza) throws Exception {
           try {
             Abilitazioni abilitazione = new Abilitazioni(Global.ABIL_QUERY, idQuery , tipoCompetenza); 
             UtenteAbilitazione ua = new UtenteAbilitazione(en.getUser(), en.getGruppo(), en.getRuolo(), en.getPwd(),  en.getUser(), en);
      
             if ((new GDM_Competenze(en)).verifica_GDM_Compentenza(ua,abilitazione)  == 1 )
                return true;
    
             return false;
           }
           catch (Exception e) {        
             throw new Exception("IQuery::verificaCompetenzaQuery(@,@)\n" + e.getMessage());
           }                  
   }
  
   /**
    * Questo metodo provvede ad inserire sulla tabella
    * RICHIESTE il codice richiesta passato.
    * Serve per l'inserimento del codice richiesta
    * del documento profilo query ed è lo stesso meccanismo
    * che utilizza la servlet modulistica
   */
   private void insertCodiceRichiesta(String cr,IDbOperationSQL dbOp) throws Exception {
           try {

             StringBuffer sStm = new StringBuffer();

             sStm.append("insert into richieste (CODICE_RICHIESTA, AREA,DATA_INSERIMENTO) ");
             sStm.append("values ('"+cr+"','"+area+"',sysdate)");

             dbOp.setStatement(sStm.toString());
             dbOp.execute();

           }
           catch (Exception e) {
             throw new Exception("IQuery::insertCodiceRichiesta("+cr+")\n" + e.getMessage());           
           }
   }

   /**
    * Inizializzazione delle variabili private
    * Metodo richiamato dai costruttori
   */
   private void inizializza(Environment vEnv) throws Exception {             
           campiRicerca = new Vector();
           ogfiRicerca = new Vector();
           campiOrdinamento = new Vector();
           cmRicerca = new Vector();
           cmAreaRicerca = new Vector();
           profili = new Vector();         
           usersFilters = new Vector();
           groupFilters = new Vector();
           ACLuser = new Vector();
           ACLtype = new Vector();  
           en=vEnv;
           queryTimeOut=vEnv.Global.QUERY_TIMEOUT;
           queryServiceLimit=vEnv.Global.QRYSERVICE_LIMIT;
           error="@";
         
           //NON vengo dal costruttore NEW Query
           if (rootQuery==null) {                               
             //Se ho passato l'id della query, mi recupero anche l'id del profilo
             if (identifierQuery!=null) {              
                 retrieveQueryInfoById();                 
             }
             //Vengo dal costruttore che ha passato cm, cr, area del profilo   
             else if (cmQuery!=null && crQuery!=null) {                                    
                  profileQuery=(new DocUtil(en)).getIdDocumentoByAreaCmCr(area, cmQuery, crQuery);
                 
                  //Identificativo Query = codice richiesta senza il segno meno
                  identifierQuery=crQuery.substring(1,crQuery.length());
                 
                  retrieveQueryInfoById();
             }
           }
           else
        	  addValue("NOME",nameQuery);
   }  

   /**
    * Recupero delle informazioni
    * base del profiloQuery a partire 
    * dall'id della query
   */
   private void retrieveQueryInfoById() throws Exception {
           try {
              //IdProfilo in Cartella
             if (profileQuery==null)
                 profileQuery = (new DocUtil(en)).getIdDocumentoByCr("-"+identifierQuery);  
    
             //Nome Query
             getNomeQuery();
             
             //Leggo i parametri dal filtro
             leggiDBQuery();
 
           }
           catch (Exception e) {        	 
             throw new Exception("IQuery::retrieveQueryInfoById - "+e.getMessage());
           }
   }
   
   private Profilo createDocumento(String idProfilo) {
	       Class classe;
	       Profilo retProfilo = null;

	       try {	    
    		 classe = Class.forName(classnameExportRicerca);
    		 Class[] argsClass = new Class[] {String.class, String.class, String.class, 
    			 						  String.class, Connection.class};
    		 Object[] argomenti = new Object[] {idProfilo, en.getUser(),en.getPwd(),
    			                            en.getIniFile(),en.Global.CONNECTION};
    	 
    		 Constructor argsConstructor = classe.getConstructor(argsClass);
    		 retProfilo = (Profilo) argsConstructor.newInstance(argomenti);    		     		
	    	 
	       } catch (Exception e) {
	    	 retProfilo = null;
	       } 

	       return retProfilo;
   }
   
   private void leggiDBQuery() throws Exception {
	   	   GD4_Gestione_Query gq = null;
	       
	   	   try {
	          gq = new GD4_Gestione_Query(Long.parseLong(identifierQuery),en,true);
	       }
	       catch (Exception e) {
	    	  throw new Exception("IQuery::leggiDBQuery Creazione GD4_Gestione_Query - "+e.getMessage()); 
	       }
	       
	       this.settaArea(gq.getArea());
	       
	       Vector vAppoggio = gq.getTipoDoc();
	       
	       if (vAppoggio!=null)
	    	   for(int i=0;i<vAppoggio.size();i++) {
	    		   this.addCodiceModello(""+vAppoggio.get(i));
	    	   }

	       Vector parametriRicerca;
		   parametriRicerca=gq.getParametryQuery();
		   
		   if (parametriRicerca!=null)
			   for (int i=0;i<parametriRicerca.size();i++) {
				   keyval k;
				   
				   ((ParametriQuery)parametriRicerca.get(i)).setColonna(((ParametriQuery)parametriRicerca.get(i)).getColonna().replace('$',','));
				   k = gq.setCondizioniParametriche(((ParametriQuery)parametriRicerca.get(i)).getChiave(),":@");
				   
				   if (k!=null)
				       if (k.getKey().equals(":@,:@")) {				   
					       k.setKey((((ParametriQuery)parametriRicerca.get(i)).getParametro()));
					       k.setTipoDaClient("Between");
					       k.setOperator(":@");
					       k.setValue(":@");
				       }
	
			   }	       
	       	       	       
	       if (gq.getCampi()!=null) {
	    	   campiRicerca.clear();
	    	   campiRicerca = gq.getCampi();
	       }
	      	       
	       if (gq.getOrdinamenti()!=null) {
	    	   campiOrdinamento.clear();
	    	   campiOrdinamento = gq.getOrdinamenti();
	       }
	       	       
	       cAnd=gq.getCondizione("AND");
	       cOr=gq.getCondizione("OR");
	       
	       String sTimeOut=gq.getCondizione("TIMEOUT");	      
		   if (sTimeOut!=null) {
		       queryTimeOut=Integer.parseInt(sTimeOut);				   
		   }
		   	       
   }

   private void prepareXMLFilter() {
	       xmlf = new XMLFilter();
	       
	       /**CONSERVO L'AREA**/
	       xmlf.setArea(areaRicerca);
	       
	       /**CONSERVO I TIPI DOCUMENTO**/
	       for(int i=0;i<cmRicerca.size();i++) 
	    	   xmlf.addTipoDoc(""+cmRicerca.get(i));	       	       
	       
	       /**CONSERVO I TIPI CAMPI**/	       
	       //if (areaRicerca!=null) 
		       for(int i=0;i<campiRicerca.size();i++) {
		    	   keyval k= ((keyval)campiRicerca.get(i));

		    	   //E' UN CAMPO DI JOIN....ESCLUDIAMOLO
		    	   if (k.getIndexJoin()!=0) continue;
		    	   
		    	   keyval kRet = createFilterCondition(k);	    	   	    	   
		    	   
		    	   if (kRet.getArea()!=null)
		    	      xmlf.addCampo(kRet.getArea(),kRet.getCm(),"",kRet.getKey(),kRet.getTipoDoc(),kRet.getVal(),kRet.getValueNvl(),kRet.getOperator());
		    	   else if (kRet.getCategoria()!=null)
		    		   xmlf.addCampo("","",kRet.getCategoria(),kRet.getKey(),kRet.getTipoDoc(),kRet.getVal(),kRet.getValueNvl(),kRet.getOperator());
		    	   else
		    		   xmlf.addCampo(kRet.getKey(),kRet.getTipoDoc(),kRet.getVal(),kRet.getValueNvl(),kRet.getOperator());
		    	   
		       }

	       for(int i=0;i<campiOrdinamento.size();i++) {
		      // String s=""+campiOrdinamento.get(i);
	    	   String s, sArea="", sCm="", sCategoria="";
	    	   boolean bIsCampoDiReturn=false;
	    	   
		       if (campiOrdinamento.get(i) instanceof keyval) {
		    	   keyval kAppoggio=(keyval)campiOrdinamento.get(i);
		    	   s=kAppoggio.getKey();
		    	   
		    	   sArea=kAppoggio.getArea();
		    	   
		    	   if (sArea==null) sArea="";
		    	   
		    	   sCm=kAppoggio.getCm();
		    	   
		    	   if (sCm==null) sCm="";
		    	   
		    	   sCategoria=kAppoggio.getCategoria();
		    	   
		    	   if (sCategoria==null) sCategoria="";
		    	   //Se è un campo di ritorno non lo devo mettere nell'ordinamento
		    	   if (kAppoggio.getCampoReturn().equals(kAppoggio.ISCAMPO_RETURN))
		    	      bIsCampoDiReturn=true;
		       }
		       else {
		    	   s=""+campiOrdinamento.get(i);   		    
		       }
		       
		       if (!bIsCampoDiReturn) {
		    	   if (sArea.equals("") && sCategoria.equals(""))
		    		   xmlf.addCampoOrdinamento(s.substring(0,s.indexOf("@")),s.substring(s.indexOf("@")+1,s.length()));
		    	   else
		    		   xmlf.addCampoOrdinamento(sArea,sCm,sCategoria,s.substring(0,s.indexOf("@")),s.substring(s.indexOf("@")+1,s.length()));
		       }
		       
		   }
	       
	       /**CERCO I CAMPI DI JOIN**/
	       int joinAttuale=0;
   		   for(int i=0;i<campiRicerca.size();i++) {
   		   	   keyval k = (keyval)campiRicerca.get(i);
   		   		  

   		   	   int indexJoin=k.getIndexJoin();
		   	   
		   	   //Se non si tratta di un campo di join skippo
		   	   if (indexJoin==0 || joinAttuale>=indexJoin) continue;
		   		   
		   	   //Se ho trovato un campo di join sicuramente il campo
		   	   //successivo sarà la chiave di join con quello trovato
		   	   keyval k2 = (keyval)campiRicerca.get(++i);
		   		   
		   	   //Qui setto i valori per il prox ciclo (inizio
		   	   //nuovamente dal primo campo a cercare altri join
		   	   //ma con indice maggiore dell'attuale)
		   	   i=0;
		   	   joinAttuale=indexJoin;
		   	   String tipo1, tipo2;
		   	   
		   	   try {
			   	    if (k.getArea()!=null)  
		    			tipo1=(((new LookUpDMTable(en)).retrieveTipo(k.getKey(),null,k.getArea()))).getTipo();
		    		else
		    			tipo1=(((new LookUpDMTable(en)).retrieveTipo(k.getKey(),null,areaRicerca))).getTipo();
		   	   }
		   	   catch (Exception e) {
		   		   tipo1="";
		   	   }
		   	   
		   	   try {
			   	    if (k2.getArea()!=null)  
		    			tipo2=(((new LookUpDMTable(en)).retrieveTipo(k2.getKey(),null,k2.getArea()))).getTipo();
		    		else
		    			tipo2=(((new LookUpDMTable(en)).retrieveTipo(k2.getKey(),null,areaRicerca))).getTipo();
		   	   }
		   	   catch (Exception e) {
		   		   tipo2="";
		   	   }		   	   
		   	   
		   	   xmlf.setJoinCondition(k.getArea(),k.getCm(),k.getCategoria(),k.getKey(),tipo1,
		   			                 k2.getArea(),k2.getCm(),k2.getCategoria(),k2.getKey(),tipo2); 
   		   }
	    
	       /**CONSERVO AND E OR**/
	       xmlf.setAndCondition(cAnd);	      
	       xmlf.setOrCondition(cOr);
	       
	       /**GESTIONE DEL CAMPO MASTER**/
	       if (bIsMaster)
	    	   xmlf.setMaster("1");
	       else
	    	   xmlf.setMaster("0");

	       /**GESTIONE DEL CAMPO TIMEOUT**/
	              
   }
   
   private keyval createFilterCondition(keyval k) {
	       keyval kRet = new keyval();
	       
	       if (k.getValueNvl()==null)
	    	   kRet.setValueNvl("");
	       else
	    	   kRet.setValueNvl(k.getValueNvl());
	       
	       if (k.getOperator()==null || k.getOperator().equals("contains")) {
	    	   if (k.getVal().equals("is null") || k.getVal().equals("is not null")) {
	    	      kRet.setKey(k.getKey());
	    		  kRet.setOperator(k.getVal());
	    		  kRet.setValue("");
	    		  try {
	    			if (k.getArea()!=null)  
	    				kRet.setTipoDoc(((new LookUpDMTable(en)).retrieveTipo(k.getKey(),null,k.getArea())).getTipo());
	    			else
	    				kRet.setTipoDoc(((new LookUpDMTable(en)).retrieveTipo(k.getKey(),null,areaRicerca)).getTipo());
     		      }
	    		  catch (Exception e) {
	    			kRet.setTipoDoc("S");  
	    		  }
	    	   }
	    	   else {
	    		  if (DateUtility.isDateValid(k.getVal(),"dd/mm/yyyy")) {
	    			  kRet.setKey(k.getKey());
	    			  kRet.setOperator("=");
	    			  kRet.setValue(k.getVal());
	    			  kRet.setTipoDoc("");  
	    		  }
	    		  else {
	    			  String tipo;
	    			  try {
	    			    tipo=((new LookUpDMTable(en)).retrieveTipo(k.getKey(),null,areaRicerca)).getTipo();
	    			  }
	    		      catch (Exception e) {
	    			    tipo="S";  
	    		      }	  
	    		      
	    		      if (tipo.equals("S"))
	    		    	  kRet.setOperator("uguale");
	    		      else
	    		    	  kRet.setOperator("=");
	    		      
	    		      kRet.setKey(k.getKey());	    			 
	    			  kRet.setValue(k.getVal());
	    			  kRet.setTipoDoc("");  
	    		  }
	    	   }	    		   
	       }
	       else {
	    	   
	    	   String tipoOperatore, valBetween="";
	    	   try {
	    	     tipoOperatore=(new LookUpDMTable(en)).lookupTipoOperatore(k.getOperator());
	    	   }
	    	   catch (Exception e) {
	    	     tipoOperatore="S";  
	    	   }	 
	    	   
	    	   if (!tipoOperatore.equals("S") || k.getOperator().equals(":@")) {
	    		   tipoOperatore="Between";
	    		   valBetween="$"+k.getOperator();
	    	   }
	    	   else
	    		   tipoOperatore=k.getOperator().replaceAll("<","#");
	    	   
	    	   
	    	   kRet.setKey(k.getKey());	    			 
	    	   kRet.setOperator(tipoOperatore);
	    	   kRet.setValue(k.getVal()+valBetween);
	    	   kRet.setTipoDoc("");  
	       }
	       
	       kRet.setArea(k.getArea());
	       kRet.setCm(k.getCm());
	       kRet.setCategoria(k.getCategoria());
	       
	       return kRet;
   }
   
   private void addObjFileCondition(keyval k) {
	       int tipoRicerca;
	       if (k.getCategoria().equals("") && k.getArea().equals("")) {
	    	   tipoRicerca=1;
	       }
	       else {	       
		       if (!(k.getCategoria().equals("")))
		    	   tipoRicerca=2;
		       else
		    	   tipoRicerca=3;
	       }
	       
	       for(int i=0;i<ogfiRicerca.size();i++) {
	    	   keyval kRicerca =  (keyval)ogfiRicerca.get(i);	    	   	    	   
	    	   
	    	   switch (tipoRicerca) {
	    	        //Ricerca ""
	    	   		case 1:
	    	   			 if (kRicerca.getArea().equals("") && kRicerca.getCategoria().equals("") && 
	    	   				 ( (kRicerca.getIsOcr() && k.getIsOcr()) || (!kRicerca.getIsOcr() && !k.getIsOcr())) ) return;
	    	   			 break;
	    	   		//Ricerca Categoria
	    	   		case 2:
	    	   			 if (kRicerca.getCategoria().equals(k.getCategoria()) && 
		    	   				 ( (kRicerca.getIsOcr() && k.getIsOcr()) || (!kRicerca.getIsOcr() && !k.getIsOcr()))) return;
	    	   			 break;
	    	   		//Ricerca Modello	 
	    	   		case 3:
	    	   			 if (kRicerca.getArea().equals(k.getArea()) && kRicerca.getCm().equals(k.getCm()) &&
		    	   				 ( (kRicerca.getIsOcr() && k.getIsOcr()) || (!kRicerca.getIsOcr() && !k.getIsOcr()))) return;
	    	   			 break;
	    	   }
	       }
	      
	       
	       //Se il valore non è presente lo aggiungo
	       ogfiRicerca.add(k);
	       
   }
   
   //Verifica se esiste la chiave nel vettore di ordinamento
   //ed eventualmente la aggiorna per evitare di doppiarla
   //quando si usa la addCampoReturn
   private boolean controllaVettoreCampoOrdinamento(String sKey,String area, String cm, String categoria, String tipoOrdinamento) {
	       for(int i=0;i<campiOrdinamento.size();i++) {
	    	   
	    	   if (campiOrdinamento.get(i) instanceof String) continue;
	    	   
	    	   String chiave = ((keyval)campiOrdinamento.get(i)).getKey();
	    	   
	    	   //Gli tolgo il pezzo dopo la @
	    	   chiave=chiave.substring(0,chiave.indexOf("@"));
	    	   
	    	   if (chiave.equals(sKey)) {
	    		   String areaOrdinamento=((keyval)campiOrdinamento.get(i)).getArea();
	    		   String cmOrdinamento=((keyval)campiOrdinamento.get(i)).getCm();
	    		   String categoriaOrdinamento=((keyval)campiOrdinamento.get(i)).getCategoria();
	    		   //Ho trovato la chiave nel vettore
	    		   if (
	    				 (area==null && cm==null && categoria==null) 
	    				 ||    			  
	    			     (area!=null && cm!=null && areaOrdinamento!=null && cmOrdinamento!=null && areaOrdinamento.equals(area) && cmOrdinamento.equals(cm) )	    				 
	    				 ||
	    			     (categoria!=null && categoriaOrdinamento!=null && categoriaOrdinamento.equals(categoria)  ) 
	    			   ) {	    			   	
	    			     
	    			     keyval k = ((keyval)campiOrdinamento.get(i));
	    			     
	    			     if (k.getCampoReturn().equals(k.ISCAMPO_ORDINAMENTO) && tipoOrdinamento.equals("DONTCARE"))
	    			      	k.setCampoReturn(k.ISCAMPO_ORDINAMENTO_AND_RETURN);
	    			     
	    			     if (k.getCampoReturn().equals(k.ISCAMPO_RETURN) && !tipoOrdinamento.equals("DONTCARE"))
	    			      	k.setCampoReturn(k.ISCAMPO_ORDINAMENTO_AND_RETURN);	    			     
	    			     
	    			     //Vengo da una addCampoOrdinamentoDesc o addCampoOrdinamentoAsc
	    			     if (tipoOrdinamento.equals("ASC") || tipoOrdinamento.equals("DESC"))  
	    			    	 k.setKey(chiave+"@"+tipoOrdinamento);
	    			    		    			     	    			    
	    			     campiOrdinamento.set(i,k);
	    			     
	    			     return true;
	    		   }
	    	   }
	       }
	       
	       return false;
   }

   public String getExtraConditionSearch() {
	      return extraConditionSearch;
   }

   public void setExtraConditionSearch(String extraConditionSearch) {
	      this.extraConditionSearch = extraConditionSearch;
   }

   public String getTypeAbilityDocument() {
	      return controlloCompetenzaQuery;
   }

   /**
    * Metodo per poter specificare quale competenza 
    * cercare per i documenti estratti dalla query stessa. 
    * Per default sarà la lettura
    * Utilizzare le costanti presenti sulla Classe Global
    * che iniziano con ABIL_, es:
    * 
    * Global.ABIL_LETT
    * 
    * @param controlloCompetenzaQuery
   */       
   public void setTypeAbilityDocument(String controlloCompetenzaQuery) {
	      this.controlloCompetenzaQuery = controlloCompetenzaQuery;
   }

   public String getCmQuery() {
		  return cmQuery;
   }
   
  /* public void test() {
	      for(int i=0;i<campiOrdinamento.size();i++) {
	    	  if (campiOrdinamento.get(i) instanceof String) continue;
	    	  
	    	  keyval k = ((keyval)campiOrdinamento.get(i));
	    	  System.out.println("key--->"+k.getKey());
	    	  System.out.println("area--->"+k.getArea());
	    	  System.out.println("cm--->"+k.getCm());
	    	  System.out.println("catego--->"+k.getCategoria());
	    	  System.out.println("tipo--->"+k.getCampoReturn());
	      }
   }*/
}