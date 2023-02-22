ALTER TABLE CATEGORIE_MODELLO ADD (
  CONSTRAINT AREA_CM_FK 
  FOREIGN KEY (AREA, CODICE_MODELLO) 
  REFERENCES MODELLI (AREA,CODICE_MODELLO)
  ON DELETE CASCADE
  ENABLE VALIDATE);

ALTER TABLE CATEGORIE_MODELLO ADD (
  CONSTRAINT CATEGORIA_FK 
  FOREIGN KEY (CATEGORIA) 
  REFERENCES CATEGORIE (CATEGORIA)
  ON DELETE CASCADE
  ENABLE VALIDATE);
