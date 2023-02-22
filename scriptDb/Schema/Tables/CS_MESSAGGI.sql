CREATE TABLE CS_MESSAGGI
(
  ID_DOCUMENTO   NUMBER(10)                     NOT NULL,
  MESSAGGIO      NUMBER(10)                     NOT NULL,
  DATA_MODIFICA  DATE                           DEFAULT SYSDATE
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


