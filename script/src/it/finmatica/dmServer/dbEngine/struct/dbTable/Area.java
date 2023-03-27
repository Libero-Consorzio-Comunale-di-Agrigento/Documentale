package it.finmatica.dmServer.dbEngine.struct.dbTable;

public class Area {
	   protected String codice, descrizione;
	   protected long id;
	   protected Object extendArea;
	   
	   public Area() {
		      
	   }
	   
	   public Area(long ident, String c, String d) {
		      codice=c;
		      descrizione=d;
		      id=ident;
	   }

	   public String getCodice() {
		      return codice;
	   }

	   public void setCodice(String codice) {
		      this.codice = codice;
	   }

	   public String getDescrizione() {
		   	  return descrizione;
	   }

	   public void setDescrizione(String descrizione) {
		      this.descrizione = descrizione;
	   }

	   public long getId() {
			  return id;
	   }

	   public void setId(long id) {
		      this.id = id;
	   }	   	   
	   
	   public Object getExtendArea() {
			  return extendArea;
	   }

	   public void setExtendArea(Object extendArea) {
			  this.extendArea = extendArea;
	   }	   
}
