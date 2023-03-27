package it.finmatica.dmServer;

/*
 * CLASSE ASTRATTA PER LA GESTIONE DEGLI STATI DOCUMENTO
 * DIPENDENZE CON: GD4_Stati_Documento
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   16/09/2005
 * 
 * */ 
 
public abstract class A_Status_Documento 
{
  // Variabili private
  private   String idDocumento = "0";
  private   String stato;    
  protected Environment varEnv;
  protected boolean bSaltaUpdateDoc;
  
 // ***************** METODO DI INIZIALIZZAZIONE ***************** // 
      
  /*
   * METHOD:      inizializzaDati(Object, String, Object)
   * SCOPE:       PUBLIC
   * DESCRIPTION: vEnv    -> Oggetto Variabile di ambiente
   *              idDoc   -> id Documento 
   *              dbOp    -> Oggetto Connessione
   *              Inizializza i dati e sostituisce il metodo costruttore 
   * RETURN:      void
  */  
  public void inizializzaDati(Object vEnv, String idDoc)
  {
         this.idDocumento=idDoc;    
         this.varEnv = (Environment)vEnv;
  }  

  // ***************** METODI DI SET E GET ***************** //
  
  public String getIdDocumento()
  {
         return idDocumento;
  }
   
  public void setIdDocumento(String newIdDocumento)
  {
         idDocumento = newIdDocumento;
  }

  public String getStato()
  {
         return stato;
  }
   
  public void setStato(String newStato)
  {
         stato = newStato;
  }
  
  public void setBSaltaUpdateDoc(boolean saltaUpdateDoc) {
	     bSaltaUpdateDoc = saltaUpdateDoc;
  }  
  
   // ***************** DEFINIZIONE METODI ASTRATTI ***************** //
 
  public abstract boolean registraStato()  throws  Exception;  
  public abstract void loadStato()  throws  Exception;  

}