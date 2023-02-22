CREATE OR REPLACE TRIGGER LINKS_TIU
/******************************************************************************
 NOME:        LINKS_TIU
 DESCRIZIONE: TRIGGER FOR CHECK DATA INTEGRITY
                          CHECK UNIQUE INTEGRITY ON PK
                          SET FUNCTIONAL INTEGRITY
                       AT INSERT OR UPDATE ON TABLE LINKS
 ECCEZIONI:  -20007, IDENTIFICAZIONE CHIAVE PRESENTE IN TABLE
 ANNOTAZIONI: -
 REVISIONI:
 REV. DATA       AUTORE DESCRIZIONE
 ---- ---------- ------ ------------------------------------------------------
    0 __/__/____ __
******************************************************************************/
   BEFORE INSERT OR UPDATE ON LINKS
FOR EACH ROW
DECLARE
   INTEGRITY_ERROR  EXCEPTION;
   ERRNO            INTEGER;
   ERRMSG           CHAR(200);
   DUMMY            INTEGER;
   FOUND            BOOLEAN;
BEGIN
   BEGIN  -- CHECK DATA INTEGRITY
      BEGIN  -- CHECK UNIQUE INTEGRITY ON PK OF "LINKS"
         IF INTEGRITYPACKAGE.GETNESTLEVEL = 0 AND NOT DELETING THEN
            DECLARE
            CURSOR CPK_LINKS(VAR_ID_LINK NUMBER) IS
               SELECT 1
                 FROM   LINKS
                WHERE  ID_LINK = VAR_ID_LINK;
            MUTATING         EXCEPTION;
            PRAGMA EXCEPTION_INIT(MUTATING, -4091);
            BEGIN
               IF :NEW.ID_LINK IS NOT NULL THEN
                  OPEN  CPK_LINKS(:NEW.ID_LINK);
                  FETCH CPK_LINKS INTO DUMMY;
                  FOUND := CPK_LINKS%FOUND;
                  close CPK_LINKS;
                  if found then
                     errno  := -20007;
                     errmsg := 'Identificazione "'||
                               :new.ID_LINK||
                               '" gia'' presente in LINKS. La registrazione  non puo'' essere inserita.';
                     raise integrity_error;
                  end if;
               end if;
            exception
               when MUTATING then null;  -- Ignora Check su UNIQUE PK Integrity
            end;
         end if;
      end;
      null;
   end;
   begin  -- Set FUNCTIONAL Integrity
      if IntegrityPackage.GetNestLevel = 0 then
         IntegrityPackage.NextNestLevel;
         BEGIN  -- GLOBAL FUNCTIONAL INTEGRITY AT LEVEL 0
            DECLARE
            CURSOR PARENT_LINKS(VAR_ID_OGGETTO NUMBER, VAR_TIPO_OGGETTO VARCHAR2) IS
               SELECT 1
                 FROM   CARTELLE
                WHERE  ID_CARTELLA = VAR_ID_OGGETTO AND VAR_TIPO_OGGETTO ='C'
            UNION
            SELECT 1
                 FROM   QUERY
                WHERE  ID_QUERY = VAR_ID_OGGETTO  AND VAR_TIPO_OGGETTO ='Q'
            UNION
            SELECT 1
                 FROM   DOCUMENTI
                WHERE  ID_DOCUMENTO = VAR_ID_OGGETTO  AND VAR_TIPO_OGGETTO ='D'
                UNION
            SELECT 1
                 FROM   COLLEGAMENTI_ESTERNI
                WHERE  ID_COLLEGAMENTO = VAR_ID_OGGETTO  AND VAR_TIPO_OGGETTO ='L'
            ;
            MUTATING         EXCEPTION;
            PRAGMA EXCEPTION_INIT(MUTATING, -4091);
            BEGIN
               IF :NEW.ID_LINK IS NOT NULL THEN
                  OPEN  PARENT_LINKS(:NEW.ID_OGGETTO,:NEW.TIPO_OGGETTO);
                  FETCH PARENT_LINKS INTO DUMMY;
                  FOUND := PARENT_LINKS%FOUND;
                  CLOSE PARENT_LINKS;
                  IF NOT FOUND THEN
                     ERRNO  := -20007;
                     ERRMSG := 'Identificazione OGGETTO "'||
                               :NEW.ID_OGGETTO||'e TIPO_OGGETTO '||:NEW.TIPO_OGGETTO||
                               '" non presente. La registrazione  non puo'' essere inserita.';
                     RAISE INTEGRITY_ERROR;
                  END IF;
               END IF;
            EXCEPTION
               WHEN MUTATING THEN NULL;  -- IGNORA CHECK SU UNIQUE PK INTEGRITY
            END;
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
/* END TRIGGER: LINKS_TIU */
/


