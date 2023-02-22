CREATE TABLE GRAFICI_MODELLO
(
  AREA            VARCHAR2(200 BYTE)            NOT NULL,
  CODICE_MODELLO  VARCHAR2(50 BYTE)             NOT NULL,
  NOMEFILE        VARCHAR2(255 BYTE)            NOT NULL,
  GRAFICO         BLOB
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

COMMENT ON TABLE GRAFICI_MODELLO IS 'FIle grafici legati al modello stesso.';



