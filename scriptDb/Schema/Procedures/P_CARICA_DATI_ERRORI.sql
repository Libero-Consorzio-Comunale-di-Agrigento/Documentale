CREATE OR REPLACE PROCEDURE P_CARICA_DATI_ERRORI (
   P_AREA   IN   VARCHAR2,
   P_CM     IN   VARCHAR2
)
IS
   ALIAS_V            VARCHAR2 (30);
   D_TESTO            VARCHAR2 (32000);
   D_NEW_TESTO        VARCHAR2 (32000);
   D_CURSOR           INTEGER;
   D_ROWS_PROCESSED   INTEGER;
   PART1              VARCHAR2 (32000);
   PART2              VARCHAR2 (32000);
   CONTATORE          NUMBER;
   CONTATORE_2        NUMBER;
   C_CLOB             CLOB;
   F_TEXT             CLOB;
   VALORE             VARCHAR2 (4000);
   ERRMSG             VARCHAR2 (4000);
   CURSOR C_DOCUMENTI
   IS
      SELECT   IDDOC ID_DOCUMENTO, T.ID_TIPODOC TIPO_DOC, A.AREA AREA,
               T.NOME CM,
               UPPER (A.ACRONIMO || '_' || T.ALIAS_MODELLO) NOMETABELLA
          FROM CARICA_DATI_ERRORI C, TIPI_DOCUMENTO T, AREE A
         WHERE T.AREA_MODELLO = P_AREA
           AND T.NOME = P_CM
           AND C.ID_TIPODOC = T.ID_TIPODOC
           AND A.AREA = T.AREA_MODELLO
           AND A.ACRONIMO IS NOT NULL
           AND T.ALIAS_MODELLO IS NOT NULL
      ORDER BY 1 ASC, 2 ASC;
   CURSOR C_CAMPI (V_AREA VARCHAR2, V_CM VARCHAR2)
   IS
      SELECT DM.DATO DATO, D.TIPO TIPO, D.FORMATO_DATA FORMATO_DATA
        FROM DATI_MODELLO DM, DATI D
       WHERE DM.AREA = V_AREA
         AND DM.CODICE_MODELLO = V_CM
         AND D.AREA = DM.AREA_DATO
         AND D.DATO = DM.DATO
         AND NVL (DM.IN_USO, 'Y') = 'Y';
   CURSOR C_CAMPI_N (V_AREA VARCHAR2, V_CM VARCHAR2)
   IS
      SELECT DM.DATO DATO, D.TIPO TIPO
        FROM DATI_MODELLO DM, DATI D
       WHERE DM.AREA = V_AREA
         AND DM.CODICE_MODELLO = V_CM
         AND D.AREA = DM.AREA_DATO
         AND D.DATO = DM.DATO
         AND D.TIPO IN ('D', 'N')
         AND NVL (DM.IN_USO, 'Y') = 'Y';
   CURSOR C_CAMPI_CLOB (V_AREA VARCHAR2, V_CM VARCHAR2)
   IS
      SELECT DM.DATO DATO
        FROM DATI_MODELLO DM, DATI D
       WHERE DM.AREA = V_AREA
         AND DM.CODICE_MODELLO = V_CM
         AND D.AREA = DM.AREA_DATO
         AND D.DATO = DM.DATO
         AND D.TIPO = 'S'
         AND NVL (DM.IN_USO, 'Y') = 'Y';
