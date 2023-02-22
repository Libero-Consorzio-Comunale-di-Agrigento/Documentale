ALTER TABLE OPERAZIONI_DI_INOLTRO ADD (
  CONSTRAINT OPERAZIONI_DI_INOLTRO_SINGO_CC
  CHECK (INOLTRO_SINGOLO IN ('Y','N'))
  ENABLE VALIDATE);

ALTER TABLE OPERAZIONI_DI_INOLTRO ADD (
  CONSTRAINT PK_OPERAZIONI_DI_INOLTRO
  PRIMARY KEY
  (ID_OP)
  USING INDEX PK_OPERAZIONI_DI_INOLTRO
  ENABLE VALIDATE);
