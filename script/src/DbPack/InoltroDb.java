package DbPack;
import xmlpack.*;
//import it.finmatica.jfc.authentication.*;
//import it.finmatica.jfc.utility.*;
//import java.io.ByteArrayInputStream;
import java.util.*;
import java.io.*;
import it.finmatica.modulistica.connessioni.Connessione;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.parametri.Parametri;
import java.sql.*;
import org.apache.log4j.Logger;

import it.finmatica.modulistica.inoltro.*;
import it.finmatica.jfc.dbUtil.*;

/**
 * Classe per l'inoltro dei dati ad un altro Db.
 * Sintassi di codifica dei parametri:<br>
 * <br>
 * <code>
 * parametri = "[DRIVER]<i>driver</i>[DSN]<i>dsn</i>[ALIAS]<i>alias</i>[LOGIN]<i>utente</i>[C_PASSWORD]<i>crypted password</i>"<br>
 * <br>
 * </code>
 * dove [DRIVER], [DSN], ... , [ALIAS], ... sono i marcatori di campo.<br>
 * L'ordine di definizione non è determinante ed i marcatori non sono <i>case sensitive</i>.<br><br>
 * <u>Descrizione dei parametri</u><br>
 * <b>[DRIVER]</b>  <br>
 * <b>[DSN]</b>  <br>
 * <b>[ALIAS]</b>              <br>
 * <b>[LOGIN]</b>              <br>
 * <b>[PASSWORD]</b> Password in chiaro. Se è specificata anche la password criptata, questo campo 
 *                   non verrà preso in considerazione e verrà utilizzata la password criptata.<br>
 * <b>[C_PASSWORD]</b> Password criptata. Si suppone che l'algoritmo di criptazione è quello 
 *                     utilizzato dalla classe <code>it.finmatica.jfc.authentication.Cryptable</code><br>
 * 
 * @author       
 * @author       
 * @version      1.0
 *              
 * @see          it.finmatica.modulistica.inoltro.Inoltro
 */

public class InoltroDb extends Inoltro {
//  private DateUtility du;            

  private String    codRichiesta;
  private String    area;
//  private String    codTipoPratica;
  private String    codModello;
//  private String    revModello;
  private String    idOp;
//  private String    pUtente;
//  private String    pAllegati;
//  private String    rept;
//  private Parametri Param;
  private InfoConnessione infoConnessione;
  private static Logger logger = Logger.getLogger("it.finmatica.modulistica.inoltro.InoltroDb");

  // Queste costanti rappresentano le posizioni dei rispettivi campi 
  // all'interno del vettore MARCATORI_DI_CAMPO[]  
  public final static int MCA_DRIVER = 0;   
  public final static int MCA_DSN = 1;
  public final static int MCA_ALIAS = 2;
  public final static int MCA_LOGIN = 3;
  public final static int MCA_PASSWORD = 4;
 

  public final static String MARCATORI_DI_CAMPO[] = {
     "[DRIVER]",
     "[DSN]",
     "[ALIAS]",
     "[LOGIN]",
     "[PASSWORD]"
  };
  
  public final static String MARCATORE_DI_CRYPTING = "[_C_]"; 
                           
  /**
   * Costruttore vuoto necessario per invocare la Class.forName()
   * in tutte le classi che intendono allocare a runtime questo oggetto 
   * di inoltro ma che conoscono la sola interfaccia Inoltro.
   */
  public InoltroDb() {
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

    idOp = idop;
    codRichiesta = cr;
    area = ar;
//    codTipoPratica = ctp;
//    Param = pm;
    infoConnessione = pm;
    codModello = cm;
//    pAllegati = allegati;
//    pUtente = utente;
  }

