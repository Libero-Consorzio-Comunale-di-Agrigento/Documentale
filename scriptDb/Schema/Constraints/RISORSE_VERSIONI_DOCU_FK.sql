ALTER TABLE RISORSE_VERSIONI_DOCU ADD (
  CONSTRAINT VEDO_RIVE_FK 
  FOREIGN KEY (ID_DOCUMENTO, VERSIONE) 
  REFERENCES VERSIONI_DOCUMENTI (ID_DOCUMENTO,VERSIONE)
  ON DELETE CASCADE
  ENABLE VALIDATE);

