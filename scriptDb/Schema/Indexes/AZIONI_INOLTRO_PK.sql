CREATE UNIQUE INDEX AZIONI_INOLTRO_PK ON AZIONI_INOLTRO
(ID_OP, ID_PROCEDURA)
TABLESPACE GDM
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            MAXSIZE          UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           );


