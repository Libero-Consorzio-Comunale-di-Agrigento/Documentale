package it.finmatica.modulistica.modulisticapack;

import java.util.*;
import java.sql.*;
import java.sql.Date;
import java.io.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.allegati.CompetenzeAllegati;
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.modulistica.personalizzazioni.Personalizzazioni;
import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.check.CheckDocumento;
import it.finmatica.dmServer.competenze.Abilitazioni;
import it.finmatica.dmServer.competenze.GDM_Competenze;
import it.finmatica.dmServer.competenze.UtenteAbilitazione;

import org.apache.log4j.Logger; 
    
/**
 * Classe astratta Modello
 *
 * @author  Adelmo Gentilini
 * @author  Antonio Plastini
 * @version  1.0
 */

public abstract class Modello {
  private   boolean         isWin = (File.separator.compareTo("\\")==0); //Per sapere su che S.O. siamo
  protected String          gdm_mma_hack;
  protected String          codiceRichiesta;
  protected String          area;
  protected String          codiceModello;
  protected Date   dataRevisione;
  protected String          gdc_link;
  protected String          valido;
  protected String          autore;
  protected Date   dataInserimento;
  protected Date   dataVariazione;
  protected Date   dataPubblicazione;
  protected String          noteInterne;
  protected String          note;
  protected String          istruzioni;
  protected String          dtd;
  protected String          tipo;
  protected String			    modello;
  protected String          oggetto;
  protected String          classificazione;
  protected String          provenienza;
  protected String          wPadre;
  protected String          lettura;
  protected String          pratica;
  protected String          visAllegati;
  protected String          listaAlle;
  protected String          allega;
  protected String          jwf_id;
  protected String          jwf_back;
  protected String          formAction;
  protected String          formHidden;
  protected String          serverScheme;
  protected String          serverName;
  protected String          serverPort;
  protected String          urlUpload;
  protected String          pdo;
  protected String          pNomeServlet;
  protected String          scriptTabpage;
  protected String          tabpage;
  protected String          paginaAttuale;
  protected int             pagine;
  protected String          w3c;
  protected String          stileModello;
  protected String           caricaBlocchi = "";
  protected String          pos_pulsanti;
  protected String          descBarcode1 = "";
  protected String          descBarcode2 = "";
  protected String          descBarcode3 = "";

  protected String          sSuc_Prec;
  protected String          modelloSuccessivo;
  protected boolean         nextModel=false;    // questa variabile viene settata a true se la stringa modelloSuccessivo � non vuota
  protected String          modelloPrecedente;
  protected boolean         beforeModel=false;    // questa variabile viene settata a true se la stringa modelloPrecedente � non vuota

  protected HttpServletRequest sRequest;
  protected String             queryHtmlIn; // Se � un modello di ingresso viene memorizzata la query con cui � stato chiamato
  protected List<IElementoModello> elementi;  // Lista di IElelmentoModello
  protected LinkedList      lModPrec = null;  
  protected String          gdmWork = "";
  protected String          wFather = "";
  protected String          sUltimoAgg = "";
  protected String          timeFirstOpen = "";
  protected String          queryModifica = "";
  protected String          utenteGDM = "";
  protected String          str_campi_obb = "";
  protected CheckDocumento	chk = null;
  protected ListaEtichette  etichette;
  protected CompetenzeAllegati  compAll = null;
  protected boolean			servletEsterna = false;	
  /**
	 * @param compAll the compAll to set
	 */
	protected static Logger logger = Logger.getLogger(Modello.class);
  
