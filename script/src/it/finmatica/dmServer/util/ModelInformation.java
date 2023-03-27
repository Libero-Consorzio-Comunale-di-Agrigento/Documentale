package it.finmatica.dmServer.util;

public class ModelInformation {
	   private String idTipoDoc, area, codModello;
	   private int isHorizModel = 0;	   
	   private long libreria = 0;
	   private long nMaxAllegato = -1;
	   
	   public ModelInformation(String itd,String ar,String cm,long lib,int isH ) {
              idTipoDoc=itd;
              area=ar;
              codModello=cm;
              libreria=lib;
              isHorizModel=isH;
	   }

	   public String getArea() {
			  return area;
	   }
	
	   public String getCodModello() {
			  return codModello;
	   }
	
	   public String getIdTipoDoc() {
			  return idTipoDoc;
	   }
	
	   public int getIsHorizModel() {
			  return isHorizModel;
	   }
	
	   public Long getLibreria() {
			  return libreria; 
	   }

	   public long getNMaxAllegato() {
		      return nMaxAllegato;
	   }

	   public void setNMaxAllegato(long maxAllegato) {
		      nMaxAllegato = maxAllegato;
	   }
	   
}
