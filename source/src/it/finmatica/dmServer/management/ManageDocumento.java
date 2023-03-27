package it.finmatica.dmServer.management;

/*
 * GESTIONE DOCUMENTO
 * 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   14/09/2005
 * 
 * */

import it.finmatica.dmServer.sysIntegration.SysIntegrationPending;
import it.finmatica.dmServer.util.DocUtil;
import it.finmatica.dmServer.util.LookUpDMTable;
import it.finmatica.dmServer.util.ManageConnection;
import it.finmatica.dmServer.util.keyval;
import it.finmatica.dmServer.Impronta.ImprontaAllegati;
import it.finmatica.dmServer.competenze.*;
import it.finmatica.dmServer.util.*;
import it.finmatica.wsPantarei.*;
import it.finmatica.jfc.dbUtil.*;
import it.finmatica.dmServer.*;
import it.finmatica.jfc.io.*;

import org.w3c.dom.Node;  
import java.util.*;
import java.io.*;

public class ManageDocumento
{   
  //Varibile che verifica
  //in fase di update se
  //ho modificato solo
  //il syspdf allegato
  int bOnlySysPdf;
  
  //Variabili di ambiente
  protected Environment varEnv;  
  //Variabile documento
  protected A_Documento aDocumento; 
  //Numero di allegati
  protected int iAllegati = 0;  
  protected String ultAggiornamento;  
  //Token per il login ad HummingBird
  protected Object token;   
  protected Node nodeTipoDoc;
  
  protected String folderAutoString        =  null;
  protected final String _CART_AUTO_FIELD  =  "CART_AUTO";  
  
  protected String codeError=null;
  protected String descrCodeError=null;
  
  protected String codeErrorPostSave=null;
  protected String descrCodeErrorPostSave=null;
  
  protected boolean bDontRepeatExistsRif=false;


  protected boolean skipReindexFullTextField=false;
  
  protected boolean creaVersione=false;
  protected long ultimaVersione=0;





public ManageDocumento(String idDocument, Environment vEnv) throws Exception
  {
          String sClasseDocumento=vEnv.Global.PACKAGE + "." + 
                                  vEnv.Global.DM + "_"  + 
                                  vEnv.Global.DOCUMENTO;
                                  
          bOnlySysPdf=0;

          //Devono esistere le variabili di ambiente
          if (vEnv==null) 
               throw new Exception("ManageDocumento::inizializzaDati - variabili d'ambiente non caricate");
               
          this.varEnv =  vEnv;
            
          //Creazione dell'istanza del documento
          try { 
            aDocumento = (A_Documento)Class.forName(sClasseDocumento).newInstance();
          }
          catch (Exception e) {
            throw new Exception("ManageDocumento::inizializzaDati Non riesco a creare l'oggetto di Classe: " + 
                                sClasseDocumento);
          }
          
          //Connessione al DB
          if (vEnv.Global.DM.equals(vEnv.Global.FINMATICA_DM))
             try {
                vEnv.connect();
                
                //Sono con una connessione Esterna CREO UN SAVEPOINT
                if (vEnv.Global.CONNECTION!=null) {
                	vEnv.createSavePoint();
                }
              }
              catch (Exception e) {
                throw new Exception("ManageDocumento::inizializzaDati connect\n" + e.getMessage());
              }              
                  
          //Inizializzazione del documento tramite connessione e variabili di ambiente
          try {
            aDocumento.inizializzaDati(vEnv);
            
            if (!idDocument.equals("0")) {
	            if (vEnv.Global.DM.equals(vEnv.Global.FINMATICA_DM)) {
	            	if (idDocument.equals("X")) {
		           	   throw new Exception("Documento inesistente");
		            }
		     		
		            if ( (new DocUtil(varEnv)).getIdTipoDocByIdDocumento(""+idDocument).equals("") )
		               throw new Exception("Tentativo di accesso al documento ("+idDocument+") Fallito! - Documento inesistente");
	            }
            }
          }
          catch (Exception e) {
        	try {vEnv.disconnectClose();} catch (Exception ei) {}
            throw new Exception(e.getMessage());
          }
            
          aDocumento.setIdDocumento(idDocument);
  }

   
  // ***************** METODI DI INIZIALIZZAZIONE ***************** //
      
  /*
   * METHOD:      loadDocument(String,Environment)
   * SCOPE:       PROTECTED
   *
   * DESCRIPTION: idDocument -> id Documento
   *              vEnv       -> Variabile di ambiente
   *              
   *              Carica un documento in memoria di una certa classe 
   *              a partire dall'id documento. 
   *              E' necessario specificare l'id del documento
   *              NON carica i relativi oggetti correlati (valori,
   *              oggetti_file) ma solo il documento con il suo id
   *              e le variabili globali e utente
   * 
   * RETURN:      void
  */  
  protected void loadDocument() throws Exception 
  {           
           
            // Interessa solo nel DM di Finmatica
            if (varEnv.Global.DM.equals(varEnv.Global.FINMATICA_DM)) {     
                //Carico gli stati del documento
                try {   
                  aDocumento.getStatusDocumento().inizializzaDati(varEnv,aDocumento.getIdDocumento());
                  aDocumento.getStatusDocumento().loadStato();                     
                  
                }
                catch (Exception e) {                 
                  throw new Exception("ManageDocumento::loadDocument di Status Documento\n"+e.getMessage());
                }                
            }
            else
                //Carico il token per il login ad HummingBird
                aDocumento.setToken(token);            
  }

