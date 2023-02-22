CREATE TABLE ALLEGATI_TEMP
(
  AREA                  VARCHAR2(200 BYTE)      NOT NULL,
  CODICE_MODELLO        VARCHAR2(50 BYTE)       NOT NULL,
  CODICE_RICHIESTA      VARCHAR2(100 BYTE)      NOT NULL,
  NOMEFILE              VARCHAR2(200 BYTE)      NOT NULL,
  STATO                 VARCHAR2(1 BYTE),
  ALLEGATO              BLOB,
  UTENTE_AGGIORNAMENTO  VARCHAR2(8 BYTE)        DEFAULT 'GDM'                 NOT NULL,
  DATA_AGGIORNAMENTO    DATE                    DEFAULT SYSDATE               NOT NULL,
  FORZA                 VARCHAR2(1 BYTE)        DEFAULT 'N'
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


