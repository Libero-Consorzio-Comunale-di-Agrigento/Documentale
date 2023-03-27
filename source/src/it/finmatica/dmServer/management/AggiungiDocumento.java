package it.finmatica.dmServer.management;

/*
 * AGGIUNTA DI UN DOCUMENTO
 * 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   14/09/2005
 * 
 * */

import java.io.*; 
import java.util.HashMap;
import java.util.Vector;

import it.finmatica.dmServer.*;
import it.finmatica.dmServer.util.*;
import it.finmatica.dmServer.competenze.*;
import it.finmatica.dmServer.management.macroAction.AutomaticFolder;
import it.finmatica.dmServer.mapping.GDMapping;

public class AggiungiDocumento extends ManageDocumento
{
    
  private String idTipoDocumento; 
  private String idLibreria;
  private String sTipoDocumento;
  private ElapsedTime elpsTime;
  private Vector  valori;
  private HashMap<String,Object> valoriHM; //Sostiuisce la vecchia valori
  private String sqlInfoCampo="";
  private long   nMaxNAllegati=-1;
  private boolean bSkipUnknowField=false;
  
  private Vector<String> listSkipUnknowField = new Vector<String>();
  
  Vector fiVector = null;
  private boolean skipAddCompetenzeModello=false;
  
  // ***************** METODI DI INIZIALIZZAZIONE ***************** // 
      
  /*
   * METHOD:      Constructor(String,Environment)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Creazione di un documento a partire
   *              dall'idTipoDocumento e variabili di
   *              ambiente
   * 
   * RETURN:      none
  */    
  public AggiungiDocumento(String idTipoDocumento, Environment vEnv) throws Exception
  {
         this(idTipoDocumento, "",  vEnv);
  }

  /*
   * METHOD:      Constructor(String,String,Environment)
   *              dal nome del tipo documento, area 
   *              e variabili di ambiente
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Creazione di un documento a partire
   *              dall'idTipoDocumento, area e variabili
   *              di ambiente
   * 
   * RETURN:      none
  */      
  public AggiungiDocumento(String tipoDocumento, String vArea, Environment vEnv) throws Exception
  {  
         super("0",vEnv);
         
         
         try {          
           this.inizializza(tipoDocumento, vArea, vEnv);          
         }
          catch (Exception e)
         {
           vEnv.disconnectClose();
           throw new Exception("Costruzione del Documento di tipo (cm,area) ("+tipoDocumento+","+vArea+")\n"+e.getMessage());
         }
  }

