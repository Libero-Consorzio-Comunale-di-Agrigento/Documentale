ALTER TABLE DATI ADD (
  CONSTRAINT DATI_SENZA_AGGIORN_CC
  CHECK (SENZA_AGGIORNAMENTO IN ('S','N'))
  ENABLE VALIDATE);

ALTER TABLE DATI ADD (
  CONSTRAINT DATI_SENZA_SALVATA_CC
  CHECK (SENZA_SALVATAGGIO IN ('S','N'))
  ENABLE VALIDATE);

ALTER TABLE DATI ADD (
  CONSTRAINT DATI_TESTO_MAIUSCO_CC
  CHECK (TESTO_MAIUSCOLO IN ('S','N'))
  ENABLE VALIDATE);

ALTER TABLE DATI ADD (
  CONSTRAINT PK_DATI
  PRIMARY KEY
  (AREA, DATO)
  USING INDEX PK_DATI
  ENABLE VALIDATE);

