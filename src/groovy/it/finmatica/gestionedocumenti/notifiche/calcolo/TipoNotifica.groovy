package it.finmatica.gestionedocumenti.notifiche.calcolo

import it.finmatica.gestionedocumenti.RegolaCalcolo

/**
 * Rappresenta le tipologie di notifiche gestite da applicativo
 *
 * Per questa domainClass non è prevista una interfaccia di gestione
 */
class TipoNotifica {

	// codici degli oggetti per cui questa notifica è disponibile
	List<String> oggetti

	// codice identificativo della tipologia di notifica
	String codice

	String titolo
	String descrizione

    // azione corrispondente per il configuratore iter (nella forma nomeBean.nomeMetodo)
	RegolaCalcolo azioneFlusso
}
