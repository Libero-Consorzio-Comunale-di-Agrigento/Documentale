CREATE TABLE ACTIVITY_LOG
(
  ID_LOG                NUMBER(10)              NOT NULL,
  ID_DOCUMENTO          NUMBER(10)              NOT NULL,
  TIPO_AZIONE           VARCHAR2(1 BYTE)        NOT NULL,
  DATA_AGGIORNAMENTO    DATE                    NOT NULL,
  UTENTE_AGGIORNAMENTO  VARCHAR2(8 BYTE)        NOT NULL,
  VERSIONE              NUMBER(10)
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


