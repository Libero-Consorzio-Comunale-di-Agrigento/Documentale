ALTER TABLE QRTZ_FIRED_TRIGGERS ADD (
  CONSTRAINT QRTZ_FIRED_TRIGGER_PK
  PRIMARY KEY
  (SCHED_NAME, ENTRY_ID)
  USING INDEX QRTZ_FIRED_TRIGGER_PK
  ENABLE VALIDATE);

