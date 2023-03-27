/***********************************************************************
 * Module:  ModelloHTMLOut.java
 * Author:  adelmo
 * Created: venerdì 5 aprile 2002 10.35.35
 * Purpose: Defines the Class ModelloHTMLOut
 ***********************************************************************/
package it.finmatica.modulistica.modulisticapack;

import javax.servlet.http.*;
import java.util.*;
import java.net.URLEncoder;
import java.sql.*;
import java.io.Serializable;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.jsuitesync.SyncSuite;
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.dmServer.management.*;
import it.finmatica.dmServer.Environment;
//import java.net.URLEncoder;
import org.apache.log4j.Logger;
 

public class ModelloHTMLOut extends Modello implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  protected String          completeContextURL;
  protected String          completeContextURL_images;
  private   String          err_msg = "";
  private   String          id_tipodoc = "";
  private   String          id_doc = "";
  private   String          pStampa = "";
  private   String          pInoltra = "";
  private   String          pInoltrato = "";
  private   Environment     vu = null;
  private   String          modelloPrecedente = "";
  private   String          modelloSuccessivo = "";
  private   String          tabpage = "";
  private   String          descBarcode1 = "";
  private   String          descBarcode2 = "";
  private   String          descBarcode3 = "";
  private   String          disloc = "";
  private  static Logger logger = Logger.getLogger(ModelloHTMLOut.class);
   
   /**
    * 
    */
   public ModelloHTMLOut(HttpServletRequest request, String a, String c, String pCodiceRichista) throws Exception{
      super(request, a, c, pCodiceRichista,null);

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
        // URL verso context principale e images
      completeContextURL = serverScheme+"://"+
                           serverName+":"+
                           serverPort+
                           request.getContextPath()+"/";
                         
      String sQuery = request.getQueryString();
      if (sQuery.indexOf("/common/") > 0) {
        disloc = "common";
      } else {
        disloc = "restrict";
      }
//      completeContextURL_images = completeContextURL+"images/";
      String mypdo = (String)request.getSession().getAttribute("pdo");
      if (mypdo.equalsIgnoreCase("CC") || mypdo.equalsIgnoreCase("HR")) {
        completeContextURL_images = completeContextURL+"common/images/gdm/";
      } else {
        completeContextURL_images = completeContextURL+"images/gdm/";
      }

      pStampa = Parametri.getParametriLabel(a,c,"STAMPA");
      pInoltra = Parametri.getParametriLabel(a,c,"INOLTRA");
      pInoltrato = Parametri.getParametriLabel(a,c,"INOLTRATO");
//      pSuccessivo = Parametri.getParametriLabel(a,c,"SUCCESSIVO");
//      pPrecedente = Parametri.getParametriLabel(a,c,"PRECEDENTE");

      gdmWork = request.getParameter("gdmwork");
      if (gdmWork == null) {
        gdmWork = "";
      }

      wFather = request.getParameter("wfather");
      if (wFather == null || wFather.length() == 0) {
        wFather = "";
      } else {
        wFather = "Y";
      }

      descBarcode1    = request.getParameter("BARCODE1");
      descBarcode2    = request.getParameter("BARCODE2");
      descBarcode3    = request.getParameter("BARCODE3");

      if (descBarcode1 == null) {
        descBarcode1 = "";
      }

      if (descBarcode2 == null) {
        descBarcode2 = "";
      }

      if (descBarcode3 == null) {
        descBarcode3 = "";
      }

      if (request.getServletPath().indexOf("ServletModulisticaView")> -1 || request.getServletPath().indexOf("ServletModulisticaPrint") > -1) {
    	  servletEsterna = true;
      }
   }

  /**
   *  Scrivere i dati provenienti dalla request sul db 'incrociandoli' con la struttura
   *  del modello di Input e creare contemporanemaente la struttura del modello di output
   
   *  @parameter request: richiesta proveiente da l chiamante usata per recuperare parametri
   *                      e sessione di tipo Http
   *  @parameter ModelloHTMLIn: il modello che devo converire in output 
   *  @author Adelmo
   *  @see modulisticpack.Modello
   **/
   public ModelloHTMLOut(HttpServletRequest request, ModelloHTMLIn md, IDbOperationSQL dbOpEsterna) throws Exception {
    super();

    int               i, j, num;
    Object            oIn;
    CampoHTMLOut      cOut;
    BloccoMultirecord bOut;
    String            valStr, campo, tipoCampo;
    
    // Inizializzo le liste
    elementi = new ArrayList<IElementoModello>();

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
      // URL verso context principale e images
    completeContextURL = serverScheme+"://"+
                         serverName+":"+
                         serverPort+
                         request.getContextPath()+"/";
                         
      String sQuery = request.getQueryString();
      if (sQuery.indexOf("/common/") > 0) {
        disloc = "common";
      } else {
        disloc = "restrict";
      }
//    completeContextURL_images = completeContextURL+"images/";
      String mypdo = (String)request.getSession().getAttribute("pdo");
      if (mypdo.equalsIgnoreCase("CC") || mypdo.equalsIgnoreCase("HR")) {
        completeContextURL_images = completeContextURL+"common/images/gdm/";
      } else {
        completeContextURL_images = completeContextURL+"images/gdm/";
      }


    // Riempimento di tutti i dati 'comuni' già precedentemene caricati in memoria
    // durante la costruzione del modello di Input.
    area            = md.area;
    codiceModello   = md.codiceModello;
    codiceRichiesta = md.codiceRichiesta;
    dataRevisione   = md.dataRevisione;
    valido          = md.valido;
    autore          = md.autore;
    dataInserimento = md.dataInserimento;
    dataVariazione  = md.dataVariazione;
    dataPubblicazione = md.dataPubblicazione;
    noteInterne     = md.noteInterne;
    note            = md.note;
    istruzioni      = md.istruzioni;
    dtd             = md.dtd;
    tipo            = md.tipo;
    modello         = md.modello;
//    daInoltrare     = md.daInoltrare;
    oggetto         = md.oggetto;
    classificazione = md.classificazione;
    provenienza     = md.provenienza;
    sRequest        = request;
    lettura         = md.lettura;
    pdo             = md.pdo;
    pNomeServlet    = md.pNomeServlet;
    jwf_back        = md.jwf_back;
    jwf_id          = md.jwf_id;
    listaAlle       = md.listaAlle;
    beforeModel     = md.beforeModel;
    modelloSuccessivo = md.modelloSuccessivo;
    nextModel     = md.nextModel;
    tabpage       = md.tabpage;
    paginaAttuale = md.paginaAttuale;
    pagine        = md.pagine;
    scriptTabpage = md.scriptTabpage;
    w3c               = md.w3c;
    modelloPrecedente = md.modelloPrecedente;
    // Elementi per l'identificazione di un eventuale modello successivo

    modelloSuccessivo   = md.modelloSuccessivo;
    queryHtmlIn         = md.queryHtmlIn;
    wPadre              = md.wPadre;
    tabpage             = md.tabpage;
//    siglaStile          = md.siglaStile;
    stileModello        = md.stileModello;
    gdmWork             = md.gdmWork;
    wFather             = md.wFather;
    pos_pulsanti        = md.pos_pulsanti;
    caricaBlocchi       = md.caricaBlocchi;
    etichette           = md.etichette;
    str_campi_obb				= md.str_campi_obb;
//    messaggioGDM				= md.messaggioGDM;
    chk									= md.chk;
    utenteGDM						= md.utenteGDM;
    servletEsterna		= md.servletEsterna;
    
    if (gdmWork == null) {
      gdmWork = "";
    }

    descBarcode1    = request.getParameter("BARCODE1");
    descBarcode2    = request.getParameter("BARCODE2");
    descBarcode3    = request.getParameter("BARCODE3");

    if (descBarcode1 == null) {
      descBarcode1 = "";
    }

    if (descBarcode2 == null) {
      descBarcode2 = "";
    }

    if (descBarcode3 == null) {
      descBarcode3 = "";
    }
    
 
    // Sarebbe piu' bello richiamare il super costruttore, invece di creare qui l'oggetto parametri!!
//    parametri = new Parametri();
    Parametri.settaParametriModello(tipo);    
    pStampa = Parametri.getParametriLabel(area,codiceModello,"STAMPA");
    pInoltra = Parametri.getParametriLabel(area,codiceModello,"INOLTRA");
    pInoltrato = Parametri.getParametriLabel(area,codiceModello,"INOLTRATO");
//    pSuccessivo = Parametri.getParametriLabel(area,codiceModello,"SUCCESSIVO");
//    pPrecedente = Parametri.getParametriLabel(area,codiceModello,"PRECEDENTE");
    
    // Scorro tutti i campi e recupero il loro valore dalla request ricreando
    // la lista del modello di output con i nuovi CampiHTMLOut
    num = md.getNumeroElementi();
    
    int numFinePagine = 0;
    for (i=0; i<num; i++){
      oIn = md.getElemento(i);
      if ((oIn.getClass().getName()).equals("it.finmatica.modulistica.modulisticapack.CampoHTMLIn")) {
        // E' un campo da tradurre
//        CampoHTMLIn cIn = (CampoHTMLIn)oIn;
//        cIn.caricaValore(request);
        campo = ((CampoHTMLIn)oIn).getDato();
        tipoCampo = ((CampoHTMLIn)oIn).getTipoCampo();     // new!   

        if (((CampoHTMLIn)oIn).hasDominio() == false){
          // Non è un campo legato ad un dominio
          valStr = request.getParameter(campo);
        } else if (tipoCampo.charAt(0) != 'B') {            // new!  
          // E' un campo legato ad un dominio, ma non è di tipo CHECKBOX
          valStr = request.getParameter(campo);
        } else {
          // E' un campo legato ad un CAMPO di tipo CHECKBOX
          valStr = "";
          for (j=0; j<1000; j++) {  // 1000 valori al massimo.
            String s = request.getParameter(campo+"_"+Integer.toString(j));
            if (s != null){
              // Attacco sempre un separatore in fondo, in tal modo quando dovrò
              // recuperare un valore lo richiamerò sempre utilizzando il separatore
              valStr = valStr + s + Parametri.SEPARAVALORI;
            }
          }
        }
        cOut = new CampoHTMLOut((CampoHTMLIn)oIn, valStr, request);
//        cOut = new CampoHTMLOut((CampoHTMLIn)cIn, cIn.getValore(), request);
        elementi.add(cOut);
      } else {
        if ((oIn.getClass().getName()).equals("it.finmatica.modulistica.modulisticapack.BloccoMultirecord")) 
        {
          String blocco = ((BloccoMultirecord)oIn).getBlocco();
          bOut = new BloccoMultirecord(request,area,codiceModello,blocco,false,false, dbOpEsterna);
          elementi.add(bOut);
        } else {
          if ((oIn.getClass().getName()).equals("it.finmatica.modulistica.modulisticapack.BloccoPopup")) 
          {
            // IL PULSANTE NON DEVE ESSERE VISIBILE IN QUESTO CASO
          } else 
          {
            // Non è un campo ma un altro elemento non da tradurre
            elementi.add((IElementoModello)oIn);  
          }
        }
      }
    }
   }

  /**
   *  Scrivere i dati provenienti dalla request sul db 'incrociandoli' con la struttura
   *  del modello di Input e creare contemporanemaente la struttura del modello di output
   
   *  @parameter request: richiesta proveiente da l chiamante usata per recuperare parametri
   *                      e sessione di tipo Http
   *  @parameter ModelloHTMLIn: il modello che devo converire in output 
   *  @author Marco Bonforte
   *  @see modulisticpack.Modello
   **/
   public ModelloHTMLOut(ModelloHTMLIn md ) throws Exception {
    super();
    
    int               i, num;
    Object            oIn;
    CampoHTMLOut      cOut;
//    BloccoMultirecord bOut;
    String            valStr/*, campo*/, tipoCampo;
    
    // Inizializzo le liste
    elementi = new ArrayList<IElementoModello>();

    // Riempimento di tutti i dati 'comuni' già precedentemene caricati in memoria
    // durante la costruzione del modello di Input.
    area            = md.area;
    codiceModello   = md.codiceModello;
    codiceRichiesta = md.codiceRichiesta;
    dataRevisione   = md.dataRevisione;
    valido          = md.valido;
    autore          = md.autore;
    dataInserimento = md.dataInserimento;
    dataVariazione  = md.dataVariazione;
    dataPubblicazione = md.dataPubblicazione;
    noteInterne       = md.noteInterne;
    note              = md.note;

    istruzioni        = md.istruzioni;
    dtd               = md.dtd;
    tipo              = md.tipo;
    modello           = md.modello;
    oggetto           = md.oggetto;
    classificazione   = md.classificazione;
    provenienza       = md.provenienza;
    lettura           = md.lettura;
    sRequest          = md.sRequest;
    serverName        = md.serverName;
    serverPort        = md.serverPort;
    pdo               = md.pdo;
    pNomeServlet      = md.pNomeServlet;
    jwf_back          = md.jwf_back;
    jwf_id            = md.jwf_id;
    listaAlle         = md.listaAlle;
    beforeModel       = md.beforeModel;
    modelloSuccessivo = md.modelloSuccessivo;
    nextModel         = md.nextModel;
    modelloPrecedente = md.modelloPrecedente;
    tabpage           = md.tabpage;
    paginaAttuale     = md.paginaAttuale;
    pagine            = md.pagine;
    scriptTabpage     = md.scriptTabpage;
    w3c               = md.w3c;
    // Elementi per l'identificazione di un eventuale modello successivo

    modelloSuccessivo   = md.modelloSuccessivo;
    queryHtmlIn         = md.queryHtmlIn;
    wPadre              = md.wPadre;
    tabpage             = md.tabpage;
//    siglaStile          = md.siglaStile;
    stileModello        = md.stileModello;
    gdmWork             = md.gdmWork;
    wFather             = md.wFather;
    pos_pulsanti        = md.pos_pulsanti;
    sRequest = md.sRequest;
    descBarcode1        = sRequest.getParameter("BARCODE1");
    descBarcode2        = sRequest.getParameter("BARCODE2");
    descBarcode3        = sRequest.getParameter("BARCODE3");
    caricaBlocchi       = md.caricaBlocchi;
    queryModifica       = md.queryModifica;
    etichette           = md.etichette;
    servletEsterna		= md.servletEsterna;

    chk									= md.chk;
    utenteGDM						= md.utenteGDM;
    str_campi_obb				= md.str_campi_obb;
    
    if (descBarcode1 == null) {
      descBarcode1 = "";
    }

    if (descBarcode2 == null) {
      descBarcode2 = "";
    }

    if (descBarcode3 == null) {
      descBarcode3 = "";
    }
    
    // Sarebbe piu' bello richiamare il super costruttore, invece di creare qui l'oggetto parametri!!
//    parametri = new Parametri();
    Parametri.settaParametriModello(tipo);    
    pStampa = Parametri.getParametriLabel(area,codiceModello,"STAMPA");
    pInoltra = Parametri.getParametriLabel(area,codiceModello,"INOLTRA");
    pInoltrato = Parametri.getParametriLabel(area,codiceModello,"INOLTRATO");
//    pSuccessivo = Parametri.getParametriLabel(area,codiceModello,"SUCCESSIVO");
//    pPrecedente = Parametri.getParametriLabel(area,codiceModello,"PRECEDENTE");
    
    if (Parametri.PROTOCOLLO.length() == 0) {
      serverScheme = sRequest.getScheme();
    } else {
      serverScheme = Parametri.PROTOCOLLO;
    }
    if (Parametri.SERVERNAME.length() == 0) {
      serverName = sRequest.getServerName();
    } else {
      serverName = Parametri.SERVERNAME;
    }
    if (Parametri.SERVERPORT.length() == 0) {
      serverPort = ""+sRequest.getServerPort();
    } else {
      serverPort = Parametri.SERVERPORT;
    }
      // URL verso context principale e images
    completeContextURL = serverScheme+"://"+
                         serverName+":"+
                         serverPort+
                         sRequest.getContextPath()+"/";
                         
    String sQuery = sRequest.getQueryString();
    if (sQuery == null) {
    	disloc="";
    } else {
      if (sQuery.indexOf("/common/") > 0) {
        disloc = "common";
      } else {
        disloc = "restrict";
      }
    }
    completeContextURL_images = completeContextURL+"images/";
    if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
      completeContextURL_images = completeContextURL+"common/images/gdm/";
    } else {
      completeContextURL_images = completeContextURL+"images/gdm/";
    }


    // Scorro tutti i campi e recupero il loro valore dalla request ricreando
    // la lista del modello di output con i nuovi CampiHTMLOut
    num = md.getNumeroElementi();
    int numFinePagine = 0;
    for (i=0; i<num; i++){
      oIn = md.getElemento(i);
      if ((oIn.getClass().getName()).equals("it.finmatica.modulistica.modulisticapack.CampoHTMLIn")) {
//        CampoHTMLIn cIn = (CampoHTMLIn)oIn;
//        cIn.caricaValore(sRequest);
        // E' un campo da tradurre
//        campo = ((CampoHTMLIn)oIn).getDato();
        tipoCampo = ((CampoHTMLIn)oIn).getTipoCampo();     // new!   

        if (((CampoHTMLIn)oIn).hasDominio() == false){
          // Non è un campo legato ad un dominio
          valStr = ((CampoHTMLIn)oIn).valore;
        } else if (tipoCampo.charAt(0) != 'B') {            // new!  
          // E' un campo legato ad un dominio, ma non è di tipo CHECKBOX
          valStr = ((CampoHTMLIn)oIn).valore;
        } else {
          // E' un campo legato ad un CAMPO di tipo CHECKBOX
          valStr = ((CampoHTMLIn)oIn).valore;
        }
        cOut = new CampoHTMLOut((CampoHTMLIn)oIn, valStr, sRequest);
        elementi.add(cOut);
      } else {
        // Non è un campo ma un altro elemento non da tradurre
        if ((oIn.getClass().getName()).equals("it.finmatica.modulistica.modulisticapack.BloccoPopup")) 
        {
          // IL PULSANTE NON DEVE ESSERE VISIBILE IN QUESTO CASO
        } else 
        {
          // Non è un campo ma un altro elemento non da tradurre
          elementi.add((IElementoModello)oIn);  
        }
//        elementi.add(oIn);  
      }
    }
   }

  /**
   * Restituisce il link al modello successivo.
   *
   * @return stringa che rappresenta la frase HTML per il link al modello successivo da inserire
   * @author Adelmo Gentilini
   * @author Antonio Plastini
   **/  
