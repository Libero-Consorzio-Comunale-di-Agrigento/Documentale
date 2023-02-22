CREATE OR REPLACE PACKAGE BODY Amv_Utente AS
FUNCTION VERSIONE  RETURN VARCHAR2 IS
/******************************************************************************
 NOME:        VERSIONE
 DESCRIZIONE: Restituisce la versione e la data di distribuzione del package.
 PARAMETRI:   --
 RITORNA:     stringa varchar2 contenente versione e data.
******************************************************************************/
BEGIN
   RETURN d_versione||' Rev.'||d_revisione;
END VERSIONE;
PROCEDURE REGISTRA_UTENTE
( p_utente         OUT VARCHAR2
, p_mezza_password OUT VARCHAR2
, p_nominativo      IN VARCHAR2
, p_cognome         IN VARCHAR2
, p_nome            IN VARCHAR2
, p_codice_fiscale  IN VARCHAR2
, p_comune      IN OUT NUMBER
, p_provincia   IN OUT NUMBER
, p_cap         IN OUT VARCHAR2
, p_via             IN VARCHAR2
, p_indirizzo       IN VARCHAR2
, p_numero          IN VARCHAR2
, p_comune_nas      IN NUMBER
, p_provincia_nas   IN NUMBER
, p_data_nascita    IN VARCHAR2
, p_sesso           IN VARCHAR2
) IS
/******************************************************************************
 NOME:        REGISTRA_UTENTE
 DESCRIZIONE: Procedure utilizzata da web: lancia la procedure di registrazione di un
            nuovo utente.
 PARAMETRI:   p_utente          VARCHAR2      codice utente assegnato
           p_mezza_password  VARCHAR2      prima parte della password assegnata al nuovo utente.
 ECCEZIONI:   nnnnn, <Descrizione eccezione>
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    20/02/2003 AO     Prima emissione.
******************************************************************************/
d_utente          VARCHAR2(8);
d_mezza_password  VARCHAR2(4);
d_indirizzo        VARCHAR2(40);
d_indirizzo_web   VARCHAR2(40);
d_telefono         VARCHAR2(40);
d_fax           VARCHAR2(40);
d_cap           VARCHAR2(40);
BEGIN
   IF NOT (p_data_nascita BETWEEN '01/01/1900' AND '30/12/2010') THEN
      NULL;
   END IF;
   BEGIN -- Registrazione utente stato Sospeso
      AD4_UTENTE.REGISTRA_WEB(d_utente, d_mezza_password,UPPER(p_nominativo), UPPER(p_cognome), UPPER(p_nome), UPPER(p_codice_fiscale), p_comune, p_provincia, p_cap, d_indirizzo, p_comune_nas, p_provincia_nas, p_data_nascita, p_sesso, d_indirizzo_web, d_telefono, d_fax);
      p_utente := d_utente;
      p_mezza_password := d_mezza_password;
   END;
END REGISTRA_UTENTE;
FUNCTION IS_PAGINA_PARAMETRI (p_modulo VARCHAR2) RETURN NUMBER AS
d_pagina   VARCHAR2(200);
BEGIN
   BEGIN
   SELECT 1 INTO d_pagina
     FROM AMV_VOCI
   WHERE VOCE = UPPER(SUBSTR(p_modulo,1,5))||'RGP';
   EXCEPTION
    WHEN NO_DATA_FOUND THEN
    RETURN 0;
   END;
  RETURN 1;
END IS_PAGINA_PARAMETRI;
PROCEDURE AGGIORNA_UTENTE
( p_richiesta     IN OUT VARCHAR2
, p_utente        IN  VARCHAR2
, p_modulo        IN  VARCHAR2
, p_istanza       IN  VARCHAR2
, p_comune        IN  NUMBER
, p_provincia     IN  NUMBER
, p_cap           IN  VARCHAR2
, p_via           IN  VARCHAR2
, p_indirizzo     IN  VARCHAR2
, p_num           IN  VARCHAR2
, p_indirizzo_web IN  VARCHAR2
, p_telefono      IN  VARCHAR2
, p_fax           IN  VARCHAR2
) IS
/******************************************************************************
 NOME:        AGGIORNA_UTENTE
 DESCRIZIONE: Procedure utilizzata da web: lancia aggiornamento dei dati dell'utente, lancia una richiesta
            di abilitazione ad un servizio e, se il servizio non prevede parametri aggiuntivi, lancia
           la gestione dello stato finale ('C' oppure 'A') della richiesta.
 PARAMETRI:   p_richiesta    NUMBER         identificativo assegnato alla richiesta
            p_utente       VARCHAR2      codice utente che sta richiedendo ablitazione al servizio.
           p_modulo       VARCHAR2      modulo.
           p_istanza      VARCHAR2      istanza.
 ECCEZIONI:   nnnnn, <Descrizione eccezione>
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    20/02/2003 AO     Prima emissione.
******************************************************************************/
d_richiesta       NUMBER;
d_abilitazione    VARCHAR2(1);
BEGIN
   AGGIORNA(p_utente, NULL, p_comune, p_provincia, p_cap, p_via, p_indirizzo, p_num, p_indirizzo_web, p_telefono, p_fax);
   AD4_UTENTE.RICHIESTA_ABILITAZIONE (d_richiesta, p_utente, p_modulo, p_istanza);
   BEGIN
      SELECT abilitazione
        INTO d_abilitazione
          FROM AD4_SERVIZI
         WHERE istanza = p_istanza
           AND modulo = p_modulo;
      EXCEPTION
      WHEN OTHERS THEN
        d_abilitazione := 'C';
   END;
   p_richiesta := d_richiesta;
