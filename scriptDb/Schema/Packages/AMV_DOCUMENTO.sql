CREATE OR REPLACE PACKAGE Amv_Documento AS
/****************************************************************************************
 DESCRIZIONE: Gestione tabelle AMV_DOCUMENTI e connesse
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    16/10/2002 __     Prima emissione.
 1    27/11/2002 AO     Gestione CLOB
 2    13/02/2003 AO     Aggiunte funzioni di gestione attributi.
 3     09/04/2003 AO     Adeguamento ai nuovi fogli di stile
 4     16/06/2003 AO     Aggiunto parametro p_stile alle function get_link_documenti
 5    11/07/2003 AO     Aggiunta function get_gruppi_area, modificata get_link_documenti
 6    14/07/2003 AO     Aggiunto parametro p_id_documento alla function crea_link_documenti
 7    18/11/2003 AO     Modifica get_link_documenti per gestione link tipo http://
 8    11/03/2004 AO     Aggiunta function get_allegati
 9    30/03/2004 AO     Modificate get_diritto, get_diritto_area e get_diritto_doc per
                        gestione diritti A e V (approvazione e verifica)
 10   06/04/2004 AO     Modificata get_blocco_documenti utilizzo classi di stile per i blocchi
                        di sinistra   e destra
 11   16/11/2004 AO     Revisione per versione 2004.09
 12   27/05/2005 AO     Revisione per versione 2005.05
 13   27/05/2005 AO     Modificata get_allegati
******************************************************************************************/
d_versione  VARCHAR2(20) := 'V2005.05';
d_revisione VARCHAR2(30) := '13   27/05/2005';
FUNCTION VERSIONE RETURN VARCHAR2;
-- Metodi relativi ai documenti
FUNCTION GET_DIRITTO (
  P_GRUPPO       VARCHAR2
, P_ID_AREA      NUMBER
, P_ID_TIPOLOGIA NUMBER ) RETURN VARCHAR2;
/******************************************************************************
 DESCRIZIONE: Riceve codice gruppo e area (di documenti) di accesso;
              Dovrebbe ritornare il diritto di Accesso piu alto per il gruppo interessato
              ritrovato su tabella AMV_DIRITTI per l'area relativa al documento indicato ('R' op 'W').
 PARAMETRI:   P_GRUPPO       VARCHAR2    Codice del gruppo
              P_ID_AREA      NUMBER      Codice dell'area
              P_ID_TIPOLOGIA NUMBER      Codice della tipologia
 RITORNA:     VARCHAR2       tipo di diritto, Read, Create, Update
******************************************************************************/
FUNCTION GET_DIRITTO_AREA ( P_UTENTE VARCHAR2, P_ID_AREA NUMBER, P_ID_TIPOLOGIA NUMBER) RETURN VARCHAR2;
/******************************************************************************
 DESCRIZIONE: Riceve codice utente, area di accesso e tipologia (di documenti) ;
              Dovrebbe ritornare il diritto di Accesso piu alto per il gruppo interessato
              ritrovato su tabella AMV_DIRITTI per l'area e tipologia indicati ('R','C' o 'U').
 PARAMETRI:   P_UTENTE       VARCHAR2    Codice del gruppo
              P_ID_AREA      NUMBER      Codice dell'area
              P_ID_TIPOLOGIA NUMBER      Codice della tipologia
 RITORNA:     VARCHAR2       tipo di diritto, Read, Create, Update
******************************************************************************/
FUNCTION GET_GRUPPI_AREA ( P_ID_AREA NUMBER, P_ID_TIPOLOGIA NUMBER) RETURN VARCHAR2;
/******************************************************************************
 DESCRIZIONE: Riceve area di accesso e tipologia (di documenti) ;
              Ritorna una stringa contenente i gruppi che hanno almeno diritto di lettura
              su area e tipologia passati.
 PARAMETRI:   P_ID_AREA      NUMBER      Codice dell'area
              P_ID_TIPOLOGIA NUMBER      Codice della tipologia
 RITORNA:     VARCHAR2       elenco dei gruppi separati da virgola
******************************************************************************/
FUNCTION GET_DIRITTO_DOC (
  P_UTENTE VARCHAR2
, P_ID_DOCUMENTO NUMBER
, P_REVISIONE    NUMBER DEFAULT 0
) RETURN VARCHAR2;
/******************************************************************************
 DESCRIZIONE: Riceve codice del documento e dell'utente.
              Ritorna il diritto di Accesso piu alto per il gruppo cui appartiene l'utente interessato
              ritrovato su tabella AMV_DIRITTI per l'area relativa al documento indicato ('R','C' o 'U').
 PARAMETRI:   P_UTENTE       VARCHAR2    Codice utente
              P_ID_DOCUMENTO NUMBER      Codice del documento
 RITORNA:     VARCHAR2       tipo di diritto, Read, Create, Update
******************************************************************************/
FUNCTION DOCUMENTO_DATA_CHK ( P_ID_DOCUMENTO NUMBER) RETURN NUMBER;
/******************************************************************************
 DESCRIZIONE: Controlla che il documento sia visualizzabile sulla base delle sue date di
            inizio e fine pubblicazione.
 PARAMETRI:   P_ID_DOCUMENTO NUMBER
              p_<par2> <type> <Descrizione parametro 2>.
 RITORNA:     NUMBER 1 : documento visualizzabile
                     0 : documento non visualizzabile
******************************************************************************/
FUNCTION GET_LINK_DOCUMENTI (P_ZONA VARCHAR2, P_SEQUENZA NUMBER, P_STILE VARCHAR2 DEFAULT NULL) RETURN VARCHAR2;
/******************************************************************************
 DESCRIZIONE: Restituisce il link alla pagina di visualizzazione dei documenti per la tipologia che
            e associata alla zona P_ZONA con sequenza passata.
 PARAMETRI:   P_ZONA        VARCHAR2     zona cui e associata la categoria di documenti che vogliamo mostrare
              P_SEQUENZA    NUMBER       sequenza di esposizione
           P_STILE       VARCHAR2     attributi assegnati al link
 RITORNA:     VARCHAR2  : stringa che contiene il link HTML alla tipologia di documento indicata attraverso la zona
******************************************************************************/
FUNCTION GET_LINK_DOCUMENTI (P_ZONA VARCHAR2, P_STILE VARCHAR2 DEFAULT NULL) RETURN VARCHAR2;
/******************************************************************************
 DESCRIZIONE: Restituisce una stringa che contiene tutti i link delle tipologie di documenti
            che sono associate alla zona passata.
           La stringa avra l'aspetto seguente :
           tipologia1 | tipologia2 |...| tipologiaN |
 PARAMETRI:   P_ZONA        VARCHAR2     zona cui e associata la categoria di documenti che vogliamo mostrare
           P_STILE       VARCHAR2     attributi assegnati al link
 RITORNA:     VARCHAR2  : stringa che contiene i link HTML alle tipologia di documenti indicate attraverso la zona
******************************************************************************/
FUNCTION GET_BLOCCO_TIPOLOGIA ( P_UTENTE VARCHAR2, P_ID_TIPOLOGIA NUMBER, P_ID_SEZIONE NUMBER DEFAULT 0) RETURN CLOB;
/******************************************************************************
 DESCRIZIONE: Restituisce il blocco relativo alla tipologia passata
 PARAMETRI:   P_ID_TIPOLOGIA NUMBER    tipologia
           P_ID_SEZIONE   NUMBER    sezione sulla quale filtrare i documenti
 RITORNA:     VARCHAR2  : stringa che contiene HTML del blocco
******************************************************************************/
FUNCTION GET_BLOCCO_RILEVANZA ( P_UTENTE VARCHAR2, P_ID_RILEVANZA NUMBER, P_ID_SEZIONE NUMBER DEFAULT 0) RETURN CLOB;
/******************************************************************************
 DESCRIZIONE: Restituisce il blocco relativo alla rilevanza passata
 PARAMETRI:   P_ID_RILEVANZA NUMBER    rilevanza
           P_ID_SEZIONE   NUMBER    sezione sulla quale filtrare i documenti
 RITORNA:     VARCHAR2  : stringa che contiene HTML del blocco
******************************************************************************/
FUNCTION GET_BLOCCO_DOCUMENTI ( P_UTENTE VARCHAR2, P_ZONA VARCHAR2, P_SEQUENZA NUMBER) RETURN CLOB;
/******************************************************************************
 DESCRIZIONE: Restituisce il blocco relativo ai documenti di tipologia corrispondente a zona e sequenza
            passate visibili all'utente indicato.
 PARAMETRI:   P_UTENTE    VARCHAR2      codice utente di AD4
              P_ZONA      VARCHAR2      zona cui e associata la categoria di documenti che vogliamo mostrare
              P_SEQUENZA  NUMBER        numero di sequenza di esposizione
 RITORNA:     VARCHAR2  : stringa che codifica il blocco HTML che appare sulla sinistra della pagina in una
                          delle seguenti zone : Alto, Centro, Basso
******************************************************************************/
FUNCTION GET_BLOCCO_DOCUMENTI ( P_UTENTE VARCHAR2, P_ZONA VARCHAR2) RETURN CLOB;
/******************************************************************************
 DESCRIZIONE: Restituisce i blocchi relativi ai documenti delle tipologie corrispondenti alla zona
            passata visibili all'utente indicato.
 PARAMETRI:   P_UTENTE        VARCHAR2    codice utente di AD4
              P_ZONA        VARCHAR2      zona cui e associata la categoria di documenti che vogliamo mostrare
 RITORNA:     VARCHAR2  : stringa che contiene il blocco HTML della tipologia di documento indicata attraverso la zona
******************************************************************************/
FUNCTION GET_NUOVI_MSG (P_UTENTE VARCHAR2) RETURN VARCHAR2;
-- Metodi di gestione attributi.
FUNCTION INITIALIZE (P_ID_DOCUMENTO NUMBER DEFAULT NULL) RETURN VARCHAR2;
/******************************************************************************
 DESCRIZIONE: Inizializza le variabile del Package acquisendo la registrazione identificata.
 PARAMETRI:   P_ID_DOCUMENTO       NUMBER     identificativo del documento.
 RITORNA:     VARCHAR2  : stringa SEMPRE VUOTA.
 ECCEZIONI:   In caso di errore svuota il contenuto delle variabili.
******************************************************************************/
FUNCTION GET_TIPOLOGIA (P_ID_DOCUMENTO NUMBER) RETURN NUMBER;
FUNCTION GET_CATEGORIA (P_ID_DOCUMENTO NUMBER) RETURN NUMBER;
FUNCTION GET_ARGOMENTO (P_ID_DOCUMENTO NUMBER) RETURN NUMBER;
FUNCTION GET_RILEVANZA (P_ID_DOCUMENTO NUMBER) RETURN NUMBER;
FUNCTION GET_AREA (P_ID_DOCUMENTO NUMBER) RETURN NUMBER;
FUNCTION GET_NOME_TIPOLOGIA (P_ID_DOCUMENTO NUMBER, P_ID_TIPOLOGIA NUMBER DEFAULT NULL) RETURN VARCHAR2;
FUNCTION GET_NOME_CATEGORIA (P_ID_DOCUMENTO NUMBER, P_ID_CATEGORIA NUMBER DEFAULT NULL) RETURN VARCHAR2;
FUNCTION GET_NOME_ARGOMENTO (P_ID_DOCUMENTO NUMBER, P_ID_ARGOMENTO NUMBER DEFAULT NULL) RETURN VARCHAR2;
FUNCTION GET_NOME_RILEVANZA (P_ID_DOCUMENTO NUMBER, P_ID_RILEVANZA NUMBER DEFAULT NULL) RETURN VARCHAR2;
FUNCTION GET_NOME_AREA (P_ID_DOCUMENTO NUMBER, P_ID_AREA NUMBER DEFAULT NULL) RETURN VARCHAR2;
FUNCTION GET_TESTO (P_ID_DOCUMENTO NUMBER) RETURN CLOB;
FUNCTION GET_TESTO_OVERFLOW (P_ID_DOCUMENTO NUMBER) RETURN CLOB;
FUNCTION CREA_LINK_DOCUMENTO
( P_ID_DOCUMENTO IN NUMBER
, P_LINK   VARCHAR2
, P_TITOLO VARCHAR2
, P_PATH   VARCHAR2 DEFAULT NULL) RETURN VARCHAR2;
FUNCTION GET_ALLEGATI
(P_ID_DOCUMENTO NUMBER
,P_REVISIONE    NUMBER
) RETURN VARCHAR2;
-- Procedure di DML
PROCEDURE INSERISCI
( P_ID_DOCUMENTO         IN OUT NUMBER
, P_ID_TIPOLOGIA         IN NUMBER
, P_ID_CATEGORIA         IN NUMBER
, P_ID_ARGOMENTO         IN NUMBER
, P_ID_RILEVANZA         IN NUMBER
, P_ID_AREA              IN NUMBER
, P_TITOLO               IN VARCHAR2
, P_TIPO_TESTO           IN VARCHAR2
, P_TESTO                IN VARCHAR2
, P_ABSTRACT             IN VARCHAR2
, P_LINK                 IN VARCHAR2
, P_IMMAGINE             IN VARCHAR2
, P_ICONA                IN VARCHAR2
, P_DATA_RIFERIMENTO     IN DATE
, P_INIZIO_PUBBLICAZIONE IN DATE
, P_FINE_PUBBLICAZIONE   IN DATE
, P_AUTORE               IN VARCHAR2
, P_REVISIONE            IN NUMBER DEFAULT 0
, P_ID_SEZIONE           IN NUMBER DEFAULT 0
, P_SEQUENZA             IN NUMBER DEFAULT 0
);
PROCEDURE AGGIORNA
( P_ID_DOCUMENTO IN NUMBER
, P_ID_TIPOLOGIA IN NUMBER
, P_ID_CATEGORIA IN NUMBER
, P_ID_ARGOMENTO IN NUMBER
, P_ID_RILEVANZA IN NUMBER
, P_ID_AREA      IN NUMBER
, P_TITOLO       IN VARCHAR2
, P_TIPO_TESTO   IN VARCHAR2
, P_TESTO        IN VARCHAR2
, P_ABSTRACT     IN VARCHAR2
, P_LINK         IN VARCHAR2
, P_IMMAGINE     IN VARCHAR2
, P_ICONA        IN VARCHAR2
, P_ID_BLOB              IN NUMBER
, P_DATA_RIFERIMENTO     IN DATE
, P_INIZIO_PUBBLICAZIONE IN DATE
, P_FINE_PUBBLICAZIONE   IN DATE
, P_UTENTE_AGGIORNAMENTO IN VARCHAR2
, P_REVISIONE            IN NUMBER DEFAULT 0
, P_ID_SEZIONE           IN NUMBER DEFAULT 0
, P_SEQUENZA             IN NUMBER DEFAULT 0
);
PROCEDURE ELIMINA
( P_ID_DOCUMENTO         IN NUMBER
, P_REVISIONE            IN NUMBER DEFAULT 0
, P_STATO                IN VARCHAR2
, P_UTENTE_AGGIORNAMENTO IN VARCHAR2
);
FUNCTION CREA_RICHIESTA
( P_ID_DOCUMENTO         IN NUMBER
, P_CODICE_RICHIESTA     IN VARCHAR2
, P_UTENTE               IN VARCHAR2
)
RETURN VARCHAR2;
FUNCTION GET_MODELLO (
  P_ID_DOCUMENTO NUMBER
, P_REVISIONE NUMBER
, P_TIPO VARCHAR2 DEFAULT 'R'
) RETURN VARCHAR2;
FUNCTION GET_MODELLO (
  P_LINK VARCHAR2
, P_TIPO VARCHAR2 DEFAULT 'R'
) RETURN VARCHAR2;
FUNCTION GET_LINK (P_ID_DOCUMENTO NUMBER, P_REVISIONE NUMBER)
RETURN VARCHAR2;
FUNCTION GET_LINK (P_ID_DOCUMENTO NUMBER, P_REVISIONE NUMBER, P_LINK VARCHAR2,P_TIPO_TESTO VARCHAR2 DEFAULT NULL)
RETURN VARCHAR2;
FUNCTION GET_RIGA (
  p_id_documento NUMBER
, p_revisione    NUMBER
, p_id_sezione   NUMBER
, p_zona         VARCHAR2
, p_importanza   VARCHAR2 DEFAULT 'N'
, p_titolo       VARCHAR2
, p_LINK         VARCHAR2
, p_immagine     VARCHAR2
, p_id_tipologia NUMBER
, p_tipo_testo   VARCHAR2
, p_id_documento_padre NUMBER
, p_dir_upload   VARCHAR2
, p_td_class     VARCHAR2
, p_link_class   VARCHAR2
, p_pfx          VARCHAR2
) RETURN VARCHAR2;
END Amv_Documento;
/

