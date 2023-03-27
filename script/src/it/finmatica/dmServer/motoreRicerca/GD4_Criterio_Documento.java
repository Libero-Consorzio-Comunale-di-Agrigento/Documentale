/***********************************************************************
 * Module:  GD4_Criterio_Documento.java
 * @author  Giuseppe Mannella, Andrea Alì
 * Purpose: Classe per la gestione dei criteri diretti sul documento
 *          nel DM di Finmatica.
 ***********************************************************************/

package it.finmatica.dmServer.motoreRicerca;

// -----------------------------------
// Definizione della classe principale
// -----------------------------------
public class GD4_Criterio_Documento extends A_Criterio_Documento
{

  /* DEFINIZIONE DEI METODI PUBBLICI */

  // -------------------------
  // Costruttore
  // ------------------------- 
  public GD4_Criterio_Documento()
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
  public String montaCriterio()
  {    
         String sIdTipoDoc = (String) getCondizioni().getIdTipoDoc();
         String sRichiesta = (String) getCondizioni().getRichiesta();
         String sArea      = (String) getCondizioni().getArea();
         String sCriterio = "";

         if (sIdTipoDoc!=null)
            sCriterio = "and d.id_tipodoc = " + sIdTipoDoc + " " ;

         if (sRichiesta!=null)
            sCriterio = sCriterio + "and codice_richiesta = '" + sRichiesta + "' " ;

         if (sArea!=null) {
        	/* String sAnd=" and ";
        	if (sCriterio.equals("")) {
        		sAnd="";
        	}*/
            sCriterio = sCriterio +" and area = '" + sArea +"' ";
         }
            
        /* if (sCriterio.startsWith("and"))
            //Wrappa la replaceFirst
            return  Global.replace(sCriterio,"and", "",false);
         else*/
            return   sCriterio ;
  }

 
}