  /*
   * METHOD:      inizializza(String,String,Environment)
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Crea un documento nuovo con ID=0 e tentando
   *              di recuperare la libreria dal tipoDocumento
   *              se collegato ad HummingBird effettua il login
   *              tramite il ws di pantarei e dopo crea il
   *              documento
   * 
   * RETURN:      void
  */
  private void inizializza(String tipoDocumento, String vArea, Environment vEnv) throws Exception
  {
  
          String sTipoDocDM="";
          String sTipoLibreria="";
          
          valori = new Vector();
          valoriHM = new HashMap<String,Object>();
          sqlInfoCampo="";
          //Gestione del Mapping per il tipo Documento
          try { 
            sTipoDocumento = tipoDocumento;
            sTipoDocDM =  varEnv.getGDMapping().getMappingTipoDoc(tipoDocumento);
          }      
          catch (Exception e)
          {           
            throw new Exception("AggiungiDocumento::Constructor() errore mapping\n"+ e.getMessage());
          }
          
          ModelInformation mi;
          //Recupero idTipoDoc e idLibreria 
          try { 
            mi = (new LookUpDMTable(vEnv)).lookUpTipoDoc(sTipoDocDM, vArea);
          }
          catch (Exception e)
          {
            throw new Exception("Errore recupero idTipoDoc da area e codice_modello\n"+ e.getMessage());
          }
          
          this.varEnv =  vEnv;          
          //int i = sTipoLibreria.indexOf("@");

          idTipoDocumento = mi.getIdTipoDoc();//sTipoLibreria.substring(0,i);
          idLibreria      = ""+mi.getLibreria();//sTipoLibreria.substring(i+1);
          nMaxNAllegati   = mi.getNMaxAllegato();
         
          elpsTime = new ElapsedTime("AGGIUNGI_DOCUMENTO",varEnv);

          try { 
          // *************  SONO COLLEGATO CON IL DOCUMENTALE DI FINMATICA ************************* //
          /* */ if (vEnv.Global.DM.equals(vEnv.Global.FINMATICA_DM)) {                                     
          /* */     //Testo la competenza di Creazione sul tipo documento
          /* */     Abilitazioni abilitazione;                                                    
          /* */     abilitazione = new Abilitazioni(Global.ABIL_TIPIDOC, ""+idTipoDocumento , "C"); 
          /* */     UtenteAbilitazione ua = new UtenteAbilitazione(varEnv.getUser(), varEnv.getGruppo(), varEnv.getRuolo(), varEnv.getPwd(),  varEnv.getUser(), varEnv);
          /* */     try {
          /* */       if (varEnv.getByPassCompetenze() || (new GDM_Competenze(varEnv)).verifica_GDM_Compentenza(ua,abilitazione) == 1) { 
          /* */           if ( (vArea == null) || (vArea.equals("")) )
          /* */                vArea = (new LookUpDMTable(vEnv)).lookUpArea(sTipoDocDM);                              
          /* */          super.createDocument(idTipoDocumento, vArea, idLibreria);                               
          /* */          iAllegati = 0;                                                          
          /* */       }                                                                             
          /* */       else throw new Exception("Utente "+varEnv.getUser()+" non autorizzato");    
          /* */     }
          /* */     catch (Exception e) 
          /* */     {
          /* */       throw new Exception("Errore verifica competenze di Creazione sul tipoDocumento\n"+e.getMessage());
          /* */     }
          /* */ }                                                                                
          // *************************************************************************************** // 

          // *************  SONO COLLEGATO CON IL DOCUMENTALE DI HUMMINGBIRD *********************** //
          /* */ else if (vEnv.Global.DM.equals(vEnv.Global.HUMMINGBIRD_DM)) {
          /* */      try{
          /* */        pantareiLogin();
          /* */      }
          /* */      catch (Exception e) 
          /* */      {
          /* */        throw new Exception("AggiungiDocumento::Constructor() pantareiLogin\n"+e.getMessage());
          /* */      }
          /* */
          /* */      try {
          /* */        super.createDocument(idTipoDocumento, vArea, idLibreria); 
          /* */      }
          /* */      catch (Exception e) 
          /* */      {
          /* */        throw new Exception("AggiungiDocumento::Constructor() creazione Documento HUMM\n"+e.getMessage());
          /* */      }
          /* */ }
          // *************************************************************************************** //                  
          }
          catch (Exception e)
          {
            throw new Exception("Funzione di Login-Creazione-Abilitazioni\n"+ e.getMessage());
          }
  }


  // ***************** METODI DI GESTIONE DEI VALORI ***************** //

  /*
   * METHOD:      aggiungiDati(Object,Object)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Data la coppia (campo,valore) questa viene inserita
   *              sulla lista dei valori.
   *              Vale per tutti i DM
   * 
   * RETURN:      void
  */ 
  public void aggiungiDati(Object campo, Object valore) throws Exception
  {       
         String sCampo = "";
         
         if (campo instanceof String){
        	if (campo.equals(_CART_AUTO_FIELD)) folderAutoString=""+valore;
        	 
            try {
              sCampo = varEnv.getGDMapping().getMappingCampo(sTipoDocumento,false,campo.toString());  
            }                  
            catch (Exception e)
            {
              varEnv.disconnectClose();
              throw new Exception("Errore mapping campo ("+campo.toString()+")\n"+ e.getMessage());
            }                      
         }        
         
         if (valore==null) valore = "";
         
         //Controllo se il campo non esiste già
         boolean bExists=false;
         for(int i=0;i<valori.size();i++)
        	 if (((keyval)valori.get(i)).getKey().equals(sCampo)) {
        		 ((keyval)valori.get(i)).setValoreInsertUpdate(valore);
        		 ((keyval)valori.get(i)).setBMarkedInUse(false);
        		 valoriHM.put(sCampo,valore);
        		 bExists=true;
        		 break;        		 
        	 }
         
         if (!bExists) {
	         keyval kVal = new keyval(campo.toString(),valore,true);         
	         valori.add(kVal);
	         valoriHM.put(sCampo,valore);
	         
	         if (!sqlInfoCampo.equals("")) sqlInfoCampo+=" UNION ALL ";
	                         
	         if (campo instanceof String)
	        	 sqlInfoCampo+=(new LookUpDMTable(varEnv)).getSQLInfoCampo(sCampo, idTipoDocumento,null);
	         else
	        	 sqlInfoCampo+=(new LookUpDMTable(varEnv)).getSQLInfoCampo(null, idTipoDocumento,campo.toString());
         }
                  
  }
  
