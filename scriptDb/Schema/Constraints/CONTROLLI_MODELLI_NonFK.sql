ALTER TABLE CONTROLLI_MODELLI ADD (
  CONSTRAINT CONTROLLI_MODELLI_PK
  PRIMARY KEY
  (AREA, CODICE_MODELLO, CONTROLLO)
  USING INDEX CONTROLLI_MODELLI_PK
  ENABLE VALIDATE);

