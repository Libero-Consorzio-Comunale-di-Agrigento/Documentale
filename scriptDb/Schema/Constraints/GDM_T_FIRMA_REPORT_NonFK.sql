ALTER TABLE GDM_T_FIRMA_REPORT ADD (
  CONSTRAINT GDM$FREP_ID_DOCUMENTO_UK
  UNIQUE (ID_DOCUMENTO)
  USING INDEX GDM$FREP_ID_DOCUMENTO_UK
  ENABLE VALIDATE);

