package it.finmatica.dmServer.dbEngine.struct.dbTable;

public class MetadatoSimple {
	   private String codice, valore;	   	  

	   public MetadatoSimple() {
		      
	   }
	   
	   public MetadatoSimple(String codice, String valore) {
		      this.codice=codice;
		      this.valore=valore;
	   }	
	   
	   public String getCodice() {
			  return codice;
		}

		public void setCodice(String codice) {
			   this.codice = codice;
		}

		public String getValore() {
			   return valore;
		}

		public void setValore(String valore) {
			   this.valore = valore;
		}	   
}
