ALTER TABLE GDM_T_REPORT_FOOTER ADD (
  CONSTRAINT GDM$REPF_ID_DOCUMENTO_UK
  UNIQUE (ID_DOCUMENTO)
  USING INDEX GDM$REPF_ID_DOCUMENTO_UK
  ENABLE VALIDATE);

