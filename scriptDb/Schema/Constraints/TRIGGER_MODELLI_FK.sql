ALTER TABLE TRIGGER_MODELLI ADD (
  CONSTRAINT TIDO_TRMO_FK 
  FOREIGN KEY (ID_TIPODOC) 
  REFERENCES TIPI_DOCUMENTO (ID_TIPODOC)
  ON DELETE CASCADE
  ENABLE VALIDATE);

