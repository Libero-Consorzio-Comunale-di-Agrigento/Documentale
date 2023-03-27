package it.finmatica.dmServer;

/*
 * GESTIONE DELLE OGGETTI FILE
 * NEL DM DI HUMMINGBIRD
 * 
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   16/09/2005
 * 
 * */
 
public class Humm_Oggetti_File extends A_Oggetti_File
{
  // ***************** METODI DI INIZIALIZZAZIONE ***************** // 
 
  /*
   * METHOD:      Constructor
   *
   * DESCRIPTION: Inizializza dati
   * 
   * RETURN:      none
  */
  public Humm_Oggetti_File() 
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
  public void inizializzaDati(Object  vUtente)
  {
         this.inizializzaDati( (Environment) vUtente);
  }

  /*
   * METHOD:      inizializzaDati(Environment)
   * SCOPE:       PRIVATE
   *
   * DESCRIPTION: inizializza le variabili di connessione
   *              e di documento
   * 
   * RETURN:      void
  */  
  private void inizializzaDati( Environment vUtente)
  {
          this.varEnv = vUtente;
          this.modificato = "N";
  }

  // ***************** METODI DI GESTIONE OGGETTI FILE ***************** //

  /*
   * METHOD:      isVisible() 
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Controlla se il file è visibile in
   *              funzione della colonna visibile su
   *              formati_file
   *
   * RETURN:      boolean
  */
  public boolean isVisible() throws Exception 
  {
         return true;
  }
  
  /*
   * METHOD:      retrieve() 
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Carica un oggetto file dal DB
   *
   * RETURN:      void
  */
  public void retrieve() throws Exception 
  {
        
  }

  /*
   * METHOD:      insert(Object, A_Libreria, Object) 
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Inserisce nella tabella oggetti_file un
   *              valore di tipo GD4_Oggetti_File.
   *              Viene passato l'id del documento di appartenenza
   *
   * RETURN:      boolean
  */
  public boolean insert(Object idDocumento, A_Libreria aLibreria) throws Exception 
  {             
        return true;
  }

  /*
   * METHOD:      update(Object, A_Libreria) 
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Aggiorna nella tabella oggetti_file un 
   *              valore di tipo Oggetti_file
   *              Viene passato l'id del documento di appartenenza
   *              
   * RETURN:      boolean
  */
  public boolean update(Object idDocumento, A_Libreria aLibreria) throws Exception 
  {      
         // Aggiornamento della stessa riga con il file (bfile o blob)
         return true;  
  }

  /*
   * METHOD:      delete(Object) 
   * SCOPE:       PUBLIC
   *
   * DESCRIPTION: Cancellazione di un oggetto file dal DM
   *              
   * RETURN:      boolean
  */
  public boolean delete(Object sDirectory,String idDocumento) throws Exception
  {
         return true;
  }

  public void setIdOggettoFilePadre(String idOggettoPadre) {}
  
  public Object getFile(boolean bCheck) throws Exception {return null;}
  public String getIdOggettoFilePadre() { return null;}

  public String toString() 
  {
         return super.toString();
  }
 
}