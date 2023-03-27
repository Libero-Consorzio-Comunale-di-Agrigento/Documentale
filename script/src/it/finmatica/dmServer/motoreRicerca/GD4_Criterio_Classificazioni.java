/***********************************************************************
 * Module:  GD4_Criterio_Campi.java
 * @author  Giuseppe Mannella, Andrea Alì
 * Purpose: Classe per la gestione dei criteri impostati a partire
 *          da un tipo documento ed i valori di alcuni suoi campi
 ***********************************************************************/
 package it.finmatica.dmServer.motoreRicerca;


public class GD4_Criterio_Classificazioni extends A_Criterio_Campi 
{
  int index;

  /* DEFINIZIONE DEI METODI PUBBLICI */

  // -------------------------
  // Costruttore
  // ------------------------- 
  public GD4_Criterio_Classificazioni()
  {
  }

  //TO BE IMPL
  public String montaCriterio(int indexCriterio) throws Exception { 
         index= indexCriterio;
         
         return montaCriterio();
  }

  public String montaCriterio() throws Exception
  {    
         int size = getCondizioni().getListaCondizioniClassif().size();
          StringBuffer sSelect = new StringBuffer("(exists ");
          
         for(int i=0; i<size; i++){
             try {
                sSelect.append((new CondizioniTabella(this.vu)).whereClassificazioniValori( getCondizioni().getClassifLista(i), 
                                                                 getCondizioni().getValoreClassifLista(i),
                                                                 getCondizioni().getOperClassifLista(i)) ); 
             }
             catch (Exception e)
             {
                throw new Exception("GD4_Criterio_Classificazioni::montaCriterio\n"+e.getMessage());
             }
             
             if (i+1!=size) sSelect.append(" and exists ");  
        }

         sSelect.append(")");
      
         return sSelect.toString();
  }

  
}