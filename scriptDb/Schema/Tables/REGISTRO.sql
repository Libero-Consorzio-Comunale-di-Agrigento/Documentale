CREATE TABLE REGISTRO
(
  CHIAVE    VARCHAR2(512 BYTE)                  NOT NULL,
  STRINGA   VARCHAR2(100 BYTE)                  NOT NULL,
  COMMENTO  VARCHAR2(2000 BYTE),
  VALORE    VARCHAR2(2000 BYTE)
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


