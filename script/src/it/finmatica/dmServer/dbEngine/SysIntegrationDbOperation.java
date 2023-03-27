package it.finmatica.dmServer.dbEngine;

import java.sql.ResultSet;
import java.util.Vector;

import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.sysIntegration.util.SysIntegrationConstant;
import it.finmatica.dmServer.util.Global;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;

public class SysIntegrationDbOperation extends MasterEngine {
	   private final String _OGGETTIFILE="OGGETTI_FILE";
	   private final String _OGGETTIFILE_FK="ID_DOCUMENTO";
	
	   public SysIntegrationDbOperation(Environment en,IDbOperationSQL dbOp, String msgElapsedTime) {
		      super(en,dbOp,msgElapsedTime);
	   }	
	   
	   public ResultSet retrieveModelAndType(String area, String cm, String type) throws Exception {
		      StringBuffer sStm = new StringBuffer();  
		      
		      sStm.append("select XML_SYNCRODESCR syncrodescr,");
		      sStm.append("       XML_DECODEREMOTEMODEL decoderemotemodel,");
		      sStm.append("       LOGITEM,");
		      sStm.append("       URLSERVICE,");
		      sStm.append("       XML_OTHERKEY_DOCUMENT_RULE otherdocrule,");
		      sStm.append("       CLASSIMPLEMENTATION ");
		      sStm.append("FROM SYSINTEGRATION_MODEL,SYSINTEGRATION_TYPE ");
		      sStm.append("WHERE SYSINTEGRATION_MODEL.area= :P_AREA ");
		      sStm.append("AND   SYSINTEGRATION_MODEL.codice_modello= :P_CM ");
		      sStm.append("AND   SYSINTEGRATION_MODEL.TYPE_INTEGRATION= :P_TYPE ");
		      sStm.append("AND   SYSINTEGRATION_MODEL.TYPE_INTEGRATION= SYSINTEGRATION_TYPE.TYPE_INTEGRATION ");
		      sStm.append("AND   SYSINTEGRATION_TYPE.ACTIVE= 1 ");
		      
		      super.appendStatement(sStm.toString());		      		      
		      
		      super.appendParameter(":P_AREA",area);
		      super.appendParameter(":P_CM",cm);
		      super.appendParameter(":P_TYPE",type);
		      
		      return super.executeSqlResultSet();
	   }
	   
	   public ResultSet retrieveField(String area, String cm, String type) throws Exception {
		   	  StringBuffer sStm = new StringBuffer();
		   	  
		   	  sStm.append("Select FIELD,");
		   	  sStm.append("		  FIELD_REMOTENAME,");
		   	  sStm.append("		  ORDERFIELD,");
		   	  sStm.append("		  KEYFIELD,");
		   	  sStm.append("		  ACTIVE,");
		   	  sStm.append("		  XML_OPTION_EXTRA_INSTRUCTION optextraisntruct ");
		   	  sStm.append("FROM SYSINTEGRATION_FIELD ");
		      sStm.append("WHERE TYPE_INTEGRATION = :P_TYPE ");
		      sStm.append("  AND AREA = :P_AREA ");
		      sStm.append("  AND CODICE_MODELLO = :P_CM ");
		      sStm.append("  AND ACTIVE = 1 ");
		      sStm.append("ORDER BY ORDERFIELD");
		      
		      super.appendStatement(sStm.toString());
		      
		      super.appendParameter(":P_TYPE",type);	
		      super.appendParameter(":P_AREA",area);
		      super.appendParameter(":P_CM",cm);
		      	            
		   	  
		   	  return super.executeSqlResultSet();
	   }
	   
