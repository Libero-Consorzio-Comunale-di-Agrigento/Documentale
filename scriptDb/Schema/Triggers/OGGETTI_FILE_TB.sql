CREATE OR REPLACE TRIGGER OGGETTI_FILE_TB
/******************************************************************************
 NOME:        OGGETTI_FILE_TB
 DESCRIZIONE: TRIGGER FOR CUSTOM FUNCTIONAL CHECK
                       AT INSERT OR UPDATE OR DELETE ON TABLE DATI
 ANNOTAZIONI: ESEGUE INIZIALIZZAZIONE TABELLA DI POST EVENT.
 REVISIONI:
 REV. DATA       AUTORE DESCRIZIONE
 ---- ---------- ------ ------------------------------------------------------
 0    __/__/____ __     PRIMA EMISSIONE.
******************************************************************************/
   BEFORE INSERT OR UPDATE OR DELETE ON OGGETTI_FILE
BEGIN
   /* RESET POSTEVENT FOR CUSTOM FUNCTIONAL CHECK */
   IF INTEGRITYPACKAGE.GETNESTLEVEL = 0 THEN
      INTEGRITYPACKAGE.INITNESTLEVEL;
   END IF;
END;
/


