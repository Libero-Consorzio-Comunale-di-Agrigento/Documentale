CREATE OR REPLACE PACKAGE BODY Amv_Menu AS
FUNCTION VERSIONE  RETURN VARCHAR2 IS
/******************************************************************************
 NOME:        VERSIONE
 DESCRIZIONE: Restituisce la versione e la data di distribuzione del package.
 PARAMETRI:   --
 RITORNA:     stringa varchar2 contenente versione e data.
 ECCEZIONI:   --
 ANNOTAZIONI: --
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    05/02/2004 AO     Creazione.
******************************************************************************/
BEGIN
   RETURN d_versione||' Rev.'||d_revisione;
END VERSIONE;
FUNCTION GET_PAGE
( P_TIPO    VARCHAR2
, P_STRINGA VARCHAR2
, P_INDICE  NUMBER
, P_CONTEXT VARCHAR2 DEFAULT NULL
) RETURN VARCHAR2 IS
/******************************************************************************
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    14/02/2003 AO     Prima emissione.
 1    01/08/2003 AO     Aggiunto parametro p_context per gestione link privi di suffisso ../
 2    06/10/2004 VA     Inserito ".." davanti alla pagina nel caso inizi con "/"
 3    27/05/2005 AO     Gestione link con parametri
******************************************************************************/
d_pagina  VARCHAR2(4000);
d_stringa VARCHAR2(4000);
i INTEGER;
BEGIN
   IF P_TIPO = 'P' THEN
      IF NVL(p_indice,0) = 0 THEN
         d_stringa := p_stringa;
      ELSE
         d_stringa := ','||p_stringa||',';
         d_stringa := SUBSTR(d_stringa, INSTR(d_stringa,',',1,p_indice)+1, INSTR(d_stringa,',',1,p_indice+1) - 2 - INSTR(d_stringa,',',1,p_indice)+1);
      END IF;
      IF INSTR(d_stringa,'/') = 1 THEN
         d_stringa := '..'||d_stringa;
     END IF;
      RETURN d_stringa;
   ELSIF P_TIPO = 'V' THEN
      BEGIN
         SELECT DECODE(p_context,NULL,'../','')||get_link_stringa(stringa, modulo, p_context) INTO d_pagina
           FROM AMV_VOCI
           WHERE voce = p_stringa
             --and stringa is not null
       ;
         i := INSTR(d_pagina,'?');
         IF i > 0 THEN
            d_pagina := SUBSTR(d_pagina,1,i-1);
         END IF;
         RETURN d_pagina;
      EXCEPTION
          WHEN NO_DATA_FOUND THEN
          RETURN '';
     END;
   ELSE
      RETURN d_stringa;
   END IF;
END GET_PAGE;
FUNCTION GET_PAGE
( P_VOCE    VARCHAR2
, P_CONTEXT VARCHAR2 DEFAULT NULL
) RETURN VARCHAR2 IS
BEGIN
  RETURN GET_PAGE ('V', p_voce, 0, p_context);
END GET_PAGE;
FUNCTION GET_VOCE_MENU (P_PAGINA VARCHAR2, P_PROGETTO VARCHAR2) RETURN VARCHAR2 IS
/******************************************************************************
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    14/02/2003 AO     Prima emissione.
 1    03/08/2004 AO     Gestione del campo modulo senza barra iniziale che va aggiunta
******************************************************************************/
d_voce_menu VARCHAR2(8);
BEGIN
 BEGIN
 SELECT voce
   INTO d_voce_menu
   FROM AMV_VOCI
  WHERE '/'||modulo = p_pagina
   AND progetto = p_progetto;
 EXCEPTION
    WHEN OTHERS THEN
       d_voce_menu :='';
 END;
 RETURN d_voce_menu;
END GET_VOCE_MENU;
FUNCTION GET_VOCE_ABILITAZIONE (P_ABILITAZIONE NUMBER) RETURN VARCHAR2 IS
/******************************************************************************
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    06/03/2003 AO     Prima emissione.
******************************************************************************/
d_voce_menu VARCHAR2(8);
BEGIN
 BEGIN
 SELECT voce_menu
   INTO d_voce_menu
   FROM AMV_ABILITAZIONI
  WHERE abilitazione = p_abilitazione
    AND ruolo = '*';
 EXCEPTION
    WHEN OTHERS THEN
       d_voce_menu :='';
 END;
 RETURN d_voce_menu;
END GET_VOCE_ABILITAZIONE;
FUNCTION GET_VOCE_PAGINA (P_UTENTE VARCHAR2, P_RUOLO VARCHAR2, P_MODULO VARCHAR2,P_PATH VARCHAR2) RETURN VARCHAR2 IS
/******************************************************************************
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    06/03/2003 AO     Prima emissione.
 1    17/01/2005 AO     Tolta condizione in OR
******************************************************************************/
d_voce_menu VARCHAR2(8);
BEGIN
   BEGIN
      SELECT DISTINCT v.voce voce INTO d_voce_menu
        FROM AMV_VOCI v, AMV_ABILITAZIONI a
       WHERE v.voce = a.voce_menu(+)
         AND NVL(a.ruolo,p_ruolo) = p_ruolo
         AND Amv_Menu.get_accesso_pagina(v.voce, p_path) = 1
      ;
   EXCEPTION WHEN OTHERS THEN
      NULL;
   END;
   RETURN d_voce_menu;
END GET_VOCE_PAGINA;
FUNCTION GET_TITOLO_PAGINA (P_PROGETTO VARCHAR2, P_PATH VARCHAR2) RETURN VARCHAR2 IS
/******************************************************************************
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    02/03/2005 AO     Prima emissione.
******************************************************************************/
d_titolo_menu VARCHAR2(40);
BEGIN
   BEGIN
      SELECT DISTINCT v.titolo titolo INTO d_titolo_menu
        FROM AMV_VOCI v
       WHERE Amv_Menu.get_accesso_pagina(v.voce, p_path) = 1
      ;
   EXCEPTION WHEN OTHERS THEN
      NULL;
   END;
   RETURN d_titolo_menu;
END GET_TITOLO_PAGINA;
FUNCTION GET_LINK_STRINGA
( P_STRINGA    IN  VARCHAR2
, P_PAGINA     IN  VARCHAR2 DEFAULT NULL
, P_CONTEXT    IN  VARCHAR2 DEFAULT NULL
) RETURN VARCHAR2 IS
/******************************************************************************
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    14/02/2003 AO     Prima emissione.
 1    17/09/2003 AO     Modifica per gestione link senza suffisso con DNS attivo
                        sulla directory di progetto (p_context risulta nullo)
 2    13/01/2004 AO     Corretto errore di creazione link quando d_link inizia
                      con carattere /
 3    11/11/2004 AO     Gestione p_stringa null per corretto funzionamento navigatore
******************************************************************************/
d_link          VARCHAR2(200);
BEGIN
   d_link := p_stringa;
   IF (INSTR(d_link,'://') > 0) AND (INSTR(d_link,'://') < 10) THEN
      RETURN d_link;
   ELSE
      IF (d_link IS NOT NULL) THEN
         IF INSTR(d_link,'/') = 1 THEN
          d_link := p_context||d_link;
       ELSE
            d_link := p_context||'/'||d_link;
       END IF;
      END IF;
      IF (p_pagina IS NOT NULL AND d_link IS NOT NULL) THEN
         IF INSTR(d_link,'?') > 0 THEN
            d_link := d_link||'&MVPG='||p_pagina;
        ELSE
            d_link := d_link||'?MVPG='||p_pagina;
        END IF;
      END IF;
   END IF;
   IF p_stringa IS NULL THEN
      d_link:='../common/Main.do';
   END IF;
   RETURN d_link;
END GET_LINK_STRINGA;
FUNCTION GET_REGISTRO
( P_CHIAVE  VARCHAR2
, P_STRINGA VARCHAR2
) RETURN VARCHAR2 IS
d_valore      VARCHAR2(2000);
BEGIN
BEGIN
   Registro_Utility.LEGGI_STRINGA(UPPER(P_CHIAVE), P_STRINGA, d_valore, FALSE);
EXCEPTION
   WHEN OTHERS THEN
      NULL;
END;RETURN d_valore;
END GET_REGISTRO;
FUNCTION GET_PARAM(P_STRINGA VARCHAR2, P_PNAME VARCHAR2) RETURN VARCHAR2 IS
/******************************************************************************
 NOME:        GET_PARAM
 DESCRIZIONE: Restituisce il valore del parametro indicato con P_PNAME cercandolo all'interno della
            stringa P_STRINGA (utilizzo su web dove i parametri vengono concatenati negli url come
           coppie nome=valore.
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    05/02/2003 AO     Prima emissione.
******************************************************************************/
output_string VARCHAR2(200);
pos NUMBER;
pos_separatore NUMBER;
BEGIN
    output_string := '';
    pos := INSTR(P_STRINGA, P_PNAME||'=');
    pos_separatore := INSTR(P_STRINGA, '&', pos);
   IF pos_separatore = 0 AND pos > 0 THEN         -- se P_PNAME e l'ultimo parametro non esiste una & finale
      pos_separatore := LENGTH(P_STRINGA)+1;
   END IF;
    pos := pos + LENGTH(P_PNAME) + 1;
    IF (pos > 0) THEN
       output_string := SUBSTR(P_STRINGA, pos, pos_separatore - pos);
    END IF;
    RETURN output_string;
END ;
FUNCTION GET_PARAM_VOCE(P_VOCE VARCHAR2, P_PNAME VARCHAR2) RETURN VARCHAR2 IS
/******************************************************************************
 NOME:        GET_PARAM_VOCE
 DESCRIZIONE: Restituisce il valore del parametro indicato con P_PNAME cercandolo all'interno del campo
            NOTE nella tabella AMV_VOCI per il record corrispondente alla voce P_VOCE.
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    05/02/2003 AO     Prima emissione.
******************************************************************************/
output_string VARCHAR2(2000);
d_note  VARCHAR2(2000);
BEGIN
   BEGIN
   SELECT note
     INTO d_note
    FROM AMV_VOCI
   WHERE voce = p_voce
   ;
   EXCEPTION
   WHEN NO_DATA_FOUND THEN
    output_string :='';
   END;
   output_string := GET_PARAM(d_note, p_pname);
    RETURN output_string;
