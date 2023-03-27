package it.finmatica.modulistica.modulisticapack;
import javax.servlet.http.*;
import java.util.*;
import java.io.Serializable;
import java.net.URLEncoder;

import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.jsuitesync.SyncSuite;
import it.finmatica.modulistica.parametri.Parametri;
import java.sql.*;
//import it.finmatica.jfc.authentication.Cryptable;
import it.finmatica.modutils.informazioniTab.InformazioniTab;
import it.finmatica.modutils.informazioniPopup.InformazioniPopup;
import org.apache.log4j.Logger;
 
/** 
 * ModelloHTMLIn
 * 
 * @author       Adelmo Gentilini
 * @version      1.0
 */

public class ModelloHTMLIn extends Modello implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  protected Timestamp      scadenza;
  private String           err_msg = "";
  private String           id_doc = "";
  private String           list_fields = "";
  private String           pPrecedente = "";
  private String           pSuccessivo = "";
  private String           pRegistra = "";
  private String           pRegistraInoltra = "";
  private String           pSalva = "";
  private String           pAggiorna = "";
  private String           gdm_new_doc = "N";
  protected ListaControlli controlli;
  private  static Logger logger = Logger.getLogger(ModelloHTMLIn.class);
  private Dati datiRicerca = null;

  public void setDatiRicerca(Dati datiRicerca) {
    this.datiRicerca = datiRicerca;
  }

  public ModelloHTMLIn(HttpServletRequest request, String a, String c, String pCodiceRichiesta, Timestamp scad, String firstOpen) throws Exception {
    this(request,a,c,pCodiceRichiesta,scad,firstOpen,null);
  }

  /**
   * 
   */
  public ModelloHTMLIn(HttpServletRequest request, String a, String c, String pCodiceRichiesta, Timestamp scad, String firstOpen, IDbOperationSQL dbOpEsterna) throws Exception {
    super(request, a, c, pCodiceRichiesta,dbOpEsterna);

    setTimeFirstOpen(firstOpen);
    scadenza = scad;
    if (!lettura.equals("V")) {
      int i = request.getQueryString().indexOf("&urlCut");
      if (i > -1) {
        queryHtmlIn = request.getQueryString().substring(0,i);
      } else {
        queryHtmlIn = request.getQueryString();
      }
      if (queryHtmlIn == null) {
        queryHtmlIn = "";
      }
    } else {
    	queryHtmlIn = "";
    }
    controlli        =  new ListaControlli(request, a, c, "M",dbOpEsterna);
    pPrecedente      = Parametri.getParametriLabel(a,c,"PRECEDENTE");
    pSuccessivo      = Parametri.getParametriLabel(a,c,"SUCCESSIVO");
    pRegistra        = Parametri.getParametriLabel(a,c,"REGISTRA");
    pRegistraInoltra = Parametri.getParametriLabel(a,c,"REGISTRAINOLTRA");
    pSalva           = Parametri.getParametriLabel(a,c,"SALVA");
    pAggiorna        = Parametri.getParametriLabel(a,c,"AGGIORNA");

//    interpretaModello();
  }

