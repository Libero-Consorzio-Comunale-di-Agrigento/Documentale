package it.finmatica.dmServer.management;

import it.finmatica.dmServer.GD4_Documento;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Timestamp;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import it.finmatica.alfresco.ws.AlfrescoACL;
import it.finmatica.alfresco.ws.finmaticaDmComponent.AlfrescoProfilo;
import it.finmatica.dmServer.ACL;
import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.GD4_Valori;
import it.finmatica.dmServer.Riferimento;
import it.finmatica.dmServer.Acl_Nominali; 
import it.finmatica.dmServer.A_Oggetti_File;
import it.finmatica.dmServer.Impronta.ImprontaAllegati;
import it.finmatica.dmServer.Impronta.SeganalazioniVerificaImpronte;
import it.finmatica.dmServer.competenze.GDM_Competenze;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.util.HashMapSet;
import it.finmatica.dmServer.util.ManageConnection;
import it.finmatica.dmServer.util.DocUtil;
import it.finmatica.dmServer.util.PdfUtil;
import it.finmatica.dmServer.util.RtfUtil;
import it.finmatica.dmServer.util.FileStruct;
import it.finmatica.dmServer.util.LookUpDMTable;
import it.finmatica.dmServer.util.UtilityDate;
import it.finmatica.dmServer.util.keyval;
import it.finmatica.jfc.io.LetturaScritturaFileFS;
import it.finmatica.jfc.utility.FileUtility;
  
/**     
 * Gestione di un profilo (Documento del documentale).<BR>
 * <BR>
 * Esempio di <B>INSERIMENTO/MODIFICA:</B><BR>
 * <BR> 		
 * 		   // Creazione del profilo passando<BR>
 * 		   // Codice Modello e Area<BR>
 * 		   // Aggiungendo il terzo parametro (COD. RICHIESTA)<BR>
 * 		   // si ottiene la modifica di un profilo esistente<BR>
 * <BR>
 * 		   Profilo p = new Profilo("NOTIFICA","MESSI");<BR>
 * <BR>
 * 		   // Inizializzazione delle variabili<BR>
 * 		   // Viene passato User Ad4, Password AD4, Connection<BR>
 * 		   // in alternativa alla connection ? possibile passare<BR>
 * 		   // il percorso del file di properties <BR>
 * 		   // es: "S:\\SI4\\GD4\\jGD4\\lib\\gd4dm.properties"<BR>
 * <BR>
 * 		   p.initVarEnv("GDM","GDM", conn);<BR>
 * <BR>
 * 		   p.settaValore("ANNO_REG","2006");<BR>
 * <BR>		   
 *		   Calendar cal = Calendar.getInstance();<BR>
 *         java.sql.Timestamp now = new java.sql.Timestamp(cal.getTimeInMillis());<BR>
 *         p.settaValore("DATA_NASCITA",now);<BR>
 * <BR>         
 *         p.settaValore("NUM_REG",67);<BR>
 * <BR>        
 *         p.setFileName("c:\\Allegato.DOC");<BR> 
 *         p.settaACL("AA4",Global.NO_ACCESS);<BR>
 * <BR>        
 *         if (p.salva().booleanValue())<BR> 
 *             &nbsp;&nbsp;&nbsp;System.out.println("Creato/Aggiornato N? Documento: " + p.getDocNumber());<BR>
 *         else<BR>
 *             &nbsp;&nbsp;&nbsp;System.out.println("Errore nella creazione del documento:\n"+p.getError());<BR>
 * <BR>            
 * Esempio di <B>ACCESSO:</B><BR>              
 * <BR>		   
 * 		   // Accesso al profilo con identificativo 43353<BR>
 * 		   // In alternativa si potrebbe utilizzare il costruttore<BR>
 * 		   // con tre parametri (area,codMod,codRich)<BR>	
 * <BR>
 * 		   Profilo p = new Profilo("43353");<BR>
 * <BR>
 * 		   // Inizializzazione delle variabili<BR>
 * 		   // Viene passato User Ad4, Password AD4, Connection<BR>
 * 		   // in alternativa alla connection ? possibile passare<BR>
 * 		   // il percorso del file di properties <BR>
 * 		   // es: "S:\\SI4\\GD4\\jGD4\\lib\\gd4dm.properties"<BR>
 * <BR>
 * 		   p.initVarEnv("GDM","GDM", conn);<BR>
 * <BR>
 * 		   // Accesso solo valori (NO ALLEGATI)<BR>
 * <BR>
 * 		   if (p.accedi(Global.ACCESS_NO_ATTACH).booleanValue()) <BR>   
 * 			  &nbsp;&nbsp;&nbsp;System.out.println(p.getCampo("ANNO_REG"));<BR>
 * 		   else<BR>
 * 			  &nbsp;&nbsp;&nbsp;System.out.println("Errore in accesso documento:\n"+p.getError());<BR>
 * <BR> 
 * <B>Cenni sulle connessioni (gestione dei commit e dei rollback):</B><BR>
 * E' possibile inizializzare il profilo con il metodo initVarEnv passando la connection
 * oppure il percorso del file di properties.<BR><BR>
 * Se siamo in presenza del file di properties, la connessione al database viene gestita 
 * internamente in maniera atomica: viene creata una sessione al richiamo del metodo salva()
 * che si premura di effettuare un commit o un rollback "interno" a seconda se le operazioni 
 * di registrazione del profilo sono andate rispettivamente a buon fine oppure sono fallite.<BR><BR> 
 * Se viene passata una connection, la gestione ? tutta a carico dell'utente: sar? quest'ultimo
 * che dovr? preoccuparsi o meno di effettuare i commit o i rollback, eccezion fatta per i casi
 * in cui si verifichino degli errori al richiamo del salvataggio del profilo.
 * In tal caso ? stata prevista una gestione con dei savepoint interni; la classe profilo,
 * in caso di errore al salvataggio, effettuer? un rollback interno automatico fino al savepoint
 * inizialmente creato, lasciando invariate nella connection passata dall'esterno tutte 
 * le operazioni precendenti a quelle interne al profilo. 
 * 
 * @author  D. Scandurra, G. Mannella
 * @version 3.2
 *
*/
  
public class Profilo extends ProfiloBase
{   	
   private Vector idAllegatiInUpdate;   
   private String newStatus=null;
   private String idDocumentoPadre=null;
   private String dataUltAgg=null;
   private String dataCreazione=null;

/* ATTRIBUTI DI ALLEGATI */
   private Vector pathFile;
   private Vector pathFileContentType;
   private Vector pathFileToDelete;
   private Vector pathFilePadre;
   private Vector completePathFile;
   private Vector isImpronta;            //"0"=senza Impronta, "1"=con Impronta
   private Vector isCreaOSettaImpronta;  //"1"=crea Impronta, "0"=setta Impronta
   private Vector file;   
   private InputStream fileP7M;
   private Vector fileStructRename;
   
   /* ATTRIBUTI DI RIFERIMENTO */
   private Vector<String> docRiferimenti;
   private Vector<String> typeRiferimenti;
   
   private Vector docRiferimentiToDelete;
   private Vector typeRiferimentiToDelete;
   
   private boolean bDontRepeatExistsRif=false;

   private int posRiferimento=0;
   private int posRiferimentoFrom=0;
   
   private AggiungiDocumento agd = null;

   /* ATTRIBUTI DI ACL */
   private Vector ACLuser;
   private Vector ACLtype;  
   
   private Vector ACLExtraUser;
   private Vector ACLExtraType;
   
   private Acl_Nominali aclNom;  
   
   private boolean kofax=false;   
    
   public static String IMPRONTA_ON = "1"; 
   public static String IMPRONTA_OFF = "0";
   
   public static String CREA_IMPRONTA = "1"; 
   public static String SETTA_IMPRONTA = "0";   
   
   public static String RICHIESTA_CONSERVAZIONE = "WORKING"; 
   public static String RICHIESTA_ARCHIVIAZIONE = "WORKING";    

   private String conservazione = null;
   private String archiviazione = null;   
   
   public boolean bAggiornaDataUltAgg = true;   
   private String ultAggiornamento = null;
   
   private boolean forceMaintaninPreBozza = false;
   
   private boolean bForceAllegatiTemp = false;
   private String  crAllegatiTemp     = null;
   
   private Exception lastException = null;
   
   private ImprontaAllegati impronte512 = null;
   
   private boolean setBSkipUnknowField=false;
   
   private boolean setSkipBusyControl=false;
   
   //Parte Alfresco
   private AlfrescoStruct aStruct=null;
   private String alfrescoXMLRet=null;
   
   private String competenzeArray[] = null;
   private static int COMP_LETTURA = 0;
   private static int COMP_MODIFICA = 1;
   private static int COMP_CANCELLAZIONE = 2;
   private static int COMP_MANAGE = 3;  
   
   private boolean alwaysNew=false;
   
   private boolean skipAddCompetenzeModello=false;
   
   private boolean skipReindexFullTextField=false;   
      
   public static int RETRIEVE_ALLACL_USER         = 1;   
   public static int RETRIEVE_ALLACL_USERANDGROUP = 2;
   public static int NORETRIEVE_ALLACL            = 0;
   
   private Vector<String> listSkipUnknowField = new Vector<String>();
   
   private boolean bogfilog = true;

   private long idLog;
   
   public Profilo( ) {        
	   
   }      
      
   
   /** 
    * Costruttore da utilizzare esclusivamente in creazione
    * di un profilo passando codice modello e area
    * 
    * @param codiceModello codice modello del profilo da creare
    * @param area area del profilo da creare
   */
   public Profilo(String codiceModello, String area ) {
          super(codiceModello,area);
   }

   /**
    * Costruttore da utilizzare esclusivamente in creazione<BR>
    * di un profilo passando codice modello e area.
    * Utilizzando questo costruttore non ? necessario
    * lanciare la initVarEnv.
    * Il parametro ini escluder? automaticamente il
    * parametro cn (che andr? passato nullo) e viceversa
    * 
    * @param codiceModello codice modello del profilo da creare
    * @param area area del profilo da creare
    * @param user utente di AD4
    * @param passwd password di AD4
    * @param ini percorso del file di properties
    * @param cn connection
   */
   public Profilo(String codiceModello, String area,
		             String user, String passwd, 
		             String ini, Connection cn) {
	      super(codiceModello,area,user,passwd,ini,cn);
   }

   /**
    * Costruttore da utilizzare esclusivamente in fase di
    * accesso o modifica di un profilo conoscendone la
    * chiave primaria
    * 
    * @param idProfilo identificativo del profilo da accedere/modificare
   */
   public Profilo(String idProfilo) {
          super(idProfilo);
   }

   /**
    * Costruttore da utilizzare esclusivamente in fase di<BR> 
    * accesso o modifica di un profilo conoscendone la<BR>
    * chiave primaria
    * Utilizzando questo costruttore non ? necessario
    * lanciare la initVarEnv.
    * Il parametro ini escluder? automaticamente il
    * parametro cn (che andr? passato nullo) e viceversa
    * 
    * @param idProfilo identificativo del profilo da accedere/modificare
    * @param user utente di AD4
    * @param passwd password di AD4
    * @param ini percorso del file di properties
    * @param cn connection 
   */
   public Profilo(String idProfilo,
		  	         String user, String passwd, 
                     String ini, Connection cn ) {
 	      super(idProfilo,user,passwd,ini,cn);
   }

   /**
    * Costruttore da utilizzare esclusivamente in fase di
    * accesso o modifica di un profilo conoscendone la
    * tripla area/codice modello/codice richiesta
    *
    * @param codiceModello codice modello del profilo da accedere/modificare
    * @param area area del profilo da accedere/modificare
    * @param codiceRichiesta codice richiesta del profilo da accedere/modificare
   */
   public Profilo(String codiceModello, String area, String codiceRichiesta ) {
          super(codiceModello, area, codiceRichiesta);    
   }  

   /**
    * Costruttore da utilizzare esclusivamente in fase di<BR> 
    * accesso o modifica di un profilo conoscendone la<BR>
    * tripla area/codice modello/codice richiesta<BR>
    * Utilizzando questo costruttore non ? necessario
    * lanciare la initVarEnv.
    * Il parametro ini escluder? automaticamente il
    * parametro cn (che andr? passato nullo) e viceversa  
    *
    * @param codiceModello codice modello del profilo da accedere/modificare
    * @param area area del profilo da accedere/modificare
    * @param codiceRichiesta codice richiesta del profilo da accedere/modificare
    * @param user utente di AD4
    * @param passwd password di AD4
    * @param ini percorso del file di properties
    * @param cn connection
   */
   public Profilo(String codiceModello, String area, String codiceRichiesta,
		  		     String user, String passwd, 
                     String ini, Connection cn ) {
	      super(codiceModello,area,codiceRichiesta,
		  		user,passwd, 
                ini,cn);
   }

   public long getIdLog() {
        return idLog;
   }

   public boolean isOgfiLog() {
	    return bogfilog;
   }


   public void setOgfiLog(boolean bogfilog) {
		this.bogfilog = bogfilog;
   }

    /**
     * Metodo che effettua lo spostamento dell'oggetto file indicato
     * come id nel parametro di input, sul documento di questo profilo.
     * Gli oggetti che verranno spostati sono:
     *
     * - id documento della tabella oggetti_file
     * - id documento della tabella impronte file
     * - percorso su FS
     * - percorso BFILE
     *
     */
   public void moveFile(String idObjFile) throws Exception {
       if (idDocumento==null)
           throw new Exception("Impossibile spostare il file su questo profilo se non se ne conosce l'id."
               + "Accedere al documento o settare l'id con l'apposita proprietà");

       AggiornaDocumento ad =null;

       ad = new AggiornaDocumento(idDocumento, en );

       ad.moveFile(idObjFile);
   }

   /**
    * Metodo che effettua il salvataggio di un profilo.
    * Se chiamato in accoppiata con il costruttore a due
    * parametri effettua l'inserimento di un nuovo profilo,
    * altrimenti si occupa dell'aggiornamento di un profilo
    * esistente.
    *
    * @return (True o False) Esito salvataggio
   */
   public Boolean salva() {         
	  
          Boolean bTest;
          
          if (idDocumento==null) 
        	  return new Boolean(false);

          if (bEscludiControlloCompetenze)
        	  en.byPassCompetenzeON();
          
          //Se  ho settato
          //che il documento deve sempre essere new
          //rimetto "X" sull'idDocumento cos?
          //che  lui rilancia la salvaBozza
          //metodo con la aggiungiDocumento
          if (alwaysNew && !idDocumento.equals("X")) {
         	  idDocumento="X";
         	  agd.setPadre(null);
              agd.settaCodiceRichiesta(null);
              agd.setConservazione(null);
              agd.setArchiviazione(null);          
              //idDocumentoPadre=null;
              cr=null;
              conservazione=null;
              archiviazione=null;   
          }
          
          /*
           * SE NON HO SETTATO L'IDDOCUMENTO, IL
           * DOCUMENTO E' CONSIDERATO NUOVO E SI
           * FA UNA INSERT, ALTRIMENTI FACCIO UNA
           * UPDATE
          */          
          if (idDocumento.equals("X")) {
        	 if (newStatus!=null && newStatus.equals(Global.STATO_PREBOZZA))             
        		 bTest = salvaBozza(true);
        	 else
        		 bTest = salvaBozza(false);
          }        	  
          else
             bTest = aggiorna();

          return bTest;
   }

   /**
    * Metodo che effettua l'accesso alle propriet? di un profilo.
    * Deve necessariamente essere chiamato in accoppiata con il
    * costruttore a tre o ad un parametro.
    * <B>Verranno recuperati anche gli allegati</B>
    *
    * @return (True o False) Esito accesso
    * @see <a href="Profilo.html#accedi(java.lang.String)">accedi(type)</a> 
   */ 
   public Boolean accedi(){
	  	  return accedi(Global.ACCESS_ATTACH,false);
   }  
   
   public Boolean accedi(String expType){
	  	  return accedi(expType,false);
   }   
   
   public Boolean accedi(String expType, boolean bLock) {
	      return accedi(expType,  bLock, NORETRIEVE_ALLACL);
   }
   
   /**
    * Metodo che effettua l'accesso alle propriet? di un profilo.
    * Deve necessariamente essere chiamato in accoppiata con il
    * costruttore a tre o ad un parametro.
    * <B>Gli allegati verranno recuperati o meno a seconda del
    *  parametro passato in input</B>
    *
    * @param expType Utilizzare:<BR>
    * 				 &nbsp;&nbsp;&nbsp;&nbsp;Global.ACCESS_NO_ATTACH 
    * 				 per evitare di recuperare anche gli allegati<BR>
    * 				 &nbsp;&nbsp;&nbsp;&nbsp;Global.ACCESS_ATTACH 
    * 				 per recuperare anche gli allegati 
    * @param bLock   Se messo a true verr? lockato il documento (tbl documenti)
    * @param retrieveAllAcl pu? valere<BR>
    * 				  RETRIEVE_ALLACL_USER - Recupera tutte le competenze Utente
    * 										 per il profilo ("esplode" la comp.
    * 										 di gruppo in maniera ricorsiva, restituendo
    * 										 tutti gli utenti in esso contenuti)
    * 				  RETRIEVE_ALLACL_USERANDGROUP - Recupera tutte le competenze Utente
    * 										 e Gruppo per il profilo 
    * 				  NORETRIEVE_ALLACL - Non Recupera nessuna competenza
    * 	     		  
    * 				  Le competenze eventualmente recuperate possono essere poi
    * 				  lette attraverso il metodo getListaTutteCompetenze
    * 
    * @return (True o False) Esito accesso
    * @see <a href="Profilo.html#accedi()">accedi</a> 
   */
   public Boolean accedi(String expType, boolean bLock, int retrieveAllAcl) {
	      return accedi(expType, bLock,"nowait",retrieveAllAcl);
   }
   
   /**
    * Metodo che effettua l'accesso alle propriet? di un profilo.
    * Deve necessariamente essere chiamato in accoppiata con il
    * costruttore a tre o ad un parametro.
    * <B>Gli allegati verranno recuperati o meno a seconda del
    *  parametro passato in input</B>
    *
    * @param expType Utilizzare:<BR>
    * 				 &nbsp;&nbsp;&nbsp;&nbsp;Global.ACCESS_NO_ATTACH 
    * 				 per evitare di recuperare anche gli allegati<BR>
    * 				 &nbsp;&nbsp;&nbsp;&nbsp;Global.ACCESS_ATTACH 
    * 				 per recuperare anche gli allegati 
    * @param bLock   Se messo a true verr? lockato il documento (tbl documenti)
    * @param typeLock Possibilit? di sepcificare il tipo di lock:
    * 				  esempio: nowait (default), wait 10, etc...
    * 
    * @return (True o False) Esito accesso
    * @see <a href="Profilo.html#accedi()">accedi</a> 
   */
   public Boolean accedi(String expType, boolean bLock, String typeLock) {
	      return accedi(expType, bLock,typeLock,NORETRIEVE_ALLACL);
   }
  
