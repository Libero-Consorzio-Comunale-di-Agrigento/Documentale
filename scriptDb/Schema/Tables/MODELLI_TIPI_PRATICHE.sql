CREATE TABLE MODELLI_TIPI_PRATICHE
(
  ID_MTP          NUMBER(10)                    NOT NULL,
  AREA            VARCHAR2(200 BYTE)            NOT NULL,
  CODICE_MODELLO  VARCHAR2(50 BYTE)             NOT NULL,
  ID_PRATICA      NUMBER(10)                    NOT NULL
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


