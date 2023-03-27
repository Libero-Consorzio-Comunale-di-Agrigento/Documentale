package it.finmatica.dmServer;

/*
 * GESTIONE DEI DOCUMENTI
 * NEL DM DI FINMATICA
 * 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   16/09/2005
 * 
 * */

import java.sql.*; 
import java.util.*;
import java.util.Date;
import java.io.*;

import it.finmatica.jfc.dbUtil.*;
import it.finmatica.jfc.io.LetturaScritturaFileFS;
import it.finmatica.dmServer.dbEngine.struct.DbOpSetParameterBuffer;
import it.finmatica.dmServer.util.*;
     
public class GD4_Documento extends A_Documento
{     
  // variabili private  
  private Environment varEnv; 
 
  


  private GD4_GestoreOggettiFile_FS gestObjFile;    
  
  private String nameHorizontalTable="";
    
  private ElapsedTime elpsTime;       
  
  private String codeError        = Global.CODERROR_NOT_DEFINED;    
  
  private Vector listaIdFileToLog = new Vector();

  private String acronimoAree;
  
  // ***************** METODI DI INIZIALIZZAZIONE ***************** // 
 
  /*
   * METHODS:      Constructor
   *
   * 
   * RETURN:      none
  */  
  public GD4_Documento() {}

  public GD4_Documento(A_Documento oDoc) throws Exception {
         this();
         
         try {
           inizializzaDati(varEnv);
         }
         catch (Exception e) {          
           throw new Exception("GD4_Documento::Costructor\n"+e.getMessage());
         }
         
         this.setTipoDocumento(oDoc.getTipoDocumento());
         this.setLibreria(oDoc.getLibreria());
         this.setValori(oDoc.getValori());
         this.setOggettiFile(oDoc.getOggettiFile());
         this.setStatusDocumento(oDoc.getStatusDocumento());   
  }

  /*
   * METHOD:      inizializzaDati(Object, Object)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: inizializza le variabili di connessione
   *              e di documento
   * 
   * RETURN:      void
  */  
  public void inizializzaDati(Object vUtente) throws Exception
  {
         this.inizializzaDati((Environment)vUtente);
  }

  /*
   * METHOD:      inizializzaDati(IDbOperationSQL, Environment)
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: inizializza le variabili di connessione
   *              e di documento
   * 
   * RETURN:      void
  */  
  private void inizializzaDati(Environment vUtente) throws Exception
  {
         // Crea la libreria del DM da utilizzare
         try {
           libreria = (A_Libreria)Class.forName(vUtente.Global.PACKAGE + 
                                                "." + vUtente.Global.DM + 
                                                "_" + vUtente.Global.LIBRERIA).newInstance();
         }
         catch (Exception e) {
               throw new Exception("GD4_Documento::inizializzaDati - "+
                                   "Non riesco a creare l'oggetto di Classe: " + 
                                   vUtente.Global.PACKAGE + "." + vUtente.Global.DM + "_" + 
                                   vUtente.Global.LIBRERIA+"\n");
         }

         // Crea il tipo documento del DM da utilizzare
         try {
           tipoDocumento = (A_Tipo_Documento)Class.forName(vUtente.Global.PACKAGE + 
                                                           "." + vUtente.Global.DM + 
                                                           "_" + vUtente.Global.TIPODOC).newInstance();
         }
         catch (Exception e) {
               throw new Exception("GD4_Documento::inizializzaDati - "+
                                   "Non riesco a creare l'oggetto di Classe: " + 
                                   vUtente.Global.PACKAGE + "." + vUtente.Global.DM + "_" + 
                                   vUtente.Global.TIPODOC+"\n");
         }
         
         // Crea il tipo documento del DM da utilizzare
         try {
           statusDocumento = (A_Status_Documento)Class.forName(vUtente.Global.PACKAGE + "." + 
                                                               vUtente.Global.DM + "_" + 
                                                               vUtente.Global.STATIDOC).newInstance();          
           
         }
         catch (Exception e) {
               throw new Exception("GD4_Documento::inizializzaDati - "+
                                   "Non riesco a creare l'oggetto di Classe: " + 
                                   vUtente.Global.PACKAGE + "." + vUtente.Global.DM + "_" + 
                                   vUtente.Global.STATIDOC+"\n");
         }

         // Crea la lista (vuota) dei valori del documento
         valori = new Vector();
         // Crea la lista (vuota) degli ACL del documento
         vACL   = new Vector();
         // Crea la lista (vuota) degli oggetti file del documento
         oggettiFile = new Vector();
         
         libreria.inizializza(vUtente);
         tipoDocumento.inizializzaDati(vUtente);         
    
         elpsTime = new ElapsedTime("GD4_DOCUMENTO",vUtente);
         
         hashMapExtraCompetenze = new HashMapSet();
         
         this.varEnv = vUtente;
         
         gestObjFile = new GD4_GestoreOggettiFile_FS(varEnv);
         
        
  }
 
  // ***************** METODI DI GESTIONE DEI DOCUMENTI ***************** //
  
  /*
   * METHOD:      insertDocument()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Inserisce un documento nella tabella documenti
   *              ed i rispettivi valori ed oggetti file
   * 
   * RETURN:      boolean
  */  
  public boolean insertDocument(String stato) throws Exception
  {      
         
	     //PER ADESSO NON VIENE GESTITO DA MODULISTICA E 
	     //IL METODO SPRECA MOLTO TEMPO...SE DEVE ESSERE 
	     //RIPRISTINATO DEVE ESSERE RIVISTO
	     /*try {
              verifyMandatoryCampi("INSERT");
          }
          catch (Exception e) 
         {                    
              throw new Exception("GD4_Documento::insertDocument() verifyMandatoryCampi\n"+e.getMessage());
         }*/
                                         
         try {
          if (insert( stato)) {
             //Inserisco il Log del documento        	         		         	
	         try {
	        	 dmALog = new DMActivity_Log(idDocumento,Global.TYPE_AZIONE_CREA,varEnv);
	        	 dmALog.setTypeLog(getTipoDocumento().getTipoLog());
	        	 dmALog.setTypeLogFile(getTipoDocumento().getTipoLogFile());
	        	 dmALog.creaVersione(creaVersione);
	        	 dmALog.insertActivityLog();
	        	 ultimaVersione=dmALog.getUltimaVersione();
	         }
	         catch (Exception e) 
	         {                     
	             throw new Exception("Insert ActivityLog\n"+e.getMessage());
	         }  
        	 
             if (salvaValori("INSERT")) {
            	 if (bAllegatiTempModulistica) sistemaOggettiFileTemp();
                 salvaOggettiFile("INSERT");                       	                                                                  
             }
          }
         }         
         catch (DocumentException Dexp) {	
     	 	codeErrorSaveDoc=Dexp.getCode();
     	 	descrErrorSaveDoc=Dexp.getMessage();
     	 	throw new Exception(Dexp.getMessage());
		 }
         catch (Exception e) 
         {                
               throw new Exception(e.getMessage());
         }
         
         //Funzione di f_log del documento
         dmALog.f_log_documento();
         
         return true;      
  }

  /*
   * METHOD:      updateDocument()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Aggiorna un documento nella tabella documenti
   *              ed i rispettivi valori ed oggetti file 
   *              se questi sono stati modificati
   *              
   * RETURN:      boolean
  */  
  public boolean updateDocument()  throws  Exception
  {
	  	 //PER ADESSO NON VIENE GESTITO DA MODULISTICA E 
	     //IL METODO SPRECA MOLTO TEMPO...SE DEVE ESSERE 
	     //RIPRISTINATO DEVE ESSERE RIVISTO
	     /*
         try {
              verifyMandatoryCampi("UPDATE");
         }
         catch (Exception e) 
         {                         
               throw new Exception("GD4_Documento::updateDocument() verifyMandatoryCampi\n"+e.getMessage());
         }*/
         
         //Inserisco il Log del documento
         if (!getTipoDocumento().getTipoLog().equals(Global.TYPE_NO_LOG)) { 
        	 try {
        		 if (dmALog==null) {        			         		 
	        		 dmALog = new DMActivity_Log(idDocumento,Global.TYPE_AZIONE_MODIFICA,varEnv);
	        		 dmALog.setTypeLog(getTipoDocumento().getTipoLog());
	        		 dmALog.setTypeLogFile(getTipoDocumento().getTipoLogFile());
	        		 dmALog.creaVersione(creaVersione);
	        		 dmALog.insertActivityLog();
	        		 ultimaVersione=dmALog.getUltimaVersione();
        		 }
        	 }
         	 catch (Exception e) 
         	 {                     
                throw new Exception("GD4_Documento::updateDocument() Insert ActivityLog\n"+e.getMessage());
         	 }
         } 
                  
         try {
        	
            if (update()) { 
               if (salvaValori("UPDATE")){
            	  if (bAllegatiTempModulistica) sistemaOggettiFileTemp();
                  if (salvaOggettiFile("UPDATE")) {                                            
                       return true;
                  }   
               }
            } 
         }
         catch (DocumentException Dexp) {	
        	 	codeErrorSaveDoc=Dexp.getCode();
        	 	descrErrorSaveDoc=Dexp.getMessage();
        	 	throw new Exception("GD4_Documento::updateDocument() update-salvaValori-salvaOggettiFile\n"+Dexp.getMessage());
		 }
         catch (Exception e) 
         {                     
                throw new Exception("GD4_Documento::updateDocument() update-salvaValori-salvaOggettiFile\n"+e.getMessage());
         }
         
         //Funzione di f_log del documento
         dmALog.f_log_documento();
         
         return true;
  }
  
  public boolean saveVersion(long lVersion, boolean bNonRipetereUguali, Date dataAggiornamentoLog) throws  Exception
  {
         
         //Inserisco il Log del documento versionato (la testata)       
    	 try {
    		 if (dmALog==null) {        			         		 
        		 dmALog = new DMActivity_Log(idDocumento,Global.TYPE_AZIONE_REVISIONE,varEnv);
        		 dmALog.setTypeLog(Global.TYPE_MAXVAL_LOG);
        		 dmALog.setTypeLogFile("Y");
        		 dmALog.setLVersione(lVersion);
        		 dmALog.setDataLogCustom(dataAggiornamentoLog);
        		 dmALog.insertActivityLog();
    		 }
    	 }
     	 catch (Exception e) 
     	 {                     
            throw new Exception("GD4_Documento::updateDocument() Insert ActivityLog\n"+e.getMessage());
     	 }
        
                   
         try {             	 
        	 nameHorizontalTable=(new LookUpDMTable(varEnv).lookUpAliasOrizzontalTable(this.getIdDocumento()));
        	 
        	 //Parte dei Valori
        	 Vector<String> vCampi = new Vector<String>();
        	 Vector<String> vTipoCampi = new Vector<String>();
        	 new LookUpDMTable(varEnv).lookUpElencoCampiTabellaHoriz(nameHorizontalTable,vCampi,vTipoCampi);
        	 
        	 if (nameHorizontalTable.equals("") || nameHorizontalTable.equals("X_X"))
        		 dmALog.insertAllVaLog(idDocumento,vCampi);
        	 else
        		 dmALog.insertAllVaLogHorizontal(idDocumento,nameHorizontalTable,vCampi,vTipoCampi,bNonRipetereUguali);
        	 //Fine parte dei valori
        	 
        	 //Parte degli Oggetti File
        	 dmALog.insertAllOgfiLog(idDocumento,(new Vector()),false,true,bNonRipetereUguali);
        	 //Fine parte degli Oggetti File
        	 
        	 //Gestisco l'eventuale FS
        	 dmALog.scriviLogFs();
        	 //Gestisco l'eventuale FS        	 
         }
         catch (Exception e) 
         {                     
                throw new Exception("GD4_Documento::saveVersion("+lVersion+") salvaValoriLog-salvaOggettiFileLog\n"+e.getMessage());
         }
         
         //Funzione di f_log del documento
         dmALog.f_log_documento();
         
         return true;
  }  

  /*
   * METHOD:      deleteDocument()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Elimina il documento logicamente dal repository
   *              
   * RETURN:      boolean
  */  
  public boolean deleteDocument() throws Exception 
  {
         try {
           return this.cambiaStatoDocumento(Global.STATO_ANNULLATO);
         }
         catch (Exception e) {              
           throw new Exception("GD4_Documento::deleteDocument()\n"+e.getMessage());
         }
  }
 
  /*
   * METHOD:      deleteDbDocument()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Elimina il documento fisicamente dal DB
   *              @deprecated Please now use deleteDocument()
   *              @see deleteDocument()
   *              
   * RETURN:      boolean
  */
  public boolean deleteDbDocument() throws Exception 
  {
         if (this.getIdDocumento().equals("0"))
             throw new Exception("GD4_Documento::delete() IdDocumento richiesto");

         try {
           IDbOperationSQL dbOpSql = varEnv.getDbOp();
          
           StringBuffer sStm = new StringBuffer();
           
           sStm.append("delete documenti ");
           sStm.append("where id_documento = "+ this.getIdDocumento());

           dbOpSql.setStatement(sStm.toString());

           dbOpSql.execute();
         }    
         catch (Exception e) {               
               throw new Exception("GD4_Documento::delete()\n" + e.getMessage());
         }
         
         return true;
  }

  /*
   * METHOD:      recoveryDocument()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Recupera il documento logicamente dal repository
   *              
   * RETURN:      boolean
  */    
  public boolean recoveryDocument() throws Exception 
  {
         try {
           return this.cambiaStatoDocumento(Global.STATO_SOSPESO);
         }
         catch (Exception e) {                
           throw new Exception("GD4_Documento::recoveryDocument()\n"+e.getMessage());
         }
  }
  
  public boolean cambiaStatoDocumento(String newStato) throws Exception
  {
	     return cambiaStatoDocumento(newStato,false);
  }
  
 /*
   * METHOD:      cambiaStatoDocumento(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Resgistra il nuovo stato per il documento  
   *              
   * RETURN:      boolean
  */ 
  public boolean cambiaStatoDocumento(String newStato, boolean bSaltaUpdateDoc) throws Exception
  {
         this.getStatusDocumento().setStato(newStato);
         this.getStatusDocumento().setBSaltaUpdateDoc(bSaltaUpdateDoc);
         
         try {        	 
             return this.getStatusDocumento().registraStato();
         }
         catch (Exception e)
         {
            throw new Exception("GD4_Documento::cambiaStatoDocumento()\n"+e.getMessage());
         }  
  }
 
