package it.finmatica.modulistica.domini;

import java.util.Properties;
import it.finmatica.textparser.AbstractParser;
import java.sql.*; 
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import org.apache.log4j.Logger;
import it.finmatica.modulistica.connessioni.ConnessioneParser;
//import it.finmatica.modulistica.modulisticapack.ConnessioneParser;
//import it.finmatica.modulistica.modulisticapack.Dominio;

/**
 * 
 */
public class DominioCONN extends Dominio {
  private static    Logger      logger = Logger.getLogger(DominioCONN.class);
  private           Connection  pConn;
  
  public DominioCONN(Connection pConn, String pArea, String pDominio, String pTipo, String pOrdinamento, String pDriver, String pConnessione,
                     String pUser, String pPasswd, String pIstruzione, AbstractParser pAbPar) throws Exception {
    super(pConn, pArea, pDominio, pTipo, pOrdinamento);
//    Parametri.leggiParametriStandard(pConn);
    
    this.pConn = pConn;
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
      user        = pUser;
      passwd      = pPasswd;
    }
    istruzione  = pIstruzione;
    
    caricaValori(pAbPar);
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
  * @author Marco Bonforte
  * @param mp AbstractParser.
  */
  protected void caricaValori(AbstractParser mp) {
//    Statement           statement = null;
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
      Properties pmp = new Properties();
      pmp.setProperty("TIPO","S");
      mp.setExtraKeys(pmp);
      queryStm = mp.bindingDeiParametri(istruzione);
      if (queryStm == null){
        // Ritorno senza riempire alcun valore poichè si è
        // verificato un errore durante il BINDING dei parametri
        // probabilmente un parametro mancante.
        logger.error("DominioCONN::caricaValori() - Area: "+area+" Dominio: "+Dominio+" Attenzione! Parametro mancante su "+istruzione);
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
               if (nCaratteri < 32000) {
                 caricaOrdinata(rstcodice,rstvalore,ordinamento);
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
              if (nCaratteri < 32000) {
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
      loggerError("DominioCONN::caricaValori() - Area: "+area+" Dominio: "+Dominio+" - Errore SQL: \n"+queryStm+"\n"+sqle.toString(),sqle);
//      sqle.printStackTrace();

    }
    finally {
      free(dbOp);
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