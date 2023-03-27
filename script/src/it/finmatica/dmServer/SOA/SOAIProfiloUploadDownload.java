package it.finmatica.dmServer.SOA;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.StringTokenizer;
import java.util.Vector;

import it.finmatica.dmServer.management.Profilo;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.util.keyval;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;

import javax.servlet.http.HttpServletRequest;
  
public class SOAIProfiloUploadDownload extends SOAIGenericService {	   
	   public  IDbOperationSQL		dbOperationConnAtt		 = null;
	   private final String         _FILESEPARATOR           = ";";
	   private final String         _BLOB_COLUMN             = "BLOB";
	   private final String         _CLOB_COLUMN             = "CLOB";
	   private boolean bDontCloseConn = false;
	   
	   public SOAIProfiloUploadDownload(HttpServletRequest request,
	                                    IDbOperationSQL dbOp,
	                                    String area,
	                                    String cm,
	                                    String cr,
	                                    String idDocument) {
		   this.httpRequest=request;
		   this.user=""+request.getSession().getAttribute("Utente");		
	  	   this.dbOperation=dbOp;

	   	   this.areaModel=area;
	  	   this.cmModel=cm;
	 	   this.crModel=cr;
	 	   this.idDocument=idDocument;
	   }
	   
	   public void dontCloseDbOp() {
		   	  bDontCloseConn=true;
	   }
	   
	   public String upload(String connAttach, String tableAttachName,
			                String columnAttachName, String columnAttach,
			                String whereAttachCondition,
			                String fileAttach, String deleteFileFs,
			                String deleteFileDb) {	
		      Profilo p;		      		      
		      
		      int iCheckIntPar=checkIntegrityParameter();
		      		      
		      if (iCheckIntPar<0) {
		    	  if (!bDontCloseConn) closeDbOp();
		    	  return (new SOAXMLErrorRet(generateErrorParameterMessage(iCheckIntPar))).getXML();
		      }
		    	  		      
		      //Tolgo il CDATA da whereAttachCondition
		      if (whereAttachCondition.indexOf(_CDATAINIT)!=-1) 
		    	  whereAttachCondition=whereAttachCondition.substring(whereAttachCondition.indexOf(_CDATAINIT)+_CDATAINIT.length(),whereAttachCondition.indexOf(_CDATAEND));		      
		      
		      
		      try {
			      if (iCheckIntPar==_INTEGRITY_PAR____OK_IDDOC)
			    	  p = new Profilo(idDocument);
			      else if (iCheckIntPar==_INTEGRITY_PAR____OK_ARCM)
			    	  p = new Profilo(cmModel,areaModel);
			      else
			    	  p = new Profilo(cmModel,areaModel,crModel);		      
	
			      p.initVarEnv(user,user,dbOperation.getConn());			     			    			     
			     
		      }
			  catch (Exception e) {
				  if (!bDontCloseConn) closeDbOp();
				  return (new SOAXMLErrorRet("Creazione del profilo: " +e.getMessage())).getXML();		    			                   
			  }				      
		   
			  //Gestione dei file allegati tramite DB
		      if (!connAttach.trim().equals("")) {
		    	  try {
		    		uploadDB(connAttach,tableAttachName,columnAttachName,columnAttach,whereAttachCondition,deleteFileDb,p);
		    	  }
		    	  catch (Exception e) {
		    	    if (!bDontCloseConn) closeDbOp();
		    		try {dbOperationConnAtt.close();}catch (Exception eClose) {}
		    		return (new SOAXMLErrorRet(e.getMessage())).getXML();
		    	  }
		      }		  
		      
		      Vector<String> vFile = new Vector<String>();
			  //Gestione dei file allegati tramite PATH		      
		      if (!fileAttach.trim().equals("")) {
	       	     try {
	       	    	vFile=uploadFs(fileAttach,p);	        	   
	        	 }
        	     catch (Exception e) {        	    	 
   				   try {dbOperation.rollback();}catch (Exception eClose) {}
   				   try {dbOperationConnAtt.close();}catch (Exception eClose) {}
   				   if (!bDontCloseConn) closeDbOp();
   				   return (new SOAXMLErrorRet(e.getMessage())).getXML();				  
   			     }			          
		      }
		      
		      try {
				  if (p.salva().booleanValue()) {					
					  try {
						deleteFile(tableAttachName,whereAttachCondition,vFile,deleteFileFs,deleteFileDb);
					  }
					  catch (Exception eDelete) {
						throw new Exception(eDelete);
					  }
					  
					  dbOperation.commit();					  					  
					  dbOperationConnAtt.commit();
					  try {dbOperationConnAtt.close();}catch (Exception eClose) {}
					  
					  Vector<Vector> vDoc = new Vector<Vector>();
					  Vector<keyval> v = new Vector<keyval>();
					  v.add(new keyval("IDDOCUMENT",p.getDocNumber()));
					  
					  vDoc.add(v);
					  
					  if (!bDontCloseConn) closeDbOp();
					  return (new SOAXMLDataRet("1",vDoc)).getXML();
				  }
				  else {		
					  dbOperation.rollback();
					  try {dbOperationConnAtt.close();}catch (Exception eClose) {}
					  if (!bDontCloseConn) closeDbOp();
					  return (new SOAXMLErrorRet("Salvataggio: " +p.getError())).getXML();
				  }
			  }
			  catch (Exception e) {
				  try {dbOperation.rollback();}catch (Exception eClose) {}
				  try {dbOperationConnAtt.close();}catch (Exception eClose) {}
				  if (!bDontCloseConn) closeDbOp();
				  return (new SOAXMLErrorRet("Salvataggio: " +e.getMessage())).getXML();				  
			  }				      		      		      		   
		      		      
	   }
	   
