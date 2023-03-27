package it.finmatica.dmServer.SOA;

import it.finmatica.dmServer.util.Global;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.jfc.io.LetturaScritturaFileDB;
import it.finmatica.jfc.io.LetturaScritturaFileFS;

import java.io.File;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;

import javax.servlet.http.HttpServletRequest;

import it.finmatica.jfc.utility.*;

public class SOAIUploadDownload {
	
	private IDbOperationSQL	dbOperation	= null;
	private String nomeIcona;
	private String modificataIcona;
	HttpServletRequest  req=null;

	public SOAIUploadDownload() {}
	
	//Costructor for SOA
	public SOAIUploadDownload(IDbOperationSQL dbOp,HttpServletRequest newreq) {
		   dbOperation=dbOp;
		   req=newreq;
	}
	
	
	public String upload(String tipoUPLOAD, String tableAttachName,String columnAttachName,String columnAttach,
						 String whereAttachCondition,String fileAttachSorg, String fileAttachDest) throws Exception {	
		   
			/** Upload da DB */	
		   if(tipoUPLOAD.equals("DB")){
			   try {
		    		uploadDB(tableAttachName,columnAttachName,columnAttach,whereAttachCondition,fileAttachDest);
		    	 }
		    	 catch (Exception e) {
		    		  try {dbOperation.close();}catch (Exception eClose) {}
		    		  throw e;
		    	 }
		   }
		   else/** Upload da FS */
			 if(tipoUPLOAD.equals("FS")){
			  	try {
			  		uploadFS(fileAttachSorg,fileAttachDest);
			  	}
			  	catch (Exception e) {
			  		try {dbOperation.close();}catch (Exception eClose) {}
			  		throw e;
			  	}
			 }
			 else /** Upload da FS parziale */
			  if(tipoUPLOAD.equals("FSP")){
				try {
					uploadFSP(fileAttachDest,tableAttachName,columnAttachName,columnAttach,whereAttachCondition);
				}
				catch (Exception e) {
				  try {dbOperation.close();}catch (Exception eClose) {}
				  throw e;
				}
			  }
		    return "OK"; 
	 }
	 
	/** Restituisce il nome di un file 
	 * 
	 * @param String path il path del file 
	 */
	public String getFileName(String path) {
		   File f = new File(path);
		   return f.getName();
	}
	
	/** Restituisce il contenuto di un file
	 *  in fromato String per visualizzarlo
	 *  in un campo
	 * 
	 * @param String path il path del file 
	 */
	public String getContentFile(String path) throws Exception {
		   String content="";
		   try {
			LetturaScritturaFileFS f= new LetturaScritturaFileFS(path);
			content = new String(Global.getBytesToEndOfStream(f.leggiFile()));
			File fs=new File(path);
			String dir=fs.getParent();
			String msg= FileSystemUtils.clearDir(dir);
	 		return content;
		   }
		   catch (Exception e) {
			 throw new Exception("getContentFile - Error: "+e.getMessage());
		   }
	}
	
	
	/** Restituisce il path completo 
	 * 
	 * @param  String path 		il path del file 
	 * @param  String nomeFile 	nome del File
	 * @return String path completo
	 */
	public String getPath(String path,String nomeFile) {
		   return path + File.separator + nomeFile;
	}
	
	/** Restituisce InputStream di un file 
	 * 
	 * @param  String path 		il path del file 
	 */
	public InputStream getInputStreamFile(String path) throws Exception {
		   try {
			LetturaScritturaFileFS fsSorg = new LetturaScritturaFileFS(path);
		    return fsSorg.leggiFile();
		   }
		   catch (Exception e) {
			 throw new Exception("getInputStreamFile - Error: "+e.getMessage());
		   }
	}
	 
	 /** OPERAZIONE DI UPLOAD */
	 
	 /**
	   * Upload del file nel file system
	   * 
	   * @param String pathSorg		path sorgente da cui prelevare il nome del file
	   * @param String pathDest		path destinazione in cui creare il file
	   * 
	 */
	 private void uploadFS(String pathSorg,String pathDest) throws Exception {
			      
		 	 try {
	 		  File fSorg=null,fDest=null;
		      String nameFile,path;
		      InputStream is=null;
		      LetturaScritturaFileFS writer = null;
		     
		      /** Nome del file */
		      fSorg = new File(pathSorg);
		      nameFile=fSorg.getName();
		     
		      /** Path completo di destinazione */
		      path=pathDest + File.separator+nameFile;
		     
		      /** InputStream del file associato */
		      LetturaScritturaFileFS fsSorg = new LetturaScritturaFileFS(pathSorg);
		      is=fsSorg.leggiFile();
		     
		      /** Creazione del file */
		      fDest = new File(path);
		     
		      if(fDest.exists())
		       fDest.delete();
		      else
		      {	
		       if (!fDest.isDirectory())
		    	 fDest.mkdirs(); 
		      }
		      writer = new LetturaScritturaFileFS(path);
		      writer.scriviFile(is);	 
		 	 }
		 	 catch (Exception e) {
		 		 throw new Exception("uploadFS - Errore nell'operazione di upload nel file system - Error: "+e.getMessage());
		 	 } 	       
     }
	 
