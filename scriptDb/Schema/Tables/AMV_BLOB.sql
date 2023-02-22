CREATE TABLE AMV_BLOB
(
  ID_BLOB    NUMBER(10)                         NOT NULL,
  NOME       VARCHAR2(100 BYTE),
  TIPO       VARCHAR2(10 BYTE),
  BLOB_FILE  BLOB
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

