CREATE OR REPLACE TRIGGER SI4_TIPI_OGGETTO_TIU
/******************************************************************************
 NOME:        SI4_TIPI_OGGETTO_TIU
 DESCRIZIONE: Trigger for Check FUNCTIONAL Integrity
                            Set FUNCTIONAL Integrity
                       at INSERT or UPDATE or DELETE on Table SI4_TIPI_OGGETTO
 ECCEZIONI:  -20007, Identificazione CHIAVE presente in TABLE
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
    0 __/__/____ __
******************************************************************************/
   before INSERT or UPDATE or DELETE on SI4_TIPI_OGGETTO
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
      --  Column "ID_TIPO_OGGETTO" uses sequence TIOG_SQ
      if :NEW.ID_TIPO_OGGETTO IS NULL and not DELETING then
         select TIOG_SQ.NEXTVAL
           into :new.ID_TIPO_OGGETTO
           from dual;
      end if;
      begin  -- Check UNIQUE Integrity on PK of "SI4_TIPI_OGGETTO"
         if IntegrityPackage.GetNestLevel = 0 and not DELETING then
            declare
            cursor cpk_si4_tipi_oggetto(var_ID_TIPO_OGGETTO number) is
               select 1
                 from   SI4_TIPI_OGGETTO
                where  ID_TIPO_OGGETTO = var_ID_TIPO_OGGETTO;
            mutating         exception;
            PRAGMA exception_init(mutating, -4091);
            begin
               if :new.ID_TIPO_OGGETTO is not null then
                  open  cpk_si4_tipi_oggetto(:new.ID_TIPO_OGGETTO);
                  fetch cpk_si4_tipi_oggetto into dummy;
                  found := cpk_si4_tipi_oggetto%FOUND;
                  close cpk_si4_tipi_oggetto;
                  if found then
                     errno  := -20007;
                     errmsg := 'Identificazione "'||
                               :new.ID_TIPO_OGGETTO||
                               '" gia'' presente in Tipi Oggetto. La registrazione  non puo'' essere inserita.';
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
/* End Trigger: SI4_TIPI_OGGETTO_TIU */
/


