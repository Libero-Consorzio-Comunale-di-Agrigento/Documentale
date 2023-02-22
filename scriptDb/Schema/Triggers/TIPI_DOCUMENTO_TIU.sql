CREATE OR REPLACE TRIGGER TIPI_DOCUMENTO_TIU
/******************************************************************************
 NOME:        TIPI_DOCUMENTO_TIU
 DESCRIZIONE: TRIGGER FOR CHECK DATA INTEGRITY
                          CHECK UNIQUE INTEGRITY ON PK
                          SET FUNCTIONAL INTEGRITY
                       AT INSERT OR UPDATE ON TABLE TIPI_DOCUMENTO
 ECCEZIONI:  -20007, IDENTIFICAZIONE CHIAVE PRESENTE IN TABLE
 ANNOTAZIONI: -
 REVISIONI:
 REV. DATA       AUTORE DESCRIZIONE
 ---- ---------- ------ ------------------------------------------------------
    0 __/__/____ __
******************************************************************************/
   BEFORE INSERT OR UPDATE ON TIPI_DOCUMENTO
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW
DECLARE
   INTEGRITY_ERROR  EXCEPTION;
   ERRNO            INTEGER;
   ERRMSG           CHAR(200);
   DUMMY            INTEGER;
   FOUND            BOOLEAN;
   A_MESSAGGIO      VARCHAR2(2000);
   A_ISTRUZIONE     VARCHAR2(2000);
BEGIN
   BEGIN  -- CHECK DATA INTEGRITY
      IF INSERTING OR UPDATING THEN
        IF :NEW.ALIAS_MODELLO IS NULL AND :NEW.ACRONIMO_MODELLO IS NOT NULL THEN
            RAISE_APPLICATION_ERROR(-20999,'Alias Modello ed Acronimo modello devo essere entrambi nulli o non nulli!');
        END IF;
        IF :NEW.ALIAS_MODELLO IS NOT NULL AND :NEW.ACRONIMO_MODELLO IS NULL THEN
            RAISE_APPLICATION_ERROR(-20999,'Alias Modello ed Acronimo modello devo essere entrambi nulli o non nulli!');
        END IF;
        IF :NEW.ALIAS_MODELLO IS NOT NULL AND :NEW.ACRONIMO_MODELLO IS NOT NULL THEN
            IF :NEW.AREA_MODELLO IS NULL THEN
                RAISE_APPLICATION_ERROR(-20999,'Area Modello nulla!');
            END IF;
            A_MESSAGGIO := 'Alias Modello o Acronimo Modello gia'' presente per l''area '||:NEW.AREA_MODELLO;
            A_ISTRUZIONE := 'select 0 '||
                            '  FROM DUAL '||
                            ' WHERE 1 < '||
                            ' (SELECT COUNT(*) '||
                            '  FROM TIPI_DOCUMENTO'||
                            ' WHERE AREA_MODELLO =    '||''''||:NEW.AREA_MODELLO||''''||
                            '   AND (ALIAS_MODELLO =  '||''''||:NEW.ALIAS_MODELLO||''''||
                            '   OR ACRONIMO_MODELLO = '||''''||:NEW.ACRONIMO_MODELLO||''''||'))';
            INTEGRITYPACKAGE.SET_POSTEVENT(A_ISTRUZIONE, A_MESSAGGIO);
        END IF;
      END IF;
      NULL;
   END;
   BEGIN  -- SET FUNCTIONAL INTEGRITY
      IF INTEGRITYPACKAGE.GETNESTLEVEL = 0 THEN
         INTEGRITYPACKAGE.NEXTNESTLEVEL;
         BEGIN  -- GLOBAL FUNCTIONAL INTEGRITY AT LEVEL 0
            /* NONE */ NULL;
         END;
         INTEGRITYPACKAGE.PREVIOUSNESTLEVEL;
      END IF;
      INTEGRITYPACKAGE.NEXTNESTLEVEL;
      BEGIN  -- FULL FUNCTIONAL INTEGRITY AT ANY LEVEL
         /* NONE */ NULL;
      END;
      INTEGRITYPACKAGE.PREVIOUSNESTLEVEL;
   END;
EXCEPTION
   WHEN INTEGRITY_ERROR THEN
        INTEGRITYPACKAGE.INITNESTLEVEL;
        RAISE_APPLICATION_ERROR(ERRNO, ERRMSG);
   WHEN OTHERS THEN
        INTEGRITYPACKAGE.INITNESTLEVEL;
        RAISE;
END;
/* END TRIGGER: TIPI_DOCUMENTO_TIU */
/


