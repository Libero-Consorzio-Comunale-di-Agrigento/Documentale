ALTER TABLE BLOCCHI_MODELLO ADD (
  CONSTRAINT BLOCCHI_MODELLO_PK
  PRIMARY KEY
  (AREA, CODICE_MODELLO, BLOCCO)
  USING INDEX BLOCCHI_MODELLO_PK
  ENABLE VALIDATE);

