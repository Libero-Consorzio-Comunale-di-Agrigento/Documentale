package it.finmatica.gestionedocumenti.storico

import groovy.json.JsonSlurper
import groovy.json.StreamingJsonBuilder
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.gestionedocumenti.commons.DateDBService
import it.finmatica.gestionedocumenti.documenti.Documento
import it.finmatica.gestionedocumenti.integrazioni.gdm.IntegrazioneGdmService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * @deprecated
 * questa classe non va più usata (e va eliminata) perché lo storico viene gestito da Envers e non più da JSON.
 * È ancora qui con il solo scopo di verificare che la trascodifica da JSON a Envers sia corretta.
 */
@CompileStatic
@Transactional
@Service
class DocumentoStoricoService {

    private final DocumentoStoricoRepository documentoStoricoRepository
    private final IntegrazioneGdmService integrazioneGdmService
    private final DateDBService dateDBService

    DocumentoStoricoService(IntegrazioneGdmService integrazioneGdmService, DocumentoStoricoRepository documentoStoricoRepository, DateDBService dateDBService) {
        this.integrazioneGdmService = integrazioneGdmService
        this.documentoStoricoRepository = documentoStoricoRepository
        this.dateDBService = dateDBService
    }

    /**
     * Come deve essere fatta la closure jsonSerializer.
     *
     * 1. Con il campo _key si identifica il record. (utile per determinare se è presente un nuovo elemento in una lista)
     * 2. Con il campo _value c'è il titolo che descrive il record.
     *    Se presente, questo campo viene sempre fatto vedere. Viene usato anche per la compare per capire se un record è diverso da un altro.
     * 3. Ogni altro campo preceduto da "_", non viene mostrato, ma viene usato per la compare.
     * 4. Ogni altro campo viene usato per la compare e viene mostrato.
     * 5. Con il campo _idFile si indica che il record storico è un puntatore ad un file.
     *
     * @param documento
     * @param jsonSerializer
     */
    void storicizza(Documento documento, Closure jsonSerializer, boolean logSoloModificati = true, Date dataAggiornamento = null, Ad4Utente utenteInserimentoStorico = null) {

        // serializzo i dati
        String datiStoricizzati = serializzaStorico(jsonSerializer)

        // li riparso per fare la diff.
        Map datiNuovi = parseJson(datiStoricizzati)
        Map datiVecchi = [:]

        // cerco lo storico precedente (se presente)
        DocumentoStorico storicoVecchio = null

        List<DocumentoStorico> documentiStorici = documentoStoricoRepository.getDocumentiStoriciOrderByDateCreatedDesc(documento)
        if (documentiStorici?.size() > 0) {
            storicoVecchio = documentiStorici?.getAt(0)
        }

        // se ho uno storico precedente, ne prendo i dati storicizzati per calcolare la differenza con i nuovi dati
        if (storicoVecchio != null) {
            datiVecchi = parseJson(storicoVecchio.datiStoricizzati)
        }

        // eseguo la diff con i dati dello storico nuovo con quello vecchio
        List<DatoStorico> diffList = diff(datiVecchi, datiNuovi, "")

        // se non ho differenze, non salvo lo storico
        if (diffList.size() == 0) {
            return
        }

        // se ho delle differenze, salvo lo storico: prima su GDM, poi da "noi":
        long revisione = integrazioneGdmService.getUltimaRevisioneDocumento(documento)
        if (storicoVecchio && !documento.salvaRevisioneDocumentaleSuccessiva) {
            storicoVecchio.revisione = revisione
            documentoStoricoRepository.save(storicoVecchio)
        }

        DocumentoStorico storicoNuovo = new DocumentoStorico(documento: documento)

        if (documento.salvaRevisioneDocumentaleSuccessiva) {
            storicoNuovo.revisione = revisione
        }

        storicoNuovo.datiStoricizzati = datiStoricizzati
        storicoNuovo.datiModificati = serializeDiff(diffList)

        // salvo il tutto e termino
        storicoNuovo = documentoStoricoRepository.saveAndFlush(storicoNuovo)

        Date now = dateDBService.currentDate
        storicoNuovo.dateCreated = dataAggiornamento ?: now
        storicoNuovo.lastUpdated = dataAggiornamento ?: now
        // #77604 - parametrizziamo l'utente di inserimento,
        // in modo che possiamo usare questo metodo per transcodificare storici mancanti in apertura dei documenti
        if(utenteInserimentoStorico){
            storicoNuovo.utenteIns = utenteInserimentoStorico
        }
        storicoNuovo = documentoStoricoRepository.saveAndFlush(storicoNuovo)

        documento.salvaRevisioneDocumentaleSuccessiva = false
    }

