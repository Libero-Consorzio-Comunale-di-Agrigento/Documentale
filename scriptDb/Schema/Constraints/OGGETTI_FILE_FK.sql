ALTER TABLE OGGETTI_FILE ADD (
  CONSTRAINT FOFI_OGFI_FK 
  FOREIGN KEY (ID_FORMATO) 
  REFERENCES FORMATI_FILE (ID_FORMATO)
  ENABLE VALIDATE);

ALTER TABLE OGGETTI_FILE ADD (
  CONSTRAINT OGFI_DOCU 
  FOREIGN KEY (ID_DOCUMENTO) 
  REFERENCES DOCUMENTI (ID_DOCUMENTO)
  ON DELETE CASCADE
  ENABLE VALIDATE);

ALTER TABLE OGGETTI_FILE ADD (
  CONSTRAINT OGFI_OGFI 
  FOREIGN KEY (ID_OGGETTO_FILE_PADRE) 
  REFERENCES OGGETTI_FILE (ID_OGGETTO_FILE)
  ON DELETE CASCADE
  ENABLE VALIDATE);

