CREATE TABLE SI4_ABILITAZIONI
(
  ID_ABILITAZIONE       NUMBER(10)              NOT NULL,
  ID_TIPO_OGGETTO       NUMBER(10),
  ID_TIPO_ABILITAZIONE  NUMBER(10)
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

COMMENT ON TABLE SI4_ABILITAZIONI IS 'ABIL - Abilitazione relative agli oggetti di competenza';



