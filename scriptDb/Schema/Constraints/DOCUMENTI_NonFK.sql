ALTER TABLE DOCUMENTI ADD (
  CONSTRAINT DOCUMENTI_PK
  PRIMARY KEY
  (ID_DOCUMENTO)
  USING INDEX DOCUMENTI_PK
  ENABLE VALIDATE);
