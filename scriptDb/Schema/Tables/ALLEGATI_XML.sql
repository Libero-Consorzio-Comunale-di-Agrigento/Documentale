CREATE TABLE ALLEGATI_XML
(
  CODICE_XML            VARCHAR2(50 BYTE),
  AREA                  VARCHAR2(200 BYTE),
  CODICE_RICHIESTA      VARCHAR2(100 BYTE),
  ALL_CODICE_RICHIESTA  VARCHAR2(100 BYTE),
  ALL_AREA              VARCHAR2(200 BYTE),
  ALL_CODICE_ALLEGATO   VARCHAR2(255 BYTE)
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

COMMENT ON TABLE ALLEGATI_XML IS 'Tabella di collegamento fra i file XML creati e gli allegati corrispondenti.';



