CREATE UNIQUE INDEX COLLEGAMENTI_UK ON COLLEGAMENTI
(ID_CARTELLA, ID_CARTELLA_COLLEGATA, CATEGORIA_COLLEGATA)
TABLESPACE GDM
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            MAXSIZE          UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           );

