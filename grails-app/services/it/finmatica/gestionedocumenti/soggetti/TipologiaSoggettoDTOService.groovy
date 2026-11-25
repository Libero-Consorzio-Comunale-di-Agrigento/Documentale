package it.finmatica.gestionedocumenti.soggetti
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service

@Service
@Transactional
class TipologiaSoggettoDTOService {

    TipologiaSoggettoDTO salva (TipologiaSoggettoDTO tipologiaSoggettoDTO, List<TipologiaSoggettoRegolaDTO> tipiSoggettoDTO = null) {
		TipologiaSoggetto tipologiaSoggetto = TipologiaSoggetto.get(tipologiaSoggettoDTO.id)?:new TipologiaSoggetto()

		tipologiaSoggetto.commento 		= tipologiaSoggettoDTO.commento
		tipologiaSoggetto.descrizione 	= tipologiaSoggettoDTO.descrizione
		tipologiaSoggetto.tipoOggetto 	= tipologiaSoggettoDTO.tipoOggetto?.domainObject
		tipologiaSoggetto.valido 		= tipologiaSoggettoDTO.valido
		tipologiaSoggetto.layoutSoggetti = tipologiaSoggettoDTO.layoutSoggetti

		// devo inserire/aggiornare/eliminare i tipiSoggetto di questa tipologia
		// elimino da tipologia tutti quelli che non ci sono in tipiSoggettoDTO
		tipologiaSoggetto.regole.findAll { s -> tipiSoggettoDTO.find { it.tipoSoggetto == s?.tipoSoggetto } == null }.each { ts ->
			if(ts != null){
				tipologiaSoggetto?.removeFromRegole (ts)
				ts?.delete()
			}
		}

		// aggiorno / inserisco su tipologia tutti quelli che sono in tipiSoggettoDTO
		for (TipologiaSoggettoRegolaDTO s : tipiSoggettoDTO) {
			TipologiaSoggettoRegola ts = tipologiaSoggetto?.regole.find { it?.tipoSoggetto == s?.tipoSoggetto }

			if (ts == null) {
				ts = new TipologiaSoggettoRegola(tipoSoggetto: s.tipoSoggetto)
				tipologiaSoggetto.addToRegole (ts)
			}

			ts.ruolo		 		= s.ruolo?.domainObject
			ts.regolaLista	 		= s.regolaLista
			ts.regolaDefault 		= s.regolaDefault
			ts.tipoSoggettoPartenza = s.tipoSoggettoPartenza
		}

		tipologiaSoggetto?.save (failOnError: true)

		return tipologiaSoggetto?.toDTO()
    }

    void elimina (TipologiaSoggettoDTO tipologiaSoggettoDTO) {
		tipologiaSoggettoDTO.domainObject.delete(failOnError: true)
	}

}
