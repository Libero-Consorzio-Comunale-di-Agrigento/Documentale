package it.finmatica.dmServer.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import it.finmatica.dmServer.util.Global;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.jfc.io.LetturaScritturaFileFS;

public class FileFsFromDb {
	   private String urlStringOracle="",area,modello,categoriaModello,pathFileTomcat,pathFileOracle,mapPathFileOracle, categorieModelliString;
	   private String usrOracle, passwdOracle;
	   private String assegna777Cartelle="N", assegna777File="N";
	   private String areaFiglio,modelloFiglio;
	   private IDbOperationSQL dbOp = null;
	   private String idOggettoFile = null;
	   private String fileLog = null;
	   private java.sql.Timestamp now = null;	  
	   private String acronimo;	   
	   private long   limit=0;	   
	   private String reverse="N";
	   private Vector<OggettoFileStruct> elementList = new Vector<OggettoFileStruct>();
	   private HashMapSet hmsLegamiModelli = new HashMapSet();
	   private HashMapLinkedSet hmsChildElementList = new HashMapLinkedSet();
	   private Properties propAreeFigle = new Properties();	   
	   
	   private String listaModelliEsclusi = "(('SEGRETERIA.PROTOCOLLO','M_PROTOCOLLO'),('SEGRETERIA.PROTOCOLLO','M_PROTOCOLLO_INTEROPERABILITA'),('SEGRETERIA','M_ALLEGATO_PROTOCOLLO'),('SEGRETERIA','M_ALLEGATO_PROTOCOLLO'),('SEGRETERIA.ATTI','DETERMINA'),('SEGRETERIA.ATTI','DELIBERA'))";
	   
	   private String anno,tiporegistro,dataprotocolloDa,dataprotocolloA,dataAdozioneDa,dataAdozioneA, esportaSoloFigli, idDocumentoDa, idDocumentoA;
	   private String dataRevisioneDa, dataRevisioneA;
	   
	   private String numeroregistroDa,numeroregistroA,conservazione,propagaFigli="Y";
	   
	   private String dataCreazioneDa =null,dataCreazioneA=null;
	   
	   private String campoDinamico=null,tipocampoDinamico=null,parDaCampoDinamico=null,parACampoDinamico=null;
	   private Vector<ChiaveCampi> vCampiChiavi = new Vector<ChiaveCampi>();
	   
	   private String typeProtoIntern=null;
	   
	   private static final String docFilesDir = "DOC_FILES";
	   private static final String nameTempOracleDir = "TEMP_FILE";
	   
	   private static final String _ANNOPAR           = "-anno";
	   private static final String _TIPOREGISTROPAR   = "-tiporegistro";
	   private static final String _DATAPROTOCOLLOPAR = "-data";
	   private static final String _DATAADOZIONEPAR   = "-dataadozione";
	   private static final String _DATAREVISIONE     = "-datarevisione";
	   private static final String _NUMEROREGISTROPAR = "-numeroregistro";
	   private static final String _CONSERVAZIONEPAR  = "-conservazione";
	   private static final String _PROPAGAFIGLIPAR   = "-propagaFigli";
	   private static final String _ESPORTASOLOFIGLIPAR  = "-esportaSoloFigli";
	   private static final String _IDDOCPAR          = "-idDocumento";
	   private static final String _CAMPODINAMICPAR   = "-campodinamico";
	   private static final String _DATACREAZ         = "-datacreaz";
	   private static final String _ASSEGNA777CART    = "-777tofolder";
	   private static final String _ASSEGNA777FILE    = "-777tofile";
	   
	   //Per i protocolli.....mia interna trasco, non documentare!!!
	   private static final String _PROTOINTER  = "-protointern";
	   
	   private static final String _PROTOINTER_PRINCIPALI  = "PRINCIPALI";
	   private static final String _PROTOINTER_SECONDARI   = "SECONDARI";
	   
	   public static void main(String[] args) {			
		   
		  // chmodfolder(args[0]);
			   FileFsFromDb fileFs = new FileFsFromDb();
			   
			   if (args.length<9) {
				   	  fileFs.showHelp(); 
			   		  System.exit(-1);
			   		  return;
			   }
			   
			   try {
			   		 fileFs.loadProperties(args[0]);
			   }
			   catch (Exception e) {
					 System.out.println(e.getMessage());
					 fileFs.showHelp(); 
			   		 System.exit(-1);
			   		 return;
			   }
			   			   			   			   
			   fileFs.area=args[1];			
			  //System.out.println("Area: "+fileFs.area);
			   if (args[2].indexOf("CAT:")!=-1)	
			   	  fileFs.categoriaModello=args[2].substring(args[2].indexOf("CAT:")+4);
			   else
				  fileFs.modello=args[2];
			  // System.out.println("modello: "+fileFs.modello);
			   fileFs.pathFileTomcat=args[3];
			  // System.out.println("pathFileTomcat: "+fileFs.pathFileTomcat);
			   fileFs.pathFileOracle=args[4];
			 //  System.out.println("pathFileOracle: "+fileFs.pathFileOracle);
			   fileFs.mapPathFileOracle=args[5];
			 //  System.out.println("mapPathFileOracle: "+fileFs.mapPathFileOracle);
			   fileFs.fileLog=args[6];
			 //  System.out.println("fileLog: "+fileFs.fileLog);
			 //  System.out.println("limit"+args[7]);
			   fileFs.limit=Long.parseLong(args[7]);	
			   fileFs.reverse=args[8];
			   
			   //Tratto i par. opzionali
			   int iIndex=9;
			   do {
				 if (args.length>= (iIndex+1)) {
					 String sPar=args[iIndex];
					 String sValore;
					 
					 try {
						 try {
						     sValore = args[iIndex+1];
						 }
						 catch (Exception e) {
							 throw new Exception("Per il parametro "+sPar+" non è stato specificato il relativo valore");
						 }
						 					 
						 fileFs.trattaParametroOpzionale(sPar.trim(),sValore);
					 }
					 catch (Exception e) {
						 System.out.println("Attenzione! Errore nel trattamento di un parametro opzionale.\nErrore: "+e.getMessage());
						 fileFs.showHelp(); 
				   		 System.exit(-1);
				   		 return;
					 }
				 }
				 else iIndex=1000;
				 
				 iIndex+=2;
					 
			   } while (iIndex<1000);
			   
		   	   java.util.Calendar cal = Calendar.getInstance();
		   	   fileFs.now = new java.sql.Timestamp(cal.getTimeInMillis());
	     	   
	     	   fileFs.scriviMessaggio("\n------------------- Inizio <"+fileFs.now+"> -------------------\n\n");	     	  		 
			   
			   try {
				   	  Class.forName("oracle.jdbc.driver.OracleDriver");
					  Connection conn=DriverManager.getConnection(fileFs.urlStringOracle,fileFs.usrOracle,fileFs.passwdOracle);
					  conn.setAutoCommit(false);
			
				    	 
					  SessioneDb.getInstance().addAlias("oracle.","oracle.jdbc.driver.OracleDriver");
					  
					  fileFs.dbOp = SessioneDb.getInstance().createIDbOperationSQL("oracle.",
							  														fileFs.urlStringOracle,
							  														fileFs.usrOracle,
							  														fileFs.passwdOracle);
		   	   }
		   	   catch (Exception e) {
		   		   System.out.println("it.finmatica.trasco.FileFs - Errore in creazione connessione verso "+fileFs.urlStringOracle);
		   		   e.printStackTrace();
		   		   System.exit(-1);
		   		   return;
		   	   }
		   
		   	   
	   	   
		   	   try {		   	
		   		 	   
			   	 Vector vListaModelli = fileFs.fillModelli();
			   	 
			   	 for (int i=0;i<vListaModelli.size();i++) {
			   		 String sArCm=""+vListaModelli.get(i);
			   		 
			   		fileFs.area=sArCm.substring(0,sArCm.indexOf(","));
			   		fileFs.modello=sArCm.substring(sArCm.indexOf(",") + 1);
			   		 
			   		 if (fileFs.reverse.equals("N"))
			   			 fileFs.makeTransport();
			   		 else
			   			fileFs.makeTransportReverse();
			   		 
			   	 }
			   }
			   catch (Exception e) {
		   		  System.out.println("it.finmatica.trasco.FileFsFromDb - Errore: in makeTransport");
		   		  e.printStackTrace();
		   		  try {fileFs.dbOp.rollback();} catch (Exception ei) {}
		   		  try {fileFs.dbOp.close();} catch (Exception ei) {}
		   		  System.exit(-1);
		   		  return;
		   	   }			  
			   
			   fileFs.scriviMessaggio("\n------------------- FINITO CON SUCCESSO -------------------\n\n");
					   
			   try {fileFs.dbOp.commit();} catch (Exception ei) {}
		   	   try {fileFs.dbOp.close();} catch (Exception e) {}
	   	   
	   }	   	   
	   
	   public FileFsFromDb() {	
		   
	   }
	   
	   public FileFsFromDb(String idOggettoFile, IDbOperationSQL dbOperation) {
		   	  dbOp = dbOperation;		   	  
		   	  this.idOggettoFile=idOggettoFile;		   	 
	   }
	   
	   public FileFsFromDb(String idOggettoFile, IDbOperationSQL dbOperation, String mapPathFileOracle) {		
		   	  dbOp = dbOperation;		   	  
		   	  this.idOggettoFile=idOggettoFile;
		   	  this.mapPathFileOracle=mapPathFileOracle;
	   }
	   
	   public void makeTransportReverse() throws Exception {			 
			   if (areaFiglio==null)
		   		   getInitAreaSQL(area);
		   	   else
		   		   getInitAreaSQL(areaFiglio);	   		   	  
		   	  
		   	  fillElementList(true);
		   	  
		   	  //*************************************1. (PASSO A) 		   
			  scriviMessaggio("\n------------------- Inizio per (area,modello)=("+area+","+modello+") <"+now+"> TROVATI "+elementList.size()+" elementi (tra oggetti file e ogg. file log) nell'area specificata. Impostato limite max di ("+limit+")-------------------\n\n");
			  
			  try  {
		        makeReverse();  	        	 	        	 
	          }
			  catch (Exception e) {							 
				scriviMessaggio("Errore in esecuzione procedura di trascodifica.\nErrore: "+e.getMessage());
			   	throw new Exception("Errore in esecuzione procedura di trascodifica.\nErrore: "+e.getMessage());
			  }
			  			   
			  java.util.Calendar cal = Calendar.getInstance();
			  now = new java.sql.Timestamp(cal.getTimeInMillis());
			  scriviMessaggio("\n------------------- Fine per (area,modello)=("+area+","+modello+") <"+now+"> -------------------\n\n");
	   }
	   
	   public void makeTransport() throws Exception {	
		   	   if (areaFiglio==null)
		   		   getInitAreaSQL(area);
		   	   else
		   		   getInitAreaSQL(areaFiglio);
		   	   
		   	   if (typeProtoIntern==null)
		   		   fillElementList();
		   	   else
		   		   fillElementListProto();
			   	   
			   //*************************************2. (PASSO A) OPERAZIONI SU FILE SYSTEM TRANSAZIONI A SE STANTE RISPETTO AL DB			   
			   scriviMessaggio("\n------------------- Inizio per (area,modello)=("+area+","+modello+") <"+now+"> TROVATI "+elementList.size()+" elementi (tra oggetti file e ogg. file log) nell'area specificata. Impostato limite max di ("+limit+")-------------------\n\n");

			   try  {
	        	 make();  
	        	 
	        	 try  {dropTempOracleDir(nameTempOracleDir);}catch (Exception ei) {}
	           }
			   catch (Exception e) {		
				 try  {dropTempOracleDir(nameTempOracleDir);}catch (Exception ei) {}
				 e.printStackTrace();
				 scriviMessaggio("Errore in esecuzione procedura di trascodifica.\nErrore: "+e.getMessage());
			   	 throw new Exception("Errore in esecuzione procedura di trascodifica.\nErrore: "+e.getMessage());
			   }
			   
			   java.util.Calendar cal = Calendar.getInstance();
			   now = new java.sql.Timestamp(cal.getTimeInMillis());
			   scriviMessaggio("\n------------------- Fine per (area,modello)=("+area+","+modello+") <"+now+"> -------------------\n\n");
			   			   			   	   
	   }
	   
	   private void make() throws Exception {		
		   	   if (areaFiglio!=null) {
		   		   area=areaFiglio;
		   		   modello=modelloFiglio;
		   	   }
		   
		   	   
		   	   /*if (typeProtoIntern==null)*/ //DA COMMENTARE QUANDO SI PROVA SUL SERVER EFFETTIVO!!! MA PRIMA METTI IL FORCE FILE A 1
		   	  // System.out.println("AAAA");
		   	   manageArea(area,acronimo);
		   	   
		   	   core(elementList,area,acronimo,true,"",false);		   		   
	   }
	   
	   private void makeReverse() throws Exception {	
			   if (areaFiglio!=null) {
		   		   area=areaFiglio;
		   		   modello=modelloFiglio;
		   	   }
			   
		       core(elementList,area,acronimo,true,"",true);
	   }
	   
