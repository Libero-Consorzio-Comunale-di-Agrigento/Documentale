ALTER TABLE GDM_ELAB_BATCH ADD (
  CONSTRAINT GDM$ELBT_DOCU_FK 
  FOREIGN KEY (ID_DOCUMENTO) 
  REFERENCES DOCUMENTI (ID_DOCUMENTO)
  ON DELETE CASCADE
  ENABLE VALIDATE);

