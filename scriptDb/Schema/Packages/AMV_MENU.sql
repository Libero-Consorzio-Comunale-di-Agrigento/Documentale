CREATE OR REPLACE PACKAGE Amv_Menu AS
/******************************************************************************
 DESCRIZIONE: Gestione tabelle AMV_MENU e AMV_ABILITAZIONI
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    16/10/2002 __     Prima emissione.
 1    27/11/2002 AO     Gestione CLOB
 2    17/12/2002 AO     Aggiunte funzioni get_param e get_param_voce
 3    14/02/2003 AO     Aggiunte funzioni get_page, get_voce_menu e get_link_stringa
 4    09/04/2003 AO     Adeguamento classi html ai nuovi fogli di stile
 5    11/07/2003 AO     Modificata get_menu per gestire espansione primo livello
                        e link privi del suffisso ../
 6    28/07/2003 AO     Modificata get_menu per gestione guide anche dai blocchi
                  di menu "Altre voci" e "Tutte le voci"
 7    01/08/2003 AO     Modificata get_page per gestione link privi di suffisso ../
 8    05/08/2003 AO     Modificata get_menu per gestione preferenza HomePage
 9    17/09/2003 AO     Modificata get_link_stringa per gestione link senza suffisso
                        con DNS attivo sulla directory di progetto
 10   03/10/2003 AO     Gestione Menu compatibile con Oracle 9
 11   30/10/2003 AO     Modificata get_navigatore per gestione di tutti i livelli
 12   30/12/2003 AO     Modificata get_menu per gestione menu senza voci
 13   20/01/2004 AO     Modificata get_albero_menu per gestione menu eccedenti 32000 char
 14   17/02/2004 AO     Modificata get_menu eliminando voce "Ritorna" per chiamate
                        fra applicativi diversi
 15   06/04/2004 AO     Modificata get_menu utilizzo classi di stile per i blocchi
                        di sinistra
 16   16/11/2004 AO     Modificata function get_navigatore
 17   25/01/2005 AO     Modificate function get_voce_pagina e get_albero_menu
 18   02/03/2005 AO     Aggiunta function get_titolo_pagina
 19   27/05/2005 AO     Modificata function get_page per getione link con parametri
******************************************************************************/
d_versione  VARCHAR2(20) := 'V2005.05';
d_revisione VARCHAR2(30) := '19   27/05/2005';
FUNCTION VERSIONE RETURN VARCHAR2;
FUNCTION GET_PAGE
( P_TIPO    VARCHAR2
, P_STRINGA VARCHAR2
, P_INDICE  NUMBER
, P_CONTEXT VARCHAR2 DEFAULT NULL
) RETURN VARCHAR2;
/******************************************************************************
 DESCRIZIONE: Ottiene il link ad una pagina sulla base della stringa passata.
            Se il tipo e "P" la stringa contiene uno o piu url.
           Se il tipo e "V" la stringa contiene una voce di menu
******************************************************************************/
FUNCTION GET_PAGE
( P_VOCE    VARCHAR2
, P_CONTEXT VARCHAR2 DEFAULT NULL
) RETURN VARCHAR2;
FUNCTION GET_VOCE_MENU (P_PAGINA VARCHAR2, P_PROGETTO VARCHAR2) RETURN VARCHAR2;
/******************************************************************************
 DESCRIZIONE: Ottiene la voce di menu associata ai valori di pagina e progetto passati.
******************************************************************************/
FUNCTION GET_VOCE_ABILITAZIONE (P_ABILITAZIONE NUMBER) RETURN VARCHAR2;
/******************************************************************************
 DESCRIZIONE: Ottiene la voce di menu associata all'abilitazione passata.
******************************************************************************/
FUNCTION GET_VOCE_PAGINA (P_UTENTE VARCHAR2, P_RUOLO VARCHAR2, P_MODULO VARCHAR2,P_PATH VARCHAR2) RETURN VARCHAR2;
FUNCTION GET_TITOLO_PAGINA (P_PROGETTO VARCHAR2,P_PATH VARCHAR2) RETURN VARCHAR2;
FUNCTION GET_LINK_STRINGA
( P_STRINGA    IN  VARCHAR2
, P_PAGINA     IN  VARCHAR2 DEFAULT NULL
, P_CONTEXT    IN  VARCHAR2 DEFAULT NULL
) RETURN VARCHAR2;
/******************************************************************************
 DESCRIZIONE: Restituisce il link corrispondente a stringa e pagina passate.
            P_STRINGA fa riferimento al campo STRINGA della tabella AMV_VOCI.
           P_PAGINA fa riferimento al campo MODULO della tabella AMV_VOCI che viene utilizzato
           per l'utilizzo, in CodeChargeStudio,  di una pagina in modalita "include" che impone
           l'utilizzo del parametro MVPG.
******************************************************************************/
FUNCTION GET_REGISTRO (P_CHIAVE VARCHAR2, P_STRINGA VARCHAR2) RETURN VARCHAR2;
FUNCTION GET_PARAM(P_STRINGA VARCHAR2, P_PNAME VARCHAR2) RETURN VARCHAR2;
FUNCTION GET_PARAM_VOCE(P_VOCE VARCHAR2, P_PNAME VARCHAR2) RETURN VARCHAR2;
FUNCTION GET_ABILITAZIONE (P_VOCE VARCHAR2, P_RUOLO VARCHAR2, P_MODULO VARCHAR2,P_PADRE NUMBER) RETURN NUMBER;
FUNCTION GET_ACCESSO_PAGINA
( P_VOCE         VARCHAR2
, P_PATH         VARCHAR2
) RETURN NUMBER;
FUNCTION GET_NAVIGATORE
/* deprecata da V.2004.8*/
( P_VOCE       VARCHAR2
, P_VOCE_L1    VARCHAR2
, P_TIPOLOGIA  VARCHAR2
, P_CONTEXT    VARCHAR2 DEFAULT NULL
) RETURN VARCHAR2;
FUNCTION GET_NAVIGATORE
( P_VOCE       VARCHAR2
, P_VOCE_L1    VARCHAR2
, P_VOCE_L2    VARCHAR2
, P_VOCE_L3    VARCHAR2
, P_TIPOLOGIA  VARCHAR2
, P_CONTEXT    VARCHAR2 DEFAULT NULL
) RETURN VARCHAR2;
FUNCTION GET_BLOCCO
( P_UTENTE     IN  VARCHAR2
, P_ISTANZA    IN  VARCHAR2
, P_MODULO     IN  VARCHAR2
, P_VOCE       IN  VARCHAR2
, P_VOCE_L1    IN  VARCHAR2
, P_VOCE_L2    IN  VARCHAR2
, P_VOCE_L3    IN  VARCHAR2
, P_RUOLO      IN  VARCHAR2
, P_RETURNPAGE IN  VARCHAR2 DEFAULT NULL
, P_BACKPAGE   IN  VARCHAR2 DEFAULT NULL
, P_CONTEXT    IN  VARCHAR2 DEFAULT NULL
) RETURN CLOB;
FUNCTION GET_MENU
( P_UTENTE     IN  VARCHAR2
, P_ISTANZA    IN  VARCHAR2
, P_MODULO     IN  VARCHAR2
, P_VOCE       IN  VARCHAR2
, P_VOCE_L1    IN  VARCHAR2
, P_VOCE_L2    IN  VARCHAR2
, P_VOCE_L3    IN  VARCHAR2
, P_RUOLO      IN  VARCHAR2
, P_RETURNPAGE IN  VARCHAR2 DEFAULT NULL
, P_BACKPAGE   IN  VARCHAR2 DEFAULT NULL
, P_CONTEXT    IN  VARCHAR2 DEFAULT NULL
) RETURN CLOB;
FUNCTION GET_ALBERO_MENU
( P_UTENTE     IN VARCHAR2
, P_ISTANZA    IN VARCHAR2
, P_MODULO     IN VARCHAR2
, P_PROGETTO   IN VARCHAR2
, P_VOCE_L2    IN VARCHAR2
, P_VOCE_L3    IN VARCHAR2
, P_RUOLO      IN VARCHAR2
, P_VOCE_SEL   IN VARCHAR2
, P_ID         IN VARCHAR2
) RETURN CLOB;
END Amv_Menu;
/

