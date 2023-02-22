ALTER TABLE QRTZ_BLOB_TRIGGERS ADD (
  CONSTRAINT QRTZ_BLOB_TRIG_TO_TRIG_FK 
  FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP) 
  REFERENCES QRTZ_TRIGGERS (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
  ENABLE VALIDATE);

