CREATE TABLE SI4_TIPI_OGGETTO
(
  ID_TIPO_OGGETTO  NUMBER(10)                   NOT NULL,
  TIPO_OGGETTO     VARCHAR2(30 BYTE)            NOT NULL,
  DESCRIZIONE      VARCHAR2(2000 BYTE),
  DELEGABILE       VARCHAR2(1 BYTE)             DEFAULT 'N'                   NOT NULL
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

COMMENT ON TABLE SI4_TIPI_OGGETTO IS 'TIOG - Tipi di Oggetto per Gestione Competenze';



