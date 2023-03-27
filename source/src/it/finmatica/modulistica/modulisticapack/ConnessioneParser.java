package it.finmatica.modulistica.modulisticapack;
import java.util.*;
//import java.sql.*;
//import javax.servlet.http.*;
//import it.finmatica.jfc.dbUtil.*;
import it.finmatica.modulistica.parametri.Parametri;
//import it.finmatica.jfc.bcUtil.*;
import it.finmatica.textparser.AbstractParser;
//import it.finmatica.dmServer.management.*;
 
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