  private void calcolaDati() throws Exception
  {
         String idCampo = "-1";
         String sCampo = "";
         Vector fiLocalVector = null;
         
         //if (sqlInfoCampo.equals("")) return;
         
         //lookUp per recuperare i campi idCampo
         try {            
        	 if (!sqlInfoCampo.equals("")) fiLocalVector = (new LookUpDMTable(varEnv)).lookUpVectorInfoCampo(sqlInfoCampo);
         }                  
         catch (Exception e)
         {
           varEnv.disconnectClose();
           throw new Exception("Errore nel recupero delle Informazioni Campo\n"+e.getMessage());
         }         
         
         //Unico il vettore globale (di istanza) con quello locale nuovo
         if (fiVector==null)
        	 fiVector= fiLocalVector;
         else {
        	 if (fiLocalVector!=null) for(int i=0;i<fiLocalVector.size();i++) fiVector.add(fiLocalVector.get(i));
         }        	 
         
         if (fiVector==null) return;
        // listSkipUnknowField
         if (!bSkipUnknowField ) {
        	 if (!checkUnknowField())  {
	        	 StringBuffer sMessage = new StringBuffer("Attenzione! Impossibile procedere, non tutti i campi specificati esistono nel modello.\n");
	        	 
	        	 sMessage.append("SQL RECUPERO INFO CAMPI="+sqlInfoCampo+"\n\nCampi passati:\n");
	        	 for(int i=0;i<valori.size();i++) {
	        		 sMessage.append("- "+((keyval)valori.get(i)).getKey()+"\n");
	        	 }
	        	 
	        	 throw new Exception(sMessage.toString());
        	 }
         }                 
         
         for(int i=0;i<fiVector.size();i++) {
        	 FieldInformation fi = (FieldInformation)fiVector.get(i);
        	 //keyval kVal= (keyval)valori.get(i);
        	 
        	 //Controllo se qualche campo inserito è non in uso
        	 if (!fi.getInUso().equals("Y")) {
        		 StringBuffer sMessaggio = new StringBuffer("");
         		   
         		 sMessaggio.append("[DMSERVER_WARNING] - Non è possibile l'aggiornamento del valore per il campo "+fi.getNomeCampo()+" per creazione (tipo_documento="+idTipoDocumento+").\n");
         		 sMessaggio.append("Il campo "+fi.getNomeCampo()+" non è IN USO");         		   
         		 System.out.println(sMessaggio.toString());
        		 //throw new Exception("Aggiunta del valore per campo ("+fi.getNomeCampo()+"). ATTENZIONE! Il campo non è in uso. Impossibile continuare\n");
        	 }
        	 
	         //Aggiungo il valore sul vettore.....        	 
	         try {               	         
	        	//.....ma solo se il campo era già dentro e non è stato toccato
	        	if (!esisteCampo(fi.getNomeCampo(),true))
	        		aDocumento.addValore(fi.getNomeCampo(),fi.getIdCampo(), valoriHM.get(fi.getNomeCampo()), (FieldInformation)fiVector.get(i));
	         }                  
	         catch (Exception e)
	         {    varEnv.disconnectClose();
	              throw new Exception("Aggiunta del valore per campo ("+fi.getNomeCampo()+") \n"+ e.getMessage());
	         }        	 
         }

  }  
  
  private boolean checkUnknowField() {
	      boolean bCheck=true;
	      
	      if (listSkipUnknowField==null || listSkipUnknowField.size()==0)
	    	 bCheck=(fiVector.size()==valori.size()); 
	      else {
	    	 for(int i=0;i<valori.size();i++) {
	    		 String nomeCampo = (((keyval)valori.get(i)).getKey()).toLowerCase();
	    		 
	    		 boolean bEsiste=false;
	    		 //Controllo se è nella lista degli skip
	    		 for(int j=0;j<listSkipUnknowField.size();j++) {
	    			 if (nomeCampo.equals(listSkipUnknowField.get(j).toLowerCase() ))  {
	    				 bEsiste=true;
	    				 break;
	    			 }
	    		 }
	    		 
	    		//Se non lo è controllo che esista
	    		 if (!bEsiste) {
	    			 for(int j=0;j<fiVector.size();j++) {
	    				 FieldInformation fi = (FieldInformation)fiVector.get(j);
	    				 
	    				 if (nomeCampo.equals(fi.getNomeCampo().toLowerCase())) {
	    					 bEsiste=true;
	    					 break;
	    				 }	    					    				
		    		 }		    		 
	    		 }
	    		 
	    		 //Se non esiste fra i campi del db....esco con errore
	    		 if (!bEsiste) {
	    			 bCheck=false;
	    			 break;
	    		 }
	    		 
	    	 }
	      }	    	  
	      
	      return bCheck;
  }
 
  
  // ***************** METODI DI GESTIONE DEGLI OGGETTI FILE ***************** //
  
