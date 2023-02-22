CREATE OR REPLACE FORCE VIEW OGGETTI_FILE_APPLICATIVI
(ID_DOCUMENTO_APPLICATIVO, ID_DOCUMENTO_EST_APPLICATIVO, ID_DOCUMENTO_GDM, ID_OGGETTO_FILE, FILENAME, 
 UTENTE_AGGIORNAMENTO, DATA_AGGIORNAMENTO)
BEQUEATH DEFINER
AS 
SELECT agspr_AGP_FILE_DOC.id_documento,
          agspr_AGP_FILE_DOC.id_documento_esterno,
          oggetti_file.id_documento,
          id_file_esterno id_oggetto_file,
          oggetti_file.filename,
          oggetti_file.UTENTE_AGGIORNAMENTO,
          oggetti_file.DATA_AGGIORNAMENTO
     FROM agspr_AGP_FILE_DOC, oggetti_file
    WHERE agspr_AGP_FILE_DOC.id_file_esterno = oggetti_file.id_oggetto_file;


