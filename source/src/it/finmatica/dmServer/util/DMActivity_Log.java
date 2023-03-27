package it.finmatica.dmServer.util;

import java.io.File;
import java.sql.ResultSet;
import java.util.Date;
import java.util.Vector;

import it.finmatica.dmServer.*;
import it.finmatica.dmServer.dbEngine.LogDocumentDbOperation;

public class DMActivity_Log {

	   private String idDoc, tipoAzione;
	   private Environment vEnv;	
	   private int id_log;
	   private String typeLog; //TipoLog Associato al documento
	   private String typeLogFile; //TipoLog Associato all'oggettoFile del documento
	   private ElapsedTime elpsTime;
	   private long lVersione = 0;	   
	   private LogDocumentDbOperation logDoc;
	   private String dataActivityLog;
	   private boolean creaVersione=false;
	   private Date dataLogCustom = null;
	   
	   private long ultimaVersione=0;

	//Creazione di un nuovo ACTIVITYLOG
	   public DMActivity_Log(String idDoc, String tipoAzione, Environment varEnv) throws Exception {
		   	  this.idDoc=idDoc;
		   	  this.tipoAzione=tipoAzione;
		   	  this.vEnv=varEnv;		   
		   	  logDoc = new LogDocumentDbOperation(vEnv,vEnv.getDbOp(),"DMActivity_Log");
			  elpsTime = new ElapsedTime("DMActivity_Log",varEnv);
		   	  // Get Id dalla sequence          		   	  
		   	  id_log = logDoc.getACLO_SQ();//vEnv.getDbOp().getNextKeyFromSequence("ACLO_SQ");
		   	  //System.out.println(id_log);
	   }
	   
	   //Accesso ad un ACTIVITYLOG esistente
	   public DMActivity_Log(int idLog, Environment varEnv) throws Exception {
		   	  this.id_log=idLog;
		   	  
		   	  retrieveActivityLog();
	   }
	   
	   //Accesso alla data di un ACTIVITYLOG esistente
	   public DMActivity_Log(int idDoc,  String tipoAzione, Environment varEnv) throws Exception {
		   	  this.idDoc=""+idDoc;
		   	  this.tipoAzione=tipoAzione;
		   	  this.vEnv=varEnv;	
		   	  
		   	  logDoc = new LogDocumentDbOperation(vEnv,vEnv.getDbOp(),"DMActivity_Log");
			  elpsTime = new ElapsedTime("DMActivity_Log",varEnv);
		   	 		   	
		   	  retrieveActivityLogData();
	   }	   
	   
	   public void setTypeLog(String typeLog){
		      this.typeLog=typeLog;
	   }
	   
	   public void setTypeLogFile(String typeLogFile){
		      this.typeLogFile=typeLogFile;
	   }		   
	   
	   public void creaVersione(boolean creaVersione) {
			  this.creaVersione = creaVersione;
	   }
	   
	   public void insertActivityLog() throws Exception
	   {      
			  try {
				 logDoc.insertActivityLog(lVersione,id_log,idDoc,tipoAzione,creaVersione);			
				 ultimaVersione=logDoc.getUltimaVersione();
			  }    
			  catch (Exception e) {
				   throw new Exception("DMActivity_Log::insertActivityLog " + e.getMessage());
			  }
      
	   }	   	   	   

	   public void insertAllVaLog(String idDocumento, Vector vListaIdModificati) throws Exception
	   {   
		      if (typeLog.equals(Global.TYPE_NO_LOG)) return;
		   
			  try {
				 logDoc.insertAllVaLog( idDocumento,  vListaIdModificati,  typeLog,  id_log);				 
			  }    
			  catch (Exception e) {
				   throw new Exception("DMActivity_Log::insertAllVaLog " + e.getMessage());
			  }      
	   }	     
	     
	   public void insertAllVaLogHorizontal(String idDocumento, String nomeTabHor, Vector vListaCampiModificati, Vector vlistaTipiCampoModificati,  boolean bNonRipetereUguali) throws Exception
	   {  
		      if (typeLog.equals(Global.TYPE_NO_LOG)) return;
		   
			  try {
				logDoc.insertAllVaLogHorizontal( idDocumento,  nomeTabHor,  vListaCampiModificati,  vlistaTipiCampoModificati,id_log,bNonRipetereUguali);					
			  }    
			  catch (Exception e) {
				   throw new Exception("DMActivity_Log::insertAllVaLogHorizontal " + e.getMessage());			   
			  }
	   }	  	  	 
	   	   
	   public void insertAllOgfiLog(String idDocumento, Vector vListaIdModificati, boolean bElimina, boolean bisNameId,boolean bNonRipetereUguali) throws Exception {
		      if (typeLog.equals(Global.TYPE_NO_LOG)) return;
		      if (typeLogFile.equals("N")) return;
		      		      		   		      
		      try {
		        logDoc.insertAllOgfiLog(idDocumento,vListaIdModificati,bElimina,id_log,tipoAzione,bisNameId,bNonRipetereUguali,dataLogCustom);		  
		      }
		      catch (Exception e) {
				throw new Exception("DMActivity_Log::insertAllOgfiLog \n" 							       
						            +"Errore: "+ e.getMessage());
			  }		      
	   }

