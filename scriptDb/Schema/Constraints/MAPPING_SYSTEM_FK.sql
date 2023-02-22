ALTER TABLE MAPPING_SYSTEM ADD (
  CONSTRAINT MASY_APPL 
  FOREIGN KEY (ID_APPLICATIVO) 
  REFERENCES APPLICATIVI (ID_APPLICATIVO)
  ENABLE VALIDATE);

ALTER TABLE MAPPING_SYSTEM ADD (
  CONSTRAINT MASY_DMSY 
  FOREIGN KEY (ID_DM) 
  REFERENCES DM_SYSTEM (ID_DM)
  ENABLE VALIDATE);

ALTER TABLE MAPPING_SYSTEM ADD (
  CONSTRAINT MASY_ENTI 
  FOREIGN KEY (ID_ENTE) 
  REFERENCES ENTI (ID_ENTE)
  ENABLE VALIDATE);

