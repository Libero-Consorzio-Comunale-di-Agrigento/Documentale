CREATE OR REPLACE PACKAGE BODY Amv_Revisione AS
FUNCTION versione  RETURN VARCHAR2 IS
/******************************************************************************
 NOME:        VERSIONE
 DESCRIZIONE: Restituisce la versione e la data di distribuzione del package.
 PARAMETRI:   --
 RITORNA:     stringa varchar2 contenente versione e data.
 ******************************************************************************/
BEGIN
   RETURN d_versione||' Rev.'||d_revisione;
END versione;
PROCEDURE creazione_revisione (
  P_ID_DOCUMENTO         NUMBER
, P_REVISIONE            NUMBER
, P_UTENTE               VARCHAR2
, P_UTENTE_REDAZIONE     VARCHAR2
, P_UTENTE_VERIFICA      VARCHAR2
, P_UTENTE_APPROVAZIONE  VARCHAR2
, P_INIZIO_PUBBLICAZIONE DATE
, P_FINE_PUBBLICAZIONE   DATE
) IS
d_area      NUMBER;
d_diritto   VARCHAR2(1);
d_id_tipologia  NUMBER;
d_id_categoria  NUMBER;
d_id_argomento  NUMBER;
d_id_rilevanza  NUMBER;
d_id_area       NUMBER;
d_id_sezione    NUMBER;
d_revisione     NUMBER;
d_stato         VARCHAR2(1);
d_titolo        VARCHAR2(4000);
d_tipo_testo    VARCHAR2(10);
d_testo         CLOB;
d_link          VARCHAR2(200);
d_data_riferimento      DATE;
d_data_inserimento      DATE;
d_inizio_pubblicazione  DATE;
d_fine_pubblicazione    DATE;
d_autore        VARCHAR2(8);
d_cronologia        VARCHAR2(4000);
d_nominativo_autore VARCHAR2(40);
BEGIN
-- Verifica che utente possa creare revisione
   -- DA FARE
-- Documento in stato 'R'
   UPDATE AMV_DOCUMENTI
      SET STATO = 'R'
      WHERE ID_DOCUMENTO = P_ID_DOCUMENTO
     AND REVISIONE = P_REVISIONE
   ;
-- Dati del documento
   SELECT id_tipologia, id_categoria, id_argomento, id_rilevanza, id_area
      , titolo, tipo_testo, LINK
      , data_riferimento, p_inizio_pubblicazione, p_fine_pubblicazione
      , autore, data_inserimento
      , testo, id_sezione, stato
    INTO d_id_tipologia, d_id_categoria, d_id_argomento, d_id_rilevanza, d_id_area
      , d_titolo, d_tipo_testo, d_link
      , d_data_riferimento, d_inizio_pubblicazione, d_fine_pubblicazione
      , d_autore, d_data_inserimento
      , d_testo, d_id_sezione, d_stato
     FROM AMV_DOCUMENTI
   WHERE id_documento = p_id_documento
     AND revisione = p_revisione
   ;
   SELECT MAX(revisione)+1
     INTO d_revisione
    FROM AMV_DOCUMENTI
   WHERE id_documento = p_id_documento
   ;
-- Creazione nuovo documento in stato 'B' con numero di revisione maggiorato di 1
   INSERT INTO AMV_DOCUMENTI(
          id_documento, id_tipologia, id_categoria, id_argomento, id_rilevanza, id_area
      , titolo, tipo_testo, LINK
      , data_riferimento, inizio_pubblicazione, fine_pubblicazione
      , autore, data_inserimento
      , utente_aggiornamento, data_aggiornamento
      , testo, id_sezione, revisione, stato)
      VALUES (
        p_id_documento, d_id_tipologia, d_id_categoria, d_id_argomento, d_id_rilevanza, d_id_area
      , d_titolo, d_tipo_testo, d_link
      , d_data_riferimento, p_inizio_pubblicazione, p_fine_pubblicazione
      , d_autore, SYSDATE
      , p_utente, SYSDATE
      , d_testo, d_id_sezione, d_revisione, 'B')
   ;
