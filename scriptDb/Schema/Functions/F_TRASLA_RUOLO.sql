CREATE OR REPLACE FUNCTION F_TRASLA_RUOLO
( P_UTENTE IN VARCHAR2
, P_MODULO IN VARCHAR2
, P_ISTANZA IN VARCHAR2)
RETURN VARCHAR2
IS
RUOLO VARCHAR2(10);
BEGIN
 RETURN 'GDM';
END;
/

