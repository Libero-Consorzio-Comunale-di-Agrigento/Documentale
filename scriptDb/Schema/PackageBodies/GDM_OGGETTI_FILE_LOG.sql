CREATE OR REPLACE PACKAGE BODY gdm_oggetti_file_log
AS
/*
RESTITUISCE LA STRINGA TIPO_TABELLA@ID_TABELLA DOVE
TIPO_TABELLA = OGGETTI_FILE OPPURE OGGETTI_FILE_LOG
ID_TABELLA = ID_OGGETTO_FILE OPPURE ID_OGGETTO_FILE_LOG

RETITUISCE NULL SE NON TROVA NULLA PER LA COPPIA PASSATA IN INPUT
*/
FUNCTION F_GET_ID_OGGETTO(P_IDDOCUMENTO NUMBER,P_NOMEFILE VARCHAR2,P_IDLOG_OR_VERSION NUMBER) RETURN VARCHAR2
IS
RET  VARCHAR2(200) :=NULL;
A_CASO_VER_OR_LOG VARCHAR2(1);
A_ID_LOG ACTIVITY_LOG.ID_LOG%TYPE := NULL;
A_ID_LOG_EFFETTIVO ACTIVITY_LOG.ID_LOG%TYPE := NULL;
BEGIN
    --CONTROLLO SE P_IDLOG_OR_VERSION RAPPRESENTA L'ID VERSIONE O L'ID_LOG E VALORIZZO A_CASO_VER_OR_LOG CON V o L
    BEGIN
        SELECT ID_LOG
            INTO A_ID_LOG
          FROM ACTIVITY_LOG
         WHERE  ACTIVITY_LOG. ID_DOCUMENTO = P_IDDOCUMENTO
                      AND VERSIONE= P_IDLOG_OR_VERSION
                      AND TIPO_AZIONE= 'R';
          A_CASO_VER_OR_LOG:='V';
    EXCEPTION WHEN NO_DATA_FOUND THEN
          A_ID_LOG:=P_IDLOG_OR_VERSION;
          A_CASO_VER_OR_LOG:='L';
    END;

    --PROVO A TIRARMI IL DATO DIRETTAMENTE DALLA OGGETTI_FILE_LOG
    BEGIN
        SELECT 'OGGETTI_FILE_LOG@'||TO_CHAR(ID_OGGETTO_FILE_LOG)
        INTO RET
        FROM OGGETTI_FILE_LOG
        WHERE ID_LOG=A_ID_LOG AND FILENAME= P_NOMEFILE;
    EXCEPTION WHEN NO_DATA_FOUND THEN
        RET:=NULL;
    END;

    IF RET IS NULL  THEN
            --NON TROVATO....LO CERCO ........
            --RECUPERO L'ID LOG EFFETTIVO A PARTIRE DA QUELLO DATO
            BEGIN
               SELECT ID_LOG
                  INTO A_ID_LOG_EFFETTIVO
               FROM (
                   SELECT 'FAKE',ACTIVITY_LOG.ID_LOG
                     FROM ACTIVITY_LOG, OGGETTI_FILE_LOG
                    WHERE ID_DOCUMENTO= P_IDDOCUMENTO AND
                                (
                                     (ACTIVITY_LOG.ID_LOG> A_ID_LOG AND
                                     ACTIVITY_LOG.TIPO_AZIONE in ('M','E','C') AND
                                     A_CASO_VER_OR_LOG='L')
                                     OR
                                    (ACTIVITY_LOG.ID_LOG<= A_ID_LOG AND
                                    ACTIVITY_LOG.TIPO_AZIONE in ('R')  AND
                                    A_CASO_VER_OR_LOG='V')
                                )
                                AND activity_log.id_log=oggetti_file_log.id_log
                                AND oggetti_file_log.FILENAME = P_NOMEFILE
                    ORDER BY DECODE( A_CASO_VER_OR_LOG,'V',ACTIVITY_LOG.ID_LOG , 1) DESC,
                                      DECODE( A_CASO_VER_OR_LOG,'L',ACTIVITY_LOG.ID_LOG , 1) ASC
                )
                WHERE ROWNUM=1;
            EXCEPTION WHEN NO_DATA_FOUND THEN
                A_ID_LOG_EFFETTIVO := NULL;
            END;

            IF (A_CASO_VER_OR_LOG='L' AND A_ID_LOG_EFFETTIVO IS NULL) THEN
                --SOLO NEL CASO LOG (non caso REVISIONE)
                --Se non l'ho trovato fra i log sicuramente ci sarà l'unica riga
                  --sulla oggetti file e la data_aggiornamento sarà la sua data di inserimento
                  --perché se ci fosse stata una modifica ci sarebbe stato il log (se acceso)
                  --quindi controllo che la data_aggiornamento dell'id_log cercato sia
                   --maggiore della data_aggiornamento dell'oggetto_file. Questo
                    --ci da la certezza che il file è stato inserito prima del log cercato
                    --e quindi che per quella versione il file esisteva già.
                  --In tal caso, invece di tornare NULL, torno 0, così il chiamante sa distinguere
                  --/i due casi
                   --NULL= vallo a cercare nella oggetti_file
                   --0= il file per quella versione non esisteva ancora

                   DECLARE
                   A_CHECK NUMBER(1);
                   BEGIN
                       SELECT MAX(1)
                       INTO A_CHECK
                        FROM ACTIVITY_LOG, OGGETTI_FILE
                        WHERE ACTIVITY_LOG.ID_LOG = A_ID_LOG
                          AND ACTIVITY_LOG.ID_DOCUMENTO = P_IDDOCUMENTO
                          AND OGGETTI_FILE.FILENAME =P_NOMEFILE
                          AND (NVL(OGGETTI_FILE.DATA_INSERIMENTO,to_date('01/01/1900','dd/mm/yyyy')) <= ACTIVITY_LOG.DATA_AGGIORNAMENTO OR ACTIVITY_LOG.TIPO_AZIONE='C');

                   EXCEPTION WHEN NO_DATA_FOUND THEN
                        A_ID_LOG_EFFETTIVO := 0;
                   END;
            END IF;

            IF (A_CASO_VER_OR_LOG='V' AND  A_ID_LOG_EFFETTIVO IS NULL) THEN
                RET:=NULL;
            ELSE
                IF (A_CASO_VER_OR_LOG='L' AND  A_ID_LOG_EFFETTIVO =0) THEN
                    RET:=NULL;
                ELSE
                    IF (A_CASO_VER_OR_LOG='L' AND   A_ID_LOG_EFFETTIVO IS NULL) THEN
                            --LO PRENDO DALL'OGGETTI_FILE
                            SELECT MAX( 'OGGETTI_FILE@'||TO_CHAR(ID_OGGETTO_FILE))
                            INTO RET
                            FROM OGGETTI_FILE
                            WHERE ID_DOCUMENTO=P_IDDOCUMENTO  AND FILENAME=P_NOMEFILE;
                    ELSE
                            --LO PRENDO DALLA OGGETTI_FILE_LOG
                            SELECT MAX('OGGETTI_FILE@'||TO_CHAR(ID_OGGETTO_FILE_LOG))
                            INTO RET
                            FROM OGGETTI_FILE_LOG
                            WHERE ID_LOG=A_ID_LOG_EFFETTIVO  AND FILENAME=P_NOMEFILE;
                    END IF;
                END IF;
            END IF;
    END IF;

    RETURN RET;
