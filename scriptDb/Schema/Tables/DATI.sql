CREATE TABLE DATI
(
  AREA                     VARCHAR2(200 BYTE)   NOT NULL,
  DATO                     VARCHAR2(30 BYTE)    NOT NULL,
  TIPO                     VARCHAR2(1 BYTE)     NOT NULL,
  DOMINIO                  VARCHAR2(50 BYTE),
  DOMINIO_FORMULA          VARCHAR2(50 BYTE),
  DOMINIO_VISUALIZZA       VARCHAR2(50 BYTE),
  LUNGHEZZA                NUMBER               NOT NULL,
  DECIMALI                 NUMBER               NOT NULL,
  NOTE                     VARCHAR2(4000 BYTE),
  ISTRUZIONI_COMPILAZIONE  CLOB,
  OPZIONI                  VARCHAR2(10 BYTE),
  FORMATO_DATA             VARCHAR2(30 BYTE),
  LABEL                    VARCHAR2(30 BYTE),
  TIPO_LOG                 VARCHAR2(1 BYTE),
  DATA_AGGIORNAMENTO       DATE                 DEFAULT SYSDATE               NOT NULL,
  UTENTE_AGGIORNAMENTO     VARCHAR2(8 BYTE)     DEFAULT 'GDM'                 NOT NULL,
  SENZA_SALVATAGGIO        VARCHAR2(1 BYTE)     DEFAULT 'N'                   NOT NULL,
  SENZA_AGGIORNAMENTO      VARCHAR2(1 BYTE)     DEFAULT 'N'                   NOT NULL,
  TESTO_MAIUSCOLO          VARCHAR2(1 BYTE)     DEFAULT 'N'                   NOT NULL,
  ACRONIMO_DATO            VARCHAR2(5 BYTE)
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

COMMENT ON TABLE DATI IS 'Dizionario effettivo dei dati.';



