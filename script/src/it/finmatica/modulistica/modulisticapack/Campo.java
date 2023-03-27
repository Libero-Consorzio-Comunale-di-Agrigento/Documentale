package it.finmatica.modulistica.modulisticapack;
 
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.sql.*;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.modulistica.personalizzazioni.Personalizzazioni;
import it.finmatica.dmServer.management.*;
import org.apache.log4j.Logger;
import org.owasp.encoder.Encode;

/**
 * Classe Campo
 *
 * @author  Nicola Samoggia
 * @author  Antonio Plastini
 * @author  Sergio Spadaro
 * @version 1.0     
 */
public abstract class Campo implements IElementoModello {
  public final static int MAXLEN_VALORE_S = 1000000;  
                                                      // Numero massimo di caratteri ammessi per campi di tipo String

  protected String    area;
  protected String    area_dato;
  protected String    modello;
  protected String    dato;
  protected String    label;
  protected String    stile;
  protected String    stileLett;
  protected String    classname;
  protected String    istruzioni;
  protected String    tipo;               // Rappresenta il tipo di dato: Numerico(N), Data(D) o Stringa(S)
  protected String    formato;               
  protected String    domForm;               
  protected String    domVisu;               
  protected int       lunghezzaStandard;
  protected int       decimaliStandard;
  protected int       lunghezza;
  protected int       decimali;
  protected String    tipoAccesso;
  protected String    tipoCampo;  // tipoCampo rappresenta il tipo di campo rappresentato in HTML:
                                  //          Standard(S) (= TextField)
                                  //          RichTextArea(Z)
                                  //          TextArea(T)
                                  //          ComboBox(C)
                                  //          RadioButton(R)
                                  //          CheckButton(B)
                                  // Si tratta di una informazione che si può ricavre solo dopo
                                  // aver interpretato la meta-informazione del capo del modello.
  protected String    campoCalcolato;
  protected String    bloccoPopup;
  protected String    note;
  protected String    istruzioniStandard;
  protected Timestamp scadenza;
  protected String    valore;
  protected String    valore_2;
  protected String    urlLocale;
  protected Dominio   dominio;
  protected Dominio   dominioForm;

  protected String    completeContextURL;
  protected String    completeContextURL_images;
  protected String    list_fields = "";
  protected String    pdo = "";
  protected String    elementiAjax = "";
  protected String    idAjax = "";
  protected String    senzaSalva = "N";
  protected String    maiuscolo = "N";
//  protected String    siglaStile = "";
  private static Logger logger = Logger.getLogger(Campo.class);
  private  boolean debuglog = logger.isDebugEnabled();
  protected static String qureyCaricaCampo = 
														  "SELECT dm.ISTRUZIONI_COMPILAZIONE, "+
														  "d.DOMINIO, "+
														  "d.TIPO, "+
														  "d.LUNGHEZZA, "+  // Lunghezza standard
														  "d.DECIMALI, "+   // Decimali standard
														  "dm.LUNGHEZZA, "+  // Lunghezza del campo per quel modello  (new)
														  "dm.DECIMALI, "+   // Decimali del campo per quel modello   (new)
														  "dm.TIPO_ACCESSO, "+  // Tipo di accesso (Lettura/Scrittura)
														  "dm.TIPO_CAMPO, "+  // Tipo di campo (S/T/C/R/B/H)
														  "d.NOTE, "+
														  "d.ISTRUZIONI_COMPILAZIONE, "+
														  "d.FORMATO_DATA, "+
														  "d.DOMINIO_FORMULA, "+
														  "d.DOMINIO_VISUALIZZA, "+
														  "dm.CAMPO_CALCOLATO, "+
														  "dm.BLOCCO, "+
														  "dm.AREA_DATO, "+
														  "d.label, " +
														  "d.senza_salvataggio, "+
														  "d.TESTO_MAIUSCOLO "+
														  "FROM DATI_MODELLO dm, "+
														  "DATI d "+
														  "WHERE dm.AREA = :AREA AND "+
														  "dm.CODICE_MODELLO = :CM AND "+
														  "dm.DATO = :DATO AND "+
														  "dm.AREA_DATO = d.AREA AND "+
														  "dm.DATO = d.DATO";
  
  /**
   * Costruttore
   */
  public Campo() {
  }