  /*
   * METHOD:      aggiungiAllegatoConPadre(InputStream,String,String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: istream         -> File da inserire
   *              pathFile        -> nome del file   
   *              filePadre   -> nome del file padre
   *              
   *              -Per Finmatica: 
   *                 Viene richiamata la funzione aggiungiAllegato della classe
   *                 superiore
   *              -Per HummingBird:
   *                 ............................
   * 
   * RETURN:      void
  */
  public void aggiungiAllegatoConPadre(InputStream istream,String pathFile, String filePadre)  throws Exception
  {     
         super.aggiungiAllegato(istream,pathFile,filePadre,"S");
  }
  
  public void aggiungiAllegato(InputStream istream, String pathFile)  throws Exception
  {
	      aggiungiAllegato(null, istream,  pathFile);
  }
  
  /*
   * METHOD:      aggiungiAllegato(InputStream,String)
   * SCOPE:       PUBLIC 
   *
   * DESCRIPTION: Inserimento del file nella lista degli oggetti file
   *              Il file viene passato sotto forma di InputStream
   *              E' il metodo aggiungiAllegato della classe superiore
   * 
   * RETURN:      void
  */ 
  public void aggiungiAllegato(String contentType,InputStream istream, String pathFile)  throws Exception
  {     
         try {
           super.aggiungiAllegato(istream,pathFile,null,"N",contentType);         
         }                  
         catch (Exception e)
         { 
           varEnv.disconnectClose();
           throw new Exception("AggiungiDocumento::aggiungiAllegato(InputStream, String)\n"+ e.getMessage());
         }

  }
  
  /*
   * METHOD:      aggiungiAllegato(String,String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Inserimento del file nella lista degli oggetti file
   *              Il file viene passato sotto forma di stringa
   *              E' il metodo aggiungiAllegato della classe superiore
   * 
   * RETURN:      void
  */ 
  public void aggiungiAllegato( String stringa, String pathFile)  throws Exception
  {     
         try {
           super.aggiungiAllegato(new ByteArrayInputStream(stringa.getBytes()), pathFile, "N");
         }                  
         catch (Exception e)
         {
           throw new Exception("AggiungiDocumento::aggiungiAllegato(String, String)\n"+ e.getMessage());
         }

  }
  
  /*
   * METHOD:      settaFileP7M(InputStream)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Setta il file p7m per HUMMINGBIRD
   * 
   * RETURN:      void
  */
  public void settaFileP7M(InputStream file) 
  {
         aDocumento.settaFileP7M(file);         
  }

  /*
   * METHOD:      setPadre()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Aggiungi il riferimento al documento padre
   *                 
   * RETURN:      void
  */    
  public void setPadre(String idPadre)
  {         
	  	 aDocumento.settaPadre(idPadre);
  }

  // ***************** METODI DI GESTIONE DEI RIFERIMENTI ***************** //  
  
  /*
   * METHOD:      aggiungiRiferimento(String,String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Inserimento riferimento di tipo "tipoRif"
   *              fra il documento attuale ed il documento "docRif"
   * 
   * RETURN:      void
  */    
  public void aggiungiRiferimento(String docRif, String tipoRif) throws Exception
  {
         try { 
           //Non ho ancora salvato la prima volta
           if (aDocumento.getIdDocumento().equals("0") && varEnv.Global.DM.equals(varEnv.Global.FINMATICA_DM))
              throw new Exception("AggiornaDocumento::aggiungiRiferimento()\n Impossibile aggiungere riferimento per documento ancora mai salvato ");
      
           aDocumento.aggiungiRiferimento(docRif, tipoRif);
         }
         catch (Exception e)
         {
           //Sono con una connessione Esterna DISTRUGGO IL SAVEPOINT
           if (varEnv.Global.CONNECTION!=null) {
        	   try {varEnv.rollbackToSavePoint();}catch(Exception ei){}
           }        	 
           varEnv.disconnectRollback();
           throw new Exception("AggiornaDocumento::aggiungiRiferimento()\n"+ e.getMessage());
         }   
  }
  
