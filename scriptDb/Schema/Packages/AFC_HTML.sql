CREATE OR REPLACE PACKAGE Afc_Html
IS
   SUBTYPE T_LUNGO IS VARCHAR2 (32767);
      V_STRINGA  t_lungo;
      v_trovato NUMBER(1);
/******************************************************************************
 PACKAGE AFC_HTML
 DESCRIZIONE: generazione html di tabella visualizzata ad albero
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    03/12/2004 SN     Prima emissione.
******************************************************************************/
   D_REVISIONE   VARCHAR2 (20) := '1 del 4/03/2005';
   FUNCTION VERSIONE
      RETURN VARCHAR2;
   TYPE T_ARRAY IS TABLE OF NUMBER
      INDEX BY BINARY_INTEGER;
   TYPE T_REF IS REF CURSOR;
   PROCEDURE TREE_IMPOSTA_PREFERENZE (
      P_ALBERO        VARCHAR2,
      P_MODULO        VARCHAR2,
      P_IMMAGINI      VARCHAR2,
      P_MOSTRA_FOGLIA VARCHAR2,
      P_RIPOSIZIONA   VARCHAR2,
      P_UNO_APERTO    VARCHAR2
   );
/******************************************************************************
 PROCEDURE TREE_IMPOSTA_PREFERENZE
 DESCRIZIONE: Imposta nei registri alcune delle preferenze di visualizzazione
             dell'albero ci sono quelle che presumibilmente potrebbero essere
             più frequentemente da variare
 ARGOMENTI:
      p_albero        VARCHAR2 nome dell'albero
      p_modulo        VARCHAR2 modulo
      p_immagini      VARCHAR2 directory dove si trovano le immagini
      p_foglia        VARCHAR2 (S/N) se visualizzare le foglie
      p_riposiziona   VARCHAR2 (S/N) se riposizionarsi dopo esplorazione albero
      p_uno_aperto    VARCHAR2 (S/N) S= solo un folder aperto
******************************************************************************/
   PROCEDURE TREE_IMPOSTA (
      P_ALBERO                 VARCHAR2,
      P_MODULO                 VARCHAR2,
      P_TABELLA                VARCHAR2,
      P_COLONNA_PADRE          VARCHAR2,
      P_COLONNA_FIGLIO         VARCHAR2,
      P_COLONNA_NOME           VARCHAR2,
      P_EXPR_ORDINAMENTO       VARCHAR2,
      P_DISLOC_IMMAGINI        VARCHAR2 DEFAULT NULL,
      P_FUNC_FOGLIA            VARCHAR2 DEFAULT NULL,
      P_MOSTRA_FOGLIA          VARCHAR2 DEFAULT 'N',
      P_RIPOSIZIONA            VARCHAR2 DEFAULT 'N',
      P_UN_FOLDER_APERTO       VARCHAR2 DEFAULT 'S',
      P_INCLUSIONE             VARCHAR2 DEFAULT 'N',
      P_PAGINA_HREF            VARCHAR2 DEFAULT NULL,
      P_PARAMETRO_INCLUSIONE   VARCHAR2 DEFAULT NULL,
      P_PARAMETRO_URL          VARCHAR2 DEFAULT 'ST',
      P_SEPARATORE             VARCHAR2 DEFAULT '|',
      P_FLD_CLOSE              VARCHAR2 DEFAULT NULL,
      P_FLD_OPEN               VARCHAR2 DEFAULT NULL,
      P_LAST_FLD_OPEN          VARCHAR2 DEFAULT NULL,
      P_PLUS_NODE              VARCHAR2 DEFAULT NULL,
      P_LAST_PLUS_NODE         VARCHAR2 DEFAULT NULL,
      P_MINUS_NODE             VARCHAR2 DEFAULT NULL,
      P_LAST_MINUS_NODE        VARCHAR2 DEFAULT NULL,
      P_IMG_LINE               VARCHAR2 DEFAULT NULL,
      P_BLANK                  VARCHAR2 DEFAULT NULL,
      P_FOGLIA                 VARCHAR2 DEFAULT NULL
   );
