package it.finmatica.syncroremotedocument;

import java.sql.ResultSet;
import java.util.Vector;

import it.finmatica.jfc.dbUtil.IDbOperationSQL;

public class Syncro {
	   private IDbOperationSQL dbOp;
	   private long idDocMaster;
	   private long idObjFileDoc;
	   private String applicativo;
	   private String ente;
	   

	   public Syncro(IDbOperationSQL dbOp, long idDocMaster, String applicativo) {
		   	  this(dbOp,idDocMaster,-1,applicativo);
	   }
	
	   public Syncro(IDbOperationSQL dbOp, long idDocMaster, long idObjFileDoc, String applicativo) {
		   	  this.dbOp=dbOp;
		      this.idDocMaster=idDocMaster;
		      this.idObjFileDoc=idObjFileDoc;
		      this.applicativo=applicativo;
	   }
	   
	   public String createUpadateSyncroDocument(String idDocRemote, String acronym_typeDoc, 
	       	      					            String action, String lastActionReport ) throws Exception {
		      return createUpadateSyncroDocument( idDocRemote,  acronym_typeDoc,  action, -1,  lastActionReport );
	   }
	   
	   public String getEnte() {
			return ente;
	   }

	   public void setEnte(String ente) {
			this.ente = ente;
	   }	   
	   
	   public String createUpadateSyncroDocument(String idDocRemote, String acronym_typeDoc, 
			   				       	             String action, long idSyncroFather, String lastActionReport ) throws Exception {
		   	  String idSyncro = null;
		   	  		   	  
		   	  idSyncro=this.getIdSyncroFromIdGDMDocument();
		   	  if (idSyncro!=null) {
		   		 updateSyncroDocument(idSyncro, idDocRemote,action,  lastActionReport);
		   		 return idSyncro;
		   	  }
		   		  		   	  
		      
		   	  StringBuffer sStm = new StringBuffer("INSERT INTO SYNCRO_REMOTE_DOCUMENT ");
		   
		      sStm.append("(ID_SYNCRO,ID_DOCUMENTO_GDM,APPLICATIVO,ACRONIM_TYPE_DOC,");
		      sStm.append("ID_DOCUMENTO_REMOTO,ULTIMA_AZIONE,DATA_ULTIMA_AZIONE");
		      if (idSyncroFather!=-1)	sStm.append(",ID_SYNCRO_RIF");
		      if (idObjFileDoc!=-1)  	sStm.append(",ID_OBJFILE_ALLEGATO_GDM");
		      if (ente!=null)  	sStm.append(",ENTE");
		      sStm.append(",LAST_ACTION_REPORT)");
		      sStm.append(" VALUES ");
		      sStm.append("(:P_ID_SYNCRO,:P_ID_DOCUMENTO_GDM,:P_APPLICATIVO,:P_ACRONIM_TYPE_DOC,");
		      sStm.append(":P_ID_DOCUMENTO_REMOTO,:P_ULTIMA_AZIONE,sysdate");
		      if (idSyncroFather!=-1)	sStm.append(",:P_ID_SYNCRO_RIF");
		      if (idObjFileDoc!=-1)  	sStm.append(",:P_ID_OBJFILE_ALLEGATO_GDM");
		      if (ente!=null)  	sStm.append(",:P_ENTE");
		      sStm.append(",:P_LAST_ACTION_REPORT)");
		      
		      try {
		        idSyncro =""+dbOp.getNextKeyFromSequence("SEQ_SYN_REM_DOC");
		      }
		      catch(Exception e) {
		    	throw new Exception("Syncro::createSyncroDocument - Errore in generazione sequence (SEQ_SYN_REM_DOC).\n Errore= "+
		    						e.getMessage());
		      }
		      
		      dbOp.setStatement(sStm.toString());
		      
		      dbOp.setParameter(":P_ID_SYNCRO", Long.parseLong(idSyncro));
		      dbOp.setParameter(":P_ID_DOCUMENTO_GDM", idDocMaster);
		      dbOp.setParameter(":P_APPLICATIVO", applicativo);
		      dbOp.setParameter(":P_ACRONIM_TYPE_DOC", acronym_typeDoc);
		      dbOp.setParameter(":P_ID_DOCUMENTO_REMOTO", idDocRemote);
		      dbOp.setParameter(":P_ULTIMA_AZIONE", action);
		      if (idSyncroFather!=-1)		    	  		    	
		    	  dbOp.setParameter(":P_ID_SYNCRO_RIF", idSyncroFather);
		      if (idObjFileDoc!=-1)  	
		    	  dbOp.setParameter(":P_ID_OBJFILE_ALLEGATO_GDM", idObjFileDoc);
		      if (ente!=null)  	
		    	  dbOp.setParameter(":P_ENTE", ente);
		      dbOp.setParameter(":P_LAST_ACTION_REPORT", lastActionReport);
		      
		      try {
		    	dbOp.execute(); 
		      }
		      catch(Exception e) {
		    	    try {dbOp.rollback();}catch(Exception ei) {}
			    	throw new Exception("Syncro::createSyncroDocument - Errore in creazione riga su SYNCRO_REMOTE_DOCUMENT"+
			    					   ". SQL="+sStm.toString()+" (idDocMaster="+idDocMaster+")"+
			    					   "\nErrore= "+e.getMessage());
			  }
		      
		      return idSyncro;
	   }
	   
