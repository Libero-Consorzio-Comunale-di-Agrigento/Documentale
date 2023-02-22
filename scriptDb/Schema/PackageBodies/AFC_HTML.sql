CREATE OR REPLACE PACKAGE BODY Afc_Html
IS
   SUBTYPE T_MILLE IS VARCHAR2 (1000);
   SUBTYPE T_CENTO IS VARCHAR2 (100);
   SUBTYPE T_UNO IS VARCHAR2 (1);
-- immagini
   D_DIRECTORY                 T_LUNGO    := '../common/images/AMV/';
   D_INIZIO_IMMAGINE           T_CENTO    := '<img border="0" src="';
   D_FINE_IMMAGINE             T_CENTO    := '" align="AbsMiddle">';
   D_FLD_CLOSE                 T_MILLE
      :=    D_INIZIO_IMMAGINE
         || D_DIRECTORY
         || 'cmsfolderclosed.gif'
         || D_FINE_IMMAGINE;
   D_FOGLIA                 T_MILLE
      :=    D_INIZIO_IMMAGINE
         || D_DIRECTORY
         || 'cmsdoc.gif'
         || D_FINE_IMMAGINE;
   D_LINE_FOGLIA                     T_MILLE
      :=    D_INIZIO_IMMAGINE
         || D_DIRECTORY
         || 'cmsnode.gif'
         || D_FINE_IMMAGINE;
   D_LAST_LINE_FOGLIA                     T_MILLE
      :=    D_INIZIO_IMMAGINE
         || D_DIRECTORY
         || 'cmslastnode.gif'
         || D_FINE_IMMAGINE;
   D_FLD_OPEN                  T_MILLE
      :=    D_INIZIO_IMMAGINE
         || D_DIRECTORY
         || 'cmsfolderopen.gif'
         || D_FINE_IMMAGINE;
   D_LAST_FLD_OPEN             T_MILLE
      :=    D_INIZIO_IMMAGINE
         || D_DIRECTORY
         || 'cmslastfolderopen.gif'
         || D_FINE_IMMAGINE;
   D_PLUS_NODE                 T_MILLE
      := D_INIZIO_IMMAGINE || D_DIRECTORY || 'cmspnode.gif' || D_FINE_IMMAGINE;
   D_LAST_PLUS_NODE            T_MILLE
      :=    D_INIZIO_IMMAGINE
         || D_DIRECTORY
         || 'cmsplastnode.gif'
         || D_FINE_IMMAGINE;
   D_MINUS_NODE                T_MILLE
      := D_INIZIO_IMMAGINE || D_DIRECTORY || 'cmsmnode.gif' || D_FINE_IMMAGINE;
   D_LAST_MINUS_NODE           T_MILLE
      :=    D_INIZIO_IMMAGINE
         || D_DIRECTORY
         || 'cmsmlastnode.gif'
         || D_FINE_IMMAGINE;
   D_IMG_LINE                  T_MILLE
      := D_INIZIO_IMMAGINE || D_DIRECTORY || 'cmsvertline.gif'
         || D_FINE_IMMAGINE;
   D_BLANK                     T_MILLE
      := D_INIZIO_IMMAGINE || D_DIRECTORY || 'cmsblank.gif' || D_FINE_IMMAGINE;
-- visualizzazione foglie
   D_FUNCTION_TABELLA          T_MILLE;
   D_IMG_TABELLA               T_MILLE;
   D_ULT_IMG_TABELLA           T_MILLE;
   D_TROVATO_TABELLA           NUMBER (1) := 0;
   D_LIVELLO_TABELLA           NUMBER (1) := 0;
   D_TABELLA_CLOB              CLOB       := EMPTY_CLOB ();
   D_IMG_OPEN                  T_MILLE;
   D_IMG_NODE                  T_MILLE;
   D_IMG_NODE_OPEN             T_MILLE;
   D_IMG                       T_MILLE    := D_FLD_CLOSE;
   D_COLONNA                   T_CENTO;
   D_COLONNA_PADRE             T_CENTO;
   D_COLONNA_ORDINAMENTO       T_CENTO;
   D_COLONNA_NOME              T_CENTO;
   D_TABELLA                   T_CENTO;
   D_SOLO_UN_FOLDER_APERTO     T_UNO;
   D_PAGINA                    T_CENTO;
   D_INCLUSIONE                T_UNO;
   D_PARAMETRO_INCLUSIONE      T_CENTO;
   D_INSERIRE_POSIZIONAMENTO   T_UNO;
   D_POSIZIONAMENTO            T_LUNGO;
   D_PAGINA_URL                T_MILLE;
   D_PARAMETRO_URL             T_CENTO;
   D_SEPARATORE                T_UNO;
   D_PARAMETRO_HREF_FIGLIO     T_LUNGO;
   D_PARAMETRO_HREF_PADRE      T_LUNGO;
   D_VISUALIZZA_FOGLIE         T_UNO;
/******************************************************************************
 PROCEDURE INIZIA_TABELLA
 DESCRIZIONE: Predispone codice html per apertura tabella
 ******************************************************************************/
PROCEDURE INIZIA_TABELLA (
      P_LIVELLO              NUMBER,
      P_FRATELLI             NUMBER,
      P_NUMERO_FIGLI_PADRE   NUMBER
   )
   IS
      D_INIZIO_TABELLA   T_LUNGO;
   BEGIN
      IF P_FRATELLI = 1
      THEN
         D_IMG_TABELLA := D_BLANK;
      ELSE
         D_IMG_TABELLA := D_IMG_LINE;
      END IF;
      IF P_NUMERO_FIGLI_PADRE = 0
      THEN
         D_ULT_IMG_TABELLA := D_BLANK;
      ELSE
         D_ULT_IMG_TABELLA := D_IMG_LINE;
      END IF;
      DBMS_LOB.CREATETEMPORARY (D_TABELLA_CLOB, TRUE, DBMS_LOB.SESSION);
      D_INIZIO_TABELLA :=
                     '<table BORDER="0" CELLPADDING="0" CELLSPACING="0">';-- <tr>';
