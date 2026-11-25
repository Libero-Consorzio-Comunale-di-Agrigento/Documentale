package it.finmatica.gestionedocumenti.integrazioni.firma

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.firmadigitale.FirmaDigitaleFile
import it.finmatica.firmadigitale.FirmaDigitaleTransazione
import it.finmatica.gestionedocumenti.IntegrationSpec
import it.finmatica.gestionedocumenti.documenti.Allegato
import it.finmatica.gestionedocumenti.documenti.Documento
import it.finmatica.gestionedocumenti.documenti.FileDocumento
import it.finmatica.gestionedocumenti.documenti.IGestoreFile
import it.finmatica.gestionedocumenti.documenti.StatoFirma
import it.finmatica.gestionedocumenti.exception.GestioneDocumentiRuntimeException
import it.finmatica.gestionedocumenti.integrazioni.gdm.IntegrazioneGdmService
import it.finmatica.gestionedocumenti.testutils.TestUtils
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import it.finmatica.so4.login.So4SpringSecurityService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore

import javax.sql.rowset.serial.SerialBlob

class GestioneDocumentiFirmaServiceIntegrationSpec extends IntegrationSpec {

    @Autowired
    private GestioneDocumentiFirmaService gestioneDocumentiFirmaService
    @Autowired
    private IntegrazioneGdmService integrazioneGdmService
    @Autowired
    private So4SpringSecurityService springSecurityService
    @Autowired
    private IGestoreFile gestoreFile

    private Documento lettera
    private WkfTipoOggetto tipoOggetto