/******************************************************************************
 PROCEDURE TREE_IMPOSTA
 DESCRIZIONE: Imposta nei registri TUTTE le informazioni necessarie per la struttura
 dell'albero e la sua visualizzazione
 ARGOMENTI:
   p_albero                 VARCHAR2 nome dell'albero
   p_modulo                 VARCHAR2 modulo
   p_tabella                VARCHAR2 tabella contenente i dati gerarchici
   p_colonna_padre          VARCHAR2 colonna padre
   p_colonna_figlio         VARCHAR2 colonna
   p_colonna_nome           VARCHAR2 descrizione da visualizzare
   p_expr_ordinamento       VARCHAR2 espressione usata per l'ordinamento
   p_disloc_immagini        VARCHAR2 directory dove si trovano le immagini
   p_func_foglia            VARCHAR2 cursore che estrae i dati "foglia"
   p_mostra_foglia          VARCHAR2 (S/N) S= visualizza i dati foglia
   p_riposiziona            VARCHAR2 (S/N) se riposizionarsi dopo esplorazione albero
   p_un_folder_aperto       VARCHAR2 (S/N) S= solo un folder aperto
   p_inclusione             VARCHAR2 (S/N) N=non è pagina inclusa (in CCS)
   p_pagina_href            VARCHAR2 nome pagina da visualizzare (in CCS)
   p_parametro_inclusione   VARCHAR2 parametro da usare in CCS per inclusione
   p_parametro_url          VARCHAR2 parametro in CCS da usare per visualizzazione
                                     informazioni in una struttura separata
 ******************************************************************************/
   FUNCTION TREE (
      P_STRUTTURA_ID   VARCHAR2,
      P_MODULO         VARCHAR2,
      P_ALBERO         VARCHAR2,
      P_PAGE           VARCHAR2
   )
      RETURN CLOB;
/******************************************************************************
 FUNCTION TREE
 DESCRIZIONE: Generazione albero -- funzione da richiamare da CCS
 Viene utilizzata indicando il nome dell'albero da rappresentare, le informazioni
 sulla struttura dell'albero sono nei registri
 PARAMETRI:
      p_struttura_id   VARCHAR2 struttura da visualizzare nell'albero
      p_modulo         VARCHAR2 modulo
      p_albero         VARCHAR2 nome dell'albero da rappresentare
      p_page           VARCHAR2 pagina in cui visualizzare l'albero
 RITORNA:     CLOB  Contiene html per rappresentazione dell'albero
******************************************************************************/
   FUNCTION TREE (
      P_STRUTTURA_ID           VARCHAR2,
      P_MODULO                 VARCHAR2,
      P_ALBERO                 VARCHAR2,
      P_PAGE                   VARCHAR2,
      P_TABELLA                VARCHAR2,
      P_COLONNA_PADRE          VARCHAR2,
      P_COLONNA                VARCHAR2,
      P_COLONNA_NOME           VARCHAR2,
      P_EXPR_ORDINAMENTO       VARCHAR2,
      P_FUNC_FOGLIA            VARCHAR2 DEFAULT NULL,
      P_MOSTRA_FOGLIA          VARCHAR2 DEFAULT 'N',
      P_RIPOSIZIONA            VARCHAR2 DEFAULT 'N',
      P_UN_FOLDER_APERTO       VARCHAR2 DEFAULT 'S',
      P_INCLUSIONE             VARCHAR2 DEFAULT 'N',
      P_PAGINA_HREF            VARCHAR2 DEFAULT NULL,
      P_PARAMETRO_INCLUSIONE   VARCHAR2 DEFAULT NULL,
      P_PARAMETRO_URL          VARCHAR2 DEFAULT 'ST',
      P_SEPARATORE             VARCHAR2 DEFAULT '|',
      P_DISLOC_IMMAGINI        VARCHAR2 DEFAULT NULL,
      P_FLD_CLOSE              VARCHAR2 DEFAULT NULL,
      P_FLD_OPEN               VARCHAR2 DEFAULT NULL,
      P_LAST_FLD_OPEN          VARCHAR2 DEFAULT NULL,
      P_PLUS_NODE              VARCHAR2 DEFAULT NULL,
      P_LAST_PLUS_NODE         VARCHAR2 DEFAULT NULL,
      P_MINUS_NODE             VARCHAR2 DEFAULT NULL,
      P_LAST_MINUS_NODE        VARCHAR2 DEFAULT NULL,
      P_IMG_LINE               VARCHAR2 DEFAULT NULL,
      P_BLANK                  VARCHAR2 DEFAULT NULL,
      P_FOGLIA                 VARCHAR2 DEFAULT NULL
   )
      RETURN CLOB;
