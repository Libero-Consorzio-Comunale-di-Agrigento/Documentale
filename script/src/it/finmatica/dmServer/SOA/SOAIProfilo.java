package it.finmatica.dmServer.SOA;

import java.io.ByteArrayInputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import it.finmatica.dmServer.dbEngine.struct.dbTable.ACL;
import it.finmatica.dmServer.dbEngine.struct.dbTable.Metadato;
import it.finmatica.dmServer.dbEngine.struct.dbTable.MetadatoSimple;
import it.finmatica.dmServer.management.ICartella;
import it.finmatica.dmServer.management.Profilo;
import it.finmatica.dmServer.management.ProfiloVersion;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.util.XMLUtilDom4j;
import it.finmatica.dmServer.util.keyval;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import javax.servlet.http.*;

public class SOAIProfilo extends SOAIGenericService {	   	
	
	   private Vector<fileInfoBase64> sFileBase64 = new Vector<fileInfoBase64>();
	   
   	   //Costructor for WS
	   public SOAIProfilo(String us, String jndi,
	              String area,
	              String cm,
	              String cr,
	              String idDocument) throws Exception {
		   	  user=us;
		   	  		  
		   	  //dbOperation = SessioneDb.getInstance().createIDbOperationSQL(jndi,0);
		   	
		   	  
		   	   dbOperation = SessioneDb.getInstance().createIDbOperationSQL(jndi,0);
		   	/*  dbOperation = SessioneDb.getInstance().createIDbOperationSQL(Global.ALIAS_ORACLE,
	  				"jdbc:oracle:thin:@test-efesto:1521:ORCL",
	  				"GDM",
	  				"GDM");*/
		   	  
		   	  if (dbOperation==null)
		   		  throw new Exception("Attenzione! Problemi nel creare la connessione verso il database.\nVerificare che la connessione jndi="+jndi+" sia stata definita per il servizio");
		   	  
	   		  this.areaModel=area;
	  		  this.cmModel=cm;
	 		  this.crModel=cr;
	 		  this.idDocument=idDocument;
	   }
	   
	   //Costructor for SOA
	   public SOAIProfilo(HttpServletRequest request,
			              IDbOperationSQL dbOp,
			              String area,
			              String cm,
			              String cr,
			              String idDocument) {
		   
		      this.httpRequest=request;
		     
		      //if (request!=null)
		    	  this.user=/*"GDM";*/""+request.getSession().getAttribute("Utente");
		      //user="GDM";
		      
	  		  this.dbOperation=dbOp;
		      try {
		    	  /*SessioneDb.getInstance().addAlias(Global.ALIAS_ORACLE,
		    			  							Global.DRIVER_ORACLE);
		    	  this.dbOperation=SessioneDb.getInstance().createIDbOperationSQL("jdbc/gdm",0);*/
		    	  
		    	  /*this.dbOperation=SessioneDb.getInstance().createIDbOperationSQL(Global.ALIAS_ORACLE,
									                		  				"jdbc:oracle:thin:@10.98.0.11:1521:ORCL",
									                		  				"GDM",
									                		  				"GDM");*/
		      }
		      catch(Exception e) {
		    	  
		      }
		      
	   		  this.areaModel=area;
	  		  this.cmModel=cm;
	 		  this.crModel=cr;
	 		  this.idDocument=idDocument;
	   }	   
	   
