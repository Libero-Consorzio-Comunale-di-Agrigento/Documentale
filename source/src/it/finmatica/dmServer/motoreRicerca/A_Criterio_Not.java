/***********************************************************************
 * Module:  Criterio_Not.java
 * @author  Giuseppe Mannella, Andrea Alì
 * Purpose: Classe astratta per la gestione dei criteri di ricerca
 *          in "not"
 *          Dipendenze con:  GD4_Criterio_Not
 ***********************************************************************/

package it.finmatica.dmServer.motoreRicerca;

// -----------------------------------
// Definizione della classe principale
// -----------------------------------
public abstract class A_Criterio_Not extends A_Criterio
{

  /* DEFINIZIONE DEI METODI PUBBLICI */

  // -----------------------------------
  // Costruisce il criterio di ricerca
  // -----------------------------------   
  public abstract String montaCriterio() throws Exception;

}