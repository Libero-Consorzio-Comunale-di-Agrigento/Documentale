CREATE OR REPLACE FUNCTION F_GETDIRECTORY_AREA_NAME(A_ID_DOCUMENTO NUMBER) RETURN VARCHAR2 IS
tmpVar AREE.ACRONIMO%TYPE;
tmpVarAconimoDir VARCHAR2(100);
/******************************************************************************
   NAME:       F_GETDIRECTORY_AREA_NAME
   PURPOSE:

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        06/05/2010          1. Created this function.

   NOTES:

   Automatically available Auto Replace Keywords:
      Object Name:     F_GETDIRECTORY_AREA_NAME
      Sysdate:         06/05/2010
      Date and Time:   06/05/2010, 15:24:17, and 06/05/2010 15:24:17
      Username:         (set in TOAD Options, Procedure Editor)
      Table Name:       (set in the "New PL/SQL Object" dialog)

******************************************************************************/
BEGIN
    tmpVarAconimoDir := NULL;

    SELECT  aree.acronimo, aree_path.PREFIX_ACRONIMO_DIRECTORY
    INTO tmpVar, tmpVarAconimoDir
    FROM AREE, DOCUMENTI, AREE_PATH
    WHERE AREE.AREA=DOCUMENTI.AREA
    AND DOCUMENTI.ID_DOCUMENTO=A_ID_DOCUMENTO
    AND aree.ID_PATH_AREE =  aree_path.ID_PATH_AREE_FILE  (+);

    IF tmpVarAconimoDir IS NULL THEN
         RETURN 'DIR_'||tmpVar;
    ELSE
         RETURN 'DIR_'||tmpVar||'_'||tmpVarAconimoDir;
    END IF;

   EXCEPTION
     WHEN NO_DATA_FOUND THEN
       NULL;
     WHEN OTHERS THEN
       -- Consider logging the error and then re-raise
       RAISE;
END F_GETDIRECTORY_AREA_NAME;
/