END AGGIORNA_UTENTE;
PROCEDURE  REGISTRA_RICHIESTA (
     p_richiesta    IN OUT VARCHAR2
   , p_utente       IN VARCHAR2
   , p_nuovo_utente IN VARCHAR2
   , p_modulo       IN VARCHAR2
   , p_istanza      IN VARCHAR2
) IS
/******************************************************************************
 NOME:        REGISTRA_RICHIESTA
 DESCRIZIONE: Crea una richiesta di abilitazione relativa all'utente sul servizio
            (modulo + istanza) passato.
 PARAMETRI:   p_richiesta    NUMBER         identificativo assegnato alla richiesta
            p_utente       VARCHAR2      codice utente che sta richiedendo ablitazione al servizio.
           p_modulo       VARCHAR2      modulo.
           p_istanza      VARCHAR2      istanza.
 ECCEZIONI:   nnnnn, <Descrizione eccezione>
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    20/02/2003 AO     Prima emissione.
******************************************************************************/
d_richiesta       NUMBER;
d_tipo_notifica   VARCHAR2(10);
d_required        VARCHAR2(100):='';
d_utente          VARCHAR2(8);
d_soggetto        NUMBER(8);
d_error           VARCHAR2(200):='';
BEGIN
   BEGIN -- Verifico se il servizio prevede una specifica modalita di notifica.
        SELECT tipo_notifica
           INTO d_tipo_notifica
             FROM ad4_servizi
            WHERE modulo  = p_modulo
              AND istanza = p_istanza;
   EXCEPTION
      WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20999,'Servizio non corretto. Verificare Modulo e Istanza.');
   END;
   -- Imposto l'utente per il quale registrare la richiesta
   --if p_nuovo_utente is not null and p_utente = 'GUEST' then
   IF p_nuovo_utente IS NOT NULL THEN
      d_utente := p_nuovo_utente;
   ELSE
      d_utente := p_utente;
   END IF;
   IF d_tipo_notifica IS NOT NULL THEN
     -- Verifico che l'utente abbia un soggetto associato
      d_soggetto := ad4_utente.get_soggetto(d_utente,'Y');
     IF d_soggetto IS NULL THEN
        d_error := 'Utente non abbinato a soggetto anagrafico.';
     ELSE
         -- Verifico che il soggetto possieda i dati necessari alla notifica
          IF d_tipo_notifica = 'MAIL' THEN
             d_required := ad4_soggetto.get_indirizzo_web(d_soggetto,'Y');
           d_error := 'Verificare indirizzo e-mail.';
          ELSIF d_tipo_notifica = 'POSTA' THEN
             d_required := ad4_soggetto.get_indirizzo_completo(d_soggetto,'Y');
           d_error := 'Verificare i dati relativi alla residenza.';
          ELSIF d_tipo_notifica = 'SMS' THEN
             d_required := ad4_soggetto.get_telefono(d_soggetto,'Y');
           d_error := 'Verificare i dati relativi al telefono per comunicazione SMS.';
           IF (d_required IS NOT NULL) THEN
              BEGIN
                SELECT TO_NUMBER(d_required)
                  INTO d_required
                 FROM dual
                ;
             EXCEPTION
                WHEN OTHERS THEN
                   d_error := 'Verificare il formato del numero telefonico indicato.';
                  d_required := NULL;
             END;
            END IF;
          END IF;
      END IF;
   ELSE
      d_required := 'no_required';
   END IF;
   IF d_required IS NOT NULL THEN
      IF p_richiesta IS NULL THEN -- Crea nuova richiesta solo se non e gia presente una richiesta
        AD4_UTENTE.RICHIESTA_ABILITAZIONE (d_richiesta, d_utente, p_modulo, p_istanza);
       p_richiesta := d_richiesta;
     END IF;
   ELSE
      RAISE_APPLICATION_ERROR(-20999,'Dati per la notifica dell''abilitazione non corretti. '||d_error);
   END IF;
END REGISTRA_RICHIESTA;
PROCEDURE  GESTISCI_RICHIESTA (
  p_richiesta      IN NUMBER
, p_tipo_convalida IN VARCHAR2 DEFAULT 'A'
) IS
/******************************************************************************
 NOME:        GESTISCI_RICHIESTA
 DESCRIZIONE: Imposta lo stato della richiesta a 'C' (da convalidare) oppure 'A' (approvata) dopo aver
            verificato se esiste una procedure di validazione specifica per il servizio richiesto.
 PARAMETRI:   p_id_richiesta NUMBER         identificativo della richiesta
 ECCEZIONI:   nnnnn, <Descrizione eccezione>
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    20/02/2003 AO     Prima emissione.
 1    20/01/2005 AO     Gestione notifica per richieste approvate
 2    12/05/2005 AO     Gestione note aggiuntive mail notifica come preferenza
******************************************************************************/
d_richiesta       NUMBER;
d_abilitazione    VARCHAR2(1);
d_stato           VARCHAR2(1);
d_automatismo     VARCHAR2(1) :='S';
d_livello        VARCHAR2(8);
d_utente        VARCHAR2(8);
d_modulo        VARCHAR2(10);
d_istanza        VARCHAR2(10);
d_validazione     NUMBER := 0;
d_approvazione    NUMBER := 0;
d_note            VARCHAR2(4000);
BEGIN
-- Cerco modulo per il quale si e fatta la richiesta abilitazione e lo stato della richiesta
   SELECT modulo, istanza, utente, stato
     INTO d_modulo, d_istanza, d_utente, d_stato
     FROM AD4_RICHIESTE_ABILITAZIONE
    WHERE id_richiesta = p_richiesta;
-- Verifica esistenza procedure di approvazione/respinta specifica
   BEGIN
        SELECT 1
        INTO d_approvazione
        FROM OBJ
         WHERE OBJECT_NAME = d_modulo||'_APPROVA_ABILITAZIONE'
           AND OBJECT_TYPE = 'PROCEDURE';
   EXCEPTION WHEN NO_DATA_FOUND THEN
      NULL;
   END;
-- Note inserite in coda alla mail
   d_note := Amvweb.get_preferenza('Notifica mail',d_modulo);
-- Se il tipo di convalida richiede di respingere la richiesta (p_tipo_convalida = 'R') la richiesta viene messa
-- in stato 'R'.
-- Altrimenti viene eseguita la gestione dell'approvazione.
   IF p_tipo_convalida = 'R' THEN
      -- Lancio procedure di approvazione specifica(per respingere la richiesta)  utilizzando package SI4
      IF d_approvazione = 1 THEN
         BEGIN
            Si4.SQL_EXECUTE('begin '||d_modulo||'_APPROVA_ABILITAZIONE('||p_richiesta||',0); END;');
         EXCEPTION WHEN OTHERS THEN
            RAISE;
         END;
      END IF;
      AD4_UTENTE.GESTISCI_RICHIESTA(p_richiesta,'R');
