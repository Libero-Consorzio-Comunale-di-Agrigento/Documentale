ALTER TABLE GDM_T_MODELLO_TESTI ADD (
  CONSTRAINT GDM$MTES_DOCU_FK 
  FOREIGN KEY (ID_DOCUMENTO) 
  REFERENCES DOCUMENTI (ID_DOCUMENTO)
  ON DELETE CASCADE
  ENABLE VALIDATE);

