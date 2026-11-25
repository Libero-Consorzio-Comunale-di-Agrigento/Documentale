package it.finmatica.gestionedocumenti.soggetti

import it.finmatica.gestionedocumenti.soggetti.MetodoCalcoloSoggetti.Categoria


class TipoSoggetto {
	
	public static final transient String REDATTORE 	 	= "REDATTORE"
	public static final transient String UO_PROTOCOLLANTE  = "UO_PROTOCOLLANTE"
	public static final transient String UO_MESSAGGIO  = "UO_MESSAGGIO"
	public static final transient String UO_ESIBENTE 	= "UO_ESIBENTE"
	public static final transient String FUNZIONARIO 	= "FUNZIONARIO"
	public static final transient String DIRIGENTE 	 	= "DIRIGENTE"
	public static final transient String PRESIDENTE	 	= "PRESIDENTE"
	public static final transient String SEGRETARIO  	= "SEGRETARIO"
	
	// Per il visto è il firmatario che deve firmare il visto.
	// Per la proposta, viene usato come firmatario aggiuntivo della proposta. Vedi richiesta di Treviso: #6921, http://svi-redmine/issues/6921
	public static final transient String FIRMATARIO  	= "FIRMATARIO"
	
	// Per la proposta, viene usata come unità a cui inoltrare la proposta. Vedi richiesta di Treviso: #6100, http://svi-redmine/issues/6100
	// Utilizzato anche per Pistoia: http://svi-redmine/issues/17041
	// Per il visto è l'unità che deve predere in carico il visto,
	public static final transient String UO_DESTINATARIA = "UO_DESTINATARIA"

	// Aggiunto per Treviso: #6921, http://svi-redmine/issues/6921
	// Utilizzato anche per Pistoia: http://svi-redmine/issues/17041
	// Serve per poter gestire il passaggio a più dirigenti firmatari. Va di pari passo con il soggetto FIRMATARIO.
	public static final transient String UO_FIRMATARIO 	= "UO_FIRMATARIO"
	
	// Aggiunti per la gestione delle ASL:
	public static final transient String DIRETTORE_GENERALE 		= "DIRETTORE_GENERALE"
	public static final transient String DIRETTORE_AMMINISTRATIVO 	= "DIRETTORE_AMMINISTRATIVO"
	public static final transient String DIRETTORE_SANITARIO 		= "DIRETTORE_SANITARIO"

	// Aggiunto per Treviso: #6920, http://svi-redmine/issues/6920
	// indica l'unità di controllo della ragioneria a cui la proposta deve passare prima della vidima del dirigente.
	public static final transient String UO_CONTROLLO 			= "UO_CONTROLLO"

	// Aggiunti per la gestione del Fascicolo
	public static final transient String UO_CREAZIONE 			= "UO_CREAZIONE"
	public static final transient String UO_COMPETENZA 			= "UO_COMPETENZA"


	Categoria categoria	// indica se è unità o componente

	String codice
	
	String descrizione
	
	String commento
}