ALTER TABLE ALLEGATI_TEMP ADD (
  CONSTRAINT ALLEGATI_TEMP_PK
  PRIMARY KEY
  (AREA, CODICE_MODELLO, CODICE_RICHIESTA, NOMEFILE)
  USING INDEX ALLEGATI_TEMP_PK
  ENABLE VALIDATE);