    List<DatoStorico> filtraDatiStorici(List<DatoStorico> datiStorici, List<String> filtri) {
        return datiStorici.findAll { DatoStorico datoStorico ->
            datoStorico.datiStorici = datoStorico.datiStorici.findAll { DatoStorico recordStorico ->
                for (String filtro : filtri) {
                    if (recordStorico.campo.matches(filtro)) {
                        return true
                    }
                }
                return false
            }

            return (datoStorico.datiStorici.size() > 0)
        }
    }

    List<DatoStorico> ricercaStorico(long idDocumento, Date dal, Date al) {
        List<DocumentoStorico> documentiStoricizzati = documentoStoricoRepository.ricerca(idDocumento, dal, al)

        List<DatoStorico> datiStorici = []

        // per ogni riga storicizzata, devo popolare una lista
        for (DocumentoStorico storico : documentiStoricizzati) {
            datiStorici << new DatoStorico(nominativoUtente: storico.utenteIns.nominativoSoggetto ?: storico.utenteIns.utente, dataModifica: storico.dateCreated, revisione: storico.revisione, datiStorici: parseDatiStorici(storico.datiModificati))
        }

        return datiStorici
    }

    DatoStorico getUltimaRevisione(Documento documento) {

        DocumentoStorico documentoStorico = null

        List<DocumentoStorico> documentiStorici = documentoStoricoRepository.getDocumentiStoriciOrderByDateCreatedDesc(documento)
        if (documentiStorici?.size() > 0) {
            documentoStorico = documentiStorici?.getAt(0)
        }

        if (documentoStorico == null) {
            return null
        }

        return new DatoStorico(nominativoUtente: documentoStorico.utenteIns.nominativoSoggetto,
                dataModifica: documentoStorico.dateCreated,
                revisione: documentoStorico.revisione,
                datiStorici: parseDatiStorici(documentoStorico.datiModificati))
    }

    @CompileStatic
    boolean isTrascodificato(Long idDocumento) {
        return documentoStoricoRepository.countDocumentiStoriciTrascodificati(idDocumento, DocumentoStorico.VERSIONE_JSON2) > 0
    }

    @CompileDynamic
    private List<DatoStorico> parseDatiStorici(String datiModificati) {
        List<Map> datiStorici = (List<Map>) new JsonSlurper().parseText(datiModificati)
        return datiStorici.collect { new DatoStorico(it) }
    }

    private String serializzaStorico(Closure jsonSerializer) {
        StringWriter stringWriter = new StringWriter()
        StreamingJsonBuilder jsonBuilder = new StreamingJsonBuilder(stringWriter)
        jsonBuilder.call(jsonSerializer)
        return stringWriter.toString()
    }

    private Map parseJson(String json) {
        return (Map) new JsonSlurper().parseText(json)
    }

    @CompileDynamic
    private String serializeDiff(List<DatoStorico> modifiche) {
        StringWriter stringWriter = new StringWriter()
        StreamingJsonBuilder jsonBuilder = new StreamingJsonBuilder(stringWriter)
        jsonBuilder.call(modifiche, {
            campo(it.campo)
            valoreNuovo(it.valoreNuovo)
            valoreVecchio(it.valoreVecchio)
            tipoStorico(it.tipoStorico)
            dati(it.dati)
        })
        return stringWriter.toString()
    }

    @CompileDynamic
    private List<DatoStorico> diff(Map datiVecchi, Map datiNuovi, String rootField) {
        if (datiVecchi == null) {
            datiVecchi = [:]
        }

        boolean valueKeyAdded = false

        // per ogni chiave presente in entrambi i set verifico i cambiamenti:
        Collection<String> keys = (datiNuovi.keySet().intersect(datiVecchi.keySet()))
        Map diffModifiche = diffAux(datiVecchi, datiNuovi, keys, rootField, valueKeyAdded, DatoStorico.TipoStorico.MODIFICATO)

        // per ogni chiave presente solo nei dati nuovi:
        keys = (datiNuovi.keySet() - datiVecchi.keySet())
        Map diffAggiunte = diffAux(datiVecchi, datiNuovi, keys, rootField, diffModifiche.valueKeyAdded, DatoStorico.TipoStorico.AGGIUNTO)

        // per ogni chiave nei dati vecchi:
        // per ogni elemento rimosso in "dati":
        keys = datiVecchi.keySet() - datiNuovi.keySet()
        Map diffCancellazioni = diffAux(datiVecchi, datiNuovi, keys, rootField, diffAggiunte.valueKeyAdded, DatoStorico.TipoStorico.CANCELLATO)

        return diffModifiche.diff + diffAggiunte.diff + diffCancellazioni.diff + diffModifiche.diffColl + diffAggiunte.diffColl + diffCancellazioni.diffColl
    }

