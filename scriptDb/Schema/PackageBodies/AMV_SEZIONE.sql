CREATE OR REPLACE PACKAGE BODY Amv_Sezione AS
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
FUNCTION GET_BLOCCO ( P_UTENTE VARCHAR2, P_ZONA VARCHAR2, P_ID_SEZIONE NUMBER, P_PAGE VARCHAR2) RETURN CLOB IS
/******************************************************************************
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    20/07/2004 AO     Prima emissione.
******************************************************************************/
d_row_start       VARCHAR2(1000);
d_row_int         VARCHAR2(1000);
d_row_end         VARCHAR2(1000);
d_row             VARCHAR2(10000);
d_row_sep         VARCHAR2(200);
d_document_row    VARCHAR2(10000);
d_section_row     VARCHAR2(10000);
d_current_row     VARCHAR2(10000);
d_href            VARCHAR2(1000);
d_sfx             VARCHAR2(10):='Left';
d_nome           VARCHAR2(1000);
d_padre           NUMBER := 0;
d_id_sezione      NUMBER;
d_livello         NUMBER := 0;
d_immagine        VARCHAR2(200) ;
d_icona           VARCHAR2(200) ;
d_descrizione     VARCHAR2(4000) ;
d_zona            VARCHAR2(1) := 'N';
d_zona_espansione VARCHAR2(1);
d_zona_tipo       VARCHAR2(1);
d_zona_formato    VARCHAR2(1);
d_max_vis         NUMBER := 0;
d_amount          BINARY_INTEGER := 32767;
d_char            VARCHAR2(32767);
d_clob            CLOB := EMPTY_CLOB() ;
d_dir_upload      VARCHAR(200);
BEGIN
-- Acquisizione directory di Upload
   d_dir_upload := NVL(Amvweb.GET_PREFERENZA('Directory Upload','{Modulo}'),'common');
   dbms_lob.createTemporary(d_clob,TRUE,dbms_lob.SESSION);
-- calcolo estremi della sezione corrente
   SELECT id_padre, nome, id_sezione, descrizione, zona, zona_tipo, zona_formato, zona_espansione, immagine, max_vis, icona
     INTO d_padre, d_nome, d_id_sezione, d_descrizione, d_zona, d_zona_tipo, d_zona_formato, d_zona_espansione, d_immagine, d_max_vis, d_icona
     FROM AMV_SEZIONI
    WHERE id_sezione = p_id_sezione
   ;
-- Assegnazione stile
   IF d_zona = 'D' THEN
      d_sfx := 'Right';
   ELSIF d_zona = 'C' THEN
      d_sfx := 'Center';
   END IF;
-- righe di apertura e chiusura
   d_row_start := '<div class="AFC'||d_sfx||'"><table width="100%" cellpadding="3"  cellspacing="0" class="AFC'||d_sfx||'FormTable">';
   d_row_end   := '</table></div>';
   d_row_sep   := '<tr><td class="AFC'||d_sfx||'SeparatorTD" width="100%"><img src="../common/images/AMV/blank_separator.gif"></tr></td>';
-- riga della sezione corrente
   d_href := '<a class="AFC'||d_sfx||'NavigatorLink" href="../common/AmvSezione.do?MVPD=0&MVSZ='||d_id_sezione||'">';
   IF d_immagine IS NOT NULL THEN
      d_immagine := '<img src="../'||d_dir_upload||'/images/'||d_immagine||'" border="0" alt="'||d_nome||'" align="AbsMiddle">&nbsp;';
   END IF;
   IF d_zona_formato = 'T' THEN
      d_current_row := '<tr><td rowspan="100" align="left" valign="top" class="AFC'||d_sfx||'Image1TD">'||d_href||d_immagine||'</a></td>';
      d_current_row := d_current_row||'<td align="left" valign="top" width="100%" class="AFC'||d_sfx||'ColumnTD">'||d_href||d_nome||'</a></td></tr>';
-- aggiungo descrizione della sezione
      d_current_row := d_current_row||'<tr><td align="left" valign="top" width="100%" class="AFC'||d_sfx||'DataTD">'||d_descrizione||'</td></tr>';
   ELSE
      d_current_row := '<tr><td valign="top" align="left" width="100%" class="AFC'||d_sfx||'ImageTD">'||d_href||d_immagine||'</a><br>'||d_descrizione||'</td></tr>';
   END IF;
-- creazione righe delle sottosezioni
   IF d_zona_tipo IN ('S','E') THEN  -- mostro sottosezioni
      d_section_row:= get_blocco_sottosezioni(d_id_sezione,0,p_zona);
--       if d_section_row is not null then
--         d_section_row:= '<tr><td valign="top"><table valign="top" width="100%" cellpadding="0" cellspacing="0">'||d_section_row||'</table></td></tr>';
--       else
--         d_section_row:='';
--      end if;
   END IF;
-- righe relative ai documenti
   IF d_zona_tipo IN ('D','E') THEN
      d_document_row:= get_blocco_documenti(p_utente, d_id_sezione, 0, d_zona, d_max_vis, d_icona)||'</td></tr>';
      IF d_document_row IS NULL THEN
        d_document_row:= '<tr><td valign="top"><table valign="top" width="100%" cellpadding="0" cellspacing="0">Non sono disponibili documenti per questa sezione</td></tr>';
     END IF;
   END IF;
   d_row:= d_current_row||d_section_row||d_document_row;
   d_char := d_row_start||d_row||d_row_sep||d_row_end;
   -- assegnazione del clob
   d_amount := LENGTH(d_char);
   dbms_lob.writeappend(d_clob, d_amount,d_char);
   RETURN d_clob;
END GET_BLOCCO;
FUNCTION GET_CORPO ( P_UTENTE VARCHAR2, P_ID_SEZIONE NUMBER, P_PAGE VARCHAR2, P_COLONNE NUMBER) RETURN CLOB IS
/******************************************************************************
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    20/07/2004 AO     Prima emissione.
******************************************************************************/
d_row_start       VARCHAR2(1000);
d_row_int         VARCHAR2(1000);
d_row_end         VARCHAR2(1000);
d_row             VARCHAR2(10000);
d_document_row    VARCHAR2(10000);
d_current_row     VARCHAR2(10000);
d_href            VARCHAR2(1000);
d_sfx             VARCHAR2(10):='Left';
d_nome           VARCHAR2(1000);
d_padre           NUMBER := 0;
d_id_sezione      NUMBER;
d_livello         NUMBER := 0;
d_immagine        VARCHAR2(200) ;
d_zona            VARCHAR2(1);
d_zona_espansione VARCHAR2(1);
d_zona_tipo       VARCHAR2(1);
d_zona_formato    VARCHAR2(1);
d_max_vis         NUMBER := 0;
d_section_sep     VARCHAR2(200);
i                 NUMBER := 0;
j                 NUMBER := 0;
d_char            VARCHAR2(32767);
d_amount          BINARY_INTEGER := 32767;
d_clob            CLOB := EMPTY_CLOB() ;
d_clob1           CLOB := EMPTY_CLOB() ;
BEGIN
   dbms_lob.createTemporary(d_clob,TRUE,dbms_lob.SESSION);
-- Assegnazione stile
   d_sfx := 'Center';
