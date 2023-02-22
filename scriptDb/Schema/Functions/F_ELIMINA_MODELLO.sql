CREATE OR REPLACE FUNCTION f_elimina_modello (
   a_area          VARCHAR2,
   a_modello       VARCHAR2,
   a_doc_cascade   VARCHAR2 DEFAULT 'N'
)
   RETURN NUMBER
IS
/*****************************************************************************************
   NAME:       F_ELIMINA_MODELLO
   PURPOSE:    SCOPO DELLA FUNZIONE E QUELLO DI ELIMINARE
               I MODELLI RINTRACCIABILI MEDIANTE:
                     AREA
                     AREA e CODICE MODELLO
    A_DOC_CASCADE
         Se N non elimina i modelli se presenti documenti del tipo indicato
      Se S prima elimina i documenti dopo i modelli
   RETURN:     0 NESSUNA ELIMINAZIONE AREA/MODELLO ERRATO
               1 ELIMINA IL MODELLO
               2 NESSUNA ELIMINAZIONE PER PRESENZA DI DOCUMENTI:
                (ELIMINARE PRIMA I DOCUMENTI)
   REVISIONS:
   VER   VER DMSERVER COMPATIBILITY DATE        AUTHOR           DESCRIPTION
   ----  -------------------------  ----------  ---------------  -------------------------
   1.0   TUTTE LE VERSIONI          06/11/2005  BONFORTE M.      1. CREATED THIS FUNCTION.
*****************************************************************************************/
   tmpvar        NUMBER (1)    := 0;
   ok_op         VARCHAR2 (1)  := 'N';
   retval        NUMBER (1)    := 0;
   nometabella   VARCHAR2 (30) := '';
   esistetab     NUMBER (1)    := 0;
   CURSOR c_0
   IS
      SELECT codice_modello,tipo_uso
        FROM modelli
       WHERE area = a_area
         AND codice_modello_padre = a_modello
         AND a_modello IS NOT NULL;
   CURSOR c_1
   IS
      SELECT codice_modello, id_tipodoc,tipo_uso
        FROM modelli
       WHERE area = a_area AND a_modello IS NULL
      UNION ALL
      SELECT codice_modello, id_tipodoc,tipo_uso
        FROM modelli
       WHERE area = a_area AND codice_modello = a_modello;
BEGIN
   BEGIN
      --ELIMINO  RICORSIVAMENTE I MODELLI FIGLI
      FOR c0 IN c_0
      LOOP
         retval :=
                 f_elimina_modello (a_area, c0.codice_modello, a_doc_cascade);
         IF retval = 2
         THEN
            RETURN retval;
         END IF;
      END LOOP;
      BEGIN
         SELECT 'N'
           INTO ok_op
           FROM DUAL
          WHERE EXISTS (
                   SELECT 1
                     FROM documenti d, modelli m
                    WHERE m.area = a_area
                      AND m.codice_modello = a_modello
                      AND m.id_tipodoc = d.id_tipodoc)
            AND a_doc_cascade = 'N';
         RETURN 2;
      EXCEPTION
         WHEN NO_DATA_FOUND
         THEN
            ok_op := 'S';
      END;
   EXCEPTION
      WHEN OTHERS
      THEN
         raise_application_error ('-20997', SQLERRM);
   END;
   IF ok_op = 'S'
   THEN
      FOR c1 IN c_1
      LOOP
         DECLARE
            aliasview   tipi_documento.alias_view%TYPE;
         BEGIN
            IF c1.id_tipodoc IS NOT NULL
            THEN
               if c1.tipo_uso in ('C','F','W') then
                  retval := f_elimina_cartelle ( a_area ,a_modello,'','S');
               else
                if c1.tipo_uso in ('Q','V') then
                  retval := f_elimina_queries (a_area ,a_modello);
                else
                  retval := f_elimina_documenti (a_area ,a_modello);
                end if;
               end if;
            END IF;
            /* DROP DELLE ETICHETTE */
            DELETE FROM etichette
                  WHERE area = a_area AND codice_modello = c1.codice_modello;
            /* DROP DEI DOMINI */
            DELETE FROM domini
                  WHERE area = a_area AND codice_modello = c1.codice_modello;
            /* DROP DEGLI REVISIONI_MODELLO */
            DELETE FROM revisioni_modello
                  WHERE area = a_area AND codice_modello = c1.codice_modello;
            /* DROP DEI TIPI_PRATICHE */
            DELETE FROM tipi_pratiche
                  WHERE mod_area = a_area
                    AND codice_modello = c1.codice_modello;
            /* DROP DEI TIPI_PRATICHE  */
            DELETE FROM modelli_tipi_pratiche
                  WHERE area = a_area AND codice_modello = c1.codice_modello;
            /* DROP DEI DATI_MODELLO  */
            DELETE FROM dati_modello
                  WHERE area = a_area AND codice_modello = c1.codice_modello;
            /* DROP DEGLI SI4_COMPETENZE  PER IL MODELLO */
            DELETE FROM si4_competenze
                  WHERE oggetto = c1.codice_modello
                    AND id_abilitazione IN (
                           SELECT id_abilitazione
                             FROM si4_abilitazioni a, si4_tipi_oggetto o
                            WHERE a.id_tipo_oggetto = o.id_tipo_oggetto
                              AND tipo_oggetto = 'MODELLI');
            IF c1.id_tipodoc IS NOT NULL then
                SELECT NVL (alias_view, ''),
                       f_nome_tabella (m.area, m.codice_modello)
                  INTO aliasview,
                       nometabella
                  FROM modelli m, tipi_documento td
                 WHERE area = a_area
                   AND codice_modello = c1.codice_modello
                   AND m.id_tipodoc = td.id_tipodoc;
            else
                aliasview := '';
                nometabella := '';
            end if;
            /* DROP DEI MODELLI*/
            DELETE FROM modelli
                  WHERE area = a_area AND codice_modello = c1.codice_modello;
            IF c1.id_tipodoc IS NOT NULL
            THEN
               BEGIN
                  /* DROP DELLE NOTIFICHE */
                  DELETE FROM notifiche
                        WHERE id_tipodoc = c1.id_tipodoc;
                  /* DROP DELLE SI4_COMPETENZE  PER IL TIPI_DOCUMENTO */
                  DELETE FROM si4_competenze
                        WHERE oggetto = TO_CHAR (c1.id_tipodoc)
                          AND id_abilitazione IN (
                                 SELECT id_abilitazione
                                   FROM si4_abilitazioni a,
                                        si4_tipi_oggetto o
                                  WHERE a.id_tipo_oggetto = o.id_tipo_oggetto
                                    AND tipo_oggetto = 'TIPI_DOCUMENTO');
                  /* DROP DEI CAMPI_DOCUMENTO */
                  DELETE      campi_documento
                        WHERE id_tipodoc = c1.id_tipodoc;
                  /* DROP DEI TIPI_DOCUMENTO */
                  DELETE FROM tipi_documento
                        WHERE id_tipodoc = c1.id_tipodoc;
               END;
            END IF;
            IF aliasview <> ''
            THEN
               si4.sql_execute ('DROP VIEW GDM_' || aliasview);
            END IF;
            SELECT f_esiste_tabella (nometabella)
              INTO esistetab
              FROM DUAL;
            IF esistetab = 1
            THEN
               si4.sql_execute ('DROP TABLE ' || nometabella);
            END IF;
            tmpvar := 1;
         EXCEPTION
           WHEN OTHERS
           THEN
               raise_application_error ('-20999', SQLERRM);
         END;
      END LOOP;
   END IF;
   RETURN tmpvar;
END f_elimina_modello;
/