/*   private String linkSucc(String newMod, String modo) {
      String retval, queryUrl;
//     String sPrec = "";

      try {
        queryUrl = queryHtmlIn;

        if (queryUrl.indexOf("cr") == -1) 
          queryUrl += "&amp;cr=" + codiceRichiesta;

        queryUrl=queryUrl.replaceFirst(codiceModello, newMod);
        if (pdo.equalsIgnoreCase("CC")) {
          queryUrl = disloc+"/"+pNomeServlet+".do?"+queryUrl;
        } else {
          queryUrl = pNomeServlet+"?"+queryUrl;
        }

        retval = ""; 

        if (modo.equalsIgnoreCase("S")) {
//          retval = "<input class='AFCButton' type='button' name='Successivo' value='"+pSuccessivo+"' onclick='vaiSuccessivo(document.submitForm);'>\n"+
          retval += "<input class='AFCButton' type='submit' name='Successivo' value='"+pSuccessivo+"' />\n"+
                  "&nbsp;\n";
//                  "<script type=\"text/javascript\" >\n"+
//                  "function vaiSuccessivo(theForm) {\n"+
//                  "  theForm.action = '"+queryUrl+"';\n"+
//                  "  var x = document.createElement(\"<input type='hidden' name='prec_succ' value='S'>\");\n"+
//                  "  theForm.appendChild(x);\n"+
//                  "  var y = document.createElement(\"<input type='hidden' name='mod_precedente' value='"+codiceModello+"'>\");\n"+
//                  "  theForm.appendChild(y);\n"+
//                  "  theForm.submit();\n"+
//                  "}\n</script>\n";
        } else {
          retval = "<input class='AFCButton' type='submit' name='Precedente' value='"+pPrecedente+"' />\n"+
                  "&nbsp;\n";
//                  "<script type=\"text/javascript\" >\n"+
//                  "function vaiPrecedente(theForm) {\n"+
//                  "  theForm.action = '"+queryUrl+"';\n"+
//                  "  var x = document.createElement(\"<input type='hidden' name='prec_succ' value='P'>\");\n"+
//                  "  theForm.appendChild(x);\n"+
//                  "  theForm.submit();\n"+
//                  "}\n</script>\n";
        }
                 
      } catch(Exception ex) {
        loggerError("MODELLOHTLMOUT - Errore nella creazione del link al documento successivo. "+ex.getMessage(),ex);
        retval = "";
//        ex.printStackTrace();
      }
      
      return retval;
   }*/
   

   
  /**
   * Restituisce la stringa che effettua il link al file che contiene il pdf.
   *
   * @return stringa che rappresenta la frase HTML per il link al file pdf che
   *         apre il pdf in una nuova pagina di browser
   * @author Adelmo Gentilini
   **/
  
