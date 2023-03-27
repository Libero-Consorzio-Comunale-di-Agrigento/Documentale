package it.finmatica.modulistica;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import java.sql.*;

import it.finmatica.modulistica.mappaactionstandard.MappaAction;
import it.finmatica.modulistica.modulisticapack.*;
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.modulistica.personalizzazioni.Personalizzazioni;
import it.finmatica.modulistica.userquery.UserQuery;
import it.finmatica.jfc.authentication.Cryptable;
import it.finmatica.jfc.crypto.TripleDESEncrypter;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.jfc.utility.Base64;
import it.finmatica.dmServer.competenze.*;
import it.finmatica.dmServer.util.*;
import it.finmatica.dmServer.modulistica.AccessoModulistica;
import it.finmatica.dmServer.management.*;
import it.finmatica.dmServer.*;
import it.finmatica.dmServer.util.Global;
import it.finmatica.dmServer.FirmaUnimatica.FirmaUnimatica;
import it.finmatica.dmServer.Environment;
import it.finmatica.modulistica.allegati.CompetenzeAllegati;
import it.finmatica.modulistica.inoltro.*;
import it.finmatica.modutils.hashing.Hashing;
import it.finmatica.dmServer.check.CheckDocumento;
import it.finmatica.instantads.WrapParser;
import it.finmatica.dmServer.jdms.*;
import java.net.*;

import org.dom4j.*;
import org.owasp.encoder.Encode;
import org.apache.log4j.*;

import xmlpack.InfoConnessione;
import it.finmatica.jsuitesync.SyncSuite;


public class Modulistica {
  public  final static int MAXLEN_PARAMETRI = 1000000;
  private String      iniPath = null;
  private String      inifile = null;
  private boolean     isAjax = false;
  private boolean     erroreControlli = false;
  private boolean     forceRedirect = false;
  private Environment vu;
  private String      id_tipodoc = null;
  private String      gdc_link = null;
  private String      corpoHtml ="";
  private String      ruolo = null;
  private String      lettura = null;
  private String      pdo= null;
  private String      pidOp = null;
  private String      id_session = null;
  private String      jwf_id = null;
  private String      jwf_back = null;
  private String      myPathTemp = null;
  private String      listaAlle = "";
  private String      modPrec = "";
  private boolean     isW3c = false;
  private String      stato_doc = null;
  private String      idCartnew = "";
  private String      pulsPrima = "";
  private String      pulsDopo = "";
  private String      iterPuls = "";
  private String      iterAction = "";
  private String      gdc_refresh = "<input type='hidden' id='gdc_refresh' value='0' />";
  private String      aggData = "";
  private String      timeFirstOpen = "";
  private String      modSuccessivo = "";
  private String      sFunInput = "";
  private String      sIdDoc = "";
  private String      sTipoUso = "";
  private String			selectQuery = "";
  private boolean     revisione = false;
  private boolean			returnSelect = false;
  private ModelloHTMLIn       mdg = null;
  private ArrayList<Modello>   modelli = null;
  private  static Logger      logger = Logger.getLogger(Modulistica.class);
  private  boolean debuglog = logger.isDebugEnabled();
  private boolean			disabilitaReload = false;
  private int livellocheck = 0;
  private int newlivellocheck = 0;
  private String			cm_padre = "";
  private String			erroreAction = "";
  private CompetenzeAllegati compAll = null;


  /**
   * Crea e inizializza un nuovo oggetto Modulistica
   * @param sPath Path reale della Servlet
   */
  public Modulistica(String sPath) {
    init(sPath);
  }

  private void init(String sPath)  {
    try {
      String separa = File.separator;

      iniPath = sPath;
      inifile = sPath + "config" + separa + "gd4dm.properties";
      File f = new File(inifile);
      if (!f.exists()) {
        inifile = sPath + ".." + separa + "jgdm" + separa + "config" + separa + "gd4dm.properties";
      }
      if (Parametri.USER.length() == 0) {
        Parametri.leggiParametriStandard(inifile);
        SessioneDb.getInstance().addAlias(Parametri.ALIAS, Parametri.SPORTELLO_DRIVER);
      }
    } catch(Exception e) {
      loggerError("ServletModulistica::init() - Attenzione! si � verificato un errore: "+e.toString(),e);
    }
  }

  private boolean esisteDatoInRequest(HttpServletRequest  request, String dato, String ar, String cm, String cr) {
  	boolean retval = true;

  	retval = request.getParameterMap().containsKey(dato);

  	return retval;
  }

  private boolean esistonoDatiInRequest(HttpServletRequest  request, ResultSet rs, String ar, String cm, String cr, String livello) throws SQLException {
    boolean dati_mancanti = false;
    String campi="";
    LinkedList<String> reqKey = new LinkedList<String>();
    LinkedList<String> reqVal = new LinkedList<String>();
    while (rs.next()) {
      String dato      = rs.getString(1);
      String tipoCampo = rs.getString(3);
      if (tipoCampo.charAt(0) == 'B' || tipoCampo.charAt(0) == 'R') {
      	reqKey.add(dato);
      	reqVal.add("----------> Dato di tipo check o radio, non verificabile <----------------");
      } else {
        reqKey.add(dato);
        if(!esisteDatoInRequest(request, dato, ar, cm, cr)) {
        	campi += dato+"<br/>";
        	dati_mancanti = true;
        	reqVal.add("----------> Errore dato non presente <----------------");
        } else {
        	String v = request.getParameter(dato);
        	if (v == null) {
        		v = "";
        	}
        	reqVal.add(v);
        }
      }
    }
    if (dati_mancanti) {
    	if (!livello.equals("0")) {
      	corpoHtml += "<span class='AFCErrorDataTD'>Attenzione problemi durante il salvataggio<br/>";
      	if (livello.equals("1")) {
      		corpoHtml += "I seguenti campi non sono stati aggiornati:<br/>";
      	} else {
      		corpoHtml += "Documento non salvato.<br/>Impossible acquisire i valori per i seguenti campi:<br/>";
      	}
    		corpoHtml += campi;
    		corpoHtml += "</span>";
    	}
    	String testa_err= "ServletModulistica:dati_mancanti:: ";
    	logger.error("-------------------------------- Inizio segnalazione errore dati mancanti in request --------------------------------");
    	logger.error(testa_err+"area="+ar+" - cm="+cm+" - cr="+cr+" - livello errore="+livello);
    	for (int i=0; i<reqKey.size(); i++) {
    		logger.error(testa_err+reqKey.get(i)+"="+reqVal.get(i));
    	}
    	logger.error("-------------------------------- Fine segnalazione errore dati mancanti in request --------------------------------");
    }

    return dati_mancanti;
  }

  /**
   *
   */
  private boolean aggiornaValori(HttpServletRequest  request, String iddoc) {
    boolean         result = true;
    IDbOperationSQL  dbOp = null;
    ResultSet       rs = null;
    String          query;
    String          dato = null;
    String          calcolato = null;
    String          tipoCampo = null;
    String          tipoAccesso = null;
    String          valore = null;
    String					codError = "";
    String					msgError = "";
    String          ar,
                    cm,
                    cr,
                    rw;

    ar   = request.getParameter("area");
    cm   = request.getParameter("cm");
    cr   = request.getParameter("cr");
    rw   = request.getParameter("rw");

    //Debug Tempo
    long ptime = stampaTempo("Modulistica::aggiornaValori - Inizio",ar,cm,cr,0);
    //Debug Tempo

    if (cr == null) {
      cr = (String)request.getSession().getAttribute("key");
    }
    try {
      dbOp = vu.getDbOp();
    	String livello_errore = "0";
      query = "SELECT VALORE "+
              "FROM   PARAMETRI "+
              "WHERE  CODICE = :CODICE AND "+
              "TIPO_MODELLO = :TIPO" ;
      dbOp.setStatement(query);
      dbOp.setParameter(":CODICE", "LIVELLO_ERRORE_CAMPI_VUOTI");
      dbOp.setParameter(":TIPO", "@STANDARD");
      dbOp.execute();
      rs = dbOp.getRstSet();
      if (rs.next())
      	livello_errore = rs.getString("VALORE");

      if (livello_errore == null) {
      	livello_errore = "0";
      }
      AggiornaDocumento ad = new AggiornaDocumento(iddoc, vu);
      ad.settaCodiceRichiesta(cr);
      ad.setUltAggiornamento(mdg.getUltimoAgg());

      Dominio dp = null;
      ListaDomini ld = (ListaDomini)request.getSession().getAttribute("listaDomini");

      query = "SELECT   DATO, CAMPO_CALCOLATO, TIPO_CAMPO, TIPO_ACCESSO "+
                "FROM DATI_MODELLO "+
              "WHERE    AREA = :AREA AND "+
                       "CODICE_MODELLO = :CODICE_MODELLO AND "+
                       "NVL(IN_USO,'Y') = 'Y' AND "+
                       "(DATO NOT LIKE '$%' "+
                       "OR DATO IN ('$BARCODE1','$BARCODE2','$BARCODE3','$MASTER')) "+
              "ORDER BY DATO ASC";

      dbOp.setStatement(query);
      dbOp.setParameter(":AREA",ar);
      dbOp.setParameter(":CODICE_MODELLO",cm);
      dbOp.execute();
      rs = dbOp.getRstSet();
      boolean dati_mancanti = esistonoDatiInRequest(request, rs, ar, cm, cr, livello_errore);
      if (dati_mancanti) {
        if (livello_errore.equals("0")) {
        	corpoHtml = "";
        } else {
          if (!livello_errore.equals("1")) {
          	result= false;
          	return result;
          }
        }
      }

      dbOp.execute();
      rs = dbOp.getRstSet();
      List<DatoModello> listaDatiModello = new ArrayList<DatoModello>();
      while (rs.next()) {
        dato      = rs.getString(1);
        calcolato = rs.getString(2);
        tipoCampo = rs.getString(3);
        tipoAccesso = rs.getString(4);

        listaDatiModello.add(new DatoModello(dato,calcolato,tipoCampo,tipoAccesso));
      }

      for(int indexDatoModello=0;indexDatoModello<listaDatiModello.size();indexDatoModello++) {
        dato      = listaDatiModello.get(indexDatoModello).getDato();
        calcolato = listaDatiModello.get(indexDatoModello).getCalcolato();
        tipoCampo = listaDatiModello.get(indexDatoModello).getTipoCampo();
        tipoAccesso =listaDatiModello.get(indexDatoModello).getTipoAccesso();

        valore = null;

        if (tipoAccesso.equalsIgnoreCase("L")) {
          String myVal = null;
          if (ld != null) {
            int numDom = ld.getNumDomini();
            int i = 0;
            while (i < numDom && valore == null) {
              dp = (Dominio)ld.getDominio(i);
              if (dp.isDominioFormulaModello()) {
                myVal = dp.getValore(dato);
              }
              i++;
            }
          }
          if (rw.equalsIgnoreCase("W") && calcolato.equalsIgnoreCase("S")) {
            valore = myVal;
          }
          if (calcolato.equalsIgnoreCase("C")) { //Esiste
            valore = myVal;
          }
          if (calcolato.equalsIgnoreCase("V")) {
            valore = myVal;
          }
        }

        int inizio = 0xFF;
        if ( valore == null) {
          if (tipoCampo.charAt(0) != 'B') {            // new!
            // E' un campo legato ad un dominio, ma non � di tipo CHECKBOX
            valore = request.getParameter(dato);
            if (valore != null) {
            	String[] valori = request.getParameterValues(dato);
            	int numValori = valori.length;
            	if (numValori > 1) {
            		valore = valori[numValori-1];
            	}
              valore = encode(valore,tipoCampo);
            }
          } else {
            // E' un campo legato ad un CAMPO di tipo CHECKBOX
            valore = "";
            for (int j=0; j<1000; j++) {  // 1000 valori al massimo.
              String s = request.getParameter(dato+"_"+Integer.toString(j));
              if (s != null){
                // Attacco sempre un separatore in fondo, in tal modo quando dovr�
                // recuperare un valore lo richiamer� sempre utilizzando il separatore
                valore = valore + s + Parametri.SEPARAVALORI;
              }
            }
            if (valore.length() == 0) {
              valore = null;
            }
          }
        }
        if (valore == null) {
          valore = "";
        }

        if (valore.length() == 0) {
          if (calcolato.equalsIgnoreCase("S") && rw.equalsIgnoreCase("W")) {
            if (ld != null) {
              int numDom = ld.getNumDomini();
              int i = 0;
              valore = null;
              while (i < numDom && valore == null) {
                dp = (Dominio)ld.getDominio(i);
                if (dp.isDominioFormulaModello()) {
                  valore = dp.getValore(dato);
                }
                i++;
              }
            }
          }
        }

        if (valore == null) {
          valore = "";
        }

        if (!calcolato.equalsIgnoreCase("V")) {
        	if (valore.length() == 0 && !esisteDatoInRequest(request, dato, ar, cm, cr) && tipoCampo.charAt(0) != 'B' && tipoCampo.charAt(0) != 'R') {
        		//Non faccio niente
        		logger.error("Campo "+dato+"  non salvato perchè non presente in request" );
        	} else {
        		ad.aggiornaDati(dato,valore);
        	}
        }
      }

      String errore = "";
      if (pulsPrima.length() != 0) {
        errore = eseguiControllo(request,pulsPrima,true,null,ad,cr,iddoc);
        if (errore.length() != 0) {
        	erroreAction = errore;
//          corpoHtml += "<span class='AFCErrorDataTD'>"+errore+"</span>";
          result = false;
          return result;
        }
        istanziaIter(request);
      }

      ad.salvaAllegatiTemp(true);
      try {
      	ad.salvaDocumentoBozza();
      } catch (Exception e2) {
      	codError = ad.getCodeError();
      	if (codError != null) {
      		msgError = ad.getDescrCodeError();
      		throw new Exception (msgError);
      	} else {
      		throw e2;
      	}
      }

      settaDataAggiornamentoModello(request,ad.getUltAggiornamento());

//      inserisciAllegati(request,cr,cm);
      if (pulsDopo.length() != 0) {
        errore = eseguiControllo(request,pulsDopo,true,null,null,cr,iddoc);
        if (errore.length() != 0) {
//          corpoHtml += "<span class='AFCErrorDataTD'>"+errore+"</span>";
        	erroreAction = errore;
          result = false;
        }
        istanziaIter(request);
      }
      if (iterAction.length() != 0) {
      	istanziaIter(request, iterAction);
      }
      mdg.settaErrMsg(ad.getDescrCodeErrorPostSave());

    } catch (Exception e) {
      loggerError("ServletModulistica::aggiornaValori() - Area: "+ar+" - Modello: "+cm+"- Richiesta: "+cr+" - Errore:  ["+ e.toString()+"]",e);
      result = false;
      corpoHtml += "<span class='AFCErrorDataTD'>"+"Errore in fase di aggiornamento valori!"+"</span>";
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        corpoHtml += e.toString();
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        corpoHtml += e.getStackTrace().toString();
      }
    }
      //Debug Tempo
      stampaTempo("Modulistica::aggiornaValori - Fine",ar,cm,cr,ptime);
      //Debug Tempo

    return result;
  }


  /**
   * calcolaNumeroRichiesta()
   * Calcola un numero univoco di richiesta per l'area in questione
   *
   * @author     Marco Bonforte
   * @parameter  httpSess � la sessione html con cui l'utente � connesso al server
   * @parameter  parea � l'area a cui si riferisce la richiesta appena fatta
   * @return     il numero di richiesta calcolato: deve essere univoco per l'area relativa
   *             in prima istanza torniamo il valore dell'id di sessione combinato con l'area stessa.
   *             NON E' UN METODO TOTALMENTE SICURO MA � MOLTO IMPROBABILE CHE IL SERVER GENERI
   *             UN NUMERO DI SESSIONE DOPPIO PER LA STESSA AREA, INOLTRE ESSENDO QUESTO MECCANISMO
   *             USATO PER QUELLE RICHIESTE CHE SOLITAMENTE HANNO SCADENZA GIORNALIERA PENSO NON SI
   *             VERIFICHERA' MAIL IL CASO DI NUMERO DOPPIO.
   */
   private String calcolaNumeroRichiesta(HttpSession httpSess, String parea) {
    IDbOperationSQL  dbOp = null;
    ResultSet       rs = null;
    String          sNumero = "";
    String          sPref = "";
    String          sSuff = "";
    String          query = "";

    if (sTipoUso.equals("F") || sTipoUso.equals("C")) {
      query = "SELECT CART_SQ.NEXTVAL"+
              "  FROM DUAL";
    } else {
      if (sTipoUso.equals("V") || sTipoUso.equals("Q")) {
        query = "SELECT QRY_SQ.NEXTVAL"+
        "  FROM DUAL";

        sPref = "-";
      } else {
       query = "SELECT RICH_SQ.NEXTVAL"+
               "  FROM DUAL";
       sPref = parea+"-";
       sSuff = "-A";
      }
    }

    try {
      dbOp = vu.getDbOp();
      dbOp.setStatement(query);
      dbOp.execute();
      rs = dbOp.getRstSet();
      rs.next();
      sNumero = rs.getString(1);

    } catch (Exception e) {

      loggerError("ServletModulistica::calcolaNumeroRichiesta() - Area: "+parea+"- Errore:  ["+ e.toString()+"]",e);
      corpoHtml += "<span class='AFCErrorDataTD'>"+"Errore in creazione Codice Richiesta!"+"</span>";
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        corpoHtml += e.toString();
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        corpoHtml += e.getStackTrace().toString();
      }
    }

     return (sPref+sNumero+sSuff);
   }

  /**
   * calcolaScadenza()
   * Funzione privata per il calcolo del Timestamp corrispondente alla scadenza di un dato modulo
   *
   * @parameter durata � una stringa che deve rappresentare un intero che sar� il numero di giorni di
   *            validit� da associare ad un dato modulo (tutti i campi di quel modulo avranno una
   *            durata di validit� dipendente dal valore di questo parametro).
   * @author    Adelmo Gentilini
   * @return    Timestamp che mi rappresenta la data di scadenza del modulo; se il valore di ritorno �
   *            un valore null allora il dato ha scadenza infinita (si suppone coi in tutti quei casi
   *            in cui verr� prima o poi chiamata la BuildXML che provveder� a vuotare la tabella da
   *            tuti irecord corrispondenti ai dati richiesti.
   */
   private Timestamp calcolaScadenza(String durata) {
     int durataInt;
     if (durata == null) {
       return null; //** Exit point: se la durata � null torno null
     }

     try {
       durataInt = (new Integer(durata)).intValue();
     } catch (Exception ex) {
       return null; //** Exit point: se il numero non � traducibile ritorno null
     }

     Calendar scad = Calendar.getInstance();   // today
     scad.add(Calendar.DATE, durataInt);
     return (new Timestamp(scad.getTimeInMillis()));
   }

  /**
   *
   */
  private boolean cancellaPreInoltro(String idOp, String ar, String cr) {
    boolean         result = true;
    IDbOperationSQL  dbOp = null;

/*    String query = "DELETE PRE_INOLTRO"+
                   " WHERE AREA = '" + ar + "'"+
                   " AND CODICE_RICHIESTA = '" + cr + "'"+
                   " AND ID_OP = " + idOp;*/

    String query = "DELETE PRE_INOLTRO"+
        " WHERE AREA = :AREA"+
        " AND CODICE_RICHIESTA = :CR"+
        " AND ID_OP = :IDOP";

    try {
      dbOp = vu.getDbOp();
      dbOp.setStatement(query);
      dbOp.setParameter(":AREA",ar);
      dbOp.setParameter(":CR",cr);
      dbOp.setParameter(":IDOP",idOp);
      dbOp.execute();
      dbOp.commit();
    } catch (Exception e) {
      loggerError("ServletModulistica::cancellaPreInoltro() - Area: "+ar+"- Richiesta: "+cr+" - Errore:  ["+ e.toString()+"]",e);
      result = false;
      corpoHtml += "<span class='AFCErrorDataTD'>"+"Attenzione! Problemi in fase di inoltro!"+"</span>";
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        corpoHtml += e.toString();
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        corpoHtml += e.getStackTrace().toString();
      }
    }
    return result;
  }

  /**
   *
   */
  private boolean cancellaRepository(HttpServletRequest  request, String iddoc, boolean binoltro) {
    boolean         result = true;
    String          ar,
                    cm,
                    cr;

    ar   = request.getParameter("area");
    cm   = request.getParameter("cm");
    cr   = request.getParameter("cr");
    if (cr == null) {
      cr = (String)request.getSession().getAttribute("key");
    }

//    String query = "DELETE REPOSITORYTEMP"+
//                   " WHERE AREA = '" + ar + "'"+
//                   " AND CODICE_RICHIESTA = '" + id_session + "'"+
//                   " AND CODICE_MODELLO = '" + cm +"'";

    try {
      if (binoltro) {
        AggiornaDocumento ad = new AggiornaDocumento(iddoc, vu);
        ad.settaCodiceRichiesta(cr);
//        ad.salvaDocumentoCompleto();
//      } else {
        ad.salvaDocumentoBozza();
      }

//      dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
//      dbOp.setStatement(query);
//      dbOp.execute();
//      dbOp.commit();
//      free(dbOp);
    } catch (Exception e) {
      loggerError("ServletModulistica::cancellaRepository() - Area: "+ar+" - Modello: "+cm+" - Errore:  ["+ e.toString()+"]",e);
      result = false;
      corpoHtml += "<span class='AFCErrorDataTD'>"+"Errore in fase di cancellazione della Repositorytemp!"+"</span>";
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        corpoHtml += e.toString();
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        corpoHtml += e.getStackTrace().toString();
      }
    }
    return result;
  }

  /**
   * cercaModello()
   * Ricerca a livello di sessione la presenza del modello richiesto.
   * Se � gi� presente evita di ricaricarlo da database.
   **/
  private Modello cercaModello(HttpSession pHttpSess, String pArea, String pCodiceModello, String pCodiceRichiesta) throws Exception {
    Modello     md = null;
    String      idDoc= "";
    String      sNomeServlet = ""/*,sUltimoAgg = ""*/;
    boolean     trovato = false;
    int         i = 0;

    try {


      //Debug Tempo
      long ptime = stampaTempo("Modulistica::cercaModello - Inizio",pArea,pCodiceModello,pCodiceRichiesta,0);
      //Debug Tempo

      idDoc = (new DocUtil(vu)).getIdDocumento(id_tipodoc,pCodiceRichiesta);
      if (idDoc == null) {
        idDoc = "";
      }
      sNomeServlet = (String) pHttpSess.getAttribute("p_nomeservlet");
      if (pdo.equalsIgnoreCase("HR")) {
        if (sNomeServlet != null) {
          int punto = sNomeServlet.indexOf(".do?");
          if (punto > 0) {
            sNomeServlet = sNomeServlet.substring(0,punto);
          }
        }
      }
      if (modelli == null) {
        //Debug Tempo
        stampaTempo("Modulistica::cercaModello - Fine",pArea,pCodiceModello,pCodiceRichiesta,ptime);
        //Debug Tempo

        return null;
      }

      if (lettura.equalsIgnoreCase("BB")) {
        while ( (!trovato) && (i < modelli.size()) ) {
          md = (Modello)modelli.get(i);
          if ((md.getArea().equals(pArea)) &&
              (md.getCodiceModello().equals(pCodiceModello)) &&
              (md.getCodiceRichiesta().equals(pCodiceRichiesta))) {
            trovato = true;
          }

          i += 1;
        }
      } else {
        while ( (!trovato) && (i < modelli.size()) ) {
          md = (Modello)modelli.get(i);
          if ((md.getArea().equals(pArea)) &&
              (md.getCodiceModello().equals(pCodiceModello)) &&
              (md.getCodiceRichiesta().equals(pCodiceRichiesta)) &&
              (md.getNomeServlet().equals(sNomeServlet)) &&
              (md.getTimeFirstOpen().equals(timeFirstOpen))) {
            trovato = true;
          } else {
            if ((md.getArea().equals(pArea)) &&
                (md.getCodiceModello().equals(pCodiceModello)) &&
                (md.getCodiceRichiesta().equals(pCodiceRichiesta))&&
                (md.getNomeServlet().equals(sNomeServlet)) &&
                (!md.getTimeFirstOpen().equals(timeFirstOpen))) {
              if (lettura.equalsIgnoreCase("Q")){
                trovato = true;
                timeFirstOpen = md.getTimeFirstOpen();
              } else {
                modelli.remove(md);
              }
            } else {
          	  i += 1;
            }
          }
        }
      }
      while (modelli.size() >= Integer.parseInt(Parametri.MAX_MODELLI_MEM)) {
        modelli.remove(0);
      }
      pHttpSess.setAttribute("modelli",modelli);

      //Debug Tempo
      stampaTempo("Modulistica::cercaModello - Fine ricerca modello in memoria",pArea,pCodiceModello,pCodiceRichiesta,ptime);
      //Debug Tempo

      if (!trovato) {
        return null;
      } else {
        return md;
      }

    } catch(Exception e) {
      loggerError("SerletModulistica::cercaModello() - Area: "+pArea+" - Modello: "+pCodiceModello+"- Richiesta: "+pCodiceRichiesta+" - Attenzione! Si � verificato un errore durante la ricerca del modello in memoria: "+e.toString(),e);
      corpoHtml += "<span class='AFCErrorDataTD'>"+"Attenzione! Si � verificato un errore durante la ricerca del modello in memoria!"+"</span>";
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        corpoHtml += e.toString();
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        corpoHtml += e.getStackTrace().toString();
      }
      return null;
    }
  }

  /**
   *
   */
  private void cercaRichiesta(String area, String codice) throws Exception {
    boolean         result = false;
    IDbOperationSQL  dbOp = null;
    ResultSet       rs = null;

    String querySel = "SELECT 1"+
                      "  FROM RICHIESTE "+
                      " WHERE AREA = :AREA"+
                      " AND CODICE_RICHIESTA = :CODICE";

/*    String queryIns = "INSERT INTO richieste (codice_richiesta, area,id_tipo_pratica,  data_inserimento, data_scadenza) "+
                      "VALUES ('"+ codice +"','"+ area +"', null, sysdate, null)";*/

    String queryIns = "INSERT INTO richieste (codice_richiesta, area,id_tipo_pratica,  data_inserimento, data_scadenza) "+
        "VALUES (:CODICE, :AREA, null, sysdate, null)";

    try {
      dbOp = vu.getDbOp();
      dbOp.setStatement(querySel);
      dbOp.setParameter(":AREA", area);
      dbOp.setParameter(":CODICE", codice);
      dbOp.execute();
      rs = dbOp.getRstSet();
      result = rs.next();
      if (!result) {
        dbOp.setStatement(queryIns);
        dbOp.setParameter(":AREA", area);
        dbOp.setParameter(":CODICE", codice);
        dbOp.execute();
        dbOp.commit();
      }

    } catch (Exception e) {
      loggerError("ServletModulistica::cercaRichiesta() - Area: "+area+"- Richiesta: "+codice+" - Errore:  ["+ e.toString()+"]",e);
      String errmsg = "Errore Codice Richiesta non valido!";
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
      	errmsg += "\n"+e.toString();
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
      	errmsg += "\n"+e.getStackTrace().toString();
      }
      corpoHtml = visualizzaMessagio(errmsg);
      throw e;
    }
  }

  /**
   *
   */
  private String controlliModello(HttpServletRequest  request, String ar, String cm, String cr, String iddoc) {
    String result = "";
    String corpo = "";
    IDbOperationSQL  dbOp = null;
    IDbOperationSQL  dbOpC = null;
    IDbOperationSQL  dbOpF = null;
    boolean dbOpAutonoma = false;
    ResultSet       rsC = null, rst = null;;
    String          queryC;
    String          javaStm = "";
    String          sxml = "";
    String          errMsg = "";
    String          controllo = "";
    String          newControllo = "";
    String          campi = "";
    String          us = "";
    SyncSuite 		sync = null;
    Vector<Integer> ite;
    String			syncErr = null;
    String			cmex = "";
//    Integer 			id;

    //Debug Tempo
    long ptime = stampaTempo("Modulistica::controlliModello - Inizio",ar,cm,cr,0);
    //Debug Tempo
    byte[] b2 = new byte[1];
    b2[0] = 13;
    String ch = new String(b2);
    String sFunInput = "<FUNCTION_INPUT><CONNESSIONE GDM><USER>"+
        Parametri.USER+"</USER><PASSWORD>"+Parametri.PASSWD+"</PASSWORD>"+
        "<HOST_STRING>"+Parametri.SPORTELLO_DSN+"</HOST_STRING></CONNESIONE_GDM>";


    us = (String)request.getSession().getAttribute("UtenteGDM");
    if (us == null) {
      us = "";
    }

    //Controllo per campi obbilgatori
    queryC = "SELECT DATO, TIPO_CAMPO "+
             "FROM DATI_MODELLO "+
             "WHERE    AREA = :AREA AND "+
             "CODICE_MODELLO = :CODICE_MODELLO AND "+
             "TIPO_ACCESSO IN ('O','R') AND "+
             "NVL(IN_USO,'Y') = 'Y' "+
             "ORDER BY DATO ASC";
    try {
      dbOpC = vu.getDbOp();
      dbOpC.setStatement(queryC);
      dbOpC.setParameter(":AREA",ar);
      dbOpC.setParameter(":CODICE_MODELLO",cm);
      dbOpC.execute();
      rsC = dbOpC.getRstSet();
      while (rsC.next()) {
        String dato = rsC.getString(1);
        String tipoDato = rsC.getString(2);
        String valore = "";

        if (tipoDato.charAt(0) != 'B') {            // new!
          // E' un campo legato ad un dominio, ma non � di tipo CHECKBOX
          valore = request.getParameter(dato);
        } else {
          // E' un campo legato ad un CAMPO di tipo CHECKBOX
          valore = "";
          for (int j=0; j<1000; j++) {  // 1000 valori al massimo.
            String s = request.getParameter(dato+"_"+Integer.toString(j));
            if (s != null){
              // Attacco sempre un separatore in fondo, in tal modo quando dovr�
              // recuperare un valore lo richiamer� sempre utilizzando il separatore
              valore = valore + s + Parametri.SEPARAVALORI;
            }
          }
        }
        if (valore == null) {
          valore = "";
        }
        if (valore.length() == 0) {
          campi += dato+";1;";
        }
      }
      if (campi.length() != 0) {
        result = "ADS_MSG_ERROR=Campi obbligatori!LIST_FIELDS_ERROR="+campi;
        erroreControlli = true;
        //Debug Tempo
        stampaTempo("Modulistica::controlliModello - Fine",ar,cm,cr,ptime);
        //Debug Tempo
        return result;
      }
    } catch (Exception e) {
      loggerError("ServletModulistica::controlliModello() - Area: "+ar+" - Modello: "+cm+"- Richiesta: "+cr+" - Errore:  ["+ e.toString()+"]",e);
      corpoHtml += "<span class='AFCErrorDataTD'>"+"Errore in fase di controllo dei campi obbligatori!"+"</span>";
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        corpoHtml += e.toString();
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        corpoHtml += e.getStackTrace().toString();
      }
    }

    queryC = "SELECT L.CORPO, L.CAMPI, L.MSG_ERRORE, L.CONTROLLO "+
             "FROM CONTROLLI_MODELLI C, LIBRERIA_CONTROLLI L "+
             "WHERE C.AREA = :AREA AND "+
             "C.CODICE_MODELLO = :CODICE_MODELLO AND "+
             "C.CONTROLLO = L.CONTROLLO AND "+
             "C.AREA = L.AREA AND "+
             "L.TIPO = 'J' " +
             "ORDER BY C.SEQUENZA";

    try {
      dbOpC = vu.getDbOp();
      dbOpC.setStatement(queryC);
      dbOpC.setParameter(":AREA",ar);
      dbOpC.setParameter(":CODICE_MODELLO",cm);
      dbOpC.execute();
      rsC = dbOpC.getRstSet();
      List<ControlloModello> controlloModelloList = new ArrayList<ControlloModello>();
      while (rsC.next()) {
        controlloModelloList.add(new ControlloModello(rsC.getString(4), rsC.getString(1),rsC.getString(2),rsC.getString(3)));
      }

      for(int indexControlloModelloList =0;indexControlloModelloList<controlloModelloList.size();indexControlloModelloList++) {
        if (result.length() != 0) break;

        controllo = controlloModelloList.get(indexControlloModelloList).getControllo();
        //Controllo se c'� una personalizzazione
        Personalizzazioni pers = null;
        pers = (Personalizzazioni)request.getSession().getAttribute("_personalizzazioni_gdm");
        if (pers != null) {
          String persAction = pers.getPersonalizzazione(Personalizzazioni.LIBRERIA_CONTROLLI, ar+"#"+controllo);
          int j = persAction.indexOf("#");
          newControllo = persAction.substring(j+1);
        }
        if (newControllo.equalsIgnoreCase(controllo)) {
          corpo = controlloModelloList.get(indexControlloModelloList).getCorpo();
          corpo = corpo.replaceAll(ch," ");
          corpo = corpo.replaceAll("\n"," ");
          errMsg = controlloModelloList.get(indexControlloModelloList).getErrMsg();
          if (errMsg == null) {
            errMsg = "";
          }
          sxml = sFunInput + "<ERROR>"+errMsg+"</ERROR><DOC>";
          campi = controlloModelloList.get(indexControlloModelloList).getCampi();
          if (campi == null) {
            campi = "";
          }
        } else {
          queryC = "SELECT CORPO, CAMPI, MSG_ERRORE, CONTROLLO "+
                    "FROM LIBRERIA_CONTROLLI "+
                    "WHERE AREA = :AREA AND "+
                    "CONTROLLO = :CONTROLLO ";

          dbOp = vu.getDbOp();
          dbOp.setStatement(queryC);
          dbOp.setParameter(":AREA", ar);
          dbOp.setParameter(":CONTROLLO", newControllo);
          dbOp.execute();
          rst = dbOp.getRstSet();
          if (rst.next()) {
            corpo = rst.getString(1);
            corpo = corpo.replaceAll(ch," ");
            corpo = corpo.replaceAll("\n"," ");
            errMsg = rst.getString(3);
            if (errMsg == null) {
              errMsg = "";
            }
            sxml = sFunInput + "<ERROR>"+errMsg+"</ERROR><DOC>";
            campi = rst.getString(2);
            if (campi == null) {
              campi = "";
            }
          }
        }
        sync = new SyncSuite(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
        if (cm_padre.length() > 0) {
        	cmex = cm_padre;
        } else {
        	cmex = cm;
        }
        ite = (Vector<Integer>)sync.isExecutable(ar,controllo,ar+"@"+cmex+"@"+cr,us);
        if (ite == null) {
        	ite = (Vector<Integer>)sync.isExecutable(ar,controllo,ar+"@"+cm+"@"+cr,us);
        }
        //ite = sync.isExecutable(ar,controllo,ar+"@"+cm+"@"+cr,us);
        syncErr = sync.getLastError();
        if (syncErr != null) {
          closeSync(sync);
          erroreControlli = true;
          //Debug Tempo
          stampaTempo("Modulistica::controlliModello - Fine",ar,cm,cr,ptime);
          //Debug Tempo
          return "ADS_MSG_ERROR="+syncErr;
        }
        if (ite == null) {
          closeSync(sync);
          erroreControlli = true;
          //Debug Tempo
          stampaTempo("Modulistica::controlliModello - Fine",ar,cm,cr,ptime);
          //Debug Tempo
          return "ADS_MSG_ERROR=Sync iterable nullo!";
        }

        StringTokenizer st = new StringTokenizer(campi, Parametri.SEPARAVALORI);
        String nextToken = "";
        while (st.hasMoreTokens())  {
          nextToken = st.nextToken();
          sxml += "<"+nextToken+">"+leggiValore(request,nextToken)+"</"+nextToken+">";
        }
        sxml += "</DOC></FUNCTION_INPUT>";
        ControlliParser cp = new ControlliParser(request,iddoc,cr,sxml,false,stato_doc,false);
        javaStm = cp.bindingDeiParametri(corpo);
//        javaStm = bindingDinamico(request,corpo,iddoc,cr,sxml,false);
        try{
          WrapParser wp = new WrapParser(javaStm);
          dbOp = Parametri.creaDbOp();
          result = wp.goExtended(request, dbOp);
          free(dbOp);
        } catch (Exception ijEx) {
          free(dbOp);
          throw new Exception(ijEx.toString());
        }
        if (result == null) {
          result = "";
        }
        if (result.length() != 0) {
          closeSync(sync);
          erroreControlli = true;
          //Debug Tempo
          stampaTempo("Modulistica::controlliModello - Fine",ar,cm,cr,ptime);
          //Debug Tempo
         return result;
        }
/*        while (ite.iterator().hasNext()) {
          id = (Integer)(ite.iterator().next());
          sync.executed(id.intValue());
        }*/
        boolean okrun = true;
        for (Integer intObj : ite) {
        okrun = okrun
            && (sync.executed(intObj.intValue(),us) == SyncSuite.SYNC_ESEGUITO);
//        logger.info("Eseguita sync su attivita " + intObj.intValue());
        }
        if (okrun) {
          sync.commit();
        } else {
          sync.rollback();
        }
        closeSync(sync);

     }
   } catch (Exception e) {
      loggerError("ServletModulistica::controlliModello() - Area: "+ar+" - Modello: "+cm+"- Richiesta: "+cr+" - Errore:  ["+ e.toString()+"]",e);
      corpoHtml += "<span class='AFCErrorDataTD'>"+"Errore in fase di lettura dei controlli da effettuare sul documento!"+"</span>";
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        corpoHtml += e.toString();
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        corpoHtml += e.getStackTrace().toString();
      }
    }
    finally {
      closeSync(sync);
    }

    queryC = "SELECT L.CORPO, L.DRIVER, L.CONNESSIONE, L.UTENTE, L.PASSWD, L.CAMPI, L.MSG_ERRORE, L.DSN, L.CONTROLLO "+
             "FROM CONTROLLI_MODELLI C, LIBRERIA_CONTROLLI L "+
             "WHERE C.AREA = :AREA AND "+
             "C.CODICE_MODELLO = :CODICE_MODELLO AND "+
             "C.CONTROLLO = L.CONTROLLO AND "+
             "C.AREA = L.AREA AND "+
             "C.SEQUENZA <> -999 AND "+
             "L.TIPO = 'P' " +
             "ORDER BY C.SEQUENZA";

    try {
      dbOpC = vu.getDbOp();
      dbOpC.setStatement(queryC);
      dbOpC.setParameter(":AREA",ar);
      dbOpC.setParameter(":CODICE_MODELLO",cm);
      dbOpC.execute();
      rsC = dbOpC.getRstSet();
      String conDRIVER = "";
      String conCONNESIONE = "";
      String conUTENTE = "";
      String conPASSWD = "";
      String sDsn = "";
      List<ControlloModello> controlloModelloList = new ArrayList<ControlloModello>();
      while (rsC.next()) {
        controlloModelloList.add(new ControlloModello(
            rsC.getString(9),
            rsC.getString(1),
            rsC.getString(6),
            rsC.getString(7),
            rsC.getString(2),
            rsC.getString(3),
            rsC.getString(4),
            rsC.getString(5),
            rsC.getString("DSN")));
      }

      for(int indexControlloModelloList =0;indexControlloModelloList<controlloModelloList.size();indexControlloModelloList++) {
        if (result.length() != 0) break;

        controllo = controlloModelloList.get(indexControlloModelloList).getControllo();

        //Controllo se c'� una personalizzazione
        Personalizzazioni pers = null;
        pers = (Personalizzazioni)request.getSession().getAttribute("_personalizzazioni_gdm");
        if (pers != null) {
          String persAction = pers.getPersonalizzazione(Personalizzazioni.LIBRERIA_CONTROLLI, ar+"#"+controllo);
          int j = persAction.indexOf("#");
          newControllo = persAction.substring(j+1);
        }
        if (newControllo.equalsIgnoreCase(controllo)) {
          conDRIVER = controlloModelloList.get(indexControlloModelloList).getConDRIVER();
          conCONNESIONE = controlloModelloList.get(indexControlloModelloList).getConCONNESIONE();
          conUTENTE = controlloModelloList.get(indexControlloModelloList).getConUTENTE();
          conPASSWD = controlloModelloList.get(indexControlloModelloList).getConPASSWD();
          corpo = controlloModelloList.get(indexControlloModelloList).getCorpo();
          corpo = corpo.replaceAll(ch," ");
          corpo = corpo.replaceAll("\n"," ");
          errMsg = controlloModelloList.get(indexControlloModelloList).getErrMsg();
          if (errMsg == null) {
            errMsg = "";
          }
          sDsn = controlloModelloList.get(indexControlloModelloList).getsDsn();
          if (sDsn == null) {
            sDsn = "";
          }
          sxml = sFunInput + "<ERROR>"+errMsg+"</ERROR><DOC>";
          campi = controlloModelloList.get(indexControlloModelloList).getCampi();
          if (campi == null) {
            campi = "";
          }
        } else {
          queryC = "SELECT CORPO, TIPO, DRIVER, CONNESSIONE, UTENTE, PASSWD, CAMPI, MSG_ERRORE, DSN, SBLOCCO_AUTOMATICO "+
                  "FROM LIBRERIA_CONTROLLI "+
                  "WHERE AREA = :AREA AND "+
                  "CONTROLLO = :CONTROLLO ";
          dbOp = vu.getDbOp();
          dbOp.setStatement(queryC);
          dbOp.setParameter(":AREA", ar);
          dbOp.setParameter(":CONTROLLO", newControllo);
          dbOp.execute();
          rst = dbOp.getRstSet();
          if (rst.next()) {
            conDRIVER = rst.getString(2);
            conCONNESIONE = rst.getString(3);
            conUTENTE = rst.getString(4);
            conPASSWD = rst.getString(5);
            corpo = rst.getString(1);
            corpo = corpo.replaceAll(ch," ");
            corpo = corpo.replaceAll("\n"," ");
            errMsg = rst.getString(7);
            if (errMsg == null) {
              errMsg = "";
            }
            sDsn = rst.getString("DSN");
            if (sDsn == null) {
              sDsn = "";
            }
            sxml = sFunInput + "<ERROR>"+errMsg+"</ERROR><DOC>";
            campi = rst.getString(6);
            if (campi == null) {
              campi = "";
            }
          }
        }
        sync = new SyncSuite(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
        if (cm_padre.length() > 0) {
        	cmex = cm_padre;
        } else {
        	cmex = cm;
        }
        ite = (Vector<Integer>)sync.isExecutable(ar,controllo,ar+"@"+cmex+"@"+cr,us);
        if (ite == null) {
        	ite = (Vector<Integer>)sync.isExecutable(ar,controllo,ar+"@"+cm+"@"+cr,us);
        }
//        ite = sync.isExecutable(ar,controllo,ar+"@"+cm+"@"+cr,us);
        syncErr = sync.getLastError();
        if (syncErr != null) {
          closeSync(sync);
          erroreControlli = true;
          //Debug Tempo
          stampaTempo("Modulistica::controlliModello - Fine",ar,cm,cr,ptime);
          //Debug Tempo
          return "ADS_MSG_ERROR="+syncErr;
        }
        if (ite == null) {
          closeSync(sync);
          erroreControlli = true;
          //Debug Tempo
          stampaTempo("Modulistica::controlliModello - Fine",ar,cm,cr,ptime);
          //Debug Tempo
          return "ADS_MSG_ERROR=Sync iterable nullo!";
        }

        StringTokenizer st = new StringTokenizer(campi, Parametri.SEPARAVALORI);
        String nextToken = "";
        while (st.hasMoreTokens())  {
          nextToken = st.nextToken();
          sxml += "<"+nextToken+">"+leggiValore(request,nextToken)+"</"+nextToken+">";
        }
        sxml += "</DOC></FUNCTION_INPUT>";

        if (sDsn.length() != 0) {
          Connessione cn = new Connessione(vu.getDbOp(),sDsn);
          conDRIVER      = cn.getDriver();
          conCONNESIONE = cn.getConnessione();
          conUTENTE        = cn.getUtente();
          conPASSWD      = cn.getPassword();
        }

        if (conDRIVER == null) {
          conDRIVER = "";
        }

        if (conDRIVER.length() != 0) {
          String compConn = completaConnessione(conCONNESIONE);
          ConnessioneParser cp = new ConnessioneParser();
          String connessione = cp.bindingDeiParametri(compConn);
          if (connessione == null){
            connessione = conCONNESIONE;
          }
          dbOpAutonoma=true;
          dbOpF = SessioneDb.getInstance().createIDbOperationSQL(conDRIVER,connessione,conUTENTE,conPASSWD);
        } else {
          dbOpF = vu.getDbOp();
        }
        ControlliParser cp = new ControlliParser(request,iddoc,cr,sxml,false,stato_doc, false);
        dbOpF.setCallFunc(cp.bindingDeiParametri(corpo));
//        dbOpF.setCallFunc(bindingDinamico(request,corpo,iddoc,cr,sxml,false));
        dbOpF.execute();
        result = dbOpF.getCallSql().getString(1);
        if (result == null) {
          result = "";
        }
        if (dbOpAutonoma) free(dbOpF);
/*        while (ite.iterator().hasNext()) {
          id = (Integer)(ite.iterator().next());
          sync.executed(id.intValue());
        }*/
        boolean okrun = true;
        for (Integer intObj : ite) {
        okrun = okrun
            && (sync.executed(intObj.intValue(),us) == SyncSuite.SYNC_ESEGUITO);
//        logger.info("Eseguita sync su attivita " + intObj.intValue());
        }
        if (okrun) {
          sync.commit();
        } else {
          sync.rollback();
        }
        closeSync(sync);
      }

    } catch (Exception e) {
      loggerError("ServletModulistica::controlliModello() - Area: "+ar+" - Modello: "+cm+"- Richiesta: "+cr+" - Errore:  ["+ e.toString()+"]",e);
      corpoHtml += "<span class='AFCErrorDataTD'>"+"Errore in fase di lettura dei controlli da effettuare sul documento!"+"</span>";
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        corpoHtml += e.toString();
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        corpoHtml += e.getStackTrace().toString();
      }
    }
    finally {
      if (dbOpAutonoma) free(dbOpF);
      closeSync(sync);
    }

    if (result.length() != 0) {
      erroreControlli = true;
    } else {
      erroreControlli = false;
    }
    //Debug Tempo
    stampaTempo("Modulistica::controlliModello - Fine",ar,cm,cr,ptime);
    //Debug Tempo
    return result;
  }

 /**
  * costruisciModulo()
  * Metodo per la costruzione del modello da presentare all'utente.
  * Provvede a verificare l'esistenza su db del modello richiesto.
  * Ricerca a livello di sessione la presenza del modello stesso e se � gi� presente evita
  * di ricaricarlo da database.
  * Tale caso si verifica quando un modulo viene costruito ma non registrato.
  * Attenzione: quando si fa una richiesta di reload si perde la querystring passata originariamente
  *             in occasione della prima chiamata al modello.
  *             Il controllo viene fatto in fase di doGet ed eventualmente viene recuperata la
  *             vecchia querystring dal modello che deve trovarsi comunque in sessione. La stringa viene
  *             passata al metodo di costruzione modello che eventualmente, in caso di costruzione ex novo,
  *             si occupa di memorizzarla nella variabile di classe queryHtmlIn.
  **/
  private void costruisciModulo(HttpServletRequest  request,
                                  String            queryURL,
                                  boolean           primo_caricamento) {

    HttpSession     httpSess;
    ModelloHTMLOut  mdOut = null;
    ListaControlli  controlli, controlliMod;
    String          ar = "", cm = "", cr = ""; //, sc;//, reload;
    String          iddoc = "";

    try {
    	iddoc = ricercaIdDocumento(request,null);


      ar = request.getParameter("area");
      cm = request.getParameter("cm");
      cr = request.getParameter("cr");

      //Debug Tempo
      long ptime = stampaTempo("Modulistica::costruisciModulo - Inizio",ar,cm,cr,0);
      //Debug Tempo

      httpSess = request.getSession();

      // ***** CONTROLLI *****
      controlli = (ListaControlli)httpSess.getAttribute("listaControlli");

      if (controlli == null) {
        // Inizializzo una variabile di tipo ListaControlli a livello di sessione
        // che servir� a contenere i puntatori ai vari controlli letti dal database.
        // In questo modo i singoli oggetti Controllo verranno inizializzati solo
        // una volta per ogni coppia di "area , controllo" (pk) e referenziati
        // nelle liste dei singoli oggetti di volta in volta.
        controlli = new ListaControlli();
        httpSess.setAttribute("listaControlli", controlli);
      }
      controlliMod = (ListaControlli)httpSess.getAttribute("listaControlliMod");

      if (controlliMod == null) {
        controlliMod = new ListaControlli();
        httpSess.setAttribute("listaControlliMod", controlliMod);
      }

      if (cr == null) {
        // Se il numero di richiesta non viene passato probabilmente non serve (� un modulo che
        // viene solo composto per essere stampato).
        // In questo caso se non viene indicata la scadenza la si assume 1 per default, poich�
        // con ogni probabilit� il successivo invio dell'XML non verr� fatto per cui i dati
        // rimarrebbero nel db all'infinito, perch� privi della data di scadenza
        cr = (String)httpSess.getAttribute("key");

      }

      if (!primo_caricamento) {
        // Se il modello era ancora in memoria dovrebbe essere di tipo HTMLIn quindi
        // risetto la request e ricalcolo i valori di default dei campi
        mdg.setNewRequest(request);
        mdg.interpretaModello(vu.getDbOp());
        mdg.setGDCLink(gdc_link);
      }
      modSuccessivo = mdg.getSuccessivo();
      String esiste = ricercaIdDocumento(request,null);
      mdg.setNuovoDoc(esiste);
      if (esiste == null) {
        esiste = "";
      }
      if (esiste.length() == 0) {
        request.getSession().setAttribute("GDM_NEW_DOC", "Y");
      } else {
        request.getSession().setAttribute("GDM_NEW_DOC", "N");
      }

      String ads_msg_err = request.getParameter("ADS_MSG_ERROR");
      String list_fields = request.getParameter("LIST_FIELDS_ERROR");
      String controllo = request.getParameter("controllo");
      if (ads_msg_err == null) {
        ads_msg_err = "";
      }
      ads_msg_err += erroreAction;
      if (list_fields == null) {
        list_fields = "";
      }
      if (controllo == null) {
        controllo = "";
      }
      if (controllo.length() == 0) {
    	  if (erroreAction.length() > 0) {
    		  mdg.settaErrMsg(erroreAction);
    	  }
        listaAlle = mdg.getAllegati();
        isW3c = mdg.isW3c();
        corpoHtml += mdg.getValue(vu.getDbOp());  // Restituisce il modello.
        if (gdc_link != null) {
          if (gdc_link.length() != 0) {
            corpoHtml = gdc_refresh + corpoHtml;
          }
        }
      } else {
        if (ads_msg_err.length() == 0 && list_fields.length() == 0) {
          cancellaRepository(request, iddoc, false);
          mdOut = new ModelloHTMLOut((ModelloHTMLIn)mdg);
          listaAlle = mdg.getAllegati();
          isW3c = mdg.isW3c();
          corpoHtml += mdOut.getValue(vu.getDbOp());
          modelli.remove(mdg);
          request.getSession().setAttribute("modelli",modelli);
        } else {
//          mdIn = (ModelloHTMLIn)md;
          mdg.settaErrMsg(ads_msg_err);
          mdg.settaListFields(list_fields);
          listaAlle = mdg.getAllegati();
          isW3c = mdg.isW3c();
          corpoHtml += mdg.getValue(vu.getDbOp());  // Restituisce il modello.
        }
      }
      //Debug Tempo
      stampaTempo("Modulistica::costruisciModulo - Fine",ar,cm,cr,ptime);
      //Debug Tempo

    } catch(Exception e) {
      loggerError("ServletModulistica::costruisciModulo() - Area: "+ar+" - Modello: "+cm+"- Richiesta: "+cr+" - Attenzione! Si � verificato un errore: "+e.toString(),e);
      corpoHtml += "<h2>Attenzione! Si � verificato un errore.</h2><br/><h4>Errore in fase di costruzione del modello.</h4>";
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
  private void costruisciModuloLettura(HttpServletRequest  request,
                                  String              queryURL) {
    HttpSession     httpSess;
//    Modello         md = null;
    ModelloHTMLOut  mdOut = null;
//    ArrayList       modelli;
    ListaControlli  controlli;
//    ListaProtetti   protetti;
    String          ar = "",
                    cm = "",
                    cr = "",
                    rw;

    try {
      ar = request.getParameter("area");
      cr = request.getParameter("cr");
      cm = request.getParameter("cm");
      rw = request.getParameter("rw");
//      sc = request.getParameter("sc");  // GIORNI DI VALIDITA' DEI DATI (sc sta per scadenza ma indica una durata)
      httpSess = request.getSession();
      if (cr == null) {
        cr = (String)httpSess.getAttribute("key");
      }
      //Debug Tempo
      long ptime = stampaTempo("Modulistica::costruisciModuloLettura - Inizio",ar,cm,cr,0);
      //Debug Tempo


      if (rw == null) {
        rw = "C";
      }
      // ***** MODELLI *****
//      modelli = (ArrayList) httpSess.getAttribute("modelli");
//      if (modelli == null) {
//        // Inizializzo la lista dei modelli a livello di sessione.
//        // In questo modo mi garantisco che la servlet restituisca il modello
//        // al client da cui viene invocato.
//        modelli = new ArrayList();
//        httpSess.setAttribute("modelli", modelli);
//      }

      // ***** CONTROLLI *****
      controlli = (ListaControlli)httpSess.getAttribute("listaControlli");

      if (controlli == null) {
        // Inizializzo una variabile di tipo ListaControlli a livello di sessione
        // che servir� a contenere i puntatori ai vari controlli letti dal database.
        // In questo modo i singoli oggetti Controllo verranno inizializzati solo
        // una volta per ogni coppia di "area , controllo" (pk) e referenziati
        // nelle liste dei singoli oggetti di volta in volta.
        controlli = new ListaControlli();
        httpSess.setAttribute("listaControlli", controlli);
      }

/*      //***** DOMINI *****
      domini = (ListaDomini) httpSess.getAttribute("listaDomini");

      if (domini == null) {
        // Inizializzo una variabile di tipo ListaDomini a livello di sessione
        // che servir� a contenere i puntatori ai vari domini letti dal database.
        // In questo modo i singoli oggetti Dominio verranno inizializzati solo
        // una volta per ogni coppia di "area , dominio" (pk) e referenziati
        // nelle liste dei campi di volta in volta.
        domini = new ListaDomini();
        httpSess.setAttribute("listaDomini", domini);
      } else {
        // Aggiorno i valori dei domini gi� in memoria (la funzione della lista
        // si preoccupa di aggiornare solo quelli che sono parametrici)
        domini.aggiornaDomini(request);
      }

      domini.caricaDominiiDiArea(ar, request);
      cm = request.getParameter("cm");
      domini.caricaDominiiDelModello(ar, cm, request);*/
//      caricaDatiIniziali(request);
//      protetti = (ListaProtetti)httpSess.getAttribute("listaProtetti");
//      if (protetti == null) {
//        protetti = new ListaProtetti();
//        protetti.caricaDominii(ar,cm,request);
//        httpSess.setAttribute("listaProtetti", protetti);
//      }

//      sc = request.getParameter("sc");  // GIORNI DI VALIDITA' DEI DATI (sc sta per scadenza ma indica una durata)
//
//      //Modifica di MMA.
//      if (sc == null) {
//        sc = "1";
//      }

      // Verifico l'esistenza del modello richiesto.
//      verificaEsistenzaModello(ar, cm);
//      if (isPrimoModello(ar, cr)) {
        // E' il primo modello quindi devo precaricare i dati necessari

//      preCaricamentoDati(request, ar, cr, cm);
//      md = new ModelloHTMLIn(request, ar, cm, cr, calcolaScadenza(sc));
//      modelli.add(md);
//      } else {
//        // Non � il primo modello per cui vado a cercare se � gi� in memoria
//        if (isModelloAperto(ar, cr, cm)) {
//          preCaricamentoDati(request, ar, cr, cm, false);
//        }
//
//        md = cercaModello(httpSess, ar, cm, cr);
//
//        if (md == null) {
//          // Se il modello non esiste devo crearne uno nuovo
//          md = new ModelloHTMLIn(request, ar, cm, cr, calcolaScadenza(sc));
//
//          if (queryURL != null)       // Se queryURL non � null allora si tratta di un reload e quindi
//            md.setQueryURL(queryURL); // bisogna valorizzare la variabile queryHtmlIn che risulterebbe nulla
//
//          modelli.add(md);
//        } else {
//          // Se il modello era ancora in memoria dovrebbe essere di tipo HTMLIn quindi
//          // risetto la request e ricalcolo i valori di default dei campi
//          md.setNewRequest(request);
//          ((ModelloHTMLIn)md).aggiornaValori(request);
//        }

//      }
      mdOut = new ModelloHTMLOut(mdg);
      String iddoc = ricercaIdDocumento(request,null);
      mdOut.settaDocumento(id_tipodoc,iddoc,vu);
      modSuccessivo = mdg.getSuccessivo();
      listaAlle = mdg.getAllegati();
      if (rw.equalsIgnoreCase("R") || rw.equalsIgnoreCase("P") || rw.equalsIgnoreCase("A") || rw.equalsIgnoreCase("V") || livellocheck == 2 || stato_doc.equals(Global.STATO_COMPLETO)) {
        corpoHtml += mdOut.getPrivPRNValue(vu.getDbOp());
        isW3c = mdOut.isW3c();
      } else {
        corpoHtml += mdOut.getPrivPRNComValue(vu.getDbOp());
        isW3c = mdOut.isW3c();
      }
      modelli.remove(mdg);
      request.getSession().setAttribute("modelli",modelli);
      //Debug Tempo
      stampaTempo("Modulistica::costruisciModuloLettura - Fine",ar,cm,cr,ptime);
      //Debug Tempo

    } catch(Exception e) {
      loggerError("ServletModulistica::costruisciModuloLettura() - Area: "+ar+" - Modello: "+cm+"- Richiesta: "+cr+" - Attenzione! Si � verificato un errore: "+e.toString(),e);
      corpoHtml += "<h2>Attenzione! Si � verificato un errore.</h2><br/><h4>Errore in fase di costruzione del modello.</h4>";
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
  private void free(IDbOperationSQL dbOp) {
    try {
      dbOp.close();
    } catch (Exception e) { }
  }

  /**
  * Inizializzo il dizionario di tutte le stringhe (composte da un carattere singolo)
  * che possono segnalarmi la fine del nome di un parametro.
  * Caratteri considerati come fine parametro quindi non utilizzabili nei
  * nomi di parametro:
  * 			spazio
  * 			,
  * 			)
  * 			(
  * 			*
  * 			+
  * 			/
  * 			-		(� meno non underscore)
  * 			\
  * 			|
  * 			&
  * 			=
  * 			<
  * 			>
  *       .
  */
//  private static void initDizionario()
//  {
//      if(dizionario != null)
//      {
//          return;
//      } else
//      {
//          byte b2[] = new byte[1];
//          b2[0] = 13;
//          String ch = new String(b2);
//          dizionario = new Properties();
//          dizionario.setProperty(" ", "OK");
//          dizionario.setProperty(",", "OK");
//          dizionario.setProperty(")", "OK");
//          dizionario.setProperty("(", "OK");
//          dizionario.setProperty("*", "OK");
//          dizionario.setProperty("+", "OK");
//          dizionario.setProperty("/", "OK");
//          dizionario.setProperty("-", "OK");
//          dizionario.setProperty("|", "OK");
//          dizionario.setProperty("&", "OK");
//          dizionario.setProperty("=", "OK");
//          dizionario.setProperty("<", "OK");
//          dizionario.setProperty(">", "OK");
//          dizionario.setProperty("'", "OK");
//          dizionario.setProperty("\"", "OK");
//          dizionario.setProperty("\n", "OK");
//          dizionario.setProperty("\t", "OK");
//          dizionario.setProperty("\\", "OK");
//          dizionario.setProperty(":", "OK");
//          dizionario.setProperty("?", "OK");
//          dizionario.setProperty(".", "OK");
//          dizionario.setProperty(ch, "OK");
//          dizionario.setProperty("!", "OK");
//          dizionario.setProperty("%", "OK");
//          dizionario.setProperty("[", "OK");
//          dizionario.setProperty("]", "OK");
//          dizionario.setProperty("{", "OK");
//          dizionario.setProperty("}", "OK");
//          return;
//      }
//  }

//  /**
//   *
//   */
//  private void inserisciAllegati(HttpServletRequest  request, String cr, String cm) {
////    IDbOperationSQL dbOp = null;
////    ResultSet      rst = null;
//    String         cancella = null;
//    String         idDoc = null;
//    String         area = request.getParameter("area");
//    int            j = 0;
//    //Debug Tempo
//    long ptime = stampaTempo("Modulistica::inserisciAllegati - Inizio",area,cm,cr,0);
//    //Debug Tempo
//
//    try {
//      idDoc = (new DocUtil(vu)).getIdDocumento(id_tipodoc,cr);
//      if (idDoc == null) {
//        idDoc = "";
//      }
//
//      String uAgg = mdg.getUltimoAgg();
//
//      if (uAgg == null) {
//        uAgg = "";
//      }
//      String iddoc = ricercaIdDocumento(request,null);
//      AccediDocumento ad = new AccediDocumento(iddoc,vu);
//      ad.accediDocumentoAllegati();
//      Vector lAllegati = ad.listaIdOggettiFile();
//      AggiornaDocumento ad2 = new AggiornaDocumento(iddoc,vu);
//
//
//      if (!uAgg.length() == 0) {
//        ad2.setUltAggiornamento(uAgg);
//      }
//      if (lAllegati != null) {
//        while(j < lAllegati.size()) {
//          String cod = request.getParameter("hAllegato_"+j);
//          cancella = request.getParameter("ceckAllegato_"+j);
//          if (cancella == null || cancella.length() == 0) {
//            cancella = null;
//          }
//          if (cancella != null) {
//            ad2.cancellaAllegato(cod);
//          }
//          j=j+1;
//        }
//      }
//
///*      String query = "SELECT NOMEFILE, ALLEGATO FROM ALLEGATI_TEMP " +
//        "WHERE AREA = :AREA AND "+
//        "CODICE_RICHIESTA = :CR AND "+
//        "CODICE_MODELLO = :CM "+
//        "ORDER BY NOMEFILE ASC";
//      dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
//      dbOp.setStatement(query);
//      dbOp.setParameter(":AREA",area);
//      dbOp.setParameter(":CM",cm);
//      dbOp.setParameter(":CR",cr);
//      dbOp.execute();
//      rst = dbOp.getRstSet();
//      while(rst.next()) {
//        String cod = rst.getString(1);
//        InputStream sAll = dbOp.readBlob("ALLEGATO");
////        cancella = request.getParameter("hAllegato_"+j);
////        if (cancella.equalsIgnoreCase("0")) {
//        cancella = request.getParameter("ceckAllegato_"+j);
//        if (cancella == null || cancella.length() == 0) {
//          cancella = null;
//        }
//        if (cancella == null) {
//          try {
//            ad2.aggiornaAllegato(sAll,cod);
//          } catch (Exception e) {
//            loggerError("ServletModulistica::inserisciAllegati() - Area: "+area+" - Modello: "+cm+"- Richiesta: "+cr+" - Errore 1:  ["+ e.toString()+"]",e);
//            corpoHtml += "<span class='AFCErrorDataTD'>"+"Errore in inserimento allegati!"+"</span>";
//            if (Parametri.DEBUG.equalsIgnoreCase("1")) {
//              corpoHtml += e.toString();
//            }
//            if (Parametri.DEBUG.equalsIgnoreCase("2")) {
//              corpoHtml += e.getStackTrace().toString();
//            }
//          }
//        }
//      }*/
//      ad2.salvaAllegatiTemp(true);
//      ad2.salvaDocumentoBozza();
//      settaDataAggiornamentoModello(request,ad2.getUltAggiornamento());
///*      query = "DELETE ALLEGATI_TEMP "+
//        "WHERE AREA = :AREA AND "+
//        "CODICE_RICHIESTA = :CR AND "+
//        "CODICE_MODELLO = :CM ";
//
//      dbOp.setStatement(query);
//      dbOp.setParameter(":AREA",area);
//      dbOp.setParameter(":CM",cm);
//      dbOp.setParameter(":CR",cr);
//      dbOp.execute();
//      dbOp.commit();
//      free(dbOp);*/
//    } catch (Exception e) {
//      //free(dbOp);
//      loggerError("ServletModulistica::inserisciAllegati() - Area: "+area+" - Modello: "+cm+"- Richiesta: "+cr+" - Errore 2:  ["+ e.toString()+"]",e);
//      corpoHtml += "<span class='AFCErrorDataTD'>"+"Errore in inserimento allegati!"+"</span>";
//      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
//        corpoHtml += e.toString();
//      }
//      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
//        corpoHtml += e.getStackTrace().toString();
//      }
//    }
//    //Debug Tempo
//    stampaTempo("Modulistica::inserisciAllegati - Fine",area,cm,cr,ptime);
//    //Debug Tempo
//  }

  /**
   *
   */
  private boolean inserisciValori(HttpServletRequest  request) {
    boolean         result = true;
    IDbOperationSQL  dbOp = null;
    ResultSet       rs = null;
    String          query;
    String          dato = null;
    String          calcolato = null;
    String          valore = null;
    String          tipoCampo = null;
    String          tipoAccesso = null;
    String          codError = "";
    String          msgError = "";
    String          ar,
                    cm,
                    cr,
                    rw;

    ar   = request.getParameter("area");
    cm   = request.getParameter("cm");
    cr   = request.getParameter("cr");
    rw   = request.getParameter("rw");
    if (cr == null) {
      cr = (String)request.getSession().getAttribute("key");
    }
    //Debug Tempo
    long ptime = stampaTempo("Modulistica::inserisciValori - Inizio",ar,cm,cr,0);
    //Debug Tempo

    String idCartProveninez = request.getParameter("idCartProveninez");
    if (idCartProveninez == null) {
      idCartProveninez = "";
    }
    String idQueryProveninez = request.getParameter("idQueryProveninez");
    if (idQueryProveninez == null) {
      idQueryProveninez = "";
    }
    String Provenienza = request.getParameter("Provenienza");
    if (Provenienza == null) {
      Provenienza = "";
    }
    String WkrSp = (String)request.getSession().getAttribute("WRKSP");
    if (WkrSp == null) {
      WkrSp = "";
    }
    String us = (String)request.getSession().getAttribute("Utente");

    try {
      dbOp = vu.getDbOp();
    	String livello_errore = "0";
      query = "SELECT VALORE "+
              "FROM   PARAMETRI "+
              "WHERE  CODICE = :CODICE AND "+
              "TIPO_MODELLO = :TIPO" ;
      dbOp.setStatement(query);
      dbOp.setParameter(":CODICE", "LIVELLO_ERRORE_CAMPI_VUOTI");
      dbOp.setParameter(":TIPO", "@STANDARD");
      dbOp.execute();
      rs = dbOp.getRstSet();
      if (rs.next())
      	livello_errore = rs.getString("VALORE");

      if (livello_errore == null) {
      	livello_errore = "0";
      }
      AggiungiDocumento ad = new AggiungiDocumento(id_tipodoc, vu);
      ad.settaCodiceRichiesta(cr);

      Dominio dp = null;
      ListaDomini ld = (ListaDomini)request.getSession().getAttribute("listaDomini");
      query = "SELECT DATO, CAMPO_CALCOLATO, TIPO_CAMPO, TIPO_ACCESSO  "+
                "FROM DATI_MODELLO "+
              "WHERE    AREA = :AREA AND "+
                       "CODICE_MODELLO = :CODICE_MODELLO AND "+
                       "NVL(IN_USO,'Y') = 'Y' AND "+
                       "(DATO NOT LIKE '$%' "+
                       "OR DATO IN ('$BARCODE1','$BARCODE2','$BARCODE3','$MASTER')) "+
              "ORDER BY DATO ASC";

      dbOp.setStatement(query);
      dbOp.setParameter(":AREA",ar);
      dbOp.setParameter(":CODICE_MODELLO",cm);
      dbOp.execute();
      rs = dbOp.getRstSet();
      boolean dati_mancanti = esistonoDatiInRequest(request, rs, ar, cm, cr, livello_errore);
      if (dati_mancanti) {
        if (livello_errore.equals("0")) {
        	corpoHtml = "";
        } else {
          if (!livello_errore.equals("1")) {
          	result= false;
          	return result;
          }
        }
      }

      dbOp.execute();
      rs = dbOp.getRstSet();
      List<DatoModello> listaDatiModello = new ArrayList<DatoModello>();
      while (rs.next()) {
        dato      = rs.getString(1);
        calcolato = rs.getString(2);
        tipoCampo = rs.getString(3);
        tipoAccesso = rs.getString(4);

        listaDatiModello.add(new DatoModello(dato,calcolato,tipoCampo,tipoAccesso));
      }

      for(int indexDatoModello=0;indexDatoModello<listaDatiModello.size();indexDatoModello++) {
        dato      = listaDatiModello.get(indexDatoModello).getDato();
        calcolato = listaDatiModello.get(indexDatoModello).getCalcolato();
        tipoCampo = listaDatiModello.get(indexDatoModello).getTipoCampo();
        tipoAccesso =listaDatiModello.get(indexDatoModello).getTipoAccesso();
        valore = null;

        if (tipoAccesso.equalsIgnoreCase("L")) {
          String myVal = null;
          if (ld != null) {
            int numDom = ld.getNumDomini();
            int i = 0;
            while (i < numDom && valore == null) {
              dp = (Dominio)ld.getDominio(i);
              if (dp.isDominioFormulaModello()) {
                myVal = dp.getValore(dato);
              }
              i++;
            }
          }
          if (rw.equalsIgnoreCase("W") && calcolato.equalsIgnoreCase("S")) {
            valore = myVal;
          }
          if (calcolato.equalsIgnoreCase("C")) { //Esiste
            valore = myVal;
          }
          if (calcolato.equalsIgnoreCase("V")) {
            valore = myVal;
          }
        }

        int inizio = 0xFF;
        if (valore == null) {
          if (tipoCampo.charAt(0) != 'B') {            // new!
            // E' un campo legato ad un dominio, ma non � di tipo CHECKBOX
            valore = request.getParameter(dato);
//            if (tipoCampo.charAt(0) == 'Z') {
//                inizio = 0x7F;
//              if (valore != null) {
//                String valoreNew = "";
//                int ic;
//                valore = URLDecoder.decode(valore,"ISO-8859-1");
//                int up = 0;
//                int posCod = valore.indexOf("mmacode(");
//                while (posCod > -1) {
//                  valoreNew += valore.substring(up,posCod);
//                  up = valore.indexOf(")",posCod);
//                  String codiceCh = valore.substring(posCod+8,up);
//                  up = up + 1;
//                  ic = Integer.parseInt(codiceCh);
//                  valoreNew += (char)ic;
//                  posCod = valore.indexOf("mmacode(",up);
//                }
//                valoreNew += valore.substring(up);
//                valore = valoreNew;
//              }
//            }
            if (valore != null) {
            	String[] valori = request.getParameterValues(dato);
            	int numValori = valori.length;
            	if (numValori > 1) {
            		valore = valori[numValori-1];
            	}
              valore = encode(valore,tipoCampo);
            }
          } else {
            // E' un campo legato ad un CAMPO di tipo CHECKBOX
            valore = "";
            for (int j=0; j<1000; j++) {  // 1000 valori al massimo.
              String s = request.getParameter(dato+"_"+Integer.toString(j));
              if (s != null){
                // Attacco sempre un separatore in fondo, in tal modo quando dovr�
                // recuperare un valore lo richiamer� sempre utilizzando il separatore
                valore = valore + s + Parametri.SEPARAVALORI;
              }
            }
            if (valore.length() == 0) {
              valore = null;
            }
          }
        }

        if (valore == null) {
          valore = "";
        }

        if (valore.length() == 0) {
          if (calcolato.equalsIgnoreCase("S") && rw.equalsIgnoreCase("W")) {
            if (ld != null) {
              int numDom = ld.getNumDomini();
              int i = 0;
              valore = null;
              while (i < numDom && valore == null) {
                dp = (Dominio)ld.getDominio(i);
                if (dp.isDominioFormulaModello()) {
                  valore = dp.getValore(dato);
                }
                i++;
              }
            }
          }
        }

        if (valore == null) {
          valore = "";
        }

//        if (valore != null) {
//          if (valore.length() > lunghezzaStandard) {
//            valore = valore.substring(0,lunghezzaStandard);
//          }
//        }
        if ((!calcolato.equalsIgnoreCase("v")) && (valore != null)) {
        	if (valore.length() == 0 && !esisteDatoInRequest(request, dato, ar, cm, cr) && tipoCampo.charAt(0) != 'B' && tipoCampo.charAt(0) != 'R') {
        		//Non faccio niente
        		logger.error("Campo "+dato+"  non salvato perch� non presente in request" );
        	} else {
        		ad.aggiungiDati(dato,valore);
        	}
        }
      }

      String errore = "";
      if (pulsPrima.length() != 0) {
        errore = eseguiControllo(request,pulsPrima,true,ad,null,cr,null);
        if (errore.length() != 0) {
//          corpoHtml += corpoHtml.replaceFirst("<!--ERROREACTION-->", "<span class='AFCErrorDataTD'>"+errore+"</span>");
        	erroreAction = errore;
          result = false;
          return result;
        }
        istanziaIter(request);
      }
      ad.salvaAllegatiTemp(true);
      try {
      	ad.salvaDocumentoBozza();
      } catch (Exception e2) {
      	codError = ad.getCodeError();
      	if (codError != null) {
      		msgError = ad.getDescrCodeError();
      		throw new Exception (msgError);
      	} else {
      		throw e2;
      	}
      }
      settaDataAggiornamentoModello(request,ad.getUltAggiornamento());
      if (idCartnew == null) {
      	idCartnew = "";
      }
//      if (idCartProveninez.length() == 0) {
      	if (idCartnew.length() != 0) {
      		idCartProveninez = idCartnew;
      		Provenienza = "C";
      		idQueryProveninez = "-1";
      	}
//      }
      if (idCartProveninez.length() != 0) {
        String creaLink = request.getParameter("CREA_LINK");
        if (creaLink == null) {
          creaLink = "";
        }
        CCS_TreeSelezionaDocumento TreeSelDoc = new
        CCS_TreeSelezionaDocumento(idCartProveninez,Provenienza,
                                     WkrSp,idQueryProveninez,
                                     ar,cr,cm,creaLink,
                                     new CCS_Common("jdbc/gdm",us));

        TreeSelDoc._afterInitialize();
      }
//      inserisciAllegati(request,cr,cm);

      if (pulsDopo.length() != 0) {
        String iddoc = ricercaIdDocumento(request,null);
        errore = eseguiControllo(request, pulsDopo, true, null, null, cr, iddoc);
        if (errore.length() != 0) {
//          corpoHtml += "<span class='AFCErrorDataTD'>"+errore+"</span>";
//        	corpoHtml += corpoHtml.replaceFirst("<!--ERROREACTION-->", "<span class='AFCErrorDataTD'>"+errore+"</span>");
        	erroreAction = errore;
          result = false;
        }
        istanziaIter(request);
      }
      if (iterAction != null && iterAction.length() != 0) {
      	istanziaIter(request, iterAction);
      }
      mdg.settaErrMsg(ad.getDescrCodeError());

    } catch (Exception e) {
      loggerError("ServletModulistica::inserisciValori() - Area: "+ar+" - Modello: "+cm+"- Richiesta: "+cr+" - Errore:  ["+ e.toString()+"]",e);
      result = false;
      corpoHtml += "<span class='AFCErrorDataTD'>"+"Errore in fase di inserimento valori!"+"</span>";
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        corpoHtml += e.toString();
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        corpoHtml += e.getStackTrace().toString();
      }
    }
    //Debug Tempo
    stampaTempo("Modulistica::inserisciValori - Fine",ar,cm,cr,ptime);
    //Debug Tempo
    return result;
  }

  /**
   *
   */
  private boolean leggiValori(HttpServletRequest  request,
                              String iddoc,
                              boolean binoltro) {
    boolean         result = true;
    IDbOperationSQL  dbOp = null;
//    IDbOperationSQL  dbOpRepo = null;
    ResultSet       rs = null;
    String          query;
    String          dato = null;
    String          valore = null;
    String          ar,
                    cm,
                    cr;

    ar   = request.getParameter("area");
    cm   = request.getParameter("cm");
    cr   = request.getParameter("cr");
    if (cr == null) {
      cr = (String)request.getSession().getAttribute("key");
    }
    //Debug Tempo
    long ptime = stampaTempo("Modulistica::leggiValori - Inizio",ar,cm,cr,0);
    //Debug Tempo

    query = "SELECT DATO "+
            "FROM DATI_MODELLO "+
            "WHERE AREA = :AREA AND "+
            "NVL(IN_USO,'Y') = 'Y' AND "+
            "CODICE_MODELLO = :CODICE_MODELLO";
    try {
      AccediDocumento ad = new AccediDocumento(iddoc,vu);
      ad.accediDocumentoValori();
//      settaDataAggiornamentoModello(request,ad.getUltAggiornamento());
      aggData = ad.getUltAggiornamento();
      request.getSession().setAttribute("valori_doc",ad);

      if (binoltro) {
        dbOp = vu.getDbOp();
        dbOp.setStatement(query);
        dbOp.setParameter(":AREA",ar);
        dbOp.setParameter(":CODICE_MODELLO",cm);
        dbOp.execute();
        rs = dbOp.getRstSet();
        List<String> listaDati = new ArrayList<String>();
        while (rs.next()) {
          listaDati.add(rs.getString(1));
        }

        for(int indexDato=0;indexDato<listaDati.size();indexDato++) {
          dato   = listaDati.get(indexDato);
          try {
            valore = ad.leggiValoreCampo(dato);
          } catch (Exception e1) {
            valore = "";
          }
          if (valore != null) {
            scriviPreInoltro(pidOp, ar, cr, cm, dato, valore);
          }
        }
      }

    }
      catch (Exception e) {
//      free(dbOpRepo);
      loggerError("ServletModulistica::leggiValori() - Area: "+ar+" - Modello: "+cm+" - Richiesta: "+cr+" - Campo: "+dato+" - Errore:  ["+ e.toString()+"]",e);
      result = false;
      corpoHtml += "<span class='AFCErrorDataTD'>"+"Errore in fase di lettura dei valori!"+"</span>";
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        corpoHtml += e.toString();
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        corpoHtml += e.getStackTrace().toString();
      }
    }
      //Debug Tempo
      stampaTempo("Modulistica::leggiValori - Fine",ar,cm,cr,ptime);
      //Debug Tempo

    return result;
  }

  /**
   *
   */
  private void scriviPreInoltro(String idOp, String ar, String cr, String cm, String dato, String valore) {
    //Debug Tempo
    long ptime = stampaTempo("Modulistica::scriviPreInoltro - Inizio",ar,cm,cr,0);
    //Debug Tempo
    IDbOperationSQL  dbOpSel = null;
    ResultSet       rs;
    String          querySel, queryIns;

    querySel = "SELECT 1 FROM PRE_INOLTRO" +
            " WHERE AREA = :AREA AND CODICE_RICHIESTA = :CR AND CODICE_MODELLO = :CM " +
            " AND DATO = :DATO AND ID_OP = :IDOP";

    try {
      dbOpSel = vu.getDbOp();
      dbOpSel.setStatement(querySel);
      dbOpSel.setParameter(":AREA",ar);
      dbOpSel.setParameter(":CR",cr);
      dbOpSel.setParameter(":CM",cm);
      dbOpSel.setParameter(":DATO",dato);
      dbOpSel.setParameter(":IDOP",idOp);
      dbOpSel.execute();
      rs = dbOpSel.getRstSet();
      if (valore.length() == 0) {
        queryIns = "DELETE PRE_INOLTRO" +
                   " WHERE AREA = :AREA AND CODICE_RICHIESTA = :CR AND CODICE_MODELLO = :CM " +
                   " AND DATO = :DATO AND ID_OP = :IDOP";
      } else {
        if (!rs.next()) {
          queryIns = "INSERT INTO PRE_INOLTRO" +
                     " (ID_OP, AREA, CODICE_RICHIESTA, CODICE_MODELLO, DATO, VALORE, PROGRESSIVO) VALUES "+
                     " (:IDOP, :AREA, :CR, :CM, :DATO, :VALORE, 1)";
        } else {
          queryIns = "UPDATE PRE_INOLTRO SET " +
                     " VALORE = :VALORE "+
                     " WHERE AREA = :AREA AND CODICE_RICHIESTA = :CR AND CODICE_MODELLO = :CM " +
                     " AND DATO = :DATO AND ID_OP = "+idOp;
        }
      }
      dbOpSel.setStatement(queryIns);
      dbOpSel.setParameter(":AREA",ar);
      dbOpSel.setParameter(":CR",cr);
      dbOpSel.setParameter(":CM",cm);
      dbOpSel.setParameter(":DATO",dato);
      dbOpSel.setParameter(":IDOP",idOp);
      if (valore.length() != 0) {
//        dbOpIns.setParameter(":VALORE",valore);
        byte bValore[] = valore.getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(bValore);
        dbOpSel.setAsciiStream(":VALORE", bais, bais.available());
      }
      dbOpSel.execute();
      dbOpSel.commit();
    } catch (Exception e) {
      loggerError("ServletModulistica::scriviPreInoltro() - Errore:  ["+dbOpSel.getStatementString()+" --"+ e.toString()+"]",e);
      corpoHtml += "<span class='AFCErrorDataTD'>"+"Attenzione! Problemi in fase di preinoltro!"+"</span>";
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        corpoHtml += e.toString();
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        corpoHtml += e.getStackTrace().toString();
      }
    }
    finally {
    }
    //Debug Tempo
    stampaTempo("Modulistica::scriviPreInoltro - Fine",ar,cm,cr,ptime);
    //Debug Tempo
  }

  /**
   *
   */
/*  private void scriviRepo (String ar, String cr, String cm, String dato, String valore, IDbOperationSQL dbOp) {
//    DbOperationSQL  dbOp = null;
//    DbOperationSQL  dbOpSel = null;
//    ResultSet       rs;
    String          query;

    querySel = "SELECT 1 FROM REPOSITORYTEMP " +
            " WHERE AREA = :AREA AND CODICE_RICHIESTA = :CR AND CODICE_MODELLO = :CM " +
            " AND DATO = :DATO";

    try {
      dbOpSel = new DbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
      dbOpSel.setStatement(querySel);
      dbOpSel.setParameter(":AREA",ar);
      dbOpSel.setParameter(":CR",id_session);
      dbOpSel.setParameter(":CM",cm);
      dbOpSel.setParameter(":DATO",dato);
      dbOpSel.execute();
      rs = dbOpSel.getRstSet();
      if (valore.length() == 0) {
        queryIns = "DELETE REPOSITORYTEMP " +
                   " WHERE AREA = :AREA AND CODICE_RICHIESTA = :CR AND CODICE_MODELLO = :CM " +
                   " AND DATO = :DATO";
      } else {
        if (!rs.next()) {
          queryIns = "INSERT INTO REPOSITORYTEMP " +
                     " (AREA, CODICE_RICHIESTA, CODICE_MODELLO, DATO, VALORE) VALUES "+
                     " (:AREA, :CR, :CM, :DATO, :VALORE)";
        } else {
          queryIns = "UPDATE REPOSITORYTEMP SET " +
                     " VALORE = :VALORE "+
                     " WHERE AREA = :AREA AND CODICE_RICHIESTA = :CR AND CODICE_MODELLO = :CM " +
                     " AND DATO = :DATO";
        }
      }
      dbOpIns = new DbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
      dbOpIns.setStatement(queryIns);
      dbOpIns.setParameter(":AREA",ar);
      dbOpIns.setParameter(":CR",id_session);
      dbOpIns.setParameter(":CM",cm);
      dbOpIns.setParameter(":DATO",dato);
      if (!valore.length() == 0) {
        byte b[] = valore.getBytes();
        InputStream is = new ByteArrayInputStream(b);
        dbOpIns.setAsciiStream(":VALORE",is,-1);
      }
      dbOpIns.execute();
      dbOpIns.commit();
      free(dbOpIns);
      free(dbOpSel);



    query = "DELETE REPOSITORYTEMP " +
               " WHERE AREA = :AREA AND CODICE_RICHIESTA = :CR AND CODICE_MODELLO = :CM " +
               " AND DATO = :DATO";

    try {
//      dbOp = new DbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
      dbOp.setStatement(query);
      dbOp.setParameter(":AREA",ar);
      dbOp.setParameter(":CR",id_session);
      dbOp.setParameter(":CM",cm);
      dbOp.setParameter(":DATO",dato);
      dbOp.execute();
      dbOp.commit();
      if (!valore.length() == 0) {
        query = "INSERT INTO REPOSITORYTEMP " +
                " (AREA, CODICE_RICHIESTA, CODICE_MODELLO, DATO, VALORE) VALUES "+
                " (:AREA, :CR, :CM, :DATO, :VALORE)";
        dbOp.setStatement(query);
        dbOp.setParameter(":AREA",ar);
        dbOp.setParameter(":CR",id_session);
        dbOp.setParameter(":CM",cm);
        dbOp.setParameter(":DATO",dato);
        byte b[] = valore.getBytes();
        InputStream is = new ByteArrayInputStream(b);
        dbOp.setAsciiStream(":VALORE",is,-1);
        dbOp.execute();
        dbOp.commit();
      }
//      free(dbOp);
    }
      catch (Exception e) {
//      free(dbOp);
      loggerError("ServletModulistica::scriviRepo() - Area: "+ar+"- Modello: "+cm+" - Campo: "+dato+" - Errore:  ["+ e.toString()+"]",e);
      corpoHtml += "Errore in fasi di scrittura della Repositorytemp! Campo: "+dato;
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        corpoHtml += e.toString();
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        corpoHtml += e.getStackTrace().toString();
      }
    }
  }
*/
  /**
   *
   */
  private String ricercaIdDocumento (HttpServletRequest  request, String cm_old) throws Exception {
    Vector      ll;
    String          idtipodoc = null;
    String          iddoc = null;
    String          ar,
                    cr,
                    cm;

    if (sIdDoc == null) {
      sIdDoc = "";
    }
    if (sIdDoc.length() != 0) {
      return sIdDoc;
    }
    ar      = request.getParameter("area");
    cr      = request.getParameter("cr");
    if (cm_old == null) {
      cm = request.getParameter("cm");
      idtipodoc = id_tipodoc;
    } else {
      cm = cm_old;
      idtipodoc = ricavaIdtipodoc(ar,cm);
    }

    if (cr == null) {
      cr = (String)request.getSession().getAttribute("key");
    }
    //Debug Tempo
    long ptime = stampaTempo("Modulistica::ricercaIdDocumento - Inizio",ar,cm,cr,0);
    //Debug Tempo

    if (lettura == null) {
      lettura = "R";
    }

    try {
    	DocUtil docU = new DocUtil(vu);
    	iddoc = docU.getIdDocumento(idtipodoc, cr);
			if (iddoc.length() > 0) {
      	stato_doc = docU.getStatoByIdDocumento(iddoc);
        if (!stato_doc.equalsIgnoreCase(Global.STATO_BOZZA)) {
          lettura = "R";
        }
			} else {
				iddoc = null;
			}

/*      RicercaDocumento rd = new RicercaDocumento(idtipodoc, ar, vu);
      rd.settaCodiceRichiesta(cr);
      ll = rd.ricerca();
      if(ll.size() == 1) {
        iddoc = (String)ll.firstElement();
        GD4_Status_Documento gd4s = new GD4_Status_Documento();
        vu.connect();
        gd4s.inizializzaDati(vu,iddoc);

        stato_doc = gd4s.verificaStato(iddoc);

        vu.disconnectClose();

        if (!stato_doc.equalsIgnoreCase(Global.STATO_BOZZA)) {
          lettura = "R";
        }
      } else {
        if (ll.size() > 0) {
          logger.error("ServletModulistica::ricercaIdDocumento() - Area: "+ar+" - Modello: "+cm+"- Richiesta: "+cr+" - Attenzione:  Numero documenti trovati "+ ll.size() +"]");
          corpoHtml += "<span class='AFCErrorDataTD'>"+"Attenzione:  Numero documenti trovati "+ ll.size()+"</span>";
        }
      }*/
    }
      catch (Exception e) {
      	e.printStackTrace();
      loggerError("ServletModulistica::ricercaIdDocumento() - Area: "+ar+" - Modello: "+cm+"- Richiesta: "+cr+" - Errore:  ["+ e.toString()+"]",e);
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        corpoHtml += e.toString();
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        corpoHtml += e.getStackTrace().toString();
      }
      throw e;
    }
    sIdDoc = iddoc;
    //Debug Tempo
    stampaTempo("Modulistica::ricercaIdDocumento - Fine",ar,cm,cr,ptime);
    //Debug Tempo
    return iddoc;
  }

  /**
   * loginSportello()
   * Test se l'utente collegato ha il diritto di richiedere il modulo attuale, quindi testo
   * se pu� accedere ai dati della attuale pratica in quella particolare area.
   *
   * @author  Marco Bonforte
   * @return  true se il login ha successo, false altrimenti
   */
  private boolean loginModulistica(String p_area,String p_cm, String p_richiesta,String p_user, String controllo, String iddoc, String esiste) {
    //Debug Tempo
    long ptime = stampaTempo("Modulistica::loginModulistica - Inizio",p_area,p_cm,p_richiesta,0);
    //Debug Tempo
    String          sComp = null;
    String          sAbil = "";

    if (controllo.equalsIgnoreCase("Y")) {
      //Debug Tempo
      stampaTempo("Modulistica::loginModulistica - Fine",p_area,p_cm,p_richiesta,ptime);
      //Debug Tempo
      return true;
    }

    try {
      if (iddoc == null) {
        if (esiste.length() == 0) {
          AccessoModulistica am = new AccessoModulistica(p_user,ruolo,id_tipodoc,p_richiesta,vu);
          sComp = am.getMaxComp();
          if (!sComp.equalsIgnoreCase("C")) {
            am = new AccessoModulistica("GUEST","GDM",id_tipodoc,p_richiesta,vu);
            sComp = am.getMaxComp();
            if (!sComp.equalsIgnoreCase("C")) {
//              corpoHtml += "<h2>Attenzione! Login NEGATO all'utente "+p_user+"</h2><h4>area "+p_area+" richiesta "+p_richiesta+"</h4>";
            	corpoHtml += errorLogin(p_area, p_cm, p_richiesta, p_user);
              //Debug Tempo
              stampaTempo("Modulistica::loginModulistica - Fine",p_area,p_cm,p_richiesta,ptime);
              //Debug Tempo
              return false;
            }
          }
        } else {
//          corpoHtml += "<h2>Attenzione! Login NEGATO all'utente "+p_user+"</h2><h4>area "+p_area+" richiesta "+p_richiesta+"</h4>";
        	corpoHtml += errorLogin(p_area, p_cm, p_richiesta, p_user);
        	//Debug Tempo
          stampaTempo("Modulistica::loginModulistica - Fine",p_area,p_cm,p_richiesta,ptime);
          //Debug Tempo
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
//          corpoHtml += "<h2>Attenzione! Login NEGATO all'utente "+p_user+"</h2><h4>area "+p_area+" richiesta "+p_richiesta+"</h4>";
        	corpoHtml += errorLogin(p_area, p_cm, p_richiesta, p_user);
          //Debug Tempo
          stampaTempo("Modulistica::loginModulistica - Fine",p_area,p_cm,p_richiesta,ptime);
          //Debug Tempo
          return false;
        } else {
          //Debug Tempo
          stampaTempo("Modulistica::loginModulistica - Fine",p_area,p_cm,p_richiesta,ptime);
          //Debug Tempo
          return true;
        }
      }

      if (sComp.equalsIgnoreCase("C")) {
        if (!p_richiesta.equalsIgnoreCase(id_session)) {
          cercaRichiesta(p_area,p_richiesta);
        }
      } else {
//        corpoHtml += "<h2>Attenzione! Login NEGATO all'utente "+p_user+"</h2><h4>area "+p_area+" richiesta "+p_richiesta+"</h4>";
      	corpoHtml += errorLogin(p_area, p_cm, p_richiesta, p_user);
        //Debug Tempo
        stampaTempo("Modulistica::loginModulistica - Fine",p_area,p_cm,p_richiesta,ptime);
        //Debug Tempo
        return false;
      }

    } catch (Exception e) {
      loggerError("ServletModulistica::loginSportello - Area: "+p_area+" - Modello: "+p_cm+" - Richiesta: "+p_richiesta+" - "+e.toString(),e);
//      corpoHtml += "<h2>Attenzione! Login NEGATO all'utente "+p_user+"</h2><h4>area "+p_area+" richiesta "+p_richiesta+"</h4>";
    	corpoHtml += errorLogin(p_area, p_cm, p_richiesta, p_user);
      //Debug Tempo
      stampaTempo("Modulistica::loginModulistica - Fine",p_area,p_cm,p_richiesta,ptime);
      //Debug Tempo
      return false;
    }
    //Debug Tempo
    stampaTempo("Modulistica::loginModulistica - Fine",p_area,p_cm,p_richiesta,ptime);
    //Debug Tempo
    return true;
  }

  /**
   * preCaricamentoDati()
   * Metodo per il precaricamento dei dati. Si cerca un dominio di area e si richiama la
   * opportuna funzione.
   *
   * @param request
   * @param pArea area di riferimento
   * @param cr codice richiesta
   * @author Adelmo Gentilini
   * @author Antonio Plastini
   */
   private void preCaricamentoDati(HttpServletRequest request,
                                   String pArea,
                                   String cr,
                                   String cm,
                                   boolean primo_caricamento) throws Exception {
    ListaDomini ld;

//    Dominio     dominioArea;
    //Debug Tempo
    long ptime = stampaTempo("Modulistica::preCaricamentoDati - Inizio",pArea,cm,cr,0);
    //Debug Tempo

    try {
      ld = (ListaDomini)request.getSession().getAttribute("listaDomini");
      if (ld == null) {
        ld = new ListaDomini();
        ld.caricaDominiiDiArea(pArea, request, primo_caricamento,vu.getDbOp());
        ld.caricaDominiiDelModello(pArea, cm, request, primo_caricamento,vu.getDbOp());
        if (request.getSession().getAttribute("esiste_documento") == null || !primo_caricamento) {
          ld.caricaDominiiFormulaModello(pArea, cm, request);
        }
        request.getSession().setAttribute("listaDomini",ld);
      } else {
        if (request.getSession().getAttribute("valori_doc") != null) {
          ld.caricaDominiiFormulaModello(pArea, cm, request,vu.getDbOp());
          request.getSession().setAttribute("listaDomini",ld);
        } else {
          // Aggiorno i valori dei domini gi� in memoria (la funzione della lista
          // si preoccupa di aggiornare solo quelli che sono parametrici)
          //ld.aggiornaDomini(request);
        }
      }
    } catch (Exception ex) {
      loggerError("ServletModulistica::preCaricamentoDati() - Area: "+pArea+" - Modello: "+cm+"- Richiesta: "+cr+" - Attenzione! Si � verificato un errore in fase di precaricamento: "+ex.toString(),ex);
      corpoHtml += "<span class='AFCErrorDataTD'>"+"Attenzione! Si � verificato un errore in fase di precaricamento"+"</span>";
      //Debug Tempo
      stampaTempo("Modulistica::preCaricamentoDati - Fine",pArea,cm,cr,ptime);
      //Debug Tempo
      throw ex;
    }
    //Debug Tempo
    stampaTempo("Modulistica::preCaricamentoDati - Fine",pArea,cm,cr,ptime);
    //Debug Tempo

  }

  /**
   * registraModulo()
   * Provvede a registrare nel db i dati caricati dall'utente.
   * Viene inoltre eliminato il modulo dalla memoria.
   * @param request
   * @param
   * @param isReload Indica se la richiesta di registrazione � stata fatta al fine di ricaricare lo stesso
   *                 modello, utilizzando informazioni inserite in prima istanza, ad esempio
   *                 in tutti i casi di comboBox in master-detail
   * @author Nicola Samoggia
   * @author Antonio Plastini
   **/
  private void registraModulo(HttpServletRequest  request,
                                boolean             isReload,
                                String              fase) {
    HttpSession     httpSess;
    ModelloHTMLOut  mdOut = null;
    String          ar = "",
                    cm = "",
                    cr = null;
    String          errore = "";
    String          ads_msg_err = "";
    String          list_field_err = "";
    int             inizio,fine;

    try {
      httpSess = request.getSession();
      ar = request.getParameter("area");
      cm = request.getParameter("cm");
      cr = request.getParameter("cr");
      if (cr == null) {
        cr = (String)httpSess.getAttribute("key");
      }
      //Debug Tempo
      long ptime = stampaTempo("Modulistica::registraModulo - Inizio",ar,cm,cr,0);
      //Debug Tempo

     // Viene visualizzato l'HTMLOut quando non si tratta di una richiesta di aggiornamento
      // Inoltre in caso di reload non si genera alcun pdf.
      if (!isReload) {
        // A questo punto suppongo di avere il ModelloHTMLIn cercato nella variabile mdIn
        mdOut = new ModelloHTMLOut(request, mdg, vu.getDbOp());
        gdc_refresh = "<input type='hidden' id='gdc_refresh' value='1' />";
        //Carico i dati nel documento
        String iddoc = ricercaIdDocumento(request,null);
        if (iddoc == null) {
          //Inserisco il documento
          //Controlli del lato SERVER del modello
          errore = controlliModello(request, ar, cm, cr, iddoc);
          if (errore == null) {
            errore = "";
          }
          inizio = errore.indexOf("ADS_MSG_ERROR=");
          fine = errore.indexOf("LIST_FIELDS_ERROR=");
          if (inizio > -1) {
            if (fine == -1) {
              fine = errore.length();
              list_field_err = "";
            } else {
              list_field_err = errore.substring(fine+18);
            }
            ads_msg_err = errore.substring(inizio+14,fine);
          } else {
            if (errore.length() == 0) {
              ads_msg_err = "";
              list_field_err = "";
            } else {
              int x = errore.indexOf("?")+1;
              String urlPar = errore.substring(x);
              String urlContr = errore.substring(0,x) + urlParam(urlPar,iddoc,cr);
              corpoHtml += "<html>";
              corpoHtml += "<head><title>ServletVisualizza</title>";
              corpoHtml += "<meta http-equiv='refresh' content='0; URL="+urlContr+"' />";
              corpoHtml += "</head><body>";
              corpoHtml += "</body></html>";
              rimuoviModello(request, mdg);
              //Debug Tempo
              stampaTempo("Modulistica::registraModulo - Fine",ar,cm,cr,ptime);
              //Debug Tempo
              return;
            }
          }
          if (ads_msg_err.length() == 0 && list_field_err.length() == 0) {
            //Cancello tutti i valori presenti nella repositorytemp per quel modello e richiesta
            if (inserisciValori(request)) {
              if (forceRedirect) {
                corpoHtml = "<html>";
                corpoHtml += "<head><title>ServletReindirizza</title>";
                corpoHtml += "<meta http-equiv='refresh' content='0; URL="+gdc_link+"' />";
                corpoHtml += "</head><body>"+campiRedirect(request);
                corpoHtml += "</body></html>";
                httpSess.setAttribute("valori_doc",null);
                httpSess.setAttribute("listaProtetti",null);
                httpSess.setAttribute("listaDomini",null);
                httpSess.setAttribute("listaDominiStandard",null);
                httpSess.setAttribute("gdm_nuovi_valori_doc",null);
                httpSess.setAttribute("gdm_valori_redirect", null);

                rimuoviModello(request, mdg);
                //Debug Tempo
                stampaTempo("Modulistica::registraModulo - Fine",ar,cm,cr,ptime);
                //Debug Tempo
                return;
              }
              iddoc = ricercaIdDocumento(request,null);
              if (cancellaRepository(request, iddoc, false)) {
                  if (fase.equalsIgnoreCase("submitinoltro")) {
                    inoltraModulo(request,(String)request.getSession().getAttribute("UtenteGDM"),mdOut);
                  }
                  if (fase.equalsIgnoreCase("salvataggio")) {
                    costruisciModulo(request, mdg.getQueryURL(),false);
                    //Debug Tempo
                    stampaTempo("Modulistica::registraModulo - Fine",ar,cm,cr,ptime);
                    //Debug Tempo
                    return;
                  }
                  if (fase.equalsIgnoreCase("firma")) {
                  	gdc_link = urlFirma(request);
                  	if (gdc_link.length() == 0 ) {
                        costruisciModulo(request, mdg.getQueryURL(),false);
                        //Debug Tempo
                        stampaTempo("Modulistica::registraModulo - Fine",ar,cm,cr,ptime);
                        //Debug Tempo
                        return;
                  	}
                  }
                  if (gdc_link.length() != 0 && corpoHtml.length() == 0) {
                    corpoHtml += "<html>";
                    corpoHtml += "<head><title>ServletReindirizza</title>";
                    corpoHtml += "<meta http-equiv='refresh' content='0; URL="+gdc_link+"' />";
                    corpoHtml += "</head><body>";
                    corpoHtml += "</body></html>";
                    rimuoviModello(request, mdg);
                  } else {
                    if (mdg.nextMod() && corpoHtml.length() == 0 && modPrec.length() == 0) {
                      corpoHtml += "<html>";
                      corpoHtml += "<head><title>ServletReindirizza</title>";
                      corpoHtml += "<meta http-equiv='refresh' content='0; URL="+urlSucc(request,cm,cr,mdg.getSuccessivo(),mdg.getQueryURL(),"S",iddoc)+"' />";
                      corpoHtml += "</head><body>";
                      corpoHtml += "</body></html>";
                    } else {
                      if (modPrec.length() != 0 && corpoHtml.length() == 0) {
                        corpoHtml += "<html>";
                        corpoHtml += "<head><title>ServletReindirizza</title>";
                        corpoHtml += "<meta http-equiv='refresh' content='0; URL="+urlSucc(request,cm,cr,modPrec,mdg.getQueryURL(),"P",iddoc)+"' />";
                        corpoHtml += "</head><body>";
                        corpoHtml += "</body></html>";
                      } else {
                        request.getSession().setAttribute("modello_precedente",null);
                        request.getSession().setAttribute("valori_modello_precedente",null);
                        mdOut.settaDocumento(id_tipodoc,iddoc,vu);
                        listaAlle = mdg.getAllegati();
                        isW3c = mdg.isW3c();
                        corpoHtml += mdOut.getValue(vu.getDbOp());
                        if (jwf_id.length() != 0) {
                          corpoHtml = modificaJwf(corpoHtml);
                        }
                        rimuoviModello(request, mdg);
                      }
                    }
                  }
//                }
              } else {
                logger.error("ServletModulistica::RegistraModulo() - Area: "+ar+" - Modello: "+cm+"- Richiesta: "+cr+" - Attenzione! Si � verificato un errore in fase di registrazione del modulo.");
              }
            } else {
              costruisciModulo(request, mdg.getQueryURL(),false);
              //Debug Tempo
              stampaTempo("Modulistica::registraModulo - Fine",ar,cm,cr,ptime);
              //Debug Tempo
              return;
//              logger.error("ServletModulistica::RegistraModulo() - Area: "+ar+" - Modello: "+cm+"- Richiesta: "+cr+" - Attenzione! Si � verificato un errore in fase di registrazione del modulo. ");
            }
          } else {
            mdg.settaErrMsg(ads_msg_err);
            mdg.settaListFields(list_field_err);
            costruisciModulo(request, mdg.getQueryURL(),false);
            //Debug Tempo
            stampaTempo("Modulistica::registraModulo - Fine",ar,cm,cr,ptime);
            //Debug Tempo
            return;
          }
        } else {
          //Controlli del lato SERVER del modello
          errore = controlliModello(request, ar, cm, cr, iddoc);
          if (errore == null) {
            errore = "";
          }
          inizio = errore.indexOf("ADS_MSG_ERROR=");
          fine = errore.indexOf("LIST_FIELDS_ERROR=");
          if (inizio > -1) {
            if (fine == -1) {
              fine = errore.length();
              list_field_err = "";
            } else {
              list_field_err = errore.substring(fine+18);
            }
            ads_msg_err = errore.substring(inizio+14,fine);
          } else {
            if (errore.length() == 0) {
              ads_msg_err = "";
              list_field_err = "";
            } else {
              int x = errore.indexOf("?")+1;
              String urlPar = errore.substring(x);
              String urlContr = errore.substring(0,x) + urlParam(urlPar,iddoc,cr);
              corpoHtml += "<html>";
              corpoHtml += "<head><title>ServletVisualizza</title>";
              corpoHtml += "<meta http-equiv='refresh' content='0; URL="+urlContr+"' />";
              corpoHtml += "</head><body>";
              corpoHtml += "</body></html>";
              rimuoviModello(request, mdg);
              //Debug Tempo
              stampaTempo("Modulistica::registraModulo - Fine",ar,cm,cr,ptime);
              //Debug Tempo
              return;
            }
          }
          if (ads_msg_err.length() == 0 && list_field_err.length() == 0) {
            //Aggiorno il documento
            if (aggiornaValori(request, iddoc)) {
              if (forceRedirect) {
                corpoHtml = "<html>";
                corpoHtml += "<head><title>ServletReindirizza</title>";
                corpoHtml += "<meta http-equiv='refresh' content='0; URL="+gdc_link+"' />";
                corpoHtml += "</head><body>"+campiRedirect(request);
                corpoHtml += "</body></html>";
                httpSess.setAttribute("valori_doc",null);
                httpSess.setAttribute("listaDomini",null);
                httpSess.setAttribute("listaProtetti",null);
                httpSess.setAttribute("listaDominiStandard",null);
                httpSess.setAttribute("gdm_nuovi_valori_doc",null);
                httpSess.setAttribute("gdm_valori_redirect", null);

                rimuoviModello(request, mdg);                //Debug Tempo
                stampaTempo("Modulistica::registraModulo - Fine",ar,cm,cr,ptime);
                //Debug Tempo
                return;
              }
              //Cancello tutti i valori presenti nella repositorytemp per quel modello e richiesta
              if (cancellaRepository(request, iddoc, false)) {
                  if (fase.equalsIgnoreCase("submitinoltro")) {
                    inoltraModulo(request,(String)request.getSession().getAttribute("UtenteGDM"),mdOut);
                  }
                  if (fase.equalsIgnoreCase("salvataggio")) {
                    costruisciModulo(request, mdg.getQueryURL(),false);
                    //Debug Tempo
                    stampaTempo("Modulistica::registraModulo - Fine",ar,cm,cr,ptime);
                    //Debug Tempo
                    return;
                  }
                  if (fase.equalsIgnoreCase("firma")) {
                  	gdc_link = urlFirma(request);
                  	if (gdc_link.length() == 0 ) {
                        costruisciModulo(request, mdg.getQueryURL(),false);
                        //Debug Tempo
                        stampaTempo("Modulistica::registraModulo - Fine",ar,cm,cr,ptime);
                        //Debug Tempo
                        return;
                  	}
                  }
                  if (gdc_link.length() != 0 && corpoHtml.length() == 0) {
                    corpoHtml += "<html>";
                    corpoHtml += "<head><title>ServletReindirizza</title>";
                    corpoHtml += "<meta http-equiv='refresh' content='0; URL="+gdc_link+"' />";
                    corpoHtml += "</head><body>";
                    corpoHtml += "</body></html>";
                    rimuoviModello(request, mdg);
                  } else {
                    if (mdg.nextMod() && corpoHtml.length() == 0 && modPrec.length() == 0) {
                      corpoHtml += "<html>";
                      corpoHtml += "<head><title>ServletReindirizza</title>";
                      corpoHtml += "<meta http-equiv='refresh' content='0; URL="+urlSucc(request,cm,cr,mdg.getSuccessivo(),mdg.getQueryURL(),"S",iddoc)+"' />";
                      corpoHtml += "</head><body>";
                      corpoHtml += "</body></html>";
                    } else {
                      if (modPrec.length() != 0 && corpoHtml.length() == 0) {
                        corpoHtml += "<html>";
                        corpoHtml += "<head><title>ServletReindirizza</title>";
                        corpoHtml += "<meta http-equiv='refresh' content='0; URL="+urlSucc(request,cm,cr,modPrec,mdg.getQueryURL(),"P",iddoc)+"' />";
                        corpoHtml += "</head><body>";
                        corpoHtml += "</body></html>";
                      } else {
                        request.getSession().setAttribute("modello_precedente",null);
                        request.getSession().setAttribute("valori_modello_precedente",null);
                        mdOut.settaDocumento(id_tipodoc,iddoc,vu);
                        listaAlle = mdg.getAllegati();
                        isW3c = mdg.isW3c();
                        corpoHtml += mdOut.getValue(vu.getDbOp());
                        if (jwf_id.length() != 0) {
                          corpoHtml = modificaJwf(corpoHtml);
                        }
                        rimuoviModello(request, mdg);
                      }
                    }
                  }
//                }
              } else {
                logger.error("ServletModulistica::RegistraModulo() - Area: "+ar+" - Modello: "+cm+"- Richiesta: "+cr+" - Attenzione! Si � verificato un errore in fase di registrazione del modulo!");
              }
            } else {
              costruisciModulo(request, mdg.getQueryURL(),false);
              //Debug Tempo
              stampaTempo("Modulistica::registraModulo - Fine",ar,cm,cr,ptime);
              //Debug Tempo
              return;
//              logger.error("ServletModulistica::RegistraModulo() - Area: "+ar+" - Modello: "+cm+"- Richiesta: "+cr+" - Attenzione! Si � verificato un errore in fase di registrazione del modulo!");
            }
          } else {
            mdg.settaErrMsg(ads_msg_err);
            mdg.settaListFields(list_field_err);
            costruisciModulo(request, mdg.getQueryURL(),false);
            //Debug Tempo
            stampaTempo("Modulistica::registraModulo - Fine",ar,cm,cr,ptime);
            //Debug Tempo
            return;
          }
        }
      }
      //Debug Tempo
      stampaTempo("Modulistica::registraModulo - Fine",ar,cm,cr,ptime);
      //Debug Tempo

    } catch(Exception e) {
      loggerError("ServletModulistica::RegistraModulo() - Area: "+ar+" - Modello: "+cm+"- Richiesta: "+cr+" - Attenzione! Si � verificato un errore in fase di registrazione del modulo: "+e.toString(),e);
      corpoHtml += "<span class='AFCErrorDataTD'>"+"Attenzione! Si � verificato un errore.<br/>Errore in fase di registrazione del modello.</span>";
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
  private String ricavaIdtipodoc (String ar, String cm) {
    IDbOperationSQL  dbOp = null;
    ResultSet       rst = null;
    String          query;
    String          idtipodoc = null;
    String          codmod = null;

    if (id_tipodoc == null) {
      id_tipodoc = "";
    }
    if (id_tipodoc.length() != 0) {
      return id_tipodoc;
    }
    //Debug Tempo
    long ptime = stampaTempo("Modulistica::ricavaIdtipodoc - Inizio",ar,cm,"",0);
    //Debug Tempo

    query = "SELECT ID_TIPODOC, CODICE_MODELLO_PADRE, TIPO_USO"+
            " FROM MODELLI "+
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
         idtipodoc = rst.getString("ID_TIPODOC");
         codmod = rst.getString("CODICE_MODELLO_PADRE");
         sTipoUso = rst.getString("TIPO_USO");
      } else {
        corpoHtml += "<span class='AFCErrorDataTD'>"+"Errore in ricerca Identificativo tipo documento. Modello non trovato!"+"</span>";
        return "";
      }

      if (idtipodoc == null) {
        idtipodoc = "";
      }

      dbOp.setStatement(query);
      if (idtipodoc.length() == 0) {
        dbOp.setParameter(":AREA",ar);
        dbOp.setParameter(":CM",codmod);
        dbOp.execute();
        rst = dbOp.getRstSet();

        if (rst.next() ) {
           idtipodoc = rst.getString("ID_TIPODOC");
           cm_padre = codmod;
        }
      }
/*      query = "SELECT COMPETENZE_ALLEGATI"+
      				" FROM TIPI_DOCUMENTO T"+
      				" WHERE ID_TIPODOC = :ID_TIPODOC";
      dbOp.setStatement(query);
      dbOp.setParameter(":ID_TIPODOC",idtipodoc);
      dbOp.execute();
      rst = dbOp.getRstSet();

      if (rst.next() ) {
        competenzeAllegati = rst.getString("COMPETENZE_ALLEGATI");
      }*/
      //Debug Tempo
      stampaTempo("Modulistica::ricavaIdtipodoc - Fine",ar,cm,"",ptime);
      //Debug Tempo

      return idtipodoc;

    } catch (Exception e) {
      loggerError("ServletModulistica::ricavaIdtipodoc - "+e.toString(),e);
      corpoHtml += "<span class='AFCErrorDataTD'>"+"Errore in ricerca Identificativo tipo documento"+"</span>";
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        corpoHtml += e.toString();
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        corpoHtml += e.getStackTrace().toString();
      }
      //Debug Tempo
      stampaTempo("Modulistica::ricavaIdtipodoc - Fine",ar,cm,"",ptime);
      //Debug Tempo

      return "";
    }
  }

  /**
   *
   */
  private String urlParam(String p_par, String iddoc, String cr) {
    //Debug Tempo
    long ptime = stampaTempo("Modulistica::urlParam - Inizio","","",cr,0);
    //Debug Tempo
    String ret = "";
    String valore = "";
    String dato = "";

    try {
      int l = p_par.length();
      int s = p_par.indexOf(":");
      int e = l;
      if (s == -1) {
        ret = p_par;
      } else {
        ret = p_par.substring(0,s);
      }
      while (s > -1) {
        e = p_par.indexOf("&",s);
        if (e == -1) {
          e = l;
        }
        dato = p_par.substring(s+1,e);
        if (dato.equalsIgnoreCase("cr")) {
          valore = cr;
        } else {
          if (dato.equalsIgnoreCase("iddoc")) {
            valore = iddoc;
          } else {
            AccediDocumento ad = new AccediDocumento(iddoc,vu);
            ad.accediDocumentoValori();

            valore = ad.leggiValoreCampo(dato);
          }
        }
        ret += valore;
        s = p_par.indexOf(":",e);
        if (s > e) {
          ret += p_par.substring(e,s);
        }
      }
      if (e < l) {
        ret += p_par.substring(e);
      }
    } catch (Exception ex) {
      loggerError("ServletModulistica::urlParam - "+ex.toString(),ex);
      corpoHtml += "<span class='AFCErrorDataTD'>"+"Errore in fase di estrazione dei parametri dall' URL!"+"</span>";
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        corpoHtml += ex.toString();
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        corpoHtml += ex.getStackTrace().toString();
      }
    }
    //Debug Tempo
    stampaTempo("Modulistica::urlParam - Fine","","",cr,ptime);
    //Debug Tempo
    return ret;
  }

//  /**
//   * verificaEsistenzaModello()
//   * Verifica l'esistenza nel database del modello richiesto alla servlet.
//   * Qualora la revisione non venga specificata dall'utente, cerca di recuperare
//   * il modello pi� recente compatibile alla richiesta (ossia quello con la revisione maggiore).
//   * Se non trova alcun modello compatibile genera una eccezione.
//   * @author Nicola
//   * @return: la revisione da utilizzare.
//   **/
//  private void verificaEsistenzaModello(String pArea,
//                                            String pCodiceModello) throws Exception {
//    //Debug Tempo
//    long ptime = stampaTempo("Modulistica::verificaEsistenzaModello - Inizio",pArea,pCodiceModello,"",0);
//    //Debug Tempo
//    IDbOperationSQL  dbOp = null;
//    ResultSet       rst = null;
//    String          query;
//
//    query = "SELECT 1 "+
//            "FROM MODELLI "+
//            "WHERE AREA = :AREA AND "+
//                  "CODICE_MODELLO = :CODICE_MODELLO AND "+
//                  "VALIDO = 'S' ";
//
//    try {
//      dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
//
//      dbOp.setStatement(query);
//      dbOp.setParameter(":AREA",pArea);
//      dbOp.setParameter(":CODICE_MODELLO",pCodiceModello);
//      dbOp.execute();
//      rst = dbOp.getRstSet();
//
//      if (rst.next() ) {
//        // Ho trovato un valore gi� caricato proprio per il modello richiesto, ma devo calcolare
//        // la revisione massima.
//        // SS modificata la select per cui la prima revisione e' la massima per quel modello
//        rst.getString(1);
//      } else {
//        // Modello non disponibile: genero la corrispondente Exception
//        free(dbOp);
//        //Debug Tempo
//        stampaTempo("Modulistica::verificaEsistenzaModello - Fine",pArea,pCodiceModello,"",ptime);
//        //Debug Tempo
//        throw new Exception("Attenzione! Il modello richiesto non � disponibile.");
//      }
//    } catch (Exception sqle) {
//      loggerError("ServletModulistica::verificaEsistenzaModello() - Errore SQL: "+sqle.toString(),sqle);
//      free(dbOp);
//      //Debug Tempo
//      stampaTempo("Modulistica::verificaEsistenzaModello - Fine",pArea,pCodiceModello,"",ptime);
//      //Debug Tempo
//      throw sqle;
//    }
//    free(dbOp);
//    //Debug Tempo
//    stampaTempo("Modulistica::verificaEsistenzaModello - Fine",pArea,pCodiceModello,"",ptime);
//    //Debug Tempo
//  }

  /**
   *  Funziona che ritorna il codice HTML del Modello
   */
  public String getValue() {
    String retval = "";
    int i,j;

    if (isAjax || revisione) {
      return corpoHtml;
    }
    i = corpoHtml.indexOf("<xml>");
    j = corpoHtml.indexOf("</xml>");
    if ((i > -1) && (j > -1)) {
      j = j + 6;
      retval = corpoHtml.substring(0,i);
      retval += corpoHtml.substring(j);
    } else {
      retval = corpoHtml;
    }
    if (pdo.length() == 0) {
      i = corpoHtml.indexOf("!DOCTYPE");
      if (i == -1 && isW3c) {
        retval = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n"+retval;
      }
    }
    if (isW3c) {
      retval = retval.replaceAll("<html", "<html xmlns=\"http://www.w3.org/1999/xhtml\"");
    }
    return retval;
  }


  /**
   *  Funzione che genera il codice HTML del Modello.
   *  Il nome della pagina che includer� il modello che deve
   *  obbligatoriamente chiamarsi ServletModulistica.
   *  Nel caso in cui il modello � incluso in una pagina
   *  sviluppata in CodeCharge occcorre settare il parametro p_do
   *  a CC, se invece � incluso in una servlet settare il parametro p_do
   *  a stringa vuota.
   *  @param request Request della pagina che incorpora il modello
   *  @param p_do Indicatore per CodeCharge
   */
  public void genera(HttpServletRequest  request, String p_do) {
    this.genera(request,p_do,"ServletModulistica");
  }

  public void genera(HttpServletRequest  request, String p_do, String p_nome) {
    String us      = (String)request.getSession().getAttribute("UtenteGDM");

    try {
      initVu(us);
      vu.connect();
      vu.setDbOpRestaConnessa(true); //Questo evita che ogni volta che passo la vu alle classi del dmserver, es. AggiornaDocumento
                                     //la classe rifaccia connect e/o disconnect.... a quello ci penso io
    } catch (Exception e) {
      corpoHtml += "<span class='AFCErrorDataTD'>"+"Attenzione! Errore in fase connessione. Errore:  ["+ e.toString()+"].</span>";
      loggerError("ServletModulistica::genera() Errore in connessione enviroment - Errore:  ["+ e.toString()+"]",e);
      return;
    }

    try {
      this.generaInterna(request,p_do,p_nome);
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
   *  Nel caso in cui il modello � incluso in una pagina
   *  sviluppata in CodeCharge occcorre settare il parametro p_do
   *  a CC o HR, se invece � incluso in una Servlet settare il parametro p_do
   *  a stringa vuota.
   *  @param request Request della pagina che incorpora il modello
   *  @param p_do Indicatore per CodeCharge
   *  @param p_nome Nome della pagina che include il modello. Nel caso in
   *  cui il parametro p_do � settato a HR � possibile settare qui un href.
   */
  public void generaInterna(HttpServletRequest  request, String p_do, String p_nome) {
  	//Inizio blocco di codice per operazioni Ajax
    String us = null;
    sIdDoc = null;


    String elimina = request.getParameter("gdm_fase_erase");
    if (elimina == null) {
      elimina = "";
    }

    if (elimina.equalsIgnoreCase("elimina")) {
      us      = (String)request.getSession().getAttribute("UtenteGDM");
      String eArea = request.getParameter("gdm_erase_area");
      String eCm = request.getParameter("gdm_erase_cm");
      String eCr = request.getParameter("gdm_erase_cr");
      //initVu(eArea, eCm, us);
      id_tipodoc = ricavaIdtipodoc(eArea,eCm);
      try {
        String eraseDoc = (new DocUtil(vu)).getIdDocumento(id_tipodoc,eCr);
        AggiornaDocumento ad = new AggiornaDocumento(eraseDoc, vu);
        ad.settaCodiceRichiesta(eCr);
        ad.salvaDocumentoCancellato();
      } catch (Exception e) {
        loggerError("ServletModulistica::genera() - Area: "+eArea+" - Modello: "+eCm+"- Richiesta: "+eCr+" - Errore:  ["+ e.toString()+"]",e);
        corpoHtml += "<span class='AFCErrorDataTD'>"+"Errore in fase di cancellazione documento!"+"</span>";
      }
    }
    if (request.getParameter("gdm_Ajax") != null) {
//    	if (request.getSession().getCreationTime() == request.getSession().getLastAccessedTime()) {
    	if (request.getSession().getAttribute("UtenteGDM") == null) {
    		isAjax = true;
    		corpoHtml = "Sessione_Scaduta_GDM_Ajax";
    		pdo = "";
    		return;
    	}
      String action = request.getParameter("gdm_ajax_action");
      if (action == null) {
        action = "";
      }
      if (action.length() != 0) {
        isAjax = true;
        us      = (String)request.getSession().getAttribute("UtenteGDM");
        String aArea = request.getParameter("area");
        String aCm = request.getParameter("cm");
        String aCr = request.getParameter("cr");
        //initVu(aArea, aCm, us);
        id_tipodoc = ricavaIdtipodoc(aArea,aCm);
        try {
          String iddoc = ricercaIdDocumento(request,null);
          pdo = p_do;
          modelli = (ArrayList<Modello>)request.getSession().getAttribute("modelli");
          if (modelli == null) {
            // Inizializzo la lista dei modelli a livello di sessione.
            // In questo modo mi garantisco che la servlet restituisca il modello
            // al client da cui viene invocato.
            modelli = new ArrayList<Modello>();
            request.getSession().setAttribute("modelli", modelli);
          }
          mdg = (ModelloHTMLIn)cercaModelloAjax(request.getSession(),aArea,aCm, aCr);
          oldValue(request,action,aArea,aCm,aCr,iddoc);
          corpoHtml += eseguiControllo(request,action,true,null,null,aCr,iddoc, true);
/*          String errore = eseguiControllo(request,action,true,null,null,aCr,iddoc, true);
          if (errore.length() > 0) {
          	erroreAction = errore;
//        	  corpoHtml += corpoHtml.replaceFirst("<!--ERROREACTION-->", "<span class='AFCErrorDataTD'>"+errore+"</span>");
          }*/

        } catch (Exception e) {
          loggerError("ServletModulistica::genera() - Area: "+aArea+" - Modello: "+aCm+"- Richiesta: "+aCr+" - Errore:  ["+ e.toString()+"]",e);
          corpoHtml += "Errore_GDM_Ajax";
        }
        return;
      }

      String barraAllegati = request.getParameter("gdm_ajax_allegati");
      if (barraAllegati == null) {
      	barraAllegati = "";
      }
      if (barraAllegati.length() != 0) {
        isAjax = true;
        us      = (String)request.getSession().getAttribute("UtenteGDM");
        String aArea = request.getParameter("area");
        String aCm = request.getParameter("cm");
        String aCr = request.getParameter("cr");
        String ua = request.getParameter("ua");
        //initVu(aArea, aCm, us);
        id_tipodoc = ricavaIdtipodoc(aArea,aCm);
        lettura = request.getParameter("rw");
        if (lettura == null) {
          lettura = "R";
        }

        if (lettura.equalsIgnoreCase("A")) {
          lettura = "R";
        }



        pdo = p_do;
        if (ua == null) {
        	ua = "";
        }
        if (ua.length() != 0) {
        	try {
	          modelli = (ArrayList<Modello>)request.getSession().getAttribute("modelli");
	          if (modelli == null) {
	            // Inizializzo la lista dei modelli a livello di sessione.
	            // In questo modo mi garantisco che la servlet restituisca il modello
	            // al client da cui viene invocato.
	            modelli = new ArrayList<Modello>();
	            request.getSession().setAttribute("modelli", modelli);
	          }
	          mdg = (ModelloHTMLIn)cercaModelloAjax(request.getSession(),aArea,aCm, aCr);
	          if (mdg != null) {
	          	modelli.remove(mdg);
	          	mdg.setUltimoAgg(ua);
	          	modelli.add(mdg);
	          	request.getSession().setAttribute("modelli", modelli);
	          }
        	} catch (Exception e) {
            loggerError("ServletModulistica::genera() - Area: "+aArea+" - Modello: "+aCm+"- Richiesta: "+aCr+" - Errore:  ["+ e.toString()+"]",e);
//            corpoHtml += "Errore_GDM_Ajax";
        	}
        }
        String iddoc = null;
        try {
        	iddoc = ricercaIdDocumento(request,null);
					compAll = new CompetenzeAllegati(vu, id_tipodoc, iddoc);
				} catch (Exception e) {
					loggerError("ServletModulistica::genera() - Area: "+aArea+" - Modello: "+aCm+"- Richiesta: "+aCr+" - Errore:  ["+ e.toString()+"]",e);
				}
        listaAllegati(request, aArea, aCm, aCr, "open");
        return;
      }

      String ajaxSession = request.getParameter("gdm_ajax_sessione");
      if (ajaxSession == null) {
      	ajaxSession = "";
      }
      if (ajaxSession.length() != 0) {
        String separatore = request.getParameter("gdm_ajax_separatore");
        if (separatore == null) {
        	separatore = "";
        }
        isAjax = true;
        StringTokenizer st = new StringTokenizer(ajaxSession,separatore);
        corpoHtml = "";
        String valore;
        while (st.hasMoreTokens()) {
        	try {
        		if (st.nextToken().equalsIgnoreCase("data_ultimo_agg") ) {
	            String aArea = request.getParameter("area");
	            String aCm = request.getParameter("cm");
	            String aCr = request.getParameter("cr");
	            pdo = p_do;
	            modelli = (ArrayList<Modello>)request.getSession().getAttribute("modelli");
	            if (modelli == null) {
	              // Inizializzo la lista dei modelli a livello di sessione.
	              // In questo modo mi garantisco che la servlet restituisca il modello
	              // al client da cui viene invocato.
	              modelli = new ArrayList<Modello>();
	              request.getSession().setAttribute("modelli", modelli);
	            }
	            mdg = (ModelloHTMLIn)cercaModelloAjax(request.getSession(),aArea,aCm, aCr);
	            if (mdg != null) {
	            	valore = mdg.getUltimoAgg();
	            } else {
	            	valore = null;
	            }
        		} else {
	        		valore = (String)request.getSession().getAttribute(st.nextToken());
        		}
        		if (valore == null) {
        			valore = "";
        		}
        	} catch (Exception e) {
        		valore = "";
        	}
        	corpoHtml += separatore + valore;
        }
        return;
      }


      Ajax aj = new Ajax(iniPath);
      aj.genera(request,p_do, p_nome,vu.getDbOp());
      corpoHtml += aj.getValue();
      isAjax = true;
      return;
    }
    //Fine blocco di codice per operazioni Ajax

    HttpSession     session = request.getSession();
    LinkedList      lModPrec = null;
    String          esiste = "";
    String          contr = null;
    String          reset   = request.getParameter("reset"),
                    fase    = request.getParameter("fase"),
                    reload  = request.getParameter("reload"),
                    stampa  = request.getParameter("STAMPA"),
                    cr      = request.getParameter("cr"),
                    ar      = request.getParameter("area"),
                    cm      = request.getParameter("cm"),
                    visAll  = request.getParameter("visAll"),
                    nominativo = request.getRemoteUser(),
                    cfm     = request.getParameter("cfm");

    logger.info("ServletModulistica::genera() INFORMAZIONE INIZIALE SU APERTURA MODELLO - Area: "+ar+" | Modello: "+cm+" | Richiesta: "+cr+" | nominativo: "+nominativo);

    //Protezione XSS
    if (Parametri.CODIFICA_XSS.equalsIgnoreCase("S")) {
    	try {
      	verificaParametroGet("reset",reset);
      	verificaParametroGet("fase",fase);
      	verificaParametroGet("reload",reload);
      	verificaParametroGet("stampa",stampa);
      	verificaParametroGet("cr",cr);
      	verificaParametroGet("ar",ar);
      	verificaParametroGet("cm",cm);
      	verificaParametroGet("visAll",visAll);
      	verificaParametroGet("cfm",cfm);
    	} catch (Exception e) {
    		pdo = p_do;
        corpoHtml = "<hr/><h2>Attenzione!</h2><hr/><h4>"+e.getMessage()+"</h4>";
        logger.error("Modulistica:doGet - Problema parametri in URL. Query: "+request.getQueryString());
        return;
    	}
    }

    if (disabilitaReload) {
    	reload = null;
    }
    if (ar == null || ar.length() == 0 || ar.equalsIgnoreCase("null")) {
      corpoHtml = "<hr/><h2>Attenzione!</h2><hr/><h4>Occorre specificare un Area!</h4>";
      logger.error("Modulistica:doGet - Area non specificata. Query: "+request.getQueryString());
      return;
    }
    if (cm == null || cm.length() == 0 || cm.equalsIgnoreCase("null")) {
      corpoHtml = "<hr/><h2>Attenzione!</h2><hr/><h4>Occorre specificare un Codice Modello!</h4>";
      logger.error("Modulistica:doGet - Codice Modello non specificato. Query: "+request.getQueryString());
      return;
    }
    if (cr == null) {
      cr = "";
    }
    //Debug Tempo
    long ptime = stampaTempo2("Modulistica::genera - Inizio",ar,cm,cr,0);
    //Debug Tempo

    modelli = (ArrayList<Modello>)request.getSession().getAttribute("modelli");
    if (modelli == null) {
      // Inizializzo la lista dei modelli a livello di sessione.
      // In questo modo mi garantisco che la servlet restituisca il modello
      // al client da cui viene invocato.
      modelli = new ArrayList<Modello>();
      request.getSession().setAttribute("modelli", modelli);
    }
    id_tipodoc = ricavaIdtipodoc(ar,cm);
    if (cr.length() == 0) {   // Se necessario creo un codice richiesta
      cr = calcolaNumeroRichiesta(request.getSession(), ar);
      session.setAttribute("key",cr);
    }

    if (nominativo == null) {
      us      = (String)session.getAttribute("Utente");
    } else {
      us = cercaUtente(nominativo.toUpperCase());
    }

    if (us == null) {
      us = encode(request.getParameter("us"),"S");
      if (us == null) {
      	us = "";
      } else {
      	us = Cryptable.decryptPasswd(us);
      }
    }
    if (us.length() == 0) {
      us = creaUtente(request.getSession());
    }
    ruolo = caricaRuolo(us);

    session.setAttribute("UtenteGDM",us);
    session.setAttribute("RuoloGDM",ruolo);

    pdo = p_do;

    //Controllo se devo aprire una versione precedente del documento
    String sVer = request.getParameter("docVer");
    if ( sVer != null && !sVer.equalsIgnoreCase("")) {
    	//Apro la il documento in versione X
    	int versione_documento = Integer.parseInt(request.getParameter("docVer"));
    	try {
    		revisione = true;
    		vu.setUser(us);
            //initVu(ar, cm, us);
            id_tipodoc = ricavaIdtipodoc(ar,cm);
	    	String iddocumento = ricercaIdDocumento(request,null);
	    	if (iddocumento != null ) {
		      UtenteAbilitazione ua = new UtenteAbilitazione(vu.getUser(), vu.getGruppo(), vu.getRuolo(), vu.getPwd(),  vu.getUser());
		      Abilitazioni ab = new Abilitazioni("DOCUMENTI", iddocumento, "L");
		      if ((new GDM_Competenze(vu)).verifica_GDM_Compentenza(ua,ab) == 0) {
		      	corpoHtml += errorLogin(ar, cm, cr, us);
		        return;
		      } else {
		      	corpoHtml = visualizzaVersione(request,ar, cm, iddocumento, versione_documento, us);
		        return;
		      }
	    	} else {
	        loggerError("ServletModulistica::genera() - Attenzione! Documento non trovato",null);
	        corpoHtml += "<span class='AFCErrorDataTD'>"+"Attenzione! Documento non trovato.</span>";
	        return;
	    	}
    	} catch (Exception e) {
        loggerError("ServletModulistica::genera() - Attenzione! Errore in fase visualizzazione revisione: "+e.toString(),e);
        corpoHtml += "<span class='AFCErrorDataTD'>"+"Attenzione! Errore in fase visualizzazione revisione.</span>";
        return;
    	}
    }

    timeFirstOpen = request.getParameter("gdm_first_open");

    session.setAttribute("GDM_NEW_DOC", null);
    session.setAttribute("valori_doc",null);
    session.setAttribute("listaDomini",null);
    session.setAttribute("listaProtetti",null);
    session.setAttribute("listaControlli",null);
    session.setAttribute("listaControlliMod",null);
    session.setAttribute("listaDominiStandard",null);
    session.setAttribute("gdm_nuovi_valori_doc",null);
    session.setAttribute("gdm_valori_redirect", null);
    session.setAttribute("GDM_CONFIG_FILE",inifile.replaceAll("\\\\","/"));

    if (cfm == null) {
      cfm = "";
    }
    if (reload == null) {
      reload = request.getParameter("Aggiorna");
      if (reload != null) {
        reload = "1";
      } else {
        reload = request.getParameter("urlCut");
        if (reload != null) {
          if (reload.equalsIgnoreCase("BN")) {
            reload = null;
          } else {
            reload = "1";
          }
        }
      }
    }

    if (request.getParameter("RegistraInoltro") != null) {
      fase = "submitinoltro";
    }

    if (request.getParameter("Salva") != null) {
      fase = "salvataggio";
    }

    if (request.getParameter("Sblocca") != null) {
      fase = "sblocca";
      reload = "1";
    }

    if (request.getParameter("Blocca") != null) {
    	String liv = (String)request.getParameter("_GDM_LIV_CHECK");
    	newlivellocheck = Integer.parseInt(liv);
      fase = "blocca";
      reload = "1";
    }

    jwf_id    = request.getParameter("_JWKF_id_attivita");
    jwf_back  = request.getParameter("_JWKF_backservlet");
    if (jwf_id == null) {
      jwf_id = "";
    }
    if (jwf_back == null) {
      jwf_back = "";
    }
    if (visAll == null) {
      visAll = "Y";
    }
    gdc_link = request.getParameter("GDC_Link");
    if (gdc_link == null) {
      gdc_link = "";
    }
    lettura = request.getParameter("rw");
    if (lettura == null) {
      lettura = "R";
    }

    if (lettura.equalsIgnoreCase("V")) {
    	visAll = "N";
    }
    if (lettura.equalsIgnoreCase("A")) {
      lettura = "R";
    }

    id_session = cr;
    try {
    	cercaRichiesta(ar,id_session);
    } catch (Exception e1) {
    	return;
    }

    if (request.getParameter("FirmaSTD") != null) {
    	fase = "firma";
    }

    if (stampa != null) {
      gdc_link = request.getRequestURL()+"?area="+ar+"&amp;cm="+cm+"&amp;rw=P&amp;cr="+cr;
      if (lettura.equalsIgnoreCase("R") || lettura.equalsIgnoreCase("V")) {
        corpoHtml += "<html>";
        corpoHtml += "<head><title>ServletReindirizza</title>";
        corpoHtml += "<meta http-equiv='refresh' content='0; URL="+gdc_link+"' />";
        corpoHtml += "</head><body>";
        corpoHtml += "</body></html>";
        return;
      }
    }

    String wPadre = request.getParameter("wfather");
    if (wPadre == null) {
      wPadre = "";
    }
    String nomeserv_old = "";
    if (wPadre.length() != 0) {
      nomeserv_old = (String)session.getAttribute("p_nomeservlet");
      if (nomeserv_old == null) {
        nomeserv_old = "";
      }
      if (!nomeserv_old.equalsIgnoreCase(p_nome)) {
        session.setAttribute("p_nomeservlet_padre",nomeserv_old);
      }
    } else {
      session.setAttribute("p_nomeservlet_padre",null);
    }
    if (p_nome == null) {
      session.setAttribute("p_nomeservlet","ServletModulistica");
    } else {
      session.setAttribute("p_nomeservlet",p_nome);
    }

    String ente = (String)session.getAttribute("Ente");
    if (ente == null) {
      ente = "";
    }
    Personalizzazioni pers = null;
    pers = (Personalizzazioni)session.getAttribute("_personalizzazioni_gdm");
    if (pers == null) {
      try {
        pers = new Personalizzazioni(ente, us,vu);
      } catch (Exception e) {

      }
      session.setAttribute("_personalizzazioni_gdm",pers);
    }

    if (lettura.equalsIgnoreCase("P")) {
      contr = "Y";
      lettura = "R";
    } else {
      contr = "N";
    }

    Object attributo = request.getSession().getAttribute("modello_precedente");
    if (attributo != null) {
      lModPrec = (LinkedList)attributo;
    } else {
      lModPrec = new LinkedList();
    }
    String sSuc_Prec = null;
    if (!lettura.equalsIgnoreCase("W")) {
      sSuc_Prec = request.getParameter("Successivo");
      if (sSuc_Prec == null) {
        sSuc_Prec = request.getParameter("Precedente");
        if (sSuc_Prec == null) {
          sSuc_Prec = request.getParameter("prec_succ");
          if (sSuc_Prec == null) {
            sSuc_Prec = "";
          } else {
            sSuc_Prec = "PASSO2";
          }
        } else {
          sSuc_Prec = "P";
        }
      } else {
        sSuc_Prec = "S";
      }
      if (sSuc_Prec.length() == 0) {
        modPrec = "";
        session.setAttribute("modello_precedente",null);
        session.setAttribute("valori_modello_precedente",null);
      } else {
        String myQuery = request.getParameter("myQuery");
        if (sSuc_Prec.equalsIgnoreCase("S")) {
            String modSucc = request.getParameter("mod_seguente");
            corpoHtml = "<html>";
            corpoHtml += "<head><title>ServletReindirizza</title>";
            corpoHtml += "<meta http-equiv='refresh' content='0; URL="+urlSucc(request,cm,cr,modSucc,myQuery,"S",null)+"' />";
            corpoHtml += "</head><body>";
            corpoHtml += "</body></html>";
            return;
        }
        if (sSuc_Prec.equalsIgnoreCase("P")) {
          modPrec = request.getParameter("mod_precedente");
          corpoHtml += "<html>";
          corpoHtml += "<head><title>ServletReindirizza</title>";
          corpoHtml += "<meta http-equiv='refresh' content='0; URL="+urlSucc(request,cm,cr,modPrec,myQuery,"P",null)+"' />";
          corpoHtml += "</head><body>";
          corpoHtml += "</body></html>";
          return;
        }
      }
    } else {
      modPrec = "";
      sSuc_Prec = request.getParameter("Successivo");
      if (sSuc_Prec == null) {
        sSuc_Prec = request.getParameter("Precedente");
        if (sSuc_Prec == null) {
          sSuc_Prec = request.getParameter("prec_succ");
          if (sSuc_Prec == null) {
            sSuc_Prec = "";
          }
        } else {
          if (attributo != null) {
            modPrec = (String)lModPrec.getLast();
          } else {
            modPrec = "";
          }
        }
      }
      if (sSuc_Prec.length() == 0 && reload == null) {
        session.setAttribute("modello_precedente",null);
        session.setAttribute("valori_modello_precedente",null);
      }
    }

    myPathTemp = request.getSession().getServletContext().getRealPath("")+File.separator+"temp"+File.separator;
    myPathTemp += ar + File.separator + cm;

    if (reset != null) {        // Si vuole fare un RESET
      try {
        Parametri.leggiParametriStandard(inifile);
        corpoHtml += "<hr/><h2>Reset Modulistica</h2><hr/><h4>Ricaricati i parametri standard.</h4>";
      } catch(Exception e) {
        corpoHtml += "<hr/><h2>Attenzione!</h2><hr/><h4>Impossibile ricaricare i parametri standard.</h4>";
      }
      //Debug Tempo
      stampaTempo2("Modulistica::genera - Fine",ar,cm,cr,ptime);
      //Debug Tempo
      return;
    }

    //initVu(ar, cm, us);
    vu.setUser(us);
    id_tipodoc = ricavaIdtipodoc(ar,cm);

    try {

      esiste = (new DocUtil(vu)).getIdDocumento(id_tipodoc,cr);
      if (lettura.equalsIgnoreCase("V") && esiste.equalsIgnoreCase("")) {
        loggerError("ServletModulistica::genera() - Attenzione! Impossibile versionare. Errore in fase di verifica esistenza documento",null);
        corpoHtml = "Errore_GDM_Ajax";
        return;
      }
      session.setAttribute("esiste_documento",esiste);

    } catch (Exception e) {
      loggerError("ServletModulistica::doGet() - Attenzione! Errore in fase di verifica esistenza documento: "+e.toString(),e);
      corpoHtml += "<span class='AFCErrorDataTD'>"+"Attenzione! Errore interno in fase di verifica esistenza documento.</span>";
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        corpoHtml += e.toString();
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        corpoHtml += e.getStackTrace().toString();
      }
      //Debug Tempo
      stampaTempo2("Modulistica::genera - Fine",ar,cm,cr,ptime);
      //Debug Tempo
      return;
    }


    boolean primo_caricamento = false;
    if (timeFirstOpen == null) {
      primo_caricamento = true;
      timeFirstOpen = ""+Calendar.getInstance().getTimeInMillis();
    }

    if (Parametri.ALLEGATI_AUTO_SAVE.equalsIgnoreCase("S")) {
    	aggData = request.getParameter("allegato");
      if (aggData == null) {
        aggData = "";
      } else {
        try {
	      	mdg = (ModelloHTMLIn)cercaModello(session, ar, cm, cr);
	      	modelli.remove(mdg);
	      	mdg.setUltimoAgg(aggData);
	      	modelli.add(mdg);
	      	session.setAttribute("modelli",modelli);
        } catch (Exception e) {
          loggerError("ServletModulistica::genera() - Area: "+ar+" - Modello: "+cm+"- Richiesta: "+cr+" - Attenzione! Errore in fase di ricerca modello in memoria: "+e.toString(),e);
          corpoHtml += "<span class='AFCErrorDataTD'>"+"Attenzione! Errore interno in fase di ricerca modello in memoria.</span>";
          if (Parametri.DEBUG.equalsIgnoreCase("1")) {
            corpoHtml += e.toString();
          }
          if (Parametri.DEBUG.equalsIgnoreCase("2")) {
            corpoHtml += e.getStackTrace().toString();
          }
          //Debug Tempo
          stampaTempo2("Modulistica::genera - Fine",ar,cm,cr,ptime);
          //Debug Tempo
          return;
        }
      }
    } else {
      aggData = "";
    }

    try {
      preCaricamentoDati(request,ar,cr,cm,primo_caricamento);
      session.setAttribute("pdo",pdo);
    } catch(Exception e) {
      loggerError("ServletModulistica::genera() - Area: "+ar+" - Modello: "+cm+"- Richiesta: "+cr+" - Attenzione! Si � verificato un errore: "+e.toString(),e);
      corpoHtml += "<span class='AFCErrorDataTD'>"+"Attenzione! Si � verificato un errore.<br/>Errore in fase di precaricamento dati.</span>";
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        corpoHtml += e.toString();
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        corpoHtml += e.getStackTrace().toString();
      }
    }

    if (reload == null && (fase == null || !fase.equalsIgnoreCase("inoltro") && !fase.equalsIgnoreCase("blocca") && !fase.equalsIgnoreCase("sblocca"))) {
      reload = pulsantePremuto(request,ar,cm,fase,cr,lettura);
      if (forceRedirect) {
        corpoHtml = "<html>";
        corpoHtml += "<head><title>ServletReindirizza</title>";
        corpoHtml += "<meta http-equiv='refresh' content='0; URL="+gdc_link+"' />";
        corpoHtml += "</head><body>"+campiRedirect(request);
        corpoHtml += "</body></html>";
        session.setAttribute("GDM_NEW_DOC", null);
        session.setAttribute("valori_doc",null);
        session.setAttribute("listaProtetti",null);
        session.setAttribute("listaDomini",null);
        session.setAttribute("listaDominiStandard",null);
        session.setAttribute("gdm_nuovi_valori_doc",null);
        session.setAttribute("gdm_valori_redirect", null);

        try {
          mdg = (ModelloHTMLIn)cercaModello(session, ar, cm, cr);
          if (mdg != null) {
            modelli.remove(mdg);
            session.setAttribute("modelli",modelli);
          }
        } catch (Exception e) {
            loggerError("ServletModulistica::doGet() - Area: "+ar+" - Modello: "+cm+"- Richiesta: "+cr+" - Attenzione! Errore in fase di redirect: "+e.toString(),e);
            corpoHtml += "<span class='AFCErrorDataTD'>"+"Attenzione! Errore interno in fase di redirect.</span>";
            if (Parametri.DEBUG.equalsIgnoreCase("1")) {
              corpoHtml += e.toString();
            }
            if (Parametri.DEBUG.equalsIgnoreCase("2")) {
              corpoHtml += e.getStackTrace().toString();
            }

            //Debug Tempo
            stampaTempo2("Modulistica::genera - Fine",ar,cm,cr,ptime);
            //Debug Tempo
            return;
        }
        return;
      }
      if (disabilitaReload) {
      	reload = null;
      }

      if (reload == null) {
        reload = "";
      }
      if (!reload.equalsIgnoreCase("1")) {
        fase = reload;
        reload = null;
      }
    }

    // Determino la fase (SUBMIT o OPEN). Dipende dalla richiesta.
    if (fase == null ) {
      fase = "open";
    } else {
      if (fase.length() == 0) {
        fase = "open";
      }
    }

    String  queryURL = null;

    try {
      if (reload != null && !fase.equalsIgnoreCase("inoltro") && !fase.equalsIgnoreCase("blocca") && !fase.equalsIgnoreCase("sblocca")) {
        mdg = (ModelloHTMLIn)cercaModello(session, ar, cm, cr);
        if (mdg == null) {
          logger.error("ServletModulistica::genera() - Area: "+ar+" - Modello: "+cm+"- Richiesta: "+cr+" - Attenzione! Il modello non � pi� presente in memoria");
          corpoHtml += "<span class='AFCErrorDataTD'>"+"Attenzione! Si � verificato un errore.<br/>Il modello non � pi� presente in memoria.</span>";
          //Debug Tempo
          stampaTempo("Modulistica::genera - Fine",ar,cm,cr,ptime);
          //Debug Tempo
          return;
        }
/*      	String errMsg = "";
      	CheckDocumento chk = mdg.getChk();
				livellocheck = chk.verificaCheck(us);
				errMsg = chk.getErrorMessage();
				if (livellocheck == 1) {
					corpoHtml = visualizzaMessagio(errMsg);
          modelli.remove(mdg);
          session.setAttribute("modelli",modelli);
          return;
				}
        mdg.setChk(chk);
				if (livellocheck == 2 & lettura.equalsIgnoreCase("W")) {
					lettura = "R";
				}
        if (fase.equalsIgnoreCase("checkout")) {
	        try {
	        	chk.checkOut();
	        	chk.commit();
	        	mdg.setChk(chk);
	        } catch (Exception echk) {
	        	mdg.settaErrMsg("Errore in fase di Check Out");
	          loggerError("ServletModulistica::genera() - Area: "+ar+" - Modello: "+cm+"- Richiesta: "+cr+" - Attenzione! Errore in fase di Check Out: "+echk.toString(),echk);
	        }
        }
        if (fase.equalsIgnoreCase("checkin")) {
	        try {
	        	chk.checkIn(us, livellocheck);
	        	chk.commit();
	        	mdg.setChk(chk);
	        } catch (Exception echk) {
	        	mdg.settaErrMsg("Errore in fase di Check In");
	          loggerError("ServletModulistica::genera() - Area: "+ar+" - Modello: "+cm+"- Richiesta: "+cr+" - Attenzione! Errore in fase di Check In: "+echk.toString(),echk);
	        }
        }*/
        settaDataAggiornamentoModello(request, aggData);
/*        modelli.remove(mdg);
        if (aggData != null && aggData.length()> 0) {
        	mdg.setUltimoAgg(aggData);
        }
        modelli.add(mdg);
        request.getSession().setAttribute("modelli",modelli);
        aggData = "";*/
        queryURL = mdg.getQueryURL();
//        registraModulo(request, true, "open");
        fase = "open";
      }
//      if (fase.equalsIgnoreCase("salvataggio")) {
//        queryURL = mdg.getQueryURL();
//      }
      if (mdg == null) {
      	mdg = (ModelloHTMLIn)cercaModello(session, ar, cm, cr);
      }
      if ( mdg != null) {
	    	String errMsg = "";
	    	CheckDocumento chk = mdg.getChk();
	    	chk.leggiCheck(vu.getDbOp());
				livellocheck = chk.verificaCheck(us);
				errMsg = chk.getErrorMessage();
				if (livellocheck == 1) {
					corpoHtml = visualizzaMessagio(errMsg);
	        modelli.remove(mdg);
	        session.setAttribute("modelli",modelli);
	        return;
				}
	      mdg.setChk(chk);
				if (livellocheck == 2 & lettura.equalsIgnoreCase("W")) {
					lettura = "R";
					fase = "open";
				}
	      if (fase.equalsIgnoreCase("sblocca")) {
	        try {
	        	chk.checkIn(vu.getDbOp());
	        	vu.getDbOp().commit();
	        	mdg.setChk(chk);
	        	fase = "open";
	        } catch (Exception echk) {
	        	mdg.settaErrMsg("Errore in fase di Sblocco");
	        	fase = "open";
	          loggerError("ServletModulistica::genera() - Area: "+ar+" - Modello: "+cm+"- Richiesta: "+cr+" - Attenzione! Errore in fase di Check Out: "+echk.toString(),echk);
	        }
	      }
	      if (fase.equalsIgnoreCase("blocca")) {
	        try {
	        	if (chk.checkOut(us, newlivellocheck,vu.getDbOp()) == -1) {
	        		mdg.settaErrMsg("Errore in fase di Blocco. "+chk.getErrorMessage());
	        	} else {
                    vu.getDbOp().commit();
	        		mdg.setChk(chk);
	        	}
	        	fase = "open";
	        } catch (Exception echk) {
	        	mdg.settaErrMsg("Errore in fase di Blocco. "+chk.getErrorMessage());
	        	fase = "open";
	          loggerError("ServletModulistica::genera() - Area: "+ar+" - Modello: "+cm+"- Richiesta: "+cr+" - Attenzione! Errore in fase di Check In: "+echk.toString(),echk);
	        }
	      }
	      modelli.remove(mdg);
	      modelli.add(mdg);
	      request.getSession().setAttribute("modelli",modelli);
      }

    } catch (Exception e) {
      loggerError("ServletModulistica::doGet() - Area: "+ar+" - Modello: "+cm+"- Richiesta: "+cr+" - Attenzione! Errore in fase di reload: "+e.toString(),e);
      corpoHtml += "<span class='AFCErrorDataTD'>"+"Attenzione! Errore interno in fase di reload.</span>";
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        corpoHtml += e.toString();
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        corpoHtml += e.getStackTrace().toString();
      }
      //Debug Tempo
      stampaTempo2("Modulistica::genera - Fine",ar,cm,cr,ptime);
      //Debug Tempo
      return;
    }

    // ---------------------------------------
    // Creazione modello in funzione della fase
    // ---------------------------------------
    if (fase.equalsIgnoreCase("open") || fase.equalsIgnoreCase("inoltro") ) {
      String iddoc = null;
			try {
				iddoc = ricercaIdDocumento(request,null);
			} catch (Exception e1) {
				String errmsg = "Errore in fase recupero id documento. ";
	      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
	      	errmsg += e1.getMessage();
	      }
	      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
	      	errmsg += e1.getStackTrace().toString();
	      }
	      corpoHtml = visualizzaMessagio(errmsg);
        return;
			}
      //Controllo se il documento � in check In
      if (loginModulistica(ar, cm, cr, us, contr, iddoc, esiste) == true) {
        if ((iddoc != null) &&  (reload == null)) {
          leggiValori(request,iddoc,false);
        }

        try {
        	compAll = new CompetenzeAllegati(vu, id_tipodoc, iddoc);
          preCaricamentoDati(request,ar,cr,cm,primo_caricamento);
          if (lettura.equalsIgnoreCase("Q")) {
            mdg = (ModelloHTMLIn)cercaModello(session, ar, cm, cr);
//            mdg.setNewRequest(request);
          }
          /*Connection conn=null;
			dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
	conn = dbOp.getConn();*/
          	CheckDocumento chk = new CheckDocumento(iddoc,new JNDIParameter(Parametri.JINDIDBNAME),false);
            chk.leggiCheck(vu.getDbOp());
          if (mdg==null) {
          	String errMsg = "";
            if ((iddoc != null) &&  (reload == null)) {
  						livellocheck = chk.verificaCheck(us);
  						errMsg = chk.getErrorMessage();
            }
						if (livellocheck == 1) {
							corpoHtml = visualizzaMessagio(errMsg);
	            return;
						}
            mdg = new ModelloHTMLIn(request, ar, cm, cr, calcolaScadenza("1"),timeFirstOpen,vu.getDbOp());
            mdg.setChk(chk);
            mdg.setCompAll(compAll);
						if (livellocheck == 2 & lettura.equalsIgnoreCase("W")) {
							lettura = "R";
						}
//						if (livellocheck == 3) {
//							mdg.setMessaggioGDM(errMsg);
//						}
            mdg.setUltimoAgg(aggData);
            mdg.interpretaModello(vu.getDbOp());
            modelli.add(mdg);
            session.setAttribute("modelli",modelli);
          } else {
						livellocheck = chk.verificaCheck(us);
						if (livellocheck == 1) {
							corpoHtml = visualizzaMessagio(chk.getErrorMessage());
              modelli.remove(mdg);
              session.setAttribute("modelli",modelli);
	            return;
						}
            mdg.setChk(chk);
            mdg.setCompAll(compAll);
            mdg.setNewRequest(request);
						if (livellocheck == 2 & lettura.equalsIgnoreCase("W")) {
							lettura = "R";
						}
          }
        } catch(Exception e) {
          loggerError("ServletModulistica::genera() - Area: "+ar+" - Modello: "+cm+"- Richiesta: "+cr+" - Attenzione! Si � verificato un errore: "+e.toString(),e);
          corpoHtml += "<span class='AFCErrorDataTD'>"+"Attenzione! Si � verificato un errore.<br/>Errore in fase di precaricamento dati.</span>";
          if (Parametri.DEBUG.equalsIgnoreCase("1")) {
            corpoHtml += e.toString();
          }
          if (Parametri.DEBUG.equalsIgnoreCase("2")) {
            corpoHtml += e.getStackTrace().toString();
          }
        }


        if (cfm.length() == 0) {
          //Eseguo la Formula modulo per vedere se mi devo reindirizzare ad un altro modello
          try {
            FormulaModello fm = new FormulaModello(request, ar, cm, vu.getDbOp());
            String fMod = fm.getCorpo();
            if (fMod == null) {
              fMod = "";
            }
            if (fMod.length() != 0) {
              String formula = "";
              ControlliParser cp = new ControlliParser(request,iddoc,cr,null,true,stato_doc, isAjax);
              formula = cp.bindingDeiParametri(fMod);
//              String formula = bindingDinamico(request, fMod, iddoc, cr, null, true);
              fm.settaCorpo(formula);
              String newUrl = fm.nuovoUrl();
              if (newUrl.length() != 0 || !newUrl.equals(cm)) {
                corpoHtml = "<html>";
                corpoHtml += "<head><title>ServletReindirizza</title>";
                corpoHtml += "<meta http-equiv='refresh' content='0; URL="+request.getRequestURL()+"?"+newUrl+"&amp;cfm=N' />";
                corpoHtml += "</head><body>";
                corpoHtml += "</body></html>";
                freeConn();
                modelli.remove(mdg);
                session.setAttribute("modelli",modelli);
                return;
              }
            }
          } catch (Exception e) {
            loggerError("ServletModulistica::doGet() - Area: "+ar+" - Modello: "+cm+"- Richiesta: "+cr+" - Attenzione! Errore in formula modello: "+e.toString(),e);
            corpoHtml += "<span class='AFCErrorDataTD'>"+"Attenzione! Errore interno in fase di calcolo Formula Modello.</span>";
            if (Parametri.DEBUG.equalsIgnoreCase("1")) {
              corpoHtml += e.toString();
            }
            if (Parametri.DEBUG.equalsIgnoreCase("2")) {
              corpoHtml += e.getStackTrace().toString();
            }
            session.setAttribute("GDM_NEW_DOC", null);
            session.setAttribute("valori_doc",null);
            session.setAttribute("listaDomini",null);
            session.setAttribute("listaProtetti",null);
            session.setAttribute("listaDominiStandard",null);
            session.setAttribute("gdm_nuovi_valori_doc",null);
            session.setAttribute("gdm_valori_redirect", null);
            modelli.remove(mdg);
            session.setAttribute("modelli",modelli);
            return;
          }
        }

        if (request.getParameter("rw").equalsIgnoreCase("A")) {
          String sysPdf;
          if (iddoc != null) {
            sysPdf = sysPdf(iddoc,ar,cm);
            if (sysPdf.length() != 0) {
              corpoHtml = "IDDOC="+iddoc+"SYS_PDF="+sysPdf;
              session.setAttribute("GDM_NEW_DOC", null);
              session.setAttribute("valori_doc",null);
              session.setAttribute("listaDomini",null);
              session.setAttribute("listaProtetti",null);
              session.setAttribute("listaDominiStandard",null);
              session.setAttribute("gdm_nuovi_valori_doc",null);
              session.setAttribute("gdm_valori_redirect", null);
              freeConn();
              return;
            }
          }
        }
        if (lettura.equalsIgnoreCase("Q")) {
          costruisciModulo(request, queryURL, primo_caricamento);  // Passo la queryString, utile in caso di reload.
        }
        if (lettura.equalsIgnoreCase("W")) {
          costruisciModulo(request, queryURL, primo_caricamento);  // Passo la queryString, utile in caso di reload.
        }
        if (lettura.equalsIgnoreCase("R") || lettura.equalsIgnoreCase("C") || lettura.equalsIgnoreCase("S") || lettura.equalsIgnoreCase("V")){
          if (fase.equalsIgnoreCase("inoltro")) {
            inoltraModulo(request, us, null);
          } else {
            costruisciModuloLettura(request, queryURL);  // Passo la queryString, utile in caso di reload.
          }
        }
        if (lettura.equalsIgnoreCase("V")) {
          //Memorizzo la versione
          IDbOperationSQL dbOpVer = null;
          try {
          	dbOpVer = Parametri.creaDbOp();
            int newVersion = calcolaNumeroVersione(iddoc, dbOpVer);
            memorizzaVersione(ar,cm,iddoc, newVersion, us, dbOpVer);
            dbOpVer.commit();
            corpoHtml = ""+newVersion;
          } catch (Exception ever) {
          	try {
          		dbOpVer.rollback();
          	} catch (Exception eroll) {}
            loggerError("ServletModulistica::genera() - Area: "+ar+" - Modello: "+cm+"- Richiesta: "+cr+" - Attenzione! Errore in fase di versioning: "+ever.toString(),ever);
            corpoHtml = "Errore_GDM_Ajax";
          }
          finally {
            free(dbOpVer);
          }

        }
      } else {
        logger.error("ServletModulistica::doGet() - Area: "+ar+" - Modello: "+cm+"- Richiesta: "+cr+" - loginSportello fallita ");
        if (lettura.equalsIgnoreCase("V")) {
        	corpoHtml = "Errore_GDM_Ajax";
        }
        //Debug Tempo
        stampaTempo2("Modulistica::genera - Fine",ar,cm,cr,ptime);
        //Debug Tempo
        return;
      }
    } else {
      try {
        mdg = (ModelloHTMLIn)cercaModello(session, ar, cm, cr);
        if (mdg == null) {
          logger.error("ServletModulistica::genera() - Area: "+ar+" - Modello: "+cm+"- Richiesta: "+cr+" - Attenzione! Il modello non � pi� presente in memoria");
          corpoHtml += "<span class='AFCErrorDataTD'>"+"Attenzione! Si � verificato un errore.<br/>Il modello non � pi� presente in memoria.</span>";
          //Debug Tempo
          stampaTempo2("Modulistica::genera - Fine",ar,cm,cr,ptime);
          //Debug Tempo
          return;
        }
        compAll = mdg.getCompAll();
      } catch (Exception e) {
        loggerError("ServletModulistica::doGet() - Area: "+ar+" - Modello: "+cm+"- Richiesta: "+cr+" - Attenzione! Errore in fase di reload: "+e.toString(),e);
        corpoHtml += "<span class='AFCErrorDataTD'>"+"Attenzione! Errore interno in fase di reload.</span>";
        if (Parametri.DEBUG.equalsIgnoreCase("1")) {
          corpoHtml += e.toString();
        }
        if (Parametri.DEBUG.equalsIgnoreCase("2")) {
          corpoHtml += e.getStackTrace().toString();
        }
        //Debug Tempo
        stampaTempo2("Modulistica::genera - Fine",ar,cm,cr,ptime);
        //Debug Tempo
        return;
      }
      if (lettura.equalsIgnoreCase("Q")) {
        effettuaRicerca(request);
      } else {
        if (fase.equalsIgnoreCase("inoltro")) {
          inoltraModulo(request, us, null);
        } else {
          registraModulo(request, false, fase);
        }
      }
    }
    if (visAll.equalsIgnoreCase("Y") && listaAlle.equalsIgnoreCase("Y")) {
      listaAllegati(request,ar,cm,cr,fase);
    }

    session.setAttribute("GDM_NEW_DOC", null);
    session.setAttribute("valori_doc",null);
    session.setAttribute("listaDomini",null);
    session.setAttribute("listaDominiStandard",null);
    session.setAttribute("gdm_nuovi_valori_doc",null);
    session.setAttribute("gdm_valori_redirect", null);
    //Debug Tempo
    stampaTempo2("Modulistica::genera - Fine",ar,cm,cr,ptime);
    //Debug Tempo
  }

  /**
   *
   */
  private void freeConn() {
//    try {
//      SessioneDb.getInstance().closeFreeConnection();
//    } catch (Exception e) {
//      loggerError("ServletModulistica::freeConn() - Attenzione! Errore in fase di rilascio connnessioni: "+e.toString(),e);
//      corpoHtml += "<h2>Attenzione! Errore in fase di rilascio connnessioni.</h2>";
//      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
//        corpoHtml += e.toString();
//      }
//      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
//        corpoHtml += e.getStackTrace().toString();
//      }
//    }

  }

  private void initVu(String p_user) {
    //Debug Tempo
    long ptime = stampaTempo("Modulistica::initVu - Inizio","","","",0);
    //Debug Tempo
    try {
      vu = new Environment(p_user, null, "MODULISTICA", "ADS", null, (new JNDIParameter("jdbc/gdm")));
      vu.setRuolo(ruolo);
    } catch (Exception e) {
      loggerError("Modulistica::initVu - "+e.toString(),e);
      corpoHtml += "<span class='AFCErrorDataTD'>"+"Errore in fase di inizializzazione dell'Environment!"+"</span>";
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        corpoHtml += e.toString();
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        corpoHtml += e.getStackTrace().toString();
      }
    }
    //Debug Tempo
    stampaTempo("Modulistica::initVu - Fine","","","",ptime);
    //Debug Tempo
  }

  /**
   *
   */
  private void inoltraModulo(HttpServletRequest request, String req_utente, ModelloHTMLOut pMdOut) {
//    ModelloHTMLIn   mdIn = null;
    ModelloHTMLOut  mdOut = null;
    String          query, queryIns;
    IDbOperationSQL  dbOpSQL = null/*,
                    dbOp = null*/;
    ResultSet       rst, rstIns = null;
    String          //pSequenza,
                    pClassName,
                    pParametri = null,
                    err_msg = "",
                    sDsn = "";
    int             inoltriFalliti = 0;     // Inoltri falliti
    Inoltro         inoltro;
    HttpSession     httpSess = request.getSession();
    String          req_area = request.getParameter("area");
    String          req_cm   = request.getParameter("cm");
    String          req_cr   = request.getParameter("cr");
    String          iddoc    = null;
    if (req_cr == null) {
      req_cr = (String)httpSess.getAttribute("key");
    }

    //Debug Tempo
    long ptime = stampaTempo("Modulistica::inoltraModulo - Inizio",req_area,req_cm,req_cr,0);
    //Debug Tempo


    query = "SELECT * "+
        "FROM OPERAZIONI_DI_INOLTRO OP "+
        "WHERE OP.AREA = :AREA "+
        "  AND OP.CODICE_MODELLO = :CM " +
        "  AND (INOLTRO_SINGOLO = 'N' OR NOT EXISTS "+
        "   (SELECT 1 " +
        "      FROM LOG_INOLTRI LI " +
        "     WHERE LI.ID_OP = OP.ID_OP " +
        "       AND LI.AREA = :AREA " +
        "       AND LI.STATO = 1 " +
        "       AND LI.CODICE_RICHIESTA = :CR )) " +
        "ORDER BY SEQUENZA ";

    try {
      iddoc = ricercaIdDocumento(request,null);
      dbOpSQL = vu.getDbOp();//SessioneDb.getInstance().createIDbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
      dbOpSQL.setStatement(query);
      dbOpSQL.setParameter(":AREA", req_area);
      dbOpSQL.setParameter(":CM", req_cm);
      dbOpSQL.setParameter(":CR", req_cr);
      dbOpSQL.execute();
      rst = dbOpSQL.getRstSet();

      List<InoltroStruct> listaInoltri = new ArrayList<InoltroStruct>();
      while (rst.next()) {
        listaInoltri.add(new InoltroStruct(rst.getString("ID_OP"), rst.getString("CLASSNAME"),rst.getString("DSN"),rst.getClob("PARAMETRI")));
      }

      if (listaInoltri.size()==0) {
        throw new Exception("ServletModulistica::inoltraModulo() - Attenzione! Impossibile trovare dati relativi all'inoltro con AREA= "+req_area);
      }

      for(int indexInoltro = 0;indexInoltro < listaInoltri.size();indexInoltro++) {
        pidOp  = listaInoltri.get(indexInoltro).getIdOp();
//        pSequenza  = rst.getString("SEQUENZA");
        pClassName = listaInoltri.get(indexInoltro).getClassName();
        sDsn = listaInoltri.get(indexInoltro).getDsn();
        Clob clobParametro = listaInoltri.get(indexInoltro).getParametri();

        if ( sDsn == null) {
          sDsn = "";
        }
        if (!leggiValori(request,iddoc,true)) {
          logger.warn("ServletModulistica::inoltraModulo()- -Attenzione! Si � verificato un errore in fase di lettura valori!");
        }

        queryIns = "SELECT 1 FROM LOG_INOLTRI " +
            "WHERE ID_OP = :ID_OP " +
            "  AND AREA = :AREA " +
            "  AND CODICE_RICHIESTA = :CR ";
        dbOpSQL.setStatement(queryIns);
        dbOpSQL.setParameter(":ID_OP",pidOp);
        dbOpSQL.setParameter(":AREA", req_area);
        dbOpSQL.setParameter(":CR", req_cr);
        dbOpSQL.execute();
        rstIns = dbOpSQL.getRstSet();
        if (!rstIns.next()) {
          queryIns = "INSERT INTO LOG_INOLTRI " +
              "(CODICE_RICHIESTA, AREA, ID_OP) " +
              " VALUES (:CR, :AREA,  :ID_OP) ";
          dbOpSQL.setStatement(queryIns);
          dbOpSQL.setParameter(":ID_OP",pidOp);
          dbOpSQL.setParameter(":AREA", req_area);
          dbOpSQL.setParameter(":CR", req_cr);
          dbOpSQL.execute();
          dbOpSQL.commit();
        }

        // Lettura CLOB campo PARAMETRI

        if (clobParametro != null) {
	        long clobLen = clobParametro.length();

	        if (clobLen < MAXLEN_PARAMETRI) {
	          int i_clobLen = (int)clobLen;
	          pParametri = clobParametro.getSubString(1, i_clobLen);
	        } else {
	          logger.error("ServletInoltro::doGet() - Area: "+req_area+" - Modello: "+req_cm+" - Richiesta: "+req_cr+" - Attenzione! Si � verificato un errore. Il campo supera i "+MAXLEN_PARAMETRI+" caratteri.");
	          corpoHtml += "<span class='AFCErrorDataTD'>"+"Attenzione! Si � verificato un errore. Il campo supera i "+MAXLEN_PARAMETRI+" caratteri."+"</span>";
	        }
        } else {
        	pParametri = "";
        }
        // ------------------------------------
        try {
          inoltro = (Inoltro)Class.forName(pClassName).newInstance();
          inoltro.init(pParametri);
          inoltro.setDSN(sDsn);
          InfoConnessione infoConnessione = new InfoConnessione(Parametri.ALIAS,
                                                                Parametri.SPORTELLO_DSN,
                                                                Parametri.USER,
                                                                Parametri.PASSWD);

          inoltro.parametriRichiesta(pidOp, req_cr, req_area, req_cm,
             null, req_utente, null, infoConnessione);
          if (inoltro.inoltra()) {
            queryIns = "UPDATE LOG_INOLTRI SET " +
                "STATO = :STATO " +
                " WHERE CODICE_RICHIESTA = :CR "+
                "   AND AREA = :AREA "+
                "   AND ID_OP = :ID_OP ";
            dbOpSQL.setStatement(queryIns);
            dbOpSQL.setParameter(":ID_OP",pidOp);
            dbOpSQL.setParameter(":STATO",1);
            dbOpSQL.setParameter(":AREA", req_area);
            dbOpSQL.setParameter(":CR", req_cr);
            dbOpSQL.execute();
            dbOpSQL.commit();
            if (!cancellaPreInoltro(pidOp, req_area, req_cr) ) {
              logger.error("ServletModulistica::inoltraModulo() - Area: "+req_area+" - Modello: "+req_cm+" - Richiesta: "+req_cr+" - Attenzione! Si � verificato un errore in fase di cancellazione valori!");
            }
          } else {
            if (!cancellaPreInoltro(pidOp, req_area, req_cr) ) {
              logger.error("ServletModulistica::inoltraModulo() - Area: "+req_area+" - Modello: "+req_cm+" - Richiesta: "+req_cr+" - Attenzione! Si � verificato un errore in fase di cancellazione valori!");
            }
            err_msg += "Inoltro "+pClassName+" fallito!\n";
            err_msg += inoltro.getErrorMessage();
            inoltriFalliti = inoltriFalliti + 1;
          }
        } catch (Exception ex) {
          loggerError("ServletModulistica::inoltraModulo() - Area: "+req_area+" - Modello: "+req_cm+" - Richiesta: "+req_cr+" - Attenzione! Problemi in fase di inoltro."+ex.toString(),ex);
          if (Parametri.DEBUG.equalsIgnoreCase("1")) {
            corpoHtml += ex.toString();
          }
          if (Parametri.DEBUG.equalsIgnoreCase("2")) {
            corpoHtml += "<span class='AFCErrorDataTD'>"+"Attenzione! Problemi in fase di inoltro!"+"</span>";
            corpoHtml += ex.toString();
            StackTraceElement[] st = ex.getStackTrace();
            for (int i = 0;i < st.length; i++) {
              corpoHtml += st[i].toString();
            }
          }
        }
      }
      //free(dbOpSQL);

      String sc = request.getParameter("sc");

      //Modifica di MMA.
      if (sc == null) {
        sc = "1";
      }

      preCaricamentoDati(request,req_area,req_cr, req_cm, false);
      if (pMdOut == null) {
//        mdIn = new ModelloHTMLIn(request, req_area, req_cm, req_cr, calcolaScadenza(sc),timeFirstOpen,pdo);
        // A questo punto suppongo di avere il ModelloHTMLIn cercato nella variabile mdIn
//        mdOut = new ModelloHTMLOut(request, mdIn);
        mdOut = new ModelloHTMLOut(mdg);
      } else {
        mdOut = pMdOut;
      }

      if (inoltriFalliti > 0) {
        mdOut.settaErrMsg(err_msg);
        cancellaRepository(request, iddoc,false);
      } else {
        cancellaRepository(request, iddoc,false);
      }
      if (pMdOut == null) {
        isW3c = mdOut.isW3c();
        corpoHtml += mdOut.getValue(vu.getDbOp());
      }
      //Debug Tempo
      stampaTempo("Modulistica::inoltraModulo - Fine",req_area,req_cm,req_cr,ptime);
      //Debug Tempo

    } catch (Exception ex) {
      loggerError("ServletInoltro::doGet() - Area: "+req_area+" - Modello: "+req_cm+" - Richiesta: "+req_cr+" - Attenzione! Si � verificato un errore (1). "+ex.toString(),ex);
      corpoHtml += "<span class='AFCErrorDataTD'>"+"Attenzione! Problemi in fase di inoltro!"+"</span>";
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        corpoHtml += ex.toString();
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        corpoHtml += ex.getStackTrace().toString();
      }
    }
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
  private String creaUtente(HttpSession httpSess) {
    String ret  = "GUEST";
    httpSess.setAttribute("UtenteGDM",ret);
    httpSess.setAttribute("RuoloGDM",ret);
    return ret;
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
      dbOp = vu.getDbOp();

      dbOp.setStatement(query);
      dbOp.setParameter(":NOMINATIVO", nominativo);
      dbOp.execute();
      rst = dbOp.getRstSet();

      if (rst.next() ) {
         retval = rst.getString(1);
      }

      return retval;
    } catch (Exception e) {
      loggerError("ServletModulistica::cercaUtente - "+e.toString(),e);
      return "";
    }
  }

  /**
   *  Funzione che restituisce TRUE se in fase di registrazione le
   *  operazione di controllo modello hanno riscontrato quache
   *  segnalazione di errore.
   */
  public boolean erroriControlli() {
    return erroreControlli;
  }

  /**
   *  Funzione che restituisce TRUE se in fase di registrazione le
   *  operazione di controllo modello hanno riscontrato quache
   *  segnalazione di errore.
   */
  public boolean esistonoErrori() {
    return erroreControlli;
  }

  /**
   *  Funzione che ritorna TRUE se sono presenti operazioni di inoltro
   *  che devono essere ancora eseguite
   *  @param areaModello Area del modello
   *  @param codiceModello Codice del modello
   *  @param codiceRichiesta Codice richiesta
   */
  public boolean esistonoInoltri(String areaModello, String codiceModello, String codiceRichiesta) {
    //Debug Tempo
    long ptime = stampaTempo("Modulistica::esistonoInoltri - Inizio",areaModello,codiceModello,codiceRichiesta,0);
    //Debug Tempo
      IDbOperationSQL dbOpSQL = null;
      ResultSet rst;
      String query = null;
      if (codiceRichiesta != null) {
        query = "SELECT * "+
            "FROM OPERAZIONI_DI_INOLTRO OP "+
            "WHERE OP.AREA = :AREA "+
            "  AND OP.CODICE_MODELLO = :CM " +
            "  AND ( INOLTRO_SINGOLO = 'N' OR NOT EXISTS "+
            "   (SELECT 1 " +
            "      FROM LOG_INOLTRI LI " +
            "     WHERE LI.ID_OP = OP.ID_OP " +
            "       AND LI.AREA = :AREA " +
            "       AND LI.STATO = 1 " +
            "       AND LI.CODICE_RICHIESTA = :CR )) " +
            "ORDER BY SEQUENZA ";
      } else {
        query = "SELECT * "+
                "FROM OPERAZIONI_DI_INOLTRO OP "+
                "WHERE OP.AREA = :AREA "+
                "AND OP.CODICE_MODELLO = :CM ";
      }
      try {
        dbOpSQL = Parametri.creaDbOp();
        dbOpSQL.setStatement(query);
        dbOpSQL.setParameter(":AREA", areaModello);
        dbOpSQL.setParameter(":CM", codiceModello);
        dbOpSQL.setParameter(":CR", codiceRichiesta);
        dbOpSQL.execute();
        rst = dbOpSQL.getRstSet();
        if (!rst.next()) {
          free(dbOpSQL);
          //Debug Tempo
          stampaTempo("Modulistica::esistonoInoltri - Fine",areaModello,codiceModello,codiceRichiesta,ptime);
          //Debug Tempo
          return false;
        }
        free(dbOpSQL);
      } catch (Exception e) {
        loggerError("Modulistica::esistonoInoltri() - Area: "+areaModello+" - Modello: "+codiceModello+" - Richiesta: "+codiceRichiesta+" - "+e.toString(),e);
        free(dbOpSQL);
        //Debug Tempo
        stampaTempo("Modulistica::esistonoInoltri - Fine",areaModello,codiceModello,codiceRichiesta,ptime);
        //Debug Tempo
        return false;
      }
      //Debug Tempo
      stampaTempo("Modulistica::esistonoInoltri - Fine",areaModello,codiceModello,codiceRichiesta,ptime);
      //Debug Tempo
      return true;
  }

  /**
   *
   */
  private void listaAllegati(HttpServletRequest  request, String areaModello, String codiceModello, String codiceRichiesta, String fase) {
    //Debug Tempo
    long ptime = stampaTempo("Modulistica::listaAllegati - Inizio",areaModello,codiceModello,codiceRichiesta,0);
    //Debug Tempo

    	IDbOperationSQL dbOpSQL = null;
      ResultSet rst;
      int i = 0;
      String query = null;
      String classDiv = "";
      String classSpan = "";
//      String competenza = " 1 COMPETENZA ";
      String path = request.getSession().getServletContext().getRealPath("")+File.separator+"upload";
      path = path+File.separator+codiceRichiesta+File.separator+codiceModello;
      String newCorpo = "";
      String endCorpo = "";

      String sUs = (String)request.getSession().getAttribute("UtenteGDM");
      String sRu = (String)request.getSession().getAttribute("RuoloGDM");
      String iddoc = null;
      if (isAjax) {
      	corpoHtml = "";
      } else {
      	int start = corpoHtml.indexOf("class='ui-layout-south'");
      	if (start > -1) {
          	int toolbar = corpoHtml.indexOf("toolbar",start);
          	if (toolbar > start) {
          		start = corpoHtml.indexOf("</div>", toolbar);
          		start = start + 6;
          		int reload = corpoHtml.indexOf("reload",start);
          		if (reload > start) {
              		start = corpoHtml.indexOf("</div>", reload);
              		start = start + 6;
          		}
          	}
          	start = corpoHtml.indexOf("</div>", start);
      		newCorpo = corpoHtml.substring(0, start);
      		endCorpo = corpoHtml.substring(start);
      		corpoHtml = newCorpo;
      		endCorpo = endCorpo.replaceAll("</body>","");
      		endCorpo = endCorpo.replaceAll("</html>","");
      	}
/*	      String newCorpo = corpoHtml.replaceAll("</div></form>","");
	      corpoHtml = newCorpo;
	      newCorpo = corpoHtml.replaceAll("</body>","");
	      corpoHtml = newCorpo.replaceAll("</html>","");*/
      }
      query = "SELECT NVL(FOFI.ICONA, 'generico.gif'), OGFI.ID_OGGETTO_FILE, OGFI.FILENAME, NVL(FOFI.VISIBILE,'S'), DA_CANCELLARE " +
              "FROM OGGETTI_FILE OGFI, FORMATI_FILE FOFI " +
              "WHERE OGFI.ID_FORMATO = FOFI.ID_FORMATO "+
              "AND OGFI.ID_DOCUMENTO = :ID_DOC "+
              "ORDER BY 3 ASC";

      try {
      	iddoc = ricercaIdDocumento(request,null);
        classDiv = " class='AFCAllegati' ";
        if (!isAjax) {
        	corpoHtml += "<div id='gdm_allegati' "+classDiv+">\n";
        } else {
        	corpoHtml += "<div id='gdmIdAjax_gdm_allegati'>";
        }
        if (compAll.getCompetenzeAllegati().equalsIgnoreCase("S")) {
/*          UtenteAbilitazione ua = new UtenteAbilitazione(vu.getUser(), vu.getGruppo(), vu.getRuolo(), vu.getPwd(),  vu.getUser());
          Abilitazioni abl = new Abilitazioni("DOCUMENTI", iddoc, "LA");
          Abilitazioni abu = new Abilitazioni("DOCUMENTI", iddoc, "UA");
          if (((new GDM_Competenze(vu)).verifica_GDM_Compentenza(ua,abl) == 0) &&
          		((new GDM_Competenze(vu)).verifica_GDM_Compentenza(ua,abu) == 0)) {*/
        	if (compAll.getLettura() == 0 && compAll.getModifica() == 0) {
            if (!isAjax) {
 //           	corpoHtml += "</div></div></form>\n";
            	corpoHtml += "</div>"+endCorpo+"\n";
    	        if (!pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
    	          corpoHtml += "</body></html>";
    	        }
            } else {
            	corpoHtml += "</div>";
            }
          	return;
          }
        }
        dbOpSQL = vu.getDbOp();
        if (iddoc != null) {
          dbOpSQL.setStatement(query);
          dbOpSQL.setParameter(":ID_DOC",iddoc);
          dbOpSQL.execute();
          rst = dbOpSQL.getRstSet();

          while (rst.next()) {
            String icona          = rst.getString(1);
            String id_ogfi        = rst.getString(2);
            String nomeFile       = rst.getString(3);
            String visibile       = rst.getString(4);
            String da_cancellare  = rst.getString(5);
            if (da_cancellare.equalsIgnoreCase("S")) {
              icona = "../fulltrash.gif";
            }
            String firma = "";
            String wopen = "_blank";
            if (nomeFile.toUpperCase().indexOf(".P7M") > 0) {
              firma = "S";
              wopen = "Firma";
            } else {
            	if (nomeFile.toUpperCase().indexOf(".PDF") > 0) {
                wopen = "Firma";
            	}
            }
            if (visibile.equalsIgnoreCase("S")) {
              corpoHtml += "<span"+classSpan+">\n";
              if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
                corpoHtml += "<a href='../common/ServletVisualizza.do?ar="+areaModello+"&amp;cm="+codiceModello;
                if (firma.equalsIgnoreCase("S")) {
                  	corpoHtml += "&amp;cr="+codiceRichiesta+"&amp;ca="+id_ogfi+"&amp;iddoc="+iddoc+"&amp;firma="+firma+"' ";
                } else {
                  corpoHtml += "&amp;cr="+codiceRichiesta+"&amp;ca="+id_ogfi+"&amp;iddoc="+iddoc+"&amp;firma=' ";
                }
                corpoHtml += "onclick='wleft = (screen.width - 500) / 2; wtop = (screen.height - 300) / 2; ";
                corpoHtml += "window.open(this.href,\""+wopen+"\",\"toolbar=no, scrollbars=1, menubar=no, resizable=yes, width= 500, height= 300, left=\"+wleft+\", top=\"+wtop+\" \"); return false;' ";
                corpoHtml += "onkeypress='wleft = (screen.width - 500) / 2; wtop = (screen.height - 300) / 2; ";
                corpoHtml += "window.open(this.href,\""+wopen+"\",\"toolbar=no, scrollbars=1, menubar=no, resizable=yes, width= 500, height= 300, left=\"+wleft+\", top=\"+wtop+\" \"); return false;' ";
                corpoHtml += "title='Attenzione apre una nuova finestra' >\n";
                corpoHtml += "<img src='../common/images/gdm/formati/"+icona+"' alt='Allegato'/> "+nomeFile+"</a> \n";
              } else {
                corpoHtml += "<a href='ServletVisualizza?ar="+areaModello+"&amp;cm="+codiceModello;
                if (Parametri.ALLEGATI_SINGLE_SIGN_ON.equalsIgnoreCase("S")) {
                	corpoHtml += "&amp;cr="+codiceRichiesta+"&amp;ca="+id_ogfi+"&amp;iddoc="+iddoc+"&amp;firma="+firma+"' ";
                } else {
                	corpoHtml += "&amp;cr="+codiceRichiesta+"&amp;ca="+id_ogfi+"&amp;iddoc="+iddoc+"&amp;firma="+firma+"&amp;us="+sUs+"&amp;ruolo="+sRu+"' ";
                }
                corpoHtml += "onclick='wleft = (screen.width - 500) / 2; wtop = (screen.height - 300) / 2; ";
                corpoHtml += "window.open(this.href,\""+wopen+"\",\"toolbar=no,scrollbars=1,menubar=no, resizable=yes, width= 500, height= 300, left=\"+wleft+\", top=\"+wtop+\" \"); return false;' ";
                corpoHtml += "onkeypress='wleft = (screen.width - 500) / 2; wtop = (screen.height - 300) / 2; ";
                corpoHtml += "window.open(this.href,\""+wopen+"\",\"toolbar=no,scrollbars=1,menubar=no, resizable=yes, width= 500, height= 300, left=\"+wleft+\", top=\"+wtop+\" \"); return false;' ";
                corpoHtml += "title='Attenzione apre una nuova finestra' >\n";
                corpoHtml += "<img src='images/gdm/formati/"+icona+"' alt='Allegato'/> "+nomeFile+"</a> \n";
              }
              if (lettura.equalsIgnoreCase("W") && (fase.equalsIgnoreCase("open") || fase.equalsIgnoreCase("salvataggio"))) {
                corpoHtml += "<input type='hidden' name='hAllegato_"+i+"' value='"+id_ogfi+"'>";
                corpoHtml += "<input class='AFCInput' type='hidden' name='ceckAllegato_"+i+"' value='' /> \n";
              }
              i++;
              corpoHtml += "</span>";
            } else {
              corpoHtml += "<input type='hidden' name='hAllegato_"+i+"' value=''>";
              i++;
            }
          }
        }

        if (lettura.equalsIgnoreCase("W")) {
          query = "SELECT NOMEFILE FROM ALLEGATI_TEMP " +
            "WHERE AREA = :AREA AND "+
            "CODICE_RICHIESTA = :CR AND "+
            "CODICE_MODELLO = :CM AND "+
            "UTENTE_AGGIORNAMENTO = :UTENTE "+
            "ORDER BY NOMEFILE ASC";
          dbOpSQL.setStatement(query);
          dbOpSQL.setParameter(":AREA",areaModello);
          if (cm_padre != null && cm_padre.length() != 0) {
          	dbOpSQL.setParameter(":CM",cm_padre);
          } else {
          	dbOpSQL.setParameter(":CM",codiceModello);
          }
          dbOpSQL.setParameter(":CR",codiceRichiesta);
          dbOpSQL.setParameter(":UTENTE",sUs);
          dbOpSQL.execute();
          rst = dbOpSQL.getRstSet();
          while(rst.next()) {
            String cod = rst.getString(1);
            corpoHtml += "<span"+classSpan+">\n";
            if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
              corpoHtml += "<a href='../common/ServletVisualizza.do?ar="+areaModello+"&amp;cm="+codiceModello;
              corpoHtml += "&amp;cr="+codiceRichiesta+"&amp;ca="+cod+"' ";
              corpoHtml += "onclick='window.open(this.href,\"_self\",\"toolbar=no,menubar=no, width= 10, height= 10\"); return false;' ";
              corpoHtml += "onkeypress='window.open(this.href,\"_self\",\"toolbar=no,menubar=no, width= 10, height= 10\"); return false;' ";
              corpoHtml += "title='Attenzione apre una nuova finestra' >\n";
              corpoHtml += "<img src='../common/images/gdm/formati/nuovo.gif' alt='Nuovo allegato'/> "+cod+"</a> \n";
            } else {
              corpoHtml += "<a href='ServletVisualizza?ar="+areaModello+"&amp;cm="+codiceModello;
              corpoHtml += "&amp;cr="+codiceRichiesta+"&amp;ca="+cod+"' ";
              corpoHtml += "onclick='window.open(this.href,\"_self\",\"toolbar=no,menubar=no, width= 10, height= 10\"); return false;' ";
              corpoHtml += "onkeypress='window.open(this.href,\"_self\",\"toolbar=no,menubar=no, width= 10, height= 10\"); return false;' ";
              corpoHtml += "title='Attenzione apre una nuova finestra' >\n";
              corpoHtml += "<img src='images/gdm/formati/nuovo.gif' alt='Nuovo allegato'/> "+cod+"</a> \n";
            }
            if (lettura.equalsIgnoreCase("W") && (fase.equalsIgnoreCase("open") || fase.equalsIgnoreCase("salvataggio"))) {
              corpoHtml += "<input class='AFCInput' type='hidden' name='ceckAllegato_"+i+"' value='' /> \n";
            }
            corpoHtml += "</span>";
            i++;
          }
        }
        if (!isAjax) {
        	corpoHtml += "</div>"+endCorpo+"\n";
	        if (!pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
	          corpoHtml += "</body></html>";
	        }
        } else {
        	corpoHtml += "</div>";
        }

      } catch (Exception e) {
        loggerError("Modulistica::listaAllegati() - "+e.toString(),e);

      }
      //Debug Tempo
      stampaTempo("Modulistica::listaAllegati - Fine",areaModello,codiceModello,codiceRichiesta,ptime);
      //Debug Tempo
  }

  private String urlFirma(HttpServletRequest  request) {
  	String retval = "";
  	IDbOperationSQL dbOp = null;
  	try {
  		dbOp = vu.getDbOp();
  		LinkedList<String> iddocs 	= new LinkedList<String>();
  		String urlServer 			= "";
  		String contextPath 			= "";
  		String us      				= (String)request.getSession().getAttribute("UtenteGDM");

      if (Parametri.PROTOCOLLO.length() == 0) {
      	urlServer = request.getScheme();
      } else {
      	urlServer = Parametri.PROTOCOLLO;
      }
      if (Parametri.SERVERNAME.length() == 0) {
      	urlServer += "://"+request.getServerName();
      } else {
      	urlServer += "://"+Parametri.SERVERNAME;
      }
      if (Parametri.SERVERPORT.length() == 0) {
      	urlServer += ":"+request.getServerPort();
      } else {
      	urlServer += ":"+Parametri.SERVERPORT;
      }

      contextPath = request.getContextPath();

  		String iddoc = ricercaIdDocumento(request,null);
  		if (iddoc == null) {
  			throw new Exception ("Documento non presente");
  		} else {
  			iddocs.add(iddoc);
  		}
    	FirmaUnimatica fu = new FirmaUnimatica(dbOp.getConn(), iddocs, us, "", urlServer,contextPath);
    	retval = fu.creaURLFirma();
/*	      corpoHtml += "<html>";
      corpoHtml += "<head><title>ServletReindirizza</title>";
      corpoHtml += "<meta http-equiv='refresh' content='0; URL="+fu.creaURLFirma()+"' />";
      corpoHtml += "</head><body>";
      corpoHtml += "</body></html>";*/
    	dbOp.commit();

  	} catch (Exception e) {
  		try {
  			dbOp.rollback();
  		} catch (Exception eroll) {}

      loggerError("ServletModulistica::genera() - Attenzione! Errore in fase di firma: "+e.toString(),e);
      corpoHtml += "<span class='AFCErrorDataTD'>"+"Attenzione! Errore in fase di firma.</span>";
  	}
    return retval;
  }

  private String urlSucc(HttpServletRequest  request,
                          String codiceModello,
                          String codiceRichiesta,
                          String modelloSuccessivo,
                          String queryHtmlIn,
                          String modo,
                          String iddoc) {

    //Debug Tempo
    long ptime = stampaTempo("Modulistica::urlSucc - Inizio","",codiceModello,codiceRichiesta,0);
    //Debug Tempo
      String retval, queryUrl;

      try {
        queryUrl = queryHtmlIn;


        if (queryUrl.indexOf("cr") == -1)
          queryUrl += "&amp;cr=" + codiceRichiesta;

        queryUrl=queryUrl.replaceFirst(codiceModello, modelloSuccessivo);

        LinkedList<String> l1 = null;
        LinkedList<AccediDocumento> l2 = null;
        Object attributo = request.getSession().getAttribute("modello_precedente");
        if (attributo != null) {
          l1 = (LinkedList<String>)attributo;
          l2 = (LinkedList<AccediDocumento>)request.getSession().getAttribute("valori_modello_precedente");
        } else {
          l1 = new LinkedList<String>();
          l2 = new LinkedList<AccediDocumento>();
        }
        if (iddoc != null) {
          if (modo.equalsIgnoreCase("S")) {
            l1.addLast(codiceModello);
            AccediDocumento adp = new AccediDocumento(iddoc,vu);
            adp.accediDocumentoValori();
            l2.addLast(adp);
          } else {
            l1.removeLast();
            l2.removeLast();
          }
          request.getSession().setAttribute("modello_precedente",l1);
          request.getSession().setAttribute("valori_modello_precedente",l2);
          int i = queryUrl.indexOf("&amp;prec_succ");
          if (i == -1) {
            retval = request.getRequestURL()+"?"+queryUrl + "&amp;prec_succ=" + modo;
          } else {
            retval = request.getRequestURL()+"?"+queryUrl.substring(0,i) + "&amp;prec_succ=" + modo;
          }
        } else {
          if (modo.equalsIgnoreCase("S")) {
            l1.addLast(codiceModello);
          } else {
            l1.removeLast();
          }
          request.getSession().setAttribute("modello_precedente",l1);
          int i = queryUrl.indexOf("&prec_succ");
          if (i == -1) {
            retval = request.getRequestURL()+"?"+queryUrl + "&amp;prec_succ=" + modo;
          } else {
            retval = request.getRequestURL()+"?"+queryUrl.substring(0,i) + "&amp;prec_succ=" + modo;
          }
        }

      } catch(Exception ex) {
        loggerError("Modulistica::urlSucc - Errore nella creazione dell'url  al documento successivo. "+ex.getMessage(),ex);
        retval = "";
//        ex.printStackTrace();
      }

      //Debug Tempo
      stampaTempo("Modulistica::urlSucc - Fine","",codiceModello,codiceRichiesta,ptime);
      //Debug Tempo
      return retval;
   }

  /**
   *
   */
  private String leggiValore(HttpServletRequest  request,
                             String dato) {
    IDbOperationSQL  dbOpC = null;
    String result = "";
    String tipoDato = "";
    String ar = request.getParameter("area");
    String cm = request.getParameter("cm");
    String query = "SELECT TIPO_CAMPO "+
             "FROM DATI_MODELLO "+
             "WHERE    AREA = :AREA AND "+
             "CODICE_MODELLO = :CM AND "+
             "DATO = :DATO";

//    String query = "SELECT TIPO_CAMPO "+
//    "FROM DATI_MODELLO "+
//    "WHERE    AREA = '"+ar+"' AND "+
//    "CODICE_MODELLO = '"+cm+"' AND "+
//    "DATO = '"+dato+"'";
    try {
      if (isAjax) {
        result = request.getParameter(dato);
        if (result == null) {
          result = "";
        }
        result = URLDecoder.decode(result,"UTF-8");
        return result;
      }
//      String campi = "";
      dbOpC = vu.getDbOp();
      dbOpC.setStatement(query);
      dbOpC.setParameter(":AREA", ar);
      dbOpC.setParameter(":CM", cm);
      dbOpC.setParameter(":DATO", dato);
      dbOpC.execute();
      ResultSet rsC = dbOpC.getRstSet();
      if (rsC.next()) {
        tipoDato = rsC.getString(1);
      } else {
        return "";
      }
    } catch (Exception e) {
      loggerError("Ajax", e);
    }
    if (tipoDato == null) {
      return "";
    }

    if (tipoDato.charAt(0) != 'B') {            // new!
      // E' un campo legato ad un dominio, ma non � di tipo CHECKBOX
      result = request.getParameter(dato);

    } else {
      // E' un campo legato ad un CAMPO di tipo CHECKBOX
      result = "";
      for (int j=0; j<1000; j++) {  // 1000 valori al massimo.
        String s = request.getParameter(dato+"_"+Integer.toString(j));
        if (s != null) {
          // Attacco sempre un separatore in fondo, in tal modo quando dovr�
          // recuperare un valore lo richiamer� sempre utilizzando il separatore
          result = result + s + Parametri.SEPARAVALORI;
        }
      }
    }
    if (result == null) {
      result = "";
    }

    return result;
  }

  /**
   *
   */
  private String modificaJwf(String corpo) {
    String retval = "";

    int j = corpo.indexOf("<meta http-equiv");
    if (j > - 1) {
      int i = corpo.indexOf("/>",j);
      retval = corpo.substring(0,j);
      retval += "<meta http-equiv='refresh' content='0; URL="+jwf_back+"' />";
      retval += corpo.substring(i+2);
    } else {
      int x = corpo.indexOf("</head>");
      if (x > -1) {
        retval = corpo.substring(0,x);
        retval += "<meta http-equiv='refresh' content='0; URL="+jwf_back+"' />";
        retval += corpo.substring(x+7);
      } else {
        retval = "<meta http-equiv='refresh' content='0; URL="+jwf_back+"' />";
        retval += corpo;
      }
    }
    return retval;
  }

  /**
   *
   */
   protected String completaConnessione(String connessione){
     String connessioneParam = connessione;
     String pCodice = null;
     String retval = null;

     int h = 0;
     int s = 0;

     h = connessioneParam.indexOf(":HOST_DOMINIO");
     if (h > -1) {
       pCodice = connessioneParam.substring(h+1,h+15);
       retval = Parametri.getParametriDomini(pCodice);
       connessioneParam = connessioneParam.replaceAll(":"+pCodice,retval);
     }
     s = connessioneParam.indexOf(":SID_DOMINIO");
     if (s > -1) {
       pCodice = connessioneParam.substring(s+1,s+14);
       retval = Parametri.getParametriDomini(pCodice);
       connessioneParam = connessioneParam.replaceAll(":"+pCodice,retval);
     }

     return connessioneParam;

   }

  /**
    *
    */
  protected String eseguiControllo(HttpServletRequest request,
                                   String             controllo,
                                   boolean            modifica,
                                   AggiungiDocumento  ad_1,
                                   AggiornaDocumento  ad_2,
                                   String             cr,
                                   String             iddoc) {
    return eseguiControllo(request, controllo, modifica, ad_1, ad_2, cr, iddoc, false);
  }

  /**
    *
    */
  protected String eseguiControllo(HttpServletRequest request,
                                   String             controllo,
                                   boolean            modifica,
                                   AggiungiDocumento  ad_1,
                                   AggiornaDocumento  ad_2,
                                   String             cr,
                                   String             iddoc,
                                   boolean            isAj) {

    String retval = "";
    String result = "";
    String queryC = "";
    String tipoC = "";
    String corpo = "";
    String sblocco = "S";
    String newArea = "", newControllo = "";
    String area = "";
    String cm = "";
    String cmex= "";
    IDbOperationSQL  dbOpF = null;
    boolean dbOpAutonoma = false;
    IDbOperationSQL  dbOp = null;
    IDbOperationSQL  dbOp2 = null;
    ResultSet       rst = null;
    SyncSuite sync = null;
    Element root;
    Vector<Integer> ite = null;
    String syncErr = null;

    area  = request.getParameter("area");
    cm    = request.getParameter("cm");

    //Debug Tempo
    long ptime = stampaTempo("Modulistica::eseguiControllo - Inizio",area,cm,cr,0);
    //Debug Tempo


    String us = (String)request.getSession().getAttribute("Utente");
    if (us == null) {
      us = "";
    }


    //Eseguo il controllo
    try {
      if (controllo.indexOf("_GDM_") == 0) {
      	sblocco = "N";
/*      	corpo = MappaAction.getCorpo(controllo);
        String javaStm = "";
        ControlliParser cp = new ControlliParser(request,iddoc,cr,sFunInput,false,stato_doc);
        javaStm = cp.bindingDeiParametri(corpo);*/
        try{
/*        	System.out.println(corpo);
        	System.out.println(sFunInput);
        	System.out.println(javaStm);
          WrapParser wp = new WrapParser(javaStm);
          dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
          result = wp.goExtended(request, dbOp);
          free(dbOp);*/
        	result = MappaAction.eseguiAction(controllo, sFunInput);
        } catch (Exception ijEx) {
          loggerError("ServletModulistica::eseguiControllo() - Area: "+area+" - Modello: "+cm+"- Controllo: "+controllo+" - Errore:  ["+ ijEx.toString()+"]",ijEx);
          throw new Exception(ijEx.toString());
        }

      } else {
        //Controllo se c'� una personalizzazione
        Personalizzazioni pers = null;
        pers = (Personalizzazioni)request.getSession().getAttribute("_personalizzazioni_gdm");
        if (pers != null) {
          String persAction = pers.getPersonalizzazione(Personalizzazioni.LIBRERIA_CONTROLLI, area+"#"+controllo);
          int j = persAction.indexOf("#");
          controllo = persAction.substring(j+1);
        }

        if (controllo.equalsIgnoreCase("-")) {
          //Debug Tempo
          stampaTempo("Modulistica::eseguiControllo - Fine",area,cm,cr,ptime);
          //Debug Tempo

          return "";
        }
        byte[] b2 = new byte[1];
        b2[0] = 13;
        String ch = new String(b2);

        sync = new SyncSuite(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
        if (cm_padre.length() > 0) {
        	cmex = cm_padre;
        } else {
        	cmex = cm;
        }

        ite = (Vector<Integer>)sync.isExecutable(area,controllo,area+"@"+cmex+"@"+cr,us);
        if (ite == null) {
        	ite = (Vector<Integer>)sync.isExecutable(area,controllo,area+"@"+cm+"@"+cr,us);
        }
        syncErr = sync.getLastError();
        if (syncErr != null) {
          closeSync(sync);
          //Debug Tempo
          stampaTempo("Modulistica::eseguiControllo - Fine",area,cm,cr,ptime);
          //Debug Tempo
          return syncErr;
        }
        if (ite == null) {
          closeSync(sync);
          //Debug Tempo
          stampaTempo("Modulistica::eseguiControllo - Fine",area,cm,cr,ptime);
          //Debug Tempo
          return "";
        }


        queryC = "SELECT CORPO, TIPO, DRIVER, CONNESSIONE, UTENTE, PASSWD, CAMPI, MSG_ERRORE, DSN, SBLOCCO_AUTOMATICO "+
                 "FROM LIBRERIA_CONTROLLI "+
                 "WHERE AREA = :AREA AND "+
                 "CONTROLLO = :CONTROLLO ";

        dbOp = vu.getDbOp();
        dbOp.setStatement(queryC);
        dbOp.setParameter(":AREA", area);
        dbOp.setParameter(":CONTROLLO", controllo);
        dbOp.execute();
        rst = dbOp.getRstSet();
        if (rst.next()) {
          sblocco = rst.getString("SBLOCCO_AUTOMATICO");
          tipoC = rst.getString("TIPO");
          corpo = rst.getString("CORPO");
          corpo = corpo.replaceAll(ch," ");
          corpo = corpo.replaceAll("\n"," ");
          if (tipoC.equalsIgnoreCase("J")) {
            String javaStm = "";
            ControlliParser cp = new ControlliParser(request,iddoc,cr,sFunInput,false,stato_doc,isAj);
            javaStm = cp.bindingDeiParametri(corpo);
  //          String javaStm = bindingDinamico(request,corpo,iddoc,cr,sFunInput,false);
            try{
              if (!Parametri.DEBUG.equalsIgnoreCase("0")) {
                loggerError(javaStm,null);
              }
              WrapParser wp = new WrapParser(javaStm);
              dbOp2 = Parametri.creaDbOp();
              result = wp.goExtended(request, dbOp2);
              free(dbOp2);
            } catch (Exception ijEx) {
              free(dbOp2);
              loggerError("ServletModulistica::eseguiControllo() - Area: "+area+" - Modello: "+cm+"- Controllo: "+controllo+" - Errore:  ["+ ijEx.toString()+"]",ijEx);
              throw new Exception(ijEx.toString());
            }
          }
          if (tipoC.equalsIgnoreCase("P")) {
            String conDRIVER = rst.getString(3);
            String conCONNESIONE = rst.getString(4);
            String conUTENTE = rst.getString(5);
            String conPASSWD = rst.getString(6);

            String sDsn = rst.getString("DSN");
            if (sDsn == null) {
              sDsn = "";
            }
            if (sDsn.length() != 0) {
              //dbOp2 = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME, 0);
              Connessione cn = new Connessione(vu.getDbOp(),sDsn);
              conDRIVER      = cn.getDriver();
              conCONNESIONE = cn.getConnessione();
              conUTENTE        = cn.getUtente();
              conPASSWD      = cn.getPassword();
              //free(dbOp2);
            }

            if (conDRIVER == null) {
              conDRIVER = "";
            }
            if (conDRIVER.length() != 0) {
              String compConn = completaConnessione(conCONNESIONE);
              ConnessioneParser cp = new ConnessioneParser();
              String connessione = cp.bindingDeiParametri(compConn);
              if (connessione == null){
                connessione = conCONNESIONE;
              }
              dbOpAutonoma=true;
              dbOpF = SessioneDb.getInstance().createIDbOperationSQL(conDRIVER,connessione,conUTENTE,conPASSWD);
            } else {
              dbOpF = dbOp;
            }
            String funcSql = ""; //bindingDinamico(request,corpo,iddoc,cr,sFunInput,false);
            ControlliParser cp = new ControlliParser(request,iddoc,cr,sFunInput,false,stato_doc, isAj);
            funcSql = cp.bindingDeiParametri(corpo);
            dbOpF.setCallFunc(funcSql);
            dbOpF.execute();
            result = dbOpF.getCallSql().getString(1);
            if (result == null) {
              result = "";
            }
            if (dbOpAutonoma) free(dbOpF);
          }
          queryC = "SELECT COUNT(1) FROM SYNCJDMSCONTROL "+
                   "WHERE AREA = :AREA AND CONTROLLO = :CONTROLLO";
          dbOp.setStatement(queryC);
          dbOp.setParameter(":AREA", area);
          dbOp.setParameter(":CONTROLLO", controllo);
          dbOp.execute();
          rst = dbOp.getRstSet();
          if (rst.next()) {
            if (rst.getInt(1) == 0) {
              sblocco = "N";
            }
          }
        }
        //free(dbOp);

      }
    } catch (Exception e) {
      closeSync(sync);
      if (dbOpAutonoma) free(dbOpF);
      //free(dbOp);
      free(dbOp2);
//      free(dbOpD);
      iterPuls = "";
      loggerError("ServletModulistica::eseguiControllo() - Area: "+area+" - Modello: "+cm+"- Controllo: "+controllo+" - Errore:  ["+ e.toString()+"]",e);
      result = "";
      if (isAj) {
//        retval = "Errore_GDM_Ajax";
      	retval = "<FUNCTION_OUTPUT><RESULT>nonok</RESULT><ERROR>Errore action: "+e.toString()+"</ERROR></FUNCTION_OUTPUT>";
      } else {
        retval += "Errore in fase di controllo!";
        if (Parametri.DEBUG.equalsIgnoreCase("1")) {
          retval += e.toString();
        }
        if (Parametri.DEBUG.equalsIgnoreCase("2")) {
          retval += e.getStackTrace().toString();
        }
      }
      //Debug Tempo
      stampaTempo("Modulistica::eseguiControllo - Fine",area,cm,cr,ptime);
      //Debug Tempo
      return retval;
    }

    //Interpretazione XML di output
    try {
      Document dInput = null;
      dInput = DocumentHelper.parseText(result);
      String esito = leggiValoreXML(dInput, "RESULT");
      if (!esito.equalsIgnoreCase("ok")) {
        String errore = leggiValoreXML(dInput, "ERROR");
        String stackError = leggiValoreXML(dInput, "STACKTRACE");
        if (stackError == null) {
          stackError = "";
        }
        if (stackError.length() != 0) {
          logger.error(stackError);
        }
        iterPuls = "";
        closeSync(sync);
        if (!isAj) {
          root = dInput.getRootElement();
          String newData = "";
          Element eAgg = leggiElementoXML(root, "DATI_AGGIORNAMENTO");
          if (eAgg != null) {
            newData = leggiValoreXML(eAgg, "DATA");
            if ((newData !=null) && (newData.length() != 0)) {
              aggData = newData;
              settaDataAggiornamentoModello(request, aggData);
            }
            idCartnew = leggiValoreXML(eAgg, "ID_CARTELLA");
          }
        } else {
          root = dInput.getRootElement();
          String newData = "";
          Element eAgg = leggiElementoXML(root, "DATI_AGGIORNAMENTO");
          if (eAgg != null) {
            newData = leggiValoreXML(eAgg, "DATA");
            if ((newData !=null) && (newData.length() != 0)) {
              aggData = newData;
              settaDataAggiornamentoModello(request, aggData);
            }
//            idCartnew = leggiValoreXML(eAgg, "ID_CARTELLA");
          }
          errore = result;
        }

        //Debug Tempo
        stampaTempo("Modulistica::eseguiControllo - Fine",area,cm,cr,ptime);
        //Debug Tempo
        return errore;
      }
      iterAction = leggiValoreXML(dInput, "ITER_ACTION");
      if (iterAction == null) {
      	iterAction = "";
      }
      String new_gdc_link =  null;
      root = dInput.getRootElement();
      Element elem;
      Element elemento;
      if (!isAj) {
        new_gdc_link = leggiValoreXML(dInput, "REDIRECT");
        if ((new_gdc_link != null) && (new_gdc_link.length() != 0)) {
          gdc_link = new_gdc_link;
        }
        String fRed = leggiValoreXML(dInput, "FORCE_REDIRECT");
        if ((fRed !=null) && (fRed.equalsIgnoreCase("Y"))) {
          forceRedirect = true;
          gdc_link = new_gdc_link;
          new_gdc_link = null;
        }

//        root = dInput.getRootElement();
        Element eVal = leggiElementoXML(root, "VALUE_REDIRECT");
//        Element elem;
        String dato = "";
        String val = "";
        Dati valori = null;
        if ( eVal != null) {
          valori = new Dati();
          for(Iterator iterator = eVal.elementIterator(); iterator != null && iterator.hasNext();) {
            elem = (Element)iterator.next();
            if(elem != null) {
              dato = elem.getName();
              val = elem.getText();
              if (val == null) {
                val = "";
              }
              valori.aggiungiDato(dato,val);
            }
          }
        }
        request.getSession().setAttribute("gdm_valori_redirect",valori);

        String newData = "";
        Element eAgg = leggiElementoXML(root, "DATI_AGGIORNAMENTO");
        if (eAgg != null) {
          newData = leggiValoreXML(eAgg, "DATA");
          if ((newData !=null) && (newData.length() != 0)) {
            aggData = newData;
            settaDataAggiornamentoModello(request, aggData);
          }
          idCartnew = leggiValoreXML(eAgg, "ID_CARTELLA");
        }

        Element eDoc = leggiElementoXML(root, "DOC");
        String campo = "";
        String valore = "";
        if (modifica) {
          if ((newData !=null) && (newData.length() != 0)) {
            if (ad_1 != null) {
                ad_1.setUltAggiornamento(newData);
            }
            if (ad_2 != null) {
                ad_2.setUltAggiornamento(newData);
            }
          }
          Dati dati = new Dati();
          for(Iterator iterator = eDoc.elementIterator(); iterator != null && iterator.hasNext();) {
            elemento = (Element)iterator.next();
            if(elemento != null) {
              campo = elemento.getName();
              valore = elemento.getText();
              if (valore == null) {
                valore = "";
              }
              if (ad_1 != null) {
                  ad_1.aggiungiDati(campo,valore);
              }
              if (ad_2 != null) {
                  ad_2.aggiornaDati(campo,valore);
              }
              dati.aggiungiDato(campo,valore);
            }
          }
          request.getSession().setAttribute("gdm_nuovi_valori_doc",dati);
        }
      } else {
        root = dInput.getRootElement();
        String newData = "";
        Element eAgg = leggiElementoXML(root, "DATI_AGGIORNAMENTO");
        if (eAgg != null) {
          newData = leggiValoreXML(eAgg, "DATA");
          if ((newData !=null) && (newData.length() != 0)) {
            aggData = newData;
            settaDataAggiornamentoModello(request, aggData);
          }
        }

      }

//      closeSync(sync);
      Element jSync = leggiElementoXML(root, "JSYNC");
      if (jSync != null ) {
        elemento = leggiElementoXML(jSync,"AREA");
        newArea = elemento.getText();
        elemento = leggiElementoXML(jSync,"CONTROLLO");
        newControllo = elemento.getText();
      } else {
        newArea = area;
        newControllo = controllo;
      }

      Element listaId = leggiElementoXML(root, "LISTAID");
      boolean okrun = true;
//      if (listaId == null && sblocco.equalsIgnoreCase("S")) {
      if (sblocco.equalsIgnoreCase("S")) {
        for (Integer intObj : ite) {
  			okrun = okrun
  					&& (sync.executed(intObj.intValue(),us) == SyncSuite.SYNC_ESEGUITO);
        }
        if (okrun) {
          sync.commit();
        } else {
          sync.rollback();
        }
        closeSync(sync);
      } else {
        closeSync(sync);
        String msg="";
        String msgElencoError="";
        String msgElenco="";
        String tipoObj = "";
        String idObj = "", idOggetto = "", idUtente = "";
        Element idError = null;
        String idMsg = "";
//        msg ="Avviso:<br>";
        if (listaId != null) {
          for(Iterator iterator = listaId.elementIterator(); iterator != null && iterator.hasNext();) {
            elemento  = (Element)iterator.next();
            tipoObj   = leggiValoreXML(elemento,"TIPOOGGETTO");
            idObj     = leggiValoreXML(elemento,"IDOGGETTO");
            idError   = leggiElementoXML(elemento,"ERROR");
            idMsg     = leggiValoreXML(elemento,"MSG");
            idUtente  = leggiValoreXML(elemento,"UTENTE");
            if (idUtente == null || idUtente.length() == 0) {
              idUtente = us;
            }
            okrun = true;

            if (idError == null) {
    //        Distinguiamo id in base al tipo oggetto
              if((tipoObj.equals("C")) || (tipoObj.equals("X"))) {
                idOggetto=(new DocUtil(vu)).getIdViewCartellaByIdCartella(idObj);
              } else {
                if(tipoObj.equals("D")) {
                  idOggetto=(new DocUtil(vu)).getAreaCmCrByIdDocumento(idObj);
                } else {
                  idOggetto=idObj;
                }
              }
              sync = new SyncSuite(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
              ite = (Vector<Integer>)sync.isExecutable(newArea ,newControllo,idOggetto,idUtente);
              syncErr = sync.getLastError();
              if (syncErr != null || ite == null) {
                msgElencoError +=" - "+syncErr+"<br>";
                closeSync(sync);
              } else {
                for (Integer intObj : ite) {
                  okrun = okrun
                      && (sync.executed(intObj.intValue(),us) == SyncSuite.SYNC_ESEGUITO);
                }
                if (okrun) {
                  msgElenco +=" - "+idMsg+"<br>";
                  sync.commit();
                } else {
                  sync.rollback();
                }
                closeSync(sync);
              }
            } else {
              msgElencoError +=" - "+idMsg+"<br>";
            }
          }
//          if (msgElencoError.length() == 0) {
////          msg+="Le operazioni sono state tutte eseguite correttamente!<br>";
//            msg+=msgElenco;
//          } else {
          if(msgElenco!="") {
            msg+="<br>Elenco delle operazioni eseguite correttamente:<br>";
            msg+=msgElenco;
          }
          if(msgElencoError!="") {
            msg+="<br>Elenco delle operazioni non eseguite correttamente:<br>";
            msg+=msgElencoError;
          }
//          }
        }
        if (isAj) {
          retval = msg;
        } else {
          if (new_gdc_link != null && jSync != null) {
  //        String sParametri="msg="+URLEncoder.encode(msg,"ISO8859-1");
            request.getSession().setAttribute("msg",msg);
			String sParametri ="";
            if (!new_gdc_link.equals("NOREDIRECT")) sParametri ="?redirect="+URLEncoder.encode(new_gdc_link,"ISO8859-1");
            forceRedirect = true;
            gdc_link = "../common/MessagePage.do"+sParametri;
          }
        }
      }

      if (isAjax) {
        retval = result;
      }
    } catch (Exception e) {
      closeSync(sync);
      loggerError("ServletModulistica::eseguiControllo() - Area: "+area+" - Modello: "+cm+"- Errore:  ["+ e.toString()+"]",e);
      result = "";
      iterPuls = "";
      if (isAj) {
//        retval = "Errore_GDM_Ajax";
      	retval = "<FUNCTION_OUTPUT><RESULT>nonok</RESULT><ERROR>Errore action: "+e.toString()+"</ERROR></FUNCTION_OUTPUT>";
      } else {
        retval += "Errore in fase di controllo!";
        if (Parametri.DEBUG.equalsIgnoreCase("1")) {
          retval += e.toString();
        }
        if (Parametri.DEBUG.equalsIgnoreCase("2")) {
          retval += e.getStackTrace().toString();
        }
      }
    }
    //Debug Tempo
    stampaTempo("Modulistica::eseguiControllo - Fine",area,cm,cr,ptime);
    //Debug Tempo

    return retval;
  }

  /**
   *
   */
  private String pulsantePremuto(HttpServletRequest request,
                               String area,
                               String cm,
                               String fase,
                               String cr,
                               String lettura) {

    //Debug Tempo
    long ptime = stampaTempo("Modulistica::pulsantePremuto - Inizio",area,cm,cr,0);
    //Debug Tempo

    IDbOperationSQL  dbOp = null;
    ResultSet       rst = null;
    boolean         trovato = false;
    String etichetta, controllo, salvataggio;
    String retval = fase;

    String query = "SELECT ETICHETTA, CONTROLLO, SALVATAGGIO, ITER_FLUSSO "+
                   "  FROM ETICHETTE "+
                   " WHERE AREA = :AREA "+
                   "   AND (CODICE_MODELLO = :CM OR CODICE_MODELLO = '-') "+
                   "   AND (CONTROLLO IS NOT NULL OR ITER_FLUSSO IS NOT NULL)";

//    String query = "SELECT ETICHETTA, CONTROLLO, SALVATAGGIO, ITER_FLUSSO "+
//                  "  FROM ETICHETTE "+
//                  " WHERE AREA = '"+area+"' "+
//                  "   AND (CODICE_MODELLO = '"+cm+"' OR CODICE_MODELLO = '-') "+
//                  "   AND (CONTROLLO IS NOT NULL OR ITER_FLUSSO IS NOT NULL)";

    iterPuls = "";
    try {
      dbOp = vu.getDbOp();
      dbOp.setStatement(query);
      dbOp.setParameter(":AREA", area);
      dbOp.setParameter(":CM", cm);
      dbOp.execute();
      rst = dbOp.getRstSet();
      List<Controllo> listaControlli = new ArrayList<Controllo>();

      while (rst.next()) {
        etichetta = rst.getString("ETICHETTA");
        controllo = rst.getString("CONTROLLO");
        salvataggio = rst.getString("SALVATAGGIO");
        iterPuls = rst.getString("ITER_FLUSSO");

        listaControlli.add(new Controllo(etichetta, controllo, iterPuls,salvataggio));
      }

      for(int indexListaControlli =0;indexListaControlli<listaControlli.size();indexListaControlli++) {
        if (trovato) break;

        etichetta   = listaControlli.get(indexListaControlli).getEtichetta();
        controllo   = listaControlli.get(indexListaControlli).getControllo();
        salvataggio = listaControlli.get(indexListaControlli).getSalvataggio();
        iterPuls    = listaControlli.get(indexListaControlli).getIterPuls();
        if (iterPuls == null) {
          iterPuls = "";
        }
        if (controllo == null) {
          controllo = "-";
        }
        if (request.getParameter("$B$"+etichetta) != null) {
          String iddoc = ricercaIdDocumento(request,null);
          oldValue(request,controllo,area,cm,cr,iddoc);
          if (salvataggio.equalsIgnoreCase("NO")) {
        	String errore = eseguiControllo(request,controllo,true,null,null,cr,iddoc);
        	if (errore.length() > 0) {
            	erroreAction = errore;
//        		corpoHtml += corpoHtml.replaceFirst("<!--ERROREACTION-->", "<span class='AFCErrorDataTD'>"+errore+"</span>");
        	}

            istanziaIter(request);
            if (lettura.equalsIgnoreCase("W")) {
              retval = "1";
            } else {
              retval = "open";
            }
            trovato = true;
          }
          if (salvataggio.equalsIgnoreCase("PS")) {
            pulsPrima = controllo;
            trovato = true;
            retval = "salvataggio";
          }
          if (salvataggio.equalsIgnoreCase("DS")) {
            pulsDopo = controllo;
            trovato = true;
            retval = "salvataggio";
          }
          if (salvataggio.equalsIgnoreCase("PR")) {
            pulsPrima = controllo;
            trovato = true;
            retval = "submit";
          }
          if (salvataggio.equalsIgnoreCase("DR")) {
            pulsDopo = controllo;
            trovato = true;
            retval = "submit";
          }
        }
      }
    } catch (Exception e) {
      loggerError("ServletModulistica::pulsantePremuto() - Area: "+area+" - Modello: "+cm+"- Errore:  ["+ e.toString()+"]",e);
      corpoHtml += "<span class='AFCErrorDataTD'>"+"Errore in fase di controllo!"+"</span>";
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        corpoHtml += e.toString();
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        corpoHtml += e.getStackTrace().toString();
      }
    }
    //Debug Tempo
    stampaTempo("Modulistica::pulsantePremuto - Fine",area,cm,cr,ptime);
    //Debug Tempo
    return retval;
  }

  /**
   *
   */
  private String sysPdf(String iddoc, String area, String cm) {
    IDbOperationSQL dbOp = null;
    ResultSet rst;
    String query = "";
    String retval = "";

    query = "SELECT OGFI.ID_OGGETTO_FILE " +
            "FROM OGGETTI_FILE OGFI, FORMATI_FILE FOFI " +
            "WHERE OGFI.ID_FORMATO = FOFI.ID_FORMATO "+
            "AND FOFI.NOME = 'SYS_PDF' "+
            "AND OGFI.ID_DOCUMENTO = :IDDOC ";

    try {
      dbOp = vu.getDbOp();
      dbOp.setStatement(query);
      dbOp.setParameter(":IDDOC", iddoc);
      dbOp.execute();
      rst = dbOp.getRstSet();
      if (rst.next()) {
        retval = rst.getString(1);
        if (retval == null) {
          retval = "";
        }
      }
    } catch (Exception e) {
      loggerError("ServletModulistica::sysPdf() - Area: "+area+" - Modello: "+cm+"- Errore:  ["+ e.toString()+"]",e);
      corpoHtml += "<span class='AFCErrorDataTD'>"+"Errore in fase di controllo!"+"</span>";
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        corpoHtml += e.toString();
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        corpoHtml += e.getStackTrace().toString();
      }
    }
    return retval;
  }

  /**
   *
   */
  private void istanziaIter(HttpServletRequest request) {
  	istanziaIter(request,iterPuls);
  }

    /**
     *
     */
    private void istanziaIter(HttpServletRequest request, String iter) {
    IDbOperationSQL  dbOp = null;
//    ResultSet       rst = null;
    String area, cm, prepFunc;
    String cr;

    if (iterPuls.length() == 0) {
     return;
    }
    area = request.getParameter("area");
    cm = request.getParameter("cm");
    int retval = 0;
    cr   = request.getParameter("cr");
    if (cr == null) {
      cr = (String)request.getSession().getAttribute("key");
    }
    //Debug Tempo
    long ptime = stampaTempo("Modulistica::istanziaIter - Inizio",area,cm,cr,0);
    //Debug Tempo
    prepFunc = "JWF_UTILITY.istanzia_iter(null, '"+iter+"','area="+area+"#@#cm="+cm+"#@#cr="+cr+"#@#$DOCMASTER="+area+"@"+cm+"@"+cr+"#@#utente="+(String)request.getSession().getAttribute("UtenteGDM")+"',null ,'"+Parametri.USER+"' )";
    try {
      dbOp = vu.getDbOp();

      dbOp.setCallFunc(prepFunc);
      dbOp.execute();
      retval = dbOp.getCallSql().getInt(1);
      if (retval == -1) {
        logger.error("ServletModulistica::istanziaIter() - Area: "+area+" - Modello: "+cm+"- Errore:  [Iter non istanziato!]");
        corpoHtml += "<span class='AFCErrorDataTD'>"+"Errore in fase di istanzazione Iter!";
        corpoHtml += "<br/>Iter non istanziato!</span>";
      }

    } catch (Exception e) {

      loggerError("ServletModulistica::istanziaIter() - Area: "+area+" - Modello: "+cm+"- Errore:  ["+ e.toString()+"]",e);
      corpoHtml += "<span class='AFCErrorDataTD'>"+"Errore in fase di istanzazione Iter!"+"</span>";
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        corpoHtml += e.toString();
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        corpoHtml += e.getStackTrace().toString();
      }
    }
    //Debug Tempo
    stampaTempo("Modulistica::istanziaIter - Fine",area,cm,cr,ptime);
    //Debug Tempo

  }

  /**
   *
   */
  private void loggerError(String sMsg, Exception e) {
      if (!Parametri.DEBUG.equalsIgnoreCase("2")) {
        logger.error(sMsg);
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        logger.error(sMsg,e);
      }
  }

    private static String leggiValoreXML(Document xmlDocument, String tagName)
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
                valore = leggiValoreXML(elemento, tagName);
        }

        return valore;
    }

    private static String leggiValoreXML(Element e, String tagName)
    {
        String valore = null;
        for(Iterator iterator = e.elementIterator(); iterator != null && iterator.hasNext() && valore == null;)
        {
            Element elemento = (Element)iterator.next();
            if(elemento != null && elemento.getName().equals(tagName))
                valore = elemento.getText();
            else
                valore = leggiValoreXML(elemento, tagName);
        }

        return valore;
    }

    private static Element leggiElementoXML(Element e, String tagName)
    {
        Element elemento = null, eFound = null;;
        for(Iterator iterator = e.elementIterator(); iterator != null && iterator.hasNext() && eFound == null;)
        {
            elemento = (Element)iterator.next();
            if(elemento != null && elemento.getName().equals(tagName)) {
               eFound = elemento;
            } else {
                eFound = leggiElementoXML(elemento, tagName);
                if ( eFound != null) {
                  return eFound;
                }
            }
        }

        return eFound;
    }

  private Element aggFiglio(Element elp, String nome, String valore) {
    Element elf = DocumentHelper.createElement(nome);
    if (valore != null) {
    	elf.setText(valore);
    }
    elp.add(elf);
    return elp;
  }

  /**
   * Funzione che ritorna il codice del modello successiovo se presente
   */
  public String getModelloSuccessivo() {
    return modSuccessivo;
  }

  /**
   *
   */
  private void closeSync(SyncSuite sync) {
    try {
      sync.close();
    } catch (Exception e) {}
  }

  private String creaXML(HttpServletRequest request,
                      		String listaid,
                          String area,
                          String cm,
                          String cr) throws Exception {
  	String retval = "";
    Element root, elp, elo, elid;
    String myUrl = "";
    String myQuery = "";
    String contextPath = "";

    //Debug Tempo
    long ptime = stampaTempo("Modulistica::oldValue - Inizio",area,cm,cr,0);
    //Debug Tempo
    myQuery = request.getQueryString();
    String idCart = request.getParameter("idCartProveninez");
    if (idCart == null) {
      idCart = "";
    }

    String wrkSpace = request.getParameter("TipoWorkSpace");
    if (wrkSpace == null) {
      wrkSpace = "";
    }
    String idQuery = request.getParameter("idQueryProveninez");
    if (idQuery == null) {
      idQuery = "-1";
    }

    String us = (String)request.getSession().getAttribute("Utente");
    if (us == null) {
      us = "";
    }

    String ruolo = (String)request.getSession().getAttribute("Ruolo");
    if (ruolo == null) {
      ruolo = "";
    }

    String modulo = (String)request.getSession().getAttribute("Modulo");
    if (modulo == null) {
      modulo = "";
    }

    String istanza = (String)request.getSession().getAttribute("Istanza");
    if (istanza == null) {
      istanza = "";
    }

    String nominativo = (String)request.getSession().getAttribute("UserLogin");
    if (nominativo == null) {
      nominativo = "";
    }

    if (Parametri.PROTOCOLLO.length() == 0) {
      myUrl = request.getScheme();
    } else {
      myUrl = Parametri.PROTOCOLLO;
    }
    if (Parametri.SERVERNAME.length() == 0) {
      myUrl += "://"+request.getServerName();
    } else {
      myUrl += "://"+Parametri.SERVERNAME;
    }
    if (Parametri.SERVERPORT.length() == 0) {
      myUrl += ":"+request.getServerPort();
    } else {
      myUrl += ":"+Parametri.SERVERPORT;
    }

    contextPath = request.getContextPath();
    try {
      root = DocumentHelper.createElement("FUNCTION_INPUT");
      Document dDoc = DocumentHelper.createDocument();
      Document dDoc_popup = DocumentHelper.createDocument();
      dDoc.setRootElement(root);

      elp = DocumentHelper.createElement("CONNESSIONE_DB");
      elp = aggFiglio(elp,"USER",Parametri.USER);
      elp = aggFiglio(elp,"PASSWORD",Parametri.PASSWD);
      elp = aggFiglio(elp,"HOST_STRING",Parametri.SPORTELLO_DSN);
      elp = aggFiglio(elp,"ALIAS",Parametri.ALIAS);
      root.add(elp);

      elp = DocumentHelper.createElement("CONNESSIONE_TOMCAT");
      elp = aggFiglio(elp,"UTENTE",us);
      elp = aggFiglio(elp,"NOMINATIVO",nominativo);
      elp = aggFiglio(elp,"RUOLO",ruolo);
      elp = aggFiglio(elp,"MODULO",modulo);
      elp = aggFiglio(elp,"ISTANZA",istanza);
      elp = aggFiglio(elp,"PROPERTIES",inifile.replaceAll("\\\\","/"));
      elp = aggFiglio(elp,"URL_SERVER",myUrl);
      elp = aggFiglio(elp,"CONTEXT_PATH",contextPath);
      root.add(elp);

      elp = DocumentHelper.createElement("CLIENT_GDM");
      elid = DocumentHelper.createElement("LISTAID");
      StringTokenizer st = new StringTokenizer(listaid, "@");
      while (st.hasMoreTokens()) {
      	String id = st.nextToken();
      	elo = DocumentHelper.createElement("ID");
      	elo = aggFiglio(elo, "TIPOOGGETTO", "D");
      	elo = aggFiglio(elo, "IDOGGETTO", id);
      	elid.add(elo);
      }
      elp.add(elid);

      elp = aggFiglio(elp,"IDCARTPROVENINEZ",idCart);
      elp = aggFiglio(elp,"TIPOWORKSPACE",wrkSpace);
      elp = aggFiglio(elp,"IDQUERYPROVENINEZ",idQuery);
      root.add(elp);
      elp = DocumentHelper.createElement("ERROR");
      root.add(elp);
      elp = DocumentHelper.createElement("DOC");
      root.add(elp);
      retval = root.asXML();
    } catch (Exception e) {
      loggerError("ServletModulistica::creaXML() - Area: "+area+" - Modello: "+cm+" - Errore:  ["+ e.toString()+"]",e);
      //Debug Tempo
      stampaTempo("Modulistica::creaXML - Fine",area,cm,cr,ptime);
      //Debug Tempo
      throw e;
    }

  	return retval;
  }

  private void oldValue(HttpServletRequest request,
                        String controllo,
                        String area,
                        String cm,
                        String cr,
                        String iddoc) throws Exception {
    //Debug Tempo
    long ptime = stampaTempo("Modulistica::oldValue - Inizio",area,cm,cr,0);
    //Debug Tempo
    IDbOperationSQL dbOpD = null;
    ResultSet rstD = null;
    IDbOperationSQL dbOp = null;
    ResultSet rst = null;
    Element root, elp, elo;
    String queryC = "";
    String errMsg = "";
    String campi = "";
    String myUrl = "";
    String myQuery = "";
    String contextPath = "";

    myQuery = request.getQueryString();
    String wFather = request.getParameter("wfather");
    if (wFather == null) {
      wFather = "";
    }

    String idCart = request.getParameter("idCartProveninez");
    if (idCart == null) {
      idCart = "";
    }

    String wrkSpace = request.getParameter("TipoWorkSpace");
    if (wrkSpace == null) {
      wrkSpace = "";
    }
    String idQuery = request.getParameter("idQueryProveninez");
    if (idQuery == null) {
      idQuery = "-1";
    }

    String us = (String)request.getSession().getAttribute("Utente");
    if (us == null) {
      us = "";
    }

    String ruolo = (String)request.getSession().getAttribute("Ruolo");
    if (ruolo == null) {
      ruolo = "";
    }

    String modulo = (String)request.getSession().getAttribute("Modulo");
    if (modulo == null) {
      modulo = "";
    }

    String istanza = (String)request.getSession().getAttribute("Istanza");
    if (istanza == null) {
      istanza = "";
    }

    String nominativo = (String)request.getSession().getAttribute("UserLogin");
    if (nominativo == null) {
      nominativo = "";
    }

    if (Parametri.PROTOCOLLO.length() == 0) {
      myUrl = request.getScheme();
    } else {
      myUrl = Parametri.PROTOCOLLO;
    }
    if (Parametri.SERVERNAME.length() == 0) {
      myUrl += "://"+request.getServerName();
    } else {
      myUrl += "://"+Parametri.SERVERNAME;
    }
    if (Parametri.SERVERPORT.length() == 0) {
      myUrl += ":"+request.getServerPort();
    } else {
      myUrl += ":"+Parametri.SERVERPORT;
    }

    contextPath = request.getContextPath();


    try {
      root = DocumentHelper.createElement("FUNCTION_INPUT");
      Document dDoc = DocumentHelper.createDocument();
      Document dDoc_popup = DocumentHelper.createDocument();
      dDoc.setRootElement(root);

      elp = DocumentHelper.createElement("CONNESSIONE_DB");
      elp = aggFiglio(elp,"USER",Parametri.USER);
      elp = aggFiglio(elp,"PASSWORD",Parametri.PASSWD);
      elp = aggFiglio(elp,"HOST_STRING",Parametri.SPORTELLO_DSN);
      elp = aggFiglio(elp,"ALIAS",Parametri.ALIAS);
      root.add(elp);

      elp = DocumentHelper.createElement("CONNESSIONE_TOMCAT");
      elp = aggFiglio(elp,"UTENTE",us);
      elp = aggFiglio(elp,"NOMINATIVO",nominativo);
      elp = aggFiglio(elp,"RUOLO",ruolo);
      elp = aggFiglio(elp,"MODULO",modulo);
      elp = aggFiglio(elp,"ISTANZA",istanza);
      elp = aggFiglio(elp,"PROPERTIES",inifile.replaceAll("\\\\","/"));
      elp = aggFiglio(elp,"URL_SERVER",myUrl);
      elp = aggFiglio(elp,"CONTEXT_PATH",contextPath);
      root.add(elp);

      elp = DocumentHelper.createElement("CLIENT_GDM");
      elp = aggFiglio(elp,"AREA",area);
      elp = aggFiglio(elp,"CODICE_MODELLO",cm);
      elp = aggFiglio(elp,"CODICE_RICHIESTA",cr);
      elp = aggFiglio(elp,"RW",lettura);
      elp = aggFiglio(elp,"GDC_LINK",gdc_link);
      elp = aggFiglio(elp,"WFATHER",wFather);
      elp = aggFiglio(elp,"QUERYSTRING",myQuery);
      elp = aggFiglio(elp,"IDCARTPROVENINEZ",idCart);
      elp = aggFiglio(elp,"TIPOWORKSPACE",wrkSpace);
      elp = aggFiglio(elp,"IDQUERYPROVENINEZ",idQuery);
      if (mdg != null) {
      	if (mdg.getUltimoAgg().length() != 0) {
      		elp = aggFiglio(elp, "DATA_AGGIORNAMENTO", mdg.getUltimoAgg());
      	} else {
      		elp = aggFiglio(elp, "DATA_AGGIORNAMENTO", "");
      	}
      } else {
      	elp = aggFiglio(elp, "DATA_AGGIORNAMENTO", "");
      }
      root.add(elp);

      elp = DocumentHelper.createElement("JSYNC");
      elp = aggFiglio(elp,"AREA",area);
      elp = aggFiglio(elp,"CONTROLLO",controllo);
      root.add(elp);

      queryC = "SELECT CORPO, DRIVER, CONNESSIONE, UTENTE, PASSWD, CAMPI, MSG_ERRORE, DSN "+
               "FROM LIBRERIA_CONTROLLI "+
               "WHERE AREA = :AREA AND "+
               "CONTROLLO = :CONTROLLO ";

      dbOp = vu.getDbOp();
      dbOp.setStatement(queryC);
      dbOp.setParameter(":AREA", area);
      dbOp.setParameter(":CONTROLLO", controllo);
      dbOp.execute();
      rst = dbOp.getRstSet();
      if (rst.next()) {
        errMsg = rst.getString("MSG_ERRORE");
        if (errMsg == null) {
          errMsg = "";
        }
        elp = DocumentHelper.createElement("ERROR");
        elp.setText(errMsg);
        root.add(elp);
        elp = DocumentHelper.createElement("DOC");
        elo = DocumentHelper.createElement("DOC_OLD");
        campi = rst.getString("CAMPI");
        if (campi == null) {
          campi = "";
        }
        String sCampo = "", valore="";
        AccediDocumento ad = null;
        if (iddoc != null) {
          ad = new AccediDocumento(iddoc,vu);
          ad.accediDocumentoValori();
        }
        if (campi.equalsIgnoreCase("%")) {

          String queryD = "SELECT   DATO "+
               "FROM DATI_MODELLO "+
               "WHERE    AREA = :AREA AND "+
                "CODICE_MODELLO = :CM AND "+
                "NVL(IN_USO,'Y') = 'Y' AND "+
                "(DATO NOT LIKE '$%' "+
                "OR DATO IN ('$BARCODE1','$BARCODE2','$BARCODE3','$MASTER')) "+
                "ORDER BY DATO ASC";
          dbOpD = vu.getDbOp();
          dbOpD.setStatement(queryD);
          dbOpD.setParameter(":AREA", area);
          dbOpD.setParameter(":CM", cm);
          dbOpD.execute();
          rstD = dbOpD.getRstSet();

          List<String> listaCampi = new ArrayList<String>();
          while (rstD.next()) {
            listaCampi.add(rstD.getString(1));
          }

          for(int indexCampi =0; indexCampi<listaCampi.size();indexCampi++) {
            sCampo = listaCampi.get(indexCampi);
//            if (sCampo.equalsIgnoreCase("FASCICOLO_OGGETTO")) {
//              String sCampo2 = sCampo;
//            }
            elp = aggFiglio(elp,sCampo,leggiValore(request,sCampo));
            if (ad != null) {
              valore = ad.leggiValoreCampo(sCampo);
              if (valore == null) {
                valore = "";
              }
            } else {
              valore = "";
            }
            elo = aggFiglio(elo,sCampo,valore);
          }

        } else {
          StringTokenizer st = new StringTokenizer(campi, Parametri.SEPARAVALORI);
          String nextToken = "";
          while (st.hasMoreTokens())  {
            nextToken = st.nextToken();
            elp = aggFiglio(elp,nextToken,leggiValore(request,nextToken));
            if (ad != null) {
              valore = ad.leggiValoreCampo(nextToken);
              if (valore == null) {
                valore = "";
              }
            } else {
              valore = "";
            }
            elo = aggFiglio(elo,nextToken,valore);
          }
        }
        root.add(elp);
        root.add(elo);
        sFunInput = dDoc.asXML();
//        loggerError(sFunInput,null);
      } else {
        elp = DocumentHelper.createElement("ERROR");
        elp.setText(errMsg);
        root.add(elp);
        elp = DocumentHelper.createElement("DOC");
        elp.setText(errMsg);
        root.add(elp);
        elp = DocumentHelper.createElement("DOC_OLD");
        elp.setText(errMsg);
        root.add(elp);
      	sFunInput = dDoc.asXML();
      }

    } catch (Exception e) {
      loggerError("ServletModulistica::oldvalue() - Area: "+area+" - Modello: "+cm+"- Controllo: "+controllo+" - Errore:  ["+ e.toString()+"]",e);
      //Debug Tempo
      stampaTempo("Modulistica::oldValue - Fine",area,cm,cr,ptime);
      //Debug Tempo
      throw e;
    }
    finally {
      try {rst.close();}catch (Exception e1) { }
      try {rstD.close();}catch (Exception e1) { }
    }
    //Debug Tempo
    stampaTempo("Modulistica::oldValue - Fine",area,cm,cr,ptime);
    //Debug Tempo
  }

  private long stampaTempo(String sMsg, String area, String cm, String cr, long ptime) {
    if (!debuglog) {
      return 0;
    }
    long adesso = Calendar.getInstance().getTimeInMillis();
    long trascorso = 0;
    if (ptime > 0) {
      trascorso = adesso - ptime;
    }
    long min_time = Long.parseLong(Parametri.MIN_TIME_LOG);
    if (trascorso < min_time) {
      return adesso;
    }
    if (Parametri.DEBUG.equalsIgnoreCase("1") && ptime > 0) {
      logger.debug("\n"+sMsg+"\n-Area:"+area+" -CodiceModello:"+cm+" -Codice Richiesta:"+cr+"\n-------->Trascorso dall'inizio: "+trascorso+"\n");
    }
    if (Parametri.DEBUG.equalsIgnoreCase("2")) {
      logger.debug("\n"+sMsg+"\n-Area:"+area+" -CodiceModello:"+cm+" -Codice Richiesta:"+cr+"\n----->TIME: "+adesso+"\n--------Trascorso dall'inizio: "+trascorso+"\n");
    }
    return adesso;
  }

  private long stampaTempo2(String sMsg, String area, String cm, String cr, long ptime) {
    long adesso = Calendar.getInstance().getTimeInMillis();
    long trascorso = 0;
    if (ptime > 0) {
      trascorso = adesso - ptime;
    }
    long min_time = Long.parseLong(Parametri.MIN_TIME_LOG);
    if (trascorso < min_time) {
      return adesso;
    }
    if (Parametri.STAMPA_TEMPO.equalsIgnoreCase("1") && ptime > 0) {
      logger.error("\n"+sMsg+"\n-Area:"+area+" -CodiceModello:"+cm+" -Codice Richiesta:"+cr+"\n-------->Trascorso dall'inizio: "+trascorso+"\n");
    }
    if (Parametri.STAMPA_TEMPO.equalsIgnoreCase("2")) {
      logger.error("\n"+sMsg+"\n-Area:"+area+" -CodiceModello:"+cm+" -Codice Richiesta:"+cr+"\n----->TIME: "+adesso+"\n--------Trascorso dall'inizio: "+trascorso+"\n");
    }
    return adesso;
  }

  private void effettuaRicerca(HttpServletRequest request) {
    Element root,
            elp;
    String  area,
            cm,
            cr,
            dato,
            tipo,
            operatore,
            ordinamento,
            valore,
            valore_2,
            errore,
            list_field_err,
            ads_msg_err,
            area_dato,
            modello_dato,
            categoria_dato,
            select,
            union,
            rigo,
            formato,
            utente;
    int     inizio,
            fine,
            other_cond = 0,
            m_prot_cond = 0;

    IDbOperationSQL dbOp  = null;
    ResultSet       rs    = null;
    ModelloHTMLIn   mdIn  = null;
    HttpSession     httpSess = request.getSession();
    Dati dati = new Dati();

    area = request.getParameter("area");
    cm   = request.getParameter("cm");
    cr   = request.getParameter("cr");
    if (cr == null) {
      cr = (String)httpSess.getAttribute("key");
    }

    String master_filtro = request.getParameter("_MASTER_FILTRO");
    utente = (String)httpSess.getAttribute("UtenteGDM");

    //Debug Tempo
    long ptime = stampaTempo("Modulistica::effettuaRicerca - Inizio",area,cm,cr,0);
    //Debug Tempo

    try {
      mdIn = (ModelloHTMLIn)cercaModello(httpSess, area, cm, cr);

      if (mdIn == null) {
        corpoHtml += "<span class='AFCErrorDataTD'>"+"Attenzione! Si � verificato un errore.<br/>Impossibile continuare: modello non trovato.</span>";
        //Debug Tempo
        stampaTempo("Modulistica::effettuaRicerca - Fine",area,cm,cr,ptime);
        //Debug Tempo
        return;
      }

      select = mdIn.getIstruzioni();
      if (select == null) {
        select = "";
      }
      int u = select.toUpperCase().indexOf("UNION");
      if (u > -1) {
        union = select.substring(u, select.length());
        select = select.substring(0, u);
      } else {
        union =  "";
      }
      errore = controlliModello(request, area, cm, cr, null);
      if (errore == null) {
        errore = "";
      }
      inizio = errore.indexOf("ADS_MSG_ERROR=");
      fine = errore.indexOf("LIST_FIELDS_ERROR=");
      if (inizio > -1) {
        if (fine == -1) {
          fine = errore.length();
          list_field_err = "";
        } else {
          list_field_err = errore.substring(fine+18);
        }
        ads_msg_err = errore.substring(inizio+14,fine);
      } else {
        if (errore.length() == 0) {
          ads_msg_err = "";
          list_field_err = "";
        } else {
          int x = errore.indexOf("?")+1;
          String urlPar = errore.substring(x);
          String urlContr = errore.substring(0,x) + urlParam(urlPar,null,cr);
          corpoHtml += "<html>";
          corpoHtml += "<head><title>ServletVisualizza</title>";
          corpoHtml += "<meta http-equiv='refresh' content='0; URL="+urlContr+"' />";
          corpoHtml += "</head><body>";
          corpoHtml += "</body></html>";
          //Debug Tempo
          stampaTempo("Modulistica::effettuaRicerca - Fine",area,cm,cr,ptime);
          //Debug Tempo
          return;
        }
      }
//      if (!ads_msg_err.length() == 0 || !list_field_err.length() == 0) {
//        mdIn.settaErrMsg(ads_msg_err);
//        mdIn.settaListFields(list_field_err);
//        mdIn.setNewRequest(request);
//        costruisciModulo(request, mdIn.getQueryURL(),true);
//        //Debug Tempo
//        stampaTempo("Modulistica::effettuaRicerca - Fine",area,cm,cr,ptime);
//        //Debug Tempo
//        return;
//      }

      root = DocumentHelper.createElement("DOC_INFO");
      root.addAttribute("xmlns:xsi","http//www.w3.org/2000/10/XMLSchema-instance");
      root.addAttribute("xsi:noNamespaceSchemaLocation","doc_info_v1.4.1.xsd");
      Document dDoc = DocumentHelper.createDocument();
      dDoc.setRootElement(root);
      elp = DocumentHelper.createElement("AREA");
      String area_filtro = request.getParameter("_AREA_FILTRO");
      if (area_filtro == null) {
        area_filtro = area;
      }
      elp.addAttribute("value",area_filtro);
      root.add(elp);

      String modelli_filtro = request.getParameter("_MODELLI_FILTRO");
      if (modelli_filtro == null) {
        modelli_filtro = "";
      }
      StringTokenizer st = new StringTokenizer(modelli_filtro,Parametri.SEPARAVALORI);
      while (st.hasMoreTokens()) {
        elp = DocumentHelper.createElement("TIPO_DOC");
        elp.addAttribute("version","1.1");
        elp.addAttribute("value",st.nextToken());
        root.add(elp);
      }

      String cm_filtro = request.getParameter("_CM_FILTRO");
      if (cm_filtro == null) {
        cm_filtro = "";
      }
      if (cm_filtro.length() != 0) {
        elp = DocumentHelper.createElement("TYPERETURN");
        elp.addAttribute("area",area_filtro);
        elp.addAttribute("cm",cm_filtro);
        root.add(elp);
      }

      String cate_filtro = request.getParameter("_CATE_FILTRO");
      if (cate_filtro == null) {
        cate_filtro = "";
      }
      if (cate_filtro.length() != 0) {
        elp = DocumentHelper.createElement("TYPERETURN");
        elp.addAttribute("categoria",cate_filtro);
        root.add(elp);
      }

      String query =  "SELECT   DM.DATO, D.TIPO, D.FORMATO_DATA "+
                      "FROM DATI_MODELLO DM, DATI D "+
                      "WHERE    DM.AREA = :AREA AND "+
                      "DM.CODICE_MODELLO = :CODICE_MODELLO AND "+
                      "DM.AREA_DATO = D.AREA AND "+
                      "DM.DATO = D.DATO "+
                      "ORDER BY DM.DATO ASC";

      dbOp = vu.getDbOp();
      dbOp.setStatement(query);
      dbOp.setParameter(":AREA",area);
      dbOp.setParameter(":CODICE_MODELLO",cm);
      dbOp.execute();
      rs = dbOp.getRstSet();

      while (rs.next()) {
        rigo = " AND ";
        dato = rs.getString(1);
        tipo = rs.getString(2);
        formato = rs.getString(3);
        if (formato != null) {
//          formato = formato.replaceAll("hh", "hh24");
          formato = "dd/mm/yyyy";
        }
        elp = DocumentHelper.createElement("CAMPI");
        elp.addAttribute("version","1.1");
        elp.addAttribute("campo",dato);
        area_dato = request.getParameter("_AREA_"+dato);
        if (area_dato == null) {
          area_dato = "";
        }
        modello_dato = request.getParameter("_MODELLO_"+dato);
        if (modello_dato == null) {
          modello_dato = "";
        }
        if ( tipo.equalsIgnoreCase("S")) {
          if (modello_dato.length() != 0) {
            rigo += modello_dato+"."+dato;
          } else {
            rigo += modelli_filtro+"."+dato;
          }
        } else {
          if (modello_dato.length() != 0) {
            rigo += modello_dato+"."+dato;
          } else {
            rigo += modelli_filtro+"."+dato;
          }
        }
        if (area_dato.length() != 0 && modello_dato.length() != 0) {
          elp.addAttribute("area",area_dato);
          elp.addAttribute("cm",modello_dato);

        } else {
          categoria_dato = request.getParameter("_CATEGORIA_"+dato);
          if (categoria_dato == null) {
            categoria_dato = "";
          }
          if (categoria_dato.length() != 0) {
            elp.addAttribute("categoria",categoria_dato);
          }
        }
        operatore = request.getParameter("_OPER_"+dato);
        if (operatore == null) {
          operatore = "";
        }
        valore = request.getParameter(dato);
        if (valore == null || valore.equalsIgnoreCase("-")) {
          valore = "";
        }
        dati.aggiungiDato(dato, valore);
        if (operatore.length() != 0) {
//          valore = request.getParameter(dato);
//          if (valore == null) {
//            valore = "";
//          }
          if (valore.length() != 0) {
            if (cm_filtro.equalsIgnoreCase("M_PROTOCOLLO") && cm_filtro.equals(modello_dato)) {
              m_prot_cond++;
            }
            if (cm_filtro.equalsIgnoreCase("M_PROTOCOLLO") && (modello_dato.length() != 0) &&(!cm_filtro.equals(modello_dato))) {
              other_cond++;
            }
          }
          if (operatore.equalsIgnoreCase("=") && tipo.equalsIgnoreCase("S"))  {
            if (valore.length() != 0) {
              valore = valore.replaceAll("'","''");
              rigo += " = '"+valore+"'";
            }
            operatore = "uguale";
          } else {
            if (operatore.equalsIgnoreCase("=") && tipo.equalsIgnoreCase("N")) {
              if (valore.length() != 0) {
                rigo += " = "+valore;
              }
            }
            if (operatore.equalsIgnoreCase("=") && tipo.equalsIgnoreCase("D")) {
              if (valore.length() != 0) {
                rigo += " = TO_DATE('"+valore+"','"+formato+"')";
              }
            }

          }
          if (operatore.equalsIgnoreCase("LIKE")) {
            if (valore.length() != 0) {
              valore = valore.replaceAll("'","''");
              rigo += " LIKE '"+valore+"%'";
            }
            if (tipo.equalsIgnoreCase("S")) {
              operatore = "uguale";
              elp.addAttribute("tipoUguaglianza","LIKE");
            } else {
              if (tipo.equalsIgnoreCase("N")) {
                if (valore.length() != 0) {
                  rigo += " = "+valore;
                }
              }
              if (tipo.equalsIgnoreCase("D")) {
                if (valore.length() != 0) {
                  rigo += " = TO_DATE('"+valore+"','"+formato+"')";
                }
              }
              operatore = "=";
            }
          }
          if (operatore.equalsIgnoreCase("CONTAINS")) {
            if (valore.length() != 0) {
              //condizione=Global.replaceAll(protectReserveWord(calcolaCondizoneFullText()),"'","''");
              valore = protectReserveWord(calcolaCondizoneFullText(valore));
              valore = valore.replaceAll("'","''");
              if (modello_dato.length() == 0) {
                rigo = " AND CONTAINS ("+modelli_filtro+"."+dato+",'"+valore+"') > 0";
              } else {
                rigo = " AND CONTAINS ("+modello_dato+"."+dato+",'"+valore+"') > 0";
              }
            }
            if (tipo.equalsIgnoreCase("S")) {
              operatore = "uguale";
              elp.addAttribute("tipoUguaglianza","CONTAINS");
            } else {
              if (tipo.equalsIgnoreCase("N")) {
                if (valore.length() != 0) {
                  rigo += " = "+valore;
                }
              }
              if (tipo.equalsIgnoreCase("D")) {
                if (valore.length() != 0) {
                  rigo += " = TO_DATE('"+valore+"','"+formato+"')";
                }
              }
              operatore = "=";
            }
          }
          if (operatore.equalsIgnoreCase("CATSEARCH")) {
            if (valore.length() != 0) {
              valore = valore.replaceAll("'","''");
              if (modello_dato.length() == 0) {
                rigo = " AND CATSEARCH  ("+modelli_filtro+"."+dato+",'"+valore+"',NULL) > 0";
              } else {
                rigo = " AND CATSEARCH  ("+modello_dato+"."+dato+",'"+valore+"',NULL) > 0";
              }
            }
            if (tipo.equalsIgnoreCase("S")) {
              operatore = "uguale";
              elp.addAttribute("tipoUguaglianza","CONTAINS");
            } else {
              if (tipo.equalsIgnoreCase("N")) {
                if (valore.length() != 0) {
                  rigo += " = "+valore;
                }
              }
              if (tipo.equalsIgnoreCase("D")) {
                if (valore.length() != 0) {
                  rigo += " = TO_DATE('"+valore+"','"+formato+"')";
                }
              }
              operatore = "=";
            }
          }
          if (operatore.equalsIgnoreCase("ESATTA")) {
            if (tipo.equalsIgnoreCase("S")) {
              if (valore.length() != 0) {
                valore = valore.replaceAll("'","''");
                rigo += " = '"+valore+"'";
              }
              operatore = "uguale";
              elp.addAttribute("tipoUguaglianza","ESATTA");
            } else {
              if (tipo.equalsIgnoreCase("N")) {
                if (valore.length() != 0) {
                  rigo += " = "+valore;
                }
              }
              if (tipo.equalsIgnoreCase("D")) {
                if (valore.length() != 0) {
                  rigo += " = TO_DATE('"+valore+"','"+formato+"')";
                }
              }
              operatore = "=";
            }
          }
          if (operatore.equalsIgnoreCase("Between") ) {
            valore_2 = request.getParameter("_2_"+dato);
            if (valore_2 == null) {
              valore_2 = "";
            }
            if (valore.length() == 0) {
              valore = valore_2;
            }
            if (valore_2.length() == 0) {
              valore_2 = valore;
            }
            dati.aggiungiDato("_2_"+dato, valore_2);
            String valore_1 = valore;
            if (valore.length() != 0 && valore_2.length() != 0) {
              valore += "$"+valore_2;
            }
            if (tipo.equalsIgnoreCase("S")) {
              if (valore.length() != 0) {
                valore_1 = valore_1.replaceAll("'","''");
                valore_2 = valore_2.replaceAll("'","''");
                rigo += " BETWEEN '"+valore_1+"' AND '"+valore_2+"'";
              }
            }
            if (tipo.equalsIgnoreCase("N")) {
              if (valore.length() != 0) {
                rigo += " BETWEEN "+valore_1+" AND "+valore_2;
              }
            }
            if (tipo.equalsIgnoreCase("D")) {
              if (valore.length() != 0) {
                rigo += " BETWEEN TO_DATE('"+valore_1+" 00:00:00"+"','dd/mm/yyyy hh24:mi:ss') AND TO_DATE('"+valore_2+"23:59:59"+"','dd/mm/yyyy hh24:mi:ss')";
              }
            }
          }
          if (operatore.equalsIgnoreCase("is null") || operatore.equalsIgnoreCase("is not null")) {
            rigo += operatore;
            elp.addAttribute("tipo",tipo);
            valore = "";
          } else {
            if (valore.length() == 0) {
              valore = null;
            }
          }
          if (valore != null) {
            select += rigo;
            elp.addAttribute("value",valore);
            elp.addAttribute("oper",operatore);
            root.add(elp);
          }
        }
      }
      if (ads_msg_err.length() != 0 || list_field_err.length() != 0) {
        mdIn.settaErrMsg(ads_msg_err);
        mdIn.settaListFields(list_field_err);
        mdIn.setNewRequest(request);
        mdIn.setDatiRicerca(dati);
        costruisciModulo(request, mdIn.getQueryURL(),true);

        //Debug Tempo
        stampaTempo("Modulistica::effettuaRicerca - Fine",area,cm,cr,ptime);
        //Debug Tempo
        return;
      }

      String scelta = request.getParameter("SCELTA_OPZIONE");
      if (scelta == null) {
        modelli.remove(mdg);
        mdg.setDatiRicerca(dati);
        modelli.add(mdg);
        request.getSession().setAttribute("modelli", modelli);
      }


      if (other_cond > 0 && m_prot_cond == 0) {
        u = select.toUpperCase().indexOf("SELECT");
        select = "SELECT /*+ OPT_PARAM('_optimizer_cost_based_transformation' 'off') */ "+select.substring(u+6, select.length());
      }
      select += " AND D.STATO_DOCUMENTO NOT IN ('CA','RE')";
      select += " "+union+" ";
 //     select += " AND GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI', d.id_documento,'L','"+utente+"',F_TRASLA_RUOLO('"+utente+"','GDMWEB','GDMWEB'),TO_CHAR(SYSDATE,'dd/mm/yyyy')) = 1";
      ordinamento = request.getParameter("_ORDINAMENTO_FILTRO");
      String tmp = "";
      String ord = "";
      if (ordinamento != null && ordinamento.length() != 0) {
        select += " ORDER BY ";
        String virgola = "";
        StringTokenizer st1 = new StringTokenizer(ordinamento,",");
        while (st1.hasMoreTokens()) {
          select += virgola;
          elp = DocumentHelper.createElement("ORDINAMENTO");
          tmp = st1.nextToken();
          if ((tmp.indexOf(" ASC") + tmp.indexOf(" asc")) > -1) {
            ord = "ASC";
            tmp = tmp.replaceAll(" ASC", "");
            tmp = tmp.replaceAll(" asc", "");
          }
          if ((tmp.indexOf(" DESC") + tmp.indexOf(" desc")) > -1) {
            ord = "DESC";
            tmp = tmp.replaceAll(" DESC", "");
            tmp = tmp.replaceAll(" desc", "");
          }
          if (ord.length() == 0) {
            ord = "ASC";
          }
//          dato = tmp.replaceAll(" ", "");
          dato = tmp;
          elp.addAttribute("campo",dato);
          elp.addAttribute("ordinamento",ord);
          area_dato = request.getParameter("_AREA_"+dato);
          if (area_dato == null) {
            area_dato = "";
          }
          modello_dato = request.getParameter("_MODELLO_"+dato);
          if (modello_dato == null) {
            modello_dato = "";
          }

          select += dato+" "+ord;
//          if (!modello_dato.length() == 0) {
//            select += modello_dato+"."+dato+" "+ord;
//          } else {
//            select += modelli_filtro+"."+dato+" "+ord;
//          }
          if (area_dato.length() != 0 && modello_dato.length() != 0) {
            elp.addAttribute("area",area_dato);
            elp.addAttribute("cm",modello_dato);

          } else {
            categoria_dato = request.getParameter("_CATEGORIA_"+dato);
            if (categoria_dato == null) {
              categoria_dato = "";
            }
            if (categoria_dato.length() != 0) {
              elp.addAttribute("categoria",categoria_dato);
            }
          }
          root.add(elp);
          virgola = ",";
        }
      }
      select = "select id, ti, da, cr from ("+select;
      select += ") a, dual ";
      select += " WHERE  GDM_COMPETENZA.GDM_VERIFICA('DOCUMENTI', a.id,'L','"+utente+"',F_TRASLA_RUOLO('"+utente+"','GDMWEB','GDMWEB'),TO_CHAR(SYSDATE,'dd/mm/yyyy'))||dummy = '1X'";
      String sJoins = request.getParameter("_JOIN_FILTRO");
      if (sJoins != null && sJoins.length() != 0) {
        StringTokenizer st1 = new StringTokenizer(sJoins,";");
        while (st1.hasMoreTokens()) {
          StringTokenizer st2 = new StringTokenizer(st1.nextToken(),"=");
          elp = DocumentHelper.createElement("JOIN");
          elp.addAttribute("version","1.1");
          StringTokenizer st3 = new StringTokenizer(st2.nextToken(),"#");
          elp = elp.addAttribute("campo1",st3.nextToken());
          elp = elp.addAttribute("tipo1",st3.nextToken());
          tmp = st3.nextToken();
          if (st3.hasMoreTokens()) {
            elp.addAttribute("area1",tmp);
            elp.addAttribute("cm1",st3.nextToken());
          } else {
            elp.addAttribute("categoria1",tmp);
          }
          st3 = new StringTokenizer(st2.nextToken(),"#");
          elp = elp.addAttribute("campo2",st3.nextToken());
          elp = elp.addAttribute("tipo2",st3.nextToken());
          tmp = st3.nextToken();
          if (st3.hasMoreTokens()) {
            elp.addAttribute("area2",tmp);
            elp.addAttribute("cm2",st3.nextToken());
          } else {
            elp.addAttribute("categoria2",tmp);
          }
          root.add(elp);
        }
      }
      if (master_filtro == null) {
        master_filtro = "0";
      }

      if (mdIn.getIstruzioni().indexOf("<USER_QUERY>")!=-1) {
    	  select=mdIn.getIstruzioni();
    	  String s="";
    	  try {
            UserQuery uq = new UserQuery(request,select);
    		s=uq.bindSearch();
    		//s=effettuaRicerca(request,select);
    		//s=s.replace("<USER_QUERY>","");
    	  }
    	  catch(Exception eIntern) {
    	        mdIn.settaErrMsg(eIntern.getMessage());
     	        mdIn.setNewRequest(request);
    	        mdIn.setDatiRicerca(dati);
    	        costruisciModulo(request, mdIn.getQueryURL(),true);
    	        //Debug Tempo
    	        stampaTempo("Modulistica::effettuaRicerca - Fine",area,cm,cr,ptime);
    	        //Debug Tempo
    	        return;

    	  }


	      s=s.replaceAll(":UtenteGDM",(String)request.getSession().getAttribute("UtenteGDM"));
    	  select=s;
      }

      String conservabili = request.getParameter("_GDM_CONSERVAZIONE");
    	String listaid = "";
      if (conservabili != null && conservabili.equalsIgnoreCase("C")) {
        //Faccio la query
        dbOp = vu.getDbOp();
        dbOp.setStatement(select);
        dbOp.execute();
        rs = dbOp.getRstSet();
        int recordTotali = 0;
        String data_iter = "";
        while (rs.next()) {
        	listaid += rs.getString(1)+"@";
        	recordTotali++;
        }
        query = "SELECT TO_CHAR(SYSDATE,'DD/MM/YYYY') FROM DUAL";
        dbOp.setStatement(query);
        dbOp.execute();
        rs = dbOp.getRstSet();
        if (rs.next()) {
        	data_iter = rs.getString(1);
        }

        elp = DocumentHelper.createElement("MASTER");
        elp.addAttribute("value",master_filtro);
        root.add(elp);
        elp = DocumentHelper.createElement("SELECT");
        elp.addAttribute("value",select);
        root.add(elp);
        //Calcolo il codice hash e crypto il vaolre del filtro
        String filtro = dDoc.asXML();
        Hashing hash = new Hashing();
        byte[] key = Base64.f_decode("bnC/ubQOdqFuWF6Kdm6BKRE7cGSSW097");
        TripleDESEncrypter enc = new TripleDESEncrypter(key, "ECB", "PKCS5Padding");
        filtro = enc.encrypt(filtro);
        byte[] ccfq = hash.getHashCode(filtro);
        httpSess.setAttribute("_FILTRO_QUERY_MODULISTICA",filtro);
        httpSess.setAttribute("_CCFQ",ccfq);
        httpSess.setAttribute("_FUNC_EXP",mdIn.getNoteInterne());

      	corpoHtml = getServletConservazione(recordTotali, listaid, data_iter, mdIn.getQueryURL());
      	//Debug Tempo
        stampaTempo("Modulistica::effettuaRicerca - Fine",area,cm,cr,ptime);
        //Debug Tempo
        return;
      }
      if (conservabili != null && conservabili.equalsIgnoreCase("E")) {
      	listaid = request.getParameter("listaid");
      	scelta = request.getParameter("SCELTA_OPZIONE");
      	String xml = "";
      	String result = "";
      	String operazione = "";
      	String datait, orait, minit, dataesecuzione;
      	if (scelta.equalsIgnoreCase("1")) {
      		//Mando in conservazione
          try{
            dbOp = vu.getDbOp();
        		dataesecuzione = "";
        		datait = request.getParameter("data_iter");
        		orait = request.getParameter("ora_iter");
        		minit = request.getParameter("minuti_iter");
        		if ((datait != null) && (datait.length() > 0)) {
        			if (orait == null || orait.length() == 0) {
        				orait = "00";
        			} else {
        				if (orait.length() == 1) {
        					orait = "0"+orait;
        				}
        			}
        			if (minit == null || minit.length() == 0) {
        				minit = "00";
        			} else {
        				if (minit.length() == 1) {
        					minit = "0"+minit;
        				}
        			}
        			query = "select to_char(to_date('"+datait+" "+orait+":"+minit+"','dd/mm/yyyy hh24:mi'),'dd/mm/yyyy hh24::mi') from dual";
              dbOp.setStatement(query);
              try {
                dbOp.execute();
                rs = dbOp.getRstSet();
                if (rs.next()) {
                	dataesecuzione = rs.getString(1);
                }
              } catch (Exception edata) {
              	mdIn.settaErrMsg("Attenzione errore: Data in formato non valido");
        	      mdIn.setNewRequest(request);
                mdIn.setDatiRicerca(dati);
                costruisciModulo(request, mdIn.getQueryURL(),true);
                return;
              }

        		}
        		operazione ="<invocation class=\"it.finmatica.jdms.conservazione.Conservazione\"> "+
        								"<method name=\"daConservare\"> "+
        								"<params> <param type=\"String\"><![CDATA[ :XML ]]></param>"+
        								"<param type=\"String\">JSUITE_CONSERVAZIONE_STD</param>"+
        								"<param type=\"String\">"+dataesecuzione+"</param>"+
        								"</params> </method> </invocation>";
        		xml = creaXML(request, listaid, area, cm, cr);
            ControlliParser cp = new ControlliParser(request,null,cr,xml,false,stato_doc,false);
            operazione = cp.bindingDeiParametri(operazione);
          	loggerError(operazione,null);
            IDbOperationSQL dbOpParser =null;
            try {
              dbOpParser= Parametri.creaDbOp();
              WrapParser wp = new WrapParser(operazione);
              result = wp.goExtended(request, dbOpParser);
            }
            finally {
          	  try {
                dbOpParser.close();
              }
          	  catch (Exception e) {

              }
            }

            if (result != null && result.length() > 0) {
              Document dInput = null;
              dInput = DocumentHelper.parseText(result);
              String esito = leggiValoreXML(dInput, "RESULT");
              if (!esito.equalsIgnoreCase("ok")) {
                String errmsg = leggiValoreXML(dInput, "ERROR");
                mdIn.settaErrMsg(errmsg);
              } else {
              	mdIn.settaErrMsg("Operazione eseguita con successo!");
              }
            }
          } catch (Exception ijEx) {
            loggerError("ServletModulistica::effettuaRicerca() - Area: "+area+" - Modello: "+cm+" - Errore:  ["+ ijEx.toString()+"]",ijEx);
            mdIn.settaErrMsg("Attenzione errore: "+ijEx.toString());
          }
  	      mdIn.setNewRequest(request);
//          mdIn.setDatiRicerca(dati);
          costruisciModulo(request, mdIn.getQueryURL(),true);
          //Debug Tempo
          stampaTempo("Modulistica::effettuaRicerca - Fine",area,cm,cr,ptime);
          //Debug Tempo

      		return;
      	}
      	if (scelta.equalsIgnoreCase("2")) {
      		//Marco per la conservazione
      		operazione ="<invocation class=\"it.finmatica.jdms.conservazione.Conservazione\"> <method name=\"marcaDaConservare\"> <params> <param type=\"String\"><![CDATA[ :XML ]]></param> </params> </method> </invocation>";
      		xml = creaXML(request, listaid, area, cm, cr);
          ControlliParser cp = new ControlliParser(request,null,cr,xml,false,stato_doc,false);
          operazione = cp.bindingDeiParametri(operazione);
          try{
          	loggerError(operazione,null);
            IDbOperationSQL dbOpParser =null;
            try {
              dbOpParser= Parametri.creaDbOp();
              WrapParser wp = new WrapParser(operazione);
              result = wp.goExtended(request, dbOpParser);
            }
            finally {
              try {
                dbOpParser.close();
              }
              catch (Exception e) {

              }
            }

            if (result != null && result.length() > 0) {
              Document dInput = null;
              dInput = DocumentHelper.parseText(result);
              String esito = leggiValoreXML(dInput, "RESULT");
              if (!esito.equalsIgnoreCase("ok")) {
                String errmsg = leggiValoreXML(dInput, "ERROR");
                mdIn.settaErrMsg(errmsg);
              } else {
              	mdIn.settaErrMsg("Operazione eseguita con successo!");
              }
            }
          } catch (Exception ijEx) {

            loggerError("ServletModulistica::effettuaRicerca() - Area: "+area+" - Modello: "+cm+" - Errore:  ["+ ijEx.toString()+"]",ijEx);
            mdIn.settaErrMsg("Attenzione errore: "+ijEx.toString());
          }
  	      mdIn.setNewRequest(request);
//          mdIn.setDatiRicerca(dati);
          costruisciModulo(request, mdIn.getQueryURL(),true);
          //Debug Tempo
          stampaTempo("Modulistica::effettuaRicerca - Fine",area,cm,cr,ptime);
          //Debug Tempo

      		return;
      	}
      }

      String stampaSelect = request.getParameter("Select");
      if (stampaSelect != null ) {
        mdIn.settaErrMsg(select);
	      mdIn.setNewRequest(request);
        mdIn.setDatiRicerca(dati);
        costruisciModulo(request, mdIn.getQueryURL(),true);
        //Debug Tempo
        stampaTempo("Modulistica::effettuaRicerca - Fine",area,cm,cr,ptime);
        //Debug Tempo
        return;
      }


      if (returnSelect) {
        selectQuery = select;
	      mdIn.setNewRequest(request);
        mdIn.setDatiRicerca(dati);
        costruisciModulo(request, mdIn.getQueryURL(),true);
        //Debug Tempo
        stampaTempo("Modulistica::effettuaRicerca - Fine",area,cm,cr,ptime);
        //Debug Tempo
        return;
      }


      elp = DocumentHelper.createElement("MASTER");
      elp.addAttribute("value",master_filtro);
      root.add(elp);
      elp = DocumentHelper.createElement("SELECT");
      elp.addAttribute("value",select);
      root.add(elp);
      //Calcolo il codice hash e crypto il vaolre del filtro
      String filtro = dDoc.asXML();
      Hashing hash = new Hashing();
      byte[] key = Base64.f_decode("bnC/ubQOdqFuWF6Kdm6BKRE7cGSSW097");
      TripleDESEncrypter enc = new TripleDESEncrypter(key, "ECB", "PKCS5Padding");
      filtro = enc.encrypt(filtro);
      byte[] ccfq = hash.getHashCode(filtro);
      String strccfq = new String(ccfq);
      httpSess.setAttribute("_FILTRO_QUERY_MODULISTICA",filtro);
      httpSess.setAttribute("_CCFQ",strccfq);
      httpSess.setAttribute("_FUNC_EXP",mdIn.getNoteInterne());

      String exportDati = request.getParameter("ExportDati");
      if (exportDati != null ) {
    	  String idQuery = request.getParameter("idQuery");
    	  String idCartella = request.getParameter("idCartAppartenenza");
    	  String sScript = "<script  type='text/javascript'>\n"+
    			  		   "var TopPosition=(screen.height)?(screen.height-300)/2:0;\n"+
    			  		   "var LeftPosition=(screen.width)?(screen.width-600)/2:0;\n"+
    	  				   "window.open('/jdms/esportaDatiServlet?idQuery="+idQuery+
    	  				   "&idCartella="+idCartella+"&fulltext=&ricercaAllegati=N&ricercaOCR=N&ricercaFT=N&ricercaMod=S&lista=','Estrai dati', "+
    	  				   "'toolbar= 0,location= 0,directories= 0,status= 0,menubar= 0,scrollbars= 1,resizable= 1,copyhistory= 0"+
    	  				   ",width=600,height=300,top='+TopPosition+',left='+LeftPosition+'');</script>";
    	  mdIn.settaErrMsg(sScript);
    	  mdIn.setNewRequest(request);
    	  mdIn.setDatiRicerca(dati);
    	  costruisciModulo(request, mdIn.getQueryURL(),true);
    	  //Debug Tempo
    	  stampaTempo("Modulistica::effettuaRicerca - Fine",area,cm,cr,ptime);
    	  //Debug Tempo
    	  return;
      }



      corpoHtml = getServletReindirizza();
    } catch (Exception e) {
      loggerError("ServletModulistica::effettuaRicerca() - Area: "+area+" - Modello: "+cm+" - Errore:  ["+ e.toString()+"]",e);
      //Debug Tempo
      stampaTempo("Modulistica::effettuaRicerca - Fine",area,cm,cr,ptime);
      //Debug Tempo
    }
  }

  public void setReturnSelect(boolean param) {
  	returnSelect = param;
  }

  public String getSelectQuery() {
  	return selectQuery;
  }

  /**
   * @param args
   */
/*  public static void main(String[] args) {
    String syncErr = null;
    Vector<Integer> ite = null;
    SyncSuite sync = null;
    String retval = "";
    String queryC = "";
    String tipoC = "";
    String corpo = "";
    String sblocco = "N";
    String newArea = "", newControllo = "";
    String area = "";
    String cm = "";
    String controllo = "";
    Element root;
    String mygdc_link = "";
    boolean myforceRedirect = false;
    String result="<FUNCTION_OUTPUT><RESULT>ok</RESULT><DOC/><FORCE_REDIRECT>N</FORCE_REDIRECT>"+
"<LISTAID><ID><IDOGGETTO>10003576</IDOGGETTO><TIPOOGGETTO>D</TIPOOGGETTO>"+
"<MSG>Protocollo \"Protocollo Generale - Doc. n. 24842 del 07/05/2007 oggetto:"+
"STEFANIA TEST AGGIUNGI: Smistamento  da 2 - Risorse umane, organizzazione, informatica e affari generali a 0.0.1 - Controllo direzionale e qualit� del 07/05/2007 10:29:40, storicizzato correttamente.</MSG></ID>"+
"<ID><IDOGGETTO>10003577</IDOGGETTO><TIPOOGGETTO>D</TIPOOGGETTO><MSG>Protocollo \"Protocollo Generale - Doc. n. 24842 del 07/05/2007 oggetto: STEFANIA TEST AGGIUNGI\": Smistamento  da 2 - Risorse umane, organizzazione, informatica e affari generali a 0.0.1 - Controllo direzionale e qualit� del 07/05/2007 10:29:40, storicizzato correttamente.</MSG></ID>"+
"<ID><IDOGGETTO>10003578</IDOGGETTO><TIPOOGGETTO>D</TIPOOGGETTO><MSG>Protocollo \"Protocollo Generale - Doc. n. 24842 del 07/05/2007 oggetto: STEFANIA TEST AGGIUNGI\": Smistamento  da 2 - Risorse umane, organizzazione, informatica e affari generali a 0.0.1 - Controllo direzionale e qualit� del 07/05/2007 10:29:40, storicizzato correttamente.</MSG></ID></LISTAID><JSYNC><AREA>SEGRETERIA.PROTOCOLLO</AREA>"+
"<CONTROLLO>VUOTO_ESECUZIONE</CONTROLLO></JSYNC></FUNCTION_OUTPUT>";

    //Interpretazione XML di output
    try {
      Document dInput = null;
      dInput = DocumentHelper.parseText(result);
      String esito = leggiValoreXML(dInput, "RESULT");
      if (!esito.equalsIgnoreCase("ok")) {
        String errore = leggiValoreXML(dInput, "ERROR");
        String stackError = leggiValoreXML(dInput, "STACKTRACE");
        System.out.println(stackError);
        return;
      }
      String new_gdc_link = leggiValoreXML(dInput, "REDIRECT");
      if ((new_gdc_link != null) && (!new_gdc_link.length() == 0)) {
        mygdc_link = new_gdc_link;
      }
      String fRed = leggiValoreXML(dInput, "FORCE_REDIRECT");
      if ((fRed !=null) && (fRed.equalsIgnoreCase("Y"))) {
        myforceRedirect = true;
        mygdc_link = new_gdc_link;
        new_gdc_link = null;
      }

      root = dInput.getRootElement();
      String newData = "";
      Element eAgg = leggiElementoXML(root, "DATI_AGGIORNAMENTO");
      if (eAgg != null) {
        newData = leggiValoreXML(eAgg, "DATA");
      }

      Element eDoc = leggiElementoXML(root, "DOC");
      Element elemento;
      String campo = "";
      String valore = "";

      Element jSync = leggiElementoXML(root, "JSYNC");
      if (jSync != null ) {
        elemento = leggiElementoXML(jSync,"AREA");
        newArea = elemento.getText();
        elemento = leggiElementoXML(jSync,"CONTROLLO");
        newControllo = elemento.getText();
      } else {
        newArea = area;
        newControllo = controllo;
      }

      Element listaId = leggiElementoXML(root, "LISTAID");
      boolean okrun = true;
//      if (listaId == null && sblocco.equalsIgnoreCase("S")) {
      if (sblocco.equalsIgnoreCase("S")) {
        System.out.println("Sblocco quello di partenza");
      } else {
        String msg="";
        String msgElencoError="";
        String msgElenco="";
        String tipoObj = "";
        String idObj = "", idOggetto = "";
        Element idError = null;
        String idMsg = "";
        if (listaId != null) {
          for(Iterator iterator = listaId.elementIterator(); iterator != null && iterator.hasNext();) {
            elemento  = (Element)iterator.next();
            tipoObj   = leggiValoreXML(elemento,"TIPOOGGETTO");
            idObj     = leggiValoreXML(elemento,"IDOGGETTO");
            idError   = leggiElementoXML(elemento,"ERROR");
            idMsg     = leggiValoreXML(elemento,"MSG");
            okrun = true;
            System.out.println("tipoObj: "+tipoObj);
            System.out.println("idObj: "+idObj);
            System.out.println("idError: "+idError);
            System.out.println("idMsg: "+idMsg);

            if (idError == null) {
              msgElenco+=" - "+idMsg+"<br>";
    //        Distinguiamo id in base al tipo oggetto
              if((tipoObj.equals("C")) || (tipoObj.equals("X"))) {
                idOggetto=tipoObj+"-"+idObj;
              } else {
                if(tipoObj.equals("D")) {
                  if (idObj.equalsIgnoreCase("10003576")) {
                    idOggetto = "SEGRETERIA.PROTOCOLLO@M_SMISTAMENTO@DMSERVER10002080";
                  }
                  if (idObj.equalsIgnoreCase("10003577")) {
                    idOggetto = "SEGRETERIA.PROTOCOLLO@M_SMISTAMENTO@DMSERVER10002081";
                  }
                  if (idObj.equalsIgnoreCase("10003578")) {
                    idOggetto = "SEGRETERIA.PROTOCOLLO@M_SMISTAMENTO@DMSERVER10002082";
                  }
                } else {
                  idOggetto=idObj;
                }
              }
System.out.println("oGGETTO: "+idOggetto);
              sync = new SyncSuite("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@10.97.11.19:1521:prmod", "GDM", "GDM");
              ite = (Vector<Integer>)sync.isExecutable(newArea ,newControllo,idOggetto,"RPI");
              syncErr = sync.getLastError();
              if (syncErr != null || ite == null) {
                if (ite == null) {
                  System.out.println("Vettore nullo ");
                } else {
                  System.out.println("Errore "+syncErr);
                }
                sync.close();
              } else {
                System.out.println("Eseguirei ");

                for (Integer intObj : ite) {
                  okrun = okrun
                      && (sync.executed(intObj.intValue()) == SyncSuite.SYNC_ESEGUITO);
                }
                if (okrun) {
                  sync.commit();
                } else {
                  sync.rollback();
                }
                sync.close();
              }
            } else {
              msgElencoError+=" - "+idMsg+"<br>";
            }
            msg+="Avviso:<br>";
            if (msgElencoError.length() == 0) {
              msg+="Le operazioni sono state tutte eseguite correttamente!<br>";
            } else {
              if(msgElenco!="") {
                msg+="<br>Elenco delle operazioni eseguite correttamente:<br>";
                msg+=msgElenco;
              }

              if(msgElencoError!="") {
                msg+="<br>Elenco delle operazioni non eseguite correttamente:<br>";
                msg+=msgElencoError;
              }
            }
          }
        }
        if (new_gdc_link != null && jSync != null) {
          String sParametri="msg="+URLEncoder.encode(msg,"ISO8859-1");
          sParametri+="&redirect="+URLEncoder.encode(new_gdc_link,"ISO8859-1");
          myforceRedirect = true;
          mygdc_link = "../common/MessagePage.do?"+sParametri;
        }
      }
      System.out.println(mygdc_link);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }*/

  private String campiRedirect(HttpServletRequest request) {
    String retval = "";
    Dati dati = (Dati) request.getAttribute("gdm_valori_redirect");
    if (dati != null) {
      int num = dati.getNumeroValori();
      int i = 0;
      for (i=0; i<num;) {
        retval += "<input type='hidden' name='"+dati.getCodice(i)+"' value='"+dati.getValore(i)+"' />";
      }
    }
    return retval;
   }

  /**
   *
   * @param condAnd
   * @return
   */
  private String calcolaCondizoneFullText(String condAnd){
    StringBuffer sCondizioneFullText = new StringBuffer("");
    String condizioneAndSistemata="";

    if (condAnd!=null && (!condAnd.equals("")) ) {
      StringTokenizer s = new StringTokenizer(protectReserveWord(condAnd)," ");
      condizioneAndSistemata="(" ;
      while (s.hasMoreTokens()) {
        condizioneAndSistemata+=s.nextElement();
        if (s.hasMoreTokens()) condizioneAndSistemata+=" AND ";
      }

      sCondizioneFullText.append(condizioneAndSistemata+")");
    }

    return sCondizioneFullText.toString();
  }

  /**
   *
   * @param phrase
   * @return
   */
  private String protectReserveWord(String phrase) {
//    for(int i=0;i<reserveWord.length;i++) {
//      phrase = phrase.replaceAll(reserveWord[i], escapeCaracter+reserveWord[i]);
////      phrase=vu.Global.replaceAll(phrase,reserveWord[i],escapeCaracter+reserveWord[i]);
//    }

    return phrase;
  }

  public static String encode (String string, String tipoCampo) {
    if (string == null) {
      return null;
    }
    if ((tipoCampo.equalsIgnoreCase("S") || tipoCampo.equalsIgnoreCase("T") || tipoCampo.equalsIgnoreCase("F")) && Parametri.CODIFICA_XSS.equalsIgnoreCase("S")) {
    	string = Encode.forHtmlAttribute(string);
    }

    return string;

//    int j = 0;
//
//    StringBuffer sb = null;
//    char c;
//    for (int i = 0; i < string.length (); ++i) {
//      c = string.charAt(i);
//      j = (int)c;
//      if (j > inizio) {
//        if( sb == null ) {
//          sb = new StringBuffer( string.length()+4 );
//          sb.append( string.substring(0,i) );
//        }
//        //encode all non basic latin characters
//        sb.append("&#" + ((int)c) + ";");
//      }
//      else if( sb != null ) {
//        sb.append(c);
//      }
//    }
//
//    return sb != null ? sb.toString() : string;
  }

  private void settaDataAggiornamentoModello(HttpServletRequest request, String newData) {
    if (newData == null) return;
    if (newData.length() == 0) return;
    if (mdg==null) return;
    modelli.remove(mdg);
    mdg.setUltimoAgg(newData);
    modelli.add(mdg);
    request.getSession().setAttribute("modelli",modelli);
    aggData = "";
  }

  private void rimuoviModello(HttpServletRequest request, Modello md) {
    if (modelli.size()>0) {
      modelli.remove(md);
      request.getSession().setAttribute("modelli", modelli);
    }
  }

  private String errorLogin (String area, String cm, String cr, String utente) {
  	String chiudi = "<input type='button' value='Chiudi' onclick='window.close()' />";
  	if (lettura.equalsIgnoreCase("Q")) {
  		chiudi = "";
  	}
  	String retval = "<html><head></head><body><form>" +
  					 "<p align='center'><table><tr><td><p align='center'><b><font color='#FF0000' size='5'>Attenzione, </font></b>"+
  	         "<font size='5' color='#FF0000'><b>Accesso Negato!</b></font></td></tr><tr><td>"+
  	         "<p align='center'><font size='4'><b>Non si dispone delle autorizzazioni necessarie per accedere al documento.</b></font>"+
  	         "</td></tr><tr><td><a href='#' id='nascondi1' onclick=\"document.getElementById('dettagli').style.display='block'; this.style.display='none'\" >Visualizza dettagli</a>"+
  	         "<div id='dettagli' style='display: none'\">"+
  	         "<a href='#' id='nascondi2' onclick=\"document.getElementById('nascondi1').style.display='block'; document.getElementById('dettagli').style.display='none'\" >Nascondi dettagli</a>"+
  	         "<p><span><b>ID Utente:</b> "+utente+"</span></p>"+
  	         "<p><span><b>Area:</b> "+area+"</span></p>"+
  	         "<p><span><b>Modello:</b> "+cm+"</span></p>"+
  	         "<p><span><b>Richiesta:</b> "+cr+"</span></p></td></tr>"+
  	         "<tr><td><p align='center'>"+chiudi+"</p></td></tr></p>"+
  	         "</body></form></html>";
  	return retval;

  }

  /**
   * cercaModello()
   * Ricerca a livello di sessione la presenza del modello richiesto.
   * Se � gi� presente evita di ricaricarlo da database.
   **/
  private Modello cercaModelloAjax(HttpSession pHttpSess, String pArea, String pCodiceModello, String pCodiceRichiesta) throws Exception {
    Modello     md = null;
    ArrayList   modelli;
//    String      idDoc= "";
    String      sNomeServlet = "";
    boolean     trovato = false;
    int         i = 0;

    try {
      modelli = (ArrayList) pHttpSess.getAttribute("modelli");
      sNomeServlet = (String) pHttpSess.getAttribute("p_nomeservlet");
      if (pdo.equalsIgnoreCase("HR")) {
        if (sNomeServlet != null) {
          int punto = sNomeServlet.indexOf(".do?");
          if (punto > 0) {
            sNomeServlet = sNomeServlet.substring(0,punto);
          }
        }
      }
      loggerError("------------------->"+sNomeServlet, null);
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
          loggerError("------------------->Trovato", null);
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
      loggerError("------------------->2--->"+sNomeServlet, null);
      while ( (!trovato) && (i < modelli.size()) ) {
        md = (Modello)modelli.get(i);
        if ((md.getArea().equals(pArea)) &&
            (md.getCodiceModello().equals(pCodiceModello)) &&
            (md.getCodiceRichiesta().equals(pCodiceRichiesta)) &&
            (md.getNomeServlet().equals(sNomeServlet))) {
          trovato = true;
          loggerError("------------------->2--->Trovato", null);
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
      loggerError("SerletModulistica::cercaModello() - Area: "+pArea+" - Modello: "+pCodiceModello+"- Richiesta: "+pCodiceRichiesta+" - Attenzione! Si � verificato un errore durante la ricerca del modello in memoria: "+e.toString(),e);
      corpoHtml += "Attenzione! Si � verificato un errore durante la ricerca del modello in memoria!";
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        corpoHtml += e.toString();
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        corpoHtml += e.getStackTrace().toString();
      }
      return null;
    }
  }

  private int calcolaNumeroVersione(String iddoc, IDbOperationSQL dbOp) throws Exception {
  	int versione = 0;
  	ResultSet rst = null;
  	String query = "SELECT MAX(VERSIONE) FROM VERSIONI_DOCUMENTI WHERE ID_DOCUMENTO = :IDDOC";
  	dbOp.setStatement(query);
  	dbOp.setParameter(":IDDOC", iddoc);
  	dbOp.execute();
  	rst = dbOp.getRstSet();
  	if (rst.next()) {
  		versione = rst.getInt(1);
  	}
  	versione++;

  	return versione;
  }

  private void memorizzaVersione (String p_area,String p_cm, String iddoc, int versione, String us, IDbOperationSQL dbOp) throws Exception {
  	String 		query;
  	ResultSet rst = null;
  	String		nomeStile, corpoStile;

    query = "SELECT S.STILE, S.CORPO FROM STILI S, MODELLI M "+
				    "WHERE M.AREA = :AREA AND "+
				    "M.CODICE_MODELLO = :CM AND "+
				    "M.AREA = S.AREA AND "+
				    "M.STILE = S.STILE";

    dbOp.setStatement(query);
    dbOp.setParameter(":AREA", p_area);
    dbOp.setParameter(":CM", p_cm);
    dbOp.execute();
    rst = dbOp.getRstSet();
    if (rst.next()) {
    	nomeStile = rst.getString("STILE");
      BufferedInputStream bis = dbOp.readClob("CORPO");
      StringBuffer sb = new StringBuffer();
      int ic;
      while ((ic =  bis.read()) != -1) {
        sb.append((char)ic);
      }
      corpoStile = sb.toString();
    } else {
    	nomeStile = "";
    	corpoStile = "";
    }

    query = "INSERT INTO VERSIONI_DOCUMENTI "+
    				"(ID_DOCUMENTO, VERSIONE, DATA_VERSIONE, UTENTE_VERSIONE, STILE, CORPO_STILE) "+
    				"VALUES "+
    				"(:IDDOC, :VERS, sysdate, :UTENTE, :STILE, :CORPO)";

    dbOp.setStatement(query);
    dbOp.setParameter(":IDDOC", iddoc);
    dbOp.setParameter(":VERS", versione);
    dbOp.setParameter(":UTENTE", us);
    dbOp.setParameter(":STILE", nomeStile);
    byte bStile[] = corpoStile.getBytes();
    ByteArrayInputStream bais = new ByteArrayInputStream(bStile);
    dbOp.setAsciiStream(":CORPO", bais, bais.available());
    dbOp.execute();

    query = "UPDATE VERSIONI_DOCUMENTI SET DOCUMENTO = :MODELLO "+
    				"WHERE ID_DOCUMENTO = :IDDOC AND VERSIONE = :VERS";
    dbOp.setStatement(query);
    dbOp.setParameter(":IDDOC", iddoc);
    dbOp.setParameter(":VERS", versione);
    byte bDoc[] = corpoHtml.getBytes();
    ByteArrayInputStream baid = new ByteArrayInputStream(bDoc);
    dbOp.setAsciiStream(":MODELLO", baid, baid.available());
    dbOp.execute();

    query = "INSERT INTO RISORSE_VERSIONI_DOCU "+
    				"(ID_DOCUMENTO, VERSIONE, NOMEFILE, RISORSA) "+
    				"(SELECT :IDDOC, :VERS, NOMEFILE, GRAFICO FROM GRAFICI_MODELLO "+
    				"WHERE AREA = :AREA AND CODICE_MODELLO = :CM)";
    dbOp.setStatement(query);
    dbOp.setParameter(":IDDOC", iddoc);
    dbOp.setParameter(":VERS", versione);
    dbOp.setParameter(":AREA", p_area);
    dbOp.setParameter(":CM", p_cm);
    dbOp.execute();

    //Eseguo il versionamento dei valori
    ProfiloVersion pv = new ProfiloVersion(iddoc,versione);
    pv.initVarEnv(us,"",dbOp.getConn());
    pv.versiona();

  }

  public String visualizzaVersione(HttpServletRequest request, String area, String cm, String iddoc, int versione_documento, String us) throws Exception {
  	String 					retval = "";
  	IDbOperationSQL dbOp = null;
  	String 					query = "";
  	ResultSet 			rst = null;
    InputStream     srcBlob = null;
    String					nomefile = "";
    String 					classDiv = "";
    String 					classSpan = "";

  	try {
      String myPathTemp = request.getSession().getServletContext().getRealPath("")+
										      File.separator+"temp"+File.separator+area+
										      File.separator+cm;
			File myFile = new File(myPathTemp);
			if (!myFile.exists()) {
				myFile.mkdirs();
			}
      dbOp = vu.getDbOp();

      query = "SELECT CORPO_STILE, DOCUMENTO "+
      				"FROM VERSIONI_DOCUMENTI "+
      				"WHERE ID_DOCUMENTO = :IDDOC"+
      				" AND VERSIONE = :VERSIONE";

      dbOp.setStatement(query);
      dbOp.setParameter(":IDDOC", iddoc);
      dbOp.setParameter(":VERSIONE", versione_documento);
      dbOp.execute();
      rst = dbOp.getRstSet();
      if (rst.next()) {
		    BufferedInputStream bis = dbOp.readClob("DOCUMENTO");
		    StringBuffer sb = new StringBuffer();
		    int ic;
		    while ((ic =  bis.read()) != -1) {
		      sb.append((char)ic);
		    }
		    retval = sb.toString();
        File ffile = new File(myPathTemp+File.separator+"Style.css");
        BufferedInputStream bisStyle = dbOp.readClob("CORPO_STILE");
        FileOutputStream fos = new FileOutputStream(ffile);
        byte buf[] = new byte[1];

        while( bisStyle.read(buf) != -1)
          fos.write(buf);
        fos.flush();
        fos.close();
        bisStyle.close();
        ffile.setLastModified(0);

        //Leggo gli eventuali allegati
				ProfiloVersion pv = new ProfiloVersion(iddoc,versione_documento);
		    pv.initVarEnv(vu);
				if (pv.accedi().booleanValue()) {
					Vector<GD4_Oggetti_File> v=pv.getListaFile();
		      String newCorpo = retval.replaceAll("</form>","");
		      retval = newCorpo;
		      newCorpo = retval.replaceAll("</body>","");
		      retval = newCorpo.replaceAll("</html>","");
	        classDiv = " class='AFCAllegati' ";
	        retval += "<div id='gdm_allegati' "+classDiv+">\n";

					for(int i=0;i<v.size();i++) {
						GD4_Oggetti_File obj = v.get(i);

            String icona          = obj.getIcona();
            String id_ogfi        = obj.getIdOggettoFile();
            String nomeFile       = obj.getFileName();
            boolean visibile      = obj.isVisibleVariable();
            String firma = "";
            String wopen = "_self";
            if (nomeFile.toUpperCase().indexOf(".P7M") > 0) {
              firma = "S";
              wopen = "Firma";
            }
            if (visibile) {
            	retval += "<span"+classSpan+">\n";
              if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
              	retval += "<a href='../common/ServletVisualizza.do?ar="+area+"&amp;cm="+cm;
                if (firma.equalsIgnoreCase("S")) {
                	retval += "&amp;docVer="+versione_documento+"&amp;ca="+id_ogfi+"&amp;iddoc="+iddoc+"&amp;firma="+firma+"' ";
                } else {
                	retval += "&amp;docVer="+versione_documento+"&amp;ca="+id_ogfi+"&amp;iddoc="+iddoc+"&amp;firma=' ";
                }
                retval += "onclick='wleft = (screen.width - 500) / 2; wtop = (screen.height - 300) / 2; ";
                retval += "window.open(this.href,\""+wopen+"\",\"toolbar=no, scrollbars=1, menubar=no, width= 500, height= 300, left=\"+wleft+\", top=\"+wtop+\" \"); return false;' ";
                retval += "onkeypress='wleft = (screen.width - 500) / 2; wtop = (screen.height - 300) / 2; ";
                retval += "window.open(this.href,\""+wopen+"\",\"toolbar=no, scrollbars=1, menubar=no, width= 500, height= 300, left=\"+wleft+\", top=\"+wtop+\" \"); return false;' ";
                retval += "title='Attenzione apre una nuova finestra' >\n";
                retval += "<img src='../common/images/gdm/formati/"+icona+"' alt='Allegato'/> "+nomeFile+"</a> \n";
              } else {
              	retval += "<a href='ServletVisualizza?ar="+area+"&amp;cm="+cm;
                if (Parametri.ALLEGATI_SINGLE_SIGN_ON.equalsIgnoreCase("S")) {
                	retval += "&amp;docVer="+versione_documento+"&amp;ca="+id_ogfi+"&amp;iddoc="+iddoc+"&amp;firma="+firma+"' ";
                } else {
                	retval += "&amp;docVer="+versione_documento+"&amp;ca="+id_ogfi+"&amp;iddoc="+iddoc+"&amp;firma="+firma+"&amp;us="+us+"&amp;ruolo=GDM' ";
                }
                retval += "onclick='wleft = (screen.width - 500) / 2; wtop = (screen.height - 300) / 2; ";
                retval += "window.open(this.href,\""+wopen+"\",\"toolbar=no,scrollbars=1,menubar=no, width= 500, height= 300, left=\"+wleft+\", top=\"+wtop+\" \"); return false;' ";
                retval += "onkeypress='wleft = (screen.width - 500) / 2; wtop = (screen.height - 300) / 2; ";
                retval += "window.open(this.href,\""+wopen+"\",\"toolbar=no,scrollbars=1,menubar=no, width= 500, height= 300, left=\"+wleft+\", top=\"+wtop+\" \"); return false;' ";
                retval += "title='Attenzione apre una nuova finestra' >\n";
                retval += "<img src='images/gdm/formati/"+icona+"' alt='Allegato'/> "+nomeFile+"</a> \n";
              }
              retval += "</span>";
            }
					}
				} else {
					retval = "<div>ERRORE ACCESSO VERSIONE:<br/>"+pv.getError()+"</div>";

	        return retval;
				}
				retval += "</div></form>\n";
        if (!pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
        	retval += "</body></html>";
        }
      } else {
      	retval = "<span>Versione non presente</span>";

        return retval;
      }

      query = "SELECT NOMEFILE, RISORSA "+
							"FROM RISORSE_VERSIONI_DOCU "+
      				"WHERE ID_DOCUMENTO = :IDDOC"+
      				" AND VERSIONE = :VERSIONE";

      dbOp.setStatement(query);
      dbOp.setParameter(":IDDOC", iddoc);
      dbOp.setParameter(":VERSIONE", versione_documento);
      dbOp.execute();
      rst = dbOp.getRstSet();
      while (rst.next()) {
      	nomefile = rst.getString("NOMEFILE");
        nomefile = URLDecoder.decode(nomefile.substring(nomefile.lastIndexOf("/") + 1),"windows-1252");
        srcBlob = dbOp.readBlob("RISORSA");
        File ffile = new File(myPathTemp+File.separator+nomefile);

        if (srcBlob == null) {
          logger.error("Modulistica::visualizzaVersione() - Attenzione! InputStream vuoto.");
          throw new Exception("InputStream vuoto.");
        }

        BufferedInputStream bis = new BufferedInputStream(srcBlob);
        FileOutputStream fos = new FileOutputStream(ffile);
        byte buf[] = new byte[1];

        while( bis.read(buf) != -1)
          fos.write(buf);
        fos.flush();
        fos.close();
        bis.close();
        ffile.setLastModified(0);
      }
  	} catch (Exception e) {
      logger.error("Modulistica::visualizzaVersione() - Attenzione! Errore in fase di recupero versione."+e.toString(),e);
  		retval = "<spa>Errore in fase di recupero versione</span>";
  	}
    return retval;
  }


  private String getServletReindirizza() {
	      String sRind;

	      sRind = "<html>";
	      sRind += "<head><title>ServletReindirizza</title>";
	      sRind += "<meta http-equiv='refresh' content='0; URL="+gdc_link+"' />";
	      sRind += "<style type=\"text/css\">td.barra { background-color: #F9F9F9; border: Solid 1px";
	      sRind += "#CCCCCC; text-align: Center; color: #778899; font: Bold 11px Verdana; }</style>\n";
	      sRind += "</head>\n";
	      sRind += "<div id=\"loading\">\n";
	      sRind += "  <table width=\"222\" align=\"center\" cellpadding=\"1\" cellspacing=\"10\">\n";
	      sRind += "    <tr>\n";
	      sRind += "      <td class=\"barra\"><div>Attendere!</div><div>Operazione in corso</div></td>\n";
	      sRind += "    </tr>\n";
	      sRind += "  </table>\n";
	      sRind += "</div>\n";
	      sRind += "</html>\n";

	      return sRind;
  }

  private String getServletConservazione(int numeroDoc, String listaid, String data_iter, String queryUrl) {
    String sRind;

//    sRind = "<html>";
//    sRind += "<head><title>ServletConservazione</title>";
    sRind = "<style type=\"text/css\">\ntd.barra { BORDER-BOTTOM: black 1px solid; BORDER-LEFT: black 1px solid;";
    sRind += "  BORDER-TOP: black 1px solid; BORDER-RIGHT: black 1px solid }\n";
    sRind += "td.barra2 { background-color: #F9F9F9; border: Solid 1px";
    sRind += "#CCCCCC; text-align: Center; color: #778899; font: Bold 11px Verdana; }\n</style>\n";
//    sRind += "</head>\n" +
    sRind += "<script type=\"text/javascript\" src=\"../gdmAjax.js\"></script>\n";
    sRind += "<script type=\"text/javascript\" src=\"../DatePicker.js\"></script>\n";
    sRind += "<script type=\"text/javascript\" src=\"controlli.js\"></script>\n";
    sRind += "<script type=\"text/javascript\" >\n"+
             "function gdmResize() {\n"+
             "  var altezza = 0;\n"+
             "  if (navigator.appName.indexOf(\"Netscape\") != -1) {\n"+
             "    altezza = window.innerHeight - document.getElementById(\"gdm_corpo\").offsetTop;\n"+
             "  } else {\n"+
             "    altezza = document.body.offsetHeight - document.getElementById(\"gdm_corpo\").offsetTop;\n"+
             "  }\n"+
             "  document.getElementById(\"gdm_corpo\").style.height = (altezza - 40);\n"+
             "}\n\n"+
             "if (navigator.appName.indexOf(\"Netscape\") != -1) {\n"+
             "  window.addEventListener(\"load\", gdmResize, false)\n"+
             "  window.addEventListener(\"resize\", gdmResize, false)\n"+
             "} else {\n"+
             "  window.attachEvent(\"onload\", gdmResize);\n"+
             "  window.attachEvent(\"onresize\", gdmResize);\n"+
             "}\n"+
             "function gdmClick() {\n"+
             "  document.getElementById(\"gdm_corpo\").style.display = 'none';\n"+
             "  document.getElementById(\"gdm_toolbar\").style.display = 'none';\n"+
             "  document.getElementById(\"loading\").style.display = 'block';\n"+
             "  var opzione = getValoreCampo('SCELTA_OPZIONE');\n"+
             "  if (opzione == 3) {\n"+
             "    document.getElementById(\"submitForm\").action = '"+gdc_link+"'\n"+
             "  }\n"+
             "  document.getElementById(\"submitForm\").submit();\n"+
             "}\n"+
             "</script>\n";
    sRind += "<form method='post' id='submitForm' action='ServletRicercaModulistica.do?"+queryUrl+"' >\n";
    sRind += "<div id='gdm_corpo' style='overflow-y: auto;text-align: center;'>\n";
    sRind += "<br/><br/><div align='center'><strong><span style='FONT-SIZE: 14pt'>Conservazione sostitutiva</span></strong></div><br/>";
    sRind += "  <table style=\"BORDER-BOTTOM: black 1px solid; BORDER-LEFT: black 1px solid; WIDTH: 60%; BORDER-COLLAPSE: collapse; BORDER-TOP: black 1px solid; BORDER-RIGHT: black 1px solid\">\n";
    sRind += "    <tr>\n";
    sRind += "      <td class=\"barra2\" colspan=2 ><strong><div align='center'>Attezione!</div><div align='center' >Trovati n� "+numeroDoc+" documenti</div></strong></td>\n";
    sRind += "    </tr>\n";
    sRind += "    <tr>\n";
    if (numeroDoc > 0) {
    	sRind += "      <td class=\"barra\"  width='30px' ><div align='center' ><input class='AFCInput' type='radio' name='SCELTA_OPZIONE' checked=true value='1' /></div></td><td class=\"barra\"><div>Conserva i documenti il";
      sRind += " <input class='AFCInput' type='input' name='data_iter'  size='10' maxlength='10' value='"+data_iter+"' onblur='data(document.getElementById(\"submitForm\").data_iter,\"dd/mm/yyyy\");' ><script type='text/javascript' >var submitForm_DatePicker_data_iter = new Object(); submitForm_DatePicker_data_iter.format = 'dd/mm/yyyy';submitForm_DatePicker_data_iter.style = 'Themes/AFC/Style.css'; submitForm_DatePicker_data_iter.relativePathPart = '../'; </script>";
      sRind += "&nbsp;<a class='AFCDataLink' href='javascript:showDatePicker(\"submitForm_DatePicker_data_iter\",\"submitForm\",\"data_iter\");' ><img style='border: none' src='../Themes/DatePicker/DatePicker1.gif' alt='DatePicker' /></a>&nbsp;";
      sRind += "alle <input class='AFCInput' type='input' name='ora_iter'  size='2' maxlength='2' value='00' >:<input class='AFCInput' type='input' name='minuti_iter'  size='2' maxlength='2' value='00' ></div></td>\n";
      sRind += "    </tr>\n";
      sRind += "    <tr>\n";
      sRind += "      <td class=\"barra\" width='30px' ><div align='center' ><input class='AFCInput' type='radio' name='SCELTA_OPZIONE' value='2' /></div></td><td class=\"barra\"><div>Marca per la conservazione</div></td>\n";
      sRind += "    </tr>\n";
      sRind += "    <tr>\n";
      sRind += "      <td class=\"barra\" width='30px' ><div align='center' ><input class='AFCInput' type='radio' name='SCELTA_OPZIONE' value='3' /></div></td><td class=\"barra\"><div>Mostra i documenti trovati</div></td>\n";
    } else {
    	sRind += "      <td class=\"barra\"  width='30px' ><div align='center' ><input class='AFCInput' type='radio' name='SCELTA_OPZIONE' disabled value='1' /></div></td><td class=\"barra\"><div>Conserva i documenti il";
      sRind += " <input class='AFCInput' type='input' name='data_iter'  size='10' maxlength='10' value='"+data_iter+"' onblur='data(document.getElementById(\"submitForm\").data_iter,\"dd/mm/yyyy\");' ><script type='text/javascript' >var submitForm_DatePicker_data_iter = new Object(); submitForm_DatePicker_data_iter.format = 'dd/mm/yyyy';submitForm_DatePicker_data_iter.style = 'Themes/AFC/Style.css'; submitForm_DatePicker_data_iter.relativePathPart = '../'; </script>";
      sRind += "&nbsp;<a class='AFCDataLink' href='javascript:showDatePicker(\"submitForm_DatePicker_data_iter\",\"submitForm\",\"data_iter\");' ><img style='border: none' src='../Themes/DatePicker/DatePicker1.gif' alt='DatePicker' /></a>&nbsp;";
      sRind += "alle <input class='AFCInput' type='input' name='ora_iter'  size='2' maxlength='2' value='00' >:<input class='AFCInput' type='input' name='minuti_iter'  size='2' maxlength='2' value='00' ></div></td>\n";
      sRind += "    </tr>\n";
      sRind += "    <tr>\n";
      sRind += "      <td class=\"barra\" width='30px' ><div align='center' ><input class='AFCInput' type='radio' name='SCELTA_OPZIONE' disabled value='2' /></div></td><td class=\"barra\"><div>Marca per la conservazione</div></td>\n";
      sRind += "    </tr>\n";
      sRind += "    <tr>\n";
      sRind += "      <td class=\"barra\" width='30px' ><div align='center' ><input class='AFCInput' type='radio' name='SCELTA_OPZIONE' checked=true value='3' /></div></td><td class=\"barra\"><div>Mostra i documenti trovati</div></td>\n";
    }
    sRind += "    </tr>\n";
    sRind += "  </table>\n";
    sRind += "<input type='hidden' name='fase' value='submit'/>\n";
    sRind += "<input type='hidden' name='listaid' value='"+listaid+"' />\n<input type='hidden' name='_GDM_CONSERVAZIONE' value='E' />\n</div>\n";
    sRind += "<div id='gdm_toolbar' class='AFCFooterTD' style='text-align: right; overflow: visible'>\n";
    sRind += "<input class='AFCButton' style='cursor: hand;' type='button' name='continua' value='Esegui' onclick='gdmClick();'/>";
    sRind += "</div>\n";
    sRind += "<div id='loading' style='display: none'>\n";
    sRind += "  <table width=\"222\" align=\"center\" cellpadding=\"1\" cellspacing=\"10\">\n";
    sRind += "    <tr>\n";
    sRind += "      <td class=\"barra2\"><div>Attendere!</div><div>Operazione in corso</div></td>\n";
    sRind += "    </tr>\n";
    sRind += "  </table>\n";
    sRind += "</div>\n";
    sRind += "</form>\n";
//    sRind += "</html>\n";

    return sRind;
}

  /**
   * Caso QUERY scritta dall'utente
   * I parametri vanno esclusi se non sono stati
   * specificati in maschera, si vanno quindi
   * a cercare i TAG <@:NOMEPARAMETRO></@:NOMEPARAMETRO>
   * per eventualmente sostituirli o escluderli a seconda
   * del valore che arriva dalla request
   *
  */
  private String effettuaRicerca(HttpServletRequest request, String strSelect) throws Exception {
	  final String _TAG_PARAMETER           ="@";
	  final String _AL_PREFIX               ="_2_";
	  final String _AL_SUFFIX_SELECT        ="_AL";
	  final String _DAL_SUFFIX_SELECT       ="_DAL";
	  final String _SPACE                   =" ";
	  final String _APICE                   ="'";

	  final String _BETWEEN                 =" BETWEEN";
	  final String _AND                     =" AND";
	  final String _WHERE                   =" WHERE";
	  final String _INTERSECT               =" INTERSECT";
	  final String _UNION                   =" UNION";

  	  //in 3.x
  	  final String _TAG_PARAMETER_PROTECT     ="<CHIOC>";

	  String sSqlResult=strSelect.replaceAll("\t"," ");
	  String sSqlResultUpper;
	  String nomeParametro;
	  String condizione;

	  //Fino a quando trovo un separatore di tipo "PARAMETRO"
	  //entro nel ciclo e lo tratto
	  while (sSqlResult.indexOf(_TAG_PARAMETER)!=-1) {
		    sSqlResultUpper=sSqlResult.toUpperCase();

		    int indiceFineParametroSpace,indiceFineParametroAt,indiceFineParametro;
		    int indiceInizioParametro;

		    indiceInizioParametro=sSqlResult.indexOf(_TAG_PARAMETER);
		    indiceFineParametroSpace=sSqlResult.indexOf(_SPACE,indiceInizioParametro);
		    indiceFineParametroAt=sSqlResult.indexOf(_APICE,indiceInizioParametro);

		    //*************RECUPERO IL PARAMETRO
		    //Un parametro inizia sempre con la @ e finisce sempre con uno spazio o con un '
		    if (indiceFineParametroAt!=-1 && indiceFineParametroAt<indiceFineParametroSpace)
		    	indiceFineParametro=indiceFineParametroAt;
		    else if (indiceFineParametroSpace!=-1)
		    	indiceFineParametro=indiceFineParametroSpace;
		    else
		    	indiceFineParametro=0;

		    if (indiceFineParametro==0)
		    	nomeParametro=sSqlResult.substring(indiceInizioParametro).replace("\n","").replace("\r","");
		    else
		    	nomeParametro=sSqlResult.substring(indiceInizioParametro,indiceFineParametro ).replace("\n","").replace("\r","");
		    //*************FINE RECUPERO DEL PARAMETRO

		    //System.out.println(nomeParametro);
		    //*************RECUPERO DELLA CONDIZIONE (DA AND ALL'ALTRO AND)
		    int indiceBetween, indiceAndOrWhereInizioCondizione;
		    int indiceOtherCondition;

		    indiceBetween=sSqlResultUpper.lastIndexOf(_BETWEEN,indiceInizioParametro);
		    indiceAndOrWhereInizioCondizione=nvlLastIndexOf(sSqlResultUpper,_AND,_WHERE,indiceInizioParametro);

		    if (indiceAndOrWhereInizioCondizione==-1)
		    	throw new Exception("Attenzione! select mal formata: mancano and o where prima della condizione che contiene il parametro "+nomeParametro+".\nSELECT="+sSqlResult);

		    //Siamo in un between
		    if (indiceBetween>indiceAndOrWhereInizioCondizione) {
		    	//Indice dell'AND dopo il between
		    	int indiceAndPostBetween=sSqlResultUpper.indexOf(_AND,indiceBetween);
		    	//Indice del parametro (2� par. del between) dopo il between
		    	int indiceParameterPostBetween=sSqlResultUpper.indexOf(_TAG_PARAMETER,indiceFineParametro);
		    	//Indice del 2� AND dopo il between (inizio dell'altra condizione) o di una select in union/intersect oppure la fine dell'SQL
		    	indiceOtherCondition=nvlIndexOf(sSqlResultUpper,_AND,_UNION,_INTERSECT,indiceAndPostBetween +1 );

		    	if (indiceAndPostBetween==-1)
		    		throw new Exception("Attenzione! select mal formata: manca and dopo between nella condizione che contiene il parametro "+nomeParametro+".\nSELECT="+sSqlResult);

		    	//if (indiceParameterPostBetween==-1)
		    	//	throw new Exception("Attenzione! select mal formata: manca parametro '_al' dopo between nella condizione che contiene il parametro "+nomeParametro+".\nSELECT="+sSqlResult);

		    	if (indiceAndPostBetween>indiceParameterPostBetween)
		    		throw new Exception("Attenzione! select mal formata: la condizione between che contiene il parametro "+nomeParametro+" � mal formata.\nSELECT="+sSqlResult);

		    }
		    //Condizione normale
		    else {
		    	//Indice dell'AND dopo il parametro (inizio dell'altra condizione) o di una select in union/intersect oppure la fine dell'SQL
		    	indiceOtherCondition=nvlIndexOf(sSqlResultUpper,_AND,_UNION,_INTERSECT,indiceInizioParametro);
		    }

		    //Estraggo la condizione

	    	//Sono alla fine dell'sql
	    	if (indiceOtherCondition==-1)
	    		condizione=sSqlResult.substring(indiceAndOrWhereInizioCondizione );
	    	//Spezzo fino alla prox condizione
	    	else
	    		condizione=sSqlResult.substring(indiceAndOrWhereInizioCondizione ,indiceOtherCondition );

		    //*************FINE RECUPERO DELLA CONDIZIONE (DA AND ALL'ALTRO AND)




	    	//*************BINDING DELLA CONDIZIONE

	    	String newCondizione;
  		    newCondizione=condizione;
  		    //Se siamo in presenza di una condizione di between
		    //devo cercare il parametro due volte con il
		    //dal e con l'al
  		if (condizione.toUpperCase().indexOf(_BETWEEN)!=-1) {
  		    	if (nomeParametro.lastIndexOf(_DAL_SUFFIX_SELECT)==-1)
  		    		throw new Exception("Attenzione! nella condizione di between il parametro "+nomeParametro+" � mal formato: specificare _DAL e _AL (MAIUSCOLI) come suffissi dei pametri stessi.\nSELECT="+sSqlResult);

  		    	String nomePar=nomeParametro.replace("@","").substring(0,nomeParametro.indexOf(_DAL_SUFFIX_SELECT) - 1);
  		    	String valoreParametro_dal=nvl(request.getParameter(nomePar),"");
  		    	String valoreParametro_al=nvl(request.getParameter(_AL_PREFIX+nomePar),"");
  		    	//String valoreParametro_dal="";
  		    	//String valoreParametro_al="";

  		    	//Devo aggiungere i minuti
  		    	String sMinutiDal="", sMinutiAl="";
  		    	if (condizione.toLowerCase().indexOf("hh24:")!=-1 || condizione.toLowerCase().indexOf("hh:")!=-1) {
  		    		sMinutiDal=" 00:00:00";
  		    		sMinutiAl=" 23:59:59";
  		    		newCondizione=newCondizione.replaceAll("hh24:","HH24:").replaceAll("hh:","HH24:");
  		    	}

  		    	if (valoreParametro_dal.length() == 0)
  		    		valoreParametro_dal = valoreParametro_al;

  		        if (valoreParametro_al.length() == 0)
  		            valoreParametro_al = valoreParametro_dal;

  		        if (valoreParametro_dal.length() != 0) {
  		        	newCondizione=newCondizione.replaceAll(nomeParametro,valoreParametro_dal+sMinutiDal);
  		        	newCondizione=newCondizione.replaceAll(nomeParametro.substring(0,nomeParametro.lastIndexOf(_DAL_SUFFIX_SELECT))+_AL_SUFFIX_SELECT,valoreParametro_al+sMinutiAl);
  		        }
  		        else
  		        	newCondizione="";

  		 }
		     else {
		    	 String valoreParametro=nvl(request.getParameter(nomeParametro.replace("@","")),"");
  		     //String valoreParametro="A";


	  		     if (valoreParametro.equals(""))
	  		    	newCondizione="";
		  		 else {
		  			newCondizione=newCondizione.replaceAll(nomeParametro,valoreParametro.replaceAll("'","''"));
	  		        newCondizione=newCondizione.replaceAll(_TAG_PARAMETER,_TAG_PARAMETER_PROTECT);
		  		 }
		     }

	    	//*************FINE BINDING DELLA CONDIZIONE




  		    //*************SOSTITUZIONE DELLA CONDIZIONE
  		    //System.out.println(condizione);
		    //System.out.println(newCondizione);
		    StringBuffer strAppoggio = new StringBuffer("");
		    strAppoggio.append(sSqlResult.substring(0,indiceAndOrWhereInizioCondizione));
		    strAppoggio.append(" ");
		    strAppoggio.append(newCondizione);
		    strAppoggio.append(" ");
		    strAppoggio.append(sSqlResult.substring(indiceOtherCondition));
		    sSqlResult=strAppoggio.toString();
		    //System.out.println(sSqlResult);
	  }

	  sSqlResult=sSqlResult.replaceAll(_TAG_PARAMETER_PROTECT,_TAG_PARAMETER);

	  return sSqlResult;
  }

  private String nvl(String campo, String valore) {
    if (campo==null) return valore;

    return campo;
  }

  private int nvlIndexOf(String strToControl, String str1, String str2, String str3, int fromIndex) {
    int indice=strToControl.indexOf(str1,fromIndex);
    int indice2=strToControl.indexOf(str2,fromIndex);
    int indice3=-1;

    if (str3!=null) indice3=strToControl.indexOf(str3,fromIndex);

    if (indice3!=-1 && indice3<indice2 && indice3<indice) {
  	  return indice3;
    }
    else {
  	  if (indice2!=-1 && indice2<indice)
  		  return indice2;
  	  else
  		  return indice;
    }

  }

  private int nvlIndexOf(String strToControl, String str1, String str2, int formIndex) {
    return nvlIndexOf(strToControl, str1, str2, null, formIndex);
  }

  private int nvlLastIndexOf(String strToControl, String str1, String str2, String str3, int fromIndex) {
    int indice=strToControl.lastIndexOf(str1,fromIndex);

    if (indice==-1) indice=strToControl.lastIndexOf(str2,fromIndex);

    if (indice==-1 && str3!=null) indice=strToControl.lastIndexOf(str3,fromIndex);

    return indice;

  }

  private int nvlLastIndexOf(String strToControl, String str1, String str2, int formIndex) {
    return nvlLastIndexOf(strToControl, str1, str2, null, formIndex);
  }

  private String visualizzaMessagio(String msg) {
  	String chiudi = "<input type='button' value='Chiudi' onclick='window.close()' />";
  	if (lettura.equalsIgnoreCase("Q")) {
  		chiudi = "";
  	}
  	String retval = "<html><head></head><body><form>" +
  					 "<p align='center'><table><tr><td><p align='center'><b><font color='#FF0000' size='5'>Attenzione, </font></b>"+
  	         "<font size='5' color='#FF0000'><b>Accesso Negato!</b></font></td></tr><tr><td>"+
  	         "<p align='center'><font size='4'><b>"+msg+"</b></font>"+
  	         "</td></tr>"+
  	         "<tr><td><p align='center'>"+chiudi+"</p></td></tr></p>"+
  	         "</body></form></html>";
  	return retval;
  }

  public void disabilitaReload(boolean disabilita) {
  	disabilitaReload = disabilita;
  }

  private void verificaParametroGet(String parametro, String valore) throws Exception {
  	if (valore == null) {
  		return;
  	}

  	String newVal = encode(valore, "S");
  	if (!newVal.equals(valore)) {
  		throw new Exception("Errore. Parametro "+parametro+" non valido!");
  	}
  }
}

class ControlloModello {
  private String controllo, corpo, campi, errMsg;
  private String conDRIVER,conCONNESIONE,conUTENTE,conPASSWD,sDsn;

  public ControlloModello(String controllo, String corpo, String campi, String errMsg) {
    this.controllo = controllo;
    this.corpo = corpo;
    this.campi = campi;
    this.errMsg = errMsg;
  }

  public ControlloModello(String controllo, String corpo, String campi, String errMsg, String conDRIVER,
      String conCONNESIONE, String conUTENTE, String conPASSWD, String sDsn) {
    this.controllo = controllo;
    this.corpo = corpo;
    this.campi = campi;
    this.errMsg = errMsg;
    this.conDRIVER = conDRIVER;
    this.conCONNESIONE = conCONNESIONE;
    this.conUTENTE = conUTENTE;
    this.conPASSWD = conPASSWD;
    this.sDsn = sDsn;
  }

  public String getControllo() {
    return controllo;
  }

  public void setControllo(String controllo) {
    this.controllo = controllo;
  }

  public String getCorpo() {
    return corpo;
  }

  public void setCorpo(String corpo) {
    this.corpo = corpo;
  }

  public String getCampi() {
    return campi;
  }

  public void setCampi(String campi) {
    this.campi = campi;
  }

  public String getErrMsg() {
    return errMsg;
  }

  public void setErrMsg(String errMsg) {
    this.errMsg = errMsg;
  }

  public String getConDRIVER() {
    return conDRIVER;
  }

  public void setConDRIVER(String conDRIVER) {
    this.conDRIVER = conDRIVER;
  }

  public String getConCONNESIONE() {
    return conCONNESIONE;
  }

  public void setConCONNESIONE(String conCONNESIONE) {
    this.conCONNESIONE = conCONNESIONE;
  }

  public String getConUTENTE() {
    return conUTENTE;
  }

  public void setConUTENTE(String conUTENTE) {
    this.conUTENTE = conUTENTE;
  }

  public String getConPASSWD() {
    return conPASSWD;
  }

  public void setConPASSWD(String conPASSWD) {
    this.conPASSWD = conPASSWD;
  }

  public String getsDsn() {
    return sDsn;
  }

  public void setsDsn(String sDsn) {
    this.sDsn = sDsn;
  }
}

class Controllo {
  private String etichetta, controllo, iterPuls, salvataggio;

  public Controllo(String etichetta, String controllo, String iterPuls, String salvataggio) {
    this.etichetta=etichetta;
    this.controllo=controllo;
    this.iterPuls=iterPuls;
    this.salvataggio=salvataggio;
  }

  public String getEtichetta() {
    return etichetta;
  }

  public void setEtichetta(String etichetta) {
    this.etichetta = etichetta;
  }

  public String getControllo() {
    return controllo;
  }

  public void setControllo(String controllo) {
    this.controllo = controllo;
  }

  public String getIterPuls() {
    return iterPuls;
  }

  public void setIterPuls(String iterPuls) {
    this.iterPuls = iterPuls;
  }

  public String getSalvataggio() {
    return salvataggio;
  }

  public void setSalvataggio(String salvataggio) {
    this.salvataggio = salvataggio;
  }
}

class DatoModello {
  private String dato, calcolato, tipoCampo, tipoAccesso;

  public DatoModello(String dato, String calcolato, String tipoCampo, String tipoAccesso) {
    this.dato = dato;
    this.calcolato = calcolato;
    this.tipoCampo = tipoCampo;
    this.tipoAccesso = tipoAccesso;
  }

  public String getDato() {
    return dato;
  }

  public void setDato(String dato) {
    this.dato = dato;
  }

  public String getCalcolato() {
    return calcolato;
  }

  public void setCalcolato(String calcolato) {
    this.calcolato = calcolato;
  }

  public String getTipoCampo() {
    return tipoCampo;
  }

  public void setTipoCampo(String tipoCampo) {
    this.tipoCampo = tipoCampo;
  }

  public String getTipoAccesso() {
    return tipoAccesso;
  }

  public void setTipoAccesso(String tipoAccesso) {
    this.tipoAccesso = tipoAccesso;
  }
}

class InoltroStruct {
  private String idOp, className, dsn;
  private Clob parametri;

  public InoltroStruct(String idOp, String className, String dsn, Clob parametri) {
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