   /**
    * Metodo che effettua l'accesso alle propriet? di un profilo.
    * Deve necessariamente essere chiamato in accoppiata con il
    * costruttore a tre o ad un parametro.
    * <B>Gli allegati verranno recuperati o meno a seconda del
    *  parametro passato in input</B>
    *
    * @param expType Utilizzare:<BR>
    * 				 &nbsp;&nbsp;&nbsp;&nbsp;Global.ACCESS_NO_ATTACH 
    * 				 per evitare di recuperare anche gli allegati<BR>
    * 				 &nbsp;&nbsp;&nbsp;&nbsp;Global.ACCESS_ATTACH 
    * 				 per recuperare anche gli allegati 
    * @param bLock   Se messo a true verr? lockato il documento (tbl documenti)
    * @param typeLock Possibilit? di sepcificare il tipo di lock:
    * 				  esempio: nowait (default), wait 10, etc...
    * @param retrieveAllAcl pu? valere<BR>
    * 				  RETRIEVE_ALLACL_USER - Recupera tutte le competenze Utente
    * 										 per il profilo ("esplode" la comp.
    * 										 di gruppo in maniera ricorsiva, restituendo
    * 										 tutti gli utenti in esso contenuti)
    * 				  RETRIEVE_ALLACL_USERANDGROUP - Recupera tutte le competenze Utente
    * 										 e Gruppo per il profilo 
    * 				  NORETRIEVE_ALLACL - Non Recupera nessuna competenza
    * 	     		  
    * 				  Le competenze eventualmente recuperate possono essere poi
    * 				  lette attraverso il metodo getListaTutteCompetenze
    * 
    * @return (True o False) Esito accesso
    * @see <a href="Profilo.html#accedi()">accedi</a> 
   */
   public Boolean accedi(String expType, boolean bLock, String typeLock, int retrieveAllAcl) {
           if (idDocumento == null) 
              return new Boolean(false);
           try{
        	 if (bEscludiControlloCompetenze)
        		 en.byPassCompetenzeON();
           
             ad = new AccediDocumento(idDocumento,en);
             
             ad.setLock(bLock);
             
             ad.setTypeLock(typeLock);
             
             ad.setSetRetrieveAllAcl(retrieveAllAcl);

             ad.setHsListAclToRetrieve(hsListAclToRetrieve);
             
             /*
              * ACCESSO AL DOCUMENTO CON ALLEGATI
              * O SENZA ALLEGATI
             */
             if (expType.equals(Global.ACCESS_ATTACH))
                ad.accediFullDocumento();
             else if (expType.equals(Global.ACCESS_NO_ATTACH))
                ad.accediDocumentoValori();
             
             /*
              * Retrieve finta...vengo da IQuery ed ho
              * gi? settato i valori nel vettore interno
              * dei campi.
              * SOLO PER DOCUMENTALE HUMMINGBIRD
             */
             else if (expType.equals("NORETRIEVE")) {
                 /* APPENDO I VALORI TORNATO DAL FILE XML DI HUMMINGBIRD (GETDOCUMENTS)*/
                 int size = campi.size();
                 for(int i=0;i<size;i++)  {                     
                     if (((String)campi.elementAt(i)).indexOf("RELATED")>=0) {
                        ad.aggiungiRiferimento((String)valori.elementAt(i),"1");
                     }
                     else
                        ad.aggiungiDati((String)campi.elementAt(i),(String)valori.elementAt(i));                                  
                 }

                 /* APPENDO LE ACL TORNATE DAL FILE XML DI HUMMINGBIRD (GETDOCUMENTS)*/
                 size = ACLuser.size();
                 for(int i=0;i<size;i++) {
                	 String sType,sRuolo;
                	 
                	 sType=(String)ACLtype.elementAt(i);
                	 
                	 sRuolo=sType.substring(sType.indexOf("@")+1,sType.length());
                	 sType=sType.substring(0,sType.indexOf("@"));
                     ad.aggiungiACL( (String)ACLuser.elementAt(i),sType,sRuolo);
                 }
             }
             
             if (ad.aDocumento!=null) {
                cr=ad.aDocumento.getCodiceRichiesta();   
                area=(new DocUtil(en)).getAreaByIdDocumento(""+idDocumento);
                tipoDocumento=(new DocUtil(en)).getModelloByIdDocumento(""+idDocumento);
                idDocumentoPadre=ad.aDocumento.getPadre();               
             }
             
             
             
             dataUltAgg=ad.ultAggiornamento;
             dataCreazione=ad.getDataCreazione();
             idLog=ad.getIdLog();
             
             return new Boolean(true);
           }
           catch (Exception e) {
             error="Accesso al Documento - \n"+e.getMessage();
             if (ad==null)
            	 codeError=Global.CODERROR_ACCESS_DOCUMENT_NOTEXISTS;
             else
            	 codeError=ad.getCodeError();
             lastException=e;
             return new Boolean(false);
           }
   }

   /**
    * Metodo che si occupa di verificare se il file
    * il cui nome ? passato in input ? coerente con
    * l'impronta memorizzata
    *
    * @param nomeFile Nome del file di cui verificare l'impronta 
    *  
    * @return (True o False) Esito verifica impronta
   */
   public Boolean verificaImpronta(String nomeFile) throws Exception {
           try {
             return new Boolean(ad.verificaImprontaOggettoFile(nomeFile));
           }
           catch(Exception e) {
             error="Profilo::verificaImpronta(String nomeFile)\n"+e.getMessage();
             throw new Exception("Profilo::verificaImpronta(String nomeFile)\n"+e.getMessage());
           }
    }

   private void caricaImpronte()  throws Exception {
  	 impronte512 = new ImprontaAllegati(this);
   }
   
 	/**
 	 * Il metodo ritona vero se almeno un allegato ha un impronta memorizzata
 	 *
 	 * @throws Exception
 	*/
   public boolean esistonoImpronte512() throws Exception {
     try {
       if (impronte512 == null) {
      	 caricaImpronte();
       }
       return impronte512.esistonoImpronte();
     }
     catch(Exception e) {
       error="Profilo::esistonoImpronte512()\n"+e.getMessage();
       throw new Exception("Profilo::esistonoImpronte512()\n"+e.getMessage());
     }
   }
   
	/**
	 * Metodo che calcola le impronte degli allegati del documento
	 * memorizzandole sul DB
	 *
	 * @throws Exception
	*/
  public void generaImpronte512() throws Exception {
     try {
       if (impronte512 == null) {
      	 caricaImpronte();
       }
       impronte512.generaImpronte();
     }
     catch(Exception e) {
       error="Profilo::generaImpronte512()\n"+e.getMessage();
       throw new Exception("Profilo::generaImpronte512()\n"+e.getMessage());
     }
   }
   
 /**
   * Metodo che calcola l'impronta dell'allegato specificato
   * memorizzandola sul DB
   * 
	 * @param nomeFile Nome dell'allegato di cui si vuole generare l'impronta
	 * @throws Exception
	 */
  public void generaImpronta512(String nomeFile)  throws Exception {
     try {
       if (impronte512 == null) {
      	 caricaImpronte();
       }
       impronte512.generaImpronta(nomeFile);
     }
     catch(Exception e) {
       error="Profilo::generaImpronta512(String nomeFile)\n"+e.getMessage();
       throw new Exception("Profilo::generaImpronta512(String nomeFile)\n"+e.getMessage());
     }
   }
   
  /**
   * Metodo che cancella le impronte degli allegati del documento
   * 
  */
	public void cancellaImpronte512() throws Exception {
    try {
      if (impronte512 == null) {
     	 caricaImpronte();
      }
      impronte512.cancellaImpronte();
    }
    catch(Exception e) {
      error="Profilo::cancellaImpronte512()\n"+e.getMessage();
      throw new Exception("Profilo::cancellaImpronte512()\n"+e.getMessage());
    }
  }

  /**
   * Metodo che cancella l'impronte dell'allegato del documento
   * specificato
   * 
   * @param nomFile	Nome dell'allegato di cui si vuole eliminare l'impronta
   * 
  */
	public void cancellaImpronta512(String nomeFile) throws Exception {
    try {
      if (impronte512 == null) {
     	 caricaImpronte();
      }
      impronte512.cancellaImpronta(nomeFile);
    }
    catch(Exception e) {
      error="Profilo::cancellaImpronte512(String nomeFile)\n"+e.getMessage();
      throw new Exception("Profilo::cancellaImpronte512(String nomeFile)\n"+e.getMessage());
    }
  }

	/**
	 * Il metodo calcola le impronte per gli allegati del documento e le
	 * confronta con  quelle memorizzate sul DB . 
	 * 
	 * @return Lista SeganalazioniVerificaImpronte.<BR>
	 * 				 La lista conterra le coppie nome file - codice segnalazione.<BR>
	 * 				 Elenco codici:<BR>
	 * 				 GLobal.CODERROR_IA_NESSUN_ERRORE						la verifica ? andata a buon fine<BR> 
	 * 				 GLobal.CODERROR_IA_DOCUMENTO_INESISTENTE		la profilo f? riferimento ad un documento non ancora memorizzato<BR> 
	 * 				 GLobal.CODERROR_IMPRONTA_ASSENTE						l'impronta dell'allegato non ? stata generata<BR> 
	 * 				 GLobal.CODERROR_ALLEGATO_MODIFICATO				l'allegato risulta modificato<BR> 
	 * 				 GLobal.CODERROR_ALLEGATO_CANCELLATO				l'allegato risulta cancellato<BR> 
	 *
	 * @throws Exception
	 */
	public SeganalazioniVerificaImpronte verificaImpronte512() throws Exception {
    try {
      if (impronte512 == null) {
     	 caricaImpronte();
      }
      impronte512.caricaDocumentiFigli(this.getArea(), this.getCodiceModello());
      return impronte512.verificaImpronte();
    }
    catch(Exception e) {
      error="Profilo::verificaImpronte512()\n"+e.getMessage();
      throw new Exception("Profilo::verificaImpronte512()\n"+e.getMessage());
    }
  }

	/**
	 * Il metodo confronta per  l'allegato specificato l'eventuale impronta
	 * presente sul DB con quella calcolata. 
	 * 
	 * @param nomeFile Nome dell'allegato da verificare
	 * @return Stringa contente il codice di errore, i valori possibili sono:<BR>
	 * 				 GLobal.CODERROR_IA_NESSUN_ERRORE						la verifica ? andata a buon fine<BR> 
	 * 				 GLobal.CODERROR_IA_DOCUMENTO_INESISTENTE		la profilo f? riferimento ad un documento non ancora memorizzato<BR> 
	 * 				 GLobal.CODERROR_IMPRONTA_ASSENTE						l'impronta dell'allegato non ? stata generata<BR> 
	 * 				 GLobal.CODERROR_ALLEGATO_MODIFICATO				l'allegato risulta modificato<BR> 
	 * 				 GLobal.CODERROR_ALLEGATO_CANCELLATO				l'allegato risulta cancellato<BR> 
	 *
	 * @throws Exception
	 */
	public String verificaImpronta512(String nomeFile) throws Exception {
    try {
      if (impronte512 == null) {
     	 caricaImpronte();
      }
      return impronte512.verificaImpronta(nomeFile);
    }
    catch(Exception e) {
      error="Profilo::verificaImpronta512(String nomeFile)\n"+e.getMessage();
      throw new Exception("Profilo::verificaImpronta512(String nomeFile)\n"+e.getMessage());
    }
  }
	
	/**
    * Metodo che provvede a cambiare lo stato di un profilo.
    * Da richiamare esclusivamente in fase di aggiornamento
    * perch? in fase di inserimento il documento verr? salvato
    * sempre in stato Bozza.
    * Esempio di utilizzo:<BR>
    * <BR>
    * Profilo p = new Profilo("43353");<BR>
    * ...................<BR>
    * ...................<BR>
    * <B>p.setStato(Global.STATO_COMPLETO);</B><BR>
    * ...................<BR>
    * ...................<BR>
    * p.salva();
    *
    * @param newStato Stato in cui registrare il profilo<BR>
    * 				  i valori possibili sono:<BR>
    * 				  	Global.STATO_BOZZA<BR>
	*					Global.STATO_COMPLETO<BR>
	*					Global.STATO_ANNULLATO<BR>
	*					Global.STATO_CANCELLATO<BR>
	*					Global.STATO_PREBOZZA<BR>
    * 
   */
   public void setStato(String newStato) {
          newStatus=newStato;
   }

   /**
    * Metodo che restituisce la data di ultimo aggiornamento
    * del profilo in formato stringa
    * 
    * @return Stringa in formato <B>yyyyMMddhhmm</B> della data di ultimo aggiornamento
   */
   public String getStringDataUltimoAggiornamento() {
          return dataUltAgg;
   }

   /**
    * Metodo che restituisce la data di ultimo aggiornamento
    * del profilo in formato Timestamp
    * 
    * @return Timestamp della data di ultimo aggiornamento
   */
   public Timestamp getTimeStampDataUltimoAggiornamento() {
	  	 try {
	  		 SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
	  		 java.util.Date date = sdf.parse(dataUltAgg);
	  		 return (new Timestamp(date.getTime()));
		 }
         catch(Exception e) {
             error="Profilo::getTimeStampDataUltimoAggiornamento()\n"+e.getMessage();
             return null;
         }
   }  
    
   /**
    * Metodo che restituisce la lista dei documenti
    * <B>a</B> cui il profilo ? riferito.
    * Da utilizzare esclusivamente dopo un accedi
    * 
    * @return Stringa dei riferimenti nel seguente formato:<BR>
    * 		  idDocRif<B>1</B>,TipoRif<B>1</B>@idDocRif<B>2</B>,
    * 		  TipoRif<B>2</B>@.......@idDocRif<B>n</B>,TipoRif<B>n</B>
   */
   public String getRiferimenti() {
          try {
            return ad.leggiRiferimenti();
          }
          catch(Exception e) 
          {
            error="Profilo::leggiRiferimenti()\n"+e.getMessage();
            return "";
          }
   }
 
   /**
    * Metodo che restituisce la lista dei documenti
    * <B>a</B> cui il profilo ? riferito. Filtrati solo 
    * per documenti della coppia (Area,TipoRelazione) dati in input. 
    * Da utilizzare esclusivamente dopo un accedi
    * 
    * @param area della lista dei riferimenti 
    * @param tipoRel Tipo Relazione della lista dei riferimenti 
    * 
    * @return Stringa dei riferimenti nel seguente formato:<BR>
    * 		  idDocRif<B>1</B>,TipoRif<B>1</B>@idDocRif<B>2</B>,
    * 		  TipoRif<B>2</B>@.......@idDocRif<B>n</B>,TipoRif<B>n</B>
   */
   public String getRiferimenti(String area,String tipoRel) {
          try {
            return ad.leggiRiferimenti(area,tipoRel);
          }
          catch(Exception e) 
          {
            error="Profilo::leggiRiferimenti(@,@)\n"+e.getMessage();
            return "";
          }
   }
   
   /**
    * Metodo che restituisce la lista dei documenti
    * <B>da</B> cui il profilo ? riferito.
    * Da utilizzare esclusivamente dopo un accedi
    * 
    * @return Stringa dei riferimenti nel seguente formato:<BR>
    * 		  idDocRif<B>1</B>,TipoRif<B>1</B>@idDocRif<B>2</B>,
    * 		  TipoRif<B>2</B>@.......@idDocRif<B>n</B>,TipoRif<B>n</B>
   */
   public String getRiferimentiFrom() {
          try {
            return ad.leggiRiferimentiFrom();
          }
          catch(Exception e) 
          {
             error="Profilo::leggiRiferimentiFrom()\n"+e.getMessage();
             return "";
          }
   }
   
   /**
    * Metodo che restituisce la lista dei documenti
    * <B>da</B> cui il profilo ? riferito, filtrati solo 
    * per documenti della coppia (Area,TipoRelazione) dati in input. 
    * Da utilizzare esclusivamente dopo un accedi
    * 
    * @param area della lista dei riferimenti 
    * @param tipoRel Tipo Relazione della lista dei riferimenti  
    * 
    * @return Stringa dei riferimenti nel seguente formato:<BR>
    * 		  idDocRif<B>1</B>,TipoRif<B>1</B>@idDocRif<B>2</B>,
    * 		  TipoRif<B>2</B>@.......@idDocRif<B>n</B>,TipoRif<B>n</B>
   */
   public String getRiferimentiFrom(String area,String tipoRel) {
          try {
            return ad.leggiRiferimentiFrom(area,tipoRel);
          }
          catch(Exception e) 
          {
             error="Profilo::leggiRiferimentiFrom(@,@)\n"+e.getMessage();
             return "";
          }
   }   
   
   /**
    * Metodo che restituisce l'hash table la cui chiave ?
    * l'idcartella che ha dei links verso il profilo instanziato.
    * Alla chiave idCartella corrisponde come valore l'iddocumento
    * profilo associato
   */
   public Hashtable getLinks() {
	      return getLinks(null,null);
   }
   
   public Hashtable getLinks(String area,String cm) {
	      try {
            return ad.leggiLinks(area,cm);
          }
          catch(Exception e) 
          {
             error="Profilo::getLinks("+area+","+cm+")\n"+e.getMessage();
             return null;
          }
   }

   /**
    * Metodo che in accoppiata con la funzione <a href="Profilo.html#successivoRiferimentoDocPrincipale()">successivoRiferimentoDocPrincipale</a>,
    * permette di leggere i riferimenti <B>a</B> cui il profilo ? riferito.
    * Sostituisce quindi la getRiferimenti() che i riferimenti li restituisce
    * in un unica stringa formattata.
    * Esempio di utilizzo:<BR>
    * <BR>
    * <B>import it.finmatica.dmServer.Riferimento;</B>
    * <BR> 
    * <BR>
    * <B>p.inizioLetturaRiferimentiDocPrincipale();</B><BR>
    * <BR>
    * do {<BR>
    * &nbsp;&nbsp;&nbsp;Riferimento r = p.successivoRiferimentoDocPrincipale()<BR>
    * &nbsp;&nbsp;&nbsp;...................<BR>
    * } while (r!=null)
    * 
    * @see <a href="Profilo.html#successivoRiferimentoDocPrincipale()">successivoRiferimentoDocPrincipale</a>
   */
   public void inizioLetturaRiferimentiDocPrincipale() {
          if (ad==null) {
             error="Profilo::inizioLetturaRiferimentiDocPrincipale()\nAttenzione! non ? stata eseguito l'accesso al documento";           
             return;
          }
         
          posRiferimento=0;
   }  
  
   /**
    * Metodo che in accoppiata con la funzione <a href="Profilo.html#inizioLetturaRiferimentiDocPrincipale()">inizioLetturaRiferimentiDocPrincipale</a>,
    * permette di leggere i riferimenti <B>a</B> cui il profilo ? riferito.
    * Sostituisce quindi la getRiferimenti() che i riferimenti li restituisce
    * in un unica stringa formattata.
    * Esempio di utilizzo:<BR>
    * <BR>
    * <B>import it.finmatica.dmServer.Riferimento;</B>
    * <BR> 
    * <BR>
    * p.inizioLetturaRiferimentiDocPrincipale();<BR>
    * <BR>
    * do {<BR>
    * &nbsp;&nbsp;&nbsp;Riferimento r = <B>p.successivoRiferimentoDocPrincipale()</B><BR>
    * &nbsp;&nbsp;&nbsp;...................<BR>
    * } while (r!=null)
    * 
    * @return Oggetto Riferimento da cui estrapolare le informazioni relative al
    * 		  profilo riferito
    * @see <a href="Profilo.html#inizioLetturaRiferimentiDocPrincipale()">inizioLetturaRiferimentiDocPrincipale</a>
   */  
   public Riferimento successivoRiferimentoDocPrincipale() {
          if (ad==null) {
             error="Profilo::successivoRiferimentoDocPrincipale()\nAttenzione! non ? stata eseguito l'accesso al documento";
             return null;            
          }
    
          try {
            Vector vRif = ad.getVectorRiferimenti();
            
            int size = vRif.size();
            if (posRiferimento>=size) return null;
            
            // *************  SONO COLLEGATO CON IL DOCUMENTALE DI HUMMINGBIRD *********************** //
            if (en.Global.DM.equals(en.Global.HUMMINGBIRD_DM)) {
                // TO BE IMPL.
                return null;
            }
            else {
                return (Riferimento)(vRif.elementAt(posRiferimento++));
            }

          }
          catch(Exception e) 
          {
            error="Profilo::successivoRiferimentoDocPrincipale()\n"+e.getMessage();
             return null;
          }
   }
  
