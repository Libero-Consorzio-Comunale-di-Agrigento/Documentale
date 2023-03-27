package it.finmatica.dmServer;

/*
 * GESTIONE DELLE LIBRERIE
 * NEL DM DI HUMMINGBIRD
 * 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   16/09/2005
 * 
 * */

public class Humm_Libreria extends A_Libreria
{

  // ***************** METODI DI GESTIONE DELLE LIBRERIE ***************** //
 
  public void inizializza(Environment newEnv) {}
 
  /*
   * METHOD:      retrieve()
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Carica una libreria dal Database
   *              
   * RETURN:      boolean
  */ 
  public void retrieve() throws Exception 
  {
  }

  public boolean creaDirectory()
  {
         return false;
  }

}