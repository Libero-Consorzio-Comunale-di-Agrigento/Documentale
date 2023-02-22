CREATE TABLE CONTROLLI_MODELLI
(
  AREA                  VARCHAR2(200 BYTE)      NOT NULL,
  CODICE_MODELLO        VARCHAR2(50 BYTE)       NOT NULL,
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
            INITIAL          64K
            NEXT             1M
            MAXSIZE          UNLIMITED
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           );


