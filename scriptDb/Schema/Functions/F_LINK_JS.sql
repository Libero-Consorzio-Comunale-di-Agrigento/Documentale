CREATE OR REPLACE FUNCTION F_LINK_JS(
      P_OGGETTO         VARCHAR2,
      P_TIPO_OGG   VARCHAR2)
RETURN VARCHAR2 IS
   DESCR VARCHAR2(2000);
 BEGIN
        IF (P_TIPO_OGG = 'C') THEN
     BEGIN
         -- (VECCHIO PROCEDIMENTO) -- SELECT NOME ||'</a><a href="javascript:riportaIDCartella('''||TO_CHAR(ID_CARTELLA)||''','''||NOME||''')" ><img name="cartellaselezione'||TO_CHAR(ID_CARTELLA)||'" border="" height="16" src="images/sendto.gif" width="16"></a>'
       --SELECT NOME ||'<a href=\"javascript:riportaIDCartella('''||TO_CHAR(ID_CARTELLA)||''','''||REPLACE(NOME,'''','\\''')||''')\" ><img name=\"cartellaselezione'||TO_CHAR(ID_CARTELLA)||'\" border=\"\" height=\"16\" src=\"images/sendto.gif\" width=\"16\"></a>'
       SELECT ' '||'<a style=\"text-decoration: none;\" href=\"javascript:riportaIDCartella('''||TO_CHAR(ID_CARTELLA)||''','''||REPLACE(NOME,'''','\\''')||''')\" >'||NOME||'</a>'
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
         SELECT NOME
       INTO DESCR
       FROM QUERY
       WHERE ID_QUERY = P_OGGETTO;
     END;
     END IF;
RETURN DESCR;
 END F_LINK_JS;
/