-- righe di apertura e chiusura
   d_row_start := '<div class="AFC'||d_sfx||'"><table width="100%" cellpadding="3"  cellspacing="0" class="AFC'||d_sfx||'Table">';
   d_row_end   := '</table></div>';
   d_amount := LENGTH(d_row_start);
   dbms_lob.writeappend(d_clob, d_amount,d_row_start);
-- calcolo estremi della sezione corrente
   SELECT id_padre, nome, id_sezione, zona, zona_tipo, zona_formato, zona_espansione, immagine, max_vis
     INTO d_padre, d_nome, d_id_sezione, d_zona, d_zona_tipo, d_zona_formato, d_zona_espansione, d_immagine, d_max_vis
     FROM AMV_SEZIONI
    WHERE id_sezione = p_id_sezione
   ;
--          d_amount := length('<tr>');
--          dbms_lob.writeappend(d_clob, d_amount,'<tr>');
-- ciclo su sottosezioni tipologie rilevanze e documenti
   FOR c IN (
      -- sottosezioni
      SELECT id_sezione      ID
          , nome            nome
         , 'sezione'       tipo
         , sequenza        sequenza
         , immagine        immagine
         , zona_tipo       zona_tipo
         , zona_formato    zona_formato
         , zona_espansione zona_espansione
         , max_vis         max_vis
        FROM AMV_SEZIONI
      WHERE (id_padre = p_id_sezione OR zona_visibilita = 'S')
        AND zona = 'C'
        AND get_visibilita(id_sezione, p_id_sezione, p_page, zona_visibilita) = 1
      AND  DECODE(id_area,'','R',Amv_Area.get_diritto_utente(p_utente, id_area)) IN ('R','C','V','A','U')
      UNION
     -- tipologie
      SELECT id_tipologia    ID
          , nome            nome
         , 'tipologia'     tipo
         , sequenza        sequenza
         , immagine        immagine
         , 'T'             zona_tipo
         , zona_formato    zona_formato
         , 'S'             zona_espansione
         , max_vis         max_vis
        FROM AMV_TIPOLOGIE
      WHERE zona = 'C'
--        and zona_visibilita = decode(p_id_sezione, 0, zona_visibilita, 'S')
         AND zona_visibilita IN ('S', DECODE(p_id_sezione, 0, 'H', 'C'))
      UNION
     -- rilevanze
      SELECT id_rilevanza    ID
          , nome            nome
         , 'rilevanza'     tipo
         , sequenza        sequenza
         , immagine        immagine
         , 'T'             zona_tipo
         , zona_formato    zona_formato
         , 'S'             zona_espansione
         , max_vis         max_vis
        FROM AMV_RILEVANZE
      WHERE zona = 'C'
        AND zona_visibilita IN ('S', DECODE(p_id_sezione, 0, 'H','C'))
     -- documenti di sezione
/*     union
      select d.id_documento  id
          , d.titolo        nome
         , 'documento'     tipo
         , r.sequenza      sequenza
         , ''              immagine
         , 'T'             zona_tipo
         , ''              zona_formato
         , 'S'             zona_espansione
         , d.id_tipologia  max_vis
        from amv_documenti d, amv_rilevanze r
      where d.id_sezione = p_id_sezione
        and d.id_rilevanza = r.id_rilevanza
        and r.importanza = 'SL'
         and sysdate between d.inizio_pubblicazione and nvl(d.fine_pubblicazione,sysdate)
         and id_sezione = decode(p_id_sezione, 0, d.id_sezione, p_id_sezione)
         and stato in ('U','R')
       and amv_documento.get_diritto_area(p_utente, d.id_area, d.id_tipologia) is not null
     -- documenti di pagina Main
     union
      select d.id_documento  id
          , d.titolo        nome
         , 'documento'     tipo
         , r.sequenza      sequenza
         , ''              immagine
         , 'T'             zona_tipo
         , ''              zona_formato
         , 'S'             zona_espansione
         , d.id_tipologia  max_vis
        from amv_documenti d, amv_rilevanze r
      where d.id_sezione = p_id_sezione
        and d.id_rilevanza = r.id_rilevanza
        and r.importanza = 'HL'
       and p_page = 'Main'
         and sysdate between d.inizio_pubblicazione and nvl(d.fine_pubblicazione,sysdate)
         and id_sezione = decode(p_id_sezione, 0, d.id_sezione, p_id_sezione)
         and stato in ('U','R')
       and amv_documento.get_diritto_area(p_utente, d.id_area, d.id_tipologia) is not null
*/      ORDER BY sequenza, nome
   )
   LOOP
      BEGIN
        i:= i+1;
         d_amount := LENGTH('<tr><td valign="top">');
         dbms_lob.writeappend(d_clob, d_amount,'<tr><td valign="top">');
      -- append sul clob dei vari blocchi
         IF c.tipo = 'sezione' THEN
            dbms_lob.append(d_clob, get_blocco(p_utente, 'C', c.ID , p_page));
         ELSIF c.tipo = 'tipologia' THEN
            dbms_lob.append(d_clob,Amv_Documento.get_blocco_tipologia(p_utente, c.ID, p_id_sezione));
         ELSIF c.tipo = 'rilevanza' THEN
            dbms_lob.append(d_clob,Amv_Documento.get_blocco_rilevanza(p_utente, c.ID, p_id_sezione));
         ELSE
            d_href := '<a class="AFC'||d_sfx||'NavigatorLink" href="../common/AmvDocumentoInfo.do?MVVC=amvdocui&ID='||c.ID||'&MVTD='||c.max_vis||'&MVSZ='||p_id_sezione||'" title="'||c.nome||'">'||c.nome||'</a>';
            d_row := '<table width="100%" cellpadding="3"  cellspacing="0" class="AFC'||d_sfx||'FormTable">';
           d_row:= d_row||'<tr><td class="AFC'||d_sfx||'ColumnTD" width="100%" valign="top">'||d_href||'</td></tr>';
            d_amount := LENGTH(d_row);
            dbms_lob.writeappend(d_clob, d_amount,d_row);
           d_row:= '<tr><td class="AFC'||d_sfx||'DataTD" width="100%" valign="top">';
            d_amount := LENGTH(d_row);
            dbms_lob.writeappend(d_clob, d_amount,d_row);
         dbms_lob.append(d_clob,Amv_Documento.get_testo(c.ID));
--         dbms_lob.append(d_clob,amv_documento.get_testo_overflow(c.id));
            d_amount := LENGTH('</td></tr></table>');
            dbms_lob.writeappend(d_clob, d_amount,'</td></tr></table>');
       END IF;
         d_amount := LENGTH('</td></tr>');
         dbms_lob.writeappend(d_clob, d_amount,'</td></tr>');
      END;
   END LOOP;
--          d_amount := length('</tr>');
--          dbms_lob.writeappend(d_clob, d_amount,'</tr>');
   IF i=0 THEN
      d_char:='Non sono disponibili documenti per questa sezione';
      -- assegnazione del clob
      d_amount := LENGTH(d_char);
      dbms_lob.writeappend(d_clob, d_amount,d_char);
   END IF;
   d_amount := LENGTH(d_row_end);
   dbms_lob.writeappend(d_clob, d_amount,d_row_end);
   RETURN d_clob;
