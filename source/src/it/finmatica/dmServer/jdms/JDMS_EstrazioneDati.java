package it.finmatica.dmServer.jdms;

import java.util.Calendar;
import java.util.Vector;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import it.finmatica.jfc.dbUtil.*;
import it.finmatica.dmServer.Environment;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.dmServer.util.CrypUtility;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.util.HashMapSet;
import it.finmatica.dmServer.util.UtilityDate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import it.finmatica.dmServer.util.EstraiIDDocumenti;
import it.finmatica.modutils.multirecord.Multirecord;
import it.finmatica.dmServer.motoreRicerca.GD4_Gestione_Query;

public class JDMS_EstrazioneDati {

	private CCS_Common CCS_common; 
	private IDbOperationSQL dbOp;  
	private Environment vu;  
	private DMServer4j log;
	private String user;
	private String idQuery;
	private String idCartella;
	private String fulltext="";
	private String RicercaAllegati; 
	private String RicercaOCR; 
	private String RicercaFT; 
	private boolean tipoEstrazione=true;
	private String tipo_oggetto;
	private String listaID;
	private String[] listaDOC;
	private Vector vlistID;
	private String[] reserveWord = {"\\","&","?","{","}",",","(",")","[","]","-",";","~","|","$","!",">","*","_"} ;
	private String escapeCaracter="\\";
	private HttpServletResponse response;
	private GD4_Gestione_Query q;
	private XSS_Encoder xss=null;
	private String filtroQuery;
	private String codiceFQ;	
	private String funzioneExport;
		 
	
	public JDMS_EstrazioneDati(GD4_Gestione_Query newq,HttpServletRequest request,HttpServletResponse newresponse,CCS_Common newCommon) throws Exception
	{
		   init(newCommon);
		   log.log_info("Inizio - Costruttore Estrazione Dati.");
		   
		   xss = new XSS_Encoder(request,CCS_common);
		   
		   //Recupero idQuery
		   idQuery=verificaParametroGet("idQuery",request.getParameter("idQuery"));
		   if(idQuery==null)
			idQuery = "-1";   
		   //Recupero idCartella
		   idCartella = verificaParametroGet("idCartella",request.getParameter("idCartella")); 
		   if(idCartella==null)
			idCartella="";   
		   if(idCartella.indexOf("C")!=-1 || idCartella.indexOf("c")!=-1)
			idCartella = idCartella.substring(1,idCartella.length());   
		   fulltext = verificaParametroGet("fulltext",request.getParameter("fulltext")); 
		   RicercaAllegati = verificaParametroGet("ricercaAllegati",request.getParameter("ricercaAllegati")); 
		   RicercaOCR = verificaParametroGet("ricercaOCR",request.getParameter("ricercaOCR")); 
		   RicercaFT = verificaParametroGet("ricercaFT",request.getParameter("ricercaFT"));
		   String tipo = verificaParametroGet("tipo",request.getParameter("tipo")); 
		   if(tipo!=null){
			 if(tipo.equals("T"))
			  tipoEstrazione=true;  
			 else
			  tipoEstrazione=false; 
		   }
		   listaID= verificaParametroGet("lista",request.getParameter("lista"));
		   if(listaID==null)
			listaID="";   
		   q=newq;
		   user = (String)request.getSession().getAttribute("Utente");
		   response=newresponse;
		   log.log_info("Fine - Costruttore Estrazione Dati.");
	}

