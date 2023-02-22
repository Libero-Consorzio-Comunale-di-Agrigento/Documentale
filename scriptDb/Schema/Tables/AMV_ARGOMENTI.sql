CREATE TABLE AMV_ARGOMENTI
(
  ID_ARGOMENTO  NUMBER(10)                      NOT NULL,
  NOME          VARCHAR2(100 BYTE)              NOT NULL,
  DESCRIZIONE   VARCHAR2(2000 BYTE)
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

COMMENT ON TABLE AMV_ARGOMENTI IS 'ARGO - Argomenti delle Categorie di Documenti';



