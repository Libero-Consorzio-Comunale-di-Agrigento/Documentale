CREATE TABLE LOG_INOLTRI
(
  CODICE_RICHIESTA  VARCHAR2(100 BYTE)          NOT NULL,
  AREA              VARCHAR2(250 BYTE)          NOT NULL,
  ID_OP             NUMBER(6)                   NOT NULL,
  ID_PROCEDURA      NUMBER(10),
  STATO             NUMBER(1)                   DEFAULT 0,
  NOTE              VARCHAR2(2000 BYTE)
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