	 /**
	   * Upload del file nel file system
	   * 
	   * @param String tableAttachName			nome della tabella
	   * @param String columnAttachName			colonna da cui prendere il nome del file
	   * @param String columnAttach  			colonna da cui prendere il file
	   * @param String whereAttachCondition  	filtro su  columnAttachName  per determinare quale riga estrarre
	   * @param String pathDest					path destinazione in cui creare il file
	   * 
	 */
	 private void uploadDB(String tableAttachName,String columnAttachName,String columnAttach,
			 			   String whereAttachCondition,String pathDest) throws Exception {
			 	  String path="",nameFile;
			 	  StringBuffer sStm = new StringBuffer("SELECT "+columnAttachName+","+columnAttach);
			 	  sStm.append(" FROM "+tableAttachName);
			 	  if (!whereAttachCondition.trim().equals(""))
					 sStm.append(" WHERE "+whereAttachCondition);
				 
			 	  try {
			 		 dbOperation.setStatement(sStm.toString());
			 		 dbOperation.execute();
			 	  }
			 	  catch (Exception e) {
					 throw new Exception("Errore esecuzione SQL=("+sStm.toString()+") per estrarre il file. Error: "+e.getMessage());
			 	  } 	    	     
				 
			 	  try {
					 ResultSet rst = dbOperation.getRstSet();
					 ResultSetMetaData	rmeta; 
		    	     if (rst.next()) {	    	    	   
		    	    	   rmeta = rst.getMetaData();
		    	    	   int tipoColonna = rmeta.getColumnType(2);
		    	    	   nameFile=rst.getString(1);
		    	    	   InputStream is=null;
		    	    	   
		    	    	   if(tipoColonna == Types.CLOB){	    	    		   
		    	    		  is=dbOperation.readClob(2);
							}else if(tipoColonna == Types.BLOB){							
							  is=dbOperation.readBlob(2);
							}else{
								throw new Exception("Attenzione! la colonna ("+columnAttach+") deve essere di tipo CLOB o BLOB");
							}
		    	    	   
		    	    	    if(nameFile==null)
		    	    	      throw new Exception("Attenzione! il nome del file da creare è nullo");
								
			    	    	/** Path completo di destinazione */
			  			    path=pathDest + File.separator+nameFile;
			  			    /** Creazione del file */
			    	    	LetturaScritturaFileFS writer = new LetturaScritturaFileFS(path);
			    	 	    writer.scriviFile(is);
		    	     }	    	     
			 	  }
			 	  catch (Exception e) {
					 throw new Exception("Errore in scrittura file su FS. Error: "+e.getMessage());
			 	  } 
	 }
	 
	 /**
	   * Upload del file parziale
	   * 
	   * @param String icona					icona
	   * @param String pathDest					path destinazione in cui creare il file
	   * @param String tableAttachName			nome della tabella
	   * @param String columnAttach  			colonna da cui prendere il file
	   * @param String whereAttachCondition  	filtro su  columnAttachName per determinare quale riga estrarre
	 * 
	 */
	 private void uploadFSP(String pathDest,String tableAttachName,String icona,
			 				String columnAttach,String whereAttachCondition) throws Exception {
			      
			 try {
	 		  if(icona!=null && !icona.equals(""))
				retrieveIcona(icona);

	 		  if(nomeIcona!=null && !nomeIcona.equals("") && icona!=null && !icona.equals(""))
			  {	 
				File f=null;
		        String pathFile;
		        LetturaScritturaFileFS writer = null;
		        LetturaScritturaFileDB reader=null;
		        pathFile=pathDest+icona+File.separator+nomeIcona;		      
		        f= new File(pathFile);
		        
		        if (!(f.exists())|| modificataIcona.equals("S")) {
		        	 boolean ok = new File(pathDest+ File.separator + icona).mkdirs();
		        	 File fdir = new File(pathDest+ File.separator + icona);
		        	
		             if (fdir.exists()) 
		             {
		               reader = new LetturaScritturaFileDB(dbOperation.getConn(),tableAttachName,columnAttach,whereAttachCondition);
		       	       InputStream bis = reader.leggiFile();
		       	       if(bis==null)
		       	    	 throw new Exception("Attenzione! La risorsa da DB è nulla."); 
		       	       else	   
		       	       {
		       	    	   writer = new LetturaScritturaFileFS(pathFile);
		       	    	   writer.scriviFile(bis);
		       	    	   updateIcona(icona);
 				       }
		             }
		        }
			  } 
		 	 }
		 	 catch (Exception e) {
		 		 throw new Exception("uploadFSP - Errore nell'operazione di upload nel file system - Error: "+e.getMessage());
		 	 } 	 		
    }
	 
	 
	private void retrieveIcona(String icona)throws Exception {
		 	IDbOperationSQL dbOp=null;
		 	String sql;
		        
	        try {
	       	  dbOp= SessioneDb.getInstance().createIDbOperationSQL(dbOperation.getConn(),0);
		      sql="SELECT NOME, MODIFICATA FROM ICONE ";
	          sql+=" WHERE ICONA = '"+icona+"'";
	          dbOp.setStatement(sql);
		      dbOp.execute();
		      ResultSet rs = dbOp.getRstSet();
		      if(rs.next()) {
	    	    nomeIcona=rs.getString("nome");
	    	    modificataIcona=rs.getString("modificata");
			  }
             }
	         catch (Exception e) {     
	          try {dbOp.close();}catch (Exception eClose) {}
			  throw e;
	         }
	}
	
	private void updateIcona(String icona)throws Exception {
		 	IDbOperationSQL dbOp=null;
		 	String sql;
	        try {
	       	  dbOp= SessioneDb.getInstance().createIDbOperationSQL(dbOperation.getConn(),0);
		      sql="UPDATE ICONE SET MODIFICATA = 'N' ";
	          sql+=" WHERE ICONA = '"+icona+"'";
	          dbOp.setStatement(sql);
		      dbOp.execute();
		      dbOp.commit();
	        }
	        catch (Exception e) {     
	          dbOp.rollback();
	          throw e;
	        }
	}
	 
}
