CREATE UNIQUE INDEX INDICI_DOCUMENTO_PK ON INDICI_DOCUMENTO
(INDICE, ID_TIPODOC)
TABLESPACE GDM
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            MAXSIZE          UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           );


