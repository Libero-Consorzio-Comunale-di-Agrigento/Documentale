CREATE OR REPLACE PACKAGE BODY Amv_Documento AS
-- Attributi.
id_documento NUMBER;
id_tipologia NUMBER;
id_categoria NUMBER;
id_argomento NUMBER;
id_rilevanza NUMBER;
id_area      NUMBER;
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
FUNCTION GET_DIRITTO
( P_GRUPPO       VARCHAR2,
  P_ID_AREA      NUMBER,
  P_ID_TIPOLOGIA NUMBER
)
RETURN VARCHAR2 IS
/******************************************************************************
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    16/10/2002 __     Prima emissione.
 1    27/11/2002 AO     Gestione diritto dove gruppo non specificato
 2    30/03/2004 AO     Gestione diritti verifica e approvazione
 3    16/11/2004 AO     Sostituita con richiamo function del pkg AMV_AREA
******************************************************************************/
BEGIN
   RETURN Amv_Area.get_diritto_gruppo(p_gruppo, p_id_area, p_id_tipologia);
END GET_DIRITTO;
FUNCTION GET_DIRITTO_AREA
( P_UTENTE       VARCHAR2,
  P_ID_AREA NUMBER,
  P_ID_TIPOLOGIA NUMBER
)
RETURN VARCHAR2 IS
/******************************************************************************
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    04/12/2002 __     Prima emissione.
 1    16/11/2004 AO     Sostituita con richiamo function del pkg AMV_AREA
******************************************************************************/
BEGIN
   RETURN Amv_Area.get_diritto_utente(p_utente, p_id_area, p_id_tipologia);
END GET_DIRITTO_AREA;
FUNCTION GET_GRUPPI_AREA
( P_ID_AREA NUMBER,
  P_ID_TIPOLOGIA NUMBER
)
RETURN VARCHAR2 IS
/******************************************************************************
 ANNOTAZIONI: Restituisce elenco gruppi aventi diritto non nullo su area passsata
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    09/07/2003 AO     Prima emissione.
******************************************************************************/
d_accesso        VARCHAR2(1);
d_gruppi_return  VARCHAR2(1000);
BEGIN
   BEGIN -- Scandisce gruppi di apparteneza dell'utente
      FOR c_gruppi IN (
        SELECT utente, nominativo
           FROM AD4_UTENTI
         WHERE tipo_utente  = 'G')
      LOOP
       BEGIN
       d_accesso := get_diritto(c_gruppi.utente, p_id_area, p_id_tipologia);
       IF  d_accesso IS NOT NULL THEN
           IF d_gruppi_return IS NULL THEN
             d_gruppi_return := d_gruppi_return||c_gruppi.nominativo;
        ELSE
             d_gruppi_return := d_gruppi_return||', '||c_gruppi.nominativo;
        END IF;
      END IF;
       END;
      END LOOP;
      RETURN d_gruppi_return;
   END;
END GET_GRUPPI_AREA;
FUNCTION GET_DIRITTO_DOC
( P_UTENTE       VARCHAR2
, P_ID_DOCUMENTO NUMBER
, P_REVISIONE    NUMBER DEFAULT 0
)
RETURN VARCHAR2 IS
/******************************************************************************
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    16/10/2002 __     Prima emissione.
 1    30/03/2004 AO     Gestione diritti verifica V e approvazione A
******************************************************************************/
d_accesso         VARCHAR2(1);
d_accesso_return   VARCHAR2(1);
d_area             NUMBER(10);
d_tipologia        NUMBER(10);
BEGIN
   BEGIN -- Seleziona area e tipologia cui appartiene il documento
      SELECT id_area, id_tipologia
       INTO d_area, d_tipologia
      FROM AMV_DOCUMENTI
      WHERE id_documento = p_id_documento
       AND revisione = p_revisione
     ;
   END;
   BEGIN -- Scandisce gruppi di appartenenza dell'utente
      d_accesso_return := Amv_Area.get_diritto_utente(p_utente, d_area, d_tipologia);
     /*for c_gruppi in (
        select gruppo
           from AD4_UTENTI_GRUPPO
         where utente  = p_utente)
      loop
        begin
       d_accesso := get_diritto(c_gruppi.gruppo, d_area, d_tipologia);
       if  d_accesso_return is null then
          d_accesso_return := d_accesso;
       else
          if d_accesso = 'U' then
          d_accesso_return := d_accesso;
        elsif d_accesso = 'A' and nvl(d_accesso_return,' ') != 'U' then
          d_accesso_return := d_accesso;
        elsif d_accesso = 'V' and nvl(d_accesso_return,' ') not in ('A','U') then
            d_accesso_return := d_accesso;
          elsif d_accesso = 'C' and nvl(d_accesso_return,' ') not in ('A','V', 'U') then
                d_accesso_return := d_accesso;
        else
           null;
        end if;
       end if;
       end;
     end loop;*/
     RETURN d_accesso_return;
   END;
END GET_DIRITTO_DOC;
FUNCTION DOCUMENTO_DATA_CHK
( P_ID_DOCUMENTO NUMBER
)
RETURN NUMBER IS
/******************************************************************************
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    16/10/2002 __     Prima emissione.
******************************************************************************/
d_result          NUMBER(1);
BEGIN
   d_result := 0;
   BEGIN -- Verifica sulla data
      SELECT 1
        INTO d_result
        FROM AMV_DOCUMENTI
      WHERE id_documento = p_id_documento
       AND stato IN ('U','R')
        AND SYSDATE BETWEEN INIZIO_PUBBLICAZIONE AND NVL(FINE_PUBBLICAZIONE,SYSDATE)
      ;
   EXCEPTION WHEN NO_DATA_FOUND THEN
     d_result := 0;
   END;
   RETURN d_result;
END DOCUMENTO_DATA_CHK;
FUNCTION GET_LINK_DOCUMENTI
( P_ZONA         VARCHAR2
, P_SEQUENZA     NUMBER
, P_STILE        VARCHAR2 DEFAULT NULL
)
RETURN VARCHAR2 IS
/******************************************************************************
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    16/10/2002 __     Prima emissione.
 1     16/06/2003 AO     Aggiunto parametro p_stile
 2    11/07/2003 AO     Utilizzo di percorsi privi di "../" nel campo link
 3    18/11/2003 AO     gestione link tipo http://
******************************************************************************/
d_nome        VARCHAR2(40);
d_tipologia   NUMBER;
d_url         VARCHAR2(4000);
d_pagina      VARCHAR2(100);
d_descrizione VARCHAR2(2000);
BEGIN
   BEGIN
      SELECT nome, id_tipologia, LINK, descrizione
        INTO d_nome, d_tipologia, d_pagina, d_descrizione
        FROM AMV_TIPOLOGIE
      WHERE zona  = p_zona
        AND id_tipologia =
         (SELECT MIN(id_tipologia)
            FROM AMV_TIPOLOGIE
             WHERE zona  = p_zona
               AND sequenza = p_sequenza
         )
      ;
   EXCEPTION WHEN NO_DATA_FOUND THEN
      RETURN NULL;
   END;
   IF d_pagina IS NULL THEN
      d_pagina := '../common/AmvDocumentiRicerca.do?MVVC=amvdocu&MVTD='||d_tipologia;
   ELSE
   IF (INSTR(d_pagina,'://') = 0) THEN
        IF INSTR(d_pagina,'?') > 0 THEN
            d_pagina := d_pagina||'&';
        ELSE
            d_pagina := d_pagina||'?';
        END IF;
       d_pagina := '../'||d_pagina||'MVTD='||d_tipologia;
    END IF;
   END IF;
   d_url :='<a '||NVL(p_stile,'class="AFCNavigatorLink"')||' href="'||d_pagina||'" title="'||d_descrizione||'">'||d_nome||'</a>';
   RETURN d_url;
END GET_LINK_DOCUMENTI;
FUNCTION GET_LINK_DOCUMENTI
( P_ZONA         VARCHAR2
, P_STILE        VARCHAR2 DEFAULT NULL
)
RETURN VARCHAR2 IS
/******************************************************************************
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    16/10/2002 AO     Prima emissione.
 1     16/06/2003 AO     Aggiunto parametro p_stile
******************************************************************************/
sReturn            VARCHAR2(32000);
BEGIN
   FOR c IN (
      SELECT DISTINCT zona, sequenza
        FROM AMV_TIPOLOGIE
      WHERE zona  = p_zona
        AND sequenza IS NOT NULL
      )
   LOOP
   BEGIN
      IF sReturn IS NOT NULL THEN
        sReturn := sReturn||'&nbsp;|&nbsp;';
     END IF;
     sReturn := sReturn||GET_LINK_DOCUMENTI(c.zona,c.sequenza, p_stile);
   END;
   END LOOP;
   RETURN sReturn;