  /**
   * Costruttore di modello
   * @param      request la richiesta spedita al servlet dal chiamante
   * @param      a l'area a cui appartiene il modello
   * @param      c il codice del modello
   * @param      r la revisione del modello
   * @return     costruisce un Modello
   * @exception  Exception generica generata nel caso si presenti un qualsiasi problema in fase di costruzione.
   */
  public Modello(HttpServletRequest request, String a, String c, String pCodiceRichiesta, IDbOperationSQL dbOp) throws Exception {
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
    elementi = new ArrayList<IElementoModello>();

    sRequest        = request;
    area            = a;
    codiceModello   = c;
    codiceRichiesta = pCodiceRichiesta;
    provenienza     = request.getParameter("prov");
    wPadre          = request.getParameter("wfather");
    lettura         = request.getParameter("rw");
    jwf_id          = request.getParameter("_JWKF_id_attivita");
    jwf_back        = request.getParameter("_JWKF_backservlet");
    gdc_link        = request.getParameter("GDC_Link");
    visAllegati     = request.getParameter("visAll");
    gdmWork         = request.getParameter("gdmwork");
    wFather         = request.getParameter("wfather");
    paginaAttuale   = request.getParameter("GDM_PAGINA_ATTUALE");
    pdo             = (String)request.getSession().getAttribute("pdo");
    pNomeServlet    = (String)request.getSession().getAttribute("p_nomeservlet");
//    sUltimoAgg      = (String)request.getSession().getAttribute("UA-"+area+"-"+codiceModello+"-"+codiceRichiesta);
    lModPrec        = (LinkedList)request.getSession().getAttribute("modello_precedente");

    if (request.getServletPath().indexOf("ServletModulisticaView")> -1 || request.getServletPath().indexOf("ServletModulisticaPrint") > -1) {
  	  servletEsterna = true;
    }

    gdm_mma_hack    = request.getParameter("gdm_mma_hack");
    if (gdm_mma_hack == null) {
      gdm_mma_hack = "";
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

    if (gdmWork == null) {
      gdmWork = "";
    }

    if (wFather == null || wFather.length() == 0) {
      wFather = "";
    } else {
      wFather = "Y";
    }

    if (paginaAttuale == null) {
      paginaAttuale = "1";
    }

    if (paginaAttuale.length() == 0) {
      paginaAttuale = "1";
    }

    String    sEditor = (String)sRequest.getSession().getAttribute("ed");

    if (sEditor == null) {
      sEditor = "N";
    }
    
    if (wPadre == null) {
      wPadre = "";
    }

    int i = -1;
    if (lModPrec != null) {
      i = lModPrec.size() - 1;
    } 
    if (i > -1) {
      modelloPrecedente = (String)lModPrec.get(i);
      beforeModel = true;
    } else {
      modelloPrecedente = "";
      beforeModel = false;
    }
    sSuc_Prec = request.getParameter("Successivo");
    if (sSuc_Prec != null) {
      sSuc_Prec = "S";
    } else {
      sSuc_Prec = request.getParameter("Precedente");
      if (sSuc_Prec != null) {
        sSuc_Prec = "P";
      } else {
        sSuc_Prec = "";
      }
    } 

    if (lettura == null) {
      lettura = "R";
    }

    if (sEditor.equalsIgnoreCase("Y") || 
        lettura.equalsIgnoreCase("P") || 
        lettura.equalsIgnoreCase("V") || 
        lettura.equalsIgnoreCase("C")) {
      paginaAttuale = "T";
    }

    if (provenienza == null) {
      provenienza = "";
    }
    if (gdc_link == null) {
      gdc_link = "";
    }
    if (jwf_id == null) {
      jwf_id = "";
    }
    if (jwf_back == null) {
      jwf_back = "";
    }

    if (visAllegati == null) {
      visAllegati = "Y";
    }

    utenteGDM = (String)(sRequest.getSession().getAttribute("UtenteGDM"));
    if (provenienza.equals("")) {
      pratica = "";
    } else {
      String returl = null;
      if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
        returl = serverScheme+"://"+
                   serverName+":"+
                   serverPort+
                   request.getContextPath()+"/"+
                   "restrict/ServletPratiche.do?cr="+codiceRichiesta+"&amp;ctp="+provenienza+
                   "&amp;rw="+lettura;
      } else {
        returl = serverScheme+"://"+
                   serverName+":"+
                   serverPort+
                   request.getContextPath()+"/"+
                   "ServletPratiche?cr="+codiceRichiesta+"&amp;ctp="+provenienza+
                   "&amp;rw="+lettura;
      }
                   
      pratica = "<input class='AFCButton' type='button' name='Pratica' value='Pratica' onclick=\"location.href='"+ returl +"'; \"/>";
    }

//   String sUs = (String)sRequest.getSession().getAttribute("UtenteGDM");
//   String sRu = (String)sRequest.getSession().getAttribute("RuoloGDM");
//   if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
//      urlUpload = serverScheme+"://"+
//                  serverName+":"+
//                  serverPort+
//                  request.getContextPath()+"/../"+Parametri.APPLICATIVO+"/"+
//                  "ServletUpload?cr="+pCodiceRichiesta+"&amp;area="+area+"&amp;cm="+codiceModello+
//                  "&us="+sUs+"&ruolo="+sRu+"&ua="+getUltimoAgg();
//    } else {
//      urlUpload = serverScheme+"://"+
//                  serverName+":"+
//                  serverPort+
//                  request.getContextPath()+"/"+
//                  "ServletUpload?cr="+pCodiceRichiesta+"&amp;area="+area+"&amp;cm="+codiceModello+
//                  "&us="+sUs+"&ruolo="+sRu+"&ua="+getUltimoAgg();
//    }
    if (pdo.equalsIgnoreCase("CC")) {
      formAction = "<script type='text/javascript'>\n function newAction() { document.getElementById(\"submitForm\").action = '"+pNomeServlet+".do';\n ";
    } else {
      formAction = "<script type='text/javascript'>\n function newAction() { document.getElementById(\"submitForm\").action = '"+pNomeServlet+"';\n ";
    }
    formAction += "document.getElementById(\"submitForm\").submit();\n } </script>\n ";
    formHidden = "";
    // Creo l'oggetto parametri che viene caricato prima di interpretare il modello
//    parametri = new Parametri();
    queryModifica = serverScheme+"://"+
                    serverName+":"+
                    serverPort+
                    request.getContextPath()+request.getServletPath()+"?";
    if (lettura.equals("V")) {
    	queryModifica = "";
    } else {
    	queryModifica += sRequest.getQueryString().replaceAll("rw=R", "rw=W");
    }
    

    etichette = new ListaEtichette(request,area,codiceModello,dbOp);
    // carico dal db i dati del modello
    caricaModelloDaDb(a, c, dbOp);
//    request.getSession().setAttribute("gdmsiglastile",siglaStile);
  }

  /**
   *
   */
  public Modello() throws Exception{
      // Inizializzo le liste
    elementi = new ArrayList<IElementoModello>();
  }

  /**
   *
   * @author Adelmo Gentilini
   */
  public void setNewRequest(HttpServletRequest request) {
    sRequest        = request;
//    pdo             = (String)request.getSession().getAttribute("pdo");
    pNomeServlet    = (String)request.getSession().getAttribute("p_nomeservlet");

    if (pdo.equalsIgnoreCase("CC")) {
      formAction = "<script type='text/javascript'>\n function newAction() { document.getElementById(\"submitForm\").action = '"+pNomeServlet+".do';\n ";
    } else {
      formAction = "<script type='text/javascript'>\n function newAction() { document.getElementById(\"submitForm\").action = '"+pNomeServlet+"';\n ";
    }
    formAction += "document.getElementById(\"submitForm\").submit();\n } </script>\n ";

  }

  /**
   * Quando chiamata questa funzione 'libera' la connessione legata al modello e a tutte
   * quelle collegate ai grafici e ai campi.
   */
  public void release() {
  }


  abstract public  String getValue(IDbOperationSQL dbOpEsterna);

  abstract public  String getValue();


  abstract public String getPrivValue(IDbOperationSQL dbOpEsterna);

  abstract public String getPrivValue();


  abstract public String getPrivPRNValue(IDbOperationSQL dbOpEsterna);

  abstract public String getPrivPRNValue();

  /**
   *
   */
  public String getCodice() {
    // Per ora ritorna la concatenazione dei tre campi chiave
    return ("A:"+area+":C:"+codiceModello);
  }

  /**
  *
  */
 public String getCaricaBlocchi() {
   
   return caricaBlocchi;
 }

  /**
   *
   */
  public String getCodiceRichiesta() {
    return codiceRichiesta;
  }

  /**
   *
   */
  public String getNomeServlet() {
    String retVal= pNomeServlet;
    if (pdo.equalsIgnoreCase("HR")) {
      if (pNomeServlet != null) {
        int punto = pNomeServlet.indexOf(".do?");
        if (punto >0) {
          retVal = pNomeServlet.substring(0,punto);
        }
        
      }
    }
    return retVal;
  }
  
  public String getNoteInterne() {
	return noteInterne;
}


  /**
   *
   */
  public String getArea() {
    return area;
  }

  /**
   *
   */
  public String getCodiceModello() {
    return codiceModello;
  }

