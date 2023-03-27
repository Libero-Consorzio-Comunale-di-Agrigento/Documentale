package it.finmatica.dmServer;

/*
 * CLASSE ASTRATTA PER LA GESTIONE DELLE LIBRERIE
 * DIPENDENZE CON: GD4_Libreria
 *                 Humm_Libreria
 * AUTHOR @MANNELLA @SCANDURRA
 * DATE   16/09/2005
 * 
 * */
 
import it.finmatica.dmServer.Environment;

public abstract class A_Libreria implements I_Libreria
{
  // Variabili private
  private   String idLibreria = "0";
  private   String libreria;
  private   String directory;
  protected Environment env; 
  
  // ***************** METODI DI INIZIALIZZAZIONE ***************** // 
  public void inizializza(Environment newEnv) 
  {
         env = newEnv;
  }
   
  // ***************** METODI DI SET E GET ***************** //
  
  public String getIdLibreria()
  {
         return idLibreria;
  }
   
  public void setIdLibreria(String newIdLibreria)
  {
         idLibreria = newIdLibreria;
  }
   
  public String getLibreria()
  {
         return libreria;
  }
   
  public void setLibreria(String newLibreria)
  {
         libreria = newLibreria;
  }
   
  public String getDirectory()
  {
         return directory;
  }
   
  public void setDirectory(String newDirectory)
  {
         directory = newDirectory;
  }

  public String toString() 
  {
         StringBuffer objectState= new StringBuffer();

         objectState.append("Classe: " + this.getClass().getName() + "\n");
         objectState.append("idLibreria = " + idLibreria + "\n");
         objectState.append("Libreria = " + libreria + "\n");
         objectState.append("Directory = " + directory);
         
         return objectState.toString();
  }

  // ***************** DEFINIZIONE METODI ASTRATTI ***************** //

  public abstract boolean creaDirectory();
  public abstract void retrieve() throws Exception;

}