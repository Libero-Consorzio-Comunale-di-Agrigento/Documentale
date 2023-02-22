CREATE TABLE DM_ERR_LOG
(
  ID_ERR_LOG  NUMBER                            NOT NULL,
  UTENTE      VARCHAR2(8 BYTE)                  NOT NULL,
  DATA        DATE                              NOT NULL,
  CATEGORIA   VARCHAR2(10 BYTE)                 NOT NULL
)
TABLESPACE GDM
PCTUSED    0
PCTFREE    10
INITRANS   1
MAXTRANS   255
STORAGE    (
            MAXSIZE          UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           );


