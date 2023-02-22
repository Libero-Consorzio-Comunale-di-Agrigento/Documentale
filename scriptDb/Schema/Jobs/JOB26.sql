DECLARE
  X NUMBER;
BEGIN
    SYS.DBMS_JOB.SUBMIT
    ( job       => X 
     ,what      => 'BEGIN
DBMS_STATS.GATHER_SCHEMA_STATS(''GDM'',DBMS_STATS.AUTO_SAMPLE_SIZE,FALSE,''FOR ALL COLUMNS SIZE AUTO'', NULL, ''DEFAULT'', TRUE);
END;'
     ,next_date => TRUNC(SYSDATE+7)
     ,interval  => 'TRUNC(SYSDATE+7)'
     ,no_parse  => TRUE
    );
  COMMIT;
END;
/



