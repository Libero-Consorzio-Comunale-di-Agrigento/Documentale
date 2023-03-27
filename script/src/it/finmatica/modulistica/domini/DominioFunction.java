package it.finmatica.modulistica.domini;

import java.util.Properties;

/**
 * La classe gestisce tutti quei domini che richiedono dati a fonti
 * esterne. Queste fonti dati possono essere fonti dati ODBC o raggiungibili 
 * con l'utilizzo di un qualsiasi driver JDBC certificato.
 * Ogni dominio di questo tipo si basa su 5 parametri fondamentali:
 *    driver:       stringa che rappresentail driver che deve essere caricabile dal servlet per la lettura 
 *                  della fonte dati (es.  org.gjt.mm.mysql.Driver per MySql). Nel caso che non venga 
 *                  specificato si suppone un collegamento ODBC standard e si carica il driver 
 *                  "sun.jdbc.odbc.JdbcOdbcDriver" ma di questo si occupa la classe chiamante;
 *    connessione:  stringa che identifica la connessione da usare, deve essere compatibile con il driver 
 *                  passato (es. jdbc:mysql://SERVER/DB per una conn MySql);
 *    user:         username da usare con la connessione se richiesto
 *    passwd:       password corrispondente allo username per la connessione
 *    funzione:     la funzione da eseguire per il recupero dei dati. Questa funzione deve contenere 
 *                  un SQL interpretabile dal driver usato.
 *    
 * @author  Marco Bonforte
  * @see moodulisticapack.ListaDomini
 */
 
//import java.util.*;
import java.sql.*; 
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.textparser.AbstractParser;
import org.apache.log4j.Logger;
import it.finmatica.modulistica.connessioni.ConnessioneParser;


public class DominioFunction extends Dominio {
  private static    Logger      logger = Logger.getLogger(DominioFunction.class);
  
  public DominioFunction (Connection pConn, String pArea, String pDominio, String pTipo, String pOrdinamento, String pDriver, String pConnessione,
                     String pUser, String pPasswd, String pIstruzione, AbstractParser pAbPar) throws Exception {
    super(pConn, pArea, pDominio, pTipo, pOrdinamento);
    
    driver      = pDriver;
    if (driver == null) {
      driver = "";
    }
    if (!driver.equalsIgnoreCase("")) {
      String compConn = completaConnessione(pConnessione);
      ConnessioneParser cp = new ConnessioneParser();
      connessione = cp.bindingDeiParametri(compConn);
      if (connessione == null){
        connessione = pConnessione;
      }
      connessione = connessione.replaceAll("::",":");
      user        = pUser;
      passwd      = pPasswd;
    }
    istruzione  = pIstruzione;
    
    caricaValori(pAbPar);
  }

 /**
  * Viene eseguita la function che ritorni una sequenza
  * di coppie che mi rappresenteranno la classica accoppiata CODICE VALORE.
  * 
 * @author  Marco Bonforte
  * @param mp AbstractParser.
  */
  protected void caricaValori(AbstractParser mp) {
    String              queryStm = null, strStream;
//    String        rstcodice = null;
//    String        rstvalore = null;
//    int           nCaratteri = 0;
    IDbOperationSQL      dbOpSql = null;

    
    try {
      Properties pmp = new Properties();
      pmp.setProperty("TIPO","S");
      mp.setExtraKeys(pmp);
      queryStm = mp.bindingDeiParametri(istruzione);
      if (queryStm == null){
        // Ritorno senza riempire alcun valore poichè si è
        // verificato un errore durante il BINDING dei parametri
        // probabilmente un parametro mancante.
        logger.error("DominioFunction::caricaValori() - Area: "+area+" Dominio: "+Dominio+" Attenzione! Parametro mancante su "+istruzione);
        return;  // *** exit point ***
      }

      dbOpSql = SessioneDb.getInstance().createIDbOperationSQL(driver, connessione, user, passwd);
      dbOpSql.setCallFunc(queryStm);
      dbOpSql.execute();
      strStream = dbOpSql.getCallSql().getString(1);

      scriviValoriDaStream(strStream);
    } catch (Exception sqle) {
      loggerError("DominioFunction::caricaValori() - Area: "+area+" Dominio: "+Dominio+" Errore SQL: "+sqle.getMessage(),sqle);

    }
    finally {
      free(dbOpSql);
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