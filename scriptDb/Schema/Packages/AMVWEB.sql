CREATE OR REPLACE PACKAGE Amvweb AS
/******************************************************************************
 NOME:        AMV_WEB
 DESCRIZIONE: Gestione interfacce Ambiente Mini Vortal verso il web
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 0    10/02/2003 AO     Prima emissione.
 1    05/04/2003 AO     Gestione generale delle preferenze
                        Eliminazione procedure set_style e function get_stile
 2    11/07/2003 AO     Aggiunta funzione get per gestione stringhe in multilinguismo
 3    05/08/2003 AO     Modificata funzione get per gestione preferenza HomePage
 4    27/11/2003 AO     Aggiunto parametro funzione get
 5    16/11/2004 AO     Aggiunta function SEND_MSG
 6    30/11/2004 AO     Aggiunta get_preferenza per preferenze di sezione
 7    27/05/2005 AO     Aggiornamento per versione 2005.05
******************************************************************************************/
d_versione  VARCHAR2(20) := 'V2005.05';
d_revisione VARCHAR2(30) := '12   27/05/2005';
FUNCTION versione RETURN VARCHAR2;
PROCEDURE set_preferenza (
  p_stringa VARCHAR2
, p_valore  VARCHAR2
, p_modulo  VARCHAR2 DEFAULT NULL
, p_utente  VARCHAR2 DEFAULT NULL
);
FUNCTION get_preferenza (
  p_stringa VARCHAR2
, p_modulo  VARCHAR2 DEFAULT NULL
, p_utente  VARCHAR2 DEFAULT NULL
) RETURN VARCHAR2;
/***********************************************************************************************
 DESCRIZIONE: Trova una preferenza cercando nella tabella REGISTRO la preferenza a livello di utente,
              se non e definita cerca a livello di portale ed infine cerca sulla chiave PRODUCT/NomeModulo.
 PARAMETRI:   P_STRINGA   VARCHAR2  Nome preferenza
              P_MODULO    VARCHAR2  Modulo AD4 per il quale cercare la chiave
 RITORNA:     VARCHAR2  : valore della preferenza
************************************************************************************************/
FUNCTION get_preferenza (
  p_stringa    VARCHAR2
, p_id_sezione NUMBER
, p_modulo     VARCHAR2 DEFAULT NULL
, p_utente     VARCHAR2 DEFAULT NULL
) RETURN VARCHAR2;
/***********************************************************************************************
 DESCRIZIONE: Trova una preferenza cercando anche le impostazioni relative alla sezione
 PARAMETRI:   P_STRINGA   VARCHAR2  Nome preferenza
              P_MODULO    VARCHAR2  Modulo AD4 per il quale cercare la chiave
 RITORNA:     VARCHAR2  : valore della preferenza
************************************************************************************************/
FUNCTION get_preferenza_utente (
  p_stringa VARCHAR2
, p_modulo  VARCHAR2 DEFAULT NULL
, p_utente  VARCHAR2 DEFAULT NULL
) RETURN VARCHAR2;
/***********************************************************************************************
 DESCRIZIONE: Trova una preferenza a livello di utente cercando nella tabella REGISTRO
            la chiave SI4_DB_USERS/NomeSi4User|NomeUser/PRODUCTS/NomeModulo o in seconda istanza
              la chiave SI4_DB_USERS/NomeSi4User|NomeUser.
 PARAMETRI:   P_STRINGA   VARCHAR2  Nome preferenza
              P_MODULO    VARCHAR2  Modulo AD4 per il quale cercare la chiave.
              P_UTENTE    VARCHAR2  Codice utente AD4
 RITORNA:     VARCHAR2  : valore della preferenza
************************************************************************************************/
FUNCTION get_preferenza_portale (
  p_stringa VARCHAR2
, p_modulo  VARCHAR2 DEFAULT NULL
) RETURN VARCHAR2;
/***********************************************************************************************
 DESCRIZIONE: Trova una preferenza a livello di portale cercando nella tabella REGISTRO
              la chiave DB_USER|NomeUser/PRODUCTS/NomeModulo o in seconda istanza la chiave
              DB_USER|NomeUser.
 PARAMETRI:   P_STRINGA   VARCHAR2  Nome preferenza
              P_MODULO    VARCHAR2  Modulo AD4 per il quale cercare la chiave
 RITORNA:     VARCHAR2  : valore della preferenza
************************************************************************************************/
FUNCTION get_returnpage (
  p_stringa VARCHAR2
) RETURN VARCHAR2;
/***********************************************************************************************
 DESCRIZIONE: Trova una preferenza a livello di portale cercando nella tabella REGISTRO
            la chiave DB_USER|NomeUser/PRODUCTS/NomeModulo o in seconda istanza la chiave
              DB_USER|NomeUser.
 PARAMETRI:   P_STRINGA   VARCHAR2  Nome preferenza
              P_MODULO    VARCHAR2  Modulo AD4 per il quale cercare la chiave
 RITORNA:     VARCHAR2  : valore della preferenza
************************************************************************************************/
FUNCTION is_preferenza (
  p_stringa VARCHAR2
, p_modulo  VARCHAR2 DEFAULT NULL
, p_utente  VARCHAR2 DEFAULT NULL
) RETURN VARCHAR2;
/***********************************************************************************************
 DESCRIZIONE: Verifica che la preferenza sia stata impostata specificamente per i parametri
              passati.
 PARAMETRI:   P_STRINGA   VARCHAR2  Nome preferenza
              P_MODULO    VARCHAR2  Modulo AD4 per il quale cercare la chiave
 RITORNA:     VARCHAR2  : 1 preferenza impostata, 0 preferenza non impostata
************************************************************************************************/
FUNCTION get (
  p_stringa VARCHAR2
, p_par1    VARCHAR2 DEFAULT NULL
, p_par2    VARCHAR2 DEFAULT NULL
, p_par3    VARCHAR2 DEFAULT NULL
, p_par4    VARCHAR2 DEFAULT NULL
) RETURN VARCHAR2;
/***********************************************************************************************
 DESCRIZIONE: Ottiene il valore corrispondente alla stringa indicata. Questo permette la estendibilita
            ad ambiente multilingue.
 PARAMETRI:   P_STRINGA   VARCHAR2  Nome preferenza
              P_PAR1      VARCHAR2  Eventuale parametro utile al ritrovamento del valore
              P_PAR2      VARCHAR2  Eventuale parametro utile al ritrovamento del valore
              P_PAR3      VARCHAR2  Eventuale parametro utile al ritrovamento del valore
              P_PAR4      VARCHAR2  Eventuale parametro utile al ritrovamento del valore
 RITORNA:     VARCHAR2  : valore corrispondente alla stringa passata
************************************************************************************************/
FUNCTION SEND_MSG
( sender IN VARCHAR2
, receiver IN VARCHAR2
, subject IN VARCHAR2
, text IN VARCHAR2
, msg_type IN VARCHAR2
)
RETURN INTEGER;
/******************************************************************************
 DESCRIZIONE: Invia un messaggio usando il Si4cim.
 PARAMETRI:   sender   varchar2 Mittente del messaggio
              receiver varchar2 Destinatario
              subject  varchar2 Oggetto del messaggio
              text     varchar2 Testo del messaggio
              msg_type varchar2 Tipo del messaggio configurati nel SI4CIM
 RITORNA:     varchar2 Eventuale messaggio di errore del SI4CIM
******************************************************************************/
END Amvweb;
/