END GET_LINK_DOCUMENTI;
FUNCTION GET_BLOCCO_TIPOLOGIA (
  P_UTENTE       VARCHAR2
, P_ID_TIPOLOGIA NUMBER
, P_ID_SEZIONE   NUMBER DEFAULT 0
) RETURN CLOB IS
/******************************************************************************
 ANNOTAZIONI: -   DA FARE!!
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    23/07/2004 AO     Prima emissione.
 1    30/03/2005 AO     Ordinamento documenti sui campi sequenza e titolo
******************************************************************************/
d_row_start     VARCHAR2(1000):= '<table cellpadding="3"  cellspacing="0" class="AFCLeftFormTable">';
d_no_rows       NUMBER := 1;
d_row_end       VARCHAR2(1000):= '</table>';
d_row_sep       VARCHAR2(200);
d_row           VARCHAR2(10000);
d_url           VARCHAR2(1000);
d_href          VARCHAR2(1000);
d_nome         VARCHAR2(1000);
d_descrizione   VARCHAR2(1000);
d_zona          VARCHAR2(1);
d_zona_formato  VARCHAR2(1);
d_max_vis       NUMBER := 0;
d_immagine      VARCHAR2(200) ;
d_icona         VARCHAR2(200) ;
d_width         VARCHAR2(20);
d_height        VARCHAR2(20);
d_pos1          NUMBER := 0;
d_pos2          NUMBER := 0;
d_img_prop      VARCHAR2(200);
d_sfx           VARCHAR2(10):='Left';
d_pfx           VARCHAR2(200);
d_amount        BINARY_INTEGER := 32767;
d_char          VARCHAR2(32767);
d_clob          CLOB := EMPTY_CLOB() ;
d_img           VARCHAR2(200);
d_count_richieste  NUMBER := 0;
d_stato_richieste  VARCHAR2(1);
d_diritto_modifica NUMBER(1);
d_id_figlio        NUMBER;
d_link_figlio      VARCHAR2(200);
d_link_richieste   VARCHAR2(1000);
d_td_style      VARCHAR2(100);
d_td_class      VARCHAR2(100);
d_td_align      VARCHAR2(100);
d_link_class    VARCHAR2(100);
d_link_form      VARCHAR2(200);
d_rw         VARCHAR2(5) := '';
d_dir_upload      VARCHAR(200);
BEGIN
-- Acquisizione directory di Upload
   d_dir_upload := NVL(Amvweb.GET_PREFERENZA('Directory Upload','{Modulo}'),'common');
   dbms_lob.createTemporary(d_clob,TRUE,dbms_lob.SESSION);
   BEGIN -- estremi del blocco
      SELECT nome, descrizione, immagine, max_vis, zona, zona_formato, icona
        INTO d_nome, d_descrizione, d_immagine, d_max_vis, d_zona, d_zona_formato, d_icona
        FROM AMV_TIPOLOGIE
       WHERE id_tipologia = p_id_tipologia
       AND zona_visibilita IN ('S', DECODE(p_id_sezione, 0, 'H', 'C'))
      ;
-- Assegnazione stile
      IF d_zona = 'D' THEN
         d_sfx := 'Right';
      ELSIF d_zona = 'C' THEN
         d_sfx := 'Center';
           d_pfx := '::&nbsp;';
       -- nowhere
       IF d_icona IS NOT NULL THEN
            d_pfx := '<img src="../common/images/icone/'||d_icona||'" border="0">';
       END IF;
      END IF;
-- Assegnazione classi di stile
      d_td_class :=  'AFC'||d_sfx||'DataTD';
     d_link_class := 'AFC'||d_sfx||'DataLink';
      d_href := '<a class="AFC'||d_sfx||'NavigatorLink" href="../common/AmvDocumentiRicerca.do?MVPD=0&MVTD='||p_id_tipologia||'&MVSZ='||p_id_sezione||'">';
      IF d_immagine IS NOT NULL THEN
-- recupero proprieta immagine
         IF INSTR(d_immagine,'width=') > 0 THEN
            d_pos1:= INSTR(d_immagine,'width=');
             d_pos2:=LENGTH(d_immagine);
            d_img_prop:=SUBSTR(d_immagine,d_pos1,d_pos2);
          d_immagine:=SUBSTR(d_immagine,0,d_pos1-1);
       END IF;
       d_immagine := '<img src="../'||d_dir_upload||'/images/'||d_immagine||'" border="0" alt="'||d_nome||'" align="AbsMiddle" '||d_img_prop||'>&nbsp;';
     END IF;
      IF d_zona_formato = 'T' THEN
        IF d_zona = 'C' THEN
          IF d_immagine IS NOT NULL THEN
               d_row := '<tr><td rowspan="100" align="left" valign="top" class="AFC'||d_sfx||'ImageTD">'||d_href||d_immagine||'</a></td>';
             d_row := d_row||'<td align="left" valign="top" width="100%" class="AFC'||d_sfx||'ColumnTD">'||d_href||d_nome||'</a></td></tr>';
            ELSE
            d_row := '<tr><td align="left" valign="top" width="100%" class="AFC'||d_sfx||'ColumnTD">'||d_href||d_nome||'</td></tr>';
         END IF;
-- aggiungo descrizione della tipologia
            d_row := d_row||'<tr><td align="left" valign="top" width="100%" class="AFC'||d_sfx||'DataTD">'||d_descrizione||'</td></tr>';
         ELSE
          d_row:= '<tr><td class="AFC'||d_sfx||'ColumnTD" align="left" valign = "top">'||d_href||d_immagine||d_nome||'</a></td></tr>';
         END IF;
     ELSE
         d_row := '<tr><td align="center" width="100%" class="AFC'||d_sfx||'ImageTD">'||d_href||d_immagine||'</a></td></tr>';
      END IF;
   EXCEPTION WHEN NO_DATA_FOUND THEN
      d_no_rows := 1;
   END;
-- righe di apertura e chiusura
   d_row_start := '<div class="AFC'||d_sfx||'"><table width="100%" cellpadding="3"  cellspacing="0" class="AFC'||d_sfx||'FormTable">';
   d_row_end   := '</table></div>';
   d_row_sep   := '<tr><td class="AFC'||d_sfx||'SeparatorTD" width="100%"><img src="../common/images/AMV/blank_separator.gif"></tr></td>';
   d_char := d_row;
-- loop per i documenti normali
   FOR c IN (
   SELECT id_documento
        , revisione
        , titolo
        , LINK
      , id_tipologia
      , tipo_testo
      , immagine
      , id_documento_padre
     FROM (SELECT id_documento
                , revisione
                , titolo
                , LINK
              , id_tipologia
            , tipo_testo
            , immagine
            , id_documento_padre
           FROM AMV_DOCUMENTI
            WHERE get_diritto_area(p_utente, id_area, id_tipologia) IS NOT NULL
              AND id_tipologia = p_id_tipologia
              AND SYSDATE BETWEEN INIZIO_PUBBLICAZIONE AND NVL(FINE_PUBBLICAZIONE,SYSDATE)
              AND id_sezione = DECODE(p_id_sezione, 0, id_sezione, p_id_sezione)
              AND stato IN ('U','R')
            ORDER BY sequenza, titolo
         )
     WHERE ROWNUM <= NVL(d_max_vis,999)
   )
   LOOP
      BEGIN
        d_no_rows := 0;
         d_row:= get_riga( c.id_documento,c.revisione
                       , p_id_sezione, d_zona, 'N', c.titolo
                   , c.LINK, c.immagine
                   , c.id_tipologia, c.tipo_testo
                   , c.id_documento_padre, d_dir_upload
                   , d_td_class, d_link_class, d_pfx);
       d_char:= d_char||d_row;
      END;
   END LOOP;
   IF d_no_rows = 1 THEN
      d_char := '<!-- null --'||'>';
   ELSE
      d_char := d_row_start||d_char||d_row_sep||d_row_end;
   END IF;
   -- assegnazione del clob
   d_amount := LENGTH(d_char);
   dbms_lob.writeappend(d_clob, d_amount,d_char);
   RETURN d_clob;
