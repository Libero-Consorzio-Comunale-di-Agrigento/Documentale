CREATE TABLE SYSINTEGRATION_PENDING
(
  IDSYSPENDING              NUMBER(10)          NOT NULL,
  TYPE_INTEGRATION          VARCHAR2(50 CHAR)   NOT NULL,
  ID_DOCUMENTO              NUMBER(10)          NOT NULL,
  XML_OPTION_OTHERKEY       VARCHAR2(4000 BYTE),
  IDENTIFIER_REMOTE_OBJECT  VARCHAR2(500 CHAR),
  CREATEDATE                DATE                NOT NULL,
  UPDATEDATE                DATE                NOT NULL,
  PENDING                   NUMBER(1)           NOT NULL,
  LASTDATE_PENDING          DATE                NOT NULL,
  LASTSTATUS                VARCHAR2(2 BYTE)    NOT NULL,
  LASTERROR                 VARCHAR2(4000 BYTE),
  EXPORTFIELD               NUMBER(1)           DEFAULT 1                     NOT NULL
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

COMMENT ON COLUMN SYSINTEGRATION_PENDING.IDSYSPENDING IS 'Sequence utilizzata per comodit¿. In pratica questa colonna fa da PK in sostituzione alle 3 chiavi sotto';

COMMENT ON COLUMN SYSINTEGRATION_PENDING.TYPE_INTEGRATION IS 'Esempio: CRV-DOCS, HUMMINGBIRD';

COMMENT ON COLUMN SYSINTEGRATION_PENDING.XML_OPTION_OTHERKEY IS 'Campo descritto da un xml preso a partire dal campo  XML_OtherKey_Document_Rule della tabella (1). L¿xml conterr¿ i valori chiave sostituiti.
Esempio:
<root>
  <object table=¿OGGETTI_FILE¿>135</object>
</root>
E¿ stato sostituito l¿id oggetto file della tabella oggetti file.
Se il campo XML_OtherKey_Document_Rule risulta vuoto oppure si sta trattando il documento (e non il suo allegato), su questa tabella verr¿ inserito NULL
';

COMMENT ON COLUMN SYSINTEGRATION_PENDING.IDENTIFIER_REMOTE_OBJECT IS 'Es. Per crv ¿ l¿uuid del document restituito dal ws dopo la sincronizzazione';

COMMENT ON COLUMN SYSINTEGRATION_PENDING.LASTSTATUS IS 'ER=Error..l¿ultima volta che ¿ stato tentato l¿invio c¿¿ stato un errore
OK=Inviato/Aggiornato con  successo
';

COMMENT ON COLUMN SYSINTEGRATION_PENDING.LASTERROR IS 'Vuoto se laststatus ¿ OK';

COMMENT ON COLUMN SYSINTEGRATION_PENDING.EXPORTFIELD IS '1 o 0 (default 1). Se settato a 0, l¿item da verr¿ esportato senza campi modello.';



