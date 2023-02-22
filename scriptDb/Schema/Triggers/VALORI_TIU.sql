CREATE OR REPLACE TRIGGER VALORI_TIU
BEFORE INSERT
ON VALORI
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW
DECLARE
TMPVAR NUMBER;
/******************************************************************************
   NAME:
   PURPOSE:
   REVISIONS:
   VER        DATE        AUTHOR           DESCRIPTION
   ---------  ----------  ---------------  ------------------------------------
   1.0        23/01/2007             1. CREATED THIS TRIGGER.
   NOTES:
   AUTOMATICALLY AVAILABLE AUTO REPLACE KEYWORDS:
      OBJECT NAME:
      SYSDATE:         23/01/2007
      DATE AND TIME:   23/01/2007, 15.16.47, AND 23/01/2007 15.16.47
      USERNAME:         (SET IN TOAD OPTIONS, PROC TEMPLATES)
      TABLE NAME:      VALORI (SET IN THE "NEW PL/SQL OBJECT" DIALOG)
      TRIGGER OPTIONS:  (SET IN THE "NEW PL/SQL OBJECT" DIALOG)
******************************************************************************/
BEGIN
  :NEW.DATA_AGGIORNAMENTO:=SYSDATE;
   EXCEPTION
     WHEN OTHERS THEN
       -- CONSIDER LOGGING THE ERROR AND THEN RE-RAISE
       RAISE;
END ;
/


