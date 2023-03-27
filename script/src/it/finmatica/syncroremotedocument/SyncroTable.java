package it.finmatica.syncroremotedocument;

public class SyncroTable {
	   private String idDocumentoGDM,idObjAllegatoGDM, applicativo;
	   private String acromymTypeDoc, idDocumentoRemoto, ultimaAzione;
	   private String dataUltimaAzione, idSyncroRif, lastActionReport, ente;
	   	   
	   public String getEnte() {
		   return ente;
	   }

	   public void setEnte(String ente) {
			this.ente = ente;
	   }

	   public String getIdDocumentoGDM() {
			return idDocumentoGDM;
	   }
	   
	   public void setIdDocumentoGDM(String idDocumentoGDM) {
			this.idDocumentoGDM = idDocumentoGDM;
		}
		public String getIdObjAllegatoGDM() {
			return idObjAllegatoGDM;
		}
		public void setIdObjAllegatoGDM(String idObjAllegatoGDM) {
			this.idObjAllegatoGDM = idObjAllegatoGDM;
		}
		public String getApplicativo() {
			return applicativo;
		}
		public void setApplicativo(String applicativo) {
			this.applicativo = applicativo;
		}
		public String getAcromymTypeDoc() {
			return acromymTypeDoc;
		}
		public void setAcromymTypeDoc(String acromymTypeDoc) {
			this.acromymTypeDoc = acromymTypeDoc;
		}
		public String getIdDocumentoRemoto() {
			return idDocumentoRemoto;
		}
		public void setIdDocumentoRemoto(String idDocumentoRemoto) {
			this.idDocumentoRemoto = idDocumentoRemoto;
		}
		public String getUltimaAzione() {
			return ultimaAzione;
		}
		public void setUltimaAzione(String ultimaAzione) {
			this.ultimaAzione = ultimaAzione;
		}
		public String getDataUltimaAzione() {
			return dataUltimaAzione;
		}
		public void setDataUltimaAzione(String dataUltimaAzione) {
			this.dataUltimaAzione = dataUltimaAzione;
		}
		public String getIdSyncroRif() {
			return idSyncroRif;
		}
		public void setIdSyncroRif(String idSyncroRif) {
			this.idSyncroRif = idSyncroRif;
		}
		public String getLastActionReport() {
			return lastActionReport;
		}
		public void setLastActionReport(String lastActionReport) {
			this.lastActionReport = lastActionReport;
		}	   
}