  /**
   *
   */
  public void caricaModelloDaDb(String a, String c, IDbOperationSQL dbOpEsterna) throws Exception {
    Date d;
    Time t;
    long data;
   //Debug Tempo
    long ptime = stampaTempo("Modello::caricaModelloDaDb - Inizio",a,c,"",0);
    //Debug Tempo
    IDbOperationSQL dbOp = null;
    ResultSet resultQuery = null;

    String query = "SELECT * "+
                   "FROM "+
                      "MODELLI "+
                   "WHERE AREA = :AREA AND "+
                      "CODICE_MODELLO = :CM ";

    try {
      if (dbOpEsterna==null) {
        dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);
      }
      else {
        dbOp = dbOpEsterna;
      }

      dbOp.setStatement(query);
      dbOp.setParameter(":AREA", a);
      dbOp.setParameter(":CM", c);
      dbOp.execute();
      resultQuery = dbOp.getRstSet();
    } catch (Exception sqle) {
      if (dbOpEsterna==null) free(dbOp);
      loggerError("Modello::caricaModelloDaDb() (1) - Errore SQL: "+sqle.toString(),sqle);
    }

    try {
      // Prendo tutti i valori relativi al modello dal database
      // ed inizializzo le variabili protette della classe.
      resultQuery.next();
      dataRevisione     = resultQuery.getDate("DATA_REVISIONE");
      valido            = resultQuery.getString("VALIDO");
      dataInserimento   = resultQuery.getDate("DATA_INSERIMENTO");
      dataVariazione    = resultQuery.getDate("DATA_VARIAZIONE");
      dataPubblicazione = resultQuery.getDate("DATA_PUBBLICAZIONE");
      noteInterne       = resultQuery.getString("NOTE_INTERNE");
      note              = resultQuery.getString("NOTE");
//      istruzioni        = resultQuery.getString("ISTRUZIONI");
      dtd               = resultQuery.getString("DTD");
      tipo              = resultQuery.getString("TIPO");
      oggetto           = resultQuery.getString("OGGETTO");
      classificazione   = resultQuery.getString("CLASSIFICAZIONE");
      pagine            = resultQuery.getInt("PAGINE");
      modelloSuccessivo = resultQuery.getString("MODELLO_SUCCESSIVO");
      w3c               = resultQuery.getString("WWWC");
      pos_pulsanti      = resultQuery.getString("POS_PULSANTI");
      str_campi_obb			= resultQuery.getString("STR_CAMPI_OBBLIG");
      stileModello      = resultQuery.getString("STILE");

      if (str_campi_obb == null) {
      	str_campi_obb = "";
      }
      if (stileModello == null || w3c.equalsIgnoreCase("S")) {
        stileModello = "";
      } else {
        query = "SELECT CORPO, DATA_AGGIORNAMENTO FROM STILI "+
                "WHERE AREA = :AREA AND "+
                "STILE = :STILE ";
        dbOp.setStatement(query);
        dbOp.setParameter(":AREA", area);
        dbOp.setParameter(":STILE", stileModello);
        dbOp.execute();
        resultQuery = dbOp.getRstSet();
        String myPathTemp = sRequest.getSession().getServletContext().getRealPath("")+
                            File.separator+"temp"+File.separator+area+
                            File.separator+codiceModello;
        File myFile = new File(myPathTemp);
        if (!myFile.exists()) {
          myFile.mkdirs();
        }
        if (resultQuery.next()) {
          d             = resultQuery.getDate("DATA_AGGIORNAMENTO");
          t             = resultQuery.getTime("DATA_AGGIORNAMENTO");
          if (d != null) {
            data = d.getTime() + t.getTime();
          } else {
            data = 0;
          }
          File ffile = new File(myPathTemp+File.separator+"Style.css");
          if (!ffile.exists() || ffile.lastModified() != data) {
            BufferedInputStream bisStyle = dbOp.readClob("CORPO");
//            LetturaScritturaFileFS lsf = new LetturaScritturaFileFS(myPathTemp+File.separator+"Style.css");
//            lsf.scriviFile(bisStyle);
            FileOutputStream fos = new FileOutputStream(ffile);
            byte buf[] = new byte[1];     
        
            while( bisStyle.read(buf) != -1) 
              fos.write(buf);
            fos.flush();
            fos.close();
            bisStyle.close();
            ffile.setLastModified(data);
          }
          if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
            stileModello = "<link href='../temp/"+area+"/"+codiceModello+"/Style.css' type='text/css' rel='stylesheet' />\n";;
          } else {
            stileModello = "<link href='temp/"+area+"/"+codiceModello+"/Style.css' type='text/css' rel='stylesheet' />\n";;
          }
        }
      }

      if (pos_pulsanti == null) {
        pos_pulsanti = "B";
      }

      if (modelloSuccessivo == null) {
        modelloSuccessivo = "";
      } else {
        // Modello successivo a true
        nextModel = true;
      }
      
      // Il campo ISTRUZIONE (CLOB) va letto a parte
/*      String querySelect =  "SELECT ISTRUZIONI "+
                            "FROM   MODELLI "+
                            "WHERE AREA = '"+a+"' AND "+
                            "CODICE_MODELLO = '"+c+"' ";*/

      String querySelect =  "SELECT ISTRUZIONI "+
          "FROM   MODELLI "+
          "WHERE AREA = :AREA AND "+
          "CODICE_MODELLO = :CM ";

      dbOp.setStatement(querySelect);
      dbOp.setParameter(":AREA", a);
      dbOp.setParameter(":CM", c);
      dbOp.execute();
      ResultSet rst = dbOp.getRstSet();
   
      if (!rst.next()) {
        throw new Exception("Attenzione! Errore nella ricerca del modello: il modello non � presente sul DB.");
      }
    
      BufferedInputStream bis=null;
      StringBuffer sb = null;
      int ic;
      try
      {
    	  bis  = dbOp.readClob("ISTRUZIONI");
          sb = new StringBuffer();
          
          while ((ic =  bis.read()) != -1) {
            sb.append((char)ic);
          }
          istruzioni = sb.toString();
      }
      catch (NullPointerException e)  {
    	  istruzioni = "";             
      }    
     
      
      // Il modello (CLOB) va letto a parte
      querySelect =  "SELECT MODELLO "+
                            "FROM   MODELLI "+
                            "WHERE AREA = '"+a+"' AND "+
                            "CODICE_MODELLO = '"+c+"' ";

      querySelect =  "SELECT MODELLO "+
          "FROM   MODELLI "+
          "WHERE AREA = :AREA AND "+
          "CODICE_MODELLO = :CM ";

