CREATE OR REPLACE TRIGGER REGOLE_TIU
/******************************************************************************
 NOME:        REGOLE_TIU
 DESCRIZIONE: TRIGGER FOR CHECK DATA INTEGRITY
                          CHECK UNIQUE INTEGRITY ON PK
                          SET FUNCTIONAL INTEGRITY
                       AT INSERT OR UPDATE ON TABLE REGOLE
 ECCEZIONI:  -20007, IDENTIFICAZIONE CHIAVE PRESENTE IN TABLE
 ANNOTAZIONI: -
 REVISIONI:
 REV. DATA       AUTORE DESCRIZIONE
 ---- ---------- ------ ------------------------------------------------------
    0 __/__/____ __
******************************************************************************/
   BEFORE INSERT OR UPDATE ON REGOLE
FOR EACH ROW
DECLARE
   INTEGRITY_ERROR  EXCEPTION;
   ERRNO            INTEGER;
   ERRMSG           CHAR(200);
   DUMMY            INTEGER;
   FOUND            BOOLEAN;
BEGIN
   BEGIN  -- CHECK DATA INTEGRITY
      --  COLUMN "ID_REGOLA" USES SEQUENCE REGO_SQ
      IF :NEW.ID_REGOLA IS NULL AND NOT DELETING THEN
         SELECT REGO_SQ.NEXTVAL
           INTO :NEW.ID_REGOLA
           FROM DUAL;
      END IF;
      BEGIN  -- CHECK UNIQUE INTEGRITY ON PK OF "REGOLE"
         IF INTEGRITYPACKAGE.GETNESTLEVEL = 0 AND NOT DELETING THEN
            DECLARE
            CURSOR CPK_REGOLE(VAR_ID_REGOLA NUMBER) IS
               SELECT 1
                 FROM   REGOLE
                WHERE  ID_REGOLA = VAR_ID_REGOLA;
            MUTATING         EXCEPTION;
            PRAGMA EXCEPTION_INIT(MUTATING, -4091);
            BEGIN
               IF :NEW.ID_REGOLA IS NOT NULL THEN
                  OPEN  CPK_REGOLE(:NEW.ID_REGOLA);
                  FETCH CPK_REGOLE INTO DUMMY;
                  FOUND := CPK_REGOLE%FOUND;
                  CLOSE CPK_REGOLE;
                  IF FOUND THEN
                     ERRNO  := -20007;
                     ERRMSG := 'Identificazione "'||
                               :NEW.ID_REGOLA||
                               '" gia'' presente in REGOLE. La registrazione  non puo'' essere inserita.';
                     RAISE INTEGRITY_ERROR;
                  END IF;
               END IF;
            EXCEPTION
               WHEN MUTATING THEN NULL;  -- IGNORA CHECK SU UNIQUE PK INTEGRITY
            END;
         END IF;
      END;
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
/* END TRIGGER: REGOLE_TIU */
/


