CREATE OR REPLACE TRIGGER AMV_SEZIONI_TIU
/******************************************************************************
 NOME:        AMV_SEZIONI_TIU
 DESCRIZIONE: Trigger for Check FUNCTIONAL Integrity
                            Set FUNCTIONAL Integrity
                       at INSERT or UPDATE or DELETE on Table AMV_SEZIONI
 ECCEZIONI:  -20007, Identificazione CHIAVE presente in TABLE
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
    0 __/__/____ __
******************************************************************************/
   before INSERT or UPDATE or DELETE on AMV_SEZIONI
for each row
declare
   functionalNestLevel integer;
   integrity_error  exception;
   errno            integer;
   errmsg           char(200);
   dummy            integer;
   found            boolean;
begin
   functionalNestLevel := IntegrityPackage.GetNestLevel;
   begin  -- Check FUNCTIONAL Integrity
      --  Column "ID_SEZIONE" attribuisce MAX+1
      if :NEW.ID_SEZIONE IS NULL and not DELETING then
         select nvl(max(ID_SEZIONE),0) + 1
           into :NEW.ID_SEZIONE
           from AMV_SEZIONI;
      end if;
      null;
   end;
   begin  -- Set FUNCTIONAL Integrity
      if functionalNestLevel = 0 then
         IntegrityPackage.NextNestLevel;
         begin  -- Global FUNCTIONAL Integrity at Level 0
            /* NONE */ null;
         end;
        IntegrityPackage.PreviousNestLevel;
      end if;
      IntegrityPackage.NextNestLevel;
      begin  -- Full FUNCTIONAL Integrity at Any Level
         /* NONE */ null;
      end;
      IntegrityPackage.PreviousNestLevel;
   end;
exception
   when integrity_error then
        IntegrityPackage.InitNestLevel;
        raise_application_error(errno, errmsg);
   when others then
        IntegrityPackage.InitNestLevel;
        raise;
end;
/* End Trigger: AMV_SEZIONI_TIU */
/