  	   //Registra for WS
	   public String registra(MetadatoSimple mt[],it.finmatica.dmServer.dbEngine.struct.dbTable.ACL acls[]) throws Exception {
		   	  int iCheckIntPar=checkIntegrityParameter();
		      if (iCheckIntPar<0) {
		    	  closeDbOp();
		    	  return (new SOAXMLErrorRet(generateErrorParameterMessage(iCheckIntPar))).getXML();
		      }		   
		      
		   	  Vector<keyval> vDati= new Vector<keyval>();
		   	  Vector<ACL> vACL= new Vector<ACL>();
		   
		   	  if (mt!=null) {
			   	  for(int i=0;i<mt.length;i++ ) {
			   		  keyval kval = new keyval(mt[i].getCodice(),mt[i].getValore());
			   		  
			   		  vDati.add(kval);
			   	  }
		   	 }
		   	  
		   	  if (acls!=null) {
			   	  for(int i=0;i<acls.length;i++ ) {
			   		  ACL acl ;
			   		  
			   		  if (acls[i].getAccesso().equals("S")) 
			   			  acl = new ACL(acls[i].getUtenteGruppo(),acls[i].getTipoCompetenza(),_ADD_ACL_TAG);
			   		  else
			   			  acl = new ACL(acls[i].getUtenteGruppo(),acls[i].getTipoCompetenza(),_REM_ACL_TAG);
			   		  
			   		  vACL.add(acl);
			   	  }
			  }
		   	  
		   	  return registraInterna(  vDati, null, vACL, Global.STATO_BOZZA,
									   "N", "", 
									   "",  "",  iCheckIntPar,false);		
	   }
	   
