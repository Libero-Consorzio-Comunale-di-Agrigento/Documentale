CREATE TABLE CHECK_DOCUMENTI
(
  ID_CHECK         NUMBER(10)                   NOT NULL,
  ID_DOCUMENTO     NUMBER(10)                   NOT NULL,
  DATA_CHECKIN     DATE                         NOT NULL,
  UTENTE_CHECKIN   VARCHAR2(8 BYTE)             NOT NULL,
  LIVELLO_CHECKIN  NUMBER(1)                    NOT NULL,
  UTENTE_MODIFICA  VARCHAR2(8 BYTE),
  DATA_MODIFICA    DATE
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