//  /**
//   * Aggiorna i valori dei campi legati al modello.
//   * Va fatta ogni volta che viene reimpostata la request, perchè si puo' supporre
//   * di avere fatto un back senza submit magari cambiando dei dati nella form
//   * precedente.
//   */
//  public void aggiornaValori(HttpServletRequest request) throws Exception {
//    //Debug Tempo
//    long ptime = stampaTempo("ModelloHTMLIn::aggiornaValori - Inizio",area,codiceModello,codiceRichiesta,0);
//    //Debug Tempo
//    IElementoModello iem;
////    DbOperationSQL   dbOp = null;
//    int              i, aggFatti;
//
//    aggFatti = 0;
//     
//    try {
////      dbOp = new DbOperationSQL(parametri.ALIAS, parametri.SPORTELLO_DSN, parametri.USER, parametri.PASSWD);
//      for (i=0; i<elementi.size(); i++){
//        iem = (IElementoModello)elementi.get(i);
//        if ((iem.getClass().getName()).equalsIgnoreCase("it.finmatica.modulistica.modulisticapack.CampoHTMLIn")) {
//          ((Campo)iem).caricaValore(request);  // E' un campo di input
//          aggFatti = aggFatti+1;
//        }
//      }
//    } catch(Exception ex) {
//      loggerError("ModelloHTMLIn::aggiornaValori() - Errore nel ricaricamento dei dati",ex);
////      ex.printStackTrace();
//    }
//    //Debug Tempo
//    stampaTempo("ModelloHTMLIn::aggiornaValori - Fine",area,codiceModello,codiceRichiesta,ptime);
//    //Debug Tempo
//  }

  public void interpretaModello() throws Exception {
    interpretaModello(null);
  }

 /**
  * Funzione senza parametri che costruisce a partire dal primo carattere dopo la stringa
  * TAGENDHEADER la lista degli IElementoModello separando in maniera ordinata blocchi di
  * testo e campi.
  * Prima di attivare l'interpretazione setta i parametri leggendoli dal database, essendo
  * specifici in base al tipo di modello
  */
  public void interpretaModello(IDbOperationSQL dbOpEsterna) throws Exception {
    //Debug Tempo
    long ptime = stampaTempo("ModelloHTMLIn::interpretaModello - Inizio",area,codiceModello,codiceRichiesta,0);
    //Debug Tempo
    // Ciclo di lettura e interpretazione del testo del modello:
    //    --> estrapolazione e creazione di tutti i campi
    int               startChar, j, jfield, jsrc, jblock, 
                      jvisual, jfinevi, jtab, jpagina,
                      jpopup, jlength, k;
    int               numeroPagina = 0;
    String            subModello, campoHtml, srcHtml, bloccoHtml;
    IElementoModello  iem;
    boolean           nonCaricareBlocco = false;
    ListaProtetti   protetti;
    protetti = (ListaProtetti)sRequest.getSession().getAttribute("listaProtetti");
    if (protetti == null) {
      protetti = new ListaProtetti();
      protetti.caricaDominii(area,codiceModello,sRequest,dbOpEsterna);
      sRequest.getSession().setAttribute("listaProtetti", protetti);
    }

    Parametri.settaParametriModello(tipo);   // Imposta i parametri per l'interpretazione del modello
    elementi.clear();
    tabpage = "";
    paginaAttuale   = sRequest.getParameter("GDM_PAGINA_ATTUALE");
    if (paginaAttuale == null) {
      paginaAttuale = "1";
    }

    if (paginaAttuale.length() == 0) {
      paginaAttuale = "1";
    }
    creaLinkAllegati();
    // ---------------------------------------------------------
    // I°  fase: salto tutto cio' che è il testo fino al </HEAD>
    // ---------------------------------------------------------    
    startChar = modello.indexOf(Parametri.getTagHeadEnd());
    if (startChar == -1){
      // Il carattere da cui partire è il primo
      startChar = 0;
    } else {
      // Il carattere da cui partire è startChar + 7 (la lunghezza di </HEAD>)
      startChar = startChar + Parametri.getTagHeadEnd().length();
    }
    // -----------------------------------------------------------------
    // II° fase: analizzo ed interpreto la parte rimanente del file HTML
    // -----------------------------------------------------------------    
    subModello = modello.substring(startChar);
    j = 0;

    while ((j != -1) && (!subModello.equals(""))) {
      jlength = subModello.length();
      jfield  = subModello.indexOf(Parametri.getNameFieldBegin());
      jsrc    = subModello.indexOf(Parametri.getTagImgBegin());
      jblock  = subModello.indexOf(Parametri.getNameBlockBegin());
      jvisual = subModello.indexOf(Parametri.getTagVisualBegin());
      jfinevi = subModello.indexOf(Parametri.getTagVisualFineBegin());
      jtab    = subModello.indexOf(Parametri.getTagTabBegin());
      jpagina = subModello.indexOf(Parametri.getTagPageBegin());
      jpopup  = subModello.indexOf(Parametri.getTagPopBegin());
      
      if (jfield == -1) {
        jfield = jlength;
      }

      if (jsrc == -1) {
        jsrc = jlength;
      }

      if (jblock == -1) {
        jblock = jlength;
      }
      
      if (jvisual == -1) {
        jvisual = jlength;
      }
      
      if (jfinevi == -1) {
        jfinevi = jlength;
      }
      
      if (jtab == -1) {
        jtab = jlength;
      }
      
      if (jpagina == -1) {
        jpagina = jlength;
      }
      
      if (jpopup == -1) {
        jpopup = jlength;
      }
      
      if ((jtab != jlength)     || 
          (jpagina != jlength)  || 
          (jfield != jlength)   || 
          (jsrc != jlength)     || 
          (jblock != jlength)   || 
          (jvisual != jlength)  || 
          (jpopup != jlength)   || 
          (jfinevi != jlength)) {
        // Esiste un altro campo di Input oppure una SRC oppure un Blocco multirecord
        if ((jsrc < jtab) && 
            (jsrc < jfield) && 
            (jsrc < jblock) && 
            (jsrc < jvisual) && 
            (jsrc < jfinevi) && 
            (jsrc < jpopup) && 
            (jsrc < jpagina)) {
          // E' una SRC
          j = jsrc;
          // In questo caso i caratteri corrispondenti a TagImgBegin e TagImgEnd li inserisco
          // nei campi testo (rispettivamente prima e dopo l'oggetto GRAFICO trovato)
          iem = new Testo(subModello.substring(0, j + Parametri.getTagImgBegin().length()));
          elementi.add(iem);
          k = j + Parametri.getTagImgBegin().length() +
              subModello.substring(j + Parametri.getTagImgBegin().length()).indexOf(Parametri.getTagImgEnd());
          try {
            srcHtml = subModello.substring(j + Parametri.getTagImgBegin().length(), k);
            iem = new Grafico(sRequest, area, codiceModello, srcHtml, dbOpEsterna);
            elementi.add(iem);
          } catch(Exception ee) {
            iem = new Testo("#ERRORE COSTRUZIONE GRAFICO->  " + subModello.substring(j, k + Parametri.getTagImgEnd().length()) + " <-FINE#");
            elementi.add(iem);
          }
          // Mi resta da analizzare tutto quanto si trova dal TAGIMGEND compreso in avanti
          // ossia tutto quello che c'è dopo K che rappresenta l'oggetto grafico
          // In pratica lascio i doppi apici che chiudono il comando IMG SRC
          subModello = subModello.substring(k);
        } 
        if ((jfield < jtab) && 
            (jfield < jsrc) && 
            (jfield < jblock) && 
            (jfield < jvisual) && 
            (jfield < jfinevi) && 
            (jfield < jpopup) && 
            (jfield < jpagina)) {
          // E' un campo !!!
          j = jfield;
          //  ---> Da 0 a j-1 è un TESTO ---> Attenzions Substring NON include J !!!
          iem = new Testo(subModello.substring(0, j));
          elementi.add(iem);
          k = subModello.indexOf(Parametri.getNameFieldEnd());
          if (k != -1) {
            // ---> Da j a k+NAMEFIELDEND.length() è un CAMPO
            int startCh = 0,endChar;
            String sHtml = "";
            startCh = k + Parametri.getNameFieldEnd().length();
            endChar = startCh;
            try {
              sHtml = subModello.substring(startCh,startCh + 10);
              if (sHtml.equalsIgnoreCase("<!-- style") ||
                  sHtml.equalsIgnoreCase("&gt;<!-- s")) {
                endChar = subModello.indexOf("-->",startCh);
                if (endChar != -1) {
                  endChar += 3;
                } else {
                  endChar = startCh;
                }
              }
              String fineChr = "&gt;";
              if (subModello.indexOf(fineChr,k) == endChar) {
                endChar = endChar+fineChr.length();
              } 
              campoHtml = subModello.substring(j, endChar);
              iem = new CampoHTMLIn(sRequest, area, codiceModello, campoHtml, scadenza,dbOpEsterna);
              ((CampoHTMLIn)iem).settaStrCampiObb(str_campi_obb);
              elementi.add(iem);
              String bloccoPopup = ((CampoHTMLIn)iem).getBlocco();
              String dato = ((CampoHTMLIn)iem).getDato();
              if (bloccoPopup != null) {
                iem = new BloccoPopup(sRequest,area,codiceModello,dato,area,bloccoPopup, bloccoPopup, pdo,"", dbOpEsterna);
                elementi.add(iem);
              }
                
            } catch(Exception ee) {
              ee.printStackTrace();
              iem = new Testo("#ERRORE COSTRUZIONE CAMPO->  " + subModello.substring(j, k + Parametri.getNameFieldEnd().length()) + " <-FINE#");
              elementi.add(iem);
            }
            // Mi resta da analizzare tutto quanto si trova dopo il TAGFIELDEND
            subModello = subModello.substring(endChar);
          } else {
            // ERRORE: Manca il tag di fine campo
            // Blocco la ricerca
            j = -1;
          }
        }
        if ((jblock < jtab) && 
            (jblock < jsrc) && 
            (jblock < jfield) && 
            (jblock < jvisual) && 
            (jblock < jfinevi) && 
            (jblock < jpopup) && 
            (jblock < jpagina)) {
          // E' un blocco multirecord !!!
          j = jblock;
          //  ---> Da 0 a j-1 è un TESTO ---> Attenzions Substring NON include J !!!
          iem = new Testo(subModello.substring(0, j));
          elementi.add(iem);
          k = subModello.indexOf(Parametri.getNameBlockEnd());
          if (k != -1) {
            // ---> Da j a k+NAMEBLOCKEND.length() è un BLOCCO
            int startCh = 0,endChar;
//            String sHtml = "";
            startCh = k + Parametri.getNameBlockEnd().length();
            endChar = startCh;
            try {
              String fineChr = "&gt;";
              if (subModello.indexOf(fineChr,k) == endChar) {
                endChar = endChar+fineChr.length();
              } 
              bloccoHtml = subModello.substring(j, endChar);
              iem = new BloccoMultirecord(sRequest, area, codiceModello, bloccoHtml, true, nonCaricareBlocco, dbOpEsterna);
              caricaBlocchi += "    if ((pagina == "+numeroPagina+") && (document.getElementById(\"GDM_BLK_"+((BloccoMultirecord)iem).getNomeBlocco()+"_CAR\").value == \"N\")) {\n";
              caricaBlocchi += "      document.getElementById(\"GDM_BLK_"+((BloccoMultirecord)iem).getNomeBlocco()+"_CAR\").value = \"Y\"\n";
              if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
                caricaBlocchi += "      if (document.getElementById(\"submitForm\").rw.value == \"W\") {\n";
                caricaBlocchi += "        f_AjaxBlocco('GdmAjax.do','"+((BloccoMultirecord)iem).getNomeBlocco()+"',null,null,null);\n";
//                caricaBlocchi += "      } else {\n";
//                caricaBlocchi += "        faiSubmit = 'S';\n";
                caricaBlocchi += "      }\n";
              } else {
                caricaBlocchi += "      if (document.getElementById(\"submitForm\").rw.value == \"W\") {\n";
                caricaBlocchi += "        f_AjaxBlocco('ServletModulistica','"+((BloccoMultirecord)iem).getNomeBlocco()+"',null,null,null);\n";
//                caricaBlocchi += "      } else {\n";
//                caricaBlocchi += "        faiSubmit = 'S';\n";
                caricaBlocchi += "      }\n";
              }
              caricaBlocchi += "    }\n";
              elementi.add(iem);
            } catch(Exception ee) {
              iem = new Testo("< #ERRORE COSTRUZIONE BLOCCO->  " + subModello.substring(j, k + Parametri.getNameBlockEnd().length()) +ee.toString()+ " <--FINE# -->");
              elementi.add(iem);
            }
            // Mi resta da analizzare tutto quanto si trova dopo il TAGFIELDEND
            subModello = subModello.substring(endChar);
          } else {
            // ERRORE: Manca il tag di fine campo
            // Blocco la ricerca
            j = -1;
          }
        }
        if ((jvisual < jtab) && 
            (jvisual < jsrc) && 
            (jvisual < jfield) && 
            (jvisual < jblock) && 
            (jvisual < jfinevi) && 
            (jvisual < jpopup) && 
            (jvisual < jpagina)) {
          // E' tag di visualizzazione!
          j = jvisual;
          //  ---> Da 0 a j-1 è un TESTO ---> Attenzions Substring NON include J !!!
          iem = new Testo(subModello.substring(0, j));
          elementi.add(iem);
          k = subModello.indexOf(Parametri.getTagVisualEnd());
          if (k != -1) {
            int endChar;
            String sHtml = "";
            endChar = k + Parametri.getTagVisualEnd().length();
            try {
              String fineChr = "&gt;";
              if (subModello.indexOf(fineChr,k) == endChar) {
                endChar = endChar+fineChr.length();
              } 
              sHtml = subModello.substring(j, endChar);
              iem = new VisualizzaHTML(sRequest, area, codiceModello, sHtml,dbOpEsterna);
              elementi.add(iem);
            } catch(Exception ee) {
              iem = new Testo("< #ERRORE COSTRUZIONE VISUALIZZAZIONE->  " + subModello.substring(j, k + Parametri.getNameVisualEnd().length()) +ee.toString()+ " <--FINE# -->");
              elementi.add(iem);
            }
            subModello = subModello.substring(endChar);
          } else {
            // ERRORE: Manca il tag di fine visualizzazione
            // Blocco la ricerca
            j = -1;
          }

        }

        if ((jfinevi < jtab) && 
            (jfinevi < jsrc) && 
            (jfinevi < jfield) && 
            (jfinevi < jblock) && 
            (jfinevi < jvisual) && 
            (jfinevi < jpopup) && 
            (jfinevi < jpagina)) {
          // E' tag di fine visualizzazione!!!
          j = jfinevi;
          //  ---> Da 0 a j-1 è un TESTO ---> Attenzions Substring NON include J !!!
          iem = new Testo(subModello.substring(0, j));
          elementi.add(iem);
          k = subModello.indexOf(Parametri.getTagVisualFineEnd());
          if (k != -1) {
            int endChar;
            endChar = k + Parametri.getTagVisualFineEnd().length();
            try {
              String fineChr = "&gt;";
              if (subModello.indexOf(fineChr,k) == endChar) {
                endChar = endChar+fineChr.length();
              } 
              iem = new FineVisualizzaHTML();
//              iem = new Testo("</span>");
              elementi.add(iem);
            } catch(Exception ee) {
              iem = new Testo("< #ERRORE COSTRUZIONE VISUALIZZAZIONE->  " + subModello.substring(j, k + Parametri.getTagVisualFineEnd().length()) +ee.toString()+ " <--FINE# -->");
              elementi.add(iem);
            }
            subModello = subModello.substring(endChar);
          } else {
            // ERRORE: Manca il tag di fine visualizzazione
            // Blocco la ricerca
            j = -1;
          }
        }

        if ((jtab < jpagina) && 
            (jtab < jsrc) && 
            (jtab < jfield) && 
            (jtab < jblock) && 
            (jtab < jvisual) && 
            (jtab < jpopup) && 
            (jtab < jfinevi)) {
          // E' tag di inizio pagina!!!
          j = jtab;
          //  ---> Da 0 a j-1 è un TESTO ---> Attenzions Substring NON include J !!!
          iem = new Testo(subModello.substring(0, j));
          elementi.add(iem);
          k = subModello.indexOf(Parametri.getTagTabEnd());
          if (k != -1) {
            int endChar;
            endChar = k + Parametri.getTagTabEnd().length();
            try {
              String fineChr = "&gt;";
              if (subModello.indexOf(fineChr,k) == endChar) {
                endChar = endChar+fineChr.length();
              }
              numeroPagina++;
              if (!paginaAttuale.equalsIgnoreCase(""+numeroPagina)) {
                nonCaricareBlocco = true;
              }

              String sHtml = subModello.substring(j,k);
              InformazioniTab itab = new InformazioniTab(sHtml,Parametri.getNameTabBegin(),Parametri.getNameTabEnd());

              String domVis = itab.getDominio();
              if (domVis == null) {
                domVis = "";
              }
              DominioVisualizza dominioVisu = new DominioVisualizza(area, itab.getDominio(), "-", "", null, "pagina", sRequest, dbOpEsterna);
              String esito = dominioVisu.getValore("pagina");
              if (esito == null) {
                esito = "";
              }

              String themesPath = "";
              if (pdo.length() == 0) {
                themesPath = "";
              } else {
                themesPath = "../";
              }
              if (numeroPagina == 1) {
                iem = new Testo("tabpageads");
                elementi.add(iem);
              }
              if ((esito.length() != 0) || (domVis.length() == 0)) {
                String tipoClass = "";
                if (paginaAttuale.equalsIgnoreCase(""+numeroPagina) || lettura.equalsIgnoreCase("P") || lettura.equalsIgnoreCase("C")  || lettura.equalsIgnoreCase("V")) {
                  sHtml = "<div id='pagina"+numeroPagina+"' style='display: block'>\n";
                  tipoClass = "GuidaSel";
                } else {
                  sHtml = "<div id='pagina"+numeroPagina+"' style='display: none'>\n";
                  tipoClass = "Guida";
                }
                tabpage += "<span class='AFC"+tipoClass+"L' id='tabpageL"+numeroPagina+"' onclick='visualizzaPagina(\""+numeroPagina+"\")'><img src='"+themesPath+"Themes/AFC/GuidaBlank.gif' alt='spazio'/></span>";  
                tabpage += "<span class='AFC"+tipoClass+"' id='tabpage"+numeroPagina+"' onclick='visualizzaPagina(\""+numeroPagina+"\")' onkeypress= 'visualizzaPagina(\""+numeroPagina+"\")' ><img src='"+themesPath+"Themes/AFC/GuidaBlank.gif' alt='spazio'/>";
                tabpage += "<a href='#tabpage' class='AFC"+"GuidaLink' >"+itab.getLabel()+"</a>";
                tabpage += "<img src='"+themesPath+"Themes/AFC/GuidaBlank.gif' alt='spazio'/></span>";
                tabpage += "<span class='AFC"+tipoClass+"R' id='tabpageR"+numeroPagina+"' onclick='visualizzaPagina(\""+numeroPagina+"\")'><img src='"+themesPath+"Themes/AFC/GuidaBlank.gif' alt='spazio'/></span>\n";
              } else {
                tabpage += "<span class='AFC"+"GuidaL' id='tabpageL"+numeroPagina+"'></span>";  
                tabpage += "<span class='AFC"+"Guida' id='tabpage"+numeroPagina+"'></span>";  
                tabpage += "<span class='AFC"+"GuidaR' id='tabpageR"+numeroPagina+"'></span>";  
                sHtml = "<div id='pagina"+numeroPagina+"' style='display: none'>\n";
              }
              iem = new Testo(sHtml);
              elementi.add(iem);
            } catch(Exception ee) {
              iem = new Testo("< #ERRORE COSTRUZIONE INIZIO PAGINA->  " + subModello.substring(j, k + Parametri.getTagTabEnd().length()) +ee.toString()+ " <--FINE# -->");
              elementi.add(iem);
            }
            subModello = subModello.substring(endChar);
          } else {
            // ERRORE: Manca il tag di TabPage
            // Blocco la ricerca
            j = -1;
          }
        }

        if ((jpagina < jtab) && 
            (jpagina < jsrc) && 
            (jpagina < jfield) && 
            (jpagina < jblock) && 
            (jpagina < jvisual) && 
            (jpagina < jpopup) && 
            (jpagina < jfinevi)) {
          // E' tag di fine pagina!!!
          j = jpagina;
          //  ---> Da 0 a j-1 è un TESTO ---> Attenzions Substring NON include J !!!
          iem = new Testo(subModello.substring(0, j));
          elementi.add(iem);
          nonCaricareBlocco = false;
          k = subModello.indexOf(Parametri.getTagPageEnd());
          if (k != -1) {
            int endChar;
            endChar = k + Parametri.getTagPageEnd().length();
            try {
              String fineChr = "&gt;";
              if (subModello.indexOf(fineChr,k) == endChar) {
                endChar = endChar+fineChr.length();
              }
              String sHtml = "";
              sHtml = "</div>\n";
              iem = new Testo(sHtml);
              elementi.add(iem);
            } catch(Exception ee) {
              iem = new Testo("< #ERRORE COSTRUZIONE FINE PAGINA->  " + subModello.substring(j, k + Parametri.getTagPageEnd().length()) +ee.toString()+ " <--FINE# -->");
              elementi.add(iem);
            }
            subModello = subModello.substring(endChar);
          } else {
            // ERRORE: Manca il tag di fine pagina
            // Blocco la ricerca
            j = -1;
          }
        }


        if ((jpopup < jtab) && 
            (jpopup < jsrc) && 
            (jpopup < jfield) && 
            (jpopup < jblock) && 
            (jpopup < jvisual) && 
            (jpopup < jpagina) && 
            (jpopup < jfinevi)) {
          // E' tag di fine pagina!!!
          j = jpopup;
          //  ---> Da 0 a j-1 è un TESTO ---> Attenzions Substring NON include J !!!
          iem = new Testo(subModello.substring(0, j));
          elementi.add(iem);
          k = subModello.indexOf(Parametri.getTagPopEnd());
          if (k != -1) {
            int endChar;
            endChar = k + Parametri.getTagPopEnd().length();
            try {
              String fineChr = "&gt;";
              if (subModello.indexOf(fineChr,k) == endChar) {
                endChar = endChar+fineChr.length();
              }
              String sHtml = subModello.substring(j,endChar);
              InformazioniPopup infoP = new InformazioniPopup(sHtml, Parametri.getNamePopBegin(), Parametri.getNamePopEnd());
              String area_blk = infoP.getAreaBlocco();
              if (area_blk.length() == 0) {
                area_blk = area;
              }
              iem = new BloccoPopup(sRequest,area,codiceModello,"X",area_blk,infoP.getBlocco() ,infoP.getNome(),pdo,infoP.getCampi(),dbOpEsterna);
              elementi.add(iem);
            } catch(Exception ee) {
              iem = new Testo("< #ERRORE COSTRUZIONE FINE PAGINA->  " + subModello.substring(j, k + Parametri.getTagPopEnd().length()) +ee.toString()+ " <--FINE# -->");
              elementi.add(iem);
            }
            subModello = subModello.substring(endChar);
          } else {
            // ERRORE: Manca il tag di fine Popup
            // Blocco la ricerca
            j = -1;
          }
        }

      } else {
        // NON vi sono campi di input o SRC, per cui costruisco
        // l'ultimo IElementoModello che è sempre un TESTO
        iem = new Testo(subModello);
        elementi.add(iem);
        j = -1;  // Blocco la ricerca
      }
    }
    //Debug Tempo
    stampaTempo("ModelloHTMLIn::interpretaModello - Fine",area,codiceModello,codiceRichiesta,ptime);
    //Debug Tempo
    
  }

 /**
  * headerHTML: funzione senza parametri che restituisce tutto ciò che va nella parte
  * "HEAD" del mio HTML
  *
  * @return stringa rappresentante l'header del mio modello.
  * @see componiModello()
  */
  private String headerHTML(IDbOperationSQL dbOpEsterna) {
    //Debug Tempo
    long ptime = stampaTempo("ModelloHTMLIn::headerHTML - Inizio",area,codiceModello,codiceRichiesta,0);
    //Debug Tempo
    // Viene strutturato nel modo seguente:
    // 1. tutto quello che c'è prima del </HEAD
    // 2. Script di controllo --> quelli caricati nella lista della sessione, da svuotare ogni volta!!
    // 3. </HEAD>
    int             startChar;
    String          header = "";
    String          h1, h2;
    Controllo       c, c2;
    HttpSession     httpSession;
    ListaControlli  controlliSessione;
    ListaControlli  controlliScript = null;
    String          newformAction = "";
    String          contrSub = "";

    if (sRequest!=null){
      httpSession = sRequest.getSession();
      // 1a Fase
      if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) 
      {
    	  header = "";
        //header = "<script type=\"text/javascript\" src=\"controlli.js\"></script>\n";
        header = header + "<script type=\"text/javascript\" src=\"../Lov.js\"></script>";
        header = header + "<script type=\"text/javascript\" src=\"/appsjsuite/Documentale/jquery/js/jquery.js\"></script>\n";
        header = header + "<script type=\"text/javascript\" src=\"/appsjsuite/Documentale/jquery/js/jquery-ui.js\"></script>\n";
        header = header + "<script type=\"text/javascript\" src=\"/appsjsuite/Documentale/jquery/js/jquery.layout.js\"></script>\n";
        header = header + "<script type=\"text/javascript\" src=\"/appsjsuite/Documentale/jquery/js/datepicker-it.js\"></script>\n";
        header = header + "<script type=\"text/javascript\" src=\"/appsjsuite/Documentale/jquery/js/gdmAjax.js\"></script>\n";
//        header = header + "<script type=\"text/javascript\" src=\"../DatePicker.js\"></script>\n";
        header = header + "<script type=\"text/javascript\" src=\"../Editor_new/scripts/language/italian/editor_lang.js\"></script>\n";
        header = header + "<script type=\"text/javascript\" src='../Editor_new/scripts/innovaeditor.js'></script>\n";
        header = header +" <link rel=\"stylesheet\" href=\"/appsjsuite/Documentale/jquery/css/jquery-ui.css\">";
        header = header + stileModello;
      } else {
        startChar = modello.indexOf(Parametri.getTagHeadEnd());
        header = modello.substring(0, startChar);
        header = header + "\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=windows-1252\" />";
        header = header + "<script type=\"text/javascript\" src=\"/appsjsuite/Documentale/jquery/js/jquery.js\"></script>\n";
        header = header + "<script type=\"text/javascript\" src=\"/appsjsuite/Documentale/jquery/js/jquery-ui.js\"></script>\n";
        header = header + "<script type=\"text/javascript\" src=\"/appsjsuite/Documentale/jquery/js/jquery.layout.js\"></script>\n";
        header = header + "<script type=\"text/javascript\" src=\"/appsjsuite/Documentale/jquery/js/datepicker-it.js\"></script>\n";
        header = header + "<script type=\"text/javascript\" src=\"/appsjsuite/Documentale/jquery/js/gdmAjax.js\"></script>\n";
//        header = header + "\n<script type=\"text/javascript\" src=\"DatePicker.js\"></script>\n";
        //header = header + "<script type=\"text/javascript\" src=\"controlli.js\"></script>\n";
        header = header + "<script type=\"text/javascript\" src=\"Editor_new/scripts/language/italian/editor_lang.js\"></script>\n";
        header = header + "<script type=\"text/javascript\" src='Editor_new/scripts/innovaeditor.js'></script>\n";
        header = header +" <link rel=\"stylesheet\" href=\"/appsjsuite/Documentale/jquery/css/jquery-ui.css\">";
        header = header + "<link href=\"Themes/AFC/Style.css\" type=\"text/css\" rel=\"stylesheet\" />\n";
        header = header + stileModello;
      }

      
      if (!isW3c()) {
        header += "<script type=\"text/javascript\" >\n"+
        		"$(document).ready(function () { $('body').layout(); });\n"+
//        		"$(document).ready(function () { $('#container').layout({center__maskContents: true}); });\n"+
/*                  "function gdmResize() {\n"+
                  "  var altezza = 0;\n"+
                  "  var allegati = 0;\n"+
                  "  if ( document.getElementById(\"gdm_allegati\") != null) {\n"+
                  "    allegati = document.getElementById(\"gdm_allegati\").offsetHeight;\n"+
                  "  }\n"+
                  "  if ( document.getElementById(\"gdm_corpo\").offsetTop < document.getElementById(\"gdm_toolbar\").offsetTop) {\n"+
                  "    if (navigator.appName.indexOf(\"Netscape\") != -1) {\n"+
                  "      altezza = window.innerHeight - (document.getElementById(\"gdm_corpo\").offsetTop + document.getElementById(\"gdm_toolbar\").offsetHeight);\n"+
                  "    } else {\n"+
                  "      altezza = document.body.offsetHeight - (document.getElementById(\"gdm_corpo\").offsetTop + document.getElementById(\"gdm_toolbar\").offsetHeight);\n"+
                  "    }\n"+
                  "  } else {\n"+
                  "    altezza = document.body.offsetHeight - document.getElementById(\"gdm_corpo\").offsetTop;\n"+
                  "    if (navigator.appName.indexOf(\"Netscape\") != -1) {\n"+
                  "      altezza = window.innerHeight - document.getElementById(\"gdm_corpo\").offsetTop;\n"+
                  "    } else {\n"+
                  "      altezza = document.body.offsetHeight - document.getElementById(\"gdm_corpo\").offsetTop;\n"+
                  "    }\n"+
                  "  }\n"+
                  "  document.getElementById(\"gdm_corpo\").style.height = altezza - (allegati + 40);\n"+
                  "}\n\n"+
                  "if (navigator.appName.indexOf(\"Netscape\") != -1) {\n"+
                  "  window.addEventListener(\"load\", gdmResize, false)\n"+
                  "  window.addEventListener(\"resize\", gdmResize, false)\n"+
                  "} else {\n"+
                  "  window.attachEvent(\"onload\", gdmResize);\n"+
                  "  window.attachEvent(\"onresize\", gdmResize);\n"+
                  "}\n"+*/
                  "</script>\n";
      }
      
      
      // 2a Fase
      if (httpSession == null) {
        logger.warn("MODELLOHTMLIN - Sessione NULLA!!");
        return "";
      }
      
      try {
        controlliScript = new ListaControlli();
      } catch (Exception e) {}
      ListaControlli contolli_dati = null;
      IDbOperationSQL dbOpSQL = null;
      ResultSet rst;
      String area_dato, dato;
      String query = "SELECT dm.area_dato, dm.dato "+
                     "FROM controlli_dati d, dati_modello dm "+
                     "WHERE d.AREA = dm.area_dato "+
                     "AND d.dato = dm.dato "+
                     "AND dm.area = :AREA "+
                     "AND dm.codice_modello = :CM "+
                     "AND dm.in_uso = 'Y' "+
                     "AND d.controllo IS NOT NULL";

      try {
        dbOpSQL = dbOpEsterna;
        dbOpSQL.setStatement(query);
        dbOpSQL.setParameter(":AREA", area);
        dbOpSQL.setParameter(":CM", codiceModello);
        dbOpSQL.execute();
        rst = dbOpSQL.getRstSet();
        List<DatoArea> listaAreaDato = new ArrayList<DatoArea>();
        while (rst.next()) {
          listaAreaDato.add(new DatoArea(rst.getString(1),rst.getString(2)));
        }

        for(int indexAreaDato=0;indexAreaDato<listaAreaDato.size();indexAreaDato++) {
          area_dato = listaAreaDato.get(indexAreaDato).area_dato;
          dato = listaAreaDato.get(indexAreaDato).dato;
          contolli_dati = new ListaControlli(sRequest,area_dato,dato ,"D", dbOpSQL);
          if (contolli_dati != null && contolli_dati.getNumeroControlli() > 0) {
//            header += "<script type=\"text/javascript\" >\n";
//            for (int i=0; i < contolli_dati.getNumeroControlli(); i++) {
//              c = contolli_dati.getControllo(i);
//              if (c != null) {
//                if (c.getValue() != null) {
//                  header = header + " " + c.getValue();
//                }
//              }
//            }
//            header += "</script>\n";

//            header += "<script type=\"text/javascript\" >\n";
            for (int i=0; i < contolli_dati.getNumeroControlli(); i++) {
              c = contolli_dati.getControllo(i);
              if (c != null) {
                c2 = controlliScript.getControllo(c.getArea(), c.getControllo(), "");
                if (c2 == null) {
                  c2 = new Controllo(c.getArea(),c.getControllo(),0,c.getValue(),"");
                  controlliScript.aggiungiControllo(c2);
                }
              }
            }
//            header += "</script>\n";
          }
        }

      } catch (Exception e) {

        loggerError("ModelloHTMLIn::header - Errore nel caricamento dei controlli dati", e);
      }

      try {
        controlliSessione = new ListaControlli(sRequest,area,codiceModello,"M");
      } catch (Exception e) {
        controlliSessione = null;
        loggerError("ModelloHTMLIn::header - Errore in fase di caricamento controlli",e);
      }
      if (controlliSessione != null){
      
        if (controlliSessione.getNumeroControlli() > 0) {
          try {
            for (int i=0; i < controlliSessione.getNumeroControlli(); i++) {
              c = controlliSessione.getControllo(i);
              if (c != null) {
                c2 = controlliScript.getControllo(c.getArea(), c.getControllo(), "");
                if (c2 == null) {
                  c2 = new Controllo(c.getControllo(),c.getControllo(),0,c.getValue(),"");
                  controlliScript.aggiungiControllo(c2);
                }
              }
            }
          } catch (Exception e) {
            loggerError("ModelloHTMLIn::header - Errore in fase di caricamento controlli",e);
          }
          
          header += "<script type=\"text/javascript\" >\n";
          for (int i=0; i < controlliScript.getNumeroControlli(); i++) {
            c = controlliScript.getControllo(i);
            if (c != null) {
              if (c.getValue() != null) {
                header = header + " " + c.getValue();
              }
            }
          }
  
          header +="</script>\n<script type=\"text/javascript\" >\n";
          for (int i=0; i < controlliSessione.getNumeroControlli(); i++) {
            c = controlliSessione.getControllo(i);
            if (c != null) {
              if (c.getValue() != null) {
//                header = header + " " + c.getValue();
                if (c.evento != null) {
                  if (c.evento.equalsIgnoreCase("onsubmit")) {
                    contrSub += c.controllo+"(); ";
                  }
                }
              }
            }
          }
          header+="</script>\n";
        }
      } else {
        try {
          // Se passo da qui puo' essere un errore
          logger.error("MODELLOHTMLIN - Ricreo la lista dei controlli vuota");
          controlliSessione = new ListaControlli();
          httpSession.setAttribute("listaControlli", controlliSessione);
        } catch (Exception ex) {
          loggerError("MODELLOHTMLIN - Errore nella creazione listacontrolli vuota "+ex.getMessage(),ex);
        } finally {
        }
      }
    }

    // 3a Fase
    if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) 
    {
      int i = formAction.indexOf("{")+1;
      newformAction = formAction.substring(0,i) + contrSub + formAction.substring(i+1);
      header += " " + newformAction + " ";
    } else {
      int i = formAction.indexOf("{")+1;
      newformAction = formAction.substring(0,i) + contrSub + formAction.substring(i+1);
      header += " " + newformAction + " " + Parametri.getTagHeadEnd();
    }
 
    //Debug Tempo
    stampaTempo("ModelloHTMLIn::headerHTML - Fine",area,codiceModello,codiceRichiesta,ptime);
    //Debug Tempo
    return header;
  }

  public String getValue() {
    return getValue(null);
  }

  /**
   * Restituisce il modello HTMLIN da inviare al browser.
   * Costruisce il modello in 3 fasi:
   * 1. Costruzione della parte HEADER
   * 2. Costruzione della parte BODY: concatena tutti gli IElementoModello della lista elementi
   *    del modello (concatena i rispettivi getValue)
   * 3. Inserisce opportunamente l'istruzione FormAction
   *
   * @return una stringa che è la rappresentazione HTML del modello da inviare al chiamante
   */
  public String getValue(IDbOperationSQL dbOpEsterna) {
    //Debug Tempo
    long ptime = stampaTempo("ModelloHTMLIn::getValue - Inizio",area,codiceModello,codiceRichiesta,0);
    //Debug Tempo
    String    retval = "", r1, r2, r3, r4, r5, bodyBeginTag;
//    String    myHidden = "";
    String    iemValue = "";
    int       i;

    if (elementi.size()>0) {
      // Prima Fase
      try {
        retval = headerHTML(dbOpEsterna);
      } catch(Exception e){
        loggerError("MODELLOHTMLIN - Errore sulla header:"+e.getMessage(),e);
        //Debug Tempo
        stampaTempo("ModelloHTMLIn::getValue - Fine",area,codiceModello,codiceRichiesta,ptime);
        //Debug Tempo
        return "";
      }
      
//      if (gdm_mma_hack.equalsIgnoreCase("666")) {
//        myHidden = Cryptable.cryptPasswd(Parametri.PASSWD)+"<P>"+
//        Cryptable.cryptPasswd(Parametri.SPORTELLO_DSN) +"<P>"+Cryptable.cryptPasswd(Parametri.USER);
//        try {
//          myHidden = URLEncoder.encode(myHidden,"ISO8859-1");
//        } catch (Exception e) {}
//        myHidden = "<input type=\"hidden\" name=\"GDM_MY_HIDDEN\" value=\"" + myHidden + "\"/>\n";
//      }
      // Seconda Fase
      try {
        int livello_prot = 0, livello_sett = 0;
        boolean protetto = false;
        IElementoModello iem = null;
        CampoHTMLIn ch = null;
        int numFinePagine = 0;
        for (i=0; i<elementi.size(); i++){
          iem = (IElementoModello)elementi.get(i);
          iem.settaListFields(list_fields);
          if ((iem.getClass().getName()).equalsIgnoreCase("it.finmatica.modulistica.modulisticapack.VisualizzaHTML")) {
            livello_sett++;
            if (!protetto) {
              if (((VisualizzaHTML)iem).getProtetto()) {
                livello_prot = livello_sett;
                protetto = true;
              }
            } 
          }
          if ((iem.getClass().getName()).equalsIgnoreCase("it.finmatica.modulistica.modulisticapack.FineVisualizzaHTML")) {
            if (livello_sett == livello_prot) {
              protetto = false;
              livello_prot = 0;
            }
            livello_sett--;
          }
          iem.settaProtetto(protetto);
          elementi.set(i, iem);
          if (lettura.equalsIgnoreCase("Q") && (iem.getClass().getName()).equalsIgnoreCase("it.finmatica.modulistica.modulisticapack.CampoHTMLIn")) {
            ch = (CampoHTMLIn)iem;
            ch.setNewRequest(sRequest);
            ch.setValore(datiRicerca);
            iemValue = ch.getValue(dbOpEsterna);
          } else {
            iemValue = iem.getValue(dbOpEsterna);
          }
//          ((IElementoModello)(elementi.get(i))).settaListFields(list_fields);
//          iemValue = ((IElementoModello)(elementi.get(i))).getValue();
          if (iemValue.equalsIgnoreCase("tabpageads")) {
            iemValue = "<a name=\"#tabpage\"></a>\n"+tabpage+"\n"+scriptTabpage.replaceAll("<!--corpoCaricaBlocchiPagina-->",caricaBlocchi );
          }
          retval=retval+iemValue;
        }
      } catch(Exception ex) {
        loggerError("MODELLOHTMLIN - Errore nella getvalue caricamento elementi: "+ex.getMessage(),ex);
        //Debug Tempo
        stampaTempo("ModelloHTMLIn::getValue - Fine",area,codiceModello,codiceRichiesta,ptime);
        //Debug Tempo
        return ("Modello non caricabile - consultare i log per le informazioni");
      }
      // Terza Fase
      try {
        bodyBeginTag = leggiTag(Parametri.getTagBodyBegin(), retval);
        if (isW3c()) {
          if (pdo.equalsIgnoreCase("CC")) {
            r1 = retval.substring(0, retval.indexOf(bodyBeginTag));
            r1 += "\n<form method='post' id='submitForm' action='"+pNomeServlet+".do?"+queryHtmlIn.replaceAll("&", "&amp;")+"' >\n";
          } else {
            if (pdo.equalsIgnoreCase("HR")) {
              r1 = retval.substring(0, retval.indexOf(bodyBeginTag));
              //r1 += "\n<form method='post' name='submitForm' id='submitForm' action='"+pNomeServlet+"?"+queryHtmlIn+"' >\n";
              r1 += "\n<form method='post' id='submitForm' action='"+pNomeServlet+"' >\n";
            } else {
              r1 = retval.substring(0, retval.indexOf(bodyBeginTag)+(bodyBeginTag.length()-1)) +
                  " class='AFCPageBODY' >\n";
              r1 += "<form method='post' id='submitForm' action='"+pNomeServlet+"?"+queryHtmlIn.replaceAll("&", "&amp;")+"' >\n";
            }
          }
        } else {
          if (pdo.equalsIgnoreCase("CC")) {
            r1 = retval.substring(0, retval.indexOf(bodyBeginTag));
            r1 += "\n<form method='post' name='submitForm' id='submitForm' action='"+pNomeServlet+".do?"+queryHtmlIn+"' >\n";
          } else {
            if (pdo.equalsIgnoreCase("HR")) {
              r1 = retval.substring(0, retval.indexOf(bodyBeginTag));
              //r1 += "\n<form method='post' name='submitForm' id='submitForm' action='"+pNomeServlet+"?"+queryHtmlIn+"' >\n";
              r1 += "\n<form method='post' name='submitForm' id='submitForm' action='"+pNomeServlet+"' >\n";
            } else {
              r1 = retval.substring(0, retval.indexOf(bodyBeginTag)+(bodyBeginTag.length()-1)) +
                  " class='AFCPageBODY' >\n";
              r1 += "<form method='post' name='submitForm' id='submitForm' action='"+pNomeServlet+"?"+queryHtmlIn+"' >\n";
            }
          }
        }
//        r1 += "<div id='container' >";

        if (pos_pulsanti.equalsIgnoreCase("A")) {
          r1 += "<div class='ui-layout-north'  style='border-bottom: 3px solid #BBB;'>"+getPulsanti(dbOpEsterna)+"</div>";
        }
        r1 += "<div id='gdm_corpo' class='ui-layout-center'  style='overflow-y: auto;'>\n";
        
        if (err_msg.length() != 0) {
          r1 += "<div class='AFCErrorDataTD'>"+err_msg+"</div>\n";
        }
          r1 += "<input type=\"hidden\" name=\"area\" value=\"" + area + "\"/>\n"+ 
                "<input type=\"hidden\" id=\"cm\" name=\"cm\" value=\"" + codiceModello+"\"/>\n"+
                "<input type=\"hidden\" id=\"cr\" name=\"cr\" value=\"" + codiceRichiesta + "\"/>\n"+
                "<input type=\"hidden\" id=\"prov\" name=\"prov\" value=\"" + provenienza + "\"/>\n"+
                "<input type=\"hidden\" id=\"wfather\" name=\"wfather\" value=\"" + wPadre + "\"/>\n"+
                "<input type=\"hidden\" id=\"rw\" name=\"rw\" value=\"" + lettura + "\"/>\n"+
                "<input type=\"hidden\" id=\"visAll\" name=\"visAll\" value=\"" + visAllegati + "\"/>\n"+
                "<input type=\"hidden\" id=\"gdm_first_open\" name=\"gdm_first_open\" value=\""+timeFirstOpen+"\"/>\n"+
                "<input type=\"hidden\" id=\"_GDM_AUTO_COMMIT\" name=\"_GDM_AUTO_COMMIT\" value=\"Y\"/>\n"+
                "<input type=\"hidden\" id=\"GDM_PAGINA_ATTUALE\" name=\"GDM_PAGINA_ATTUALE\" value=\"" + paginaAttuale + "\"/>\n"+
                "<input type=\"hidden\" id=\"GDC_Link\" name=\"GDC_Link\" value=\"" + gdc_link + "\"/>\n"+
                "<input type=\"hidden\" id=\"_JWKF_id_attivita\" name=\"_JWKF_id_attivita\" value=\"" + jwf_id + "\"/>\n"+
                "<input type=\"hidden\" id=\"_JWKF_backservlet\" name=\"_JWKF_backservlet\" value=\"" + jwf_back + "\"/>\n"+
                "<input type=\"hidden\" id=\"GDM_NEW_DOC\" name=\"GDM_NEW_DOC\" value=\"" + gdm_new_doc + "\"/>\n"+
                "<input type=\"hidden\" id=\"_GDM_MSG\" name=\"_GDM_MSG\" value=\"" + chk.getErrorMessage() + "\"/>\n"+
                campi_e_obbligatori(area,codiceModello, dbOpEsterna)+ //myHidden+
                "<input type=\"hidden\" name=\"fase\" value=\"submit\"/>\n" + formHidden;
        if (lettura.equalsIgnoreCase("Q")) {
          String area_filtro = null;
          String modelli_filtro = null;
          String ordine_filtro = null;
          String master_filtro = null;
          String join_filtro = null;
          String cm_filtro = null;
          String cate_filtro = null;
          ListaDomini ld = (ListaDomini)sRequest.getSession().getAttribute("listaDomini");
          Dominio dp = null;
          if (ld != null) {
            int numDom = ld.domini.size();
            i = 0;
            while (i < numDom && area_filtro == null) { 
              dp = (Dominio)ld.domini.get(i);
              if (dp.isDominioDelModello()) {
                area_filtro = dp.getValore("AREA_FILTRO");
              }
              i++;
            }
            i = 0;
            while (i < numDom && modelli_filtro == null) { 
              dp = (Dominio)ld.domini.get(i);
              if (dp.isDominioDelModello()) {
                modelli_filtro = dp.getValore("MODELLI_FILTRO"); 
              }
              i++;
            }
            i = 0;
            while (i < numDom && ordine_filtro == null) { 
              dp = (Dominio)ld.domini.get(i);
              if (dp.isDominioDelModello()) {
                ordine_filtro = dp.getValore("ORDINE_FILTRO"); 
              }
              i++;
            }
            i = 0;
            while (i < numDom && master_filtro == null) { 
              dp = (Dominio)ld.domini.get(i);
              if (dp.isDominioDelModello()) {
                master_filtro = dp.getValore("MASTER_FILTRO"); 
              }
              i++;
            }
            i = 0;
            while (i < numDom && join_filtro == null) { 
              dp = (Dominio)ld.domini.get(i);
              if (dp.isDominioDelModello()) {
                join_filtro = dp.getValore("JOIN_FILTRO"); 
              }
              i++;
            }
            i = 0;
            while (i < numDom && cm_filtro == null) { 
              dp = (Dominio)ld.domini.get(i);
              if (dp.isDominioDelModello()) {
                cm_filtro = dp.getValore("CM_FILTRO"); 
              }
              i++;
            }
            i = 0;
            while (i < numDom && cate_filtro == null) { 
              dp = (Dominio)ld.domini.get(i);
              if (dp.isDominioDelModello()) {
                cate_filtro = dp.getValore("CATE_FILTRO"); 
              }
              i++;
            }
            if (area_filtro == null || area_filtro.length() == 0) {
              area_filtro = area;
            }
            if (modelli_filtro == null || modelli_filtro.length() == 0) {
              modelli_filtro = "";
            }
            if (ordine_filtro == null || ordine_filtro.length() == 0) {
              ordine_filtro = "";
            }
            if (master_filtro == null || master_filtro.length() == 0) {
              master_filtro = "0";
            }
            if (join_filtro == null || join_filtro.length() == 0) {
              join_filtro = "";
            }
            if (cm_filtro == null || cm_filtro.length() == 0) {
              cm_filtro = "";
            }
            if (cate_filtro == null || cate_filtro.length() == 0) {
              cate_filtro = "";
            }
          }
          r1 += "<input type=\"hidden\" id=\"_AREA_FILTRO\" name=\"_AREA_FILTRO\" value=\"" + area_filtro + "\"/>\n"+
                "<input type=\"hidden\" id=\"_MODELLI_FILTRO\" name=\"_MODELLI_FILTRO\" value=\"" + modelli_filtro + "\"/>\n"+
                "<input type=\"hidden\" id=\"_ORDINAMENTO_FILTRO\" name=\"_ORDINAMENTO_FILTRO\" value=\"" + ordine_filtro + "\"/>\n"+
                "<input type=\"hidden\" id=\"_MASTER_FILTRO\" name=\"_MASTER_FILTRO\" value=\"" + master_filtro + "\"/>\n"+
                "<input type=\"hidden\" id=\"_JOIN_FILTRO\" name=\"_JOIN_FILTRO\" value=\"" + join_filtro + "\"/>\n"+
                "<input type=\"hidden\" id=\"_CM_FILTRO\" name=\"_CM_FILTRO\" value=\"" + cm_filtro + "\"/>\n"+
                "<input type=\"hidden\" id=\"_CATE_FILTRO\" name=\"_CATE_FILTRO\" value=\"" + cate_filtro + "\"/>\n";
          
        }
        r2 = retval.substring(retval.indexOf(bodyBeginTag)+bodyBeginTag.length(), retval.indexOf(Parametri.getTagBodyEnd()))+"\n";
        r3 = formActionClose(dbOpEsterna)+"\n"+formattaControlli(dbOpEsterna);
        r4 = "\n<script type='text/javascript'>\n"+
             "function changeCursor() {\n"+
             "  document.body.style.cursor='wait';\n"+
             "}\n"+
             "function resetCursor() {\n"+
             "	document.body.style.cursor='default';\n"+	
             "}\n"+
             "if (navigator.appName.indexOf(\"Netscape\") != -1) {\n" +
             "  window.addEventListener(\"load\", ShowMessage, false)\n"+
             "  window.addEventListener(\"load\", resetCursor, false)\n" +
             "  window.addEventListener(\"onbeforeunload\", changeCursor, false)\n" +
             "} else {\n"+
             "  window.attachEvent(\"onload\", ShowMessage);\n"+
             "  window.attachEvent(\"onload\", resetCursor);\n"+
             "  window.attachEvent(\"onbeforeunload\", changeCursor);\n"+
             "}\n"+
             "</script>\n";

        if (lettura.equalsIgnoreCase("Q")) {
        	r5 = "";
        } else {
        	r5 = retval.substring(retval.indexOf(Parametri.getTagBodyEnd()));
        }
        retval = r1+r2+r3+r4+r5; //+"<div id=\"dialog\" style='overflow: hidden'></div>";
      } catch (Exception ex) {
        loggerError("MODELLOHTMLIN - Errore nella creazione form: "+ex.getMessage(),ex);
        return ("Modello non caricabile - consultare i log per le informazioni");
      }
      err_msg = "";
      list_fields = "";
    } else {
      retval="<html><head><title>ERRORE MODELLO NON TROVATO</title></head><body><p>Modello non trovato</body></html>";
    }

    //Debug Tempo
    stampaTempo("ModelloHTMLIn::getValue - Fine",area,codiceModello,codiceRichiesta,ptime);
    //Debug Tempo
    return retval;
  }

  /**
   * Restituisce quanto necessario per definire il pulsante di registrazione dei dati e
   * la modalità POST della mia form HTML
   *
   * @return stringa rappresentante l'header del mio modello.
   * @see componiModello()
   */
  private String formActionClose(IDbOperationSQL dbOpEsterna) {
    String retval = "";
    String messaggio = "";
    
    if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
      messaggio = "    if (navigator.appName.indexOf(\"Netscape\") == -1) {\n"+
                  "      window.showModelessDialog('../../"+Parametri.APPLICATIVO+"/AmvMessaggi.html','','dialogHeight: 100px; "+
                  "dialogWidth: 300px; dialogTop: 300px; dialogLeft: 400px; edge: Raised; center: Yes; help: No; "+
                  "resizable: No; status: No;');\n    }\n";
    } else {
      messaggio = "    if (navigator.appName.indexOf(\"Netscape\") == -1) {\n"+
                  "      window.showModelessDialog('AmvMessaggi.html','','dialogHeight: 100px; "+
                  "dialogWidth: 300px; dialogTop: 300px; dialogLeft: 400px; edge: Raised; center: Yes; help: No; "+
                  "resizable: No; status: No;');\n    }\n";
//      "    window.showModelessDialog('AmvMessaggi.html','','dialogHeight: 100px; "+
//                  "dialogWidth: 300px; dialogTop: 300px; dialogLeft: 400px; edge: Raised; center: Yes; help: No; "+
//                  "resizable: No; status: No;');\n";
    }


    retval += "</div>\n";
    if (pos_pulsanti.equalsIgnoreCase("B")) {
      retval += "<div class='ui-layout-south' id='gdm_basso' style='border-top: 3px solid #BBB;'>"+getPulsanti(dbOpEsterna)+"</div>";
    } else {
    	retval += "<div class='ui-layout-south' id='gdm_basso' style='border-top: 3px solid #BBB;'></div>";
    }
    retval += "<div id='dialog'></div></form>\n";

    // Script per inserimento dinamico del parametro reload=1 alla pressione del bottone Aggiorna.
    retval = retval +
    "\n<script type='text/javascript' >\n"+
    "function clickButton(theForm,nome) {\n"+
    "  if (theForm._GDM_AUTO_COMMIT.value == 'Y') {  \n"+
    "    theForm._GDM_AUTO_COMMIT.value='N'; \n"+
    messaggio+
    "    var x = document.createElement('input');\n"+
    "    x.setAttribute('type','hidden');\n"+
    "    x.setAttribute('name',nome);\n"+
    "    x.setAttribute('value','1');\n"+
    "    theForm.appendChild(x);\n"+
    "    theForm.submit();\n"+
    "  } \n"+
    "}\n"+
    "function addHiddenInputAncor(theForm,nomecampo) {\n"+
    "  if (theForm._GDM_AUTO_COMMIT.value == 'Y') {  \n"+
    "    theForm._GDM_AUTO_COMMIT.value='N'; \n"+
    messaggio+
    "    var myAct = document.getElementById(\"submitForm\").action;\n"+
    "    document.getElementById(\"submitForm\").action = myAct+'#'+nomecampo;\n"+
    "    theForm.reload.click(); \n"+
    "  } \n"+
    "}\n"+
    "function addHiddenAllegato(theForm,sUagg) {\n"+
    messaggio+
    "  var x = document.createElement('input');\n"+
    "  x.setAttribute('type','hidden');\n"+
    "  x.setAttribute('name','allegato');\n"+
    "  x.setAttribute('value',sUagg);\n"+
    "  theForm.appendChild(x);\n"+
      "  theForm.reload.click(); \n"+
    "}\n"+
    "function addHiddenInput(theForm) {\n"+
    "  if (theForm._GDM_AUTO_COMMIT.value == 'Y') {  \n"+
    "     theForm._GDM_AUTO_COMMIT.value='N'; \n"+
    messaggio+
    "     theForm.reload.click(); \n"+
    "  } \n"+
    "}\n"+
    "function apriServletUpload() {\n"+
