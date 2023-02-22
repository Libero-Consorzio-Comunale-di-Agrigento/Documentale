CREATE OR REPLACE PACKAGE BODY SI4 IS
/******************************************************************************
 NOME:        SI4.
 DESCRIZIONE: Procedure e Funzioni di utilita comune.
 ANNOTAZIONI:
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 000  25/01/2001 SN     Inserito commento
 001  29/06/2001 MM     Inserite le variabili di connessione, la funzione
                        VERSIONE, le procedure GET_ACCESSO_UTENTE e
                        SET_ACCESSO_UTENTE.
 002  05/03/2002 MM     Inserita la funzione GET_ERROR e relativa variabile
                        d_ErrMsg.
 003  28/10/2003 MM     Modifica della funzione GET_ERROR con:
                        - introduzione dell'uso del package KeyPackage;
                        - introduzione della valorizzazione di eventuali
                          parametri presenti nel messaggio.
 004  27/09/2005 MF     Introduzione Revisione differenziata tra Specification e Body.
                        Inclusione funzione get_stringParm di Package AFC.
                        Chiamata a SQL_Execute di Package AFC.
                        Modifiche a function Get_ERROR.
 005  12/01/2006 MF     Get_error: Aggiunto parametro esclusione Codice Applicativo da messaggio.
                        Gestito messaggio con Codice Applicativo che termina con ritorno a capo.
                        Migliorata manipolazione dello stack.
 006  05/05/2006 MF     Get_error: Risolve errata interpretazione del . che segue un $ terminale di variabile.
                        - Cerca variabili solo fino al prossimo '$' e non '$ '.
                        - Se trova la variabile terminata da $ la toglie.
 007  27/02/2007 FT     Aggiunto metodo init_cronologia
 008  07/06/2010 MF     Lunghezza variabili interne di manipolazione Errore da 2000 a 32000.
 009  27/10/2010 SNeg   Aggiunte funzioni di get
 010  29/6/2016  Sneg   Modificata view modify x consentire creazione di viste con testo più lungo di 32000
*******************************************************************************/
s_revisione_body AFC.t_revision := '010';
function VERSIONE return varchar2 is
/******************************************************************************
 NOME:        VERSIONE
 DESCRIZIONE: Restituisce versione e revisione di distribuzione del package.
 RITORNA:     stringa VARCHAR2 contenente versione e revisione.
 NOTE:        Primo numero  : versione compatibilita del Package.
              Secondo numero: revisione del Package specification.
              Terzo numero  : revisione del Package body.
******************************************************************************/
begin
   return AFC.version( s_revisione, s_revisione_body );
end VERSIONE;
procedure SQL_EXECUTE
( p_stringa varchar2
) is
/******************************************************************************
 NOME:        SQL_EXECUTE
 DESCRIZIONE: Esegue lo statement passato.
 ARGOMENTI:   p_stringa varchar2 statement sql da eseguire
 ECCEZIONI:
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 000  25/01/2001 SN     Inserito commento.
 004  27/09/2005 MF     Chiamata a SQL_Eexecute di Package AFC.
******************************************************************************/
BEGIN
   AFC.SQL_execute( p_stringa );
