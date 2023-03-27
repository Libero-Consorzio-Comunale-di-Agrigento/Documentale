/***********************************************************************
 * Module:  A_Condizioni.java
 * @author  Giuseppe Mannella, Andrea Alì
 * Purpose: Classe per la memorizzazione delle condizioni
 *          di ricerca
 ***********************************************************************/

package it.finmatica.dmServer.motoreRicerca;

// --------------
// Sezione Import
// --------------
import it.finmatica.dmServer.util.*;
import java.util.*;

// -----------------------------------
// Definizione della classe principale
// -----------------------------------
public abstract class A_Condizioni 
{
  /* DEFINIZIONE DELLE VARIABILI PRIVATE */

  private   String[]   condizioni ; 
  private   Vector     dominioRicerca ;
  protected Vector     listaCondizioniTipiDoc;
  protected Vector     listaCondizioniCampi;
  protected Vector     listaCondizioniDati;
  protected Vector     listaCondizioniClassif;
  protected String     idTipoDoc;  
  protected String     richiesta;
  protected String     area;
  protected String     fileName;
  protected boolean    sameWord = false; 

  /* DEFINIZIONE DEI METODI PUBBLICI */   

  // ------------------------------------
  // Inizializza l'array delle condizioni
  // and, or, not, singole
  // ------------------------------------
  public void inizializzaCondizioni()
  {
         condizioni = new String[Global.MAX_COND];
         dominioRicerca = new Vector();
         dominioRicerca.add("Valori");
         listaCondizioniCampi = new Vector();  
         listaCondizioniDati = new Vector();
  }

  // -----------------------
  // Metodi gettere e setter
  // -----------------------
  public void addCondizione(int tipoCond, String cond) 
  {
         condizioni[tipoCond]=cond;
  }

  public String getCondizione(int tipoCond) 
  {
         return condizioni[tipoCond];
  }

  public void addDominioRicerca( String sEntita) 
  {
         if (sEntita != null)
            dominioRicerca.add(sEntita);
  }

  public Vector getDominioRicerca() 
  {
         return dominioRicerca;
  }

  public String[] splitDominio()
  {
        int size = dominioRicerca.size();
        String[] aDominio = new String[size];
                
        for (int i = 0; i <size; i++) {
              aDominio[i] = (String)dominioRicerca.elementAt(i);
        } 
        return aDominio;
  }

  /* DEFINIZIONE DEI METODI ASTRATTI */   

  public abstract void setIdTipoDoc(Object newIdTipoDoc) ;
  public abstract Object getIdTipoDoc() ;
  
  public abstract void addInListaCondizioniTipiDoc(Object idTipiDoc) ;
  public abstract Vector getListaCondizioniTipiDoc() ;
  public abstract String getTipiDocLista(int idx);
  
  public abstract void setRichiesta(Object newRichiesta) ;
  public abstract Object getRichiesta() ;

  public abstract void setArea(Object newArea) ;
  public abstract Object getArea() ;
  
  public abstract void addInListaCondizioniCampi(Object idCampo, Object valoreCampo, String operatore) ;
  public abstract Vector getListaCondizioniCampi(); 
  public abstract String getIdCampoLista(int idx);
  public abstract String getValoreCampoLista(int idx);
  public abstract String getOperCampoLista(int idx);

  public abstract void addInListaCondizioniDati(Object idDato, Object valoreDato, String operatore) ;
  public abstract Vector getListaCondizioniDati(); 
  public abstract String getDatoLista(int idx);
  public abstract String getValoreDatoLista(int idx);
  public abstract String getOperDatoLista(int idx);
  
  public abstract void addInListaCondizioniClassif(Object idClassif, Object valoreClassif, String operatore) ;
  public abstract Vector getListaCondizioniClassif(); 
  public abstract String getClassifLista(int idx);
  public abstract String getValoreClassifLista(int idx);
  public abstract String getOperClassifLista(int idx);
  
  public abstract void setFileName(Object newFileName);
  public abstract Object getFileName();
  
  public abstract boolean getSameWord();
  public abstract void setSameWord(boolean newSameWord);
}
