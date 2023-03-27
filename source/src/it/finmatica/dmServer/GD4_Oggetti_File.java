package it.finmatica.dmServer;

/*
 * GESTIONE DEGLI OGGETTI FILE
 * NEL DM DI FINMATICA
 * 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   16/09/2005
 * 
 * */

import java.io.*;
import java.net.MalformedURLException;
import java.sql.*;
import java.util.Vector;

import it.finmatica.jfc.dbUtil.*;
import it.finmatica.jfc.io.*;
import it.finmatica.dmServer.gdmSyncro.GDMSyncroCore;
import it.finmatica.dmServer.util.*;
import oracle.jdbc.driver.*;
import oracle.sql.BFILE;

public class GD4_Oggetti_File extends A_Oggetti_File
{
  // variabili  
  private String oggettoPadre;
  private ElapsedTime elpsTime;  
  private DMActivity_Log dmALog;  
  private String pathFileArea="";
  private String forceFileOnBlob="0";
  private String arcmcr = "";
  private String mark_as="";
  private String pathObjFile="";
  private String acrarea = "";
  private String acrmodello = "";
  
  private String effectiveAction="";
  private boolean bLogEseguito=false;
  



  private boolean bogfilog=true;
  


  private String idSyncro;
  private String idServizioEsterno;
  private String chiaveServizioEsterno;

  



private String userGDM_Imposyncro;
  private String dsn_Imposyncro;
  private String servlet_Imposyncro;
  
  // ***************** METODI DI INIZIALIZZAZIONE ***************** // 
 
  /*
   * METHOD:      Constructor
   *
   * DESCRIPTION: Inizializza dati
   * 
   * RETURN:      none
  */
  public GD4_Oggetti_File() { }

