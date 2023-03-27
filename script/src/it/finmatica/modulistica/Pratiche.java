package it.finmatica.modulistica;
import javax.servlet.http.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.dmServer.modulistica.AccessoModulistica;
import it.finmatica.dmServer.management.*;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.Environment;
import org.apache.log4j.Logger;

public class Pratiche {
  private String      inifile = null;
  private String      remoteUser = null;
  private String      completeContextURL = null;
  private String      urlToImg = null;
  private String      filesep = File.separator;
  private String      serverScheme;
  private String      serverName;
  private String      serverPort;
  private String      corpo = null;
  private String      prov = null;
  private String      noBody = Global.NOBODY;
  private static Properties confLogger = null;
  private static Logger     logger = Logger.getLogger(Pratiche.class);
  
  public Pratiche(String sPath) {
    init(sPath);
  }
  
  /**
   * 
   */
  public void init(String sPath) {
//    super.init(config);
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
      logger.error("Upload::init() - Attenzione! si è verificato un errore: "+e.toString());
    }
  }
  
  public void genera(HttpServletRequest request, String pdo) {
    HttpSession session = request.getSession();

    String gruppo     = (String)session.getAttribute("user_group");
    String cr         = request.getParameter("cr");
    String area_tp    = null;
    String codice_tp  = request.getParameter("ctp");
    String lettura    = request.getParameter("rw");
    String id_tp      = null;
    String descr_tp   = null;

    String [] paths = {request.getSession().getServletContext().getRealPath("")+filesep+"template", "."}; 
    String fileName = "ServletPratiche.tmpl";

    if (lettura == null) {
      lettura = "C";
    }

    prov = pdo;

    // -------------------------------------------------
    // Inizializzo il completeContextURL che è del tipo:
    // http://hostName:port/Sportello/
    // ed anche l'URL verso le immagini
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
    

    urlToImg = completeContextURL +"images/gdm/";
          
    // -----------------------------------
    // Chi è l'utente che fa la richiesta?
    // -----------------------------------
    String nominativo  = request.getRemoteUser();

    if (nominativo == null) {
      remoteUser      = (String)session.getAttribute("Utente");
    } else {
      remoteUser = cercaUtente(nominativo.toUpperCase());
    }

    if (remoteUser == null) {
      remoteUser = "";
    }
    if (remoteUser.length() == 0) {
      remoteUser = creaUtente(request.getSession());
    }
    remoteUser = remoteUser.toUpperCase();
  
    String ruolo = caricaRuolo(remoteUser);
    session.setAttribute("UtenteGDM",remoteUser);
    session.setAttribute("RuoloGDM",ruolo);

    // ---------------------------------
    // Qual'è il codice della richiesta?
    // ---------------------------------
    if (cr == null) {
      cr = "";
    }
    if (cr.length() == 0) {
      cr = calcolaNumeroRichiestaPratica(session, codice_tp);
    }
      

    // Costruzione pagina HTML
    Hashtable tmpl_args = new Hashtable();
    tmpl_args.put("filename",fileName);
    tmpl_args.put("path", paths);
    HTML.Template tmpl = null;
        
    try {
      tmpl = new HTML.Template(tmpl_args);
    } catch (Exception e) {
      logger.error("ServletPratica::genera() - Errore nel template!");
    }

    if (pdo.equalsIgnoreCase("CC")) {
      tmpl.setParam("header", "0");
    } else {
      tmpl.setParam("header", "1");
    }
    // ------------------------------------------------------------------------
    // Gestisco la richiesta: se non specifico il tipo pratica allora li elenco 
    // tutti, altrimenti inserisco una richiesta di pratica e passo ad elencare 
    // i modelli che ne fanno parte.
    // ------------------------------------------------------------------------
      if (codice_tp == null) {
        tmpl.setParam("elenco_pratiche", "1");
        nuovoElencaTipiPratiche(tmpl);
      } else {
        tmpl.setParam("elenco_pratiche", "0");
        id_tp    = ricavaIdTipoPratica(codice_tp);          // Ricavo l'id del tipo di pratica
        area_tp  = ricavaAreaPratica(codice_tp);            // Ricavo l'area del tipo di pratica
        descr_tp = ricavaDescrizioneTipoPratica(codice_tp); // Ricavo la descrizione del tipo di pratica

        if (loginPratica(area_tp, cr, remoteUser, gruppo, id_tp) == true) {
          String          servletProtocolloURL = null;
          String          servletInoltroURL = null;

          if (pdo.equalsIgnoreCase("CC")) {
            servletProtocolloURL = completeContextURL+"restrict/ServletProtocollo.do";
            servletInoltroURL = completeContextURL + "restrict/ServletInoltro.do";
          } else {
            servletProtocolloURL = completeContextURL+"ServletProtocollo";
            servletInoltroURL = completeContextURL + "ServletInoltro";
          }

          try {
          

            tmpl.setParam("utente", remoteUser);
            tmpl.setParam("area", area_tp);
            tmpl.setParam("tipoPratica", descr_tp);
            tmpl.setParam("codRich", cr);
      
            nuovoElencaModelliPratica(tmpl, request, area_tp, cr, id_tp, lettura);

            if (lettura.equals("W")) {
              tmpl.setParam("bottoni_azioni",1);
            } else {
              tmpl.setParam("bottoni_azioni",0);
            }

            tmpl.setParam("protocolloUrl", servletProtocolloURL);
            tmpl.setParam("inoltroUrl", servletInoltroURL);
            tmpl.setParam("codPratica", codice_tp);
            tmpl.setParam("prov", "SP");
            tmpl.setParam("lettura", lettura);
            tmpl.setParam("allegati", elencoAllegati(area_tp,cr));
                  
          }catch(Exception e) {
            logger.error("doget html path="+ paths[0]+" - Exception: " + e);
          }

      } else {
        tmpl.setParam("accesso_autorizzato", "0");
        logger.error("ServletPratica::doGet() - loginPratica fallita ");
      } 
    }
    corpo = tmpl.output();
    freeConn();
  }

  /**
   * loginPratica()
   * Test se l'utente collegato ha il diritto di accedere alla pratica.
   *
   * @author  Antonio Plastini
   * @return  true se il login ha successo, false altrimenti
   */
  boolean loginPratica(String p_area, String p_cr, String p_user, String p_gruppo, String p_id_tp) {
      return true;
  }
  
  /**
   * calcolaNumeroRichiestaPratica()
   * Calcola un codice univoco che identifica una richiesta di pratica. 
   * Memorizza anche il codice del tipo pratica.
   *
   * @author     Antonio Plastini
   * @parameter  httpSess è la sessione html con cui l'utente è connesso al server
   * @parameter  pCodiceTipoPratica è il tipo di pratica richiesto.
   */
   private String calcolaNumeroRichiestaPratica(HttpSession httpSess, String pCodiceTipoPratica) {
     return ("["+pCodiceTipoPratica+"]-"+httpSess.getId());
   }

  /**
   * ricavaIdTipoPratica()
   * Dato il codice del tipo pratica, restituisce l'ID_TIPO_PRATICA.
   */
  private String ricavaIdTipoPratica(String codice_tp) {
    IDbOperationSQL  dbOp = null;
    ResultSet       rst = null;
    String          query;
    String          result = null;
    
    try {
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);

/*      query = "SELECT id_tipo_pratica "+
              "FROM   tipi_pratiche "+
              "WHERE  codice_tipo_pratica = '"+ codice_tp +"'";*/
              
      query = "SELECT id_tipo_pratica "+
          "FROM   tipi_pratiche "+
          "WHERE  codice_tipo_pratica = :CODICE";
          
      dbOp.setStatement(query);
      dbOp.setParameter(":CODICE", codice_tp);
      dbOp.execute();
      rst = dbOp.getRstSet();

      if (rst.next())
        result = rst.getString("id_tipo_pratica");
      
    } catch (Exception ex) {
      corpo = "ServletPratica::ricavaIdTipoPratica() - Attenzione! Si è verificato un errore: "+ex.toString();
      logger.error("ServletPratica::ricavaIdTipoPratica() - Attenzione! Si è verificato un errore: "+ex.toString());
    }

    free(dbOp);
    return result;
  }

  /**
   * ricavaDescrizioneTipoPratica()
   * Dato il codice del tipo pratica, restituisce la DESCRIZONE_TIPO.
   */
  private String ricavaDescrizioneTipoPratica(String codice_tp) {
    IDbOperationSQL  dbOp = null;
    ResultSet       rst = null;
    String          query;
    String          result = null;
    
    try {
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);

/*      query = "SELECT descrizione_tipo "+
              "FROM   tipi_pratiche "+
              "WHERE  codice_tipo_pratica = '"+ codice_tp +"'";*/
              
      query = "SELECT descrizione_tipo "+
          "FROM   tipi_pratiche "+
          "WHERE  codice_tipo_pratica = :CODICE";
          
      dbOp.setStatement(query);
      dbOp.setParameter(":CODICE", codice_tp);
      dbOp.execute();
      rst = dbOp.getRstSet();

      if (rst.next())
        result = rst.getString("descrizione_tipo");
      
    } catch (Exception ex) {
      corpo = "ServletPratica::ricavaDescrizioneTipoPratica() - Attenzione! Si è verificato un errore: "+ex.toString();
      logger.error("ServletPratica::ricavaDescrizioneTipoPratica() - Attenzione! Si è verificato un errore: "+ex.toString());
    }

    free(dbOp);
    return result;
  }

  /**
   * ricavaAreaPratica()
   * Dato il codice del tipo pratica, restituisce l'area di appartenenza.
   */
  private String ricavaAreaPratica(String codice_tp) {
    IDbOperationSQL  dbOp = null;
    ResultSet       rst = null;
    String          query;
    String          result = null;
    
    try {
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);

/*      query = "SELECT area "+
              "FROM   tipi_pratiche "+
              "WHERE  codice_tipo_pratica = '"+ codice_tp +"'";*/
              
      query = "SELECT area "+
          "FROM   tipi_pratiche "+
          "WHERE  codice_tipo_pratica = :CODICE";
          
      dbOp.setStatement(query);
      dbOp.setParameter(":CODICE", codice_tp);
      dbOp.execute();
      rst = dbOp.getRstSet();

      if (rst.next())
        result = rst.getString("area");
      
    } catch (Exception ex) {
      corpo = "ServletPratica::ricavaAreaPratica() - Attenzione! Si è verificato un errore: "+ex.toString();
      logger.error("ServletPratica::ricavaAreaPratica() - Attenzione! Si è verificato un errore: "+ex.toString());
    }

    free(dbOp);
    return result;
  }

