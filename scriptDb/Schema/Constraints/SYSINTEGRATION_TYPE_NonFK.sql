ALTER TABLE SYSINTEGRATION_TYPE ADD (
  CONSTRAINT SYSINTEGRATIO_TYPE_ACTIVE_CC
  CHECK (ACTIVE IS NULL OR (ACTIVE IN (1,0)))
  ENABLE VALIDATE);

ALTER TABLE SYSINTEGRATION_TYPE ADD (
  CONSTRAINT SYSINTEGRATION_TYPE_PK
  PRIMARY KEY
  (TYPE_INTEGRATION)
  USING INDEX SYSINTEGRATION_TYPE_PK
  ENABLE VALIDATE);
