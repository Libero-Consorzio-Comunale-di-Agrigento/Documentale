ALTER TABLE TIPI_PRATICHE ADD (
  CONSTRAINT TIPI_PRATICHE_IS_DEFAULT_CC
  CHECK (IS_DEFAULT IS NULL OR (IS_DEFAULT IN ('1','0')))
  ENABLE VALIDATE);

ALTER TABLE TIPI_PRATICHE ADD (
  CONSTRAINT TIPI_PRATICHE_PK
  PRIMARY KEY
  (ID_TIPO_PRATICA)
  USING INDEX TIPI_PRATICHE_PK
  ENABLE VALIDATE);