	   /*private void makeReverse() throws Exception {		
			   long thisLimit=0;		   
			   
		   	   if (!(new File(mapPathFileOracle+"/"+acronimo)).exists())
		   		   throw new Exception("Non esiste la directory iniziale: "+mapPathFileOracle+"/"+acronimo);
		   	   
		   	   String modello="";
		   	   String idDoc="", idObjFile, idLog="", acrModello="";
		   	   String sDir="", sDirLog="";
		   	   for(int i=0;i<elementList.size();i++) {		
		   		   if (!modello.equals(elementList.get(i).codiceModello)) 
		   			   scriviMessaggio("\t-(Area,Modello): ("+area+","+elementList.get(i).codiceModello+")");
		   		   
		   		   if (!idDoc.equals(elementList.get(i).idDocumento))  {
		   			   java.util.Calendar cal = Calendar.getInstance();
		   			   now = new java.sql.Timestamp(cal.getTimeInMillis());
		   			   scriviMessaggio("\t\t-idDocumento: "+elementList.get(i).idDocumento+" - Alle: "+now);
 		   		   	 		   		   		 
		   		   	   if (thisLimit==limit && limit!=0) {
		   		   		   scriviMessaggio("Raggiunto limite massimo di "+limit+" documenti trattati. Fine procedura");
		   		   		   return;
		   		   	   }
		   		   	 
		   		   	   sDir=mapPathFileOracle+"/"+acronimo+"/"+elementList.get(i).acronimoModello+"/"+getDirMille(elementList.get(i).idDocumento)+"/"+elementList.get(i).idDocumento;

		   		   	   if (!(new File(sDir)).exists()) 
		   		   		   throw new Exception("Errore in ricerca directory: "+sDir);		   		   			 		   		   		
	   		   		
		   		   	   thisLimit++;
		   		   }
		   		   
		   		   if (!elementList.get(i).idLog.equals("-1"))  {	
	   		   		 	sDirLog=mapPathFileOracle+"/"+acronimo+"/"+elementList.get(i).acronimoModello+"/"+getDirMille(elementList.get(i).idDocumento)+"/"+elementList.get(i).idDocumento+"/LOG_"+elementList.get(i).idLog;
	   		   		 	//Provo a creare la dir del log....tanto se essite la sovrascrive
	   		   		 	if (!(new File(sDirLog)).exists()) 
	   		   		 		throw new Exception("Errore in ricerca directory: "+sDirLog);		   		   			 		   		   			   		   			 
		   		   }	
		   		   
		   		   modello   = elementList.get(i).codiceModello;
	   		   	   idDoc 	   = elementList.get(i).idDocumento;
	   		   	   idObjFile = elementList.get(i).idObjFile;
	   		   	   idLog	   = elementList.get(i).idLog; 		   		     
	   		   	   acrModello=elementList.get(i).acronimoModello;
	   		     
	   		   	   String dirMille = ""+getDirMille(elementList.get(i).idDocumento);
	   		   	 
	   		   	   String msg = "\t\t\t-idOggettoFile: "+idObjFile;
	   		   	   if (!idLog.equals("-1")) msg+= ", idLog: "+idLog;
	   		   	   scriviMessaggio(msg);
	   		   	   
	   		   	   String pathLog="";
	   		   	   if (!idLog.equals("-1")) pathLog="/LOG_"+idLog;
	   		   	   
	   		   	   String sDirectoryFile;			   		   	
	   		   	   sDirectoryFile=mapPathFileOracle+"/"+acronimo+"/"+acrModello+"/"+dirMille+"/"+idDoc+pathLog;
	   		   	   
	   		   	   InputStream is=null;
	   		   	   try {
	   		   		   LetturaScritturaFileFS f = new LetturaScritturaFileFS(sDirectoryFile+"/"+idObjFile);
	   		   		   is=f.leggiFile();          		 	   		   		   
	   		   	   }
	   		   	   catch (Exception e) {
	   		   		   throw new Exception("Errore lettura file "+sDirectoryFile+"/"+idObjFile+" da FS\nErrore: "+e.getMessage());
	   		   	   }
	   		   	   
	   		   	   try {
	   		   		   updObjFile(idDoc,idObjFile,idLog,true,is);	   		   		   
	   		   	   }
	   		   	   catch (Exception e) {
	   		   		   try { is.close();}catch (Exception ei) {}
	   		   		   throw new Exception("Errore scrittura file "+sDirectoryFile+"/"+idObjFile+" da FS su colonna Blob\nErrore: "+e.getMessage());
	   		   	   }
	   		   	   
	   		   	   try { is.close();}catch (Exception e) {}
	   		   	   
	   		   	 
		   	   }
	   }*/
	   
	   private void core(Vector<OggettoFileStruct> element,String ar, String acr, boolean bVerificaFigli, String prefixMessaggio, boolean bReverse) throws Exception {
		   	   long thisLimit=0;
		   	   
		   	   if  (bReverse && (!(new File(mapPathFileOracle+"/"+acr)).exists()))
		   		   throw new Exception("Non esiste la directory: "+mapPathFileOracle+"/"+acr);
		   	   
			   String modello="";
		   	   String idDoc="", idObjFile, idLog="", acrModello="", idObjFileExt="";
		   	   String sDir="", sDirLog="";
		   	   boolean bPassaFigli=false;
		   	   for(int i=0;i<element.size() + 1 ;i++) {
		   		   
		   		     //************************************************TEST SUI FIGLI
		   		   	 if (i < element.size() ) 
		   		   		 bPassaFigli=(!idDoc.equals(element.get(i).idDocumento));
		   		   	 else
		   		   		 bPassaFigli=true;
		   		   	 
		   		   	 //TODO DA CANCELLARE
	   		    	 scriviMessaggio("******** INIZIO **********");		   		   	 
		   		   
		   		     if (bPassaFigli && !idDoc.equals("") && bVerificaFigli)  {
		   		    	 	//TODO DA CANCELLARE
		   		    	 scriviMessaggio("******** CASO FIGLI **********");	
		   		    	 //Sto facendo il cambio da un idDoc ad un altro della lista.
		   		   		 //Verifico se su quello già trattato ci sono figli da trattare
	   		   			 Iterator itrChild = hmsChildElementList.getHashSet(idDoc);
	   		   			 
	   		   			 if (itrChild!=null && itrChild.hasNext()) {
	   		   				 scriviMessaggio(prefixMessaggio+"\t\t-*****************Esistono documenti figli per idDoc="+idDoc+". Li tratto*****************");
	   		   				 
	   		   				 Properties propElementListAreaDivide = new Properties();
	   		   				 while (itrChild.hasNext()) {
	   		   					 OggettoFileStruct objFiStr = (OggettoFileStruct)itrChild.next();
	   		   					 
	   		   					 //Prima controllo l'area, se non è ancora stata importata
	   		   					 //System.out.println(objFiStr.area+"@"+objFiStr.acronimoArea);
	   		   					 if (propAreeFigle.containsKey(objFiStr.area+"@"+objFiStr.acronimoArea)) {
		   		   					 if (propAreeFigle.get(objFiStr.area+"@"+objFiStr.acronimoArea).equals("N")) {
		   		   						 manageArea(objFiStr.area,objFiStr.acronimoArea);
		   		   						 propAreeFigle.put(objFiStr.area+"@"+objFiStr.acronimoArea,"S");
		   		   					 }
	   		   					 }
	   		   					 
	   		   					 Vector<OggettoFileStruct> childElement = (Vector<OggettoFileStruct>)propElementListAreaDivide.get(objFiStr.area+"@"+objFiStr.acronimoArea);
	   		   					 if (childElement==null) childElement = new Vector<OggettoFileStruct>();
	   		   					 
	   		   					 childElement.add(objFiStr);
	   		   					 propElementListAreaDivide.put(objFiStr.area+"@"+objFiStr.acronimoArea,childElement);
	   		   				 }
	   		   				 
	   		   				 for (Iterator it = propElementListAreaDivide.keySet().iterator(); it.hasNext();) {
	   		   					 String key = ""+it.next();
	   		   					 Vector<OggettoFileStruct> childElement = (Vector<OggettoFileStruct>)(propElementListAreaDivide.get(key));
	   		   					 
	   		   					 String areaFiglio=key.substring(0,key.indexOf("@") -1);
	   		   					 String acrAreaFiglio=key.substring(key.indexOf("@") +1);
	   		   					 
	   		   					 core(childElement,areaFiglio,acrAreaFiglio,false,"\t\t",bReverse);	   		   					 
	   		   				 }	   		   				
	   		   				 
	   		   				 scriviMessaggio(prefixMessaggio+"\t\t-*****************Fine export figli per idDoc="+idDoc+".*****************");
	   		   			 }		   		   		 
		   		     }
		   		   	 
		   		   	 if (i == element.size() ) continue; 
		   		     //************************************************FINE CICLO SUI FIGLI
		   		   
		   		     if (!modello.equals(element.get(i).codiceModello)) 
		   		    	scriviMessaggio(prefixMessaggio+"\t-(Area,Modello): ("+ar+","+element.get(i).codiceModello+")");
		   		     		   		   
		   		     boolean bEsisteCartellaReverse=true;
		   		   	 if (!idDoc.equals(element.get(i).idDocumento))  {
		   		   		 java.util.Calendar cal = Calendar.getInstance();
		   		   		 now = new java.sql.Timestamp(cal.getTimeInMillis());
		   		   		 scriviMessaggio(prefixMessaggio+"\t\t-idDocumento: "+element.get(i).idDocumento+" - Alle: "+now);
		 		   		 
		   		   		 //Il limit vale solo nel ciclo principale dei padri
			   		   	 if (thisLimit==limit && limit!=0 && bVerificaFigli) {
			   		    	scriviMessaggio(prefixMessaggio+"Raggiunto limite massimo di "+limit+" documenti trattati. Fine procedura");
			   		    	return;
			   		     }
			   		   	 
			   		   	 String sDirMille;
			   		   	 
			   		   	 sDir=mapPathFileOracle+"/"+acr+"/"+element.get(i).acronimoModello+"/"+getDirMille(element.get(i).idDocumento)+"/"+element.get(i).idDocumento;
			   		   	 sDirMille=mapPathFileOracle+"/"+acr+"/"+element.get(i).acronimoModello+"/"+getDirMille(element.get(i).idDocumento);
			   		  
			   		   	 if (bReverse) {
			   		   		 if (element.get(i).idObjFile!=null) {
			   		   			 if (!(new File(sDir)).exists()) {
			   		   				 //throw new Exception("Errore in ricerca directory: "+sDir);
			   		   				 bEsisteCartellaReverse=false;
			   		   			 }
			   		   		 }
			   		   	 }
			   		   	 else {
			   		   		 if (element.get(i).idObjFile!=null) {
				   		   		 if (!mkDirs(sDir,true,sDirMille)) 
				   		   			 throw new Exception("Errore in creazione directory: "+sDir);		  
			   		   		 }
			   		   	 }
			   		   	 
			   		   	 if (!element.get(i).padreGiaTrascodificato) thisLimit++;
		   		   	 }
		   		   	 
		   		   	 if (!element.get(i).idLog.equals("-1"))  {	
		   		   		 sDirLog=mapPathFileOracle+"/"+acr+"/"+element.get(i).acronimoModello+"/"+getDirMille(element.get(i).idDocumento)+"/"+element.get(i).idDocumento+"/LOG_"+element.get(i).idLog;
		   		   		 //Provo a creare la dir del log....tanto se essite la sovrascrive
		   		   		 
			   		   	if (bReverse) {
			   		   		if (element.get(i).idObjFile!=null) {
			   		   			if (!(new File(sDirLog)).exists()) {
			   		   				//throw new Exception("Errore in ricerca directory: "+sDirLog);
			   		   				bEsisteCartellaReverse=false;
			   		   			}
			   		   		}
			   		   	} else {
			   		   		if (element.get(i).idObjFile!=null) {
			   		   			if (!mkDirs(sDirLog,false,"")) 
			   		   				throw new Exception("Errore in creazione directory: "+sDirLog);	
			   		   		}
			   		   	}
		   		   			 
		   		   	 }		   		   	 
		   		   	 
		   		   	 modello     = element.get(i).codiceModello;
		   		   	 idDoc 	     = element.get(i).idDocumento;
		   		     idObjFile   = element.get(i).idObjFile;
		   		     idObjFileExt = element.get(i).idObjFileExtern;
		   		     idLog	     = element.get(i).idLog; 		   		     
		   		     acrModello  = element.get(i).acronimoModello;
		   		     
		   		     String dirMille = ""+getDirMille(element.get(i).idDocumento);
		   		     
		   		     //Se ancora lo devo trattare......lo tratto
		   		     String msg = prefixMessaggio+"\t\t\t-idOggettoFile: "+idObjFile;
		   		     if (!idLog.equals("-1")) msg+= ", idLog: "+idLog;
		   		     
		   		     if (!element.get(i).padreGiaTrascodificato) {			   		     
			   		     scriviMessaggio(msg);		   		     
			   		     
			   		     StringBuffer stmCreateDir = null;
			   		     String sDirectoryFile;			
			   		     String pathLog="";
		   		    	 if (!idLog.equals("-1")) pathLog="/LOG_"+idLog;
		   		    	 sDirectoryFile=mapPathFileOracle+"/"+acr+"/"+acrModello+"/"+dirMille+"/"+idDoc+pathLog;
		   		    	
			   		     if (!bReverse) {
			   		    	
			   		    	try {				   		    		
			   		    		scriviMessaggio("******** CREO DIRECTORY **********");		   	
			   		    		stmCreateDir = new StringBuffer("CREATE OR REPLACE DIRECTORY ");
			   		    		stmCreateDir.append(nameTempOracleDir+" AS '"+pathFileOracle+"/"+acr+"/"+acrModello+"/"+dirMille+"/"+idDoc+pathLog+"'");			   		    					   		    	
			   		    		
						        dbOp.setStatement(stmCreateDir.toString());
								   
								dbOp.execute();		 
			   		    	
				   		     }
				   		     catch (Exception e) {	
				   		    	 	e.printStackTrace();
				   		    	 	throw new Exception("Errore creazione directory oracle. SQL= "+stmCreateDir.toString()+"\nErrore: "+e.getMessage());
				   		     }			   		    			   		     
			   		     
				   		     try {
				   		       //dbOp.setStatement("BEGIN gdm_utility.extract_file("+idObjFile+","+idLog+"); END;");
				   		       if (idObjFile!=null) {
				   		    	   //TODO DA CANCELLARE
				   		    		scriviMessaggio("******** PRENDO FILE **********");	
					   		       if (!idLog.equals("-1")) 
					   		    	  dbOp.setStatement("SELECT TESTOOCR FROM oggetti_file_log where id_oggetto_file="+idObjFile+" and id_log="+idLog);  		   		       
					   		       else 
					   		    	  dbOp.setStatement(getSQLForBlob(idObjFile,idObjFileExt));				   		    	 		   		    	  				   		         				   		       
					   		    	
					   		       dbOp.execute();		   		       		   		      	   	     		   	 
						   	     
					   		       ResultSet rst = dbOp.getRstSet();
						   	     
					   		       if (rst.next()) {
					   		    	   
					   		    	   InputStream is=null;
				                  	   try {			                    	  
				                  	 	 is=dbOp.readBlob(1);
				                  	   }
				                  	   catch (NullPointerException e) {  
				                  		  //DONTCARE
				                  	   }   	   
				                  	   if (is!=null) {
				                  		 //TODO DA CANCELLARE
						   		    	 scriviMessaggio("******** SCRIVO FILE SU FS **********");
				                  		 LetturaScritturaFileFS f = new LetturaScritturaFileFS(sDirectoryFile+"/"+idObjFile);
				                  		 f.scriviFile(is);
				                  		 
				                  		 is.close();
				                  		 
				                  		 chmod(sDirectoryFile+"/"+idObjFile);
				                  	   }
					   		    	   
					   		       }
					   		       else
					   		    	 throw new Exception("L'oggetto file "+idObjFile+" non è più presente sulla tabella....verificare!!");
				   		         }  
				   		     }		   	 
				   		     catch (Exception e) {		   		    
				   		    	 e.printStackTrace();
			   		    	 	throw new Exception("Errore passaggio file "+idObjFile+" da blob a FS\nErrore: "+e.getMessage());
				   		     }
				   		     
				   
				   		     if (idOggettoFile==null) {
				   		    	 if (idObjFile!=null) {
					   		    	 scriviMessaggio("******** AGGIORNO DB **********");
						   		     updObjFile(idDoc,idObjFile,idLog,acr);
						   		     scriviMessaggio("******** FINE AGGIORNO DB **********");
				   		    	 }
				   		     }
			   		     }
			   		     //REVERSE
			   		     else {
			   		    	if (idObjFile!=null && bEsisteCartellaReverse) {
				   		    	   InputStream is=null;
					   		   	   try {
					   		   		   LetturaScritturaFileFS f = new LetturaScritturaFileFS(sDirectoryFile+"/"+idObjFile);
					   		   		   is=f.leggiFile();          		 	   		   		   
					   		   	   }
					   		   	   catch (Exception e) {
					   		   		   throw new Exception("Errore lettura file "+sDirectoryFile+"/"+idObjFile+" da FS\nErrore: "+e.getMessage());
					   		   	   }
					   		   	   
					   		   	   try {
					   		   		   updObjFile(idDoc,idObjFile,idLog,acr,true,is);	   		   		   
					   		   	   }
					   		   	   catch (Exception e) {
					   		   		   try { is.close();}catch (Exception ei) {}
					   		   		   throw new Exception("Errore scrittura file "+sDirectoryFile+"/"+idObjFile+" da FS su colonna Blob\nErrore: "+e.getMessage());
					   		   	   }
					   		   	   
					   		   	   try { is.close();}catch (Exception e) {}
			   		    	}
			   		     }
		   		     }
		   		     else {		   		    	
		   		    	 msg+=" già trattato! passo al prossimo";	
		   		    	scriviMessaggio(msg);
		   		     }
		   		     //System.out.println("("+(elementList.size() - i+1)+" elementi rimanenti)");
		   		   scriviMessaggio("******** FINE CICLO **********");
		   	   }
		   	   
		   	   if (idOggettoFile!=null) {
		   		   for(int i=0;i<element.size();i++) {	 
		   			   updObjFile(element.get(i).idDocumento,element.get(i).idObjFile ,element.get(i).idLog,acr);
		   		   }
		   	   }
	   }
	   