    @CompileDynamic
    private Map diffAux(Map datiVecchi, Map datiNuovi, Collection<Object> keys, String rootField, boolean valueKeyAdded, DatoStorico.TipoStorico tipoStorico) {
        List<DatoStorico> differenze = []
        List<DatoStorico> differenzeCollection = []
        for (String key : keys) {
            // salto il campo _key
            if (key == "_key") {
                continue
            }

            Object valoreNuovo = datiNuovi[key]
            Object valoreVecchio = datiVecchi[key]

            if (valoreNuovo instanceof Map && valoreVecchio instanceof Map) {
                differenze.addAll(diff(valoreVecchio, valoreNuovo, rootField + key + "."))
            } else if (valoreVecchio instanceof Map && valoreNuovo == null) {
                differenze.addAll(diff(valoreVecchio, [:], rootField + key + "."))
            } else if (valoreNuovo instanceof Collection || (valoreNuovo == null && valoreVecchio instanceof Collection)) {

                if (valoreNuovo == null) {
                    valoreNuovo = []
                }

                if (valoreVecchio == null) {
                    valoreVecchio = []
                }

                // utilizzo la keyword: "_key" per capire se un elemento è presente o no nelle collection:
                List<Object> chiaviNuove = valoreNuovo*._key
                List<Object> chiaviVecchie = valoreVecchio*._key

                // per ogni chiave in comune, controllo se ci sono modifiche:
                List<Object> chiavi = chiaviNuove.intersect(chiaviVecchie)
                for (Object chiaveComune : chiavi) {
                    Map nuovo = valoreNuovo.find { it._key == chiaveComune }
                    Map vecchio = valoreVecchio.find { it._key == chiaveComune }
                    differenzeCollection.addAll(diff(vecchio, nuovo, rootField + key + "."))
                }

                // per ogni chiave presente solo nei dati nuovi:
                chiavi = (chiaviNuove - chiaviVecchie)
                for (def chiaveAggiunta : chiavi) {
                    Map nuovo = valoreNuovo.find { it._key == chiaveAggiunta }
                    differenzeCollection.addAll(diff([:], nuovo, rootField + key + "."))
                }

                // per ogni chiave nei dati vecchi:
                // per ogni elemento rimosso in "dati":
                chiavi = (chiaviVecchie - chiaviNuove)
                for (def chiaveRimossa : chiavi) {
                    Map vecchio = valoreVecchio.find { it._key == chiaveRimossa }
                    differenzeCollection.addAll(diff(vecchio, [:], rootField + key + "."))
                }
            } else {
                if (valoreNuovo == valoreVecchio) {
                    continue
                }

                // se la mappa corrente ha un campo _value, uso quello come descrizione del campo
                if (!valueKeyAdded && (datiNuovi?.containsKey("_value") || datiVecchi?.containsKey("_value"))) {
                    differenze << new DatoStorico(tipoStorico: tipoStorico, campo: rootField + "_value", valoreVecchio: datiVecchi?._value, valoreNuovo: datiNuovi?._value, dati: getDati(datiVecchi, datiNuovi))
                    valueKeyAdded = true
                }

                // poi aggiungo il dato che ho confrontato (a meno che esso stesso non sia il campo "_value", perché nel caso l'ho appena aggiunto nell'if qui sopra
                if (key.startsWith("_")) {
                    continue
                }

                differenze << new DatoStorico(tipoStorico: tipoStorico, campo: rootField + key, valoreVecchio: datiVecchi[key], valoreNuovo: datiNuovi[key])
            }
        }

        return [diff: differenze, diffColl: differenzeCollection, valueKeyAdded: valueKeyAdded]
    }

    @CompileDynamic
    Map<String, Object> getDati(Map datiVecchi, Map datiNuovi) {
        Map storico = datiNuovi
        if (storico._value == null) {
            storico = datiVecchi
        }
        return storico.subMap(storico.keySet().findAll { it.startsWith("_") })
    }
}
