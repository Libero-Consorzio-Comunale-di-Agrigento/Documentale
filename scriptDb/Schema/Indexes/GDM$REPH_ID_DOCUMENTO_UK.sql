CREATE UNIQUE INDEX GDM$REPH_ID_DOCUMENTO_UK ON GDM_T_REPORT_HEADER
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