END GET_CORPO;
FUNCTION GET_MENU ( P_UTENTE VARCHAR2, P_ZONA VARCHAR2, P_ID_SEZIONE NUMBER, P_PAGE VARCHAR2) RETURN CLOB IS
/******************************************************************************
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    20/07/2004 AO     Prima emissione.
******************************************************************************/
d_row_start     VARCHAR2(1000);
d_row_int       VARCHAR2(1000);
d_row_end       VARCHAR2(1000);
d_row_sep       VARCHAR2(200);
d_row           VARCHAR2(10000);
d_href          VARCHAR2(1000);
d_title         VARCHAR2(1000);
d_padre_blocco  NUMBER := 0;
d_padre         NUMBER := 0;
d_immagine      VARCHAR2(200) ;
d_selected      VARCHAR2(10000):='';
d_sfx           VARCHAR2(10):='Left';
d_amount        BINARY_INTEGER := 32767;
d_char          VARCHAR2(32767);
d_blocco_sel    VARCHAR2(32767);
d_blocco_mod    VARCHAR2(32767);
d_clob          CLOB := EMPTY_CLOB() ;
d_clob1         CLOB := EMPTY_CLOB() ;
d_dir_upload      VARCHAR(200);
BEGIN
-- Acquisizione directory di Upload
   d_dir_upload := NVL(Amvweb.GET_PREFERENZA('Directory Upload','{Modulo}'),'common');
   dbms_lob.createTemporary(d_clob,TRUE,dbms_lob.SESSION);
   d_blocco_sel := get_blocco_menu(p_utente, p_zona, p_id_sezione, NULL);
   d_padre_blocco := get_padre_blocco(p_id_sezione);
   d_padre := get_padre(p_id_sezione);
-- Assegnazione stile
   IF p_zona = 'D' THEN
      d_sfx := 'Right';
   ELSIF p_zona = 'C' THEN
      d_sfx := 'Center';
   END IF;
-- righe di apertura chiusura e separazione
   d_row_start := '<div class="AFC'||d_sfx||'"><table width="100%" cellpadding="3"  cellspacing="0" class="AFC'||d_sfx||'FormTable">';
   d_row_end   := '</table></div>';
   d_row_sep   := '<tr><td class="AFC'||d_sfx||'SeparatorTD" width="100%"><img src="../common/images/AMV/blank_separator.gif"></tr></td>';
-- ATTENZIONE ATTENZIONE ATTENZIONE ATTENZIONE ATTENZIONE ATTENZIONE
-- CREAZIONE DEL BLOCCO MODULI ON-LINE - LE MIE RICHIESTE PER ZONA S
-- Utilizza preferenza da registro per titolo blocco
   IF p_zona = 'S' AND p_utente != 'GUEST' THEN
      d_blocco_mod := get_blocco_modulistica(p_utente, p_id_sezione, p_zona);
     d_char := d_blocco_mod;
   END IF;
-- ATTENZIONE ATTENZIONE ATTENZIONE ATTENZIONE ATTENZIONE ATTENZIONE
-- ciclo per sezioni relative alla zona selezionata
   FOR c IN (
       SELECT id_sezione, nome, id_padre, visibilita, zona_tipo, zona_espansione, zona_formato, immagine, max_vis
         FROM AMV_SEZIONI
        WHERE zona = p_zona
          -- condizioni per visibilita
        AND get_visibilita(id_sezione, p_id_sezione, p_page, zona_visibilita) = 1
        AND  DECODE(id_area,'','R',Amv_Area.get_diritto_utente(p_utente, id_area)) IN ('R','C','V','A','U')
        ORDER BY sequenza, nome
   )
   LOOP
   BEGIN
      d_href := '<a class="AFC'||d_sfx||'NavigatorLink" href="../common/AmvSezione.do?MVPD=0&MVSZ='||c.id_sezione||'">';
      IF c.immagine IS NOT NULL THEN
        d_immagine := '<img src="../'||d_dir_upload||'/images/'||c.immagine||'" border="0" alt="'||c.nome||'" align="AbsMiddle">&nbsp;';
     ELSE
        d_immagine := '';
     END IF;
     IF c.zona_formato = 'T' THEN
         d_row:= '<tr><td class="AFC'||d_sfx||'ColumnTD" align="left" valign = "top">'||d_href||d_immagine||c.nome||'</a></td></tr>';
      ELSE
         d_row := '<tr><td align="center" width="100%" class="AFC'||d_sfx||'ImageTD">'||d_href||d_immagine||'</a></td></tr>';
      END IF;
      -- espansione blocco
      IF c.zona_espansione = 'S' THEN
         IF c.zona_tipo = 'S' THEN  -- mostro solo sottosezioni
          d_row:= d_row||get_blocco_sottosezioni(c.id_sezione,0,p_zona);
       ELSIF c.zona_tipo = 'D' THEN  -- mostro solo documenti
          d_row:= d_row||get_blocco_documenti(p_utente, c.id_sezione, 0, p_zona, c.max_vis);
        ELSE  -- mostro sottosezioni e documenti
          d_row:= d_row||get_blocco_sottosezioni(c.id_sezione,0,p_zona);
          d_row:= d_row||get_blocco_documenti(p_utente, c.id_sezione, 0, p_zona, c.max_vis);
       END IF;
     END IF;
     IF c.id_sezione = d_padre_blocco THEN
        d_char := d_char||d_row_start||d_blocco_sel||d_row_sep||d_row_end;
      ELSE
        d_char := d_char||d_row_start||d_row||d_row_sep||d_row_end;
     END IF;
   END;
   END LOOP;
   IF d_char IS NULL THEN
      d_char:='<!-- null --'||'>';
   END IF;
   -- assegnazione del clob
   d_amount := LENGTH(d_char);
   dbms_lob.writeappend(d_clob, d_amount,d_char);
   RETURN d_clob;
END GET_MENU;
FUNCTION GET_BLOCCO_MENU ( P_UTENTE VARCHAR2, P_ZONA VARCHAR2, P_ID_SEZIONE NUMBER, P_BLOCCO VARCHAR2 DEFAULT NULL) RETURN VARCHAR2 IS
/******************************************************************************
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    20/07/2004 AO     Prima emissione.
******************************************************************************/
d_row_start       VARCHAR2(1000);
d_row_int         VARCHAR2(1000);
d_row_end         VARCHAR2(1000);
d_row             VARCHAR2(32767);
d_document_row    VARCHAR2(32767);
d_current_row     VARCHAR2(32767);
d_href            VARCHAR2(1000);
d_td_style        VARCHAR2(100) := '';
d_sfx             VARCHAR2(10):='Left';
d_nome           VARCHAR2(1000);
d_padre           NUMBER := 0;
d_id_sezione      NUMBER := p_id_sezione;
d_livello         NUMBER := 0;
d_immagine        VARCHAR2(200) ;
d_zona            VARCHAR2(1) := 'N';
d_zona_espansione VARCHAR2(1);
d_zona_tipo       VARCHAR2(1);
d_zona_formato    VARCHAR2(1);
d_visibilita      VARCHAR2(1);
d_max_vis         NUMBER := 0;
d_amount          BINARY_INTEGER := 32767;
d_char            VARCHAR2(32767):='';
d_blocco          VARCHAR2(32767):='';
d_dir_upload      VARCHAR(200);
BEGIN
-- Acquisizione directory di Upload
   d_dir_upload := NVL(Amvweb.GET_PREFERENZA('Directory Upload','{Modulo}'),'common');
