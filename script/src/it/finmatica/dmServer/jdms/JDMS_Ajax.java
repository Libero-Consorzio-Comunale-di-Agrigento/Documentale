package it.finmatica.dmServer.jdms;

import it.finmatica.dmServer.util.CrypUtility;
import it.finmatica.modulistica.domutility.DomUtility;
import it.finmatica.modulistica.parametri.Parametri;

import javax.servlet.http.HttpServletRequest;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import java.io.File;

public class JDMS_Ajax {

	private HttpServletRequest req;
	private DMServer4j log;
	private String inifile;
	private String sPath;
	private String contextPath;
	private String sFunInput = "";
	private String action = "";
    private String nrecord ="";
    private String user;
    private String nominativo;
    private String ruolo;
    private String modulo;
    private String istanza;
    private String URLserver;
    private String resultAction="";
      
	
	public JDMS_Ajax(HttpServletRequest newreq) throws Exception
	{
		try {
           req=newreq; 
		   log= new DMServer4j(JDMS_Ajax.class);
		   init();
		 } 
		 catch (Exception e) {
		e.printStackTrace();
		   }	
	}
	
	public String eseguiAction() throws Exception
	{
		   JDMS_CountRecords rec = null;
		   try {
			   action = req.getParameter("jdms_ajax_action");
			   if (action == null) {
			     action = "";
			   }
			  
			   creatFunctionInput();
		     
			   if (action.equalsIgnoreCase("_GDM_COUNTRECORD")) {
					rec = new JDMS_CountRecords(sFunInput);
					resultAction = rec.countRecords();
			   }
		   } 
		   catch (Exception e) {
			  try {log.log_error("JDMSAjax::eseguiAction - Esecuzione action "+action+" - Errore: "+e.getMessage());} catch (Exception ex) {}
	  	      resultAction=rec.getMessageError();
		   }	   
		   return resultAction;
	}
	
	
	private String showMessageError(String messageError,String messageStacktrace) {
		    Element root;
		    Document docOut = DocumentHelper.createDocument(); 
		    root = DocumentHelper.createElement("FUNCTION_OUTPUT");
		    docOut.setRootElement(root);
	  		DomUtility.aggFiglio(root, "RESULT", "nonok");
	  		DomUtility.aggFiglio(root, "ERROR", messageError);
	  		DomUtility.aggFiglio(root, "STACKTRACE", messageStacktrace);
		    return docOut.asXML();
	}
	
	private void init() throws Exception {
		    contextPath  =req.getContextPath();
		    sPath=req.getRealPath("/"); 
		    user= req.getSession().getAttribute("Utente").toString();
		    nominativo= req.getSession().getAttribute("UserLogin").toString();
		    ruolo= req.getSession().getAttribute("Ruolo").toString();
		    modulo= req.getSession().getAttribute("Modulo").toString();
		    istanza= req.getSession().getAttribute("Istanza").toString();
		    URLserver = req.getScheme();
		    URLserver += "://"+req.getServerName();
		    URLserver += ":"+req.getServerPort();
		    String separa="/";
	        
		    
		    if(req.getSession().getAttribute("PATH_INIFILE")!=null &&  !req.getSession().getAttribute("PATH_INIFILE").equals(""))
		    	inifile = req.getSession().getAttribute("PATH_INIFILE").toString();
	         
	         if(inifile==null || (inifile!=null && inifile.equals("")))
	        	inifile = sPath.replace("jdms","jgdm") + "config" + separa + "gd4dm.properties"; 
	         
	         log.log_info("CCS_Bottoniera::initVarEnv() - Il parametro inifile:"+inifile);
	         
	         /** Controllo path del file properties */ 
	         File f = new File(inifile);
	         if (!f.exists()) {
	        	 inifile = sPath.replace("jdms","jgdm") + "config" + separa + "gd4dm.properties";
	        	 log.log_info("CCS_Bottoniera::initVarEnv() - Controllo del file che non esiste - inifile:"+inifile);
	         }
	        
	        //if(Parametri.PARAM_CARICATI<=1)
	        Parametri.leggiParametriStandard(inifile);
	}
	
	private void creatFunctionInput()throws Exception {
	        try {	
		      Element root, elp;
		      root = DocumentHelper.createElement("FUNCTION_INPUT");
		      Document dDoc = DocumentHelper.createDocument();
		      dDoc.setRootElement(root);
		     
		      elp = DocumentHelper.createElement("CONNESSIONE_DB");
		      elp = aggFiglio(elp,"USER",Parametri.USER);
		      elp = aggFiglio(elp,"PASSWORD",Parametri.PASSWD);
		      elp = aggFiglio(elp,"HOST_STRING",Parametri.SPORTELLO_DSN);
		      elp = aggFiglio(elp,"ALIAS",Parametri.ALIAS);
		      root.add(elp);
		  
		      elp = DocumentHelper.createElement("CONNESSIONE_TOMCAT");
		      elp = aggFiglio(elp,"UTENTE",user);
		      elp = aggFiglio(elp,"NOMINATIVO",nominativo);
		      elp = aggFiglio(elp,"RUOLO",ruolo);
		      elp = aggFiglio(elp,"MODULO",modulo);
		      elp = aggFiglio(elp,"ISTANZA",istanza);
		      elp = aggFiglio(elp,"PROPERTIES",inifile.replaceAll("\\\\","/"));
		      elp = aggFiglio(elp,"URL_SERVER",URLserver);
		      elp = aggFiglio(elp,"CONTEXT_PATH",contextPath);
		      root.add(elp);
		      
		      String valore = req.getSession().getAttribute("NRECORD").toString();
		      
		      String decrpval =null;
		      try
	          {   
			     decrpval=CrypUtility.decriptare(valore);
	          }
			  catch (Exception e) {
			     log.log_error("JDMSAjax::creatFunctionInput::Processo per decriptare la variabile NRECORD:"+e.getMessage());
			  }		      
		      
		      nrecord = decrpval;
		      elp = DocumentHelper.createElement("DOC");
		      elp = aggFiglio(elp,"NRECORD",nrecord);
		      root.add(elp);
		      sFunInput = dDoc.asXML();
		     
	        }
	        catch (Exception e) {
	        	try {log.log_error("JDMSAjax::creatFunctionInput - Costruzione FUNCTION INPUT: "+sFunInput+"- Errore: "+e.getMessage());} catch (Exception ex) {}
	  	        resultAction=showMessageError("Problemi durante l'esecuzione dell'operazione. Non è possibile calcolare il numero di record.",e.getMessage());
	        	throw e;
	        }
	}
	
	private Element aggFiglio(Element elp, String nome, String valore) {
		    Element elf = DocumentHelper.createElement(nome);
		    elf.setText(valore);
		    elp.add(elf);
		    return elp;
	}

}
