CREATE OR REPLACE FUNCTION F_ALLINEA_BLOCCHI ( A_TIPODOC VARCHAR2 )
RETURN VARCHAR2 IS
    CURSOR  C_1 IS
    SELECT  BLOCCO_JDMS NOME ,  AREA ,  CODICE_MODELLO ,   'Documento di tipo ' ||CODICE_MODELLO CORPO
     FROM MODELLI  MO
     WHERE ID_TIPODOC =  A_TIPODOC
       AND NOT EXISTS (SELECT 1 FROM BLOCCHI BL WHERE BL.BLOCCO = MO.BLOCCO_JDMS AND AREA =  MO.AREA )
       AND A_TIPODOC IS   NOT   NULL
     UNION ALL
     SELECT  BLOCCO_JDMS NOME ,  AREA ,  CODICE_MODELLO ,   'Documento di tipo ' ||CODICE_MODELLO CORPO
     FROM TIPI_DOCUMENTO  TD ,   MODELLI  MO
     WHERE TD.ID_TIPODOC =  MO.ID_TIPODOC
       AND NOT   EXISTS   (SELECT   1   FROM   BLOCCHI  BL WHERE  BL.BLOCCO =  MO.BLOCCO_JDMS   AND  AREA =  MO.AREA )
       AND A_TIPODOC IS   NULL;
BEGIN
   FOR  C1 IN  C_1 LOOP
      BEGIN
         INSERT INTO   BLOCCHI   (  BLOCCO ,  AREA ,  CODICE_MODELLO ,  CORPO, TIPO )
         VALUES(  C1.NOME ,   C1.AREA ,  C1.CODICE_MODELLO ,  C1.CORPO, 'J' );
      EXCEPTION
         WHEN OTHERS THEN
            RETURN '-20990' || SQLERRM;
      END;
   END LOOP;
   return   '1' ;
END F_ALLINEA_BLOCCHI;
/

