CREATE TABLE TIPI_PRATICHE
(
  ID_TIPO_PRATICA       NUMBER(10)              NOT NULL,
  AREA                  VARCHAR2(200 BYTE)      NOT NULL,
  MOD_AREA              VARCHAR2(200 BYTE),
  CODICE_MODELLO        VARCHAR2(50 BYTE),
  CODICE_TIPO_PRATICA   VARCHAR2(50 BYTE)       NOT NULL,
  IS_DEFAULT            VARCHAR2(1 BYTE)        DEFAULT '0',
  DESCRIZIONE_TIPO      VARCHAR2(2000 BYTE),
  OGGETTO               VARCHAR2(2000 BYTE),
  TIPO_DOCUMENTO        VARCHAR2(50 BYTE),
  MOVIMENTO             VARCHAR2(50 BYTE),
  CLASSIFICAZIONE       VARCHAR2(40 BYTE),
  SMISTAMENTO           VARCHAR2(50 BYTE),
  UNITA_PROTOCOLLANTE   VARCHAR2(50 BYTE),
  UTENTE                VARCHAR2(50 BYTE),
  APPLICATIVO           VARCHAR2(50 BYTE),
  DOCUMENTO_PRINCIPALE  VARCHAR2(50 BYTE),
  RICHIESTA_INOLTRO     VARCHAR2(1 BYTE),
  MITTENTE              VARCHAR2(50 BYTE)
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

COMMENT ON TABLE TIPI_PRATICHE IS 'Tabella che rappresenta i tipi d pratiche possibili.';