	   private void updObjFile(String idDocumento, String oggettoFile, String idLog, String acron) throws Exception {	
		   	   updObjFile(idDocumento,oggettoFile,idLog,acron,false,null);
	   }
	   
	   private void updObjFile(String idDocumento, String oggettoFile, String idLog,String acron, boolean bReverse, InputStream is) throws Exception {	
		 //TODO....da togliere
	   	//	if (1==1) return;
		   	   StringBuffer stm = new StringBuffer("");
		   	   String sqlTest="", msg="";
		   	   
		   	   if (oggettoFile==null || oggettoFile.equals("null")) {
		   		   
		   	   }
		   	   else {
			   	   //Per prima cosa e PER SICUREZZA ESTREMA controllo se il bfile punta correttamente alla directory ed al file
			   	   String path="";
			   	   if (idLog.equals("-1")) 
			   		   path="TIPI_DOCUMENTO.ACRONIMO_MODELLO || '/' || to_char(trunc(DOCUMENTI.ID_DOCUMENTO/1000)) || '/' || DOCUMENTI.ID_DOCUMENTO || '/"+oggettoFile+"'";
			   	   else {
			   		   path="TIPI_DOCUMENTO.ACRONIMO_MODELLO || '/' || to_char(trunc(DOCUMENTI.ID_DOCUMENTO/1000)) || '/' || DOCUMENTI.ID_DOCUMENTO || '/LOG_' || "+idLog+" || '/"+oggettoFile+"'";
			   		   msg=" e idLog= "+idLog;
			   	   }
			   	   
			   	   sqlTest="select count(*), ";
		   		   sqlTest+="nvl(max("+path+"),'') ";
		   		   sqlTest+="from TIPI_DOCUMENTO, AREE, DOCUMENTI ";
		   		   sqlTest+="WHERE DOCUMENTI.ID_DOCUMENTO="+idDocumento+" AND DOCUMENTI.AREA=AREE.AREA AND DOCUMENTI.ID_TIPODOC=TIPI_DOCUMENTO.ID_TIPODOC AND ";
		   		   sqlTest+="dbms_lob.fileexists(bfilename(F_GETDIRECTORY_AREA_NAME("+idDocumento+"), ";
		   		   sqlTest+=path+" )    )=0";
		   		   
		   		  // System.out.println("sqlTest-->"+sqlTest);
		   		   
		   		   try {
		   			  scriviMessaggio("******** CONTROLLO ESISTENZA FILE SU FS **********");
		   			 dbOp.setStatement(sqlTest);	
		   			 dbOp.execute();
		   			 
		   			 dbOp.next();
		   			 
		   			 ResultSet rst = dbOp.getRstSet();
		   			 if (rst.getLong(1)>0)
		   				 throw new Exception("Impossibile effettuare la pulizia del BLOB per l'oggetto_file (idObjFile="+oggettoFile+") "+msg+". Non trovo il percorso su FS: "+rst.getString(2));
		   			 
		   			 try {rst.close();}catch (Exception ei){}
		   		   }
		   		   catch (Exception e){
		   			 scriviMessaggio("******** ERRORE ESISTENZA FILE SU FS **********-->"+e.getMessage());
		   			 throw(e); 
		   		   }
		   	   }
	   		
		   	  /* if (idLog.equals("-1")) {		   		  		   			   
			   	   stm.append("UPDATE OGGETTI_FILE ");
			   	   if (!bReverse) {
			   		   stm.append("	SET PATH_FILE='"+acron+"' ");
			   		   //stm.append(",\"FILE\"" + " = bfilename( 'DIR_"+acronimo+"', '"+idDocumento+"'||'/'||FILENAME ) ");
			   		   stm.append(",TESTOOCR=null ");
			   	   } 
			   	   else {
			   		   stm.append(" SET TESTOOCR = :P_TESTOOCR ");
			   		   stm.append(" ,PATH_FILE = null ");
			   		   stm.append(" ,\"FILE\"" + " = null ");
			   		   			   		
			   	   }
		   		   stm.append("WHERE ID_OGGETTO_FILE="+oggettoFile);
		   	   }
		   	   else {
		   		   stm.append("UPDATE OGGETTI_FILE_LOG");
		   		   if (!bReverse) {
		   			   stm.append("	SET PATH_FILE='"+acron+"\\LOG_'||id_log ");
		   			   //stm.append("\"FILE\"" + " = bfilename( 'DIR_"+acronimo+"', '"+idDocumento+"'||'/LOG_'||id_log||'/'||FILENAME ) ");
		   			   stm.append(",testoocr=null ");
		   		   } 
		   		   else {
		   			   stm.append(" SET TESTOOCR = :P_TESTOOCR ");
			   		   stm.append(" ,PATH_FILE = null ");
			   		   stm.append(" ,\"FILE\"" + " = null ");
		   		   }
		   		   stm.append("WHERE ID_OGGETTO_FILE="+oggettoFile);
		   		   stm.append("  AND ID_LOG="+idLog);
		   	   }*/
		   		  
		   	 
	   		   try {		   		    
	   			    /*scriviMessaggio("******** PULIZIA BLOB TABELLA **********");
			        dbOp.setStatement(stm.toString());	
			        
			        if (bReverse) {
			        	dbOp.setParameter(":P_TESTOOCR", is, is.available());         
			        }
					   
					dbOp.execute();	
					dbOp.commit();*/
	   			   	
	   			   for(int i=0;i<10;i++) {
	   				   try {
	   					   updateSingleObjFile( idLog,  bReverse,  acron, oggettoFile,  is ,i );
	   					   
	   					   i=10;
	   				   }
	   				   catch(Exception e)  {
	   					   if (i>=8) throw(e); 
	   				   }	   				   
	   			   }
	   			   
	   		   }
	   		   catch (Exception e) {
	   			   scriviMessaggio("******** ERRORE PULIZIA BLOB TABELLA **********"+e.getMessage());
	   			   dbOp.rollback();
	   			   e.printStackTrace();
	   			   
	   			   String colonna="bFile";
	   			   if (bReverse) colonna="blob";
	   			   
	   			    if (idLog.equals("-1")) 
	   			    	throw new Exception("Errore update "+colonna+" su oggetti_file (idObjFile="+oggettoFile+"). SQL= "+stm.toString()+"\nErrore: "+e.getMessage());
	   			    else
	   			    	throw new Exception("Errore update "+colonna+" su oggetti_file_log (idObjFile="+oggettoFile+",idLog="+idLog+"). SQL= "+stm.toString()+"\nErrore: "+e.getMessage());
	   		   }
	   		   	   		      		   
	   }
	   
	   private void updateSingleObjFile(String idLog, boolean bReverse, String acron,String oggettoFile, InputStream is , int tentativo ) throws Exception {
		   	   StringBuffer stm = new StringBuffer("");
		   	   
		   	   if (idLog.equals("-1")) {		   		  		   			   
			   	   stm.append("UPDATE OGGETTI_FILE ");
			   	   if (!bReverse) {
			   		   stm.append("	SET PATH_FILE='"+acron+"' ");
			   		   //stm.append(",\"FILE\"" + " = bfilename( 'DIR_"+acronimo+"', '"+idDocumento+"'||'/'||FILENAME ) ");
			   		   stm.append(",TESTOOCR=null ");
			   	   } 
			   	   else {
			   		   stm.append(" SET TESTOOCR = :P_TESTOOCR ");
			   		   stm.append(" ,PATH_FILE = null ");
			   		   stm.append(" ,\"FILE\"" + " = null ");
			   		   			   		
			   	   }
		   		   stm.append("WHERE ID_OGGETTO_FILE="+oggettoFile);
		   	   }
		   	   else {
		   		   stm.append("UPDATE OGGETTI_FILE_LOG");
		   		   if (!bReverse) {
		   			   stm.append("	SET PATH_FILE='"+acron+"\\LOG_'||id_log ");
		   			   //stm.append("\"FILE\"" + " = bfilename( 'DIR_"+acronimo+"', '"+idDocumento+"'||'/LOG_'||id_log||'/'||FILENAME ) ");
		   			   stm.append(",testoocr=null ");
		   		   } 
		   		   else {
		   			   stm.append(" SET TESTOOCR = :P_TESTOOCR ");
			   		   stm.append(" ,PATH_FILE = null ");
			   		   stm.append(" ,\"FILE\"" + " = null ");
		   		   }
		   		   stm.append("WHERE ID_OGGETTO_FILE="+oggettoFile);
		   		   stm.append("  AND ID_LOG="+idLog);
		   	   }
		   	   
		   	   
		   	    scriviMessaggio("******** PULIZIA BLOB TABELLA per file="+oggettoFile+" - tentativo n° "+(tentativo+1)+" **********");
		        dbOp.setStatement(stm.toString());	
		        
		        if (bReverse) {
		        	dbOp.setParameter(":P_TESTOOCR", is, is.available());         
		        }
				   
				dbOp.execute();	
				dbOp.commit();		   		   	   	  
		   	   
	   }
	   
	   
	   
	   /*private static void makePassoB() throws Exception {		
		   	   StringBuffer stm = new StringBuffer("");
		   	   
		   	   stm = new StringBuffer("CREATE OR REPLACE DIRECTORY ");
		   	   stm.append("DIR_"+acronimo+" AS '"+pathFileOracle+"/"+docFilesDir+"'");
	    	   	    	   
		   	   try {		   		    	 	   		      			        
			        dbOp.setStatement(stm.toString());
					   
					dbOp.execute();		   		    				   		    
	   		   }
	   		   catch (Exception e) {		   		    	 	
	   		    	throw new Exception("Errore creazione directory oracle. SQL= "+stm.toString()+"\nErrore: "+e.getMessage());
	   		   }		   	   
	   		   
	   		   stm = new StringBuffer("");
	   		   stm.append("update (select PATH_FILE,\"FILE\",oggetti_file.ID_DOCUMENTO,FILENAME,testoocr from oggetti_file ");
	   		   stm.append("inner join documenti on (oggetti_file.id_documento=documenti.id_documento and DOCUMENTI.AREA = :P_AREA))");
	   		   stm.append("	set PATH_FILE='"+docFilesDir+"',");
	   		   stm.append("\"FILE\"" + " = bfilename( 'DIR_"+acronimo+"', ID_DOCUMENTO||'/'||FILENAME ) ");
	   		   stm.append(",testoocr=null ");
	   		   
	   		   try {		   		    	 	   		      			        
			        dbOp.setStatement(stm.toString());
			        
			        dbOp.setParameter(":P_AREA", area);
					   
					dbOp.execute();		   		    				   		    
	   		   }
	   		   catch (Exception e) {		   		    	 	
	   		    	throw new Exception("Errore update bfile su oggetti file. SQL= "+stm.toString()+"\nErrore: "+e.getMessage());
	   		   }
	   		   
	   		   stm = new StringBuffer("");
	   		   stm.append("update (select PATH_FILE,\"FILE\",DOCUMENTI.ID_DOCUMENTO,FILENAME,testoocr,oggetti_file_log.id_log from oggetti_file_log ");
	   		   stm.append(", documenti , activity_log ");
	   		   stm.append("where DOCUMENTI.AREA = :P_AREA and ");
	   		   stm.append(" activity_log.id_log=oggetti_file_log.id_log  and "); 
	   		   stm.append(" activity_log.id_documento=documenti.id_documento) ");
	   		   stm.append("	set PATH_FILE='"+docFilesDir+"\\LOG_'||id_log,");
	   		   stm.append("\"FILE\"" + " = bfilename( 'DIR_"+acronimo+"', ID_DOCUMENTO||'/LOG_'||id_log||'/'||FILENAME ) ");
	   		   stm.append(",testoocr=null ");
		   	  
	   		   try {		   		    	 	   		      			        
			        dbOp.setStatement(stm.toString());
			        
			        dbOp.setParameter(":P_AREA", area);
					   
					dbOp.execute();		   		    				   		    
	   		   }
	   		   catch (Exception e) {		   		    	 	
	   		    	throw new Exception("Errore update bfile su oggetti file. SQL= "+stm.toString()+"\nErrore: "+e.getMessage());
	   		   }	   		   
	   		   
	   		   stm = new StringBuffer("");
	   		   stm.append("update aree set PATH_FILE=:P_PATHTOMCAT, PATH_FILE_ORACLE=:P_PATHORCL where area=:P_AREA ");
	   		   
	   		   try {		   		    	 	   		      			        
			        dbOp.setStatement(stm.toString());
			        
			        dbOp.setParameter(":P_PATHTOMCAT", pathFileTomcat);
			        dbOp.setParameter(":P_PATHORCL", pathFileOracle);
			        dbOp.setParameter(":P_AREA", area);
					   
					dbOp.execute();		   		    				   		    
	   		   }
	   		   catch (Exception e) {		   		    	 	
	   		    	throw new Exception("Errore update path su aree. SQL= "+stm.toString()+"\nErrore: "+e.getMessage());
	   		   }
	   }*/
	   
