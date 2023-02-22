CREATE OR REPLACE FUNCTION F_NOME_TABELLA(P_AREA VARCHAR2, P_CODICE_MODELLO VARCHAR2)
RETURN VARCHAR2 IS
NOMETABELLA         VARCHAR2(30);
BEGIN
  BEGIN
    SELECT UPPER(A.ACRONIMO||'_'||T.ALIAS_MODELLO)
      INTO NOMETABELLA
      FROM AREE A,
           TIPI_DOCUMENTO T
     WHERE A.AREA = T.AREA_MODELLO
       AND A.ACRONIMO IS NOT NULL
       AND T.ALIAS_MODELLO IS NOT NULL
       AND T.AREA_MODELLO = P_AREA
       AND T.NOME = P_CODICE_MODELLO;
   EXCEPTION
     WHEN NO_DATA_FOUND THEN
       NOMETABELLA := '';
     WHEN OTHERS THEN
       NOMETABELLA := SUBSTR(SQLERRM,1,4000);
  END;
  RETURN NOMETABELLA;
END F_NOME_TABELLA;
/