      dbOp.setStatement(querySelect);
      dbOp.setParameter(":AREA",a);
      dbOp.setParameter(":CM",c);
      dbOp.execute();
      rst = dbOp.getRstSet();
   
      if (!rst.next()) {
        throw new Exception("Attenzione! Errore nella ricerca del modello: il modello non � presente sul DB.");
      }
    
      bis = dbOp.readClob("MODELLO");
      sb = new StringBuffer();
      while ((ic =  bis.read()) != -1) {
        sb.append((char)ic);
      }
      modello = sb.toString();

//      String salleg = "";
//      int iall= 0;
//
//      allega = "";
//      iall = modello.indexOf("<o:Allegati>");
//      if (iall > -1) {
//        salleg = modello.substring(iall+12,iall+13);
//        if (salleg.equalsIgnoreCase("Y")) {
//          String pAllega = Parametri.getParametriLabel(a,c,"ALLEGA");
//          if (pAllega.length() == 0) {
//            allega = "";
//            listaAlle = "Y";
//          } else {
//            allega = "<a href='"+urlUpload+"' class='AFCGuidaLink' "+
//                      "onclick='window.open(this.href,\"\",\"width=600,height=500, resizable=yes\"); return false;' "+
//                      "onkeypress='window.open(this.href,\"\",\"width=600,height=500, resizable=yes\"); return false;' "+
//                      "title='Attenzione apre una nuova finestra per gli allegati' >";
//            if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
//              allega += "<img style='border: none' title='"+pAllega+"' src='../common/images/gdm/clip.gif' alt='"+pAllega+"' /></a>&nbsp;\n";
//            } else {
//              allega += "<img style='border: none' title='"+pAllega+"' src='images/gdm/clip.gif' alt='"+pAllega+"' /></a>&nbsp;&nbsp;\n";
//            }
//            listaAlle = "Y";
//          }
//        } else {
//          listaAlle = "N";
//        }
//      }

