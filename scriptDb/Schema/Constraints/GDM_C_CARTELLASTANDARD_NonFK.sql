ALTER TABLE GDM_C_CARTELLASTANDARD ADD (
  CONSTRAINT GDM$CSTD_ID_DOCUMENTO_UK
  UNIQUE (ID_DOCUMENTO)
  USING INDEX GDM$CSTD_ID_DOCUMENTO_UK
  ENABLE VALIDATE);
