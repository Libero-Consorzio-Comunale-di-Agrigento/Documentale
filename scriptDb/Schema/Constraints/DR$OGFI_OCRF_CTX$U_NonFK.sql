ALTER TABLE DR$OGFI_OCRF_CTX$U ADD (
  PRIMARY KEY
  (RID)
  USING INDEX
    TABLESPACE GDM
    PCTFREE    10
    INITRANS   2
    MAXTRANS   255
    STORAGE    (
                MAXSIZE          UNLIMITED
                PCTINCREASE      0
                BUFFER_POOL      DEFAULT
               )
  ENABLE VALIDATE);

