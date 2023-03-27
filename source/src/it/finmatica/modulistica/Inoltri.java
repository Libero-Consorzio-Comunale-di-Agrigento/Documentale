package it.finmatica.modulistica;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import it.finmatica.modulistica.inoltro.*;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.parametri.Parametri;
import java.sql.*;
import it.finmatica.dmServer.management.*;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.util.JNDIParameter;
import it.finmatica.dmServer.Environment;
import org.apache.log4j.Logger;
//import org.apache.log4j.PropertyConfigurator;

import xmlpack.InfoConnessione;
 
public class Inoltri {
  public final static int MAXLEN_PARAMETRI = 1000000;   
  public final static String FASE_PROT_INIZIALE = "0";
  private String pidOp = null;
  private String      req_ctp,       // parametri che arrivano dalla request
                      req_cr,
                      req_cm,
                      req_area,
                      req_utente,
                      req_ruolo,
                      req_allegati,
                      req_provenienza,
                      req_rw;
  private String      inifile = null;
  private String      completeContextURL = null;
  private String      filesep = File.separator;
  private String      serverName;
  private String      serverPort;
  private String      iddoc;
  private String      corpoHtml = "";
  private String      noBody = Global.NOBODY;
  private Environment vu;
  private static Properties confLogger = null;
  private static Logger     logger = Logger.getLogger(Inoltri.class);
  
  public Inoltri(String sPath) {
    init(sPath);
  }
  /**
   * init()
   */
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

      // --------------------------------------------------------------------------------------------
      // Creazione alias
      // --------------------------------------------------------------------------------------------
      Parametri.leggiParametriStandard(inifile);
     
