CREATE UNIQUE INDEX SYSINTEGRATION_MODEL_PK ON SYSINTEGRATION_MODEL
(TYPE_INTEGRATION, AREA, CODICE_MODELLO)
TABLESPACE GDM
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            MAXSIZE          UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           );


