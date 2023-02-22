CREATE OR REPLACE TRIGGER AMV_VOCI_TIU
/******************************************************************************
 NOME:        AMV_VOCI_TIU
 DESCRIZIONE: Trigger for Set DATA Integrity
                          Set FUNCTIONAL Integrity
                       on Table AMV_VOCI
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
  1   13/02/2003   AO     Gestione Nodi di tipo Altro
******************************************************************************/
   before INSERT or UPDATE on AMV_VOCI
for each row
declare
   integrity_error  exception;
   errno            integer;
   errmsg           char(200);
   found            boolean;
begin
   begin
      BEGIN
        if :new.TIPO_VOCE is null then
            if :new.TIPO = 'M' then
               :new.TIPO_VOCE := 'N';
            elsif :new.TIPO = 'V' then
            :new.TIPO_VOCE := 'N';
            :new.TIPO := 'A';
         else
               :new.TIPO_VOCE := 'F';
            end if;
         end if;
      END;
      BEGIN
        if :new.TIPO_VOCE = 'N' then
            if :new.TIPO != 'M' and :new.TIPO != 'A' then
               raise_application_error(-20999,'Tipo non ammesso su Tipo Voce ''Nodo''.');
            end if;
         end if;
      END;
     BEGIN
        if :new.TIPO_VOCE = 'F' then
          DECLARE
            d_dummy number;
         BEGIN
            select max(1)
              into d_dummy
              from amv_abilitazioni
             where padre in
                 (select abilitazione
                  from amv_abilitazioni
                  where voce_menu = :old.Voce
                )
            ;
            if d_dummy = 1 then
                  raise_application_error(-20999,'La voce è un nodo di menu con foglie gia'' abilitate.');
            end if;
         END;
         end if;
      END;
   end;
   begin
      if IntegrityPackage.GetNestLevel = 0 then
         IntegrityPackage.NextNestLevel;
         begin
          null;
         end;
         IntegrityPackage.PreviousNestLevel;
      end if;
      IntegrityPackage.NextNestLevel;
      begin
        null;
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
/* End Trigger: AMV_VOCI_TIU */
/


