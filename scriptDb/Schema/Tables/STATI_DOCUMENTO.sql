CREATE TABLE STATI_DOCUMENTO
(
  ID_STATO              NUMBER(10)              NOT NULL,
  ID_DOCUMENTO          NUMBER(10)              NOT NULL,
  STATO                 VARCHAR2(2 BYTE)        NOT NULL,
  COMMENTO              VARCHAR2(2000 BYTE),
  DATA_AGGIORNAMENTO    DATE                    NOT NULL,
  UTENTE_AGGIORNAMENTO  VARCHAR2(8 BYTE)        NOT NULL
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


