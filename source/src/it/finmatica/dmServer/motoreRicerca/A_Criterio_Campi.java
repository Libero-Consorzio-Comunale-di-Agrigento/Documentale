/***********************************************************************
 * Module:  Criterio_Singolo.java
 * @author  Giuseppe Mannella, Andrea Alì
 * Purpose: Classe astratta per la gestione dei criteri di ricerca
 *          in full sul testo passato come parametro
 *          Dipendenze con:  GD4_Criterio_Singolo
 ***********************************************************************/

package it.finmatica.dmServer.motoreRicerca;

// -----------------------------------
// Definizione della classe principale
// -----------------------------------
public abstract class A_Criterio_Campi extends A_Criterio
{
  /* DEFINIZIONE DEI METODI PUBBLICI */

  // -----------------------------------
  // Costruisce il criterio di ricerca
  // -----------------------------------   
  public abstract String montaCriterio() throws Exception;

}