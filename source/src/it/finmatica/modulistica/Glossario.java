package it.finmatica.modulistica;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.parametri.Parametri;
import org.apache.log4j.Logger;

public class Glossario {
//  private static final String CONTENT_TYPE = "text/html; charset=windows-1252";
  private String      inifile = null;
  private String      filesep = File.separator;
  private String      corpoHtml = "";
//  private boolean     isVerbose;
  private static Logger     logger = Logger.getLogger(Glossario.class);

  public Glossario(String sPath) {
    init(sPath);
  }

  public void init(String sPath)  {
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
      logger.error("ServletGlossario::init() - Attenzione! si è verificato un errore: "+e.toString());
    }
//    isVerbose = Parametri.ISVERBOSE;
  }
  
  /**
   *
   */
  private void free(IDbOperationSQL dbOp) {
    try {
//      dbOp.getStmSql().clearParameters();
      dbOp.close();
    } catch (Exception e) { }
  }

  /**
   * 
   */
  protected void freeConn() {
//    try {
//      SessioneDb.getInstance().closeFreeConnection();
//    } catch (Exception e) {
//      logger.error("ServletEditing::freeConn() - Attenzione! Errore in fase di rilascio connnessioni: "+e.toString());
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
    IDbOperationSQL  dbOp      = null;
    String          ar        = request.getParameter("area"),
                    frase     = request.getParameter("frase"),
                    frase_new = request.getParameter("frase_new"),
                    id_padre  = request.getParameter("id_frase_padre"),
                    des_padre = request.getParameter("des_padre"),
                    titolo    = request.getParameter("titolo"),
                    paramet   = request.getParameter("parametrica"),
                    salva     = request.getParameter("salva");

    String ptemp = null;
    if (pdo.equalsIgnoreCase("CC")) {
      ptemp = request.getSession().getServletContext().getRealPath("")+filesep+".."+filesep+Parametri.APPLICATIVO+filesep+"template";
    } else {
      ptemp = request.getSession().getServletContext().getRealPath("")+filesep+"template";
    }
    String [] paths = {ptemp,"."}; 
//    String [] paths = {request.getSession().getServletContext().getRealPath("")+filesep+"template", "."}; 
    String fileName = "ServletGlossario.tmpl";

    Hashtable tmpl_args = new Hashtable();
    tmpl_args.put("filename",fileName);
    tmpl_args.put("path", paths);
    HTML.Template tmpl = null;

     if (paramet == null) {
       paramet = "N";
     }
    try {
      tmpl = new HTML.Template(tmpl_args);
    } catch (Exception e) {
      logger.error("ServletGlossario::genera() - Errore nel template!");
    }

    if (pdo.equalsIgnoreCase("CC")) {
      tmpl.setParam("header", "0");
    } else {
      tmpl.setParam("header", "1");
    }

    if (frase_new != null) {
      frase = frase_new;
    }
    tmpl.setParam("inserimento","1");
    tmpl.setParam("area", ar);
    tmpl.setParam("titolo", titolo);
    tmpl.setParam("frase", frase);
    tmpl.setParam("id_frase_padre", id_padre);
    tmpl.setParam("des_padre", des_padre);
    if (paramet.equalsIgnoreCase("S")) {
      tmpl.setParam("parametrica", 1);
    } else {
      tmpl.setParam("parametrica", 0);
    }
    tmpl.setParam("errore","");

    if (salva != null) {
      if (id_padre == null) {
        tmpl.setParam("errore","Cartella padre non specificata!");
        corpoHtml = tmpl.output();
        return;
      }
      if (id_padre.length() == 0) {
        tmpl.setParam("errore","Cartella padre non specificata!");
        corpoHtml = tmpl.output();
        return;
      }
      if (frase == null) {
        tmpl.setParam("errore","La frase non può essere nulla!");
        corpoHtml = tmpl.output();
        return;
      }
      if (frase.length() == 0) {
        tmpl.setParam("errore","La frase non può essere nulla!");
        corpoHtml = tmpl.output();
        return;
      }
      if (titolo == null) {
        tmpl.setParam("errore","Occore indicare un titolo!");
        corpoHtml = tmpl.output();
        return;
      }
      if (titolo.length() == 0) {
        tmpl.setParam("errore","Occore indicare un titolo!");
        corpoHtml = tmpl.output();
        return;
      }
/*      String query = "INSERT INTO FRASI_GLOSSARIO (AREA,ID_FRASE_PADRE,TITOLO,PARAMETRICA,FRASE)"+
        " VALUES ('"+ar+"',"+id_padre+",'"+titolo+"','"+paramet+"','"+frase+"')";*/
      String query = "INSERT INTO FRASI_GLOSSARIO (AREA,ID_FRASE_PADRE,TITOLO,PARAMETRICA,FRASE)"+
          " VALUES (:AREA,:IDPADRE,:TITOLTO,:PARAMET,:FRASE)";
      try {
        dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);

        dbOp.setStatement(query);
        dbOp.setParameter(":AREA", ar);
        dbOp.setParameter(":IDPADRE", id_padre);
        dbOp.setParameter(":TITOLTO", titolo);
        dbOp.setParameter(":PARAMET", paramet);
        dbOp.setParameter(":FRASE", frase);
        dbOp.execute();
        dbOp.commit();
        free(dbOp);
        tmpl.setParam("inserimento","0");
    
      } catch (Exception e) {
        logger.error("ServletGlossario::genera - "+e.toString());
        tmpl.setParam("errore",e.toString());
        free(dbOp);
      }
    }
    corpoHtml = tmpl.output();
    freeConn();

  }
  
}