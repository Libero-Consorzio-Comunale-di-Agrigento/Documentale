CREATE TABLE OGGETTI_FILE_CHECK
(
  ID_OGGETTO_FILE     NUMBER(10)                NOT NULL,
  UTENTE              VARCHAR2(8 BYTE)          NOT NULL,
  NUM_CHECK           NUMBER(10)                NOT NULL,
  DATA_AGGIORNAMENTO  DATE                      NOT NULL
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


