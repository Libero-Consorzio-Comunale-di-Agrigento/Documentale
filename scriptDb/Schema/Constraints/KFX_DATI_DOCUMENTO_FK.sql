ALTER TABLE KFX_DATI_DOCUMENTO ADD (
  CONSTRAINT PEDO_DADO_FK 
  FOREIGN KEY (ID_DOCUMENTO) 
  REFERENCES KFX_PERCORSI_DOCUMENTI (ID_DOCUMENTO)
  ON DELETE CASCADE
  ENABLE VALIDATE);