/*   private String linkPdf(){
      String retval;

      retval = "<a class='AFCDataLink' href=\""+urlFileTemp()+".pdf\" >"+ 
               "Pdf"+
//                 "<img src='"+ this.completeContextURL_images +"acrobat.gif'>"+ 
               "</a>"+
               "&nbsp;";

      return retval;
   }*/

  /**
   * Restituisce la stringa che effettua il link al file html su una nuova pagina (per stamparla).
   *
   * @return stringa che rappresenta la frase HTML per il link al filel'html 
   * @author Antonio Plastini
   * @author Sergio Spadaro
   **/
  
   private String linkHtml(){
      String retval = "";

/*      try {
        descBarcode1 = URLEncoder.encode(descBarcode1,"ISO-8859-1");
        descBarcode2 = URLEncoder.encode(descBarcode2,"ISO-8859-1");
        descBarcode3 = URLEncoder.encode(descBarcode3,"ISO-8859-1");
      } catch (Exception e){
        descBarcode1 = "";
        descBarcode2 = "";
        descBarcode3 = "";
      }
      String sBarcode = descBarcode1+descBarcode2+descBarcode3;

      if (!sBarcode.length() == 0) {
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
*/
      retval = linkBarcode();

      if (pStampa.length() != 0) {
        if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
          retval += "<a class='AFCDataLink' href='../common/ServletStampa.do?"+
            "area="+area+"&amp;cm="+codiceModello+"&amp;cr="+codiceRichiesta+"&amp;rw=P&amp;visAll=N&amp;gdmwork="+gdmWork+"' "+ 
            "onclick='window.open(this.href,\"\",\"\"); return false;' "+
            "onkeypress='window.open(this.href,\"\",\"\"); return false;' "+
            "title='Attenzione apre una nuova finestra' >"+
            "<img style='border: none' title='"+pStampa+"' src='../common/images/gdm/printer.gif' alt='"+pStampa+"'/>";
        } else {
          retval += "<a class='AFCDataLink' href='ServletStampa?"+
            "area="+area+"&amp;cm="+codiceModello+"&amp;cr="+codiceRichiesta+"&amp;rw=P&amp;visAll=N&amp;gdmwork="+gdmWork+"' "+ 
            "onclick='window.open(this.href,\"\",\"\"); return false;' "+
            "onkeypress='window.open(this.href,\"\",\"\"); return false;' "+
            "title='Attenzione apre una nuova finestra' >"+
            "<img style='border: none' title='"+pStampa+"' src='images/gdm/printer.gif' alt='"+pStampa+"'/>";
        }
        retval += "</a>&nbsp;";
      }

      return retval;
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
  
        if (sBarcode.length() != 0) {
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

   
  /**
   *  Restituisce la stringa che effettua il link alla chiamata al ServletInoltro con i parametri
   *  per l'inoltro e ci mette una gif
   *
   * @return stringa che rappresenta la frase HTML per il link al ServletInoltro che
   *         si occupa di inoltrare il file xml tramite una classforname della libreria che
   *         trova sulla tabella operazioni_di_inoltro
   *         (utente per il momento è fisso)
   * @author Marco
   **/
   private String linkInoltro(IDbOperationSQL dbOpEsterna){
      IDbOperationSQL dbOpSQL = null;
      ResultSet rst;
      String query = "SELECT * "+
                     "FROM OPERAZIONI_DI_INOLTRO OP "+
                     "WHERE OP.AREA = :AREA "+
                     "AND OP.CODICE_MODELLO = :CM ";

      try {
          if (dbOpEsterna==null) {
              dbOpSQL = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);
          }
          else {
              dbOpSQL = dbOpEsterna;
          }

        dbOpSQL.setStatement(query);
        dbOpSQL.setParameter(":AREA", area);
        dbOpSQL.setParameter(":CM", codiceModello);
        dbOpSQL.execute();
        rst = dbOpSQL.getRstSet();
        if (!rst.next()) {
          return "";
        } 
        /*query = "SELECT * "+
                "FROM OPERAZIONI_DI_INOLTRO OP "+
                "WHERE OP.AREA ='"+area+"' "+
                "  AND OP.CODICE_MODELLO = '"+ codiceModello +"' " +
                "  AND NOT EXISTS "+
                "   (SELECT 1 " +
                "      FROM LOG_INOLTRI LI " +
                "     WHERE LI.ID_OP = OP.ID_OP " +
                "       AND LI.AREA = '"+area+"' " +
                "       AND LI.STATO = 1 " +
                "       AND LI.CODICE_RICHIESTA = '"+codiceRichiesta+"') " +
                "ORDER BY SEQUENZA ";*/

        query = "SELECT * "+
            "FROM OPERAZIONI_DI_INOLTRO OP "+
            "WHERE OP.AREA = :AREA "+
            "  AND OP.CODICE_MODELLO = :CM " +
            "  AND NOT EXISTS "+
            "   (SELECT 1 " +
            "      FROM LOG_INOLTRI LI " +
            "     WHERE LI.ID_OP = OP.ID_OP " +
            "       AND LI.AREA = :AREA " +
            "       AND LI.STATO = 1 " +
            "       AND LI.CODICE_RICHIESTA = :CR) " +
            "ORDER BY SEQUENZA ";

        dbOpSQL.setStatement(query);
        dbOpSQL.setParameter(":AREA",area);
        dbOpSQL.setParameter(":CM",codiceModello);
        dbOpSQL.setParameter(":CR",codiceRichiesta);
        dbOpSQL.execute();
        rst = dbOpSQL.getRstSet();
        if (!rst.next()) {
          if (pInoltrato.length() != 0) {
            return pInoltrato;
          } else {
            return "";
          }
        } 

      } catch (Exception e) {
        loggerError("ModelloHTMLOut::linkInoltro() - "+e.toString(),e);
      }
      finally {
          if (dbOpEsterna==null) free(dbOpSQL);
      }
      
      String retval = "";


      if (pInoltra.length() != 0) {
        if (pdo.equalsIgnoreCase("CC")) {
          retval = "<a class='AFCDataLink' href=\""+serverScheme+"://"+
                       serverName+":"+
                       serverPort+
                       sRequest.getContextPath()+"/"+
                       disloc+"/"+pNomeServlet+".do?"+
                          "fase=inoltro&rw=R" +
                          "&amp;cr="+codiceRichiesta +
                          "&amp;area="+area + "&amp;cm="+codiceModello+
                          "\" >"+
          pInoltra+
          "</a>"+
          "&nbsp;"; 
        } else {
          retval = "<a class='AFCDataLink' href=\""+serverScheme+"://"+
                       serverName+":"+
                       serverPort+
                       sRequest.getContextPath()+"/"+
                       pNomeServlet+"?"+
                          "fase=inoltro&rw=R" +
                          "&amp;cr="+codiceRichiesta +
                          "&amp;area="+area + "&amp;cm="+codiceModello+
                          "\" >"+
          pInoltra+
          "</a>"+
          "&nbsp;"; 
        }
      }
     
      return retval;
   }

   /**
    * 
    */
