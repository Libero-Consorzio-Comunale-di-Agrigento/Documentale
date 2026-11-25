package it.finmatica.gestionedocumenti.registri

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import it.finmatica.gestionedocumenti.exception.GestioneDocumentiRuntimeException

import org.springframework.transaction.annotation.Transactional
import org.zkoss.util.resource.Labels

import java.sql.SQLException

@Transactional
@Service
class RegistroDTOService {

    private @Autowired RegistroService registroService

    public RegistroDTO salva(RegistroDTO registroDto){

		try{
			Registro registro = Registro.get(registroDto.id)?:new Registro()

			//controllo concorrenza
			if (registro.version != registroDto.version)
				throw new RuntimeException (Labels.getLabel("dizionario.recordModificatoAltroUtente"))


			registro.anno = registroDto.anno
			registro.ultimoNumero = registroDto.ultimoNumero
			registro.dataUltimoNumero = new Date()
			registro.valido = registroDto.valido
			registro.tipoRegistro = TipoRegistro.get(registroDto.tipoRegistro.codice)
			registro = registro.save()
			return registro.toDTO()
		}catch (SQLException e){
			throw new GestioneDocumentiRuntimeException(e)
		}
	}

	public void elimina(RegistroDTO registroDto){
		try{
			Registro registro = Registro.get(registroDto.id)
			//controllo concorrenza
			if (registro.version != registroDto.version)
				throw new RuntimeException (Labels.getLabel("dizionario.recordModificatoAltroUtente"))

			registro.delete()
		}catch (SQLException e){
			throw new GestioneDocumentiRuntimeException(e)
		}
	}

	public void chiudiRegistro (RegistroDTO registro) {
		try{
			Registro r = registro.domainObject
			registroService.chiudiRegistro(r.tipoRegistro, registro.anno)
		}catch (SQLException e){
			throw new GestioneDocumentiRuntimeException(e)
		}
	}

	public void riapriRegistro (RegistroDTO registro) {

		Registro r = registro.domainObject
		registroService.riapriRegistro(r)
	}
	
	public TipoRegistroDTO salva(TipoRegistroDTO tipoRegistroDto) {

		try{

			TipoRegistro tipoRegistro = TipoRegistro.get(tipoRegistroDto.codice)?:new TipoRegistro()

			// controllo concorrenza
			if (tipoRegistro.version != tipoRegistroDto.version)
				throw new GestioneDocumentiRuntimeException (Labels.getLabel("dizionario.recordModificatoAltroUtente"))

			tipoRegistro.codice 		= tipoRegistroDto.codice
			tipoRegistro.commento 		= tipoRegistroDto.commento

			tipoRegistro.registroEsterno 	= tipoRegistroDto.registroEsterno
			tipoRegistro.rinnovoAutomatico 		= tipoRegistroDto.rinnovoAutomatico
			tipoRegistro.chiusuraAutomatica = tipoRegistroDto.rinnovoAutomatico ? tipoRegistroDto.chiusuraAutomatica : false;

			tipoRegistro.valido 			= tipoRegistroDto.valido

			tipoRegistro = tipoRegistro.save()

			return tipoRegistro.toDTO()

		}catch (SQLException e){
			throw new GestioneDocumentiRuntimeException(e)
		}
	}
	
	public void elimina(TipoRegistroDTO tipoRegistroDto){

		try {
			TipoRegistro tipoRegistro = TipoRegistro.get(tipoRegistroDto.codice)

			// controllo concorrenza
			if (tipoRegistro.version != tipoRegistroDto.version)
				throw new GestioneDocumentiRuntimeException(Labels.getLabel("dizionario.recordModificatoAltroUtente"))

			// controllo registri presenti

			List registri = Registro.createCriteria().list {
					eq ("tipoRegistro.codice", tipoRegistroDto?.codice)
			}
			if (registri.size()>0)
				throw new GestioneDocumentiRuntimeException(Labels.getLabel("label.dizionario.registro.calcellazioneTipoRegistroNonVuoto"))

			tipoRegistro.delete()
		}catch (SQLException e){
			throw new GestioneDocumentiRuntimeException(e)
		}
	}
}