END ;
FUNCTION GET_ABILITAZIONE
( P_VOCE         VARCHAR2
, P_RUOLO         VARCHAR2
, P_MODULO       VARCHAR2
, P_PADRE        NUMBER
) RETURN NUMBER IS
/******************************************************************************
 NOME:        DIRITTI_CHK
 DESCRIZIONE: <Descrizione function>
 PARAMETRI:    P_VOCE        VARCHAR2      voce di menu per la quale si vuole fare il check
               P_RUOLO       VARCHAR2      ruolo di AD4 per cui si vuole controllare che la voce sia abilitata
               P_MODULO      VARCHAR2      modulo di AD4 su cui si sta effettuando la verifica
 RITORNA:     <type> n : <Descrizione caso n>
                     m : <Descrizione caso m>
 ECCEZIONI:   nnnnn, <Descrizione eccezione>
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    16/10/2002 __     Prima emissione.
 1    08/05/2003 AO     Possibilita di avere voci sempre abilitate (p_voce null)
******************************************************************************/
d_ab            NUMBER(8);
BEGIN
   BEGIN -- Verifica Diritto accesso sulla voce
   IF p_voce IS NULL THEN
      RETURN 1;
   END IF;
      SELECT COUNT(1) INTO d_ab
           FROM AMV_ABILITAZIONI
         WHERE voce_menu = p_voce
           AND ruolo = p_ruolo
           AND modulo = p_modulo
           AND padre = p_padre
         ;
   IF d_ab = 0 THEN
     RETURN 0;
   ELSE
      RETURN 1;
   END IF;
   END;
END GET_ABILITAZIONE;
FUNCTION GET_ACCESSO_PAGINA
( P_VOCE         VARCHAR2
, P_PATH         VARCHAR2
) RETURN NUMBER IS
/******************************************************************************
 NOME:        DIRITTI_CHK
 DESCRIZIONE: Verifica se il percorso passato in P_PATH e compatibile con la voce passata in P_VOCE
 PARAMETRI:    P_VOCE        VARCHAR2      voce di menu per la quale si vuole fare il check
               P_PATH        VARCHAR2      percorso pagina corrente completo di parametri
               P_CONTEXT     VARCHAR2      contesto corrente
 RITORNA:     number : 1 compatibile
                     : 0 non compatibile
 ECCEZIONI:   nnnnn, <Descrizione eccezione>
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    09/11/2004 AO     Prima emissione.
******************************************************************************/
d_path_voce     VARCHAR2(1000);
d_servlet_path  VARCHAR2(1000);
d_parametri     VARCHAR2(1000);
BEGIN
   d_path_voce := SUBSTR(get_page(p_voce),4);
   IF INSTR(d_path_voce,'?') > 0 THEN
      d_servlet_path := SUBSTR(d_path_voce,1,INSTR(d_path_voce,'?'));
     d_parametri := SUBSTR(d_path_voce,INSTR(d_path_voce,'?'),LENGTH(d_path_voce));
   ELSE
      d_servlet_path := d_path_voce;
     d_parametri := '';
   END IF;
   IF INSTR(d_servlet_path,'?') > 0 THEN
      IF INSTR(p_path,d_servlet_path) > 0 AND INSTR(p_path, d_parametri) > 0 THEN
        RETURN 1;
     END IF;
   ELSE
      IF INSTR(p_path,d_servlet_path) > 0 THEN
        RETURN 1;
     END IF;
   END IF;
   RETURN 0;
END GET_ACCESSO_PAGINA;
FUNCTION GET_NAVIGATORE
( P_VOCE       VARCHAR2
, P_VOCE_L1    VARCHAR2
, P_TIPOLOGIA  VARCHAR2
, P_CONTEXT    VARCHAR2 DEFAULT NULL
) RETURN VARCHAR2 IS
BEGIN
RETURN GET_NAVIGATORE
( P_VOCE
, P_VOCE_L1
, ''
, ''
, P_TIPOLOGIA
, P_CONTEXT
);
END;
FUNCTION GET_NAVIGATORE
( P_VOCE       VARCHAR2
, P_VOCE_L1    VARCHAR2
, P_VOCE_L2    VARCHAR2
, P_VOCE_L3    VARCHAR2
, P_TIPOLOGIA  VARCHAR2
, P_CONTEXT    VARCHAR2 DEFAULT NULL
) RETURN VARCHAR2 IS
/******************************************************************************
 NOME:        GET_NAVIGATORE
 DESCRIZIONE: <Descrizione function>
 PARAMETRI:   P_VOCE         VARCHAR2      voce di menu selezionata
              P_VOCE_LI      VARCHAR2      voce di livello 1 selezionata
              P_VOCE_L2      VARCHAR2      voce di livello 2 selezionata
              P_VOCE_L3      VARCHAR2      voce di livello 3 selezionata
              P_PADRE        VARCHAR2      identificativo numerico dell'abilitazione padre
              P_TIPOLOGIA    VARCHAR2      identificativo numerico della tipologia o descrizione alternativa
              P_CONTEXT      VARCHAR2      context web di riferimento
 RITORNA:     VARCHAR2  : stringa che mostra il percorso di navigazione dalla Home Page
 ECCEZIONI:   nnnnn, <Descrizione eccezione>
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    16/10/2002 __     Prima emissione.
 2    30/10/2003 AO     Modificata per gestione di tutti i livelli
******************************************************************************/
sReturn   VARCHAR2(32000);
d_voce    VARCHAR2(8);
d_voce1   VARCHAR2(8);
d_voce2   VARCHAR2(8);
d_voce3   VARCHAR2(8);
d_titolo  VARCHAR2(40);
d_titolo1 VARCHAR2(40);
d_titolo2 VARCHAR2(40);
d_titolo3 VARCHAR2(40);
d_link    VARCHAR2(200);
d_link1   VARCHAR2(200);
d_link2   VARCHAR2(200);
d_link3   VARCHAR2(200);
d_url     VARCHAR2(2000);
d_home    VARCHAR2(100) := '../common/Main.do';
d_nome    VARCHAR2(40);
BEGIN
   IF p_tipologia IS NOT NULL THEN
      BEGIN
         SELECT nome
             INTO d_nome
            FROM AMV_TIPOLOGIE
            WHERE id_tipologia = TO_NUMBER(p_tipologia)
           ;
      EXCEPTION
         WHEN OTHERS THEN
              d_nome := p_tipologia;
      END;
      d_nome := REPLACE(d_nome,'<br>',' ');
     d_url := '&nbsp;<span class="AFCHeaderBarNavigator">'||d_nome||'</span>';
   ELSE
      BEGIN -- Selezione titolo voce L1
         SELECT voce, titolo, get_page(voce, p_context)
           INTO d_voce1, d_titolo1, d_link1
           FROM AMV_VOCI
         WHERE voce = p_voce_l1
         ;
      EXCEPTION WHEN NO_DATA_FOUND THEN
         d_voce1 :='';
      END;
      BEGIN -- Selezione titolo voce L2
         SELECT voce, titolo, get_page(voce, p_context)
           INTO d_voce2, d_titolo2, d_link2
           FROM AMV_VOCI
         WHERE voce = p_voce_l2
         ;
      EXCEPTION WHEN NO_DATA_FOUND THEN
         d_voce2 :='';
      END;
      BEGIN -- Selezione titolo voce L3
         SELECT voce, titolo, get_page(voce, p_context)
           INTO d_voce3, d_titolo3, d_link3
           FROM AMV_VOCI
         WHERE voce = p_voce_l3
         ;
      EXCEPTION WHEN NO_DATA_FOUND THEN
         d_voce3 :='';
      END;
      BEGIN -- Selezione titolo voce selezionata
         SELECT voce, titolo, get_page(voce, p_context)
           INTO d_voce, d_titolo, d_link
           FROM AMV_VOCI
         WHERE voce  = p_voce
         ;
      EXCEPTION WHEN NO_DATA_FOUND THEN
         d_url :='';
      END;
      d_url := '';
     -- Voce livello 1
     IF d_voce1 IS NOT NULL AND d_voce1 <> NVL(d_voce,' ') THEN
        d_link1 := d_link1||'?MVL1='||p_voce_l1||'&MVPD=0&MVVC='||p_voce_l1;
        d_link1 := SUBSTR(d_link1,1,INSTR(d_link1,'?'))||REPLACE(SUBSTR(d_link1,INSTR(d_link1,'?'),LENGTH(d_link1)),'?','&');
        d_url := '::&nbsp;<a class="AFCHeaderBarNavigator" href="'||d_link1||'" name="'||d_titolo1||'">'||d_titolo1||'</a>';
      END IF;
     -- Voce livello 2
     IF d_voce2 IS NOT NULL AND d_voce2 <> NVL(d_voce,' ') THEN
           d_link2 := d_link2||'?MVL1='||p_voce_l1||'&MVL2='||p_voce_l2||'&MVPD='||p_voce_l1||'&MVVC='||p_voce_l2;
           d_link2 := SUBSTR(d_link2,1,INSTR(d_link2,'?'))||REPLACE(SUBSTR(d_link2,INSTR(d_link2,'?'),LENGTH(d_link2)),'?','&');
           d_url := '&nbsp;::&nbsp;<a class="AFCHeaderBarNavigator" href="'||d_link2||'" name="'||d_titolo2||'">'||d_titolo2||'</a>';
      END IF;
     -- Voce livello 3
     IF d_voce3 IS NOT NULL AND d_voce3 <> NVL(d_voce,' ') THEN
           d_link3 := d_link3||'?MVL1='||p_voce_l1||'&MVL2='||p_voce_l2||'&MVL3='||p_voce_l3||'&MVPD='||p_voce_l2||'&MVVC='||p_voce_l3;
           d_link3 := SUBSTR(d_link3,1,INSTR(d_link3,'?'))||REPLACE(SUBSTR(d_link3,INSTR(d_link3,'?'),LENGTH(d_link3)),'?','&');
           d_url := '&nbsp;::&nbsp;<a class="AFCHeaderBarNavigator" href="'||d_link3||'" name="'||d_titolo3||'">'||d_titolo3||'</a>';
      END IF;
     IF d_url IS NOT NULL THEN
        IF d_titolo IS NOT NULL THEN
           d_url := d_url||'&nbsp;::&nbsp;'||d_titolo;
         END IF;
     ELSE
        d_url := d_url||d_titolo;
     END IF;
      d_url := REPLACE(d_url,'<br>',' ');
     d_url := '<span class="AFCHeaderBarNavigator">'||d_url||'&nbsp;</span>';
   END IF;
   RETURN d_url;
