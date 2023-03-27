package it.finmatica.dmServer;

/*
 * GESTIONE DEGLI VALORI
 * NEL DM DI HUMMINGBIRD
 * 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   16/09/2005
 * 
 * */ 

public class Humm_Valori  extends A_Valori
{
  // variabile private
  private A_Campi_Documento campo;
  
  // ***************** METODI DI INIZIALIZZAZIONE ***************** // 
 
  /*
   * METHOD:      Constructor
   *
   * DESCRIPTION: Inizializza dati
   * 
   * RETURN:      none
  */public Humm_Valori() 
  {
  }

  /*
   * METHOD:      inizializzaDati(Object, Object)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: inizializza le variabili di connessione
   *              e di documento
   * 
   * RETURN:      void
  */  
  public void inizializzaDati(Object vUtente) throws Exception
  {
         this.inizializzaDati((Environment) vUtente);
  }

  /*
   * METHOD:      inizializzaDati(Environment)
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: inizializza le variabili di connessione
   *              e di documento
   * 
   * RETURN:      void
  */  
  private void inizializzaDati( Environment vUtente) throws Exception
  {
         this.variabiliUtente = vUtente;
         this.modificato = "N";
         try {
             campo = (A_Campi_Documento)Class.forName(vUtente.Global.PACKAGE + "." + vUtente.Global.DM + 
                                            "_"+ vUtente.Global.CAMPI_DOCUMENTO).newInstance();
         }
         catch (Exception e) {
             throw new Exception("Humm_Valori::inizializzaDati() non riesco a creare l'oggetto di Classe: " + 
                                vUtente.Global.PACKAGE + "." + vUtente.Global.DM + "_"+ vUtente.Global.CAMPI_DOCUMENTO);
         }
  }
  
  // ***************** METODI DI GESTIONE VALORI ***************** //
 
  public void retrieve() throws Exception 
  {
        
  }

  public boolean insert(Object idDocumento) throws Exception 
  {     
         
         return true;
  }

  public boolean update(Object idDocumento) throws Exception 
  {
         
         return true;
  }
  
  public A_Campi_Documento getCampo()
  {
     return campo;
  }
  
  public String toString() 
  {
         return super.toString();
  }
  
}