	   //Registra for SOA
	   public String registra(String datiXML, String allegatiXML, String aclXML, String stato,
			   				  String escludiControlloCompetenze, String setFather, 
			   				  String addIntoFolder, String removeFromFolder) {		   		      		   
		      fileInfo fInfo=null;
		      Vector<ACL> acl=null;
		      
		      int iCheckIntPar=checkIntegrityParameter();
		      if (iCheckIntPar<0) {
		    	  closeDbOp();
		    	  return (new SOAXMLErrorRet(generateErrorParameterMessage(iCheckIntPar))).getXML();
		      }
		    	  		      
		      //Tolgo il CDATA dagli XML
		      if (datiXML.equals("%20")) datiXML="";
		      if (allegatiXML.equals("%20")) allegatiXML="";
		      if (aclXML.equals("%20")) aclXML="";
		      if (setFather.equals("%20")) setFather="";
		      if (stato.equals("%20")) stato="";
		      if (escludiControlloCompetenze.equals("%20")) escludiControlloCompetenze="";
		      if (addIntoFolder.equals("%20")) addIntoFolder="";
		      if (removeFromFolder.equals("%20")) removeFromFolder="";
		      
		      datiXML=datiXML.replaceAll("%20"," ");
		      allegatiXML=allegatiXML.replaceAll("%20"," ");
		      aclXML=aclXML.replaceAll("%20"," ");
		      addIntoFolder=addIntoFolder.replaceAll("%20"," ");
		      removeFromFolder=removeFromFolder.replaceAll("%20"," ");
		      
		      datiXML=removeCDATATAG(datiXML);
		      allegatiXML=removeCDATATAG(allegatiXML);
		      aclXML=removeCDATATAG(aclXML);
		      
		      //Gestione allegati
		      if (!allegatiXML.equals(""))	{
		    	  try {
			    	  fInfo=parseAllegatiXML(allegatiXML);				     
			      }
				  catch (Exception e) {
					  closeDbOp();
					  return (new SOAXMLErrorRet("Errore nel parsing dell'XML allegati: " +e.getMessage())).getXML();		    			                   
				  }  
		      }
		      
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
		      
		      Vector<keyval> vDati=null;
		      
		      if (!datiXML.equals(""))	{
		    	  try {
		    	  	vDati=parseDatiXML(datiXML);
			      }
				  catch (Exception e) {
					  closeDbOp();
					  return (new SOAXMLErrorRet("Parse dell'xml dei dati: " +e.getMessage())).getXML();		    			                   
				  }
		      }	
		      
		      try {
			      return registraInterna(  vDati, fInfo, acl, stato,
										   escludiControlloCompetenze,  setFather, 
										   addIntoFolder,  removeFromFolder,  iCheckIntPar,true);	
		      }
		      catch (Exception e) {
		    	  return (new SOAXMLErrorRet("Salvataggio excp: " +e.getMessage())).getXML();
		      }
	   }
	   
	   
	   public String registraInterna(Vector<keyval> vDati, fileInfo fInfo, Vector<ACL> acl, String stato,
									  String escludiControlloCompetenze, String setFather, 
									  String addIntoFolder, String removeFromFolder, int iCheckIntPar,
									  boolean bSOA) throws Exception {		   		      
					Profilo p;
					
	  
					//Inserimento/Aggiornamento dei valori
					try {
					   if (iCheckIntPar==_INTEGRITY_PAR____OK_IDDOC)
					 	  p = new Profilo(idDocument);
					   else if (iCheckIntPar==_INTEGRITY_PAR____OK_ARCM)
					 	  p = new Profilo(cmModel,areaModel);
					   else
					 	  p = new Profilo(cmModel,areaModel,crModel);		      
					
					   p.initVarEnv(user,user,dbOperation.getConn());
					   
						if (vDati!=null) {	  
					      for(int i=0;i<vDati.size();i++) {
							//  System.out.println("valoreCampo decode="+(new URLDecoder()).decode(vDati.get(i).getVal()));
					    	  p.settaValore(vDati.get(i).getKey(),(new URLDecoder()).decode(vDati.get(i).getVal(),"UTF-8"));
					      }				    	  
						}     					      
					}
					catch (Exception e) {
						  closeDbOp();
						  if (bSOA) 
							  return (new SOAXMLErrorRet("Caricamento dei valori: " +e.getMessage())).getXML();
						  else
							  throw new Exception("Errore in salvataggio: "+e.getMessage());
					}
					
					SOAIProfiloUploadDownload upDown = null;
					Vector<String> vFile = null;
					
					//Gestione Upload
					if (fInfo!=null) {
						  upDown = new SOAIProfiloUploadDownload(this.httpRequest,
												                 this.dbOperation,
												                 this.areaModel,
															     this.cmModel,
															     this.crModel,
															     this.idDocument);
						  
						  upDown.dontCloseDbOp();
						  
						  //Inserimento/Aggiornamento degli allegati da connessione DB			  			  
						  if (!nvl(fInfo.connattach,"").trim().equals("")) {
							  
							  try {
								  upDown.uploadDB(fInfo.connattach,fInfo.tableattachname,fInfo.columnattachname,
										          fInfo.columnattach,fInfo.whereattachcondition,fInfo.deletefiledb,p);  
							  }				  		      
							  catch (Exception e) { 
								  closeDbOp();
								  try {upDown.dbOperationConnAtt.rollback();}catch (Exception eClose) {}
								  try {upDown.dbOperationConnAtt.close();}catch (Exception eClose) {}
								  return (new SOAXMLErrorRet("Caricamento degli allegati da DB: " +e.getMessage())).getXML();		    			                   
							  }				  
						  }
						  
						  vFile = new Vector<String>();
						  //Inserimento/Aggiornamento degli allegati da FS	  			  
						  if (!nvl(fInfo.fileattach,"").trim().equals("")) {			     
							  //Gestione dei file allegati tramite PATH		      
						      if (!fInfo.fileattach.trim().equals("")) {
					    	     try {
					    	    	vFile=upDown.uploadFs(fInfo.fileattach,p);   
					     	 }
					 	     catch (Exception e) {
					 	    	closeDbOp();
					 	    	return (new SOAXMLErrorRet("Caricamento degli allegati da FS: " +e.getMessage())).getXML();				  
							     }
						      }
						  }
					}
					else if (sFileBase64.size()>0) {
						  for(int i=0;i<sFileBase64.size();i++) {
							  fileInfoBase64 fFile = sFileBase64.get(i);
							  ByteArrayInputStream bais;
							  
							  try {						  						  
								  it.finmatica.jfc.utility.Base64 b64 = new it.finmatica.jfc.utility.Base64();
								  
								  byte b[] = b64.f_decode(fFile.sBase64);
								  
								  bais = new ByteArrayInputStream(b);
							  }
					 	  catch (Exception e) {
					 		closeDbOp();
					 	    return (new SOAXMLErrorRet("Caricamento allegato - Errore in decode Base64 del file: " +fFile.fileName+"\nErrore: "+e.getMessage())).getXML();				  
							  }						  
							  
							  try {
							    p.setFileName(fFile.fileName,bais);
							  }
					 	  catch (Exception e) {
					 		closeDbOp();
					 	    return (new SOAXMLErrorRet("Caricamento allegato - Errore in setFilename: " +fFile.fileName+"\nErrore: "+e.getMessage())).getXML();				  
							  }
						  }
					}
					//FINE Gestione UPLOAD
					
					//Gestione ACL
					if (acl!=null) {
						  for(int i=0;i<acl.size();i++) {
							  ACL aclVar = acl.get(i);
							  
							  if (aclVar.versus.equals(_ADD_ACL_TAG)) 
								  p.addCompetenza(aclVar.user,aclVar.type);
							  else
								  p.removeCompetenza(aclVar.user,aclVar.type);
						  }
					}
					//FINE Gestione ACL
					
					if (!stato.equals("")) p.setStato(stato);
					
					if (!escludiControlloCompetenze.equals("")) p.escludiControlloCompetenze(true);
					
					if (!setFather.equals("")) p.settaPadre(setFather);
					
					try {
						  if (p.salva().booleanValue()) {
							  if (fInfo!=null) {
								  try { 
										upDown.deleteFile(fInfo.tableattachname,fInfo.whereattachcondition,vFile,fInfo.deletefilefs,fInfo.deletefiledb);
								  }
								  catch (Exception eDelete) {							  
									try {dbOperation.rollback();}catch (Exception eClose) {}
									closeDbOp();
									throw new Exception(eDelete);
								  }
								  
								  try {upDown.dbOperationConnAtt.commit();}catch (Exception eClose) {}
							  }	
							  
							  //Gestione inserimento del documento dalla cartella
							  if (!addIntoFolder.equals("")) {
								  try {
									ICartella Ic = new ICartella(addIntoFolder);
									
									Ic.initVarEnv(user,user,dbOperation.getConn());
									
									if (!escludiControlloCompetenze.equals("")) Ic.escludiControlloCompetenze(true);
									
									Ic.addInObject(p.getDocNumber(),"D");
																
									Ic.update();
								  }
								  catch (Exception eInsertIntoFolder) {
									try {dbOperation.rollback();}catch (Exception eClose) {}
									closeDbOp();
									throw new Exception(eInsertIntoFolder);						  
								  }
							  }
							  
							  //Gestione rimozione del documento dalla cartella
							  if (iCheckIntPar!=_INTEGRITY_PAR____OK_ARCM && !removeFromFolder.equals("")) {
								  try {
									ICartella Ic = new ICartella(removeFromFolder);
									
									Ic.initVarEnv(user,user,dbOperation.getConn());
									
									if (!escludiControlloCompetenze.equals("")) Ic.escludiControlloCompetenze(true);
									
									Ic.deleteInObject(p.getDocNumber(),"D");
																
									Ic.update();
								  }
								  catch (Exception eInsertIntoFolder) {
									try {dbOperation.rollback();}catch (Exception eClose) {}
									closeDbOp();
									throw new Exception(eInsertIntoFolder);						  
								  }						  
							  }
							  
							  dbOperation.commit();				
							  closeDbOp();
							  
							  Vector<Vector> vDoc = new Vector<Vector>();
							  Vector<keyval> v = new Vector<keyval>();
							  v.add(new keyval("IDDOCUMENT",p.getDocNumber()));
							 
							  vDoc.add(v);
							  
							  try {upDown.dbOperationConnAtt.close();}catch (Exception eClose) {} 
							  
							  if (bSOA)  
								  return (new SOAXMLDataRet("1",vDoc)).getXML();
							  else
								  return p.getDocNumber();
						  }
						  else {					
							  closeDbOp();
							  if (bSOA)  
								  return (new SOAXMLErrorRet("Salvataggio: " +p.getError())).getXML();
							  else
								  throw new Exception("Errore in salvataggio: "+p.getError());
						  }
					}
					catch (Exception e) {
						  //e.printStackTrace();
						  closeDbOp();
						  if (bSOA)  
							  return (new SOAXMLErrorRet("Salvataggio excp: " +e.getMessage())).getXML();
						  else
							  throw new Exception(e);
					}			  		      
	   }
	   
