CREATE TABLE TIPI_RELAZIONE
(
  AREA                  VARCHAR2(200 BYTE)      NOT NULL,
  TIPO_RELAZIONE        VARCHAR2(10 BYTE)       NOT NULL,
  DESCRIZIONE           VARCHAR2(200 BYTE),
  VISIBILE              VARCHAR2(1 BYTE)        DEFAULT 'N',
  DIPENDENZA            VARCHAR2(1 BYTE)        DEFAULT 'N',
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