-- Lancio della notifica per le richieste approvate ma non notificate
   ELSIF p_tipo_convalida = 'N' THEN
          AD4_UTENTE.NOTIFICA_RICHIESTA(p_richiesta);
   ELSE
   -- Se la richiesta e in carico (stato='C') e il tipo convalida e di richiesta approvazione (A)
   -- viene automaticamente approvata. Il tipo di convalida puo essere C in caso di controllo.
   -- Se si trova in stato da convalidare (stato='F') viene eseguito il controllo e la successiva
   -- modifica dello stato in 'C' o 'A' (approvata) in funzione del tipo di servizio (automatico
   -- o controllato) e del successo della validazione specifica.
      IF d_stato = 'C' AND p_tipo_convalida = 'A' THEN
         -- Lancio procedure di approvazione specifica(per approvare la richiesta)  utilizzando package SI4
         IF d_approvazione = 1 THEN
            BEGIN
               Si4.SQL_EXECUTE('begin '||d_modulo||'_APPROVA_ABILITAZIONE('||p_richiesta||',1); END;');
            EXCEPTION WHEN OTHERS THEN
               RAISE;
            END;
         END IF;
         AD4_UTENTE.GESTISCI_RICHIESTA(p_richiesta,'A',NULL, NULL, d_note);
      ELSE
      -- Verifica esistenza procedure di validazione specifica
         BEGIN
              SELECT 1
              INTO d_validazione
              FROM OBJ
               WHERE OBJECT_NAME = d_modulo||'_VALIDA_ABILITAZIONE'
                 AND OBJECT_TYPE = 'PROCEDURE';
         EXCEPTION WHEN NO_DATA_FOUND THEN
            NULL;
         END;
         IF d_validazione = 1 THEN
            BEGIN
      -- Lancio procedure di validazione specifica utilizzando package SI4
               Si4.SQL_EXECUTE('begin '||d_modulo||'_VALIDA_ABILITAZIONE('||p_richiesta||'); END;');
            EXCEPTION WHEN OTHERS THEN
               RAISE;
            END;
         END IF;
      /* Verifico livello di sicurezza dei servizi per cui l'utente h gi` abilitato
         Il controllo viene fatto in join sulle tabelle AD4_DIRITTI_ACCESSO E AD4_SERVIZI.
         Risultati possibili => conseguenze:
         1) Nessun servizio sul quale l'utente abbia diritto di accesso (caso
            di prima registrazione dell'utente al portale)  => automatismo del servizio preservato.
         2) Servizi su cui l'utente ha diritto di accesso ed adeguato livello di sicurezza
            rispetto al servizio che si sta richiedendo => automatismo del servizio preservato.
         3) Servizi su cui l'utente ha diritto di accesso ma livello di sicurezza non adeguato
            rispetto al servizio che si sta richiedendo => servizio SEMPRE NON automatico.
      */
         BEGIN -- Livello del servizio che sto richiedendo (per ora serve solo sapere che non sia nullo)
            SELECT s.livello
             INTO d_livello
             FROM AD4_SERVIZI s
            WHERE s.istanza = d_istanza
             AND s.modulo = d_modulo
            ;
           IF d_livello IS NOT NULL THEN  -- Verifico livello dei servizi gia abilitati
               BEGIN
                  d_automatismo := 'N';   -- Inibisco automatismo prima della verifica
                 SELECT 'S'
                    INTO d_automatismo
                    FROM dual
                    WHERE EXISTS (SELECT s.livello       -- la condizione sarebbe: d_livello in (select...)
                                   FROM AD4_SERVIZI s, AD4_DIRITTI_ACCESSO d
                              WHERE d.utente = d_utente
                                AND s.istanza = d.istanza
                                AND s.modulo = d.modulo
                               AND s.livello IS NOT NULL
                            )
                  ;
                EXCEPTION
                     WHEN NO_DATA_FOUND THEN
                     d_automatismo := 'N';
              END;
              END IF;
         EXCEPTION
              WHEN NO_DATA_FOUND THEN
               NULL;
         END;
      -- Verifico tipo di abilitazione prevista per il servizio (automatica o controllata)
         BEGIN
            SELECT s.abilitazione
              INTO d_abilitazione
                FROM AD4_SERVIZI s, AD4_RICHIESTE_ABILITAZIONE r
               WHERE s.istanza = r.istanza
                 AND s.modulo = r.modulo
             AND r.id_richiesta = p_richiesta
             ;
            EXCEPTION
            WHEN OTHERS THEN
              d_abilitazione := 'C';
         END;
         IF d_abilitazione = 'A' AND d_automatismo = 'S' AND (p_tipo_convalida = 'A' OR p_tipo_convalida = 'C') THEN -- custom
            -- Lancio procedure di approvazione specifica(per approvare la richiesta)  utilizzando package SI4
            IF d_approvazione = 1 THEN
               BEGIN
                  Si4.SQL_EXECUTE('begin '||d_modulo||'_APPROVA_ABILITAZIONE('||p_richiesta||',1); END;');
               EXCEPTION WHEN OTHERS THEN
                  RAISE;
               END;
            END IF;
            AD4_UTENTE.GESTISCI_RICHIESTA(p_richiesta,'A',NULL, NULL, d_note);
         ELSE
            AD4_UTENTE.GESTISCI_RICHIESTA(p_richiesta,'C');
         END IF;
      END IF;
   END IF;
END GESTISCI_RICHIESTA;
PROCEDURE  SET_PARAMETRO_RICHIESTA (
     p_id_richiesta IN NUMBER
   , p_nome         IN VARCHAR2
   , p_valore       IN VARCHAR2
   , p_utente       IN VARCHAR2
) IS
/******************************************************************************
 NOME:        SET_PARAMETRO_RICHIESTA
 DESCRIZIONE: Imposta il generico parametro della richiesta di abilitazione ad un servizio.
 PARAMETRI:   p_id_richiesta NUMBER         identificativo della richiesta
            p_nome         VARCHAR2      nome che si vuole assegnare al parametro.
           p_valore       VARCHAR2      valore che si vuole assegnare al parametro.
           p_utente       VARCHAR2      stringa che identifica l'utente di aggiornamento del record
                                    (non ha vincoli di FK con tabella AD4_UTENTI).
 ECCEZIONI:   nnnnn, <Descrizione eccezione>
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    20/02/2003 AO     Prima emissione.
******************************************************************************/
d_exists      NUMBER := 0;
d_error       VARCHAR2(2000);
BEGIN
   BEGIN
      SELECT 1 INTO d_exists
       FROM AD4_PARAMETRI_RICHIESTA
      WHERE id_richiesta = p_id_richiesta
        AND parametro = p_nome;
      UPDATE AD4_PARAMETRI_RICHIESTA
         SET VALORE = p_valore
         , UTENTE_AGGIORNAMENTO = p_utente
       WHERE id_richiesta = p_id_richiesta
         AND parametro = p_nome;
   EXCEPTION
      WHEN NO_DATA_FOUND THEN
         INSERT INTO AD4_PARAMETRI_RICHIESTA (ID_RICHIESTA, PARAMETRO, VALORE, UTENTE_AGGIORNAMENTO)
                                   VALUES (p_id_richiesta, p_nome, p_valore, p_utente)
       ;
      WHEN OTHERS THEN
         d_error := 'Errore nell''inserimento del parametro '||p_nome;
   END;
   /*if p_nome = 'CF_AZIENDA' and p_valore is not null then
   if ad4_codice_fiscale.controllo(p_valore) <0 then
         d_error := 'Codice Fiscale o Partita IVA non corretti.';
      end if;
   end if;*/
   IF d_error IS NOT NULL THEN
        RAISE_APPLICATION_ERROR(-20999,d_error);
   END IF;
END SET_PARAMETRO_RICHIESTA;
FUNCTION  GET_PARAMETRO_RICHIESTA (
     p_id_richiesta IN NUMBER
   , p_nome         IN VARCHAR2
) RETURN VARCHAR2 IS
d_error       VARCHAR2(2000);
d_valore     VARCHAR2(2000);
BEGIN
   BEGIN
      SELECT valore
       INTO d_valore
       FROM AD4_PARAMETRI_RICHIESTA
      WHERE id_richiesta = p_id_richiesta
        AND parametro = p_nome;
   EXCEPTION WHEN NO_DATA_FOUND THEN
         RETURN '';
   END;
   RETURN d_valore;
END GET_PARAMETRO_RICHIESTA;
PROCEDURE  SET_PARAMETRI_AZIENDA (
     p_id_richiesta IN NUMBER
   , p_rs           IN VARCHAR2
   , p_rs_valore    IN VARCHAR2
   , p_cf           IN VARCHAR2
   , p_cf_valore    IN VARCHAR2
   , p_utente       IN VARCHAR2
) IS
/******************************************************************************
 NOME:        SET_PARAMETRI_AZIENDA
 DESCRIZIONE: Imposta come parametri della richiesta di abilitazione ad un servizio la ragione
            sociale ed il codice fiscale dell'azienda.
 PARAMETRI:   p_id_richiesta NUMBER         identificativo della richiesta
            p_rs           VARCHAR2      nome che si vuole assegnare al parametro relativo alla
                                    ragione sociale
           p_rs_valore    VARCHAR2      valore che si vuole assegnare al parametro relativo alla
                                    ragione sociale
           p_cf           VARCHAR2      nome che si vuole assegnare al parametro relativo al
                                    codice fiscale
           p_cf_valore    VARCHAR2      valore che si vuole assegnare al parametro relativo al
                                    codice fiscale
           p_utente       VARCHAR2      stringa che identifica l'utente di aggiornamento del record
                                    (non ha vincoli di FK con tabella AD4_UTENTI). Da web
                                    si puo utilizzare una stringa standard cosi da identificare
                                 tutti i record che hanno provenienza web.
 ECCEZIONI:   nnnnn, <Descrizione eccezione>
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    20/02/2003 AO     Prima emissione.
******************************************************************************/
d_error       VARCHAR2(2000);
d_abilitazione    VARCHAR2(1);
BEGIN
   IF p_cf_valore IS NOT NULL THEN
     IF ad4_codice_fiscale.controllo(p_cf_valore) <0 THEN
         RAISE_APPLICATION_ERROR(-20999,'Codice Fiscale o Partita IVA non corretti.');
      END IF;
   END IF;
   SET_PARAMETRO_RICHIESTA(p_id_richiesta, p_rs, p_rs_valore, p_utente);
   SET_PARAMETRO_RICHIESTA(p_id_richiesta, p_cf, p_cf_valore, p_utente);
