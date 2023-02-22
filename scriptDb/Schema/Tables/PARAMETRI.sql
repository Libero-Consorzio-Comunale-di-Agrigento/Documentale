CREATE TABLE PARAMETRI
(
  CODICE        VARCHAR2(100 BYTE)              NOT NULL,
  TIPO_MODELLO  VARCHAR2(10 BYTE)               NOT NULL,
  VALORE        VARCHAR2(4000 BYTE)
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

COMMENT ON TABLE PARAMETRI IS 'Tabella dei parametri per la configurazione dell''ambiente.';