END GET_NAVIGATORE;
FUNCTION GET_BLOCCO
( P_UTENTE     IN  VARCHAR2
, P_ISTANZA    IN  VARCHAR2
, P_MODULO     IN  VARCHAR2
, P_VOCE       IN  VARCHAR2
, P_VOCE_L1    IN  VARCHAR2
, P_VOCE_L2    IN  VARCHAR2
, P_VOCE_L3    IN  VARCHAR2
, P_RUOLO      IN  VARCHAR2
, P_RETURNPAGE IN  VARCHAR2 DEFAULT NULL  -- MVURL
, P_BACKPAGE   IN  VARCHAR2 DEFAULT NULL  -- MVRP
, P_CONTEXT    IN  VARCHAR2 DEFAULT NULL  -- MVCONTEXT (es: /Standard)
)  RETURN CLOB IS
/******************************************************************************
 NOME:        GET_MENU
 DESCRIZIONE: Restituisce blocco di voci per il menu.
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    03/08/2004 AO     Prima emissione in sostituzione della function GET_MENU
                      Utilizza nuove classi di stile e diversa impostazione
                  creando una tabella html per ogni voce di primo livello
******************************************************************************/
d_row_start     VARCHAR2(1000):= '<table class="AFCMenuFormTable" width="150" cellspacing="0" cellpadding="2">';
d_row_int       VARCHAR2(1000);
d_row_sep       VARCHAR2(200);
d_row_end       VARCHAR2(1000):= '</table>';
d_row           VARCHAR2(1000);
d_block_change  VARCHAR2(1000):= '';
d_row_class     VARCHAR2(1000);
d_td_class      VARCHAR2(200);
d_td_style      VARCHAR2(200);
d_link_class    VARCHAR2(200);
d_menu_page     VARCHAR2(100) := 'amvadm/AdmMenu.do';
d_home_page     VARCHAR2(100) := 'common/Main.do';
d_home_page_pref VARCHAR2(100);
d_ruolo         VARCHAR2(8);
d_url           VARCHAR2(4000);
d_par           VARCHAR2(4000);
d_par_voce      VARCHAR2(4000);
d_par_level     VARCHAR2(4000);
d_par_padre     VARCHAR2(4000);
d_target        VARCHAR2(100) := '';
d_titolo        VARCHAR2(200);
d_title         VARCHAR2(400);
d_expand        VARCHAR2(200) := '<img src="../common/images/AMV/NodoExpand.gif" BORDER=0 >';
d_collapse      VARCHAR2(200) := '<img src="../common/images/AMV/NodoCollapse.gif" BORDER=0 >';
d_l0            VARCHAR2(200) := '<img src="../common/images/AMV/NodoL2.gif" BORDER="0" >';
d_l1            VARCHAR2(200) := '<img src="../common/images/AMV/NodoL3.gif" BORDER="0" >';
d_l2            VARCHAR2(200) := '<img src="../common/images/AMV/NodoL4.gif" BORDER="0" >';
dummy           NUMBER := 0;
i               NUMBER := 0;
d_espansione_menu  VARCHAR2(2);
-- gestione tramite clob
d_amount        BINARY_INTEGER := 32767;
d_char          VARCHAR2(32767);
d_clob          CLOB := EMPTY_CLOB() ;
BEGIN
   -- Impostazioni per menu senza frecce
   d_l0:='';
--   d_expand := d_l0;
--   d_collapse := d_l0;
   BEGIN -- Verifica Diritto accesso Utente
      SELECT ruolo
        INTO d_ruolo
        FROM AD4_DIRITTI_ACCESSO
      WHERE utente  = p_utente
        AND istanza = p_istanza
       AND modulo  = p_modulo
      ;
   EXCEPTION WHEN NO_DATA_FOUND THEN
      d_ruolo := p_ruolo; --to_char(null);
   END;
   -- Impostazione HomePage
   d_home_page_pref := Amvweb.get_preferenza('HomePage', p_modulo);
   d_home_page_pref :=  REPLACE(REPLACE(d_home_page_pref,'../',''),'"','');
   d_home_page := NVL(d_home_page_pref,d_home_page);
   d_row_int := Amvweb.get_preferenza('Menu intestazione', p_modulo);
-- Riga di separazione
   d_row_sep   := '<tr><td class="AFCMenuSeparatorTD" width="100%"><img src="../common/images/AMV/blank_separator.gif"></tr></td>';
   -- Riga Iniziale
--    if d_row_int is not null then
--       d_row_start := d_row_start||'<tr><td align="center" width="100%" class="AFCMenuColumnTD">'||d_row_int||'</td></tr>';
--    end if;
   d_espansione_menu := UPPER(Amvweb.get_preferenza ('Menu espansione', p_modulo));
 BEGIN
     -- VOCI DELL'ALBERO
     FOR c_voci IN (
         SELECT DISTINCT
           m1.sequenza  sequenza1
         , -999         sequenza2
         , -999         sequenza3
         , -999         sequenza4
         , vm.titolo    titolo
         , vm.stringa
         , vm.note
         , 1            livello
         , vm.tipo_voce
         , vm.tipo
         , vm.modulo    pagina
         , m1.abilitazione
         , m1.padre
         , m1.voce_menu voce1
         , ''           voce2
         , ''           voce3
         , ''           voce4
       FROM AMV_ABILITAZIONI m1
          , AMV_VOCI vm
     WHERE m1.modulo = p_modulo
       AND NVL(m1.ruolo,' ') = NVL(d_ruolo,' ')
       AND m1.padre = 0
       AND vm.voce = m1.voce_menu
       AND vm.tipo != 'A' -- Pagina non visibile a menu
     UNION
     SELECT DISTINCT
           m1.sequenza  sequenza1
         , m2.sequenza  sequenza2
         , -999            sequenza3
         , -999            sequenza4
--          , decode( m2.voce_menu
--                  , P_VOCE_L2, ''||decode(vm.tipo_voce,'N',d_collapse,d_l0)
--                          , ''||decode(vm.tipo_voce,'N',d_expand,d_l0)
--                 )||vm.titolo
         , vm.titolo
         , vm.stringa
         , vm.note
         , 2            livello
         , vm.tipo_voce
         , vm.tipo
         , vm.modulo    pagina
         , m2.abilitazione
         , m2.padre
         , m1.voce_menu voce1
         , m2.voce_menu voce2
         , ''           voce3
         , ''           voce4
       FROM AMV_ABILITAZIONI m1, AMV_ABILITAZIONI m2
          , AMV_VOCI vm
     WHERE m1.modulo = p_modulo
       AND m2.modulo = m1.modulo
       AND NVL(m1.ruolo,' ') = NVL(d_ruolo,' ')
       AND NVL(m2.ruolo,' ') = NVL(m1.ruolo,' ')
       AND m1.padre = 0
       AND m2.padre = m1.abilitazione
       AND (m1.voce_menu = P_VOCE_L1 OR d_espansione_menu = 'SI')
       AND vm.voce = m2.voce_menu
       AND vm.tipo != 'A' -- Pagina non visibile a menu
     UNION
     SELECT DISTINCT
           m1.sequenza  sequenza1
         , m2.sequenza  sequenza2
         , m3.sequenza  sequenza3
         , -999            sequenza4
--          , decode( m3.voce_menu
--                  , P_VOCE_L3,d_l1||decode(vm.tipo_voce,'N',d_collapse,'')
--                          , d_l1||decode(vm.tipo_voce,'N',d_expand,'')
--                 )||vm.titolo
         , vm.titolo
       , vm.stringa
         , vm.note
         , 3            livello
         , vm.tipo_voce
         , vm.tipo
         , vm.modulo    pagina
         , m3.abilitazione
         , m3.padre
         , m1.voce_menu voce1
         , m2.voce_menu voce2
         , m3.voce_menu voce3
         , ''           voce4
       FROM AMV_ABILITAZIONI m1, AMV_ABILITAZIONI m2, AMV_ABILITAZIONI m3
          , AMV_VOCI vm
     WHERE m1.modulo = p_modulo
       AND m2.modulo = m1.modulo
       AND m3.modulo = m2.modulo
       AND NVL(m1.ruolo,' ') = NVL(d_ruolo,' ')
       AND NVL(m2.ruolo,' ') = NVL(m1.ruolo,' ')
       AND NVL(m3.ruolo,' ') = NVL(m2.ruolo,' ')
       AND m1.padre = 0
       AND m2.padre = m1.abilitazione
       AND m3.padre = m2.abilitazione
       AND m2.voce_menu = P_VOCE_L2
       AND vm.voce = m3.voce_menu
       AND vm.tipo != 'A' -- Pagina non visibile a menu
     UNION
     SELECT DISTINCT
          m1.sequenza  sequenza1
         , m2.sequenza  sequenza2
         , m3.sequenza  sequenza3
         , m4.sequenza  sequenza4
--          , d_l1||d_l2||decode(vm.tipo_voce,'N',d_expand,'')||vm.titolo
         , vm.titolo
       , vm.stringa
         , vm.note
         , 4            livello
         , vm.tipo_voce
         , vm.tipo
         , vm.modulo    pagina
         , m4.abilitazione
         , m4.padre
         , m1.voce_menu voce1
         , m2.voce_menu voce2
         , m3.voce_menu voce3
         , m4.voce_menu voce4
      FROM AMV_ABILITAZIONI m1, AMV_ABILITAZIONI m2, AMV_ABILITAZIONI m3, AMV_ABILITAZIONI m4
          , AMV_VOCI vm
     WHERE m1.modulo = p_modulo
       AND m2.modulo = m1.modulo
       AND m3.modulo = m2.modulo
       AND m4.modulo = m3.modulo
       AND NVL(m1.ruolo,' ') = NVL(d_ruolo,' ')
       AND NVL(m2.ruolo,' ') = NVL(m1.ruolo,' ')
       AND NVL(m3.ruolo,' ') = NVL(m2.ruolo,' ')
       AND NVL(m4.ruolo,' ') = NVL(m3.ruolo,' ')
       AND m1.padre = 0
       AND m2.padre = m1.abilitazione
       AND m3.padre = m2.abilitazione
       AND m4.padre = m3.abilitazione
       AND m3.voce_menu = P_VOCE_L3
       AND vm.voce = m4.voce_menu
       AND vm.tipo != 'A' -- Pagina non visibile a menu
--      union
--      select -- Unisce voce di Ritorno al modulo chiamante
--            -999  sequenza1
--          , 0  sequenza2
--          , 0  sequenza3
--          , 0  sequenza4
--          , 'Ritorna'
--          , replace(p_backpage,'*','&')
--          , 'title=Ritorna all''applicazione precedente'
--          , 1          livello
--          , 'F'
--          , 'C'
--          , ''         pagina
--          , 1
--          , 0
--          , null       voce1
--          , null       voce2
--          , null       voce3
--          , null       voce4
--       from dual
--      where p_backpage is not null
     UNION
     SELECT -- Unisce voce di Ammnistrazione Menu se non gia presente
          0  sequenza1
         , 0  sequenza2
         , 0  sequenza3
         , 0  sequenza4
         , d_l1||'Menu'
         , d_menu_page||'?MVVC=admmenu'
         , 'title=Inizializzazione Menu'
         , 1          livello
         , 'F'
         , 'C'
         , ''         pagina
         , 1
         , 0
         , NULL       voce1
         , NULL       voce2
         , NULL       voce3
         , NULL       voce4
      FROM dual
     WHERE p_ruolo = 'AMM'
       AND NOT EXISTS
        (SELECT 1
          FROM AMV_ABILITAZIONI a, AMV_VOCI v
         WHERE v.voce = a.voce_menu
           AND a.ruolo = 'AMM'
           AND v.stringa = d_menu_page
          AND a.modulo IN (SELECT modulo
                             FROM ad4_moduli
                        WHERE progetto =
                             (SELECT progetto
                              FROM ad4_moduli
                               WHERE modulo = p_modulo
                            )
                       )
       )
     ORDER BY sequenza1,voce1,sequenza2,voce2,sequenza3,voce3,sequenza4,voce4)
     LOOP
       BEGIN
        i := i+1;
         d_row := '';
       d_row_class  := '';
       IF d_td_class = 'AFCMenuSelectedColumnTD'
       OR d_td_class = 'AFCMenuSelectedTD'
       OR d_td_class = 'AFCMenuDetailTD' THEN
          d_td_class := 'AFCMenuDetailTD';
            d_link_class := 'AFCMenuDataLink';
       ELSE
          d_td_class := 'AFCMenuDataTD';
            d_link_class := 'AFCMenuDataLink';
       END IF;
       d_block_change := '';
       d_par_voce := '';
         d_par_level := '';
       IF get_voce_abilitazione(c_voci.padre) IS NOT NULL THEN
          d_par_padre := 'MVPD='||get_voce_abilitazione(c_voci.padre);
       ELSE
          d_par_padre:= 'MVPD=0';
       END IF;
         -- Inizializzo url per tutte le voci anche di livello 1
       d_url := NVL(get_link_stringa(c_voci.stringa, c_voci.pagina, p_context),p_context||'/'||d_home_page);
       IF c_voci.livello = 1 THEN
          d_td_style := 'style="padding-left:2"';
         IF c_voci.voce1 = p_voce_l1 THEN
            d_td_class := 'AFCMenuSelectedColumnTD';
            d_link_class := 'AFCMenuNavigatorLink';
         ELSE
            d_td_class := 'AFCMenuColumnTD';
            d_link_class := 'AFCMenuNavigatorLink';
         END IF;
            IF get_abilitazione(c_voci.voce1,NVL(p_ruolo,' '),p_modulo,c_voci.padre) = 0 THEN
               d_titolo := '';
            ELSE
               d_titolo := c_voci.titolo;
               IF c_voci.voce1 IS NOT NULL THEN
               d_par_voce := 'MVVC='||c_voci.voce1;
            END IF;
            END IF;
         IF i>1 THEN
            d_block_change := d_row_sep||'</table>'||d_row_start;
         ELSE
            d_block_change := d_row_start;
         END IF;
         -- Gestione voce selezionata
            IF (c_voci.voce1 = P_VOCE) THEN
               d_row_class  := 'AFCMenuSelectedTR';
            END IF;
