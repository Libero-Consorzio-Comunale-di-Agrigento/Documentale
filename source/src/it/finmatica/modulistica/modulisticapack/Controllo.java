package it.finmatica.modulistica.modulisticapack;
 
//import java.util.*;
import java.io.Serializable;

/**
 * Classe Controllo
 *
 * @author  Adelmo Gentilini
 * @author  Antonio Plastini
 * @author  Sergio Spadaro
 * @version 1.0
 */
public class Controllo implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  protected String  area;
  protected String  controllo;
  protected int     sequenza;
  protected String  corpo;
  protected String  evento;

  /**
   *
   */
  public Controllo(String pArea, String pControllo, int pSequenza, String pCorpo, String pEvento) throws Exception {

    area      = pArea;
    controllo = pControllo;
    sequenza  = pSequenza;
    corpo     = pCorpo;
    if (pEvento == null) {
      evento = "";
    } else {
      evento    = pEvento;
    }

  }

  /**
   *
   */
  public String getArea() {
    return area;
  }

  /**
   *
   */
  public String getControllo() {
    return controllo;
  }

  /**
   *
   */
  public String getEvento() {
    return evento;
  }

  /**
   *
   */
  public String getValue() {
    // Si presuppone che il controllo sia fatto in tal modo function controllo() {x=2323; ...}
    // ----> il valore di un controllo corrisponde ad una funzione impostata nel repository
    return corpo;
  }

  public int getSequenza() {
    return sequenza;
  }

}

