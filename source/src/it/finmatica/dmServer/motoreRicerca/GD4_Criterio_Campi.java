/***********************************************************************
 * Module:  GD4_Criterio_Campi.java
 * @author  Giuseppe Mannella, Andrea Alì
 * Purpose: Classe per la gestione dei criteri impostati a partire
 *          da un tipo documento ed i valori di alcuni suoi campi
 ***********************************************************************/

package it.finmatica.dmServer.motoreRicerca;
import java.util.*;
// -----------------------------------
// Definizione della classe principale
// -----------------------------------
public class GD4_Criterio_Campi extends A_Criterio_Campi
{
  
  int index;
  
  /* DEFINIZIONE DEI METODI PUBBLICI */

  // -------------------------
  // Costruttore
  // ------------------------- 
  public GD4_Criterio_Campi()
  {

  }
  
  public String montaCriterio() throws Exception
  {    
         int size = getCondizioni().getListaCondizioniCampi().size();         
         StringBuffer sSelect = new StringBuffer("");
         Properties p = new Properties();
         String sSingoloCriterio;

         for(int i=0; i<size; i++){
         
             try {
                sSingoloCriterio=((new CondizioniTabella(this.vu)).whereCampiValori( getCondizioni().getIdCampoLista(i), 
                                                                 getCondizioni().getValoreCampoLista(i),
                                                                 getCondizioni().getOperCampoLista(i),i)); 
             
                if (i!=0) sSelect.append(" and ");
                sSelect.append(" c"+(i+1)+".id_campo= "+getCondizioni().getIdCampoLista(i));
                sSelect.append(" and d.id_documento = t"+(i+1)+".id_documento ");
                sSelect.append(" and c"+(i+1)+".id_campo=t"+(i+1)+".id_campo ");
             }
             catch (Exception e)
             {
                throw new Exception("GD4_Criterio_Campi::montaCriterio\n"+ e.getMessage());
             }                          
           
             if (p.containsKey(getCondizioni().getIdCampoLista(i)) &&
            	 !getCondizioni().getOperCampoLista(i).equals("M") &&
            	 !getCondizioni().getOperCampoLista(i).equals("m") &&
            	 !getCondizioni().getOperCampoLista(i).equals("MU") &&
            	 !getCondizioni().getOperCampoLista(i).equals("mu")) {
                Vector v = (Vector)p.get(getCondizioni().getIdCampoLista(i));

                v.add(sSingoloCriterio);
                         
                p.put(getCondizioni().getIdCampoLista(i),v);
             }
             else {
                Vector v = new Vector();
                          
                v.add(sSingoloCriterio);
                if (getCondizioni().getOperCampoLista(i).equals("M") ||
                   	getCondizioni().getOperCampoLista(i).equals("m") ||
                	getCondizioni().getOperCampoLista(i).equals("MU") ||
                	getCondizioni().getOperCampoLista(i).equals("mu"))                	
                	p.put(getCondizioni().getIdCampoLista(i)+""+i,v);
                else
                	p.put(getCondizioni().getIdCampoLista(i),v);
             }
             
        }
        
        Enumeration eCriteriAnd = p.propertyNames();                    
        
        sSelect.append(" and ");
        
        while (eCriteriAnd.hasMoreElements()) {
              String idCampo=(String)eCriteriAnd.nextElement();
             
              Vector vOr=(Vector)p.get(idCampo);
              
              sSelect.append(" ( ");
              
              for(int i=0;i<vOr.size();i++) {
                 sSelect.append("  "+vOr.get(i)+" ");
 
                 if (i+1!=vOr.size()) sSelect.append(" or ");
              }
              
              if (eCriteriAnd.hasMoreElements()) 
                  sSelect.append(" ) and ");  
              else
                  sSelect.append(" ) ");  
              
        }
 
        // sSelect.append(")");
      
         return sSelect.toString();
  }

  
}