CREATE TABLE ADS_ORIZZONTALE
(
  ID_DOCUMENTO              NUMBER(10)          NOT NULL,
  CART_AUTO                 CLOB,
  RICH_TEXT_AREA            CLOB,
  STATO_AVANZAMENTO_FLUSSO  VARCHAR2(50 BYTE),
  TEST_CHECK                VARCHAR2(10 BYTE),
  TEST_COMBO                VARCHAR2(2 BYTE),
  TEST_DATA                 DATE,
  TEST_RADIO                VARCHAR2(2 BYTE),
  TEXT_AREA                 VARCHAR2(4000 BYTE),
  FULL_TEXT                 CLOB
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


