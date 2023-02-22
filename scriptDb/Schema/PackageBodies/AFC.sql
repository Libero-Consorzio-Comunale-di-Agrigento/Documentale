CREATE OR REPLACE PACKAGE BODY AFC IS
/******************************************************************************
 NOME:        AFC
 DESCRIZIONE: Procedure e Funzioni di utilita' comune.

 ANNOTAZIONI: -
 REVISIONI:
 Rev.  Data        Autore  Descrizione
 ----  ----------  ------  ----------------------------------------------------
 000   20/01/2003  MM      Prima emissione.
 001   26/04/2005  CZ      Aggiunte to_boolean e xor
 002   14/06/2005  MM      Introduzione funczione GET_SUBSTR
                           (p_stringa IN varchar2, p_separatore IN  varchar2
                           , p_occorrenza IN  varchar2).
 003   01/09/2005  FT      Aggiunta dei metodi protect_wildcard, version
                           aggiunta dei subtype t_object_name, t_message,
                           t_statement, t_revision
 004   27/09/2005  MF      Cambio nomenclatura s_revisione e s_revisione_body.
                           Tolta dipendenza get_stringParm da Package Si4.
                           Inserimento SQL_execute per istruzioni dinamiche.
 005   24/11/2005  FT      Aggiunta di mxor
 006   04/01/2006  MM      Aggiunta is_number
 007   12/01/2006  MM      Aggiunte is_numeric e to_number(p_value in varchar2),
                           corretta is_number, corretta get_substr in modo che
                           gestisca stringhe fino a 32000 caratteri.
 008   01/02/2006  FT      Aumento di parametri per mxor
 009   22/02/2006  FT      Aggiunta dei metodi get_field_condition e decode_value
 010   02/03/2006  FT      Aggiunta function SQL_execute
 011   21/03/2006  MF      get_filed_condition:
                           - Introdotto prefix e suffix
                           - return type t_statement
                           decode_value:
                           - return type t_statement
 012   26/04/2006  MM      Modifica get_stringParm.
 013   19/05/2006  FT      Aggiunta metodo to_clob
 014   25/06/2006  MF      Parametro in to_clob per ottenere empty in caso di null.
 015   28/06/2006  FT      Aggiunta function date_format; in get_field_condition,
                           modificata gestione di p_field_value (gestione di operatori
                           di default e scorporo dell'operatore da p_field_value)
                           e aggiunto parametro p_date_format
 016   30/08/2006  FT      Eliminazione della funzione to_clob
 017   04/09/2006  FT      Corretto get_field_condition: aggiunta di apici a p_field_value
                           in caso di 'like' implicito; controllo con 'lower' per caso di like
 018   05/09/2006  FT      Modificata gestione 'like' implicito e raddoppio apici singoli
                           in get_field_condition
 019   19/10/2006  FT      Aggiunta funzione quote
 020   30/10/2006  FT      Aggiunta funzione countOccurrenceOf
 021   21/12/2006  FT      Aggiunta funzione init_cronologia
 022   03/01/2007  FT      Modificata logica di gestione del flag in get_field_condition
                           per permettere di passare NULL e comportarsi come fosse il valore
                           di default
 023   27/02/2007  FT      Spostata funzione init_cronologia nel package SI4
 024   14/03/2007  FT      Aggiunta overloading di get_field_condition per p_field_value di tipo DATE
******************************************************************************/

s_revisione_body t_revision := '024';

--------------------------------------------------------------------------------

function versione return t_revision is
/******************************************************************************
 NOME:        VERSIONE
 DESCRIZIONE: Restituisce versione e revisione di distribuzione del package.

 RITORNA:     stringa VARCHAR2 contenente versione e revisione.
 NOTE:        Primo numero  : versione compatibilita del Package.
              Secondo numero: revisione del Package specification.
              Terzo numero  : revisione del Package body.
******************************************************************************/
begin
   return version( s_revisione, s_revisione_body );
end versione;

--------------------------------------------------------------------------------

function version
( p_revisione t_revision
, p_revisione_body t_revision
) return t_revision is
/******************************************************************************
 NOME:        VERSION
 DESCRIZIONE: Restituisce versione e revisione di distribuzione del package.

 PARAMETRI:   p_revisione      revisione del Package specification.
              p_revision_body  revisione del Package body.
 RITORNA:     stringa VARCHAR2 contenente versione e revisione.
 NOTE:        Primo numero  : versione compatibilita del Package.
              Secondo numero: revisione del Package specification.
              Terzo numero  : revisione del Package body.
******************************************************************************/
   d_result varchar2(10);
