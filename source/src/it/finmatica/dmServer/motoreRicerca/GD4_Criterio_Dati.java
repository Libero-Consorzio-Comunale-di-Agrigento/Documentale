/***********************************************************************
 * Module:  GD4_Criterio_Dati.java
 * @author  Giuseppe Mannella, Andrea Alì
 * Purpose: Classe per la gestione dei criteri impostati a partire
 *          da un tipo documento ed i valori di alcuni suoi Dati
 ***********************************************************************/

package it.finmatica.dmServer.motoreRicerca;
import java.util.*;
// -----------------------------------
// Definizione della classe principale
// -----------------------------------
public class GD4_Criterio_Dati extends A_Criterio_Campi
{
  
  /* DEFINIZIONE DEI METODI PUBBLICI */

  // -------------------------
  // Costruttore
  // ------------------------- 
  public GD4_Criterio_Dati()
  {

  }

  //TO BE IMPL
  public String montaCriterio(int indexCriterio) { return ""; }

  public String montaCriterio() throws Exception
  {    
         int size = getCondizioni().getListaCondizioniDati().size();
         StringBuffer sSelect = new StringBuffer("(");
         Properties p = new Properties();
         String sSingoloCriterio;
         
         for(int i=0; i<size; i++){
             try {

                sSingoloCriterio=((new CondizioniTabella(this.vu)).whereDatiValori(  getCondizioni().getArea().toString(),
                                                                getCondizioni().getDatoLista(i), 
                                                                getCondizioni().getValoreDatoLista(i),
                                                                getCondizioni().getOperDatoLista(i)));
                        
             }
             catch (Exception e)
             {
                throw new Exception("GD4_Criterio_Dati::montaCriterio()\n"+e.getMessage());
             }
          
             if (p.containsKey(getCondizioni().getDatoLista(i))) {
                Vector v = (Vector)p.get(getCondizioni().getDatoLista(i));
                          
                v.add(sSingoloCriterio);
                         
                p.put(getCondizioni().getDatoLista(i),v);
             }
             else {
                Vector v = new Vector();
                          
                v.add(sSingoloCriterio);
                         
                p.put(getCondizioni().getDatoLista(i),v);
             }             
        }

        Enumeration eCriteriAnd = p.propertyNames();                    
                  
        while (eCriteriAnd.hasMoreElements()) {
              String idCampo=(String)eCriteriAnd.nextElement();
              
              Vector vOr=(Vector)p.get(idCampo);
              
              sSelect.append(" ( ");
              
              for(int i=0;i<vOr.size();i++) {
                 sSelect.append(" (exists "+vOr.get(i)+")");
                 
                 if (i+1!=vOr.size()) sSelect.append(" or ");
              }
              
              if (eCriteriAnd.hasMoreElements()) 
                  sSelect.append(" ) and ");  
              else
                  sSelect.append(" ) ");  
              
        }

         sSelect.append(")");

         
         return sSelect.toString();
  }

  
}