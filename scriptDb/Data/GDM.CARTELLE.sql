﻿Insert into CARTELLE
   (ID_CARTELLA, NOME, TIPO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, 
    ID_DOCUMENTO_PROFILO, STATO, CODICEADS)
 Values
   (-1, 'WRKSPSISTEMA', 'S', TO_DATE('09/02/2004 00:00:00', 'MM/DD/YYYY HH24:MI:SS'), '_SYS', 
    -1, NULL, NULL);
Insert into CARTELLE
   (ID_CARTELLA, NOME, TIPO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, 
    ID_DOCUMENTO_PROFILO, STATO, CODICEADS)
 Values
   (-2, 'WRKSPUTENTE', 'S', TO_DATE('09/02/2004 00:00:00', 'MM/DD/YYYY HH24:MI:SS'), '_SYS', 
    -2, NULL, NULL);
Insert into CARTELLE
   (ID_CARTELLA, NOME, TIPO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, 
    ID_DOCUMENTO_PROFILO, STATO, CODICEADS)
 Values
   (-4, 'TESTADS', 'S', TO_DATE('06/22/2009 15:35:36', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 
    7, NULL, NULL);
Insert into CARTELLE
   (ID_CARTELLA, NOME, TIPO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, 
    ID_DOCUMENTO_PROFILO, STATO, CODICEADS)
 Values
   (1, 'Cartella Standard', 'U', TO_DATE('06/22/2009 15:36:06', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 
    8, NULL, NULL);
Insert into CARTELLE
   (ID_CARTELLA, NOME, TIPO, DATA_AGGIORNAMENTO, UTENTE_AGGIORNAMENTO, 
    ID_DOCUMENTO_PROFILO, STATO, CODICEADS)
 Values
   (2, 'Sottocartella Standard', 'U', TO_DATE('06/22/2009 15:36:23', 'MM/DD/YYYY HH24:MI:SS'), 'GDM', 
    9, NULL, NULL);
COMMIT;