CREATE OR REPLACE PACKAGE "SI4_COMPETENZA" IS /* MASTER_LINK */
/******************************************************************************
 NOME:        SI4_COMPETENZA
 DESCRIZIONE: Package di gestione competenze sugli oggetti  di database
 ANNOTAZIONI: Versione V1.8
 REVISIONI:
 Rev.  Data        Autore  Descrizione
 ----  ----------  ------  ----------------------------------------------------
 0     __/__/____  VA      Creazione.
 1    21/01/2005   VA     Gestione competenze ricorsive sui gruppi
 2    01/02/2005   VA     Introddotta Function Oggetti
 3    25/01/2006   VA     Introdotta competenza Funzionale
 4    12/10/2006   VA     Introdotta Function Assegna_multipla
 5    09/11/2006   VA     Introdotta Function open_comp_cv, get_competenze
 6    28/11/2006   VA     Introdotte Function sposta, apri, chiudi
 7    12/01/2009   VA     Modifiche varie
 8    11/03/2009   VA     Corretto bug competenze duplicate
 9    25/02/2010   VA     Introdotta gestione deleghe.
 ******************************************************************************/
revisione VARCHAR2(30) := 'V1.9';
g_diritto NUMBER(1);
g_delegante number(1) := 0; /* 0 = controllo l'utente; 1 = coltrollo il delegante*/
--TYPE ComRecTyp IS RECORD ( grantor      SI4_COMPETENZE.utente_aggiornamento%TYPE
--                         , DATA         SI4_COMPETENZE.data_aggiornamento%TYPE
--                         , abilitazione SI4_TIPI_ABILITAZIONE.tipo_abilitazione%TYPE);
TYPE CompCurTyp IS REF CURSOR; -- RETURN ComRecTyp;
--TYPE DelRecTyp IS RECORD ( tipo_oggetto      SI4_TIPI_OGGETTO.tipo_oggetto%TYPE
--                         , descrizione       SI4_TIPI_OGGETTO.descrizione%TYPE
--                         , oggetto           SI4_COMPETENZE.oggetto%TYPE
--                         , tipo_abilitazione SI4_TIPI_ABILITAZIONE.tipo_abilitazione%TYPE
--                         , utente            SI4_COMPETENZE.utente%TYPE
--                         , ruolo             SI4_COMPETENZE.ruolo%TYPE
--                         , dal               SI4_COMPETENZE.dal%TYPE
--                         , al                SI4_COMPETENZE.al%TYPE
--                         , autore            SI4_COMPETENZE.utente_aggiornamento%TYPE);
TYPE DelCurTyp IS REF CURSOR; -- RETURN DelRecTyp;
FUNCTION  VERSIONE
/******************************************************************************
 NOME:        VERSIONE
 DESCRIZIONE: Restituisce la versione e la data di distribuzione del package.
 RITORNA:     stringa VARCHAR2 contenente versione e data.
 NOTE:        Il secondo numero della versione corrisponde alla revisione
              del package.
******************************************************************************/
RETURN VARCHAR2;
PRAGMA RESTRICT_REFERENCES(VERSIONE, WNDS, WNPS);
PROCEDURE SET_TRUE;
/******************************************************************************
 NOME:        SET_TRUE
 DESCRIZIONE: Setta la variabile globale g_diritto a 1.
******************************************************************************/
PROCEDURE SET_FALSE;
/******************************************************************************
 NOME:        SET_FALSE
 DESCRIZIONE: Setta la variabile globale g_diritto a 0
******************************************************************************/
FUNCTION assegna
/******************************************************************************
 NOME:        assegna
 DESCRIZIONE: Registra la competenza per il soggetto (utente o gruppo) indicato, relativamente alla abilitazione di un determinato oggetto.
              - chiude periodo precedente alla data DAL - 1
              - sposta periodo successivo alla data AL+ 1
              - elimina periodi contenuti
              Return:
               0 : Inserimento corretto
              -1 : Tipo di abilitazione incompatibile con l'oggetto indicato
              -2 : Non esiste l'oggetto
              -3 : Non esiste il tipo di abilitazione
******************************************************************************/
( p_Tipo_Oggetto IN VARCHAR2
, p_Oggetto IN VARCHAR2
, p_Tipo_Abilitazione IN VARCHAR2
, p_Utente IN VARCHAR2
, p_Ruolo IN VARCHAR2 DEFAULT NULL
, p_Autore IN VARCHAR2
, p_Accesso IN VARCHAR2 DEFAULT 'S'
, p_Dal IN VARCHAR2 DEFAULT NULL
, p_Al IN VARCHAR2 DEFAULT NULL
, p_funzione IN VARCHAR2 DEFAULT NULL
, p_controllo IN VARCHAR2 DEFAULT 1)
RETURN NUMBER;
FUNCTION assegna_multipla
/******************************************************************************
 NOME:        assegna_multipla
 DESCRIZIONE: Registra la competenza per il soggetto (utente o gruppo) indicato, relativamente alle abilitazioni
                passate, separate da ";" di un determinato oggetto.
              - chiude periodo precedente alla data DAL - 1
              - sposta periodo successivo alla data AL+ 1
              - elimina periodi contenuti
              Return:
               0 : Inserimento corretto
              -1 : Tipo di abilitazione incompatibile con l'oggetto indicato
              -2 : Non esiste l'oggetto
              -3 : Non esiste il tipo di abilitazione
******************************************************************************/
( p_Tipo_Oggetto IN VARCHAR2
, p_Oggetto IN VARCHAR2
, p_Tipo_Abilitazione IN VARCHAR2
, p_Utente IN VARCHAR2
, p_Ruolo IN VARCHAR2 DEFAULT NULL
, p_Autore IN VARCHAR2
, p_Accesso IN VARCHAR2 DEFAULT 'S'
, p_Dal IN VARCHAR2 DEFAULT NULL
, p_Al IN VARCHAR2 DEFAULT NULL
, p_funzione IN VARCHAR2 DEFAULT NULL
)
RETURN NUMBER;
FUNCTION verifica_funzionale
/******************************************************************************
 NOME:        verifica_funzionale
 DESCRIZIONE: Verifica l'abilitazione di accesso ad una certa data restituendo il valore 1 o 0 in funzione del contenuto nel campo ACCESSO.
              Ritorna:
              1 : se esiste il diritto di accesso
              0 : se non esiste diritto di accesso
******************************************************************************/
( p_Null_Gestito IN NUMBER
, p_Tipo_Oggetto IN VARCHAR2
, p_Oggetto IN VARCHAR2
, p_Tipo_Abilitazione IN VARCHAR2
, p_Utente IN VARCHAR2
, p_Ruolo IN VARCHAR2 DEFAULT NULL
, p_Data IN VARCHAR2 DEFAULT TO_CHAR(SYSDATE,'dd/mm/yyyy'))
RETURN NUMBER;
FUNCTION verifica
/******************************************************************************
 NOME:        verifica
 DESCRIZIONE: Verifica l'abilitazione di accesso ad una certa data restituendo il valore 1 o 0 in funzione del contenuto nel campo ACCESSO.
              Controlla prima l'accesso specifico per il Soggetto e poi per appartenenza ai diversi Gruppi.
              Se il ruolo e indicato in ingresso controlla l'esistenza di una competenza con quel ruolo o in assenza di questa, senza ruolo.
              Se il ruolo non e indicato in ingresso, per ogni gruppo acquisisce il Ruolo del Soggetto nel Gruppo
              Ritorna:
              1 : se esiste il diritto di accesso
              0 : se non esiste diritto di accesso
******************************************************************************/
( p_Tipo_Oggetto IN VARCHAR2
, p_Oggetto IN VARCHAR2
, p_Tipo_Abilitazione IN VARCHAR2
, p_Utente IN VARCHAR2
, p_Ruolo IN VARCHAR2 DEFAULT NULL
, p_Data IN VARCHAR2 DEFAULT TO_CHAR(SYSDATE,'dd/mm/yyyy'))
RETURN NUMBER;
FUNCTION verifica
/******************************************************************************
 NOME:        verifica
 DESCRIZIONE: Verifica l'abilitazione di accesso ad una certa data restituendo il valore 1 o 0 in funzione del contenuto nel campo ACCESSO.
              Controlla prima l'accesso specifico per il Soggetto e poi per appartenenza ai diversi Gruppi.
              Se il ruolo e indicato in ingresso controlla l'esistenza di una competenza con quel ruolo o in assenza di questa, senza ruolo.
              Se il ruolo non e indicato in ingresso, per ogni gruppo acquisisce il Ruolo del Soggetto nel Gruppo
              Ritorna:
              1 : se esiste il diritto di accesso
              0 : se non esiste diritto di accesso
******************************************************************************/
( p_Null_Gestito IN NUMBER
, p_Tipo_Oggetto IN VARCHAR2
, p_Oggetto IN VARCHAR2
, p_Tipo_Abilitazione IN VARCHAR2
, p_Utente IN VARCHAR2
, p_Ruolo IN VARCHAR2 DEFAULT NULL
, p_Data IN VARCHAR2 DEFAULT TO_CHAR(SYSDATE,'dd/mm/yyyy')
, p_chk_funz IN NUMBER DEFAULT 1
)
RETURN NUMBER;
FUNCTION get_tipo_abilitazione
/******************************************************************************
 NOME:        get_tipo_abilitazione
 DESCRIZIONE: Ritorna la descrizione della abilitazione richiesta.
              NULL : se l'abilitazione non esiste
******************************************************************************/
( p_tipo_abilitazione IN VARCHAR2)
RETURN VARCHAR2;
FUNCTION oggetti
/******************************************************************************
 NOME:        oggetti
 DESCRIZIONE: Ritorna una stringa con gli oggetti su cui l'utente
              ha una qualche competenza separati da un separatore parametrizzabile:
******************************************************************************/
( p_Utente IN VARCHAR2
, p_Tipo_Abilitazione IN VARCHAR2 DEFAULT '%'
, p_Ruolo IN VARCHAR2 DEFAULT NULL
, p_Tipo_Oggetto IN VARCHAR2 DEFAULT '%'
, p_Oggetto IN VARCHAR2 DEFAULT'%'
, p_Data IN VARCHAR2 DEFAULT TO_CHAR(SYSDATE,'dd/mm/yyyy')
, p_separatore IN VARCHAR2 DEFAULT ';'
)
RETURN VARCHAR2;
FUNCTION open_comp_cv
/******************************************************************************
 NOME:        open_comp_cv
 DESCRIZIONE: Dato un utente e un oggetto, ritorna le informazioni su chi ha dato
              l'abilitazione, quando ed il tipo di abilitazione.
 RITORNA:  REF CURSOR con l'utente che ha assegnato la competenza,
           la data di assegnazione e il tipo di competenza:
******************************************************************************/
( p_Utente IN VARCHAR2
, p_oggetto IN VARCHAR2)
 RETURN CompCurTyp;
