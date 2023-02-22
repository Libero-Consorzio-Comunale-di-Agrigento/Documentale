CREATE UNIQUE INDEX QRTZ_BLOB_TRIG_PK ON QRTZ_BLOB_TRIGGERS
(SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
TABLESPACE GDM
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            MAXSIZE          UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           );

