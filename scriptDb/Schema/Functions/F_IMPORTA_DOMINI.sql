CREATE OR REPLACE FUNCTION F_Importa_Domini(
   AREA_IN       IN   VARCHAR2,
   AREA_OUT      IN   VARCHAR2,
   DOMINIO_OUT    IN   VARCHAR2,
   CODICE_MOD_OUT IN   VARCHAR2,
   PRECARICA_OUT   IN   VARCHAR2,
   TIPO_INS      IN   VARCHAR2,
   SOTTO_AREE    IN      VARCHAR2,
   UTENTE_AGG    IN   VARCHAR2)
RETURN NUMBER
IS
   CURSOR C_DOMINI_TUTTI (VAR_AREA_OUT VARCHAR2,VAR_DOMINIO_OUT VARCHAR2, VAR_CODICE_MODELLO_OUT VARCHAR2, VAR_PRECARICA_OUT VARCHAR2) IS
      SELECT DOMINIO,PRECARICA,SEQ_DOMINIO_AREA,DESCRIZIONE,TIPO,DRIVER,CONNESSIONE,UTENTE,PASSWD,DSN,ORDINAMENTO,CODICE_MODELLO
      FROM DOMINI
        WHERE AREA = VAR_AREA_OUT AND PRECARICA NOT IN('E','C')
          AND (UPPER(DOMINIO) LIKE '%'||UPPER(VAR_DOMINIO_OUT)||'%' OR VAR_DOMINIO_OUT IS NULL)
          AND CODICE_MODELLO = VAR_CODICE_MODELLO_OUT
          AND (PRECARICA LIKE '%'||VAR_PRECARICA_OUT||'%' OR VAR_PRECARICA_OUT IS NULL);
   R_DT  C_DOMINI_TUTTI%ROWTYPE;
  CURSOR C_DOMINI_SEL (VAR_AREA_OUT VARCHAR2,VAR_DOMINIO_OUT VARCHAR2,VAR_CODICE_MODELLO_OUT VARCHAR2) IS
      SELECT DOMINIO,PRECARICA,SEQ_DOMINIO_AREA,DESCRIZIONE,TIPO,DRIVER,CONNESSIONE,UTENTE,PASSWD,DSN,ORDINAMENTO,CODICE_MODELLO
      FROM DOMINI
      WHERE AREA = VAR_AREA_OUT AND DOMINIO = VAR_DOMINIO_OUT
       AND CODICE_MODELLO = VAR_CODICE_MODELLO_OUT AND PRECARICA NOT IN('E','C');
  R_DS  C_DOMINI_SEL%ROWTYPE;
  CURSOR C_VALORI (VAR_AREA VARCHAR2,VAR_DOMINIO VARCHAR2, VAR_CODICE_MOD VARCHAR2) IS
      SELECT CODICE, VALORE
      FROM VALORI_DOMINIO
      WHERE AREA = VAR_AREA AND DOMINIO = VAR_DOMINIO AND CODICE_MODELLO = VAR_CODICE_MOD;
   R_V  C_VALORI%ROWTYPE;
  CURSOR C_AREE (VAR_AREA VARCHAR2,VAR_AREA_ST VARCHAR2) IS
   SELECT AREA
   FROM AREE
   WHERE AREA LIKE VAR_AREA
   UNION
   SELECT AREA
   FROM AREE
   WHERE AREA LIKE VAR_AREA_ST;
   R_AR  C_AREE%ROWTYPE;
   ID_TIPODOC     tipi_documento.id_tipodoc%TYPE;
   NUM_DOMINI     NUMBER;
   NUM_AREE       NUMBER;
   NESIST         NUMBER;
   VESIST         NUMBER;
   CINSTR         CLOB;
   RESULT         NUMBER(1) := 0;
