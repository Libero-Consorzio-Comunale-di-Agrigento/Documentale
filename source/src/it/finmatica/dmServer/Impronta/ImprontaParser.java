package it.finmatica.dmServer.Impronta;
import it.finmatica.dmServer.management.Profilo;
import it.finmatica.textparser.AbstractParser;
import java.util.*;


public class ImprontaParser extends AbstractParser {
  protected Profilo profilo;

  public ImprontaParser(Profilo p ) {
    profilo = p;
  }

  protected String findParamValue(String nomePar, Properties extraKeys) {
    String retval;
    retval = profilo.getCampo(nomePar);
    return retval;
  }
}