	   private void writeLog(String file, String message) {		       		   
	       if (file==null) return;
	       try {      
	    	 StringBuffer messaggio = new StringBuffer("");
	       		       		 
	    	 messaggio.append(message);		       
	   	   
	   		 FileOutputStream outfile = new FileOutputStream(file,true);
	   		 PrintStream Output = new PrintStream(outfile);

		     Output.println(messaggio.toString());
		     Output.close();
		     outfile.close();
	   	   } 
	   	   catch (IOException e) {
	   		 e.printStackTrace();
	   	   }
	   }
	   
	   private void fillElementList() throws Exception {
		   	   fillElementList(false);
	   }	   	   
	   
	   private void fillElementList(boolean bReverse) throws Exception {
		   	   elementList.removeAllElements();
		   	   String pathFileOperatorCondition="=";
		   	   if (bReverse) pathFileOperatorCondition="<>";
		   	   
		   	   //Per prima cosa controllo se esiste la tbl della conservazione
		   	   boolean bEsisteTblConservazione=false;
		   	   if (this.conservazione!=null) {
		   		   try {
		   			   dbOp.setStatement("SELECT 1 FROM GDM_T_LOG_CONSERVAZIONE");
		   			   dbOp.execute();
		   			   bEsisteTblConservazione=true;
		   		   }
		   		   catch (Exception e) {
		   			   bEsisteTblConservazione=false;
		   		   }
		   	   }		   	  		   	   
		   	  
		   	   //Test se modello è orizzontale o verticale
		   	   String nomeTabellaOrizzontale=testModelloVerticalHorizontal(true,area, modello);		   	   
		   	   
		   	   if (nomeTabellaOrizzontale.equals("") && areaFiglio!=null)
		   	      throw new Exception("Impossibile esportare solo i figli di un modello non orizzontale.");
		   	   		   		   
		   	   String nomeTabellaOrizzontaleFiglia="";
		   	   if (areaFiglio!=null) nomeTabellaOrizzontaleFiglia=testModelloVerticalHorizontal(false,areaFiglio,modelloFiglio);
		   	   if (!nomeTabellaOrizzontaleFiglia.equals("")) propagaFigli="N";
		   		   		  
		   	   StringBuffer sStm = new StringBuffer("");
		   	   
		   	   sStm.append("SELECT codice_modello, qry.id_documento,id_oggetto_file,id_log,acronimo_modello,pFile ");		   	
		   	   sStm.append("FROM (");
		   	   
		   	   if (nomeTabellaOrizzontaleFiglia.equals(""))
		   		   sStm.append("SELECT MODELLI.CODICE_MODELLO, DOCUMENTI.ID_DOCUMENTO, OGGETTI_FILE.ID_OGGETTO_FILE,-1 id_log,TIPI_DOCUMENTO.ACRONIMO_MODELLO,NVL(OGGETTI_FILE.PATH_FILE,' ') pFile,tipi_documento.id_tipodoc ");
		   	   else
		   		   sStm.append("SELECT modFiglio.CODICE_MODELLO, docFiglio.ID_DOCUMENTO, OGGETTI_FILE.ID_OGGETTO_FILE,-1 id_log,tipiDocFiglio.ACRONIMO_MODELLO,NVL(OGGETTI_FILE.PATH_FILE,' ') pFile,tipiDocFiglio.id_tipodoc ");
		   	   
		   	   sStm.append("FROM DOCUMENTI, MODELLI, OGGETTI_FILE, TIPI_DOCUMENTO ");
		   	   if (!nomeTabellaOrizzontale.equals("")) sStm.append(","+nomeTabellaOrizzontale+" ");
		   	   if (!nomeTabellaOrizzontaleFiglia.equals("")) {
		   		  sStm.append(",documenti docFiglio,modelli modFiglio,tipi_documento tipiDocFiglio ");
		   	   }
		   	   
		   	   sStm.append("WHERE DOCUMENTI.ID_TIPODOC = MODELLI.ID_TIPODOC ");
		   	   		   	   
		   	   if (!nomeTabellaOrizzontaleFiglia.equals("")) 
		   		   sStm.append("AND  docFiglio.ID_DOCUMENTO = OGGETTI_FILE.ID_DOCUMENTO (+) ");
		   	   else		   		   
		   		   sStm.append("AND  DOCUMENTI.ID_DOCUMENTO = OGGETTI_FILE.ID_DOCUMENTO(+) ");
		   	   
		   	   
		   	   if (idOggettoFile!=null) {
		   		   sStm.append("AND OGGETTI_FILE.ID_OGGETTO_FILE = :P_IDOBJFILE ");
		   	   }
		   	   else {
		   		   sStm.append("AND DOCUMENTI.AREA = :P_AREA ");
		   		   sStm.append("AND MODELLI.CODICE_MODELLO = :P_MODELLO ");
		   		   //sStm.append("AND NVL(OGGETTI_FILE.PATH_FILE,' ') "+pathFileOperatorCondition+" ' ' ");
		   	   }		   		   
		       sStm.append("AND MODELLI.ID_TIPODOC= TIPI_DOCUMENTO.ID_TIPODOC  ");		       
		       if (!nomeTabellaOrizzontale.equals("")) sStm.append("AND "+nomeTabellaOrizzontale+".ID_DOCUMENTO=DOCUMENTI.ID_DOCUMENTO ");
		       
		       if (!nomeTabellaOrizzontaleFiglia.equals("")) {
		    	     sStm.append("AND "+nomeTabellaOrizzontaleFiglia+".idRif = "+nomeTabellaOrizzontale+".idRif ");
		    	     sStm.append("AND "+nomeTabellaOrizzontaleFiglia+".id_Documento = docFiglio.id_Documento ");
		    	     sStm.append("AND docFiglio.id_tipodoc = modFiglio.id_tipodoc ");
		    	     sStm.append("AND modFiglio.id_tipodoc = tipiDocFiglio.id_tipodoc ");
		       }
		       
		       if (!bReverse) sStm.append(" AND  ( nvl(dbms_lob.getLength(OGGETTI_FILE.testoocr),0) <>0 OR (OGGETTI_FILE.ID_DOCUMENTO is null)  ) ");		      
		       
		       if (!nomeTabellaOrizzontale.equals("")) sStm.append(getSQLConditionHorizontalTable(nomeTabellaOrizzontale));
		       
		   	   
		   	   sStm.append("UNION ALL ");
		   	   
		   	   if (nomeTabellaOrizzontaleFiglia.equals(""))
		   		   sStm.append("SELECT MODELLI.CODICE_MODELLO, DOCUMENTI.ID_DOCUMENTO, OGGETTI_FILE_LOG.ID_OGGETTO_FILE,oggetti_file_log.id_log,TIPI_DOCUMENTO.ACRONIMO_MODELLO,NVL(oggetti_file_log.PATH_FILE,' ') pfile,tipi_documento.id_tipodoc ");
		   	   else
		   		   sStm.append("SELECT modFiglio.CODICE_MODELLO, docFiglio.ID_DOCUMENTO, OGGETTI_FILE_LOG.ID_OGGETTO_FILE,oggetti_file_log.id_log,tipiDocFiglio.ACRONIMO_MODELLO,NVL(oggetti_file_log.PATH_FILE,' ') pfile,tipiDocFiglio.id_tipodoc ");
		   		   
		   	   sStm.append("FROM DOCUMENTI, MODELLI, oggetti_file_log, activity_log, TIPI_DOCUMENTO ");		   	   
		   	   if (!nomeTabellaOrizzontale.equals("")) sStm.append(","+nomeTabellaOrizzontale+" ");
		   	   if (!nomeTabellaOrizzontaleFiglia.equals("")) {
		   		  sStm.append(",documenti docFiglio,modelli modFiglio,tipi_documento tipiDocFiglio ");
		   	   }
		   	   
		   	   sStm.append("WHERE DOCUMENTI.ID_TIPODOC = MODELLI.ID_TIPODOC ");
		   	   if (idOggettoFile!=null) {
		   		   sStm.append("AND OGGETTI_FILE_LOG.ID_OGGETTO_FILE = :P_IDOBJFILE ");
		   	   }
		   	   else {
		   		   sStm.append("AND DOCUMENTI.AREA = :P_AREA ");
		   		   sStm.append("AND MODELLI.CODICE_MODELLO = :P_MODELLO ");
			   	   sStm.append("AND NVL(oggetti_file_log.PATH_FILE,' ') "+pathFileOperatorCondition+" ' ' ");			   	      
		   	   }		   	   		   	  		   	   
		   	   sStm.append("AND oggetti_file_log.ID_LOG =  activity_log.id_log ");
		   	   //sStm.append("AND nvl(oggetti_file_log.tipo_operazione,'X') <>'E' ");
		   	   
		   	   if (!nomeTabellaOrizzontaleFiglia.equals("")) 
		   		   sStm.append("AND activity_log.id_documento=docFiglio.id_documento ");
		   	   else
		   		   sStm.append("AND activity_log.id_documento=documenti.id_documento ");
		       
		   	   sStm.append("AND MODELLI.ID_TIPODOC= TIPI_DOCUMENTO.ID_TIPODOC  ");		 
		       if (!nomeTabellaOrizzontale.equals("")) sStm.append("AND "+nomeTabellaOrizzontale+".ID_DOCUMENTO=DOCUMENTI.ID_DOCUMENTO ");
		       
		       if (!nomeTabellaOrizzontaleFiglia.equals("")) {
		    	   sStm.append("AND "+nomeTabellaOrizzontaleFiglia+".idRif = "+nomeTabellaOrizzontale+".idRif ");
	    	       sStm.append("AND "+nomeTabellaOrizzontaleFiglia+".id_Documento = docFiglio.id_Documento ");
	    	       sStm.append("AND docFiglio.id_tipodoc = modFiglio.id_tipodoc ");
	    	       sStm.append("AND modFiglio.id_tipodoc = tipiDocFiglio.id_tipodoc ");
		       }
		       
		       if (!bReverse) sStm.append(" AND nvl(dbms_lob.getLength(oggetti_file_log.testoocr),0) <>0  ");		
		       
		       if (!nomeTabellaOrizzontale.equals("")) sStm.append(getSQLConditionHorizontalTable(nomeTabellaOrizzontale));
		       
		       sStm.append(") QRY ");		       
		       sStm.append("WHERE 1=1 ");		       		   	   		       
		       
		       if (nomeTabellaOrizzontale.equals("")) {
		    	   //Gestisco i parametri con la f_valore_campo trattandosi di tbl verticale		    	   
			   	   if (anno!=null)
			    	   sStm.append("AND (TO_NUMBER(F_VALORE_CAMPO(TO_CHAR(ID_DOCUMENTO),'ANNO')) = "+anno+" or F_CAMPO('ANNO',id_tipodoc)=0) ");
			   	   if (tiporegistro!=null) {
			   		   if (tiporegistro.toUpperCase().equals("ALTRO")) {
			   			   sStm.append("AND (:P_AREA,CODICE_MODELLO) NOT IN "+listaModelliEsclusi);
			   		   }
			   		   else {
			   			   sStm.append("AND (F_VALORE_CAMPO(TO_CHAR(ID_DOCUMENTO),'TIPO_REGISTRO') = '"+tiporegistro+"' or F_CAMPO('TIPO_REGISTRO',id_tipodoc)=0) ");  
			   		   }		    	   		    	  
			   	   }
			   	   if (dataprotocolloDa!=null)
			    	   sStm.append("AND (  (TO_DATE(substr(nvl(F_VALORE_CAMPO(TO_CHAR(ID_DOCUMENTO),'DATA'),'01/01/1900'),1,10),'dd/mm/yyyy') BETWEEN TO_DATE('"+dataprotocolloDa+"','dd/mm/yyyy') AND TO_DATE('"+dataprotocolloA+"','dd/mm/yyyy')) or F_CAMPO('DATA',id_tipodoc)=0) ");
			   	   if (dataAdozioneDa!=null)
			    	   sStm.append("AND (  (TO_DATE(substr(nvl(F_VALORE_CAMPO(TO_CHAR(ID_DOCUMENTO),'DATA_REG_DETERMINA'),'01/01/1900'),1,10),'dd/mm/yyyy') BETWEEN TO_DATE('"+dataAdozioneDa+"','dd/mm/yyyy') AND TO_DATE('"+dataAdozioneA+"','dd/mm/yyyy')) or F_CAMPO('DATA_REG_DETERMINA',id_tipodoc)=0) ");		   	   
			   	   if (dataRevisioneDa!=null)
			    	   sStm.append("AND (  (TO_DATE(substr(nvl(F_VALORE_CAMPO(TO_CHAR(ID_DOCUMENTO),'DATA_REVISIONE'),'01/01/1900'),1,10),'dd/mm/yyyy') BETWEEN TO_DATE('"+dataRevisioneDa+"','dd/mm/yyyy') AND TO_DATE('"+dataRevisioneA+"','dd/mm/yyyy')) or F_CAMPO('DATA_REVISIONE',id_tipodoc)=0) ");			   	   
			   	   if (numeroregistroDa!=null)
			    	   sStm.append("AND (  (TO_NUMBER(F_VALORE_CAMPO(TO_CHAR(ID_DOCUMENTO),'NUMERO')) BETWEEN "+numeroregistroDa+" AND "+numeroregistroA+") or F_CAMPO('NUMERO',id_tipodoc)=0) ");
			   	   if (idDocumentoDa!=null)
			   		   sStm.append("AND (ID_DOCUMENTO BETWEEN "+idDocumentoDa+" AND "+idDocumentoA+") ");
		       }
		       
		       if (dataCreazioneDa!=null) {		    	    
		    	   sStm.append("AND exists (select 'x' from stati_documento where stati_documento.id_documento=QRY.id_documento having trunc(min(stati_documento.data_aggiornamento)) BETWEEN TO_DATE('"+dataCreazioneDa+"','dd/mm/yyyy') AND TO_DATE('"+dataCreazioneA+"','dd/mm/yyyy')    ) ");
		       }
		       
		   	   if (this.conservazione!=null && bEsisteTblConservazione) {
		   		   String operator;
		   		   
		   		   if (conservazione.toUpperCase().equals("N"))
		   			   operator="=";
		   		   else
		   			   operator="<>";
		   		   
		   		   sStm.append("AND (  (SELECT COUNT(*) FROM GDM_T_LOG_CONSERVAZIONE WHERE GDM_T_LOG_CONSERVAZIONE.ID_DOCUMENTO_RIF=QRY.ID_DOCUMENTO AND STATO_CONSERVAZIONE='CC') "+operator+" 0 )");
		   	   }
		   	 
		   	   //Escludo comunque i modelli figli che sono specificati nel legame (se i figli sono da esportare "subordinati ai padri")
			   if (propagaFigli.equals("Y")) {
				   Iterator iHm = hmsLegamiModelli.getHashMap();
				   
		   	    	if (iHm!=null) {
		   	    		while (iHm.hasNext()) {	
			   	    		String sAreaCm = (String)iHm.next();
			   	    		String areaModelloPadre=area+","+modello;
			   	    		
			   	    		if (!(areaModelloPadre).equals(sAreaCm))  continue;
			   	    		
			   	    		Iterator iHs = hmsLegamiModelli.getHashSet(sAreaCm);
			   	    		
			   	    		if (iHs!=null)
					   	    	while (iHs.hasNext()) 	{
					   	    		LegameStruct legStruct = (LegameStruct)iHs.next();
					   	    		
					   	    		if (!legStruct.arCmFiglio.equals("SEGRETERIA.PROTOCOLLO,REVISIONE"))
					   	    			sStm.append(" AND :P_AREA ||','|| CODICE_MODELLO <> '"+legStruct.arCmFiglio+"' ");
					   	    		
					   	    	}
		   	    		}
		   	    	}
			   }
		   			   	   
		   	   sStm.append("ORDER BY 1,2,3,4");
		   	   
		   	   ResultSet rst = null;
		   	   try  {
		   		 scriviMessaggio("SELECT PRINCIPALE PER area="+area+", modello="+modello+"\n");
		   		 scriviMessaggio(sStm.toString()+"\n\n");
		   	     dbOp.setStatement(sStm.toString());
		   	   
		   	     if (idOggettoFile!=null) 
		   	    	 dbOp.setParameter(":P_IDOBJFILE", idOggettoFile);
		   	     else {
		   	    	dbOp.setParameter(":P_AREA", area);
		   	        dbOp.setParameter(":P_MODELLO", modello);
		   	     }
		   	    	 
		   	     
		   	     dbOp.execute();		   	     		   	   
		   	     
		   	     rst = dbOp.getRstSet();
		   	     
		   	     while (rst.next()) {
		   	    	boolean bGiaTrascodificato=false; 
		   	    	if (!bReverse) {
		   	    		if (!rst.getString(6).equals(" "))  bGiaTrascodificato=true;
		   	    	}
		   	    	else {
		   	    		if (rst.getString(6).equals(" "))  bGiaTrascodificato=true;
		   	    	}
		   	    					   	    	 
		   	    	elementList.add(new OggettoFileStruct(rst.getString(3),rst.getString(2),rst.getString(1),rst.getString(4),rst.getString(5),bGiaTrascodificato));
		   	    			   	    	
		   	    	if (rst.getString(4).equals("-1") && propagaFigli.equals("Y") && (!nomeTabellaOrizzontale.equals(""))) {
		   	    	//TODO FILL MODELLI FIGLI LISTAIDSTRING		   	    		   	    		
		   	    		Iterator iHs = hmsLegamiModelli.getHashSet(area+","+rst.getString(1));
		   	    		
		   	    		if (iHs!=null)
				   	    	while (iHs.hasNext()) {
				   	    		LegameStruct legStruct = (LegameStruct)iHs.next();
				   	    		
				   	    		String campolegame = legStruct.tblFiglia.substring(legStruct.tblFiglia.indexOf(",")+1);				   	    		
				   	    		
				   	    		String select="";
				   	    		//if (!nomeTabellaOrizzontale.equals("")) 
				   	    			select=campolegame+" FROM "+nomeTabellaOrizzontale+" WHERE ID_documento="+rst.getString(2);
				   	    		//else
				   	    	//		select="F_VALORE_CAMPO('"+rst.getString(2)+"','"+campolegame+"') FROM DUAL";
				   	    		
				   	    	/*	try  {
				   	    			select="SELECT F_VALORE_CAMPO('"+rst.getString(2)+"','"+campolegame+"') FROM DUAL";
					   	    		dbOp.setStatement(select);
					   	    		dbOp.execute();		   	     		   	   
							   	    rst = dbOp.getRstSet();
							   	    rst.next();
							   	    sIdRiferimento=rst.getString(1);							   	    
				   	    		}
				   	    		catch(Exception e) {
				   	    			throw new Exception("Errore in esecuzione "+select+"\nErrore: "+e.getMessage());
				   	    		}*/
				   	    			
				   	    		legStruct.listaIdDocSingolo.add("SELECT "+select);
				   	    		
						   	    if (legStruct.listaIdDoc==null) legStruct.listaIdDoc="";
				   	    		
				   	    		if ( ((legStruct.listaIdDoc).indexOf("SELECT "+select))==-1) {
				   	    		
					   	    		if (!legStruct.listaIdDoc.equals("")) legStruct.listaIdDoc+=" UNION ALL ";					   	    							   	    					   	    		
					   	    						   	    		
					   	    		legStruct.listaIdDoc+="SELECT "+select;
				   	    		}
				   	    	}
		   	    	}
		   	    	 
		   	     }
		   	     
		   	     
		   	   }
			   catch (Exception e) {
			   	 throw new Exception("Errore in recupero lista documenti da trattare. SQL= "+sStm.toString()+"\nErrore: "+e.getMessage());
			   }		
			   

	   	      if (propagaFigli.equals("Y") && (!nomeTabellaOrizzontale.equals(""))) {
	   	    	Iterator iHm = hmsLegamiModelli.getHashMap();
	   	    	
	   	    	if (iHm!=null) {
	   	    		while (iHm.hasNext()) {	
		   	    		String sAreaCm = (String)iHm.next();
		   	    		
		   	    		Iterator iHs = hmsLegamiModelli.getHashSet(sAreaCm);
		   	    		
		   	    		if (iHs!=null)
				   	    	while (iHs.hasNext()) 	{
				   	    		LegameStruct legStruct = (LegameStruct)iHs.next();
				   	    		
				   	    		if (legStruct.listaIdDoc==null) continue;
				   	    		
				   	    		for(int iSingoloDocPadre=0;iSingoloDocPadre<legStruct.listaIdDocSingolo.size();iSingoloDocPadre++) {
				   	    		
					   	    		//Genero l'SQL per il singolo IDDOC PADRE
					   	    		ResultSet res = getSQLModelliFigli(legStruct,iSingoloDocPadre,nomeTabellaOrizzontale,bReverse);
					   	    		
					   	    		if (res==null) continue;
					   	    		
					   	    		while (res.next()) {
					   	    			if (res.getString(8).equals(""))
					   	    				throw new Exception("Attenzione! non tutti gli oggetti file figli hanno l'area orizzontale\n"+
					   	    									"idOggettoFile="+res.getString(3)+", area="+res.getString(7)+". Impossibile continuare");
					   	    			
					   	    			String sPadre = res.getString(6);
					   	    			OggettoFileStruct ogfi = new OggettoFileStruct(res.getString(3),res.getString(2),res.getString(1),
					   	    														   res.getString(4),res.getString(5),res.getString(7),
					   	    														   res.getString(8));
					   	    			
					   	    			if (!res.getString(7).equals(area)) propAreeFigle.put(res.getString(7)+"@"+res.getString(8), "N");
					   	    			
					   	    			hmsChildElementList.add(sPadre, ogfi);
					   	    		}
				   	    	   }
				   	    	}
	   	    		}
	   	    	 }
	   	      }
	   	      
	   	      //printHmsChildElementListStruct();
	   	      //System.out.println("");
	   }	   	  
	   
