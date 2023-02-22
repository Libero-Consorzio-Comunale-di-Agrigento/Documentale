CREATE TABLE PRE_INOLTRO
(
  ID_OP             NUMBER(6)                   NOT NULL,
  CODICE_RICHIESTA  VARCHAR2(100 BYTE)          NOT NULL,
  CODICE_MODELLO    VARCHAR2(50 BYTE)           NOT NULL,
  AREA              VARCHAR2(250 BYTE)          NOT NULL,
  DATO              VARCHAR2(30 BYTE)           NOT NULL,
  PROGRESSIVO       NUMBER                      NOT NULL,
  VALORE            CLOB,
  ELABORATO         VARCHAR2(1 BYTE)            DEFAULT 'N'
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

COMMENT ON TABLE PRE_INOLTRO IS 'Repository temporaneo dei dati per fase di inoltro su DB esterni';



