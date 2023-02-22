CREATE OR REPLACE PACKAGE KeyPackage IS
/******************************************************************************
 NOME:        KeyPackage.
 DESCRIZIONE: Procedure e Funzioni di gestione Key Constraint and Error.
 ANNOTAZIONI: Gestione delle tabelle KEY_... .
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 00   27/10/2003 MM     Creazione.
 01   27/09/2005 MF     Gestione Precisazione di KEY_CONSTRAINT_ERROR.
 02   12/01/2006 MF     Get_error: Aggiunto parametro esclusione Codice da messaggio.
 03   08/09/2006 FT     Compatibilità subtype varchar2 a Oracle 7.
******************************************************************************/
   s_revisione AFC.t_revision := 'V1.03';
   d_error_text varchar2(2000);
   subtype t_error_text is d_error_text%type;
   function VERSIONE return VARCHAR2;
   pragma restrict_references(versione, WNDS, WNPS);
   function GET_ERROR
   ( p_errore       varchar2
   , p_exclude_code number default 0
   ) return varchar2;
   pragma restrict_references( GET_ERROR, WNDS);
   function GET_ERRORCODE
   ( p_error    VARCHAR2
   , p_ErrorKey varchar2
   ) return varchar2;
   pragma restrict_references( GET_ERRORCODE, WNDS);
   function GET_ERROR_PRECISAZIONE
   ( p_error    VARCHAR2
   , p_ErrorKey varchar2
   ) return varchar2;
   pragma restrict_references( GET_ERROR_PRECISAZIONE, WNDS);
   function GET_NTEXT
   ( p_tabella IN varchar2
   , p_colonna IN varchar2
   , p_pk      IN varchar2
   , p_lingua  IN varchar2
   ) return varchar2;
   pragma restrict_references( GET_NTEXT, WNDS);
   function GET_NTEXT
   ( p_tabella IN VARCHAR2
   , p_colonna IN VARCHAR2
   , p_pk      IN varchar2
   ) return varchar2;
   pragma restrict_references( GET_NTEXT, WNDS);
   function GET_NTEXT
   ( p_pk      IN varchar2
   , p_lingua  IN varchar2
   ) return varchar2;
   pragma restrict_references( GET_NTEXT, WNDS);
   function  GET_NTEXT
   ( p_pk      IN varchar2
   ) return varchar2;
   pragma restrict_references( GET_NTEXT, WNDS);
   procedure SET_NTEXT
   ( p_tabella IN varchar2
   , p_colonna IN varchar2
   , p_pk      IN varchar2
   , p_lingua  IN varchar2
   , p_testo   IN varchar2
   );
   procedure SET_NTEXT
   ( p_tabella IN varchar2
   , p_colonna IN varchar2
   , p_pk      IN varchar2
   , p_testo   IN varchar2
   );
   procedure SET_NTEXT
   ( p_pk      IN varchar2
   , p_lingua  IN varchar2
   , p_testo   IN varchar2
   );
   procedure SET_NTEXT
   ( p_pk      IN varchar2
   , p_testo   IN varchar2
   );
END KeyPackage;
/

