package it.finmatica.dmServer.SOA;

import java.net.URLDecoder;
import java.util.StringTokenizer;
import java.util.Vector;

import it.finmatica.dmServer.dbEngine.struct.dbTable.MetadatoSimple;
import it.finmatica.dmServer.management.ICartella;
import it.finmatica.dmServer.management.Profilo;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.util.keyval;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;

import javax.servlet.http.HttpServletRequest;

public class SOAICartella extends SOAIGenericService {

	   private HttpServletRequest 	request		= null;
	   //private IDbOperationSQL		dbOperation		= null;
	   private String 				idCartella		= null;
	   private String				user			= null;
	   private String				rootFolder		= null;
	   private String				pathFolder		= null;
	   private String				nameFolder		= null;	   		   
	   
	   protected final static int _ERR_INTEGRITY_PAR____NONAME       = -5;	   
	   
	   public SOAICartella(HttpServletRequest req,IDbOperationSQL dbOp,String idFolder) throws Exception {
		      try  {   
		        request=req;
		        if(request!=null)
		           user=request.getSession().getAttribute("Utente")+"";
		        dbOperation=dbOp;
		        idCartella=idFolder;		        
		      }
		      catch (Exception e) {
		    	closeDbOp();
		    	throw new Exception((new SOAXMLErrorRet("Inizializzazione: " +e.getMessage())).getXML().toString());				  
		      }	  
	   }	
	   
	   public SOAICartella(String us, String jndi,String idFolder, 
               			   String area, String cm, String cr) throws Exception {
			  try  {   
			    			    
			    user=us;
			    
			    dbOperation=SessioneDb.getInstance().createIDbOperationSQL(jndi,0);
			    idCartella=idFolder;	
			    this.areaModel=area;
				this.cmModel=cm;
				this.crModel=cr;			     
					
			  }
			  catch (Exception e) {
				closeDbOp();
				throw new Exception((new SOAXMLErrorRet("Inizializzazione: " +e.getMessage())).getXML().toString());				  
			  }	  
	   }		   
	   
	   public SOAICartella(HttpServletRequest req,IDbOperationSQL dbOp,String idFolder, 
			               String area, String cm, String cr) throws Exception {
		      try  {   
		        request=req;
		        if(request!=null)
		           user=request.getSession().getAttribute("Utente")+"";
		        dbOperation=dbOp;
		        idCartella=idFolder;	
		        this.areaModel=area;
		  		this.cmModel=cm;
		 		this.crModel=cr;			     
		 		
		      }
		      catch (Exception e) {
		    	closeDbOp();
		    	throw new Exception((new SOAXMLErrorRet("Inizializzazione: " +e.getMessage())).getXML().toString());				  
		      }	  
	   }	   
	   
	   public SOAICartella(HttpServletRequest req,IDbOperationSQL dbOp,
			               String area, String cm, String cr, String idFolder,
			               String rootFolder, String pathFolder, String nameFolder) throws Exception {
		      try  {   
		        request=req;
		        if(request!=null)
		           user=request.getSession().getAttribute("Utente")+"";
		        dbOperation=dbOp;
		        idCartella=idFolder;	
		        this.areaModel=area;
		  		this.cmModel=cm;
		 		this.crModel=cr;		 	
		 		this.rootFolder=rootFolder;
		 		this.pathFolder=pathFolder;
		 		this.nameFolder=nameFolder;
		 		
		      }
		      catch (Exception e) {
		    	closeDbOp();
		    	throw new Exception((new SOAXMLErrorRet("Inizializzazione: " +e.getMessage())).getXML().toString());				  
		      }	  
	   }	   
	   
