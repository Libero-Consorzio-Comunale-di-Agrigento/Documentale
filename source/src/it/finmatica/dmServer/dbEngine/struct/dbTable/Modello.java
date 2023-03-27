package it.finmatica.dmServer.dbEngine.struct.dbTable;

public class Modello {
	   private String area, codice;
	   private long id;
	   
	   private Object extendModello;	 	  

	   public Modello() {
		      
	   }
	   
	   public Modello(long ident, String a, String c) {
		   	  area=a;
		   	  codice=c;
		   	  id=ident;
	   }	   
	   
	   public String getArea() {
		   	  return area;
	   }

	   public void setArea(String area) {
		      this.area = area;
	   }

	   public String getCodice() {
		   	  return codice;
	   }

	   public void setCodice(String codice) {
		   	  this.codice = codice;
	   }

	   public long getId() {
		   	  return id;
	   }

	   public void setId(long id) {
		   	  this.id = id;
	   }
	   
	   public Object getExtendModello() {
			  return extendModello;
	   }

	   public void setExtendModello(Object extendModello) {
			  this.extendModello = extendModello;
	   }	   
}
