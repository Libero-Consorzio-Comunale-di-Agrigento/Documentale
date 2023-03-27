package it.finmatica.modulistica.modulisticapack;
import it.finmatica.textparser.AbstractParser;
import java.util.*;

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
 *    istruzione:   la istruzione da eseguire per il recupero dei dati. Questa istruzione deve essere scritta 
 *                  in un SQL interpretabile dal driver usato.
 *    
 * @author  Adelmo Gentilini
 *          Nicola Samoggia
 * @see moodulisticapack.ListaDomini
 */
 
import java.sql.*; 
import javax.servlet.http.*;
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import org.apache.log4j.Logger;
import it.finmatica.modulistica.modulisticapack.ConnessioneParser;
import it.finmatica.modulistica.modulisticapack.Dominio;
  
/**
 *     
 */
public class DominioCONN extends Dominio {
//  Connection  connessioneDBCONN;
  private static    Logger      logger = Logger.getLogger(DominioCONN.class);
  
  public DominioCONN(String pArea, String pDominio, String pCm, String pTipo, String pOrdinamento, String pDriver, String pConnessione,
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
      if (connessione == null){
        connessione = pConnessione;
      }
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
  * Ridefinita per consentire l'uso della connessioneDBCONN.
  * Suppongo che la SELECT di questo tipo di dominio ritorni una stringa nella forma 
  * standard, che è la stessa richiesta dall'OUTPUT NOTES oppure che ritorni una sequenza
  * di coppie che mi rappresenteranno la classica accoppiata CODICE VALORE.
  * Idenifica se usare il primo o il secondo formato a sconda del numero di colonne
  * presenti nella SELECT chiamata: 
  *     se è una sola colonna se la aspetta nel primo formato
  *     e considera solo il primo record estratto;
  *     se sono più di una suppone SEMPRE che il CODICE sia la colonna 1 e che 
  *     il VALORE sia la colonna 2 e tenta di caricare tutti i valori estratti
  *     nelle liste codici e valori.
  * 
  * @author Adelmo Gentilini
  * @param pRequest stringa di request ricevuta dal servlet chiamante.
  */
  protected void caricaValori(HttpServletRequest pRequest, IDbOperationSQL dbOpEsterna) {
    //Debug Tempo
    long ptime = stampaTempo("DominioCONN::caricaValori - Inizio",area,Dominio,0);
    //Debug Tempo
    IDbOperationSQL      dbOp = null;
    ResultSet           resultQuery = null;
    String              queryStm = null, strStream;
    ResultSetMetaData   rsMeta;          
    String        rstcodice = null;
    String        rstvalore = null;
    int           nCaratteri = 0;
    int           nColonne = 0;
    int           i;

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
        logger.error("DominioCONN::caricaValori() - Area: "+area+" Dominio: "+Dominio+" Attenzione! Parametro mancante su "+istruzione);
        //Debug Tempo
        stampaTempo("DominioCONN::caricaValori - Fine",area,Dominio,ptime);
        //Debug Tempo
        return;  // *** exit point ***
      }
//      statement = connessioneDBCONN.createStatement();
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(driver, connessione, user, passwd);
      dbOp.setStatement(queryStm);
      dbOp.execute();
      resultQuery = dbOp.getRstSet();

      if (resultQuery.next()) {
        rsMeta = resultQuery.getMetaData();
      
        if (rsMeta.getColumnCount() == 1) {
          // Prendo lo stream tornato e lo interpreto
          strStream = resultQuery.getString(1);
          scriviValoriDaStream(strStream);
        } else {
          if (rsMeta.getColumnCount() == 2) {
            // Sono già sul primo record li esamino tutti suponendo
            // che siano nella forma CODICE, VALORE
            do {
               rstcodice = resultQuery.getString(1);
               rstvalore = resultQuery.getString(2);
               nCaratteri = nCaratteri + rstcodice.length() + rstvalore.length();
               if (nCaratteri < MAXLEN_VALORI) {
                 caricaOrdinata(rstcodice,rstvalore,ordinamento);
//                 codici.add(rstcodice); // CODICE
//                 valori.add(rstvalore); // VALORE
               } else {
                 codici.clear();
                 valori.clear();
                 codici.add("TROPPI DATI");
                 valori.add("TROPPI DATI");
                 break;
              }
            } while (resultQuery.next());
          } else {
            //Nel caso di più di 3 colonne il nome delle colonne (ALIAS) sarà il codice
            //mentre il corrispettivo valore il valore.
            nColonne = rsMeta.getColumnCount();
            for (i = 1; i <= nColonne; i++) {
              rstcodice = rsMeta.getColumnName(i);
              rstvalore = resultQuery.getString(i);
              if (rstvalore == null) {
                rstvalore = "";
              }
              nCaratteri = nCaratteri + rstcodice.length() + rstvalore.length();
              if (nCaratteri < MAXLEN_VALORI) {
                codici.add(rstcodice); // CODICE
                valori.add(rstvalore); // VALORE
              } else {
                codici.clear();
                valori.clear();
                codici.add("TROPPI DATI");
                valori.add("TROPPI DATI");
                break;
              }
            }
          }
        }
      }
    } catch (Exception sqle) {
      loggerError("DominioCONN::caricaValori() - Area: "+area+" Dominio: "+Dominio+" - Errore SQL: "+queryStm+" \n "+sqle.toString(),sqle);
//      sqle.printStackTrace();
    }
    finally {
      free(dbOp);
    }

