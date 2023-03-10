ALTER TABLE DOMINI ADD (
  CONSTRAINT DOMINI_PERSONALIZZAZ_CC
  CHECK (PERSONALIZZAZIONE IN ('S','N'))
  ENABLE VALIDATE);

ALTER TABLE DOMINI ADD (
  CONSTRAINT DOMINI_PRECARICA_CC
  CHECK (PRECARICA IN ('S','P','F','V','E','M','D','C','I'))
  ENABLE VALIDATE);

ALTER TABLE DOMINI ADD (
  CONSTRAINT DOMINI_PK
  PRIMARY KEY
  (AREA, DOMINIO, CODICE_MODELLO)
  USING INDEX DOMINI_PK
  ENABLE VALIDATE);