   /**
    * Metodo che in accoppiata con la funzione <a href="Profilo.html#successivoRiferimentoDocRif()">successivoRiferimentoDocRif</a>,
    * permette di leggere i riferimenti <B>da</B> cui il profilo ? riferito.
    * Sostituisce quindi la getRiferimentiFrom() che i riferimenti li restituisce
    * in un unica stringa formattata.
    * Esempio di utilizzo:<BR>
    * <BR>
    * <B>import it.finmatica.dmServer.Riferimento;</B>
    * <BR> 
    * <BR>
    * <B>inizioLetturaRiferimentiDocRif();</B><BR>
    * <BR>
    * do {<BR>
    * &nbsp;&nbsp;&nbsp;Riferimento r = p.successivoRiferimentoDocRif()<BR>
    * &nbsp;&nbsp;&nbsp;...................<BR>
    * } while (r!=null)
    * 
    * @see <a href="Profilo.html#successivoRiferimentoDocRif()">successivoRiferimentoDocRif</a>
   */
   public void inizioLetturaRiferimentiDocRif() {
          if (ad==null) {
             error="Profilo::inizioLetturaRiferimentiDocRif()\nAttenzione! non ? stata eseguito l'accesso al documento";           
             return;
          }
         
          posRiferimentoFrom=0;
   }  
  
   /**
    * Metodo che in accoppiata con la funzione <a href="Profilo.html#inizioLetturaRiferimentiDocRif()">inizioLetturaRiferimentiDocRif</a>,
    * permette di leggere i riferimenti <B>da</B> cui il profilo ? riferito.
    * Sostituisce quindi la getRiferimentiFrom() che i riferimenti li restituisce
    * in un unica stringa formattata.
    * Esempio di utilizzo:<BR>
    * <BR>
    * <B>import it.finmatica.dmServer.Riferimento;</B>
    * <BR> 
    * <BR>
    * inizioLetturaRiferimentiDocRif();<BR>
    * <BR>
    * do {<BR>
    * &nbsp;&nbsp;&nbsp;Riferimento r = <B>p.successivoRiferimentoDocRif()</B><BR>
    * &nbsp;&nbsp;&nbsp;...................<BR>
    * } while (r!=null)
    * 
    * @return Oggetto Riferimento da cui estrapolare le informazioni relative al
    * 		  documento che riferisce il profilo
    * @see <a href="Profilo.html#inizioLetturaRiferimentiDocRif()">inizioLetturaRiferimentiDocRif</a>
   */  
   public Riferimento successivoRiferimentoDocRif() {
          if (ad==null) {
             error="Profilo::successivoRiferimentoDocRif()\nAttenzione! non ? stata eseguito l'accesso al documento";
             return null;            
          }
    
          try {
            Vector vRifFrom = ad.getVectorRiferimentiFrom();
            
            int size = vRifFrom.size();
            if (posRiferimentoFrom>=size) return null;
            
            // *************  SONO COLLEGATO CON IL DOCUMENTALE DI HUMMINGBIRD *********************** //
            if (en.Global.DM.equals(en.Global.HUMMINGBIRD_DM)) {
                // TO BE IMPL.
                return null;
            }
            else {
                return (Riferimento)(vRifFrom.elementAt(posRiferimentoFrom++));
            }

          }
          catch(Exception e) 
          {
              error="Profilo::successivoRiferimentoDocRif()\n"+e.getMessage();
              return null;
          }
   }
   
   /**
    * Metodo che restituisce la lista dei figli diretti discendenti
    * del profilo. Per figli si intende i record della tabella documenti
    * legati al documento principale attraverso la colonna 
    * id_documento_padre 
   */
   public Vector getListaFigli() {
          try {
            return ad.getListaFigli();
          }
          catch(Exception e) 
          {
             error="Profilo::getListaFigli()\n"+e.getMessage();
             return null;
          }
   }    
   
   /**
    * Metodo che restituisce la lista dei discendenti
    * del profilo. Per discendenti si intende i record della 
    * tabella documenti legati al documento principale attraverso 
    * la colonna id_documento_padre legati l'uno a l'altro in maniera
    * ricorsiva. 
   */
   public Vector getListaDiscendenti() {
          try {
            return ad.getListaDiscendenti();
          }
          catch(Exception e) 
          {
             error="Profilo::getListaDiscendenti()\n"+e.getMessage();
             return null;
          }
   }       
  
   /**
    * Metodo che restituisce il padre del profilo
    *  
    * @return identificativo del profilo padre
   */
   public String getPadre() {
          return idDocumentoPadre;
   }  

   /**
    * Metodo che restituisce la lista dei nomi degli allegati
    * del profilo divisi dal separatore passato in input
    * 
    * @param separator stringa separatrice dei nomi degli allegati 
    * @return lista degli allegati separati dal separatore
    * @see <a href="Profilo.html#getlistaFile()">getlistaFile</a>,&nbsp;
    * 	   <a href="Profilo.html#getListaFilesize()">getListaFilesize</a>  
   */
   public String getlistaFile(String separator) {
          StringBuffer sFile = new StringBuffer();
          try {
              int size = ad.listaOggettiFile().size();

              if (size==0) return "";
              if (size==1) return ((A_Oggetti_File)ad.listaOggettiFile().elementAt(0)).getFileName();      
        
              for(int i=0;i<size;i++) 
              {
                 if (i==size-1)
                    sFile.append(((A_Oggetti_File)ad.listaOggettiFile().elementAt(i)).getFileName());
                 else
                    sFile.append(((A_Oggetti_File)ad.listaOggettiFile().elementAt(i)).getFileName()+separator);
                   
              }

              return sFile.toString();
          }
          catch(Exception e) 
          {
             error="Profilo::listaFile()\n"+e.getMessage();
             return "";
          }
   }      

   /**
    * Metodo che restituisce la lista dei nomi degli allegati
    * del profilo divisi dal separatore "@"
    * 
    * @return lista degli allegati separati dal separatore "@"
    * @see <a href="Profilo.html#getlistaFile(java.lang.String)">getlistaFile(separator)</a>,&nbsp;
    * 	   <a href="Profilo.html#getListaFilesize()">getListaFilesize</a>  
   */
   public String getlistaFile() {
          return getlistaFile("@");
   }
   
   /**
    * Metodo che restituisce la lista degli id degli allegati
    * del profilo
    * 
    * @return lista degli id degli allegati
    * @see <a href="Profilo.html#getlistaFile()">getlistaFile</a>,&nbsp;
    * 	   <a href="Profilo.html#getListaFilesize()">getListaFilesize</a>  
   */
   public Vector<String> getlistaIdOggettiFile() throws Exception {
	   	  Vector<String> vRet = new Vector<String>();  
	   	  
	   	  if (ad==null) return vRet;
	   	  
          try {
              int size = ad.listaOggettiFile().size();

              for(int i=0;i<size;i++) vRet.add(((A_Oggetti_File)ad.listaOggettiFile().elementAt(i)).getIdOggettoFile());             

              return vRet;
          }
          catch(Exception e) 
          {
        	 throw new Exception("Profilo::getlistaIdOggettiFile()\n"+e.getMessage());     
          }
   }

    public Vector<String> getlistaNomiOggettiFile() throws Exception {
        Vector<String> vRet = new Vector<String>();

        if (ad==null) return vRet;

        try {
            int size = ad.listaOggettiFile().size();

            for(int i=0;i<size;i++) vRet.add(((A_Oggetti_File)ad.listaOggettiFile().elementAt(i)).getFileName());

            return vRet;
        }
        catch(Exception e)
        {
            throw new Exception("Profilo::getlistaIdOggettiFile()\n"+e.getMessage());
        }
    }

    /**
    * Metodo che restituisce la lista dei nomi degli allegati temporanei
    * del profilo divisi dal separatore passato in input
    * 
    * @param separator stringa separatrice dei nomi degli allegati temporanei
    * @return lista degli allegati temporanei separati dal separatore
    * @see <a href="Profilo.html#getlistaAllegatiTemp()">getlistaAllegatiTemp</a>  
   */
   public String getlistaAllegatiTemp(String separator) throws Exception {
	      if (idDocumento==null || idDocumento.equals("X")) 
	    	  throw new Exception("Profilo::getlistaAllegatiTemp\n. E' necessario accedere al profilo");
	      
	      return (new DocUtil(en)).listaAllegatiTemp(idDocumento,separator);
	    	  
   }    
   
   /**
    * Metodo che restituisce la lista dei nomi degli allegati temporanei
    * del profilo divisi dal separatore "@"
    * 
    * @return lista degli allegati temporanei separati dal separatore "@"
    * @see <a href="Profilo.html#getlistaAllegatiTemp(java.lang.String)">getlistaAllegatiTemp(separator)</a>
   */
   public String getlistaAllegatiTemp() throws Exception {
          return getlistaAllegatiTemp("@");
   }   
   
   /**
    * Metodo che restituisce la lista delle coppie (campo,valore) di
    * tutti i campi presenti nel modello.
    * Verr? restituito un vettore di classe keyval.
    * Per ogni oggetto di tipo keyval andr? poi interrogata la
    * propriet? "key" per i campi e "val" per i valori mediante le
    * apposite "get" presenti sulla classe 
    *  
    * @return lista delle coppie (campo,valore)
   */
   public Vector<keyval> getlistaValori() {
	      Vector<keyval> vValori = new Vector<keyval>();
	               
          try {
            for(int i=0;i<ad.listaValori().size();i++) {
            	GD4_Valori val=(GD4_Valori)ad.listaValori().get(i);
            	
            	vValori.add(new keyval(val.getCampo().getNomeCampo(),val.getValore()+""));
            }
          } 
          catch(Exception e) 
          {
            error="Profilo::getlistaValori()\n"+e.getMessage();            
          }
          
          return vValori;
   }       
   
   /**
    * Meotodo che aggiunge una competenza alla lista di
    * competenze da controllare e restituire mediante 
    * il metodo getListaCompetenze.
	*
	* @param type Utilizzare le costanti appropriate
	* 				Global.ABIL_*
   */
   public void addTypeAclReturn(String type) {
	      if (!hsListAclToRetrieve.contains(type))
	    	  hsListAclToRetrieve.add(type);	      
   }
   
   /**
    * Meotodo che rimuove una competenza dalla lista di
    * competenze da controllare e restituire mediante 
    * il metodo getListaCompetenze.
	*
	* @param type Utilizzare le costanti appropriate
	* 				Global.ABIL_*
   */   
   public void removeTypeAclReturn(String type) {
	   	  hsListAclToRetrieve.remove(type);
   }   
   
   /**
    * Metodo che restituisce la lista delle competenze per il profilo
    * Verr? restituito un vettore di classe ACL.
    * La lista sar? piena solo se ? stata fatta
    * precedentemente un accedi con il terzo parametro
    * settato a RETRIEVE_ALLACL_USER o a RETRIEVE_ALLACL_USERANDGROUP
    * 
    * Le competenze da controllare e da restituire dipendono dall'utilizzo
    * del metodo addTypeAclReturn e removeTypeAclReturn mediante i quali si
    * potranno aggiungere le competenze da restituire.
    * Di default viene controllata e restituita la sola LETTURA
    *  
    * @return lista delle ACL
   */   
   public Vector<ACL> getListaCompetenze() {
	      return getListaCompetenze(null);
   }      
   
   /**
    * Metodo che restituisce la lista delle competenze per il profilo
    * per l'utente passato in input
    * Verr? restituito un vettore di classe ACL.
    * La lista sar? piena solo se ? stata fatta
    * precedentemente un accedi con il terzo parametro
    * settato a RETRIEVE_ALLACL_USER o a RETRIEVE_ALLACL_USERANDGROUP
    * 
    * Le competenze da controllare e da restituire dipendono dall'utilizzo
    * del metodo addTypeAclReturn e removeTypeAclReturn mediante i quali si
    * potranno aggiungere le competenze da restituire.
    * Di default viene controllata e restituita la sola LETTURA
    * 
    * @param user Utente di cui si vogliono conoscere le competenze per questo profilo
    *  
    * @return lista delle ACL
   */     
   public Vector<ACL> getListaCompetenze(String user) {
	      Vector<ACL> vRet = new Vector<ACL>();
	      
	      if (ad==null) return vRet;
	      
	      HashMapSet hms = ad.getHmsACL();
	      
	      if (hms==null) return vRet;
	      
	      if (user!=null) {
	    	  Iterator i =  hms.getHashSet(user);

              if (i!=null) {
            	  while (i.hasNext()) vRet.add((ACL)i.next());
              }              
	      }
	      else {
	    	  vRet = hms.getAllHashSet();	    	 
	      }
	      
	      return vRet;
   }
   
   /**
    * Metodo che restituisce una HaskMapSet delle competenze per il profilo
    * La HasMap sar? pieno solo se ? stata fatta
    * precedentemente un accedi con il terzo parametro
    * settato a RETRIEVE_ALLACL_USER o a RETRIEVE_ALLACL_USERANDGROUP
    *  
    * Le competenze da controllare e da restituire dipendono dall'utilizzo
    * del metodo addTypeAclReturn e removeTypeAclReturn mediante i quali si
    * potranno aggiungere le competenze da restituire.
    * Di default viene controllata e restituita la sola LETTURA
    *  
    * Esempio java per poter usare l'HashMapSet per avere quindi
    * le competenze divise per utente:
    * 
    *   	   HashMapSet hms = pDocumento.getCompetenze();
          	   Iterator i =  hms.getIterator();
 		       
               while (i.hasNext()) {           	  
            	   String user=""+i.next(); //QUI MI RICAVO UTENTE
            	   
            	   Iterator iIntern = hms.getHashSet(user);
            	   while (iIntern.hasNext()) {    
            	       ACL = (ACL)iIntern.next() //QUI MI RICAVO ACL PER L'UTENTE 		              		  
            	   }
               }
    *  
    * @return HashMapSet delle ACL
   */     
   public HashMapSet getCompetenze() {
	   	  if (ad==null) return null;
	   	  
	   	  return ad.getHmsACL();
   }
   
   /**
    * Metodo che verifica una competenza per il profilo
    * per l'utente passato in input
    * La verifica funziona solo se ? stata fatta
    * precedentemente un accedi con il terzo parametro
    * settato a RETRIEVE_ALLACL_USER o a RETRIEVE_ALLACL_USERANDGROUP
    * 
    * @param user Utente di cui si vogliono conoscere le competenze per questo profilo
    * @param tipoCompetenza Pu? essere uno di questi valori:<BR>
    * 						Global.ABIL_LETT (Verifica lettura)<BR>
    * 						Global.ABIL_MODI (Verifica Modifica)<BR>
    * 						Global.ABIL_CANC (Verifica Cancellazione)<BR>
    * 						Global.ABIL_GEST (Verifica Gestione Competenze)<BR>
    *  
    * @return 0   - Non abilitato
    * 		  1   - Abilitato
    *         -1  - Lista delle competenze vuota. Non ? stato fatto l'accesso con il parametro corretto<BR> 
    *               (RETRIEVE_ALLACL_USER o a RETRIEVE_ALLACL_USERANDGROUP)
   */    
   public int verificaCompetenza(String user,String tipoCompetenza) {
	      if (ad==null) return -1;
	      
	      if (ad.getHmsACL()==null) return -1;
	      	    
	      Vector<ACL> vLista = getListaCompetenze(user);
	      
	      if (vLista.size()==0) return -1;
	      
	      for(int i=0;i<vLista.size();i++) {
	    	  if (vLista.get(i).getTipoCompetenza().equals(tipoCompetenza)) return vLista.get(i).getAccesso();
	      }
	      
	      return 0;
   }

   /**
    * Metodo che restituisce il numero degli allegati del profilo
    * 
    * @return Numero degli allegati del profilo
    * @see <a href="Profilo.html#getlistaFile(java.lang.String)">getlistaFile(Separator)</a>,&nbsp;
    * 	   <a href="Profilo.html#getlistaFile()">getlistaFile</a>  
   */
   public long getListaFilesize() {
          return ad.listaOggettiFile().size();
   }

   /**
    * Metodo che effettua il download dell'allegato il cui nome
    * viene passato come parametro di input.
    * Il file verr? scaricato sulla directory temporanea specificata
    * nel file di properties alla voce DIR_CLI_TEMP. Se non ? stata
    * specificata alcuna voce DIR_CLI_TEMP o non ? stato passato il 
    * file di properties, la directory di download sar? c:\windows\temp
    * 
    * @param nomeFile nome dell'allegato da scaricare
    * @return percorso del download del file (es: c:\windows\temp\allegato.doc)
    * @see <a href="Profilo.html#getFile(int)">getFile(Index)</a>  
   */
   public String getFile(String nomeFile) throws Exception {   
	   
          try {
            int i = getIndexFileByName(nomeFile);
              
            return getFile(i+1);                                                           
          }
          catch (Exception e) 
          {
            error="Profilo::getFile(String nomeFile)\n"+e.getMessage();
            return "";
          }              
   }

   /**
    * Metodo che effettua il download dell'allegato il cui indice (posizione 
    * nell'elenco di tutti gli allegati) viene passato come parametro di input.
    * Il file verr? scaricato sulla directory temporanea specificata
    * nel file di properties alla voce DIR_CLI_TEMP. Se non ? stata
    * specificata alcuna voce DIR_CLI_TEMP o non ? stato passato il 
    * file di properties, la directory di download sar? c:\windows\temp
    * 
    * @param index indice dell'allegato da scaricare.<B>L'indice si considera partente 
    * 	     dalla posizione uno</B>
    * @return percorso del download del file (es: c:\windows\temp\allegato.doc)
    * @see <a href="Profilo.html#getFile(java.lang.String)">getFile(fileName)</a>  
   */   
   public String getFile(int index) throws Exception {
          try {
           if (index>ad.listaOggettiFile().size()) {
             error="Profilo::getFile() - index of list file out range";          
             throw new Exception(error);
           }
           
           ad.connect();
           
           //Se il file non ? stato caricato (ACCESS_NO_ATTACH), lo carico "al volo"
           if ((((A_Oggetti_File)ad.listaOggettiFile().elementAt(index-1)).getFile())==null)
                ad.caricaOggettoFile(((A_Oggetti_File)ad.listaOggettiFile().elementAt(index-1)).getIdOggettoFile());          
                     
           LetturaScritturaFileFS f = new LetturaScritturaFileFS(en.Global.DIR_CLI_TEMP+"\\"+((A_Oggetti_File)ad.listaOggettiFile().elementAt(index-1)).getFileName());
                     
           f.scriviFile((InputStream)((A_Oggetti_File)ad.listaOggettiFile().elementAt(index-1)).getFile());
           
           ad.disconnect();
           
           return en.Global.DIR_CLI_TEMP+"\\"+((A_Oggetti_File)ad.listaOggettiFile().elementAt(index-1)).getFileName();
          }
          catch(Exception e) 
          {
        	e.printStackTrace();
            error="Profilo::getFile()\n"+e.getMessage();
            throw new Exception(error);
          }
   }
   
