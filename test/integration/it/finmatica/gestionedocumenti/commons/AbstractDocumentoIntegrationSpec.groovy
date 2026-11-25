package it.finmatica.gestionedocumenti.commons

import it.finmatica.gestionedocumenti.IntegrationSpec
import it.finmatica.gestionedocumenti.notifiche.Notifica
import it.finmatica.gestionedocumenti.notifiche.NotificaDTO
import it.finmatica.gestionedocumenti.testutils.TestUtils

class AbstractDocumentoIntegrationSpec extends IntegrationSpec {

    def setup() {
        TestUtils.setUpEnte()
        TestUtils.login()
    }

    def cleanup() {
        TestUtils.logout()
    }

    void "test creazione oggetto AbstractDomain e trasformazione in DTO"() {
        given:
        def notifica = new Notifica(tipoNotifica: "ESECUTIVITA", titolo: "Esecutività", oggetto: "Notifica", testo: "Notifica", modalitaInvio: "EMAIL", allegati: "TESTO", oggetti: "DETERMINA")

        when:
        notifica.save(failOnError: true, flush: true)

        then:
        notifica.tipoNotifica == "ESECUTIVITA"
        notifica.titolo == "Esecutività"
        notifica.oggetto == "Notifica"
        notifica.testo == "Notifica"
        notifica.allegati == "TESTO"
        notifica.oggetti == "DETERMINA"
        notifica.dateCreated != null
        notifica.lastUpdated != null
        notifica.utenteUpd.nominativo.toLowerCase() == TestUtils.UTENTE_LOGIN.toLowerCase()
        notifica.utenteIns.nominativo.toLowerCase() == TestUtils.UTENTE_LOGIN.toLowerCase()

        when:
        NotificaDTO notificaDto = notifica.toDTO()

        then:
        notificaDto.tipoNotifica == notifica.tipoNotifica
        notificaDto.titolo == notifica.titolo
        notificaDto.oggetto == notifica.oggetto
        notificaDto.testo == notifica.testo
        notificaDto.allegati == notifica.allegati
        notificaDto.oggetti == notifica.oggetti
        notificaDto.dateCreated == notifica.dateCreated
        notificaDto.lastUpdated == notifica.lastUpdated
        notificaDto.utenteUpd.id == notifica.utenteUpd.id
        notificaDto.utenteIns.id == notifica.utenteIns.id
    }
}
