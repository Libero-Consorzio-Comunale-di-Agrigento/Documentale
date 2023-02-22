CREATE UNIQUE INDEX TIPI_DOMINIO_PK ON TIPI_DOMINIO
(TIPO_DOMINIO_ID)
TABLESPACE GDM
PCTFREE    10
INITRANS   2
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


