package xmlpack;

public class DatoXML 
{
  protected String    nomeCampo;
  protected String    valore;

  /**
  * Costruttore
  */
  public DatoXML(String pNomeCampo, String pValore)
  {
    nomeCampo = pNomeCampo;
    valore = pValore;
  }
  /**
   * Metodo di lettura pubblico
   *
   * @return valore del nomeCampo
   */
   public String getNomeCampo(){
    return nomeCampo;
   };
   
  /**
   * Metodo di lettura pubblico
   *
   * @return valore del valore
   */
   public String getValore(){
    return valore;
   };
}