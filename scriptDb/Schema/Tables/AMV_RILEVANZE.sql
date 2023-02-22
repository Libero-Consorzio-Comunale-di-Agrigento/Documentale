CREATE TABLE AMV_RILEVANZE
(
  ID_RILEVANZA     NUMBER(10)                   NOT NULL,
  NOME             VARCHAR2(40 BYTE)            NOT NULL,
  IMPORTANZA       VARCHAR2(2 BYTE),
  SEQUENZA         NUMBER(4),
  ZONA             VARCHAR2(1 BYTE),
  ZONA_FORMATO     VARCHAR2(1 BYTE)             DEFAULT 'T',
  ZONA_VISIBILITA  VARCHAR2(1 BYTE)             DEFAULT 'H',
  MAX_VIS          NUMBER(4),
  IMMAGINE         VARCHAR2(200 BYTE),
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

COMMENT ON TABLE AMV_RILEVANZE IS 'RILE - Rilevanze usate principalmente per la esposizione degli articoli';



