ALTER TABLE GRUPPI ADD (
  CONSTRAINT GRUPPI_PK
  PRIMARY KEY
  (ID_GRUPPO)
  USING INDEX GRUPPI_PK
  ENABLE VALIDATE);
