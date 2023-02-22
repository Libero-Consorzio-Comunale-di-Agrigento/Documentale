CREATE OR REPLACE TRIGGER SI4_COMPETENZE_TIU
/******************************************************************************
 NOME:        SI4_COMPETENZE_TIU
 DESCRIZIONE: Trigger for Check FUNCTIONAL Integrity
                            Set FUNCTIONAL Integrity
                       at INSERT or UPDATE or DELETE on Table SI4_COMPETENZE
 ECCEZIONI:  -20007, Identificazione CHIAVE presente in TABLE
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
    0 __/__/____ __
******************************************************************************/
   BEFORE INSERT OR UPDATE OR DELETE ON SI4_COMPETENZE
FOR EACH ROW
DECLARE
   functionalNestLevel INTEGER;
   integrity_error  EXCEPTION;
   errno            INTEGER;
   errmsg           CHAR(200);
   dummy            INTEGER;
   FOUND            BOOLEAN;
BEGIN
   BEGIN
      IF (:NEW.utente IS NULL OR :NEW.oggetto IS NULL) AND :NEW.id_funzione IS NULL AND NOT DELETING THEN
         RAISE_APPLICATION_ERROR(-20999,'Utente e/o Oggetto nullo e Funzione nulla.');
      END IF;
   END;
   functionalNestLevel := integritypackage.GetNestLevel;
   BEGIN  -- Check FUNCTIONAL Integrity
      --  Column "ID_COMPETENZA" uses sequence COMP_SQ
      IF :NEW.ID_COMPETENZA IS NULL AND NOT DELETING THEN
         SELECT COMP_SQ.NEXTVAL
           INTO :NEW.ID_COMPETENZA
           FROM dual;
      END IF;
      IF :NEW.id_funzione IS NOT NULL THEN
         :NEW.tipo_competenza := 'F';
      END IF;
      BEGIN  -- Check UNIQUE Integrity on PK of "SI4_COMPETENZE"
         IF integritypackage.GetNestLevel = 0 AND NOT DELETING THEN
            DECLARE
            CURSOR cpk_si4_competenze(var_ID_COMPETENZA NUMBER) IS
               SELECT 1
                 FROM   SI4_COMPETENZE
                WHERE  ID_COMPETENZA = var_ID_COMPETENZA;
            mutating         EXCEPTION;
            PRAGMA EXCEPTION_INIT(mutating, -4091);
            BEGIN
               IF :NEW.ID_COMPETENZA IS NOT NULL THEN
                  OPEN  cpk_si4_competenze(:NEW.ID_COMPETENZA);
                  FETCH cpk_si4_competenze INTO dummy;
                  FOUND := cpk_si4_competenze%FOUND;
                  CLOSE cpk_si4_competenze;
                  IF FOUND THEN
                     errno  := -20007;
                     errmsg := 'Identificazione "'||
                               :NEW.ID_COMPETENZA||
                               '" gia'' presente in Competenze. La registrazione  non puo'' essere inserita.';
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
      BEGIN  -- Full FUNCTIONAL Integrity at Any Level
      IF NOT DELETING THEN
         :NEW.data_aggiornamento := SYSDATE;
      END IF;
      END;
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
/* End Trigger: SI4_COMPETENZE_TIU */
/