BEGIN
   CONTATORE := 0;
   CONTATORE_2 := 0;
   FOR V_DOCUMENTI IN C_DOCUMENTI
   LOOP
      BEGIN
         F_TEXT := '';
         CONTATORE := CONTATORE + 1;
         PART1 :=
               'INSERT /*+ APPEND */ INTO '
            || V_DOCUMENTI.NOMETABELLA
            || ' (ID_DOCUMENTO ';
         PART2 := 'VALUES (' || V_DOCUMENTI.ID_DOCUMENTO;
         FOR V_CAMPI IN C_CAMPI (V_DOCUMENTI.AREA, V_DOCUMENTI.CM)
         LOOP
            PART1 := PART1 || ', ' || V_CAMPI.DATO;
            IF (V_CAMPI.TIPO = 'D')
            THEN
               PART2 :=
                     PART2
                  || ', TO_DATE(:'
                  || V_CAMPI.DATO
                  || ','''
                  || REPLACE (V_CAMPI.FORMATO_DATA, 'hh:', 'hh24:')
                  || ''')';
            ELSE
               PART2 := PART2 || ', :' || V_CAMPI.DATO;
            END IF;
         END LOOP;
         D_NEW_TESTO := PART1 || ', FULL_TEXT) ' || PART2 || ', :FULL_TEXT)';
         D_CURSOR := DBMS_SQL.OPEN_CURSOR;
         DBMS_SQL.PARSE (D_CURSOR, D_NEW_TESTO, DBMS_SQL.NATIVE);
         FOR V_CAMPI_N IN C_CAMPI_N (V_DOCUMENTI.AREA, V_DOCUMENTI.CM)
         LOOP
            PART1 := ', ' || V_CAMPI_N.DATO;
            VALORE :=
               F_VALORE_CAMPO_VER_TRASCO (V_DOCUMENTI.ID_DOCUMENTO,
                                          V_CAMPI_N.DATO
                                         );
            IF (NVL (VALORE, '-') <> '-')
            THEN
               IF (NVL (F_TEXT, '-') <> '-')
               THEN
                  F_TEXT := F_TEXT || CHR (13) || VALORE;
               ELSE
                  F_TEXT := VALORE;
               END IF;
            END IF;
            IF (V_CAMPI_N.TIPO = 'N')
            THEN
               DBMS_SQL.BIND_VARIABLE (D_CURSOR,
                                       ':' || V_CAMPI_N.DATO,
                                       TO_NUMBER (VALORE)
                                      );
            ELSE
               DBMS_SQL.BIND_VARIABLE (D_CURSOR,
                                       ':' || V_CAMPI_N.DATO,
                                       VALORE
                                      );
            END IF;
         END LOOP;
         FOR V_CAMPI_CLOB IN C_CAMPI_CLOB (V_DOCUMENTI.AREA, V_DOCUMENTI.CM)
         LOOP
            C_CLOB :=
               F_VALORE_CAMPO_CLOB (V_DOCUMENTI.ID_DOCUMENTO,
                                    V_CAMPI_CLOB.DATO
                                   );
            F_TEXT := F_TEXT || CHR (13) || C_CLOB;
            DBMS_SQL.BIND_VARIABLE (D_CURSOR, ':' || V_CAMPI_CLOB.DATO,
                                    C_CLOB);
         END LOOP;
         DBMS_SQL.BIND_VARIABLE (D_CURSOR, ':FULL_TEXT', F_TEXT);
         D_ROWS_PROCESSED := DBMS_SQL.EXECUTE (D_CURSOR);
         DBMS_SQL.CLOSE_CURSOR (D_CURSOR);
         DELETE      CARICA_DATI_ERRORI
               WHERE IDDOC = V_DOCUMENTI.ID_DOCUMENTO;
         IF (CONTATORE >= 1000)
         THEN
            CONTATORE_2 := CONTATORE_2 + 1;
            COMMIT;
            CONTATORE := 0;
         END IF;
      EXCEPTION
         WHEN NO_DATA_FOUND
         THEN
            RAISE;
         WHEN OTHERS
         THEN
            ERRMSG := SUBSTR (DBMS_UTILITY.FORMAT_ERROR_STACK, 1, 4000);
            UPDATE CARICA_DATI_ERRORI
               SET ERRORE = ERRMSG
             WHERE IDDOC = V_DOCUMENTI.ID_DOCUMENTO;
      END;
   END LOOP;
   COMMIT;
   SELECT COUNT (1)
     INTO CONTATORE
     FROM CARICA_DATI_ERRORI C, TIPI_DOCUMENTO T
    WHERE T.AREA_MODELLO = P_AREA
      AND T.NOME = P_CM
      AND C.ID_TIPODOC = T.ID_TIPODOC;
   IF (CONTATORE > 0)
   THEN
      RAISE_APPLICATION_ERROR (-20999,
                                  'Esistono ancora '
                               || TO_CHAR (CONTATORE)
                               || ' documenti con errore!'
                              );
   END IF;
EXCEPTION
   WHEN NO_DATA_FOUND
   THEN
      NULL;
   WHEN OTHERS
   THEN
      -- CONSIDER LOGGING THE ERROR AND THEN RE-RAISE
      RAISE;
END P_CARICA_DATI_ERRORI;
/