	   public String download(String connToDownAttach, String tableToDownAttachName,
				              String columnToDownAttachName, String columnToDownAttach,
				              String fileName,String prefix, String prefixColumn) {
		      Profilo p;		      		      
		      Vector<String> vElencoFile = new Vector<String>();
		      
		      int iCheckIntPar=checkIntegrityParameter();
		      		      
		      if (iCheckIntPar<0) {
		    	  if (!bDontCloseConn) closeDbOp();
		    	  return (new SOAXMLErrorRet(generateErrorParameterMessage(iCheckIntPar))).getXML();
		      }
		      
		      if (prefixColumn.trim().equals(""))  {
		    	  if (!bDontCloseConn) closeDbOp();
		      	  return (new SOAXMLErrorRet("E' necessario specificare il parametro prefix")).getXML();
		      }
		      
		      if (iCheckIntPar==_INTEGRITY_PAR____OK_ARCM) {
		    	  if (!bDontCloseConn) closeDbOp();
		    	  return (new SOAXMLErrorRet("E' necessario specificare ar/cm/cr  oppure idDocumento")).getXML();
		      }
		      
		      //Estraggo i file passati		      
		      StringTokenizer fileList =  new StringTokenizer(fileName, _FILESEPARATOR);
	          while (fileList.hasMoreTokens()) {
	        	     String filePath = fileList.nextToken();
	        	     
	        	     vElencoFile.add(filePath);
	          }
	          
	          if (vElencoFile.size()==0) {
	        	  if (!bDontCloseConn) closeDbOp();
	        	  return (new SOAXMLErrorRet("E' necessario specificare un elenco di file nel parametro <fileName>")).getXML();
	          }
	          
		      try {
			      if (iCheckIntPar==_INTEGRITY_PAR____OK_IDDOC)
			    	  p = new Profilo(idDocument);
			      else
			    	  p = new Profilo(cmModel,areaModel,crModel);		      
	
			      p.initVarEnv(user,user,dbOperation.getConn());			     			    			     			     
		      }
			  catch (Exception e) {
				  if (!bDontCloseConn) closeDbOp();
				  return (new SOAXMLErrorRet("Prima di accedere al documento: " +e.getMessage())).getXML();		    			                   
			  }		
			  
			  try {
				  if (p.accedi(Global.ACCESS_ATTACH).booleanValue()) {					  
					  if (!columnToDownAttachName.trim().equals("")) {
						  //Gestisco lo scarico sul db
						  try {
							  downloadDB(connToDownAttach,tableToDownAttachName,
			                             columnToDownAttachName,columnToDownAttach,
			                             vElencoFile,prefix,prefixColumn,
			                             p);  
						  }
						  catch (Exception e) {
							try {dbOperationConnAtt.rollback();}catch (Exception eClose) {}
							try {dbOperationConnAtt.close();}catch (Exception eClose) {}
							if (!bDontCloseConn) closeDbOp();
							return (new SOAXMLErrorRet(e.getMessage())).getXML();		    			                   
						  }	
						  
						  try {dbOperationConnAtt.commit();}catch (Exception eClose) {}
						  try {dbOperationConnAtt.close();}catch (Exception eClose) {}
					  }
					  
					  Vector<Vector> vDoc = new Vector<Vector>();
					  Vector<keyval> v = new Vector<keyval>();
					  v.add(new keyval("FILEDOWNLOAD",""+vElencoFile.size()));
					  
					  vDoc.add(v);
					  if (!bDontCloseConn) closeDbOp();
					  return (new SOAXMLDataRet("1",vDoc)).getXML();					  
				  }
				  else {
					  if (!bDontCloseConn) closeDbOp();
					  return (new SOAXMLErrorRet("Accesso al documento: " +p.getError())).getXML();
				  }
			  }
			  catch (Exception e) {
				  if (!bDontCloseConn) closeDbOp();
				  return (new SOAXMLErrorRet("Accesso al documento: " +e.getMessage())).getXML();		    			                   
			  }				  
		      		     
	   }
	   
