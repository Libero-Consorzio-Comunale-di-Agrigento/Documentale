ALTER TABLE QRTZ_BLOB_TRIGGERS ADD (
  CONSTRAINT QRTZ_BLOB_TRIG_PK
  PRIMARY KEY
  (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
  USING INDEX QRTZ_BLOB_TRIG_PK
  ENABLE VALIDATE);
