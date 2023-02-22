ALTER TABLE QRTZ_TRIGGERS ADD (
  CONSTRAINT QRTZ_TRIGGER_TO_JOBS_FK 
  FOREIGN KEY (SCHED_NAME, JOB_NAME, JOB_GROUP) 
  REFERENCES QRTZ_JOB_DETAILS (SCHED_NAME,JOB_NAME,JOB_GROUP)
  ENABLE VALIDATE);
