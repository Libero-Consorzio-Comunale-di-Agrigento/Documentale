CREATE OR REPLACE PACKAGE SI4 IS
/******************************************************************************
 NOME:        SI4.
 DESCRIZIONE: Procedure e Funzioni di utilita comune.
 ANNOTAZIONI:
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 00   25/01/2001 SN     Inserito commento
 01   29/06/2001 MM     Inserite le variabili di connessione, la funzione
                        VERSIONE, le procedure GET_ACCESSO_UTENTE e
                        SET_ACCESSO_UTENTE.
 02   05/03/2002 MM     Inserita la funzione GET_ERROR e relativa variabile
                        d_ErrMsg.
 03   28/10/2003 MM     Modifica della funzione GET_ERROR con:
                        - introduzione dell'uso del package KeyPackage;
                        - introduzione della valorizzazione di eventuali
                          parametri presenti nel messaggio.
 04   27/09/2005 MF     Introduzione Revisione differenziata tra Specification e Body.
                         Inclusione funzione get_stringParm di Package AFC.
                        Function GET_ERROR con parametro Stack Trace.
 05   12/01/2006 MF     Get_error: Aggiunto parametro esclusione Codice Applicativo da messaggio.
                        Gestito messaggio con Codice Applicativo che termina con ritorno a capo.
                        Migliorata manipolazione dello stack.
 06   27/02/2007 FT     Aggiunto metodo init_cronologia
 07   27/10/2010 SNeg   Aggiunte funzioni di get
******************************************************************************/
   s_revisione AFC.t_revision := 'V1.07';
   UTENTE        varchar2(8);
   NOTE_UTENTE   varchar2(2000);
   MODULO        varchar2(10);
   NOTE_MODULO   varchar2(2000);
   ISTANZA       varchar2(10);
   NOTE_ISTANZA  varchar2(2000);
   NOTE_ACCESSO  varchar2(2000);
   ENTE          varchar2(4);
   NOTE_ENTE     varchar2(2000);
   PROGETTO      varchar2(8);
   NOTE_PROGETTO varchar2(2000);
   AMBIENTE      varchar2(8);
   function VERSIONE return VARCHAR2;
   pragma restrict_references(versione, WNDS, WNPS);
   procedure SQL_EXECUTE
   ( p_stringa  varchar2
   );
   procedure VIEW_MODIFY
   ( nome_vista varchar2
   , testo      varchar2
   , operatore  varchar2
   );
   function NEXT_ID
   ( table_name varchar2
   , column_id  varchar2  default 'ID'
   , new_id     integer   default null
   , locked     number    default 1     --- Di default esegue lock
   ) return INTEGER;
   procedure GET_ACCESSO_UTENTE
   ( p_utente        IN OUT varchar2
   , p_note_utente   IN OUT varchar2
   , p_modulo        IN OUT varchar2
   , p_note_modulo   IN OUT varchar2
   , p_istanza       IN OUT varchar2
   , p_note_istanza  IN OUT varchar2
   , p_note_accesso  IN OUT varchar2
   , p_ente          IN OUT varchar2
   , p_note_ente     IN OUT varchar2
   , p_progetto      IN OUT varchar2
   , p_note_progetto IN OUT varchar2
   , p_ambiente      IN OUT varchar2
   );
   pragma restrict_references ( GET_ACCESSO_UTENTE, WNDS, WNPS );
   procedure SET_ACCESSO_UTENTE
   ( p_utente        varchar2
   , p_note_utente   varchar2
   , p_modulo        varchar2
   , p_note_modulo   varchar2
   , p_istanza       varchar2
   , p_note_istanza  varchar2
   , p_note_accesso  varchar2
   , p_ente          varchar2
   , p_note_ente     varchar2
   , p_progetto      varchar2
   , p_note_progetto varchar2
   , p_ambiente      varchar2
   );
   function GET_ERROR
   ( p_errore       varchar2
   , p_stack        number default 0
   , p_exclude_code number default 0
   ) return VARCHAR2;
   function  GET_NTEXT
   ( p_tabella IN varchar2
   , p_colonna IN varchar2
   , p_pk      IN varchar2
   , p_lingua  IN varchar2
   ) return VARCHAR2;
   pragma restrict_references( GET_NTEXT, WNDS);
   function  GET_NTEXT
   ( p_tabella IN varchar2
   , p_colonna IN varchar2
   , p_pk      IN varchar2
   ) return VARCHAR2;
   pragma restrict_references( GET_NTEXT, WNDS);
   function  GET_NTEXT
   ( p_pk      IN varchar2
   , p_lingua  IN varchar2
   ) return VARCHAR2;
   pragma restrict_references( GET_NTEXT, WNDS);
   function  GET_NTEXT
   ( p_pk      IN varchar2
   ) return VARCHAR2;
   pragma restrict_references( GET_NTEXT, WNDS);
   procedure SET_NTEXT
   ( p_tabella IN varchar2
   , p_colonna IN varchar2
   , p_pk      IN varchar2
   , p_lingua  IN varchar2
   , p_testo   IN VARCHAR2);
   procedure SET_NTEXT
   ( p_tabella IN varchar2
   , p_colonna IN varchar2
   , p_pk      IN varchar2
   , p_testo   IN VARCHAR2);
   procedure SET_NTEXT
   ( p_pk      IN varchar2
   , p_lingua  IN varchar2
   , p_testo   IN VARCHAR2);
   procedure SET_NTEXT
   ( p_pk      IN varchar2
   , p_testo   IN VARCHAR2);
   function GET_STRINGPARM
   ( p_stringa        IN VARCHAR2
   , p_identificativo IN VARCHAR2
   , p_where          IN VARCHAR2 default null
   ) return VARCHAR2;
   pragma restrict_references( GET_STRINGPARM, WNDS);
   -- Valorizza i campi UTENTE e DATA_AGG con i relativi valori
   procedure init_cronologia
   ( p_utente   in out varchar2
   , p_data_agg in out date
   );
   function get_UTENTE        return VARCHAR2;
   function get_NOTE_UTENTE   return VARCHAR2;
   function get_MODULO        return VARCHAR2;
   function get_NOTE_MODULO   return VARCHAR2;
   function get_ISTANZA       return VARCHAR2;
   function get_NOTE_ISTANZA  return VARCHAR2;
   function get_NOTE_ACCESSO  return VARCHAR2;
   function get_ENTE          return VARCHAR2;
   function get_NOTE_ENTE     return VARCHAR2;
   function get_PROGETTO      return VARCHAR2;
   function get_NOTE_PROGETTO return VARCHAR2;
   function get_AMBIENTE      return VARCHAR2;
END SI4;
/

