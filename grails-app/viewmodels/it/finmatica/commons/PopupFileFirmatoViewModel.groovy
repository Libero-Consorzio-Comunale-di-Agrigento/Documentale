package it.finmatica.commons

import it.finmatica.firmadigitale.utils.VerificatoreFirma
import it.finmatica.gestionedocumenti.documenti.Documento
import it.finmatica.gestionedocumenti.documenti.FileDocumento
import it.finmatica.gestionedocumenti.documenti.IGestoreFile
import it.finmatica.gestionedocumenti.documenti.beans.FileDownloader
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.ContextParam
import org.zkoss.bind.annotation.ContextType
import org.zkoss.bind.annotation.ExecutionArgParam
import org.zkoss.bind.annotation.Init
import org.zkoss.util.media.AMedia
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.select.annotation.VariableResolver
import org.zkoss.zk.ui.select.annotation.WireVariable
import org.zkoss.zkplus.spring.DelegatingVariableResolver
import org.zkoss.zul.Filedownload
import org.zkoss.zul.Window

// TODO[SPRINGBOOT]: probabilmente questa classe non serve
@VariableResolver(DelegatingVariableResolver)
class PopupFileFirmatoViewModel {

	// services
	@WireVariable private IGestoreFile 	     gestoreFile
	@WireVariable private FileDownloader 		 fileDownloader

	// componenti
	Window self

	// dati
	def documentoDto
	long idFileAllegato
	def risultatiVerifica
	String nomeFile

	// stato
	boolean solalettura = true
	boolean storico     = false
	boolean copiaTesto  = false

	@Init init (@ContextParam(ContextType.COMPONENT)Window 	w
			  , @ExecutionArgParam("documento") doc
			  , @ExecutionArgParam("nomeFile") 		String 	nomeFile
			  , @ExecutionArgParam("idFileAllegato")long 	idFileAllegato
			  , @ExecutionArgParam("storico") 		boolean storico
			  , @ExecutionArgParam("solalettura") 	boolean solalettura) {

		self				= w
		documentoDto 		= doc
		this.idFileAllegato = idFileAllegato
		this.solalettura 	= solalettura
		this.storico 		= storico
		this.nomeFile 		= nomeFile
		this.copiaTesto		= !storico && doc.hasProperty("testoOdt") && doc.testoOdt != null
	}

	@Command onChiudi () {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}

	@Command onP7m () {
		def documento = documentoDto.getDomainObject()
		def fAllegato = (storico ? FileAllegatoStorico.get(idFileAllegato) : FileAllegato.get(idFileAllegato))
		fileDownloader.downloadFile(documento, fAllegato, solalettura, false)
    }

	@Command onSbusta () {
		def documento = documentoDto.getDomainObject()
		def fAllegato = (storico ? FileAllegatoStorico.get(idFileAllegato) : FileAllegato.get(idFileAllegato))
		fileDownloader.downloadFile(documento, fAllegato, solalettura, true)
    }

	@Command onCopia () {
		Documento documento = documentoDto.getDomainObject()
		AMedia media = new AMedia(documento.testoOdt.nome, null, documento.testoOdt.contentType, new ByteArrayInputStream(documento.testoOdt.allegato));
		Filedownload.save(media)
		//Filedownload.save(new ByteArrayInputStream(documento.testoOdt.allegato), documento.testoOdt.contentType, documento.testoOdt.nome)
    }

	@Command onVerifica () {
		def documento = documentoDto.getDomainObject()
		def fAllegato = FileDocumento.get(idFileAllegato)
		InputStream inputStream = (storico ? gestoreFile.getFileStorico(documento, fAllegato) : gestoreFile.getFile(documento, fAllegato))
		risultatiVerifica = new VerificatoreFirma(inputStream).verificaFirma()
		BindUtils.postNotifyChange(null, null, this, "risultatiVerifica")
	}
}
