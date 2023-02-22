CREATE TABLE COLLEGAMENTI_ESTERNI
(
  ID_COLLEGAMENTO             NUMBER(10)        NOT NULL,
  NOME                        VARCHAR2(100 BYTE) NOT NULL,
  URL                         VARCHAR2(1000 BYTE) NOT NULL,
  ICONA                       VARCHAR2(250 BYTE) NOT NULL,
  TOOLTIP                     VARCHAR2(250 BYTE),
  TIPO_LINK                   VARCHAR2(1 BYTE)  DEFAULT 'E'                   NOT NULL,
  CODICEADS                   VARCHAR2(100 BYTE),
  DATA_AGGIORNAMENTO          DATE              NOT NULL,
  UTENTE_AGGIORNAMENTO        VARCHAR2(8 BYTE)  NOT NULL,
  ID_COLLEGAMENTO_SOSTITUITO  NUMBER(10),
  FUNZIONE_LETTURA            VARCHAR2(4000 BYTE)
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


