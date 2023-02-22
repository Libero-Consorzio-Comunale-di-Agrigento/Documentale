ALTER TABLE MODELLI_IMPORT ADD (
  CONSTRAINT MODE_MOIM_FK 
  FOREIGN KEY (AREA, CODICE_MODELLO) 
  REFERENCES MODELLI (AREA,CODICE_MODELLO)
  ON DELETE CASCADE
  ENABLE VALIDATE);

