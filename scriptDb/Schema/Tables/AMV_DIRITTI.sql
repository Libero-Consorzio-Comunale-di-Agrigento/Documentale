CREATE TABLE AMV_DIRITTI
(
  ID_DIRITTO    NUMBER(10)                      NOT NULL,
  ID_AREA       NUMBER(10)                      NOT NULL,
  GRUPPO        VARCHAR2(8 BYTE),
  ACCESSO       VARCHAR2(1 BYTE)                DEFAULT 'R'                   NOT NULL,
  ID_TIPOLOGIA  NUMBER(10)
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

COMMENT ON TABLE AMV_DIRITTI IS 'DIRI - Diritti per Gruppo e Categoria di articoli';



