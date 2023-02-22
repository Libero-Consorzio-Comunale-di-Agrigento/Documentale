CREATE OR REPLACE FUNCTION F_USER(P_USERNAME VARCHAR2)
RETURN VARCHAR2 IS
/*  BY MANNY*/
   N_UTENTE VARCHAR2(8);
 BEGIN
        SELECT UTENTE
        INTO N_UTENTE
        FROM AD4_UTENTI
         WHERE NOMINATIVO=P_USERNAME;
      RETURN N_UTENTE;
   EXCEPTION
     WHEN OTHERS THEN
      RETURN '';
 END  F_USER ;
/

