package it.finmatica.modutils.multirecord;

import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.authentication.Cryptable;
import java.sql.*;
import org.apache.log4j.Logger;
import it.finmatica.modulistica.parametri.Parametri;

public class Connessione {
  private String driver = "";
  private String connessione = "";
  private String utente = "";
  private String passwd = "";
  private static Logger logger = Logger.getLogger(Connessione.class);

  public Connessione(IDbOperationSQL dbOp, String sDsn) {
    String query ="";
    String istanza = "";
    String passwdC = "";
    ResultSet rst = null;
    try {
      query = "SELECT DRIVER, CONNESSIONE, "+ 
              "UTENTE, PASSWD, ISTANZA_AD4 "+
              "FROM CONNESSIONI "+
              "WHERE DSN = :DSN";

      dbOp.setStatement(query);
      dbOp.setParameter(":DSN", sDsn);
      dbOp.execute();
      rst = dbOp.getRstSet();

      if (rst.next()) {
        istanza = rst.getString("ISTANZA_AD4");
        driver = rst.getString("DRIVER");
        connessione = rst.getString("CONNESSIONE");
        utente = rst.getString("UTENTE");
        passwdC = rst.getString("PASSWD");

        if (istanza == null) {
          istanza = "";
        }
        if(!istanza.equalsIgnoreCase("")) {
          query = "SELECT DATABASE_LINK, USER_ORACLE, PASSWORD_ORACLE"+
                  "  FROM AD4_ISTANZE"+
                  " WHERE ISTANZA = :ISTANZA";
          dbOp.setStatement(query);
          dbOp.setParameter(":ISTANZA", istanza);
          dbOp.execute();
          rst = dbOp.getRstSet();
          if (rst.next()) {
            driver      = "oracle.jdbc.driver.OracleDriver";
            connessione = rst.getString("DATABASE_LINK");
            utente      = rst.getString("USER_ORACLE");
            passwdC     = rst.getString("PASSWORD_ORACLE");
          }
        }
        passwd = Cryptable.decryptPasswd(passwdC);
      }
              
    } catch (Exception e) {
      loggerError("Connessione - Attenzione: Si è verificato un errore: "+e.toString(),e);
    }

  }

  public String getDriver() {
    return driver;
  }

  public String getConnessione() {
    return connessione;
  }

  public String getUtente() {
    return utente;
  }

  public String getPassword() {
    return passwd;
  }

  /**
   * 
   */
  private void loggerError(String sMsg, Exception e) {
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        logger.error(sMsg);
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        logger.error(sMsg,e);
      } 
  }
}
