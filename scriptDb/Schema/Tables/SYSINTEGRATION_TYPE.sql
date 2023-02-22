CREATE TABLE SYSINTEGRATION_TYPE
(
  TYPE_INTEGRATION            VARCHAR2(50 CHAR) NOT NULL,
  URLSERVICE                  VARCHAR2(4000 CHAR) NOT NULL,
  DESCRIPTION                 VARCHAR2(200 CHAR),
  XML_OTHERKEY_DOCUMENT_RULE  VARCHAR2(4000 CHAR),
  CLASSIMPLEMENTATION         VARCHAR2(100 BYTE) NOT NULL,
  ACTIVE                      NUMBER(1)         DEFAULT 1,
  NOTES                       VARCHAR2(2000 CHAR)
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

COMMENT ON COLUMN SYSINTEGRATION_TYPE.TYPE_INTEGRATION IS 'Esempio: CRV-DOCS, HUMMINGBIRD';

COMMENT ON COLUMN SYSINTEGRATION_TYPE.URLSERVICE IS 'Esempio: http://10.20.101.10:8080/ESBConsole/jbi/DOCSServiceWSPort/main.wsdl';

COMMENT ON COLUMN SYSINTEGRATION_TYPE.XML_OTHERKEY_DOCUMENT_RULE IS 'Default vuoto.
Altrimenti descritto da questo tipo di xml:
<root>
  <object table=¿OGGETTI_FILE¿>ID_OGGETTO_FILE</object>
   ¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿.
   ¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿.
</root>
XML descrittivo che serve a stabilire qual ¿ la seconda chiave della tabella SINCRO_DOCUMENT. Se vuoto la seconda chiave sar¿ di conseguenza il solo id_documento altrimenti sar¿ quanto descritto nell¿xml (nell¿esempio c¿¿ anche l¿id oggetto file¿.esempio che va bene per crv)
';

COMMENT ON COLUMN SYSINTEGRATION_TYPE.CLASSIMPLEMENTATION IS 'Nome della classe che implemeter¿ il lancio del servizio';



