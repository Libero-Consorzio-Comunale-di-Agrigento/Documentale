CREATE TABLE AMV_AREE
(
  ID_AREA      NUMBER(10)                       NOT NULL,
  NOME         VARCHAR2(40 BYTE)                NOT NULL,
  DESCRIZIONE  VARCHAR2(2000 BYTE)
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

COMMENT ON TABLE AMV_AREE IS 'AREE - Aree di competenza dei documenti';



