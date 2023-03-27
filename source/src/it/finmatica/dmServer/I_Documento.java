package it.finmatica.dmServer;

/*
 * CLASSE ASTRATTA PER LA GESTIONE DEI CAMPI DOCUMENTO
 * Purpose: Interfaccia per la gestione dei Campi Documento
 *          Dipendenze con: A_Documento
 *                          GD4_Documento
 *                          HummingBird_Documento 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   16/09/2005
 * 
 * */

public interface I_Documento
{
 // ***************** DEFINIZIONE METODI IMPLEMENTATI ***************** //
  boolean retrieve(boolean flagTipoDocumento,
                   boolean flagValori,
                   boolean flagOggettiFile,
                   boolean flagLog,
                   String  idLog) throws Exception;
  boolean insertDocument(String stato) throws Exception;
  boolean updateDocument() throws Exception;
  boolean deleteDocument() throws Exception;
  boolean visualizza();

}