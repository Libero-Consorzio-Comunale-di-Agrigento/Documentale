package it.finmatica.modulistica;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.parametri.Parametri;
import java.sql.*;
import org.apache.log4j.Logger;

public class Tagcampo {
  private String      inifile = null;
  private String      filesep = File.separator;
  private String      corpoHtml = "";
//  private static Properties confLogger = null;
  private static Logger     logger = Logger.getLogger(Tagcampo.class);

  public Tagcampo(String sPath) {
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
      logger.error("ServletTagcampo::init() - Attenzione! si è verificato un errore: "+e.toString());
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
   * 
   */
  protected void freeConn() {
//    try {
//      SessioneDb.getInstance().closeFreeConnection();
//    } catch (Exception e) {
//      logger.error("ServletTagcampo::freeConn() - Attenzione! Errore in fase di rilascio connnessioni: "+e.toString());
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
    String          id_tdoc   = null;
    String          area      = request.getParameter("area"),
                    cm        = request.getParameter("cm");
    ResultSet       rst       = null;


    try {
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);
    }
    catch (Exception e) {
      logger.error("ServletTagCampo::genera Errore in creazione dbOp - "+e.toString());
      return;
    }

    if (dbOp==null) {
      logger.error("ServletTagCampo::genera Errore in creazione dbOp - risulta nulla..");
      return;
    }

    try {
      String [] paths = {request.getSession().getServletContext().getRealPath("")+filesep+"template", "."};
      String fileName = "ServletTagcampo.tmpl";

      Hashtable tmpl_args = new Hashtable();
      tmpl_args.put("filename",fileName);
      tmpl_args.put("path", paths);
      HTML.Template tmpl = null;

      id_tdoc = ricavaIdtipodoc(area,cm,dbOp);
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
      String query = "SELECT NOME FROM CAMPI_DOCUMENTO "+
        "WHERE ID_TIPODOC = :ID_TDOC ORDER BY NOME ASC";
      try {

        dbOp.setStatement(query);
        dbOp.setParameter(":ID_TDOC", id_tdoc);
        dbOp.execute();
        rst = dbOp.getRstSet();
        String lbValore = "";
        while (rst.next()) {
          String nome     = rst.getString(1);
          lbValore += "<option value=\""+nome+"\">"+nome+"</option>";
        }
        tmpl.setParam("campi",lbValore);

      } catch (Exception e) {
        logger.error("ServletTagCampo::genera - "+e.toString());
        tmpl.setParam("errore",e.toString());

      }
      corpoHtml = tmpl.output();

    }
    finally {
      free(dbOp);
    }
  }

  /**
   * 
   */
  private String ricavaIdtipodoc (String ar, String cm, IDbOperationSQL dbOpEsterna) {
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
      dbOp = dbOpEsterna;

      dbOp.setStatement(query);
      dbOp.setParameter(":AREA",ar);
      dbOp.setParameter(":CM",cm);
      dbOp.execute();
      rst = dbOp.getRstSet();

      if (rst.next() ) {
         idtipodoc = ""+rst.getInt("ID_TIPODOC");
         codmod = rst.getString("CODICE_MODELLO_PADRE");
      } else {
        corpoHtml += "Errore in ricerca Identificativo tipo documento. Modello non trovato!";
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
      logger.error("ServletModulistica::ricavaIdtipodoc - "+e.toString());
      corpoHtml += "Errore in ricerca Identificativo tipo documento";
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        corpoHtml += e.toString();
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        corpoHtml += e.getStackTrace().toString();
      }
      return "";
    }
  }

}