/*          -- Gestione espansione nodo
            if (c_voci.voce1 = P_VOCE_L1) and (c_voci.tipo_voce = 'N') and (d_espansione_menu = 'NO')  then
               d_par_level := '';
            else
               d_par_level := 'MVL1='||c_voci.voce1;
            end if;
*/
            d_par_level := 'MVL1='||c_voci.voce1;
         ELSIF c_voci.livello = 2 THEN
          d_td_style := 'style="padding-left:5"';
         IF c_voci.voce2 = p_voce_l2 THEN
            d_td_class := 'AFCMenuSelectedTD';
            d_link_class := 'AFCMenuDataLink';
         END IF;
            IF get_abilitazione(c_voci.voce2,NVL(p_ruolo,' '),p_modulo,c_voci.padre) = 0 THEN
                d_titolo := '';
            ELSE
               d_titolo := c_voci.titolo;
           -- Gestione expand collapse in luogo delle decode su query
           IF c_voci.tipo_voce = 'N' THEN
              IF c_voci.voce2 = P_VOCE_L2 THEN
                 d_titolo := d_collapse||c_voci.titolo;
              ELSE
                 d_titolo := d_expand||c_voci.titolo;
              END IF;
           ELSE
              d_titolo := d_l0||c_voci.titolo;
           END IF;
           -- fine gestione expand collapse
               d_par_voce := 'MVVC='||c_voci.voce2;
            END IF;
          -- Gestione voce selezionata
            IF (c_voci.voce2 = P_VOCE) THEN
               d_row_class  := 'AFCMenuSelectedTR';
            END IF;
/*          -- Gestione espansione nodo
            if (c_voci.voce2 = P_VOCE_L2) and (c_voci.tipo_voce = 'N')  then
               d_par_level := 'MVL1='||c_voci.voce1;
            else
               d_par_level := 'MVL1='||c_voci.voce1||'&MVL2='||c_voci.voce2;
            end if;
*/
            d_par_level := 'MVL1='||c_voci.voce1||'&MVL2='||c_voci.voce2;
         ELSIF c_voci.livello = 3 THEN
          d_td_style := 'style="padding-left:10"';
         IF c_voci.voce3 = p_voce_l3 THEN
            d_td_class := 'AFCMenuSelectedTD';
            d_link_class := 'AFCMenuDataLink';
         END IF;
            IF get_abilitazione(c_voci.voce3,NVL(p_ruolo,' '),p_modulo,c_voci.padre) = 0 THEN
               d_titolo := '';
            ELSE
               d_titolo := c_voci.titolo;
            -- Gestione expand collapse in luogo delle decode su query
           IF c_voci.tipo_voce = 'N' THEN
              IF c_voci.voce3 = P_VOCE_L3 THEN
                 d_titolo := d_l0||d_collapse||c_voci.titolo;
              ELSE
                 d_titolo := d_l0||d_expand||c_voci.titolo;
              END IF;
           ELSE
              d_titolo := d_l0||d_l0||c_voci.titolo;
           END IF;
           -- fine gestione expand collapse
              d_par_voce := 'MVVC='||c_voci.voce3;
            END IF;
         -- Gestione voce selezionata
            IF (c_voci.voce3 = P_VOCE) THEN
               d_row_class  := 'AFCMenuSelectedTR';
            END IF;
/*          -- Gestione espansione nodo
            if (c_voci.voce3 = P_VOCE_L3) and (c_voci.tipo_voce = 'N')  then
               d_par_level:= 'MVL1='||c_voci.voce1||'&MVL2='||c_voci.voce2;
            else
               d_par_level:= 'MVL1='||c_voci.voce1||'&MVL2='||c_voci.voce2||'&MVL3='||c_voci.voce3;
            end if;
*/
            d_par_level:= 'MVL1='||c_voci.voce1||'&MVL2='||c_voci.voce2||'&MVL3='||c_voci.voce3;
         ELSE
          d_td_style := 'style="padding-left:15"';
         IF c_voci.voce4 = p_voce THEN
            d_td_class := 'AFCMenuSelectedTD';
            d_link_class := 'AFCMenuDataLink';
         END IF;
            d_par_level:= 'MVL1='||c_voci.voce1||'&MVL2='||c_voci.voce2||'&MVL3='||c_voci.voce3;
            IF get_abilitazione(c_voci.voce4,NVL(p_ruolo,' '),p_modulo,c_voci.padre) = 0 THEN
               d_titolo := '';
            ELSE
               d_titolo := c_voci.titolo;
           -- Gestione expand collapse in luogo delle decode su query
           IF c_voci.tipo_voce = 'N' THEN
                d_titolo := d_l1||d_expand||c_voci.titolo;
           ELSE
              d_titolo := d_l1||d_l0||c_voci.titolo;
           END IF;
           -- fine gestione expand collapse
               d_par_voce := 'MVVC='||c_voci.voce4;
            END IF;
         END IF;
         IF c_voci.tipo = 'N' THEN
            d_target := ' target="_blank "';
         ELSE
            d_target := '';
         END IF;
         d_par := d_par_padre;
       IF d_par_level IS NOT NULL THEN
          IF d_par IS NOT NULL THEN
            d_par := d_par||'&';
         END IF;
         d_par:= d_par||d_par_level;
       END IF;
       IF d_par_voce IS NOT NULL THEN
          IF d_par IS NOT NULL THEN
            d_par := d_par||'&';
         END IF;
         d_par:= d_par||d_par_voce;
       END IF;
       -- Se la voce prevede una return page (navigazione fra progetti differenti)
       -- allora il parametro p_returnpage viene aggiunto come parametro di url
        IF get_param(c_voci.note,'return') = 'SI'
        OR get_param(c_voci.note,'return') = 'si'
        OR get_param(c_voci.note,'return') = 'S'
        OR get_param(c_voci.note,'return') = 's'
       AND INSTR(p_returnpage,'MVRP=') = 0
        THEN
           d_par := d_par||'&MVRP='||'../..'||REPLACE(REPLACE(p_returnpage,'&','*'),'?','%3F');
        END IF;
       -- Se la voce appartiene ad altro modulo verifico se utente corrente ha diritto di accesso
       -- se non esiste la voce non e visibile (set d_titolo a null)
       IF get_param(c_voci.note,'modulo') IS NOT NULL THEN
         BEGIN
         SELECT d_titolo INTO d_titolo
           FROM ad4_diritti_accesso
          WHERE modulo = get_param(c_voci.note,'modulo')
            AND istanza = p_istanza
            AND utente = p_utente;
         EXCEPTION WHEN NO_DATA_FOUND THEN
               d_titolo := TO_CHAR(NULL);
            END;
       END IF;
         IF (d_titolo IS NOT NULL) THEN
            IF d_url IS NOT NULL THEN
            IF d_par IS NOT NULL AND (d_titolo != '<b>Ritorna</b>') AND (INSTR(d_url,'http://') != 1) THEN
                  IF INSTR(d_url, '?') > 0 THEN
                     d_url := d_url||'&'||d_par;
                  ELSE
                     d_url := d_url||'?'||d_par;
                  END IF;
            END IF;
               --d_url := '<a class="AFCMenuDataLink" href="'||d_url||'" title="'||get_param(c_voci.note,'title')||'" '||d_target||' >';
               d_url := '<a class="'||d_link_class||'" href="'||d_url||'" title="'||get_param(c_voci.note,'title')||'" '||d_target||' >';
            --d_row := '<tr class="'||d_row_class||'"><td class="AFCMenuDataTD" style="'||d_row_class||'" align="left" valign = "top" nowrap>'||d_url||d_titolo||'</a></td></tr>';
               d_row := '<tr class="'||d_row_class||'"><td class="'||d_td_class||'" align="left" valign = "top"'||d_td_style||'>'||d_url||d_titolo||'</a></td></tr>';
         ELSE
               --d_row := '<tr class="'||d_row_class||'"><td class="AFCMenuDataTD" align="left" valign = "top" nowrap>'||d_titolo||'</td></tr>';
               d_row := '<tr class="'||d_row_class||'"><td class="'||d_td_class||'" align="left" valign = "top" '||d_td_style||'>'||d_titolo||'</td></tr>';
         END IF;
         -- apertura della prima tabella
