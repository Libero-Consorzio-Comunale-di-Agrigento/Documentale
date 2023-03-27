package it.finmatica.modulistica.modulisticapack;

import java.util.*;
import java.sql.*; 
import javax.servlet.http.*;
import it.finmatica.modulistica.parametri.Parametri;
//import it.finmatica.jfc.dbUtil.*;
import org.apache.log4j.Logger;
 
/**
 * 
 */
public class DominioODBC extends Dominio {
  Connection  connessioneODBC;
  private static    Logger      logger = Logger.getLogger(DominioODBC.class);
  
  public DominioODBC(String pArea, String pDominio, String pCm, String pTipo, String pOrdinamento, String pDriver, String pConnessione,
                     String pUser, String pPasswd, String pIstruzione, HttpServletRequest pRequest) throws Exception {
    super(pArea, pDominio, pCm, pTipo, pOrdinamento);
    
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

  
  /**
   * 
   */
  protected Connection apriConnessioneDb() {
    Connection  conn;

    try {
      Class.forName(driver).newInstance();
      conn = DriverManager.getConnection(connessione, user, passwd);
    } catch(Exception e) {
      conn = null;
    }
    return conn;
  }
  
  /**
  * Ridefinita per consentire l'uso della connessioneODBC.
  * Suppongo che la SELECT di questo tipo di dominio ritorni una stringa nella forma 
  * standard, che � la stessa richiesta dall'OUTPUT NOTES.
  */
  protected void caricaValori(HttpServletRequest pRequest) {
    //Debug Tempo
    long ptime = stampaTempo("DominioODBC::caricaValori - Inizio",area,Dominio,0);
    //Debug Tempo
    Statement           statement = null;
    ResultSet           resultQuery = null;
    String              queryStm, strStream;
    ResultSetMetaData   rsMeta;          
    String        rstcodice = null;
    String        rstvalore = null;
    int           nCaratteri = 0;

    connessioneODBC = apriConnessioneDb();
    
    try {
      ModulisticaParser mp = new ModulisticaParser(pRequest);
      Properties pmp = new Properties();
      pmp.setProperty("TIPO","S");
      mp.setExtraKeys(pmp);
      queryStm = mp.bindingDeiParametri(istruzione);
//      queryStm = bindingDinamico(pRequest, istruzione, false);
      if (queryStm == null){
        // Ritorno senza riempire alcun valore poich� si �
        // verificato un errore durante il BINDING dei parametri
        // probabilmente un parametro mancante.
        logger.error("DOMINIOODBC - Parametro mancante su "+istruzione);
        //Debug Tempo
        stampaTempo("DominioNotes::caricaValori - Fine",area,Dominio,ptime);
        //Debug Tempo
        return;  // *** exit point ***
      }

      statement = connessioneODBC.createStatement();
    	resultQuery = statement.executeQuery(queryStm);
      resultQuery.next();
      rsMeta = resultQuery.getMetaData();
      if (rsMeta.getColumnCount() == 1){
        // Prendo lo stream tornato e lo interpreto
        strStream = resultQuery.getString(1);
        scriviValoriDaStream(strStream);
      } else {
        // Sono gi� sul primo record li esamino tutti suponendo
        // che siano nella forma CODICE, VALORE
        do {
           rstcodice = resultQuery.getString("CODICE");
           rstvalore = resultQuery.getString("VALORE");
           nCaratteri = nCaratteri + rstcodice.length() + rstvalore.length();
           if (nCaratteri < MAXLEN_VALORI) {
             caricaOrdinata(rstcodice,rstvalore,ordinamento);
//             codici.add(rstcodice); // CODICE
//             valori.add(rstvalore); // VALORE
           } else {
             codici.clear();
             valori.clear();
             codici.add("TROPPI DATI");
             valori.add("TROPPI DATI");
             break;
          }
        } while (resultQuery.next());
      }
    } catch (Exception sqle) {
      logger.error("DOMINIOODBC - Errore SQL: "+sqle.getMessage());
//      sqle.printStackTrace();
      //throw new Exception("Errore nella ricerca dei valore sul database.");
    } finally {
      try {
        resultQuery.close();       
      } catch (Exception ex) {
        ex.printStackTrace();
      } 
      
      try {
        statement.close();        
      } catch (Exception ex) {
        ex.printStackTrace();
      }
       
      try {        
        connessioneODBC.close();
      } catch (Exception ex) {
        ex.printStackTrace();
      }
      
    }
    //Debug Tempo
    stampaTempo("DominioNotes::caricaValori - Fine",area,Dominio,ptime);
    //Debug Tempo
  }
}