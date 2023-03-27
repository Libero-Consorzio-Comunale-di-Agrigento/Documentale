package it.finmatica.dmServer.jdms;

import it.finmatica.dmServer.management.*;
import it.finmatica.jfc.dbUtil.*;
import it.finmatica.dmServer.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.io.*;

import javax.servlet.http.HttpServletRequest;

/**
 * Gestione del caricamento degli allegati di un Documento.
 * Classe di servizio per la gestione del Client
*/
     
public class CCS_DownloadAll 
{
	   /**
	    * Variabili private
	   */	 
	   private String idDoc="";
	   private String idOgfi="";
	   private String idLog="";
	   CCS_Common CCS_common;
	   private Environment vu;  
	   private IDbOperationSQL dbOp;
	   private String nomeFile="";
	   private String idOggettoFile="";
	   private InputStream isStream = null;
	   HttpServletRequest req;
	   
	   /**
		  * Variabile gestione logging
		*/
	   private DMServer4j log;
	   
	   /**
		 * Costruttore utilizzato per la gestione
		 * degli allegati di un Documento.
		 * 
		 */
	   public CCS_DownloadAll(String newidDoc,String newidOgfi,CCS_Common newCommon) throws Exception
	   {
		      idDoc=newidDoc;
		      idOgfi=newidOgfi;
		      CCS_common=newCommon;
		      log= new DMServer4j(CCS_DownloadAll.class,CCS_common);
		      dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
		      vu = new Environment(CCS_common.user, CCS_common.user, "MODULISTICA","ADS", null,dbOp.getConn(),false);
	   }
	   
	   /**
		 * Costruttore utilizzato per la gestione
		 * degli allegati di un Documento.
		 * 
		 */
	   public CCS_DownloadAll(HttpServletRequest newreq,String newidDoc,CCS_Common newCommon) throws Exception
	   {
		      req=newreq;
		      idDoc=newidDoc;
		      CCS_common=newCommon;
		      log= new DMServer4j(CCS_DownloadAll.class,CCS_common);
		      if (!CCS_common.dataSource.equals("")) {
	            dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
	            vu = new Environment(CCS_common.user, null,null,null, null,dbOp.getConn(),false);
	           }
	           else 
	           {
	            vu=CCS_common.ev;
	            dbOp=CCS_common.ev.getDbOp();
	           }     
	   }
	   
