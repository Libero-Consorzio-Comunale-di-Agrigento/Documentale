ALTER TABLE COLLEGAMENTI_ESTERNI ADD (
  CONSTRAINT COLLEGAMENTI__TIPO_LINK_CC
  CHECK (TIPO_LINK in ('E','I'))
  ENABLE VALIDATE);

ALTER TABLE COLLEGAMENTI_ESTERNI ADD (
  CONSTRAINT COLLEGAMENTI_ESTERNI_PK
  PRIMARY KEY
  (ID_COLLEGAMENTO)
  USING INDEX COLLEGAMENTI_ESTERNI_PK
  ENABLE VALIDATE);

