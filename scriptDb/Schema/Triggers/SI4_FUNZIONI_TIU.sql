CREATE OR REPLACE TRIGGER SI4_FUNZIONI_TIU
/******************************************************************************
 NOME:        SI4_FUNZIONI_TIU
 DESCRIZIONE: Trigger for Check FUNCTIONAL Integrity
                            Set FUNCTIONAL Integrity
                       at INSERT or UPDATE or DELETE on Table SI4_FUNZIONI
 ECCEZIONI:  -20007, Identificazione CHIAVE presente in TABLE
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
    0 __/__/____ __
******************************************************************************/
   BEFORE INSERT OR UPDATE OR DELETE ON SI4_FUNZIONI
FOR EACH ROW
DECLARE
   functionalNestLevel INTEGER;
   integrity_error  EXCEPTION;
   errno            INTEGER;
   errmsg           CHAR(200);
   dummy            INTEGER;
   FOUND            BOOLEAN;
BEGIN
   functionalNestLevel := integritypackage.GetNestLevel;
   BEGIN  -- Check FUNCTIONAL Integrity
      --  Column "ID_COMPETENZA" uses sequence COMP_SQ
      IF :NEW.ID_FUNZIONE IS NULL AND NOT DELETING THEN
         SELECT FUNZ_SQ.NEXTVAL
           INTO :NEW.ID_FUNZIONE
           FROM dual;
      END IF;
      BEGIN  -- Check UNIQUE Integrity on PK of "SI4_COMPETENZE"
         IF integritypackage.GetNestLevel = 0 AND NOT DELETING THEN
            DECLARE
            CURSOR cpk_si4_funzione(var_ID_FUNZIONE NUMBER) IS
               SELECT 1
                 FROM   SI4_FUNZIONI
                WHERE  ID_FUNZIONE = var_ID_FUNZIONE;
            mutating         EXCEPTION;
            PRAGMA EXCEPTION_INIT(mutating, -4091);
            BEGIN
               IF :NEW.ID_FUNZIONE IS NOT NULL THEN
                  OPEN  cpk_si4_funzione(:NEW.ID_FUNZIONE);
                  FETCH cpk_si4_funzione INTO dummy;
                  FOUND := cpk_si4_funzione%FOUND;
                  CLOSE cpk_si4_funzione;
                  IF FOUND THEN
                     errno  := -20007;
                     errmsg := 'Identificazione "'||
                               :NEW.ID_FUNZIONE||
                               '" gia'' presente in Funzioni. La registrazione  non puo'' essere inserita.';
                     RAISE integrity_error;
                  END IF;
               END IF;
            EXCEPTION
               WHEN MUTATING THEN NULL;  -- Ignora Check su UNIQUE PK Integrity
            END;
         END IF;
      END;
      NULL;
   END;
   BEGIN  -- Set FUNCTIONAL Integrity
      IF functionalNestLevel = 0 THEN
         integritypackage.NextNestLevel;
         BEGIN  -- Global FUNCTIONAL Integrity at Level 0
            /* NONE */ NULL;
         END;
        integritypackage.PreviousNestLevel;
      END IF;
      integritypackage.NextNestLevel;
      integritypackage.PreviousNestLevel;
   END;
EXCEPTION
   WHEN integrity_error THEN
        integritypackage.InitNestLevel;
        RAISE_APPLICATION_ERROR(errno, errmsg);
   WHEN OTHERS THEN
        integritypackage.InitNestLevel;
        RAISE;
END;
/* End Trigger: SI4_FUNZIONI_TIU */
/


