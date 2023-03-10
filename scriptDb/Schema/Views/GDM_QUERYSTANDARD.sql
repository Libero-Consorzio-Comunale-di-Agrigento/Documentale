CREATE OR REPLACE FORCE VIEW GDM_QUERYSTANDARD
(DOCUMENTO, CODICE_RICHIESTA, AREA, CODICE_MODELLO, STATO, 
 NOME)
BEQUEATH DEFINER
AS 
SELECT D.ID_DOCUMENTO,D.CODICE_RICHIESTA, D.AREA,'QueryStandard',D.STATO_DOCUMENTO, T.NOME FROM DOCUMENTI D, GDM_Q_QUERYSTANDARD T WHERE D.ID_DOCUMENTO = T.ID_DOCUMENTO AND D.ID_TIPODOC = 12;

COMMENT ON TABLE GDM_QUERYSTANDARD IS 'Area: GDMSYS - Modello: QueryStandard';



