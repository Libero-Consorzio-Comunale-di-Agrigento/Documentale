CREATE OR REPLACE FORCE VIEW GDM_REPORT_FOOTER
(DOCUMENTO, CODICE_RICHIESTA, AREA, CODICE_MODELLO, STATO, 
 TITOLO)
BEQUEATH DEFINER
AS 
SELECT D.ID_DOCUMENTO,D.CODICE_RICHIESTA, D.AREA,'REPORT_FOOTER',D.STATO_DOCUMENTO, T.TITOLO FROM DOCUMENTI D, GDM_T_REPORT_FOOTER T WHERE D.ID_DOCUMENTO = T.ID_DOCUMENTO AND D.ID_TIPODOC = 10;

COMMENT ON TABLE GDM_REPORT_FOOTER IS 'Area: GDMSYS - Modello: REPORT_FOOTER';