-- calcolo estremi della sezione corrente
   SELECT id_padre, nome, id_sezione
        , zona, zona_tipo, zona_formato, zona_espansione
      , visibilita, immagine, max_vis
     INTO d_padre, d_nome, d_id_sezione
       , d_zona, d_zona_tipo, d_zona_formato, d_zona_espansione
      , d_visibilita, d_immagine, d_max_vis
     FROM AMV_SEZIONI
    WHERE id_sezione = p_id_sezione
   ;
-- Assegnazione stile
   IF p_zona = 'D' THEN
      d_sfx := 'Right';
   ELSIF d_zona = 'C' THEN
      d_sfx := 'Center';
   END IF;
-- righe di apertura e chiusura
   d_row_start := '<div class="AFC'||d_sfx||'"><table width="100%" cellpadding="3"  cellspacing="0" class="AFC'||d_sfx||'FormTable">';
   d_row_end   := '</table></div>';
-- creazione righe delle sottosezioni e/o dei documenti
   IF p_blocco IS NULL THEN
      IF d_zona_tipo IN ('S','E') THEN  -- mostro sottosezioni
         d_blocco:= get_blocco_sottosezioni(d_id_sezione,1,p_zona);
      END IF;
   END IF;
   d_livello := get_livello_sezione(p_id_sezione) - get_livello_sezione(get_padre_blocco(p_id_sezione));
   IF d_livello > 1 THEN
      d_td_style := 'style="padding-left:8"';
   END IF;
-- riga relativa ai documenti
   IF d_zona_tipo IN ('D','E') THEN
      d_document_row:= get_blocco_documenti(p_utente, d_id_sezione, 1, d_zona, d_max_vis);
   END IF;
-- riga della sezione corrente
      IF d_immagine IS NOT NULL THEN
        d_immagine := '<img src="../'||d_dir_upload||'/images/'||d_immagine||'" border="0" alt="'||d_nome||'" align="AbsMiddle">&nbsp;';
     END IF;
      IF d_zona = p_zona THEN
         d_href := '<a class="AFC'||d_sfx||'NavigatorLink" href="../common/AmvSezione.do?MVPD=0&MVSZ='||d_id_sezione||'">';
         IF d_zona_formato = 'T' THEN
          d_current_row := '<tr><td align="left" width="100%" class="AFC'||d_sfx||'SelectedColumnTD">'||d_href||d_immagine||d_nome||'</a></td></tr>';
         ELSE
          d_current_row := '<tr><td align="center" width="100%" class="AFC'||d_sfx||'ImageTD">'||d_href||d_immagine||'</a></td></tr>';
       END IF;
       d_row := d_current_row||NVL(d_blocco,p_blocco)||d_document_row;
       RETURN d_row;
     ELSE
         d_href := '<a class="AFC'||d_sfx||'DataLink" href="../common/AmvSezione.do?MVPD=0&MVSZ='||d_id_sezione||'">';
         IF d_visibilita = 'T' OR d_immagine IS NULL THEN
          d_current_row := '<tr><td align="left" width="100%" class="AFC'||d_sfx||'SelectedTD" '||d_td_style||'>'||d_href/*||d_immagine*/||d_nome||'</a></td></tr>';
         ELSE
          d_current_row := '<tr><td align="center" width="100%" class="AFC'||d_sfx||'ImageTD">'||d_href||d_immagine||'</a></td></tr>';
       END IF;
-- ciclo per sezioni di pari livello
         FOR c IN (
             SELECT id_sezione, nome, id_padre, visibilita, zona_espansione, immagine
               FROM AMV_SEZIONI s
              WHERE id_padre = d_padre
                AND NVL(visibilita,'N') != 'N'
              ORDER BY s.sequenza, s.nome
         )
         LOOP
         BEGIN
            d_href := '<a class="AFC'||d_sfx||'DataLink" href="../common/AmvSezione.do?MVPD=0&MVSZ='||c.id_sezione||'">';
            IF c.immagine IS NOT NULL THEN
               d_immagine := '<img src="../'||d_dir_upload||'/images/'||c.immagine||'" border="0" alt="'||c.nome||'" align="AbsMiddle">';
            END IF;
            -- Mostro solo sezioni di primo livello
         IF d_livello < 2 THEN
               IF c.visibilita = 'T' OR c.immagine IS NULL THEN
                  d_row:= '<tr><td class="AFC'||d_sfx||'DetailTD" align="left" '||d_td_style||'>'||d_href/*||d_immagine*/||c.nome||'</a></td></tr>';
               ELSE
                  d_row := '<tr><td align="center" width="100%" class="AFC'||d_sfx||'ImageTD">'||d_href||d_immagine||'</a></td></tr>';
               END IF;
         ELSE
            d_row:= '';
         END IF;
         IF c.id_sezione = p_id_sezione THEN
               d_row:= d_current_row||NVL(d_blocco,p_blocco);
            END IF;
         END;
       d_char := d_char||d_row;
         END LOOP;
         IF d_padre <> 0 THEN
          RETURN get_blocco_menu(p_utente,p_zona, d_padre, d_char);
       ELSE
          RETURN d_char||d_document_row;
       END IF;
      END IF;
END GET_BLOCCO_MENU;
FUNCTION GET_BLOCCO_SOTTOSEZIONI ( P_ID_SEZIONE NUMBER, P_SELECTED NUMBER, P_ZONA VARCHAR2) RETURN VARCHAR2 IS
/******************************************************************************
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    20/07/2004 AO     Prima emissione.
******************************************************************************/
d_row           VARCHAR2(1000);
d_char          VARCHAR2(32767);
d_td            VARCHAR2(1000);
d_livello       NUMBER := 0;
d_td_style      VARCHAR2(100);
d_td_class      VARCHAR2(100);
d_link_class    VARCHAR2(100);
d_sfx           VARCHAR2(10):='Left';
d_pfx           VARCHAR2(20):='';
d_dir_upload      VARCHAR(200);
BEGIN
-- Acquisizione directory di Upload
   d_dir_upload := NVL(Amvweb.GET_PREFERENZA('Directory Upload','{Modulo}'),'common');
-- Assegnazione stile
   IF p_zona = 'D' THEN
      d_sfx := 'Right';
   ELSIF p_zona = 'C' THEN
      d_sfx := 'Center';
     d_pfx := '::&nbsp;';
   END IF;
-- Assegnazione stile righe (selezionate o non selezionate)
   IF p_selected = 1 THEN
      d_td_class :=  'AFC'||d_sfx||'DetailTD';
     d_link_class := 'AFC'||d_sfx||'DataLink';
   ELSE
      d_td_class :=  'AFC'||d_sfx||'DataTD';
     d_link_class := 'AFC'||d_sfx||'DataLink';
   END IF;
