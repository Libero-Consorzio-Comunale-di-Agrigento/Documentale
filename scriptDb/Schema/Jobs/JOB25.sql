DECLARE
  X NUMBER;
BEGIN
    SYS.DBMS_JOB.SUBMIT
    ( job       => X 
     ,what      => 'begin
RT_REBUILD_OGFI_INDEX;
end;'
     ,next_date => SYSDATE+1/24
     ,interval  => 'SYSDATE+1/24'
     ,no_parse  => TRUE
    );
  COMMIT;
END;
/