END SQL_EXECUTE;
 PROCEDURE VIEW_MODIFY (nome_vista    VARCHAR2,
                          testo         VARCHAR2,
                          operatore     VARCHAR2)
   IS
   /******************************************************************************
    NOME:        VIEW_MODIFY
    DESCRIZIONE: Modifica il testo della vista in base all'operatore indicato
                 usando il testo specificato.
    ARGOMENTI:   nome_vista varchar2 vista da modificare
                 testo      varchar2 codice da inserire nella vista
                 operatore  varchar2 create,replace,union, intersect o minus
    ECCEZIONI:   -20999, con messaggio che specifica l'errore verificatosi.
    ANNOTAZIONI:
    REVISIONI:
    Rev. Data       Autore Descrizione
    ---- ---------- ------ ------------------------------------------------------
    000             SN     Prima emissione.
    001  25/01/2001 SN     Inserito commento.
    010  29/6/2016  Sneg   Modificata view modify x consentire creazione di viste con testo più lungo di 32000
   ******************************************************************************/
   d_v_testo     LONG;                                      --varchar2(32000);
   d_testo       LONG;                                      --varchar2(32000);
   d_pos         NUMBER;
   parte         BOOLEAN;
   cursor_name   INTEGER;
   v_num_riga    NUMBER := 0;
   text_table    DBMS_SQL.varchar2a;
   PROCEDURE get_view_text (p_value IN VARCHAR2)
   AS
      l_cursor          INTEGER DEFAULT DBMS_SQL.open_cursor ;
      l_n               NUMBER;
      l_long_val        VARCHAR2 (32000);
      l_long_len        NUMBER;
      l_buflen          NUMBER := 32000;
      l_curpos          NUMBER := 0;
      v_pos_operatore   NUMBER;
      v_query VARCHAR2 (250)
            := 'select text from user_views where view_name = :x' ;
   BEGIN
      DBMS_SQL.parse (l_cursor, v_query, DBMS_SQL.native);
      DBMS_SQL.bind_variable (l_cursor, ':x', p_value);
      DBMS_SQL.define_column_long (l_cursor, 1);
      l_n := DBMS_SQL.execute (l_cursor);
      IF (DBMS_SQL.fetch_rows (l_cursor) > 0)
      THEN
         LOOP
            DBMS_SQL.column_value_long (l_cursor,
                                        1,
                                        l_buflen,
                                        l_curpos,
                                        l_long_val,
                                        l_long_len);
            v_pos_operatore := INSTR (l_long_val, 'UNION', -1);
            IF v_pos_operatore IS NULL
            THEN
               v_pos_operatore := INSTR (l_long_val, 'MINUS', -1);
               IF v_pos_operatore IS NULL
               THEN
                  v_pos_operatore := INSTR (l_long_val, 'INTERSECT', -1);
               END IF;
            END IF;
            IF v_pos_operatore IS NOT NULL and v_pos_operatore  != 1
            THEN
               DBMS_OUTPUT.put_line ('pos union NON NULLO');
               l_long_len := v_pos_operatore - 1;
               l_long_val := SUBSTR (l_long_val, 1, v_pos_operatore - 1);
            END IF;
            DBMS_OUTPUT.put_line (
               'pos union ' || INSTR (l_long_val, 'UNION', -1)
            );
            l_curpos := l_curpos + l_long_len;
            IF v_num_riga = 0
            THEN
               v_num_riga := v_num_riga + 1;
               text_table (v_num_riga) :=
                  'create or replace view ' || p_value || ' as ';
            END IF;
            v_num_riga := v_num_riga + 1;
            text_table (v_num_riga) := l_long_val;
            EXIT WHEN l_long_len = 0;
         END LOOP;
      END IF;
      --   dbms_output.put_line( '====================' );
      --   dbms_output.put_line( 'Long was ' || l_curpos ||
      --                         ' bytes in length' );
      DBMS_SQL.close_cursor (l_cursor);
   EXCEPTION
      WHEN OTHERS
      THEN
         IF DBMS_SQL.is_open (l_cursor)
         THEN
            DBMS_SQL.close_cursor (l_cursor);
         END IF;
         RAISE;
   END get_view_text;
   PROCEDURE esegui (p_text_table DBMS_SQL.varchar2a)
   IS
      d_cursor    INTEGER;
      d_ret_val   INTEGER;
   BEGIN
      IF p_text_table.COUNT > 0
      THEN
         d_cursor := DBMS_SQL.open_cursor;
         --         dbms_output.put_line( 'da ' || p_text_table.FIRST || ' a ' || p_text_table.LAST);
         DBMS_SQL.parse (d_cursor,
                         p_text_table,
                         p_text_table.FIRST,
                         p_text_table.LAST,
                         TRUE,
                         DBMS_SQL.native);
         d_ret_val := DBMS_SQL.EXECUTE (d_cursor);
         DBMS_SQL.close_cursor (d_cursor);
      END IF;
   EXCEPTION
      WHEN OTHERS
      THEN
         DBMS_SQL.close_cursor (d_cursor);
         RAISE;
   END;
   BEGIN
      IF operatore IS NULL OR UPPER (operatore) = 'REPLACE'
      THEN
         v_num_riga := v_num_riga + 1;
         text_table (v_num_riga) :=
            'create or replace view ' || nome_vista || ' as ';
         v_num_riga := v_num_riga + 1;
         text_table (v_num_riga) := ' ' || testo;
      ELSIF UPPER (operatore) = 'CREATE'
      THEN
         v_num_riga := v_num_riga + 1;
         text_table (v_num_riga) :=
            'create or replace view ' || nome_vista || ' as ';
         v_num_riga := v_num_riga + 1;
         text_table (v_num_riga) := ' ' || testo;
      ELSIF UPPER (operatore) IN ('MINUS', 'INTERSECT', 'UNION')
      THEN
        /* operatore per legame con la vista preesistente*/
        <<estrazione_testo>>
         BEGIN
            get_view_text (nome_vista);
            -- controllo se è una sottoparte della vista
            parte := FALSE;
            FOR indx IN NVL (text_table.FIRST, 0) .. NVL (text_table.LAST,
                                                          -1)
            LOOP
               --        dbms_output.put_line('lunghezza ' || length(v_testo));
               d_v_testo :=
                  UPPER(TRANSLATE (text_table (indx),
                                   'a ' || CHR (10) || CHR (13),
                                   'a'));
               d_testo :=
                  UPPER (
                     TRANSLATE (testo, 'a ' || CHR (10) || CHR (13), 'a')
                  );
               d_pos := INSTR (d_v_testo, d_testo);
               IF d_pos > 0
               THEN
                  parte := TRUE;
               END IF;
            END LOOP;
            IF parte = FALSE
            THEN
               /* NON e' sottoparte */
               v_num_riga := v_num_riga + 1;
               text_table (v_num_riga) := ' ' || operatore || ' ' || testo;
            END IF;
         EXCEPTION
            WHEN NO_DATA_FOUND
            THEN
               IF UPPER (operatore) = 'UNION'
               THEN
                  /* creo comunque la vista */
                  v_num_riga := v_num_riga + 1;
                  text_table (v_num_riga) := ' ' || testo;
               ELSE        /* operatore Intersect o Minus e vista esistente */
                  raise_application_error (
                     -20999,
                        'Vista '
                     || nome_vista
                     || ' inesistente, operatore '
                     || operatore
                     || ' non valido'
                  );
               END IF;
         END estrazione_testo;
      ELSE                                          /* operatore non valido */
         raise_application_error (-20999,
                                  'Operatore ' || operatore || ' non valido');
      END IF;
      esegui (text_table);
   END VIEW_MODIFY;
function NEXT_ID
( table_name varchar2
, column_id  varchar2 default 'ID'
, new_id     integer  default null
, locked     number   default 1
) return integer is
/******************************************************************************
 NOME:        NEXT_ID
 DESCRIZIONE: Genera numerazioni progressive sulla table come valore max + 1
              della colonna indicata.
 PARAMETRI:   table_name varchar2          tabella da numerare
              column_id  varchar2 default 'ID' colonna con numerazione
              new_id     integer  default null numerazione proposta
              locked     number   default 1    per impedire modifiche simultanee
 RITORNA:     INTEGER valore con cui effettuare la prossima numerazione.
 ECCEZIONI:   -20999, Valore proposto per numerazione > massimo attuale
 ANNOTAZIONI: -
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 000             MM     Prima emissione.
 001  25/01/2001 SN     Inserito commento.
******************************************************************************/
   d_cursor         integer;
   d_rows_processed integer;
   d_sql_next_val   varchar2(32000);
   d_next_id        integer;
   d_lock_table     varchar2(2000);
