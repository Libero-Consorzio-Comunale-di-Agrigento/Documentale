package it.finmatica.dmServer.dbEngine.struct.searchObject;

import it.finmatica.dmServer.dbEngine.struct.dbTable.Documento;

public class RisultatoRicerca {
	   protected long numeroDocumentiTotali;
	   protected long numeroDocumentiRestituiti;
	   private Documento[] listaDocumenti;
	   
	   protected Object extendRisultatoRicerca;	  
	   public RisultatoRicerca(){}
	   
	   public RisultatoRicerca(Documento[] ld, long nDocTot, long nDocRest) {
		   	  listaDocumenti=ld;
		   	  numeroDocumentiTotali=nDocTot;
		   	  numeroDocumentiRestituiti=nDocRest;
	   }
	   
	   public long getNumeroDocumentiTotali() {
		      return numeroDocumentiTotali;
	   }

	   public void setNumeroDocumentiTotali(long numeroDocumentiTotali) {
		      this.numeroDocumentiTotali = numeroDocumentiTotali;
	   }

	   public long getNumeroDocumentiRestituiti() {
		      return numeroDocumentiRestituiti;
	   }

	   public void setNumeroDocumentiRestituiti(long numeroDocumentiRestituiti) {
		   	  this.numeroDocumentiRestituiti = numeroDocumentiRestituiti;
	   }

	   public Documento[] getListaDocumenti() {
		      return listaDocumenti;
	   }

	   public void setListaDocumenti(Documento[] listaDocumenti) {
		      this.listaDocumenti = listaDocumenti;
	   }

	   public Object getExtendRisultatoRicerca() {
			  return extendRisultatoRicerca;
	   }

	   public void setExtendRisultatoRicerca(Object extendRisultatoRicerca) {
			  this.extendRisultatoRicerca = extendRisultatoRicerca;
	   }	   
}
