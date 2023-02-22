CREATE TABLE DATABASECHANGELOGLOCK
(
  ID           INTEGER                          NOT NULL,
  LOCKED       NUMBER(1)                        NOT NULL,
  LOCKGRANTED  TIMESTAMP(6),
  LOCKEDBY     VARCHAR2(255 BYTE)
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


