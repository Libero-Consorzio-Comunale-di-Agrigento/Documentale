CREATE OR REPLACE PACKAGE BODY GDM_UTILITY AS
   FUNCTION PARSEQUERYSTRING (P_QRYSTR IN VARCHAR2,P_CAMPO IN VARCHAR2) RETURN VARCHAR2 AS
          SEPARATOR VARCHAR2(1) := ',';
         EQUALS VARCHAR2(1) := '=';
         QRYSTR  VARCHAR2(32000);
      BEGIN
          QRYSTR := P_QRYSTR;
            --CASO BASE
         --STRINGA DEL TIPO CAMPO=VALORE
         IF (INSTR(QRYSTR,SEPARATOR)=0) THEN
            IF (SUBSTR(QRYSTR,1,INSTR(QRYSTR,EQUALS)-1)=P_CAMPO) THEN
              RETURN SUBSTR(QRYSTR,INSTR(QRYSTR,EQUALS)+1,LENGTH(QRYSTR));
           END IF;
            --CASO GENERALE
         --STRINGA DEL TIPO CAMPO1=VALORE1&CAMPO2=VALORE2&.....&CAMPON=VALOREN
         ELSE
            DECLARE
             D_INDEX_CAMPO NUMBER;
            D_VALORESINGOLO VARCHAR2(2000);
              BEGIN
             D_INDEX_CAMPO:=INSTR(SEPARATOR||QRYSTR,SEPARATOR||P_CAMPO);
            D_VALORESINGOLO:=SUBSTR(SEPARATOR||QRYSTR||SEPARATOR,D_INDEX_CAMPO+1,
                                                 INSTR(SEPARATOR||QRYSTR||SEPARATOR,SEPARATOR,D_INDEX_CAMPO+1)-
                                      D_INDEX_CAMPO-1);
            RETURN PARSEQUERYSTRING(D_VALORESINGOLO,P_CAMPO);
           END;
         END IF;
         RETURN SEPARATOR;
      END;
   FUNCTION SOVRASCRIVI_SEMPRE (P_AREA IN VARCHAR2,P_TIPODOC IN VARCHAR2,P_STATUS IN VARCHAR2) RETURN VARCHAR2 AS
      BEGIN
          RETURN 'N';
      END;
   FUNCTION PARSEBODY_CF (P_IDDOCUMENTO IN NUMBER,P_BODY IN VARCHAR2) RETURN VARCHAR2 AS
         A_RET              NUMBER(1) := 0;
        ACTUALINDEX_I       NUMBER(10) := 0;
        ACTUALINDEX_F       NUMBER(10) := 0;
        A_VALORE_SOSTITUITO VARCHAR2(32000);
        A_BODY              VARCHAR2(32000) :='';
        A_SEPARATOR         VARCHAR2(1) :='#';
      BEGIN
            --SOSTITUZIONE DEI PARAMETRI
         ACTUALINDEX_I := INSTR(P_BODY,A_SEPARATOR);
         ACTUALINDEX_F := INSTR(P_BODY,A_SEPARATOR,ACTUALINDEX_I+1);
         A_BODY        := P_BODY;
         WHILE ACTUALINDEX_F<>0 LOOP
            --ESISTE IL # FINALE
            DECLARE
            A_PARAMETRO VARCHAR2(500);
            BEGIN
                A_PARAMETRO:=SUBSTR(A_BODY,ACTUALINDEX_I+1,ACTUALINDEX_F-ACTUALINDEX_I-1);
             BEGIN
                   SELECT NVL(DBMS_LOB.SUBSTR(VALORE_CLOB, DBMS_LOB.GETLENGTH(VALORE_CLOB),1), NVL(TO_CHAR(VALORE_DATA, REPLACE(FORMATO_DATA, 'hh:', 'hh24:')),TO_CHAR(VALORE_NUMERO) ))
                        INTO A_VALORE_SOSTITUITO
                     FROM VALORI VA, CAMPI_DOCUMENTO CD, DATI D, DOCUMENTI DOC
                    WHERE VA.ID_DOCUMENTO   = P_IDDOCUMENTO
                      AND D.DATO = CD.NOME
                      AND VA.ID_CAMPO=CD.ID_CAMPO
                      AND CD.ID_TIPODOC = DOC.ID_TIPODOC
                      AND DOC.ID_DOCUMENTO = P_IDDOCUMENTO
                      AND D.AREA = DOC.AREA AND D.DATO=A_PARAMETRO;
                    EXCEPTION WHEN NO_DATA_FOUND THEN
                      A_VALORE_SOSTITUITO:=A_PARAMETRO;
                           WHEN OTHERS THEN
                      A_VALORE_SOSTITUITO:=A_PARAMETRO;
             END;
             A_BODY:=SUBSTR(A_BODY,1,ACTUALINDEX_I-1)||A_VALORE_SOSTITUITO||SUBSTR(A_BODY,ACTUALINDEX_F+1,LENGTH(A_BODY));
            END;
            ACTUALINDEX_I := INSTR(A_BODY,A_SEPARATOR);
            ACTUALINDEX_F := INSTR(A_BODY,A_SEPARATOR,ACTUALINDEX_I+1);
            END LOOP;
         RETURN A_BODY;
      END;
    PROCEDURE extract_file (idobj IN NUMBER, idlog IN NUMBER)
       IS
       vblob      BLOB;
       vstart     NUMBER                       := 1;
       bytelen    NUMBER                       := 32000;
       len        NUMBER;
       filename   oggetti_file.filename%TYPE;
       my_vr      RAW (32000);
       x          NUMBER;
       l_output   UTL_FILE.file_type;
    BEGIN
       IF idlog > 0
       THEN
          SELECT DBMS_LOB.getlength (testoocr), filename, testoocr
            INTO len, filename, vblob
            FROM oggetti_file_log
           WHERE id_oggetto_file = idobj AND id_log = idlog;
       ELSE
          SELECT DBMS_LOB.getlength (testoocr), filename, testoocr
            INTO len, filename, vblob
            FROM oggetti_file
           WHERE id_oggetto_file = idobj;
       END IF;
       IF len=0 THEN
          RETURN;
       END IF;
       l_output := UTL_FILE.fopen ('TEMP_FILE', idobj, 'wb', 32760);
       vstart := 1;
       bytelen := 32000;
       x := len;
       IF len < 32760
       THEN
          UTL_FILE.put_raw (l_output, vblob);
          UTL_FILE.fflush (l_output);
       ELSE
          vstart := 1;
          WHILE vstart < len AND bytelen > 0
          LOOP
             DBMS_LOB.READ (vblob, bytelen, vstart, my_vr);
             UTL_FILE.put_raw (l_output, my_vr);
             UTL_FILE.fflush (l_output);
             vstart := vstart + bytelen;
             x := x - bytelen;
             IF x < 32000
             THEN
                bytelen := x;
             END IF;
          END LOOP;
       END IF;
       UTL_FILE.fclose (l_output);
    END;

    FUNCTION MKDIR(A_PERCORSO in VARCHAR2) return NUMBER is language java name 'FileSystemModule.mkdir(java.lang.String) return int';

   PROCEDURE MKDIR(A_PERCORSO VARCHAR2)
   AS
   --A_CMD                            VARCHAR2(1000);
   A_RESULT                         NUMBER;
   BEGIN
     /* A_CMD:='mkdir -m 777 -p '||A_PERCORSO;
      execute immediate 'call os_command.exec('''||A_CMD||''') into :A_RESULT ' using out A_RESULT;
      IF A_RESULT<>0 THEN
         RAISE_APPLICATION_ERROR(-20999,'Errore nel creare/verificare il percorso '||A_PERCORSO);
      END IF;*/
       A_RESULT := MKDIR(A_PERCORSO);

       IF A_RESULT=-1 THEN
             RAISE_APPLICATION_ERROR(-20999,'Non riesco a creare il percorso. Verificare che questo sia corretto!');
       END IF;

   EXCEPTION WHEN OTHERS THEN
      /*BEGIN
           A_CMD:='cmd.exe /c IF exist '||A_PERCORSO||' ( echo dir exists ) ELSE ( mkdir '||A_PERCORSO||' ) ';

        execute immediate 'call os_command.exec('''||A_CMD||''') into :A_RESULT ' using out A_RESULT;
          IF A_RESULT<>0 THEN
             RAISE_APPLICATION_ERROR(-20999,'Errore nel creare il percorso '||A_PERCORSO);
          END IF;
      EXCEPTION WHEN OTHERS THEN*/
         RAISE_APPLICATION_ERROR(-20999,'Errore in MKDIR('||A_PERCORSO||'): '||sqlerrm);
      /*END;     */
   END;

   PROCEDURE RMDIR(A_PERCORSO VARCHAR2, A_FILE NUMBER DEFAULT 0)
   AS
   A_CMD                            VARCHAR2(1000);
   A_RESULT                         NUMBER;
   BEGIN
      IF A_FILE=1 THEN
          A_CMD:='rm '||A_PERCORSO;
      ELSE
          A_CMD:='rm -rf '||A_PERCORSO;
      END IF;
      execute immediate 'call os_command.exec('''||A_CMD||''') into :A_RESULT ' using out A_RESULT;
      IF A_RESULT<>0 THEN
         RAISE_APPLICATION_ERROR(-20999,'Errore nel rimuovere il percorso '||A_PERCORSO);
      END IF;
   EXCEPTION WHEN OTHERS THEN
      -- TENTO DI ESEGURILO WINDOWS
      BEGIN
           IF A_FILE=1 THEN
               A_CMD:='cmd.exe /c IF not exist '||A_PERCORSO||' ( echo dir noexists ) ELSE ( del '||A_PERCORSO||' ) ';
           ELSE
                A_CMD:='cmd.exe /c IF not exist '||A_PERCORSO||' ( echo dir noexists ) ELSE ( rmdir '||A_PERCORSO||' ) ';
           END IF;

        execute immediate 'call os_command.exec('''||A_CMD||''') into :A_RESULT ' using out A_RESULT;
          IF A_RESULT<>0 THEN
             RAISE_APPLICATION_ERROR(-20999,'Errore nel rimuovere il percorso '||A_PERCORSO);
          END IF;
      EXCEPTION WHEN OTHERS THEN
         RAISE_APPLICATION_ERROR(-20999,'Errore in RMDIR('||A_PERCORSO||'): '||sqlerrm);
      END;

   END;

   PROCEDURE CHMOD(A_PERCORSO VARCHAR2)
   AS
   A_CMD                            VARCHAR2(1000);
   A_RESULT                         NUMBER;
   BEGIN
      A_CMD:='chmod 777 '||A_PERCORSO;
      execute immediate 'call os_command.exec('''||A_CMD||''') into :A_RESULT ' using out A_RESULT;
      IF A_RESULT<>0 THEN
         RAISE_APPLICATION_ERROR(-20999,'Errore nel fornire i diritti al percorso '||A_PERCORSO);
      END IF;
   EXCEPTION WHEN OTHERS THEN
      if instr(sqlerrm,'Cannot run program')   >0 then
         NULL;
      ELSE
          RAISE_APPLICATION_ERROR(-20999,'Errore in CHMOD('||A_PERCORSO||'): '||sqlerrm);
      end if;
   END;

   FUNCTION F_MULTILINGUA(P_ESPRESSIONE IN  DIZIONARIO_LINGUA.ESPRESSIONE%TYPE,P_LINGUA IN DIZIONARIO_LINGUA.LINGUA%TYPE) RETURN VARCHAR2 AS
        RESULT VARCHAR2(2000) ;
      BEGIN

        SELECT TRADUZIONE
        INTO RESULT
        FROM DIZIONARIO_LINGUA
        WHERE ESPRESSIONE = P_ESPRESSIONE AND LINGUA = P_LINGUA;
        RETURN RESULT;
        EXCEPTION
         WHEN NO_DATA_FOUND THEN
           RETURN P_ESPRESSIONE;
     END;

END GDM_UTILITY;
/