  /*
   * METHOD:      retrieve(boolean, boolean, boolean, boolean)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Carica un documento dal Database.
   *              Insieme al documento vengono caricati:
   *              libreria, oggetti file, valori,
   *              campi documento, tipo documento
   *              a seconda dei "deviatori" booleani
   *              attivati o meno
   *              
   *              Il deviatore flagLog serve per caricare i valori
   *              più recenti dalla valori_log e non dalla valori
   * RETURN:      boolean
  */ 
  public boolean retrieve(boolean flagTipoDocumento, 
                          boolean flagValori,
                          boolean flagOggettiFile,
                          boolean flagLog,
                          String  idLog) throws Exception 
  {
         
	     ElapsedTime eT = new ElapsedTime("GD4_DOCUMENTO",varEnv);
	     
	     if (this.getIdDocumento().equals("0")) return false;
               
         try {
           
           eT.start("********** ACCEDI DOCUMENTO ***************","");
           
           IDbOperationSQL dbOp = varEnv.getDbOp();
           StringBuffer sStm = new StringBuffer();
           
           if (bLock) {
        	   try {
        		   sStm.append("select * from documenti where id_documento = " + this.getIdDocumento()+"  for update "+typeLock);
        	   
        		   dbOp.setStatement(sStm.toString());
               
        		   dbOp.execute(); 
        	   }
        	   catch (Exception e) {
        		   throw new Exception("GD4_Documento::retrieve() con lock: Errore nell'effettuare il lock sulla riga del documento.\nErrore:  "+e.getMessage()); 
        	   }
        	   /*System.out.println("CHIAMATA ACCEDI CON LOCK PER ID_DOCUMENTO = "+this.getIdDocumento());        	   
        	   System.out.println("STACK: ");
        	   System.out.println(CallStackUtil.getCallStackAsString());*/
        	   
        	   nameHorizontalTable=(new LookUpDMTable(varEnv).lookUpAliasOrizzontalTable(this.getIdDocumento()));
        	   
        	   if (!nameHorizontalTable.equals("") && !nameHorizontalTable.equals("X_X")) {
        		   try {
        			   sStm = new StringBuffer();
            		   sStm.append("select * from "+nameHorizontalTable+" where id_documento = " + this.getIdDocumento()+"  for update "+typeLock);
            	   
            		   dbOp.setStatement(sStm.toString());
                   
            		   dbOp.execute(); 
            	   }
            	   catch (Exception e) {
            		   throw new Exception("GD4_Documento::retrieve() con lock: Errore nell'effettuare il lock sulla riga della tabella orizzontale ("+nameHorizontalTable+").\nErrore: "+e.getMessage()); 
            	   }
        	   }
           }
                   	 
           sStm = new StringBuffer();
          

           sStm.append("select documenti.id_libreria,documenti.id_tipodoc, to_char(documenti.data_aggiornamento, 'YYYYMMDDHH24MISS'),documenti.codice_richiesta,id_documento_padre,documenti.area,conservazione,archiviazione, ");
           sStm.append(" decode(aree_path.ID_PATH_AREE_FILE,null,  nvl(aree.path_file,''),  nvl(aree_path.path_file,'')), aree.AREA || '.' || tipi_documento.NOME || '.' || documenti.CODICE_RICHIESTA, ");
           sStm.append(" TIPI_DOCUMENTO.ACRONIMO_MODELLO || '/' || to_char(trunc(DOCUMENTI.ID_DOCUMENTO/1000)) || '/' || DOCUMENTI.ID_DOCUMENTO cartellaFile, ");
           sStm.append(" nvl(COMPETENZE_ALLEGATI,'N') compall, aree.acronimo acrAree, nvl(aree.path_file,'') pathFileAreaAree ");
           sStm.append(" from documenti, aree, tipi_documento, aree_path  ");
           sStm.append(" where documenti.id_documento = " + this.getIdDocumento()+" and documenti.area=aree.area and");
 	       sStm.append(" documenti.id_tipodoc=tipi_documento.ID_TIPODOC and ");
           sStm.append(" aree.ID_PATH_AREE =  aree_path.ID_PATH_AREE_FILE  (+) ");
           /*if (bLock) {
        	   sStm.append(" for update nowait");*/
        	   
        	   /*System.out.println("CHIAMATA ACCEDI CON LOCK PER ID_DOCUMENTO = "+this.getIdDocumento());        	   
        	   System.out.println("STACK: ");
        	   System.out.println(CallStackUtil.getCallStackAsString());*/
          // }
        	   
           
           dbOp.setStatement(sStm.toString());
                     
           dbOp.execute();         

           ResultSet rst = dbOp.getRstSet();

           if (rst.next()) {
              String idLib=rst.getString(1);
              String idTipoDoc=rst.getString(2);
              String data=rst.getString(3);              
              this.setCodiceRichiesta(rst.getString(4));              
              idDocumentoPadre=rst.getString(5);
              this.setArea(rst.getString(6));
              this.setConservazione(rst.getString(7));
              this.setArchiviazione(rst.getString(8)); 
              pathFileArea=rst.getString(9);
              pathFileAreaAree=rst.getString("pathFileAreaAree");
              arcmcr=rst.getString("cartellaFile");
              acronimoAree=rst.getString("acrAree");
              
              if (rst.getString("compall").equals("N"))
            	  competenzeAllegati = false;
              else
            	  competenzeAllegati = true;
              
              gestObjFile.setPathFileArea(pathFileArea);
              gestObjFile.setArcmcr(arcmcr);
              gestObjFile.setIdDoc(this.getIdDocumento());
              
              
              //Carico la data di creazione del documento             
              DMActivity_Log dmALog = new DMActivity_Log(Integer.parseInt(this.getIdDocumento()),Global.TYPE_AZIONE_CREA,varEnv);
              dataCreazione = dmALog.getDataActivityLog();
              lastIdLog= dmALog.retrieveLastActivityLog();
           
              // Carico l'oggetto libreria con i dati presi dal DB
              this.getLibreria().setIdLibreria(idLib);
              try {
                this.getLibreria().retrieve();
               
              }
              catch (Exception e) {
                throw new Exception("GD4_Documento::retrieve() - Retrieve Libreria\n" + e.getMessage());
              }

              // SEZIONE TIPO DOCUMENTO
              if (flagTipoDocumento) {
                 // Carico l'oggetto tipoDocumento 
                 this.getTipoDocumento().setIdTipodoc(idTipoDoc);
                 try {
                   this.getTipoDocumento().retrieve(true);
                 }
                 catch (Exception e) {
                   throw new Exception("GD4_Documento::retrieve() - Retrieve Tipo Documento\n" + e.getMessage());
                 }

                  //GESTIONE RECUPERO INFO SU GESTIONE ALLEGATI
                  if (!this.getTipoDocumento().getCompetenzeAllegati().equals("N") && !this.varEnv.getByPassCompetenze()) {
                      sStm = new StringBuffer("SELECT ");
                      sStm.append(" GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI', '"+ this.getIdDocumento()+"', 'LA', '"+varEnv.getUser()+"', F_TRASLA_RUOLO('"+varEnv.getUser()+"','GDMWEB','GDMWEB')) letturaAllegati, ");
                      sStm.append(" GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI', '"+ this.getIdDocumento()+"', 'UA', '"+varEnv.getUser()+"', F_TRASLA_RUOLO('"+varEnv.getUser()+"','GDMWEB','GDMWEB')) aggiornaAllegati, ");
                      sStm.append(" GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI', '"+ this.getIdDocumento()+"', 'DA', '"+varEnv.getUser()+"', F_TRASLA_RUOLO('"+varEnv.getUser()+"','GDMWEB','GDMWEB')) cancellaAllegati ");
                      sStm.append(" FROM DUAL");

                      try {
                          dbOp.setStatement(sStm.toString());

                          dbOp.execute();

                          rst = dbOp.getRstSet();

                          rst.next();

                          letturaAllegati=rst.getInt(1);
                          modificaAllegati=rst.getInt(2);
                          cancellaAllegati=rst.getInt(3);
                      }
                      catch (Exception e) {
                          throw new Exception("GD4_Documento::retrieve() - Errore in Retrieve Competenze Allegati.\nSQL="+sStm.toString()+"\nErrore: " + e.getMessage());
                      }
                  }

                  if (this.varEnv.getByPassCompetenze()) {
                      letturaAllegati=1;
                      modificaAllegati=1;
                      cancellaAllegati=1;
                  }
              }
			  /*	
			   * System.out.println("letturaAllegati->"+letturaAllegati);
				System.out.println("modificaAllegati->"+modificaAllegati);
				System.out.println("cancellaAllegati->"+cancellaAllegati);
			  */
              //Data Ultimo aggiornamento 
              this.setDataAggiornamento(data);
              
              // SEZIONE VALORI
              
              if (flagValori) {
                 // Carico la lista dei valori 
                 try {
                   if (flagLog) {
                	   if (this.getTipoDocumento().getIsHorizontalModel()==0)
                		   retrieveAllValoriLog(idLog);
                	   else
                		   retrieveAllValoriLogH(idLog);
                   }
                   else {
                	   if (this.getTipoDocumento().getIsHorizontalModel()==0)
                		   retrieveAllValori();
                	   else
                		   retrieveAllValoriH();
                   }
                 }
                 catch (Exception e) {
                   codeError=Global.CODERROR_ACCESS_VALORI;
                   throw new Exception("GD4_Documento::retrieve() - Retrieve Tutti i Valori\n" + e.getMessage());
                 }
              }                           
              
              try {
            	 //if (!flagLog)
                    retrieveAllOggettiFile(flagOggettiFile,flagLog,idLog);
              }
              catch (Exception e) {
            	   codeError=Global.CODERROR_ACCESS_OGGETTIFILE;
                   throw new Exception("GD4_Documento::retrieve() - Retrieve Tutti gli Oggetti File\n" + e.getMessage());
              }
                            
              // Retrieve Riferimenti
               try {
            	 if (!flagLog)
            		 retrieveRiferimenti();
               }
               catch (Exception e) {
                 throw new Exception("GD4_Documento::retrieve() - Retrieve Tutti i riferimenti\n" + e.getMessage());
               }
               
               // Retrieve RiferimentiFrom
               try {
            	 if (!flagLog)            	   
                     retrieveRiferimentiFrom();
               }
               catch (Exception e) {            	 
                 throw new Exception("GD4_Documento::retrieve() - Retrieve Tutti i riferimenti di cui sono il documento riferito\n" + e.getMessage());
               }
                                          
               eT.stop();
               
               return true;
           }
           else {
        	  codeError=Global.CODERROR_ACCESS_TBLDOCUMENTI;
              throw new Exception("GD4_Documento::retrieve() -> Select fallita per idDocumento: " + 
                                  this.getIdDocumento());                   
           }                      
         }
         catch (Exception e) {                              
               throw new Exception("GD4_Documento::retrieve()\n" + e.getMessage());
         }
  }

  /*
   * METHOD:      retrieveAbstract()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Carica un documento dal Database.
   *              Insieme al documento vengono caricati:
   *              libreria, oggetti file, valori,
   *              campi documento, tipo documento
   *              a seconda dei "deviatori" booleani
   *              attivati o meno
   *              
   * RETURN:      boolean
  */ 
  public boolean retrieveAbstract() throws Exception 
  {     
         if (this.getIdDocumento().equals("0")) return false;
         
         IDbOperationSQL dbOp = null;
         try {

           dbOp = varEnv.getDbOp();
          
           StringBuffer sStm = new StringBuffer();

           sStm.append("select id_tipodoc , to_char(data_aggiornamento, 'YYYYMMDDHH24MISS'),id_documento_padre from documenti");
           sStm.append(" where id_documento = " + this.getIdDocumento());

           dbOp.setStatement(sStm.toString());

           elpsTime.start("Retrieve da tabella Documenti",sStm.toString());
           dbOp.execute();
           elpsTime.stop();

           ResultSet rst = dbOp.getRstSet();

           if (rst.next()) {
               String idTipoDoc=rst.getString(1);
               String data=rst.getString(2);
               idDocumentoPadre=rst.getString(3);
               
              
               
               // Riempio l'oggetto tipoDocumento con dati obbligatori presi dal DB
               this.getTipoDocumento().setIdTipodoc(idTipoDoc);
               this.setDataAggiornamento(data);
               try {
                //Riempio Nome, Libreria e Campi Obbligatori
                 this.getTipoDocumento().retrieve(true);
               }
               catch (Exception e) {
                 throw new Exception("GD4_Documento::retrieveAbstract() - retrieve del tipo documento\n" + e.getMessage());
               }
               // Riempio la lista dei valori obbligatori
               try {
                   retrieveViewValori();
               }
               catch (Exception e) {
                 throw new Exception("GD4_Documento::retrieveAbstract() - retrieve dei valori messi nel view Campo\n" + e.getMessage());
               } 
               
               // Retrieve Riferimenti
               try {
                 retrieveRiferimenti();
               }
               catch (Exception e) {
                 throw new Exception("GD4_Documento::retrieveAbstract() - Retrieve Tutti i riferimenti\n" + e.getMessage());
               }
                              
               return true;
           }
           else {              
              throw new Exception("GD4_Documento::retrieveAbstract() Select fallita per idDocumento: " + 
                                     this.getIdDocumento());                   
           }
         }
         catch (Exception e) {               
               throw new Exception("GD4_Documento::retrieveAbstract()\n" + e.getMessage());
         }
  }

 /*
   * METHOD:      getUltAggiornamento()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Carica Data Ultimo aggiornamento.
   *              
   * RETURN:      String
  */ 
  public String getUltAggiornamento() throws Exception 
  {
         if (this.getIdDocumento().equals("0")) return "";
         
         String sSelect, ultAgg = "";
         IDbOperationSQL dbOp = null;
                  
         try {
           dbOp = varEnv.getDbOp();
            
           /*sSelect = "select GDM_UTILITY.SOVRASCRIVI_SEMPRE('"+this.getArea()+"','"+this.getTipoDocumento().getIdTipodoc()+"','"+this.getStatusDocumento()+"') FROM DUAL";
           
           elpsTime.start("getUltAggiornamento - 1 ",sSelect);
          
           dbOp.setStatement(sSelect);
           dbOp.execute();
           ResultSet rst = dbOp.getRstSet();
           
           elpsTime.stop();
           
           if (rst.next() && (rst.getString(1).equals("S"))) 
              ultAgg =  "00000000000000";
           else {*/
               sSelect = "select to_char(data_aggiornamento, 'YYYYMMDDHH24MISS') from documenti where id_documento = " + this.getIdDocumento();
               
               elpsTime.start("getUltAggiornamento ",sSelect);
               
               dbOp.setStatement(sSelect);
               dbOp.execute();
               
               elpsTime.stop();
               
               ResultSet rst = dbOp.getRstSet();

               if (rst.next())     
                  ultAgg =  rst.getString(1);
               else 
                  throw new Exception("GD4_Documento::getUltAggiornamento() -> Select fallita per idDocumento: " + 
                                         this.getIdDocumento());                   
           //}
                      
           return ultAgg;
         }
         catch (Exception e) {
               throw new Exception("GD4_Documento::getUltAggiornamento()\n" + e.getMessage());
         }
  }
  
    /*
   * METHOD:      logDocument(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Inserimento dell'attività nel file log  (SOLO LETTURA PER ADESSO..)
   *              
   * RETURN:      void
  */
  public void logDocument(String sTipoAzione) throws Exception
  {      
         try {
           dmALog = new DMActivity_Log(idDocumento,Global.TYPE_AZIONE_LETTURA,varEnv);
	       dmALog.setTypeLog(getTipoDocumento().getTipoLog());
	       dmALog.setTypeLogFile(getTipoDocumento().getTipoLogFile());
	       dmALog.insertActivityLog();
	       dmALog.f_log_documento();  
         }    
         catch (Exception e) {
           throw new Exception("GD4_Documento::logDocument " + e.getMessage());
         }      
  }   
  
  // ***************** METODI DI GESTIONE DEI VALORI ***************** //
 
  /*
   * METHOD:      addValore(String String, String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Aggiunge valore dato id Campo 
   *              
   * RETURN:      boolean
  */ 
  public boolean addValore(String nomeCampo, String idCampo, Object valore) throws Exception 
  {
         // Richiamo dell'addValore() con idValore=0        
         try {
           return this.addValore(nomeCampo,"0",idCampo,valore,null);
         }
         catch (Exception e) {           
           throw new Exception("GD4_Documento::addValore()\n" + e.getMessage());
         }
  }
  
  public boolean addValore(String nomeCampo, String idCampo, Object valore,FieldInformation fi) throws Exception 
  {
         // Richiamo dell'addValore() con idValore=0        
         try {
           return this.addValore(nomeCampo,"0",idCampo,valore,fi);
         }
         catch (Exception e) {           
           throw new Exception("GD4_Documento::addValore()\n" + e.getMessage());
         }
  }  
  
  /*
   * METHOD:      addValore(String, String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Aggiunge valore dato id Campo 
   *              
   * RETURN:      boolean
  */ 
  public boolean addValore(String idCampo, Object valore) throws Exception 
  {
         // Richiamo dell'addValore() con idValore=0        
         try {
           return this.addValore(null,"0",idCampo,valore,null);
         }
         catch (Exception e) {           
           throw new Exception("GD4_Documento::addValore()\n" + e.getMessage());
         }
  }
   
  /*
   * METHOD:      cancellaAllValori()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Cancella tutti valori  
   *              
   * RETURN:      boolean
  */ 
  public boolean cancellaAllValori() throws Exception 
  {
          if  (this.getIdDocumento().equals("0")) return false;
          IDbOperationSQL dbOp = null;
          try {

            dbOp = varEnv.getDbOp();
            StringBuffer sStm = new StringBuffer();

            sStm.append("delete from valori where id_documento="+this.getIdDocumento());
  
            dbOp.setStatement(sStm.toString());

            dbOp.execute();            

            return true;
          }
          catch (Exception e) {
               throw new Exception("GD4_Documento::cancellaAllValori()\n" + e.getMessage());
          }
  }

  // ***************** METODI DI GESTIONE DEGLI OGGETTI FILE ***************** //
 
  /*
   * METHOD:      addOggettoFile(String, String, String,
   *                             String, Object)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Aggiunge oggetto file  
   *              
   * RETURN:      boolean
  */ 
  public boolean addOggettoFile(String idFormato,
                                String fileName,
                                String allegato,
                                String idFilePadre,
                                Object file) throws Exception
  {
         // Richiamo dell'addOggettoFile() con idValore=0
         try {                      
           
           return this.addOggettoFile("0",idFormato, fileName,allegato,idFilePadre,file,true);
         }
         catch (Exception e) {           
           throw new Exception("GD4_Documento::addOggettoFile()\n" + e.getMessage());
         }
  }  

  /*
   * METHOD:      addOggettoFile(String, String,
   *                             String, Object)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Aggiunge oggetto file  
   *              
   * RETURN:      boolean
  */ 
  public boolean addOggettoFile(String idFormato,
                                String fileName,
                                String allegato,
                                Object file) throws Exception
  {
         // Richiamo dell'addOggettoFile() con idValore=0         
         return this.addOggettoFile("0",idFormato, fileName,allegato,null,file,false);        
  }  

