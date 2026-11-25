package it.finmatica.gestionedocumenti.firma

import it.finmatica.ad4.security.SpringSecurityService
import it.finmatica.gestionedocumenti.documenti.Documento
import it.finmatica.gestionedocumenti.documenti.beans.GestoreTransazioneFirma
import it.finmatica.gestionedocumenti.integrazioni.firma.GestioneDocumentiFirmaService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

import javax.servlet.http.HttpServletResponse

@Controller
@RequestMapping("/firma")
class FirmaController {

    @Autowired private GestioneDocumentiFirmaService gestioneDocumentiFirmaService
    @Autowired private GestoreTransazioneFirma gestoreTransazioneFirma
    @Autowired private SpringSecurityService springSecurityService

    @GetMapping("/")
    def index() {
        // se non ho l'utente corrente, lo riautentico usando i parametri
        if (springSecurityService.currentUser == null) {
            gestoreTransazioneFirma.autenticaConToken(params.long("idTransazioneFirma"), params.utente, params.ente, params.token)
        }

        render(view: 'firma', model: [finito: false])
    }

    @GetMapping("/firma")
    def firma(@RequestParam("LISTA_ID") String lista, HttpServletResponse response) {

        if (lista == null) {
            return
        }

        Collection<Documento> documentiDaFirmare = []
        String[] listaRiferimenti = lista.split("#")


        for (String riferimento in listaRiferimenti) {
            Documento doc = Documento.findByIdDocumentoEsterno(Long.parseLong(riferimento))
            documentiDaFirmare.add(doc)
        }

        String urlFirma = gestioneDocumentiFirmaService.multiFirma(documentiDaFirmare)

        if (urlFirma != null && urlFirma.indexOf("/UploadDownload", 0) != -1)
            urlFirma = urlFirma.replace("/UploadDownload", "/../UploadDownload")

        response.sendRedirect(urlFirma)
    }
}
