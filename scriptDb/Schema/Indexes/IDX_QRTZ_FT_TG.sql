CREATE INDEX IDX_QRTZ_FT_TG ON QRTZ_FIRED_TRIGGERS
(SCHED_NAME, TRIGGER_GROUP)
TABLESPACE GDM
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            MAXSIZE          UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           );


