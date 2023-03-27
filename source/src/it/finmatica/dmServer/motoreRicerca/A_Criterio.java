/***********************************************************************
 * Module:  A_Criterio.java
 * @author  Giuseppe Mannella, Andrea Alì
 * Purpose: Classe astratta per la gestione dei criteri di ricerca
 *          Classe di comportamento comuni a tutti i tipi di ricerca 
 *          (and, or, not, singola)
 *          Dipendenze con:  Criterio_And
 *                           Criterio_Or,
 *                           Criterio_Not,
 *                           Criterio_Singolo
 ***********************************************************************/

package it.finmatica.dmServer.motoreRicerca;

import it.finmatica.dmServer.Environment;

// -----------------------------------
// Definizione della classe principale
// -----------------------------------
public abstract class A_Criterio implements I_Criterio
{
  // -----------------
  // Variabili private
  // -----------------

  private   A_Condizioni condizioni;
  protected Environment vu;
  
  /* DEFINIZIONE DEI METODI PUBBLICI */

  // -------------------------
  // Metodi di getter e setter
  // ------------------------- 

  public A_Condizioni getCondizioni()
  {
         return condizioni;
  }
   
  public void setCondizioni(A_Condizioni newCondizioni)
  {
    
         condizioni = newCondizioni;
  }
    
  public void setEnvironment(Environment newVu)
  {
         vu = newVu;
  }
 
  /* DEFINIZIONE DEI METODI ASTRATTI */

  public abstract String montaCriterio() throws Exception;
  
}