CREATE UNIQUE INDEX PK_XML ON XML
(CODICE_XML, AREA, CODICE_RICHIESTA)
TABLESPACE GDM
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            MAXSIZE          UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           );


