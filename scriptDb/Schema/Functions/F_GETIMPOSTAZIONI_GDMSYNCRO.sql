CREATE OR REPLACE FUNCTION F_GETIMPOSTAZIONI_GDMSYNCRO RETURN VARCHAR2 IS
v_no number;
v_error exception;
pragma exception_init(v_error,-942);
a_sql varchar2(10000);
a_ret varchar2(10000) :='';
BEGIN
   --1. ricerca della tabella , se non la trovo torno @inesistente@
   BEGIN
        a_sql:='SELECT NVL(USER_GDM,'''')||''#DELIM#''||NVL(DSN,'''')||''#DELIM#''||NVL(SERVER_SERVLET,'''')  FROM GDMSYNCRO_IMPOSTAZIONI';
        EXECUTE IMMEDIATE A_SQL INTO a_ret;
   EXCEPTION WHEN v_error then
        a_ret :='@NOTEXIST@';
   END;

   RETURN a_ret;

EXCEPTION WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20999,'F_GETIMPOSTAZIONI_GDMSYNCRO - '||SQLERRM);
END F_GETIMPOSTAZIONI_GDMSYNCRO;
/

