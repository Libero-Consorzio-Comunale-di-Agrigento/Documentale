ALTER TABLE LOG_GDM ADD (
  CONSTRAINT LOG_GDM_PK
  PRIMARY KEY
  (ID_LOG)
  USING INDEX LOG_GDM_PK
  ENABLE VALIDATE);

