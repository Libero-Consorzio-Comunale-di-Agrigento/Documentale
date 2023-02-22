CREATE OR REPLACE FUNCTION F_GETLENGTH_FILEDOCUMENTO(A_ID_DOCUMENTO NUMBER, A_NOMEFILE VARCHAR2) RETURN NUMBER IS
tmpVar NUMBER;

BEGIN
    tmpVar := 0;

   Select decode(path_file, null, nvl(round(dbms_lob.getlength(testoocr)/1048576,2),0)  , nvl(round(dbms_lob.getlength("FILE")/1048576,2)  ,0))
   into tmpVar
   from oggetti_file
   where id_documento= A_ID_DOCUMENTO and upper(filename) = upper(A_NOMEFILE)
   and rownum=1;



   RETURN tmpVar;
   EXCEPTION
     WHEN NO_DATA_FOUND THEN
       return 0;
     WHEN OTHERS THEN
       -- Consider logging the error and then re-raise
       RAISE;
END F_GETLENGTH_FILEDOCUMENTO;
/

