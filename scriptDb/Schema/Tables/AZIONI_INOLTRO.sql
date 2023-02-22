CREATE TABLE AZIONI_INOLTRO
(
  ID_OP                 NUMBER(6)               NOT NULL,
  ID_PROCEDURA          NUMBER(10)              DEFAULT 0                     NOT NULL,
  DESCRIZIONE           VARCHAR2(250 BYTE),
  ISTRUZIONE_SQL        CLOB,
  PARAMETRI             VARCHAR2(4000 BYTE),
  TIPO                  VARCHAR2(1 BYTE)        DEFAULT 'I'                   NOT NULL,
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


