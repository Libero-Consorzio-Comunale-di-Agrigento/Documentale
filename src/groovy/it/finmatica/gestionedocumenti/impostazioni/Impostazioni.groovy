package it.finmatica.gestionedocumenti.impostazioni

import org.zkoss.util.resource.Labels

enum Impostazioni {

    // aggiungere qui i codici delle impostazioni
    AGGIORNAMENTO_IN_CORSO("Impedisce l'accesso all'applicativo a tutti gli utenti tranne all'utente di amministrazione. Può contenere i valori Y/N oppure una data nel formato dd/MM/yyyy hh:mm.", "AGGIORNAMENTO IN CORSO", "N", null, false),
    AGGIORNAMENTO_TERMINATO("Indica se eseguire l'aggiorna azioni e trascodicifica storica al primo avvio", "AGGIORNAMENTO TERMINATO", "N", null, false),
    ALLEGATO_CONVERTI_PDF("Definisce se è abilitata la conversione degli allegati in pdf", "ALLEGATO CONVERTI PDF", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
    ALLEGATO_CONVERTI_PDF_FORMATO("Definisce quali formati di allegati vengono convertiti in pdf (odt, xls, doc), la sintassi è formato1#formato2#formato3", "ALLEGATO CONVERTI PDF FORMATO", "odt#doc#xls", null, true),
    ENTI_SO4("Elenco enti (separati da #) gestiti dall'applicativo per installazione", null, null, null, false),
    OTTICA_SO4("Ottica di SO4 utilizzata per l'amministrazione", "OTTICA SO4", "IST", null, false),
    DB_CHARSET("Charset del database", "DB CHARSET", "windows-1252", '<rowset><row label="Caratteri latini senza euro (iso-8859-1)" value="iso-8859-1" /><row label="Caratteri latini con euro (iso-8859-15)" codiceModello="iso-8859-15" /><row label="Tutti i caratteri (utf-8)" codiceModello="utf-8" /><row label="Caratteri Windows-Compatibili" codiceModello="windows-1252" /></rowset>', true),
    RUOLO_SO4_CONSERVAZIONE("Ruolo di accesso alla sezione 'Conservazione'", "RUOLO SO4 CONSERVAZIONE", "AGDCONS", null, true),
    RUOLO_SO4_NOTIFICHE("Ruolo per la ricezione delle notifiche", "RUOLO SO4 NOTIFICHE", "AGPSUP", null, true),
    ALLEGATO_FORMATI_POSSIBILI("Definisce tutti i formati possibili che possono essere allegati, la sintassi è formato1#formato2#formato3", "ALLEGATO FORMATI POSSIBILI", "", null, true),
    ALLEGATO_DIMENSIONE_MASSIMA("Definisce la dimensione massima dell allegato espressa in MB", "ALLEGATO DIMENSIONE MASSIMA", "25", null, true),
    EDITOR_DEFAULT("Indica l'editor di default per i testi", "Editor default", "SOFFICE", '<rowset><row label="Open Office" value="SOFFICE" /><row label="MS Word" codiceModello="WORD" /><row label="Open Office (senza controllo chiusura)" codiceModello="SOFFICE.NOCHECK" /><row label="MS Word (senza controllo chiusura)" codiceModello="WORD.NOCHECK" /></rowset>', true),
    EDITOR_DEFAULT_PATH("Indica il percorso sul client dove trovare l'eseguibile dell'editor di default. (utile per ambienti citrix)", "Path Editor default", "\"C:\\\\Program Files (x86)\\\\Microsoft Application Virtualization Client\\\\sfttray.exe\" \"OpenOffice.org Writer 3.4.9593.500\"", null, true),
    FILE_REPOSITORY_PATH("Indica il percorso dove trovare i file generati", "Path File Repository", "repository", null, true),
    FORMATO_DEFAULT("Indica il formato di default per i testi", "Formato di default", "odt", '<rowset><row label=".doc" value="doc" /><row label=".docx" codiceModello="docx" /><row label=".odt" codiceModello="odt" /></rowset>', true),
    JCONS_WSDL_URL("Indica l'url al wsdl del ws della conservazione", "JCONS_WSDL_URL", "http://localhost:8080/jconsWS/JConsServicePort?wsdl", null, true),
    JCONS_URL_SERVER("Indica l'url del server per la visualizzazione del documento in conservazione all'interno del LOG", "JCONS_URL_SERVER", "http://localhost:8080", null, true),
    JCONS_CONTEXT_PATH("Indica il nome del contesto del documentale per la visualizzazione del documento in conservazione all'interno del LOG", "JCONS_CONTEXT_PATH", "/jdms", null, true),
    JCONS_NOME_ITER("Indica il nome dell'iter di workflow da instanziare per la conservazione", "JCONS_NOME_ITER", "JSUITE_CONSERVAZIONE_STD", null, true),
    FILE_DA_PUBBLICARE("Indica il file da pubblicare all'albo, o la stampa unica o il testo", "File da pubblicare", "SU", '<rowset><row label="Stampa unica" value="SU" /><row label="Testo" codiceModello="T" /><row label="Nessuno" codiceModello="N" /></rowset>', true),
    RISERVATO("Definisce se è abilitata la riservatezza oppure no", "RISERVATO", "Y", '<rowset><row label="Si" value="Y" /><row label="No" codiceModello="N" /></rowset>', true),
    TAG_MAIL_AUTO("Tag Mail Inviate", "Tag Mail Inviate", "", null, true),
    TAG_INVIO_MAIL("Alias selezionato fra quelli indicati nel si4cim.cfg per l'invio di mail NON certicate", "Alias mail NON certificate", "mail", null, true),
    ALIAS_INVIO_MAIL_CERT("Alias selezionato fra quelli indicati nel si4cim.cfg per l'invio di mail certicate", "Alias mail certificate", "mail_cert", null, true),
    MITTENTE_INVIO_MAIL("Mittente per l'invio di mail", "Mittente mail", "segreteria@ads.it", null, true),
    MITTENTE_INVIO_MAIL_CERT("Mittente per l'invio di mail certicate", "Mittente mail certicate", "istituzionaleads@cert.legalmail.it", null, true),
    INTEGRAZIONE_ALBO("Abilita o meno l'integrazione con l'Albo JMessi.", "Integrazione Albo JMessi", "N", '<rowset><row label="Si" value="Y" /><row label="No" codiceModello="N" /></rowset>', true),
    INTEGRAZIONE_GDM("Abilita o meno l'integrazione con il documentale GDM", "Integrazione JDMS", "Y", '<rowset><row label="Si" value="Y" /><row label="No" codiceModello="N" /></rowset>', true),
    INTEGRAZIONE_CASA_DI_VETRO("Abilita o meno l'integrazione con la Casa di Vetro", "Integrazione Casa di Vetro", "N", '<rowset><row label="Si" value="Y" /><row label="No" codiceModello="N" /></rowset>', true),
    FIRMA_REMOTA("Definisce se è abilitata la firma remota", "FIRMA REMOTA", "N", '<rowset><row label="Si" value="Y" /><row label="No" codiceModello="N" /></rowset>', true),
    SO4_SUDDIVISIONE_AREA("Codice della suddivisione SO4 che individua l'codiceArea (Unità di Primo Livello)", "CODICE AREA SUDDIVISIONE SO4", "AREA", null, true),
    SO4_SUDDIVISIONE_SERVIZIO("Codice della suddivisione SO4 che individua il servizio (Unità di Secondo Livello)", "CODICE SERVIZIO SUDDIVISIONE SO4", "SERVIZIO", null, true),
    DOCER("Indica l'integrazione al repository documentale di Doc/Er", "DOCER", "Y", '<rowset><row label="Si" value="Y" /><row label="No" codiceModello="N" /></rowset>', true),
    DOCER_WS_UTENTE("Utente Doc/Er webservice", "UTENTE DOC/ER WS", "ads", null, true),
    DOCER_WSDL("Indica l'url al wsdl del ws per l'interrogazione a Do/Er", "DOCER_WSDL", "http://10.97.11.13:8080/WSDocer/services/DocerServices?wsdl", null, true),
    DOCER_STATO_SINCRONIZAZZIONE_URL("Indica l'url alla servlet da invocare per recuperare lo stato di sincronizzazione su Do/Er", "DOCER_STATO_SINCRONIZAZZIONE_URL", "http://localhost:8080/Doc-Er-Jdms-Web/SyncroInfoServlet?IDDOCGDM=", null, true),
    DOCER_AUTH_WSDL("Indica l'url al wsdl del ws per l'autenticazione a Doc/Er", "DOCER_AUTH_WSDL", "http://10.97.11.13:8080/docersystem/services/AuthenticationService?wsdl", null, true),
    DOCER_FASCICOLAZIONE_WSDL("Indica l'url al wsdl del ws per la fascicolazione a Doc/Er", "DOCER_FASCICOLAZIONE_WSDL", "http://10.97.11.13:8080/WSFascicolazione/services/WSFascicolazione?wsdl", null, true),
    DOCER_PROTOCOLLAZIONE_WSDL("Indica l'url al wsdl del ws per la protocollazione a Doc/Er", "DOCER_PROTOCOLLAZIONE_WSDL", "http://10.97.11.13:8080/WSProtocollazione/services/WSProtocollazione?wsdl", null, true),
    DOCER_REGISTRAZIONE_WSDL("Indica l'url al wsdl del ws per la registrazioneparticolare a Doc/Er", "DOCER_REGISTRAZIONE_WSDL", "http://10.97.11.13:8080/WSRegistrazione/services/WSRegistrazione?wsdl", null, true),
    DOCER_WS_CODICE_ENTE("Indica l'ente Doc/Er webservice", "CODICE ENTE DOC/ER WS", "P_RA", null, true),
    DOCER_WS_CODICE_AOO("Indica il codice AOO Doc/Er webservice", "CODICE AOO DOC/ER WS", "AOOPRORA", null, true),
    DOCER_WS_DENOMINAZIONE_AOO("Indica la denominazione AOO Doc/Er webservice", "DENOMINAZIONE AOO DOC/ER WS", "AOO Provincia di Ravenna", null, true),
    IMPORT_ALLEGATO_GDM("Indica la funzionalità di import degli allegati di GDM", "IMPORT_ALLEGATO_GDM", "N", '<rowset><row label="Si" value="Y" /><row label="No" codiceModello="N" /></rowset>', true),
    IMPORT_ALLEGATO_GDM_URL("Indica l'url alla servlet da invocare per effettuare l'import degli allegati su GDM", "IMPORT_ALLEGATO_GDM_URL", "http://localhost:8080", null, true),
    VIS_MENU("Voci del menu del Visualizzatore", "MENU VISUALIZZATORE", """<menu login="true"><link sezione="DELIBERE" action="ricercaDelibere">Deliberazioni</link><link sezione="DETERMINE" action="ricercaDetermine">Determinazioni</link><link sezione="ODG" action="ricercaSedute">O. d. G.</link></menu>""", null, false),
    LUNGHEZZA_OGGETTO("Lunghezza massima dell'oggetto dei documenti determina/proposta delibera/delibera", "Lunghezza massima dell'oggetto dei documenti", "2000", null, true),
    DEFAULT_FUNZIONARIO("Indica il valore di default della presenza del funzionario per le tipologie di atti che prevedono il funzionario non obbligatorio", "DEFAULT FUNZIONARIO", "N", '<rowset><row label="Si" value="Y" /><row label="No" codiceModello="N" /></rowset>', true),
    ALLEGATO_STATO_FIRMA_DEFAULT("Indica il valore di default dello stato della firma in creazione nuovo allegato", "ALLEGATO STATO FIRMA DEFAULT", "DA_FIRMARE", '<rowset><row label="Da Non Firmare" value="DA_NON_FIRMARE" /><row label="Da Firmare" codiceModello="DA_FIRMARE" /></rowset>', true),
    ALLEGATO_STAMPA_UNICA_DEFAULT("Indica il valore di default della stampa unica in creazione nuovo allegato", "ALLEGATO STAMPA UNICA DEFAULT", "Y", '<rowset><row label="Si" value="Y" /><row label="No" codiceModello="N" /></rowset>', true),
    RUOLO_SO4_DIZIONARI_PROTOCOLLO("Ruolo di accesso alla sezione 'Dizionari:Protocollo'", "RUOLO SO4 DIZIONARI PROTOCOLLO", "AGPSUP", null, true),
    RUOLO_SO4_DIZIONARI_IMPOSTAZIONI("Ruolo di accesso alla sezione 'Dizionari:Impostazioni'", "RUOLO SO4 DIZIONARI IMPOSTAZIONI", "AGPSUP", null, true),
    FIRMA_CON_TIMESTAMP("Indica se va aggiunto il timestamp della data di firma (non la marca temporale) al p7m.", "TIMESTAMP IN FIRMA", "Y", '<rowset><row label="Si" value="Y" /><row label="No" codiceModello="N" /></rowset>', true),
    FIRMA_HASH("Abilita la firma tramite hash", "FIRMA HASH", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
    FIRMA_PADES("Abilita la firma PADES", "FIRMA PADES", "N", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
    SUFFISSO_FIRMA_PADES("Suffisso da inserire nel nomefile per la firma Pades se valorizzato", "SUFFISSO FIRMA PADES", "", null, true),

    FIRMA_PDF_VISIBILE ("SI oppure NO (default)", "FIRMA_PDF_VISIBILE", "NO", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
    FIRMA_PDF_PAGINA("ULTIMA (questo è il default, altrimenti si può mettere il numero di pagina)", "FIRMA PDF PAGINA", "ULTIMA", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
    FIRMA_PDF_POSIZIONE("Abilita la firma PADES", "FIRMA PDF POSIZIONE", "BASSO_DX", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
    GESTIONE_CORTE_CONTI("Indica se va gestito l'invio alla corte dei conti", "GESTIONE CORTE DEI CONTI", "N", '<rowset><row label="Si" value="Y" /><row label="No" codiceModello="N" /></rowset>', true),
    ODG_NUMERA_DELIBERE("Indica se le delibere vengono numerate nella gestione odg.", "ODG NUMERA DELIBERE", "Y", '<rowset><row label="Si" value="Y" /><row label="No" codiceModello="N" /></rowset>', true),
    URL_SERVER_PUBBLICO("Indica l'indirizzo pubblico dell'applicativo (atti e visualizzatore)", "URL SERVER PUBBLICO", " ", '', true),
    DATO_AGGIUNTIVO("Flag che definisce se è presente il dizionario " + Labels.getLabel("label.datiAggiuntivi.determina") + " (Y/N)", Labels.getLabel("label.datiAggiuntivi.determina")?.toUpperCase(), "N", '<rowset><row label="Si" value="Y" /><row label="No" codiceModello="N" /></rowset>', true),
    ESPORTAZIONE_NUMERO_MASSIMO("Definisce il numero massimo di documento da esportare", "ESPORTAZIONE_NUMERO_MASSIMO", "2000", null, true),
    ESEGUIBILITA_IMMEDIATA_ATTIVA("Indica se è abilitata l'eseguibilità immediata.", "ESEGUIBILITA_IMMEDIATA_ATTIVA", "Y", '<rowset><row label="No"  value="N" /><row label="Si"  codiceModello="Y" /></rowset>', true),
    GDM_URL("Indica l'url per accedere alla casa di vetro", "URL GDM", "/jdms/common/DocumentoView.do", null, true),
    URL_SERVER("Url di accesso all'applicativo Sfera. Utile per la costruzione di link da dare ad integrazioni, ad es. CFA.", "URL SERVER", " ", null, true),
    TIPOLOGIA_PREFIX("Prefisso per la tipologia presente nelle deleghe", "Prefisso per la tipologia presente nelle deleghe", "AGP_", null, true),
    RUOLO_SO4_FIRMATARIO_CERT_PUBB("Ruolo che identifica i firmatari dei certificati di pubblicazione e avvenuta pubblicazione", "Ruolo del firmatario dei Certificati", "AGDCERTF", null, true),
    MAXDIM_ATTACH("Dimensione massima degli allegati di un protocollo in bytes", "\"Dimensione massima degli allegati di un protocollo in bytes\"", null, null, true),
    MAXDIM_ATTACH_ALT("Controllo bloccante sulla dimensione degli allegati di un protocollo", "\"Controllo bloccante sulla dimensione degli allegati di un protocollo\"", null, null, true),
    AG_SERVER_URL("Url di accesso all'applicativo Prisma. Utile per la costruzione di link da dare ad integrazioni, ad es. CFA.", "URL SERVER", "..", null, true),
    AG_CONTEXT_NAME("Nome del contesto dell'applicativo Prisma. Utile per la costruzione di link da dare ad integrazioni, ad es. SmartDektop.", "CONTEXT NAME", "/Protocollo", null, true),
    ALLEGATO_VERIFICA_FIRMA("Abilita la verifica automatica della firma degli allegati", "ALLEGATO_VERIFICA_FIRMA", "", '', true),
    CARATTERI_SPECIALI_NOME_FILE("Caratteri speciali non utilizzabili in nome file", "CARATTERI_SPECIALI_NOME_FILE", "", '', true),
    UNITA_CONCAT_CODICE("Indica se alla descrizione delle unità deve essere prefisso il codice. Valori possibili: Y/N; default N", "UNITA_CONCAT_CODICE", "N", null, true),
    RUOLO_SUPER_UTENTE("Ruolo che descrive l'utente amministratore", 'RUOLO_SUPER_UTENTE', 'AGPSUP', null, false),
    RUOLO_CONSERVAZIONE("Ruolo che descrive l'utente abilitato alla conservazione", 'RUOLO_CONSERVAZIONE', 'AGPCONS', null, false)

    // proprietà
    private final String descrizione
    private final String etichetta
    private final String predefinito
    private final String caratteristiche
    private final boolean modificabile = false

    public static final String SEPARATORE = "#"
    public static ImpostazioniMap map

    Impostazioni(String descrizione
                 , String etichetta
                 , String predefinito
                 , String caratteristiche
                 , boolean modificabile) {
        this.descrizione = descrizione
        this.etichetta = etichetta
        this.predefinito = predefinito
        this.caratteristiche = caratteristiche
        this.modificabile = modificabile
    }

    String getPredefinito() {
        return this.predefinito
    }

    String getValore(Long ente) {
        return map.getValore(this.toString(), predefinito, ente)
    }

    String getValore() {
        return map.getValore(this.toString(), predefinito)
    }

    int getValoreInt() {
        if (!getValore()) {
            return 0
        }
        return Integer.parseInt(getValore())
    }

    //ritorna il valore in Long, in caso di NumberFormatException( es: valore non rappresentabile nel range del Long) ritorna il valore massimo rappresentabile.
    long getValoreLong() {
        if (!getValore()) {
            return new Long(0)
        }
        try {
            return Long.parseLong(getValore())
        } catch (NumberFormatException e) {
            return Long.MAX_VALUE
        }
    }

    String[] getValori() {
        return this.getValore()?.split(SEPARATORE) ?: []
    }

    boolean isAbilitato() {
        return "Y".equalsIgnoreCase(this.getValore())
    }

    byte[] getRisorsa() {
        return map.getRisorsa(this.toString())
    }
}