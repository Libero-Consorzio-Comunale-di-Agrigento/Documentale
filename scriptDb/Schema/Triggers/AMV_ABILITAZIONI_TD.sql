CREATE OR REPLACE TRIGGER AMV_ABILITAZIONI_TD
/******************************************************************************
 NOME:        AMV_ABILITAZIONI_TD
 DESCRIZIONE: Trigger for Set FUNCTIONAL Integrity
                        Check REFERENTIAL Integrity
                       at DELETE on Table AMV_ABILITAZIONI
 ANNOTAZIONI: Richiama Procedure AMV_ABILITAZIONI_TD
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
                        Generato in automatico.
******************************************************************************/
   before DELETE on AMV_ABILITAZIONI
for each row
declare
   integrity_error  exception;
   errno            integer;
   errmsg           char(200);
   dummy            integer;
   found            boolean;
   a_messaggio      varchar2(2000);
   a_istruzione     varchar2(2000);
begin
   begin -- Set FUNCTIONAL Integrity on DELETE
      if IntegrityPackage.GetNestLevel = 0 then
         IntegrityPackage.NextNestLevel;
         begin  -- Global FUNCTIONAL Integrity at Level 0
            /* NONE */ null;
         end;
         IntegrityPackage.PreviousNestLevel;
      end if;
   end;
   begin  -- Check REFERENTIAL Integrity on DELETE
      AMV_ABILITAZIONI_PD(:OLD.ABILITAZIONE,
                    :OLD.RUOLO);
   end;
   BEGIN -- Elimina abilitazioni di tutti i ruoli in caso di eliminazione ruolo principale
      if :OLD.RUOLO = '*' then
        a_messaggio := 'Errore in eliminazione voci di menu figlie.';
       a_istruzione := 'delete AMV_ABILITAZIONI where abilitazione = '''||:OLD.ABILITAZIONE||'''';
         IntegrityPackage.Set_PostEvent(a_istruzione, a_messaggio);
      end if;
   END;
   BEGIN -- Elimina Livello zero di Ruolo eliminato
     a_messaggio := 'Errore in eliminazione livello zero.';
      a_istruzione := 'delete AMV_ABILITAZIONI where abilitazione = 0 and ruolo = '''||:OLD.ruolo||''''
                    ||' and not exists '
                    ||'   (select 1 '
                    ||'      from AMV_ABILITAZIONI '
                    ||'     where abilitazione != 0 '
                    ||'       and ruolo = '''||:OLD.ruolo||''')';
      IntegrityPackage.Set_PostEvent(a_istruzione, a_messaggio);
   END;
exception
   when integrity_error then
        IntegrityPackage.InitNestLevel;
        raise_application_error(errno, errmsg);
   when others then
        IntegrityPackage.InitNestLevel;
        raise;
end;
/* End Trigger: AMV_ABILITAZIONI_TD */
/


