CREATE OR REPLACE PACKAGE Amv_Revisione AS
/******************************************************************************
 NOME:        AMV_WEB
 DESCRIZIONE: Gestione Revisioni dei Documenti
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    16/11/2004 AO     Prima emissione.
 1    27/05/2005 AO     Revisione per versione 2005.05
******************************************************************************************/
d_versione  VARCHAR2(20) := 'V2005.05';
d_revisione VARCHAR2(30) := '12   27/05/2005';
FUNCTION versione RETURN VARCHAR2;
PROCEDURE creazione_revisione (
  P_ID_DOCUMENTO         NUMBER
, P_REVISIONE            NUMBER
, P_UTENTE               VARCHAR2
, P_UTENTE_REDAZIONE     VARCHAR2
, P_UTENTE_VERIFICA      VARCHAR2
, P_UTENTE_APPROVAZIONE  VARCHAR2
, P_INIZIO_PUBBLICAZIONE DATE
, P_FINE_PUBBLICAZIONE   DATE
);
/***********************************************************************************************
 DESCRIZIONE: .
 PARAMETRI:
 RITORNA:
************************************************************************************************/
PROCEDURE gestisci_revisione (
  p_id_documento NUMBER
, p_revisione    NUMBER
, p_stato_futuro VARCHAR2
, p_note         VARCHAR2
, p_autore       VARCHAR2
, p_destinatario VARCHAR2 DEFAULT NULL
);
/***********************************************************************************************
 DESCRIZIONE: .
 PARAMETRI:
 RITORNA:
************************************************************************************************/
PROCEDURE pubblicazione_revisione (
  p_id_documento         NUMBER
, p_revisione            NUMBER
, p_inizio_pubblicazione DATE
, p_fine_pubblicazione   DATE
);
/***********************************************************************************************
 DESCRIZIONE: .
 PARAMETRI:
 RITORNA:
************************************************************************************************/
PROCEDURE notifica_revisione (
  p_utente IN VARCHAR2
, p_msg IN VARCHAR2
);
FUNCTION get_diritto (
  p_utente       VARCHAR2
, p_id_documento NUMBER
, p_revisione    NUMBER
, p_elenco       NUMBER DEFAULT 0
) RETURN VARCHAR2;
/***********************************************************************************************
 DESCRIZIONE: .
 PARAMETRI:
 RITORNA:
************************************************************************************************/
FUNCTION get_diritto_revisione (
  p_utente       VARCHAR2
, p_id_documento NUMBER
, p_revisione    NUMBER
) RETURN VARCHAR2;
/***********************************************************************************************
 DESCRIZIONE: .
 PARAMETRI:
 RITORNA:
************************************************************************************************/
FUNCTION get_diritto_modifica (
  p_utente       VARCHAR2
, p_id_documento NUMBER
, p_revisione    NUMBER
) RETURN NUMBER;
/***********************************************************************************************
 DESCRIZIONE: .
 PARAMETRI:
 RITORNA:
************************************************************************************************/
FUNCTION get_flusso (
  p_utente       VARCHAR2
, p_id_documento NUMBER
, p_revisione    NUMBER
, p_stato        VARCHAR2
, p_tipo         VARCHAR2 DEFAULT NULL
, p_args         VARCHAR2 DEFAULT NULL
) RETURN VARCHAR2;
/***********************************************************************************************
 DESCRIZIONE: .
 PARAMETRI: p_tipo: se p_tipo diverso da null emette anche flusso indietro
 RITORNA:
************************************************************************************************/
FUNCTION get_img_stato (
  p_stato        VARCHAR2
 ,p_tipo_testo   VARCHAR2 DEFAULT NULL
) RETURN VARCHAR2;
/***********************************************************************************************
 DESCRIZIONE: Ritorna l'immagine da visualizzare e la descrizione dello stato del documento
 PARAMETRI: p_stato
 RITORNA:
************************************************************************************************/
FUNCTION get_des_stato (
  p_stato        VARCHAR2
 ,p_tipo_testo   VARCHAR2 DEFAULT NULL
) RETURN VARCHAR2;
/***********************************************************************************************
 DESCRIZIONE: Ritorna la descrizione dello stato del documento
 PARAMETRI: p_stato
 RITORNA:
************************************************************************************************/
FUNCTION get_stato (
  p_id_documento        NUMBER
, p_revisione         NUMBER
) RETURN VARCHAR2;
/***********************************************************************************************
 DESCRIZIONE: Ritorna lo stato del documento
 PARAMETRI: p_id_documento
          p_revisione
 RITORNA: varchar2
************************************************************************************************/
END Amv_Revisione;
/

