package it.finmatica.dmServer.jdms;

import it.finmatica.dmServer.GD4_Status_Documento;
import it.finmatica.dmServer.jdms.DMServer4j;
import it.finmatica.dmServer.util.DocUtil;
import it.finmatica.dmServer.controlli.*;
import it.finmatica.dmServer.Environment;
import it.finmatica.jfc.dbUtil.*;
import java.net.URLEncoder;
import java.sql.*;

/**
 * Gestione delle ACK.
 * Classe di servizio per la gestione del Client
*/

 
public class CCS_ACKPage 
{
   private String ack="";
   CCS_Common CCS_common;
   private Environment vu;  
   private IDbOperationSQL dbOp;
   
   /**
	  * Variabile gestione logging
	*/
   private DMServer4j log;

   public CCS_ACKPage(String newack,CCS_Common newCommon) throws Exception
   {
	   	  ack=newack;
	   	  CCS_common=newCommon;
	   	  dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
	   	  vu = new Environment(CCS_common.user, CCS_common.user, "MODULISTICA","ADS", null,dbOp.getConn(),false);
	      log= new DMServer4j(CCS_ACKPage.class,CCS_common); 
   }
   
   /**
    * 
    * 
    * @param element  elemento della lista
   */	   
   public String _afterInitialize(String element) throws Exception
   {
          String ceck=null;
         
          try
          {
			ACKControlli controlli = new ACKControlli(ack,"GDMSYS",element,vu);
			ceck= controlli.execControlli();
          }
          catch (Exception e) 
          {
            throw e;
          }
          finally {
              _finally();
          }
          return ceck;
   }
  
   /**
     * Costruzione dell'url della popup
     * 
     * @param  urlBase  		nome della pagina da effettuare il redirect
     * @param  idCartProv	idCartella
     * @param  idDoc			identificativo del Documento
     * @return String		url  
   */	
   public String _afterInitializePopup(String urlBase,String idCartProv,String idDoc) throws Exception
   {
          StringBuffer urlFinal=new StringBuffer(urlBase);
         
          try 
          {
			if (urlBase.indexOf("DocumentoView.do")>0)
			{              
               DocUtil du = new DocUtil(vu);
               String idTipoDoc=du.getIdTipoDocByIdDocumento(idDoc);
               vu.connect();
               GD4_Status_Documento gd4s = new GD4_Status_Documento();
               gd4s.inizializzaDati(vu,idDoc);
               gd4s.loadStato();
               String stato=gd4s.getStato();
               vu.disconnectClose();
               urlFinal.append("&rw=W&idCartProveninez="+idCartProv.substring(1,idCartProv.length()));
			   urlFinal.append("&idQueryProveninez=-1&Provenienza=C");
               urlFinal.append("&stato="+stato+"&idTipoDoc="+idTipoDoc);               
               urlFinal.append("&MVPG=ServletModulisticaDocumento");
               urlFinal.append("&GDC_Link="+URLEncoder.encode("../common/ClosePageAndRefresh.do?idQueryProveninez=-1&MVPG=ServletModulisticaDocumento"));
            }
          }
          catch (Exception e) 
          {
        	throw e;
          }
          finally {
              _finally();
          }
          return urlFinal.toString();
   }
   
   /**
     * Genera il codice richiesta
     * 
     * @return String	cr 
   */
   public String _generaCR() throws Exception
   {	        		    
	   	  String cr=null,sql;
			
	   	  try
	   	  {
             sql="select 'GDCLIENT'||to_char(CODR_SQ.nextval) from dual";
             dbOp.setStatement(sql);
             dbOp.execute();
             ResultSet rs=dbOp.getRstSet();
             if (rs.next()) 
               cr= rs.getString(1);
          }
          catch ( SQLException e ) {
        	  throw e;
          }
          finally {
              _finally();
          }
       	 return cr;
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