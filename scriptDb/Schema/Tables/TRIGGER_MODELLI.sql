CREATE TABLE TRIGGER_MODELLI
(
  NOME                  VARCHAR2(20 BYTE)       NOT NULL,
  ID_TIPODOC            NUMBER(10)              NOT NULL,
  DESCRIZIONE           VARCHAR2(2000 BYTE),
  TIPO                  VARCHAR2(3 BYTE)        NOT NULL,
  CREA_TB_TC            VARCHAR2(1 BYTE),
  ELIMINARE             VARCHAR2(1 BYTE)        NOT NULL,
  CORPO                 CLOB,
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


