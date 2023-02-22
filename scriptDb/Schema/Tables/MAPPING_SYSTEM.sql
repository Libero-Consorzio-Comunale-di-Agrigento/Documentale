CREATE TABLE MAPPING_SYSTEM
(
  ID_MAPPING_SYSTEM     NUMBER(10)              NOT NULL,
  ID_ENTE               NUMBER(10)              NOT NULL,
  ID_APPLICATIVO        NUMBER(10)              NOT NULL,
  ID_DM                 NUMBER(10)              NOT NULL,
  REVISIONE             NUMBER(8)               NOT NULL,
  DATA_AGGIORNAMENTO    DATE                    NOT NULL,
  UTENTE_AGGIORNAMENTO  VARCHAR2(8 BYTE)        NOT NULL
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