  /*
   * METHOD:      inizializzaDati(Object, Object)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: inizializza le variabili di connessione
   *              e di documento
   * 
   * RETURN:      void
  */  
  public void inizializzaDati(Object  vEnv)
  {
         this.inizializzaDati((Environment) vEnv);
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
  private void inizializzaDati(Environment vEnv)
  {         
         this.varEnv = vEnv;
         this.modificato = "N";
         elpsTime = new ElapsedTime("GD4_OGGETTI_FILE",vEnv);
  }

  // ***************** METODI DI GESTIONE OGGETTI FILE ***************** //

  /*
   * METHOD:      isVisible() 
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Controlla se il file è visibile in
   *              funzione della colonna visibile su
   *              formati_file
   *
   * RETURN:      boolean
  */
  public boolean isVisibleVariable() throws Exception 
  {
	  	 return isVisible;
  }
  
  /*
   * METHOD:      isVisible() 
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Controlla se il file è visibile in
   *              funzione della colonna visibile su
   *              formati_file
   *
   * RETURN:      boolean
  */
  public boolean isVisible() throws Exception 
  {
         IDbOperationSQL dbOp = null;

         if (this.getIdOggettoFile().equals("0")) return false;

         try {
           StringBuffer sStm = new StringBuffer();
           String sVisible;
                 
           dbOp = varEnv.getDbOp();

           sStm.append("select visibile");
           sStm.append(" from oggetti_file, formati_file");
           sStm.append(" where oggetti_file.id_formato=formati_file.id_formato");
           sStm.append("   and oggetti_file.id_oggetto_file = " + this.getIdOggettoFile());

           dbOp.setStatement(sStm.toString());

           dbOp.execute();

           ResultSet rst = dbOp.getRstSet();

           rst.next();

           sVisible=rst.getString(1);           

           if (sVisible.equals("S")) return true;
           else return false;
              
         }
         catch (Exception e) {
               throw new Exception("GD4_Oggetti_File::isVisible()\n" + e.getMessage());
         }        
         
  }
  
  public String getEffectiveAction() {
		return effectiveAction;
	}

	public void setEffectiveAction(String effectiveAction) {
		this.effectiveAction = effectiveAction;
	}  
	public String getIdServizioEsterno() {
		return idServizioEsterno;
	}

	public void setIdServizioEsterno(String idServizioEsterno) {
		this.idServizioEsterno = idServizioEsterno;
	}
	
	public String getChiaveServizioEsterno() {
		return chiaveServizioEsterno;
	}
 
	public void setChiaveServizioEsterno(String chiaveServizioEsterno) {
		this.chiaveServizioEsterno = chiaveServizioEsterno;
	}
  /*
   * METHOD:      retrieve() 
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Carica un oggetto file dal DB
   *
   * RETURN:      void
  */
  public void retrieve() throws Exception 
  {
	     retrieveInterna(null);         
  }
 
  /*
   * METHOD:      retrieve() 
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Carica un oggetto file dal DB
   *
   * RETURN:      void
  */
  public void retrieveLog(String idLog) throws Exception 
  {
	     retrieveInterna(idLog);
         
  } 
  
  private void retrieveInterna(String idLog) throws Exception {	  
	      IDbOperationSQL dbOp = null;
	      boolean bIsLog;
	
	      if (this.getIdOggettoFile().equals("0")) return;
	      
	      String sTipoRetrieve;
	      if (idLog==null) {
	    	  sTipoRetrieve="retrieve()";
	      	  bIsLog=false;
	      }
	      else {
	    	  sTipoRetrieve="retrieveLog()";
	    	  bIsLog=true;
	      }
	      
	      try {
	     	 Vector<String> v;
	          v = (new LookUpDMTable(varEnv)).lookUpInfoAr_Cm_Cr_Area("",this.getIdOggettoFile(),false);
	         
	          pathFileArea=v.get(10);
	          if (pathFileArea==null) pathFileArea="";
	          arcmcr=v.get(1);
	      }                  
	      catch (Exception e) {              
	          throw new Exception("GD4_Oggetti_File::"+sTipoRetrieve+" - Ricerca pathFileArea e arcmcr "+e.getMessage());
	      }           
	    
	      try {
	        StringBuffer sStm = new StringBuffer();
	
	        dbOp = varEnv.getDbOp();
	       
	        if (idLog==null) {
		        sStm.append("select id_oggetto_file_padre,");
		        sStm.append("f.id_formato,filename,\"FILE\",testoocr,allegato,visibile,nvl(PATH_FILE,''),nvl(PATH_FILE_ROOT,'')");
		        sStm.append(" from oggetti_file o , formati_file f");
		        sStm.append(" where id_oggetto_file = " + this.getIdOggettoFile() +" and ");
		        sStm.append(" o.ID_FORMATO = f.ID_FORMATO");
		        sStm.append(" order by filename");
	        }
	        else {
	        	sStm.append("select 0,");
		        sStm.append("0,filename,\"FILE\",testoocr,allegato,'S',nvl(PATH_FILE,''),nvl(PATH_FILE_ROOT,'')");
		        sStm.append(" from oggetti_file_log ");
		        sStm.append(" where id_oggetto_file = " + this.getIdOggettoFile() +" and ");
		        sStm.append(" id_log = "+idLog);     
	        }
	        	 
	        dbOp.setStatement(sStm.toString());
	
	        dbOp.execute();
	
	        ResultSet rst = dbOp.getRstSet();
	
	        if (rst.next()) {
	           this.setIdFormato(rst.getString(2));
	           this.setIdOggettoFilePadre(rst.getString(1));
	           this.setFileName(rst.getString(3));
	           this.setAllegato(rst.getString(6));
	           this.setVisible(rst.getString(7));

	           String pathFileAreaScrittoInOggettiFile = Global.nvl(rst.getString(9),"");

	           if ((Global.nvl(pathFileArea,"").equals("") &&  pathFileAreaScrittoInOggettiFile.equals("")) ||  Global.nvl(rst.getString(8),"").equals("")) {
		              if (rst.getBinaryStream(5)!=null) {
		            	  closeFile(false);
		 		          this.setFile((InputStream)rst.getBinaryStream(5));
		              }
	//	              else if (!varEnv.Global.MANAGE_TYPE_FILE.equals(Global.MANAGE_TYPE_BLOB)) {
	//	                 caricaBFile(rst);
	//	              }
                      this.setPathFileArea("");
                      this.setArcmcr("");
                      this.setPathObjFile("");
	           }	
	           else {
	        	   if (idLog==null) {
	        	       if (pathFileAreaScrittoInOggettiFile.equals("")) {
                           this.setPathFileArea(pathFileArea);
                       }
	        		   else {
                           this.setPathFileArea(pathFileAreaScrittoInOggettiFile);
                       }
	                   this.setArcmcr(arcmcr);
	                   this.setPathObjFile(rst.getString(8));   
	        	   }
	        	   else {
	        		   /*String pathObj=this.getIdOggettoFile();
	        		   
	        		   String logPthFile=Global.lastTrim(pathObj, "\\",varEnv.Global.WEB_SERVER_TYPE);	        		  
	        		   String pathObjFile=pathObj.substring(0,pathObj.indexOf(logPthFile)-1);*/

                       if (pathFileAreaScrittoInOggettiFile.equals("")) {
                           this.setPathFileArea(pathFileArea);
                       }
                       else {
                           this.setPathFileArea(pathFileAreaScrittoInOggettiFile);
                       }
                       this.setArcmcr(arcmcr);
	                 /*  this.setArcmcr(arcmcr+"\\"+logPthFile);*/
	                   this.setPathObjFile(rst.getString(8));
	        		   
	        	   }                   	         	  
	           }
	        }
	        else {               
	            throw new Exception("Select fallita per idOggettoFile: " +  this.getIdOggettoFile()); 
	        }   
	        
	      }
	      catch (Exception e) {               
	            throw new Exception("GD4_Oggetti_File::"+sTipoRetrieve+"\n" + e.getMessage());
	      }	      
  }
  
  
  /*
   * METHOD:      insert(Object, A_Libreria, Object) 
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Inserisce nella tabella oggetti_file un
   *              valore di tipo GD4_Oggetti_File.
   *              Viene passato l'id del documento di appartenenza
   *
   * RETURN:      boolean
  */
  public boolean insert(Object idDocumento, A_Libreria aLibreria) throws Exception 
  {             
	  	 this.bLogEseguito=false;
         String sColonnaFile;

         if (this.getFileName() == null)
            throw new Exception("GD4_Oggetti_File::insert() filename richiesto");
         if (this.getAllegato() == null)
            throw new Exception("GD4_Oggetti_File::insert() allegato richiesto");   
         
         //Caso SYS_PDF o KFX
         //Se il sys_pdf o KFX esiste devo andare in update dell'esistente
         if ( (this.getIdFormato().equals((new LookUpDMTable(varEnv)).lookUpFormato("SYS_PDF")) ||
               this.getIdFormato().equals((new LookUpDMTable(varEnv)).lookUpFormato("KFX")) ) &&
               !this.getIdFormato().equals("0")
             ) 
         {   
             String getSysDoc=getSysFormato(this.getIdFormato(),(String)idDocumento);
             
             if (!getSysDoc.equals("0")) {
                this.setIdOggettoFile(getSysDoc);
                return update(idDocumento,aLibreria);   
             }
         }

         //Testo se il file esiste già (attraverso il nome)
         try {
            String idOgfi=existsOggettoFile(this.getFileName(),(String)idDocumento);
            if (!idOgfi.equals("0")) {
               this.setIdOggettoFile(idOgfi);
               return update(idDocumento,aLibreria);               
            }
         }
         catch (Exception e) 
         {
            throw new Exception("GD4_Oggetti_File::insert() existsOggettoFile\n" + e.getMessage());
         }

         String idValoreInInsert = this.getIdOggettoFile();

         if ((varEnv.Global.MANAGE_TYPE_FILE).equals(Global.MANAGE_TYPE_AUTO))
         {   
            if (this.getAllegato().equals("N"))
               sColonnaFile ="TESTOOCR";
            else
               sColonnaFile = "\"FILE\"";            
         }
         else
             sColonnaFile = (varEnv.Global.MANAGE_TYPE_FILE.equals(Global.MANAGE_TYPE_BLOB))?
                                "TESTOOCR":"\"FILE\"";
         
         long dimMaxAllegati=-1; 
         String idServizioGdmSyncro="-1";
         String pathFileAreaOracle;
         try {
        	 Vector<String> v;
             v = (new LookUpDMTable(varEnv)).lookUpInfoAr_Cm_Cr_Area(idDocumento+"",null,false);
             
             pathFileArea=v.get(0);             
             if (pathFileArea==null) pathFileArea="";
             arcmcr=v.get(1);
             acrarea=v.get(2);
             acrmodello=v.get(3);
             forceFileOnBlob=v.get(4);
             dimMaxAllegati=getDimMaxAllegati(v.get(7));
             idServizioGdmSyncro=v.get(8);

             pathFileAreaOracle=v.get(9);
             if (pathFileAreaOracle==null) pathFileAreaOracle="";
         }                  
         catch (Exception e) {              
             throw new Exception("GD4_Oggetti_File::insert() - "+e.getMessage());
          }         
    
          // Lancio dell'insert sul Database (senza il blob o bfile)
          try {
            calcolaIdFilepadre((String)idDocumento);
            
            StringBuffer sStm = new StringBuffer();
            
            IDbOperationSQL dbOpSql = varEnv.getDbOp();

            if (idValoreInInsert.equals("0")) {
               idValoreInInsert=dbOpSql.getNextKeyFromSequence("OGG_FILE_SQ")+"";              
               this.setIdOggettoFile(idValoreInInsert);
            }
            
            
                              
            sStm.append("INSERT INTO OGGETTI_FILE (ID_OGGETTO_FILE,ID_DOCUMENTO,");
            sStm.append("ID_OGGETTO_FILE_PADRE,ID_FORMATO,FILENAME,DATA_INSERIMENTO,");
            sStm.append("ALLEGATO,UTENTE_AGGIORNAMENTO");
            //if (idServizioGdmSyncro.equals("-1"))  {
	            if (pathFileArea.equals("") || forceFileOnBlob.equals("1"))
	            	sStm.append(",TESTOOCR");
	            else {
	            	sStm.append(",PATH_FILE");
                    sStm.append(",PATH_FILE_ROOT");
                    sStm.append(",PATH_FILE_ROOT_ORACLE");
	            	isFileFs=true;
	            }   
            //}
           // else {
            //	sStm.append(",ID_SERVIZIO_ESTERNO,CHIAVE_SERVIZIO_ESTERNO");
           // }
            sStm.append(") values ");
            sStm.append("(" +  idValoreInInsert );
            sStm.append("," +  idDocumento );
            if  (this.getAllegato().equals("S")) {
                if (oggettoPadre.equals(""))
                   sStm.append(", null" );
                else
                   sStm.append("," +  oggettoPadre );
            }
            else
                sStm.append(", null" );
            sStm.append("," +  this.getIdFormato() );
            sStm.append(",:P_PARFILENAME");
            sStm.append(", SYSDATE ");
            sStm.append(",'" + this.getAllegato() + "'");
            sStm.append(",'" + varEnv.getUser() + "'");
            //if (idServizioGdmSyncro.equals("-1"))  {
	            if (pathFileArea.equals("") || forceFileOnBlob.equals("1"))
	            	sStm.append(",:TESTOOCR");
	            else {
	            	sStm.append(",'"+acrarea+"'");

	            	if (pathFileArea.equals("")) {
                        sStm.append(",null");
                    }
	            	else {
                        sStm.append(",'"+pathFileArea+"'");
                    }

                    if (pathFileAreaOracle.equals("")) {
                        sStm.append(",null");
                    }
                    else {
                        sStm.append(",'"+pathFileAreaOracle+"'");
                    }
	            }
            //}
           // else {
            	// sStm.append(","+idServizioGdmSyncro+",'RIVERSATO_DA_GDM'");
            //}
            sStm.append(")");

            
            InputStream isFile=null;
            if ((pathFileArea.equals("") || forceFileOnBlob.equals("1"))  /*|| (!idServizioGdmSyncro.equals("-1"))*/  ) {
	            //NON SONO IN RENAME
	            if (this.getOldFileName().equals(""))
	            	isFile=(InputStream)this.getFile(false);
	            //SONO IN RENAME
	            else
	            	isFile=(InputStream)this.getOldFile();
            }

            this.setPathFileArea(pathFileArea);
            
            dbOpSql.setStatement(sStm.toString());                 
            dbOpSql.setParameter(":P_PARFILENAME",this.getFileName());
            
            boolean usatoBlob=false;
            if ((pathFileArea.equals("") || forceFileOnBlob.equals("1")) /*&& (idServizioGdmSyncro.equals("-1"))  */ ) {
            	usatoBlob=true;
            	dbOpSql.setParameter(":TESTOOCR", isFile, isFile.available());            	
            }
            
            elpsTime.start("Inserimento in tabella Oggetti File",sStm.toString());                        
            
            dbOpSql.execute();
            
            this.setMark_as(Global.MARK_AS_OBJFILE_INSERT);
            this.setPathObjFile(acrarea);
         
            
            long idSyncroRet=0;
            //Se sono su un servizio esterno devo mandare il file richiamandolo
            if (!idServizioGdmSyncro.equals("-1")) {
            	
            	//Mi recupero l'inputstream dal blob appena scritto
            	InputStream isBlob=null;            	
            	try  {
            		String sql="select testoocr from oggetti_file where id_oggetto_file="+idValoreInInsert;
            		
            		dbOpSql.setStatement(sql);
            		dbOpSql.execute();
            		ResultSet rst = dbOpSql.getRstSet();
            		if (rst.next())  {
            			try {			                    	  
            				isBlob=dbOpSql.readBlob(1);            				
	                   	 }
	                   	 catch (NullPointerException e) {  
	                   		  //DONTCARE
	                   	 }
            		}
            	}
            	catch (Exception e) {
  	          	  throw new Exception("Errore in lettura blob per utilizzo servizio esterno con idServizio: "+idServizioGdmSyncro+"\n" + e.getMessage());
  	            }
            	
            	if (isBlob!=null) {
            		//Se il blob è pieno procedo, altrimento salto
		            try  {	          		 		            		            		            	
		            	GDMSyncroCore gdmSyncroCore = new GDMSyncroCore(idServizioGdmSyncro,""+idDocumento,idValoreInInsert,this.varEnv);
		            	idSyncroRet=Long.parseLong(gdmSyncroCore.syncro(isBlob));
		            }
		            catch (Exception e) {
		          	   //throw new Exception("Errore in utilizzo servizio esterno con idServizio: "+idServizioGdmSyncro+"\n" + e.getMessage());
		               //Ho avuto un problema . Il log del problema lo trovo sul servizio. Scrivo cmq l'errore sulla consolle del tomcat
		            	//Quindi vado avanti lasciando il file sul blob
		            	System.out.println("[WARN] GD4_Oggetti_File::insert() Errore nel tentativo di inserire il file dell'id_oggetto_file ="+idValoreInInsert+
		            						" sul servizio esterno con id="+idServizioGdmSyncro+". Lascio il file sul blob. Si rimanda per l'errore dettagliato al relativo file di log del servizio esterno."+
		            						" L'errore è: "+e.getMessage());	
		            }		            	
		            	
		            if (idSyncroRet!=0) {
		            	//effettuo update dell'idsyncro sulla oggetti_file e svuoto il blob se è andato tutto OK
		            	try  {
			            	String sql="update oggetti_file set ID_SYNCRO="+idSyncroRet+",testoocr=null,ID_SERVIZIO_ESTERNO="+idServizioGdmSyncro+",CHIAVE_SERVIZIO_ESTERNO='RIVERSATO_DA_GDM'  where id_oggetto_file= "+idValoreInInsert;
			            	dbOpSql.setStatement(sql);  
			            	dbOpSql.execute();
			            	usatoBlob=false;
		            	}
			            catch (Exception ei) {
			            	throw new Exception("Errore in aggiornamento oggetti file update su ID_SYNCRO con il valore "+idSyncroRet+"\n" + ei.getMessage());
			            }
		            }
		            
		            try  {isBlob.close();}catch (Exception e) {}
            	}
            }
                        
            //Lo devo caricare sul FS perché non l'ho messo su oracle
            //Gestito a livello di area su FS e non su BLOB 
            //if (!pathFileArea.equals("")) scriviFileFs(isFile,Global.PATH_FILE_ABSOLUTE,Global.replaceAll(this.getFileName(),"'","''"),"");
            //NOOOOOOOOOOO SPOSTATO TUTTO SULLA GESTORE_FS
                        
            
            //Devo gestire la max dim degli allegati.
            //TODO andrebbe gestita anche per FS e servizio esterno....
            if (/*idServizioGdmSyncro.equals("-1")*/usatoBlob) checkDimMaxAllegati(Long.parseLong(idValoreInInsert),dimMaxAllegati);
                    
            
          }    
          catch (DocumentException Dexp) {
        	    throw Dexp;
          }
          catch (Exception e) {
        	   // e.printStackTrace();
                throw new Exception("GD4_Oggetti_File::insert()\n" + e.getMessage());
          }
          
          
          
          this.setEffectiveAction("INSERT");
          
          return true;
/*          try {
            // Aggiornamento della stessa riga con il file (bfile o blob)
        	boolean bRet = updateFile(sColonnaFile, aLibreria);
        	        	
        	elpsTime.stop();
        	
            return bRet;
          }    
          catch (Exception e) {
            throw new Exception("GD4_Oggetti_File::insert() updateFile\n" + e.getMessage());
          }*/
          
  }
 
  /*
   * METHOD:      update(Object, A_Libreria) 
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Aggiorna nella tabella oggetti_file un 
   *              valore di tipo Oggetti_file
   *              Viene passato l'id del documento di appartenenza
   *              
   * RETURN:      boolean
  */
  public boolean update(Object idDocumento, A_Libreria aLibreria) throws Exception 
  { 
	  	 this.bLogEseguito=false;
         String sColonnaFile;

         //Caso di aggionamento documento dove aggiungo un oggetto file
         if (this.getIdOggettoFile().equals("0")) 
             return insert(idDocumento, aLibreria);

         if (varEnv.Global.MANAGE_TYPE_FILE.equals(Global.MANAGE_TYPE_AUTO))
         {
            if (this.getAllegato().equals("N")) 
               sColonnaFile ="TESTOOCR";                           
            else
               sColonnaFile = "\"FILE\"";
         }
         else
               sColonnaFile = (varEnv.Global.MANAGE_TYPE_FILE.equals(Global.MANAGE_TYPE_BLOB))?
                              "TESTOOCR":"\"FILE\"";
         
         long dimMaxAllegati=-1;         
         try {
        	 Vector<String> v;
             v = (new LookUpDMTable(varEnv)).lookUpInfoAr_Cm_Cr_Area(idDocumento+"",this.getIdOggettoFile(),false);
             
             pathFileArea=v.get(11);
             if (pathFileArea==null) pathFileArea="";
             arcmcr=v.get(1);
             acrarea=v.get(2);
             acrmodello=v.get(3);
             dimMaxAllegati=getDimMaxAllegati(v.get(7));
         }                  
         catch (Exception e) {
             throw new Exception("GD4_Oggetti_File::update() - "+e.getMessage());
         }

         //Se non siamo in presenza di un allegato e 
         //oldFileName="" (non siamo in rename del file)
         //Allora cancello i figli
         if (this.getAllegato().equals("N") && this.getOldFileName().equals("")) 
             deleteAllegatiFigli(this.getIdOggettoFile(),(String)idDocumento,aLibreria.getDirectory());
             
         //Lancio la log prima di fare update
         //TODO per ora se idServizioGdmSyncro è pieno...lo salto...sarà da fare per file esterni in futuro???
         if (bogfilog && Global.nvl(this.getIdSyncro(),"").equals("") ) {
        	 Vector<String> listaId = new Vector<String>();
        	 listaId.add(this.getIdOggettoFile());
             this.executeLog(""+idDocumento,listaId,false);          
             this.bLogEseguito=true;
         }
         
         
         
         // Lancio dell'update sul Database
         try {
        	String pathObjFile=checkIsFileFS();
        	 
            calcolaIdFilepadre((String)idDocumento);
         
            IDbOperationSQL dbOpSql = varEnv.getDbOp();
            
            StringBuffer sStm = new StringBuffer();
            
            String fileName;
            
            //NON SONO IN RENAME
            if (this.getOldFileName().equals(""))
            	fileName=Global.replaceAll(this.getFileName(),"'","''");
            //SONO IN RENAME
            else
            	fileName=Global.replaceAll(this.getOldFileName(),"'","''");            		
            
            boolean bMettiVirgolaAvanti=false;
            sStm.append("UPDATE OGGETTI_FILE SET ");
            //sStm.append("ID_DOCUMENTO = " + idDocumento);

             boolean bRenameSenzaFile=false;
            if (pathObjFile.equals("") && Global.nvl(this.getIdSyncro(),"").equals("")) {
                if (!this.getOldFileName().equals("") && this.getOldFile()==null) {
                    bRenameSenzaFile=true;
                }
                else {
                    sStm.append("TESTOOCR = :TESTOOCR ");
                    bMettiVirgolaAvanti=true;
                }
            }
            else {            	
            	if (Global.nvl(this.getIdSyncro(),"").equals("")) isFileFs=true;
            }            	
                        
            if (!this.getIdFormato().equals("0")) {
            	if (bMettiVirgolaAvanti) sStm.append(",");
            	bMettiVirgolaAvanti=true;
            	sStm.append("ID_FORMATO = " + this.getIdFormato());
            }                
            if (this.getIdOggettoFilePadre()!=null) {
            	if (bMettiVirgolaAvanti) sStm.append(",");
            	bMettiVirgolaAvanti=true;
                sStm.append("ID_OGGETTO_FILE_PADRE = " + this.getIdOggettoFilePadre());   
            }
            if (this.getFileName()!=null) {
            	if (bMettiVirgolaAvanti) sStm.append(",");
            	bMettiVirgolaAvanti=true;
                sStm.append("FILENAME = :P_PARFILENAME ");
            }
            if (this.getAllegato()!=null) {
            	if (bMettiVirgolaAvanti) sStm.append(",");
            	bMettiVirgolaAvanti=true;
                sStm.append("ALLEGATO = '" + this.getAllegato() + "'");
            }
            if (bMettiVirgolaAvanti) sStm.append(",");
            sStm.append("UTENTE_AGGIORNAMENTO = '"+ varEnv.getUser() + "' ");  
            //sStm.append(",data_aggiornamento = sysdate");
            sStm.append(" WHERE ID_OGGETTO_FILE = "+ this.getIdOggettoFile());            
              
            InputStream isFile=null;            
            if (pathObjFile.equals("") || (!Global.nvl(this.getIdSyncro(),"").equals("")))  {
            	//NON SONO IN RENAME
	            if (this.getOldFileName().equals(""))
	            	isFile=(InputStream)this.getFile();
	            //SONO IN RENAME
	            else
	            	isFile=(InputStream)this.getOldFile();
            }
           
            dbOpSql.setStatement(sStm.toString());
            
            dbOpSql.setParameter(":P_PARFILENAME",fileName);
            
            if (pathObjFile.equals("") && Global.nvl(this.getIdSyncro(),"").equals("") && bRenameSenzaFile==false) dbOpSql.setParameter(":TESTOOCR", isFile, isFile.available());

            elpsTime.start("Aggiornamento tabella Oggetti File",sStm.toString());
            //System.out.println(sStm.toString());
            dbOpSql.execute();
            
            this.setMark_as(Global.MARK_AS_OBJFILE_UPDATE);
            this.setPathObjFile(acrarea);
            this.setPathFileArea(pathFileArea);
            
            //if (!pathObjFile.equals("")) scriviFileFs(isFile,pathObjFile,fileName,this.getOldFileName());
            //NOOOOOOOOOOO SPOSTATO TUTTO SULLA GESTORE_FS
            
            //COMMENTATO PER ADESSO.......DA SPOSTARE LATO MODULISTICA OPPURE CON FLAG SOLO DA LI
            /*try {
              GD4_Oggetti_File_Check gd4Chk = new GD4_Oggetti_File_Check(this.getIdOggettoFile(),varEnv.getUser(), varEnv);
              
              gd4Chk.updateCheck();
            }                  
            catch (Exception ei) {
              throw new Exception("Update numCheck ObjFile ("+this.getIdOggettoFile()+") - "+ei.getMessage());
            }  */     
            
            if (!Global.nvl(this.getIdSyncro(),"").equals("")) {
           	 try  {	          	
   	            	
   		            
   	            	GDMSyncroCore gdmSyncroCore = new GDMSyncroCore(this.getIdServizioEsterno(),this.getIdSyncro(),""+idDocumento,this.getIdOggettoFile(),this.varEnv);
   	            	
   	            	gdmSyncroCore.syncro(isFile);
   	            }
   	            catch (Exception e) {
   	          	  throw new Exception("Errore in utilizzo servizio esterno con idServizio: "+this.getIdServizioEsterno()+" e idSyncro="+this.getIdSyncro()+"\n" + e.getMessage());
   	            }
            }
                        
            //TODO poi da fare per i servizi esterni e per i file su FS
            if (Global.nvl(this.getIdSyncro(),"").equals("")) checkDimMaxAllegati(Long.parseLong(this.getIdOggettoFile()),dimMaxAllegati);
            
          }          
		  catch (DocumentException Dexp) {
			    throw Dexp;
		  }
          catch (Exception e) {                 	  
        	 
                throw new Exception("GD4_Oggetti_File::update() " + e.getMessage());
          }
          
          this.setEffectiveAction("UPDATE");
          
          return true;
          // Aggiornamento della stessa riga con il file (bfile o blob)
/*          try {
        	boolean bRet;
        	
        	bRet=updateFile(sColonnaFile, aLibreria);
        	elpsTime.stop();
        	
            return bRet;
                       
          }    
          catch (Exception e) {              
                throw new Exception("GD4_Oggetti_File::update() updateFile\n" + e.getMessage());
          }*/
  
  }

  /*
   * METHOD:      delete(Object) 
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Cancellazione di un oggetto file dal DM
   *              
   * RETURN:      boolean
  */
  public boolean delete(Object sDirectory, String idDocumento) throws Exception 
  {
          IDbOperationSQL dbOp = null;
          
          try {
            StringBuffer sStm = new StringBuffer();                        

            dbOp = varEnv.getDbOp();
            
//            sStm.append("select 1 from oggetti_file  ");
//            sStm.append("where id_oggetto_file = "+ this.getIdOggettoFile() + " ");  
//            sStm.append("and \"FILE\" is not null" );            
//    
//            dbOp.setStatement(sStm.toString());
//    
//            dbOp.execute();
//    
//            ResultSet rst = dbOp.getRstSet();
//    
//            if (rst.next()) {
//                deleteBFile(getIdOggettoFile(),(String)sDirectory);              
//            }
            
            //Elimino gli allegati figli
            deleteAllegatiFigli(this.getIdOggettoFile(),idDocumento,(String)sDirectory);
                       
            sStm = new StringBuffer();
            sStm.append("update oggetti_file set DA_CANCELLARE='S' ");
            sStm.append("where id_oggetto_file = "+ this.getIdOggettoFile());            
        
            dbOp.setStatement(sStm.toString());

            dbOp.execute();    
            
            this.setMark_as(Global.MARK_AS_OBJFILE_DELETE);            
                        
          }    
          catch (Exception e) {
                throw new Exception("GD4_Oggetti_File::delete() \n" + 
                                    e.getMessage());
          }

          return true;
  }
  
  public InputStream downloadBlobBFile_ToInputStream()  throws Exception {
	  return downloadBlobBFile_ToInputStream(false);
  }
  
  public InputStream downloadBlobBFile_ToInputStream(boolean bLog)  throws Exception {
	  IDbOperationSQL dbOp = null;
      InputStream is=null;      
      try {
        StringBuffer sStm = new StringBuffer();                        

        dbOp = varEnv.getDbOp();      

        sStm = new StringBuffer();
        if (bLog)
        	sStm.append("select gdm_oggetti_file.DOWNLOADOGGETTOFILE_LOG("+this.getIdOggettoFile()+"),null,'DB'  from dual ");
        else
        	sStm.append("select  testoocr,\"FILE\" , decode(path_file,null,  'DB', decode(path_file,'','DB','FS')) from oggetti_file where id_oggetto_file= "+this.getIdOggettoFile());
        
    
        dbOp.setStatement(sStm.toString());

        dbOp.execute();     
        dbOp.getRstSet();
        ResultSet rst = dbOp.getRstSet();
        rst.next();
        
        try {			                    	  
   		  //is=dbOp.readBlob(1);
        	if (rst.getString(3).equals("FS")) {  
	            bFile = ((OracleResultSet)rst).getBFILE(2); 
	            bFile.openFile();
	        	is= bFile.getBinaryStream();
        	}
        	else { 
        		is=dbOp.readBlob(1);
        	}
 
   	  	}
   	  	catch (NullPointerException e) {  
   	  		try {super.closeBFile();}catch (Exception ei) { } 
   		  return null;
   	  	}              
        
        return is;
      }    
      catch (Exception e) {
    	  super.closeBFile();
           throw new Exception("GD4_Oggetti_File::downloadBlobBFile_ToInputStream() \n" + 
                                e.getMessage());
      }
  }
  
  public void executeLog(String idDocumento,Vector vListaIdModificati, boolean bIsNameId) throws Exception {
	        //Inserisco i Log sugli oggettiFile
	        try {
	          if (dmALog!=null) dmALog.insertAllOgfiLog(idDocumento,vListaIdModificati,false,bIsNameId,false);
	        }    
	        catch (Exception e) {        	
	     	  throw new Exception("GD4_Oggetti_File::executeLog() Errore in scrittura Oggetti File Log\n"+e.getMessage());
	       }	     
  }  

  /*
   * METHOD:      caricaBFile(ResultSet)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Caricamento di un external file (BFile) nel DB
   *              
   * RETURN:      boolean
  */
  public void caricaBFile(ResultSet rst) throws Exception 
  {
     BFILE docum;
     try {
       docum  = ((OracleResultSet)rst).getBFILE(4);
     }
     catch (Exception e) {
        throw new Exception("GD4_Oggetti_File::retrieve() getBFILE\n" + e.getMessage());
     }
  
     try {
       if (docum.fileExists()) {
         if (docum.isFileOpen()) docum.closeFile();
         docum.openFile();
      
         java.io.InputStream in = docum.getBinaryStream();
         this.setFile(in);
         docum.closeFile();
       }
     }
     catch (Exception e) {
        throw new Exception("GD4_Oggetti_File::retrieve() open-set-close BFILE\n" + e.getMessage());
     }            
  }

  // ***************** METODI DI GET E SET ***************** //

  public void setIdOggettoFilePadre(String idOggettoPadre) 
  {
         oggettoPadre=idOggettoPadre;
  }
  
  public String getIdOggettoFilePadre() {
       
         return oggettoPadre;
  }
  
  /*public Object getFile() throws Exception
  {	  
	 
	  if (fileStreamAllinizio) {
		 
		  fileStreamAllinizio=false;
		  return file;
	  }
	  
      IDbOperationSQL dbOp = null;
      
      try {
        StringBuffer sStm = new StringBuffer();                        

        dbOp = varEnv.getDbOp();
        
        sStm.append("select TESTOOCR from oggetti_file  ");
        sStm.append("where id_oggetto_file = "+ this.getIdOggettoFile() + " ");             

        dbOp.setStatement(sStm.toString());

        dbOp.execute();

        ResultSet rst = dbOp.getRstSet();
      
        if (rst.next()) 
            if (rst.getBinaryStream(1)!=null) {
                file=(InputStream)rst.getBinaryStream(1);
             
             }                    
                                   
      }
      catch (Exception e) {
            throw new Exception("GD4_Oggetti_File::getFile() \n" + 
                                e.getMessage());
      }
         		  
      return file;
  }*/
  
  public String toString() 
  {
         return super.toString();
  }

  // ***************** METODI PRIVATI ***************** //

  /*
   * METHOD:      updateFile(String, A_Libreria) 
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Aggiornamento di un file nella tabella
   *              oggetti_file. Il file potrebbe essere 
   *              un blob o un Bfile a seconda del parametro
   *              stringa nome della colonna da aggiornare.
   *              Il secondo parametro è obbligatorio per 
   *              aggiornare un BFile
   *              
   * RETURN:      boolean
  */
  private boolean updateFile(String sColonna, A_Libreria aLibreria ) throws Exception 
  {
          boolean okOp = true;
          
          try {
            if (sColonna.equals("TESTOOCR"))
                try { okOp = this.updateBLob(sColonna); } 
                catch (Exception e) {throw new Exception("GD4_Oggetti_File::updateFile() updateBLob\n" + e.getMessage());}
            else if (sColonna.equals("\"FILE\"") && (aLibreria != null)){ 
                try { okOp = this.updateBFile(sColonna, aLibreria); }
                catch (Exception e) {throw new Exception("GD4_Oggetti_File::updateFile() updateBFile\n" + e.getMessage());}
            }
            else{
                okOp = false; 
            }
          }
          catch (Exception e) {
            throw new Exception("GD4_Oggetti_File::updateFile() " + e.getMessage());
          }

          elpsTime.addMsg("SottoAzione: UPDATE TESTOOCR CON FILE");
          
          return okOp;
  }
 
  /*
   * METHOD:      updateBLob(String) 
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Aggiornamento di un file blob nella 
   *              tabella oggetti_file.
   *              
   * RETURN:      boolean
  */
  private boolean updateBLob(String sColonna) throws Exception 
  {
          try {
            LetturaScritturaFileDB file;
            
            InputStream isFile;
            //NON SONO IN RENAME
            if (this.getOldFileName().equals(""))
            	isFile=(InputStream)this.getFile();
            //SONO IN RENAME
            else
            	isFile=(InputStream)this.getOldFile();  
            
            file = new LetturaScritturaFileDB( varEnv.getDbOp().getConn(),
                                               "OGGETTI_FILE",sColonna,
                                               "WHERE ID_OGGETTO_FILE="+
                                                this.getIdOggettoFile());

            file.scriviFile(isFile);
          }
          catch (Exception e) {
                throw new Exception("GD4_Oggetti_File::updateBLob() " + e.getMessage());
          }
          
          return true;          
  }

  /*
   * METHOD:      updateBFile(String, A_Libreria)
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Aggiornamento di un external file (BFile)
   *              nella tabella oggetti_file.
   *              
   * RETURN:      boolean
  */
  private boolean updateBFile(String sColonna, A_Libreria aLibreria) throws Exception 
  {
          if (updateBFileFS(sColonna, aLibreria))
             return updateBFileDB( sColonna, aLibreria);
          else
             return false;
  }

  /*
   * METHOD:      updateBFileFS(String, A_Libreria)
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Aggiornamento di un external file (BFile) nel FS
   *              
   * RETURN:      boolean
  */
  private boolean updateBFileFS(String sColonna, A_Libreria aLibreria) throws Exception 
  {
         String pathFile =  varEnv.Global.WEB_ROOT_REPOSITORY+"\\"+
                            aLibreria.getDirectory()+"\\"+
                            this.getIdOggettoFile();

          pathFile = Global.adjustsPath(varEnv.Global.WEB_SERVER_TYPE, pathFile);

          try {                  
            LetturaScritturaFileFS file = new LetturaScritturaFileFS(pathFile);
            file.scriviFile((java.io.InputStream)this.getFile());     
          }
          catch (Exception e) { 
                throw new Exception("GD4_Oggetti_File::updateBFileFS() " + e.getMessage());
          }

          return true;
  }
    
  /*
   * METHOD:      updateBFileDB(String, A_Libreria)
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Aggiornamento di un external file (BFile) nel DB
   *              
   * RETURN:      boolean
  */
  private boolean updateBFileDB(String sColonna, A_Libreria aLibreria) throws Exception 
  {
          try {
            IDbOperationSQL dbOpSql = varEnv.getDbOp();
          
            StringBuffer sStm = new StringBuffer();

            sStm.append("update oggetti_file set ");
            sStm.append(sColonna + " = bfilename( F_GETDIRECTORY_AREA_NAME("+ this.getIdOggettoFile() +"), '" + this.getIdOggettoFile() +"' )" );
            sStm.append("where id_oggetto_file = "+ this.getIdOggettoFile());            

            dbOpSql.setStatement(sStm.toString());

            dbOpSql.execute();

          }    
          catch (Exception e) {
            throw new Exception("GD4_Oggetti_File::updateBFileDB() " + 
                                e.getMessage());          
          }

          return true;
  }

  /*
   * METHOD:      deleteAllegatiFigli()
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Elimina i figli allegati del padre
   *              
   * RETURN:      boolean
  */  
  private void deleteAllegatiFigli(String idPadre,String idDocumento,String sDirectory) throws Exception 
  {
          
	      IDbOperationSQL dbOp = null;
	      
	      try {
	        StringBuffer sStm = new StringBuffer();
	
	        dbOp = varEnv.getDbOp();
	
//	        //Elimino i BFile se esistono
//	        sStm.append("SELECT id_oggetto_file ");
//	        sStm.append("FROM OGGETTI_FILE ");
//	        sStm.append("WHERE id_documento="+idDocumento+" AND id_oggetto_file<>"+idPadre+" ");
//	        sStm.append("START WITH  id_oggetto_file="+idPadre+" ");
//	        sStm.append("CONNECT BY PRIOR id_oggetto_file=id_oggetto_file_padre ");
//	        sStm.append("AND \"FILE\" IS NOT NULL" );
//	
//	        dbOp.setStatement(sStm.toString());
//	
//	        dbOp.execute();
//	
//	        ResultSet rst = dbOp.getRstSet();
//	
//	        while (rst.next()) {                      
//	              deleteBFile(rst.getString(1),(String)sDirectory);              
//	        }
	        
	        //Elimino i figli dell'allegato padre
	        sStm = new StringBuffer();
	        sStm.append("UPDATE  OGGETTI_FILE SET DA_CANCELLARE='S' ");
	        sStm.append("WHERE id_oggetto_file IN ");
	        sStm.append("(SELECT id_oggetto_file ");
	        sStm.append("FROM OGGETTI_FILE ");
	        sStm.append("WHERE id_documento="+idDocumento+" AND id_oggetto_file<>"+idPadre+" ");
	        sStm.append("START WITH  id_oggetto_file="+idPadre+" ");
	        sStm.append("CONNECT BY PRIOR id_oggetto_file=id_oggetto_file_padre)");
	    
	        dbOp.setStatement(sStm.toString());
	
	        dbOp.execute();
	        
	      }
	      catch (Exception e) {                 
	             throw new Exception("GD4_Oggetti_File::deleteAllegatiFigli() \n"+e.getMessage());
	      }


  }

  /*
   * METHOD:      existsOggettoFile
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Verifica se esiste un oggetto file a partire dal nome del file
   *              
   * RETURN:      boolean
  */    
  private String existsOggettoFile(String filename, String idDocumento) throws Exception 
  {         
          try {
            IDbOperationSQL dbOpSql = varEnv.getDbOp();
            
            StringBuffer sStm = new StringBuffer();                      

            sStm.append("select ID_OGGETTO_FILE from OGGETTI_FILE");
            sStm.append(" where id_documento = " + idDocumento);
            sStm.append(" and FILENAME = :P_FILENAME ");

            dbOpSql.setStatement(sStm.toString());
            dbOpSql.setParameter(":P_FILENAME", filename);
            dbOpSql.execute();

            ResultSet rst = dbOpSql.getRstSet();
                                  
            String sRet;                      
                                  
            if (rst.next()) 
                sRet=""+rst.getLong(1);
            else
                sRet="0";
           
            return sRet;
             
          }
          catch (Exception e) {           
                throw new Exception("GD4_Oggetti_File::existsOggettoFile() " + e.getMessage());
          }          
         
  }
  
    /*
   * METHOD:      getSysPDF
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Verifica se esiste un oggetto file SysPDF a partire dal formato
   *              
   * RETURN:      boolean
  */    
  private String getSysFormato(String idFormato, String idDocumento) throws Exception 
  {       
          String sRet="0";
          
          try {
            IDbOperationSQL dbOpSql = varEnv.getDbOp();          
          
            StringBuffer sStm = new StringBuffer();                      

            sStm.append("select ID_OGGETTO_FILE from OGGETTI_FILE");
            sStm.append(" where id_formato = " + idFormato +" and id_documento="+idDocumento);

            dbOpSql.setStatement(sStm.toString());

            dbOpSql.execute();

            ResultSet rst = dbOpSql.getRstSet();
                                  
            if (rst.next()) 
                sRet=""+rst.getLong(1);
           
            return sRet;
             
          }
          catch (Exception e) {           
            throw new Exception("GD4_Oggetti_File::getSysFormato() " + e.getMessage());
          }          
         
  }

  /*
   * METHOD:      deleteBFile
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Cancella il BFile sul disco
   *              
   * RETURN:      boolean
  */    
  private void deleteBFile(String idOgfi, String sDirectory) throws Exception
  {
          File f = new File(Global.adjustsPath(varEnv.Global.WEB_SERVER_TYPE,
                                               varEnv.Global.WEB_ROOT_REPOSITORY+"\\"+
                                               sDirectory+"\\"+idOgfi));
          try {
               f.delete();
          }
          catch (Exception e) {                 
                throw new Exception("GD4_Oggetti_File::deleteBFile() file delete\n" + 
                                     e.getMessage());
          }
  }

  /*
   * METHOD:      calcolaIdFilepadre
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: Cerca di recuperare l'idFilePadre dal nome
   *              
   * RETURN:      boolean
  */     
  private void calcolaIdFilepadre(String idDocumento) throws Exception 
  {
          
          //L'id è già numerico, tutto ok
          try {
            Integer.parseInt(this.getIdOggettoFilePadre());
            return;
          }
          catch (Exception e) {}
          
          try {
            //Cerco di restituire l'id del file padre nel caso abbia passato
            //il nome, altrimenti viene passato l'id.
            if (this.getIdOggettoFilePadre()!=null && !this.getIdOggettoFilePadre().equals("")) {
                IDbOperationSQL dbOpSql = varEnv.getDbOp();
             
                StringBuffer sStm = new StringBuffer();
                
                sStm.append("select ID_OGGETTO_FILE from oggetti_file");
                sStm.append(" where ID_DOCUMENTO = " + idDocumento );
                sStm.append("   and FILENAME = '" + this.getIdOggettoFilePadre() + "'" );

                dbOpSql.setStatement(sStm.toString());
                dbOpSql.execute();  

                ResultSet rst = dbOpSql.getRstSet();
                                
                if ( rst.next() )
                    this.setIdOggettoFilePadre(""+rst.getLong(1));
                else 
                     this.setIdOggettoFilePadre("");
            }
            else
                return;
                             
          }
           catch (Exception e) {
                 throw new Exception("GD4_Oggetti_File::calcolaIdFilepadre \n"+e.getMessage());
          }
           
  }
  
  private String checkIsFileFS() throws Exception 
  {
          
          IDbOperationSQL dbOp = null;
          
          try {
            StringBuffer sStm = new StringBuffer();

            dbOp = varEnv.getDbOp();

            //Elimino i BFile se esistono
            sStm.append("SELECT nvl(PATH_FILE,'') ");
            sStm.append("FROM OGGETTI_FILE ");
            sStm.append("WHERE id_oggetto_file="+this.getIdOggettoFile()+" ");

            dbOp.setStatement(sStm.toString());

            dbOp.execute();

            ResultSet rst = dbOp.getRstSet();

            rst.next();                      
                              
            if (rst.getString(1)==null)
            	return "";
            
            return rst.getString(1);
            
          }
          catch (Exception e) {                 
                 throw new Exception("GD4_Oggetti_File::checkIsFileFS() \n"+e.getMessage());
          }

  }  
  
  /*private void scriviFileFs(InputStream isFile, String pathObjFile, String fileName, String frename) throws Exception {
	  	  String dir=pathFileArea+
		     		 "/"+pathObjFile+
		     		 "/"+arcmcr;
	  	  
		  File fDir = new File(dir);
		
		  if (!fDir.exists()) fDir.mkdirs();
		  		  
		  if (!frename.equals("")) {			  
			  File fFileToDel = new File(dir+
					                      "/"+
					                      fileName);			
			  
			  File fFileToRen = new File(frename);
					  			  
			  if (!(fFileToDel.renameTo(fFileToRen)))
				  throw new Exception("Errore! in scriviFileFs: Non riesco a rinominare il file:"+fileName);
		  }
		  else {			  
			  
			  LetturaScritturaFileFS f = new LetturaScritturaFileFS(dir+
						                                            "/"+
						                                            fileName);
				
			  f.scriviFile(isFile);		 
			  isFile.close();		  		 
		  }
  }*/
  
 /* private void deleteFileFs(String nomeFile, String pathObjFile) throws Exception {
  	  	  String dir=pathFileArea+
	     		     "/"+pathObjFile+
	     		     "/"+arcmcr;
  	  
	 
		  File fFileToDel = new File(dir+
				                     "/"+
				                     nomeFile);
		  
		  if (!fFileToDel.delete())
			  throw new Exception("Errore! in deleteFileFs: Non riesco a cancellare il file:"+nomeFile);	   		 
		  
  }  */
  
  
  public String getPathFileArea() {
		 return pathFileArea; 
		
  }

  public void setPathFileArea(String pathFileArea) {
	 	 this.pathFileArea = pathFileArea;
  }  
  
  public void setLog(DMActivity_Log dmALog) {
	     this.dmALog=dmALog;
  }

  public String getMark_as() {
	 	 return mark_as;
  }

  public void setMark_as(String mark_as) {
	     this.mark_as = mark_as;
  }

  public String getPathObjFile() {
		 return pathObjFile;
  }

  public void setPathObjFile(String pathObjFile) {
	     this.pathObjFile = pathObjFile;
  }

  public String getArcmcr() {
		 return arcmcr;
  }

  public void setArcmcr(String arcmcr) {
	     this.arcmcr = arcmcr;
  }
  
  public void closeFile(boolean onlyFile) throws Exception {
		 if (file!=null) ((InputStream)file).close();
		 file=null;
		 
		 if (!onlyFile) {
			 if (oldFile!=null && Global.nvl(oldFileName,"").equals("")) {
				 ((InputStream)oldFile).close();
				 oldFile=null;
			 }			 	 
		 }
		 
  }
  
  public void setImpostazioniSyncro(String impoString) {
	  if (Global.nvl(impoString, "@NOTEXIST@").equals("@NOTEXIST@")) return;
	  
	  String sImpo[] = impoString.split("#DELIM#");
	  
	  if (sImpo.length<3) return;
	  
	  userGDM_Imposyncro = sImpo[0];
	  dsn_Imposyncro = sImpo[1];
	  servlet_Imposyncro = sImpo[2]+"/GdmSyncroWebServlet/GDMSyncroServlet";
  }
  
  public String getIdSyncro() {
		return idSyncro;
  }

  public void setIdSyncro(String idSyncro) {
		this.idSyncro = idSyncro;
  }
  
  public String getUserGDM_Imposyncro() {
		return userGDM_Imposyncro;
	}

	public String getDsn_Imposyncro() {
		return dsn_Imposyncro;
	}

	public String getServlet_Imposyncro() {
		return servlet_Imposyncro;
	}
  
  
  public Object getFile() throws Exception {
	     return getFile(true);
  }

  public String getPathFS(boolean soloDir) {
      String logDir="";
      String pathObj=pathObjFile;
      if (pathObjFile.indexOf("LOG_")>0) {
          logDir=File.separator+pathObjFile.substring(pathObjFile.indexOf("LOG_") );
          pathObj=pathObjFile.substring(0,pathObjFile.indexOf("LOG_") - 1);
      }

      String dir=pathFileArea+
          File.separator+
          pathObj+
          File.separator+
          arcmcr+logDir+
          File.separator;

      if (soloDir) {
          return dir;
      }
      else {
          return dir+this.getIdOggettoFile();
      }
  }

  public Object getFile(boolean bCheck) throws Exception {	  
	  	 //COMMENTATO PER ADESSO.......DA SPOSTARE LATO MODULISTICA OPPURE CON FLAG SOLO DA LI
         //if (bCheck) {
		  	 //Check dell'oggetto file sulla tabella dei check
	      /*   GD4_Oggetti_File_Check gd4ObjFileChk;
	   
	         gd4ObjFileChk = new GD4_Oggetti_File_Check(this.getIdOggettoFile(),varEnv.getUser(),varEnv);
	         gd4ObjFileChk.checkObjFileToUser();
         }*/
         
	  	 if (isOggettoFileTemp || file!=null)
	  		 return file;
	  	 
	  	 if (!Global.nvl(getIdSyncro(),"").equals("") ) {
	  		 //Caso Sistema esterno
	  		 
	  		 if(Global.nvl(this.getChiaveServizioEsterno(),"").equals("RIVERSATO_DA_GDM")) {
	  			//Caso Sistema interno (quindi diretto....è stato riversato nel GDMSYNCRO dal GDM stesso
	  			GDMSyncroCore gdmSyncroCore = new GDMSyncroCore(this.getIdServizioEsterno(),this.getIdSyncro(),null,this.getIdOggettoFile(),this.varEnv);	  				  		
	  			return gdmSyncroCore.download();
	  		 }
	  		 else if (!Global.nvl(getServlet_Imposyncro(),"").equals(""))  {
	  			 
	  			 	//Caso Sistema esterno con servlet (quindi indiretto....è stato riversato dal GDMSYNCRO e non con lib interna 
	  			 
			  		 /* Genero token */
			  		 
			  		String token="";
			  		try  {	 
				  		 ManageToken mt = new ManageToken(this.varEnv.getUser(),this.varEnv.Global,varEnv.getDbOp());
				  		 token=mt.generateToken();
			  		}
			  		 catch (Exception e) {
			  			 throw new Exception("Errore in recupero file - Caso servizio esterno - Errore in generazione token. Errore= "+e.getMessage());
			  		 }
			  		 
			  		 String par="DOWNLOAD=1&IDSYNCRO="+getIdSyncro()+"&USERGDM="+getUserGDM_Imposyncro()+"&DSN="+getDsn_Imposyncro()+"&TOKEN="+token;  		 
			  		 CallServlet calls = new CallServlet(getServlet_Imposyncro(),par);
			  		 String sChiamata=getServlet_Imposyncro()+"?"+par;
			  		 //System.out.println("MANNY--->1");
			  		 try  {	  			 
			  			 return new ByteArrayInputStream( calls.call()); 	  			
			  		 }
			  		 catch (MalformedURLException e) {
			  			 throw new Exception("Errore in recupero file - Caso servizio esterno - Url malformata. Per dettagli sull'errore eseguire la chiamata "+sChiamata+"\nErrore="+e.getMessage());
			  		 }
			  		 catch (IOException e) {
			  			 throw new Exception("Errore in recupero file - Caso servizio esterno - Errore del servizio. Per dettagli sull'errore eseguire la chiamata "+sChiamata+"\nErrore="+e.getMessage());
			  		 }
			  		 catch (Exception e) {
			  			 throw new Exception("Errore in recupero file - Caso servizio esterno - Errore generico. Per dettagli sull'errore eseguire la chiamata "+sChiamata+"\nErrore="+e.getMessage());
			  		 }
	  		 }
	  	 }
	  	 else if (!Global.nvl(pathFileArea,"").equals("") && 
	         !Global.nvl(arcmcr,"").equals("") &&
	         !Global.nvl(pathObjFile,"").equals("") ) {
	  		 //Caso file system
	    	 String path=getPathFS(false);

            // System.out.println("AAAA1->"+path);
	    	// System.out.println("AAAA2->"+(new File(path)).exists());
	    	 
	    	 InputStream is=null;
	    	 File fEx = new File(path);
       	     if (fEx.exists()){               	  
           	  //LetturaScritturaFileFS f = new LetturaScritturaFileFS(path);

           	  closeFile(true);            	  
           	  //is=f.leggiFile();
              is = new BufferedInputStream(new FileInputStream(new File(path)));

                // System.out.println("AAAA3->"+is);
         	 }

       	     return is;
	     }
	     
         return file;
  }  
  
  private long getDimMaxAllegati(String sDimMax)  {
	      if (Global.nvl(sDimMax, "").equals("")) return -1;
	      
	      long allmax;
	      String sBloccante;
	      
	      allmax=Long.parseLong(sDimMax.substring(0,sDimMax.indexOf("@") ));
	      sBloccante=sDimMax.substring(sDimMax.indexOf("@") + 1);
	      
	      if (sBloccante.equals("N")) return -1;
	      
	      return allmax;
  }
   
  private void checkDimMaxAllegati(long idOggettoFile, long dimMax) throws Exception {
	      if (dimMax==-1) return;
	      
	      if (this.getForzaPerMidMax().equals("Y")) return;
	      
	      long dimAllegato=-1;
	      StringBuffer strStm = new StringBuffer("");
	      strStm.append("SELECT dbms_lob.getlength(testoocr) ");
	      strStm.append("from oggetti_file ");
	      strStm.append("where ID_OGGETTO_FILE= "+idOggettoFile);
	      
	      try {
	    	  IDbOperationSQL dbOpSql = varEnv.getDbOp();
	    	  
	    	  dbOpSql.setStatement(strStm.toString());

	          dbOpSql.execute();

	          ResultSet rst = dbOpSql.getRstSet();
	                                  
	          if (rst.next()) dimAllegato=rst.getLong(1);
	      }
	      catch (Exception e) {	    	  
	    	  throw new Exception("GD4_Oggetti_File::checkDimMaxAllegati\n Errore in esecuzione query per dimensione allegato.\nSQL="+strStm.toString()+"\nErrore: "+ e.getMessage());
	      }
	      	      
	      if (dimAllegato>dimMax)
	    	  throw new DocumentException(Global.CODERROR_SAVEDOCUMENT_ALLEGATIMAXDIM,"Attenzione! E' stata superata la dimensione massima prevista per gli allegati. Dim max = "+dimMax+" byte , Dim Allegato = "+dimAllegato+" byte.");
  }
  
  public boolean isOgfiLog() {
	    return bogfilog;
  }


  public void setOgfiLog(boolean bogfilog) {
		this.bogfilog = bogfilog;
  }   
  
  public boolean isbLogEseguito() {
		 return bLogEseguito;
	}
}