CREATE OR REPLACE FUNCTION f_filtro_fulltext_warea (
   p_doc      VARCHAR2,
   p_valore   VARCHAR2,
   p_ogfi     VARCHAR2 DEFAULT ' ',
   p_ocr      VARCHAR2 DEFAULT ' ',
   p_fulltext VARCHAR2 DEFAULT 'Y'
)
   RETURN VARCHAR2
IS
   s_valore      VARCHAR2 (4000);
   s_formato     VARCHAR2 (40);
   s_tipo        VARCHAR2 (1);
   n_tipodoc     NUMBER (10);
   alias_v       VARCHAR2 (30);
   nometabella   VARCHAR2 (30);
   d_testo       VARCHAR2 (4000);
   n_esiste      NUMBER(1);
   n_ritorno     NUMBER(1);
   d_testo_contains  VARCHAR2 (4000);
   d_or         VARCHAR2 (4) := ' ';
   d_testo_contains_ogfi  VARCHAR2 (4000);
   d_testo_contains_ocr   VARCHAR2 (4000);
BEGIN
    if p_doc is null then
        return '0';
    end if;

    n_ritorno:=0;
    SELECT UPPER (NVL(A.ACRONIMO,'-')||'_'||NVL (T.ALIAS_MODELLO, '-'))
       INTO NOMETABELLA
       FROM DOCUMENTI D, TIPI_DOCUMENTO T, AREE A
      WHERE D.ID_DOCUMENTO = P_DOC AND D.ID_TIPODOC = T.ID_TIPODOC
        AND A.AREA = T.AREA_MODELLO;
    n_esiste:=f_esiste_tabella (nometabella);
    d_testo_contains_ogfi := ' (select count(*) from oggetti_file where id_documento='||p_doc||' and    decode(nvl(path_file,'' ''),'' '', contains (testoocr, '''||p_valore|| '''),contains ("FILE", '''||p_valore|| '''))>0 )  > 0 ';
    d_testo_contains_ocr  := ' (select count(*) from oggetti_file where id_documento='||p_doc||' and    contains (OCR_FILE, '''||p_valore|| ''')>0 )  > 0 ';
    IF n_esiste=1 then
       d_testo_contains:=' contains (FULL_TEXT,'''  || p_valore || ''')>0 ';
       d_testo :=
            'SELECT 1'
         || ' FROM '
         || nometabella
         || ' WHERE ID_DOCUMENTO = '
         || p_doc
         || ' AND ( ';
    ELSE
       d_testo_contains:=' contains (valori.valore_clob,'''  || p_valore || ''')>0 ';
       d_testo :=
            'SELECT /*+ INDEX(valori valo_doca_ak) */   nvl(max(1),0) '
         || ' FROM VALORI '
         || ' WHERE VALORI.ID_DOCUMENTO = '
         || p_doc
         || ' AND ( ';
    END IF;
    IF p_fulltext <> ' ' THEN
       d_testo := d_testo || d_testo_contains;
       d_or := ' OR ';
    END IF;
    IF p_ogfi<>' ' THEN
       d_testo := d_testo || d_or || d_testo_contains_ogfi;
       d_or := ' OR ';
    END IF;
    IF p_ocr<>' ' THEN
       d_testo := d_testo || d_or || d_testo_contains_ocr;
       d_or := ' OR ';
    END IF;
    IF d_or=' ' THEN
       d_testo := d_testo || ' 1=1 ';
    END IF;
    d_testo := d_testo ||')';
    --dbms_output.put_line(d_testo);
   BEGIN
     EXECUTE IMMEDIATE d_testo INTO n_ritorno;
     EXCEPTION WHEN NO_DATA_FOUND THEN
               RETURN 0;
   END;
   RETURN n_ritorno;
EXCEPTION
   WHEN OTHERS THEN

        raise_application_error('-20999',sqlerrm);
   RETURN 0;
END f_filtro_fulltext_warea;
/

