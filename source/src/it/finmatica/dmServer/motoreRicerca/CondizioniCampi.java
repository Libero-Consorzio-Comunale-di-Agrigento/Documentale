package it.finmatica.dmServer.motoreRicerca;

// ------------------------------------
// Definizione della classe che serve 
// alla gestione della ricerca sulla
// coppia id_campo, valore da ricercare
// per quel campo
// ------------------------------------
public class CondizioniCampi
{
  /* DEFINIZIONE DELLE VARIABILI PRIVATE */

  private String idCampo;
  private String valoreCampo;
  private String nomeCampo;
  private String operatore;

  // -----------------------
  // Costruttore
  // -----------------------



  public CondizioniCampi(Object newIdCampo, Object newValoreCampo, String oper) 
  {
         idCampo     = (String) newIdCampo;
         valoreCampo = (String) newValoreCampo;
         operatore   =  oper;
  }
  // -----------------------
  // Metodi getter e setter
  // -----------------------
  public void setIdCampo(String newIdCampo) 
  {
         idCampo = newIdCampo;
  }

  public String getIdCampo() 
  {
         return idCampo;
  }
  
  public void setNomeCampo(String newNomeCampo) 
  {
         nomeCampo=  newNomeCampo;
  }

  public String getNomeCampo() 
  {
         return nomeCampo;
  }
  
  public void setValoreCampo(String newValoreCampo) 
  {
         valoreCampo=newValoreCampo;
  }

  public String getValoreCampo() 
  {
         return valoreCampo;
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