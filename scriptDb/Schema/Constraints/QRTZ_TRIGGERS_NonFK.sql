ALTER TABLE QRTZ_TRIGGERS ADD (
  CONSTRAINT QRTZ_TRIGGERS_PK
  PRIMARY KEY
  (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
  USING INDEX QRTZ_TRIGGERS_PK
  ENABLE VALIDATE);