	   private void fillElementListProto() throws Exception {
		       StringBuffer sStm = new  StringBuffer("");
		       
		       if (typeProtoIntern.equals(_PROTOINTER_PRINCIPALI) )  {
		       
			       sStm.append("SELECT MODELLI.CODICE_MODELLO, DOCUMENTI.ID_DOCUMENTO, -1 id_log, TIPI_DOCUMENTO.ACRONIMO_MODELLO, TIPI_DOCUMENTO.id_tipodoc , ");
			       sStm.append("allegato_principale, PROTO_VIEW.NUMERO,gs4_file_documento.id idFileDocumento ");
			       
			       sStm.append("FROM PROTO_VIEW, DOCUMENTI, TIPI_DOCUMENTO, MODELLI, gs4_file_documento ");
			       
			       sStm.append("WHERE PROTO_VIEW.ID_DOCUMENTO=DOCUMENTI.ID_DOCUMENTO ");
			       sStm.append("AND TIPI_DOCUMENTO.ID_TIPODOC=DOCUMENTI.ID_TIPODOC AND ");
			       sStm.append("MODELLI.ID_TIPODOC=TIPI_DOCUMENTO.ID_TIPODOC AND ");
			       
			       if (anno!=null) sStm.append("PROTO_VIEW.ANNO = :P_ANNO AND "); 
			       if (numeroregistroDa!=null) sStm.append("PROTO_VIEW.NUMERO BETWEEN :P_NUMERO_DA AND :P_NUMERO_A AND ");
			       if (tiporegistro!=null) sStm.append("PROTO_VIEW.TIPO_REGISTRO=:P_TIPOREG AND ");
			   	   if (idDocumentoDa!=null) sStm.append("PROTO_VIEW.ID_DOCUMENTO BETWEEN "+idDocumentoDa+" AND "+idDocumentoA+" AND ");
	
			       sStm.append("MODELLI.CODICE_MODELLO IN ('M_PROTOCOLLO','M_PROTOCOLLO_INTERO')  AND ");
			       sStm.append("NVL(allegato_principale,'X')<>'X' AND ");
			       sStm.append("0 = (SELECT COUNT(*) from OGGETTI_FILE WHERE OGGETTI_FILE.ID_DOCUMENTO=DOCUMENTI.ID_DOCUMENTO) AND ");
			       sStm.append("gs4_file_documento.ANNO=PROTO_VIEW.ANNO AND ");
			       sStm.append("gs4_file_documento.NUMERO=PROTO_VIEW.NUMERO AND ");
			       sStm.append("gs4_file_documento.TIPO_REGISTRO=PROTO_VIEW.TIPO_REGISTRO ");
			       
			       sStm.append("ORDER BY 1,2,3,4");
		       }
		       else {
		    	   sStm.append("SELECT   modelli.codice_modello, docAlle.id_documento, -1 id_log,");
		    	   sStm.append("tipi_documento.acronimo_modello, tipi_documento.id_tipodoc,");
		    	   sStm.append("seg_allegati_protocollo.FILE_ALLEGATO, proto_view.numero,");
		    	   sStm.append("FIAL.ID idfiledocumento ");
		    	   sStm.append("FROM proto_view,  tipi_documento, modelli, seg_allegati_protocollo,");
		    	   sStm.append("gs4_ALLEGATI_DOCUMENTO ALDO,gs4_FILE_ALLEGATO FIAL  , documenti docAlle ");
		    	   sStm.append("WHERE tipi_documento.id_tipodoc = docAlle.id_tipodoc ");
		    	   sStm.append("AND modelli.id_tipodoc = tipi_documento.id_tipodoc ");
		    	   sStm.append("AND proto_view.anno = :P_ANNO ");
		    	   sStm.append("AND proto_view.numero BETWEEN :P_NUMERO_DA AND :P_NUMERO_A ");
		    	   sStm.append("AND PROTO_VIEW.ID_DOCUMENTO BETWEEN "+idDocumentoDa+" AND "+idDocumentoA+" ");
		    	   sStm.append("AND proto_view.tipo_registro = :P_TIPOREG ");
		    	   sStm.append("AND modelli.codice_modello IN ('M_ALLEGATO_PROTOCOLLO') ");
		    	   sStm.append("and proto_view.idrif=seg_allegati_protocollo.idrif ");   
		    	   sStm.append("and docAlle.id_documento = seg_allegati_protocollo.id_documento ");
		    	   sStm.append("AND 0 = (SELECT COUNT (*) ");
		    	   sStm.append(" FROM oggetti_file ");
		    	   sStm.append("     WHERE oggetti_file.id_documento = docAlle.id_documento) ");
		    	   sStm.append(" AND ALDO.ANNO = proto_view.anno ");
		    	   sStm.append(" AND ALDO.TIPO_REGISTRO = proto_view.tipo_registro ");
		    	   sStm.append(" AND ALDO.NUMERO = proto_view.numero ");
		    	   sStm.append(" AND ALDO.FILE_ALLEGATO=seg_allegati_protocollo.FILE_ALLEGATO ");
		    	   sStm.append(" AND FIAL.ALLEGATO_ID (+) = ALDO.ALLEGATO_ID ");
		    	   sStm.append(" and nvl(seg_allegati_protocollo.FILE_ALLEGATO,'X')<>'X' ");
		    	   sStm.append(" and nvl(dbms_lob.getlength(FIAL.FILE_ALLEGATO),0)>0 ");
		    	   sStm.append(" ORDER BY 1, 2, 3, 4 ");
		       }
		       
		   	   ResultSet rst = null;
		   	   try  {
		   		 scriviMessaggio("SELECT PRINCIPALE PROTO PER area="+area+", modello="+modello+"\n");
		   		 scriviMessaggio(sStm.toString()+"\n\n");
		   	     dbOp.setStatement(sStm.toString());
		   	   
		   	     if (anno!=null) dbOp.setParameter(":P_ANNO", anno);		   	     
		   	     if (numeroregistroDa!=null) {
		   	    	 dbOp.setParameter(":P_NUMERO_DA", numeroregistroDa);
		   	    	 dbOp.setParameter(":P_NUMERO_A", numeroregistroA);
		   	     }
		   	     if (tiporegistro!=null) dbOp.setParameter(":P_TIPOREG", tiporegistro);
		   	     
		   	     dbOp.execute();		   	     		   	   
		   	     
		   	     rst = dbOp.getRstSet();
		   	     		   	     		   	     
		   	     while (rst.next()) {
		   	    	 //Per prima cosa inserisco l'oggetto_file senza blob
		   	    	 String nomeFile;
		   	    	 String idDocumento, codModello	, idLog, acrMod, idFileDoc;	   	    
		   	    	 
		   	    	 idDocumento=rst.getString(2);
		   	    	 codModello=rst.getString(1);
		   	    	 idLog=rst.getString(3);
		   	    	 acrMod=rst.getString(4);
		   	    	 idFileDoc=rst.getString(8);
		   	    	 nomeFile=rst.getString(6);		   	    	 		   	    	 
		   	    	 
		   	    	 //Li inserisco in lista con al posto dell'id il nome file
		   	    	 elementList.add(new OggettoFileStruct(nomeFile,idDocumento,codModello,idLog,acrMod,false,idFileDoc));
		   	     }
		   	     
		   	     //Alla fine riciclo ed inserisco gli oggetti file
		   	     for (int i=0;i<elementList.size();i++) {
		   	    	 String idOggettoFile;
		   	    	 OggettoFileStruct of;
		   	    	 
		   	    	 of=elementList.get(i);
		   	    	 
		   	    	 idOggettoFile=insertOggettoFile(of.idObjFile,of.idDocumento);
		   	    	 
		   	    	 of.idObjFile=idOggettoFile;
		   	    	 
		   	    	 elementList.set(i, of);
		   	    	 of=null;
		   	     }
		   	  }
			  catch (Exception e) {
			   	 throw new Exception("Errore in recupero lista documenti da trattare. SQL= "+sStm.toString()+"\nErrore: "+e.getMessage());
			  }		
			   		   	     
	   }
	   