  /*
   * METHOD:      createDocument(String,String,String,Environment)
   * SCOPE:       PROTECTED
   *
   * DESCRIPTION: idTipoDocumento -> id Tipo Documento
   *              vArea           -> Area
   *              libreria        -> Libreria
   *              vEnv            -> Variabile di ambiente
   *              
   *              Crea un documento a partire
   *              dal (tipoDocumento,Area)=>Modello, id della libreria
   * 
   * RETURN:      void
  */  
  protected void createDocument(String idTipoDocumento, String vArea, 
                                String libreria) throws Exception
  {            

            aDocumento.getTipoDocumento().setIdTipodoc(idTipoDocumento);

            // Interessa solo nel DM di Finmatica
            if (varEnv.Global.DM.equals(varEnv.Global.FINMATICA_DM)) {
               aDocumento.setArea(vArea);
               aDocumento.getLibreria().setIdLibreria(libreria);               
        	   
               //Riempio l'oggetto libreria
               try {
                 aDocumento.getLibreria().retrieve();
               }
               catch (Exception e) {                 
                 throw new Exception("ManageDocumento::createDocument retrieve Libreria\n" + e.getMessage());
               }

               //Riempio l'oggetto TipoDocumento
               try {
                 aDocumento.getTipoDocumento().retrieve(false); 
               }
               catch (Exception e) {                 
                 throw new Exception("ManageDocumento::createDocument retrieve TipoDocumento\n" + e.getMessage());
               }       
               
               //Inizializzo lo stato del documento a BOZZA
               try {
                 aDocumento.getStatusDocumento().inizializzaDati(varEnv,"0");
               }
               catch (Exception e) {                 
                 throw new Exception("ManageDocumento::createDocument inizializzaDati di Status Documento\n"+e.getMessage());
               }
           
               aDocumento.getStatusDocumento().setStato(Global.STATO_BOZZA);
            }
            else 
               aDocumento.setToken(token);                    
  }
  
    
  // ***************** METODI DI GESTIONE DEI VALORI ***************** //
  
  /*
   * METHOD:      aggiungiDatiDaTipoDoc(String,Object,Object)
   * SCOPE:       PROTECTED
   *
   * DESCRIPTION: fromIdTipoDoc -> id tipo documento
   *              campo
   *              valore
   *              
   *              Inserisce nel vettore dei valori di aDocumento
   *              la coppia (idCampo,valore).
   *              idCampo è ottenuto a partire da campo e fromIdTipoDoc
   *              mediante lookUp
   *             
   *              Il metodo è richiamato dalla loadDocument e createDocument
   * 
   * RETURN:      void
  */   
  protected void aggiungiDatiDaTipoDoc(String fromIdTipoDoc, Object campo, Object valore) throws Exception
  {
         String idCampo = "-1";        
         
         //Se campo è una stringa, allora
         //mi cerco l'id tramite lookUp
         if (campo instanceof String){           
            try {
              idCampo = (new LookUpDMTable(varEnv)).lookUpCampi((String)campo, fromIdTipoDoc);
            }                  
            catch (Exception e)
            { 
              varEnv.disconnectClose();
              throw new Exception("ManageDocumento::aggiungiDatiDaTipoDoc() errore lookUp\n"+ e.getMessage());
            }
         }
         //Ho passato direttamente l'id
         else            
            idCampo = campo.toString();

         if (valore==null) valore = "";
         
         //Aggiungo il valore sul vettore    
         try {
           aDocumento.addValore(idCampo, valore.toString());        
         }                  
         catch (Exception e)
         {
              varEnv.disconnectClose();
              throw new Exception("ManageDocumento::aggiungiDatiDaTipoDoc() errore addValore\n"+ e.getMessage());
         }
  }
  
  
  // ***************** METODI DI GESTIONE DEGLI OGGETTI FILE ***************** //
  
  /*
   * METHOD:      aggiungiAllegato(InputStream,String,String)
   * SCOPE:       PROTECTED
   *
   * DESCRIPTION: Aggiunge un allegato al documento dato in input come
   *              InputStream.
   *              Il path serve per memorizzare anche il nome del file
   *              Viene passato anche il flag "allegato" (S o N)
   * 
   * RETURN:      void
  */   
  protected void aggiungiAllegato(InputStream istream, 
                                  String pathFile, 
                                  String sAllegato) throws Exception
  {     
            aggiungiAllegato(istream,pathFile,null,sAllegato);
  }
  
  protected void aggiungiAllegato(InputStream istream, String pathFile, 
            String idFilePadre,String sAllegato) throws Exception
  { 
	        aggiungiAllegato( istream,  pathFile, 
	        				  idFilePadre, sAllegato, null);
  }
  
