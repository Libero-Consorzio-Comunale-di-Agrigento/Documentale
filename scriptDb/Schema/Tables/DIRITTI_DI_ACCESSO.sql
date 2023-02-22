CREATE TABLE DIRITTI_DI_ACCESSO
(
  CODICE_RICHIESTA  VARCHAR2(100 BYTE)          NOT NULL,
  AREA              VARCHAR2(200 BYTE)          NOT NULL,
  UTENTE            VARCHAR2(20 BYTE)           NOT NULL,
  DATA_INSERIMENTO  DATE
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

COMMENT ON TABLE DIRITTI_DI_ACCESSO IS 'Diritti di accesso sulle singole richieste specificati per utente.';



