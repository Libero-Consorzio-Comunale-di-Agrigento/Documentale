CREATE TABLE DATI_MODELLO
(
  AREA                     VARCHAR2(200 BYTE)   NOT NULL,
  CODICE_MODELLO           VARCHAR2(50 BYTE)    NOT NULL,
  DATO                     VARCHAR2(30 BYTE)    NOT NULL,
  AREA_DATO                VARCHAR2(200 BYTE)   NOT NULL,
  ID_CAMPO                 NUMBER(10),
  ISTRUZIONI_COMPILAZIONE  VARCHAR2(250 BYTE),
  LUNGHEZZA                NUMBER(11)           NOT NULL,
  DECIMALI                 NUMBER(11)           NOT NULL,
  TIPO_CAMPO               VARCHAR2(1 BYTE)     NOT NULL,
  TIPO_ACCESSO             VARCHAR2(1 BYTE),
  CAMPO_CALCOLATO          VARCHAR2(1 BYTE)     DEFAULT 'N',
  BLOCCO                   VARCHAR2(250 BYTE),
  IN_USO                   VARCHAR2(1 BYTE)
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

COMMENT ON TABLE DATI_MODELLO IS 'Tabella di collegamento dati e modelli, contiene anche info sul modo di vedere i dati nel modello.';