  /*
   * METHOD:      eliminaRiferimento(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Elimina riferimento fra il 
   *              documento attuale ed il 
   *              documento "docRif"
   * 
   * RETURN:      void
  */      
  public void eliminaRiferimento(String docRif, String tipoRif) throws Exception
  {
         try {
           //Non ho ancora salvato la prima volta
           if (aDocumento.getIdDocumento().equals("0")  && varEnv.Global.DM.equals(varEnv.Global.FINMATICA_DM))
              throw new Exception("AggiornaDocumento::aggiungiRiferimento()\n Impossibile eliminare riferimento per documento ancora mai salvato ");
   
           aDocumento.eliminaRiferimento(docRif,tipoRif);
         }
         catch (Exception e)
         {
           //Sono con una connessione Esterna DISTRUGGO IL SAVEPOINT
           if (varEnv.Global.CONNECTION!=null) {
              varEnv.rollbackToSavePoint();
           }        	 
           varEnv.disconnectRollback();
           throw new Exception("AggiornaDocumento::eliminaRiferimento()\n"+ e.getMessage());
         }
  }  
  
  public void salvaAllegatiTemp(boolean bFlag) {	     
	     aDocumento.setAllegatiTempModulistica(bFlag);
  }
  
  public void salvaAllegatiTempCr(String cr) {	     
	     aDocumento.setAllegatiCrTempModulistica(cr);
  }  

  // ***************** METODI DI REGISTRAZIONE DEL DOCUMENTO ***************** //

  /*
   * METHOD:      salvaDocumentoBozza()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Inserimento del documento con stato BOZZA
   * 
   * RETURN:      boolean
  */ 
  public boolean salvaDocumentoBozza() throws Exception
  {    
         return salvaDocumento(Global.STATO_BOZZA);
          
  }

  /*
   * METHOD:      salvaDocumentoCompleto()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Inserimento del documento con stato COMPLETO
   * 
   * RETURN:      boolean
  */ 
  public boolean salvaDocumentoCompleto() throws Exception
  {      
         return salvaDocumento(Global.STATO_COMPLETO);
  }

  /*
   * METHOD:      salvaDocumentoAnnullato()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Inserimento del documento con stato ANNULLATO
   *              (Attenzione che per adesso usiamo lo stato
   *               di cancellato per intendere l'annullato)
   * 
   * RETURN:      boolean
  */
  public boolean salvaDocumentoAnnullato() throws Exception
  {      
         return salvaDocumento(Global.STATO_ANNULLATO);
  }
  
  /*
   * METHOD:      salvaDocumentoPreBozza()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Inserimento del documento con stato PREBOZZA
   * 
   * RETURN:      boolean
  */
  public boolean salvaDocumentoPreBozza() throws Exception
  {      
         return salvaDocumento(Global.STATO_PREBOZZA);
  }  
  
