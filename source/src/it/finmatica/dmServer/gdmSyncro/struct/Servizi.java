package it.finmatica.dmServer.gdmSyncro.struct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Servizi {
	private String nomeServizio, classeImplementazione;
	private HashMap<String,String> infoServizioParametri = new HashMap<String,String>();
	
	public Servizi() {
		
	}
			
	public Servizi(String nomeServizio, String classeImplementazione) {
		this.nomeServizio=nomeServizio;
		this.classeImplementazione=classeImplementazione;
	}

	public String getNomeServizio() {
		return nomeServizio;
	}

	public void setNomeServizio(String nomeServizio) {
		this.nomeServizio = nomeServizio;
	}

	public String getClasseImplementazione() {
		return classeImplementazione;
	}

	public void setClasseImplementazione(String classeImplementazione) {
		this.classeImplementazione = classeImplementazione;
	}

	public HashMap<String,String> getInfoServizioParametri() {
		return infoServizioParametri;
	}

	public void setInfoServizioParametri(
			HashMap<String,String> infoServizioParametri) {
		this.infoServizioParametri = infoServizioParametri;
	}
	
	public void addParametro(String codice, String valore) {
		this.infoServizioParametri.put(codice,valore);
	}
	
	public String toString()  {
		String sRet;
		
		sRet="nomeServizio="+nomeServizio+", classeImplementazione="+classeImplementazione+"\n";
		sRet+="ListaParametri="+this.infoServizioParametri;
		
		return sRet;
	}
}