//    "  if (typeof(upwin) == \"undefined\"||upwin.closed){\n"+
    "    var szURL;\n"+
    "    szURL=\""+urlUpload+"&ua=\"+document.getElementById(\"_SESSION_DATA_ULTIMO_AGG\").value;\n"+
//    "    upwin =  window.open(szURL,\"upwin\",\"width=600,height=500, resizable=yes\");\n"+
    "    apriServletUploadModale(szURL);\n"+
//    "  }\n"+ 
//    "  upwin.focus();\n"+
    "}\n"+
    "</script>\n";
    
    return retval;
  }


  public String getPrivValue(){
    return getPrivValue(null);
  }

 /**
  * Restituisce il modello HTMLIN da inviare al browser richiamando la getValue()
  *
  * @see getValue()
  **/
  public String getPrivValue(IDbOperationSQL dbOpEsterna){
    return getValue(dbOpEsterna);
  }


  public String getPrivPRNValue(){
    return getPrivPRNValue(null);
  }

 /**
  * Restituisce il modello HTMLIN da inviare al browser richiamando la getValue().
  * Servirà nel caso in cui il modello in input possa essere soggetto a stampe; per il 
  * momento restituisce la stessa cosa di getPrivValue.
  *
  * @see getValue()
  **/
  public String getPrivPRNValue(IDbOperationSQL dbOpEsterna){
    return getValue(dbOpEsterna);
  }  

 /**
  * Restituisce il contenuto puro delle variabili.
  *
  * @return il valore HTML della stringa
  */
  public String getDebugValue() {
    String retval;
    retval = area + " <.> " + codiceModello + " <<<<<<<<   " + modello;
    return retval;
  }