    //Debug Tempo
    stampaTempo("DominioCONN::caricaValori - Fine",area,Dominio,ptime);
    //Debug Tempo
  }

  protected void caricaValori(AbstractParser mp) {
    //Debug Tempo
    long ptime = stampaTempo("DominioCONN::caricaValori - Inizio",area,Dominio,0);
    //Debug Tempo
    IDbOperationSQL      dbOp = null;
    ResultSet           resultQuery = null;
    String              queryStm = null, strStream;
    ResultSetMetaData   rsMeta;          
    String        rstcodice = null;
    String        rstvalore = null;
    int           nCaratteri = 0;
    int           nColonne = 0;
    int           i;

//    connessioneDBCONN = apriConnessioneDb();
    
    try {
//      ModulisticaParser mp = new ModulisticaParser(pRequest);
      queryStm = mp.bindingDeiParametri(istruzione);
//      queryStm = bindingDinamico(pRequest, istruzione, false);
      if (queryStm == null){
        // Ritorno senza riempire alcun valore poichè si è
        // verificato un errore durante il BINDING dei parametri
        // probabilmente un parametro mancante.
        logger.error("DominioCONN::caricaValori() - Area: "+area+" Dominio: "+Dominio+" Attenzione! Parametro mancante su "+istruzione);
        //Debug Tempo
        stampaTempo("DominioCONN::caricaValori - Fine",area,Dominio,ptime);
        //Debug Tempo
        return;  // *** exit point ***
      }

//      statement = connessioneDBCONN.createStatement();
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(driver, connessione, user, passwd);
      dbOp.setStatement(queryStm);
      dbOp.execute();
      resultQuery = dbOp.getRstSet();

      if (resultQuery.next()) {
        rsMeta = resultQuery.getMetaData();
      
        if (rsMeta.getColumnCount() == 1) {
          // Prendo lo stream tornato e lo interpreto
          strStream = resultQuery.getString(1);
          scriviValoriDaStream(strStream);
        } else {
          if (rsMeta.getColumnCount() == 2) {
            // Sono già sul primo record li esamino tutti suponendo
            // che siano nella forma CODICE, VALORE
            do {
               rstcodice = resultQuery.getString(1);
               rstvalore = resultQuery.getString(2);
               nCaratteri = nCaratteri + rstcodice.length() + rstvalore.length();
               if (nCaratteri < MAXLEN_VALORI) {
                 caricaOrdinata(rstcodice,rstvalore,ordinamento);
//                 codici.add(rstcodice); // CODICE
//                 valori.add(rstvalore); // VALORE
               } else {
                 codici.clear();
                 valori.clear();
                 codici.add("TROPPI DATI");
                 valori.add("TROPPI DATI");
                 break;
              }
            } while (resultQuery.next());
          } else {
            //Nel caso di più di 3 colonne il nome delle colonne (ALIAS) sarà il codice
            //mentre il corrispettivo valore il valore.
            nColonne = rsMeta.getColumnCount();
            for (i = 1; i <= nColonne; i++) {
              rstcodice = rsMeta.getColumnName(i);
              rstvalore = resultQuery.getString(i);
              if (rstvalore == null) {
                rstvalore = "";
              }
              nCaratteri = nCaratteri + rstcodice.length() + rstvalore.length();
              if (nCaratteri < MAXLEN_VALORI) {
                codici.add(rstcodice); // CODICE
                valori.add(rstvalore); // VALORE
              } else {
                codici.clear();
                valori.clear();
                codici.add("TROPPI DATI");
                valori.add("TROPPI DATI");
                break;
              }
            }
          }
        }
      }
    } catch (Exception sqle) {
      loggerError("DominioCONN::caricaValori() - Area: "+area+" Dominio: "+Dominio+" - Errore SQL: "+queryStm+" \n "+sqle.toString(),sqle);
//      sqle.printStackTrace();
    }
    finally {
      free(dbOp);
    }

    //Debug Tempo
    stampaTempo("DominioCONN::caricaValori - Fine",area,Dominio,ptime);
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