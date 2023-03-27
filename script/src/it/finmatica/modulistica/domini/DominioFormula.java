package it.finmatica.modulistica.domini;

import java.util.Properties;
import java.sql.*; 
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.instantads.WrapParser;
import org.apache.log4j.Logger;
import it.finmatica.textparser.AbstractParser;
import it.finmatica.modulistica.connessioni.Connessione;
import it.finmatica.modulistica.connessioni.ConnessioneParser;

public class DominioFormula extends Dominio {
  private        String sCampo = null;
  private static Logger logger = Logger.getLogger(DominioFormula.class);
  private        Connection  pConn;

  public DominioFormula(Connection pConn, String pArea, String pDominio, String pTipo, String pOrdinamento, String pCampo, AbstractParser pAbPar) throws Exception {
    super(pConn, pArea, pDominio, pTipo, pOrdinamento);
    this.pConn = pConn;
    sCampo = pCampo;
    caricaDominio(pAbPar);
  }

  /**
   * 
   */
  private void caricaDominio(AbstractParser pAbPar) {
    IDbOperationSQL dbOp = null;
    ResultSet   resultQuery = null;
    String      query, pConnessione;

    query = "SELECT * FROM DOMINI " +
            "WHERE AREA = :AREA"+
            "  AND DOMINIO = :DOMINIO"+
            "  AND PRECARICA = 'F'";
            
    try {
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(this.pConn,0);
      dbOp.setStatement(query);
      dbOp.setParameter(":AREA", area);
      dbOp.setParameter(":DOMINIO", Dominio);
      dbOp.execute();
      resultQuery = dbOp.getRstSet();
      
      if (resultQuery.next()) {
        tipo = resultQuery.getString("TIPO");

        // Ricavo l'istruzione (il campo è un CLOB).
        Clob clob = resultQuery.getClob("ISTRUZIONE");
        long clobLen = clob.length();
        if (clobLen < MAXLEN_ISTRUZIONE) {
          int i_clobLen = (int)clobLen;
          istruzione = clob.getSubString(1, i_clobLen);
        } else {
          logger.error("DominioFormula::caricaDominio() - Area: "+area+" Dominio: "+Dominio+" Attenzione! Si è verificato un errore. L'istruzione per il caricamento del dominio supera i "+MAXLEN_ISTRUZIONE+" caratteri.");
        }

        if (tipo.equals("O") || tipo.equals("F")) {
          // Determino il driver da utilizzare
          driver = resultQuery.getString("DRIVER");
          if ((driver == null) || (driver.trim().length() == 0)) {
            driver = ""; 
          }
        } else {
          driver = ""; 
        }

        if (!driver.equalsIgnoreCase("")) {
          // Leggo i parametri di connessione
          pConnessione = resultQuery.getString("CONNESSIONE");
          if (pConnessione == null) {
            connessione = "";
          } else {
  //          connessione = completaConnessione(resultQuery.getString("CONNESSIONE"));
            String compConn = completaConnessione(pConnessione);
            ConnessioneParser cp = new ConnessioneParser();
            connessione = cp.bindingDeiParametri(compConn);
  //          connessione = bindingDinamico(request, compConn, true);
            if (connessione == null){
              connessione = pConnessione;
            }
          }
          user = resultQuery.getString("UTENTE");
          passwd = resultQuery.getString("PASSWD");
        }
        
        String sDsn = resultQuery.getString("DSN");
        if (sDsn == null) {
          sDsn = "";
        }
        if (!sDsn.equalsIgnoreCase("")) {
          Connessione cn = new Connessione(dbOp,sDsn);
          driver      = cn.getDriver();
          connessione = cn.getConnessione();
          user        = cn.getUtente();
          passwd      = cn.getPassword();
        }

        if (tipo.equals("O"))
            caricaValoriConn(pAbPar);
        else if (tipo.equals("F"))
            caricaValoriFunction(pAbPar);
        else if (tipo.equals("J"))
            caricaValoriJava(pAbPar);
        else if (tipo.equals("S"))
            caricaValoriStandard(pAbPar);
      }
      
    } catch(Exception e) {
      loggerError("DominioFormula::caricaDominio() - Area: "+area+" Dominio: "+Dominio+" Attenzione: il dominio presenta delle anomalie. "+e.toString(),e);
    }
    finally {
      free(dbOp);
    }

  }

  /**
   * 
   */
  protected void caricaValoriStandard(AbstractParser mp) {
    String              strStream = null;

    try {
      strStream = mp.bindingDeiParametri(istruzione);
      if (strStream == null){
        strStream = "";
      }

      if (strStream.length() < 32000) {
        codici.add(sCampo); // CODICE
        valori.add(strStream); // VALORE
      } else {
        codici.clear();
        valori.clear();
        codici.add("TROPPI DATI");
        valori.add("TROPPI DATI");
      }

    } catch (Exception sqle) {
      loggerError("DominioVisualizza::caricaValoriStandard() - Area: "+area+" Dominio: "+Dominio+" Errore SQL: "+sqle.getMessage(),sqle);
    } 
  }

