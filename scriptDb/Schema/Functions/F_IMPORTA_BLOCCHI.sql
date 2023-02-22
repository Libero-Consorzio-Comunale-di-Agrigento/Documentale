CREATE OR REPLACE FUNCTION F_IMPORTA_BLOCCHI(
   AREA_IN             IN     VARCHAR2,
   AREA_OUT          IN     VARCHAR2,
   BLOCCO_OUT        IN     VARCHAR2,
   CODICE_MODELLO_OUT IN     VARCHAR2,
   TIPO_INS           IN      VARCHAR2,
   SOTTO_AREE        IN      VARCHAR2,
   UTENTE_AGG        IN     VARCHAR2)
RETURN NUMBER
IS
   CURSOR C_BLOCCHI_TUTTI (VAR_AREA_OUT VARCHAR2,VAR_BLOCCO_OUT VARCHAR2,VAR_CODICE_MODELLO_OUT VARCHAR2) IS
      SELECT BLOCCO,CODICE_MODELLO,TIPO,ISTRUZIONE,DRIVER,CONNESSIONE,UTENTE,PASSWD,DSN
      FROM BLOCCHI
      WHERE AREA = VAR_AREA_OUT
            AND (UPPER(BLOCCO) LIKE '%'||UPPER(VAR_BLOCCO_OUT)||'%' OR VAR_BLOCCO_OUT IS NULL)
            AND CODICE_MODELLO = VAR_CODICE_MODELLO_OUT;
   R_BT  C_BLOCCHI_TUTTI%ROWTYPE;
   CURSOR C_BLOCCHI_SEL (VAR_AREA_OUT VARCHAR2,VAR_CODICE_MODELLO_OUT VARCHAR2,VAR_BLOCCO_OUT VARCHAR2) IS
    SELECT BLOCCO,CODICE_MODELLO,TIPO,ISTRUZIONE,DRIVER,CONNESSIONE,UTENTE,PASSWD,DSN
      FROM BLOCCHI
      WHERE AREA = VAR_AREA_OUT
            AND CODICE_MODELLO = VAR_CODICE_MODELLO_OUT
            AND BLOCCO = VAR_BLOCCO_OUT;
   R_BS  C_BLOCCHI_SEL%ROWTYPE;
   CURSOR C_AREE (VAR_AREA VARCHAR2,VAR_AREA_ST VARCHAR2) IS
      SELECT AREA
      FROM AREE
      WHERE AREA LIKE VAR_AREA
      UNION
      SELECT AREA
      FROM AREE
      WHERE AREA LIKE VAR_AREA_ST;
   R_AR  C_AREE%ROWTYPE;
   ID_TIPODOC     MODELLI.ID_TIPODOC%TYPE;
   NUM_BLOCCHI    NUMBER;
   NUM_AREE       NUMBER;
   NESIST         NUMBER;
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
        BEGIN
         SELECT ID_TIPODOC
         INTO ID_TIPODOC
         FROM MODELLI WHERE AREA = R_AR.AREA
         AND CODICE_MODELLO=CODICE_MODELLO_OUT;
         EXCEPTION WHEN NO_DATA_FOUND THEN
           raise_application_error (-20999,'MODELLO INESISTENTE PER AREA '||R_AR.AREA||' '||SQLERRM);
        END;
        IF (TIPO_INS = 'T') THEN
          NUM_BLOCCHI:=0;
          OPEN C_BLOCCHI_TUTTI(AREA_OUT,BLOCCO_OUT,CODICE_MODELLO_OUT);
           LOOP
            FETCH C_BLOCCHI_TUTTI INTO R_BT;
            EXIT WHEN C_BLOCCHI_TUTTI%NOTFOUND;
            NUM_BLOCCHI:= NUM_BLOCCHI + 1;
            BEGIN
               SELECT COUNT(1)
                INTO NESIST
                FROM BLOCCHI
               WHERE AREA = R_AR.AREA
                 AND BLOCCO = R_BT.BLOCCO
                 AND CODICE_MODELLO = R_BT.CODICE_MODELLO;
               EXCEPTION WHEN OTHERS THEN
                 raise_application_error (-20998,'RICERCA BLOCCHI DA IMPORTARE (AREA,CODICE_MODELLO,BLOCCO): ('||R_AR.AREA||','||R_BT.CODICE_MODELLO||','||R_BT.BLOCCO||') '||SQLERRM);
            END;
              IF (NESIST > 0) THEN
               BEGIN
                  UPDATE BLOCCHI SET
                   TIPO = R_BT.TIPO,
                   ISTRUZIONE = R_BT.ISTRUZIONE,
                   DRIVER = R_BT.DRIVER,
                   CONNESSIONE = R_BT.CONNESSIONE,
                   UTENTE = R_BT.UTENTE,
                   PASSWD   = R_BT.PASSWD,
                   DSN   = R_BT.DSN,
                   DATA_AGGIORNAMENTO = SYSDATE,
                   UTENTE_AGGIORNAMENTO = UTENTE_AGG
                  WHERE AREA = R_AR.AREA AND BLOCCO = R_BT.BLOCCO AND CODICE_MODELLO = R_BT.CODICE_MODELLO;
                  --dbms_output.put_line('UPDATE= '||R_AR.AREA||' '||R_BT.BLOCCO||' '||R_BT.CODICE_MODELLO);
                   EXCEPTION WHEN OTHERS THEN
                     raise_application_error (-20997,'IMPOSSIBILE AGGIORNARE IL BLOCCO (AREA,CODICE_MODELLO,BLOCCO):('||R_AR.AREA||','||R_BT.CODICE_MODELLO||','||R_BT.BLOCCO||') '||SQLERRM);
                END;
              ELSE
                BEGIN
                    INSERT INTO BLOCCHI
                    (AREA,BLOCCO,CODICE_MODELLO,TIPO,ISTRUZIONE,
                    DRIVER,CONNESSIONE,UTENTE,PASSWD,DSN,DATA_AGGIORNAMENTO,UTENTE_AGGIORNAMENTO)
                 VALUES
                    (R_AR.AREA,R_BT.BLOCCO,R_BT.CODICE_MODELLO,R_BT.TIPO,R_BT.ISTRUZIONE,
                    R_BT.DRIVER,R_BT.CONNESSIONE,R_BT.UTENTE,R_BT.PASSWD,R_BT.DSN,SYSDATE,UTENTE_AGG);
                --dbms_output.put_line('INSERT= '||R_AR.AREA||' '||R_BT.BLOCCO||' '||R_BT.CODICE_MODELLO);
                 EXCEPTION WHEN OTHERS THEN
                     raise_application_error (-20996,'IMPOSSIBILE INSERIRE IL BLOCCO (AREA,CODICE_MODELLO,BLOCCO):('||R_AR.AREA||','||R_BT.CODICE_MODELLO||','||R_BT.BLOCCO||') '||SQLERRM);
                END;
              END IF;
              SELECT CORPO
                INTO CINSTR
               FROM BLOCCHI
               WHERE AREA = AREA_OUT
                 AND BLOCCO = R_BT.BLOCCO;
             UPDATE BLOCCHI
                 SET CORPO = CINSTR
               WHERE AREA = R_AR.AREA
                     AND BLOCCO = R_BT.BLOCCO;
            END LOOP;
           CLOSE C_BLOCCHI_TUTTI;
        ELSE
           NUM_BLOCCHI:=0;
           OPEN C_BLOCCHI_SEL(AREA_OUT,CODICE_MODELLO_OUT,BLOCCO_OUT);
           LOOP
              FETCH  C_BLOCCHI_SEL INTO R_BS;
              EXIT WHEN C_BLOCCHI_SEL%NOTFOUND;
              NUM_BLOCCHI:= NUM_BLOCCHI + 1;
              BEGIN
               SELECT COUNT(1)
               INTO NESIST
               FROM BLOCCHI
               WHERE AREA = R_AR.AREA
                     AND BLOCCO = R_BS.BLOCCO
                     AND CODICE_MODELLO = R_BS.CODICE_MODELLO;
               EXCEPTION WHEN OTHERS THEN
                 raise_application_error (-20995,'RICERCA BLOCCHI DA IMPORTARE (AREA,CODICE_MODELLO,BLOCCO): ('||R_AR.AREA||','||R_BS.CODICE_MODELLO||','||R_BS.BLOCCO||') '||SQLERRM);
              END;
              IF (NESIST > 0) THEN
                BEGIN
                  UPDATE BLOCCHI SET
                         TIPO = R_BS.TIPO,
                         ISTRUZIONE = R_BS.ISTRUZIONE,
                         DRIVER = R_BS.DRIVER,
                         CONNESSIONE = R_BS.CONNESSIONE,
                         UTENTE = R_BS.UTENTE,
                         PASSWD   = R_BS.PASSWD,
                         DSN   = R_BS.DSN,
                         DATA_AGGIORNAMENTO = SYSDATE,
                         UTENTE_AGGIORNAMENTO = UTENTE_AGG
                  WHERE AREA = R_AR.AREA AND BLOCCO = R_BS.BLOCCO AND CODICE_MODELLO = R_BS.CODICE_MODELLO;
                  EXCEPTION WHEN OTHERS THEN
                     raise_application_error (-20994,'IMPOSSIBILE AGGIORNARE IL BLOCCO (AREA,CODICE_MODELLO,BLOCCO):('||R_AR.AREA||','||R_BS.CODICE_MODELLO||','||R_BS.BLOCCO||') '||SQLERRM);
                 END;
              ELSE
               BEGIN
                INSERT INTO BLOCCHI
                     (AREA,BLOCCO,CODICE_MODELLO,TIPO,ISTRUZIONE,
                     DRIVER,CONNESSIONE,UTENTE,PASSWD,DSN,DATA_AGGIORNAMENTO,UTENTE_AGGIORNAMENTO)
                  VALUES
                     (R_AR.AREA,R_BS.BLOCCO,R_BS.CODICE_MODELLO,R_BS.TIPO,R_BS.ISTRUZIONE,
                     R_BS.DRIVER,R_BS.CONNESSIONE,R_BS.UTENTE,R_BS.PASSWD,R_BS.DSN,SYSDATE,UTENTE_AGG);
                  EXCEPTION WHEN OTHERS THEN
                     raise_application_error (-20993,'IMPOSSIBILE INSERIRE IL BLOCCO (AREA,CODICE_MODELLO,BLOCCO):('||R_AR.AREA||','||R_BS.CODICE_MODELLO||','||R_BS.BLOCCO||') '||SQLERRM);
               END;
              END IF;
              SELECT CORPO
                INTO CINSTR
               FROM BLOCCHI
               WHERE AREA = AREA_OUT
                 AND BLOCCO = BLOCCO_OUT;
              UPDATE BLOCCHI
                 SET CORPO = CINSTR
               WHERE AREA = R_AR.AREA
                     AND BLOCCO = R_BS.BLOCCO;
            END LOOP;
           CLOSE C_BLOCCHI_SEL;
        END IF;
      END LOOP;
      CLOSE C_AREE;
      BEGIN
        IF NUM_AREE = 0 THEN
         raise_application_error (-20992,'AREA INESISTENTE'||SQLERRM);
        END IF;
      END;
      BEGIN
        IF NUM_BLOCCHI = 0 THEN
         raise_application_error (-20991,'BLOCCHI SELEZIONATI INESISTENTI (AREA,CODICE_MODELLO,BLOCCO): ('||AREA_OUT||','||CODICE_MODELLO_OUT||','||BLOCCO_OUT||') '||SQLERRM);
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

