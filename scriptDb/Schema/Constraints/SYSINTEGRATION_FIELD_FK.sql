ALTER TABLE SYSINTEGRATION_FIELD ADD (
  CONSTRAINT SYMO_SYFI_FK 
  FOREIGN KEY (TYPE_INTEGRATION, AREA, CODICE_MODELLO) 
  REFERENCES SYSINTEGRATION_MODEL (TYPE_INTEGRATION,AREA,CODICE_MODELLO)
  ON DELETE CASCADE
  ENABLE VALIDATE);

