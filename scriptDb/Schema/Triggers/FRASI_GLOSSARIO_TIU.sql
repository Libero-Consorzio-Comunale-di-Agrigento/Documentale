CREATE OR REPLACE TRIGGER FRASI_GLOSSARIO_TIU
/******************************************************************************
 NOME:        FRASI_GLOSSARIO_TIU
 DESCRIZIONE: TRIGGER FOR CHECK DATA INTEGRITY
                          CHECK UNIQUE INTEGRITY ON PK
                          SET FUNCTIONAL INTEGRITY
                       AT INSERT OR UPDATE ON TABLE FRASI_GLOSSARIO
 ECCEZIONI:  -20007, IDENTIFICAZIONE CHIAVE PRESENTE IN TABLE
 ANNOTAZIONI: -
 REVISIONI:
 REV. DATA       AUTORE DESCRIZIONE
 ---- ---------- ------ ------------------------------------------------------
    0 __/__/____ __
******************************************************************************/
   BEFORE INSERT OR UPDATE ON FRASI_GLOSSARIO
FOR EACH ROW
DECLARE
   INTEGRITY_ERROR  EXCEPTION;
   ERRNO            INTEGER;
   ERRMSG           CHAR(200);
   DUMMY            INTEGER;
   FOUND            BOOLEAN;
BEGIN
   BEGIN  -- CHECK DATA INTEGRITY
      --  COLUMN "ID_FRASE" USES SEQUENCE SEQ_ID_FRASE
      IF :NEW.ID_FRASE IS NULL AND NOT DELETING THEN
         SELECT SEQ_ID_FRASE.NEXTVAL
           INTO :NEW.ID_FRASE
           FROM DUAL;
      END IF;
      BEGIN  -- CHECK UNIQUE INTEGRITY ON PK OF "FRASI_GLOSSARIO"
         IF INTEGRITYPACKAGE.GETNESTLEVEL = 0 AND NOT DELETING THEN
            DECLARE
            CURSOR CPK_FRASI_GLOSSARIO(VAR_ID_FRASE NUMBER) IS
               SELECT 1
                 FROM   FRASI_GLOSSARIO
                WHERE  ID_FRASE = VAR_ID_FRASE;
            MUTATING         EXCEPTION;
            PRAGMA EXCEPTION_INIT(MUTATING, -4091);
            BEGIN
               IF :NEW.ID_FRASE IS NOT NULL THEN
                  OPEN  CPK_FRASI_GLOSSARIO(:NEW.ID_FRASE);
                  FETCH CPK_FRASI_GLOSSARIO INTO DUMMY;
                  FOUND := CPK_FRASI_GLOSSARIO%FOUND;
                  CLOSE CPK_FRASI_GLOSSARIO;
                  IF FOUND THEN
                     ERRNO  := -20007;
                     ERRMSG := 'Identificazione "'||
                               :NEW.ID_FRASE||
                               '" gia'' presente in FRASI_GLOSSARIO. La registrazione  non puo'' essere inserita.';
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
/* END TRIGGER: FRASI_GLOSSARIO_TIU */
/


