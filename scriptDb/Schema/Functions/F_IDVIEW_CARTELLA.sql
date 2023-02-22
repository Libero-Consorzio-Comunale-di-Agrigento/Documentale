CREATE OR REPLACE FUNCTION F_IDVIEW_CARTELLA(A_CARTELLA NUMBER)
RETURN VARCHAR2 IS
/*  BY ANDREA*/
   IDC VARCHAR2(20);
 BEGIN
   BEGIN
    SELECT ID_VIEWCARTELLA
      INTO IDC
      FROM VIEW_CARTELLA
     WHERE ID_CARTELLA = A_CARTELLA
      ;
    EXCEPTION WHEN NO_DATA_FOUND THEN
      RETURN NULL;
   END;
   RETURN IDC;
 END F_IDVIEW_CARTELLA ;
/