	   private void updateSyncroDocument(String idSyncroDoc, String idDocRemote, String action, String lastActionReport)  throws Exception {
		   	  StringBuffer sStm = new StringBuffer("UPDATE SYNCRO_REMOTE_DOCUMENT SET ");
		   	  
		   	  sStm.append("ULTIMA_AZIONE=:P_ULT_AZIONE,");
		   	  sStm.append("DATA_ULTIMA_AZIONE=sysdate,");
		   	  if (ente!=null)  	sStm.append("ENTE=:P_ENTE,");
		   	  sStm.append("LAST_ACTION_REPORT=:P_LASTA_ACT");
		   	  if (idDocRemote!=null)
		     	 sStm.append(",ID_DOCUMENTO_REMOTO=:P_ID_DOCUMENTO_REMOTO");
		 
		   	  sStm.append(" WHERE ID_SYNCRO=:P_ID ");
		   	  
		   	  dbOp.setStatement(sStm.toString());		      		
		      
		      dbOp.setParameter(":P_ULT_AZIONE", action);	
		      if (ente!=null)  	
		    	  dbOp.setParameter(":P_ENTE",ente);
		      dbOp.setParameter(":P_LASTA_ACT",lastActionReport);
		      if (idDocRemote!=null)
		    	  dbOp.setParameter(":P_ID_DOCUMENTO_REMOTO",idDocRemote);		      
		      dbOp.setParameter(":P_ID", Long.parseLong(idSyncroDoc));
		      
		      try {
			    dbOp.execute(); 
		      }
		      catch(Exception e) {
		    	    try {dbOp.rollback();}catch(Exception ei) {}
			    	throw new Exception("Syncro::updateSyncroDocument - Errore in aggiornamento riga su SYNCRO_REMOTE_DOCUMENT"+
			    					   ". SQL="+sStm.toString()+" (idDocMaster="+idDocMaster+")"+
			    					   "\nErrore= "+e.getMessage());
			  }
		      
	   }
	   
	   public String getIdSyncroFromIdGDMDocument() throws Exception {
		      return getIdSyncroFromIdGDMDocument(idDocMaster, idObjFileDoc);
	   }
	   
	   public String getIdSyncroFromIdGDMDocument(long thisidDoc, long thisidObjFileDoc) throws Exception {
		   	  StringBuffer sStm = new StringBuffer("SELECT ID_SYNCRO FROM SYNCRO_REMOTE_DOCUMENT ");
		   	  
		   	  sStm.append("WHERE ID_DOCUMENTO_GDM=:P_IDDOC ");
		   	  sStm.append("  AND APPLICATIVO=:P_APPLICATIVO ");
		   	  if (thisidObjFileDoc!=-1) {
			      sStm.append("  AND ID_OBJFILE_ALLEGATO_GDM= "+thisidObjFileDoc);
			  }
		   	  
		   	  dbOp.setStatement(sStm.toString());	
		   	  dbOp.setParameter(":P_IDDOC", thisidDoc);
		   	  dbOp.setParameter(":P_APPLICATIVO", applicativo);
		   	  
		      try {
			    dbOp.execute();
			    
			    ResultSet rst = dbOp.getRstSet();
			    
			    if (rst.next()) {
			    	return rst.getString(1);
			    }
			    else {
			    	return null;
			    }
		      }
		      catch(Exception e) {
			    	throw new Exception("Syncro::getIdSyncroFromIdGDMDocument - Errore in recupero idSyncro da documentomaster"+
			    					   ". SQL="+sStm.toString()+" (idDocMaster="+idDocMaster+", idObjFileDoc="+idObjFileDoc+")"+
			    					   "\nErrore= "+e.getMessage());
			  }	   
	   }
	   
