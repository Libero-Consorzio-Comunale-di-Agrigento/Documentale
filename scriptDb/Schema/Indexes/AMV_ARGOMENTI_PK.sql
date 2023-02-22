CREATE UNIQUE INDEX AMV_ARGOMENTI_PK ON AMV_ARGOMENTI
(ID_ARGOMENTO)
TABLESPACE GDM
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MAXSIZE          UNLIMITED
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           );


