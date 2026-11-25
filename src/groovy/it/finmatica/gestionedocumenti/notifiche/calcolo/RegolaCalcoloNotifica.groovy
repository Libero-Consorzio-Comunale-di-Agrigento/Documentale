package it.finmatica.gestionedocumenti.notifiche.calcolo

class RegolaCalcoloNotifica {

	enum TipoMetodo {
		TAG,		// ritorna una String
		ALLEGATO, 	// ritorna un InputStream
		DESTINATARI // ritorna una lista di NotificaSoggetto
	}
	
	String codice
	String descrizione
	String titolo
	TipoMetodo tipo
	List<String> tipiDocumento
}
