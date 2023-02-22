CREATE TABLE EVENTI
(
  EVENTO       VARCHAR2(100 BYTE)               NOT NULL,
  DESCRIZIONE  VARCHAR2(500 BYTE),
  TABELLA      VARCHAR2(20 BYTE)
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


