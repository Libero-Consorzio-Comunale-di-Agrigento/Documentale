ALTER TABLE GRAFICI_MODELLO ADD (
  CONSTRAINT MODE_GRMO_FK 
  FOREIGN KEY (AREA, CODICE_MODELLO) 
  REFERENCES MODELLI (AREA,CODICE_MODELLO)
  ON DELETE CASCADE
  ENABLE VALIDATE);
