CREATE OR REPLACE FUNCTION F_VALORE (P_DOC VARCHAR2,  P_CAMPO         VARCHAR2)
RETURN VARCHAR2 IS
/*  BY ANDREA*/
   S_VALORE VARCHAR2(4000);
 BEGIN
        SELECT NVL(DBMS_LOB.SUBSTR(VALORE_CLOB, 4000), NVL(TO_CHAR(VALORE_DATA, nvl(REPLACE(formato_data,'hh:','hh24:'),'dd/mm/yyyy')),TO_CHAR(VALORE_NUMERO) ))
        INTO S_VALORE
        FROM VALORI,campi_documento,dati_modello, dati
         WHERE ID_DOCUMENTO = P_DOC
         AND valori.ID_CAMPO = P_CAMPO
         AND campi_documento.id_campo = valori.ID_CAMPO
         AND campi_documento.id_campo = dati_modello.id_campo
         AND dati_modello.AREA_DATO = dati.area
         AND dati_modello.dato = dati.dato;
      RETURN S_VALORE;
   EXCEPTION
     WHEN OTHERS THEN
      RETURN NULL;
 END  F_VALORE;
/

