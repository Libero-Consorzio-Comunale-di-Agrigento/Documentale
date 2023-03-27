package it.finmatica.dmServer;

/*
 * CLASSE ASTRATTA PER LA GESTIONE DEI CAMPI DOCUMENTO
 * Purpose: Interfaccia per la gestione dei Campi Documento
 *          Dipendenze con: A_Valori
 *                          GD4_Valori
 *                          HummingBird_Valori 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   16/09/2005
 * 
 * */
  
public interface I_Valori
{
 // ***************** DEFINIZIONE METODI IMPLEMENTATI ***************** //
  
  abstract void retrieve() throws Exception;
  abstract boolean insert(Object idDocumento) throws Exception;
  abstract boolean update(Object idDocumento) throws Exception;
}