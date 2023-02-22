CREATE TABLE KEY_CONSTRAINT_TYPE
(
  DB_ERROR     VARCHAR2(10 BYTE)                NOT NULL,
  TIPO_ERRORE  VARCHAR2(2 BYTE)                 NOT NULL
)
TABLESPACE GDM
PCTUSED    0
PCTFREE    10
INITRANS   1
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


