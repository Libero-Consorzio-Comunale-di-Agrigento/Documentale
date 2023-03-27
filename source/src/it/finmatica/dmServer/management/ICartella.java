package it.finmatica.dmServer.management;

import java.sql.*;
import java.util.Vector; 
import it.finmatica.jfc.dbUtil.*;
import it.finmatica.dmServer.util.*;
import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.competenze.*;
  
/** 
 * Gestione di un Cartella del documentale.<BR>
 * <BR>
 * Esempio di <B>INSERIMENTO:</B><BR>
 * <BR> 		
 * 		   // Creazione della Cartella passando<BR>
 * 		   // Area, Codice Modello, Radice (nell'esempio sistema),<BR>
 * 		   // Percorso di inserimento della cartella, nome della cartella<BR>
 * <BR>
 * 		   ICartella Ic = new ICartella("AD4","MODELLOCARTELLA_AD4",
                                        Global.ROOT_SYSTEM_FOLDER,"Folder1\\Folder2'",
                                        "Prova Inserimento Cartella");<BR>
 * <BR> 
 * 		   // Inizializzazione delle variabili<BR>
 * 		   // Viene passato User AD4, Password AD4, Connection<BR>
 * 		   // in alternativa alla connection è possibile passare<BR>
 * 		   // il percorso del file di properties <BR>
 * 		   // es: "S:\\SI4\\GD4\\jGD4\\lib\\gd4dm.properties"<BR>
 * <BR>
 * 		   Ic.initVarEnv("GDM","GDM", conn);<BR>
 * <BR>    
 * 		   try {<BR>
 * 		   &nbsp;&nbsp;&nbsp;Ic.addValue("Numero_Fascicolo","33/A");<BR>
 *         &nbsp;&nbsp;&nbsp;Ic.settaACL("AA4",Global.NO_ACCESS);<BR>
 *         &nbsp;&nbsp;&nbsp;Ic.addInObject("293","Q");<BR>
 * <BR>        
 *         &nbsp;&nbsp;&nbsp;Ic.insert();<BR> 
 *         catch (Exception e) {<BR>
 *         &nbsp;&nbsp;&nbsp;//GESTIONE DELL'ERRORE...<BR>
 *         }<BR>   
 * <BR>            
 * Esempio di <B>MODIFICA:</B><BR>              
 * <BR>		   
 * 		   // Accesso alla cartella con identificativo 1257<BR>
 * 		   // In alternativa si potrebbe utilizzare il costruttore<BR>
 * 		   // con tre parametri (area,codMod,codRich).<BR>
 * 		   // Il codice richiesta di un profilo cartella<BR>
 * 		   // corrisponderà sempre all'indentificativo della cartella
 * <BR>
 * 		   ICartella Ic = new ICartella("1257");<BR>
 * <BR>
 * 		   // Inizializzazione delle variabili<BR>
 * 		   // Viene passato User AD4, Password AD4, Connection<BR>
 * 		   // in alternativa alla connection è possibile passare<BR>
 * 		   // il percorso del file di properties <BR>
 * 		   // es: "S:\\SI4\\GD4\\jGD4\\lib\\gd4dm.properties"<BR>
 * <BR>
 * 		   Ic.initVarEnv("GDM","GDM", conn);<BR>
 * <BR>    
 * 		   try {<BR>
 * 		   &nbsp;&nbsp;&nbsp;Ic.addValue("Numero_Fascicolo","66/A");<BR>
 *         &nbsp;&nbsp;&nbsp;Ic.addInObject("293","D");<BR>
 * <BR>        
 *         &nbsp;&nbsp;&nbsp;Ic.update();<BR> 
 *         catch (Exception e) {<BR>
 *         &nbsp;&nbsp;&nbsp;//GESTIONE DELL'ERRORE...<BR>
 *         }<BR> 
 * <BR> 
 * <B>Cenni sulle connessioni (gestione dei commit e dei rollback):</B><BR>
 * E' possibile inizializzare la cartella con il meotodo initVarEnv passando la connection
 * oppure il percorso del file di properties.<BR><BR>
 * Se siamo in presenza del file di properties, la connessione al database viene gestita 
 * internamente in maniera atomica: viene creata una sessione al richiamo dei metodi insert()/update()/delete()
 * che si premura di effettuare un commit o un rollback "interno" a seconda se le operazioni 
 * di registrazione della cartella sono andate rispettivamente a buon fine oppure sono fallite.<BR><BR> 
 * Se viene passata una connection, la gestione è tutta a carico dell'utente: sarà quest'ultimo
 * che dovrà preoccuparsi o meno di effettuare i commit o i rollback, eccezion fatta per i casi
 * in cui si verifichino degli errori al richiamo del salvataggio della cartella.
 * In tal caso è stata prevista una gestione con dei savepoint interni; la classe ICartella,
 * in caso di errore al salvataggio, effettuerà un rollback interno automatico fino al savepoint
 * inizialmente creato, lasciando invariate nella connection passata dall'esterno tutte 
 * le operazioni precendenti a quelle interne alla cartella. 
 * 
 * @author  D. Scandurra, G. Mannella
 * @version 2.8
 *
*/

public class ICartella
{  
   /** 
    * Elenco di identificativi degli
    * oggetti contenuti nella Cartella
    * per effettuare l'inserimento
   */
   private Vector inObject;
 
   /** 
    * Elenco di identificativi degli
    * oggetti contenuti nella Cartella
    * per effettuare la cancellazione
   */
   private Vector outObject;
 
   /**
    * Elenco dei valori del profilo
    * della cartella     
   */
   private Vector profileValue;

   /**
    * Codice Modello   di profilo-cartella
    * Area             di profilo-cartella
    * Codice Richiesta di profilo-cartella
   */
   
   private String cmFolder;
   private String crFolder;
   private String areaFolder;

   /** 
    * Identificativo del profilo Cartella 
   */
   private String profileFolder;

   /** 
    * Identificativo della Cartella padre
   */
   private String identifierUpFolder;

   /** 
    * Identificativo della Cartella
   */
   private String identifierFolder;
  
   /** 
    * Identificativo della ViewCartella
   */
   private String identifierViewFolder;

   /** 
    * Nome della Cartella
   */
   private String nameFolder;

   /** 
    * Percorso  che  porta alla  Cartella
   */
   private String pathFolder;

   /** 
    * Radice:   U = Utente,  S = Sistema, A=Altra Wrksp
   */
   private String rootFolder;
   private String rootName;
   
   /**
    * Attributi di Riferimento
   */
   private Vector cartRiferimenti;
   private Vector typeRiferimenti;
   
   private Vector cartRiferimentiToDelete;
   private Vector typeRiferimentiToDelete;   

   /**
    * ATTRIBUTI DI ACL 
   */
   private Vector ACLuser;
   private Vector ACLtype;
   private Vector ACLversus;
   
   private boolean bEscludiControlloCompetenze=false;
   
   /**
    * Variabile di ambiente 
   */  
   private Environment en;

   private ElapsedTime elpsTime;   
   
   private boolean bCreaLink = true;

   /**
    * Costruttore da utilizzare esclusivamente in fase di
    * accesso o modifica di una cartella conoscendone la
    * chiave primaria
    * 
    * @param idFolder identificativo della cartella da accedere/modificare
   */
   public ICartella(String idFolder) throws Exception {
          this.identifierFolder  =  idFolder;
   }

   /**
    * Costruttore da utilizzare esclusivamente in fase di
    * accesso o modifica di una cartella conoscendone la
    * tripla area/codice modello/codice richiesta
    * 
    * @param areaProfileFolder area del profilo-cartella
    * @param cmProfileFolder codice modello del profilo-cartella
    * @param crProfileFolder codice richiesta del profilo-cartella
   */   
   public ICartella(String areaProfileFolder, String cmProfileFolder, String crProfileFolder) throws Exception {         
          this.cmFolder          = cmProfileFolder;
          this.crFolder          = crProfileFolder;
          this.areaFolder        = areaProfileFolder;
   }

   /**
    * Costruttore da utilizzare esclusivamente in creazione
    * di una cartella passando workspace e percorso (compreso
    * di nome cartella in questione)
    * @param rootFolder radice dell'albero. Possibili valori:<BR>
    * 		 1. Global.ROOT_SYSTEM_FOLDER<BR>
    * 		 2. Global.ROOT_USER_FOLDER<BR>
    *        3. Nome della workspace, se diversa da una di quelle sopra.
    * @param pathFolder percorso della cartella, es.:<BR>
    * 		 Folder1\\Folder2 (Folder2 è la cartella da modificare)
   */ 
   public ICartella(String rootFolder, String pathFolder) throws Exception {
       	  this.rootFolder  = rootFolder;   
	      this.rootName  = rootFolder;         
          this.pathFolder=pathFolder;
   }   

   /**
    * Costruttore da utilizzare esclusivamente in creazione
    * di una cartella passando area, codiceModello,
    * radice, percorso, nome
    * 
    * @param areaProfileFolder area del profilio-cartella
    * @param cmProfileFolder codice modello del profilio-cartella
    * @param rootFolder radice dell'albero. Possibili valori:<BR>
    * 		 1. Global.ROOT_SYSTEM_FOLDER<BR>
    * 		 2. Global.ROOT_USER_FOLDER<BR>
    *        3. Nome della workspace, se diversa da una di quelle sopra.
    *        4. "" Se si vuole creare una workspace (pathFolder dovrà essere pure "") 
    * @param pathFolder percorso dove inserire la cartella, es.:<BR>
    * 		 Folder1\\Folder2
    * @param nameFolder nome della cartella
   */ 
   public ICartella(String areaProfileFolder, String cmProfileFolder,
                   String rootFolder, String pathFolder, String nameFolder) throws Exception {
          this.areaFolder=areaProfileFolder;
          this.cmFolder=cmProfileFolder;
          this.rootFolder=rootFolder;
          this.rootName  = rootFolder;
          this.pathFolder=pathFolder;
          this.nameFolder=nameFolder;
   }

   /**
    * Costruttore da utilizzare esclusivamente in creazione
    * di una cartella passando area, codiceModello e codice richiesta
    * di un profilo già creato in precendenza.
    * Radice e identificativo della cartella padre
    * per poter collocare il nuovo folder 
    * nell'albero.
    * Il nome della cartella verrà ereditato dal campo NOME del profilo
    * cartella 
    * 
    * @param areaProfileFolder area del profilio-cartella
    * @param cmProfileFolder codice modello del profilio-cartella
    * @param crProfileFolder codice richiesta del profilio-cartella     
    * @param rootFolder radice dell'albero. Possibili valori:<BR>
    * 		 1. Global.ROOT_SYSTEM_FOLDER<BR>
    * 		 2. Global.ROOT_USER_FOLDER<BR>
    *        3. Nome della workspace, se diversa da una di quelle sopra.
    *        4. "" Se si vuole creare una workspace (pathFolder dovrà essere pure "") 
    * @param idUpFolder identificativo della cartella padre 
   */ 
   public ICartella(String areaProfileFolder, String cmProfileFolder, String crProfileFolder,
                    String rootFolder, long idUpFolder) throws Exception {
          this.areaFolder         = areaProfileFolder;
          this.cmFolder           = cmProfileFolder;
          this.crFolder           = crProfileFolder;
          this.rootFolder         = rootFolder;
          this.rootName           = rootFolder;
          this.identifierUpFolder = idUpFolder+"";
   }  

