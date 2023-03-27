package it.finmatica.modutils.multirecord;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.*;

import javax.servlet.http.*;

import jxl.CellView;
import jxl.Workbook;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import oracle.jdbc.OracleTypes;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.jfc.io.LetturaScritturaFileFS;
import it.finmatica.modutils.informazionicampo.InformazioniCampo;
import it.finmatica.textparser.AbstractParser;
import it.finmatica.instantads.WrapParser;

import org.apache.log4j.Logger;
import org.dom4j.*;


import it.finmatica.modulistica.domini.*;
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.modulistica.personalizzazioni.Personalizzazioni;
import it.finmatica.dmServer.management.IQuery;
import it.finmatica.dmServer.management.Profilo;
import it.finmatica.dmServer.motoreRicerca.ResultSetIQuery;
import it.finmatica.dmServer.util.HashMapSet;
import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.competenze.Abilitazioni;
import it.finmatica.dmServer.competenze.GDM_Competenze;
import it.finmatica.dmServer.competenze.UtenteAbilitazione; 

/**
 * Lo scopo del multirecord è quello di ottenere una visualizazione di informazioni
 * relative ad un select, in gruppi di n record per volta all'interno di pagine HTML,
 * con una rappresentazione opportunamente formattata mediante un template HTML.
 * Si può avere inoltre la possibilita di navigare attraverso i record(first prev next last),
 * e la possibilità  di definire delle colonne di ordinamento dinamico (quest'ultime 
 * vengono specificate nell'head del template). 
 * Nel caso in cui il multirecord sia inglobato all'interno di un modello, si ha la
 * possibilità  di indicare una o più colonne come dei link ai modello a cui il record fa 
 * riferiemnto. Se specificato in fase di pubblicazione del modello che ospita il multirecord,
 * questi può contenere un link per la creazione di un nuovo documento relativo al tipo
 * documento a cui il multirecord si rifierisce
 * @author Marco Bonforte
 * @version 3.1.4
 */       
public class Multirecord {
  protected final static  String BEGIN_CAMPO        = "<a href=\"#taglayout\""; 
  protected final static  String BEGIN_FUNCT        = "<a href=\"#tagfunc\""; 
  protected final static  String BEGIN_BLOCCO       = "&lt;<!-- ADSTBB -->"; 
  protected final static  String END_BLOCCO         = "<!-- ADSTBE -->&gt;"; 
  protected final static  String END_CAMPO          = "</a>"; 
  protected final static  String BEGIN_NAME_CAMPO   = "<!-- ADSPROPERTY"; 
  protected final static  String END_NAME_CAMPO     = "ADSFORMAT -->"; 
  protected final static  String BEGIN_NAME_BLOCCO  = "<!-- BLOCCOPROPERTY"; 
  protected final static  String END_NAME_BLOCCO    = "BLOCCOFORMAT -->"; 
  protected final static  String BEGIN_CORPO        = "<p><a href=\"#inizio\">--- Inizio corpo ---</a></p>"; 
  protected final static  String END_CORPO          = "<p><a href=\"#fine\">--- Fine corpo ---</a></p>"; 

  protected String      erroreMsg = "";
  protected String      headerHtml = "";
  protected String      corpoHtml = "";
  protected String      footerHtml = "";
  protected String      pathThemes = "";
  protected String      nomeBlocco = "";
  protected String      letturaScrittura = "W";
  protected String      campi = "";
  protected String      tabella = "";
  protected String      competenzeUtente = "";
  protected String      legame = "";
  protected String      ordinamento = "";
  protected String      nomeServlet = "";
  protected String      nomeServletAjax = "";
  protected String      nuovaServlet = "";
  protected String      pDo = "";
  protected boolean     isHorizontal = false;
  protected boolean     ajax = false;
  protected boolean     nested = false;
  protected boolean     aggiungi = false;
  protected boolean     protetto = false;
  protected boolean     ricerca = false;
  protected boolean     distinct = false;
  protected boolean     isTable = false;
  protected boolean     isMono = false;
  protected boolean     domini = false;
  protected boolean     nonCaricaDati = false;
  protected boolean     monoRecord = false;
  protected boolean     noCompetenze = false;
  protected boolean     bWait = false;
  protected String      risultato = "";
  protected int         numeroRecord = 1;
  protected int         recordTotali = 1;
  protected String[]    queryCategorie;
  protected String      queryMulti = "";
  protected String      queryPrepar = "";
  protected String      queryUrl = "";
  protected String      utenteGDM = "";
  protected String      sMvpg = "";
  protected String      sXml = "";
  protected String      sIstruzione = "";
  protected String      sJoin = "";
  protected String      sTipo = "";
  protected String      sWait = "";
  protected String      iddoc = "";
  protected String      codice_modello = "";
  protected Vector<String> iddocs = null;
  protected ListaDomini ld = null;
  protected BloccoNested bn = null;
  protected ListaBlocchiNested lblocchiN = null;
  protected LinkedList<String>  listaCampi  = new LinkedList<String>();
  protected LinkedList<String>  listaCate   = new LinkedList<String>();
  protected LinkedList<String>  listaArea   = new LinkedList<String>();
  protected LinkedList<String>  listaCm     = new LinkedList<String>();
  protected LinkedList<String>  listaDom    = new LinkedList<String>();
  protected LinkedList<String>  listaArDom  = new LinkedList<String>();
  protected LinkedList<String>  listaDati = new LinkedList<String>();
  protected HttpServletRequest pRequest = null;
  private  static Logger logger = Logger.getLogger(Multirecord.class);
  private boolean debuglog = logger.isDebugEnabled();
  private Properties dizionario = null;
  protected String      multilingua = "";

   public Multirecord() {
     
   }


  /**
   * 
   * @author Marco Bonforte
   * @version 3.1.4
   * @param Request HttpServletRequest della pagina che incorpora il multirecord.
   * @param NomeBlocco Nome di riferimento del blocco all'interno della pagina. 
   * @param Blocco Template del blocco multirecord
   * @param Campi Colonne della select utilizzata per la valorizzazione del multirecord. 
   * I nomi delle colonne devono coincidere con i nomi dei campi presenti nel template del blocco.
   * @param Tabelle Tabelle della select utilizzata per la valorizzazione del multirecord. 
   * Nel caso di più tabelle quaeste postranno essere seguita da un alias e dovranno essere
   * separate da una virgola (es. DATI D, MODELLI M, DATI_MODELLI DM).
   * @param Where Condizioni di where della select utilizzata per per la valorizzazione del multirecord. 
   * @param Ordinamento Condizioni di ordinamento della select utilizzata per per la valorizzazione del multirecord.
   * @param Record Numero di record da visualizzare per ogni pagina.
   */
  public Multirecord(HttpServletRequest Request, 
                     String NomeBlocco, 
                     String Blocco, 
                     String Campi, 
                     String Tabelle, 
                     String Where, 
                     String Ordinamento, 
                     int Record) throws Exception {
    init(Request, NomeBlocco, Blocco, Campi, Tabelle, Where, Ordinamento, Record, null);
  }

  /**
   * 
   * @author Marco Bonforte
   * @version 3.1.4
   * @param Request HttpServletRequest della pagina che incorpora il multirecord.
   * @param NomeBlocco Nome di riferimento del blocco all'interno della pagina. 
   * @param Blocco Template del blocco multirecord
   * @param Campi Colonne della select utilizzata per la valorizzazione del multirecord. 
   * I nomi delle colonne devono coincidere con i nomi dei campi presenti nel template del blocco.
   * @param Tabelle Tabelle della select utilizzata per la valorizzazione del multirecord. 
   * Nel caso di più tabelle quaeste postranno essere seguita da un alias e dovranno essere
   * separate da una virgola (es. DATI D, MODELLI M, DATI_MODELLI DM).
   * @param Where Condizioni di where della select utilizzata per per la valorizzazione del multirecord. 
   * @param Ordinamento Condizioni di ordinamento della select utilizzata per per la valorizzazione del multirecord.
   * @param Record Numero di record da visualizzare per ogni pagina.
   * @param ap Parser per la valorizzazione degli eventuali parametri presenti nella select.
   */
  public Multirecord(HttpServletRequest Request, 
                     String NomeBlocco, 
                     String Blocco, 
                     String Campi, 
                     String Tabelle, 
                     String Where, 
                     String Ordinamento, 
                     int Record, 
                     AbstractParser ap) throws Exception {
    init(Request, NomeBlocco, Blocco, Campi, Tabelle, Where, Ordinamento, Record, ap);
  }

  public Multirecord(HttpServletRequest Request, 
      String NomeBlocco, 
      String Blocco, 
      String Campi, 
      String Tabelle, 
      String Where, 
      String Ordinamento, 
      int Record, 
      AbstractParser ap,
      boolean isHorizontal) throws Exception {
    init(Request, NomeBlocco, Blocco, Campi, Tabelle, Where, Ordinamento, Record, ap, isHorizontal);
  }

  /**
   * 
   * @author Marco Bonforte
   * @version 3.1.4
   * @param Request HttpServletRequest della pagina che incorpora il multirecord.
   * @param NomeBlocco Nome di riferimento del blocco all'interno della pagina. 
   * @param Blocco Template del blocco multirecord
   * @param Campi Colonne della select utilizzata per la valorizzazione del multirecord. 
   * I nomi delle colonne devono coincidere con i nomi dei campi presenti nel template del blocco.
   * @param Tabelle Tabelle della select utilizzata per la valorizzazione del multirecord. 
   * Nel caso di più tabelle quaeste postranno essere seguita da un alias e dovranno essere
   * separate da una virgola (es. DATI D, MODELLI M, DATI_MODELLI DM).
   * @param Where Condizioni di where della select utilizzata per per la valorizzazione del multirecord. 
   * @param Ordinamento Condizioni di ordinamento della select utilizzata per per la valorizzazione del multirecord.
   * @param Record Numero di record da visualizzare per ogni pagina.
   * @param ap Parser per la valorizzazione degli eventuali parametri presenti nella select.
   */
  public Multirecord(HttpServletRequest Request, 
                     String NomeBlocco, 
                     String Blocco, 
                     String Where, 
                     int Record, 
                     AbstractParser ap) throws Exception {
    init(Request, NomeBlocco, Blocco, Where, Record, ap);
  }

  /**
   * 
   */
  public Multirecord(HttpServletRequest Request, 
                     String NomeBlocco, 
                     String Blocco, 
                     String Tipo, 
                     String Istruzione, 
                     String sXml, 
                     int Record) throws Exception {
    init(Request, NomeBlocco, Blocco, Tipo, Istruzione, sXml, Record);
  }
  
  /**
   * 
   */
  private void init(HttpServletRequest request, 
                    String pNomeBlocco,
                    String pCorpo,
                    String pTipo, 
                    String pIstruzione, 
                    String pXml, 
                    int pRecord) throws Exception {
    pRequest = request;
    sMvpg = request.getParameter("MVPG");
    if (sMvpg == null) {
      sMvpg = "";
    }
    utenteGDM = (String)request.getSession().getAttribute("UtenteGDM");
    if (utenteGDM == null) {
      utenteGDM = (String)request.getSession().getAttribute("Utente");
    }
    multilingua = (String)request.getSession().getAttribute("MULTILINGUA");
    if (multilingua == null) {
    	multilingua = "";
    }
    queryUrl = request.getContextPath()+request.getServletPath()+"?";
    String qryStr = request.getQueryString();
    if (qryStr == null) {
      qryStr = "";
    }
    int i = qryStr.indexOf("&urlCut");
    if (i > -1) {
      queryUrl += request.getQueryString().substring(0,i);
    } else {
      queryUrl += request.getQueryString();
    }
    queryUrl = queryUrl.replaceAll("&amp;","&");
    queryUrl = queryUrl.replaceAll("&","&amp;");

    isHorizontal = false;
    setNomeBlocco(pNomeBlocco);
    setLetturaScrittura(request);
    setCorpo(pCorpo);
    setIstruzione(pIstruzione);
    setXml(pXml);
    setTipo(pTipo);
    setOrdinamento(request, "");
    setListaCampi(corpoHtml);
    setNumeroRecord(pRecord);
    setNomeServlet(request);
  }
  
  private void init(HttpServletRequest request, String pNomeBlocco, String pCorpo, String pSelect, String pTabella, String pLegami, String pOrdinamento, int pRecord, AbstractParser ap) throws Exception {
    init(request, pNomeBlocco, pCorpo, pSelect, pTabella, pLegami, pOrdinamento, pRecord, ap, false);
  }

  /**
   * 
   */
  private void init(HttpServletRequest request, String pNomeBlocco, String pCorpo, String pSelect, String pTabella, String pLegami, String pOrdinamento, int pRecord, AbstractParser ap, boolean pIsHorizontal) throws Exception {
    pRequest = request;
    sMvpg = request.getParameter("MVPG");
    if (sMvpg == null) {
      sMvpg = "";
    }
    utenteGDM = (String)request.getSession().getAttribute("UtenteGDM");
    if (utenteGDM == null) {
      utenteGDM = (String)request.getSession().getAttribute("Utente");
    }
    multilingua = (String)request.getSession().getAttribute("MULTILINGUA");
    if (multilingua == null) {
    	multilingua = "";
    }
    queryUrl = request.getContextPath()+request.getServletPath()+"?";
    String qryStr = request.getQueryString();
    if (qryStr == null) {
      qryStr = "";
    }
    int i = qryStr.indexOf("&urlCut");
    if (i > -1) {
      queryUrl += request.getQueryString().substring(0,i);
    } else {
      queryUrl += request.getQueryString();
    }
    queryUrl = queryUrl.replaceAll("&amp;","&");
    queryUrl = queryUrl.replaceAll("&","&amp;");

//    siglaStile = (String)request.getSession().getAttribute("gdmsiglastile");
//    if (siglaStile == null) {
//      siglaStile = "AFC";
//    }
//    if (siglaStile.equalsIgnoreCase("")) {
//      siglaStile = ""+siglaStile+"";
//    }
    isHorizontal = pIsHorizontal;
    setNomeBlocco(pNomeBlocco);
    setLetturaScrittura(request);
    setCorpo(pCorpo);
    if (isHorizontal) {
      setCampi(null);
      setCodice_modello(pSelect);
    } else {
      setCampi(pSelect);
    }
    setTabella(pTabella);
    setLegame(pLegami);
    setOrdinamento(request, pOrdinamento);
    setListaCampi(corpoHtml);
    creaSelect();
    setNumeroRecord(pRecord);
    setPrepSelect(getSelect(), ap);
    setNomeServlet(request);
  }
  
  /**
   * 
   */
  private void init(HttpServletRequest request, String pNomeBlocco, String pCorpo, String pLegami, int pRecord, AbstractParser ap) throws Exception {
    pRequest = request;
    sMvpg = request.getParameter("MVPG");
    if (sMvpg == null) {
      sMvpg = "";
    }
    utenteGDM = (String)request.getSession().getAttribute("UtenteGDM");
    if (utenteGDM == null) {
      utenteGDM = (String)request.getSession().getAttribute("Utente");
    }
    multilingua = (String)request.getSession().getAttribute("MULTILINGUA");
    if (multilingua == null) {
    	multilingua = "";
    }
    queryUrl = request.getContextPath()+request.getServletPath()+"?";
    String qryStr = request.getQueryString();
    if (qryStr == null) {
      qryStr = "";
    }
    int i = qryStr.indexOf("&urlCut");
    if (i > -1) {
      queryUrl += request.getQueryString().substring(0,i);
    } else {
      queryUrl += request.getQueryString();
    }
    queryUrl = queryUrl.replaceAll("&amp;","&");
    queryUrl = queryUrl.replaceAll("&","&amp;");

    isHorizontal = false;
    setNomeBlocco(pNomeBlocco);
    setLetturaScrittura(request);
    setCorpo(pCorpo);
    setLegame(pLegami);
    setListaCampi(corpoHtml,true);
    setNumeroRecord(pRecord);
    setNomeServlet(request);
  }
  
  /**
   * La funzione ritorna la frase di select
   * 
   * @author Marco Bonforte
   * @version 1.0
   */
  public String getSelect() {
    return queryMulti;
  }

  public String getMVPG() {
    return sMvpg;
  }
  /**
   * La funzione ritorna la frase di select con gli eventuali parametri valorizzati
   * 
   * @author Marco Bonforte
   * @version 1.0
   */
  public String getPrepSelect() {
    return queryPrepar;
  }

  /**
   * La funzione ritorna le condizioni di Legame indicate nel template del Multirecord
   * 
   * @author Marco Bonforte
   * @version 1.0
   */
  public String getLegame() {
    return legame;
  }

  /**
   * La funzione ritorna le condizioni di Navigazione indicate nel template del Multirecord
   * 
   * @author Marco Bonforte
   * @version 1.0
   */
  public int getNumeroRecord() {
    return numeroRecord;
  }

  /**
   * La funzione ritorna l'ordinamento indicato nel template del Multirecord
   * 
   * @author Marco Bonforte
   * @version 1.0
   */
  public String getOrdinamento() {
    return ordinamento;
  }
  
  /**
   * La funzione setta l'istruzione javo o PL/SQL da eseguire per ottenere i record
   * del multirecord
   * 
   * @author Marco Bonforte
   * @version 1.0
   * @param newIstruzione  Istruzione di tipo Java o PL/SQL
   */
  private void setIstruzione(String newIstruzione) {
    sIstruzione = newIstruzione;
  }

  public void setJoin(String newJoin) {
    sJoin = newJoin;
  }

  public void setIdDoc(String newIddoc) {
    iddoc = newIddoc;
  }

 /**
   * La funzione setta la stringa XML che sarà  utilizzata come parametro di input
   * nell'istruzione Java o PL/SQL
   * 
   * @author Marco Bonforte
   * @version 1.0
   * @param newXml  nuova stringa xml
   */
  private void setXml(String newXml) {
    sXml = newXml;
  }

  /**
   * La funzione setta la condizione di legame da associare al template del Multirecord
   * 
   * @author Marco Bonforte
   * @version 1.0
   * @param newLegame  nuova condizione di legame
   */
  private void setLegame(String newLegame) {
    legame = newLegame.replaceAll("&lt;","<");
    legame = legame.replaceAll("&gt;",">");
  }

  /**
   * 
   */
  private void setLetturaScrittura(HttpServletRequest request) {
    if (request != null) {
      letturaScrittura = request.getParameter("rw");
      if (letturaScrittura == null) {
        letturaScrittura = "W";
      }
    }
  }

  /**
   * La funzione setta la condizione di ordinamento da associare al template del Multirecord
   * 
   * @author Marco Bonforte
   * @version 1.0
   * @param newOrdinamento  nuova condizione di ordinamento
   */
  public void setOrdinamento(String newOrdinamento) throws Exception {
    setOrdinamento(null,newOrdinamento);
  }
  /**
   * La funzione setta la condizione di ordinamento da associare al template del Multirecord
   * 
   * @author Marco Bonforte
   * @version 1.0
   * @param newOrdinamento  nuova condizione di ordinamento
   */
  public void setOrdinamento(HttpServletRequest request, String newOrdinamento) throws Exception {
    String myord = "";
    String oldCampo  = "";
    String tipoOrd  = "";

    try {
      if (request != null) {
        String campoOrd = request.getParameter(nomeBlocco+"_BLCORDC");

        if (campoOrd == null) {
          campoOrd = "";
        }
        if (tipoOrd == null) {
          tipoOrd = "";
        }
        if (campoOrd.equalsIgnoreCase("")) {
          campoOrd = (String)request.getSession().getAttribute(nomeBlocco+"_BLCORDC");
          tipoOrd  = (String)request.getSession().getAttribute(nomeBlocco+"_BLCORDT");
          if (campoOrd == null) {
            campoOrd = "";
          }
        } else {
          oldCampo = (String)request.getSession().getAttribute(nomeBlocco+"_BLCORDC");
          if (campoOrd.equalsIgnoreCase(oldCampo)) {
            tipoOrd  = (String)request.getSession().getAttribute(nomeBlocco+"_BLCORDT");
          } else {
            tipoOrd  = "";
          }
          if (tipoOrd.equalsIgnoreCase("")) {
            tipoOrd = "ASC";
          } else {
            if (tipoOrd.equalsIgnoreCase("ASC")) {
              tipoOrd = "DESC";
            } else {
              if (tipoOrd.equalsIgnoreCase("DESC")) {
                tipoOrd = "";
                campoOrd = "";
              }
            }
          }
          request.getSession().setAttribute(nomeBlocco+"_BLCORDC",campoOrd);
          request.getSession().setAttribute(nomeBlocco+"_BLCORDT",tipoOrd);
          request.getSession().setAttribute(nomeBlocco+"_BLCNAV","1");
        }
        if (!campoOrd.equalsIgnoreCase("")) {
          myord = campoOrd+" "+tipoOrd;
        }
      }

      if (myord.equalsIgnoreCase("")) {
        ordinamento = newOrdinamento;
      } else {
        if (newOrdinamento.equalsIgnoreCase("")) {
          ordinamento = myord;
        } else {
          ordinamento = myord+", "+newOrdinamento;
        }
      }
    } catch (Exception e) {
      throw new Exception ("Errore setOrdinamento! "+e.toString());
    }
  }

  /**
   * La funzione setta la condizione di navigazione da associare al template del Multirecord
   * 
   * @author Marco Bonforte
   * @version 1.0
   * @param newNumeroRecord  nuova condizione di navigazione
   */
  private void setNumeroRecord(int newNumeroRecord) {
    numeroRecord = newNumeroRecord;
  }

   /**
   * 
   * @author Marco Bonforte
   * @version 1.0
   * @param newAggiungi  nuova condizione di Aggiungi
   */
  public void setAggiungi(String newAggiungi) {
    if (newAggiungi.equalsIgnoreCase("N")) {
      aggiungi = false;
    } else {
      aggiungi = true;
      nuovaServlet = newAggiungi;
    }
  }

   /**
   * 
   * @author Marco Bonforte
   * @version 1.0
   * @param newProtetto  nuova condizione di protezione del multirecord
   */
  public void setProteggi(boolean newProtetto) {
    protetto = newProtetto;
  }
  
  public void setMVPG(String newMVPG) {
    sMvpg = newMVPG;
    if (aggiungi) {
      nuovaServlet += "&amp;MVPG="+sMvpg;
    }
  }

   /**
   * 
   * @author Marco Bonforte
   * @version 1.0
   * @param newDomini  indicaca se utilizzare i domini per la decodifica
   */
  public void setDomini(boolean newDomini) {
    domini = newDomini;
  }

  /**
   * 
   */
  private void setCorpo(String pCorpo) throws Exception {
    String sCorpo = pCorpo.toUpperCase();

    if (pCorpo.equalsIgnoreCase("")) {
      
    } else {
      int i = pCorpo.indexOf(BEGIN_CORPO);
      int j = pCorpo.indexOf(END_CORPO);

      if (i == -1 && j == -1) {
        i = sCorpo.indexOf("<TBODY");
        j = sCorpo.lastIndexOf("</TBODY>");
        if ( i > - 1) {
          isTable = true;
          i = sCorpo.indexOf("<TR",i);
        }
      }
      try {
        if (i > -1) {
        	try {
        		headerHtml =  campiOrdinamento(pCorpo.substring(0,i));
          } catch (Exception e) {
          	headerHtml = "";
          }
        } else {
          headerHtml = "";
          i = 0;
        }

        if ( j > -1) {
          if (!isTable) {
            j = j + END_CORPO.length();
          }
          try {
          	footerHtml = campiOrdinamento(pCorpo.substring(j));
          } catch (Exception e) {
          	footerHtml = "";
          }
        } else {
          j = pCorpo.length();
          footerHtml = "";
        }
        corpoHtml = pCorpo.substring(i,j);
      } catch (Exception e) {
        throw new Exception ("Errore setCorpo! "+e.toString());
      }
    }
  }

