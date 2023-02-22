CREATE OR REPLACE TRIGGER LOG_GDM_TIU
/******************************************************************************
 NOME:        LOG_GDM_TIU
 DESCRIZIONE: TRIGGER FOR CHECK DATA INTEGRITY
                          CHECK UNIQUE INTEGRITY ON PK
                          SET FUNCTIONAL INTEGRITY
                       AT INSERT OR UPDATE ON TABLE LOG_GDM
 ECCEZIONI:  -20007, IDENTIFICAZIONE CHIAVE PRESENTE IN TABLE
 ANNOTAZIONI: -
 REVISIONI:
 REV. DATA       AUTORE DESCRIZIONE
 ---- ---------- ------ ------------------------------------------------------
    0 __/__/____ __
******************************************************************************/
   BEFORE INSERT OR UPDATE ON LOG_GDM
FOR EACH ROW
DECLARE
   INTEGRITY_ERROR  EXCEPTION;
   ERRNO            INTEGER;
   ERRMSG           CHAR(200);
   DUMMY            INTEGER;
   FOUND            BOOLEAN;
BEGIN
   BEGIN  -- CHECK DATA INTEGRITY
      --  COLUMN "ID_LOG" USES SEQUENCE LOGG_SQ
      IF :NEW.ID_LOG IS NULL AND NOT DELETING THEN
         SELECT LOGG_SQ.NEXTVAL
           INTO :NEW.ID_LOG
           FROM DUAL;
      END IF;
      BEGIN  -- CHECK UNIQUE INTEGRITY ON PK OF "LOG_GDM"
         IF INTEGRITYPACKAGE.GETNESTLEVEL = 0 AND NOT DELETING THEN
            DECLARE
            CURSOR CPK_LOG_GDM(VAR_ID_LOG NUMBER) IS
               SELECT 1
                 FROM   LOG_GDM
                WHERE  ID_LOG = VAR_ID_LOG;
            MUTATING         EXCEPTION;
            PRAGMA EXCEPTION_INIT(MUTATING, -4091);
            BEGIN
               IF :NEW.ID_LOG IS NOT NULL THEN
                  OPEN  CPK_LOG_GDM(:NEW.ID_LOG);
                  FETCH CPK_LOG_GDM INTO DUMMY;
                  FOUND := CPK_LOG_GDM%FOUND;
                  CLOSE CPK_LOG_GDM;
                  IF FOUND THEN
                     ERRNO  := -20007;
                     ERRMSG := 'Identificazione "'||
                               :NEW.ID_LOG||
                               '" gia'' presente in LOG_  La registrazione  non puo'' essere inserita.';
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
/* END TRIGGER: LOG_GDM_TIU */
/