      scriptTabpage = "";
      tabpage = "";
      if (pagine > 1 && (lettura.equalsIgnoreCase("W") || lettura.equalsIgnoreCase("R") || lettura.equalsIgnoreCase("V"))) {
        scriptTabpage   += "<script type='text/javascript'>\n";
        scriptTabpage   += "  function visualizzaPagina(pagina) {\n";
        scriptTabpage   += "    nascondiTutte();\n";
        scriptTabpage   += "    document.getElementById('pagina'+pagina).style.display = 'block';\n";
        scriptTabpage   += "    document.getElementById('submitForm').GDM_PAGINA_ATTUALE.value = pagina;\n";
        scriptTabpage   += "    document.getElementById('tabpageL'+pagina).className = 'AFCGuidaSelL';\n";
        scriptTabpage   += "    document.getElementById('tabpage'+pagina).className = 'AFCGuidaSel';\n";
        scriptTabpage   += "    document.getElementById('tabpageR'+pagina).className = 'AFCGuidaSelR';\n";
        scriptTabpage   += "    caricaBlocchiPagina(pagina);\n";
        scriptTabpage   += "  }\n";
        String nascoTutte= "  function nascondiTutte() {\n";
        for (int contP = 1; contP < pagine; contP++) {
          nascoTutte += "    document.getElementById('tabpageR"+contP+"').className = 'AFCGuidaR';\n";
          nascoTutte += "    document.getElementById('tabpage"+contP+"').className = 'AFCGuida';\n";
          nascoTutte += "    document.getElementById('tabpageL"+contP+"').className = 'AFCGuidaL';\n";
          nascoTutte += "    document.getElementById('pagina"+contP+"').style.display = 'none';\n";
        }
        scriptTabpage += nascoTutte+"  }\n";
        scriptTabpage += "  function caricaBlocchiPagina(pagina) {\n";
        scriptTabpage += "    var faiSubmit = 'N';\n";
        scriptTabpage += "    <!--corpoCaricaBlocchiPagina-->\n";
        scriptTabpage += "    if ( faiSubmit == 'S') {\n";
        scriptTabpage += "      addHiddenInput(document.getElementById(\"submitForm\"));\n";
        scriptTabpage += "    }\n";
        scriptTabpage += "  }\n";
        scriptTabpage += "</script>\n";
      }
      
    
    } catch (SQLException sqle) {
      loggerError("Modello::caricaModelloDaDb() (3) -"+ sqle.getMessage(),sqle);
    } finally {
      if (dbOpEsterna==null) free(dbOp);
    }
    //Debug Tempo
    stampaTempo("Modello::caricaModelloDaDb - Fine",a,c,"",ptime);
    //Debug Tempo


  }

  /**
   * Ritorna il size della lista degli elementi del modello
   **/
  public int getNumeroElementi(){
    return elementi.size();
  }

  /**
   * Ritorna l'elemnto in posizione pos
   **/
  public IElementoModello getElemento(int pos) {
    if ((pos >= 0) && (pos <= elementi.size()))
      return (IElementoModello)elementi.get(pos);
    else
      return null;
  }

  public String getIstruzioni() {
    return istruzioni;
  }
  /**
   * Funzione che restituisce una stringa che rappresenta il nome del file
   * temporaneo corrispondente al modello.
   * Il nome del file temporaneo � sempre composto dalla concatenazione dell'area e del
   * nome del modello, ma stanno in una directory in cui nome � dato direttamente
   * dall'id della sessione. Questa funzione testa l'esisenza della directory e se non �
   * presente la crea ex-novo.
   * NOTARE che l'estensione viene caricata dal chiamante.
   *
   * @return directoryTemporanea + area + modello
   * @author Adelmo
   **/

  public String nomeFileTemp() {
    String    dirTemp;
    File      ft;

    String myPathTemp = sRequest.getSession().getServletContext().getRealPath("")+File.separator+"temp"+File.separator;
    
    //dirTemp = Parametri.PATHTEMP+(sRequest.getSession()).getId();
    dirTemp = myPathTemp+(sRequest.getSession()).getId();
    
    ft = new File(dirTemp);
    if (!ft.isDirectory()){
      ft.mkdir();
    }
    return (dirTemp+File.separator+area+codiceModello);
  }

  /**
   * urlFileTemp
   *
   * @author Adelmo
   **/
  public String urlFileTemp(){
    String    dirTemp;
//    File      ft;
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
    String    myRetUrl = serverScheme+"://"+
                         serverName+":"+
                         serverPort+
                         sRequest.getContextPath()+"/temp/";

    dirTemp = myRetUrl+(sRequest.getSession()).getId();
    
    return (dirTemp+"/"+area+codiceModello);
  }

  /**
   * Scrive su un file in una opportuna directory di lavoro il risultato HTML appena prodotto
   * restiuisce il nome del file completo di path al chiamante.
   * Puo' generare una exception.
   *
   * @author Adelmo
   **/
   public String scriviSuFile() throws Exception{
     //Debug Tempo
     long ptime = stampaTempo("Modello::scriviSuFile - Inizio",area,codiceModello,codiceRichiesta,0);
     //Debug Tempo
      String      nomefile, modval;
      File        idFile;
      FileWriter  wFile;

      nomefile=nomeFileTemp()+".htm";
      if (isWin) {
       nomefile=nomefile.replaceAll("/","\\\\");
      } else {
        nomefile=nomefile.replaceAll("\\\\","/");
      }

      idFile = new File(nomefile);
      if (idFile.canWrite()==false){
        new Exception("FILE NON SCRIVIBILE");
      }

      wFile = new FileWriter(idFile);
      modval = getPrivPRNValue();
      wFile.write(modval,0,modval.length());
      wFile.flush();
      wFile.close();
      //Debug Tempo
      stampaTempo("Modello::scriviSuFile - Fine",area,codiceModello,codiceRichiesta,ptime);
      //Debug Tempo
      return nomefile;
   }

  /**
   *
   */
  public String getQueryURL() {
    return this.queryHtmlIn;
  }

  /**
   *
   */
  public String getUltimoAgg() {
    if (this.sUltimoAgg == null) {
      return "";
    } else {
      return this.sUltimoAgg;
    }
  }

  /**
   *
   */
  public String getSuccessivo() {
    return this.modelloSuccessivo;
  }

  /**
   *
   */
  public String getPrecedente() {
    return this.modelloPrecedente;
  }

  /**
   *
   */
  public void setQueryURL(String s) {
    this.queryHtmlIn = s;
  }


  /**
  *
  */
 public void setTimeFirstOpen(String s) {
   this.timeFirstOpen = s;
 }

  /**
   *
   */
  public void setUltimoAgg(String s) {
    this.sUltimoAgg = s;
  }


  /**
   *
   */
  private void free(IDbOperationSQL dbOp) {
    try {
//      dbOp.getStmSql().clearParameters();
      dbOp.close();
    } catch (Exception e) {
      //e.printStackTrace();
    }
  }

  /**
   * 
   */
  public String getAllegati() {
    return listaAlle;
  }

  /**
   * 
   */
  public boolean nextMod() {
    return nextModel;
  }
  
  /**
   * 
   */
  public boolean beforeMod() {
    return beforeModel;
  }

  /**
   * 
   */
  public boolean isW3c() {
    if (w3c.equalsIgnoreCase("N")) {
      return false;
    } else {
      return true;
    }
  }

  public String creaPulsante(String pulsante, String pLabel, String icona, String separatore, String queryUrl2, String controllo_js ) {
    return creaPulsante(pulsante, pLabel, icona, separatore, queryUrl2, controllo_js, (Etichetta)null , (IDbOperationSQL)null );
  }

  /**
   * 
   */
  public String creaPulsante(String pulsante, String pLabel, String icona, String separatore, String queryUrl2, String controllo_js, IDbOperationSQL dbOpEsterna ) {
    return creaPulsante(pulsante, pLabel, icona, separatore, queryUrl2, controllo_js, (Etichetta)null , dbOpEsterna);
  }

  public String creaPulsante(String pulsante, String pLabel, String icona, String separatore, String queryUrl2, String controllo_js, Etichetta et ) {
    return creaPulsante(pulsante,pLabel,icona,separatore,queryUrl2,controllo_js,et,null);
  }
    /**
   * 
   */
  public String creaPulsante(String pulsante, String pLabel, String icona, String separatore, String queryUrl2, String controllo_js, Etichetta et, IDbOperationSQL dbOpEsterna ) {
    //Debug Tempo
    long ptime = stampaTempo("Modello::creaPulsante - Inizio",area,codiceModello,codiceRichiesta,0);
    //Debug Tempo
    IDbOperationSQL  dbOp = null;
    ResultSet       rst = null;
    InputStream     srcBlob;
    File            fdir, ffile;
    String          tooltip = "";
    String          nomefile = "";
    String          pathfile = "";
    String          retval = "";
    String          label = pLabel;
    String          openButton = "<span id='"+pulsante+"'>";
    String          closeButton = "</span>";

    if (pulsante.length() == 0) {
      //Debug Tempo
      stampaTempo("Modello::creaPulsante - Fine",area,codiceModello,codiceRichiesta,ptime);
      //Debug Tempo
      return "";
    }
    if (label == null) {
      label = pulsante;
    }

    if (label.length() == 0) {
      label = pulsante;
    }

    label = label.replaceAll("'", "&#39");
    if (pulsante.equalsIgnoreCase("$B$VERSIONA")) {
    	controllo_js = "f_AjaxVersiona";
    }
    if (pulsante.equalsIgnoreCase("$B$CREAPDFSTD")) {
    	controllo_js = "f_AjaxCreaPDF";
    }
//    message = "onclick=\"window.showModelessDialog('../common/AmvMessaggi.do?msg=Attendere.%20Operazione%20in%20corso','','dialogHeight: 100px; dialogWidth: 300px; dialogTop: 300px; dialogLeft: 400px; edge: Raised; center: Yes; help: No; resizable: No; status: No;');\"";
    if (icona.length() == 0) {
      if (pulsante.equalsIgnoreCase("stampa")) {
        if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
            retval += "<a class='AFCDataLink' href='../common/ServletStampa.do?"+
              "area="+area+"&amp;cm="+codiceModello+"&amp;cr="+codiceRichiesta+"&amp;rw=P&amp;visAll=N&amp;gdmwork="+gdmWork+"' "+ 
              "onclick='window.open(this.href,\"\",\"\"); return false;' "+
              "onkeypress='window.open(this.href,\"\",\"\"); return false;' "+
              "title='Attenzione apre una nuova finestra' >"+
              "<img style='border: none' title='"+label+"' src='../common/images/gdm/printer.gif' alt='"+label+"'/>";
         } else {
            retval += "<a class='AFCDataLink' href='ServletStampa?"+
              "area="+area+"&amp;cm="+codiceModello+"&amp;cr="+codiceRichiesta+"&amp;rw=P&amp;visAll=N&amp;gdmwork="+gdmWork+"' "+ 
              "onclick='window.open(this.href,\"\",\"\"); return false;' "+
              "onkeypress='window.open(this.href,\"\",\"\"); return false;' "+
              "title='Attenzione apre una nuova finestra' >"+
              "<img style='border: none' title='"+label+"' src='images/gdm/printer.gif' alt='"+label+"'/>";
         }
         retval += "</a>&nbsp;";
         //Debug Tempo
         stampaTempo("Modello::creaPulsante - Fine",area,codiceModello,codiceRichiesta,ptime);
         //Debug Tempo
         return openButton+retval+closeButton;
      }
      if (pulsante.equalsIgnoreCase("Allega")) {
      	if (compAll.getModifica() == 0) {
      		return "";
      	}
        retval += "<a href='' class='AFCGuidaLink' "+
        "onclick='f_AjaxSessione(\"\",\"DATA_ULTIMO_AGG\",\"\",\"apriServletUpload()\",\"\"); return false;' "+
        "onkeypress='f_AjaxSessione(\"\",\"DATA_ULTIMO_AGG\",\"\",\"apriServletUpload()\",\"\"); return false;' "+
        "title='Attenzione apre una nuova finestra per gli allegati' >";

        if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
          retval += "<img style='border: none; ' title='"+label+"' src='../common/images/gdm/clip.gif' alt='"+label+"' /></a>&nbsp;\n";
        } else {
          retval += "<img style='border: none; ' title='"+label+"' src='images/gdm/clip.gif' alt='"+label+"' /></a>&nbsp;&nbsp;\n";
        }
        retval += "</a>&nbsp;";
        //Debug Tempo
        stampaTempo("Modello::creaPulsante - Fine",area,codiceModello,codiceRichiesta,ptime);
        //Debug Tempo
        return openButton+retval+closeButton;
      }
