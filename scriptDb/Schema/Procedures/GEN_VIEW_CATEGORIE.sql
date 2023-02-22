CREATE OR REPLACE PROCEDURE GEN_VIEW_CATEGORIE(P_CATEGORIA CATEGORIE_MODELLO.CATEGORIA%TYPE) is
   SUBTYPE st_stringa IS VARCHAR2 (256);
   TYPE t_col IS RECORD (
      col_name    afc.t_object_name,
      type_data   st_stringa
   );
   TYPE tb_col IS TABLE OF t_col
      INDEX BY BINARY_INTEGER;
   d_col         tb_col;
   d_c_ub        INTEGER                            := 0;
   d_body        DBMS_SQL.varchar2s;
   d_body_ub     INTEGER                            := 0;
   d_categoria   categorie_modello.categoria%TYPE   := P_CATEGORIA;
   D_ESISTE      NUMBER (1)                         := 0;
   D_CURSOR      INTEGER;
   D_RET_VAL     INTEGER;
   CURSOR CAMPI
   IS
      SELECT DISTINCT UPPER(DM.DATO) DATO, D.TIPO TIPO
                 FROM CATEGORIE_MODELLO CM, DATI_MODELLO DM, DATI D
                WHERE CM.CATEGORIA = D_CATEGORIA
                  AND DM.AREA = CM.AREA
                  AND DM.CODICE_MODELLO = CM.CODICE_MODELLO
                  AND D.AREA = DM.AREA_DATO
                  AND D.DATO = DM.DATO
                  AND D.LUNGHEZZA <= 4000
                  AND DM.IN_USO = 'Y'
             ORDER BY 1;
BEGIN
   FOR C_CAMPI IN CAMPI
   LOOP
      D_COL (D_C_UB).COL_NAME := C_CAMPI.DATO;
      D_COL (D_C_UB).TYPE_DATA := C_CAMPI.TIPO;
      D_C_UB := D_C_UB + 1;
   END LOOP;
   D_BODY (D_BODY_UB) := 'create or replace force view '||D_CATEGORIA||'_view ';
   D_BODY_UB := D_BODY_UB + 1;
   D_BODY (D_BODY_UB) := '( id_documento, ';
   D_BODY_UB := D_BODY_UB + 1;
   FOR J IN D_COL.FIRST .. D_COL.LAST
   LOOP
      D_BODY (D_BODY_UB) := D_COL (J).COL_NAME || ', ';
      D_BODY_UB := D_BODY_UB + 1;
   END LOOP;
   D_BODY (D_BODY.LAST) := RTRIM (D_BODY (D_BODY.LAST), ', ');
   D_BODY (D_BODY_UB) := ' ,FULL_TEXT) as';
   /* ESTRAGGO LE TABELLE DELLA STESSA CATEGORIA */
   FOR C_TAB IN (SELECT UPPER(A.ACRONIMO||'_'||ALIAS_MODELLO) NOME_TABELLA
                   FROM CATEGORIE_MODELLO CM
                      , MODELLI M
                      , AREE A
                      , TIPI_DOCUMENTO TD
                      , USER_OBJECTS UO
                  WHERE CM.CATEGORIA = D_CATEGORIA
                    AND M.AREA = CM.AREA
                    AND M.AREA = A.AREA
                    AND M.CODICE_MODELLO = CM.CODICE_MODELLO
                    AND M.CODICE_MODELLO_PADRE IS NULL
                    AND M.ID_TIPODOC = TD.ID_TIPODOC
                    AND UO.OBJECT_NAME = UPPER(A.ACRONIMO||'_'||ALIAS_MODELLO))
   LOOP
      D_BODY_UB := D_BODY_UB + 1;
      D_BODY (D_BODY_UB) := 'select id_documento, ';
--        D_BODY_UB := D_BODY_UB + 1;
        /* LOOP SULLE COLONNE */
      FOR J IN D_COL.FIRST .. D_COL.LAST
      LOOP
         D_BODY_UB := D_BODY_UB + 1;
         SELECT NVL (MAX (1), 0)
           INTO D_ESISTE
           FROM USER_TAB_COLUMNS
          WHERE TABLE_NAME = C_TAB.NOME_TABELLA
            AND COLUMN_NAME = UPPER(D_COL (J).COL_NAME);
         IF D_ESISTE = 1
         THEN
            D_BODY (D_BODY_UB) := D_COL (J).COL_NAME || ', ';
         ELSE
            IF D_COL (J).TYPE_DATA = 'S'
            THEN
               D_BODY (D_BODY_UB) :=
                               'to_char(null) ' || D_COL (J).COL_NAME || ', ';
            ELSIF D_COL (J).TYPE_DATA = 'N'
            THEN
               D_BODY (D_BODY_UB) :=
                             'to_number(null) ' || D_COL (J).COL_NAME || ', ';
            ELSIF D_COL (J).TYPE_DATA = 'D'
            THEN
               D_BODY (D_BODY_UB) :=
                               'to_date(null) ' || D_COL (J).COL_NAME || ', ';
            END IF;
         END IF;
      END LOOP;
      D_BODY_UB := D_BODY_UB + 1;
      D_BODY (D_BODY_UB) := 'FULL_TEXT ,';
      D_BODY (D_BODY.LAST) := RTRIM (D_BODY (D_BODY.LAST), ', ');
      D_BODY_UB := D_BODY_UB + 1;
      D_BODY (D_BODY_UB) := 'from ' || C_TAB.NOME_TABELLA;
      D_BODY_UB := D_BODY_UB + 1;
      D_BODY (D_BODY_UB) := 'union all';
   END LOOP;
   IF D_BODY (D_BODY_UB) = 'union all'
   THEN
      D_BODY.DELETE (D_BODY_UB);
   END IF;
   D_CURSOR := DBMS_SQL.OPEN_CURSOR;
   DBMS_SQL.PARSE(D_CURSOR, D_BODY, D_BODY.FIRST, D_BODY.LAST, TRUE, DBMS_SQL.NATIVE);
   D_RET_VAL := DBMS_SQL.EXECUTE(D_CURSOR);
   DBMS_SQL.CLOSE_CURSOR(D_CURSOR);
   D_BODY.DELETE;
   D_BODY_UB := 0;
END;
/

