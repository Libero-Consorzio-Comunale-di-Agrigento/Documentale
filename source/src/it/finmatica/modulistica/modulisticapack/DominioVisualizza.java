package it.finmatica.modulistica.modulisticapack;
 
import java.util.*;
import java.sql.*;
import javax.servlet.http.*;
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.instantads.WrapParser;
import org.apache.log4j.Logger;

public class DominioVisualizza extends Dominio {
//  Connection  connessioneDBCONN;
  private String sCampo = null;
  private static Logger logger = Logger.getLogger(DominioVisualizza.class);

  public DominioVisualizza(String pArea, String pDominio, String pCm, String pTipo, String pOrdinamento, String pCampo, HttpServletRequest pRequest, IDbOperationSQL dbOpEsterna) throws Exception {
    super(pArea, pDominio, pCm, pTipo, pOrdinamento);
    sCampo = pCampo;
    caricaDominio(pRequest,dbOpEsterna);
  }

  /**
   * 
   */
  private void caricaDominio(HttpServletRequest request, IDbOperationSQL dbOpEsterna) {
    //Debug Tempo
    long ptime = stampaTempo("DominioVisualizza::caricaDominio - Inizio",area,Dominio,0);
    //Debug Tempo
    IDbOperationSQL dbOp = null;
    ResultSet   resultQuery = null;
    String      query,pConnessione;

    query = "SELECT * FROM DOMINI " +
            "WHERE AREA = :AREA"+
            "  AND DOMINIO = :DOMINIO"+
            "  AND PRECARICA = 'V'";
            
    try {
      if (dbOpEsterna==null) {
        dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);
      }
      else {
        dbOp = dbOpEsterna;
      }

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
          logger.error("DominioVisualizza::caricaDominio() - Area: "+area+" Dominio: "+Dominio+" Attenzione! Si è verificato un errore. L'istruzione per il caricamento del dominio supera i "+MAXLEN_ISTRUZIONE+" caratteri.");
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

        if (driver.length() != 0) {
          // Leggo i parametri di connessione
          pConnessione = resultQuery.getString("CONNESSIONE");
          if (pConnessione == null) {
            connessione = "";
          } else {
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
        } else {
          driver      = Parametri.ALIAS;
          connessione = Parametri.SPORTELLO_DSN;
          user        = Parametri.USER;
          passwd      = Parametri.PASSWD;
        }
        
        String sDsn = resultQuery.getString("DSN");
        if (sDsn == null) {
          sDsn = "";
        }
        if (sDsn.length() != 0) {
          Connessione cn = new Connessione(dbOp,sDsn);
          driver      = cn.getDriver();
          connessione = cn.getConnessione();
          user        = cn.getUtente();
          passwd      = cn.getPassword();
        }

        if (tipo.equals("O"))
            caricaValoriConn(request);
        else if (tipo.equals("F"))
            caricaValoriFunction(request);
        else if (tipo.equals("J"))
            caricaValoriJava(request);
        else if (tipo.equals("S"))
            caricaValoriStandard(request);
      }
      
    } catch(Exception e) {
      loggerError("DominioVisualizza::caricaDominio() - Area: "+area+" Dominio: "+Dominio+" Attenzione: il dominio presenta delle anomalie! "+e.toString(),e);
    }
    finally {
      if (dbOpEsterna==null) free(dbOp);
    }