 /*
   * METHOD:      cancellaOggettiFile(String, boolean)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Cancella oggetti file  
   *              
   * RETURN:      boolean
  */ 
  public boolean cancellaOggettiFile(String idOggettoFile,boolean flagCancellaLista) throws Exception 
  {
      int conta=0,size=this.getOggettiFile().size();
      Object obj;
      GD4_Oggetti_File oFile;
      
      if (cancellaAllegati==0 && !varEnv.getByPassCompetenze()) {
    	  String error="Attenzione! l'utente "+varEnv.getUser()+" non possiede le competenze per poter eliminare i file contenuti nel documento. Impossibile procedere.";
 		  codeErrorSaveDoc=Global.CODERROR_SAVEDOCUMENT_COMPALLEGATI;
		  descrErrorSaveDoc=error;
    	  throw new Exception(error);
      }
    	  
      if (dmALog==null) {
    	  dmALog = new DMActivity_Log(idDocumento,Global.TYPE_AZIONE_MODIFICA,varEnv);
	      dmALog.setTypeLog(getTipoDocumento().getTipoLog());
	      dmALog.setTypeLogFile(getTipoDocumento().getTipoLogFile());
	      dmALog.insertActivityLog();    	  
      }
	                  
      while (conta!=size) {    
          obj = this.getOggettiFile().elementAt(conta++);
          if (obj instanceof GD4_Oggetti_File) {
              oFile = (GD4_Oggetti_File)obj;
              if (oFile.getIdOggettoFile().compareTo(idOggettoFile) == 0){
                 try {
                	  //SPOSTATO SULLA SYNCROFS DELLA GD4_GESTOREOGGETTIFILE_FS - PARTE DI CANCELLAZIONE DEI FILES                	 
                	  /*Vector<String> vLista = new Vector<String>();
                	  vLista.add("'"+oFile.getFileName().replaceAll("'","''")+"'");
                	  oFile.setLog(dmALog);                	  
                	  oFile.executeLog(idDocumento,vLista);*/
                      oFile.delete(this.getLibreria().getDirectory(),this.getIdDocumento());
                      oFile.closeFile(false);
                      //oFile.setModificato("N");
                      //gestObjFile.addObjFile(oFile);//ELIMINATO....LI CANCELLO CICLANDO SULLA TABELLA , NON CON GLI OGGETTI
                      if (flagCancellaLista) 
                         this.getOggettiFile().remove(conta-1);
                      return true;
                  }
                  catch(Exception e) {
                      throw new Exception("GD4_Documento::cancellaOggettiFile() Errore in fase di delete oggetto file\n"
                                          +e.getMessage());
                  }
              }  
         }
         else {
              return false;
         }
      }

      return false;
  }

  // ***************** METODI DI GESTIONE DEI RIFERIMENTI ***************** //
 
  /*
   * METHOD:      aggiungiRiferimento(String, String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Carica un riferimento fra il documento
   *              attuale e quello passato con l'id documento 
   *              
   * RETURN:      void
  */  
  public void aggiungiRiferimento(String idDoc, String rif) throws Exception
  {
        try {
        	
           GD4_Riferimento gd4rif = new GD4_Riferimento(this.getIdDocumento());
           
           gd4rif.inizializzaDati(varEnv);

           gd4rif.insertRiferimento(idDoc,rif,bDontRepeatExistsRif);
           
          String sDip= (new LookUpDMTable(varEnv)).lookUpRelazioneDipendenza(this.getArea(),rif); 
           
          if (sDip.equals("S")) {this.settaPadre(idDoc);}
         }    
         catch (Exception e) {                              
               throw new Exception("GD4_Documento::aggiungiRiferimento() " + e.getMessage());
         }        
  }
  
  /*
   * METHOD:      eliminaRiferimento(String)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Elimina un riferimento  
   *              
   * RETURN:      void
  */ 
  public void eliminaRiferimento(String idDoc, String rif) throws Exception
  {
          try {
               GD4_Riferimento gd4rif = new GD4_Riferimento(this.getIdDocumento());
               
               gd4rif.inizializzaDati(varEnv);
               
               gd4rif.deleteRiferimento(idDoc,rif);
             }    
             catch (Exception e) {                              
                   throw new Exception("GD4_Documento::eliminaRiferimento() " + e.getMessage());
             }    
  }
  
  public String getPercorsoKFX() throws Exception
  {
         try {
           StringBuffer sStm = new StringBuffer();   
    
           IDbOperationSQL dbOp = varEnv.getDbOp();                  
  
           sStm.append("SELECT nvl(PERCORSO_DOCUMENTO,'-1') FROM ");
           sStm.append("kfx_dati_documento kd, kfx_percorsi_documenti kp ");
           sStm.append("WHERE kd.ID_DOCUMENTO=kp.ID_DOCUMENTO AND ");
           sStm.append("kd.BC_DOCUMENTO=LPAD("+this.getIdDocumento()+",10,'0')");
  
           dbOp.setStatement(sStm.toString());
  
           dbOp.execute();
  
           ResultSet rst = dbOp.getRstSet();
  
           if (rst.next()) {           
              return rst.getString(1);
           }
           else
              return "-1";
         }    
         catch (Exception e) {                              
               throw new Exception("GD4_Documento::getPercorsoKFX() " + e.getMessage());
         }  
  }
  
  // ***************** METODI DI SET E GET ***************** //

  public Environment getEnvironment() {
         return varEnv;
  }

  public void setSearchXml(String s) {}
  public void annullaRiferimento() {}  
   
  public boolean visualizza() { return false; }  
  public void settaFileP7M(Object file) {}    
 
  public String toString() 
  {
         return super.toString();
  }
 
 
  // ***************** METODI PRIVATI ***************** //
  /*
   * METHOD:      insert()
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Inserimento del documento  
   *              
   * RETURN:      boolean
  */
  private boolean insert(String stato) throws Exception 
  {			
	     StringBuffer sStm = new StringBuffer();
	     
         if (this.getLibreria().getIdLibreria().equals("0")) 
             throw new Exception("GD4_Documento::insert() Necessario inserire una libreria");
         
         if (this.getTipoDocumento().getIdTipodoc().equals("0"))
             throw new Exception("GD4_Documento::insert() Necessario inserire un tipo documento");

         try {
                            
           IDbOperationSQL dbOpSql = varEnv.getDbOp();
   
           // Se l'Id viene passato vuoto viene caricato con la sequence
           if (this.getIdDocumento().equals("0")) 
               this.setIdDocumento(dbOpSql.getNextKeyFromSequence("DOCU_SQ")+"");              
           
           this.generaCodiceRichiesta();
   
           sStm.append("insert into documenti (id_documento,id_libreria,id_tipodoc,");
           sStm.append("codice_richiesta,area,");
           sStm.append("data_aggiornamento,utente_aggiornamento,id_documento_padre,STATO_DOCUMENTO  ");
           
           if (conservazione!=null) 
        	   sStm.append(",conservazione ");
           if (archiviazione!=null) 
        	   sStm.append(",archiviazione ");
           
        	   		
           sStm.append(") values ");
           sStm.append("("+  this.getIdDocumento() );
           sStm.append(","+ this.getLibreria().getIdLibreria());
           sStm.append(","+ this.getTipoDocumento().getIdTipodoc());
           sStm.append(",'"+Global.replaceAll(this.codRich,"'","''")+"'");                                   
           sStm.append(",'"+Global.replaceAll(this.getArea(),"'","''")+"'");
           sStm.append(",sysdate,'"+ varEnv.getUser() +"',"+idDocumentoPadre+",'"+stato+"'");
           
           if (conservazione!=null) 
        	   sStm.append(",'"+conservazione+"'");
           if (archiviazione!=null) 
        	   sStm.append(",'"+archiviazione+"'");           
           
           sStm.append(")");
           
           dbOpSql.setStatement(sStm.toString());
           
           elpsTime.start("Inserimento in tabella Documenti",sStm.toString());
  
           dbOpSql.execute();
           
           elpsTime.stop();           
     
         }    
         catch (Exception e) {                    
               throw new Exception("Errore in inserimento del Documento\nFRASE SQL: "+sStm.toString()+"\nErrore:"+ e.getMessage());
         }
         
         return true;
  }

  /*
   * METHOD:      addValore(String, String, String)
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Aggiunge un elemento nella lista dei valori.
   *              E' necessario specificare (idValore, idCampo, valore)  
   *              
   * RETURN:      boolean
  */
  private boolean addValore(String nomeCampo, String idValore, String idCampo, Object valore, FieldInformation fi) throws Exception 
  {
          A_Valori val;
          try {      
            val = (A_Valori)Class.forName(varEnv.Global.PACKAGE + "." + varEnv.Global.DM + 
                                          "_" + varEnv.Global.VALORI).newInstance();
          }
          catch (Exception e) {
                throw new Exception("GD4_Documento::addValore() non riesco a creare l'oggetto di Classe: " + 
                                    varEnv.Global.PACKAGE + "." + varEnv.Global.DM + "_" + 
                                    varEnv.Global.VALORI);                
          }

          try { 
            val.inizializzaDati(varEnv);
          }
          catch (Exception e) {
             throw new Exception("GD4_Documento::addValore() inizializzaDati\n"+e.getMessage());
          }   

          try {   
            val.setIdValore(idValore);
            val.getCampo().setIdCampo(idCampo);
            val.getCampo().setNomeCampo(nomeCampo);            
            val.setValore(valore);
            val.setModificato("S");       
            val.setFieldInformation(fi);
          
            this.getValori().addElement(val);
         }
          catch (Exception e) {
             throw new Exception("GD4_Documento::addValore()\n"+e.getMessage());
          }    

          return true;          
  }
  