END GET_BLOCCO_TIPOLOGIA;
FUNCTION GET_BLOCCO_RILEVANZA (
  P_UTENTE       VARCHAR2
, P_ID_RILEVANZA NUMBER
, P_ID_SEZIONE   NUMBER DEFAULT 0
) RETURN CLOB IS
/******************************************************************************
 ANNOTAZIONI: -   DA FARE!!
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    23/07/2004 AO     Prima emissione.
 1    30/03/2005 AO     Ordinamento documenti sui campi sequenza e titolo
******************************************************************************/
d_row_start     VARCHAR2(1000):= '<table cellpadding="3"  cellspacing="0" class="AFCLeftFormTable">';
d_no_rows       NUMBER := 1;
d_row_end       VARCHAR2(1000):= '</table>';
d_row_sep       VARCHAR2(200);
d_row           VARCHAR2(10000);
d_href          VARCHAR2(1000);
d_nome         VARCHAR2(1000);
d_descrizione   VARCHAR2(1000);
d_importanza    VARCHAR2(2);
d_zona          VARCHAR2(1);
d_zona_formato  VARCHAR2(1);
d_max_vis       NUMBER := 0;
d_immagine      VARCHAR2(200) ;
d_icona         VARCHAR2(200) ;
d_sfx           VARCHAR2(10):='Left';
d_pfx           VARCHAR2(200);
d_amount        BINARY_INTEGER := 32767;
d_char          VARCHAR2(32767);
d_clob          CLOB := EMPTY_CLOB() ;
d_clob_null     CLOB := EMPTY_CLOB() ;
d_img           VARCHAR2(200);
d_count_richieste  NUMBER := 0;
d_stato_richieste  VARCHAR2(1);
d_diritto_modifica NUMBER(1);
d_id_figlio        NUMBER;
d_link_figlio      VARCHAR2(200);
d_link_richieste   VARCHAR2(1000);
d_td_style      VARCHAR2(100);
d_td_class      VARCHAR2(100);
d_td_align      VARCHAR2(100);
d_link_class    VARCHAR2(100);
d_link_form      VARCHAR2(200);
d_rw         VARCHAR2(5) := '';
d_dir_upload      VARCHAR(200);
d_titolo_blocco VARCHAR(200);
BEGIN
-- Acquisizione directory di Upload
   d_dir_upload := NVL(Amvweb.GET_PREFERENZA('Directory Upload','{Modulo}'),'common');
   dbms_lob.createTemporary(d_clob,TRUE,dbms_lob.SESSION);
   dbms_lob.createTemporary(d_clob_null,TRUE,dbms_lob.SESSION);
   BEGIN -- estremi del blocco
      SELECT nome, importanza, immagine, max_vis, zona, zona_formato, icona
--           , zona, sequenza, link
--          , zona_visibilita, zona_formato, max_vis
        INTO d_nome, d_importanza, d_immagine, d_max_vis, d_zona, d_zona_formato, d_icona
        FROM AMV_RILEVANZE
       WHERE id_rilevanza = p_id_rilevanza
       AND zona_visibilita IN ('S', DECODE(p_id_sezione, 0, 'H', 'C'))
      ;
-- Assegnazione stile
      IF d_zona = 'D' THEN
         d_sfx := 'Right';
      ELSIF d_zona = 'C' THEN
         d_sfx := 'Center';
       IF d_importanza != 'HL' OR d_importanza IS NULL THEN
              d_pfx := '::&nbsp;';
         -- nowhere
          IF d_icona IS NOT NULL THEN
               d_pfx := '<img src="../common/images/icone/'||d_icona||'" border="0">';
          END IF;
         d_titolo_blocco := '<div class="AFC'||d_sfx||'Title">'||d_nome;
       END IF;
      END IF;
-- Assegnazione classi di stile
      d_td_class :=  'AFC'||d_sfx||'DataTD';
     d_link_class := 'AFC'||d_sfx||'DataLink';
      IF d_immagine IS NOT NULL THEN
        d_immagine := '<img src="../'||d_dir_upload||'/images/'||d_immagine||'" border="0" alt="'||d_nome||'" align="AbsMiddle">&nbsp;';
     END IF;
      IF NVL(d_zona_formato,'T') = 'T' THEN
        IF d_zona = 'C' THEN
          IF d_immagine IS NOT NULL THEN
               d_row := '<tr><td rowspan="100" align="left" valign="top" class="AFC'||d_sfx||'ImageTD">'||d_href||d_immagine||'</a></td>';
             d_row := d_row||'<td align="left" valign="top" width="100%" class="AFC'||d_sfx||'ColumnTD">'||d_href||d_nome||'</a></td></tr>';
            ELSE
            d_row := '<tr><td align="left" valign="top" width="100%" class="AFC'||d_sfx||'ColumnTD">'||d_href||d_nome||'</td></tr>';
         END IF;
       ELSE
          d_row:= '<tr><td class="AFC'||d_sfx||'ColumnTD" align="left" valign = "top">'||d_href||d_immagine||d_nome||'</a></td></tr>';
         END IF;
     ELSE
         d_row := '<tr><td align="center" width="100%" class="AFC'||d_sfx||'ImageTD">'||d_href||d_immagine||'</a></td></tr>';
      END IF;
   EXCEPTION WHEN NO_DATA_FOUND THEN
      d_no_rows := 1;
   END;
-- righe di apertura e chiusura
   d_row_start := '<div class="AFC'||d_sfx||'"><table width="100%" cellpadding="3"  cellspacing="0" class="AFC'||d_sfx||'FormTable">';
   d_row_end   := '</table></div>';
   d_row_sep  := '<tr><td class="AFC'||d_sfx||'SeparatorTD" width="100%"><img src="../common/images/AMV/blank_separator.gif"></tr></td>';
   -- Per blocco con importanza HeadLine nessuna intestazione di blocco
   IF d_importanza = 'HL' THEN
      d_row := '';
   END IF;
   -- inserimento riga iniziale nel clob
   d_amount := LENGTH(d_row_start||d_row);
   dbms_lob.writeappend(d_clob, d_amount,d_row_start||d_row);
   FOR c IN (
   SELECT id_documento
        , revisione
        , titolo
        , LINK
      , immagine
      , icona
      , id_tipologia
      , tipo_testo
      , id_documento_padre
     FROM (SELECT id_documento
                , revisione
                , titolo
                , LINK
            , immagine
            , icona
              , id_tipologia
            , tipo_testo
              , id_documento_padre
           FROM AMV_DOCUMENTI
            WHERE get_diritto_area(p_utente, id_area, id_tipologia) IS NOT NULL
              AND id_rilevanza = p_id_rilevanza
              AND SYSDATE BETWEEN INIZIO_PUBBLICAZIONE AND NVL(FINE_PUBBLICAZIONE,SYSDATE)
              AND id_sezione = DECODE(p_id_sezione, 0, id_sezione, p_id_sezione)
--           and id_sezione = p_id_sezione
              AND stato IN ('U','R')
            ORDER BY sequenza, titolo
         )
     WHERE ROWNUM <= NVL(d_max_vis,999)
   )
   LOOP
      BEGIN
        d_no_rows := 0;
         d_row:= get_riga( c.id_documento,c.revisione
                       , p_id_sezione, d_zona, d_importanza, c.titolo
                   , c.LINK, c.immagine
                   , c.id_tipologia, c.tipo_testo
                   , c.id_documento_padre, d_dir_upload
                   , d_td_class, d_link_class, d_pfx);
         -- inserimento inizio riga nel clob
         d_amount := LENGTH(d_row);
         dbms_lob.writeappend(d_clob, d_amount,d_row);
       -- aggiungo il testo se importanza Headline
       IF d_importanza = 'HL' AND c.tipo_testo IN ('Testo','Form') THEN
           d_row:= '<br>';
            d_amount := LENGTH(d_row);
            dbms_lob.writeappend(d_clob, d_amount,d_row);
          dbms_lob.append(d_clob,Amv_Documento.get_testo(c.id_documento));
       END IF;
         -- inserimento fine riga nel clob
       IF d_importanza = 'HL' THEN --AND c.tipo_testo IN ('Testo','Form') THEN
           d_row:= '</td></tr>';
            d_amount := LENGTH(d_row);
            dbms_lob.writeappend(d_clob, d_amount,d_row);
         END IF;
     END;
   END LOOP;
   IF d_no_rows = 1 THEN
      d_char := '<!-- null --'||'>';
      d_amount := LENGTH(d_char);
      dbms_lob.writeappend(d_clob_null, d_amount,d_char);
     RETURN d_clob_null;
   ELSE
      d_char := d_row_sep||d_row_end;
      d_amount := LENGTH(d_char);
      dbms_lob.writeappend(d_clob, d_amount,d_char);
     RETURN d_clob;
   END IF;
