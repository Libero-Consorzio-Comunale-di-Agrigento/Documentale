CREATE TABLE GRUPPI
(
  ID_GRUPPO  NUMBER(10)                         NOT NULL,
  GRUPPO     VARCHAR2(50 BYTE),
  UTENTE     VARCHAR2(20 BYTE)
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

COMMENT ON TABLE GRUPPI IS 'Gruppi di appartenenza dei singoli utenti.';



