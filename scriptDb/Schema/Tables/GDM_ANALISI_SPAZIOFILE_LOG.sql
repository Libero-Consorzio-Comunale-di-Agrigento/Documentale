CREATE TABLE GDM_ANALISI_SPAZIOFILE_LOG
(
  TIPO_MODELLO         VARCHAR2(100 BYTE),
  TIPO_OGGETTO_FILE    VARCHAR2(100 BYTE),
  POSIZIONE            VARCHAR2(50 BYTE),
  NUMERO_OGGETTI_FILE  NUMBER(10),
  DIMENSIONE_MB        NUMBER(10,2),
  PAR_RICERCA          VARCHAR2(2000 BYTE),
  DATA_ELABORAZIONE    DATE
)
TABLESPACE GDM
PCTUSED    0
PCTFREE    10
INITRANS   1
MAXTRANS   255
STORAGE    (
            MAXSIZE          UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           );