BEGIN
   d_sql_next_val := 'select nvl(max(' || column_id || '),0)+ 1 ' ||
                  '  from ' || table_name ;
   if locked = 1 then
      -- Modificato il 22/01/2001
     -- nonostante il lock exclusivo le select sulla tabella sono consentite
       d_lock_table := 'lock table ' || table_name || ' in exclusive mode';
     SI4.SQL_EXECUTE(d_lock_table);
   end if;
   BEGIN
      d_cursor := dbms_sql.open_cursor;
      dbms_sql.parse( d_cursor, d_sql_next_val, dbms_sql.native );
      dbms_sql.define_column( d_cursor, 1, d_next_id );
      d_rows_processed:= dbms_sql.execute( d_cursor );
      if dbms_sql.fetch_rows( d_cursor ) > 0 then
        dbms_sql.column_value( d_cursor, 1, d_next_id );
      else
        raise_application_error( -20999,'Impossibile numerare' );
      end if;
      dbms_sql.close_cursor(d_cursor);
      EXCEPTION
      WHEN OTHERS THEN
         dbms_sql.close_cursor(d_cursor);
         raise;
   END;
   if ( new_id is not null ) then
      if ( new_id > d_next_id ) then
         raise_application_error( -20999, 'Progressivo numerazione ' || to_char( new_id ) || ' non attribuibile' );
      else
         d_next_id := new_id;
      end if;
   end if;
   return d_next_id;
END NEXT_ID;
procedure GET_ACCESSO_UTENTE
( p_utente IN OUT varchar2, p_note_utente IN OUT varchar2
, p_modulo IN OUT varchar2, p_note_modulo IN OUT varchar2
, p_istanza IN OUT varchar2, p_note_istanza IN OUT varchar2
, p_note_accesso IN OUT varchar2
, p_ente IN OUT varchar2, p_note_ente IN OUT varchar2
, p_progetto IN OUT varchar2, p_note_progetto IN OUT varchar2
, p_ambiente IN OUT varchar2
) is
/******************************************************************************
 NOME:        GET_ACCESSO_UTENTE
 DESCRIZIONE: Restituisce i valori di tutte le variabili di ambiente (sono riempite in fase di connessione ai singoli applicativi).
 ARGOMENTI:   p_utente  varchar2         codice utente di applicazione
              p_note_utente varchar2     note relative ad utente di applicazione
              p_modulo varchar2          codice modulo di connessione
              p_note_modulo varchar2     note relative a modulo di connessione
              p_istanza varchar2         codice istanza di connessione
              p_note_istanza varchar2    note relative a istanza di connessione
              p_note_accesso varchar2    note relative a diritto di accesso di connessione
              p_ente varchar2            codice ente associato all'istanza di connessione
              p_note_ente varchar2       note relative ad ente di connessione
              p_progetto varchar2        codice progetto di connessione
              p_note_progetto varchar2   note relative a progetto di connessione
              p_ambiente varchar2        codice ambiente di connessione (SI, A00, AD4)
 ECCEZIONI:   --
 ANNOTAZIONI: --
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 000  29/06/2001 MM     Creazione.
******************************************************************************/
BEGIN
   p_utente       := UTENTE;
   p_note_utente  := NOTE_UTENTE;
   p_modulo       := MODULO;
   p_note_modulo  := NOTE_MODULO;
   p_istanza      := ISTANZA;
   p_note_istanza := NOTE_ISTANZA;
   p_note_accesso := NOTE_ACCESSO;
   p_ente         := ENTE;
   p_note_ente    := NOTE_ENTE;
   p_progetto     := PROGETTO;
   p_note_progetto:= NOTE_PROGETTO;
   p_ambiente     := AMBIENTE;
END GET_ACCESSO_UTENTE;
procedure SET_ACCESSO_UTENTE
( p_utente varchar2, p_note_utente varchar2
, p_modulo varchar2, p_note_modulo varchar2
, p_istanza varchar2, p_note_istanza varchar2
, p_note_accesso varchar2
, p_ente varchar2, p_note_ente varchar2
, p_progetto varchar2, p_note_progetto varchar2
, p_ambiente varchar2
) is
/******************************************************************************
 NOME:        SET_ACCESSO_UTENTE
 DESCRIZIONE: Permette di riempire tutte le variabili di ambiente.
 ARGOMENTI:   p_utente  varchar2         codice utente di applicazione
              p_note_utente varchar2     note relative ad utente di applicazione
              p_modulo varchar2          codice modulo di connessione
              p_note_modulo varchar2     note relative a modulo di connessione
              p_istanza varchar2         codice istanza di connessione
              p_note_istanza varchar2    note relative a istanza di connessione
              p_note_accesso varchar2    note relative a diritto di accesso di connessione
              p_ente varchar2            codice ente associato all'istanza di connessione
              p_note_ente varchar2       note relative ad ente di connessione
              p_progetto varchar2        codice progetto di connessione
              p_note_progetto varchar2   note relative a progetto di connessione
              p_ambiente varchar2        codice ambiente di connessione (SI, A00, AD4)
 ECCEZIONI:   --
 ANNOTAZIONI: --
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 000  29/06/2001 MM     Creazione.
******************************************************************************/
BEGIN
   UTENTE       := p_utente;
   NOTE_UTENTE  := p_note_utente;
   MODULO       := p_modulo;
   NOTE_MODULO  := p_note_modulo;
   ISTANZA      := p_istanza;
   NOTE_ISTANZA := p_note_istanza;
   NOTE_ACCESSO := p_note_accesso;
   ENTE         := p_ente;
   NOTE_ENTE    := p_note_ente;
   PROGETTO     := p_progetto;
   NOTE_PROGETTO:= p_note_progetto;
   AMBIENTE     := p_ambiente;