-- Inserimento degli attributi della revisione su tabella AMV_DOCUMENTI_REVISIONI
   d_nominativo_autore := ad4_utente.get_nominativo(p_utente);
   d_cronologia := CHR(10)||'- '||TO_CHAR(SYSDATE,'dd/mm/yyyy hh:mi')||' - '||d_nominativo_autore||' - Creazione Revisione';
   INSERT INTO AMV_DOCUMENTI_REVISIONI(
               id_documento, revisione
          , utente_redazione, utente_verifica, utente_approvazione
          , cronologia)
      VALUES (
             p_id_documento, d_revisione
          , p_utente_redazione, p_utente_verifica, p_utente_approvazione
          , d_cronologia)
   ;
-- Inserimento dei riferimenti agli allegati
   INSERT INTO AMV_DOCUMENTI_BLOB (
                ID_DOCUMENTO, ID_BLOB, REVISIONE)
    SELECT id_documento, id_blob,  d_revisione
      FROM AMV_DOCUMENTI_BLOB
    WHERE id_documento = p_id_documento
      AND revisione = d_revisione;
END creazione_revisione;
PROCEDURE gestisci_revisione (
  p_id_documento NUMBER
, p_revisione    NUMBER
, p_stato_futuro VARCHAR2
, p_note         VARCHAR2
, p_autore       VARCHAR2
, p_destinatario VARCHAR2 DEFAULT NULL
) IS
d_cronologia        VARCHAR2(4000);
d_note              VARCHAR2(4000);
d_nominativo_autore VARCHAR2(40);
BEGIN
-- Nuovo Documento nello stato futuro
   UPDATE AMV_DOCUMENTI
      SET stato = p_stato_futuro
      WHERE id_documento = p_id_documento
     AND revisione = p_revisione
   ;
-- Inserimento degli attributi della revisione su tabella AMV_DOCUMENTI_REVISIONI
   IF p_stato_futuro ='N' THEN
      d_cronologia := '- Redazione';
     d_note := '(Redazione)';
   ELSIF p_stato_futuro = 'V' THEN
      d_cronologia := '- Verifica';
     d_note := '(Verifica)';
   ELSIF p_stato_futuro ='A' THEN
      d_cronologia := '- Approvazione';
     d_note := '(Approvazione)';
   ELSIF p_stato_futuro = 'B' THEN
      d_cronologia := '- Respinto';
     d_note := '(Respinto)';
   ELSIF p_stato_futuro = 'F' THEN
      d_cronologia := '- Rifiutato';
     d_note := '(Rifiutato)';
   END IF;
   d_nominativo_autore := ad4_utente.get_nominativo(p_autore);
   d_cronologia := '- '||TO_CHAR(SYSDATE,'dd/mm/yyyy hh:mi')||' - '||d_nominativo_autore||d_cronologia ;
   IF p_note IS NOT NULL THEN
      d_note := TO_CHAR(SYSDATE,'dd/mm/yyyy hh:mi')||' '||d_note||CHR(10)||p_note||CHR(10);
   ELSE
      d_note :='';
   END IF;
   UPDATE AMV_DOCUMENTI_REVISIONI
      SET data_redazione = DECODE(p_stato_futuro,'N',SYSDATE, data_redazione)
      , data_verifica = DECODE(p_stato_futuro,'V',SYSDATE, data_verifica)
      , data_approvazione = DECODE(p_stato_futuro,'A',SYSDATE, data_approvazione)
        , utente_verifica = DECODE(p_stato_futuro,'N',NVL(p_destinatario,utente_verifica),utente_verifica)
      , utente_approvazione = DECODE(p_stato_futuro,'V',NVL(p_destinatario,utente_approvazione),utente_approvazione)
      , cronologia = DECODE(cronologia,NULL,d_cronologia,cronologia||CHR(10)||d_cronologia)
      , note = DECODE(note,NULL,d_note,note||CHR(10)||d_note)
    WHERE id_documento = p_id_documento
     AND revisione = p_revisione
   ;
     IF P_STATO_FUTURO = 'F' THEN
        UPDATE AMV_DOCUMENTI
           SET STATO = 'U'
         WHERE ID_DOCUMENTO = P_ID_DOCUMENTO
        AND STATO = 'R'
       AND NOT EXISTS
          (SELECT 'x'
            FROM AMV_DOCUMENTI
           WHERE id_documento = p_id_documento
             AND stato = 'U'
         )
       ;
     END IF;