	public JDMS_EstrazioneDati(CCS_Common newCommon,String idQuery,String idCartella,String fulltext,String ricercaAllegati,String tipo,String lista,String listaDoc,String newUser) throws Exception
	{
		   init(newCommon);
		   log.log_info("Inizio - Costruttore Estrazione Dati invocato dal JOB");
		   
		   xss = new XSS_Encoder(CCS_common);
		   
		   //Recupero idQuery
		   this.idQuery=verificaParametroGet("idQuery",idQuery);
		   if(this.idQuery==null || (this.idQuery!=null && this.idQuery.equals("")))
			   this.idQuery = "-1";   
		   
		   //Recupero idCartella
		   this.idCartella = verificaParametroGet("idCartella",idCartella); 
		   if(this.idCartella==null)
			   this.idCartella="";   
		   if(this.idCartella.indexOf("C")!=-1 || this.idCartella.indexOf("c")!=-1)
			   this.idCartella = this.idCartella.substring(1,this.idCartella.length());   
		   
		   this.fulltext = verificaParametroGet("fulltext",fulltext); 
		   RicercaAllegati = verificaParametroGet("ricercaAllegati",ricercaAllegati); 
		   RicercaOCR = "";
		   RicercaFT = "";
		   String tipoEstr = verificaParametroGet("tipo",tipo); 
		   if(tipoEstr!=null){
			 if(tipoEstr.equals("T"))
			  tipoEstrazione=true;  
			 else
			  tipoEstrazione=false; 
		   }
		   listaID= verificaParametroGet("lista",lista);
		   if(listaID==null)
			listaID=""; 
		   
		   String l =verificaParametroGet("listaDoc",listaDoc);
		   if(l!=null && !l.equals(""))
			listaDOC=l.split("#");
		  
		   user = newUser;
		   log.log_info("Fine - Costruttore Estrazione Dati invocato dal JOB");
	}
	
	public JDMS_EstrazioneDati(HttpServletRequest request, CCS_Common newCommon) throws Exception
	{
		   init(newCommon);
		   q = (GD4_Gestione_Query) request.getSession().getAttribute("GD4_GESTIONE_QUERY");	 
		   fulltext = verificaParametroGet("fulltext",request.getParameter("fulltext")); 
		   RicercaAllegati = verificaParametroGet("ricercaAllegati",request.getParameter("ricercaAllegati")); 
		   RicercaOCR = verificaParametroGet("ricercaOCR",request.getParameter("ricercaOCR")); 
		   RicercaFT = verificaParametroGet("ricercaFT",request.getParameter("ricercaFT"));
	}
	
	public JDMS_EstrazioneDati(CCS_Common newCommon,String newUser,String filtroQuery,String codiceFQ,String idQuery,String idCartella,String tipo,String funzioneExport) throws Exception
	{								
		   init(newCommon);
		   log.log_info("Inizio - Costruttore Estrazione Dati Ricerca invocato dal JOB");
		   xss = new XSS_Encoder(CCS_common);
		   this.filtroQuery = filtroQuery;
		   this.codiceFQ = codiceFQ;
		   this.funzioneExport = funzioneExport;
		   user = newUser;
		   this.idQuery=verificaParametroGet("idQuery",idQuery);
		   if(this.idQuery==null || (this.idQuery!=null && this.idQuery.equals("")))
			   this.idQuery = "-1";   
		   this.idCartella = verificaParametroGet("idCartella",idCartella); 
		   String tipoEstr = verificaParametroGet("tipo",tipo); 
		   if(tipoEstr!=null){
			 if(tipoEstr.equals("T"))
			  tipoEstrazione=true;  
			 else
			  tipoEstrazione=false; 
		   }
		   log.log_info("Fine - Costruttore Estrazione Dati invocato dal JOB");
	}
	
	
    private String verificaParametroGet(String parametro, String valore) throws Exception {
	   if (valore == null) {
	    return null;
	   }
	   String newVal=valore;
	   
	   if(xss!=null){
		   newVal = xss.encodeHtmlAttribute(parametro,valore);
		   if (!newVal.equals(valore)) {
		    throw new Exception("Parametro "+parametro+" non valido!");
		   }
	   }
	   return newVal;
    }
	
