CREATE OR REPLACE FUNCTION F_IMPORTA_BLOCCHIPU(
    AREA_IN             IN     VARCHAR2,
    AREA_OUT          IN     VARCHAR2,
    BLOCCO_OUT        IN     VARCHAR2,
    TIPO_INS           IN      VARCHAR2,
    SOTTO_AREE        IN      VARCHAR2,
    UTENTE_AGG        IN     VARCHAR2)
RETURN NUMBER
IS
   CURSOR C_BLOCCHI_TUTTI (VAR_AREA_OUT VARCHAR2,VAR_BLOCCO_OUT VARCHAR2) IS
      SELECT BLOCCO, DRIVER, CONNESSIONE, UTENTE, PASSWD, DSN, VALORE_RITORNO, RECORD_DA_VISUALIZZARE,
            CAMPI_DI_RICERCA, SEPARATORE, TIPO, AUTOLOAD, FILTRI, AGGIORNA_PADRE,
            FILTRI_ESTERNI,CONTROLLO_JS,CHIUDI_POPUP
      FROM BLOCCHI_POPUP
      WHERE AREA = VAR_AREA_OUT AND (UPPER(BLOCCO) LIKE '%'||UPPER(VAR_BLOCCO_OUT)||'%' OR VAR_BLOCCO_OUT IS NULL);
   R_BT  C_BLOCCHI_TUTTI%ROWTYPE;
   CURSOR C_BLOCCHI_SEL (VAR_AREA_OUT VARCHAR2,VAR_BLOCCO_OUT VARCHAR2) IS
      SELECT BLOCCO, DRIVER, CONNESSIONE, UTENTE, PASSWD, DSN, VALORE_RITORNO, RECORD_DA_VISUALIZZARE,
            CAMPI_DI_RICERCA, SEPARATORE, TIPO, AUTOLOAD, FILTRI, AGGIORNA_PADRE,
            FILTRI_ESTERNI,CONTROLLO_JS,CHIUDI_POPUP
      FROM  BLOCCHI_POPUP
      WHERE AREA = VAR_AREA_OUT AND BLOCCO = VAR_BLOCCO_OUT;
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
   CURSOR C_PARAMETRI_POPUP (VAR_AREA_OUT VARCHAR2,VAR_BLOCCO_OUT VARCHAR2) IS
      SELECT BLOCCO,PARAMETRO,SEQUENZA,TIPO,VALORE_CAMPO,VALORE_DEFAULT,LABEL,AREA_DOMINIO,DOMINIO,CONDIZIONE
      FROM PARAMETRI_POPUP
      WHERE AREA = VAR_AREA_OUT AND BLOCCO = VAR_BLOCCO_OUT;
   R_PP  C_PARAMETRI_POPUP%ROWTYPE;
   NUM_BLOCCHI    NUMBER;
   NUM_AREE       NUMBER;
   NESIST         NUMBER;
   CINSTRI         CLOB;
   CINSTRC         CLOB;
   RESULT         NUMBER(1) := 0;
