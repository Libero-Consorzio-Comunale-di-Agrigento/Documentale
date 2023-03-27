/***********************************************************************
 * Module:  I_Criterio.java
 * @author  Giuseppe Mannella, Andrea Alì
 * Purpose: Interfaccia per la gestione dei criteri di ricerca
 *          Dipendenze con: A_Criterio                           
 ***********************************************************************/

package it.finmatica.dmServer.motoreRicerca;

// ----------------------------
// Definizione dell'interfaccia
// ----------------------------
public interface I_Criterio
{

  /* DEFINIZIONE DEI METODI IMPLEMENTATI */  
   
  String montaCriterio() throws Exception;

}