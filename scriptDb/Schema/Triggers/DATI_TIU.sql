CREATE OR REPLACE TRIGGER DATI_TIU
/******************************************************************************
 NOME:        DATI_TIU
 DESCRIZIONE: TRIGGER FOR CHECK DATA INTEGRITY
                          CHECK UNIQUE INTEGRITY ON PK
                          SET FUNCTIONAL INTEGRITY
                       AT INSERT OR UPDATE ON TABLE DATI
 ANNOTAZIONI: -
 REVISIONI:
 REV. DATA       AUTORE DESCRIZIONE
 ---- ---------- ------ ------------------------------------------------------
    0 __/__/____ __
******************************************************************************/
   BEFORE INSERT OR UPDATE ON DATI
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
        IF :NEW.ACRONIMO_DATO IS NOT NULL THEN
            A_MESSAGGIO := 'Acronimo dato gia'' presente per l''area '||:NEW.AREA;
            A_ISTRUZIONE := 'SELECT 0 '||
                            '  FROM DUAL '||
                            ' WHERE 1 < '||
                            ' (SELECT COUNT(*) '||
                            '  FROM DATI'||
                            ' WHERE AREA =    '||''''||:NEW.AREA||''''||
                            '   AND ACRONIMO_DATO = '||''''||:NEW.ACRONIMO_DATO||''''||')';
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
/* END TRIGGER: DATI_TIU */
/


