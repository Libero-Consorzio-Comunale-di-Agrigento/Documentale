ALTER TABLE REPOSITORYTEMP ADD (
  CONSTRAINT PK_REPOSITORYTEMP
  PRIMARY KEY
  (CODICE_RICHIESTA, CODICE_MODELLO, PROGRESSIVO, AREA, DATO)
  USING INDEX PK_REPOSITORYTEMP
  ENABLE VALIDATE);