   /**
    * Metodo che effettua il download del primo allegato 
    * (rispetto alla lista degli allegati) del profilo.
    * Il file verr? scaricato sulla directory temporanea specificata
    * nel file di properties alla voce DIR_CLI_TEMP. Se non ? stata
    * specificata alcuna voce DIR_CLI_TEMP o non ? stato passato il 
    * file di properties, la directory di download sar? c:\windows\temp
    * 
    * @return percorso del download del file (es: c:\windows\temp\allegato.doc)
    * @see <a href="Profilo.html#getFile(int)">getFile(index)</a>  
   */   
   public String getFile() throws Exception {
          return getFile(1);
   }

   /**
    * Metodo che restituisce l'identificativo (di Tabella Oggetti_File) dell'allegato
    * di cui viene passato il nome.
    * 
    * @param nomeFile Nome dell'allegato da cui estrapolare l'identificativo
    * @return identificativo dell'allegato
   */
   public String getIdFile(String nomeFile) throws Exception {         
         try {
           int i = getIndexFileByName(nomeFile);
              
           return ((A_Oggetti_File)ad.listaOggettiFile().elementAt(i)).getIdOggettoFile();                                                           
         }
         catch (Exception e) 
         {
           error="Profilo::getIdFile(String nomeFile)\n"+e.getMessage();
           return "";
         }              
   }   

   /**
    * Metodo che restituisce il nome dell'allegato dato l'indice nella lista
    * dei file del profilo
    * 
    * @param index indice dell'allegato nella lista degli allegati.
    * 			   <B>L'indice si considera partente dalla posizione uno</B>
    * @return nome dell'allegato
   */
   public String getFileName(int index) throws Exception {
          int size = ad.listaOggettiFile().size();
         
          try {
            if (index>size) {
               error="index of list file out range";          
               throw new Exception(error);
            }           
           
            return ((A_Oggetti_File)ad.listaOggettiFile().elementAt(index-1)).getFileName();                                                     
          } 
          catch(Exception e) {
            error="Profilo::getFileName()\n"+e.getMessage();
            throw new Exception(error);
          }
   }
   
   /**
    * Metodo che restituisce la data di aggiornamento dell'allegato dato l'indice nella lista
    * dei file del profilo
    * 
    * @param index indice dell'allegato nella lista degli allegati.
    * 			   <B>L'indice si considera partente dalla posizione uno</B>
    * @return data di aggiornamento dell'allegato restituita come stringa nel formato dd/mm/yyyy hh:mm:ss
   */
   public String getUpdateDateFile(int index) throws Exception {
          int size = ad.listaOggettiFile().size();
         
          try {
            if (index>size) {
               error="index of list file out range";          
               throw new Exception(error);
            }           
           
            return ((A_Oggetti_File)ad.listaOggettiFile().elementAt(index-1)).getDataAggiornamento();                                                     
          } 
          catch(Exception e) {
            error="Profilo::getFileName()\n"+e.getMessage();
            throw new Exception(error);
          }
   }   
   
   /**
    * Metodo che restituisce la data di aggiornamento dell'allegato dato l'id dell'allegato
    * dei file del profilo
    * 
    * @param idOggettoFile id dell'oggetto file
    * 			   <B>L'indice si considera partente dalla posizione uno</B>
    * @return data di aggiornamento dell'allegato restituita come stringa nel formato dd/mm/yyyy hh:mm:ss
   */
   public String getUpdateDateFile(long idOggettoFile) throws Exception {
		   int size = ad.listaOggettiFile().size();
	      
	       try {
	     	for(int i=0;i<size;i++) {
	     		A_Oggetti_File obj = (A_Oggetti_File)ad.listaOggettiFile().get(i);
	     		
	     		if (obj.getIdOggettoFile().equals(""+idOggettoFile)) return getUpdateDateFile(i+1);        			
	     	}                                                                                        
	       } 
	       catch(Exception e) {
	         error="Profilo::getUpdateDateFile(idOggettoFile)\n"+e.getMessage();
	         throw new Exception(error);
	       }
	       
	       throw new Exception("Profilo::getUpdateDateFile(idOggettoFile)\n Non ? stato trovato il file per idOggettoFile="+idOggettoFile);
   }     
   
   /**
    * Metodo che restituisce il nome dell'allegato dato l'id dell'oggettoFile
    * (null se non esiste o l'id passato non ? fra quelli contenuti nel profilo)
    * 
    * @param idOggettoFile id dell'oggetto file
    * @return nome dell'allegato
   */
   public String getFileName(long idOggettoFile) throws Exception {
          int size = ad.listaOggettiFile().size();
         
          try {
        	for(int i=0;i<size;i++) {
        		A_Oggetti_File obj = (A_Oggetti_File)ad.listaOggettiFile().get(i);
        		
        		if (obj.getIdOggettoFile().equals(""+idOggettoFile)) return obj.getFileName();        			
        	}                                                                                        
          } 
          catch(Exception e) {
            error="Profilo::getFileName(idOggettoFile)\n"+e.getMessage();
            throw new Exception(error);
          }
          
          return null;
   }   
   
   /**
    * Metodo che restituisce il nome dell'allegato P7M
    * dato il nome del file padre.
    * 
    * @param nomeFile nome dell'allegato di cui ricercare il suo P7M
    * @return Nome dell'allegato P7m
    * @see <a href="Profilo.html#getFileName(int)">getFileName(index)</a>
   */
   public String getFileNameP7M(String nomeFile) throws Exception {         
          try {
            int i = getIndexP7MFileByFileNamePadre(nomeFile);

            if (i!=-1)
               return this.getFileName(i+1);
            else
               return null;
          }
          catch (Exception e) 
          {
            error="Profilo::getFileNameP7M(String nomeFile)\n"+e.getMessage();
            throw new Exception(error);
          }              
   }   
   
   /**
    * Metodo che restituisce l'InputStream dell'allegato del profilo
    * dato l'indice nella lista dei file
    * 
    * @param index indice dell'allegato nella lista degli allegati.
    * 			   <B>L'indice si considera partente dalla posizione uno</B>
    * @return InputStream dell'allegato
    * @see <a href="Profilo.html#getFileStream(java.lang.String)">getFileStream(fileName)</a>
   */   
   public InputStream getFileStream(int index) throws Exception {       
          try {
            if (index>ad.listaOggettiFile().size()) {
               error="Profilo::getFileStream() - Si sta cercando di accedere al file n? "+index+". Il file non esiste sul documento!!";          
               throw new Exception(error);
            }
            
            ad.connect();

            InputStream iFile= null;
            try {
                iFile=((InputStream)((A_Oggetti_File)ad.listaOggettiFile().elementAt(index-1)).getFile());
            }
            catch (NullPointerException np) {
                try {
                    iFile.close();
                }
                catch (Exception ei) {

                }
                iFile=null;
            }
            
            //Se il file non ? stato caricato (ACCESS_NO_ATTACH), lo carico "al volo"
            if (iFile == null) {
                ad.caricaOggettoFile(((A_Oggetti_File)ad.listaOggettiFile().elementAt(index-1)).getIdOggettoFile());
                iFile= ((InputStream)((A_Oggetti_File)ad.listaOggettiFile().elementAt(index-1)).getFile());
            }


            try {

              iFile.available();
            }
            catch(java.io.IOException ioe) {
              if (ioe.getMessage().toLowerCase().indexOf("stream closed")!=-1) {
            	  //Lo ricarico            	  
            	  ad.caricaOggettoFile(((A_Oggetti_File)ad.listaOggettiFile().elementAt(index-1)).getIdOggettoFile());
            	  iFile = ((InputStream)((A_Oggetti_File)ad.listaOggettiFile().elementAt(index-1)).getFile());
              }
              else
            	 throw new Exception(ioe);
            }
            //System.out.println("MANNY--->4");         
            return iFile;
          }
          catch(Exception e) 
          {
        	//  System.out.println("MANNY--->5");
        	//  e.printStackTrace();
            error="Profilo::getFileStream(int index)\n"+e.getMessage();
            throw new Exception(error);
          }
   }

   /**
    * Metodo che restituisce un array di byte contenente il file
    * pdf (fileName) del profilo modificato nel suo footer.
    * In basso viene aggiunto il testo recuperato tramite
    * l'istruzione SQL messa nel campo "istruzione" del modello
    * a cui appartiene il profilo.
    *
    * 
    * @return byte[] contenente il PDF modificato nel footer
    * @see <a href="Profilo.html#getPdfFooter(int)">getPdfFooter(index)</a>
   */    
   public byte[] getPdfFooter(String fileName) throws Exception {
	      return getPdfFooter(fileName,-1);
   }

   /**
    * Metodo che restituisce un array di byte contenente il file
    * pdf (di indice <index>) del profilo modificato nel suo footer.
    * In basso viene aggiunto il testo recuperato tramite
    * l'istruzione SQL messa nel campo "istruzione" del modello
    * a cui appartiene il profilo.
    * 
    * @param index indice del file allegato PDF del profilo
    * 
    * @return byte[] contenente il PDF modificato nel footer
    * @see <a href="Profilo.html#getPdfFooter(java.lang.String)">getPdfFooter(fileName)</a>
   */    
   public byte[] getPdfFooter(int index) throws Exception {
	      return getPdfFooter(null,index);
   }   
   
   private byte[] getPdfFooter(String fileName,int index) throws Exception {	       
	      InputStream iFile;
	      String      nameFile;
	   
	      if (ad==null) {
	      	  error="Profilo::getPdfFooter - Impossibile prodecere! E' necessario accedere prima al Profilo\n";
              throw new Exception(error); 
          }
	      
	      //Recupero NomeFile ed InputStream dall'indice
	      if (index!=-1) {
	    	  try {
	    	    nameFile=this.getFileName(index);
	    	  }
	    	  catch(Exception e) {
	    		error="Profilo::getPdfFooter - Errore nella lettura dello Nome File\n"+e.getMessage();
                throw new Exception(error); 
	    	  }
	    	  
	    	  //Controllo che sia un PDF
	    	  /*if (!Global.lastTrim(nameFile,".",en.Global.WEB_SERVER_TYPE).toUpperCase().equals("PDF")) {
	    		 error="Profilo::getPdfFooter - Il file richiesto non ? un PDF! Impossibile continuare.\n";
                 throw new Exception(error); 
	    	  }*/
	    	  
	    	  try {
	    	    iFile=this.getFileStream(index);
	    	  }
	    	  catch(Exception e) {
	    		error="Profilo::getPdfFooter - Errore nella lettura dello Stream\n"+e.getMessage();
                throw new Exception(error); 
	    	  }
	    	  
	    	  if (!Global.lastTrim(nameFile,".",en.Global.WEB_SERVER_TYPE).toUpperCase().equals("PDF") &&
	    		  !Global.lastTrim(nameFile,".",en.Global.WEB_SERVER_TYPE).toUpperCase().equals("RTF")) {	    		
	    		  ByteArrayOutputStream outPDF = new ByteArrayOutputStream();
	    		  for (int n; (n = iFile.read()) != -1;) 	    			  
	    			  outPDF.write((byte)n);
	    		  
	    		  ad.disconnect();
	    		  return outPDF.toByteArray();
	    	  }   	    	  
	      }
	      //Ho gi? il NomeFile,
	      else {
	    	  nameFile=fileName;
	    	  
	    	  //Controllo che sia un PDF
	    	  /*if (!Global.lastTrim(nameFile,".",en.Global.WEB_SERVER_TYPE).toUpperCase().equals("PDF")) {
	    		 error="Profilo::getPdfFooter - Il file richiesto non ? un PDF! Impossibile continuare.\n";
                 throw new Exception(error); 
	    	  }*/
	    	  
	    	  try {
	    	    iFile=this.getFileStream(nameFile);
	    	  }
	    	  catch(Exception e) {
	    		error="Profilo::getPdfFooter - Errore nella lettura dello Stream\n"+e.getMessage();
                throw new Exception(error); 
	    	  }	  
	    	  
	    	  if (!Global.lastTrim(nameFile,".",en.Global.WEB_SERVER_TYPE).toUpperCase().equals("PDF") &&
	    		  !Global.lastTrim(nameFile,".",en.Global.WEB_SERVER_TYPE).toUpperCase().equals("RTF")) {	    		
	    		  ByteArrayOutputStream outPDF = new ByteArrayOutputStream();
	    		  for (int n; (n = iFile.read()) != -1;) 	    			  
	    			  outPDF.write((byte)n);
	    		  
	    		  ad.disconnect();
	    		  return outPDF.toByteArray();
	    	  }  
	      }
	      
	      //Leggo il testo
	      String testo=(new LookUpDMTable(en)).lookUpIstruzioneTestoFooterPDFModello(this.getDocNumber());	      //
	      	      
	      byte[] ret;
	      
	      if (Global.lastTrim(nameFile,".",en.Global.WEB_SERVER_TYPE).toUpperCase().equals("PDF"))
	    	  ret=(new PdfUtil(iFile,testo)).getPdfFooter();
	      else 
	          ret=(new RtfUtil(iFile,testo,ad.aDocumento.getIdDocumento())).getRtfFooter();
	      
	      ad.disconnect();
	      
	      return ret;
   }

   /**
    * Metodo che restituisce l'InputStream dell'allegato del profilo
    * dato il nome del file
    * 
    * @param nomeFile nome dell'allegato
    * @return InputStream dell'allegato
    * @see <a href="Profilo.html#getFileStream(int)">getFileStream(index)</a>
   */
   public InputStream getFileStream(String nomeFile) throws Exception {         
          try {
            int i = getIndexFileByName(nomeFile);

            return getFileStream(i+1);                                                           
          }
          catch (Exception e) 
          {
            error="Profilo::getFileStream(String nomeFile)\n"+e.getMessage();
            throw new Exception(error);
          }              
   }     

   /**
    * Metodo che restituisce l'InputStream dell'allegato di sistema del profilo
    * dato il nome del file ed il tipo di SystemFile ricercato
    * 
    * @param nomeFile nome dell'allegato
    * @param tipoFileSys Tipo di file di sistema ricercato. Valori possibili:<BR>
    *        Global.SYS_HASH (File impronta)<BR>
    *        Global.SYS_PDF
    * @return InputStream dell'allegato
    * @see <a href="Profilo.html#getFileStream(int)">getFileStream(index)</a>
   */
   public InputStream getSystemFileStream(String nomeFile,int tipoFileSys) throws Exception {         
          try {
            int i = getIndexSysFileByName(nomeFile,tipoFileSys);

            return getFileStream(i+1);                                                           
          }
          catch (Exception e) 
          {
            error="Profilo::getSystemFileStream(String nomeFile,int tipoFileSys)\n"+e.getMessage();
            throw new Exception(error);
          }              
   }   

   /**
    * Metodo che restituisce l'InputStream dell'allegato P7M
    * dato il nome del file padre.
    * 
    * @param nomeFile nome dell'allegato di cui ricercare il suo P7M
    * @return InputStream dell'allegato
    * @see <a href="Profilo.html#getFileStream(int)">getFileStream(index)</a>
   */
   public InputStream getP7MFileStream(String nomeFile) throws Exception {         
          try {
            int i = getIndexP7MFileByFileNamePadre(nomeFile);

            if (i!=-1)
               return getFileStream(i+1);
            else
               return null;
          }
          catch (Exception e) 
          {
            error="Profilo::getP7MFileStream(String nomeFile)\n"+e.getMessage();
            throw new Exception(error);
          }              
   }      

   public void settaArea(String sArea) {
          area=sArea;
   }    
   
   public void setCodiceModello(String codMod) {
          tipoDocumento = codMod;
   }

   public void setDocNumber(String idDoc) {
          idDocumento = idDoc;
   }      
   
   public void setAggiornaDataUltAggiornamento(boolean bFlag) {
	      bAggiornaDataUltAgg=bFlag;
   }
   
   public void setSkipUnknowField(boolean bFlag) {
	   	  setBSkipUnknowField=bFlag; 
   }
   
   public void addSkipunknowField(String fieldName) {
	   	  listSkipUnknowField.add(fieldName);
   }   
   
   /**
    * Il metodo fa si che se la variabile viene
    * settata a true, quando il profilo
    * verr? aggiornato questo salter? il controllo
    * di "Documento aggiornato da altro utente"
   */   
   public void setSkipBusyControl(boolean bFlag) {
	   	  setSkipBusyControl=bFlag; 
   }   
   
   /**
    * Il metodo fa si che quando il profilo
    * viene aggiornato e si trova in uno stato
    * iniziale di PRE_BOZZA, lo stato rimanga tale
    * anche dopo il salvataggio.
   */
   public void forcePreBozza() {
	      forceMaintaninPreBozza=true;
   }

   /**
    * Metodo che aggiunge, alla lista dei valori da inseire
    * nel profilo, la coppia (campo,valore).
    * Da utilizzare esclusivamente in Inserimento/Aggiornamento
    * del profilo.<BR>
    * Questo metodo pu? essere utilizzato per inserire valori
    * su campi di qualunque tipo (stringa, data, numero): verr?
    * fatta la conversione automatica del tipo in funzione del
    * tipo di campo in funzione del modello del profilo.
    * 
    * Il valore sar? "appeso" al valore gi? presente nel campo
    * del profilo se questo esiste gi?.   
    * 
    * @param campo nome del campo di cui aggiungere/modificare il valore
    * @param valore valore da aggiungere/modificare in formato Stringa
   */
   public void appendiValore(String campo, String valore) {  
          settaValoreInterna(campo,valore,true);
   }   
   
   /**
    * Metodo che aggiunge, alla lista dei valori da inseire
    * nel profilo, la coppia (campo,valore).
    * Da utilizzare esclusivamente in Inserimento/Aggiornamento
    * del profilo.<BR>
    * Questo metodo pu? essere utilizzato per inserire valori
    * su campi di qualunque tipo (stringa, data, numero): verr?
    * fatta la conversione automatica del tipo in funzione del
    * tipo di campo in funzione del modello del profilo.
    * Esempio:<BR>
    * <BR>
    * p.settaValore("COGNOME","ROSSI"); //campo COGNOME per il profilo ? stringa-> 
    * 									  Nessuna conversione
    * <BR>
    * p.settaValore("N_REG","14"); //campo N_REG per il profilo ? numerico-> 
    * 								 Conversione di 14 in numerico
    * <BR>
    * p.settaValore("DATA","01/01/2006"); //campo DATA per il profilo ? data-> 
    * 									    Conversione di 01/01/2006 in data con
    * 										formato standard dd/mm/yyyy
    * <BR><BR>
    * Se non ? stato possibile effettuare la conversione verr? restituito
    * un errore in fase di salvataggio del profilo 
    * 
    * @param campo nome del campo di cui aggiungere/modificare il valore
    * @param valore valore da aggiungere/modificare in formato Stringa
    * @see <a href="Profilo.html#settaValore(java.lang.String, java.sql.Date)">settaValore(String,Date)</a>,&nbsp;
    * 	   <a href="Profilo.html#settaValore(java.lang.String, java.sql.Timestamp)">settaValore(String,Timestamp)</a>,&nbsp;
    * 	   <a href="Profilo.html#settaValore(java.lang.String, java.math.BigDecimal)">settaValore(String,BigDecimal)</a>
   */
   public void settaValore(String campo, String valore) {  
          settaValoreInterna(campo,valore,false);
   }