-- creazione righe delle sottosezioni
      FOR c IN (
          SELECT id_sezione, nome, immagine, visibilita
            FROM AMV_SEZIONI
           WHERE id_padre = p_id_sezione
             AND id_sezione <> 0
          AND NVL(visibilita,'N') != 'N'
           ORDER BY sequenza, nome
      )
      LOOP
      BEGIN
         -- livello della sezione corrente
         d_livello := get_livello_sezione(c.id_sezione) - get_livello_sezione(get_padre_blocco(c.id_sezione));
       -- indentazione del livello corrente
       IF d_livello > 1 AND p_zona != 'C' THEN
          d_td_style := 'style="padding-left:12"';
        END IF;
         d_td := '<td class="'||d_td_class||'" align="left" valign = "top"'||d_td_style||'>';
         -- creazione della riga
         IF c.visibilita = 'T' OR c.immagine IS NULL OR p_zona = 'C' THEN
            d_row:= '<tr>'||d_td||d_pfx||'<a class="'||d_link_class||'" href="../common/AmvSezione.do?MVPD=0&MVSZ='||c.id_sezione||'">'||c.nome||'</a></td></tr>';
         ELSE
            d_row := '<tr><td align="center" width="100%" class="AFC'||d_sfx||'ImageTD"><a class="AFC'||d_sfx||'NavigatorLink" href="../common/AmvSezione.do?MVPD=0&MVSZ='||c.id_sezione||'"><img src="../'||d_dir_upload||'/images/'||c.immagine||'" border="0" alt="'||c.nome||'"></a></td></tr>';
         END IF;
       d_char := d_char||d_row;
      END;
      END LOOP;
 RETURN d_char;
END GET_BLOCCO_SOTTOSEZIONI;
FUNCTION GET_BLOCCO_DOCUMENTI (
  P_UTENTE     VARCHAR2
, P_ID_SEZIONE NUMBER
, P_SELECTED NUMBER
, P_ZONA    VARCHAR2
, P_MAX_VIS NUMBER
, P_ICONA     VARCHAR2 DEFAULT NULL
) RETURN    VARCHAR2 IS
/******************************************************************************
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
  0    20/07/2004 AO     Prima emissione.
  1    30/03/2005 AO     Ordinamento documenti sui campi sequenza e titolo
******************************************************************************/
d_row              VARCHAR2(1000);
d_url              VARCHAR2(1000);
d_char             VARCHAR2(32767);
d_space            VARCHAR2(1000);
d_livello          NUMBER := 0;
d_count_richieste  NUMBER := 0;
d_stato_richieste  VARCHAR2(1);
d_diritto_modifica NUMBER(1);
d_id_figlio        NUMBER;
d_link_figlio      VARCHAR2(200);
d_link_richieste   VARCHAR2(1000);
d_td_style         VARCHAR2(100);
d_td_class         VARCHAR2(100);
d_td_align         VARCHAR2(100);
d_link_class       VARCHAR2(100);
d_sfx              VARCHAR2(10):='Left';
d_pfx              VARCHAR2(200);
d_pfx_sez          VARCHAR2(200);
d_img              VARCHAR2(200);
d_link_form         VARCHAR2(200);
d_rw            VARCHAR2(5) := '';
d_dir_upload      VARCHAR(200);
BEGIN
-- Acquisizione directory di Upload
   d_dir_upload := NVL(Amvweb.GET_PREFERENZA('Directory Upload','{Modulo}'),'common');
-- Assegnazione stile
   IF p_zona = 'D' THEN
      d_sfx := 'Right';
   ELSIF p_zona = 'C' THEN
      d_sfx := 'Center';
     d_pfx := '::&nbsp;';
      IF p_icona IS NOT NULL THEN
         d_pfx_sez := '<img src="../common/images/icone/'||p_icona||'" border="0">';
      END IF;
   END IF;
-- Assegnazione stile righe (selezionate o non selezionate)
   IF p_selected = 1 THEN
      d_td_class :=  'AFC'||d_sfx||'DetailTD';
     d_link_class := 'AFC'||d_sfx||'DataLink';
   ELSE
      d_td_class :=  'AFC'||d_sfx||'DataTD';
     d_link_class := 'AFC'||d_sfx||'DataLink';
   END IF;
-- creazione righe dei documenti
   FOR c IN (
   SELECT id_documento
        , revisione
        , titolo
      , tipo_testo
      , testo
        , LINK
      , immagine
      , icona
      , id_tipologia
      , id_documento_padre
     FROM (SELECT id_documento
                , revisione
                , titolo
            , tipo_testo
            , testo
                , LINK
            , immagine
            , icona
              , id_tipologia
            , id_documento_padre
           FROM AMV_DOCUMENTI
            WHERE Amv_Documento.get_diritto_area(p_utente, id_area, id_tipologia) IS NOT NULL
              AND SYSDATE BETWEEN INIZIO_PUBBLICAZIONE AND NVL(FINE_PUBBLICAZIONE,SYSDATE)
              AND id_sezione = p_id_sezione --decode(p_id_sezione, 0, id_sezione, p_id_sezione)
              AND stato IN ('U','R')
            ORDER BY sequenza, titolo
         )
     WHERE ROWNUM <= NVL(p_max_vis,999)
   )
   LOOP
      BEGIN
       d_count_richieste := 0;
       d_link_richieste := '';
        IF c.tipo_testo = 'Form' THEN
          BEGIN
            SELECT COUNT(*)
                 , MIN(id_documento)
                  , MIN(stato)
                 , MIN(Amv_Revisione.get_diritto_modifica(p_utente,id_documento, revisione))
                    , MIN(LINK)
              INTO d_count_richieste
                , d_id_figlio
                , d_stato_richieste
                , d_diritto_modifica
               , d_link_figlio
              FROM AMV_DOCUMENTI
                WHERE id_documento_padre = c.id_documento
              AND tipo_testo = 'Richiesta'
              AND stato IN ('B','N','V')
              AND Amv_Revisione.get_diritto_modifica(p_utente, id_documento, revisione) =1
               AND p_utente != 'GUEST'
            ;
         EXCEPTION WHEN OTHERS THEN NULL;
         END;
       END IF;
         IF c.icona IS NOT NULL THEN
            d_pfx := '<img src="../common/images/icone/'||c.icona||'" border="0">';
       ELSE
            d_pfx := d_pfx_sez;
         END IF;
         d_row:= Amv_Documento.get_riga( c.id_documento,c.revisione
                       , p_id_sezione, p_zona, 'N', c.titolo
                   , c.LINK, c.immagine
                   , c.id_tipologia, c.tipo_testo
                   , c.id_documento_padre, d_dir_upload
                   , d_td_class, d_link_class, d_pfx);
       d_char:= d_char||d_row;
      END;
   END LOOP;
 RETURN d_char;
END GET_BLOCCO_DOCUMENTI;
FUNCTION GET_NAVIGATORE (P_ID_SEZIONE NUMBER, P_UTENTE VARCHAR2, P_MODULO VARCHAR2)
RETURN VARCHAR2 IS
d_path  VARCHAR2(4000);
d_padre NUMBER;
d_id_sezione NUMBER;
d_sequenza NUMBER;
d_nome  VARCHAR2(100) := '';
BEGIN
   BEGIN
      SELECT id_padre, nome, id_sezione
       INTO d_padre, d_nome, d_id_sezione
       FROM AMV_SEZIONI
      WHERE id_sezione = p_id_sezione
        AND id_padre <> id_sezione;
   EXCEPTION WHEN NO_DATA_FOUND THEN
      DECLARE
     d_usa_home_page NUMBER(1);
     BEGIN
        RETURN '';
      END;
   END;
   d_path := get_navigatore(d_padre, p_utente, p_modulo)||'&nbsp;::&nbsp;<a class="AFCDataLink" href="../common/AmvSezione.do?MVPD=0&MVSZ='||d_id_sezione||'">'||d_nome||'</a>';
   RETURN d_path;
