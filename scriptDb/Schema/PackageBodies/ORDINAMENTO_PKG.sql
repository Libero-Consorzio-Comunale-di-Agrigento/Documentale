CREATE OR REPLACE PACKAGE BODY ORDINAMENTO_PKG AS
/******************************************************************************
   NAME:       ORDINAMENTO_PKG
   PURPOSE:
   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        21/11/2007  VA               1. Created this package body.
******************************************************************************/
  FUNCTION binding (p_criterio IN VARCHAR2, p_oggetto IN VARCHAR2) return varchar2 is
  d_stringa_ritorno varchar2(32000);
  d_campo           varchar2(30);
  d_valore_campo    varchar2(1000);
  components     OWA_TEXT.vc_arr;
  begin
    /* estraggo il campo su cui ordinare */
    IF OWA_PATTERN.match(p_criterio,
                             '(.*):([0-9a-z_$A-Z.]*)',
                             components) THEN
          d_campo := components(2);
    end if;
    d_valore_campo := 'f_valore_campo('''||p_oggetto||''','''||d_campo||''')';

    IF d_campo<>'ID_DOCUMENTO' THEN
       d_stringa_ritorno := replace(p_criterio,':'||d_campo,d_valore_campo);
    ELSE
       d_stringa_ritorno := replace(p_criterio,':ID_DOCUMENTO',p_oggetto);
    END IF;
    return d_stringa_ritorno;
  end;
  FUNCTION genera_chiave( p_oggetto      IN links.id_oggetto%type
                        , p_tipo_oggetto IN links.tipo_oggetto%type
                        , p_cartella     IN links.id_cartella%type default null)  RETURN NUMBER IS
  d_tipodoc              documenti.ID_TIPODOC%type;
  d_ordinamento          links.ORDINAMENTO%type;
  d_valore               links.ORDINAMENTO%type;
  d_criterio             ordinamenti_cartella.CRITERIO%type;
  d_id_profilo           documenti.ID_DOCUMENTO%type;
  d_tipo_oggetto         links.tipo_oggetto%type := p_tipo_oggetto;
  d_oggetto              links.id_oggetto%type;
  d_tipo_uso             modelli.tipo_uso%type;
  BEGIN
  if p_tipo_oggetto = 'D' then
     /* In caso di tipo Oggetto D mi viene passato id_profilo_documento */
    /* verifico il tipo oggetto */
     select tipo_uso
       into d_tipo_uso
       from modelli m
          , documenti d
      where d.id_documento = p_oggetto
        and m.id_tipodoc = d.id_tipodoc
        and m.codice_modello_padre is null;
     if d_tipo_uso in ('Q','V')  then
        d_tipo_oggetto := 'Q';
        d_id_profilo := p_oggetto;
        select id_query
          into d_oggetto
          from query q
         where q.id_documento_profilo = p_oggetto;
     elsif d_tipo_uso in ('C','F','W') then
        d_tipo_oggetto := 'C';
        d_id_profilo   := p_oggetto;
        select id_cartella
          into d_oggetto
          from cartelle c
         where c.id_documento_profilo = p_oggetto;
     else
        d_tipo_oggetto := 'D';
        d_oggetto := p_oggetto;
     end if;
  elsif p_tipo_oggetto = 'C' then
     /* In caso di tipo Oggetto C mi viene passato id_cartella */
     d_tipo_oggetto := 'C';
     d_oggetto      := p_oggetto;
      select id_documento_profilo
          into d_id_profilo
          from cartelle c
         where c.id_cartella = p_oggetto;
  elsif p_tipo_oggetto = 'Q' then
     /* In caso di tipo Oggetto Q mi viene passato id_query */
     d_tipo_oggetto := 'Q';
     d_oggetto      := p_oggetto;
      select id_documento_profilo
          into d_id_profilo
          from query c
         where c.id_query = p_oggetto;
  end if;
    /* Estraggo le cartelle in cui ¿ presente l'oggetto*/
    for c_cartella in (select id_cartella
                            , tipo_oggetto
                         from links
                        where id_oggetto   = d_oggetto
                          and tipo_oggetto = d_tipo_oggetto
                          and id_cartella  = nvl(p_cartella,id_cartella)
                          ) loop
         d_ordinamento := null;
        /* Estraggo il tipo doc per la cartella*/
        select id_tipodoc
          into d_tipodoc
          from documenti d
             , cartelle c
         where c.id_documento_profilo = d.id_documento
           and c.id_cartella          = c_cartella.id_cartella;
         /* Verifico se esiste un ordinamento per quel tipo cartella e tipo_oggetto*/
        for c_ordinamento in (select *
                                from ordinamenti_cartella
                               where id_tipodoc = d_tipodoc
                                 and tipo_obj   = c_cartella.tipo_oggetto
                               order by seq
                               ) loop
           /* faccio il binding delle eventuali variabili */
           if c_cartella.tipo_oggetto = 'D' then
              d_criterio := binding(c_ordinamento.criterio,d_oggetto);
           elsif c_cartella.tipo_oggetto = 'C' then
--              select id_documento_profilo
--                into d_id_profilo
--                from cartelle
--               where id_cartella = p_oggetto;
              d_criterio := binding(c_ordinamento.criterio,d_id_profilo);
            elsif c_cartella.tipo_oggetto = 'Q' then
--              select id_documento_profilo
--                into d_id_profilo
--                from query
--               where id_query = p_oggetto;
              d_criterio := binding(c_ordinamento.criterio,d_id_profilo);
           end if;
            /* calcolo il valore */
             execute immediate 'select '||d_criterio||' from dual'
                          into d_valore;
            if c_ordinamento.tipo_criterio = 'S' then
               /*Faccio RPAD  della stringa con tanti spazi*/
               if d_valore is null then
                  d_valore := rpad('z',c_ordinamento.lunghezza_criterio,'z');
               else
                  d_valore := rpad(d_valore,c_ordinamento.lunghezza_criterio,' ');
               end if;
            elsif c_ordinamento.tipo_criterio = 'N' then
              /* Verifico se l'ordinamento e ASC o DESC*/
              if c_ordinamento.tipo_ordinamento = 'DESC' then
                 /* Eseguo il complemento a 9 del numero */
                 execute immediate 'select '||lpad('9',c_ordinamento.lunghezza_criterio,'9')||'-nvl('''||d_valore||''',0) from dual'
                              into d_valore;
              else
                 if d_valore is not null then
                    d_valore := lpad(d_valore,c_ordinamento.lunghezza_criterio,'0');
                 else
                    d_valore := lpad('9',c_ordinamento.lunghezza_criterio,'9');
                 end if;
              end if;
            elsif c_ordinamento.tipo_criterio = 'D' then
              d_valore := to_char(to_date(d_valore,'dd/mm/yyyy hh24:mi:ss'),'yyyymmddhh24miss');
              if c_ordinamento.tipo_ordinamento = 'DESC' then
                d_valore := '99999999999999'-nvl(d_valore,0);
              else
                d_valore := nvl(d_valore,'99999999999999');
              end if;
            end if;
               d_ordinamento := d_ordinamento||d_valore;
        end loop;
        /* Se non esiste un ordinamento */
        if d_ordinamento is null then
           if c_cartella.tipo_oggetto = 'D' then
              /* Ordino per data aggiornamento DESC*/
              select '99999999999999'-to_char(data_aggiornamento,'yyyymmddhh24miss')
                into d_ordinamento
               from documenti
              where id_documento = d_oggetto;
           elsif c_cartella.tipo_oggetto = 'C' then
              /*Ordino per nome ASC*/
              select nome
                into d_ordinamento
                from cartelle
                where id_cartella = d_oggetto;
           elsif c_cartella.tipo_oggetto = 'Q' then
              /*Ordino per nome ASC*/
              select nome
                into d_ordinamento
                from query
                where id_query = d_oggetto;
           end if;
        end if;
        /* Aggiorno il campo nella tabella LINKS */
        update links
           set ordinamento = d_ordinamento
         where id_oggetto = d_oggetto
           and id_cartella = c_cartella.id_cartella;
     end loop;
    RETURN 0;
    exception when others then
   --     raise_application_error(-20999,'Oggetto: '||p_oggetto||' - '||sqlerrm);
    return -1;
  END;
END ORDINAMENTO_PKG;
/

