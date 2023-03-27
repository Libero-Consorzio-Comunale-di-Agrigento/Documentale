package it.finmatica.dmServer.motoreRicerca;

// ------------------------------------
// Definizione della classe che serve 
// alla gestione della ricerca sulla
// coppia dato, valore da ricercare
// per quel tipo dato
// ------------------------------------
public class CondizioniDati
{
  /* DEFINIZIONE DELLE VARIABILI PRIVATE */

  private String sDato;
  private String valoreDato;
  private String operatore;

  // -----------------------
  // Costruttore
  // -----------------------

  
  public CondizioniDati(Object newDato, Object newValoreDato, String oper) 
  {
         sDato      = (String) newDato;
         valoreDato = (String) newValoreDato;
         operatore  = oper;
  }
  
  // -----------------------
  // Metodi getter e setter
  // -----------------------
  public void setDato(String newDato) 
  {
         sDato= newDato;
  }

  public String getDato() 
  {
         return sDato;
  }

  public void setValoreDato(String newValoreDato) 
  {
         valoreDato=newValoreDato;
  }

  public String getValoreDato() 
  {
         return valoreDato;
  }

    public void setOperatore(String newOperatore) 
  {
         operatore= newOperatore;
  }

  public String getOperatore() 
  {
         return operatore;
  }
}