/*   private String linkInoltro2(){
      String retval = 
      "<a href='"+ this.completeContextURL + "AttivaFlusso?EMAIL="+codiceRichiesta +"'>"+
      pInoltra+
      "</a>"+
      "&nbsp;";
     
      return retval;
   }*/

  /**
   * 
   */
  private String linkPratica() {
    String retval = null;
    if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
      retval = "<a class='AFCDataLink' href='" + this.completeContextURL + disloc+"/ServletPratiche.do?"+
               "cr="+codiceRichiesta+"&amp;ctp="+provenienza+"&amp;rw="+lettura+"'>"+
               "Partica</a>&nbsp;";
    } else {
      retval = "<a class='AFCDataLink' href='" + this.completeContextURL + "ServletPratiche?"+
               "cr="+codiceRichiesta+"&amp;ctp="+provenienza+"&amp;rw="+lettura+"'>"+
               "Partica</a>&nbsp;";
    }
     
    return retval;

  }
   
  /**
   * Restituisce tutto ciò che va nella parte <HEAD> ... </HEAD> del mio HTML
   *
   * @return: stringa rappresentante l'header del mio modello.
   * @see: modulisticapack.ModelloHTMLIn#componiModello()
   **/
   private String headerHTML(IDbOperationSQL dbOpEsterna) {
    // Viene strutturato nel modo seguente:
    // 1. tutto quello che c'è prima del </HEAD eliminando il titolo, quindi deve verificare la presenza 
    //    dei tag <TITLE> o <title> e </TITLE> o </title> e non considerare quanto è compreso fra loro.
    // 2. Script di controllo --> quelli caricati nella lista della sessione, da svuotare ogni volta!!
    // 3. </HEAD>
    int             startChar, endChar;
    String          header, header2 = "";
    HttpSession     httpSession;
    Controllo       c;
    ListaControlli  controlliSessione;
    String          sEditor = "";
    String          messaggio = "";

    // 1a Fase
    // tolgo il titolo
    startChar = modello.indexOf(Parametri.getTagTitleBegin());
    if (startChar == -1){
      startChar = modello.indexOf((Parametri.getTagTitleBegin()).toLowerCase());      
    }
    if (startChar != -1){
      endChar = modello.indexOf(Parametri.getTagTitleEnd());
      if (endChar == -1){
        endChar = modello.indexOf((Parametri.getTagTitleEnd()).toLowerCase());      
        if (endChar != -1){
          header = modello.substring(0, startChar)+Parametri.getTagTitleBegin()+modello.substring(endChar);
        } else {
          // Titolo non identificabile: errore non bloccante 
          header = modello;
        }
      }else{
        header = modello.substring(0, startChar)+Parametri.getTagTitleBegin()+modello.substring(endChar);
      }
    } else {
      // Titolo non presente: errore non bloccante 
      header = modello;
    }
    // prendo tutta la parte fino alla </HEAD
    startChar = header.indexOf(Parametri.getTagHeadEnd());
    header = header.substring(0, startChar);
//    header = "<html>" + header + " <link href='Themes/Style.css' type='text/css' rel='stylesheet'>  " + Parametri.getTagHeadEnd();
    header = header + "\n<meta http-equiv='Content-Type' content='text/html; charset=windows-1252' />";
    header = header + "\n<link id='AFC' href='Themes/AFC/Style.css' type='text/css' rel='stylesheet' />  ";
    if (!lettura.equals("P")) header = header +" <link rel=\"stylesheet\" href=\"/appsjsuite/Documentale/jquery/css/style.css\">\n";

    if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
        header2 = header2 +" <style type='text/css'> #container {height: 95%; margin: 0 auto; width: 100%; } </style>\n";
    	header2 = header2 + "<script type=\"text/javascript\" src=\"/appsjsuite/Documentale/jquery/js/jquery.js\"></script>\n";
    	header2 = header2 + "<script type=\"text/javascript\" src=\"/appsjsuite/Documentale/jquery/js/jquery-ui.js\"></script>\n";
    	header2 = header2 + "<script type=\"text/javascript\" src=\"/appsjsuite/Documentale/jquery/js/jquery.layout.js\"></script>\n";
    	header2 = header2 + "<script type=\"text/javascript\" src=\"/appsjsuite/Documentale/jquery/js/datepicker-it.js\"></script>\n";
    	header2 = header2 + "<script type=\"text/javascript\" src=\"/appsjsuite/Documentale/jquery/js/gdmAjax.js\"></script>\n";
    	header2 = header2 +" <link rel=\"stylesheet\" href=\"/appsjsuite/Documentale/jquery/css/jquery-ui.css\">\n";
    } else {
        header = header +" <style type='text/css'> #container {height: 95%; margin: 0 auto; width: 100%; } </style>\n";
        header = header + "<script type=\"text/javascript\" src=\"/appsjsuite/Documentale/jquery/js/jquery.js\"></script>\n";
        header = header + "<script type=\"text/javascript\" src=\"/appsjsuite/Documentale/jquery/js/jquery-ui.js\"></script>\n";
        header = header + "<script type=\"text/javascript\" src=\"/appsjsuite/Documentale/jquery/js/jquery.layout.js\"></script>\n";
        header = header + "<script type=\"text/javascript\" src=\"/appsjsuite/Documentale/jquery/js/datepicker-it.js\"></script>\n";
        header = header + "<script type=\"text/javascript\" src=\"/appsjsuite/Documentale/jquery/js/gdmAjax.js\"></script>\n";
        header = header +" <link rel=\"stylesheet\" href=\"/appsjsuite/Documentale/jquery/css/jquery-ui.css\">\n";
    }
    if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
