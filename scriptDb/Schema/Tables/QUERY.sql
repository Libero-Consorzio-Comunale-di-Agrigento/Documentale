CREATE TABLE QUERY
(
  ID_QUERY              NUMBER(10)              NOT NULL,
  NOME                  VARCHAR2(100 BYTE)      NOT NULL,
  TIPO                  VARCHAR2(1 BYTE)        NOT NULL,
  FILTRO                VARCHAR2(4000 BYTE),
  DATA_AGGIORNAMENTO    DATE                    NOT NULL,
  UTENTE_AGGIORNAMENTO  VARCHAR2(8 BYTE)        NOT NULL,
  ID_DOCUMENTO_PROFILO  NUMBER(10),
  CODICEADS             VARCHAR2(100 BYTE)
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


