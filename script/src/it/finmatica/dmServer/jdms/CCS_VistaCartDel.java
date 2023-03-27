package it.finmatica.dmServer.jdms;

import it.finmatica.dmServer.management.*;
import it.finmatica.dmServer.Environment;
import it.finmatica.jfc.dbUtil.*;

/**
 * Gestione dell'eliminazione di oggetti (Cartella o Query).
 * Classe di servizio per la gestione del Client
*/
 
public class CCS_VistaCartDel 
{
	   /**
	    * Variabili private
	   */
	   CCS_Common CCS_common;
	   private String idOggetto;
	   private String tipoOggetto;
	   private Environment vu;  
	   private IDbOperationSQL dbOp;
	   /**
		  * Variabile gestione logging
		*/
	   private DMServer4j log;
	   
	   /**
		 * Costruttore utilizzato per eliminare 
		 * gli oggetti di tipo Cartella o Query.
		 * 
		 */
	   public CCS_VistaCartDel(String newidOggetto,String newtipoOggetto,CCS_Common newCommon)throws Exception
	   {
		   	  idOggetto=newidOggetto;
		   	  tipoOggetto=newtipoOggetto;
		   	  CCS_common=newCommon;
		   	  dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
		   	  vu = new Environment(CCS_common.user, CCS_common.user, "MODULISTICA","ADS", null,dbOp.getConn(),false);
		      log= new DMServer4j(CCS_VistaCartDel.class,CCS_common); 
	   }
	   
	   /**
		 * Esegue l'operazione di eliminazione
		 * distinguendo in Cartella e Query.
		 * 
		 */
	   public void _afterInitialize() throws Exception
	   {
		      if (tipoOggetto.equals("C"))
		    	deleteCartella();
		      else
		    	deleteQuery(); 
	   } 
	   
	   /**
		 * Esegue l'operazione di eliminazione
		 * di una Cartella.
		 * 
		 */
	   private void deleteCartella() throws Exception
	   {
			   try
			   {
			      ICartella Ic = new ICartella(idOggetto);
		          Ic.initVarEnv(vu);
		          Ic.delete();
		          CCS_common.closeConnection(dbOp,true);              
			   }
			   catch (Exception e) 
			   {
	            CCS_common.closeConnection(dbOp,false);   
	            log.log_error("CCS_VistaCartDel::_afterInitialize - deleteCartella() - idCartella:"+idOggetto);
	            throw e;
	            //throw new Exception("CCS_VistaCartDel::_afterInitialize - deleteCartella\n"+e.getMessage());
			   }
	   }
	   
	   /**
		 * Esegue l'operazione di eliminazione
		 * di una Query.
		 * 
		 */
	   private void deleteQuery() throws Exception
	   {
		       try 
		       {
		    	   IQuery q=new IQuery(idOggetto);
		    	   q.initVarEnv(vu);
		    	   q.delete();
		    	   CCS_common.closeConnection(dbOp,true);   
		       }
 		       catch (Exception e) 
 		       {
 		    	 CCS_common.closeConnection(dbOp,false);           
 		    	 log.log_error("CCS_VistaCartDel::_afterInitialize - deleteQuery() - idQuery:"+idOggetto);
 	             throw e;
 	             //throw new Exception("CCS_VistaCartDel::_afterInitialize - deleteQuery\n"+e.getMessage());
 		       }
	   }
}