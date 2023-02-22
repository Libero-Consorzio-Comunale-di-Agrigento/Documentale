CREATE TABLE AMV_DOCUMENTI_BLOB
(
  ID_DOCUMENTO  NUMBER(10)                      NOT NULL,
  REVISIONE     NUMBER(10)                      DEFAULT 0                     NOT NULL,
  ID_BLOB       NUMBER(10)                      NOT NULL
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


