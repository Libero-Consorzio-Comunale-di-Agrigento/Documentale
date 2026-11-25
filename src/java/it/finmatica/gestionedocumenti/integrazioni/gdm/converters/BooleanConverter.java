package it.finmatica.gestionedocumenti.integrazioni.gdm.converters;

import it.finmatica.gestionedocumenti.integrazioni.gdm.Converter;


public class BooleanConverter {

    public static class Y_N extends Converter<Boolean, String> {
		public static final Y_N INSTANCE = new Y_N();

		@Override
		public String convert (Boolean input) {
			if (input != null && input.booleanValue()) {
				return "Y";
			} else {
			    return "N";
            }
		}
	}

    public static class Yes_No extends Converter<Boolean, String> {
		public static final Yes_No INSTANCE = new Yes_No();

		@Override
		public String convert (Boolean input) {
			if (input != null && input.booleanValue()) {
				return "Yes";
			} else {
			    return "No";
            }
		}
	}

    public static class S_N extends Converter<Boolean, String> {
		public static final S_N INSTANCE = new S_N();

		@Override
		public String convert (Boolean input) {
			if (input != null && input.booleanValue()) {
				return "S";
			} else {
			    return "N";
            }
		}
	}
}