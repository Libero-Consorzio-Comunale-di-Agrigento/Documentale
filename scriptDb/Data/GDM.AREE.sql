﻿Insert into AREE
   (ID_AREA, AREA, DESCRIZIONE, GESTIONE_MODELLI, ACRONIMO, 
    PATH_FILE, PATH_FILE_ORACLE, FORCE_FILE_ON_BLOB, DIM_MAX_ALL_BYTE, DIM_MAX_ALL_BYTE_BLOCCANTE, 
    GENERAZIONE_OCR, ID_PATH_AREE)
 Values
   (-1, 'GDMSYS', 'Area di Sistema', 'V', 'GDM', 
    NULL, NULL, 0, NULL, 'Y', 
    'N', NULL);
Insert into AREE
   (ID_AREA, AREA, DESCRIZIONE, GESTIONE_MODELLI, ACRONIMO, 
    PATH_FILE, PATH_FILE_ORACLE, FORCE_FILE_ON_BLOB, DIM_MAX_ALL_BYTE, DIM_MAX_ALL_BYTE_BLOCCANTE, 
    GENERAZIONE_OCR, ID_PATH_AREE)
 Values
   (1, 'TESTADS', 'Area di TEST post installazione/aggiornamento JSUITE', 'O', 'ADS', 
    NULL, NULL, 0, NULL, 'Y', 
    'N', NULL);
COMMIT;