CREATE TABLE ICONE
(
  ICONA                 VARCHAR2(50 BYTE)       NOT NULL,
  NOME                  VARCHAR2(100 BYTE)      NOT NULL,
  TOOLTIP               VARCHAR2(250 BYTE),
  RISORSA               BLOB,
  MODIFICATA            VARCHAR2(1 BYTE)        DEFAULT 'S'                   NOT NULL,
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


