package it.finmatica.dmServer;

import it.finmatica.dmServer.gdmSyncro.GDMSyncroCore;
import it.finmatica.dmServer.util.DMActivity_Log;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.util.IoExtendUtility;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.io.LetturaScritturaFileFS;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.util.Vector;

public class GD4_GestoreOggettiFile_FS {	   
	   private Environment vEnv;
	   private Vector<GD4_Oggetti_File> vObjFile;
	   private Vector<GD4_Oggetti_File> vAllObjFile;
	   
	   private DMActivity_Log dmALog; 
	   private Vector listaIdFileToLog = new Vector();
	  
	   private String pathFileArea="";
	   private String arcmcr="";
	   private String idDoc="";
	   
	   private boolean bogfilog=true;
	   
	   public GD4_GestoreOggettiFile_FS(Environment ev) throws Exception {		
		   	  vEnv=ev;
		   	  vObjFile= new Vector<GD4_Oggetti_File>();		   	 
	   }
	   
	   public void syncroFs() throws Exception {
		      syncroFs(1);
	   }
	   
	   public void syncroFs(int compCancellacancellaAllegati) throws Exception {
		      //I file sono gestiti solo come BLOB nell'area
		      if (pathFileArea!=null && !pathFileArea.equals("")) {		   
		      
			      //Ciclo sui <vObjFile> per:
			      //Per trattare quelli da inserire o modificare o rinominare
			      //Per trattare quelli da cancellare devo andare sul DB
			      //perché ci sono da trattare gli eventuali figli
	
			      //In effetti la ragione per la quale passo gli oggetti_file
			      //su insert e update è perché dentro contengono gli inputstream
			      //da inserire nel FS
			      //In delete invece gli inputStream non mi servono ed in più
			      //vengono cancellati anche i figli di 1 padre...quindi devo
			      //andare sul db per forza per uscirmi la lista
			   
			      
			 	  //Ciclo per insert/update
			   	  try {
				  
				   	  for(int i=0;i<vObjFile.size();i++) {
				   		 GD4_Oggetti_File obj = vObjFile.get(i);		
				   		  //Controllo se sono in rename
			             InputStream isFile;
			             String file, fileToRen;
			             //NON SONO IN RENAME
			             if (obj.getOldFileName().equals("")) {
			            	 isFile=(InputStream)obj.getFile(false); 
			            	 file=obj.getFileName();
			            	 fileToRen="";
			             }
			             //SONO IN RENAME
			             else {
			            	 isFile=(InputStream)obj.getOldFile();
			            	 
			            	 obj.closeFile(true);
			            	 
			            	 file=obj.getFileName();
			            	 fileToRen=obj.getOldFileName();
			            	 obj.setFile(isFile);			            	 
			             }
			             try {		      
			               //mod del 31/01/2011. il nome del file è sempre = id_oggetto_file
			               fileToRen="";
			               file = obj.idOggettoFile;

			               scriviFileFs(isFile,obj.getPathObjFile(),file,fileToRen,obj.getPercorsoFileFS(),obj.getPathFileArea());
			               obj.setOggettoFileTemp(false);
			               obj.setOldFileName("");
			             }
			             catch (Exception ei) {
			               throw new Exception("Errore nella scrittura file: "+file+" - "+ei.getMessage());
			             }
			             		             
				   	  }
			   	  }
				  catch (Exception e) {					  
					  throw new Exception("GD4_GestoreOggettiFile_FS::syncroFs - Gestione Insert/Update/Rename\n" + e.getMessage());
				  }
		      }
			  
		      IDbOperationSQL dbOp = null;
			  
		   	  //Ciclo per delete
		      String idSyncro;
			  try {
				dbOp = vEnv.getDbOp();  
				
				StringBuffer sStmToDelete = new StringBuffer("");
				
				sStmToDelete.append("SELECT OGGETTI_FILE.FILENAME, nvl(OGGETTI_FILE.PATH_FILE,''),id_oggetto_file, OGGETTI_FILE.ID_SYNCRO, ID_SERVIZIO_ESTERNO, nvl(OGGETTI_FILE.PATH_FILE_ROOT,''), ");
				sStmToDelete.append(" nvl(AREE.PATH_FILE,'') ");
				sStmToDelete.append("FROM OGGETTI_FILE, DOCUMENTI,AREE ");
				sStmToDelete.append("WHERE DA_CANCELLARE='S' AND OGGETTI_FILE.ID_DOCUMENTO= "+idDoc+" AND DOCUMENTI.ID_DOCUMENTO = OGGETTI_FILE.ID_DOCUMENTO");
				sStmToDelete.append("      AND DOCUMENTI.AREA = AREE.AREA ");
				
				dbOp.setStatement(sStmToDelete.toString());
	            
	            dbOp.execute();
	            
	            ResultSet rst = dbOp.getRstSet();
	            
	            //OTTENGO LA LISTA DEGLI OGGETTI FILE DA CANCELLARE
	            Vector<String> vLista = new Vector<String>();
	            Vector<fileInfo> vListaFInfo = new Vector<fileInfo>();
	            while (rst.next()) {
	            	fileInfo f = new fileInfo(rst.getString(1),Global.nvl(rst.getString(2),""),
						rst.getString(3),rst.getString(4), rst.getString(5), rst.getString(6), rst.getString(7));
	            	
	            	vListaFInfo.add(f);	            	
	            	vLista.add("'"+rst.getString(1).replaceAll("'","''")+"'");
	            }	            
	            
	            if (compCancellacancellaAllegati==0 && vLista.size() >0) {
	            	String error=Global.CODERROR_SAVEDOCUMENT_COMPALLEGATI;	            		            		            
	          	    throw new Exception(error);
	            }	          	    
	            
	            //SCRIVO SU DB PRIMA DI CANCELLARLI FISICAMENTE COSì SE SI VERIFICA
	            //QUALCHE PROBLEMA NELLA CANCELLAZIONE FISICA, TUTTO QUESTO VERRA' ROLLBACCATO (HI HI HI, consentitemelo...:-)) )
	            if (dmALog!=null && vLista.size()>0) {
	            	try {
	      	          if (bogfilog) dmALog.insertAllOgfiLog(idDoc,vLista,false,true,false);
	      	        }    
	      	        catch (Exception e) {        	
	      	     	  throw new Exception("Errore in scrittura Oggetti File Log\n"+e.getMessage());
	      	        }
	            }
	            
	            try { 	
		            sStmToDelete = new StringBuffer("");
		            sStmToDelete.append("DELETE IMPRONTE_FILE WHERE FILENAME IN (");
					sStmToDelete.append("SELECT FILENAME ");
					sStmToDelete.append("FROM OGGETTI_FILE ");
					sStmToDelete.append("WHERE DA_CANCELLARE='S' AND ID_DOCUMENTO= "+idDoc+") AND ID_DOCUMENTO= "+idDoc);
					
					dbOp.setStatement(sStmToDelete.toString());
		            
		            dbOp.execute();					
	            }
				catch (Exception e) {        	
	      	     	  throw new Exception("Errore in cancellazione Impronta Oggetti File \n"+e.getMessage());
	      	    }			            

	            try { 	
		            sStmToDelete = new StringBuffer("");
		            sStmToDelete.append("DELETE OGGETTI_FILE WHERE ID_OGGETTO_FILE IN (");
					sStmToDelete.append("SELECT ID_OGGETTO_FILE ");
					sStmToDelete.append("FROM OGGETTI_FILE ");
					sStmToDelete.append("WHERE DA_CANCELLARE='S' AND ID_DOCUMENTO= "+idDoc+") ");
					
					dbOp.setStatement(sStmToDelete.toString());
		            
		            dbOp.execute();					
	            }
				catch (Exception e) {        	
	      	     	  throw new Exception("Errore in cancellazione Oggetti File \n"+e.getMessage());
	      	    }							
	            //FINE SCRITTURA SU DB
	            
	            if (pathFileArea!=null && !pathFileArea.equals("")) {	            
		            //CICLO PRIMA DELLA DELETE FISICA - (BACKUP DEI FILE DA CANCELLARE)
		            backupFS(vListaFInfo);
		            	           
		            //CICLO PER LA DELETE FISICA
		            try {
			            for (int i=0;i<vListaFInfo.size();i++) {	            	 
			            	  String fileName=vListaFInfo.get(i).idObjFile/*fileName--> mod del 31/01/2011*/;
			            	  String pathFile=vListaFInfo.get(i).pathFile;
			            	  String pathRoot=vListaFInfo.get(i).pathFileRoot;
			            	  
			            	  //Se il file è gestito da FS lo devo cancellare anche su FS
			            	  if (pathFile!=null && !pathFile.equals("")) {
			            		  //Chiudo gli eventuali InputStream Aperti
			            		  closeOpenIs(vListaFInfo.get(i).idObjFile);
			            		  
			            		  deleteFileFs(pathRoot,fileName,pathFile,true,true,bogfilog,vListaFInfo.get(i).pathFileAreaAree);
			            	  }
			            }
	      	        }    
	      	        catch (Exception e) {   
	      	          //Ripristino il file system con il backup che mi sono creato	
	      	          restoreFS(vListaFInfo);
	      	          cleanBackupFile(vListaFInfo);
	      	          cleanLogDir(vListaFInfo);
	      	     	  throw new Exception("Cancellazione fisica dei file\n"+e.getMessage());
	      	        }		            
		            //FINE CICLO DELETE FISICA
		            
				    //Elimino i backup che ho creato fino ad ora
				    cleanBackupFile(vListaFInfo);
	            }
	           
	            
	            //Testo il Tipo servizio esterno e chiamo il suo servizio di delete
            	for (int i=0;i<vListaFInfo.size();i++) {
            		  String idFile=vListaFInfo.get(i).idObjFile;
            		  String idSyncroFile=vListaFInfo.get(i).idSyncro;
            		  String isServizioEsterno=vListaFInfo.get(i).idServizioEsterno;
            		  
            		  if (!Global.nvl(idSyncroFile, "").equals("")) {
            			  try {
	            			  GDMSyncroCore gdmSyncroCore = new GDMSyncroCore(isServizioEsterno,idSyncroFile,idDoc,idFile,this.vEnv);
	      	            	
	      	            	  gdmSyncroCore.syncro(null);
            			  }
            			  catch (Exception e) {						
        					throw new Exception("Errore in delete allegato con servizio esterno: idSyncro="+idSyncroFile+
        										 ", idServizioEsterno="+isServizioEsterno+", idOggettoFile="+idFile+"\n" + e.getMessage());
        				  }
            		  }
            	}
		   	  }
			  catch (Exception e) {						
				throw new Exception("GD4_GestoreOggettiFile_FS::syncroFs - Gestione Delete\n" + e.getMessage());
			  }
		   	  //Fine ciclo per delete			  
	   }
	   
