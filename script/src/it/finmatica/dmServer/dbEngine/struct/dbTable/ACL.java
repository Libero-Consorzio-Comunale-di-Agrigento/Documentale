package it.finmatica.dmServer.dbEngine.struct.dbTable;

public class ACL {
	   protected String utenteGruppo, tipoCompetenza, accesso;
	   

	   public ACL() {
		      
	   }
	   
	   public ACL(String utenteGruppo, String tipoCompetenza, String accesso) {
		      this.utenteGruppo=utenteGruppo;
		      this.tipoCompetenza=tipoCompetenza;
		      this.accesso=accesso;
	   }	   
	   	
	   public String getUtenteGruppo() {
		      return utenteGruppo;
	   }

	   public void setUtenteGruppo(String utenteGruppo) {
			  this.utenteGruppo = utenteGruppo;
	   }

	   public String getTipoCompetenza() {
		      return tipoCompetenza;
	   }

	   public void setTipoCompetenza(String tipoCompetenza) {
		      this.tipoCompetenza = tipoCompetenza;
	   }

	   public String getAccesso() {
		      return accesso;
	   }

	   public void setAccesso(String accesso) {
		      this.accesso = accesso;
	   }
	   
}