  /*
   * METHOD:      aggiungiAllegato(InputStream,String,String,String)
   * SCOPE:       PROTECTED
   *
   * DESCRIPTION: Aggiunge un allegato al documento dato in input come
   *              InputStream.
   *              Il path serve per memorizzare anche il nome del file
   *              Viene passato anche il flag "allegato" (S o N)
   *              Viene passato anche l'eventuale id del file padre
   * 
   * RETURN:      void
  */     
  protected void aggiungiAllegato(InputStream istream, String pathFile, 
                                  String idFilePadre,String sAllegato, String contentType) throws Exception
  {              
          String idFormato="";
          String sFileName="";

          //Estrae il nome file e sistema il path
          try{
            String sPath = Global.adjustsPath(varEnv.Global.WEB_SERVER_TYPE, pathFile);          
            sFileName = Global.lastTrim(sPath, "/",varEnv.Global.WEB_SERVER_TYPE);
          }
          catch (Exception e) {              
            throw new Exception("ManageDocumento::aggiungiAllegato errori di trasformazione path\n"+ e.getMessage());
          }

          //Recupero l'id del formato a partire dall'estensione del file
          try {
        	String ext;
        	
        	
        	if (contentType==null)
        		ext=Global.lastTrim(sFileName,".",varEnv.Global.WEB_SERVER_TYPE);
        	else {
        		MimeTypeMapping mtm = new MimeTypeMapping();
        		
        		ext=mtm.getFileExtFromMime(contentType);
        	}
        		
        	
        	
        	//javax.activation.MimetypesFileTypeMap a;
        	//a.getDefaultFileTypeMap().;
           
            idFormato = (new LookUpDMTable(varEnv)).lookUpFormato(ext);
            
            //GESTIONE SYS_PDF
            if (Global.lastTrim(sFileName,".",varEnv.Global.WEB_SERVER_TYPE).equals("SYS_PDF")) {
               if (bOnlySysPdf==0) bOnlySysPdf=1;
               
               Vector v = aDocumento.getOggettiFile();
               
               for(int i=0;i<v.size();i++) {
                  A_Oggetti_File aOgfi = (A_Oggetti_File)v.get(i);
                  
                  //Esiste già e quindi se è sto aggiornando un completo , evito di farlo
                  if (aOgfi.getIdFormato().equals((new LookUpDMTable(varEnv)).lookUpFormato("SYS_PDF"))) {
                     //bOnlySysPdf=2;
                     i=v.size();
                  }
               }
                              
               sFileName=sFileName.substring(0,sFileName.indexOf(Global.lastTrim(sFileName,".",varEnv.Global.WEB_SERVER_TYPE))-1);
            }
            else
               bOnlySysPdf=2;
               
            //GESTIONE KFX   
            if (Global.lastTrim(sFileName,".",varEnv.Global.WEB_SERVER_TYPE).equals("KFX")) {                          
               sFileName=sFileName.substring(sFileName.lastIndexOf("\\")+1,sFileName.lastIndexOf("."));              
            }
          }
          catch (Exception e) {              
            throw new Exception("ManageDocumento::aggiungiAllegato errore di lookup\n"+ e.getMessage());
          }

          //Aggiungo l'oggetto file nel vettore
          try {                  	
            aDocumento.addOggettoFile(idFormato,sFileName,sAllegato,idFilePadre,istream);
          }
          catch (Exception e) {              
            throw new Exception("ManageDocumento::aggiungiAllegato errore di addOggettoFile\n"+ e.getMessage());
          }
                   
  }


  // ***************** METODI DI GESTIONE DEGLI OGGETTI FILE CON IMPRONTA ***************** //
  
  /*
   * METHOD:      aggiungiAllegatoeImpronta(InputStream,InputStream,String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Inserimento del file nella lista degli oggetti file
   *              Inserisce anche un oggetto file figlio che è 
   *              l'impronta
   *                
   * 
   * RETURN:      void
  */   
  public void aggiungiAllegatoeImpronta(InputStream istream,
                                        InputStream istreamImpronta, 
                                        String pathFile)  throws Exception
  {     
         try {
               //Sono in nuovo, lo aggiungo direttamente        
	           this.aggiungiAllegato(istream, pathFile,"N");
	          
	           Impronta impronta = new Impronta("SHA1");
	
	           byte[] bImpronta = impronta.dbHash(istreamImpronta, "SHA1");
	
	           this.aggiungiAllegato((new ByteArrayInputStream(bImpronta)), Global.replaceAll(pathFile,".","_")+".SYS_HASH", "S");	                      
           //Sono in modifica....cerco l'allegato per aggiornarlo    
         }                  
         catch (Exception e)
         {
           varEnv.disconnectRollback();
           throw new Exception("AggiungiDocumento::aggiungiAllegato()\n"+ e.getMessage());
         }

  }    