--   for i in 1 .. p_livello - 1 loop
--      d_inizio_tabella :=D_inizio_tabella || '<td>'|| d_img_line||'</td>' ;--????
--    end loop;
--    d_inizio_tabella :=D_inizio_tabella || '<td>'|| d_img_tabella ||'</td>' ;
--    d_inizio_tabella :=D_inizio_tabella || '<td>'|| d_ult_img_tabella ||'</td>' ;
      DBMS_LOB.WRITEAPPEND (D_TABELLA_CLOB,
                            LENGTH (D_INIZIO_TABELLA),
                            D_INIZIO_TABELLA
                           );
      D_TROVATO_TABELLA := 0;
      D_LIVELLO_TABELLA := P_LIVELLO;
   END;
 /******************************************************************************
 PROCEDURE INIZIA_RIGA
 DESCRIZIONE: Predispone codice html per inziare una riga
 ******************************************************************************/
   PROCEDURE INIZIA_RIGA
   IS
      D_ROW   T_LUNGO;
   BEGIN
      D_ROW := '<tr>';
      FOR I IN 1 .. D_LIVELLO_TABELLA - 1
      LOOP
         D_ROW := D_ROW || '<td>' || D_IMG_LINE || '</td>';
      END LOOP;
      D_ROW := D_ROW || '<td>' || D_IMG_TABELLA || '</td>';
      D_ROW := D_ROW || '<td>' || D_ULT_IMG_TABELLA || '</td>';
      D_ROW := D_ROW || '<td>' || D_BLANK || '</td>';
      DBMS_LOB.WRITEAPPEND (D_TABELLA_CLOB, LENGTH (D_ROW), D_ROW);
   END;
   FUNCTION AGGIUNGI_DATO (D_COLONNA VARCHAR2)
      RETURN VARCHAR2
   IS
   BEGIN
      --return('<TD class="AFCDataTD">'||D_colonna ||'&nbsp;</td>');
      RETURN ('<TD>' || D_COLONNA || '&nbsp;</td>');
   END;
 /******************************************************************************
 PROCEDURE METTI_INTESTAZIONE
 DESCRIZIONE: Predispone codice html per intestazione tabella
 ******************************************************************************/
   PROCEDURE METTI_INTESTAZIONE (P_STRINGA VARCHAR2)
   IS
      D_INTESTAZIONE   T_LUNGO := AGGIUNGI_DATO (P_STRINGA);
   BEGIN
      DBMS_LOB.WRITEAPPEND (D_TABELLA_CLOB,
                            LENGTH (D_INTESTAZIONE),
                            D_INTESTAZIONE
                           );
   END;
 /******************************************************************************
 PROCEDURE CHIUDI_RIGA
 DESCRIZIONE: Predispone codice html per terminare una riga
 ******************************************************************************/
   PROCEDURE CHIUDI_RIGA
   IS
   BEGIN
      DBMS_LOB.WRITEAPPEND (D_TABELLA_CLOB, LENGTH ('</tr>'), '</tr>');
   END;
 /******************************************************************************
 PROCEDURE SCRIVI_COLONNA
 DESCRIZIONE: Predispone codice html per scrivere una intestazione
 ******************************************************************************/
   PROCEDURE SCRIVI_COLONNA (P_STRINGA VARCHAR2)
   IS
      D_RIGA   T_LUNGO := AGGIUNGI_DATO (P_STRINGA);
   BEGIN
      D_TROVATO_TABELLA := 1;
      DBMS_LOB.WRITEAPPEND (D_TABELLA_CLOB, LENGTH (D_RIGA), D_RIGA);
   END SCRIVI_COLONNA;
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
)
   IS
      D_CHIAVE               T_LUNGO
         :=    'PRODUCTS/'
            || UPPER (P_MODULO)
            || '/AFC_HTML.TREE/'
            || UPPER (P_ALBERO);
      D_CHIAVE_DEFINIZIONE   T_LUNGO := D_CHIAVE || '/DEFINIZIONE';
   BEGIN
      Registro_Utility.SCRIVI_STRINGA (D_CHIAVE_DEFINIZIONE,
                                       'Colonna Figlio',
                                       P_COLONNA_FIGLIO
                                      );
      Registro_Utility.SCRIVI_STRINGA (D_CHIAVE_DEFINIZIONE,
                                       'Colonna Nome',
                                       P_COLONNA_NOME
                                      );
      Registro_Utility.SCRIVI_STRINGA (D_CHIAVE_DEFINIZIONE,
                                       'Colonna Ordinamento',
                                       P_EXPR_ORDINAMENTO
                                      );
      Registro_Utility.SCRIVI_STRINGA (D_CHIAVE_DEFINIZIONE,
                                       'Colonna Padre',
                                       P_COLONNA_PADRE
                                      );
      Registro_Utility.SCRIVI_STRINGA (D_CHIAVE_DEFINIZIONE,
                                       'Dati foglia',
                                       P_FUNC_FOGLIA
                                      );
      Registro_Utility.SCRIVI_STRINGA (D_CHIAVE_DEFINIZIONE,
                                       'Inclusione',
                                       P_INCLUSIONE
                                      );
      Registro_Utility.SCRIVI_STRINGA (D_CHIAVE_DEFINIZIONE,
                                       'Pagina href',
                                       P_PAGINA_HREF
                                      );
      Registro_Utility.SCRIVI_STRINGA (D_CHIAVE_DEFINIZIONE,
                                       'Parametro Inclusione',
                                       P_PARAMETRO_INCLUSIONE
                                      );
      Registro_Utility.SCRIVI_STRINGA (D_CHIAVE_DEFINIZIONE,
                                       'Parametro url',
                                       P_PARAMETRO_URL
                                      );
      Registro_Utility.SCRIVI_STRINGA (D_CHIAVE_DEFINIZIONE, 'Separatore',
                                       '|');
      Registro_Utility.SCRIVI_STRINGA (D_CHIAVE_DEFINIZIONE,
                                       'Tabella',
                                       P_TABELLA
                                      );
      Registro_Utility.SCRIVI_STRINGA (D_CHIAVE_DEFINIZIONE,
                                       'fld_close',
                                       NVL(P_FLD_CLOSE,'cmsfolderclosed.gif')
                                      );
      Registro_Utility.SCRIVI_STRINGA (D_CHIAVE_DEFINIZIONE,
                                       'fld_open',
                                       NVL(P_FLD_OPEN,'cmsfolderopen.gif')
                                      );
      Registro_Utility.SCRIVI_STRINGA (D_CHIAVE_DEFINIZIONE,
                                       'last_fld_open',
                                       NVL(P_LAST_FLD_OPEN,'cmslastfolderopen.gif')
                                      );
      Registro_Utility.SCRIVI_STRINGA (D_CHIAVE_DEFINIZIONE,
                                       'plus_node',
                                       NVL(P_PLUS_NODE,'cmspnode.gif')
                                      );
      Registro_Utility.SCRIVI_STRINGA (D_CHIAVE_DEFINIZIONE,
                                       'last_plus_node',
                                       NVL(P_LAST_PLUS_NODE,'cmsplastnode.gif')
                                      );
      Registro_Utility.SCRIVI_STRINGA (D_CHIAVE_DEFINIZIONE,
                                       'minus_node',
                                       NVL(P_MINUS_NODE,'cmsmnode.gif')
                                      );
      Registro_Utility.SCRIVI_STRINGA (D_CHIAVE_DEFINIZIONE,
                                       'last_minus_node',
                                       NVL(P_LAST_MINUS_NODE,'cmsmlastnode.gif')
                                      );
      Registro_Utility.SCRIVI_STRINGA (D_CHIAVE_DEFINIZIONE,
                                       'img_line',
                                       'cmsvertline.gif'
                                      );
      Registro_Utility.SCRIVI_STRINGA (D_CHIAVE_DEFINIZIONE,
                                       'blank',
                                       NVL(P_BLANK,'cmsblank.gif')
                                      );
      Registro_Utility.SCRIVI_STRINGA (D_CHIAVE,
                                       'Path_immagini',
                                       NVL (P_DISLOC_IMMAGINI,
                                            '../common/images/AMV/'
                                           )
                                      );
      Registro_Utility.SCRIVI_STRINGA (D_CHIAVE,
                                       'Visualizzazione dati foglia',
                                       P_MOSTRA_FOGLIA
                                      );
      Registro_Utility.SCRIVI_STRINGA (D_CHIAVE,
                                       'Riposizionamento',
                                       P_RIPOSIZIONA
                                      );
      Registro_Utility.SCRIVI_STRINGA (D_CHIAVE,
                                       'Un solo folder aperto',
                                       P_UN_FOLDER_APERTO
                                      );
      Registro_Utility.SCRIVI_STRINGA (D_CHIAVE,
                                       'Immagine Foglia',
                                       NVL(P_FOGLIA,'cmsdoc.gif')
                                      );
   END;
   PROCEDURE TREE_IMPOSTA_PREFERENZE (
      P_ALBERO        VARCHAR2,
      P_MODULO        VARCHAR2,
      P_IMMAGINI      VARCHAR2,
      P_MOSTRA_FOGLIA VARCHAR2,
      P_RIPOSIZIONA   VARCHAR2,
      P_UNO_APERTO    VARCHAR2
   )
   IS
      D_CHIAVE   T_LUNGO
         :=    'PRODUCTS/'
            || UPPER (P_MODULO)
            || '/AFC_HTML.TREE/'
            || UPPER (P_ALBERO);
   BEGIN
      Registro_Utility.SCRIVI_STRINGA (D_CHIAVE,
                                       'Path_immagini',
                                       P_IMMAGINI,
                                       NULL,
                                       FALSE
                                      );
      Registro_Utility.SCRIVI_STRINGA (D_CHIAVE,
                                       'Visualizzazione dati foglia',
                                       P_MOSTRA_FOGLIA,
                                       NULL,
                                       FALSE
                                      );
      Registro_Utility.SCRIVI_STRINGA (D_CHIAVE,
                                       'Riposizionamento',
                                       P_RIPOSIZIONA,
                                       NULL,
                                       FALSE
                                      );
      Registro_Utility.SCRIVI_STRINGA (D_CHIAVE,
                                       'Un solo folder aperto',
                                       P_UNO_APERTO,
                                       NULL,
                                       FALSE
                                      );
   END;
   FUNCTION LEGGI_REGISTRO (P_CHIAVE VARCHAR2, P_STRINGA VARCHAR2)
      RETURN VARCHAR2
   IS
      D_VALORE   T_LUNGO;
   BEGIN
      Registro_Utility.LEGGI_STRINGA (P_CHIAVE, P_STRINGA, D_VALORE);
      RETURN D_VALORE;
   END;
   PROCEDURE INIZIALIZZA_PARAMETRI (P_MODULO VARCHAR2, P_ALBERO VARCHAR2)
   IS
      D_CHIAVE               T_LUNGO
         :=    'PRODUCTS/'
            || UPPER (P_MODULO)
            || '/AFC_HTML.TREE/'
            || UPPER (P_ALBERO);
      D_CHIAVE_DEFINIZIONE   T_LUNGO := D_CHIAVE || '/DEFINIZIONE';
   BEGIN