END GET_BLOCCO_RILEVANZA;
FUNCTION GET_BLOCCO_DOCUMENTI
( P_UTENTE        VARCHAR2,
  P_ZONA          VARCHAR2,
  P_SEQUENZA     NUMBER
)
RETURN CLOB IS
/******************************************************************************
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    16/10/2002 __     Prima emissione.
 1    27/11/2002 AO     Gestione CLOB
 2    27/11/2002 AO     Gestione documento con campo link non nullo
 3    06/04/2004 AO     Utilizzo classi di stile per i blocchi di sinistra e destra
                        AFCLeftFormTable AFCLeftColumnTD AFCLeftDataTD
                  AFCRightFormTable AFCRightColumnTD AFCRightDataTD
 4    03/08/2004 AO     MANTENUTO PER COMPATIBILITA CON VERSIONE 2003.11
******************************************************************************/
d_return           VARCHAR2(32000);
d_gruppo           VARCHAR2(8);
d_url              VARCHAR2(2000);
d_start            VARCHAR2(2000);
d_counter          NUMBER(8)     := 0;
d_path_pagina      VARCHAR2(200) := '../common';
d_path_immagine    VARCHAR2(200) := '../common/images';
d_immagine         VARCHAR2(200) ;
d_section_sep      VARCHAR2(200) := '<img src="../common/images/AMV/section_sep.jpg" border="0" align="top">';
d_blocco           VARCHAR2(32000):='';
d_title            VARCHAR2(200) := '';
d_stile_url         VARCHAR2(200) := '';
d_td_align         VARCHAR2(20):='';
-- gestione tramite clob
d_amount           BINARY_INTEGER := 32767;
d_char             VARCHAR2(32767);
d_clob             CLOB := EMPTY_CLOB() ;
BEGIN
   BEGIN
    d_start:='<table class="AFCFormTABLE" width="100%" cellspacing="0" cellpadding="3">';
    FOR c_voci IN (
    SELECT doc.id_documento
        , doc.revisione
       , doc.id_tipologia
       , doc.titolo
       , doc.LINK
       , tip.nome titolo_blocco
       , tip.immagine
       , tip.zona_formato
       , DECODE(INSTR(tip.immagine,1,2),'..','',d_path_immagine||'/')||tip.immagine img
      FROM (SELECT id_documento
                 , revisione
                 , titolo
                 , LINK
               , id_tipologia
            FROM AMV_DOCUMENTI
             WHERE get_diritto_area(p_utente, id_area, id_tipologia) IS NOT NULL
               AND SYSDATE BETWEEN INIZIO_PUBBLICAZIONE AND NVL(FINE_PUBBLICAZIONE,SYSDATE)
               AND stato IN ('U','R')
             ORDER BY data_riferimento DESC, data_aggiornamento DESC
          ) doc
          , AMV_TIPOLOGIE tip
     WHERE tip.id_tipologia =
         (SELECT MIN(id_tipologia)
            FROM AMV_TIPOLOGIE
             WHERE zona  = p_zona
               AND sequenza = p_sequenza
         )
       AND doc.id_tipologia = tip.id_tipologia
         AND ROWNUM <= NVL(tip.max_vis,999)
    )
     LOOP
        BEGIN
        d_td_align := '';
          IF c_voci.immagine IS NOT NULL THEN
             d_immagine := '<img src="'||c_voci.img||'" border="0">';
             IF c_voci.zona_formato = 'E' THEN
                d_immagine := d_immagine||'<br>';
            d_td_align := ' align="center"';
             ELSIF c_voci.zona_formato = 'T' THEN
                d_immagine := d_immagine||'&nbsp';
             END IF;
          ELSE
              d_immagine := '';
          END IF;
          IF d_counter = 0 THEN
             d_blocco:= d_blocco||'<tr><td class="AFCColumnTD" width="150"'||d_td_align||'>'||d_immagine||get_link_documenti(p_zona,p_sequenza)||'</td></tr>';
          END IF;
          d_counter := 1;
          IF c_voci.LINK IS NOT NULL THEN
             d_url := crea_link_documento(c_voci.id_documento, c_voci.LINK, c_voci.titolo, d_path_pagina);
          ELSE
             IF LENGTH(c_voci.titolo) > 90 THEN
                 d_title := c_voci.titolo;
                c_voci.titolo := SUBSTR(c_voci.titolo,1,87)||'...';
             END IF;
          IF p_zona = 'S' THEN
             d_stile_url := 'AFCLeftDoc';
          ELSE
             d_stile_url := 'AFCRightDoc';
          END IF;
             d_url := '<a class="'||d_stile_url||'" href="'||d_path_pagina||'/AmvDocumentoInfo.do?MVVC=amvdocui&ID='||c_voci.id_documento||'&MVTD='||c_voci.id_tipologia||'" title="'||d_title||'">'||c_voci.titolo||'</a>';
          END IF;
          d_blocco:= d_blocco||'<tr><td class="AFCDataTD" width="100%" >'||d_url||'</td></tr>';
        END;
    END LOOP;
   IF d_blocco IS NULL THEN
      d_return:= '<!-- null --'||'>';
   ELSE
       d_return:= d_start||d_blocco||'</table>'||d_section_sep;
   END IF;
   END;
   -- assegnazione del clob
   d_amount := LENGTH(d_return);
   dbms_lob.createTemporary(d_clob,TRUE,dbms_lob.SESSION);
   dbms_lob.WRITE(d_clob, d_amount,1, d_return);
   RETURN d_clob;
END GET_BLOCCO_DOCUMENTI;
FUNCTION GET_BLOCCO_DOCUMENTI
( P_UTENTE        VARCHAR2,
  P_ZONA          VARCHAR2
)
RETURN CLOB IS
/******************************************************************************
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    27/11/2002 __     Prima emissione.
******************************************************************************/
cReturn           CLOB := EMPTY_CLOB() ;
d_clob            CLOB := EMPTY_CLOB() ;
BEGIN
   dbms_lob.createTemporary(d_clob,TRUE,dbms_lob.SESSION);
   FOR c IN (
      SELECT DISTINCT zona, sequenza
        FROM AMV_TIPOLOGIE
      WHERE zona  = p_zona
        AND sequenza IS NOT NULL
      )
   LOOP
   BEGIN
      cReturn := GET_BLOCCO_DOCUMENTI(p_utente, c.zona, c.sequenza);
      dbms_lob.append(d_clob, cReturn);
   END;
   END LOOP;
   -- assegnazione del clob
   RETURN d_clob;
END GET_BLOCCO_DOCUMENTI;
FUNCTION GET_NUOVI_MSG (P_UTENTE VARCHAR2) RETURN VARCHAR2 IS
BEGIN
RETURN '';
END GET_NUOVI_MSG;
FUNCTION INITIALIZE (P_ID_DOCUMENTO NUMBER DEFAULT NULL)
/******************************************************************************
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    13/02/2003 __     Prima emissione.
******************************************************************************/
RETURN VARCHAR2
IS
BEGIN
   IF p_id_documento IS NULL THEN
      BEGIN
         id_documento := NULL;
           id_area      := NULL;
         id_tipologia := NULL;
     EXCEPTION
        WHEN OTHERS THEN
          RETURN NULL;
      END;
   ELSE
      SELECT id_documento
           , id_area
           , id_tipologia
        INTO id_documento
           , id_area
           , id_tipologia
        FROM AMV_DOCUMENTI
       WHERE id_documento = p_id_documento
        AND stato IN ('U','R')
      ;
   END IF;
   RETURN NULL;
EXCEPTION
   WHEN OTHERS THEN
      RETURN INITIALIZE;
END INITIALIZE;
PROCEDURE set_initialize (P_ID_DOCUMENTO NUMBER)
IS
d_return VARCHAR2(1);
BEGIN
   IF id_documento != p_id_documento THEN
      d_return := INITIALIZE(p_id_documento);
   END IF;
EXCEPTION
   WHEN OTHERS THEN NULL;
END set_initialize;
FUNCTION GET_TIPOLOGIA (P_ID_DOCUMENTO NUMBER) RETURN NUMBER
IS
BEGIN
   set_initialize(p_id_documento);
   RETURN id_tipologia;
END GET_TIPOLOGIA;
FUNCTION GET_CATEGORIA (P_ID_DOCUMENTO NUMBER) RETURN NUMBER
IS
BEGIN
   set_initialize(p_id_documento);
   RETURN id_categoria;
END GET_CATEGORIA;
FUNCTION GET_ARGOMENTO (P_ID_DOCUMENTO NUMBER) RETURN NUMBER
IS
BEGIN
   set_initialize(p_id_documento);
   RETURN id_argomento;
END GET_ARGOMENTO;
FUNCTION GET_RILEVANZA (P_ID_DOCUMENTO NUMBER) RETURN NUMBER
IS
BEGIN
   set_initialize(p_id_documento);
   RETURN id_rilevanza;
