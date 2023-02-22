DECLARE
  X NUMBER;
BEGIN
    SYS.DBMS_JOB.SUBMIT
    ( job       => X 
     ,what      => 'begin
RT_REBUILD_VAL_CLOB_INDEX;
end;'
     ,next_date => SYSDATE+1/1440
     ,interval  => 'SYSDATE+1/1440'
     ,no_parse  => TRUE
    );
  COMMIT;
END;
/



