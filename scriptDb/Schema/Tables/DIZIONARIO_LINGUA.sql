CREATE TABLE DIZIONARIO_LINGUA
(
  ESPRESSIONE  VARCHAR2(2000 BYTE)              NOT NULL,
  LINGUA       VARCHAR2(100 BYTE)               NOT NULL,
  TRADUZIONE   VARCHAR2(2000 BYTE)              NOT NULL
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


