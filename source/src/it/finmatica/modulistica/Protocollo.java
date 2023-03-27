package it.finmatica.modulistica;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.parametri.Parametri;
import java.sql.*;
import HTML.Template;
import org.apache.log4j.Logger;

public class Protocollo {
  private String      req_ctp;  
  private String      inifile = null;
  private String      completeContextURL = null;
  private String      filesep = File.separator;
  private String      serverScheme;
  private String      serverName;
  private String      serverPort;
  private String      corpo = null;
  private String      p_do = "";
  private static Properties confLogger = null;
  private static Logger     logger = Logger.getLogger(Protocollo.class);

  public Protocollo(String sPath) {
    init(sPath);
  }

  public void init(String sPath) {
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
      logger.error("Upload::init() - Attenzione! si � verificato un errore: "+e.toString());
    }
  }

  public void genera(HttpServletRequest request, String pdo) {
    IDbOperationSQL    dbOpSQLProt = null;
    ResultSet         rstProt;
    String            queryProt = null,
                      pOggetto = null,
                      pTipoDoc = null,
                      pMovimento = null,
                      pClassificazione = null,
                      pUnitaProt = null,
                      pUtente = null,
                      pApplicativo = null,
                      pDocPrinc = null,
                      pSmistamento = null,
                      pMittente = null,
                      pAzione = null;
    String [] paths = {request.getSession().getServletContext().getRealPath("")+filesep+"template", "."}; 
    String fileName = "ServletProtocollo.tmpl";

    req_ctp         = request.getParameter("ctp");
//    req_area        = request.getParameter("area");             // area di competenza: mandatory
//    req_cr          = request.getParameter("cr");               // codice richiesta
    pAzione         = request.getParameter("azione"); 
    p_do = pdo;
    if (pAzione == null)
      pAzione = "0";

    // -------------------------------------------------
    // Inizializzo il completeContextURL che � del tipo:
    // http://hostName:port/Sportello/
    // -------------------------------------------------
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

    if (completeContextURL == null) {
      completeContextURL = 
        serverScheme+"://"+
        serverName+":"+
        serverPort+
        request.getContextPath()+"/";
    }

        
    // Inizio con il caricare i parametri standard di connessione al db
    try {
      Parametri.leggiParametriStandard(inifile);
//      isVerbose = Parametri.ISVERBOSE;

    } catch (Exception e0) {
      logger.error("ServletInoltro::doGet() - Attenzione! Si � verificato un errore (1). "+e0.toString());
      return;
    }

    Hashtable tmpl_args = new Hashtable();
    tmpl_args.put("filename",fileName);
    tmpl_args.put("path", paths);
    Template tmpl = null;
        
    try {
      tmpl = new Template(tmpl_args);
    } catch (Exception e) {
      logger.error("ServletProtocollo::genera() - Errore nel template!");
    }

    if (pdo.equalsIgnoreCase("CC")) {
      tmpl.setParam("header", "0");
    } else {
      tmpl.setParam("header", "1");
    }
    // Inizio con il tirare su i dati del Protocollo
    // Attenzione: Se siamo in faseProtocollo 0 (iniziale) allora
    // bisogna visualizzare i dati del protocollo e predisporre
    // un link (sempre a ServletInoltro) per l'inoltro effettivo.

    if (pAzione.equals("1")) {
              
      pOggetto         = request.getParameter("oggetto");
      pTipoDoc         = request.getParameter("documento");
      pMovimento       = request.getParameter("movimento");
      pSmistamento     = request.getParameter("smistamento");
      pUnitaProt       = request.getParameter("unitaProtocollante");
      pApplicativo     = request.getParameter("applicativo");
      pUtente          = request.getParameter("utente");
      pClassificazione = request.getParameter("classificazione");
      pDocPrinc        = request.getParameter("docPrinc");
      pMittente        = request.getParameter("mittente");         // mittente
      queryProt = "UPDATE TIPI_PRATICHE "+
                  "SET OGGETTO = :OGGETTO, "+
                  "TIPO_DOCUMENTO = :DOCUMENTO, " +
                  "MOVIMENTO = :MOVIMENTO, " +
                  "SMISTAMENTO = :SMISTAMENTO, " +
                  "UNITA_PROTOCOLLANTE = :UNITA_PROTOCOLLANTE, " + 
                  "APPLICATIVO = :APPLICATIVO, " +
                  "UTENTE = :UTENTE, " +
                  "CLASSIFICAZIONE = :CLASSIFICAZIONE, " +
                  "DOCUMENTO_PRINCIPALE = :DOCUMENTO_PRINCIPALE, " + 
                  "MITTENTE = :MITTENTE " +
                  "WHERE CODICE_TIPO_PRATICA = :CTP ";
      try {
        dbOpSQLProt = SessioneDb.getInstance().createIDbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
        dbOpSQLProt.setStatement(queryProt);
        dbOpSQLProt.setParameter(":OGGETTO",pOggetto);
        dbOpSQLProt.setParameter(":DOCUMENTO",pTipoDoc);
        dbOpSQLProt.setParameter(":MOVIMENTO",pMovimento);
        dbOpSQLProt.setParameter(":SMISTAMENTO",pSmistamento);
        dbOpSQLProt.setParameter(":UNITA_PROTOCOLLANTE",pUnitaProt);
        dbOpSQLProt.setParameter(":APPLICATIVO",pApplicativo);
        dbOpSQLProt.setParameter(":UTENTE",pUtente);
        dbOpSQLProt.setParameter(":CLASSIFICAZIONE",pClassificazione);
        dbOpSQLProt.setParameter(":DOCUMENTO_PRINCIPALE",pDocPrinc);
        dbOpSQLProt.setParameter(":MITTENTE",pMittente);
        dbOpSQLProt.setParameter(":CTP",req_ctp);
        dbOpSQLProt.execute();
        dbOpSQLProt.commit();
        dbOpSQLProt.close();
      } catch (Exception ex) {
        logger.error("ServletProtocollo::doGet() - "+ex.toString());
      }
    } else {

      queryProt = "SELECT * "+
                  "FROM TIPI_PRATICHE "+
                  "WHERE CODICE_TIPO_PRATICA = :CTP ";
      try {
        dbOpSQLProt = SessioneDb.getInstance().createIDbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
        dbOpSQLProt.setStatement(queryProt);
        dbOpSQLProt.setParameter(":CTP", req_ctp);
        dbOpSQLProt.execute();
        rstProt = dbOpSQLProt.getRstSet();
        if (!rstProt.next()) {
          throw new Exception("ServletProtocolloo::doGet() - Attenzione! Impossibile trovare dati relativi alla pratica con CODICE_TIPO_PRATICA= "+req_ctp);
        }

        pOggetto = rstProt.getString("OGGETTO");
        pTipoDoc = rstProt.getString("TIPO_DOCUMENTO");
        pMovimento = rstProt.getString("MOVIMENTO");
        pClassificazione = rstProt.getString("CLASSIFICAZIONE");
        pUnitaProt = rstProt.getString("UNITA_PROTOCOLLANTE");
        pUtente = rstProt.getString("UTENTE");
        pApplicativo = rstProt.getString("APPLICATIVO");
        pDocPrinc = rstProt.getString("DOCUMENTO_PRINCIPALE");
        pSmistamento = rstProt.getString("SMISTAMENTO");
        pMittente = rstProt.getString("MITTENTE");

        dbOpSQLProt.close();
      } catch (Exception ex) {
        logger.error("ServletProtocollo::doGet() - "+ex.toString());
      }
    }

    if (pOggetto == null)
      pOggetto = "";
    if (pTipoDoc == null)
      pTipoDoc = "";
    if (pMovimento == null)
      pMovimento = "";
    if (pClassificazione == null)
      pClassificazione = "";
    if (pUnitaProt == null)
      pUnitaProt = "";
    if (pUtente == null)
      pUtente = "";
    if (pApplicativo == null)
      pApplicativo = "";
    if (pDocPrinc == null)
      pDocPrinc = "";
    if (pSmistamento == null)
      pSmistamento = "";
    if (pMittente == null)
      pMittente = "";

    CreaFormProtocollo(request.getServletPath(),
                       request.getContextPath(),
                       request.getQueryString(),
                       tmpl,
                       pOggetto,
                       pTipoDoc,
                       pMovimento,
                       pSmistamento,
                       pUnitaProt,
                       pApplicativo,
                       pUtente,
                       pClassificazione,
                       pDocPrinc,
                       pMittente);                

    corpo = tmpl.output();
    freeConn();

  }