	   public String creaCollegamentoDesktop(String categoria) {		   		      
		      ICartella Ic;
		      
		      int iCheckIntPar=checkIntegrityParameter();
		      if (iCheckIntPar<0) {
		    	  closeDbOp();
		    	  return (new SOAXMLErrorRet(generateErrorParameterMessage(iCheckIntPar))).getXML();		    			                   
		      }
		      
		      try {
			    Ic = new ICartella(idCartella);		      
                Ic.initVarEnv(user,user,dbOperation.getConn());			      			      			      			      
		      }
			  catch (Exception e) {
				closeDbOp();
				return (new SOAXMLErrorRet("Prima dell'aggiornamento: " +e.getMessage())).getXML();		    			                   
			  }
			  
			  try {
				Ic.creaCollegamentoDesktop(categoria);
	            Ic.update();
	 		  }
			  catch (Exception e) {
				closeDbOp();
				return (new SOAXMLErrorRet("Aggiornamento: " +e.getMessage())).getXML();				  
			  }	
			  
			  try {
				dbOperation.commit();
				closeDbOp();
				return (new SOAXMLResult()).getXML();
			  }
			  catch (Exception e) {
				closeDbOp();
				return (new SOAXMLErrorRet("Salvataggio: " +e.getMessage())).getXML();				  
			  }	
	   }
	   		   
