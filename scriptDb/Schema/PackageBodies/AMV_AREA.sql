CREATE OR REPLACE PACKAGE BODY Amv_Area AS
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
FUNCTION get_diritto_gruppo
( p_gruppo       VARCHAR2,
  p_id_area      NUMBER,
  p_id_tipologia NUMBER DEFAULT NULL
)
RETURN VARCHAR2 IS
/******************************************************************************
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    31/03/2004 AO      Prima emissione.
******************************************************************************/
d_diritto        NUMBER(10);
d_accesso        VARCHAR2(1);
d_accesso_gruppo VARCHAR2(1);
BEGIN
   BEGIN -- Verifica Diritto accesso sulla area
      SELECT DECODE(MAX(DECODE(accesso,'R',1,'C',2,'V',3,'A',4,'U',5,0)),1,'R',2,'C',3,'V',4,'A',5,'U','')
        INTO d_accesso
        FROM AMV_DIRITTI
      WHERE NVL(gruppo,' ') =
           (SELECT NVL(MAX(gruppo),' ')
            FROM AMV_DIRITTI
           WHERE NVL(gruppo,p_gruppo)  = p_gruppo
             AND id_area = p_id_area
            AND NVL(id_tipologia, NVL(p_id_tipologia,1)) = NVL(p_id_tipologia,1)
          )
        AND id_area = p_id_area
       AND NVL(id_tipologia,-1) =
          (SELECT NVL(MAX(id_tipologia),-1)
            FROM AMV_DIRITTI
           WHERE NVL(gruppo,p_gruppo)  = p_gruppo
             AND id_area = p_id_area
            AND NVL(id_tipologia, NVL(p_id_tipologia,1)) = NVL(p_id_tipologia,1)
         )
      ;
   EXCEPTION WHEN NO_DATA_FOUND THEN
     d_accesso := '';
   END;
   -- Ottiene il diritto di accesso di eventuale Gruppo di appartenenza del Gruppo
   d_accesso_gruppo := get_diritto_utente(p_gruppo, p_id_area, p_id_tipologia);
   -- Restituisce il maggiore dei due valori
   SELECT DECODE( GREATEST( DECODE(d_accesso       ,'R',1,'C',2,'V',3,'A',4,'U',5,0)
                          , DECODE(d_accesso_gruppo,'R',1,'C',2,'V',3,'A',4,'U',5,0)
                    )
            ,1,'R',2,'C',3,'V',4,'A',5,'U','')
    INTO d_accesso
    FROM dual;
   RETURN d_accesso;
END get_diritto_gruppo;
FUNCTION get_diritto_utente
( p_utente       VARCHAR2,
  p_id_area      NUMBER,
  p_id_tipologia NUMBER DEFAULT NULL
)
RETURN VARCHAR2 IS
/******************************************************************************
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    31/03/2004 AO     Prima emissione.
 1    16/11/2004 AO     Inserimento commenti per richiamo a pkg AMV_DOCUMENTO
******************************************************************************/
d_accesso         VARCHAR2(1);
d_accesso_return  VARCHAR2(1);
BEGIN
   BEGIN -- Scandisce gruppi di apparteneza dell'utente
      FOR c_gruppi IN (
        SELECT gruppo
           FROM AD4_UTENTI_GRUPPO
         WHERE utente  = p_utente)
      LOOP
         BEGIN
         d_accesso := get_diritto_gruppo(c_gruppi.gruppo, p_id_area, p_id_tipologia);
         -- diritto di update
         IF d_accesso = 'U' THEN
            d_accesso_return := d_accesso;
         -- diritto di approvazione
         ELSIF d_accesso = 'A' AND NVL(d_accesso_return,' ') != 'U' THEN
           d_accesso_return := d_accesso;
         -- diritto di verifica
         ELSIF d_accesso = 'V' AND NVL(d_accesso_return,' ') NOT IN ('A','U') THEN
            d_accesso_return := d_accesso;
         -- diritto di creazione (redazione)
         ELSIF d_accesso = 'C' AND NVL(d_accesso_return,' ') NOT IN ('A','V', 'U') THEN
            d_accesso_return := d_accesso;
         -- diritto di lettura
         ELSIF d_accesso = 'R' AND NVL(d_accesso_return,' ') NOT IN ('A','V', 'U','C') THEN
            d_accesso_return := d_accesso;
         END IF;
         END;
      END LOOP;
      RETURN d_accesso_return;
   END;
END get_diritto_utente;
FUNCTION get_albero (
  p_id_area      NUMBER
, p_pagina       VARCHAR2
) RETURN CLOB IS
BEGIN
RETURN Afc_Html.tree (p_id_area,
                                'AMV',
                                'AREE',
                                 p_pagina,
                                'AMV_VISTA_AREE',
                                'PADRE_AREA_ID',
                                'ID_AREA',
                                'nome',
                                'nome',
                                NULL,
                                'N',
                                'N',
                                'S',
                                'N',
                                NULL,
                                NULL,
                                'ST',
                                P_FOGLIA => 'cmsfolderclosed.gif'
                               );
END get_albero;
END Amv_Area;
/

