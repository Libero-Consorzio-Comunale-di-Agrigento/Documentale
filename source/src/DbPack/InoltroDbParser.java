package DbPack;
import java.io.*;
import java.util.*;
import java.sql.*;
//import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.jfc.dbUtil.*;
import it.finmatica.textparser.AbstractParser;
import org.apache.log4j.Logger;

import xmlpack.InfoConnessione;

public class InoltroDbParser extends AbstractParser {
  private InfoConnessione infoConnessione;
  private String    idOp = "";
  private String    area = "";
  private String    cm = "";
  private String    cr = "";
  private static Logger logger = Logger.getLogger("it.finmatica.modulistica.inoltro.InoltroDbParser");

  public InoltroDbParser(String idop, String ar, String codiceModello, String codRich, InfoConnessione pm) {
    infoConnessione = pm;
    idOp  = idop;
    area  = ar;
    cm    = codiceModello;
    cr    = codRich;
  }

  protected String findParamValue(String nomePar, Properties extraKeys) {
    DbOperationSQL dbOpVa = null;
    ResultSet      rstVa = null;
    String         query = "";
    String         val = "";

    if (nomePar.equalsIgnoreCase("CR")) {
      val = cr;
      return val;
    }
    if (nomePar.equalsIgnoreCase("AREA")) {
      val = area;
      return val;
    }
    if (nomePar.equalsIgnoreCase("CM")) {
      val = cm;
      return val;
    }
    try {
      dbOpVa = new DbOperationSQL(infoConnessione.getAlias(), 
          infoConnessione.getDsn(), 
          infoConnessione.getUser(), 
          infoConnessione.getPasswd());
      query = "SELECT VALORE"+
               "  FROM PRE_INOLTRO "+
               " WHERE ID_OP = :ID_OP" +
               "   AND CODICE_RICHIESTA = :CODICE_RICHIESTA" +
               "   AND AREA = :AREA" +
               "   AND DATO = :DATO" +
               "   AND CODICE_MODELLO = :CM";
      dbOpVa.setStatement(query);
      dbOpVa.setParameter(":CM",cm);
      dbOpVa.setParameter(":ID_OP",idOp);
      dbOpVa.setParameter(":CODICE_RICHIESTA",cr);
      dbOpVa.setParameter(":AREA",area);
      dbOpVa.setParameter(":DATO",nomePar);
      dbOpVa.execute();
      rstVa = dbOpVa.getRstSet();
      if (!rstVa.next()) {
         val = "";
      } else {
        BufferedInputStream bis2 = dbOpVa.readClob("VALORE");
        StringBuffer sb2 = new StringBuffer();
        int ic2;
        while ((ic2 =  bis2.read()) != -1) {
          sb2.append((char)ic2);
        }
        val = sb2.toString();
      }
    } catch (Exception ex) {
      free (dbOpVa);
      logger.error("InoltroDb::findParamValue() - Attenzione! Errore nella SELECT della FindParamValue: "+ex.toString(),ex);
//      ex.printStackTrace();
      val = "";
    }
          
    free(dbOpVa);
    return val;

  }

  /**
   *
   */
  private void free(DbOperationSQL dbOp) {
    try {
      dbOp.close();
    } catch (Exception e) { }
  }

}