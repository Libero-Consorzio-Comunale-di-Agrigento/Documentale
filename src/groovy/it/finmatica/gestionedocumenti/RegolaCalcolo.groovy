package it.finmatica.gestionedocumenti;

class RegolaCalcolo {
	
    String nomeBean
    String nomeMetodo
	
	String getId () {
		// utile per il it.finmatica.gestionedocumenti.zkutils.PropertyConverter nelle combobox
		return nomeBean + "." + nomeMetodo
	}
}
