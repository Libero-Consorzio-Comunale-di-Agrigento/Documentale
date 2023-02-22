CREATE OR REPLACE TRIGGER AMV_BLOB_TIU
/******************************************************************************
 NOME:        AMV_BLOB_TIU
 DESCRIZIONE: Trigger for Check FUNCTIONAL Integrity
                            Set FUNCTIONAL Integrity
                       at INSERT or UPDATE or DELETE on Table AMV_BLOB
 ECCEZIONI:  -20007, Identificazione CHIAVE presente in TABLE
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
    0 __/__/____ __
******************************************************************************/
   before INSERT or UPDATE or DELETE on AMV_BLOB
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
      --  Column "ID_BLOB" uses sequence AMV_BLOB_SEQ
      if :NEW.ID_BLOB IS NULL and not DELETING then
         select AMV_BLOB_SEQ.NEXTVAL
           into :new.ID_BLOB
           from dual;
      end if;
      begin  -- Check UNIQUE Integrity on PK of "AMV_BLOB"
         if IntegrityPackage.GetNestLevel = 0 and not DELETING then
            declare
            cursor cpk_amv_blob(var_ID_BLOB number) is
               select 1
                 from   AMV_BLOB
                where  ID_BLOB = var_ID_BLOB;
            mutating         exception;
            PRAGMA exception_init(mutating, -4091);
            begin
               if :new.ID_BLOB is not null then
                  open  cpk_amv_blob(:new.ID_BLOB);
                  fetch cpk_amv_blob into dummy;
                  found := cpk_amv_blob%FOUND;
                  close cpk_amv_blob;
                  if found then
                     errno  := -20007;
                     errmsg := 'Identificazione "'||
                               :new.ID_BLOB||
                               '" gia'' presente in AMV_BLOB. La registrazione  non puo'' essere inserita.';
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
/* End Trigger: AMV_BLOB_TIU */
/


