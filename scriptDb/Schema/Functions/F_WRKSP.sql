CREATE OR REPLACE FUNCTION F_WRKSP (A_ID_OGGETTO NUMBER, A_TIPO_OGGETTO VARCHAR2)
RETURN NUMBER IS
/*****************************************************************************************
   NAME:        F_WRKSP
   PURPOSE:    RESTITUISCE LA WORKSPACE IN CUI SI TROVA L'A_ID_OGGETTO
               DI TIPO A_TIPO_OGGETTO
    RETURN:    IDENTIFICATIVO DEL PROFILO-CARTELLA
   REVISIONS:
   VER   VER DMSERVER.COMPATIBILITY DATE        AUTHOR           DESCRIPTION
   ----  -------------------------  ----------  ---------------  -------------------------
   1.0   TUTTE LE VERSIONI          07/11/2006  MANNELLA G.      1. CREATED THIS FUNCTION.
*****************************************************************************************/
       IDWRKSP NUMBER;
BEGIN
  BEGIN
    SELECT ID_CARTELLA
      INTO IDWRKSP
      FROM (SELECT ID_CARTELLA, TIPO_OGGETTO
              FROM LINKS
             START WITH  ID_OGGETTO = A_ID_OGGETTO AND TIPO_OGGETTO = A_TIPO_OGGETTO
             CONNECT BY PRIOR ID_CARTELLA = ID_OGGETTO
                          AND TIPO_OGGETTO = 'C'
             ) LNK
      WHERE LNK.ID_CARTELLA<0 AND LNK.TIPO_OGGETTO=A_TIPO_OGGETTO;
  EXCEPTION WHEN NO_DATA_FOUND THEN
      RETURN 0;
            WHEN OTHERS THEN
    RETURN 0;
  END;
  RETURN IDWRKSP;
END F_WRKSP;
/