  /*
   * METHOD:      salvaValori(String)
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Memorizza tutti i valori contenuti nella lista
   *              nella tabella valori.
   *              sTipoSalvataggio=="INSERT" -> Inserisce i valori
   *              sTipoSalvataggio=="UPDATE" -> Aggiorna i valori
   * RETURN:      boolean
  */
  private boolean salvaValori(String sTipoSalvataggio) throws Exception 
  {
         int conta=0,size=this.getValori().size(), contaClob=0;
         Object obj;         
         boolean bEnterInInsert=false;
         boolean bEnterInUpdate=false;
         StringBuffer sUpdateHorizontal = new StringBuffer();       
         Vector vParameterBufferHorizontalTable = new Vector();
         Vector<DbOpSetParameterBuffer> vParameterBufferHorizontalTableOtherClob = new Vector<DbOpSetParameterBuffer>();
         Vector vValoriClob4000 = new Vector();
         IDbOperationSQL dbOpSqlBatchUpdate = null;
         IDbOperationSQL dbOpSqlBatchInsert = null;   
         Vector listaIdModificati = new Vector();
         Vector listaNomiCampoModificati = new Vector();
         Vector listaTipiCampoModificati = new Vector();
         
         boolean bExistsSetFieldHorizTbl=false;
         boolean bExistsTabellaOrizzontale=false;
         bExistsTabellaOrizzontale=controllaTabellaOrizzontale(sTipoSalvataggio);
         
         String sValoreFullCompletoInsert="";
         
         /* XXX DEVIATORE ORIZZONTALE-VERTICALE_MISTO INSERITO XXX */
         if (size>0 && this.getTipoDocumento().getIsHorizontalModel()==0) {
        	 dbOpSqlBatchUpdate = SessioneDb.getInstance().createIDbOperationSQL(varEnv.getDbOp());
        	 dbOpSqlBatchInsert = SessioneDb.getInstance().createIDbOperationSQL(varEnv.getDbOp());
         }	 

         elpsTime.start("******** INSERT DEI VALORI *******","");
         
         if (bExistsTabellaOrizzontale) {        	 
        	 sUpdateHorizontal.append("UPDATE "+nameHorizontalTable+" SET ");
         }
         GD4_Valori val = null;
         while (conta!=size) {        	 
              obj = this.getValori().elementAt(conta++);
              val = (GD4_Valori)obj;
              
              val.setLog(dmALog);
              val.setUpdateOrizzontalTable(bExistsTabellaOrizzontale);
              
              /* XXX DEVIATORE ORIZZONTALE-VERTICALE_MISTO INSERITO XXX */
              if (this.getTipoDocumento().getIsHorizontalModel()==0) {
	              val.setDbOpBatchInsert(dbOpSqlBatchInsert);
	              val.setDbOpBatchUpdate(dbOpSqlBatchUpdate);
              }  
              
              //val.isLastValue(conta==size);              
              
              if (val.getModificato().equals("S"))
              {                 
                	 
            	 if (sTipoSalvataggio.equals("INSERT")) {
                   try {                 	   
                	   
                	   val.setIndexParHorizontalTable(conta);                	   
                       val.insert(this.getIdDocumento()); 
                       if (val.getValoreFull()!=null) {
                    	   if (!sValoreFullCompletoInsert.equals("")) {
                    		   sValoreFullCompletoInsert+="\n"+val.getValoreFull();
                    	   }
                    	   else
                    		  sValoreFullCompletoInsert+=val.getValoreFull();
                       }
                       
                       
                       //Per ActivityLog VERTICALE
                       listaIdModificati.add(val.getIdValore());    

                       //Per ActivityLog ORIZZONTALE                       
                       if ( (getTipoDocumento().getTipoLog().equals(Global.TYPE_MAX_LOG) || 
                    		 getTipoDocumento().getTipoLog().equals(Global.TYPE_STD_LOG)) &&
                    		 ((FieldInformation)val.getFieldInformation()).getLog()==1) {
                    	     listaNomiCampoModificati.add(val.getCampo().getNomeCampo().toUpperCase());
                    	     listaTipiCampoModificati.add(((FieldInformation)val.getFieldInformation()).getTipo());
                       }
                       
                       //Nella tabella c'è un clob con 
                       //valori > 4000....va lanciata                       
                       //una frase SQL a parte come update 
                       //a parte
                       if (val.getValore4000()!=null) 
                    	   vValoriClob4000.add(new keyval(val.getIdValore4000(),val.getValore4000()));
                   }
                   catch (Exception e) 
                   {   
                	   
                	   if (dbOpSqlBatchInsert!=null) {
				        	dbOpSqlBatchInsert.close();
				        	dbOpSqlBatchInsert=null;
                	   }
                	   
				       if (dbOpSqlBatchUpdate!=null) {
				        	 dbOpSqlBatchUpdate.close();
				        	 dbOpSqlBatchUpdate=null;
				       }
				       
                       throw new Exception("Errore nell'inserimento del valore per il campo ("+val.getCampo().getNomeCampo()+")\n"+e.getMessage());
                   }
            	}                   
                if (sTipoSalvataggio.equals("UPDATE")) {
                   try { 
                	  
                	   val.setIndexParHorizontalTable(conta);
                       val.update(this.getIdDocumento());   
                       //Per ActivityLog VERTICALE
                       listaIdModificati.add(val.getIdValore());    

                       //Per ActivityLog ORIZZONTALE                       
                       if ( (getTipoDocumento().getTipoLog().equals(Global.TYPE_MAX_LOG) || 
                    		 getTipoDocumento().getTipoLog().equals(Global.TYPE_STD_LOG)) &&
                    		 ((FieldInformation)val.getFieldInformation()).getLog()==1) {
                    	     listaNomiCampoModificati.add(val.getCampo().getNomeCampo().toUpperCase());
                    	     listaTipiCampoModificati.add(((FieldInformation)val.getFieldInformation()).getTipo());
                       }
                       
                       //Nella tabella c'è un clob con 
                       //valori > 4000....va lanciata                       
                       //una frase SQL a parte come update 
                       //a parte
                       if (val.getValore4000()!=null) 
                    	   vValoriClob4000.add(new keyval(val.getIdValore4000(),val.getValore4000()));                       
                   }
                   catch (Exception e) {
                   
                	   if (dbOpSqlBatchInsert!=null) {
				        	dbOpSqlBatchInsert.close();
				        	dbOpSqlBatchInsert=null;
                	   }
                	   
				       if (dbOpSqlBatchUpdate!=null) {
				        	 dbOpSqlBatchUpdate.close();
				        	 dbOpSqlBatchUpdate=null;
				       }
				       
                       throw new Exception("Errore nell'aggiornamento del valore per il campo ("+val.getCampo().getNomeCampo()+")\n"+e.getMessage());
                   }    
                }
                  
                if (!val.getIsInsertOrUpdate().equals("NESSUNA")) {
	                if (val.getIsInsertOrUpdate().equals("INSERT"))
	                	bEnterInInsert=true;
	                else
	                	bEnterInUpdate=true;
	                   
	                if (bExistsTabellaOrizzontale) { 
	                	if (!val.getHorizontalPhrase().equals("")) {
	                	   bExistsSetFieldHorizTbl=true;
	                	}                   
	                	
	                	//Conto il numero di clob	                	
	                	DbOpSetParameterBuffer dbOpPar = ((DbOpSetParameterBuffer)val.getHorizontalParameterBuffer());
	                	
	                	if (dbOpPar!=null && dbOpPar.getType()==DbOpSetParameterBuffer.IS_ASCIISTREAM) contaClob++;                		                
	                	
	                	//Se il clob è il primo lo metto nella frase generale di update della tbl orizzontale
	                	if (dbOpPar==null || contaClob==1 || dbOpPar.getType()!=DbOpSetParameterBuffer.IS_ASCIISTREAM) {
	                		sUpdateHorizontal.append(val.getHorizontalPhrase());	              
	                		vParameterBufferHorizontalTable.add(val.getHorizontalParameterBuffer());
	                	}
	                	else {
	                		dbOpPar.setNameColumn(val.getCampo().getNomeCampo());	                		
	                		vParameterBufferHorizontalTableOtherClob.add(dbOpPar);
	                	}
	                }
                }
                
                val.setModificato("N");
              }
         }
         
         /* XXX DEVIATORE ORIZZONTALE-VERTICALE_MISTO INSERITO XXX */
         if (bEnterInInsert && this.getTipoDocumento().getIsHorizontalModel()==0)
        	 val.executeBatch(""+this.getIdDocumento(),"INSERT",vValoriClob4000);
         
         /* XXX DEVIATORE ORIZZONTALE-VERTICALE_MISTO INSERITO XXX */
         if (bEnterInUpdate && this.getTipoDocumento().getIsHorizontalModel()==0)
        	 val.executeBatch(""+this.getIdDocumento(),"UPDATE",vValoriClob4000);

         if (bEnterInUpdate || bEnterInInsert) {
        	 //ActivityLog VERTICALE
        	 if (this.getTipoDocumento().getIsHorizontalModel()==0) {
        		 if (listaIdModificati.size()>0)
        		     val.executeLog(""+this.getIdDocumento(),listaIdModificati);
        	 }        	
         }        	            
                  
		 if (dbOpSqlBatchInsert!=null) {
	        	dbOpSqlBatchInsert.close();
	        	dbOpSqlBatchInsert=null;
		 }
		   
	     if (dbOpSqlBatchUpdate!=null) {
	        	 dbOpSqlBatchUpdate.close();
	        	 dbOpSqlBatchUpdate=null;
	     }      
         
         if (bExistsTabellaOrizzontale)
            sUpdateHorizontal.append(" WHERE ID_DOCUMENTO="+idDocumento);         
        	          
         elpsTime.stop();
         
         StringBuffer valoriParametri = new StringBuffer("");
         
         if (bExistsSetFieldHorizTbl) {         	         
        	 
	         String phrase=Global.replaceAll(sUpdateHorizontal.toString(),", WHERE"," WHERE");	         	        
	         String elencoColonneTrattateComeClob="";
	         elpsTime.start("******** UPDATE TABELLA ORIZZONTALE *******",phrase);
	         
	         try {
	        	 IDbOperationSQL dbOpSql = varEnv.getDbOp();

	        	 phrase=phrase.replaceAll("=:"," = :");
	        	 phrase=phrase.replaceAll(","," , ");

	        	 dbOpSql.setStatement(phrase);
	        	 
	        	 for(int i=0;i<vParameterBufferHorizontalTable.size();i++) {
	        		 DbOpSetParameterBuffer dbOpSpB = (DbOpSetParameterBuffer)vParameterBufferHorizontalTable.get(i);

	        		 if (dbOpSpB==null) continue;

	        		 if (dbOpSpB.getType()==DbOpSetParameterBuffer.IS_ASCIISTREAM) {
	        			 ///ByteArrayInputStream bais = (ByteArrayInputStream)dbOpSpB.getValue();
	        			 //dbOpSql.setAsciiStream(dbOpSpB.getNamePar(),bais,bais.available());
	        			 //oracle.sql.CLOB newClob = oracle.sql.CLOB.createTemporary(dbOpSql.getConn(), false, oracle.sql.CLOB.DURATION_CALL);
	                     
	                     //newClob.putString(1,(String)dbOpSpB.getValue());
	        			 dbOpSql.setParameter(dbOpSpB.getNamePar(),(String)dbOpSpB.getValue());
	        			 if (!elencoColonneTrattateComeClob.equals("")) elencoColonneTrattateComeClob+=",";
	        			 elencoColonneTrattateComeClob=dbOpSpB.getNameColumn();
	        		 }
	        		 else {
	        			 if (dbOpSpB.getValue() ==null) {
	        				 if (dbOpSpB.getValueTypeNull() instanceof java.sql.Date)
	        					 dbOpSql.setParameter(dbOpSpB.getNamePar(),(java.sql.Date)null);
	        				 
	        				 if (dbOpSpB.getValueTypeNull() instanceof java.math.BigDecimal)
	        					 dbOpSql.setParameter(dbOpSpB.getNamePar(),(java.math.BigDecimal)null);	  	        				 	        				 
	        			 }

	        			 if (dbOpSpB.getValue() instanceof java.lang.String) 
	        				 dbOpSql.setParameter(dbOpSpB.getNamePar(),(java.lang.String)dbOpSpB.getValue());	        					        			 
	        				 
	
	        			 if (dbOpSpB.getValue() instanceof java.sql.Date)	        				 
	        				 dbOpSql.setParameter(dbOpSpB.getNamePar(),(new java.sql.Timestamp(((java.sql.Date)dbOpSpB.getValue()).getTime())));
	
	        			 if (dbOpSpB.getValue() instanceof java.sql.Timestamp)
	        				 dbOpSql.setParameter(dbOpSpB.getNamePar(),(java.sql.Timestamp)dbOpSpB.getValue());
	
	        			 if (dbOpSpB.getValue() instanceof java.math.BigDecimal)
	        				 dbOpSql.setParameter(dbOpSpB.getNamePar(),(java.math.BigDecimal)dbOpSpB.getValue());   
	        			 
	        			 valoriParametri.append(dbOpSpB.getNamePar()+"="+dbOpSpB.getValue()+"\n");
	        		 }        			         		
	        	 }
	        	 
	        	 try {
	        	   dbOpSql.execute();
	        	 }
	        	 catch(NullPointerException nle) {
	        	   //GM 20/04/2012	 
	        	   //Se sono nel caso di nullPointer cerco di capire se c'è errore nel
	        	   //clob. Ad es. se cerco di mettere un valore passato dall'utente >4000
	        	   //ma la colonna è varchar2(4000) io cerco di fare la setAsciiStream
	        	   //e in  questo caso la fillClob della dbOp torna NULL....allora
	        	   //avverto l'utente che il problema potrebbe essere quello.
	        	   String sStackTrace = CallStackUtil.stack2string(nle);
	        	   
	        	   if (sStackTrace!=null && sStackTrace.indexOf("fillClob")!=-1) {	        		   
	        		   String erroreParlante="Attenzione! Errore di nullPointer sul riempimento del Clob.\n"+
	        			   					 "Una spiegazione possibile all'errore è che si stia cercando " +
	        		   						 "di inserire un valore maggiore di 4000 caratteri "+
	        		   						 "su un campo definito come VARCHAR2. \nIn tal caso il sistema "+
	        		   						 "cercherà di trattare il campo come CLOB.\n"+
	        		   						 "I campi trattati come clob in questa update sono: "+
	        		   						 "("+elencoColonneTrattateComeClob+"): verificare se fra questi "+
	        		   						 "ce n'è almeno uno definito come VARCHAR2.";
	        		   throw new Exception(erroreParlante);
	        	   }
	        	   else 
	        		   throw new Exception(nle);
	        	 }
	        	 catch(Exception e) {
	        		
	        	   if (e.getMessage().indexOf("ORA-00001")!=-1) {
	        		   codeErrorSaveDoc=Global.CODERROR_SAVEDOCUMENT_UK;
	        		   descrErrorSaveDoc=Global.DESCRERROR_SAVEDOCUMENT_UK;	        		   
	        	   }
	        	   if (e.getMessage().indexOf("ORA-20008")!=-1) {
	        		   String sMessage=e.getMessage();
	        		   codeErrorSaveDoc=Global.CODERROR_SAVEDOCUMENT_TRIGGER;
	        		   descrErrorSaveDoc=sMessage.substring(sMessage.indexOf("ORA-20008")+("ORA-20008").length()+1,sMessage.indexOf("ORA-",sMessage.indexOf("ORA-20008")+1));
	        	   }
	        	   
	        	   throw new Exception(e);
	        	 }
	        	 
	        	 //Aggiorno gli altri eventuali clob (se ce n'è più di 1)
	        	 for(int i=0;i<vParameterBufferHorizontalTableOtherClob.size();i++) {
	        		 String sPhraseClob="UPDATE "+nameHorizontalTable+" SET ";
	        		 DbOpSetParameterBuffer dbOpPar = vParameterBufferHorizontalTableOtherClob.get(i);
	        			 
	        		 sPhraseClob+=dbOpPar.getNameColumn()+" = :P_PAR";
	        		 sPhraseClob+=" WHERE ID_DOCUMENTO="+idDocumento;
	        		 
	        		 dbOpSql.setStatement(sPhraseClob);
	        		 
	        		 //ByteArrayInputStream bais = (ByteArrayInputStream)dbOpPar.getValue();
	        		 //dbOpSql.setAsciiStream(":P_PAR",bais,bais.available());
	        		 dbOpSql.setParameter(":P_PAR",(String)dbOpPar.getValue());
	        		 
	        		 dbOpSql.execute();
	        	 }
	        	 
	        	 elpsTime.stop();
	        	 if (bEnterInUpdate || bEnterInInsert) {		        
	        	     //ActivityLog ORIZZONTALE		     
	        		 if (this.getTipoDocumento().getIsHorizontalModel()==1) {
		        		 if (listaNomiCampoModificati.size()>0)
		        		     val.executeLogHorizontal(""+this.getIdDocumento(),nameHorizontalTable,listaNomiCampoModificati,listaTipiCampoModificati);
	        		 } 
		         }
	        	 
	        	 //FULL TEXT
		  		  if (!skipReindexFullTextField) {
		        	  StringBuffer stmFullTest= new StringBuffer("");
			  		  
		        	  if (sTipoSalvataggio.equals("INSERT")) {
		        		  if (!sValoreFullCompletoInsert.equals("")) {
			        		  elpsTime.start("******** LANCIO FULL_TEXT INSERT *******","UPDATE CAMPO FULL_TEXT");
			        		  
			        		  stmFullTest.append("UPDATE "+nameHorizontalTable+" SET ");
			        		  stmFullTest.append(" FULL_TEXT = :P_PARFULLTEXT");
			        		  stmFullTest.append(" WHERE ID_DOCUMENTO="+idDocumento);
			        		  
			        		  dbOpSql.setStatement(stmFullTest.toString());
			        		  
			        		  ByteArrayInputStream bais = new ByteArrayInputStream(sValoreFullCompletoInsert.getBytes("UTF-8"));;
				        	  dbOpSql.setAsciiStream(":P_PARFULLTEXT",bais,bais.available());
				        		 
				        	  dbOpSql.execute();				        		
		        		  }  		 
		        	  }
		        	  else {
				  		  elpsTime.start("******** LANCIO F_FULL_TEXT *******","F_FULL_TEXT_HORIZ("+idDocumento+",'"+nameHorizontalTable+"')");
				  		  stmFullTest.append("F_FULL_TEXT_HORIZ("+idDocumento+",'"+nameHorizontalTable+"')");
				  		  
				  		  dbOpSql.setCallFunc(stmFullTest.toString());
				  		  
				  		  dbOpSql.execute();
				  		  elpsTime.stop();
				  		  
				  		  String ret = dbOpSql.getCallSql().getString(1);
				  		  
				  		  if (ret!=null)
				  			  throw new Exception("Errore in F_FULL_TEXT_HORIZ("+idDocumento+",'"+nameHorizontalTable+"')\n "+ret);	
		        	  }
		  		  }  	 
	         }    
	         catch (Exception e) {                              
	        	 e.printStackTrace();
	        	 throw new Exception("Errore in aggiornamento TABELLA ORIZZONTALE\nFrase SQL: "+phrase+"\nCon Parametri:\n"+valoriParametri+"\nErrore: " + e.getMessage());
	         }         
	         
	         
         } 
         //Indicizzazione
         //rt_rebuild_index();
         //Fine indicizzazione
         
         return true;
  }
  
  private boolean controllaTabellaOrizzontale(String sTipoSalvataggio) throws Exception {
	      boolean ret=true;
	      	      
	      nameHorizontalTable=(new LookUpDMTable(varEnv).lookUpAliasOrizzontalTable(this.getIdDocumento()));
	      
	      if (nameHorizontalTable.equals(""))
	    	  return false;
	      
	      if (sTipoSalvataggio.equals("INSERT")) {
	    	  try {
		    	  IDbOperationSQL dbOpSql = varEnv.getDbOp();        	         	              
		    	  
	              StringBuffer stm = new StringBuffer("");
		    	  stm.append("INSERT INTO "+nameHorizontalTable+" (ID_DOCUMENTO) ");
		    	  stm.append("VALUES ("+this.getIdDocumento()+") ");
		    	  
		    	  elpsTime.start("******** INSERT TABELLA ORIZZONTALE *******",stm.toString());
		    	  dbOpSql.setStatement(stm.toString());
		    	  
	              dbOpSql.execute();
	              elpsTime.stop();
	    	  }    
	          catch (Exception e) {                    
	              throw new Exception("GD4_Documento::controllaTabellaOrizzontale() - INSERT " + e.getMessage());
	          }
	      } 
	      
	      return ret;
  }

  private boolean addOggettoFile(String idOggettoFile, 
          String idFormato,
          String fileName,
          String allegato,
          String idFilePadre,
          Object file,
          boolean isFileTemp) throws Exception
  {
	      return addOggettoFile(idOggettoFile,idFormato,fileName,allegato,idFilePadre,file,isFileTemp,"N",null);
  }
  
  /*
   * METHOD:      addOggettoFile(String idOggettoFile, 
   *                             String idFormato,
   *                             String fileName,
   *                             String allegato,
   *                             String idFilePadre,
   *                             Object file)
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION:  Aggiunge un elemento nella lista 
   *               degli oggetti_file
   *               
   * RETURN:      boolean
  */
  private boolean addOggettoFile(String idOggettoFile, 
                                 String idFormato,
                                 String fileName,
                                 String allegato,
                                 String idFilePadre,
                                 Object file,
                                 boolean isFileTemp,
                                 String forzaDimMaxAll,
                                 String pathFileFS) throws Exception
  {
          A_Oggetti_File oFile;

          try {     
            oFile = (A_Oggetti_File)Class.forName(varEnv.Global.PACKAGE + "." + varEnv.Global.DM + 
                                                   "_" + varEnv.Global.OGGETTI_FILE).newInstance();
          }
          catch (Exception e) {
               throw new Exception("GD4_Documento::addOggettoFile() non riesco a creare l'oggetto di Classe: " + 
                                       varEnv.Global.PACKAGE + "." + varEnv.Global.DM + "_" + 
                                       varEnv.Global.OGGETTI_FILE);                  
          }

          try {  
            oFile.inizializzaDati(varEnv);
          }
          catch (Exception e) {
             throw new Exception("GD4_Documento::addOggettoFile() inizializzaDati\n" + e.getMessage());                  
          }

          try { 
            oFile.setIdOggettoFile(idOggettoFile);
            oFile.setIdFormato(idFormato);
            oFile.setFileName(fileName);
            oFile.setAllegato(allegato);
            if (idFilePadre!=null)
               oFile.setIdOggettoFilePadre(idFilePadre);
            oFile.setFile(file);
            oFile.setModificato("S"); 
            oFile.setOggettoFileTemp(isFileTemp);
            oFile.setForzaPerMidMax(forzaDimMaxAll);
            oFile.setPercorsoFileFS(pathFileFS);
                              
            this.getOggettiFile().addElement(oFile);
          }
          catch (Exception e) {
             throw new Exception("GD4_Documento::addOggettoFile()\n" + e.getMessage());                  
          } 
       
          return true;
          
  }

