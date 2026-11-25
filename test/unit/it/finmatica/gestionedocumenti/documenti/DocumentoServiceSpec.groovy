package it.finmatica.gestionedocumenti.documenti

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.gestionedocumenti.commons.Ad4Service
import it.finmatica.gestionedocumenti.impostazioni.Impostazioni
import it.finmatica.gestionedocumenti.impostazioni.ImpostazioniMap
import it.finmatica.gestionedocumenti.test.GestioneDocumentiSpecification
import it.finmatica.gestionetesti.GestioneTestiService
import it.finmatica.gestionetesti.Holders
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.gestionetesti.reporter.GestioneTestiModelloDTO
import it.finmatica.gestionetesti.reporter.GestioneTestiTipoModello
import it.finmatica.gestionetesti.reporter.GestioneTestiTipoModelloDTO
import it.finmatica.so4.login.So4SpringSecurityService
import it.finmatica.so4.login.So4UserDetail
import it.finmatica.so4.struttura.So4Amministrazione
import org.apache.commons.lang.math.RandomUtils

class DocumentoServiceSpec extends GestioneDocumentiSpecification {

    private DocumentoService service

    void setup() {
        service = new DocumentoService()
        service.ad4Service = Mock(Ad4Service)
        service.gestioneTestiService = Mock(GestioneTestiService)
        service.springSecurityService = springSecurityService
        service.gestoreFile = Mock(IGestoreFile)
        Impostazioni.map = Mock(ImpostazioniMap)
        Impostazioni.map.getValore(_, _) >> 'odt'
    }

    void "test so4 user detail"() {
        setup:
        Ad4Utente utente = new Ad4Utente(nominativo: "re.n", utente: "re.n").save(failOnError: true, flush: true)
        As4SoggettoCorrente soggettoCorrente = new As4SoggettoCorrente(nome: "mario", cognome: "rossi", utenteAd4: utente)
        soggettoCorrente.id = 101
        soggettoCorrente.save(failOnError: true, flush: true)

        So4SpringSecurityService springSecurityService = Mock(So4SpringSecurityService)
        So4UserDetail so4UserDetail = new So4UserDetail(utente, [], [])
        springSecurityService.getPrincipal() >> so4UserDetail

        expect:
        so4UserDetail.getUtente() != null
    }

    void 'il primo editaTesto deve creare il testo dal template'() {
        setup:
        DocumentoDTO documentoDto = Mock(DocumentoDTO)
        Documento documento = new Documento(statoFirma: StatoFirma.DA_FIRMARE)
        documentoDto.getDomainObject() >> documento

        FileDocumentoDTO fileDocumentoDto = Mock(FileDocumentoDTO)
        FileDocumento fileDocumento = new FileDocumento(modificabile: true, contentType: 'application/pdf', nome: 'test.odt', documento: documento
                , modelloTesto: new GestioneTestiModello(fileTemplate: 'template'.bytes, tipoModello: new GestioneTestiTipoModello(codice: 'TESTO', query: '<root/>'.bytes)))
        fileDocumentoDto.getDomainObject() >> fileDocumento

        when:
        service.editaTesto(documentoDto, fileDocumentoDto)

        then:
        1 * service.gestioneTestiService.stampaUnione({ InputStream it -> new String(it.bytes) == 'template' }, _, _, _, _)

        when: 'il secondo editaTesto deve creare il testo dal file esistente'
        fileDocumento.idFileEsterno = 10
        service.gestoreFile.getFile(_, _, _) >> new ByteArrayInputStream('file di testo'.bytes)
        service.editaTesto(documentoDto, fileDocumentoDto)

        then:
        1 * service.gestioneTestiService.stampaUnione({ InputStream it -> new String(it.bytes) == 'file di testo' }, _, _, _, _)
    }

    void "test gestionetestimodello"() {
        when:
        GestioneTestiTipoModello tipoModello = new GestioneTestiTipoModello(codice: 'TESTO', descrizione: 'test', query: '<root/>'.bytes)
        tipoModello.utenteIns = Holders.getApplicationContext().getBean(So4SpringSecurityService).getPrincipal().utente
        tipoModello.utenteUpd = Holders.getApplicationContext().getBean(So4SpringSecurityService).getPrincipal().utente
        tipoModello.ente = Holders.getApplicationContext().getBean(So4SpringSecurityService).getPrincipal().amministrazione

        then:
        tipoModello.ente.soggetto != null
    }

    void "test gestionetestimodello 2"() {
        when:
        As4SoggettoCorrente soggetto = new As4SoggettoCorrente()
        soggetto.id = RandomUtils.nextLong()
        soggetto.denominazione = "test"
        soggetto.nome = "test"
        soggetto.cognome = "test"
        soggetto.save(failOnError: true, flush: true)

        So4Amministrazione amm = new So4Amministrazione()
        amm.codice = "PIPPO"
        amm.soggetto = soggetto
        amm.save(failOnError: true, flush: true)

        then:
        amm != null
        amm.soggetto != null

        when:
        GestioneTestiTipoModello tipoModello = new GestioneTestiTipoModello(codice: 'TESTO', descrizione: 'test', query: '<root/>'.bytes)
        tipoModello.utenteIns = Holders.getApplicationContext().getBean(So4SpringSecurityService).getPrincipal().utente
        tipoModello.utenteUpd = Holders.getApplicationContext().getBean(So4SpringSecurityService).getPrincipal().utente
        tipoModello.ente = amm
        tipoModello.save(failOnError: true, flush: true)

        then:
        tipoModello.ente.soggetto != null
    }

    void 'se faccio edita testo su un documento di cui ho appena eliminato il testo e cambiato il modello testo, il testo generato deve usare il secondo modello testo'() {
        setup:
        Documento documento = new Documento(statoFirma: StatoFirma.DA_FIRMARE).save(failOnError: true)
        GestioneTestiTipoModello tipoModello = new GestioneTestiTipoModello(codice: 'TESTO', descrizione: 'test', query: '<root/>'.bytes).save(failOnError: true)
        GestioneTestiModello primoModelloTesto = new GestioneTestiModello(tipo: 'odt', nome: 'primo modello', fileTemplate: 'primo template'.bytes, tipoModello: tipoModello).save(failOnError: true)
        GestioneTestiModello secondoModelloTesto = new GestioneTestiModello(tipo: 'odt', nome: 'secondo modello', fileTemplate: 'secondo template'.bytes, tipoModello: tipoModello).save(failOnError: true)
        FileDocumento fileDocumento = new FileDocumento(modificabile: true, contentType: 'application/pdf', nome: 'test.odt', documento: documento, modelloTesto: primoModelloTesto)
        documento.addToFileDocumenti(fileDocumento)
        documento.save(failOnError: true, flush: true)

        when:
        FileDocumentoDTO fileDocumentoDTO = new FileDocumentoDTO(id: fileDocumento.id, nome: fileDocumento.nome, contentType: fileDocumento.contentType, modelloTesto: new GestioneTestiModelloDTO(id: secondoModelloTesto.id, tipoModello: new GestioneTestiTipoModelloDTO(codice: tipoModello.codice)))
        service.editaTesto(new DocumentoDTO(id: documento.id), fileDocumentoDTO)

        then:
        1 * service.gestioneTestiService.stampaUnione({ InputStream it -> new String(it.bytes) == 'secondo template' }, _, _, _, _)
    }
}
