Insert into QUERY
   (ID_QUERY, NOME, TIPO, FILTRO, DATA_AGGIORNAMENTO, 
    UTENTE_AGGIORNAMENTO, ID_DOCUMENTO_PROFILO, CODICEADS)
 Values
   (1, 'Ricerca Standard(Verticale)', 'S', '<?xml version="1.0" encoding="UTF-8" ?><DOC_INFO xmlns:xsi="http://www.w3.org/2000/10/XMLSchema-instance" xsi:noNamespaceSchemaLocation="doc_info_v1.4.1.xsd"><AREA value="TESTADS" /><TIPO_DOC version="1.1" value="M_VERTICALE" /><MASTER value="0" /></DOC_INFO>', TO_DATE('06/29/2009 11:15:15', 'MM/DD/YYYY HH24:MI:SS'), 
    'GDM', 5, NULL);
Insert into QUERY
   (ID_QUERY, NOME, TIPO, FILTRO, DATA_AGGIORNAMENTO, 
    UTENTE_AGGIORNAMENTO, ID_DOCUMENTO_PROFILO, CODICEADS)
 Values
   (2, 'Ricerca Orizzontale', 'S', 'RICERCAMODULISTICA_TESTADS@M_RICERCA_ORIZZONTALE', TO_DATE('06/25/2009 11:19:37', 'MM/DD/YYYY HH24:MI:SS'), 
    'GDM', 6, NULL);
COMMIT;
