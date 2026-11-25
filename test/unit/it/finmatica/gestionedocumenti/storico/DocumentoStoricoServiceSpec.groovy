package it.finmatica.gestionedocumenti.storico

import spock.lang.Specification

/**
 */
//@TestFor(DocumentoStoricoService)
//@Mock([TestLettera, TipoLettera, DocumentoStorico, Ad4Utente])
class DocumentoStoricoServiceSpec extends Specification {
/*
    void "test filtraDatiStorici" () {
        setup:
        def datiStorici
        def filtro

        when:
        datiStorici = [new DatoStorico(nominativoUtente: "utente1", dataModifica: new Date(), datiStorici: [new DatoStorico(campo: "oggetto", valoreNuovo: "valore nuovo", tipoStorico: DatoStorico.TipoStorico.MODIFICATO)])]
        filtro      = ["oggetto"]
        List<DatoStorico> datiStoriciFiltrati = service.filtraDatiStorici(datiStorici, filtro)
        then:
        datiStoriciFiltrati.size() == 1

        when:
        datiStorici = [new DatoStorico(nominativoUtente: "utente1", dataModifica: new Date(), datiStorici: [new DatoStorico(campo: "oggetto", valoreNuovo: "valore nuovo", tipoStorico: DatoStorico.TipoStorico.MODIFICATO), new DatoStorico(campo: "fascicolo", valoreNuovo: "fasc 1", tipoStorico: DatoStorico.TipoStorico.MODIFICATO)])]
        filtro      = ["oggetto"]
        datiStoriciFiltrati = service.filtraDatiStorici(datiStorici, filtro)
        then:
        datiStoriciFiltrati.size() == 1
        datiStoriciFiltrati[0].datiStorici.size() == 1

        when:
        datiStorici = [new DatoStorico(nominativoUtente: "utente1", dataModifica: new Date(), datiStorici: [new DatoStorico(campo: "oggetto", valoreNuovo: "valore nuovo", tipoStorico: DatoStorico.TipoStorico.MODIFICATO), new DatoStorico(campo: "fascicolo", valoreNuovo: "fasc 1", tipoStorico: DatoStorico.TipoStorico.MODIFICATO)])
                     , new DatoStorico(nominativoUtente: "utente1", dataModifica: new Date(), datiStorici: [new DatoStorico(campo: "oggetto", valoreNuovo: "valore nuovo", tipoStorico: DatoStorico.TipoStorico.MODIFICATO)])]
        filtro      = ["oggetto"]
        datiStoriciFiltrati = service.filtraDatiStorici(datiStorici, filtro)
        then:
        datiStoriciFiltrati.size() == 2
        datiStoriciFiltrati[0].datiStorici.size() == 1
        datiStoriciFiltrati[1].datiStorici.size() == 1

        when:
        datiStorici = [new DatoStorico(nominativoUtente: "utente1", dataModifica: new Date(), datiStorici: [new DatoStorico(campo: "oggetto", valoreNuovo: "valore nuovo", tipoStorico: DatoStorico.TipoStorico.MODIFICATO)
                                                                                                            , new DatoStorico(campo: "fascicolo", valoreNuovo: "fasc 1", tipoStorico: DatoStorico.TipoStorico.MODIFICATO)])
                     , new DatoStorico(nominativoUtente: "utente1", dataModifica: new Date(), datiStorici: [new DatoStorico(campo: "oggetto", valoreNuovo: "valore nuovo", tipoStorico: DatoStorico.TipoStorico.MODIFICATO)])]
        filtro      = ["oggetto"]
        datiStoriciFiltrati = service.filtraDatiStorici(datiStorici, filtro)
        then:
        datiStoriciFiltrati.size() == 2
        datiStoriciFiltrati[0].datiStorici.size() == 1
        datiStoriciFiltrati[1].datiStorici.size() == 1

        when: "il filtro è una regular-expression"
        datiStorici = [new DatoStorico(nominativoUtente: "utente1", dataModifica: new Date(), datiStorici: [new DatoStorico(campo: "oggetto", valoreNuovo: "valore nuovo", tipoStorico: DatoStorico.TipoStorico.MODIFICATO)
                                                                                                            , new DatoStorico(campo: "fascicolo", valoreNuovo: "fasc 1", tipoStorico: DatoStorico.TipoStorico.MODIFICATO)])
                     , new DatoStorico(nominativoUtente: "utente1", dataModifica: new Date(), datiStorici: [new DatoStorico(campo: "oggetto", valoreNuovo: "valore nuovo", tipoStorico: DatoStorico.TipoStorico.MODIFICATO)
                                                                                                            , new DatoStorico(campo: "classifica.codice", valoreNuovo: "class 1", tipoStorico: DatoStorico.TipoStorico.MODIFICATO)])]
        filtro      = ["classifica\\..*"]
        datiStoriciFiltrati = service.filtraDatiStorici(datiStorici, filtro)
        then:
        datiStoriciFiltrati.size() == 1
        datiStoriciFiltrati[0].datiStorici.size() == 1

        when: "filtro su più campi"
        datiStorici = [new DatoStorico(nominativoUtente: "utente1", dataModifica: new Date(), datiStorici: [new DatoStorico(campo: "oggetto", valoreNuovo: "valore nuovo", tipoStorico: DatoStorico.TipoStorico.MODIFICATO)
                                                                                                            , new DatoStorico(campo: "fascicolo", valoreNuovo: "fasc 1", tipoStorico: DatoStorico.TipoStorico.MODIFICATO)])
                     , new DatoStorico(nominativoUtente: "utente1", dataModifica: new Date(), datiStorici: [new DatoStorico(campo: "oggetto", valoreNuovo: "valore nuovo", tipoStorico: DatoStorico.TipoStorico.MODIFICATO)
                                                                                                            , new DatoStorico(campo: "classifica.codice", valoreNuovo: "class 1", tipoStorico: DatoStorico.TipoStorico.MODIFICATO)
                                                                                                            , new DatoStorico(campo: "classifica.dal", valoreNuovo: "class 1", tipoStorico: DatoStorico.TipoStorico.MODIFICATO)
                                                                                                            , new DatoStorico(campo: "classifica.descrizione", valoreNuovo: "class 1", tipoStorico: DatoStorico.TipoStorico.MODIFICATO)
                                                                                                            , new DatoStorico(campo: "fascicolo.codice", valoreNuovo: "class 1", tipoStorico: DatoStorico.TipoStorico.MODIFICATO)
                                                                                                            , new DatoStorico(campo: "fascicolo.numero", valoreNuovo: "class 1", tipoStorico: DatoStorico.TipoStorico.MODIFICATO)
                                                                                                            , new DatoStorico(campo: "fascicolo.descrizione", valoreNuovo: "class 1", tipoStorico: DatoStorico.TipoStorico.MODIFICATO)])]
        filtro      = ["classifica\\..*", "fascicolo\\..*"]
        datiStoriciFiltrati = service.filtraDatiStorici(datiStorici, filtro)
        then:
        datiStoriciFiltrati.size() == 1
        datiStoriciFiltrati[0].datiStorici.size() == 6
    }

    void "test serializzazione" () {
        when:
        Documento documento = new Documento(riservato:true)
        documento.id = 100
        documento.dateCreated = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "1984-06-22T10:12:13.444Z")
        String json = service.serializzaStorico {
            id (documento.id)
            riservato (documento.riservato)
            valido (documento.valido)
            dateCreated (documento.dateCreated)
        }
        then:
        json == '{"id":100,"riservato":true,"valido":true,"dateCreated":"1984-06-22T08:12:13+0000"}'

        when:
        TestLettera lettera = new TestLettera(riservato:true)
        lettera.id = 100
        lettera.dateCreated  = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "1984-06-22T10:12:13.444Z")
        lettera.tipoRegistro = new TipoRegistro(codice:"PROT")

        String jsonLettera = service.serializzaStorico {
            id (lettera.id)
            riservato (lettera.riservato)
            valido (lettera.valido)
            dateCreated (lettera.dateCreated)
            tipoRegistro (codice: lettera.tipoRegistro.codice)
        }
        then:
        jsonLettera == '{"id":100,"riservato":true,"valido":true,"dateCreated":"1984-06-22T08:12:13+0000","tipoRegistro":{"codice":"PROT"}}'
    }

    void "test diff" () {

        when: "storico con valori normali"
        def mapNuovo   = [id:1, testo:"ciao"]
        def mapVecchio = [id:1, testo:"ciaone"]
        List<DatoStorico> diff = service.diff(mapVecchio, mapNuovo, "")
        then:
        diff.size()             == 1
        diff[0].valoreNuovo     == "ciao"
        diff[0].tipoStorico     == DatoStorico.TipoStorico.MODIFICATO
        diff[0].valoreVecchio   == "ciaone"
        diff[0].campo           == "testo"

        when: "storico con valori normali di cui uno nuovo"
        mapVecchio = [id:1, testo:"ciaone"]
        mapNuovo   = [id:1, testo:"ciao", riservato: true]
        diff = service.diff(mapVecchio, mapNuovo, "")
        then:
        diff.size()             == 2
        diff[0].campo           == "testo"
        diff[0].valore          == "ciao"
        diff[0].valoreVecchio   == "ciaone"
        diff[0].tipoStorico     == DatoStorico.TipoStorico.MODIFICATO
        diff[1].campo           == "riservato"
        diff[1].valoreNuovo     == true
        diff[1].valoreVecchio   == null
        diff[1].valore          == true
        diff[1].tipoStorico     == DatoStorico.TipoStorico.AGGIUNTO

        when: "storico con valori normali di cui uno eliminato"
        mapVecchio  = [id:1, testo:"ciaone", riservato: true]
        mapNuovo    = [id:1, testo:"ciao"]
        diff = service.diff(mapVecchio, mapNuovo, "")
        then:
        diff.size()             == 2
        diff[0].campo           == "testo"
        diff[0].valore          == "ciao"
        diff[0].valoreVecchio   == "ciaone"
        diff[0].tipoStorico     == DatoStorico.TipoStorico.MODIFICATO
        diff[1].campo           == "riservato"
        diff[1].valoreVecchio   == true
        diff[1].valoreNuovo     == null
        diff[1].valore          == true
        diff[1].tipoStorico     == DatoStorico.TipoStorico.CANCELLATO

        when: "storico con valori valori complessi con descrizione e valori nascosti"
        mapVecchio = [id:1, testo:"ciaone", riservato: true, classificazione:[_value: "01-01 - 01/02/2015 / Classificazione 1", _codice:"01-01", _dal:"01/02/2015", _descrizione:"classificazione 1"], fascicolo:[_value:"1 / 2015 - Fascicolo 1", _numero:"1", _anno:2015, _oggetto:"fascicolo 1"]]
        mapNuovo   = [id:1, testo:"ciao",                    classificazione:[_value: "01-02 - 01/02/2015 / Classificazione 2", _codice:"01-02", _dal:"01/02/2015", _descrizione:"classificazione 2"], fascicolo:[_value:"1 / 2016 - Fascicolo 1", _numero:"1", _anno:2016, _oggetto:"fascicolo 1"]]
        diff = service.diff(mapVecchio, mapNuovo, "")
        then:
        diff.size()             == 4
        diff[0].campo           == "testo"
        diff[0].valore          == "ciao"
        diff[0].valoreVecchio   == "ciaone"
        diff[0].tipoStorico     == DatoStorico.TipoStorico.MODIFICATO

        diff[1].campo           == "classificazione._value"
        diff[1].valoreVecchio   == "01-01 - 01/02/2015 / Classificazione 1"
        diff[1].valoreNuovo     == "01-02 - 01/02/2015 / Classificazione 2"
        diff[1].valore          == "01-02 - 01/02/2015 / Classificazione 2"
        diff[1].tipoStorico     == DatoStorico.TipoStorico.MODIFICATO

        diff[2].campo           == "fascicolo._value"
        diff[2].valoreVecchio   == "1 / 2015 - Fascicolo 1"
        diff[2].valoreNuovo     == "1 / 2016 - Fascicolo 1"
        diff[2].valore          == "1 / 2016 - Fascicolo 1"
        diff[2].tipoStorico     == DatoStorico.TipoStorico.MODIFICATO

        diff[3].campo           == "riservato"
        diff[3].valoreVecchio   == true
        diff[3].valoreNuovo     == null
        diff[3].valore          == true
        diff[3].tipoStorico     == DatoStorico.TipoStorico.CANCELLATO

        when: "storico con valori annidati modificati"
        mapVecchio = [id:1
                      , testo:"ciaone"
                      , riservato: true
                      , classificazione:[_value: "01-01 - 01/02/2015 / Classificazione 1"
                                         , _codice:"01-01"
                                         , _dal:"01/02/2015"
                                         , _descrizione:"classificazione 1"]
                      , fascicolo:[_value:"1 / 2015 - Fascicolo 1"
                                   , _numero:"1"
                                   , _anno:2015
                                   , _oggetto:"fascicolo 1"]
                      , allegati:[  [_key:1, _value:"Allegato 1 modificato", riservato:false]
                                  , [_key:4, _value:"Allegato 41",           riservato:true]]]

        mapNuovo   = [id:1
                      , testo:"ciao"
                      , classificazione:[_value: "01-02 - 01/02/2015 / Classificazione 2"
                                         , _codice:"01-02"
                                         , _dal:"01/02/2015"
                                         , _descrizione:"classificazione 2"]
                      , fascicolo:[_value:"1 / 2016 - Fascicolo 1"
                                   , _numero:"1"
                                   , _anno:2016
                                   , _oggetto:"fascicolo 1"]
                      , allegati:  [[_key:1, _value:"Allegato 1",  riservato:true ]
                                  , [_key:4, _value:"Allegato 41", riservato:true]]]
        diff = service.diff(mapVecchio, mapNuovo, "")
        then:
        diff.size()             == 6
        diff[0].campo           == "testo"
        diff[0].valore          == "ciao"
        diff[0].valoreVecchio   == "ciaone"
        diff[0].tipoStorico     == DatoStorico.TipoStorico.MODIFICATO

        diff[1].campo           == "classificazione._value"
        diff[1].valoreVecchio   == "01-01 - 01/02/2015 / Classificazione 1"
        diff[1].valoreNuovo     == "01-02 - 01/02/2015 / Classificazione 2"
        diff[1].valore          == "01-02 - 01/02/2015 / Classificazione 2"
        diff[1].tipoStorico     == DatoStorico.TipoStorico.MODIFICATO

        diff[2].campo           == "fascicolo._value"
        diff[2].valoreVecchio   == "1 / 2015 - Fascicolo 1"
        diff[2].valoreNuovo     == "1 / 2016 - Fascicolo 1"
        diff[2].valore          == "1 / 2016 - Fascicolo 1"
        diff[2].tipoStorico     == DatoStorico.TipoStorico.MODIFICATO

        diff[3].campo           == "riservato"
        diff[3].valoreVecchio   == true
        diff[3].valoreNuovo     == null
        diff[3].valore          == true
        diff[3].tipoStorico     == DatoStorico.TipoStorico.CANCELLATO

        diff[4].campo           == "allegati._value"
        diff[4].valoreVecchio   == "Allegato 1 modificato"
        diff[4].valoreNuovo     == "Allegato 1"
        diff[4].valore          == "Allegato 1"
        diff[4].tipoStorico     == DatoStorico.TipoStorico.MODIFICATO

        diff[5].campo           == "allegati.riservato"
        diff[5].valoreVecchio   == false
        diff[5].valoreNuovo     == true
        diff[5].valore          == true
        diff[5].tipoStorico     == DatoStorico.TipoStorico.MODIFICATO

        diff[5].campo           == "allegati.riservato"
        diff[5].valoreVecchio   == false
        diff[5].valoreNuovo     == true
        diff[5].valore          == true
        diff[5].tipoStorico     == DatoStorico.TipoStorico.MODIFICATO

        when: "storico con valori annidati modificati, aggiunti ed eliminati"
        mapVecchio = [id:1
                      , testo:"ciaone"
                      , riservato: true
                      , classificazione:[_value: "01-01 - 01/02/2015 / Classificazione 1"
                                         , _codice:"01-01"
                                         , _dal:"01/02/2015"
                                         , _descrizione:"classificazione 1"]
                      , fascicolo:[_value:"1 / 2015 - Fascicolo 1"
                                   , _numero:"1"
                                   , _anno:2015
                                   , _oggetto:"fascicolo 1"]
                      , allegati:[  [_key:1, _value:"Allegato 1 modificato", riservato:false]
                                  , [_key:3, _value:"Allegato 3",            riservato:true]
                                  , [_key:4, _value:"Allegato 41",           riservato:true]]]

        mapNuovo   = [id:1
                      , testo:"ciao"
                      , classificazione:[_value: "01-02 - 01/02/2015 / Classificazione 2"
                                         , _codice:"01-02"
                                         , _dal:"01/02/2015"
                                         , _descrizione:"classificazione 2"]
                      , fascicolo:[_value:"1 / 2016 - Fascicolo 1"
                                   , _numero:"1"
                                   , _anno:2016
                                   , _oggetto:"fascicolo 1"]
                      , allegati:  [[_key:1, _value:"Allegato 1",  riservato:true ]
                                  , [_key:2, _value:"Allegato 2",  riservato:true]
                                  , [_key:4, _value:"Allegato 41", riservato:true]]]
        diff = service.diff(mapVecchio, mapNuovo, "")
        then:
        diff.size()             == 10
        diff[0].campo           == "testo"
        diff[0].valore          == "ciao"
        diff[0].valoreVecchio   == "ciaone"
        diff[0].tipoStorico     == DatoStorico.TipoStorico.MODIFICATO

        diff[1].campo           == "classificazione._value"
        diff[1].valoreVecchio   == "01-01 - 01/02/2015 / Classificazione 1"
        diff[1].valoreNuovo     == "01-02 - 01/02/2015 / Classificazione 2"
        diff[1].valore          == "01-02 - 01/02/2015 / Classificazione 2"
        diff[1].tipoStorico     == DatoStorico.TipoStorico.MODIFICATO

        diff[2].campo           == "fascicolo._value"
        diff[2].valoreVecchio   == "1 / 2015 - Fascicolo 1"
        diff[2].valoreNuovo     == "1 / 2016 - Fascicolo 1"
        diff[2].valore          == "1 / 2016 - Fascicolo 1"
        diff[2].tipoStorico     == DatoStorico.TipoStorico.MODIFICATO

        diff[3].campo           == "riservato"
        diff[3].valoreVecchio   == true
        diff[3].valoreNuovo     == null
        diff[3].valore          == true
        diff[3].tipoStorico     == DatoStorico.TipoStorico.CANCELLATO

        diff[4].campo           == "allegati._value"
        diff[4].valoreVecchio   == "Allegato 1 modificato"
        diff[4].valoreNuovo     == "Allegato 1"
        diff[4].valore          == "Allegato 1"
        diff[4].tipoStorico     == DatoStorico.TipoStorico.MODIFICATO

        diff[5].campo           == "allegati.riservato"
        diff[5].valoreVecchio   == false
        diff[5].valoreNuovo     == true
        diff[5].valore          == true
        diff[5].tipoStorico     == DatoStorico.TipoStorico.MODIFICATO

        diff[6].campo           == "allegati._value"
        diff[6].valoreVecchio   == null
        diff[6].valoreNuovo     == "Allegato 2"
        diff[6].valore          == "Allegato 2"
        diff[6].tipoStorico     == DatoStorico.TipoStorico.AGGIUNTO

        diff[7].campo           == "allegati.riservato"
        diff[7].valoreVecchio   == null
        diff[7].valoreNuovo     == true
        diff[7].valore          == true
        diff[7].tipoStorico     == DatoStorico.TipoStorico.AGGIUNTO

        diff[8].campo           == "allegati._value"
        diff[8].valoreVecchio   == "Allegato 3"
        diff[8].valoreNuovo     == null
        diff[8].valore          == "Allegato 3"
        diff[8].tipoStorico     == DatoStorico.TipoStorico.CANCELLATO

        diff[9].campo           == "allegati.riservato"
        diff[9].valoreVecchio   == true
        diff[9].valoreNuovo     == null
        diff[9].valore          == true
        diff[9].tipoStorico     == DatoStorico.TipoStorico.CANCELLATO
    }

    void "proprietà complessa eliminata" () {
        when:
        def mapVecchio = [classificazione: [_value: "11.1 - Classificazione", _codice:"test"]]
        def mapNuovo   = [:]

        def diff = service.diff (mapVecchio, mapNuovo, "")
        then:
        diff.size()             == 1
        diff[0].campo           == "classificazione._value"
        diff[0].valoreNuovo     == null
        diff[0].valoreVecchio   == "11.1 - Classificazione"
        diff[0].valore          == "11.1 - Classificazione"
        diff[0].tipoStorico     == DatoStorico.TipoStorico.CANCELLATO
    }

    void "proprietà complessa aggiunta" () {
        when:
        def mapVecchio  = [:]
        def mapNuovo    = [classificazione: [_value: "11.1 - Classificazione", _codice:"test"]]

        def diff = service.diff (mapVecchio, mapNuovo, "")
        then:
        diff.size()             == 1
        diff[0].campo           == "classificazione"
        diff[0].valoreVecchio   == null
        diff[0].valoreNuovo._value     == "11.1 - Classificazione"
        diff[0].valore._value         == "11.1 - Classificazione"
        diff[0].tipoStorico     == DatoStorico.TipoStorico.AGGIUNTO
    }

    void "test storico con file allegati" () {
        when: "storico con valori annidati modificati, aggiunti ed eliminati"
        def mapVecchio = [testo:[_idFile: 1, _value: "file1.png"]]
        def mapNuovo   = [:]

        def diff = service.diff(mapVecchio, mapNuovo, "")
        then:
        diff.size()             == 1
        diff[0].campo           == "testo._value"
        diff[0].valore          == "file1.png"
        diff[0].valoreVecchio   == "file1.png"
        diff[0].valoreNuovo     == null
        diff[0].dati._idFile    == 1
        diff[0].tipoStorico     == DatoStorico.TipoStorico.CANCELLATO
    }
    */
}