END GET_RILEVANZA;
FUNCTION GET_AREA (P_ID_DOCUMENTO NUMBER) RETURN NUMBER
IS
BEGIN
   set_initialize(p_id_documento);
   RETURN id_area;
END GET_AREA;
FUNCTION GET_NOME_TIPOLOGIA (P_ID_DOCUMENTO NUMBER, P_ID_TIPOLOGIA NUMBER DEFAULT NULL) RETURN VARCHAR2
IS
d_id   NUMBER;
d_nome VARCHAR2(2000);
BEGIN
   IF p_id_documento IS NULL THEN
      d_id := p_id_tipologia;
   ELSE
      d_id := get_tipologia(p_id_documento);
   END IF;
   SELECT nome
     INTO d_nome
     FROM AMV_TIPOLOGIE
    WHERE id_tipologia = d_id
   ;
   RETURN d_nome;
EXCEPTION
   WHEN OTHERS THEN
      RETURN '';
END GET_NOME_TIPOLOGIA;
FUNCTION GET_NOME_CATEGORIA (P_ID_DOCUMENTO NUMBER, P_ID_CATEGORIA NUMBER DEFAULT NULL) RETURN VARCHAR2
IS
d_id   NUMBER;
d_nome VARCHAR2(2000);
BEGIN
   IF p_id_documento IS NULL THEN
      d_id := p_id_categoria;
   ELSE
      d_id := get_categoria(p_id_documento);
   END IF;
   SELECT nome
     INTO d_nome
     FROM AMV_CATEGORIE
    WHERE id_categoria = d_id
   ;
   RETURN d_nome;
EXCEPTION
   WHEN OTHERS THEN
      RETURN '';
END GET_NOME_CATEGORIA;
FUNCTION GET_NOME_ARGOMENTO (P_ID_DOCUMENTO NUMBER, P_ID_ARGOMENTO NUMBER DEFAULT NULL) RETURN VARCHAR2
IS
d_id   NUMBER;
d_nome VARCHAR2(2000);
BEGIN
   IF p_id_documento IS NULL THEN
      d_id := p_id_argomento;
   ELSE
      d_id := get_argomento(p_id_documento);
   END IF;
   SELECT nome
     INTO d_nome
     FROM AMV_ARGOMENTI
    WHERE id_argomento = d_id
   ;
   RETURN d_nome;
EXCEPTION
   WHEN OTHERS THEN
      RETURN '';
END GET_NOME_ARGOMENTO;
FUNCTION GET_NOME_RILEVANZA (P_ID_DOCUMENTO NUMBER, P_ID_RILEVANZA NUMBER DEFAULT NULL) RETURN VARCHAR2
IS
d_id   NUMBER;
d_nome VARCHAR2(2000);
BEGIN
   IF p_id_documento IS NULL THEN
      d_id := p_id_rilevanza;
   ELSE
      d_id := get_rilevanza(p_id_documento);
   END IF;
   SELECT nome
     INTO d_nome
     FROM AMV_RILEVANZE
    WHERE id_rilevanza = d_id
   ;
   RETURN d_nome;
EXCEPTION
   WHEN OTHERS THEN
      RETURN '';
END GET_NOME_RILEVANZA;
FUNCTION GET_NOME_AREA (P_ID_DOCUMENTO NUMBER, P_ID_AREA NUMBER DEFAULT NULL) RETURN VARCHAR2
IS
d_id   NUMBER;
d_nome VARCHAR2(2000);
BEGIN
   IF p_id_documento IS NULL THEN
      d_id := p_id_area;
   ELSE
      d_id := get_area(p_id_documento);
   END IF;
   SELECT nome
     INTO d_nome
     FROM AMV_AREE
    WHERE id_area = d_id
   ;
   RETURN d_nome;
EXCEPTION
   WHEN OTHERS THEN
      RETURN '';
END GET_NOME_AREA;
FUNCTION GET_TESTO (P_ID_DOCUMENTO NUMBER) RETURN CLOB
IS
d_clob          CLOB := EMPTY_CLOB();
d_abstract      CLOB := EMPTY_CLOB();
d_amount        BINARY_INTEGER := 32767;
d_allegati      VARCHAR2(32000);
d_char          VARCHAR2(32000);
d_link          VARCHAR2(200);
d_revisione     NUMBER;
d_id_sezione    NUMBER;
d_id_tipologia  NUMBER;
d_clob_result   CLOB := EMPTY_CLOB();
BEGIN
   dbms_lob.createTemporary(d_clob_result,TRUE,dbms_lob.SESSION);
   BEGIN
   SELECT testo, abstract, revisione, id_sezione, id_tipologia
     INTO d_clob, d_abstract, d_revisione, d_id_sezione, d_id_tipologia
    FROM AMV_DOCUMENTI
   WHERE id_documento = p_id_documento
     AND stato IN ('U','R');
   EXCEPTION WHEN OTHERS THEN NULL;
   END;
   IF d_abstract IS NOT NULL THEN
      dbms_lob.append(d_clob_result,d_abstract);
     d_char := '&nbsp;(<a href="../common/AmvDocumentoInfo.do?MVVC=amvdocui&ID='||p_id_documento||'&REV='||d_revisione||'&MVPD=0&MVTD='||d_id_tipologia||'&MVSZ='||
         d_id_sezione||'" title="Visualizza testo completo">segue...</a>)';
      d_amount := LENGTH(d_char);
      dbms_lob.writeappend(d_clob_result, d_amount,d_char);
   ELSE
      IF d_clob IS NOT NULL THEN
         dbms_lob.append(d_clob_result,d_clob);
      END IF;
--       d_allegati := get_allegati(p_id_documento, d_revisione);
--       IF d_allegati IS NOT NULL THEN
--          d_amount := LENGTH(d_allegati);
--          dbms_lob.writeappend(d_clob_result, d_amount,d_allegati);
--       END IF;
   END IF;
   RETURN d_clob_result;
END GET_TESTO;
FUNCTION GET_TESTO_OVERFLOW (P_ID_DOCUMENTO NUMBER) RETURN CLOB
IS
d_char            VARCHAR2(32767);
d_amount          BINARY_INTEGER := 32767;
d_testo           CLOB := EMPTY_CLOB() ;
d_clob            CLOB := EMPTY_CLOB() ;
BEGIN
   dbms_lob.createTemporary(d_clob,TRUE,dbms_lob.SESSION);
   d_char:= '<div style="width:400px; height:12px; white-space:nowrap; overflow: hidden; text-overflow:ellipsis">';
   SELECT testo
     INTO d_testo
    FROM AMV_DOCUMENTI
   WHERE id_documento = p_id_documento;
    d_amount := LENGTH(d_char);
    dbms_lob.writeappend(d_clob, d_amount,d_char);
   dbms_lob.append(d_clob,d_testo);
    d_char:= '</div>(segue)';
    d_amount := LENGTH(d_char);
    dbms_lob.writeappend(d_clob, d_amount,d_char);
   RETURN d_clob;
END GET_TESTO_OVERFLOW;
FUNCTION CREA_LINK_DOCUMENTO
( P_ID_DOCUMENTO IN NUMBER
, P_LINK   VARCHAR2
, P_TITOLO VARCHAR2
, P_PATH   VARCHAR2 DEFAULT NULL
) RETURN  VARCHAR2 IS
d_url VARCHAR2(2000);
d_path_pagina VARCHAR2(200) := '../common';
d_title  VARCHAR2(200);
d_titolo VARCHAR2(200);
BEGIN
   IF LENGTH(p_titolo) > 90 THEN
     d_titolo := SUBSTR(p_titolo,1,87)||'...';
     d_title := p_titolo;
   ELSE
     d_titolo := p_titolo;
   END IF;
   IF p_link IS NOT NULL THEN
      IF (INSTR(p_link,'://') > 0) AND (INSTR(p_link,'://') < 10)  THEN
         d_url := '<a class="AFCLeftDoc" href="'||p_link||'" target="_blank" title="'||d_title||'">'||d_titolo||'</a>';
      ELSIF (INSTR(p_link,'../') > 0) THEN
         d_url := '<a class="AFCLeftDoc" href="'||p_link||'" title="'||d_title||'">'||d_titolo||'</a>';
     ELSE
         d_url := '<a class="AFCLeftDoc" href="'||NVL(p_path,d_path_pagina)||'/AmvDocLink.do?ID='||p_id_documento||'" title="'||d_title||'">'||d_titolo||'</a>';
      END IF;
   ELSE
     d_url := d_titolo;
   END IF;
   RETURN d_url;
