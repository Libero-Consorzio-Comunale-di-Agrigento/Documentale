ALTER TABLE VALORI_LOG ADD (
  CONSTRAINT VALORI_LOG_PK
  PRIMARY KEY
  (ID_VALORE_LOG)
  USING INDEX VALORI_LOG_PK
  ENABLE VALIDATE);

