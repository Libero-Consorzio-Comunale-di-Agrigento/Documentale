CREATE OR REPLACE FUNCTION F_ESISTE_TABELLA(P_NOMETABELLA VARCHAR2) RETURN NUMBER IS
tmpVar       NUMBER;
d_testo       VARCHAR2 (4000);
/******************************************************************************
   NAME:       F_ESISTE_TABELLA
   PURPOSE:
   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   2.0        09/03/2011   MMA       1. Created this function.
   NOTES:
   Automatically available Auto Replace Keywords:
      Object Name:     F_ESISTE_TABELLA
      Sysdate:         29/10/2007
      Date and Time:   29/10/2007, 12.21.30, and 29/10/2007 12.21.30
      Username:         (set in TOAD Options, Procedure Editor)
      Table Name:       (set in the "New PL/SQL Object" dialog)
******************************************************************************/
BEGIN
    tmpVar := 0;
     d_testo :=
            'SELECT count(1)'
         || ' FROM '
         || P_NOMETABELLA
         || ' WHERE ID_DOCUMENTO = -1000';
   EXECUTE IMMEDIATE d_testo
                INTO tmpVar;
    RETURN 1;
   EXCEPTION
     WHEN OTHERS THEN
       RETURN -1;
END F_ESISTE_TABELLA;
/

