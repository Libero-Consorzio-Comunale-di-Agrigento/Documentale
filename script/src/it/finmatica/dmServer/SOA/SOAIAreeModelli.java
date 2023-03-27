package it.finmatica.dmServer.SOA;

import java.util.Vector;

import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.dbEngine.struct.dbTable.Area;
import it.finmatica.dmServer.dbEngine.struct.dbTable.Metadato;
import it.finmatica.dmServer.dbEngine.struct.dbTable.Modello;
import it.finmatica.dmServer.util.LookUpDMTable;
import it.finmatica.dmServer.util.keyval;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;

import javax.servlet.http.HttpServletRequest;

public class SOAIAreeModelli extends SOAIGenericService {
	   private HttpServletRequest 	request			= null;
	   private String				user			= null;
	   
	   //Costructor for WS
	   public SOAIAreeModelli(String us, String jndi) throws Exception {
		   	  user=us;
		   	  
		   	  dbOperation = SessioneDb.getInstance().createIDbOperationSQL(jndi,0);
		   	  
		   	  if (dbOperation==null)
		   		  throw new Exception("Attenzione! Problemi nel creare la connessione verso il database.\nVerificare che la connessione jndi="+jndi+" sia stata definita per il servizio");
	   }
	   
	   //Costructor for SOA
	   public SOAIAreeModelli(HttpServletRequest req,IDbOperationSQL dbOp) throws Exception {
		      
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
	   public String elencoAree() {
		   	  try {
		        Vector<Area> vAree = elencoAreeForWS();
		        Vector<Vector> vAreeRet = new Vector<Vector>();
		        
		        for(int i=0;i<vAree.size();i++) {
		        	Area ar = (Area)vAree.get(i);		        			        	
		        	
		        	keyval kVal = new keyval(ar.getCodice(),ar.getDescrizione());

                    kVal.setTipoDoc(SOAXMLDataRet._XML_TYPE_AREE);

                    Vector<keyval> v = new Vector<keyval>();	
                    
                    v.add(kVal);
                    
                    vAreeRet.add(v);                                        
		        }
		        
		        return (new SOAXMLDataRet("1",vAreeRet)).getXML();
		   	  }
			  catch (Exception e) {
				try {closeDbOp();}catch(Exception ei){}
			    return (new SOAXMLErrorRet("Errore in elenco aree: " +e.getMessage())).getXML();		    			                   
			  }				  			  
	   }
	   
	   public String elencoModelli(String area) {
		   	  try {
		        Vector<Modello> vModelli = elencoModelliForWS(area);
		        Vector<Vector> vModelliRet = new Vector<Vector>();
		        
		        for(int i=0;i<vModelli.size();i++) {
		        	Modello mod = (Modello)vModelli.get(i);		        			        	
		        	
		        	keyval kVal = new keyval(mod.getArea(),mod.getCodice());

	                 kVal.setTipoDoc(SOAXMLDataRet._XML_TYPE_MODELLI);
	
	                 Vector<keyval> v = new Vector<keyval>();	
	                 
	                 v.add(kVal);
	                 
	                 vModelliRet.add(v);                                        
		        }
		        
		        return (new SOAXMLDataRet("1",vModelliRet)).getXML();
		   	  }
			  catch (Exception e) {
				try {closeDbOp();}catch(Exception ei){}
			    return (new SOAXMLErrorRet("Errore in elenco modelli per area ("+area+"): " +e.getMessage())).getXML();		    			                   
			  }				  			  
	   }	  
	   
	   public String elencoMetadati(String area,String codiceModello) {
		   	  try {
		        Vector<Metadato> vMetadati = elencoMetadatiForWS(area,codiceModello);
		        Vector<Vector> vMetadatiRet = new Vector<Vector>();
		        
		        for(int i=0;i<vMetadati.size();i++) {
		        	Metadato metadato = (Metadato)vMetadati.get(i);		        			        	
		        	
		        	keyval kVal = new keyval(metadato.getArea(),metadato.getCodiceModello());

	                kVal.setTipoDoc(SOAXMLDataRet._XML_TYPE_METADATI);
	                kVal.setTipoDaClient(metadato.getCodice());
	                kVal.setOperator(metadato.getTipo());
	
	                Vector<keyval> v = new Vector<keyval>();	
	              
	                v.add(kVal);
	              
	                vMetadatiRet.add(v);                                        
		        }
		        
		        return (new SOAXMLDataRet("1",vMetadatiRet)).getXML();
		   	  }
			  catch (Exception e) {
				try {closeDbOp();}catch(Exception ei){}
			    return (new SOAXMLErrorRet("Errore in elenco metadati per area ("+area+") e codiceModello("+codiceModello+"): " +e.getMessage())).getXML();		    			                   
			  }				  			  
	   }	   
	   
	   
	   /////////PARTE PUBLIC (PER WS) -- DA NON METTERE NELLA PUBBL. AUTOMATICA DELLA SOA
	   public Vector<Area> elencoAreeForWS() throws Exception {
		   	     LookUpDMTable lUpDmT = new LookUpDMTable(new Environment(user,"","","","",dbOperation.getConn()));
		   		 
		   	   
		   	     Vector<Area> vArea=lUpDmT.lookUpListaAree();
		   	  	 try {closeDbOp();}catch(Exception e){}
		   	  	 
		   	     return vArea;				   		 		   	
	   }
	   
	   public Vector<Modello> elencoModelliForWS(String area) throws Exception {
	   	      LookUpDMTable lUpDmT = new LookUpDMTable(new Environment(user,"","","","",dbOperation.getConn()));
	   		 
	   	     
	   	      Vector<Modello> vMod  = lUpDmT.lookUpListaAree(area);
	   	      try {closeDbOp();}catch(Exception e){}
	   	   
	   	      return vMod;			   		 		   	
	   }
	   
	   public Vector<Metadato> elencoMetadatiForWS(String area,String codiceModello) throws Exception {
	   	      LookUpDMTable lUpDmT = new LookUpDMTable(new Environment(user,"","","","",dbOperation.getConn()));	   		 
	   	     
	   	      Vector<Metadato> vMetaDati =lUpDmT.lookUpListaMetaDati(area, codiceModello);
	   	      
	   	      try {closeDbOp();}catch(Exception e){}
	   	      
	   	      return vMetaDati;			   		 		   	
	   }	   
}
