ALTER TABLE VERSIONI_DOCUMENTI ADD (
  CONSTRAINT DOCU_VEDO_FK 
  FOREIGN KEY (ID_DOCUMENTO) 
  REFERENCES DOCUMENTI (ID_DOCUMENTO)
  ON DELETE CASCADE
  ENABLE VALIDATE);

