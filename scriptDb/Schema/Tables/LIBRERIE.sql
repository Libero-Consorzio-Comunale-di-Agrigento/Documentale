CREATE TABLE LIBRERIE
(
  ID_LIBRERIA           NUMBER(10)              NOT NULL,
  LIBRERIA              VARCHAR2(100 BYTE)      NOT NULL,
  DIRECTORY             VARCHAR2(500 BYTE)      NOT NULL,
  NOTE                  VARCHAR2(2000 BYTE),
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


