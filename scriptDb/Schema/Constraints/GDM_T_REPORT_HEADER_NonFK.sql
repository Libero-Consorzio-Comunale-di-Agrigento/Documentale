ALTER TABLE GDM_T_REPORT_HEADER ADD (
  CONSTRAINT GDM$REPH_ID_DOCUMENTO_UK
  UNIQUE (ID_DOCUMENTO)
  USING INDEX GDM$REPH_ID_DOCUMENTO_UK
  ENABLE VALIDATE);
