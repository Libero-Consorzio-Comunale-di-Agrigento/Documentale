ALTER TABLE CLASSIFICAZIONI ADD (
  CONSTRAINT CLASSIFICAZIONI_PK
  PRIMARY KEY
  (ID_CLASSIFICAZIONE)
  USING INDEX CLASSIFICAZIONI_PK
  ENABLE VALIDATE);

