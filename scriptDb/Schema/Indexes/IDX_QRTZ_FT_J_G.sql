CREATE INDEX IDX_QRTZ_FT_J_G ON QRTZ_FIRED_TRIGGERS
(SCHED_NAME, JOB_NAME, JOB_GROUP)
TABLESPACE GDM
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            MAXSIZE          UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           );


