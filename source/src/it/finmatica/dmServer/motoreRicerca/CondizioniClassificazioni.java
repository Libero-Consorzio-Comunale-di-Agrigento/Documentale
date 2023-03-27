package it.finmatica.dmServer.motoreRicerca;

// ------------------------------------
// Definizione della classe che serve 
// alla gestione della ricerca sulla
// coppia dato, valore da ricercare
// per quel tipo dato
// ------------------------------------
public class CondizioniClassificazioni
{
  /* DEFINIZIONE DELLE VARIABILI PRIVATE */

  private String sClassif;
  private String valoreClassif;
  private String operatore; 

  // -----------------------
  // Costruttore
  // -----------------------

  
  public CondizioniClassificazioni(Object newClassif, Object newValoreClassif, String oper) 
  {
         sClassif      = (String) newClassif;
         valoreClassif = (String) newValoreClassif;
         operatore  = oper;
  }
  
  // -----------------------
  // Metodi getter e setter
  // -----------------------
  public void setClassif(String newClassif) 
  {
         sClassif= newClassif;
  }

  public String getClassif() 
  {
         return sClassif;
  }

  public void setValoreClassif(String newValoreClassif) 
  {
         valoreClassif=newValoreClassif;
  }

  public String getValoreClassif() 
  {
         return valoreClassif;
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