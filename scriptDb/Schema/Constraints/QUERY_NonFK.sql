ALTER TABLE QUERY ADD (
  CONSTRAINT QUERY_TIPO_CC
  CHECK (TIPO IN ('S','U'))
  ENABLE VALIDATE);

ALTER TABLE QUERY ADD (
  CONSTRAINT QUERY_PK
  PRIMARY KEY
  (ID_QUERY)
  USING INDEX QUERY_PK
  ENABLE VALIDATE);

