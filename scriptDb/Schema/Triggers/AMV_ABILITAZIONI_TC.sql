CREATE OR REPLACE TRIGGER AMV_ABILITAZIONI_TC
/******************************************************************************
 NOME:        AMV_ABILITAZIONI_TC
 DESCRIZIONE: Trigger for Custom Functional Check
                    after INSERT or UPDATE or DELETE on Table AMV_ABILITAZIONI
 ANNOTAZIONI: Esegue operazioni di POST Event prenotate.
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    __/__/____ __     Prima emissione.
******************************************************************************/
   after INSERT or UPDATE or DELETE on AMV_ABILITAZIONI
BEGIN
   /* EXEC PostEvent for Custom Functional Check */
   IntegrityPackage.Exec_PostEvent;
END;
/* End Trigger: AMV_ABILITAZIONI_TC */
/