--          if i=1 then
--             d_row:=d_row_start||d_row;
--          end if;
            --d_char := d_char||d_row;
         d_char := d_char||d_block_change||d_row;
         END IF;
        END;
    END LOOP;
     IF d_char IS NULL AND p_ruolo = 'AMM' THEN
        d_row := '<tr><td class="AFCMenuDataTD" align="left" valign = "top"><a class="AFCMenuDataLink" href="../amvadm/AdmMenu.do">Admin Menu</a></td></tr>';
        d_char := d_row_start||d_row||d_row_sep||d_row_end;
    ELSE
        --d_char := d_row_start||d_char||d_row_end;
      d_char := d_char||d_row_sep||d_row_end;
    END IF;
-- se non ci sono voci a menu restituisco un tag che non viene visualizzato
-- poiche restituire un clob nullo genera errore a runtime
   IF i=0 THEN
      d_char := '<!-- null --'||'>';
   END IF;
-- assegnazione del clob
   d_amount := LENGTH(d_char);
   dbms_lob.createTemporary(d_clob,TRUE,dbms_lob.SESSION);
   dbms_lob.WRITE(d_clob, d_amount,1, d_char);
   RETURN d_clob;
 END;
END GET_BLOCCO;
FUNCTION GET_MENU
( P_UTENTE     IN  VARCHAR2
, P_ISTANZA    IN  VARCHAR2
, P_MODULO     IN  VARCHAR2
, P_VOCE       IN  VARCHAR2
, P_VOCE_L1    IN  VARCHAR2
, P_VOCE_L2    IN  VARCHAR2
, P_VOCE_L3    IN  VARCHAR2
, P_RUOLO      IN  VARCHAR2
, P_RETURNPAGE IN  VARCHAR2 DEFAULT NULL  -- MVURL
, P_BACKPAGE   IN  VARCHAR2 DEFAULT NULL  -- MVRP
, P_CONTEXT    IN  VARCHAR2 DEFAULT NULL  -- MVCONTEXT (es: /Standard)
)  RETURN CLOB IS
/******************************************************************************
 NOME:        GET_MENU
 DESCRIZIONE: Restituisce blocco di voci per il menu.
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    16/10/2002 __     Prima emissione.
 1    27/11/2002 AO     Gestione CLOB
 2    11/07/2003 AO     Gestione espansione primo livello e link privi del suffisso ../
 3    28/07/2003 AO     Gestione guide anche dai blocchi di menu "Altre voci"
                      e "Tutte le voci"
 4    05/08/2003 AO     Gestione preferenza HomePage
 5    30/12/2003 AO     Gestione menu senza voci
 6    17/02/2004 AO     Eliminata voce "Ritorna" per chiamate fra applicativi diversi
 7    03/08/2004 AO     MANTENUTO PER COMPATIBILITA CON VERSIONE 2003.11
******************************************************************************/
d_row_start     VARCHAR2(1000):= '<table class="AFCFormTable" width="100%" cellspacing="0" cellpadding="2px">';
d_row_int       VARCHAR2(1000);
d_row_end       VARCHAR2(1000):= '</table>';
d_row           VARCHAR2(1000);
d_row_class     VARCHAR2(1000);
d_menu_page     VARCHAR2(100) := 'amvadm/AdmMenu.do';
d_home_page     VARCHAR2(100) := 'common/Main.do';
d_home_page_pref VARCHAR2(100);
d_ruolo         VARCHAR2(8);
d_url           VARCHAR2(4000);
d_par           VARCHAR2(4000);
d_par_voce      VARCHAR2(4000);
d_par_level     VARCHAR2(4000);
d_par_padre     VARCHAR2(4000);
d_target        VARCHAR2(100) := '';
d_titolo        VARCHAR2(200);
d_title         VARCHAR2(400);
d_expand        VARCHAR2(200) := '<img src="../common/images/AMV/NodoExpand.gif" BORDER=0 >';
d_collapse      VARCHAR2(200) := '<img src="../common/images/AMV/NodoCollapse.gif" BORDER=0 >';
d_l0            VARCHAR2(200) := '<img src="../common/images/AMV/NodoL2.gif" BORDER="0" >';
d_l1            VARCHAR2(200) := '<img src="../common/images/AMV/NodoL3.gif" BORDER="0" >';
d_l2            VARCHAR2(200) := '<img src="../common/images/AMV/NodoL4.gif" BORDER="0" >';
dummy           NUMBER := 0;
i               NUMBER := 0;
d_espansione_menu  VARCHAR2(2);
-- gestione tramite clob
d_amount        BINARY_INTEGER := 32767;
d_char          VARCHAR2(32767);
d_clob          CLOB := EMPTY_CLOB() ;
BEGIN
   BEGIN -- Verifica Diritto accesso Utente
      SELECT ruolo
        INTO d_ruolo
        FROM AD4_DIRITTI_ACCESSO
      WHERE utente  = p_utente
        AND istanza = p_istanza
       AND modulo  = p_modulo
      ;
   EXCEPTION WHEN NO_DATA_FOUND THEN
      d_ruolo := p_ruolo; --to_char(null);
   END;
   -- Impostazione HomePage
   d_home_page_pref := Amvweb.get_preferenza('HomePage', p_modulo);
   d_home_page_pref :=  REPLACE(REPLACE(d_home_page_pref,'../',''),'"','');
   d_home_page := NVL(d_home_page_pref,d_home_page);
   --dbms_output.PUT_LINE(d_home_page);
   d_row_int := Amvweb.get_preferenza('Menu intestazione', p_modulo);
   IF d_row_int IS NOT NULL THEN
      d_row_start := d_row_start||'<tr><td align="center" width="100%" class="AFCColumnTD">'||d_row_int||'</td></tr>';
   END IF;
   d_espansione_menu := UPPER(Amvweb.get_preferenza_portale ('Menu espansione', p_modulo));
 BEGIN
     -- VOCI DELL'ALBERO
     FOR c_voci IN (
         SELECT DISTINCT
           m1.sequenza  sequenza1
         , -999         sequenza2
         , -999         sequenza3
         , -999         sequenza4
         , vm.titolo    titolo
         , vm.stringa
         , vm.note
         , 1            livello
         , vm.tipo_voce
         , vm.tipo
         , vm.modulo    pagina
         , m1.abilitazione
         , m1.padre
         , m1.voce_menu voce1
         , ''           voce2
         , ''           voce3
         , ''           voce4
       FROM AMV_ABILITAZIONI m1
          , AMV_VOCI vm
     WHERE m1.modulo = p_modulo
       AND NVL(m1.ruolo,' ') = NVL(d_ruolo,' ')
       AND m1.padre = 0
       AND vm.voce = m1.voce_menu
       AND vm.tipo != 'A' -- Pagina non visibile a menu
     UNION
     SELECT DISTINCT
           m1.sequenza  sequenza1
         , m2.sequenza  sequenza2
         , -999            sequenza3
         , -999            sequenza4
--          , decode( m2.voce_menu
--                  , P_VOCE_L2, ''||decode(vm.tipo_voce,'N',d_collapse,d_l0)
--                          , ''||decode(vm.tipo_voce,'N',d_expand,d_l0)
--                 )||vm.titolo
         , vm.titolo
         , vm.stringa
         , vm.note
         , 2            livello
         , vm.tipo_voce
         , vm.tipo
         , vm.modulo    pagina
         , m2.abilitazione
         , m2.padre
         , m1.voce_menu voce1
         , m2.voce_menu voce2
         , ''           voce3
         , ''           voce4
       FROM AMV_ABILITAZIONI m1, AMV_ABILITAZIONI m2
          , AMV_VOCI vm
     WHERE m1.modulo = p_modulo
       AND m2.modulo = m1.modulo
       AND NVL(m1.ruolo,' ') = NVL(d_ruolo,' ')
       AND NVL(m2.ruolo,' ') = NVL(m1.ruolo,' ')
       AND m1.padre = 0
       AND m2.padre = m1.abilitazione
       AND (m1.voce_menu = P_VOCE_L1 OR d_espansione_menu = 'SI')
       AND vm.voce = m2.voce_menu
       AND vm.tipo != 'A' -- Pagina non visibile a menu
     UNION
     SELECT DISTINCT
           m1.sequenza  sequenza1
         , m2.sequenza  sequenza2
         , m3.sequenza  sequenza3
         , -999            sequenza4
--          , decode( m3.voce_menu
--                  , P_VOCE_L3,d_l1||decode(vm.tipo_voce,'N',d_collapse,'')
--                          , d_l1||decode(vm.tipo_voce,'N',d_expand,'')
--                 )||vm.titolo
         , vm.titolo
       , vm.stringa
         , vm.note
         , 3            livello
         , vm.tipo_voce
         , vm.tipo
         , vm.modulo    pagina
         , m3.abilitazione
         , m3.padre
         , m1.voce_menu voce1
         , m2.voce_menu voce2
         , m3.voce_menu voce3
         , ''           voce4
       FROM AMV_ABILITAZIONI m1, AMV_ABILITAZIONI m2, AMV_ABILITAZIONI m3
          , AMV_VOCI vm
     WHERE m1.modulo = p_modulo
       AND m2.modulo = m1.modulo
       AND m3.modulo = m2.modulo
       AND NVL(m1.ruolo,' ') = NVL(d_ruolo,' ')
       AND NVL(m2.ruolo,' ') = NVL(m1.ruolo,' ')
       AND NVL(m3.ruolo,' ') = NVL(m2.ruolo,' ')
       AND m1.padre = 0
       AND m2.padre = m1.abilitazione
       AND m3.padre = m2.abilitazione
       AND m2.voce_menu = P_VOCE_L2
       AND vm.voce = m3.voce_menu
       AND vm.tipo != 'A' -- Pagina non visibile a menu
     UNION
     SELECT DISTINCT
          m1.sequenza  sequenza1
         , m2.sequenza  sequenza2
         , m3.sequenza  sequenza3
         , m4.sequenza  sequenza4
--          , d_l1||d_l2||decode(vm.tipo_voce,'N',d_expand,'')||vm.titolo
         , vm.titolo
       , vm.stringa
         , vm.note
         , 4            livello
         , vm.tipo_voce
         , vm.tipo
         , vm.modulo    pagina
         , m4.abilitazione
         , m4.padre
         , m1.voce_menu voce1
         , m2.voce_menu voce2
         , m3.voce_menu voce3
         , m4.voce_menu voce4
      FROM AMV_ABILITAZIONI m1, AMV_ABILITAZIONI m2, AMV_ABILITAZIONI m3, AMV_ABILITAZIONI m4
          , AMV_VOCI vm
     WHERE m1.modulo = p_modulo
       AND m2.modulo = m1.modulo
       AND m3.modulo = m2.modulo
       AND m4.modulo = m3.modulo
       AND NVL(m1.ruolo,' ') = NVL(d_ruolo,' ')
       AND NVL(m2.ruolo,' ') = NVL(m1.ruolo,' ')
       AND NVL(m3.ruolo,' ') = NVL(m2.ruolo,' ')
       AND NVL(m4.ruolo,' ') = NVL(m3.ruolo,' ')
       AND m1.padre = 0
       AND m2.padre = m1.abilitazione
       AND m3.padre = m2.abilitazione
       AND m4.padre = m3.abilitazione
       AND m3.voce_menu = P_VOCE_L3
       AND vm.voce = m4.voce_menu
       AND vm.tipo != 'A' -- Pagina non visibile a menu
--      union
--      select -- Unisce voce di Ritorno al modulo chiamante
--            -999  sequenza1
--          , 0  sequenza2
--          , 0  sequenza3
--          , 0  sequenza4
--          , 'Ritorna'
--          , replace(p_backpage,'*','&')
--          , 'title=Ritorna all''applicazione precedente'
--          , 1          livello
--          , 'F'
--          , 'C'
--          , ''         pagina
--          , 1
--          , 0
--          , null       voce1
--          , null       voce2
--          , null       voce3
--          , null       voce4
--       from dual
--      where p_backpage is not null
     UNION
     SELECT -- Unisce voce di Ammnistrazione Menu se non gia presente
          0  sequenza1
         , 0  sequenza2
         , 0  sequenza3
         , 0  sequenza4
         , d_l1||'Menu'
         , d_menu_page||'?MVVC=admmenu'
         , 'title=Inizializzazione Menu'
         , 1          livello
         , 'F'
         , 'C'
         , ''         pagina
         , 1
         , 0
         , NULL       voce1
         , NULL       voce2
         , NULL       voce3
         , NULL       voce4
      FROM dual
     WHERE p_ruolo = 'AMM'
       AND NOT EXISTS
        (SELECT 1
          FROM AMV_ABILITAZIONI a, AMV_VOCI v
         WHERE v.voce = a.voce_menu
           AND a.ruolo = 'AMM'
           AND v.stringa = d_menu_page
          AND a.modulo IN (SELECT modulo
                             FROM ad4_moduli
                        WHERE progetto =
                             (SELECT progetto
                              FROM ad4_moduli
                               WHERE modulo = p_modulo
                            )
                       )
       )
     ORDER BY sequenza1,voce1,sequenza2,voce2,sequenza3,voce3,sequenza4,voce4)
     LOOP
       BEGIN
        i := i+1;
         d_row := '';
       d_row_class  := '';
         d_par_voce := '';
         d_par_level := '';
       IF get_voce_abilitazione(c_voci.padre) IS NOT NULL THEN
          d_par_padre := 'MVPD='||get_voce_abilitazione(c_voci.padre);
       ELSE
          d_par_padre:= 'MVPD=0';
       END IF;
         -- Inizializzo url per tutte le voci anche di livello 1
       d_url := NVL(get_link_stringa(c_voci.stringa, c_voci.pagina, p_context),p_context||'/'||d_home_page);
       IF c_voci.livello = 1 THEN
            IF get_abilitazione(c_voci.voce1,NVL(p_ruolo,' '),p_modulo,c_voci.padre) = 0 THEN
               d_titolo := '';
            ELSE
               d_titolo := '<b>'||c_voci.titolo||'</b>';
               IF c_voci.voce1 IS NOT NULL THEN
               d_par_voce := 'MVVC='||c_voci.voce1;
            END IF;
            END IF;
         -- Gestione voce selezionata
            IF (c_voci.voce1 = P_VOCE) THEN
               d_row_class  := 'AFCMenuSelectedTR';
            END IF;
            IF (c_voci.voce1 = P_VOCE_L1) AND (c_voci.tipo_voce = 'N') AND (d_espansione_menu = 'NO')  THEN
               d_par_level := '';
            ELSE
               d_par_level := 'MVL1='||c_voci.voce1;
            END IF;
         ELSIF c_voci.livello = 2 THEN
            IF get_abilitazione(c_voci.voce2,NVL(p_ruolo,' '),p_modulo,c_voci.padre) = 0 THEN
                d_titolo := '';
            ELSE
               d_titolo := c_voci.titolo;
           -- Gestione expand collapse in luogo delle decode su query
           IF c_voci.tipo_voce = 'N' THEN
              IF c_voci.voce2 = P_VOCE_L2 THEN
                 d_titolo := d_collapse||c_voci.titolo;
              ELSE
                 d_titolo := d_expand||c_voci.titolo;
              END IF;
           ELSE
              d_titolo := d_l0||c_voci.titolo;
           END IF;
           -- fine gestione expand collapse
               d_par_voce := 'MVVC='||c_voci.voce2;
            END IF;
         -- Gestione voce selezionata
            IF (c_voci.voce2 = P_VOCE) THEN
               d_row_class  := 'AFCMenuSelectedTR';
            END IF;
            IF (c_voci.voce2 = P_VOCE_L2) AND (c_voci.tipo_voce = 'N')  THEN
               d_par_level := 'MVL1='||c_voci.voce1;
            ELSE
               d_par_level := 'MVL1='||c_voci.voce1||'&MVL2='||c_voci.voce2;
            END IF;
         ELSIF c_voci.livello = 3 THEN
            IF get_abilitazione(c_voci.voce3,NVL(p_ruolo,' '),p_modulo,c_voci.padre) = 0 THEN
               d_titolo := '';
            ELSE
               d_titolo := c_voci.titolo;
            -- Gestione expand collapse in luogo delle decode su query
           IF c_voci.tipo_voce = 'N' THEN
              IF c_voci.voce3 = P_VOCE_L3 THEN
                 d_titolo := d_l0||d_collapse||c_voci.titolo;
              ELSE
                 d_titolo := d_l0||d_expand||c_voci.titolo;
              END IF;
           ELSE
              d_titolo := d_l0||d_l0||c_voci.titolo;
           END IF;
           -- fine gestione expand collapse
              d_par_voce := 'MVVC='||c_voci.voce3;
            END IF;
         -- Gestione voce selezionata
            IF (c_voci.voce3 = P_VOCE) THEN
               d_row_class  := 'AFCMenuSelTR';
            END IF;
            IF (c_voci.voce3 = P_VOCE_L3) AND (c_voci.tipo_voce = 'N')  THEN
               d_par_level:= 'MVL1='||c_voci.voce1||'&MVL2='||c_voci.voce2;
            ELSE
               d_par_level:= 'MVL1='||c_voci.voce1||'&MVL2='||c_voci.voce2||'&MVL3='||c_voci.voce3;
            END IF;
         ELSE
            d_par_level:= 'MVL1='||c_voci.voce1||'&MVL2='||c_voci.voce2||'&MVL3='||c_voci.voce3;
            IF get_abilitazione(c_voci.voce4,NVL(p_ruolo,' '),p_modulo,c_voci.padre) = 0 THEN
               d_titolo := '';
            ELSE
               d_titolo := c_voci.titolo;
           -- Gestione expand collapse in luogo delle decode su query
           IF c_voci.tipo_voce = 'N' THEN
                d_titolo := d_l1||d_expand||c_voci.titolo;
           ELSE
              d_titolo := d_l1||d_l0||c_voci.titolo;
           END IF;
           -- fine gestione expand collapse
               d_par_voce := 'MVVC='||c_voci.voce4;
            END IF;
         END IF;
         IF c_voci.tipo = 'N' THEN
            d_target := ' target="_blank "';
         ELSE
            d_target := '';
         END IF;
         d_par := d_par_padre;
       IF d_par_level IS NOT NULL THEN
          IF d_par IS NOT NULL THEN
            d_par := d_par||'&';
         END IF;
         d_par:= d_par||d_par_level;
       END IF;
       IF d_par_voce IS NOT NULL THEN
          IF d_par IS NOT NULL THEN
            d_par := d_par||'&';
         END IF;
         d_par:= d_par||d_par_voce;
       END IF;
       -- Se la voce prevede una return page (navigazione fra progetti differenti)
       -- allora il parametro p_returnpage viene aggiunto come parametro di url
        IF get_param(c_voci.note,'return') = 'SI'
        OR get_param(c_voci.note,'return') = 'si'
        OR get_param(c_voci.note,'return') = 'S'
        OR get_param(c_voci.note,'return') = 's'
       AND INSTR(p_returnpage,'MVRP=') = 0
        THEN
           d_par := d_par||'&MVRP='||'../..'||REPLACE(REPLACE(p_returnpage,'&','*'),'?','%3F');
        END IF;
       -- Se la voce appartiene ad altro modulo verifico se utente corrente ha diritto di accesso
       -- se non esiste la voce non e visibile (set d_titolo a null)
       IF get_param(c_voci.note,'modulo') IS NOT NULL THEN
         BEGIN
         SELECT d_titolo INTO d_titolo
           FROM ad4_diritti_accesso
          WHERE modulo = get_param(c_voci.note,'modulo')
            AND istanza = p_istanza
            AND utente = p_utente;
         EXCEPTION WHEN NO_DATA_FOUND THEN
               d_titolo := TO_CHAR(NULL);
            END;
       END IF;
         IF (d_titolo IS NOT NULL) THEN
            IF d_url IS NOT NULL THEN
            IF d_par IS NOT NULL AND (d_titolo != '<b>Ritorna</b>') AND (INSTR(d_url,'http://') != 1) THEN
                  IF INSTR(d_url, '?') > 0 THEN
                     d_url := d_url||'&'||d_par;
                  ELSE
                     d_url := d_url||'?'||d_par;
                  END IF;
            END IF;
               d_url := '<a class="AFCLeftMenu" href="'||d_url||'" title="'||get_param(c_voci.note,'title')||'" '||d_target||' >';
               d_row := '<tr class="'||d_row_class||'"><td class="AFCDataTD" style="'||d_row_class||'" align="left" valign = "top" nowrap>'||d_url||d_titolo||'</a></td></tr>';
            ELSE
               d_row := '<tr class="'||d_row_class||'"><td class="AFCDataTD" align="left" valign = "top" nowrap><span class="AFCLeftMenu">'||d_titolo||'</span></td></tr>';
            END IF;
            d_char := d_char||d_row;
         END IF;
        END;
    END LOOP;
     IF d_char IS NULL AND p_ruolo = 'AMM' THEN
        d_row := '<tr><td class="AFCDataTD" align="left" valign = "top"><a class="AFCLeftMenu" href="../amvadm/AdmMenu.do">Admin Menu</a></td></tr>';
        d_char := d_row_start||d_row||d_row_end;
    ELSE
        d_char := d_row_start||d_char||d_row_end;
    END IF;
