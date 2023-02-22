DECLARE
  X NUMBER;
BEGIN
    SYS.DBMS_JOB.SUBMIT
    ( job       => X 
     ,what      => 'begin
P_PULISCI_ALLEGATI_TEMP;
end;'
     ,next_date => SYSDATE+1
     ,interval  => 'SYSDATE+1'
     ,no_parse  => TRUE
    );
  COMMIT;
END;
/



