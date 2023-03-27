package it.finmatica.dmServer;

import java.io.File;
import java.io.InputStream;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import it.finmatica.dmServer.Impronta.ImprontaAllegati;
import it.finmatica.dmServer.util.Global;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.io.LetturaScritturaFileFS;

public class GD4_Oggetti_File_Log {
	IDbOperationSQL dbOp;
	private List<GD4_Oggetto_File_Log> listaLogFile = new ArrayList<GD4_Oggetto_File_Log>();
	
	public GD4_Oggetti_File_Log(IDbOperationSQL dbOp) {
		this.dbOp= dbOp;
	}
			
	public void retrieveOggettiFileLog(String idDocumento, String idLog) throws Exception {
		listaLogFile.clear();
		
		String sql;
		sql= "select  nvl( oggetti_file_log.path_file,''), ";
		sql+="		  decode(path_file_root,NULL,nvl(aree.path_file,''),nvl(path_file_root,'')) ,";
		sql+="		  aree.ACRONIMO||'/'|| TIPI_DOCUMENTO.ACRONIMO_MODELLO || '/' || to_char(trunc(DOCUMENTI.ID_DOCUMENTO/1000)) || '/' || DOCUMENTI.ID_DOCUMENTO|| '/' ||'LOG_'||activity_log.id_log||'/'||oggetti_file_log.id_oggetto_file,";
		sql+="		  oggetti_file_log.ID_OGGETTO_FILE_LOG,";
		sql+="		  oggetti_file_log.ID_LOG,";
		sql+="		  oggetti_file_log.ID_OGGETTO_FILE,";
		sql+="		  oggetti_file_log.FILENAME,";
		sql+="		  oggetti_file_log.TESTOOCR,";
		sql+="		  oggetti_file_log.IMPRONTA";
		sql+="   from oggetti_file_log , activity_log , documenti, aree, tipi_documento ";
		sql+="  where documenti.id_documento="+idDocumento+"  and ";
		sql+="        aree.area = documenti.area and ";
		sql+=" 		  tipi_documento.id_tipodoc= documenti.id_tipodoc and ";
		sql+=" 		  activity_log.id_documento=documenti.id_documento and ";
		sql+=" 		  activity_log.id_log=oggetti_file_log.id_log ";
		if (!(idLog.equals(""))) sql+=" and activity_log.id_log="+idLog;
		
		try {	
			dbOp.setStatement(sql);
			dbOp.execute();
	        ResultSet rst = dbOp.getRstSet();
	        while (rst.next()) {
	        	GD4_Oggetto_File_Log ogfiLog = new GD4_Oggetto_File_Log();
	        	ogfiLog.idOggettoFileLog=rst.getString("ID_OGGETTO_FILE_LOG");
	        	ogfiLog.id_log=rst.getString("ID_LOG");
	        	ogfiLog.id_oggetto_file=rst.getString("ID_OGGETTO_FILE");
	        	ogfiLog.filename=rst.getString("FILENAME");
	        	ogfiLog.impronta=rst.getString("IMPRONTA");
	       	  	
	       	  	if (! (Global.nvl(rst.getString(1),"")).equals("") && !(Global.nvl(rst.getString(2),"")).equals("")) {
	       	  		ogfiLog.pathFile  = rst.getString(2)+"/"+rst.getString(3); 	  		
	       	  	}
	       	  	else  {            	
	            	try {			                    	  
	            		ogfiLog.testoOcr=dbOp.readBlob(8);
	            	}
	           	  	catch (NullPointerException e) {  
	           	  		//DONTCARE           	  		
	           	  	}       	  		
	       	  	}
	       	  	
	       	  	listaLogFile.add(ogfiLog);
	        }
		 }
	     catch (Exception e) {
	    	 throw new Exception("GD4_Oggetti_File_Log::retrieveOggettiFileLog "+e.getMessage());
	     }
	}	
	
	public void generaImpronte() throws Exception {		
		try {			
			ImprontaAllegati iall = new ImprontaAllegati();
			//Ciclo per generare l'impronta
			for(int i=0;i<listaLogFile.size();i++) {			
				GD4_Oggetto_File_Log ogfiLog = listaLogFile.get(i);
				
				
				InputStream is = null;
				try {
					is =ogfiLog.getFile();
				}
			    catch (Exception e) {
			    	throw new Exception("Errore in recupero file per id_oggetto_file_log="+ogfiLog.idOggettoFileLog+". Errore = "+e.getMessage());
			    }
			    
			    if (is==null) continue;
			    
			    try {
			    	ogfiLog.improntaByte=iall.hashCodeAllegato(ogfiLog.filename, is);
				}
			    catch (Exception e) {
			    	try {is.close();}catch (Exception ei) {}
			    	throw new Exception("Errore in creazione impronta per id_oggetto_file_log="+ogfiLog.idOggettoFileLog+". Errore = "+e.getMessage());
			    }			    
			    try {is.close();}catch (Exception ei) {}
			}
			
			//Ciclo per aggiornare l'impronta sulla tabella			
			for(int i=0;i<listaLogFile.size();i++) {
				GD4_Oggetto_File_Log ogfiLog = listaLogFile.get(i);
				if (ogfiLog.improntaByte==null) continue;
				
				ogfiLog.updateImpronta(dbOp);				
			}
		 }
	     catch (Exception e) {
	    	 throw new Exception("GD4_Oggetti_File_Log::generaImpronte "+e.getMessage());
	     }			
	}
	
}

class GD4_Oggetto_File_Log {
	public String idOggettoFileLog,id_log, id_oggetto_file;
	public InputStream testoOcr =null;
	public String filename, pathFile, impronta;
	public byte[] improntaByte = null;
	
	public GD4_Oggetto_File_Log() {		
	}		
	
	public InputStream getFile() throws Exception {
		InputStream is =null;
		
		//Se è su blob lo restitiusco direttamente
		if (testoOcr!=null) return testoOcr;
		
		//Altrimenti è un file su FS		
   	 	File fEx = new File(pathFile);
  	    if (fEx.exists()){               	  
      	  LetturaScritturaFileFS f = new LetturaScritturaFileFS(pathFile);
      	          	 
      	  is=f.leggiFile();           	             	  
    	}
 
		return is;
	}
	
	public void updateImpronta(IDbOperationSQL dbOp) throws Exception {
		String sql;
		
		sql="update oggetti_file_log set impronta=:P_IMPRONTA where id_oggetto_file_log= "+idOggettoFileLog;
		
		try {
			dbOp.setStatement(sql);
			dbOp.setParameter(":P_IMPRONTA", improntaByte);
			dbOp.execute();
		 }
	     catch (Exception e) {
	    	 throw new Exception("GD4_Oggetto_File_Log::updateImpronta(idOggettoFileLog="+idOggettoFileLog+"). Errore= "+e.getMessage());
	     }				
	}
	
}
