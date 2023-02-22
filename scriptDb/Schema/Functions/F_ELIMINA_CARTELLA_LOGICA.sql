CREATE OR REPLACE FUNCTION F_ELIMINA_CARTELLA_LOGICA(P_IDCARTELLA NUMBER,P_USER VARCHAR2)
RETURN NUMBER IS
BEGIN
 RETURN F_Elimina_Cartella_LF(P_IDCARTELLA,P_USER,'N','L');
 EXCEPTION
   WHEN OTHERS THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR('-20999','PROBLEMI IN ELIMINA CARTELLA LOGICA'
                                ||') DESCRIZIONE ERRORE: '||SQLERRM);
   RETURN 0;
END F_ELIMINA_CARTELLA_LOGICA;
/

