CREATE UNIQUE INDEX CODICE ON TIPI_PRATICHE
(CODICE_TIPO_PRATICA)
TABLESPACE GDM
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            MAXSIZE          UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           );