    void setup() {
        TestUtils.setUpEnte()
        TestUtils.login()

        tipoOggetto = new WkfTipoOggetto(codice: "TEST_LETT", nome: "Lettera", descrizione: "Lettera")
        tipoOggetto.save()

        lettera = new Documento(riservato: true, tipoOggetto: tipoOggetto)
        lettera.id = 100
        lettera.statoFirma = StatoFirma.DA_FIRMARE
        lettera.dateCreated = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "1984-06-22T10:12:13.444Z")
        lettera.save()
    }

    def cleanup() {
        TestUtils.logout()
//        gestioneDocumentiFirmaService.gestoreTransazioneFirma.idTransazioneFirma = -1
    }

    void "firma di un file con la nuova firma"() {

    }

    @Ignore
    // firma di un file
    void "firma di un file"() {
        setup:
        TestUtils.setUpEnte()
        TestUtils.login()

        FileDocumento filePrincipale = new FileDocumento(nome: "test.odt", contentType: "application/odt", codice: FileDocumento.CODICE_FILE_PRINCIPALE)
        lettera.addToFileDocumenti(filePrincipale)
        gestoreFile.addFile(lettera, filePrincipale, new File("test/resources/TEST_LETTERA.odt").newInputStream())

        Ad4Utente firmatario = springSecurityService.currentUser

        when: "aggiungo il firmatario alla coda dei firmatari"
        gestioneDocumentiFirmaService.aggiungiFirmatarioAllaCoda(lettera, firmatario)
        then: "in coda ci deve essere un solo firmatario"
        CodaFirma.firmatariPerDocumento(lettera).list().size() == 1
        CodaFirma.prossimoInCoda(lettera).get().firmatario.id == firmatario.id
        CodaFirma.inCodaPerFirmare(lettera).list().size() == 1

        when: "aggiungo di nuovo il firmatario alla coda dei firmatari"
        gestioneDocumentiFirmaService.aggiungiFirmatarioAllaCoda(lettera, firmatario)
        then: "in coda ci deve essere comunque un solo firmatario"
        CodaFirma.firmatariPerDocumento(lettera).list().size() == 1
        CodaFirma.prossimoInCoda(lettera).get().firmatario.id == firmatario.id
        CodaFirma.inCodaPerFirmare(lettera).list().size() == 1

        when: "preparo il firmatario in coda per la firma"
        gestioneDocumentiFirmaService.preparaFirmatarioInCoda(lettera)
        then: "il firmatario deve essere il primo"
        CodaFirma.inCodaPerFirmare(lettera).count() == 0
        CodaFirma.inCodaPerFirmare(lettera, firmatario).count() == 0
        CodaFirma.cheStaFirmando(lettera, firmatario).get().firmatario.id == firmatario.id

        when: "aggiungo il testo del documento da firmare"
        gestioneDocumentiFirmaService.preparaFilePerFirma(lettera.filePrincipale)
        // TODO: sarebbe da migliorare il plugin di firma così da poter fare dei test più agevolmente
        FirmaDigitaleTransazione transazione = FirmaDigitaleTransazione.get(gestioneDocumentiFirmaService.gestoreTransazioneFirma.idTransazioneFirma)
        then: "il testo principale viene aggiunto alla firma"
        transazione.firmaFile.size() == 1
        transazione.firmaFile[0].nome == lettera.filePrincipale.nome

        when: "ottengo l'url della pagina da aprire"
        gestioneDocumentiFirmaService.finalizzaTransazioneFirma(false, Impostazioni.FIRMA_PADES.abilitato)
        String url = gestioneDocumentiFirmaService.getUrlPopupFirma()
        then: "l'url da aprire non è nullo"
        url != null

        when: "simulo la firma caricando un file firmato nelle tabelle di appoggio della firma digitale, quindi invoco il controller di fine firma"
        // tengo da conto i dati del file principale prima della firma
        long dimensioneFile = lettera.filePrincipale.dimensione
        String contentType = lettera.filePrincipale.contentType
        String nomeFile = lettera.filePrincipale.nome

        // simulo la firma digitale
        FirmaDigitaleFile fileFirmato = transazione.firmaFile[0]
        fileFirmato.nomeFirmato = fileFirmato.nome + ".p7m"

        // normalmente il "fileDaFirmare" non sarebbe null, qui lo faccio perché altrimenti Hibernate tenta di rileggerne il Blob e va in errore: http://stackoverflow.com/questions/7634638/hibernate-throws-sqlexception-could-not-reset-reader
        fileFirmato.fileDaFirmare = null
        fileFirmato.fileFirmato = new SerialBlob(new File("test/resources/TEST_LETTERA.odt.p7m").getBytes())
        transazione.dataFirma = new Date()
        transazione.utente = springSecurityService.principal.id
        transazione.save()

        // simulo l'invocazione al fineFirmaController
//        FineFirmaController fineFirmaController = new FineFirmaController()
//        fineFirmaController.gestioneDocumentiFirmaService = gestioneDocumentiFirmaService
//        fineFirmaController.springSecurityService = springSecurityService
//        fineFirmaController.successHandler = new SuccessHandler()
//        fineFirmaController.params.idTransazioneFirma = transazione.id
//        fineFirmaController.termina()

        FileDocumento fileStorico = lettera.filePrincipale.getFileStorico(FileDocumento.CODICE_FILE_ORIGINALE)
        // ricarico la lettera siccome la funzione "termina" del controller pulisce la sessione hibernate
        lettera = Documento.findById(lettera.id)
        then: "il file del documento deve essere effettivamente il file firmato, deve essere presente il file di storico originale"
        TestUtils.streamEquals(gestoreFile.getFile(lettera, lettera.filePrincipale), new File("test/resources/TEST_LETTERA.odt.p7m").newInputStream())
        TestUtils.streamEquals(integrazioneGdmService.getFileStorico(fileStorico), new File("test/resources/TEST_LETTERA.odt").newInputStream())
        lettera.filePrincipale.firmatari.size() == 1
        lettera.filePrincipale.firmatari[0].sequenza == 0
        lettera.filePrincipale.firmatari[0].utenteAd4.id == springSecurityService.principal.id
        // devo fare .clearTime perché dopo che la domain viene riletta non ha i millisecondi perché non vengono salvati su db.
        lettera.filePrincipale.firmatari[0].dataFirma.clearTime() == transazione.dataFirma.clearTime()
        lettera.statoFirma == StatoFirma.FIRMATO
        lettera.filePrincipale.firmato == true
        lettera.filePrincipale.nome.endsWith("p7m") == true
        fileStorico != null
        fileStorico.nome == nomeFile
        fileStorico.dimensione == dimensioneFile
        fileStorico.contentType == contentType
    }

    @Ignore
    void "firma di più file e file allegati"() {
        setup:
        TestUtils.setUpEnte("P_MO", "1", "AOOPMO")
        TestUtils.login("romagnoli.p", "P_MO")
        TestUtils.setUpTipoCollegamento(Allegato.CODICE_TIPO_COLLEGAMENTO, "Allegato")

        Documento lettera = TestUtils.setUpDocumento()
        TestUtils.setUpFile(lettera, FileDocumento.CODICE_FILE_PRINCIPALE)
        lettera.statoFirma = StatoFirma.DA_FIRMARE
        lettera.save()

        Allegato allegato1 = TestUtils.setUpAllegato()
        TestUtils.setUpFile(allegato1, FileDocumento.CODICE_FILE_ALLEGATO)
        allegato1.statoFirma = StatoFirma.DA_FIRMARE
        allegato1.save()

        Allegato allegato2 = TestUtils.setUpAllegato()
        TestUtils.setUpFile(allegato2, FileDocumento.CODICE_FILE_ALLEGATO)
        allegato2.statoFirma = StatoFirma.DA_FIRMARE
        allegato2.save()

        lettera.addDocumentoCollegato(allegato1, Allegato.CODICE_TIPO_COLLEGAMENTO)
        lettera.addDocumentoCollegato(allegato2, Allegato.CODICE_TIPO_COLLEGAMENTO)
        lettera.save()

        when:
        gestioneDocumentiFirmaService.aggiungiFirmatarioAllaCoda(lettera, springSecurityService.currentUser)
        gestioneDocumentiFirmaService.preparaFirmatarioInCoda(lettera)
        gestioneDocumentiFirmaService.preparaFilePerFirma(lettera.filePrincipale)
        for (Allegato allegato : lettera.getDocumentiCollegati(Allegato.CODICE_TIPO_COLLEGAMENTO)) {
            gestioneDocumentiFirmaService.preparaFilePerFirma(allegato.getListaFile(FileDocumento.CODICE_FILE_ALLEGATO))
        }
        gestioneDocumentiFirmaService.finalizzaTransazioneFirma(false, Impostazioni.FIRMA_PADES.abilitato)

        // simulo la firma digitale:
        FirmaDigitaleTransazione transazione = FirmaDigitaleTransazione.get(gestioneDocumentiFirmaService.gestoreTransazioneFirma.idTransazioneFirma)
        for (FirmaDigitaleFile fileFirmato : transazione.firmaFile) {
            fileFirmato.nomeFirmato = fileFirmato.nome + ".p7m"

            // normalmente il "fileDaFirmare" non sarebbe null, qui lo faccio perché altrimenti Hibernate tenta di rileggerne il Blob e va in errore: http://stackoverflow.com/questions/7634638/hibernate-throws-sqlexception-could-not-reset-reader
            fileFirmato.fileDaFirmare = null
            fileFirmato.fileFirmato = new SerialBlob(new File("test/resources/TEST_LETTERA.odt.p7m").getBytes())
        }
        transazione.dataFirma = new Date()
        transazione.utente = springSecurityService.principal.id
        transazione.save()

        // simulo il FineFirmaController
        // simulo l'invocazione al fineFirmaController
//        FineFirmaController fineFirmaController = new FineFirmaController()
//        fineFirmaController.gestioneDocumentiFirmaService = gestioneDocumentiFirmaService
//        fineFirmaController.springSecurityService = springSecurityService
//        fineFirmaController.successHandler = new SuccessHandler()
//        fineFirmaController.params.idTransazioneFirma = transazione.id
//        fineFirmaController.termina()

        // siccome viene invalidata la sessione hibernate, devo ricaricare tutto:
        lettera = Documento.findById(lettera.id)
        allegato1 = Allegato.findById(allegato1.id)
        allegato2 = Allegato.findById(allegato2.id)

        then: "verifico che il file sia firmato, che i documenti risultino firmati e che i vari file risultino firmati e storicizzati"
        lettera.statoFirma == StatoFirma.FIRMATO
        allegato1.statoFirma == StatoFirma.FIRMATO
        allegato2.statoFirma == StatoFirma.FIRMATO
        TestUtils.streamEquals(gestoreFile.getFile(lettera, lettera.filePrincipale), new File("test/resources/TEST_LETTERA.odt.p7m").newInputStream())
        TestUtils.streamEquals(gestoreFile.getFile(lettera, lettera.filePrincipale.getFileStorico(FileDocumento.CODICE_FILE_ORIGINALE)), new File("test/resources/TEST_LETTERA.odt.p7m").newInputStream())
        TestUtils.streamEquals(gestoreFile.getFile(allegato1, allegato1.getListaFile(FileDocumento.CODICE_FILE_ALLEGATO).first()), new File("test/resources/TEST_LETTERA.odt.p7m").newInputStream())
        TestUtils.streamEquals(integrazioneGdmService.getFileStorico(allegato1.getListaFile(FileDocumento.CODICE_FILE_ALLEGATO).first().getFileStorico(FileDocumento.CODICE_FILE_ORIGINALE)), new File("test/resources/TEST_LETTERA.odt").newInputStream())
        TestUtils.streamEquals(gestoreFile.getFile(allegato2, allegato2.getListaFile(FileDocumento.CODICE_FILE_ALLEGATO).first()), new File("test/resources/TEST_LETTERA.odt.p7m").newInputStream())
        TestUtils.streamEquals(integrazioneGdmService.getFileStorico(allegato2.getListaFile(FileDocumento.CODICE_FILE_ALLEGATO).first().getFileStorico(FileDocumento.CODICE_FILE_ORIGINALE)), new File("test/resources/TEST_LETTERA.odt").newInputStream())
    }

    @Ignore
    void "firma di un file allegato ma non del documento principale"() {
        setup:
        TestUtils.setUpEnte("P_MO", "1", "AOOPMO")
        TestUtils.login("romagnoli.p", "P_MO")
        TestUtils.setUpTipoCollegamento(Allegato.CODICE_TIPO_COLLEGAMENTO, "Allegato")

        Documento lettera = TestUtils.setUpDocumento()
        TestUtils.setUpFile(lettera, FileDocumento.CODICE_FILE_PRINCIPALE)
        lettera.statoFirma = StatoFirma.DA_FIRMARE
        lettera.save()

        Allegato allegato1 = TestUtils.setUpAllegato()
        TestUtils.setUpFile(allegato1, FileDocumento.CODICE_FILE_ALLEGATO)
        allegato1.statoFirma = StatoFirma.DA_FIRMARE
        allegato1.save()

        Allegato allegato2 = TestUtils.setUpAllegato()
        TestUtils.setUpFile(allegato2, FileDocumento.CODICE_FILE_ALLEGATO)
        allegato2.statoFirma = StatoFirma.DA_FIRMARE
        allegato2.save()

        lettera.addDocumentoCollegato(allegato1, Allegato.CODICE_TIPO_COLLEGAMENTO)
        lettera.addDocumentoCollegato(allegato2, Allegato.CODICE_TIPO_COLLEGAMENTO)
        lettera.save()

        when:
        gestioneDocumentiFirmaService.aggiungiFirmatarioAllaCoda(lettera, springSecurityService.currentUser)
        gestioneDocumentiFirmaService.preparaFirmatarioInCoda(lettera)
        for (Allegato allegato : lettera.getDocumentiCollegati(Allegato.CODICE_TIPO_COLLEGAMENTO)) {
            gestioneDocumentiFirmaService.preparaFilePerFirma(allegato.getListaFile(FileDocumento.CODICE_FILE_ALLEGATO))
        }
        gestioneDocumentiFirmaService.finalizzaTransazioneFirma(false, Impostazioni.FIRMA_PADES.abilitato)

        // simulo la firma digitale:
        FirmaDigitaleTransazione transazione = FirmaDigitaleTransazione.get(gestioneDocumentiFirmaService.gestoreTransazioneFirma.idTransazioneFirma)
        for (FirmaDigitaleFile fileFirmato : transazione.firmaFile) {
            fileFirmato.nomeFirmato = fileFirmato.nome + ".p7m"

            // normalmente il "fileDaFirmare" non sarebbe null, qui lo faccio perché altrimenti Hibernate tenta di rileggerne il Blob e va in errore: http://stackoverflow.com/questions/7634638/hibernate-throws-sqlexception-could-not-reset-reader
            fileFirmato.fileDaFirmare = null
            fileFirmato.fileFirmato = new SerialBlob(new File("test/resources/TEST_LETTERA.odt.p7m").getBytes())
        }
        transazione.dataFirma = new Date()
        transazione.utente = springSecurityService.principal.id
        transazione.save()

        // simulo il FineFirmaController
        // simulo l'invocazione al fineFirmaController
//        FineFirmaController fineFirmaController = new FineFirmaController()
//        fineFirmaController.gestioneDocumentiFirmaService = gestioneDocumentiFirmaService
//        fineFirmaController.springSecurityService = springSecurityService
//        fineFirmaController.successHandler = new SuccessHandler()
//        fineFirmaController.params.idTransazioneFirma = transazione.id
//        fineFirmaController.termina()

        // siccome viene invalidata la sessione hibernate, devo ricaricare tutto:
        lettera = Documento.findById(lettera.id)
        allegato1 = Allegato.findById(allegato1.id)
        allegato2 = Allegato.findById(allegato2.id)

        then: "verifico che il file sia firmato, che i documenti risultino firmati e che i vari file risultino firmati e storicizzati"
        lettera.statoFirma == StatoFirma.FIRMATO
        allegato1.statoFirma == StatoFirma.FIRMATO
        allegato2.statoFirma == StatoFirma.FIRMATO
        TestUtils.streamEquals(gestoreFile.getFile(allegato1, allegato1.getListaFile(FileDocumento.CODICE_FILE_ALLEGATO).first()), new File("test/resources/TEST_LETTERA.odt.p7m").newInputStream())
        TestUtils.streamEquals(integrazioneGdmService.getFileStorico(allegato1.getListaFile(FileDocumento.CODICE_FILE_ALLEGATO).first().getFileStorico(FileDocumento.CODICE_FILE_ORIGINALE)), new File("test/resources/TEST_LETTERA.odt").newInputStream())
        TestUtils.streamEquals(gestoreFile.getFile(allegato2, allegato2.getListaFile(FileDocumento.CODICE_FILE_ALLEGATO).first()), new File("test/resources/TEST_LETTERA.odt.p7m").newInputStream())
        TestUtils.streamEquals(integrazioneGdmService.getFileStorico(allegato2.getListaFile(FileDocumento.CODICE_FILE_ALLEGATO).first().getFileStorico(FileDocumento.CODICE_FILE_ORIGINALE)), new File("test/resources/TEST_LETTERA.odt").newInputStream())
    }

    @Ignore
    void "non è possibile predisporre alla firma dei documenti non validi o in stato non DA_FIRMARE o non IN_FIRMA e dei file non validi"() {
        setup:
        TestUtils.setUpEnte("P_MO", "1", "AOOPMO")
        TestUtils.login("romagnoli.p", "P_MO")

        Documento lettera = TestUtils.setUpDocumento()
        lettera.valido = documentoValido
        lettera.statoFirma = statoDocumento
        lettera.save()
        FileDocumento fileDocumento = TestUtils.setUpFile(lettera)
        fileDocumento.valido = fileValido
        fileDocumento.save()

        gestioneDocumentiFirmaService.preparaFilePerFirma(fileDocumento)

        expect:
        gestioneDocumentiFirmaService.gestoreTransazioneFirma.getIdTransazioneFirma() <= 0

        where:
        documentoValido | fileValido | statoDocumento
        false           | false      | StatoFirma.DA_FIRMARE
        true            | false      | StatoFirma.DA_FIRMARE
        true            | false      | StatoFirma.IN_FIRMA
        true            | true       | StatoFirma.DA_NON_FIRMARE
    }

    @Ignore
    void "non è possibile proseguire se non è stata creata la transazione di firma"() {
        setup:
        TestUtils.setUpEnte("P_MO", "1", "AOOPMO")
        TestUtils.login("romagnoli.p", "P_MO")

        when:
        gestioneDocumentiFirmaService.finalizzaTransazioneFirma(false, Impostazioni.FIRMA_PADES.abilitato)
        then:
        def e = thrown(GestioneDocumentiRuntimeException)
        e.message == "Non è stata avviata nessuna transazione di firma."
    }

    @Ignore
    void gestioneCodaFirmatari() {
        setup:
        TestUtils.setUpEnte("P_MO", "1", "AOOPMO")
        TestUtils.login("romagnoli.p", "P_MO")
        Documento lettera = TestUtils.setUpDocumento()

        Ad4Utente utenteFirmatario1 = Ad4Utente.findByNominativo("ROMAGNOLI.P")
        Ad4Utente utenteFirmatario2 = Ad4Utente.findByNominativo("BINDO.D")

        when: "aggiungo un firmatario alla coda"
        gestioneDocumentiFirmaService.aggiungiFirmatarioAllaCoda(lettera, utenteFirmatario1)
        then: "un firmatario è presente nella coda di firma"
        CodaFirma.firmatariPerDocumento(lettera).count() == 1
        CodaFirma.inCodaPerFirmare(lettera).count() == 1
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario1).count() == 1

        when: "aggiungo lo stesso firmatario alla coda"
        gestioneDocumentiFirmaService.aggiungiFirmatarioAllaCoda(lettera, utenteFirmatario1)
        then: "non viene aggiunto perché già presente che deve firmare"
        CodaFirma.firmatariPerDocumento(lettera).count() == 1
        CodaFirma.inCodaPerFirmare(lettera).count() == 1
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario1).count() == 1

        when: "aggiungo un secondo firmatario alla coda"
        gestioneDocumentiFirmaService.aggiungiFirmatarioAllaCoda(lettera, utenteFirmatario2)
        then: "nella coda sono presenti due firmatari"
        CodaFirma.firmatariPerDocumento(lettera).count() == 2
        CodaFirma.inCodaPerFirmare(lettera).count() == 2
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario1).count() == 1
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario1).get().sequenza == 1
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario2).count() == 1
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario2).get().sequenza == 2

        when: "predispongo il prossimo firmatario per la firma"
        gestioneDocumentiFirmaService.preparaFirmatarioInCoda(lettera, utenteFirmatario1)
        then: "il primo firmatario ha data di firma ma firmato = N, il secondo firmatario è rimasto invariato"
        CodaFirma.inCodaPerFirmare(lettera).count() == 1
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario1).count() == 0
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario2).count() == 1
        CodaFirma.cheStaFirmando(lettera).get().firmatario.id == utenteFirmatario1.id
        CodaFirma.cheStaFirmando(lettera, utenteFirmatario1).get().firmatario.id == utenteFirmatario1.id
        CodaFirma.cheStaFirmando(lettera, utenteFirmatario2).get() == null

        when: "processo il firmatario che ha firmato dopo la firma del documento"
        gestioneDocumentiFirmaService.processaFirmatario(lettera, utenteFirmatario1)
        then: "il primo firmatario ha firmato = Y e data di firma, il secondo firmatario è rimasto invariato"
        CodaFirma.inCodaPerFirmare(lettera).count() == 1
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario1).count() == 0
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario2).count() == 1
        CodaFirma.cheStaFirmando(lettera).get() == null

        when: "predispongo il prossimo firmatario per la firma"
        // simulo una nuova request:
        gestioneDocumentiFirmaService.gestoreTransazioneFirma.idTransazioneFirma = -1
        gestioneDocumentiFirmaService.preparaFirmatarioInCoda(lettera, utenteFirmatario2)
        then: "il primo firmatario rimane invariato, il secondo firmatario ha data firma e firmato = N"
        CodaFirma.inCodaPerFirmare(lettera).count() == 0
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario1).count() == 0
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario2).count() == 0
        CodaFirma.cheStaFirmando(lettera).get().firmatario.id == utenteFirmatario2.id

        when: "processo il firmatario che ha firmato dopo la firma del documento"
        gestioneDocumentiFirmaService.processaFirmatario(lettera, utenteFirmatario2)
        then: "il primo firmatario rimane invariato, il secondo firmatario ha data firma e firmato = Y"
        CodaFirma.inCodaPerFirmare(lettera).count() == 0
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario1).count() == 0
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario2).count() == 0
        CodaFirma.cheStaFirmando(lettera).get() == null

        when: "riaggiungo il primo firmatario alla coda"
        gestioneDocumentiFirmaService.aggiungiFirmatarioAllaCoda(lettera, utenteFirmatario1)
        then: "il firmatario viene riaggiunto perché ha già firmato precedentemente, siccome la coda è stata precedentemente svuotata, il nuovo firmatario avrà sequenza 1"
        CodaFirma.firmatariPerDocumento(lettera).count() == 3
        CodaFirma.inCodaPerFirmare(lettera).count() == 1
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario1).count() == 1
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario1).get().sequenza == 1

        when: "elimino dalla coda un firmatario che deve ancora firmare"
        gestioneDocumentiFirmaService.eliminaUltimoFirmatarioInCoda(lettera)
        then: "il firmatario che non ha ancora firmato viene eliminato"
        CodaFirma.firmatariPerDocumento(lettera).count() == 2
        CodaFirma.inCodaPerFirmare(lettera).count() == 0
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario1).count() == 0
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario1).get() == null

        when: "elimino dalla coda un firmatario che ha già firmato"
        gestioneDocumentiFirmaService.eliminaUltimoFirmatarioInCoda(lettera)
        then: "il firmatario che non viene eliminato"
        CodaFirma.firmatariPerDocumento(lettera).count() == 2
        CodaFirma.inCodaPerFirmare(lettera).count() == 0
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario1).count() == 0
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario1).get() == null
    }

    @Ignore
    void eliminaFirmatarioInCoda() {
        setup:
        TestUtils.setUpEnte("P_MO", "1", "AOOPMO")
        TestUtils.login("romagnoli.p", "P_MO")
        Documento lettera = TestUtils.setUpDocumento()

        Ad4Utente utenteFirmatario1 = Ad4Utente.findByNominativo("ROMAGNOLI.P")
        Ad4Utente utenteFirmatario2 = Ad4Utente.findByNominativo("BINDO.D")

        when: "aggiungo un firmatario alla coda"
        gestioneDocumentiFirmaService.aggiungiFirmatarioAllaCoda(lettera, utenteFirmatario1)
        then: "un firmatario è presente nella coda di firma"
        CodaFirma.firmatariPerDocumento(lettera).count() == 1
        CodaFirma.inCodaPerFirmare(lettera).count() == 1
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario1).count() == 1

        when: "aggiungo lo stesso firmatario alla coda"
        gestioneDocumentiFirmaService.aggiungiFirmatarioAllaCoda(lettera, utenteFirmatario1)
        then: "non viene aggiunto perché già presente che deve firmare"
        CodaFirma.firmatariPerDocumento(lettera).count() == 1
        CodaFirma.inCodaPerFirmare(lettera).count() == 1
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario1).count() == 1

        when: "aggiungo un secondo firmatario alla coda"
        gestioneDocumentiFirmaService.aggiungiFirmatarioAllaCoda(lettera, utenteFirmatario2)
        then: "nella coda sono presenti due firmatari"
        CodaFirma.firmatariPerDocumento(lettera).count() == 2
        CodaFirma.inCodaPerFirmare(lettera).count() == 2
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario1).count() == 1
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario1).get().sequenza == 1
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario2).count() == 1
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario2).get().sequenza == 2

        when: "predispongo il prossimo firmatario per la firma"
        gestioneDocumentiFirmaService.preparaFirmatarioInCoda(lettera, utenteFirmatario1)
        then: "il primo firmatario ha data di firma ma firmato = N, il secondo firmatario è rimasto invariato"
        CodaFirma.inCodaPerFirmare(lettera).count() == 1
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario1).count() == 0
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario2).count() == 1
        CodaFirma.cheStaFirmando(lettera).get().firmatario.id == utenteFirmatario1.id
        CodaFirma.cheStaFirmando(lettera, utenteFirmatario1).get().firmatario.id == utenteFirmatario1.id
        CodaFirma.cheStaFirmando(lettera, utenteFirmatario2).get() == null

        when: "processo il firmatario che ha firmato dopo la firma del documento"
        gestioneDocumentiFirmaService.processaFirmatario(lettera, utenteFirmatario1)
        then: "il primo firmatario ha firmato = Y e data di firma, il secondo firmatario è rimasto invariato"
        CodaFirma.inCodaPerFirmare(lettera).count() == 1
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario1).count() == 0
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario2).count() == 1
        CodaFirma.cheStaFirmando(lettera).get() == null

        when: "predispongo il prossimo firmatario per la firma"
        // simulo una nuova request:
        gestioneDocumentiFirmaService.gestoreTransazioneFirma.idTransazioneFirma = -1
        gestioneDocumentiFirmaService.preparaFirmatarioInCoda(lettera)
        then: "il primo firmatario rimane invariato, il secondo firmatario ha data firma e firmato = N"
        CodaFirma.inCodaPerFirmare(lettera).count() == 0
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario1).count() == 0
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario2).count() == 0
        CodaFirma.cheStaFirmando(lettera).get().firmatario.id == utenteFirmatario2.id

        when: "processo il firmatario che ha firmato dopo la firma del documento"
        gestioneDocumentiFirmaService.processaFirmatario(lettera, utenteFirmatario2)
        then: "il primo firmatario rimane invariato, il secondo firmatario ha data firma e firmato = Y"
        CodaFirma.inCodaPerFirmare(lettera).count() == 0
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario1).count() == 0
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario2).count() == 0
        CodaFirma.cheStaFirmando(lettera).get() == null

        when: "riaggiungo il primo firmatario alla coda"
        gestioneDocumentiFirmaService.aggiungiFirmatarioAllaCoda(lettera, utenteFirmatario1)
        then: "il firmatario viene riaggiunto perché ha già firmato precedentemente, siccome la coda è stata precedentemente svuotata, il nuovo firmatario avrà sequenza 1"
        CodaFirma.firmatariPerDocumento(lettera).count() == 3
        CodaFirma.inCodaPerFirmare(lettera).count() == 1
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario1).count() == 1
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario1).get().sequenza == 1

        when: "elimino dalla coda un firmatario che deve ancora firmare"
        gestioneDocumentiFirmaService.eliminaUltimoFirmatarioInCoda(lettera)
        then: "il firmatario che non ha ancora firmato viene eliminato"
        CodaFirma.firmatariPerDocumento(lettera).count() == 2
        CodaFirma.inCodaPerFirmare(lettera).count() == 0
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario1).count() == 0
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario1).get() == null

        when: "elimino dalla coda un firmatario che ha già firmato"
        gestioneDocumentiFirmaService.eliminaUltimoFirmatarioInCoda(lettera)
        then: "il firmatario che non viene eliminato"
        CodaFirma.firmatariPerDocumento(lettera).count() == 2
        CodaFirma.inCodaPerFirmare(lettera).count() == 0
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario1).count() == 0
        CodaFirma.inCodaPerFirmare(lettera, utenteFirmatario1).get() == null
    }

    @Ignore
    void "elimina firmatario in coda"() {
        setup:
        TestUtils.setUpEnte("P_MO", "1", "AOOPMO")
        TestUtils.login("romagnoli.p", "P_MO")
        Documento lettera = TestUtils.setUpDocumento()

        Ad4Utente utenteFirmatario1 = Ad4Utente.findByNominativo("ROMAGNOLI.P")
        Ad4Utente utenteFirmatario2 = Ad4Utente.findByNominativo("BINDO.D")
        Ad4Utente utenteFirmatario3 = Ad4Utente.findByNominativo("BENASSI.P")
        Ad4Utente utenteFirmatario4 = Ad4Utente.findByNominativo("LOCANE.F")
        Ad4Utente utenteFirmatario5 = Ad4Utente.findByNominativo("MASETTI.F")

        when: "popolo la coda dei firmatari per il documento"
        gestioneDocumentiFirmaService.aggiungiFirmatarioAllaCoda(lettera, utenteFirmatario1)
        gestioneDocumentiFirmaService.aggiungiFirmatarioAllaCoda(lettera, utenteFirmatario2)
        gestioneDocumentiFirmaService.aggiungiFirmatarioAllaCoda(lettera, utenteFirmatario3)
        gestioneDocumentiFirmaService.aggiungiFirmatarioAllaCoda(lettera, utenteFirmatario4)
        gestioneDocumentiFirmaService.aggiungiFirmatarioAllaCoda(lettera, utenteFirmatario5)
        then: "la sequenza deve essere correttamente ordinata"
        CodaFirma.inCodaPerFirmare(lettera).list().eachWithIndex { coda, index ->
            assert coda.sequenza == (index + 1)
        }

        when: "elimino un firmatario in mezzo alla coda"
        gestioneDocumentiFirmaService.eliminaFirmatarioInCoda(lettera, utenteFirmatario3)
        then: "la sequenza di firma è corretta e ha coperto il buco lasciato dal firmatario eliminato"
        CodaFirma.inCodaPerFirmare(lettera).list().eachWithIndex { coda, index ->
            assert coda.sequenza == (index + 1)
        }

        when: "elimino tutti i firmatari dalla coda"
        gestioneDocumentiFirmaService.eliminaFirmatariInCoda(lettera)
        then: "la coda deve essere vuota"
        CodaFirma.inCodaPerFirmare(lettera).list().size() == 0
    }
}
