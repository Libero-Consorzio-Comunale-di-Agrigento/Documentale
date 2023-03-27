package it.finmatica.modulistica;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.*;

import it.finmatica.modulistica.modulisticapack.*;
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.jfc.io.LetturaScritturaFileFS;
import it.finmatica.dmServer.competenze.*;
import it.finmatica.dmServer.util.*;
import it.finmatica.dmServer.*;
import it.finmatica.dmServer.modulistica.AccessoModulistica;
import it.finmatica.dmServer.management.*;
import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.util.Global;
import java.net.*;
import it.finmatica.GDMTreeViewWeb.TreeView.*;
import org.apache.log4j.Logger;
   
public class Editing {
  private String      inifile = null;
  private String      filesep = File.separator;
  private Environment vu;
  private String      id_tipodoc = null;
  private String      ruolo = null;
  private String      id_session = null;
  private String      corpoHtml = "";
  private String      lettura = "";
  private String      pdo = "";
  private String      myPathTemp = "";
  private static Logger     logger = Logger.getLogger(Editing.class);

  /**
   * Crea e inizializza un nuovo oggetto Editing
   * @param sPath Path reale della Servlet 
   */
  public Editing(String sPath) {
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
      Parametri.leggiParametriStandard(inifile);
      SessioneDb.getInstance().addAlias(Parametri.ALIAS, Parametri.SPORTELLO_DRIVER);
    } catch(Exception e) {
      loggerError("ServletEditing::init() - Attenzione! si è verificato un errore: "+e.toString(),e);
    }
  }
  
  /**
   * calcolaNumeroRichiesta()
   * Calcola un numero univoco di richiesta per l'area in questione
   */
   private String calcolaNumeroRichiesta(HttpSession httpSess, String parea) {
     return (parea+"-"+httpSess.getId());
   }

  /**
   * calcolaScadenza()
   * Funzione privata per il calcolo del Timestamp corrispondente alla scadenza di un dato modulo
   *
   * @parameter durata è una stringa che deve rappresentare un intero che sarà il numero di giorni di
   *            validità da associare ad un dato modulo (tutti i campi di quel modulo avranno una
   *            durata di validità dipendente dal valore di questo parametro).
   * @author    Adelmo Gentilini
   * @return    Timestamp che mi rappresenta la data di scadenza del modulo; se il valore di ritorno è
   *            un valore null allora il dato ha scadenza infinita (si suppone coi in tutti quei casi
   *            in cui verrà prima o poi chiamata la BuildXML che provvederà a vuotare la tabella da
   *            tuti irecord corrispondenti ai dati richiesti.
   */
   private Timestamp calcolaScadenza(String durata) {
     int durataInt;
     if (durata == null) {
       return null; //** Exit point: se la durata è null torno null
     }

     try {
       durataInt = (new Integer(durata)).intValue();
     } catch (Exception ex) {
       return null; //** Exit point: se il numero non è traducibile ritorno null
     }

     Calendar scad = Calendar.getInstance();   // today
     scad.add(Calendar.DATE, durataInt);
     return (new Timestamp(scad.getTimeInMillis()));
   }

  /**
   * 
   */
  private String caricaRuolo(String p_user) {
    return "GDM";
  }

  /**
   * 
   */
  private void cercaRichiesta(String area, String codice, IDbOperationSQL dbOpEsterna) {
    boolean         result = false;
    IDbOperationSQL  dbOp = null;
    ResultSet       rs = null;

/*    String querySel = "SELECT 1"+
                      "  FROM RICHIESTE "+
                      " WHERE AREA = '" + area + "'"+
                      " AND CODICE_RICHIESTA = '" + codice + "'";
    
    String queryIns = "INSERT INTO richieste (codice_richiesta, area,id_tipo_pratica,  data_inserimento, data_scadenza) "+
                      "VALUES ('"+ codice +"','"+ area +"', null, sysdate, null)";*/

    String querySel = "SELECT 1"+
        "  FROM RICHIESTE "+
        " WHERE AREA = :AREA"+
        " AND CODICE_RICHIESTA = :CR";

    String queryIns = "INSERT INTO richieste (codice_richiesta, area,id_tipo_pratica,  data_inserimento, data_scadenza) "+
        "VALUES (:CR,:AREA, null, sysdate, null)";

    try {
      dbOp = dbOpEsterna;
      dbOp.setStatement(querySel);
      dbOp.setParameter(":AREA",area);
      dbOp.setParameter(":CR",codice);
      dbOp.execute();
      rs = dbOp.getRstSet();
      result = rs.next();
      if (!result) {
        dbOp.setStatement(queryIns);
        dbOp.setParameter(":AREA",area);
        dbOp.setParameter(":CR",codice);
        dbOp.execute();
        dbOp.commit();
      }
    } catch (Exception e) {
      loggerError("ServletModulistica::cercaRichiesta() - Errore:  ["+ e.toString()+"]",e);
      corpoHtml += "<H2>Attenzione! Errore interno in fase di ricerca codice richiesta.</h2>";
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        corpoHtml += e.toString();
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        corpoHtml += e.getStackTrace().toString();
      }
    }
  }

  /**
   * 
   */
  private String costruisciModuloLettura(HttpServletRequest  request,
                                  String              queryURL, IDbOperationSQL dbOpEsterna) {
    HttpSession     httpSess;
    ModelloHTMLIn   md = null;
    ModelloHTMLOut  mdOut = null;
    ListaControlli  controlli = null;
    String          ar, cm, cr, sc;

    try {
      ar = request.getParameter("area");
      cr = request.getParameter("cr");
      cm = request.getParameter("cm");
      sc = request.getParameter("sc");  // GIORNI DI VALIDITA' DEI DATI (sc sta per scadenza ma indica una durata)
      httpSess = request.getSession();
      if (cr == null) {   // Se necessario creo un codice richiesta
        cr = (String)httpSess.getAttribute("key");
      }

      // ***** CONTROLLI *****
      controlli = (ListaControlli)httpSess.getAttribute("listaControlli");
      
      if (controlli == null) {
        // Inizializzo una variabile di tipo ListaControlli a livello di sessione
        // che servirà a contenere i puntatori ai vari controlli letti dal database.
        // In questo modo i singoli oggetti Controllo verranno inizializzati solo
        // una volta per ogni coppia di "area , controllo" (pk) e referenziati
        // nelle liste dei singoli oggetti di volta in volta.
        controlli = new ListaControlli();
        httpSess.setAttribute("listaControlli", controlli);
      }

      //Modifica di MMA.
      if (sc == null) {
        sc = "1";
      }

      if (cr == null) {
        cr = calcolaNumeroRichiesta(httpSess, ar);
      }

      // Memorizzo il codice richiesta nella variabile di sessione 'key'
      httpSess.setAttribute("key", cr);

      preCaricamentoDati(request, ar, id_session, cm,dbOpEsterna);
      md = new ModelloHTMLIn(request, ar, cm, cr, calcolaScadenza(sc),""+Calendar.getInstance().getTimeInMillis());
//      mdg.setUltimoAgg(aggData);
      md.interpretaModello(dbOpEsterna);
      mdOut = new ModelloHTMLOut(md);
      String corpoMod = mdOut.getPrivPRNValue(dbOpEsterna);
      corpoMod = corpoMod.replaceAll("</textarea>","");
      corpoMod = corpoMod.replaceAll("<textarea","<!--");
      return corpoMod;
    } catch(Exception e) {
      loggerError("ServletEditing::costruisciModuloLettura() - Attenzione! Si è verificato un errore: "+e.toString(),e);
      corpoHtml += "<h2>Attenzione! Si è verificato un errore.</h2><br><h4>Errore in fase di costruzione del modello.</h4>";
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        corpoHtml += e.toString();
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        corpoHtml += e.getStackTrace().toString();
      }
      return "";
    }
  }

  /**
   * 
   */
  private String creaUtente(HttpSession httpSess) {
    String ret  = "GUEST";
    httpSess.setAttribute("UtenteGDM",ret);
    httpSess.setAttribute("RuoloGDM",ret);
    return ret;
  }

  /**
   *  Funzione che genera il codice HTML del Modello.
   *  Il nome della pagina che includerà il modello che deve 
   *  obbligatoriamente chiamarsi ServletEditing.
   *  Nel caso in cui il modello è incluso in una pagina 
   *  sviluppata in CodeCharge occcorre settare il parametro p_do
   *  a CC, se invece è incluso in una servlet settare il parametro p_do
   *  a stringa vuota.
   *  @param request Request della pagina che incorpora il modello
   *  @param p_do Indicatore per CodeCharge
   */
  public void genera(HttpServletRequest  request, String p_do) {
    IDbOperationSQL    dbOpSQL   = null;
    String            ta        = null;
    String            queryURL  = null;
    String            path      = null;
    String            nomeFile  = null;
    String            pathImm   = null;
    String            esiste    = null;
    String            us        = null;
    String            permesso  = "N";
    String            id_docE   = null;
    RicercaDocumento  r2        = null;
    Vector            ldoc      = null;
    int               j0, j1, j2, j3, j4;
    HttpSession       session  = request.getSession();
    String            cr      = request.getParameter("cr"),
                      ar      = request.getParameter("area"),
                      cm      = request.getParameter("cm"),
                      contr   = request.getParameter("controllo"),
                      nominativo = request.getRemoteUser(),
                      salva   = request.getParameter("salvaModello"),
                      filtro  = request.getParameter("filtro_glossario"),
                      casese  = request.getParameter("casesensitive"),
                      aggiorna  = request.getParameter("aggiorna"),
                      rw      = request.getParameter("rw"),
                      gdmWork = request.getParameter("gdmwork");



    if (gdmWork == null) {
      gdmWork = "";
    }

    session.setAttribute("valori_doc",null);
    session.setAttribute("listaDomini",null);
    session.setAttribute("listaControlli",null);
    session.setAttribute("listaControlliMod",null);
    session.setAttribute("gdm_nuovi_valori_doc",null);
    session.setAttribute("pdo",p_do);
    pdo = p_do;

    id_session = ar+'-'+session.getId();
    if (cr != null) {
      if (!id_session.equalsIgnoreCase(cr)) {
        id_session = ar+'-'+cr+'-'+session.getId();
      }
    }


    if (cr == null) {
      cr = "";
    }
    if (cr.length() == 0) {   // Se necessario creo un codice richiesta
      cr = (String)request.getSession().getAttribute("key");
    }
    if (cr == null) {   // Se necessario creo un codice richiesta
      cr = calcolaNumeroRichiesta(request.getSession(), ar);
      request.getSession().setAttribute("key",cr);
    }

    lettura = request.getParameter("rw");
    if (lettura == null) {
      lettura = "R";
    }

    if (nominativo == null) {
      us      = (String)session.getAttribute("Utente");
    } else {
      us = cercaUtente(nominativo.toUpperCase());
    }

    if (us == null) {
      us = "";
    }
    if (us.length() == 0) {
      us = creaUtente(request.getSession());
    } 
    us = us.toUpperCase();
    session.setAttribute("UtenteGDM",us);

  
    ruolo = caricaRuolo(us);
    session.setAttribute("RuoloGDM",ruolo);

    if (rw == null) {
      rw = "R";
    }
    
    if (contr == null) {
      contr = "";
    }

    if (filtro == null) {
      filtro = "";
    }

    if (casese == null) {
      casese = "";
    }

    if (aggiorna == null) {
      aggiorna = "Y";
    }

    myPathTemp = request.getSession().getServletContext().getRealPath("")+File.separator+"temp"+File.separator;
    myPathTemp += ar + File.separator + cm +  File.separator + cr;
    session.setAttribute("ed","Y");

    try {
      initVu(ar,cm,us);
      vu.connect();
      vu.setDbOpRestaConnessa(true); //Questo evita che ogni volta che passo la vu alle classi del dmserver, es. AggiornaDocumento
      //la classe rifaccia connect e/o disconnect.... a quello ci penso io
    } catch (Exception e) {
      corpoHtml += "<span class='AFCErrorDataTD'>"+"Attenzione! Errore in fase connessione. Errore:  ["+ e.toString()+"].</span>";
      loggerError("ServletEditing::genera() Errore in connessione enviroment - Errore:  ["+ e.toString()+"]",e);
      return;
    }

    try {
      id_tipodoc = ricavaIdtipodoc(ar,cm);
      cercaRichiesta(ar,id_session, vu.getDbOp());

      String ptemp = null;
      if (p_do.equalsIgnoreCase("CC")) {
        ptemp = request.getSession().getServletContext().getRealPath("")+filesep+".."+filesep+Parametri.APPLICATIVO+filesep+"template";
      } else {
        ptemp = request.getSession().getServletContext().getRealPath("")+filesep+"template";
      }
      String [] paths = {ptemp,"."};
      String fileName = "ServletEditing.tmpl";

      Hashtable tmpl_args = new Hashtable();
      tmpl_args.put("filename",fileName);
      tmpl_args.put("path", paths);

      HTML.Template tmpl = null;
      try {
        tmpl = new HTML.Template(tmpl_args);
      } catch (Exception e) {
        loggerError("errore - "+e.toString(),e);
      }
      if (p_do.equalsIgnoreCase("CC")) {
        tmpl.setParam("header", "0");
        tmpl.setParam("menuEditing", "SCC");
        tmpl.setParam("contx", request.getContextPath());
      } else {
        tmpl.setParam("menuEditing", "S");
        tmpl.setParam("header", "1");
        tmpl.setParam("contx", "");
      }


      try {
        r2 = new RicercaDocumento(id_tipodoc, ar, vu);
        r2.settaCodiceRichiesta(cr);
        ldoc = r2.ricerca();
        if (ldoc.size() > 0 ) {
          id_docE = (String)ldoc.firstElement();
          GD4_Status_Documento gd4s = new GD4_Status_Documento();
          gd4s.inizializzaDati(vu,id_docE);

          if (!gd4s.verificaStato(id_docE).equals(Global.STATO_BOZZA)) {
            rw = "R";
          }
        }
      } catch (Exception e) {
        loggerError("ServletEditing::doGet - "+e.toString(),e);
        corpoHtml += "<h2>Attenzione! Errore interno in fase di ricerca documento.</h2>";
        if (Parametri.DEBUG.equalsIgnoreCase("1")) {
          corpoHtml += e.toString();
        }
        if (Parametri.DEBUG.equalsIgnoreCase("2")) {
          corpoHtml += e.getStackTrace().toString();
        }
      }
      if (rw.equalsIgnoreCase("W")) {
        tmpl.setParam("scrittura", "1");
      } else {
        tmpl.setParam("scrittura", "0");
      }

      if (rw.equalsIgnoreCase("P") && gdmWork.length() == 0) {
        tmpl.setParam("stampa", "1");
      } else {
        tmpl.setParam("stampa", "0");
      }

      tmpl.setParam("stile", "0");

      //Inizio Treeview
      TreeView t = null;
      if (p_do.equalsIgnoreCase("CC")) {
        t = new TreeView("black","../common/images");
      }else{
        t = new TreeView();
      }

      t.setDefaultTarget("");
      t.setMarginLeft("12");
      t.setFont("verdana");
      t.setFontPt("7");
      if (casese.equalsIgnoreCase("S")) {
        t.settaFiltro(filtro,true);
        tmpl.setParam("case_sensitive", "checked");
      } else {
        t.settaFiltro(filtro,false);
        tmpl.setParam("case_sensitive", "");
      }
      tmpl.setParam("filtro_glossario", filtro);

      try {
        dbOpSQL = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);

        t.loadFromDb(dbOpSQL,"glossario_tree",ar);
        t.display();
        tmpl.setParam("albero", t.getOut());
      } catch(Exception ex) {
        loggerError("Errore albero",ex);
      }
      finally {
        free(dbOpSQL);
      }
      //Fine treeview

      path = request.getSession().getServletContext().getRealPath("")+filesep+"temp";
      path = path+filesep+ar+filesep+cm+filesep+cr;
      File fdir = new File(path);
      if (!fdir.exists()) {
        fdir.mkdirs();
      }

      tmpl.setParam("area", ar);
      tmpl.setParam("cr", cr);
      tmpl.setParam("id_sess", id_session);
      tmpl.setParam("cm", cm);

      try {
        if (salva != null) {
          String salvaNew = "";
          int ic;
          salva = URLDecoder.decode(salva,"ISO-8859-1");
          int up = 0;
          int posCod = salva.indexOf("mmacode(");
          while (posCod > -1) {
            salvaNew += salva.substring(up,posCod);
            up = salva.indexOf(")",posCod);
            String codiceCh = salva.substring(posCod+8,up);
            up = up + 1;
            ic = Integer.parseInt(codiceCh);
            salvaNew += (char)ic;
            posCod = salva.indexOf("mmacode(",up);
          }
          salvaNew += salva.substring(up);
          salva = nuovoPathImmagini(salvaNew,ar,cm,cr);
          caricaDati(request, salva);
          preCaricamentoDati(request, ar, id_session, cm,vu.getDbOp());
          salva = aggiornaCampi(request,salva,ar,cm,vu.getDbOp());
          if (aggiorna.equalsIgnoreCase("Y")) {
            permesso = "Y";
            if (ldoc.size()==0) {
              AggiungiDocumentoBody ad = new AggiungiDocumentoBody(id_tipodoc, ar, vu);
              ad.settaCodiceRichiesta(cr);
              ad.caricaDocumentoBody(salva, cm+".BODY");

              //Inserisco i valori dei campi tag
              j0= salva.indexOf("<a");
              while (j0 > -1) {
                j1 = salva.indexOf("</a>",j0);
                String sLink = salva.substring(j0,j1);
                if (sLink.indexOf("#campotag") > -1 || sLink.indexOf("#tagcampo") > -1) {
                  //Estraggo nome campo e valore
                  j2 = sLink.indexOf("title=")+7;
                  if (sLink.indexOf("\"",j2) > -1) {
                    if (sLink.indexOf("'",j2) > -1) {
                      if (sLink.indexOf("\"",j2) > sLink.indexOf("'",j2)) {
                        j3 = sLink.indexOf("'",j2);
                      } else {
                        j3 = sLink.indexOf("\"",j2);
                      }
                    } else {
                      j3 = sLink.indexOf("\"",j2);
                    }
                  } else {
                    j3 = sLink.indexOf("'",j2);
                  }
                  j4 = sLink.indexOf(">")+1;
                  String dato = sLink.substring(j2,j3);
                  String valore = sLink.substring(j4);
                  ad.aggiungiDatiBody(id_tipodoc,dato,valore);
                }
                j0= salva.indexOf("<a",j1);
              }

              //Inserisco le eventuali immagini come allegati
              j0 = salva.indexOf("<img");
              while (j0 > -1) {
                j2 = salva.indexOf("src=",j0);
                j1 = salva.indexOf("temp",j2);
                j4 = salva.indexOf("<img",j2);
                if (j4 == -1) {
                  j4 = j1+1;
                }
                if ((j1 > -1) && (j1 < j4)) {
                  j2 = salva.indexOf("\"",j1);
                  pathImm = salva.substring(j1,j2);
                  j3 = pathImm.lastIndexOf("/");
                  if (j3 > -1) {
                    nomeFile = pathImm.substring(j3+1);
                  } else {
                    nomeFile = pathImm;
                  }
                  nomeFile = nomeFile.replaceAll("%20"," ");
                  pathImm = request.getSession().getServletContext().getRealPath("") + filesep + pathImm.replaceAll("%20"," ");
                  LetturaScritturaFileFS f = new LetturaScritturaFileFS(pathImm);
                  ad.caricaAllegatiBody(f.leggiFile(), nomeFile, cm+".BODY");
                  j0 = salva.indexOf("<img",j1);
                } else {
                  j0 = salva.indexOf("<img",j2);
                }
              }
              try {
                ad.salvaDocumentoBozza();
              } catch (Exception e) {
                try { vu.getDbOp().rollback(); } catch (Exception ei) { }
                loggerError("ServletEditing::doGet - "+e.toString(),e);
                corpoHtml += "<h2>Attenzione! Errore interno in fase di inserimento.</h2>";
                if (Parametri.DEBUG.equalsIgnoreCase("1")) {
                  corpoHtml += e.toString();
                }
                if (Parametri.DEBUG.equalsIgnoreCase("2")) {
                  corpoHtml += e.getStackTrace().toString();
                }
              }

            } else {
              AggiornaDocumentoBody ad = new AggiornaDocumentoBody(id_docE, vu);
              //ad.cancellaTuttiAllegatiWithBody(cm+".BODY");
              ad.caricaDocumentoBody(salva, cm+".BODY");

              //Inserisco i valori dei campi tag
              j0= salva.indexOf("<a");
              while (j0 > -1) {
                j1 = salva.indexOf("</a>",j0);
                String sLink = salva.substring(j0,j1);
                if (sLink.indexOf("#campotag") > -1 || sLink.indexOf("#tagcampo") > -1) {
                  //Estraggo nome campo e valore
                  j2 = sLink.indexOf("title=")+7;
                  if (sLink.indexOf("\"",j2) > -1) {
                    if (sLink.indexOf("'",j2) > -1) {
                      if (sLink.indexOf("\"",j2) > sLink.indexOf("'",j2)) {
                        j3 = sLink.indexOf("'",j2);
                      } else {
                        j3 = sLink.indexOf("\"",j2);
                      }
                    } else {
                      j3 = sLink.indexOf("\"",j2);
                    }
                  } else {
                    j3 = sLink.indexOf("'",j2);
                  }
                  //        j3 = sLink.indexOf("href")-2;
                  j4 = sLink.indexOf(">")+1;
                  String dato = sLink.substring(j2,j3);
                  String valore = sLink.substring(j4);
                  ad.aggiornaDati(dato,valore);
                }
                j0= salva.indexOf("<a",j1);
              }

              //Inserisco le eventuali immagini come allegati
              j0 = salva.indexOf("<img");
              while (j0 > -1) {
                j2 = salva.indexOf("src=",j0);
                j1 = salva.indexOf("temp",j2);
                j4 = salva.indexOf("<img",j2);
                if (j4 == -1) {
                  j4 = j1+1;
                }
                if ((j1 > -1) && (j1 < j4)) {
                  j2 = salva.indexOf("\"",j1);
                  pathImm = salva.substring(j1,j2);
                  j3 = pathImm.lastIndexOf("/");
                  if (j3 > -1) {
                    nomeFile = pathImm.substring(j3+1);
                  } else {
                    nomeFile = pathImm;
                  }
                  nomeFile = nomeFile.replaceAll("%20"," ");
                  pathImm = request.getSession().getServletContext().getRealPath("") + filesep + pathImm.replaceAll("%20"," ");;
                  LetturaScritturaFileFS f = new LetturaScritturaFileFS(pathImm);
                  ad.caricaAllegatiBody(f.leggiFile(), nomeFile, cm+".BODY");
                  j0 = salva.indexOf("<img",j1);
                } else {
                  j0 = salva.indexOf("<img",j2);
                }
              }
              try {
                ad.salvaDocumentoBozza();
              } catch (Exception e) {
                try { vu.getDbOp().rollback(); } catch (Exception ei) { }
                loggerError("ServletEditing::doGet - "+e.toString(),e);
                corpoHtml += "<h2>Attenzione! Errore interno in fase di inserimento valori/immagini.</h2>";
                if (Parametri.DEBUG.equalsIgnoreCase("1")) {
                  corpoHtml += e.toString();
                }
                if (Parametri.DEBUG.equalsIgnoreCase("2")) {
                  corpoHtml += e.getStackTrace().toString();
                }
              }

            }
          }
          salva = salva.replaceAll("\"","&quot;");
          tmpl.setParam("ta", salva);
          tmpl.setParam("permesso",permesso);
        } else {
          String iddoc = null;
          if (ldoc.size()!=0) {
           iddoc = (String)ldoc.get(0);
          }
          esiste = (new DocUtil(vu)).getIdDocumento(id_tipodoc,cr);
          session.setAttribute("esiste_documento",esiste);
          if (loginModulistica(ar, cm, cr, us, contr, iddoc, esiste, vu.getDbOp())) {
            if (ldoc.size()!=0) {
              permesso = "Y";
              //preCaricamentoDati(request, ar, id_session, cm);
              AccediDocumento ad = new AccediDocumento(id_docE,vu);
              Vector v = ad.accediDocumentoAllegatiAndBody(cm+".BODY");
              if (v.size() > 0) {
                ta = (String)v.get(0);
                ta = nuovoPathImmagini(ta,ar,cm,cr);
                caricaDati(request, ta);
                preCaricamentoDati(request, ar, id_session, cm,vu.getDbOp());
                ta = aggiornaCampi(request,ta,ar,cm,vu.getDbOp());
                int i;
                for (i=1; i < v.size(); i++) {
                  nomeFile = ad.nomeOggettoFile((String)v.get(i));
                  InputStream in = ad.leggiOggettoFile((String)v.get(i));
                  LetturaScritturaFileFS fs = new LetturaScritturaFileFS(path+filesep+nomeFile);
                  fs.scriviFile(in);
                  in.close();
                }
              } else {
                leggiValori(request,id_docE,false);
                preCaricamentoDati(request, ar, id_session, cm,vu.getDbOp());
                ta = costruisciModuloLettura(request, queryURL,vu.getDbOp());
                ta = nuovoPathImmagini(ta,ar,cm,cr);
                caricaDati(request, ta);
                preCaricamentoDati(request, ar, id_session, cm,vu.getDbOp());
                ta = aggiornaCampi(request,ta,ar,cm,vu.getDbOp());
              }
            } else {
              String iddoc2 = ricercaIdDocumento(request);
              if (iddoc2 != null) {
                leggiValori(request,iddoc2,false);
              }
              preCaricamentoDati(request, ar, id_session, cm,vu.getDbOp());
              ta = costruisciModuloLettura(request, queryURL,vu.getDbOp());
              ta = nuovoPathImmagini(ta,ar,cm,cr);
              caricaDati(request, ta);
              preCaricamentoDati(request, ar, id_session, cm,vu.getDbOp());
              ta = aggiornaCampi(request,ta,ar,cm,vu.getDbOp());
            }
            if (lettura.equalsIgnoreCase("W")) {
              ta = ta.replaceAll("\"","&quot;");
            }
            if (ta.toLowerCase().indexOf("<head") > -1) {
               tmpl.setParam("stile", "1");
            }
            tmpl.setParam("ta", ta);
            tmpl.setParam("permesso",permesso);
          } else {
            logger.error("ServletEditing::doGet() - loginSportello fallita ");
            session.setAttribute("ed",null);
            session.setAttribute("valori_doc",null);
            session.setAttribute("listaDomini",null);
            session.setAttribute("gdm_nuovi_valori_doc",null);
            return;
          }
        }
      } catch (Exception e) {
        loggerError("ServletEditing::doGet - "+e.toString(),e);
        corpoHtml += "<h2>Attenzione! Errore interno in fase di creazione.</h2>";
        if (Parametri.DEBUG.equalsIgnoreCase("1")) {
          corpoHtml += e.toString();
        }
        if (Parametri.DEBUG.equalsIgnoreCase("2")) {
          corpoHtml += e.getStackTrace().toString();
        }
      }

      corpoHtml += tmpl.output();
      session.setAttribute("ed",null);
      session.setAttribute("valori_doc",null);
      session.setAttribute("listaDomini",null);
      session.setAttribute("gdm_nuovi_valori_doc",null);

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
   *  Funziona che ritorna il codice HTML del modello
   */
  public String getValue() {
    return corpoHtml;
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
  private void initVu(String p_area,String p_cm,String p_user) {
    try {
//      vu = new Environment(p_user, null, "MODULISTICA", "ADS", null, inifile);
      vu = new Environment(p_user, null, "MODULISTICA", "ADS", null, (new JNDIParameter("jdbc/gdm")));
      vu.setRuolo(ruolo);
    } catch (Exception e) {
      loggerError("ServletEditing::initVu - "+e.toString(),e);
      corpoHtml += "<H2>Attenzione! Errore interno in fase di inizializzazione Environment.</h2>";
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        corpoHtml += e.toString();
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        corpoHtml += e.getStackTrace().toString();
      }
    }
  }

  /**
   * loginSportello()
   * Test se l'utente collegato ha il diritto di richiedere il modulo attuale, quindi testo
   * se può accedere ai dati della attuale pratica in quella particolare area.
   *
   * @author  Marco Bonforte
   * @return  true se il login ha successo, false altrimenti
   */
  private boolean loginModulistica(String p_area,String p_cm, String p_richiesta,String p_user, String controllo, String iddoc, String esiste, IDbOperationSQL dbOpEsterna) {
    String          sComp = null;
    String          sAbil = "";

    try {
      if (iddoc == null) {
        if (esiste.length() == 0) {
          AccessoModulistica am = new AccessoModulistica(p_user,ruolo,id_tipodoc,p_richiesta,inifile);
          sComp = am.getMaxComp();
          if (!sComp.equalsIgnoreCase("C")) {
            am = new AccessoModulistica("GUEST","GDM",id_tipodoc,p_richiesta,inifile);
            sComp = am.getMaxComp();
            if (!sComp.equalsIgnoreCase("C")) {
              corpoHtml += "<h2>Attenzione! Login NEGATO all'utente "+p_user+"</h2><h4>area "+p_area+" richiesta "+p_richiesta+"</h4>";
              return false;
            }
          }
        } else {
          corpoHtml += "<h2>Attenzione! Login NEGATO all'utente "+p_user+"</h2><h4>area "+p_area+" richiesta "+p_richiesta+"</h4>";
          return false;
        }
      } else {
        sComp = "";
        if (lettura.equalsIgnoreCase("W")) {
          sAbil = "U";
        } else {
          sAbil = "L";          
        }
        UtenteAbilitazione ua = new UtenteAbilitazione(vu.getUser(), vu.getGruppo(), vu.getRuolo(), vu.getPwd(),  vu.getUser());
        Abilitazioni ab = new Abilitazioni("DOCUMENTI", iddoc, sAbil);
        if ((new GDM_Competenze(vu)).verifica_GDM_Compentenza(ua,ab) == 0) {
          corpoHtml += "<h2>Attenzione! Login NEGATO all'utente "+p_user+"</h2><h4>area "+p_area+" richiesta "+p_richiesta+"</h4>";
          return false;
        } else {
          return true;
        }
      }

      if (sComp.equalsIgnoreCase("C")) {
        if (!p_richiesta.equalsIgnoreCase(id_session)) {
          cercaRichiesta(p_area,p_richiesta,dbOpEsterna);
        }
      } else {
        corpoHtml += "<h2>Attenzione! Login NEGATO all'utente "+p_user+"</h2><h4>area "+p_area+" richiesta "+p_richiesta+"</h4>";
        return false;
      }

      if (controllo.equalsIgnoreCase("Y")) {
        return true;
      }

    } catch (Exception e) {
      loggerError("ServletEditing::loginSportello - "+e.toString(),e);
      corpoHtml += "<h2>Attenzione! Login NEGATO all'utente "+p_user+"</h2><h4>area "+p_area+" richiesta "+p_richiesta+"</h4>";
      return false;
    }
    return true;
  }

  /**
   * 
   */
  private String nuovoPathImmagini(String vecchio, String ar, String cm, String cr) {
    int j0, j1, j2, j3, j4, j5;// j6;
    String  nuovo = "";
    String  resto = "";

    j0 = vecchio.indexOf("<img");
    while (j0 > -1) {
      j1 = vecchio.indexOf("src=",j0);
      j2 = vecchio.indexOf("temp",j1);
      j4 = vecchio.indexOf("<img",j1);
      if (j4 == -1) {
        j4 = j2+1;
      }
      if ((j2 > -1) && (j2 < j4)) {
        j3 = vecchio.indexOf("\"",j2);
        String imgold = vecchio.substring(j2,j3);
        imgold = imgold.replaceAll("\\\\","/");
        j5 = imgold.lastIndexOf("/");
        nuovo = nuovo + vecchio.substring(0,j1);
        if (pdo.equalsIgnoreCase("CC")) {
          nuovo = nuovo + "src=\".."+filesep;
        } else {
          nuovo = nuovo + "src=\"";
        }
        nuovo = nuovo + "temp"+ filesep + ar + filesep + cm + filesep + cr + imgold.substring(j5);
        resto = vecchio.substring(j3);
        vecchio = resto;
        j0 = vecchio.indexOf("<img");
      } else {
        j0 = vecchio.indexOf("<img",j1);
      }
    }
    nuovo += vecchio;
    return nuovo;
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
  private void preCaricamentoDati(HttpServletRequest request, String pArea, String cr, String cm, IDbOperationSQL dbOpEsterna) {
    ListaDomini ld;
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
      ld.caricaDominiiDiArea(pArea, request, true,dbOpEsterna);
      ld.caricaDominiiDelModello(pArea, cm, request, true,dbOpEsterna);
      ld.caricaDominiiFormulaModello(pArea, cm, request,dbOpEsterna);
      request.getSession().setAttribute("listaDomini",ld);
    } catch (Exception ex) {
      loggerError("ServletModulistica::preCaricamentoDati() - Attenzione! Si è verificato un errore in fase di precaricamento: "+ex.toString(),ex);
    }
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
      dbOp = vu.getDbOp();

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
      return idtipodoc;
    
    } catch (Exception e) {
      loggerError("ServletEditing::ricavaIdtipodoc - "+e.toString(),e);
      corpoHtml += "<H2>Attenzione! Errore interno in fase di ricerca identificativo documento.</h2>";
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        corpoHtml += e.toString();
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        corpoHtml += e.getStackTrace().toString();
      }
      return "";
    }
  }

  /**
   * 
   */
  private String ricercaIdDocumento (HttpServletRequest  request) {
    Vector      ll;
    String          iddoc = null;
    String          cr;

    cr      = request.getParameter("cr");

    if (cr == null) {
      cr = (String)request.getSession().getAttribute("key");
    }
    try {
      RicercaDocumento rd = new RicercaDocumento(id_tipodoc,vu);
      rd.settaCodiceRichiesta(cr);
      ll = rd.ricerca();
      if(ll.size() == 1) {
        iddoc = (String)ll.firstElement();
      } 
    }
      catch (Exception e) {
      loggerError("ServletEditing::ricercaIdDocumento() - Errore:  ["+ e.toString()+"]",e);
    }
    return iddoc;
  }

  /**
   * 
   */
  private boolean leggiValori(HttpServletRequest  request, String iddoc, boolean binoltro) {
    boolean         result = true;
    String          cr;

    cr   = request.getParameter("cr");
    if (cr == null) {
      cr = (String)request.getSession().getAttribute("key");
    }

    try {
      AccediDocumento ad = new AccediDocumento(iddoc,vu);
      ad.accediDocumentoValori();
      request.getSession().setAttribute("valori_doc",ad);

    }
      catch (Exception e) {
      loggerError("ServletEditing::leggiValori() - Errore:  ["+ e.toString()+"]",e);
      result = false;
    }
    return result;
  }

  /**
   * 
   */
  private void freeConn() {
  }

  /**
   * 
   */
  private String cercaUtente(String nominativo) {
    String          retval = null;
    IDbOperationSQL  dbOp = null;
    ResultSet       rst = null;
    String          query;
    
    //RICAVO IL RUOLO DELL'UTENTE
//    query = "select utente from ad4_utenti where nominativo = '"+nominativo+"'";
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
      loggerError("ServletEditing::cercaUtente - "+e.toString(),e);
      free(dbOp);
      return "";
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
   * 
   * @param documento
   * @return
   */
  private String aggiornaCampi(HttpServletRequest request, String documento, String area, String cm, IDbOperationSQL dbOpEsterna) {
    IDbOperationSQL  dbOp  = null;
    Dominio         dp    = null;
    ResultSet       rst   = null;
    int             j0, j1, j2, j3, j4;
    String          dato    = "",
                    valore  = "",
                    tipoAcc = "",
                    calcol  = "",
                    query   = "",
                    retval  = "";
    
    j0= documento.indexOf("<a");
    j1 = 0;
    while (j0 > -1) {
      retval += documento.substring(j1,j0);
      j1 = documento.indexOf("</a>",j0);
      String sLink = documento.substring(j0,j1);
      if (sLink.indexOf("#campotag") > -1 || sLink.indexOf("#tagcampo") > -1) {
        //Estraggo nome campo e valore
        j2 = sLink.indexOf("title=")+7;
        if (sLink.indexOf("\"",j2) > -1) {
          if (sLink.indexOf("'",j2) > -1) {
            if (sLink.indexOf("\"",j2) > sLink.indexOf("'",j2)) {
              j3 = sLink.indexOf("'",j2);
            } else {
              j3 = sLink.indexOf("\"",j2);
            }
          } else {
            j3 = sLink.indexOf("\"",j2); 
          }
        } else {
          j3 = sLink.indexOf("'",j2);
        }
        j4 = sLink.indexOf(">")+1;
        dato = sLink.substring(j2,j3);
        valore = sLink.substring(j4);
        try {
/*          query = "SELECT TIPO_ACCESSO, CAMPO_CALCOLATO FROM DATI_MODELLO WHERE AREA = '"+area+"' "+
              " AND CODICE_MODELLO = '"+cm+"' AND DATO ='"+dato+"'";*/
          query = "SELECT TIPO_ACCESSO, CAMPO_CALCOLATO FROM DATI_MODELLO WHERE AREA = :AREA "+
                  " AND CODICE_MODELLO = :CM AND DATO = :DATO";
          dbOp = dbOpEsterna;
          dbOp.setStatement(query);
          dbOp.setParameter(":AREA", area);
          dbOp.setParameter(":CM", cm);
          dbOp.setParameter(":DATO", dato);
          dbOp.execute();
          rst = dbOp.getRstSet();
          if (rst.next()) {
            tipoAcc = rst.getString(1);
            calcol  = rst.getString(2);
            if (tipoAcc.equalsIgnoreCase("L") && calcol.equalsIgnoreCase("S")) {
              ListaDomini ld = (ListaDomini)request.getSession().getAttribute("listaDomini");
              String myVal = null;
              if (ld != null) {
                int numDom = ld.getNumDomini();
                int i = 0;
                while (i < numDom && myVal == null) {
                  dp = ld.getDominio(i);
                  if (dp.isDominioFormulaModello()) {
                    myVal = dp.getValore(dato);
                  }
                  i++;
                }
                if (myVal != null) {
                  valore = myVal;
                }
              }
            }
          }
        } catch (Exception e) {
          loggerError("ServletEditing::aggiornaCampi() - Errore:  ["+ e.toString()+"]",e);
        }
        if (lettura.equalsIgnoreCase("W")) {
          retval += "<a style='color: #000000; text-decoration: none; background-color: #FFFF00' title='"+dato+"' href='#campotag' >"+valore; 
        } else {
          retval += valore;
          j1 = j1 + 4;
        }
      }
      j0= documento.indexOf("<a",j1);
    }
    if (j1 > 0) {
      retval += documento.substring(j1); 
    } 
    if (retval.length() == 0) {
      retval = documento;
    }
    return retval;
  }

  private void caricaDati(HttpServletRequest request, String documento) {
    int     j0, j1, j2, j3, j4;
    String  campo   = "",
            valore  = "";
    Dati dati = new Dati();
    j0= documento.indexOf("<a");
    j1 = 0;
    while (j0 > -1) {
      j1 = documento.indexOf("</a>",j0);
      String sLink = documento.substring(j0,j1);
      if (sLink.indexOf("#campotag") > -1 || sLink.indexOf("#tagcampo") > -1) {
        //Estraggo nome campo e valore
        j2 = sLink.indexOf("title=")+7;
        if (sLink.indexOf("\"",j2) > -1) {
          if (sLink.indexOf("'",j2) > -1) {
            if (sLink.indexOf("\"",j2) > sLink.indexOf("'",j2)) {
              j3 = sLink.indexOf("'",j2);
            } else {
              j3 = sLink.indexOf("\"",j2);
            }
          } else {
            j3 = sLink.indexOf("\"",j2); 
          }
        } else {
          j3 = sLink.indexOf("'",j2);
        }
        //        j3 = sLink.indexOf("href")-2;
        j4 = sLink.indexOf(">")+1;
        campo  = sLink.substring(j2,j3);
        valore = sLink.substring(j4);
        dati.aggiungiDato(campo,valore);
      }
      j0= documento.indexOf("<a",j1);
    }
    request.getSession().setAttribute("gdm_nuovi_valori_doc",dati);
  }

}