/***********************************************************************
 * Module:  Condizioni.java
 * @author  Giuseppe Mannella, Andrea Alì
 * Purpose: Classe per la memorizzazione delle condizioni
 *          di ricerca
 ***********************************************************************/

package it.finmatica.dmServer.motoreRicerca;

// --------------
// Sezione Import
// --------------
import it.finmatica.dmServer.Environment;
import java.util.*;

// -----------------------------------
// Definizione della classe principale
// -----------------------------------
public class GD4_Condizioni extends A_Condizioni
{
 
  /* DEFINIZIONE DEI METODI PUBBLICI */

  // ------------------------------------
  // Inizializza l'array delle condizioni
  // e delle condizioni sui campi
  // ------------------------------------
  public void inizializzaCondizioni(Environment vu) 
  {
         super.inizializzaCondizioni();
        
         super.addDominioRicerca(vu.Global.DOMINIO_RICERCA_VALORI);  
         if (vu.Global.USE_INTERMEDIA.equals("S")) 
             super.addDominioRicerca(vu.Global.DOMINIO_RICERCA_OGGETTIFILE);
                              
  }  

  public void inizializzaCondizioniNoIntermedia(String ricercaValori) 
  {
         super.inizializzaCondizioni();
         super.addDominioRicerca(ricercaValori);  
         /*if (Global.USE_INTERMEDIA.equals("S")) 
             super.addDominioRicerca(Global.DOMINIO_RICERCA_OGGETTIFILE);*/
         listaCondizioniTipiDoc = new Vector();  
         listaCondizioniCampi = new Vector();  
         listaCondizioniDati = new Vector();  
  } 
  // -----------------------
  // Metodi getter e setter
  // -----------------------
  public void setIdTipoDoc(Object newIdTipoDoc) 
  {
         idTipoDoc = (String) newIdTipoDoc;
  }

  public Object getIdTipoDoc() 
  {
         return idTipoDoc;
  }

  public void setRichiesta(Object newRichiesta) 
  {
         richiesta = (String) newRichiesta;
  }

  public void setFileName(Object newFileName) 
  {
         fileName = (String) newFileName;
  }

  public Object getFileName() 
  {
         return fileName;
  }

  public Object getRichiesta() 
  {
         return richiesta;
  }

  public void setArea(Object newArea) 
  {
         area = (String) newArea;
  }

  public Object getArea() 
  {
         return area;
  }

// Gestione lista dei campi
  public void addInListaCondizioniCampi(Object idCampo, Object valoreCampo, String operatore) 
  {
         CondizioniCampi cond = new CondizioniCampi(idCampo,valoreCampo, operatore);

         listaCondizioniCampi.addElement(cond);
  }
  
  public Vector getListaCondizioniTipiDoc() 
  {
         return listaCondizioniTipiDoc;
  }
  
  public  void addInListaCondizioniTipiDoc(Object idTipiDoc) 
  {
        listaCondizioniTipiDoc.addElement(idTipiDoc);
  }
  
  public  String getTipiDocLista(int idx)
  {
     return listaCondizioniTipiDoc.elementAt(idx).toString();
  }
  
  public Vector getListaCondizioniCampi() 
  {
         return listaCondizioniCampi;
  }

  public String getIdCampoLista(int idx) 
  {
         return ((CondizioniCampi)(listaCondizioniCampi.elementAt(idx))).getIdCampo();
  }

  public String getValoreCampoLista(int idx) 
  {
         return ((CondizioniCampi)(listaCondizioniCampi.elementAt(idx))).getValoreCampo();
  }

  public String getOperCampoLista(int idx) 
  {
         return ((CondizioniCampi)(listaCondizioniCampi.elementAt(idx))).getOperatore();
  }


// Gestione lista dei dati
  public void addInListaCondizioniDati(Object sDato, Object valoreDato,String operatore ) 
  {
         CondizioniDati cond = new CondizioniDati(sDato,valoreDato, operatore);

         listaCondizioniDati.addElement(cond);
  }
  
  public Vector getListaCondizioniDati() 
  {
         return listaCondizioniDati;
  }

  public String getDatoLista(int idx) 
  {
         return ((CondizioniDati)(listaCondizioniDati.elementAt(idx))).getDato();
  }

  public String getValoreDatoLista(int idx) 
  {
         return ((CondizioniDati)(listaCondizioniDati.elementAt(idx))).getValoreDato();
  }

  public String getOperDatoLista(int idx) 
  {
         return ((CondizioniDati)(listaCondizioniDati.elementAt(idx))).getOperatore();
  }
  
  // Gestione lista dei classificazioni
  public void addInListaCondizioniClassif(Object sClassif, Object valoreClassif, String operatore ) 
  {
         CondizioniClassificazioni cond = new CondizioniClassificazioni(sClassif, valoreClassif, operatore);

         listaCondizioniClassif.addElement(cond);
  }
  
  public Vector getListaCondizioniClassif() 
  {
         return listaCondizioniClassif;
  }

  public String getClassifLista(int idx) 
  {
         return ((CondizioniClassificazioni)(listaCondizioniClassif.elementAt(idx))).getClassif();
  }

  public String getValoreClassifLista(int idx) 
  {
         return ((CondizioniClassificazioni)(listaCondizioniClassif.elementAt(idx))).getValoreClassif();
  }

  public String getOperClassifLista(int idx) 
  {
         return ((CondizioniClassificazioni)(listaCondizioniClassif.elementAt(idx))).getOperatore();
  }
  
  
  public boolean getSameWord() 
  {
         return sameWord;
  }

  public void setSameWord(boolean newSameWord) 
  {
         sameWord = newSameWord;
  }
 
}



