package it.finmatica.gestionedocumenti.soggetti

import it.finmatica.gestionedocumenti.RegolaCalcolo
import it.finmatica.gestionedocumenti.soggetti.MetodoCalcoloSoggetti.Categoria
import it.finmatica.gestionedocumenti.soggetti.MetodoCalcoloSoggetti.Tipo

class RegolaCalcoloSoggetti extends RegolaCalcolo {
	String 		descrizione
	String 		titolo
	Categoria 	categoria
	Tipo 		tipo
}
