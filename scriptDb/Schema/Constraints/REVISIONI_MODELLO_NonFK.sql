ALTER TABLE REVISIONI_MODELLO ADD (
  CONSTRAINT REVISIONI_MODELLO_PK
  PRIMARY KEY
  (AREA, CODICE_MODELLO, REVISIONE)
  USING INDEX REVISIONI_MODELLO_PK
  ENABLE VALIDATE);

