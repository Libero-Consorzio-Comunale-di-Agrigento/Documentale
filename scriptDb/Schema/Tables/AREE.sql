CREATE TABLE AREE
(
  ID_AREA                     NUMBER(10)        NOT NULL,
  AREA                        VARCHAR2(200 BYTE) NOT NULL,
  DESCRIZIONE                 VARCHAR2(250 BYTE),
  GESTIONE_MODELLI            VARCHAR2(1 BYTE)  DEFAULT 'V'                   NOT NULL,
  ACRONIMO                    VARCHAR2(3 BYTE),
  PATH_FILE                   VARCHAR2(1000 BYTE),
  PATH_FILE_ORACLE            VARCHAR2(1000 BYTE),
  FORCE_FILE_ON_BLOB          NUMBER(1)         DEFAULT 0,
  DIM_MAX_ALL_BYTE            NUMBER(10),
  DIM_MAX_ALL_BYTE_BLOCCANTE  VARCHAR2(1 BYTE)  DEFAULT 'Y'                   NOT NULL,
  GENERAZIONE_OCR             VARCHAR2(1 BYTE)  DEFAULT 'N'                   NOT NULL,
  ID_PATH_AREE                NUMBER(10)
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

COMMENT ON TABLE AREE IS 'Aree disponibili.';



