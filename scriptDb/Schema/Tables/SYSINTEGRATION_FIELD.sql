CREATE TABLE SYSINTEGRATION_FIELD
(
  TYPE_INTEGRATION              VARCHAR2(50 CHAR) NOT NULL,
  AREA                          VARCHAR2(200 CHAR) NOT NULL,
  CODICE_MODELLO                VARCHAR2(50 CHAR) NOT NULL,
  FIELD                         VARCHAR2(100 CHAR) NOT NULL,
  FIELD_REMOTENAME              VARCHAR2(100 CHAR),
  ORDERFIELD                    NUMBER(3)       NOT NULL,
  KEYFIELD                      NUMBER(1)       NOT NULL,
  ACTIVE                        NUMBER(1)       NOT NULL,
  XML_OPTION_EXTRA_INSTRUCTION  VARCHAR2(4000 CHAR)
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

COMMENT ON COLUMN SYSINTEGRATION_FIELD.FIELD_REMOTENAME IS 'Nome del campo remoto corrispondente a questo sul GDM. Se nullo verr¿ inviato Field';

COMMENT ON COLUMN SYSINTEGRATION_FIELD.XML_OPTION_EXTRA_INSTRUCTION IS 'Campo NULLO oppure descritto da questo tipo di xml:
<root>
  <Instruction>FASCICOLO</Instruction >
  <Instruction>METADATO</Instruction >
  ¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿..
  ¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿..
  ¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿..
</root>
XML descrittivo che serve a trattare il campo in questione non solo come campo da inviare ma anche come oggetto da inviare altre n-volte secondo determinate specifiche presenti sul campo Instruction.
L¿esempio sopra (per CRV) sta ad indicare il fatto che questo campo fa parte anche del subset dei campi di fascicolo e che deve essere inviato anche come metadato.
Ovviamente ogni integrazione avr¿ i propri tag Instruction decodificati nella maniera corretta.
';



