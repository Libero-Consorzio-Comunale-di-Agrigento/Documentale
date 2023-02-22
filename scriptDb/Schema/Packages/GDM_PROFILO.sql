CREATE OR REPLACE PACKAGE gdm_profilo
AS
   TYPE VALORI_CAMPO IS RECORD (
        VALORE_STRINGA      CLOB,
        VALORE_NUMERO       NUMBER(15,5),
        VALORE_DATA         DATE,
        TIPO                DATI.TIPO%TYPE,
        FORMATO_DATA        DATI.FORMATO_DATA%TYPE,
        IN_USO              DATI_MODELLO.IN_USO%TYPE,
        ID_CAMPO            CAMPI_DOCUMENTO.ID_CAMPO%TYPE,
        SENZA_SALVATAGGIO   DATI.SENZA_SALVATAGGIO%TYPE,
        SENZA_AGGIORNAMENTO DATI.SENZA_AGGIORNAMENTO%TYPE,
        TIPO_LOG            DATI.TIPO_LOG%TYPE,
        LUNGHEZZA           DATI.LUNGHEZZA%TYPE
   );
   TYPE CAMPO IS TABLE OF VALORI_CAMPO INDEX BY VARCHAR2(1000);
   vTypeCampo VALORI_CAMPO;
   vCampo     CAMPO;
   PROCEDURE ADDCAMPO(P_AREA VARCHAR2, P_MODELLO VARCHAR2, P_NOME_CAMPO VARCHAR2, P_VALORE CLOB);
   PROCEDURE ADDCAMPO(P_AREA VARCHAR2, P_MODELLO VARCHAR2, P_NOME_CAMPO VARCHAR2, P_VALORE NUMBER);
   PROCEDURE ADDCAMPO(P_AREA VARCHAR2, P_MODELLO VARCHAR2, P_NOME_CAMPO VARCHAR2, P_VALORE DATE);
   PROCEDURE RESETCAMPO(P_AREA VARCHAR2, P_MODELLO VARCHAR2, P_NOME_CAMPO VARCHAR2);
   PROCEDURE RESETCAMPI;
   FUNCTION get_allegati (p_id_documento IN NUMBER)
      RETURN afc.t_ref_cursor;
   FUNCTION getdocumento (
      p_codice_modello     IN   VARCHAR2,
      p_area               IN   VARCHAR2,
      p_codice_richiesta   IN   VARCHAR2
   )
      RETURN NUMBER;
   FUNCTION valida (p_documento IN NUMBER, p_utente IN VARCHAR2)
      RETURN NUMBER;
   FUNCTION cancella (p_documento IN NUMBER, p_utente IN VARCHAR2)
      RETURN NUMBER;
   FUNCTION annulla (p_documento IN NUMBER, p_utente IN VARCHAR2)
      RETURN NUMBER;
   FUNCTION cambia_stato (
      p_documento   IN   NUMBER,
      p_utente      IN   VARCHAR2,
      p_stato       IN   VARCHAR2
   )
      RETURN NUMBER;
   FUNCTION duplica_documento (p_documento NUMBER, p_utente VARCHAR2)
      RETURN NUMBER;
   FUNCTION duplica_documento (p_documento NUMBER, p_utente VARCHAR2, p_duplica_tabella_oriz NUMBER)
      RETURN NUMBER;
   FUNCTION consentidocumento (
      p_utente                   VARCHAR2,
      p_tipi_abilitazione   IN   VARCHAR2,
      p_id_documento             VARCHAR2,
      p_autore              IN   VARCHAR2 DEFAULT NULL,
      p_ruolo               IN   VARCHAR2 DEFAULT 'GDM',
      p_escludicomp         IN   VARCHAR2 DEFAULT 'N'
   )
      RETURN NUMBER;
   FUNCTION consentidocumentodaa (
      p_utente                   VARCHAR2,
      p_tipi_abilitazione   IN   VARCHAR2,
      p_id_documento             VARCHAR2,
      p_dal                      VARCHAR2,
      p_al                       VARCHAR2,
      p_autore              IN   VARCHAR2 DEFAULT NULL,
      p_escludicomp         IN   VARCHAR2 DEFAULT 'N'
   )
      RETURN NUMBER;
   FUNCTION negadocumentodaa (
      p_utente                   VARCHAR2,
      p_tipi_abilitazione   IN   VARCHAR2,
      p_id_documento             VARCHAR2,
      p_dal                      VARCHAR2,
      p_al                       VARCHAR2,
      p_autore              IN   VARCHAR2 DEFAULT NULL,
      p_escludicomp         IN   VARCHAR2 DEFAULT 'N'
   )
      RETURN NUMBER;
   FUNCTION negadocumento (
      p_utente                   VARCHAR2,
      p_tipi_abilitazione   IN   VARCHAR2,
      p_id_documento             VARCHAR2,
      p_autore              IN   VARCHAR2 DEFAULT NULL,
      p_ruolo               IN   VARCHAR2 DEFAULT 'GDM',
      p_escludicomp         IN   VARCHAR2 DEFAULT 'N'
   )
      RETURN NUMBER;
   FUNCTION esistedocumento (p_id_documento NUMBER)
      RETURN VARCHAR2;
   FUNCTION crea_documento (p_area VARCHAR2, p_modello VARCHAR2, p_cr VARCHAR2, p_utente VARCHAR2, p_crea_record_orizzontale NUMBER) RETURN NUMBER;
   FUNCTION crea_documento (p_area VARCHAR2, p_modello VARCHAR2, p_cr VARCHAR2, p_utente VARCHAR2) RETURN NUMBER;
   FUNCTION crea_documento (p_area VARCHAR2, p_modello VARCHAR2, p_cr VARCHAR2, p_utente VARCHAR2, p_creaCompetenzeUtente BOOLEAN) RETURN NUMBER;
   FUNCTION cambiamodelloarea_documento (
      p_codice_modello   IN   VARCHAR2,
      p_area             IN   VARCHAR2,
      p_iddocumento      IN   NUMBER,
      p_user                  VARCHAR2
   )
      RETURN NUMBER;
   PROCEDURE delete_allegati(P_ID_DOCUMENTO IN NUMBER);
 PROCEDURE insert_update_allegato
   (    p_id_documento in number,
        p_filename in varchar2,
        p_estensione in varchar2,
        p_file     in BLOB,
        p_utente in varchar2) ;
   FUNCTION insert_update_allegato
   (    p_id_documento in number,
        p_filename in varchar2,
        p_estensione in varchar2,
        p_file     in BLOB,
        p_utente in varchar2,
        p_fileonfs in number default 0,
        p_typefs varchar2 default 'LNX') RETURN NUMBER ;
END gdm_profilo;
/

