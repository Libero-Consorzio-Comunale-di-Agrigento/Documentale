package it.finmatica.gestionedocumenti.documenti

import it.finmatica.gorm.criteria.PagedResultList
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service

import it.finmatica.gestionedocumenti.commons.Ente
import it.finmatica.gestionedocumenti.exception.GestioneDocumentiRuntimeException


@Service
@Transactional
class TipoCollegamentoService {

	@Autowired SessionFactory sessionFactory

	TipoCollegamentoDTO salva (TipoCollegamentoDTO tipoCollegamentoDto) {
		TipoCollegamento tipoCollegamento = TipoCollegamento.get(tipoCollegamentoDto.id)?:new TipoCollegamento()
		
		tipoCollegamento.codice = tipoCollegamentoDto.codice
		tipoCollegamento.descrizione = tipoCollegamentoDto.descrizione
		tipoCollegamento.commento = tipoCollegamentoDto.commento
		tipoCollegamento.valido = tipoCollegamentoDto.valido

		/*controllo che la versione del DTO sia = a quella appena letta su db: se uguali ok, altrimenti errore*/
		//if(tipoCollegamento.version != tipoCollegamentoDto.version) throw new GestioneDocumentiRuntimeException("Un altro utente ha modificato il dato sottostante, operazione annullata!")
		tipoCollegamento = tipoCollegamento.save ()

		return 	tipoCollegamento.toDTO()
	}

    void elimina (TipoCollegamentoDTO tipoCollegamentoDto) {
		TipoCollegamento tipoCollegamento = TipoCollegamento.get(tipoCollegamentoDto.id)
		/*controllo che la versione del DTO sia = a quella appena letta su db: se uguali ok, altrimenti errore*/
		//if(tipoCollegamento.version != tipoCollegamentoDto.version) throw new GestioneDocumentiRuntimeException("Un altro utente ha modificato il dato sottostante, operazione annullata!")
		tipoCollegamento.delete(failOnError: true)
	}

	PagedResultList list(int pageSize, int activePage, String filterCondition, boolean visualizzaTutti) {
		if(visualizzaTutti) {
			sessionFactory.getCurrentSession().disableFilter("soloValidiFilter")
		}

		try {
			return TipoCollegamento.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
				if(!visualizzaTutti) {
					eq("valido", true)
				}
				if (filterCondition ?: "" != "") ilike("codice", "%${filterCondition}%")
				order("codice", "asc")
				order("descrizione", "asc")
			}
		} finally {
			sessionFactory.getCurrentSession().enableFilter("soloValidiFilter")
		}
	}

	TipoCollegamento findById(Long id) {
		sessionFactory.getCurrentSession().disableFilter("soloValidiFilter")
		try {
			return TipoCollegamento.createCriteria().get {
				eq("id", id)
			}
		} finally {
			sessionFactory.getCurrentSession().enableFilter("soloValidiFilter")
		}
	}

}
