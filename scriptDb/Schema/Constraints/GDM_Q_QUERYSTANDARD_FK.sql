ALTER TABLE GDM_Q_QUERYSTANDARD ADD (
  CONSTRAINT GDM$QSTD_DOCU_FK 
  FOREIGN KEY (ID_DOCUMENTO) 
  REFERENCES DOCUMENTI (ID_DOCUMENTO)
  ON DELETE CASCADE
  ENABLE VALIDATE);
