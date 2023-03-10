ALTER TABLE BLOCCHI ADD (
  CONSTRAINT BLOC_MODE_FK 
  FOREIGN KEY (AREA, CODICE_MODELLO) 
  REFERENCES MODELLI (AREA,CODICE_MODELLO)
  ON DELETE CASCADE
  ENABLE VALIDATE);

ALTER TABLE BLOCCHI ADD (
  CONSTRAINT CONN_BLOC_FK 
  FOREIGN KEY (DSN) 
  REFERENCES CONNESSIONI (DSN)
  ENABLE VALIDATE);

