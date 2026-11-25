package it.finmatica.test

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import it.finmatica.ad4.security.SpringSecurityService
import it.finmatica.firmadigitale.FirmaDigitaleFile
import it.finmatica.firmadigitale.FirmaDigitaleTransazione
import it.finmatica.firmadigitale.FirmaDigitaleTransazioneService
import it.finmatica.gestionedocumenti.integrazioni.firma.GestioneDocumentiFirmaService
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.RandomStringUtils
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletResponse

@CompileStatic
@Controller
@RequestMapping("/test")
class FirmaController {

    private final SpringSecurityService springSecurityService
    private final GestioneDocumentiFirmaService gestioneDocumentiFirmaService

    FirmaController(GestioneDocumentiFirmaService gestioneDocumentiFirmaService, SpringSecurityService springSecurityService) {
        this.gestioneDocumentiFirmaService = gestioneDocumentiFirmaService
        this.springSecurityService = springSecurityService
    }

    @CompileDynamic
    @Transactional(readOnly = true)
    @GetMapping("/download/originale/{idRiferimento}")
    void downloadFileOriginale(@PathVariable("idRiferimento") String idRiferimento, HttpServletResponse response) {
        response.setHeader("Pragma", "no-cache")
        response.setHeader("Cache-Control", "no-store, private, no-cache, must-revalidate, pre-check=0, post-check=0, max-age=0")
        response.setDateHeader("Last-Modified", 0)
        response.setDateHeader("Expires", 0)

        FirmaDigitaleFile file = FirmaDigitaleFile.findByIdRiferimentoFile(idRiferimento)

        response.setHeader("Content-Disposition", "attachment; filename=\"${file.nome}\"");
        response.setHeader("Content-Transfer-Encoding", "binary");
        IOUtils.copy(file.fileDaFirmare.binaryStream, response.getOutputStream())
    }

    @CompileDynamic
    @Transactional(readOnly = true)
    @GetMapping("/download/firmato/{idRiferimento}")
    void downloadFileFirmato(@PathVariable("idRiferimento") String idRiferimento, HttpServletResponse response) {
        response.setHeader("Pragma", "no-cache")
        response.setHeader("Cache-Control", "no-store, private, no-cache, must-revalidate, pre-check=0, post-check=0, max-age=0")
        response.setDateHeader("Last-Modified", 0)
        response.setDateHeader("Expires", 0)

        FirmaDigitaleFile file = FirmaDigitaleFile.findByIdRiferimentoFile(idRiferimento)

        response.setHeader("Content-Disposition", "attachment; filename=\"${file.nomeFirmato}\"");
        response.setHeader("Content-Transfer-Encoding", "binary");
        IOUtils.copy(file.fileFirmato.binaryStream, response.getOutputStream())
    }

    @GetMapping("/risultato/{idRiferimento}")
    String risultato(@PathVariable("idRiferimento") String idRiferimento, Model model) {
        model.addAttribute("idRiferimento", idRiferimento)
        return "risultato"
    }

    @RequestMapping("/")
    String firma() {
        return "firma"
    }

    @PostMapping("/")
    @Transactional
    void uploadFile(@RequestParam('file') MultipartFile file, @RequestParam(name="firmaHash", required = false, defaultValue = "false")boolean firmaHash, HttpServletResponse response) {
        String idRiferimento = RandomStringUtils.randomAlphabetic(10)
//        gestioneDocumentiFirmaService.preparaFirmatarioInCoda(, springSecurityService.currentUser)
//        firmaDigitaleService.aggiungiFileDaFirmare(idRiferimento, file.name, file.inputStream)
//
//        String urlPopupFirma = firmaDigitaleService.getConfigurazioneFirma().setFirmaHash(firmaHash).buildURL()
//        response.sendRedirect(urlPopupFirma)
    }
}
