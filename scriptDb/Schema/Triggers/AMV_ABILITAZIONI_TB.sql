CREATE OR REPLACE TRIGGER AMV_ABILITAZIONI_TB
/******************************************************************************
 NOME:        AMV_ABILITAZIONI_TC
 DESCRIZIONE: Trigger for Custom Functional Check
                       at INSERT or UPDATE or DELETE on Table AMV_ABILITAZIONI
 ANNOTAZIONI: Esegue inizializzazione tabella di POST Event.
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    __/__/____ __     Prima emissione.
******************************************************************************/
   before INSERT or UPDATE or DELETE on AMV_ABILITAZIONI
BEGIN
   /* RESET PostEvent for Custom Functional Check */
   IF IntegrityPackage.GetNestLevel = 0 THEN
      IntegrityPackage.InitNestLevel;
   END IF;
END;
/* End Trigger: AMV_ABILITAZIONI_TB */
/


