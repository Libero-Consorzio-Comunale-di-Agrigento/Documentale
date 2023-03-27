package it.finmatica.dmServer.dbEngine;

import java.sql.ResultSet;
import java.util.Vector;

import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.dbEngine.struct.dbTable.Area;
import it.finmatica.dmServer.dbEngine.struct.dbTable.Metadato;
import it.finmatica.dmServer.dbEngine.struct.dbTable.Modello;
import it.finmatica.dmServer.util.ManageConnection;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;

public class LookUpDbOperation extends MasterEngine {
	   public LookUpDbOperation(Environment en,IDbOperationSQL dbOp, String msgElapsedTime) {
		      super(en,dbOp,msgElapsedTime);
	   }	
	   
	   /************************************ LookUp on DOCUMENT **********************************************/	   
	   
	   public String getSQLListaAllegati(String idDocumento)  {
		      StringBuffer sStm = new StringBuffer();      

		      sStm.append("select id_oggetto_file,");
		      sStm.append("o.id_formato,filename,\"FILE\",testoocr,allegato,id_oggetto_file_padre,NVL(F.VISIBILE,'S'), nvl(PATH_FILE,''),");
		      sStm.append("NVL(F.ICONA, 'generico.gif'),TO_NUMBER(NULL),nvl(o.DA_CANCELLARE,'N') dacancellare ");
		      sStm.append(" from oggetti_file o, formati_file f");
		      sStm.append(" where id_documento = " +idDocumento );
		      sStm.append(" and o.ID_FORMATO = f.ID_FORMATO");
		      sStm.append(" order by filename");
		     
		      return sStm.toString();
	   }
	   
	   public String getIdDocumento(String area, String cm, String cr) throws Exception {
		   	  String idDoc = "";
		   	  
		   	  super.appendStatement("Select ID_DOCUMENTO ");
		   	  super.appendStatement("from documenti, modelli ");
		   	  super.appendStatement("where documenti.area= :P_AREA " );
		   	  super.appendStatement("and modelli.codice_modello= :P_MODELLO ");
		   	  super.appendStatement("and documenti.id_tipodoc=modelli.id_tipodoc ");
		   	  super.appendStatement("and documenti.codice_richiesta= :P_RICHIESTA ");
		   	  super.appendStatement("and modelli.id_tipodoc is not null ");
		   	  super.appendStatement("UNION ");
	    	  super.appendStatement("Select ID_DOCUMENTO ");
	    	  super.appendStatement("from documenti, modelli mfiglio, modelli mpadre ");
	    	  super.appendStatement("where documenti.area= :P_AREA ");
	    	  super.appendStatement("and mfiglio.codice_modello= :P_MODELLO ");
	    	  super.appendStatement("and documenti.id_tipodoc=mpadre.id_tipodoc ");
	    	  super.appendStatement("and mfiglio.id_tipodoc is  null ");
	    	  super.appendStatement("and MFIGLIO.AREA=mpadre.area ");
	    	  super.appendStatement("and MFIGLIO.codice_modello_padre=mpadre.codice_modello ");
	    	  super.appendStatement("and documenti.codice_richiesta= :P_RICHIESTA ");	    	  
	    	  	    	 
	    	  super.appendParameter(":P_AREA",area);
	    	  super.appendParameter(":P_MODELLO",cm);
	    	  super.appendParameter(":P_RICHIESTA",cr);
	    	  
	    	  connect();
	    	  
	    	  try {
		    	  ResultSet rst = super.executeSqlResultSet("Recupero idDocumento da ar/cm/cr");
		    	  
		    	  if (rst.next() ) 
		    		  idDoc = rst.getString("ID_DOCUMENTO");
	    	  }
	    	  catch (Exception e) {
	    		  close();
	    		  throw new Exception(e);
	    	  }
		    	  
	    	  close();
	    	  return idDoc;	    	  
	   }
	  

	   /************************************************************************************************/
	   
	   
	   
	   
	   /************************************ LookUp on LookUpDMTable **********************************************/	  
	   