	   public ResultSet checkDocumentIntegration(String idDocumento, String type) throws Exception {
		   	  StringBuffer sStm = new StringBuffer();
		   	  
		   	  if (type==null) {
			   	  sStm.append("Select SYSINTEGRATION_MODEL.AREA,");
			   	  sStm.append("       SYSINTEGRATION_MODEL.CODICE_MODELLO, ");
			      sStm.append("       SYSINTEGRATION_MODEL.TYPE_INTEGRATION ");
			   	  sStm.append(" FROM DOCUMENTI,MODELLI,SYSINTEGRATION_MODEL,SYSINTEGRATION_TYPE ");
			   	  sStm.append("WHERE DOCUMENTI.ID_DOCUMENTO = :P_IDDOC ");
			      sStm.append("  AND MODELLI.ID_TIPODOC = DOCUMENTI.ID_TIPODOC ");
			      sStm.append("  AND SYSINTEGRATION_MODEL.AREA = DOCUMENTI.AREA ");
			      sStm.append("  AND SYSINTEGRATION_MODEL.CODICE_MODELLO = MODELLI.CODICE_MODELLO ");
			      sStm.append("  AND SYSINTEGRATION_MODEL.TYPE_INTEGRATION = SYSINTEGRATION_TYPE.TYPE_INTEGRATION ");
			      sStm.append("  AND SYSINTEGRATION_TYPE.ACTIVE= 1 ");
		   	  }
		   	  else {
		   		  sStm.append("Select SYSINTEGRATION_MODEL.AREA,");
			   	  sStm.append("       SYSINTEGRATION_MODEL.CODICE_MODELLO, ");
			      sStm.append("       SYSINTEGRATION_MODEL.TYPE_INTEGRATION ");
			   	  sStm.append(" FROM SYSINTEGRATION_MODEL,SYSINTEGRATION_TYPE ");
			   	  sStm.append("WHERE SYSINTEGRATION_MODEL.TYPE_INTEGRATION = :P_TYPE "); 
			   	  sStm.append("  AND SYSINTEGRATION_MODEL.TYPE_INTEGRATION = SYSINTEGRATION_TYPE.TYPE_INTEGRATION ");
			      sStm.append("  AND SYSINTEGRATION_TYPE.ACTIVE= 1 ");
		   	  }
		    
		      super.appendStatement(sStm.toString());
		      
		      if (type==null) 
		    	  super.appendParameter(":P_IDDOC",idDocumento);
		      else
		    	  super.appendParameter(":P_TYPE",type);
		   	  
		   	  return super.executeSqlResultSet();
	   }
	   
	   public boolean checkSysIntegrationPending(String type,String idDocument,String xmlOption,String parmRet[]) throws Exception {
		   	  parmRet[0]=null;
		   	  
		      StringBuffer sStm = new StringBuffer();
		   	  
		   	  //1. Controllo se esiste il record per le chiavi passate
		   	  sStm.append("SELECT COUNT(*),max(IDENTIFIER_REMOTE_OBJECT) ");
		   	  sStm.append("  FROM SYSINTEGRATION_PENDING ");
		   	  sStm.append(" WHERE TYPE_INTEGRATION = :P_TYPE ");
		   	  sStm.append("   AND ID_DOCUMENTO = :P_IDDOC ");
		   	  if (xmlOption.equals(""))
		   		  sStm.append("   AND XML_OPTION_OTHERKEY IS NULL ");
		   	  else
		   		  sStm.append("   AND XML_OPTION_OTHERKEY = :P_XMLOTHER ");		   	  
		   	  
		   	  super.appendStatement(sStm.toString());
		      super.appendParameter(":P_TYPE",type);
		 	  super.appendParameter(":P_IDDOC",idDocument);
		 	  if (!xmlOption.equals("")) super.appendParameter(":P_XMLOTHER",xmlOption);
		 	 
		 	  ResultSet rst=null;
		 	  try {
		 		rst = super.executeSqlResultSet();		   	  
		 		
		 		rst.next();
		 	  }
		 	  catch (Exception e) {
		 		throw new Exception("SysIntegrationDbOperation::insertSysIntegrationPending - Errore in ricerca record su SYSINTEGRATION_PENDING (SQL="+sStm.toString()+")\n"+
		 				            e.getMessage());
		 	  }
		 	  
		 	  if (rst.getLong(1)>0) {
		 		 parmRet[0]=rst.getString(2);
		 		 return true;
		 	  }		 		  
		 		 
		 	  return false;
	   }
	   
