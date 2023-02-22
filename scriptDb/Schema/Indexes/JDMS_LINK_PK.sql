CREATE UNIQUE INDEX JDMS_LINK_PK ON JDMS_LINK
(ID_TIPODOC, TAG)
TABLESPACE GDM
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            MAXSIZE          UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           );


