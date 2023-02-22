DECLARE
  X NUMBER;
BEGIN
    SYS.DBMS_JOB.SUBMIT
    ( job       => X 
     ,what      => 'begin
RT_REBUILD_OGFI_OCR_INDEX;
end;'
     ,next_date => SYSDATE+1/144
     ,interval  => 'SYSDATE+1/144'
     ,no_parse  => TRUE
    );
  COMMIT;
END;
/



