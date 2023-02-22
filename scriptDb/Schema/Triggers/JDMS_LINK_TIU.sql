CREATE OR REPLACE TRIGGER JDMS_LINK_TIU
BEFORE INSERT OR UPDATE
ON JDMS_LINK
REFERENCING NEW AS New OLD AS Old
FOR EACH ROW
DECLARE
tmpVar NUMBER;
/******************************************************************************
   NAME:
   PURPOSE:
   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        27/06/2012             1. Created this trigger.
   NOTES:
   Automatically available Auto Replace Keywords:
      Object Name:
      Sysdate:         27/06/2012
      Date and Time:   27/06/2012, 11:08:51, and 27/06/2012 11:08:51
      Username:         (set in TOAD Options, Proc Templates)
      Table Name:      JDMS_LINK (set in the "New PL/SQL Object" dialog)
      Trigger Options:  (set in the "New PL/SQL Object" dialog)
******************************************************************************/
BEGIN
   IF :NEW.ICONA IS NULL AND :NEW.ICONA_EXP IS NULL THEN
      RAISE_APPLICATION_ERROR(-20999,'Attenzione! Il campo <icona> ed <espressione icona> non possono essere contemporaneamente vuoti');
   END IF;
END ;
/


