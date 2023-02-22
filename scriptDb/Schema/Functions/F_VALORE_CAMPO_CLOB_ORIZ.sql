CREATE OR REPLACE FUNCTION F_VALORE_CAMPO_CLOB_ORIZ (
   p_doc     VARCHAR2,
   p_campo   VARCHAR2
)
   RETURN CLOB
IS
   s_valore      CLOB;
   s_formato     VARCHAR2 (40);
   s_tipo        VARCHAR2 (1);
   n_tipodoc     NUMBER (10);
   nometabella   VARCHAR2 (30);
   d_testo       VARCHAR2 (4000);
   ex_custom EXCEPTION;
   PRAGMA EXCEPTION_INIT( ex_custom, -20999 );
BEGIN
   SELECT d.id_tipodoc, UPPER (nvl(a.acronimo,'-')||'_'||NVL (t.alias_modello, '-'))
     INTO n_tipodoc, nometabella
     FROM documenti d, tipi_documento t, aree a
    WHERE d.id_documento = p_doc AND d.id_tipodoc = t.id_tipodoc
    and a.area = t.area_modello;
   IF (nometabella = '-_-')
   THEN
      raise_application_error ('-20999',
                               'Nome tabella non presente'
                              );
   ELSE
       IF f_esiste_tabella (nometabella) = -1 THEN
            raise_application_error ('-20999',
                               'Tabella non presente'
                              );
       END IF;
   END IF;
   IF p_campo='FULL_TEXT' THEN
      s_tipo:='F';
   ELSE
       SELECT d.tipo, NVL (REPLACE (d.formato_data, 'hh:', 'hh24:'), 'DD/MM/YYYY')
         INTO s_tipo, s_formato
         FROM dati d, dati_modello dm, modelli m, documenti doc
        WHERE d.area = NVL (dm.area_dato, dm.area)
          AND d.dato = dm.dato
          AND d.dato = p_campo
          AND m.area = dm.area
          AND m.codice_modello = dm.codice_modello
          AND m.id_tipodoc = doc.id_tipodoc
          AND doc.id_documento = p_doc;
   END IF;
   IF s_tipo = 'S' OR s_tipo='F'
   THEN
      d_testo :=
            'SELECT '
         || p_campo
         || ' FROM '
         || nometabella
         || ' WHERE ID_DOCUMENTO = '
         || p_doc;
   END IF;
   IF s_tipo = 'N'
   THEN
      d_testo :=
            'SELECT TO_CHAR('
         || p_campo
         || ') FROM '
         || nometabella
         || ' WHERE ID_DOCUMENTO = '
         || p_doc;
   END IF;
   IF s_tipo = 'D'
   THEN
      d_testo :=
            'SELECT TO_CHAR('
         || p_campo
         || ','''
         || s_formato
         || ''') FROM '
         || nometabella
         || ' WHERE ID_DOCUMENTO = '
         || p_doc;
   END IF;
   EXECUTE IMMEDIATE d_testo
                INTO s_valore;
   RETURN s_valore;
   exception
    when ex_custom then  raise;
    when others then return null;
END F_VALORE_CAMPO_CLOB_ORIZ;
/