   /**
    * Metodo che aggiunge, alla lista dei valori da inserire
    * nel profilo, la coppia (campo,valore).
    * Da utilizzare esclusivamente in <B>Inserimento/Aggiornamento
    * del profilo.</B>
    * Questo metodo pu? essere utilizzato per inserire valori
    * esclusivamente su campi di tipo Data. In caso contrario verr?
    * restituito un errore.
    * 
    * @param campo nome del campo di cui aggiungere/modificare il valore
    * @param valore valore da aggiungere/modificare in formato Date
    * @see <a href="Profilo.html#settaValore(java.lang.String, java.lang.String)">settaValore(String,String)</a>,&nbsp;
    * 	   <a href="Profilo.html#settaValore(java.lang.String, java.sql.Timestamp)">settaValore(String,Timestamp)</a>,&nbsp;
    * 	   <a href="Profilo.html#settaValore(java.lang.String, java.math.BigDecimal)">settaValore(String,BigDecimal)</a>
   */
   public void settaValore(String campo, java.sql.Date valore) {           
          settaValoreInterna(campo,valore,false);
   }   
  
   /**
    * Metodo che aggiunge, alla lista dei valori da inserire
    * nel profilo, la coppia (campo,valore).
    * Da utilizzare esclusivamente in <B>Inserimento/Aggiornamento
    * del profilo.</B>
    * Questo metodo pu? essere utilizzato per inserire valori
    * esclusivamente su campi di tipo Data. In caso contrario verr?
    * restituito un errore.
    * 
    * @param campo nome del campo di cui aggiungere/modificare il valore
    * @param valore valore da aggiungere/modificare in formato TimeStamp
    * @see <a href="Profilo.html#settaValore(java.lang.String, java.lang.String)">settaValore(String,String)</a>,&nbsp;
    * 	   <a href="Profilo.html#settaValore(java.lang.String, java.sql.Date)">settaValore(String,Date)</a>,&nbsp;
    * 	   <a href="Profilo.html#settaValore(java.lang.String, java.sql.BigDecimal)">settaValore(String,BigDecimal)</a>
   */   
   public void settaValore(String campo, java.sql.Timestamp valore) {           
          settaValoreInterna(campo,valore,false);
   }  

   /**
    * Metodo che aggiunge, alla lista dei valori da inserire
    * nel profilo, la coppia (campo,valore).
    * Da utilizzare esclusivamente in <B>Inserimento/Aggiornamento
    * del profilo.</B>
    * Questo metodo pu? essere utilizzato per inserire valori
    * esclusivamente su campi di tipo Numerico. In caso contrario verr?
    * restituito un errore.
    * 
    * @param campo nome del campo di cui aggiungere/modificare il valore
    * @param valore valore da aggiungere/modificare in formato TimeStamp
    * @see <a href="Profilo.html#settaValore(java.lang.String, java.lang.String)">settaValore(String,String)</a>,&nbsp;
    * 	   <a href="Profilo.html#settaValore(java.lang.String, java.sql.Date)">settaValore(String,Date)</a>,&nbsp;
    * 	   <a href="Profilo.html#settaValore(java.lang.String, java.sql.Timestamp)">settaValore(String,Timestamp)</a>
   */   
   public void settaValore(String campo, java.math.BigDecimal valore) {           
          settaValoreInterna(campo,valore,false);
   }

   /**
    * Metodo che aggiunge, alla lista dei riferimenti da inserire
    * nel profilo, la coppia (identificativo del documento da riferire,tipo di relazione).
    * Da utilizzare esclusivamente in <B>Inserimento/Aggiornamento
    * del profilo.</B>
    * 
    * @param idDocRif identificativo del documento da riferire al profilo
    * @param typeRiferimento tipo di Relazione		 
    * @see <a href="Profilo.html#setDeleteRiferimento(java.lang.String, java.lang.String)">setDeleteRiferimento(String,String)</a> 	   
   */   
   public void settaRiferimento(String idDocRif,String typeRiferimento) {
          docRiferimenti.addElement(idDocRif);
          typeRiferimenti.addElement(typeRiferimento);
   }
   
   /**
    * Metodo che se impostato a true (di default ? false),
    * in inserimento di qualsiasi riferimento, nella tabella
    * riferimenti, controlla prima se la chiave passata
    * esiste gi? e in tal caso invece di provare ad inserire
    * la stessa chiave (che restituirebbe errore), salta
    * l'inserimento.</B>
    * 
    * @param bDontRepeatExistsRif 	  	   
   */   
   public void setDontRepeatExistsRif(boolean bDontRepeatExistsRif) {
	      this.bDontRepeatExistsRif = bDontRepeatExistsRif;
   }

   /**
    * Metodo che aggiunge, alla lista dei riferimenti da eliminare
    * nel profilo, la coppia (identificativo del documento da riferire,tipo di relazione).
    * Da utilizzare esclusivamente in <B>Aggiornamento del profilo</B>.
    * 
    * @param idDocRif identificativo del documento da eliminare come riferimento al profilo
    * @param typeRiferimento tipo di Relazione		 
    * @see <a href="Profilo.html#settaRiferimento(java.lang.String, java.lang.String)">settaRiferimento(String,String)</a> 	   
   */   
   public void setDeleteRiferimento(String idDocRif,String typeRiferimento) {
          docRiferimentiToDelete.addElement(idDocRif);
          typeRiferimentiToDelete.addElement(typeRiferimento);
   }  
   
   /**
    * Metodo che aggiunge, alla lista degli allegati da inserire
    * nel profilo, la coppia (path del file,nome del file padre).
    * Da utilizzare esclusivamente in <B>Aggiornamento del profilo</B>.<BR>
    * Il path del file verr? utilizzato anche per stabilire quale nome dare
    * all'allegato.<BR>
    * Il nome del file padre lo si passa supponendo che questo faccia gi?
    * parte degli allegati del documento.<BR>
    * Esempio:<BR>
    * <BR>
    * p.setFileName("c:\file\allegato.doc","padreAllegato.doc");<BR>
    * <BR>
    * Verr? aggiunto un allegato con nome "allegato.doc" che ha per padre
    * il file con nome "padreAllegato.doc", il quale dovr? esistere come
    * allegato del profilo.
    * <BR>
    * Se siamo in presenza di un aggiornamento del documento ed il nome
    * del file esiste gi? fra la lista degli allegati, questi verr?
    * aggiornato nel suo contenuto
    * 
    * @param sPath percorso del file da allegare
    * @param sNameFilePadre nome dell'allegato padre
    * @see <a href="Profilo.html#setFileName(java.lang.String)">setFileName(String)</a>,&nbsp; 
    *      <a href="Profilo.html#setFileName(java.lang.String, java.io.InputStream)">setFileName(String,InputStream)</a>,&nbsp;	   
    *      <a href="Profilo.html#setFileName(java.lang.String, java.io.InputStream,java.lang.String)">setFileName(String,InputStream,String)</a>
   */   
   public void setFileName(String sPath, String sNameFilePadre) throws Exception {
          setFileName(sPath,"",IMPRONTA_OFF,SETTA_IMPRONTA,sNameFilePadre,null);
   }

   /**
    * Metodo che aggiunge il file passato in input alla lista degli allegati 
    * da inserire nel profilo.
    * Da utilizzare esclusivamente in <B>Inserimento / Aggiornamento del profilo</B>.<BR>
    * Il path del file verr? utilizzato anche per stabilire quale nome dare
    * all'allegato.<BR>
    * Esempio:<BR>
    * <BR>
    * p.setFileName("c:\file\allegato.doc");<BR>
    * <BR>
    * Verr? aggiunto un allegato con nome "allegato.doc".
    * Se siamo in presenza di un aggiornamento del documento ed il nome
    * del file esiste gi? fra la lista degli allegati, questi verr?
    * aggiornato nel suo contenuto
    * 
    * @param sPath percorso del file da allegare
    * @see <a href="Profilo.html#setFileName(java.lang.String, java.lang.String)">setFileName(String,String)</a>,&nbsp; 
    *      <a href="Profilo.html#setFileName(java.lang.String, java.io.InputStream)">setFileName(String,InputStream)</a>,&nbsp;	   
    *      <a href="Profilo.html#setFileName(java.lang.String, java.io.InputStream, java.lang.String)">setFileName(String,InputStream,String)</a>
   */
   public void setFileName(String sPath) throws Exception {
          setFileName(sPath,"",IMPRONTA_OFF,SETTA_IMPRONTA,null,null);
   }

   /**
    * Metodo che aggiunge il file passato in input (InputStream) alla lista degli allegati 
    * da inserire nel profilo.
    * Da utilizzare esclusivamente in <B>Inserimento / Aggiornamento del profilo</B>.<BR>
    * Il nome del file verr? utilizzato per stabilire quale nome dare
    * all'allegato mentre l'InputStream servir? per "scrivere" fisicamente il file
    * sulla colonna del database.<BR>
    * Esempio:<BR>
    * <BR>
    * p.setFileName("allegato.doc",is);<BR>
    * <BR>
    * Verr? aggiunto un allegato con nome "allegato.doc". La colonna di database
    * sar? riempita con il contenuto dell'InputStream
    * Se siamo in presenza di un aggiornamento del documento ed il nome
    * del file esiste gi? fra la lista degli allegati, questi verr?
    * aggiornato nel suo contenuto
    * 
    * @param sNomeFile nome del file da allegare
    * @param file InputStream con il contenuto dell'allegato da aggiungere
    * @see <a href="Profilo.html#setFileName(java.lang.String, java.lang.String)">setFileName(String,String)</a>,&nbsp; 
    *      <a href="Profilo.html#setFileName(java.lang.String)">setFileName(String)</a>,&nbsp;	   
    *      <a href="Profilo.html#setFileName(java.lang.String, java.io.InputStream, java.lang.String)">setFileName(String,InputStream,String)</a>
   */
   public void setFileName(String sNomeFile, InputStream file) throws Exception {
          setFileName(sNomeFile,"",IMPRONTA_OFF,SETTA_IMPRONTA,null,file);
   }  
   
   /**
    * Metodo che aggiunge il file passato in input (InputStream) alla lista degli allegati 
    * da inserire nel profilo.
    * Da utilizzare esclusivamente in <B>Inserimento / Aggiornamento del profilo</B>.<BR>
    * Il nome del file verr? utilizzato per stabilire quale nome dare
    * all'allegato mentre l'InputStream servir? per "scrivere" fisicamente il file
    * sulla colonna del database.<BR>
    * Esempio:<BR>
    * <BR>
    * p.setFileName("allegato.doc","application/vnd.ms-word.document.12",is);<BR>
    * <BR>
    * Verr? aggiunto un allegato con nome "allegato.doc". La colonna di database
    * sar? riempita con il contenuto dell'InputStream
    * Se siamo in presenza di un aggiornamento del documento ed il nome
    * del file esiste gi? fra la lista degli allegati, questi verr?
    * aggiornato nel suo contenuto
    * 
    * @param sNomeFile nome del file da allegare
    * @param contentType content type del file che sar? utilizzato per dedurre l'estenz. del file
    * @param file InputStream con il contenuto dell'allegato da aggiungere
    * @see <a href="Profilo.html#setFileName(java.lang.String, java.lang.String)">setFileName(String,String)</a>,&nbsp; 
    *      <a href="Profilo.html#setFileName(java.lang.String)">setFileName(String)</a>,&nbsp;	   
    *      <a href="Profilo.html#setFileName(java.lang.String, java.io.InputStream, java.lang.String)">setFileName(String,InputStream,String)</a>
   */
   public void setFileName(String sNomeFile, String contentType, InputStream file) throws Exception {
          setFileName(sNomeFile,"",IMPRONTA_OFF,SETTA_IMPRONTA,null,file,contentType);
   }   
    
   /**
    * Metodo che aggiunge il file passato in input (InputStream) alla lista degli allegati 
    * da inserire nel profilo.
    * Da utilizzare esclusivamente in <B>Inserimento / Aggiornamento del profilo</B>.<BR>
    * Il nome del file verr? utilizzato per stabilire quale nome dare
    * all'allegato mentre l'InputStream servir? per "scrivere" fisicamente il file
    * sulla colonna del database. Il file sar? automaticamente aggiunto come figlio
    * dell'allegato corrispondente al nomeFile passato come terzo parametro<BR>
    * Esempio:<BR>
    * <BR>
    * p.setFileName("allegato.doc",is,"padreAllegato.doc");<BR>
    * <BR>
    * Verr? aggiunto un allegato con nome "allegato.doc". La colonna di database
    * sar? riempita con il contenuto dell'InputStream. Allegato.doc diventer?
    * figlio dell'allegato passato come terzo parametro, il quale dovr? esistere come
    * allegato del profilo.
    * Se siamo in presenza di un aggiornamento del documento ed il nome
    * del file esiste gi? fra la lista degli allegati, questi verr?
    * aggiornato nel suo contenuto
    * 
    * @param sNomeFile nome del file da allegare
    * @param file InputStream con il contenuto dell'allegato da aggiungere
    * @param sNameFilePadre nome dell'allegato padre
    * @see <a href="Profilo.html#setFileName(java.lang.String, java.lang.String)">setFileName(String,String)</a>,&nbsp; 
    *      <a href="Profilo.html#setFileName(java.lang.String)">setFileName(String)</a>,&nbsp;	   
    *      <a href="Profilo.html#setFileName(java.lang.String, java.io.InputStream)">setFileName(String,InputStream)</a>
   */   
   public void setFileName(String sNomeFile, InputStream file, String sNameFilePadre) throws Exception {
          setFileName(sNomeFile,"",IMPRONTA_OFF,SETTA_IMPRONTA,sNameFilePadre,file);
   }

   /**
    * Metodo che aggiunge il file passato (e la sua impronta) in input alla lista 
    * degli allegati da inserire nel profilo. 
    * Da utilizzare esclusivamente in <B>Inserimento / Aggiornamento del profilo</B>.<BR>
    * Il path del file verr? utilizzato anche per stabilire quale nome dare
    * all'allegato. Oltre al file verr? creata in automatico la sua impronta
    * e verr? aggiunta agli allegati del profilo come figlio (invisibile) del file.<BR>
    * Esempio:<BR>
    * <BR>
    * p.setFileNameImpronta("c:\file\allegatoImpronta.doc",);<BR>
    * <BR>
    * Verr? aggiunto un allegato con nome "allegato.doc" ed un altro
    * allegato invisibile figlio di "allegato.doc" che conterr?
    * la sua impronta.
    * Se siamo in presenza di un aggiornamento del documento ed il nome
    * del file esiste gi? fra la lista degli allegati, questi verr?
    * aggiornato nel suo contenuto
    * 
    * @param sPath percorso del file da allegare
   */
   public void setFileNameImpronta(String sPath) throws Exception {
          setFileName(sPath,"",IMPRONTA_ON,SETTA_IMPRONTA,null,null);
   }
   
   /**
    * Metodo che aggiunge un'impronta) al file 
    * allegato del profilo (se questo esiste) 
    * Da utilizzare esclusivamente in <B>Aggiornamento del profilo</B>.<BR>
    * Esempio:<BR>
    * <BR>
    * p.setFileNameImpronta("allegatoImpronta.doc",);<BR>
    * <BR>
    * Verr? aggiunto un impronta allegato invisibile figlio 
    * di "allegatoImpronta.doc" (se questo esiste) che conterr? la sua impronta.
    * 
    * @param sFileName file a cui legare l'impronta
   */
   public void creaFileNameImpronta(String sFileName) throws Exception {
          setFileName(sFileName,"",IMPRONTA_ON,CREA_IMPRONTA,null,null);
   }   

   /**
    * Metodo che aggiunge il file passato in input alla lista 
    * degli allegati da inserire nel profilo come allegato PDF di sistema. 
    * Da utilizzare esclusivamente in <B>Inserimento / Aggiornamento del profilo</B>.<BR>
    * Il path del file verr? utilizzato anche per stabilire quale nome dare
    * all'allegato. L'allegato perder? la sua estenzione originaria e diventer? .SYS_PDF.<BR>
    * Esempio:<BR>
    * <BR>
    * p.setSysPDF("c:\file\allegato.pdf");<BR>
    * <BR>
    * Verr? aggiunto un allegato con nome "allegato.SYS_PDF"
    * Se siamo in presenza di un aggiornamento del documento ed il nome
    * del file esiste gi? fra la lista degli allegati, questi verr?
    * aggiornato nel suo contenuto
    *
    * @param sPath percorso del file da allegare
    * @see <a href="Profilo.html#setSysPDF(java.lang.String, java.io.InputStream)">setSysPDF(String,InputStream)</a>
   */   
   public void setSysPDF(String sPath) throws Exception {
          setFileName(sPath+".SYS_PDF","",IMPRONTA_OFF,SETTA_IMPRONTA,null,null);
   }

   /**
    * Metodo che aggiunge il file passato in input (inputStream) alla lista 
    * degli allegati da inserire nel profilo come allegato PDF di sistema. 
    * Da utilizzare esclusivamente in <B>Inserimento / Aggiornamento del profilo</B>.<BR>
    * Il path del file verr? utilizzato anche per stabilire quale nome dare
    * all'allegato mentre l'InputStream servir? per "scrivere" fisicamente il file
    * sulla colonna del database. 
    * L'allegato perder? la sua estenzione originaria e diventer? .SYS_PDF.<BR>
    * Esempio:<BR>
    * <BR>
    * p.setSysPDF("allegato.pdf",is);<BR>
    * <BR>
    * Verr? aggiunto un allegato con nome "allegato.SYS_PDF" dal contenuto
    * dell'InputStream.
    * Se siamo in presenza di un aggiornamento del documento ed il nome
    * del file esiste gi? fra la lista degli allegati, questi verr?
    * aggiornato nel suo contenuto
    *
    * @param sNomeFile nome del file da allegare
    * @param is InputStream con il contenuto del file
    * @see <a href="Profilo.html#setSysPDF(java.lang.String)">setSysPDF(String)</a>
   */
   public void setSysPDF(String sNomeFile, InputStream is) throws Exception {
          setFileName(sNomeFile+".SYS_PDF","",IMPRONTA_OFF,SETTA_IMPRONTA,null,is);
   }    

   /**
    * Metodo che aggiunge il file passato in input alla lista 
    * degli allegati da eliminare nel profilo. 
    * Da utilizzare esclusivamente in <B>Aggiornamento del profilo</B>.<BR>
    * Esempio:<BR>
    * <BR>
    * p.setDeleteFileName("allegato.doc");<BR>
    * <BR>
    * Verr? eliminato l'allegato con nome "allegato.doc".
    * Se il file ha associata un'impronta questa verr? eliminata
    *
    * @param sNomeFile nome dell'allegato da eliminare
   */   
   public void setDeleteFileName(String sNomeFile) throws Exception {                     
          pathFileToDelete.add(sNomeFile);           
   }
   
