CREATE TABLE SI4_TIPI_ABILITAZIONE
(
  ID_TIPO_ABILITAZIONE  NUMBER(10)              NOT NULL,
  TIPO_ABILITAZIONE     VARCHAR2(2 BYTE)        NOT NULL,
  DESCRIZIONE           VARCHAR2(2000 BYTE)
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

COMMENT ON TABLE SI4_TIPI_ABILITAZIONE IS 'TIAB - Tipi di abiliazione attribuibili agli oggetti di competenza';