	   public void uploadDB(String connAttach, String tableAttachName, 
			                 String columnAttachName, String columnAttach,
			                 String whereAttachCondition, String deleteFileDb,
			                 Profilo p) throws Exception {
		       
		   	   try {
		         SessioneDb.getInstance().addAlias(Global.ALIAS_ORACLE,
						                           Global.DRIVER_ORACLE);
		       
		         dbOperationConnAtt=SessioneDb.getInstance().createIDbOperationSQL(connAttach,0);
	    	   }
	    	   catch (Exception e) {	    		
	    		 throw new Exception("Errore in connessione jndi ("+connAttach+"). Error: "+e.getMessage());
	    	   }
	    	   
	    	   StringBuffer sStm = new StringBuffer("SELECT "+columnAttachName+","+columnAttach);
	    	   sStm.append(" FROM "+tableAttachName);
	    	   if (!whereAttachCondition.trim().equals(""))
	    		   sStm.append(" WHERE "+whereAttachCondition);
	    	   
	    	   try {
	    	     dbOperationConnAtt.setStatement(sStm.toString());
	    	     dbOperationConnAtt.execute();
	    	   }
	    	   catch (Exception e) {	    
	    		 throw new Exception("Errore esecuzione SQL=("+sStm.toString()+") per estrarre i file da allegare. Error: "+e.getMessage());
	    	   } 	    	     
	    	   
	    	   try {
	    	     ResultSet rst = dbOperationConnAtt.getRstSet();
	    	     ResultSetMetaData	rmeta; 
	    	     while (rst.next()) {	    	    	   
	    	    	   rmeta = rst.getMetaData();
	    	    	 
	    	    	   int tipoColonna = rmeta.getColumnType(2);
	    	    	   
	    	    	   try {
		    	    	   if(tipoColonna == Types.CLOB){	    	    		   
		    	    		   p.setFileName(rst.getString(1),dbOperationConnAtt.readClob(2));
							}else if(tipoColonna == Types.BLOB){							
							   p.setFileName(rst.getString(1),dbOperationConnAtt.readBlob(2));
							}else{
								throw new Exception("Attenzione! la colonna ("+columnAttach+") deve essere di tipo CLOB o BLOB");
							}
	    	           }
	    	           catch (java.lang.NullPointerException e)  {     
	    	        	    StringBuffer stm = new StringBuffer("");
	    	            	ByteArrayInputStream bis = new ByteArrayInputStream(stm.toString().getBytes("UTF-8"));

	    	            	p.setFileName(rst.getString(1),bis);        
	    	           }     

	    	     }	    	     
	    	   }
	    	   catch (Exception e) {	    		
	    		 throw new Exception("Errore in scrittura file su Profilo. Error: "+e.getMessage());
	    	   } 	   	    	   	    	  
	   }	   	   
	   
