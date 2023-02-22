ALTER TABLE VALORI_LOG ADD (
  CONSTRAINT VALOG_ACTIVITY_FK 
  FOREIGN KEY (ID_LOG) 
  REFERENCES ACTIVITY_LOG (ID_LOG)
  ON DELETE CASCADE
  ENABLE VALIDATE);

ALTER TABLE VALORI_LOG ADD (
  CONSTRAINT VALOG_VALO_FK 
  FOREIGN KEY (ID_VALORE) 
  REFERENCES VALORI (ID_VALORE)
  ENABLE VALIDATE);
