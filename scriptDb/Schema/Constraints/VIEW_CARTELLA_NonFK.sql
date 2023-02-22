ALTER TABLE VIEW_CARTELLA ADD (
  CONSTRAINT VIEW_CARTELLA_TIPO_VISUALIZ_CC
  CHECK (TIPO_VISUALIZZAZIONE IN ('A','P'))
  ENABLE VALIDATE);

ALTER TABLE VIEW_CARTELLA ADD (
  CONSTRAINT VIEW_CARTELLA_PK
  PRIMARY KEY
  (ID_VIEWCARTELLA)
  USING INDEX VIEW_CARTELLA_PK
  ENABLE VALIDATE);

