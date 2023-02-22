CREATE INDEX IDX_QRTZ_T_STATE ON QRTZ_TRIGGERS
(SCHED_NAME, TRIGGER_STATE)
TABLESPACE GDM
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            MAXSIZE          UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           );

