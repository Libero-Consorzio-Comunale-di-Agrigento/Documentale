package it.finmatica.gestionedocumenti.notifiche.calcolo.annotated

import it.finmatica.gestionedocumenti.notifiche.calcolo.RegolaCalcoloNotifica

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Target(value = ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
@interface RegolaCalcolo {
	public static final String TUTTI_DOCUMENTI = "*"

	String codice ()
	String[] tipiDocumento () default ["*"]
	RegolaCalcoloNotifica.TipoMetodo tipo ()
	String titolo ()
	String descrizione ()
}
