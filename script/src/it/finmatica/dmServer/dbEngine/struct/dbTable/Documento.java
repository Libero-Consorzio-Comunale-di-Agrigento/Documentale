package it.finmatica.dmServer.dbEngine.struct.dbTable;

public class Documento {
	   protected long id;
	   protected String area,modello;
	   protected String dataCreazione, stato;
		   
	   protected Metadato[] metadati;
	   protected Allegato[] allegati;		
	   
	   protected Object extendDocumento;

	   public Documento() {
			   
	   }
		   
	   public Documento(long idDoc,String a,String m,String dc,String st,
				   		Metadato[] mt,Allegato[] al) {
			  id=idDoc;
			  area=a;
			  modello=m;
			  dataCreazione=dc;
			  stato=st;
			  metadati=mt;
			  allegati=al;
	   }	   
	   
	   public Documento(long idDoc,String a,String m,String dc,String st) {
			  this(idDoc,a,m,dc,st,null,null);
		}	  	   
	   
	   public long getId() {
		      return id;
	   }
	   
	   public void setId(long id) {
		      this.id = id;
	   }

	   public String getArea() {
		   	  return area;
	   }

	   public void setArea(String area) {
		   	  this.area = area;
	   }

	   public String getModello() {
		   	  return modello;
	   }

	   public void setModello(String modello) {
		      this.modello = modello;
	   }

	   public String getDataCreazione() {
		   	  return dataCreazione;
	   }

	   public void setDataCreazione(String dataCreazione) {
		   	  this.dataCreazione = dataCreazione;
	   }

	   public String getStato() {
		   	  return stato;
	   }

	   public void setStato(String stato) {
		      this.stato = stato;
	   }

	   public Metadato[] getMetadati() {
		   	  return metadati;
	   }

	   public void setMetadati(Metadato[] metadati) {
		   	  this.metadati = metadati;
	   }

	   public Allegato[] getAllegati() {
		   	  return allegati;
	   }

	   public void setAllegati(Allegato[] allegati) {
		      this.allegati = allegati;
	   }
	   	   
	   public Object getExtendDocumento() {
		      return extendDocumento;
	   }

	   public void setExtendDocumento(Object extendDocumento) {
		      this.extendDocumento = extendDocumento;
	   }	   
}
