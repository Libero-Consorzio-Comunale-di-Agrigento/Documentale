package it.finmatica.dmServer.jdms;

import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.management.*;
import it.finmatica.jfc.dbUtil.*;
import java.util.StringTokenizer;

/**
 * Gestione delle operazione di Copia/Sposta degli oggetti.
 * Classe di servizio per la gestione del Client
*/

public class CCS_TreeSelezionaCopiaSposta 
{
   /**
	* Variabili  
   */	
   private String cartDest;
   private String tipoOperazione;
   private String cartSorg;
   private String sListaID;
   CCS_Common CCS_common;
   CCS_HTML h;
   private IDbOperationSQL dbOp;
   private Environment vu;  
   private String user;
   /**
	 * Variabile gestione logging
	*/
   private DMServer4j log;
 
   
   /**
    * Constructor
    * Effettua l'operazione di Copia/Sposta degli oggetti 
    * selezionati in un'altra cartella 
    * 
   */	   
   public CCS_TreeSelezionaCopiaSposta(String oggettoDest,String tipoOp,String oggettoSorg,String newsListaID,CCS_Common newCommon) throws Exception
   {
	     cartDest=oggettoDest; 
	     tipoOperazione=tipoOp;
         cartSorg=oggettoSorg;
         if(cartSorg!=null && cartSorg.indexOf("C")!=-1)
          cartSorg=cartSorg.substring(1,cartSorg.length()); 
         sListaID=newsListaID;
         CCS_common=newCommon;
         user=CCS_common.user;
         dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
         vu = new Environment(CCS_common.user, CCS_common.user,"MODULISTICA","ADS",null,dbOp.getConn());
         log= new DMServer4j(CCS_TreeSelezionaCopiaSposta.class,CCS_common); 
   }
   
   public CCS_TreeSelezionaCopiaSposta(String oggettoDest,String tipoOp,String oggettoSorg,String newsListaID,IDbOperationSQL newdbOp,Environment env) throws Exception
   {
	     cartDest=oggettoDest; 
	     tipoOperazione=tipoOp;
         cartSorg=oggettoSorg;
         if(cartSorg!=null && cartSorg.indexOf("C")!=-1)
          cartSorg=cartSorg.substring(1,cartSorg.length()); 
         sListaID=newsListaID;
         dbOp=newdbOp;
         vu = env;
         user=vu.getUser();
         CCS_common= new CCS_Common(vu,user); 
         log= new DMServer4j(CCS_TreeSelezionaCopiaSposta.class,CCS_common); 
   }
   
   /**
    * Costruzione della sequenza per effettuare l'operazione di 
    * Spostamento 
    * 
    * @return Stringa la sequenza
   */	   
   private String setListaID()
   { 
	       String seq="";
	       
	       if(sListaID!=null)
	       {
	         StringTokenizer st = new StringTokenizer(sListaID,"@");
	         while (st.hasMoreTokens())
	         {
	           String s=st.nextToken();
	           seq+=s+","+cartSorg+"@";
	         }
	       }
	       return seq;
   } 

   /**
    * Invoca la ICartella per effettura l'operazione di Copia/Sposta
    * della lista di oggetti 
    * 
    * @return Stringa messaggio di errore o avvenuta esecuzione 
    * dell'operazione
   */	   
   public String  _onclick() throws Exception
   {
          String sMessaggio="";
          boolean commit = false;

          try {
              //Operazione di spostamento
              if (tipoOperazione.equals("S")) {
                  try {
                      ICartella Ic = new ICartella(cartDest);
                      Ic.initVarEnv(vu);

                      Ic.spostaOggetti(setListaID());
                      sMessaggio = "OK";

                      commit = true;
                  } catch (Exception e) {
                      commit=false;
                      sMessaggio = e.getMessage();
                      log.log_error("CCS_TreeSelezionaCopiaSposta::_onClick() - Operazione di Sposta - Cartella di Destinazione:" + cartDest + " - lista Oggetti:" + setListaID());
                      throw e;
                  }
              } else // Operazione di copia
              {

                  try {
                      ICartella Ic = new ICartella(cartDest);
                      Ic.initVarEnv(vu);

                      Ic.copiaOggetti(sListaID);
                      sMessaggio = "OK";
                      commit = true;

                  } catch (Exception e) {
                      commit=false;
                      sMessaggio = e.getMessage();
                      log.log_error("CCS_TreeSelezionaCopiaSposta::_onClick() - Operazione di Copia - Cartella di Destinazione:" + cartDest + " - lista Oggetti:" + sListaID);
                      throw e;
                  }
              }
          }
          catch (Exception e){
              commit=false;
              throw e;
          }
          finally {
              _finally(commit);
          }
          return sMessaggio;
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

    private void _finally(boolean commit) throws Exception {
        try
        {
            if (CCS_common.dataSource.equals("")){
                try{vu.disconnectClose();}catch(Exception ei){}
            }
            CCS_common.closeConnection(dbOp,commit);
        }
        catch (Exception e) {
            throw e;
        }
    }
 
}    