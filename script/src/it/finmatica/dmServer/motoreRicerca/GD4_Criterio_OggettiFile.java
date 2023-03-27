/***********************************************************************
 * Module:  GD4_Criterio_OggettiFile.java
 * @author  Giuseppe Mannella, Andrea Alì
 * Purpose: Classe astratta per la gestione dei criteri di ricerca
 *          diretti sugli oggetti file
 ***********************************************************************/

package it.finmatica.dmServer.motoreRicerca;
import  it.finmatica.dmServer.util.Global;
// -----------------------------------
// Definizione della classe principale
// -----------------------------------
public class GD4_Criterio_OggettiFile extends A_Criterio_OggettiFile
{

  /* DEFINIZIONE DEI METODI PUBBLICI */

  // -------------------------
  // Costruttore
  // ------------------------- 
  public GD4_Criterio_OggettiFile()
  {

  }

  //TO BE IMPL
  public String montaCriterio(int indexCriterio) { return ""; }

  // -----------------------------------
  // Costruisce il criterio di ricerca
  // -----------------------------------   
  public String montaCriterio() throws Exception
  {
         String sFileName = (String) getCondizioni().getFileName();

        if (sFileName!=null){
            if (sFileName.equals(Global.NOBODY))
                return " ( not exists (select 'x' from oggetti_file where filename like '%.BODY' and d.ID_DOCUMENTO=oggetti_file.ID_DOCUMENTO) ) ";
            else 
                return " ( exists (select 'x' from oggetti_file where filename like '%"+sFileName+"%' and d.ID_DOCUMENTO=oggetti_file.ID_DOCUMENTO) ) ";
        }
        else
            return "";
  }

}