   /**
    * Metodo che setta la colonna CONSERVAZIONE sulla tabella
    * documenti con il valore passato come parametro di input 
    * 
    * E' possibile passare come parametro la costante 
    * Profilo.RICHIESTA_CONSERVAZIONE  
   */    
   public void setConservazione(String sConservazione) {
	      try {
	        conservazione=Global.replaceAll(sConservazione.substring(0,200),"'","''");
	      }
	      catch (NullPointerException e) {
	    	 
	      }
	      catch (java.lang.StringIndexOutOfBoundsException e) {
	    	conservazione=Global.replaceAll(sConservazione,"'","''");
	      }
   }
   
   /**
    * Metodo che restituisce la colonna CONSERVAZIONE 
    * memorizzato sulla tabella documenti
   */      
   public String getConservazione() {
	   	  if (ad==null) return "";
	   	  
	      return ad.getConservazione();
   }    

   /**
    * Metodo che setta la colonna CONSERVAZIONE sulla tabella
    * documenti con il valore passato come parametro di input
    * 
    * E' possibile passare come parametro la costante 
    * Profilo.RICHIESTA_ARCHIVIAZIONE   
   */   
   public void setArchiviazione(String sArchiviazione) {
	      try {
	        archiviazione=Global.replaceAll(sArchiviazione.substring(0,200),"'","''");
	      }
	      catch (NullPointerException e) {
	    	 
	      }
	      catch (java.lang.StringIndexOutOfBoundsException e) {
	    	archiviazione=Global.replaceAll(sArchiviazione,"'","''");
	      }
   }    
   
   /**
    * Metodo che restituisce la colonna ARCHIVIAZIONE 
    * memorizzato sulla tabella documenti
   */     
   public String getArchiviazione() {
	      if (ad==null) return "";
	      
	      return ad.getArchiviazione();
   }         
   
   /**
    * Metodo che rinomina un file esistente aggiornando pure il relativo campo binario
    * Da utilizzare esclusivamente in <B>Aggiornamento del profilo</B>.<BR>
    * Esempio:<BR>
    * <BR>
    * p.renameFileName("allegato.doc","nuovoAllegato.doc",is);<BR>
    * <BR>
    * Verr? sostituito "allegato.doc" con nuovoAllegato.doc. La colonna di database
    * sar? riempita con il contenuto dell'InputStream.
    * Se l'allegato presentava dei figli prima della modifica, questi rimarranno
    * tali anche successivamente. 
    * Il metodo effettua il controllo di univocit? sul nome del nuovo file
    * rispetto a quelli gi? presenti sul documento.
    * 
    * @param oldNameFile nome del file da sostituire
    * @param newNameFile nuovo nome del file da sostituire
    * @param newFile     InputStream con il contenuto dell'allegato da sostituire
   */
   public void renameFileName(String oldNameFile, String newNameFile , InputStream newFile) throws Exception {
	      FileStruct fs = new FileStruct(newNameFile,newFile);
	      
	      fs.setFileNameToRename(oldNameFile);
	      
          fileStructRename.add(fs); 
   }   
  
   public void setScannedDocument() throws Exception {
          kofax=true;
   }

    /**
     * Metodo che rinomina un file esistente dato un id oggetto file
     * Da utilizzare esclusivamente in <B>Aggiornamento del profilo</B>.<BR>
     * Esempio:<BR>
     * <BR>
     * p.renameFileName("allegato.doc",12345);<BR>
     * <BR>
     * Verrà sostituito "allegato.doc" con nuovoAllegato.doc
     * Se l'allegato presentava dei figli prima della modifica, questi rimarranno
     * tali anche successivamente.
     * Il metodo effettua il controllo di univocità sul nome del nuovo file
     * rispetto a quelli già presenti sul documento.
     *
     * @param oldNameFile nome del file da sostituire
     * @param newNameFile nuovo nome del file da sostituire
     * @param idOggettoFile id dell'oggetto file
     */
    public void renameFileName(String oldNameFile, String newNameFile , long idOggettoFile) throws Exception {
        FileStruct fs = new FileStruct(newNameFile,idOggettoFile);

        fs.setFileNameToRename(oldNameFile);

        fileStructRename.add(fs);
    }

   public void settaPadre(String sPadre) {
          idDocumentoPadre=sPadre;
   }
    
   public Connection getCn() {
          if (en.getDbOp()==null)
             return en.Global.CONNECTION;
          else
             return en.getDbOp().getConn();
   }

   /**
    * Metodo che aggiunge alla lista delle ACL del profilo
    * la coppia (Utente,TipoACL) per un determinato ruolo
    * Da utilizzare esclusivamente in <B>Inserimento / Aggiornamento del profilo</B>.<BR>
    * Esempio:<BR>
    * <BR>
    * p.settaACL("AA4",Global.NORMAL_ACCESS,"AMM");<BR>
    * <BR>
    * Verranno inserite le competenze di "NORMAL_ACCESS"
    * per l'utente AA4 e ruolo AMM
    *
    * @param user  utente a cui assegnare le ACL
    * @param type  tipo di ACL. Tipi possibili:<BR>
    * 		 	   Global.NO_ACCESS -> Nega competenze di Lettura, Modifica, Cancellazione, Management<BR>
    *			   Global.COMPLETE_ACCESS -> Assegna competenze di Lettura, Modifica, Cancellazione, Management<BR>
    *			   Global.NORMAL_ACCESS -> Assegna competenze di Lettura, Modifica. Nega competenze di Cancellazione<BR>
    *			   Global.READONLY_ACCESS -> Assegna competenze di Lettura. Nega competenze di Modifica, Cancellazione<BR>
    * @param ruolo ruolo dell'utente
   */         
   public void settaACL(String user, String type, String ruolo) {
           ACLuser.addElement(user);
           ACLtype.addElement(type+"@"+ruolo);
   }   
   
   /**
    * Metodo che aggiunge alla lista delle ACL del profilo
    * la coppia (Utente,TipoACL) con ruolo GDM
    * Da utilizzare esclusivamente in <B>Inserimento / Aggiornamento del profilo</B>.<BR>
    * Esempio:<BR>
    * <BR>
    * p.settaACL("AA4",Global.NORMAL_ACCESS);<BR>
    * <BR>
    * Verranno inserite le competenze di "NORMAL_ACCESS"
    * per l'utente AA4 con ruolo GDM
    *
    * @param user utente a cui assegnare le ACL
    * @param type tipo di ACL. Tipi possibili:<BR>
    * 		 	  Global.NO_ACCESS -> Nega competenze di Lettura, Modifica, Cancellazione, Management<BR>
    *			  Global.COMPLETE_ACCESS -> Assegna competenze di Lettura, Modifica, Cancellazione, Management<BR>
    *			  Global.NORMAL_ACCESS -> Assegna competenze di Lettura, Modifica. Nega competenze di Cancellazione<BR>
    *			  Global.READONLY_ACCESS -> Assegna competenze di Lettura. Nega competenze di Modifica, Cancellazione<BR>
   */         
   public void settaACL(String user, String type) {
           ACLuser.addElement(user);
           ACLtype.addElement(type+"@GDM");
   }   

   /**
    * Metodo che aggiunge alla lista delle competenze da aggiungere al profilo
    * la coppia (Utente,TipoCompetenza) con un determinato ruolo
    * Da utilizzare esclusivamente in <B>Inserimento / Aggiornamento del profilo</B>.<BR>
    * Il metodo serve ad aggiungere una competenza "libera" per l'utente, senza quindi 
    * avere la necessit? di utilizzare uno schema di ACL che si ? obbligati ad usare mediante la 
    * <a href="Profilo.html#settaACL(java.lang.String, java.lang.String)">settaACL</a>.
    * Le date devono essere passate nel formato dd/mm/yyyy, stringa vuota per ignorarle<BR>
    * Esempio:<BR>
    * <BR>
    * p.addCompetenza("AA4","AMM","X","01/01/2008","");<BR>
    * p.addCompetenza("AA4","AMM","Y","","31/12/2009");<BR>
    * <BR>
    * Verranno assegnate le competenze di tipo X e Y
    * per l'utente AA4 con ruolo AMM per le date indicate
    *
    * @param user utente a cui assegnare le ACL
    * @param ruolo ruolo dell'utente
    * @param type tipo di competenza da assegnare all'utente per il profilo
    * @param dataDa Data Inizio competenza (o stringa vuota)
    * @param dataA  Data Fine competenza (o stringa vuota)
    * @see <a href="Profilo.html#removeCompetenza(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)">removeCompetenza</a>
   */   
   public void addCompetenza(String user, String ruolo , String type, String dataDa, String dataA) {
	      ACLExtraUser.addElement(user);
	      
	      String sDataDaPassare, sDataAPassare;
	      
	      if (dataDa==null) 
	    	  sDataDaPassare="";
	      else
	    	  sDataDaPassare=dataDa;
	      
	      if (dataA==null) 
	    	  sDataAPassare="";
	      else
	    	  sDataAPassare=dataA;
	      
          ACLExtraType.addElement(type+"@S#"+ruolo+"$"+sDataDaPassare+"&"+sDataAPassare);
   }   
   
   /**
    * Metodo che aggiunge alla lista delle competenze da aggiungere al profilo
    * la coppia (Utente,TipoCompetenza) con ruolo GDM
    * Da utilizzare esclusivamente in <B>Inserimento / Aggiornamento del profilo</B>.<BR>
    * Il metodo serve ad aggiungere una competenza "libera" per l'utente, senza quindi 
    * avere la necessit? di utilizzare uno schema di ACL che si ? obbligati ad usare mediante la 
    * <a href="Profilo.html#settaACL(java.lang.String, java.lang.String)">settaACL</a>
    * Le date devono essere passate nel formato dd/mm/yyyy, stringa vuota per ignorarle<BR>
    * Esempio:<BR>
    * <BR>
    * p.addCompetenza("AA4","X","01/01/2008","");<BR>
    * p.addCompetenza("AA4","Y","","31/12/2009");<BR>
    * <BR>
    * Verranno assegnate le competenze di tipo X e Y
    * per l'utente AA4 con ruolo GDM per le date indicate
    *
    * @param user utente a cui assegnare le ACL
    * @param type tipo di competenza da assegnare all'utente per il profilo
    * @param dataDa Data Inizio competenza (o stringa vuota)
    * @param dataA  Data Fine competenza (o stringa vuota)
    * @see <a href="Profilo.html#removeCompetenza(java.lang.String, java.lang.String, java.lang.String, java.lang.String)">removeCompetenza</a>
   */   
   public void addCompetenza(String user , String type, String dataDa, String dataA) {	   
	      addCompetenza(user, "GDM" ,type, dataDa,dataA);
   }      
   
   /**
    * Metodo che aggiunge alla lista delle competenze da aggiungere al profilo
    * la coppia (Utente,TipoCompetenza) con un determinato ruolo
    * Da utilizzare esclusivamente in <B>Inserimento / Aggiornamento del profilo</B>.<BR>
    * Il metodo serve ad aggiungere una competenza "libera" per l'utente, senza quindi 
    * avere la necessit? di utilizzare uno schema di ACL che si ? obbligati ad usare mediante la 
    * <a href="Profilo.html#settaACL(java.lang.String, java.lang.String)">settaACL</a><BR>
    * Esempio:<BR>
    * <BR>
    * p.addCompetenza("AA4","AMM","X");<BR>
    * p.addCompetenza("AA4","AMM","Y");<BR>
    * <BR>
    * Verranno assegnate le competenze di tipo X e Y
    * per l'utente AA4 con ruolo AMM
    *
    * @param user utente a cui assegnare le ACL
    * @param ruolo ruolo dell'utente
    * @param type tipo di competenza da assegnare all'utente per il profilo
    * @see <a href="Profilo.html#removeCompetenza(java.lang.String, java.lang.String, java.lang.String, java.lang.String)">removeCompetenza</a>
   */   
   public void addCompetenza(String user, String ruolo , String type) {
	      addCompetenza(user, "GDM" ,type, "","");
   }

   /**
    * Metodo che aggiunge alla lista delle competenze da aggiungere al profilo
    * la coppia (Utente,TipoCompetenza) con ruolo GDM
    * Da utilizzare esclusivamente in <B>Inserimento / Aggiornamento del profilo</B>.<BR>
    * Il metodo serve ad aggiungere una competenza "libera" per l'utente, senza quindi 
    * avere la necessit? di utilizzare uno schema di ACL che si ? obbligati ad usare mediante la 
    * <a href="Profilo.html#settaACL(java.lang.String, java.lang.String)">settaACL</a><BR>
    * Esempio:<BR>
    * <BR>
    * p.addCompetenza("AA4","X");<BR>
    * p.addCompetenza("AA4","Y");<BR>
    * <BR>
    * Verranno assegnate le competenze di tipo X e Y
    * per l'utente AA4 con ruolo GDM
    *
    * @param user utente a cui assegnare le ACL
    * @param type tipo di competenza da assegnare all'utente per il profilo
    * @see <a href="Profilo.html#removeCompetenza(java.lang.String, java.lang.String)">removeCompetenza</a>
   */
   public void addCompetenza(String user, String type) {
          addCompetenza(user,"GDM",type);
   }
   
   /**
    * Metodo che aggiunge alla lista delle competenze da negare al profilo
    * la coppia (Utente,TipoCompetenza) con un determinato ruolo
    * Da utilizzare esclusivamente in <B>Inserimento / Aggiornamento del profilo</B>.<BR>
    * Il metodo serve ad negare una competenza "libera" per l'utente, senza quindi 
    * avere la necessit? di utilizzare uno schema di ACL che si ? obbligati ad usare mediante la 
    * <a href="Profilo.html#settaACL(java.lang.String, java.lang.String)">settaACL</a>
    * Le date devono essere passate nel formato dd/mm/yyyy, stringa vuota per ignorarle<BR>
    * Esempio:<BR>
    * <BR>
    * p.removeCompetenza("AA4","AMM","X","01/01/2008","");<BR>
    * p.removeCompetenza("AA4","AMM","Y","","31/12/2009");<BR>
    * <BR>
    * Verranno negate le competenze di tipo X e Y
    * per l'utente AA4 con ruolo AMM per le date indicate
    *
    * @param user utente a cui negare la competenza
    * @param ruolo ruolo dell'utente
    * @param type tipo di competenza da negare all'utente per il profilo
    * @param dataDa Data Inizio competenza (o stringa vuota)
    * @param dataA  Data Fine competenza (o stringa vuota) 
    * @see <a href="Profilo.html#addCompetenza(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)">addCompetenza</a>
   */     
   public void removeCompetenza(String user, String ruolo , String type, String dataDa, String dataA) {
	      ACLExtraUser.addElement(user);
	      
	      String sDataDaPassare, sDataAPassare;
	      
	      if (dataDa==null) 
	    	  sDataDaPassare="";
	      else
	    	  sDataDaPassare=dataDa;
	      
	      if (dataA==null) 
	    	  sDataAPassare="";
	      else
	    	  sDataAPassare=dataA;
	      
          ACLExtraType.addElement(type+"@N#"+ruolo+"$"+sDataDaPassare+"&"+sDataAPassare);
   }      
   
   /**
    * Metodo che aggiunge alla lista delle competenze da negare al profilo
    * la coppia (Utente,TipoCompetenza) con ruolo GDM
    * Da utilizzare esclusivamente in <B>Inserimento / Aggiornamento del profilo</B>.<BR>
    * Il metodo serve ad negare una competenza "libera" per l'utente, senza quindi 
    * avere la necessit? di utilizzare uno schema di ACL che si ? obbligati ad usare mediante la 
    * <a href="Profilo.html#settaACL(java.lang.String, java.lang.String)">settaACL</a>
    * Le date devono essere passate nel formato dd/mm/yyyy, stringa vuota per ignorarle<BR>
    * Esempio:<BR>
    * <BR>
    * p.removeCompetenza("AA4","X","01/01/2008","");<BR>
    * p.removeCompetenza("AA4","Y","","31/12/2009");<BR>
    * <BR>
    * Verranno negate le competenze di tipo X e Y
    * per l'utente AA4 con ruolo GDM per le date indicate
    *
    * @param user utente a cui negare la competenza
    * @param type tipo di competenza da negare all'utente per il profilo
    * @param dataDa Data Inizio competenza (o stringa vuota)
    * @param dataA  Data Fine competenza (o stringa vuota)  
    * @see <a href="Profilo.html#addCompetenza(java.lang.String, java.lang.String, java.lang.String, java.lang.String)">addCompetenza</a>
   */     
   public void removeCompetenza(String user , String type, String dataDa, String dataA) {	   
	      removeCompetenza(user, "GDM" ,type, dataDa,dataA);
   }    

   /**
    * Metodo che aggiunge alla lista delle competenze da negare al profilo
    * la coppia (Utente,TipoCompetenza) con un determinato ruolo
    * Da utilizzare esclusivamente in <B>Inserimento / Aggiornamento del profilo</B>.<BR>
    * Il metodo serve ad negare una competenza "libera" per l'utente, senza quindi 
    * avere la necessit? di utilizzare uno schema di ACL che si ? obbligati ad usare mediante la 
    * <a href="Profilo.html#settaACL(java.lang.String, java.lang.String)">settaACL</a><BR>
    * Esempio:<BR>
    * <BR>
    * p.removeCompetenza("AA4","AMM","X");<BR>
    * p.removeCompetenza("AA4","AMM","Y");<BR>
    * <BR>
    * Verranno negate le competenze di tipo X e Y
    * per l'utente AA4 con ruolo AMM
    *
    * @param user utente a cui negare la competenza
    * @param ruolo ruolo dell'utente
    * @param type tipo di competenza da negare all'utente per il profilo
    * @see <a href="Profilo.html#addCompetenza(java.lang.String, java.lang.String, java.lang.String, java.lang.String)">addCompetenza</a>
   */   
   public void removeCompetenza(String user, String ruolo, String type) {
          removeCompetenza(user, "GDM" ,type, "","");
   }

   /**
    * Metodo che aggiunge alla lista delle competenze da negare al profilo
    * la coppia (Utente,TipoCompetenza) con ruolo GDM
    * Da utilizzare esclusivamente in <B>Inserimento / Aggiornamento del profilo</B>.<BR>
    * Il metodo serve ad negare una competenza "libera" per l'utente, senza quindi 
    * avere la necessit? di utilizzare uno schema di ACL che si ? obbligati ad usare mediante la 
    * <a href="Profilo.html#settaACL(java.lang.String, java.lang.String)">settaACL</a><BR>
    * Esempio:<BR>
    * <BR>
    * p.removeCompetenza("AA4","X");<BR>
    * p.removeCompetenza("AA4","Y");<BR>
    * <BR>
    * Verranno negate le competenze di tipo X e Y
    * per l'utente AA4 con ruolo GDM
    *
    * @param user utente a cui negare la competenza
    * @param type tipo di competenza da negare all'utente per il profilo
    * @see <a href="Profilo.html#addCompetenza(java.lang.String, java.lang.String)">addCompetenza</a>
   */   
   public void removeCompetenza(String user, String type) {
          removeCompetenza(user,"GDM",type);
   }

   /**
    * Metodo di integrazione con <B>HUMMINGBIRD</B>
   */
   public void CreaSincro(String CodiceEnte) {
          aclNom = new Acl_Nominali(CodiceEnte);
   }