	   public void cancella() throws Exception {
		      if (nvl(idDocument,"").trim().equals("")) 
		    	  throw new Exception("E' necessario passare un idDocumento valido");
		   	  		      
		   	  registraInterna(  null, null, null, Global.STATO_CANCELLATO,
							   "N", "", 
							   "",  "",  _INTEGRITY_PAR____OK_IDDOC,false);	
	   }
	   
	   public String getListaValori() {
		      Profilo p;
		      
		      int iCheckIntPar=checkIntegrityParameter();
		      if (iCheckIntPar<0) {
		    	  closeDbOp();
		    	  return (new SOAXMLErrorRet(generateErrorParameterMessage(iCheckIntPar))).getXML();
		      }
		      
		      if (iCheckIntPar==_INTEGRITY_PAR____OK_ARCM) {
		    	  closeDbOp();
		    	  return (new SOAXMLErrorRet("E' necessario passare ar/cm/cr oppure idDocumento")).getXML();
		      }
		      
		      try {
			      if (iCheckIntPar==_INTEGRITY_PAR____OK_IDDOC)
			    	  p = new Profilo(idDocument);
			      else
			    	  p = new Profilo(cmModel,areaModel,crModel);		      
	
			      p.initVarEnv(user,user,dbOperation.getConn());			     			    			     			     
		      }
			  catch (Exception e) {
				  closeDbOp();
				  return (new SOAXMLErrorRet("Prima di accedere al documento: " +e.getMessage())).getXML();		    			                   
			  }	
			  
			  try {
				  if (p.accedi(Global.ACCESS_NO_ATTACH).booleanValue()) {
					  
					  Vector<Vector> vDoc = new Vector<Vector>();
						 
					  if (p.getlistaValori().size()>0) {	
						  for(int i=0;i<p.getlistaValori().size();i++)  {
							  Vector<keyval> v = new Vector<keyval>();	
		                      
							  keyval k = p.getlistaValori().get(i);
		                      k.setValue(_CDATAINIT+k.getVal().replaceAll("null","")+_CDATAEND);
		                       
							  k.setTipoDoc(SOAXMLDataRet._XML_TYPE_VALORI);
							  
		                      v.add(k);
		                      vDoc.add(v);
						  }						               		                 		                  		                
					  }
					  else
						  vDoc.add(new Vector<keyval>());
					  
					  closeDbOp();
					  return (new SOAXMLDataRet("1",vDoc)).getXML();					  
				  }
				  else {
					  closeDbOp();
					  return (new SOAXMLErrorRet("Accesso al documento: " +p.getError())).getXML();
				  }
			  }
			  catch (Exception e) {
				  closeDbOp();
				  return (new SOAXMLErrorRet("Accesso al documento: " +e.getMessage())).getXML();		    			                   
			  }			  
	   }
	   
