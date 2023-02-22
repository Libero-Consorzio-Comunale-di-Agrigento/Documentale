CREATE TABLE JDMS_LINK
(
  ID_TIPODOC            NUMBER(10)              NOT NULL,
  TAG                   VARCHAR2(4 BYTE)        NOT NULL,
  URL                   VARCHAR2(1000 BYTE)     NOT NULL,
  ICONA                 VARCHAR2(250 BYTE),
  TOOLTIP               VARCHAR2(250 BYTE),
  UTENTE_AGGIORNAMENTO  VARCHAR2(8 BYTE)        DEFAULT 'GDM'                 NOT NULL,
  DATA_AGGIORNAMENTO    DATE                    DEFAULT SYSDATE               NOT NULL,
  ICONA_EXP             VARCHAR2(4000 BYTE)
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


