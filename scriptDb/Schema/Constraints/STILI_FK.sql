ALTER TABLE STILI ADD (
  CONSTRAINT AREE_STIL_FK 
  FOREIGN KEY (AREA) 
  REFERENCES AREE (AREA)
  ON DELETE CASCADE
  ENABLE VALIDATE);