END GET_NAVIGATORE;
FUNCTION GET_ALBERO_SEZIONI (
  P_ID_SEZIONE   NUMBER
, P_ID_TIPOLOGIA NUMBER
, P_ID_CATEGORIA NUMBER
, P_ID_ARGOMENTO NUMBER
, P_TIPO_VISUALIZZAZIONE VARCHAR2
, P_UTENTE VARCHAR2
, P_PAGE   VARCHAR2
) RETURN CLOB
IS
BEGIN
RETURN Afc_Html.TREE(
      P_ID_SEZIONE,
      'AMV',
      'SEZIONI',
      P_PAGE,
      'amv_sezioni',
      'id_padre',
      'id_sezione',
      'nome',
      'sequenza || nome',
      NULL,
      'N',
      'N',
      'S',
      'N',
      '',
      '',
      'SZ',
      P_FOGLIA => 'cmsfolderclosed.gif'
                     );
END GET_ALBERO_SEZIONI;
FUNCTION GET_DOCUMENTI_SEZIONE (
  P_ID_SEZIONE   NUMBER
, P_UTENTE VARCHAR2
) RETURN VARCHAR2
IS
d_row   VARCHAR2(32000);
d_char  VARCHAR2(32767) := '<b>Documenti Sezione</b><br>';
d_img   VARCHAR2(1000) := '<img border="0" src="../common/images/AMV/cmsdoc.gif" align="AbsMiddle">';
d_img_node   VARCHAR2(1000) := '<img border="0" src="../common/images/AMV/cmsnode.gif" align="AbsMiddle">';
d_img_line VARCHAR2(1000) := '<img border="0" src="../common/images/AMV/cmsvertline.gif" align="AbsMiddle">';
d_img_blank VARCHAR2(1000) := '<img border="0" src="../common/images/AMV/cmsblank.gif" align="AbsMiddle">';
i NUMBER;
d_nome_sezione VARCHAR2(100);
d_nrows NUMBER := 0;
BEGIN
-- numero di record del cursore
   SELECT COUNT(1)
     INTO d_nrows
     FROM amv_vista_documenti
    WHERE utente = p_utente
      AND id_sezione = p_id_sezione
     ;
   SELECT nome
     INTO d_nome_sezione
     FROM AMV_SEZIONI
    WHERE id_sezione = p_id_sezione
     ;
   d_char  := '<b>'||d_nome_sezione||'</b><br>';
   d_char:= '';
-- documenti per la sezione selezionata
   FOR c IN (
     SELECT DISTINCT
      ID_DOCUMENTO, ID_TIPOLOGIA, DES_TIPOLOGIA, ID_CATEGORIA, DES_CATEGORIA
      , ID_ARGOMENTO, DES_ARGOMENTO, ID_RILEVANZA, DES_RILEVANZA
      , IMPORTANZA, SEQUENZA, ID_AREA
      , TITOLO, TIPO_TESTO, LINK
      , DATA_RIFERIMENTO, INIZIO_PUBBLICAZIONE, FINE_PUBBLICAZIONE
      , AUTORE, NOME_AUTORE
      , DATA_INSERIMENTO
      , UTENTE_AGGIORNAMENTO, NOME_UTENTE
      , DATA_ULTIMA_MODIFICA
      , UTENTE
      , EDIT
      , DECODE(EDIT,'SI','Modifica','') AS MODIFICA
     , ID_SEZIONE
      FROM AMV_VISTA_DOCUMENTI
      WHERE UTENTE = p_utente
       AND ID_SEZIONE = P_ID_SEZIONE
      AND stato IN ('U','R')
    ORDER BY SEQUENZA, TITOLO
   )
   LOOP
   BEGIN
     d_row := '<a href="AmvDocumentoInfo.do?ID='||c.id_documento||'">'||d_img||c.titolo||' ('||TO_CHAR(c.data_riferimento,'dd/mm/yyyy')||')'||'</a>&nbsp;<br>';
     d_char:=d_char||d_row;
   END;
   END LOOP;
   IF d_nrows = 0 THEN
      d_char:= d_char||'<i>Nessun documento presente</i>';
   END IF;
   RETURN d_char;
END GET_DOCUMENTI_SEZIONE;
FUNCTION GET_PATH_SEZIONE (P_ID_SEZIONE NUMBER, P_PATH VARCHAR2 DEFAULT NULL)
RETURN VARCHAR2 IS
d_path  VARCHAR2 (200);
d_padre NUMBER;
d_nome  VARCHAR2(100) := '';
BEGIN
   BEGIN
      SELECT id_padre, nome
       INTO d_padre, d_nome
       FROM AMV_SEZIONI
      WHERE id_sezione = p_id_sezione
        AND id_padre <> id_sezione;
   EXCEPTION WHEN NO_DATA_FOUND THEN
         --return '0'||p_path;
       RETURN d_nome||p_path;
   END;
   d_path := get_path_sezione(d_padre, p_path)||' '||TO_CHAR(p_id_sezione)||'.';
   RETURN d_path;
END GET_PATH_SEZIONE;
FUNCTION GET_SORTED_PATH_SEZIONE (P_ID_SEZIONE NUMBER, P_PATH VARCHAR2 DEFAULT NULL)
RETURN VARCHAR2 IS
d_path  VARCHAR2(200);
d_padre NUMBER;
d_sequenza NUMBER;
d_nome  VARCHAR2(100) := '';
BEGIN
   BEGIN
      SELECT id_padre, nome, sequenza
       INTO d_padre, d_nome, d_sequenza
       FROM AMV_SEZIONI
      WHERE id_sezione = p_id_sezione
        AND id_padre <> id_sezione;
   EXCEPTION WHEN NO_DATA_FOUND THEN
         RETURN d_nome;
   END;
   d_path := get_sorted_path_sezione(d_padre, 'padri')||' '||d_sequenza||d_nome||'.';
   RETURN d_path;
END GET_SORTED_PATH_SEZIONE;
FUNCTION GET_GRAPHIC_PATH_SEZIONE (
  P_ID_SEZIONE NUMBER
, P_CHILD t_array
, P_CHILD_RIMANENTI t_array
, P_PATH VARCHAR2 DEFAULT NULL)
RETURN VARCHAR2 IS
d_path  VARCHAR2 (32000);
d_nfigli NUMBER := 0;
d_padre NUMBER;
d_padre2 NUMBER;
d_sezione NUMBER;
d_img_line VARCHAR2(1000) := '<img border="0" src="../common/images/AMV/cmsvertline.gif" align="AbsMiddle">';
d_img_blank VARCHAR2(1000) := '<img border="0" src="../common/images/AMV/cmsblank.gif" align="AbsMiddle">';
BEGIN
   BEGIN
      BEGIN  -- nodo padre
     SELECT id_padre
       INTO d_padre
       FROM AMV_SEZIONI
      WHERE id_sezione =p_id_sezione;
     SELECT id_padre -- nodo padre del padre
       INTO d_padre2
      FROM AMV_SEZIONI
      WHERE id_sezione =
      (SELECT id_padre
         FROM AMV_SEZIONI
        WHERE id_sezione =p_id_sezione);
      EXCEPTION WHEN NO_DATA_FOUND THEN
         RETURN '';
     END;
     -- Numero di figli della sezione
      SELECT COUNT(id_sezione)-DECODE(p_id_sezione,0,1,0), MAX(id_padre)
       INTO d_nfigli, d_sezione
       FROM AMV_SEZIONI
      WHERE id_padre = d_padre
        AND id_sezione <> 0
     ;
   IF (p_id_sezione = 0 OR d_padre=0) THEN
         RETURN '';
   END IF;
   IF p_child_rimanenti(d_padre2)>0 THEN
      d_path := get_graphic_path_sezione(d_padre, p_child,p_child_rimanenti, p_path)||d_img_line;
   ELSE
      d_path:= get_graphic_path_sezione(d_padre, p_child,p_child_rimanenti, p_path)||d_img_blank;
   END IF;
   END;
   RETURN d_path;
