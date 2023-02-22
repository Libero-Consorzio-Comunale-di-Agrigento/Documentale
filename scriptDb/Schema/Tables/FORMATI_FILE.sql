CREATE TABLE FORMATI_FILE
(
  ID_FORMATO            NUMBER(10)              NOT NULL,
  NOME                  VARCHAR2(10 BYTE)       NOT NULL,
  ICONA                 VARCHAR2(50 BYTE)       DEFAULT 'generico.gif',
  VISIBILE              VARCHAR2(1 BYTE)        DEFAULT 'S'                   NOT NULL,
  DATA_AGGIORNAMENTO    DATE                    NOT NULL,
  UTENTE_AGGIORNAMENTO  VARCHAR2(8 BYTE)        NOT NULL
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