	   public void addObjFile(GD4_Oggetti_File vObj) {
		   	  vObjFile.add(vObj);
	   }
	   	   
	   /**
	    * Metodo che effettua il backup dei file da cancellare
	    * 	
		* @return
		* @throws Exception
	   */
	   private void backupFS(Vector<fileInfo> elencoFile) throws Exception {		   	   
		   	   for (int i=0;i<elencoFile.size();i++) {	
		   		   if (Global.nvl(elencoFile.get(i).pathFile,"").equals("")) continue;

		   		   
			   	   String dir=((Global.nvl(elencoFile.get(i).pathFileRoot,"").equals(""))?elencoFile.get(i).pathFileAreaAree:elencoFile.get(i).pathFileRoot)+
			   	   			  File.separator+elencoFile.get(i).pathFile+
			   	   			  File.separator+arcmcr;
			   	   String nomeFile=elencoFile.get(i).idObjFile /*.fileName---->mod del 31/01/2011*/; 
			   	   String nomeFileBck=nomeFile+"_BACK";
			   	   
			   	   Vector<fileInfo> vBckFileAppoggio = new Vector<fileInfo>();
			   	   
			   	   try {			   	   
				       LetturaScritturaFileFS f = new LetturaScritturaFileFS(dir+
				    		   												File.separator	+
		                                          						     nomeFile);
		
				       InputStream isFile = f.leggiFile();	
				       
				       LetturaScritturaFileFS fBck = new LetturaScritturaFileFS(dir+
				    		   													File.separator+
							     												nomeFileBck);
				       
				       fBck.scriviFile(isFile);
				       
				       vBckFileAppoggio.add(new fileInfo(nomeFile,elencoFile.get(i).pathFile,"","","", elencoFile.get(i).pathFileRoot,elencoFile.get(i).pathFileAreaAree));
				       
					   isFile.close();					   
			   	   }
				   catch (Exception e) {
					   //Elimino i backup che ho creato fino ad ora
					   cleanBackupFile(vBckFileAppoggio);
					   
	   			       throw new Exception("Errore nell'effettuare il backup del file "+nomeFile);
	   			   }
		   	   
		   	   }   	   
	   }	   	  
	   