END CREA_LINK_DOCUMENTO;
FUNCTION GET_ALLEGATI
(P_ID_DOCUMENTO NUMBER
,P_REVISIONE    NUMBER
) RETURN VARCHAR2
IS
d_allegati    VARCHAR2(32000);
d_row_start   VARCHAR2(2000);
d_servlet_sfx VARCHAR2(10);
BEGIN
   d_row_start := '<div>';
   FOR c IN (
      SELECT DISTINCT b.NOME nome
                   , DECODE(LOWER(b.TIPO)
                      ,'doc','application/msword'
                      ,'pdf','application/pdf'
                      ,'txt','text/plain'
                      ,'xls','application/vnd.ms-excel'
                      ,'pps','application/vnd.ms-powerpoint'
                      ,'ppt','application/vnd.ms-powerpoint'
                      ,'wks','application/vnd.ms-works'
                      ,'rtf','application/rtf'
                      ,'zip','application/zip'
                      ,'bmp','image/bmp'
                      ,'jpg','image/jpg'
                      ,'jpeg','image/jpg'
                      ,'gif','image/gif'
                      ,'tiff','image/tiff'
                      ,'tif','image/tiff'
                      ,'png','image/png'
                      ,'xml','text/xml'
                      ,'mpg','video/mpeg'
                      ,'mpeg','video/mpeg'
                      ,'mov','video/quicktime'
                      ,'html','text/html'
                                ,'htm','text/html'
                        ) mimetype
               , b.tipo tipo
               , b.id_blob id_blob
        FROM AMV_DOCUMENTI_BLOB doc_blob, AMV_BLOB b
      WHERE doc_blob.ID_DOCUMENTO  = p_id_documento
       AND doc_blob.REVISIONE = p_revisione
        AND doc_blob.ID_BLOB = b.ID_BLOB
      )
   LOOP
   BEGIN
      d_servlet_sfx := '';
      IF c.mimetype IS NULL THEN
         c.tipo := 'app';
      ELSIF c.mimetype = 'application/pdf' THEN
         d_servlet_sfx := '.pdf';
      END IF;
 /* Versione che genera solamente l'icona in base al tipo di file e il suo nome*/
 d_allegati := d_allegati||'<div>'
            ||'<img src="../common/images/AMV/'||LOWER(c.tipo)||'.gif"'
            ||' title="Download del file" alt="'||c.nome||'"/>'
            ||c.nome||'</div>';
   END;
   END LOOP;
   IF d_allegati IS NOT NULL THEN
      d_allegati := d_row_start||d_allegati||'</div>';
   END IF;
   RETURN d_allegati;
   END GET_ALLEGATI;
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
        )
IS
d_diritto   VARCHAR2(1);
d_id_documento NUMBER;
BEGIN
   d_diritto := GET_DIRITTO_AREA (P_AUTORE, P_ID_AREA, P_ID_TIPOLOGIA);
   IF d_diritto IN ('C','V','A','U') THEN
      IF p_id_documento IS NULL THEN
        d_id_documento := Si4.next_id('AMV_DOCUMENTI','ID_DOCUMENTO');
         p_id_documento := d_id_documento;
     ELSE
        d_id_documento := p_id_documento;
     END IF;
      INSERT INTO AMV_DOCUMENTI ( ID_DOCUMENTO, ID_TIPOLOGIA, ID_CATEGORIA, ID_ARGOMENTO, ID_RILEVANZA, ID_AREA, TITOLO, TIPO_TESTO, TESTO, ABSTRACT, LINK, IMMAGINE, ICONA, DATA_RIFERIMENTO, INIZIO_PUBBLICAZIONE, FINE_PUBBLICAZIONE, AUTORE, DATA_INSERIMENTO, UTENTE_AGGIORNAMENTO, DATA_AGGIORNAMENTO, REVISIONE, ID_SEZIONE, STATO, SEQUENZA)
          VALUES ( d_id_documento, P_ID_TIPOLOGIA, P_ID_CATEGORIA, P_ID_ARGOMENTO, P_ID_RILEVANZA, P_ID_AREA, P_TITOLO, P_TIPO_TESTO, P_TESTO, P_ABSTRACT, P_LINK, P_IMMAGINE, P_ICONA, NVL(P_DATA_RIFERIMENTO,SYSDATE), P_INIZIO_PUBBLICAZIONE, P_FINE_PUBBLICAZIONE, P_AUTORE, SYSDATE, P_AUTORE, SYSDATE, P_REVISIONE, P_ID_SEZIONE, 'B', P_SEQUENZA)
      ;
-- Inserimento degli attributi della revisione su tabella AMV_DOCUMENTI_REVISIONI
      INSERT INTO AMV_DOCUMENTI_REVISIONI(
                  id_documento, revisione, cronologia)
            VALUES (
                   d_id_documento, p_revisione
               , 'Creazione Revisione in data '||TO_CHAR(SYSDATE,'dd/mm/yyyy hh:mi'))
   ;
   --amv_revisione.creazione_revisione (p_id_documento, P_REVISIONE, P_AUTORE, P_AUTORE,P_AUTORE,P_AUTORE, P_INIZIO_PUBBLICAZIONE, P_FINE_PUBBLICAZIONE);
   ELSE
      RAISE_APPLICATION_ERROR(-20999,'Utente non abilitato a creare documenti di tipologia "'||
                                    get_nome_tipologia(NULL,p_id_tipologia)||
                            '" in area "'||get_nome_area(NULL,p_id_area)||'"');
   END IF;
END INSERISCI;
FUNCTION CREA_ERRORE (FLAG NUMBER) RETURN VARCHAR2
IS
BEGIN
   RAISE_APPLICATION_ERROR(-20999, 'errore di rimando');
   RETURN '1';
END CREA_ERRORE;
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
        )
IS
d_diritto   VARCHAR2(1);
BEGIN
   d_diritto := GET_DIRITTO_AREA (P_UTENTE_AGGIORNAMENTO, GET_AREA(P_ID_DOCUMENTO), GET_TIPOLOGIA(P_ID_DOCUMENTO));
   IF d_diritto NOT IN ('U','A','V','C') THEN
     RAISE_APPLICATION_ERROR(-20999,'Utente non abilitato a modificare documenti di tipologia "'||
                                    get_nome_tipologia(NULL,GET_TIPOLOGIA(P_ID_DOCUMENTO))||
                            '" in area "'||get_nome_area(NULL,GET_AREA(P_ID_DOCUMENTO))||'"');
   ELSE
      d_diritto := GET_DIRITTO_AREA (P_UTENTE_AGGIORNAMENTO, P_ID_AREA, P_ID_TIPOLOGIA);
      IF d_diritto NOT IN ('U','A','V','C') THEN
         RAISE_APPLICATION_ERROR(-20999,'Utente non abilitato a modificare documenti di tipologia "'||
                                       get_nome_tipologia(NULL,p_id_tipologia)||
                               '" in area "'||get_nome_area(NULL,p_id_area)||'"');
      ELSE
         UPDATE AMV_DOCUMENTI
            SET ID_TIPOLOGIA = P_ID_TIPOLOGIA
               , ID_CATEGORIA = P_ID_CATEGORIA
              , ID_ARGOMENTO = P_ID_ARGOMENTO
               , ID_RILEVANZA = P_ID_RILEVANZA
               , ID_AREA = P_ID_AREA
               , TITOLO = P_TITOLO
               , TIPO_TESTO = P_TIPO_TESTO
              , TESTO = P_TESTO
              , ABSTRACT = P_ABSTRACT
               , LINK = P_LINK
           , IMMAGINE = P_IMMAGINE
           , ICONA = P_ICONA
               , DATA_RIFERIMENTO = P_DATA_RIFERIMENTO
               , INIZIO_PUBBLICAZIONE = P_INIZIO_PUBBLICAZIONE
               , FINE_PUBBLICAZIONE = P_FINE_PUBBLICAZIONE
               , UTENTE_AGGIORNAMENTO = P_UTENTE_AGGIORNAMENTO
           , ID_SEZIONE = P_ID_SEZIONE
           , SEQUENZA = P_SEQUENZA
            WHERE ID_DOCUMENTO = P_ID_DOCUMENTO
          AND REVISIONE = P_REVISIONE
           ;
       IF P_ID_BLOB != -1 THEN
          DELETE FROM AMV_DOCUMENTI_BLOB
          WHERE REVISIONE    = P_REVISIONE
            AND ID_DOCUMENTO = P_ID_DOCUMENTO
            AND ID_BLOB      = P_ID_BLOB;
         DELETE FROM AMV_BLOB B
          WHERE ID_BLOB = P_ID_BLOB
          AND NOT EXISTS ( SELECT 'x'
                             FROM AMV_DOCUMENTI_BLOB
                         WHERE id_blob = b.id_blob
                           AND ( id_documento != p_id_documento
                             OR   revisione    != p_revisione   ))
          ;
       END IF;
      END IF;
   END IF;