//  /**
//   * 
//   */
//  protected String formattaControlli() {
//    // Inserimento dei controlli del modello
//    String    retval ="";
//    String    contr = "";
//    String    evento = "";
//    String    controllo = "";
//    Controllo c;
//    int       npos;
//    
//    // Implemento i controlli del campo che impongo vengano attivati al momento del SUBMIT
//    if (controlliModello.getNumeroControlli() > 0) {
//      contr = retval;
//      for (int i = 0; i < controlliModello.getNumeroControlli(); i++) {
//        c = controlliModello.getControllo(i);
//        evento = c.getEvento();
//        controllo = c.getControllo();
//        npos = contr.indexOf(evento);
//        if (npos >= 0) {
//          retval = contr.substring(0,npos+(evento.length()+2));
//          retval += controllo+"(window.document.forms[0]); ";
//          retval += contr.substring(npos+(evento.length()+3));
//        } else {
//          retval += evento + "='" + controllo + "(window.document.forms[0])' ";
//        }
//        contr = retval;
//      }
//    } else {
//      retval = "";
//    }
//    return retval;
//  }

  /**
   * 
   */
  private String campi_e_obbligatori(String ar, String cm, IDbOperationSQL dbOpEsterna) {
    //Debug Tempo
    long ptime = stampaTempo("ModelloHTMLIn::campi_e_obbligatori - Inizio",area,codiceModello,codiceRichiesta,0);
    //Debug Tempo
    String          campi = "<input type=\"hidden\" id=\"gdm_campimodello\" name=\"gdm_campimodello\" value=\"";
    String          obbliga = "<input type=\"hidden\" id=\"campiobbligatori\" name=\"campiobbligatori\" value=\"";
    String          check 	= "<input type=\"hidden\" id=\"gdm_campi_check\" name=\"gdm_campi_check\" value=\"";
    String          separatore = "@";
    IDbOperationSQL  dbOp = null;
    ResultSet       resultQuery = null;

    String query = "SELECT DATO, TIPO_ACCESSO, TIPO_CAMPO " +
                   "  FROM DATI_MODELLO "+
                   " WHERE AREA = :AREA " +
                   "   AND CODICE_MODELLO = :CM " +
                   "   AND NVL(IN_USO,'Y') = 'Y'";


    try {
      dbOp = dbOpEsterna;

      dbOp.setStatement(query);
      dbOp.setParameter(":AREA", ar);
      dbOp.setParameter(":CM", cm);
      dbOp.execute();
      resultQuery = dbOp.getRstSet();
      while (resultQuery.next()) {
        if (resultQuery.getString("TIPO_ACCESSO").equalsIgnoreCase("O") || resultQuery.getString("TIPO_ACCESSO").equalsIgnoreCase("R")) {
          obbliga += separatore+resultQuery.getString("DATO");
        }
        if (resultQuery.getString("TIPO_CAMPO").equalsIgnoreCase("B")) {
        	check += separatore+resultQuery.getString("DATO");
        }
        campi += separatore+resultQuery.getString("DATO");
      }
      campi 	+= separatore;
      obbliga += separatore;
      check 	+= separatore;

    } catch (Exception sqle) {
      loggerError("ModelloHTMLIn::campiObbligatori - Errore SQL: "+sqle.toString(),sqle);
      campi = "";
    }
                    
    campi   += "\"/>\n";
    obbliga += "\"/>\n";
    check 	+= "\"/>\n";
    //Debug Tempo
    stampaTempo("ModelloHTMLIn::campi_e_obbligatori - Fine",area,codiceModello,codiceRichiesta,ptime);
    //Debug Tempo
    return campi+obbliga+check;
  }
   
  /**
   *
   */
  private void free(IDbOperationSQL dbOp) {
    try {
      dbOp.close();
    } catch (Exception e) {
    }
  }

  /**
   * 
   */
  public void settaErrMsg(String e_msg) {
  	if (e_msg == null) {
  		err_msg = "";
  	} else {
  		err_msg = e_msg;
  	}
  }

  /**
   * 
   */
  public void settaListFields(String l_fields) {
    list_fields = l_fields;
  }

  /**
   * 
   */
  protected String formattaControlli(IDbOperationSQL dbOpEsterna) {
    String    retval ="";
    String    evento = "";
    String    controllo = "";
    Controllo c;

    if (controlli == null) {
      try {
        controlli = new ListaControlli(sRequest,area,codiceModello,"M", dbOpEsterna);
      } catch (Exception e) {
        loggerError("ModelloHTMLIn::formattaControlli() - Errore nel caricamento dei controlli js",e);
      }
    }
    if (controlli.getNumeroControlli() > 0) {
      retval = "<script type='text/javascript' >\n";
      for (int i = controlli.getNumeroControlli(); i > 0 ; i--) {
        c = controlli.getControllo(i - 1);
        evento = c.getEvento();
        controllo = c.getControllo();
        if (evento == null) {
          evento = "";
        }
        if ((evento.length() != 0)) {
          if (evento.equalsIgnoreCase("onsubmit")) {
          } else {
            retval += "if (navigator.appName.indexOf(\"Netscape\") != -1) {\n" +
                 "  window.addEventListener(\""+evento.replaceAll("onload", "load")+"\", "+controllo+", false)\n" +
                 "} else {\n   window.attachEvent(\""+evento+"\", "+controllo+");\n}\n";
          }
        } 
      }
      retval += "</script> ";
    } else {
      retval = "";
    }
   
    return retval;
  }

   private String buttonInoltro(String icona, String separatore,IDbOperationSQL dbOpEsterna){
     //Debug Tempo
     long ptime = stampaTempo("ModelloHTMLIn::buttonInoltro - Inizio",area,codiceModello,codiceRichiesta,0);
     //Debug Tempo
     IDbOperationSQL dbOpSQL = null;
      ResultSet rst;
      String query = "SELECT * "+
                     "FROM OPERAZIONI_DI_INOLTRO OP "+
                     "WHERE OP.AREA = :AREA "+
                     "AND OP.CODICE_MODELLO = :CM ";

      try {
        dbOpSQL = dbOpEsterna;
        dbOpSQL.setStatement(query);
        dbOpSQL.setParameter(":AREA", area);
        dbOpSQL.setParameter(":CM", codiceModello);
        dbOpSQL.execute();
        rst = dbOpSQL.getRstSet();
        if (!rst.next()) {
          //Debug Tempo
          stampaTempo("ModelloHTMLIn::buttonInoltro - Fine",area,codiceModello,codiceRichiesta,ptime);
          //Debug Tempo
          return "";
        } 
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
                "       AND LI.CODICE_RICHIESTA = :CR)) " +
                "ORDER BY SEQUENZA ";

        dbOpSQL.setStatement(query);
        dbOpSQL.setParameter(":AREA", area);
        dbOpSQL.setParameter(":CM", codiceModello);
        dbOpSQL.setParameter(":CR", codiceRichiesta);
        dbOpSQL.execute();
        rst = dbOpSQL.getRstSet();
        if (!rst.next()) {

          //Debug Tempo
          stampaTempo("ModelloHTMLIn::buttonInoltro - Fine",area,codiceModello,codiceRichiesta,ptime);
          //Debug Tempo
          return "";
        } 

      } catch (Exception e) {
        loggerError("ModelloHTMLIn::buttonInoltro() - "+e.toString(),e);
      }
      
      String retval = "";
      String sHref = "";

      if (pRegistraInoltra.length() != 0) {
//        retval = "<input class='AFCButton' type='button' name='RegistraInoltro' "+
//                 "value='"+pRegistra+" - "+pInoltra+"' "+
//                 "onclick='changeFaseValue(document.document.getElementById(\"submitForm\")); campiObb(document.document.getElementById(\"submitForm\"));'>\n"+
//        retval = "<input class='"+siglaStile+"Button' type='submit' name='RegistraInoltro' "+
//                 "value='"+pRegistraInoltra+"'  />\n"+
//        if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR") ) {
//          sHref = pNomeServlet+".do?"+queryHtmlIn;
//        } else {
//          sHref = pNomeServlet+"?"+queryHtmlIn;
//        }
        if (pdo.equalsIgnoreCase("CC")) {
          sHref = pNomeServlet+".do?"+queryHtmlIn;
          if (pdo.equalsIgnoreCase("HR") ) {
            sHref = pNomeServlet;
          } else {
            sHref = pNomeServlet+"?"+queryHtmlIn;
          }
        }
        retval = creaPulsante("RegistraInoltro",pRegistraInoltra,icona,separatore, sHref, "", dbOpEsterna)+"&nbsp;";
      } 
     
      //Debug Tempo
      stampaTempo("ModelloHTMLIn::buttonInoltro - Fine",area,codiceModello,codiceRichiesta,ptime);
      //Debug Tempo
      return retval;
   }

  /**
   * 
   */
   private String getPulsanti(IDbOperationSQL dbOpEsterna) {
     //Debug Tempo
     long ptime = stampaTempo("ModelloHTMLIn::getPulsanti - Inizio",area,codiceModello,codiceRichiesta,0);
     //Debug Tempo
     SyncSuite sync = null;
     IDbOperationSQL  dbOp = null;
     ResultSet       rst = null;
     String retval = "";
     String registra;
     String salva;
     String precedente;
     String sblocca;
     String blocca;
     String etichetta = null, 
            label = null, 
            icona = null, 
            separatore = null,
            controllo = null,
            controllo_js = null;
     String sHref = "";
     
     if (pdo.equalsIgnoreCase("CC")) {
       sHref = pNomeServlet+".do?"+queryHtmlIn;
       if (pdo.equalsIgnoreCase("HR") ) {
         sHref = pNomeServlet;
       } else {
         sHref = pNomeServlet+"?"+queryHtmlIn;
       }
     }

     if (lettura.equalsIgnoreCase("Q")) {
       retval = 
         "<div id='gdm_toolbar' class='AFCFooterTD' style='text-align: right; overflow: visible'>\n";
       retval += creaPulsante("ExportDati","Estrazione dati","","", sHref, "",dbOpEsterna);
       retval += creaPulsante("Ricerca","","","", sHref, "",dbOpEsterna);
       if (Parametri.STAMPA_SELECT_RICERCA.equalsIgnoreCase("S")) {
      	 retval += creaPulsante("Select","","","", sHref, "",dbOpEsterna);
       }
       retval += "</div>\n<div style='display: none'>\n"+
         "   <input type='submit' name='reload' value='1' />\n"+
         "</div>\n";
       return retval;
     }

     String pulsanti = listaPulsanti(sRequest,dbOpEsterna);
     if (pulsanti == null) {
       pulsanti = "";
     }
     if (pulsanti.length() == 0) {
       if (this.beforeMod()) {
         if (pPrecedente.length() == 0) {
           precedente = "";
         } else {
           precedente = creaPulsante("Precedente",pPrecedente,"","", sHref,"",dbOpEsterna);
         }
       } else {
         precedente = "";
       }

       if (pSalva.length() == 0) {
         salva = "";
       } else {
         salva = creaPulsante("Salva",pSalva,"","", sHref, "",dbOpEsterna);
       }
       if (this.nextModel) {
         if (pSuccessivo.length() == 0) {
           registra = "";
         } else {
           registra = creaPulsante("Successivo",pSuccessivo,"","", sHref,"",dbOpEsterna);
         }
       } else {
         if (pRegistra.length() == 0) {
           registra = "";
         } else {
           registra = creaPulsante("Registra",pRegistra,"","", sHref,"",dbOpEsterna);
         }
       }
			if (chk.getLivello() > 0 && chk.getUtente().equals(utenteGDM)) {
				 sblocca = creaPulsante("Sblocca",Parametri.getParametriLabel(area,codiceModello,"SBLOCCA"),"","", sHref,"",dbOpEsterna)+"&nbsp;\n";
			 } else {
				 sblocca = "";
			 }
       try {
         if (chk.getLivello() == 0 && id_doc.length()> 0 && chk.verificaCompetenze(utenteGDM, 3, dbOpEsterna) ) {
        	 blocca = creaPulsante("Blocca",Parametri.getParametriLabel(area,codiceModello,"BLOCCA"),"","", sHref,"ShowBlocca",dbOpEsterna)+"&nbsp;\n";
         } else {
        	 blocca = "";
         }
       } catch (Exception e) {
      	 loggerError("ModelloHTMLIn::getPulsanti -"+e.toString(),e);
      	 blocca = "";
       }
       // ... ATTENZIONE: devo passare anche AREA e REVISIONE ???
       retval = 
         "<div id='gdm_toolbar' class='AFCFooterTD' style='text-align: right; overflow: visible'>\n";
       
       // Bottone di Aggiornamento
       if (pAggiorna.length() != 0) {
         retval += creaPulsante("Aggiorna",pAggiorna,"","", sHref,"",dbOpEsterna)+"&nbsp;\n";
       }

       // Bottone di Salva
       retval += salva+"&nbsp;\n";   

       // Bottone di Precedente
       retval += precedente+"&nbsp;\n";   

       // Bottone di Registrazione o Successivo
       retval += registra+"&nbsp;\n";   

       // Bottone di Check Out e Check In
       retval += sblocca + blocca;   

       // Bottone di Registrazione e Inoltro
       retval += buttonInoltro("","", dbOpEsterna)+"&nbsp;\n"+

       // Bottone di ritorno a pratiche
       pratica+
       "\n"+allega+"&nbsp;\n"+linkBarcode()+  
       "</div>\n";
     } else {
       retval += "<div id='gdm_toolbar' class='AFCFooterTD' style='text-align: right; overflow: visible'>\n";

       StringTokenizer st = new StringTokenizer(pulsanti, Parametri.SEPARAVALORI);
       String nextToken = "";
       Etichetta et = null;
       pulsanti = "";
       try {
         //dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
         sync = new SyncSuite(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
         while (st.hasMoreTokens())  {
           nextToken = st.nextToken();
           et = etichette.getEtichetta(nextToken);
           if (et != null) {
             etichetta   = et.getEtichetta();
             label       = et.getValore();
             icona       = et.getIcona();
             separatore  = et.getSeparatore();
             controllo   = et.getControllo();
             controllo_js= et.getControllo_js();
           } else {
             etichetta = null;
             controllo = null;
           }
           boolean creaPulsante;

           if (etichetta == null) {
             etichetta = "";
           }
           if (controllo == null) {
             controllo = "";
           }
           if (controllo.length() == 0) {
             creaPulsante = true;
           } else {
             //Debug Tempo
             long ptime2 = stampaTempo("ModelloHTMLIn::getPulsanti - Inzio chiamata JSync",area,codiceModello,codiceRichiesta,0);
             //Debug Tempo
             try {
               creaPulsante = sync.isVisible(area,controllo,area+"@"+codiceModello+"@"+codiceRichiesta,(String)(sRequest.getSession().getAttribute("UtenteGDM")));
             } catch (Exception se) {
               loggerError("ModelloHTMLIn::getPulsanti -"+se.toString(),se);
               creaPulsante = false;
             }
             //Debug Tempo
             stampaTempo("ModelloHTMLIn::getPulsanti - Fine chiamata JSync",area,codiceModello,codiceRichiesta,ptime2);
             //Debug Tempo
           }
           if (etichetta.length() == 0) {
             creaPulsante = false;
           }
           if (creaPulsante) {
  
             if (label == null) {
               label = etichetta;
             }
   
             etichetta = "$B$"+etichetta;
             if (etichetta.equalsIgnoreCase("$B$AGGIORNA")) {
               etichetta = "Aggiorna";
             }
             if (etichetta.equalsIgnoreCase("$B$REGISTRA")) {
               etichetta = "Registra";
             }
             if (etichetta.equalsIgnoreCase("$B$SALVA")) {
               etichetta = "Salva";
             }
             if (etichetta.equalsIgnoreCase("$B$REGISTRAINOLTRA")) {
               etichetta = "RegistraInoltro";
             }
             if (etichetta.equalsIgnoreCase("$B$SUCCESSIVO")) {
               etichetta = "Successivo";
             }
             if (etichetta.equalsIgnoreCase("$B$PRECEDENTE")) {
               etichetta = "Precedente";
             }
             if (etichetta.equalsIgnoreCase("$B$MODIFICA")) {
               etichetta = "";
             }
             if (etichetta.equalsIgnoreCase("$B$FIRMASTD")) {
               etichetta = "FirmaSTD";
             }
             if (etichetta.equalsIgnoreCase("$B$SBLOCCA")) {
            	 if (chk.getLivello() > 0 && chk.getUtente().equals(utenteGDM)) {
            		 etichetta = "Sblocca";
            	 } else {
            		 etichetta = "";
            	 }
             }
             if (etichetta.equalsIgnoreCase("$B$BLOCCA")) {
            	 if (chk.getLivello() == 0  && chk.verificaCompetenze(utenteGDM, 3)) {
            		 etichetta = "Blocca";
            		 controllo_js = "ShowBlocca";
            	 } else {
            		 etichetta = "";
            	 }
             }
             if (etichetta.equalsIgnoreCase("$B$STAMPA")) {
               etichetta = "Stampa";
             }
             if (etichetta.equalsIgnoreCase("$B$ALLEGA")) {
               if (getAllegati().equalsIgnoreCase("Y")) {
                 etichetta = "Allega";
               } else {
                 pulsanti += "";
               }
             } //else {
               pulsanti += creaPulsante(etichetta, label, icona, separatore, sHref, controllo_js, et,dbOpEsterna);
             //}
           }
         }
         sync.close();
         //free(dbOp);
       } catch (Exception e) {
         try {
           sync.close();
         } catch (Exception synce) {}
         //free(dbOp);
         loggerError("ModelloHTMLIn::getPulsanti -"+e.toString(),e);
       }
         
       retval += pulsanti+linkBarcode()+"</div>\n";
     }

     retval += "<div style='display: none'>\n"+
               "   <input type='submit' name='reload' value='1' />\n"+
               "</div>\n";
     //Debug Tempo
     stampaTempo("ModelloHTMLIn::getPulsanti - Fine",area,codiceModello,codiceRichiesta,ptime);
     //Debug Tempo
     return retval;
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

  public void setNuovoDoc(String esiste) {
    if (esiste == null) {
      esiste = "";
    } 
    id_doc = esiste;
    if (id_doc.length() == 0) {
      gdm_new_doc = "Y";
    } else {
      gdm_new_doc = "N";
    }
  }

  /**
   * Legge un tag HTML cercando nella stringa passata il carattere di chiusura tag '>'.
   */
  public static String leggiTag(String tag, String s) {
    String retval;
    int startChar, endChar;
   
    startChar = s.indexOf(tag);
    retval = s.substring(startChar);
    endChar = startChar + retval.indexOf('>') + 1;
    if ((startChar == -1) || (endChar == -1)) {
      return "";
    } else {
      retval = s.substring(startChar, endChar);
      return retval;
    }
  }

   private String linkBarcode(){
      String retval = "";
      if (Parametri.SCANNER.equalsIgnoreCase("SI")) {
        try {
          descBarcode1 = URLEncoder.encode(descBarcode1,"ISO-8859-1");
          descBarcode2 = URLEncoder.encode(descBarcode2,"ISO-8859-1");
          descBarcode3 = URLEncoder.encode(descBarcode3,"ISO-8859-1");
        } catch (Exception e){
          descBarcode1 = "";
          descBarcode2 = "";
          descBarcode3 = "";
        }
        String sBarcode = descBarcode1+descBarcode2+descBarcode3;
  
        if ((sBarcode.length() != 0) && (id_doc.length() != 0)) {
          if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
            retval = "<a class='AFCDataLink' href='../../"+Parametri.APPLICATIVO+"/ServletBarcode?"+
              "iddoc="+id_doc+"&amp;desc1="+descBarcode1+"&amp;desc2="+descBarcode2+"&amp;desc3="+descBarcode3+"'"+
              "onclick='window.open(this.href,\"\",\"toolbar=no,menubar=no, width= 230, height= 150\"); return false;' "+
              "onkeypress='window.open(this.href,\"\",\"toolbar=no,menubar=no, width= 230, height= 150\"); return false;' "+
              "title='Attenzione apre una nuova finestra' >"+
              "<img style='border: none' title='Stampa Barcode' src='../common/images/gdm/barcode.gif' alt='Barcode'/>";
          } else {
            retval = "<a class='AFCDataLink' href='ServletBarcode?"+
              "iddoc="+id_doc+"&amp;desc1="+descBarcode1+"&amp;desc2="+descBarcode2+"&amp;desc3="+descBarcode3+"' "+
              "onclick='window.open(this.href,\"\",\"toolbar=no,menubar=no, width= 230, height= 150\"); return false;' "+
              "onkeypress='window.open(this.href,\"\",\"toolbar=no,menubar=no, width= 230, height= 150\"); return false;' "+
              "title='Attenzione apre una nuova finestra' >"+
              "<img style='border: none' title='Stampa Barcode' src='images/gdm/barcode.gif' alt='Barcode'/>";
          }
          retval += "</a>&nbsp;&nbsp;";
        }
      }

      return retval;
   }

  
}

class DatoArea {
  public  String area_dato, dato;

  public DatoArea(String area_dato, String dato) {
    this.area_dato = area_dato;
    this.dato = dato;
  }
}