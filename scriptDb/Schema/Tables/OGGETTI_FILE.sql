CREATE TABLE OGGETTI_FILE
(
  ID_OGGETTO_FILE                NUMBER(10)     NOT NULL,
  ID_DOCUMENTO                   NUMBER(10)     NOT NULL,
  ID_OGGETTO_FILE_PADRE          NUMBER(10),
  ID_FORMATO                     NUMBER(10)     NOT NULL,
  FILENAME                       VARCHAR2(200 BYTE) NOT NULL,
  "FILE"                         BFILE,
  TESTOOCR                       BLOB,
  ALLEGATO                       VARCHAR2(1 BYTE) NOT NULL,
  DATA_AGGIORNAMENTO             DATE           NOT NULL,
  UTENTE_AGGIORNAMENTO           VARCHAR2(8 BYTE) NOT NULL,
  DA_CANCELLARE                  VARCHAR2(1 BYTE) DEFAULT 'N' NOT NULL,
  PATH_FILE                      VARCHAR2(1000 BYTE),
  OCR_FILE                       CLOB,
  OCR_PENDING                    NUMBER(1),
  DATA_INSERIMENTO               DATE,
  DATA_ULTIMA_ESTERNALIZZAZIONE  DATE,
  ID_SYNCRO                      NUMBER(10),
  CHIAVE_SERVIZIO_ESTERNO        VARCHAR2(200 BYTE),
  ID_SERVIZIO_ESTERNO            NUMBER(10),
  PATH_FILE_ROOT                 VARCHAR2(1000 BYTE),
  PATH_FILE_ROOT_ORACLE          VARCHAR2(1000 BYTE)
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