END GET_GRAPHIC_PATH_SEZIONE;
FUNCTION GET_LIVELLO_SEZIONE (P_ID_SEZIONE NUMBER, P_LIVELLO NUMBER DEFAULT 0)
RETURN NUMBER IS
d_livello NUMBER := p_livello;
d_padre   NUMBER;
BEGIN
   BEGIN
      SELECT id_padre
       INTO d_padre
       FROM AMV_SEZIONI
      WHERE id_sezione = p_id_sezione
        AND id_padre <> id_sezione;
   EXCEPTION WHEN NO_DATA_FOUND THEN
      RETURN d_livello;
   END;
   d_livello := get_livello_sezione(d_padre, p_livello + 1);
   RETURN d_livello;
END GET_LIVELLO_SEZIONE;
FUNCTION GET_PADRE_BLOCCO (P_ID_SEZIONE NUMBER, P_ZONA VARCHAR2 DEFAULT NULL)
RETURN NUMBER IS
d_return  NUMBER;
d_padre   NUMBER;
d_zona    VARCHAR2(1);
BEGIN
   BEGIN
      SELECT id_padre, zona
        INTO d_padre, d_zona
        FROM AMV_SEZIONI
       WHERE id_sezione = p_id_sezione
           AND id_padre <> id_sezione
      ;
   EXCEPTION WHEN NO_DATA_FOUND THEN
      RETURN 0;
   END;
   IF p_zona IS NULL THEN
      IF d_zona IS NOT NULL AND d_zona != 'C' THEN
         d_return := p_id_sezione;
     ELSE
        d_return := get_padre_blocco(d_padre);
     END IF;
   ELSE
      IF d_zona = p_zona THEN
         d_return := p_id_sezione;
      ELSE
         d_return := get_padre_blocco(d_padre, p_zona);
      END IF;
   END IF;
   RETURN d_return;
END GET_PADRE_BLOCCO;
FUNCTION GET_PADRE (P_ID_SEZIONE NUMBER)
RETURN NUMBER IS
d_padre   NUMBER;
BEGIN
   BEGIN
      SELECT id_padre
        INTO d_padre
        FROM AMV_SEZIONI
       WHERE id_sezione = p_id_sezione
      ;
   EXCEPTION WHEN NO_DATA_FOUND THEN
      RETURN 0;
   END;
   RETURN d_padre;
END GET_PADRE;
FUNCTION GET_VISIBILITA (
  P_ID_SEZIONE NUMBER
, P_ID_SEZIONE_SEL NUMBER
, P_PAGE VARCHAR2
, P_ZONA_VISIBILITA VARCHAR2
)
RETURN NUMBER IS
d_return  NUMBER := 0;
BEGIN
   IF INSTR(p_page,'Main') > 0 THEN
      IF p_zona_visibilita IN ('H','E') THEN
        d_return := 1;
     END IF;
      IF p_zona_visibilita IN ('C','E') THEN
        IF get_padre(p_id_sezione) = 0 THEN
          d_return := 1;
       END IF;
     END IF;
   ELSE
      IF p_zona_visibilita IN ('C','E') THEN
     -- la sezione e padre di quella selezionata
        IF p_id_sezione = get_padre(p_id_sezione_sel)
     -- la sezione e quella selezionata
       OR p_id_sezione = p_id_sezione_sel
     -- la sezione e un blocco ed e padre(in senso lato) di quella selezionata
       OR p_id_sezione = get_padre_blocco(p_id_sezione_sel)
       OR get_padre(p_id_sezione) = get_padre_blocco(p_id_sezione_sel)
     -- la sezione e figlia di quella selezionata
       OR get_padre(p_id_sezione) = p_id_sezione_sel THEN
          d_return := 1;
         END IF;
     ELSE
       IF p_id_sezione = p_id_sezione_sel
      OR p_id_sezione = get_padre_blocco(p_id_sezione_sel) THEN
         d_return := 1;
      END IF;
     END IF;
   END IF;
   IF p_zona_visibilita = 'S' THEN
      d_return := 1;
   END IF;
   RETURN d_return;
END GET_VISIBILITA;
PROCEDURE INSERISCI_SEZIONE (
  p_id_sezione   NUMBER
, p_nome         VARCHAR2
, p_descrizione  VARCHAR2
, p_zona         VARCHAR2
, p_sequenza     NUMBER
, p_immagine     VARCHAR2
, p_max_vis      NUMBER
, p_id_padre     NUMBER
) IS
BEGIN
-- inserimento nuova sezione
INSERT INTO AMV_SEZIONI(id_sezione, nome, descrizione, zona, sequenza, immagine, max_vis, id_padre)
 VALUES(p_id_sezione, p_nome, p_descrizione, p_zona, p_sequenza, p_immagine, p_max_vis, p_id_padre);
END INSERISCI_SEZIONE;
FUNCTION GET_PREFERENZA (
  P_ID_SEZIONE NUMBER
, P_PREFERENZA VARCHAR2
)
RETURN VARCHAR2 IS
rows_processed INTEGER;
cursor_name    INTEGER;
p_stringa      VARCHAR2(200);
d_valore       VARCHAR2(1000) := '';
d_padre        NUMBER(10);
BEGIN
   p_stringa := 'select '||p_preferenza||', id_padre'
              ||' from amv_sezioni'
           ||' where id_sezione = :condition';
   cursor_name   := DBMS_SQL.OPEN_CURSOR;
   DBMS_SQL.PARSE(cursor_name,p_stringa,dbms_sql.native);
   DBMS_SQL.BIND_VARIABLE(cursor_name,':condition',p_id_sezione);
   DBMS_SQL.DEFINE_COLUMN(cursor_name,1,d_valore,1000);
   DBMS_SQL.DEFINE_COLUMN(cursor_name,2,d_padre);
   rows_processed:= DBMS_SQL.EXECUTE(cursor_name);
   IF DBMS_SQL.FETCH_ROWS(cursor_name) > 0 THEN
   DBMS_SQL.COLUMN_VALUE(cursor_name,1,d_valore);
   DBMS_SQL.COLUMN_VALUE(cursor_name,2,d_padre);
      IF d_valore IS NULL AND d_padre != p_id_sezione THEN
        d_valore := get_preferenza(d_padre, p_preferenza);
     END IF;
   ELSE
   d_valore := '';
   END IF;
   DBMS_SQL.CLOSE_CURSOR(cursor_name);
   RETURN d_valore;
