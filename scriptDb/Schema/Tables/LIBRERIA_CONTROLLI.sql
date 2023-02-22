CREATE TABLE LIBRERIA_CONTROLLI
(
  AREA                  VARCHAR2(200 BYTE)      NOT NULL,
  CONTROLLO             VARCHAR2(30 BYTE)       NOT NULL,
  CORPO                 VARCHAR2(4000 BYTE),
  TIPO                  VARCHAR2(1 BYTE),
  DRIVER                VARCHAR2(100 BYTE),
  CONNESSIONE           VARCHAR2(100 BYTE),
  UTENTE                VARCHAR2(50 BYTE),
  PASSWD                VARCHAR2(50 BYTE),
  MSG_ERRORE            VARCHAR2(250 BYTE),
  CAMPI                 VARCHAR2(4000 BYTE),
  DSN                   VARCHAR2(50 BYTE),
  SBLOCCO_AUTOMATICO    VARCHAR2(1 BYTE)        DEFAULT 'S'                   NOT NULL,
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


