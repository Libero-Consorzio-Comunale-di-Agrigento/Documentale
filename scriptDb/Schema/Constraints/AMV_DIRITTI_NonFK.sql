ALTER TABLE AMV_DIRITTI ADD (
  CONSTRAINT AMV_DIRITTI_ACCESSO_CC
  CHECK (ACCESSO in ('R','C','U','V','A'))
  ENABLE VALIDATE);

ALTER TABLE AMV_DIRITTI ADD (
  CONSTRAINT AMV_DIRITTI_ID_AREA_CC
  CHECK (ID_AREA >= 1)
  ENABLE VALIDATE);

ALTER TABLE AMV_DIRITTI ADD (
  CONSTRAINT AMV_DIRITTI_ID_DIRITTO_CC
  CHECK (ID_DIRITTO >= 1)
  ENABLE VALIDATE);

ALTER TABLE AMV_DIRITTI ADD (
  CONSTRAINT AMV_DIRITTI_ID_TIPOLOGIA_CC
  CHECK (ID_TIPOLOGIA is null or (ID_TIPOLOGIA >= 1 ))
  ENABLE VALIDATE);

ALTER TABLE AMV_DIRITTI ADD (
  CONSTRAINT AMV_DIRITTI_PK
  PRIMARY KEY
  (ID_DIRITTO)
  USING INDEX AMV_DIRITTI_PK
  ENABLE VALIDATE);