	   public String registra(String datiXML, String aclXML, String escludiControlloCompetenze,
			   				  String copiaOggetti, String spostaOggetti, String eliminaOggetti) {
		      ICartella Ic;		
		      Vector<ACL> acl=null;
		   
		      int iCheckIntPar=checkIntegrityParameter();
		      if (iCheckIntPar<0) {
		    	  closeDbOp();
		    	  return (new SOAXMLErrorRet(generateErrorParameterMessage(iCheckIntPar))).getXML();
		      }
		      
		      //Tolgo il CDATA dagli XML
		      datiXML=removeCDATATAG(datiXML);		      
		      aclXML=removeCDATATAG(aclXML);
		      copiaOggetti=removeCDATATAG(copiaOggetti);
		      spostaOggetti=removeCDATATAG(spostaOggetti);
		      eliminaOggetti=removeCDATATAG(eliminaOggetti);
		      		      
			  //Inserimento/Aggiornamento dei valori
		      try {
			      if (iCheckIntPar==_INTEGRITY_PAR____OK_IDDOC)
			    	  Ic = new ICartella(idCartella);
			      else if (iCheckIntPar==_INTEGRITY_PAR____OK_ARCM)
			    	  Ic = new ICartella(areaModel,cmModel,rootFolder,pathFolder,nameFolder);			      
			      else
			    	  Ic = new ICartella(areaModel,cmModel,crModel);
	
			      Ic.initVarEnv(user,user,dbOperation.getConn());
			      
			      if (!datiXML.equals("")) {
			    	  Vector<keyval> vDati=parseDatiXML(datiXML);
				      
				      for(int i=0;i<vDati.size();i++)
				    	  Ic.addValue(vDati.get(i).getKey(),(new URLDecoder()).decode(vDati.get(i).getVal()));				      				      
			      }				      			     
			      
			      if (!nameFolder.equals(""))
			    	  Ic.addValue("NOME",nameFolder);
			      
			      //Gestione ACL
			      if (!aclXML.equals("")) {
			    	  acl = new Vector<ACL>();
			    	  
			    	  try {		     	 	  
			    	      acl=parseACLXML(aclXML);
			    	  }
					  catch (Exception e) {
						  closeDbOp();
						  return (new SOAXMLErrorRet("Errore nel parsing dell'XML ACL: " +e.getMessage())).getXML();		    			                   
					  }		    	  
			      }			      
			      
				  //Gestione ACL
				  if (acl!=null) {
					  for(int i=0;i<acl.size();i++) {
						  ACL aclVar = acl.get(i);
						  						
						  Ic.settaACL(aclVar.user,aclVar.type);						
					  }
				  }
				  //FINE Gestione ACL
				  
				  //Gestione elimina oggetti
			      if (!eliminaOggetti.equals("")) {
			    	  Vector<keyval> vDati=parseDatiXML(eliminaOggetti,"IdObject","TypeObject",null);
				      			    	  
				      for(int i=0;i<vDati.size();i++) 
				    	  Ic.deleteInObject(vDati.get(i).getKey(),vDati.get(i).getVal());				      				      
			      }
				  //FINE Gestione elimina oggetti			      
				  				  
				  if (!escludiControlloCompetenze.equals("")) Ic.escludiControlloCompetenze(true);
			      
		      }
			  catch (Exception e) {
				  closeDbOp();
				  return (new SOAXMLErrorRet("Caricamento dei valori: " +e.getMessage())).getXML();		    			                   
			  }		      
			  
			  try {
			      if (iCheckIntPar==_INTEGRITY_PAR____OK_ARCM)
			    	  Ic.insert();
			      else
			    	  Ic.update();
			      
			      //Ora lancio la copia oggetti...che esegue operazioni dopo la insert o update ma sulla
			      //stessa sessione dbOp
			      if (!copiaOggetti.equals("")) {
			    	  Vector<keyval> vDati=parseDatiXML(copiaOggetti,"IdObject","TypeObject",null);
				      
			    	  StringBuffer sCopiaObj = new StringBuffer("");
				      for(int i=0;i<vDati.size();i++) {
				    	  sCopiaObj.append(vDati.get(i).getVal()+vDati.get(i).getKey());
				    	  
				    	  if (i!=vDati.size()-1) sCopiaObj.append("@");
				      }
				      
				      Ic.copiaOggetti(sCopiaObj.toString());
			      }
			      
			      //Ora lancio la sposta oggetti...che esegue operazioni dopo la insert o update ma sulla
			      //stessa sessione dbOp			      
			      if (!spostaOggetti.equals("")) {
			    	  Vector<keyval> vDati=parseDatiXML(spostaOggetti,"IdObject","TypeObject","IdFolderFrom");
				      
			    	  StringBuffer sCopiaObj = new StringBuffer("");
				      for(int i=0;i<vDati.size();i++) {
				    	  sCopiaObj.append(vDati.get(i).getVal()+vDati.get(i).getKey()+","+vDati.get(i).getValueBetween());
				    	  
				    	  if (i!=vDati.size()-1) sCopiaObj.append("@");
				      }
				      
				      Ic.spostaOggetti(sCopiaObj.toString());
			      }			      
			      
			      dbOperation.commit();
			      closeDbOp();
			      
				  Vector<Vector> vDoc = new Vector<Vector>();
				  Vector<keyval> v = new Vector<keyval>();
				  v.add(new keyval("IDFOLDER",Ic.getIdentifierFolder()));
				 
				  vDoc.add(v);
				 
				  return (new SOAXMLDataRet("1",vDoc)).getXML();			      
		      }
			  catch (Exception eInsertIntoFolder) {
					try {dbOperation.rollback();}catch (Exception eClose) {}
					closeDbOp();
					return (new SOAXMLErrorRet("Salvataggio: " +eInsertIntoFolder.getMessage())).getXML();						  
			  }		
			  		      
	   }	  	  
	   
