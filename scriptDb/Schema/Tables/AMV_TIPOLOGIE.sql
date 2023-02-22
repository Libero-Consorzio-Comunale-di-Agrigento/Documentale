CREATE TABLE AMV_TIPOLOGIE
(
  ID_TIPOLOGIA     NUMBER(10)                   NOT NULL,
  NOME             VARCHAR2(40 BYTE)            NOT NULL,
  DESCRIZIONE      VARCHAR2(2000 BYTE),
  ZONA             VARCHAR2(1 BYTE),
  SEQUENZA         NUMBER(4),
  IMMAGINE         VARCHAR2(200 BYTE),
  LINK             VARCHAR2(200 BYTE),
  ZONA_VISIBILITA  VARCHAR2(1 BYTE)             DEFAULT 'H',
  ZONA_FORMATO     VARCHAR2(1 BYTE)             DEFAULT 'T',
  MAX_VIS          NUMBER(4),
  ICONA            VARCHAR2(200 BYTE)
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

COMMENT ON TABLE AMV_TIPOLOGIE IS 'TIPO - Tipologie di articoli';



