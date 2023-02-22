CREATE TABLE ETICHETTE
(
  AREA                  VARCHAR2(250 BYTE)      NOT NULL,
  CODICE_MODELLO        VARCHAR2(50 BYTE)       NOT NULL,
  ETICHETTA             VARCHAR2(50 BYTE)       NOT NULL,
  VALORE                VARCHAR2(250 BYTE),
  ICONA                 VARCHAR2(50 BYTE),
  ICONA_DS              VARCHAR2(50 BYTE),
  SALVATAGGIO           VARCHAR2(2 BYTE)        DEFAULT 'NO',
  SEPARATORE            VARCHAR2(1 BYTE)        DEFAULT 'N'                   NOT NULL,
  CONTROLLO_JS          VARCHAR2(30 BYTE),
  CONTROLLO             VARCHAR2(30 BYTE),
  ITER_FLUSSO           VARCHAR2(250 BYTE),
  TIPO_USO              VARCHAR2(1 BYTE)        DEFAULT 'E'                   NOT NULL,
  DATA_AGGIORNAMENTO    DATE                    DEFAULT SYSDATE               NOT NULL,
  UTENTE_AGGIORNAMENTO  VARCHAR2(8 BYTE)        DEFAULT 'GDM'                 NOT NULL,
  PERSONALIZZAZIONE     VARCHAR2(1 BYTE)        DEFAULT 'N'                   NOT NULL
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


