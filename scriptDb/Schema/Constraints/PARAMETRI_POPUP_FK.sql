ALTER TABLE PARAMETRI_POPUP ADD (
  CONSTRAINT BLPO_PAPO_FK 
  FOREIGN KEY (AREA, BLOCCO) 
  REFERENCES BLOCCHI_POPUP (AREA,BLOCCO)
  ON DELETE CASCADE
  ENABLE VALIDATE);
