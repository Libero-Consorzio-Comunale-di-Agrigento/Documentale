CREATE OR REPLACE TRIGGER AREE_PATH_BU
BEFORE UPDATE
ON AREE_PATH
REFERENCING NEW AS New OLD AS Old
FOR EACH ROW
DECLARE
a_conta NUMBER;
BEGIN
 Select count(*)
 into a_conta
 from dba_directories
 where DIRECTORY_NAME like 'DIR_%_'||:new.PREFIX_ACRONIMO_DIRECTORY   and DIRECTORY_PATH <> :new.PATH_FILE_ORACLE;

 if a_conta>0 then
    raise_application_error(-20999,'Attenzione! Esiste gi un percorso utilizzato per questo acronimo. Non  possibile cambiarlo');
 end if;

EXCEPTION
 WHEN OTHERS THEN
   RAISE;
END AREE_PATH_BU;
/


