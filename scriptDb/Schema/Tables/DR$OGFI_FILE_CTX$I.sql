CREATE TABLE DR$OGFI_FILE_CTX$I
(
  TOKEN_TEXT   VARCHAR2(255 BYTE)               NOT NULL,
  TOKEN_TYPE   NUMBER(10)                       NOT NULL,
  TOKEN_FIRST  NUMBER(10)                       NOT NULL,
  TOKEN_LAST   NUMBER(10)                       NOT NULL,
  TOKEN_COUNT  NUMBER(10)                       NOT NULL,
  TOKEN_INFO   BLOB
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


