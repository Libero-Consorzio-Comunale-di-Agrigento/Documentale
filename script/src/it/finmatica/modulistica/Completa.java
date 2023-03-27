package it.finmatica.modulistica;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.parametri.Parametri;
import java.sql.*;
import it.finmatica.dmServer.management.*;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.util.JNDIParameter;
import it.finmatica.dmServer.Environment;
import org.apache.log4j.Logger;

public class Completa {
  private String      inifile = null;
  private Environment vu;
  private String      ruolo = null;
  private String      serverScheme;
  private String      serverName;
  private String      serverPort;
  private String      corpoHtml = "";
  private String      id_tipodoc = null;
  private String      noBody = Global.NOBODY;
  private static Logger     logger = Logger.getLogger(Completa.class);

  /**
   * Oggetto per settare a completo lo stato di un documento
   * @param sPath Path reale della Servlet 
   */
  public Completa(String sPath) {
    init(sPath);
  }

  private void init(String sPath) {
     try {
      String separa = File.separator;
      inifile = sPath + separa + "config" + separa + "gd4dm.properties";
      File f = new File(inifile);
      if (!f.exists()) {
        inifile = sPath + separa + ".." + separa + "jgdm" + separa + "config" + separa + "gd4dm.properties";
      }
//      FileInputStream fis = new FileInputStream(inifile);
//      confLogger = new Properties();
//      confLogger.load(fis);
//      String esiste =confLogger.getProperty("log4j.logger.it.finmatica.modulistica");
//      if (esiste == null) {
//        esiste = "";
//      }
//      if (esiste == "") {
//        confLogger.setProperty("log4j.logger.it.finmatica.modulistica","ERROR, S");
//        confLogger.setProperty("log4j.appender.S","org.apache.log4j.RollingFileAppender");
//        confLogger.setProperty("log4j.appender.S.File","${catalina.home}/logs/jgdm.log");
//        confLogger.setProperty("log4j.appender.S.MaxFileSize","10MB");
//        confLogger.setProperty("log4j.appender.S.MaxBackupIndex","10");
//        confLogger.setProperty("log4j.appender.S.layout","org.apache.log4j.PatternLayout");
//        confLogger.setProperty("log4j.appender.S.layout.ConversionPattern","%d{HH:mm:ss} [%p] %c: %m%n");
//      }
//      PropertyConfigurator.configure(confLogger);

      // Lettura parametri da file ini
      Parametri.leggiParametriStandard(inifile);

      // Creazione alias
      SessioneDb.getInstance().addAlias(Parametri.ALIAS, Parametri.SPORTELLO_DRIVER);
    } catch(Exception e) {
      loggerError("Upload::init() - Attenzione! si è verificato un errore: "+e.toString(),e);
    }
  }

  /**
   *  Funziona che ritorna il codice HTML del modello aperto in lettura
   */
  public String getValue() {
    return corpoHtml;
  }

  /**
   *  Funzione che genera il codice HTML del modello e setta lo stato
   *  del documento a completo.
   *  @param request Request della pagina che incorpora l'albero
   *
   */
  public void genera(HttpServletRequest request, String pdo) {
    HttpSession     session = request.getSession();
    String          cr      = request.getParameter("cr"),
                    ar      = request.getParameter("area"),
                    cm      = request.getParameter("cm"),
                    us      = (String)session.getAttribute("UtenteGDM");
    String          iddoc = null;


    if (Parametri.PROTOCOLLO.length() == 0) {
      serverScheme = request.getScheme();
    } else {
      serverScheme = Parametri.PROTOCOLLO;
    }
    if (Parametri.SERVERNAME.length() == 0) {
      serverName = request.getServerName();
    } else {
      serverName = Parametri.SERVERNAME;
    }
    if (Parametri.SERVERPORT.length() == 0) {
      serverPort = ""+request.getServerPort();
    } else {
      serverPort = Parametri.SERVERPORT;
    }

    String returl = serverScheme+"://"+
                 serverName+":"+
                 serverPort+
                 request.getContextPath();
    if (pdo.equalsIgnoreCase("CC")) {
      returl += "/restrict/ServletModulistica.do?cr="+cr+"&area="+ar+
                 "&cm="+cm+"&rw=R";
    } else {
      returl += "/ServletModulistica?cr="+cr+"&area="+ar+
                 "&cm="+cm+"&rw=R";
    }

    try {
      initVu(ar,cm,cr,us);
      vu.connect();
      vu.setDbOpRestaConnessa(true); //Questo evita che ogni volta che passo la vu alle classi del dmserver, es. AggiornaDocumento
      //la classe rifaccia connect e/o disconnect.... a quello ci penso io
    } catch (Exception e) {
      corpoHtml += "<span class='AFCErrorDataTD'>"+"Attenzione! Errore in fase connessione. Errore:  ["+ e.toString()+"].</span>";
      loggerError("ServletCompleta::genera() Errore in connessione enviroment - Errore:  ["+ e.toString()+"]",e);
      return;
    }


    try {
      id_tipodoc = ricavaIdtipodoc(ar,cm);
      iddoc = ricercaIdDocumento(ar, cr, cm);
      if (!iddoc.equalsIgnoreCase("-1") ) {
        AggiornaDocumento ad = new AggiornaDocumento(iddoc, vu);
        ad.settaCodiceRichiesta(cr);
        ad.salvaDocumentoCompleto();
      }
    } catch (Exception e) {
       try {vu.getDbOp().rollback();} catch (Exception ei) { }
       loggerError("Completa::genera() - "+e.toString(),e);
    }
    finally {
      //Chiusura della connessione
      try {
        vu.setDbOpRestaConnessa(false);
        vu.disconnectCommit();
      }
      catch (Exception e) {
      }
    }

    if (!iddoc.equalsIgnoreCase("-1") ) {
      corpoHtml = "<html>";
      corpoHtml += "<head><title>ServletVisualizza</title>";
      corpoHtml += "<meta http-equiv='refresh' content='0; URL="+returl+"'>";
      corpoHtml += "</head><body>";
      corpoHtml += "</body></html>";
    } else {
      corpoHtml = "<html>";
      corpoHtml += "<head><title>ServletCompleta</title></head>";
      corpoHtml += "<body>";
      corpoHtml += "<p>Documento non trovato!</p>";
      corpoHtml += "</body></html>";
    }


  }

