CREATE TABLE LINKS
(
  ID_LINK               NUMBER(10)              NOT NULL,
  ID_CARTELLA           NUMBER(10)              NOT NULL,
  ID_OGGETTO            NUMBER(10),
  TIPO_OGGETTO          VARCHAR2(1 BYTE)        NOT NULL,
  DATA_AGGIORNAMENTO    DATE                    NOT NULL,
  UTENTE_AGGIORNAMENTO  VARCHAR2(8 BYTE)        NOT NULL,
  ORDINAMENTO           VARCHAR2(256 BYTE),
  AUTOMATICO            VARCHAR2(1 BYTE)
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