	   public Vector<String> uploadFs(String listaFile, Profilo p) throws Exception {
		      Vector<String> vFile = new Vector<String>();
			  //Gestione dei file allegati tramite PATH		      
	    	  StringTokenizer fileList =  new StringTokenizer(listaFile, _FILESEPARATOR);

	          //Ciclo sui File
	          while (fileList.hasMoreTokens()) {
	        	     String filePath = fileList.nextToken();
	        	     
	        	     try {
	        	       p.setFileName(filePath);
	        	       
	        	       vFile.add(filePath);
	        	     }
	        	     catch (Exception e) {
	        	       throw new Exception("Errore in estrazione file ("+filePath+") da inserire in profilo: " +p.getError());		   				   				  
	   			     }	
	          }	
	          
	          return vFile;
	   }
	   
	   public void deleteFile(String tableAttachName,String whereAttachCondition,
			                   Vector<String> vFile,String deleteFileFs,String deleteFileDb) throws Exception {
		   
		       //Cancello i file DB
			   if (nvl(deleteFileDb,"N").equals("Y")) {
	    		   StringBuffer sStm = new StringBuffer("DELETE ");
		    	   sStm.append(" FROM "+tableAttachName);
		    	   if (!whereAttachCondition.trim().equals(""))
		    		   sStm.append(" WHERE "+whereAttachCondition);
		    	   
		    	   try {
		    	     dbOperationConnAtt.setStatement(sStm.toString());
		    	     dbOperationConnAtt.execute();		    	     		    	     
		    	   }
		    	   catch (Exception e) {
		    		 throw new Exception("Errore esecuzione SQL=("+sStm.toString()+") per eliminare i file da allegare dalla tabella temporanea. Error: "+e.getMessage());
		    	   } 	
	    	   }
			   
			   //Cancello i file FS
			   if (nvl(deleteFileFs,"N").equals("Y")) {
				   for(int i=0;i<vFile.size();i++) {
					   try {
						   File f = new File(vFile.get(i));
						   f.delete();
					   }
					   catch (Exception e) {
						   e.printStackTrace();
					   }
				   }				   
			   }
	   }
	   