END SET_ACCESSO_UTENTE;
function GET_ERROR
( p_errore       varchar2
, p_stack        number default 0
, p_exclude_code number default 0
) return varchar2 is
/******************************************************************************
 NOME:        GET_ERROR
 DESCRIZIONE: Ricerca descrizione errore su archivio Messaggi di Errore.
 PARAMETRI:   p_errore VARCHAR2 Codifica errore
              p_stack  NUMBER   0 (default) toglie lo stack Trace ; 1 lo lascia.
 RITORNA:     VARCHAR2 Descrizione errore
 ANNOTAZIONI: Se codifica non corretta ritorna la stringa in ingresso, con la
              sola valorizzazione delle eventuali variabili in essa contenute.
              Formati p_errore validi:
              - perche venga effettuata una ricerca nella tabella di decodifica
                p_errore deve cominciare con:
                    X00000  dove X    : primo carattere alfanumerico
                                 00000: seguito da 5 caratteri numerici
              - perche venga effettuata una ricerca nella tabella di decodifica
                tramite "mappatura" dell'errore Oracle p_errore deve cominciare
                con:
                    ORA-00000 dove 00000: 5 caratteri numerici
              PERSONALIZZAZIONE MESSAGGIO
              ---------------------------
              Il messaggio decodificato puo essere personalizzato tramite
              l'utilizzo di variabili e/o aggiungendo una precisazione.
              Una variabile e' una qualsiasi stringa alfanumerica preceduta dal
              carattere $.
              Se p_errore inizia con un codice del tipo X00000 ed e' seguito da
              uno spazio:
               - se il messaggio non contiene la variabile $$,
                    la stringa che segue viene accodata alla descrizione estratta
               - altrimenti
                    la stringa che segue viene sostituita alla variabile $$.
              Variabili:
              - $$:          permette di includere nel messaggio una precisazione
                             nella posizione desiderata.
                             La variabile $$ viene ignorata se p_errore NON inizia
                             con un codice del tipo X00000.
              - $table:      se l'errore scaturisce da violazione di un constraint,
                             tabella proprietaria del constraint.
                             La variabile $table viene ignorata se p_errore NON
                             inizia con un codice del tipo ORA-00000.
              - $ref_table:  se l'errore scaturisce da violazione di un constraint,
                             tabella proprietaria del constraint referenziato.
                             La variabile $ref_table viene ignorata se p_errore NON
                             inizia con un codice del tipo ORA-00000.
              - $<variabile> variabile definita dall'utente; p_errore dovra'
                             contenere la sua valorizzazione ($<variabile>=<valore>)
              Se le variabili sono composte con "." (punto) a seguito del "$", qualora
              non sostituite, il nome della variabile rimane nella stringa:
                 ... $table ...   => ... ...
                 ... $.table ...  => ... table ...
              Se la variabile e seguita da ":", quanto segue viene usato da prefisso:
                 ... $table:in ...  =>  ... in Utenti ...
              La variabile termina al primo spazio o alla fine della stringa.
              Per terminare la variabile senza includere uno spazio aggiuntivo
              deve essere indicato un ulteriore simbolo "$":
                 ... $table:in$.    => ... in Utenti.
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 000  01/03/2001 MF     Prima emissione.
 001  05/03/2002 MM     Inclusione in PACKAGE SI4 e sostituzione della SELECT
                        con SQL_EXECUTE in modo che, se la tabella non esiste,
                        restituisca il parametro in ingresso e non dia errore.
 003  28/10/2003 MM     - Utilizzo del package KEYPACKAGE per l'accesso alle
                        tabelle KEY_... che deve esistere anche se non
                        esistono le tabelle KEY_...
                        - Introduzione della valorizzazione di eventuali
                        parametri presenti nel messaggio.
 004  27/09/2005 MF     - Se richiesto Stack Trace riemette l'errore originale in coda.
                        - Cerca nome del Constraint tra parentesi tonde se
                          non trova il "." (punto) all'interno delle parentesi.
                        - Eliminazione errori DB non significativi (ORA-00604).
                        - Migliorie alla gestione parametri presenti nel messaggio,
                          con uso colonna "precisazione" della tavola KEY_CONSTRAINT_ERROR.
                        - Sostituisce variabili nominali anche in presenza di variabile $$
                          (tolto boolean d_valorizza).
                        - Memorizza le note a partire dal carattere 8 (non 7).
                        - Mantiene note aggiuntive successive a errore Applicativo anche
                          se proveniente da errore di DB.
 005  12/01/2006 MF     Aggiunto parametro esclusione Codice Applicativo da messaggio.
                        Gestito messaggio con Codice Applicativo che termina con ritorno a capo.
                        Migliorata manipolazione dello stack.
 006  05/05/2006 MF     Risolve errata interpretazione del . che segue un $ terminale di variabile.
                        - Cerca variabili solo fino al prossimo '$' e non '$ '.
                        - Se trova la variabile terminata da $ la toglie.
 008  07/06/2010 MF     Lunghezza variabili interne di manipolazione Errore da 2000 a 32000.
******************************************************************************/
   d_codice         VARCHAR2(9);
   d_ErrorCode      KeyPackage.t_error_text;
   d_precisazione   Key_Constraint_Error.precisazione%type;
   d_note           varchar2(32000);
   d_messaggio      varchar2(32000);
   d_ConstraintName KeyPackage.t_error_text;
   d_Punto          NUMBER;
   d_Tonda          NUMBER;
