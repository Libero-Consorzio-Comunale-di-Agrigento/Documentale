CREATE TABLE BLOCCHI_POPUP
(
  AREA                    VARCHAR2(200 BYTE)    NOT NULL,
  BLOCCO                  VARCHAR2(250 BYTE)    NOT NULL,
  DRIVER                  VARCHAR2(100 BYTE),
  CONNESSIONE             VARCHAR2(100 BYTE),
  UTENTE                  VARCHAR2(50 BYTE),
  PASSWD                  VARCHAR2(50 BYTE),
  VALORE_RITORNO          VARCHAR2(2000 BYTE)   NOT NULL,
  RECORD_DA_VISUALIZZARE  VARCHAR2(50 BYTE)     NOT NULL,
  CAMPI_DI_RICERCA        VARCHAR2(2000 BYTE),
  SEPARATORE              VARCHAR2(100 BYTE),
  CORPO                   CLOB,
  ISTRUZIONE              CLOB,
  TIPO                    VARCHAR2(1 BYTE)      DEFAULT 'O'                   NOT NULL,
  AUTOLOAD                VARCHAR2(1 BYTE)      DEFAULT 'N'                   NOT NULL,
  FILTRI                  VARCHAR2(2000 BYTE),
  AGGIORNA_PADRE          VARCHAR2(1 BYTE)      DEFAULT 'N',
  DSN                     VARCHAR2(50 BYTE),
  DATA_AGGIORNAMENTO      DATE                  DEFAULT SYSDATE               NOT NULL,
  UTENTE_AGGIORNAMENTO    VARCHAR2(8 BYTE)      DEFAULT 'GDM'                 NOT NULL,
  FILTRI_ESTERNI          VARCHAR2(1 BYTE)      DEFAULT 'N'                   NOT NULL,
  CONTROLLO_JS            VARCHAR2(30 BYTE),
  CHIUDI_POPUP            VARCHAR2(1 BYTE)      DEFAULT 'S'                   NOT NULL
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