END gestisci_revisione;
PROCEDURE pubblicazione_revisione (
  P_ID_DOCUMENTO         NUMBER
, P_REVISIONE            NUMBER
, P_INIZIO_PUBBLICAZIONE DATE
, P_FINE_PUBBLICAZIONE   DATE
) IS
BEGIN
-- Precedente Documento in stato 'F'
   UPDATE AMV_DOCUMENTI
      SET stato = 'F'
      WHERE id_documento = p_id_documento
     AND stato = 'R'
   ;
-- Nuovo Documento in stato 'U' con eventuali date di inizio e fine pubblicazione
   UPDATE AMV_DOCUMENTI
      SET stato = 'U'
       , inizio_pubblicazione = NVL(p_inizio_pubblicazione, inizio_pubblicazione)
      , fine_pubblicazione = NVL(p_fine_pubblicazione, fine_pubblicazione)
      WHERE id_documento = p_id_documento
     AND revisione = p_revisione
   ;
END pubblicazione_revisione;
PROCEDURE notifica_revisione (
  P_UTENTE IN VARCHAR2
, P_MSG IN VARCHAR2
) IS
d_soggetto NUMBER(8);
d_mail VARCHAR2(40);
send NUMBER;
BEGIN
d_soggetto := ad4_utente.get_soggetto(p_utente);
d_mail := ad4_soggetto.get_indirizzo_web(d_soggetto);
send := Amvweb.send_msg('cms-system@ads.it', d_mail,'Notifica sistema CMS',p_msg,'dbmail');
NULL;
END notifica_revisione;
FUNCTION get_diritto (
  p_utente       VARCHAR2
, p_id_documento NUMBER
, p_revisione    NUMBER
, p_elenco       NUMBER DEFAULT 0
) RETURN VARCHAR2 IS
d_accesso    VARCHAR2(2);
d_stato      VARCHAR2(1);
d_area       NUMBER(10);
d_tipologia  NUMBER(10);
d_utente     VARCHAR2(8);
d_autore     VARCHAR2(8);
BEGIN
      -- Seleziona area, tipologia e stato del documento
   SELECT d.id_area
        , d.id_tipologia
      , d.stato
      , DECODE( stato
                , 'B', r.utente_redazione
                , 'N', r.utente_verifica
                , 'V', r.utente_approvazione
                , 'A', r.utente_approvazione
                 , ''
             )
        , DECODE( stato, 'B', autore, '') autore
     INTO d_area, d_tipologia, d_stato, d_utente, d_autore
     FROM AMV_DOCUMENTI d, AMV_DOCUMENTI_REVISIONI r
    WHERE d.id_documento = p_id_documento
     AND d.revisione = p_revisione
     AND r.id_documento (+) = d.id_documento
     AND r.revisione (+) = d.revisione
   ;
   -- Se utente e destinatario dello step successivo puo modificare il documento
   IF d_utente = p_utente THEN
        d_accesso := 'U';
   ELSE
      d_accesso := Amv_Area.get_diritto_utente(p_utente, d_area, d_tipologia);
     -- diritti su elenco revisioni in corso (si applica ai doc non in stato U o R)
     IF p_elenco = 1 THEN
         IF d_autore IS NOT NULL AND d_accesso IS NULL THEN
            d_accesso := 'C';
         END IF;
      ELSE
         IF d_accesso != 'U' THEN
            IF d_stato NOT IN('U','R') THEN
               d_accesso := NULL;
            END IF;
         END IF;
      NULL;
     END IF;
   END IF;
   RETURN d_accesso;
