package it.finmatica.dmServer.jdms;

import it.finmatica.modulistica.domutility.DomUtility;
import java.util.Calendar;
import java.sql.ResultSet;

import it.finmatica.jfc.dbUtil.*;
import it.finmatica.dmServer.Environment;
import it.finmatica.jfc.dbUtil.SessioneDb;


import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class JDMS_CountRecords {

	private CCS_Common CCS_common; 
	private IDbOperationSQL dbOp;  
	private Environment vu;  
	private DMServer4j log;
	private Document xmlDocument = null;
	private Element root = null;
	private String urlServer = null;
	private String utenteDb = null;
	private String passwDb = null;
	private String aliasDb = null;
	private String dsnDb = null;
	private String utente = null;
	private String contextPath = null;
	private String nrecord = "";
	private String msgError="";

	
	public JDMS_CountRecords(String newxml) throws Exception
	{
		   try  
		   {   
	    	  String xml=null;   
			  if(newxml == null){
				throw new Exception("Problemi durante l'operazione di conteggio dei record. XML FUNCTION_INPUT vuota.");     
			  }
			   
			  xml = newxml.substring(newxml.indexOf("<FUNCTION_INPUT"), newxml.length());
			  xmlDocument = DomUtility.xmlToDocument(xml);
			  root = xmlDocument.getRootElement();  		
			  //init();		        
			}
			catch (Exception e) {
			  closeDbOp();	
			  throw new Exception("JDMS_CountRecords::Inizializzazione: Problemi recupero parametri FUNCTION_INPUT. " +e.getMessage());				  
			}	  
	}
	
	public String getMessageError(){
		   return msgError;
	}
	
	public String countRecords() throws Exception
	{
		   String count="";
           try {
        	   init();
        	   
        	   long inizio=Calendar.getInstance().getTimeInMillis();
        	   log.log_info("*************** INIZIO TOTALIZZATORE RECORD ["+inizio+"]***************");      	
        	   
        	   if(nrecord==null || (nrecord!=null && nrecord.equals(""))){
   			 	throw new Exception("Problemi durante l'operazione di conteggio dei record. Variabile di sessione NRECORD vuota.");     
 			   }
        		   
        	   String v_sessione[] = nrecord.split("#@#"); 
        	   String prov = v_sessione[0];
        	   String valore = v_sessione[1];
        		   
        	   if(prov.equals("C"))
        		 count = valore;
        	   else
        		 count = getNumRecordRicerca(valore);        				   
        	   
			   CCS_common.closeConnection(dbOp);    
        	   long fine=Calendar.getInstance().getTimeInMillis();
			   log.log_info("*************** FINE TOTALIZZATORE RECORD ["+fine+"] ***************");  
			   long trascorso=fine-inizio;
			   log.log_info("*************** TEMPO TRASCORSO TOTALIZZATORE RECORD ["+trascorso+"] millisecondi ***************");       
               
			}
		    catch (Exception e) { 
		    	try { CCS_common.closeConnection(dbOp); } catch (Exception ex) {}
			 	try { log.log_error("JDMS_CountRecords::countRecords - Totalizzatore record trovati - Errore: "+e.getMessage());} catch (Exception ex) {}
		      	msgError = buildFunctionOutput(false,"","Problemi durante l'esecuzione dell'operazione. Non è possibile calcolare il numero di record.",e.getMessage());
		        throw e;
		    }   
		    return  buildFunctionOutput(true,count,"","");
	}
		
	private String getNumRecordRicerca(String sql) throws Exception
	{
	     	String count="";
		    try 
	     	{
		     long inizio=Calendar.getInstance().getTimeInMillis();	
    		 log.log_info("Inizio - Esecuzione select Ricerca. Tempo iniziale: "+inizio);  
    		 
    		 boolean bIsNumeric=true;    		
    		 try {
    			Long.parseLong(sql); 
    		 }
    		 catch (Exception e) {
    			 bIsNumeric=false;
			}
    		 
    		 if (!bIsNumeric) {
	    		 dbOp.setStatement(sql.toString());
	    		 dbOp.execute();
				 ResultSet rs = dbOp.getRstSet();
				 if(rs.next())
				  count = rs.getInt(1)+"";		   
    		 }
    		 else {
    			 count=sql;
    		 }
    			
    		 CCS_common.closeConnection(dbOp);
			 long fine=Calendar.getInstance().getTimeInMillis();
			 log.log_info("Fine - Esecuzione select Ricerca. Tempo finale: "+fine);  
	        }
	        catch (Exception e) {
	        	try { CCS_common.closeConnection(dbOp); } catch (Exception ex) {} 	
	            try {log.log_error("JDMS_CountRecords::getNumRecordRicerca - Problemi durante l'estrazione degli id documenti della Ricerca. SQL: "+sql+"\nErrore: "+e.getMessage());} catch (Exception ex) {}
	            msgError = buildFunctionOutput(false,"","Problemi durante l'esecuzione dell'operazione. Non è possibile calcolare il numero di record.",e.getMessage());
	            throw e;
	        } 
	        return count;
	}
	    
    private void init()  throws Exception
    {
	   		try {
		        Element conTomcat = null,doc = null,connessioneDB = null;
		
				connessioneDB = DomUtility.leggiElementoXML(root, "CONNESSIONE_DB");
				utenteDb = DomUtility.leggiValoreXML(connessioneDB, "USER");
				passwDb = DomUtility.leggiValoreXML(connessioneDB, "PASSWORD");
				aliasDb = DomUtility.leggiValoreXML(connessioneDB, "ALIAS");
				dsnDb = DomUtility.leggiValoreXML(connessioneDB, "HOST_STRING");
				
				conTomcat = DomUtility.leggiElementoXML(root, "CONNESSIONE_TOMCAT");
				contextPath = DomUtility.leggiValoreXML(conTomcat, "CONTEXT_PATH");
				urlServer = DomUtility.leggiValoreXML(conTomcat, "URL_SERVER");
				utente = DomUtility.leggiValoreXML(conTomcat, "UTENTE");
	
				doc = DomUtility.leggiElementoXML(root, "DOC");
				nrecord = DomUtility.leggiValoreXML(doc, "NRECORD");			
				
				dbOp = SessioneDb.getInstance().createIDbOperationSQL(aliasDb,dsnDb, utenteDb, passwDb);
				vu = new Environment(dbOp.getUser(), null,null,null, null,dbOp.getConn(),false);
		        CCS_common = new CCS_Common(vu,dbOp.getUser());
		        log= new DMServer4j(JDMS_CountRecords.class,CCS_common); 		    	
	   		}
	        catch (Exception e) {
	          try {log.log_error("JDMS_CountRecords::init - Problemi recupero parametri FUNCTION INPUT.\nErrore: "+e.getMessage());} catch (Exception ex) {}
	          msgError = buildFunctionOutput(false,"","Problemi durante l'esecuzione dell'operazione. Non è possibile calcolare il numero di record.",e.getMessage().toString());
	          throw e;
	        } 
    }
   
    private String buildFunctionOutput(boolean esito,String nrecord,String messageError,String stacktrace) {
		    String retval = "";
		    String messageStacktrace ="";
		    Element root, eData, eDoc;
		  	
		    Document docOut = DocumentHelper.createDocument(); 
		    root = DocumentHelper.createElement("FUNCTION_OUTPUT");
		    docOut.setRootElement(root);
		  	
		    if (esito) {
		  		DomUtility.aggFiglio(root, "RESULT", "ok");
		  		DomUtility.aggFiglio(root, "ERROR", "");
		  		DomUtility.aggFiglio(root, "STACKTRACE", "");
		  		DomUtility.aggFiglio(root, "ITER_ACTION", "");
		  		DomUtility.aggFiglio(root, "REDIRECT", "");
		  		DomUtility.aggFiglio(root, "FORCE_REDIRECT", "N");
		  		eData = DomUtility.aggFiglio(root, "DATI_AGGIORNAMENTO");
		  		DomUtility.aggFiglio(eData, "DATA","");
		  		eDoc = DomUtility.aggFiglio(root, "DOC");
		  		DomUtility.aggFiglio(eDoc, "NRECORD",nrecord);
		    } else {
		  		DomUtility.aggFiglio(root, "RESULT", "nonok");
		  		DomUtility.aggFiglio(root, "ERROR", messageError);
		  		if(stacktrace!=null)
		  		 messageStacktrace = stacktrace;	
		  		DomUtility.aggFiglio(root, "STACKTRACE", messageStacktrace);
		    }

	  	   	retval = docOut.asXML();
	  	   	return retval;
	 }
   
     protected void closeDbOp() {		  
    	 	   try {this.dbOp.close();}catch(Exception e){}
   	 }
	   	
	 public static void main(String[] args) throws Exception {
			 try {
				SessioneDb.getInstance().addAlias("oracle.","oracle.jdbc.driver.OracleDriver");
			 } catch (Exception e) {
				e.printStackTrace();
			 }
			 String xml = "<FUNCTION_INPUT><CONNESSIONE_DB><USER>GDM</USER><PASSWORD>GDM</PASSWORD><HOST_STRING>jdbc:oracle:thin:@jvm-efesto:1521:orcl</HOST_STRING><ALIAS>oracle.</ALIAS></CONNESSIONE_DB><CONNESSIONE_TOMCAT><UTENTE>GDM</UTENTE><NOMINATIVO>GDM</NOMINATIVO><RUOLO/><MODULO/><ISTANZA/><PROPERTIES>inifile</PROPERTIES><URL_SERVER>http://localhost:8080</URL_SERVER><CONTEXT_PATH>/jdms</CONTEXT_PATH></CONNESSIONE_TOMCAT><CLIENT_GDM><AREA>TESTADS</AREA><CODICE_MODELLO>M_ORIZZONTALE</CODICE_MODELLO><CODICE_RICHIESTA>GDCLIENT1</CODICE_RICHIESTA><RW>W</RW><DATA_AGGIORNAMENTO/><GDC_LINK></GDC_LINK><WFATHER></WFATHER><QUERYSTRING></QUERYSTRING><IDCARTPROVENINEZ></IDCARTPROVENINEZ><TIPOWORKSPACE></TIPOWORKSPACE><IDQUERYPROVENINEZ></IDQUERYPROVENINEZ></CLIENT_GDM>"
				          //+"<DOC><NRECORD>C#@#500</NRECORD></DOC>" 
				          +"<DOC>"
				          +"<NRECORD>Q#@#Select count(*) from (SELECT ID,TI,DA,CR FROM ( SELECT DOCU.ID_DOCUMENTO ID, DOCU.ID_TIPODOC TI, DOCU.DATA_AGGIORNAMENTO DA , DOCU.CODICE_RICHIESTA CR FROM DOCUMENTI DOCU WHERE 1=1 AND DOCU.ID_TIPODOC IN (1,6,8,14,19,20,481,482,483) AND DOCU.STATO_DOCUMENTO NOT IN ('CA','RE','PB') UNION SELECT TO_NUMBER(NULL),TO_NUMBER(NULL),TO_DATE(NULL),TO_CHAR(NULL) FROM DUAL ) A, DUAL WHERE GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI', A.ID, 'L', 'GDM', F_TRASLA_RUOLO('GDM','GDMWEB','GDMWEB'), TO_CHAR(SYSDATE,'dd/mm/yyyy') )||DUMMY = '1X' ORDER BY DA DESC)</NRECORD>" 
				          +"</DOC>"
				          +"</FUNCTION_INPUT>";
			 JDMS_CountRecords  r = new JDMS_CountRecords(xml);
			 System.out.println(r.countRecords());
	 }
}
