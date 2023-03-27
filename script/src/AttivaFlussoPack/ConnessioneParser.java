package AttivaFlussoPack;
import java.util.*;
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.textparser.AbstractParser;

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