END get_diritto;
FUNCTION get_diritto_revisione (
  p_utente       VARCHAR2
, p_id_documento NUMBER
, p_revisione    NUMBER
) RETURN VARCHAR2 IS
d_accesso    VARCHAR2(2);
d_stato      VARCHAR2(1);
d_area       NUMBER(10);
d_tipologia  NUMBER(10);
d_utente     VARCHAR2(8);
BEGIN
      -- Seleziona area, tipologia e stato del documento
   SELECT d.id_area
        , d.id_tipologia
      , d.stato
      , DECODE( stato
                , 'U', r.utente_approvazione
                 , ''
             )
     INTO d_area, d_tipologia, d_stato, d_utente
     FROM AMV_DOCUMENTI d, AMV_DOCUMENTI_REVISIONI r
    WHERE d.id_documento = p_id_documento
     AND d.revisione = p_revisione
     AND r.id_documento (+) = d.id_documento
     AND r.revisione (+) = d.revisione
   ;
   IF d_utente = p_utente THEN
        d_accesso := 'U';
   ELSE
      IF d_stato = 'U' THEN
            d_accesso := Amv_Area.get_diritto_utente(p_utente, d_area, d_tipologia);
      END IF;
   END IF;
   RETURN d_accesso;
END get_diritto_revisione;
FUNCTION get_diritto_modifica (
  p_utente       VARCHAR2
, p_id_documento NUMBER
, p_revisione    NUMBER
) RETURN NUMBER IS
d_accesso    VARCHAR2(2);
d_stato      VARCHAR2(1);
d_area       NUMBER(10);
d_tipologia  NUMBER(10);
d_utente     VARCHAR2(8);
d_autore     VARCHAR2(8);
d_modifica   NUMBER := 0;
d_accessi_permessi VARCHAR2(10);
BEGIN
      -- Seleziona area, tipologia e stato del documento
   SELECT d.id_area
        , d.id_tipologia
      , d.stato
      , DECODE( stato
                , 'B', r.utente_redazione
                , 'N', r.utente_verifica
                , 'V', r.utente_approvazione
                , 'A', r.utente_approvazione
                 , ''
             )
      , DECODE( stato
                , 'B', 'V,A,U'
                , 'N', 'V,A,U'
                , 'V', 'A,U'
                , 'A', 'U'
            , 'U', 'U'
                 , ''
             ) accessi_permessi
        , DECODE( stato, 'B', autore, '') autore
     INTO d_area, d_tipologia, d_stato, d_utente, d_accessi_permessi, d_autore
     FROM AMV_DOCUMENTI d, AMV_DOCUMENTI_REVISIONI r
    WHERE d.id_documento = p_id_documento
     AND d.revisione = p_revisione
     AND r.id_documento (+) = d.id_documento
     AND r.revisione (+) = d.revisione
   ;
   -- Se utente e destinatario dello step successivo puo modificare il documento
   IF d_utente = p_utente THEN
        d_modifica := 1;
   ELSE
      d_accesso := Amv_Area.get_diritto_utente(p_utente, d_area, d_tipologia);
     IF INSTR(d_accessi_permessi, d_accesso) > 0 THEN
        d_modifica := 1;
     END IF;
   END IF;
   RETURN d_modifica;