	   private void downloadDB(String connToDownAttach, String tableToDownAttachName,
	                           String columnToDownAttachName, String columnToDownAttach,
	                           Vector<String> vFileName,String prefix, String prefixColumn,
	                           Profilo p) throws Exception {
		   	   String typeColumn="";
		   	   boolean bInsert=true;
		   
		   	   try {
		         SessioneDb.getInstance().addAlias(Global.ALIAS_ORACLE,
						                           Global.DRIVER_ORACLE);
		       
		         dbOperationConnAtt=SessioneDb.getInstance().createIDbOperationSQL(connToDownAttach,0);
	    	   }
	    	   catch (Exception e) {
	    		 throw new Exception("Errore in connessione jndi ("+connToDownAttach+"). Error: "+e.getMessage());
	    	   }
	    	   
	    	   StringBuffer stm = new StringBuffer("select data_type ");
	    	   stm.append("from user_tab_columns ");
	    	   stm.append("where upper(table_name)='"+tableToDownAttachName.toUpperCase()+"' and upper(column_name)='"+columnToDownAttach.toUpperCase()+"' ");
	    	   
	    	   try {
		    	 dbOperationConnAtt.setStatement(stm.toString());
		    	 dbOperationConnAtt.execute();		    	     		    	     
	    	   }
	    	   catch (Exception e) {
	    		 throw new Exception("Errore esecuzione SQL=("+stm.toString()+") per ricavare informazioni sulla tabella e la colonna dove scaricare i file. Error: "+e.getMessage());
	    	   } 		    	   
	    	   
	    	   ResultSet rst = dbOperationConnAtt.getRstSet();
	    	   
	    	   if (rst.next()) {
	    		   typeColumn=rst.getString(1);
	    		   
	    		   if (!typeColumn.equals(_BLOB_COLUMN) && !typeColumn.equals(_CLOB_COLUMN)) 
	    			   throw new Exception("La colonna "+columnToDownAttach+" dove essere di tipo CLOB o BLOB.");
	    	   }	    		   
	    	   else
	    		   throw new Exception("Non esiste la tabella "+tableToDownAttachName+" o la colonna "+columnToDownAttach+" dove scaricare i file.");
	    	   
	    	   	    	   
	    	   //Ciclo sui file da allegare...
	    	   for(int i=0;i<vFileName.size();i++) {
	    		   InputStream is;
	    		   try {
	    			   is=p.getFileStream(vFileName.get(i));
	    		   }
	    	       catch (Exception e) {
	    		       throw new Exception("Errore in estrazione file ("+vFileName.get(i)+") dal profilo. Error: "+e.getMessage());
	    		   } 	
	    	       
	    	       //Controllo se esiste la chiave
	    	       StringBuffer sStmSelect = new StringBuffer("SELECT 'x' ");
	    	       sStmSelect.append("FROM "+tableToDownAttachName);
	    	       sStmSelect.append(" WHERE ");
	    	       sStmSelect.append(columnToDownAttachName+" = :P_FILENAME");
	    	       sStmSelect.append(" AND ");
	    	       sStmSelect.append(prefixColumn+" = :P_SPREFIX");
	    	       
	    	       dbOperationConnAtt.setStatement(sStmSelect.toString());   
		    	   
		    	   dbOperationConnAtt.setParameter(":P_FILENAME",vFileName.get(i));		    	   		    	   
		    	   dbOperationConnAtt.setParameter(":P_SPREFIX",prefix);	
		    	   
		    	   try {		    	
				     dbOperationConnAtt.execute();
				     rst = dbOperationConnAtt.getRstSet();		
				     
				     if (rst.next()) bInsert=false;
		    	   }
		    	   catch (Exception e) {	    		
		    		 throw new Exception("Errore in selezione informazioni file "+vFileName.get(i)+" in tabella - SQL=("+sStmSelect.toString()+"). Error: "+e.getMessage());
		    	   }		    	   
	    	       
	    	       if (bInsert) {
			    	   StringBuffer sStmInsert = new StringBuffer("Insert Into "+tableToDownAttachName);
			    	   sStmInsert.append(" (");
			    	   sStmInsert.append(columnToDownAttachName);
			    	   sStmInsert.append(",");
			    	   sStmInsert.append(prefixColumn);			       		    	  
			    	   sStmInsert.append(")");
			    	   
			    	   sStmInsert.append(" values ");
			    	   
			    	   sStmInsert.append("(");
			    	   
			    	   sStmInsert.append(":P_FILENAME");
			    	   sStmInsert.append(",");		    	   
			    	   sStmInsert.append(":P_SPREFIX");			       		    	  
			    	   sStmInsert.append(")");
			    	   
			    	   dbOperationConnAtt.setStatement(sStmInsert.toString());   
			    	   
			    	   dbOperationConnAtt.setParameter(":P_FILENAME",vFileName.get(i));		    	   		    	   
			    	   dbOperationConnAtt.setParameter(":P_SPREFIX",prefix);
			    	   
			    	   try {		    	
				    	 dbOperationConnAtt.execute();		    	     		    	     
			    	   }
			    	   catch (Exception e) {	    		
			    		 throw new Exception("Errore in inserimento informazioni file "+vFileName.get(i)+" in tabella - SQL=("+sStmInsert.toString()+"). Error: "+e.getMessage());
			    	   }			    	   
	    	       }   		    	   
		    	   
		    	   StringBuffer sStmUpdate = new StringBuffer("UPDATE "+tableToDownAttachName);
		    	   sStmUpdate.append(" SET "+columnToDownAttach+" = :P_IS ");		    	   
		    	   sStmUpdate.append(" WHERE ");
		    	   sStmUpdate.append(columnToDownAttachName+" = :P_FILENAME");
		    	   sStmUpdate.append(" AND ");
		    	   sStmUpdate.append(prefixColumn+" = :P_SPREFIX");
		    	   
		    	   dbOperationConnAtt.setStatement(sStmUpdate.toString());
		    	   
				   if (typeColumn.equals(_BLOB_COLUMN))
		    		  dbOperationConnAtt.setParameter(":P_IS",is,is.available());
		           else
		    		  dbOperationConnAtt.setAsciiStream(":P_IS",is,is.available());
		    	   dbOperationConnAtt.setParameter(":P_FILENAME",vFileName.get(i));		    	   		    	   
		    	   dbOperationConnAtt.setParameter(":P_SPREFIX",prefix);
		    	   
		    	   try {		    	
				     dbOperationConnAtt.execute();		    	     		    	     
		    	   }
		    	   catch (Exception e) {	    		
		    		 throw new Exception("Errore in inserimento file "+vFileName.get(i)+" in tabella - SQL=("+sStmUpdate.toString()+"). Error: "+e.getMessage());
		    	   }		    	   
	    	   }
	    	   
	    	   
	    	  	    	   
	   }
	   
	   
}