  /**
   * 
   */
  public boolean inoltra() {
    Hashtable       hashParams = null;
    String          p_driver, 
                    p_dsn, 
                    p_alias,  
                    p_login, 
                    p_password; 
//    StringTokenizer st;
//    int             num, j;

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

    String sDsn = getDSN();
    if (!sDsn.equalsIgnoreCase("")) {
      IDbOperationSQL dbOp = null;
      try {
        dbOp = SessioneDb.getInstance().createIDbOperationSQL(infoConnessione.getAlias(), 
                                                              infoConnessione.getDsn(), 
                                                              infoConnessione.getUser(), 
                                                              infoConnessione.getPasswd());
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
        StackTraceElement[] st = ex.getStackTrace();
        for (int i = 0;i < st.length; i++) {
          errorMessage += "<br/>"+st[i].toString();
        }
        errorMessage += "</div>";
        return false;
      }
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
      StackTraceElement[] st = ex.getStackTrace();
      for (int i = 0;i < st.length; i++) {
        errorMessage += "<br/>"+st[i].toString();
      }
      errorMessage += "</div>";
      return false;
    }

    String compConn = completaConnessione(p_dsn);
    ConnessioneParser cp = new ConnessioneParser();
    String connessione = cp.bindingDeiParametri(compConn);
    if (connessione == null){
      connessione = p_dsn;
    }
    
//    if (!caricaDati(idOp,codRichiesta,area)) {
//      return false;
//    }

    if (!eseguiProcedura(idOp,p_alias,connessione,p_login,p_password)) {
      return false;
    }
    
//    if (!cancellaDati(idOp,codRichiesta,area)) {
//      return false;
//    }

    return true;
  }
  
