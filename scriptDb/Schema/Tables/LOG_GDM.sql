CREATE TABLE LOG_GDM
(
  ID_LOG       NUMBER(10)                       NOT NULL,
  AZIONE       VARCHAR2(100 BYTE)               NOT NULL,
  DATA         DATE                             NOT NULL,
  UTENTE       VARCHAR2(8 BYTE)                 NOT NULL,
  DESCRIZIONE  VARCHAR2(2000 BYTE)
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


