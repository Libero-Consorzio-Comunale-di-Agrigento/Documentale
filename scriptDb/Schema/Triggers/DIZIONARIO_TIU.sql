CREATE OR REPLACE TRIGGER DIZIONARIO_TIU
/******************************************************************************
 NOME:        DIZIONARIO_TIU
 DESCRIZIONE: TRIGGER FOR CHECK DATA INTEGRITY
                          CHECK UNIQUE INTEGRITY ON PK
                          SET FUNCTIONAL INTEGRITY
                       AT INSERT OR UPDATE ON TABLE DIZIONARI
 ECCEZIONI:  -20007, IDENTIFICAZIONE CHIAVE PRESENTE IN TABLE
 ANNOTAZIONI: -
 REVISIONI:
 REV. DATA       AUTORE DESCRIZIONE
 ---- ---------- ------ ------------------------------------------------------
    0 __/__/____ __
******************************************************************************/
   BEFORE INSERT OR UPDATE ON DIZIONARI
FOR EACH ROW
DECLARE
   INTEGRITY_ERROR  EXCEPTION;
   ERRNO            INTEGER;
   ERRMSG           CHAR(200);
   DUMMY            INTEGER;
   FOUND            BOOLEAN;
BEGIN
   BEGIN  -- CHECK DATA INTEGRITY
      --  COLUMN "ID_DIZIONARIO" USES SEQUENCE DIZI_SQ
      IF :NEW.ID_DIZIONARIO IS NULL AND NOT DELETING THEN
         SELECT DIZI_SQ.NEXTVAL
           INTO :NEW.ID_DIZIONARIO
           FROM DUAL;
      END IF;
      BEGIN  -- CHECK UNIQUE INTEGRITY ON PK OF "DIZIONARI"
         IF INTEGRITYPACKAGE.GETNESTLEVEL = 0 AND NOT DELETING THEN
            DECLARE
            CURSOR CPK_DIZIONARI(VAR_ID_DIZIONARIO NUMBER) IS
               SELECT 1
                 FROM   DIZIONARI
                WHERE  ID_DIZIONARIO = VAR_ID_DIZIONARIO;
            MUTATING         EXCEPTION;
            PRAGMA EXCEPTION_INIT(MUTATING, -4091);
            BEGIN
               IF :NEW.ID_DIZIONARIO IS NOT NULL THEN
                  OPEN  CPK_DIZIONARI(:NEW.ID_DIZIONARIO);
                  FETCH CPK_DIZIONARI INTO DUMMY;
                  FOUND := CPK_DIZIONARI%FOUND;
                  CLOSE CPK_DIZIONARI;
                  IF FOUND THEN
                     ERRNO  := -20007;
                     ERRMSG := 'Identificazione "'||
                               :NEW.ID_DIZIONARIO||
                               '" gia'' presente in DIZIONARI. La registrazione  non puo'' essere inserita.';
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
/* END TRIGGER: DIZIONARIO_TIU */
/


