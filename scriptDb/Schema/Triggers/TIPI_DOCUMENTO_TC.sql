CREATE OR REPLACE TRIGGER TIPI_DOCUMENTO_TC
/******************************************************************************
 NOME:        TIPI_DOCUMENTO_TC
 DESCRIZIONE: TRIGGER FOR CUSTOM FUNCTIONAL CHECK
                    AFTER INSERT OR UPDATE OR DELETE ON TABLE TIPI_DOCUMENTO
 ANNOTAZIONI: ESEGUE OPERAZIONI DI POST EVENT PRENOTATE.
 REVISIONI:
 REV. DATA       AUTORE DESCRIZIONE
 ---- ---------- ------ ------------------------------------------------------
 0    __/__/____ __     PRIMA EMISSIONE.
******************************************************************************/
   AFTER INSERT OR UPDATE OR DELETE ON TIPI_DOCUMENTO
BEGIN
   /* EXEC POSTEVENT FOR CUSTOM FUNCTIONAL CHECK */
   INTEGRITYPACKAGE.EXEC_POSTEVENT;
END;
/