  /**
   * 
   */
  protected void caricaValoriConn(AbstractParser mp) {
    IDbOperationSQL      dbOp = null;
    ResultSet           resultQuery = null;
//    String              queryStm = null, strStream;
    String              queryStm = null;
    String              rstvalore = null;


    try {
      Properties pmp = new Properties();
      pmp.setProperty("TIPO","S");
      mp.setExtraKeys(pmp);
      queryStm = mp.bindingDeiParametri(istruzione);
      if (queryStm == null){
        // Ritorno senza riempire alcun valore poichè si è
        // verificato un errore durante il BINDING dei parametri
        // probabilmente un parametro mancante.
        logger.error("DominioFormula::caricaValoriConn() - Area: "+area+" Dominio: "+Dominio+" Attenzione! Parametro mancante su "+istruzione);
        return;  // *** exit point ***
      }

      if (driver.equalsIgnoreCase("")) {
        dbOp = SessioneDb.getInstance().createIDbOperationSQL(this.pConn,0);
      } else {
        dbOp = SessioneDb.getInstance().createIDbOperationSQL(driver, connessione, user, passwd);
      }
      dbOp.setStatement(queryStm);
      dbOp.execute();
      resultQuery = dbOp.getRstSet();
      while (resultQuery.next()) {
        if (rstvalore == null) {
          rstvalore = resultQuery.getString(1);
        } else {
          rstvalore += ";"+resultQuery.getString(1);
        }
      }
      if (rstvalore == null) {
        rstvalore = "";
      }
      

      if (rstvalore.length() < 32000) {
        codici.add(sCampo); // CODICE
        valori.add(rstvalore); // VALORE
      } else {
        codici.clear();
        valori.clear();
        codici.add("TROPPI DATI");
        valori.add("TROPPI DATI");
      }

    } catch (Exception e) {
      loggerError("DominioFormula::caricaValoriConn() - Area: "+area+" Dominio: "+Dominio+" Attenzione: il dominio presenta delle anomalie! Errore "+e.toString(),e);

      codici.add(sCampo); // CODICE
      valori.add(""); // VALORE
    }
    finally {
      free(dbOp);
    }
    
  }

  /**
   * 
   */
  protected void caricaValoriFunction(AbstractParser mp) {
    IDbOperationSQL      dbOp = null;
    String              queryStm = null;
    String              rstvalore = null;

    try {
      Properties pmp = new Properties();
      pmp.setProperty("TIPO","S");
      mp.setExtraKeys(pmp);
      queryStm = mp.bindingDeiParametri(istruzione);
      if (queryStm == null){
        // Ritorno senza riempire alcun valore poichè si è
        // verificato un errore durante il BINDING dei parametri
        // probabilmente un parametro mancante.
        logger.error("DominioFormula::caricaValoriFunction() - Area: "+area+" Dominio: "+Dominio+" Attenzione! Parametro mancante su "+istruzione);
        return;  // *** exit point ***
      }

      if (driver.equalsIgnoreCase("")) {
        dbOp = SessioneDb.getInstance().createIDbOperationSQL(this.pConn,0);
      } else {
        dbOp = SessioneDb.getInstance().createIDbOperationSQL(driver, connessione, user, passwd);
      }
      dbOp.setCallFunc(queryStm);
      dbOp.execute();
      rstvalore = dbOp.getCallSql().getString(1);
      if (rstvalore == null) {
        rstvalore = "";
      }
      if (rstvalore.length() < 32000) {
        codici.add(sCampo); // CODICE
        valori.add(rstvalore); // VALORE
      } else {
        codici.clear();
        valori.clear();
        codici.add("TROPPI DATI");
        valori.add("TROPPI DATI");
      }

    } catch (Exception e) {
      loggerError("DominioFormula::caricaValoriFunction() - Area: "+area+" Dominio: "+Dominio+" Attenzione: il dominio presenta delle anomalie! Errore "+e.toString(),e);

      codici.add(sCampo); // CODICE
      valori.add(""); // VALORE
    }
    finally {
      free(dbOp);
    }

  }

  /**
   * 
   */
  protected void caricaValoriJava(AbstractParser mp) {
    String              javaStm = null;
    String              strStream = null;

    try {
      Properties pmp = new Properties();
      pmp.setProperty("TIPO","J");
      mp.setExtraKeys(pmp);
      javaStm = mp.bindingDeiParametri(istruzione);
      if (javaStm == null){
        // Ritorno senza riempire alcun valore poichè si è
        // verificato un errore durante il BINDING dei parametri
        // probabilmente un parametro mancante.
        logger.error("DominioJava::caricaValori() - Area: "+area+" Dominio: "+Dominio+" Attenzione! Parametro mancante su "+istruzione);
        return;  // *** exit point ***
      }

      try{
        WrapParser wp = new WrapParser(javaStm);
        strStream = wp.go();
      } catch (Exception ijEx) {
        loggerError("DominioJava::caricaValori() - Area: "+area+" Dominio: "+Dominio+" Errore Java: "+ijEx.toString(),ijEx);
      }
      if (strStream == null) {
        strStream = "";
      }

      if (strStream.length() < 32000) {
        codici.add(sCampo); // CODICE
        valori.add(strStream); // VALORE
      } else {
        codici.clear();
        valori.clear();
        codici.add("TROPPI DATI");
        valori.add("TROPPI DATI");
      }

    } catch (Exception sqle) {
      loggerError("DominioJava::caricaValori() - Area: "+area+" Dominio: "+Dominio+" Errore SQL: "+sqle.getMessage(),sqle);
      codici.add(sCampo); // CODICE
      valori.add(""); // VALORE
    } 
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