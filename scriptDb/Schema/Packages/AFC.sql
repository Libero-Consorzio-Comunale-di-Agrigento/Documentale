CREATE OR REPLACE PACKAGE AFC IS
/******************************************************************************
 NOME:        AFC
 DESCRIZIONE: Procedure e Funzioni di utilita' comune.

 ANNOTAZIONI: -
 REVISIONI:
 Rev.  Data        Autore  Descrizione
 ----  ----------  ------  ----------------------------------------------------
 00    20/01/2003  MM      Prima emissione.
 01    18/03/2005  MF      Adozione nello Standard AFC (nessuna modifica).
 02    26/04/2005  CZ      Aggiunte to_boolean e xor
 03    14/06/2005  MM      Creazione GET_SUBSTR ( p_stringa    IN  varchar2
                                                , p_separatore IN  varchar2
                                                , p_occorrenza IN  varchar2
                                                )
 04    01/09/2005  FT      Aggiunta dei metodi protect_wildcard, version
                           aggiunta dei subtype t_object_name, t_message,
                           t_statement, t_revision
 05    27/09/2005  MF      Cambio nomenclatura s_revisione e s_revisione_body.
                           Tolta dipendenza get_stringParm da Package Si4.
 06    24/11/2005  FT      Aggiunta di mxor
 07    04/01/2006  MM      Aggiunta is_number
 08    12/01/2006  MM      Aggiunta is_numeric e to_number(p_value in varchar2)
 09    01/02/2006  FT      Aumento di parametri per mxor
 10    22/02/2006  FT      Aggiunta dei metodi get_field_condition e decode_value
                           e del type t_ref_cursor
 11    02/03/2006  FT      Aggiunta della function SQL_execute
 12    21/03/2006  MF      Get_filed_condition: Introdotto prefix e suffix.
 13    19/05/2006  FT      Aggiunta metodo to_clob
 14    25/06/2006  MF      Parametro in to_clob per ottenere empty in caso di null.
 15    28/06/2006  FT      Aggiunta funzione date_format e parametro p_date_format
                           in get_field_condition
 16    30/08/2006  FT      Modifica dichiarazione subtype per incompatibilit¿ con
                           versione 7 di Oracle; eliminazione della funzione to_clob
 17    19/10/2006  FT      Aggiunta funzione quote
 18    30/10/2006  FT      Aggiunta funzione countOccurrenceOf
 19    21/12/2006  FT      Aggiunta funzione init_cronologia
 20    27/02/2007  FT      Spostata funzione init_cronologia nel package SI4
 21    14/03/2007  FT      Aggiunta overloading di get_field_condition per
                           p_field_value di tipo DATE
******************************************************************************/
   d_revision  VARCHAR2(30);
   SUBTYPE t_revision IS d_revision%TYPE;
   d_object_name  VARCHAR2(30);
   SUBTYPE t_object_name IS d_object_name%TYPE;
   d_message  VARCHAR2(1000);
   subtype t_message is d_message%TYPE;
   d_statement  VARCHAR2(32000);
   subtype t_statement is d_statement%TYPE;

   type t_ref_cursor is REF CURSOR;

   s_revisione t_revision := 'V1.21';

   function versione return t_revision;
   pragma restrict_references(versione, WNDS, WNPS);

   function version
   ( p_revisione t_revision
   , p_revisione_body t_revision
   ) return t_revision;
   pragma restrict_references(version, WNDS, WNPS );

   -- Ottiene la stringa precedente alla stringa di separazione, modificando
   -- la stringa di partenza con la parte seguente, escludendo la stringa di
   -- separazione
   function get_substr
   ( p_stringa    IN OUT varchar2
   , p_separatore IN     varchar2
   ) return VARCHAR2;
   pragma restrict_references(get_substr, WNDS);
   function get_substr
   ( p_stringa    IN  varchar2
   , p_separatore IN  varchar2
   , p_occorrenza IN  varchar2
   ) return VARCHAR2;
   pragma restrict_references(get_substr, WNDS);

   -- Estrapola un Parametro da una Stringa
   function get_stringParm
   ( p_stringa        IN VARCHAR2
   , p_identificativo IN VARCHAR2
   ) return VARCHAR2;
   pragma restrict_references(get_stringParm, WNDS);

   function countOccurrenceOf
   ( p_stringa in varchar2
   , p_sottostringa in varchar2)
   return number;

   -- Protezione dei caratteri speciali ('_' e '%') nella stringa p_stringa
   function protect_wildcard
   ( p_stringa        IN VARCHAR2
   ) return VARCHAR2;
   pragma restrict_references(protect_wildcard, WNDS);

   -- Gestione apici (aggiunta di quelli esterni e raddoppio di quelli interni)
   -- per la stringa p_stringa
   function quote
   ( p_stringa   in varchar2
   ) return varchar2;

   -- Cast number [0,1] => boolean [false, true]
   -- null arguments are NOT handled
   function to_boolean
   ( p_value in number
   ) return boolean;
   pragma restrict_references( to_boolean, WNDS );

   -- Cast boolean [false, true] => number [0,1]
   -- null arguments are NOT handled
   function to_number
   ( p_value in boolean
   ) return number;
   pragma restrict_references( to_number, WNDS );
   function to_number
   ( p_value in varchar2
   )
   return number;
   pragma restrict_references( to_number, WNDS );

   -- Esecuzione istruzione dinamica
   procedure SQL_execute
   ( p_stringa t_statement
   );

   -- Esecuzione istruzione dinamica con valore di ritorno
   function SQL_execute
   ( p_stringa t_statement
   ) return varchar2;

   -- Exclusive xor
   -- null arguments are NOT handled
   function xor
   ( p_value_1 in boolean
   , p_value_2 in boolean
   ) return boolean;
   pragma restrict_references( xor, WNDS );
   function xor
   ( p_value_1 in boolean
   , p_value_2 in boolean
   , p_value_3 in boolean
   ) return boolean;
   pragma restrict_references( xor, WNDS );
   function xor
   ( p_value_1 in boolean
   , p_value_2 in boolean
   , p_value_3 in boolean
   , p_value_4 in boolean
   ) return boolean;
   pragma restrict_references( xor, WNDS );

   -- Multiple xor
   function mxor
   ( p_value_1 in boolean
   , p_value_2 in boolean
   , p_value_3 in boolean default false
   , p_value_4 in boolean default false
   , p_value_5 in boolean default false
   , p_value_6 in boolean default false
   , p_value_7 in boolean default false
   , p_value_8 in boolean default false
   ) return boolean;
   pragma restrict_references( mxor, WNDS );

   -- Verifica che la stringa passata sia un numero
   function is_number
   ( p_char in varchar2) return number;
   pragma restrict_references( is_number, WNDS );

   -- Verifica che la stringa passata sia formata da soli numeri
   function is_numeric
   ( p_char in varchar2) return number;
   pragma restrict_references( is_numeric, WNDS );

   -- Ottiene stringa con condizione SQL
   function get_field_condition
   ( p_prefix      in varchar2
   , p_field_value in varchar2
   , p_suffix      in varchar2
   , p_flag        in number   default 0
   , p_date_format in varchar2 default null
   ) return varchar2;

   -- Ottiene stringa con condizione SQL
   -- overloading per p_field_condition di tipo DATE
   function get_field_condition
   ( p_prefix      in varchar2
   , p_field_value in date
   , p_suffix      in varchar2
   , p_flag        in number   default 0
   , p_date_format in varchar2 default null
   ) return varchar2;

   -- Istruzione "decode" per PL/SQL
   function decode_value
   ( p_check_value in varchar2
   , p_against_value in varchar2
   , p_then_result in varchar2
   , p_else_result in varchar2
   ) return varchar2;

   function date_format
   return varchar2;

END AFC;
/

