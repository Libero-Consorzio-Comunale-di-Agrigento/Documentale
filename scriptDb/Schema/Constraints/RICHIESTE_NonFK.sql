ALTER TABLE RICHIESTE ADD (
  CONSTRAINT PK_RICHIESTE
  PRIMARY KEY
  (CODICE_RICHIESTA, AREA)
  USING INDEX PK_RICHIESTE
  ENABLE VALIDATE);

