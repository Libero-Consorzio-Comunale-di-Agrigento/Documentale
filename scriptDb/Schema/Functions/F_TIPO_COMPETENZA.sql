CREATE OR REPLACE FUNCTION F_TIPO_COMPETENZA(
      P_OGGETTO         VARCHAR2,
      P_TIPO_OGG   VARCHAR2)
RETURN VARCHAR2 IS
   DESCR VARCHAR2(200);
 BEGIN
        IF (P_TIPO_OGG = 'C') THEN
     BEGIN
         SELECT TIPO
       INTO DESCR
       FROM CARTELLE
       WHERE ID_CARTELLA = P_OGGETTO;
     END;
     ELSIF (P_TIPO_OGG = 'D') THEN
     BEGIN
         DESCR := 'DOCUMENTO'||P_OGGETTO;
     END;
     ELSIF (P_TIPO_OGG = 'Q')  THEN
      BEGIN
         SELECT TIPO
       INTO DESCR
       FROM QUERY
       WHERE ID_QUERY = P_OGGETTO;
     END;
     END IF;
RETURN DESCR;
 END F_TIPO_COMPETENZA ;
/

