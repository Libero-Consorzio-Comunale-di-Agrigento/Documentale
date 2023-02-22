CREATE TABLE RISORSE_VERSIONI_DOCU
(
  ID_DOCUMENTO  NUMBER(10)                      NOT NULL,
  VERSIONE      NUMBER(10)                      NOT NULL,
  NOMEFILE      VARCHAR2(255 BYTE)              NOT NULL,
  RISORSA       BLOB
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

