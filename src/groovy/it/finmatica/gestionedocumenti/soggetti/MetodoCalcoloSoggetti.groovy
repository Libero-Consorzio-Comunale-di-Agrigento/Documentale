package it.finmatica.gestionedocumenti.soggetti;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface MetodoCalcoloSoggetti {
	
	public enum Tipo {
		DEFAULT,
	    LISTA
	}
	
    public enum Categoria {
		UNITA,
		COMPONENTE
	}
	
	/**
	 * indica se è per unità o componenti
	 */
    Categoria categoria();  
	Tipo tipo();
	String titolo();
	String descrizione();
	
}
