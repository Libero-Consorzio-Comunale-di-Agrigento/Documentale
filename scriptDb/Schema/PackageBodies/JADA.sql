CREATE OR REPLACE PACKAGE BODY JADA
AS
   FUNCTION VERIFICA_UTENTE (A_AREA IN VARCHAR2, A_UTENTE IN VARCHAR2)
      RETURN NUMBER
   IS
      RETVAL   NUMBER;
   BEGIN
      SELECT 1
        INTO RETVAL
        FROM AREE_GRUPPI AG, AD4_UTENTI_GRUPPO UG
       WHERE     UG.UTENTE = A_UTENTE
             AND UG.GRUPPO = AG.GRUPPO
             AND AG.AREA = A_AREA;
      RETURN RETVAL;
   EXCEPTION
      WHEN NO_DATA_FOUND
      THEN
         RETURN 1;
   END;
   PROCEDURE GEN_VIEW (P_CATEGORIA CATEGORIE_MODELLO.CATEGORIA%TYPE)
   IS
      SUBTYPE st_stringa IS VARCHAR2 (256);
      TYPE t_col IS RECORD (col_name afc.t_object_name, type_data st_stringa);
      TYPE tb_col IS TABLE OF t_col
                        INDEX BY BINARY_INTEGER;
      d_col         tb_col;
      d_c_ub        INTEGER := 0;
      d_body        DBMS_SQL.varchar2s;
      d_body_ub     INTEGER := 0;
      d_categoria   categorie_modello.categoria%TYPE := P_CATEGORIA;
      D_ESISTE      NUMBER (1) := 0;
      D_CURSOR      INTEGER;
      D_RET_VAL     INTEGER;
      CURSOR CAMPI
      IS
           SELECT DISTINCT UPPER (DM.DATO) DATO, D.TIPO TIPO
             FROM CATEGORIE_MODELLO CM, DATI_MODELLO DM, DATI D
            WHERE     CM.CATEGORIA = D_CATEGORIA
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
      D_BODY (D_BODY_UB) := 'create or replace force view ' || D_CATEGORIA;
      D_BODY_UB := D_BODY_UB + 1;
      D_BODY (D_BODY_UB) := '( id_documento, area_ricerca, ';
      D_BODY_UB := D_BODY_UB + 1;
      FOR J IN D_COL.FIRST .. D_COL.LAST
      LOOP
         D_BODY (D_BODY_UB) := D_COL (J).COL_NAME || ', ';
         D_BODY_UB := D_BODY_UB + 1;
      END LOOP;
      D_BODY (D_BODY.LAST) := RTRIM (D_BODY (D_BODY.LAST), ', ');
      D_BODY (D_BODY_UB) := ' ,FULL_TEXT) as';
      /* ESTRAGGO LE TABELLE DELLA STESSA CATEGORIA */
      FOR C_TAB
         IN (SELECT UPPER (A.ACRONIMO || '_' || ALIAS_MODELLO) NOME_TABELLA
               FROM CATEGORIE_MODELLO CM,
                    MODELLI M,
                    AREE A,
                    TIPI_DOCUMENTO TD,
                    USER_OBJECTS UO
              WHERE     CM.CATEGORIA = D_CATEGORIA
                    AND M.AREA = CM.AREA
                    AND M.AREA = A.AREA
                    AND M.CODICE_MODELLO = CM.CODICE_MODELLO
                    AND M.CODICE_MODELLO_PADRE IS NULL
                    AND M.ID_TIPODOC = TD.ID_TIPODOC
                    AND UO.OBJECT_NAME =
                           UPPER (A.ACRONIMO || '_' || ALIAS_MODELLO))
      LOOP
         D_BODY_UB := D_BODY_UB + 1;
         D_BODY (D_BODY_UB) := 'select T.id_documento, d.area, ';
         --        D_BODY_UB := D_BODY_UB + 1;
         /* LOOP SULLE COLONNE */
         FOR J IN D_COL.FIRST .. D_COL.LAST
         LOOP
            D_BODY_UB := D_BODY_UB + 1;
            SELECT NVL (MAX (1), 0)
              INTO D_ESISTE
              FROM USER_TAB_COLUMNS
             WHERE TABLE_NAME = C_TAB.NOME_TABELLA
                   AND COLUMN_NAME = UPPER (D_COL (J).COL_NAME);
            IF D_ESISTE = 1
            THEN
               D_BODY (D_BODY_UB) := 'T.' || D_COL (J).COL_NAME || ', ';
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
         D_BODY (D_BODY_UB) := 'FULL_TEXT ';
         D_BODY (D_BODY.LAST) := RTRIM (D_BODY (D_BODY.LAST), ', ');
         D_BODY_UB := D_BODY_UB + 1;
         D_BODY (D_BODY_UB) :=
               'from '
            || C_TAB.NOME_TABELLA
            || ' t, documenti d where d.id_documento = t.id_documento ';
         D_BODY_UB := D_BODY_UB + 1;
         D_BODY (D_BODY_UB) := 'union all';
      END LOOP;
      IF D_BODY (D_BODY_UB) = 'union all'
      THEN
         D_BODY.DELETE (D_BODY_UB);
      END IF;
      D_CURSOR := DBMS_SQL.OPEN_CURSOR;
      DBMS_SQL.PARSE (D_CURSOR,
                      D_BODY,
                      D_BODY.FIRST,
                      D_BODY.LAST,
                      TRUE,
                      DBMS_SQL.NATIVE);
      D_RET_VAL := DBMS_SQL.EXECUTE (D_CURSOR);
      DBMS_SQL.CLOSE_CURSOR (D_CURSOR);
      D_BODY.DELETE;
      D_BODY_UB := 0;
   END;
   PROCEDURE GEN_WRKSPS (P_AREA IN VARCHAR2, P_NOME IN VARCHAR2)
   IS
      A_AREA_Q              VARCHAR2 (32767);
      A_AREA                VARCHAR2 (32767);
      A_MODELLO             VARCHAR2 (32767);
      A_MODELLO_Q           VARCHAR2 (32767);
      A_FILTRO              VARCHAR2 (32767);
      A_NOME_CARTELLA       VARCHAR2 (32767);
      A_ID_CARTELLA_PADRE   NUMBER;
      A_ID_WRKSPS           NUMBER;
      A_ID_QUERY            NUMBER;
      A_UTENTE              VARCHAR2 (32767);
   BEGIN
      A_AREA_Q := P_AREA;
      A_NOME_CARTELLA := P_NOME;
      A_AREA := 'GDMSYS';
      A_MODELLO := 'WRKSPStandard';
      A_ID_WRKSPS := NULL;
      A_ID_CARTELLA_PADRE := NULL;
      A_ID_QUERY := NULL;
      A_ID_WRKSPS := NULL;
      A_FILTRO := NULL;
      SELECT gruppo
        INTO A_UTENTE
        FROM aree_gruppi
       WHERE area = P_AREA;
      A_ID_WRKSPS :=
         GDM_CARTELLE.CREA_CARTELLA (A_AREA,
                                     A_MODELLO,
                                     A_NOME_CARTELLA,
                                     A_ID_CARTELLA_PADRE,
                                     A_UTENTE,
                                     1);
      A_MODELLO := 'CartellaStandard';
      A_NOME_CARTELLA := 'Delibere';
      A_ID_CARTELLA_PADRE :=
         GDM_CARTELLE.CREA_CARTELLA (A_AREA,
                                     A_MODELLO,
                                     A_NOME_CARTELLA,
                                     A_ID_WRKSPS,
                                     A_UTENTE,
                                     1);
      A_MODELLO_Q := 'R_DELIBERA';
      A_FILTRO := 'RICERCAMODULISTICA_' || A_AREA_Q || '@' || A_MODELLO_Q;
      A_ID_QUERY :=
         GDM_QUERY.CREA_QUERY (A_AREA,
                               'QueryStandard',
                               'Delibere',
                               A_FILTRO,
                               A_UTENTE,
                               A_ID_CARTELLA_PADRE,
                               1);
      A_MODELLO_Q := 'R_CONS_DELE';
      A_FILTRO := 'RICERCAMODULISTICA_' || A_AREA_Q || '@' || A_MODELLO_Q;
      A_ID_QUERY :=
         GDM_QUERY.CREA_QUERY (A_AREA,
                               'QueryStandard',
                               'Delibere da conservare',
                               A_FILTRO,
                               A_UTENTE,
                               A_ID_CARTELLA_PADRE,
                               1);
      A_NOME_CARTELLA := 'Dertermine';
      A_ID_CARTELLA_PADRE :=
         GDM_CARTELLE.CREA_CARTELLA (A_AREA,
                                     A_MODELLO,
                                     A_NOME_CARTELLA,
                                     A_ID_WRKSPS,
                                     A_UTENTE,
                                     1);
      A_MODELLO_Q := 'R_DETERMINA';
      A_FILTRO := 'RICERCAMODULISTICA_' || A_AREA_Q || '@' || A_MODELLO_Q;
      A_ID_QUERY :=
         GDM_QUERY.CREA_QUERY (A_AREA,
                               'QueryStandard',
                               'Determine',
                               A_FILTRO,
                               A_UTENTE,
                               A_ID_CARTELLA_PADRE,
                               1);
      A_MODELLO_Q := 'R_CONS_DETE';
      A_FILTRO := 'RICERCAMODULISTICA_' || A_AREA_Q || '@' || A_MODELLO_Q;
      A_ID_QUERY :=
         GDM_QUERY.CREA_QUERY (A_AREA,
                               'QueryStandard',
                               'Determine da conservare',
                               A_FILTRO,
                               A_UTENTE,
                               A_ID_CARTELLA_PADRE,
                               1);
      A_NOME_CARTELLA := 'Protocolli';
      A_ID_CARTELLA_PADRE :=
         GDM_CARTELLE.CREA_CARTELLA (A_AREA,
                                     A_MODELLO,
                                     A_NOME_CARTELLA,
                                     A_ID_WRKSPS,
                                     A_UTENTE,
                                     1);
      A_MODELLO_Q := 'R_PROTOCOLLO';
      A_FILTRO := 'RICERCAMODULISTICA_' || A_AREA_Q || '@' || A_MODELLO_Q;
      A_ID_QUERY :=
         GDM_QUERY.CREA_QUERY (A_AREA,
                               'QueryStandard',
                               'Protocolli',
                               A_FILTRO,
                               A_UTENTE,
                               A_ID_CARTELLA_PADRE,
                               1);
      A_MODELLO_Q := 'R_CONS_PROTOCOLLO';
      A_FILTRO := 'RICERCAMODULISTICA_' || A_AREA_Q || '@' || A_MODELLO_Q;
      A_ID_QUERY :=
         GDM_QUERY.CREA_QUERY (A_AREA,
                               'QueryStandard',
                               'Protocolli da conservare',
                               A_FILTRO,
                               A_UTENTE,
                               A_ID_CARTELLA_PADRE,
                               1);
      A_NOME_CARTELLA := 'Documenti generici';
      A_ID_CARTELLA_PADRE :=
         GDM_CARTELLE.CREA_CARTELLA (A_AREA,
                                     A_MODELLO,
                                     A_NOME_CARTELLA,
                                     A_ID_WRKSPS,
                                     A_UTENTE,
                                     1);
      A_MODELLO_Q := 'R_GENERICI';
      A_FILTRO := 'RICERCAMODULISTICA_' || A_AREA_Q || '@' || A_MODELLO_Q;
      A_ID_QUERY :=
         GDM_QUERY.CREA_QUERY (A_AREA,
                               'QueryStandard',
                               'Documenti generici',
                               A_FILTRO,
                               A_UTENTE,
                               A_ID_CARTELLA_PADRE,
                               1);
      A_MODELLO_Q := 'R_CONS_GENE';
      A_FILTRO := 'RICERCAMODULISTICA_' || A_AREA_Q || '@' || A_MODELLO_Q;
      A_ID_QUERY :=
         GDM_QUERY.CREA_QUERY (A_AREA,
                               'QueryStandard',
                               'Documenti generici da conservare',
                               A_FILTRO,
                               A_UTENTE,
                               A_ID_CARTELLA_PADRE,
                               1);
      A_NOME_CARTELLA := 'Fatture passive';
      A_ID_CARTELLA_PADRE :=
         GDM_CARTELLE.CREA_CARTELLA (A_AREA,
                                     A_MODELLO,
                                     A_NOME_CARTELLA,
                                     A_ID_WRKSPS,
                                     A_UTENTE,
                                     1);
      A_MODELLO_Q := 'R_FATTURE';
      A_FILTRO := 'RICERCAMODULISTICA_' || A_AREA_Q || '@' || A_MODELLO_Q;
      A_ID_QUERY :=
         GDM_QUERY.CREA_QUERY (A_AREA,
                               'QueryStandard',
                               'Fatture',
                               A_FILTRO,
                               A_UTENTE,
                               A_ID_CARTELLA_PADRE,
                               1);
      A_MODELLO_Q := 'R_CONS_FATTURE';
      A_FILTRO := 'RICERCAMODULISTICA_' || A_AREA_Q || '@' || A_MODELLO_Q;
      A_ID_QUERY :=
         GDM_QUERY.CREA_QUERY (A_AREA,
                               'QueryStandard',
                               'Fatture da conservare',
                               A_FILTRO,
                               A_UTENTE,
                               A_ID_CARTELLA_PADRE,
                               1);
      A_NOME_CARTELLA := 'Certificati malattia';
      A_ID_CARTELLA_PADRE :=
         GDM_CARTELLE.CREA_CARTELLA (A_AREA,
                                     A_MODELLO,
                                     A_NOME_CARTELLA,
                                     A_ID_WRKSPS,
                                     A_UTENTE,
                                     1);
      A_MODELLO_Q := 'R_CERT_MALATTIA';
      A_FILTRO := 'RICERCAMODULISTICA_' || A_AREA_Q || '@' || A_MODELLO_Q;
      A_ID_QUERY :=
         GDM_QUERY.CREA_QUERY (A_AREA,
                               'QueryStandard',
                               'Certificati di malattia',
                               A_FILTRO,
                               A_UTENTE,
                               A_ID_CARTELLA_PADRE,
                               1);
      A_MODELLO_Q := 'R_CONS_CERT_MALATTIA';
      A_FILTRO := 'RICERCAMODULISTICA_' || A_AREA_Q || '@' || A_MODELLO_Q;
      A_ID_QUERY :=
         GDM_QUERY.CREA_QUERY (A_AREA,
                               'QueryStandard',
                               'Certificati di malattia da conservare',
                               A_FILTRO,
                               A_UTENTE,
                               A_ID_CARTELLA_PADRE,
                               1);
      A_MODELLO_Q := 'R_MAIL_ATTESTATI';
      A_FILTRO := 'RICERCAMODULISTICA_' || A_AREA_Q || '@' || A_MODELLO_Q;
      A_ID_QUERY :=
         GDM_QUERY.CREA_QUERY (A_AREA,
                               'QueryStandard',
                               'Mail attestati di malattia',
                               A_FILTRO,
                               A_UTENTE,
                               A_ID_CARTELLA_PADRE,
                               1);
      A_NOME_CARTELLA := 'Ordinativi informatici';
      A_ID_CARTELLA_PADRE :=
         GDM_CARTELLE.CREA_CARTELLA (A_AREA,
                                     A_MODELLO,
                                     A_NOME_CARTELLA,
                                     A_ID_WRKSPS,
                                     A_UTENTE,
                                     1);
      A_MODELLO_Q := 'R_ORDINATIVI';
      A_FILTRO := 'RICERCAMODULISTICA_' || A_AREA_Q || '@' || A_MODELLO_Q;
      A_ID_QUERY :=
         GDM_QUERY.CREA_QUERY (A_AREA,
                               'QueryStandard',
                               'Ordinativi',
                               A_FILTRO,
                               A_UTENTE,
                               A_ID_CARTELLA_PADRE,
                               1);
      A_MODELLO_Q := 'R_CONS_ORDINATIVI';
      A_FILTRO := 'RICERCAMODULISTICA_' || A_AREA_Q || '@' || A_MODELLO_Q;
      A_ID_QUERY :=
         GDM_QUERY.CREA_QUERY (A_AREA,
                               'QueryStandard',
                               'Ordinativi da conservare',
                               A_FILTRO,
                               A_UTENTE,
                               A_ID_CARTELLA_PADRE,
                               1);
      A_MODELLO_Q := 'R_MAIL_FLUSSI';
      A_FILTRO := 'RICERCAMODULISTICA_' || A_AREA_Q || '@' || A_MODELLO_Q;
      A_ID_QUERY :=
         GDM_QUERY.CREA_QUERY (A_AREA,
                               'QueryStandard',
                               'Mail flussi ordinativi',
                               A_FILTRO,
                               A_UTENTE,
                               A_ID_CARTELLA_PADRE,
                               1);
      A_NOME_CARTELLA := 'Conservazioni';
      A_ID_CARTELLA_PADRE :=
         GDM_CARTELLE.CREA_CARTELLA (A_AREA,
                                     A_MODELLO,
                                     A_NOME_CARTELLA,
                                     A_ID_WRKSPS,
                                     A_UTENTE,
                                     1);
      A_MODELLO_Q := 'R_LOG_CONSERVAZIONI';
      A_FILTRO := 'RICERCAMODULISTICA_' || A_AREA_Q || '@' || A_MODELLO_Q;
      A_ID_QUERY :=
         GDM_QUERY.CREA_QUERY (A_AREA,
                               'QueryStandard',
                               'Log conservazioni',
                               A_FILTRO,
                               A_UTENTE,
                               A_ID_CARTELLA_PADRE,
                               1);
      A_MODELLO_Q := 'R_REPORT';
      A_FILTRO := 'RICERCAMODULISTICA_' || A_AREA_Q || '@' || A_MODELLO_Q;
      A_ID_QUERY :=
         GDM_QUERY.CREA_QUERY (A_AREA,
                               'QueryStandard',
                               'Report conservazioni',
                               A_FILTRO,
                               A_UTENTE,
                               A_ID_CARTELLA_PADRE,
                               1);
      A_MODELLO_Q := 'R_CONSERVABILI';
      A_FILTRO := 'RICERCAMODULISTICA_' || A_AREA_Q || '@' || A_MODELLO_Q;
      A_ID_QUERY :=
         GDM_QUERY.CREA_QUERY (A_AREA,
                               'QueryStandard',
                               'Conservabili',
                               A_FILTRO,
                               A_UTENTE,
                               A_ID_WRKSPS,
                               1);
      A_MODELLO_Q := 'R_MARCATI';
      A_FILTRO := 'RICERCAMODULISTICA_' || A_AREA_Q || '@' || A_MODELLO_Q;
      A_ID_QUERY :=
         GDM_QUERY.CREA_QUERY (A_AREA,
                               'QueryStandard',
                               'Marcati',
                               A_FILTRO,
                               A_UTENTE,
                               A_ID_WRKSPS,
                               1);
      A_MODELLO_Q := 'R_INCOMPLETI';
      A_FILTRO := 'RICERCAMODULISTICA_' || A_AREA_Q || '@' || A_MODELLO_Q;
      A_ID_QUERY :=
         GDM_QUERY.CREA_QUERY (A_AREA,
                               'QueryStandard',
                               'Incompleti',
                               A_FILTRO,
                               A_UTENTE,
                               A_ID_WRKSPS,
                               1);
      COMMIT;
   END;
   PROCEDURE GEN_ALL_VIEW (P_AREA IN VARCHAR2)
   IS
      CURSOR C1
      IS
         SELECT DISTINCT CATEGORIA
           FROM CATEGORIE_MODELLO
          WHERE AREA = P_AREA;
   BEGIN
      FOR C_1 IN C1
      LOOP
         JADA.GEN_VIEW (C_1.CATEGORIA);
      END LOOP;
   END;
END JADA;
/

