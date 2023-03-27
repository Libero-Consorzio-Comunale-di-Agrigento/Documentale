package it.finmatica.dmServer.jdmsfx;

import it.finmatica.dmServer.jdms.CCS_TreeSelezionaCopiaSposta;
import it.finmatica.dmServer.SOA.SOAXMLErrorRet;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.dmServer.jdms.CCS_Common;
import javax.servlet.http.HttpServletRequest;
import it.finmatica.dmServer.jdms.DMServer4j;
import it.finmatica.dmServer.Environment;

public class treeCopiaSposta {

   private String cartDest;
   private String tipoOperazione;
   private String cartSorg;
   private String sListaID;
   CCS_Common CCS_common;
   private IDbOperationSQL dbOp;
   private Environment vu;  
   private String user;
   private HttpServletRequest request;
   
   /**
	 * Variabile gestione logging
	*/
   private DMServer4j log;
   
	public treeCopiaSposta(String oggettoDest,String tipoOp,String oggettoSorg,String newsListaID,HttpServletRequest req,IDbOperationSQL newdbOp) throws Exception
	{
			cartDest=oggettoDest; 
			tipoOperazione=tipoOp;
	        cartSorg=oggettoSorg;
	        if(cartSorg.indexOf("C")!=-1)
	          cartSorg.substring(1,cartSorg.length()); 
	        sListaID=newsListaID;
	        dbOp=newdbOp;
	        request=req;
			user=""+req.getSession().getAttribute("Utente");
	        vu = new Environment(user, user,"MODULISTICA","ADS",null,dbOp.getConn());
	        CCS_common= new CCS_Common(vu,user); 
	        log= new DMServer4j(treeCopiaSposta.class,CCS_common); 
    }
	
	public String onclickCopiaSposta() throws Exception {
		   String result="";
		   try {
			   CCS_TreeSelezionaCopiaSposta t=new CCS_TreeSelezionaCopiaSposta(cartDest,tipoOperazione,cartSorg,sListaID,dbOp,vu);
			   result=t._onclick();			   
			   return result;
		   }
		   catch (Exception e) 
		   {
		   	log.log_error("onclickCopiaSposta - Problemi durante l'operazione di Copia/Sposta:"+e.getMessage());
			return (new SOAXMLErrorRet("Problemi durante l'operazione di Copia/Sposta \n"+e.getMessage())).getXML();
		   }
	}
}
