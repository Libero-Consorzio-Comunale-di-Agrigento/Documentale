ALTER TABLE BLOCCHI_MODELLO ADD (
  CONSTRAINT BLMO_BLOC_FK 
  FOREIGN KEY (BLOCCO, AREA_BLOCCO) 
  REFERENCES BLOCCHI (BLOCCO,AREA)
  ENABLE VALIDATE);

ALTER TABLE BLOCCHI_MODELLO ADD (
  CONSTRAINT BLMO_MODE_FK 
  FOREIGN KEY (AREA, CODICE_MODELLO) 
  REFERENCES MODELLI (AREA,CODICE_MODELLO)
  ON DELETE CASCADE
  ENABLE VALIDATE);
