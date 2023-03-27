package it.finmatica.dmServer.SOA;

import java.io.InputStream;
import java.util.Vector;

import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.Impronta.ImprontaAllegati;
import it.finmatica.dmServer.dbEngine.struct.dbTable.Allegato;
import it.finmatica.dmServer.management.Profilo;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.util.LookUpDMTable;
import it.finmatica.dmServer.util.keyval;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.jfc.utility.Base64;

import javax.servlet.http.HttpServletRequest;

public class SOAIAllegati extends SOAIGenericService {
	   private HttpServletRequest 	request			= null;
	   private String				user			= null;
	   
	   //Costructor for WS
	   public SOAIAllegati(String us, String jndi) throws Exception {
		   	  user=us;
		   	  
		   	  dbOperation = SessioneDb.getInstance().createIDbOperationSQL(jndi,0);
		   	  
		   	  if (dbOperation==null)
		   		  throw new Exception("Attenzione! Problemi nel creare la connessione verso il database.\nVerificare che la connessione jndi="+jndi+" sia stata definita per il servizio");
	   }
	   
	   //Costructor for SOA
	   public SOAIAllegati(HttpServletRequest req,IDbOperationSQL dbOp) throws Exception {		      
		      try  {   
			        request=req;
			        if(request!=null)
			           user=request.getSession().getAttribute("Utente")+"";
			        dbOperation=dbOp;			        
			      }
			      catch (Exception e) {
			    	closeDbOp();
			    	throw new Exception((new SOAXMLErrorRet("Inizializzazione: " +e.getMessage())).getXML().toString());				  
			      }	  
	   }
	   	 
	   /////////PARTE PUBLIC (PER SOA)
	   public String getAllegato(String idAllegato) throws Exception {
		   	  Allegato a = null;
		   	  try {
		   	    a = getAllegatoPrivate(Integer.parseInt(idAllegato));
		   	  }
		   	  catch(Exception e) {
		   		try {closeDbOp();}catch(Exception ei){}
		   		return (new SOAXMLErrorRet("Errore in getAllegato ("+idAllegato+"): " +e.getMessage())).getXML();
		   	  }		   	  
		   	  
		   	  Base64 b = new Base64();
		   	  String sBase64 = b.f_encode(a.getAllegato());
		   	  try {closeDbOp();}catch(Exception e){}		   	  
		   	  
		   	  Vector<Vector> vMetaAllegatiRet = new Vector<Vector>();
			  keyval kVal = new keyval(""+a.getIdAllegato(),a.getDescrizione());
	
	          kVal.setTipoDoc(SOAXMLDataRet._XML_TYPE_ALLEGATI);
	          kVal.setTipoDaClient(sBase64);
	          Vector<keyval> v = new Vector<keyval>();	
              
              v.add(kVal);
            
              vMetaAllegatiRet.add(v);
	          
		   	  return (new SOAXMLDataRet("1",vMetaAllegatiRet)).getXML();
	   }
	   
	   /*TODO DA IMPLEMENTARE
	   public String setAllegato(String idDocumento, String fileBase64, String filename ) throws Exception {
		   	  return "";
	   }*/
	   
	   /////////PARTE PUBLIC (PER WS)
	   public Allegato getAllegatoForWs(int idAllegato) throws Exception {
		      Allegato a = getAllegatoPrivate(idAllegato);
		   	  		   	  		   	 
		   	  try {closeDbOp();}catch(Exception e){}
	   		
		   	  return a;
	   }
	   
	   public boolean verificaFirmaAllegatoForWs(int idAllegato) throws Exception {
		      boolean bRet = verificaFirmaAllegatoPrivate(idAllegato);
		   	  		   	  		   	 
		   	  try {closeDbOp();}catch(Exception e){}
	   		
		   	  return bRet;
	   }
	   
	   
	   /////////PARTE PRIVATA (NON ESPOSTA)
	   private Allegato getAllegatoPrivate(int idAllegato) throws Exception {
		   	   Environment vu = new Environment(user,"","","","",dbOperation.getConn());
		   	   LookUpDMTable lUpDmT = new LookUpDMTable(vu);
		   	   	   	       
	   	       String idDoc=lUpDmT.lookUpIdDocumentoFromIdAllegato(""+idAllegato);	
	   	       
	   	       Profilo p = new Profilo(idDoc);
	   	       p.initVarEnv(vu);
	   	       
	   	       if (p.accedi(Global.ACCESS_ATTACH).booleanValue()) {
	   	    	   InputStream is=null;
	   	    	   String nomeFile=p.getFileName(Long.parseLong(""+idAllegato));
	   	    	   String dataAgg = p.getUpdateDateFile(Long.parseLong(""+idAllegato));
	   	    	   try {
	   	    		   is = p.getFileStream(nomeFile);
	   	    	   }
	   	    	   catch (Exception e) {
	   	    		   throw new Exception("Attenzione! Errore in accesso al contenuto del file con id="+idAllegato);
	   	    	   }
	   	    	   
	   	    	   Allegato a = new Allegato();
	   	    	   
	   	    	   a.setAllegato(Global.getBytesToEndOfStream(is));
	   	    	   //TODO
	   	    	   //a.setDataInserimento(dataInserimento)
	   	    	   a.setIdAllegato(idAllegato);
	   	    	   a.setTipoAllegato(Global.lastTrim(nomeFile,".","").toUpperCase());
	   	    	   a.setDescrizione(nomeFile);
	   	    	   a.setDataAggiornamento(dataAgg);
	   	    	   
	   	    	   return a;
	   	       }
	   	       else {
	   	    	   throw new Exception("Attenzione! Errore in accesso al documento "+idDoc+" per recuperare l'allegato "+idAllegato+"\nErrore: "+p.getError());
	   	       }
	   }
	   
	   private boolean verificaFirmaAllegatoPrivate(int idAllegato) throws Exception {
	   	      Environment vu = new Environment(user,"","","","",dbOperation.getConn());
	   	      LookUpDMTable lUpDmT = new LookUpDMTable(vu);
	   	   	   	       
   	          String idDoc=lUpDmT.lookUpIdDocumentoFromIdAllegato(""+idAllegato);	
   	       
   	          Profilo p = new Profilo(idDoc);
   	          p.initVarEnv(vu);
   	       
   	          if (p.accedi(Global.ACCESS_ATTACH).booleanValue()) {
   	    	      String nomeFile;
   	        	  try {nomeFile=p.getFileName(Long.parseLong(""+idAllegato));}catch(Exception e){throw new Exception("Attenzione! Errore in recupero nome allegato da identificativo "+idAllegato+".\nErrore: "+e.getMessage());}
   	    	      
   	        	  ImprontaAllegati ia = new ImprontaAllegati(p);
   	        	  
   	        	  try {
   	        	    if (ia.verificaImpronta(nomeFile).equals(Global.CODERROR_IA_NESSUN_ERRORE)) return true;
   	        	  } 
   	        	  catch(Exception e) {
   	        		throw new Exception("Attenzione! Errore in verifica impronta per allegato con nome="+nomeFile+" per documento="+idDoc+"\nErrore: "+e.getMessage());
   	        	  }
   	        	  
   	          }
   	          else {
   	    	     throw new Exception("Attenzione! Errore in accesso al documento "+idDoc+" per recuperare l'allegato "+idAllegato+"\nErrore: "+p.getError());
   	          }
   	          
   	          return false;
   	          
	   }	   
}	
