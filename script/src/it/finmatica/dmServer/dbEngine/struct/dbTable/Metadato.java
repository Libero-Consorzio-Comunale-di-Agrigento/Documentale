package it.finmatica.dmServer.dbEngine.struct.dbTable;

public class Metadato {
	   private String area, codiceModello;
	   private String codice, tipo, descrizione,valore;

	   private Object extendMetadato;	 

	   public Metadato() {
		      
	   }
	   
	   public Metadato(String a,String cm , String c, 
			   		   String t, String d, String v) {
		      area=a;
		      codiceModello=cm;
		      codice=c;
		      tipo=t;
		      descrizione=d;
		      valore=v;
	   }
	   	   
	   
	   public String getValore() {
		      return valore;
	   }

	   public void setValore(String valore) {
		   	  this.valore = valore;
	   }
	   
	   public String getArea() {
		   	  return area;
	   }

	   public void setArea(String area) {
		      this.area = area;
	   }

	   public String getCodiceModello() {
		   	  return codiceModello;
	   }

	   public void setCodiceModello(String codiceModello) {
		   	  this.codiceModello = codiceModello;
	   }

	   public String getCodice() {
		   	  return codice;
	   }

	   public void setCodice(String codice) {
		   	  this.codice = codice;
	   }

	   public String getTipo() {
		   	  return tipo;
	   }

	   public void setTipo(String tipo) {
		   	  this.tipo = tipo;
	   }

	   public String getDescrizione() {
		   	  return descrizione;
	   }

	   public void setDescrizione(String descrizione) {
		   	  this.descrizione = descrizione;
	   }
	   
	   public Object getExtendMetadato() {
			  return extendMetadato;
	   }

	   public void setExtendMetadato(Object extendMetadato) {
			  this.extendMetadato = extendMetadato;
	   }	
}
