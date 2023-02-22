CREATE TABLE GDM_T_REPORT_FOOTER
(
  ID_DOCUMENTO  NUMBER(10)                      NOT NULL,
  TITOLO        VARCHAR2(50 BYTE),
  FULL_TEXT     CLOB
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