  /*
   * METHOD:      salvaOggettiFile(String)
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Memorizza tutti i valori contenuti nella lista
   *              nella tabella oggetti file.
   *              sTipoSalvataggio=="INSERT" -> Inserisce i valori
   *              sTipoSalvataggio=="UPDATE" -> Aggiorna i valori
   * RETURN:      boolean
  */
  private boolean salvaOggettiFile(String sTipoSalvataggio) throws Exception 
  {
	  	 listaIdFileToLog = new Vector();	     
	     vElencoNomiAllegatiModificati.removeAllElements();
	     vElencoNomiAllegatiRinominati.removeAllElements();
	 
         int conta=0,contaPadre=-1,size=this.getOggettiFile().size();
         Object obj;
         Vector<String> listaIdModificati = new Vector<String>();
         Vector<String> listaIdFileToLog = new Vector<String>();
         GD4_Oggetti_File oFile = null;
         
         //GESTIONE DEI LOG (VECCHIA GESTIONE)
         /*if (sTipoSalvataggio.equals("UPDATE")) {
        	 while (conta!=size) {
        		 obj = this.getOggettiFile().elementAt(conta++);
        		 oFile = (GD4_Oggetti_File)obj;
        		 
        		 oFile.setLog(dmALog);
        		 
        		 if (oFile.getModificato().equals("N")) continue;
        		 
        		 listaIdModificati.add("'"+oFile.getFileName().replaceAll("'","''")+"'");
        	 }
        	 
        	 if (oFile!=null && listaIdModificati.size()>0)          
        		 oFile.executeLog(""+this.getIdDocumento(),listaIdModificati);          	 
         }    */                         
         
         elpsTime.start("******** INSERT DEGLI OGGETTI FILE *******","");
         
         conta=0;
         while (conta!=size) {
         
              obj = this.getOggettiFile().elementAt(conta++);
              oFile = (GD4_Oggetti_File)obj;
              
              oFile.setLog(dmALog);
              oFile.setOgfiLog(bogfilog);
 
              if (oFile.getModificato().equals("S"))
              {
                 //Gestione del file padre
                 if (oFile.getAllegato().equals("N"))  {                 
                     contaPadre=conta - 1;
                 }
                  else {                       
                      if ( (contaPadre!=-1) && ((oFile.getIdOggettoFilePadre()==null) || 
                           (oFile.getIdOggettoFilePadre().equals(""))) )             
                         oFile.setIdOggettoFilePadre(((GD4_Oggetti_File)this.getOggettiFile().elementAt(contaPadre)).getIdOggettoFile());                         
                      
                  }
             
                  if (sTipoSalvataggio.equals("INSERT")) {                	
                		 
                     try {
                       if (this.getTipoDocumento().modificaAllegati==0 && !varEnv.getByPassCompetenze())  {
                		 String error="Attenzione! l'utente "+varEnv.getUser()+" non possiede le competenze per poter inserire gli allegati. Impossibile procedere.";
                		 codeErrorSaveDoc=Global.CODERROR_SAVEDOCUMENT_COMPALLEGATI;
  	        		     descrErrorSaveDoc=error;
                		 throw new Exception(error);
                	   }
                    	 
                       vElencoNomiAllegatiModificati.add(oFile.getFileName());
                       oFile.insert(this.getIdDocumento(), this.getLibreria());
                       listaIdModificati.add("'"+oFile.getFileName().replaceAll("'","''")+"'");
                       
                       
                       if (oFile.isbLogEseguito())  {
                    	   this.listaIdFileToLog.add(oFile.getIdOggettoFile());
                           listaIdFileToLog.add("'"+oFile.getFileName().replaceAll("'","''")+"'");
                       }
                       if (oFile.isFileFs()) gestObjFile.addObjFile(oFile);
                     }
                     catch (DocumentException Doce)
                     {
                    	 throw Doce;
                     }
                     catch (Exception e)
                     {
                        throw new Exception("GD4_Documento::salvaOggettiFile errore in insert\n"
                                               +e.getMessage());
                     }
                  }
                  if (sTipoSalvataggio.equals("UPDATE")) {
                	 if (modificaAllegati==0 && !varEnv.getByPassCompetenze())  {
                		 String error="Attenzione! l'utente "+varEnv.getUser()+" non possiede le competenze per poter aggiornare gli allegati. Impossibile procedere.";
                		 codeErrorSaveDoc=Global.CODERROR_SAVEDOCUMENT_COMPALLEGATI;
  	        		     descrErrorSaveDoc=error;
                		 throw new Exception(error);
                	 }
                	  
                     try {           
                      String fileToLog;	 
                      if (oFile.getOldFileName().equals("")) {
                    	  vElencoNomiAllegatiModificati.add(oFile.getFileName());
                    	  fileToLog=oFile.getFileName();
                      }
                      else  {
                    	  vElencoNomiAllegatiModificati.add(oFile.getOldFileName());
                      	  vElencoNomiAllegatiRinominati.add(oFile.getFileName());
                      	  fileToLog=oFile.getOldFileName();
                      }
                       oFile.update(this.getIdDocumento(), this.getLibreria());   
                       listaIdModificati.add("'"+oFile.getFileName().replaceAll("'","''")+"'");
                       if (oFile.isbLogEseguito()) {
                    	   this.listaIdFileToLog.add(oFile.getIdOggettoFile());
                    	   listaIdFileToLog.add("'"+fileToLog.replaceAll("'","''")+"'");
                       }
                       if (oFile.isFileFs()) gestObjFile.addObjFile(oFile);
                     }
                     catch (DocumentException Doce)
                     {
                    	 throw Doce;
                     }
                     catch (Exception e)
                     {
                        throw new Exception("GD4_Documento::salvaOggettiFile errore in update\n"
                                               +e.getMessage());
                     }
                  }                
                  oFile.setModificato("N");
              }
         }
                  
         //Nuova Gestione dei LOG
         //VEcchia gestione dei log. Adesso è dentro la update dell'oggetto file
        /*if (listaIdFileToLog.size()>0 && oFile!=null && this.isOgfiLog())  {        	
            oFile.executeLog(""+this.getIdDocumento(),listaIdFileToLog);
         }*/
         
         
         elpsTime.stop();                    
         
         return true;
  }
  
  //OLD METHOD
  /*private void salvaOggettiFileTemp() throws Exception {	  	   
	  	  IDbOperationSQL dbOpSql = varEnv.getDbOp();
	  	  Vector<String> vListaIdModificati = new Vector<String>();
	  	  
		  //Gestione dei log		  	  
		  try {
			  
			  StringBuffer sStmTemp = new StringBuffer("SELECT NOMEFILE ");
			  sStmTemp.append("FROM ALLEGATI_TEMP ");
			  sStmTemp.append("WHERE AREA = '"+this.getArea()+"' ");
			  if (crAllegatiTempModulistica==null)
				  sStmTemp.append(" AND CODICE_RICHIESTA = '"+this.getCodiceRichiesta()+"' ");
			  else
				  sStmTemp.append(" AND CODICE_RICHIESTA = '"+crAllegatiTempModulistica+"' ");
			  sStmTemp.append("  AND CODICE_MODELLO = '"+this.getTipoDocumento().getNome()+"' ");
			  sStmTemp.append("  AND UTENTE_AGGIORNAMENTO = '"+varEnv.getUser()+"' ");
			  sStmTemp.append("  ORDER BY NOMEFILE ASC ");
	      
			  dbOpSql = varEnv.getDbOp();
			  			  
			  dbOpSql.setStatement(sStmTemp.toString());
	          
			  dbOpSql.execute();         
	
	          ResultSet rst = dbOpSql.getRstSet();
	
	          while (rst.next()) {
	        	  vListaIdModificati.add("'"+rst.getString(1).replaceAll("'","''")+"'");
	          }
	          	         
	        }    
	        catch (Exception e) {        	
	     	  throw new Exception("GD4_Documento::salvaOggettiFileTemp() Errore in lettura allegati_temp\n"+e.getMessage());
	       }	  	  
	        
	        //Gestione dei log		  	  
	        try {		
	          if (dmALog!=null) dmALog.insertAllOgfiLog(idDocumento,vListaIdModificati,true);
	         }    
	         catch (Exception e) {        	
	     	   throw new Exception("GD4_Documento::salvaOggettiFileTemp() Errore in scrittura Oggetti File Log\n"+e.getMessage());
	        }		        
	  	  
	       StringBuffer stmAllegatiTemp= new StringBuffer("");
		  		  
		   elpsTime.start("******** LANCIO F_ALLINEA_ALLEGATI *******","");
		   stmAllegatiTemp.append("f_allinea_allegati('"+this.getArea()+"',");
		   stmAllegatiTemp.append("'"+this.getTipoDocumento().getNome()+"',");
		   if (crAllegatiTempModulistica==null)
			  stmAllegatiTemp.append("'"+this.getCodiceRichiesta()+"',");
		   else
		 	  stmAllegatiTemp.append("'"+crAllegatiTempModulistica+"',");
		   stmAllegatiTemp.append("'"+this.getIdDocumento()+"',");
		   stmAllegatiTemp.append("'"+varEnv.getUser()+"')");
		  		   		  
		   dbOpSql.setCallFunc(stmAllegatiTemp.toString());	  		  
		  		  
		   dbOpSql.execute();
		  		  		  
		  	
		   //Gestione dei log		  	  
		   try {		
	          if (dmALog!=null && vListaIdModificati.size()>0) dmALog.insertAllOgfiLog(idDocumento,vListaIdModificati,false);
	         }    
	         catch (Exception e) {        	
	     	   throw new Exception("GD4_Documento::salvaOggettiFileTemp() Errore in scrittura Oggetti File Log\n"+e.getMessage());
	        }
	        
		   elpsTime.stop();		  		  		  
  }*/
  
  private void sistemaOggettiFileTemp() throws Exception {	      
		  StringBuffer sStmTemp = new StringBuffer("SELECT ALLEGATI_TEMP.NOMEFILE, ALLEGATO, SUBSTR (ALLEGATI_TEMP.NOMEFILE, INSTR (ALLEGATI_TEMP.NOMEFILE, '.', -1) + 1) ESTENSIONE, FORZA, ");
		  sStmTemp.append("nvl(dbms_lob.getlength(ALLEGATO),0),nvl(percorso,'')  ");
		  sStmTemp.append("FROM ALLEGATI_TEMP,ALLEGATI_TEMP_PERCORSI ");
		  sStmTemp.append("WHERE ALLEGATI_TEMP.AREA = '"+this.getArea()+"' ");
		  if (crAllegatiTempModulistica==null)
			  sStmTemp.append(" AND ALLEGATI_TEMP.CODICE_RICHIESTA = '"+this.getCodiceRichiesta()+"' ");
		  else
			  sStmTemp.append(" AND ALLEGATI_TEMP.CODICE_RICHIESTA = '"+crAllegatiTempModulistica+"' ");
		  sStmTemp.append("  AND ALLEGATI_TEMP.CODICE_MODELLO = '"+this.getTipoDocumento().getNome()+"' ");
		  sStmTemp.append("  AND ALLEGATI_TEMP.UTENTE_AGGIORNAMENTO = '"+varEnv.getUser()+"' ");
		  sStmTemp.append("  AND ALLEGATI_TEMP.AREA=ALLEGATI_TEMP_PERCORSI.AREA (+) ");
		  sStmTemp.append("  AND ALLEGATI_TEMP.CODICE_RICHIESTA=ALLEGATI_TEMP_PERCORSI.CODICE_RICHIESTA (+) ");
		  sStmTemp.append("  AND ALLEGATI_TEMP.CODICE_MODELLO=ALLEGATI_TEMP_PERCORSI.CODICE_MODELLO (+) ");
		  sStmTemp.append("  AND ALLEGATI_TEMP.NOMEFILE=ALLEGATI_TEMP_PERCORSI.NOMEFILE (+) ");
		  sStmTemp.append("  ORDER BY ALLEGATI_TEMP.NOMEFILE ASC ");
		  
		  try {
			if (dbOpSqlAllegatiTemp==null)
				dbOpSqlAllegatiTemp = SessioneDb.getInstance().createIDbOperationSQL(varEnv.getDbOp()); 
  			  
			dbOpSqlAllegatiTemp.setStatement(sStmTemp.toString());
	          
			dbOpSqlAllegatiTemp.execute();         
	
	        ResultSet rst = dbOpSqlAllegatiTemp.getRstSet();
	
	        while (rst.next()) {
	        	String forza=rst.getString("FORZA");
	        	String sNomeFile=rst.getString(1);
	        	String idFormato;
	        	String pathFS=null;
	        	
	        	//Recupero l'id del formato a partire dall'estensione del file
	            try {	             
	              idFormato = (new LookUpDMTable(varEnv)).lookUpFormato(Global.lastTrim(sNomeFile,".",varEnv.Global.WEB_SERVER_TYPE));
	            }
	            catch (Exception e) {        	
	    	     	throw new Exception("Errore in lettura formato file "+sNomeFile+"\n"+e.getMessage());
	    	    }	
	        		        		        	
	        	InputStream is=null;
	        	if (rst.getLong(5)>0) {
	        		 try {is=dbOpSqlAllegatiTemp.readBlob(2);}catch (NullPointerException e) {}
	        	}
	        	else {
	        		pathFS=Global.nvl(rst.getString(6), "");
					 if (pathFS.equals("")) throw new Exception("Attenzione! il blob dell'allegati_temp è vuoto e anche il percorso su FS non è presente!");
					 
					 try {
						 LetturaScritturaFileFS fs = new  LetturaScritturaFileFS(pathFS);
					 
						 is = fs.leggiFile();
						 
					 } catch (Exception e) {
						 throw new Exception("Attenzione! Impossibile leggere l'allegato_temp dal path "+pathFS);
					 }	        		
	        	}
	        			
	        	GD4_Oggetti_File of=(GD4_Oggetti_File)findOggettoFileByName(sNomeFile);
	        	
	        	//L'obj file non esiste, lo inserisco
	        	if (of==null) {
	        		this.addOggettoFile("0",idFormato, sNomeFile,"N",null,is,true,forza,pathFS);
	        	}	  
	        	else {
	        		if (is!=null) {
	        			of.closeFile(false);
	        			of.setFile(is);
	        			of.setModificato("S");
	        			of.setOggettoFileTemp(true);
	        			of.setForzaPerMidMax(forza);
	        			of.setPercorsoFileFS(pathFS);
	        		}	
	        	}
	        		      	        		        		
	        }
	      }    
	      catch (Exception e) {        	
	     	throw new Exception("GD4_Documento::sistemaOggettiFileTemp() Errore in lettura allegati_temp e allegati_temp_percorsi\n"+e.getMessage());
	      }		      	      
  }
  
  public void finalizzaGestioneAllegatiTemp() throws Exception {
	  	  if (!bAllegatiTempModulistica) return;
	  
	  	 String cr;
	  	if (crAllegatiTempModulistica==null)
	  		cr=this.getCodiceRichiesta();
		  else
			cr=crAllegatiTempModulistica;;
	  	  StringBuffer sStmTemp = new StringBuffer("DELETE ");
		  sStmTemp.append("ALLEGATI_TEMP ");
		  sStmTemp.append("WHERE AREA = '"+this.getArea()+"' ");
		  sStmTemp.append(" AND CODICE_RICHIESTA = '"+cr+"' ");		 
		  sStmTemp.append("  AND CODICE_MODELLO = '"+this.getTipoDocumento().getNome()+"' ");
		  sStmTemp.append("  AND UTENTE_AGGIORNAMENTO = '"+varEnv.getUser()+"' ");		  
		  
		  try {
			  IDbOperationSQL dbOpSql = varEnv.getDbOp();
			  
			  dbOpSql = varEnv.getDbOp();
				  
			  dbOpSql.setStatement(sStmTemp.toString());
	          
			  dbOpSql.execute();    
	      }    
	      catch (Exception e) {        	
	     	throw new Exception("GD4_Documento::finalizzaGestioneAllegatiTemp() Errore pulitura allegati_temp\n"+e.getMessage());
	      }		
	      
		  try {
			DocUtil du = new DocUtil(varEnv);
			du.cancellaAllegatoTempPercorsi(this.getArea(), this.getTipoDocumento().getNome(), cr, null, varEnv.getUser(),null,false, false,false);			  
		  } catch (Exception ex) {		  
			  //throw new Exception("GD4_Documento::finalizzaGestioneAllegatiTemp Percorsi - Errore: "+ex.getMessage());
			  //In tal caso non faccio niente!
			  //Se sono arrivato fino a qua...sicuramente ho già registrato gli oggetti_file, quindi è tutto ok
			  //non riesco a cancellare i file temporanei, ma poi passa la notte l'agente che li ripilisce, quindi non do errore al documento!
	      }
  }
  
  public void disconnectDbOpAllegatiTemp() {
	  	  if (!bAllegatiTempModulistica) return;
	  		
	      try {dbOpSqlAllegatiTemp.close();}catch (Exception e) {}
  }

  /*
   * METHOD:      update()
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Aggiornamento dei valori del documento
   * 
   * RETURN:      boolean
  */
  private boolean update() throws Exception 
  {
         if (this.getIdDocumento().equals("0"))
             throw new Exception("GD4_Documento::update() IdDocumento richiesto");

         try {
           StringBuffer sStm = new StringBuffer();
         
           IDbOperationSQL dbOpSql = varEnv.getDbOp();
         
           sStm.append("update documenti set ");
           sStm.append("id_libreria = "+ this.getLibreria().getIdLibreria());
           sStm.append(",id_tipodoc = "+ this.getTipoDocumento().getIdTipodoc());
           sStm.append(",utente_aggiornamento = '"+ varEnv.getUser() + "'");
           if (bAggiornaDataUltAgg)
        	   sStm.append(",data_aggiornamento = sysdate");           
           sStm.append(",id_documento_padre = "+ idDocumentoPadre);
           if (conservazione!=null) 
        	   sStm.append(",conservazione='"+conservazione+"' ");
           if (archiviazione!=null) 
        	   sStm.append(",archiviazione='"+archiviazione+"' ");
           sStm.append(" where id_documento = "+ this.getIdDocumento());
           
           dbOpSql.setStatement(sStm.toString());

           elpsTime.start("Aggiornamento tabella Documenti",sStm.toString());
           
           dbOpSql.execute();

           elpsTime.stop();
         }    
         catch (Exception e) {                
                throw new Exception("GD4_Documento::update() " + e.toString());
         }
         
         return true;
  }

