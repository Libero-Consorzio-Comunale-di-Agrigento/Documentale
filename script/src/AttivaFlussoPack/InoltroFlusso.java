package AttivaFlussoPack;

import xmlpack.*;
//import it.finmatica.jfc.utility.*;
import java.util.*;
import java.io.*;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.parametri.Parametri;
import java.sql.*;
import it.finmatica.modulistica.inoltro.*;
import org.apache.log4j.Logger;
import it.finmatica.modulistica.connessioni.Connessione;
import it.finmatica.jfc.dbUtil.*;

public class InoltroFlusso extends Inoltro {
  private String    codRichiesta;
  private String    area;
  private String    codMod;
//  private Parametri Param;
  private InfoConnessione infoConnessione;
  private static Logger logger = Logger.getLogger("it.finmatica.modulistica.inoltro.InoltroFlusso");
  
//  private String    rept;

  // Queste costanti rappresentano le posizioni dei rispettivi campi 
  // all'interno del vettore MARCATORI_DI_CAMPO[]  
  public final static int MCA_DRIVER = 0;   
  public final static int MCA_DSN = 1;
  public final static int MCA_ALIAS = 2;
  public final static int MCA_LOGIN = 3;
  public final static int MCA_PASSWORD = 4;
  public final static int MCA_NOMEITER = 5;
  public final static int MCA_FIELDS = 6;

  public final static String MARCATORI_DI_CAMPO[] = {
     "[DRIVER]",
     "[DSN]",
     "[ALIAS]",
     "[LOGIN]",
     "[PASSWORD]",
     "[NOMEITER]",
     "[FIELDS]"
  };
  
  public final static String MARCATORE_DI_CRYPTING = "[_C_]"; 
                           
  public InoltroFlusso() {
  }

  /**
   * Metodo di inizializzazione. 
   * Vengono indicati tutti i parametri di inoltro.
   * Per questa particolare tipologia di inoltro i marcatori di campo sono stabiliti a priori.
   * Queste inizializzazioni avvengono attraverso l'invocazione del metodo 
   * <code>init(parametri, marcatoriDiCampo)</code> della classe base <code>Inoltro</code>
   * 
   * @author      
   * @version      1.0
   * @see          it.finmatica.modulistica.inoltro.Inoltro
   * @param        parametri Stringa contenente i parametri di inoltro in forma codificata.
   * @return       void
   */
  public void init(String parametri) {
    init(parametri, MARCATORI_DI_CAMPO);
    try {
      setMarcatoreDiCrypting(MARCATORE_DI_CRYPTING);
    } catch (BadCryptingMarkerException e) {
      writeLog(e, null);
    }
  }

  /**
   * Metodo per l'inoltro effettivo della mail. Viene allegato l'oggetto XML 
   * indicato in fase di creazione.
   * 
   * @author       Antonio
   * @since        1.0
   * @return       void
   */
  public boolean doInoltro(OggettoXML oggettoXML) {
    return false;
  }

  /**
   * Metodo unico per il logging.
   * Quando si individuerà una modalità standard di logging basterà modificare solo questo metodo.
   * 
   * @param        e Exception che ha generato l'errore. Può essere null.
   * @param        messaggio Un messaggio aggiuntivo.
   * @since        1.0
   * @return       void
   */
  private void writeLog(Exception e, String messaggio) {
    if (e != null) 
      e.printStackTrace();
      
    System.err.println(messaggio);
  }

  /**
   * 
   */
  public void parametriRichiesta(String idop, String cr, 
        String ar, String cm, String ctp, String utente, String allegati, InfoConnessione pm) {

    codRichiesta = cr;
    area = ar;
    codMod = cm;
    infoConnessione = pm;
  }