  /*
   * METHOD:      aggiungiImprontaAdAllegato(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Inserisce l'impronta al relativo allegato
   *              già esistente sul DB
   *                
   * RETURN:      void
  */     
  public void aggiungiImprontaAdAllegato(String nomeFile)  throws Exception
  {
         
         Vector vOgfi = aDocumento.getOggettiFile();        

         //Ricerca allegato "nomeFile" sul DB
         for (int i=0;i<vOgfi.size();i++) 
         {
             A_Oggetti_File obj = (A_Oggetti_File)vOgfi.get(i);

             if (obj.getFileName().equals(nomeFile)) {
                
                //Se l'impornta esiste già, non ha senso reinserirla
                try  {
                  if (verificaEsistenzaImpronta(nomeFile)) 
                     throw new Exception("ManageDocumento::aggiungiImprontaAdAllegato(@) - Il file "+nomeFile+" contiene già un'impronta"); 
                }
                catch (Exception e) 
                {
                  varEnv.disconnectRollback();
                  throw new Exception("ManageDocumento::aggiungiImprontaAdAllegato(@) - verificaImpronta\n"+e.getMessage()); 
                }
                
                //Inserimento dell'impronta
                try {                                 
                   String idOggettoFile = (new LookUpDMTable(varEnv)).lookUpOggettoByName(nomeFile, aDocumento.getIdDocumento());
                   
                   Impronta impronta = new Impronta("SHA1");

                   byte[] bImpronta = impronta.dbHash((InputStream)obj.getFile(), "SHA1");

                   this.aggiungiAllegato((new ByteArrayInputStream(bImpronta)), Global.replaceAll(nomeFile,".","_")+".SYS_HASH",idOggettoFile, "S");
                   
                   return;
                }
                catch (Exception e) 
                {
                   varEnv.disconnectRollback();
                   throw new Exception("ManageDocumento::aggiungiImprontaAdAllegato(@) - Genera e registra impronta\n"+e.getMessage()); 
                }
                
             }    
         }
         
         varEnv.disconnectRollback();
         throw new Exception("ManageDocumento::aggiungiImprontaAdAllegato(@) - Il file "+nomeFile+" a cui aggiungere l'impronta non esiste");
  }

  /*
   * METHOD:      verificaImprontaOggettoFile(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: VERIFICA SE IL FILE NOMEOGGETTOFILE 
   *              HA L'IMPRONTA E SE QUESTA E' CORRETTA
   *              
   *              TRUE  -> L'impronta esiste ed è corretta
   *              FALSE -> L'impronta non esiste o non è corretta
   *                
   * RETURN:      boolean
  */       
  public boolean verificaImprontaOggettoFile(String nomeOggettoFile) throws Exception
  {
         A_Oggetti_File aOgg;
         Vector oggetti;
         int conta=0,  size;
         
         oggetti =  aDocumento.getOggettiFile();
         
         size=oggetti.size();
         while (conta!=size) { 
              aOgg = (A_Oggetti_File) oggetti.elementAt(conta++);
  
              if (aOgg.getFileName().equals(nomeOggettoFile))
                  try {
                    return verificaImpronta(aOgg.getFile(),nomeOggettoFile);
                  }
                  catch (Exception e) 
                  {
                    varEnv.disconnectClose();
                    throw new Exception("ManageDocumento::verificaImprontaOggettoFile("+nomeOggettoFile+").\n"+e.getMessage());
                  }
         }
         
         varEnv.disconnectClose();
         throw new Exception("ManageDocumento::verificaImprontaOggettoFile("+nomeOggettoFile+"). Non esiste il file in questo documento o retrieve mancante");
  }  

  // ***************** METODI DI GESTIONE DELLE ACL ***************** //

  /*
   * METHOD:      aggiungiACL(String, String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Inserimento della ACL es: ("GDM","0")
   * 
   * RETURN:      void
  */ 
  public void aggiungiACL(String aclPersonGroup,String aclMask) throws Exception
  {
         try {
           aDocumento.aggiungiACL(aclPersonGroup,aclMask);
         }                  
         catch (Exception e)
         {
           varEnv.disconnectRollback();
           throw new Exception("ManageDocumento::aggiungiACL(@,@)\n"+ e.getMessage());
         }  
  }

  public void aggiungiACL(String aclPersonGroup,String aclMask,String ruolo) throws Exception
  {
         try {
           aDocumento.aggiungiACL(aclPersonGroup,aclMask,ruolo);
         }                  
         catch (Exception e)
         {
           varEnv.disconnectRollback();
           throw new Exception("ManageDocumento::aggiungiACL(@,@,@)\n"+ e.getMessage());
         }  
  }  
  