  /**   
   * Costruttore
   */
  public Campo(HttpServletRequest pRequest, String pArea, String pModello, String pDato, Timestamp scad) throws Exception {
    area      = pArea;
    modello   = pModello;
    dato      = pDato;
    scadenza  = scad;

    //Debug Tempo
    stampaTempo("BloccoPopup - Inizio",area,modello,dato,0);
    //Debug Tempo

    String  serverScheme,serverName,serverPort;
    String lettura;

    if (Parametri.PROTOCOLLO.length() == 0) {
      serverScheme = pRequest.getScheme();
    } else {
      serverScheme = Parametri.PROTOCOLLO;
    }
    if (Parametri.SERVERNAME.length() == 0) {
      serverName = pRequest.getServerName();
    } else {
      serverName = Parametri.SERVERNAME;
    }
    if (Parametri.SERVERPORT.length() == 0) {
      serverPort = ""+pRequest.getServerPort();
    } else {
      serverPort = Parametri.SERVERPORT;
    }
    completeContextURL = serverScheme+"://"+
                         serverName+":"+
                         serverPort+
                         pRequest.getContextPath()+"/";
                         
    pdo = (String)pRequest.getSession().getAttribute("pdo");

    lettura = pRequest.getParameter("rw"); 
  	if (lettura == null) {
  		lettura = "";
  	}
     
    if (lettura.equalsIgnoreCase("V")) {
      if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
        completeContextURL_images = "../common/images/gdm/";
      } else {
        completeContextURL_images = "./images/gdm/";
      }
    } else {
      if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
        completeContextURL_images = completeContextURL+"common/images/gdm/";
      } else {
        completeContextURL_images = completeContextURL+"images/gdm/";
      }
    }
