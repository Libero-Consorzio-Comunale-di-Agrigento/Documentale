CREATE OR REPLACE FUNCTION F_VALORE_PARAMETRI (P_codicemodello VARCHAR2)
RETURN VARCHAR2 IS
   S_VALORE VARCHAR2(4000);
 BEGIN
        SELECT valore
        INTO S_VALORE
  FROM parametri
WHERE  upper(codice || tipo_modello) = upper( p_codicemodello)
       ;
      RETURN S_VALORE;
   EXCEPTION
     WHEN OTHERS THEN
      RETURN NULL;
 END  F_VALORE_PARAMETRI ;
/

