ALTER TABLE MAPPING_CAMPI ADD (
  CONSTRAINT MAPPING_CAMPI_PK
  PRIMARY KEY
  (ID_MAPPING_CAMPO)
  USING INDEX MAPPING_CAMPI_PK
  ENABLE VALIDATE);

