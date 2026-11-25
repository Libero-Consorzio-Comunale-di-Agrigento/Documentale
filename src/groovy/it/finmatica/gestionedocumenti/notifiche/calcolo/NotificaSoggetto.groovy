package it.finmatica.gestionedocumenti.notifiche.calcolo

import it.finmatica.ad4.autenticazione.Ad4RuoloDTO
import it.finmatica.ad4.autenticazione.Ad4UtenteDTO
import it.finmatica.as4.As4SoggettoCorrenteDTO

import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubbDTO

class NotificaSoggetto implements Comparable<NotificaSoggetto> {

	enum Assegnazione {
		COMPETENZA, CONOSCENZA
	}

	String email
	Ad4UtenteDTO utente
	Assegnazione assegnazione
	String funzione

	As4SoggettoCorrenteDTO soggetto
	So4UnitaPubbDTO unitaSo4
	Ad4RuoloDTO ruoloAd4

	@Override
	int compareTo(NotificaSoggetto object) {
		if (!(object instanceof NotificaSoggetto)) {
			return 1
		}

		NotificaSoggetto soggettoNotifica = object as NotificaSoggetto

		if (this.utente != null && soggettoNotifica.utente != null && this.utente.id == soggettoNotifica.utente.id) {
			return 0
		}

		if (this.soggetto != null && soggettoNotifica.soggetto != null && this.soggetto.id == soggettoNotifica.soggetto.id) {
			return 0
		}

		if (this.email != null && soggettoNotifica.email != null && this.email == soggettoNotifica.email) {
			return 0
		}

		return 1
	}

	@Override
	boolean equals(Object o) {
		if (!(o instanceof NotificaSoggetto)) {
			return false
		}

		return (compareTo(o as NotificaSoggetto) == 0)
	}

	String toString() {
		String s = ""
		if (unitaSo4 != null) {
			s += "unita: ${unitaSo4.descrizione} (${unitaSo4.progr}, ${unitaSo4.ottica?.codice}), "
		}

		if (utente != null) {
			s += "utente: ${utente.nominativo}, "
		}

		if (email != null) {
			s += "email: ${email}, "
		}

		return s
	}
}