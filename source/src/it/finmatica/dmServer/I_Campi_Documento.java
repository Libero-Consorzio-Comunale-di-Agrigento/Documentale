/*
 * CLASSE ASTRATTA PER LA GESTIONE DEI CAMPI DOCUMENTO
 * Purpose: Interfaccia per la gestione dei Campi Documento
 *          Dipendenze con: A_Campi_Documento
 *                          Campi_Documento
 *                          HummingBird_Campi_Documento 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   16/09/2005
 * 
 * */
package it.finmatica.dmServer;

public interface I_Campi_Documento
{
 // ***************** DEFINIZIONE METODI IMPLEMENTATI ***************** //
  void retrieve() throws Exception;
}