	   public void updateSysIntegrationPending(String type,String idDocument,String xmlOption,
			   								  String pending,String lastStatus,String lastError,
			   								  String idRemObjRet) throws Exception {
		      StringBuffer sStm = new StringBuffer();
		      
		      sStm.append("UPDATE SYSINTEGRATION_PENDING ");
		      sStm.append("   SET UPDATEDATE=SYSDATE,");
		      
		      if (pending.equals(SysIntegrationConstant._PENDING)) {
		    	  sStm.append("   PENDING="+pending+",");
		    	  sStm.append("   LASTDATE_PENDING=SYSDATE,");
		      } 
		      else if (pending.equals(SysIntegrationConstant._NOPENDING)) {
		    	  sStm.append("   PENDING=0,");		    	  
		      }
		    	   
		      sStm.append("       LASTSTATUS='"+lastStatus+"'");
		      if (!lastError.equals("")) {
		    	  sStm.append("   ,LASTERROR=:P_LASTERR"); 
		      }
		      
		      if (idRemObjRet==null)
		    	  sStm.append("       ,IDENTIFIER_REMOTE_OBJECT=NULL");
		      else
		    	  sStm.append("       ,IDENTIFIER_REMOTE_OBJECT=:P_REMOBJ");
		      		    
		      sStm.append(" WHERE TYPE_INTEGRATION = :P_TYPE");
		   	  sStm.append("   AND ID_DOCUMENTO = :P_IDDOC ");
		   	  if (xmlOption.equals(""))
		   		  sStm.append("   AND XML_OPTION_OTHERKEY IS NULL ");
		   	  else
		   		  sStm.append("   AND XML_OPTION_OTHERKEY = :P_XMLOTHER ");	
		   	  
		   	  super.appendStatement(sStm.toString());
		   	  if (!lastError.equals("")) super.appendParameter(":P_LASTERR",lastError);
		   	  if (idRemObjRet!=null) super.appendParameter(":P_REMOBJ",idRemObjRet);
		      super.appendParameter(":P_TYPE",type);
		 	  super.appendParameter(":P_IDDOC",idDocument);
		 	  if (!xmlOption.equals("")) super.appendParameter(":P_XMLOTHER",xmlOption);
		 	  
		      
		 	  super.executeSql();
	   }
	   
	   public void insertSysIntegrationPending(String type,String idDocument,String xmlOption) throws Exception {
		   	  StringBuffer sStm = new StringBuffer();
		   	  		   	  
		   	  sStm.append("INSERT INTO SYSINTEGRATION_PENDING ");
		   	  sStm.append("(IDSYSPENDING,TYPE_INTEGRATION,ID_DOCUMENTO,XML_OPTION_OTHERKEY,CREATEDATE,UPDATEDATE,");
		      sStm.append("PENDING,LASTDATE_PENDING,LASTSTATUS)");
		      sStm.append(" VALUES ");
		      sStm.append("(SYPE_SQ.NEXTVAL,:P_TYPE,:P_IDDOCUMENTO,");
		      if (xmlOption.equals(""))
		    	  sStm.append("NULL");
		      else
		    	  sStm.append(":P_XMLOPT");
		      sStm.append(",SYSDATE,SYSDATE,");
		      sStm.append("1,SYSDATE,'"+SysIntegrationConstant._LASTSTATUS_PENDING_WAIT+"')");
		      
		      super.appendStatement(sStm.toString());
		      super.appendParameter(":P_TYPE",type);
		 	  super.appendParameter(":P_IDDOCUMENTO",idDocument);
		 	  		 	  
		 	  if (!xmlOption.equals(""))		 		  
		 		  super.appendParameter(":P_XMLOPT",xmlOption);
		 	  
		 	  super.executeSql();
	   }
	   
