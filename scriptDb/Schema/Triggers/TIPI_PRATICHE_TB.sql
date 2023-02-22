CREATE OR REPLACE TRIGGER TIPI_PRATICHE_TB
/******************************************************************************
 NOME:        TIPI_PRATICHE_TC
 DESCRIZIONE: TRIGGER FOR CUSTOM FUNCTIONAL CHECK
                       AT INSERT OR UPDATE OR DELETE ON TABLE TIPI_PRATICHE
 ANNOTAZIONI: ESEGUE INIZIALIZZAZIONE TABELLA DI POST EVENT.
 REVISIONI:
 REV. DATA       AUTORE DESCRIZIONE
 ---- ---------- ------ ------------------------------------------------------
                        GENERATA IN AUTOMATICO.
******************************************************************************/
   BEFORE INSERT OR UPDATE OR DELETE ON TIPI_PRATICHE
BEGIN
   /* RESET POSTEVENT FOR CUSTOM FUNCTIONAL CHECK */
   IF INTEGRITYPACKAGE.GETNESTLEVEL = 0 THEN
      INTEGRITYPACKAGE.INITNESTLEVEL;
   END IF;
END;
/* END TRIGGER: TIPI_PRATICHE_TB */
/