  /*
   * METHOD:      salvaACL(String)
   * SCOPE:       PROTECTED
   *
   * DESCRIPTION: UTILIZZATA ESCLUSIVAMENTE
   *              DA FINMATICA.
   *              Cicla sul vettore di ACL per
   *              assegnare le competenze aggiuntive
   *              agli utenti
   *              
   *              tipoSalvataggio='U' -> sono in update
   *              tipoSalvataggio='I' -> sono in insert
   * 
   * RETURN:      void
  */  
  protected void salvaACL(String tipoSalvataggio) throws Exception {
          Vector vACL = (Vector)aDocumento.getACL();
          
          //Se sono in modifica del documento, prima di modificare le competenze
          //controllo se ho la comp. di manage
          //if (tipoSalvataggio.equals("U")) {
          if ( (tipoSalvataggio.equals("U")) && (vACL.size()!=0) && !varEnv.getByPassCompetenze())
          {
            UtenteAbilitazione env = new UtenteAbilitazione( varEnv.getUser(), varEnv.getPwd(),null,null);								
            Abilitazioni ab = new Abilitazioni("DOCUMENTI",aDocumento.getIdDocumento(),Global.ABIL_GEST);				
            if ((new GDM_Competenze(varEnv)).verifica_GDM_Compentenza(env,ab)==0) {
               throw new Exception("ManageDocumento::salvaACL() Impossibile aggiornare le competenze, non si possiede la competenza per modificarle");
            }
          }
          
          for(int i=0;i<vACL.size();i++) {
             ACL objACL = (ACL)vACL.get(i);
             
             String user=objACL.getPersonGroup();
             String mask=objACL.getMask();
             String ruolo=objACL.getRuolo();
             try {
               if (mask.equals(Global.NO_ACCESS)) {
                   (new GDM_Competenze(varEnv)).negaDocumento(user,aDocumento.getIdDocumento(),"LUDM",varEnv.getUser(),ruolo);
               }
               else if (mask.equals(Global.COMPLETE_ACCESS)) {
                   (new GDM_Competenze(varEnv)).consentiDocumento(user,aDocumento.getIdDocumento(),"LUDM",varEnv.getUser(),ruolo);
               }
               else if (mask.equals(Global.NORMAL_ACCESS)) {
                   (new GDM_Competenze(varEnv)).consentiDocumento(user,aDocumento.getIdDocumento(),"LU",varEnv.getUser(),ruolo);
                   (new GDM_Competenze(varEnv)).negaDocumento(user,aDocumento.getIdDocumento(),"D",varEnv.getUser());
               }
               else if (mask.equals(Global.READONLY_ACCESS)) {
                   (new GDM_Competenze(varEnv)).consentiDocumento(user,aDocumento.getIdDocumento(),"L",varEnv.getUser(),ruolo);
                   (new GDM_Competenze(varEnv)).negaDocumento(user,aDocumento.getIdDocumento(),"UD",varEnv.getUser(),ruolo);
               }               
             }
             catch (Exception e) {
               throw new Exception("ManageDocumento::salvaACL() per (maschera,utente,ruolo) ("+mask+","+user+","+ruolo+")\n" + e.getMessage());
             }
          }
  }

  // ***************** METODI DI GESTIONE DELLE EXTRA-COMPETENZE ***************** //

  /*
   * METHOD:      aggiungiCompetenza(String, String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Inserimento della ExtraCompetebza es: ("GDM","RI")
   * 
   * RETURN:      void
  */ 
  public void aggiungiCompetenza(String aclPersonGroup,String comp) throws Exception
  {
         try {
           aDocumento.aggiungiExtraCompetenze(aclPersonGroup,comp);
         }                  
         catch (Exception e)
         {
           varEnv.disconnectRollback();
           throw new Exception("ManageDocumento::aggiungiCompetenza()\n"+ e.getMessage());
         }  
  }
  
  /*
   * METHOD:      salvaCompetenze(String)
   * SCOPE:       PROTECTED
   *
   * DESCRIPTION: UTILIZZATA ESCLUSIVAMENTE
   *              DA FINMATICA.
   *              Cicla sulla HashTableSet per
   *              assegnare le competenze extra
   *              agli utenti
   *              
   *              tipoSalvataggio='U' -> sono in update
   *              tipoSalvataggio='I' -> sono in insert
   * 
   * RETURN:      void
  */  
  protected void salvaCompetenze(String tipoSalvataggio) throws Exception {
	  		HashMapSet hmSet = (HashMapSet)aDocumento.getExtraCompetenze();
          
	        //Se sono in modifica del documento, prima di modificare le competenze
	        //controllo se ho la comp. di manage
	        //if (tipoSalvataggio.equals("U")) {
	        if ( (tipoSalvataggio.equals("U")) && (hmSet.size()!=0))
	        {
	            UtenteAbilitazione env = new UtenteAbilitazione( varEnv.getUser(), varEnv.getPwd(),null,null);								
	            Abilitazioni ab = new Abilitazioni("DOCUMENTI",aDocumento.getIdDocumento(),Global.ABIL_GEST);				
	            if ((new GDM_Competenze(varEnv)).verifica_GDM_Compentenza(env,ab)==0) {
	               throw new Exception("ManageDocumento::salvaCompetenze() Impossibile aggiornare le competenze, non si possiede la competenza per modificarle");
	            }
	        }
	        
	        Iterator iHm = hmSet.getHashMap();
	        String sUser=null, sCompetenza=null, sAccesso=null, sRuolo="GDM";
	        
	        try {	        	
		        while (iHm.hasNext()) {
		        	sUser = (String)iHm.next();
		        	
		        	Iterator iHs = hmSet.getHashSet(sUser);
		        	
		        	while (iHs.hasNext()) {
		        		sCompetenza = (String)iHs.next();
		        		sRuolo=sCompetenza.substring(sCompetenza.indexOf("#")+1,sCompetenza.length());		
		        		
		        		//Estraggo le date
		        		String sLeDueDate;
		        		String dataDa, dataA;
		        		
		        		sLeDueDate=sRuolo.substring(sRuolo.indexOf("$")+1);
		        		sRuolo=sRuolo.substring(0,sRuolo.indexOf("$"));
		        		
		        		dataDa=sLeDueDate.substring(0,sLeDueDate.indexOf("&"));
		        		dataA=sLeDueDate.substring(sLeDueDate.indexOf("&")+1);
		        		//Fine Estrazione Date
		        		
		        		sAccesso=sCompetenza.substring(sCompetenza.indexOf("@")+1,sCompetenza.indexOf("@")+2);
		        		sCompetenza=sCompetenza.substring(0,sCompetenza.indexOf("@"));
		        		UtenteAbilitazione uab = new UtenteAbilitazione(sUser,sUser,sRuolo,"");
		        		Abilitazioni ab = new Abilitazioni(varEnv.Global.ABIL_DOC,aDocumento.getIdDocumento(),sCompetenza);		        		
		        		ab.setAccesso(sAccesso);
		        		
		        		if (!dataDa.equals("")) ab.setDataInizio("to_char(to_date('"+dataDa+"','dd/mm/yyyy'),'dd/mm/yyyy')");
		        		if (!dataA.equals("")) ab.setDataFine("to_char(to_date('"+dataA+"','dd/mm/yyyy'),'dd/mm/yyyy')");
		        		
		        		(new GDM_Competenze(varEnv)).si4AssegnaCompetenza(uab,ab);
		        	}
		        }
		     }
             catch (Exception e) {
               throw new Exception("ManageDocumento::salvaCompetenze() per (competenza,utente) ("+sCompetenza+","+sUser+")\n" + e.getMessage());
             }
  }
  
