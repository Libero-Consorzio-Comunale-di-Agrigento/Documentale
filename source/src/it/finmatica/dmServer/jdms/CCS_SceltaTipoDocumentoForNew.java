package it.finmatica.dmServer.jdms;

import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.util.DocUtil;
import it.finmatica.dmServer.competenze.*;
import it.finmatica.jfc.dbUtil.*;
import java.net.URLEncoder;
import java.sql.*;

import javax.servlet.http.HttpServletRequest;

/**
 * Gestione della creazione di un Documento.
 * Classe di servizio per la gestione del Client
*/

public class CCS_SceltaTipoDocumentoForNew 
{

	   /**
	    * Variabili private
	   */	
	   String idDocumento,identifierUpFolder;
	   String idTipoDoc="";
	   String cm,area,cr,rw;
	   String gdc_link,idQueryProvenienza,idCartProvenienza,Provenienza;
	   String tag="5";
	   String parametro="JDMS_LINK";
	   HttpServletRequest req;
	   CCS_Common CCS_common; 
	   private Environment vu;
	   private DMServer4j log;
	   
	   /**
		 * Costruttore utilizzato per scegliere un
		 * modello di un Documento.
		 * 
		 */
	   public CCS_SceltaTipoDocumentoForNew(String newidDocumento,String newidentifierUpFolder,String newrw,
			   								String newidQueryProvenienza,String newProvenienza,
			   								HttpServletRequest newreq,CCS_Common newCommon) throws Exception
	   {
		      rw=newrw;
		      if (rw==null) rw="W";
		      idDocumento=newidDocumento;
		      idQueryProvenienza=newidQueryProvenienza;
		      identifierUpFolder=newidentifierUpFolder;
		      if(identifierUpFolder.indexOf("C")!=-1)
		       identifierUpFolder=identifierUpFolder.substring(1,identifierUpFolder.length());	
		      Provenienza=newProvenienza;
		      req=newreq;   
		      CCS_common=newCommon;
		      log= new DMServer4j(CCS_SceltaTipoDocumentoForNew.class,CCS_common); 
	   }
	   
	   /**
		 * Costruttore utilizzato per scegliere un
		 * modello di un Documento.
		 * 
		 */
	   public CCS_SceltaTipoDocumentoForNew(String newProvenienza,String newidQueryProvenienza,String newidTipoDoc,
			   								String newidCartProvenienza,HttpServletRequest newreq,CCS_Common newCommon) throws Exception
	   {
		      Provenienza=newProvenienza;
		      idQueryProvenienza=newidQueryProvenienza;
		      if(newidTipoDoc!=null && !(newidTipoDoc.equals("")) && !(newidTipoDoc.equals("null")))
		       idTipoDoc=newidTipoDoc;
		      idCartProvenienza=newidCartProvenienza;
		      if(idCartProvenienza.indexOf("C")!=-1)
		       idCartProvenienza=idCartProvenienza.substring(1,idCartProvenienza.length());	
		      req=newreq;         
		      rw="W";
		      CCS_common=newCommon;
		      log= new DMServer4j(CCS_SceltaTipoDocumentoForNew.class,CCS_common); 
	   }
	   
	   /**
		 * Nel caso di creazione di un Documento viene verificato se si 
		 * possiedono le competenze e si visualizza la lista di modelli
		 * altrimenti viene effettuata la redirect alla pagina del Documento
		 * in modifica.
		 * 
		 */
	   public String _afterInitialize() throws Exception 
	   {
           	  String message=null;
		   	  IDbOperationSQL dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);               
              vu = new Environment(CCS_common.user, null,null,null, null,dbOp.getConn());
              vu.connect();                              
       
