package it.finmatica.modulistica;

import javax.servlet.http.*;
import java.io.*;
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.GDMTreeViewWeb.TreeView.*;
import org.apache.log4j.Logger;
//import org.apache.log4j.PropertyConfigurator;
import it.finmatica.jfc.dbUtil.*;

public class Albero {
  private String      inifile = null;
//  private String      logfile = null;
//  private String      dmsfile = null;
//  private String      filesep = File.separator;
//  private boolean     isVerbose;
  private String      corpoHtml = "";
//  private static Properties confLogger = null;
  private static Logger     logger = Logger.getLogger(Albero.class);

  /**
   * Crea e inizializza un nuovo oggetto Albero
   * @param sPath Path reale della Servlet 
   */
  public Albero(String sPath) {
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
      loggerError("ServletAlbero::init() - Attenzione! si è verificato un errore: "+e.toString(),e);
    }
//    isVerbose = Parametri.ISVERBOSE;
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
   *  Funzione che genera il codice HTML dell'albero.
   *  Nel caso in cui l'albero è incluso in una pagina 
   *  sviluppata in CodeCharge occcorre settare il parametro p_do
   *  a CC, se invece è incluso in una servlet settare il parametro p_do
   *  a stringa vuota.
   *  @param request Request della pagina che incorpora l'albero
   *  @param p_do Indicatore per CodeCharge
   */
  public void genera(HttpServletRequest  request, String p_do) {
    IDbOperationSQL dbOpSQL = null;
    String  ar  = request.getParameter("area"),
            sql = request.getParameter("table");
    
    //Qua inserisco la rte del treeview
    TreeView t = new TreeView();

    t.setDefaultTarget("");
    t.setMarginLeft("12");               
    t.setFont("verdana");
    t.setFontPt("7");

    try {
      dbOpSQL = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);
      

      t.loadFromDb(dbOpSQL,sql,ar);
      t.display();
      corpoHtml = "<html>\n<head>\n<title>Albero</title>\n</head>\n"+
        "<script type=\"text/javascript\">\n"+
        "function insertHTML(html) {parent.insertHTML(html);}\n"+
        "function inserimentoCartella(idpadre,titolo,area) {parent.inserimentoCartella(idpadre,titolo,area);}\n"+
        "</script>\n"+
        "<body><form>\n"+
        "<div unselectable='on' onmouseout=\"this.style.textDecoration='none'\" onmouseover=\"this.style.cursor='hand'; this.style.textDecoration='underline';\" onclick=\"javascript:inserimentoCartella(0,'Radice','"+ar+"')\"><font face=\"Verdana\" size=\"1\">&nbsp;Radice</font></a></div>"+
        t.getOut()+"\n</form></body>\n</html>";
		      	
      free(dbOpSQL);
    } catch(Exception ex) {
      loggerError("ServletAlbero::genera() - Attenzione! si è verificato un errore: "+ex.toString(),ex);
		  free(dbOpSQL);   
    }
  }

  /**
   *  Funziona che ritorna il codice HTML dell'albero
   */
  public String getValue() {
    return corpoHtml;
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