/***********************************************************************
 * Module:  GD4_Criterio_Singolo.java
 * @author  Giuseppe Mannella, Andrea Alì
 * Purpose: Classe per la gestione dei criteri 
 *          in full sul testo passato come parametro
 *          nel DM di Finmatica.
 ***********************************************************************/

package it.finmatica.dmServer.motoreRicerca;
import  it.finmatica.dmServer.util.*;

// -----------------------------------
// Definizione della classe principale
// -----------------------------------
public class GD4_Criterio_Singolo extends A_Criterio_Singolo
{

  /* DEFINIZIONE DEI METODI PUBBLICI */

  // -------------------------
  // Costruttore
  // ------------------------- 
  public GD4_Criterio_Singolo()
  {

  }

  //TO BE IMPL
  public String montaCriterio(int indexCriterio) { return ""; }

  // -----------------------------------
  // Costruisce il criterio di ricerca
  // select 1 from dual
  // where exists (condiz_tab)
	//    or exists (condiz_tab) 
  // ----------------------------------- 
  public String montaCriterio() throws Exception
  {    
         String[] aDominio = getCondizioni().splitDominio();
         StringBuffer sSelect = new StringBuffer();
      
         for (int i = 0; i <aDominio.length; i++) {
             sSelect.append(montaCriterio(getCondizioni().getCondizione(Global.COND_SINGLE),aDominio[i]));

            // if (i+1!=aDominio.length) sSelect.append(" or exists "); 
             if (i+1!=aDominio.length) sSelect.append(" or  ");    
         }
          
         return sSelect.toString();
  }

  /* DEFINIZIONE DEI METODI PRIVATI */

  // -----------------------------------
  // Costruisce il criterio di ricerca
  // select 1 from dual
  // where exists (condiz_tab)
  // ----------------------------------- 
  private String montaCriterio(String aCond,String aDominio) throws Exception
  {    
        
          //          StringBuffer sSelect = new StringBuffer("(Select 1 from dual where exists ");
          StringBuffer sSelect = new StringBuffer("(exists ");
        
          try {
            sSelect.append((new CondizioniTabella(this.vu)).whereTabella(aDominio,aCond)+")");
          }
          catch (Exception e)
          {
                 throw new Exception("GD4_Criterio_Singolo::montaCriterio\n"+e.getMessage());
          }
 
          return sSelect.toString();

  }
}