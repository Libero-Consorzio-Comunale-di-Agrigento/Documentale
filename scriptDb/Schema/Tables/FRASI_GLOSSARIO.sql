CREATE TABLE FRASI_GLOSSARIO
(
  ID_FRASE        NUMBER(10)                    NOT NULL,
  AREA            VARCHAR2(200 BYTE),
  ID_FRASE_PADRE  NUMBER(10),
  PARAMETRICA     VARCHAR2(1 BYTE)              DEFAULT 'N',
  TITOLO          VARCHAR2(250 BYTE),
  FRASE           VARCHAR2(2000 BYTE),
  BLOCCO          VARCHAR2(250 BYTE)
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


