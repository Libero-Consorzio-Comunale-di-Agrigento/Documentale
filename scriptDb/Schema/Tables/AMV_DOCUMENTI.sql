CREATE TABLE AMV_DOCUMENTI
(
  ID_DOCUMENTO          NUMBER(10)              NOT NULL,
  ID_TIPOLOGIA          NUMBER(10)              NOT NULL,
  ID_CATEGORIA          NUMBER(10)              NOT NULL,
  ID_ARGOMENTO          NUMBER(10)              NOT NULL,
  ID_RILEVANZA          NUMBER(10)              NOT NULL,
  ID_AREA               NUMBER(10)              NOT NULL,
  TITOLO                VARCHAR2(4000 BYTE)     NOT NULL,
  TIPO_TESTO            VARCHAR2(10 BYTE)       DEFAULT 'Testo'               NOT NULL,
  TESTO                 CLOB,
  LINK                  VARCHAR2(200 BYTE),
  DATA_RIFERIMENTO      DATE                    DEFAULT SYSDATE               NOT NULL,
  INIZIO_PUBBLICAZIONE  DATE,
  FINE_PUBBLICAZIONE    DATE,
  AUTORE                VARCHAR2(8 BYTE),
  DATA_INSERIMENTO      DATE                    DEFAULT SYSDATE,
  UTENTE_AGGIORNAMENTO  VARCHAR2(8 BYTE),
  DATA_AGGIORNAMENTO    DATE                    DEFAULT SYSDATE,
  ID_SEZIONE            NUMBER(10),
  REVISIONE             NUMBER(10)              DEFAULT 0                     NOT NULL,
  STATO                 VARCHAR2(1 BYTE),
  XML                   CLOB,
  IMMAGINE              VARCHAR2(200 BYTE),
  ID_DOCUMENTO_PADRE    NUMBER(10),
  ICONA                 VARCHAR2(200 BYTE),
  SEQUENZA              NUMBER(10),
  ABSTRACT              CLOB
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

COMMENT ON TABLE AMV_DOCUMENTI IS 'DOCU - Documenti da pubblicare';