	   /**
		 * Caricamento degli allegati di un Documento.
		 * 
		 */
	   public void _BeforeShow() throws Exception
	   {
		      AccediDocumento ad;   
		      if (idOgfi==null)
	          {
	           try 
	           {
	             ad = new AccediDocumento(idDoc,vu);
	             ad.accediDocumentoAllegati();
	             Vector v = ad.listaIdOggettiFile();
	             for(int i=0;i<v.size();i++) 
	             { 
	               if (ad.isOggettoFileVisibile((String)v.get(i))) 
	               {
	                 isStream = ad.leggiOggettoFile((String)v.get(i));
	                 nomeFile = ad.nomeOggettoFile((String)v.get(i));
	          	     idOggettoFile = (String)v.get(i);
	                 i=v.size();
	               }	
	             } 
	    
	           }
	           catch (Exception e) {
	             log.log_error("CCS_DownloadAll::_BeforeShow() - idDocumento:"+idDoc);
	             throw e;
	           }
			   finally {
				   _finally();
			   }
	          }
	          else 
	          {
	           try 
	           { 
	             /** Controllo competenze di lettura sul documento prima di accedere all'oggetto file */
	        	 this.getIDDOCFromOggettiFile();
	        	
	        	 if(idDoc!=null && !idDoc.equals(""))
	        	 {
	        		 ad = new AccediDocumento(idDoc,vu); 
	        		 if (idLog!=null && !idLog.equals(""))
	        			 ad.accediLogDocumento(idLog);
	        		 else
	        			 ad.accediDocumentoAllegati();
	        		 
	        		 Vector<GD4_Oggetti_File> listaFile = ad.listaOggettiFile();
	        		 
	        		 for (int i=0;i<listaFile.size();i++ ){
	        			 GD4_Oggetti_File ogfi = listaFile.get(i);
	        			 
	        			 if (!ogfi.getIdOggettoFile().equals(idOgfi)) continue;
	        			 
			             nomeFile = ogfi.getFileName();
			             isStream = (java.io.InputStream)ogfi.getFile();
			             break;
	        		 }	
	        	 }	
	        	 else
	        	  throw new Exception("Accesso al documento - Controllare l'esistenza del Documento");
	             
	            }
	            catch (Exception e) {
	               log.log_error("CCS_DownloadAll::_BeforeShow() - idOggettoFile:"+idOgfi);
		           throw e;
	            }
	            finally {
                  _finally();
			    }
	          }
	   }
	   
	   
	   /**
		 * Recupero idDocumento a partire da un oggettoFile.
		 * 
		 */
	   private void getIDDOCFromOggettiFile() throws Exception
	   {
	           String sql;
	           try
	           {			    
			     sql="select id_documento from oggetti_file where id_oggetto_file = :IDOGGETTOFILE";
			     dbOp.setStatement(sql);
			     dbOp.setParameter(":IDOGGETTOFILE",idOgfi);
			     dbOp.execute();
			     ResultSet rs = dbOp.getRstSet();
			     if(rs.next()) 
			       idDoc = rs.getString(1);
			    }
	            catch ( SQLException e ) {
	              log.log_error("CCS_DownloadAll::getIDDOCFromOggettiFile() -  Recupero idDocumento a partire da OggettoFile - idOggettoFile:"+idOgfi);
	              throw e;         
              }  
	  }
	   
	   
	   /**
	     * Gestione della lista di allegati per un Documento.
	     * 
	     * @return String 		listbox elenco allegati
	     * 
		 */
	   public String getListBoxAllegati() throws Exception
	   {
	           String sql,list="",url="";

	           try
	           {	
	        	 if(idDoc!=null && !idDoc.equals(""))
		         {
		        	 /** Controllo competenze di lettura sul Documento */
		        	 //AccediDocumento  ad = new AccediDocumento(idDoc,vu);
				     sql="  SELECT ogfi.FILENAME, ogfi.ID_OGGETTO_FILE ";
				     sql+=" FROM OGGETTI_FILE ogfi, FORMATI_FILE fofi ";
				     sql+=" WHERE ogfi.ID_FORMATO = fofi.ID_FORMATO ";
				     sql+=" AND fofi.VISIBILE='S' ";
				     sql+=" AND ogfi.id_documento = :IDDOCUMENTO";
				     
				     list="<select id=\"lista\" class=\"AFCSelect\" name=\"lista\" style=\"WIDTH: 400px\" >";
				     list+="<option value=\"\" selected>- -</option>";
				      
				     dbOp.setStatement(sql);
				     dbOp.setParameter(":IDDOCUMENTO",idDoc);
				     dbOp.execute();
				     ResultSet rs = dbOp.getRstSet();
				     while ( rs.next() ) 
				     {
				    	String nomeFile = rs.getString(1);
			            String idOgFile = rs.getString(2);
		                url=req.getScheme()+"://"+req.getServerName()+":"+req.getServerPort()+"/jdms/common/AllegatoVisualizza.do?idOgfi="+idOgFile+"&amp;idDoc="+idDoc;
		         	    list+="<option value=\""+url+"\" >"+nomeFile+"</option>";
				     }
				     list+="</select>";
			      }	
	        	  else
	        	    throw new Exception("Accesso al documento - Controllare l'esistenza del Documento");
  			   }
	           catch ( SQLException e ) {
	              log.log_error("CCS_DownloadAll::getListBoxAllegati() - idDoc:"+idDoc);
	              throw e;         
               }
			   finally {
				   _finally();
			   }
	          return list;
	  }
	   
	   /**
		 * Recupera il nome del file.
		 * 
		 */
	   public String _getNomeFile() throws Exception
	   {
		   	  return nomeFile;
	   }
	   
	   /**
		 * Recupera id del file.
		 * 
		 */
	   public String _getIdOggettoFile() throws Exception
	   {
		   	  return idOggettoFile;
	   }
	   
	   /**
		 * Recupera lo stream del file.
		 * 
		 */
	   public InputStream _getisStream() throws Exception
	   {
		   	  return isStream;
	   }
	   
	   public void setIdLog(String idLog) {
			  this.idLog = idLog;
	   }	 	   
	   
	   public void connect() throws Exception {
		      try {
		        vu.connect();
		      } 
		      catch (Exception e) {
		        log.log_error("CCS_DownloadAll::connect()");
		        throw e;
		      }
	   }

		//Chiusura della connessione
		private void _finally() throws Exception {
			try
			{
				if (CCS_common.dataSource.equals("")){
					try{vu.disconnectClose();}catch(Exception ei){}
				}
				CCS_common.closeConnection(dbOp);
			}
			catch (Exception e) {
				throw e;
			}
		}
  
}