   /**
    * Costruttore da utilizzare esclusivamente in creazione
    * di una cartella passando area, codiceModello e codice richiesta
    * di un profilo già creato in precendenza.
    * Radice per poter collocare il nuovo folder 
    * nell'albero sotto la wrksp.
    * Il nome della cartella verrà ereditato dal campo NOME del profilo
    * cartella 
    * 
    * @param areaProfileFolder area del profilio-cartella
    * @param cmProfileFolder codice modello del profilio-cartella
    * @param crProfileFolder codice richiesta del profilio-cartella     
    * @param rootFolder radice dell'albero. Possibili valori:<BR>
    * 		 1. Global.ROOT_SYSTEM_FOLDER<BR>
    * 		 2. Global.ROOT_USER_FOLDER<BR>
    *        3. Nome della workspace, se diversa da una di quelle sopra.
    *        4. "" Se si vuole creare una workspace (pathFolder dovrà essere pure "") 
   */ 
   public ICartella(String areaProfileFolder, String cmProfileFolder, String crProfileFolder,
                    String rootFolder) throws Exception {
          this.areaFolder         = areaProfileFolder;
          this.cmFolder           = cmProfileFolder;
          this.crFolder           = crProfileFolder;
          this.rootFolder         = rootFolder;
          this.rootName           = rootFolder;
          this.identifierUpFolder = "USALAWRKSP";
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
            throw new Exception("ICartella::initVarEnv()\n"+e.getMessage());             
          }
   }

   /**
    * @param user utente di AD4
    * @param passwd password di AD4
    * @param ruolo ruolo dell'utente di AD4
    * @param appl applicativo chiamante
    * @param ente ente chiamante
    * @param lib libreria
    * @param ini percorso del file di properties
   */   
   public void initVarEnv(String user,String passwd,String ruolo,String appl,String ente,String lib,String ini) throws Exception {
          try {
            Environment env = new Environment(user,passwd,appl,ente,lib,ini);
            inizializza(env);
          }
          catch (Exception e) {
            throw new Exception("ICartella::initVarEnv()\n"+e.getMessage());             
          }         
   }

   /**
    * @param user utente di AD4
    * @param passwd password di AD4
    * @param ruolo ruolo dell'utente di AD4
    * @param appl applicativo chiamante
    * @param ente ente chiamante
    * @param lib libreria
    * @param cn connection
   */
   public void initVarEnv(String user,String passwd,String ruolo,String appl,String ente,String lib,Connection cn) throws Exception {
          try {
            Environment env = new Environment(user,passwd,appl,ente,lib,cn);
            inizializza(env);
          }
          catch (Exception e) {
            throw new Exception("ICartella::initVarEnv()\n"+e.getMessage());             
          }
   }

   /**
    * @param user utente di AD4
    * @param passwd password di AD4
    * @param appl applicativo chiamante
    * @param ente ente chiamante
    * @param ini percorso del file di properties
   */
   public void initVarEnv(String user,String passwd,String appl,String ente, String ini) throws Exception {
          initVarEnv( user, passwd, "GDM", appl, ente , null, ini);         
   }

   /**
    * @param user utente di AD4
    * @param passwd password di AD4
    * @param appl applicativo chiamante
    * @param ente ente chiamante
    * @param cn connection
   */   
   public void initVarEnv(String user,String passwd,String appl,String ente, Connection cn) throws Exception {
          initVarEnv( user, passwd, "GDM", appl, ente , null, cn);         
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
   public void initVarEnv(String user,String passwd, String ini) throws Exception {
          initVarEnv( user, passwd, "GDM", "", "" , null, ini);         
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
   public void initVarEnv(String user,String passwd, Connection cn) throws Exception {
          initVarEnv( user, passwd, "GDM", "", "" , null, cn);         
   }

   /**
    * Metodo che serve a settare l'identificativo
    * del profilo-cartella
    * 
    * @param newProfilefolder identificativo del profilo-cartella
    * @see <a href="ICartella.html#getProfileFolder()">getProfileFolder</a> 
   */   
   public void setProfileFolder(String newProfilefolder) {
          profileFolder=newProfilefolder;         
   }

   /**
    * Metodo che serve a restituire l'identificativo
    * del profilo-cartella
    * 
    * @return identificativo del profilo-cartella
    * @see <a href="ICartella.html#setProfileFolder(java.lang.String)">setProfileFolder</a> 
   */      
   public String getProfileFolder() {
          return profileFolder;
   }    

   /**
    * Metodo che serve a settare l'identificativo
    * della cartella
    * 
    * @param idFolder identificativo della cartella
    * @see <a href="ICartella.html#getIdentifierFolder()">getIdentifierFolder</a> 
   */
   public void setIdentifierFolder(String idFolder) {
          this.identifierFolder=idFolder;
   }    

   /**
    * Metodo che serve a restituire l'identificativo
    * della cartella
    * 
    * @return identificativo della cartella
    * @see <a href="ICartella.html#setIdentifierFolder(java.lang.String)">setIdentifierFolder</a> 
   */   
   public String getIdentifierFolder() {
          return this.identifierFolder;
   }
   
   /**
    * Metodo che serve a settare l'identificativo
    * della cartella padre
    * 
    * @param idUpFolder identificativo della cartella padre
    * @see <a href="ICartella.html#getIndentifierUpFolder()">getIndentifierUpFolder</a> 
   */   
   public void setIndentifierUpFolder(String idUpFolder) {
          this.identifierUpFolder=idUpFolder;
   }
   
   /**
    * Metodo che serve a restituire l'identificativo
    * della cartella padre
    * 
    * @return identificativo della cartella padre
    * @see <a href="ICartella.html#setIndentifierUpFolder(java.lang.String)">setIndentifierUpFolder</a> 
   */  
   public String getIndentifierUpFolder() {
          return this.identifierUpFolder;
   }   

   /**
    * Metodo che serve a settare il nome della cartella
    * 
    * @param nameFolder nome della cartella
    * @see <a href="ICartella.html#getNameFolder()">getNameFolder</a> 
   */
   public void setNameFolder(String nameFolder) {
          this.nameFolder=nameFolder;
   }   
   
   /**
    * Metodo che serve a restituire il nome della cartella
    * 
    * @return nome della cartella
    * @see <a href="ICartella.html#setNameFolder(java.lang.String)">setNameFolder</a> 
   */
   public String getNameFolder() {
          return this.nameFolder;
   }
   
   /**
    * Metodo che serve a settare il percorso della cartella
    * 
    * @param newPathFolder percorso dalla radice al padre della cartella
    * @see <a href="ICartella.html#getPathFolder()">getPathFolder</a> 
   */
   public void setPathFolder(String newPathFolder) {
          pathFolder=newPathFolder;
   }

   /**
    * Metodo che serve a restituire il percorso della cartella
    * 
    * @return percorso dalla radice al padre della cartella
    * @see <a href="ICartella.html#setPathFolder(java.lang.String)">setPathFolder</a> 
   */
   public String getPathFolder() {
          return pathFolder;
   }   
 
   /**
    * Metodo che serve a settare la radice della cartella
    * 
    * @param newRootFolder radice della cartella
    * @see <a href="ICartella.html#getRootFolder()">getRootFolder</a> 
   */
   public void setRootFolder(String newRootFolder) {
          rootFolder=newRootFolder;
   }
   
   /**
    * Metodo che serve a restituire la radice della cartella
    * 
    * @return radice della cartella
    * @see <a href="ICartella.html#setRootFolder(java.lang.String)">setRootFolder</a> 
   */  
   public String getRootFolder() throws Exception {
	      if (rootName==null) return null;   
	   
	      if (rootName.equals(Global.ROOT_USER_FOLDER))
              return "-2";
           else if (rootName.equals(Global.ROOT_SYSTEM_FOLDER))
              return "-1";
           else {
        	  return existsWorkSpaceFolder(rootName);
           }	     
   }   
   
   /**
    * Metodo che aggiunge, alla lista dei valori da inseire
    * nel profilo-cartella, la coppia (campo,valore).
    * 
    * @param key campo del profilo-cartella
    * @param val valore da aggiungere al campo del profilo-cartella
   */
   public void addValue(String key,String val) {
          Valori v = new Valori(key,val);

          profileValue.add(v);
   }

   /**
    * Metodo che aggiunge, alla lista dei valori da inseire
    * nel profilo-cartella, la coppia (campo,valore), con 
    * valore del tipo Data
    * 
    * @param key campo del profilo-cartella
    * @param val valore da aggiungere al campo del profilo-cartella
   */   
   public void addValue(String campo, java.sql.Date valore) {           
          Valori v = new Valori(campo,valore);

          profileValue.add(v);
   }   
   
   /**
    * Metodo che aggiunge, alla lista dei valori da inseire
    * nel profilo-cartella, la coppia (campo,valore), con 
    * valore dei tipo Timestamp
    * 
    * @param key campo del profilo-cartella
    * @param val valore da aggiungere al campo del profilo-cartella
   */   
   public void addValue(String campo, java.sql.Timestamp valore) {           
          Valori v = new Valori(campo,valore);

          profileValue.add(v);
   }
   
   /**
    * Metodo che aggiunge, alla lista dei valori da inseire
    * nel profilo-cartella, la coppia (campo,valore), con 
    * valore dei tipo Decimal
    * 
    * @param key campo del profilo-cartella
    * @param val valore da aggiungere al campo del profilo-cartella
   */   
   public void addValue(String campo, java.math.BigDecimal valore) {           
          Valori v = new Valori(campo,valore);

          profileValue.add(v);
   }      

   public void addInObject(String objectIdentifier, String typeObject) {
	      addInObject(objectIdentifier,typeObject,false);
   }
  
   /**
    * Metodo che aggiunge un oggetto (objectIdentifier,typeObject) alla lista 
    * degli oggetti da inserire in cartella
    * 
    * @param objectIdentifier identificativo dell'oggetto da inserire in cartella 
    * @param typeObject tipo di oggetto da inserire in cartella. Valori possibili:<BR>
    * 		            "D" per documento , "X" per collegamenti a cartella, "Q" per query
    * @see <a href="ICartella.html#deleteInObject(java.lang.String, java.lang.String)">deleteInObject(String,String)</a> 
   */
   public void addInObject(String objectIdentifier, String typeObject, boolean bIsAutomatic) {
          Valori v = new Valori(objectIdentifier,typeObject);

          v.setBCheck(bIsAutomatic);
          
          inObject.add(v);
   }
   
   /**
    * Metodo che aggiunge un oggetto (objectIdentifier,typeObject) alla lista 
    * degli oggetti da eliminare dalla cartella
    * 
    * @param objectIdentifier identificativo dell'oggetto da eliminare dalla cartella 
    * @param typeObject tipo di oggetto da eliminare dalla cartella. Valori possibili:<BR>
    * 		            "D" per documento , "C" per cartella, "Q" per query
    * @see <a href="ICartella.html#addInObject(java.lang.String, java.lang.String)">addInObject(String,String)</a> 
   */
   public void deleteInObject(String objectIdentifier, String typeObject) {
          Valori v = new Valori(objectIdentifier,typeObject);

          outObject.add(v);   
   }
  
   /**
    * Metodo che restituisce il vettore degli oggetti contenuti in cartella.
    * Gli elementi del vettore sono istanze della classe it.finmatica.management.Valori.
    * Esempio di utilizzo:<BR>
    * <BR>
    * <B>C</B>
    * <BR>
    * &nbsp;&nbsp;Valori v = (Valori)inObject.get(0);<BR>
    * &nbsp;&nbsp;System.out.println("identificativo del primo oggetto: "+(String)v.key);<BR>
    * &nbsp;&nbsp;System.out.println("tipo del primo oggetto: "+(String)v.value);<BR>    
    * 
    * @return vettore di oggetti contenuti nella cartella   
   */   
   public Vector getInElementList() {
          return inObject;
   }
   
   
   //public Vector getInElementList(String area,String cm) {
	    /*  Vector vApp = new Vector;
	      
	      for(int i=0;i<inObject.size();i++) {
	    	  
	      }
          return inObject;*/
  //s }
   

   /**
    * Metodo che restituisce la stringa degli identificativi 
    * di documenti contenuti in cartella.
    * Gli identificativi sono divisi dal separatore "@".
    * Se il parametro in input è true la stringa sarà composta come segue:<BR>
    * D,idDocumento1,areaDocumento1@D,idDocumento2,areaDocumento2@......<BR>
    * altrimenti (se false)<BR>
    * idDocumento1@idDocumento2@......<BR>
    * 
    * @param b_type tipo di stringa restituita (come in esempio)
    * @return Stringa formata da identificativi di documenti separati da "@".   
   */
   public String getDocumentInFolder(boolean b_type) throws Exception {      
          String elencoDoc;
         
          elencoDoc=getElementInFolder("D",true,b_type,null,null,false);
         
          return elencoDoc;
   }
   
   /**
    * Metodo che restituisce la stringa degli identificativi 
    * degli contenuti in cartella.
    * Gli identificativi sono divisi dal separatore "@".
    * Il parametro type potra assumere il valore<BR>
    * C per Cartelle<BR>
    * Q per Query<BR>
    * D per Documenti<BR>
    * T per Tutti<BR>
    * area è cm servono per eventuale filtro ulteriore
    * Verrà restituita una stringa formata in questo modo:<BR>
    * type1,id1@type2,id2@....@typen,idn
    * 
    * @param type tipo di oggetto da ricercare (T=Tutti)
    * @param area area sulla quale filtrare
    * @param cm   Codice modello sul quale filtrare
    * @return Stringa formata da identificativi di elementi separati da "@".   
   */
   public String getElementInFolder(String type, String area, String cm) throws Exception {      
          String elencoDoc;
         
          elencoDoc=getElementInFolder(type,false,false,area,cm,true);
         
          return elencoDoc;
   }   

   /**
    * Metodo che aggiunge alla lista delle ACL della cartella
    * la coppia (Utente,TipoACL).
    * Esempio:<BR>
    * <BR>
    * Ic.settaACL("AA4",Global.NORMAL_ACCESS);<BR>
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
          ACLversus.addElement("");
   }
   
   /**
    * Metodo che aggiunge alla lista delle ACL della cartella
    * la coppia (Utente,Competenza).
    * Esempio:<BR>
    * <BR>
    * addCompetenza("AA4",Global.ABIL_LETT);<BR>
    * <BR>
    * Verranno inserite le competenze di Lettura
    * per l'utente AA4
    *
    * @param user utente a cui assegnare le ACL
    * @param competenza
   */ 
   public void addCompetenza(String user, String competenza) {
          ACLuser.addElement(user);
          ACLtype.addElement(competenza);
          ACLversus.addElement("S");
   }   
   
   /**
    * Metodo che rimuove dalla lista delle ACL della cartella
    * la coppia (Utente,Competenza).
    * Esempio:<BR>
    * <BR>
    * removeCompetenza("AA4",Global.ABIL_LETT);<BR>
    * <BR>
    * Verranno rimosse le competenze di Lettura
    * per l'utente AA4
    *
    * @param user utente a cui rimuovere le ACL
    * @param competenza
   */ 
   public void removeCompetenza(String user, String competenza) {
          ACLuser.addElement(user);
          ACLtype.addElement(competenza);
          ACLversus.addElement("N");
   }   
   
  /**
    * Metodo che aggiunge, alla lista dei riferimenti da inserire
    * nella cartella, la coppia (identificativo della cartella da riferire,tipo di relazione).
    * Da utilizzare esclusivamente in <B>Inserimento/Aggiornamento
    * della cartella.</B>
    * 
    * @param idCartRif identificativo della cartella da riferire a questa ICartella
    * @param typeRiferimento tipo di Relazione		 
    * @see <a href="Profilo.html#setDeleteRiferimento(java.lang.String, java.lang.String)">setDeleteRiferimento(String,String)</a> 	   
   */   
   public void settaRiferimento(String idCartRif,String typeRiferimento) {
          cartRiferimenti.addElement(idCartRif);
          typeRiferimenti.addElement(typeRiferimento);
   }

   /**
    * Metodo che aggiunge, alla lista dei riferimenti da eliminare
    * nella cartella, la coppia (identificativo della cartella da riferire,tipo di relazione).
    * Da utilizzare esclusivamente in <B>Aggiornamento del profilo</B>.
    * 
    * @param idCartRif identificativo della Cartella da eliminare come riferimento a questa ICartella
    * @param typeRiferimento tipo di Relazione		 
    * @see <a href="Profilo.html#settaRiferimento(java.lang.String, java.lang.String)">settaRiferimento(String,String)</a> 	   
   */   
   public void setDeleteRiferimento(String idCartRif,String typeRiferimento) {
          cartRiferimentiToDelete.addElement(idCartRif);
          typeRiferimentiToDelete.addElement(typeRiferimento);
   }    
   
   /**
    * Metodo che provvede ad inserire una cartella se questa non esiste.
    * Vengono effettuati i seguenti passi:<BR>
    *	&nbsp;&nbsp;&nbsp;1) Verifica se sulla cartella padre si hanno
    *                        i diritti di scrittura<BR>
    *   &nbsp;&nbsp;&nbsp;2) Creazione del profilo a partire dai valori passati
    *                        se questo già non esiste<BR>
    *   &nbsp;&nbsp;&nbsp;3) Creazione della cartella<BR>
    *   &nbsp;&nbsp;&nbsp;4) Creazione della view_cartella<BR>
    *   &nbsp;&nbsp;&nbsp;5) Creazione del link con il padre<BR>         
    *   &nbsp;&nbsp;&nbsp;6) Inserimento delle competenze standard per Profilo-Cartella e Cartella<BR>   
    *   &nbsp;&nbsp;&nbsp;7) Inserimento delle eventuali (ACL) per la Cartella
    *   
    *   Il parametro bMakeLink se false evita di aggiungere il link al padre della cartella
    *   creata
    * 
   */
   public void insert(boolean bMakeLink) throws Exception {  
	      bCreaLink=bMakeLink;
	      
	      insert();
   }

   /**
    * Metodo che provvede ad inserire una cartella se questa non esiste.
    * Vengono effettuati i seguenti passi:<BR>
    *	&nbsp;&nbsp;&nbsp;1) Verifica se sulla cartella padre si hanno
    *                        i diritti di scrittura<BR>
    *   &nbsp;&nbsp;&nbsp;2) Creazione del profilo a partire dai valori passati
    *                        se questo già non esiste<BR>
    *   &nbsp;&nbsp;&nbsp;3) Creazione della cartella<BR>
    *   &nbsp;&nbsp;&nbsp;4) Creazione della view_cartella<BR>
    *   &nbsp;&nbsp;&nbsp;5) Creazione del link con il padre<BR>         
    *   &nbsp;&nbsp;&nbsp;6) Inserimento delle competenze standard per Profilo-Cartella e Cartella<BR>   
    *   &nbsp;&nbsp;&nbsp;7) Inserimento delle eventuali (ACL) per la Cartella
    * 
   */
   public void insert() throws Exception {                  
          IDbOperationSQL dbOp = null;
          elpsTime.start("Insert Cartella","Inizio Insert");
          
          try {
            en.connect();         
            if (en.Global.CONNECTION!=null) en.createSavePoint();         
            dbOp = en.getDbOp();     
 
           //Inserimento del profilo (passo 1-2)
            elpsTime.start("insertProfilo_Folder","insertProfilo_Folder");
            if (profileFolder==null) 
                insertProfilo_Folder(dbOp);
            elpsTime.stop();
  
            elpsTime.start("insertFolder","insertFolder");
            //Inserimento della cartella e view_cartella (passo 3-4)     
            insertFolder(dbOp);
            elpsTime.stop();

            //Se sto creando una workspace non ho bisogno
            //di creare le competenze per nessun utente.
            //Sarà poi l'amministratore (che vede le workspace
            //a presindere dalle competenze) che le assegnerà
            //tramite apposita form
            //14/12/2006....reintrodotto il meccanismo di 
            //assegnazione delle competenze
            //if (!identifierUpFolder.equals("0")) {
            	elpsTime.start("assegnaCompetenzeCartella","assegnaCompetenzeCartella");            
            	assegnaCompetenzeCartella(dbOp);
            	elpsTime.stop();
            //}
           
            elpsTime.start("salvaACL","salvaACL");
            salvaACL("I");
            elpsTime.stop();
           
            en.disconnectCommit();
            
            elpsTime.stop();
          }
          catch (Exception e) {        	
        	if (en.Global.CONNECTION!=null) en.rollbackToSavePoint();
            en.disconnectRollback();                       
            throw new Exception("ICartella::insert()\n" + e.getMessage());
          }
   }

   /**
    * Metodo che provvede ad aggiornare una cartella
    * Vengono effettuati i seguenti passi:<BR>
    *	&nbsp;&nbsp;&nbsp;1) Aggiornamento dei valori del profilo<BR>
    *   &nbsp;&nbsp;&nbsp;2) Aggiornamento del nome cartella su cartelle 
    *                        in funzione del nuovo valore sul profilo-cartella<BR>
    *   &nbsp;&nbsp;&nbsp;3) Verifica della competenza di creazione sulla cartella<BR>
    *   &nbsp;&nbsp;&nbsp;4) Inserimento degli oggetti nella cartella 
    *                        (escludendo quelli già presenti)<BR>
    *   &nbsp;&nbsp;&nbsp;5) Rimozione degli oggetti dentro la cartella<BR>          
   */  
   public void update() throws Exception {      
          IDbOperationSQL dbOp = null;
          
          //System.out.println("************************** MANNYSTE UPDATE CARTELLA INIZIO ************************");
          try {
        	  
             en.connect();
             
             if (en.Global.CONNECTION!=null) en.createSavePoint();
             dbOp = en.getDbOp();
          
             boolean wasNull=false;    
             try {
               //Se la Global.CONNECTION è nulla,
               //significa che sto gestendo la connessione
               //sulla ICartella dall'interno.
               //Sull'AggiungiDocumento la gestirò comunque
               //esterna.....
            	 
               if (en.Global.CONNECTION==null) {
                   wasNull=true;
                   en.setExternalConnection(dbOp.getConn());
               }
              
               //Parte 1) - Aggiornamento dei valori del profilo
               if (profileValue.size()!=0 || cartRiferimenti.size()!=0 || cartRiferimentiToDelete.size()!=0) {
            	   if (bEscludiControlloCompetenze)
            		   en.byPassCompetenzeON();
            	   
            	   AggiornaDocumento ad = new AggiornaDocumento(profileFolder,en);
	               
            	   for(int i=0;i<profileValue.size();i++) {            		
	                    ad.aggiornaDati(((Valori)profileValue.get(i)).key,((Valori)profileValue.get(i)).value);
            	   }
            	  
            	   int size = cartRiferimenti.size();
	               for(int j=0;j<size;j++) {	                 
	                  String idCart=(String)cartRiferimenti.elementAt(j);
	                  String idDoc=(new DocUtil(en)).getIdDocumentoByCr(idCart);
	                  ad.aggiungiRiferimento( idDoc,(String)typeRiferimenti.elementAt(j));
	               }
	               
            	   size = cartRiferimentiToDelete.size();
	               for(int j=0;j<size;j++) {	                 
	                  String idCart=(String)cartRiferimentiToDelete.elementAt(j);
	                  String idDoc=(new DocUtil(en)).getIdDocumentoByCr(idCart);
	                  ad.eliminaRiferimento( idDoc,(String)typeRiferimentiToDelete.elementAt(j));
	               }		    
	               
	               ad.setDontRebuildOrdinamenti(true);
	               
	               ad.salvaDocumento();
               }
            
               //....qui la risetto a null nel caso in cui lo era            
               if (wasNull) {
                  en.resetExternalConnection();                  
               }
              
               en.setDbOp(dbOp);
             }
             catch (Exception e) {
               //....qui la risetto a null nel caso in cui lo era            
               if (wasNull) {
                  en.resetExternalConnection();
                  
               }  
              
               en.setDbOp(dbOp);
               
               throw new Exception("AggiornaDocumento::aggiornaDati\n"+e.getMessage());
             }
             //System.out.println("DBOP--->"+en.getDbOp()); 
            
             //Parte 2-3) Aggiornamento del nome cartella - Verifica della competenza di creazione sulla cartella
             try {         
    
              for(int i=0;i<profileValue.size();i++) 
                   if ((((Valori)profileValue.get(i)).key).equals("NOME"))    
                       updateNameFolder(""+((Valori)profileValue.get(i)).value,dbOp);
               
              try {
                 if (profileValue.size()>0 || cartRiferimenti.size()>0 || cartRiferimentiToDelete.size()>0) {
                    GestioneOrdinamentiCartelle ord =new GestioneOrdinamentiCartelle(en,profileFolder,"D");
                    ord.rebuild(false,false);
                 }
              }
  	          catch (Exception e) {
  	            throw new Exception("Errore in ordinamenti cartella:\n"+e.getMessage());
  	          }
              
  	        
              // Se il vettore inObject non è vuoto verifichiamo le competenze
              if (!inObject.isEmpty())
              {   // Verifica della competenza di inserimento degli oggetti sulla cartella
                 if (!bEscludiControlloCompetenze && !verificaCompetenzaCartella(identifierFolder,Global.ABIL_CREA)) {
                   String sMessaggio ="Non si possiedono le competenze ";
                   sMessaggio+="di inserimento degli oggetti sulla cartella";
                  
                   throw new Exception("ICartella::update - "+sMessaggio);
                   
                 }
              }
             
             //Parte 4) Inserimento degli oggetti nella cartella                            
              for(int i=0;i<inObject.size();i++) 
              {
                String typeobj=""+((Valori)inObject.get(i)).value;
                if (typeobj.equals("D"))//Documento
                   insertDocumentInFolder((String)((Valori)inObject.get(i)).key,dbOp,((Valori)inObject.get(i)).bCheck);
                else
                  if (typeobj.equals("C"))//Cartella
                   insertCartellaInFolder((String)((Valori)inObject.get(i)).key,dbOp);
                   else
                    if (typeobj.equals("Q"))//Query
                     insertQueryInFolder((String)((Valori)inObject.get(i)).key,dbOp);
              }
             }
             catch (Exception e) {
               throw new Exception("Update - Insert Object\n"+e.getMessage());
             }      
             
             try {
               deleteObjectList(dbOp);
             }
             catch (Exception e) {
               throw new Exception("Update - deleteObjectList\n"+e.getMessage());
             }
             
             //Parte 5) Inserimento/Aggiornamento delle ACL
             try {         
               salvaACL("U");
             }
             catch (Exception e) {
               throw new Exception("Update - salvaACL\n"+e.getMessage());
             }
            
             en.disconnectCommit();     
             
             //System.out.println("************************** MANNYSTE UPDATE CARTELLA FINE ************************");
         }
         catch (Exception e) {
        	
           //e.printStackTrace();
           if (en.Global.CONNECTION!=null) try{en.rollbackToSavePoint();}catch (Exception ei) {}
           en.disconnectRollback();   
           throw new Exception("ICartella::update\n"+e.getMessage());
         }
         finally {
            try {en.getDbOp().close();}catch (Exception e) {}
         }
         
         
   }

   /**
    * Metodo che provvede ad eliminare una cartella 
    * in maniera "logica": viene settato lo stato della
    * cartella a 'CA' (Cancellato)         
   */
   public void delete() throws Exception {
	      delete(true);
   }

   /**
    * Metodo che provvede ad eliminare una cartella 
    * in maniera "fisica"    
   */
   public void deletePhisical() throws Exception {
	      delete(false);
   }   

   /**
    * Metodo che provvede ad eliminare una cartella eliminando 
    * ricorsivamente tutti gli elementi in essa contenuti.
    * Prima di procedere all'eliminazione si controllano le competenze
    * di cancellazione sull'oggetto cartella.         
   */   
   /*public void deleteLogical() throws Exception {
	      delete(false);
   }*/

   private void delete(boolean bLogical) throws Exception {                 
         //0) Verifica della competenza di delete della cartella            	              		   
         if (bEscludiControlloCompetenze==false && !verificaCompetenzaCartella(identifierFolder,Global.ABIL_CANC)) {
            String sMessaggio ="Non si possiedono le competenze ";
                   sMessaggio+="di delete sulla cartella";
            throw new Exception("ICartella::delete - "+sMessaggio);
         }
         
         //1) Procedo all'eliminazione attraverso l'apposita function
         //   F_ELIMINA_CARTELLA.
         IDbOperationSQL dbOpSQL = null;
                           
         try 
         {    
              en.connect();
              if (en.Global.CONNECTION!=null) en.createSavePoint();      
              dbOpSQL = en.getDbOp();
              
              String controlla="S";
              if (bEscludiControlloCompetenze) controlla="N";
              
              if (!bLogical)
            	  dbOpSQL.setCallFunc("F_Elimina_Cartella("+identifierFolder+",'"+en.getUser()+"','"+controlla+"')");
              else
            	  dbOpSQL.setCallFunc("F_Elimina_Cartella_LF("+identifierFolder+",'"+en.getUser()+"','S','L')");
              
              dbOpSQL.execute();
              
              if ( dbOpSQL.getCallSql().getInt(1)==0 ) {
            	  if (!bLogical)
            		  throw new Exception("ICartella::delete Errore function F_Elimina_Cartella ");
            	  else
            		  throw new Exception("ICartella::delete Errore function F_ELIMINA_CARTELLA_LOGICA ");
              }
              
              en.disconnectCommit();
         }
         catch (Exception e)
         {	  
        	  if (en.Global.CONNECTION!=null) en.rollbackToSavePoint();    
              en.disconnectRollback();
              throw new Exception("ICartella::delete "+e.getMessage());
         }
   }

   /**
    * Metodo che provvede a "copiare" gli oggetti passati in input
    * dentro la cartella in questione. Copiare un oggetto ha il seguente significato:<BR>
    * Se si tratta di Query o Documento viene inserito l'oggetto in cartella
    * come da funzione <a href="ICartella.html#addInObject(java.lang.String, java.lang.String)">addInObject(String,String)</a><BR>
    * Se si tratta di Cartella viene creata una cartella collegata all'interno
    * del folder in questione.<BR>
    * Il parametro in input deve essere nel formato:<BR> 
    * 		tipoOggetto1identificativoOggetto1@tipoOggetto2identificativoOggetto2@...<BR><BR>
    * Nel caso di documento non è necessario specificare alcun tipo, ma passare solo l'identificativo<BR>
    * Esempio:<BR>
    * C567@D587@Q345@C678<BR>
    * In questo esempio verranno aggiunti il documento 587 e la query 345 alla cartella.
    * Delle cartelle 567 e 678 verranno creati dei collegamenti dentro la cartella attuale.
    * 
    * @param listaID Stringa nel formato tipoOggetto1identificativoOggetto1@tipoOggetto2identificativoOggetto2@...
   */ 
   public void copiaOggetti(String listaID) throws Exception {
	      String[] vLista=listaID.split("@");
		  String id=null,tipoOggetto=null;
          String user= en.getUser();           
     	  boolean copia=true;
          IDbOperationSQL dbOp = null;
          
		  try {
           // Controllo diritti di Creazione sulla cartella di destinazione
		   if (!bEscludiControlloCompetenze) {
	           if (!verificaCompetenzaCartella(identifierFolder,Global.ABIL_CREA)) {
	               String sMessaggio ="Errore: Impossibile copiare gli oggetti nella cartella, non si possiedono i diritti di Creazione sulla Cartella di destinazione!";
	               throw new Exception("ICartella::copiaOggetti - "+sMessaggio);
	            }
		   }
		   
            en.connect();
            dbOp = en.getDbOp();     
            copia=true;
             
            for(int i=0;i<vLista.length;i++)
            {
             //E' una query
             if (vLista[i].indexOf("Q")!=-1) {
                 id=vLista[i].substring(1,vLista[i].length());				   	
                 tipoOggetto="Q";
             }
             else
             if (vLista[i].indexOf("C")!=-1) {
                 id=vLista[i].substring(1,vLista[i].length());				   	
                 tipoOggetto="C";
             }
             else { //E' un documento
            	 if (vLista[i].indexOf("D")!=-1) id=vLista[i].substring(1,vLista[i].length());
                 tipoOggetto="D";
             }
			
			// Controllo non è possibile copiare uno o più documenti nella workspace utente
            if(tipoOggetto.equals("D") &&  (identifierFolder.indexOf("-")!=-1) && (!identifierFolder.equals("-1"))) 
            {
              copia=false;
              throw new Exception("Errore: Uno o più Documenti non si possono copiare nella Workspace!\n");
            }

            // Gestione copia dell'oggetto
               
            // Nel caso di copia di un oggetto di tipo Cartella
            if(tipoOggetto.equals("C"))
            { 
                //CASO 1
                //Occorre inibire l'inserimento di Cartelle nella radice Workspace
                if ((Long.parseLong(identifierFolder)<0)) 
                {  
                   copia=false;
                   throw new Exception("Errore: Non è possibile creare cartelle collegate nella Workspace!\n");
                }
                
               if(copia)
               {
                //CASO 2
                //Creazione del collegamento
                StringBuffer sSql = new StringBuffer("");
                sSql.append("BEGIN GDM_CARTELLE.CREA_COLLEGAMENTO("+id+","+identifierFolder+",'"+user+"'); END; ");
                dbOp.setStatement(sSql.toString());			   
                dbOp.execute();                
               }
            }
            else // Caso di copia di oggetti di tipo Query o Documento
            {
              //Controllo se esiste l'oggetto nella cartella di arrivo
              StringBuffer sSqlVerExistDoc = new StringBuffer("");
              sSqlVerExistDoc.append("select count(*) from links ");			   
              sSqlVerExistDoc.append("where ID_CARTELLA="+identifierFolder+" and ");
              sSqlVerExistDoc.append("      ID_OGGETTO="+id+" and ");
              sSqlVerExistDoc.append("      TIPO_OGGETTO='"+tipoOggetto+"' ");
              dbOp.setStatement(sSqlVerExistDoc.toString());			   
              dbOp.execute();
              ResultSet rst = dbOp.getRstSet();
              rst.next();
              if (rst.getLong(1)>0) copia=false;
            
              
              if(copia)
              {
                // Inserimento dell'oggetto nella cartella di destinazione
                StringBuffer sSql = new StringBuffer("");
                sSql.append("insert into links (ID_LINK, ID_CARTELLA, ID_OGGETTO, TIPO_OGGETTO,DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO) ");
                sSql.append("values (" + dbOp.getNextKeyFromSequence("LINK_SQ") + ",");
                sSql.append(identifierFolder + ",");
                sSql.append(id + ",'"+tipoOggetto+"',");
                sSql.append("sysdate,");
                sSql.append("'"+user+"')");		  			  			   
           
                dbOp.setStatement(sSql.toString());			   
                dbOp.execute();			
                
                this.rebuildOrdCartelle(identifierFolder,id,tipoOggetto);                
              }
           
           }
            
            
	      }// end for		   
             
          en.disconnectCommit();
         }
	     catch (Exception e) 
         {
           en.disconnectRollback();   
           throw new Exception("ICartella::copiaOggetti  \n"+e.getMessage());
         }   
    
   }  

   /**
    * Metodo che provvede a "spostare" gli oggetti passati in input
    * dentro la cartella in questione eliminandoli dalla loro cartella originaria. 
    * E' possibile spostare gli oggetti Cartella, Documento, Query e Cartella collegata::<BR>
    * Per permettere di spostare l'oggetto viene aggiornato il "puntatore"
    * al suo padre con la cartella in questione.<BR>
    * Il parametro in input deve essere nel formato:<BR> 
    * 		tipoOggetto1identificativoOggetto1,idCartellaProvenienza1@tipoOggetto2identificativoOggetto2,idCartellaProvenienza2@...<BR><BR>
    * Nel caso di documento non è necessario specificare alcun tipo, ma passare solo l'identificativo<BR>
    * Esempio:<BR>
    * C567,123@D587,452@Q345,567<BR>
    * In questo esempio verranno spostati il documento 587 con cartella padre 452, la query 345 con cartella padre 567,
    * la cartella 567 con cartella padre 123 e la cartella collegata 678 con cartella padre 1223
    * 
    * @param listaID Stringa nel formato tipoOggetto1identificativoOggetto1,identificativoCartellaPadre1@tipoOggetto2identificativoOggetto2,identificativoCartellaPadre2@...
   */ 
   public void spostaOggetti(String listaID) throws Exception {
	      String[] vLista=listaID.split("@");
		  String user= en.getUser();
          boolean sposta=true;
          IDbOperationSQL dbOp = null;
          String sCartellaProvenienza=null;
          
          //System.out.println("SPSTA COSì: "+listaID);
          
          try {
              // Controllo diritti di Creazione sulla cartella di destinazione
              if (!bEscludiControlloCompetenze && !verificaCompetenzaCartella(identifierFolder,Global.ABIL_CREA)) {
                 String sMessaggio ="Errore: Impossibile spostare gli oggetti nella cartella, non si possiedono i diritti di Creazione sulla Cartella di destinazione!";
                 throw new Exception("ICartella::spostaOggetti - "+sMessaggio);
              }
        	 // System.out.println("SONO QUI CONNECT");
             en.connect();
             dbOp = en.getDbOp();     

        	 for(int i=0;i<vLista.length;i++) 
        	 {
       		    String sOggetto=vLista[i].substring(0,vLista[i].indexOf(","));
  	  		    sCartellaProvenienza=vLista[i].substring(vLista[i].indexOf(",")+1,vLista[i].length());
  	  		    String sTipoOggetto;
  	  		    sposta=true;
  	  		      	  		                 
                // Verifica il tipo Oggetto
                if (sOggetto.indexOf("C")!=-1) {
                   sTipoOggetto="C";
                   sOggetto=sOggetto.substring(1,sOggetto.length());
                }
                else 
                 if (sOggetto.indexOf("X")!=-1) {
                   sTipoOggetto="X";
                   sOggetto=sOggetto.substring(1,sOggetto.length());
                }
                else
                 if (sOggetto.indexOf("Q")!=-1) {
                    sTipoOggetto="Q";
                    sOggetto=sOggetto.substring(1,sOggetto.length());
                 }
                 else
                  if (sOggetto.indexOf("L")!=-1) {
                        sTipoOggetto="L";
                        sOggetto=sOggetto.substring(1,sOggetto.length());
                     }
                  else 
                  {
                	 sTipoOggetto="D";	
                	 if(sOggetto.indexOf("D")!=-1)
                		sOggetto=sOggetto.substring(1,sOggetto.length()); 
                  }
                
                //Controllo non è possibile spostare un documento nella workspace 
                if(sTipoOggetto.equals("D") &&  (identifierFolder.indexOf("-")!=-1) && (!identifierFolder.equals("-1")) ) {
                  sposta=false;
                  throw new Exception("Errore: Uno o più Documenti non si possono spostare nella Area di Lavoro!\n");
                }
       
                // Controllo non è possibile spostare un oggetto C o X su se stesso
                if((sTipoOggetto.equals("C") || (sTipoOggetto.equals("X"))) &&  identifierFolder.equals(sOggetto)) {
                  sposta=false;
                  throw new Exception("Errore: Non è possibile spostare una Cartella su se stessa!\n");
                }
               
                // Gestione spostamento dell'oggetto
               
                // Nel caso di spostamento di un oggetto di tipo Cartella Collegata
                if(sTipoOggetto.equals("X")) 
                {
                 //CASO 1
                 //Occorre inibire l'inserimento di Cartelle nella radice WorkSpace 
                 if((Long.parseLong(identifierFolder)<0)) 
                 {  
                    sposta=false;
                    throw new Exception("Errore: Non è possibile creare cartelle collegate nella Workspace!\n");
                 }
                 
                 //CASO 2
                 if(sposta)
                 {
                  //CASO 2
                  //Creazione del collegamento
                  StringBuffer sSql = new StringBuffer("");
                  sSql.append("BEGIN GDM_CARTELLE.SPOSTA_COLLEGAMENTO("+sOggetto+","+sCartellaProvenienza+","+identifierFolder+",'"+user+"'); END; ");
                  //System.out.println("SPOSTA Cartella X= "+sSql.toString());
                  dbOp.setStatement(sSql.toString());			   
                  dbOp.execute();                
                 }
                  
               }
               else // Caso di spostamento di oggetti di tipo Cartella o Query o Documento o Collegamenti Esterni
               {
                 //Controllo se esiste l'oggetto nella cartella di arrivo
            	 //ammesso che la cartella di arrivo non coincida con quella di partenza
                 StringBuffer sSqlVerExistDoc = new StringBuffer("");
                 sSqlVerExistDoc.append("select count(*) from links ");			   
                 sSqlVerExistDoc.append("where ID_CARTELLA="+identifierFolder+" and ");
                 sSqlVerExistDoc.append("      ID_OGGETTO="+sOggetto+" and ");
                 sSqlVerExistDoc.append("      TIPO_OGGETTO='"+sTipoOggetto+"' and ");
                 sSqlVerExistDoc.append("      "+sCartellaProvenienza+"<>"+identifierFolder+" ");
                  
                 dbOp.setStatement(sSqlVerExistDoc.toString());			   
                 dbOp.execute();
                 ResultSet rst = dbOp.getRstSet();
                 rst.next();
                 boolean bEliminaLink=false;
                 if (rst.getLong(1)>0) { sposta=false; bEliminaLink=true; }
              
                 // Se non esiste
                 if((sposta) && (sCartellaProvenienza!=null) && (!sCartellaProvenienza.equals(identifierFolder)))
                 {
                   bEliminaLink=false;
			  
                   StringBuffer sSql = new StringBuffer("");
                   sSql = new StringBuffer("");
                   sSql.append("Update links set ");
                   sSql.append("ID_CARTELLA=" + identifierFolder + ",");
                   sSql.append("DATA_AGGIORNAMENTO=sysdate,");
                   sSql.append("UTENTE_AGGIORNAMENTO='" +user + "'");
                   sSql.append("where ID_OGGETTO=" + sOggetto + "and ");
                   sSql.append("TIPO_OGGETTO='" + sTipoOggetto + "' and ");
                   sSql.append("ID_CARTELLA=" + sCartellaProvenienza);
   
                   dbOp.setStatement(sSql.toString());			   
                   dbOp.execute();
                   
                   //Aggiorno idDocumentoPadre (se esiste già)
                   if (sTipoOggetto.equals("D")) {
                	   sSql = new StringBuffer("");
                	   sSql.append("Update documenti set ID_DOCUMENTO_PADRE = ");
                	   sSql.append("decode(nvl(ID_DOCUMENTO_PADRE,0),0,ID_DOCUMENTO_PADRE,(select id_documento_profilo from cartelle where id_cartella="+identifierFolder+")) ");
                	   sSql.append("where id_documento="+sOggetto);
                	   
                	   try {
                		   dbOp.setStatement(sSql.toString());			   
                		   dbOp.execute();
                	   }
                	   catch (Exception e) {
                		   throw new Exception("Errore in assegnamento id_documento_padre.\nErrore: "+e.getMessage());
                	   }
                   }                   
                   
                   //Rigenera l'ordinamento valido per Documento o Cartella o Query 
                   if (!sTipoOggetto.equals("L"))
                    this.rebuildOrdCartelle(identifierFolder,sOggetto,sTipoOggetto);                   
                 }
                 
                 //
                 if (bEliminaLink==true) {
                	 StringBuffer sSql = new StringBuffer("");
                     sSql = new StringBuffer("");
                     sSql.append("delete from links ");                     
                     sSql.append("where ID_OGGETTO=" + sOggetto + " and ");
                     sSql.append("TIPO_OGGETTO='" + sTipoOggetto + "' and ");
                     sSql.append("ID_CARTELLA=" + sCartellaProvenienza);
                     
                     dbOp.setStatement(sSql.toString());			   
                     dbOp.execute();                     
                 }
                	 
               }// end else
         
			 }//end for
             
             en.disconnectCommit();
         }
 	     catch (Exception e) 
         {
           en.disconnectRollback();   
           throw new Exception("ICartella::spostaOggetti  \n"+e.getMessage());
         }   
    
   }
   
   /**
    * Rigenerazione degli ordinamenti sull'oggetto contenuto nella Cartella. 
    * 
    * @param idCart ID della Cartella        
    * @param idObj  ID dell'oggetto
    * @param tipoObj  tipo di oggetto C=Cartella, Q=Query, D=Documento
   */  
   private void rebuildOrdCartelle(String idCart,String idObj,String tipoObj) throws Exception
   { 
	   	   try
           {
	   		   //System.out.println("rebuild-->"+idCart+","+idObj+","+tipoObj);
        	   GestioneOrdinamentiCartelle ord =new GestioneOrdinamentiCartelle(en,idCart,idObj,tipoObj);
        	   ord.rebuild(true);
           }
		   catch (Exception e) 
		   {
		    throw new Exception("ICartella::rebuildOrdCartelle \n"+e.getMessage());
		   }   
   }   

   /**
    * Metodo che provvede ad controllare se un elemento 
    * è presente sulla cartella
    * 
    * @param idElement ID dell'Elemento da controllare        
    * @param typeElement C=Cartella, Q=Query, D=Documento
   */    
   public boolean isElementInFolder(String idElement, String typeElement) throws Exception {
	   	  StringBuffer sStm = new StringBuffer("");
	   	  boolean bRet;
	   	  IDbOperationSQL dbOpSQL = null;
	   	
	   	  sStm.append("select 'x' ");
	   	  sStm.append("from links l ");
	   	  sStm.append("where id_oggetto="+idElement);
	   	  sStm.append("  and id_cartella="+identifierFolder);
	   	  sStm.append("  and tipo_oggetto='"+typeElement+"'");
	   	  	   	  
	   	  try {    
            en.connect();
            dbOpSQL = en.getDbOp();                                           
            dbOpSQL.setStatement(sStm.toString());			   
			dbOpSQL.execute();
			ResultSet rs=null;
			rs=dbOpSQL.getRstSet();
                                  
       	 	if (rs.next()) 
       	 		bRet=true;
       	 	else
       	 		bRet=false;
       	 	       	 	 		                    
            en.disconnectCommit();
              
            return bRet;
          }
          catch (Exception e) {
            en.disconnectRollback();
            throw new Exception("ICartella::isElementInFolder "+e.getMessage());
          }	      
   }
   
   /**
    * Metodo che restituisce lo stato di una cartella
    * i valori possibili sono:<BR>
    * 				  	Global.STATO_BOZZA<BR>
	*					Global.STATO_COMPLETO<BR>
	*					Global.STATO_ANNULLATO<BR>
	*					Global.STATO_CANCELLATO<BR>
    * 
   */    
   public String getStato() throws Exception {
	   	  StringBuffer sStm = new StringBuffer("");
	   	  String sRet=null;
	   	  IDbOperationSQL dbOpSQL = null;
	   	
	   	  sStm.append("select nvl(stato,'"+Global.STATO_BOZZA+"') ");
	   	  sStm.append("from cartelle ");
	   	  sStm.append("where id_cartella="+identifierFolder);	   	  
	   	  	   	  
	   	  try {    
            en.connect();
            dbOpSQL = en.getDbOp();                                           
            dbOpSQL.setStatement(sStm.toString());			   
			dbOpSQL.execute();
			ResultSet rs=null;
			rs=dbOpSQL.getRstSet();
                                  
       	 	if (rs.next()) 
       	 		sRet=rs.getString(1);  
       	 	       	 	 		                    
            en.disconnectCommit();
              
            return sRet;
          }
          catch (Exception e) {
            en.disconnectRollback();
            throw new Exception("ICartella::getStato "+e.getMessage());
          }	      
   }   
   
   public void escludiControlloCompetenze(boolean bFlag) {
   		  bEscludiControlloCompetenze=bFlag;
   }   
   
   public boolean testEsclusoControlloCompetenze()  {
          return bEscludiControlloCompetenze;
   }   

   // ***************** MEOTODI PRIVATI ***************** //
  
   /**
    * Metodo che fornisce l'elenco di oggetti 
    * contenuti nella cartella 
   */
   private String getElementInFolder(String tipoObj,boolean bAreaCmNotVisible,boolean b_type,String area,String cm,boolean DVisible) throws Exception {                 
           String elenco="";
           IDbOperationSQL dbOpSQL = null;
           StringBuffer sStm = new StringBuffer();
        
           sStm.append("select decode('"+tipoObj+"','T',decode('"+bAreaCmNotVisible+"','true',l.tipo_oggetto||','||l.id_oggetto||','||d.area||','||tp.nome,l.tipo_oggetto||','||l.id_oggetto),decode('"+b_type+"','true',l.id_oggetto||','||d.area||','||tp.nome,decode('"+DVisible+"','true',l.tipo_oggetto||','||l.id_oggetto,l.id_oggetto))) OGGETTO ");
           sStm.append("from links l  , documenti d, tipi_documento tp ");
           sStm.append("where l.id_cartella = "+identifierFolder+" ");
           sStm.append("and tipo_oggetto='D' and ");
           sStm.append("id_oggetto=d.id_documento and ");
           sStm.append("d.id_tipodoc=tp.id_tipoDoc ");
           if (!bEscludiControlloCompetenze)
        	   sStm.append("and GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI',l.id_oggetto,'L', '"+en.getUser()+"',  F_TRASLA_RUOLO('"+en.getUser()+"','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy'))=1 ");
           sStm.append("and ('"+tipoObj+"'=tipo_oggetto or '"+tipoObj+"'='T') ");
           
           if (area!=null)  sStm.append("and d.area  ='"+area+"' ");
           if (cm!=null)    sStm.append("and tp.nome ='"+cm+"' ");
           
           sStm.append("union ");
           sStm.append("select l.tipo_oggetto||','|| ");
           sStm.append("l.id_oggetto ");
           sStm.append("from links l, cartelle c, documenti d, tipi_documento tp ");
           sStm.append("where l.id_cartella = "+identifierFolder+" ");
           sStm.append("and tipo_oggetto='C' ");
           sStm.append("and c.id_cartella=l.id_oggetto ");
           sStm.append("and c.id_documento_profilo=d.id_documento ");
           sStm.append("and d.id_tipodoc=tp.id_tipoDoc ");
           if (!bEscludiControlloCompetenze)
        	   sStm.append("and GDM_COMPETENZA.GDM_VERIFICA('VIEW_CARTELLA',F_IDVIEW_CARTELLA(l.id_oggetto),'L', '"+en.getUser()+"',  F_TRASLA_RUOLO('"+en.getUser()+"','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy'))=1 ");
           sStm.append("and ('"+tipoObj+"'=tipo_oggetto or '"+tipoObj+"'='T') ");

           if (area!=null)  sStm.append("and d.area  ='"+area+"' ");
           if (cm!=null)    sStm.append("and tp.nome ='"+cm+"' ");           
           
           sStm.append("union ");
           sStm.append("select l.tipo_oggetto||','|| ");
           sStm.append("l.id_oggetto ");
           sStm.append("from links l, query q, documenti d, tipi_documento tp ");
           sStm.append("where l.id_cartella = "+identifierFolder+" ");
           sStm.append("and tipo_oggetto='Q' ");
           sStm.append("and q.id_query=l.id_oggetto ");
           sStm.append("and q.id_documento_profilo=d.id_documento ");
           sStm.append("and d.id_tipodoc=tp.id_tipoDoc "); 
           if (!bEscludiControlloCompetenze)
        	   sStm.append("and GDM_COMPETENZA.GDM_VERIFICA('QUERY',l.id_oggetto,'L', '"+en.getUser()+"',  F_TRASLA_RUOLO('"+en.getUser()+"','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy'))=1 ");
           sStm.append("and ('"+tipoObj+"'=tipo_oggetto or '"+tipoObj+"'='T')");

           if (area!=null)  sStm.append("and d.area  ='"+area+"' ");
           if (cm!=null)    sStm.append("and tp.nome ='"+cm+"' ");
           
           try {    
             en.connect();
             dbOpSQL = en.getDbOp();                                           
             dbOpSQL.setStatement(sStm.toString());			   
			 dbOpSQL.execute();
			 ResultSet rs=null;
			 rs=dbOpSQL.getRstSet();
                                  
       	 	 while ( rs.next() ) {
		           elenco += rs.getString("OGGETTO")+"@";
       	 	 }
             
       	 	 if (!elenco.equals("")) 
       	 	 	elenco = elenco.substring(0,elenco.length()-1);
           
             en.disconnectCommit();
              
             return elenco;
           }
           catch (Exception e) {
             en.disconnectRollback();
             throw new Exception("ICartella::getElementInFolder "+e.getMessage());
           }
   }

   /**
    * Metodo che provvede ad eliminare gli 
    * elementi dellalista dei link.
    * Prima di procedere con l'eliminazione si 
    * controllano le competenze di delete sull'oggetto 
    * cartella.
   */
   private void deleteObjectList(IDbOperationSQL dbOpSQL) throws Exception {                 
           String id=null, tipoOggetto=null;
		              
           // Verifica della competenza di delete della cartella
           //!!!!!!!!!!!LA COMPETENZA DI CANCELLAZIONE DI OGGETTI DALLA CARTELLA NON C'E' ANCORA!!!!!!!
           /*if (!verificaCompetenzaCartella(identifierFolder,Global.ABIL_CANC)) {
                 String sMessaggio ="Non si possiedono le competenze ";
                        sMessaggio+="di delete sulla cartella";
                 throw new Exception("ICartella::delete - "+sMessaggio);
           }*/
                                    
           try {    
             //Eliminazione di ogni oggetto della lista link o Collegamenti
             for(int i=0;i<outObject.size();i++) {
                 tipoOggetto=""+((Valori)outObject.get(i)).value;
              
                 id=""+((Valori)outObject.get(i)).key;
       
                 if(tipoOggetto.equals("X"))
                   eliminaCollegamento(dbOpSQL,id); 
                 else 
                   deleteObject(dbOpSQL,id,tipoOggetto);                           
             }                
           }
           catch (Exception e) {
             throw new Exception("ICartella::deleteObjectList "+e.getMessage());
           }    
   }


   /**
    * Cancellazione del relativo link dato 
    * id dell'oggetto e il tipo dell'oggetto 
    *
   */
   private void deleteObject(IDbOperationSQL dbOp,String idObject,String typeObject) throws Exception {
           //Controllo ID passato
	   	   try {
	   		 Long.parseLong(idObject);
	       }
           catch (Exception e) {
             throw new Exception("ICartella::deleteObject - E' stato passato un idOggetto da cancellare non corretto: valore passato=("+idObject+")\n"+e.getMessage());
           } 
           
	       StringBuffer sStm = new StringBuffer();
     
           sStm.append("DELETE FROM LINKS ");
           sStm.append("WHERE ID_OGGETTO = "+idObject+" AND ");
           sStm.append("ID_CARTELLA = "+identifierFolder+" AND ");
		   sStm.append("TIPO_OGGETTO = '"+typeObject+"'");
          
           dbOp.setStatement(sStm.toString());
           //System.out.println("Delete link= "+sStm.toString());
           try {
  			 dbOp.execute();
           }
           catch (Exception e) {
             throw new Exception("ICartella::deleteObject - Delete Object\n"+e.getMessage());
           }
   }

   public void eliminaCollegamento(String idObject) throws Exception {
	      try {
	    	en.connect();
	    	eliminaCollegamento(en.getDbOp(),idObject);
	    	en.disconnectClose();
	      }
	      catch(Exception e) {
	    	en.disconnectClose();
	    	throw new Exception(e);
	      }
	      	
   }

   public void creaCollegamento(String idObject) throws Exception {
	      try {
	    	en.connect();
	    	creaCollegamento(en.getDbOp(),idObject);
	    	en.disconnectClose();
	      }
	      catch(Exception e) {
	    	en.disconnectClose();
	    	throw new Exception(e);
	      }
   }   
   
   public void creaCollegamentoDesktop(String categoria) throws Exception {
	      try {
	    	en.connect();
	    	String[] seq=null;
	    	if(categoria.indexOf("@")!=-1)
	         seq = categoria.split("@");		
	    	
	    	if(seq!=null)
	    	{
	    		for(int i=0;i<seq.length;i++)
		    	{
		    		creaCollegamentoDesktop(en.getDbOp(),seq[i]);
		    	}
	    	}
	    	
	    	en.disconnectClose();
	      }
	      catch(Exception e) {
	    	en.disconnectClose();
	    	throw new Exception(e);
	      }
   }   
   
   /**
    * Cancellazione del relativo Collegamento 
    * dato id dell'oggetto 
   */
   private void eliminaCollegamento(IDbOperationSQL dbOp,String idObject) throws Exception {
           StringBuffer sStm = new StringBuffer();
         
           // Invocare la function F_ELIMINA_COLLEGAMENTO 
           sStm.append("BEGIN GDM_CARTELLE.ELIMINA_COLLEGAMENTO("+idObject+","+identifierFolder+"); END; ");
           dbOp.setStatement(sStm.toString());

           try {
   		     dbOp.execute();
           }
           catch (Exception e) {
             throw new Exception("ICartella::eliminaCollegamento - Delete Object Collegamento\n"+e.getMessage());
           }
   }
   

   /**
    * Inserimento del relativo Collegamento 
    * dato id dell'oggetto 
   */
   private void creaCollegamento(IDbOperationSQL dbOp,String idObject) throws Exception {
           StringBuffer sStm = new StringBuffer();
                  
           sStm.append("BEGIN GDM_CARTELLE.CREA_COLLEGAMENTO("+idObject+","+identifierFolder+",'"+en.getUser()+"'); END; ");
           dbOp.setStatement(sStm.toString());

           try {
   		     dbOp.execute();
           }
           catch (Exception e) {
             throw new Exception("ICartella::creaCollegamento\n"+e.getMessage());
           }
   }   

   /**
    * Inserimento del relativo Collegamento 
    * dato id dell'oggetto 
   */
   private void creaCollegamentoDesktop(IDbOperationSQL dbOp,String categoria) throws Exception {
           StringBuffer sStm = new StringBuffer();
            
           if(categoria.indexOf("'")!=-1)
        	 categoria= categoria.replaceAll("'","''");  
           
           sStm.append("BEGIN GDM_CARTELLE.CREA_COLLEGAMENTO_DESKTOP('"+categoria+"',"+identifierFolder+",'"+en.getUser()+"'); END; ");
           //System.out.println("---------------------"+sStm.toString());
           
           dbOp.setStatement(sStm.toString());

           try {
   		     dbOp.execute();
           }
           catch (Exception e) {
             throw new Exception("ICartella::creaCollegamento\n"+e.getMessage());
           }
   }   
   
   /**
    * Inserisce il profilo della cartella
   */  
   private void insertProfilo_Folder(IDbOperationSQL dbOp) throws Exception {  
           // 1) Verifico se ho competenza di creazione sulla cartella
           //    superiore dove tento di creare la nuova cartella
	   	   //    Se il padre è stato settato a 0 significa che sto creando
	       //    una nuova workspace. Non devo controllare nessuna competenza
	   	   if (!identifierUpFolder.equals("0"))
	           if (!bEscludiControlloCompetenze && !verificaCompetenzaCartella(identifierUpFolder,Global.ABIL_CREA)) {         
	               String sMessaggio ="Non si possiedono le competenze di creazione ";
	                      sMessaggio+="di oggetti sulla cartella padre";
	              throw new Exception("ICartella::insertProfilo_Folder - "+sMessaggio);
	           }

           // 2) Genero dalla sequence il numero di cartella che poi diventerà
           //    il codice richiesta del documento = <valore_seq>         
           String cr;
           try {            
        	 //Sto generando una workspace, utilizzo la sequence delle
        	 //workspace con numeri negativi
        	 if (identifierUpFolder.equals("0")) {   
        		 identifierFolder="-"+dbOp.getNextKeyFromSequence("WRKSP_SQ");
         		 cr="WRKSP"+identifierFolder;
        	 }
        	 //Sto generando una cartella normale, utilizzo la sequence
        	 //delle cartelle
        	 else {
        		 identifierFolder=""+dbOp.getNextKeyFromSequence("CART_SQ");
        		 cr=identifierFolder;
        	 }        	 
        	          
           }
           catch (Exception e) {            
             throw new Exception("ICartella::insertProfilo_Folder()- Errore generazione sequence cartella\n" + e.getMessage());
           }         

           // 3) Inserisco il profilo a partire dai valori e obbligo ad inserire
           //    il nome della cartella
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
             //sulla ICartella dall'interno.
             //Sull'AggiungiDocumento la gestirò comunque
             //esterna.....
            
             if (en.Global.CONNECTION==null) {
                 wasNull=true;
                 en.setExternalConnection(dbOp.getConn());
             }
             
             if (bEscludiControlloCompetenze)
            	 en.byPassCompetenzeON();
             
             AggiungiDocumento ad = new AggiungiDocumento(cmFolder,areaFolder,en);
           
             boolean bNome=false;
           
             for(int i=0;i<profileValue.size();i++) {
                 if ((((Valori)profileValue.get(i)).key).equals("NOME")) {
                     bNome=true;
                     nameFolder=""+((Valori)profileValue.get(i)).value;      
                 }

                 ad.aggiungiDati(((Valori)profileValue.get(i)).key,
                                ((Valori)profileValue.get(i)).value);                         
             }

             if (nameFolder==null) 
                throw new Exception("ICartella::insertProfilo_Folder - Nome cartella Obbligatorio");                     

             if (!bNome)             
                 ad.aggiungiDati("NOME",nameFolder);

             ad.settaCodiceRichiesta(cr);
             insertCodiceRichiesta(cr,areaFolder,dbOp);
            
             ad.salvaDocumentoBozza();
            
             profileFolder=ad.aDocumento.getIdDocumento();
            
             //....qui la risetto a null nel caso in cui lo era
             if (wasNull) {
                en.resetExternalConnection();            
             }
            
             en.setDbOp(dbOp);            
          }
          catch (Exception e) {
             //....qui la risetto a null nel caso in cui lo era
             if (wasNull) {
                en.resetExternalConnection();            
             }
            
             en.setDbOp(dbOp);
            
             throw new Exception("ICartella::insertProfilo_Folder - AggiungiDocumento\n"+e.getMessage());
          }         
   }  

   /**
    * Inserisce la cartella e la view_cartella
   */
   private void insertFolder(IDbOperationSQL dbOp) throws Exception {
           StringBuffer sStm = new StringBuffer();

           sStm.append("insert into cartelle (ID_CARTELLA, NOME, TIPO,ID_DOCUMENTO_PROFILO");
           sStm.append(",DATA_AGGIORNAMENTO,UTENTE_AGGIORNAMENTO) ");
           sStm.append("values (" + identifierFolder + ",");
           
           if (nameFolder==null) nameFolder="Cartella "+identifierFolder;
           
           if (nameFolder.length()>100)
 		       sStm.append("'"+Global.replaceAll(nameFolder.substring(0,99),"'","''")+"'" + ",");
           else
        	   sStm.append("'"+Global.replaceAll(nameFolder,"'","''")+"'" + ",");
           if (rootFolder.equals("A") || rootFolder==null) rootFolder="S";
           sStm.append("'"+rootFolder+"',"+profileFolder+",");         
		   sStm.append("sysdate,");
   		   sStm.append("'" + en.getUser() + "')");

           dbOp.setStatement(sStm.toString());

           try {
  			 dbOp.execute();
           }
           catch (Exception e) {
             throw new Exception("ICartella::insertFolder - Insert Cartelle\n"+e.getMessage());
           }
         
           try {
             identifierViewFolder=dbOp.getNextKeyFromSequence("VWCART_SQ")+"";
           }
           catch (Exception e) {
             throw new Exception("ICartella::insertFolder - Sequence VWCART_SQ\n"+e.getMessage());
           }

           sStm = new StringBuffer();

		   sStm.append("insert into view_cartella (ID_VIEWCARTELLA,ID_CARTELLA,TIPO_VISUALIZZAZIONE,");
           sStm.append("DATA_AGGIORNAMENTO,UTENTE_AGGIORNAMENTO) ");
		   sStm.append("values (" + identifierViewFolder + ",");
		   sStm.append( identifierFolder + ",");
		   sStm.append("'P',");
	  	   sStm.append("sysdate,");
		   sStm.append("'" + en.getUser() + "')");

           dbOp.setStatement(sStm.toString());

           try {
  			 dbOp.execute();
           }
           catch (Exception e) {
             throw new Exception("ICartella::insertFolder - Insert View Cartelle\n"+e.getMessage());
           }
           
           //Inserisco il link al padre solo se non sto
           //creando una workspace ma una cartella normale
           if (!identifierUpFolder.equals("0") && bCreaLink) {
	           String sLinkSq=null;
	           try {
	             sLinkSq=dbOp.getNextKeyFromSequence("LINK_SQ")+"";
	           }
	           catch (Exception e) {
	             throw new Exception("ICartella::insertFolder - Sequence LINK_SQ\n"+e.getMessage());
	           }
	
	           sStm = new StringBuffer();
	         
	           sStm.append("insert into links (ID_LINK, ID_CARTELLA, ID_OGGETTO, ");
	           sStm.append("TIPO_OGGETTO,DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO)");
		       sStm.append(" values (" + sLinkSq + ",");
	 	       sStm.append(identifierUpFolder + ",");
			   sStm.append(identifierFolder + ",'C',");
			   sStm.append("sysdate,");
			   sStm.append("'" + en.getUser() + "')");
	
	           dbOp.setStatement(sStm.toString());
	
	           try {
	  		     dbOp.execute();
	  		     
	  		     this.rebuildOrdCartelle(identifierUpFolder,identifierFolder,"C");	  		     
	           }
	           catch (Exception e) {
	             throw new Exception("ICartella::insertFolder - Insert links\n"+e.getMessage());
	           }
           }
           
           // Inserimento degli oggetti della lista 
           try {
             for(int i=0;i<inObject.size();i++) {
                String typeobj=""+((Valori)inObject.get(i)).value;
              
                if (typeobj.equals("D"))//Documento
                   insertDocumentInFolder((String)((Valori)inObject.get(i)).key,dbOp,((Valori)inObject.get(i)).bCheck);
                else
                  if (typeobj.equals("C"))//Cartella
                    insertCartellaInFolder((String)((Valori)inObject.get(i)).key,dbOp);
                else
                  if (typeobj.equals("Q"))//Query
                    insertQueryInFolder((String)((Valori)inObject.get(i)).key,dbOp);
             }
           }
           catch (Exception e) {
             throw new Exception("ICartella::insertFolder - Insert insertDocumentInFolder\n"+e.getMessage());
           }
   }
  
   /**
    * Questo metodo provvede ad inserire identifierDocument
    * nella tabella link per collegare il documento in questione
    * on la cartella.
    *              I passi da verificare sono:<BR>
    *                1) Verifica che identifierDocument esista e
    *                   lo user abbia competenze di lettura sul doc.
    *                   e il doc non abbia lo stato CA,RE,PB<BR>
    *                2) Verifica che identifierDocument non sia
    *                   già presente su links<BR>
    *                3) Inserimento su Links
    *
   */
   private void insertDocumentInFolder(String identifierDocument, IDbOperationSQL dbOp, boolean bAutomatic) throws Exception {     
           //Verifica passo 1 - 2
           try {
             StringBuffer sStm = new StringBuffer();
      
             sStm.append("SELECT 'x' ");
             sStm.append("FROM DOCUMENTI ");
             sStm.append("WHERE ");
             if (!bEscludiControlloCompetenze) 
            	 sStm.append("GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI',DOCUMENTI.ID_DOCUMENTO, 'L', '"+en.getUser()+"',F_TRASLA_RUOLO('"+en.getUser()+"','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy'))= 1 AND ");
             sStm.append("DOCUMENTI.ID_DOCUMENTO = "+identifierDocument+" AND ");
             sStm.append("DOCUMENTI.STATO_DOCUMENTO NOT IN ('CA','RE','PB') ");
             sStm.append("and not exists (select 'x' from links where id_oggetto=DOCUMENTI.ID_DOCUMENTO and id_Cartella="+identifierFolder+" and tipo_oggetto='D')");           
           
             dbOp.setStatement(sStm.toString());
             dbOp.execute();  

             ResultSet rst = dbOp.getRstSet();

             //Se non passo la verifica non posso
             //effettuare l'operazione
             if ( !rst.next() ) return;          

           }
           catch (Exception e) {           
             throw new Exception("ICartella::insertDocumentInFolder - Verifica del documento\n" + e.getMessage());           
           }

           //Inserisco il link al documento
           try {          
             StringBuffer sStm = new StringBuffer();

             sStm.append("insert into links ");
 			 sStm.append("(ID_LINK, ID_CARTELLA, ID_OGGETTO, TIPO_OGGETTO,DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO ");
 			 if (bAutomatic) sStm.append(",AUTOMATICO");
			 sStm.append(") values (" + dbOp.getNextKeyFromSequence("LINK_SQ") + ",");
			 sStm.append(identifierFolder + ",");
			 sStm.append(identifierDocument + ",'D',");
			 sStm.append("sysdate,");
			 sStm.append("'" + en.getUser() + "' ");
			 if (bAutomatic) sStm.append(",'Y'");
			 sStm.append(")");

             dbOp.setStatement(sStm.toString());
             dbOp.execute();
           
             this.rebuildOrdCartelle(identifierFolder,identifierDocument,"D");             
           }
           catch (Exception e) {           
             throw new Exception("ICartella::insertDocumentInFolder - Insert in Links\n" + e.getMessage());           
           }         
   }

   /**
    * Questo metodo provvede ad inserire identifierCartella
    * nella tabella link per collegare la sotto-cartella in questione
    * con la cartella padre.
    *              I passi da verificare sono:<BR>
    *                1) Verifica che identifierCartella esista e
    *                   lo user abbia competenze di lettura sulla cartella.<BR>
    *                2) Verifica che identifierCartella non sia
    *                   già presente su links<BR>
    *                3) Inserimento su Links
   */
   private void insertCartellaInFolder(String identifierCartella, IDbOperationSQL dbOp) throws Exception {     
           //Verifica passo 1 - 2
           try {
             StringBuffer sStm = new StringBuffer();
      
             sStm.append("SELECT 'x' ");
             sStm.append("FROM CARTELLE , VIEW_CARTELLA ");
             sStm.append("WHERE ");             
             if (!bEscludiControlloCompetenze) 
            	 sStm.append("GDM_COMPETENZA.GDM_VERIFICA('VIEW_CARTELLA',VIEW_CARTELLA.ID_VIEWCARTELLA, 'L', '"+en.getUser()+"',F_TRASLA_RUOLO('"+en.getUser()+"','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy'))= 1 AND ");
             sStm.append("VIEW_CARTELLA.ID_CARTELLA = CARTELLE.ID_CARTELLA AND ");
             sStm.append("CARTELLE.ID_CARTELLA = "+identifierCartella);
             sStm.append(" and not exists (select 'x' from links where id_oggetto=CARTELLE.ID_CARTELLA and id_Cartella="+identifierFolder+" and tipo_oggetto='C')");           
        
             dbOp.setStatement(sStm.toString());
             dbOp.execute();  

             ResultSet rst = dbOp.getRstSet();

             //Se non passo la verifica non posso
             //effettuare l'operazione
             if ( !rst.next() ) return;          

           }
           catch (Exception e) {           
             throw new Exception("ICartella::insertCartellaInFolder - Verifica del documento\n" + e.getMessage());           
           }

           //Inserisco il link alla cartella
           try {          
             StringBuffer sStm = new StringBuffer();

             sStm.append("insert into links ");
 			 sStm.append("(ID_LINK, ID_CARTELLA, ID_OGGETTO, TIPO_OGGETTO,DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO)");
			 sStm.append(" values (" + dbOp.getNextKeyFromSequence("LINK_SQ") + ",");
			 sStm.append(identifierFolder + ",");
			 sStm.append(identifierCartella + ",'C',");
			 sStm.append("sysdate,");
			 sStm.append("'" + en.getUser() + "')");

             dbOp.setStatement(sStm.toString());
             dbOp.execute();
           
             //Rigenerazione dell'Ordinamento in Cartella
             this.rebuildOrdCartelle(identifierFolder,identifierCartella,"C");
             
           }
           catch (Exception e) {           
             throw new Exception("ICartella::insertCartellaInFolder - Insert in Links\n" + e.getMessage());           
           }         
   }

   /**
    * Questo metodo provvede ad inserire identifierQuery
    * nella tabella link per collegare la query in questione
    * con la cartella.
    *              I passi da verificare sono:<BR>
    *                1) Verifica che identifierQuery esista e
    *                   lo user abbia competenze di lettura.<BR>
    *                2) Verifica che identifierQuery non sia
    *                   già presente su links<BR>
    *                3) Inserimento su Links
   */
   private void insertQueryInFolder(String identifierQuery, IDbOperationSQL dbOp) throws Exception {     
           //Verifica passo 1 - 2
           try {
           
             StringBuffer sStm = new StringBuffer();
             sStm.append("SELECT 'x' ");
             sStm.append("FROM QUERY ");
             sStm.append("WHERE ");
             if (!bEscludiControlloCompetenze) 
            	 sStm.append("GDM_COMPETENZA.GDM_VERIFICA('QUERY',QUERY.ID_QUERY, 'L', '"+en.getUser()+"',F_TRASLA_RUOLO('"+en.getUser()+"','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy'))= 1 AND ");
             sStm.append("QUERY.ID_QUERY = "+identifierQuery);
             sStm.append(" and not exists (select 'x' from links where id_oggetto= QUERY.ID_QUERY  and id_Cartella="+identifierFolder+" and tipo_oggetto='Q')");           
      
             dbOp.setStatement(sStm.toString());
             dbOp.execute();  

             ResultSet rst = dbOp.getRstSet();

             //Se non passo la verifica non posso
             //effettuare l'operazione
             if ( !rst.next() ) return;          
           }
           catch (Exception e) {           
             throw new Exception("ICartella::insertQueryInFolder - Verifica del documento\n" + e.getMessage());           
           }

           //Inserisco il link al documento
           try {          
             StringBuffer sStm = new StringBuffer();

             sStm.append("insert into links ");
 			 sStm.append("(ID_LINK, ID_CARTELLA, ID_OGGETTO, TIPO_OGGETTO,DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO)");
			 sStm.append(" values (" + dbOp.getNextKeyFromSequence("LINK_SQ") + ",");
			 sStm.append(identifierFolder + ",");
			 sStm.append(identifierQuery + ",'Q',");
			 sStm.append("sysdate,");
			 sStm.append("'" + en.getUser() + "')");

             dbOp.setStatement(sStm.toString());
             dbOp.execute();
             
             //Rigenerazione dell'Ordinamento in Cartella
             this.rebuildOrdCartelle(identifierFolder,identifierQuery,"Q");             
           
          }
          catch (Exception e) {           
            throw new Exception("ICartella::insertQueryInFolder - Insert in Links\n" + e.getMessage());           
          }         
   }

   /**
    * Aggiorna il nome della cartella
    * nella tabella cartelle
   */  
   private void updateNameFolder(String name,IDbOperationSQL dbOp)  throws Exception {         
           try {          
             StringBuffer sStm = new StringBuffer();

             sStm.append("UPDATE CARTELLE ");
 			 if (name.length()>100)
 		       sStm.append("SET NOME='" + Global.replaceAll(name,"'","''").substring(0,99) + "',");
             else        	   
 			   sStm.append("SET NOME='" + Global.replaceAll(name,"'","''") + "',");
			 sStm.append("DATA_AGGIORNAMENTO=sysdate,");
             sStm.append("UTENTE_AGGIORNAMENTO='"+en.getUser()+ "'");
			 sStm.append("where id_cartella=" + identifierFolder);

             dbOp.setStatement(sStm.toString());
             dbOp.execute();
           
           }
           catch (Exception e) {           
             throw new Exception("ICartella::updateNameFolder\n" + e.getMessage());           
           }
   }

   /**
    * Assegna le competenze alla cartella ed al profilo-cartella
   */
   private void assegnaCompetenzeCartella(IDbOperationSQL dbOp) throws Exception {         
           try {                   	   
        	   
             //Se non sono sulla cartella utente 
             //devo ereditare le competenze dal tipo documento
             if (!identifierUpFolder.equals("-2"))                
                (new GDM_Competenze(en)).allineaCompetenzaCartQueryDoc(identifierViewFolder,profileFolder,en.getUser(),"C");                
           
             //SONO SULLA WRKSP UTENTE E QUINDI ELIMINO QUELLI DEL DOC CHE LA
             //AggiungiDocumento HA MESSO DI DEFAULT E FORNISCO LE SEGUENTI STANDARD:
             //DOCUMENTO -> LUDM    VIEW_CARTELLA -> LUCDM		 
             else {               
               StringBuffer sStm = new StringBuffer();
               
               try {              	             	 
                 sStm.append("DELETE FROM  SI4_COMPETENZE ");
                 sStm.append("WHERE OGGETTO='"+profileFolder+"' ");
                 sStm.append("AND id_abilitazione IN ");
                 sStm.append(" (SELECT id_abilitazione FROM SI4_ABILITAZIONI,SI4_TIPI_OGGETTO ");
                 sStm.append(" WHERE SI4_TIPI_OGGETTO.TIPO_OGGETTO='DOCUMENTI' ");
                 sStm.append(" AND SI4_TIPI_OGGETTO.ID_TIPO_OGGETTO=SI4_ABILITAZIONI.ID_TIPO_OGGETTO)");
                     
                 dbOp.setStatement(sStm.toString());
                 
                 elpsTime.start("assegnaCompetenzeCartella","Elimino vecchie competenze");
                 dbOp.execute();
                 elpsTime.stop();
               }
               catch (Exception e) {
                 throw new Exception("ICartella::assegnaCompetenzeCartella - Delete SI4_COMPETENZE\n"+e.getMessage());
               }
            
               try {											
                 UtenteAbilitazione env = new UtenteAbilitazione( en.getUser(), en.getPwd(),null,null);								
                 Abilitazioni ab = new Abilitazioni("DOCUMENTI",profileFolder,"L;U;D;M;");
                 elpsTime.start("assegnaCompetenzeCartella","Assegno L-U-D a Documento");
                 (new GDM_Competenze(en)).assegnaCompentenzaMultipla(env,ab);	
                 elpsTime.stop();
                                   
                 Abilitazioni ab2;
                 ab2 = new Abilitazioni("VIEW_CARTELLA",identifierViewFolder,"L;U;C;D;M;");	
                 elpsTime.start("assegnaCompetenzeCartella","Assegno L-U-C-D a VIEW-CARTELLA");                 
                 (new GDM_Competenze(en)).assegnaCompentenzaMultipla(env,ab2);				
                 elpsTime.stop();
               }
               catch (Exception e) {
                 throw new Exception("ICartella::assegnaCompetenzeCartella - Errore assegnamento competenze Documento-ViewCartella\n"+e.getMessage());
               }
             
             }
           }
           catch (Exception e) {
             throw new Exception("ICartella::assegnaCompetenzeCartella\n"+e.getMessage());
           }         
   }
  
   /**
    * Inserisce la competenze ACL sulla ICartella
   */
   private void salvaACL(String tipoSalvataggio) throws Exception {
           //Ciclo sulle ACL
           int size = ACLuser.size();
          
           if (size==0) return;
          
           //Se sono in modifica del documento, prima di modificare le competenze
           //controllo se ho la comp. di manage
           if (tipoSalvataggio.equals("U") && Long.parseLong(identifierFolder)>0) {
              UtenteAbilitazione env = new UtenteAbilitazione( en.getUser(), en.getPwd(),null,null);								
              Abilitazioni ab = new Abilitazioni("VIEW_CARTELLA",identifierViewFolder,Global.ABIL_GEST);				
              
              if (!this.bEscludiControlloCompetenze) {
	              if ((new GDM_Competenze(en)).verifica_GDM_Compentenza(env,ab)==0) {
	                 throw new Exception("ICartella::salvaACL() Impossibile aggiornare le competenze, non si possiede la competenza per modificarle");
	              }
              }
           }
          
           for(int i=0;i<size;i++) {  
              String user,key, sComp="", sCompDoc="", sCompNega="", sCompDocNega="", sVerso="";
                
              user=(String)ACLuser.elementAt(i);
              key=(String)ACLtype.elementAt(i);
              sVerso=(String)ACLversus.elementAt(i);
                    
             
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
              else {
            	 if (sVerso.equals("S")) {
            		 sComp=key;
                     if (!key.equals("C")) sCompDoc=key;
                     sCompNega="";
                     sCompDocNega=""; 
            	 }
            	 else if (sVerso.equals("N")) {
            		 sComp="";
                     sCompDoc="";
                     sCompNega=key;
                     if (!key.equals("C")) sCompDocNega=key; 
            	 }
              }
                        
              try {
                UtenteAbilitazione env = new UtenteAbilitazione( user, user,null,null);								

                if (!sCompDoc.equals("")) {                
                   Abilitazioni ab = new Abilitazioni("DOCUMENTI",profileFolder,sCompDoc);				
                   (new GDM_Competenze(en)).assegnaCompentenza(env,ab);	
                }
               
                if (!sComp.equals("")) {
                   Abilitazioni ab = new Abilitazioni("VIEW_CARTELLA",identifierViewFolder,sComp);							
                   (new GDM_Competenze(en)).assegnaCompentenza(env,ab);		
                }
               
                if (!sCompDocNega.equals("")) {                
                   Abilitazioni ab = new Abilitazioni("DOCUMENTI",profileFolder,sCompDocNega);				
                   ab.setAccesso("N");
                   (new GDM_Competenze(en)).assegnaCompentenza(env,ab);	
                }

                if (!sCompNega.equals("")) {                
                   Abilitazioni ab = new Abilitazioni("VIEW_CARTELLA",identifierViewFolder,sCompNega);				
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
    * Verifica la competenza "tipoCompetenza" sulla cartella idCartella
   */
   private boolean verificaCompetenzaCartella(String idCartella,String tipoCompetenza) throws Exception {
           //Non controllo la competenza sulle cartelle WorkSpace
	   	   //ELIMINATO IL 18/12/2006
           //if (Long.parseLong(idCartella)<0) 
           //   return true;
   
           try {
             String idw = (new DocUtil(en)).getIdViewCartellaByIdCartella(idCartella);

             Abilitazioni abilitazione = new Abilitazioni(Global.ABIL_CARTELLA, idw , tipoCompetenza); 
             UtenteAbilitazione ua = new UtenteAbilitazione(en.getUser(), en.getGruppo(), en.getRuolo(), en.getPwd(),  en.getUser(), en);
             if ((new GDM_Competenze(en)).verifica_GDM_Compentenza(ua,abilitazione)  == 1 ) return true;

             return false;
           }
           catch (Exception e) {        
             throw new Exception("ICartella::verificaCompetenzaCartella(@,@)\n" + e.getMessage());
           }                  
   }
    
   /**
    * Restituisce il nome della cartella corrente   
   */
   private void getNomeCartella() throws Exception {          
           IDbOperationSQL dbOp = null;         
          
           if (identifierFolder==null)
              throw new Exception("ICartella::getNomeCartella - "+
                                  "Impossibile recuperare nameFolder se idFolder è nullo");
                                 
              try {
                StringBuffer sStm = new StringBuffer();
          
                en.connect();
                dbOp = en.getDbOp();
                
                //Lo prendo dalla cartella
                if (cmFolder==null) {
	                sStm.append("select nome ");
	                sStm.append("from cartelle ");
	                sStm.append("where id_cartella= "+identifierFolder);
                }
                //Lo prendo dal profilo...la cartella la sto creando da li
	            else {
	            	sStm.append("select F_VALORE_CAMPO("+profileFolder+",'NOME') ");
	                sStm.append("from dual ");	                
	            }

                dbOp.setStatement(sStm.toString());
                dbOp.execute();

                ResultSet rst = dbOp.getRstSet();

                if ( rst.next() ) 
                   nameFolder = rst.getString(1);               
                else {
                   throw new Exception("ICartella::getNomeCartella - "+
                                       "Errore nel recupero del nameFolder da idFolder");     
                }
               
                en.disconnectClose();
               
              }
              catch (Exception e) {               
                en.disconnectClose();
                throw new Exception("ICartella::getNomeCartella()\n" + e.getMessage());
              }               
   }

  
   /**
    * Restituisce l'id della cartella padre
    * della cartella corrente
   */
   private void identifierUpFolderFromIdentifierFolder() throws Exception {          
           IDbOperationSQL dbOp = null;         

           if (identifierFolder==null)
              throw new Exception("ICartella::identifierUpFolderFromIdentifierFolder - "+
                                 "Impossibile recuperare upFolder se idFolder è nullo");

           //Cartelle WorkSpace non hanno padre
           if (Long.parseLong(identifierFolder)<0) {
              identifierUpFolder=null;
              return;
           }
                                 
           try {
             StringBuffer sStm = new StringBuffer();

             en.connect();
                 
             dbOp = en.getDbOp();

             sStm.append("select id_Cartella ");
             sStm.append("from links ");
             sStm.append("where id_oggetto= "+identifierFolder);
             sStm.append("  and tipo_oggetto='C'");

             dbOp.setStatement(sStm.toString());

             dbOp.execute();  

             ResultSet rst = dbOp.getRstSet();

             if ( rst.next() ) 
                identifierUpFolder = rst.getString(1);               
             else {                  	
                throw new Exception("ICartella::identifierUpFolderFromIdentifierFolder - "+
                                    "Errore nel recupero dell'upFolder da idFolder");     
             }
             
             en.disconnectClose();
             
           }
           catch (Exception e) {    
        	 en.disconnectClose();
             throw new Exception("ICartella::identifierUpFolderFromIdentifierFolder()\n" + e.getMessage());
           }               
   }

   /**
    * Questo metodo provvede alla verifica del path passato
    * utilizzando il costruttore "in insert"
    * Prima di uscire setta l'idUpFolder
    * Es.:  root=User, Path: Personale\Giuridico
    *       restituisce
    *       errore se Giuridico non sta sotto Personale o non esiste 
    *       oppure il path è mal formato.
    * 
    * @param bCreazione True: siamo in creazione, il path serve per trovare
    *                   il padre della cartella che andremo a creare
    * 					False: siamo in modifica, il path serve per trovare
    * 					la cartella da modificare
   */
   private void verifyFolderPath(boolean bCreazione) throws Exception {
           String idRoot;
           String idCartella;
           String sPathRimasto;
           String msg;
           IDbOperationSQL dbOp = null;
           
           //Se non specifico alcuna wrkspace di partenza,
           //significa che la cartella che voglio creare
           //è una workspace
           if (rootFolder.equals("")) {
        	   
        	   if (!bCreazione)
        		   throw new Exception("ICartella::verifyFolderPath()\n" + 
                                       "Specificare la workspace dove cercare cartella");
        	   
        	   rootFolder="A"; //Altra wrksp
        	   idRoot="0";
        	   identifierUpFolder="0";
        	   return;
           }
           
           if (pathFolder==null || pathFolder.equals("")) {
        	   if (bCreazione)
        		   msg="creare";
        	   else
        		   msg="cercare";
        	   
               throw new Exception("ICartella::verifyFolderPath()\n" + 
                                 "Specificare path dove "+msg+" la nuova cartella");
           }

           idRoot=convertWrkSpNameToId();
           
           if (idRoot==null) {
        	   if (bCreazione)
        		   msg="creare";
        	   else
        		   msg="cercare";
        	   
        	   throw new Exception("ICartella::verifyFolderPath()\n" + 
                                   "Workspace dove "+msg+" la nuova cartella inesistente!");
           }
           
           //Caso radice
           if (pathFolder.equals("\\")) {
        	  
        	  if (!bCreazione)
        		   throw new Exception("ICartella::verifyFolderPath()\n" + 
                                       "Specificare un path corretto");        	   
        	   
              identifierUpFolder=idRoot;
              return;
           }

           //Effettuo il ciclo per trovare il percorso corretto
           idCartella = idRoot;
           sPathRimasto = pathFolder;
          
           while (true) {
             try {
               StringBuffer sStm = new StringBuffer();
               String sOggetto;

               if (sPathRimasto.equals("")) {       
            	  
            	  if (bCreazione)
            		  identifierUpFolder=idCartella;
            	  else
            		  identifierFolder=idCartella;
            	  
                  return;
               }
               else {
            	  if (sPathRimasto.indexOf("\\")!=-1) {
	                  sOggetto=sPathRimasto.substring(0,sPathRimasto.indexOf("\\"));
	                  sPathRimasto=sPathRimasto.substring(sPathRimasto.indexOf("\\")+1,sPathRimasto.length());
            	  }
	              else {
	            	  sOggetto=sPathRimasto;
	                  sPathRimasto="";
	              }
               }
               
               en.connect();
               dbOp = en.getDbOp();

               sStm.append("select id_oggetto ");
               sStm.append("from links,cartelle ");
               sStm.append("where cartelle.nome= '"+Global.replaceAll(sOggetto,"'","''")+"' ");
               sStm.append("and cartelle.id_cartella= links.id_oggetto ");
               sStm.append("and links.id_cartella= "+idCartella+" ");            
               sStm.append("and tipo_oggetto= 'C' and nvl(cartelle.stato,'BO')<>'CA'");

               dbOp.setStatement(sStm.toString());
               dbOp.execute();  

               ResultSet rst = dbOp.getRstSet();

               if ( rst.next() ) 
                  idCartella = rst.getString(1);               
               else {
                  String sMessaggio;

                  sMessaggio  = "Attenzione! il path "+ pathFolder +" è inesistente o malformato.\n";
                  sMessaggio += "Non esiste la cartella "+ sOggetto;                  
                  throw new Exception("ICartella::verifyFolderPath()\n" + sMessaggio);
               }
               
               en.disconnectClose();
             }
             catch (Exception e) {               
               en.disconnectClose();
               throw new Exception("ICartella::verifyFolderPath()\n" + e.getMessage());
             }     
         }
   }

   /** 
    * Questo metodo provvede a recuperare il path
    * della cartella a partire dall'id della cartella
   */  
   private void pathFolderFromIdentifier() throws Exception {  
           IDbOperationSQL dbOp = null;         
          
           StringBuffer sStm = new StringBuffer();

           try {         
             en.connect();
             dbOp = en.getDbOp();

             sStm.append("select F_Path_Folder('"+identifierFolder+"','','"+en.getUser()+"') from dual");

             dbOp.setStatement(sStm.toString());
             dbOp.execute();  

             ResultSet rst = dbOp.getRstSet();
               
             rst.next();
              
             pathFolder=rst.getString(1)+"\\";
             
             en.disconnectClose();
           }
           catch (Exception e) {               
             en.disconnectClose();
             throw new Exception("ICartella::pathFolderFromIdentifier()\n" + e.getMessage());
           }                    
   }
  
   private void viewCartFromidCart() throws Exception {       
           IDbOperationSQL dbOp = null;         
          
           StringBuffer sStm = new StringBuffer();

           try {         
             en.connect();
             dbOp = en.getDbOp();

             sStm.append("select F_IDVIEW_CARTELLA("+identifierFolder+") from dual");

             dbOp.setStatement(sStm.toString());
             dbOp.execute();

             ResultSet rst = dbOp.getRstSet();
               
             rst.next();
               
             identifierViewFolder=rst.getString(1);
             
             en.disconnectClose();
           }
           catch (Exception e) {               
             en.disconnectClose();
             throw new Exception("ICartella::viewCartFromidCart()\n" + e.getMessage());
           }             
   }
  
   /** 
    * Questo metodo provvede a recuperare il path
    * della cartella a partire dall'id della cartella padre
   */  
   private void pathFolderFromidentifierUpFolder() throws Exception {  
           IDbOperationSQL dbOp = null;         
          
           StringBuffer sStm = new StringBuffer();

           try {         
             en.connect();
             dbOp = en.getDbOp();

             sStm.append("select F_Path_Folder('"+identifierUpFolder+"','','"+en.getUser()+"') from dual");
             dbOp.setStatement(sStm.toString());
             dbOp.execute();  

             ResultSet rst = dbOp.getRstSet();
               
             rst.next();
               
             pathFolder=rst.getString(1)+"\\";
       
             en.disconnectClose();
           }
           catch (Exception e) {               
             en.disconnectClose();
             throw new Exception("ICartella::pathFolderFromidentifierUpFolder()\n" + e.getMessage());
           }                    
   }

   /**
    * Questo metodo provvede ad inserire sulla tabella
    * RICHIESTE il codice richiesta passato.
    * Serve per l'inserimento del codice richiesta
    * del documento profilo cartella ed è lo stesso meccanismo
    * che utilizza la servlet modulistica
   */
   private void insertCodiceRichiesta(String cr,String area,IDbOperationSQL dbOp) throws Exception {
           try {
             StringBuffer sStm = new StringBuffer();

             sStm.append("insert into richieste (CODICE_RICHIESTA, AREA,DATA_INSERIMENTO) ");
             sStm.append("values ('"+cr+"','"+area+"',sysdate)");

             dbOp.setStatement(sStm.toString());
             dbOp.execute();
           }
           catch (Exception e) {
             throw new Exception("ICartella::insertCodiceRichiesta("+cr+")\n" + e.getMessage());           
           }
   }
  
   /**
    * Inizializzazione delle variabili private
    * Metodo richiamato dai costruttori
   */
   private void inizializza(Environment newEn) throws Exception {

           en                      = newEn; 
           inObject                = new Vector();
           outObject               = new Vector();
           profileValue            = new Vector();
           ACLuser                 = new Vector();
           ACLversus			   = new Vector();
           ACLtype                 = new Vector();  
           cartRiferimentiToDelete = new Vector();
           typeRiferimentiToDelete = new Vector();
           cartRiferimenti         = new Vector();
           typeRiferimenti         = new Vector();
           
           elpsTime = new ElapsedTime("ICartella",newEn);
           
           elpsTime.start("Inizializza","Inizio Inizializza");
           
           try {
           
	           if (pathFolder!=null) {
	        	   //Sono in creazione
	        	   if (areaFolder!=null) {
		              try {
		                identifierUpFolder=Long.parseLong(pathFolder)+"";
		                pathFolderFromidentifierUpFolder();        
		              }
		              catch (Exception e) {
		                pathFolder=pathFolder+"\\";
		                verifyFolderPath(true);          
		              }
	        	  }
	        	  //Sono in Modifica
	        	  else {
	        		  verifyFolderPath(false);
	        		  retrieveFolderInfoById();
	        	  }
	           }
	           else {
	              //Vengo dal costruttore che ha passato l'id
	              if (identifierFolder!=null) {              
	                   retrieveFolderInfoById();
	              }
	              //Vengo dal costruttore che ha passato cm, cr, area del profilo   
	              else if (cmFolder!=null) {
	                 profileFolder=(new DocUtil(en)).getIdDocumentoByAreaCmCr(areaFolder, cmFolder, crFolder);                                  
	                                  
	                 if (profileFolder==null || profileFolder.equals("")) {
	                	 throw new Exception("ICartella::inizializza. Impossibile trovare il profilo per area="+areaFolder+",cm="+cmFolder+",cr="+crFolder);
	                 }
	                 
	                 identifierFolder=crFolder;
	                 
	                 //Vengo dal costruttore che ha passato cm, cr, area e basta 
	                 //Sono quindi in retrieve
	                 if (identifierUpFolder==null) 
	                    retrieveFolderInfoById();
	                 //Vengo dal costruttore che ha passato cm, cr, area e idUpFolder 
	                 //Sono quindi in insert conoscendo il profilo
	                 else {               
	                    en.connect();
	            
	                    //Mi recupero il nome della cartella dal profilo esistente
	                    AccediDocumento ad = new AccediDocumento(profileFolder,en);
	                    
	                    ad.accediDocumentoValori();
	                    
	                    nameFolder=ad.leggiValoreCampo("NOME");
	                    
	                    String idRoot=convertWrkSpNameToId();
	                    
	                    //identifierUpFolder DEVE ESSERE LA WORKSPACE
	                    if (identifierUpFolder.equals("USALAWRKSP")) 
	                    	identifierUpFolder=idRoot;                   
	                    
	                    en.disconnectClose();
	                 }                 
	            }               
	          }
           }
           catch (Exception eExtern) {
        	   try {en.disconnectClose();} catch (Exception eIntern) {}
        	   throw new Exception(eExtern);
           }
           
           elpsTime.stop(); 
   }
  
   private void retrieveFolderInfoById() throws Exception {
           try {
             //IdProfilo in Cartella
             if (profileFolder==null) {
            	 if (Long.parseLong(identifierFolder)<0)
            		 profileFolder = (new DocUtil(en)).getIdDocumentoByCr("WRKSP"+identifierFolder);
            	 else
            		 profileFolder = (new DocUtil(en)).getIdDocumentoByCr(identifierFolder);
             }
  
             //Path della Cartella
             pathFolderFromIdentifier();
             
             rootName=getWrkSp();
  
             //Root della Cartella
             //Path della Cartella senza root
             rootFolder=pathFolder.substring(0,pathFolder.indexOf("\\"));
             pathFolder=pathFolder.substring(pathFolder.indexOf("\\")+1,pathFolder.length());
  
           	 identifierUpFolderFromIdentifierFolder();
  
             //Nome Cartella
             getNomeCartella();
            
             //idViewCartella
             viewCartFromidCart();

           }
           catch (Exception e) {
             throw new Exception("ICartella::retrieveFolderInfoById - "+e.getMessage());
           }
   }
   
   /**
    * Restituisce id della cartella Workspace se esiste 
    * una Workspace con nome "name", null altrimenti
   */
   private String existsWorkSpaceFolder(String name) throws Exception {          
           IDbOperationSQL dbOp = null;         
           String id;
           
           try {
             StringBuffer sStm = new StringBuffer();
          
             en.connect();
             dbOp = en.getDbOp();

             sStm.append("select id_cartella ");
             sStm.append("from cartelle ");
             sStm.append("where nome='"+Global.replaceAll(name,"'","''")+"'");
             sStm.append("      and id_cartella<0");

             dbOp.setStatement(sStm.toString());
             dbOp.execute();
             
             ResultSet rst = dbOp.getRstSet();

             if ( rst.next() ) 
                 id = rst.getString(1);               
             else {
                 id = null;     
             }
               
             en.disconnectClose();
             
             return id;
               
           }
           catch (Exception e) {               
             en.disconnectClose();
             throw new Exception("ICartella::existsNameFolder(String)\n" + e.getMessage());
           }               
   }
   
   private String convertWrkSpNameToId() throws Exception {
	       String idRoot;
           
	       if (rootFolder.equals(Global.ROOT_USER_FOLDER))
              idRoot="-2";
           else if (rootFolder.equals(Global.ROOT_SYSTEM_FOLDER))
              idRoot="-1";
           else {
        	  idRoot=existsWorkSpaceFolder(rootFolder);
        	  rootFolder="A";
           }
	       
	       return idRoot;
   }
   
   //Recupera la wrksp della cartella
   private String getWrkSp() throws Exception {
           IDbOperationSQL dbOp = null;         
           String sWrksp;
           
           try {
             StringBuffer sStm = new StringBuffer();
          
             en.connect();
             dbOp = en.getDbOp();

             sStm.append("select nome ");
             sStm.append("from cartelle ");
             sStm.append("where F_WRKSP("+identifierFolder+",'C')=id_cartella");

             dbOp.setStatement(sStm.toString());
             dbOp.execute();
             
             ResultSet rst = dbOp.getRstSet();

             if ( rst.next() ) 
                 sWrksp = rst.getString(1);               
             else {
                 sWrksp = null;     
             }
               
             en.disconnectClose();
             
             return sWrksp;
               
           }
           catch (Exception e) {               
             en.disconnectClose();
             throw new Exception("ICartella::getWrkSp()\n" + e.getMessage());
           }               
	       
   }
  
}

