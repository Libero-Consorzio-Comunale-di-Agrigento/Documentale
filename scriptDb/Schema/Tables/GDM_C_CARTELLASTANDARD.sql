CREATE TABLE GDM_C_CARTELLASTANDARD
(
  ID_DOCUMENTO  NUMBER(10)                      NOT NULL,
  "$ACTIONKEY"  CLOB,
  NOME          VARCHAR2(100 BYTE),
  FULL_TEXT     CLOB
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


