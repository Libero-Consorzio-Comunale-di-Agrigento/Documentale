package it.finmatica.gestionedocumenti.notifiche;

import java.io.InputStream;

public class Allegato {
	private String 		nome;
	private InputStream testo;

	public Allegato (String nome, InputStream testo) {
		this.nome = nome;
		this.testo = testo;
	}

	public String getNome() {
		return nome;
	}

	public InputStream getTesto() {
		return testo;
	}
}
