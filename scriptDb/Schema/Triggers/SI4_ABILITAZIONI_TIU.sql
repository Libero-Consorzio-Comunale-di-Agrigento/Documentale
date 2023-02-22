CREATE OR REPLACE TRIGGER SI4_ABILITAZIONI_TIU
/******************************************************************************
 NOME:        SI4_ABILITAZIONI_TIU
 DESCRIZIONE: Trigger for Check FUNCTIONAL Integrity
                            Set FUNCTIONAL Integrity
                       at INSERT or UPDATE or DELETE on Table SI4_ABILITAZIONI
 ECCEZIONI:  -20007, Identificazione CHIAVE presente in TABLE
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
    0 __/__/____ __
******************************************************************************/
   before INSERT or UPDATE or DELETE on SI4_ABILITAZIONI
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
      --  Column "ID_ABILITAZIONE" uses sequence ABIL_SQ
      if :NEW.ID_ABILITAZIONE IS NULL and not DELETING then
         select ABIL_SQ.NEXTVAL
           into :new.ID_ABILITAZIONE
           from dual;
      end if;
      begin  -- Check UNIQUE Integrity on PK of "SI4_ABILITAZIONI"
         if IntegrityPackage.GetNestLevel = 0 and not DELETING then
            declare
            cursor cpk_si4_abilitazioni(var_ID_ABILITAZIONE number) is
               select 1
                 from   SI4_ABILITAZIONI
                where  ID_ABILITAZIONE = var_ID_ABILITAZIONE;
            mutating         exception;
            PRAGMA exception_init(mutating, -4091);
            begin
               if :new.ID_ABILITAZIONE is not null then
                  open  cpk_si4_abilitazioni(:new.ID_ABILITAZIONE);
                  fetch cpk_si4_abilitazioni into dummy;
                  found := cpk_si4_abilitazioni%FOUND;
                  close cpk_si4_abilitazioni;
                  if found then
                     errno  := -20007;
                     errmsg := 'Identificazione "'||
                               :new.ID_ABILITAZIONE||
                               '" gia'' presente in Abilitazioni. La registrazione  non puo'' essere inserita.';
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
/* End Trigger: SI4_ABILITAZIONI_TIU */
/


