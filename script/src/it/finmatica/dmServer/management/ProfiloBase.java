package it.finmatica.dmServer.management;

import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.util.DocUtil;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.util.ElapsedTime;
import it.finmatica.dmServer.util.JNDIParameter;

/**
 * Classe che gestisce un profilo (Documento del documentale).
 * Da questa classe derivano le due effettive classi instanziabili
 * Profilo, ProfiloLog 
 * 
 * @author  D. Scandurra, G. Mannella
 * @version 2.8
 *
*/ 
public abstract class ProfiloBase {
   
   protected ElapsedTime elpsTime; 
	
   protected String tipoDocumento;
   protected String area, cr;   
   protected Environment en;

   protected String idDocumento=null;

   protected Vector campi;
   protected Vector valori;
   protected Vector valoriAppend;
   
   protected AccediDocumento ad;   
   
   protected String error;
   protected String codeError;
   protected String version = "3.2";
   
   protected String errorPostSave=null;
   protected String codeErrorPostStave=null; 
   
   protected String descrCodeError=null;
   
   protected boolean bEscludiControlloCompetenze=false;
   
   private   boolean bAccessCrCmArea=false;
   
   protected HashSet<String> hsListAclToRetrieve = new HashSet<String>();
   
   protected boolean creaVersione = false;
   protected long ultimaVersioneCreata = -1;
   


public ProfiloBase( ) {        
   }         
   
  /**
   * Costruttore da utilizzare esclusivamente in creazione<BR>
   * di un profilo passando codice modello e area
   * 
   * @param codiceModello codice modello del profilo da creare
   * @param area area del profilo da creare
  */
  public ProfiloBase(String codiceModello, String area ) {
         this.tipoDocumento = codiceModello;
         this.area          = area;                  
  }

  /**
   * Costruttore da utilizzare esclusivamente in creazione<BR>
   * di un profilo passando codice modello e area.
   * Utilizzando questo costruttore non è necessario
   * lanciare la initVarEnv.
   * Il parametro cn escluderà automaticamente il
   * parametro ini (che andrà passato nullo) e viceversa
   * 
   * @param codiceModello codice modello del profilo da creare
   * @param area area del profilo da creare
   * @param user utente di AD4
   * @param passwd password di AD4
   * @param ini percorso del file di properties
   * @param cn connection
  */
  public ProfiloBase(String codiceModello, String area,
		             String user, String passwd, 
		             String ini, Connection cn) {
         this.tipoDocumento = codiceModello;
         this.area          = area;
         
         if (cn!=null)
        	 this.initVarEnv(user,passwd,cn);
         else
        	 this.initVarEnv(user,passwd,ini);
  }
    
  /**
   * Costruttore da utilizzare esclusivamente in fase di<BR> 
   * accesso o modifica di un profilo conoscendone la<BR>
   * chiave primaria
   * 
   * @param idProfilo identificativo del profilo da accedere/modificare
  */
  public ProfiloBase(String idProfilo) {
         this.idDocumento   = idProfilo;
  }
  
  /**
   * Costruttore da utilizzare esclusivamente in fase di<BR> 
   * accesso o modifica di un profilo conoscendone la<BR>
   * chiave primaria
   * Utilizzando questo costruttore non è necessario
   * lanciare la initVarEnv.
   * Il parametro cn escluderà automaticamente il
   * parametro ini (che andrà passato nullo) e viceversa
   * 
   * @param idProfilo identificativo del profilo da accedere/modificare
   * @param user utente di AD4
   * @param passwd password di AD4
   * @param ini percorso del file di properties
   * @param cn connection 
  */
  public ProfiloBase(String idProfilo,
		  	         String user, String passwd, 
                     String ini, Connection cn ) {
         this.idDocumento   = idProfilo;
         
         if (cn!=null)
        	 this.initVarEnv(user,passwd,cn);
         else
        	 this.initVarEnv(user,passwd,ini);
  }  

  /**
   * Costruttore da utilizzare esclusivamente in fase di<BR> 
   * accesso o modifica di un profilo conoscendone la<BR>
   * tripla area/codice modello/codice richiesta<BR>
   *
   * @param codiceModello codice modello del profilo da accedere/modificare
   * @param area area del profilo da accedere/modificare
   * @param codiceRichiesta codice richiesta del profilo da accedere/modificare
  */
  public ProfiloBase(String codiceModello, String area, String codiceRichiesta ) {
         this.tipoDocumento = codiceModello;
         this.area          = area;          
         this.cr            = codiceRichiesta;
  }

