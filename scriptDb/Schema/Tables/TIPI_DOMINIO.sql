CREATE TABLE TIPI_DOMINIO
(
  TIPO_DOMINIO_ID  CHAR(1 BYTE)                 NOT NULL,
  TIPO_DOMINIO     VARCHAR2(50 BYTE)
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

COMMENT ON TABLE TIPI_DOMINIO IS 'Tipologie di dominio.';