              try 
              {   
                /** Inserimento di un nuovo Documento */ 
                if (idDocumento == null)     
                {
                   /** Verifica delle competenze di creazione sulla cartella padre 
                     * Se si vuole inserire un nuovo Documento all'interno di una Query
                     * non viene controllato se si possiedono le copmetenze 
                     * di inserimento in cartella dato che il documento creato non 
                     * viene inserito nella cartella padre della query */
                   if( Provenienza.equals("C"))
                   {         
                	 if (!verificaCompetenzaCartella(identifierUpFolder,Global.ABIL_CREA))
                	 {
                       message = "NONOK";                         
                       throw new Exception(message);
                	 }
                	 else
                	 {
                      try{vu.disconnectClose();}catch(Exception ei){}
                      CCS_common.closeConnection(dbOp);
                      message="OK";
                	 }
                   }
                   else
                   {
                      try{vu.disconnectClose();}catch(Exception ei){}
                      CCS_common.closeConnection(dbOp);
                      message="OK";
                   }
                }
                else 
                {
                    try {
                     this.getDatiDocumento(dbOp);
                    }
                    catch (Exception e) {
                     throw new Exception("CCS_SceltaTipoDocumentoForNewHandler::_afterInitialize() - getDatiDocumento - idDocumento:"+idDocumento+"\n" + e.getMessage());
                    }
              
                    /** Setto il messaggio di ritorno per effettuare la Redirect */
                    message=SetMessage();
                    
                    try{vu.disconnectClose();}catch(Exception ei){}
                    CCS_common.closeConnection(dbOp);
                 
                }
                return message;
              }
              catch (Exception e) 
              {
            	  try{vu.disconnectClose();}catch(Exception ei){}
            	  CCS_common.closeConnection(dbOp);  
            	  log.log_error("CCS_SceltaTipoDocumentoForNewHandler::_afterInitialize() - idDocumento:"+idDocumento+"\n" + e.getMessage());
            	  throw e;
              }   
	   }
	   
	   /**
		 * Se esiste un solo modello per qule tipo di oggetto (Cartella o Query)
		 * viene effettuata la redirect sulla pagina del Documento da creare
		 * per quel determinato modello.
		 * 
		 */
	   public String _beforeShow(String Tendina) throws Exception 
	   {
	          
		   /** Controllo il valore della Tendina */       
		   	  if (Tendina==null || Tendina.equals("")) return "";	
		   	  
		   	  /** Se il valore è un elenco di possibili valori return */
		   	  if (Tendina.indexOf("</OPTION>")!=Tendina.lastIndexOf("</OPTION>")) return "";
       
		   	  String message;
		   	  IDbOperationSQL dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
		   	  
		   	  try
		   	  {
                String id=Tendina.substring(Tendina.indexOf("=\"")+2,Tendina.indexOf("\">"));
                
                if((id!=null) && (id!=""))
 			    {	
	                cm=null;
	                cr=null;
	                area=null;
	                idTipoDoc = id;
	           
		            try {
		              this.getDatiModelli(idTipoDoc,dbOp);
		            }
		            catch (Exception e) {
		            	throw new Exception("CCS_SceltaTipoDocumentoForNewHandler::_beforeShow - getDatiModelli - idTipoDocumento:"+idTipoDoc+"\n" + e.getMessage());
		            }
	
		            /**  Setto il parametro rw  a MODIFICA */
		            rw="W"; 
		            
		            /** Setto il messaggio di ritorno per effettuare la Redirect */
		            message= SetMessage();
 			    }
 		   	    else
 		   	     message="";
 		   		
	            CCS_common.closeConnection(dbOp);     
	            return message;
	            
              }
	          catch (Exception e) 
	          {
	           CCS_common.closeConnection(dbOp);  
	           log.log_error("CCS_SceltaTipoDocumentoForNewHandler::_beforeShow() - idTipoDocumento:"+idTipoDoc+"\n" + e.getMessage());
	           throw e;
	          }   
	   }
	   
	   /**
		 * Viene gestita la scelta del modello e la redirect sulla pagina
		 * del Documento da creare.
		 * 
		 */
	   public String _OnClick() throws Exception 
	   {
 		   	  String message="";
 		   	  IDbOperationSQL dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
 		   	  
 		   	  try
 		   	  {   
 		   		if((idTipoDoc!=null) && (idTipoDoc!=""))
 			    {	
 		   		 cm=null;
 		   		 cr=null;
 		   		 area=null;
           
 		   		 try {
                  this.getDatiModelli(idTipoDoc,dbOp);
	             }
	             catch (Exception e) {
	              	throw new Exception("CCS_SceltaModelloScegli::_OnClick - getDatiModelli - idTipoDocumento:"+idTipoDoc+"\n" + e.getMessage());
	             }
             
	             /** Setto il messaggio di ritorno per effettuare la Redirect */
	             message=SetMessage();
 			    }
 		   		else
 		   		message="";	
 		   		CCS_common.closeConnection(dbOp);
                return message;
 		   	  }
	         catch (Exception e) 
	         {
	           CCS_common.closeConnection(dbOp);  
	           log.log_error("CCS_SceltaTipoDocumentoForNewHandler::_OnClick() - idTipoDocumento:"+idTipoDoc+"\n" + e.getMessage());
	           throw e;
	         }   
	   }
	   
	   /**
		 * Nel caso di creazione documenti automatizzati.
		 * Se esiste la variabile di sessione del tipo di modello
		 * viene effettuata la redirect della pagina di creazione
		 * di un Documento. 
		 * 
		 */
	   public String creaDoc(String idTipoDocSession) throws Exception 
	   {
		 	  String message=null; 
		 	  IDbOperationSQL dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
		 	  vu = new Environment(CCS_common.user, null,null,null, null,dbOp.getConn());
       
		      if((idTipoDocSession!=null) && (idTipoDocSession!=""))
		      {	   
		    	 try
		    	 {
		            cm=null;
		            cr=null;
		            area=null;
		            idTipoDoc=idTipoDocSession;
		            
		            try {
		              this.getDatiModelli(idTipoDoc,dbOp);
		            }
		            catch (Exception e) {
		             	throw new Exception("CCS_SceltaModelloScegli::creaDoc - getDatiModelli - idTipoDocumento:"+idTipoDoc+"\n" + e.getMessage());
		   	        }
		            /**  Setto il parametro rw  a MODIFICA */
		            rw="W"; 
					/** Setto il messaggio di ritorno per effettuare la Redirect */
		            message= SetMessage();
		            CCS_common.closeConnection(dbOp);     
		            return message;
		          }
		          catch (Exception e) 
		          {
		           CCS_common.closeConnection(dbOp);
		           throw e;
		          }   
		      }
		      return message;
	   }

	   
	   /**
		 * Costruisce il messaggio da ritornare. 
		 * Viene effettuato un controllo se esiste nel tag=5 per quel tipo_documento
		 * un URL ad un'altra pagina altrimenti vine invocata la DocumentView.
		 * Viene effettuato il binding di alcuni parametri.
		 * 
		 */
	   private String SetMessage()throws Exception 
	   {
              String url_page="";
              IDbOperationSQL dbOp = SessioneDb.getInstance().createIDbOperationSQL(CCS_common.dataSource,0);
		 	  
		      try
		      {
		    	  String parametri =buildParametri();
		    	  JDMSLink link = new JDMSLink(idTipoDoc,tag,parametro,dbOp);
		    	  link.retrieveJDMLink();
		    	
		    	  if(!link.getURL().equals("")){
		    		 
		    	    url_page = bindingURLPage(link.getURL(),"","D",area,cm,rw,"","",idCartProvenienza,idQueryProvenienza,"ServletModulisticaDocumento","BO",Provenienza,gdc_link);
		    	    
		    	  }
		    	  else {
		    		url_page="DocumentoView.do?"+parametri;
		    	  
		    		
		    	  }	
		      }
		      catch (Exception e) 
		      {
		           CCS_common.closeConnection(dbOp);
		           throw e;
		      }
           
              return url_page;
      }   
	  
	  
	  /** 
	   *   Sostituizione dei parametri per la costruzione del url della pagina 
	   * */ 
	  private String bindingURLPage(String url,String idOggetto,String tipoOggetto,String area,String cm,String rw,
			  					   String cr,String profilo,String idCartProvenienza,String idQueryProvenienza,
			  					   String mvpg,String stato,String provenienza,String gdc_link) throws Exception {
		      
		  	  JDMSLinkParser p = new JDMSLinkParser(req,idOggetto,tipoOggetto,area,cm,cr,profilo,idCartProvenienza,idQueryProvenienza,rw,
		  			                 mvpg,stato,provenienza,gdc_link);
		  
	          String urlpagina = p.bindingDeiParametri(url);
	          
	          
	          urlpagina="../common/URLPageRedirect.do?redirect="+URLEncoder.encode(urlpagina);
	          
		      return urlpagina;
	  } 
	   
	   /**
		 * Costruisce dei parametri da associare all'url della pagina 
		 * 
		 */
	   private String buildParametri()
	   {
              String par;
              String qry,idCart,link;
              
              par = "MVPG=ServletModulisticaDocumento&rw="+rw+"&cm="+cm+"&area="+area+"&cr="+cr+"&stato=BO";
              
              if (idDocumento==null){
	           	   if (idCartProvenienza!=null)
	           	   {
	           		if(idCartProvenienza.equals("null"))
	           		  idCart="&idCartProveninez=";
	           		else 
	           	  	  idCart="&idCartProveninez="+idCartProvenienza;
	           	  }
	           	  else
	           	  {	 
	           	   if(identifierUpFolder==null)
	           		 idCart="&idCartProveninez=";
	           	   else
	           		 idCart="&idCartProveninez="+identifierUpFolder;
	           	  }
              }	 
              else
               idCart="&idCartProveninez="+identifierUpFolder;
              
              if (Provenienza.equals("Q")) 
           	   qry = "&idQueryProveninez="+idQueryProvenienza;
              else
           	   qry = "&idQueryProveninez=-1";
      
              if (Provenienza.equals("Q"))
            	link = "../common/ClosePageAndRefresh.do?idQueryProveninez="+idQueryProvenienza;
              else
            	link = "../common/ClosePageAndRefresh.do?idQueryProveninez=-1";    
              
              //Settare la variabile GDC_LINK
              gdc_link = URLEncoder.encode(link);
              
              par+=idCart+qry+"&Provenienza="+Provenienza+"&GDC_Link="+gdc_link;
              
              return par;//"DocumentoView.do?MVPG=ServletModulisticaDocumento&rw="+rw+"&cm="+cm+"&area="+area+"&cr="+cr+"&stato=BO"+qry+"&GDC_Link="+URLEncoder.encode(u);
      }   
	   
	   
	   /**
		 * Verifica la competenza "tipoCompetenza" sulla cartella idCartella. 
		 * 
		 */
	   private boolean verificaCompetenzaCartella(String idCartella,String tipoCompetenza) throws Exception
	   {
	           /** Non controllo la competenza sulle WorkSpace Standard User o System */
	           if (idCartella.equals("-2") || idCartella.equals("-1")) 
	            return true;
	           
	           try
	           {
	        	  String idw = (new DocUtil(vu)).getIdViewCartellaByIdCartella(idCartella);
	        	  Abilitazioni abilitazione = new Abilitazioni(Global.ABIL_CARTELLA, idw , tipoCompetenza); 
	        	  UtenteAbilitazione ua = new UtenteAbilitazione(vu.getUser(), vu.getGruppo(), vu.getRuolo(),vu.getPwd(),  vu.getUser(), vu);
	        	  if ((new GDM_Competenze(vu)).verifica_GDM_Compentenza(ua,abilitazione)  == 1 ) {               
	               return true;
	        	  }
	              return false;
	           }
	           catch (Exception e) {        
	        	   log.log_error("CCS_SceltaTipoDocumentoForNewHandler::verificaCompetenzaCartella() - idCartella:"+idCartella+" - tipoCompetenza:"+tipoCompetenza);
	        	   throw e;
		       }                  
	   }
	   
	   /**
		 * Recupera alcune informazioni relative al docuemnto 
		 * 	AREA, CM, CR, ID_TIPO_DOCUMENTO 
		 */
	   private void getDatiDocumento(IDbOperationSQL dbOp) throws Exception
	   {
		       StringBuffer sStm=null;
		       try 
		   	   {          
		   		  sStm = new StringBuffer();
		   		  sStm.append(" SELECT TD.NOME,D.AREA,D.CODICE_RICHIESTA,D.ID_TIPODOC ");
		   		  sStm.append(" FROM DOCUMENTI D,TIPI_DOCUMENTO TD");
		   		  sStm.append(" WHERE TD.ID_TIPODOC=D.ID_TIPODOC AND");
		   		  sStm.append(" D.ID_DOCUMENTO = :IDDOCUMENTO");
		   		  dbOp.setStatement(sStm.toString());
		   		  dbOp.setParameter(":IDDOCUMENTO",idDocumento);
		   		  dbOp.execute();
		   		  ResultSet rst = dbOp.getRstSet();
		   		  
		   		  if (rst.next()) 
		   		  {
		   			 this.cm=rst.getString(1);
		   			 this.area=rst.getString(2);
		   			 this.cr=rst.getString(3);
		   			 this.idTipoDoc=rst.getString(3);
		          }
		          else 
		             throw new Exception("CCS_SceltaTipoDocumentoForNewHandler::getDatiDocumento() - idDocumento:"+idDocumento);                              
		   	   }
		   	   catch (Exception e) {   
		   		  log.log_error("CCS_SceltaTipoDocumentoForNewHandler::getDatiDocumento() - SQL:"+sStm.toString());
		   		  throw e; 
		   	   }
	   }
	   
	   /**
		 * Recupera alcuni dati di un modello. 
		 * 
		 * @param idTipoDoc  indica id_tipo_documento altrimenti
		 * 					 nel caso di un modello figlio è composto dalla terna 
		 *  					CODICE_MODELLO_FIGLIO@AREA@ID_TIPODOC_CODICE_MODELLO_PADRE 
		 */
	   private void getDatiModelli(String idTipoDoc,IDbOperationSQL dbOp) throws Exception
	   {
		       StringBuffer sStm=null; 	   
		       try
		   	   {  
		    	  sStm = new StringBuffer(); 
		    	  //Caso di codice modello figlio 
		    	  if(idTipoDoc.indexOf("@")!=-1){
		    		String[] seq= idTipoDoc.split("@");
		    		sStm.append("select codice_modello,area,'GDCLIENT'||to_char(CODR_SQ.nextval) ");
			   		sStm.append("from modelli where area = :area and codice_modello = :codice_modello");
			   		dbOp.setStatement(sStm.toString());
			   		dbOp.setParameter(":area", seq[1]);
			   		dbOp.setParameter(":codice_modello", seq[0]);
			   	  }
		    	  else {
		    		sStm.append("select codice_modello,area,'GDCLIENT'||to_char(CODR_SQ.nextval)");
			   		sStm.append(" from modelli where id_tipodoc = :idTipoDoc");
			   		dbOp.setStatement(sStm.toString());
			   		dbOp.setParameter(":idTipoDoc",idTipoDoc);
		    	  }
		    	  
		   		  dbOp.execute();
		   		  ResultSet rst = dbOp.getRstSet();
	              if (rst.next()) {
				     cm=rst.getString(1);
				     area=rst.getString(2);
				     cr=rst.getString(3);
				  }
	              else 
	            	throw new Exception("CCS_SceltaTipoDocumentoForNewHandler::getDatiModelli() - idTipoDoc:"+idTipoDoc);                              
	      		
		   	   }
		   	   catch (Exception e) {    
		   		  log.log_error("CCS_SceltaTipoDocumentoForNewHandler::getDatiModelli() - SQL:"+sStm.toString());
		   		  throw e; 
		   	   }
	   }
}