CREATE TABLE XML
(
  CODICE_RICHIESTA  VARCHAR2(100 BYTE)          NOT NULL,
  AREA              VARCHAR2(200 BYTE)          NOT NULL,
  CODICE_XML        VARCHAR2(50 BYTE)           NOT NULL
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