	   /**
	    * Metodo che effettua il ripristino dei file
	    * dal loro backup
	    * 
		* @return
		* @throws Exception
	   */	   
	   private void restoreFS(Vector<fileInfo> elencoFile) {		   	   		   	   
		   	   for (int i=0;i<elencoFile.size();i++) {
		   		   if (Global.nvl(elencoFile.get(i).pathFile,"").equals("")) continue;
			   	   
		   		   String dir=((Global.nvl(elencoFile.get(i).pathFileRoot,"").equals(""))?elencoFile.get(i).pathFileAreaAree:elencoFile.get(i).pathFileRoot)+
					   		File.separator+elencoFile.get(i).pathFile+
					   		File.separator+arcmcr;
			   	   String nomeFile=elencoFile.get(i).idObjFile/*.fileName---> mod del 31/01/2011*/; 
			   	   String nomeFileBck=nomeFile+"_BACK";
			   	   
			   	   File fFileToRestore = new File(dir+
			   			   							File.separator+
			   			 					      nomeFile);
			   	   
			   	   //Se non esiste significa che lo devo 
			   	   //ripristinare perché è stato cancellato
			   	   if (!fFileToRestore.exists()) {
			   		   File fFileToDel = new File(dir+
			   				 File.separator+
		                     nomeFileBck);			
 
			   		   File fFileToRen = new File(dir+
			   				 File.separator+
		                     nomeFileBck.substring(0,nomeFileBck.length()-"_BACK".length()));
		  			  
			   		   fFileToDel.renameTo(fFileToRen);
			   	   }
			   	   
		       }
	   }	
	   