/**
   * 
   */
  private void CreaFormProtocollo(
                          String servletPath,
                          String contextPath,
                          String queryString,
                          Template tmpl,
                          String oggetto,
                          String tipoDoc,
                          String movimento,
                          String smistam,
                          String unitProt,
                          String applicativo,
                          String utente,
                          String classificazione,
                          String docPrinc,
                          String mittente) {

      tmpl.setParam("nomeServer", serverName);
      tmpl.setParam("portaServer", serverPort);
      tmpl.setParam("pathSpor", contextPath);
      if (p_do.equalsIgnoreCase("CC")) {
        tmpl.setParam("praticheURL", completeContextURL+"ServletPratiche.do");
      } else {
        tmpl.setParam("praticheURL", completeContextURL+"ServletPratiche");
      }
      tmpl.setParam("pathServlet", servletPath);
      tmpl.setParam("stringaQuery", queryString);
      tmpl.setParam("oggetto", oggetto);
      tmpl.setParam("tipoDoc", tipoDoc);
      tmpl.setParam("movimento", movimento);
      tmpl.setParam("smistam", smistam);
      tmpl.setParam("unitProt", unitProt);
      tmpl.setParam("applicativo", applicativo);
      tmpl.setParam("utente", utente);
      tmpl.setParam("classificazione", classificazione);
      tmpl.setParam("docPrinc", docPrinc);
      tmpl.setParam("mittente", mittente);
  }

  /**
   * 
   */
  public String getValue() {
    return corpo;
  }

  /**
   * 
   */
  protected void freeConn() {
//    try {
//      SessioneDb.getInstance().closeFreeConnection();
//    } catch (Exception e) {
//      logger.error("ServletEditing::freeConn() - Attenzione! Errore in fase di rilascio connnessioni: "+e.toString());
//      corpo += "<H2>Attenzione! Errore in fase di rilascio connnessioni.</h2>";
//      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
//        corpo += e.toString();
//      }
//      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
//        corpo += e.getStackTrace().toString();
//      }
//    }
  }

}