package it.finmatica.modulistica;
//import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.kofax.JBarCode;
import org.apache.log4j.Logger;

public class Barcode {
//  private static final String CONTENT_TYPE = "text/html; charset=windows-1252";
  private String      inifile = null;
//  private String      filesep = File.separator;
  private String      corpoHtml = "";
//  private boolean     isVerbose;
//  private static Properties confLogger = null;
  private static Logger     logger = Logger.getLogger(Barcode.class);


  public Barcode(String sPath) {
    init(sPath);
  }

  private void init(String sPath)  {
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
      Parametri.leggiParametriStandard(inifile);

      SessioneDb.getInstance().addAlias(Parametri.ALIAS, Parametri.SPORTELLO_DRIVER);
    } catch(Exception e) {
      loggerError("ServletTagcampo::init() - Attenzione! si è verificato un errore: "+e.toString(),e);
    }
//    isVerbose = Parametri.ISVERBOSE;
  }
  
  /**
   *
   */
/*  private void free(IDbOperationSQL dbOp) {
    try {
      dbOp.close();
    } catch (Exception e) { }
  }*/

  /**
   * 
   */
  protected void freeConn() {
//    try {
//      SessioneDb.getInstance().closeFreeConnection();
//    } catch (Exception e) {
//      loggerError("ServletTagcampo::freeConn() - Attenzione! Errore in fase di rilascio connnessioni: "+e.toString(),e);
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
  public String getValue() {
    return corpoHtml;
  }

  /**
   * 
   */
  public void genera(HttpServletRequest request, String pdo) {
//    HttpSession     session   = request.getSession();
    String          desc1     = request.getParameter("desc1");
    String          desc2     = request.getParameter("desc2");
    String          desc3     = request.getParameter("desc3");
    String          iddoc     = request.getParameter("iddoc");
//    ResultSet       rst       = null;


//    JBarCode jb = new JBarCode(iddoc,desc1,desc2);
    JBarCode jb = new JBarCode(iddoc,desc1,desc2,desc3);
    corpoHtml = jb.getHTMLBarCode();
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