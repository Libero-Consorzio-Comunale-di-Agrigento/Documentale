CREATE OR REPLACE TRIGGER TIPI_DOCUMENTO_TB
/******************************************************************************
 NOME:        TIPI_DOCUMENTO_TB
 DESCRIZIONE: TRIGGER FOR CUSTOM FUNCTIONAL CHECK
                       AT INSERT OR UPDATE OR DELETE ON TABLE TIPI_DOCUMENTO
 ANNOTAZIONI: ESEGUE INIZIALIZZAZIONE TABELLA DI POST EVENT.
 REVISIONI:
 REV. DATA       AUTORE DESCRIZIONE
 ---- ---------- ------ ------------------------------------------------------
 0    __/__/____ __     PRIMA EMISSIONE.
******************************************************************************/
   BEFORE INSERT OR UPDATE OR DELETE ON TIPI_DOCUMENTO
BEGIN
   /* RESET POSTEVENT FOR CUSTOM FUNCTIONAL CHECK */
   IF INTEGRITYPACKAGE.GETNESTLEVEL = 0 THEN
      INTEGRITYPACKAGE.INITNESTLEVEL;
   END IF;
END;
/


