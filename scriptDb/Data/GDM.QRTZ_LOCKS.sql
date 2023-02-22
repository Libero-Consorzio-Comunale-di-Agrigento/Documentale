Insert into QRTZ_LOCKS
   (SCHED_NAME, LOCK_NAME)
 Values
   ('JDMS-Scheduler', 'STATE_ACCESS');
Insert into QRTZ_LOCKS
   (SCHED_NAME, LOCK_NAME)
 Values
   ('JDMS-Scheduler', 'TRIGGER_ACCESS');
COMMIT;
