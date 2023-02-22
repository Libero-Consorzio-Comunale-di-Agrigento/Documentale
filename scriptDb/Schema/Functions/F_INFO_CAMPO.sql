CREATE OR REPLACE FUNCTION F_INFO_CAMPO (P_AREA VARCHAR2, P_MODELLO VARCHAR2, P_NOME_CAMPO VARCHAR2)
   RETURN VARCHAR2
IS
   A_RET VARCHAR2(200);
BEGIN
   select dati.tipo||'#@#'||nvl(formato_data,'')||'#@#'||nvl(in_uso,'N')||'#@#'||campi_documento.id_campo||'#@#'||
          nvl(SENZA_SALVATAGGIO,'N')||'#@#'||nvl(SENZA_AGGIORNAMENTO,'N')||'#@#'||campi_documento.nome||'#@#'||
          nvl(tipo_log,0)||'#@#'||nvl(dati.lunghezza,0)
     INTO A_RET
     from campi_documento,dati_modello, dati,modelli
     where campi_documento.id_campo = dati_modello.id_campo and
     dati_modello.AREA_DATO = dati.area and
     dati_modello.dato = dati.dato and
     campi_documento.nome = P_NOME_CAMPO and
     campi_documento.ID_TIPODOC = modelli.id_tipodoc
     AND modelli.area=P_AREA
     AND modelli.codice_modello = P_MODELLO
     and dati_modello.area=modelli.area;
   RETURN A_RET;
EXCEPTION WHEN NO_DATA_FOUND THEN
   RETURN 'X';
END;
/

