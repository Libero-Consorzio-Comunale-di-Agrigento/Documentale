CREATE TABLE INDICI_DOCUMENTO
(
  INDICE                VARCHAR2(255 BYTE)      NOT NULL,
  ID_TIPODOC            NUMBER(10)              NOT NULL,
  DESCRIZIONE           VARCHAR2(2000 BYTE),
  TIPO_USO              VARCHAR2(3 BYTE)        NOT NULL,
  FORMATO               VARCHAR2(1 BYTE)        DEFAULT 'C'                   NOT NULL,
  ISTRUZIONE            VARCHAR2(2000 BYTE)     NOT NULL,
  STATO                 VARCHAR2(1 BYTE)        DEFAULT 'N'                   NOT NULL,
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


