CREATE TABLE KFX_DATI_DOCUMENTO
(
  ID_DOCUMENTO      VARCHAR2(10 BYTE)           NOT NULL,
  BC_DOCUMENTO      VARCHAR2(10 BYTE)           NOT NULL,
  ID_STAZIONE       VARCHAR2(25 BYTE),
  DATA_ORA          DATE,
  ELABORATO         VARCHAR2(1 BYTE),
  DETTAGLIO_ERRORE  VARCHAR2(2000 BYTE),
  UTENTE            VARCHAR2(8 BYTE)
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