END AGGIORNA;
PROCEDURE ELIMINA
        ( P_ID_DOCUMENTO         IN NUMBER
        , P_REVISIONE            IN NUMBER DEFAULT 0
      , P_STATO                IN VARCHAR2
        , P_UTENTE_AGGIORNAMENTO IN VARCHAR2
   )
IS
d_diritto   VARCHAR2(1);
TYPE t_id_blob IS TABLE OF NUMBER INDEX BY BINARY_INTEGER;
v_id_blob t_id_blob;
i INTEGER := 1;
BEGIN
   d_diritto := GET_DIRITTO_AREA (P_UTENTE_AGGIORNAMENTO, GET_AREA(P_ID_DOCUMENTO), GET_TIPOLOGIA(P_ID_DOCUMENTO));
   IF d_diritto != 'U' THEN
     RAISE_APPLICATION_ERROR(-20999,'Utente non abilitato a eliminare documenti di tipologia "'||
                                    get_nome_tipologia(NULL,GET_TIPOLOGIA(P_ID_DOCUMENTO))||
                            '" in area "'||get_nome_area(NULL,GET_AREA(P_ID_DOCUMENTO))||'"');
   ELSE
   /* modificato per foreign key
      for b in (select id_blob
                       from AMV_DOCUMENTI_BLOB
                   where id_documento = p_id_documento
                   and revisione    = p_revisione) loop
        delete from amv_blob
        where id_blob = b.id_blob
          and not exists( select 'x'
                           from amv_documenti_blob
                       where id_blob = b.id_blob
                         and ( id_documento != p_id_documento
                           or   revisione    != p_revisione   ));
     end loop;
     */
     FOR b IN (SELECT id_blob
                       FROM AMV_DOCUMENTI_BLOB b
                   WHERE id_documento = p_id_documento
                   AND revisione    = p_revisione
                  AND NOT EXISTS ( SELECT 'x'
                                        FROM AMV_DOCUMENTI_BLOB
                                    WHERE id_blob = b.id_blob
                                      AND ( id_documento != p_id_documento
                                        OR   revisione    != p_revisione   )
                               )
        ) LOOP
     v_id_blob(i) := b.id_blob;
     i := i+1;
     END LOOP;
     DELETE FROM AMV_DOCUMENTI_BLOB
      WHERE ID_DOCUMENTO = P_ID_DOCUMENTO
        AND REVISIONE    = P_REVISIONE
     ;
     DELETE FROM AMV_DOCUMENTI_REVISIONI
      WHERE ID_DOCUMENTO = P_ID_DOCUMENTO
        AND REVISIONE    = P_REVISIONE
      ;
      DELETE FROM AMV_DOCUMENTI
      WHERE ID_DOCUMENTO = P_ID_DOCUMENTO
        AND REVISIONE    = P_REVISIONE
      ;
     IF P_STATO != 'F' AND P_STATO != 'U' THEN
        UPDATE AMV_DOCUMENTI
           SET STATO = 'U'
         WHERE ID_DOCUMENTO = P_ID_DOCUMENTO
        AND STATO = 'R';
     END IF;
     FORALL v IN 1..i-1
     DELETE AMV_BLOB WHERE id_blob = v_id_blob(v);
      v_id_blob.DELETE;
   END IF;
END ELIMINA;
FUNCTION CREA_RICHIESTA
( P_ID_DOCUMENTO         IN NUMBER
, P_CODICE_RICHIESTA     IN VARCHAR2
, P_UTENTE               IN VARCHAR2
)
RETURN VARCHAR2
IS
d_cronologia VARCHAR2(4000);
d_parametri_stampa VARCHAR2(200);
PRAGMA autonomous_transaction;
v_documento AMV_DOCUMENTI%ROWTYPE;
new_id_documento NUMBER(10);
new_data_inserimento DATE;
d_stato   VARCHAR2(1);
d_return_message VARCHAR2(400);
d_aggiorna NUMBER(1) :=-1;
BEGIN
-- Se il documento in ingresso e di tipo richiesta esegue aggiornamento
SELECT NVL(MIN(revisione),-1)
  INTO d_aggiorna
  FROM AMV_DOCUMENTI
 WHERE id_documento = p_id_documento
   AND tipo_testo = 'Richiesta'
   ;
-- Inserisce nuova richiesta, p_id_documento e id del padre
IF d_aggiorna = -1 THEN
   SELECT *
     INTO v_documento
     FROM AMV_DOCUMENTI
    WHERE id_documento = p_id_documento
      AND stato IN ('U','R');
   IF Afc.GET_STRINGPARM(v_documento.LINK,'inoltro') = 'P' THEN
      d_stato := 'B';
   ELSE
      IF Afc.GET_STRINGPARM(v_documento.LINK,'iter') = 'N' THEN
         d_stato := 'A';
      ELSIF Afc.GET_STRINGPARM(v_documento.LINK,'iter') = 'A' THEN
         d_stato := 'V';
      ELSE
         d_stato := 'N';
      END IF;
   END IF;
   INSERT INTO AMV_DOCUMENTI (
      ID_TIPOLOGIA, ID_CATEGORIA,
      ID_ARGOMENTO, ID_RILEVANZA, ID_AREA,
      TITOLO, TIPO_TESTO, TESTO,
      LINK, DATA_RIFERIMENTO, INIZIO_PUBBLICAZIONE,
      FINE_PUBBLICAZIONE, AUTORE, DATA_INSERIMENTO,
      UTENTE_AGGIORNAMENTO, DATA_AGGIORNAMENTO, ID_SEZIONE,
      REVISIONE, STATO, XML,
      IMMAGINE, ID_DOCUMENTO_PADRE)
   VALUES ( v_documento.id_tipologia , v_documento.id_categoria,
       v_documento.id_argomento, v_documento.id_rilevanza,v_documento.id_area ,
       v_documento.titolo,'Richiesta' ,NULL,
       'cr='||P_CODICE_RICHIESTA, TRUNC(SYSDATE),NULL ,
       NULL ,P_UTENTE ,SYSDATE,
       P_UTENTE ,SYSDATE ,v_documento.id_sezione,
       v_documento.revisione ,d_stato ,NULL,
       NULL,v_documento.id_documento )
      RETURNING id_documento, data_inserimento INTO
               new_id_documento, new_data_inserimento
      ;
   IF d_stato = 'B' THEN
      d_return_message := 'ID='||TO_CHAR(new_id_documento);
      d_cronologia :=  CHR(10)||'- '||TO_CHAR(SYSDATE,'dd/mm/yyyy hh:mi')||
                    ' - '||P_UTENTE||'- Registrazione';
   ELSE
      --d_return_message := 'Il modello e stata inoltrato.';
      -- link alla pagina di stampa
      IF INSTR(v_documento.LINK,'|') = 0 THEN
         d_parametri_stampa := v_documento.LINK;
      ELSE
         d_parametri_stampa := SUBSTR(v_documento.LINK,1,INSTR(v_documento.LINK,'|')-1);
      END IF;
      d_parametri_stampa := REPLACE(d_parametri_stampa,'/','&');
     d_parametri_stampa := d_parametri_stampa||'&cr='||p_codice_richiesta;
     d_return_message := '<a target="_blank" href="../restrict/ServletModulisticaPrint.do?'||d_parametri_stampa||'">Stampa&nbsp;il&nbsp;modulo&nbsp;<img src="../common/images/AMV/printer.gif" border="0"></a>';
      d_cronologia :=  CHR(10)||'- '||TO_CHAR(SYSDATE,'dd/mm/yyyy hh:mi')||
                    ' - '||P_UTENTE||'- Inoltro';
   END IF;
   INSERT INTO AMV_DOCUMENTI_REVISIONI (
      ID_DOCUMENTO, REVISIONE, DATA_REDAZIONE,
      UTENTE_REDAZIONE, DATA_VERIFICA, UTENTE_VERIFICA,
      DATA_APPROVAZIONE, UTENTE_APPROVAZIONE, CRONOLOGIA,
      NOTE)
   VALUES (new_id_documento, v_documento.revisione,new_data_inserimento,
       P_UTENTE, NULL ,NULL ,
       NULL, NULL, d_cronologia,
      NULL );
