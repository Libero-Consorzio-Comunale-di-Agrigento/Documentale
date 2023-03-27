package it.finmatica.modulistica.modulisticapack;
import it.finmatica.textparser.AbstractParser;

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
 
//import java.sql.*; 
import javax.servlet.http.*;
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import java.util.*;
import org.apache.log4j.Logger;

 
public class DominioFunction extends Dominio {
  private static    Logger      logger = Logger.getLogger(DominioFunction.class);
  
  public DominioFunction (String pArea, String pDominio, String pCm, String pTipo, String pOrdinamento, String pDriver, String pConnessione,
                     String pUser, String pPasswd, String pIstruzione, HttpServletRequest pRequest, IDbOperationSQL dbOpEsterna) throws Exception {
    super(pArea, pDominio, pCm, pTipo, pOrdinamento,dbOpEsterna);
    
    driver      = pDriver;
    if (driver == null) {
      driver = "";
    }
    if (driver.length() != 0) {
      String compConn = completaConnessione(pConnessione);
      ConnessioneParser cp = new ConnessioneParser();
      connessione = cp.bindingDeiParametri(compConn);
  //    connessione = bindingDinamico(pRequest, compConn, true);
      if (connessione == null){
        connessione = pConnessione;
      }
      connessione = connessione.replaceAll("::",":");
      user        = pUser;
      passwd      = pPasswd;
    } else {
      driver      = Parametri.ALIAS;
      connessione = Parametri.SPORTELLO_DSN;
      user        = Parametri.USER;
      passwd      = Parametri.PASSWD;
    }
    istruzione  = pIstruzione;
    
    caricaValori(pRequest);
  }



  protected void caricaValori(HttpServletRequest pRequest) {
    caricaValori(pRequest,null);
  }


  /**
  * Viene eseguita la function che ritorni una sequenza
  * di coppie che mi rappresenteranno la classica accoppiata CODICE VALORE.
  * 
 * @author  Marco Bonforte
  * @param pRequest stringa di request ricevuta dal servlet chiamante.
  */
  protected void caricaValori(HttpServletRequest pRequest, IDbOperationSQL dbOpEsterna) {
    //Debug Tempo
    long ptime = stampaTempo("DominioFunction::caricaValori - Inizio",area,Dominio,0);
    //Debug Tempo
    String              queryStm = null, strStream;
//    String        rstcodice = null;
//    String        rstvalore = null;
//    int           nCaratteri = 0;
    IDbOperationSQL      dbOpSql = null;

    
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
        logger.error("DominioFunction::caricaValori() - Area: "+area+" Dominio: "+Dominio+" Attenzione! Parametro mancante su "+istruzione);
        //Debug Tempo
        stampaTempo("DominioFunction::caricaValori - Fine",area,Dominio,ptime);
        //Debug Tempo
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
    //Debug Tempo
    stampaTempo("DominioFunction::caricaValori - Fine",area,Dominio,ptime);
    //Debug Tempo
  }

  protected void caricaValori(AbstractParser mp) {
    //Debug Tempo
    long ptime = stampaTempo("DominioFunction::caricaValori - Inizio",area,Dominio,0);
    //Debug Tempo
    String              queryStm = null, strStream;
    IDbOperationSQL      dbOpSql = null;

    
    try {
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
    //Debug Tempo
    stampaTempo("DominioFunction::caricaValori - Fine",area,Dominio,ptime);
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