  // ***************** METODI DI VERIFICA DELL'ESISTENZA DEL DOCUMENTO ***************** //  
    
  /*
   * METHOD:      verificaEsistenza(String,String,String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Verifica se esiste un documento per 
   *              Area, Codice Modello, Codice Richiesta
   *              torna l'id_documento se esiste o "" altrimenti
   *                
   * RETURN:      String
  */          
/*  public synchronized static String verificaEsistenza( String ar, 
                                                       String cm, 
                                                       String codric) throws Exception
  {    
         return (new DocUtil(vEnv)).getIdDocumento (ar,cm,codric);
  }*/
  
  /*
   * METHOD:      verificaEsistenza(String,String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Verifica se esiste un documento (sTipoDoc, sCodRic)
   *              torna l'id_documento se esiste o "" altrimenti
   *              
   * RETURN:      String
  */  
/*  public synchronized static String verificaEsistenza( String sTipoDoc, 
                                                       String sCodRic) throws Exception
  {
         return DocUtil.getIdDocumento(sTipoDoc,sCodRic);
  }*/
  
  /*
   * METHOD:      verificaStato(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Restituisce l'ultimo stato del documento
   *              
   * RETURN:      String
  */    
/*  public synchronized static String verificaStato(String idDoc) throws Exception
  {
        return GD4_Status_Documento.verificaStato(idDoc);
  }*/
  
  
  // ***************** METODI DI SET E GET ***************** //
  
  protected void resetDocument(A_Documento aDoc) 
  {
            aDoc.svuotaListaValori();
            aDoc.svuotaListaOggettiFile();
  }
  
  public void settaCodiceRichiesta(String newCodRich)
  {
         aDocumento.setCodiceRichiesta(newCodRich);
  }
    
  public void setUltAggiornamento(String newUltAggiornamento)
  {
         this.ultAggiornamento = newUltAggiornamento;
  }

  public String getUltAggiornamento()
  {
         return this.ultAggiornamento;                 
  }
  
  
  // ***************** METODI ESCLUSIVAMENTE DI HUMMINGBIRD ***************** //    
  
  /*
   * METHOD:      pantareiLogin()
   * SCOPE:       PROTECTED
   *
   * DESCRIPTION: Effettua il login ad HummingBird tramite il ws di pantarei
   * 
   * RETURN:      void
  */  
  protected void pantareiLogin() throws Exception
  {             
            try {
              DMPantaReiStub dmPantaStub = new DMPantaReiStub(varEnv.Global.WEB_HOST_SERVICE_PANTA);
              token = dmPantaStub.Login(varEnv.getLibrary(),varEnv.getUser(),varEnv.getPwd());
            }
            catch (Exception e)
            {
              throw new Exception("ManageDocumento::pantareiLogin()\n"+varEnv.getLibrary()+"-"+varEnv.getUser()+"-"+varEnv.getPwd()+"n\""+e.getMessage());
            }

            if (((LoginRet)token).getLngErrNumber().longValue()!=0) 
                 throw new Exception("ManageDocumento::pantareiLogin() Errore Login: " + varEnv.getLibrary()+"-"+varEnv.getUser()+"-"+varEnv.getPwd()+ "\n" +
                                     ((LoginRet)token).getLngErrNumber() + " - " + 
                                     ((LoginRet)token).getStrErrString());            
   
  }
    

  // ***************** METODI PRIVATI ***************** //

