ALTER TABLE SYSINTEGRATION_MODEL ADD (
  CONSTRAINT SYMO_MODE_FK 
  FOREIGN KEY (AREA, CODICE_MODELLO) 
  REFERENCES MODELLI (AREA,CODICE_MODELLO)
  ON DELETE CASCADE
  ENABLE VALIDATE);

ALTER TABLE SYSINTEGRATION_MODEL ADD (
  CONSTRAINT SYTY_SYMO_FK 
  FOREIGN KEY (TYPE_INTEGRATION) 
  REFERENCES SYSINTEGRATION_TYPE (TYPE_INTEGRATION)
  ON DELETE CASCADE
  ENABLE VALIDATE);