	   public String getRemoteDocument() throws Exception {
		   	  StringBuffer sStm = new StringBuffer("SELECT ID_DOCUMENTO_REMOTO FROM SYNCRO_REMOTE_DOCUMENT ");
		   	  
		   	  sStm.append("WHERE ID_DOCUMENTO_GDM=:P_IDDOC ");
		   	  sStm.append("  AND APPLICATIVO=:P_APPLICATIVO ");
		   	  if (idObjFileDoc!=-1) {
		   		sStm.append("  AND ID_OBJFILE_ALLEGATO_GDM= "+idObjFileDoc);
		   	  }
		   	  
		   	  dbOp.setStatement(sStm.toString());	
		   	  dbOp.setParameter(":P_IDDOC", idDocMaster);
		   	  dbOp.setParameter(":P_APPLICATIVO", applicativo);
		   	  
		      try {
			    dbOp.execute();
			    
			    ResultSet rst = dbOp.getRstSet();
			    
			    if (rst.next()) {
			    	return rst.getString(1);
			    }
			    else {
			    	return null;
			    }
		      }
		      catch(Exception e) {
			    	throw new Exception("Syncro::getRemoteDocument - Errore in recupero documentoremoto da documentomaster"+
			    					   ". SQL="+sStm.toString()+" (idDocMaster="+idDocMaster+", idObjFileDoc="+idObjFileDoc+")"+
			    					   "\nErrore= "+e.getMessage());
			  }
	   }
	   
	   public String getGDMDocument(String idDocumentoRemoto) throws Exception {
		   	  StringBuffer sStm = new StringBuffer("SELECT ID_DOCUMENTO_GDM  FROM SYNCRO_REMOTE_DOCUMENT ");
		   	  
		   	  sStm.append("WHERE ID_DOCUMENTO_REMOTO=:P_IDDOC ");
		   	  sStm.append("  AND APPLICATIVO=:P_APPLICATIVO ");
		   	  if (idObjFileDoc!=-1) {
		   		sStm.append("  AND ID_OBJFILE_ALLEGATO_GDM= "+idObjFileDoc);
		   	  }
		   	  
		   	  dbOp.setStatement(sStm.toString());	
		   	  dbOp.setParameter(":P_IDDOC", idDocumentoRemoto);
		   	  dbOp.setParameter(":P_APPLICATIVO", applicativo);
		   	  
		      try {
			    dbOp.execute();
			    
			    ResultSet rst = dbOp.getRstSet();
			    
			    if (rst.next()) {
			    	return rst.getString(1);
			    }
			    else {
			    	return null;
			    }
		      }
		      catch(Exception e) {
			    	throw new Exception("Syncro::getGDMDocument - Errore in recupero documento GDM da idDocumentoRemoto"+
			    					   ". SQL="+sStm.toString()+" (idDocumentoRemoto="+idDocumentoRemoto+", idObjFileDoc="+idObjFileDoc+")"+
			    					   "\nErrore= "+e.getMessage());
			  }
	   }	   	   
	   