//      messaggio = "    window.showModelessDialog('../../"+Parametri.APPLICATIVO+"/AmvMessaggi.html','','dialogHeight: 100px; "+
//                  "dialogWidth: 300px; dialogTop: 300px; dialogLeft: 400px; edge: Raised; center: Yes; help: No; "+
//                  "resizable: No; status: No;');\n";
      messaggio = "    if (navigator.appName.indexOf(\"Netscape\") == -1) {\n"+
                  "      window.showModelessDialog('../../"+Parametri.APPLICATIVO+"/AmvMessaggi.html','','dialogHeight: 100px; "+
                  "dialogWidth: 300px; dialogTop: 300px; dialogLeft: 400px; edge: Raised; center: Yes; help: No; "+
                  "resizable: No; status: No;');\n    }\n";
   } else {
//      messaggio = "    window.showModelessDialog('AmvMessaggi.html','','dialogHeight: 100px; "+
//                  "dialogWidth: 300px; dialogTop: 300px; dialogLeft: 400px; edge: Raised; center: Yes; help: No; "+
//                  "resizable: No; status: No;');\n";
      messaggio = "    if (navigator.appName.indexOf(\"Netscape\") == -1) {\n"+
                  "      window.showModelessDialog('AmvMessaggi.html','','dialogHeight: 100px; "+
                  "dialogWidth: 300px; dialogTop: 300px; dialogLeft: 400px; edge: Raised; center: Yes; help: No; "+
                  "resizable: No; status: No;');\n    }\n";
    }
    header2 = header2 +
    "\n<script type='text/javascript' >\n"+
    "function clickButton(theForm,nome) {\n"+
//    "  if (theForm._GDM_AUTO_COMMIT.value == 'Y') {  \n"+
//    "    theForm._GDM_AUTO_COMMIT.value='N'; \n"+
    messaggio+
    "    var x = document.createElement('input');\n"+
    "    x.setAttribute('type','hidden');\n"+
    "    x.setAttribute('name',nome);\n"+
    "    x.setAttribute('value','1');\n"+
    "    theForm.appendChild(x);\n"+
    "    theForm.submit();\n"+