BEGIN
   d_messaggio := p_errore;
   -- Verifica presenza di errori da eliminare: ORA-00604
   if upper(substr(p_errore,1,9)) = 'ORA-00604' then
      -- cerca Errore successivo sulla stringa
      d_Punto := instr( p_errore, CHR(10)||'ORA-');
      if d_Punto = 0 then
         d_Punto := length(p_errore);
      end if;
      d_messaggio := substr(p_errore, d_Punto + 1);
   end if;
   -- Estrae testo del messaggio togliendo eventuali errori ORA- successivi
   d_Punto := instr( d_messaggio, CHR(10)||'ORA-', 2 );
   if d_Punto > 0 then
      d_messaggio := substr(d_messaggio, 1, d_Punto - 1 );
   end if;
   -- Trattamento errore proveniente da DB ORACLE
   if upper(substr(d_messaggio,1,4)) = 'ORA-' then
      d_codice := upper(substr(d_messaggio,1,9));
      -----------------------------------------------------------
      -- Estrazione della KEY di precisazione per Errore ORACLE
      -----------------------------------------------------------
      -- Estrae la posizione del "." dopo la prima "("
      d_Punto := instr( d_messaggio, '.', instr(d_messaggio,'(') );
      -- Rev.004: Dopo aver cercato il nome del Constraint dopo al "." (punto),
      --          se non lo trova, prende tutto il contenuto tra parentesi tonde.
      if d_Punto = 0 then
         d_Punto := instr( d_messaggio, '(' );
      end if;
      -- Estrae la posizione della ")" successiva
      d_Tonda := instr( d_messaggio, ')', d_Punto );
      -- Se non trova ")" setta con lunghezza 30
      if d_Tonda = 0 then
         d_Tonda := d_Punto + 1 + 30;
      end if;
      -- Estrae il Nome del Constraint Violated
      if d_Punto > 0 then
         d_ConstraintName := substr(d_messaggio, d_Punto + 1 , d_Tonda - 1 - d_Punto );
      end if;
      -----------------------------------------------------------
      -- Ricerca codice errore in archivio Messaggi di Errore
      -----------------------------------------------------------
      d_ErrorCode    := KeyPackage.get_errorCode(to_number(substr(d_codice,5)), d_ConstraintName);
      d_precisazione := KeyPackage.get_error_precisazione(to_number(substr(d_codice,5)), d_ConstraintName);
      if d_ErrorCode is null then -- NON trovata decodifica Errore da Constraint
         -- Verifica caso di Errore per "Raise_Application_Error" con messaggio di errore
         -- che inizia con un codice di errore applicativo (stringa del tipo X0000).
         if  substr(d_messaggio,1,6) = 'ORA-20'
         and translate(substr(d_messaggio,13,5),'123456789','000000000') = '00000' then
             d_messaggio := substr( d_messaggio,12 );
         end if;
      else  -- Analizza presenza codifica Applicazione nel testo Errore del DB
         if  translate(substr(d_ErrorCode,2,5),'123456789','000000000') = '00000'
         and ( length(d_ErrorCode)     < 7
            or substr(d_ErrorCode,7,1) = ' '
            or substr(d_ErrorCode,7,1) = chr(10)
             ) then
            d_messaggio := d_ErrorCode;
         else
            d_messaggio := d_codice||': '||d_ErrorCode;
         end if;
      end if;
   end if; -- Trattamento errore proveniente da DB ORACLE
   -- Analizza presenza Codifica Applicazione nel testo del messaggio
   if translate(substr(d_messaggio,2,5),'123456789','000000000') = '00000'
   and ( length(d_messaggio)     < 7
      or substr(d_messaggio,7,1) = ' '
      or substr(d_messaggio,7,1) = chr(10)
       ) then
      d_note      := substr(d_messaggio,8);
      d_messaggio := substr(d_messaggio,1,6);
   end if;
   -----------------------------------------------------------
   -- Ricerca descrizione errore in archivio Messaggi
   -----------------------------------------------------------
   d_messaggio := KeyPackage.GET_ERROR( rtrim(ltrim(d_messaggio)), p_exclude_code );
   if d_messaggio is not null then
      if d_note is null then
         d_note := d_precisazione; -- se non sono state indicate note specifiche usa la precisazione
      end if;
      if instr(d_messaggio,'$$') > 0 then
         d_messaggio := replace(d_messaggio,'$$',rtrim(ltrim(d_note)));
         d_messaggio := replace(d_messaggio,'  ', ' ');
      else
         if d_note is not null then
            d_messaggio := d_messaggio||' '||d_note;
         end if;
      end if;
   end if;
   -----------------------------------------------------------
   -- Valorizzazione eventuali variabili del Messaggio
   -----------------------------------------------------------
   DECLARE
      d_variabile        varchar2(2000);
      d_variabile_prefix varchar2(2000);
      d_variabile_point  varchar2(2000);
      d_valore           varchar2(2000);
      d_prefix           varchar2(2000);
      d_prima            varchar2(32000);
      d_dopo             varchar2(32000);
      d_togli            varchar2(2000);
      i                  integer;
      d_table            varchar2(100);
      d_ref_table        varchar2(100);
   begin
      -- Se l'errore scaturisce dalla violazione di un constraint ed il messaggio
      -- contiene una delle variabili $table o $ref_table, calcola il loro valore.
      if  d_ConstraintName is not null
      and (   instr(d_messaggio,'$table') > 0     or instr(d_messaggio,'$.table') > 0
           or instr(d_messaggio,'$ref_table') > 0 or instr(d_messaggio,'$.ref_table') > 0
          ) then
         BEGIN
         -- valorizza d_table con il nome della tabella proprietaria del constraint
         -- e d_ref_table con il nome del constraint referenziato.
            select table_name
                 , r_constraint_name
              into d_table
                 , d_ref_table
              from user_constraints
             where constraint_name = upper(d_ConstraintName)
            ;
         EXCEPTION
            WHEN OTHERS THEN
               d_table     := to_char(null);
               d_ref_table := to_char(null);
         END;
         -- valorizza d_ref_table con il nome della tabella proprietaria del
         -- constraint referenziato (se precedentemente trovato).
         if d_ref_table is not null then
            BEGIN
               select table_name
                 into d_ref_table
                 from user_constraints
                where constraint_name = upper(d_ref_table)
               ;
            EXCEPTION
               WHEN OTHERS THEN
                  d_ref_table := to_char(null);
            END;
         end if;
      end if;
      -- inserisce un "$" prima di ogni spazio per individuare tutte le variabili sequite da spazio o da "$"
      d_dopo := replace(d_messaggio, ' ', '$ ');
      i := instr(d_dopo,'$');
      while i > 0 loop
         d_prima := afc.GET_SUBSTR( d_dopo, '$' );
         if substr(d_dopo, 1, 1) != ' ' then -- carattere successivo al "$" e diverso da spazio
            -- ispeziona la stringa per trovare il nome della variabile da sostituire
            -- Rev.006
            -- d_variabile_prefix := '$'||afc.GET_SUBSTR( d_dopo, '$ ' );
            d_variabile_prefix := '$'||afc.GET_SUBSTR( d_dopo, '$' );
            d_variabile_point  := replace( d_variabile_prefix, '.', '' ); -- elimina eventuale "." da variabile
            d_prefix           := d_variabile_prefix;
            d_variabile        := afc.GET_SUBSTR( d_prefix, ':' ); -- divide variabile da prefisso
            if d_prefix is not null then
               d_prefix        := d_prefix||' '; -- aggiunge spazio di separazione dopo prefisso
            end if;
            d_variabile        := replace( d_variabile, '.', '' ); -- elimina eventuale "." da variabile
            -- ispeziona la stringa per trovare il valore della variabile da sostituire
            d_valore := afc.GET_STRINGPARM(d_messaggio,d_variabile);
            if d_valore is null then -- se non trovato ispeziona la precisazione di errore
               d_valore := afc.GET_STRINGPARM(d_precisazione,d_variabile);
            end if;
            if  d_valore is null
            and d_variabile = '$table' then
                d_valore := d_table;
            end if;
            if  d_valore is null
            and d_variabile = '$ref_table' then
                d_valore := d_ref_table;
            end if;
            -- Rev.006
            -- se trova la variabile terminata da $ la toglie
            d_messaggio := replace( d_messaggio, d_variabile_prefix||'$', d_variabile_prefix );
            -- Se ha trova il valore, lo sostituisce alla variabile e toglie la stringa
            -- $<variabile>=<valore>, altrimenti cerca la successiva variabile.
            if d_valore is not null then
               -- elimina dalla stringa $<variabile>=<valore>
               if instr(d_valore,' ') > 0 then
                  d_togli := d_variabile||'='||'"'||d_valore||'"';
               else
                  d_togli := d_variabile||'='||d_valore;
               end if;
               d_messaggio := replace(d_messaggio,d_togli||'$',''); -- con $ finale
               d_messaggio := replace(d_messaggio,d_togli,'');      -- senza $ finale
               -- sostituisce il valore alla variabile
               d_messaggio := replace(d_messaggio,d_variabile_prefix,d_prefix||d_valore);
            else
               -- elimina le variabili non sostituite che non hanno il "."
               d_messaggio := replace(d_messaggio, ' '||d_variabile_point, '');
               d_messaggio := replace(d_messaggio, d_variabile_point, '');
            end if;
         end if; -- carattere successivo al "$" e diverso da spazio
         i := instr(d_dopo,'$');
      end loop;
      d_messaggio := replace(d_messaggio, '$.', '');   -- elimina tutte le stringhe "$." residuali non sostituite
      d_messaggio := replace(d_messaggio, '  ', ' ');  -- sostituisce 2 spazi con 1 solo
   END;
   -----------------------------------------------------------
   -- Gestione Stack Trace
   -----------------------------------------------------------
   if p_stack = 1 -- se richiesto lo Stack Trace riemette l'errore originale in coda
   and d_messaggio != p_errore then
       d_messaggio :=  d_messaggio||chr(10)||chr(10)||p_errore;
   end if;
   RETURN d_messaggio;
