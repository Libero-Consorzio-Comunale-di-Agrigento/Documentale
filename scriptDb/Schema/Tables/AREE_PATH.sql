CREATE TABLE AREE_PATH
(
  ID_PATH_AREE_FILE          NUMBER(10)         NOT NULL,
  PATH_FILE                  VARCHAR2(1000 BYTE) NOT NULL,
  PATH_FILE_ORACLE           VARCHAR2(1000 BYTE) NOT NULL,
  PREFIX_ACRONIMO_DIRECTORY  VARCHAR2(20 BYTE)  NOT NULL
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


