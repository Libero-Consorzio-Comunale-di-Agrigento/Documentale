ALTER TABLE GDM_Q_QUERYSTANDARD ADD (
  CONSTRAINT GDM$QSTD_ID_DOCUMENTO_UK
  UNIQUE (ID_DOCUMENTO)
  USING INDEX GDM$QSTD_ID_DOCUMENTO_UK
  ENABLE VALIDATE);

