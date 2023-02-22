CREATE OR REPLACE TRIGGER TIPI_PRATICHE_TIU
/******************************************************************************
 NOME:        TIPI_PRATICHE_TIU
 DESCRIZIONE: TRIGGER FOR CHECK DATA INTEGRITY
                          CHECK UNIQUE INTEGRITY ON PK
                          SET FUNCTIONAL INTEGRITY
                       AT INSERT OR UPDATE ON TABLE TIPI_PRATICHE
 ECCEZIONI:  -20007, IDENTIFICAZIONE CHIAVE PRESENTE IN TABLE
 ANNOTAZIONI: -
 REVISIONI:
 REV. DATA       AUTORE DESCRIZIONE
 ---- ---------- ------ ------------------------------------------------------
    0 __/__/____ __
******************************************************************************/
   BEFORE INSERT OR UPDATE ON TIPI_PRATICHE
FOR EACH ROW
DECLARE
   INTEGRITY_ERROR  EXCEPTION;
   ERRNO            INTEGER;
   ERRMSG           CHAR(200);
   DUMMY            INTEGER;
   FOUND            BOOLEAN;
   A_ISTRUZIONE  VARCHAR2(2000);
   A_MESSAGGIO   VARCHAR2(2000);
BEGIN
   BEGIN  -- CHECK DATA INTEGRITY
      --  COLUMN "ID_TIPO_PRATICA" USES SEQUENCE GD4.SEQ_ID_TIPO_PRATICA
      IF :NEW.ID_TIPO_PRATICA IS NULL AND NOT DELETING THEN
         SELECT SEQ_ID_TIPO_PRATICA.NEXTVAL
           INTO :NEW.ID_TIPO_PRATICA
           FROM DUAL;
      END IF;
      BEGIN  -- CHECK UNIQUE INTEGRITY ON PK OF "TIPI_PRATICHE"
         IF INTEGRITYPACKAGE.GETNESTLEVEL = 0 AND NOT DELETING THEN
            DECLARE
            CURSOR CPK_TIPI_PRATICHE(VAR_ID_TIPO_PRATICA NUMBER) IS
               SELECT 1
                 FROM   TIPI_PRATICHE
                WHERE  ID_TIPO_PRATICA = VAR_ID_TIPO_PRATICA;
            MUTATING         EXCEPTION;
            PRAGMA EXCEPTION_INIT(MUTATING, -4091);
            BEGIN
               IF :NEW.ID_TIPO_PRATICA IS NOT NULL THEN
                  OPEN  CPK_TIPI_PRATICHE(:NEW.ID_TIPO_PRATICA);
                  FETCH CPK_TIPI_PRATICHE INTO DUMMY;
                  FOUND := CPK_TIPI_PRATICHE%FOUND;
                  CLOSE CPK_TIPI_PRATICHE;
                  IF FOUND THEN
                     ERRNO  := -20007;
                     ERRMSG := 'Identificazione "'||
                               :NEW.ID_TIPO_PRATICA||
                               '" gia'' presente in TIPI_PRATICHE. La registrazione  non puo'' essere inserita.';
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
       A_ISTRUZIONE := 'CheckDefaultRecord';
       A_MESSAGGIO := 'Caso non previsto di variazione flag IS_DEFAULT';
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
/* END TRIGGER: TIPI_PRATICHE_TIU */
/