/******************************************************************************
 FUNCTION TREE
 DESCRIZIONE: Generazione html per albero -- funzione da richiamare da CCS
 Viene utilizzata indicando tutte le caratteristiche dell'albero da rappresentare
 ARGOMENTI:
   p_id_struttura           VARCHAR2 identificativo struttura da visualizzare
   p_modulo                 VARCHAR2 modulo
   p_albero                 VARCHAR2 nome dell'albero
   p_page                   VARCHAR2 nome pagina da visualizzare (in CCS)
   p_tabella                VARCHAR2 tabella contenente i dati gerarchici
   p_colonna_padre          VARCHAR2 colonna padre
   p_colonna_figlio         VARCHAR2 colonna
   p_colonna_nome           VARCHAR2 descrizione da visualizzare
   p_expr_ordinamento       VARCHAR2 espressione usata per l'ordinamento
   p_func_foglia            VARCHAR2 cursore che estrae i dati "foglia"
   p_mostra_foglia          VARCHAR2 (S/N) S= visualizza i dati foglia
   p_riposiziona            VARCHAR2 (S/N) se riposizionarsi dopo esplorazione albero
   p_un_folder_aperto       VARCHAR2 (S/N) S= solo un folder aperto
   p_inclusione             VARCHAR2 (S/N) N=non è pagina inclusa (in CCS)
   p_parametro_inclusione   VARCHAR2 parametro da usare in CCS per inclusione
   p_parametro_url          VARCHAR2 parametro in CCS da usare per visualizzazione
                                     informazioni in una struttura separata
   p_separatore             VARCHAR2 carattere di separazione per gli id nell'url
   p_disloc_immagini        VARCHAR2 directory dove si trovano le immagini
   p_fld_close              VARCHAR2 nome immagine folder chiuso
   p_fld_open               VARCHAR2 nome immagine folder aperto
   p_last_fld_open          VARCHAR2 nome immagine ultimo folder aperto
   p_plus_node              VARCHAR2 nome immagine nodo da esplodere
   p_last_plus_node         VARCHAR2 nome immagine ultimo nodo da esplodere
   p_minus_node             VARCHAR2 nome immagine nodo già esploso
   p_last_minus_node        VARCHAR2 nome immagine ultimo nodo già esploso
   p_img_line               VARCHAR2 nome immagine linea
   p_blank                  VARCHAR2 nome immagine bianca
 RITORNA:     CLOB  Contiene html per rappresentazione dell'albero
******************************************************************************/
   FUNCTION TAB_FOLDER (
      IN_LINK     IN   VARCHAR2,
      IN_HREF     IN   VARCHAR2,
      IN_ACTIVE   IN   VARCHAR2 DEFAULT 'N'
   )
      RETURN T_LUNGO;
   /******************************************************************************
    FUNCTION TAB_FOLDER
    DESCRIZIONE: Generazione html per folder-- funzione da richiamare da CCS
    ARGOMENTI:
      in_link      VARCHAR2 Nome del link eventuale titolo separato da virgola
      in_href      VARCHAR2 Collegamento da attribuire al link
      in_active    VARCHAR2 S/N se è la pagina selezionata o meno
    ESEMPIO:
    da code charge creo un sql che torna tante colonne quanti sono i possibili
    folder.
    select afc_html.tab_folder
          ('Risorse',
           'AspMain.do?MVPG=AmbRisorseGruppo&GRUPPO_ID='||'{GRUPPO_ID}',
           decode('{MVPG}','AmbRisorseGruppo','S','N')
          ) folder1
          ... (altri folder...)
     from dual
    RITORNA:     varchar2  Contiene html per rappresentazione dei folder
    ******************************************************************************/
   FUNCTION GUIDA_BAR (
      IN_LINK            IN   VARCHAR2,
      IN_HREF            IN   VARCHAR2,
      IN_SEPARATORE_SN   IN   VARCHAR2 DEFAULT 'S',
      IN_CLASSE          IN   VARCHAR2 DEFAULT NULL
   )
      RETURN T_LUNGO;
/******************************************************************************
     FUNCTION GUIDA_BAR
     DESCRIZIONE: Generazione html per menu-- funzione da richiamare da CCS
     ARGOMENTI:
       in_link           VARCHAR2 Nome del link eventuale titolo separato da virgola
       in_href           VARCHAR2 Collegamento da attribuire al link
       in_separatore_SN  VARCHAR2 S/N se aggiungere in coda il separatore
       in_classe         VARCHAR2 Nome della classe, viene accodato Link
     ESEMPIO:
     da code charge creo un sql che torna tante colonne quanti sono i possibili
     folder.
     select afc_html.guida_bar
        ('Accesso,Informazioni per accesso al Luogo',
         'PsaMain.do?MVPG=PsaLuogoPrestazioneAcce&LUOGO_ID='||'{LUOGO_ID}'||'&PRESTAZIONE_ID='||'{PRESTAZIONE_ID}'
                                                 ||'&PRES_LUOG_ID='||'{PRES_LUOG_ID}',
         'S'
        ) menu1,
           ... (altri menu...)
      from dual
     RITORNA:     varchar2  Contiene html per rappresentazione del menu
    ******************************************************************************/
   FUNCTION FILTER_SEARCH (IN_FILTER_VALUE IN VARCHAR2)
      RETURN T_LUNGO;
 /******************************************************************************
    FUNCTION FILTER_SEARCH
    DESCRIZIONE: Generazione html per immagine filtro-- funzione da richiamare da CCS
    ARGOMENTI:
      in_filter_value      VARCHAR2 Valore del filtro
    ESEMPIO:
    da code charge basta mettere un link che richiama questa funzione passando
    il valore del filtro, in realtà interessa se è nullo o no.
    RITORNA:     varchar2  Contiene html per rappresentazione del filtro
   ******************************************************************************/
END;
/

