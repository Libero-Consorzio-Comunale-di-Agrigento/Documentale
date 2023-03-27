package it.finmatica.modulistica;
import it.finmatica.dmServer.Environment;
import javax.servlet.http.*;

import java.io.*;
import java.sql.ResultSet;
import java.util.*;

import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.modulisticapack.*;
import it.finmatica.modulistica.parametri.Parametri;
import org.apache.log4j.Logger;

public class Ajax {
  private String      inifile = null;
  private String      corpoHtml = "";
//  private  static Properties  confLogger = null;
  private  static Logger logger = Logger.getLogger(Modulistica.class);
  protected Parametri parametri;
  private     String      pdo = "";
  private Environment vu = null;

  public Ajax(String sPath) {
    this(sPath, null);
  }

  public Ajax(String sPath, Environment vu) {
    this.vu = vu;
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
      loggerError("ServletModulistica::init() - Attenzione! si è verificato un errore: "+e.toString(),e);
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
  public void genera(HttpServletRequest  request, String p_do) {
    try {
      try {
        vu.connect();
        vu.setDbOpRestaConnessa(true);
      }
      catch (Exception e) {
        e.printStackTrace();
        return;
      }

      this.genera(request,p_do,"ServletAjax",vu.getDbOp());
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
  }

  /**
   *  Funzione che genera il codice HTML del Modello.
   *  Nel caso in cui il modello è incluso in una pagina 
   *  sviluppata in CodeCharge occcorre settare il parametro p_do
   *  a CC o HR, se invece è incluso in una Servlet settare il parametro p_do
   *  a stringa vuota.
   *  @param request Request della pagina che incorpora il modello
   *  @param p_do Indicatore per CodeCharge
   *  @param p_nome Nome della pagina che include il modello. Nel caso in
   *  cui il parametro p_do è settato a HR è possibile settare qui un href.
   */
  public void genera(HttpServletRequest  request, String p_do, String p_nome, IDbOperationSQL dbOpEsterna) {
    IElementoModello iem = null;
    CampoHTMLIn ch = null;
    BloccoMultirecord bl = null;
    BloccoMultirecord bl_new = null;
    VisualizzaHTML vh = null;
    VisualizzaHTML vh_new = null;
    boolean trovato = false;
    String  area    = request.getParameter("area");
    String  cm      = request.getParameter("cm");
    String  cr      = request.getParameter("cr");
    String  campo   = request.getParameter("gdm_ajax_campo");
    String  blocco  = request.getParameter("gdm_ajax_blocco");
    String  settore = request.getParameter("gdm_ajax_settore");

    if (campo == null) {
      campo = "";
    }
    if (blocco == null) {
      blocco = "";
    }
    if (settore == null) {
      settore = "";
    }
    pdo = p_do;
    try {
      Modello md = cercaModello(request.getSession(),area,cm, cr);

      if (md == null) {
        corpoHtml = "Errore_GDM_Ajax";
        return;
      }
      ListaProtetti   protetti;
      protetti = new ListaProtetti();
      protetti.caricaDominii(area,cm,request,dbOpEsterna);
      request.getSession().setAttribute("listaProtetti", protetti);
      int i = 0;
      while ((i < md.getNumeroElementi()) && (!trovato)) {
        iem = md.getElemento(i);
        if ((campo.length() != 0) && (iem.getClass().getName()).equalsIgnoreCase("it.finmatica.modulistica.modulisticapack.CampoHTMLIn")) {
          ch = (CampoHTMLIn)iem;
          if (ch.getDato().equalsIgnoreCase(campo)) {
            ch.identificaCampo(ch.getTestoHTML());
            ch.caricaCampo(request);
            ch.caricaValore(request);
            corpoHtml = ch.getValue(dbOpEsterna);
            trovato = true;
          }
        } 
        if ((blocco.length() != 0) && (iem.getClass().getName()).equalsIgnoreCase("it.finmatica.modulistica.modulisticapack.BloccoMultirecord")) {
          bl = (BloccoMultirecord)iem;
          if (bl.getNomeBlocco().equalsIgnoreCase(blocco)) {
            bl_new = new BloccoMultirecord(request,area,cm,bl.getBlocco(),bl.getNavigatore(),false,dbOpEsterna);
            if (bl.getMVPG().length() != 0) {
              bl_new.setMVPG(bl.getMVPG());
            }
            bl_new.settaProtetto(bl.getProtetto());
            corpoHtml = bl_new.getValue(dbOpEsterna);
            trovato = true;
          }
        } 
        if ((settore.length() != 0) && (iem.getClass().getName()).equalsIgnoreCase("it.finmatica.modulistica.modulisticapack.VisualizzaHTML")) {
          vh = (VisualizzaHTML)iem;
          if (vh.getSettore().equalsIgnoreCase(settore)) {
            vh_new = new VisualizzaHTML(request,area,cm,vh.getTagSettore(),dbOpEsterna);
            if (vh.getProtetto() == vh_new.getProtetto()) {
              corpoHtml = vh_new.getAjaxValue();
              trovato = true;
            } else {
              corpoHtml = "Errore_GDM_Ajax";
              trovato = true;
            }
          }
        } 
        i++;
      }
      if (!trovato) {
        corpoHtml = "Errore_GDM_Ajax";
      }
    } catch (Exception e) {
      corpoHtml = "Errore_GDM_Ajax";
      logger.error("Errore Ajax: "+e.toString(),e);
    }
  }


  /**
   *  Funziona che ritorna il codice HTML del Modello
   */
  public String getValue() {
    return corpoHtml;
  }

  /**
   * cercaModello()
   * Ricerca a livello di sessione la presenza del modello richiesto.
   * Se è già presente evita di ricaricarlo da database.
   **/
  private Modello cercaModello(HttpSession pHttpSess, String pArea, String pCodiceModello, String pCodiceRichiesta) throws Exception {
    Modello     md = null;
    ArrayList   modelli;
    String      sNomeServlet = "";
    boolean     trovato = false;
    int         i = 0;

    try {
      modelli = (ArrayList) pHttpSess.getAttribute("modelli");
      sNomeServlet = (String) pHttpSess.getAttribute("p_nomeservlet");
//      if (pdo.equalsIgnoreCase("HR")) {
        if (sNomeServlet != null) {
          int punto = sNomeServlet.indexOf(".do?");
          if (punto > 0) {
            sNomeServlet = sNomeServlet.substring(0,punto);
          }
        }
//      }
      if (modelli == null) {
        return null;
      }

      while ( (!trovato) && (i < modelli.size()) ) {
        md = (Modello)modelli.get(i);
        if ((md.getArea().equals(pArea)) &&
            (md.getCodiceModello().equals(pCodiceModello)) &&
            (md.getCodiceRichiesta().equals(pCodiceRichiesta)) &&
            (md.getNomeServlet().equals(sNomeServlet))) {
          trovato = true;
        } else {
          i += 1;
        }
      }

      if (trovato) {
        return md;
      }
      i=0;
      sNomeServlet = (String) pHttpSess.getAttribute("p_nomeservlet_padre");
      if (sNomeServlet == null) {
        sNomeServlet = "";
      }
      if (sNomeServlet.length() == 0) {
        return null;
      } else {
        pHttpSess.setAttribute("p_nomeservlet",sNomeServlet);
      }
      while ( (!trovato) && (i < modelli.size()) ) {
        md = (Modello)modelli.get(i);
        if ((md.getArea().equals(pArea)) &&
            (md.getCodiceModello().equals(pCodiceModello)) &&
            (md.getCodiceRichiesta().equals(pCodiceRichiesta)) &&
            (md.getNomeServlet().equals(sNomeServlet))) {
          trovato = true;
        } else {
          i += 1;
        }
      }

      if (!trovato) {
        return null;
      } else {
        return md;
      }

    } catch(Exception e) {
      loggerError("SerletModulistica::cercaModello() - Area: "+pArea+" - Modello: "+pCodiceModello+"- Richiesta: "+pCodiceRichiesta+" - Attenzione! Si è verificato un errore durante la ricerca del modello in memoria: "+e.toString(),e);
      corpoHtml += "Attenzione! Si è verificato un errore durante la ricerca del modello in memoria!";
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        corpoHtml += e.toString();
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        corpoHtml += e.getStackTrace().toString();
      }
      return null;
    }
  }

}