	   private String getSQLForBlob(String idObjFile, String idObjFileExt) {
		   	   String sql="";
		   	   
		       if (typeProtoIntern==null)
		    	   sql= "SELECT TESTOOCR FROM oggetti_file where id_oggetto_file="+idObjFile;
		       else if (typeProtoIntern.equals(_PROTOINTER_PRINCIPALI)) 
		    	   sql= "SELECT FILE_DOCUMENTO FROM gs4_file_documento where id="+idObjFileExt;	
		       else if (typeProtoIntern.equals(_PROTOINTER_SECONDARI)) 
		    	   sql= "SELECT FILE_ALLEGATO FROM gs4_FILE_ALLEGATO where id="+idObjFileExt;
		       
		       return sql;
	   }
	   
	   private String getSQLConditionHorizontalTable(String nomeTabellaOrizzontale) {
		   	   StringBuffer sStm = new StringBuffer("");
		   	   
			   if (anno!=null)
		    	   sStm.append("AND "+nomeTabellaOrizzontale+".ANNO = "+anno+" ");
			   if (tiporegistro!=null) {
		   		   if (tiporegistro.toUpperCase().equals("ALTRO")) {
		   			   sStm.append("AND (:P_AREA,CODICE_MODELLO) NOT IN "+listaModelliEsclusi);
		   		   }
		   		   else {
		   			   sStm.append("AND "+nomeTabellaOrizzontale+".TIPO_REGISTRO '"+tiporegistro+"' ");  
		   		   }		    	   		    	  
		   	   }
			   if (dataprotocolloDa!=null)
		    	   sStm.append("AND ("+nomeTabellaOrizzontale+".DATA BETWEEN TO_DATE('"+dataprotocolloDa+" 00:00:00','dd/mm/yyyy hh24:mi:ss') AND TO_DATE('"+dataprotocolloA+" 23:59:59','dd/mm/yyyy hh24:mi:ss')) ");
			   if (dataAdozioneDa!=null)
		    	   sStm.append("AND (TRUNC("+nomeTabellaOrizzontale+".DATA_REG_DETERMINA) BETWEEN TO_DATE('"+dataAdozioneDa+" 00:00:00','dd/mm/yyyy hh24:mi:ss') AND TO_DATE('"+dataAdozioneA+" 23:59:59','dd/mm/yyyy hh24:mi:ss')) ");
			   if (dataRevisioneDa!=null)
		    	  sStm.append(" AND  (TO_DATE(substr(nvl("+nomeTabellaOrizzontale+".DATA_REVISIONE,'01/01/1900'),1,10),'dd/mm/yyyy') BETWEEN TO_DATE('"+dataRevisioneDa+"','dd/mm/yyyy') AND TO_DATE('"+dataRevisioneA+"','dd/mm/yyyy'))  ");			   
			   if (numeroregistroDa!=null)
		    	   sStm.append("AND (  "+nomeTabellaOrizzontale+".NUMERO  BETWEEN "+numeroregistroDa+" AND "+numeroregistroA+" ) ");
			   if (idDocumentoDa!=null)
		    	   sStm.append("AND (  "+nomeTabellaOrizzontale+".ID_DOCUMENTO  BETWEEN "+idDocumentoDa+" AND "+idDocumentoA+" ) ");
			   
			   if (vCampiChiavi.size()>0) {				   
				   for(int i=0;i<vCampiChiavi.size();i++) {
					   tipocampoDinamico=vCampiChiavi.get(i).tipocampoDinamico;
					   campoDinamico=vCampiChiavi.get(i).campoDinamico;
					   parACampoDinamico=vCampiChiavi.get(i).parACampoDinamico;
					   parDaCampoDinamico=vCampiChiavi.get(i).parDaCampoDinamico;
					   
					   if (tipocampoDinamico.equals("N")) 
						   sStm.append("AND (  "+nomeTabellaOrizzontale+"."+campoDinamico+"  BETWEEN "+parDaCampoDinamico+" AND "+parACampoDinamico+" ) ");
					   else if (tipocampoDinamico.equals("S")) 
						   sStm.append("AND (  "+nomeTabellaOrizzontale+"."+campoDinamico+"  BETWEEN '"+parDaCampoDinamico.replaceAll("'", "''")+"' AND '"+parACampoDinamico.replaceAll("'", "''")+"' ) ");
					   else
						   sStm.append("AND ("+nomeTabellaOrizzontale+"."+campoDinamico+" BETWEEN TO_DATE('"+parDaCampoDinamico+" 00:00:00','dd/mm/yyyy hh24:mi:ss') AND TO_DATE('"+parACampoDinamico+" 23:59:59','dd/mm/yyyy hh24:mi:ss')) ");
				   }
			   }
			   
			   return sStm.toString();
	   }
	   private void dropTempOracleDir(String nameDir) {
		   	   try {dbOp.setStatement("DROP DIRECTORY "+nameDir);dbOp.execute();} catch (Exception ei) {}
	   }
	   
	   private void scriviMessaggio(String messaggio) {
		       if (idOggettoFile!=null) return;
		       
		       System.out.println(messaggio);
		   	   writeLog(fileLog,messaggio);
	   }
	   
	 /*  public static void chmodfolder(String path) throws Exception {
			  
			 // System.out.println("creaz,,"+exists);
			   
				   
				   
				   
				 // System.out.println("success,,"+success);
				   try {
					   path="/jdocattach/DOCUMENTALE/SPR/$PROT/5176/5176304";
						   Process p = Runtime.getRuntime().exec("chmod -R 777 "+path);
						   
						   System.out.println("ESEGUO CHMOD: "+"chmod -R 777 "+path);
						   
						   int exitCode = p.waitFor();
						  
						   //writeLog(fileLog,"EXITCODE-->"+exitCode);
					   
				   }			   
				   catch (Exception e) {
					   e.printStackTrace();
					   throw new Exception("Errore in chmod777 di "+path+"\nErrore: "+e.getMessage());
				   }
				   
			   	 
			   		   	  
	   }	   */
	   
	   private boolean mkDirs(String path, boolean bControlla777Padre, String pathPadre) throws Exception {
			   boolean exists = (new File(path)).exists();
			   boolean existsPadre = (new File(pathPadre)).exists();
			 // System.out.println("creaz,,"+exists);

			   
			   if (exists) {
			       return true;
			   } else {
				   File f = new File(path);
				   				  
				   boolean success = (f).mkdirs();
				   
				  // scriviMessaggio("CREO LA DIRECTORY: "+path);
				   
				 // System.out.println("success,,"+success);
				   try {
					   String sistemaoperativo = System.getProperty("os.name"); 

					   if (!sistemaoperativo.startsWith("Windows") && Global.nvl(assegna777Cartelle, "Y").equals("Y")) { 
						   if (bControlla777Padre && (!existsPadre)) {
							   //Significa che devo la dir non esisteva ed è stata creata per ricorsione quindi va appicciato il 777
							   Process p = Runtime.getRuntime().exec("chmod -R 777 "+pathPadre);
							   
							  // scriviMessaggio("ESEGUO CHMOD DEL PADRE : "+"chmod -R 777 "+pathPadre);
							   
							   int exitCode = p.waitFor();
						   }
						   
						   Process p = Runtime.getRuntime().exec("chmod -R 777 "+path);
						   
						  // scriviMessaggio("ESEGUO CHMOD: "+"chmod -R 777 "+path);
						   
						   int exitCode = p.waitFor();
						  
						   //writeLog(fileLog,"EXITCODE-->"+exitCode);
					   }
				   }			   
				   catch (Exception e) {
					   e.printStackTrace();
					   throw new Exception("Errore in chmod777 di "+path+"\nErrore: "+e.getMessage());
				   }
				   
			   	   return success;
			   }		   	  
	   }
	   
	   private void chmod(String path) throws Exception {
		 
			   try {
				   String sistemaoperativo = System.getProperty("os.name"); 

				   if (!sistemaoperativo.startsWith("Windows") && Global.nvl(assegna777File, "Y").equals("Y") ) { 
					   Process p = Runtime.getRuntime().exec("chmod -R 777 "+path);
					   int exitCode = p.waitFor();
					  
					  // scriviMessaggio("ESEGUO CHMOD: "+"chmod -R 777 "+path);
					   //writeLog(fileLog,"EXITCODE-->"+exitCode);
				   }
			   }			   
			   catch (Exception e) {
				   e.printStackTrace();
				   throw new Exception("Errore in chmod777 di "+path+"\nErrore: "+e.getMessage());
			   }
			   
		   	   
	   }		   	  
 
	   
	   private long getDirMille(String idDoc) {
		   	    long lDir = Long.parseLong(idDoc);
		   	    return ((long)(lDir/1000));
	   }
	   
	   private void getInitAreaSQL(String areaToControl) throws Exception {
			   StringBuffer stmArea = new StringBuffer("SELECT AREE.AREA, AREE.ACRONIMO, AREE.PATH_FILE, AREE.PATH_FILE_ORACLE ");
		   	   stmArea.append("FROM AREE ");
		   	   if (idOggettoFile!=null) {
		   		 stmArea.append(",OGGETTI_FILE, DOCUMENTI WHERE OGGETTI_FILE.ID_OGGETTO_FILE="+idOggettoFile+" AND ");
		   		 stmArea.append("DOCUMENTI.ID_DOCUMENTO=OGGETTI_FILE.ID_DOCUMENTO AND DOCUMENTI.AREA=AREE.AREA AND ");
		   		 stmArea.append("NVL(AREE.ACRONIMO,' ')<>' '");
		   	   }
		   	   else {
		   		 stmArea.append("WHERE NVL(ACRONIMO,' ')<>' ' and area='"+areaToControl+"'");
		   	   }
		   	   
			   	try  {
			   	     dbOp.setStatement(stmArea.toString());
			   	   
			   	     dbOp.execute();
			   	     
			   	     ResultSet rst = dbOp.getRstSet();
			   	     
			   	     if (!rst.next()) {
			   	    	 if (idOggettoFile!=null)
			   	    		throw new Exception("Attenzione potrebbe essere inesistente l'oggetto file passato ("+idOggettoFile+"), oppure fa parte di un'area non orizzontale");
			   	    	 else
			   	    		 throw new Exception("L'area passata ("+areaToControl+") potrebbe non esistere o se esiste potrebbe non essere orizzontale");
			   	     }
			   	     else {
			   	    	 acronimo=rst.getString(2);
			   	    	 if (idOggettoFile!=null) {
			   	    		pathFileTomcat=rst.getString(3); 
			   	    		pathFileOracle=rst.getString(4);
			   	    		
			   	    		if (this.mapPathFileOracle==null) this.mapPathFileOracle=pathFileTomcat;
			   	    		
			   	    		if (Global.nvl(pathFileTomcat, "").equals("") || Global.nvl(pathFileOracle, "").equals("")) {
			   	    			throw new Exception("Per l'area dell'idOggetto File ("+idOggettoFile+") passato non esiste il percorso File Oracle o Tomcat.\nImpossibile continuare a trattare il passaggio del file su File System");
			   	    		}
			   	    	 } 
			   	     }		   	     
			   	   }
				   catch (Exception e) {
				   	 throw new Exception("Errore in recupero informazioni per area. SQL= "+stmArea.toString()+"\nErrore: "+e.getMessage());
				   }
	   }

	   private void showHelp() {
		   	   System.out.println("it.finmatica.dmServer.util.FileFsFromDb - Usage\n");
	   		   System.out.println("Parametri (posizionali) obbligatori richiesti:\n" +
	   		  				     "- pathFileProperties Percorso del file di properties\n" +
	   		  				     "- area Area da trasportare su file system (obbligatorio se non specificata una categoriaModello)\n" +	
	   		  				     "- modello<CAT:CategoriaModello> codiceModello da trascodificare oppure CAT:CategoriaModello (una fra quelle presenti dentro il file properties) \n" +	   		  				     	   		  				     
	   		  				     "- pathFileTomcat Percorso a partire da tomcat e che punta al path dove risederanno i file (necessariamente su FS dove sta oracle.)\n" +
	   		  				     "- pathFileOracle Percorso dove risederanno i file su FS (macchina oracle)\n" +
	   		  				     "- mapPathFileOracle Eventuale disco mappato su pathFileOracle per permettere a questo software di poter copiare i file in quel percorso senza necessariamente essere sulla macchina oracle.\n"+
	   		  				     "- fileLog - percorso del file di log\n"+
	   		  					 "- limit - se messo a 0 significa nessun limite altrimenti \"limit\" sta a significare che devo trattare un numero di documenti per area/codiceModello al max=limit\n"+
	   		  					 "- reverse S/N se messo a S significa che viene fatta la trasco al contrario (da FS a DB)\n\n"+
	   		  					 "Parametri opzionali:\n" +
	   		  					 "["+_ANNOPAR+" X] - Parametro non obbligatorio che filtra i documenti per il metadato 'anno' (se presente). Esempio di utilizzo: -anno 2011\n"+
	   		  					 "["+_TIPOREGISTROPAR+" X or ALTRO] - Parametro non obbligatorio che filtra i documenti per il metadato 'tipo_registro' (se presente). Se specificato 'ALTRO' verrano esclusi i modelli delibere/determine/protocolli. Esempio di utilizzo: -tiporegistro PRGE oppure -tiporegistro ALTRO\n"+
	   		  					 "["+_DATAPROTOCOLLOPAR+" X@Z] - Parametro non obbligatorio che filtra i documenti per il metadato 'data' (se presente) (per il protocollo è la data di protocollo). Esempio di utilizzo: -data 01/01/2011@26/09/2011\n"+
	   		  					 "["+_DATAADOZIONEPAR+" X@Z] - Parametro non obbligatorio che filtra i documenti per il metadato 'data_reg_determina' (se presente). Esempio di utilizzo: -dataadozione 01/03/2011@22/07/2011\n"+
	   		  					 "["+_NUMEROREGISTROPAR+" X@Z] - Parametro non obbligatorio che filtra i documenti per il metadato 'numero_registro' (se presente). Esempio di utilizzo: -numeroregistro 6@55\n"+
	   		  					 "["+_CONSERVAZIONEPAR+" Y or N] - Parametro non obbligatorio per filtrare quei documenti che sono (o non sono) stati portati in conservazione. Esempio di utilizzo: -conservazione N\n"+
	   		  					 "["+_PROPAGAFIGLIPAR+" Y or N] - Parametro non obbligatorio per propagare l'export anche ai documenti figli (default Y). Esempio di utilizzo: -propagaFigli N\n"+
	   		  					 "["+_ESPORTASOLOFIGLIPAR+" area@cm] - Parametro non obbligatorio per esportare solo ed esclusivamente il modello figlio legato (tramite IDRIF) al modello principale specificato nei parametri area/modello.\nSe specificata questa opzione, verrà ignorato il parametro di propagazione dei figli, inoltre i par. di ricerca saranno sempre applicati al modello padre, ma l'export sarà applicato ai figli legati tramite IDRIF.\nEsempio di utilizzo: -"+_ESPORTASOLOFIGLIPAR+" SEGRETERIA@M_ALLEGATO_PROTOCOLLO\n"+
	   		   					 "["+_IDDOCPAR+" idDocDa@idDocA] - Parametro non obbligatorio per filtrare i documenti per campo idDocumento.nEsempio di utilizzo: -"+_IDDOCPAR+" 1000@1500\n"+   		
	   		   					 "["+_CAMPODINAMICPAR+" 'nomeCampo#tipoCampo^valoreDa@valoreA%FINECAMPO%......%FINECAMPO%'] - Parametro non obbligatorio per filtrare una qualsiasi collezione di campi rispetto a dei valori passati in input (tipoCampo può valere S,N oppure D). Questa opzione vale SOLO per le tabelle orizzontali: -"+_CAMPODINAMICPAR+" 'N_DETERMINA#N^1000@1500%FINECAMPO%ANNO_DETERMINA#N^2000@2500%FINECAMPO%'\n"+
	   		   				     "["+_DATACREAZ+" dataDa@dataA] - Parametro non obbligatorio per filtrare i documenti per data di creazione: -"+_DATACREAZ+" 01/01/2013@31/01/2013\n"+
	   		   				     "["+_ASSEGNA777CART+" Y o N] - Parametro non obbligatorio per specificare se assegnare il 777 (solo per linux) alle cartelle create su FS (se non specificato di default è Y): -"+_ASSEGNA777CART+" N\n"+
	   		   				     "["+_ASSEGNA777FILE+" Y o N] - Parametro non obbligatorio per specificare se assegnare il 777 (solo per linux) ai file creati su FS (se non specificato di default è Y): -"+_ASSEGNA777FILE+" N\n");
	   }
	   
