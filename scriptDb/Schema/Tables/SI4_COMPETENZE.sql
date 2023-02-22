CREATE TABLE SI4_COMPETENZE
(
  ID_COMPETENZA         NUMBER(10)              NOT NULL,
  ID_ABILITAZIONE       NUMBER(10)              NOT NULL,
  UTENTE                VARCHAR2(8 BYTE),
  OGGETTO               VARCHAR2(250 BYTE),
  ACCESSO               VARCHAR2(1 BYTE)        NOT NULL,
  RUOLO                 VARCHAR2(250 BYTE),
  DAL                   DATE,
  AL                    DATE,
  DATA_AGGIORNAMENTO    DATE,
  UTENTE_AGGIORNAMENTO  VARCHAR2(8 BYTE),
  ID_FUNZIONE           NUMBER(10),
  TIPO_COMPETENZA       VARCHAR2(1 BYTE)        DEFAULT 'U'                   NOT NULL
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

COMMENT ON TABLE SI4_COMPETENZE IS 'COMP - Competenze su Oggetti del DataBase';

COMMENT ON COLUMN SI4_COMPETENZE.ACCESSO IS 'Abilitazione di accesso sulla informazione';