  /**
   * La funzione setta la lista dei campi presenti nel Multirecord
   * 
   * @author Marco Bonforte
   * @version 1.0
   * @param stringStream  stringa di input da interpretare
   */
  private void setListaCampi(String stringStream) throws Exception {
    setListaCampi(stringStream,false);
  }
  /**
   * La funzione setta la lista dei campi presenti nel Multirecord
   * 
   * @author Marco Bonforte
   * @version 1.0
   * @param stringStream  stringa di input da interpretare
   */
  private void setListaCampi(String stringStream, boolean bIQuery) throws Exception {
    int i,j = 0;
    String stringaDaElaborare = "";

    i = stringStream.indexOf(BEGIN_BLOCCO);
    if (i > -1 || bIQuery) {
      if (!listaCampi.contains("AREA")) {
        settaCampo("AREA",null);
      }
      if (!listaCampi.contains("CODICE_MODELLO")) {
        settaCampo("CODICE_MODELLO",null);
      }
      if (!listaCampi.contains("CODICE_RICHIESTA")) {
        settaCampo("CODICE_RICHIESTA",null);
      }
    }
    i = stringStream.indexOf(BEGIN_CORPO);
    j = stringStream.indexOf(END_CORPO);
    if (i > -1 && j > -1) {
      i = i + BEGIN_CORPO.length();
    } else {
      i = stringStream.toUpperCase().indexOf("<TBODY");
      if (i > - 1) {
        i = stringStream.toUpperCase().indexOf("<TR",i);
      }
      j = stringStream.lastIndexOf("</TBODY>");
    }
    if (i == -1) {
      i = 0;
    }
    if (j == -1) {
      j = stringStream.length();
    }
    
    stringaDaElaborare = stringStream.substring(i,j);
    int x = stringaDaElaborare.indexOf("<a href=\"#tagelimina\">&lt;X&gt;</a>");
    if (x > -1) {
      if (!listaCampi.contains("AREA")) {
        //listaCampi.addLast("AREA");
        settaCampo("AREA",null);
      }
      if (!listaCampi.contains("CODICE_MODELLO")) {
//        listaCampi.addLast("CODICE_MODELLO");
        settaCampo("CODICE_MODELLO",null);
      }
      if (!listaCampi.contains("CODICE_RICHIESTA")) {
//        listaCampi.addLast("CODICE_RICHIESTA");
        settaCampo("CODICE_RICHIESTA",null);
      }
    }
    int inizioCampo = stringaDaElaborare.indexOf(BEGIN_NAME_CAMPO);
    int fineCampo = stringaDaElaborare.indexOf(END_CAMPO,inizioCampo);
    while(inizioCampo > -1) {
      try {
        String pCampo = stringaDaElaborare.substring(inizioCampo,fineCampo);
        InformazioniCampo infoCampo = new InformazioniCampo(pCampo,END_NAME_CAMPO);
        String func = infoCampo.getFunc();
        if (func.equalsIgnoreCase("") || bIQuery) {
//          listaCampi.addLast(infoCampo.getDato());
          settaCampo(infoCampo.getDato(),infoCampo);
        } else {
//          listaCampi.addLast(func+" "+infoCampo.getDato());
          settaCampo(func+" "+infoCampo.getDato(),infoCampo);
        }
        if (infoCampo.getTipoCampo().equalsIgnoreCase("D") && !listaCampi.contains("AREA")) {
//          listaCampi.addLast("AREA");
          settaCampo("AREA",null);
        }
        if (infoCampo.getTipoCampo().equalsIgnoreCase("D") && !listaCampi.contains("CODICE_MODELLO")) {
//          listaCampi.addLast("CODICE_MODELLO");
          settaCampo("CODICE_MODELLO",null);
        }
        if (infoCampo.getTipoCampo().equalsIgnoreCase("D") && !listaCampi.contains("CODICE_RICHIESTA")) {
//          listaCampi.addLast("CODICE_RICHIESTA");
          settaCampo("CODICE_RICHIESTA",null);
        }
      } catch (Exception e) {
        listaCampi = null;
        throw new Exception ("Errore setListaCampi! "+e.toString());
//          break;
      }
      inizioCampo = stringaDaElaborare.indexOf(BEGIN_NAME_CAMPO,fineCampo);
      fineCampo = stringaDaElaborare.indexOf(END_CAMPO,inizioCampo);
    }
    //Aggiungo competenze
    if (utenteGDM == null) {
      utenteGDM = "";
    }
    if (!utenteGDM.equalsIgnoreCase("")) {
      if (bIQuery) {
        listaCampi.addLast("GDM_COMP_W");
        listaCampi.addLast("GDM_COMP_D");
      } else {
        if (isHorizontal) {
          listaCampi.addLast("GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI',d.id_documento,'U','"+utenteGDM+"','GDM',TO_CHAR(SYSDATE,'dd/mm/yyyy')) GDM_COMP_W" );
          listaCampi.addLast("GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI',d.id_documento,'D','"+utenteGDM+"','GDM',TO_CHAR(SYSDATE,'dd/mm/yyyy')) GDM_COMP_D" );
        } else {
          listaCampi.addLast("GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI',documento,'U','"+utenteGDM+"','GDM',TO_CHAR(SYSDATE,'dd/mm/yyyy')) GDM_COMP_W" );
          listaCampi.addLast("GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI',documento,'D','"+utenteGDM+"','GDM',TO_CHAR(SYSDATE,'dd/mm/yyyy')) GDM_COMP_D" );
        }
      }
    }
  }


//  /**
//   * 
//   */
//  private void setListaCategorie(String stringaDaElaborare) throws Exception {
//    int inizioCampo = stringaDaElaborare.indexOf(BEGIN_NAME_CAMPO);
//    int fineCampo = stringaDaElaborare.indexOf(END_CAMPO,inizioCampo);
//    while(inizioCampo > -1) {
//      try {
//        String pCampo = stringaDaElaborare.substring(inizioCampo,fineCampo);
//        InformazioniCampo infoCampo = new InformazioniCampo(pCampo,END_NAME_CAMPO);
//        listaCategorie.addLast(infoCampo.getDato());
//      } catch (Exception e) {
//        listaCategorie = null;
//        throw new Exception ("Errore setListaCategorie! "+e.toString());
////        break;
//      }
//      inizioCampo = stringaDaElaborare.indexOf(BEGIN_NAME_CAMPO,fineCampo);
//      fineCampo = stringaDaElaborare.indexOf(END_CAMPO,inizioCampo);
//    }
//  }

  /**
   * La funzione costruisce la select per il caricamento dei dati
   * 
   * @author Marco Bonforte
   * @version 1.0
   */
  protected void creaSelect() throws Exception {
    String campo = "";
    String prefisso = "";
    
    queryMulti = "SELECT ";
    if (distinct) {
      queryMulti += "DISTINCT ";
    }
    try {
      if (campi.equalsIgnoreCase("")) {
        if (listaCampi != null) {
          int numeroCampi = listaCampi.size();
          int j = 0;
          String virgola = " ";
          while (j < numeroCampi) {
            campo = (String)listaCampi.get(j);
            if (isHorizontal) {
              if (campo.equalsIgnoreCase("CODICE_MODELLO")) {
                queryMulti += virgola+"'"+codice_modello+"' CODICE_MODELLO";
              } else {
                if (campo.equalsIgnoreCase("AREA") || 
                    campo.equalsIgnoreCase("CODICE_RICHIESTA")) {
                  prefisso = "D.";
                } else {
                  if (campo.indexOf("GDM_COMPETENZA.GDM_VERIFICA") > -1) {
                    prefisso = "";
                  } else {
                    prefisso = "T.";
                  }
                }
                if (campo.indexOf("$") == 0) 
                {
                  queryMulti += virgola+"\""+prefisso+campo+"\"";
                } else  {
                  queryMulti += virgola+prefisso+campo;
                }
              }
            } else {
              if (campo.indexOf("$") == 0) 
              {
                queryMulti += virgola+"\""+campo+"\"";
              } else  {
                queryMulti += virgola+campo;
              }
            }
            virgola = ", ";
            j = j + 1;
          }
        } else {
          queryMulti += " * ";
        }
      } else {
        queryMulti += " "+campi;
      }
      if (isHorizontal) {
        queryMulti += " FROM " + tabella +" T, DOCUMENTI D";
        queryMulti += " WHERE T.ID_DOCUMENTO = D.ID_DOCUMENTO ";
        if (!getLegame().equalsIgnoreCase("")) {
          queryMulti += " AND "+getLegame();
        }
      } else {
        queryMulti += " FROM " + tabella;
        if (!getLegame().equalsIgnoreCase("")) {
          queryMulti += " WHERE "+getLegame();
        }
      }
      if (!getOrdinamento().equalsIgnoreCase("")) {
        queryMulti += " ORDER BY "+getOrdinamento();
      }
    } catch (Exception e) {
      throw new Exception ("Errore Crea Select! "+e.toString());
    }
  }

  /**
   * 
   */
  public void setRicerca(boolean pRicerca, String pCampoRisultato) {
    ricerca = pRicerca;
    risultato = pCampoRisultato;
  }

  /**
   * 
   */
  private void setTipo(String pTipo) {
    sTipo = pTipo;
  }

  /**
   * 
   */
  private void setTabella(String pTabella) {
    tabella = pTabella;
  }

  /**
   * 
   */
  public void setDistinct(boolean pDistinct) {
    distinct = pDistinct;
  }

  /**
   * 
   */
  public void setNonCaricareDati(boolean pNonCaricare) {
    nonCaricaDati = pNonCaricare;
  }

  /**
   * 
   */
  private void setCampi(String pCampi) {
    if (pCampi == null) {
      campi = "";
    } else {
      int posDistinct = pCampi.indexOf("DISTINCT");
      if (posDistinct == -1) { 
        campi = pCampi;
        setDistinct(false);
      } else {
        campi = pCampi.substring(posDistinct+9);
        setDistinct(true);
      }
    }
  }

  /**
   * La funzione costruisce la select per il caricamento dei dati
   * 
   * @author Marco Bonforte
   * @version 1.0
   */
//  private void setPrepSelect(String query) {
//    setPrepSelect(query, (AbstractParser)null);
//  }
  
  /**
   * La funzione costruisce la select per il caricamento dei dati
   * 
   * @author Marco Bonforte
   * @version 1.0
   */
//  private void setPrepSelect(AbstractParser ap) {
//    setPrepSelect(getSelect(), ap);
//  }
  
  /**
   * La funzione costruisce la select per il caricamento dei dati
   * 
   * @author Marco Bonforte
   * @version 1.0
   */
  private void setPrepSelect(String query, AbstractParser ap) {
    if (ap != null) {
      queryPrepar = ap.bindingDeiParametri(query);
    } else {
      queryPrepar = query;
    }
  }

  /**
   * La funzione esegue l'istruzione che restituisce i dati dei record
   * 
   * @author Marco Bonforte
   * @version 1.0
   */
  private void caricaDatiXML(IDbOperationSQL dbOp, int prevRec) throws Exception {
    String outXml = "";
    String sRecord = "";
    listaDati.clear();

    erroreMsg = "";
    if (nonCaricaDati) {
      recordTotali = 0;
      return;
    }
    if (sTipo.equalsIgnoreCase("J")) {
      String javaStm = sIstruzione.replaceAll(":xml",sXml);
      javaStm = javaStm.replaceAll(":XML",sXml);
      try{
        WrapParser wp = new WrapParser(javaStm);
        outXml = wp.go();
      } catch (InvocationTargetException itjEx) {
        throw new Exception("Attenzione! Errore in Multirecord. Fase di caricamento dati - Errore java: "+itjEx.getCause()+".\nErrore completo: "+itjEx);
      } catch (Exception ijEx) {
        throw new Exception("Attenzione! Errore in Multirecord. Fase di caricamento dati. "+ijEx);
      }
    }
    
    if (sTipo.equalsIgnoreCase("P")) {
      String funcSql = sIstruzione.replaceAll(":xml",sXml);
      funcSql = funcSql.replaceAll(":XML",sXml);
      dbOp.setCallFunc(funcSql);
      dbOp.execute();
      outXml = dbOp.getCallSql().getString(1);
      if (outXml == null) {
        outXml = "";
      }
    }

/*outXml="<FUNCTION_OUTPUT><RESULT>nonok</RESULT><ERROR>ricerca non effettuata per mancata impostazioni chiavi di ricerca</ERROR><STACKTRACE/>"+
"<RECORD1><ANNO>1999</ANNO><CLASS_COD>01.04</CLASS_COD><VCLASS_COD>01.04</VCLASS_COD>"+
  "<CLASS_DAL>01/01/1951</CLASS_DAL><CLASS_DESCR>Elezioni Amministrative</CLASS_DESCR>"+
  "<ULTIMO_NUMERO_SUB>-1</ULTIMO_NUMERO_SUB><CR_PADRE>223</CR_PADRE><FASCICOLO_ANNO>1999</FASCICOLO_ANNO>"+
  "<FASCICOLO_NUMERO/><VFASCICOLO_ANNO>1999</VFASCICOLO_ANNO><VFASCICOLO_NUMERO/>"+
  "<STATO_FASCICOLO>1</STATO_FASCICOLO><FASCICOLO_OGGETTO>AAA</FASCICOLO_OGGETTO>"+
  "<VFASCICOLO_OGGETTO>AAA</VFASCICOLO_OGGETTO><DATA_APERTURA>17/05/2006</DATA_APERTURA>"+
  "<DATA_CHIUSURA/><RESPONSABILE/><UFFICIO_COMPETENZA/></RECORD1>"+
"<RECORD2><ANNO>1999</ANNO><VCLASS_COD>01.04</VCLASS_COD>"+
  "<CLASS_DAL>01/01/1951</CLASS_DAL><CLASS_DESCR>Elezioni Ã¨ Amministrative</CLASS_DESCR>"+
  "<ULTIMO_NUMERO_SUB>-1</ULTIMO_NUMERO_SUB><CR_PADRE>222</CR_PADRE><FASCICOLO_ANNO>1999</FASCICOLO_ANNO>"+
  "<FASCICOLO_NUMERO/><VFASCICOLO_ANNO>1999</VFASCICOLO_ANNO><VFASCICOLO_NUMERO/>"+
  "<STATO_FASCICOLO>1</STATO_FASCICOLO><FASCICOLO_OGGETTO>STAVOLTA A S</FASCICOLO_OGGETTO>"+
  "<VFASCICOLO_OGGETTO>STAVOLTA A S</VFASCICOLO_OGGETTO><DATA_APERTURA>17/05/2006</DATA_APERTURA>"+
  "<DATA_CHIUSURA/><RESPONSABILE/><UFFICIO_COMPETENZA/></RECORD2>"+
  "<RECORD_TOTALI>56</RECORD_TOTALI></FUNCTION_OUTPUT>";*/



    recordTotali = 0;
    //Interpretazione XML
    try {
      Document dInput = null;
      dInput = DocumentHelper.parseText(outXml);
      String esito = leggiValore(dInput, "RESULT");
      if (!esito.equalsIgnoreCase("ok")) {
        String errore = leggiValore(dInput, "ERROR");
        erroreMsg = errore;
        recordTotali = 0;
        return;
        //throw new Exception(errore);
      }
      String recTot = leggiValore(dInput, "RECORD_TOTALI");
      recordTotali = Integer.parseInt(recTot);
      int j = 0, l = 0;
      int i = 1;
      String campo = "", valore = "";
      Element root = dInput.getRootElement();
      while (i <= numeroRecord) {
        sRecord = "";
        Element eRec_i = leggiElemento(root, "RECORD"+i);
        if (eRec_i != null) {
          l = listaCampi.size();
          j = 0;
          sRecord = "";
          while (j < l) {
            campo = (String)listaCampi.get(j);
            valore = leggiValore(eRec_i,campo);
            if (valore == null) {
              valore = "";
            }
            sRecord += "<C>"+campo+"</C><V>"+valore+"</V>";
            j++;
          }
          listaDati.addLast(sRecord);
        } 
        i++;
      }

    } catch (Exception e) {
      throw new Exception ("Attenzione! Errore in Multirecord, Fase di caricamento dati. "+e.toString());
    }
 
  }
  
