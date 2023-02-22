CREATE OR REPLACE package integritypackage
/******************************************************************************
 NOME:        IntegrityPackage
 DESCRIZIONE: Oggetti per la gestione della Integrita Referenziale.
              Contiene le Procedure e function per la gestione del livello di
              annidamento dei trigger.
              Contiene le Procedure per il POSTING degli script alla fase di
              AFTER STATEMENT.
 REVISIONI:
 Rev. Data        Autore  Descrizione
 ---- ----------  ------  ----------------------------------------------------
 01    23/01/2001  MF      Inserimento commento.
 02    04/12/2002  SN      In caso di errore visualizza lo statement
 03    22/12/2003  SN      Rilevamento errore in caso di select
                          Sistemazione frase in base a default stabiliti.
 04    10/05/2004  SN      Se errore 20999 non si visualizza lo statement a
                          meno che non sia stata settata la variabile debug a 1.
 05    20/12/2004  SN      Se errore non compreso fra 20000 e 20999 non visualizza
                          lo statement a meno che debug sia = 1.
 06    04/08/2005  SN      Gestione di integrityerror.
 07    26/08/2005  SN      Sistemazione messaggio di errore
 08    12/10/2005  SN      Errore rimappato attraverso si4.get_error
 09    21/10/2005  SN      Modificato controllo errore
 10    30/08/2006  FT      Modifica dichiarazione subtype per incompatibilit¿ con
                           versione 7 di Oracle
 11    04/12/2008  MM      Creazione procedure log.
 12    20/03/2009  MM      Creazione procedure log con parametro clob.
 NOTA: In futuro verra tolta la substr nella SISTEMA MESSAGGIO quando verra
 rilasciata una versione di ad4 che lo consenta.
******************************************************************************/
as
   d_revision varchar2(30);
   subtype t_revision is d_revision%type;
   s_revisione      t_revision     := 'V1.12';
   function versione
      return t_revision;
   -- Variabili per SET Switched FUNCTIONAL Integrity
   functional       boolean        := true;
   integrityerror   exception;
   procedure setdebugon;
   procedure setdebugoff;
   -- Procedure for Referential Integrity
   procedure setfunctional;
   procedure resetfunctional;
   procedure initnestlevel;
   function getnestlevel
      return number;
   procedure nextnestlevel;
   procedure previousnestlevel;
   /* Variabili e Procedure per IR su Relazioni Ricorsive */
   type t_operazione is table of varchar2 (32000)
      index by binary_integer;
   type t_messaggio is table of varchar2 (2000)
      index by binary_integer;
   d_istruzione     t_operazione;
   d_messaggio      t_messaggio;
   d_entry          binary_integer := 0;
   procedure set_postevent (p_istruzione in varchar2, p_messaggio in varchar2);
   procedure exec_postevent;
/******************************************************************************
 DESCRIZIONE: Esegue gli statement precedentemente impostati.
              Se inizia con:
              SELECT: toglie eventuale ';' in fondo
              :=    : si suppone segua la chiamata ad una funzione, viene
                      dichiarata una variabile e le si assegna il ritorno
              in caso di stringa diversa mette il codice fra begin e end
 ******************************************************************************/
   procedure log(p_log in varchar2);
   procedure log(p_log in clob);
end integritypackage;
/* End Package: IntegrityPackage
   N.B.: In caso di "Generate Trigger" successive alla prima
         IGNORARE Errore di Package gia presente
*/
/

