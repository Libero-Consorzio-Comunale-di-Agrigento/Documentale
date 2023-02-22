CREATE OR REPLACE PACKAGE Amv_Utente AS
/******************************************************************************
 NOME:        AMV_UTENTE
 DESCRIZIONE: Gestione utenti
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    20/01/2003 AO     Prima emissione.
 1    05/04/2003 AO      Revisione funzionalita per registrazione nuovo utente
                      Funzionalita di modifica password
 2    27/05/2003 AO      Aggiunta chiamata a procedure specifica in approvazione
                      o respinta di una richiesta di abilitazione
 3    27/06/2003 AO      Aggiunte funzioni IS_UTENTE_VALIDO e GET_SOGGETTO.
 4    28/07/2003 AO      Aggiunte funzioni GET_PAGINA_ABILITATA.
 5    04/08/2003 AO      Aggiunta funzione CREA_SOGGETTO.
 6    28/11/2003 AO     Modifiche a funzione CREA_SOGGETTO per controllo corretto
                      codice fiscale.
 7    19/04/2004 AO     Modifica su IS_UTENTE_VALIDO per gestire utenti non abilitati
                        ma che abbiano fatto richiesta di abilitazione al servizio
 8    29/12/2004 AO     Modifica su MODIFICA_PASSWORD per integrazione LDAP
 9    20/01/2005 AO     Modifica su MODIFICA_PASSWORD per setting utente aggiornamento
 10   27/05/2005 AO     Modifica su GESTISCI_RICHIESTA per note aggiuntive mail notifica
******************************************************************************/
d_versione  VARCHAR2(20) := 'V2005.05';
d_revisione VARCHAR2(30) := '12   27/05/2005';
FUNCTION VERSIONE RETURN VARCHAR2;
PROCEDURE REGISTRA_UTENTE
/******************************************************************************
 NOME:        REGISTRA_UTENTE
 DESCRIZIONE: Procedure utilizzata da web: lancia la procedure di registrazione di un
            nuovo utente.
 PARAMETRI:   p_utente          VARCHAR2      codice utente assegnato
           p_mezza_password  VARCHAR2      prima parte della password assegnata al nuovo utente.
 ECCEZIONI:
******************************************************************************/
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
);
PROCEDURE  AGGIORNA_UTENTE
/******************************************************************************
 NOME:        AGGIORNA_UTENTE
 DESCRIZIONE: Procedure utilizzata da web: lancia aggiornamento dei dati dello utente, lancia una richiesta
            di abilitazione ad un servizio e, se il servizio non prevede parametri aggiuntivi, lancia
           la gestione dello stato finale ('C' oppure 'A') della richiesta.
 PARAMETRI:   p_richiesta    NUMBER         identificativo assegnato alla richiesta
            p_utente       VARCHAR2      codice utente che sta richiedendo ablitazione al servizio.
           p_modulo       VARCHAR2      modulo.
           p_istanza      VARCHAR2      istanza.
 ECCEZIONI:
******************************************************************************/
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
) ;
PROCEDURE  REGISTRA_RICHIESTA
/******************************************************************************
 NOME:        REGISTRA_RICHIESTA
 DESCRIZIONE: Crea una richiesta di abilitazione relativa allo utente sul servizio
            (modulo + istanza) passato.
 PARAMETRI:   p_richiesta    NUMBER         identificativo assegnato alla richiesta
            p_utente       VARCHAR2      codice utente che sta richiedendo ablitazione al servizio.
           p_modulo       VARCHAR2      modulo.
           p_istanza      VARCHAR2      istanza.
 ECCEZIONI:
******************************************************************************/
( p_richiesta    IN OUT VARCHAR2
, p_utente       IN VARCHAR2
, p_nuovo_utente IN VARCHAR2
, p_modulo       IN VARCHAR2
, p_istanza      IN VARCHAR2
) ;
PROCEDURE  GESTISCI_RICHIESTA
/******************************************************************************
 NOME:        GESTISCI_RICHIESTA
 DESCRIZIONE: Imposta lo stato della richiesta a 'C' (da convalidare) oppure 'A' (approvata) dopo aver
            verificato se esiste una procedure di validazione specifica per il servizio richiesto.
 PARAMETRI:   p_id_richiesta NUMBER         identificativo della richiesta
 ECCEZIONI:
******************************************************************************/
( p_richiesta      IN NUMBER
, p_tipo_convalida IN VARCHAR2 DEFAULT 'A'
) ;
PROCEDURE  SET_PARAMETRO_RICHIESTA
/******************************************************************************
 NOME:        SET_PARAMETRO_RICHIESTA
 DESCRIZIONE: Imposta il generico parametro della richiesta di abilitazione ad un servizio.
 PARAMETRI:   p_id_richiesta NUMBER         identificativo della richiesta
            p_nome         VARCHAR2      nome che si vuole assegnare al parametro.
           p_valore       VARCHAR2      valore che si vuole assegnare al parametro.
           p_utente       VARCHAR2      stringa che identifica l'utente di aggiornamento del record
                                    (non ha vincoli di FK con tabella AD4_UTENTI).
 ECCEZIONI:   nnnnn, <Descrizione eccezione>
******************************************************************************/
( p_id_richiesta IN NUMBER
, p_nome         IN VARCHAR2
, p_valore       IN VARCHAR2
, p_utente       IN VARCHAR2
) ;
FUNCTION  GET_PARAMETRO_RICHIESTA
( p_id_richiesta IN NUMBER
, p_nome         IN VARCHAR2
) RETURN VARCHAR2;
PROCEDURE  SET_PARAMETRI_AZIENDA
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
******************************************************************************/
( p_id_richiesta IN NUMBER
, p_rs           IN VARCHAR2
, p_rs_valore    IN VARCHAR2
, p_cf           IN VARCHAR2
, p_cf_valore    IN VARCHAR2
, p_utente       IN VARCHAR2
) ;
PROCEDURE  MODIFICA_PASSWORD
/******************************************************************************
 NOME:        MODIFICA_PASSWORD
 DESCRIZIONE: Aggiorna la password dell'utente
 PARAMETRI:   p_utente            VARCHAR2      codice utente
           p_nuova_password    VARCHAR2      in chiaro
           p_conferma_password VARCHAR2      in chiaro
 ECCEZIONI:   Messaggio nel caso in cui utente non abilitato al rinnovo password
******************************************************************************/
( p_nuova_password    IN VARCHAR2
, p_conferma_password IN VARCHAR2
, p_utente           IN VARCHAR2
) ;
PROCEDURE  MODIFICA_PASSWORD
/******************************************************************************
 NOME:        MODIFICA_PASSWORD
 DESCRIZIONE: Aggiorna la password dell'utente
 PARAMETRI:   p_utente            VARCHAR2      codice utente
              p_password_attuale  VARCHAR2      in chiaro
           p_nuova_password    VARCHAR2      in chiaro
           p_conferma_password VARCHAR2      in chiaro
 ECCEZIONI:   Messaggio nel caso in cui utente non abilitato al rinnovo password
******************************************************************************/
( p_password_attuale  IN VARCHAR2
, p_nuova_password    IN VARCHAR2
, p_conferma_password IN VARCHAR2
, p_utente           IN VARCHAR2
) ;
PROCEDURE  RECUPERA_LOGIN
/******************************************************************************
 NOME:        RECUPERA_LOGIN
 DESCRIZIONE: Recupera lo username e la password dell'utente
 PARAMETRI:   p_nome              VARCHAR2      nome soggetto
              p_cognome           VARCHAR2      cognome soggetto
           p_email             VARCHAR2      email soggetto
 ECCEZIONI:   Messaggio nel caso in cui utente non abilitato al rinnovo password
******************************************************************************/
( p_nome              IN VARCHAR2
, p_cognome           IN VARCHAR2
, p_email             IN VARCHAR2
, p_modulo            IN VARCHAR2
, p_istanza            IN VARCHAR2
, p_progetto         IN VARCHAR2
, p_link              IN VARCHAR2
) ;
PROCEDURE  AGGIORNA
/******************************************************************************
 NOME:        AGGIORNA
 DESCRIZIONE: Aggiorna i dati dell'utente sulla anagrafica dei soggetti
 PARAMETRI:   p_utente        VARCHAR2      codice utente
            p_nuovo_utente  VARCHAR2      codice utente per nuove registrazioni
******************************************************************************/
( p_utente    IN VARCHAR2
, p_nuovo_utente  IN  VARCHAR2
, p_comune    IN  NUMBER
, p_provincia IN  NUMBER
, p_cap       IN VARCHAR2
, p_via       IN VARCHAR2
, p_indirizzo IN VARCHAR2
, p_num       IN VARCHAR2
, p_indirizzo_web IN VARCHAR2
, p_telefono  IN VARCHAR2
, p_fax       IN VARCHAR2
) ;
PROCEDURE  SET_GRUPPO
/******************************************************************************
 NOME:        SET_GRUPPO
 DESCRIZIONE: Associa l'utente al gruppo
 PARAMETRI:   p_utente    VARCHAR2      codice utente
              p_gruppo    VARCHAR2      codice gruppo
******************************************************************************/
( p_utente    IN VARCHAR2
, p_gruppo    IN VARCHAR2
) ;
FUNCTION  GET_UTENTE_RICHIESTA
/******************************************************************************
 NOME:        GET_UTENTE_RICHIESTA
 DESCRIZIONE: Restituisce l'utente associato ad una richiesta
 PARAMETRI:   p_id_richiesta  NUMBER identificativo della richiesta
******************************************************************************/
( p_id_richiesta IN NUMBER
) RETURN VARCHAR2;
FUNCTION GET_GRUPPI
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
******************************************************************************/
( p_utente VARCHAR2
, p_amministratore VARCHAR2)
RETURN VARCHAR2;
FUNCTION GET_DIRITTI_ACCESSO
/******************************************************************************
 NOME:        GET_GRUPPI
 DESCRIZIONE: Dato un utente, ritorna i diritti di accesso sui moduli del progetto
            passato.
 PARAMETRI:   p_utente varchar2   codice dell'utente
            p_istanza varchar2  istanza attuale
           p_progetto varchar2
 RITORNA:     varchar2: lista dei diritti di accesso separati da virgola
******************************************************************************/
( p_utente          VARCHAR2
, p_amministratore  VARCHAR2
, p_istanza         VARCHAR2
, p_progetto        VARCHAR2
, p_profilo         VARCHAR2 DEFAULT NULL
) RETURN VARCHAR2;
FUNCTION  GET_MESSAGGIO_RICHIESTA
( p_id_richiesta IN NUMBER
) RETURN VARCHAR2;
FUNCTION IS_UTENTE_VALIDO
/***********************************************************************************************
 NOME:        IS_UTENTE_VALIDO.
 DESCRIZIONE: Verifica che lo utente indicato sia visibile a amministratore
 PARAMETRI:   P_UTENTE          VARCHAR2  Codice utente
              P_AMMINISTRATORE  VARCHAR2  Codice utente amministratore
           P_PROFILO         VARCHAR2  Profilo di utente amministratore sul servizio attuale
 RITORNA:     VARCHAR2  : 1 valido, 0 non valido
************************************************************************************************/
( p_utente          VARCHAR2
, p_amministratore  VARCHAR2
, p_progetto      VARCHAR2 DEFAULT NULL
, p_profilo         VARCHAR2 DEFAULT NULL
) RETURN NUMBER;
FUNCTION IS_PWD_VALIDA
/***********************************************************************************************
 NOME:        IS_PWD_VALIDA.
 DESCRIZIONE: Verifica che la password dello utente indicato sia valida
 PARAMETRI:   P_UTENTE          VARCHAR2  Codice utente
              P_MODULO          VARCHAR2  Codice modulo AD4
           P_ISTANZA         VARCHAR2  Codice istanza AD4
           P_PROGETTO      VARCHAR2  Codice progetto AD4
 RITORNA:     NUMBER  : 0 oppure 1
************************************************************************************************/
( p_utente          VARCHAR2
, p_modulo          VARCHAR2
, p_istanza          VARCHAR2
, p_progetto       VARCHAR2
) RETURN NUMBER;
FUNCTION GET_PWD_MSG
/***********************************************************************************************
 NOME:        GET_PWD_MSG.
 DESCRIZIONE: Restituisce messaggio sulla validita password
 PARAMETRI:   P_UTENTE          VARCHAR2  Codice utente
              P_MODULO          VARCHAR2  Codice modulo AD4
           P_ISTANZA         VARCHAR2  Codice istanza AD4
           P_PROGETTO      VARCHAR2  Codice progetto AD4
 RITORNA:     VARCHAR2  : messaggio sulla validita password
************************************************************************************************/
( p_utente          VARCHAR2
, p_modulo          VARCHAR2
, p_istanza          VARCHAR2
, p_progetto       VARCHAR2
) RETURN VARCHAR2;
FUNCTION GET_SOGGETTO
/******************************************************************************
 NOME:        GET_SOGGETTO.
 DESCRIZIONE: Dato l'utente ritorna il numero individuale dell'eventuale
              registrazione anagrafica associata.
 ARGOMENTI:   p_utente:     codice dell'utente.
 ECCEZIONI:   Se il soggetto non esiste ritorna null.
******************************************************************************/
( p_utente IN VARCHAR2) RETURN NUMBER;
FUNCTION GET_PAGINA_ABILITATA
/******************************************************************************
 NOME:        GET_PAGINA_ABILITATA.
 DESCRIZIONE: Data voce, padre, modulo e ruolo ritorna la stringa
              con l'url della pagina corrispondente
 ARGOMENTI:   p_voce:   codice della voce.
            p_padre:  codice del padre
            p_modulo: codice modulo attuale
            p_ruolo:  ruolo per il quale si verifica la abilitazione
 ECCEZIONI:   Se la pagina non e abilitata ritorna null.
******************************************************************************/
( p_voce    IN VARCHAR2
, p_padre   IN NUMBER
, p_modulo  IN VARCHAR2
, p_ruolo   IN VARCHAR2
) RETURN VARCHAR2;
FUNCTION CREA_SOGGETTO
/******************************************************************************
 NOME:        CREA_SOGGETTO.
 DESCRIZIONE: Dato codice utente crea soggetto con i dati anagrafici passati
              con l'url della pagina corrispondente
 ARGOMENTI:   p_utente: codice utente.
 ECCEZIONI:   Ritorna 1 se creazione ha esito positivo.
******************************************************************************/
( p_utente          IN VARCHAR2
, p_cognome         IN VARCHAR2
, p_nome            IN VARCHAR2
, p_sesso           IN VARCHAR2
, p_comune_nas      IN NUMBER
, p_provincia_nas   IN NUMBER
, p_data_nascita    IN VARCHAR2
, p_codice_fiscale  IN VARCHAR2
) RETURN NUMBER;
FUNCTION GET_DIRITTO_DOCUMENTI
/******************************************************************************
 NOME:        GET_DIRITTO_DOCUMENTI
 DESCRIZIONE: Dato un utente, ritorna il massimo diritto sulle aree di documenti
 PARAMETRI:   p_utente varchar2   codice dell'utente
 RITORNA:     varchar2: diritto di accesso
              (U aggiornamento
           ,A approvazione
           ,V verifica
           ,C creazione
           ,R lettura)
******************************************************************************/
( p_utente VARCHAR2) RETURN VARCHAR2;
END Amv_Utente;
/