  /*
   * METHOD:      retrieveAllValori()
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Carica tutti i valori del documento dal database
   * 
   * RETURN:      void
  */  
  private void retrieveAllValori() throws Exception 
  {
          IDbOperationSQL dbOp = null;
          
          try {
            StringBuffer sStm = new StringBuffer();
            String val_clob;

            dbOp = varEnv.getDbOp();
            
            sStm.append("select v.id_valore, c.id_campo, c.NOME,");
            sStm.append(" v.valore_clob,v.valore_numero,to_char(v.valore_data,f_formato_data(c.id_campo)) valore_data");
            sStm.append("  from valori v, campi_documento c");
            sStm.append(" where c.id_campo = v.id_campo");
            sStm.append("   and (DBMS_LOB.SUBSTR(VALORE_CLOB, 1)  is not null");
            sStm.append("        or v.valore_numero is not null");
            sStm.append("        or v.valore_data is not null)");
            sStm.append("   and v.id_documento = " + this.getIdDocumento());

            dbOp.setStatement(sStm.toString());

            elpsTime.start("Retrieve da tabella Valori",sStm.toString());
            dbOp.execute();
            elpsTime.stop();
            
            ResultSet rst = dbOp.getRstSet();

            while (rst.next()) {
                  A_Valori val;

                  try {  
                    val = (A_Valori)Class.forName(varEnv.Global.PACKAGE + "." + 
                                                  varEnv.Global.DM + "_" +
                                                  varEnv.Global.VALORI).newInstance();
                  }
                  catch (Exception e) {                                  
                    throw new Exception("GD4_Documento::retrieveAllValori - Non riesco a valorizzare l'oggetto di Classe: " + 
                                        varEnv.Global.PACKAGE + "." + varEnv.Global.DM + "_" + 
                                        varEnv.Global.VALORI+" "+e.getMessage());
                  } 
                  try {  
                      val.inizializzaDati(varEnv);
                  
                      val.setIdValore(rst.getString("id_valore"));

                      val_clob = Global.leggiClob(dbOp,"VALORE_CLOB");
                      val.setValore(Global.selezioneValore(val_clob,rst.getString("valore_numero"),rst.getString("valore_data")));
                      
                      val.setValoreBD(rst.getBigDecimal("valore_numero"));
                      
                      val.getCampo().setIdCampo(rst.getString("id_campo"));
                      val.getCampo().setNomeCampo(rst.getString("nome"));
                                            
                      this.getValori().addElement(val);
                  }
                  catch (Exception e) {                                     
                    throw new Exception("GD4_Documento::retrieveAllValori - " + 
                                        "retrieve\n"+e.getMessage());
                  }   
            }

          }
          catch (Exception e) {                              
               throw new Exception("GD4_Documento::retrieveAllValori() " + e.getMessage());
          }
                              
  }
  
  /*
   * METHOD:      retrieveAllValoriH()
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Carica tutti i valori del documento dal database dalla tabella orizzontale
   * 
   * RETURN:      void
  */  
  private void retrieveAllValoriH() throws Exception {
	      IDbOperationSQL dbOp = null;
          Environment env = null;
          try {
        	StringBuffer sStm = new StringBuffer();
            String val_clob;
            Vector vCol = new Vector();
            Vector vType = new Vector();
            Vector vFInfo = new Vector();
            
            nameHorizontalTable=(new LookUpDMTable(varEnv).lookUpAliasOrizzontalTable(this.getIdDocumento()));
            
            dbOp = varEnv.getDbOp();
            
            sStm.append("select COLUMN_NAME, DATA_TYPE, DATA_LENGTH, ");
            sStm.append("       tipo, nvl(formato_data,'dd/mm/yyyy'), nvl(in_uso,'N'),campi_documento.id_campo,nvl(SENZA_SALVATAGGIO,'N'),nvl(SENZA_AGGIORNAMENTO,'N'),campi_documento.nome ");
            sStm.append(" from user_tab_columns,campi_documento,dati_modello,dati ");
            sStm.append(" where table_name='"+nameHorizontalTable+"'");
            sStm.append(" and COLUMN_NAME<>'ID_DOCUMENTO' and COLUMN_NAME<>'FULL_TEXT' and upper(COLUMN_NAME)<>'$ACTIONKEY' and ");
            sStm.append(" upper(campi_documento.nome) = upper(COLUMN_NAME) and ");
           	sStm.append(" campi_documento.ID_TIPODOC = " + this.getTipoDocumento().getIdTipodoc()+" and " );
           	sStm.append(" campi_documento.id_campo = dati_modello.id_campo and ");
            sStm.append(" dati_modello.AREA_DATO = dati.area and ");
            sStm.append(" dati_modello.dato = dati.dato  ");

            dbOp.setStatement(sStm.toString());

            elpsTime.start("Retrieve da campi della tabella "+nameHorizontalTable,sStm.toString());
            dbOp.execute();
            elpsTime.stop();            
            
            ResultSet rst = dbOp.getRstSet();

            StringBuffer sStmTab = new StringBuffer();
            	  
            sStmTab.append("SELECT ");
            
            int index=0;
            
            while (rst.next()) {          
            	  vCol.add(rst.getString(1));
            	  vType.add(rst.getString(2));
            	  if (index!=0) {sStmTab.append(",");}
            	  index=1;            	              	    
            	  
            	  FieldInformation fi=null;
            	  
	              if (rst.getString(4).equals("D")) {	            	   
	                  fi = (new FieldInformation(rst.getString(7),
	                		                       rst.getString(4),
	                		                       Global.replaceAll(rst.getString(5), "hh:", "HH:"),
	                		                       rst.getString(6),
	                		                       rst.getString(8),
	                		                       rst.getString(9)));  
	                  fi.setNomeCampo( rst.getString(10));
	                  
	                  sStmTab.append("TO_CHAR("+rst.getString(1)+",'"+Global.replaceAll(rst.getString(5), "hh:", "hh24:")+"')");
	              }
	              else {
	                  fi = (new FieldInformation(rst.getString(7),
	                		                       rst.getString(4),
	                		                       "",
	                		                       rst.getString(6),
	                		                       rst.getString(8),
	                		                       rst.getString(9)));
	                  
	                  fi.setNomeCampo( rst.getString(10));
	                  sStmTab.append(rst.getString(1));
	              }   
	              
	              vFInfo.add(fi);
            }            
            
            sStmTab.append(" FROM "+nameHorizontalTable);
            sStmTab.append(" WHERE ID_DOCUMENTO="+this.getIdDocumento());

            dbOp.setStatement(sStmTab.toString());
            
            elpsTime.start("Retrieve dei valori della tabella "+nameHorizontalTable,sStm.toString());
            dbOp.execute();
            elpsTime.stop();
            
            ResultSet rstTab = dbOp.getRstSet();
            
            if (!rstTab.next()) return;
            
            /*env = new Environment(varEnv.getUser(),varEnv.getPwd(),null,null,null,dbOp.getConn());
            env.connect();*/
            for(int i=0; i<vCol.size();i++)  {         
            	A_Valori val;

                try {  
                  val = (A_Valori)Class.forName(varEnv.Global.PACKAGE + "." + 
                                                  varEnv.Global.DM + "_" +
                                                  varEnv.Global.VALORI).newInstance();
                }
                catch (Exception e) {                                  
                  throw new Exception("GD4_Documento::retrieveAllValoriH - Non riesco a valorizzare l'oggetto di Classe: " + 
                                        varEnv.Global.PACKAGE + "." + varEnv.Global.DM + "_" + 
                                        varEnv.Global.VALORI+" "+e.getMessage());
                }
                
                try {  
                  val.inizializzaDati(varEnv);                  
                  
                  FieldInformation fi = (FieldInformation)vFInfo.get(i);                                       
                  
                  if (vType.get(i).equals("CLOB")) {
                      val_clob = Global.leggiClob(dbOp,""+vCol.get(i));                    	
                      val.setValore(val_clob);
                  }
                  else {                 
                	  if (rstTab.getString(i+1)==null)
                		  val.setValore(null);
                	  else
                		  val.setValore(rstTab.getString(i+1));
                  }
                  
                  if (vType.get(i).equals("NUMBER")) {                          	
                	  val.setValoreBD(rstTab.getBigDecimal(i+1));
                  }
                  
                  val.setNome(""+vCol.get(i));                  
                  
                  val.setFieldInformation(fi);
                  val.getCampo().setIdCampo(fi.getIdCampo());
                  val.getCampo().setNomeCampo(fi.getNomeCampo());
                  val.setNome(fi.getNomeCampo());
                  
                  this.getValori().addElement(val);
                }
                catch (Exception e) {                                     
                  throw new Exception("GD4_Documento::retrieveAllValoriH - " + 
                                        "retrieve\n"+e.getMessage());
                }
            }
            //env.disconnectClose();
            
          }                      
          catch (Exception e) {    
        	  // try {env.disconnectClose();}  catch (Exception ei) { }
               throw new Exception("GD4_Documento::retrieveAllValoriH() " + e.getMessage());
          }          
  }
  
  /*
   * METHOD:      retrieveAllValoriLog()
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Carica tutti i valori log del documento dal database
   * 
   * RETURN:      void
  */  
  private void retrieveAllValoriLog(String idLog) throws Exception 
  {
	  	  IDbOperationSQL dbOp = null;
	  	  ResultSet rst = null;
          
          try {
        	 StringBuffer sStm = new StringBuffer();
        	 
        	 dbOp = varEnv.getDbOp();
             
        	 //Mi estraggo tutti i campi del documento
             sStm.append("SELECT TO_CHAR(ID_CAMPO) ");
             sStm.append("FROM VALORI ");
             sStm.append("WHERE ID_DOCUMENTO="+this.getIdDocumento());
        	 
             try {
	             dbOp.setStatement(sStm.toString());
	        	 dbOp.execute();            
	             rst = dbOp.getRstSet();
             }
             catch (Exception e) {
            	 throw new Exception("Estrazione Campi del documento\n"+e.getMessage());
             }
        	 
             //I campi estratti li inserisco in un vettore
             Vector vCampi = new Vector();        	 
        	 while (rst.next()) vCampi.add(rst.getString(1));
        	 
        	 //Ciclo su tutti i campi per recuperare il loro
        	 //ultimo valore log
        	 for(int i=0;i<vCampi.size();i++) {
        		 sStm = new StringBuffer();
        		 
        		 //Cerco l'ultimo valore log in ordine di activity log.
        		 //Se non trovo nulla restituisco il campo dalla valori
        	     sStm.append("SELECT NVL (VALOG.ID_VALORE, V.ID_VALORE) ID_VALORE, C.ID_CAMPO, ");
				 sStm.append("		         C.NOME NOME,									   ");
				 sStm.append("			         DECODE (VALOG.ID_VALORE,                      ");
				 sStm.append("			                 NULL, V.VALORE_CLOB,                  ");
				 sStm.append("			                 VALOG.VALORE_CLOB                     ");
				 sStm.append("			                ) VALORE_CLOB,                         ");  
				 sStm.append("			         DECODE (VALOG.ID_VALORE,                      "); 
				 sStm.append("			                 NULL, V.VALORE_NUMERO,                ");
				 sStm.append("			                 VALOG.VALORE_NUMERO                   ");
				 sStm.append("			                ) VALORE_NUMERO,                       ");
				 sStm.append("			         DECODE (VALOG.ID_VALORE,                      ");
				 sStm.append("			                 NULL, TO_CHAR (V.VALORE_DATA, F_FORMATO_DATA (C.ID_CAMPO)),  ");
			     sStm.append("				                 TO_CHAR (VALOG.VALORE_DATA, F_FORMATO_DATA (C.ID_CAMPO)) ");
				 sStm.append("			                ) VALORE_DATA												  ");
				 sStm.append("			    FROM VALORI V, CAMPI_DOCUMENTO C, VALORI_LOG VALOG, ACTIVITY_LOG ALOG     ");
				 sStm.append("			   WHERE C.ID_CAMPO = V.ID_CAMPO                       ");
				 sStm.append("			     AND V.ID_DOCUMENTO = "+this.getIdDocumento()	    );
				 sStm.append("			     AND V.ID_VALORE = VALOG.ID_VALORE(+)              ");
				 sStm.append("			     AND VALOG.ID_LOG = ALOG.ID_LOG(+)                 ");
				 sStm.append("			     AND V.ID_CAMPO = "+vCampi.get(i)                   );
				 if (idLog==null)
					 sStm.append("			ORDER BY NVL (VALOG.ID_LOG, 0) DESC                    ");
				 else
					 sStm.append("			AND VALOG.ID_LOG=  "+idLog);
				 
				 try {
					
					 dbOp.setStatement(sStm.toString());
					 dbOp.execute();            
					 rst = dbOp.getRstSet();
				 }
				 catch (Exception e) {
					 throw new Exception("Estrazione valore log del campo(id): "+vCampi.get(i) +"\n"+e.getMessage());
				 }
					                  
                 if (rst.next()) { 
                	 A_Valori val;

                     try {  
                       val = (A_Valori)Class.forName(varEnv.Global.PACKAGE + "." + 
                                                  varEnv.Global.DM + "_" +
                                                  varEnv.Global.VALORI).newInstance();
                     }
                    catch (Exception e) {                                  
                       throw new Exception("Non riesco a valorizzare l'oggetto di Classe: " + 
                                           varEnv.Global.PACKAGE + "." + varEnv.Global.DM + "_" + 
                                           varEnv.Global.VALORI+" "+e.getMessage());
                    } 
                    try {  
                       val.inizializzaDati(varEnv);
                  
                       val.setIdValore(rst.getString("id_valore"));
                      	
                       String val_clob = Global.leggiClob(dbOp,"VALORE_CLOB");
                       val.setValore(Global.selezioneValore(val_clob,rst.getString("valore_numero"),rst.getString("valore_data")));
                      
                       val.getCampo().setIdCampo(rst.getString("id_campo"));
                       val.getCampo().setNomeCampo(rst.getString("nome"));
                                            
                       this.getValori().addElement(val);
                    }
                    catch (Exception e) {                                     
                       throw new Exception("Aggiunta in vettore valori\n"+e.getMessage());
                    }   
                 }
        	 }	 
          }
          catch (Exception e) {                              
               throw new Exception("GD4_Documento::retrieveValoriLog() " + e.getMessage());
          }          
  }

  /*
   * METHOD:      retrieveAllValoriLogH()
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Carica tutti i valori log del documento dal database (versione orizzontale)
   * 
   * RETURN:      void
  */  
  private void retrieveAllValoriLogH(String idLog) throws Exception 
  {
	      IDbOperationSQL dbOp = null;
	  	  ResultSet rst = null;
          
          try {        	
        	dbOp = varEnv.getDbOp();  
        	  
            StringBuffer sStm = new StringBuffer();        		 
            
            //Cerco l'ultimo valore log in ordine di activity log,    
            //ma se passo l'idLog prendo quello specifico
        	sStm.append("SELECT   valog.id_valore_log, ");
        	sStm.append("         c.id_campo, ");
        	sStm.append("         c.nome nome, ");
        	sStm.append("         valog.valore_clob,valog.valore_numero,to_char(valog.valore_data,f_formato_data(c.id_campo)) valore_data ");
        	sStm.append("FROM tipi_documento td , campi_documento c, valori_log valog, activity_log alog, dati_modello, dati ");
        	sStm.append("WHERE c.ID_TIPODOC  = td.ID_TIPODOC  ");
        	sStm.append("AND alog.id_documento="+this.getIdDocumento()+" ");
        	sStm.append("AND td.ID_TIPODOC = "+this.getTipoDocumento().getIdTipodoc()+" ");
        	sStm.append("AND valog.id_log = alog.id_log ");
        	sStm.append("AND valog.colonna=c.nome ");
        	sStm.append("AND c.id_campo = dati_modello.id_campo ");
        	sStm.append("AND dati_modello.area_dato = dati.area ");
        	sStm.append("AND dati_modello.dato = dati.dato ");
        	if (idLog==null) {
	        	sStm.append("AND valog.id_valore_log = ");
	        	sStm.append("    (select max(id_valore_log) ");
	        	sStm.append("       from valori_log valog2, activity_log alog2");
	        	sStm.append("      where valog2.colonna=c.nome  and  valog2.id_log = alog2.id_log AND  alog2.id_documento="+this.getIdDocumento()+") ");
        	}
        	else 
        		sStm.append(" AND alog.id_log = "+idLog);
			
				
			dbOp.setStatement(sStm.toString());
			dbOp.execute();            
			rst = dbOp.getRstSet();
			
	        while (rst.next()) {
	        	 A_Valori val;
	
	             try {  
	               val = (A_Valori)Class.forName(varEnv.Global.PACKAGE + "." + 
	                                          varEnv.Global.DM + "_" +
	                                          varEnv.Global.VALORI).newInstance();
	             }
	            catch (Exception e) {                                  
	               throw new Exception("Non riesco a valorizzare l'oggetto di Classe: " + 
	                                   varEnv.Global.PACKAGE + "." + varEnv.Global.DM + "_" + 
	                                   varEnv.Global.VALORI+" "+e.getMessage());
	            } 
	            try {  
	               val.inizializzaDati(varEnv);	          	               
	              	
	               String val_clob = Global.leggiClob(dbOp,"VALORE_CLOB");
	               val.setValore(Global.selezioneValore(val_clob,rst.getString("valore_numero"),rst.getString("valore_data")));
	              
	               val.getCampo().setIdCampo(rst.getString("id_campo"));
	               val.getCampo().setNomeCampo(rst.getString("nome"));
	                                    
	               this.getValori().addElement(val);
	            }
	            catch (Exception e) {                                     
	               throw new Exception("Aggiunta in vettore valori\n"+e.getMessage());
	            }   
	        }			

          }
          catch (Exception e) {                              
            throw new Exception("GD4_Documento::retrieveAllValoriLogH() " + e.getMessage());
          }            
  }
  
