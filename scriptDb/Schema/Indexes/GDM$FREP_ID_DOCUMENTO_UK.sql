CREATE UNIQUE INDEX GDM$FREP_ID_DOCUMENTO_UK ON GDM_T_FIRMA_REPORT
(ID_DOCUMENTO)
TABLESPACE GDM
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            MAXSIZE          UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           );