END SET_PARAMETRI_AZIENDA;
PROCEDURE  MODIFICA_PASSWORD
( p_nuova_password    IN VARCHAR2
, p_conferma_password IN VARCHAR2
, p_utente        IN VARCHAR2
) IS
/******************************************************************************
 NOME:        MODIFICA_PASSWORD
 DESCRIZIONE: Aggiorna la password dell'utente
 PARAMETRI:   p_password_attuale  VARCHAR2      in chiaro
           p_nuova_password    VARCHAR2      in chiaro
           p_conferma_password VARCHAR2      in chiaro
 ECCEZIONI:   Messaggio nel caso in cui utente non abilitato al rinnovo password
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    20/02/2003 AO     Prima emissione.
 1    20/01/2005 AO     Aggiunto setting utente aggiornamento
******************************************************************************/
d_nominativo     VARCHAR2(40);
d_password       VARCHAR2(40);
d_verifica       NUMBER;
BEGIN
-- Recupero il nominativo dell'utente
   BEGIN
      SELECT nominativo
        INTO d_nominativo
        FROM AD4_UTENTI
         WHERE utente = p_utente
           AND NVL(rinnovo_password,'SI') = 'SI';
   EXCEPTION WHEN NO_DATA_FOUND THEN
      RAISE_APPLICATION_ERROR(-20999,'Utente non abilitato al cambio password');
   END;
-- Verifica che la nuova password e la conferma coincidano
   IF p_nuova_password = p_conferma_password THEN
      NULL;
   ELSE
      RAISE_APPLICATION_ERROR(-20999,'La nuova password e la sua conferma non coincidono');
   END IF;
-- Aggiorno password
      DECLARE
        d_utente VARCHAR2(8);
     BEGIN
           d_utente := p_utente;
         AD4_UTENTE.INITIALIZE(d_utente);
         AD4_UTENTE.SET_PASSWORD(p_nuova_password);
         AD4_UTENTE.SET_DATA_PASSWORD(TO_CHAR(SYSDATE,'dd/mm/yyyy'));
         AD4_UTENTE.SET_UTENTE_AGG(d_utente);
        AD4_UTENTE.UPDATE_UTENTE(p_modifica_sogg => 'N');
       UPDATE ad4_utenti
           SET note = REPLACE(note,SUBSTR(note,INSTR(note,'Codice='),23),'')
          WHERE utente = d_utente;
      EXCEPTION WHEN OTHERS THEN
        RAISE;
     END;
END MODIFICA_PASSWORD;
PROCEDURE  MODIFICA_PASSWORD
( p_password_attuale  IN VARCHAR2
, p_nuova_password    IN VARCHAR2
, p_conferma_password IN VARCHAR2
, p_utente        IN VARCHAR2
) IS
/******************************************************************************
 NOME:        MODIFICA_PASSWORD
 DESCRIZIONE: Aggiorna la password dell'utente
 PARAMETRI:   p_utente            VARCHAR2      codice utente
              p_password_attuale  VARCHAR2      in chiaro
           p_nuova_password    VARCHAR2      in chiaro
           p_conferma_password VARCHAR2      in chiaro
 ECCEZIONI:   Messaggio nel caso in cui utente non abilitato al rinnovo password
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    20/02/2003 AO     Prima emissione.
 1    29/12/2004 AO     Predisposizione per AD4 integrato con LDAP
 2    20/01/2005 AO     Aggiunto setting utente aggiornamento
******************************************************************************/
d_nominativo     VARCHAR2(40);
d_password       VARCHAR2(40);
d_ldap           NUMBER := 0;
d_verifica       NUMBER;
BEGIN
-- Recupero il nominativo dell'utente
   BEGIN
      SELECT nominativo
        INTO d_nominativo
        FROM AD4_UTENTI
         WHERE utente = p_utente
           AND NVL(rinnovo_password,'SI') = 'SI';
   EXCEPTION WHEN NO_DATA_FOUND THEN
      RAISE_APPLICATION_ERROR(-20999,'Utente non abilitato al cambio password');
   END;
-- Verifica che la nuova password e la conferma coincidano
   IF p_nuova_password = p_conferma_password THEN
      NULL;
   ELSE
      RAISE_APPLICATION_ERROR(-20999,'La nuova password e la sua conferma non coincidono');
   END IF;
-- Verifica che la password attuale sia corretta
   d_password := p_password_attuale;
   d_verifica := AD4_CRYPT.VERIFICA_PASSWORD(d_nominativo, d_password);
