ALTER TABLE GDM_ANALISI_SPAZIOFILE ADD (
  CONSTRAINT GDM_ANALISI_SPAZIOFILE_PK
  PRIMARY KEY
  (TIPO_MODELLO, TIPO_OGGETTO_FILE, POSIZIONE)
  USING INDEX GDM_ANALISI_SPAZIOFILE_PK
  ENABLE VALIDATE);