  /**
   * 
   */
  private boolean eseguiProcedura(String idOp,String aliasDB, String dsnDB, String utenteDB, String passwdDB) 
  {
    IDbOperationSQL dbOpIn = null,
                   dbOpVa = null,
                   dbOpEx = null,
                   dbOpLo = null;
    ResultSet      rstIn = null,
                   rstVa = null/*,
                   rstEx = null*/;
    String         paramSql = null,
                   istruzioneSql = null,
                   tipo = null;
    String         par = null;
    String         dato = null;
    String         val = null;
    String         cm = null;
    int            id_proc = 0, id_proc_ex = 0;
    String query = "SELECT DECODE(NVL(STATO,0),0,NVL(ID_PROCEDURA,0),0) ID_PROC"+
                   "  FROM LOG_INOLTRI "+
                   " WHERE ID_OP = :ID_OP" +
                   "   AND CODICE_RICHIESTA = :CR " +
                   "   AND AREA = :AREA ";
    try {
      dbOpIn = SessioneDb.getInstance().createIDbOperationSQL(infoConnessione.getAlias(), 
          infoConnessione.getDsn(), 
          infoConnessione.getUser(), 
          infoConnessione.getPasswd());
      dbOpIn.setStatement(query);
      dbOpIn.setParameter(":ID_OP",idOp);
      dbOpIn.setParameter(":CR",codRichiesta);
      dbOpIn.setParameter(":AREA",area);
      dbOpIn.execute();
      rstIn = dbOpIn.getRstSet();
      if (rstIn.next()) {
        id_proc = rstIn.getInt("ID_PROC");
      }

      query = "SELECT ISTRUZIONE_SQL, PARAMETRI , TIPO, ID_PROCEDURA"+
                   "  FROM AZIONI_INOLTRO "+
                   " WHERE ID_OP = :ID_OP " +
//                   "   AND ID_PROCEDURA > :ID_PROC " +
                   " ORDER BY ID_PROCEDURA ASC ";
      dbOpIn.setStatement(query);
      dbOpIn.setParameter(":ID_OP",idOp);
      dbOpIn.setParameter(":ID_PROC",id_proc);
      dbOpIn.execute();
      rstIn = dbOpIn.getRstSet();
      while (rstIn.next()) {
        paramSql = rstIn.getString("PARAMETRI");
        tipo = rstIn.getString("TIPO");
        id_proc_ex = rstIn.getInt("ID_PROCEDURA");

        BufferedInputStream bis = dbOpIn.readClob("ISTRUZIONE_SQL");
        StringBuffer sb = new StringBuffer();
        int ic;
        while ((ic =  bis.read()) != -1) {
          sb.append((char)ic);
        }
        istruzioneSql = sb.toString();
        istruzioneSql = istruzioneSql.replaceAll("\r"," ");
        istruzioneSql = istruzioneSql.replaceAll("\n"," ");

        if (paramSql == null) {
          paramSql = "";
        }
        if (paramSql.equalsIgnoreCase("")) {
          InoltroDbParser iPar = new InoltroDbParser(idOp,area,codModello,codRichiesta,infoConnessione);
          istruzioneSql = iPar.bindingDeiParametri(istruzioneSql);
        }
        dbOpEx =SessioneDb.getInstance().createIDbOperationSQL(aliasDB, dsnDB, utenteDB, passwdDB);

        if (tipo.equalsIgnoreCase("I")) {
          dbOpEx.setStatement(istruzioneSql);
        } else {
          if (tipo.equalsIgnoreCase("P")) {
            dbOpEx.setCallFunc(istruzioneSql);
          } else {
            //Il tipo di operazione non è conosciuto
            free(dbOpEx);
            free(dbOpIn);
            return false;
          }
        }

        StringTokenizer st = new StringTokenizer(paramSql,"@");
        while (st.hasMoreTokens()) {
          par = st.nextToken();
          dato = st.nextToken();
          cm = st.nextToken();
          dbOpVa = SessioneDb.getInstance().createIDbOperationSQL(infoConnessione.getAlias(), 
              infoConnessione.getDsn(), 
              infoConnessione.getUser(), 
              infoConnessione.getPasswd());
          if (cm.equalsIgnoreCase("%")) {
            query = "SELECT VALORE"+
                     "  FROM PRE_INOLTRO "+
                     " WHERE ID_OP = :ID_OP" +
                     "   AND CODICE_RICHIESTA = :CODICE_RICHIESTA" +
                     "   AND AREA = :AREA" +
                     "   AND DATO = :DATO";
            dbOpVa.setStatement(query);
            
          } else {
            query = "SELECT VALORE"+
                     "  FROM PRE_INOLTRO "+
                     " WHERE ID_OP = :ID_OP" +
                     "   AND CODICE_RICHIESTA = :CODICE_RICHIESTA" +
                     "   AND AREA = :AREA" +
                     "   AND DATO = :DATO" +
                     "   AND CODICE_MODELLO = :CM";
            dbOpVa.setStatement(query);
            dbOpVa.setParameter(":CM",cm);
          }
          dbOpVa.setParameter(":ID_OP",idOp);
          dbOpVa.setParameter(":CODICE_RICHIESTA",codRichiesta);
          dbOpVa.setParameter(":AREA",area);
          dbOpVa.setParameter(":DATO",dato);
          dbOpVa.execute();
          rstVa = dbOpVa.getRstSet();
          if (!rstVa.next()) {
//            return false;
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
          
          free(dbOpVa);
          par = ":"+par;

          dbOpEx.setParameter(par,val);
        }

        dbOpEx.execute();
        if (tipo.equalsIgnoreCase("I")) {
          dbOpEx.commit();
        } else {
          if (tipo.equalsIgnoreCase("P")) {
            //presumo che le function ritornino un integer
//            if (dbOpEx.getCallSql().getInt(1) == 0) {
//              dbOpEx.close();
//              dbOpIn.close();
//              return false;
//            }
          } 
        }
        
        free(dbOpEx);

        StringTokenizer st2 = new StringTokenizer(paramSql,"@");
        while (st2.hasMoreTokens()) {
          par = st2.nextToken();
          dato = st2.nextToken();
          cm = st2.nextToken();

          dbOpVa = SessioneDb.getInstance().createIDbOperationSQL(infoConnessione.getAlias(), 
              infoConnessione.getDsn(), 
              infoConnessione.getUser(), 
              infoConnessione.getPasswd());
          if (cm.equalsIgnoreCase("%")) {
            query = " UPDATE PRE_INOLTRO "+
                    " SET ELABORATO = 'S' " +
                    " WHERE ID_OP = :ID_OP" +
                    "   AND CODICE_RICHIESTA = :CODICE_RICHIESTA" +
                    "   AND AREA = :AREA" +
                    "   AND DATO = :DATO";
            dbOpVa.setStatement(query);
          } else {
            query = " UPDATE PRE_INOLTRO "+
                    " SET ELABORATO = 'S' " +
                    " WHERE ID_OP = :ID_OP" +
                    "   AND CODICE_RICHIESTA = :CODICE_RICHIESTA" +
                    "   AND AREA = :AREA" +
                    "   AND DATO = :DATO" +
                    "   AND CODICE_MODELLO = :CM";
            dbOpVa.setStatement(query);
            dbOpVa.setParameter(":CM",cm);
          }
          dbOpVa.setParameter(":ID_OP",idOp);
          dbOpVa.setParameter(":CODICE_RICHIESTA",codRichiesta);
          dbOpVa.setParameter(":AREA",area);
          dbOpVa.setParameter(":DATO",dato);
          dbOpVa.execute();
          dbOpVa.commit();
          free(dbOpVa);
        }

          query = " UPDATE LOG_INOLTRI "+
                  " SET ID_PROCEDURA = :ID_PROC " +
                  " WHERE ID_OP = :ID_OP" +
                  "   AND CODICE_RICHIESTA = :CODICE_RICHIESTA" +
                  "   AND AREA = :AREA";
          dbOpLo = SessioneDb.getInstance().createIDbOperationSQL(infoConnessione.getAlias(), 
              infoConnessione.getDsn(), 
              infoConnessione.getUser(), 
              infoConnessione.getPasswd());
          dbOpLo.setStatement(query);
          dbOpLo.setParameter(":ID_PROC",id_proc_ex);
          dbOpLo.setParameter(":ID_OP",idOp);
          dbOpLo.setParameter(":CODICE_RICHIESTA",codRichiesta);
          dbOpLo.setParameter(":AREA",area);
          dbOpLo.execute();
          dbOpLo.commit();
          free(dbOpLo);
      }
      free(dbOpIn);
    } catch (Exception ex) {
      free(dbOpIn);
      free(dbOpEx);
      free(dbOpVa);
      free(dbOpLo);
      logger.error("InoltroDb::eseguiProcedura() - Errore :"+ex.toString(),ex);
      errorMessage = "<div id='_gdm_error_small' style='display: block'>";
      errorMessage += "<a href='#' onclick='document.getElementById(\"_gdm_error_ext\").style.display =\"block\";";
      errorMessage += "document.getElementById(\"_gdm_error_small\").style.display =\"none\";'>";
      errorMessage += ex.toString()+"</a></div>";
      errorMessage += "<div id='_gdm_error_ext' style='display: none'>";
      errorMessage += "<a href='#' onclick='document.getElementById(\"_gdm_error_small\").style.display =\"block\";";
      errorMessage += "document.getElementById(\"_gdm_error_ext\").style.display =\"none\";'>";
      errorMessage += ex.toString()+"</a>";
      StackTraceElement[] st = ex.getStackTrace();
      for (int i = 0;i < st.length; i++) {
        errorMessage += "<br/>"+st[i].toString();
      }
      errorMessage += "</div>";
      return false;
    }
    return true;
  }

  /**
   * 
   */
 /* private boolean caricaDati(String id, String cr, String area) 
  {
    if (!cancellaDati(id,cr,area)) {
      return false;
    }
    IDbOperationSQL dbOpIn = null;
    String queryIn = "INSERT INTO PRE_INOLTRO "+
                   " (ID_OP, CODICE_RICHIESTA, CODICE_MODELLO, "+
                   " AREA, PROGRESSIVO, DATO, VALORE) "+
                   " (SELECT :ID_OP, CODICE_RICHIESTA, CODICE_MODELLO, "+
                   " AREA, PROGRESSIVO, DATO, VALORE "+
                   "  FROM REPOSITORYTEMP "+
                   " WHERE CODICE_RICHIESTA = :CODICE_RICHIESTA AND " +
                   " AREA = :AREA )";
    try {
      dbOpIn = SessioneDb.getInstance().createIDbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
      dbOpIn.setStatement(queryIn);
      dbOpIn.setParameter(":ID_OP",idOp);
      dbOpIn.setParameter(":CODICE_RICHIESTA",codRichiesta);
      dbOpIn.setParameter(":AREA",area);
      dbOpIn.execute();
      dbOpIn.commit();
      free(dbOpIn);
    } catch (Exception ex) {
      free(dbOpIn);
      logger.error("InoltroDb::caricaDati() - Errore :"+ex.toString(),ex);
      return false;
    }
    return true;
  }*/

  /**
   * 
   */
/*  private boolean cancellaDati(String id, String cr, String area) 
  {
    IDbOperationSQL dbOpIn = null;
    String queryIn = "DELETE PRE_INOLTRO "+
                   " WHERE CODICE_RICHIESTA = :CODICE_RICHIESTA " +
                   "   AND AREA = :AREA " +
                   "   AND ID_OP = :ID_OP ";
                   
    try {
      dbOpIn = SessioneDb.getInstance().createIDbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
      dbOpIn.setStatement(queryIn);
      dbOpIn.setParameter(":ID_OP",idOp);
      dbOpIn.setParameter(":CODICE_RICHIESTA",codRichiesta);
      dbOpIn.setParameter(":AREA",area);
      dbOpIn.execute();
      dbOpIn.commit();
      free(dbOpIn);
    } catch (Exception ex) {
      free(dbOpIn);
      logger.error("InoltroDb::cancellaDati() - Errore :"+ex.toString(),ex);
      return false;
    }
    return true;
  }*/

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
} // Class InoltroMail