//    "  } \n"+
    "}\n</script>\n";
    if (wPadre.length() != 0 && lettura.equalsIgnoreCase("W")) {
      header2 = header2 + "<script type='text/javascript'>\n";
      header2 = header2 + "function aggiornaPadre() {\n";
//      header2 = header2 + "window.opener.addHiddenInput(window.opener.document.getElementById('submitForm'));\n";
      if (pdo.equalsIgnoreCase("CC")) {
        header2 = header2 + "window.opener.f_AjaxBlocco('GdmAjax.do','"+wPadre+"',null,null,null);\n";
      } else {
        header2 = header2 + "window.opener.f_AjaxBlocco('ServletModulistica','"+wPadre+"',null,null,null);\n";
      }
      header2 = header2 + "}\n";
      header2 = header2 + "if (navigator.appName.indexOf('Netscape') != -1) {\n";
      header2 = header2 + "  window.addEventListener('load', aggiornaPadre, false)\n";
      header2 = header2 + "} else {\n";
      header2 = header2 + "  window.attachEvent('onload', aggiornaPadre);\n";
      header2 = header2 + "}\n";
      header2 = header2 + "</script>\n";
    }
    if (!(lettura.equalsIgnoreCase("P") || lettura.equalsIgnoreCase("V"))) {
        header2 += "<script type=\"text/javascript\" >\n";
//        header2 += "$(document).ready(function () { $('body').layout(); });\n";
		if (servletEsterna) {
			header2 += "$(document).ready(function () { $('body').layout(); });\n";
		} else {
			header2 += "$(document).ready(function () { $('#container').layout(); });\n";
		}
/*      header2 += "<script type=\"text/javascript\" >\n"+
                "function gdmResize() {\n"+
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
                "  window.addEventListener(\"load\", ShowMessage, false)\n"+
               "} else {\n"+
                "  window.attachEvent(\"onload\", gdmResize);\n"+
                "  window.attachEvent(\"onresize\", gdmResize);\n"+
                "  window.attachEvent(\"onload\", ShowMessage);\n"+
               "}\n"+*/
        		header2 += "function addHiddenInput(theForm) {\n"+
                messaggio+
                "     theForm.submit(); \n"+
//                "  } \n"+
                "}\n"+
                "</script>\n";
    }
    if (lettura.equalsIgnoreCase("P") && gdmWork.length() == 0) {
      header2 += "\n<script type='text/javascript' >\n"+
           "function stampaModulo() {\n"+
           "  window.print();\n"+           //"  window.close();\n"+
           "}\n"+
           "if (navigator.appName.indexOf(\"Netscape\") != -1) {\n" +
           "  window.addEventListener(\"load\", stampaModulo, false)\n" +
           "} else {\n"+
           "  window.attachEvent(\"onload\", stampaModulo);\n"+
           "}\n"+
           "</script>\n";
    }

    if (lettura.equalsIgnoreCase("W") && wFather.equalsIgnoreCase("Y")) {
      header2 += "\n<script type='text/javascript' >\n"+
           "function chiudiModulo() {\n"+
           "  window.close();\n"+
           "}\n"+
           "if (navigator.appName.indexOf(\"Netscape\") != -1) {\n" +
           "  window.addEventListener(\"load\", chiudiModulo, false)\n" +
           "} else {\n"+
           "  window.attachEvent(\"onload\", chiudiModulo);\n"+
           "}\n"+
           "</script>\n";
    }

    if (sRequest!=null){
      httpSession = sRequest.getSession();
      if (httpSession == null) {
        logger.warn("MODELLOHTMLOUT - Sessione NULLA!!");
        return "";
      }
      sEditor = (String)httpSession.getAttribute("ed");
      if (sEditor == null) {
        sEditor = "";
      }

      if (!sEditor.equalsIgnoreCase("Y")) {
        header2 += formattaControlli(dbOpEsterna);
        controlliSessione = (ListaControlli)httpSession.getAttribute("listaControlli");
        if (controlliSessione != null){
      
          if (controlliSessione.getNumeroControlli() > 0) {
            header2 += "<script type=\"text/javascript\" >\n";
            for (int i=0; i < controlliSessione.getNumeroControlli(); i++) {
              c = controlliSessione.getControllo(i);
              if (c != null) {
                if (c.getValue() != null) {
                  header2 = header2 + " " + c.getValue();
//                  if (c.evento != null) {
//                    if (c.evento.equalsIgnoreCase("onsubmit")) {
//                      contrSub += c.controllo+"(); ";
//                    }
//                  }
                }
              }
            }
            header2 += "</script>\n";
          }

          String evento = "", controllo = ""/*, corpo = ""*/;
          header2 += "<script type='text/javascript' >\n";
          for (int i = controlliSessione.getNumeroControlli(); i > 0 ; i--) {
            c = controlliSessione.getControllo(i - 1);
            evento = c.getEvento();
            controllo = c.getControllo();
//            corpo = c.getValue();
            if (evento == null) {
              evento = "";
            }
            if ((evento.length() != 0)) {
              if (evento.equalsIgnoreCase("onload")) {
                header2 += "if (navigator.appName.indexOf(\"Netscape\") != -1) {\n" +
                     "  window.addEventListener(\""+evento.replaceAll("onload", "load")+"\", "+controllo+", false)\n" +
                     "} else {\n   window.attachEvent(\""+evento+"\", "+controllo+");\n}\n";
              }
            } 
          }
          header2 += "</script> ";

        }
      } else {
        header2 = "";
      }
    }

    if ((pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR"))  && !sEditor.equalsIgnoreCase("Y")) {
//      header2 += "<link href='../../"+Parametri.APPLICATIVO+"/Themes/GDM/Style.css' type='text/css' rel='stylesheet' />";
      header2 += stileModello;
      return header2 + scriptTabpage+"\n";
    } else {
//      header2 += "<link href='Themes/GDM/Style.css' type='text/css' rel='stylesheet' />";
      header2 += stileModello;
      header = header + header2 + scriptTabpage + Parametri.getTagHeadEnd();
      return header+"\n";
    }
  }

    public String getValue() {
       return getValue(null);
    }
   
  /**
   *  Metodo per la creazione del modello in output
   *  
   *  @return il modello in formato HTML  
   *  @author Adelmo,Sergio
   **/
  public String getValue(IDbOperationSQL dbOpEsterna) {
    String retval;
//    int    i;
    String originalOutput = getPrivValue(dbOpEsterna);

    if (elementi.size() > 0) {
      retval = getPrivValue(dbOpEsterna);  // In retval ho tutto il documento in output originale

      if (jwf_back.length() == 0) {
        // Lascio inalterato il documento fino al carattere che precede il tag di chiusura
        // del corpo (BODY).
        retval = originalOutput.substring(0, originalOutput.indexOf(Parametri.getTagBodyEnd())); 
      
        // I link vanno allineati e posti in basso 
        retval += "</div> <div id='gdm_toolbar' class='AFCFooterTD' style='text-align: right' > ";     
        // Link al file .pdf
        retval += linkHtml();   // Un link alla pagina html per la stampa
      
        // Esiste un modello successivo? Se si, predisporre il link.
//        if ((modelloSuccessivo != null) && (modelloSuccessivo.trim().length() > 0)) {
//          retval += linkSucc();
//        }  else {
//          sRequest.getSession().setAttribute("modello_precedente",null);
//        }
      

        // Il modello può essere inoltrato? Se si, predisporre il link.
        // ATTENZIONE! Per il momento è commentato.
        /*
        if ((daInoltrare != null) && (daInoltrare.trim().length() > 0)) {
          if (Integer.parseInt(daInoltrare) > 0)
            retval += linkInoltro();
        }
        */

        if (provenienza.equals("")) {
          retval += linkInoltro(dbOpEsterna);  //
//          retval += "</td></tr></table></form>";
          retval += "</div></div></form>";
        } else {
//          retval += linkPratica()+"</td></tr></table></form>";
          retval += linkPratica()+"</div></div></form>";
        }
        // Chiusura del documento
        retval += originalOutput.substring(originalOutput.indexOf(Parametri.getTagBodyEnd()));
        retval = "<div class='AFCErrorDataTD'>"+err_msg+"</div>"+retval;
      }
    } else {
      retval="<html><head><title>ERRORE - MODELLO NON TROVATO</title></head><body><p>Attenzione! Modello non trovato.</body></html>";
    }
    
    return retval;      
  }

    public String getPrivValue() {
        return getPrivValue(null);
    }

  /** 
   * Restituisce il modello HTMLOUT da inviare al browser.
   * Costruisce il modello in 2 fasi:
   * 1. Costruzione della parte HEADER
   * 2. Costruzione della parte BODY: concatena tutti gli IElementoModello della lista elementi 
   *    del modello (concatena i rispettivi getValue)
   *
   * @return modello di output in formato HTML
   **/
   public String getPrivValue(IDbOperationSQL dbOpEsterna) {
      String    retval;
      String    iemValue = "";
      int       i;

      if (elementi.size()>0) {
        // 1a Fase
        retval=headerHTML(dbOpEsterna);
        // 2a Fase
        if (!jwf_id.equals("")) {
          retval += "<script type='text/javascript'> function newActionJWF() { document.getElementById(\"submitForm\").action = '"+jwf_back+"'; ";
          retval += "document.getElementById(\"submitForm\").submit(); } " +
             " if (navigator.appName.indexOf(\"Netscape\") != -1) { " +
             " window.addEventListener(\"load\", newActionJWF, false) " +
             " } else {	window.attachEvent(\"onload\", newActionJWF); } ";
          retval += "</script><body> </body></form></html>";
        } else {
            IElementoModello iem = null;
          for (i=0; i<elementi.size(); i++) {
             iem = ((IElementoModello)(elementi.get(i)));
            iemValue = iem.getValue();
/*            if ((iem.getClass().getName()).equals("it.finmatica.modulistica.modulisticapack.CampoHTMLOut")) {
              String nomeC = ((CampoHTMLOut)iem).getDato();
              if (nomeC.equalsIgnoreCase("$BARCODE1")) {
                descBarcode1 = iemValue;
              }
              if (nomeC.equalsIgnoreCase("$BARCODE2")) {
                descBarcode2 = iemValue;
              }
              if (nomeC.equalsIgnoreCase("$BARCODE3")) {
                descBarcode3 = iemValue;
              }
            }*/
            if (iemValue.equalsIgnoreCase("tabpageads")) {
              iemValue = "<a name=\"tabpage\"></a>\n"+tabpage+"\n"+scriptTabpage.replaceAll("<!--corpoCaricaBlocchiPagina-->",getCaricaBlocchi() );
            }
            retval=retval+iemValue;
          }
        }
      } else {
        retval="<html><head><title>ERRORE MODELLO NON TROVATO</title></head><body><p>Modello non trovato</body></html>";
      }
      try {
        retval = retval.replaceFirst(Parametri.getTagBodyBegin(),cambiaBody(dbOpEsterna).replaceAll("\\$","\\\\\\$"));
      } catch (Exception e) {
        loggerError("ModelloHTMLOut::getPrivValue - Errore: "+e.getStackTrace().toString(),e);
      }
      return retval;
   }

    public String getPrivPRNValue() {
       return getPrivPRNValue(null);
    }

  /** 
   * Restituisce il modello HTMLOUT da inviare al browser per la stampa.
   * Costruisce il modello in 2 fasi:
   * 1. Costruzione della parte HEADER
   * 2. Costruzione della parte BODY: concatena tutti gli IElementoModello della lista elementi 
   *    del modello (concatena i rispettivi getValue)
   *
   * @return modello di output in formato HTML adatto alla stampa
   **/
   public String getPrivPRNValue(IDbOperationSQL dbOpEsterna) {
      String    retval/*,sPrec,sSucc*/;
      String    formClose = "";
      String    iemValue = "";
      String    pulsanti = "";
      String    sEditor = (String)sRequest.getSession().getAttribute("ed");
      int       i;

      if (sEditor == null) {
        sEditor = "N";
      }
    
      if (elementi.size()>0) {
        // 1a Fase
        retval=headerHTML(dbOpEsterna);
        // 2a Fase
        for (i=0; i<elementi.size(); i++) {
//          iemValue = ((IElementoModello)(elementi.get(i))).getPRNValue();
            IElementoModello iem = ((IElementoModello)(elementi.get(i)));
            iemValue = iem.getValue(dbOpEsterna);
            if ((iem.getClass().getName()).equals("it.finmatica.modulistica.modulisticapack.CampoHTMLOut")) {
              String nomeC = ((CampoHTMLOut)iem).getDato();
              if (nomeC.equalsIgnoreCase("BARCODE1")) {
                descBarcode1 = ((CampoHTMLOut)iem).valore;
              }
              if (nomeC.equalsIgnoreCase("BARCODE2")) {
                descBarcode2 = ((CampoHTMLOut)iem).valore;
              }
              if (nomeC.equalsIgnoreCase("BARCODE3")) {
                descBarcode3 = ((CampoHTMLOut)iem).valore;
              }
              if (sEditor.equalsIgnoreCase("Y")) {
                if (((CampoHTMLOut)iem).getCalcolato().equalsIgnoreCase("S") && ((CampoHTMLOut)iem).getTipoAccesso().equalsIgnoreCase("L")) {
                  iemValue = "<a style='color: #000000; text-decoration: none; background-color: #FFFF00' title=\""+nomeC+"\" href=\"#campotag\" >"+iemValue+"</a>";
                }
              }

            }
          if (iemValue.equalsIgnoreCase("tabpageads")) {
            if ((lettura.equalsIgnoreCase("R") ) && sEditor.equalsIgnoreCase("N")) {
              //iemValue = "<a name=\"tabpage\"></a>\n"+tabpage+"\n"+scriptTabpage.replaceAll("<!--corpoCaricaBlocchiPagina-->",getCaricaBlocchi() );
              iemValue = tabpage;
            } else {
              iemValue = "";
            }
          }
          retval=retval+iemValue;
        }
       /* if (pos_pulsanti.equalsIgnoreCase("B")) {
          pulsanti = getPulsanti();
        }*/
        if (pos_pulsanti.equalsIgnoreCase("B")) {
        	pulsanti += "<div class='ui-layout-south' style='border-top: 3px solid #BBB;'>"+getPulsanti(dbOpEsterna)+"</div>";
          } else {
        	  pulsanti += "<div class='ui-layout-south' style='border-top: 3px solid #BBB;'></div>";
          }

      } else {
        retval="<html><head><title>ERRORE MODELLO NON TROVATO</title></head><body><p>Modello non trovato</body></html>";
      }
/*      if (beforeModel) {
        sPrec = linkSucc(modelloPrecedente,"P");
      } else {
        sPrec = "";
      }
      if (nextModel) {
        sSucc = linkSucc(modelloSuccessivo,"S");
      } else {
        sSucc = "";
      }*/
      retval = retval.replaceFirst(Parametri.getTagBodyBegin(),cambiaBody(dbOpEsterna).replaceAll("\\$","\\\\\\$"));
      if (lettura.equalsIgnoreCase("V")  || lettura.equalsIgnoreCase("P")  || sEditor.equalsIgnoreCase("Y")) {
          formClose = "</div></div></form>";
      } else {
        formClose = "<input type=\"hidden\" name=\"mod_precedente\" value=\"" + modelloPrecedente + "\" />\n"+
                    "<input type=\"hidden\" name=\"mod_seguente\" value=\"" + modelloSuccessivo + "\" />\n"+
                    "<input type=\"hidden\" name=\"myQuery\" value=\"" + queryHtmlIn.replaceAll("&","&amp;") + "\" />\n"+
                    "</div>\n"+pulsanti+"</div></form>";
//                    "</div>\n<div class='"+siglaStile+"FooterTD' style='text-align: right' >"+sPrec+"&nbsp;"+sSucc+"</div></form>";
      }
      if ((pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) && sEditor.equalsIgnoreCase("N")) {
//        retval = retval.replaceFirst(Parametri.getTagBodyEnd(),"</td></tr><tr><td class='AFCFooterTD' align=right colspan='2' >&nbsp;</td></tr></table></form>");
      } else {
        formClose += Parametri.getTagBodyEnd();
      }
      int posIn, posFin;
      posIn = retval.indexOf(Parametri.getTagBodyEnd());
      if (posIn > -1) {
        posFin = posIn + Parametri.getTagBodyEnd().length();
        retval = retval.substring(0,posIn) + formClose + retval.substring(posFin);
      }
      if ((pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) && sEditor.equalsIgnoreCase("N")) {
      	retval = retval.replaceAll("</html>", "");
      }
      retval = retval.replaceFirst("<!--corpoCaricaBlocchiPagina-->",getCaricaBlocchi() );
//      retval = retval.replaceFirst(Parametri.getTagBodyEnd(),formClose);
      return retval;
   }
   
  /**   
   * Restituisce il modello HTMLOUT da inviare al browser per la stampa.
   * Costruisce il modello in 2 fasi:
   * 1. Costruzione della parte HEADER
   * 2. Costruzione della parte BODY: concatena tutti gli IElementoModello della lista elementi 
   *    del modello (concatena i rispettivi getValue)
   *
   * @return modello di output in formato HTML adatto alla stampa
   **/
   public String getPrivPRNComValue(IDbOperationSQL dbOpEsterna) {
      String    retval;
      String    iemValue = "";
      int       i;

      if (elementi.size()>0) {
        // 1a Fase
        retval=headerHTML(dbOpEsterna);
        // 2a Fase
        for (i=0; i<elementi.size(); i++) {
          iemValue = ((IElementoModello)(elementi.get(i))).getPRNComValue(dbOpEsterna);
          if (iemValue.equalsIgnoreCase("tabpageads")) {
            iemValue = "";
          }
          retval=retval+iemValue;
        }
      } else {
        retval="<html><head><title>ERRORE MODELLO NON TROVATO</title></head><body><p>Modello non trovato</body></html>";
      }
      retval = retval.replaceFirst(Parametri.getTagBodyBegin(),cambiaBody(dbOpEsterna).replaceAll("\\$","\\\\\\$"));
      if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
//        retval = retval.replaceFirst(Parametri.getTagBodyEnd(),"</td></tr></table></form>");
        retval = retval.replaceFirst(Parametri.getTagBodyEnd(),"</div></form>");
      } else {
//        retval = retval.replaceFirst(Parametri.getTagBodyEnd(),"</td></tr></table></form>"+Parametri.getTagBodyEnd());
        retval = retval.replaceFirst(Parametri.getTagBodyEnd(),"</div></form>"+Parametri.getTagBodyEnd());
      }
      if (gdmWork.length() == 0) {
        retval += "\n<script type='text/javascript' >\n"+
             "function stampaModulo() {\n"+
             "  window.print();\n"+
             "  window.close();\n"+
             "}\n"+
             "if (navigator.appName.indexOf(\"Netscape\") != -1) {\n" +
             "  window.addEventListener(\"load\", stampaModulo, false)\n" +
             "} else {\n"+
             "  window.attachEvent(\"onload\", stampaModulo);\n"+
             "}\n"+
             "</script>\n";
      }
      
      return retval;      
   }

  /**
   * 
   */
  private String cambiaBody(IDbOperationSQL dbOpEsterna) {
    String          retval = "";
    IDbOperationSQL  dbOp = null;
    ResultSet       rs = null;
    String          dato = null;
    String          valore = null;
    String          pulsanti = "";
    String    sEditor = (String)sRequest.getSession().getAttribute("ed");

    if (sEditor == null) {
      sEditor = "N";
    }
    if (pos_pulsanti.equalsIgnoreCase("A")) {
//      pulsanti = getPulsanti();
      pulsanti = "<div class='ui-layout-north'  style='border-bottom: 3px solid #BBB;'>"+getPulsanti(dbOpEsterna)+"</div>";
    }
    
    if (jwf_back.length() == 0) {
      if ((pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) && sEditor.equalsIgnoreCase("N")) {
        retval = "<form id='submitForm'  method='post' action='' >\n";
        if (sEditor.equalsIgnoreCase("N")) {
          retval += "<div id='container'>\n"+
        		  	"<span style='display: none'>\n"+
                    "<input type=\"hidden\" name=\"area\" value=\"" + area + "\"/>\n"+ 
                    "<input type=\"hidden\" name=\"cm\" value=\"" + codiceModello+"\"/>\n"+
                    "<input type=\"hidden\" name=\"cr\" value=\"" + codiceRichiesta + "\"/>\n"+
                    "<input type=\"hidden\" name=\"rw\" value=\""+lettura+"\"/>\n"+
                    "<input type=\"hidden\" name=\"_GDM_MSG\" value=\"" + chk.getErrorMessage() + "\"/>\n"+
                    campi_e_obbligatori(area,codiceModello, dbOpEsterna)+"</span";
        } else {
          retval += "<div";
        }
      } else {
        retval = "<body  class='AFCPageBODY' >\n<form id='submitForm'  method='post' action='' >\n";
        if (sEditor.equalsIgnoreCase("N")) {
          retval += "<div id='container'>\n"+
      		  		"<span style='display: none'>\n"+
                    "<input type=\"hidden\" name=\"area\" value=\"" + area + "\"/>\n"+ 
                    "<input type=\"hidden\" name=\"cm\" value=\"" + codiceModello+"\"/>\n"+
                    "<input type=\"hidden\" name=\"cr\" value=\"" + codiceRichiesta + "\"/>\n"+
                    "<input type=\"hidden\" name=\"rw\" value=\""+lettura+"\"/>\n"+
                    "<input type=\"hidden\" name=\"_GDM_MSG\" value=\"" + chk.getErrorMessage() + "\"/>\n"+
                    campi_e_obbligatori(area,codiceModello, dbOpEsterna)+"</span";
        } else {
          retval += "<div";
        }
      }
      if (lettura.equalsIgnoreCase("V")  || lettura.equalsIgnoreCase("P") || sEditor.equalsIgnoreCase("Y")) {
        retval += "";
      } else {
        retval += ">\n"+pulsanti+"<div id='gdm_corpo' class='ui-layout-center' style='overflow-y: auto;'>\n" +
            "<input type='hidden' name='GDM_PAGINA_ATTUALE' value='" + paginaAttuale + "' /";
      }
    } else {
      retval = "<body class='AFCPageBODY' > <form id='submitForm' method='post' action='' > <div>";
      retval += " <input type='hidden' name='_JWKF_backservlet' value='" + jwf_back + "' /> ";
      retval += " <input type='hidden' name='_JWKF_id_attivita' value='" + jwf_id + "' /> ";
      retval += " <input type='hidden' name='_JWKF_data_from_gdm' value='1' /> ";

      try {
        AccediDocumento ad = new AccediDocumento(id_doc,vu);
        ad.accediFullDocumento();
        if (dbOpEsterna==null) {
            dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);
        }
        else {
            dbOp = dbOpEsterna;
        }
        String query = "SELECT NOME FROM CAMPI_DOCUMENTO WHERE id_TIPODOC = :TD";
        
        dbOp.setStatement(query);
        dbOp.setParameter(":TD",id_tipodoc);
        dbOp.execute();
        rs = dbOp.getRstSet();
        while (rs.next()) {
          dato   = rs.getString(1);
          try {
            valore = ad.leggiValoreCampo(dato);
            if (valore == null) {
              valore = "";
            }
          } catch (Exception e1) {
            valore = "";
          }
          retval += " <input type='hidden' name='" + dato + "' value='" + valore + "' /> ";
        }

      } catch (Exception e) {

        loggerError("ModelloHTMLOut::cambiaBody - Errore: "+e.toString(),e);
      }
      finally {
        if (dbOpEsterna==null) free(dbOp);
      }
//      retval += " <table class='AFCFormTABLE' cellspacing='0' cellpadding='3' width='100%'> <tr><td";
      retval += " <div class=''";
    }
    return retval;
  }

  /**
   * 
   */
  protected String formattaControlli(IDbOperationSQL dbOpEsterna) {
    String    retval ="";
    String    evento = "";
//    String    controllo = "";
    ListaControlli controlli;
    Controllo c;

    try {
      controlli = new ListaControlli(sRequest,area,codiceModello,"M", dbOpEsterna);
    } catch (Exception e) {
      loggerError("ModelloHTMLIn::formattaControlli() - Errore nel caricamento dei controlli js",e);
      return "";
    }
    if (controlli.getNumeroControlli() > 0) {
      retval = "<script type='text/javascript' >\n";
      for (int i = controlli.getNumeroControlli(); i > 0 ; i--) {
        c = controlli.getControllo(i - 1);
        evento = c.getEvento();
//        controllo = c.getControllo();
        if (evento == null) {
          evento = "";
        }
        if ((evento.length() == 0)) {
          retval += c.getValue();
        }
      }
      retval += "</script> ";
    } else {
      retval = "";
    }
    return retval;
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
  public void settaErrMsg(String e_msg) {
    err_msg = e_msg;
  }

  /**
   * 
   */
  public void settaDocumento(String id_td,String id_d, Environment evu) {
    id_tipodoc = id_td;
    id_doc = id_d;
    vu = evu;
  }

  /**
   * 
   */
  private String getPulsanti(IDbOperationSQL dbOpEsterna) {
//    IDbOperationSQL  dbOp = null;
//    ResultSet       rst = null;
    String retval = "";
    String etichetta, label, icona, separatore, controllo, controllo_js;
    String sHref = "";


    if (pdo.equalsIgnoreCase("CC")) {
      sHref = pNomeServlet+".do?"+queryHtmlIn;
      if (pdo.equalsIgnoreCase("HR") ) {
        sHref = pNomeServlet;
      } else {
        sHref = pNomeServlet+"?"+queryHtmlIn;
      }
    }

    retval += "<div id='gdm_toolbar' class='AFCFooterTD' style='text-align: right'>\n";
    String pulsanti = listaPulsanti(sRequest,dbOpEsterna);
    if (pulsanti == null) {
      pulsanti = "";
    }
    if (pulsanti.length() == 0) {
      String bc = linkBarcode();
      if (bc.length() != 0) {
        retval += bc+"</div>";
        return retval;
      }
      return "<div id='gdm_toolbar' style='display:none'>\n</div>";
    } else {

      StringTokenizer st = new StringTokenizer(pulsanti, Parametri.SEPARAVALORI);
      String nextToken = "";
      String query = "";
      pulsanti = "";
      try {
        Etichetta et;
//        dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
        while (st.hasMoreTokens())  {
          nextToken = st.nextToken();

//          query = "SELECT ETICHETTA, VALORE, ICONA, SEPARATORE, CONTROLLO, CONTROLLO_JS FROM ETICHETTE "+
//            "WHERE UPPER(ETICHETTA) = '"+nextToken.toUpperCase()+"' AND " +
//            " AREA = '"+area+"' AND CODICE_MODELLO in ('"+codiceModello+"','-') AND "+
//            " SALVATAGGIO = 'NO' "+
//          "UNION SELECT ETICHETTA, VALORE, ICONA, SEPARATORE, CONTROLLO, CONTROLLO_JS FROM ETICHETTE "+
//          "WHERE UPPER(ETICHETTA) = '"+nextToken.toUpperCase()+"' AND " +
//          " AREA = '-' AND CODICE_MODELLO = '-' AND "+
//          " UPPER(ETICHETTA) = 'MODIFICA' AND "+
//          " SALVATAGGIO = 'NO' ";
//          dbOp.setStatement(query);
//          dbOp.execute();
//          rst = dbOp.getRstSet();
//          if (rst.next()) {
          et = etichette.getEtichetta(nextToken);
          if (et != null) {
	          etichetta   = et.getEtichetta();
	          label       = et.getValore();
	          icona       = et.getIcona();
	          separatore  = et.getSeparatore();
	          controllo   = et.getControllo();
	          controllo_js= et.getControllo_js();
	          boolean creaPulsante;
	          if (controllo == null) {
	            controllo = "";
	          }
	          if (controllo_js == null) {
	            controllo_js = "";
	          }
	          if (controllo.length() == 0) {
	            creaPulsante = true;
	          } else {
	            SyncSuite sync = null;
	            try {
	              sync = new SyncSuite(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
	              creaPulsante = sync.isVisible(area,controllo,area+"@"+codiceModello+"@"+codiceRichiesta,(String)(sRequest.getSession().getAttribute("UtenteGDM")));
	              sync.close();
	            } catch (Exception se) {
	              try {
	                sync.close();
	              } catch (Exception synce) {}
	              loggerError("ModelloHTMLIn::getPulsanti -"+se.toString(),se);
	              creaPulsante = false;
	            }
	          }
	          if (creaPulsante) {
	            if (label == null) {
	              label = "";
	            }
	            if (icona == null) {
	              icona = "";
	            }
	
	            if (label == null) {
	              label = etichetta;
	            }
	
	            etichetta = "$B$"+etichetta;
	            if (etichetta.equalsIgnoreCase("$B$AGGIORNA")) {
	              etichetta = "";
	            }
	            if (etichetta.equalsIgnoreCase("$B$REGISTRA")) {
	              etichetta = "";
	            }
	            if (etichetta.equalsIgnoreCase("$B$SALVA")) {
	              etichetta = "";
	            }
	            if (etichetta.equalsIgnoreCase("$B$REGISTRAINOLTRA")) {
	              etichetta = "";
	            }
	            if (etichetta.equalsIgnoreCase("$B$SUCCESSIVO")) {
	              etichetta = "Successivo";
	            }
	            if (etichetta.equalsIgnoreCase("$B$PRECEDENTE")) {
	              etichetta = "Precedente";
	            }
	            if (etichetta.equalsIgnoreCase("$B$MODIFICA")) {
	              etichetta = "Modifica";
	            }
	            if (etichetta.equalsIgnoreCase("$B$STAMPA")) {
	              etichetta = "Stampa";
	            }
	             if (etichetta.equalsIgnoreCase("$B$BLOCCA")) {
	            	 etichetta = "";
	             }
	             if (etichetta.equalsIgnoreCase("$B$SBLOCCA")) {
	            	 etichetta = "";
	             }
	            if (etichetta.equalsIgnoreCase("$B$ALLEGA")) {
	              pulsanti += "";
	            } else {
	              pulsanti += creaPulsante(etichetta, label, icona, separatore, sHref, controllo_js, et, dbOpEsterna);
	            }
	          }
          } else {
            loggerError("ModelloHTMLIn::getPulsanti - Pulsante "+nextToken+" non trovato",null);
          }
        }
//        }
//        free(dbOp);
      } catch (Exception e) {
//        free(dbOp);
        loggerError("ModelloHTMLIn::getPulsanti -"+e.toString(),e);
      }
        
      retval += pulsanti+linkBarcode()+"</div>\n";
    }

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

  /**
   * 
   */
  private String campi_e_obbligatori(String ar, String cm, IDbOperationSQL dbOpEsterna) {
    String          campi = "<input type=\"hidden\" name=\"gdm_campimodello\" value=\"";
    String          separatore = "@";
    IDbOperationSQL  dbOp = null;
    ResultSet       resultQuery = null;

    String query = "SELECT DATO, TIPO_ACCESSO " +
                   "  FROM DATI_MODELLO "+
                   " WHERE AREA = :AREA " +
                   "   AND CODICE_MODELLO = :CM " +
                   "   AND NVL(IN_USO,'Y') = 'Y'";


    try {
      if (dbOpEsterna==null) {
         dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);
      }
      else {
         dbOp = dbOpEsterna;
      }

      dbOp.setStatement(query);
      dbOp.setParameter(":AREA", ar);
      dbOp.setParameter(":CM", cm);
      dbOp.execute();
      resultQuery = dbOp.getRstSet();
      while (resultQuery.next()) {
        campi += separatore+resultQuery.getString("DATO");
      }
      campi += separatore;
    } catch (Exception sqle) {

      loggerError("ModelloHTMLIn::campiObbligatori - Errore SQL: "+sqle.toString(),sqle);
      campi = "";
    }
    finally {
      if (dbOpEsterna==null) free(dbOp);
    }
                    
    campi   += "\"/>\n";
    return campi;
  }
}