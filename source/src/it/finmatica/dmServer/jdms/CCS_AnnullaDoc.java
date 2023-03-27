package it.finmatica.dmServer.jdms;

import it.finmatica.dmServer.GD4_Status_Documento;
import it.finmatica.dmServer.Environment;
import it.finmatica.jfc.dbUtil.*;

/**
 * Gestione dei Documenti.
 * Classe di servizio per la gestione del Client
*/

public class CCS_AnnullaDoc
{
	/**
	  * identificativo Documento
	 */
	
	String idDocumento;
	/**
	  * Variabile di connessione
	 */
	CCS_Common CCS_common;
	private IDbOperationSQL dbOp;
	private Environment vu;

	/**
	  * Variabile gestione logging
	*/
    private DMServer4j log;
	
	/**
	 * Costruttore utilizzato per settare lo stato di 
	 * un Documento.
	 * 
	 */
	public CCS_AnnullaDoc(String newidDocumento,CCS_Common newCommon) throws Exception
	{
		   idDocumento=newidDocumento;
		   CCS_common=newCommon;
		   log= new DMServer4j(CCS_AnnullaDoc.class,CCS_common);
		   dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
		   vu = new Environment(CCS_common.user,null,null,null,null,dbOp.getConn());
	}
	
	/**
	  *  Annulla il Documento settando lo stao a "CA".
	  * 
	*/
	public void _afterInitialize_setStatoToCA() throws Exception 
	{
		   GD4_Status_Documento gd4StDoc = new GD4_Status_Documento();
           vu.connect();
           gd4StDoc.inizializzaDati(vu,idDocumento);
		   gd4StDoc.setStato("CA");
		   try 
		   {   
			 gd4StDoc.registraStato();
             try{vu.disconnectClose();}catch (Exception ei){} 
             CCS_common.closeConnection(dbOp,true);       
		   }
		   catch (Exception e) 
		   {
			 try{vu.disconnectClose();}catch (Exception ei){} 
			 CCS_common.closeConnection(dbOp,false);
			 throw e;
		   }
	}

}