END F_GET_ID_OGGETTO;
FUNCTION F_GET_HASH(P_IDDOCUMENTO NUMBER,P_NOMEFILE VARCHAR2,P_IDLOGORVERSION NUMBER) RETURN VARCHAR2
IS
RET  VARCHAR2(128) :=NULL;
A_VAR_OGGETTO VARCHAR2(200);
A_TABELLA VARCHAR2(100);
A_ID_TABELLA VARCHAR2(100);
BEGIN
    A_VAR_OGGETTO:=F_GET_ID_OGGETTO(P_IDDOCUMENTO,P_NOMEFILE,P_IDLOGORVERSION);

    IF A_VAR_OGGETTO  IS NOT NULL THEN
        A_TABELLA := SUBSTR(A_VAR_OGGETTO,1,INSTR(A_VAR_OGGETTO,'@')-1);
        A_ID_TABELLA := SUBSTR(A_VAR_OGGETTO,INSTR(A_VAR_OGGETTO,'@')+1);

        IF A_TABELLA='OGGETTI_FILE' THEN
            SELECT MAX(HASHCODE)
            INTO RET
            FROM OGGETTI_FILE, IMPRONTE_FILE
            WHERE ID_OGGETTO_FILE = TO_NUMBER(A_ID_TABELLA) AND
                         IMPRONTE_FILE.ID_DOCUMENTO = OGGETTI_FILE.ID_DOCUMENTO AND
                         IMPRONTE_FILE.FILENAME = OGGETTI_FILE.FILENAME;
        ELSE
            SELECT IMPRONTA
            INTO RET
            FROM OGGETTI_FILE_LOG
            WHERE ID_OGGETTO_FILE_LOG = TO_NUMBER(A_ID_TABELLA);
        END IF;
    END IF;

    RETURN RET;
END F_GET_HASH;
END;
/

