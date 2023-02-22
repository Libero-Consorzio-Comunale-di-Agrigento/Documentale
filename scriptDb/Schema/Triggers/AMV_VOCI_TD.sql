CREATE OR REPLACE TRIGGER AMV_VOCI_TD
/******************************************************************************
 NOME:        AMV_VOCI_TD
 DESCRIZIONE: Trigger for Set FUNCTIONAL Integrity
                        Check REFERENTIAL Integrity
                       at DELETE on Table AMV_VOCI
 ANNOTAZIONI: Richiama Procedure AMV_VOCI_TD
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
                        Generato in automatico.
******************************************************************************/
   before DELETE on AMV_VOCI
for each row
declare
   integrity_error  exception;
   errno            integer;
   errmsg           char(200);
   dummy            integer;
   found            boolean;
   d_abilitazione  number(8);
   d_guida         number(8) := 0;
begin
   begin -- Set FUNCTIONAL Integrity on DELETE
      if IntegrityPackage.GetNestLevel = 0 then
         IntegrityPackage.NextNestLevel;
         begin  -- Global FUNCTIONAL Integrity at Level 0
            /* NONE */ null;
         end;
         IntegrityPackage.PreviousNestLevel;
      end if;
      BEGIN -- Ricerca ed eliminazione della abilitazione principale
            SELECT ABILITAZIONE INTO d_abilitazione
              FROM AMV_ABILITAZIONI
             WHERE VOCE_MENU = :OLD.VOCE
               AND RUOLO = '*'
            ;
            DELETE FROM AMV_ABILITAZIONI
             WHERE ABILITAZIONE = d_abilitazione
           ;
      EXCEPTION
         WHEN OTHERS THEN NULL;
      END;
      BEGIN -- Ricerca ed eliminazione delle guide della voce
            SELECT count(GUIDA) INTO d_guida
              FROM AMV_GUIDE
             WHERE GUIDA = :OLD.VOCE
            ;
         if d_guida > 0 then
               DELETE FROM AMV_GUIDE
                WHERE GUIDA = :OLD.VOCE
              ;
         end if;
      EXCEPTION
         WHEN OTHERS THEN NULL;
      END;
   end;
   begin  -- Check REFERENTIAL Integrity on DELETE
      AMV_VOCI_PD(:OLD.VOCE);
   end;
exception
   when integrity_error then
        IntegrityPackage.InitNestLevel;
        raise_application_error(errno, errmsg);
   when others then
        IntegrityPackage.InitNestLevel;
        raise;
end;
/* End Trigger: AMV_VOCI_TD */
/


