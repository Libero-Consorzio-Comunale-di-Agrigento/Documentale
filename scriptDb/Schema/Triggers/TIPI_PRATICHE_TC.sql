CREATE OR REPLACE TRIGGER TIPI_PRATICHE_TC
/******************************************************************************
 NOME:        TIPI_PRATICHE_TC
 DESCRIZIONE: TRIGGER FOR CUSTOM FUNCTIONAL CHECK
                    AFTER INSERT OR UPDATE OR DELETE ON TABLE TIPI_PRATICHE
 ANNOTAZIONI: ESEGUE OPERAZIONI DI POST EVENT PRENOTATE.
 REVISIONI:
 REV. DATA       AUTORE DESCRIZIONE
 ---- ---------- ------ ------------------------------------------------------
                        GENERATA IN AUTOMATICO.
******************************************************************************/
   AFTER INSERT OR UPDATE OR DELETE ON TIPI_PRATICHE
BEGIN
   /* EXEC POSTEVENT FOR CUSTOM FUNCTIONAL CHECK */
   INTEGRITYPACKAGE.EXEC_POSTEVENT;
END;
/* END TRIGGER: TIPI_PRATICHE_TC */
/