BEGIN
   IF ((AREA_IN != AREA_OUT) OR (SOTTO_AREE = 'S')) THEN
    IF (SOTTO_AREE = 'S') THEN
      OPEN C_AREE(AREA_IN,AREA_IN||'.%');
     ELSE
      OPEN C_AREE(AREA_IN,AREA_IN);
     END IF;
     NUM_AREE:=0;
    LOOP
     BEGIN
      FETCH C_AREE INTO R_AR;
      EXIT WHEN C_AREE%NOTFOUND;
     END;
     NUM_AREE:=NUM_AREE + 1;
     IF (CODICE_MOD_OUT <> '-') THEN
       BEGIN
        SELECT ID_TIPODOC
        INTO ID_TIPODOC
        FROM MODELLI WHERE AREA = R_AR.AREA
        AND CODICE_MODELLO = CODICE_MOD_OUT;
        EXCEPTION WHEN NO_DATA_FOUND THEN
          raise_application_error (-20999,'MODELLO INESISTENTE PER AREA '||R_AR.AREA||' '||SQLERRM);
       END;
     END IF;
     IF (TIPO_INS = 'T') THEN
       NUM_DOMINI:=0;
       OPEN C_DOMINI_TUTTI(AREA_OUT,DOMINIO_OUT,CODICE_MOD_OUT,PRECARICA_OUT);
       LOOP
         FETCH C_DOMINI_TUTTI INTO R_DT;
         EXIT WHEN C_DOMINI_TUTTI%NOTFOUND;
         NUM_DOMINI:= NUM_DOMINI + 1;
        BEGIN
           SELECT COUNT(1)
           INTO NESIST
           FROM DOMINI
           WHERE AREA = R_AR.AREA AND DOMINIO = R_DT.DOMINIO
             AND CODICE_MODELLO = R_DT.CODICE_MODELLO;
           EXCEPTION WHEN OTHERS THEN
             raise_application_error (-20998,'RICERCA DOMINI DA IMPORTARE (AREA,CODICE_MODELLO,DOMINIO): ('||R_AR.AREA||','||R_DT.CODICE_MODELLO||','||R_DT.DOMINIO||') '||SQLERRM);
        END;
        IF (NESIST > 0) THEN
          BEGIN
           UPDATE DOMINI SET
             PRECARICA = R_DT.PRECARICA,
             SEQ_DOMINIO_AREA = R_DT.SEQ_DOMINIO_AREA,
             DESCRIZIONE = R_DT.DESCRIZIONE,
             TIPO = R_DT.TIPO,
             DRIVER = R_DT.DRIVER,
             CONNESSIONE = R_DT.CONNESSIONE,
             UTENTE = R_DT.UTENTE,
             PASSWD = R_DT.PASSWD,
             DSN = R_DT.DSN,
             ORDINAMENTO = R_DT.ORDINAMENTO,
             CODICE_MODELLO = R_DT.CODICE_MODELLO,
             DATA_AGGIORNAMENTO = SYSDATE,
             UTENTE_AGGIORNAMENTO = UTENTE_AGG
           WHERE AREA = R_AR.AREA AND DOMINIO = R_DT.DOMINIO AND CODICE_MODELLO = R_DT.CODICE_MODELLO;
            EXCEPTION WHEN OTHERS THEN
             raise_application_error (-20997,'IMPOSSIBILE AGGIORNARE IL DOMINIO (AREA,CODICE_MODELLO,DOMINIO):('||R_AR.AREA||','||R_DT.CODICE_MODELLO||','||R_DT.DOMINIO||') '||SQLERRM);
          END;
        ELSE
          BEGIN
           INSERT INTO DOMINI(AREA,DOMINIO,PRECARICA,SEQ_DOMINIO_AREA,DESCRIZIONE,TIPO,DRIVER,CONNESSIONE,UTENTE,PASSWD,DSN,
               ORDINAMENTO,CODICE_MODELLO,DATA_AGGIORNAMENTO,UTENTE_AGGIORNAMENTO)
           VALUES
               (R_AR.AREA,R_DT.DOMINIO,R_DT.PRECARICA,R_DT.SEQ_DOMINIO_AREA,R_DT.DESCRIZIONE,R_DT.TIPO,R_DT.DRIVER,R_DT.CONNESSIONE,
               R_DT.UTENTE,R_DT.PASSWD,R_DT.DSN,R_DT.ORDINAMENTO,R_DT.CODICE_MODELLO,SYSDATE,UTENTE_AGG);
           EXCEPTION WHEN OTHERS THEN
               raise_application_error (-20996,'IMPOSSIBILE INSERIRE IL DOMINIO (AREA,CODICE_MODELLO,DOMINIO):('||R_AR.AREA||','||R_DT.CODICE_MODELLO||','||R_DT.DOMINIO||') '||SQLERRM);
          END;
        END IF;
        SELECT ISTRUZIONE
        INTO CINSTR
        FROM DOMINI
        WHERE AREA = AREA_OUT AND DOMINIO = R_DT.DOMINIO AND CODICE_MODELLO = R_DT.CODICE_MODELLO;
        UPDATE DOMINI
        SET ISTRUZIONE = CINSTR
        WHERE AREA = R_AR.AREA AND DOMINIO = R_DT.DOMINIO AND CODICE_MODELLO = R_DT.CODICE_MODELLO;
        OPEN C_VALORI(AREA_OUT,R_DT.DOMINIO,R_DT.CODICE_MODELLO);
        LOOP
            FETCH C_VALORI INTO R_V;
            EXIT WHEN C_VALORI%NOTFOUND;
            BEGIN
             SELECT COUNT(1)
             INTO VESIST
             FROM VALORI_DOMINIO
             WHERE AREA = R_AR.AREA AND DOMINIO = R_DT.DOMINIO
                   AND CODICE_MODELLO = R_DT.CODICE_MODELLO AND CODICE = R_V.CODICE;
             EXCEPTION WHEN OTHERS THEN
                raise_application_error (-20995,'RICERCA VALORI DOMINIO DA IMPORTARE (AREA,CODICE_MODELLO,DOMINIO,CODICE): ('||R_AR.AREA||','||R_DT.CODICE_MODELLO||','||R_DT.DOMINIO||','||R_V.CODICE||') '||SQLERRM);
            END;
            IF (VESIST > 0) THEN
             BEGIN
                UPDATE VALORI_DOMINIO SET
                VALORE = R_V.VALORE
                WHERE AREA = R_AR.AREA AND DOMINIO = R_DT.DOMINIO
                      AND CODICE_MODELLO = R_DT.CODICE_MODELLO AND CODICE = R_V.CODICE;
               EXCEPTION WHEN OTHERS THEN
               raise_application_error (-20994,'IMPOSSIBILE AGGIORNARE IL VALORE DOMINIO (AREA,CODICE_MODELLO,DOMINIO,CODICE):('||R_AR.AREA||','||R_DT.CODICE_MODELLO||','||R_DT.DOMINIO||','||R_V.CODICE||') '||SQLERRM);
             END;
            ELSE
             BEGIN
               INSERT INTO VALORI_DOMINIO(AREA,DOMINIO,CODICE,VALORE) VALUES(R_AR.AREA,R_DT.DOMINIO,R_V.CODICE,R_V.VALORE);
              EXCEPTION WHEN OTHERS THEN
              raise_application_error (-20993,'IMPOSSIBILE INSERIRE IL VALORE DOMINIO (AREA,CODICE_MODELLO,DOMINIO,CODICE):('||R_AR.AREA||','||R_DT.CODICE_MODELLO||','||R_DT.DOMINIO||','||R_V.CODICE||') '||SQLERRM);
             END;
           END IF;
        END LOOP;
        CLOSE C_VALORI;
       END LOOP;
       CLOSE C_DOMINI_TUTTI;
     ELSE
      NUM_DOMINI:=0;
      OPEN C_DOMINI_SEL(AREA_OUT,DOMINIO_OUT,CODICE_MOD_OUT);
      LOOP
         FETCH C_DOMINI_SEL INTO R_DS;
         EXIT WHEN C_DOMINI_SEL%NOTFOUND;
         NUM_DOMINI:= NUM_DOMINI + 1;
         BEGIN
           SELECT COUNT(1)
           INTO NESIST
           FROM DOMINI
           WHERE AREA = R_AR.AREA AND DOMINIO = R_DS.DOMINIO
            AND CODICE_MODELLO = R_DS.CODICE_MODELLO;
           EXCEPTION WHEN OTHERS THEN
             raise_application_error (-20992,'RICERCA DOMINI DA IMPORTARE (AREA,CODICE_MODELLO,DOMINIO): ('||R_AR.AREA||','||R_DS.CODICE_MODELLO||','||R_DS.DOMINIO||') '||SQLERRM);
         END;
         IF (NESIST > 0) THEN
            BEGIN
             UPDATE DOMINI SET
              PRECARICA = R_DS.PRECARICA,
              SEQ_DOMINIO_AREA = R_DS.SEQ_DOMINIO_AREA,
              DESCRIZIONE = R_DS.DESCRIZIONE,
               TIPO = R_DS.TIPO,
              DRIVER = R_DS.DRIVER,
              CONNESSIONE = R_DS.CONNESSIONE,
              UTENTE = R_DS.UTENTE,
              PASSWD = R_DS.PASSWD,
              DSN = R_DS.DSN,
              ORDINAMENTO=R_DS.ORDINAMENTO,
              CODICE_MODELLO=R_DS.CODICE_MODELLO,
              DATA_AGGIORNAMENTO = SYSDATE,
              UTENTE_AGGIORNAMENTO = UTENTE_AGG
             WHERE AREA = R_AR.AREA  AND DOMINIO = R_DS.DOMINIO AND CODICE_MODELLO = R_DS.CODICE_MODELLO;
             EXCEPTION WHEN OTHERS THEN
                raise_application_error (-20991,'IMPOSSIBILE AGGIORNARE IL DOMINIO (AREA,CODICE_MODELLO,DOMINIO):('||R_AR.AREA||','||R_DS.CODICE_MODELLO||','||R_DS.DOMINIO||') '||SQLERRM);
            END;
         ELSE
            BEGIN
             INSERT INTO DOMINI(AREA,DOMINIO,PRECARICA,SEQ_DOMINIO_AREA,DESCRIZIONE,TIPO,DRIVER,CONNESSIONE,UTENTE,PASSWD,DSN,
              ORDINAMENTO,CODICE_MODELLO,DATA_AGGIORNAMENTO,UTENTE_AGGIORNAMENTO)
             VALUES (R_AR.AREA,R_DS.DOMINIO,R_DS.PRECARICA,R_DS.SEQ_DOMINIO_AREA,R_DS.DESCRIZIONE,R_DS.TIPO,R_DS.DRIVER,
              R_DS.CONNESSIONE,R_DS.UTENTE,R_DS.PASSWD,R_DS.DSN,R_DS.ORDINAMENTO,R_DS.CODICE_MODELLO,SYSDATE,UTENTE_AGG);
               EXCEPTION WHEN OTHERS THEN
                  raise_application_error (-20990,'IMPOSSIBILE INSERIRE IL DOMINIO (AREA,CODICE_MODELLO,DOMINIO):('||R_AR.AREA||','||R_DS.CODICE_MODELLO||','||R_DS.DOMINIO||') '||SQLERRM);
            END;
         END IF;
         SELECT ISTRUZIONE
         INTO CINSTR
         FROM DOMINI
         WHERE AREA = AREA_OUT AND DOMINIO = R_DS.DOMINIO  AND CODICE_MODELLO = R_DS.CODICE_MODELLO;
         UPDATE DOMINI
         SET ISTRUZIONE = CINSTR
         WHERE AREA = R_AR.AREA AND DOMINIO = R_DS.DOMINIO AND CODICE_MODELLO = R_DS.CODICE_MODELLO;
         OPEN C_VALORI(AREA_OUT,R_DS.DOMINIO,R_DS.CODICE_MODELLO);
         LOOP
            FETCH C_VALORI INTO R_V;
            EXIT WHEN C_VALORI%NOTFOUND;
            BEGIN
             SELECT COUNT(1)
             INTO VESIST
             FROM VALORI_DOMINIO
               WHERE AREA = R_AR.AREA AND DOMINIO = R_DS.DOMINIO
              AND CODICE_MODELLO = R_DS.CODICE_MODELLO AND CODICE = R_V.CODICE;
             EXCEPTION WHEN OTHERS THEN
                raise_application_error (-20989,'RICERCA VALORI DOMINIO DA IMPORTARE (AREA,CODICE_MODELLO,DOMINIO,CODICE): ('||R_AR.AREA||','||R_DT.CODICE_MODELLO||','||R_DT.DOMINIO||','||R_V.CODICE||') '||SQLERRM);
            END;
            IF (VESIST > 0) THEN
             BEGIN
                UPDATE VALORI_DOMINIO SET
                VALORE = R_V.VALORE
                WHERE AREA = R_AR.AREA AND DOMINIO = R_DS.DOMINIO
                      AND CODICE_MODELLO = R_DS.CODICE_MODELLO AND CODICE = R_V.CODICE;
               EXCEPTION WHEN OTHERS THEN
               raise_application_error (-20987,'IMPOSSIBILE AGGIORNARE IL VALORE DOMINIO (AREA,CODICE_MODELLO,DOMINIO,CODICE):('||R_AR.AREA||','||R_DT.CODICE_MODELLO||','||R_DT.DOMINIO||','||R_V.CODICE||') '||SQLERRM);
             END;
            ELSE
             BEGIN
               INSERT INTO VALORI_DOMINIO(AREA,DOMINIO,CODICE,VALORE) VALUES(R_AR.AREA,R_DS.DOMINIO,R_V.CODICE,R_V.VALORE);
              EXCEPTION WHEN OTHERS THEN
              raise_application_error (-20986,'IMPOSSIBILE INSERIRE IL VALORE DOMINIO (AREA,CODICE_MODELLO,DOMINIO,CODICE):('||R_AR.AREA||','||R_DT.CODICE_MODELLO||','||R_DT.DOMINIO||','||R_V.CODICE||') '||SQLERRM);
             END;
            END IF;
         END LOOP;
         CLOSE C_VALORI;
      END LOOP;
     CLOSE C_DOMINI_SEL;
     END IF;
    END LOOP;
    CLOSE C_AREE;
    BEGIN
       IF NUM_AREE = 0 THEN
        raise_application_error (-20992,'AREA INESISTENTE'||SQLERRM);
       END IF;
     END;
     BEGIN
       IF NUM_DOMINI = 0 THEN
        raise_application_error (-20991,'DOMINI SELEZIONATI INESISTENTI (AREA,CODICE_MODELLO,DOMINIO): ('||AREA_OUT||','||CODICE_MOD_OUT||','||DOMINIO_OUT||') '||SQLERRM);
       END IF;
     END;
     RESULT:=1;
     COMMIT;
   END IF;
   --dbms_output.put_line('RESULT= '||RESULT);
   RETURN RESULT;
EXCEPTION WHEN OTHERS THEN
      ROLLBACK;
      RAISE;
      RETURN NULL;
END;
/