END GET_ERROR;
function GET_NTEXT
( p_tabella IN VARCHAR2
, p_colonna IN VARCHAR2
, p_pk      IN VARCHAR2
, p_lingua  IN varchar2
) RETURN varchar2 is
/******************************************************************************
 NOME:        GET_NTEXT
 DESCRIZIONE: Ricerca il testo corrispondente al valore della colonna p_colonna
              della tabella p_tabella con chiave primaria p_pk per la lingua
              p_lingua.
 PARAMETRI:   p_tabella IN VARCHAR2 Nome della tabella su cui cercare il testo
              p_colonna IN VARCHAR2 Nome della colonna su cui cercare il testo
              p_pk      IN VARCHAR2 Chiave per cui cercare il testo
              p_lingua  IN VARCHAR2 Lingua per cui cercare il testo
 RITORNA:     VARCHAR2 testo nella lingua passata.
 ANNOTAZIONI: --
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 003  28/10/2003 MM     Creazione.
******************************************************************************/
BEGIN
   RETURN KeyPackage.GET_NTEXT(p_tabella, p_colonna, p_pk, p_lingua);
END GET_NTEXT;
function GET_NTEXT
( p_tabella IN VARCHAR2
, p_colonna IN VARCHAR2
, p_pk      IN varchar2
) return varchar2 is
/******************************************************************************
 NOME:        GET_NTEXT
 DESCRIZIONE: Ricerca il testo corrispondente al valore della colonna p_colonna
              della tabella p_tabella con chiave primaria p_pk per la lingua
              corrente.
 PARAMETRI:   p_tabella IN VARCHAR2 Nome della tabella su cui cercare il testo
              p_colonna IN VARCHAR2 Nome della colonna su cui cercare il testo
              p_pk      IN VARCHAR2 Chiave per cui cercare il testo
 RITORNA:     VARCHAR2 testo nella lingua corrente.
 ANNOTAZIONI: --
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 003  28/10/2003 MM     Creazione.
******************************************************************************/
BEGIN
   RETURN Keypackage.GET_NTEXT(p_tabella, p_colonna, p_pk);