    //Debug Tempo
    stampaTempo("DominioVisualizza::caricaDominio - Fine",area,Dominio,ptime);
    //Debug Tempo
  }

  /**
   * 
   */
  protected void caricaValoriConn(HttpServletRequest pRequest) {
    //Debug Tempo
    long ptime = stampaTempo("DominioVisualizza::caricaValoriConn - Inizio",area,Dominio,0);
    //Debug Tempo
    IDbOperationSQL      dbOp = null;
    ResultSet           resultQuery = null;
    String              queryStm = null/*, strStream*/;
 //   String              rstvalore = null;

//    connessioneDBCONN = apriConnessioneDb();

    try {
      ModulisticaParser mp = new ModulisticaParser(pRequest);
      Properties pmp = new Properties();
      pmp.setProperty("TIPO","S");
      mp.setExtraKeys(pmp);
      queryStm = mp.bindingDeiParametri(istruzione);
//      queryStm = bindingDinamico(pRequest, istruzione, false);
      if (queryStm == null){
        // Ritorno senza riempire alcun valore poichè si è
        // verificato un errore durante il BINDING dei parametri
        // probabilmente un parametro mancante.
        logger.error("DominioVisualizza::caricaValoriConn() - Area: "+area+" Dominio: "+Dominio+" Attenzione! Parametro mancante su "+istruzione);
        //Debug Tempo
        stampaTempo("DominioVisualizza::caricaValoriConn - Fine",area,Dominio,ptime);
        //Debug Tempo
        return;  // *** exit point ***
      }

      dbOp = SessioneDb.getInstance().createIDbOperationSQL(driver, connessione, user, passwd);
      dbOp.setStatement(queryStm);
      dbOp.execute();
      resultQuery = dbOp.getRstSet();
      if (resultQuery.next()) {
        codici.add(sCampo); // CODICE
        valori.add(resultQuery.getString(1)); // VALORE
      } else {
        codici.clear();
        valori.clear();
        codici.add(sCampo); // CODICE
        valori.add(""); // VALORE
      }

    } catch (Exception e) {
      loggerError("DominioFormula::caricaValoriConn() - Area: "+area+" Dominio: "+Dominio+" Attenzione! Errore "+e.toString(),e);

    }
    finally {
      free(dbOp);
    }
    //Debug Tempo
    stampaTempo("DominioVisualizza::caricaValoriConn - Fine",area,Dominio,ptime);
    //Debug Tempo
   
  }

  /**
   * 
   */
  protected void caricaValoriFunction(HttpServletRequest pRequest) {
    //Debug Tempo
    long ptime = stampaTempo("DominioVisualizza::caricaValoriFunction - Inizio",area,Dominio,0);
    //Debug Tempo
    IDbOperationSQL      dbOp = null;
    String              queryStm = null;
    String              rstvalore = null;

//    connessioneDBCONN = apriConnessioneDb();

    try {
      ModulisticaParser mp = new ModulisticaParser(pRequest);
      Properties pmp = new Properties();
      pmp.setProperty("TIPO","S");
      mp.setExtraKeys(pmp);
      queryStm = mp.bindingDeiParametri(istruzione);
//      queryStm = bindingDinamico(pRequest, istruzione, false);
      if (queryStm == null){
        // Ritorno senza riempire alcun valore poichè si è
        // verificato un errore durante il BINDING dei parametri
        // probabilmente un parametro mancante.
        logger.error("DominioVisualizza::caricaValoriFunction() - Area: "+area+" Dominio: "+Dominio+" Attenzione! Parametro mancante su "+istruzione);
        //Debug Tempo
        stampaTempo("DominioVisualizza::caricaValoriFunction - Fine",area,Dominio,ptime);
        //Debug Tempo
        return;  // *** exit point ***
      }
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(driver, connessione, user, passwd);
      dbOp.setCallFunc(queryStm);
      dbOp.execute();
      rstvalore = dbOp.getCallSql().getString(1);
      if (rstvalore.length() < MAXLEN_VALORI) {
        codici.add(sCampo); // CODICE
        valori.add(rstvalore); // VALORE
      } else {
        codici.clear();
        valori.clear();
        codici.add("TROPPI DATI");
        valori.add("TROPPI DATI");
      }

    } catch (Exception e) {
      loggerError("DominioVisualizza::caricaValoriFunction() - Area: "+area+" Dominio: "+Dominio+" Attenzione! Errore "+e.toString(),e);

    }
    finally {
      free(dbOp);
    }
    //Debug Tempo
    stampaTempo("DominioVisualizza::caricaValoriFunction - Fine",area,Dominio,ptime);
    //Debug Tempo
  }

  /**
   * 
   */
  protected void caricaValoriJava(HttpServletRequest pRequest) {
    //Debug Tempo
    long ptime = stampaTempo("DominioVisualizza::caricaValoriJava - Inizio",area,Dominio,0);
    //Debug Tempo
    String              javaStm = null;
    String              strStream = null;

    try {
      ModulisticaParser mp = new ModulisticaParser(pRequest);
      Properties pmp = new Properties();
      pmp.setProperty("TIPO","J");
      mp.setExtraKeys(pmp);
      javaStm = mp.bindingDeiParametri(istruzione);
//      javaStm = bindingDinamico(pRequest, istruzione, false);
      if (javaStm == null){
        // Ritorno senza riempire alcun valore poichè si è
        // verificato un errore durante il BINDING dei parametri
        // probabilmente un parametro mancante.
        logger.error("DominioVisualizza::caricaValoriJava() - Area: "+area+" Dominio: "+Dominio+" Attenzione! Parametro mancante su "+istruzione);
        //Debug Tempo
        stampaTempo("DominioVisualizza::caricaValoriJava - Fine",area,Dominio,ptime);
        //Debug Tempo
        return;  // *** exit point ***
      }

      try{
        WrapParser wp = new WrapParser(javaStm);
        strStream = wp.go();
      } catch (Exception ijEx) {
        loggerError("DominioVisualizza::caricaValoriJava()) - Area: "+area+" Dominio: "+Dominio+" Errore Java: "+ijEx.toString(),ijEx);
      }

      if (strStream.length() < MAXLEN_VALORI) {
        codici.add(sCampo); // CODICE
        valori.add(strStream); // VALORE
      } else {
        codici.clear();
        valori.clear();
        codici.add("TROPPI DATI");
        valori.add("TROPPI DATI");
      }

    } catch (Exception sqle) {
      loggerError("DominioVisualizza::caricaValoriJava() - Area: "+area+" Dominio: "+Dominio+" Errore: "+sqle.getMessage(),sqle);
    } 
    //Debug Tempo
    stampaTempo("DominioVisualizza::caricaValoriJava - Fine",area,Dominio,ptime);
    //Debug Tempo
  }

  /**
   * 
   */
  protected void caricaValoriStandard(HttpServletRequest pRequest) {
    //Debug Tempo
    long ptime = stampaTempo("DominioVisualizza::caricaValoriStandard - Inizio",area,Dominio,0);
    //Debug Tempo
    String              strStream = null;

    try {
      ModulisticaParser mp = new ModulisticaParser(pRequest);
      strStream = mp.bindingDeiParametri(istruzione);
      if (strStream == null){
        strStream = "";
      }
      if (strStream.length() < MAXLEN_VALORI) {
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
    //Debug Tempo
    stampaTempo("DominioVisualizza::caricaValoriStandard - Fine",area,Dominio,ptime);
    //Debug Tempo
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