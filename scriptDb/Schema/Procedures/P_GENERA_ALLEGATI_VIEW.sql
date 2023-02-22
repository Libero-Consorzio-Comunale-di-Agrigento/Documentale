CREATE OR REPLACE procedure P_GENERA_ALLEGATI_VIEW
AS
D_BODY          DBMS_SQL.varchar2a;
D_BODY_UB     INTEGER                            := 0;
A_QRY_STANDARD VARCHAR2(32767);
d_ret_val     INTEGER;
d_cursor      INTEGER;
--varchar2a
BEGIN
     A_QRY_STANDARD :=                               ' create or replace view ALLEGATI_DOCUMENTI_VIEW as SELECT d.id_documento_padre id_documento, ' ;
     A_QRY_STANDARD:= A_QRY_STANDARD||' d.id_documento id_documento_allegato ';

     A_QRY_STANDARD:= A_QRY_STANDARD||' FROM documenti d, oggetti_file ogfi, formati_file fofi, modelli m ';
     A_QRY_STANDARD:= A_QRY_STANDARD||' WHERE     ogfi.id_formato = fofi.id_formato ';
     A_QRY_STANDARD:= A_QRY_STANDARD||' AND fofi.visibile = ''S'' ';
     A_QRY_STANDARD:= A_QRY_STANDARD||' AND d.id_documento = ogfi.id_documento ';
     A_QRY_STANDARD:= A_QRY_STANDARD||' AND d.stato_documento NOT IN (''CA'', ''RE'', ''PB'') ';
     A_QRY_STANDARD:= A_QRY_STANDARD||' AND d.id_tipodoc = m.id_tipodoc ';
     A_QRY_STANDARD:= A_QRY_STANDARD||' AND d.id_documento_padre IS NOT NULL ';
     A_QRY_STANDARD:= A_QRY_STANDARD||' AND m.view_allegati_padre = ''Y'' ';
     A_QRY_STANDARD:= A_QRY_STANDARD||' GROUP BY d.id_documento_padre, d.id_documento ';

     d_body (d_body_ub) :=   A_QRY_STANDARD    ;
     d_body_ub := d_body_ub + 1;


     -- Gestione qry custom da tabella
     FOR I  IN (SELECT AREA, CODICE_MODELLO,REGOLA_SQL FROM ALLEGATI_DOCUMENTI_REGOLE)
     LOOP
            BEGIN
                -- TESTO LA SINGOLA REGOLA
                 d_cursor := DBMS_SQL.open_cursor;
                  DBMS_SQL.parse (d_cursor,
                                   ' create or replace view   ALLEGATI_DOCUMENTI_VIEW_TMP AS '||I.REGOLA_SQL,
                                  DBMS_SQL.native
                                 );
                  d_ret_val := DBMS_SQL.EXECUTE (d_cursor);
                  DBMS_SQL.close_cursor (d_cursor);
            EXCEPTION WHEN OTHERS THEN
                 DBMS_OUTPUT.put_line ('Attenzione! Impossibile aggiungere la regola sql per area '||I.AREA||' e modello '||I.CODICE_MODELLO||' risulta malformata. Errore='||SQLERRM);
                CONTINUE;
            END;
            d_body (d_body_ub) := ' UNION ';
            d_body_ub := d_body_ub + 1;
            d_body (d_body_ub) :=  I.REGOLA_SQL    ;
            d_body_ub := d_body_ub + 1;
     END LOOP;
       DBMS_OUTPUT.put_line ('Genero vista');
      d_cursor := DBMS_SQL.open_cursor;
      DBMS_SQL.parse (d_cursor,
                      d_body,
                      d_body.FIRST,
                      d_body.LAST,
                      TRUE,
                      DBMS_SQL.native
                     );
      d_ret_val := DBMS_SQL.EXECUTE (d_cursor);
      DBMS_SQL.close_cursor (d_cursor);
      d_body.DELETE;
      d_body_ub := 0;
      BEGIN
        EXECUTE IMMEDIATE 'drop view  ALLEGATI_DOCUMENTI_VIEW_TMP ';
        EXCEPTION WHEN OTHERS THEN  NULL;
      END;
EXCEPTION WHEN OTHERS THEN
      d_body.DELETE;
      d_body_ub := 0;
      BEGIN
        EXECUTE IMMEDIATE 'drop view  ALLEGATI_DOCUMENTI_VIEW_TMP ';
        EXCEPTION WHEN OTHERS THEN  NULL;
      END;
      RAISE;
END;
/

