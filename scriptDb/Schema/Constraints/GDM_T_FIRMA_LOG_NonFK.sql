ALTER TABLE GDM_T_FIRMA_LOG ADD (
  CONSTRAINT GDM$FLOG_ID_DOCUMENTO_UK
  UNIQUE (ID_DOCUMENTO)
  USING INDEX GDM$FLOG_ID_DOCUMENTO_UK
  ENABLE VALIDATE);