      SessioneDb.getInstance().addAlias(Parametri.ALIAS, Parametri.SPORTELLO_DRIVER);
      
/*      infoConnessione = new InfoConnessione(Parametri.ALIAS, 
                                            Parametri.SPORTELLO_DSN, 
                                            Parametri.USER, 
                                            Parametri.PASSWD);*/
    } catch(Exception e) {
      logger.error("Inoltro::init() - Attenzione: problemi in fase di init()");
//      e.printStackTrace();
    }
  }

  /**
   * Process the HTTP doGet request.
   */
  public void genera(HttpServletRequest request, String pdo) {
    IDbOperationSQL dbOpSQL = null,
                   dbOpIns = null,
                   dbOpMod = null;
    ResultSet      rst, rstIns = null, rstMod = null; 
//    boolean        isValidRequest = true;   // Se qualche parametro non va, non si avvia nessun inoltro.
//    StringBuffer   msg = new StringBuffer();
    int            inoltriEseguiti = 0;     // Inoltri eseguiti
    int            inoltriFalliti = 0;     // Inoltri falliti
    String         queryIns = null;
    String         /*pSequenza,*/
                   pClassName,
                   pParametri = null,
                   sDsn = "";;
    Inoltro        inoltro;
    HTML.Template  tmpl = null;

    String [] paths = {request.getSession().getServletContext().getRealPath("")+filesep+"template", "."}; 
    String fileName = "ServletInoltro.tmpl";
    String query = "";
    String qMod = null;
    String cm = null;
    
    HttpSession     session = request.getSession();

    req_ctp         = request.getParameter("ctp");
    req_area        = request.getParameter("area");             // area di competenza: mandatory
    req_cr          = request.getParameter("cr");               // codice richiesta
    req_cm          = request.getParameter("cm");                // codice modello
    req_utente      = null;
    req_allegati    = request.getParameter("allegati");         //........da gestire
    req_provenienza = request.getParameter("prov");
    req_rw          = request.getParameter("rw");

    if (req_utente == null) {
      req_utente = (String)session.getAttribute("UtenteGDM");
      req_ruolo  = (String)session.getAttribute("RuoloGDM");
    }
    if (req_ruolo == null) {
      req_ruolo = caricaRuolo(req_utente);
    }

    if (req_provenienza == null){
      req_provenienza = "";
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
        request.getScheme()+"://"+
        serverName+":"+
        serverPort+
        request.getContextPath()+"/";
    }

        
    // Inizio con il caricare i parametri standard di connessione al db
    try {
      Parametri.leggiParametriStandard(inifile);
//      isVerbose = Parametri.ISVERBOSE;

    } catch (Exception e0) {
      logger.error("ServletInoltro::doGet() - Attenzione! Si è verificato un errore (1). "+e0.toString());
      return;
    }

    try {
      initVu(req_area, req_cr, req_utente, req_ruolo);
      vu.connect();
      vu.setDbOpRestaConnessa(true); //Questo evita che ogni volta che passo la vu alle classi del dmserver, es. AggiornaDocumento
      //la classe rifaccia connect e/o disconnect.... a quello ci penso io
    } catch (Exception e) {
      corpoHtml += "<span class='AFCErrorDataTD'>"+"Attenzione! Errore in fase connessione. Errore:  ["+ e.toString()+"].</span>";
      logger.error("ServletModulistica::inoltro() Errore in connessione enviroment - Errore:  ["+ e.toString()+"]",e);
      return;
    }

    try {
      //Carico i dati sulla RepositoryTemp
      if (req_cm != null) {
        try {
          query = "SELECT * "+
                  "FROM OPERAZIONI_DI_INOLTRO OP "+
                  "WHERE OP.AREA ='"+req_area+"' "+
                  "  AND OP.CODICE_MODELLO = '"+ req_cm +"' ";
          dbOpSQL = vu.getDbOp();
          dbOpSQL.setStatement(query);
          dbOpSQL.execute();
          rst = dbOpSQL.getRstSet();
          if (rst.next()) {
            query = "SELECT * "+
                    "FROM OPERAZIONI_DI_INOLTRO OP "+
                    "WHERE OP.AREA ='"+req_area+"' "+
                    "  AND OP.CODICE_MODELLO = '"+ req_cm +"' " +
                    "  AND NOT EXISTS "+
                    "   (SELECT 1 " +
                    "      FROM LOG_INOLTRI LI " +
                    "     WHERE LI.ID_OP = OP.ID_OP " +
                    "       AND LI.AREA = '"+req_area+"' " +
                    "       AND LI.STATO = 1 " +
                    "       AND LI.CODICE_RICHIESTA = '"+req_cr+"') " +
                    "ORDER BY SEQUENZA ";
        } else {
          query = "SELECT * "+
                  "FROM OPERAZIONI_DI_INOLTRO OP "+
                  "WHERE OP.AREA ='"+req_area+"' "+
                  "  AND OP.ID_TIPO_PRATICA = '"+ ricavaIdTipoPratica(req_ctp,req_area)+"' " +
                  "  AND NOT EXISTS "+
                  "   (SELECT 1 " +
                  "      FROM LOG_INOLTRI LI " +
                  "     WHERE LI.ID_OP = OP.ID_OP " +
                  "       AND LI.AREA = '"+req_area+"' " +
                  "       AND LI.STATO = 1 " +
                  "       AND LI.CODICE_RICHIESTA = '"+req_cr+"') " +
                  "ORDER BY SEQUENZA ";
        }
        qMod = "SELECT '"+req_cm+"'"+
                      "  FROM DUAL ";
        } catch (Exception e) {
        }
      } else {
          query = "SELECT * "+
                  "FROM OPERAZIONI_DI_INOLTRO OP "+
                  "WHERE OP.AREA ='"+req_area+"' "+
                  "  AND OP.ID_TIPO_PRATICA = '"+ ricavaIdTipoPratica(req_ctp,req_area)+"' " +
                  "  AND NOT EXISTS "+
                  "   (SELECT 1 " +
                  "      FROM LOG_INOLTRI LI " +
                  "     WHERE LI.ID_OP = OP.ID_OP " +
                  "       AND LI.AREA = '"+req_area+"' " +
                  "       AND LI.STATO = 1 " +
                  "       AND LI.CODICE_RICHIESTA = '"+req_area+"') " +
                  "ORDER BY SEQUENZA ";

        qMod = "SELECT M.CODICE_MODELLO "+
                      "  FROM MODELLI_TIPI_PRATICHE M, "+
                      "       TIPI_PRATICHE P "+
                      " WHERE M.ID_PRATICA = P.ID_TIPO_PRATICA "+
                      "   AND M.AREA = P.AREA "+
                      "   AND P.AREA = '"+req_area+"' " +
                      "   AND P.CODICE_TIPO_PRATICA = '"+req_ctp+"'";
      }

     try {

        dbOpSQL = vu.getDbOp();
        dbOpSQL.setStatement(query);
        dbOpSQL.execute();
        rst = dbOpSQL.getRstSet();

       List<InoltroStructInoltri> listaInoltri = new ArrayList<InoltroStructInoltri>();
       while (rst.next()) {
         listaInoltri.add(new InoltroStructInoltri(rst.getString("ID_OP"), rst.getString("CLASSNAME"),rst.getString("DSN"),rst.getClob("PARAMETRI")));
       }

        if (listaInoltri.size()==0) {
            throw new Exception("Inoltro::genera() - Attenzione! Impossibile trovare dati relativi all'inoltro con AREA= "+req_area);
        }

        for(int indexInoltro = 0;indexInoltro < listaInoltri.size();indexInoltro++) {
          pidOp  = listaInoltri.get(indexInoltro).getIdOp();
          pClassName = listaInoltri.get(indexInoltro).getClassName();
          sDsn = listaInoltri.get(indexInoltro).getDsn();
          Clob clobParametro = listaInoltri.get(indexInoltro).getParametri();

          if ( sDsn == null) {
            sDsn = "";
          }
          dbOpMod = vu.getDbOp();
          dbOpMod.setStatement(qMod);
          dbOpMod.execute();
          rstMod = dbOpMod.getRstSet();
          List<String> listaMod = new ArrayList<String>();
          while (rstMod.next()) {
            listaMod.add(rstMod.getString(1));
          }

          for(int indexListaMod = 0;indexListaMod <listaMod.size(); indexListaMod++) {
            cm = listaMod.get(indexListaMod);
            iddoc = ricercaIdDocumento(req_area, req_cr, cm);
            if (!leggiValori(request,iddoc, cm, vu.getDbOp())) {
              logger.error("Inoltro::genera() - Attenzione! Si è verificato un errore in fase di lettura valori!");
            }
          }

          queryIns = "SELECT 1 FROM LOG_INOLTRI " +
              "WHERE ID_OP = :ID_OP " +
              "  AND AREA = '"+req_area+"' " +
              "  AND CODICE_RICHIESTA = '"+req_cr+"' ";
          dbOpSQL.setStatement(queryIns);
          dbOpSQL.setParameter(":ID_OP",pidOp);
          dbOpSQL.execute();
          rstIns = dbOpSQL.getRstSet();
          if (!rstIns.next()) {
            queryIns = "INSERT INTO LOG_INOLTRI " +
                "(CODICE_RICHIESTA, AREA, ID_OP) " +
                " VALUES ('"+req_cr+"', '"+req_area+"', :ID_OP) ";
            dbOpSQL.setStatement(queryIns);
            dbOpSQL.setParameter(":ID_OP",pidOp);
            dbOpSQL.execute();
            dbOpSQL.commit();
          }

          // Lettura CLOB campo PARAMETRI
          long clobLen = clobParametro.length();

          if (clobLen < MAXLEN_PARAMETRI) {
            int i_clobLen = (int)clobLen;
            pParametri = clobParametro.getSubString(1, i_clobLen);
          } else {
            logger.error("ServletInoltro::doGet() - Attenzione! Si è verificato un errore. Il campo supera i "+MAXLEN_PARAMETRI+" caratteri.");
          }
          // ------------------------------------
          try {
            inoltro = (Inoltro)Class.forName(pClassName).newInstance();
            inoltro.init(pParametri);
            inoltro.setDSN(sDsn);
  /*          Parametri pPar = new Parametri();
            pPar.ALIAS = Parametri.ALIAS;
            pPar.SPORTELLO_DRIVER = Parametri.SPORTELLO_DRIVER;
            pPar.SPORTELLO_DSN = Parametri.SPORTELLO_DSN;
            pPar.USER = Parametri.USER;
            pPar.PASSWD = Parametri.PASSWD;*/
            InfoConnessione infoConnessione = new InfoConnessione(Parametri.ALIAS,
                Parametri.SPORTELLO_DSN,
                Parametri.USER,
                Parametri.PASSWD);
            inoltro.parametriRichiesta(pidOp, req_cr, req_area, req_cm,
               req_ctp, req_utente, req_allegati, infoConnessione);
            if (inoltro.inoltra()) {
              inoltriEseguiti = inoltriEseguiti + 1;
              queryIns = "UPDATE LOG_INOLTRI SET " +
                  "STATO = :STATO " +
                  " WHERE CODICE_RICHIESTA = '"+req_cr+"' "+
                  "   AND AREA = '"+req_area+"' "+
                  "   AND ID_OP = :ID_OP ";
              dbOpSQL.setStatement(queryIns);
              dbOpSQL.setParameter(":ID_OP",pidOp);
              dbOpSQL.setParameter(":STATO",1);
              dbOpSQL.execute();
              dbOpSQL.commit();
              if (!cancellaPreInoltro(pidOp, req_area, req_cr,vu.getDbOp()) ) {
                logger.error("Inoltro::genera() - Attenzione! Si è verificato un errore in fase di cancellazione valori!");
              }
            } else {
              if (!cancellaPreInoltro(pidOp, req_area, req_cr,vu.getDbOp()) ) {
                logger.error("Inoltro::genera() - Attenzione! Si è verificato un errore in fase di cancellazione valori!");
              }
              inoltriFalliti = inoltriFalliti + 1;
            }
          } catch (Exception ex) {
            logger.error("Inoltro::genera() - Attenzione! Problemi in fase di inoltro."+ex.toString());
          }
        }


      } catch (Exception ex) {
        logger.error("Inoltro::genera() - Attenzione! Si è verificato un errore (1). "+ex.toString());
      }

      Hashtable tmpl_args = new Hashtable();
      tmpl_args.put("filename",fileName);
      tmpl_args.put("path", paths);

      try {
        tmpl = new HTML.Template(tmpl_args);
      } catch (Exception e) {
        logger.error("Inoltro::genera() - Attenzione! Si è verificato un errore (1). "+e.toString());
      }

      if (pdo.equalsIgnoreCase("CC")) {
        tmpl.setParam("header",0);
      } else {
        tmpl.setParam("header",1);
      }
      if (req_provenienza.equalsIgnoreCase("SP")){
        tmpl.setParam("da_pratiche","1");
        tmpl.setParam("area", req_area);
        tmpl.setParam("codRich", req_cr);
        tmpl.setParam("codPratica", req_ctp);
        tmpl.setParam("rw", req_rw);

        if (pdo.equalsIgnoreCase("CC")) {
          tmpl.setParam("PraticheUrl", completeContextURL + "ServletPratiche.do");
        } else {
          tmpl.setParam("PraticheUrl", completeContextURL + "ServletPratiche");
        }
      }else{
          tmpl.setParam("da_pratiche","0");
      }

      // Messaggi finali
      tmpl.setParam("nonInizio_tipo2","1");

      tmpl.setParam("inoltriEseguiti",inoltriEseguiti);
      tmpl.setParam("inoltriFalliti",inoltriFalliti);

      if (inoltriEseguiti > 0) {
        tmpl.setParam("inoltri_eseguiti","1");
        tmpl.setParam("no_inoltri","0 ");
      } else {
        tmpl.setParam("inoltri_eseguiti","0");
      }
      if (inoltriFalliti > 0) {
        tmpl.setParam("inoltri_falliti","1");
        tmpl.setParam("no_inoltri","0 ");
      } else {
        tmpl.setParam("inoltri_falliti","0");
        if (inoltriEseguiti > 0) {
          try {
            if (req_cm != null) {
              iddoc = ricercaIdDocumento(req_area, req_cr, req_cm);
              AggiornaDocumento ad = new AggiornaDocumento(iddoc, vu);
              ad.settaCodiceRichiesta(req_cr);
              ad.salvaDocumentoCompleto();
            }
          } catch (Exception e) {
             logger.error("ServletInoltro::doGet() - "+e.toString());
          }
        } else {
          tmpl.setParam("no_inoltri","1 ");
        }
      }

      corpoHtml = tmpl.output();
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
   * ricavaIdTipoPratica()
   * Dato il codice del tipo pratica, restituisce l'ID_TIPO_PRATICA.
   */
  private String ricavaIdTipoPratica(String codice_tp, String area) {
    IDbOperationSQL  dbOp = null;
    ResultSet       rst = null;
    String          query;
    String          result = null;
    
    try {
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);

      if (codice_tp == null) {
        query = "SELECT id_tipo_pratica "+
                "FROM   tipi_pratiche "+
                "WHERE  area = '"+ area +"' and" +
                " is_default = '1'";
      } else {
        query = "SELECT id_tipo_pratica "+
                "FROM   tipi_pratiche "+
                "WHERE  codice_tipo_pratica = '"+ codice_tp +"'";
      }
              
      dbOp.setStatement(query);
      dbOp.execute();
      rst = dbOp.getRstSet();

      if (rst.next())
        result = rst.getString("id_tipo_pratica");
      free(dbOp);
      
    } catch (Exception ex) {
      free(dbOp);
      logger.error("ServletInoltro::ricavaIdTipoPratica() - Attenzione! Si è verificato un errore: "+ex.toString());
    }

    return result;
  }

  /**
   * 
   */
  private String ricercaIdDocumento (String ar, String cr, String cm) {
    Vector      ll;
    String      iddoc = null;
    String      id_tipodoc = ricavaIdtipodoc(ar,cm);

    try {
      RicercaDocumento rd = new RicercaDocumento(id_tipodoc,vu);
      rd.settaFileName(noBody);
      rd.settaCodiceRichiesta(cr);
      ll = rd.ricercaBozza();
      if(ll.size() == 1) {
        iddoc = (String)ll.firstElement();
      } else {
        if (ll.size() > 0) {
          logger.error("Inoltro::ricercaIdDocumento() - Attenzione:  Numero documenti trovati "+ ll.size() );
          return "-1";
        }
      }
    }
      catch (Exception e) {
      logger.error("Inoltro::ricercaIdDocumento() - Errore:  ["+ e.toString()+"]");
    }
    return iddoc;
  }

  /**
   * 
   */
  private void scriviPreInoltro (String idOp, String ar, String cr, String cm, String dato, String valore, IDbOperationSQL dbOperationSQL) {
    IDbOperationSQL  dbOpIns = null;
    IDbOperationSQL  dbOpSel = null;
    ResultSet       rs;
    String          querySel, queryIns;

    querySel = "SELECT 1 FROM PRE_INOLTRO" + 
            " WHERE AREA = :AREA AND CODICE_RICHIESTA = :CR AND CODICE_MODELLO = :CM " +
            " AND DATO = :DATO AND ID_OP = "+idOp;

    try {
      dbOpSel = dbOperationSQL;
      dbOpSel.setStatement(querySel);
      dbOpSel.setParameter(":AREA",ar);
      dbOpSel.setParameter(":CR",cr);
      dbOpSel.setParameter(":CM",cm);
      dbOpSel.setParameter(":DATO",dato);
      dbOpSel.execute();
      rs = dbOpSel.getRstSet();
      if (valore.length() == 0) {
        queryIns = "DELETE PRE_INOLTRO" +
                   " WHERE AREA = :AREA AND CODICE_RICHIESTA = :CR AND CODICE_MODELLO = :CM " +
                   " AND DATO = :DATO AND ID_OP = "+idOp;
      } else {
        if (!rs.next()) {
          queryIns = "INSERT INTO PRE_INOLTRO" +
                     " (ID_OP, AREA, CODICE_RICHIESTA, CODICE_MODELLO, DATO, VALORE, PROGRESSIVO) VALUES "+
                     " ("+idOp+", :AREA, :CR, :CM, :DATO, :VALORE, 1)";
        } else {
          queryIns = "UPDATE PRE_INOLTRO SET " +
                     " VALORE = :VALORE "+
                     " WHERE AREA = :AREA AND CODICE_RICHIESTA = :CR AND CODICE_MODELLO = :CM " +
                     " AND DATO = :DATO AND ID_OP = "+idOp;
        }
      }
      dbOpIns = dbOperationSQL;
      dbOpIns.setStatement(queryIns);
      dbOpIns.setParameter(":AREA",ar);
      dbOpIns.setParameter(":CR",cr);
      dbOpIns.setParameter(":CM",cm);
      dbOpIns.setParameter(":DATO",dato);
      if (valore.length() != 0) {
        dbOpIns.setParameter(":VALORE",valore);
      }
      dbOpIns.execute();
      dbOpIns.commit();
    }
      catch (Exception e) {
      logger.error("Inoltro::scriviPreInoltro() - Errore:  ["+dbOpIns.getStatementString()+" --"+ e.toString()+"]");
    }
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
  private boolean leggiValori(HttpServletRequest  request, String iddoc, String cm, IDbOperationSQL dbOpEsterna) {
    boolean         result = true;
    IDbOperationSQL  dbOp = null;
    ResultSet       rs = null;
    String          query;
    String          dato = null;
    String          valore = null;
    String          ar,
                    cr;

    ar   = request.getParameter("area");
    cr   = request.getParameter("cr");

    query = "SELECT DATO "+
            "FROM DATI_MODELLO "+
            "WHERE AREA = '"+ar+"' AND "+
            "CODICE_MODELLO = '"+cm+"' ";
    try {
      AccediDocumento ad = new AccediDocumento(iddoc,vu);
      ad.accediFullDocumento();
      dbOp = dbOpEsterna;
      dbOp.setStatement(query);
      dbOp.execute();
      rs = dbOp.getRstSet();

      List<String> listaDati = new ArrayList<String>();
      while (rs.next()) {
        listaDati.add(rs.getString(1));
      }

      for(int i=0;i<listaDati.size();i++) {
        dato   = listaDati.get(i);
        try {
          valore = ad.leggiValoreCampo(dato);
        } catch (Exception e1) {
          valore = "";
        }
        scriviPreInoltro(pidOp, ar, cr, cm, dato, valore,dbOpEsterna);
      }

    }
      catch (Exception e) {
      logger.error("ServletInoltro::leggiValori() - Errore:  ["+ e.toString()+"]");
      result = false;
    }
    return result;
  }

  /**
   * 
   */
  private boolean cancellaPreInoltro(String idOp, String ar, String cr, IDbOperationSQL dbOpEsterna) {
    boolean         result = true;
    IDbOperationSQL  dbOp = null;

    String query = "DELETE PRE_INOLTRO"+
                   " WHERE AREA = '" + ar + "'"+
                   " AND CODICE_RICHIESTA = '" + cr + "'"+
                   " AND ID_OP = " + idOp;

    try {
      dbOp = dbOpEsterna;
      dbOp.setStatement(query);
      dbOp.execute();
      dbOp.commit();
    } catch (Exception e) {
      logger.error("Inoltra::cancellaPreInoltro() - Errore:  ["+ e.toString()+"]");
      result = false;
    }
    return result;
  }

  private void initVu(String p_area,String p_cm,String p_user, String p_ruolo) {
    try {
      vu = new Environment(p_user, null, "MODULISTICA", "ADS", null, (new JNDIParameter("jdbc/gdm")));
      vu.setRuolo(p_ruolo);
    } catch (Exception e) {
      logger.error("Inoltra::initVu - "+e.toString());
    }
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
  private String caricaRuolo(String p_user) {
//    String          retval = null;
//    DbOperationSQL  dbOp = null;
//    ResultSet       rst = null;
//    String          query;
//
//    //RICAVO IL RUOLO DELL'UTENTE
//    query = "SELECT D.RUOLO"+
//            " FROM AD4_DIRITTI_ACCESSO D,"+
//            "      AD4_MODULI M"+
//            " WHERE M.MODULO = D.MODULO"+
//            "   AND M.PROGETTO = :PROGETTO"+
//            "   AND D.ISTANZA = :ISTANZA"+
//            "   AND UTENTE = :UTENTE";
//    try {
//      dbOp = new DbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
//
//      dbOp.setStatement(query);
//      dbOp.setParameter(":PROGETTO",Parametri.PROGETTO);
//      dbOp.setParameter(":ISTANZA",Parametri.ISTANZA);
//      dbOp.setParameter(":UTENTE",p_user);
//      dbOp.execute();
//      rst = dbOp.getRstSet();
//
//      if (rst.next() ) {
//         retval = rst.getString(1);
//      }
//      dbOp.close();
//      return retval;
//    } catch (Exception e) {
//      Util.writeErr("ServletModulistica::caricaRuolo ",e.toString());
//      return "";
//    }
//    if (p_user.equalsIgnoreCase("GUEST")) {
//      return "GUEST";
//    } else {
      return "GDM";
//    }
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
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);

      dbOp.setStatement(query);
      dbOp.setParameter(":AREA",ar);
      dbOp.setParameter(":CM",cm);
      dbOp.execute();
      rst = dbOp.getRstSet();

      if (rst.next() ) {
         idtipodoc = ""+rst.getInt("ID_TIPODOC");
         codmod = rst.getString("CODICE_MODELLO_PADRE");
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
      dbOp.close();
      return idtipodoc;
    
    } catch (Exception e) {
      logger.error("ServletPratiche::accessoModello - "+e.toString());
      return "";
    }
  }

}

class InoltroStructInoltri {
  private String idOp, className, dsn;
  private Clob parametri;

  public InoltroStructInoltri(String idOp, String className, String dsn, Clob parametri) {
    this.idOp = idOp;
    this.className = className;
    this.dsn = dsn;
    this.parametri = parametri;
  }

  public String getIdOp() {
    return idOp;
  }

  public void setIdOp(String idOp) {
    this.idOp = idOp;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public String getDsn() {
    return dsn;
  }

  public void setDsn(String dsn) {
    this.dsn = dsn;
  }

  public Clob getParametri() {
    return parametri;
  }

  public void setParametri(Clob parametri) {
    this.parametri = parametri;
  }
}