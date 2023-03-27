package it.finmatica.dmServer;

/*
 * GESTIONE DEGLI TIPI DOCUMENTO
 * NEL DM DI HUMMINGBIRD
 * 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   16/09/2005
 * 
 * */

public class Humm_Tipo_Documento extends A_Tipo_Documento
{

  // ***************** METODI DI INIZIALIZZAZIONE ***************** // 
 
  /*
   * METHOD:      Constructor
   *
   * DESCRIPTION: Inizializza dati
   * 
   * RETURN:      none
  */
  public Humm_Tipo_Documento()
  {
  }

 // ***************** METODI DI GESTIONE TIPO DOCUMENTO ***************** //
 
  public String toString() 
  {        
         return super.toString();
  }

  /*
   * METHOD:      retrieve(boolean) 
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Carica un tipo documento dal Database
   *
   * RETURN:      void
  */
  // -------------------------------------
  // Carica un tipo documento dal Database
  // -------------------------------------
  public void retrieve(boolean flagLibreria) throws Exception 
  {
         
  }

}