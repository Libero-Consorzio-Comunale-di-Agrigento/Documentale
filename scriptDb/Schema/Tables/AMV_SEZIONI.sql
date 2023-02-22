CREATE TABLE AMV_SEZIONI
(
  ID_SEZIONE       NUMBER(10)                   NOT NULL,
  NOME             VARCHAR2(100 BYTE),
  DESCRIZIONE      VARCHAR2(2000 BYTE),
  ZONA             VARCHAR2(1 BYTE),
  SEQUENZA         NUMBER(4),
  IMMAGINE         VARCHAR2(200 BYTE),
  MAX_VIS          NUMBER(4),
  ID_PADRE         NUMBER(10),
  VISIBILITA       VARCHAR2(2 BYTE),
  ZONA_FORMATO     VARCHAR2(1 BYTE)             DEFAULT 'T',
  ZONA_TIPO        VARCHAR2(1 BYTE)             DEFAULT 'S',
  ZONA_ESPANSIONE  VARCHAR2(1 BYTE)             DEFAULT 'S',
  ZONA_VISIBILITA  VARCHAR2(1 BYTE)             DEFAULT 'S',
  STYLE            VARCHAR2(100 BYTE),
  LOGO_SX          VARCHAR2(100 BYTE),
  LOGO_SX_LINK     VARCHAR2(200 BYTE),
  LOGO_DX          VARCHAR2(100 BYTE),
  LOGO_DX_LINK     VARCHAR2(200 BYTE),
  IMG_HEADER       VARCHAR2(100 BYTE),
  INTESTAZIONE     VARCHAR2(2000 BYTE),
  COPYRIGHT        VARCHAR2(2000 BYTE),
  ID_AREA          NUMBER(10),
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


