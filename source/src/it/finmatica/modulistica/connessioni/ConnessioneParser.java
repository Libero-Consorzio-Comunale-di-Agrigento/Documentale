package it.finmatica.modulistica.connessioni;
import java.util.*;
import it.finmatica.textparser.AbstractParser;
import it.finmatica.modulistica.parametri.Parametri;

public class ConnessioneParser extends AbstractParser {
  
  public ConnessioneParser() {
  }

  /**
   * 
   */
  protected String findParamValue(String nomePar, Properties extraKeys) {
    String retval = Parametri.getParametriDomini(nomePar);
    if (retval == null) {
      retval = ":"+nomePar;
    }
    return retval;
  }

}