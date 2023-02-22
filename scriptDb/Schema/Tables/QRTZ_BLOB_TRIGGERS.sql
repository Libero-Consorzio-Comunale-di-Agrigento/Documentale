CREATE TABLE QRTZ_BLOB_TRIGGERS
(
  SCHED_NAME     VARCHAR2(120 BYTE)             NOT NULL,
  TRIGGER_NAME   VARCHAR2(200 BYTE)             NOT NULL,
  TRIGGER_GROUP  VARCHAR2(200 BYTE)             NOT NULL,
  BLOB_DATA      BLOB
)
TABLESPACE GDM
PCTUSED    0
PCTFREE    10
INITRANS   1
MAXTRANS   255
STORAGE    (
            MAXSIZE          UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           );

