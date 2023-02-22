CREATE OR REPLACE TRIGGER AMV_DOCUMENTI_TIU
/******************************************************************************
 NOME:        AMV_DOCUMENTI_TIU
 DESCRIZIONE: Trigger for Check DATA Integrity
                          Check REFERENTIAL Integrity
                       at INSERT or UPDATE on Table AMV_DOCUMENTI
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
    0 09/10/2002 MF     Creazione.
    1 12/10/2004 MF     Uso di si4.next_id
******************************************************************************/
   before INSERT or UPDATE on AMV_DOCUMENTI
for each row
declare
   integrity_error  exception;
   errno            integer;
   errmsg           char(200);
   dummy            integer;
   found            boolean;
begin
   begin  -- Check DATA Integrity on INSERT or UPDATE
      :NEW.DATA_AGGIORNAMENTO := SYSDATE;
      --  Column "ID_DOCUMENTO" attribuisce MAX+1 con la si4.next_id
      if :NEW.ID_DOCUMENTO IS NULL THEN
         :NEW.ID_DOCUMENTO := si4.next_id('AMV_DOCUMENTI','ID_DOCUMENTO');
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
/* End Trigger: AMV_DOCUMENTI_TIU */
/


