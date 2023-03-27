package it.finmatica.dmServer.jdms;

import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import javax.servlet.http.HttpServletRequest;
import it.finmatica.dmServer.FirmaUnimatica.*;
import it.finmatica.jfc.dbUtil.SessioneDb;
import java.util.StringTokenizer;
import java.util.LinkedList;

/**
 * Gestione Firma Standard (Unimatica) dei documenti.
 * Classe di servizio per la gestione del Client
*/

public class CCS_Gestionefirma {

	private String seqDoc="";
	CCS_Common CCS_common;
	CCS_HTML h;
	HttpServletRequest request;
	private IDbOperationSQL dbOp;
	private DMServer4j log;
		
	public CCS_Gestionefirma(String seq,HttpServletRequest req,CCS_Common newCommon) throws Exception
	{
		   if(seq!=null)   
		    seqDoc=seq;
		   request=req;
		   CCS_common=newCommon;
	       log= new DMServer4j(CCS_EliminaOggetti.class,CCS_common); 
	       dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
    }	
	
    public String firma() throws Exception {
    	   String redirect="";   
    	   String utente,serverurl,contextpath;
    	
    	   try
		   {
    		StringTokenizer st = new StringTokenizer(seqDoc, "@");
    		LinkedList<String> li = new LinkedList<String>(); 
			while (st.hasMoreTokens())
			{
			 String id=st.nextElement().toString();
			 if(id.indexOf("D")!=-1) 
			  id=id.substring(1,id.length());
			 li.add(id);
   	        }
    		
			utente= (String)request.getSession().getAttribute("Utente");
    		
			serverurl =request.getScheme();
			serverurl += "://"+request.getServerName();
			serverurl+= ":"+request.getServerPort();
			contextpath = request.getContextPath();
			
			FirmaUnimatica fu = new FirmaUnimatica(dbOp.getConn(), li,utente, "", serverurl,contextpath);
			redirect = fu.creaURLFirma();
			
			if(redirect == null || (redirect!=null && redirect.equals("")) )
			 throw new Exception("Attenzione! Gestione Firma Standard: Impossibile aprire la pagina per la gestine della firma!");
			
			CCS_common.closeConnection(dbOp,true);
		   }
		   catch (Exception e) 
		   {
			 log.log_error("CCS_Gestionefirma::frima -- Gestione Firma Standard: "+seqDoc+" "+e.getMessage());
			 CCS_common.closeConnection(dbOp,false); 
		     throw e;
		   }
		   return redirect;
     }
    
    
}
