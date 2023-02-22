CREATE TABLE KEY_ERROR
(
  ERRORE       VARCHAR2(6 BYTE)                 NOT NULL,
  DESCRIZIONE  VARCHAR2(240 BYTE)               NOT NULL,
  TIPO         VARCHAR2(1 BYTE),
  KEY          VARCHAR2(30 BYTE)
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

COMMENT ON COLUMN KEY_ERROR.DESCRIZIONE IS 'Descrizione ERRORE <NLS>';



