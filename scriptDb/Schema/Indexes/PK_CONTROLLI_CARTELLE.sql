CREATE UNIQUE INDEX PK_CONTROLLI_CARTELLE ON CONTROLLI_CARTELLE
(ACTION_KEY, AREA)
TABLESPACE GDM
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            MAXSIZE          UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           );