  protected void gestisciImpronte() throws Exception {
	  	    //Controllo se l'imponta è abilitata
	  		String check;
	  		LookUpDMTable lookUp = (new LookUpDMTable(varEnv));
	  		
	  		check=lookUp.lookUpParametro("GENERA_IMPRONTA","@STANDARD");
	  		if (check==null) check="N";
	  		
	  		if (check.equals("N")) return;
	  
	  		GD4_Documento gd4Doc = new GD4_Documento();
		    
	  		try {			  		  
	  		  gd4Doc.inizializzaDati(varEnv);
	  		  gd4Doc.setIdDocumento(aDocumento.getIdDocumento());
	  		  gd4Doc.retrieve(true,false,true,false,null);
		    }  
		    catch (Exception e) {
  	          throw new Exception("ManageDocumento::gestisciImpronte - Errore in retrieve elenco allegati.\nErrore: "+e.getMessage());
		    }
		    
	        Vector<GD4_Oggetti_File> vobjFile = gd4Doc.getOggettiFile();
	        String listaFile="";
	       
	        try {		        
		        for(int i=0;i<vobjFile.size();i++) {		        			        	
		        	GD4_Oggetti_File objFile=vobjFile.get(i);	
		        	
		        	if (!bIsOggettoFileModificato(objFile.getFileName())) continue;
		        	
		        	listaFile+=objFile.getFileName();
		        			        
		        	if (i!=vobjFile.size()-1) listaFile+="@_#_@";
		        }
	        }
		    catch (Exception e) {
		    	throw new Exception("ManageDocumento::gestisciImpronte - Errore in costruzione elenco allegati.\nErrore: "+e.getMessage());
		    }
		    		    
	        ImprontaAllegati iAll = new ImprontaAllegati(aDocumento.getIdDocumento(),varEnv.getDbOp(),listaFile,false);
	       
	        //Prima cancello i vecchi file rinominati
	        for(int i=0;i<aDocumento.getVElencoNomiAllegatiRinominati().size();i++) {
	        	try {
		          iAll.cancellaImpronta(aDocumento.getVElencoNomiAllegatiRinominati().get(i),false);
		        }
			    catch (Exception e) {
			    	throw new Exception("ManageDocumento::gestisciImpronte - Errore in cancellazione impronta per file ("+aDocumento.getVElencoNomiAllegatiRinominati().get(i)+").\nErrore: "+e.getMessage());
			    }
	        }
	        
	        for(int i=0;i<vobjFile.size();i++) {
	        	GD4_Oggetti_File objFile=vobjFile.get(i);
	        	
	        	if (!bIsOggettoFileModificato(objFile.getFileName())) continue;
	        	
	        	try {
	        	  iAll.cancellaImpronta(objFile.getFileName(),false);
		        }
			    catch (Exception e) {
			    	throw new Exception("ManageDocumento::gestisciImpronte - Errore in cancellazione impronta per file ("+objFile.getFileName()+").\nErrore: "+e.getMessage());
			    }			    			   		    
			    
			    try {
	        	  iAll.setFile(objFile.getFileName(),(InputStream)objFile.getFile(false));	        	  
			    }
			    catch (NullPointerException e) {
			       throw new Exception("ManageDocumento::gestisciImpronte - Errore in set impronta per file ("+objFile.getFileName()+").\nErrore: InputStream del file non presente o nullo");
			    }
			    catch (Exception e) {
			    	throw new Exception("ManageDocumento::gestisciImpronte - Errore in set impronta per file ("+objFile.getFileName()+").\nErrore: "+e.getMessage());
			    }
			    
			    try {
		          iAll.generaImpronta(objFile.getFileName());
		        }
			    catch (Exception e) {
			    	throw new Exception("ManageDocumento::gestisciImpronte - Errore in generazione dell'impronta per il file ("+objFile.getFileName()+").\nErrore: "+e.getMessage());
			    }			    
	        }
	       // GD4_Documento gd4Doc3=null;
	       // gd4Doc3.retrieve(true,false,true,false,null);
	        /*try {
	          iAll.generaImpronte();
	        }
		    catch (Exception e) {
		    	throw new Exception("ManageDocumento::gestisciImpronte - Errore in generazione delle impronte per i file modificati.\nErrore: "+e.getMessage());
		    }*/
  }
  
  private boolean bIsOggettoFileModificato(String fileName) {
	  	  for(int i=0;i<aDocumento.getVElencoNomiAllegatiModificati().size();i++) 
	  		  if (aDocumento.getVElencoNomiAllegatiModificati().get(i).equals(fileName)) return true;
	  	  
	  	  return false;
  }
    
  /*
   * METHOD:      verificaEsistenzaImpronta(String)
   * SCOPE:       PRIVATE 
  */  
  private boolean verificaEsistenzaImpronta(String nomeFile) throws Exception
  {
         String nomeFileImpronta = Global.replaceAll(nomeFile,".","_")+".SYS_HASH";
         String idOggettoFileImpronta;

         try {

            idOggettoFileImpronta = (new LookUpDMTable(varEnv)).lookUpOggettoByName(nomeFileImpronta, aDocumento.getIdDocumento());

            // Se il file impronta esiste, controllo se si tratta
            // del file impronta relativo al "filename"            
            if (!nomeFileImpronta.equals(idOggettoFileImpronta)) 
            {
               String idOggettoFile = (new LookUpDMTable(varEnv)).lookUpOggettoByName(nomeFile, aDocumento.getIdDocumento());

               return (idOggettoFile.equals((new LookUpDMTable(varEnv)).lookUpOggettoPadre(idOggettoFileImpronta)));
             
            }
            else return false;
         }
         catch (Exception e) 
         {           
            throw new Exception("ManageDocumento::verificaEsistenzaImpronta(@) - lookUp\n"+e.getMessage());
         }        
  }

