CREATE OR REPLACE FUNCTION F_VALORE_CAMPO_ORIZ (
   p_doc     VARCHAR2,
   p_campo   VARCHAR2
)
   RETURN VARCHAR2
IS
   s_valore      VARCHAR2 (32767);
   ex_custom EXCEPTION;
   PRAGMA EXCEPTION_INIT( ex_custom, -20999 );
BEGIN
    s_valore := F_VALORE_CAMPO_CLOB_ORIZ(p_doc,p_campo);

    return s_valore;
   exception
    when ex_custom then  raise;
    when others then return null;
END F_VALORE_CAMPO_ORIZ;
/

