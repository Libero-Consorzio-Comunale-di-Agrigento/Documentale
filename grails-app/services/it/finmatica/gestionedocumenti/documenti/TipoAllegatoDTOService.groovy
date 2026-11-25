package it.finmatica.gestionedocumenti.documenti
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service

import it.finmatica.gestionedocumenti.exception.GestioneDocumentiRuntimeException


@Service
@Transactional
class TipoAllegatoDTOService {

	TipoAllegatoDTO salva (TipoAllegatoDTO tipoAllegatoDto) {
		TipoAllegato tipoAllegato = TipoAllegato.get(tipoAllegatoDto.id)?:new TipoAllegato()
		tipoAllegato.codice = TipoAllegato.CODICE_TIPO_ALLEGATO
		tipoAllegato.acronimo = tipoAllegatoDto.acronimo
		tipoAllegato.descrizione = tipoAllegatoDto.descrizione
		tipoAllegato.commento = tipoAllegatoDto.commento
		tipoAllegato.valido = tipoAllegatoDto.valido
		/*controllo che la versione del DTO sia = a quella appena letta su db: se uguali ok, altrimenti errore*/
		if(tipoAllegato.version != tipoAllegatoDto.version) throw new GestioneDocumentiRuntimeException("Un altro utente ha modificato il dato sottostante, operazione annullata!")
		tipoAllegato = tipoAllegato.save ()

		return 	tipoAllegato.toDTO()
	}

	void elimina (TipoAllegatoDTO tipoAllegatoDto) {
		TipoAllegato tipoAllegato = TipoAllegato.get(tipoAllegatoDto.id)
		/*controllo che la versione del DTO sia = a quella appena letta su db: se uguali ok, altrimenti errore*/
		if(tipoAllegato.version != tipoAllegatoDto.version) throw new GestioneDocumentiRuntimeException("Un altro utente ha modificato il dato sottostante, operazione annullata!")
		/*controllo che il tipo non sia già usato da qualche allegato (vincolo GDO_ALLE_TIAL_FK)*/
		if(Allegato.countByTipoAllegato(tipoAllegato) > 0) throw new GestioneDocumentiRuntimeException("Tipo documento non eliminabile in quanto già associato ad un protocollo.")
		tipoAllegato.delete(failOnError: true)
	}

}