-- parametrizzabili dall'utente
      D_VISUALIZZA_FOGLIE :=
          NVL (LEGGI_REGISTRO (D_CHIAVE, 'Visualizzazione dati foglia'), 'N');
      D_SOLO_UN_FOLDER_APERTO :=
                NVL (LEGGI_REGISTRO (D_CHIAVE, 'Un solo folder aperto'), 'S');
      D_INSERIRE_POSIZIONAMENTO :=
                     NVL (LEGGI_REGISTRO (D_CHIAVE, 'Riposizionamento'), 'N');
      D_DIRECTORY :=
         NVL (LEGGI_REGISTRO (D_CHIAVE, 'Path_immagini'),
              '../common/images/AMV/'
             );
-- impostazioni albero
      D_COLONNA :=
           NVL (LEGGI_REGISTRO (D_CHIAVE_DEFINIZIONE, 'Colonna Figlio'), 'id');
      D_COLONNA_NOME :=
           NVL (LEGGI_REGISTRO (D_CHIAVE_DEFINIZIONE, 'Colonna Nome'), 'nome');
      D_COLONNA_ORDINAMENTO :=
         NVL (LEGGI_REGISTRO (D_CHIAVE_DEFINIZIONE, 'Colonna Ordinamento'),
              'nome'
             );
      D_COLONNA_PADRE :=
         NVL (LEGGI_REGISTRO (D_CHIAVE_DEFINIZIONE, 'Colonna Padre'),
              'padre_id'
             );
      D_FUNCTION_TABELLA :=
                NVL (LEGGI_REGISTRO (D_CHIAVE_DEFINIZIONE, 'Dati foglia'), '');
      D_INCLUSIONE :=
                NVL (LEGGI_REGISTRO (D_CHIAVE_DEFINIZIONE, 'Inclusione'), 'N');
      D_PAGINA :=
         NVL (LEGGI_REGISTRO (D_CHIAVE_DEFINIZIONE, 'Pagina href'), 'Main.do');
      D_PARAMETRO_INCLUSIONE :=
         NVL (LEGGI_REGISTRO (D_CHIAVE_DEFINIZIONE, 'Parametro Inclusione'),
              'MVPG'
             );
      D_PARAMETRO_URL :=
            NVL (LEGGI_REGISTRO (D_CHIAVE_DEFINIZIONE, 'Parametro url'), 'ST')
         || '=';
      D_SEPARATORE :=
                NVL (LEGGI_REGISTRO (D_CHIAVE_DEFINIZIONE, 'Separatore'), '|');
      D_TABELLA :=
             NVL (LEGGI_REGISTRO (D_CHIAVE_DEFINIZIONE, 'Tabella'), 'tabella');
      D_FLD_CLOSE :=
            '<img border="0" src="'
         || D_DIRECTORY
         || NVL (LEGGI_REGISTRO (D_CHIAVE_DEFINIZIONE, 'fld_close'),
                 'cmsfolderclosed.gif'
                )
         || '" align="AbsMiddle">';
      D_FLD_OPEN :=
            '<img border="0" src="'
         || D_DIRECTORY
         || NVL (LEGGI_REGISTRO (D_CHIAVE_DEFINIZIONE, 'fld_open'),
                 'cmsfolderopen.gif'
                )
         || '" align="AbsMiddle">';
      D_LAST_FLD_OPEN :=
            '<img border="0" src="'
         || D_DIRECTORY
         || NVL (LEGGI_REGISTRO (D_CHIAVE_DEFINIZIONE, 'last_fld_open'),
                 'cmslastfolderopen.gif'
                )
         || '" align="AbsMiddle">';
      D_PLUS_NODE :=
            '<img border="0" src="'
         || D_DIRECTORY
         || NVL (LEGGI_REGISTRO (D_CHIAVE_DEFINIZIONE, 'plus_node'),
                 'cmspnode.gif'
                )
         || '" align="AbsMiddle">';
      D_LAST_PLUS_NODE :=
            '<img border="0" src="'
         || D_DIRECTORY
         || NVL (LEGGI_REGISTRO (D_CHIAVE_DEFINIZIONE, 'last_plus_node'),
                 'cmsplastnode.gif'
                )
         || '" align="AbsMiddle">';
      D_MINUS_NODE :=
            '<img border="0" src="'
         || D_DIRECTORY
         || NVL (LEGGI_REGISTRO (D_CHIAVE_DEFINIZIONE, 'minus_node'),
                 'cmsmnode.gif'
                )
         || '" align="AbsMiddle">';
      D_LAST_MINUS_NODE :=
            '<img border="0" src="'
         || D_DIRECTORY
         || NVL (LEGGI_REGISTRO (D_CHIAVE_DEFINIZIONE, 'last_minus_node'),
                 'cmsmlastnode.gif'
                )
         || '" align="AbsMiddle">';
      D_IMG_LINE :=
            '<img border="0" src="'
         || D_DIRECTORY
         || NVL (LEGGI_REGISTRO (D_CHIAVE_DEFINIZIONE, 'img_line'),
                 'cmsvertline.gif'
                )
         || '" align="AbsMiddle">';
      D_BLANK :=
            '<img border="0" src="'
         || D_DIRECTORY
         || NVL (LEGGI_REGISTRO (D_CHIAVE_DEFINIZIONE, 'blank'),
                 'cmsblank.gif'
                )
         || '" align="AbsMiddle">';
      D_FOGLIA :=
            '<img border="0" src="'
         || D_DIRECTORY
         || NVL (LEGGI_REGISTRO (D_CHIAVE_DEFINIZIONE, 'Immagine Foglia'),
                 'cmsdoc.gif'
                )
         || '" align="AbsMiddle">';
   END;
 /******************************************************************************
 FUNCTION CHIUDI_TABELLA
 DESCRIZIONE: Restituisce il codice html di visualizzazione dei dati
 ******************************************************************************/
   FUNCTION CHIUDI_TABELLA
      RETURN CLOB
   IS
   BEGIN
      DBMS_LOB.WRITEAPPEND (D_TABELLA_CLOB, LENGTH ('</table>'), '</table>');
      IF D_TROVATO_TABELLA = 1
      THEN
         RETURN D_TABELLA_CLOB;
      ELSE
         RETURN NULL;
      END IF;
   END;
