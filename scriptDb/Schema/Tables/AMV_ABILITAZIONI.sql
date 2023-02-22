CREATE TABLE AMV_ABILITAZIONI
(
  ABILITAZIONE  NUMBER(8)                       NOT NULL,
  RUOLO         VARCHAR2(8 BYTE)                NOT NULL,
  MODULO        VARCHAR2(10 BYTE),
  VOCE_MENU     VARCHAR2(8 BYTE),
  PADRE         NUMBER(8),
  SEQUENZA      NUMBER(2),
  DISPOSITIVO   VARCHAR2(100 BYTE)
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

COMMENT ON TABLE AMV_ABILITAZIONI IS 'ABIL - Abilitazione delle voci di Menu';



