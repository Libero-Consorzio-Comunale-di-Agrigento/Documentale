CREATE TABLE PARAMETRI_POPUP
(
  AREA                  VARCHAR2(200 BYTE)      NOT NULL,
  BLOCCO                VARCHAR2(250 BYTE)      NOT NULL,
  PARAMETRO             VARCHAR2(30 BYTE)       NOT NULL,
  SEQUENZA              NUMBER                  DEFAULT 0                     NOT NULL,
  TIPO                  VARCHAR2(1 BYTE)        DEFAULT 'R'                   NOT NULL,
  VALORE_CAMPO          VARCHAR2(30 BYTE),
  VALORE_DEFAULT        VARCHAR2(200 BYTE),
  LABEL                 VARCHAR2(30 BYTE),
  AREA_DOMINIO          VARCHAR2(200 BYTE),
  DOMINIO               VARCHAR2(50 BYTE),
  CONDIZIONE            VARCHAR2(2000 BYTE)     NOT NULL,
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