   /**
    * Metodo di integrazione con <B>HUMMINGBIRD</B>
   */
   public void AggiungiCompetenze(String soggetto,String documento,String NuovaVecchia,String valore) {
          aclNom.aggiungiCompetenze(soggetto, documento, NuovaVecchia, valore);
   }

   /**
    * Metodo di integrazione con <B>HUMMINGBIRD</B>
   */   
   public Boolean GeneraXML(String sPath) {
          try {
            FileUtility.createDataFile(sPath,aclNom.generaXML(),true);
            return new Boolean(true);
          }
          catch (Exception e) {
            error="Profilo::GeneraXML()\n"+e.getMessage();
            return new Boolean(false);
          }      
   }  
   
   public void setConfermaAllegatiTemp(boolean bFlag) {
	      bForceAllegatiTemp = bFlag;
   }
   
   public void setConfermaAllegatiTemp(boolean bFlag, String cr) {
	      bForceAllegatiTemp = bFlag;
	      crAllegatiTemp = cr;
   }   
            
   /**
    * Metodo di gestione dell'errore chiamando l'integrazione con <B>HUMMINGBIRD</B>
   */     
   public String getCleanError() {
          if (en.Global.DM.equals(en.Global.FINMATICA_DM))
              return error;
               
          if (error.indexOf("PANTAERROR")>0)
              return error.substring(error.indexOf(" -- ")+4,error.length());
          else
              return "Errore sconosciuto";
   }   
   
   public void escludiControlloCompetenze(boolean bFlag) {
   		  bEscludiControlloCompetenze=bFlag;
   		  if (ad!=null) ad.setControlloCompetenze(bFlag);   		  
   }
   
   public boolean testEsclusoControlloCompetenze()  {	   
          return bEscludiControlloCompetenze;
   } 
   
   /**
    * Metodo che restituisce il valore
    * del flag "CompetenzeAllegati" sul modello
    * relativo al documento istanziato.
    * 
    * E' necessario effettuare un accedi, altrimenti
    * il metodo restituisce sempre false.
   */ 
   public boolean getGestioneCompetenzeAllegati() {
	   	  if (ad==null) return false;
	      return ad.isCompetenzeAllegati();
   }
   
   /**
    * Metodo che restituisce lo stato del profilo.
    * i valori possibili sono:<BR>
    * 				  	Global.STATO_BOZZA<BR>
	*					Global.STATO_COMPLETO<BR>
	*					Global.STATO_ANNULLATO<BR>
	*					Global.STATO_CANCELLATO<BR>
   */     
   public String getStato() throws Exception {
	   	  if (ad!=null) 
	   		  return ad.aDocumento.getStatusDocumento().getStato();
	   	  else {
	   		  return "";
	   		  /*error="Profilo::getStatus() - E' necessario accedere al profilo per poter conoscerne lo stato";
              throw new Exception(error);*/
	   	  }
   }
  
   /**
    * Metodo che permette di attivare la sincronizzazione
    * del profilo verso il respository di alfresco
    * 
	* @param endPoint Url verso le api WS di alfresco
	* @param user Utente che ha i permessi di scrittura via WS di alfresco
	* @param password Password
	* @param pathFolder Percorso (XPATH) dove creare il profilo (es: /app:company_home )
	* @param name Nome da visualizzare come profilo su alfresco (es: Documento n? 1)
	* @param nameSpaceModelDocument nome del modello alfresco corrispondente al modello di questo profilo (es: ads.customProtocolModel.model) 
	* @param prefixNameSpaceModelDocument prefisso del modello di alfresco (es: cpm)
	* @param typeContent nome della collezione dati (types) associati al modello alfresco (es: campiDocumento)
	* @param associationName nome dell'association alfresco che lega documento con allegati
   */
   public void attivaAlfresco(String endPoint, 
		     				  String user, 
		     				  String password, 
					          String pathFolder, 
					          String name,
					          String nameSpaceModelDocument, 
					          String prefixNameSpaceModelDocument, 
					          String typeContent,
					          String associationName) {
		   attivaAlfresco(endPoint, 
				          user, 
					      password, 
			              pathFolder, 
			              name,
			              nameSpaceModelDocument, 
			              prefixNameSpaceModelDocument, 
			              typeContent,
			              associationName,
			              null,
			              null);
	   	  	   	  
   }   
   
   /**
    * Metodo che permette di attivare la sincronizzazione
    * del profilo verso il respository di alfresco
    * 
	* @param endPoint Url verso le api WS di alfresco
	* @param user Utente che ha i permessi di scrittura via WS di alfresco
	* @param password Password
	* @param pathFolder Percorso (XPATH) dove creare il profilo (es: /app:company_home )
	* @param name Nome da visualizzare come profilo su alfresco (es: Documento n? 1)
	* @param nameSpaceModelDocument nome del modello alfresco corrispondente al modello di questo profilo (es: ads.customProtocolModel.model) 
	* @param prefixNameSpaceModelDocument prefisso del modello di alfresco (es: cpm)
	* @param typeContent nome della collezione dati (types) associati al modello alfresco (es: campiDocumento)
	* @param associationName nome dell'association alfresco che lega documento con allegati
	* @param aclAccepted	Array di tipo ACL che contiene la coppia utente,tipoCompetenza da rendere abilitata\n
	* 						Valori possibili per tipo competenza:\n
	* 									Global.ABIL_CONSUMER Pu? leggere il contenuto del documento\n
	* 								    Global.ABIL_EDITOR Pu? leggere ed editare il contenuto del documento\n
	* 									Global.ABIL_CONTRIBUTOR Pu? leggere ed aggiungere un contenuto\n
	* 									Global.ABIL_COORDINATOR Pu? leggere editare, aggiungere e cancellare un contenuto (FULL ACCESS)\n
	* 						esempio:
	* 									new ACL("guest",Global.ABIL_COORDINATOR)
	* @param aclDeclined	Array di tipo ACL che contiene la coppia utente,tipoCompetenza da disabilitare\n
   */
   public void attivaAlfresco(String endPoint, 
		     				  String user, 
		     				  String password, 
					          String pathFolder, 
					          String name,
					          String nameSpaceModelDocument, 
					          String prefixNameSpaceModelDocument, 
					          String typeContent,
					          String associationName,
					          ACL[] aclAccepted,
					          ACL[] aclDeclined) {
	   	  aStruct = new AlfrescoStruct(endPoint,
	   			  					   user, 
	   			  					   password, 
	   			  					   pathFolder, 
	   			  					   name, 
	   			  					   nameSpaceModelDocument, 
	   			  					   prefixNameSpaceModelDocument, 
	   			  					   typeContent,associationName);
	   	  
	   	 if (aclAccepted!=null)
	   		 for(int i=0;i<aclAccepted.length;i++)
	   			 aStruct.addCompetenza(aclAccepted[i].getPersonGroup(),aclAccepted[i].getMask());
	   	  
	   	  if (aclDeclined!=null)
	   		  for(int i=0;i<aclDeclined.length;i++)
	   			  aStruct.romoveCompetenza(aclAccepted[i].getPersonGroup(),aclAccepted[i].getMask());
	   	  	   	  
   }
   
   public boolean getCompLettura() throws Exception {
	   	  return testCompetenza(COMP_LETTURA);
   }   
   
   public boolean getCompModifica() throws Exception {
	   	  return testCompetenza(COMP_MODIFICA);
   }     
   
   public boolean getCompCancellazione() throws Exception {
	   	  return testCompetenza(COMP_CANCELLAZIONE);
   }    
   
   public boolean getCompManage() throws Exception {
	   	  return testCompetenza(COMP_MANAGE);
   }     
   
   
   
   /**
    * Se settato a true fa si che il documento sia sempre
    * in fase di "nuovo".
    * Ovvero, se si istanzia un nuovo profilo col metodo
    * area,cm
    * e si fa salva, i successivi salvataggi non saranno
    * un'aggiornamento del primo profilo creato ma
    * verranno sempre creati nuovi profili mantenendo
    * in memoria le variabili impostate all'inizio.
    * 
    * Utilizzare questo metodo per velocizzare gli inserimenti
    * in fase di trascodifica.
    * 
	* @param alwaysNew
   */
   public void setAlwaysNew(boolean alwaysNew) {
	   	  this.alwaysNew = alwaysNew;
   }   
   
   /**
    * Se settato a true fa si che il documento appena
    * creato (quindi solo in caso di un documento
    * nuovo) questo non erediti le competenze
    * settate sul modello
    * 
	* @param skipAddCompetenzeModello
   */
   public void setSkipAddCompetenzeModello(boolean skipAddCompetenzeModello) {
	      this.skipAddCompetenzeModello = skipAddCompetenzeModello;
   }
   
   /**
    * Se settato a true fa si che il documento inserito
    * (se orizzontale) non lanci la funzione di generazione
    * del campo FULL_TEXT della tabella orizzontale
    * (campo che serve al sistema per ricercare in maniera
    * full text sul documento)
    * 
	* @param skipReindexFullTextField
   */   
   public void setSkipReindexFullTextField(boolean skipReindexFullTextField) {
		  this.skipReindexFullTextField = skipReindexFullTextField;
   }
   
   // ***************** PRIVATE ACCESS ***************** //
   
   private boolean testCompetenza(int index) throws Exception {
	   	   if (en==null) throw new Exception("Profilo::testCompetenza - E' prima necessario lanciare il metodo di initVarEnv");
	   
	   	   if (competenzeArray==null) retrieveCompetenze();
	   	  
	   	   if (competenzeArray==null) {
	   		  throw new Exception("Profilo::testCompetenza - E' necessario prima specificare l'idDocumento");
	   	   }
	   	  
	   	   if (competenzeArray[index].equals("1")) return true;
	   	  
	   	   return false;
   }
   
   private void retrieveCompetenze() throws Exception {
	   	   if (idDocumento==null || idDocumento.equals("X")) return;	   	   
	   	   
	   	   GDM_Competenze gdmCom = new GDM_Competenze(en);
	   	   
	   	   String sComp = gdmCom.getCompetenzeDocumento(idDocumento);
	   	   competenzeArray = new String[4];
	   	   
	   	   competenzeArray=sComp.split("@");	   	   	   	  	   	 
   }
    
   private Boolean salvaBozza(boolean bPrebozza) {       
	   	   
	   
	       try {
             boolean ret=false;
             
             elpsTime.start(" ******************  SALVA PROFILO  ***************** ","");
             
             
             if (agd==null)             	 
            	 //Se ? la prima volta la devo creare per forza 
            	 agd = new AggiungiDocumento(tipoDocumento, area,  en );  
             else {
            	 agd.reconnect();
            	 agd.settaIdDoc("0");
             }
             
             int size = campi.size();
             for(int i=0;i<size;i++) 
             {
            	 agd.aggiungiDati((String)campi.elementAt(i),valori.elementAt(i));
             }

             size = pathFile.size();
             for(int i=0;i<size;i++) {
                if (((String)isImpronta.get(i)).equals(IMPRONTA_OFF)) {                                    
                    if (pathFilePadre.elementAt(i).equals("NOPADRE"))
                    	agd.aggiungiAllegato( (String)pathFileContentType.elementAt(i), (InputStream)(file.elementAt(i)),(String)pathFile.elementAt(i)); 
                    else {                      
                    	agd.aggiungiAllegatoConPadre((InputStream)file.elementAt(i),(String)pathFile.elementAt(i), (String)pathFilePadre.elementAt(i));                                                                                        
                    }
                }
                else {
                   File f = new File((String)completePathFile.elementAt(i));
                   FileInputStream fis = new FileInputStream(f);
                   
                   agd.aggiungiAllegatoeImpronta( (InputStream)(file.elementAt(i)), (InputStream)(fis),(String)pathFile.elementAt(i));            
                }
             }

             size = ACLuser.size();
             for(int i=0;i<size;i++) {
            	 String sType,sRuolo;
            	 
            	 sType=(String)ACLtype.elementAt(i);
            	 
            	 sRuolo=sType.substring(sType.indexOf("@")+1,sType.length());
            	 sType=sType.substring(0,sType.indexOf("@"));
            	 agd.aggiungiACL( (String)ACLuser.elementAt(i),sType,sRuolo);
             }
             
             size = ACLExtraUser.size();
             for(int i=0;i<size;i++)  
            	 agd.aggiungiCompetenza( (String)ACLExtraUser.elementAt(i),(String)ACLExtraType.elementAt(i));
                
             if (fileP7M!=null) 
             {
            	 agd.settaFileP7M(fileP7M);
             }
                         
             agd.setPadre(idDocumentoPadre);
             agd.settaCodiceRichiesta(cr);
             agd.setConservazione(conservazione);
             agd.setArchiviazione(archiviazione);   
             agd.setBSkipUnknowField(setBSkipUnknowField);
             agd.setListSkipUnknowField(listSkipUnknowField);
             agd.setSkipAddCompetenzeModello(skipAddCompetenzeModello);
             agd.setSkipReindexFullTextField(skipReindexFullTextField);
             
             if (bForceAllegatiTemp) agd.salvaAllegatiTemp(true);
             agd.salvaAllegatiTempCr(crAllegatiTemp);
             agd.creaVersione(creaVersione);
             
             codeErrorPostStave=null;
             errorPostSave=null;
             
             if (bPrebozza)
            	 ret=agd.salvaDocumentoPreBozza();
             else
            	 ret=agd.salvaDocumentoBozza();             

             idDocumento=agd.aDocumento.getIdDocumento();
             try {
                   idLog=agd.getIdLog();
             }
             catch(Exception e) {
                   idLog=0;
             }
             
             ultimaVersioneCreata=agd.ultimaVersione;
             //Se abilitato porto le cose su alfresco
             if (ret && aStruct!=null && aStruct.endPoint!=null) this.salvaAlfresco();             

             file.removeAllElements();
             pathFile.removeAllElements();           
             pathFileContentType.removeAllElements(); 
             pathFilePadre.removeAllElements(); 
             completePathFile.removeAllElements();           
             isImpronta.removeAllElements();
             idAllegatiInUpdate.removeAllElements();
             docRiferimenti.removeAllElements();
             typeRiferimenti.removeAllElements();  
             docRiferimentiToDelete.removeAllElements();;
             typeRiferimentiToDelete.removeAllElements();
             valori.removeAllElements();
             valoriAppend.removeAllElements();
             campi.removeAllElements();
             
             fileP7M=null;
             
             dataUltAgg=agd.ultAggiornamento;
             
             elpsTime.stop();
             
             codeErrorPostStave=agd.getCodeErrorPostSave();
             errorPostSave=agd.getDescrCodeErrorPostSave();
             
             descrCodeError=agd.getDescrCodeError();            
                             
             return new Boolean(ret);
           }
           catch (Exception e) 
           {
        	 if (ad!=null) descrCodeError=ad.getDescrCodeError();
             error = "Inserimento del Documento - \n"+e.getMessage();
             return new Boolean(false);
           }
   }  

   private Boolean aggiorna() {
	   	   AggiornaDocumento ad =null;
           
	   	   try { 
             boolean ret=false;            

             if (en.Global.DM.equals(en.Global.FINMATICA_DM))
                ad = new AggiornaDocumento(idDocumento, en );
             else
                ad = new AggiornaDocumento(idDocumento+"@"+tipoDocumento, en );
             
             ad.setbDontRepeatExistsRif(bDontRepeatExistsRif);     
             
             ad.setBSkipUnknowField(setBSkipUnknowField);
             ad.setListSkipUnknowField(listSkipUnknowField);
             if (setSkipBusyControl) ad.setSalvaSempre(true);
             
             int size = campi.size();
            
        	 ad.aggiornaDatiMultipla(campi, valori, valoriAppend);
             
            /* for(int i=0;i<size;i++) {            	            	 
            	 if (valoriAppend.elementAt(i)!=null && valoriAppend.elementAt(i).equals("S"))
         			ad.aggiornaDati((String)campi.elementAt(i),valori.elementAt(i),true);
         		else
         			ad.aggiornaDati((String)campi.elementAt(i),valori.elementAt(i));                    	 
             }*/
            	       
             
             size = ACLuser.size();
             for(int i=0;i<size;i++) { 
	             String sType,sRuolo;
	        	 
	        	 sType=(String)ACLtype.elementAt(i);
	        	 
	        	 sRuolo=sType.substring(sType.indexOf("@")+1,sType.length());
	        	 sType=sType.substring(0,sType.indexOf("@"));
	             ad.aggiungiACL( (String)ACLuser.elementAt(i),sType,sRuolo);
             }

             size = ACLExtraUser.size();
             for(int i=0;i<size;i++)  
                ad.aggiungiCompetenza( (String)ACLExtraUser.elementAt(i),(String)ACLExtraType.elementAt(i));
                
             size = docRiferimenti.size();
             for(int i=0;i<size;i++)  
                ad.aggiungiRiferimento( (String)docRiferimenti.elementAt(i),(String)typeRiferimenti.elementAt(i));                          

             size = docRiferimentiToDelete.size();
             for(int i=0;i<size;i++)
                ad.eliminaRiferimento( (String)docRiferimentiToDelete.elementAt(i),(String)typeRiferimentiToDelete.elementAt(i));
                           
             size = pathFile.size();

             for(int i=0;i<size;i++)  {            	 
                if (((String)idAllegatiInUpdate.elementAt(i)).equals("")) {
                   if (((String)isImpronta.get(i)).equals(IMPRONTA_OFF)) {   
                      if (fileP7M!=null) 
                          ad.aggiungiAllegato( (InputStream)file.elementAt(i),(String)pathFile.elementAt(i), fileP7M,null,"N");                        
                      else {   
                         if (pathFilePadre.elementAt(i).equals("NOPADRE"))
                           ad.aggiungiAllegato((String)pathFileContentType.elementAt(i),  (InputStream)file.elementAt(i),(String)pathFile.elementAt(i)); 
                         else {                      
                           ad.aggiungiAllegatoConPadre((InputStream)file.elementAt(i),(String)pathFile.elementAt(i), (String)pathFilePadre.elementAt(i));                                                                                        
                         }
                      }   
                   }
                   //Gestione Impronta
                   else {
                	    if (((String)isCreaOSettaImpronta.get(i)).equals(SETTA_IMPRONTA)) {
	                	    File f = new File((String)completePathFile.elementAt(i));
	                        FileInputStream fis = new FileInputStream(f);
                	    
	                        ad.aggiornaAllegatoeImpronta( (InputStream)(file.elementAt(i)), (InputStream)(fis),(String)pathFile.elementAt(i));
                        }   
                	    else
                	    	ad.creaImprontaPerAllegato((String)pathFile.elementAt(i));
                   }
                }
             }
               //L'aggiornamento del file ? stato volutamente abolito perch? non funziona da HUMM
               /*else {
                   ad.aggiornaAllegato( ((String)idAllegatiInUpdate.elementAt(i)), (InputStream)file.elementAt(i),(String)pathFile.elementAt(i));                                                   
               }*/

            size = pathFileToDelete.size();
            for(int i=0;i<size;i++)  
                ad.cancellaAllegato( (String)pathFileToDelete.elementAt(i));
                                    
            size = fileStructRename.size();
            for(int i=0;i<size;i++)  
            	ad.renameAllegato((FileStruct)fileStructRename.get(i));
            
            if (kofax) 
            	 ad.setScannedDocument();
            
            ad.setAggiornaDataUltAggiornamento(bAggiornaDataUltAgg);
            
            if (ultAggiornamento!=null)
            	ad.setUltAggiornamento(ultAggiornamento);

            ad.setPadre(idDocumentoPadre);
            ad.setBForceMaintainPreBozza(forceMaintaninPreBozza);
            ad.setOgfiLog(bogfilog);   

            if (conservazione!=null)
            	ad.setConservazione(conservazione);
            if (archiviazione!=null)
            	ad.setArchiviazione(archiviazione);  
            
            if (bForceAllegatiTemp) ad.salvaAllegatiTemp(true);
            ad.creaVersione(creaVersione);

            codeErrorPostStave=null;
            errorPostSave=null;
            
            if (en.Global.DM.equals(en.Global.FINMATICA_DM)) {
                 //La X la uso dalla DLL VB perch? il null non lo prende
                 if (newStatus==null || newStatus.equals("X"))          
                     ret=ad.salvaDocumento();
                 else if (newStatus.equals(Global.STATO_BOZZA))
                     ret=ad.salvaDocumentoBozza();   
                 else if (newStatus.equals(Global.STATO_COMPLETO))
                     ret=ad.salvaDocumentoCompleto();   
                 else if (newStatus.equals(Global.STATO_ANNULLATO))                   
                     ret=ad.salvaDocumentoAnnullato();
                 else if (newStatus.equals(Global.STATO_CANCELLATO))  
                     ret=ad.salvaDocumentoCancellato();                 
                 else {
                    error="Profilo::aggiornaCompleto()\n"+
                           "Stato ("+newStatus+") impostato per documento inesistente";
                    return new Boolean(false);
                 }
             }
             else
                 ret=ad.salvaDocumentoCompleto();   
            
             ultimaVersioneCreata=ad.ultimaVersione;
             //Se abilitato porto le cose su alfresco
             if (ret && aStruct!=null && aStruct.endPoint!=null) this.salvaAlfresco();

             try {
                   idLog=ad.getIdLog();
             }
             catch(Exception e) {
                   idLog=0;
             }

             file.removeAllElements();
             pathFile.removeAllElements();           
             pathFileContentType.removeAllElements(); 
             pathFilePadre.removeAllElements(); 
             idAllegatiInUpdate.removeAllElements();
             docRiferimenti.removeAllElements();
             typeRiferimenti.removeAllElements();
             docRiferimentiToDelete.removeAllElements();
             typeRiferimentiToDelete.removeAllElements();    
             fileStructRename.removeAllElements();
             fileP7M=null;
             valori.removeAllElements();
             valoriAppend.removeAllElements();
             campi.removeAllElements();
             
             dataUltAgg=ad.ultAggiornamento;
             
             codeErrorPostStave=ad.getCodeErrorPostSave();
             errorPostSave=ad.getDescrCodeErrorPostSave(); 
             
             descrCodeError=ad.getDescrCodeError();

             return new Boolean(ret);

           }
           catch (Exception e) {
        	 if (ad!=null) descrCodeError=ad.getDescrCodeError();
             error="Aggiornamento del Documento - \n"+e.getMessage();
             return new Boolean(false);
           }        
   }       
   
