CREATE TABLE RIFERIMENTI
(
  ID_DOCUMENTO          NUMBER(10)              NOT NULL,
  ID_DOCUMENTO_RIF      NUMBER(10)              NOT NULL,
  LIBRERIA_REMOTA       VARCHAR2(200 BYTE),
  AREA                  VARCHAR2(200 BYTE)      NOT NULL,
  TIPO_RELAZIONE        VARCHAR2(10 BYTE)       NOT NULL,
  DATA_AGGIORNAMENTO    DATE                    NOT NULL,
  UTENTE_AGGIORNAMENTO  VARCHAR2(8 BYTE)        NOT NULL
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


