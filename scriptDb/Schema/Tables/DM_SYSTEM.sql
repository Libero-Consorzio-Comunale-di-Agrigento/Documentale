CREATE TABLE DM_SYSTEM
(
  ID_DM                 NUMBER(10)              NOT NULL,
  NOME                  VARCHAR2(100 BYTE)      NOT NULL,
  LIBRARY               VARCHAR2(20 BYTE),
  UTENTE                VARCHAR2(8 BYTE),
  PASSWD                VARCHAR2(40 BYTE),
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


