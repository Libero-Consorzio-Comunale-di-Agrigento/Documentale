CREATE TABLE OPERAZIONI_DI_INOLTRO
(
  ID_OP                 NUMBER(6)               NOT NULL,
  SEQUENZA              NUMBER                  NOT NULL,
  AREA                  VARCHAR2(200 BYTE)      NOT NULL,
  ID_TIPO_PRATICA       NUMBER(10),
  CODICE_MODELLO        VARCHAR2(50 BYTE),
  CLASSNAME             VARCHAR2(200 BYTE),
  PARAMETRI             CLOB,
  INOLTRO_SINGOLO       VARCHAR2(1 BYTE)        DEFAULT 'N'                   NOT NULL,
  DSN                   VARCHAR2(50 BYTE),
  DATA_AGGIORNAMENTO    DATE                    DEFAULT SYSDATE               NOT NULL,
  UTENTE_AGGIORNAMENTO  VARCHAR2(8 BYTE)        DEFAULT 'GDM'                 NOT NULL
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

COMMENT ON TABLE OPERAZIONI_DI_INOLTRO IS 'Se esiste un valore per id_tipo_pratica l''operazione di inoltro si riferisce ad uno specifico tipo di pratica altrimenti si riferisce ad un area.';