	   private void trattaParametroOpzionale(String parametro,String sValore) throws Exception {
		   	   String sPar;
		       if (parametro.equals(_ANNOPAR)) {
		    	   this.anno=sValore;
		       }
		       else if (parametro.equals(_TIPOREGISTROPAR)) {
		    	   this.tiporegistro=sValore;
		       }
		       else if (parametro.equals(_DATAPROTOCOLLOPAR)) {
		    	   sPar=sValore;
		    	   
		    	   if (sPar!=null && sPar.indexOf("@")!=-1) {
		    		   this.dataprotocolloDa=sPar.substring(0,sPar.indexOf("@"));
		    		   this.dataprotocolloA=sPar.substring(sPar.indexOf("@")+1);
		    	   }
		    	   else {
		    		   throw new Exception("Attenzione! Parametro data non valido. Utilizzare "+_DATAPROTOCOLLOPAR+" <dataDal>@<dataAl>");
		    	   }
		       }
		       else if (parametro.equals(_DATAADOZIONEPAR)) {
		    	   sPar=sValore;
		    	   
		    	   if (sPar!=null && sPar.indexOf("@")!=-1) {
		    		   this.dataAdozioneDa=sPar.substring(0,sPar.indexOf("@"));
		    		   this.dataAdozioneA=sPar.substring(sPar.indexOf("@")+1);
		    	   }
		    	   else {
		    		   throw new Exception("Attenzione! Parametro dataadozione non valido. Utilizzare "+_DATAADOZIONEPAR+" <dataDal>@<dataAl>");
		    	   }
		       }	
		       else if (parametro.equals(_DATAREVISIONE)) {
		    	   sPar=sValore;
		    	   
		    	   if (sPar!=null && sPar.indexOf("@")!=-1) {
		    		   this.dataRevisioneDa=sPar.substring(0,sPar.indexOf("@"));
		    		   this.dataRevisioneA=sPar.substring(sPar.indexOf("@")+1);
		    	   }
		    	   else {
		    		   throw new Exception("Attenzione! Parametro dataadozione non valido. Utilizzare "+_DATAREVISIONE+" <dataDal>@<dataAl>");
		    	   }
		       }				       
		       else if (parametro.equals(_NUMEROREGISTROPAR)) {
		    	   sPar=sValore;
		    	   
		    	   if (sPar!=null && sPar.indexOf("@")!=-1) {
		    		   this.numeroregistroDa=sPar.substring(0,sPar.indexOf("@"));
		    		   this.numeroregistroA=sPar.substring(sPar.indexOf("@")+1);
		    	   }
		    	   else {
		    		   throw new Exception("Attenzione! Parametro numeroRegistro non valido. Utilizzare "+_NUMEROREGISTROPAR+" <regDal>@<regAl>");
		    	   }
		       }
		       else if (parametro.equals(_CONSERVAZIONEPAR)) {
		    	   this.conservazione=sValore;
		       }
		       else if (parametro.equals(_PROPAGAFIGLIPAR)) {
		    	   this.propagaFigli=sValore;
		       }
		       else if (parametro.equals(_ESPORTASOLOFIGLIPAR)) {
		    	   this.esportaSoloFigli=sValore;
		    	   if (this.esportaSoloFigli.indexOf("@")!=-1) {
		    		   areaFiglio=this.esportaSoloFigli.substring(0,this.esportaSoloFigli.indexOf("@") );
		    		   modelloFiglio=this.esportaSoloFigli.substring(this.esportaSoloFigli.indexOf("@") + 1);
		    	   }
		       }
		       else if (parametro.equals(_IDDOCPAR)) {		    	   
		    	   sPar=sValore;
		    	   
		    	   if (sPar!=null && sPar.indexOf("@")!=-1) {
		    		   this.idDocumentoDa=sPar.substring(0,sPar.indexOf("@"));
		    		   this.idDocumentoA=sPar.substring(sPar.indexOf("@")+1);
		    	   }
		    	   else {
		    		   throw new Exception("Attenzione! Parametro idDocumento non valido. Utilizzare "+_IDDOCPAR+" <idDocDal>@<idDocAl>");
		    	   }		    	   
		       }
		       else if (parametro.equals(_PROTOINTER)) {
		    	   this.typeProtoIntern=sValore;
		    	  
		       }
		       else if (parametro.equals(_CAMPODINAMICPAR)) {
		    	   sPar=sValore;

		    	   if (sPar!=null && sPar.charAt(0)!='\'' && sPar.charAt(sPar.length()-1)!='\'' && sPar.indexOf("@")!=-1 && sPar.indexOf("#")!=-1  && sPar.indexOf("^")!=-1 && sPar.indexOf("%FINECAMPO%")!=-1) {
		    		   //String  sPar
		    		   
		    		   //sPar=sPar.substring(1,sPar.length()-1);
		    		   //System.out.println("1--->"+sPar);
		    		   
		    		   String sLista[] = it.finmatica.dmServer.util.Global.Split(sPar, "%FINECAMPO%");
		    		   
		    		   if (sLista!=null) {
		    			   for (int i=0;i<sLista.length;i++) {
		    				   String sElemento=sLista[i];
		    				   if (sElemento.length()==0) continue;
		    				   //System.out.println("2--->"+campoDinamico);
		    				   campoDinamico=sElemento.substring(0,sElemento.indexOf("#"));
				    		   tipocampoDinamico=sElemento.substring(sElemento.indexOf("#")+1,sElemento.indexOf("^"));
				    		   parDaCampoDinamico=sElemento.substring(sElemento.indexOf("^")+1,sElemento.indexOf("@"));
				    		   parACampoDinamico=sElemento.substring(sElemento.indexOf("@")+1);
				    		   
		    				   ChiaveCampi chCh = new ChiaveCampi(campoDinamico,tipocampoDinamico,parDaCampoDinamico,parACampoDinamico);
		    				   vCampiChiavi.add(chCh);		    				   				    		   
		    			   }
		    		   }
		    		   
		    		   	    		  
		    	   }
		    	   else {
		    		   throw new Exception("Attenzione! Parametro "+sPar+" non valido. Utilizzare "+_CAMPODINAMICPAR+" 'nomeCampo#tipoCampo^valoreDa@valoreA%FINECAMPO%...'");
		    	   }	
		       }			   
		       else if (parametro.equals(_ASSEGNA777CART)) {
		    	   this.assegna777Cartelle=sValore;
		       }
		       else if (parametro.equals(_ASSEGNA777FILE)) {
		    	   this.assegna777File=sValore;
		       }		       
		       else if (parametro.equals(_DATACREAZ)) {
		    	   sPar=sValore;
		    	   dataCreazioneDa=sPar.substring(0,sPar.indexOf("@"));
		    	   dataCreazioneA=sPar.substring(sPar.indexOf("@")+1);	    		  
		       }
		       
	   }
	   
	   private void loadProperties(String pathFile) throws Exception {
		   	   Properties properties = new Properties();
		   	   String legameModelli = "";
		       try {
	    	     Global g = new Global();			     
			     g.load(properties,pathFile);
		       }
		       catch (Exception e) {
		    	 throw new Exception("Errore nella lettura del file properties "+pathFile+".\nVerificarne l'esistenza. Se necessario utilizzare il file di default.\nErrore: "+e.getMessage());
		       }
		       
		       try {
		    	 urlStringOracle = properties.getProperty("URL_ORACLE");
		    	 
		    	 usrOracle  = properties.getProperty("USR_ORACLE"); 
		    	 if (usrOracle==null) usrOracle="GDM";
		    	 
		    	 passwdOracle = properties.getProperty("PSWD_ORACLE"); 
		    	 if (passwdOracle==null) passwdOracle="GDM";
		    	 
		    	 legameModelli = Global.nvl(properties.getProperty("LEGAME_MODELLI"),"");
		       }
		       catch (Exception e) {
		    	 throw new Exception("Errore nella lettura dei parametri da file "+pathFile+".\nErrore: "+e.getMessage());
		       }
		       
		       categorieModelliString=Global.nvl(properties.getProperty("CATEGORIE_MODELLI"),"");
		       
		       if (urlStringOracle==null)
		    	   throw new Exception("Attenzione! non è stato trovato il parametro URL_ORACLE");
		       
		       if (!legameModelli.trim().equals("")) {
		    	   String sLegami[] = legameModelli.split("<FINELEGAME>");   
			       for(int i=0;i<sLegami.length;i++) {
			    	   String sLegame = sLegami[i];
			    	   
			    	   String sItemLegami[] = sLegame.split("@#@"); 
			    	   
			    	   if (sItemLegami.length<3) 
			    		   throw new Exception("Attenzione! la seguente condizione di legame fra modelli: "+sLegame+" non è formulata correttamente.");
			    	   
			    	   String sConditionExtra="";
			    	   if (sItemLegami.length>3) sConditionExtra=sItemLegami[3];
			    	   
			    	   
			    	   hmsLegamiModelli.add(sItemLegami[0], new LegameStruct(sItemLegami[0],sItemLegami[1],sItemLegami[2],sConditionExtra,null));
			       }
		       }
		       
		       int i;
		       i=0;
	   }
	   
	   private ResultSet getSQLModelliFigli(LegameStruct ls,int indexSingoloPadre, String nomeTabellaOrizzontalePrincipale, boolean bReverse) throws Exception {
		   	   ResultSet rst;
		       StringBuffer sStm = new StringBuffer("");
		       
		       String sArrayTemp[] = ls.tblFiglia.split(",");
		       if (sArrayTemp==null || sArrayTemp.length<2) return null;
		    	   
		       String tabellaOrizzontale=sArrayTemp[0];
		       String campoRif=sArrayTemp[1];
		     
		       //String condition=campoRif+" IN ("+ls.listaIdDoc+")";
		       String condition=campoRif+" IN ("+ls.listaIdDocSingolo.get(indexSingoloPadre)+")";
		    
		       if (!Global.nvl(ls.condizione,"").equals(""))
		    	   condition+="AND "+ls.condizione;
		       
		       String conditionPathFile="=";
		       if (bReverse) conditionPathFile="<>";
		       
		       String rifFieldReturn="(select id_documento from "+nomeTabellaOrizzontalePrincipale+" where "+nomeTabellaOrizzontalePrincipale+"."+campoRif+"="+tabellaOrizzontale+"."+campoRif+")";
		       		       
		       sStm.append("SELECT MODELLI.CODICE_MODELLO, DOCUMENTI.ID_DOCUMENTO, OGGETTI_FILE.ID_OGGETTO_FILE,-1 id_log,TIPI_DOCUMENTO.ACRONIMO_MODELLO,"+rifFieldReturn+",AREE.AREA,nvl(AREE.ACRONIMO,'') ");
		       sStm.append("FROM "+tabellaOrizzontale+",DOCUMENTI,MODELLI,TIPI_DOCUMENTO,OGGETTI_FILE,AREE ");
		       sStm.append("WHERE "+condition+" AND DOCUMENTI.ID_DOCUMENTO="+tabellaOrizzontale+".ID_DOCUMENTO ");
		       sStm.append("AND DOCUMENTI.ID_TIPODOC = MODELLI.ID_TIPODOC ");
		       sStm.append("AND DOCUMENTI.AREA = AREE.AREA ");
		       sStm.append("AND MODELLI.ID_TIPODOC= TIPI_DOCUMENTO.ID_TIPODOC ");
		       sStm.append("AND OGGETTI_FILE.ID_DOCUMENTO = DOCUMENTI.ID_DOCUMENTO ");
		       sStm.append("AND NVL(OGGETTI_FILE.PATH_FILE,' ') "+conditionPathFile+" ' ' ");
		       if (!bReverse) sStm.append(" AND  ( nvl(dbms_lob.getLength(OGGETTI_FILE.testoocr),0) <>0  ) ");
		       sStm.append("UNION ");
		       sStm.append("SELECT MODELLI.CODICE_MODELLO, DOCUMENTI.ID_DOCUMENTO, OGGETTI_FILE_LOG.ID_OGGETTO_FILE,oggetti_file_log.id_log id_log,TIPI_DOCUMENTO.ACRONIMO_MODELLO,"+rifFieldReturn+",AREE.AREA,nvl(AREE.ACRONIMO,'') ");
		       sStm.append("FROM "+tabellaOrizzontale+",DOCUMENTI,MODELLI,TIPI_DOCUMENTO,oggetti_file_log, activity_log,AREE ");
		       sStm.append("WHERE "+condition+" AND DOCUMENTI.ID_DOCUMENTO="+tabellaOrizzontale+".ID_DOCUMENTO ");
		       sStm.append("AND DOCUMENTI.ID_TIPODOC = MODELLI.ID_TIPODOC ");
		       sStm.append("AND DOCUMENTI.AREA = AREE.AREA ");
		       sStm.append("AND MODELLI.ID_TIPODOC= TIPI_DOCUMENTO.ID_TIPODOC ");
		       sStm.append("AND activity_log.id_documento=documenti.id_documento ");
		       sStm.append("AND oggetti_file_log.ID_LOG =  activity_log.id_log ");
		       sStm.append("AND NVL(oggetti_file_log.PATH_FILE,' ') "+conditionPathFile+" ' ' ");	
		       if (!bReverse) sStm.append(" AND  ( nvl(dbms_lob.getLength(OGGETTI_FILE_LOG.testoocr),0) <>0  ) ");
		       sStm.append("ORDER BY 1,2,3,4");
		       
		       try  {
		    	 dbOp.setStatement(sStm.toString());
		    	 scriviMessaggio("SELECT FIGLIA DI area="+area+", modello="+modello+" PER TABELLA FIGLIA "+tabellaOrizzontale+"\n");
		   		 scriviMessaggio(sStm.toString()+"\n\n");
		    	 dbOp.execute();		   	     		   	   
		   	     
		   	     rst = dbOp.getRstSet();  
		   	     
		   	     return rst;
		       }
		       catch (Exception e) {
		    	 throw new Exception("Errore in esecuzione SQL per modelli figli. SQL="+sStm.toString()+"\nErrore: "+e.getMessage());
		       }
	   }
	   
