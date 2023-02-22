CREATE TABLE TIPI_ALLEGATI_MODELLO
(
  AREA            VARCHAR2(200 BYTE)            NOT NULL,
  CODICE_MODELLO  VARCHAR2(50 BYTE)             NOT NULL,
  FORMATO         VARCHAR2(10 BYTE)             NOT NULL
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

