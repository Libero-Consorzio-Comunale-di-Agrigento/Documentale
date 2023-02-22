CREATE OR REPLACE TRIGGER AMV_CLASSIFICAZIONI_TIU
/******************************************************************************
 NOME:        AMV_CLASSIFICAZIONI_TIU
 DESCRIZIONE: Trigger for Check DATA Integrity
                          Check REFERENTIAL Integrity
                       at INSERT or UPDATE on Table AMV_CLASSIFICAZIONI
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
    0 09/10/2002 MF     Creazione.
******************************************************************************/
   before INSERT or UPDATE on AMV_CLASSIFICAZIONI
for each row
declare
   integrity_error  exception;
   errno            integer;
   errmsg           char(200);
   dummy            integer;
   found            boolean;
begin
   begin  -- Check DATA Integrity on INSERT or UPDATE
      --  Column "ID_CLASSIFICAZIONE" attribuisce MAX+1
      if :NEW.ID_CLASSIFICAZIONE IS NULL and not DELETING then
         select nvl(max(ID_CLASSIFICAZIONE),0) + 1
           into :NEW.ID_CLASSIFICAZIONE
           from AMV_CLASSIFICAZIONI;
      end if;
      /* NONE */ null;
   end;
   begin  -- Check REFERENTIAL Integrity on INSERT or UPDATE
      /* NONE */ null;
   end;
exception
   when integrity_error then
        IntegrityPackage.InitNestLevel;
        raise_application_error(errno, errmsg);
   when others then
        IntegrityPackage.InitNestLevel;
        raise;
end;
/* End Trigger: AMV_CLASSIFICAZIONI_TIU */
/


