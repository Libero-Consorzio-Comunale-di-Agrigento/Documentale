ALTER TABLE CHECK_DOCUMENTI ADD (
  CONSTRAINT CHECK_DOCUMENTI_PK
  PRIMARY KEY
  (ID_CHECK)
  USING INDEX CHECK_DOCUMENTI_PK
  ENABLE VALIDATE);