/******************************************************************************
 PROCEDURE SCRIVI_FOGLIE
 DESCRIZIONE: Predispone codice html per scrivere le foglie
 ******************************************************************************/
    FUNCTION SCRIVI_FOGLIE (
      P_ID                   VARCHAR2,
      P_LIVELLO              NUMBER,
      P_FRATELLI             NUMBER,
      P_NUMERO_FIGLI_PADRE   NUMBER,
      P_NOME_CURSORE         VARCHAR2
   )
      RETURN CLOB
   IS
      D_CLOB      CLOB;
      D_STRINGA   VARCHAR2 (32767);
   BEGIN
      INIZIA_TABELLA (P_LIVELLO, P_FRATELLI, P_NUMERO_FIGLI_PADRE);
       Si4.SQL_EXECUTE (   'begin open '
                       || P_NOME_CURSORE
                       || '('''
                       || P_ID
                       || '''); end;');
       v_trovato := 1;
      WHILE v_trovato = 1 LOOP
       Si4.sql_execute('begin fetch ' || p_NOME_CURSORE || ' into afc_html.v_stringa;'||
                       ' if ' || p_NOME_CURSORE || '%FOUND then afc_html.v_trovato := 1;'
                       || ' else afc_html.v_trovato := 0; end if; end;'  );
       IF v_trovato = 1 THEN
         Afc_Html.inizia_riga;
         scrivi_colonna (v_stringa);
         chiudi_riga;
       END IF;
      END LOOP;
       Si4.SQL_EXECUTE (   'begin close '
                       || P_NOME_CURSORE
                       || '; end;');
--       SI4.SQL_EXECUTE (   'begin for v in '
--                        || P_NOME_CURSORE
--                        || '('''
--                        || P_ID
--                        || ''') loop '
--                        || ' inizia_riga; '
--                        || ' scrivi_colonna(v.stringa); '
--                        || ' chiudi_riga; '
--                        || ' END LOOP; end;'
--                       );
      D_CLOB := CHIUDI_TABELLA;
      RETURN D_CLOB;
      EXCEPTION WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE(  'begin if ' || P_NOME_CURSORE
                       || '%ISOPEN THEN close '
                       || P_NOME_CURSORE
                       || '; END IF; end;');
       Si4.SQL_EXECUTE (   'begin if ' || P_NOME_CURSORE
                       || '%ISOPEN THEN close '
                       || P_NOME_CURSORE
                       || '; END IF; end;');
      RETURN NULL;--sqlerrm;
   END;
   PROCEDURE DISEGNA_ALBERO (
      P_ID_STRUTTURA                           VARCHAR2,
      P_LIVELLO                                NUMBER,
      P_PATH                                   VARCHAR2,
      P_PAGE                                   VARCHAR2,
      P_IMMAGINI_LIVELLI_PRECEDENTI            VARCHAR2,
      P_CLOB                          IN OUT   CLOB
   )
   IS
      D_POS                           INTEGER := 0;
      D_LUOGO_ID                      T_CENTO;
      D_NUMERO_FIGLI_RIMANENTI        NUMBER  := 0;
      D_NUMERO_FIGLI                  NUMBER  := 0;
      D_ROW                           T_LUNGO;
      D_LEN                           NUMBER;
      D_SOTTOALBERO_CLOB              CLOB;               --:= empty_clob() ;
      D_FOGLIE_CLOB                   CLOB;               --:= empty_clob() ;
      D_STRUTTURA                     T_REF;
      D_CONTA                         T_REF;
      D_FIGLIO_ID                     T_CENTO;
      D_PADRE_ID                      T_CENTO;
      D_DENOMINAZIONE                 T_CENTO;
      D_N_FIGLI                       NUMBER  := 0;
      D_IMMAGINI_LIVELLI_PRECEDENTI   T_LUNGO := NULL;
      D_IMMAGINE_ATTUALE              T_LUNGO := NULL;
   BEGIN
      D_IMMAGINI_LIVELLI_PRECEDENTI := P_IMMAGINI_LIVELLI_PRECEDENTI;
      IF D_INCLUSIONE = 'S'
      THEN
         D_PAGINA_URL :=
            D_PAGINA || '?' || D_PARAMETRO_INCLUSIONE || '=' || P_PAGE || '&';
      ELSE
         D_PAGINA_URL := P_PAGE || '?';
      END IF;
      DBMS_LOB.CREATETEMPORARY (D_SOTTOALBERO_CLOB, TRUE, DBMS_LOB.SESSION);
      OPEN D_CONTA
       FOR    'select  nvl(count('
           || D_COLONNA
           || '),0) '
           || ' from '
           || D_TABELLA
           || ' where '
           || D_COLONNA_PADRE
           || ' = '''
           || P_ID_STRUTTURA
           || ''''
           || ' and '
           || D_COLONNA_PADRE
           || ' <> '
           || D_COLONNA
           || ' order by '
           || D_COLONNA_ORDINAMENTO;
      FETCH D_CONTA
       INTO D_NUMERO_FIGLI_RIMANENTI;
      CLOSE D_CONTA;
      OPEN D_STRUTTURA
       FOR    'select '
           || D_COLONNA
           || ' figlio_id, '
           || D_COLONNA_PADRE
           || ' padre_id, '
           || D_COLONNA_NOME
           || ' denominazione '
           || ' from '
           || D_TABELLA
           || ' where '
           || D_COLONNA_PADRE
           || ' = '''
           || P_ID_STRUTTURA
           || ''''
           || ' and '
           || D_COLONNA_PADRE
           || ' <> '
           || D_COLONNA
           || ' order by '
           || D_COLONNA_ORDINAMENTO;
      LOOP
         FETCH D_STRUTTURA
          INTO D_FIGLIO_ID, D_PADRE_ID, D_DENOMINAZIONE;
         EXIT WHEN D_STRUTTURA%NOTFOUND;
         D_POS := INSTR (P_PATH, D_SEPARATORE || D_FIGLIO_ID || D_SEPARATORE);
         OPEN D_CONTA
          FOR    'select  nvl(count('
              || D_COLONNA
              || '),0) '
              || ' from '
              || D_TABELLA
              || ' where '
              || D_COLONNA_PADRE
              || ' = '''
              || D_FIGLIO_ID
              || ''''
              || ' and '
              || D_COLONNA_PADRE
              || ' <> '
              || D_COLONNA
              || ' order by '
              || D_COLONNA_ORDINAMENTO;
         FETCH D_CONTA
          INTO D_NUMERO_FIGLI;
         CLOSE D_CONTA;
         IF D_NUMERO_FIGLI_RIMANENTI < 2
         THEN
            D_IMMAGINE_ATTUALE := D_BLANK;
            D_IMG_NODE := D_LAST_PLUS_NODE;
            D_IMG_NODE_OPEN := D_LAST_MINUS_NODE;
         ELSE
            D_IMMAGINE_ATTUALE := D_IMG_LINE;
            D_IMG_NODE := D_PLUS_NODE;
            D_IMG_NODE_OPEN := D_MINUS_NODE;
         END IF;
         -- per cartella senza sottocartelle img cartella terminale
         IF (D_NUMERO_FIGLI = 0)
         THEN
            D_IMG_OPEN := D_LAST_FLD_OPEN;
            D_IMG := D_FOGLIA;            --sn??
            IF D_NUMERO_FIGLI_RIMANENTI < 2
            THEN
                           D_IMG_NODE := D_LAST_LINE_FOGLIA;
--               D_IMG_NODE_OPEN := D_LAST_LINE_FOGLIA;
            ELSE
--               D_IMG_NODE_OPEN := D_LINE_FOGLIA;
              D_IMG_NODE := D_LINE_FOGLIA;
            END IF;
         ELSE
            D_IMG_OPEN := D_FLD_OPEN;
            D_IMG  := D_FLD_CLOSE;            --sn??
         END IF;
         IF D_INSERIRE_POSIZIONAMENTO = 'S'
         THEN
            D_POSIZIONAMENTO := '#' || D_FIGLIO_ID;
         ELSE
            D_POSIZIONAMENTO := NULL;
         END IF;
         -- link dipende dal numero di rami contemporanei aperti che voglio
         IF D_SOLO_UN_FOLDER_APERTO = 'S'
         THEN
            D_PARAMETRO_HREF_FIGLIO := D_FIGLIO_ID;
            D_PARAMETRO_HREF_PADRE := D_PADRE_ID;
         ELSE
            D_PARAMETRO_HREF_FIGLIO :=
                  LTRIM (P_PATH, D_SEPARATORE || '0' || D_SEPARATORE)
               || D_FIGLIO_ID;
            D_PARAMETRO_HREF_PADRE :=
                  LTRIM (P_PATH, D_SEPARATORE || '0' || D_SEPARATORE)
               || D_FIGLIO_ID;                                   --D_padre_id;
         END IF;
         IF D_POS > 0
         THEN                                         -- la sezione e un padre
            -- prepara disegno del figlio
            -- per ultimo record img nodo terminale
             -- per cartella senza sottocartelle img cartella terminale
         IF (D_NUMERO_FIGLI != 0) THEN
            IF D_NUMERO_FIGLI_RIMANENTI < 2
            THEN
               D_IMMAGINE_ATTUALE := D_BLANK;
               D_IMG_NODE := D_LAST_PLUS_NODE;
               D_IMG_NODE_OPEN := D_LAST_MINUS_NODE;
            ELSE
               D_IMMAGINE_ATTUALE := D_IMG_LINE;
               D_IMG_NODE := D_PLUS_NODE;
               D_IMG_NODE_OPEN := D_MINUS_NODE;
            END IF;
         ELSE -- non ci sono figli
          D_IMG_NODE := '';
         IF D_NUMERO_FIGLI_RIMANENTI < 2
            THEN
               D_IMG_NODE_OPEN := D_LAST_LINE_FOGLIA;
            ELSE
               D_IMG_NODE_OPEN := D_LINE_FOGLIA;
            END IF;
               D_IMMAGINE_ATTUALE := D_BLANK;
--               D_IMG_NODE_OPEN := D_LINE_FOGLIA;
               D_IMG_OPEN := D_FOGLIA;
         END IF;
            D_ROW :=
                  '<a name="'
               || D_FIGLIO_ID
               || '"></a><a href="'
               || D_PAGINA_URL
               || D_PARAMETRO_URL
               || D_PARAMETRO_HREF_PADRE
               || '">'
               || D_IMG_NODE_OPEN
               || '</a>'
               || D_IMG_OPEN
               || '&nbsp;'
               || '<a name="'
               || D_FIGLIO_ID
               || '"></a><a href="'
               || D_PAGINA_URL
               || D_PARAMETRO_URL
               || D_PARAMETRO_HREF_FIGLIO
               || '&ID='
               || D_FIGLIO_ID
               || D_POSIZIONAMENTO
               || '">'
               || D_DENOMINAZIONE
               || '</a>';
            -- visualizzazione dipendenti della struttura
            IF D_FUNCTION_TABELLA IS NOT NULL AND D_VISUALIZZA_FOGLIE = 'S'
            THEN
               d_foglie_clob :=  scrivi_foglie(
                      D_FIGLIO_ID, P_LIVELLO,
                    D_NUMERO_FIGLI_RIMANENTI, D_NUMERO_FIGLI,
                    D_FUNCTION_TABELLA);
--                FETCH D_CONTA
--                 INTO D_FOGLIE_CLOB;
--
--                CLOSE D_CONTA;
            END IF;
            --disegno sottoalbero solo se ho figli
            OPEN D_CONTA
             FOR    'select  nvl(count('
                 || D_COLONNA
                 || '),0) '
                 || ' from '
                 || D_TABELLA
                 || ' where '
                 || D_COLONNA_PADRE
                 || ' = '''
                 || D_FIGLIO_ID
                 || ''''
                 || ' and '
                 || D_COLONNA_PADRE
                 || ' <> '
                 || D_COLONNA
                 || ' order by '
                 || D_COLONNA_ORDINAMENTO;
            FETCH D_CONTA
             INTO D_N_FIGLI;
            CLOSE D_CONTA;
            IF D_N_FIGLI != 0
            THEN
               D_LEN := DBMS_LOB.GETLENGTH (D_SOTTOALBERO_CLOB);
               IF D_LEN > 0
               THEN
                  DBMS_LOB.TRIM (D_SOTTOALBERO_CLOB, 0);
               END IF;
               DISEGNA_ALBERO (D_FIGLIO_ID,
                               P_LIVELLO + 1,
                               P_PATH,
                               P_PAGE,
                                  D_IMMAGINI_LIVELLI_PRECEDENTI
                               || D_IMMAGINE_ATTUALE,
                               D_SOTTOALBERO_CLOB
                              );
            END IF;
            IF P_LIVELLO != 1
            THEN
               D_ROW := D_IMMAGINI_LIVELLI_PRECEDENTI || D_ROW;
            END IF;
         ELSE                                                   -- non e padre
            D_ROW :=
                  '<a name="'
               || D_FIGLIO_ID
               || '"></a><a href="'
               || D_PAGINA_URL
               || D_PARAMETRO_URL
               || D_PARAMETRO_HREF_FIGLIO
               || '&ID=0'
               || D_POSIZIONAMENTO
               || '">'
               || D_IMG_NODE
               || '</a>'
               || D_IMG
               || '&nbsp;'
               || '<a name="'
               || D_FIGLIO_ID
               || '"></a><a href="'
               || D_PAGINA_URL
               || D_PARAMETRO_URL
               || D_PARAMETRO_HREF_FIGLIO
               || '&ID='
               || D_FIGLIO_ID
               || D_POSIZIONAMENTO
               || '">'
               || D_DENOMINAZIONE
               || '</a>';
            --sn?? INIZIO
            IF (D_NUMERO_FIGLI = 0) THEN
            -- visualizzazione dipendenti della struttura
            IF D_FUNCTION_TABELLA IS NOT NULL AND D_VISUALIZZA_FOGLIE = 'S'
            THEN
               d_foglie_clob :=  scrivi_foglie(
                      D_FIGLIO_ID, P_LIVELLO,
                    D_NUMERO_FIGLI_RIMANENTI, D_NUMERO_FIGLI,
                    D_FUNCTION_TABELLA);
--                FETCH D_CONTA
--                 INTO D_FOGLIE_CLOB;
--
--                CLOSE D_CONTA;
            END IF;
           END IF;
            --Sn?? FINE
            IF P_LIVELLO != 1
            THEN
               D_ROW :=
                     D_IMMAGINI_LIVELLI_PRECEDENTI   -- || D_immagine_attuale
                  || D_ROW;
            END IF;                                                --||'<br>';
         END IF;
         D_ROW := D_ROW || '<br>';
--          FOR i IN 1 .. p_livello - 1
--          LOOP
--             IF p_num_fratelli_rimanenti < 2
--             THEN
--                d_row := d_blank || d_row;
--             ELSE
--                d_row := d_img_line || d_row;
--             END IF;
--          END LOOP;
         D_LEN := LENGTH (D_ROW);
         DBMS_LOB.WRITEAPPEND (P_CLOB, D_LEN, D_ROW);
         IF DBMS_LOB.GETLENGTH (D_FOGLIE_CLOB) > 0
            THEN
               DBMS_LOB.APPEND (P_CLOB, D_FOGLIE_CLOB);
            END IF;
         IF D_POS > 0
         THEN
         --sn??
--             IF DBMS_LOB.GETLENGTH (D_FOGLIE_CLOB) > 0
--             THEN
--                DBMS_LOB.APPEND (P_CLOB, D_FOGLIE_CLOB);
--             END IF;
            --SN??
            IF D_N_FIGLI != 0
            THEN
               DBMS_LOB.APPEND (P_CLOB, D_SOTTOALBERO_CLOB);
            END IF;
            D_LEN := DBMS_LOB.GETLENGTH (D_SOTTOALBERO_CLOB);
            IF D_LEN > 0
            THEN
               DBMS_LOB.TRIM (D_SOTTOALBERO_CLOB, 0);
            END IF;
         END IF;
         D_LEN := DBMS_LOB.GETLENGTH (D_FOGLIE_CLOB);
         IF D_LEN > 0
         THEN
            DBMS_LOB.TRIM (D_FOGLIE_CLOB, 0);
         END IF;
         D_ROW := NULL;
         D_NUMERO_FIGLI_RIMANENTI := D_NUMERO_FIGLI_RIMANENTI - 1;
      END LOOP;
      CLOSE D_STRUTTURA;
   EXCEPTION
      WHEN OTHERS
      THEN
         IF D_STRUTTURA%ISOPEN
         THEN
            CLOSE D_STRUTTURA;
         END IF;
         RAISE;
   END;
   FUNCTION GET_PATH_STRUTTURA (
      P_ID_STRUTTURA   VARCHAR2,
      P_PATH           VARCHAR2 DEFAULT NULL
   )
      RETURN VARCHAR2
   IS
      D_PATH        T_LUNGO;
      D_PADRE       NUMBER;
      D_NOME        T_CENTO := '';
      D_STRUTTURA   T_REF;
   BEGIN
      OPEN D_STRUTTURA
       FOR    'select '
           || D_COLONNA_PADRE
           || ' from '
           || D_TABELLA
           || ' where '
           || D_COLONNA
           || ' = '''
           || P_ID_STRUTTURA
           || ''' and '
           || D_COLONNA_PADRE
           || ' <> '
           || D_COLONNA;
      FETCH D_STRUTTURA
       INTO D_PADRE;
      IF D_STRUTTURA%NOTFOUND
      THEN
         IF P_ID_STRUTTURA IS NOT NULL AND P_ID_STRUTTURA != '0'
         THEN
            RETURN P_ID_STRUTTURA || D_SEPARATORE;
         ELSE
            RETURN D_SEPARATORE || '0' || D_SEPARATORE;
         END IF;
      END IF;
      CLOSE D_STRUTTURA;
      D_PATH :=
         GET_PATH_STRUTTURA (D_PADRE, P_PATH) || P_ID_STRUTTURA
         || D_SEPARATORE;
      RETURN D_PATH;
   END GET_PATH_STRUTTURA;
   FUNCTION CREA_TREE (
      P_STRUTTURA_ID   VARCHAR2,
      P_MODULO         VARCHAR2,
      P_ALBERO         VARCHAR2,
      P_PAGE           VARCHAR2,
      P_INIZIALIZZA    VARCHAR2
   )
      RETURN CLOB
   IS
      D_PATH              T_LUNGO;
      D_ROW               T_LUNGO;
      D_ROW_START         T_LUNGO;
      D_ROW_END           T_LUNGO;
      D_DOCUMENT          T_LUNGO;
      D_CHILD             T_ARRAY;
      D_CHILD_RIMANENTI   T_ARRAY;
      D_TEMP              T_LUNGO;
      D_NFIGLI            NUMBER          := 0;
      D_PADRE             NUMBER;
      D_PADRE2            NUMBER;
      D_PAGE              T_CENTO;
--      I                   NUMBER          := 0;
--      J                   NUMBER          := 0;
      D_NROWS             NUMBER          := 0;
      D_NCURRENT          NUMBER          := 0;
-- gestione tramite clob
      D_AMOUNT            BINARY_INTEGER  := 32767;
      D_CHAR              T_LUNGO;
      D_CLOB              CLOB            := EMPTY_CLOB ();
      D_CLOB1             CLOB            := EMPTY_CLOB ();
      D_CHILD_RIMANENTI   NUMBER;
      D_LIVELLO           NUMBER          := 1;
      D_ID_STRUTTURA      VARCHAR2 (2000);
      D_NVL_P_STRUTTURA_ID  VARCHAR2 (2000) := NVL (P_STRUTTURA_ID, 0);
      D_FIGLI             NUMBER;
      D_POS               NUMBER          := 0;
      D_ULTIMO            T_LUNGO;
      D_SENZA_ULTIMO      T_LUNGO;
   BEGIN
      IF NVL (P_INIZIALIZZA, 'NO') = 'SI'
      THEN
         INIZIALIZZA_PARAMETRI (P_MODULO, P_ALBERO);
      END IF;
      IF INSTR (P_PAGE, '/') = 1
      THEN
         D_PAGE := '..' || P_PAGE;
      ELSE
         D_PAGE := P_PAGE;
      END IF;
      IF INSTR (D_PAGE, '.') > 0
      THEN
         D_PAGE := SUBSTR (D_PAGE, 1, INSTR (D_PAGE, '.') - 1);
      END IF;
-- inizializzazione CLOB
      DBMS_LOB.CREATETEMPORARY (D_CLOB, TRUE, DBMS_LOB.SESSION);
      DBMS_LOB.CREATETEMPORARY (D_CLOB1, TRUE, DBMS_LOB.SESSION);
-- riga di intestazione albero
      D_ROW_START :=
         '<table width="100%" height="100%"><tr><td width="100%" valign="top" nowrap>';
      D_ROW_END := '</td></tr></table>';
-- nodo corrente selezionato
      IF D_SOLO_UN_FOLDER_APERTO = 'S'
      THEN
         D_PATH := GET_PATH_STRUTTURA (D_NVL_P_STRUTTURA_ID);
      ELSE
         IF P_STRUTTURA_ID IS NOT NULL
         THEN
            D_PATH := '';
            D_POS := 1;
            D_ULTIMO :=
               SUBSTR (P_STRUTTURA_ID,
                       INSTR (P_STRUTTURA_ID, D_SEPARATORE, -1, 1) + 1
                      );
            D_SENZA_ULTIMO :=
               SUBSTR (P_STRUTTURA_ID,
                       1,
                       INSTR (P_STRUTTURA_ID, D_SEPARATORE, -1, 1) - 1
                      );
            IF INSTR (D_SEPARATORE || D_SENZA_ULTIMO || D_SEPARATORE,
                      D_SEPARATORE || D_ULTIMO || D_SEPARATORE
                     ) > 0
            THEN
               D_NVL_P_STRUTTURA_ID :=
                  REPLACE (D_SEPARATORE || D_SENZA_ULTIMO || D_SEPARATORE,
                           D_SEPARATORE || D_ULTIMO || D_SEPARATORE,
                           D_SEPARATORE
                          );
            END IF;
            D_PATH :=
                  D_SEPARATORE
               || '0'
               || D_SEPARATORE
               || RTRIM (SUBSTR (D_NVL_P_STRUTTURA_ID, D_POS), D_SEPARATORE)
               || D_SEPARATORE;
         ELSE
            D_PATH := D_SEPARATORE || '0' || D_SEPARATORE;
         END IF;
      END IF;
      D_LIVELLO := 1;
      D_ID_STRUTTURA :=
                  SUBSTR (D_PATH, 2                                        --1
                                   ,
                          INSTR (D_PATH, D_SEPARATORE, 1, 2) - 2);
      -- albero per il nodo selezionato
      D_AMOUNT := LENGTH (D_ROW_START);
      DBMS_LOB.WRITEAPPEND (D_CLOB, D_AMOUNT, D_ROW_START);
      DISEGNA_ALBERO (D_ID_STRUTTURA, D_LIVELLO, D_PATH, D_PAGE, '', D_CLOB1);
      DBMS_LOB.APPEND (D_CLOB, D_CLOB1);
      D_AMOUNT := LENGTH (D_ROW_END);
      DBMS_LOB.WRITEAPPEND (D_CLOB, D_AMOUNT, D_ROW_END);
      RETURN D_CLOB;
   END CREA_TREE;
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
      RETURN CLOB
   IS
   BEGIN
      D_VISUALIZZA_FOGLIE := NVL (P_MOSTRA_FOGLIA, 'N');
      D_SOLO_UN_FOLDER_APERTO := NVL (P_UN_FOLDER_APERTO, 'S');
      D_INSERIRE_POSIZIONAMENTO := NVL (P_RIPOSIZIONA, 'N');
      D_DIRECTORY := NVL (P_DISLOC_IMMAGINI, '../common/images/AMV/');
-- impostazioni albero
      D_COLONNA := NVL (P_COLONNA, 'id');
      D_COLONNA_NOME := NVL (P_COLONNA_NOME, 'nome');
      D_COLONNA_ORDINAMENTO := NVL (P_EXPR_ORDINAMENTO, 'nome');
      D_COLONNA_PADRE := NVL (P_COLONNA_PADRE, 'padre_id');
      D_FUNCTION_TABELLA := NVL (P_FUNC_FOGLIA, '');
      D_INCLUSIONE := NVL (P_INCLUSIONE, 'N');
      D_PAGINA := NVL (P_PAGINA_HREF, 'Main.do');
      D_PARAMETRO_INCLUSIONE := NVL (P_PARAMETRO_INCLUSIONE, 'MVPG');
      D_PARAMETRO_URL := NVL (P_PARAMETRO_URL, 'ST') || '=';
      D_SEPARATORE := NVL (P_SEPARATORE, '|');
      D_TABELLA := NVL (P_TABELLA, 'tabella');
      D_FLD_CLOSE :=
            D_INIZIO_IMMAGINE
         || D_DIRECTORY
         || NVL (P_FLD_CLOSE, 'cmsfolderclosed.gif')
         || D_FINE_IMMAGINE;
      D_FLD_OPEN :=
            D_INIZIO_IMMAGINE
         || D_DIRECTORY
         || NVL (P_FLD_OPEN, 'cmsfolderopen.gif')
         || D_FINE_IMMAGINE;
      D_LAST_FLD_OPEN :=
            D_INIZIO_IMMAGINE
         || D_DIRECTORY
         || NVL (P_LAST_FLD_OPEN, 'cmslastfolderopen.gif')
         || D_FINE_IMMAGINE;
      D_PLUS_NODE :=
            D_INIZIO_IMMAGINE
         || D_DIRECTORY
         || NVL (P_PLUS_NODE, 'cmspnode.gif')
         || D_FINE_IMMAGINE;
      D_LAST_PLUS_NODE :=
            D_INIZIO_IMMAGINE
         || D_DIRECTORY
         || NVL (P_LAST_PLUS_NODE, 'cmsplastnode.gif')
         || D_FINE_IMMAGINE;
      D_MINUS_NODE :=
            D_INIZIO_IMMAGINE
         || D_DIRECTORY
         || NVL (P_MINUS_NODE, 'cmsmnode.gif')
         || D_FINE_IMMAGINE;
      D_LAST_MINUS_NODE :=
            D_INIZIO_IMMAGINE
         || D_DIRECTORY
         || NVL (P_LAST_MINUS_NODE, 'cmsmlastnode.gif')
         || D_FINE_IMMAGINE;
      D_IMG_LINE :=
            D_INIZIO_IMMAGINE
         || D_DIRECTORY
         || NVL (P_IMG_LINE, 'cmsvertline.gif')
         || D_FINE_IMMAGINE;
      D_BLANK :=
            D_INIZIO_IMMAGINE
         || D_DIRECTORY
         || NVL (P_BLANK, 'cmsblank.gif')
         || D_FINE_IMMAGINE;
       D_FOGLIA :=
            D_INIZIO_IMMAGINE
         || D_DIRECTORY
         || NVL(P_FOGLIA,'cmsdoc.gif')
         || D_FINE_IMMAGINE;
      RETURN CREA_TREE (P_STRUTTURA_ID, P_MODULO, P_ALBERO, P_PAGE, 'NO');
   END;
   FUNCTION TREE (
      P_STRUTTURA_ID   VARCHAR2,
      P_MODULO         VARCHAR2,
      P_ALBERO         VARCHAR2,
      P_PAGE           VARCHAR2
   )
      RETURN CLOB
   IS
   BEGIN
      RETURN CREA_TREE (P_STRUTTURA_ID, P_MODULO, P_ALBERO, P_PAGE, 'SI');
   END;
   FUNCTION TAB_FOLDER (
      IN_LINK     IN   VARCHAR2,
      IN_HREF     IN   VARCHAR2,
      IN_ACTIVE   IN   VARCHAR2 DEFAULT 'N'
   )
      RETURN T_LUNGO
   IS
      CLASS_BEGIN   T_LUNGO;
      CLASS_BODY    T_LUNGO := 'AFCGuida';
      CLASS_END     T_LUNGO;
      CLASS_LINK    T_LUNGO;
      IMAGE         T_LUNGO := '../Themes/AFC/GuidaBlank.gif';
      OUT_HTML      T_LUNGO := NULL;
      D_TITOLO      T_LUNGO := SUBSTR (IN_LINK, INSTR (IN_LINK, ',') + 1);
      D_LINK        T_LUNGO := IN_LINK;
   BEGIN
      IF D_TITOLO != IN_LINK
      THEN
         D_LINK := SUBSTR (IN_LINK, 1, INSTR (IN_LINK, ',') - 1);
      END IF;
      D_TITOLO := ' title="' || D_TITOLO || '"';
      IF IN_ACTIVE = 'S'
      THEN
         CLASS_BODY := CLASS_BODY || 'Sel';
      END IF;
      CLASS_BEGIN := CLASS_BODY || 'L';
      CLASS_END := CLASS_BODY || 'R';
      CLASS_LINK := CLASS_BODY || 'Link';
      OUT_HTML :=
            OUT_HTML
         || ' <table cellpadding="0" cellspacing="0" border="0"><tr> '
         || ' <td align="left" valign="top" class="'
         || CLASS_BEGIN
         || '">  <img src="'
         || IMAGE
         || '" > </td> <td align="left" valign="center" nowrap class="'
         || CLASS_BODY
         || '">  <a class="'
         || CLASS_LINK
         || '"'
         || D_TITOLO
         || ' href="'
         || IN_HREF
         || '">'
         || D_LINK
         || '</a> </td> <td align="left" valign="top" class="'
         || CLASS_END
         || '">  <img src="'
         || IMAGE
         || '" > </td> </tr></table>';
      RETURN OUT_HTML;
   END;
   FUNCTION GUIDA_BAR (
      IN_LINK            IN   VARCHAR2,
      IN_HREF            IN   VARCHAR2,
      IN_SEPARATORE_SN   IN   VARCHAR2 DEFAULT 'S',
      IN_CLASSE          IN   VARCHAR2 DEFAULT NULL
   )
      RETURN T_LUNGO
   IS
      OUT_HTML       T_LUNGO := NULL;
      D_SEPARATORE   T_LUNGO;
      D_TITOLO       T_LUNGO := SUBSTR (IN_LINK, INSTR (IN_LINK, ',') + 1);
      D_LINK         T_LUNGO := IN_LINK;
      D_CLASSE       T_LUNGO := IN_CLASSE;
   BEGIN
      IF     INSTR (IN_CLASSE, 'Link') != 0
         AND INSTR (IN_CLASSE, 'Link') + 4 != LENGTH (IN_CLASSE)
      THEN
         D_CLASSE := IN_CLASSE || 'Link';
      END IF;
      IF IN_SEPARATORE_SN = 'S'
      THEN
         D_SEPARATORE := '<td> &nbsp;|&nbsp; </td>';
      END IF;
      IF D_TITOLO != IN_LINK
      THEN
         D_LINK := SUBSTR (IN_LINK, 1, INSTR (IN_LINK, ',') - 1);
      END IF;
      D_TITOLO := ' title="' || D_TITOLO || '"';
      IF IN_CLASSE IS NOT NULL
      THEN
         D_CLASSE := ' class="' || D_CLASSE || '"';
      END IF;
      OUT_HTML :=
            OUT_HTML
         || ' <table cellpadding="0" cellspacing="0" border="0"><tr> '
         || D_SEPARATORE
         || ' <td align="left" valign="center" nowrap> '
         || ' <a '
         || D_CLASSE
         || D_TITOLO
         || ' href="'
         || IN_HREF
         || '">'
         || D_LINK
         || '</a> </td> '
         || ' </tr></table>';
      RETURN OUT_HTML;
   END;
   FUNCTION FILTER_SEARCH (IN_FILTER_VALUE IN VARCHAR2)
      RETURN T_LUNGO
   IS
      IMAGE      VARCHAR2 (512);
      TITLE      VARCHAR2 (512);
      OUT_HTML   T_LUNGO        := NULL;
   BEGIN
      IF IN_FILTER_VALUE IS NULL
      THEN
         IMAGE := '../images/filtro_off.gif';
         TITLE := 'Filtro non attivo';
      ELSE
         IMAGE := '../images/filtro_on.gif';
         TITLE := 'Filtro attivo';
      END IF;
      OUT_HTML :=
            OUT_HTML
         || '<img src="'
         || IMAGE
         || '" width="18" height="18" border="0" alt="'
         || TITLE
         || '">';
      RETURN OUT_HTML;
   END;
    FUNCTION VERSIONE
       RETURN VARCHAR2
    IS
/******************************************************************************
  NOME:        VERSIONE
  DESCRIZIONE: Restituisce la versione e la data di distribuzione del package.
  PARAMETRI:   --
  RITORNA:     stringa varchar2 contenente versione e data.
******************************************************************************/
    BEGIN
       RETURN 'V1.' || D_REVISIONE;
    END VERSIONE;
END;
/

