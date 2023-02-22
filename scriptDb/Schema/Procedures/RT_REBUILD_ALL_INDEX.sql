CREATE OR REPLACE procedure RT_REBUILD_ALL_INDEX
/******************************************************************************
 NOME:        RT_REBUILD_ALL_INDEX
 DESCRIZIONE: Procedure for lanciare il rebuild degli indici su tutte le tabelle
                         che usano ricerche di intermedia.
 ARGOMENTI:
 ECCEZIONI:
 REVISIONI:
 Rev. Data       Autore  Descrizione
 ---- ---------- ------ --------- ---------------------------------------------
1.0 25/07/2007   NSVA
******************************************************************************/
is
cursor index_cursor is (
          select distinct PND_INDEX_NAME
           from CTX_PENDING
         where PND_INDEX_OWNER = user
           and PND_INDEX_NAME not in ('VAL_CLOB_CTX', 'OGFI_FILE_CTX','OGFI_TEOC_CTX'))
          ;
begin
   for index_pending in index_cursor loop
      ctx_ddl.sync_index(index_pending.pnd_index_name, '10M');
--      ctx_ddl.optimize_index(index_pending.pnd_index_name, CTX_DDL.OPTLEVEL_FAST);
   end loop;
exception
when others then
   null;
end;
/* End Procedure: RT_REBUILD_INDEX */
/

