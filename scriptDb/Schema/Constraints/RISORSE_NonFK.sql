ALTER TABLE RISORSE ADD (
  CONSTRAINT RISORSE_TIPO_OGGETTO_CC
  CHECK (TIPO_OGGETTO IN ('D','Q','C','S'))
  ENABLE VALIDATE);

ALTER TABLE RISORSE ADD (
  CONSTRAINT RISORSE_PK
  PRIMARY KEY
  (ID_RISORSA)
  USING INDEX RISORSE_PK
  ENABLE VALIDATE);

