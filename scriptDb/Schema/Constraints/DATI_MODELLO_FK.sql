ALTER TABLE DATI_MODELLO ADD (
  CONSTRAINT BLPO_DAMO_FK 
  FOREIGN KEY (AREA, BLOCCO) 
  REFERENCES BLOCCHI_POPUP (AREA,BLOCCO)
  ON DELETE SET NULL
  ENABLE VALIDATE);

ALTER TABLE DATI_MODELLO ADD (
  CONSTRAINT CADO_DAMO_FK 
  FOREIGN KEY (ID_CAMPO) 
  REFERENCES CAMPI_DOCUMENTO (ID_CAMPO)
  ENABLE VALIDATE);

ALTER TABLE DATI_MODELLO ADD (
  CONSTRAINT DATI_DAMO_FK 
  FOREIGN KEY (AREA_DATO, DATO) 
  REFERENCES DATI (AREA,DATO)
  ENABLE VALIDATE);

ALTER TABLE DATI_MODELLO ADD (
  CONSTRAINT MODE_DAMO_FK 
  FOREIGN KEY (AREA, CODICE_MODELLO) 
  REFERENCES MODELLI (AREA,CODICE_MODELLO)
  ON DELETE CASCADE
  ENABLE VALIDATE);