begin
   d_result := p_revisione||'.'||p_revisione_body;
   return d_result;
end version;

--------------------------------------------------------------------------------

function get_substr
( p_stringa    IN OUT varchar2
, p_separatore IN     varchar2
) return VARCHAR2 is
/******************************************************************************
 NOME:        GET_SUBSTR
 DESCRIZIONE: Ottiene la stringa precedente alla stringa di separazione, modificando
              la stringa di partenza con la parte seguente, escludendo la stringa di
              separazione.
 PARAMETRI:   p_stringa      Stringa da esaminare.
              p_separatore   Stringa di separazione.
 RITORNA:     varchar2: se trovata stringa di separazione : la sottostringa;
                        se non trovata                    : la stringa originale.
              Esempio:
                 da  stringa     ABCD.
                 con sub-stringa B.
                     ritorna A.
                     modificando l'originale in CD.
 ANNOTAZIONI: -
 REVISIONI:
 Rev.  Data        Autore  Descrizione
 ----  ----------  ------  ------------------------------------------------------
 000   7/01/2003   MM      Prima emissione.
******************************************************************************/
   sStringa     VARCHAR2(32000);
   iPos         INTEGER;
begin
   iPos := instr(p_stringa,p_separatore);
   if iPos = 0 then
      sStringa  := p_stringa;
      p_stringa := '';
   else
      sStringa  := substr(p_stringa,1,iPos - 1);
      p_stringa := substr(p_stringa,iPos+length(p_separatore));
   end if;
   RETURN sStringa;
end get_substr;

--------------------------------------------------------------------------------

function get_substr
( p_stringa    IN  varchar2
, p_separatore IN  varchar2
, p_occorrenza IN  varchar2
) return VARCHAR2 is
/******************************************************************************
 NOME:        GET_SUBSTR
 DESCRIZIONE: Ottiene la stringa precedente alla stringa di separazione.
 PARAMETRI:   p_stringa      Stringa da esaminare.
              p_separatore   Stringa di separazione.
           p_occorrenza   P o U a seconda che si voglia considerare la Prima
                          o l'ultima occorrenza della stringa di separazione.
 RITORNA:     varchar2: se trovata stringa di separazione : la sottostringa;
                        se non trovata                    : la stringa originale.
 ANNOTAZIONI: -
 REVISIONI:
 Rev.  Data        Autore  Descrizione
 ----  ----------  ------  ------------------------------------------------------
 000   14/04/2005  MM      Prima emissione.
******************************************************************************/
   sStringa     VARCHAR2(32000);
   iPos         INTEGER;
   iOcc         INTEGER;
begin
   if p_occorrenza = 'P' then
      iOcc := 1;
   elsif p_occorrenza = 'U' then
      iOcc := -1;
   else
      iOcc := 1;
   end if;
   iPos := instr(p_stringa,p_separatore,iOcc,1);
   if iPos = 0 then
      sStringa  := p_stringa;
   else
      sStringa  := substr(p_stringa,1,iPos - 1);
   end if;
   RETURN sStringa;
end get_substr;

--------------------------------------------------------------------------------

function get_stringParm
( p_stringa        IN varchar2
, p_identificativo IN varchar2
) return VARCHAR2 is
/******************************************************************************
 NOME:        GET_STRINGPARM.
 DESCRIZIONE: Estrapola un Parametro da una Stringa.
              L'identificativo puo essere :
                     /x      seguito da " " (spazio) - Case sensitive.
                     -x      seguito da " " (spazio) - Case sensitive.
                     X      seguito da "=" (uguale) - Ignore Case.
              Se il Parametro inizia con "'" (apice) o '"' (doppio apice)
                         viene estratto fino al prossimo apice o doppio apice;
              altrimenti
                         viene estratto fino allo " " (spazio).
 PARAMETRI:   p_Stringa        varchar2 Valore contenente la stringa da esaminare.
              p_Identificativo varchar2 Stringa identificativa del Parametro da estrarre.
 RITORNA:     varchar2: Valore del parametro estrapolato dalla stringa.
 ANNOTAZIONI: -
 REVISIONI:
 Rev.  Data        Autore  Descrizione
 ----  ----------  ------  ------------------------------------------------------
 000   17/01/2003  MM      Prima emissione.
 004   27/09/2005  MF      Tolta dipendenza get_stringParm da Package Si4.
 012   26/04/2006  MM      (BO14061) Ritorna un risultato errato se il valore del
                           parametro richiesto e nullo.
******************************************************************************/
   d_stringa   varchar2(2000);
   d_parametro varchar2(2000);
   d_termine   varchar2(2000);
   d_pos       integer;
