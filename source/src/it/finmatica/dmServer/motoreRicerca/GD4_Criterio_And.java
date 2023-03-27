/***********************************************************************
 * Module:  GD4_Criterio_And.java
 * @author  Giuseppe Mannella, Andrea Alì
 * Purpose: Classe per la gestione dei criteri in "and" 
 *          nel DM di Finmatica.
 ***********************************************************************/

package it.finmatica.dmServer.motoreRicerca;
import it.finmatica.dmServer.util.*;

// -----------------------------------
// Definizione della classe principale
// -----------------------------------
public class GD4_Criterio_And extends A_Criterio_And
{
  
  /* DEFINIZIONE DEI METODI PUBBLICI */

  // -------------------------
  // Costruttore
  // ------------------------- 
  public GD4_Criterio_And()
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
             sSelect.append(montaCriterio(Global.Split(getCondizioni().getCondizione(Global.COND_AND),(" ")),aDominio[i]));

           //  if (i+1!=aDominio.length) sSelect.append(" or exists ");  
              if (i+1!=aDominio.length) sSelect.append(" or  ");  
        }
                
         return sSelect.toString();
  }

  /* DEFINIZIONE DEI METODI PRIVATI */

  // -----------------------------------
  // Costruisce il criterio di ricerca
  // select 1 from dual
  // where exists (condiz_tab)
	//   and exists (condiz_tab) 
  // -----------------------------------  
  private String montaCriterio(String[] aCond,String aDominio) throws Exception
  {    
          StringBuffer sSelect = new StringBuffer("(exists ");
          StringBuffer sContains = new StringBuffer("");
          
          for (int i = 0; i <aCond.length; i++) {     
         
              if (aCond[i].equals("") ) continue;
              if (!aDominio.equals(vu.Global.DOMINIO_RICERCA_OGGETTIFILE)) {
                 try {
                   sSelect.append((new CondizioniTabella(this.vu)).whereTabella(aDominio,aCond[i]));
                 }
                 catch (Exception e)
                 {
                   throw new Exception("GD4_Criterio_And::montaCriterio exception 1\n"+e.getMessage());
                 }
                  if (i+1!=aCond.length) sSelect.append(" and exists ");
              }
              else{
                sContains.append(aCond[i]);
                if (i+1!=aCond.length)
                    sContains.append(" and ");
                else{
                    if (getCondizioni().getSameWord()) 
                        try {
                           sSelect.append((new CondizioniTabella(this.vu)).whereTabella(aDominio,Global.replaceAll(sContains.toString(),"and ", "and $?"))); 
                        }
                        catch (Exception e)
                        {
                           throw new Exception("GD4_Criterio_And::montaCriterio exception 2\n"+e.getMessage());
                        }
                    else
                        try {
                           sSelect.append((new CondizioniTabella(this.vu)).whereTabella(aDominio,sContains.toString()));                         
                        }
                         catch (Exception e)
                        {
                           throw new Exception("GD4_Criterio_And::montaCriterio exception 3\n"+e.getMessage());
                        }
                }
              }
          }
          sSelect.append(")");
          return sSelect.toString();
  }

}