ALTER TABLE CS_MESSAGGI ADD (
  CONSTRAINT CS_MESSAGGI_PK
  PRIMARY KEY
  (ID_DOCUMENTO, MESSAGGIO)
  USING INDEX CS_MESSAGGI_PK
  ENABLE VALIDATE);

