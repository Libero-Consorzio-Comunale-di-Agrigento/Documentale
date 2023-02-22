CREATE OR REPLACE TRIGGER OGGETTI_FILE_TIU
BEFORE INSERT OR UPDATE
ON OGGETTI_FILE
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW
DECLARE
TMPVAR NUMBER;
/******************************************************************************
   NAME:       OGGETTI_FILE_TIU
   PURPOSE:
   REVISIONS:
   VER        DATE        AUTHOR           DESCRIPTION
   ---------  ----------  ---------------  ------------------------------------
   1.0        19/12/2007             1. CREATED THIS TRIGGER.
   NOTES:
   AUTOMATICALLY AVAILABLE AUTO REPLACE KEYWORDS:
      OBJECT NAME:     OGFI_TIU
      SYSDATE:         19/12/2007
      DATE AND TIME:   19/12/2007, 15.39.36, AND 19/12/2007 15.39.36
      USERNAME:         (SET IN TOAD OPTIONS, PROC TEMPLATES)
      TABLE NAME:      OGGETTI_FILE (SET IN THE "NEW PL/SQL OBJECT" DIALOG)
      TRIGGER OPTIONS:  (SET IN THE "NEW PL/SQL OBJECT" DIALOG)
******************************************************************************/
BEGIN
  IF NVL( :NEW.DA_CANCELLARE,'N')<>'S' THEN
     IF NVL(:NEW.DATA_ULTIMA_ESTERNALIZZAZIONE,TO_DATE('01/01/1900','dd/mm/yyyy') )  =   NVL( :OLD.DATA_ULTIMA_ESTERNALIZZAZIONE,TO_DATE('01/01/1900','dd/mm/yyyy') )  THEN
        :NEW.DATA_AGGIORNAMENTO:=SYSDATE;
     END IF;
  END IF;
  :NEW.FILENAME := replace(:NEW.FILENAME, chr(9), ' ');
   EXCEPTION
     WHEN OTHERS THEN
       -- CONSIDER LOGGING THE ERROR AND THEN RE-RAISE
       RAISE;
END OGGETTI_FILE_TIU;
/


