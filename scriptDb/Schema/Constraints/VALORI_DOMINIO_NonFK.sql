ALTER TABLE VALORI_DOMINIO ADD (
  CONSTRAINT PK_VALORI_DOMINIO
  PRIMARY KEY
  (AREA, DOMINIO, CODICE_MODELLO, CODICE)
  USING INDEX PK_VALORI_DOMINIO
  ENABLE VALIDATE);

