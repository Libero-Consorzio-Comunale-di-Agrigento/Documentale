ALTER TABLE BLOCCHI_POPUP ADD (
  CONSTRAINT BLOCCHI_POPUP_CHIUDI_POPUP_CC
  CHECK (CHIUDI_POPUP IN ('S','N'))
  ENABLE VALIDATE);

ALTER TABLE BLOCCHI_POPUP ADD (
  CONSTRAINT BLOCCHI_POPUP_FILTRI_ESTERN_CC
  CHECK (FILTRI_ESTERNI IN ('Y','N'))
  ENABLE VALIDATE);

ALTER TABLE BLOCCHI_POPUP ADD (
  CONSTRAINT BLOCCHI_POPUP_PK
  PRIMARY KEY
  (AREA, BLOCCO)
  USING INDEX BLOCCHI_POPUP_PK
  ENABLE VALIDATE);
