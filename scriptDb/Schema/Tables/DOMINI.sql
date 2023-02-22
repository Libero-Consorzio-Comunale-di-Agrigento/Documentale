CREATE TABLE DOMINI
(
  AREA                  VARCHAR2(200 BYTE)      NOT NULL,
  DOMINIO               VARCHAR2(50 BYTE)       NOT NULL,
  CODICE_MODELLO        VARCHAR2(50 BYTE)       DEFAULT '-'                   NOT NULL,
  PRECARICA             VARCHAR2(1 BYTE)        DEFAULT 'S'                   NOT NULL,
  SEQ_DOMINIO_AREA      NUMBER(11)              NOT NULL,
  DESCRIZIONE           VARCHAR2(100 BYTE),
  TIPO                  CHAR(1 BYTE),
  DRIVER                VARCHAR2(100 BYTE),
  CONNESSIONE           VARCHAR2(500 BYTE),
  UTENTE                VARCHAR2(50 BYTE),
  PASSWD                VARCHAR2(50 BYTE),
  ISTRUZIONE            CLOB,
  ORDINAMENTO           VARCHAR2(1 BYTE),
  DSN                   VARCHAR2(50 BYTE),
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

COMMENT ON TABLE DOMINI IS 'Testata dei domini (inseme di valori che puo'' assumere un dato).';



