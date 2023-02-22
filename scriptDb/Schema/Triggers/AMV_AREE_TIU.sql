CREATE OR REPLACE TRIGGER AMV_AREE_TIU
/******************************************************************************
 NOME:        AMV_AREE_TIU
 DESCRIZIONE: Trigger for Check DATA Integrity
                          Check REFERENTIAL Integrity
                       at INSERT or UPDATE on Table AMV_AREE
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
    0 09/10/2002 MF     Creazione.
******************************************************************************/
   before INSERT or UPDATE on AMV_AREE
for each row
declare
   integrity_error  exception;
   errno            integer;
   errmsg           char(200);
   dummy            integer;
   found            boolean;
begin
   begin  -- Check DATA Integrity on INSERT or UPDATE
      --  Column "ID_AREA" attribuisce MAX+1
      if :NEW.ID_AREA IS NULL and not DELETING then
         select nvl(max(ID_AREA),0) + 1
           into :NEW.ID_AREA
           from AMV_AREE;
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
/* End Trigger: AMV_AREE_TIU */
/


