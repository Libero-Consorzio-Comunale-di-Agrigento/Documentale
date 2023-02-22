CREATE TABLE QRTZ_SCHEDULER_STATE
(
  SCHED_NAME         VARCHAR2(120 BYTE)         NOT NULL,
  INSTANCE_NAME      VARCHAR2(200 BYTE)         NOT NULL,
  LAST_CHECKIN_TIME  NUMBER(13)                 NOT NULL,
  CHECKIN_INTERVAL   NUMBER(13)                 NOT NULL
)
TABLESPACE GDM
PCTUSED    0
PCTFREE    10
INITRANS   1
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MAXSIZE          UNLIMITED
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           );


