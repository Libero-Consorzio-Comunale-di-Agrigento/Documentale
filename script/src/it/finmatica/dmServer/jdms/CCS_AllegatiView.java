package it.finmatica.dmServer.jdms;

import it.finmatica.jfc.dbUtil.*;
import java.sql.*;

/**
 * Gestione degli allegati di un Documento.
 * Classe di servizio per la gestione del Client
*/

public class CCS_AllegatiView 
{
   /**
     * Variabile di connessione
   */
   CCS_Common CCS_common;
   
   /**
     * identificativo Documento
   */
   private String idDoc;
   
   /**
     * Variabile di generazione HTML
   */
   CCS_HTML h;     
   
   /**
     * Variabile di IDbOperationSQL
   */ 
   private IDbOperationSQL dbOp;
   
   /**
	 * Variabile gestione logging
	*/
   private DMServer4j log;
   
   /**
	 * Costruttore utilizzato per determinare gli
	 * allegati ad un Documento della WorkArea.
	 * 
	 */
   public CCS_AllegatiView(String newidDoc,CCS_Common newCommon) throws Exception
   {
	   	  idDoc=newidDoc;
	   	  CCS_common=newCommon;
	   	  h = new CCS_HTML();
	   	  dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
	   	  log= new DMServer4j(CCS_AllegatiView.class,CCS_common); 
   }
   
   /**
    *  Visualizza la lista di allegati relativi al documento.
    * 
    * @return String   elenco
   */
   public String _BeforeShow() throws Exception
   {
          String sql,TableAll="",row="";
          try
          {			    
			 sql="select allegato from allegati_html where id_documento = :IDDOCUMENTO";
			 dbOp.setStatement(sql);
			 dbOp.setParameter(":IDDOCUMENTO",idDoc);
			 dbOp.execute();
			 ResultSet rs = dbOp.getRstSet();
			 while ( rs.next() ) {
		       row+= rs.getString(1);
			 }
			 TableAll=h.getTable("2","#FFFFFF","#FFFFFF","border: 1 solid #C0C0C0","1",h.getTR(row));
          }
          catch (Exception e) {
        	 throw e;
          }
          finally {
              _finally();
          }
          return TableAll;
   	}

    //Chiusura della connessione
    private void _finally() throws Exception {
        try
        {
            CCS_common.closeConnection(dbOp);
        }
        catch (Exception e) {
            throw e;
        }
    }
}