BEGIN
   IF ((AREA_IN != AREA_OUT) OR (SOTTO_AREE = 'S')) THEN
      IF (SOTTO_AREE = 'S') THEN
       OPEN C_AREE(AREA_IN,AREA_IN||'.%');
      ELSE
       OPEN C_AREE(AREA_IN,AREA_IN);
      END IF;
      LOOP
        BEGIN
         FETCH C_AREE INTO R_AR;
         EXIT WHEN C_AREE%NOTFOUND;
        END;
        NUM_AREE:=NUM_AREE + 1;
        IF (TIPO_INS = 'T') THEN
          NUM_BLOCCHI:=0;
          OPEN C_BLOCCHI_TUTTI(AREA_OUT, BLOCCO_OUT);
           LOOP
              FETCH C_BLOCCHI_TUTTI INTO R_BT;
              EXIT WHEN C_BLOCCHI_TUTTI%NOTFOUND;
              NUM_BLOCCHI:= NUM_BLOCCHI + 1;
              BEGIN
               SELECT COUNT(1)
                INTO NESIST
                FROM BLOCCHI_POPUP
               WHERE AREA = R_AR.AREA
                 AND BLOCCO = R_BT.BLOCCO;
               EXCEPTION WHEN OTHERS THEN
                 raise_application_error (-20999,'RICERCA BLOCCHI POPUP DA IMPORTARE (AREA,BLOCCO): ('||R_AR.AREA||','||R_BT.BLOCCO||') '||SQLERRM);
              END;
              IF (NESIST > 0) THEN
                BEGIN
                  UPDATE BLOCCHI_POPUP SET
                     DRIVER = R_BT.DRIVER,
                     CONNESSIONE = R_BT.CONNESSIONE,
                     UTENTE = R_BT.UTENTE,
                     PASSWD = R_BT.PASSWD,
                     VALORE_RITORNO = R_BT.VALORE_RITORNO,
                     RECORD_DA_VISUALIZZARE = R_BT.RECORD_DA_VISUALIZZARE,
                     CAMPI_DI_RICERCA = R_BT.CAMPI_DI_RICERCA,
                     SEPARATORE = R_BT.SEPARATORE,
                     TIPO = R_BT.TIPO,
                     AUTOLOAD = R_BT.AUTOLOAD,
                     FILTRI = R_BT.FILTRI,
                     AGGIORNA_PADRE = R_BT.AGGIORNA_PADRE,
                     DSN = R_BT.DSN,
                     DATA_AGGIORNAMENTO = SYSDATE,
                     UTENTE_AGGIORNAMENTO = UTENTE_AGG,
                     FILTRI_ESTERNI = R_BT.FILTRI_ESTERNI,
                     CONTROLLO_JS =  R_BT.CONTROLLO_JS,
                     CHIUDI_POPUP =  R_BT.CHIUDI_POPUP
                  WHERE AREA = R_AR.AREA AND BLOCCO = R_BT.BLOCCO;
                  EXCEPTION WHEN OTHERS THEN
                     raise_application_error (-20998,'IMPOSSIBILE AGGIORNARE IL BLOCCO POPUP (AREA,BLOCCO):('||R_AR.AREA||','||R_BT.BLOCCO||') '||SQLERRM);
                END;
              ELSE
                BEGIN
                  INSERT INTO BLOCCHI_POPUP
                    (AREA, BLOCCO, DRIVER, CONNESSIONE, UTENTE, PASSWD, DSN, VALORE_RITORNO,
                    RECORD_DA_VISUALIZZARE, CAMPI_DI_RICERCA, SEPARATORE, TIPO, AUTOLOAD,
                    FILTRI, AGGIORNA_PADRE, FILTRI_ESTERNI, CONTROLLO_JS, CHIUDI_POPUP,
                    DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO)
                 VALUES
                    (R_AR.AREA, R_BT.BLOCCO, R_BT.DRIVER, R_BT.CONNESSIONE, R_BT.UTENTE,
                    R_BT.PASSWD, R_BT.DSN, R_BT.VALORE_RITORNO, R_BT.RECORD_DA_VISUALIZZARE,
                    R_BT.CAMPI_DI_RICERCA, R_BT.SEPARATORE, R_BT.TIPO, R_BT.AUTOLOAD,
                    R_BT.FILTRI, R_BT.AGGIORNA_PADRE, R_BT.FILTRI_ESTERNI,
                    R_BT.CONTROLLO_JS, R_BT.CHIUDI_POPUP, SYSDATE, UTENTE_AGG);
                 EXCEPTION WHEN OTHERS THEN
                   BEGIN
                     IF (INSTR(SQLERRM,'LICO_BLPO_FK')>0) THEN
                      raise_application_error (-20997,'IMPOSSIBILE INSERIRE IL BLOCCO POPUP (AREA,BLOCCO):('||R_AR.AREA||','||R_BT.BLOCCO||') - CONTROLLO ('||R_BT.CONTROLLO_JS||') NON PRESENTE IN '||R_AR.AREA||' '||SQLERRM);
                     ELSE
                      raise_application_error (-20997,'IMPOSSIBILE INSERIRE IL BLOCCO POPUP (AREA,BLOCCO):('||R_AR.AREA||','||R_BT.BLOCCO||') '||SQLERRM);
                     END IF;
                   END;
                END;
              END IF;
              SELECT CORPO
                INTO CINSTRC
               FROM BLOCCHI_POPUP
               WHERE AREA = AREA_OUT
                 AND BLOCCO = R_BT.BLOCCO;
              SELECT ISTRUZIONE
                INTO CINSTRI
               FROM BLOCCHI_POPUP
               WHERE AREA = AREA_OUT
                 AND BLOCCO = R_BT.BLOCCO;
              UPDATE BLOCCHI_POPUP
                 SET CORPO = CINSTRC,
                     ISTRUZIONE = CINSTRI
               WHERE AREA = R_AR.AREA
                     AND BLOCCO = R_BT.BLOCCO;
              /* INSERIMENTO/AGGIORNAMNETO DI EVENTUALI FILTRI ESTERNI */
              IF (R_BT.FILTRI_ESTERNI = 'Y') THEN
                OPEN C_PARAMETRI_POPUP(AREA_OUT, R_BT.BLOCCO);
                LOOP
                  FETCH C_PARAMETRI_POPUP INTO R_PP;
                  EXIT WHEN C_PARAMETRI_POPUP%NOTFOUND;
                   BEGIN
                   SELECT COUNT(1)
                    INTO NESIST
                    FROM PARAMETRI_POPUP
                   WHERE AREA = R_AR.AREA
                     AND BLOCCO = R_PP.BLOCCO
                     AND PARAMETRO = R_PP.PARAMETRO;
                   EXCEPTION WHEN OTHERS THEN
                     raise_application_error (-20996,'RICERCA PARAMETRI POPUP DA IMPORTARE (AREA,BLOCCO,PARAMETRO):('||R_AR.AREA||','||R_PP.BLOCCO||','||R_PP.PARAMETRO||') '||SQLERRM);
                  END;
                   IF (NESIST > 0) THEN
                     BEGIN
                      UPDATE PARAMETRI_POPUP SET
                        SEQUENZA = R_PP.SEQUENZA,
                        TIPO = R_PP.TIPO,
                        VALORE_CAMPO = R_PP.VALORE_CAMPO,
                        VALORE_DEFAULT = R_PP.VALORE_DEFAULT,
                        LABEL = R_PP.LABEL,
                        AREA_DOMINIO = R_PP.AREA_DOMINIO,
                        DOMINIO = R_PP.DOMINIO,
                        CONDIZIONE = R_PP.CONDIZIONE,
                        DATA_AGGIORNAMENTO = SYSDATE,
                        UTENTE_AGGIORNAMENTO = UTENTE_AGG
                      WHERE AREA = R_AR.AREA AND BLOCCO = R_PP.BLOCCO AND PARAMETRO = R_PP.PARAMETRO;
                      EXCEPTION WHEN OTHERS THEN
                         raise_application_error (-20995,'IMPOSSIBILE AGGIORNARE IL PARAMETRO POPUP (AREA,BLOCCO,PARAMETRO):('||R_AR.AREA||','||R_PP.BLOCCO||','||R_PP.PARAMETRO||') '||SQLERRM);
                    END;
                  ELSE
                   BEGIN
                    INSERT INTO PARAMETRI_POPUP
                         (AREA, BLOCCO, PARAMETRO,SEQUENZA,TIPO,VALORE_CAMPO,VALORE_DEFAULT,LABEL,
                          AREA_DOMINIO,DOMINIO,CONDIZIONE, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO)
                       VALUES
                          (R_AR.AREA, R_PP.BLOCCO, R_PP.PARAMETRO, R_PP.SEQUENZA, R_PP.TIPO, R_PP.VALORE_CAMPO,
                           R_PP.VALORE_DEFAULT, R_PP.LABEL, R_PP.AREA_DOMINIO, R_PP.DOMINIO, R_PP.CONDIZIONE,
                           SYSDATE, UTENTE_AGG);
                    EXCEPTION WHEN OTHERS THEN
                       raise_application_error (-20994,'IMPOSSIBILE INSERIRE IL PARAMETRO POPUP (AREA,BLOCCO,PARAMETRO):('||R_AR.AREA||','||R_PP.BLOCCO||','||R_PP.PARAMETRO||') '||SQLERRM);
                   END;
                  END IF;
                END LOOP;
                CLOSE C_PARAMETRI_POPUP;
               END IF;
            END LOOP;
           CLOSE C_BLOCCHI_TUTTI;
          ELSE
            NUM_BLOCCHI:=0;
            OPEN C_BLOCCHI_SEL(AREA_OUT,BLOCCO_OUT);
           LOOP
               FETCH C_BLOCCHI_SEL INTO R_BS;
               EXIT WHEN C_BLOCCHI_SEL%NOTFOUND;
               NUM_BLOCCHI:= NUM_BLOCCHI + 1;
              BEGIN
               SELECT COUNT(1)
                INTO NESIST
                FROM BLOCCHI_POPUP
               WHERE AREA = R_AR.AREA
                 AND BLOCCO = R_BS.BLOCCO;
               EXCEPTION WHEN OTHERS THEN
                 raise_application_error (-20993,'RICERCA BLOCCHI POPUP DA IMPORTARE (AREA,BLOCCO): ('||R_AR.AREA||','||R_BS.BLOCCO||') '||SQLERRM);
              END;
              IF (NESIST > 0) THEN
                BEGIN
                  UPDATE BLOCCHI_POPUP SET
                    DRIVER = R_BS.DRIVER,
                   CONNESSIONE = R_BS.CONNESSIONE,
                   UTENTE = R_BS.UTENTE,
                   PASSWD = R_BS.PASSWD,
                   DSN = R_BS.DSN,
                   VALORE_RITORNO = R_BS.VALORE_RITORNO,
                   RECORD_DA_VISUALIZZARE = R_BS.RECORD_DA_VISUALIZZARE,
                   CAMPI_DI_RICERCA = R_BS.CAMPI_DI_RICERCA,
                   SEPARATORE = R_BS.SEPARATORE,
                   TIPO = R_BS.TIPO,
                   AUTOLOAD = R_BS.AUTOLOAD,
                   FILTRI = R_BS.FILTRI,
                   AGGIORNA_PADRE = R_BS.AGGIORNA_PADRE,
                   FILTRI_ESTERNI = R_BS.FILTRI_ESTERNI,
                   CONTROLLO_JS =  R_BS.CONTROLLO_JS,
                   CHIUDI_POPUP =  R_BS.CHIUDI_POPUP,
                   DATA_AGGIORNAMENTO = SYSDATE,
                   UTENTE_AGGIORNAMENTO = UTENTE_AGG
                  WHERE AREA = R_AR.AREA AND BLOCCO = R_BS.BLOCCO;
                 EXCEPTION WHEN OTHERS THEN
                     raise_application_error (-20992,'IMPOSSIBILE AGGIORNARE IL BLOCCO POPUP (AREA,BLOCCO):('||R_AR.AREA||','||R_BS.BLOCCO||') '||SQLERRM);
                 END;
              ELSE
               BEGIN
                INSERT INTO BLOCCHI_POPUP
                    (AREA, BLOCCO, DRIVER, CONNESSIONE, UTENTE, PASSWD, DSN, VALORE_RITORNO,
                    RECORD_DA_VISUALIZZARE, CAMPI_DI_RICERCA, SEPARATORE, TIPO, AUTOLOAD,
                    FILTRI, AGGIORNA_PADRE, FILTRI_ESTERNI, CONTROLLO_JS, CHIUDI_POPUP,
                    DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO)
                 VALUES
                    (R_AR.AREA, R_BS.BLOCCO, R_BS.DRIVER, R_BS.CONNESSIONE, R_BS.UTENTE,
                    R_BS.PASSWD, R_BS.DSN, R_BS.VALORE_RITORNO, R_BS.RECORD_DA_VISUALIZZARE,
                    R_BS.CAMPI_DI_RICERCA, R_BS.SEPARATORE, R_BS.TIPO, R_BS.AUTOLOAD,
                    R_BS.FILTRI, R_BS.AGGIORNA_PADRE, R_BS.FILTRI_ESTERNI, R_BS.CONTROLLO_JS, R_BS.CHIUDI_POPUP,
                    SYSDATE, UTENTE_AGG);
                  EXCEPTION WHEN OTHERS THEN
                   BEGIN
                     IF (INSTR(SQLERRM,'LICO_BLPO_FK')>0) THEN
                      raise_application_error (-20991,'IMPOSSIBILE INSERIRE IL BLOCCO POPUP (AREA,BLOCCO):('||R_AR.AREA||','||R_BS.BLOCCO||') - CONTROLLO ('||R_BS.CONTROLLO_JS||') NON PRESENTE IN '||R_AR.AREA||' '||SQLERRM);
                     ELSE
                      raise_application_error (-20991,'IMPOSSIBILE INSERIRE IL BLOCCO POPUP (AREA,BLOCCO):('||R_AR.AREA||','||R_BS.BLOCCO||') '||SQLERRM);
                     END IF;
                   END;
               END;
              END IF;
              SELECT CORPO
                INTO CINSTRC
               FROM BLOCCHI_POPUP
               WHERE AREA = AREA_OUT
                 AND BLOCCO = R_BS.BLOCCO;
              SELECT ISTRUZIONE
                INTO CINSTRI
               FROM BLOCCHI_POPUP
               WHERE AREA = AREA_OUT
                 AND BLOCCO = R_BS.BLOCCO;
              UPDATE BLOCCHI_POPUP
                 SET CORPO = CINSTRC,
                    ISTRUZIONE = CINSTRI
               WHERE AREA = R_AR.AREA
                     AND BLOCCO = R_BS.BLOCCO;
              /* INSERIMENTO/AGGIORNAMNETO DI EVENTUALI FILTRI ESTERNI */
              IF (R_BS.FILTRI_ESTERNI = 'Y') THEN
                OPEN C_PARAMETRI_POPUP(AREA_OUT, BLOCCO_OUT);
                LOOP
                  FETCH C_PARAMETRI_POPUP INTO R_PP;
                  EXIT WHEN C_PARAMETRI_POPUP%NOTFOUND;
                   BEGIN
                   SELECT COUNT(1)
                    INTO NESIST
                    FROM PARAMETRI_POPUP
                   WHERE AREA = R_AR.AREA
                     AND BLOCCO = R_PP.BLOCCO
                     AND PARAMETRO = R_PP.PARAMETRO;
                   EXCEPTION WHEN OTHERS THEN
                     raise_application_error (-20990,'RICERCA PARAMETRI POPUP DA IMPORTARE (AREA,BLOCCO,PARAMETRO):('||R_AR.AREA||','||R_PP.BLOCCO||','||R_PP.PARAMETRO||') '||SQLERRM);
                  END;
                   IF (NESIST > 0) THEN
                     BEGIN
                        UPDATE PARAMETRI_POPUP SET
                        SEQUENZA = R_PP.SEQUENZA,
                        TIPO = R_PP.TIPO,
                        VALORE_CAMPO = R_PP.VALORE_CAMPO,
                        VALORE_DEFAULT = R_PP.VALORE_DEFAULT,
                        LABEL = R_PP.LABEL,
                        AREA_DOMINIO = R_PP.AREA_DOMINIO,
                        DOMINIO = R_PP.DOMINIO,
                        CONDIZIONE = R_PP.CONDIZIONE,
                        DATA_AGGIORNAMENTO = SYSDATE,
                        UTENTE_AGGIORNAMENTO = UTENTE_AGG
                      WHERE AREA = R_AR.AREA AND BLOCCO = R_PP.BLOCCO AND PARAMETRO = R_PP.PARAMETRO;
                      EXCEPTION WHEN OTHERS THEN
                         raise_application_error (-20989,'IMPOSSIBILE AGGIORNARE IL PARAMETRO POPUP (AREA,BLOCCO,PARAMETRO):('||R_AR.AREA||','||R_PP.BLOCCO||','||R_PP.PARAMETRO||') '||SQLERRM);
                    END;
                  ELSE
                   BEGIN
                    INSERT INTO PARAMETRI_POPUP
                         (AREA, BLOCCO, PARAMETRO,SEQUENZA,TIPO,VALORE_CAMPO,VALORE_DEFAULT,LABEL,
                          AREA_DOMINIO,DOMINIO,CONDIZIONE, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO)
                       VALUES
                          (R_AR.AREA, R_PP.BLOCCO, R_PP.PARAMETRO, R_PP.SEQUENZA, R_PP.TIPO, R_PP.VALORE_CAMPO,
                           R_PP.VALORE_DEFAULT, R_PP.LABEL, R_PP.AREA_DOMINIO, R_PP.DOMINIO, R_PP.CONDIZIONE,
                           SYSDATE, UTENTE_AGG);
                    EXCEPTION WHEN OTHERS THEN
                       raise_application_error (-20987,'IMPOSSIBILE INSERIRE IL PARAMETRO POPUP (AREA,BLOCCO,PARAMETRO):('||R_AR.AREA||','||R_PP.BLOCCO||','||R_PP.PARAMETRO||') '||SQLERRM);
                   END;
                  END IF;
                END LOOP;
                CLOSE C_PARAMETRI_POPUP;
               END IF;
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
         raise_application_error (-20991,'BLOCCHI POPUP SELEZIONATI INESISTENTI (AREA,BLOCCO): ('||AREA_OUT||','||BLOCCO_OUT||') '||SQLERRM);
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

