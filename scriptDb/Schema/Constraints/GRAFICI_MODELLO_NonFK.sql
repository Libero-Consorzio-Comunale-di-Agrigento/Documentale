ALTER TABLE GRAFICI_MODELLO ADD (
  CONSTRAINT PK_GRAFICI_MODELLO
  PRIMARY KEY
  (AREA, CODICE_MODELLO, NOMEFILE)
  USING INDEX PK_GRAFICI_MODELLO
  ENABLE VALIDATE);
