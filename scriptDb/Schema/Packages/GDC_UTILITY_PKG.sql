CREATE OR REPLACE PACKAGE GDC_UTILITY_PKG
AS
/******************************************************************************
   NAME:       GDC_UTILITY_PKG
   PURPOSE:    Package di gestione del Client Documentale
   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        02/04/2008  DS               1. Created this package.
******************************************************************************/
   TYPE WRK_RIF IS REF CURSOR;
   TYPE STRING_ARRAY IS TABLE OF VARCHAR2(32767);
   FUNCTION F_TREEVIEW (
      a_wrksp    IN   VARCHAR2,
      a_utente   IN   VARCHAR2,
      a_ruolo    IN   VARCHAR2
   )
   RETURN VARCHAR2;
   FUNCTION F_LOGO (
      a_modulo    IN   VARCHAR2
   )
   RETURN VARCHAR2;
   FUNCTION F_LOGO_DOCUMENTO (
      a_modulo    IN   VARCHAR2
   )
   RETURN VARCHAR2;
   PROCEDURE F_CREA_COLLEGAMENTI (
    LISTA_DOC IN VARCHAR2, LISTA_COLL IN VARCHAR2, UTENTE IN VARCHAR2
   );
   FUNCTION F_JDMSMANUALI (
      WRKSP    IN   VARCHAR2
   )
   RETURN VARCHAR2;
   PROCEDURE F_JDMSMANUALI_INSERT_UPDATE (
      WRKSP    IN   VARCHAR2,
      PATHM VARCHAR2
   );
   PROCEDURE F_JDMSMANUALI_DELETE(
      WRKSP    IN   VARCHAR2
   );
   FUNCTION F_JDMSMANUALI_EXIST (
      WRKSP    IN   VARCHAR2
   )
   RETURN  VARCHAR2;
   FUNCTION F_LISTWRKSP (
     a_utente IN VARCHAR2,
     a_ruolo IN VARCHAR2
   )
   RETURN WRK_RIF;
   FUNCTION F_LISTWRKSP_ML (
     a_utente IN VARCHAR2,
     a_ruolo IN VARCHAR2,
     a_lingua IN VARCHAR2
   )
   RETURN WRK_RIF;
   FUNCTION F_SETWRKSP (
     a_wrksp IN VARCHAR2,
     a_modulo IN VARCHAR2,
     a_utente IN VARCHAR2
   )
   RETURN  NUMBER;
   FUNCTION F_GETWORKAREA( p_oggetto varchar2,
                           p_tipo_oggetto varchar2,
                           p_utente varchar2)
   RETURN WRK_RIF;
   /******************************************************************************
     nome:        set_password_utente
     descrizione: Esegue modifica della password dell'utente.
   ******************************************************************************/
   procedure set_password_utente (
         p_new_password  VARCHAR2
       , p_old_password  VARCHAR2
   );
   /***********************************************************************************************
   DESCRIZIONE: Viene concatenato nel filtro della ricerca la sequnenza di coppie CODICE_MODELLO_FIGLIO@CODICE_MODELLO_PADRE
                utilizzato dal client per la visualizzazione in lettura/modifica del documento con quel relativo codice modello
                figlio selezionato. Questa sequenza viene costruita nella parte Lista Modelli-Modello di Ricerca Modulistica
                dell'amministratore.
   PARAMETRI:   P_AREA               ARCHAR2  area
                P_CODICE_MODELLO    VARCHAR2  codice modello
                P_SEQUENZA          VARCHAR2 sequenza da appendere
  ************************************************************************************************/
   PROCEDURE P_APPEND_FILTRO_QUERY(
             P_AREA IN VARCHAR2,
             P_CODICE_MODELLO IN VARCHAR2,
             P_SEQUENZA IN VARCHAR2);
   PROCEDURE P_INSERT_COLLEGAMENTO_ESTERNO(P_ID_CARTELLA IN NUMBER, P_NOME IN VARCHAR2, P_URL IN VARCHAR2, P_ICONA IN VARCHAR2, P_TOOLTIP IN VARCHAR2, P_TIPO_LINK IN VARCHAR2, P_UTENTE IN VARCHAR2);
   FUNCTION F_GET_URL_OGGETTO(P_SERVER_URL IN VARCHAR2,P_CONTEXT_PATH IN VARCHAR2,P_ID_OGGETTO IN VARCHAR2,
                              P_TIPO_OGGETTO IN VARCHAR2,P_AREA IN VARCHAR2,P_CM IN VARCHAR2,
                              P_CR IN VARCHAR2,P_RW IN VARCHAR2,P_ID_CARTPROVENIENZA IN VARCHAR2,
                              P_ID_QUERYPROVENIENZA IN VARCHAR2,P_TAG IN VARCHAR2 DEFAULT '1',
                              P_JAVASCRIPT IN VARCHAR2 DEFAULT 'S')
   RETURN VARCHAR2;
   FUNCTION SPLIT_STRING (STR IN VARCHAR2, DELIMETER IN CHAR)
   RETURN STRING_ARRAY;
END GDC_UTILITY_PKG;
/

