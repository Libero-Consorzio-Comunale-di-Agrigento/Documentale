ALTER TABLE ACTIVITY_LOG ADD (
  CONSTRAINT ACTIVITY_LOG_TIPO_AZIONE_CC
  CHECK (TIPO_AZIONE IN ('L','M','C','E','R'))
  ENABLE VALIDATE);

ALTER TABLE ACTIVITY_LOG ADD (
  CONSTRAINT ACTIVITY_LOG_PK
  PRIMARY KEY
  (ID_LOG)
  USING INDEX ACTIVITY_LOG_PK
  ENABLE VALIDATE);

