ALTER TABLE GDM_T_FIRMA_REPORT ADD (
  CONSTRAINT GDM$FREP_DOCU_FK 
  FOREIGN KEY (ID_DOCUMENTO) 
  REFERENCES DOCUMENTI (ID_DOCUMENTO)
  ON DELETE CASCADE
  ENABLE VALIDATE);

