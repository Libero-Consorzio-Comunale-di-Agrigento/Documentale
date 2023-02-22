CREATE OR REPLACE PACKAGE GDM_CARTELLE AS
   FUNCTION CREA_CARTELLA(A_AREA IN VARCHAR2, A_MODELLO IN VARCHAR2, A_NOME_CARTELLA IN VARCHAR2, A_ID_CARTELLA_PADRE IN NUMBER, A_UTENTE IN VARCHAR2) RETURN NUMBER;
   FUNCTION CREA_CARTELLA(A_AREA IN VARCHAR2, A_MODELLO IN VARCHAR2, A_NOME_CARTELLA IN VARCHAR2, A_ID_CARTELLA_PADRE IN NUMBER, A_UTENTE IN VARCHAR2, A_CREA_RECORD_ORIZZONTALE IN NUMBER) RETURN NUMBER;
   PROCEDURE ADDCAMPO(P_AREA VARCHAR2, P_MODELLO VARCHAR2, P_NOME_CAMPO VARCHAR2, P_VALORE CLOB);
   PROCEDURE ADDCAMPO(P_AREA VARCHAR2, P_MODELLO VARCHAR2, P_NOME_CAMPO VARCHAR2, P_VALORE NUMBER);
   PROCEDURE ADDCAMPO(P_AREA VARCHAR2, P_MODELLO VARCHAR2, P_NOME_CAMPO VARCHAR2, P_VALORE DATE);
   PROCEDURE RESETCAMPO(P_AREA VARCHAR2, P_MODELLO VARCHAR2, P_NOME_CAMPO VARCHAR2);
   PROCEDURE RESETCAMPI;
   PROCEDURE INSERISCI_OGGETTO(A_CARTELLA IN NUMBER, A_OGGETTO IN NUMBER, A_TIPO_OGGETTO IN VARCHAR2, A_UTENTE IN VARCHAR2);
   PROCEDURE CREA_COLLEGAMENTO_DESKTOP (A_CATEGORIA IN VARCHAR2,A_CARTELLA_PADRE IN NUMBER,A_USER IN VARCHAR2);
   PROCEDURE CREA_COLLEGAMENTO (A_CARTELLA IN NUMBER,A_CARTELLA_PADRE IN NUMBER,A_USER IN VARCHAR2);
   PROCEDURE ELIMINA_COLLEGAMENTO_DESKTOP (A_CATEGORIA IN VARCHAR2,A_CARTELLA_PADRE IN NUMBER);
   PROCEDURE ELIMINA_COLLEGAMENTO (A_CARTELLA IN NUMBER,A_CARTELLA_PADRE IN NUMBER);
   PROCEDURE SPOSTA_COLLEGAMENTO (A_CARTELLA IN NUMBER,A_CARTELLA_PADRE IN NUMBER,A_NEW_CARTELLA_PADRE IN NUMBER,A_USER IN VARCHAR2);
   FUNCTION F_VERIFICA_LEGGE1_COLLEGAMENTO (A_CARTELLA_ORIGINE IN NUMBER,A_CARTELLA_PADRE_DESTINAZIONE IN NUMBER) RETURN NUMBER;
   FUNCTION F_VERIFICA_LEGGE2_COLLEGAMENTO (A_CARTELLA IN NUMBER,A_CARTELLA_PADRE IN NUMBER) RETURN NUMBER;
   FUNCTION F_VERIFICA_COLLEGAMENTO (A_CARTELLA IN NUMBER,A_CARTELLA_PADRE IN NUMBER) RETURN NUMBER;
END GDM_CARTELLE;
/

