ALTER TABLE GDM_W_WORKSPACESTANDARD ADD (
  CONSTRAINT GDM$WSTD_ID_DOCUMENTO_UK
  UNIQUE (ID_DOCUMENTO)
  USING INDEX GDM$WSTD_ID_DOCUMENTO_UK
  ENABLE VALIDATE);
