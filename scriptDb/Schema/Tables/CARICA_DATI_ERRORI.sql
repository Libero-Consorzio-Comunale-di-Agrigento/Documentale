CREATE TABLE CARICA_DATI_ERRORI
(
  IDDOC       NUMBER(10)                        NOT NULL,
  ID_TIPODOC  NUMBER(10)                        NOT NULL,
  ERRORE      VARCHAR2(4000 BYTE)
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


