package it.finmatica.modulistica;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.parametri.Parametri;
import java.sql.*;
import it.finmatica.modulistica.modulisticapack.*;
import org.apache.log4j.Logger;

public class Valore {
  private String      inifile = null;
  private String      filesep = File.separator;
  private String      corpoHtml = "";
//  private static Properties confLogger = null;
  private  static Logger logger = Logger.getLogger(Valore.class);

  public Valore(String sPath) {
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
      logger.error("ServletValore::init() - Attenzione! si è verificato un errore: "+e.toString());
    }
//    isVerbose = Parametri.ISVERBOSE;
//    Util.isVerbose = isVerbose;
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
   * Process the HTTP doGet request.
   */
  public void genera(HttpServletRequest request, String pdo) {
//    HttpSession     session = request.getSession();
    String          ar      = request.getParameter("area");
    String          cm      = request.getParameter("cm");
    String          id_sess = request.getParameter("id_sess");
    IDbOperationSQL  dbOp = null;
//    DbOperationSQL  dbOpVal = null;
    ResultSet       rst = null;
//    ResultSet       rstVal = null;
    String          query;
    String          dato, /*cval,*/ valore = "", domForm, tipo;
    String          lbValore = "";
    DominioFormula  dominioForm = null;
    String ptemp = null;
    request.getSession().setAttribute("listaDomini",null);
    if (pdo.equalsIgnoreCase("CC")) {
      ptemp = request.getSession().getServletContext().getRealPath("")+filesep+".."+filesep+Parametri.APPLICATIVO+filesep+"template";
    } else {
      ptemp = request.getSession().getServletContext().getRealPath("")+filesep+"template";
    }
    String [] paths = {ptemp,"."}; 
//    String [] paths = {request.getSession().getServletContext().getRealPath("")+filesep+"template", "."}; 
    String fileName = "ServletValore.tmpl";

    Hashtable tmpl_args = new Hashtable();
    tmpl_args.put("filename",fileName);
    tmpl_args.put("path", paths);
    HTML.Template tmpl = null;
        
    try {
      tmpl = new HTML.Template(tmpl_args);
    } catch (Exception e) {
      logger.error("ServletValore::genera() - Errore nel template!");
    }

    if (pdo.equalsIgnoreCase("CC")) {
      tmpl.setParam("header", "0");
    } else {
      tmpl.setParam("header", "1");
    }

    query = "SELECT DATO, 'NO' CVAL, DOMINIO_FORMULA, TIPO "+
        "  FROM DATI "+
        " WHERE AREA = :AREA"+
        "   AND DOMINIO_FORMULA IS NOT NULL ";

    try {
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);
    } catch (Exception e) {
      logger.error("ServletValore::genera() Errore in connessione dbOp - Errore:  ["+ e.toString()+"]",e);
      return;
    }

    if (dbOp==null) {
      logger.error("ServletValore::genera() Errore in connessione dbOp - Errore:  risulta nulla");
      return;
    }

            
    try {
      preCaricamentoDati(request, ar, id_sess, cm,dbOp);

      dbOp.setStatement(query);
      dbOp.setParameter(":AREA",ar);
      dbOp.execute();
      rst = dbOp.getRstSet();
      List<DatoForm> listaDatiForm = new ArrayList<DatoForm>();
      while (rst.next()) {
        listaDatiForm.add(new DatoForm(rst.getString(1),rst.getString(3),rst.getString(4)));
      }

      for(int indexDatoForm = 0;indexDatoForm<listaDatiForm.size();indexDatoForm++) {
        dato = listaDatiForm.get(indexDatoForm).getDato();
//        cval = rst.getString(2);
        domForm = listaDatiForm.get(indexDatoForm).getDatoForm();
        tipo = listaDatiForm.get(indexDatoForm).getTipo();
//        if (cval.equalsIgnoreCase("NO")) {
        dominioForm = new DominioFormula(ar, domForm, "-", tipo, null, dato, request,dbOp);
        valore = dominioForm.getValore(dato);
//        } else {
//          dbOpVal = new DbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
//          dbOpVal.setStatement(queryVal);
//          dbOpVal.setParameter(":AREA",ar);
//          dbOpVal.execute();
//          rstVal = dbOpVal.getRstSet();
//          if (rstVal.next()) {
//            Clob clob = rstVal.getClob(1);
//            long clobLen = clob.length();
//            int i_clobLen = (int)clobLen;
//            valore = clob.getSubString(1, i_clobLen);
//          }
//          free(dbOpVal);
//        }
        lbValore += "<option value=\""+valore+"\">"+dato+"</option>";
      }
    
    } catch (Exception e) {
      logger.error("ServletValore::genera - "+e.toString());
    }
    finally {
      free(dbOp);
    }

    tmpl.setParam("valori",lbValore);

    corpoHtml = tmpl.output();
    request.getSession().setAttribute("listaDomini",null);

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
  protected void freeConn() {
//    try {
//      SessioneDb.getInstance().closeFreeConnection();
//    } catch (Exception e) {
//      logger.error("ServletValore::freeConn() - Attenzione! Errore in fase di rilascio connnessioni: "+e.toString());
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
   * preCaricamentoDati()
   * Metodo per il precaricamento dei dati. Si cerca un dominio di area e si richiama la
   * opportuna funzione.
   *
   * @param request
   * @param pArea area di riferimento
   * @param cr codice richiesta
   */
  private void preCaricamentoDati(HttpServletRequest request, String pArea, String cr, String cm, IDbOperationSQL dbOpEsterna) throws Exception {
    ListaDomini ld;
//    Dominio     dominioArea;
    try {
      ld = (ListaDomini)request.getSession().getAttribute("listaDomini");
      if (ld == null) {
        // Inizializzo una variabile di tipo ListaDomini a livello di sessione
        // che servirà a contenere i puntatori ai vari domini letti dal database.
        // In questo modo i singoli oggetti Dominio verranno inizializzati solo
        // una volta per ogni coppia di "area , dominio" (pk) e referenziati
        // nelle liste dei campi di volta in volta.
        ld = new ListaDomini();
        request.getSession().setAttribute("listaDomini", ld);
      } else {
        // Aggiorno i valori dei domini già in memoria (la funzione della lista
        // si preoccupa di aggiornare solo quelli che sono parametrici)
        ld.aggiornaDomini(request,dbOpEsterna);
      }

      // Tiro su i dominii di area ordinati per sequenza
      ld.caricaDominiiDiArea(pArea, request, false, dbOpEsterna);
      ld.caricaDominiiDelModello(pArea, cm, request, false,dbOpEsterna);
      request.getSession().setAttribute("listaDomini",ld);
    } catch (Exception ex) {
      logger.error("ServletValore::preCaricamentoDati() - Attenzione! Si è verificato un errore in fase di precaricamento: "+ex.toString(),ex);
      throw ex; 
    }
  }

}

class DatoForm {
  private String dato, datoForm, tipo;

  public DatoForm(String dato, String datoForm, String tipo) {
    this.dato = dato;
    this.datoForm = datoForm;
    this.tipo = tipo;
  }

  public String getDato() {
    return dato;
  }

  public void setDato(String dato) {
    this.dato = dato;
  }

  public String getDatoForm() {
    return datoForm;
  }

  public void setDatoForm(String datoForm) {
    this.datoForm = datoForm;
  }

  public String getTipo() {
    return tipo;
  }

  public void setTipo(String tipo) {
    this.tipo = tipo;
  }
}