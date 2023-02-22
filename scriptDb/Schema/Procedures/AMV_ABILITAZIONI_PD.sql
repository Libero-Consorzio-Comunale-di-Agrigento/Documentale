CREATE OR REPLACE procedure AMV_ABILITAZIONI_PD
/******************************************************************************
 NOME:        AMV_ABILITAZIONI_PD
 DESCRIZIONE: Procedure for Check REFERENTIAL Integrity
                         at DELETE on Table AMV_ABILITAZIONI
 ARGOMENTI:   Rigenerati in automatico.
 ECCEZIONI:  -20006, Esistono riferimenti su CHILD TABLE
 ANNOTAZIONI: Richiamata da Trigger AMV_ABILITAZIONI_TD
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
                        Generata in automatico.
******************************************************************************/
(old_abilitazione IN number,
 old_ruolo IN varchar)
is
   integrity_error  exception;
   errno            integer;
   errmsg           char(200);
   dummy            integer;
   found            boolean;
begin
   begin  -- Check REFERENTIAL Integrity
      null;
   end;
exception
   when integrity_error then
        IntegrityPackage.InitNestLevel;
        raise_application_error(errno, errmsg);
   when others then
        IntegrityPackage.InitNestLevel;
        raise;
end;
/* End Procedure: AMV_ABILITAZIONI_PD */
/

