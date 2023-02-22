CREATE INDEX IDX_QRTZ_T_NFT_ST ON QRTZ_TRIGGERS
(SCHED_NAME, TRIGGER_STATE, NEXT_FIRE_TIME)
TABLESPACE GDM
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            MAXSIZE          UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           );

