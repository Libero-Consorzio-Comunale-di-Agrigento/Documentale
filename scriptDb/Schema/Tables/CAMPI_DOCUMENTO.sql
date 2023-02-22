CREATE TABLE CAMPI_DOCUMENTO
(
  ID_CAMPO              NUMBER(10)              NOT NULL,
  ID_TIPODOC            NUMBER(10)              NOT NULL,
  NOME                  VARCHAR2(100 BYTE)      NOT NULL,
  OBBLIGATORIO          VARCHAR2(1 BYTE)        NOT NULL,
  RICERCA               VARCHAR2(1 BYTE),
  SEQUENZA              NUMBER,
  ID_ALIASCAMPO         NUMBER(10),
  FLAG_VIEW             VARCHAR2(1 BYTE)        DEFAULT 'S',
  DATA_AGGIORNAMENTO    DATE                    NOT NULL,
  UTENTE_AGGIORNAMENTO  VARCHAR2(8 BYTE)        NOT NULL,
  MODALITA_RICERCA      VARCHAR2(1 BYTE)        DEFAULT 'N'                   NOT NULL,
  INDICE_GENERATO       NUMBER(1)               DEFAULT 0                     NOT NULL
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


