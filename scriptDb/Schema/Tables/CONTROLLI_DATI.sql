CREATE TABLE CONTROLLI_DATI
(
  AREA                  VARCHAR2(200 BYTE)      NOT NULL,
  DATO                  VARCHAR2(30 BYTE)       NOT NULL,
  CONTROLLO             VARCHAR2(30 BYTE)       NOT NULL,
  SEQUENZA              NUMBER                  NOT NULL,
  EVENTO                VARCHAR2(100 BYTE),
  DATA_AGGIORNAMENTO    DATE                    DEFAULT SYSDATE               NOT NULL,
  UTENTE_AGGIORNAMENTO  VARCHAR2(8 BYTE)        DEFAULT 'GDM'                 NOT NULL
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

COMMENT ON TABLE CONTROLLI_DATI IS 'Dizionario dei controlli disponibili sui singoli dati.';