  /**
   * 
   */
  private void caricaDatiIQuery(Connection cn, int prevRec, String area, String cm, AbstractParser ap) throws Exception {
    //Debug Tempo
    long ptime = stampaTempo("Multirecord::caricaDatiIQuery - Inizio",area,cm,nomeBlocco,0);
    //Debug Tempo
    String            sRecord = "";
    String            campo = "";
    String            categoria = "";
    String            area_cm_dato = "";
    String            cm_dato = "";
    String            valore = "";
    int               prevRecord = 0;
    String  campo1 = "",
    area1  ="",
    cm1    = "",
    cate1  = "";
    String  campo2 = "",
    area2  ="",
    cm2    = "",
    cate2  = "";



    listaDati.clear();
    erroreMsg = "";

    if (nonCaricaDati) {
      recordTotali = 0;
      return;
    }
    if (listaCampi.size() < 1) {
      recordTotali = 0;
      return;
    }
    try {   
      IQuery iq = new IQuery();
      iq.initVarEnv(utenteGDM,utenteGDM,cn);
      iq.setAccessProfile(false);
      iq.setInstanceProfile(false);
      if (monoRecord || noCompetenze) {
        iq.escludiControlloCompetenze(true);
      }
      //Debug Tempo
      stampaTempo("Multirecord::caricaDatiIQuery - Parziale 1",area,cm,nomeBlocco,ptime);
      //Debug Tempo

      //Ordinamento
      ordinamento = ordinamento.replaceAll("asc","ASC");
      ordinamento = ordinamento.replaceAll("desc","DESC");
      String ord = "";
      StringTokenizer st = new StringTokenizer(ordinamento,",");
      while (st.hasMoreTokens()) {
        ord = st.nextToken();
        int posAsc = ord.indexOf(" ASC");
        int posDesc = ord.indexOf(" DESC");
        if (posAsc > -1 || posDesc == -1) {
          ord = ord.replaceAll(" ASC","");
          ord = ord.replaceAll(" ","");
          StringTokenizer st2 = new StringTokenizer(ord,"#");
          int ii = 0;
          while (st2.hasMoreTokens()) {
            ii++;
            if (ii == 1) {
              campo = st2.nextToken();
              categoria = "";
              area_cm_dato = area;
              cm_dato = cm;
            }
            if (ii == 2) {
              categoria = st2.nextToken();
            }
            if (ii == 3) {
              cm_dato = st2.nextToken();
              area_cm_dato = categoria;
              categoria = "";
            }
          }
          if (categoria.equalsIgnoreCase("")) {
            iq.addCampoOrdinamentoAsc(campo,area_cm_dato,cm_dato);
          } else {
            iq.addCampoOrdinamentoAsc(campo,categoria);
          }
        }  else {
          if (posDesc > -1) {
            ord = ord.replaceAll(" DESC","");
            ord = ord.replaceAll(" ","");
            StringTokenizer st2 = new StringTokenizer(ord,"#");
            int ii = 0;
            while (st2.hasMoreTokens()) {
              ii++;
              if (ii == 1) {
                campo = st2.nextToken();
                categoria = "";
                area_cm_dato = area;
                cm_dato = cm;
              }
              if (ii == 2) {
                categoria = st2.nextToken();
              }
              if (ii == 3) {
                cm_dato = st2.nextToken();
                area_cm_dato = categoria;
                categoria = "";
              }
            }
            if (categoria.equalsIgnoreCase("")) {
              iq.addCampoOrdinamentoDesc(campo,area_cm_dato,cm_dato);
            } else {
              iq.addCampoOrdinamentoDesc(campo,categoria);
            }
          }
        }
      }
      //Debug Tempo
      stampaTempo("Multirecord::caricaDatiIQuery - Parziale 2",area,cm,nomeBlocco,ptime);
      //Debug Tempo

      //Condizioni di legame
      String lega = ap.bindingDeiParametri(legame);
      lega = lega.replaceAll(" and "," AND ");
      lega = lega.replaceAll(" like "," LIKE ");
      lega = lega.replaceAll(" between "," BETWEEN ");
      String strval= "";
      int posAnd = 0;
      int lung = lega.length();
      while (lung > 0) {
        posAnd = lega.indexOf(" AND ");
        if (posAnd > 0) {
         strval = lega.substring(0,posAnd);
         lega = lega.substring(posAnd+5);
        } else {
          strval = lega;
          lega = "";
        }
        lung = lega.length();
        String campoF = "";
        String valoreF = "";
        String ope = "";
        int posDiv    = strval.indexOf("<>");
        int posMinUg  = strval.indexOf("<=");
        int posMagUg  = strval.indexOf(">=");
        int posUg     = strval.indexOf("=");
        int posLike   = strval.indexOf("LIKE");
        if (posDiv > 0) {
          posUg = -1;
          campoF = strval.substring(0,posDiv);
          campoF = campoF.replaceAll("'","");
          campoF = campoF.replaceAll(" ","");
          valoreF = strval.substring(posDiv+2);
          valoreF = valoreF.replaceAll("'","");
//          valoreF = valoreF.replaceAll(" ","");
          valoreF = valoreF.trim();
          ope = "<>";
//          iq.addCampo(campoF,valoreF,"<>");
        }
        if (posMinUg > 0) {
          posUg = -1;
          campoF = strval.substring(0,posMinUg);
          campoF = campoF.replaceAll("'","");
          campoF = campoF.replaceAll(" ","");
          valoreF = strval.substring(posMinUg+2);
          valoreF = valoreF.replaceAll("'","");
//        valoreF = valoreF.replaceAll(" ","");
          valoreF = valoreF.trim();
          ope = "<=";
          //iq.addCampo(campoF,valoreF,"<=");
        }
        if (posMagUg > 0) {
          posUg = -1;
          campoF = strval.substring(0,posMagUg);
          campoF = campoF.replaceAll("'","");
          campoF = campoF.replaceAll(" ","");
          valoreF = strval.substring(posMagUg+2);
          valoreF = valoreF.replaceAll("'","");
//        valoreF = valoreF.replaceAll(" ","");
          valoreF = valoreF.trim();
          ope = ">=";
          //iq.addCampo(campoF,valoreF,">=");
        }
        if (posUg > 0) {
          campoF = strval.substring(0,posUg);
          campoF = campoF.replaceAll("'","");
          campoF = campoF.replaceAll(" ","");
          valoreF = strval.substring(posUg+1);
          valoreF = valoreF.replaceAll("'","");
//        valoreF = valoreF.replaceAll(" ","");
          valoreF = valoreF.trim();
          ope = "";
          //iq.addCampo(campoF,valoreF);
        }
        if (posLike > 0) {
          campoF = strval.substring(0,posLike);
          campoF = campoF.replaceAll("'","");
          campoF = campoF.replaceAll(" ","");
          valoreF = strval.substring(posLike+4);
          valoreF = valoreF.replaceAll("'","");
//        valoreF = valoreF.replaceAll(" ","");
          valoreF = valoreF.trim();
          ope = "";
          //iq.addCampo(campoF,valoreF);
        }
        campo1 = "";
        area1  ="";
        cm1    = "";
        cate1  = "";

        StringTokenizer st3 = new StringTokenizer(campoF,"#");
        int i = 0;
        while (st3.hasMoreTokens()) {
          i++;
          if (i == 1) {
            campo1 = st3.nextToken();
            area1 = area;
            cm1 = cm;
          }
          if (i == 2) {
            cate1 = st3.nextToken();
            area1 = "";
            cm1 = "";
          }
          if (i == 3) {
            cm1 = st3.nextToken();
            area1 = cate1;
            cate1 = "";
          }
        }
        if (cate1.equalsIgnoreCase("")) {
          if (ope.equalsIgnoreCase("")) {
            iq.addCampo(campo1,valoreF,area1,cm1);
          } else {
            iq.addCampo(campo1,valoreF,ope,area1,cm1);
          }
        } else {
          if (ope.equalsIgnoreCase("")) {
            iq.addCampoCategoria(campo1,valoreF,cate1);
          } else {
            iq.addCampoCategoria(campo1,valoreF,ope,cate1);
          }
        }
      }

      //Debug Tempo
      stampaTempo("Multirecord::caricaDatiIQuery - Parziale 3",area,cm,nomeBlocco,ptime);
      //Debug Tempo
      
      //Campi di ritorno
      int l = listaCampi.size();
      int j = 0;
      while (j <l ) {
        campo = (String)listaCampi.get(j);
        if (!campo.equalsIgnoreCase("AREA") &&
            !campo.equalsIgnoreCase("CODICE_MODELLO")&&
            !campo.equalsIgnoreCase("CODICE_RICHIESTA")&&
            !campo.equalsIgnoreCase("GDM_COMP_W") && 
            !campo.equalsIgnoreCase("GDM_COMP_D")) {
          categoria = (String)listaCate.get(j);
          area_cm_dato = (String)listaArea.get(j);
          cm_dato = (String)listaCm.get(j);
          if (categoria.equalsIgnoreCase("")) {
            if (area_cm_dato.equalsIgnoreCase("")) {
              iq.addCampoReturn(campo,area,cm);
            } else {
              iq.addCampoReturn(campo,area_cm_dato,cm_dato);
            }
          } else {
            iq.addCampoReturn(campo,categoria);
          }
        }
        j++;
      }

      //Debug Tempo
      stampaTempo("Multirecord::caricaDatiIQuery - Parziale 4",area,cm,nomeBlocco,ptime);
      //Debug Tempo
      if (!sJoin.equalsIgnoreCase("")) {
        st = new StringTokenizer(sJoin,";");
        while (st.hasMoreTokens()) {
          String cnd = st.nextToken();
          int pu = cnd.indexOf("=");
          if (pu > 0) {
            StringTokenizer st1 = new StringTokenizer(cnd.substring(0,pu),"#");
            j = 0;
            while (st1.hasMoreTokens() && j<3) {
              j++;
              if (j == 1) {
                campo1 = st1.nextToken();
                area1 = area;
               cm1 = cm;
                cate1 = "";
              }
              if (j == 2) {
                cate1 = st1.nextToken();
              }
              if (j == 3) {
                cm1 = st1.nextToken();
                area1 = cate1;
                cate1 = "";
              }
            }
            StringTokenizer st2 = new StringTokenizer(cnd.substring(pu+1),"#");
            j = 0;
            while (st2.hasMoreTokens() && j<3) {
              j++;
              if (j == 1) {
                campo2 = st2.nextToken();
                area2 = area;
                cm2 = cm;
                cate2 = "";
              }
              if (j == 2) {
                cate2 = st2.nextToken();
              }
              if (j == 3) {
                cm2 = st2.nextToken();
                area2 = cate2;
                cate2 = "";
              }
            }
            if (cate1.equalsIgnoreCase("")) {
              if (cate2.equalsIgnoreCase("")) {
                iq.addJoinModel(area1,cm1,campo1,area2,cm2,campo2);
              } else {
                iq.addJoinMix(area1,cm1,campo1,cate2,campo2);
              }
            } else {
              if (cate2.equalsIgnoreCase("")) {
                iq.addJoinMix(area2,cm2,campo2,cate1,campo1);
              } else {
                iq.addJoinClass(cate1,campo1,cate2,campo2);
              }
            }
          }
        }
      }

      //Debug Tempo
      stampaTempo("Multirecord::caricaDatiIQuery - Parziale 5",area,cm,nomeBlocco,ptime);
      //Debug Tempo
      
      iq.setTypeModelReturn(area,cm);
//      iq.setFetchInit(prevRec);
//      iq.setFetchSize(numeroRecord);
      if (!iddoc.equalsIgnoreCase("")) {
        iq.settaIdDocumentoRicerca(iddoc);
      }

      if (iq.ricerca().booleanValue()) {
        recordTotali = iq.getProfileNumber();
        if (prevRec < recordTotali) {
          prevRecord = prevRec;
        }
        int posRelativa = 0;
        int posizione = 0;
        //Debug Tempo    
        stampaTempo("Multirecord::caricaDatiIQuery - Parziale 6",area,cm,nomeBlocco,ptime);
        //Debug Tempo
        ResultSetIQuery  rst = iq.getResultSet();
        while (rst.next() && (prevRecord+posRelativa) <= recordTotali && posRelativa < numeroRecord) {
          posizione++;
          if (posizione > prevRecord) {
            j = 0;
            sRecord = "";
            while (j < l) {
              campo = (String)listaCampi.get(j);
              if (campo.equalsIgnoreCase("AREA")) {
                valore = area;
              } else {
                if (campo.equalsIgnoreCase("CODICE_MODELLO")) {
                  valore = cm;
                } else {
                  if (campo.equalsIgnoreCase("CODICE_RICHIESTA")) {
                    valore = rst.getCr();
                  } else {
                    if (campo.equalsIgnoreCase("GDM_COMP_W") || campo.equalsIgnoreCase("GDM_COMP_D")) {
                      if (monoRecord || noCompetenze) {
                        valore = "1";
                      } else {
                        Environment varEnv = new Environment(utenteGDM, utenteGDM, "", "", "", cn);
                        GDM_Competenze gdmComp = new GDM_Competenze(varEnv);
                        UtenteAbilitazione ua = new UtenteAbilitazione(utenteGDM, null, null, null);
                        String tipoC = "U";
                        if (campo.equalsIgnoreCase("GDM_COMP_D")) {
                          tipoC = "D";
                        }
                        Abilitazioni ab = new Abilitazioni("DOCUMENTI", rst.getId(), tipoC);
                        valore = ""+gdmComp.verifica_GDM_Compentenza(ua, ab);
                      }
                    } else {
                      categoria = (String)listaCate.get(j);
                      area_cm_dato = (String)listaArea.get(j);
                      cm_dato = (String)listaCm.get(j);
                      if (categoria == null || categoria.equalsIgnoreCase("")) {
                        if (area_cm_dato.equalsIgnoreCase("")) {
                          valore = rst.get(campo,area,cm);
                        } else {
                          valore = rst.get(campo,area_cm_dato,cm_dato);
                          campo += "#"+area_cm_dato+"#"+cm_dato;
                        }
                      } else {
                        valore = rst.get(campo,categoria);
                        campo += "#"+categoria;
                      }
                    }
                  }
                }
              }
              if (valore == null || valore.equalsIgnoreCase("null")) {
                valore = "";
              }
  
              sRecord += "<C>"+campo+"</C><V>"+valore+"</V>";
              
              j++;
            }
            listaDati.addLast(sRecord);
            posRelativa++;
          }
        }
      } else {
        recordTotali = 0;
        if (iq.isQueryTimeOut()) {
          logger.error("Blocco: "+nomeBlocco+" ------- Sono andato in timeout");
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      //Debug Tempo
      stampaTempo("Multirecord::caricaDatiIQuery - Fine",area,cm,nomeBlocco,ptime);
      //Debug Tempo
//      erroreMsg = "Errore in fase di caricamento dati.";
      logger.error("Errore",e);
      throw new Exception ("Attenzione! Errore in Multirecord, Fase di caricamento dati. "+e.toString());
    }
    //Debug Tempo
    stampaTempo("Multirecord::caricaDatiIQuery - Fine",area,cm,nomeBlocco,ptime);
    //Debug Tempo
  }

  /**
   * La funzione costruisce la select per il caricamento dei dati
   * 
   * @author Marco Bonforte
   * @version 1.0
   */
  private void caricaDati(IDbOperationSQL dbOp, int prevRec) throws Exception {
    ResultSet         rst = null;
    String            query = "";
    String            sRecord = "";
    String            sColonna = "";
    String            sValore = "";
    int               stepNav = 0;
    int               prevRecord = 0;
    boolean           bEsite = false;
    ResultSetMetaData rstMd = null;

    listaDati.clear();
    erroreMsg = "";
    if (nonCaricaDati) {
      recordTotali = 0;
      return;
    }
    query = getPrepSelect();
    if (query.equalsIgnoreCase("")) {
      query = getSelect();
      if (query.equalsIgnoreCase("")) {
        throw new Exception ("Attenzione! Errore in Multirecord fase di caricamento dati. Non è stata definita la select!");
      }
    }
    try {
      int oldType = dbOp.getTypeResultSet();
      int oldCurr = dbOp.getTypeConcurrency();
      
      dbOp.setTypeResultSet(ResultSet.TYPE_SCROLL_INSENSITIVE);
      dbOp.setTypeConcurrency(ResultSet.CONCUR_UPDATABLE);
      dbOp.setStatement(query);
      dbOp.execute();
      rst = dbOp.getRstSet();
      rst.last();
      recordTotali = rst.getRow();

      stepNav = recordTotali;
      
      if (prevRec < recordTotali) {
        prevRecord = prevRec;
      }
      bEsite = rst.first();
      if (bEsite) {
        rstMd = rst.getMetaData();
        int posRelativa = 1;
        while (posRelativa <= stepNav && rst.absolute(prevRecord+posRelativa)) {
         int colNum = rstMd.getColumnCount();
          sRecord = "";
          int j = 1;
          while (j <= colNum) {
            sColonna = rstMd.getColumnName(j);
            sValore = rst.getString(j);
            if (sValore == null) {
              sValore = "";
            }
            sRecord += "<C>"+sColonna+"</C>";
            sRecord += "<V>"+sValore+"</V>";
            j = j + 1;
          }
          listaDati.addLast(sRecord);
          posRelativa++;
        }
      }
      dbOp.setTypeResultSet(oldType);
      dbOp.setTypeConcurrency(oldCurr);
    } catch (Exception e) {
//      erroreMsg = "Errore in fase di caricamento dati.";
      throw new Exception ("Attenzione! Errore in Multirecord fase di caricamento dati. "+e.toString()+"\n"+query);
    }

  }

  /**
   * La funzione ritorna il codice HTML del multirecord 
   * @param dbOp DbOperationSQL connessa al DB su cui effettuare la select
   * @param riga Numero di riga da cui il multirecord deve essere visualizzato.
   * @param navigatore Indica se visulaizzare o no il navigatore
   */
  public String creaHtml(IDbOperationSQL dbOp, 
                          int riga, 
                          boolean navigatore) throws Exception {
    return creaHtml(dbOp, riga, (AbstractParser) null, navigatore);
  }

  /**
   * La funzione ritorna il codice HTML del multirecord 
   * @param dbOp DbOperationSQL connessa al DB su cui effettuare la select
   * @param request HttpServletRequest della pagina che incorpora il multirecord.
   * @param navigatore Indica se visulaizzare o no il navigatore
   */
  public String creaHtml(IDbOperationSQL dbOp, 
                         HttpServletRequest request, 
                         boolean navigatore) throws Exception {
    return creaHtml(dbOp, request, (AbstractParser) null, navigatore);
  }

  /**
   * 
   */
  private String creaRiga(IDbOperationSQL dbOp, int riga,  AbstractParser ap) {
    return creaRiga(dbOp, riga,  ap, false);
  }

  /**
   * 
   */
  private String creaRiga(IDbOperationSQL dbOp, int riga,  AbstractParser ap, boolean stampa) {
    //Debug Tempo
    long ptime = stampaTempo("Multirecord::creaRiga - Inizio","","",nomeBlocco,0);
    //Debug Tempo
    String retval = "";
    String pCampo = "";
    String nomeCampo = "";
    String stileCampo = "";
    String tipoCampo = "";
    String valoreCampo = "";
    String valoreDato = "";
    String rwBlocco = "";
    String pBlocco = "";
    int i,j,inizioCampo,fineCampo = 0;
    String stringaDaElaborare = corpoHtml.replaceAll(BEGIN_FUNCT,BEGIN_CAMPO);
    Properties extraKeys = new Properties();
//    if (!erroreMsg.equalsIgnoreCase("")) {
//      retval += "<div>"+erroreMsg+"</div>\n";
//    }
    if (riga >= listaDati.size()) {
      return retval;
    }

    String mypath = "./";
    if (pathThemes.equalsIgnoreCase("../")) {
      mypath = "../common/";
    }                                

    String area = leggiValore("AREA",riga);
    String cm = leggiValore("CODICE_MODELLO",riga);
    String cr = leggiValore("CODICE_RICHIESTA",riga);
    String competenza_del = leggiValore("GDM_COMP_D",riga);

    String elimina = "<a href='#' "+
      "onclick='eliminaDocumento"+nomeBlocco+"(this,\""+area+"\",\""+cm+"\",\""+cr+"\"); return false;' "+
      "onkeypress='eliminaDocumento"+nomeBlocco+"(this,\""+area+"\",\""+cm+"\",\""+cr+"\"); return false;'>"+
      "<img style='border: none' title='Elimina documento' src='"+mypath+"images/gdm/delete.gif' alt='Elimina documento' /></a>";
    if (competenza_del == null) {
      competenza_del = "";
    }
    if ((!competenza_del.equalsIgnoreCase("1")) || letturaScrittura.equalsIgnoreCase("P") || protetto) {
      elimina = "<img style='border: none' title='Elimina documento' src='"+mypath+"images/gdm/nodelete.gif' alt='Elimina documento' />";
    } 
    stringaDaElaborare = stringaDaElaborare.replaceAll("<a href=\"#tagelimina\">&lt;X&gt;</a>",elimina);

    stringaDaElaborare = stringaDaElaborare.replaceAll(BEGIN_CORPO,"");
    stringaDaElaborare = stringaDaElaborare.replaceAll(END_CORPO,"");
    i = stringaDaElaborare.indexOf(BEGIN_CAMPO);
    j = stringaDaElaborare.indexOf(END_CAMPO,i) + END_CAMPO.length();
    inizioCampo = stringaDaElaborare.indexOf(BEGIN_NAME_CAMPO);
    fineCampo = stringaDaElaborare.indexOf(END_CAMPO,inizioCampo);
    while(inizioCampo > -1) {
      retval += stringaDaElaborare.substring(0,i);
      try {
        //Creo la stringa per il campo
        pCampo = stringaDaElaborare.substring(inizioCampo,fineCampo);
        InformazioniCampo infoCampo = new InformazioniCampo(pCampo,END_NAME_CAMPO);
        nomeCampo = infoCampo.getDato();
        stileCampo = infoCampo.getStile();
        tipoCampo = infoCampo.getTipoCampo();
        valoreDato = leggiValore(nomeCampo,riga);
        if (tipoCampo.equalsIgnoreCase("H")) {
        	//valoreDato = valoreDato.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        	valoreDato = valoreDato.replaceAll("\\<.*?>","");
        }
        if (domini && tipoCampo.equalsIgnoreCase("T")) {

          String area_dato = "", sDom = "";
          area_dato = getAreaDominio(nomeCampo);
          sDom = getDominio(nomeCampo);

          if ((sDom != null) && (!sDom.equalsIgnoreCase(""))) {
            MultiRecParser mp = new MultiRecParser(listaDati,riga, utenteGDM);
            //Controllo se c'è una personalizzazione
            Personalizzazioni pers = null;
            String ente = (String)pRequest.getSession().getAttribute("Ente");
            if (ente == null) {
              ente = "";
            }
            String us = (String)pRequest.getSession().getAttribute("Utente");
            if (us == null) {
              us = "";
            }
            pers = (Personalizzazioni)pRequest.getSession().getAttribute("_personalizzazioni_gdm");
            if (pers == null) {
              try {
                pers = new Personalizzazioni(ente, us);
              } catch (Exception e) {
                
              }
              pRequest.getSession().setAttribute("_personalizzazioni_gdm",pers);
            }
            String persDom = null;
            String areaDom = area_dato;
            if (pers != null) {
              if (pers != null) {
                persDom = pers.getPersonalizzazione(Personalizzazioni.DOMINI, area_dato+"#"+sDom+"#-");
                int z = persDom.indexOf("#");
                int k = persDom.lastIndexOf("#");
                areaDom = persDom.substring(0,z);
                sDom = persDom.substring(z+1,k);
              }
            }
            Dominio dominio = ld.getDominio(areaDom, sDom, "-", mp);
            if (dominio != null) {
              String myvalore = dominio.getValore(valoreDato.replaceFirst("#",""));
              if ((myvalore != null) && (!myvalore.equalsIgnoreCase(""))) {
                valoreDato = myvalore;
              }
            }
          }
        }
        
        //Controllo la lunghezza
        int lunghezza = 0;
        String sLunghezza = infoCampo.getLunghezza();
        if (sLunghezza.length() > 0) {
      	  lunghezza = Integer.parseInt(sLunghezza);
      	  if (lunghezza < valoreDato.length()) {
      		valoreDato = valoreDato.substring(0, lunghezza);
      	  }
        }
        //TODO
        //Rimuovo caratteri non printable
        valoreDato = codificaCaratteriSpeciali(valoreDato);
       
        
        if (!monoRecord) {
        	valoreDato = proteggiValore(valoreDato);
        }
        if (stampa) {
          valoreDato = "<![CDATA["+valoreDato+"]]>";
        }
        if (stileCampo.equalsIgnoreCase("")) 
        {
          valoreCampo =  valoreDato+"";
        } else {
          valoreCampo =  "<span style='"+stileCampo+"' >"+valoreDato+"</span>\n";
        }

        if (tipoCampo.equalsIgnoreCase("K") && !stampa) {
          extraKeys.put(nomeCampo,valoreDato);
          valoreCampo = "<input type='hidden' name='"+nomeCampo+"' value=\""+valoreDato.replaceAll("\"","&quot;")+"\" />\n";
        }

        if (tipoCampo.equalsIgnoreCase("D")) {
          if (monoRecord) {
/*        	  valoreCampo = 
  	            "<a class='AFCDataLink' href=':URL_DOCUMENTOVIEW' \n"+
  	            " onclick='popupFullScreen(this.href,\"\"); return false;'\n"+
  	            " onkeypress='popupFullScreen(this.href,\"\"); return false;'\n"+
  	            " title='Attenzione apre una nuova finestra'>"+valoreCampo+"</a>\n";*/
        	  valoreCampo = 
        	            "<a class='AFCDataLink' href='#' \n"+
        	            " onclick=\":URL_DOCUMENTOVIEW\"\n"+
        	            " onkeypress=\":URL_DOCUMENTOVIEW\"\n"+
        	            " title='Attenzione apre una nuova finestra'>"+valoreCampo+"</a>\n";
          } else {
	          String competenza = leggiValore("GDM_COMP_W",riga);
	          if (competenza == null) {
	            competenza = "";
	          }
	          if (competenza.equalsIgnoreCase("1") && !protetto) {
	            rwBlocco = "W";
	          } else {
	            rwBlocco = "R";
	          }
	          String newMvpg = sMvpg;
	          if ((nomeServlet.indexOf("ServletModulisticaCartella") > 0) && (sMvpg.equalsIgnoreCase(""))) {
	            newMvpg = "ServletModulisticaDocumento";
	          }
	          String newNomeServ = nomeServlet.replaceFirst("ServletModulisticaCartella","VisualizzaModello");
	          valoreCampo = 
	            "<a class='AFCDataLink' href='"+newNomeServ+"?area="+area+"&amp;cm="+cm+"&amp;cr="+cr+"&amp;rw="+rwBlocco+"&amp;wfather="+nomeBlocco+"&amp;MVPG="+newMvpg+"' \n"+
	            " onclick='window.open(this.href,\"\",\"toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=0,resizable=0,copyhistory=0\"); return false;'\n"+
	            " onkeypress='window.open(this.href,\"\",\"toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=0,resizable=0,copyhistory=0\"); return false;'\n"+
	            " title='Attenzione apre una nuova finestra'>"+valoreCampo+"</a>\n";
          }
        }
        
        if (tipoCampo.equalsIgnoreCase("L")) {
        	String url = infoCampo.getHref();
          retval +=
            "<a class='AFCDataLink' href='"+url+"' \n"+
            " onclick='window.open(this.href); return false;'\n"+
            " onkeypress='window.open(this.href); return false;'\n"+
            " title='Attenzione apre una nuova finestra' >"+valoreCampo+"</a>\n";
        } else {
          if (!risultato.equalsIgnoreCase("")) {
            if (nomeCampo.equalsIgnoreCase(risultato)) {
              if (isMono) {
                retval += "<input  class='AFCInput' onclick='document.getElementById(\"B3\").disabled = false; document.getElementById(\"B2\").disabled = false; ' type='radio' name='"+risultato+"' value=\""+valoreCampo.replaceAll("\"","&quot;")+"\" />";
              } else {
                retval += "<input  class='AFCInput' onclick='document.getElementById(\"B3\").disabled = false; document.getElementById(\"B2\").disabled = false; ' type='checkbox' name='"+risultato+"_"+riga+"' value=\""+valoreCampo.replaceAll("\"","&quot;")+"\" />";
              }
            } else {
              int posDue = risultato.indexOf("@"+nomeCampo+"@");
              int posUna = risultato.indexOf("@"+nomeCampo);
              int posIni = risultato.indexOf(nomeCampo+"@");
              if ((posDue > -1) || (posUna > -1)) {
                retval += "<input  class='AFCInput' type='hidden' name='"+nomeCampo+"_"+riga+"' value=\""+valoreCampo.replaceAll("\"","&quot;")+"\" />";
                if (!tipoCampo.equalsIgnoreCase("H")) {
                  retval += valoreCampo;
                }
              } else {
                if (posIni == 0) {
                  if (isMono) {
                    retval += "<input  class='AFCInput' onclick='document.getElementById(\"B3\").disabled = false; document.getElementById(\"B2\").disabled = false; ' type='radio' name='"+nomeCampo+"' value=\""+valoreCampo.replaceAll("\"","&quot;")+"\" />";
                  } else {
                    retval += "<input  class='AFCInput' onclick='document.getElementById(\"B3\").disabled = false; document.getElementById(\"B2\").disabled = false; ' type='checkbox' name='"+nomeCampo+"_"+riga+"' value=\""+valoreCampo.replaceAll("\"","&quot;")+"\" />";
                  }
                } else {
                  retval += valoreCampo;
                }
              }
            }
          } else {
            retval += valoreCampo;
          }
        }
      } catch (Exception e) {
        nomeCampo = "<!-- Errore "+pCampo+" -->\n";
      }
      stringaDaElaborare = stringaDaElaborare.substring(j);
      i = stringaDaElaborare.indexOf(BEGIN_CAMPO);
      j = stringaDaElaborare.indexOf(END_CAMPO,i) + END_CAMPO.length();
      inizioCampo = stringaDaElaborare.indexOf(BEGIN_NAME_CAMPO);
      fineCampo = stringaDaElaborare.indexOf(END_CAMPO,inizioCampo);
    }
    retval += stringaDaElaborare;

    //Inserisco i blocchi nested
    stringaDaElaborare = retval;
    retval = "";
    i = stringaDaElaborare.indexOf(BEGIN_BLOCCO);
    j = stringaDaElaborare.indexOf(END_BLOCCO,i) + END_BLOCCO.length();
    int inizioBlocco = stringaDaElaborare.indexOf(BEGIN_NAME_BLOCCO);
    int fineBlocco = stringaDaElaborare.indexOf(END_BLOCCO,inizioBlocco);
    while(inizioBlocco > -1) {
      retval += stringaDaElaborare.substring(0,i);
      try {
        //Creo la stringa per il campo
        pBlocco = stringaDaElaborare.substring(inizioBlocco,fineBlocco);
        if (monoRecord && lblocchiN != null) {
        	bn = lblocchiN.getBlocco(pBlocco);
        }
        
        if (bn == null) {
          bn = new BloccoNested(pRequest, area, cm, pBlocco, listaDati, riga, dbOp.getConn(),monoRecord);
        } else {
          if (!bn.getArea().equalsIgnoreCase(area) || !bn.getCm().equalsIgnoreCase(cm) || !bn.getBlocco().equalsIgnoreCase(pBlocco)) {
            bn = new BloccoNested(pRequest, area, cm, pBlocco, listaDati, riga, dbOp.getConn(),monoRecord);
          }
        }
        bn.setRiga(riga);
        retval += bn.getValue();
      } catch (Exception e) {
        e.printStackTrace();
        retval += "<!-- Errore "+e.toString()+" -->\n";
      }
      stringaDaElaborare = stringaDaElaborare.substring(j);
      i = stringaDaElaborare.indexOf(BEGIN_BLOCCO);
      j = stringaDaElaborare.indexOf(END_BLOCCO,i) + END_BLOCCO.length();
      inizioBlocco = stringaDaElaborare.indexOf(BEGIN_NAME_BLOCCO);
      fineBlocco = stringaDaElaborare.indexOf(END_BLOCCO,inizioBlocco);
    }
    retval += stringaDaElaborare;
    
    //---------------------------------------
    if (ap != null && !monoRecord) {
       ap.setExtraKeys(extraKeys);
       retval = ap.bindingDeiParametri(retval);
    }

    //Debug Tempo
    stampaTempo("Multirecord::creaRiga - Inizio",area,cm,nomeBlocco,ptime);
    //Debug Tempo
    if (isTable || monoRecord || nested) {
      return retval;
    } else {
      return "<div class='AFCDataTD' >"+retval+"</div>";
    }
  }

  /**
   * 
   */
  private String leggiValore(String nomeCampo, int riga) {
    String retval ="";
    String valori = "";
    
    if (riga < listaDati.size()) {
      valori = (String)listaDati.get(riga);
    }
    int i = valori.indexOf("<C>"+nomeCampo.toUpperCase()+"</C>");
    if (i == -1) { 
      i = valori.indexOf("<C>"+nomeCampo+"</C>");
    }  
    if (i > -1) {
      int j = valori.indexOf("<V>",i)+3;
      int x = valori.indexOf("</V>",j);
      retval = valori.substring(j,x);
    } else {
      if (monoRecord) {
        retval = "";
      } else {
        retval = "<span >Campo "+nomeCampo+" non presente!</span>";
      }
    }

    return retval;
  }

  /**
   * La funzione ritorna il codice HTML del multirecord 
   * @param dbOp DbOperationSQL connessa al DB su cui effettuare la select
   * @param rigaIniziale Numero di riga da cui il multirecord deve essere visualizzato.
   * @param ap AbstractParser per l'eventuale valorizzazione di parametri presenti ne
   * codice HTML
   * @param navigatore Indica se visulaizzare o no il navigatore
   */
  public String creaHtml(IDbOperationSQL dbOp, 
                         int rigaIniziale, 
                         AbstractParser ap, 
                         boolean navigatore) throws Exception {
    int stepNav = getNumeroRecord();
    int i = 0;
    String myNav = "";
    String retval = "";

    if (ajax) {
      retval = "<script type='text/javascript'>\n"+
        "function ordinaBlocco"+nomeBlocco+"(elemento,ord) {\n"+
        "  f_AjaxBlocco('"+nomeServletAjax+"','"+nomeBlocco+"','"+nomeBlocco+"_BLCORDC',null,ord);\n"+
        "}\n"+
        "</script>\n";
    } else {
      retval = "<script type='text/javascript'>\n"+
        "function ordinaBlocco"+nomeBlocco+"(elemento,ord) {\n"+
        sWait+
        "  theForm = formParent"+nomeBlocco+"(elemento);\n"+
        "  var x = document.createElement('input');\n"+
        "  x.setAttribute('type','hidden');\n"+
        "  x.setAttribute('name','"+nomeBlocco+"_BLCORDC');\n"+
        "  x.setAttribute('value',ord);\n"+
        "  theForm.appendChild(x);\n"+
        "  reloadForm(theForm);\n"+
        "}\n"+
        "function formParent"+nomeBlocco+"(elemento) {\n"+
/*        "  myparent = elemento.parentElement;\n"+
        "  if (myparent.tagName == \"FORM\") {\n"+
        "     return myparent;\n"+
        "  }\n"+
        "  return formParent"+nomeBlocco+"(myparent);\n"+*/
        "  return document.getElementsByTagName('form')[0];\n"+
        "}\n"+
        "function reloadForm(theForm) {\n";
      if (letturaScrittura.equalsIgnoreCase("W")) {
         retval +=  "  var x = document.createElement('input');\n"+
            "  x.setAttribute('type','hidden');\n"+
            "  x.setAttribute('name','reload');\n"+
            "  x.setAttribute('value','1');\n"+
            "  theForm.appendChild(x);\n";
      }
      retval += "  theForm.submit();\n"+
        "}\n"+
        "</script>\n";
    }
    caricaDati(dbOp, rigaIniziale -1);
    if (domini && ld == null) {
      settaDominioCampo(dbOp);
      ld = new ListaDomini(dbOp.getConn());
    }

    if (isTable) {
      retval += headerHtml;
    } else {
      retval += "<div class='AFCFormTABLE' >\n";
      retval += headerHtml+"\n</div>\n";
    }
  
    while (i < stepNav) {
      retval += creaRiga(dbOp,i,ap)+"\n";
      i++;
    }
    myNav = creaNavigatore(rigaIniziale, navigatore)+"\n";
    if (ap != null) {
      retval += ap.bindingDeiParametri(myNav);
    } else {
      retval += myNav;
    }

    return retval;
  }

  /**
   * La funzione ritorna il codice HTML del multirecord 
   * @param dbOp DbOperationSQL connessa al DB su cui effettuare la select
   * @param request HttpServletRequest della pagina che incorpora il multirecord.
   * @param ap AbstractParser per l'eventuale valorizzazione di parametri presenti ne
   * codice HTML
   * @param navigatore Indica se visulaizzare o no il navigatore
   */
  public String creaHtml(IDbOperationSQL dbOp , 
                         HttpServletRequest request, 
                         AbstractParser ap, 
                         boolean navigatore) throws Exception {
    int rigaIniziale = 1;
    String primRec = request.getParameter(nomeBlocco+"_BLCNAV");
    if (primRec == null) {
      primRec = (String)request.getSession().getAttribute(nomeBlocco+"_BLCNAV");
    }
    if (primRec == null) {
      primRec = "1";
    }

    if (primRec.equalsIgnoreCase("")) {
      primRec = "1";
    }

    request.getSession().setAttribute(nomeBlocco+"_BLCNAV",primRec);
    rigaIniziale = Integer.parseInt(primRec);
    
    return creaHtml(dbOp, rigaIniziale, ap, navigatore);
  }

  /**
   * 
   */
  private String creaNavigatore(int riga, boolean navigatore) {
    String  retval = "";
    String  sPrec = "";
    String  sSucc = "";
    String  sAggi = "";
//    int     riga = 1;
//    if (rigaAttuale < recordTotali) {
//      riga = rigaAttuale;
//    }
    if (monoRecord) {
      return "";
    }
    if (riga > recordTotali) {
    	riga = 1;
    }
    int     step = getNumeroRecord();
    int     recPrec = riga - step;
    int     recSucc = riga + step; 
    int     recLast = recordTotali - (recordTotali%step) + 1;
    int     finePag = recSucc-1;
    
    
    if (letturaScrittura.equalsIgnoreCase("P") || nested) {
      return footerHtml;
    }
    if (navigatore && !letturaScrittura.equalsIgnoreCase("P")) {
      sPrec = "<a href='"+queryUrl+"&amp;urlCut=x&amp;"+nomeBlocco+"_BLCNAV=1' onclick='blocco"+nomeBlocco+"First(this); return false;' onkeypress='blocco"+nomeBlocco+"First(this); return false;' ><img style='border-style: none' src='"+pathThemes+"Themes/AFC/FirstOn.gif'  alt='Primo' /></a> &nbsp;\n"+
              "<a href='"+queryUrl+"&amp;urlCut=x&amp;"+nomeBlocco+"_BLCNAV="+recPrec+"' onclick='blocco"+nomeBlocco+"Prec(this); return false;' onkeypress='blocco"+nomeBlocco+"Prec(this); return false;' ><img style='border-style: none' src='"+pathThemes+"Themes/AFC/PrevOn.gif' alt='Precedente' /></a> &nbsp;\n";
      sSucc = "<a href='"+queryUrl+"&amp;urlCut=x&amp;"+nomeBlocco+"_BLCNAV="+recSucc+"' onclick='blocco"+nomeBlocco+"Succ(this); return false;' onkeypress='blocco"+nomeBlocco+"Succ(this); return false;' ><img style='border-style: none' src='"+pathThemes+"Themes/AFC/NextOn.gif' alt='Prossimo' /></a> &nbsp;\n"+
              "<a href='"+queryUrl+"&amp;urlCut=x&amp;"+nomeBlocco+"_BLCNAV="+recLast+"' onclick='blocco"+nomeBlocco+"Last(this); return false;' onkeypress='blocco"+nomeBlocco+"Last(this); return false;' ><img style='border-style: none' src='"+pathThemes+"Themes/AFC/LastOn.gif' alt='Ultimo' /></a> &nbsp;\n";
      if (aggiungi) {
        String mypath = "./";
        if (pathThemes.equalsIgnoreCase("../")) {
          mypath = "../common/";
        }
        sAggi = "&nbsp; <a class='AFCNavigatorLink' style='text-align: center' href='"+nuovaServlet+"' "+
                " onclick='window.open(this.href); return false;'\n"+
                " onkeypress='window.open(this.href); return false;'\n"+
                " title='Attenzione apre una nuova finestra'>"+
                "<img style='border: none' title='Nuovo documento' src='"+mypath+"images/gdm/document_new.gif' alt='Nuovo documento'/></a>&nbsp;\n";
      } else {
        sAggi = "&nbsp;\n";
      }
    } else {
      sAggi = "&nbsp;\n";
    }
    if (recPrec < 1) {
      recPrec = -1;
      sPrec = "";
    }
    if (recSucc > recordTotali) {
      recSucc = -1;
      finePag = recordTotali;
      sSucc = "";
    }
    if (recLast > recordTotali) {
      recLast = recordTotali - step + 1;
    }
    if (isTable) {
      if (!footerHtml.equalsIgnoreCase("")) {
        if (navigatore) {
          retval = footerHtml.replaceAll("type=\"ordinaBlocco","onclick=\"ordinaBlocco"+nomeBlocco);
        } else {
          retval = footerHtml;
        }
      }
      retval += "\n<div class='AFCFooterBLK'  style='text-align: center;' >\n"+
        "<span style='text-align: center; width: 20%'>&nbsp;</span>\n";
        if (recordTotali > 0) {
          retval += "<span style='text-align: center; width: 59%'>"+sPrec+riga+"&nbsp;-&nbsp;"+finePag+"&nbsp;di&nbsp;"+recordTotali+
          "&nbsp;\n"+sSucc+"</span>\n";
        } else {
          retval += "<span style='text-align: center; width: 59%'></span>";
        }
        retval += "<span style='text-align: right; width: 20%'>"+sAggi+"</span></div>\n";
    } else {
      retval = "\n<div class='AFCFooterBLK'  style='text-align: center;' >\n"+
        "<span style='text-align: center; width: 20%'>&nbsp;</span>\n";
        if (recordTotali > 0) {
          retval += "<span style='text-align: center; width: 59%'>"+sPrec+riga+"&nbsp;-&nbsp;"+finePag+"&nbsp;di&nbsp;"+recordTotali+
          "&nbsp;\n"+sSucc+"</span>\n";
        } else {
          retval += "<span style='text-align: center; width: 59%'></span>";
        }
        retval += "<span style='text-align: right; width: 20%'>"+sAggi+"</span></div>\n";
      if (!footerHtml.equalsIgnoreCase("")) {
        if (navigatore) {
          retval += "<div class='AFCDataTD' >"+footerHtml.replaceAll("type=\"ordinaBlocco","onclick=\"ordinaBlocco"+nomeBlocco)+"\n</div>\n";
        } else {
          retval += "<div class='AFCDataTD' >"+footerHtml+"\n</div>\n";
        }
      }
    }
//    String ricarica = "";
    if (ajax) {
      retval += "<input type='hidden' id='"+nomeBlocco+"_BLCNAV_F' value='1' />\n";
      retval += "<input type='hidden' id='"+nomeBlocco+"_BLCNAV_B' value='"+recPrec+"' />\n";
      retval += "<input type='hidden' id='"+nomeBlocco+"_BLCNAV_N' value='"+recSucc+"' />\n";
      retval += "<input type='hidden' id='"+nomeBlocco+"_BLCNAV_L' value='"+recLast+"' />\n";
      retval += "<script type='text/javascript'>\n"+
        "function eliminaDocumento"+nomeBlocco+"(elemento, area, cm, cr) {\n"+
        "  if (confirm('Attenzione questa operazione cancellerà il documento! Vuoi continuare?')) {\n"+
//        "    f_AjaxBlocco('"+nomeServletAjax+"','"+nomeBlocco+"','gdm_erase_area',null,area+'&amp;gdm_erase_cm='+cm+'&amp;gdm_erase_cr='+cr+'&amp;gdm_fase_erase=elimina');\n"+
        "    f_AjaxBlocco('"+nomeServletAjax+"','"+nomeBlocco+"','gdm_erase_area',null,area+'&gdm_erase_cm='+cm+'&gdm_erase_cr='+cr+'&gdm_fase_erase=elimina');\n"+
        "}\n}\n"+
        "function blocco"+nomeBlocco+"First(elemento) {\n"+
        "  f_AjaxBlocco('"+nomeServletAjax+"','"+nomeBlocco+"','"+nomeBlocco+"_BLCNAV','F',null);\n"+
        "}\n"+
        "function blocco"+nomeBlocco+"Prec(elemento) {\n"+
        "  f_AjaxBlocco('"+nomeServletAjax+"','"+nomeBlocco+"','"+nomeBlocco+"_BLCNAV','B',null);\n"+
        "}\n"+
        "function blocco"+nomeBlocco+"Succ(elemento) {\n"+
        "  f_AjaxBlocco('"+nomeServletAjax+"','"+nomeBlocco+"','"+nomeBlocco+"_BLCNAV','N',null);\n"+
        "}\n"+
        "function blocco"+nomeBlocco+"Last(elemento) {\n"+
        "  f_AjaxBlocco('"+nomeServletAjax+"','"+nomeBlocco+"','"+nomeBlocco+"_BLCNAV','L',null);\n"+
        "}\n"+
        "</script>\n";
    } else {
      retval += "<script type='text/javascript'>\n"+
        "function eliminaDocumento"+nomeBlocco+"(elemento, area, cm, cr) {\n"+
        sWait+
        "  if (confirm('Attenzione questa operazione cancellerà il documento! Vuoi continuare?')) {\n"+
        "  theForm = formParent"+nomeBlocco+"(elemento);\n"+
        "  var x = document.createElement('input');\n"+
        "  x.setAttribute('type','hidden');\n"+
        "  x.setAttribute('name','gdm_erase_area');\n"+
        "  x.setAttribute('value',area);\n"+
        "  theForm.appendChild(x);\n"+
        "  var x = document.createElement('input');\n"+
        "  x.setAttribute('type','hidden');\n"+
        "  x.setAttribute('name','gdm_erase_cm');\n"+
        "  x.setAttribute('value',cm);\n"+
        "  theForm.appendChild(x);\n"+
        "  var x = document.createElement('input');\n"+
        "  x.setAttribute('type','hidden');\n"+
        "  x.setAttribute('name','gdm_erase_cr');\n"+
        "  x.setAttribute('value',cr);\n"+
        "  theForm.appendChild(x);\n"+
        "  var x = document.createElement('input');\n"+
        "  x.setAttribute('type','hidden');\n"+
        "  x.setAttribute('name','gdm_fase_erase');\n"+
        "  x.setAttribute('value','elimina');\n"+
        "  theForm.appendChild(x);\n"+
        "  reloadForm(theForm);\n"+
        "}\n}\n"+
        "function blocco"+nomeBlocco+"First(elemento) {\n"+
        sWait+
        "  theForm = formParent"+nomeBlocco+"(elemento);\n"+
        "  var x = document.createElement('input');\n"+
        "  x.setAttribute('type','hidden');\n"+
        "  x.setAttribute('name','"+nomeBlocco+"_BLCNAV');\n"+
        "  x.setAttribute('value','1');\n"+
        "  theForm.appendChild(x);\n"+
        "  reloadForm(theForm);\n"+
        "}\n"+
        "function blocco"+nomeBlocco+"Prec(elemento) {\n"+
        sWait+
        "  theForm = formParent"+nomeBlocco+"(elemento);\n"+
        "  var x = document.createElement('input');\n"+
        "  x.setAttribute('type','hidden');\n"+
        "  x.setAttribute('name','"+nomeBlocco+"_BLCNAV');\n"+
        "  x.setAttribute('value','"+recPrec+"');\n"+
        "  theForm.appendChild(x);\n"+
        "  reloadForm(theForm);\n"+
        "}\n"+
        "function blocco"+nomeBlocco+"Succ(elemento) {\n"+
        sWait+
        "  theForm = formParent"+nomeBlocco+"(elemento);\n"+
        "  var x = document.createElement('input');\n"+
        "  x.setAttribute('type','hidden');\n"+
        "  x.setAttribute('name','"+nomeBlocco+"_BLCNAV');\n"+
        "  x.setAttribute('value','"+recSucc+"');\n"+
        "  theForm.appendChild(x);\n"+
        "  reloadForm(theForm);\n"+
        "}\n"+
        "function blocco"+nomeBlocco+"Last(elemento) {\n"+
        sWait+
        "  theForm = formParent"+nomeBlocco+"(elemento);\n"+
        "  var x = document.createElement('input');\n"+
        "  x.setAttribute('type','hidden');\n"+
        "  x.setAttribute('name','"+nomeBlocco+"_BLCNAV');\n"+
        "  x.setAttribute('value','"+recLast+"');\n"+
        "  theForm.appendChild(x);\n"+
        "  reloadForm(theForm);\n"+
        "}\n"+
        "</script>\n";
    }

    return retval;
  }

  /**
   * 
   */
  private String campiOrdinamento(String strHtml) {
    String str = strHtml;
    String campoOrd = "";
    String retval = "";
    
    int x = 0;
    int y = -1;
    int z = -1;

    while (x > -1) {
      x = str.indexOf("<a");
      if (x > -1) {
        y = str.indexOf("#sorter",x);
        z = str.indexOf("</a>",y);
        if (z > y) {
          retval += str.substring(0,x)+ "<a class='AFCSorterLink' href='"+queryUrl;
          campoOrd = str.substring(y,z);
          str = str.substring(z);

          int posNomeI = campoOrd.indexOf("<!-- ") + 5;
          int posNomeF = campoOrd.indexOf(" -->");
          int posFin = campoOrd.indexOf("&gt;");
          String nomeCampo = campoOrd.substring(posNomeI,posNomeF);
          String visuCampo = campoOrd.substring(posNomeF+4,posFin);
          retval += "&amp;urlCut=x&amp;"+nomeBlocco+"_BLCORDC="+nomeCampo+"' ";
          retval += " onclick='ordinaBlocco"+nomeBlocco+"(this,\""+nomeCampo+"\"); return false;' onkeypress='ordinaBlocco"+nomeBlocco+"(this,\""+nomeCampo+"\"); return false;' >";
          retval += visuCampo;
        } else {
          retval += str.substring(0,z);
          str = str.substring(z);
        }
      } else {
        retval += str;
      }
    }
    return retval;
  }

  /**
   * 
   */
  public void setThemesPath(String newPath) {
    if (newPath.equalsIgnoreCase("")) {
      pathThemes = "";
    } else {
      pathThemes = newPath+"/";
    }
  }

  /**
   * 
   */
  private void setNomeBlocco(String pNome) {
    nomeBlocco = pNome;
  }

  /**
   * 
   */
  public void setMono(boolean pMono) {
    isMono = pMono;
  }

  /**
   * 
   */
  public void setMonoRecord(boolean pVisual) {
    monoRecord = pVisual;
  }

  public void setWait(boolean pWait, String pAmbiente) {
    bWait = pWait;
    if (bWait) {
      if (pAmbiente.equalsIgnoreCase("")) {
      	sWait  = "if (navigator.appName.indexOf('Netscape') == -1) {\n";
        sWait += "  window.showModelessDialog('AmvMessaggi.html','','dialogHeight: 100px; dialogWidth: 300px; dialogTop: 300px; dialogLeft: 400px; edge: Raised; center: Yes; help: No; resizable: No; status: No;');\n}\n";
      } else {
      	sWait  = "if (navigator.appName.indexOf('Netscape') == -1) {\n";
        sWait = "../../"+Parametri.APPLICATIVO+"/window.showModelessDialog('AmvMessaggi.html','','dialogHeight: 100px; dialogWidth: 300px; dialogTop: 300px; dialogLeft: 400px; edge: Raised; center: Yes; help: No; resizable: No; status: No;');\n}\n";
      }
    } else {
      sWait = "";
    }
  }

  /**
   * 
   */
  public void setAjax(boolean pAjax) {
    ajax = pAjax;
  }

  /**
   * 
   */
  public void setNested(boolean pNested) {
    nested = pNested;
  }

  /**
   * 
   */
  public void setEscludiCompetenze(boolean pnoCompetenze) {
    noCompetenze = pnoCompetenze;
  }

  /**
   * 
   */
  private void setNomeServlet(HttpServletRequest request) {
    if (request == null) {
      nomeServlet = "";
      pDo = "";
    } else {
      pDo = (String)request.getSession().getAttribute("pdo");
      if (pDo == null) {
        pDo = "CC";
      }
      String nomeServ = (String)request.getSession().getAttribute("p_nomeservlet");
      if (pDo.equalsIgnoreCase("HR")) {
        int j = nomeServ.indexOf("?");
        nomeServlet = nomeServ.substring(0,j);
      } 
      if (pDo.equalsIgnoreCase("CC")) {
        nomeServlet = nomeServ+".do";
      } 
      if (pDo.equalsIgnoreCase("")) {
        nomeServlet = nomeServ;
      } 
      if (pDo.equalsIgnoreCase("HR") || pDo.equalsIgnoreCase("CC")) {
        nomeServletAjax = "GdmAjax.do";
      } 
      if (pDo.equalsIgnoreCase("")) {
        nomeServletAjax = "ServletModulistica";
      } 
    }
  }
  
  /**
   * La funzione ritorna il codice HTML del multirecord 
   * @param dbOp DbOperationSQL connessa al DB su cui eseguire l'eventuale function
   * @param request HttpServletRequest della pagina che incorpora il multirecord.
   * @param rigaIniziale Numero di riga da cui il multirecord deve essere visualizzato.
   * @param ap AbstractParser per l'eventuale valorizzazione di parametri presenti nel
   * codice HTML
   * @param navigatore Indica se visulaizzare o no il navigatore
   */
  public String creaHtmlXML(IDbOperationSQL dbOp , 
                         HttpServletRequest request, 
                         AbstractParser ap, 
                         boolean navigatore) throws Exception {
    int rigaIniziale = 1;
    String primRec = request.getParameter(nomeBlocco+"_BLCNAV");
    if (primRec == null) {
      primRec = (String)request.getSession().getAttribute(nomeBlocco+"_BLCNAV");
    }
    if (primRec == null) {
      primRec = "1";
    }

    if (primRec.equalsIgnoreCase("")) {
      primRec = "1";
    }

    request.getSession().setAttribute(nomeBlocco+"_BLCNAV",primRec);
    rigaIniziale = Integer.parseInt(primRec);
    sXml = sXml.replaceAll("</FUNCTION_INPUT>","<RECORD>");
    sXml += "<NUMERO_INIZIALE>"+rigaIniziale+"</NUMERO_INIZIALE>";
    sXml += "<QUANTITA>"+numeroRecord+"</QUANTITA>";
    sXml += "<ORDINAMENTO>"+ordinamento+"</ORDINAMENTO></RECORD></FUNCTION_INPUT>";
    int stepNav = getNumeroRecord();
    int i = 0;
    String myNav = "";
    String retval = "";

    if (ajax) {
      retval = "<script type='text/javascript'>\n"+
        "function ordinaBlocco"+nomeBlocco+"(elemento,ord) {\n"+
        "  f_AjaxBlocco('"+nomeServletAjax+"','"+nomeBlocco+"','"+nomeBlocco+"_BLCORDC',null,ord);\n"+
        "}\n"+
        "</script>\n";
    } else {
      retval = "<script type='text/javascript'>\n"+
        "function ordinaBlocco"+nomeBlocco+"(elemento,ord) {\n"+
        sWait+
        "  theForm = formParent"+nomeBlocco+"(elemento);\n"+
        "  var x = document.createElement('input');\n"+
        "  x.setAttribute('type','hidden');\n"+
        "  x.setAttribute('name','"+nomeBlocco+"_BLCORDC');\n"+
        "  x.setAttribute('value',ord);\n"+
        "  theForm.appendChild(x);\n"+
        "  reloadForm(theForm);\n"+
        "}\n"+
        "function formParent"+nomeBlocco+"(elemento) {\n"+
/*        "  myparent = elemento.parentElement;\n"+
        "  if (myparent.tagName == \"FORM\") {\n"+
        "     return myparent;\n"+
        "  }\n"+
        "  return formParent"+nomeBlocco+"(myparent);\n"+*/
        "  return document.getElementsByTagName('form')[0];\n"+
        "}\n"+
        "function reloadForm(theForm) {\n";
      if (letturaScrittura.equalsIgnoreCase("W")) {
         retval +=  "  var x = document.createElement('input');\n"+
            "  x.setAttribute('type','hidden');\n"+
            "  x.setAttribute('name','reload');\n"+
            "  x.setAttribute('value','1');\n"+
            "  theForm.appendChild(x);\n";
      }
      retval += "  theForm.submit();\n"+
        "}\n"+
        "</script>\n";
    }

      caricaDatiXML(dbOp, rigaIniziale -1);
      if (domini && ld == null) {
        settaDominioCampo(dbOp);
        ld = new ListaDomini(dbOp.getConn());
      }
      if (!erroreMsg.equalsIgnoreCase("")) {
        retval += "<div>"+erroreMsg+"</div>\n";
      }
      if (isTable || nested) {
        retval += headerHtml;
      } else {
        retval += "<div class='AFCFormTABLE' >\n";
        retval += headerHtml+"\n</div>\n";
      }
    
      while (i < stepNav) {
        retval += creaRiga(dbOp,i,ap)+"\n";
        i++;
      }
      myNav = creaNavigatore(rigaIniziale, navigatore)+"\n";
      if (ap != null) {
        retval += ap.bindingDeiParametri(myNav);
      } else {
        retval += myNav;
      }

    return retval;
  }

  /**
   * La funzione ritorna il codice HTML del multirecord 
   * @param dbOp DbOperationSQL connessa al DB per il calcolo di eventuali domini
   * @param sProperties percorso del file di propterties del DMserver
   * @param request HttpServletRequest della pagina che incorpora il multirecord.
   * @param rigaIniziale Numero di riga da cui il multirecord deve essere visualizzato.
   * @param ap AbstractParser per l'eventuale valorizzazione di parametri presenti nel
   * codice HTML
   * @param navigatore Indica se visulaizzare o no il navigatore
   */
  public String creaHtmlIQuery(IDbOperationSQL dbOp,
                         HttpServletRequest request, 
                         AbstractParser ap, 
                         boolean navigatore,
                         String pArea,
                         String codMod) throws Exception {
    int rigaIniziale = 1;
    String primRec = request.getParameter(nomeBlocco+"_BLCNAV");
    if (primRec == null) {
      HttpSession session = request.getSession();
      Object obj = session.getAttribute(nomeBlocco+"_BLCNAV");
      if (obj != null) {
        primRec = (String)obj;
      }
    }
    if (primRec == null) {
      primRec = "1";
    }

    if (primRec.equalsIgnoreCase("")) {
      primRec = "1";
    }

    request.getSession().setAttribute(nomeBlocco+"_BLCNAV",primRec);
    rigaIniziale = Integer.parseInt(primRec);
    int stepNav = getNumeroRecord();
    int i = 0;
    String myNav = "";
    String retval = "";

    if (ajax) {
      retval = "<script type='text/javascript'>\n"+
        "function ordinaBlocco"+nomeBlocco+"(elemento,ord) {\n"+
        "  f_AjaxBlocco('"+nomeServletAjax+"','"+nomeBlocco+"','"+nomeBlocco+"_BLCORDC',null,ord);\n"+
        "}\n"+
        "</script>\n";
    } else {
      retval = "<script type='text/javascript'>\n"+
        "function ordinaBlocco"+nomeBlocco+"(elemento,ord) {\n"+
        sWait+
        "  theForm = formParent"+nomeBlocco+"(elemento);\n"+
        "  var x = document.createElement('input');\n"+
        "  x.setAttribute('type','hidden');\n"+
        "  x.setAttribute('name','"+nomeBlocco+"_BLCORDC');\n"+
        "  x.setAttribute('value',ord);\n"+
        "  theForm.appendChild(x);\n"+
        "  reloadForm(theForm);\n"+
        "}\n"+
        "function formParent"+nomeBlocco+"(elemento) {\n"+
/*        "  myparent = elemento.parentElement;\n"+
        "  if (myparent.tagName == \"FORM\") {\n"+
        "     return myparent;\n"+
        "  }\n"+
        "  return formParent"+nomeBlocco+"(myparent);\n"+*/
        "  return document.getElementsByTagName('form')[0];\n"+
        "}\n"+
        "function reloadForm(theForm) {\n";
      if (letturaScrittura.equalsIgnoreCase("W")) {
         retval +=  "  var x = document.createElement('input');\n"+
            "  x.setAttribute('type','hidden');\n"+
            "  x.setAttribute('name','reload');\n"+
            "  x.setAttribute('value','1');\n"+
            "  theForm.appendChild(x);\n";
      }
      retval += "  theForm.submit();\n"+
        "}\n"+
        "</script>\n";
    }

    caricaDatiIQuery(dbOp.getConn(), rigaIniziale -1, pArea, codMod, ap);
    if (domini && ld == null) {
      settaDominioCampo(dbOp);
      ld = new ListaDomini(dbOp.getConn());
    }
    if (isTable || nested) {
      retval += headerHtml;
    } else {
      retval += "<div class='AFCFormTABLE' >\n";
      retval += headerHtml+"\n</div>\n";
    }
    if (monoRecord) {
      if (isTable) {
        retval = "<table width='100%'>";
      } else {
        retval = "";
      }
    }
    while (i < stepNav) {
      retval += creaRiga(dbOp,i,ap)+"\n";
      i++;
    }
    if (!monoRecord) {
      myNav = creaNavigatore(rigaIniziale, navigatore)+"\n";
      if (ap != null) {
        retval += ap.bindingDeiParametri(myNav);
      } else {
        retval += myNav;
      }
    } else {
      if (isTable) {
        retval += "</table>";
      }
    }

    return retval;
  }

  /**
   * La funzione ritorna il codice HTML del multirecord 
   * @param dbOp DbOperationSQL connessa al DB per il calcolo di eventuali domini
   * @param sProperties percorso del file di propterties del DMserver
   * @param request HttpServletRequest della pagina che incorpora il multirecord.
   * @param rigaIniziale Numero di riga da cui il multirecord deve essere visualizzato.
   * @param ap AbstractParser per l'eventuale valorizzazione di parametri presenti nel
   * codice HTML
   * @param navigatore Indica se visulaizzare o no il navigatore
   */
  public String creaHtmlNestedMonorecord(IDbOperationSQL dbOp,
                         HttpServletRequest request, 
                         AbstractParser ap, 
                         LinkedList<String>  lDati,
                         String pArea,
                         String codMod) throws Exception {
    int rigaIniziale = 1;
    String primRec = request.getParameter(nomeBlocco+"_BLCNAV");
    if (primRec == null) {
      HttpSession session = request.getSession();
      Object obj = session.getAttribute(nomeBlocco+"_BLCNAV");
      if (obj != null) {
        primRec = (String)obj;
      }
    }
    if (primRec == null) {
      primRec = "1";
    }

    if (primRec.equalsIgnoreCase("")) {
      primRec = "1";
    }

    request.getSession().setAttribute(nomeBlocco+"_BLCNAV",primRec);
    rigaIniziale = Integer.parseInt(primRec);
    int stepNav = getNumeroRecord();
    int i = 0;
    String myNav = "";
    String retval = "";

    if (ajax) {
      retval = "<script type='text/javascript'>\n"+
        "function ordinaBlocco"+nomeBlocco+"(elemento,ord) {\n"+
        "  f_AjaxBlocco('"+nomeServletAjax+"','"+nomeBlocco+"','"+nomeBlocco+"_BLCORDC',null,ord);\n"+
        "}\n"+
        "</script>\n";
    } else {
      retval = "<script type='text/javascript'>\n"+
        "function ordinaBlocco"+nomeBlocco+"(elemento,ord) {\n"+
        sWait+
        "  theForm = formParent"+nomeBlocco+"(elemento);\n"+
        "  var x = document.createElement('input');\n"+
        "  x.setAttribute('type','hidden');\n"+
        "  x.setAttribute('name','"+nomeBlocco+"_BLCORDC');\n"+
        "  x.setAttribute('value',ord);\n"+
        "  theForm.appendChild(x);\n"+
        "  reloadForm(theForm);\n"+
        "}\n"+
        "function formParent"+nomeBlocco+"(elemento) {\n"+
/*        "  myparent = elemento.parentElement;\n"+
        "  if (myparent.tagName == \"FORM\") {\n"+
        "     return myparent;\n"+
        "  }\n"+
        "  return formParent"+nomeBlocco+"(myparent);\n"+*/
        "  return document.getElementsByTagName('form')[0];\n"+
        "}\n"+
        "function reloadForm(theForm) {\n";
      if (letturaScrittura.equalsIgnoreCase("W")) {
         retval +=  "  var x = document.createElement('input');\n"+
            "  x.setAttribute('type','hidden');\n"+
            "  x.setAttribute('name','reload');\n"+
            "  x.setAttribute('value','1');\n"+
            "  theForm.appendChild(x);\n";
      }
      retval += "  theForm.submit();\n"+
        "}\n"+
        "</script>\n";
    }

    listaDati = lDati;
//    caricaDatiIQuery(dbOp.getConn(), rigaIniziale -1, pArea, codMod, ap);
    if (domini && ld == null) {
      settaDominioCampo(dbOp);
      ld = new ListaDomini(dbOp.getConn());
    }
    if (isTable || nested) {
      retval += headerHtml;
    } else {
      retval += "<div class='AFCFormTABLE' >\n";
      retval += headerHtml+"\n</div>\n";
    }
    if (monoRecord) {
      if (isTable) {
        retval = "<table width='100%'>";
      } else {
        retval = "";
      }
    }
    while (i < stepNav) {
      retval += creaRiga(dbOp,i,ap)+"\n";
      i++;
    }
    if (!monoRecord) {
      myNav = creaNavigatore(rigaIniziale, false)+"\n";
      if (ap != null) {
        retval += ap.bindingDeiParametri(myNav);
      } else {
        retval += myNav;
      }
    } else {
      if (isTable) {
        retval += "</table>";
      }
    }

    return retval;
  }
  
/*  public void esportaDatiExcell(IDbOperationSQL dbOp, HashMapSet hms, OutputStream os, boolean tutto) throws Exception {
  	String sql = "";
  	String tipoDoc = "";
  	String iddoc = "";
  	String area = "";
  	String cm = "";
  	String tabella = "";
  	String funzione = "";
  	String blocco = "";
  	String corpo = "";
  	int pagina = 0;
  	ResultSet rst = null;
  	
  	
		WritableWorkbook workbook = Workbook.createWorkbook(os);
    Iterator i = hms.getIterator();
    while (i.hasNext()) {
    	pagina++;
    	tipoDoc = ""+i.next();
    	if (tutto) {
    		listaCampi = new LinkedList<String>();
      	sql = "SELECT  AREA, CODICE_MODELLO,  F_NOME_TABELLA (AREA, CODICE_MODELLO) TABELLA "+
             	"FROM MODELLI   "+
             	"WHERE ID_TIPODOC = :ID_TIPODOC";
      	dbOp.setStatement(sql);
      	dbOp.setParameter(":ID_TIPODOC", tipoDoc);
      	dbOp.execute();
      	rst = dbOp.getRstSet();
      	if (rst.next()) {
      		area = rst.getString("AREA");
      		cm = rst.getString("CODICE_MODELLO");
      		tabella = rst.getString("TABELLA");
      		if (tabella == null) {
      			tabella = "";
      		}
      	}
      	sql = "SELECT  D.DATO "+
             	"FROM MODELLI M, DATI_MODELLO D  "+
             	"WHERE ID_TIPODOC = :ID_TIPODOC "+
             	"AND D.AREA = M.AREA "+
             	"AND D.CODICE_MODELLO = M.CODICE_MODELLO "+
             	"AND D.IN_USO = 'Y' "+
             	"ORDER BY 1 ASC";
      	settaCampo("AREA",null);
      	settaCampo("CODICE_MODELLO",null);
      	settaCampo("CODICE_RICHIESTA",null);
      	dbOp.setStatement(sql);
      	dbOp.setParameter(":ID_TIPODOC", tipoDoc);
      	dbOp.execute();
      	rst = dbOp.getRstSet();
      	while (rst.next()) {
   				settaCampo(rst.getString(1),null);
      	}
    	} else {
      	sql = "SELECT M.AREA AREA, "+
      				"M.CODICE_MODELLO CODICE_MODELLO, "+
      				"F_NOME_TABELLA (M.AREA, M.CODICE_MODELLO) TABELLA, NOTE_INTERNE, "+
      				"M.BLOCCO_JDMS BLOCCO, "+
      				"B.CORPO CORPO "+
      				"FROM MODELLI M, BLOCCHI B "+
      				"WHERE ID_TIPODOC = :ID_TIPODOC "+
      				"AND B.AREA = M.AREA "+
      				"AND B.BLOCCO = M.BLOCCO_JDMS";
      	dbOp.setStatement(sql);
      	dbOp.setParameter(":ID_TIPODOC", tipoDoc);
      	dbOp.execute();
      	rst = dbOp.getRstSet();
      	if (rst.next()) {
      		area = rst.getString("AREA");
      		cm = rst.getString("CODICE_MODELLO");
      		tabella = rst.getString("TABELLA");
      		if (tabella == null) {
      			tabella = "";
      		}
      		funzione = rst.getString("NOTE_INTERNE");
      		if (funzione == null) {
      			funzione = "";
      		}
      		blocco = rst.getString("BLOCCO");
          BufferedInputStream bis = dbOp.readClob("CORPO");
          StringBuffer sb = new StringBuffer();
          int ic;
          while ((ic =  bis.read()) != -1) {
            sb.append((char)ic);
          }
          corpo = sb.toString();
      	}
      	
        setNomeBlocco(blocco);
        setCorpo(corpo);
        listaCampi = new LinkedList<String>();
        setListaCampi(corpoHtml,true);
    	}
//        letturaScrittura = "W";
//        setLegame("");
//        setNumeroRecord(1);
//        setNomeServlet(null);
//        noCompetenze = true;


    	iddocs = new Vector<String>();
      Iterator iIntern = hms.getHashSet(tipoDoc);
 	   	while (iIntern.hasNext()) {    
 	       iddoc = (String)iIntern.next();
 	       iddocs.add(iddoc);
 	   	}
      caricaDatiExcell(dbOp.getConn(), workbook, area, cm, pagina, tabella, funzione, dbOp);
    }

    workbook.write(); 
    workbook.close();
//    os.flush();
//    os.close();
  }*/
  
  public InputStream esportaDatiExcellToInputStream(IDbOperationSQL dbOp, HashMapSet hms, boolean tutto) throws Exception {
	  return esportaDatiExcellToInputStream(dbOp, hms, tutto, "", "");
  }
  
  public InputStream esportaDatiExcellToInputStream(IDbOperationSQL dbOp, HashMapSet hms, boolean tutto, String func_Ricerca) throws Exception {
	  return esportaDatiExcellToInputStream(dbOp, hms, tutto, func_Ricerca, "");
  }
  
  public InputStream esportaDatiExcellToInputStream(IDbOperationSQL dbOp, HashMapSet hms, boolean tutto, String func_Ricerca, String utente) throws Exception {
	  	String sql = "";
	  	String tipoDoc = "";
	  	String iddoc = "";
	  	String area = "";
	  	String cm = "";
	  	String tabella = "";
	  	String funzione = "";
	  	String blocco = "";
	  	String corpo = "";
	  	int pagina = 0;
	  	ResultSet rst = null;
	  	InputStream is =null;
	  	
	  	try{
			ByteArrayOutputStream ous = new ByteArrayOutputStream();
	  	
			WritableWorkbook workbook = Workbook.createWorkbook(ous);
			Iterator i = hms.getIterator();
			
			if (func_Ricerca != null && func_Ricerca.length()> 0) {
				iddocs = hms.getAllHashSet();
				pagina++;
				tipoDoc = ""+i.next();
		      	sql = "SELECT M.AREA AREA, "+
	      				"M.CODICE_MODELLO CODICE_MODELLO, "+
	      				"M.BLOCCO_JDMS BLOCCO, "+
	      				"B.CORPO CORPO "+
	      				"FROM MODELLI M, BLOCCHI B "+
	      				"WHERE ID_TIPODOC = :ID_TIPODOC "+
	      				"AND B.AREA = M.AREA "+
	      				"AND B.BLOCCO = M.BLOCCO_JDMS";
		      	dbOp.setStatement(sql);
		      	dbOp.setParameter(":ID_TIPODOC", tipoDoc);
		      	dbOp.execute();
		      	rst = dbOp.getRstSet();
		      	if (rst.next()) {
		      		area = rst.getString("AREA");
		      		cm = rst.getString("CODICE_MODELLO");
		      	}
		      	caricaDatiExcell(dbOp.getConn(), workbook, area, cm, pagina, tabella, func_Ricerca, dbOp, utente);
			} else {
				
			    while (i.hasNext()) {
			    	pagina++;
			    	tipoDoc = ""+i.next();
			    	if (tutto) {
			    		listaCampi = new LinkedList<String>();
				      	sql = "SELECT  AREA, CODICE_MODELLO,  F_NOME_TABELLA(AREA, CODICE_MODELLO) TABELLA "+
				             	"FROM MODELLI   "+
				             	"WHERE ID_TIPODOC = :ID_TIPODOC";
				      	dbOp.setStatement(sql);
				      	dbOp.setParameter(":ID_TIPODOC", tipoDoc);
				      	dbOp.execute();
				      	rst = dbOp.getRstSet();
				      	if (rst.next()) {
				      		area = rst.getString("AREA");
				      		cm = rst.getString("CODICE_MODELLO");
				      		tabella = rst.getString("TABELLA");
				      		if (tabella == null) {
				      			tabella = "";
				      		}
				      	}
				      	sql = "SELECT  D.DATO "+
				             	"FROM MODELLI M, DATI_MODELLO D  "+
				             	"WHERE ID_TIPODOC = :ID_TIPODOC "+
				             	"AND D.AREA = M.AREA "+
				             	"AND D.CODICE_MODELLO = M.CODICE_MODELLO "+
				             	"AND D.IN_USO = 'Y' "+
				             	"ORDER BY 1 ASC";
				      	settaCampo("AREA",null);
				      	settaCampo("CODICE_MODELLO",null);
				      	settaCampo("CODICE_RICHIESTA",null);
				      	dbOp.setStatement(sql);
				      	dbOp.setParameter(":ID_TIPODOC", tipoDoc);
				      	dbOp.execute();
				      	rst = dbOp.getRstSet();
				      	while (rst.next()) {
				   				settaCampo(rst.getString(1),null);
				      	}
			    	} else {
				      	sql = "SELECT M.AREA AREA, "+
			      				"M.CODICE_MODELLO CODICE_MODELLO, "+
			      				"F_NOME_TABELLA (M.AREA, M.CODICE_MODELLO) TABELLA, NOTE_INTERNE, "+
			      				"M.BLOCCO_JDMS BLOCCO, "+
			      				"B.CORPO CORPO "+
			      				"FROM MODELLI M, BLOCCHI B "+
			      				"WHERE ID_TIPODOC = :ID_TIPODOC "+
			      				"AND B.AREA = M.AREA "+
			      				"AND B.BLOCCO = M.BLOCCO_JDMS";
				      	dbOp.setStatement(sql);
				      	dbOp.setParameter(":ID_TIPODOC", tipoDoc);
				      	dbOp.execute();
				      	rst = dbOp.getRstSet();
				      	if (rst.next()) {
				      		area = rst.getString("AREA");
				      		cm = rst.getString("CODICE_MODELLO");
				      		tabella = rst.getString("TABELLA");
				      		if (tabella == null) {
				      			tabella = "";
			      		}
			      		funzione = rst.getString("NOTE_INTERNE");
			      		if (funzione == null) {
			      			funzione = "";
			      		}
			      		blocco = rst.getString("BLOCCO");
			      		BufferedInputStream bis = dbOp.readClob("CORPO");
			      		StringBuffer sb = new StringBuffer();
			      		int ic;
			      		while ((ic =  bis.read()) != -1) {
			      			sb.append((char)ic);
			      		}
			      		corpo = sb.toString();
				      	}
				      	setNomeBlocco(blocco);
				      	setCorpo(corpo);
				      	listaCampi = new LinkedList<String>();
				      	setListaCampi(corpoHtml,true);
			    	}
		
			    	iddocs = new Vector<String>();
			    	Iterator iIntern = hms.getHashSet(tipoDoc);
			 	   	while (iIntern.hasNext()) {    
			 	       iddoc = (String)iIntern.next();
			 	       iddocs.add(iddoc);
			 	   	}
	
			 	   	caricaDatiExcell(dbOp.getConn(), workbook, area, cm, pagina, tabella, funzione, dbOp, utente);
			    }
			}

		    workbook.write(); 
		    workbook.close();
		    is = (InputStream)new ByteArrayInputStream(ous.toByteArray());
		   
		}
		catch(Exception e){
			e.printStackTrace();			
		}		
	  	
	  	return is;
	  }
  
  public void esportaDatiExcell(IDbOperationSQL dbOp, HashMapSet hms, OutputStream ous, boolean tutto) throws Exception {
	esportaDatiExcell(dbOp, hms, ous, tutto, "","");  
  }
  
  public void esportaDatiExcell(IDbOperationSQL dbOp, HashMapSet hms, OutputStream ous, boolean tutto, String func_Ricerca,String utente) throws Exception {
	  	String sql = "";
	  	String tipoDoc = "";
	  	String iddoc = "";
	  	String area = "";
	  	String cm = "";
	  	String tabella = "";
	  	String funzione = "";
	  	String blocco = "";
	  	String corpo = "";
	  	int pagina = 0;
	  	ResultSet rst = null;
	  	InputStream is =null;
	  	
	  	try{
			WritableWorkbook workbook = Workbook.createWorkbook(ous);
			Iterator i = hms.getIterator();
			
			if (func_Ricerca != null && func_Ricerca.length()> 0) {
				iddocs = hms.getAllHashSet();
				pagina++;
				tipoDoc = ""+i.next();
		      	sql = "SELECT M.AREA AREA, "+
	      				"M.CODICE_MODELLO CODICE_MODELLO, "+
	      				"M.BLOCCO_JDMS BLOCCO, "+
	      				"B.CORPO CORPO "+
	      				"FROM MODELLI M, BLOCCHI B "+
	      				"WHERE ID_TIPODOC = :ID_TIPODOC "+
	      				"AND B.AREA = M.AREA "+
	      				"AND B.BLOCCO = M.BLOCCO_JDMS";
		      	dbOp.setStatement(sql);
		      	dbOp.setParameter(":ID_TIPODOC", tipoDoc);
		      	dbOp.execute();
		      	rst = dbOp.getRstSet();
		      	if (rst.next()) {
		      		area = rst.getString("AREA");
		      		cm = rst.getString("CODICE_MODELLO");
		      	}
/*		      		blocco = rst.getString("BLOCCO");
		      		BufferedInputStream bis = dbOp.readClob("CORPO");
		      		StringBuffer sb = new StringBuffer();
		      		int ic;
		      		while ((ic =  bis.read()) != -1) {
		      			sb.append((char)ic);
		      		}
		      		corpo = sb.toString();
		      	}
		      	setNomeBlocco(blocco);
		      	setCorpo(corpo);
		      	listaCampi = new LinkedList<String>();
		      	setListaCampi(corpoHtml,true);*/
		      	caricaDatiExcell(dbOp.getConn(), workbook, area, cm, pagina, tabella, func_Ricerca, dbOp,utente);
			} else {
				
			    while (i.hasNext()) {
			    	pagina++;
			    	tipoDoc = ""+i.next();
			    	if (tutto) {
			    		listaCampi = new LinkedList<String>();
				      	sql = "SELECT  AREA, CODICE_MODELLO,  F_NOME_TABELLA(AREA, CODICE_MODELLO) TABELLA "+
				             	"FROM MODELLI   "+
				             	"WHERE ID_TIPODOC = :ID_TIPODOC";
				      	dbOp.setStatement(sql);
				      	dbOp.setParameter(":ID_TIPODOC", tipoDoc);
				      	dbOp.execute();
				      	rst = dbOp.getRstSet();
				      	if (rst.next()) {
				      		area = rst.getString("AREA");
				      		cm = rst.getString("CODICE_MODELLO");
				      		tabella = rst.getString("TABELLA");
				      		if (tabella == null) {
				      			tabella = "";
				      		}
				      	}
				      	sql = "SELECT  D.DATO "+
				             	"FROM MODELLI M, DATI_MODELLO D  "+
				             	"WHERE ID_TIPODOC = :ID_TIPODOC "+
				             	"AND D.AREA = M.AREA "+
				             	"AND D.CODICE_MODELLO = M.CODICE_MODELLO "+
				             	"AND D.IN_USO = 'Y' "+
				             	"ORDER BY 1 ASC";
				      	settaCampo("AREA",null);
				      	settaCampo("CODICE_MODELLO",null);
				      	settaCampo("CODICE_RICHIESTA",null);
				      	dbOp.setStatement(sql);
				      	dbOp.setParameter(":ID_TIPODOC", tipoDoc);
				      	dbOp.execute();
				      	rst = dbOp.getRstSet();
				      	while (rst.next()) {
				   				settaCampo(rst.getString(1),null);
				      	}
			    	} else {
				      	sql = "SELECT M.AREA AREA, "+
			      				"M.CODICE_MODELLO CODICE_MODELLO, "+
			      				"F_NOME_TABELLA (M.AREA, M.CODICE_MODELLO) TABELLA, NOTE_INTERNE, "+
			      				"M.BLOCCO_JDMS BLOCCO, "+
			      				"B.CORPO CORPO "+
			      				"FROM MODELLI M, BLOCCHI B "+
			      				"WHERE ID_TIPODOC = :ID_TIPODOC "+
			      				"AND B.AREA = M.AREA "+
			      				"AND B.BLOCCO = M.BLOCCO_JDMS";
				      	dbOp.setStatement(sql);
				      	dbOp.setParameter(":ID_TIPODOC", tipoDoc);
				      	dbOp.execute();
				      	rst = dbOp.getRstSet();
				      	if (rst.next()) {
				      		area = rst.getString("AREA");
				      		cm = rst.getString("CODICE_MODELLO");
				      		tabella = rst.getString("TABELLA");
				      		if (tabella == null) {
				      			tabella = "";
			      		}
			      		funzione = rst.getString("NOTE_INTERNE");
			      		if (funzione == null) {
			      			funzione = "";
			      		}
			      		blocco = rst.getString("BLOCCO");
			      		BufferedInputStream bis = dbOp.readClob("CORPO");
			      		StringBuffer sb = new StringBuffer();
			      		int ic;
			      		while ((ic =  bis.read()) != -1) {
			      			sb.append((char)ic);
			      		}
			      		corpo = sb.toString();
				      	}
				      	setNomeBlocco(blocco);
				      	setCorpo(corpo);
				      	listaCampi = new LinkedList<String>();
				      	setListaCampi(corpoHtml,true);
			    	}
		
			    	iddocs = new Vector<String>();
			    	Iterator iIntern = hms.getHashSet(tipoDoc);
			 	   	while (iIntern.hasNext()) {    
			 	       iddoc = (String)iIntern.next();
			 	       iddocs.add(iddoc);
			 	   	}
	
			 	   	caricaDatiExcell(dbOp.getConn(), workbook, area, cm, pagina, tabella, funzione, dbOp,utente);
			    }
			}

		    workbook.write(); 
		    workbook.close();
//		    is = (InputStream)new ByteArrayInputStream(ous.toByteArray());
		   
		}
		catch(Exception e){
			e.printStackTrace();			
		}		
	  	
//	  	return is;
	  }

  /**
   * 
   */
  public static void main_no(String[] args) {
  	Connection conn=null;
		try {
    	Class.forName("oracle.jdbc.driver.OracleDriver");
    	conn=DriverManager.getConnection("jdbc:oracle:thin:@test-EFESTO-LNX:1521:ORCL","GDM","GDM");
    	conn.setAutoCommit(false);
    	IDbOperationSQL dbOp = SessioneDb.getInstance().createIDbOperationSQL(conn, 0);
    	ResultSet rst = null;
    	
    	String query = "SELECT ID, ti, da, cr FROM (SELECT DISTINCT D.ID_DOCUMENTO ID, D.ID_TIPODOC TI, D.DATA_AGGIORNAMENTO DA, D.CODICE_RICHIESTA CR, NVL (FATTURE.NUMERO_PROTOCOLLO, -1) PROGRESSIVO, TO_DATE (FATTURE.DATA_CREAZIONE, 'DD/MM/YYYY HH24:MI:SS') CREAZIONE FROM DOCUMENTI D, FATTURE_ACQ_VIEW FATTURE, FAT_DETTAGLIO_VIEW DETTAGLIO WHERE FATTURE.ID_DOCUMENTO = D.ID_DOCUMENTO AND FATTURE.IDRIF = DETTAGLIO.IDRIF(+) AND ( (GDM.fe_utility_SO4. isUoCompetente ('GDM', DETTAGLIO.UO_COMPETENTE, 'ENTE') = 'OK' AND GDM.fe_utility. f_valore_parametri_sep ('SMISTAMENTI#FATTPA') = 'S') OR GDM.fe_utility. f_valore_parametri_sep ('SMISTAMENTI#FATTPA') = 'N') AND FATTURE.ENTE = 'ENTE' AND DETTAGLIO.TIPO_DOCUMENTO = DECODE ('-', '-', DETTAGLIO.TIPO_DOCUMENTO, '-') AND (FATTURE.STATO = DECODE ('-', '-', FATTURE.STATO, '-') OR DETTAGLIO.STATO = DECODE ('-', '-', DETTAGLIO.STATO, '-')) AND NVL (FATTURE.ANNO_PROTOCOLLO, -1) = DECODE ('T', 'T', NVL (FATTURE.ANNO_PROTOCOLLO, -1), 'S', NVL (FATTURE.ANNO_PROTOCOLLO, -2), 'N', -1) AND d.stato_documento NOT IN ('CA', 'RE') UNION SELECT TO_NUMBER (NULL), TO_NUMBER (NULL), TO_DATE (NULL), TO_CHAR (NULL), TO_NUMBER (NULL), TO_DATE (NULL) FROM DUAL ORDER BY CREAZIONE DESC) a, DUAL WHERE gdm_competenza. gdm_verifica ('DOCUMENTI', a.ID, 'L', 'GDM', f_trasla_ruolo ('GDM', 'GDMWEB', 'GDMWEB'), TO_CHAR (SYSDATE, 'dd/mm/yyyy')) || dummy = '1X'";

    	dbOp.setStatement(query);
    	dbOp.execute();
    	rst = dbOp.getRstSet();
    	
    	Multirecord	 mr = new Multirecord();
    	HashMapSet hms = new HashMapSet();
    	while (rst.next()) {
    		hms.add(rst.getString(2), rst.getString(1));
    	}
    	
//		FileOutputStream outputStream = new FileOutputStream(new File("c:/temp/elab.xls"));
		InputStream is = mr.esportaDatiExcellToInputStream(dbOp, hms, false, "FE_UTILITY.XLS_FATTURE_ACQ");
    	LetturaScritturaFileFS lsf = new LetturaScritturaFileFS("c:/temp/elab.xls");
    	lsf.scriviFile(is);
    	dbOp.close();
 		conn.close();
    } catch(Exception e){
      e.printStackTrace();
    }
  }
  
  public static void main(String[] args) {
		Connection cn = null;
		IDbOperationSQL dbOp = null;
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			cn = DriverManager
					.getConnection(
							"jdbc:oracle:thin:@test-efesto-lnx:1521:orcl",
							"GDM", "GDM");
			cn.setAutoCommit(false);
			Multirecord mr = new Multirecord();
			mr.creaCollegamenti(cn, "598342");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			cn.close();
		} catch (Exception e) {}
  }

  public String creaCollegamenti(Connection cn, String iddoc ) throws Exception {
	  IDbOperationSQL dbOp = null;
	  IDbOperationSQL dbOp2 = null;
	  ResultSet rst = null;
	  ResultSet rst2 = null;
	  String descrizione = "";
	  String istruzione = "";
	  String iddoc_rif = "";
	  String area = "";
	  String cm = "";
	  String cr = "";
	  String url = "";
	  String divclass = "";
	  String retval = "";
	  
	  
	  String query="SELECT NVL(M.OGGETTO,m.codice_modello) DESCRIZIONE, d.id_documento ID_DOCUMENTO, B.ISTRUZIONE ISTRUZIONE, ";
      		  query+="m.codice_modello CM, m.area AREA, D.CODICE_RICHIESTA CR ";
		      query+="FROM DOCUMENTI D,";
			  query+="TIPI_DOCUMENTO TD,";
		      query+="RIFERIMENTI R,";
		      query+="MODELLI M,";
		      query+="BLOCCHI B ";
		      query+="WHERE R.ID_DOCUMENTO = :IDDOC AND ";
		      query+="D.ID_DOCUMENTO = R.ID_DOCUMENTO_RIF AND ";
		      query+="D.ID_TIPODOC = TD.ID_TIPODOC AND ";
		      query+="D.ID_TIPODOC = M.ID_TIPODOC AND ";
		      query+="D.AREA = M.AREA AND ";
		      query+="M.BLOCCO_JDMS=B.BLOCCO AND ";
		      query+="M.area=B.area ";
		      query+="order by 1,2 ";

	  try {
		  dbOp = SessioneDb.getInstance().createIDbOperationSQL(cn, 0);
		  dbOp.setStatement(query);
		  dbOp.setParameter(":IDDOC", iddoc);
		  dbOp.execute();
		  rst = dbOp.getRstSet();
		  while (rst.next()) {
			  descrizione = rst.getString("DESCRIZIONE");
			  iddoc_rif = rst.getString("ID_DOCUMENTO");
			  area = rst.getString("AREA");
			  cm = rst.getString("CM");
			  cr = rst.getString("CR");
			  try {
				  BufferedInputStream bis = dbOp.readClob("ISTRUZIONE");
				  StringBuffer sb = new StringBuffer();
				  int ic;
				  while ((ic =  bis.read()) != -1) {
					  sb.append((char)ic);
				  }
				  istruzione = sb.toString();
			  } catch (Exception eis) {
				  istruzione = "";
			  }
			  if (divclass.equals("AFCDataTD")) {
				  divclass= "AFCAltDataTD";
			  } else {
				  divclass= "AFCDataTD";
			  }
			  url = "<a class='AFCDataLink' href='DocumentoView.do?idDoc="+iddoc_rif+"&rw=W&cm="+cm+"&area="+area+"&cr="+cr+"&idCartProveninez=&idQueryProveninez=-1&Provenienza=&stato=BO&MVPG=ServletModulisticaDocumento'>";
			  if (istruzione.length() > 0) {
				  dbOp2 = SessioneDb.getInstance().createIDbOperationSQL(dbOp.getConn(), 0);
				  dbOp2.setStatement(istruzione);
				  dbOp2.setParameter(":ID_DOCUMENTO", iddoc_rif);
				  dbOp2.setParameter(":id_documento", iddoc_rif);
				  dbOp2.execute();
				  rst2 = dbOp2.getRstSet();
				  if (rst2.next()) {
					  retval += "<div class='"+divclass+"'>"+url+rst2.getString(1)+"</a></div>\n"; 
				  }
				  dbOp2.close();
			  } else {
				  retval += "<div class='"+divclass+"'>"+url+descrizione+" ("+iddoc_rif+")</a></div>\n"; 
			  }
		  }
		  dbOp.close();
		  retval = "<div style='display:none'><div id='_doc_collegati'>"+retval+"</div></div>";
		  System.out.println(retval);
		  
	  } catch (Exception e) {
		  try {
			  dbOp.close();
		  } catch (Exception e2) {}
		  try {
			  dbOp2.close();
		  } catch (Exception e2) {}
		  e.printStackTrace();
		  throw new Exception("Erorre in creaCollegamenti: "+e.getMessage());
	  }
	  return retval;
	  
  }
  
    private static Element leggiElemento(Element e, String tagName)
    {
        Element elemento = null, eFound = null;;
        for(Iterator iterator = e.elementIterator(); iterator != null && iterator.hasNext() && eFound == null;)
        {
            elemento = (Element)iterator.next();
            if(elemento != null && elemento.getName().equals(tagName)) {
               eFound = elemento;
            } else {
                eFound = leggiElemento(elemento, tagName);
                if ( eFound != null) {
                  return eFound;
                }
            }
        }

        return eFound;
    }

    private static String leggiValore(Document xmlDocument, String tagName)
    {
        String valore = null;
        if(xmlDocument == null)
            System.out.println("xml document null");
        Element root = xmlDocument.getRootElement();
        for(Iterator iterator = root.elementIterator(); iterator != null && iterator.hasNext() && valore == null;)
        {
            Element elemento = (Element)iterator.next();
            if(elemento != null && elemento.getName().equals(tagName))
                valore = elemento.getText();
            else
                valore = leggiValore(elemento, tagName);
        }

        return valore;
    }

    private static String leggiValore(Element e, String tagName)
    {
        String valore = null;
        for(Iterator iterator = e.elementIterator(); iterator != null && iterator.hasNext() && valore == null;)
        {
            Element elemento = (Element)iterator.next();
            if(elemento != null && elemento.getName().equals(tagName))
                valore = elemento.getText();
            else
                valore = leggiValore(elemento, tagName);
        }

        return valore;
    }

    private long stampaTempo(String sMsg, String area, String cm, String blocco, long ptime) {
      if (!debuglog) {
        return 0;
      }
      long adesso = Calendar.getInstance().getTimeInMillis();
      long trascorso = 0;
      if (ptime > 0) {
        trascorso = adesso - ptime;
      }
      if (Parametri.DEBUG.equalsIgnoreCase("1") && ptime > 0) {
        logger.debug("\n"+sMsg+"\n-Area:"+area+" -CodiceModello:"+cm+" -Blocco:"+blocco+"\n-------->Trascorso dall'inizio: "+trascorso+"\n");
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        logger.debug("\n"+sMsg+"\n-Area:"+area+" -CodiceModello:"+cm+" -Blocco:"+blocco+"\n----->TIME: "+adesso+"\n--------Trascorso dall'inizio: "+trascorso+"\n");
      }
      return adesso;
    }

    private void settaCampo(String campo, InformazioniCampo ic) {
      if (ic == null) {
        listaCampi.addLast(campo);
        listaCate.addLast("");
        listaArea.addLast("");
        listaCm.addLast("");
      } else {
        listaCampi.addLast(ic.getNomeDato());
        listaCate.addLast(ic.getCategoriaDato());
        listaArea.addLast(ic.getAreaModelloDato());
        listaCm.addLast(ic.getModelloDato());
      }
    }
    
    private String proteggiValore(String pValore) {
      String oldValue = pValore;
      String newValore = "";
      String nextCh = "";
      int i = 0;
      
      dizionario();
      while (i > -1 && i < oldValue.length()-1) {
        i = oldValue.indexOf(":");
        if (i > -1 && i < oldValue.length()-1) {
          nextCh = oldValue.substring(i+1, i+2);
          if (dizionario.getProperty(nextCh) != null) {
            newValore += oldValue.substring(0, i+1);
          } else {
            newValore += oldValue.substring(0, i) + "::";
          }
          i++;
          oldValue = oldValue.substring(i);
        }
      }
      newValore += oldValue;
      
      return newValore;
    }

    /**
     * 
     */
    private void caricaDatiMonoRecord(Connection cn, int prevRec, String area, String cm, AbstractParser ap, IDbOperationSQL dbOp) throws Exception {
      //Debug Tempo
      long ptime = stampaTempo("Multirecord::caricaDatiMonoRecord - Inizio",area,cm,nomeBlocco,0);
      //Debug Tempo
      String            sRecord = "";
      String            campo = "";
      String            categoria = "";
      String            area_cm_dato = "";
      String            cm_dato = "";
      String            valore = "";
      int               prevRecord = 0;
      ResultSet			rstSql = null;



      listaDati.clear();
      erroreMsg = "";

      if (nonCaricaDati) {
        recordTotali = 0;
        return;
      }
      if (listaCampi.size() < 1) {
        recordTotali = 0;
        return;
      }
      try {   
        IQuery iq = new IQuery();
        iq.initVarEnv(utenteGDM,utenteGDM,cn);
        iq.setAccessProfile(false);
        iq.setInstanceProfile(false);
        if (monoRecord || noCompetenze) {
          iq.escludiControlloCompetenze(true);
        }
        //Debug Tempo
        stampaTempo("Multirecord::caricaDatiMonoRecord - Parziale 1",area,cm,nomeBlocco,ptime);
        //Debug Tempo
        
        //Campi di ritorno
        int l = listaCampi.size();
        int j = 0;
        while (j <l ) {
          campo = (String)listaCampi.get(j);
          if (!campo.equalsIgnoreCase("AREA") &&
              !campo.equalsIgnoreCase("CODICE_MODELLO")&&
              !campo.equalsIgnoreCase("CODICE_RICHIESTA")&&
              !campo.equalsIgnoreCase("GDM_COMP_W") && 
              !campo.equalsIgnoreCase("GDM_COMP_D")) {
            categoria = (String)listaCate.get(j);
            area_cm_dato = (String)listaArea.get(j);
            cm_dato = (String)listaCm.get(j);
            if (categoria.equalsIgnoreCase("")) {
              if (area_cm_dato.equalsIgnoreCase("")) {
                iq.addCampoReturn(campo,area,cm);
              } else {
                iq.addCampoReturn(campo,area_cm_dato,cm_dato);
              }
            } else {
              iq.addCampoReturn(campo,categoria);
            }
          }
          j++;
        }

        //Debug Tempo
        stampaTempo("Multirecord::caricaDatiMonoRecord - Parziale 2",area,cm,nomeBlocco,ptime);
        //Debug Tempo

        iq.setTypeModelReturn(area,cm);
        iq.settaIdDocumentoRicerca(iddocs);

        if (iq.ricerca().booleanValue()) {
          recordTotali = iq.getProfileNumber();
          if (prevRec < recordTotali) {
            prevRecord = prevRec;
          }
          int posRelativa = 0;
          int posizione = 0;
          //Debug Tempo    
          stampaTempo("Multirecord::caricaDatiMonoRecord - Parziale 3",area,cm,nomeBlocco,ptime);
          //Debug Tempo
          ResultSetIQuery  rst = iq.getResultSet();
          while (rst.next() && (prevRecord+posRelativa) <= recordTotali) {
            posizione++;
            if (posizione > prevRecord) {
              j = 0;
              sRecord = "<C>ID_DOCUMENTO</C><V>"+rst.getId()+"</V>";
              while (j < l) {
                campo = (String)listaCampi.get(j);
                if (campo.equalsIgnoreCase("AREA")) {
                  valore = area;
                } else {
                  if (campo.equalsIgnoreCase("CODICE_MODELLO")) {
                    valore = cm;
                  } else {
                    if (campo.equalsIgnoreCase("CODICE_RICHIESTA")) {
                      valore = rst.getCr();
                    } else {
                      if (campo.equalsIgnoreCase("GDM_COMP_W") || campo.equalsIgnoreCase("GDM_COMP_D")) {
                      	valore = "0";
                      } else {
                        categoria = (String)listaCate.get(j);
                        area_cm_dato = (String)listaArea.get(j);
                        cm_dato = (String)listaCm.get(j);
                        if (categoria == null || categoria.equalsIgnoreCase("")) {
                          if (area_cm_dato.equalsIgnoreCase("")) {
                            valore = rst.get(campo,area,cm);
                          } else {
                            valore = rst.get(campo,area_cm_dato,cm_dato);
                            campo += "#"+area_cm_dato+"#"+cm_dato;
                          }
                        } else {
                          valore = rst.get(campo,categoria);
                          campo += "#"+categoria;
                        }
                      }
                    }
                  }
                }
                if (valore == null || valore.equalsIgnoreCase("null")) {
                  valore = "";
                }
                
                //System.out.println("MANNY MUTIREC="+valore);
                valore=stripNonValidXMLCharacters(valore);
                if (multilingua.length() > 0 && valore != null && valore.length() > 0) {
                	dbOp.setStatement("SELECT GDM_UTILITY.F_MULTILINGUA (:VAL,'"+multilingua+"') FROM DUAL");
                	dbOp.setParameter(":VAL", valore);
                	dbOp.execute();
                	rstSql = dbOp.getRstSet();
                	if (rstSql.next()) {
                		valore = rstSql.getString(1);
                	}
                }
                //System.out.println("MANNY MUTIREC DOPO="+valore);
                sRecord += "<C>"+campo+"</C><V>"+valore+"</V>";
                j++;
              }
              listaDati.addLast(sRecord);
              posRelativa++;
            }
          }
        } else {
          recordTotali = 0;
          if (iq.isQueryTimeOut()) {
            logger.error("Blocco: "+nomeBlocco+" ------- Sono andato in timeout");
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
        //Debug Tempo
        stampaTempo("Multirecord::caricaDatiMonoRecord - Fine",area,cm,nomeBlocco,ptime);
        //Debug Tempo
        logger.error("Errore",e);
        throw new Exception ("Attenzione! Errore in Multirecord, Fase di caricamento dati. "+e.toString());
      }
      //Debug Tempo
      stampaTempo("Multirecord::caricaDatiMonoRecord - Fine",area,cm,nomeBlocco,ptime);
      //Debug Tempo
    }

    private String stripNonValidXMLCharacters(String in) {
        StringBuffer out = new StringBuffer(); // Used to hold the output.
        char current; // Used to reference the current character.

        if (in == null || ("".equals(in))) return ""; // vacancy test.
        for (int i = 0; i < in.length(); i++) {
            current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
            if ((current == 0x9) ||
                (current == 0xA) ||
                (current == 0xD) ||
                ((current >= 0x20) && (current <= 0xD7FF)) ||
                ((current >= 0xE000) && (current <= 0xFFFD)) ||
                ((current >= 0x10000) && (current <= 0x10FFFF)))
                out.append(current);
        }
        return out.toString();

    } 
    
    private void setAutoSize(WritableSheet sheet, int colNum, int rowNum) {
    	int maxlength = 0;
        for (int i = 0; i < colNum; i++) {
        	maxlength = 0;
            for (int j = 0; j < rowNum; j++) {
                if (maxlength<sheet.getCell(i, j).getContents().length()) {
                	maxlength = sheet.getCell(i, j).getContents().length();
                }
            }

            /* If not found, skip the column. */
            if (maxlength == 0) 
                continue;

            maxlength = (int)(maxlength*1.5);
            if (maxlength > 255)
            	maxlength = 255;

            CellView cv = sheet.getColumnView(i);
            sheet.setColumnView(i, maxlength);
        }
    }

        
    /**
     * 
     */
    private void caricaDatiExcell(Connection cn, WritableWorkbook wkb, String area, String cm, int foglio, String tabella, String funzione) throws Exception {
    	caricaDatiExcell(cn,wkb,area,cm,foglio,tabella,funzione,null,"");
    }
    
    /**
     * 
     */
    private void caricaDatiExcell(Connection cn, WritableWorkbook wkb, String area, String cm, int foglio, String tabella, String funzione, IDbOperationSQL dbOp, String utente) throws Exception {
      //Debug Tempo
      long ptime = stampaTempo("Multirecord::caricaDatiExcell - Inizio",area,cm,nomeBlocco,0);
      //Debug Tempo
      String            campo = "";
//      String            categoria = "";
//      String            area_cm_dato = "";
//      String            cm_dato = "";
      String            valore = "";
      Label lbl; 
      int               nRecord = 1;
      Profilo			pdoc = null;
      ResultSet			rst = null;
      String			query = "";
      


//      listaDati.clear();
      erroreMsg = "";

      if (nonCaricaDati) {
        recordTotali = 0;
        return;
      }
      if (funzione.length() > 0) {
    	  try {
  	        recordTotali = iddocs.size();
  	        if (recordTotali < 1) {
  	        	return;
  	        }
  			int oldrettype = dbOp.getTypeRetFunction();
  			dbOp.setTypeRetFunction(OracleTypes.CURSOR);
  			dbOp.setCallFunc(funzione+"('"+iddocs.get(0)+"','"+utente+"')");
  			dbOp.execute();
	    	rst = (ResultSet)dbOp.getCallSql().getObject(1);
	    	int numcol = rst.getMetaData().getColumnCount();
	    	
	        WritableSheet sheet = wkb.createSheet(cm, foglio);
	        WritableFont cellFont = new WritableFont(WritableFont.ARIAL);
	        cellFont.setBoldStyle(WritableFont.BOLD); 
	        WritableCellFormat cellFormat = new WritableCellFormat(cellFont);
			cellFormat.setBackground(Colour.GREY_25_PERCENT);
	        if (numcol < 1) {
	        	dbOp.setTypeRetFunction(oldrettype);
	        	recordTotali = 0;
	        	return;
	        } else {
	        	for (int j = 0; j < numcol; j++) {
	        		valore = rst.getMetaData().getColumnName(j+1);
	        		sheet.setColumnView(j,(int)(valore.length()*1.5));
	        		lbl = new Label(j,0,valore,cellFormat);
	        		sheet.addCell(lbl);
	        	}
	        	while (rst.next()) {
		        	for (int j = 0; j < numcol; j++) {
		        		valore = (rst.getString(j+1)==null)?"":rst.getString(j+1);
		        		lbl = new Label(j,1,valore);
		        		sheet.addCell(lbl);
		        	}
	        	}
	        }
        	rst.close();
	        for (int i=1; i < recordTotali; i++) {
	        	dbOp.setCallFunc(funzione+"('"+iddocs.get(i)+"','"+utente+"')");
	        	dbOp.execute();
		    	rst = (ResultSet)dbOp.getCallSql().getObject(1);
	        	while (rst.next()) {
		        	for (int j = 0; j < numcol; j++) {
		        		lbl = new Label(j,i+1,(rst.getString(j+1)==null)?"":rst.getString(j+1));
		        		sheet.addCell(lbl);
		        	}
	        	}
	        	rst.close();
	        }
        	dbOp.setTypeRetFunction(oldrettype);
        	setAutoSize(sheet, numcol, recordTotali);
    	  } catch (Exception e) {
        	  e.printStackTrace();
        	  
        	  //Debug Tempo
        	  stampaTempo("Multirecord::caricaDatiExcell - Fine",area,cm,nomeBlocco,ptime);
        	  //Debug Tempo
        	  logger.error("Errore",e);
        	  throw new Exception ("Attenzione! Errore in Multirecord, Fase di caricamento dati. "+e.toString());
    	  }
    	  return;
      }
      
      WritableSheet sheet = wkb.createSheet(cm, foglio);
      WritableFont cellFont = new WritableFont(WritableFont.ARIAL);
      cellFont.setBoldStyle(WritableFont.BOLD); 
      WritableCellFormat cellFormat = new WritableCellFormat(cellFont);
      cellFormat.setBackground(Colour.GREY_25_PERCENT);
      if (listaCampi.size() < 1) {
        recordTotali = 0;
        return;
      } else {
      	for (int j = 0; j < listaCampi.size(); j++) {
      		lbl = new Label(j,0,listaCampi.get(j),cellFormat);
      		sheet.addCell(lbl);
      	}
      }
      try {
      	
        //Debug Tempo
        stampaTempo("Multirecord::caricaDatiExcell - Parziale 2",area,cm,nomeBlocco,ptime);
        //Debug Tempo

        int l = listaCampi.size();
        int j = 0;

        recordTotali = iddocs.size();

        //Debug Tempo    
        stampaTempo("Multirecord::caricaDatiExcell - Parziale 3",area,cm,nomeBlocco,ptime);
        //Debug Tempo
        for (int i=0; i < recordTotali; i++) {
        	if (tabella.length() > 0 && dbOp != null) {
        		j = 0;
        		if (query.length() == 0) {
	        		query = "SELECT ";
	        		while (j < l) {
		        		campo = (String)listaCampi.get(j);
		        		if (!(campo.equalsIgnoreCase("AREA") || campo.equalsIgnoreCase("CODICE_MODELLO") || campo.equalsIgnoreCase("GDM_COMP_W") || campo.equalsIgnoreCase("GDM_COMP_D"))) {
			        		if (campo.equalsIgnoreCase("CODICE_RICHIESTA")) {
			        			query += "D.CODICE_RICHIESTA CODICE_RICHIESTA,";
			        		} else {
			        			query += "T."+campo+" "+campo+",";
			        		}
		        		}
		        		j++;
		        	}
	        		query = query.substring(0, query.length()-1);
	        		query += " FROM "+tabella+" T, DOCUMENTI D WHERE D.ID_DOCUMENTO = T.ID_DOCUMENTO AND T.ID_DOCUMENTO = :IDDOC";
        		}
        		dbOp.setStatement(query);
        		dbOp.setParameter(":IDDOC", iddocs.get(i));
        		dbOp.execute();
        		rst = dbOp.getRstSet();
        		if (rst.next()) {
		        	j = 0;
		        	while (j < l) {
		        		campo = (String)listaCampi.get(j);
		        		if (campo.equalsIgnoreCase("AREA")) {
		        			valore = area;
		        		} else {
		        			if (campo.equalsIgnoreCase("CODICE_MODELLO")) {
		        				valore = cm;
		        			} else {
		        				if (campo.equalsIgnoreCase("CODICE_RICHIESTA")) {
		        					valore = rst.getString("CODICE_RICHIESTA");
		        				} else {
		        					if (campo.equalsIgnoreCase("GDM_COMP_W") || campo.equalsIgnoreCase("GDM_COMP_D")) {
		        						valore = "0";
		        					} else {
		        						valore = rst.getString(campo);
		        					}
		        				}
		        			}
		        		}
		        		if (valore == null || valore.equalsIgnoreCase("null")) {
		        			valore = "";
		        		}
		        		lbl = new Label(j,nRecord,valore);
		        		sheet.addCell(lbl);
			            j++;
		        	}
		        	nRecord++;
        		}
        	} else {
	        	pdoc = new Profilo(iddocs.get(i), "GDM", "", "", cn);
	        	pdoc.escludiControlloCompetenze(true);
	        	if (pdoc.accedi().booleanValue()) {
		        	j = 0;
		        	while (j < l) {
		        		campo = (String)listaCampi.get(j);
		        		if (campo.equalsIgnoreCase("AREA")) {
		        			valore = area;
		        		} else {
		        			if (campo.equalsIgnoreCase("CODICE_MODELLO")) {
		        				valore = cm;
		        			} else {
		        				if (campo.equalsIgnoreCase("CODICE_RICHIESTA")) {
		        					valore = pdoc.getCodiceRichiesta();
		        				} else {
		        					if (campo.equalsIgnoreCase("GDM_COMP_W") || campo.equalsIgnoreCase("GDM_COMP_D")) {
		        						valore = "0";
		        					} else {
		        						valore = pdoc.getCampo(campo);
		        					}
		        				}
		        			}
		        		}
		        		if (valore == null || valore.equalsIgnoreCase("null")) {
		        			valore = "";
		        		}
		        		lbl = new Label(j,nRecord,valore);
		        		sheet.addCell(lbl);
			            j++;
		        	}
		        	nRecord++;
	        	} else {
	        		logger.error("Blocco: "+nomeBlocco+" ------- "+pdoc.getError());
	        	}
        	}
        }
        setAutoSize(sheet, listaCampi.size(), recordTotali);
      } catch (Exception e) {
    	  e.printStackTrace();
    	  //Debug Tempo
    	  stampaTempo("Multirecord::caricaDatiExcell - Fine",area,cm,nomeBlocco,ptime);
    	  //Debug Tempo
    	  logger.error("Errore",e);
    	  throw new Exception ("Attenzione! Errore in Multirecord, Fase di caricamento dati. "+e.toString());
      }
      //Debug Tempo
      stampaTempo("Multirecord::caricaDatiExcell - Fine",area,cm,nomeBlocco,ptime);
      //Debug Tempo
    }

    private void caricaDatiExcellOld(Connection cn, WritableWorkbook wkb, String area, String cm, int foglio, String tabella) throws Exception {
        //Debug Tempo
        long ptime = stampaTempo("Multirecord::caricaDatiExcell - Inizio",area,cm,nomeBlocco,0);
        //Debug Tempo
        String            campo = "";
        String            categoria = "";
        String            area_cm_dato = "";
        String            cm_dato = "";
        String            valore = "";
        Label lbl; 
        int               nRecord = 1;



//        listaDati.clear();
        erroreMsg = "";

        if (nonCaricaDati) {
          recordTotali = 0;
          return;
        }
        WritableSheet sheet = wkb.createSheet(cm, foglio);
        if (listaCampi.size() < 1) {
          recordTotali = 0;
          return;
        } else {
        	for (int j = 0; j < listaCampi.size(); j++) {
        		lbl = new Label(j,0,listaCampi.get(j));
        		sheet.addCell(lbl);
        	}
        }
        try {
        	
          IQuery iq = new IQuery();
          iq.initVarEnv(utenteGDM,utenteGDM,cn);
          iq.setAccessProfile(false);
          iq.setInstanceProfile(false);
          iq.escludiControlloCompetenze(true);
          //Debug Tempo
          stampaTempo("Multirecord::caricaDatiExcell - Parziale 1",area,cm,nomeBlocco,ptime);
          //Debug Tempo
          
          //Campi di ritorno
          int l = listaCampi.size();
          int j = 0;
          while (j <l ) {
            campo = (String)listaCampi.get(j);
            if (!campo.equalsIgnoreCase("AREA") &&
                !campo.equalsIgnoreCase("CODICE_MODELLO")&&
                !campo.equalsIgnoreCase("CODICE_RICHIESTA")&&
                !campo.equalsIgnoreCase("GDM_COMP_W") && 
                !campo.equalsIgnoreCase("GDM_COMP_D")) {
            	categoria = (String)listaCate.get(j);
              area_cm_dato = (String)listaArea.get(j);
              cm_dato = (String)listaCm.get(j);
              if (categoria.equalsIgnoreCase("")) {
                if (area_cm_dato.equalsIgnoreCase("")) {
                  iq.addCampoReturn(campo,area,cm);
                } else {
                  iq.addCampoReturn(campo,area_cm_dato,cm_dato);
                }
              } else {
                iq.addCampoReturn(campo,categoria);
              }
            }
            j++;
          }

          //Debug Tempo
          stampaTempo("Multirecord::caricaDatiExcell - Parziale 2",area,cm,nomeBlocco,ptime);
          //Debug Tempo

          iq.setTypeModelReturn(area,cm);
          iq.settaIdDocumentoRicerca(iddocs);

          
          if (iq.ricerca().booleanValue()) {
            recordTotali = iq.getProfileNumber();
  	    	System.out.println("----------- Record trovati "+recordTotali+" -------------------");

            //Debug Tempo    
            stampaTempo("Multirecord::caricaDatiExcell - Parziale 3",area,cm,nomeBlocco,ptime);
            //Debug Tempo
            ResultSetIQuery  rst = iq.getResultSet();
            while (rst.next()) {
      	    	System.out.println("----------- Record "+nRecord+" -------------------");
                j = 0;
                while (j < l) {
                  campo = (String)listaCampi.get(j);
                  if (campo.equalsIgnoreCase("AREA")) {
                    valore = area;
                  } else {
                    if (campo.equalsIgnoreCase("CODICE_MODELLO")) {
                      valore = cm;
                    } else {
                      if (campo.equalsIgnoreCase("CODICE_RICHIESTA")) {
                        valore = rst.getCr();
                      } else {
                        if (campo.equalsIgnoreCase("GDM_COMP_W") || campo.equalsIgnoreCase("GDM_COMP_D")) {
                        	valore = "0";
                        } else {
                          categoria = (String)listaCate.get(j);
                          area_cm_dato = (String)listaArea.get(j);
                          cm_dato = (String)listaCm.get(j);
                          if (categoria == null || categoria.equalsIgnoreCase("")) {
                            if (area_cm_dato.equalsIgnoreCase("")) {
                              valore = rst.get(campo,area,cm);
                            } else {
                              valore = rst.get(campo,area_cm_dato,cm_dato);
                              campo += "#"+area_cm_dato+"#"+cm_dato;
                            }
                          } else {
                            valore = rst.get(campo,categoria);
                            campo += "#"+categoria;
                          }
                        }
                      }
                    }
                  }
                  if (valore == null || valore.equalsIgnoreCase("null")) {
                    valore = "";
                  }
                  lbl = new Label(j,nRecord,valore);
              		sheet.addCell(lbl);
                  j++;
                }
                nRecord++;

            }
          } else {
            recordTotali = 0;
            if (iq.isQueryTimeOut()) {
              logger.error("Blocco: "+nomeBlocco+" ------- Sono andato in timeout");
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
          //Debug Tempo
          stampaTempo("Multirecord::caricaDatiExcell - Fine",area,cm,nomeBlocco,ptime);
          //Debug Tempo
          logger.error("Errore",e);
          throw new Exception ("Attenzione! Errore in Multirecord, Fase di caricamento dati. "+e.toString());
        }
        //Debug Tempo
        stampaTempo("Multirecord::caricaDatiExcell - Fine",area,cm,nomeBlocco,ptime);
        //Debug Tempo
      }

    public String creaHtmlMonoRecord(IDbOperationSQL dbOp,
        HttpServletRequest request, 
        AbstractParser ap, 
        boolean navigatore,
        String pArea,
        String codMod) throws Exception {
      return creaHtmlMonoRecord(dbOp,request,ap,navigatore,pArea,codMod,false);
    }
    
    public String creaHtmlMonoRecord(IDbOperationSQL dbOp,
                                     HttpServletRequest request, 
                                     AbstractParser ap, 
                                     boolean navigatore,
                                     String pArea,
                                     String codMod,
                                     boolean stampa) throws Exception {
      int rigaIniziale = 1;
      String primRec = "1";
      Element root, elp;

      rigaIniziale = Integer.parseInt(primRec);
      int stepNav = iddocs.size();
      int i = 0;
      int j = 0;
      String rigo = "";
      String retval = "";
      String pBlocco = "";

      caricaDatiMonoRecord(dbOp.getConn(), rigaIniziale -1, pArea, codMod, ap, dbOp);
      if (domini && ld == null) {
        settaDominioCampo(dbOp);
        ld = new ListaDomini(dbOp.getConn());
      }
      //Estraggo i blocchi nested
      String area = leggiValore("AREA",1);
      String cm = leggiValore("CODICE_MODELLO",1);
      String nomeTabellaPadre = nomeTabella(dbOp, area, cm);
      if (nomeTabellaPadre == null) {
      	nomeTabellaPadre = "";
      }
      if (!nomeTabellaPadre.equalsIgnoreCase("")) {
	      lblocchiN = new ListaBlocchiNested();
	      String stringaDaElaborare = corpoHtml;
	      i = stringaDaElaborare.indexOf(BEGIN_BLOCCO);
	      j = stringaDaElaborare.indexOf(END_BLOCCO,i) + END_BLOCCO.length();
	      int inizioBlocco = stringaDaElaborare.indexOf(BEGIN_NAME_BLOCCO);
	      int fineBlocco = stringaDaElaborare.indexOf(END_BLOCCO,inizioBlocco);
	      while(inizioBlocco > -1) {
	        try {
	        	String selectDual = "select null, null";
	        	String selectDatiE = "select id_documento, id_figlio";
	        	String selectDati = "select p.id_documento id_documento, f.id_documento id_figlio";
	        	String fromDati = 	" FROM DOCUMENTI D, "+nomeTabellaPadre+" P, ";
	        	String whereDati = 	" WHERE D.ID_DOCUMENTO = F.ID_DOCUMENTO AND "+
//											        	"GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI',F.ID_DOCUMENTO,'L','"+utenteGDM+"','GDM',TO_CHAR(SYSDATE,'dd/mm/yyyy')) = 1 AND "+
											        	"D.STATO_DOCUMENTO NOT IN ('CA','RE','PB') ";
//	        	String orderDati =	"ORDER BY P.ID_DOCUMENTO ASC";
	        	String orderDati =	"ORDER BY ID_DOCUMENTO ASC";
              String whereExt;
              if (monoRecord)
                whereExt="";
              else
                whereExt=" WHERE GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI',A.ID_DOCUMENTO,'L','"+utenteGDM+"','GDM',TO_CHAR(SYSDATE,'dd/mm/yyyy')) = 1 ";
	          pBlocco = stringaDaElaborare.substring(inizioBlocco,fineBlocco);
	          BloccoNested bnm = new BloccoNested(pRequest, area, cm, pBlocco, listaDati, 1, dbOp.getConn(),monoRecord);
	          String nomeTabellaFiglio = nomeTabella(dbOp, bnm.area, bnm.codMod);
	          if (nomeTabellaFiglio == null) {
	          	nomeTabellaFiglio = "";
	          }
	          if (!nomeTabellaFiglio.equalsIgnoreCase("")) {
	          	fromDati += nomeTabellaFiglio+" F ";
		          for (int lc=0; lc < bnm.getListaCampi().size(); lc++) {
		          	String nomeCampo = bnm.getListaCampi().get(lc);
		          	boolean trovato = false;
		          	if (nomeCampo.equalsIgnoreCase("AREA")) {
		          		selectDual += ", null";
		  	        	selectDatiE += ", AREA";
		          		selectDati += ", '"+bnm.area+"' AREA";
		          		trovato = true;
		          	}
		          	if (nomeCampo.equalsIgnoreCase("CODICE_MODELLO")) {
		          			selectDual += ", null";
			  	        	selectDatiE += ", CODICE_MODELLO";
			          		selectDati += ", '"+bnm.codMod+"' CODICE_MODELLO";
			          		trovato = true;
		          	}
		          	if (nomeCampo.equalsIgnoreCase("CODICE_RICHIESTA")) {
		          		selectDual += ", null";
		  	        	selectDatiE += ", CODICE_RICHIESTA";
		          		selectDati += ", d.codice_richiesta CODICE_RICHIESTA";
		          		trovato = true;
		          	}
		          	if (nomeCampo.indexOf("GDM_COMP_W") > -1) {
		          		selectDual += ", null";
//		          		selectDati += ", GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI',F.ID_DOCUMENTO,'U','"+utenteGDM+"','GDM',TO_CHAR(SYSDATE,'dd/mm/yyyy')) GDM_COMP_W";
		  	        	selectDatiE += ", GDM_COMP_W";
		          		selectDati += ", 0 GDM_COMP_W";
		          		trovato = true;
		          	}
		          	if (nomeCampo.indexOf("GDM_COMP_D") > -1) {
		          		selectDual += ", null";
//		          		selectDati += ", GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI',F.ID_DOCUMENTO,'D','"+utenteGDM+"','GDM',TO_CHAR(SYSDATE,'dd/mm/yyyy')) GDM_COMP_D";
		  	        	selectDatiE += ", GDM_COMP_D";
		          		selectDati += ", 0 GDM_COMP_D";
		          		trovato = true;
		          	}
		          	if (!trovato) {
		          		selectDual += ", null";
		  	        	selectDatiE += ", "+nomeCampo;
		          		selectDati += ", F."+nomeCampo;
		          	}
		          }

		          //Condizioni di legame
		          String lega = bnm.mr.getLegame();
		          lega = lega.replaceAll(" and "," AND ");
		          lega = lega.replaceAll(" like "," LIKE ");
		          lega = lega.replaceAll(" between "," BETWEEN ");
		          String strval= "";
		          int posAnd = 0;
		          int lung = lega.length();
		          while (lung > 0) {
		            posAnd = lega.indexOf(" AND ");
		            if (posAnd > 0) {
		             strval = lega.substring(0,posAnd);
		             lega = lega.substring(posAnd+5);
		            } else {
		              strval = lega;
		              lega = "";
		            }
		            lung = lega.length();
		            String campoF = "";
		            String valoreF = "";
		            String ope = "";
		            int posDiv    = strval.indexOf("<>");
		            int posMinUg  = strval.indexOf("<=");
		            int posMagUg  = strval.indexOf(">=");
		            int posUg     = strval.indexOf("=");
		            int posLike   = strval.indexOf("LIKE");
		            if (posDiv > 0) {
		              posUg = -1;
		              campoF = strval.substring(0,posDiv);
		              campoF = campoF.replaceAll("'","");
		              campoF = campoF.replaceAll(" ","");
		              valoreF = strval.substring(posDiv+2);
		              valoreF = valoreF.replaceAll("'","");
		              valoreF = valoreF.replaceAll(" ","");
		              ope = "<>";
		            }
		            if (posMinUg > 0) {
		              posUg = -1;
		              campoF = strval.substring(0,posMinUg);
		              campoF = campoF.replaceAll("'","");
		              campoF = campoF.replaceAll(" ","");
		              valoreF = strval.substring(posMinUg+2);
		              valoreF = valoreF.replaceAll("'","");
		              valoreF = valoreF.replaceAll(" ","");
		              ope = "<=";
		            }
		            if (posMagUg > 0) {
		              posUg = -1;
		              campoF = strval.substring(0,posMagUg);
		              campoF = campoF.replaceAll("'","");
		              campoF = campoF.replaceAll(" ","");
		              valoreF = strval.substring(posMagUg+2);
		              valoreF = valoreF.replaceAll("'","");
		              valoreF = valoreF.replaceAll(" ","");
		              ope = ">=";
		            }
		            if (posUg > 0) {
		              campoF = strval.substring(0,posUg);
		              campoF = campoF.replaceAll("'","");
		              campoF = campoF.replaceAll(" ","");
		              valoreF = strval.substring(posUg+1);
		              valoreF = valoreF.replaceAll("'","");
		              valoreF = valoreF.replaceAll(" ","");
		              ope = "=";
		            }
		            if (posLike > 0) {
		              campoF = strval.substring(0,posLike);
		              campoF = campoF.replaceAll("'","");
		              campoF = campoF.replaceAll(" ","");
		              valoreF = strval.substring(posLike+4);
		              valoreF = valoreF.replaceAll("'","");
		              valoreF = valoreF.replaceAll(" ","");
		              ope = "LIKE";
		            }
//		            selectDati += ", F."+campoF;
		            if (campoF.indexOf("GDM_COMPETENZA") < 0 && !campoF.equalsIgnoreCase("")) {
		            	if (!ope.equalsIgnoreCase("LIKE")) {
		            		if (valoreF.indexOf(":") > -1) {
		            			whereDati += " AND F."+campoF+" "+ope+valoreF.replaceAll(":", " P.");
		            		} else {
		            			whereDati += " AND F."+campoF+" "+ope+" '"+valoreF+"' ";
		            		}
		            	} else {
		            		valoreF = valoreF.replaceAll(":"," P.");
		            		if (valoreF.indexOf("%") == 0) {
		            			if (valoreF.indexOf("%", 1) > 0) {
		            				valoreF = valoreF.replaceAll("%", "");
		            				valoreF = "'%'||"+valoreF+"||'%'";
		            			} else {
		            				valoreF = valoreF.replaceAll("%", "");
		            				valoreF = "'%'||"+valoreF;
		            			}
		            		} else {
	            				valoreF = valoreF.replaceAll("%", "");
	            				valoreF = valoreF+"||'%'";
		            		}
			            	whereDati += " AND F."+campoF+" "+ope+valoreF.replaceAll(":", "");
		            	}
		            }

		          }
            	whereDati += " AND p.id_documento in (";
            	String virgola = "";
            	for (int c=0; c < iddocs.size(); c++) {
            		whereDati += virgola+iddocs.get(c);
            		virgola = ",";
            	}
            	whereDati += ") "; 
            	
            	if (!bnm.mr.ordinamento.equalsIgnoreCase("")) {
//            		orderDati += ", F."+bnm.mr.ordinamento.replaceAll(",",", F.");
            		orderDati += ", bnm.mr.ordinamento";
            	}
            	//String query = selectDati+fromDati+whereDati+orderDati;
            	String query = selectDatiE+" FROM ("+selectDati+fromDati+whereDati+" UNION "+selectDual+" FROM DUAL) A "+whereExt+orderDati;
            	ResultSet rst = null;
            //	System.out.println("NUOVA QUERY MONORECORD--->"+query);
		          dbOp.setStatement(query);
		          dbOp.execute();
		          rst = dbOp.getRstSet();
		          DatiBlocchiNested dbn = new DatiBlocchiNested();
		          String iddoc = "";
		          String sRecord = "";
		          String nomeCampo = "";
		          String valoreCampo = "";
		          while (rst.next()) {
		          	sRecord = "";
		          	iddoc = rst.getString("ID_DOCUMENTO");
			          for (int lc=0; lc < bnm.getListaCampi().size(); lc++) {
			          	nomeCampo = bnm.getListaCampi().get(lc);
			          	if (nomeCampo.indexOf("GDM_COMP_W") > 0) {
			          		nomeCampo = "GDM_COMP_W";
			          	}
			          	if (nomeCampo.indexOf("GDM_COMP_D") > 0) {
			          		nomeCampo = "GDM_COMP_D";
			          	}
			          	valoreCampo = rst.getString(nomeCampo);
			          	if (valoreCampo == null) {
			          		valoreCampo = "";
			          	}
			          	sRecord += "<C>"+nomeCampo+"</C><V>"+valoreCampo+"</V>";
			          }
		          	dbn.aggiungiRecord(iddoc, sRecord);
		          }
		          bnm.setDati(dbn);
	          	lblocchiN.aggiungiBlocco(bnm);
	        	}
	
	        } catch (Exception e) {
	          e.printStackTrace();
	        }
		      stringaDaElaborare = stringaDaElaborare.substring(j);
		      i = stringaDaElaborare.indexOf(BEGIN_BLOCCO);
		      j = stringaDaElaborare.indexOf(END_BLOCCO,i) + END_BLOCCO.length();
		      inizioBlocco = stringaDaElaborare.indexOf(BEGIN_NAME_BLOCCO);
		      fineBlocco = stringaDaElaborare.indexOf(END_BLOCCO,inizioBlocco);
      	}
      }

      
      i = 0;
      root = DocumentHelper.createElement(nomeBlocco);
      Document dDoc = DocumentHelper.createDocument();
      dDoc.setRootElement(root);
      while (i < stepNav) {
        elp = DocumentHelper.createElement("ID"+leggiValore("ID_DOCUMENTO", i));
        if (isTable) {
          if (stampa) {
            rigo = headerHtml;
          } else {
            rigo = "<table width='100%'>";
          }
        } else {
          rigo = "";
        }
        rigo += creaRiga(dbOp,i,ap,stampa)+"\n";
        if (isTable) { 
          if (stampa) {
            rigo += footerHtml;
          } else {
            rigo += "</table>";
          }
          
        }
        elp.setText(rigo);
        root.add(elp);
        i++;
      }
      
      retval = dDoc.asXML();

//      //Eleaboro i blocci nested
//      String stringaDaElaborare, pBlocco,area,cm;
//      stringaDaElaborare = retval;
//      area = "";
//      cm = "";
//      if (listaDati.size() > 0) {
//        area = leggiValore("AREA",1);
//        cm = leggiValore("CODICE_MODELLO",1);
//      }
//      retval = "";
//      i = stringaDaElaborare.indexOf(BEGIN_BLOCCO);
//      j = stringaDaElaborare.indexOf(END_BLOCCO,i) + END_BLOCCO.length();
//      int inizioBlocco = stringaDaElaborare.indexOf(BEGIN_NAME_BLOCCO);
//      int fineBlocco = stringaDaElaborare.indexOf(END_BLOCCO,inizioBlocco);
//      while(inizioBlocco > -1) {
//        retval += stringaDaElaborare.substring(0,i);
//        try {
//          //Creo la stringa per il campo
//          pBlocco = stringaDaElaborare.substring(inizioBlocco,fineBlocco);
//  //        InformazioniBlocco infoBlocco = new InformazioniBlocco(pBlocco);
//          BloccoNested bn = new BloccoNested(pRequest, area, cm, pBlocco, listaDati, 1, dbOp.getConn(),monoRecord);
//          retval += bn.getValue();
//        } catch (Exception e) {
//          e.printStackTrace();
//          retval += "<!-- Errore "+e.toString()+" -->\n";
//        }
//        stringaDaElaborare = stringaDaElaborare.substring(j);
//        i = stringaDaElaborare.indexOf(BEGIN_BLOCCO);
//        j = stringaDaElaborare.indexOf(END_BLOCCO,i) + END_BLOCCO.length();
//        inizioBlocco = stringaDaElaborare.indexOf(BEGIN_NAME_BLOCCO);
//        fineBlocco = stringaDaElaborare.indexOf(END_BLOCCO,inizioBlocco);
//      }
//      retval += stringaDaElaborare;
//      
//      //Fine elaborazione blocchi nested
//      retval = dDoc.asXML();
      return retval;
    }
    
	public String creaHtmlCollRecord(IDbOperationSQL dbOp,String pArea, String codMod) throws Exception {
		int rigaIniziale = 1;
		String primRec = "1";
		Element root, elp;

		rigaIniziale = Integer.parseInt(primRec);
		int stepNav = iddocs.size();
		int i = 0;
		int j = 0;
		String rigo = "";
		String retval = "";
		String pBlocco = "";

		caricaDatiMonoRecord(dbOp.getConn(), rigaIniziale - 1, pArea, codMod, null, dbOp);
		if (domini && ld == null) {
			settaDominioCampo(dbOp);
			ld = new ListaDomini(dbOp.getConn());
		}
		// Estraggo i blocchi nested
		String area = leggiValore("AREA", 1);
		String cm = leggiValore("CODICE_MODELLO", 1);
		String nomeTabellaPadre = nomeTabella(dbOp, area, cm);
		if (nomeTabellaPadre == null) {
			nomeTabellaPadre = "";
		}
		if (!nomeTabellaPadre.equalsIgnoreCase("")) {
			lblocchiN = new ListaBlocchiNested();
			String stringaDaElaborare = corpoHtml;
			i = stringaDaElaborare.indexOf(BEGIN_BLOCCO);
			j = stringaDaElaborare.indexOf(END_BLOCCO, i) + END_BLOCCO.length();
			int inizioBlocco = stringaDaElaborare.indexOf(BEGIN_NAME_BLOCCO);
			int fineBlocco = stringaDaElaborare.indexOf(END_BLOCCO, inizioBlocco);
			while (inizioBlocco > -1) {
				try {
					String selectDual = "select null, null";
					String selectDatiE = "select id_documento, id_figlio";
					String selectDati = "select p.id_documento id_documento, f.id_documento id_figlio";
					String fromDati = " FROM DOCUMENTI D, " + nomeTabellaPadre + " P, ";
					String whereDati = " WHERE D.ID_DOCUMENTO = F.ID_DOCUMENTO AND " +
					// "GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI',F.ID_DOCUMENTO,'L','"+utenteGDM+"','GDM',TO_CHAR(SYSDATE,'dd/mm/yyyy'))
					// = 1 AND "+
							"D.STATO_DOCUMENTO NOT IN ('CA','RE','PB') ";
					// String orderDati = "ORDER BY P.ID_DOCUMENTO ASC";
					String orderDati = "ORDER BY ID_DOCUMENTO ASC";
					String whereExt = " WHERE GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI',A.ID_DOCUMENTO,'L','" + utenteGDM
							+ "','GDM',TO_CHAR(SYSDATE,'dd/mm/yyyy')) = 1 ";
					pBlocco = stringaDaElaborare.substring(inizioBlocco, fineBlocco);
					BloccoNested bnm = new BloccoNested(pRequest, area, cm, pBlocco, listaDati, 1, dbOp.getConn(),
							monoRecord);
					String nomeTabellaFiglio = nomeTabella(dbOp, bnm.area, bnm.codMod);
					if (nomeTabellaFiglio == null) {
						nomeTabellaFiglio = "";
					}
					if (!nomeTabellaFiglio.equalsIgnoreCase("")) {
						fromDati += nomeTabellaFiglio + " F ";
						for (int lc = 0; lc < bnm.getListaCampi().size(); lc++) {
							String nomeCampo = bnm.getListaCampi().get(lc);
							boolean trovato = false;
							if (nomeCampo.equalsIgnoreCase("AREA")) {
								selectDual += ", null";
								selectDatiE += ", AREA";
								selectDati += ", '" + bnm.area + "' AREA";
								trovato = true;
							}
							if (nomeCampo.equalsIgnoreCase("CODICE_MODELLO")) {
								selectDual += ", null";
								selectDatiE += ", CODICE_MODELLO";
								selectDati += ", '" + bnm.codMod + "' CODICE_MODELLO";
								trovato = true;
							}
							if (nomeCampo.equalsIgnoreCase("CODICE_RICHIESTA")) {
								selectDual += ", null";
								selectDatiE += ", CODICE_RICHIESTA";
								selectDati += ", d.codice_richiesta CODICE_RICHIESTA";
								trovato = true;
							}
							if (nomeCampo.indexOf("GDM_COMP_W") > -1) {
								selectDual += ", null";
								// selectDati += ",
								// GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI',F.ID_DOCUMENTO,'U','"+utenteGDM+"','GDM',TO_CHAR(SYSDATE,'dd/mm/yyyy'))
								// GDM_COMP_W";
								selectDatiE += ", GDM_COMP_W";
								selectDati += ", 0 GDM_COMP_W";
								trovato = true;
							}
							if (nomeCampo.indexOf("GDM_COMP_D") > -1) {
								selectDual += ", null";
								// selectDati += ",
								// GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI',F.ID_DOCUMENTO,'D','"+utenteGDM+"','GDM',TO_CHAR(SYSDATE,'dd/mm/yyyy'))
								// GDM_COMP_D";
								selectDatiE += ", GDM_COMP_D";
								selectDati += ", 0 GDM_COMP_D";
								trovato = true;
							}
							if (!trovato) {
								selectDual += ", null";
								selectDatiE += ", " + nomeCampo;
								selectDati += ", F." + nomeCampo;
							}
						}

						// Condizioni di legame
						String lega = bnm.mr.getLegame();
						lega = lega.replaceAll(" and ", " AND ");
						lega = lega.replaceAll(" like ", " LIKE ");
						lega = lega.replaceAll(" between ", " BETWEEN ");
						String strval = "";
						int posAnd = 0;
						int lung = lega.length();
						while (lung > 0) {
							posAnd = lega.indexOf(" AND ");
							if (posAnd > 0) {
								strval = lega.substring(0, posAnd);
								lega = lega.substring(posAnd + 5);
							} else {
								strval = lega;
								lega = "";
							}
							lung = lega.length();
							String campoF = "";
							String valoreF = "";
							String ope = "";
							int posDiv = strval.indexOf("<>");
							int posMinUg = strval.indexOf("<=");
							int posMagUg = strval.indexOf(">=");
							int posUg = strval.indexOf("=");
							int posLike = strval.indexOf("LIKE");
							if (posDiv > 0) {
								posUg = -1;
								campoF = strval.substring(0, posDiv);
								campoF = campoF.replaceAll("'", "");
								campoF = campoF.replaceAll(" ", "");
								valoreF = strval.substring(posDiv + 2);
								valoreF = valoreF.replaceAll("'", "");
								valoreF = valoreF.replaceAll(" ", "");
								ope = "<>";
							}
							if (posMinUg > 0) {
								posUg = -1;
								campoF = strval.substring(0, posMinUg);
								campoF = campoF.replaceAll("'", "");
								campoF = campoF.replaceAll(" ", "");
								valoreF = strval.substring(posMinUg + 2);
								valoreF = valoreF.replaceAll("'", "");
								valoreF = valoreF.replaceAll(" ", "");
								ope = "<=";
							}
							if (posMagUg > 0) {
								posUg = -1;
								campoF = strval.substring(0, posMagUg);
								campoF = campoF.replaceAll("'", "");
								campoF = campoF.replaceAll(" ", "");
								valoreF = strval.substring(posMagUg + 2);
								valoreF = valoreF.replaceAll("'", "");
								valoreF = valoreF.replaceAll(" ", "");
								ope = ">=";
							}
							if (posUg > 0) {
								campoF = strval.substring(0, posUg);
								campoF = campoF.replaceAll("'", "");
								campoF = campoF.replaceAll(" ", "");
								valoreF = strval.substring(posUg + 1);
								valoreF = valoreF.replaceAll("'", "");
								valoreF = valoreF.replaceAll(" ", "");
								ope = "=";
							}
							if (posLike > 0) {
								campoF = strval.substring(0, posLike);
								campoF = campoF.replaceAll("'", "");
								campoF = campoF.replaceAll(" ", "");
								valoreF = strval.substring(posLike + 4);
								valoreF = valoreF.replaceAll("'", "");
								valoreF = valoreF.replaceAll(" ", "");
								ope = "LIKE";
							}
							// selectDati += ", F."+campoF;
							if (campoF.indexOf("GDM_COMPETENZA") < 0 && !campoF.equalsIgnoreCase("")) {
								if (!ope.equalsIgnoreCase("LIKE")) {
									if (valoreF.indexOf(":") > -1) {
										whereDati += " AND F." + campoF + " " + ope + valoreF.replaceAll(":", " P.");
									} else {
										whereDati += " AND F." + campoF + " " + ope + " '" + valoreF + "' ";
									}
								} else {
									valoreF = valoreF.replaceAll(":", " P.");
									if (valoreF.indexOf("%") == 0) {
										if (valoreF.indexOf("%", 1) > 0) {
											valoreF = valoreF.replaceAll("%", "");
											valoreF = "'%'||" + valoreF + "||'%'";
										} else {
											valoreF = valoreF.replaceAll("%", "");
											valoreF = "'%'||" + valoreF;
										}
									} else {
										valoreF = valoreF.replaceAll("%", "");
										valoreF = valoreF + "||'%'";
									}
									whereDati += " AND F." + campoF + " " + ope + valoreF.replaceAll(":", "");
								}
							}

						}
						whereDati += " AND p.id_documento in (";
						String virgola = "";
						for (int c = 0; c < iddocs.size(); c++) {
							whereDati += virgola + iddocs.get(c);
							virgola = ",";
						}
						whereDati += ") ";

						if (!bnm.mr.ordinamento.equalsIgnoreCase("")) {
							// orderDati += ",
							// F."+bnm.mr.ordinamento.replaceAll(",",", F.");
							orderDati += ", bnm.mr.ordinamento";
						}
						// String query =
						// selectDati+fromDati+whereDati+orderDati;
						String query = selectDatiE + " FROM (" + selectDati + fromDati + whereDati + " UNION "
								+ selectDual + " FROM DUAL) A " + whereExt + orderDati;
						ResultSet rst = null;
						// System.out.println("NUOVA QUERY
						// MONORECORD--->"+query);
						dbOp.setStatement(query);
						dbOp.execute();
						rst = dbOp.getRstSet();
						DatiBlocchiNested dbn = new DatiBlocchiNested();
						String iddoc = "";
						String sRecord = "";
						String nomeCampo = "";
						String valoreCampo = "";
						while (rst.next()) {
							sRecord = "";
							iddoc = rst.getString("ID_DOCUMENTO");
							for (int lc = 0; lc < bnm.getListaCampi().size(); lc++) {
								nomeCampo = bnm.getListaCampi().get(lc);
								if (nomeCampo.indexOf("GDM_COMP_W") > 0) {
									nomeCampo = "GDM_COMP_W";
								}
								if (nomeCampo.indexOf("GDM_COMP_D") > 0) {
									nomeCampo = "GDM_COMP_D";
								}
								valoreCampo = rst.getString(nomeCampo);
								if (valoreCampo == null) {
									valoreCampo = "";
								}
								sRecord += "<C>" + nomeCampo + "</C><V>" + valoreCampo + "</V>";
							}
							dbn.aggiungiRecord(iddoc, sRecord);
						}
						bnm.setDati(dbn);
						lblocchiN.aggiungiBlocco(bnm);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				stringaDaElaborare = stringaDaElaborare.substring(j);
				i = stringaDaElaborare.indexOf(BEGIN_BLOCCO);
				j = stringaDaElaborare.indexOf(END_BLOCCO, i) + END_BLOCCO.length();
				inizioBlocco = stringaDaElaborare.indexOf(BEGIN_NAME_BLOCCO);
				fineBlocco = stringaDaElaborare.indexOf(END_BLOCCO, inizioBlocco);
			}
		}

		retval = creaRiga(dbOp, i, null, false);

		return retval;
	}



    public void setIdDocs(Vector<String> newIddocs) {
      iddocs = newIddocs;
    }

    private void settaDominioCampo(IDbOperationSQL dbOp) throws Exception {
      int size1, size2;
      String  area_dato,
              sDom, 
              query;
      ResultSet rst = null;
     
      String area = leggiValore("AREA",0);
      String cm = leggiValore("CODICE_MODELLO",0);
      size1 = listaArea.size();
      size2 = listaCampi.size();
      
      for (int i =0; i < size1 && i < size2; i++) {
        area_dato = "";
        sDom = "";
        if (domini ) {
          query = "SELECT AREA_DATO, DOMINIO FROM DATI_MODELLO DM, DATI D WHERE "+
                  "DM.AREA = :AREA AND DM.CODICE_MODELLO = :CM"+
                  " AND DM.DATO = '"+listaCampi.get(i)+"'"+
                  " AND DM.AREA_DATO = D.AREA AND DM.DATO = D.DATO ";
          dbOp.setStatement(query);
          dbOp.setParameter(":AREA", area);
          dbOp.setParameter(":CM", cm);
          dbOp.execute();
          rst = dbOp.getRstSet();
          if (rst.next()) {
            area_dato = rst.getString(1);
            sDom =  rst.getString(2);
          }
          if (area_dato == null) {
            area_dato = "";
          }
          if (sDom == null) {
            sDom = "";
          }
        }
        listaArDom.addLast(area_dato);
        listaDom.addLast(sDom);
      }
    }
    
    private String getAreaDominio(String campo) {
      String retval = "";
      int i = 0;
      while (i< listaCampi.size() && !campo.equals(listaCampi.get(i))) {
        i++;
      }
      if (campo.equals(listaCampi.get(i))) {
        retval = listaArDom.get(i);
      }
      return retval;
    }
    
    private String getDominio(String campo) {
      String retval = "";
      int i = 0;
      while (i< listaCampi.size() && !campo.equals(listaCampi.get(i))) {
        i++;
      }
      if (campo.equals(listaCampi.get(i))) {
        retval = listaDom.get(i);
      }
      return retval;
    }


    public String getCodice_modello() {
      return codice_modello;
    }


    public void setCodice_modello(String codice_modello) {
      this.codice_modello = codice_modello;
    }
    
    private String nomeTabella(IDbOperationSQL dbOp, String area, String cm) {
    	String retval = "";
    	ResultSet rst = null;
    	try {
    		dbOp.setStatement("SELECT F_NOME_TABELLA(:AREA, :CM) NOME FROM DUAL");
        dbOp.setParameter(":AREA", area);
        dbOp.setParameter(":CM", cm);
    		dbOp.execute();
    		rst = dbOp.getRstSet();
    		if (rst.next()) {
    			retval = rst.getString(1);
    		}
    	} catch (Exception e) {
    		
    	}
    	
    	return retval;
    }
    
    private void dizionario() {
    	if (dizionario != null) {
    		return;
    	}
    	dizionario = new Properties();
      byte b2[] = new byte[1];
      b2[0] = 13;
      String ch = new String(b2);

      dizionario.setProperty(" ", "OK");
      dizionario.setProperty(",", "OK");
      dizionario.setProperty(")", "OK");
      dizionario.setProperty("(", "OK");
      dizionario.setProperty("*", "OK");
      dizionario.setProperty("+", "OK");
      dizionario.setProperty("/", "OK");
      dizionario.setProperty("-", "OK");
      dizionario.setProperty("|", "OK");
      dizionario.setProperty("&", "OK");
      dizionario.setProperty("=", "OK");
      dizionario.setProperty("<", "OK");
      dizionario.setProperty(">", "OK");
      dizionario.setProperty("'", "OK");
      dizionario.setProperty("\"", "OK");
      dizionario.setProperty("\n", "OK");
      dizionario.setProperty("\t", "OK");
      dizionario.setProperty("\\", "OK");
      dizionario.setProperty(":", "OK");
      dizionario.setProperty("?", "OK");
      dizionario.setProperty(".", "OK");
      dizionario.setProperty(ch, "OK");
      dizionario.setProperty("!", "OK");
      dizionario.setProperty("%", "OK");
      dizionario.setProperty("[", "OK");
      dizionario.setProperty("]", "OK");
      dizionario.setProperty("{", "OK");
      dizionario.setProperty("}", "OK");
      return;
    }

    public String codificaCaratteriSpeciali(String testo){
	  	 String retval = testo;
		 
	  	 try { 	
	  		
	  		if(testo!=null && !testo.equals("")) {	  		 
			  	retval = retval.replaceAll("€", "&euro;");
			  	retval = retval.replaceAll("Á", "&Aacute;");
			  	retval = retval.replaceAll("À", "&Agrave;");
			  	retval = retval.replaceAll("É", "&Eacute;");
			  	retval = retval.replaceAll("È", "&Egrave;");
			  	retval = retval.replaceAll("Ì", "&Igrave;");
			  	retval = retval.replaceAll("Í", "&Iacute;");
			  	retval = retval.replaceAll("Ó", "&Oacute;");
			  	retval = retval.replaceAll("Ò", "&Ograve;");
			  	retval = retval.replaceAll("Ù", "&Ugrave;");
			  	retval = retval.replaceAll("Ú", "&Uacute;");
			  	retval = retval.replaceAll("à", "&agrave;");
			  	retval = retval.replaceAll("è", "&egrave;");
			  	retval = retval.replaceAll("é", "&eacute;");
			  	retval = retval.replaceAll("ò", "&ograve;");
			  	retval = retval.replaceAll("ì", "&igrave;");
			  	retval = retval.replaceAll("ù", "&ugrave;");
			  	retval = retval.replaceAll("°", "&deg;");
			  	
			  	retval = retval.replaceAll("[^\\p{Print}]", " ");
	  		}
		 } catch (Exception e) {
	  		e.printStackTrace();
	  	 }  	
	  	 return retval;
 }

}

