package it.finmatica.dmServer.jdms;

import javax.servlet.http.HttpServletRequest;

import it.finmatica.dmServer.Environment;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.parametri.Parametri;
import org.owasp.encoder.*;

public class XSS_Encoder {

	
	private CCS_Common CCS_common; 
	private DMServer4j log;
	private HttpServletRequest request;
	private IDbOperationSQL	dbOpSQL;
	private Environment en;
	private String requestID;
	private String user;
	
	 public XSS_Encoder(CCS_Common newCommon) throws Exception
	 {
		 init(newCommon);
	 }
	 
	 public XSS_Encoder(HttpServletRequest newreq,CCS_Common newCommon) throws Exception
	 {
		 init(newCommon);
		 request=newreq;
	 }
	 
	 public XSS_Encoder(HttpServletRequest req,IDbOperationSQL dbOp) throws Exception {
			request=req;
			requestID=request.getRequestedSessionId();
			user=""+req.getSession().getAttribute("Utente");
			dbOpSQL=dbOp;
			en = new Environment(user, null,null,null, null,dbOp.getConn(),false);
			CCS_common= new CCS_Common(en,user); 
			log= new DMServer4j(XSS_Encoder.class,CCS_common);
	}
	
	 private void init(CCS_Common newCommon)  throws Exception
	 {
	     CCS_common = newCommon;
	     log= new DMServer4j(XSS_Encoder.class,CCS_common); 		    
	}
	
	public String encodeHtmlAttribute(String nome, String input) throws Exception
	{      
		   String output = input;
		   String codificaXSS = "N";
		   try {
			   codificaXSS = getCodificaXSS();
				
			   if(codificaXSS.equals("S") && input!=null){
				   output = Encode.forHtmlAttribute(input);
			   }	   
			}
		    catch (Exception e) {  
			 	log.log_error("XSS_Encoder::encodeHtmlAttribute -- Controllo XSS del parametro - Nome:"+nome);
			 	e.printStackTrace();
			 	throw e;
		    }  
		   
		   return output;
	}
	
	public String encodeJavascriptAttribute(String nome, String input) throws Exception
	{      
		   String output = input;
		   String codificaXSS = "N";
		   try {
			   codificaXSS = getCodificaXSS();
				
			   if(codificaXSS.equals("S") && input!=null){
				   output = Encode.forJavaScriptBlock(output);
			   }	   
			}
		    catch (Exception e) {  
			 	log.log_error("XSS_Encoder::encodeJavascriptAttribute -- Controllo XSS del parametro - Nome:"+nome);
			 	e.printStackTrace();
			 	throw e;
		    }  
		   
		   return output;
	}
	
	public String getCodificaXSS() throws Exception {
		   String parametro="";
		   IDbOperationSQL dbOp=null;  
		   try {
			   if (!CCS_common.dataSource.equals("")) 
			       dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
			    else 
			       dbOp=CCS_common.ev.getDbOp(); 
			   
			   if(Parametri.PARAM_CARICATI==0)	
				   Parametri.leggiParametriConnection(dbOp.getConn());
				
			   parametro = Parametri.CODIFICA_XSS;
			   
			   CCS_common.closeConnection(dbOp); 
			}
		    catch (Exception e) { 
		    	try{CCS_common.closeConnection(dbOp);}catch(Exception ei){}			 	
		    	log.log_error("XSS_Encoder::getCodificaXSS -- Recupero del parametro XSS");
			 	throw e;
		    }  
		   return parametro;
	}
}
