package it.finmatica.gestionedocumenti.exception;

public class GestioneDocumentiRuntimeException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	public static final String ERRORE_MODIFICA_CONCORRENTE = "Un utente ha già modificato il dato.";

	public GestioneDocumentiRuntimeException() {
	}

	public GestioneDocumentiRuntimeException(String message) {
		super(message);
	}

	public GestioneDocumentiRuntimeException(Throwable cause) {
		super(cause);
	}

	public GestioneDocumentiRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
}