//    siglaStile = (String)pRequest.getSession().getAttribute("gdmsiglastile");
  }

  public void caricaCampo(HttpServletRequest request) throws Exception {
    caricaCampo(request,null);
  }

  /**
   * caricaCampo()
   */
  public void caricaCampo(HttpServletRequest request, IDbOperationSQL dbOpEsterna) throws Exception {
    //Debug Tempo
    long ptime = stampaTempo("Campo::caricaCampo - Inizio",area,modello,dato,0);
    //Debug Tempo
    IDbOperationSQL dbOp = null;
    ResultSet   resultQuery = null;
    ListaDomini ld;

/*    String query = "SELECT dm.ISTRUZIONI_COMPILAZIONE, "+
                          "d.DOMINIO, "+
                          "d.TIPO, "+
                          "d.LUNGHEZZA, "+  // Lunghezza standard
                          "d.DECIMALI, "+   // Decimali standard
                          "dm.LUNGHEZZA, "+  // Lunghezza del campo per quel modello  (new)
                          "dm.DECIMALI, "+   // Decimali del campo per quel modello   (new)
                          "dm.TIPO_ACCESSO, "+  // Tipo di accesso (Lettura/Scrittura)
                          "dm.TIPO_CAMPO, "+  // Tipo di campo (S/T/C/R/B/H)
                          "d.NOTE, "+
                          "d.ISTRUZIONI_COMPILAZIONE, "+
                          "d.FORMATO_DATA, "+
                          "d.DOMINIO_FORMULA, "+
                          "d.DOMINIO_VISUALIZZA, "+
                          "dm.CAMPO_CALCOLATO, "+
                          "dm.BLOCCO, "+
                          "dm.AREA_DATO, "+
                          "d.label, " +
                          "d.senza_salvataggio, "+
                          "d.TESTO_MAIUSCOLO "+
                     "FROM DATI_MODELLO dm, "+
                          "DATI d "+
                    "WHERE dm.AREA = '" + area + "' AND "+
                          "dm.CODICE_MODELLO = '" + modello + "' AND "+
                          "dm.DATO = '" + dato + "' AND "+
                          "dm.AREA_DATO = d.AREA AND "+
                          "dm.DATO = d.DATO";*/


    try {
      try {
        if (dbOpEsterna==null) {
          dbOp = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);
        }
        else {
          dbOp=dbOpEsterna;
        }

        dbOp.setStatement(qureyCaricaCampo);
        dbOp.setParameter(":AREA", area);
        dbOp.setParameter(":CM", modello);
        dbOp.setParameter(":DATO", dato);
          dbOp.execute();
        resultQuery = dbOp.getRstSet();
      } catch (Exception sqle) {
        loggerError("Campo::caricaCampo() - Area: "+area+" - Modello: "+modello+" - Campo: "+dato+" - Attenzione: Si è verificato un errore SQL (1): "+sqle.toString(),sqle);
      }

      try {
       if (!resultQuery.next()) {
           logger.error("Campo::caricaCampo() - Area: "+area+" - Modello: "+modello+" - Campo: "+dato+" - resulset vuoto su dati e dati_modello"+dbOp.getPrepString());

       }
       istruzioni = resultQuery.getString(1);
       area_dato  = resultQuery.getString("AREA_DATO");
       tipoCampo  = resultQuery.getString("TIPO_CAMPO");
       tipo               = resultQuery.getString("TIPO");
       campoCalcolato     = resultQuery.getString("CAMPO_CALCOLATO");
       bloccoPopup        = resultQuery.getString("BLOCCO");
       lunghezzaStandard  = resultQuery.getInt(4);
       decimaliStandard   = resultQuery.getInt(5);
       lunghezza          = resultQuery.getInt(6);
       decimali           = resultQuery.getInt(7);
       tipoAccesso        = resultQuery.getString("TIPO_ACCESSO");
       note               = resultQuery.getString("NOTE");
       formato            = resultQuery.getString("FORMATO_DATA");
       domForm            = resultQuery.getString("DOMINIO_FORMULA");
       label              = resultQuery.getString("LABEL");
       senzaSalva         = resultQuery.getString("SENZA_SALVATAGGIO");
       maiuscolo          = resultQuery.getString("TESTO_MAIUSCOLO");
       domVisu            = resultQuery.getString("DOMINIO_VISUALIZZA");

       String persDom = null;
       Personalizzazioni pers = null;
       pers = (Personalizzazioni)request.getSession().getAttribute("_personalizzazioni_gdm");
       try {
          if (resultQuery.getString(2) == null) {
            // Dominio nullo
            dominio = null;
          } else {
            if ((resultQuery.getString(2)).length() == 0) {
              // Stringa che rappresenta il dominio nullo
              dominio = null;
            } else {
  /*            // Dominio non nullo
              ld = (ListaDomini)request.getSession().getAttribute("listaDominiStandard");
              if (ld == null) {
                ld = new ListaDomini();
              }
              String sDom = resultQuery.getString(2);
              if (pers != null) {
                persDom = pers.getPersonalizzazione(Personalizzazioni.DOMINI, area_dato+"#"+sDom+"#-");
                int j = persDom.indexOf("#");
                int k = persDom.lastIndexOf("#");
                sDom = persDom.substring(j+1,k);
              }
              dominio = ld.getDominio(area_dato, sDom, "-", request);
              request.getSession().setAttribute("listaDominiStandard",ld);
  */
                if (tipoCampo.equalsIgnoreCase("C") || tipoCampo.equalsIgnoreCase("R") || tipoCampo.equalsIgnoreCase("B")) {
                  // Dominio non nullo
                  ld = (ListaDomini)request.getSession().getAttribute("listaDominiStandard");
                  if (ld == null) {
                    ld = new ListaDomini();
                  }
                  String sDom = resultQuery.getString(2);
                  if (pers != null) {
                    persDom = pers.getPersonalizzazione(Personalizzazioni.DOMINI, area_dato+"#"+sDom+"#-");
                    int j = persDom.indexOf("#");
                    int k = persDom.lastIndexOf("#");
                    sDom = persDom.substring(j+1,k);
                  }
                  dominio = ld.getDominio(area_dato, sDom, "-", request,dbOpEsterna);
                  request.getSession().setAttribute("listaDominiStandard",ld);
                } else {
                  dominio = null;
                }
              }
          }
        } catch(Exception e) {
          loggerError("Campo::caricaCampo() - Area: "+area+" - Modello: "+modello+" - Campo: "+dato+" - Attenzione: Si è verificato un errore (2): "+e.toString(),e);
        }


        if (senzaSalva == null) {
          senzaSalva = "N";
        }
        if (maiuscolo == null) {
          maiuscolo = "N";
        }
        if (label == null) {
          label = dato;
        }
        if (domForm == null) {
          dominioForm = null;
        } else {
          if (pers != null) {
            persDom = pers.getPersonalizzazione(Personalizzazioni.DOMINI, area_dato+"#"+domForm+"#-");
            int j = persDom.indexOf("#");
            int k = persDom.lastIndexOf("#");
            domForm = persDom.substring(j+1,k);
          }
          dominioForm = new DominioFormula(area_dato, domForm, "-", tipo, null, dato, request,dbOpEsterna);
        }

        if (domVisu != null) {
          if (pers != null) {
            persDom = pers.getPersonalizzazione(Personalizzazioni.DOMINI, area_dato+"#"+domVisu+"#-");
            int j = persDom.indexOf("#");
            int k = persDom.lastIndexOf("#");
            domVisu = persDom.substring(j+1,k);
          }
        }

        Dominio dp = null;
        ListaProtetti lp = (ListaProtetti)request.getSession().getAttribute("listaProtetti");
        String myVal = null;
        if (lp != null) {
          int numDom = lp.domini.size();
          int i = 0;
          while (i < numDom && myVal == null) {
            dp = (Dominio)lp.domini.get(i);
              myVal = dp.getValore("$C$"+dato);
            i++;
          }
        }
        if (myVal != null) {
          if (myVal.equalsIgnoreCase("S")) {
            tipoAccesso = "P";
          }
        }

        if (request.getParameter("gdm_Ajax") == null) {
          idAjax = dato;
        } else {
          idAjax = "gdmIdAjax_"+dato;
        }
      } catch (SQLException sqle) {
        loggerError("Campo::caricaCampo() - Area: "+area+" - Modello: "+modello+" - Campo: "+dato+" - Attenzione! Si è verificato un errore SQL (3): "+sqle.toString(),sqle);
      }
    }
    finally {
      if (dbOpEsterna==null) free(dbOp);
    }

    //Debug Tempo
    stampaTempo("Campo::caricaCampo - Fine",area,modello,dato,ptime);
    //Debug Tempo
  }

  /**
   * Carica il valore del campo.
   * Il campo viene valorizzato, nell'ordine, in uno dei seguenti modi:
   * 1 - cercando dato già caricato in un modello precedente identico a quello attuale
   * 2 - cercando dato caricato per un altro modello nella stessa richiesta
   * 3 - cercando un valore di default, cioè controllando se il dato è collegato ad un dominio
   *     nel quale esiste un valore corrispondente ad un codice uguale al nome del campo.
   *     Il valore di default puo' essere caricato anche se il valore trovato sul database è vuoto.
   * Appena una delle condizioni si verifica le altre non vengono considerate.
   */
  public void caricaValore(HttpServletRequest request) throws Exception {
    //Debug Tempo
    long ptime = stampaTempo("Campo::caricaValore - Inizio",area,modello,dato,0);
    //Debug Tempo
    String      rw;//, id_tipodoc;

    rw = request.getParameter("rw");
//    ar = request.getParameter("area");
    
    if (rw == null){
      rw = "R";
    }
    if (rw.equalsIgnoreCase("S")) {
      valore = "";
      //Debug Tempo
      stampaTempo("Campo::caricaValore - Fine",area,modello,dato,ptime);
      //Debug Tempo
      return;
    }
    valore_2 = null;
    if (rw.equalsIgnoreCase("Q")) {
      if (tipoCampo.charAt(0) != 'B') {            // new!  
        // E' un campo legato ad un dominio, ma non è di tipo CHECKBOX
        valore_2 = request.getParameter("_2_"+dato);
//        if (tipoCampo.charAt(0) == 'Z') {
//          if (valore != null) {
//            String valoreNew = "";
//            int ic;
//            valore_2 = URLDecoder.decode(valore,"ISO-8859-1");
//            int up = 0;
//            int posCod = valore.indexOf("mmacode(");
//            while (posCod > -1) {
//              valoreNew += valore_2.substring(up,posCod);
//              up = valore_2.indexOf(")",posCod);
//              String codiceCh = valore.substring(posCod+8,up);
//              up = up + 1;
//              ic = Integer.parseInt(codiceCh);
//              valoreNew += (char)ic;
//              posCod = valore_2.indexOf("mmacode(",up);
//            }
//            valoreNew += valore_2.substring(up);
//            valore_2 = valoreNew;
//          }
//        }
        if (valore_2 != null) {
          if (valore_2.length() == 0) {
            valore_2 = null;
          }
        }
      } else {
        // E' un campo legato ad un CAMPO di tipo CHECKBOX
        valore_2 = "";
        for (int j=0; j<1000; j++) {  // 1000 valori al massimo.
          String s = request.getParameter("_2_"+dato+Integer.toString(j));
          if (s != null){
            // Attacco sempre un separatore in fondo, in tal modo quando dovrò
            // recuperare un valore lo richiamerò sempre utilizzando il separatore
            valore_2 = valore_2 + s + Parametri.SEPARAVALORI;
          }
        }
        if (valore_2.length() == 0) {
          valore_2 = null;
        }
      }
    }
    if (valore_2 == null) {
      valore_2 = "";
    }

    String codiceRichiesta = null;
    
    String cr = request.getParameter("cr");
    if (cr == null) {
      codiceRichiesta = (String)request.getSession().getAttribute("key");
      if (codiceRichiesta == null) {
        codiceRichiesta = area+'-'+request.getSession().getId();
      } else {
        if (!codiceRichiesta.equalsIgnoreCase(area+'-'+request.getSession().getId())) {
          codiceRichiesta = area+'-'+codiceRichiesta+'-'+request.getSession().getId();
        } 
      }
    } else {
      if (!cr.equalsIgnoreCase(area+'-'+request.getSession().getId())) {
        codiceRichiesta = area+'-'+cr+'-'+request.getSession().getId();
      } else {
        codiceRichiesta = cr;
      }
    }

    String mycr = cr;
    if (mycr == null) {
      mycr = (String)request.getSession().getAttribute("key");
      if (mycr == null) {
        mycr = area+'-'+request.getSession().getId();
      }
    }
    String esiste =(String)request.getSession().getAttribute("esiste_documento");

    if (tipoAccesso.equalsIgnoreCase("L") && dominioForm != null) {
      if (rw.equalsIgnoreCase("W") && campoCalcolato.equalsIgnoreCase("S")) {
        valore = dominioForm.getValore(dato);
        if (valore.length() > lunghezzaStandard) {
          valore = valore.substring(0,lunghezzaStandard);
        }
        //Debug Tempo
        stampaTempo("Campo::caricaValore - Fine",area,modello,dato,ptime);
        //Debug Tempo
        return;
      }
      if (esiste.length() == 0 && campoCalcolato.equalsIgnoreCase("C")) {
        valore = dominioForm.getValore(dato);
        if (valore.length() > lunghezzaStandard) {
          valore = valore.substring(0,lunghezzaStandard);
        }
        //Debug Tempo
        stampaTempo("Campo::caricaValore - Fine",area,modello,dato,ptime);
        //Debug Tempo
        return;
      }
      if (campoCalcolato.equalsIgnoreCase("V")) {
        valore = dominioForm.getValore(dato);
        if (valore.length() > lunghezzaStandard) {
          valore = valore.substring(0,lunghezzaStandard);
        }
        //Debug Tempo
        stampaTempo("Campo::caricaValore - Fine",area,modello,dato,ptime);
        //Debug Tempo
        return;
      }
    }


    Dominio dp = null;
    ListaDomini ld = (ListaDomini)request.getSession().getAttribute("listaDomini");
    if (tipoAccesso.equalsIgnoreCase("L")) {
      String myVal = null;
      if (ld != null) {
        int numDom = ld.domini.size();
        int i = 0;
        while (i < numDom && myVal == null) { 
          dp = (Dominio)ld.domini.get(i);
          if (dp.isDominioFormulaModello()) {
            myVal = dp.getValore(dato);
          }
          i++;
        }
      }
      if (myVal != null) {
        if (myVal.length() != 0) {
          if (myVal.length() > lunghezzaStandard) {
            myVal = myVal.substring(0,lunghezzaStandard);
          }
        } 
      }
      if (myVal != null) {
        if (rw.equalsIgnoreCase("W") && campoCalcolato.equalsIgnoreCase("S")) {
          valore = myVal;
          //Debug Tempo
          stampaTempo("Campo::caricaValore - Fine",area,modello,dato,ptime);
          //Debug Tempo
          return;
        }
        if (esiste.length() == 0 && campoCalcolato.equalsIgnoreCase("C")) {
          valore = myVal;
          //Debug Tempo
          stampaTempo("Campo::caricaValore - Fine",area,modello,dato,ptime);
          //Debug Tempo
          return;
        }
        if (campoCalcolato.equalsIgnoreCase("V")) {
          valore = myVal;
          //Debug Tempo
          stampaTempo("Campo::caricaValore - Fine",area,modello,dato,ptime);
          //Debug Tempo
          return;
        }
      }
    }

    
    Dati dati = (Dati)request.getSession().getAttribute("gdm_nuovi_valori_doc");
    if (dati != null) {
      valore = dati.getValore(dato);

      if (valore != null) {
        //Debug Tempo
        stampaTempo("Campo::caricaValore - Fine",area,modello,dato,ptime);
        //Debug Tempo
        return; 
      }
    }
    AccediDocumento ad = (AccediDocumento)request.getSession().getAttribute("valori_doc");
    if (ad != null) {
      valore = ad.leggiValoreCampo(dato);
      if (valore != null) {
        if (valore.length() > lunghezzaStandard) {
          valore = valore.substring(0,lunghezzaStandard);
        }
        //Debug Tempo
        stampaTempo("Campo::caricaValore - Fine",area,modello,dato,ptime);
        //Debug Tempo
        return;
      }
    } 

    if (tipoCampo.charAt(0) != 'B') {            // new!  
      // E' un campo legato ad un dominio, ma non è di tipo CHECKBOX
      valore = request.getParameter(dato);
      if (valore != null && Parametri.CODIFICA_XSS.equalsIgnoreCase("S")) {
        if (tipoCampo.charAt(0) == 'H' || 
        		((tipoCampo.charAt(0) == 'S' || tipoCampo.charAt(0) == 'T') && 
        				(tipoAccesso.equalsIgnoreCase("P") || tipoAccesso.equalsIgnoreCase("L")))) {
        	String[] valori = request.getParameterValues(dato);
        	int numValori = valori.length;
        	if (numValori > 1) {
        		valore = valori[numValori-1];
        	}
        }
        if ((tipoCampo.equalsIgnoreCase("S") || tipoCampo.equalsIgnoreCase("T") || tipoCampo.equalsIgnoreCase("F")) && tipo.equalsIgnoreCase("S")) {
        	valore = Encode.forHtmlAttribute(valore);
        }
      }
      if (tipoCampo.charAt(0) == 'Z') {
        if (valore != null) {
          String valoreNew = "";
          int ic;
//          valore = URLDecoder.decode(valore,"ISO-8859-1");
          int up = 0;
          int posCod = valore.indexOf("mmacode(");
          while (posCod > -1) {
            valoreNew += valore.substring(up,posCod);
            up = valore.indexOf(")",posCod);
            String codiceCh = valore.substring(posCod+8,up);
            up = up + 1;
            ic = Integer.parseInt(codiceCh);
            valoreNew += (char)ic;
            posCod = valore.indexOf("mmacode(",up);
          }
          valoreNew += valore.substring(up);
          valore = valoreNew;
        }
      }
      if (valore != null) {
        if (valore.length() == 0) {
          valore = null;
        }
      }
    } else {
      // E' un campo legato ad un CAMPO di tipo CHECKBOX
      String ajax_campo = request.getParameter("gdm_ajax_campo");
      if ( ajax_campo != null && ajax_campo.equalsIgnoreCase(dato)) {
        valore = request.getParameter(dato); 
      } else {
        valore = "";
        for (int j=0; j<1000; j++) {  // 1000 valori al massimo.
          String s = request.getParameter(dato+"_"+Integer.toString(j));
          if (s != null){
            // Attacco sempre un separatore in fondo, in tal modo quando dovrò
            // recuperare un valore lo richiamerò sempre utilizzando il separatore
            valore = valore + s + Parametri.SEPARAVALORI;
          }
        }
      }
      if (valore.length() == 0) {
        valore = null;
      }
    }
    if (valore != null) {
      if (valore.length() > lunghezzaStandard) {
        valore = valore.substring(0,lunghezzaStandard);
      }
      //Debug Tempo
      stampaTempo("Campo::caricaValore - Fine",area,modello,dato,ptime);
      //Debug Tempo
      return;
    }

    LinkedList lValPrec = null;
    Object attributo = request.getSession().getAttribute("valori_modello_precedente");
    if (attributo != null) {
      lValPrec = (LinkedList)attributo;
      int i = 0;
      int j = lValPrec.size();
      while (( i < j) && (valore == null)) {
        ad = (AccediDocumento)lValPrec.get(i);
        valore = ad.leggiValoreCampo(dato);
        i++;
      }
    }

//    Dominio dp = null;
//    ListaDomini ld = (ListaDomini)request.getSession().getAttribute("listaDomini");
    if (ld != null) {
      int numDom = ld.domini.size();
      int i = 0;
      while (i < numDom && valore == null) { 
        dp = (Dominio)ld.domini.get(i);
        if (dp.isDominioDelModello() || dp.isDominioDiArea()) {
          valore = dp.getValore(dato);
        }
        i++;
      }
    }
    if (valore != null) {
      if (valore.length() != 0) {
        if (valore.length() > lunghezzaStandard) {
          valore = valore.substring(0,lunghezzaStandard);
        }
        //Debug Tempo
        stampaTempo("Campo::caricaValore - Fine",area,modello,dato,ptime);
        //Debug Tempo
        return;
      } else {
        valore = null;
      }
    }

    if (dominioForm != null && campoCalcolato.equalsIgnoreCase("S") && rw.equalsIgnoreCase("W")) {
      valore = dominioForm.getValore(dato);
    }
    if (valore != null) {
      if (valore.length() > lunghezzaStandard) {
        valore = valore.substring(0,lunghezzaStandard);
      }
      //Debug Tempo
      stampaTempo("Campo::caricaValore - Fine",area,modello,dato,ptime);
      //Debug Tempo
      return;
    } 

    if (campoCalcolato.equalsIgnoreCase("S") && rw.equalsIgnoreCase("W")) {
      if (ld != null) {
        int numDom = ld.domini.size();
        int i = 0;
        while (i < numDom && valore == null) { 
          dp = (Dominio)ld.domini.get(i);
          if (dp.isDominioFormulaModello()) {
            valore = dp.getValore(dato);
          }
          i++;
        }
      }
    }
    if (valore != null) {
      if (valore.length() > lunghezzaStandard) {
        valore = valore.substring(0,lunghezzaStandard);
      }
      //Debug Tempo
      stampaTempo("Campo::caricaValore - Fine",area,modello,dato,ptime);
      //Debug Tempo
      return;
    } 
    
    if (dominio == null) {
      // Dominio vuoto quindi non esiste alcun valore di default
      valore = "";
    } else {
      // Esiste un dominio associato che probabilmente mi darà
      // un valore di default per il campo nella forma CODICE <-> VALORE
      valore = dominio.getValore(dato);
    }


    if (valore == null)
      valore="";

    if (valore.length() > lunghezzaStandard) {
      valore = valore.substring(0,lunghezzaStandard);
    }
    //Debug Tempo
    stampaTempo("Campo::caricaValore - Fine",area,modello,dato,ptime);
    //Debug Tempo
  }

  /**
   *
   */
  protected void scriviValore(HttpServletRequest request) throws Exception {
    //Debug Tempo
    long ptime = stampaTempo("Campo::scriviValore - Inizio",area,modello,dato,0);
    //Debug Tempo
    String      codiceRichiesta = null;

    if (campoCalcolato.equalsIgnoreCase("V")) {
      //I campi calcolati in visualizzazione non vanno salvati su DB
      return;
    }

    String cr = request.getParameter("cr");
    if (cr == null) {
      codiceRichiesta = (String)request.getSession().getAttribute("key");
      if (codiceRichiesta == null) {
        codiceRichiesta = area+'-'+request.getSession().getId();
      } else {
        if (!codiceRichiesta.equalsIgnoreCase(area+'-'+request.getSession().getId())) {
          codiceRichiesta = area+'-'+codiceRichiesta+'-'+request.getSession().getId();
        } 
      }
    } else {
      if (!cr.equalsIgnoreCase(area+'-'+request.getSession().getId())) {
        codiceRichiesta = area+'-'+cr+'-'+request.getSession().getId();
      } else {
        codiceRichiesta = cr;
      }
    }
    

/*    try {
      dbOp = new DbOperationSQL(Parametri.ALIAS, Parametri.SPORTELLO_DSN, Parametri.USER, Parametri.PASSWD);
    } catch (Exception e) {
      Util.writeErr("Campo::caricaValore()", "Impossibile creare il dbOp.");
      return;
    }
    
    Timestamp today = dbOp.getSysdate();
    
    // In ogni caso cerco prima di cancellare valori preesistenti nel repository
    query = "DELETE FROM REPOSITORYTEMP "+
            "WHERE AREA = '" + area + "' "+
            "AND CODICE_MODELLO = '" + modello + "' " +
            "AND DATO = '" + dato + "' " +
            "AND CODICE_RICHIESTA = '" + codiceRichiesta + "'";


    try {
      dbOp.setStatement(query);
    	dbOp.execute();
      dbOp.commit();
      resultQuery = dbOp.getRstSet();
    } catch (Exception sqle) {
      Util.writeErr("Campo::scriviValore()", "Attenzione! Si è verificato un errore (Cancellazione repository): "+sqle.toString());
      sqle.printStackTrace();
    }


    // Se il valore non è significativo (NULL o stringa vuota) allora
    // evito la scrittura sul repository.
    if ((valore != null) && (valore.trim().length() != 0)) {   
      // Poi procedo inserendo nel repository i valori attualmente in memoria
      query = "INSERT INTO REPOSITORYTEMP "+
                "(CODICE_RICHIESTA,  CODICE_MODELLO,  AREA,  DATO,  DATA_SCADENZA,  DATA_INSERIMENTO) VALUES "+
                "(:CODICE_RICHIESTA, :CODICE_MODELLO, :AREA, :DATO, :DATA_SCADENZA, :DATA_INSERIMENTO)";


      try {
        dbOp.setStatement(query);
        dbOp.setParameter(":CODICE_RICHIESTA",codiceRichiesta);
        dbOp.setParameter(":CODICE_MODELLO",modello);
        dbOp.setParameter(":AREA",area);
        dbOp.setParameter(":DATO",dato);
        //dbOp.setParameter(":VALORE",valore);
        if (scadenza == null) {
          // Scadenza è null
          //dbOp.setParameter(":DATA_SCADENZA",Types.TIMESTAMP);
          dbOp.setParameter(":DATA_SCADENZA",today);
        } else {
          // Scadenza non è null
          dbOp.setParameter(":DATA_SCADENZA", scadenza);
        }
        dbOp.setParameter(":DATA_INSERIMENTO", today);
        dbOp.execute();
        dbOp.commit();
      } catch (Exception sqle) {
        Util.writeErr("Campo::scriviValore()", "Attenzione! Si è verificato un errore : "+sqle.toString());
      }


      // Update per campo CLOB
      query = "UPDATE REPOSITORYTEMP "+
              "SET VALORE = :VALORE "+
              "WHERE CODICE_RICHIESTA = :CODICE_RICHIESTA AND "+
                    "CODICE_MODELLO = :CODICE_MODELLO AND "+
                    "AREA = :AREA AND "+
                    "DATO = :DATO AND "+
                    "PROGRESSIVO = 0";
               
      
      try {
        dbOp.setStatement(query);
        dbOp.setParameter(":CODICE_RICHIESTA",codiceRichiesta);
        dbOp.setParameter(":CODICE_MODELLO",modello);
        dbOp.setParameter(":AREA",area);
        dbOp.setParameter(":DATO",dato);

        byte bValore[] = valore.getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(bValore);
      
        dbOp.setAsciiStream(":VALORE", bais, bais.available());

        dbOp.execute();
        dbOp.commit();
      } catch (Exception sqle2) {
        Util.writeErr("Campo::scriviValore()", "Attenzione! Si è verificato un errore : "+sqle2.toString());
      }
    }

    free(dbOp);*/
    //Debug Tempo
    stampaTempo("Campo::scriviValore - Fine",area,modello,dato,ptime);
    //Debug Tempo
  }

  /**
   *
   */
  public String getDato(){
    return dato;
  }

  /**
   *
   */
  public String getTipo(){
    return tipo;
  }

  /**
   *
   */
  public String getBlocco(){
    return bloccoPopup;
  }

  /**
  *
  */
 public String getCalcolato(){
   return campoCalcolato;
 }

  /**
   *
   */
  public String getValore(){
    return valore;
  }

  /**
   *
   */
  public String getTipoCampo() {
    return tipoCampo;
  }

  /**
   *
   */
  public String getTipoAccesso() {
    return tipoAccesso;
  }

  /**
   *
   */
  public boolean hasDominio() {
    if (dominio == null) {
      return false;
    } else {
      return true;
    }
  }

  /**
   *
   */
  public void release() {
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
  public void settaListFields(String l_fields) {
    list_fields = l_fields;
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

  protected long stampaTempo(String sMsg, String area, String cm, String campo, long ptime) {
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
      logger.debug("\n"+sMsg+"\n-Area:"+area+" -CodiceModello:"+cm+" -Campo:"+campo+"\n-------->Trascorso dall'inizio: "+trascorso+"\n");
    }
    if (Parametri.DEBUG.equalsIgnoreCase("2")) {
      logger.debug("\n"+sMsg+"\n-Area:"+area+" -CodiceModello:"+cm+" -Campo:"+campo+"\n----->TIME: "+adesso+"\n--------Trascorso dall'inizio: "+trascorso+"\n");
    }
    return adesso;
  }
}