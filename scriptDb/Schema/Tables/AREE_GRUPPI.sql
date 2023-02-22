CREATE TABLE AREE_GRUPPI
(
  GRUPPO  VARCHAR2(8 BYTE)                      NOT NULL,
  AREA    VARCHAR2(250 BYTE)                    NOT NULL
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