  /**
   * Effettua il recupero di tutti i valori log e li divide per testata
   * (restituisce un vettore con un elemento per ogni testata di activity_log)
   * 
   * @return Vettore di ValoriLogStruct
   * @throws Exception
  */
  public  Vector retrieveListaValoriLog() throws Exception {
	      Vector<ValoriLogStruct> vValStruct = new Vector<ValoriLogStruct>();
	      
	      IDbOperationSQL dbOp = null;
	  	  ResultSet rst = null;
	  	
	  	  dbOp = varEnv.getDbOp(); 
	  	  
	  	  try {	  		  
            StringBuffer sStm = new StringBuffer();        		 
	        
	        sStm.append("SELECT ID_LOG,TIPO_AZIONE,TO_CHAR(DATA_AGGIORNAMENTO,'dd/mm/yyyy hh24:mi:ss'),UTENTE_AGGIORNAMENTO ");
	    	sStm.append("  FROM activity_log act ");
	    	sStm.append(" WHERE act.id_documento = "+this.getIdDocumento());
            sStm.append(" ORDER BY 1 ");  
            
            dbOp.setStatement(sStm.toString());
			dbOp.execute();            
			rst = dbOp.getRstSet();
	        
			//Riempio la testata dei vari activity_log
			while (rst.next()) vValStruct.add(new ValoriLogStruct(rst.getString(1),rst.getString(2),
					                                              rst.getString(3),rst.getString(4)));
			
			//Mi costruisco la select sui valoriLog
			StringBuffer sStmValog = new StringBuffer();
			
			sStmValog.append("SELECT COLONNA, NVL(dbms_lob.substr(valore_clob,4000),");
			sStmValog.append("                NVL(TO_CHAR(VALORE_DATA, 'dd/mm/yyyy hh24:mi:ss'),");
			sStmValog.append("                TO_CHAR(VALORE_NUMERO) )) ");
			sStmValog.append("FROM VALORI_LOG ");			
			sStmValog.append("WHERE ID_LOG=:P_IDLOG AND COLONNA IS NOT NULL ");
			sStmValog.append("UNION ");
			sStmValog.append("SELECT NOME, NVL(dbms_lob.substr(vl.valore_clob,4000),");
			sStmValog.append("             NVL(TO_CHAR(vl.VALORE_DATA, 'dd/mm/yyyy hh24:mi:ss'),");
			sStmValog.append("                TO_CHAR(vl.VALORE_NUMERO) )) ");
			sStmValog.append("FROM VALORI_LOG vl, VALORI, CAMPI_DOCUMENTO ");
			sStmValog.append("WHERE ID_LOG=:P_IDLOG AND COLONNA IS NULL AND ");
			sStmValog.append("VALORI.ID_VALORE=vl.ID_VALORE AND ");
			sStmValog.append("CAMPI_DOCUMENTO.ID_CAMPO=VALORI.ID_CAMPO ");
			sStmValog.append("ORDER BY 1");
			//FINE COSTRUZIONE QUERY VALOG
			
			//Per ogni testata riempio i campi su valoriLog da ogni activityLog
			for(int i=0;i<vValStruct.size();i++) {				
				ValoriLogStruct vStruct = vValStruct.get(i);				
				dbOp.setStatement(sStmValog.toString());				
				dbOp.setParameter(":P_IDLOG",Long.parseLong(vStruct.getIdLog()));				
				dbOp.execute();           
				
				rst = dbOp.getRstSet();
				
				while(rst.next()) vStruct.getHmCampiValori().put(rst.getString(1),rst.getString(2));
				
				vValStruct.set(i,vStruct);
			}
	  	  }		
	  	  catch (Exception e) {                              
	  		throw new Exception("GD4_Documento::retrieveListaValoriLog() " + e.getMessage());
	  	  }  
	      
	      return vValStruct;
  }

  public List<String> moveFile(String idObjFile) throws Exception {
        IDbOperationSQL dbOp = null;
        ResultSet rst = null;
        List<String> listaFileFsDaCancellare = new ArrayList<String>();

        dbOp = varEnv.getDbOp();


        //1. controllo se su questo documento esiste un file con lo stesso nome....operazione non permessa
        String sqlCheckNomeFile ="Select count(*) from oggetti_file ogfiQuestoDocumento, oggetti_file ogfiDaSpostare "
            + "where ogfiQuestoDocumento.id_documento = "+this.getIdDocumento()+" and ogfiDaSpostare.id_oggetto_file = "+idObjFile
            +" and ogfiQuestoDocumento.filename=ogfiDaSpostare.filename " ;
        dbOp.setStatement(sqlCheckNomeFile);
        dbOp.execute();
        rst = dbOp.getRstSet();
        rst.next();
        if (rst.getLong(1)>0) {
            throw new Exception("Impossibile spostare il file con id "+idObjFile+" sul documento "+this.getIdDocumento()+" perché quest'ultimo "+
                "contiene già un file con lo stesso nome");
        }

        GD4_Oggetti_File fileDaSpostare = new GD4_Oggetti_File();
        fileDaSpostare.inizializzaDati(varEnv);
        fileDaSpostare.setIdOggettoFile(idObjFile);
        fileDaSpostare.retrieve();

        //3. parte file su FS
        if (!Global.nvl(fileDaSpostare.getPathObjFile(),"").equals("")) {
            FileInputStream isFile = null;
            //Sposto il file sul db. Poi ci penserà il gdmsyncro a rispostarlo su FS (se il nuovo doc sta su FS)
            try {
                String filePath=fileDaSpostare.getPathFS(false);
                listaFileFsDaCancellare.add(filePath);
                isFile = new FileInputStream(fileDaSpostare.getPathFS(false));
                String nullStr=null;

                String sqlUpdateTestoOcr="UPDATE OGGETTI_FILE SET TESTOOCR = :TESTOOCR, PATH_FILE=:PATH_FILE, PATH_FILE_ROOT=null, PATH_FILE_ROOT_ORACLE=null, \"FILE\"=null WHERE ID_OGGETTO_FILE="+idObjFile;
                dbOp.setStatement(sqlUpdateTestoOcr);
                dbOp.setParameter(":TESTOOCR",isFile, isFile.available());
                dbOp.setParameter(":PATH_FILE", nullStr);
                dbOp.execute();
            }
            finally {
               try { isFile.close();}catch (Exception e){}
            }
        }

        //*************OGGETTI LOG
        String sqlListaOgfiLogSuFS="select ID_LOG from oggetti_file_log where id_oggetto_file = "+idObjFile;
        dbOp.setStatement(sqlListaOgfiLogSuFS);
        dbOp.execute();
        rst = dbOp.getRstSet();
        List<String> listaIdLog = new ArrayList<String>();
        while (rst.next()) {
            listaIdLog.add(rst.getString(1));
        }

        for(int i=0;i<listaIdLog.size();i++) {
          String idLogPartenza = listaIdLog.get(i);

          fileDaSpostare = new GD4_Oggetti_File();
          fileDaSpostare.inizializzaDati(varEnv);
          fileDaSpostare.setIdOggettoFile(idObjFile);
          fileDaSpostare.retrieveLog(idLogPartenza);

          FileInputStream isFile = null;
          try {
              if (!Global.nvl(fileDaSpostare.getPathObjFile(),"").equals("")) {

                  //Sposto il file sul db. Poi ci penserà il gdmsyncro a rispostarlo su FS (se il nuovo doc sta su FS)

                  String filePath=fileDaSpostare.getPathFS(false);
                  listaFileFsDaCancellare.add(filePath);
                  String nullStr=null;

                  isFile = new FileInputStream(fileDaSpostare.getPathFS(false));
                  String sqlUpdateOggettiLogIdLog="UPDATE OGGETTI_FILE_LOG SET";
                  sqlUpdateOggettiLogIdLog+=" TESTOOCR = :TESTOOCR, PATH_FILE=:PATH_FILE, PATH_FILE_ROOT=null, PATH_FILE_ROOT_ORACLE=null, \"FILE\"=null ";
                  sqlUpdateOggettiLogIdLog+=" where id_oggetto_file="+idObjFile+" and id_log = "+idLogPartenza;

                  dbOp.setStatement(sqlUpdateOggettiLogIdLog);
                  dbOp.setParameter(":TESTOOCR", isFile, isFile.available());
                  dbOp.setParameter(":PATH_FILE", nullStr);
                  dbOp.execute();
              }
          }
          finally {
              try { isFile.close();}catch (Exception e){}
          }
      }


        for(int i=0;i<listaIdLog.size();i++) {
          //Per ogni log devo duplicare l'activity log verso il nuovo documento
          //ed appendergli il relativo oggetto file log a cui aggiungerò
          //il relativo file sul blob se quello di partenza era su FS
          //quindi cancellare il file log di partenza
          String idLogPartenza = listaIdLog.get(i);

          fileDaSpostare = new GD4_Oggetti_File();
          fileDaSpostare.inizializzaDati(varEnv);
          fileDaSpostare.setIdOggettoFile(idObjFile);
          fileDaSpostare.retrieveLog(idLogPartenza);

          String newIdLog = ""+dbOp.getNextKeyFromSequence("ACLO_SQ");


          String sqlActivityLog = "insert into activity_log (ID_LOG, ID_DOCUMENTO, TIPO_AZIONE, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO) "+
              " select "+newIdLog+", "+this.getIdDocumento()+", TIPO_AZIONE, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO "+
              " from activity_log where id_log= "+idLogPartenza;

          dbOp.setStatement(sqlActivityLog);
          dbOp.execute();

          String sqlUpdateOggettiLogIdLog=" update oggetti_file_log set ID_LOG= "+newIdLog;
          sqlUpdateOggettiLogIdLog+=" where id_oggetto_file="+idObjFile+" and id_log = "+idLogPartenza;
          dbOp.setStatement(sqlUpdateOggettiLogIdLog);
          dbOp.execute();
      }


      String sqlUpdateOggettiFile = "update oggetti_file set id_documento=" + this.getIdDocumento() +
          " where id_oggetto_file=" + idObjFile;
      String sqlUpdateImpronteFile = "update IMPRONTE_FILE set id_documento=" +  this.getIdDocumento()  +
          " where id_documento = (select id_documento from oggetti_file where id_oggetto_file=" + idObjFile + ")" +
          " and filename = (select filename from oggetti_file where id_oggetto_file=" + idObjFile + ")";

      dbOp.setStatement(sqlUpdateImpronteFile);
      dbOp.execute();
      dbOp.setStatement(sqlUpdateOggettiFile);
      dbOp.execute();

      return listaFileFsDaCancellare;
  }

  
  /*
   * METHOD:      retrieveViewValori()
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Carica tutti i valori del documento dal database
   * 
   * RETURN:      void
  */  
  private void retrieveViewValori() throws Exception 
  {
          IDbOperationSQL dbOp = null;
         
          try {
            StringBuffer sStm = new StringBuffer();

            dbOp = varEnv.getDbOp();

            sStm.append("SELECT ID_VALORE FROM VALORI V, CAMPI_DOCUMENTO C ");
            sStm.append("WHERE V.ID_CAMPO=C.ID_CAMPO AND FLAG_VIEW='S' ");
            sStm.append("AND ID_DOCUMENTO = " + this.getIdDocumento());

            dbOp.setStatement(sStm.toString());

            elpsTime.start("Retrieve da tabella Documenti",sStm.toString());
            dbOp.execute();
            elpsTime.stop();

            ResultSet rst = dbOp.getRstSet();

            while (rst.next()) {
                  A_Valori val;
                  
                  try {  
                      val = (A_Valori)Class.forName(varEnv.Global.PACKAGE + "." + 
                                                    varEnv.Global.DM + "_" +
                                                    varEnv.Global.VALORI).newInstance();
                  }
                  catch (Exception e) {                    
                      throw new Exception("GD4_Documento.retrieveViewValori - Non riesco a creare l'oggetto di Classe: " + 
                                          varEnv.Global.PACKAGE + "." + varEnv.Global.DM + "_" +
                                          varEnv.Global.VALORI);                    
                  }    

                  try {  
                      val.inizializzaDati(varEnv);
                  }
                  catch (Exception e) {                                           
                      throw new Exception("GD4_Documento.retrieveViewValori - inizializzaDati\n"+e.getMessage());
                  }

                  try {
                      val.setIdValore(rst.getString(1));
                      
                     
                      
                      val.retrieve();
                      this.getValori().addElement(val);
                  }
                  catch (Exception e) {                                           
                      throw new Exception("GD4_Documento.retrieveViewValori - Retrieve Valori\n"+e.getMessage());
                  }
            }
          }
          catch (Exception e) {                              
               throw new Exception("GD4_Tipo_Documento::retrieveMandatoryValori() " + e.getMessage());
          }
                         
  }