  /**
   * 
   */
  private void initVu(String p_area,String p_cm, String p_richiesta,String p_user) {
    IDbOperationSQL  dbOp = null;
    ResultSet       rst = null;
    String          query;

    //RICAVO IL RUOLO DELL'UTENTE
    query = "SELECT D.RUOLO"+
            " FROM AD4_DIRITTI_ACCESSO D,"+
            "      AD4_MODULI M"+
            " WHERE M.MODULO = D.MODULO"+
            "   AND M.PROGETTO = :PROGETTO"+
            "   AND D.ISTANZA = :ISTANZA"+
            "   AND UTENTE = :UTENTE";
    try {
      /*dbOp =SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);

      dbOp.setStatement(query);
      dbOp.setParameter(":PROGETTO",Parametri.PROGETTO);
      dbOp.setParameter(":ISTANZA",Parametri.ISTANZA);
      dbOp.setParameter(":UTENTE",p_user);
      dbOp.execute();
      rst = dbOp.getRstSet();

      if (rst.next() ) {
         ruolo = rst.getString(1);
      }*/
      ruolo="GDM";
      //free(dbOp);
      vu = new Environment(p_user, null, "MODULISTICA", "ADS", null, (new JNDIParameter("jdbc/gdm")));
      vu.setRuolo(ruolo);

    } catch (Exception e) {
      //free(dbOp);
      loggerError("ServletCompleta::initVu - "+e.toString(),e);
    }
  }

  /**
   *
   */
  private void free(IDbOperationSQL dbOp) {
    try {
      dbOp.close();
    } catch (Exception e) { }
  }

  /**
   * 
   */
  private String ricercaIdDocumento (String ar, String cr, String cm) {
    Vector      ll;
    String      iddoc = "-1";

    try {
      RicercaDocumento rd = new RicercaDocumento(id_tipodoc,vu);
      rd.settaCodiceRichiesta(cr);
      rd.settaFileName(noBody);
      ll = rd.ricercaBozza();
      if(ll.size() == 1) {
        iddoc = (String)ll.firstElement();
      } else {
        if (ll.size() > 0) {
          logger.error("ServletCompleta::ricercaIdDocumento() - Attenzione:  Numero documenti trovati "+ ll.size() );
          return "-1";
        }
      }
    }
      catch (Exception e) {
      loggerError("ServletCompleta::ricercaIdDocumento() - Errore:  ["+ e.toString()+"]",e);
    }
    return iddoc;
  }

  /**
   * 
   */
  private String ricavaIdtipodoc (String ar, String cm) {
    IDbOperationSQL  dbOp = null;
    ResultSet       rst = null;
    String          query;
    String          idtipodoc = null;
    String          codmod = null;

    query = "SELECT ID_TIPODOC, CODICE_MODELLO_PADRE"+
            " FROM MODELLI"+
            " WHERE AREA = :AREA"+
            "   AND CODICE_MODELLO = :CM";
    try {
      dbOp = vu.getDbOp();

      dbOp.setStatement(query);
      dbOp.setParameter(":AREA",ar);
      dbOp.setParameter(":CM",cm);
      dbOp.execute();
      rst = dbOp.getRstSet();

      if (rst.next() ) {
         idtipodoc = ""+rst.getInt("ID_TIPODOC");
         codmod = rst.getString("CODICE_MODELLO_PADRE");
      } else {
        return "";
      }

      if (idtipodoc == null) {
        idtipodoc = "";
      }

      if (idtipodoc.length() == 0) {
        dbOp.setParameter(":AREA",ar);
        dbOp.setParameter(":CM",codmod);
        dbOp.execute();
        rst = dbOp.getRstSet();

        if (rst.next() ) {
           idtipodoc = rst.getString("ID_TIPODOC");
        }
      }
      return idtipodoc;
    
    } catch (Exception e) {
      loggerError("ServletCompleta::ricavaIdtipodoc - "+e.toString(),e);
      return "";
    }
  }

  /**
   * 
   */
  protected void freeConn() {
//    try {
//      SessioneDb.getInstance().closeFreeConnection();
//    } catch (Exception e) {
//      loggerError("ServletEditing::freeConn() - Attenzione! Errore in fase di rilascio connnessioni: "+e.toString(),e);
//      corpoHtml += "<H2>Attenzione! Errore in fase di rilascio connnessioni.</h2>";
//      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
//        corpoHtml += e.toString();
//      }
//      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
//        corpoHtml += e.getStackTrace().toString();
//      }
//    }
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