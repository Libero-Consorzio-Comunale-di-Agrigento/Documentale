CREATE OR REPLACE TRIGGER AREE_TIU
/******************************************************************************
 NOME:        AREE_TIU
 DESCRIZIONE: TRIGGER FOR CHECK DATA INTEGRITY
                          CHECK UNIQUE INTEGRITY ON PK
                          SET FUNCTIONAL INTEGRITY
                       AT INSERT OR UPDATE ON TABLE AREE
 ECCEZIONI:  -20007, IDENTIFICAZIONE CHIAVE PRESENTE IN TABLE
 ANNOTAZIONI: -
 REVISIONI:
 REV. DATA       AUTORE DESCRIZIONE
 ---- ---------- ------ ------------------------------------------------------
    0 __/__/____ __
******************************************************************************/
   BEFORE INSERT OR UPDATE ON AREE
FOR EACH ROW
DECLARE
   INTEGRITY_ERROR  EXCEPTION;
   ERRNO            INTEGER;
   ERRMSG           CHAR(200);
   DUMMY            INTEGER;
   FOUND            BOOLEAN;
BEGIN
   BEGIN  -- CHECK DATA INTEGRITY
      --  COLUMN "ID_AREA" USES SEQUENCE SEQ_ID_AREA
      IF :NEW.ID_AREA IS NULL AND NOT DELETING THEN
         SELECT SEQ_ID_AREA.NEXTVAL
           INTO :NEW.ID_AREA
           FROM DUAL;
      END IF;
      BEGIN  -- CHECK UNIQUE INTEGRITY ON PK OF "AREE"
         IF INTEGRITYPACKAGE.GETNESTLEVEL = 0 AND NOT DELETING THEN
            DECLARE
            CURSOR CPK_AREE(VAR_AREA VARCHAR) IS
               SELECT 1
                 FROM   AREE
                WHERE  AREA = VAR_AREA;
            MUTATING         EXCEPTION;
            PRAGMA EXCEPTION_INIT(MUTATING, -4091);
            BEGIN
               IF :NEW.AREA IS NOT NULL THEN
                  OPEN  CPK_AREE(:NEW.AREA);
                  FETCH CPK_AREE INTO DUMMY;
                  FOUND := CPK_AREE%FOUND;
                  CLOSE CPK_AREE;
                  IF FOUND THEN
                     ERRNO  := -20007;
                     ERRMSG := 'Identificazione "'||
                               :NEW.AREA||
                               '" gia'' presente in AREE. La registrazione  non puo'' essere inserita.';
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
/* END TRIGGER: AREE_TIU */
/