  /*
   * METHOD:      retrieveAllOggettiFile()
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Carica tutti gli oggetti file del documento dal DB
   * 
   * RETURN:      void
  */ 
  private void retrieveAllOggettiFile(boolean bCaricaFile, boolean bFlagLog, String idLog) throws Exception 
  {
          IDbOperationSQL dbOp = null;
          
          try {
            
        	removeOggettiFile();          
        	
        	if (letturaAllegati==0) return;
            
            dbOp = varEnv.getDbOp();
            
            
            StringBuffer sStm = new StringBuffer();
           

            if (!bFlagLog) {
            	sStm.append((new DocUtil()).getSQLElencoAllegatiDocumento(this.getIdDocumento()));	  
            }
            else {
            	sStm.append("select oggetti_file_log.id_oggetto_file,");
	            sStm.append("nvl(f.id_formato,0),oggetti_file_log.filename,oggetti_file_log.\"FILE\",");
	            sStm.append("oggetti_file_log.testoocr,oggetti_file_log.allegato,to_number(null),NVL(F.VISIBILE,'S'),nvl(oggetti_file_log.PATH_FILE,''), ");
	            sStm.append("NVL(F.ICONA, 'generico.gif'),activity_log.id_log,'N' dacancellare, ");
	            sStm.append("F_GETIMPOSTAZIONI_GDMSYNCRO impostsyncto, to_number(null) as id_syncro, null  ID_SERVIZIO_ESTERNO, null   CHIAVE_SERVIZIO_ESTERNO, ");
                sStm.append(" nvl(oggetti_file_log.PATH_FILE_ROOT,'') pathFileRoot ");
	            sStm.append(" from oggetti_file_log, activity_log, formati_file f"); 
	            sStm.append(" where activity_log.id_documento = " + this.getIdDocumento() );
	            sStm.append(" and activity_log.id_log=oggetti_file_log.id_log " );
	            sStm.append(" and oggetti_file_log.nome_formato = f.nome (+) " );
	            if (idLog==null)
	            	sStm.append(" and activity_log.id_log=(select max(id_log) from activity_log where id_documento="+this.getIdDocumento()+") " );
	            else
	            	sStm.append(" and activity_log.id_log="+idLog );	            
	            sStm.append(" order by filename");
            }
            
            dbOp.setStatement(sStm.toString());

            elpsTime.start("Retrieve da tabella Oggetti File",sStm.toString());
            dbOp.execute();
            elpsTime.stop();
            
            ResultSet rst = dbOp.getRstSet();

            while (rst.next()) {
                  GD4_Oggetti_File oFile;
                  try {
                    String pathFileAreaScrittoInOggettiFile=Global.nvl(rst.getString("pathFileRoot"),"");

                    String pathFileAreaLocale = pathFileAreaAree;

                    if (!pathFileAreaScrittoInOggettiFile.equals(""))  pathFileAreaLocale=pathFileAreaScrittoInOggettiFile;

                    oFile = (GD4_Oggetti_File)Class.forName(varEnv.Global.PACKAGE + "." + 
                                                          varEnv.Global.DM + "_" +
                                                          varEnv.Global.OGGETTI_FILE).newInstance();
                    oFile.inizializzaDati(varEnv);
                    oFile.setIdOggettoFile(rst.getString(1));
                    oFile.setIdFormato(rst.getString(2));                     
                    oFile.setFileName(rst.getString(3));
                    oFile.setAllegato(rst.getString(6));
                    
                    oFile.setIdOggettoFilePadre(rst.getString(7));
                    
                    oFile.setVisible(rst.getString(8));                    
                    oFile.setIcona(rst.getString(10));
                    oFile.setIdLog(rst.getString(11));
                      
                    oFile.setIdSyncro(Global.nvl(rst.getString("id_syncro"),""));
                    oFile.setIdServizioEsterno(Global.nvl(rst.getString("ID_SERVIZIO_ESTERNO"),""));
                    oFile.setChiaveServizioEsterno(Global.nvl(rst.getString("CHIAVE_SERVIZIO_ESTERNO"),""));
                    
                    oFile.setImpostazioniSyncro(rst.getString("impostsyncto"));
                     
                    oFile.setDacancellare(rst.getString("dacancellare"));
                    
                    if (!bFlagLog) oFile.setDataAggiornamento(rst.getString("dataAgg"));
                    
                    if (bCaricaFile) {
                      //Gestisco il file su BLOB
                                          	
                      if (Global.nvl(pathFileAreaLocale,"").equals("") || rst.getString(9)==null) {
                    	  	 InputStream is=null;
	                    	  try {			                    	  
	                    		 is=dbOp.readBlob(5);
	                    	  }
	                    	  catch (NullPointerException e) {  
	                    		  //DONTCARE
	                    	  }
		                      if (is!=null) {	                    		  
		                    	  oFile.setFile(is);	                    	 
		                       //  oFile.setFile((InputStream)rst.getBinaryStream(5));
		                        // oFile.setFileStreamAllinizio(true);
		                      }
		                      else if (!varEnv.Global.MANAGE_TYPE_FILE.equals(Global.MANAGE_TYPE_BLOB))
		                      {   
		                         ((GD4_Oggetti_File)oFile).caricaBFile(rst);
		                      }
		                      
		                      ((GD4_Oggetti_File)oFile).setPathFileArea("");
		                      ((GD4_Oggetti_File)oFile).setArcmcr("");
		                      ((GD4_Oggetti_File)oFile).setPathObjFile("");

                      }
                      //Gestisco il file su sistemi esterni
                      else if (  (! Global.nvl(oFile.getIdSyncro(),"").equals(""))  && (! Global.nvl(oFile.getServlet_Imposyncro(),"").equals("")) ) {
                    	  //NOn faccio niente...ci pensa poi al momento di tirare il file la getFile.. del singolo oggettofile
                      }
                      //Gestisco il file su FS  
                      else {
	                      ((GD4_Oggetti_File)oFile).setPathFileArea(pathFileAreaLocale);
	                      ((GD4_Oggetti_File)oFile).setArcmcr(arcmcr);
	                      ((GD4_Oggetti_File)oFile).setPathObjFile(rst.getString(9));                    	  
                      }
                    }

                    try {
                      ///oFile.retrieve();
                      this.getOggettiFile().addElement(oFile);
                    }
                    catch (Exception e) {                      
                        throw new Exception("GD4_Documento::retrieveAllOggettiFile() - Retrieve" + e.getMessage());
                    }
                    
                  }
                  catch (Exception e) {   
                    throw new Exception("GD4_Documento::retrieveAllOggettiFile() \n" +e.getMessage());
                  }     
            }
          }
          catch (Exception e) {
               throw new Exception("GD4_Documento::retrieveAllOggettiFile() " + e.getMessage());
          }
          
  }

  /*
   * METHOD:      verifyMandatoryCampi(String)
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Verifica se tutti i campi mandatory siano presenti
   *              nella lista valori caricata in memoria
   * RETURN:      void
  */ 
  private void verifyMandatoryCampi(String sInput) throws Exception 
  {
            IDbOperationSQL dbOp = null;
            StringBuffer sStm = new StringBuffer();
            ResultSet rst;                                  	
            
            try {
                dbOp = varEnv.getDbOp();

                sStm.append("select id_campo from campi_documento c");
                sStm.append(" where c.obbligatorio = 'S'");
                sStm.append("   and id_tipodoc = " + this.getTipoDocumento().getIdTipodoc());
                
                elpsTime.start("verifyMandatoryCampi",sStm.toString());
                
                dbOp.setStatement(sStm.toString());

                dbOp.execute();

                rst = dbOp.getRstSet();
            }
            catch (Exception e) {               
               throw new Exception("GD4_Documento::verifyMandatoryValori() " + e.getMessage());
            }
            
            if (sInput.equals("INSERT")) {
                while (rst.next()) {
                       A_Valori val = findCampo(rst.getString(1));
                       if (val==null ||val.getValore()==null||val.getValore().equals("")) {
                          throw new Exception("GD4_Documento::verifyMandatoryValori() Attenzione Non tutti i campi obbligatori sono stati inseriti");
                       }
                }
            }
            else{
                while (rst.next()) {
                       A_Valori val = findCampo(rst.getString(1));

                       if (val==null) continue;
                       
                       if (val.getValore()==null||val.getValore().equals("")) {
                          throw new Exception("GD4_Documento::verifyMandatoryValori() Il campo " + val.getCampo().getNomeCampo()+ " non può essere vuoto!");
                       }
                }
            }
            
            elpsTime.stop();
            
  }

  /*
   * METHOD:      findCampo(String)
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Trova e restituisce un A_Valore nella lista
   *              dei valori dato l'id campo
   *
   * RETURN:      A_Valori
  */ 
   private A_Valori findCampo(String idCampo) 
  {
         int conta=0,size=this.getValori().size();
         Object obj;
         
         while (conta!=size) {
               obj = this.getValori().elementAt(conta++);
               A_Valori val = (A_Valori)obj;
               if (val.getCampo().getIdCampo().equals(idCampo)) return val;
         }

         return null;         
  }
   
  private A_Oggetti_File findOggettoFileByName(String nomeFile) throws Exception {      
          int conta=0,size=oggettiFile.size();
          Object obj;

          if (size==0)
             return null;
                                 
          while (conta!=size) {
                obj = oggettiFile.elementAt(conta++);
                A_Oggetti_File oFile = (A_Oggetti_File)obj;
                if (oFile.getFileName().compareTo(nomeFile) == 0) return oFile;
          }
          
          return null;     
   }
   
 
  /*
   * METHOD:      generaCodiceRichiesta()
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Genera e restituisce un codice richiesta interno
   *
   * RETURN:      String
  */
  private void generaCodiceRichiesta() throws Exception
  {
       ResultSet rst;       
  
       if (this.codRich!=null) {
         try {
             IDbOperationSQL dbOpSql = varEnv.getDbOp();
                          
             String sStm = "select 1 ";
             sStm += "from richieste ";
             sStm += "where area='"+this.areaDoc+"' ";
             sStm += "and CODICE_RICHIESTA='"+Global.replaceAll(this.codRich,"'","''")+"'";
               
             dbOpSql.setStatement(sStm.toString());
             
             elpsTime.start("GENERAZIONE DEL COD_RICH",sStm.toString());
             
             dbOpSql.execute();
             
             elpsTime.stop();
             
             rst = dbOpSql.getRstSet();
             
             if (rst.next()) return;
          } 
            catch (Exception e) {
             throw new Exception("GD4_Documento::genCodiceRichiesta - controllo univocità AREA-CR" + e.getMessage());
          }   
       }
       
  
       try {
           IDbOperationSQL dbOpSql = varEnv.getDbOp();
                                           
           if (this.codRich==null) {
        	  String sTipoUso=(new LookUpDMTable(varEnv)).lookUpTipoUsoAreaModello(tipoDocumento.getIdTipodoc());
        	  String idCodRic;
        	  
        	  if (sTipoUso.equals("F") || sTipoUso.equals("C"))
        		 idCodRic =  ""+dbOpSql.getNextKeyFromSequence("CART_SQ");
        	  else if (sTipoUso.equals("V") || sTipoUso.equals("Q"))
        		  idCodRic =  "-"+dbOpSql.getNextKeyFromSequence("QRY_SQ");
        	  else
                  idCodRic = "DMSERVER"+dbOpSql.getNextKeyFromSequence("CODR_SQ");
        	  
              this.codRich = idCodRic;                          
           }           
           String sStm = "insert into RICHIESTE (CODICE_RICHIESTA, AREA, DATA_INSERIMENTO)";
           sStm += "values ( '"+Global.replaceAll(this.codRich,"'","''")+"','"+this.getArea()+"',sysdate)";
             
           dbOpSql.setStatement(sStm.toString());
           dbOpSql.execute();
                    
        } 
          catch (Exception e) {
           throw new Exception("GD4_Documento::genCodiceRichiesta()" + e.getMessage());
        }
  }
  
  private void retrieveRiferimenti() throws Exception {
        
        try {
           GD4_Riferimento gd4rif = new GD4_Riferimento(this.getIdDocumento());
           
           gd4rif.inizializzaDati(varEnv);
           
           gd4rif.retrieveRiferimento();
          
           related= gd4rif.getVectorRiferimenti();
          
         }    
         catch (Exception e) {                              
               throw new Exception("GD4_Documento::retrieveRiferimenti() " + e.getMessage());
         }                
  }
  
  private void retrieveRiferimentiFrom() throws Exception {
        
          try {
           GD4_Riferimento gd4rif = new GD4_Riferimento(this.getIdDocumento());
           
           gd4rif.inizializzaDati(varEnv);
           
           gd4rif.retrieveRiferimentoFrom();
          
           relatedFrom= gd4rif.getVectorRiferimentiFrom();
          
          }    
          catch (Exception e) {                              
               throw new Exception("GD4_Documento::retrieveRiferimentiFrom() " + e.getMessage());
          }                
  }
  
  public Vector listaDiscendenti(int livelloDiscendenti)  throws Exception {
	     Vector vDiscendenti = new Vector();
	     
         try {
           ResultSet rst;
           
           IDbOperationSQL dbOpSql = varEnv.getDbOp();
                          
           String sStm = "SELECT ID_DOCUMENTO FROM DOCUMENTI ";           
           sStm += "START WITH ID_DOCUMENTO_PADRE= "+this.getIdDocumento()+" ";
           sStm += "CONNECT BY PRIOR ID_DOCUMENTO=ID_DOCUMENTO_PADRE ";
           
           if (livelloDiscendenti==1)
        	   sStm += "AND LEVEL <= 1 ";
               
           dbOpSql.setStatement(sStm.toString());
             
           elpsTime.start("GENERAZIONE lista dei Discendenti",sStm.toString());
             
           dbOpSql.execute();
             
           elpsTime.stop();
             
           rst = dbOpSql.getRstSet();
             
           while (rst.next()) vDiscendenti.add(rst.getString(1));
           
        } 
        catch (Exception e) {                       
        	throw new Exception("GD4_Documento::listaDiscendenti " + e.getMessage());
        }   	     
	     
	    return vDiscendenti;
  }
  
  public void retrieveLinks(String area, String cm) throws Exception {
	     if (this.getIdDocumento().equals("0")) return;
	     
	     IDbOperationSQL dbOp = null;
         links = new Hashtable();
         
         try {        	 
           dbOp = varEnv.getDbOp();
           
           StringBuffer sStm = new StringBuffer("SELECT ");
           
           sStm.append("LINKS.ID_CARTELLA,CARTELLE.ID_DOCUMENTO_PROFILO ");
           sStm.append("FROM LINKS,CARTELLE,DOCUMENTI, TIPI_DOCUMENTO ");
           sStm.append("WHERE ID_OGGETTO="+this.getIdDocumento()+" ");
           sStm.append("  AND TIPO_OGGETTO='D'");
           sStm.append("  AND LINKS.ID_CARTELLA=CARTELLE.ID_CARTELLA");
           sStm.append("  AND CARTELLE.ID_DOCUMENTO_PROFILO=DOCUMENTI.ID_DOCUMENTO");
           sStm.append("  AND DOCUMENTI.ID_TIPODOC=TIPI_DOCUMENTO.ID_TIPODOC");
           sStm.append("  AND NVL(CARTELLE.STATO,'BO')<>'"+Global.STATO_CANCELLATO+"'");
           if (area!=null)
        	   sStm.append("  AND DOCUMENTI.AREA='"+area+"'");
           if (cm!=null)
        	   sStm.append("  AND TIPI_DOCUMENTO.NOME='"+cm+"'");
           if (!varEnv.getByPassCompetenze()) {
		   	   sStm.append("  AND GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI',");
		   	   sStm.append("  								    CARTELLE.ID_DOCUMENTO_PROFILO,");
		   	   sStm.append("  								    'L',");
		   	   sStm.append("  								    '"+varEnv.getUser()+"',");
		   	   sStm.append("  								    F_TRASLA_RUOLO('"+varEnv.getUser()+"','GDMWEB','GDMWEB'),");
		   	   sStm.append("  								    TO_CHAR(SYSDATE,'dd/mm/yyyy')) = 1 ");
		   	   sStm.append("");
		   	}
            
           dbOp.setStatement(sStm.toString());
           dbOp.execute();
           
           ResultSet rst = dbOp.getRstSet();          
         
           while (rst.next()) links.put(""+rst.getLong(1),""+rst.getLong(2));
           
         }
         catch (Exception e) {
               throw new Exception("GD4_Documento::retrieveLinks()\n" + e.getMessage());
         }
  }
  
  private void removeOggettiFile()  throws Exception {
	  	  Vector<GD4_Oggetti_File> vObjFileAppoggio = new Vector<GD4_Oggetti_File>();
	      for(int i=0;i<this.getOggettiFile().size();i++) {
	    	  GD4_Oggetti_File oFile=(GD4_Oggetti_File)this.getOggettiFile().get(i);
	    	  
	    	  if (oFile.isOggettoFileTemp()) { 
	    		  vObjFileAppoggio.add(oFile);
	    		  continue;
	    	  }
	    	  
	    	  oFile.closeFile(false);      	          	 
	      }	      	    
	      
	      this.getOggettiFile().removeAllElements();
	      this.setOggettiFile(null);
	      this.setOggettiFile(vObjFileAppoggio);	      
  }
  
  public void syncroFS(boolean bSonoInInsert) throws Exception {
	     gestObjFile.setPathFileArea(pathFileArea);
	  	 gestObjFile.setArcmcr(arcmcr);
	  	 gestObjFile.setIdDoc(this.getIdDocumento());
	  	 gestObjFile.setVAllObjFile(this.getOggettiFile());
	     gestObjFile.setListaIdFileToLog(this.listaIdFileToLog);
	     gestObjFile.setOgfiLog(bogfilog);
	  	//this.listaIdFileToLog
	  	 if ( (!bSonoInInsert)) gestObjFile.setDmALog(dmALog);
	  	 
	  	 try {
	  		gestObjFile.syncroFs(this.cancellaAllegati);
	  	 }
	  	 catch (Exception e) {
	  		if (e.getMessage().indexOf(Global.CODERROR_SAVEDOCUMENT_COMPALLEGATI)!=-1) {
	  			 String error="Attenzione! l'utente "+varEnv.getUser()+" non possiede le competenze per poter inserire/aggiornare gli allegati. Impossibile procedere.";
       		     codeErrorSaveDoc=Global.CODERROR_SAVEDOCUMENT_COMPALLEGATI;
     		     descrErrorSaveDoc=error;
     		     throw new Exception("GD4_Documento::syncroFS - Errore in gestObjFile.syncroFs: "+error);
	  		}
	  		else {
	  			throw new Exception("GD4_Documento::syncroFS - Errore in gestObjFile.syncroFs: "+e.getMessage());
	  		}
	  	 }
	  		 
	  
  }
  
  private void rt_rebuild_index() throws Exception {
	  	  try {
	  		 // (new ManageConnection(varEnv.Global)).rt_rebuild_index(varEnv.getDbOp()); 
          }    
          catch (Exception e) {                
                throw new Exception("GD4_Documento::rt_rebuild_index() "+ e.getMessage());
          }     
  }
  
  private boolean isInAllegatiModificati(String name) {
	  	  for(int i=0;i<vElencoNomiAllegatiModificati.size();i++)
	  		  if (vElencoNomiAllegatiModificati.get(i).equals(name)) return true;
	  	  
	  	  return false;
  }

  public String getCodeError() {
	     return codeError;
  }
  

  

}