	   public void removeSyncroDoc(String idSyncroDoc, boolean bCommit) throws Exception {
		   	  //Prima cancello gli eventuali figli
		   	  StringBuffer sStm = new StringBuffer("DELETE FROM SYNCRO_REMOTE_DOCUMENT WHERE ID_SYNCRO_RIF = :P_IDSYNCRO");
		   	  		   	  
		   	  dbOp.setStatement(sStm.toString());	
		   	  dbOp.setParameter(":P_IDSYNCRO", idSyncroDoc);

			  try {
				  dbOp.execute();
			  }
		      catch(Exception e) {		
		    	    try{dbOp.rollback();}catch(Exception ei) {}
			    	throw new Exception("Syncro::removeSyncroDoc - Errore in cancellazione figli syncro "+
			    					   ". SQL="+sStm.toString()+" (ID_SYNCRO_RIF="+idSyncroDoc+")"+
			    					   "\nErrore= "+e.getMessage());
			  }
		      
		      sStm = new StringBuffer("DELETE FROM SYNCRO_REMOTE_DOCUMENT WHERE ID_SYNCRO = :P_IDSYNCRO");
	  		   	  
		   	  dbOp.setStatement(sStm.toString());	
		   	  dbOp.setParameter(":P_IDSYNCRO", idSyncroDoc);

			  try {
				  dbOp.execute();
			  }
		      catch(Exception e) {
		    	    try{dbOp.rollback();}catch(Exception ei) {}
			    	throw new Exception("Syncro::removeSyncroDoc - Errore in cancellazione padre syncro "+
			    					   ". SQL="+sStm.toString()+" (ID_SYNCRO="+idSyncroDoc+")"+
			    					   "\nErrore= "+e.getMessage());
			  }		
		      
		      if (bCommit) try{dbOp.commit();}catch(Exception e) {}
		   	  
	   }
	   
	   public Vector<SyncroTable> retrieve(String whereCondition, String orderBy) throws Exception {
		   	  Vector<SyncroTable> vRet = new Vector<SyncroTable>();
		   	  StringBuffer sStm = new StringBuffer("SELECT ID_SYNCRO,ID_DOCUMENTO_GDM,");
		   	  sStm.append("ID_OBJFILE_ALLEGATO_GDM,APPLICATIVO,ACRONIM_TYPE_DOC,");
		   	  sStm.append("ID_DOCUMENTO_REMOTO,ULTIMA_AZIONE,TO_CHAR(DATA_ULTIMA_AZIONE,'dd/mm/yyyy hh24:mi:ss') DUA,");
		   	  sStm.append("ID_SYNCRO_RIF,LAST_ACTION_REPORT,ENTE	FROM SYNCRO_REMOTE_DOCUMENT ");
		   	   
		   	  if (whereCondition.equals("")) whereCondition="1=1";
		   	  if (orderBy.equals("")) orderBy="1";
		   	  
		   	  sStm.append("WHERE "+whereCondition);
		   	  sStm.append("ORDER BY "+orderBy);
		   	  
		   	  dbOp.setStatement(sStm.toString());
		   	  
		   	  try {
				 dbOp.execute();
			  }
		      catch(Exception e) {
		    	    try{dbOp.rollback();}catch(Exception ei) {}
			    	throw new Exception("Syncro::retrieve - Errore retrieve syncro "+
			    					   ". SQL="+sStm.toString()+
			    					   "\nErrore= "+e.getMessage());
			  }	
		      
		      ResultSet rst= dbOp.getRstSet();
		      while (rst.next())  {
		    	  SyncroTable st = new SyncroTable();
		    	  
		    	  st.setAcromymTypeDoc(rst.getString("ACRONIM_TYPE_DOC"));
		    	  st.setApplicativo(rst.getString("APPLICATIVO"));
		    	  st.setDataUltimaAzione(rst.getString("DUA"));
		    	  st.setIdDocumentoGDM(rst.getString("ID_DOCUMENTO_GDM"));
		    	  st.setIdDocumentoRemoto(rst.getString("ID_DOCUMENTO_REMOTO"));
		    	  st.setIdObjAllegatoGDM(rst.getString("ID_OBJFILE_ALLEGATO_GDM"));
		    	  st.setIdSyncroRif(rst.getString("ID_SYNCRO_RIF"));
		    	  st.setLastActionReport(rst.getString("LAST_ACTION_REPORT"));
		    	  st.setUltimaAzione(rst.getString("ULTIMA_AZIONE"));
		    	  st.setEnte(rst.getString("ENTE"));
		    	  
		    	  vRet.add(st);
		      }
		   	  
		      return vRet;
	   }
	    
}
