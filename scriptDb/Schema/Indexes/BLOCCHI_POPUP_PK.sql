CREATE UNIQUE INDEX BLOCCHI_POPUP_PK ON BLOCCHI_POPUP
(AREA, BLOCCO)
TABLESPACE GDM
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            MAXSIZE          UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           );


