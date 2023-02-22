CREATE TABLE AMV_VOCI
(
  VOCE          VARCHAR2(8 BYTE)                NOT NULL,
  PROGETTO      VARCHAR2(8 BYTE),
  ACRONIMO      VARCHAR2(5 BYTE),
  ACRONIMO_AL1  VARCHAR2(5 BYTE),
  ACRONIMO_AL2  VARCHAR2(5 BYTE),
  TITOLO        VARCHAR2(40 BYTE),
  TITOLO_AL1    VARCHAR2(40 BYTE),
  TITOLO_AL2    VARCHAR2(40 BYTE),
  TIPO_VOCE     VARCHAR2(1 BYTE)                NOT NULL,
  TIPO          VARCHAR2(1 BYTE)                NOT NULL,
  MODULO        VARCHAR2(40 BYTE),
  STRINGA       VARCHAR2(200 BYTE),
  PROFILO       NUMBER(2),
  VOCE_GUIDA    VARCHAR2(8 BYTE),
  PROPRIETA     VARCHAR2(1 BYTE),
  NOTE          VARCHAR2(2000 BYTE)
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

COMMENT ON TABLE AMV_VOCI IS 'VOCI - Voci di Menu';