	   private void printHmsChildElementListStruct() {
			   Iterator iHm = hmsChildElementList.getHashMap();
	  	    	
	  	    	if (iHm!=null) {
	  	    		while (iHm.hasNext()) {	
		   	    		String sPadre = (String)iHm.next();
		   	    		
		   	    		System.out.println("********PADRE: "+sPadre+"********\n");
		   	    		
		   	    		Iterator iHs = hmsChildElementList.getHashSet(sPadre);
		   	    		
		   	    		if (iHs!=null)
				   	    	while (iHs.hasNext()) 	{
				   	    		OggettoFileStruct ogfiStruct = (OggettoFileStruct)iHs.next();
				   	    		
				   	    		System.out.println(ogfiStruct.toString());
				   	    	}
	  	    		}
	  	    	 }
	   }
	   
	   private void manageArea(String ar, String acr) throws Exception {
		   		//TODO....da togliere
		   		//if (1==1) return;
		  // System.out.println("BBBB");
			   if (!mkDirs(mapPathFileOracle+"/"+acr,false,""))
		   		   throw new Exception("Errore in creazione directory iniziale: "+mapPathFileOracle+"/"+acr);
		   	    
		   	   
		   	   StringBuffer stm = new StringBuffer("");
		   	   
		   	   stm = new StringBuffer("CREATE OR REPLACE DIRECTORY ");
		   	   stm.append("DIR_"+acr+" AS '"+pathFileOracle+"/"+acr+"'");
		   	   
			   try {		   		    	 	   		      			        
			        dbOp.setStatement(stm.toString());
					   
					dbOp.execute();		   		    				   		    
	   		   }
	   		   catch (Exception e) {		   		    	 	
	   		    	throw new Exception("Errore creazione directory oracle. SQL= "+stm.toString()+"\nErrore: "+e.getMessage());
	   		   }		   	   
		   	   
	   		   if (idOggettoFile==null) {
			   	   stm = new StringBuffer("");
		   		   stm.append("update aree set PATH_FILE=:P_PATHTOMCAT, PATH_FILE_ORACLE=:P_PATHORCL, FORCE_FILE_ON_BLOB=1 where area=:P_AREA ");
	   		   	   		   
		   		   try {		   		    	 	   		      			        
				        dbOp.setStatement(stm.toString());
				        
				        dbOp.setParameter(":P_PATHTOMCAT", pathFileTomcat);
				        dbOp.setParameter(":P_PATHORCL", pathFileOracle);
				        dbOp.setParameter(":P_AREA", ar);
	
						dbOp.execute();		   		    				   		    
		   		   }
		   		   catch (Exception e) {		   		    	 	
		   		    	throw new Exception("Errore update path su aree. SQL= "+stm.toString()+"\nErrore: "+e.getMessage());
		   		   }			   	   
	    	   	    	   
	   		   }
	   }
	   
	   private String testModelloVerticalHorizontal(boolean bRecuperaParametri, String ar, String mod) throws Exception {
		       String sTable="";
		       
		       StringBuffer sStm = new StringBuffer("SELECT ");
		       sStm.append("F_NOME_TABELLA(:P_AREA,:P_CODICE_MODELLO) FROM DUAL");
		       
		       try {		   		    	 	   		      			        
			        dbOp.setStatement(sStm.toString());
			        
			        dbOp.setParameter(":P_AREA", ar);
			        dbOp.setParameter(":P_CODICE_MODELLO", mod);
			       
					dbOp.execute();
					
					ResultSet rst = dbOp.getRstSet();
					rst.next();
					sTable=Global.nvl(rst.getString(1),"");
	   		   }
	   		   catch (Exception e) {		   		    	 	
	   		    	throw new Exception("Errore in recupero nome tabella orizzontale per area/modello ("+ar+"/"+mod+"). SQL= "+sStm.toString()+"\nErrore: "+e.getMessage());
	   		   }
	   		   
	   		   if (!sTable.equals("") && bRecuperaParametri) {
	   			   //E' orizzontale....vedo quali parametri possono essere accettati
	   			   //se alcuni di questi non esistono per la tabella orizzontale allora 
	   			   //annullo l'eventuale parametro per evitare di metterlo nella AND
	   			   //della condizione principale per il modello principale
	   			   if (!bEsisteColonnaInTabella(sTable,"ANNO")) anno=null;
	   			   if (!bEsisteColonnaInTabella(sTable,"DATA")) dataprotocolloDa=null;
	   			   if (!bEsisteColonnaInTabella(sTable,"TIPO_REGISTRO")) tiporegistro=null;
	   			   if (!bEsisteColonnaInTabella(sTable,"DATA_REG_DETERMINA")) dataAdozioneDa=null;
	   			   if (!bEsisteColonnaInTabella(sTable,"NUMERO")) numeroregistroDa=null;
	   		   }
		       
		       return sTable;
	   }
	   
	   private boolean bEsisteColonnaInTabella(String tabella, String colonna) throws Exception {  
		       try {		   		    	 	   		      			        
			        dbOp.setStatement("SELECT "+colonna+" FROM "+tabella+" WHERE 1<>1");
			        
					dbOp.execute();										
	   		   }
		       catch (SQLException e) {	
		    	   if (e.getErrorCode()==904) return false;
		    	   else throw new Exception("Errore in test esistenza colonna "+colonna+" per tabella"+tabella+"\nErrore: "+e.getMessage());
		       }
	   		   catch (Exception e) {		   		    	 	
	   		    	throw new Exception("Errore in test esistenza colonna "+colonna+" per tabella"+tabella+"\nErrore: "+e.getMessage());
	   		   }
		       
		       return true;
	   }
	   
	   private Vector fillModelli() throws Exception {  
		   	   Vector listaArCm = new Vector();
		   	   
		   	   if (Global.nvl(categoriaModello,"").trim().equals("")) {
		   		   listaArCm.add(area+","+modello);
		   	   }
		   	   else {
		   		   try {
			   		   String sListaCategorie[] = categorieModelliString.split("<CATSEPARATOR>");
			   		   
			   		   for(int i=0;i<sListaCategorie.length;i++) {
			   			   String sCatego=sListaCategorie[i];
			   			   
			   			   if (sCatego.indexOf(categoriaModello)!=-1) {
			   				   if (sCatego.indexOf("@#@")==-1)
			   					  throw new Exception("formato non valido per la categoria ("+categoriaModello+") sul file di properties!!!!");
			   				   
			   				   String listaAreeModelli=sCatego.substring(sCatego.indexOf("@#@")+3);
			   				   String arrayListaCategorie[] = listaAreeModelli.split("@");
			   				   
				   				for(int j=0;j<arrayListaCategorie.length;j++) {
				   					if (arrayListaCategorie[j].indexOf(",")==-1)
				   						throw new Exception("formato non valido per la categoria ("+categoriaModello+") per la coppia (area,modello)="+arrayListaCategorie[j]+" sul file di properties!!!!");
				   					listaArCm.add(arrayListaCategorie[j]);
				   				}
			   			   }
			   		   }
			   		   
			   		   if (listaArCm.size()==0)
			   			  throw new Exception("Attenzione non è stata trovata alcuna categoria ("+categoriaModello+") sul file di properties!!!!");
		   		   }
			   	   catch(Exception e) {
			   		   throw new Exception("FileFsFromDb::fillModelli - "+e.getMessage());
			   	   }
		   	   }
		   	   
		   	   return listaArCm;
	   }
	   
	   private String insertOggettoFile(String nomeFile, String idDocumento) throws Exception {
		   	   try {		   		   
		   		 ResultSet rst;
		   		 String idObjFile;
		   		 String idFormato="0";
			     StringBuffer sStmBuf = new StringBuffer(""); 
			   
			   	 sStmBuf.append("SELECT ID_FORMATO ");
			   	 sStmBuf.append("FROM FORMATI_FILE ");
			   	 sStmBuf.append("WHERE UPPER(NOME)=UPPER(SUBSTR(:P_NOMEFILE,");
			   									sStmBuf.append("INSTR(:P_NOMEFILE, '.', -1) + 1,");
			   									sStmBuf.append("LENGTH(:P_NOMEFILE)))");
			   	   
				try {
					dbOp.setStatement(sStmBuf.toString());
				   	   
					dbOp.setParameter(":P_NOMEFILE", nomeFile);
					
			   	    dbOp.execute();			   	  			   	  
			   	     
			   	    rst = dbOp.getRstSet();
			   	     
			   	    if (rst.next()) idFormato=""+rst.getLong(1); 
			   	    	 			   	    
				}
				catch(Exception e) {
					throw new Exception("Errore in recupero idformato - Errore: "+e.getMessage());
				}
				
				try {
				  idObjFile=dbOp.getNextKeyFromSequence("OGG_FILE_SQ")+"";   
				}
				catch(Exception e) {
					throw new Exception("Errore in recupero valore da sequence OGG_FILE_SQ - Errore: "+e.getMessage());
				}
				
				sStmBuf = new StringBuffer(""); 
				sStmBuf.append("INSERT INTO OGGETTI_FILE " );
			    sStmBuf.append("(ID_OGGETTO_FILE, ID_DOCUMENTO,");
			    sStmBuf.append("ID_FORMATO, FILENAME, ALLEGATO, DATA_AGGIORNAMENTO,");
			    sStmBuf.append("UTENTE_AGGIORNAMENTO) ");
			    sStmBuf.append("VALUES ");
			    sStmBuf.append("(:P_ID, :P_IDDOC,  ");				
			    sStmBuf.append(":P_ID_FORMATO, :P_FILENAME,'N',SYSDATE,'GDM')  ");
			    
			    try {
			    	dbOp.setStatement(sStmBuf.toString());
			        
			    	dbOp.setParameter(":P_ID",Long.parseLong(idObjFile) );
			    	dbOp.setParameter(":P_IDDOC",Long.parseLong(idDocumento) );
			    	dbOp.setParameter(":P_ID_FORMATO",Long.parseLong(idFormato) );
			    	dbOp.setParameter(":P_FILENAME",nomeFile );
			    	
					dbOp.execute();	
				}
				catch(Exception e) {
					throw new Exception("Errore in Esecuzione insert nella tabella oggetti_file - Errore: "+e.getMessage());
				}
			    
			   	return idObjFile;							
		   	  }
		   	  catch(Exception e) {
		   		throw new Exception("FileFsFromDb::insertOggettoFile("+nomeFile+","+idDocumento+") - "+e.getMessage());
		   	  }
		   	   
		   	   
	   }
	   
}

class OggettoFileStruct {
	  String idObjFile, idDocumento, codiceModello, idLog, acronimoModello, area, acronimoArea;
	  boolean padreGiaTrascodificato=false;
	  String idObjFileExtern;	  	  
	  
	  public OggettoFileStruct(String idObj, String idDoc, String cm, String idL, String acrModello, String ar, String acrArea) {
		     this(idObj,idDoc,cm,idL,acrModello);
		     
		     area=ar;
		     acronimoArea=acrArea;
	  }
	  
	  public OggettoFileStruct(String idObj, String idDoc, String cm, String idL, String acrModello, boolean isPadreTrascoChildNo, String idExt) {
		  	 this(idObj,idDoc,cm,idL,acrModello,isPadreTrascoChildNo);
		  	 
		  	 idObjFileExtern=idExt;
	  }
	  
	  public OggettoFileStruct(String idObj, String idDoc, String cm, String idL, String acrModello, boolean isPadreTrascoChildNo) {
		  	 this(idObj,idDoc,cm,idL,acrModello);
		  	 
		  	 padreGiaTrascodificato=isPadreTrascoChildNo;
	  }
	  
	  public OggettoFileStruct(String idObj, String idDoc, String cm, String idL, String acrModello) {
		  	 idObjFile=idObj;
		  	 idDocumento=idDoc;
		  	 codiceModello=cm;
		  	 idLog=idL;
		  	 acronimoModello=acrModello;
		  	 
		  	
	  }
	  
	  public String toString() {
		     String sRet;
		     
		     sRet="[OgfiStruct]\n";
		     sRet+="Area = "+area+"\n";
		     sRet+="acronimoArea = "+acronimoArea+"\n";
		     sRet+="codiceModello = "+codiceModello+"\n";
		     sRet+="acronimoModello = "+acronimoModello+"\n";
		     sRet+="idDocumento = "+idDocumento+"\n";
		     sRet+="idObjFile = "+idObjFile+"\n";
		     sRet+="idLog = "+idLog+"\n";
		     sRet+="padreGiaTrascodificato = "+padreGiaTrascodificato+"\n\n";
		     
		     return sRet;
	  }
}

class LegameStruct {
	  String arCm, tblFiglia, condizione, listaIdDoc, arCmFiglio;
	  Vector<String> listaIdDocSingolo; 
	  
	  public LegameStruct(String areaCm,String areaCmFiglia, String tabella,String cond,String lista) {
		  	 arCm=areaCm;
		  	 tblFiglia=tabella;
		  	 condizione=cond;
		  	 listaIdDoc=lista;
		  	 arCmFiglio=areaCmFiglia;
		  	 listaIdDocSingolo= new Vector<String>();
	  }
}

class ChiaveCampi {
	  String campoDinamico,tipocampoDinamico,parDaCampoDinamico,parACampoDinamico;
	  
	  public ChiaveCampi(String campo, String tipo, String parDa, String parA) {
		     campoDinamico=campo;
		     tipocampoDinamico=tipo;
		     parDaCampoDinamico=parDa;
		     parACampoDinamico=parA;
	  }
}
