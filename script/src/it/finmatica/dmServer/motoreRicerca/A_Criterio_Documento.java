/***********************************************************************
 * Module:  A_Criterio_Documento.java
 * @author  Giuseppe Mannella, Andrea Alì
 * Purpose: Classe astratta per la gestione dei criteri di ricerca
 *          diretti sul documento
 *          Dipendenze con:  GD4_Criterio_Documento
 ***********************************************************************/

package it.finmatica.dmServer.motoreRicerca;

// -----------------------------------
// Definizione della classe principale
// -----------------------------------
public abstract class A_Criterio_Documento  extends A_Criterio
{

  /* DEFINIZIONE DEI METODI PUBBLICI */

  // -----------------------------------
  // Costruisce il criterio di ricerca
  // -----------------------------------   
  public abstract String montaCriterio() throws Exception;

}