	   //FOR SOA
	   public String versiona()  {
		      try {
		        return versionaInterna("SOA");
		      }
		      catch (Exception e ) {
		    	return (new SOAXMLErrorRet(e.getMessage())).getXML();  
		      }
	   }
	   
	   //FOR WS
	   public String versionaWS() throws Exception {
		      return versionaInterna("WS");
	   }	   

	   private String versionaInterna(String tipoRitorno) throws Exception {
		      long numVer;
		   	  if (nvl(idDocument,"").trim().equals(""))  {
		   		  if (tipoRitorno.equals("SOA"))
		   			  return (new SOAXMLErrorRet("E' necessario passare un idDocumento valido")).getXML();
		   		  else
		   			  throw new Exception("E' necessario passare un idDocumento valido");
		   	  }
		   	  
		   	  ProfiloVersion pv;
		   	  		   	
		   	  String sql="select nvl(versione,0) ";
		   	  sql+=" from ACTIVITY_LOG ";
		   	  sql+=" where ID_DOCUMENTO= "+idDocument;
		   	  sql+=" for update ";
		   	  sql+=" order by 1 desc ";
		   	  
		   	  try  {
		   	    dbOperation.setStatement(sql);
		   	    
		   	    dbOperation.execute();
		   	    
		   	    ResultSet rst=dbOperation.getRstSet();
		   	    
		   	    if (rst.next()) numVer=rst.getLong(1);
		   	    else numVer=0;
		   	    
		   	    numVer++;
		   	  }
		   	  catch (Exception e) {
		   		closeDbOpAndRollback();
		   		if (tipoRitorno.equals("SOA")) 
		   			return (new SOAXMLErrorRet("Errore in creazione numero versione documento: " +e.getMessage())).getXML();
		   		else
		   			throw new Exception("Errore in creazione numero versione documento: " +e.getMessage());
			  }		
		   	  		   	  
		   	  pv = new ProfiloVersion(idDocument,numVer);
		   	  pv.initVarEnv(user, "", dbOperation.getConn());
		   	  
		   	  try  { 
		   	    pv.versiona();
		   	  }
		   	  catch (Exception e) {
		   		closeDbOpAndRollback();
		   		if (tipoRitorno.equals("SOA")) 
		   			return (new SOAXMLErrorRet("Errore in versiona documento: " +e.getMessage())).getXML();
		   		else
		   		    throw new Exception("Errore in versiona documento: " +e.getMessage());
			  }	
			   	
		   	  try  {
		   		 dbOperation.commit();
		   	  }
		   	  catch (Exception e) {
		   		 try {this.dbOperation.rollback();}catch(Exception ei){}
		      }			
			  closeDbOp();
			  
			  if (tipoRitorno.equals("SOA")) {
				  Vector<Vector> vDoc = new Vector<Vector>();
				  Vector<keyval> v = new Vector<keyval>();
				  keyval k = new keyval();
				  k.setTipoDoc(SOAXMLDataRet._XML_TYPE_VALORI);
				  k.setKey("Numero Versione");
				  k.setValue(""+numVer);
				  v.add(k);
	              vDoc.add(v);
				  
			   	  return (new SOAXMLDataRet("1",vDoc)).getXML();
			  }
			  else
				  return ""+numVer;
	   }	   
	   
