CREATE OR REPLACE TRIGGER AMV_ABILITAZIONI_TIU
/******************************************************************************
 NOME:        AMV_ABILITAZIONI_TIU
 DESCRIZIONE: Trigger for Check DATA Integrity
                          Check REFERENTIAL Integrity
                       at INSERT or UPDATE on Table AMV_ABILITAZIONI
 ECCEZIONI:   -20007, Identificazione CHIAVE presente in TABLE
 ANNOTAZIONI: Richiama Procedure AMV_ABILITAZIONI_PI e AMV_ABILITAZIONI_PU
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
                        Generato in automatico.
******************************************************************************/
   before INSERT or UPDATE on AMV_ABILITAZIONI
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
   begin  -- Check DATA Integrity on INSERT or UPDATE
       if NVL(:new.ABILITAZIONE,-1) != 0 then
          -- Assegna Padre = 0 solo se non si tratta di Abilitazione = 0
          :new.PADRE := nvl(:new.Padre,0);
       end if;
       :new.RUOLO := nvl(:new.RUOLO,'*');
   end;
   begin  -- Check REFERENTIAL Integrity on INSERT or UPDATE
         if IntegrityPackage.GetNestLevel = 0 then
            declare  --  Check UNIQUE Integrity per la tabella "AMV_ABILITAZIONI"
            cursor cpk_amv_abilitazioni(var_RUOLO varchar,
                            var_MODULO varchar,
                            var_VOCE_MENU varchar,
                            var_PADRE number) is
               select 1
                 from   AMV_ABILITAZIONI
                where  RUOLO = var_RUOLO and
                       MODULO = var_MODULO and
                       VOCE_MENU = var_VOCE_MENU and
                       PADRE = var_PADRE;
            mutating         exception;
            PRAGMA exception_init(mutating, -4091);
            begin  -- Check UNIQUE Integrity on "AMV_ABILITAZIONI"
               if :new.RUOLO is not null and
                  :new.MODULO is not null and
                  :new.VOCE_MENU is not null and
                  :new.PADRE is not null then
                  open  cpk_amv_abilitazioni(:new.RUOLO,
                                 :new.MODULO,
                                 :new.VOCE_MENU,
                                 :new.PADRE);
                  fetch cpk_amv_abilitazioni into dummy;
                  found := cpk_amv_abilitazioni%FOUND;
                  close cpk_amv_abilitazioni;
                  if found then
                     errno  := -20007;
                     errmsg := 'Identificazione "'||
                               :new.RUOLO||' '||
                               :new.MODULO||' '||
                               :new.VOCE_MENU||' '||
                               :new.PADRE||
                               '" gia'' presente in AMV Abilitazioni. La registrazione  non puo'' essere inserita.';
                     raise integrity_error;
                  end if;
               end if;
            exception
               when MUTATING then null;  -- Ignora Check su UNIQUE PK Integrity
            end;
         end if;
   end;
   begin  -- Set FUNCTIONAL Integrity on INSERT or UPDATE
       if INSERTING then
         if :new.ABILITAZIONE is NULL then
            --  Column "ABILITAZIONE" uses sequence AMV_ABIL_SEQ
            if :NEW.ABILITAZIONE IS NULL and not DELETING then
               select AMV_ABIL_SEQ.NEXTVAL
                 into :new.ABILITAZIONE
                 from dual;
            end if;
         end if;
         if IntegrityPackage.GetNestLevel = 0 then
         BEGIN -- Inserisce Padre 0 del Ruolo se non presente
           if :NEW.ABILITAZIONE <> 0 then
               begin
                  select 1
                  into dummy
                    from AMV_ABILITAZIONI
                   where abilitazione = 0
                     and ruolo = :NEW.RUOLO
                 ;
                exception
                 when NO_DATA_FOUND then
                    INSERT into AMV_ABILITAZIONI (ABILITAZIONE, RUOLO)
                     values(0, :NEW.RUOLO)
                     ;
            end;
          end if;
        END;
         BEGIN -- Inserimento della stessa Abilitazione per ruolo * se non esiste
          if :NEW.RUOLO <> '*' then
             begin
                   select 1  into dummy
                    from AMV_ABILITAZIONI
                   where abilitazione = :new.ABILITAZIONE
                     and ruolo = '*';
               exception
                   when NO_DATA_FOUND then
                         INSERT into AMV_ABILITAZIONI (ABILITAZIONE, RUOLO, MODULO, VOCE_MENU, PADRE, SEQUENZA, DISPOSITIVO)
                         values( :new.ABILITAZIONE, '*', :new.MODULO, :new.VOCE_MENU, :new.PADRE, :new.SEQUENZA, :new.DISPOSITIVO)
                      ;
               end;
          end if;
        END;
         end if;
      end if;
     if :NEW.ABILITAZIONE <> 0 then
         if :new.ABILITAZIONE = :new.PADRE then -- Controllo se Padre indicato uguale a figlio
            raise_application_error(-20999,'Nodo Padre non autorizzato');
         end if;
         if :new.MODULO is null then
             RAISE_APPLICATION_ERROR(-20999,'Il Modulo deve essere indicato');
         end if;
         if :new.VOCE_MENU is null then
             RAISE_APPLICATION_ERROR(-20999,'La voce di abilitazione deve essere indicata');
         end if;
      end if;
      if INSERTING then
         BEGIN -- Abilitazione di tutti i Figli della stessa Abilitazione
            a_messaggio := '';
            a_istruzione := 'insert into AMV_ABILITAZIONI (ABILITAZIONE, RUOLO, MODULO, VOCE_MENU, PADRE, SEQUENZA,DISPOSITIVO) '
                          ||'select  ABILITAZIONE,'''||:new.ruolo||''', MODULO, VOCE_MENU, PADRE, SEQUENZA,DISPOSITIVO '
                          ||'  from  AMV_ABILITAZIONI '
                          ||' where PADRE = '''||:new.ABILITAZIONE||''''
                          ||'   and RUOLO = ''*'''
                          ||'   and MODULO = '''||:new.MODULO||''''
                    ||'   and '''||:new.ruolo||''' != ''*''';
            IntegrityPackage.Set_PostEvent(a_istruzione, a_messaggio);
         END;
      end if;
     if IntegrityPackage.GetNestLevel = 0 then
         begin  -- Global FUNCTIONAL Integrity at Level 0
           if UPDATING then
              BEGIN -- Aggiornamento della stessa Abilitazione su altri ruoli
                 a_messaggio := '';
                 a_istruzione := 'update AMV_ABILITAZIONI '
                               ||'   set ABILITAZIONE = '''||:new.ABILITAZIONE||''''
                               ||'     , MODULO       = '''||:new.MODULO||''''
                               ||'     , VOCE_MENU    = '''||:new.VOCE_MENU||''''
                               ||'     , PADRE        = '''||:new.PADRE||''''
                               ||'     , SEQUENZA     = '''||:new.SEQUENZA||''''
                               ||' where ABILITAZIONE = '''||:old.ABILITAZIONE||''''
                               ||'   and RUOLO       != '''||:old.RUOLO||'''';
                 IntegrityPackage.Set_PostEvent(a_istruzione, a_messaggio);
              END;
           end if;
         if :NEW.Padre <> '0' then
              BEGIN -- Controllo se Voce di Tipo = M in caso di riferimento come Padre
                 a_messaggio := 'Voce indicata come Padre deve essere di Tipo ''Menu''.';
                 a_istruzione := 'select 1 from AMV_VOCI V, AMV_ABILITAZIONI A'
                               ||' where V.TIPO_VOCE    = ''N'''
                        ||'   and V.VOCE         = A.VOCE_MENU '
                               ||'   and A.ABILITAZIONE = '''||:new.PADRE||''''
                               ||'   and A.RUOLO        = '''||:new.RUOLO||'''';
                 IntegrityPackage.Set_PostEvent(a_istruzione, a_messaggio);
               END;
         end if;
         end;
      end if;
   end;
exception
   when integrity_error then
        IntegrityPackage.InitNestLevel;
        raise_application_error(errno, errmsg);
   when others then
        IntegrityPackage.InitNestLevel;
        raise;
end;
/* End Trigger: AMV_ABILITAZIONI_TIU */
/


