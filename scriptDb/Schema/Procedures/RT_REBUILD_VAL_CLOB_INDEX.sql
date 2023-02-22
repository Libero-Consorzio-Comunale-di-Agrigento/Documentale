CREATE OR REPLACE PROCEDURE rt_rebuild_val_clob_index
/******************************************************************************
 NOME:        RT_REBUILD_VAL_CLOB_INDEX
 DESCRIZIONE: PROCEDURE FOR LANCIARE IL REBUILD DELL'INDICE SUI VALORI DEI DOCUMENTI
                         PER LE RICERCHE DI INTERMEDIA.
                   LANCIAMO L'ottimizzazione FAST perche si considera
                   che le delete siano minime o inesistenti.
 ARGOMENTI:
 ECCEZIONI:
 ANNOTAZIONI: Richiamata da DMSERVER in base a REBUILD_IMMEDIATE in table PARAMETRI
 REVISIONI:
 Rev. Data       Autore  Descrizione
 ---- ---------- ------ --------- ---------------------------------------------
1.0 08/09/2006 NS
******************************************************************************/
IS
BEGIN
   BEGIN
      CTX_DDL.SYNC_INDEX('VAL_CLOB_CTX','10M');
   COMMIT;
  END;
EXCEPTION
WHEN OTHERS THEN
RAISE_APPLICATION_ERROR('-20999', SQLERRM);
END;
/* END PROCEDURE: RT_REBUILD_VAL_CLOB_INDEX */
/

