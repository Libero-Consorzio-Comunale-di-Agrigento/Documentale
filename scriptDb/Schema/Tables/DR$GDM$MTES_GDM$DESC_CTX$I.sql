CREATE TABLE DR$GDM$MTES_GDM$DESC_CTX$I
(
  DR$TOKEN       VARCHAR2(64 BYTE)              NOT NULL,
  DR$TOKEN_TYPE  NUMBER(10)                     NOT NULL,
  DR$ROWID       ROWID                          NOT NULL,
  DR$TOKEN_INFO  RAW(2000)                      NOT NULL
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