-- Aggiorno password
   IF d_verifica = 1 THEN
      DECLARE
        d_utente VARCHAR2(8);
     BEGIN
           d_utente := p_utente;
         AD4_UTENTE.INITIALIZE(d_utente);
       BEGIN
            SELECT MIN(1)
              INTO d_ldap
              FROM all_source
             WHERE owner = 'AD4'
               AND NAME = 'UTENTE'
               AND INSTR(UPPER(text),'SI4AU') > 0
          ;
            IF d_ldap = 1 THEN
          -- chiamata con 2 parametri
              Si4.SQL_EXECUTE('begin AD4_UTENTE.SET_PASSWORD('''||p_nuova_password||''','''||p_password_attuale||'''); END;');
            ELSE
               AD4_UTENTE.SET_PASSWORD(p_nuova_password);
            END IF;
       END;
         AD4_UTENTE.SET_DATA_PASSWORD(TO_CHAR(SYSDATE,'dd/mm/yyyy'));
         AD4_UTENTE.SET_UTENTE_AGG(d_utente);
           AD4_UTENTE.UPDATE_UTENTE(p_modifica_sogg => 'N');
      EXCEPTION WHEN OTHERS THEN
        RAISE;
     END;
   ELSE
      RAISE_APPLICATION_ERROR(-20999,'La password attuale immessa non e corretta');
   END IF;
END MODIFICA_PASSWORD;
PROCEDURE  RECUPERA_LOGIN
( p_nome              IN VARCHAR2
, p_cognome           IN VARCHAR2
, p_email             IN VARCHAR2
, p_modulo            IN VARCHAR2
, p_istanza            IN VARCHAR2
, p_progetto         IN VARCHAR2
, p_link              IN VARCHAR2
) IS
d_soggetto        NUMBER(8);
d_nome            VARCHAR2(50);
d_email           VARCHAR2(100);
d_esiste          NUMBER(1);
d_utente          VARCHAR2(8);
d_nominativo      VARCHAR2(40);
d_verifica_pwd    NUMBER(1);
d_progetto        VARCHAR2(8);
d_codice          VARCHAR2(16);
d_send            NUMBER;
d_string          VARCHAR2(4000);
d_link            VARCHAR2(4000);
BEGIN
   IF p_cognome IS NULL THEN
      RAISE_APPLICATION_ERROR(-20999,'Cognome Utente obbligatorio.');
   END IF;
   IF p_nome IS NULL THEN
      RAISE_APPLICATION_ERROR(-20999,'Nome Utente obbligatorio.');
   END IF;
   IF p_email IS NULL THEN
      RAISE_APPLICATION_ERROR(-20999,'Email Utente obbligatoria.');
   END IF;
   d_nome := UPPER(p_cognome)||'  '||UPPER(p_nome);
   /* Verifico esistenza soggetto*/
   SELECT NVL(MIN(1),0)
     INTO d_esiste
     FROM ad4_soggetti
   WHERE nome = d_nome;
   IF d_esiste = 0 THEN
      /*Il soggetto non risulta registrato */
      RAISE_APPLICATION_ERROR(-20999,'Soggetto non previsto.');
   ELSE
      BEGIN
     /* Verifico univocita del soggetto*/
        SELECT soggetto
          INTO d_soggetto
          FROM ad4_soggetti
          WHERE nome = d_nome
          AND indirizzo_web = p_email;
     EXCEPTION
        WHEN TOO_MANY_ROWS THEN
           RAISE_APPLICATION_ERROR(-20999,'Piu soggetti registrati con questi dati');
       WHEN NO_DATA_FOUND THEN
           RAISE_APPLICATION_ERROR(-20999,'Dati inseriti non congruenti.');
     END;
   END IF;
   /* Determino il nominativo dell'utente */
   BEGIN
      SELECT uten.utente, uten.nominativo
       INTO d_utente, d_nominativo
       FROM ad4_utenti uten, ad4_utenti_soggetti utso
      WHERE utso.soggetto = d_soggetto
        AND uten.utente = utso.utente;
   EXCEPTION
      WHEN OTHERS THEN
      RAISE_APPLICATION_ERROR(-20999,'Nominativo non recuperabile'||TO_CHAR(d_soggetto));
   END;
   /* Verifico che la password non sia scaduta o che ci siano ancora dei tentativi possibili
       e l'utente sia abilitato al cambio*/
   SELECT progetto
     INTO d_progetto
    FROM ad4_moduli
   WHERE modulo = p_modulo;
   d_verifica_pwd := is_pwd_valida(d_utente, p_modulo, p_istanza, d_progetto);
   /* Genero codice di controllo */
    d_codice := ad4_crypt.genera_password(16);
   /* Aggiorno campo note dell'utente richiedente*/
   UPDATE ad4_utenti
      SET note = REPLACE(note,SUBSTR(note,INSTR(note,'Codice='),23),'')||'Codice='||d_codice
    WHERE utente = d_utente;
    /* Invio Mail con il nominativo ed il link alla pagina di rinnovo password */
    d_string := 'Come richiesto le inviamo le informazioni sull''account e '||CHR(10)||
                'il link per poter modificare la Password.';
    d_link := SUBSTR(p_link,1,INSTR(p_link,'/',-1))||'AmvModificaPassword.do?COD=';
    d_send := Amvweb.send_msg('webmail@ads.it',
                         p_email,
                         'Informazioni Account',
                         d_string||CHR(10)||
                   'Nominativo: '||d_nominativo||CHR(10)||
                   'Link:'||d_link||d_codice,
                        'mail'
                        );
END RECUPERA_LOGIN;
PROCEDURE AGGIORNA
( p_utente        IN  VARCHAR2
, p_nuovo_utente  IN  VARCHAR2
, p_comune        IN  NUMBER
, p_provincia     IN  NUMBER
, p_cap           IN  VARCHAR2
, p_via           IN  VARCHAR2
, p_indirizzo     IN  VARCHAR2
, p_num           IN  VARCHAR2
, p_indirizzo_web IN  VARCHAR2
, p_telefono      IN  VARCHAR2
, p_fax           IN  VARCHAR2
) IS
/******************************************************************************
 NOME:        AGGIORNA
 DESCRIZIONE: Aggiorna i dati dell'utente sulla anagrafica dei soggetti
 PARAMETRI:   p_utente        VARCHAR2      codice utente
            p_nuovo_utente  VARCHAR2      codice utente per nuove registrazioni
 ECCEZIONI:   nnnnn, <Descrizione eccezione>
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    20/02/2003 AO     Prima emissione.
 1    14/03/2003 AO     Aggiunto parametro p_nuovo_utente
******************************************************************************/
d_indirizzo       VARCHAR2(80);
d_richiesta       NUMBER;
d_abilitazione    VARCHAR2(1);
BEGIN
-- Costruzione indirizzo dai singoli parametri passati
   IF p_indirizzo IS NOT NULL THEN
      d_indirizzo := p_via||' '||p_indirizzo||', '||p_num;
   END IF;
-- Aggiornamento dei dati dell'utente
   AD4_UTENTE.AGGIORNA_WEB(NVL(p_nuovo_utente,p_utente), NVL(p_comune,0), NVL(p_provincia,0), NVL(p_cap,'NO'), NVL(d_indirizzo,'NO'), NVL(p_indirizzo_web,'NO'), NVL(p_telefono,'NO'), NVL(p_fax,'NO'), 'Y');
END AGGIORNA;
PROCEDURE  SET_GRUPPO
( p_utente    IN VARCHAR2
, p_gruppo    IN VARCHAR2
) IS
/******************************************************************************
 NOME:        SET_GRUPPO
 DESCRIZIONE: Associa l'utente al gruppo
 PARAMETRI:   p_utente    VARCHAR2      codice utente
              p_gruppo    VARCHAR2      codice gruppo
 ECCEZIONI:   nnnnn, <Descrizione eccezione>
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    20/02/2003 AO     Prima emissione.
******************************************************************************/
d_indirizzo       VARCHAR2(40);
d_richiesta       NUMBER;
d_abilitazione    VARCHAR2(1);
BEGIN
-- verifico che il codice del gruppo corrisponda ad un gruppo
-- Aggiornamento dei dati dell'utente
NULL;
END SET_GRUPPO;
FUNCTION  GET_UTENTE_RICHIESTA (
     p_id_richiesta IN NUMBER
) RETURN VARCHAR2 IS
/******************************************************************************
 NOME:        GET_UTENTE_RICHIESTA
 DESCRIZIONE: Restituisce l'utente associato ad una richiesta
 PARAMETRI:   p_id_richiesta  NUMBER identificativo della richiesta
 ECCEZIONI:   nnnnn, <Descrizione eccezione>
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    27/03/2003 AO     Prima emissione.
******************************************************************************/
d_error       VARCHAR2(2000);
d_valore     VARCHAR2(2000);
BEGIN
   BEGIN
      SELECT utente
       INTO d_valore
       FROM AD4_RICHIESTE_ABILITAZIONE
      WHERE id_richiesta = p_id_richiesta;
   EXCEPTION WHEN NO_DATA_FOUND THEN
         RETURN '';
   END;
   RETURN d_valore;
END GET_UTENTE_RICHIESTA;
FUNCTION GET_GRUPPI (p_utente VARCHAR2, p_amministratore VARCHAR2)
RETURN VARCHAR2
IS
/******************************************************************************
 NOME:        GET_GRUPPI
 DESCRIZIONE: Dato un utente, ritorna i gruppi a cui appartiene dei quali
            l'amministratore ha visibilita cioe quei gruppi che hanno
           accesso ai servizi sui quali ha accesso anche l'amministratore.
           Passando come amministratore l'utente medesimo si ottengono tutti
           i gruppi cui appartiene.
 PARAMETRI:   p_utente varchar2   codice dell'utente
            p_ammministratore   codice utente dell'amministratore
 RITORNA:     varchar2: lista dei gruppi a cui l'utente appartiene separati da
                        virgola
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    16/04/2003 AO     Prima emissione.
******************************************************************************/
   d_gruppi     VARCHAR2(2000);
   BEGIN
      FOR GRUPPI IN (SELECT u.nominativo||', ' gruppo
                      FROM ad4_utenti_gruppo ug, ad4_utenti u
                     WHERE ug.utente =  p_utente
                  AND u.utente = ug.gruppo
                       AND gruppo <> 'ric_abil'
                  AND EXISTS (SELECT 1
                                   FROM ad4_diritti_accesso d1
                               , ad4_diritti_accesso d2
                              , ad4_utenti
                           WHERE d1.modulo  = d2.modulo
                             AND d1.istanza = d2.istanza
                             AND d1.utente  = ug.gruppo
                             AND d2.utente  = p_amministratore
                             AND d2.ruolo   = 'AMM'
                           )
                     ORDER BY gruppo)
      LOOP
         d_gruppi := d_gruppi||GRUPPI.gruppo;
      END LOOP;
      d_gruppi := SUBSTR(NVL(d_gruppi,', '),1,LENGTH(NVL(d_gruppi,', ')) - 2 );
      RETURN d_gruppi;
END GET_GRUPPI;
FUNCTION GET_DIRITTI_ACCESSO (
  p_utente          VARCHAR2
, p_amministratore  VARCHAR2
, p_istanza         VARCHAR2
, p_progetto        VARCHAR2
, p_profilo         VARCHAR2 DEFAULT NULL
)
RETURN VARCHAR2
IS
/******************************************************************************
 NOME:        GET_GRUPPI
 DESCRIZIONE: Dato un utente, ritorna i diritti di accesso sui moduli del progetto
            passato.
 PARAMETRI:   p_utente varchar2   codice dell'utente
            p_istanza varchar2  istanza attuale
           p_progetto varchar2
 RITORNA:     varchar2: lista dei diritti di accesso separati da virgola
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    26/06/2003 AO     Prima emissione.
******************************************************************************/
   d_diritti     VARCHAR2(2000);
   BEGIN
      FOR diritti IN (SELECT m.descrizione modulo, i.descrizione istanza, r.descrizione ruolo
                      FROM ad4_diritti_accesso d, ad4_ruoli r, ad4_moduli m, ad4_istanze i
                     WHERE d.ruolo =  r.ruolo
                  AND m.progetto = p_progetto
                  AND m.modulo = d.modulo
                  AND d.istanza = p_istanza
                  AND i.istanza = d.istanza
                  AND d.utente = p_utente
                  --and is_utente_valido (p_utente,p_amministratore,p_progetto,p_profilo) = 1
                       )
      LOOP
        IF (d_diritti IS NULL) THEN
          d_diritti := d_diritti||diritti.modulo||' - '||diritti.istanza||' ('||diritti.ruolo||')';
       ELSE
          d_diritti := d_diritti||'<br>&nbsp;'||diritti.modulo||' - '||diritti.istanza||' ('||diritti.ruolo||')';
       END IF;
      END LOOP;
      --d_diritti := substr(nvl(d_diritti,', '),1,length(nvl(d_diritti,', ')) - 2 );
      RETURN d_diritti;
END GET_DIRITTI_ACCESSO;
FUNCTION  GET_MESSAGGIO_RICHIESTA (
     p_id_richiesta IN NUMBER
) RETURN VARCHAR2 IS
d_stato     VARCHAR2(1);
d_messaggio VARCHAR2(2000);
d_email     VARCHAR2(100);
d_utente    VARCHAR2(100);
BEGIN
   SELECT stato, utente
     INTO d_stato, d_utente
     FROM ad4_richieste_abilitazione
   WHERE id_richiesta = p_id_richiesta
   ;
   SELECT ad4_soggetto.get_indirizzo_web(soggetto,'Y')
     INTO d_email
     FROM ad4_utenti_soggetti
   WHERE utente = d_utente
   ;
   IF d_stato = 'C' THEN
      d_messaggio := REPLACE(Amvweb.get_preferenza('Messaggio Registrazione Controllata'),'&mail',d_email);
     IF d_messaggio IS NULL THEN
         d_messaggio := '<br>La notifica di avvenuta abilitazione al servizio verra inviata via e-mail all''indirizzo: <b>'||d_email||'</b>';
      END IF;
   ELSIF d_stato = 'A' THEN
      d_messaggio := REPLACE(Amvweb.get_preferenza('Messaggio Registrazione Automatica'),'&mail',d_email);
     IF d_messaggio IS NULL THEN
         d_messaggio := 'Il servizio e stato abilitato.<br><br>La notifica di avvenuta abilitazione e stata inviata via e-mail all''indirizzo: <b>'||d_email||'</b>';
      END IF;
   ELSE
      d_messaggio := REPLACE(Amvweb.get_preferenza('Messaggio Registrazione Fallita'),'&mail',d_email);
     IF d_messaggio IS NULL THEN
         d_messaggio := 'Le informazioni inserite <b>non sono corrette</b> o <b>sono incomplete</b> e dovranno essere verificate.<br><br>L''eventuale notifica di avvenuta abilitazione al servizio verra inviata via e-mail all''indirizzo: <b>'||d_email||'</b>';
      END IF;
   END IF;
   RETURN d_messaggio;
END GET_MESSAGGIO_RICHIESTA;
FUNCTION IS_UTENTE_VALIDO
/******************************************************************************
 NOME:        IS_UTENTE_VALIDO.
 DESCRIZIONE: Verifica che lo utente indicato sia visibile a amministratore
 PARAMETRI:   P_UTENTE          VARCHAR2  Codice utente
              P_AMMINISTRATORE  VARCHAR2  Codice utente amministratore
           P_PROFILO         VARCHAR2  Profilo di utente amministratore sul servizio attuale
 RITORNA:     VARCHAR2  : 1 valido, 0 non valido
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    27/06/2003 AO     Prima emissione.
 1    19/04/2004 AO     Modifica per validita di utenti senza diritti di accesso
                        ma per i quali esiste richiesta in carico
******************************************************************************/
( p_utente          VARCHAR2
, p_amministratore  VARCHAR2
, p_progetto      VARCHAR2 DEFAULT NULL
, p_profilo         VARCHAR2 DEFAULT NULL
) RETURN NUMBER IS
d_return NUMBER(1);
d_tipo_utente VARCHAR2(1);
BEGIN
   BEGIN
      -- Verifico se gruppo o utente
      SELECT tipo_utente
        INTO d_tipo_utente
          FROM ad4_utenti
         WHERE utente = p_utente
      ;
      IF d_tipo_utente = 'U' THEN
         -- Utente valido se ha diritto di accesso su modulo istanza
           -- visibili ad amministratore
         SELECT DISTINCT 1
           INTO d_return
           FROM ad4_utenti u
              , ad4_diritti_accesso d1
              , ad4_moduli m
              , ad4_diritti_accesso d2
              , ad4_ruoli r
          WHERE d1.utente  = p_amministratore
            AND d2.utente  = p_utente
            AND d1.istanza = d2.istanza
            AND d1.modulo  = d2.modulo
            AND m.modulo   = d1.modulo
            AND m.progetto = NVL(p_progetto,m.progetto)
            AND r.ruolo    = d1.ruolo
            AND NVL(r.profilo,-1) = NVL(p_profilo,NVL(r.profilo,-1))
         ;
      ELSE
        -- Gruppo con Utenti che hanno diritto di accesso su un modulo del progetto
         SELECT DISTINCT 1
           INTO d_return
           FROM ad4_utenti_gruppo ug
              , ad4_diritti_accesso d
              , ad4_moduli m
          WHERE ug.gruppo  = p_utente
            AND d.utente   = ug.utente
            AND m.modulo   = d.modulo
            AND m.progetto = NVL(p_progetto,m.progetto)
         ;
      END IF;
   EXCEPTION
      WHEN NO_DATA_FOUND THEN
        BEGIN
          -- Utente valido se ha inoltrato richiesta per modulo e istanza
         -- relativi ad amministratore
            SELECT DISTINCT 1
              INTO d_return
              FROM ad4_utenti u
                 , ad4_diritti_accesso d1
                 , ad4_moduli m
                 , ad4_richieste_abilitazione r
             WHERE d1.utente  = p_amministratore
               AND r.utente  = p_utente
               AND d1.istanza = r.istanza
               AND d1.modulo  = r.modulo
               AND m.modulo   = d1.modulo
               AND m.progetto = NVL(p_progetto,m.progetto)
               AND r.stato IN ('C','F')
            ;
       EXCEPTION WHEN OTHERS THEN d_return := 0;
       END;
     WHEN OTHERS THEN d_return := 0;
   END;
   RETURN d_return;
END is_utente_valido;
FUNCTION IS_PWD_VALIDA
/******************************************************************************
 NOME:        IS_UTENTE_VALIDO.
 DESCRIZIONE: Verifica che lo utente indicato sia visibile a amministratore
 PARAMETRI:   P_UTENTE          VARCHAR2  Codice utente
              P_AMMINISTRATORE  VARCHAR2  Codice utente amministratore
           P_PROFILO         VARCHAR2  Profilo di utente amministratore sul servizio attuale
 RITORNA:     VARCHAR2  : 1 valido, 0 non valido
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    27/06/2003 AO     Prima emissione.
******************************************************************************/
( p_utente          VARCHAR2
, p_modulo          VARCHAR2
, p_istanza          VARCHAR2
, p_progetto       VARCHAR2
) RETURN NUMBER IS
   d_return NUMBER(1) := 1;
/*   d_tentativi_pwd  number(2);
   d_val_pwd        number(3);
   d_num_tentativi integer;
   d_giorni_pwd    integer;
   d_rinnovo_pwd   varchar2(2);
*/
      d_tipo_accesso       VARCHAR2(1);
      d_accesso            VARCHAR2(1);
      d_accesso_se         VARCHAR2(2);
      d_traccia            VARCHAR2(1);
      d_giorni_traccia     INTEGER;
      d_tentativi_pwd      INTEGER;
      d_val_pwd            INTEGER;
      d_err_code           INTEGER;
      d_err_msg            VARCHAR2(2000);
/*
begin
   begin
      select tentativi_password, validita_password
        into d_tentativi_pwd, d_val_pwd
        from AD4_CARATTERISTICHE_ACCESSO
      where progetto = p_progetto
        and istanza = p_istanza
       and modulo = p_modulo
       and utente = p_utente
     ;
   exception
      when others then d_return := 0;
   end;
   begin
      select sysdate - data_password, nvl(rinnovo_password,'SI'),numero_tentativi
        into d_giorni_pwd, d_rinnovo_pwd, d_num_tentativi
        from ad4_utenti
       where utente = p_utente
      ;
   exception when others then
      d_return := 0;
   end;
   return d_return;
*/
   BEGIN
      d_tipo_accesso := 'D';
      -- Legge caratteristiche effettive di accesso
      ad4_accesso.GET_CAAC_EFFETTIVE ( d_tipo_accesso, p_progetto, p_istanza, p_modulo, p_utente
                         , d_accesso, d_accesso_se, d_traccia, d_giorni_traccia
                         , d_tentativi_pwd, d_val_pwd);
      d_err_code := 0;
      d_err_msg  := '';
      BEGIN
         -- Controllo validita' password
         ad4_accesso.VALIDITA_PWD(p_utente, d_tentativi_pwd, d_val_pwd);
      EXCEPTION WHEN OTHERS THEN
         IF SQLCODE = -20999 THEN
          d_err_msg := SUBSTR(SQLERRM,12);
            RAISE_APPLICATION_ERROR(-20999,d_err_msg);
         NULL;
         ELSE
            d_err_code := SQLCODE;
            d_err_msg := SUBSTR(SQLERRM,12);
--         raise;
         END IF;
       d_return :=0;
      END;
      RETURN d_return;
END IS_PWD_VALIDA;
FUNCTION GET_PWD_MSG
/******************************************************************************
 NOME:        GET_PWD_MSG.
 DESCRIZIONE: Verifica che lo utente indicato sia visibile a amministratore
 PARAMETRI:   P_UTENTE          VARCHAR2  Codice utente
              P_AMMINISTRATORE  VARCHAR2  Codice utente amministratore
           P_PROFILO         VARCHAR2  Profilo di utente amministratore sul servizio attuale
 RITORNA:     VARCHAR2  : 1 valido, 0 non valido
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    20/01/2005 AO     Prima emissione.
******************************************************************************/
( p_utente          VARCHAR2
, p_modulo          VARCHAR2
, p_istanza          VARCHAR2
, p_progetto       VARCHAR2
) RETURN VARCHAR2 IS
   d_return VARCHAR2(2000) := '1';
      d_tipo_accesso       VARCHAR2(1);
      d_accesso            VARCHAR2(1);
      d_accesso_se         VARCHAR2(2);
      d_traccia            VARCHAR2(1);
      d_giorni_traccia     INTEGER;
      d_tentativi_pwd      INTEGER;
      d_val_pwd            INTEGER;
      d_err_code           INTEGER;
      d_err_msg            VARCHAR2(2000);
   BEGIN
      d_tipo_accesso := 'D';
      -- Legge caratteristiche effettive di accesso
      ad4_accesso.GET_CAAC_EFFETTIVE ( d_tipo_accesso, p_progetto, p_istanza, p_modulo, p_utente
                         , d_accesso, d_accesso_se, d_traccia, d_giorni_traccia
                         , d_tentativi_pwd, d_val_pwd);
      d_err_code := 0;
      d_err_msg  := '';
      BEGIN
         -- Controllo validita' password
         ad4_accesso.VALIDITA_PWD(p_utente, d_tentativi_pwd, d_val_pwd);
      EXCEPTION WHEN OTHERS THEN
         IF SQLCODE = -20999 THEN
          d_err_msg := SUBSTR(SQLERRM,12);
            RAISE_APPLICATION_ERROR(-20999,d_err_msg);
         NULL;
         ELSE
            d_err_code := SQLCODE;
            d_err_msg := SUBSTR(SQLERRM,12);
--         raise;
         END IF;
       d_return :=d_err_msg;
      END;
      RETURN d_return;
END GET_PWD_MSG;
  FUNCTION GET_SOGGETTO
/******************************************************************************
 NOME:        GET_SOGGETTO.
 DESCRIZIONE: Dato l'utente ritorna il numero individuale dell'eventuale
              registrazione anagrafica associata.
 ARGOMENTI:   p_utente:     codice dell'utente.
 ECCEZIONI:   Se il soggetto non esiste ritorna null.
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    27/06/2003 AO     Prima emissione.
******************************************************************************/
   ( p_utente IN VARCHAR2)
   RETURN NUMBER
   IS
   BEGIN
         BEGIN
            RETURN AD4_UTENTE.GET_SOGGETTO(p_utente,'Y');
         EXCEPTION WHEN OTHERS THEN
            RETURN NULL;
         END;
   END GET_SOGGETTO;
FUNCTION GET_PAGINA_ABILITATA
/******************************************************************************
 NOME:        GET_PAGINA_ABILITATE.
 DESCRIZIONE: Dato l'utente, modulo, istanza e ruolo ritorna la stringa
              con l'elenco degli url richiamabili dall'utente
 ARGOMENTI:   p_utente:     codice dell'utente.
 ECCEZIONI:   Se il soggetto non esiste ritorna null.
******************************************************************************/
( p_voce    IN VARCHAR2
, p_padre   IN NUMBER
, p_modulo  IN VARCHAR2
, p_ruolo   IN VARCHAR2
) RETURN VARCHAR2 IS
d_return VARCHAR2(4000);
BEGIN
   IF Amv_Menu.GET_ABILITAZIONE(p_voce, p_ruolo, p_modulo, p_padre) = 1 THEN
      d_return := Amv_Menu.GET_PAGE(p_voce);
   END IF;
RETURN d_return;
END GET_PAGINA_ABILITATA;
FUNCTION CREA_SOGGETTO
/******************************************************************************
 NOME:        CREA_SOGGETTO.
 DESCRIZIONE: Dato codice utente crea soggetto con i dati anagrafici passati
              con l'url della pagina corrispondente
 ARGOMENTI:   p_utente: codice utente.
 ECCEZIONI:   Ritorna 1 se creazione ha esito positivo.
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    04/08/2003 AO     Prima emissione.
 1    28/11/2003 AO     Upper di p_codice_fiscale per controllo da AD4
                      Upper di p_sesso, p_nome e p_cognome
******************************************************************************/
( p_utente          IN VARCHAR2
, p_cognome         IN VARCHAR2
, p_nome            IN VARCHAR2
, p_sesso           IN VARCHAR2
, p_comune_nas      IN NUMBER
, p_provincia_nas   IN NUMBER
, p_data_nascita    IN VARCHAR2
, p_codice_fiscale  IN VARCHAR2
) RETURN NUMBER IS
d_nome     VARCHAR2(100);
d_soggetto NUMBER;
d_utente   VARCHAR2(8) := p_utente;
d_codice_fiscale VARCHAR2(16) := UPPER(p_codice_fiscale);
d_sesso VARCHAR2(1) := UPPER(p_sesso);
BEGIN
      /*-----------------------------------------------------
                   Verifica parametri obbligatori
      -----------------------------------------------------*/
      IF p_cognome IS NULL THEN
         RAISE_APPLICATION_ERROR(-20999,'Cognome Utente obbligatorio.');
      END IF;
      IF p_nome IS NULL THEN
         RAISE_APPLICATION_ERROR(-20999,'Nome Utente obbligatorio.');
      END IF;
      IF p_sesso IS NULL THEN
         RAISE_APPLICATION_ERROR(-20999,'Sesso obbligatorio.');
      END IF;
      IF p_comune_nas IS NULL THEN
         RAISE_APPLICATION_ERROR(-20999,'Comune di nascita utente obbligatorio.');
      END IF;
      IF p_provincia_nas IS NULL THEN
         RAISE_APPLICATION_ERROR(-20999,'Provincia di nascita utente obbligatoria.');
      END IF;
      IF p_data_nascita IS NULL THEN
         RAISE_APPLICATION_ERROR(-20999,'Data di nascita utente obbligatoria.');
      END IF;
      IF p_codice_fiscale IS NULL THEN
         RAISE_APPLICATION_ERROR(-20999,'Codice fiscale obbligatorio.');
      END IF;
      /*-----------------------------------------------------
                   Controllo del codice fiscale
      -----------------------------------------------------*/
      DECLARE
         derror NUMBER;
         dmsg   VARCHAR(2000);
      BEGIN
         derror := ad4_codice_fiscale.controllo(d_codice_fiscale, d_sesso, p_comune_nas, p_provincia_nas, p_data_nascita);
         IF derror < 0 THEN
            dmsg := ad4_codice_fiscale.get_error_msg(derror);
            RAISE_APPLICATION_ERROR(-20900 + derror, dmsg);
         END IF;
      EXCEPTION WHEN OTHERS THEN
         RAISE;
      END;
     BEGIN
           ad4_utente.initialize(p_utente);
           d_soggetto := ad4_utente.get_soggetto(p_utente);
           IF d_soggetto IS NULL THEN
             ad4_soggetto.initialize(d_soggetto);
            d_nome := UPPER(p_cognome)||'  '||UPPER(p_nome);
            ad4_soggetto.set_nome(d_nome);
            ad4_soggetto.set_codice_fiscale(d_codice_fiscale);
            ad4_soggetto.set_comune_nas(p_comune_nas);
            ad4_soggetto.set_provincia_nas(p_provincia_nas);
            ad4_soggetto.set_data_nascita(p_data_nascita);
            ad4_soggetto.set_sesso(d_sesso);
             ad4_soggetto.update_soggetto(d_soggetto);
             ad4_utente.set_soggetto(d_soggetto);
             ad4_utente.update_utente(d_utente,'T');
           END IF;
      EXCEPTION WHEN OTHERS THEN
         RAISE;
      END;
     RETURN 1;
END CREA_SOGGETTO;
FUNCTION GET_DIRITTO_DOCUMENTI (
  p_utente          VARCHAR2
)
RETURN VARCHAR2
IS
/******************************************************************************
 NOME:        GET_GRUPPI
 DESCRIZIONE: Dato un utente, ritorna i diritti di accesso sui moduli del progetto
            passato.
 PARAMETRI:   p_utente varchar2   codice dell'utente
            p_istanza varchar2  istanza attuale
           p_progetto varchar2
 RITORNA:     varchar2: lista dei diritti di accesso separati da virgola
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    26/08/2004 AO     Prima emissione.
******************************************************************************/
d_diritto_return  VARCHAR2(1);
BEGIN
      FOR c IN (
        SELECT Amv_Area.get_diritto_utente(p_utente, id_area) diritto
           FROM AMV_AREE
     )
      LOOP
         BEGIN
         IF c.diritto = 'U' THEN
            d_diritto_return := c.diritto;
         ELSIF c.diritto = 'A' AND NVL(d_diritto_return,' ') != 'U' THEN
           d_diritto_return := c.diritto;
         ELSIF c.diritto = 'V' AND NVL(d_diritto_return,' ') NOT IN ('A','U') THEN
            d_diritto_return := c.diritto;
         ELSIF c.diritto = 'C' AND NVL(d_diritto_return,' ') NOT IN ('A','V', 'U') THEN
            d_diritto_return := c.diritto;
         ELSIF c.diritto = 'R' AND NVL(d_diritto_return,' ') NOT IN ('A','V', 'U','C') THEN
            d_diritto_return := c.diritto;
         END IF;
         END;
      END LOOP;
      RETURN d_diritto_return;
END GET_DIRITTO_DOCUMENTI;
END Amv_Utente;
/

