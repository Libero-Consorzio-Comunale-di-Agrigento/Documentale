ALTER TABLE AREE ADD (
  FOREIGN KEY (ID_PATH_AREE) 
  REFERENCES AREE_PATH (ID_PATH_AREE_FILE)
  ON DELETE SET NULL
  ENABLE VALIDATE);