   private void setFileName(String sPath, String id, String impronta, String creaOrSetImpronta, String padre, InputStream is) throws Exception {
	       setFileName( sPath,  id,  impronta,  creaOrSetImpronta,  padre,  is, null);
   }
    
   private void setFileName(String sPath, String id, String impronta, String creaOrSetImpronta, String padre, InputStream is, String contentType) throws Exception {
	     
	     sPath = Global.replaceSpecialChrFile(sPath);
	     
	     //Il p7m va trattato come p7m solo nel caso di inserimento del
         //documento la prima volta.
         //In caso di aggiornamento il p7m ? sempre trattato come un
         //normalissimo allegato.
         if (idDocumento.equals("X") && Global.lastTrim(sPath,".",en.Global.WEB_SERVER_TYPE).toUpperCase().equals("P7M") &&  (en.Global.DM.equals(en.Global.HUMMINGBIRD_DM))) 
         {
                try {
                      if (is==null) {                       
                          File f = new File(sPath);
                          FileInputStream fis = new FileInputStream(f);
                          fileP7M = (InputStream)fis;
                      }
                      else {
                          fileP7M=is;
                      }
                }
                catch (Exception e) 
                { 
                      error="Profilo::settaFile - P7M\n"+e.getMessage();
                      throw new Exception(error);
                }
         }
         else {
               try{
                   String sPathApp=sPath;
                   
                   if (creaOrSetImpronta.equals(CREA_IMPRONTA))  {
                	   pathFile.addElement(sPath);
                	   pathFileContentType.addElement(contentType);
                	   completePathFile.addElement(sPath);
	                   isImpronta.addElement(impronta);
	                   isCreaOSettaImpronta.addElement(creaOrSetImpronta);
	                   idAllegatiInUpdate.addElement(id);
	                   pathFilePadre.addElement("NOPADRE");
               	   }
                   else {
	                   if (Global.lastTrim(sPathApp,".",en.Global.WEB_SERVER_TYPE).equals("SYS_PDF")) 
	                      sPathApp=sPath.substring(0,sPathApp.indexOf(Global.lastTrim(sPathApp,".",en.Global.WEB_SERVER_TYPE))-1);                   
	               
	                   if (is==null) {               
	                      File f = new File(sPathApp);
	                      FileInputStream fis = new FileInputStream(f);
	                      file.addElement(fis);
	                   }
	                   else
	                      file.addElement(is);                      
	                      
	                   pathFile.addElement(Global.lastTrim(sPath,"\\",en.Global.WEB_SERVER_TYPE));
	                   pathFileContentType.addElement(contentType);
	                   
	                   if (padre!=null) 
	                      pathFilePadre.addElement(padre);
	                   else
	                      pathFilePadre.addElement("NOPADRE");
	                   
	                   completePathFile.addElement(sPath);
	                   isImpronta.addElement(impronta);
	                   isCreaOSettaImpronta.addElement(creaOrSetImpronta);
	                   idAllegatiInUpdate.addElement(id);
                   }
               }
               catch (Exception e) 
               { 
                     error="Profilo::setFileName()\n"+e.getMessage();
                     throw new Exception(error);
               }
         }
   }      
    
   protected void inizializza(Environment env) {             
      	  super.inizializza(env);    
	      
      	  pathFile = new Vector();
      	  pathFileContentType = new Vector();
          pathFileToDelete = new Vector();          
          pathFilePadre = new Vector();
          completePathFile = new Vector();
          fileStructRename = new Vector();
          isImpronta = new Vector();
          isCreaOSettaImpronta = new Vector();
          ACLuser = new Vector();
          ACLtype = new Vector();
          ACLExtraUser = new Vector();
          ACLExtraType = new Vector();
          file = new Vector();        
          docRiferimenti = new Vector();
          typeRiferimenti = new Vector();
          docRiferimentiToDelete = new Vector();
          typeRiferimentiToDelete = new Vector();          
          idAllegatiInUpdate = new Vector();          
   }
    
   private int getIndexFileByName(String nomeFile) throws Exception {
	       nomeFile = Global.replaceSpecialChrFile(nomeFile);
           
	       int size = ad.listaOggettiFile().size();
                              
           for(int i=0;i<size;i++) {
              String nome=((A_Oggetti_File)ad.listaOggettiFile().elementAt(i)).getFileName();

              //if (nome.equals(nomeFile)) return i;
              //Confronto tra stringhe non case sensitive 
              
              
              if (nome.toUpperCase().equals(nomeFile.toUpperCase())) return i;
              //if (nome.equals(nomeFile)) return i;
              
              //if (nome.equalsIgnoreCase(nomeFile)) return i;
                            
           }
           
           throw new Exception("Profilo::getIndexFileByName(String nomeFile) - Non trovato il file "+nomeFile);
   }

   /**
    * Restituisce l'indice del vettore che contiene il nomefile
    * di tipo: se improntaOrsyspdf=0 SYS_HASH (impronta),
    *          se improntaOrsyspdf=1 SYS_PDF
   */
   private int getIndexSysFileByName(String nomeFile,int improntaOrsyspdf) throws Exception {
           int size = ad.listaOggettiFile().size();
           String sNomeOriginale=nomeFile;
           
           for(int i=0;i<size;i++) {
              String nome=((A_Oggetti_File)ad.listaOggettiFile().elementAt(i)).getFileName();
              String idFormato=((A_Oggetti_File)ad.listaOggettiFile().elementAt(i)).getIdFormato();

              //SYS_HASH
              if (improntaOrsyspdf==Global.SYS_HASH)
                 nomeFile=Global.replaceAll(nomeFile,".","_")+".SYS_HASH";
              else if (improntaOrsyspdf==Global.SYS_PDF) 
            	 nomeFile=""+nomeFile;
              else
            	  throw new Exception("Profilo - Specificare un formato di SysFile esistente!");
              //if (nome.equals(nomeFile)) return i;
              //Confronto tra stringhe non case sensitive 
              if (improntaOrsyspdf==Global.SYS_HASH) { 
                  if (nome.toUpperCase().equals(nomeFile.toUpperCase())) return i;
              }
              else {
            	  if (nome.toUpperCase().equals(nomeFile.toUpperCase())&&
            	     idFormato.equals("-3")) return i;
              }
              
              //if (nome.equalsIgnoreCase(nomeFile)) return i;
              nomeFile=sNomeOriginale;          
           }
           
           if (improntaOrsyspdf==0)
        	   throw new Exception("Profilo::getIndexSysFileByName() - Non trovato il file impronta del file "+nomeFile);
           else
        	   throw new Exception("Profilo::getIndexSysFileByName() - Non trovato il file sysPDF "+nomeFile);
   }   

   /**
    * Restituisce l'indice del vettore che contiene il file p7m figlio
    * del file che ha per nome nomeFile passato in input
   */
   private int getIndexP7MFileByFileNamePadre(String nomeFile) throws Exception {
           int size = ad.listaOggettiFile().size();
           String sNomeOriginale=nomeFile;
           
           for(int i=0;i<size;i++) {
              String nome=((A_Oggetti_File)ad.listaOggettiFile().elementAt(i)).getFileName();
              String idFormato=((A_Oggetti_File)ad.listaOggettiFile().elementAt(i)).getIdFormato();

              //La prima parola ? il nome del file
              if (nome.indexOf(nomeFile)==0 && idFormato.equals("0")) {
            	  String sEtxP7m;
            	  
            	  sEtxP7m=nome.substring(nomeFile.length(),nome.length());
            	  
            	  //E' se stesso
            	  if (sEtxP7m.equals("")) continue;
            	  
            	  if (sEtxP7m.toLowerCase().indexOf("p7m")!=-1) {
            		  return i;            		  
            	  }
              }
           }
           
           return -1;
      	   //throw new Exception("Profilo::getIndexSysFileByName() - Non trovato il file sysPDF "+nomeFile);
   }   
   
   private void settaValoreInterna(String campo, Object valore, boolean append) {
  
            for(int i=0;i<campi.size();i++)            
              if (((String)campi.get(i)).equals(campo)) {
            	  
            	  if (append)
            		  valoriAppend.set(i,"S");
            	  else
            		  valoriAppend.set(i,"N");
            	  
                  valori.set(i,valore);                
                  return;
              }
                         
           campi.addElement(campo);
           valori.addElement(valore);
           
           if (append)
        	   valoriAppend.addElement("S");
           else
        	   valoriAppend.addElement("N");
   }
   
   public void salvaAlfresco() {
	       AlfrescoProfilo ap;
		
		   ap = new AlfrescoProfilo(aStruct.endPoint,aStruct.user,aStruct.password,
				                 	aStruct.pathFolder,idDocumento,aStruct.name,				                 
				                 	aStruct.getSimpleHTMLModello(campi,valori),
				                    AlfrescoProfilo._TYPECONTENT_HTML,
				                    aStruct.nameSpaceModelDocument,
				                    aStruct.prefixNameSpaceModelDocument,
				                    aStruct.typeContent);
		   
		   for(int i=0;i<campi.size();i++) {
			   String sValore=null;
			   java.sql.Date jsqlD=null;
			   
			   if (valori.get(i) instanceof String) {
				   sValore=((String)valori.get(i));
				   //Testo se ? una data
				   jsqlD=UtilityDate.StringToJavaSQLDate(sValore);
				   
				   if (jsqlD!=null) sValore=UtilityDate.dateToStringISO8601(jsqlD);
			   }				   
			   else if (valori.get(i) instanceof java.math.BigDecimal) {
				   java.math.BigDecimal bd = (java.math.BigDecimal)valori.get(i);
				   
				   sValore=bd.toString();
			   }
			   else if (valori.get(i) instanceof java.sql.Timestamp) {
				   Timestamp t = (Timestamp)valori.get(i);
				   jsqlD = new java.sql.Date(t.getTime());
				   sValore=UtilityDate.dateToStringISO8601(jsqlD);
			   }
			   else if (valori.get(i) instanceof java.sql.Date) {
				   sValore=UtilityDate.dateToStringISO8601((java.sql.Date)valori.get(i));
			   }
			   
			   ap.settaValore((String)campi.get(i),sValore);
		   }
		   
		   try {
			   InputStream[] is=null;
			   if (pathFile.size()>0) {
				  try{en.connect();}catch(Exception ei) {}
				  
			      try {
					 is=getFileFromProfiloAppoggio(en.getDbOp().getConn());   
				  }
		       	  catch(Exception e) {
		       		 try{en.disconnectClose();}catch(Exception ei) {}
		       		 alfrescoXMLRet=ap.preSalvaFallito(e.getMessage());
		       		 return;
		       	  }
			   }
			   
			   for(int i=0;i<pathFile.size();i++) {
				   String nome, percorso, ext;
				   
				   percorso=(String)pathFile.elementAt(i);
				   nome=Global.lastTrim(percorso,"/",en.Global.WEB_SERVER_TYPE);
				   ext=aStruct.getTypeContentAttach(Global.lastTrim(nome,".",en.Global.WEB_SERVER_TYPE));
	
				   ap.setFileName(nome,aStruct.associationName,ext,is[i]);
			   }
			   
			   try{en.disconnectClose();}catch(Exception ei) {}
		   }
	       catch(Exception e) {
	    	   try{en.disconnectClose();}catch(Exception ei) {}
	       }	       	       

		   for(int i=0;i<pathFileToDelete.size();i++) {
			   ap.setDeleteFileName((String)pathFileToDelete.elementAt(i),aStruct.associationName);
		   }
		   
		   //Tratto le ACL
		   for(int i=0;i<aStruct.getVAcl().size();i++) {
			   ap.settaACL(aStruct.getVAcl().get(i));
		   }		   
		   
		   alfrescoXMLRet=ap.salva();

	
   }
 
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
   protected void finalize() throws Throwable {
		try {
			ad.disconnect();			
		} catch (Exception e) {
			// Qualcuno aveva gi? chiuso la Accedi Documento
			// che io avevo connesso per recuperare 
			// l'eventuale InputStream nella chiamata
			// alla getFileStream
		}
		super.finalize();
	}
   
   private InputStream[] getFileFromProfiloAppoggio(Connection cn) throws Exception {
	   	   InputStream[] is=null;    	   	   	       	   	 
	   	   
	       try {	    	   	    	 
	    	   Profilo pApp = new Profilo(idDocumento);
		       pApp.initVarEnv(en.getUser(),en.getPwd(),cn);
		       
		       if (pApp.accedi(Global.ACCESS_ATTACH).booleanValue()) {
		    	   is = new InputStream[pathFile.size()];
		    	   for(int i=0;i<pathFile.size();i++) {
		    		   String nome, percorso;
					   
					   percorso=(String)pathFile.elementAt(i);
					   nome=Global.lastTrim(percorso,"/",en.Global.WEB_SERVER_TYPE);
					   
					   is[i]=pApp.getFileStream(nome);
		    	   }
		    	   
		       }
		       else
		    	   throw new Exception("Errore recupero dei file dal profilo (Accedendo allo stesso).\nErrore:"+pApp.getError());
	       }
	       catch(Exception e) {	    	  
	    	   throw new Exception("Errore recupero dei file dal profilo.\nErrore:"+e.getMessage());
	       }
	       	     
	       return is;
   }

   public Exception getLastException() {
	    return lastException;
   }

   public void setUltAggiornamento(String ultAggiornamento) {
	      this.ultAggiornamento = ultAggiornamento;
   }

   public String getAlfrescoXMLRet() {
	   	  return alfrescoXMLRet;
   }
   
   public String getDataCreazione() {
	      return dataCreazione;
   }	
}

class AlfrescoStruct {
	  String endPoint, user, password; 
	  String pathFolder, name; 
	  String nameSpaceModelDocument;
	  String prefixNameSpaceModelDocument; 
	  String typeContent;
	  String associationName;
	  Vector<AlfrescoACL> vAcl;
	
	  public AlfrescoStruct(String endPoint, String user, String password, 
			                String pathFolder, String name, 
			                String nameSpaceModelDocument, 
			                String prefixNameSpaceModelDocument, 
			                String typeContent,String associationName) {
		     this.endPoint=endPoint;
		     this.user=user;
		     this.password=password;
			 this.pathFolder=pathFolder;
			 this.name=name; 
			 this.nameSpaceModelDocument=nameSpaceModelDocument;
			 this.prefixNameSpaceModelDocument=prefixNameSpaceModelDocument; 
			 this.typeContent=typeContent;
			 this.associationName=associationName;
			 vAcl= new Vector<AlfrescoACL>();
	  }
	  
	  public String getSimpleHTMLModello(Vector campiProfilo, Vector valoriProfilo) {
		     StringBuffer sHtml = new StringBuffer("<html>");
		     
		     sHtml.append("<head>");		     		     
		     sHtml.append("<body>");
		     
		     for(int i=0;i<campiProfilo.size();i++) 		    	 
		    	 sHtml.append("<b>"+campiProfilo.get(i)+":</b> "+valoriProfilo.get(i)+"<BR><BR>");		    	 		    		 
		     
		     
		     sHtml.append("</body>");		     
		     sHtml.append("</head>");
		     sHtml.append("</html>");
		     
		     return sHtml.toString();
	  }
	  
	  public String getTypeContentAttach(String ext) {
		     String contAtt=AlfrescoProfilo._TYPECONTENT_HTML;
		     
		     ext=ext.toUpperCase();
		     
		     if (ext.equals("JPG") || ext.equals("JPEG")) 
		    	 contAtt= AlfrescoProfilo._TYPECONTENT_JPG;
		     else if (ext.equals("BMP")) 
		    	 contAtt= AlfrescoProfilo._TYPECONTENT_BMP;
		     else if (ext.equals("GIF")) 
		    	 contAtt= AlfrescoProfilo._TYPECONTENT_GIF;
		     else if (ext.equals("DOC")) 
		    	 contAtt= AlfrescoProfilo._TYPECONTENT_DOC;		     
		     else if (ext.equals("PDF")) 
		    	 contAtt= AlfrescoProfilo._TYPECONTENT_PDF;		     
		     
		     return contAtt;
	  }
	  
	  public void addCompetenza(String person,String acl) {
		  	 vAcl.add(new AlfrescoACL(person,acl,AlfrescoACL._ACCEPTED));
	  }
	  
	  public void romoveCompetenza(String person,String acl) {
		     vAcl.add(new AlfrescoACL(person,acl,AlfrescoACL._DECLINED));
	  }

	  public Vector<AlfrescoACL> getVAcl() {
		     return vAcl;
	  }	  
	  
  
}