  /*
   * METHOD:      salvaDocumento(String)
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Inserimento del documento con stato "stato"
   *              Se la regitrazione è stata effettuata, il 
   *              metodo effettua la commit, in caso contrario
   *              la rollback
   * 
   * RETURN:      boolean
  */
  private boolean salvaDocumento(String stato) throws Exception
  {      
	     long nAllegatiDoc = getNumAllegati();
	     if (nMaxNAllegati!=-1 && nAllegatiDoc>nMaxNAllegati) {
	    	 if (varEnv.Global.CONNECTION!=null) {
                varEnv.rollbackToSavePoint();
             }           
             varEnv.disconnectRollback();
             throw new Exception("Attenzione! il modello può contenere al massimo "+nMaxNAllegati+" allegato/i.\nSi sta cercando di inserirne "+nAllegatiDoc); 
	     }
	  
	     try {
	       calcolaDati();	       
	     }
         catch (Exception e) {
	        // e.printStackTrace();
           //Sono con una connessione Esterna DISTRUGGO IL SAVEPOINT

           if (varEnv.Global.CONNECTION!=null) {
               try {
                varEnv.rollbackToSavePoint();
               }
               catch (Exception ei) {

               }
           }
           varEnv.disconnectRollback();
           throw new Exception("Salvataggio del Documento di tipo (idTipoDoc) ("+idTipoDocumento+") - Errore nel calcolaDati\n"+e.getMessage());                      
         }
	  
         try {  
        	 
              insertDocumentInRepository(stato);    
        	 
        	 
		   	  try {
		   		  Vector<String> v = (new LookUpDMTable(varEnv)).lookUpInfoAr_Cm_Cr_Area(aDocumento.getIdDocumento(),null,false);
		   		  
		   		  aDocumento.setPathFileArea(v.get(0));
		   		  aDocumento.setArcmcr(v.get(1));
		   	  }
		   	  catch (Exception e) {				 
				  throw new Exception("lookUpInfoAr_Cm_Cr_Area\n" + e.getMessage());
			  }            
             
             //Sincronizzo i file gestiti da FS
             aDocumento.syncroFS(true);                           
             
             aDocumento.finalizzaGestioneAllegatiTemp();   
             aDocumento.disconnectDbOpAllegatiTemp();      
          
             //Gestisco le impronte dei file che trovo dentro la lista (se il parametro è impostato)
             if (aDocumento.getOggettiFile().size()!=0)
            	 super.gestisciImpronte();
             
             this.ultAggiornamento = aDocumento.getUltAggiornamento();
             //valori.clear();
             sqlInfoCampo="";
         }
         catch (Exception e) {      
           //Sono con una connessione Esterna DISTRUGGO IL SAVEPOINT
           if (varEnv.Global.CONNECTION!=null) {
              try {varEnv.rollbackToSavePoint();}catch (Exception ei){varEnv.getDbOp().getConn().rollback(); varEnv.getDbOp().rollback(); }        	   
           }           
           
           aDocumento.finalizzaGestioneAllegatiTemp();   
           aDocumento.disconnectDbOpAllegatiTemp();         
           
           varEnv.disconnectRollback();
           codeError=aDocumento.getCodeErrorSaveDoc();
           descrCodeError=aDocumento.getDescrErrorSaveDoc();
           throw new Exception("Salvataggio del Documento di tipo (idTipoDoc) ("+idTipoDocumento+") - Errore nel salvataggio\n"+e.getMessage());                      
         }        
    
         varEnv.disconnectCommit();
         
         //Pulisco gli oggetti in memoria per il profilo
         aDocumento.getOggettiFile().removeAllElements();
         ((Vector)aDocumento.getACL()).removeAllElements();
         aDocumento.cleanExtraCompetenze();
         
         //Marco i campi come già usati
         for(int i=0;i<valori.size();i++) ((keyval)valori.get(i)).setBMarkedInUse(true);         
         
         //Gestione SysIntegration
         /*try {  
           manageSysIntegration();
         }
         catch (Exception e){ 
           varEnv.disconnectRollback();
           codeErrorPostSave=Global.CODERROR_SYNCRO_INTEGRATION_ERROR;
           descrCodeErrorPostSave="Errore in sinconizzazione con altri sistemi";  
           System.out.println("DMServer - AggiungiDocumento post save error: "+descrCodeErrorPostSave);  
           return true;
         }   */      
         
         //GESTIONE DELL'AUTOMATIC FOLDER CON RELATIVA CATCH DELLA varEnv.disconnectRollback(); nel caso di errore o commit se ok.
         if (folderAutoString!=null) {
        	 try {
     			AutomaticFolder am = new AutomaticFolder(aDocumento.getIdDocumento(),folderAutoString,varEnv);
     			
     			am.make();
     			varEnv.disconnectCommit();
     		 }
     	     catch (Exception e) {     	    	
     	    	codeErrorPostSave=Global.CODERROR_POSTSAVEDOCUMENT_FOLDERAUTO;
     	    	descrCodeErrorPostSave=Global.DESCRERROR_POSTSAVEDOCUMENT_FOLDERAUTO+"\n"+e.getMessage();
     	    	System.out.println("DMServer - AggiungiDocumento post save error: "+descrCodeErrorPostSave);
     	    	varEnv.disconnectRollback();          	    	
             }
         }         
        	 
         folderAutoString=null;
         //FINE GESTIONE DELL'AUTOMATIC FOLDER
         
         return true;
  }