	   public String getListaFile() {	
		      Profilo p;
		      
		      int iCheckIntPar=checkIntegrityParameter();
		      if (iCheckIntPar<0) {
		    	  closeDbOp();
		    	  return (new SOAXMLErrorRet(generateErrorParameterMessage(iCheckIntPar))).getXML();
		      }
		      
		      if (iCheckIntPar==_INTEGRITY_PAR____OK_ARCM) {
		    	  closeDbOp();
		    	  return (new SOAXMLErrorRet("E' necessario passare ar/cm/cr oppure idDocumento")).getXML();
		      }
		      
		      try {
			      if (iCheckIntPar==_INTEGRITY_PAR____OK_IDDOC)
			    	  p = new Profilo(idDocument);
			      else
			    	  p = new Profilo(cmModel,areaModel,crModel);		      
	
			      p.initVarEnv(user,user,dbOperation.getConn());			     			    			     			     
		      }
			  catch (Exception e) {
				  closeDbOp();
				  return (new SOAXMLErrorRet("Prima di accedere al documento: " +e.getMessage())).getXML();		    			                   
			  }		      
			  
			  try {
				  if (p.accedi(Global.ACCESS_ATTACH).booleanValue()) {
					  String sListaFile=p.getlistaFile();
					
					  Vector<Vector> vDoc = new Vector<Vector>();
					  				  
					  
					  if (p.getListaFilesize()>0) {						  						  
						  StringTokenizer files = new StringTokenizer(sListaFile, "@");
		                    
		                  while (files.hasMoreTokens()) {
		                        String fileName = files.nextToken();
		                        keyval kVal = new keyval(p.getIdFile(fileName),fileName);
		                        
		                        kVal.setTipoDoc(SOAXMLDataRet._XML_TYPE_OGGETTIFILE);
		                        
		                        Vector<keyval> v = new Vector<keyval>();	
		                        
		                        v.add(kVal);
		                        
		                        vDoc.add(v);
		                  }		                 		                 		                  		                 
					  }
					  else
						  vDoc.add(new Vector<keyval>());
					  
					  closeDbOp();
					  return (new SOAXMLDataRet("1",vDoc)).getXML();
					  
				  }
				  else {
					  closeDbOp();
					  return (new SOAXMLErrorRet("Accesso al documento: " +p.getError())).getXML();
				  }
			  }
			  catch (Exception e) {
				  closeDbOp();
				  return (new SOAXMLErrorRet("Accesso al documento: " +e.getMessage())).getXML();		    			                   
			  }		 			  
	   }	   	      		   
	   
