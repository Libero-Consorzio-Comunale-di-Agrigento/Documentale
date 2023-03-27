package it.finmatica.modulistica;

import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.jfc.http.utils.multipartrequest.ServletMultipartRequest;
import it.finmatica.jfc.http.utils.multipartrequest.MultipartRequest;
import org.apache.log4j.Logger;

public class Immagini {

  private String inifile = null;
  private String path = null;
  private String filesep = File.separator;
  private String corpoHtml = "";
  private static Logger     logger = Logger.getLogger(Immagini.class);

  public Immagini(String sPath) {
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
      logger.error("ServletImmagini:init() - Attenzione! si è verificato un errore: "+e.toString());
    }
  }

  /**
   * 
   */
  public void genera(HttpServletRequest request, String pdo) {
    String      reqContentType = request.getContentType();
    HttpSession session = request.getSession();
    String      cr, cm, area;
    String      filename = null;
    String      contx = request.getParameter("contx");
    
    if (contx == null) {
      contx = "";
    }

    if ( !request.getContextPath().equalsIgnoreCase(contx) && contx.length() != 0) {
      contx = filesep+".."+contx;
    } else {
      contx = "";
    }
    String [] paths = {request.getSession().getServletContext().getRealPath("")+filesep+"template","."}; 
    String fileName = "ServletImmagini.tmpl";
    
    // -------------------------------------------------
    // Inizializzo il completeContextURL che è del tipo:
    // http://hostName:port/Sportello/
    // -------------------------------------------------
/*    if (Parametri.SERVERNAME.length() == 0) {
      serverName = request.getServerName();
    } else {
      serverName = Parametri.SERVERNAME;
    }
    if (Parametri.SERVERPORT.length() == 0) {
      serverPort = ""+request.getServerPort();
    } else {
      serverPort = Parametri.SERVERPORT;
    }*/

    area = request.getParameter("area");
    cm = request.getParameter("cm");
    cr = request.getParameter("cr");
    if (cr == null) {
      cr = (String)session.getAttribute("key");
    }
//    p_user = (String)session.getAttribute("UtenteGDM");

    path = request.getSession().getServletContext().getRealPath("")+contx+filesep+"temp";
    path = path+filesep+area+filesep+cm+filesep+cr;
    File filedir = new File(path);
    if (!filedir.exists()) {
      filedir.mkdirs();
    }

    Hashtable tmpl_args = new Hashtable();
    tmpl_args.put("filename",fileName);
    tmpl_args.put("path", paths);

    HTML.Template tmpl = null;
    try {
      tmpl = new HTML.Template(tmpl_args);
    } catch (Exception e) {
    }

    if (pdo.equalsIgnoreCase("CC")) {
      tmpl.setParam("header", "0");
    } else {
      tmpl.setParam("header", "1");
    }

//    tmpl.setParam("percorso","temp"+filesep+area+filesep+cm+filesep+p_user+filesep+cr);
    if (contx.length() == 0) {
      tmpl.setParam("percorso","temp"+filesep+area+filesep+cm+filesep+cr);
    } else {
      tmpl.setParam("percorso",".."+filesep+"temp"+filesep+area+filesep+cm+filesep+cr);
    }
    // ----------------------------------------------------------------------------------
    // Trattamento della request nel caso di submit multipart/form-data
    // ----------------------------------------------------------------------------------
    if ((reqContentType != null) && (reqContentType.indexOf("multipart/form-data") >= 0)) {
      MultipartRequest multipartRequest = null;
      try {
        multipartRequest = new ServletMultipartRequest(request, path);

        String immagine = multipartRequest.getURLParameter("filecompleto");
        int j1 = immagine.lastIndexOf("/");
        int j2 = immagine.lastIndexOf("\\");
        if (j1 > j2) {
          filename = immagine.substring(j1+1);
        } else {
          filename = immagine.substring(j2+1);
        }
//        File f = new File(immagine);
//        filename = f.getName();

        tmpl.setParam("nomeImmagine",filename);
      } catch (Exception ex) {
        logger.error("ServletImmagini::doGet() - Attenzione! Si è verificato un errore: "+ex.toString());
        return;
      }
    }

    corpoHtml += tmpl.output();
  }

  /**
   * 
   */
  public String getValue() {
    return corpoHtml;
  }

}