  /**
   * 
   */
  public boolean inoltra() {
    IDbOperationSQL dbOpSQL = null, dbOpVa = null, dbOp = null;
    ResultSet      rstSQL = null;
    ResultSet      rstVa = null;
    Hashtable       hashParams = null;
    String          p_driver, 
                    p_dsn, 
                    p_alias,  
                    p_login, 
                    p_password,
                    p_nomeiter,
                    p_campi = null,
                    p_campo = null,
                    p_valore = null; 
    StringTokenizer st;
    int             /*num, j,*/ p_progressivo;
    String query   = null;
    String queryVa = null;
    String queryLe = null;
    
    // "Tiro su" i parametri in forma umana (hashtable)
    try {

      hashParams = getHashtableParametri();

    } catch (BadInitException e1) {
      writeLog(e1, null);
      return false;
    }

    p_driver         = (String)hashParams.get(MARCATORI_DI_CAMPO[MCA_DRIVER]);
    p_dsn            = (String)hashParams.get(MARCATORI_DI_CAMPO[MCA_DSN]);
    p_alias          = (String)hashParams.get(MARCATORI_DI_CAMPO[MCA_ALIAS]);
    p_login          = (String)hashParams.get(MARCATORI_DI_CAMPO[MCA_LOGIN]);
    p_password       = (String)hashParams.get(MARCATORI_DI_CAMPO[MCA_PASSWORD]);
    p_nomeiter       = (String)hashParams.get(MARCATORI_DI_CAMPO[MCA_NOMEITER]);
    p_campi          = (String)hashParams.get(MARCATORI_DI_CAMPO[MCA_FIELDS]);
    if (p_campi == null) {
      p_campi = "";
    }

    String sDsn = getDSN();
    if (!sDsn.equalsIgnoreCase("")) {
      try {
        dbOp = SessioneDb.getInstance().createIDbOperationSQL(infoConnessione.getAlias(), infoConnessione.getDsn(), infoConnessione.getUser(), infoConnessione.getPasswd());
        Connessione cn = new Connessione((DbOperation)dbOp,sDsn);
        p_driver      = cn.getDriver();
        p_dsn         = cn.getConnessione();
        p_login       = cn.getUtente();
        p_password    = cn.getPassword();
        p_alias       = "oracle.";  //da verificare
        free(dbOp);
      } catch (Exception ex) {
        free(dbOp);
        logger.error("InaltraFlusso::inoltra - Errore: "+ex.toString(),ex);
        errorMessage = "<div id='_gdm_error_small' style='display: block'>";
        errorMessage += "<a href='#' onclick='document.getElementById(\"_gdm_error_ext\").style.display =\"block\";";
        errorMessage += "document.getElementById(\"_gdm_error_small\").style.display =\"none\";'>";
        errorMessage += ex.toString()+"</a></div>";
        errorMessage += "<div id='_gdm_error_ext' style='display: none'>";
        errorMessage += "<a href='#' onclick='document.getElementById(\"_gdm_error_small\").style.display =\"block\";";
        errorMessage += "document.getElementById(\"_gdm_error_ext\").style.display =\"none\";'>";
        errorMessage += ex.toString()+"</a>";
        StackTraceElement[] st1 = ex.getStackTrace();
        for (int i = 0;i < st1.length; i++) {
          errorMessage += "<br/>"+st1[i].toString();
        }
        errorMessage += "</div>";
        return false;
      }
    }
    String compConn = completaConnessione(p_dsn);
    ConnessioneParser cp = new ConnessioneParser();
    String connessione = cp.bindingDeiParametri(compConn);
    if (connessione == null){
      connessione = p_dsn;
    }
    

    try {
      SessioneDb.getInstance().addAlias(p_alias,p_driver);
    } catch (Exception ex) {
      errorMessage = "<div id='_gdm_error_small' style='display: block'>";
      errorMessage += "<a href='#' onclick='document.getElementById(\"_gdm_error_ext\").style.display =\"block\";";
      errorMessage += "document.getElementById(\"_gdm_error_small\").style.display =\"none\";'>";
      errorMessage += ex.toString()+"</a></div>";
      errorMessage += "<div id='_gdm_error_ext' style='display: none'>";
      errorMessage += "<a href='#' onclick='document.getElementById(\"_gdm_error_small\").style.display =\"block\";";
      errorMessage += "document.getElementById(\"_gdm_error_ext\").style.display =\"none\";'>";
      errorMessage += ex.toString()+"</a>";
      StackTraceElement[] st1 = ex.getStackTrace();
      for (int i = 0;i < st1.length; i++) {
        errorMessage += "<br/>"+st1[i].toString();
      }
      errorMessage += "</div>";
      return false;
    }

    try {
      query = "INSERT INTO RICHIESTE_OPERAZIONI "+ 
              "   (OPERAZIONE, PARAMETRI,ESEGUITA)VALUES " +
              "   ('INIT','NOMEITER="+p_nomeiter;

      
      if (p_campi.trim().length() > 0) {
        st = new StringTokenizer(p_campi,";");
        dbOpSQL = SessioneDb.getInstance().createIDbOperationSQL(infoConnessione.getAlias(), 
                                                                 infoConnessione.getDsn(), 
                                                                 infoConnessione.getUser(), 
                                                                 infoConnessione.getPasswd());
        while (st.hasMoreTokens()) {
           p_campo = st.nextToken();
           queryLe = "SELECT MAX(PROGRESSIVO) " +
                    "  FROM PRE_INOLTRO " +
                    " WHERE AREA = '"+area+"' " + 
                    "   AND CODICE_RICHIESTA = '"+codRichiesta+"' "+
                    "   AND DATO = '"+p_campo+"' ";
           dbOpSQL.setStatement(queryLe);
           dbOpSQL.execute();
           rstSQL = dbOpSQL.getRstSet();
           if (rstSQL.next()) {
              p_progressivo = rstSQL.getInt(1);
              dbOpVa = SessioneDb.getInstance().createIDbOperationSQL(infoConnessione.getAlias(), 
                                                                      infoConnessione.getDsn(), 
                                                                      infoConnessione.getUser(), 
                                                                      infoConnessione.getPasswd());
              queryVa = "SELECT VALORE " +
                     "  FROM PRE_INOLTRO " +
                     " WHERE AREA = '"+area+"' " + 
                     "   AND CODICE_RICHIESTA = '"+codRichiesta+"' "+
                     "   AND DATO = '"+p_campo+"' "+
                     "   AND PROGRESSIVO = "+p_progressivo;
    
              dbOpVa.setStatement(queryVa);
              dbOpVa.execute();
              rstVa = dbOpVa.getRstSet();
               if (rstVa.next()) {
                  BufferedInputStream bis = dbOpVa.readClob("VALORE");
                  StringBuffer sb = new StringBuffer();
                  int ic;
                  while ((ic =  bis.read()) != -1) {
                    sb.append((char)ic);
                  }
                  p_valore = sb.toString();
               } else {
                 p_valore = "";
               }
              free(dbOpVa);
              query += "\n"+p_campo+"="+p_valore.replaceAll("'","''");
          } 
        }
        free(dbOpSQL);
      } else {
        queryLe = "SELECT DATO,MAX(PROGRESSIVO) " +
                  "  FROM PRE_INOLTRO " +
                  " WHERE AREA = '"+area+"' " + 
                  "   AND CODICE_RICHIESTA = '"+codRichiesta+"' "+
                  " GROUP BY DATO";
        dbOpSQL = SessioneDb.getInstance().createIDbOperationSQL(infoConnessione.getAlias(), 
                                                                  infoConnessione.getDsn(), 
                                                                  infoConnessione.getUser(), 
                                                                  infoConnessione.getPasswd());
        dbOpSQL.setStatement(queryLe);
        dbOpSQL.execute();
        rstSQL = dbOpSQL.getRstSet();
        while (rstSQL.next()) {
           p_campo = rstSQL.getString(1);
           p_progressivo = rstSQL.getInt(2);
            dbOpVa = SessioneDb.getInstance().createIDbOperationSQL(infoConnessione.getAlias(), 
                                                                    infoConnessione.getDsn(), 
                                                                    infoConnessione.getUser(), 
                                                                    infoConnessione.getPasswd());
            queryVa = "SELECT VALORE " +
                   "  FROM PRE_INOLTRO " +
                   " WHERE AREA = '"+area+"' " + 
                   "   AND CODICE_RICHIESTA = '"+codRichiesta+"' "+
                   "   AND DATO = '"+p_campo+"' "+
                   "   AND PROGRESSIVO = "+p_progressivo;
    
            dbOpVa.setStatement(queryVa);
            dbOpVa.execute();
            rstVa = dbOpVa.getRstSet();
            if (rstVa.next()) {
               BufferedInputStream bis = dbOpVa.readClob("VALORE");
               StringBuffer sb = new StringBuffer();
               int ic;
               while ((ic =  bis.read()) != -1) {
                  sb.append((char)ic);
               }
               p_valore = sb.toString();
            } else {
               p_valore = "";
            }
            free(dbOpVa);
            query += "\n"+p_campo+"="+p_valore.replaceAll("'","''");
        }
        free(dbOpSQL);
      }
      query += "\nAREA="+area+"\nCR="+codRichiesta+"\nCM="+codMod;
      query += "\narea="+area+"\ncr="+codRichiesta+"\ncm="+codMod;
      query += "','0')";
      dbOpSQL = SessioneDb.getInstance().createIDbOperationSQL(p_alias, connessione, p_login, p_password);
      dbOpSQL.setStatement(query);
      dbOpSQL.execute();
      dbOpSQL.commit();
      free(dbOpSQL);
    } catch (Exception ex) {
      logger.error("InaltraFlusso::inoltra - Errore: "+ex.toString(),ex);
      errorMessage = "<div id='_gdm_error_small' style='display: block'>";
      errorMessage += "<a href='#' onclick='document.getElementById(\"_gdm_error_ext\").style.display =\"block\";";
      errorMessage += "document.getElementById(\"_gdm_error_small\").style.display =\"none\";'>";
      errorMessage += ex.toString()+"</a></div>";
      errorMessage += "<div id='_gdm_error_ext' style='display: none'>";
      errorMessage += "<a href='#' onclick='document.getElementById(\"_gdm_error_small\").style.display =\"block\";";
      errorMessage += "document.getElementById(\"_gdm_error_ext\").style.display =\"none\";'>";
      errorMessage += ex.toString()+"</a>";
      StackTraceElement[] st1 = ex.getStackTrace();
      for (int i = 0;i < st1.length; i++) {
        errorMessage += "<br/>"+st1[i].toString();
      }
      errorMessage += "</div>";
      free(dbOpSQL);
      free(dbOpVa);
      return false;
    }
    return true;
  }

  /**
   *
   */
  private void free(IDbOperationSQL dbOp) {
    try {
      dbOp.getStmSql().clearParameters();
      dbOp.close();
    } catch (Exception e) { }
  }

  /**
   * 
   */
   protected String completaConnessione(String connessione){
     String connessioneParam = connessione;
     String pCodice = null;
     String retval = null;

     int h = 0;
     int s = 0;

     h = connessioneParam.indexOf(":HOST_INOLTRO");
     if (h > -1) {
       pCodice = connessioneParam.substring(h+1,h+15);
       retval = Parametri.getParametriDomini(pCodice);
       connessioneParam = connessioneParam.replaceAll(":"+pCodice,retval);
     }
     s = connessioneParam.indexOf(":SID_INOLTRO");
     if (s > -1) {
       pCodice = connessioneParam.substring(s+1,s+14);
       retval = Parametri.getParametriDomini(pCodice);
       connessioneParam = connessioneParam.replaceAll(":"+pCodice,retval);
     }
     
     return connessioneParam;
     
   }
}