END;
FUNCTION GET_HEADER_BAR (
  P_UTENTE VARCHAR2
, P_ID_SEZIONE NUMBER
, P_PAGE VARCHAR2
) RETURN VARCHAR2 IS
d_nome           VARCHAR2(1000);
d_padre           NUMBER := 0;
d_id_sezione      NUMBER;
d_livello         NUMBER := 0;
d_immagine        VARCHAR2(200) ;
d_zona            VARCHAR2(1);
d_zona_espansione VARCHAR2(1);
d_zona_tipo       VARCHAR2(1);
d_zona_formato    VARCHAR2(1);
d_max_vis         NUMBER := 0;
d_link            VARCHAR2(1000);
d_return          VARCHAR2(32000);
BEGIN
   FOR c IN (
      -- sottosezioni zona alta
      SELECT id_sezione
          , nome
        FROM AMV_SEZIONI
      WHERE zona = 'A'
        AND (id_padre = p_id_sezione OR zona_visibilita = 'S')
        AND get_visibilita(id_sezione, p_id_sezione, p_page, zona_visibilita) = 1
      AND  DECODE(id_area,'','R',Amv_Area.get_diritto_utente(p_utente, id_area)) IN ('R','C','V','A','U')
     ORDER BY sequenza
      )
   LOOP
      BEGIN
         d_link:= '&nbsp;|&nbsp;'||'<a class="AFCHeaderBarMenu" href="../common/AmvSezione.do?MVPD=0&MVSZ='||c.id_sezione||'">'||c.nome||'</a>';
         d_return := d_return||d_link;
      END;
   END LOOP;
   RETURN d_return;
END GET_HEADER_BAR;
FUNCTION GET_BLOCCO_MODULISTICA ( P_UTENTE VARCHAR2, P_ID_SEZIONE NUMBER, P_ZONA VARCHAR2) RETURN VARCHAR2 IS
/******************************************************************************
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
  0    20/07/2004 AO     Prima emissione.
  1    22/12/2004 AO     Gestione richieste con inoltro immediato senza approvazione
******************************************************************************/
d_row_start     VARCHAR2(1000);
d_row_int       VARCHAR2(1000);
d_row_end       VARCHAR2(1000);
d_row_sep       VARCHAR2(200);
d_row              VARCHAR2(1000);
d_url              VARCHAR2(1000);
d_char             VARCHAR2(32767);
d_space            VARCHAR2(1000);
d_count            NUMBER := 0;
d_count_richieste  NUMBER := 0;
d_stato_richieste  VARCHAR2(1);
d_mie_richieste    VARCHAR2(1000);
d_tutte_richieste  VARCHAR2(1000);
d_titolo           VARCHAR2(1000);
d_diritto_modifica NUMBER(1);
d_id_figlio        NUMBER;
d_td_style         VARCHAR2(100);
d_td_class         VARCHAR2(100);
d_td_align         VARCHAR2(100);
d_link_class       VARCHAR2(100);
d_sfx              VARCHAR2(10):='Left';
d_pfx              VARCHAR2(20);
d_img              VARCHAR2(200);
d_link_form         VARCHAR2(200);
d_rw            VARCHAR2(5) := '';
d_dir_upload      VARCHAR(200);
BEGIN
-- Assegnazione stile
   IF p_zona = 'D' THEN
      d_sfx := 'Right';
   ELSIF p_zona = 'C' THEN
      d_sfx := 'Center';
     d_pfx := '::&nbsp;';
   END IF;
-- Assegnazione stile righe
      d_td_class :=  'AFC'||d_sfx||'DataTD';
     d_link_class := 'AFC'||d_sfx||'DataLink';
-- creazione riga relativa alle proprie richieste
   SELECT COUNT(1)
     INTO d_count
    FROM AMV_DOCUMENTI d, AMV_DOCUMENTI padri
         WHERE d.tipo_testo = 'Richiesta'
         AND d.ID_DOCUMENTO_PADRE = padri.ID_DOCUMENTO
         AND d.REVISIONE = padri.REVISIONE
         AND INSTR(padri.LINK, 'iter=N inoltro=I') < 1
         AND d.autore = p_utente
   ;
   IF d_count > 0 THEN
      d_mie_richieste := Amvweb.get_preferenza('Le mie richieste');
      d_url := '<a class="'||d_link_class||'" href="../restrict/AmvRichiesteAutore.do">'||NVL(d_mie_richieste,'Le mie richieste')||'</a>';
      d_row := '<tr><td class="'||d_td_class||'" width="100%" '||d_td_align||'>'||d_url||'</td></tr>';
      d_char:= d_char||d_row;
   END IF;
   d_count := 0;
-- creazione riga relativa alle richieste da gestire
   SELECT COUNT(1)
     INTO d_count
    FROM AMV_DOCUMENTI d, AMV_DOCUMENTI padri
         WHERE d.tipo_testo = 'Richiesta'
         AND d.ID_DOCUMENTO_PADRE = padri.ID_DOCUMENTO
         AND d.REVISIONE = padri.REVISIONE
         AND INSTR(padri.LINK, 'iter=N inoltro=I') < 1
         AND d.stato NOT IN ('B','A','U','F')
         AND  Amv_Revisione.get_diritto_modifica(p_utente, d.id_documento, d.revisione) =1
   ;
   IF d_count > 0 THEN
      d_tutte_richieste := Amvweb.get_preferenza('Tutte le richieste');
      d_url := '<a class="'||d_link_class||'" href="../restrict/AmvRichiesteElenco.do">'||NVL(d_tutte_richieste,'Tutte le richieste')||'</a>';
      d_row := '<tr><td class="'||d_td_class||'" width="100%" '||d_td_align||'>'||d_url||'</td></tr>';
      d_char:= d_char||d_row;
   END IF;
   IF d_char IS NOT NULL THEN
      d_titolo := Amvweb.get_preferenza('Titolo blocco richieste');
      d_row_start := '<div class="AFC'||d_sfx||'"><table width="100%" cellpadding="3"  cellspacing="0" class="AFC'||d_sfx||'FormTable">';
     d_row_end   := '</table></div>';
      d_row_sep   := '<tr><td class="AFC'||d_sfx||'SeparatorTD" width="100%"><img src="../common/images/AMV/blank_separator.gif"></tr></td>';
      d_row_int   := '<tr><td class="AFC'||d_sfx||'ColumnTD" align="left" valign = "top">'||NVL(d_titolo,'Richieste on-line')||'</a></td></tr>';
      d_char := d_row_start||d_row_int||d_char||d_row_sep||d_row_end;
   END IF;
 RETURN d_char;
END GET_BLOCCO_MODULISTICA;
FUNCTION GET_NOME (
  P_ID_SEZIONE NUMBER
)
RETURN VARCHAR2 IS
d_valore       VARCHAR2(2000) := '';
BEGIN
   SELECT nome
     INTO d_valore
    FROM AMV_SEZIONI
    WHERE id_sezione = p_id_sezione;
   RETURN d_valore;
   EXCEPTION WHEN NO_DATA_FOUND THEN
    d_valore := '';
   RETURN d_valore;
END;
END Amv_Sezione;
/* End Procedure: AMV_SEZIONE */
/

