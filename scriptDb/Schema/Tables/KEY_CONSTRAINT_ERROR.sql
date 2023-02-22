CREATE TABLE KEY_CONSTRAINT_ERROR
(
  NOME          VARCHAR2(30 BYTE)               NOT NULL,
  TIPO_ERRORE   VARCHAR2(2 BYTE)                NOT NULL,
  ERRORE        VARCHAR2(6 BYTE)                NOT NULL,
  PRECISAZIONE  VARCHAR2(2000 BYTE)
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