	   /**
	    * Metodo che effettua la cancellazione dei file di backup
	    * 
		* @return
		* @throws Exception
	   */		   
	   private void cleanBackupFile(Vector<fileInfo> elencoFile) throws Exception {
		       try {		   
			   	   for (int iBck=0;iBck<elencoFile.size();iBck++) {	
			   		    if (Global.nvl(elencoFile.get(iBck).pathFile,"").equals("")) continue;
			   		    
				        deleteFileFs(elencoFile.get(iBck).pathFileRoot,elencoFile.get(iBck).idObjFile/*.fileName-->mod del 31/01/2011*/+"_BACK",
						elencoFile.get(iBck).pathFile,
						false,
						false,
						true,
							elencoFile.get(iBck).pathFileAreaAree);
	 		       } 
			   }
			   catch (Exception ei) {
				   //DONTCARE
			   }			   	   
	   }	   
	   
	   
	   /**
	    * Metodo che effettua la cancellazione della directory di log
	    * 
		* @return
		* @throws Exception
	   */		   
	   private void cleanLogDir(Vector<fileInfo> elencoFile) {
		   for (int iBck=0;iBck<elencoFile.size();iBck++) {	
			    if (Global.nvl(elencoFile.get(iBck).pathFile,"").equals("")) continue;
		       
			   String dir=((Global.nvl(elencoFile.get(iBck).pathFileRoot,"").equals(""))?elencoFile.get(iBck).pathFileAreaAree:elencoFile.get(iBck).pathFileRoot)+
						   File.separator+elencoFile.get(iBck).pathFile+
						   File.separator+arcmcr;
			   
		       if (dmALog!=null) {		    	   
		   		   int idLog=dmALog.getId_log();   
		   		  
		   		   String dirLog=dir+File.separator+"LOG_"+idLog;
		   			
		   		   IoExtendUtility ioe = new IoExtendUtility();
		   		   ioe.DelDir2(new File(dirLog));
		   	  }
		   }
	   }		   

	   public void setPathFileArea(String pathFileArea) {
			  this.pathFileArea = pathFileArea;
	   }
	
