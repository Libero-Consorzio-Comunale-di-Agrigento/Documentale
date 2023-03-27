package it.finmatica.modulistica;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;

import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.parametri.Parametri;
import org.apache.log4j.Logger;

public class Firma {
  private String      inifile = null;
  private String      corpoHtml = "";
  private  static Logger logger = Logger.getLogger(Firma.class);

  public Firma(String sPath) {
    init(sPath);
  }

  private void init(String sPath)  {
    try {
      String separa = File.separator;

      inifile = sPath + "config" + separa + "gd4dm.properties";
      File f = new File(inifile);
      if (!f.exists()) {
        inifile = sPath + ".." + separa + "jgdm" + separa + "config" + separa + "gd4dm.properties";
      }
      Parametri.leggiParametriStandard(inifile);
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
//      
//      PropertyConfigurator.configure(confLogger);

      SessioneDb.getInstance().addAlias(Parametri.ALIAS, Parametri.SPORTELLO_DRIVER);
    } catch(Exception e) {
      loggerError("ServletFirma::init() - Attenzione! si è verificato un errore: "+e.toString(),e);
    }
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


  /**
   *  Funzione che genera il codice HTML del Modello.
   *  Il nome della pagina che includerà il modello che deve 
   *  obbligatoriamente chiamarsi ServletModulistica.
   *  Nel caso in cui il modello è incluso in una pagina 
   *  sviluppata in CodeCharge occcorre settare il parametro p_do
   *  a CC, se invece è incluso in una servlet settare il parametro p_do
   *  a stringa vuota.
   *  @param request Request della pagina che incorpora il modello
   *  @param p_do Indicatore per CodeCharge
   */
  public void genera(HttpServletRequest  request) {
    HTML.Template  tmpl = null;

    String [] paths = {request.getSession().getServletContext().getRealPath("")+File.separator+"template", "."}; 
    String fileName = "ServletFirma.tmpl";
    String  area        = request.getParameter("area"),             // area di competenza: mandatory
            cr          = request.getParameter("cr"),               // codice richiesta
            cm          = request.getParameter("cm"),                // codice modello
            ca          = request.getParameter("ca"),               // codice richiesta
            iddoc          = request.getParameter("iddoc"),                // codice modello
            scelta          = request.getParameter("scelta");                // codice modello

    if (scelta == null) {
      Hashtable tmpl_args = new Hashtable();
      tmpl_args.put("filename",fileName);
      tmpl_args.put("path", paths);
  
      try {
        tmpl = new HTML.Template(tmpl_args);
      } catch (Exception e) {
        logger.error("Firma::genera() - Attenzione! Si è verificato un errore: "+e.toString());
      }
      tmpl.setParam("varea", area);
      tmpl.setParam("codModello", cm);
      tmpl.setParam("codRichiesta", cr);
      tmpl.setParam("idOgg", ca);
      tmpl.setParam("idDoc", iddoc);
      corpoHtml = tmpl.output();
    } else {
      if (scelta.equalsIgnoreCase("1")) {
        //Verifico la firma
      }
      if (scelta.equalsIgnoreCase("2")) {
        //Utilizzo il file in formato P7M
      }
      if (scelta.equalsIgnoreCase("3")) {
        //Utilizzo il documento ripulito
      }
    }
  }

  /**
   *  Funziona che ritorna il codice HTML del Modello
   */
  public String getValue() {
    return corpoHtml;
  }

}