  /*
   * METHOD:      verificaImpronta(Object,String)
   * SCOPE:       PRIVATE 
  */
  private boolean verificaImpronta(Object file,String nomeFile) throws Exception
  {
       InputStream fileOrigine=(InputStream)file;
       String sNomeImpronta=Global.replaceAll(nomeFile,".","_")+".SYS_HASH";
       A_Oggetti_File aOgg;
       Vector oggetti;
       int conta=0,  size;
       
       oggetti =  aDocumento.getOggettiFile();

       size=oggetti.size();
       String idOggettoFile = (new LookUpDMTable(varEnv)).lookUpOggettoByName(nomeFile, aDocumento.getIdDocumento());
       
       while (conta!=size) { 
            aOgg = (A_Oggetti_File) oggetti.elementAt(conta++);

            String idOggettoFileImpronta = (new LookUpDMTable(varEnv)).lookUpOggettoByNameEOggettoPadre(aOgg.getFileName(), aDocumento.getIdDocumento(),idOggettoFile);

            if ( !aOgg.getFileName().equals(idOggettoFileImpronta) ) 
               try {
                return compareImpronta(fileOrigine, aOgg.getFile());
               }
               catch(Exception e) 
               {                 
                 throw new Exception("ManageDocumento::verificaImpronta(file,"+nomeFile+"). Errore in compareImpronta"+e.getMessage());
               }
       }
              
       throw new Exception("ManageDocumento::verificaImpronta(file,"+nomeFile+"). Non esiste l'impronta in questo documento");
  }

  /*
   * METHOD:      compareImpronta(Object,Object)
   * SCOPE:       PRIVATE 
  */
  private boolean compareImpronta(Object file, Object fimpronta) throws Exception
  {
          InputStream fileOrigine=(InputStream)file;
          InputStream fileImpronta=(InputStream)fimpronta;
     
          byte b[]=null;

          try {          
            b = Global.getBytesToEndOfStream(fileImpronta);
          }
          catch(Exception e) 
          {               
             throw new Exception("ManageDocumento::compareImpronta(@,@) - Problemi nella lettura dell'impronta\n"+e.getMessage());          
          }
          
          Impronta impronta = new Impronta("SHA1");          
          
          return impronta.compareTo(fileOrigine, "SHA1", b);
  }

  public void setConservazione(String sConservazione) {
	      try {
	        aDocumento.setConservazione(sConservazione);
	      }
	      catch (NullPointerException e) {
	    	 
	      }
  }

   /**
    * Metodo che setta la colonna CONSERVAZIONE sulla tabella
    * documenti con il valore passato come parametro di input 
   */   
   public void setArchiviazione(String sArchiviazione) {
	      try {
	        aDocumento.setArchiviazione(sArchiviazione);
	      }
	      catch (NullPointerException e) {
	    	 
	      }
   }  
   
   protected long getNumAllegati() {
	      long nAllegati=0;
	      
	      Vector objFile = aDocumento.getOggettiFile();
	      	      
	      for(int i=0;i<objFile.size();i++) {
	    	  A_Oggetti_File aObjFile = (A_Oggetti_File)objFile.get(i);	    	  	    	 
	    	  
	    	  if (Long.parseLong(aObjFile.getIdFormato())>=0 && aObjFile.getDacancellare().equals("N"))
	    		  nAllegati++;
	      }
	      
	      return nAllegati;
   }
   

   protected void manageSysIntegration() throws Exception {
	         SysIntegrationPending syp = new SysIntegrationPending(Long.parseLong(aDocumento.getIdDocumento()),varEnv);		
	         
	         syp.insertPending();
	   		
   }

   public String getCodeError() {
		  return codeError;
   }
		
   public String getDescrCodeError() {
		  return descrCodeError;
   }  

   public String getCodeErrorPostSave() {
		  return codeErrorPostSave;
   }
		
   public String getDescrCodeErrorPostSave() {
		  return descrCodeErrorPostSave;
   }   
   
   protected void settaIdDoc(String idDoc) {
   			 aDocumento.setIdDocumento(idDoc);
   }
   
   public void setSkipReindexFullTextField(boolean skipReindexFullTextField) {
		  this.skipReindexFullTextField = skipReindexFullTextField;
   }  
   
   public void setbDontRepeatExistsRif(boolean bDontRepeatExistsRif) {
	      this.bDontRepeatExistsRif = bDontRepeatExistsRif;
	      aDocumento.setbDontRepeatExistsRif(bDontRepeatExistsRif)	;   
   } 
   
   public void creaVersione(boolean creaVersione) {
		  this.creaVersione = creaVersione;
   }
   public long getUltimaVersione() {
		return ultimaVersione;
   }
}