	public void esportaDati() throws Exception
	{
           try {
        	   long inizio=Calendar.getInstance().getTimeInMillis();
			   log.log_info("*************** INIZIO ESTRAZIONE ID_DOCUMENTI ["+inizio+"]***************");      	
				
			   //Nel caso di una selezione di oggetti dalla workarea
			   if(!listaID.equals("")){
				   vlistID = new Vector();
				   tipo_oggetto="Q";
	        	   fulltext="";
				   getListaID();
			   }	
			   else {			   
	        	   //Nel caso di una ricerca
	        	   if(!idQuery.equals("-1")){
	        	    getElencoIDDocumentiRicerca();
	        	    tipo_oggetto="Q";
	        	    fulltext="";
	        	   }
	        	   //Nel caso di una cartella
	        	   if(idQuery.equals("-1") && !idCartella.equals("")){
	        		 if((fulltext!=null) && (fulltext!=""))
	        	      setWhereFullText(fulltext);   
	        		 getInfoCartella(idCartella);   
	        	 	 tipo_oggetto="C";
	        	   }	        	   
			   }
			   
        	   if(vlistID==null)
        		throw new Exception("Problemi durante l'estrazione degli id documenti. Vettore vuoto.");  
        	   
        	   // Costruzione del Hash Map Set di ID_TIPODOCUMENTO di vettori di ID_DOCUMENTO 
         	   HashMapSet hms = new HashMapSet();
               try{
	        	   log.log_info("Inizio - Costruzione della struttura HMS. - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
	        	   EstraiIDDocumenti ex = new EstraiIDDocumenti(user,vlistID,dbOp);
	        	   hms = ex.estrai(tipo_oggetto,fulltext);
	        	   log.log_info("Inizio - Costruzione della struttura HMS. - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
	          }
         	   catch (Exception e) { 
              	log.log_error("Errore durante la costruzione della struttura HMS. Errore:"+e.getMessage());
              	throw e;
               } 
         	    
         	   if(hms.size()==0)
            	 throw new Exception("Problemi durante l'estrazione degli id documenti. Hash Map Set vuoto.");   
               
         	   // Esportazione dei dati in formato exe 
         	   try{
               	log.log_info("Inizio - Esportazione dei dati con invocazione della MultiRecord. - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
   	               Multirecord mr = new Multirecord();
                   response.setContentType("application/vnd.ms-excel");	
                   response.setHeader("Content-Disposition", "inline;filename=\"estrazione_"+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+".xls\"");
                   mr.esportaDatiExcell(dbOp,hms,response.getOutputStream(),tipoEstrazione);
                   log.log_info("Fine - Esportazione dei dati con invocazione della MultiRecord. - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
       	       } 
   	     	   catch (Exception e) { 
   	     		   e.printStackTrace();
   	     		   log.log_error("Errore durante l'esportazione dei dati in excell - Invocazione MultiRecord::esportaDatiExcell - Errore: "+e.getMessage());
   	     		   throw e;
   	           } 
               
        	   CCS_common.closeConnection(dbOp);    
        	   long fine=Calendar.getInstance().getTimeInMillis();
			   log.log_info("*************** FINE ESTRAZIONE ID_DOCUMENTI ["+fine+"] ***************");  
			   long trascorso=fine-inizio;
			   log.log_info("*************** TEMPO TRASCORSO ESTRAZIONE ID_DOCUMENTI ["+trascorso+"] millisecondi ***************");       

           }
           catch (Exception e) { 
        	CCS_common.closeConnection(dbOp);   
        	log.log_error("JDMS_EstrazioneDati::esportaDati -- Esportazione dei dati - idQuery:"+idQuery+" e idCartella:"+idCartella+" - Errore: "+e.getMessage());
        	throw e;
           }  
	}   	
	
	public InputStream esportaDatiToInputStream() throws Exception
	{

		   InputStream is;
		
           try {
        	   long inizio=Calendar.getInstance().getTimeInMillis();
			   log.log_info("*************** INIZIO ESTRAZIONE ID_DOCUMENTI ["+inizio+"]***************");      	
				
			   //Nel caso di una selezione di oggetti dalla workarea
			   if(!listaID.equals("")){
				   vlistID = new Vector();
				   tipo_oggetto="Q";
	        	   fulltext="";
				   getListaID();
			   }	
			   else {			   
	        	   //Nel caso di una ricerca
	        	   if(!idQuery.equals("-1")){
	        	    getVectorDocumentiRicerca(listaDOC);   
	        	    tipo_oggetto="Q";
	        	    fulltext="";
	        	   }
	        	   //Nel caso di una cartella
	        	   if(idQuery.equals("-1") && !idCartella.equals("")){
	        		 if((fulltext!=null) && (fulltext!=""))
	        	      setWhereFullText(fulltext);   
	        		 getInfoCartella(idCartella);   
	        	 	 tipo_oggetto="C";
	        	   }	        	   
			   }
			   
        	   if(vlistID==null)
        		throw new Exception("Problemi durante l'estrazione degli id documenti. Vettore vuoto.");  
        	   
        	   // Costruzione del Hash Map Set di ID_TIPODOCUMENTO di vettori di ID_DOCUMENTO 
         	   HashMapSet hms = new HashMapSet();
               try{
	        	   log.log_info("Inizio - Costruzione della struttura HMS. - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
	        	   EstraiIDDocumenti ex = new EstraiIDDocumenti(user,vlistID,dbOp);
	        	   hms = ex.estrai(tipo_oggetto,fulltext);
	        	   log.log_info("Inizio - Costruzione della struttura HMS. - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
	          }
         	   catch (Exception e) { 
         		e.printStackTrace();   
              	log.log_error("Errore durante la costruzione della struttura HMS. Errore:"+e.getMessage());
              	throw e;
               } 
         	    
         	   if(hms.size()==0)
            	 throw new Exception("Problemi durante l'estrazione degli id documenti. Hash Map Set vuoto.");   
               
         	   try{
            	   log.log_info("Inizio - Esportazione dei dati con invocazione della MultiRecord. - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
   	               Multirecord mr = new Multirecord();
                   is = mr.esportaDatiExcellToInputStream(dbOp,hms,tipoEstrazione,"",user);
                   log.log_info("Fine - Esportazione dei dati con invocazione della MultiRecord. - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
       	       } 
   	     	   catch (Exception e) { 
   	     		e.printStackTrace();   
   	          	log.log_error("Errore durante l'esportazione dei dati in excell - Invocazione MultiRecord::esportaDatiExcell - Errore: "+e.getMessage());
   	          	throw e;
   	           }   
     	  
        	   CCS_common.closeConnection(dbOp);    
        	   long fine=Calendar.getInstance().getTimeInMillis();
			   log.log_info("*************** FINE ESTRAZIONE ID_DOCUMENTI ["+fine+"] ***************");  
			   long trascorso=fine-inizio;
			   log.log_info("*************** TEMPO TRASCORSO ESTRAZIONE ID_DOCUMENTI ["+trascorso+"] millisecondi ***************");       

           }
           catch (Exception e) { 
        	CCS_common.closeConnection(dbOp);   
        	log.log_error("JDMS_EstrazioneDati::esportaDati -- Esportazione dei dati - idQuery:"+idQuery+" e idCartella:"+idCartella+" - Errore: "+e.getMessage());
        	throw e;
           }  
           return is;
	}   	
	
	
	public InputStream esportaRicercaToInputStream() throws Exception
	{

		   InputStream is;
		   String sSelect="";
		
           try {
        	   long inizio=Calendar.getInstance().getTimeInMillis();
			   log.log_info("*************** INIZIO ESTRAZIONE ID_DOCUMENTI ["+inizio+"]***************");      	
				
			   String xmlFiltroSelect = verificaFiltro(filtroQuery, codiceFQ.getBytes());
			   
        	   if(!idQuery.equals("-1"))
	        	  tipo_oggetto="Q";
        	   else
        		  tipo_oggetto="C";
			   
			   DocumentBuilder builder =  DocumentBuilderFactory.newInstance().newDocumentBuilder();
               Document doc = null;
               try{
                InputSource inStream = new InputSource();
                
                inStream.setCharacterStream(new StringReader(xmlFiltroSelect));
                
                doc = builder.parse(inStream);
                
               }
               catch(Exception e){ doc=null;}
	      
               NodeList nodes = doc.getElementsByTagName("SELECT");  
		          
               nodes = doc.getElementsByTagName("SELECT");  
               for (int i = 0; i < nodes.getLength(); i++) {       
             	  sSelect=Global.replaceAll(Global.replaceAll(nodes.item(i).getAttributes().item(0).getNodeValue(),"&gt;",">"),"&lt;","<");
             	  sSelect=sSelect.replaceAll(":UtenteGDM",this.user.toUpperCase());
               }
			   
               log.log_info("FILTRO SQL :: "+sSelect);
			   getElencoFromRicercaMod(sSelect);
			   			   
        	   if(vlistID==null || (vlistID!=null && vlistID.size()==0))
        		throw new Exception("Problemi durante l'estrazione degli id documenti. Vettore vuoto.");  
        	   
        	   log.log_info("Numero :: "+vlistID.size());
        	   
        	   // Costruzione del Hash Map Set di ID_TIPODOCUMENTO di vettori di ID_DOCUMENTO 
         	   HashMapSet hms = new HashMapSet();
               try{
	        	   log.log_info("Inizio - Costruzione della struttura HMS. - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
	        	   EstraiIDDocumenti ex = new EstraiIDDocumenti(user,vlistID,dbOp);
	        	   hms = ex.estrai(tipo_oggetto,"");
	        	   log.log_info("Inizio - Costruzione della struttura HMS. - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
	          }
         	   catch (Exception e) { 
         		e.printStackTrace();   
              	log.log_error("Errore durante la costruzione della struttura HMS. Errore:"+e.getMessage());
              	throw e;
               } 
         	    
         	   if(hms.size()==0)
            	 throw new Exception("Problemi durante l'estrazione degli id documenti. Hash Map Set vuoto.");   
               
         	   try{
            	   log.log_info("Inizio - Esportazione dei dati con invocazione della MultiRecord. - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
   	               Multirecord mr = new Multirecord();
                   is = mr.esportaDatiExcellToInputStream(dbOp,hms,tipoEstrazione,funzioneExport);
                   log.log_info("Fine - Esportazione dei dati con invocazione della MultiRecord. - ["+UtilityDate.now("dd/MM/yyyy HH:mm:ss")+"] ");
       	       } 
   	     	   catch (Exception e) { 
   	     		e.printStackTrace();   
   	          	log.log_error("Errore durante l'esportazione dei dati in excell - Invocazione MultiRecord::esportaDatiExcell - Errore: "+e.getMessage());
   	          	throw e;
   	           }   
     	  
        	   CCS_common.closeConnection(dbOp);    
        	   long fine=Calendar.getInstance().getTimeInMillis();
			   log.log_info("*************** FINE ESTRAZIONE ID_DOCUMENTI ["+fine+"] ***************");  
			   long trascorso=fine-inizio;
			   log.log_info("*************** TEMPO TRASCORSO ESTRAZIONE ID_DOCUMENTI ["+trascorso+"] millisecondi ***************");       

           }
           catch (Exception e) { 
        	CCS_common.closeConnection(dbOp);   
        	log.log_error("JDMS_EstrazioneDati::esportaRicercaToInputStream -- Esportazione dei dati da ricerca modulistica - idQuery:"+idQuery+" e idCartella:"+idCartella+" - Errore: "+e.getMessage());
        	throw e;
           }  
           return is;
	}   
	
	public String elaboraElencoIDDocumentiRicerca() throws Exception
	{
		   String doc = ""; 
	     	try 
	     	{         
    		 log.log_info("Inizio - Costruzione del vettore di id documenti.");  
    		 vlistID = new Vector();  
    		 
    		 if(q!=null){
	             if (fulltext!=null) 
	             {    
	             	 if(RicercaAllegati!=null && RicercaAllegati.equals("S"))  {
	                   q.setFullTextObjCondition(fulltext);
	                 }
	                 
	                 if(RicercaOCR!=null && RicercaOCR.equals("S"))  {
	 	               q.setFullTextObjOCRCondition(fulltext);
	 	             }	 
	                 
	                 if(RicercaFT!=null && RicercaFT.equals("S"))  {
	               	   q.setFullTextCondition(fulltext); 
	 	             }	 
	             }
	             
	             q.setFetchSize(-1);
	    		 vlistID=q.risultatoQuery(true);
	    		 	    			
	    		 for(int i=0;i<vlistID.size();i++){
	    			doc+=vlistID.get(i)+"#";
	    		 }
	    		 
	    		 if(vlistID.size()>0)
	    			 doc = doc.substring(0,doc.length()-1);
    		 }
    	
             log.log_info("Fine - Costruzione del vettore di id documenti.");  
	        }
	        catch (Exception e) {
	          log.log_error("JDMS_EstrazioneDati::getElencoIDDocumenti - Problemi durante l'estrazione degli id documenti della Ricerca. Errore: "+e.getMessage());
  		      throw e;                                        
	        } 
	     	return doc;
	}
	
	private String verificaFiltro(String filtro,byte[] CCFQ) throws Exception 
	{
            String ret="";
		    try
	        {   
				ret = CrypUtility.decriptare(filtro);   		    	   
	        }
            catch (Exception e) {             
            	log.log_error("CCS_GestioneQuery_Common::verificaFiltro - Problemi durante la verifica del filtro.");
            	throw e;
            } 
            return ret;
	}
	
	private void getListaID()throws Exception
	{
		    String[] seq=listaID.split("@");
		    String seqD="",seqC="";
		    
		    for(int i=0;i<seq.length;i++){
		    	String e=seq[i];
		    	
		    	if(e.indexOf("D")!=-1){
		    	 seqD+="SELECT "+e.substring(1,e.length())+ " FROM DUAL UNION ";
		    	}
		    	
		    	if(e.indexOf("C")!=-1){
			   	 seqC+="SELECT "+e.substring(1,e.length())+ " FROM DUAL UNION ";		
		    	}
		    }   
		    
		    if(!seqC.equals("")){
		    	if(seqC.lastIndexOf("UNION")!=-1)
				  seqC = seqC.substring(0,seqC.lastIndexOf("UNION"));	
		    	getTipoDocumentoCartelle(seqC);
		    }
		    
		    if(!seqD.equals("")){
		    	if(seqD.lastIndexOf("UNION")!=-1)
		    	  seqD = seqD.substring(0,seqD.lastIndexOf("UNION"));	
		    	getTipoDocumentoDocumenti(seqD);
		    }
	}
	
	private void getTipoDocumentoCartelle(String seq) throws Exception
	{
		    String info="";
		    IDbOperationSQL dbOpSQL=null;
		    StringBuffer sql = new StringBuffer();
            try
            {			    
	           sql.append("select id_tipodoc||'@'||id_documento_profilo info ");
	           sql.append("from cartelle,documenti  ");
	           sql.append("where id_cartella in ("+seq+")");
	           sql.append(" and id_documento = id_documento_profilo  ");
               
	           dbOpSQL= SessioneDb.getInstance().createIDbOperationSQL(dbOp.getConn(),0);
	           dbOpSQL.setStatement(sql.toString());
	           dbOpSQL.execute();
			   ResultSet rs = dbOpSQL.getRstSet();
			   while(rs.next()){ 
			    info=rs.getString("info");			    
			    vlistID.add(info);
			   } 
			   dbOpSQL.close();
	        }
	        catch (SQLException e) {
	        	dbOpSQL.close();
	        	log.log_error("JDMS_EstrazioneDati::getTipoDocumentoCartelle(seq):("+seq+") - SQL: "+sql+"- Errore: "+e.getMessage());
	        	throw e;
	         }
	}
	
	private void getTipoDocumentoDocumenti(String seq) throws Exception
	{
		    String info="";
		    IDbOperationSQL dbOpSQL=null;
		    StringBuffer sql = new StringBuffer();
            try
            {			    
	           sql.append("select id_tipodoc||'@'||id_documento info ");
	           sql.append("from documenti  ");
	           sql.append("where id_documento in ("+seq+")");
	       
	           dbOpSQL= SessioneDb.getInstance().createIDbOperationSQL(dbOp.getConn(),0);
	           dbOpSQL.setStatement(sql.toString());
	           dbOpSQL.execute();
			   ResultSet rs = dbOpSQL.getRstSet();
			   while(rs.next()){ 
			    info=rs.getString("info");			    
			    vlistID.add(info);
			   } 
			   dbOpSQL.close();
	        }
	        catch (SQLException e) {
	        	dbOpSQL.close();
	        	log.log_error("JDMS_EstrazioneDati::getTipoDocumentoDocumenti(seq):("+seq+") - SQL: "+sql+"- Errore: "+e.getMessage());
	        	throw e;
	         }
	}
	
	
	private void getInfoCartella(String id) throws Exception
	{
		    String info="";
		    IDbOperationSQL dbOpSQL=null;
		    StringBuffer sql = new StringBuffer();
            try
            {			    
	           sql.append("select id_tipodoc||'@'||id_documento_profilo info ");
	           sql.append("from cartelle,documenti  ");
	           sql.append("where id_cartella = :id ");
	           sql.append(" and id_documento = id_documento_profilo  ");
               
	           dbOpSQL= SessioneDb.getInstance().createIDbOperationSQL(dbOp.getConn(),0);
	           dbOpSQL.setStatement(sql.toString());
	           dbOpSQL.setParameter(":id", id);
	           dbOpSQL.execute();
			   ResultSet rs = dbOpSQL.getRstSet();
			   if(rs.next()){ 
			    info=rs.getString("info");
			    vlistID = new Vector();  
			    vlistID.add(info);
			   } 
			   dbOpSQL.close();
	        }
	        catch (SQLException e) {
	        	dbOpSQL.close();
	        	log.log_error("JDMS_EstrazioneDati::getInfoCartella(id):("+id+") - SQL: "+sql+"- Errore: "+e.getMessage());
	        	throw e;
	         }
	}
	
	private void getElencoFromRicercaMod(String sql) throws Exception
	{
		    IDbOperationSQL dbOpSQL=null;
		    
		    vlistID = new Vector();  	
            try
            {	
               dbOpSQL = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
               dbOpSQL.setStatement(sql.toString());
               dbOpSQL.execute();
			   ResultSet rs = dbOpSQL.getRstSet();
			   while(rs.next()){ 
                String id_documento = rs.getString("id");
                String id_tipodoc = rs.getString("ti");
                vlistID.add(id_tipodoc+'@'+id_documento);
			   } 
			   try{dbOpSQL.close();}catch (Exception ei) {}
	        }
	        catch (SQLException e) {
	        	try{dbOpSQL.close();}catch (Exception ei) {}
	        	log.log_error("JDMS_EstrazioneDati::getElencoFromRicercaMod(SQL) :: "+sql+"- Errore: "+e.getMessage());
	        	throw e;
	         }
	}
	
	
	private void getElencoIDDocumentiRicerca() throws Exception
	{
	     	try 
	     	{         
    		 log.log_info("Inizio - Costruzione del vettore di id documenti.");  
    		 vlistID = new Vector();
    		 
    		 
             if (fulltext!=null) 
             {    
             	 if(RicercaAllegati.equals("S"))  {
                   q.setFullTextObjCondition(fulltext);
                 }
                 
                 if(RicercaOCR.equals("S"))  {
 	               q.setFullTextObjOCRCondition(fulltext);
 	             }	 
                 
                 if(RicercaFT.equals("S"))  {
               	   q.setFullTextCondition(fulltext); 
 	             }	 
             }
             
    		 q.setFetchSize(-1);
    		 vlistID=q.risultatoQuery(true);
             log.log_info("Fine - Costruzione del vettore di id documenti.");  
	        }
	        catch (Exception e) {
	          log.log_error("JDMS_EstrazioneDati::getElencoIDDocumenti - Problemi durante l'estrazione degli id documenti della Ricerca. Errore: "+e.getMessage());
  		      throw e;                                        
	        } 
	}
	
	private void getVectorDocumentiRicerca(String[] lista) throws Exception
	{
	     	try 
	     	{         
    		 log.log_info("Inizio - Costruzione del vettore di id documenti per la lista ");  
    		 vlistID = new Vector();  
    		 if(lista!=null){
	    		 for(int i=0;i<lista.length;i++){
	    			 vlistID.add(lista[i]);
	    		 }
    		 }
             log.log_info("Fine - Costruzione del vettore di id documenti.");  
	        }
	        catch (Exception e) {
	          log.log_error("JDMS_EstrazioneDati::getVectorDocumentiRicerca - Problemi durante la costruzione del vettore. Errore: "+e.getMessage());
  		      throw e;                                        
	        } 
	}
	
   private void setWhereFullText(String w)  throws Exception
   {
	       String sCondizioneFullText="";
	       String filtro="";
	       
		   if (w!=null && (!w.equals("")) ) 
		   {
				  try {
					  Long.parseLong(w);
					  sCondizioneFullText="("+w+")";
				  }
				  catch(Exception e) {
					  java.util.StringTokenizer s = new java.util.StringTokenizer(protectReserveWord(w)," ");			  			  			  
					  sCondizioneFullText+="(" ;
					  while (s.hasMoreTokens())
					  {
						sCondizioneFullText+=s.nextElement();
		   	            if (s.hasMoreTokens()) sCondizioneFullText+=" AND ";
		              }
				     sCondizioneFullText+=")";		     		    				  
				  }
				  
                  filtro = " AND f_filtro_fulltext_warea(d.id_documento,'"+sCondizioneFullText+"'";
				  
				  if(RicercaAllegati.equals("S"))
					filtro+=" ,'S'";
				  else
					filtro+=" ,' '";   
				
				  if(RicercaOCR.equals("S"))
					filtro+=" ,'S'";	   
				  else
					filtro+=" ,' '";  
				  
				  if(RicercaFT.equals("S"))
					filtro+=" ,'S'";	   
				  else
					filtro+=" ,' '";  
				  
				  filtro+=" )>0 "; 
				  
				  fulltext = filtro;
		  }
   } 
   
   /**
    * Costruzione della frase da filtrare con la protezione 
    * di alcuni caratteri speciali.
    */
   private String protectReserveWord(String phrase) {
 	   for(int i=0;i<reserveWord.length;i++) {
 		   phrase=vu.Global.replaceAll(phrase,reserveWord[i],escapeCaracter+reserveWord[i]);
 	   }
 
     return phrase;
   }
	    
   private void init(CCS_Common newCommon)  throws Exception
   {
	        CCS_common=newCommon;
	        log= new DMServer4j(JDMS_EstrazioneDati.class,CCS_common); 
		    if (!CCS_common.dataSource.equals("")) {
             dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
             vu = new Environment(CCS_common.user, null,null,null, null,dbOp.getConn(),false);
	        }
	        else 
	        {
	         vu=CCS_common.ev;
	         dbOp=CCS_common.ev.getDbOp();
	        }		    
   }
	
}
