package it.finmatica.dmServer;

/*
 * CLASSE ASTRATTA PER LA GESTIONE DEI CAMPI DOCUMENTO
 * Purpose: Interfaccia per la gestione dei Campi Documento
 *          Dipendenze con: A_Oggetti_File
 *                          GD4_Oggetti_File
 *                          HummingBird_Oggetti_File 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   16/09/2005
 * 
 * */

public interface I_Oggetti_File
{
  // ***************** DEFINIZIONE METODI IMPLEMENTATI ***************** //
 
  void retrieve() throws Exception;
  boolean insert(Object idDocumento, A_Libreria aLibreria) throws Exception;
  boolean update(Object idDocumento, A_Libreria aLibreria) throws Exception;
  boolean delete(Object sDirectory,String idDocumento) throws Exception;
}