	   public void setArcmcr(String arcmcr) {
		      this.arcmcr = arcmcr;
	   }

	   
	   private void scriviFileFs(InputStream isFile, String pathObjFile, String fileName, String frename, String pathFileOrigin, String pathFileAreaObjFile) throws Exception {
	  	       String pathFileAreaLocal;

			   if (Global.nvl(pathFileAreaObjFile,"").equals("")) {
				   pathFileAreaLocal=pathFileArea;
			   }
			   else {
				   pathFileAreaLocal=pathFileAreaObjFile;
			   }

	   		   String nomeFile;
		       String dir=pathFileAreaLocal+
					       File.separator+pathObjFile+
					       File.separator+arcmcr;		      
		  	  
			   File fDir = new File(dir);
			   //fDir.setReadable(true, true);
			   //fDir.setWritable(true, true);
			   if (!fDir.exists()) fDir.mkdirs();
			  
			   
			   String dirLog=null;
			   if (dmALog!=null) {
				   int idLog=dmALog.getId_log();   		   		 
				   dirLog=dir+File.separator+"LOG_"+idLog;   
			   }
			   
			   boolean bPrimaVolta=true;
			   
			   //Scrittura evenuale Log (altre volte)
			   if ( dmALog!=null && !(Global.nvl(dmALog.getTypeLogFile(),Global.TYPE_NO_LOG).equals(Global.TYPE_NO_LOG)) && (new File(dir+File.separator+fileName)).exists()) {			   		 			   	   			   						   
	              scriviLog(dirLog,dir+File.separator+fileName,fileName,false);	
	              bPrimaVolta=false;
		   	   }			   
			  		  
			   //LA RENAME NON C'è + perché il nome del file su FS=id_oggetto_file
			   //quindi se rinomini cambia solo il contenuto...il file si chiamerà uguale
			   //la rinomina coinvolgerà solamente il Filename e non il file su FS
			   /*if (!frename.equals("")) {			  
				  File fFileToDel = new File(dir+
						                     "/"+
						                     fileName);			
				  
				  File fFileToRen = new File(dir+
						                     "/"+
						                     frename);
						  			  
				  if (!(fFileToDel.renameTo(fFileToRen)))
					  throw new Exception("Errore! in scriviFileFs: Non riesco a rinominare il file: "+fileName);
				  
				  nomeFile=frename;
				  LetturaScritturaFileFS f = new LetturaScritturaFileFS(dir+
													                     "/"+
													                     frename);
				  f.scriviFile(isFile);						 				 
				  isFile.close();					  
			   }
			   else {		*/	  
				  
			  /* System.out.println("PATHFILE-->"+dir+"/"+ fileName);
			   ByteArrayOutputStream bos = new ByteArrayOutputStream(); 

	            //lettura dallo stream 
	            int r; 
	            while ((r = isFile.read()) != -1) 
	            { 
	            	System.out.println("LEGGO");
	                bos.write(r); 
	            } 

				
			   System.out.println("available--->"+isFile.available());*/
			   nomeFile=fileName;
			   if (pathFileOrigin==null) {
				   //Se il percorso di origine è vuoto significa che mi arriva lo stream da db o da altro punto che nn sia l'allegati temp da FS

				  copyInputStreamToFile(isFile,dir+ File.separator+ fileName);

			   }
			   else {
				   //Altrimenti significa che mi sta arrivando direttamente dall'allegati_temp che ha posato il file su FS....posso fare la move
				   isFile.close();
				   
				   File fOrigin= new File(pathFileOrigin);
				   File fDest = new File(dir+File.separator+fileName);
				   if (!(fOrigin.renameTo(fDest))) throw new Exception("Attenzione! Errore nello spostare da "+pathFileOrigin+" a "+dir+File.separator+fileName); 
				   
			   }
				//  System.out.println("OK");
			  /*}*/
			   
			   
			   
			   //Scrittura evenuale Log (prima volte)
			   if (dmALog!=null && bPrimaVolta && !(Global.nvl(dmALog.getTypeLogFile(),Global.TYPE_NO_LOG).equals(Global.TYPE_NO_LOG))) {			   		 			   	   			   						   
	              scriviLog(dirLog,dir+File.separator+fileName,fileName,false);		              
		   	   }
			  			  			   
	  }

	private void copyInputStreamToFile(InputStream inputStream, String pathFile)
		throws Exception {

	   	File file = new File(pathFile);
		File fileTmp = new File(pathFile+"_tmp");

		FileOutputStream outputStream;
		boolean bEsisteva=false;
		if (file.exists()) {
			outputStream = new FileOutputStream(fileTmp);
			bEsisteva=true;
		}
		else {
			outputStream = new FileOutputStream(file);
		}


		int read;
		byte[] bytes = new byte[1024];

		while ((read = inputStream.read(bytes)) != -1) {
			outputStream.write(bytes, 0, read);
		}

		outputStream.close();
		try {inputStream.close();}catch (Exception e){}

		if (bEsisteva) {
			copyFileUsingStream(fileTmp,file);
			fileTmp.delete();
		}

	}

