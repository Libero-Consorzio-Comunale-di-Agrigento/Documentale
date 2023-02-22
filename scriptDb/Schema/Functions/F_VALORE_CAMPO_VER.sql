CREATE OR REPLACE FUNCTION F_VALORE_CAMPO_VER (
   p_doc     VARCHAR2,
   p_campo   VARCHAR2
)
   RETURN VARCHAR2
IS
   s_valore    VARCHAR2 (4000);
   n_campo     NUMBER (20);
   n_tipodoc   NUMBER (10);
BEGIN
   SELECT id_tipodoc
     INTO n_tipodoc
     FROM documenti
    WHERE id_documento = p_doc;
   SELECT c.id_campo
     INTO n_campo
     FROM campi_documento c
    WHERE c.id_tipodoc = n_tipodoc AND c.nome = p_campo;
   SELECT NVL (valore_stringa,
               NVL (TO_CHAR (valore_data,
                             NVL (REPLACE (formato_data, 'hh:', 'hh24:'),
                                  'dd/mm/yyyy'
                                 )
                            ),
                    TO_CHAR (valore_numero)
                   )
              )
     INTO s_valore
     FROM valori, campi_documento, dati_modello, dati
    WHERE id_documento = p_doc
      AND valori.id_campo = n_campo
      AND campi_documento.id_campo = valori.id_campo
      AND campi_documento.id_campo = dati_modello.id_campo
      AND dati_modello.area_dato = dati.area
      AND dati_modello.dato = dati.dato;
   RETURN s_valore;
EXCEPTION
   WHEN OTHERS
   THEN
      RETURN NULL;
END F_VALORE_CAMPO_VER;
/

