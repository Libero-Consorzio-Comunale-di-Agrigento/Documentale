ALTER TABLE ALLEGATI_XML ADD (
  CONSTRAINT ALLE_ALXM_FK 
  FOREIGN KEY (ALL_CODICE_RICHIESTA, ALL_AREA, ALL_CODICE_ALLEGATO) 
  REFERENCES ALLEGATI (CODICE_RICHIESTA,AREA,CODICE_ALLEGATO)
  ON DELETE CASCADE
  ENABLE VALIDATE);

ALTER TABLE ALLEGATI_XML ADD (
  CONSTRAINT XML_ALXM_FK 
  FOREIGN KEY (CODICE_XML, AREA, CODICE_RICHIESTA) 
  REFERENCES XML (CODICE_XML,AREA,CODICE_RICHIESTA)
  ON DELETE CASCADE
  ENABLE VALIDATE);