  /**
   * Costruttore da utilizzare esclusivamente in fase di<BR> 
   * accesso o modifica di un profilo conoscendone la<BR>
   * tripla area/codice modello/codice richiesta<BR>
   * Utilizzando questo costruttore non è necessario
   * lanciare la initVarEnv.
   * Il parametro cn escluderà automaticamente il
   * parametro ini (che andrà passato nullo) e viceversa 
   *
   * @param codiceModello codice modello del profilo da accedere/modificare
   * @param area area del profilo da accedere/modificare
   * @param codiceRichiesta codice richiesta del profilo da accedere/modificare
   * @param user utente di AD4
   * @param passwd password di AD4
   * @param ini percorso del file di properties
   * @param cn connection
  */
  public ProfiloBase(String codiceModello, String area, String codiceRichiesta,
		  		     String user, String passwd, 
                     String ini, Connection cn ) {
         this.tipoDocumento = codiceModello;
         this.area          = area;          
         this.cr            = codiceRichiesta;
         
         if (cn!=null)
        	 this.initVarEnv(user,passwd,cn);
         else
        	 this.initVarEnv(user,passwd,ini);         
  }
        
  /**
   * @param environment environment da cui leggere le
   * 				    variabili d'ambiente
  */
  public void initVarEnv(Environment environment) {
         try {           
           inizializza(environment);
         }
         catch (Exception e) {
           error = "Profilo::initVarEnv()\n"+e.getMessage();             
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
             Environment env = new Environment(user,passwd,appl,ente,"",ini );             
             inizializza(env);                     
         }
         catch (Exception e) 
         {
             error = "Profilo::initVarEnv()\n"+e.getMessage();             
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
             Environment env = new Environment(user,passwd,appl,ente,"",cn );             
             inizializza(env);
         }
         catch (Exception e) 
         {
             error = "Profilo::initVarEnv()\n"+e.getMessage();             
         }          
  }
  
