ALTER TABLE SI4_TIPI_ABILITAZIONE ADD (
  CONSTRAINT SI4_TIPI_ABILITAZIONE_PK
  PRIMARY KEY
  (ID_TIPO_ABILITAZIONE)
  USING INDEX SI4_TIPI_ABILITAZIONE_PK
  ENABLE VALIDATE);

ALTER TABLE SI4_TIPI_ABILITAZIONE ADD (
  CONSTRAINT TIPO_ABILITAZIONE_UK
  UNIQUE (TIPO_ABILITAZIONE)
  USING INDEX TIPO_ABILITAZIONE_UK
  ENABLE VALIDATE);
