package it.finmatica.gestionedocumenti.integrazioni.gdm;

public abstract class Converter <Input, Output> {
	
	public abstract Output convert (Input input);

}