  /**
   * @param user utente di AD4
   * @param passwd password di AD4
   * @param appl applicativo chiamante
   * @param ente ente chiamante
   * @param JNDIParameter Parametro jndi
  */
  public void initVarEnv(String user,String passwd,String appl,String ente, JNDIParameter jndi) {
         try {
             Environment env = new Environment(user,passwd,appl,ente,"",jndi );             
             inizializza(env);
         }
         catch (Exception e) 
         {
             error = "Profilo::initVarEnv()\n"+e.getMessage();             
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
   * Metodo da richiamare subito dopo il costruttore<BR>
   * user e password sono individuati da "AD4"<BR>
   * JNDIParameter rappresenta la stringa JNDI.
   * JNDIParameter è un classe del package
   * it.finmatica.dmServer.util
   *  
   * @param user utente di AD4
   * @param passwd password di AD4
   * @param jndi Parametro jndi
  */
  public void initVarEnv(String user,String passwd, JNDIParameter jndi) {
         initVarEnv( user, passwd, Global.APPL_STANDARD, Global.ENTE_STANDARD, jndi);         
  }  

  /**
   * Metodo da richiamare subito dopo il costruttore e
   * prima della initVarEnv
   * per decidere di non recuperare immediatamente
   * Area,Codice Modello, Codice Richiesta a fronte
   * dell'identificativo del documento. 
   * 
   * @param bAccess <BR>
   * 				True  vengono recuperati Area,Codice Modello, Codice Richiesta<BR>
   * 				False non vengono recuperati Area,Codice Modello, Codice Richiesta
  */
  public void setAccessCmCrArea(boolean bAccess) {
	     bAccessCrCmArea=bAccess;
  }
  
  /**
   * Gestione degli errori
   * 
   * @return errore in fase di registrazione o accesso
  */
  public String getError() {
         return error;
  }
  	
  /**
   * Metodo di lettura del valore di un campo
   * 
   * @param campo campo del profilo di cui leggere il valore
   * @return valore per il campo del profilo
  */
  public String getCampo(String campo) {
         try {
           return ad.leggiValoreCampo(campo);
         }
         catch(Exception e) 
         {
           error="Profilo::getCampo()\n"+e.getMessage();
           return "";
         }
  }
    
  /**
   * Restituisce l'identificativo del profilo
   * 
   * @return identificativo del profilo
  */
  public String getDocNumber() {
         return idDocumento;
  }   
    
  /**
   * @return area del profilo
  */
  public String getArea() {
	     if (area!=null) return area;
	     
	     if (idDocumento!=null) {
	    	 try {
		    	 if (en.getDbOp()==null) {  
		    		 en.connect();		    	 
			    	 area=(new DocUtil(en)).getAreaByIdDocumento(""+idDocumento);
			    	 en.disconnectClose();
		    	 }
		    	 else 
		    		 area=(new DocUtil(en)).getAreaByIdDocumento(""+idDocumento);
		    	 
		    	 return area;
	    	 }
	    	 catch(Exception e){
	    		 return null;
	    	 }
	     }
	     else
	    	 return area;
  }

  /**
   * @return codice richiesta del profilo
  */
  public String getCodiceRichiesta() {
      	 if (cr!=null) return cr;
	     
	     if (idDocumento!=null) {
	    	 try {
	    		 if (en.getDbOp()==null) {  
	    			 en.connect();	    		 
	    			 cr=(new DocUtil(en)).getCrByIdDocumento(""+idDocumento);    
	    			 en.disconnectClose();
	    		 }
		    	 else
		    		 cr=(new DocUtil(en)).getCrByIdDocumento(""+idDocumento);    
	    		 
		    	 return cr;
	    	 }
	    	 catch(Exception e){
	    		 return null;
	    	 }
	     }
	     else
	    	 return cr;   	  
  }

  /**
   * @return codice modello del profilo
  */
  public String getCodiceModello() {
	     if (tipoDocumento!=null) return tipoDocumento;
	     
	     if (idDocumento!=null) {
	    	 try {
	    		 if (en.getDbOp()==null) { 
	    			 en.connect();	    		 
			    	 tipoDocumento=(new DocUtil(en)).getModelloByIdDocumento(""+idDocumento);    
			    	 en.disconnectClose();
	    		 }
		    	 else
		    		 tipoDocumento=(new DocUtil(en)).getModelloByIdDocumento(""+idDocumento);  
			    	 
		    	 return tipoDocumento;
	    	 }
	    	 catch(Exception e){
	    		 return null;
	    	 }
	     }
	     else
	    	 return tipoDocumento;   	          
  }
  
  public String getUser() {
	     return en.getUser();
  }
    
  /**
   * @return True o False
  */
  public abstract Boolean accedi();  
  
  
  protected void inizializza(Environment env) {             
          campi        = new Vector();
          valori       = new Vector();
          valoriAppend = new Vector();
          en = env;
          error="@";
          codeError=Global.CODERROR_NOT_DEFINED;
          
          hsListAclToRetrieve.add(Global.ABIL_LETT);
          
          if (bEscludiControlloCompetenze==false) 
          	 en.byPassCompetenzeOFF();
          else
        	 en.byPassCompetenzeON();
          
          elpsTime = new ElapsedTime("PROFILO",en);
          
          //Cerco di settare l'idDocumento
          if (idDocumento==null) {
              try { 
             //    en.connect();
                 if (cr!=null) {
                    idDocumento=(new DocUtil(en)).getIdDocumento(area,tipoDocumento,cr);
                    if (idDocumento.equals("")) idDocumento="X";
                 }
                 else
                    idDocumento="X";           
              }
              catch (Exception e)
              {
                 idDocumento="X";
              }
          }
          //Ho il docnumber e ricavo ar,cm,cr
          else {
        	  try {
        		  Long.parseLong(idDocumento.trim());
        	  }
        	  catch (Exception e)
              {    
            	  error="Attenzione! IdDocumento passato ("+idDocumento+") invalido";         
        		  idDocumento=null;            	             
              }
        	  
              try {
            	if (bAccessCrCmArea) {
                   en.connect();
                   area=(new DocUtil(en)).getAreaByIdDocumento(""+idDocumento);
                   tipoDocumento=(new DocUtil(en)).getModelloByIdDocumento(""+idDocumento);
                   cr=(new DocUtil(en)).getCrByIdDocumento(""+idDocumento);            	  
                   en.disconnectClose();
            	}
              }
              catch (Exception e)
              {    
            	   idDocumento=null;
            	   error="Attenzione! Errore nel recupero di area/cm/cr\n"+e.getMessage();
                   try{en.disconnectClose();}catch (Exception ei){}
              }
          }
  }

  public String getCodeError() {
	     return codeError;
  }

  /**
   * Descrizione errore di post save
   * Questo errore si può verificare 
   * quando è stato impostato un meccanismo
   * di post salvataggio che può fallire.
   * Il documento sarà comunque già stato 
   * salvato quindi la salva della Profilo
   * restituirà true, mentre l'evento dopo il
   * salvataggio potrebbe essere fallito 
   * per qualche ragione.
   * E' quindi possibile testare questa variabile
   * per capire se si è o meno verificato
   * un errore 
   * 
   * @return Errore di post save o null
  */
  public String getErrorPostSave() {
	  	 return errorPostSave;
  }

  /**
   * Codice errore di post save
   * Questo errore si può verificare 
   * quando è stato impostato un meccanismo
   * di post salvataggio che può fallire.
   * Il documento sarà comunque già stato 
   * salvato quindi la salva della Profilo
   * restituirà true, mentre l'evento dopo il
   * salvataggio potrebbe essere fallito 
   * per qualche ragione.
   * E' quindi possibile testare questa variabile
   * per capire se si è o meno verificato
   * un errore 
   * 
   * Codici di errore possibili:
   * 
   * Global.CODERROR_POSTSAVEDOCUMENT_FOLDERAUTO
   * 		Codice Errore di post salvataggio per
   * 		cartelle in automatico
   * 
   * @return Codice errore di post save o null
  */  
  public String getCodeErrorPostStave() {
	  	 return codeErrorPostStave;
  }

  /**
   * Errore "pulito" generato da un trigger
   *
  */    
  public String getDescrCleanError() {
	     return descrCodeError;
  }
  
  public Environment getEn() {
		return en;
  }
    

  public void creaVersione(boolean creaVersione) {
		this.creaVersione = creaVersione;
  }
  
  	public long getUltimaVersioneCreata() {
  		if (ultimaVersioneCreata==0) return -1;
		return ultimaVersioneCreata;
	}
    	
}