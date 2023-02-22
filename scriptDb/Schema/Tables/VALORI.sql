CREATE TABLE VALORI
(
  ID_VALORE             NUMBER(10)              NOT NULL,
  ID_DOCUMENTO          NUMBER(10)              NOT NULL,
  ID_CAMPO              NUMBER(10)              NOT NULL,
  VALORE_NUMERO         NUMBER(15,5),
  VALORE_DATA           DATE,
  VALORE_STRINGA        VARCHAR2(1000 BYTE),
  VALORE_CLOB           CLOB,
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

COMMENT ON COLUMN VALORI.ID_VALORE IS 'Chiave progressiva';

COMMENT ON COLUMN VALORI.VALORE_NUMERO IS 'Valore del dato se numero';

COMMENT ON COLUMN VALORI.VALORE_DATA IS 'Valore del dato se data';



