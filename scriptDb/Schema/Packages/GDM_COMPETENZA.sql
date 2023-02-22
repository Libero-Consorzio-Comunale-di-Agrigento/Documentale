CREATE OR REPLACE PACKAGE GDM_COMPETENZA AS
 g_diritto NUMBER(1);
 TYPE DelCurTyp IS REF CURSOR;
 TYPE ARRSTRING IS VARRAY(100) OF VARCHAR2(10);
 PROCEDURE SET_TRUE;
 PROCEDURE SET_FALSE;
 PROCEDURE SI4_ASSEGNA_MULTIPLA (P_TIPO_OGGETTO IN VARCHAR2,P_OGGETTO IN VARCHAR2,P_TIPO_ABILITAZIONE IN VARCHAR2,P_UTENTE IN VARCHAR2,P_RUOLO IN VARCHAR2 DEFAULT NULL,P_AUTORE IN VARCHAR2,P_ACCESSO IN VARCHAR2 DEFAULT 'S',P_DAL IN VARCHAR2 DEFAULT NULL,P_AL IN VARCHAR2 DEFAULT NULL);
 PROCEDURE SI4_ASSEGNA (P_TIPO_OGGETTO IN VARCHAR2,P_OGGETTO IN VARCHAR2,P_TIPO_ABILITAZIONE IN VARCHAR2,P_UTENTE IN VARCHAR2,P_RUOLO IN VARCHAR2 DEFAULT NULL,P_AUTORE IN VARCHAR2,P_ACCESSO IN VARCHAR2 DEFAULT 'S',P_DAL IN VARCHAR2 DEFAULT NULL,P_AL IN VARCHAR2 DEFAULT NULL);
 FUNCTION ASSEGNA_COMP (P_TIPO_OGGETTO IN VARCHAR2,P_OGGETTO IN VARCHAR2,P_TIPO_ABILITAZIONE IN VARCHAR2,P_UTENTE IN VARCHAR2,P_RUOLO IN VARCHAR2 DEFAULT NULL,P_AUTORE IN VARCHAR2,P_ACCESSO IN VARCHAR2 DEFAULT 'S',P_DAL IN VARCHAR2 DEFAULT NULL,P_AL IN VARCHAR2 DEFAULT NULL, P_ID_FUNZIONE IN VARCHAR2 DEFAULT NULL) RETURN NUMBER;
 PROCEDURE GDM_ASSEGNA (P_TIPO_OGGETTO IN VARCHAR2,P_OGGETTO IN VARCHAR2,P_DA_TIPO_OGGETTO IN VARCHAR2,P_DA_OGGETTO IN VARCHAR2,P_AUTORE IN VARCHAR2,P_RUOLO IN VARCHAR2 DEFAULT NULL);
 FUNCTION SI4_VERIFICA (P_TIPO_OGGETTO IN VARCHAR2,P_OGGETTO IN VARCHAR2,P_TIPO_ABILITAZIONE IN VARCHAR2,P_UTENTE IN VARCHAR2,P_RUOLO IN VARCHAR2 DEFAULT NULL,P_DATA IN VARCHAR2 DEFAULT TO_CHAR(SYSDATE,'dd/mm/yyyy')) RETURN NUMBER;
 PROCEDURE GDM_ASSEGNA_GRUPPO (P_TIPO_OGGETTO IN VARCHAR2,P_OGGETTO IN VARCHAR2,P_TIPO_ABILITAZIONE IN VARCHAR2,P_UTENTE IN VARCHAR2,P_RUOLO IN VARCHAR2 DEFAULT NULL,P_AUTORE IN VARCHAR2,P_ACCESSO IN VARCHAR2 DEFAULT 'S',P_DAL IN VARCHAR2 DEFAULT NULL,P_AL IN VARCHAR2 DEFAULT NULL);
 PROCEDURE GDM_ASSEGNA_GRUPPO_MULTIPLA (P_TIPO_OGGETTO IN VARCHAR2,P_OGGETTO IN VARCHAR2,P_TIPO_ABILITAZIONE IN VARCHAR2,P_UTENTE IN VARCHAR2,P_RUOLO IN VARCHAR2 DEFAULT NULL,P_AUTORE IN VARCHAR2,P_ACCESSO IN VARCHAR2 DEFAULT 'S',P_DAL IN VARCHAR2 DEFAULT NULL,P_AL IN VARCHAR2 DEFAULT NULL);
 FUNCTION GDM_AGGIUNGI_A_TUTTI (P_TIPO_OGGETTO IN VARCHAR2,P_OGGETTO IN VARCHAR2,P_TIPO_ABILITAZIONE IN VARCHAR2,P_ACCESSO IN VARCHAR2 DEFAULT 'S',P_AUTORE IN VARCHAR2,P_DAL IN VARCHAR2 DEFAULT NULL,P_AL IN VARCHAR2 DEFAULT NULL) RETURN NUMBER;
 PROCEDURE GDM_ALLINEA_COMP_CQ_DOC (P_IDVIEW_CARTELLA NUMBER,P_ID_DOC NUMBER,P_AUTORE VARCHAR2,P_CART_QUERY VARCHAR2);
 PROCEDURE GDM_ELIMINA(P_TIPO_OGGETTO IN VARCHAR2,P_OGGETTO IN VARCHAR2);
 FUNCTION GDM_VERIFICA (P_TIPO_OGGETTO IN VARCHAR2,P_OGGETTO IN VARCHAR2,P_TIPO_ABILITAZIONE IN VARCHAR2,P_UTENTE IN VARCHAR2,P_RUOLO IN VARCHAR2 DEFAULT NULL,P_DATA IN VARCHAR2 DEFAULT TO_CHAR(SYSDATE,'dd/mm/yyyy'), P_CONTROLLA_IDPADRE IN VARCHAR2 DEFAULT 'N') RETURN NUMBER;
 FUNCTION GDM_TIPODOC_ACL (P_ID_TIPODOC IN VARCHAR2, P_TIPI_ABILITAZIONE IN VARCHAR2, P_UTENTE IN VARCHAR2,P_AUTORE IN VARCHAR2,P_ACCESSO IN VARCHAR2 DEFAULT 'S',P_RUOLO IN VARCHAR2 DEFAULT 'GDM', P_DAL IN VARCHAR2 DEFAULT TO_CHAR(SYSDATE, 'DD/MM/YYYY'),P_AL IN VARCHAR2 DEFAULT NULL, P_ID_FUNZIONE IN VARCHAR2 DEFAULT NULL) RETURN NUMBER;
 FUNCTION GDM_VER_FUNZ_TIPODOC(p_Null_Gestito IN NUMBER, p_Tipo_Oggetto IN VARCHAR2, p_Oggetto IN VARCHAR2, p_Tipo_Abilitazione IN VARCHAR2, p_Utente IN VARCHAR2, p_Ruolo IN VARCHAR2 DEFAULT NULL, p_Data IN VARCHAR2 DEFAULT TO_CHAR(SYSDATE,'dd/mm/yyyy')) RETURN NUMBER ;
 FUNCTION GDM_VERIFICA_DELEGA (P_TIPO_OGGETTO IN VARCHAR2,P_OGGETTO IN VARCHAR2,P_TIPO_ABILITAZIONE IN VARCHAR2,P_UTENTE IN VARCHAR2,P_RUOLO IN VARCHAR2 DEFAULT NULL,P_DATA IN VARCHAR2 DEFAULT TO_CHAR(SYSDATE,'dd/mm/yyyy')) RETURN NUMBER;
 FUNCTION GDM_GET_ABILITAZIONI (P_TIPO_OGGETTO IN VARCHAR2,P_OGGETTO IN VARCHAR2 DEFAULT NULL,P_TIPO_ABILITAZIONE IN VARCHAR2,P_UTENTE IN VARCHAR2 DEFAULT NULL,P_DATA IN VARCHAR2 DEFAULT TO_CHAR(SYSDATE,'dd/mm/yyyy')) RETURN DelCurTyp;
 PROCEDURE GDM_CHIUDI( P_UTENTE IN VARCHAR2, P_TIPO_OGGETTO IN VARCHAR2 DEFAULT NULL, P_DATA IN VARCHAR2 DEFAULT TO_CHAR(SYSDATE,'dd/mm/yyyy'), P_OGGETTO IN VARCHAR2 DEFAULT NULL, P_TIPO_ABILITAZIONE IN VARCHAR2 DEFAULT NULL, P_DATA_AL IN VARCHAR2 DEFAULT NULL);
 PROCEDURE GDM_ASSEGNA_DELEGA (P_TIPO_OGGETTO IN VARCHAR2,P_OGGETTO IN VARCHAR2,P_TIPO_ABILITAZIONE IN VARCHAR2, P_UTENTE IN VARCHAR2, P_RUOLO IN VARCHAR2 DEFAULT NULL, P_AUTORE IN VARCHAR2, P_ACCESSO IN VARCHAR2 DEFAULT 'S', P_DAL IN VARCHAR2 DEFAULT NULL, P_AL IN VARCHAR2 DEFAULT NULL);
 FUNCTION GDM_GET_ABILITAZIONI_NOME(P_UTENTE IN VARCHAR2, P_DATA IN VARCHAR2 DEFAULT TO_CHAR(SYSDATE,'dd/mm/yyyy')) RETURN VARCHAR2;
 FUNCTION GDM_GET_ABILITAZIONI_ELENCO(P_UTENTE IN VARCHAR2, P_DATA IN VARCHAR2 DEFAULT TO_CHAR(SYSDATE,'dd/mm/yyyy')) RETURN VARCHAR2;
 FUNCTION GDM_GET_DELEGANTI_ELENCO (p_utente IN VARCHAR2, p_data IN VARCHAR2 DEFAULT TO_CHAR(SYSDATE,'dd/mm/yyyy')) RETURN VARCHAR2;
 FUNCTION addSeparatorToAbil(p_abilitazioni VARCHAR2) RETURN VARCHAR2;
 FUNCTION getArrayListAbil(p_abilitazioni VARCHAR2) RETURN ARRSTRING;
 FUNCTION getStringException(P_CODE NUMBER, P_ABIL VARCHAR2, P_OGGETTO VARCHAR2) RETURN VARCHAR2;
 FUNCTION getElencoUtentiAccesso(P_TIPO_OGGETTO IN VARCHAR2,P_OGGETTO IN VARCHAR2,P_LISTAABILITAZIONI IN VARCHAR2, P_SOLOUTENTI IN VARCHAR2) RETURN CLOB;
 PROCEDURE gdm_comp_tipodoc_doc_allegati(id_doc NUMBER,p_utente VARCHAR2);
 PROCEDURE GDM_DISABILITA_COMP_ALLEGATI(P_ID_TIPODOC NUMBER,P_AUTORE VARCHAR2, P_COMPETENZA_ATTUALE_ALLEGATO VARCHAR2);
 FUNCTION  GDM_VERIFICA_GESTIONE_DELEGA (P_TIPO_OGGETTO IN VARCHAR2,P_TIPO_ABILITAZIONE IN VARCHAR2,P_UTENTE IN VARCHAR2,P_DATA IN VARCHAR2 DEFAULT TO_CHAR(SYSDATE,'dd/mm/yyyy')) RETURN VARCHAR2;
END GDM_COMPETENZA;
/

