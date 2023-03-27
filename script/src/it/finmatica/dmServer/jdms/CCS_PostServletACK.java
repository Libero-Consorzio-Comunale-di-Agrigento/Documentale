package it.finmatica.dmServer.jdms;

import it.finmatica.dmServer.management.*;
import it.finmatica.dmServer.*;
import it.finmatica.jfc.dbUtil.*;

/**
 * Gestione degli ACK.
 * Classe di servizio per la gestione del Client
*/

public class CCS_PostServletACK 
{
	   /**
	    * Variabili private
	   */
	   private String idDoc;
	   private String htmlBottone;
	   CCS_Common CCS_common;
	   private Environment vu;  
	   private IDbOperationSQL dbOp;
	   /**
		  * Variabile gestione logging
		*/
	   private DMServer4j log;
	   
	   /**
		 * Costruttore per la gestione degli ACK.
		 */
	   public CCS_PostServletACK(String newidDoc,String newhtmlBottone,CCS_Common newCommon) throws Exception
	   {
		   	  idDoc=newidDoc;
		   	  htmlBottone=newhtmlBottone;
		   	  CCS_common=newCommon;
		   	  log= new DMServer4j(CCS_PostServletACK.class,CCS_common); 
		   	  dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
		   	  vu = new Environment(CCS_common.user, CCS_common.user,"MODULISTICA","ADS", null,dbOp.getConn(),false);
	   }
	   
	   /**
		 * Visualizza ACK relativi al documento.
		 */
	   public boolean _AfterInitialize() throws Exception
	   {
	          try 
	          { 
				AggiornaDocumento ad = new AggiornaDocumento(idDoc,vu);
				ad.aggiornaDati("$ACTIONKEY",htmlBottone);			   
	 			if (!ad.salvaDocumentoBozza())    {
	        	  CCS_common.closeConnection(dbOp,false);
				  return false;				
	 			}
	 			CCS_common.closeConnection(dbOp,true);
			  }
	          catch (Exception e) {
	             CCS_common.closeConnection(dbOp,false);
	             log.log_error("CCS_PostServletACK::_AfterInitialize() - idDocumento:"+idDoc);
	             throw e;
	             //throw new Exception("CCS_PostServletACK::_AfterInitialize\n"+e.getMessage());
	          }	
	          return true;
	   }
}