/*      if (pulsante.equalsIgnoreCase("ExportDati")) {
          retval += "<a href='' class='AFCGuidaLink' "+
          "onclick='clickButton(document.getElementById(\"submitForm\"),\"ExportDati\"); ' "+
          "onkeypress='clickButton(document.getElementById(\"submitForm\"),\"ExportDati\"); ' "+
          "title='Estrai dati' >";

          retval += "<img style='border: none; ' title='Estrai dati' src='../common/images/report-excel.png' alt='Estrai dati' /></a>&nbsp;\n";
 //         retval += "</a>&nbsp;";
          //Debug Tempo
          stampaTempo("Modello::creaPulsante - Fine",area,codiceModello,codiceRichiesta,ptime);
          //Debug Tempo
          return openButton+retval+closeButton;
        }*/

      if (pulsante.equalsIgnoreCase("Modifica")) {
          retval = "<input class='AFCButton' style='cursor: hand;' type='button' name='"+pulsante+"' value='"+label+"' onclick='document.getElementById(\"submitForm\").action = \""+queryModifica+"\"; clickButton(document.getElementById(\"submitForm\"),\""+pulsante+"\"); ' />\n";
      } else {
        if (controllo_js.length() == 0) {
          retval = "<input class='AFCButton' style='cursor: hand;' type='button' name='"+pulsante+"' value='"+label+"' onclick='clickButton(document.getElementById(\"submitForm\"),\""+pulsante+"\"); ' />\n";
        } else {
          retval = "<input class='AFCButton' style='cursor: hand;' type='button' name='"+pulsante+"' value='"+label+"' onclick='"+controllo_js+"();' />\n";
        }
      }
    } else {
      String myPathTemp = sRequest.getSession().getServletContext().getRealPath("")+File.separator+"temp"+File.separator;

      String query = "SELECT * "+
                     "FROM ICONE "+
                     "WHERE ICONA = :ICONA ";

      try {
        fdir = new File(myPathTemp + area + File.separator + codiceModello);
        if (!fdir.isDirectory()) {
            fdir.mkdirs();   
        }
       // et = etichette.getEtichetta(pulsante.toUpperCase());
        tooltip = et.getTooltip();
        nomefile = et.getNome();
        if (tooltip == null || tooltip.length() == 0) {
          tooltip = pulsante;
        }
        pathfile = serverScheme+"://"+
        serverName+":"+
        serverPort+
        sRequest.getContextPath()+
        "/temp/"+ area + "/" + codiceModello +"/"+ nomefile;
        
        ffile = new File(myPathTemp + area + File.separator + codiceModello + File.separator + nomefile);
        long agg = et.getData();
        boolean esisteicona = true;
        if (!ffile.exists()  || ffile.lastModified() != agg) {
          esisteicona = false;
          if (dbOpEsterna==null) {
            dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);
          }
          else {
            dbOp = dbOpEsterna;
          }

          try {
            dbOp.setStatement(query);
            dbOp.setParameter(":ICONA", icona);
            dbOp.execute();
            rst = dbOp.getRstSet();
            if (rst.next()) {
              srcBlob = dbOp.readBlob("RISORSA");
              if (srcBlob == null) {
                logger.error("Modello::creaPulsante() - Attenzione! InputStream vuoto.");
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
              ffile.setLastModified(agg);
              esisteicona = true;
            }
          }
          finally {
            if (dbOpEsterna==null) free(dbOp);
          }

          
        }
        if (esisteicona) {
          if (pulsante.equalsIgnoreCase("stampa")) {
            if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
                retval += "<a class='AFCDataLink' href='../common/ServletStampa.do?"+
                  "area="+area+"&amp;cm="+codiceModello+"&amp;cr="+codiceRichiesta+"&amp;rw=P&amp;visAll=N&amp;gdmwork="+gdmWork+"' "+ 
                  "onclick='window.open(this.href,\"\",\"\"); return false;' "+
                  "onkeypress='window.open(this.href,\"\",\"\"); return false;' "+
                  "title='Attenzione apre una nuova finestra' >"+
                  "<img style='border: none' title='"+tooltip+"' src='"+pathfile+"' alt='"+tooltip+"'/>";
             } else {
                retval += "<a class='AFCDataLink' href='ServletStampa?"+
                  "area="+area+"&amp;cm="+codiceModello+"&amp;cr="+codiceRichiesta+"&amp;rw=P&amp;visAll=N&amp;gdmwork="+gdmWork+"' "+ 
                  "onclick='window.open(this.href,\"\",\"\"); return false;' "+
                  "onkeypress='window.open(this.href,\"\",\"\"); return false;' "+
                  "title='Attenzione apre una nuova finestra' >"+
                  "<img style='border: none' title='"+tooltip+"' src='"+pathfile+"' alt='"+tooltip+"'/>";
             }
             retval += "</a>&nbsp;";
             //Debug Tempo
             stampaTempo("Modello::creaPulsante - Fine",area,codiceModello,codiceRichiesta,ptime);
             //Debug Tempo
             return openButton+retval+closeButton;
          }
          if (pulsante.equalsIgnoreCase("Allega")) {
          	if (compAll.getModifica() == 0) {
          		return "";
          	}
            retval += "<a href='' class='AFCGuidaLink' "+
            "onclick='f_AjaxSessione(\"\",\"DATA_ULTIMO_AGG\",\"\",\"apriServletUpload()\",\"\"); return false;' "+
            "onkeypress='f_AjaxSessione(\"\",\"DATA_ULTIMO_AGG\",\"\",\"apriServletUpload()\",\"\"); return false;' "+
            "title='Attenzione apre una nuova finestra per gli allegati' >";
            if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
              retval += "<img style='border: none; ' title='"+tooltip+"' src='"+pathfile+"' alt='"+tooltip+"' /></a>&nbsp;";
            } else {
              retval += "<img style='border: none; ' title='"+tooltip+"' src='"+pathfile+"' alt='"+tooltip+"' /></a>&nbsp;";
            }
         //   retval += "</a>&nbsp;";
            //Debug Tempo
            stampaTempo("Modello::creaPulsante - Fine",area,codiceModello,codiceRichiesta,ptime);
            //Debug Tempo
            return openButton+retval+closeButton;
          } 

          if (pulsante.equalsIgnoreCase("Modifica")) {
            retval = "<a href='' onclick='document.getElementById(\"submitForm\").action = \""+queryModifica+"\"; clickButton(document.getElementById(\"submitForm\"),\""+pulsante+"\"); return false;' title='"+tooltip+"' ><img style='border: none;' src='"+pathfile+"' alt='"+tooltip+"'/></a>&nbsp;";
          } else {
            if (controllo_js.length() == 0) {
              retval = "<a href='' onclick='clickButton(document.getElementById(\"submitForm\"),\""+pulsante+"\"); return false;' title='"+tooltip+"' ><img style='border: none;' src='"+pathfile+"' alt='"+tooltip+"'/></a>&nbsp;";
            } else {
              retval = "<a href='' onclick='"+controllo_js+"(); return false;' title='"+tooltip+"' ><img style='border: none;' src='"+pathfile+"' alt='"+tooltip+"'/></a>&nbsp;";
            }
          }
        } else {
          if (pulsante.equalsIgnoreCase("Modifica")) {
            retval = "<input class='AFCButton' style='cursor: hand;' type='button' name='"+pulsante+"' value='"+label+"' onclick='document.getElementById(\"submitForm\").action = \""+queryModifica+"\"; clickButton(document.getElementById(\"submitForm\"),\""+pulsante+"\"); ' />\n";
          } else {
            if (controllo_js.length() == 0) {
              retval = "<input class='AFCButton' style='cursor: hand;' type='button' name='"+pulsante+"' value='"+label+"' onclick='clickButton(document.getElementById(\"submitForm\"),\""+pulsante+"\");' />\n";
            } else {
              retval = "<input class='AFCButton' style='cursor: hand;' type='button' name='"+pulsante+"' value='"+label+"' onclick='"+controllo_js+"();' />\n";
            }
          }
        }
        
      } catch (Exception sqle) {
        loggerError("Modello::creaPulsante() (1) - Errore SQL: "+sqle.toString(),sqle);
      }
    }
    if (separatore.equalsIgnoreCase("S")) {
      if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
        retval += "<img style='border: none;' title='"+label+"' src='../common/images/gdm/separator.gif' alt='"+label+"' /></a>\n";
      } else {
        retval += "<img style='border: none;' title='"+label+"' src='images/gdm/separator.gif' alt='"+label+"' /></a>\n";
      }
    }
    
    //Debug Tempo
    stampaTempo("Modello::creaPulsante - Fine",area,codiceModello,codiceRichiesta,ptime);
    //Debug Tempo
    return openButton+retval+closeButton;
  }

  public String listaPulsanti(HttpServletRequest pRequest) {
    return listaPulsanti(pRequest,null);
  }

  /**
   * 
   */
  public String listaPulsanti(HttpServletRequest pRequest, IDbOperationSQL dbOpEsterna) {
    //Debug Tempo
    long ptime = stampaTempo("Modello::listaPulsanti - Inizio",area,codiceModello,codiceRichiesta,0);
    //Debug Tempo
    
    if (lettura.equalsIgnoreCase("V")) {
    	return "";
    }
    IDbOperationSQL  dbOp = null;
//    IDbOperationSQL  dbOpI = null;
//    ResultSet       resultQuery = null;
    ResultSet       rs = null;
    String          query = "";
    String          istruzione = "";
    String          dominio = "";
    String          personalizzazione = "";
    String          lista = "";
    String          union = "";
    String          queryStm = null;

    query = "SELECT   DOMINIO, PERSONALIZZAZIONE, ISTRUZIONE "+
            "FROM     DOMINI "+
            "WHERE    AREA = :AREA AND "+
                     "SEQ_DOMINIO_AREA > 0 AND "+
                     "PRECARICA = 'E' AND "+
                     "CODICE_MODELLO = :CM "+
            "ORDER BY SEQ_DOMINIO_AREA";
    
    try {
      if (dbOpEsterna==null) {
        dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);
      }
      else {
        dbOp = dbOpEsterna;
      }

      
      dbOp.setStatement(query);
      dbOp.setParameter(":AREA", area);
      dbOp.setParameter(":CM", codiceModello);
      dbOp.execute();
    	rs = dbOp.getRstSet();
      
      boolean aggiungi = true;
      Personalizzazioni pers = null;
      pers = (Personalizzazioni)pRequest.getSession().getAttribute("_personalizzazioni_gdm");
      while (rs.next()) {
        aggiungi = true;
          dominio = rs.getString("DOMINIO");
          personalizzazione = rs.getString("PERSONALIZZAZIONE");
          if (personalizzazione.equalsIgnoreCase("N")) {
            if (pers != null) {
              if (pers.existPersonalizzazione(Personalizzazioni.BARRA_ETICHETTE,area+"#"+dominio+"#"+codiceModello)) {
                aggiungi = false;
              }
            }
          } else {
            if (pers != null) {
              if (!pers.isPersonalizzazione(Personalizzazioni.BARRA_ETICHETTE,area+"#"+dominio+"#"+codiceModello)) {
                aggiungi = false;
              }
            } else {
              aggiungi = false;
            }
          }
          if (aggiungi) {
            Clob clob = rs.getClob("ISTRUZIONE");
            long clobLen = clob.length();
            int i_clobLen = (int)clobLen;
            istruzione += union + clob.getSubString(1, i_clobLen);
            union = " UNION ALL ";
          }
      }
      
      if (istruzione != null && istruzione.length() == 0) {
        return "";
      }
      ModulisticaParser mp = new ModulisticaParser(pRequest);
      Properties pmp = new Properties();
      pmp.setProperty("TIPO","S");
      mp.setExtraKeys(pmp);
      queryStm = mp.bindingDeiParametri(istruzione);
      if (queryStm == null ){
        logger.error("Modello::listaPulsanti() - Attenzione! Parametro mancante su "+istruzione);

        //Debug Tempo
        stampaTempo("Modello::listaPulsanti - Fine",area,codiceModello,codiceRichiesta,ptime);
        //Debug Tempo
        return "";  // *** exit point ***
      }
      dbOp.setStatement(queryStm);
      dbOp.execute();
      rs = dbOp.getRstSet();
      while (rs.next()) {
        String pulsanti = rs.getString(1);
        if (pulsanti != null) {
          //Aggiungo le nuove etichette alla lista
          StringTokenizer st = new StringTokenizer(pulsanti, Parametri.SEPARAVALORI);
          String nextToken = "";
          while (st.hasMoreTokens()) {
            nextToken = st.nextToken().toUpperCase();
            int i = lista.indexOf(nextToken);
            if ( i > -1) {
              if (lista.equalsIgnoreCase(nextToken)) {
                nextToken = "";
              }
              i = lista.indexOf(nextToken+Parametri.SEPARAVALORI);
              if (i == 0) {
                nextToken = "";
              }
              i = lista.indexOf(Parametri.SEPARAVALORI+nextToken+Parametri.SEPARAVALORI);
              if (i > -1) {
                nextToken = "";
              }
              i = lista.indexOf(Parametri.SEPARAVALORI+nextToken)+nextToken.length()+1;
              if (i >= lista.length()) {
                nextToken = "";
              }
            }
            if (lista.length() == 0) {
              lista = nextToken;
            } else {
              lista += Parametri.SEPARAVALORI+nextToken;
            }
          }
        }
      }

//      free(dbOpI);
    } catch(Exception e) {
      loggerError("Modello::listaPulsanti() - Attenzione: il dominio "+dominio+" presenta delle anomalie. "+e.toString(),e);

//      free(dbOpI);
    }
    finally {
      if (dbOpEsterna==null) free(dbOp);
    }

    //Debug Tempo
    stampaTempo("Modello::listaPulsanti - Fine",area,codiceModello,codiceRichiesta,ptime);
    //Debug Tempo
    return lista;
    
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
 public String getTimeFirstOpen() {
   return timeFirstOpen;
 }

 protected long stampaTempo(String sMsg, String area, String cm, String cr, long ptime) {
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
 
 public void creaLinkAllegati() {
   String salleg = "";
   int iall= 0;
   String sUs = (String)sRequest.getSession().getAttribute("UtenteGDM");
   String sRu = (String)sRequest.getSession().getAttribute("RuoloGDM");

   if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
     urlUpload = serverScheme+"://"+
                 serverName+":"+
                 serverPort+
                 sRequest.getContextPath()+"/../"+Parametri.APPLICATIVO+"/"+
                 "ServletUpload?cr="+codiceRichiesta+"&area="+area+"&cm="+codiceModello;
//                 "&ua="+getUltimoAgg();
   } else {
     urlUpload = serverScheme+"://"+
                 serverName+":"+
                 serverPort+
                 sRequest.getContextPath()+"/"+
                 "ServletUpload?cr="+codiceRichiesta+"&area="+area+"&cm="+codiceModello;
//                 "&ua="+getUltimoAgg();
   }
   if (Parametri.ALLEGATI_SINGLE_SIGN_ON.equalsIgnoreCase("N")) {
  	 urlUpload += "&us="+sUs+"&ruolo="+sRu;
   }
   allega = "";
   iall = modello.indexOf("<o:Allegati>");
   if (iall > -1) {
     salleg = modello.substring(iall+12,iall+13);
     if (salleg.equalsIgnoreCase("Y")) {
       String pAllega = Parametri.getParametriLabel(area,codiceModello,"ALLEGA");
       if (pAllega.length() == 0) {
         allega = "";
         listaAlle = "Y";
       } else {
      	 allega = "<a href='#' class='AFCGuidaLink' "+
					        "onclick='f_AjaxSessione(\"\",\"DATA_ULTIMO_AGG\",\"\",\"apriServletUpload()\",\"\"); return false;' "+
					        "onkeypress='f_AjaxSessione(\"\",\"DATA_ULTIMO_AGG\",\"\",\"apriServletUpload()\",\"\"); return false;' "+
					        "title='Attenzione apre una nuova finestra per gli allegati' >";
/*         allega = "<a href='"+urlUpload+"' class='AFCGuidaLink' "+
                   "onclick='window.open(this.href,\"\",\"width=600,height=500, resizable=yes\"); return false;' "+
                   "onkeypress='window.open(this.href,\"\",\"width=600,height=500, resizable=yes\"); return false;' "+
                   "title='Attenzione apre una nuova finestra per gli allegati' >";*/
         if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
           allega += "<img style='border: none' title='"+pAllega+"' src='../common/images/gdm/clip.gif' alt='"+pAllega+"' /></a>&nbsp;\n";
         } else {
           allega += "<img style='border: none' title='"+pAllega+"' src='images/gdm/clip.gif' alt='"+pAllega+"' /></a>&nbsp;&nbsp;\n";
         }
         listaAlle = "Y";
       }
     } else {
       listaAlle = "N";
     }
   }
   if (compAll == null || compAll.getModifica() == 0) {
  	 allega = "";
 		}

 }

	/**
	 * @param chk the chk to set
	 */
	public void setChk(CheckDocumento chk) {
		this.chk = chk;
	}
	
	public CheckDocumento getChk() {
		return chk;
	}
	
	public void setGDCLink (String valore) {
		gdc_link = valore;
	}
	
	public void setCompAll(CompetenzeAllegati compAll) {
		this.compAll = compAll;
	}

	public CompetenzeAllegati getCompAll() {
		return compAll;
	}


}