FUNCTION get_competenze
/******************************************************************************
 NOME:        get_competenze
 DESCRIZIONE: Dato un utente ed un oggetto ritorna una stringa con le competenze
              separati da un separatore parametrizzabile:
 RITORNA:  Stringa
******************************************************************************/
( p_Utente IN VARCHAR2
, p_oggetto IN VARCHAR2
, p_separatore IN VARCHAR2 DEFAULT ';')
 RETURN VARCHAR2;
PROCEDURE chiudi
/******************************************************************************
 NOME:        CHIUDI
 DESCRIZIONE: Chiude le registrazioni di competenza previste per p_utente alla
              data prevista in parametro.
              Se il p_tipo_oggetto non ¿ indicato opera su tutti gli oggetti.
              Se la p_data non ¿ indicata le chiude alla data di oggi.
 NOTE:        .
******************************************************************************/
( p_utente       IN VARCHAR2
, p_tipo_oggetto IN VARCHAR2 DEFAULT NULL
, p_data         IN VARCHAR2 DEFAULT TO_CHAR(SYSDATE,'dd/mm/yyyy')
, p_oggetto      IN VARCHAR2 DEFAULT NULL
, p_tipo_abilitazione IN VARCHAR2 DEFAULT NULL
, p_data_al      IN VARCHAR2 DEFAULT NULL);
PROCEDURE apri
/******************************************************************************
 NOME:        APRI
 DESCRIZIONE: Apre una nuova registrazione con le caratteristiche previste per le
              competenze relative a p_da_utente.
              Se il p_tipo_oggetto non ¿ indicato opera su tutti gli oggetti.
              Se la p_data non ¿ indicata assume la data di oggi.
 NOTE:        .
******************************************************************************/
( p_da_utente       IN VARCHAR2
, p_a_utente        IN VARCHAR2
, p_tipo_oggetto    IN VARCHAR2 DEFAULT NULL
, p_data            IN VARCHAR2 DEFAULT TO_CHAR(SYSDATE,'dd/mm/yyyy'));
PROCEDURE sposta
/******************************************************************************
 NOME:        SPOSTA
 DESCRIZIONE: Sposta le competenze da un utente ad un altro utente per il tipo_oggetto
              indicato, chiudendo la competenza di provenienza alla data p_data-1 e
              aprendo la nuova competenza alla data p_data.
              Se il p_tipo_oggetto non ¿ indicato opera su tutti gli oggetti.
              Se la p_data non ¿ indicata assume la data di oggi.
              Se come p_data viene passato NULL agisce in modalit¿ UPDATE, modificando
              direttamente le competenze presenti da p_da_utente a p_a_utente.
 NOTE:        .
******************************************************************************/
( p_da_utente       IN VARCHAR2
, p_a_utente        IN VARCHAR2
, p_tipo_oggetto    IN VARCHAR2 DEFAULT NULL
, p_data            IN VARCHAR2 DEFAULT TO_CHAR(SYSDATE,'dd/mm/yyyy'));
FUNCTION get_abilitazioni
/******************************************************************************
 NOME:        get_abilitazioni
 DESCRIZIONE: La funzione restituisce un REF_CURSOR contenente l'elenco delle
              abilitazioni che rispondono alle caratteristiche richieste.
 RITORNA:  REF_CURSOR: DelCurTyp
******************************************************************************/
( p_tipo_oggetto      IN SI4_TIPI_OGGETTO.tipo_oggetto%TYPE
, p_oggetto           IN SI4_COMPETENZE.oggetto%TYPE
, p_tipo_abilitazione IN SI4_TIPI_ABILITAZIONE.tipo_abilitazione%TYPE
, p_utente            IN SI4_COMPETENZE.utente%TYPE
, p_data              IN varchar2 default null)
 RETURN DelCurTyp;
END Si4_Competenza;
/

