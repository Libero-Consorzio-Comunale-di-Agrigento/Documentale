CREATE OR REPLACE FUNCTION F_FORMATO_DATA(
      P_CAMPO         VARCHAR2)
RETURN VARCHAR2 IS
   S_TIPO VARCHAR2(5);
   S_FORMATO VARCHAR2(30);
 BEGIN
        SELECT DATI.TIPO, DATI.FORMATO_DATA
        INTO S_TIPO, S_FORMATO
        FROM CAMPI_DOCUMENTO,DATI_MODELLO, DATI
         WHERE CAMPI_DOCUMENTO.ID_CAMPO = DATI_MODELLO.ID_CAMPO
           AND DATI_MODELLO.AREA_DATO = DATI.AREA
           AND DATI_MODELLO.DATO = DATI.DATO
           AND CAMPI_DOCUMENTO.ID_CAMPO = P_CAMPO
         AND ROWNUM < 2;
   IF S_TIPO = 'D' THEN
      RETURN REPLACE(S_FORMATO, 'hh:', 'hh24:');
   ELSE
      RETURN 'dd/mm/yyyy';
   END IF;
   EXCEPTION
     WHEN OTHERS THEN
      RETURN 'dd/mm/yyyy';
 END F_FORMATO_DATA ;
/