	   public Vector<Area> getListaAree() throws Exception {
		   	  Vector<Area> vAree = new Vector<Area>();
		   	  
		   	  super.appendStatement("Select ID_AREA,AREA, DESCRIZIONE from aree ");
		   	  
		   	  connect();
		   	  try {
		    	  ResultSet rst = super.executeSqlResultSet("Recupero elenco aree");
		    	  
		    	  while (rst.next() ) 
		    		  	vAree.add(new Area(rst.getLong(1),rst.getString(2),rst.getString(3)));
	    	  }
	    	  catch (Exception e) {
	    		  close();
	    		  throw new Exception(e);
	    	  }
	    	  
		   	  close();
		   	  return vAree;
	   }
	   
	   public Vector<Modello> getListaModelli(String area) throws Exception {
		   	  Vector<Modello> vModelli = new Vector<Modello>();
		   	  
		   	  super.appendStatement("Select ID_TIPODOC,area,codice_modello  from MODELLI where AREA = :P_AREA ");
		   	  super.appendParameter(":P_AREA",area);
		   	  
		   	  connect();
		   	  try {
		    	  ResultSet rst = super.executeSqlResultSet("Recupero elenco modelli da area="+area);
		    	  
		    	  while (rst.next() ) 
		    		  vModelli.add(new Modello(rst.getLong(1),rst.getString(2),rst.getString(3)));
	    	  }
	    	  catch (Exception e) {
	    		  close();
	    		  throw new Exception(e);
	    	  }
	    	  
		   	  close();
		   	  return vModelli;
	   }
	   
	   public Vector<Metadato> getListaMetadati(String area, String codiceModello) throws Exception {
		   	  Vector<Metadato> vMetadati = new Vector<Metadato>();
		   	  
		   	  super.appendStatement("Select DATI_MODELLO.area,DATI_MODELLO.codice_modello,DATI_MODELLO.DATO,DATI.TIPO,DATI.NOTE  from DATI_MODELLO,DATI "+
		   			  				"where DATI_MODELLO.AREA_DATO=DATI.AREA AND DATI_MODELLO.DATO=DATI.DATO "+
		   			  				"  AND DATI_MODELLO.AREA= :P_AREA AND DATI_MODELLO.CODICE_MODELLO= :P_CM ");
		   	  super.appendParameter(":P_AREA",area);
		   	  super.appendParameter(":P_CM",codiceModello);
		   	  
		   	  connect();
		   	  try { 
		    	  ResultSet rst = super.executeSqlResultSet("Recupero elenco metadati da area="+area+" e codiceModello="+codiceModello);
		    	  
		    	  while (rst.next() ) 
		    		  vMetadati.add(new Metadato(rst.getString(1),rst.getString(2),rst.getString(3),rst.getString(4),rst.getString(5),null));
	    	  }
	    	  catch (Exception e) {
	    		  close();
	    		  throw new Exception(e);
	    	  }
	    	  
		   	  close();
		   	  return vMetadati;
	   }	   
	   
	   public String getIdDocumentoFromIdAllegato(String idAllegato) throws Exception {
		   	  String sRet=null;
		      super.appendStatement("SELECT ID_DOCUMENTO FROM OGGETTI_FILE WHERE ID_OGGETTO_FILE=:P_ID");
		      super.appendParameter(":P_ID",Long.parseLong(idAllegato));

		      connect();
		   	  try { 
		    	  ResultSet rst = super.executeSqlResultSet("Recupero idDocumento da ID_ALLEGATO="+idAllegato);
		    	  
		    	  if (rst.next() ) sRet= rst.getString(1);
		    	  else
		    		  throw new Exception("Attenzione! idDocumento per idAllegato "+idAllegato+" Inesistente");
		    		  
	    	  }
	    	  catch (Exception e) {
	    		  close();
	    		  throw new Exception(e);
	    	  }
	    	  
		   	  close();
		   	  return sRet;
	   }
	   
	   
	   /************************************************************************************************/
	   
	   
	   

}
