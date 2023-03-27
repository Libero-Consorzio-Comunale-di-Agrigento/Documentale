package it.finmatica.dmServer.util;

import java.util.HashMap;

public class ValoriLogStruct {
	   private String idLog,azione,dataAgg,utenteAgg;
	   
	   private HashMap<String,String> hmCampiValori;
	   
	   public ValoriLogStruct(String id, String action,
			   				  String data, String ut) {
		   	  idLog=id;
		   	  azione=action;
		   	  dataAgg=data;
		   	  utenteAgg=ut;
		   	  
		   	  hmCampiValori = new HashMap<String,String>();
	   }

	   public String getAzione() {
		      return azione;
	   }

	   public void setAzione(String azione) {
		      this.azione = azione;
	   }

	   public String getDataAggiornamento() {
		      return dataAgg;
	   }

	   public void setDataAggiornamento(String dataAggiornamento) {
		      this.dataAgg = dataAggiornamento;
	   }

	   public HashMap getHmCampiValori() {
		      return hmCampiValori;
	   }

	   public void setHmCampiValori(HashMap hmCampiValori) {
		      this.hmCampiValori = hmCampiValori;
	   }

	   public String getIdLog() {
		      return idLog;
	   }

	   public void setIdLog(String idLog) {
		      this.idLog = idLog;
	   }

	   public String getUtenteAgg() {
		      return utenteAgg;
	   }

	   public void setUtenteAgg(String utenteAgg) {
		      this.utenteAgg = utenteAgg;
	   }	   
	   
}
