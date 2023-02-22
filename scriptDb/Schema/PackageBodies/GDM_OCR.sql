CREATE OR REPLACE PACKAGE BODY GDM_OCR AS
    FUNCTION CHECK_GEN_OCR_PARAMETRI RETURN NUMBER
    IS
      A_RET NUMBER(1);
    BEGIN
       SELECT DECODE(NVL(VALORE,'N'),'S',1,0)
         INTO A_RET
         FROM PARAMETRI
        WHERE CODICE='GENERAZIONE_OCR' AND TIPO_MODELLO='@STANDARD';
        RETURN A_RET;
    EXCEPTION WHEN NO_DATA_FOUND THEN
        RETURN 0;
              WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20999,'Errore in ricerca parametro GENERAZIONE_OCR su tabella Parametri. Errore: '||sqlerrm);
    END;
    FUNCTION CHECK_GEN_OCR_AREA(A_AREA VARCHAR2) RETURN NUMBER
    IS
      A_GENERAZIONE_OCR AREE.GENERAZIONE_OCR%TYPE;
      A_RET             NUMBER(1);
      A_RET_IMP         NUMBER(1);
    BEGIN
       A_RET_IMP := CHECK_GEN_OCR_PARAMETRI;
       IF A_RET_IMP = 1 THEN
           SELECT NVL(GENERAZIONE_OCR,'S')
             INTO A_GENERAZIONE_OCR
             FROM AREE
            WHERE AREA=A_AREA;
           IF A_GENERAZIONE_OCR='S' THEN
              A_RET := 1;
           ELSE
              A_RET := 0;
           END IF;
        ELSE
            A_RET := 0;
        END IF;
        RETURN A_RET;
    EXCEPTION WHEN NO_DATA_FOUND THEN
        RETURN 0;
              WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20999,'Errore in ricerca parametro GENERAZIONE_OCR su tabella AREE per Area='||A_AREA||'. Errore: '||sqlerrm);
    END;
    FUNCTION CHECK_GEN_OCR_MODELLO(A_AREA VARCHAR2, A_MODELLO VARCHAR2) RETURN NUMBER
    IS
      A_GENERAZIONE_OCR MODELLI.GENERAZIONE_OCR%TYPE;
      A_RET             NUMBER(1);
      A_RET_AREA        NUMBER(1);
    BEGIN
       A_RET_AREA := CHECK_GEN_OCR_AREA(A_AREA);
       IF A_RET_AREA = 1 THEN
           SELECT NVL(GENERAZIONE_OCR,'S')
             INTO A_GENERAZIONE_OCR
             FROM MODELLI
            WHERE AREA=A_AREA AND CODICE_MODELLO=A_MODELLO;
           IF A_GENERAZIONE_OCR='S' THEN
              A_RET := 1;
           ELSE
              A_RET := 0;
           END IF;
       ELSE
           A_RET := 0;
       END IF;
        RETURN A_RET;
    EXCEPTION WHEN NO_DATA_FOUND THEN
        RETURN 0;
              WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20999,'Errore in ricerca parametro GENERAZIONE_OCR su tabella MODELLI per Area='||A_AREA||
                                        ' e Modello='||A_MODELLO||'. Errore: '||sqlerrm);
    END;
    FUNCTION CHECK_GEN_OCR_OGFI(A_ID_OGGETTO_FILE NUMBER) RETURN NUMBER
    IS
        A_RET       NUMBER(1);
        A_AREA      MODELLI.AREA%TYPE;
        A_MODELLO   MODELLI.CODICE_MODELLO%TYPE;
    BEGIN
    NULL;
        BEGIN
            SELECT DECODE(INSTR(';'||NVL(UPPER(PARAMETRI.VALORE),'XXXXXXXXXXXXXXX')||';',';'||UPPER(F_EXTFILE(FILENAME))||';'),
                          0,0,1),  MODELLI.AREA, MODELLI.CODICE_MODELLO
              INTO A_RET,A_AREA,A_MODELLO
              FROM OGGETTI_FILE, PARAMETRI, DOCUMENTI, MODELLI
             WHERE ID_OGGETTO_FILE=A_ID_OGGETTO_FILE AND
                   CODICE='OCR_EXTFILE' AND TIPO_MODELLO='@STANDARD' AND
                   DOCUMENTI.ID_DOCUMENTO=OGGETTI_FILE.ID_DOCUMENTO AND
                   MODELLI.ID_TIPODOC=DOCUMENTI.ID_TIPODOC;
        EXCEPTION WHEN NO_DATA_FOUND THEN
            RETURN 0;
        END;
        IF A_RET=1 THEN
           A_RET := CHECK_GEN_OCR_MODELLO(A_AREA,A_MODELLO);
        END IF;
        RETURN A_RET;
    EXCEPTION WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20999,'Errore in test GENERAZIONE_OCR su oggetto file per id_oggetto_file='||
                                        A_ID_OGGETTO_FILE||'. Errore: '||sqlerrm);
    END;
    FUNCTION CHECK_GEN_OCR_OGFI(A_NOME_FILE VARCHAR2, A_ID_DOC NUMBER) RETURN NUMBER
    IS
        A_RET       NUMBER(1);
        A_AREA      MODELLI.AREA%TYPE;
        A_MODELLO   MODELLI.CODICE_MODELLO%TYPE;
    BEGIN
    NULL;
        BEGIN
            SELECT DECODE(INSTR(';'||NVL(UPPER(PARAMETRI.VALORE),'XXXXXXXXXXXXXXX')||';',';'||UPPER(F_EXTFILE(A_NOME_FILE))||';'),
                          0,0,1),  MODELLI.AREA, MODELLI.CODICE_MODELLO
              INTO A_RET,A_AREA,A_MODELLO
              FROM PARAMETRI, DOCUMENTI, MODELLI
             WHERE CODICE='OCR_EXTFILE' AND TIPO_MODELLO='@STANDARD' AND
                   DOCUMENTI.ID_DOCUMENTO=A_ID_DOC AND
                   MODELLI.ID_TIPODOC=DOCUMENTI.ID_TIPODOC;
        EXCEPTION WHEN NO_DATA_FOUND THEN
            RETURN 0;
        END;
        IF A_RET=1 THEN
           A_RET := CHECK_GEN_OCR_MODELLO(A_AREA,A_MODELLO);
        END IF;
        RETURN A_RET;
    EXCEPTION WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20999,'Errore in test GENERAZIONE_OCR su oggetto file per file='||
                                        A_NOME_FILE||'. Errore: '||sqlerrm);
    END;
 PROCEDURE P_GENERAZIONE_OCR_OGFI(P_ID_DOCUMENTO VARCHAR2 DEFAULT NULL, P_ID_OGGETTO_FILE VARCHAR2 DEFAULT NULL,
                                  P_FORMATO VARCHAR2 DEFAULT NULL, P_TIPO_ORD_DATA_AGG IN VARCHAR2 DEFAULT NULL,
                                  P_COMMIT IN VARCHAR2 DEFAULT 'N',P_LIMIT VARCHAR2 DEFAULT '',
                                  P_AREA VARCHAR2 DEFAULT NULL, P_CODICE_MODELLO VARCHAR2 DEFAULT NULL
                                  )
  AS
    BEGIN
      DECLARE
       QUERY_STR                VARCHAR2(32000);
       TABLE_STR                VARCHAR2(1000);
       WHERE_STR                VARCHAR2(1000);
       TYPE OggettiFileCurTyp   IS REF CURSOR;
       v_oggetti_file_cursor    OggettiFileCurTyp;
       oggetti_file_record      oggetti_file%ROWTYPE;
       NUM                      NUMBER;
       RET NUMBER;
     BEGIN
      QUERY_STR := 'select o.* from ';
      TABLE_STR := ' oggetti_file o';
      WHERE_STR := ' where ocr_pending is null';
      IF LENGTH(NVL(P_LIMIT,'')) > 0 THEN
        WHERE_STR := WHERE_STR || ' and rownum <= ' || P_LIMIT;
      END IF;
      IF LENGTH(NVL(P_ID_DOCUMENTO,'')) > 0 THEN
        WHERE_STR := WHERE_STR ||  ' and o.id_documento = ' || P_ID_DOCUMENTO;
      END IF;
      IF LENGTH(NVL(P_ID_OGGETTO_FILE,'')) > 0 THEN
       WHERE_STR := WHERE_STR || ' and o.id_oggetto_file = ' || P_ID_OGGETTO_FILE;
      END IF;
      IF LENGTH(NVL(P_FORMATO,'')) > 0 THEN
       WHERE_STR := WHERE_STR || ' and F_EXTFILE(o.filename) = ''' || P_FORMATO||'''';
      END IF;
      IF (LENGTH(NVL(P_AREA,'')) > 0) THEN
        TABLE_STR := TABLE_STR || ' , documenti d ';
        WHERE_STR := WHERE_STR || ' and o.id_documento = d.id_documento and d.area = ''' || P_AREA||'''';
      END IF;
      IF LENGTH(NVL(P_CODICE_MODELLO,'')) > 0  THEN
        TABLE_STR := TABLE_STR || ' , modelli m ';
        WHERE_STR := WHERE_STR || ' and d.id_tipodoc = m.id_tipodoc and d.area = m.area'
                     || ' and m.codice_modello = '''|| P_CODICE_MODELLO||'''';
      END IF;
      QUERY_STR := QUERY_STR || TABLE_STR || WHERE_STR;
      IF NVL(P_TIPO_ORD_DATA_AGG,'') = 'ASC' THEN
       QUERY_STR := QUERY_STR || ' order by o.data_aggiornamento asc;';
      ELSE
        IF NVL(P_TIPO_ORD_DATA_AGG,'') = 'DESC' THEN
         QUERY_STR := QUERY_STR || ' order by o.data_aggiornamento desc;';
        END IF;
      END IF;
     dbms_output.put_line(QUERY_STR);
     OPEN v_oggetti_file_cursor FOR QUERY_STR;
        NUM:= 0;
        LOOP
         BEGIN
           FETCH v_oggetti_file_cursor INTO oggetti_file_record;
           EXIT WHEN v_oggetti_file_cursor%NOTFOUND;
           BEGIN
             RET:=CHECK_GEN_OCR_OGFI(oggetti_file_record.id_oggetto_file);
             dbms_output.put_line('id '||oggetti_file_record.id_oggetto_file);
             dbms_output.put_line('ret '||ret);
           EXCEPTION WHEN OTHERS THEN
             RAISE_APPLICATION_ERROR(-20999,'Errore in check generazione ocr per id_oggetto_file='||
                                             oggetti_file_record.id_oggetto_file||'.Errore: '||sqlerrm
                                             ||' SQL: '||QUERY_STR);
           END;
           IF RET=1 THEN
              BEGIN
                 UPDATE OGGETTI_FILE SET OCR_PENDING=1 WHERE ID_OGGETTO_FILE=oggetti_file_record.id_oggetto_file;
              EXCEPTION WHEN OTHERS THEN
                 RAISE_APPLICATION_ERROR(-20999,'Errore in update OCR_PENDING per id_oggetto_file='||
                                                 oggetti_file_record.id_oggetto_file||'.Errore: '||sqlerrm
                                                 ||' SQL: '||QUERY_STR);
              END;
           END IF;
           NUM:= NUM + 1;
           IF ((NVL(P_COMMIT,'N') = 'S') AND NUM = 1000) THEN
             COMMIT;
             NUM:= 0;
           END IF;
         END;
        END LOOP;
        CLOSE v_oggetti_file_cursor;
        IF (NVL(P_COMMIT,'N') = 'S') THEN
           COMMIT;
        END IF;
     END;
  EXCEPTION WHEN OTHERS THEN
     RAISE;
     ROLLBACK;
  END;
  PROCEDURE P_SET_AREE_GENERAZIONE_OCR(P_COMMIT IN VARCHAR2 DEFAULT 'N')
   AS
    BEGIN
     BEGIN
      UPDATE AREE SET GENERAZIONE_OCR = 'S';
      EXCEPTION WHEN OTHERS THEN
       RAISE_APPLICATION_ERROR(-20999,'Errore in update GENERAZIONE_OCR per tutte le AREE'||'.Errore: '||sqlerrm);
     END;
     IF NVL(P_COMMIT,'N') = 'S' THEN
       COMMIT;
     END IF;
  EXCEPTION WHEN OTHERS THEN
     RAISE;
     ROLLBACK;
  END;
  PROCEDURE P_SET_MODELLI_GENERAZIONE_OCR(P_AREA VARCHAR2,P_COMMIT IN VARCHAR2 DEFAULT 'N')
   AS
    A_AREA  AREE.AREA%type;
    BEGIN
      A_AREA := NVL(P_AREA,'*');
      BEGIN
      UPDATE MODELLI SET GENERAZIONE_OCR = 'S' WHERE AREA = DECODE(A_AREA,'*',AREA,A_AREA);
      EXCEPTION WHEN OTHERS THEN
       RAISE_APPLICATION_ERROR(-20999,'Errore in update GENERAZIONE_OCR per tutte i MODELLI di AREA:'||P_AREA||'.Errore: '||sqlerrm);
      END;
       IF NVL(P_COMMIT,'N') = 'S' THEN
         COMMIT;
       END IF;
  EXCEPTION WHEN OTHERS THEN
     RAISE;
     ROLLBACK;
  END;
 END GDM_OCR;
/

