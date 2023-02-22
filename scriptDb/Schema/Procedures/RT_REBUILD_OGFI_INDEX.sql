CREATE OR REPLACE PROCEDURE rt_rebuild_ogfi_index
/******************************************************************************
 NOME:        RT_REBUILD_OGFI_INDEX
 DESCRIZIONE: PROCEDURE FOR LANCIARE IL REBUILD DELL'INDICE SUGLI OGGETTI_FILE DEI DOCUMENTI
                         PER LE RICERCHE DI INTERMEDIA.
                   LANCIAMO L'ottimizzazione FAST perche si considera
                   che le delete siano minime o inesistenti.
 ARGOMENTI:
 ECCEZIONI:
 ANNOTAZIONI: richiamata da task programmati su Oracle oppure JWorkflow
 REVISIONI:
 Rev. Data       Autore  Descrizione
 ---- ---------- ------ --------- ---------------------------------------------
1.0 08/09/2006  NS
******************************************************************************/
IS
BEGIN
   BEGIN
      CTX_DDL.SYNC_INDEX('OGFI_FILE_CTX','10M');
      CTX_DDL.SYNC_INDEX('OGFI_TEOC_CTX','10M');
   COMMIT;
  END;
EXCEPTION
WHEN OTHERS THEN
RAISE_APPLICATION_ERROR('-20999', SQLERRM);
END;
/* END PROCEDURE: RT_REBUILD_OGFI_INDEX */
/