/**
   * elencaTipiPratiche()
   * Semplice elenco dei tipi di pratiche 
   */
  private void nuovoElencaTipiPratiche(HTML.Template tmpl) {
    IDbOperationSQL  dbOp = null;
    ResultSet       rst = null;
    String          query;

 
    
    try {
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);

      query = "SELECT id_tipo_pratica, "+
                     "area, "+
                     "descrizione_tipo, "+
                     "codice_tipo_pratica "+
              "FROM   tipi_pratiche ";
              
      dbOp.setStatement(query);
      dbOp.execute();
      rst = dbOp.getRstSet();

      String id = null;
      String area = null;
      String desc = null;
      String cod = null;
    
      Vector v = new Vector(); /*Marika*/
      
      while (rst.next()) {
        id = rst.getString("id_tipo_pratica");
        area = rst.getString("area");
        desc = rst.getString("descrizione_tipo");
        cod = rst.getString("codice_tipo_pratica");
        
        Hashtable h = new Hashtable();
        
        h.put("idPratica", id);
        h.put("area", area);
        h.put("descTipo", desc);
        h.put("codPratica",cod);
        
        v.addElement(h);
      }
      tmpl.setParam("lista_pratiche", v);
    } catch (Exception ex) {
      corpo = "ServletPratica::elencaPratiche() - Attenzione! Si è verificato un errore: "+ex.toString();
      logger.error("ServletPratica::elencaPratiche() - Attenzione! Si è verificato un errore: "+ex.toString());
    }

    free(dbOp);
  }
  