	private void copyFileUsingStream(File source, File dest) throws IOException {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new FileInputStream(source);
			os = new FileOutputStream(dest);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} finally {
			is.close();
			os.close();
		}
	}
	   
	  private void deleteFileFs(String pathFileAreaRootInOggettoFile, String nomeFile, String pathObjFile, boolean bScriviLog, boolean bErrorBloccante, boolean bLogFile,String pathFileAreaAree) throws Exception {
		  	  String dir=((Global.nvl(pathFileAreaRootInOggettoFile,"").equals(""))?pathFileAreaAree:pathFileAreaRootInOggettoFile)+
					  	File.separator+pathObjFile+
					  	File.separator+arcmcr;

		  	  if (bScriviLog) {
			      if (dmALog!=null && bLogFile) {		    	   
			   		   int idLog=dmALog.getId_log();   
			   		  
			   		   String dirLog=dir+File.separator+"LOG_"+idLog;
			   			
			   		   scriviLog(dirLog,dir+File.separator+nomeFile,nomeFile,true);		   
			   	  }	   	   
		  	  }
		 
			  File fFileToDel = new File(dir+
					  					File.separator+
					                     nomeFile);
			  
			  if (!fFileToDel.delete())
				  if (bErrorBloccante) throw new Exception("Errore! in deleteFileFs: Non riesco a cancellare il file:"+nomeFile);	   		 
			  
	  }
	  
	  public void scriviLog(String dirLog, String pathFileOrigin, String nomeFile, boolean bScriviSempre) throws Exception {		  	  
		  	  try {
		  		  if (!bScriviSempre) {
		  			  if (!isFileInObjLogVector(nomeFile)) return;
		  		  }
		  		  
		  		  File fDirLog = new File(dirLog);
		  	  
			  	  if (!fDirLog.exists()) fDirLog.mkdirs();
			  	
			  	  LetturaScritturaFileFS fOrigine = new LetturaScritturaFileFS(pathFileOrigin); 	
			  	  LetturaScritturaFileFS fDestinazione = new LetturaScritturaFileFS(dirLog+File.separator+nomeFile);
			  
			  	  InputStream isOrigine=fOrigine.leggiFile();
			  	  fDestinazione.scriviFile(isOrigine);
			  	  isOrigine.close();			  	
			   }
			   catch(Exception e) {
				   throw new Exception("\nscriviLog - Errore in scrittura log del file "+nomeFile+" Errore: "+e.getMessage());
			   }
	  }
	  
	  private void closeOpenIs(String idObjFile) throws Exception {
		      if (vAllObjFile==null) return;
		      
		      for(int i=0;i<vAllObjFile.size();i++) {
		    	  GD4_Oggetti_File oFile = (GD4_Oggetti_File)(vAllObjFile.get(i));
		    	  if (oFile.getIdOggettoFile().equals(idObjFile)) {
            		  oFile.closeFile(false);
		    	  }
		      }
	  }

	  public String getIdDoc() {
		     return idDoc;
	  }

	  public void setIdDoc(String idDoc) {
		     this.idDoc = idDoc;
	  }

	  public Vector<GD4_Oggetti_File> getVAllObjFile() {
		     return vAllObjFile;
	  }

	  public void setVAllObjFile(Vector<GD4_Oggetti_File> allObjFile) {
		     vAllObjFile = allObjFile;
	  }

	  public void setDmALog(DMActivity_Log dmALog) {
		     this.dmALog = dmALog;
	  }  	   
	  
	  public void setListaIdFileToLog(Vector listaIdFileToLog) {
		     this.listaIdFileToLog = listaIdFileToLog;
	  }
	  
	  private boolean isFileInObjLogVector(String filename) {		    
		      for(int i=0;i<this.listaIdFileToLog.size();i++)
		    	  if (this.listaIdFileToLog.get(i).equals(filename)) return true;
		      
		      return false;
	  }
	   
	  public void setOgfiLog(boolean bogfilog) {
			this.bogfilog = bogfilog;
	  }   
}

class fileInfo {
	  public String fileName,pathFile,idObjFile, idSyncro, idServizioEsterno, pathFileRoot, pathFileAreaAree;
	  
	  public fileInfo(String fileName,String pathFile,String idObjFile, String idSyncro, String idServizioEsterno, String pathFileRoot, String pathFileAreaAree) {
		     this.fileName=fileName;
		     this.pathFile=pathFile;
		     this.idObjFile=idObjFile;		    
		     this.idSyncro=idSyncro;
		     this.idServizioEsterno=idServizioEsterno;
		     this.pathFileRoot = pathFileRoot;
		     this.pathFileAreaAree=pathFileAreaAree;
	  }	  	  
}
