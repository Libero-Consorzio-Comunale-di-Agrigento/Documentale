ALTER TABLE DIRITTI_DI_ACCESSO ADD (
  CONSTRAINT PK_DIRITTI_DI_ACCESSO
  PRIMARY KEY
  (CODICE_RICHIESTA, AREA, UTENTE)
  USING INDEX PK_DIRITTI_DI_ACCESSO
  ENABLE VALIDATE);