END get_diritto_modifica;
FUNCTION get_flusso (
  p_utente       VARCHAR2
, p_id_documento NUMBER
, p_revisione    NUMBER
, p_stato        VARCHAR2
, p_tipo         VARCHAR2 DEFAULT NULL
, p_args         VARCHAR2 DEFAULT NULL
) RETURN VARCHAR2 IS
d_accesso    VARCHAR2(2);
d_flusso     VARCHAR2(1000);
d_tipo_testo VARCHAR2(10);
d_modifica   NUMBER := 0;
d_img        VARCHAR2(200) := '<img align="absMiddle" src="../common/images/AMV/arrow1.gif" border="0">';
d_img_b      VARCHAR2(200) := '<img align="absMiddle" src="../common/images/AMV/arrow2.gif" border="0">';
d_args       VARCHAR2(100);
d_link       VARCHAR2(200);
d_id_documento_padre  NUMBER(10);
d_modello    VARCHAR2(200);
d_richiesta  VARCHAR2(200);
BEGIN
   SELECT Amv_Area.get_diritto_utente(p_utente, id_area, id_tipologia), tipo_testo, LINK, id_documento_padre
     INTO d_accesso, d_tipo_testo, d_link, d_id_documento_padre
     FROM AMV_DOCUMENTI
    WHERE id_documento = p_id_documento
     AND revisione = p_revisione
   ;
   -- Seleziona diritto utente
   d_modifica := get_diritto_modifica(p_utente, p_id_documento ,p_revisione);
   IF d_modifica = 1 THEN
      -- Seleziona flusso sulla base dello stato
      IF p_args IS NOT NULL THEN
        d_args := '&'||p_args;
     END IF;
     IF p_stato = 'B' AND d_tipo_testo != 'Richiesta' THEN
        -- Inoltro non disponibile su documenti di Richiesta,
       -- poiche questa funzione di inoltro e riservata all'amministratore
         d_flusso := '<a href="../amvadm/AdmRevisioneRedazione.do?ID='||p_id_documento||'&REV='||p_revisione||d_args||'" title="Inoltro del documento">'||d_img||'&nbsp;Inoltra</a>';
      ELSIF p_stato = 'N' THEN
         d_flusso := '<a href="../amvadm/AdmRevisioneVerifica.do?ID='||p_id_documento||'&REV='||p_revisione||d_args||'" title="Verifica del documento">'||d_img||'&nbsp;Verifica</a>';
      ELSIF p_stato = 'V' THEN
        IF d_tipo_testo = 'Richiesta' THEN
          d_richiesta := '&'||d_link;
          d_modello := REPLACE(Amv_Documento.get_modello(d_id_documento_padre, p_revisione,'A'),'/','&');
         IF d_modello IS NOT NULL THEN
           d_modello := '&'||d_modello||'&rw=W';
         END IF;
         -- Al momento non richiamiamo ServletModulistica in approvazione richiesta
         d_flusso := '<a href="../restrict/ServletModulistica.do?ID='||p_id_documento||'&REV='||p_revisione||'&IDPD='||d_id_documento_padre||d_richiesta||d_modello||d_args||'" title="Approvazione del documento">'||d_img||'&nbsp;Approva</a>';
         --d_flusso := '<a href="../amvadm/AdmRevisioneApprova.do?ID='||p_id_documento||'&REV='||p_revisione||d_richiesta||d_modello||d_args||'" title="Approvazione del documento">'||d_img||'&nbsp;Approva</a>';
       ELSE
          d_flusso := '<a href="../amvadm/AdmRevisioneApprova.do?ID='||p_id_documento||'&REV='||p_revisione||d_args||'" title="Approvazione del documento">'||d_img||'&nbsp;Approva</a>';
       END IF;
      ELSIF p_stato = 'A' AND d_tipo_testo != 'Richiesta' THEN
         d_flusso := '<a href="../amvadm/AdmRevisionePubblica.do?ID='||p_id_documento||'&REV='||p_revisione||d_args||'" title="Pubblicazione del documento">'||d_img||'&nbsp;Pubblica</a>';
      ELSE
         d_flusso := '';
      END IF;
     IF p_tipo IS NOT NULL THEN
        IF p_stato = 'N' OR p_stato = 'V' THEN
          d_flusso := '<a href="../amvadm/AdmRevisioneRespingi.do?ID='||p_id_documento||'&REV='||p_revisione||d_args||'" title="Rifiuta o Respinge il documento in Redazione">'||d_img_b||'&nbsp;Respingi</a>&nbsp;'||d_flusso;
         END IF;
     END IF;
   END IF;
   -- Se utente amministratore possibilita di pubblicare direttamente
   IF d_accesso = 'U' AND p_stato IN ('B', 'N', 'V') AND d_tipo_testo != 'Richiesta' THEN
        d_flusso := d_flusso||'&nbsp;-&nbsp;<a href="../amvadm/AdmRevisionePubblica.do?ID='||p_id_documento||'&REV='||p_revisione||d_args||'" title="Pubblicazione del documento">Pubblica</a>';
   END IF;
   RETURN d_flusso;
