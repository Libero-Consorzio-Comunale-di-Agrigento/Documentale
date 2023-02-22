CREATE TABLE PERSONALIZZAZIONI
(
  ID_PERSONALIZZAZIONE  NUMBER(10)              NOT NULL,
  TIPO_OGGETTO          VARCHAR2(30 BYTE)       NOT NULL,
  ORIGINALE             VARCHAR2(2000 BYTE)     NOT NULL,
  PERSONALIZZATO        VARCHAR2(2000 BYTE)     NOT NULL,
  RIFERIMENTO           VARCHAR2(2000 BYTE),
  ATTIVO                VARCHAR2(1 BYTE)        DEFAULT 'N'                   NOT NULL
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


