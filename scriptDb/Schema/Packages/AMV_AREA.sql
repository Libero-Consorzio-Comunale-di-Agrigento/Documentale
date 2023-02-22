CREATE OR REPLACE PACKAGE Amv_Area AS
/******************************************************************************
 NOME:        AMV_AREA
 DESCRIZIONE: Gestione Aree di Documenti
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    31/03/2004 AO     Prima emissione.
 1    16/11/2004 AO     Versione 2004.09
************************************************************************************/
d_versione  VARCHAR2(20) := 'V2004.09';
d_revisione VARCHAR2(30) := '1    16/11/2004';
FUNCTION versione RETURN VARCHAR2;
FUNCTION get_diritto_gruppo (
  p_gruppo       VARCHAR2
, p_id_area      NUMBER
, p_id_tipologia NUMBER DEFAULT NULL
) RETURN VARCHAR2;
/******************************************************************************
 DESCRIZIONE: Riceve codice gruppo e area (di documenti) di accesso;
              Dovrebbe ritornare il diritto di Accesso piu alto per il gruppo interessato
              ritrovato su tabella AMV_DIRITTI per l'area relativa al documento indicato.
 PARAMETRI:   P_GRUPPO       VARCHAR2    Codice del gruppo
              P_ID_AREA      NUMBER      Codice dell'area
              P_ID_TIPOLOGIA NUMBER      Codice della tipologia
 RITORNA:     VARCHAR2       tipo di diritto, Read, Create, Update
******************************************************************************/
FUNCTION get_diritto_utente (
   p_utente       VARCHAR2
 , p_id_area      NUMBER
 , p_id_tipologia NUMBER DEFAULT NULL
) RETURN VARCHAR2;
/******************************************************************************
 DESCRIZIONE: Riceve codice utente, area di accesso e tipologia (di documenti) ;
              Dovrebbe ritornare il diritto di Accesso piu alto per il gruppo interessato
              ritrovato su tabella AMV_DIRITTI per l'area e tipologia indicati.
 PARAMETRI:   P_UTENTE       VARCHAR2    Codice del gruppo
              P_ID_AREA      NUMBER      Codice dell'area
              P_ID_TIPOLOGIA NUMBER      Codice della tipologia
 RITORNA:     VARCHAR2       tipo di diritto, Read, Create, Update
******************************************************************************/
FUNCTION get_albero (
  p_id_area      NUMBER
, p_pagina       VARCHAR2
) RETURN CLOB;
/******************************************************************************
 DESCRIZIONE: Riceve area (di documenti) di accesso e pagina che utilizza albero;
              Ritorna albero html.
 PARAMETRI:   P_ID_AREA      NUMBER      Codice dell'area
              P_PAGINA       NUMBER      url della pagina
 RITORNA:     CLOB       html relativo ad albero aree
******************************************************************************/
END Amv_Area;
/

