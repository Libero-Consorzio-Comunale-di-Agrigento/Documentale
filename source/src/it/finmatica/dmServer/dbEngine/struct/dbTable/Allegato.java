package it.finmatica.dmServer.dbEngine.struct.dbTable;

public class Allegato {
	   private long idAllegato;
	   private String descrizione, tipoAllegato;
	   private String dataAggiornamento;
	   private byte[] allegato; 
	   private Object extendAllegato;	   	  

	   public Allegato() {
		   	  
	   }
	   
	   public Allegato(long id,String descr,String type,String dataAgg,byte[] all) {
		   	  idAllegato=id;
		      descrizione=descr; 
		      tipoAllegato=type;
		      dataAggiornamento=dataAgg;
		      allegato=all;
	   }
	   
	   
	   public long getIdAllegato() {
		      return idAllegato;
	   }

	   public void setIdAllegato(long idAllegato) {
		      this.idAllegato = idAllegato;
	   }	

	   public String getDescrizione() {
		   	  return descrizione;
	   }

	   public void setDescrizione(String descrizione) {
		   	  this.descrizione = descrizione;
	   }

	   public String getTipoAllegato() {
		      return tipoAllegato;
	   }

	   public void setTipoAllegato(String tipoAllegato) {
		   	  this.tipoAllegato = tipoAllegato;
	   }

	   public String getDataAggiornamento() {
		   	  return dataAggiornamento;
	   }

	   public void setDataAggiornamento(String dataAgg) {
		   	  this.dataAggiornamento = dataAgg;
	   }

	   public byte[] getAllegato() {
		   	  return allegato;
	   }

	   public void setAllegato(byte[] allegato) {
		   	  this.allegato = allegato;
	   }

	   public Object getExtendAllegato() {
			  return extendAllegato;
	   }
	
	   public void setExtendAllegato(Object extendAllegato) {
			  this.extendAllegato = extendAllegato;
	   }
	   
}
