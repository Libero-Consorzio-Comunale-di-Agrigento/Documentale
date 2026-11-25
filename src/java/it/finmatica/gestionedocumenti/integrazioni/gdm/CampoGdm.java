package it.finmatica.gestionedocumenti.integrazioni.gdm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface CampoGdm {
	
	/**
	 * Il nome del campo su GDM.
	 */
	String nome ();
	
	/**
	 * L'eventuale converter che trasforma il valore del dato originale in un valore "giusto" per GDM
	 */
	Class<? extends Converter<?, ?>> converter() default DEFAULT_CONVERTER.class;
	
	/**
	 * Identifica il campo come lo Stato del Documento su GDM.
	 */
	boolean statoDocumento () default false;

	class DEFAULT_CONVERTER extends Converter<Object, Object> {
		public Object convert (Object input) {

			// gdm non accetta nulli, ma "stringhe vuote"
			if (input == null) {
				return "";
			}

			return input;
		}
	}
}
