package it.finmatica.gestionedocumenti.testutils

import it.finmatica.dto.DtoGroovyExtension
import it.finmatica.gestionedocumenti.Holders
import it.finmatica.gestionedocumenti.commons.Ente
import it.finmatica.gestionedocumenti.commons.Utils
import it.finmatica.gestionedocumenti.documenti.Allegato
import it.finmatica.gestionedocumenti.documenti.Documento
import it.finmatica.gestionedocumenti.documenti.FileDocumento
import it.finmatica.gestionedocumenti.documenti.IGestoreFile
import it.finmatica.gestionedocumenti.documenti.TipoAllegato
import it.finmatica.gestionedocumenti.documenti.TipoCollegamento
import it.finmatica.gestionedocumenti.exception.GestioneDocumentiRuntimeException
import it.finmatica.gestionedocumenti.impostazioni.Impostazione
import it.finmatica.gestionedocumenti.impostazioni.Impostazioni
import it.finmatica.gestionedocumenti.impostazioni.ImpostazioniMap
import it.finmatica.gestionedocumenti.registri.TipoRegistro
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import it.finmatica.so4.struttura.So4AOO
import it.finmatica.so4.struttura.So4Amministrazione
import it.finmatica.so4.struttura.So4Ottica
import org.apache.commons.io.IOUtils
import org.hibernate.SessionFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.zkoss.util.resource.LabelLocator
import org.zkoss.util.resource.Labels

import java.security.MessageDigest

class TestUtils {

    // questi dati sono relativi al database "test-agspr-db02"
    public static String CODICE_AMMINISTRAZIONE = 'P_MO'
    public static String CODICE_OTTICA = 'IST'
    public static String CODICE_AOO = 'AOOPMO'
    public static String UTENTE_LOGIN = 'brena01'

    private static ImpostazioniMap mapImpostazioni = null

    /**
     * Questa schifezza serve per poter personalizzare le impostazioni negli integration tests siccome vanno su una vista su gdm e non posso quindi fare update.
     * Dopo aver invocato questo metodo, è necessario invocare il metodo cleanupImpostazioni che ripristina le impostazioni originali.
     * @param impostazioni
     */
    static void setUpImpostazioni(Map<String, String> impostazioni) {
        if (mapImpostazioni == null) {
            mapImpostazioni = Impostazioni.map
            Impostazioni.map = new MockImpostazioni(mapImpostazioni)
        }

        ((MockImpostazioni) Impostazioni.map).addImpostazioni(impostazioni)
    }

    /**
     * Ripristina le impostazioni originali come modificate da setUpImpostazioni
     */
    static void cleanupImpostazioni() {
        if (mapImpostazioni != null) {
            Impostazioni.map = mapImpostazioni
            mapImpostazioni = null
        }
    }

    static void setUpDTO(SessionFactory sessionFactory) {
        DtoGroovyExtension.init(sessionFactory, "DTO")
    }

    static void setUpDefaults() {
        Labels.register(new LabelLocator() {
            URL locate(Locale paramLocale) throws Exception {
                return new File("web-app/WEB-INF/zk-label.properties").toURI().toURL()
            }
        })
    }

    static Ente setUpEnte(String codiceAmministrazione = CODICE_AMMINISTRAZIONE, String codiceOttica = CODICE_OTTICA, String codiceAoo = CODICE_AOO) {
        // cerco l'aoo con il codice specificato
        So4AOO aoo = So4AOO.findByCodiceAndAlIsNull(codiceAoo)
        if (aoo == null) {
            throw new Exception("Non ho trovato una aoo con codice ${codiceAoo} nella vista so4_v_aoo")
        }

        So4Ottica ottica = So4Ottica.get(codiceOttica)
        if (ottica == null) {
            throw new Exception("Non ho trovato una ottica con codice ${codiceOttica} nella vista so4_v_ottiche")
        }

        So4Amministrazione amm = So4Amministrazione.get(codiceAmministrazione)
        if (amm == null) {
            throw new Exception("Non ho trovato una aoo con codice ${codiceAmministrazione} nella vista so4_v_mministrazioni")
        }

        // cerco un ente con amministrazione / aoo richiesti:
        Ente ente = Ente.perAmministrazioneOtticaAoo(codiceAmministrazione, codiceOttica, codiceAoo).get()
        if (ente == null) {
            // se non lo ho trovato, ne creo uno nuovo
            ente = new Ente(descrizione: "Ente default", amministrazione: amm, ottica: ottica, aoo: codiceAoo)
            ente.sequenza = Ente.count()
            ente.save()
        }

        return ente
    }

