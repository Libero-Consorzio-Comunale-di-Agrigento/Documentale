CREATE OR REPLACE FUNCTION F_IconaFromCartellaOrQuery(A_ID NUMBER,TIPO VARCHAR2)
RETURN VARCHAR2 IS
/*  BY MANNY*/
   ICONA TIPI_DOCUMENTO.ICONA%TYPE;
 BEGIN
   IF TIPO='C' THEN
       BEGIN
         select icona
         INTO ICONA
         from tipi_documento, cartelle, documenti
         where cartelle.id_cartella=A_ID and
              ID_DOCUMENTO_PROFILO=id_documento and
              documenti.id_tipodoc=tipi_documento.id_tipodoc;
         EXCEPTION WHEN NO_DATA_FOUND THEN
                  RETURN TO_CHAR(NULL);
     END;
    ELSE
      BEGIN
         select icona
         INTO ICONA
         from tipi_documento, query, documenti
         where query.id_query=A_ID and
              ID_DOCUMENTO_PROFILO=id_documento and
              documenti.id_tipodoc=tipi_documento.id_tipodoc;
         EXCEPTION WHEN NO_DATA_FOUND THEN
                  RETURN TO_CHAR(NULL);
      END;
   END IF;
    RETURN ICONA;
 END F_IconaFromCartellaOrQuery;
/

