ALTER TABLE MODELLI_TIPI_PRATICHE ADD (
  CONSTRAINT MODE_MOTP_FK 
  FOREIGN KEY (AREA, CODICE_MODELLO) 
  REFERENCES MODELLI (AREA,CODICE_MODELLO)
  ON DELETE CASCADE
  ENABLE VALIDATE);

ALTER TABLE MODELLI_TIPI_PRATICHE ADD (
  CONSTRAINT TIPR_MOTP_FK 
  FOREIGN KEY (ID_PRATICA) 
  REFERENCES TIPI_PRATICHE (ID_TIPO_PRATICA)
  ON DELETE CASCADE
  ENABLE VALIDATE);