END GET_NTEXT;
function GET_NTEXT
( p_pk     IN VARCHAR2
, p_lingua IN varchar2
) return varchar2 is
/******************************************************************************
 NOME:        GET_NTEXT
 DESCRIZIONE: Ricerca il testo corrispondente al valore della colonna
              DESCRIZIONE della tabella KEY_ERROR con chiave primaria p_pk per
              la lingua  passata.
 PARAMETRI:   p_pk      IN VARCHAR2 Chiave per cui cercare il testo
              p_lingua  IN VARCHAR2 Lingua per cui cercare il testo
 RITORNA:     VARCHAR2 testo nella lingua passata.
 ANNOTAZIONI: --
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 003  28/10/2003 MM     Creazione.
******************************************************************************/
BEGIN
   RETURN Keypackage.GET_NTEXT(p_pk, p_lingua);
END GET_NTEXT;
function GET_NTEXT
( p_pk     IN varchar2
) return varchar2 is
/******************************************************************************
 NOME:        GET_NTEXT
 DESCRIZIONE: Ricerca il testo corrispondente al valore della colonna
              DESCRIZIONE della tabella KEY_ERROR con chiave primaria p_pk per
              la lingua corrente.
 PARAMETRI:   p_pk      IN VARCHAR2 Chiave per cui cercare il testo
 RITORNA:     VARCHAR2 testo nella lingua corrente.
 ANNOTAZIONI: --
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 003  28/10/2003 MM     Creazione.
******************************************************************************/
BEGIN
   RETURN Keypackage.GET_NTEXT(p_pk);
END GET_NTEXT;
procedure SET_NTEXT
( p_tabella IN VARCHAR2
, p_colonna IN VARCHAR2
, p_pk      IN VARCHAR2
, p_lingua  IN VARCHAR2
, p_testo   IN varchar2
) is
/******************************************************************************
 NOME:        SET_NTEXT
 DESCRIZIONE: Modifica con p_testo il testo corrispondente al valore della
              colonna p_colonna della tabella p_tabella con chiave primaria p_pk
              per la lingua p_lingua.
 ARGOMENTI:   p_tabella IN VARCHAR2 Nome della tabella in cui modificare il testo
              p_colonna IN VARCHAR2 Nome della colonna in cui modificare il testo
              p_pk      IN VARCHAR2 Chiave per cui modificare il testo
              p_lingua  IN VARCHAR2 Lingua in cui modificare il testo
              p_testo   IN VARCHAR2 Testo con cui modificare
 ECCEZIONI:   -
 ANNOTAZIONI:
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 003  28/10/2003 MM     Creazione.
******************************************************************************/
BEGIN
   KeyPackage.SET_NTEXT(p_tabella, p_colonna, p_pk, p_lingua, p_testo);
END SET_NTEXT;
procedure SET_NTEXT
( p_tabella IN VARCHAR2
, p_colonna IN VARCHAR2
, p_pk      IN VARCHAR2
, p_testo   IN varchar2
) is
/******************************************************************************
 NOME:        SET_NTEXT
 DESCRIZIONE: Modifica con p_testo il testo corrispondente al valore della
              colonna p_colonna della tabella p_tabella con chiave primaria p_pk
              per la lingua corrente.
 ARGOMENTI:   p_tabella IN VARCHAR2 Nome della tabella in cui modificare il testo
              p_colonna IN VARCHAR2 Nome della colonna in cui modificare il testo
              p_pk      IN VARCHAR2 Chiave per cui modificare il testo
              p_testo   IN VARCHAR2 Testo con cui modificare
 ECCEZIONI:   -
 ANNOTAZIONI:
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 003  28/10/2003 MM     Creazione.
******************************************************************************/
BEGIN
   KeyPackage.SET_NTEXT(p_tabella, p_colonna, p_pk, p_testo);
END SET_NTEXT;
procedure SET_NTEXT
( p_pk      IN VARCHAR2
, p_lingua  IN VARCHAR2
, p_testo   IN varchar2
) is
/******************************************************************************
 NOME:        SET_NTEXT
 DESCRIZIONE: Modifica con p_testo il testo corrispondente al valore della
              colonna p_colonna della tabella p_tabella con chiave primaria p_pk
              per la lingua p_lingua.
 ARGOMENTI:   p_pk      IN VARCHAR2 Chiave per cui modificare il testo
              p_lingua  IN VARCHAR2 Lingua in cui modificare il testo
              p_testo   IN VARCHAR2 Testo con cui modificare
 ECCEZIONI:   -
 ANNOTAZIONI:
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 003  28/10/2003 MM     Creazione.
******************************************************************************/
BEGIN
   KeyPackage.SET_NTEXT(p_pk, p_lingua, p_testo);