/**
   * elencaModelliPratica()
   * Metodo che elenca i modelli legati alla pratica specificata.
   * E' un elenco di link che rimandano ai modelli veri e propri; 
   * inoltre i modelli parzialmente inseriti (ci sono elementi in repositoryTemp)
   * vengono segnalati come tali.
   */
  private void nuovoElencaModelliPratica(HTML.Template tmpl,
                                         HttpServletRequest request, 
                                         String area, 
                                         String cr, 
                                         String id_tp,
                                         String lettura) {
    IDbOperationSQL  dbOp = null;
    ResultSet       rst = null;
    String          query;
    String          accesso = "";
    //String          ServletModulisticaURL = completeContextURL+"ServletModulistica";
    
    try {
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);

/*      query = "SELECT "+ 
                "mtp.id_mtp, "+
                "mtp.codice_modello "+
              "FROM "+ 
                "modelli_tipi_pratiche mtp,"+
                "modelli m "+
              "WHERE "+
                "m.area = mtp.area and "+
                "m.codice_modello = mtp.codice_modello and "+
                "mtp.id_pratica = '"+ id_tp +"' "+
              "GROUP BY "+
                "mtp.id_mtp, mtp.codice_modello";*/
              
      query = "SELECT "+ 
          "mtp.id_mtp, "+
          "mtp.codice_modello "+
        "FROM "+ 
          "modelli_tipi_pratiche mtp,"+
          "modelli m "+
        "WHERE "+
          "m.area = mtp.area and "+
          "m.codice_modello = mtp.codice_modello and "+
          "mtp.id_pratica = :ID_TP "+
        "GROUP BY "+
          "mtp.id_mtp, mtp.codice_modello";
        
      dbOp.setStatement(query);
      dbOp.setParameter(":ID_TP", id_tp);
      dbOp.execute();
      rst = dbOp.getRstSet();
              
      String cm = null;
      
      // Scorro i modelli della pratica
      String ServletModulisticaURL = null;
      if (prov.equalsIgnoreCase("CC")) {
        ServletModulisticaURL = completeContextURL+"restrict/ServletModulistica.do";
      } else {
        ServletModulisticaURL = completeContextURL+"ServletModulistica";
      }
      Vector v = new Vector(); /*Marika*/
      while (rst.next()) {
        cm = rst.getString("codice_modello");
       /*Marika*/
    
        Hashtable h = new Hashtable();
        accesso = accessoModello(area,cm,cr,remoteUser,lettura);
        h.put("modello_autorizzato",accesso);
        h.put("sporUrl", ServletModulisticaURL);
        h.put("toImgUrl", urlToImg);
        h.put("cMod", cm);
        h.put("area", area);
        h.put("prov", request.getParameter("ctp"));
        h.put("codRich", cr);
        h.put("lettura", lettura);

        if (isModelloVisitato(cr, cm, area)) {
          h.put("imgGif","visitato.gif");
          h.put("descVisita","Modello già visitato");
        } else {
          h.put("imgGif","nuovo.gif");
          h.put("descVisita","Modello nuovo");
        }
      v.addElement(h);
      }
      
      tmpl.setParam("lista_modelli", v);

      /*Marika*/
    
     
    } catch (Exception ex) {
      corpo = "ServletPratica::elencaPratiche() - Attenzione! Si è verificato un errore: "+ex.toString();
      logger.error("ServletPratica::elencaPratiche() - Attenzione! Si è verificato un errore: "+ex.toString());
    }
    free(dbOp);
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
  private boolean isModelloVisitato(String cr, String cm, String area) {
    Vector      ll;
//    Vector      lb;
//    Vector      lp;
    String      id_tipodoc = ricavaIdtipodoc(area,cm);
    try {
      Environment vu = new Environment(remoteUser, null, "MODULISTICA", "ADS", null, inifile);
      RicercaDocumento rd = new RicercaDocumento(id_tipodoc,vu);
      rd.settaFileName(noBody);
      rd.settaCodiceRichiesta(cr);
      ll = rd.ricerca();
      if(ll.size() == 1 ) {
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      return false;
    }

}

  /**
   * 
   */
/*  private String creaLinkInoltro_ProtocolloXML(HttpServletRequest request, 
                                               String area, 
                                               String cr, 
                                               String ctp) {

    String url = completeContextURL + "ServletInoltro?";

      String retval = "<input type='button' name='InAProtocollo' value='Protocollo' "+ 
        "onclick=\"location.href='"+ url + "type=1&area="+area+"&cr="+ cr +"&ctp="+ ctp +"';\">";

    return retval;
  }*/

  /**
   * 
   */
/*  private String creaLinkInoltro_PraticaXML(HttpServletRequest request, 
                                            String area, 
                                            String cr, 
                                            String ctp) {

    String url = completeContextURL + "ServletInoltro?";
      String retval = "<input type='button' name='InADSXML' value='ADSXML' "+ 
        "onclick=\"location.href='"+ url + "type=2&area="+area+"&cr="+ cr +"&ctp="+ ctp +"';\">";

    return retval;
  }*/

  /**
   * 
   */
/*  private String creaLinkAllega_File(HttpServletRequest request, 
                                     String codice_tp, 
                                     String area, 
                                     String cr) {

//    String rich_rev = request.getParameter("rrv");
    String lettura = request.getParameter("rw");
    String url = completeContextURL + "ServletAllega?";
      String retval = "<input type='button' name='Allega' value='Allegati' "+ 
        "onclick=\"location.href='"+ url +"ctp="+codice_tp+"&area="+area+"&cr="+
        cr + "&rw=" + lettura + "';\">";

    return retval;
  }*/

  /**
   * 
   */
  protected String creaUtente(HttpSession httpSess) {
    String ret  = "GUEST";
    httpSess.setAttribute("UtenteGDM",ret);
    httpSess.setAttribute("RuoloGDM",ret);
    return ret;
  }

  /**
   * 
   */
  private String elencoAllegati(String area, String cr) {
    IDbOperationSQL  dbOp = null;
    ResultSet       rst = null;
    String          query;
    String          retVal = null;
    
    try {
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);

      query = "SELECT * FROM ALLEGATI WHERE CODICE_RICHIESTA = '"+cr+
                  "' AND AREA = :AREA"+
                  " AND CODICE_ALLEGATO <> 'ADSXML' "+
                  " AND CODICE_ALLEGATO <> 'ProtocolloXML' ORDER BY CODICE_ALLEGATO ASC";
      dbOp.setStatement(query);
      dbOp.setParameter(":AREA", area);
      dbOp.execute();
      rst = dbOp.getRstSet();
      int i = 0;
      while (rst.next()) {
        if (i == 0) {
          retVal = rst.getString("CODICE_ALLEGATO");
        } else {
          retVal = retVal +"@#@"+ rst.getString("CODICE_ALLEGATO");
        }
        i = i + 1;
      }
      if (retVal != null){
        retVal = URLEncoder.encode(retVal,"windows-1252");
      }
    } catch (Exception ex) {
      logger.error("ServletPratica::elencoAllegati() - Attenzione! Si è verificato un errore: "+ex.toString());
      return null;
    }
    free(dbOp);
    
    return retVal;
  }

  /**
   * 
   */
  private String accessoModello(String p_area,String p_cm, String p_richiesta,String p_user, String p_lettura) {
    String          ruolo = null;
    String          sComp = null;
    IDbOperationSQL  dbOp = null;
    ResultSet       rst = null;
    String          query;

    //RICAVO IL RUOLO DELL'UTENTE
    query = "SELECT D.RUOLO"+
            " FROM AD4_DIRITTI_ACCESSO D,"+
            "      AD4_MODULI M"+
            " WHERE M.MODULO = D.MODULO"+
            "   AND M.PROGETTO = :PROGETTO"+
            "   AND D.ISTANZA = :ISTANZA"+
            "   AND UTENTE = :UTENTE";
    try {
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);

      dbOp.setStatement(query);
      dbOp.setParameter(":PROGETTO",Parametri.PROGETTO);
      dbOp.setParameter(":ISTANZA",Parametri.ISTANZA);
      dbOp.setParameter(":UTENTE",p_user);
      dbOp.execute();
      rst = dbOp.getRstSet();

      if (rst.next() ) {
         ruolo = rst.getString(1);
      }
      dbOp.close();
    } catch (Exception e) {
      logger.error("ServletPratiche::accessoModello - "+e.toString());
      return "0";
    }

    try {
      Environment vu = new Environment(p_user, null, "MODULISTICA", "ADS", null, inifile);
      vu.setRuolo(ruolo);
      String id_tipodoc = ricavaIdtipodoc(p_area,p_cm);
      AccessoModulistica am = new AccessoModulistica(p_user,ruolo,id_tipodoc,p_richiesta,inifile);
      sComp = am.getMaxComp();
      if (sComp.equalsIgnoreCase("-1")) {
        am = new AccessoModulistica("GUEST","GUEST",id_tipodoc,p_richiesta,inifile);
        sComp = am.getMaxComp();
        if (sComp.equalsIgnoreCase("-1")) {
          return "0";
        }
      } 
      if (sComp.equalsIgnoreCase("L") && p_lettura.equalsIgnoreCase("W")) {
        return "0";
      }
    } catch (Exception e) {
      logger.error("ServletPratiche::accessoModello - "+e.toString());
      return "0";
    }
    return "1";
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

  /**
   * 
   */
  protected String cercaUtente(String nominativo) {
    String          retval = null;
    IDbOperationSQL  dbOp = null;
    ResultSet       rst = null;
    String          query;
    
    //RICAVO IL RUOLO DELL'UTENTE
    query = "select utente from ad4_utenti where nominativo = :NOMINATIVO";
    try {
      dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);

      dbOp.setStatement(query);
      dbOp.setParameter(":NOMINATIVO", nominativo);
      dbOp.execute();
      rst = dbOp.getRstSet();

      if (rst.next() ) {
         retval = rst.getString(1);
      }
      free(dbOp);
      return retval;
    } catch (Exception e) {
      logger.error("ServletModulistica::cercaUtente - "+e.toString());
      free(dbOp);
      return "";
    }
  }

  /**
   * 
   */
  private String caricaRuolo(String p_user) {
//    if (p_user.equalsIgnoreCase("GUEST")) {
//      return "GUEST";
//    } else {
      return "GDM";
//    }
  }

}