-- se non ci sono voci a menu restituisco un tag che non viene visualizzato
-- poiche restituire un clob nullo genera errore a runtime
   IF i=0 THEN
      d_char := '<!-- NULL --'||'>';
   END IF;
-- assegnazione del clob
   d_amount := LENGTH(d_char);
   dbms_lob.createTemporary(d_clob,TRUE,dbms_lob.SESSION);
   dbms_lob.WRITE(d_clob, d_amount,1, d_char);
   RETURN d_clob;
 END;
END GET_MENU;
FUNCTION GET_ALBERO_MENU
( P_UTENTE    IN  VARCHAR2
, P_ISTANZA   IN  VARCHAR2
, P_MODULO    IN  VARCHAR2
, P_PROGETTO  IN  VARCHAR2
, P_VOCE_L2   IN  VARCHAR2
, P_VOCE_L3   IN  VARCHAR2
, P_RUOLO     IN  VARCHAR2
, P_VOCE_SEL  IN  VARCHAR2
, P_ID        IN  VARCHAR2
) RETURN CLOB IS
/******************************************************************************
 NOME:        GET_ALBERO_MENU
 DESCRIZIONE: Restituisce blocco di voci per l'amministrazione dei menu e delle voci disabilitate.
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    16/10/2002 __     Prima emissione.
 1    27/11/2002 AO     Gestione CLOB
 2     21/10/2003 AO      Gestione voci di diversi progetti
 3    20/01/2004 AO     Gestione scrittura su CLOB di oltre 32000 caratteri
                        per elenco tutte le voci
 4    24/01/2004 AO     Gestione scrittura su CLOB di oltre 32000 caratteri
                        per albero abilitazioni
******************************************************************************/
d_menu_page       VARCHAR2(100) := '../amvadm/AdmMenu.do';
d_guide_page      VARCHAR2(100) := '../amvadm/AdmGuide.do';
d_row_start       VARCHAR2(1000):= '<table class="AFCFormTABLE" width="100%" cellspacing="0" cellpadding="3" border="0" style="BORDER-COLLAPSE: collapse">';
d_row_amm_menu    VARCHAR2(1000);
d_row_end         VARCHAR2(1000):= '</table>';
d_ruolo           VARCHAR2(8);
d_url_menu        VARCHAR2(4000);
d_url             VARCHAR2(4000);
d_guida           VARCHAR2(100);
d_par             VARCHAR2(4000);
d_par_voce        VARCHAR2(4000);
d_par_level       VARCHAR2(4000);
d_par_padre       VARCHAR2(4000);
d_titolo          VARCHAR2(4000);
d_titolo_menu     VARCHAR2(4000);
d_font_l1         VARCHAR2(100) := '<b>';
d_font_l2         VARCHAR2(100) := '';
d_font_l3         VARCHAR2(100) := '';
d_font_l4         VARCHAR2(100) := '';
d_font_l1_ab      VARCHAR2(100) := '<b>';
d_font_l2_ab      VARCHAR2(100) := '';
d_font_l3_ab      VARCHAR2(100) := '';
d_font_l4_ab      VARCHAR2(100) := '';
d_endfont_l1      VARCHAR2(100) := '</b>';
d_endfont_l2      VARCHAR2(100) := '';
d_endfont_l3      VARCHAR2(100) := '';
d_endfont_l4      VARCHAR2(100) := '';
d_endfont_l1_ab   VARCHAR2(100) := '</b>';
d_endfont_l2_ab   VARCHAR2(100) := '';
d_endfont_l3_ab   VARCHAR2(100) := '';
d_endfont_l4_ab   VARCHAR2(100) := '';
d_expand          VARCHAR2(200) := '<img src="../common/images/AMV/NodoExpand.gif" BORDER=0 >';
d_collapse        VARCHAR2(200) := '<img src="../common/images/AMV/NodoCollapse.gif" BORDER=0 >';
d_l0              VARCHAR2(200) := '<img src="../common/images/AMV/NodoL2.gif" BORDER="0" >';
d_l1              VARCHAR2(200) := '<img src="../common/images/AMV/NodoL3.gif" BORDER="0" >';
d_l2              VARCHAR2(200) := '<img src="../common/images/AMV/NodoL4.gif" BORDER="0" >';
d_seq             NUMBER := 0;
dummy             NUMBER := 0;
d_class           VARCHAR2(100);
-- gestione tramite clob
d_amount          BINARY_INTEGER := 32767;
d_char            VARCHAR2(32767);
d_clob            CLOB := EMPTY_CLOB() ;
BEGIN
 d_ruolo := '*';
