CREATE OR REPLACE PACKAGE Amv_Sezione AS
/****************************************************************************************
 DESCRIZIONE: Gestione tabelle AMV_SEZIONI e connesse
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    09/04/2004 AO     Prima emissione.
 1    16/11/2004 AO     Versione 2004.09
 2    27/05/2005 AO     Versione 2005.05 modificata get_albero e ordinamento documenti
******************************************************************************************/
d_versione  VARCHAR2(20) := 'V2005.05';
d_revisione VARCHAR2(30) := '12   27/05/2005';
FUNCTION VERSIONE RETURN VARCHAR2;
-- Metodi relativi ai documenti
FUNCTION GET_BLOCCO ( P_UTENTE VARCHAR2, P_ZONA VARCHAR2, P_ID_SEZIONE NUMBER, P_PAGE VARCHAR2) RETURN CLOB;
/******************************************************************************
 DESCRIZIONE: Restituisce il blocco relativo alla sezione selezionata.
 PARAMETRI:   P_ID_SEZIONE  NUMBER      id della sezione
 RITORNA:     CLOB  : stringa che codifica il blocco HTML che appare sulla sinistra
******************************************************************************/
FUNCTION GET_CORPO (
  P_UTENTE VARCHAR2
, P_ID_SEZIONE NUMBER
, P_PAGE VARCHAR2
, P_COLONNE NUMBER
) RETURN CLOB;
FUNCTION GET_MENU (
  P_UTENTE VARCHAR2
, P_ZONA VARCHAR2
, P_ID_SEZIONE NUMBER
, P_PAGE VARCHAR2
) RETURN CLOB;
/******************************************************************************
 DESCRIZIONE: Restituisce il menu relativo alle sezioni di contenuti.
 PARAMETRI:   P_UTENTE  VARCHAR2      utente
            P_ZONA    VARCHAR2      zona di visualizzazione del menu
           P_ID_SEZIONE NUMBER     identificativo sezione selezionata
           P_PAGE    VARCHAR2      pagina che richiama il menu
 RITORNA:     CLOB  : stringa che codifica il codice HTML relativo al menu sezioni
******************************************************************************/
FUNCTION GET_BLOCCO_MENU ( P_UTENTE VARCHAR2, P_ZONA VARCHAR2, P_ID_SEZIONE NUMBER, P_BLOCCO VARCHAR2 DEFAULT NULL) RETURN VARCHAR2;
/******************************************************************************
 DESCRIZIONE: Restituisce il blocco relativo alla sezione selezionata.
 PARAMETRI:   P_UTENTE  VARCHAR2      utente
            P_ZONA    VARCHAR2      zona di visualizzazione del blocco
           P_ID_SEZIONE NUMBER     identificativo sezione selezionata
           P_BLOCCO VARCHAR2       parametro usato per la ricorsione
 RITORNA:     CLOB  : stringa che codifica il blocco HTML relativo alla sezione
******************************************************************************/
FUNCTION GET_BLOCCO_SOTTOSEZIONI ( P_ID_SEZIONE NUMBER, P_SELECTED NUMBER, P_ZONA VARCHAR2) RETURN VARCHAR2;
/******************************************************************************
 DESCRIZIONE: Restituisce le righe relative alle sottosezioni della sezione passata
 PARAMETRI:   P_ID_SEZIONE NUMBER  sezione passata
              P_SELECTED   NUMBER  indica se si sta considerando una sezione selezionata
            P_ZONA    VARCHAR2   zona di visualizzazione
 RITORNA:     VARCHAR2  : stringa che codifica le righe HTML relative alle sottosezioni
******************************************************************************/
FUNCTION GET_BLOCCO_DOCUMENTI (
  P_UTENTE VARCHAR2
, P_ID_SEZIONE NUMBER
, P_SELECTED NUMBER
, P_ZONA VARCHAR2
, P_MAX_VIS NUMBER
, P_ICONA   VARCHAR2 DEFAULT NULL
) RETURN VARCHAR2;
/******************************************************************************
 DESCRIZIONE: Restituisce le righe relative ai documenti della sezione passata
 PARAMETRI:   P_ID_SEZIONE NUMBER  sezione passata
              P_SELECTED   NUMBER  indica se si sta considerando una sezione selezionata
 RITORNA:     VARCHAR2  : stringa che codifica le righe HTML relative ai documenti
******************************************************************************/
FUNCTION GET_NAVIGATORE (
  P_ID_SEZIONE NUMBER
, P_UTENTE VARCHAR2
, P_MODULO VARCHAR2) RETURN VARCHAR2;
FUNCTION GET_ALBERO_SEZIONI (
  P_ID_SEZIONE   NUMBER
, P_ID_TIPOLOGIA NUMBER
, P_ID_CATEGORIA NUMBER
, P_ID_ARGOMENTO NUMBER
, P_TIPO_VISUALIZZAZIONE VARCHAR2
, P_UTENTE VARCHAR2
, P_PAGE   VARCHAR2
) RETURN CLOB;
FUNCTION GET_DOCUMENTI_SEZIONE (
  P_ID_SEZIONE   NUMBER
, P_UTENTE VARCHAR2
) RETURN VARCHAR2;
FUNCTION GET_PATH_SEZIONE (
  P_ID_SEZIONE NUMBER
, P_PATH VARCHAR2 DEFAULT NULL) RETURN VARCHAR2;
FUNCTION GET_SORTED_PATH_SEZIONE (
  P_ID_SEZIONE NUMBER
, P_PATH VARCHAR2 DEFAULT NULL) RETURN VARCHAR2;
TYPE t_array IS TABLE OF NUMBER INDEX BY BINARY_INTEGER;
FUNCTION GET_GRAPHIC_PATH_SEZIONE (
  P_ID_SEZIONE NUMBER
, P_CHILD t_array
, P_CHILD_RIMANENTI t_array
, P_PATH VARCHAR2 DEFAULT NULL) RETURN VARCHAR2;
FUNCTION GET_LIVELLO_SEZIONE (P_ID_SEZIONE NUMBER, P_LIVELLO NUMBER DEFAULT 0) RETURN NUMBER;
FUNCTION GET_PADRE_BLOCCO (P_ID_SEZIONE NUMBER, P_ZONA VARCHAR2 DEFAULT NULL) RETURN NUMBER;
FUNCTION GET_PADRE (P_ID_SEZIONE NUMBER) RETURN NUMBER;
FUNCTION GET_VISIBILITA (
  P_ID_SEZIONE NUMBER
, P_ID_SEZIONE_SEL NUMBER
, P_PAGE VARCHAR2
, P_ZONA_VISIBILITA VARCHAR2
)
RETURN NUMBER;
PROCEDURE INSERISCI_SEZIONE (
  p_id_sezione   IN NUMBER
, p_nome         IN VARCHAR2
, p_descrizione  IN VARCHAR2
, p_zona         IN VARCHAR2
, p_sequenza     IN NUMBER
, p_immagine     IN VARCHAR2
, p_max_vis      IN NUMBER
, p_id_padre     IN NUMBER
);
FUNCTION GET_PREFERENZA (
  P_ID_SEZIONE NUMBER
, P_PREFERENZA VARCHAR2
)
RETURN VARCHAR2;
FUNCTION GET_HEADER_BAR (
  P_UTENTE VARCHAR2
, P_ID_SEZIONE NUMBER
, P_PAGE VARCHAR2
)
RETURN VARCHAR2;
FUNCTION GET_BLOCCO_MODULISTICA (
  P_UTENTE VARCHAR2
, P_ID_SEZIONE NUMBER
, P_ZONA VARCHAR2
)
RETURN VARCHAR2;
FUNCTION GET_NOME (
  P_ID_SEZIONE NUMBER
)
RETURN VARCHAR2;
END Amv_Sezione;
/