  /*
   * METHOD:      insertDocumentInRepository(String)
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Richiamo delle funzioni interne di inserimento
   *              di un documento a seconda del tipo di documentale
   * 
   * RETURN:      boolean
  */
  private boolean insertDocumentInRepository(String stato) throws Exception
  {      
          try {
        	elpsTime.start("********* INSERT DOCUMENT *********","INSERIMENTO DOCUMENTO CON VALORI ED OGGETTI FILE");  
        	
        	aDocumento.setSkipReindexFullTextField(skipReindexFullTextField);
        	aDocumento.creaVersione(creaVersione);
        	
            if (aDocumento.insertDocument(stato)) {                           	            	
            	ultimaVersione=aDocumento.getUltimaVersione();
                // ******************  SONO COLLEGATO CON IL DOCUMENTALE DI FINMATICA ****************** //
                /* */ if (varEnv.Global.DM.equals(varEnv.Global.FINMATICA_DM))  {                        
                /* */ 
                /* */    //Cambia lo stato documento a "stato"
                /* */    aDocumento.getStatusDocumento().setIdDocumento(aDocumento.getIdDocumento());
                /* */    try {                
                /* */      aDocumento.cambiaStatoDocumento(stato,true); 
                /* */    }
                /* */    catch (Exception e) {                   
                /* */      throw new Exception("Errore in cambiaStatoDocumento a stato: "+stato+"\n" + e.getMessage());
                /* */    }
                /* */     
                /* */    //Eredita le competenze dal tipo documento
                /* */    //Salva le competenze inserite nel vettore di ACL
                /* */    try {
                			  if (!skipAddCompetenzeModello) {
	                /* */         String tmp =  aDocumento.getTipoDocumento().getCompetenze();
	                /* */         String comp = tmp.substring(0, tmp.indexOf("@"));
	                /* */         String mana = tmp.substring(tmp.indexOf("@")+1);
	                /* */         Abilitazioni abilitazione = new Abilitazioni(Global.ABIL_DOC, aDocumento.getIdDocumento() , 
	                /* */                                     comp, mana); 
	                /* */         abilitazione.setEreditaTipoOggetto(Global.ABIL_TIPIDOC);
	                /* */         abilitazione.setEreditaOggetto(aDocumento.getTipoDocumento().getIdTipodoc());
	                /* */ 
	                /* */         UtenteAbilitazione ua = new UtenteAbilitazione(varEnv.getUser(), varEnv.getGruppo(), varEnv.getRuolo(), varEnv.getPwd(),  varEnv.getUser());
	                /* */
	                /* */         (new GDM_Competenze(varEnv)).assegnaCompetenzaDocumento(ua, abilitazione);
                /* */  		  }	
                			  
                /* */         salvaACL("I"); 
                /* */
                /* */         salvaCompetenze("I");
                /* */    }
                /* */    catch (Exception e) 
                /* */    {                  
                /* */         throw new Exception("Errore nell'assegnare le competenze all'utente ("+varEnv.getUser()+") per il documento a partire dal suo modello\n" + e.getMessage());
                /* */    }               
                /* */    
                /* */ }
                // ******************************************************************************** //                               
            }

            else 
             throw new Exception("AggiungiDocumento::insertDocumentInRepository() Problemi nell'insert");
            
             elpsTime.stop(); 
          }
          catch (Exception e) {
                 throw new Exception(e.getMessage());
          } 

          return true;
  }

  public void setBSkipUnknowField(boolean skipUnknowField) {
	     bSkipUnknowField = skipUnknowField;
  }   
  
  private boolean esisteCampo(String sCampo, boolean bInUse) {
	  	  if (valori==null) return false;
	  	
		  for(int i=0;i<valori.size();i++) {
			  if (((keyval)valori.get(i)).getKey().equals(sCampo)) {
				  if (!bInUse) 
					  return true;
				  else {
					  if (((keyval)valori.get(i)).isBMarkedInUse() ) 
						  return true;
					  else
						  return false;
				  }
			  }
				 				 
		  }
			  
		  return false;
  }
  
  public void reconnect() throws Exception {
	     this.varEnv.connect();
  }
  
  public void setSkipAddCompetenzeModello(boolean skipAddCompetenzeModello) {
         this.skipAddCompetenzeModello = skipAddCompetenzeModello;
  }
  
  public void setListSkipUnknowField(Vector<String> listSkipUnknowField) {
		this.listSkipUnknowField = listSkipUnknowField;
  }

  public int getIdLog()  {
        return aDocumento.getDmALog().getId_log();
  }
}