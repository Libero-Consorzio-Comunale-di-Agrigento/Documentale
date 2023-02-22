CREATE TABLE MODELLI
(
  AREA                        VARCHAR2(200 BYTE) NOT NULL,
  CODICE_MODELLO              VARCHAR2(50 BYTE) NOT NULL,
  ID_TIPODOC                  NUMBER(10),
  CODICE_MODELLO_PADRE        VARCHAR2(50 BYTE),
  DATA_REVISIONE              DATE,
  VALIDO                      VARCHAR2(1 BYTE),
  AUTORE                      VARCHAR2(100 BYTE),
  DATA_INSERIMENTO            DATE,
  DATA_VARIAZIONE             DATE,
  DATA_PUBBLICAZIONE          DATE,
  NOTE_INTERNE                VARCHAR2(250 BYTE),
  NOTE                        VARCHAR2(250 BYTE),
  ISTRUZIONI                  CLOB,
  DTD                         VARCHAR2(250 BYTE),
  MODELLO                     CLOB,
  TIPO                        VARCHAR2(10 BYTE),
  OGGETTO                     VARCHAR2(250 BYTE),
  CLASSIFICAZIONE             VARCHAR2(250 BYTE),
  MODELLO_SUCCESSIVO          VARCHAR2(50 BYTE),
  FILE_ORIGINALE              CLOB,
  TIPO_USO                    VARCHAR2(1 BYTE)  DEFAULT 'X'                   NOT NULL,
  PAGINE                      NUMBER(2)         DEFAULT 1                     NOT NULL,
  WWWC                        VARCHAR2(1 BYTE)  DEFAULT 'N'                   NOT NULL,
  STILE                       VARCHAR2(50 BYTE),
  POS_PULSANTI                VARCHAR2(1 BYTE)  DEFAULT 'B'                   NOT NULL,
  REINDIRIZZAMENTO            VARCHAR2(30 BYTE),
  PROTOCOLLO                  VARCHAR2(1 BYTE)  DEFAULT 'N'                   NOT NULL,
  UTENTE_AGGIORNAMENTO        VARCHAR2(8 BYTE)  DEFAULT 'GDM'                 NOT NULL,
  BLOCCO_JDMS                 VARCHAR2(250 BYTE),
  NUM_MAX_ALLEGATI            NUMBER(3),
  BLOCCO_STAMPA_JDMS          VARCHAR2(250 BYTE),
  STR_CAMPI_OBBLIG            VARCHAR2(2000 BYTE),
  DIM_MAX_ALL_BYTE            NUMBER(10),
  DIM_MAX_ALL_BYTE_BLOCCANTE  VARCHAR2(1 BYTE)  DEFAULT 'Y'                   NOT NULL,
  GENERAZIONE_OCR             VARCHAR2(1 BYTE)  DEFAULT 'N'                   NOT NULL,
  VIEW_ALLEGATI_PADRE         VARCHAR2(1 BYTE)  DEFAULT 'Y',
  ID_SERVIZIO_GDMSYNCRO       NUMBER(10)
)
TABLESPACE GDM
PCTUSED    0
PCTFREE    10
INITRANS   1
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MAXSIZE          UNLIMITED
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           );

COMMENT ON TABLE MODELLI IS 'Tabella dei template dei modelli.';