	   public String getInfo() {
		   	  final String _FOLDERINFO     = "<FOLDERINFO>";
		   	  final String _END_FOLDERINFO = "</FOLDERINFO>";
		   	  
		   	  final String _DATAFIELD      = "<DATAFIELD>";
		   	  final String _END_DATAFIELD  = "</DATAFIELD>";
		   	  
		   	  final String _OBJFIELD       = "<OBJFIELD>";
		   	  final String _END_OBJFIELD   = "</OBJFIELD>";		   	  
		   	  		   			   
		      int iCheckIntPar=checkIntegrityParameterRetrieve();
		      if (iCheckIntPar<0) {
		    	  closeDbOp(); 
		    	  return (new SOAXMLErrorRet(generateErrorParameterMessage(iCheckIntPar))).getXML();
		      }
		      
		      ICartella Ic = null;
		      StringBuffer sXmlFolderInfo  = new StringBuffer(_FOLDERINFO);
		      StringBuffer sXmlDatiProfilo = new StringBuffer(_DATAFIELD);
		      StringBuffer sXmlObjProfilo  = new StringBuffer(_OBJFIELD);
		      StringBuffer sXmlFull        = new StringBuffer("");
		      
		      try {
			      if (iCheckIntPar==_INTEGRITY_PAR____OK_IDDOC)
			    	  Ic = new ICartella(idCartella);			      		     
			      else
			    	  Ic = new ICartella(areaModel,cmModel,crModel);
	
			      Ic.initVarEnv(user,user,dbOperation.getConn());
		      }
			  catch (Exception e) {
				  closeDbOp(); 
				  return (new SOAXMLErrorRet("Errore in accesso cartella: " +e.getMessage())).getXML();		    			                   
			  }		
			  			  
			  //*******************************Recupero i valori del profilo			  
			  Profilo p = null;
			  
			  try {
				  p = new Profilo(Ic.getProfileFolder());
				  
				  p.initVarEnv(user,user,dbOperation.getConn());
				  
				  if (p.accedi(Global.ACCESS_NO_ATTACH).booleanValue()) {
					  sXmlFolderInfo.append("<AREA>"+p.getArea()+"</AREA>");
					  sXmlFolderInfo.append("<CODICE_MODELLO>"+p.getCodiceModello()+"</CODICE_MODELLO>");
					  sXmlFolderInfo.append("<CODICE_RICHIESTA>"+p.getCodiceRichiesta()+"</CODICE_RICHIESTA>");
					  sXmlFolderInfo.append("<ID>"+Ic.getIdentifierFolder()+"</ID>");
					  sXmlFolderInfo.append(_END_FOLDERINFO);
					  
					  if (p.getlistaValori().size()>0) {	
						  for(int i=0;i<p.getlistaValori().size();i++)  {
							  keyval k = p.getlistaValori().get(i);
							  
							  sXmlDatiProfilo.append("<field>");
							  sXmlDatiProfilo.append("<nome>"+k.getKey()+"</nome>");
							  sXmlDatiProfilo.append("<valore>"+_CDATAINIT+k.getVal().replaceAll("null","")+_CDATAEND+"</valore>");
				              sXmlDatiProfilo.append("</field>");		                      
						  }						               		                 		                  		                
					  }					  
					  					  
					  sXmlDatiProfilo.append(_END_DATAFIELD);
				  }
				  else {
					  closeDbOp(); 
					  return (new SOAXMLErrorRet("Errore in accesso ai valori della cartella: " +p.getError())).getXML();
				  }
			  }	      
			  catch (Exception e) {
				  closeDbOp(); 
				  return (new SOAXMLErrorRet("Errore in accesso ai valori della cartella: " +e.getMessage())).getXML();		    			                   
			  }				  
			  //*******************************Fine Recupero i valori del profilo
			  
			  			  
			  //*******************************Recupero gli oggetti della cartella
			  try {
				  String element=Ic.getElementInFolder("T",null,null);
				  
				  StringTokenizer sElenco = new StringTokenizer(Global.nvl(element,""),"@");
				  
				  while (sElenco.hasMoreTokens()) {
					    String item = sElenco.nextToken();
					    
					    String type, id;
					    
					    type=item.substring(0,item.indexOf(","));
					    id=item.substring(item.indexOf(",")+1);
					    
					   	sXmlObjProfilo.append("<field>");
					   	sXmlObjProfilo.append("<IdObject>"+id+"</IdObject>");
					   	sXmlObjProfilo.append("<TypeObject>"+type+"</TypeObject>");
					   	sXmlObjProfilo.append("</field>");							   						   	
				  }
				  
				  sXmlObjProfilo.append(_END_OBJFIELD);
				  
			  }	      
			  catch (Exception e) {
				  closeDbOp(); 
				  return (new SOAXMLErrorRet("Errore in accesso agli oggetti presenti nella cartella: " +e.getMessage())).getXML();		    			                   
			  }	
			  //*******************************Fine recupero oggetti della cartella
			  			
			  closeDbOp();
			  
			  sXmlFull.append(sXmlFolderInfo); 
			  sXmlFull.append(sXmlDatiProfilo);
			  sXmlFull.append(sXmlObjProfilo);
			  
			  sXmlFull.append("<IDUPFOLDER>");
			  sXmlFull.append(Ic.getIndentifierUpFolder());
			  sXmlFull.append("</IDUPFOLDER>");
			  			  
			  sXmlFull.append("<STATO>");
			  try {sXmlFull.append(Ic.getStato());}catch (Exception e) {}
			  sXmlFull.append("</STATO>");
			  			   
			  return (new SOAXMLDataRet("1",sXmlFull.toString())).getXML();
	   }
	   