END SET_NTEXT;
procedure SET_NTEXT
( p_pk      IN VARCHAR2
, p_testo   IN varchar2
) is
/******************************************************************************
 NOME:        SET_NTEXT
 DESCRIZIONE: Modifica con p_testo il testo corrispondente al valore della
              colonna p_colonna della tabella p_tabella con chiave primaria p_pk
              per la lingua corrente.
 ARGOMENTI:   p_pk      IN VARCHAR2 Chiave per cui modificare il testo
              p_testo   IN VARCHAR2 Testo con cui modificare
 ECCEZIONI:   -
 ANNOTAZIONI:
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 003  28/10/2003 MM     Creazione.
******************************************************************************/
BEGIN
   KeyPackage.SET_NTEXT(p_pk, p_testo);
END SET_NTEXT;
function GET_STRINGPARM
( p_stringa        IN varchar2
, p_identificativo IN varchar2
, p_where          IN varchar2 default null
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
              p_where          varchar2 Eventuale diversa chiave di accesso a Table.
 RITORNA:     varchar2: Valore del parametro estrapolato dalla stringa.
 ANNOTAZIONI: Se la stringa contiene la parola chiave:
              - "ente" (enti),
              - "utente" (utenti)
              - "modulo" (moduli)
              - "istanza" (istanze)
              - "accesso" (diritti_accesso)
              - "progetto" (progetti)
              , la stringa da ispezionare e da prelevare dal campo NOTE della Table ENTI,
              UTENTI, MODULI, ISTANZE, DIRITTI_ACCESSO e PROGETTI.
 REVISIONI:
 Rev. Data       Autore Descrizione
 ---- ---------- ------ ------------------------------------------------------
 004  27/09/2005 MF     Inclusione funzione get_stringParm di Package AFC.
******************************************************************************/
   d_stringa   varchar2(2000);
begin
   d_stringa := lTrim(rTrim(p_stringa));
   begin
      -- Estrae Annotazioni di ENTE, ISTANZA e DIRITTI_ACCESSO da ambiente SI4
      if lower(d_stringa) in ('ente','enti') then
         select note
           into d_stringa
           from ad4_enti
          where ente = nvl(p_where,SI4.ente)
         ;
      elsif lower(d_stringa) in ('utente','utenti') then
         select note
           into d_stringa
           from ad4_utenti
          where utente = nvl(p_where,SI4.utente)
         ;
      elsif lower(d_stringa) in ('modulo','moduli') then
         select note
           into d_stringa
           from ad4_moduli
          where modulo = nvl(p_where,SI4.modulo)
         ;
      elsif lower(d_stringa) in ('istanza','istanze') then
         select note
           into d_stringa
           from ad4_istanze
          where istanza = nvl(p_where, SI4.istanza)
         ;
      elsif lower(d_stringa) in ('accesso','diritti_accesso') then
         declare
            d_istanza varchar2(10);
            d_modulo  varchar2(10);
            d_utente  varchar2(8);
         begin
            d_utente  := p_where;
            d_istanza := AFC.get_substr(d_utente,'@');
            d_modulo  := AFC.get_substr(d_utente,'@');
            select note
              into d_stringa
              from ad4_diritti_accesso
             where istanza  = nvl(d_istanza, SI4.istanza)
               and modulo   = nvl(d_modulo, SI4.modulo)
               and utente   = nvl(d_utente, SI4.utente)
            ;
         end;
      elsif lower(d_stringa) in ('progetto','progetti') then
         select note
           into d_stringa
           from ad4_progetti
          where progetto = nvl(p_where,SI4.progetto)
         ;
      end if;
   exception WHEN OTHERS then
      d_stringa := '';
   end;
   return AFC.get_stringParm(d_stringa, p_identificativo );
end GET_STRINGPARM;
----------------------------------------------------------------------------------
procedure init_cronologia
( p_utente   IN OUT varchar2
, p_data_agg IN OUT date
) is
/******************************************************************************
 NOME:        init_cronologia.
 DESCRIZIONE: Valorizza i campi UTENTE e DATA_AGG con i relativi valori.
 PARAMETRI:   p_utente   IN OUT varchar2(8) campo UTENTE.
              p_data_agg IN OUT varchar2(8) campo DATA_AGG.
 ECCEZIONI:   --
 REVISIONI:
 Rev. Data         Autore      Descrizione
 ---- -----------  ----------  ------------------------------------------------
 007   27/02/2007  FT          Prima emissione.
******************************************************************************/
begin
   p_utente := nvl( p_utente, UTENTE );
   p_data_agg := sysdate;
end init_cronologia;
----------------------------------------------------------------------------------
FUNCTION get_utente
   RETURN VARCHAR2
IS
BEGIN
   RETURN utente;
END;
FUNCTION get_note_utente
   RETURN VARCHAR2
IS
BEGIN
   RETURN note_utente;
END;
FUNCTION get_modulo
   RETURN VARCHAR2
IS
BEGIN
   RETURN modulo;
END;
FUNCTION get_note_modulo
   RETURN VARCHAR2
IS
BEGIN
   RETURN note_modulo;
END;
FUNCTION get_istanza
   RETURN VARCHAR2
IS
BEGIN
   RETURN istanza;
END;
FUNCTION get_note_istanza
   RETURN VARCHAR2
IS
BEGIN
   RETURN note_istanza;
END;
FUNCTION get_note_accesso
   RETURN VARCHAR2
IS
BEGIN
   RETURN note_accesso;
END;
FUNCTION get_ente
   RETURN VARCHAR2
IS
BEGIN
   RETURN ente;
END;
FUNCTION get_note_ente
   RETURN VARCHAR2
IS
BEGIN
   RETURN note_ente;
END;
FUNCTION get_progetto
   RETURN VARCHAR2
IS
BEGIN
   RETURN progetto;
END;
FUNCTION get_note_progetto
   RETURN VARCHAR2
IS
BEGIN
   RETURN note_progetto;
END;
FUNCTION get_ambiente
   RETURN VARCHAR2
IS
BEGIN
   RETURN ambiente;
END;
END SI4;
/