begin
   d_stringa := lTrim(rTrim(p_stringa));
   if substr(p_identificativo,1,1) in ('/','-') then
      d_parametro := p_identificativo;
      d_pos       := instr(d_stringa,d_parametro);
   else
      d_parametro := upper(p_identificativo)||'=';
      d_pos       := instr(Upper(d_stringa),d_parametro);
   end if;
   if d_pos = 0 then
      RETURN '';
   else
     d_pos := d_pos + length(d_parametro);
   end if;
   d_stringa := rTrim(substr(d_stringa, d_pos));
   -- Carattere finale determinato in funzione del carattere iniziale
   if substr(ltrim(d_stringa),1,1) = '''' or substr(ltrim(d_stringa),1,1) = '"' then
      d_stringa := ltrim(d_stringa);
      d_termine := substr(d_stringa,1,1);
      d_stringa := substr(d_stringa,2);
   else
      d_termine := ' ';
   end if;

   d_stringa := GET_SUBSTR(d_stringa,d_termine);
   RETURN d_stringa;

end get_stringParm;

--------------------------------------------------------------------------------

function countOccurrenceOf
( p_stringa in varchar2
, p_sottostringa in varchar2)
return number
/******************************************************************************
 NOME:        countOccurrenceOf
 DESCRIZIONE: numero di occorrenze di p_sottostringa in p_stringa
 PARAMETRI:   p_stringa
              p_sottostringa
 RITORNA:     varchar2
 NOTE:        -
******************************************************************************/
is
   d_result integer:= 0;
   d_pos integer := 0;
begin
   d_pos := instr( p_stringa, p_sottostringa );
   while d_pos > 0 loop
      d_result := d_result + 1;
      d_pos := instr( p_stringa, p_sottostringa, d_pos + 1 );
   end loop;
   return d_result;
end countOccurrenceOf;

--------------------------------------------------------------------------------

function protect_wildcard
( p_stringa        in varchar2
) return varchar2 is
/******************************************************************************
 NOME:        protect_wildcard
 VISIBILITA': pubblica
 DESCRIZIONE: protezione dei caratteri speciali ('_' e '%') nella stringa p_stringa
 PARAMETRI:   p_stringa
 RITORNA:     VARCHAR2
 NOTE:        -
******************************************************************************/
   d_result varchar2(2000);
begin

   d_result := replace( p_stringa, '_', '\_' );
   d_result := replace( d_result, '%', '\%' );
   return d_result;

end protect_wildcard;

--------------------------------------------------------------------------------

function quote
( p_stringa   in varchar2
) return varchar2 is
/******************************************************************************
 NOME:        quote
 VISIBILITA': pubblica
 DESCRIZIONE: Gestione apici (aggiunta di quelli esterni e raddoppio di quelli
              interni) per la stringa p_stringa
 PARAMETRI:   p_stringa
 RITORNA:     VARCHAR2
 NOTE:        -
 REVISIONI:
 Rev.  Data        Autore  Descrizione
 ----  ----------  ------  ------------------------------------------------------
 019   19/10/2006  FT      Aggiunta funzione quote
******************************************************************************/
   d_result varchar2(2000);
begin

   d_result := replace( p_stringa, '''', '''''' );
   d_result := '''' || d_result || '''';
   return d_result;

end quote;

--------------------------------------------------------------------------------

function to_boolean
( p_value in number
)
return boolean is
/******************************************************************************
 NOME:        to_boolean
 VISIBILITA': pubblica
 DESCRIZIONE: conversione booleana di valori number (1,0)
 PARAMETRI:   p_value: number: 1 o 0
 RITORNA:     boolean: true se 1, false se 0
 NOTE:        accetta solo argomenti validi (non nulli: NON implementa logica
              booleana estesa al null)
******************************************************************************/
   d_result boolean;
begin
   DbC.PRE( p_value is not null );
   DbC.PRE( p_value = 1  or  p_value = 0 );

   if p_value = 1 then
      d_result := true;
   else
      d_result := false;
   end if;

   DbC.POST( d_result is not null );
   return  d_result;
end; -- AFC.to_boolean

--------------------------------------------------------------------------------

function to_number
( p_value in boolean
)
return number
/******************************************************************************
 NOME:        to_number
 VISIBILITA': pubblica
 DESCRIZIONE: conversione number di valori booleani
 PARAMETRI:   p_value: boolean: true o false
 RITORNA:     boolean: 1 se true, 0 se false
 NOTE:        accetta solo argomenti validi (non nulli: NON implementa logica
              booleana estesa al null)
******************************************************************************/
is
   d_result number;
begin
   DbC.PRE( p_value is not null );

   if p_value then
      d_result := 1;
   else
      d_result := 0;
   end if;

   DbC.POST( d_result is not null );
   DbC.POST( d_result = 1  or  d_result = 0 );
   return  d_result;
end; -- AFC.to_number

--------------------------------------------------------------------------------

function to_number
( p_value in varchar2
)
return number
/******************************************************************************
 NOME:        to_number
 VISIBILITA': pubblica
 DESCRIZIONE: conversione number di stringhe
 PARAMETRI:   p_value: varchar2
 RITORNA:     number corrispondente o exception
 NOTE:        In caso la stringa passata non sia un numero esce con eccezione
              ORA -06502.
******************************************************************************/
is
   d_result number;
begin
   d_result := standard.to_number(p_value);
   return  d_result;
end to_number;

--------------------------------------------------------------------------------

procedure SQL_execute
( p_stringa t_statement
) is
/******************************************************************************
 NOME:        SQL_execute
 DESCRIZIONE: Esegue lo statement passato.
 ARGOMENTI:   p_stringa varchar2 statement sql da eseguire
 ECCEZIONI:
 ANNOTAZIONI: -
 REVISIONI:
 Rev.  Data        Autore  Descrizione
 ----  ----------  ------  ------------------------------------------------------
 004   27/09/2005  MF      Cambio nomenclatura s_revisione e s_revisione_body.
                           Tolta dipendenza get_stringParm da Package Si4.
                           Inserimento SQL_execute per istruzioni dinamiche.
******************************************************************************/
   d_cursor         INTEGER;
   d_rows_processed INTEGER;
BEGIN
   d_cursor := dbms_sql.open_cursor;
   dbms_sql.parse( d_cursor, p_stringa, dbms_sql.native );
   d_rows_processed := dbms_sql.execute( d_cursor );
   dbms_sql.close_cursor( d_cursor );
EXCEPTION
   WHEN OTHERS THEN
      dbms_sql.close_cursor( d_cursor );
      raise;
END SQL_EXECUTE;

--------------------------------------------------------------------------------

function SQL_execute
( p_stringa t_statement
) return varchar2 is
/******************************************************************************
 NOME:        SQL_execute
 DESCRIZIONE: Esegue lo statement passato e rotorna il valore di ritorno.
 ARGOMENTI:   p_stringa varchar2 statement sql da eseguire
 ECCEZIONI:
 ANNOTAZIONI: -
 RITORNA:     varchar2 il valore di ritorno dello statement SQL p_stringa
******************************************************************************/
   d_cursor         INTEGER;
   d_rows_processed INTEGER;
   d_result         varchar2(32000);
begin

   d_cursor := dbms_sql.open_cursor;
   dbms_sql.parse( d_cursor, p_stringa, dbms_sql.native );
   dbms_sql.define_column( d_cursor, 1, d_result, 32000 );
   d_rows_processed := dbms_sql.execute( d_cursor );
   if dbms_sql.fetch_rows( d_cursor ) > 0
   then
      dbms_sql.column_value( d_cursor, 1, d_result );
   end if;

   dbms_sql.close_cursor( d_cursor );

   return d_result;

exception
   when others then
      dbms_sql.close_cursor( d_cursor );
      raise;
end SQL_execute;

--------------------------------------------------------------------------------

function xor
( p_value_1 in boolean
, p_value_2 in boolean
) return boolean is
/******************************************************************************
 NOME:        xor
 VISIBILITA': pubblica
 DESCRIZIONE: operatore booleano di or esclusivo
 PARAMETRI:   p_value_1: boolean
              p_value_2: boolean
 RITORNA:     boolean
 NOTE:        accetta solo argomenti validi (non nulli: NON implementa logica
              booleana estesa al null)
******************************************************************************/
   d_result boolean;
begin
   DbC.PRE( p_value_1 is not null );
   DbC.PRE( p_value_2 is not null );

   d_result := p_value_1 != p_value_2;

   DbC.POST( d_result is not null );
   return  d_result;
end; -- AFC.xor

--------------------------------------------------------------------------------

function xor
( p_value_1 in boolean
, p_value_2 in boolean
, p_value_3 in boolean
) return boolean is
/******************************************************************************
 NOME:        xor
 VISIBILITA': pubblica
 DESCRIZIONE: operatore booleano di or esclusivo
 PARAMETRI:   p_value_1: boolean
              p_value_2: boolean
 RITORNA:     boolean
 NOTE:        accetta solo argomenti validi (non nulli: NON implementa logica
              booleana estesa al null)
******************************************************************************/
   d_result boolean;
begin
   DbC.PRE( p_value_3 is not null );
   -- p_value_1 and p_value_2 checked into the binary base function

   d_result  := xor( p_value_1,  p_value_2 ) != p_value_3;

   DbC.POST( d_result is not null );
   return  d_result;
end; -- AFC.xor

--------------------------------------------------------------------------------

function xor
( p_value_1 in boolean
, p_value_2 in boolean
, p_value_3 in boolean
, p_value_4 in boolean
) return boolean is
/******************************************************************************
 NOME:        xor
 VISIBILITA': pubblica
 DESCRIZIONE: operatore booleano di or esclusivo
 PARAMETRI:   p_value_1: boolean
              p_value_2: boolean
              p_value_3: boolean
              p_value_4: boolean
 RITORNA:     boolean
 NOTE:        accetta solo argomenti validi (non nulli: NON implementa logica
              booleana estesa al null)
******************************************************************************/
   d_result boolean;
begin

   d_result  := xor( p_value_1,  p_value_2 ) != xor( p_value_3,  p_value_4 );

   DbC.POST( d_result is not null );
   return  d_result;
end; -- AFC.xor

--------------------------------------------------------------------------------

function mxor
( p_value_1 in boolean
, p_value_2 in boolean
, p_value_3 in boolean default false
, p_value_4 in boolean default false
, p_value_5 in boolean default false
, p_value_6 in boolean default false
, p_value_7 in boolean default false
, p_value_8 in boolean default false
) return boolean is
/******************************************************************************
 NOME:        mxor
 VISIBILITA': pubblica
 DESCRIZIONE: operatore booleano di or esclusivo: ritorna true se solo uno dei
              parametri e true e tutti gli altri sono false
 PARAMETRI:   p_value_1: boolean
              p_value_2: boolean
              p_value_3: boolean
              p_value_4: boolean
              p_value_5: boolean
              p_value_6: boolean
              p_value_7: boolean
              p_value_8: boolean
 RITORNA:     boolean
 NOTE:        funziona per 2, 3, 4, 5, 6, 7 e 8 operandi
******************************************************************************/
   d_result boolean;
begin

   d_result :=     p_value_1 and not p_value_2 and not p_value_3 and not p_value_4 and not p_value_5 and not p_value_6 and not p_value_7 and not p_value_8
            or not p_value_1 and     p_value_2 and not p_value_3 and not p_value_4 and not p_value_5 and not p_value_6 and not p_value_7 and not p_value_8
            or not p_value_1 and not p_value_2 and     p_value_3 and not p_value_4 and not p_value_5 and not p_value_6 and not p_value_7 and not p_value_8
            or not p_value_1 and not p_value_2 and not p_value_3 and     p_value_4 and not p_value_5 and not p_value_6 and not p_value_7 and not p_value_8
            or not p_value_1 and not p_value_2 and not p_value_3 and not p_value_4 and     p_value_5 and not p_value_6 and not p_value_7 and not p_value_8
            or not p_value_1 and not p_value_2 and not p_value_3 and not p_value_4 and not p_value_5 and     p_value_6 and not p_value_7 and not p_value_8
            or not p_value_1 and not p_value_2 and not p_value_3 and not p_value_4 and not p_value_5 and not p_value_6 and     p_value_7 and not p_value_8
            or not p_value_1 and not p_value_2 and not p_value_3 and not p_value_4 and not p_value_5 and not p_value_6 and not p_value_7 and     p_value_8
            ;

   DbC.POST( d_result is not null );
   return  d_result;
end; -- AFC.mxor

--------------------------------------------------------------------------------

function is_number
( p_char in varchar2
) return number is
/******************************************************************************
 NOME:        is_number
 VISIBILITA': pubblica
 DESCRIZIONE: Verifica che la stringa passata sia un numero.
 PARAMETRI:   p_char: varchar2 stringa da controllare.
 RITORNA:     number 1: e' un numero
                     0: NON e' un numero
 NOTE:        in caso che p_char sia nullo, la funzione ritorna 1.
******************************************************************************/
   d_result    number := 1;
   d_test      number;
begin
   d_test := to_number(p_char);
   RETURN d_result;
EXCEPTION
   WHEN OTHERS THEN
      if sqlcode = -6502 then
         RETURN 0;
      else
         raise;
      end if;
end is_number;

--------------------------------------------------------------------------------

function is_numeric
( p_char in varchar2
) return number is
/******************************************************************************
 NOME:        is_numeric
 VISIBILITA': pubblica
 DESCRIZIONE: Verifica che la stringa passata sia formata da soli numeri.
 PARAMETRI:   p_char: varchar2 stringa da controllare.
 RITORNA:     number 1: e' formata da soli numeri
                     0: NON e' formata da soli numeri
 NOTE:        in caso che p_char sia nullo, la funzione ritorna 0.
              La lunghezza massima della stringa passata e' 32000.
******************************************************************************/
   d_result    number := 0;
   d_translate varchar2(32000);
   d_compare   varchar2(32000);
   d_len       number := length(p_char);
   d_loop      number := 1;
begin
   if p_char is not null then
      d_translate := translate(p_char, '0123456789x', 'xxxxxxxxxxa');
      while d_loop <= d_len loop
         d_compare := d_compare||'x';
         d_loop := d_loop + 1;
      end loop;
      if d_compare = d_translate then
         d_result := 1;
      end if;
   end if;
   RETURN d_result;
end is_numeric;

--------------------------------------------------------------------------------

function get_field_condition
( p_prefix      in varchar2
, p_field_value in varchar2
, p_suffix      in varchar2
, p_flag        in number   default 0
, p_date_format in varchar2 default null
) return varchar2 is
/******************************************************************************
 NOME:        get_field_condition
 DESCRIZIONE: Ottiene stringa con condizione SQL.

 PARAMETRI:   p_prefix       stringa per prefissare la condizione
              p_field_value  valore da controllare
              p_suffix       stringa per suffissare la condizione
              p_flag         0= condizione per uguale
                             1= condizione indicata in valore
              p_date_format  se p_field_value ¿ di tipo date, contiene il formato
                             da utilizzare per effettuare la conversione

 RITORNA:     varchar2 con stringa SQL

 NOTE:        Se p_field_value e NULL ritorna NULL.

 REVISIONI:
 Rev.  Data        Autore  Descrizione
 ----  ----------  ------  ------------------------------------------------------
 009   22/02/2006  FT      Aggiunta dei metodi get_field_condition e decode_value
 011   21/03/2006  MF      - Introdotto prefix e suffix
                           - return type t_statement
 015   28/06/2006  FT      modificata gestione di p_field_value: gestione di operatori
                           di default e scorporo dell'operatore dal valore;
                           aggiunto parametro p_date_format
 017   04/09/2006  FT      Aggiunta di apici a p_field_value in caso di 'like' implicito;
                           controllo con 'lower' per caso di like
 018   05/09/2006  FT      Modificata gestione 'like' implicito e raddoppio apici singoli
 022   03/01/2007  FT      Modificata logica di gestione di p_flag per permettere di
                           passare NULL e comportarsi come fosse il valore di default (0)
******************************************************************************/
   d_result t_statement;
   d_operator varchar2(4);
   d_value t_statement;
   d_field_value AFC.t_statement;
begin

   d_field_value := ltrim( rtrim( p_field_value ) );
   if d_field_value is not null
   then
      if p_flag = 0
      or p_flag is null
      then
         if (  instr( d_field_value, '_' ) != 0
            or instr( d_field_value, '%' ) != 0
            )
         and not(   substr( d_field_value, 1, 1 ) = ''''
                and substr( d_field_value, -1 ) = ''''
                )
         then
            d_operator := 'like';
            d_value := '''' || replace( d_field_value, '''', '''''' ) || '''';
         else
            d_operator := '=';

            if  substr( d_field_value, 1, 1 ) = ''''
            and substr( d_field_value, -1 ) = ''''
            then
               d_value := d_field_value;
            else
               d_value := '''' || replace( d_field_value, '''', '''''' ) || '''';
            end if;
         end if;

         if p_date_format is not null
         then
            d_value := 'to_date( ' || d_value || ', ''' || p_date_format || ''' ) ';
         end if;

         d_value := d_operator || ' ' || d_value;

      elsif p_flag = 1
      then
         d_value := p_field_value;
      end if;

      d_result := p_prefix || ' ' || d_value || ' ' || p_suffix;

   end if;

   return d_result;

end get_field_condition;

--------------------------------------------------------------------------------

function get_field_condition
( p_prefix      in varchar2
, p_field_value in date
, p_suffix      in varchar2
, p_flag        in number   default 0
, p_date_format in varchar2 default null
) return varchar2 is
/******************************************************************************
 NOME:        get_field_condition
 DESCRIZIONE: Ottiene stringa con condizione SQL.

 PARAMETRI:   p_prefix       stringa per prefissare la condizione
              p_field_value  valore da controllare
              p_suffix       stringa per suffissare la condizione
              p_flag         0= condizione per uguale
                             1= condizione indicata in valore
              p_date_format  se p_field_value ¿ di tipo date, contiene il formato
                             da utilizzare per effettuare la conversione

 RITORNA:     varchar2 con stringa SQL

 NOTE:        overloading per field_value di tipo DATE

 REVISIONI:
 Rev.  Data        Autore  Descrizione
 ----  ----------  ------  ------------------------------------------------------
 024   14/03/2007  FT      Aggiunta overloading di get_field_condition per p_field_value
                           di tipo DATE
******************************************************************************/
   d_result t_statement;
begin

   d_result := get_field_condition( p_prefix => p_prefix
                                  , p_field_value => to_char( p_field_value, date_format )
                                  , p_suffix => p_suffix
                                  , p_flag => p_flag
                                  , p_date_format => p_date_format
                                  );

   return d_result;

end get_field_condition;

--------------------------------------------------------------------------------

function decode_value
( p_check_value in varchar2
, p_against_value in varchar2
, p_then_result in varchar2
, p_else_result in varchar2
) return varchar2 is
/******************************************************************************
 NOME:        decode_value
 DESCRIZIONE: Istruzione "decode" per PL/SQL.

 PARAMETRI:   p_check_value    valore da controllare
              p_against_value  valore di confronto
              p_then_result    risultato per uguale
              p_else_result    risultato per diverso

 RITORNA:     varchar2

 REVISIONI:
 Rev.  Data        Autore  Descrizione
 ----  ----------  ------  ------------------------------------------------------
 009   22/02/2006  FT      Aggiunta dei metodi get_field_condition e decode_value
 011   21/03/2006  MF      - return type t_statement
 ******************************************************************************/
   d_result t_statement;
begin

   if p_check_value = p_against_value
   or (   p_check_value is null
      and p_against_value is null
      )
   then
      d_result := p_then_result;
   else
      d_result := p_else_result;
   end if;

   return d_result;

end decode_value;

--------------------------------------------------------------------------------

function date_format
return varchar2 is
/******************************************************************************
 NOME:        date_format
 DESCRIZIONE: Ritorna il formato standard di conversione di una data.

 PARAMETRI:   -

 RITORNA:     varchar2

 REVISIONI:
 Rev.  Data        Autore  Descrizione
 ----  ----------  ------  ------------------------------------------------------
 015   19/05/2006  --      Prima emissione
******************************************************************************/
   d_result  varchar2(21);
begin

   d_result := 'dd/mm/yyyy hh24:mi:ss';

   return d_result;

end date_format;

--------------------------------------------------------------------------------

END AFC;
/