	   private fileInfo parseAllegatiXML(String allegati) throws Exception {
			   Document dXMLDati = null;
		       XMLUtilDom4j xmlUt = null;
		       
		       try {
		    	 dXMLDati = DocumentHelper.parseText(allegati);
		       }
			   catch (Exception e) {
			     throw new Exception("parseDatiXML - Errore in parse XML Allegati ("+allegati+")\n"+e.getMessage()); 
			   }
			   
			   if (dXMLDati == null)		      
			       throw new Exception("parseDatiXML - Document XML Allegati ("+allegati+") nullo!");	
			   
			   
			   xmlUt = new XMLUtilDom4j(dXMLDati);
			   
			   if (xmlUt.leggiElementoXML(dXMLDati.getRootElement(),"FILE")==null) 
				   return (new fileInfo(xmlUt.leggiValoreXML("connattach"),
						                xmlUt.leggiValoreXML("tableattachname"),
						                xmlUt.leggiValoreXML("columnattachname"),
						                xmlUt.leggiValoreXML("columnattach"),
						                xmlUt.leggiValoreXML("whereattachcondition"),
						                xmlUt.leggiValoreXML("fileattach"),
						                xmlUt.leggiValoreXML("deletefilefs"),
						                xmlUt.leggiValoreXML("deletefiledb")));
			  
			   //Tratto i Base64
			   Vector<Element> vEl = xmlUt.leggiChildElementXML(dXMLDati.getRootElement());			  			  
			  
			   for(int i=0;i<vEl.size();i++) sFileBase64.add(new fileInfoBase64(vEl.get(i).attribute("name").getText(),vEl.get(i).getText()));
			   			   
			   return null;				   			   
	   }	     
	   


}	


class fileInfo {
	  public String connattach,tableattachname,columnattachname;
	  public String columnattach,whereattachcondition,fileattach;
	  public String deletefilefs,deletefiledb;
	  
	  public fileInfo(String p_connattach, String p_tableattachname, String p_columnattachname,
			          String p_columnattach, String p_whereattachcondition, String p_fileattach,
			          String p_deletefilefs, String p_deletefiledb) {
		  connattach=p_connattach;
		  tableattachname=p_tableattachname;
		  columnattachname=p_columnattachname;
		  columnattach=p_columnattach;
		  whereattachcondition=p_whereattachcondition;
		  fileattach=p_fileattach;
		  deletefilefs=p_deletefilefs;
		  deletefiledb=p_deletefiledb;
	  }
}

class fileInfoBase64 {
	  public String fileName,sBase64;
	  
	  public fileInfoBase64(String p_fileName, String p_Base64) {
		  fileName=p_fileName;
		  sBase64=p_Base64;		
	  }
}