-- inizializzazione CLOB
 dbms_lob.createTemporary(d_clob,TRUE,dbms_lob.SESSION);
 BEGIN
     -- VOCI DELL'ALBERO
 IF P_ID != 1 AND P_ID !=2 THEN
     d_row_amm_menu := '<tr><td class="AFCColumnTD" align="left" width = "100%" valign = "top"><b>Struttura Menu</b></td><td class="AFCColumnTD" align="right" width = "20%" valign = "top">Guide</td></tr>';
     d_char := d_row_start||d_row_amm_menu;
     FOR c_voci IN (
         SELECT DISTINCT
           m1.sequenza  sequenza1
         , -999            sequenza2
         , -999            sequenza3
         , -999            sequenza4
         , vm.titolo    titolo
         , vm.voce
         , vm.stringa
         , 1          livello
         , vm.tipo_voce
         , m1.abilitazione
         , m1.padre
         , m1.voce_menu voce1
         , ''           voce2
         , ''           voce3
         , ''           voce4
       FROM AMV_ABILITAZIONI m1
          , AMV_VOCI vm
     WHERE m1.modulo = p_modulo
       AND NVL(m1.ruolo,' ') = NVL(d_ruolo,' ')
       AND m1.padre = 0
       AND vm.voce = m1.voce_menu
     UNION
     SELECT DISTINCT
           m1.sequenza  sequenza1
         , m2.sequenza  sequenza2
         , -999            sequenza3
         , -999            sequenza4
--          , decode( m2.voce_menu
--                  , P_VOCE_L2, ''||decode(vm.tipo_voce,'N',d_collapse,d_l0)
--                          , ''||decode(vm.tipo_voce,'N',d_expand,d_l0)
--                 )||vm.titolo
         , vm.titolo
         , vm.voce
         , vm.stringa
         , 2          livello
         , vm.tipo_voce
         , m2.abilitazione
         , m2.padre
         , m1.voce_menu voce1
         , m2.voce_menu voce2
         , ''           voce3
         , ''           voce4
       FROM AMV_ABILITAZIONI m1, AMV_ABILITAZIONI m2
          , AMV_VOCI vm
     WHERE m1.modulo = p_modulo
       AND m2.modulo = m1.modulo
       AND NVL(m1.ruolo,' ') = NVL(d_ruolo,' ')
       AND NVL(m2.ruolo,' ') = NVL(m1.ruolo,' ')
       AND m1.padre = 0
       AND m2.padre = m1.abilitazione
       AND vm.voce = m2.voce_menu
     UNION
     SELECT DISTINCT
           m1.sequenza  sequenza1
         , m2.sequenza  sequenza2
         , m3.sequenza  sequenza3
         , -999            sequenza4
--          , decode( m3.voce_menu
--                  , P_VOCE_L3,d_l0||decode(vm.tipo_voce,'N',d_collapse,d_l0)
--                          , d_l0||decode(vm.tipo_voce,'N',d_expand,d_l0)
--                 )||vm.titolo
         , vm.titolo
         , vm.voce
         , vm.stringa
         , 3          livello
         , vm.tipo_voce
         , m3.abilitazione
         , m3.padre
         , m1.voce_menu voce1
         , m2.voce_menu voce2
         , m3.voce_menu voce3
         , ''           voce4
       FROM AMV_ABILITAZIONI m1, AMV_ABILITAZIONI m2, AMV_ABILITAZIONI m3
          , AMV_VOCI vm
     WHERE m1.modulo = p_modulo
       AND m2.modulo = m1.modulo
       AND m3.modulo = m2.modulo
       AND NVL(m1.ruolo,' ') = NVL(d_ruolo,' ')
       AND NVL(m2.ruolo,' ') = NVL(m1.ruolo,' ')
       AND NVL(m3.ruolo,' ') = NVL(m2.ruolo,' ')
       AND m1.padre = 0
       AND m2.padre = m1.abilitazione
       AND m3.padre = m2.abilitazione
       AND vm.voce = m3.voce_menu
       AND m2.voce_menu = P_VOCE_L2
     UNION
     SELECT DISTINCT
          m1.sequenza  sequenza1
         , m2.sequenza  sequenza2
         , m3.sequenza  sequenza3
         , m4.sequenza  sequenza4
--          , d_l1||decode(vm.tipo_voce,'N',d_expand,d_l0)||vm.titolo
         , vm.titolo
         , vm.voce
         , vm.stringa
         , 4          livello
         , vm.tipo_voce
         , m4.abilitazione
         , m4.padre
         , m1.voce_menu voce1
         , m2.voce_menu voce2
         , m3.voce_menu voce3
         , m4.voce_menu voce4
      FROM AMV_ABILITAZIONI m1, AMV_ABILITAZIONI m2, AMV_ABILITAZIONI m3, AMV_ABILITAZIONI m4
          , AMV_VOCI vm
     WHERE m1.modulo = p_modulo
       AND m2.modulo = m1.modulo
       AND m3.modulo = m2.modulo
       AND m4.modulo = m3.modulo
       AND NVL(m1.ruolo,' ') = NVL(d_ruolo,' ')
       AND NVL(m2.ruolo,' ') = NVL(m1.ruolo,' ')
       AND NVL(m3.ruolo,' ') = NVL(m2.ruolo,' ')
       AND NVL(m4.ruolo,' ') = NVL(m3.ruolo,' ')
       AND m1.padre = 0
       AND m2.padre = m1.abilitazione
       AND m3.padre = m2.abilitazione
       AND m4.padre = m3.abilitazione
       AND m3.voce_menu = P_VOCE_L3
       AND vm.voce = m4.voce_menu
     ORDER BY sequenza1,voce1,sequenza2,voce2,sequenza3,voce3,sequenza4,voce4)
     LOOP
       BEGIN
         d_row_amm_menu := '';
         d_par_voce := '';
         d_par_level := '';
         d_par_padre := 'PD='||c_voci.padre||'&MVID='||P_ID;
         d_url_menu := NVL(get_link_stringa(c_voci.stringa),'#');
         d_url := d_menu_page||'?';
         d_class := 'class = "AFCTextEnable"';
         d_titolo := c_voci.titolo;
       IF c_voci.livello = 1 THEN
            d_par_voce := '&VC='||c_voci.voce1||'&SQ='||c_voci.sequenza1;
            d_seq := c_voci.sequenza1;
            IF get_abilitazione(c_voci.voce1,NVL(p_ruolo,' '),p_modulo,c_voci.padre) = 0 THEN
            d_class := 'class = "AFCTextDisable"';
               d_titolo := d_font_l1||d_titolo||d_endfont_l1;
               d_url_menu := '';
               d_titolo_menu := '';
               d_par_padre := d_par_padre||'&AB=-1';
            ELSE
               d_titolo := d_font_l1_ab||d_titolo||d_endfont_l1_ab;
               d_titolo_menu := d_titolo;
            d_par_padre := d_par_padre||'&AB=1';
            END IF;
         ELSIF c_voci.livello = 2 THEN
            d_par_voce := '&VC='||c_voci.voce2||'&SQ='||c_voci.sequenza2;
            d_seq := c_voci.sequenza2;
          -- Gestione expand collapse in luogo delle decode su query
          IF c_voci.tipo_voce = 'N' THEN
               IF c_voci.voce2 = P_VOCE_L2 THEN
                d_titolo := d_collapse||d_titolo;
               ELSE
              d_titolo := d_expand||d_titolo;
               END IF;
          ELSE
            d_titolo := d_l0||d_titolo;
          END IF;
          -- fine gestione expand collapse
            IF get_abilitazione(c_voci.voce2,NVL(p_ruolo,' '),p_modulo,c_voci.padre) = 0 THEN
            d_class := 'class = "AFCTextDisable"';
               d_titolo := d_font_l2||d_titolo||d_endfont_l2;
               d_url_menu :='';
               d_titolo_menu := '';
               d_par_padre := d_par_padre||'&AB=-1';
            ELSE
               d_titolo := d_font_l2_ab||d_titolo||d_endfont_l2_ab;
               d_titolo_menu := d_titolo;
            d_par_padre := d_par_padre||'&AB=1';
            END IF;
            IF (c_voci.voce2 = P_VOCE_L2) AND (c_voci.tipo_voce = 'N')  THEN
               NULL;
            ELSE
               d_par_level := d_par_level||'&L2='||c_voci.voce2;
            END IF;
         ELSIF c_voci.livello = 3 THEN
            d_par_voce := '&VC='||c_voci.voce3||'&SQ='||c_voci.sequenza3;
            d_seq := c_voci.sequenza3;
         -- Gestione expand collapse in luogo delle decode su query
         IF c_voci.tipo_voce = 'N' THEN
             IF c_voci.voce3 = P_VOCE_L3 THEN
                d_titolo := d_l0||d_collapse||d_titolo;
             ELSE
                d_titolo := d_l0||d_expand||d_titolo;
             END IF;
         ELSE
            d_titolo := d_l0||d_l0||d_titolo;
         END IF;
         -- fine gestione expand collapse
            IF get_abilitazione(c_voci.voce3,NVL(p_ruolo,' '),p_modulo,c_voci.padre) = 0 THEN
            d_class := 'class = "AFCTextDisable"';
               d_titolo := d_font_l3||d_titolo||d_endfont_l3;
               d_url_menu := '';
               d_titolo_menu := '';
               d_par_padre := d_par_padre||'&AB=-1';
            ELSE
               d_titolo := d_font_l3_ab||d_titolo||d_endfont_l3_ab;
               d_titolo_menu := d_titolo;
            d_par_padre := d_par_padre||'&AB=1';
            END IF;
            IF (c_voci.voce3 = P_VOCE_L3) AND (c_voci.tipo_voce = 'N')  THEN
               d_par_level:= '&L2='||c_voci.voce2;
            ELSE
               d_par_level:= '&L2='||c_voci.voce2||'&L3='||c_voci.voce3;
            END IF;
         ELSE
            d_par_voce := '&VC='||c_voci.voce4||'&SQ='||c_voci.sequenza4;
            d_seq := c_voci.sequenza4;
         -- Gestione expand collapse in luogo delle decode su query
         IF c_voci.tipo_voce = 'N' THEN
             d_titolo := d_l1||d_expand||d_titolo;
         ELSE
            d_titolo := d_l1||d_l0||d_titolo;
         END IF;
         -- fine gestione expand collapse
            d_par_level:= '&L2='||c_voci.voce2||'&L3='||c_voci.voce3;
            IF get_abilitazione(c_voci.voce4,NVL(p_ruolo,' '),p_modulo,c_voci.padre) = 0 THEN
            d_class := 'class = "AFCTextDisable"';
               d_titolo := d_font_l4||d_titolo||d_endfont_l4;
               d_url_menu := '';
               d_titolo_menu := '';
               d_par_padre := d_par_padre||'&AB=-1';
            ELSE
               d_titolo := d_font_l4_ab||d_titolo||d_endfont_l4_ab;
               d_titolo_menu := d_titolo;
            d_par_padre := d_par_padre||'&AB=1';
            END IF;
         END IF;
         d_par := d_par_padre||d_par_level||d_par_voce;
         d_url := d_url||d_par;
         d_url := '<a '||d_class||' href="'||d_url||'">';