    static void login(String user = UTENTE_LOGIN, String amministrazione = CODICE_AMMINISTRAZIONE) {
        Utils.eseguiAutenticazione(user, amministrazione)
    }

    static void logout() {
        SecurityContextHolder.getContext().authentication = null
    }

    static void setImpostazioni(Map<String, String> impostazioni, Ente ente = null) {
        for (def impostazione : impostazioni) {
            Impostazione i = Impostazione.getImpostazione(impostazione.key, ente).get()

            if (i == null) {
                Impostazioni imp = Impostazioni.valueOf(impostazione.key)

                i = new Impostazione()
                i.codice = imp.toString()
                i.descrizione = imp.descrizione
                i.etichetta = imp.etichetta
                i.predefinito = imp.predefinito
                i.caratteristiche = imp.caratteristiche
                i.ente = ente
                i.valore = imp.predefinito
                i.save()
            }

            i.valore = impostazione.value
            i.save(flush: true)
        }

        Impostazioni.map.refresh()
    }

    static TipoAllegato setUpTipoAllegato(String codice = "ALLEGATO") {
        TipoAllegato tipoAllegato = TipoAllegato.findByCodice(codice)
        if (tipoAllegato != null) {
            return tipoAllegato;
        } else {
            throw new GestioneDocumentiRuntimeException("Bisogna usare TIPI_ALLEGATO già esistenti.");
        }
    }

    static Allegato setUpAllegato() {
        TipoAllegato tipoDocumento = setUpTipoAllegato()
        WkfTipoOggetto tipoOggetto = setUpTipoOggetto("ALLEGATO", "allegato", false)

        Allegato allegato = new Allegato(riservato: true, tipoOggetto: tipoOggetto, tipoAllegato: tipoDocumento)
        allegato.save()

        return allegato
    }

    static TipoRegistro setUpTipoRegistro(String codice) {
        TipoRegistro tipoRegistro = TipoRegistro.findByCodice(codice)
        if (tipoRegistro == null) {
            tipoRegistro = new TipoRegistro()
            tipoRegistro.codice = codice
            tipoRegistro.save()
        }
        return tipoRegistro
    }

    static TipoCollegamento setUpTipoCollegamento(String codice, String descrizione) {
        TipoCollegamento tipoCollegamento = TipoCollegamento.findByCodice(codice)
        if (tipoCollegamento == null) {
            tipoCollegamento = new TipoCollegamento(codice: codice)
        }

        tipoCollegamento.descrizione = descrizione
        tipoCollegamento.save()
        return tipoCollegamento
    }

    static WkfTipoOggetto setUpTipoOggetto(String codice, String nome, boolean iterabile = true) {
        WkfTipoOggetto tipoOggetto = WkfTipoOggetto.findByCodice(codice)
        if (tipoOggetto == null) {
            tipoOggetto = new WkfTipoOggetto(codice: codice)
        }

        tipoOggetto.iterabile = iterabile
        tipoOggetto.nome = nome
        tipoOggetto.save(deepValidate:false)
        return tipoOggetto
    }

    static boolean streamEquals(InputStream is1, InputStream is2) {
        String md51 = MessageDigest.getInstance("MD5").digest(IOUtils.toByteArray(is1)).encodeHex().toString()
        String md52 = MessageDigest.getInstance("MD5").digest(IOUtils.toByteArray(is2)).encodeHex().toString()
        return (md51 == md52)
    }

    static FileDocumento setUpFile(Documento documento, String codice = FileDocumento.CODICE_FILE_PRINCIPALE, String filepath = "test/resources/TEST_LETTERA.odt", String contentType = "application/odt", String filename = null) {
        File file = new File(filepath)
        if (filename == null) {
            filename = file.name
        }
        FileDocumento filePrincipale = new FileDocumento(nome: filename, contentType: contentType, codice: codice)
        documento.addToFileDocumenti(filePrincipale)
        documento.save()
        Holders.getApplicationContext().getBean(IGestoreFile).addFile(documento, filePrincipale, new File(filepath).newInputStream())
        return filePrincipale
    }
}