	   public void f_log_documento() {      
		      if (typeLog.equals(Global.TYPE_NO_LOG)) return;
		   
			  try {
				logDoc.f_log_documento(id_log);
			  }    
			  catch (Exception e) {				
			  }      
	   }

	   private void retrieveActivityLog() throws Exception {      
			  try {				
		         ResultSet rst = logDoc.retrieveActivityLog(id_log);
		         
		         if (rst.next()) {
		        	 idDoc=""+rst.getLong(1);
		        	 tipoAzione=rst.getString(2);
		         }
			  }
			  catch (Exception e) {
				   throw new Exception("DMActivity_Log::retrieveActivityLog " + e.getMessage());
			  }      
	   }

		public long retrieveLastActivityLog() throws Exception {
			try {
				ResultSet rst = logDoc.retrieveLastActivityLog(idDoc);

				if (rst.next()) return rst.getLong(1);

			}
			catch (Exception e) {
				throw new Exception("DMActivity_Log::retrieveLastActivityLog " + e.getMessage());
			}

			return 0;
		}
	   
	   private void retrieveActivityLogData() throws Exception {      
			  try {				
		         ResultSet rst = logDoc.retrieveActivityLogData(idDoc,tipoAzione);
		         
		         if (rst.next()) {
		        	 dataActivityLog=""+rst.getString(1);		        	 
		         }
			  }
			  catch (Exception e) {
				   throw new Exception("DMActivity_Log::retrieveActivityLogData " + e.getMessage());
			  }      
	   }	   
	   
	   
	   /**
		 * Metodo che serve a scrivere su FS l'ogg. file log direttamente
		 * senza farlo contemporaneamente alla scrittura su FS
		 * degli oggetti file da cui deriva il log stesso.
		 * Questo serve quando si versiona un documento, dove
		 * siamo sicuri di voler solo "clonare" gli oggetti
		 * file dentro un log, mentre gli ogg. file principali
		 * rimangono invariati
	   */
	    public void scriviLogFs() throws Exception { 
	    	   Vector<String> v;
	    	   String pathFileArea,arcmcr;
	    	   
	           v = (new LookUpDMTable(vEnv)).lookUpInfoAr_Cm_Cr_Area(idDoc,null,false);
	           	           
	           pathFileArea=v.get(0);
	           if (pathFileArea==null) pathFileArea="";
	           arcmcr=v.get(1);
	           
	           //Non è gestito il FS, va solo con i BLOB
	           if (Global.nvl(pathFileArea,"").equals("")) return;
	          
	    	   StringBuffer sStm = new StringBuffer();
	    	   
	    	   sStm.append("SELECT filename,testoocr,nvl(PATH_FILE,''), ID_OGGETTO_FILE ");
	    	   sStm.append("  FROM OGGETTI_FILE ");
	    	   sStm.append(" WHERE ID_DOCUMENTO="+idDoc);
	    	   
	    	   try {
	    		 GD4_GestoreOggettiFile_FS gFs = new GD4_GestoreOggettiFile_FS(vEnv);
	    		 
	             vEnv.getDbOp().setStatement(sStm.toString());
	     	
	             vEnv.getDbOp().execute();	         
	         
	             ResultSet rst = vEnv.getDbOp().getRstSet();
	         
	             while (rst.next()) {
	            	 //Questo file è gestito a BLOB anche se l'area va con i FS....quindi lo salto	
	            	 if (Global.nvl(rst.getString(3),"").equals("")) continue;
	            	 
	            	 String dir=pathFileArea+
		     		            File.separator+rst.getString(3)+
		     		           File.separator+arcmcr;
	            	 
	            	 String dirLog=dir+File.separator+"LOG_"+id_log;
	            	 	            	 
	            	 gFs.scriviLog(dirLog, dir+File.separator+rst.getString(4), rst.getString(4),true);
	             }  	   
			  }    
			  catch (Exception e) {
				   throw new Exception("DMActivity_Log::scriviLogFs " + e.getMessage());
			  }  		           
	    }

	   public long getLVersione() {
		      return lVersione;
	   }

	   public void setLVersione(long versione) {
		      lVersione = versione;
	   }

	   public int getId_log() {
		      return id_log;
	   }

	   public String getTypeLogFile() {
		      return typeLogFile;
	   }	   
	   
	   public String getDataActivityLog() {
		   	  return dataActivityLog;
	   }
	   public long getUltimaVersione() {
			return ultimaVersione;
	   }
	   
		public void setDataLogCustom(Date dataLogCustom) {
			this.dataLogCustom = dataLogCustom;
		}
}