	   public int checkIntegrityParameter() {
	          if (nvl(areaModel,"").trim().equals("") && nvl(crModel,"").trim().equals("") && nvl(cmModel,"").trim().equals("") && nvl(idCartella,"").trim().equals(""))
	    	      return _ERR_INTEGRITY_PAR____NOPARAMETER;
	       
	          if (!nvl(idCartella,"").trim().equals("")) 
	    	      return _INTEGRITY_PAR____OK_IDDOC; 		       		      
	          else {
	    	      if (nvl(areaModel,"").trim().equals(""))
	    		      return _ERR_INTEGRITY_PAR____NOAR;
	    		  
	    	      if (nvl(cmModel,"").trim().equals(""))  
	    		      return _ERR_INTEGRITY_PAR____NOCM;
	    	  
	    	      if (nvl(crModel,"").trim().equals(""))
	    		      return _INTEGRITY_PAR____OK_ARCM;
	    	      else {
	    	    	  //Sono nel caso ar/cm , controllo se passato nome cartella
	    	    	  if (nvl(nameFolder,"").trim().equals(""))
	    	    	     return  _ERR_INTEGRITY_PAR____NONAME;
	    	    	     
	    	    	  return _INTEGRITY_PAR____OK_ARCMCR;
	    	      }
	    		      
	       }		    
	   }	   
	   
	   public int checkIntegrityParameterRetrieve() {
	          if (nvl(areaModel,"").trim().equals("") && nvl(crModel,"").trim().equals("") && nvl(cmModel,"").trim().equals("") && nvl(idCartella,"").trim().equals(""))
	    	      return _ERR_INTEGRITY_PAR____NOPARAMETER;
	       
	          if (!nvl(idCartella,"").trim().equals("")) 
	    	      return _INTEGRITY_PAR____OK_IDDOC; 		       		      
	          else {
	    	      if (nvl(areaModel,"").trim().equals("") || nvl(cmModel,"").trim().equals("") || nvl(crModel,"").trim().equals(""))
	    		      return _ERR_INTEGRITY_PAR____NOPARAMETER;
	    	      else
	    		      return _INTEGRITY_PAR____OK_ARCMCR;	    	  	    	    	    		      
	          }		    
	   }	   
	   
	   public String generateErrorParameterMessage(int iCheckPar) {
	          StringBuffer ret = new StringBuffer("");
	     
	          switch (iCheckPar) {
			       	  case _ERR_INTEGRITY_PAR____NOPARAMETER:
			       		   ret.append("Mancano parametri chiave: ar,cm,cr o idFolder.");
			       		   break;
			       	  case _ERR_INTEGRITY_PAR____NOAR:
			       		   ret.append("Manca parametro chiave: ar.");
			       		   break;
			       	  case _ERR_INTEGRITY_PAR____NOCM:
			       		   ret.append("Manca parametro chiave: cm.");
			       		   break;
			       	  case _ERR_INTEGRITY_PAR____NONAME:
			       		   ret.append("Manca parametro chiave: nameFolder");
			       		   break;			       		   
	          }
	     
	          return ret.toString();
	   }	   
	
}
