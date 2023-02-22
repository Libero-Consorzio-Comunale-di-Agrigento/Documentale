CREATE TABLE VALORI_DOMINIO
(
  AREA                  VARCHAR2(200 BYTE)      NOT NULL,
  DOMINIO               VARCHAR2(50 BYTE)       NOT NULL,
  CODICE_MODELLO        VARCHAR2(50 BYTE)       DEFAULT '-'                   NOT NULL,
  CODICE                VARCHAR2(50 BYTE)       NOT NULL,
  VALORE                VARCHAR2(300 BYTE),
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