END get_flusso;
FUNCTION get_img_stato (
  p_stato        VARCHAR2
 ,p_tipo_testo   VARCHAR2 DEFAULT NULL
) RETURN VARCHAR2 IS
d_img    VARCHAR2(200);
BEGIN
   IF p_stato ='B' THEN
      d_img := '<img align="AbsMiddle" src="../common/images/AMV/rosso.gif" border="0">&nbsp;';
   ELSIF p_stato = 'N' THEN
      d_img := '<img align="AbsMiddle" src="../common/images/AMV/rosso.gif" border="0">&nbsp;';
   ELSIF p_stato = 'V' THEN
      d_img := '<img align="AbsMiddle" src="../common/images/AMV/giallo.gif" border="0">&nbsp;';
   ELSIF p_stato = 'A' THEN
      IF p_tipo_testo = 'Richiesta' THEN
         d_img := '<img align="AbsMiddle" src="../common/images/AMV/verde.gif" border="0">&nbsp;';
      ELSE
        d_img := '<img align="AbsMiddle" src="../common/images/AMV/giallo.gif" border="0">&nbsp;';
     END IF;
   ELSIF p_stato = 'U' THEN
      d_img := '<img align="AbsMiddle" src="../common/images/AMV/verde.gif" border="0">&nbsp;';
   ELSIF p_stato = 'R' THEN
      d_img := '<img align="AbsMiddle" src="../common/images/AMV/verde.gif" border="0">&nbsp;';
   ELSIF p_stato = 'F' THEN
      d_img := '<img align="AbsMiddle" src="../common/images/AMV/rosso.gif" border="0">&nbsp;';
   END IF;
   RETURN d_img||get_des_stato(p_stato,p_tipo_testo);
END get_img_stato;
FUNCTION get_des_stato (
  p_stato        VARCHAR2
 ,p_tipo_testo   VARCHAR2 DEFAULT NULL
) RETURN VARCHAR2 IS
d_des    VARCHAR2(200) := NULL;
BEGIN
   IF p_stato ='B' THEN
      d_des := 'Redazione';
   ELSIF p_stato = 'N' THEN
      d_des := 'Inoltrato';
   ELSIF p_stato = 'V' THEN
      d_des := 'da Approvare';
   ELSIF p_stato = 'A' THEN
      d_des := 'Approvato';
   ELSIF p_stato = 'U' THEN
      d_des := 'in Uso';
   ELSIF p_stato = 'R' THEN
      d_des := 'in Revisione';
   ELSIF p_stato = 'F' THEN
      IF p_tipo_testo = 'Richiesta' THEN
         d_des := 'Rifiutata';
     ELSE
        d_des := 'Fuori Uso';
     END IF;
   END IF;
   RETURN d_des;
END get_des_stato;
FUNCTION get_stato (
  p_id_documento        NUMBER
, p_revisione         NUMBER
) RETURN VARCHAR2 IS
d_stato    VARCHAR2(1);
BEGIN
   SELECT stato
     INTO d_stato
     FROM AMV_DOCUMENTI
    WHERE id_documento = p_id_documento
     AND revisione = p_revisione
   ;
   RETURN d_stato;
EXCEPTION
   WHEN OTHERS THEN
   RETURN NULL;
END get_stato;
END Amv_Revisione;
/