ELSE
   d_cronologia :=  CHR(10)||'- '||TO_CHAR(SYSDATE,'dd/mm/yyyy hh:mi')||
                    ' - '||P_UTENTE||'- Aggiornamento';
   UPDATE AMV_DOCUMENTI
      SET utente_aggiornamento = p_utente
        ,data_aggiornamento = SYSDATE
   WHERE id_documento = p_id_documento
     AND revisione = d_aggiorna;
   UPDATE AMV_DOCUMENTI_REVISIONI
      SET cronologia = cronologia||d_cronologia
    WHERE id_documento = p_id_documento
      AND revisione = d_aggiorna;
    d_return_message := 'Dati modificati.';
END IF;
   COMMIT;
RETURN d_return_message;
EXCEPTION
WHEN OTHERS THEN
   RETURN 'ERRORE in fase di inoltro del modello.'||CHR(10)||
           SQLERRM;
END;
FUNCTION GET_MODELLO (
  P_ID_DOCUMENTO NUMBER
, P_REVISIONE NUMBER
, P_TIPO VARCHAR2 DEFAULT 'R'
) RETURN VARCHAR2
IS
d_link_modello VARCHAR2(200);
BEGIN
   SELECT LINK
     INTO d_link_modello
     FROM AMV_DOCUMENTI
    WHERE id_documento = P_ID_DOCUMENTO
     AND revisione = P_REVISIONE
     AND tipo_testo = 'Form'
   ;
   IF p_tipo = 'R' THEN
      IF INSTR(d_link_modello,'|') != 0 THEN
           d_link_modello := SUBSTR(d_link_modello,1,INSTR(d_link_modello,'|')-1);
      END IF;
   END IF;
   IF p_tipo = 'A' THEN
      d_link_modello := SUBSTR(d_link_modello,INSTR(d_link_modello,'|')+1,INSTR(d_link_modello,'|',1,2)-INSTR(d_link_modello,'|')-1);
   END IF;
   RETURN d_link_modello;
EXCEPTION
   WHEN OTHERS THEN
      RETURN '';
END GET_MODELLO;
FUNCTION GET_MODELLO (
  P_LINK VARCHAR2
, P_TIPO VARCHAR2 DEFAULT 'R'
) RETURN VARCHAR2
IS
d_link_modello VARCHAR2(200);
BEGIN
   d_link_modello := p_link;
   IF INSTR(d_link_modello,'|') != 0 THEN
     d_link_modello := SUBSTR(p_link,1,INSTR(p_link,'|')-1);
   END IF;
   IF p_tipo = 'A' THEN
      d_link_modello := SUBSTR(p_link,INSTR(p_link,'|')+1,INSTR(p_link,'|',1,2)-INSTR(p_link,'|')-1);
   END IF;
   RETURN d_link_modello;
EXCEPTION
   WHEN OTHERS THEN
      RETURN '';
END GET_MODELLO;
FUNCTION GET_LINK (P_ID_DOCUMENTO NUMBER, P_REVISIONE NUMBER) RETURN VARCHAR2
IS
d_link       VARCHAR2(200);
d_tipo_testo VARCHAR2(10);
BEGIN
   SELECT LINK, tipo_testo
     INTO d_link, d_tipo_testo
     FROM AMV_DOCUMENTI
    WHERE id_documento = P_ID_DOCUMENTO
     AND revisione = P_REVISIONE
   ;
   d_link := get_link(p_id_documento, p_revisione, d_link, d_tipo_testo);
   RETURN d_link;
EXCEPTION
   WHEN OTHERS THEN
      RETURN '';
END GET_LINK;
FUNCTION GET_LINK (P_ID_DOCUMENTO NUMBER, P_REVISIONE NUMBER, P_LINK VARCHAR2,P_TIPO_TESTO VARCHAR2 DEFAULT NULL) RETURN VARCHAR2
IS
d_link VARCHAR2(200);
BEGIN
   IF p_tipo_testo = 'Testo' THEN
      IF INSTR(p_link, 'http://') != -1 THEN
         RETURN p_link;
     END IF;
   ELSIF p_tipo_testo = 'Form' THEN
      d_link := p_link;
   ELSIF INSTR(p_link, 'ID=') != -1 THEN
      d_link := '../common/AmvDocumentoInfo.do?'||p_link;
   END IF;
   RETURN d_link;
EXCEPTION
   WHEN OTHERS THEN
      RETURN '';
END GET_LINK;
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
) RETURN VARCHAR2 IS
d_row           VARCHAR2(10000);
d_url           VARCHAR2(1000);
d_td_class      VARCHAR2(100);
d_td_align      VARCHAR2(100);
d_link_class    VARCHAR2(100);
d_img           VARCHAR2(200);
d_link_form      VARCHAR2(200);
d_rw         VARCHAR2(5) := '';
BEGIN
   IF p_immagine IS NOT NULL THEN
      d_img := '<img alt="'||p_titolo||'" src="../'||p_dir_upload||'/docs/'||p_immagine||'" border="0">';
      IF p_zona = 'C' THEN
        d_td_align := 'align="left"';
       d_img := '';
     ELSE
        d_td_align := 'align="center"';
     END IF;
   ELSE
      d_img := '';
      d_td_align := 'align="left"';
   END IF;
   IF p_tipo_testo = 'Link' THEN
   -- verifico se link a documento del portale o link esterno
      IF INSTR(p_link,'ID=') = 1 THEN
         d_url := '<a class="'||p_link_class||'" href="../common/AmvDocumentoInfo.do?MVVC=amvdocui&'||p_link||'&MVPD=0&MVTD='||p_id_tipologia||'&MVSZ='||
         p_id_sezione||'" title="'||p_titolo||'">'||NVL(d_img,p_titolo)||'</a>';
      ELSE
         d_url := '<a class="'||p_link_class||'" target="_blank" href="'||p_link||'" title="'||p_titolo||'">'||NVL(d_img,p_titolo)||'</a>';
      END IF;
   ELSIF p_tipo_testo = 'Form' THEN
      IF INSTR(p_LINK,'|') = 0 THEN
         d_link_form := p_LINK;
      ELSE
         d_link_form := SUBSTR(p_LINK,1,INSTR(p_link,'|')-1);
      END IF;
      d_link_form := REPLACE(d_link_form,'/','&');
      d_url := '<a class="'||p_link_class||'" href="../restrict/ServletModulistica.do?'||d_link_form||'&rw=W&MVVC=amvdocui&ID=0&IDPD='||
      p_id_documento||'&REV='||p_revisione||'&MVPD=0&MVTD='||p_id_tipologia||'&MVSZ='||p_id_sezione||'" title="'||p_titolo||'">'||p_titolo||'</a>';
   ELSIF p_tipo_testo = 'Xquery' THEN
      d_url := '<a class="'||p_link_class||'" href="../common/AmvXqueryDoc.do?MVVC=amvdocui&ID='||p_id_documento||'&REV='||p_revisione||'&MVPD=0&MVTD='||
      p_id_tipologia||'&MVSZ='||p_id_sezione||'" title="'||p_titolo||'">'||p_titolo||'</a>';
   ELSIF p_tipo_testo = 'SQLquery' THEN
      d_url := '<a class="'||p_link_class||'" href="../common/AmvSQLqueryDoc.do?MVVC=amvdocui&ID='||p_id_documento||'&REV='||p_revisione||'&MVPD=0&MVTD='||
      p_id_tipologia||'&MVSZ='||p_id_sezione||'" title="'||p_titolo||'">'||p_titolo||'</a>';
   ELSE
      IF p_importanza = 'HL' THEN
        d_url := NVL(d_img,'<font class="AFCCenterTitle">'||p_titolo||'</font>');
      ELSE
         d_url := '<a class="'||p_link_class||'" href="../common/AmvDocumentoInfo.do?MVVC=amvdocui&ID='||p_id_documento||'&REV='||p_revisione||'&MVPD=0&MVTD='||
         p_id_tipologia||'&MVSZ='||p_id_sezione||'" title="'||p_titolo||'">'||NVL(d_img,p_titolo)||'</a>';
      END IF;
   END IF;
   -- Test significativo solo per un blocco rilevanza che passa una importanza HL o HP
   -- in questo caso la chiusura di td e tr avviene nella function chiamante
   IF p_importanza = 'HL' THEN --AND p_tipo_testo IN ('Testo','Form') THEN
      d_row:= '<tr><td class="AFCCenterHeadlineTD" width="100%">'||d_url;
   ELSE
      d_row:= '<tr><td class="'||p_td_class||'" width="100%" '||d_td_align||'>'||p_pfx||d_url||'</td></tr>';
   END IF;
   RETURN d_row;
END GET_RIGA;
END Amv_Documento;
/* End Procedure: AMV_DOCUMENTO */
/