	   public void insertSysIntegrationPendingLog(String type,String idDocument,String xmlOption,
			   									  String action,String actor,String user,String result,String errString) throws Exception {
		   	  StringBuffer sStmSubQuery = new StringBuffer();
		   	  
		   	  sStmSubQuery.append("SELECT IDSYSPENDING ");
		   	  sStmSubQuery.append("FROM SYSINTEGRATION_PENDING ");
		   	  sStmSubQuery.append(" WHERE TYPE_INTEGRATION = :P_TYPE");
		      sStmSubQuery.append("   AND ID_DOCUMENTO = :P_IDDOC ");
		   	  if (xmlOption.equals(""))
		   		  sStmSubQuery.append("   AND XML_OPTION_OTHERKEY IS NULL ");
		   	  else
		   		  sStmSubQuery.append("   AND XML_OPTION_OTHERKEY = :P_XMLOTHER ");	
		   
		   	  StringBuffer sStm = new StringBuffer();
		   	  
		   	  sStm.append("INSERT INTO SYSINTEGRATION_PENDING_LOG ");
		   	  sStm.append("(IDLOGPENDING,IDSYSPENDING,DATELOG,ACTION,ACTOR,\"USER\",RESULT,ERRORSTRING)");
		   	  sStm.append(" SELECT ");
		      sStm.append(" SYPE_LOG_SQ.NEXTVAL,("+sStmSubQuery.toString()+"), ");
		      sStm.append(" SYSDATE, ");
		      sStm.append(" :P_ACTION, ");
		      sStm.append(" :P_ACTOR, ");
		      sStm.append(" :P_USER, ");
		      sStm.append(" :P_RESULT, ");
		      sStm.append(" :P_ERRSTRING ");
		      sStm.append("FROM DUAL ");
		     
		      super.appendStatement(sStm.toString());
		      super.appendParameter(":P_TYPE",type);
		 	  super.appendParameter(":P_IDDOC",idDocument);
		 	  		 	  
		 	  if (!xmlOption.equals(""))		 		  
		 		  super.appendParameter(":P_XMLOTHER",xmlOption);
		 	  
		 	  super.appendParameter(":P_ACTION",action);
		 	  super.appendParameter(":P_ACTOR",actor);
		 	  super.appendParameter(":P_USER",user);
		 	  super.appendParameter(":P_RESULT",result);
		 	  super.appendParameter(":P_ERRSTRING",errString);
		 	  
		 	  super.executeSql();
		   	  
	   }
	   
	   public Vector<String> getValueFieldFromGenericIdDocSql(String table, String field, String idDocument) throws Exception {
		   	  Vector<String> vRet = new Vector<String>();
		      StringBuffer sStm = new StringBuffer();
		   	  
		   	  sStm.append("SELECT "+field);
		   	  sStm.append("  FROM "+table);
		   	  sStm.append(" WHERE "+getFKIdDocFromTable(table));
		   	  sStm.append("       = "+idDocument);
		   	  
		   	  super.appendStatement(sStm.toString());
		   	  
		      ResultSet rst = super.executeSqlResultSet();
		      
		      while (rst.next()) {
		    	  vRet.add(rst.getString(1));			      
		      }
		    	  
		      return vRet;
	   }	   	   	   
	   
	   public Vector<Object[]> getPendingItemList(String typeIntegration) throws Exception {
		      
		      Vector<Object[]> vRet = new Vector<Object[]>();
		      		      
		      StringBuffer sStm = new StringBuffer();
		      sStm.append("SELECT SYSINTEGRATION_PENDING.ID_DOCUMENTO,XML_OPTION_OTHERKEY,MODELLI.AREA,CODICE_MODELLO,IDENTIFIER_REMOTE_OBJECT ");
		      sStm.append("  FROM SYSINTEGRATION_PENDING,DOCUMENTI,MODELLI");
		      sStm.append(" WHERE SYSINTEGRATION_PENDING.PENDING=1");
		      sStm.append("   AND SYSINTEGRATION_PENDING.TYPE_INTEGRATION=:P_TYPE");
		      sStm.append("   AND DOCUMENTI.ID_DOCUMENTO=SYSINTEGRATION_PENDING.ID_DOCUMENTO");
		      sStm.append("   AND MODELLI.ID_TIPODOC=DOCUMENTI.ID_TIPODOC");
		      sStm.append(" ORDER BY UPDATEDATE DESC");
		      
		      super.appendStatement(sStm.toString());
		      super.appendParameter(":P_TYPE",typeIntegration);
		      
		 	  ResultSet rst=null;
		 	  try {
		 		rst = super.executeSqlResultSet();		   	  		 				 		
		 	  }
		 	  catch (Exception e) {
		 		throw new Exception("SysIntegrationDbOperation::getPendingItemList - Errore in ricerca elementi in pending (SQL="+sStm.toString()+")\n"+
		 				            e.getMessage());
		 	  }
		 	  		 	  
		 	  while (rst.next()) 		 		
		 		vRet.add(new Object[]{rst.getString(1),Global.nvl(rst.getString(2),""),rst.getString(3),rst.getString(4),rst.getString(5)});		 				 	  
		      
		      return vRet;
	   }
	   
	   private String getFKIdDocFromTable(String table) {
		       if (table.toUpperCase().equals(_OGGETTIFILE))
		          return _OGGETTIFILE_FK;
		       //Aggiungere qui gli eventuali altri elseif
		       //sulle altre tabelle che servono (aggiungendo
		       //anche le relative costanti
		       
		       return "";	  		    	   		    	   
	   }
}