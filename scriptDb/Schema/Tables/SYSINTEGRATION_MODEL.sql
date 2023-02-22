CREATE TABLE SYSINTEGRATION_MODEL
(
  TYPE_INTEGRATION       VARCHAR2(50 CHAR)      NOT NULL,
  AREA                   VARCHAR2(200 CHAR)     NOT NULL,
  CODICE_MODELLO         VARCHAR2(50 CHAR)      NOT NULL,
  XML_SYNCRODESCR        VARCHAR2(4000 CHAR)    NOT NULL,
  XML_DECODEREMOTEMODEL  VARCHAR2(4000 CHAR)    NOT NULL,
  LOGITEM                VARCHAR2(1 BYTE)       NOT NULL
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

COMMENT ON COLUMN SYSINTEGRATION_MODEL.XML_SYNCRODESCR IS 'Campo descritto da questo tipo di xml:
<root>
  <type>S</type>
  <object>
     <type>ACTION</type>
     <identifier>REGISTRA_CRVDOCS</ identifier >
  </object>
</root>
XML descrittivo che serve a stabilire in che modo verr¿ fatta la sincronizzazione fra un modello sul GDM ed un certo tipo di integrazione.
Type conterr¿ valore:
   A > Asincrono
   S > Sincrono
Object > Type conterr¿ valore:
   ACTION,FLUSSO
Object > Identifier conterr¿ valore:
   Nome della action o id del flusso
Ovviamente questi valori potranno essere estesi ad altri casi/oggetti atti alla sincronizzazione.
';

COMMENT ON COLUMN SYSINTEGRATION_MODEL.XML_DECODEREMOTEMODEL IS 'Campo descritto da questo tipo di xml:
<root>
  <type>NUMBER</type>
  <identifier>897</ identifier >
</root>
XML descrittivo che serve a stabilire la corrispondenza univoca fra l¿oggetto MODELLO del GDM e il relativo MODELLO nel sistema remoto che potr¿ essere identificato attraverso un numero o un char.
Ovviamente questi valori potranno essere estesi ad altri casi/oggetti atti alla sincronizzazione.
';

COMMENT ON COLUMN SYSINTEGRATION_MODEL.LOGITEM IS 'H=High
L=Low
O=OFF (default)
H= logga sugli item della tabella SYSINTEGRATION_PENDING_SINCRO_LOGDOCUMENT tutte le operazioni di sincronizzazione.
L= logga sugli item della tabella SYSINTEGRATION_PENDING_SINCRO_LOGDOCUMENT tutte le operazioni di sincronizzazione che hanno avuto errori.
O = off, Nessun log.
';



