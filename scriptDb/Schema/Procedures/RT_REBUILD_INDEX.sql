CREATE OR REPLACE PROCEDURE rt_rebuild_index
/******************************************************************************
 NOME:        RT_REBUILD_INDEX
 DESCRIZIONE: PROCEDURE FOR LANCIARE IL REBUILD DI TUTTI GLI INDICI DI GDM
                         PER LE RICERCHE INTERMEDIA.
                   LANCIAMO L'ottimizzazione FAST perche si considera
                   che le delete siano minime o inesistenti.
 ARGOMENTI:
 ECCEZIONI:
 ANNOTAZIONI:
 REVISIONI:
 Rev. Data       Autore  Descrizione
 ---- ---------- ------ --------- ---------------------------------------------
1.0 08/09/2006  NS
******************************************************************************/
IS
BEGIN
   BEGIN
      CTX_DDL.SYNC_INDEX('VAL_CLOB_CTX','10M');
      CTX_DDL.OPTIMIZE_INDEX('VAL_CLOB_CTX', CTX_DDL.OPTLEVEL_FAST);
      CTX_DDL.SYNC_INDEX('OGFI_FILE_CTX','10M');
      CTX_DDL.OPTIMIZE_INDEX('OGFI_FILE_CTX', CTX_DDL.OPTLEVEL_FAST);
      CTX_DDL.SYNC_INDEX('OGFI_TEOC_CTX','10M');
      CTX_DDL.OPTIMIZE_INDEX('OGFI_TEOC_CTX', CTX_DDL.OPTLEVEL_FAST);
   COMMIT;
  END;
EXCEPTION
WHEN OTHERS THEN
RAISE_APPLICATION_ERROR('-20999', SQLERRM);
END;
/* END PROCEDURE: RT_REBUILD_INDEX */
/