-- Verifica che la voce abbia gia delle guide associate direttamente
         dummy:= 0;
       SELECT COUNT(1)
         INTO dummy
         FROM AMV_GUIDE
        WHERE guida = c_voci.voce
       ;
       IF dummy > 0 THEN
          d_guida := 'Modifica';
       ELSE
          d_guida := 'Inserisci';
       END IF;
-- Creazione della riga di tabella HTML corrispondente alla voce
         d_row_amm_menu := '<tr><td class="AFCDataTD" align="left" width = "70%" valign = "top">'||d_url||d_titolo||'</a>&nbsp;('||d_seq||')</td><td class="AFCDataTD" align="right" width = "30%"><a href="'||d_guide_page||'?ID='||SUBSTR(d_par_voce,5)||'">'||d_guida||'</a></td></tr>';
         d_char := d_char||d_row_amm_menu;
        -- assegnazione del clob se inizio ad avvicinarmi al limite di 32000
        IF LENGTH(d_char) > 28000 THEN
           d_amount := LENGTH(d_char);
           dbms_lob.writeappend(d_clob, d_amount,d_char);
           d_char := '';
        END IF;
        END;
     END LOOP;
     d_char := d_char||'<td class="AFCFooterTD" colspan="2" >&nbsp;</td>'||d_row_end;
 END IF;
--VOCI NON IN ALBERO
 IF P_ID = 1 OR P_ID = 2 THEN
    IF P_ID = 1 THEN
       d_titolo := 'Altre voci';
    ELSE
       d_titolo := 'Tutte le voci';
    END IF;
     d_char := d_row_start;
     d_row_amm_menu := '<tr><td class="AFCColumnTD" colspan="2" align="left" width = "100%" valign = "top">'||d_titolo||'</td><td class="AFCColumnTD" align="right" width = "20%" valign = "top">Guide</td></tr>';
     d_char := d_char||d_row_amm_menu;
    SELECT COUNT(1) INTO dummy
      FROM AMV_VOCI
           WHERE (P_ID=1
                  AND NOT EXISTS
                     (SELECT 1 FROM AMV_ABILITAZIONI
                       WHERE VOCE = VOCE_MENU
                         AND MODULO = P_MODULO
                     )
                  OR P_ID=2
                )
             AND PROGETTO = P_PROGETTO
    ;
    IF dummy > 0 THEN
       BEGIN
       d_row_amm_menu := '<tr><td class="AFCDataTD" align="left" valign = "top" nowrap><a href="'||d_menu_page||'?PD=0&SR='||NVL(p_ruolo,d_ruolo);
       IF P_ID = 1 THEN
          d_row_amm_menu := d_row_amm_menu||'&AB=-1';
       ELSE
          d_row_amm_menu := d_row_amm_menu||'&AB=0';
       END IF;
       --d_row_amm_menu := d_row_amm_menu||'&MVID='||P_ID;
      d_row_amm_menu := d_row_amm_menu||'&MVID=0';
       d_row_amm_menu := d_row_amm_menu||'&VC=';
       FOR c_voci_dis IN (
            SELECT d_row_amm_menu||VOCE||'">'||TITOLO||'</a>'||DECODE(TIPO,'M','&nbsp;&nbsp;(Menu)')||'</td>' riga
               ,voce
              ,DECODE(stringa,'','','&nbsp;&nbsp;Link:&nbsp;'||get_link_stringa(stringa,modulo)) LINK
               FROM AMV_VOCI
              WHERE (P_ID=1
                     AND NOT EXISTS
                        (SELECT 1 FROM AMV_ABILITAZIONI
                          WHERE VOCE = VOCE_MENU
                            AND MODULO = P_MODULO
                        )
                     OR P_ID=2
                )
             AND PROGETTO = P_PROGETTO
          ORDER BY TITOLO
            )
       LOOP
       -- Verifica che la voce abbia gia delle guide associate direttamente
         dummy:= 0;
       SELECT COUNT(1)
         INTO dummy
         FROM AMV_GUIDE
        WHERE guida = c_voci_dis.voce
       ;
       IF dummy > 0 THEN
          d_guida := 'Modifica';
       ELSE
          d_guida := 'Inserisci';
       END IF;
         c_voci_dis.riga:= c_voci_dis.riga||'<td width="100%" class="AFCDataTD">'||c_voci_dis.LINK||'</td><td class="AFCDataTD" align="right" width = "30%"><a href="'||d_guide_page||'?ID='||c_voci_dis.voce||'">'||d_guida||'</a></td></tr>';
          d_char := d_char||c_voci_dis.riga;
          -- assegnazione del clob se inizio ad avvicinarmi al limite di 32000
        IF LENGTH(d_char) > 28000 THEN
            d_amount := LENGTH(d_char);
              dbms_lob.writeappend(d_clob, d_amount,d_char);
           d_char := '';
        END IF;
       END LOOP;
       END;
    END IF;
    d_char := d_char||'<td class="AFCFooterTD" colspan="3" >&nbsp;</td>'||d_row_end;
 END IF;
-- assegnazione finale del clob
 d_amount := LENGTH(d_char);
 dbms_lob.writeappend(d_clob, d_amount,d_char);
 RETURN d_clob;
 END;
END GET_ALBERO_MENU;
END Amv_Menu;
/* End Procedure: AMV_MENU */
/

