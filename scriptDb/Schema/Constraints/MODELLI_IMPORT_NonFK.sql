ALTER TABLE MODELLI_IMPORT ADD (
  CONSTRAINT MODELLI_IMPORT_PK
  PRIMARY KEY
  (AREA, CODICE_MODELLO)
  USING INDEX MODELLI_IMPORT_PK
  ENABLE VALIDATE);

