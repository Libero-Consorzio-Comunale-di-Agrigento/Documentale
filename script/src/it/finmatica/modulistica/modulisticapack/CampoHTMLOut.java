package it.finmatica.modulistica.modulisticapack;
 
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
//import it.finmatica.jfc.dbUtil.IDbOperationSQL;
//import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.parametri.Parametri;
import org.apache.log4j.Logger;


/**
 * Classe CampoHTMLOut
 * 
 * @author  Adelmo Gentilini
 * @author  Antonio Plastini
 * @author  Sergio Spadaro
 * @version 1.0
 */
public class CampoHTMLOut extends Campo { 
//  private boolean isVerbose = Parametri.ISVERBOSE;
  private String preFormat, postFormat; // Tag da inserire prima e dopo la restituzione del valore effettivo del campo.
  protected String    domVisu;               
  protected Dominio   dominioVisu;
  protected HttpServletRequest sRequest;
  private  static Logger logger = Logger.getLogger(CampoHTMLOut.class);
    
  /**
   * Costruisco un campo 'trasformando' un CampoHTMLIn in un Out e 
   * impostandogli un nuovo valore.
   * @author Adelmo
   * @see modulisticapack.Campo
   **/
  public CampoHTMLOut(CampoHTMLIn c, String val, HttpServletRequest pRequest) throws Exception {
    super();
    
    // preFormat e postFormat per ora vengono impostati ai tag per il BOLD
    preFormat   = "";
    postFormat  = "";
    
    // Copio i valori attualmente in memoria del campo di Input cambiando il valore
    area               = c.area;   
    modello            = c.modello;   
    dato               = c.dato;
    istruzioni         = c.istruzioni;
    tipo               = c.tipo;
    dominio            = c.dominio;
    lunghezza          = c.lunghezza;
    decimali           = c.decimali;
    lunghezzaStandard  = c.lunghezzaStandard;  
    decimaliStandard   = c.decimaliStandard;   
    tipoCampo          = c.tipoCampo;          
    tipoAccesso        = c.tipoAccesso;
    campoCalcolato     = c.campoCalcolato;
    note               = c.note;
    istruzioniStandard = c.istruzioniStandard;
    scadenza           = c.scadenza;
    urlLocale          = c.urlLocale;
    dominioVisu        = c.dominioVisu;
    domVisu            = c.domVisu;
    stile              = c.stile;
    stileLett          = c.stileLett;
    sRequest           = pRequest;
    senzaSalva         = c.senzaSalva;
   
    String  serverScheme,serverName,serverPort;
   
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
                         
    String lettura = pRequest.getParameter("rw"); 
  	if (lettura == null) {
  		lettura = "";
  	}
     
    pdo = (String)pRequest.getSession().getAttribute("pdo");
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

    valore             = val;  // cambio il valore con quello ricevuto come parametro e scrivo il campo sul database
    scriviValore(pRequest);
  }

  /**
   * Costruisco un campo 'trasformando' un CampoHTMLIn in un Out e 
   * impostandogli un nuovo valore.
   * @author Marco Bonforte
   * @see modulisticapack.Campo
   **/
  public CampoHTMLOut(CampoHTMLIn c, String val) throws Exception {
    super();
    
    // preFormat e postFormat per ora vengono impostati ai tag per il BOLD
//    preFormat   = "<b>";
//    postFormat  = "</b>";
    preFormat   = "";
    postFormat  = "";
    
    // Copio i valori attualmente in memoria del campo di Input cambiando il valore
    area               = c.area;   
    modello            = c.modello;   
    dato               = c.dato;
    istruzioni         = c.istruzioni;
    tipo               = c.tipo;
    dominio            = c.dominio;
    lunghezza          = c.lunghezza;
    decimali           = c.decimali;
    lunghezzaStandard  = c.lunghezzaStandard;  
    decimaliStandard   = c.decimaliStandard;   
    tipoCampo          = c.tipoCampo;          
    tipoAccesso        = c.tipoAccesso;        
    campoCalcolato     = c.campoCalcolato;
    note               = c.note;
    istruzioniStandard = c.istruzioniStandard;
    scadenza           = c.scadenza;
    urlLocale          = c.urlLocale;
    dominioVisu        = c.dominioVisu;
    domVisu            = c.domVisu;
    stile              = c.stile;
    stileLett          = c.stileLett;
    valore             = val;  
    sRequest           = c.sRequest;
    senzaSalva         = c.senzaSalva;
  }


  public String getPRNValue() {
    return getPRNValue(null);
  }

  /**
   * NUOVO
   */
  public String getPRNValue(IDbOperationSQL dbOpEsterna) {
    String retval = "";
    String cd, vd, s;
    int    i;

    // Modalit� di accesso al campo. Mi serve solo a visualizzare un segnaposto in caso di errore.
    boolean readWrite = ((tipoAccesso.compareTo("P") == 0) || (tipoAccesso.compareTo("S") == 0) || (tipoAccesso.compareTo("O") == 0)  || (tipoAccesso.compareTo("A") == 0)  || (tipoAccesso.compareTo("R") == 0));
    boolean readOnly = (tipoAccesso.compareTo("L") == 0);
    boolean tipoAccessoValido = (readWrite || readOnly);
    
    if (!tipoAccessoValido) {
      //retval = "<img border='0' src='"+urlLocale+"/badacctype.gif' width='145' height='22'>";   
      retval = "<img style='border: none' src='"+this.completeContextURL_images+"/badacctype.gif' width='145' height='22' alt='Tipo di accesso sconosciuto!' />";   
      return retval;
    }
    
    if (domVisu != null) {
      String esito = dominioVisu.getValore(dato);
      if (esito == null) {
        esito = "";
      }
      if (esito.length() == 0) {
//        retval = "<input type='hidden' "+
//                        "name='" + dato + "' "+
//                        "size='" + lunghezza + "' "+
//                        "value=\"" + valore.replaceAll("\"","&quot;") + "\" />\n";
        retval = "";
        return retval;
      }
    }

    // ---------------------------------------------------------------------------------------
    // Rappresentazione campi
    // In caso di campi legati a domini cerco di rappresentarlo con lo stesso 
    // stile di INPUT ma disabilitandolo. negli altri casi lo rappresento in maniera standard.
    // ---------------------------------------------------------------------------------------
    switch (tipoCampo.charAt(0)) {
      case 'T': // TEXTBOX (standard) e TEXTAREA (non devono pescare da un dominio)
//        retval = "<textarea rows='" + decimali + "' " +
//                           "name='" + dato + "' "+ 
//                           "cols='" + lunghezza + "' "+
//                           "disabled>" + valore + "</textarea>\n";
//          retval = "<pre>"+valore+"</pre>\n" ;
        String newVal = valore.replaceAll("\n","<br/>");
        newVal = newVal.replaceAll("\t"," &nbsp;&nbsp;&nbsp;");
        newVal = newVal.replaceAll("  "," &nbsp;");
        newVal = newVal.replaceAll(">","&gt;");
        newVal = newVal.replaceAll("<","&lt;");
        retval = "<span "+stileLett+">"+newVal+"</span>";
        break;

     case 'Z': 
      retval = "<span "+stileLett+">"+valore+"</span>\n" ;
      break;
        
      case 'F':
        retval = preFormat + "<span "+stileLett+">"+valore+"</span>" + postFormat;
        break;
        
     case 'S':
        retval = preFormat + "<span "+stileLett+">"+valore+"</span>" + postFormat;
        break;
        
     case 'H':
        retval = "";
        break;
        
      case 'R': // RADIOBUTTON : come gestisco i controlli ??
        
        if (dominio != null) {

          for (i = 0; i < dominio.getNumeroValori(); i++) { 
            if ((decimali > 0) && ((i % decimali) == 0) && (i > 0)) {
              // In base al numero di colonne che desidero forzo una nuova riga 
              retval = retval + "<br/>\n";
            }
         
            cd = dominio.getCodice(i);
            vd = dominio.getValore(i);
           
            if (cd.equals(valore)) {            
              retval = retval + "<img style='border: none' src='"+this.completeContextURL_images+"/rb_check.gif' width='18' height='18' alt='Selezionato' /> \n"+ "<span "+stileLett+">"+vd+"</span>";
            } else {
               retval = retval + "<img style='border: none' src='"+this.completeContextURL_images+"/rb_nocheck.gif' width='18' height='18' alt='Non selezionato' /> \n"+ "<span "+stileLett+">"+vd+"</span>";
            }
            
            //  retval += preFormat +vd+ postFormat;  
                       
          }
        }
      break;

      // -----------------------------------------------------------------------------
      case 'B':   // CHECKBOX
//        if (dominio != null) {
//          int numeroValori = dominio.getNumeroValori();
//          int contaTrovati = 0;
//          
//          for (i=0; i<numeroValori; i++) { 
//            cd = dominio.getCodice(i);
//            vd = dominio.getValore(i);
//
//            // ------------------------------------------------------------
//            // Controllo se il valore di dominio in esame � presente
//            // all'interno della stringa che rappresenta i valori del campo 
//            // (un campo checkbox pu� assumere pi� valori).
//            // ------------------------------------------------------------
//            StringTokenizer st = new StringTokenizer(valore, Parametri.SEPARAVALORI);
//            boolean trovato = false;
//            String nextToken = "";
//            while ((st.hasMoreTokens()) && (!trovato)) {
//              nextToken = st.nextToken();
//              if (nextToken.compareTo(cd) == 0) trovato = true;
//            }
//
//            if (trovato) {
//              contaTrovati++;
//              if (contaTrovati > 1)
//                retval = retval + ", ";
//              retval += preFormat +vd+ postFormat;
//            } 
//          }
//        }
//        break;

        if (dominio != null) {
          for (i = 0; i < dominio.getNumeroValori(); i++) { 
            if ((decimali > 0) && ((i % decimali) == 0) && (i > 0)) {
              // In base al numero di colonne che desidero forzo una nuova riga 
              retval = retval + "<br/>\n";
            }
            cd = dominio.getCodice(i);
            vd = dominio.getValore(i);

            // ------------------------------------------------------------
            // Controllo se il valore di dominio in esame � presente
            // all'interno della stringa che rappresenta i valori del campo 
            // (un campo checkbox pu� assumere pi� valori).
            // ------------------------------------------------------------
            StringTokenizer st = new StringTokenizer(valore, Parametri.SEPARAVALORI);
            boolean trovato = false;
            String nextToken = "";
            while ((st.hasMoreTokens()) && (!trovato)) {
              nextToken = st.nextToken();
              if (nextToken.compareTo(cd) == 0) trovato = true;
            }

            if (trovato) {
              // ESEMPIO <input type="checkbox" name="C1" value="ON" checked>uno<br>
              retval = retval + "<img style='border: none' src='"+this.completeContextURL_images+"/cb_check.gif' width='18' height='18' alt='Selezionato' />\n"+ "<span "+stileLett+">"+vd+"</span>";
            } else {
              // ESEMPIO <input type="checkbox" name="C2" value="ON">due</p>
              retval = retval + "<img style='border: none' src='"+this.completeContextURL_images+"/cb_nocheck.gif' width='18' height='18' alt='Non selezionato' />\n"+ "<span "+stileLett+">"+vd+"</span>";
            }
          }
        }
        break;


      // ----------------------------------------------------------------------------------------
      // ComboBox: visualizzo comunque un campo preselezionato e non modificabile.
      // Attenzione: La costruizone del campo contempla anche il caso in cui esistano pi� valori
      // associati al campo in questione. Attualmente per� i campi di tipo ComboBox non prevedono
      // la multiselezione.
      // ----------------------------------------------------------------------------------------
      
      case 'C':  // COMBOBOX
        String domErrore = "";
        if (dominio != null) {
          // Occorre fare un loop per selezionare la descrizione corrispondente al valore scelto.
                    
          if (valore == null) {
            s = "";
          } else {
            s = valore;
          }
          String myvalore = "";
          String mycodice = "";
          String mystile = "";
          myvalore = dominio.getValore(s);
          mycodice = dominio.getCodice(myvalore);
          if (myvalore == null) {
            logger.error("Errore in decodifica campo: '"+dato+"' area: '"+area+"' codice modello: '"+modello+"'");
            myvalore = "";
            mycodice = s;
            if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
              domErrore = "<img style='border: none; cursor: hand;' src='../common/images/gdm/1.gif' alt='Errore in decodifica campo' );' />&nbsp;\n";
            } else {
              domErrore = "<img style='border: none; cursor: hand;' src='images/gdm/1.gif' );' />&nbsp;\n";
            }
          }
          if (myvalore == null) {
            myvalore = "";
          }
          if (mycodice == null) {
            mycodice = "";
          }
          int j = mycodice.indexOf("style=");
          if (j > -1) {
            mystile = mycodice.substring(j)+"'";
            retval = "<span "+stileLett+"><span "+mystile+">"+myvalore+"</span></span>"+domErrore;
          } else {
            retval += preFormat +"<span "+stileLett+">"+myvalore+"</span>"+ postFormat+domErrore;
          }

//          retval += preFormat +"<FONT "+stile+">"+dominio.getValore(s)+"</FONT>"+ postFormat;
        }
        break;

      default: 
        // Quando il tipo di campo non � stato riconosciuto, lo segnalo con un segnaposto in rosso.
        //retval += "<img border='0' src='"+urlLocale+"/badtype.gif' width='145' height='22'>";      
        retval += "<img style='border: none' src='"+this.completeContextURL_images+"/badtype.gif' width='145' height='22' alt='Tipo di campo sconosciuto' />\n";      
        break;
    }

    String    sEditor = (String)sRequest.getSession().getAttribute("ed");
    if (sEditor == null) {
      sEditor = "";
    }
    if (!sEditor.equalsIgnoreCase("Y")) {
      if (valore != null ) {
        retval += "\n<input type='hidden' "+
                        "name='" + dato + "' "+
                        "size='" + lunghezza + "' "+
                        "value=\"" + valore.replaceAll("\"","&quot;") + "\" />\n";
      }
    }

    return retval;
  }

  
  /**
   * 
   */
  public String getZValue() {
    return null;
  }


  public String getValue() {
    return getValue(null);
  }

  /**
   * 
   */
  public String getValue(IDbOperationSQL dbOpEsterna) {

    String retval = "";
    String cd, vd, s;
    int    i;

    // Modalit� di accesso al campo. Mi serve solo a visualizzare un segnaposto in caso di errore.
    boolean readWrite = ((tipoAccesso.compareTo("P") == 0) || (tipoAccesso.compareTo("S") == 0) || (tipoAccesso.compareTo("O") == 0)  || (tipoAccesso.compareTo("A") == 0)  || (tipoAccesso.compareTo("R") == 0));
    boolean readOnly = (tipoAccesso.compareTo("L") == 0);
    boolean tipoAccessoValido = (readWrite || readOnly);
    
    if (!tipoAccessoValido) {
      retval = "<img style='border: none' src='"+this.completeContextURL_images+"/badacctype.gif' width='145' height='22' alt='Tipo di accesso sconosciuto!' />";   
      return retval;
    }
    
    if (domVisu != null) {
      String esito = dominioVisu.getValore(dato);
      if (esito == null) {
        esito = "";
      }
      if (esito.length() == 0) {
        retval = "<input type='hidden' "+
        				"id='_C_" + dato + "' "+
                        "name='" + dato + "' "+
                        "size='" + lunghezza + "' "+
                        "value=\"" + valore.replaceAll("\"","&quot;") + "\" />\n";
        return retval;
      }
    }

    // ---------------------------------------------------------------------------------------
    // Rappresentazione campi
    // In caso di campi legati a domini cerco di rappresentarlo con lo stesso 
    // stile di INPUT ma disabilitandolo. negli altri casi lo rappresento in maniera standard.
    // ---------------------------------------------------------------------------------------
    switch (tipoCampo.charAt(0)) {
      case 'T': // TEXTBOX (standard) e TEXTAREA (non devono pescare da un dominio)
      	String newVal = valore.replaceAll(">","&gt;");
        newVal = newVal.replaceAll("<","&lt;");
        newVal = newVal.replaceAll("\n","<br/>");
        newVal = newVal.replaceAll("\t"," &nbsp;&nbsp;&nbsp;");
        newVal = newVal.replaceAll("  "," &nbsp;");
        retval = "<span "+stileLett+">"+newVal+"</span>";
        break;
  
      case 'Z': 
        if (valore != null) {
          String valoreNew = "";
          int ic;
//          try {
//          valore = URLDecoder.decode(valore,"ISO-8859-1");
//          } catch (Exception e ) {
//            retval = "Errore in decodifica";
//            break;
//          }
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
        retval = "<span "+stileLett+">"+valore+"</span>";
        break;
        
      case 'F':
        retval = preFormat + "<span "+stileLett+">"+valore+"</span>" + postFormat;
        break;
        
      case 'S':
        retval = preFormat + "<span "+stileLett+">"+valore+"</span>" + postFormat;
        break;
        
     case 'H':
        retval = "";
        break;
        
      case 'R': // RADIOBUTTON : come gestisco i controlli ??
        if (dominio != null) {
          for (i = 0; i < dominio.getNumeroValori(); i++) {
            if ((decimali > 0) && ((i % decimali) == 0) && (i > 0)) {
              // In base al numero di colonne che desidero forzo una nuova riga 
              retval = retval + "<br/>\n";
            }
            cd = dominio.getCodice(i);
            vd = dominio.getValore(i);
            // NB: il valore del campo in realt� e' il suo codice, sebbene l'utente 
            // scelga in base alla descrizione
            if (cd.equals(valore)) {
              // ESEMPIO <input type="radio" name="R1" value="V2" checked>B
              // retval = retval + "<input type=\"radio\" name=\"" + dato + "\" value=\"" + cd + "\"checked>" + vd;
              //retval = retval + "<img border=\"0\"src=\""+urlLocale+"/rb_check.gif\" width=\"18\" height=\"18\"> "+ vd;
              retval = retval + "<img style='border: none' src='"+this.completeContextURL_images+"/rb_check.gif' width='18' height='18' alt='Selezionato' /> \n"+ "<span "+stileLett+">"+vd+"</span>";
            } else {
              // ESEMPIO <input type="radio" value="V1" name="R1">A
              //retval = retval + "<input type=\"radio\" value=\"" + cd + "\" name=\"" + dato + "\">" + vd;
              //retval = retval + "<img border=\"0\" src=\""+urlLocale+"/rb_nocheck.gif\" width=\"18\" height=\"18\"> "+ vd;
              retval = retval + "<img style='border: none' src='"+this.completeContextURL_images+"/rb_nocheck.gif' width='18' height='18' alt='Non selezionato' /> \n"+ "<span "+stileLett+">"+vd+"</span>";
            }
          }
        }
      break;

      // -----------------------------------------------------------------------------
      case 'B':   // CHECKBOX
        if (dominio != null) {
          for (i = 0; i < dominio.getNumeroValori(); i++) { 
            if ((decimali > 0) && ((i % decimali) == 0) && (i > 0)) {
              // In base al numero di colonne che desidero forzo una nuova riga 
              retval = retval + "<br/>\n";
            }
            cd = dominio.getCodice(i);
            vd = dominio.getValore(i);

            // ------------------------------------------------------------
            // Controllo se il valore di dominio in esame � presente
            // all'interno della stringa che rappresenta i valori del campo 
            // (un campo checkbox pu� assumere pi� valori).
            // ------------------------------------------------------------
            StringTokenizer st = new StringTokenizer(valore, Parametri.SEPARAVALORI);
            boolean trovato = false;
            String nextToken = "";
            while ((st.hasMoreTokens()) && (!trovato)) {
              nextToken = st.nextToken();
              if (nextToken.compareTo(cd) == 0) trovato = true;
            }

            if (trovato) {
              // ESEMPIO <input type="checkbox" name="C1" value="ON" checked>uno<br>
              //retval = retval + "<input type=\"checkbox\" name=\"" + dato + "_" + Integer.toString(i) + "\" DISABLED value=\"" + cd + "\" checked>" + vd;
              //retval = retval + "<img border=\"0\" src=\""+urlLocale+"/cb_check.gif\" width=\"18\" height=\"18\"> "+ vd;
              retval = retval + "<img style='border: none' src='"+this.completeContextURL_images+"/cb_check.gif' width='18' height='18' alt='Selezionato' /> \n"+ "<span "+stileLett+">"+vd+"</span>";
            } else {
              // ESEMPIO <input type="checkbox" name="C2" value="ON">due</p>
              //retval = retval + "<input type=\"checkbox\" name=\"" + dato + "_" + Integer.toString(i) + "\" DISABLED value=\"" + cd + "\" >" + vd;
              //retval = retval + "<img border=\"0\" src=\""+urlLocale+"/cb_nocheck.gif\" width=\"18\" height=\"18\"> "+ vd;
              retval = retval + "<img style='border: none' src='"+this.completeContextURL_images+"/cb_nocheck.gif' width='18' height='18' alt='Non selezionato' /> \n"+ "<span "+stileLett+">"+vd+"</span>";
            }
          }
        }
        break;

      // ----------------------------------------------------------------------------------------
      // ComboBox: visualizzo comunque un campo preselezionato e non modificabile.
      // Attenzione: La costruizone del campo contempla anche il caso in cui esistano pi� valori
      // associati al campo in questione. Attualmente per� i campi di tipo ComboBox non prevedono
      // la multiselezione.
      // ----------------------------------------------------------------------------------------
      
      case 'C':  // COMBOBOX
        if (dominio != null) {
          // Occorre fare un loop per selezionare la descrizione corrispondente al valore scelto.
//          retval = preFormat;
          if (valore == null) {
            s = "";
          } else {
            s = valore;
          }
          String myvalore = dominio.getValore(s);
          String mycodice = dominio.getCodice(myvalore);
          if (myvalore == null) {
            myvalore = "";
          }
          String mystile = "";
          int j = mycodice.indexOf("style=");
          if (j > -1) {
            mystile = mycodice.substring(j)+"'";
            retval = "<span "+stileLett+"><span "+mystile+">"+myvalore+"</span></span>";
          } else {
            retval += preFormat +"<span "+stileLett+">"+myvalore+"</span>"+ postFormat;
          }
        } 
        break;

      default: 
        // Quando il tipo di campo non � stato riconosciuto, lo segnalo con un segnaposto in rosso.
        //retval += "<img border='0' src='"+urlLocale+"/badtype.gif' width='145' height='22'>";      
        retval += "<img style='border: none' src='"+this.completeContextURL_images+"/badtype.gif' width='145' height='22'  alt='Tipo di campo sconosciuto' />\n";      
        break;
    }

    String    sEditor = (String)sRequest.getSession().getAttribute("ed");
    if (sEditor == null) {
      sEditor = "";
    }

    if (!sEditor.equalsIgnoreCase("Y")) {
      if (valore != null ) {
        retval += "\n<input type='hidden' "+
        				"id='_C_" + dato + "' "+
                        "name='" + dato + "' "+
                        "size='" + lunghezza + "' "+
                        "value=\"" + valore.replaceAll("\"","&quot;") + "\" />\n";
      }
    }

    return retval;
  }

  public String getPRNComValue() {
    return getPRNComValue(null);
  }

  /**
   * NUOVO
   */
  public String getPRNComValue(IDbOperationSQL dbOpEsterna) {
    String retval = "";
    String cd, vd, s;
    int    i;
//    String rigo= "";

    // Modalit� di accesso al campo. Mi serve solo a visualizzare un segnaposto in caso di errore.
    boolean readWrite = ((tipoAccesso.compareTo("P") == 0) || (tipoAccesso.compareTo("S") == 0) || (tipoAccesso.compareTo("O") == 0)  || (tipoAccesso.compareTo("A") == 0)  || (tipoAccesso.compareTo("R") == 0));
    boolean readOnly = (tipoAccesso.compareTo("L") == 0);
    boolean tipoAccessoValido = (readWrite || readOnly);
    
    if (!tipoAccessoValido) {
      //retval = "<img border='0' src='"+urlLocale+"/badacctype.gif' width='145' height='22'>";   
      retval = "<img style='border: none' src='"+this.completeContextURL_images+"/badacctype.gif' width='145' height='22'  alt='Tipo di accesso sconosciuto' />";   
      return retval;
    }
    
    if (domVisu != null) {
      String esito = dominioVisu.getValore(dato);
      if (esito == null) {
        esito = "";
      }
      if (esito.length() == 0) {
        retval = "<input type='hidden' "+
        				"id='_C_" + dato + "' "+
                        "name='" + dato + "' "+
                        "size='" + lunghezza + "' "+
                        "value=\"" + valore.replaceAll("\"","&quot;") + "\" />\n";
        return retval;
      }
    }

    // ---------------------------------------------------------------------------------------
    // Rappresentazione campi
    // In caso di campi legati a domini cerco di rappresentarlo con lo stesso 
    // stile di INPUT ma disabilitandolo. negli altri casi lo rappresento in maniera standard.
    // ---------------------------------------------------------------------------------------
    switch (tipoCampo.charAt(0)) {
      case 'T': // TEXTBOX (standard) e TEXTAREA (non devono pescare da un dominio)
        if (valore == null || valore.equals("") ) {
          for (i=0; i < decimali; i++) {
            retval = retval + "<hr>";
          }
        } else {
//          retval = "<textarea rows='" + decimali + "' " +
//                             "name='" + dato + "' "+ 
//                             "cols='" + lunghezza + "' "+
//                             "disabled>" + valore + "</textarea>\n";
//          retval = "<pre>"+valore+"</pre>\n" ;
          String newVal = valore.replaceAll("\n","<br/>");
          newVal = newVal.replaceAll("\t"," &nbsp;&nbsp;&nbsp;");
          newVal = newVal.replaceAll("  "," &nbsp;");
          newVal = newVal.replaceAll(">","&gt;");
          newVal = newVal.replaceAll("<","&lt;");
          retval = "<span "+stileLett+">"+newVal+"</span>";
        }
        break;
        
     case 'Z': 
        if (valore == null || valore.equals("") ) {
          for (i=0; i <= decimali; i++) {
            retval = retval + "<hr/>\n";
          }
        } else {
          retval = "<span "+stileLett+">"+valore+"</span>\n" ;
        }
      break;
        
      case 'F':
        retval = preFormat + "<span "+stileLett+">"+valore+"</span>" + postFormat;
        break;
        
     case 'S':
        if (valore == null || valore.equals("") ) {
          for (i=0; i <= lunghezza; i++) {
            retval = retval + "_";
          }
        } else {
          retval = preFormat + "<span "+stileLett+">"+valore+"</span>" + postFormat;
        }
        break;
        
     case 'H':
        retval = "";
        break;
        
      case 'R': // RADIOBUTTON : come gestisco i controlli ??
        
        if (dominio != null) {
          for (i = 0; i < dominio.getNumeroValori(); i++) {
            cd = dominio.getCodice(i);
            vd = dominio.getValore(i);
            
            if (valore == null || valore.equals("")) {
              retval += preFormat +" _ "+"<span "+stileLett+">"+vd+"</span>"+ postFormat;
            } else {
              if (cd.equals(valore)) {
                retval += preFormat +"<span "+stileLett+">"+vd+"</span>"+ postFormat;
              }
            }
          }
        }
      break;

      // -----------------------------------------------------------------------------
      case 'B':   // CHECKBOX
        if (dominio != null) {
          int numeroValori = dominio.getNumeroValori();
          int contaTrovati = 0;
          
          for (i=0; i<numeroValori; i++) { 
            cd = dominio.getCodice(i);
            vd = dominio.getValore(i);

            
            if (valore == null || valore.equals("")) {
              if ((decimali > 0) && ((i % decimali) == 0) && (i > 0)) {
                // In base al numero di colonne che desidero forzo una nuova riga 
                retval = retval + "<br/>\n";
              }
              retval += preFormat +" _ "+vd+ postFormat;
            } else {
              // ------------------------------------------------------------
              // Controllo se il valore di dominio in esame � presente
              // all'interno della stringa che rappresenta i valori del campo 
              // (un campo checkbox pu� assumere pi� valori).
              // ------------------------------------------------------------
              StringTokenizer st = new StringTokenizer(valore, Parametri.SEPARAVALORI);
              boolean trovato = false;
              String nextToken = "";
              while ((st.hasMoreTokens()) && (!trovato)) {
                nextToken = st.nextToken();
                if (nextToken.compareTo(cd) == 0) trovato = true;
              }

              if (trovato) {
                contaTrovati++;
                if (contaTrovati > 1)
                  retval = retval + ", ";
                retval += preFormat +"<span "+stileLett+">"+vd+"</span>"+ postFormat;
              } 
            }
          }
        }
        break;

      // ----------------------------------------------------------------------------------------
      // ComboBox: visualizzo comunque un campo preselezionato e non modificabile.
      // Attenzione: La costruizone del campo contempla anche il caso in cui esistano pi� valori
      // associati al campo in questione. Attualmente per� i campi di tipo ComboBox non prevedono
      // la multiselezione.
      // ----------------------------------------------------------------------------------------
      
      case 'C':  // COMBOBOX
        if (valore == null || valore.equals("")) {
          s = "";
          for (i=0; i < lunghezza; i++) {
            s = s + "_";
          }
        } else {
          s = dominio.getValore(valore);
        }
          if (s == null) {
            s = "";
          }
        retval += preFormat +"<span "+stileLett+">"+s+"</span>"+ postFormat;
        break;

      default: 
        // Quando il tipo di campo non � stato riconosciuto, lo segnalo con un segnaposto in rosso.
        //retval += "<img border='0' src='"+urlLocale+"/badtype.gif' width='145' height='22'>";      
        retval += "<img style='border: none' src='"+this.completeContextURL_images+"/badtype.gif' width='145' height='22' alt='Tipo di campo sconosciuto' />\n";      
        break;
    }

    String    sEditor = (String)sRequest.getSession().getAttribute("ed");
    if (sEditor == null) {
      sEditor = "";
    }
    if (!sEditor.equalsIgnoreCase("Y")) {
      if (valore != null ) {
        retval += "\n<input type='hidden' "+
        				"id='_C_" + dato + "' "+
                        "name='" + dato + "' "+
                        "size='" + lunghezza + "' "+
                        "value=\"" + valore.replaceAll("\"","&quot;") + "\" />\n";
      }
    }
    return retval;
  }

  public void settaListFields(String l_fields){
  }

  public void settaProtetto(boolean b_protetto) {
    // TODO Auto-generated method stub
    
  }
  
  /**
   * 
   */
/*  public String getValueSS() {
    //Debug Tempo
    long ptime = stampaTempo("CampoHTMLIn::getValue - Inizio",area,modello,dato,0);
    //Debug Tempo
    String    retval = ""; 
    String    cd; 
    String    vd;
    String    sControlli = "";
    String    sErrore = "";
    String    sSeverity;
    String    aggiorna = "";
    String    ancor = "";
//    String    protetto = "";
    int       i;
    int       npos;
    int       posCampo;
    int       posSeverity;
    int       endSeverity;
    boolean   readWrite = ((tipoAccesso.compareTo("P") == 0) || (tipoAccesso.compareTo("S") == 0) || (tipoAccesso.compareTo("O") == 0)  || (tipoAccesso.compareTo("A") == 0)  || (tipoAccesso.compareTo("R") == 0));
    boolean   readOnly = (tipoAccesso.compareTo("L") == 0);
    boolean   tipoAccessoValido = (readWrite || readOnly);

    
    
    if (!tipoAccessoValido) {
      //retval = "<img border='0' src='"+urlLocale+"/badacctype.gif' width='145' height='22'>";   
      retval = "<img style='border: none' src='"+this.completeContextURL_images+"badacctype.gif' width='145' height='22' alt='Tipo di accesso sconosciuto!' />";   
      //Debug Tempo
      stampaTempo("CampoHTMLIn::getValue - Fine",area,modello,dato,ptime);
      //Debug Tempo
      return retval;
    }
    posCampo = list_fields.indexOf(dato+";");
    if (posCampo > 0) {
      posCampo = list_fields.indexOf(";"+dato+";") + 1;
      if (posCampo == 0) {
        posCampo = -1;
      }
    }
    if (posCampo > -1) {
      posSeverity = posCampo + dato.length() + 1;
      endSeverity = list_fields.indexOf(";",posSeverity);
      if (endSeverity == -1) {
        endSeverity = list_fields.length();
      }
      sSeverity = list_fields.substring(posSeverity,endSeverity);
      sErrore = "<img style='border: none' src='"+this.completeContextURL_images+Parametri.getParametriErrore(sSeverity)+"' alt='Errore' />";   
    } else {
      sErrore = "";
    }

    if (tipoAccesso.equalsIgnoreCase("A") || tipoAccesso.equalsIgnoreCase("R")) {
      ancor = "\n<a name=\"#ancor_"+dato+"\"></a>";
      aggiorna = " addHiddenInputAncor(document.getElementById(\"submitForm\"),\"ancor_"+dato+"\");";
    }

//    if (tipoAccesso.equalsIgnoreCase("P")) {
//      protetto = " onfocus=\"alert('Attenzione! Campo protetto.');\" ";
//    }

    if (domVisu != null) {
      String esito = dominioVisu.getValore(dato);
      if (esito == null) {
        esito = "";
      }
      if (esito.length() == 0) {
        retval = "<input type='hidden' "+
                        "name='" + dato + "' "+
                        "size='" + lunghezza + "' "+
                        "value=\"" + valore.replaceAll("\"","&quot;") + "\" ";
        retval += formattaControlli()+"/>\n";
        //Debug Tempo
        stampaTempo("CampoHTMLIn::getValue - Fine",area,modello,dato,ptime);
        //Debug Tempo
        return retval;
      }
    }
    // In base al tipo di campo visualizzo il campo HTML corrispondente.
    // I campi di tipo COMBOBOX (C), RADIOBUTTON (R) e CHECKBOX (B) possono fare riferimento
    // ad un dominio. In questo caso viene fatto il loop su tutti i valori del dominio e
    // viene selezionato il default letto dal repository.
    // Se non sono di un tipo legato ad un dominio suppongo che siano 
    // un campo di tipo editing che puo' essere  TEXTAREA (T) oppure
    // un campo di tipo TEXTBOX (standard) e quindi inserisco un editing di testo.
    if (lettura.equalsIgnoreCase("Q")) {
      formato = "dd/mm/yyyy";
    }
    String opRic = opzioniRicerca(dato);
    String valHidden = "";
    switch (tipoCampo.charAt(0)) {
      case 'H':  // HIDDEN (nascosto) 
        retval = "\n<input type='hidden' "+
                        "name='" + dato + "' "+
                        "size='" + lunghezza + "' "+
                        "value=\"" + valore.replaceAll("\"","&quot;") + "\" ";
        retval += formattaControlli()+"/>\n";
        break;
      case 'F':  // Filtro
        retval = ancor+"\n<input type='text' "+
                        "class='"+classname+"' " +
                        "name='" + dato + "' "+
                        "size='" + lunghezza + "' "+
                        "maxlength='" + lunghezzaStandard + "' "+
                        "value=\"" + valore.replaceAll("\"","&quot;") + "\" " +
                        stile +" />\n";
        if (formattaControlli().length() == 0) {
          if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
            retval += "<img style='border: none; cursor: hand;' src='../common/images/gdm/funnel.gif' alt='Filtro valori' onclick='addHiddenInput(document.getElementById(\"submitForm\"));' />&nbsp;\n";
          } else {
            retval += "<img style='border: none; cursor: hand;' src='images/gdm/funnel.gif' alt='Filtro valori' onclick='addHiddenInput(document.getElementById(\"submitForm\"));' />&nbsp;\n";
          }
        } else {
          if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
            retval += "<img style='border: none; cursor: hand;' src='../common/images/gdm/funnel.gif' alt='Filtro valori' "+formattaControlli().replaceFirst("onchange","onclick")+" />&nbsp;\n";
          } else {
            retval += "<img style='border: none; cursor: hand;' src='images/gdm/funnel.gif' alt='Filtro valori' "+formattaControlli().replaceFirst("onchange","onclick")+" />&nbsp;\n";
          }
        }
        break;
      case 'S':  // TEXTBOX (standard) (non pu� pescare da un dominio)
        if (readOnly) {
          retval = "<span "+stileLett+">"+valore+"</span>";
          retval += "\n<input type='hidden' "+
                          "name='" + dato + "' "+
                          "size='" + lunghezza + "' "+
                          "value=\"" + valore.replaceAll("\"","&quot;") + "\" ";
//          retval = "<pre>"+valore+"</pre>\n" ;
        } else {
          retval = ancor+"\n<input type='text' "+
                          "class='"+classname+"' " +
                          "name='" + dato + "' "+
                          "size='" + lunghezza + "' "+
                          "maxlength='" + lunghezzaStandard + "' "+
                          "value=\"" + valore.replaceAll("\"","&quot;") + "\" " +
                          stile+ " ";
          if (tipoAccesso.equalsIgnoreCase("P")) {
             retval += "READONLY ";
          }
          sControlli = formattaControlli();
          npos = sControlli.indexOf("onblur");
          if ( npos >= 0) {
            retval += sControlli.substring(0,npos+8);
            if (tipo.equals("D")) {
               retval += "data(document.getElementById(\"submitForm\")."+dato+",\""+formato+"\");"+aggiorna;
            }
            if (tipo.equals("N")) {
               retval += "numero(document.getElementById(\"submitForm\")."+dato+","+lunghezzaStandard+","+decimaliStandard+");"+aggiorna;
            }
            if (tipo.equals("S")) {
               retval += "lunghezza(document.getElementById(\"submitForm\")."+dato+","+lunghezzaStandard+");"+aggiorna;
            }
            retval += sControlli.substring(npos+8);
            sControlli = sControlli.substring(0,npos);
          } else {
            retval += " onblur='";
            if (tipo.equals("D")) {
               retval += "data(document.getElementById(\"submitForm\")."+dato+",\""+formato+"\");"+aggiorna+"'" ;
            }
            if (tipo.equals("N")) {
               retval += "numero(document.getElementById(\"submitForm\")."+dato+","+lunghezzaStandard+","+decimaliStandard+");"+aggiorna+"'" ;
            }
            if (tipo.equals("S")) {
               retval += "lunghezza(document.getElementById(\"submitForm\")."+dato+","+lunghezzaStandard+");"+aggiorna+"'" ;
            }
          }
        }
        retval += sControlli+"/>\n";
        if (tipo.equalsIgnoreCase("D") && readWrite && !tipoAccesso.equalsIgnoreCase("P")) {
          retval += "<script type='text/javascript' >var submitForm_DatePicker_"+dato+" = new Object(); ";
          retval += "submitForm_DatePicker_"+dato+".format = '"+formato.replaceAll("mi", "nn")+"';";
          retval += "submitForm_DatePicker_"+dato+".style = 'Themes/AFC/Style.css'; ";
          if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
            retval += "submitForm_DatePicker_"+dato+".relativePathPart = '../'; </script>";
          } else {
            retval += "submitForm_DatePicker_"+dato+".relativePathPart = './'; </script>";
          }

          retval += "&nbsp;<a class='AFCDataLink' href='javascript:showDatePicker(\"submitForm_DatePicker_"+dato+"\",\"submitForm\",\""+dato+"\");' >";
          if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
            retval += "<img style='border: none' src='../Themes/DatePicker/DatePicker1.gif' alt='DatePicker' /></a>&nbsp;\n";
          } else {
            retval += "<img style='border: none' src='Themes/DatePicker/DatePicker1.gif' alt='DatePicker' /></a>&nbsp;\n";
          }
        }
        retval += sErrore;
        if (lettura.equalsIgnoreCase("Q") && between) {
//          retval += "<span id='_2_"+dato+"'  style='display:none'><input type='text' "+
          retval += "<span id='_2_"+dato+"' > al <input type='text' "+
            "class='"+classname+"'  " +
            "name='_2_" + dato + "' "+
            "size='" + lunghezza + "' "+
            "maxlength='" + lunghezzaStandard + "' "+
            "value=\"" + valore_2.replaceAll("\"","&quot;") + "\" " +
            stile+ " ";
          retval += " onblur='";
          if (tipo.equals("D")) {
            retval += "data(document.getElementById(\"submitForm\")._2_"+dato+",\""+formato+"\");'" ;
          }
          if (tipo.equals("N")) {
            retval += "numero(document.getElementById(\"submitForm\")._2_"+dato+","+lunghezzaStandard+","+decimaliStandard+");'" ;
          }
          if (tipo.equals("S")) {
            retval += "lunghezza(document.getElementById(\"submitForm\")._2_"+dato+","+lunghezzaStandard+");'" ;
          }
          retval += "/>\n";
          if (tipo.equalsIgnoreCase("D") && readWrite && !tipoAccesso.equalsIgnoreCase("P")) {
            retval += "<script type='text/javascript' >var submitForm_DatePicker__2_"+dato+" = new Object(); ";
            retval += "submitForm_DatePicker__2_"+dato+".format = '"+formato.replaceAll("mi", "nn")+"';";
            retval += "submitForm_DatePicker__2_"+dato+".style = 'Themes/AFC/Style.css'; ";
            if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
              retval += "submitForm_DatePicker__2_"+dato+".relativePathPart = '../'; </script>";
            } else {
              retval += "submitForm_DatePicker__2_"+dato+".relativePathPart = './'; </script>";
            }

            retval += "&nbsp;<a class='AFCDataLink' href='javascript:showDatePicker(\"submitForm_DatePicker__2_"+dato+"\",\"submitForm\",\"_2_"+dato+"\");' >";
            if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
              retval += "<img style='border: none' src='../Themes/DatePicker/DatePicker1.gif' alt='DatePicker' /></a>&nbsp;\n";
            } else {
              retval += "<img style='border: none' src='Themes/DatePicker/DatePicker1.gif' alt='DatePicker' /></a>&nbsp;\n";
            }
          }
          retval += "</span>\n";
        }
        break;
      
      case 'Z': // RICHTextArea
        if (readOnly || tipoAccesso.equalsIgnoreCase("P")) {
          retval = "<span "+stileLett+">"+valore+"</span>";

          retval += "\n<input type='hidden' name='" + dato + "' value=\"" + valore.replaceAll("\"","&quot;") + "\"/>\n"; 
        } else {
          retval = ancor+"\n<textarea rows='" + decimali + "' " +
                           "id='_ta_" + dato + "' name='_ta_" + dato + "' "+ 
                           "cols='" + lunghezza + "' " + stile + " " + formattaControlli() + " onblur='lunghezza(this,"+lunghezzaStandard+"); "+aggiorna+"' ></textarea>\n"+sErrore;
          retval += "\n<input type='hidden' name='" + dato + "' value=\"" + valore.replaceAll("\"","&quot;") +  "\" "+ stile +"/>\n"; 
          retval +=
            "\n<script>"+
            "\n   var oEdit1 = new InnovaEditor('oEdit1');"+
            "\n   oEdit1.width=\"100%\";"+
            "\n   oEdit1.css='../Themes/AFC/Style.css';"+
            "\n   oEdit1.mode='XHTMLBody';"+
            "\n   oEdit1.useBR=false;"+
            "\n   oEdit1.publishingPath='';"+
            "\n   oEdit1.features=['Paragraph','FontName','FontSize','|','Cut','Copy',"+
            "\n     'Paste','PasteText','PasteWord','|','Undo','Redo','|','Bold','Italic','Underline',"+
            "\n     'Strikethrough','Superscript','Subscript','|','BRK','JustifyLeft','JustifyCenter',"+
            "\n     'JustifyRight','JustifyFull','|','Numbering','Bullets','Indent','Outdent','|',"+
            "\n     'ForeColor','BackColor','|','Hyperlink','|','ClearAll','|','Table','Border',"+
            "\n     'Guidelines','|','Characters','Line','Form','XHTMLSource','|',"+
            "\n     'Search','|','StyleAndFormatting','TextFormatting','ListFormatting',"+
            "\n     'BoxFormatting','ParagraphFormatting','CssText','|'];"+
            "\n   oEdit1.REPLACE('_ta_" + dato + "');"+
            "\n   oEdit1.putHTML(document.getElementById(\"submitForm\")."+dato+".value);"+
            "\n   salvaRTA();"+
            "\n   var oEditor=eval(\"idContent\"+oEdit1.oName);"+
            "\n   oEditor.document.body.onblur=salvaRTA;"+
            "\n   salvaRTA();"+
            "\n   function contextMenu() {"+
            "\n      return false;"+
            "\n   }"+
            "\n   function hideMenu() {"+
            "\n      return false;"+
            "\n   }"+
            "\n   function salvaRTA() {"+  
            "\n      document.body.style.cursor='wait';"+
            "\n      var testo = new String(\"\");"+
            "\n      var testoNew =  new String(\"\");"+
            "\n      var sbte = new String(\"\");"+
            "\n      testo = oEdit1.getXHTMLBody();"+
            "\n      var up = 0;"+
            "\n      testoNew = \"\";"+
            "\n      for(i=0;i<testo.length;i++) {"+
            "\n         cCode=testo.charCodeAt(i);"+
            "\n         if (cCode > 255) {"+
            "\n            sbte = testo.substring(up, i);"+
            "\n            testoNew = testoNew + sbte;"+
            "\n            testoNew = testoNew + \"mmacode(\"+testo.charCodeAt(i)+\")\";"+
            "\n            up = i+1;"+
            "\n         }"+
            "\n      }"+
            "\n      sbte = testo.substring(up, i);"+
            "\n      testoNew = testoNew + sbte;"+
            "\n      var testonuovo = new String(\"\");"+
            "\n      var i = 0;"+
            "\n      do {"+
            "\n         testonuovo = testonuovo + escape(testoNew.substr(i,100));"+
            "\n         i = i + 100;"+ 
            "\n      } while (i < testoNew.length );"+
            "\n      var x = document.getElementById(\"submitForm\")."+dato+";"+
            "\n      x.setAttribute('value',testonuovo);"+
            "\n      document.body.style.cursor='default';"+
            "\n   }"+
            "\n</script>\n";
        }

        break;
        
      case 'C':  // COMBOBOX 
        String domErrore = "";
        if (readOnly){
          String s="";
          if (valore != null) {
            s = valore;
            retval += s;
          }
          if (dominio != null) {
            String myvalore = "";
            String mycodice = "";
            String mystile = "";
            myvalore = dominio.getValore(s);
            mycodice = dominio.getCodice(myvalore);
            if (myvalore == null) {
              if (!s.length() == 0) {
                logger.error("Errore in decodifica campo: '"+dato+"' area: '"+area+"' codice modello: '"+modello+"'");
                if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
                  domErrore = "<img style='border: none; cursor: hand;' src='../common/images/gdm/1.gif' alt='Errore in decodifica campo' );' />&nbsp;\n";
                } else {
                  domErrore = "<img style='border: none; cursor: hand;' src='images/gdm/1.gif' );' />&nbsp;\n";
                }
              }
              myvalore = "";
              mycodice = s;
            }
            int j = mycodice.indexOf("style=");
            if (j > -1) {
              mystile = mycodice.substring(j)+"'";
              retval = "<span "+stileLett+"><span "+mystile+">"+myvalore+"</span></span>";
            } else {
              retval = "<span "+stileLett+">"+myvalore+"</span>";
            }

          }
          retval += domErrore+"\n<input type='hidden' name='" + dato + "' value=\"" + valore.replaceAll("\"","&quot;") + "\"/>\n"; 
          
        }else{
          if (tipoAccesso.equalsIgnoreCase("P")) {
            String s="";
            String myvalore = "";
            if (valore != null) {
              s = valore;
            }
            if (dominio != null) {
              myvalore = dominio.getValore(s);
              if (myvalore == null) {
                if (!s.length() == 0) {
                  logger.error("Errore in decodifica campo: '"+dato+"' area: '"+area+"' codice modello: '"+modello+"'");
                  if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
                    domErrore = "<img style='border: none; cursor: hand;' src='../common/images/gdm/1.gif' alt='Errore in decodifica campo' );' />&nbsp;\n";
                  } else {
                    domErrore = "<img style='border: none; cursor: hand;' src='images/gdm/1.gif' );' />&nbsp;\n";
                  }
                }
                myvalore = "";
              }
            }
            retval = "\n<input type='hidden' name='" + dato + "' value=\"" + valore.replaceAll("\"","&quot;") + "\"/>\n"; 
            retval += ancor+"\n<input type='text' "+
                            "class='"+classname+"' " +
                            "name='" + dato + "_GDM_DISABLED' "+
                            "maxlength='" + lunghezzaStandard + "' "+
                            "value=\"" + myvalore.replaceAll("\"","&quot;") + "\" " +
                            stile+ " READONLY />\n"+domErrore;
          } else {
            retval = ancor+"\n<select class='"+classname+"' size='" + decimali + "' name='" + dato + "' " + stile +" ";
            retval = retval + formattaControlli();
            retval += "onchange='"+aggiorna+"'>";

            if (dominio != null) {
              for (i=0; i<dominio.getNumeroValori(); i++) {
                cd = dominio.getCodice(i);
                vd = dominio.getValore(i);
              // NB: il valore del campo in realt� e' il suo codice, sebbene l'utente 
              // scelga in base alla descrizione

                retval += "\n<option ";
                if (cd.equals(valore))   // Preseleziona quello che rappresenta il valore attuale del campo
                  retval += "selected='selected' ";
                retval += "value='" + cd + "'>"+ vd +"</option>";
              }
            }
            retval = retval + " </select>\n"+sErrore; 
          }
          if (lettura.equalsIgnoreCase("Q") && between) {
//            retval += "\n<span id='_2_"+dato+"' style='display:none'><select class='"+classname+"' size='" + decimali + "' name='_2_" + dato + "' " + stile +" ";
            retval += "\n<span id='_2_"+dato+"' > al <select class='"+classname+"' size='" + decimali + "' name='_2_" + dato + "' " + stile +" ";
            retval += "onchange=''>";

            if (dominio != null) {
              for (i=0; i<dominio.getNumeroValori(); i++) {
                cd = dominio.getCodice(i);
                vd = dominio.getValore(i);
              // NB: il valore del campo in realt� e' il suo codice, sebbene l'utente 
              // scelga in base alla descrizione

                retval += "\n<option ";
                if (cd.equals(valore_2))   // Preseleziona quello che rappresenta il valore attuale del campo
                  retval += "selected='selected' ";
                retval += "value='" + cd + "'>"+ vd +"</option>";
              }
            }
            retval = retval + " </select></span>\n"; 
            
          }
        }
        break;

      case 'R':   // RADIO BUTTON
        // Attenzione: Tutti i RadioButton dello stesso gruppo devono avere
        //             lo stesso nome (e naturalmente valori diversi)
//        String valHidden = "";
        retval += ancor;
        if (dominio != null) {
          for (i=0; i<dominio.getNumeroValori(); i++) {
            if ((decimali > 0) && ((i % decimali) == 0) && (i > 0)) {
              // In base al numero di colonne che desidero forzo una nuova riga 
              retval = retval + "<br/>";
            }
            cd = dominio.getCodice(i);
            vd = dominio.getValore(i);
            // NB: il valore del campo in realt� e' il suo codice, sebbene l'utente 
            // scelga in base alla descrizione

            retval += "\n<input class='"+classname+"' type='radio' name='" + dato + "' value='" + cd + "' ";
            
            if (cd.equals(valore)) {
              retval += "checked='checked' ";
              if (readOnly || tipoAccesso.equalsIgnoreCase("P")) {
                valHidden = "<input type='hidden' "+
                            "name='" + dato + "' "+
                            "value=\"" + cd.replaceAll("\"","&quot;") + "\"/>\n";
              }
            }
            if (readOnly || tipoAccesso.equalsIgnoreCase("P")) 
              retval += "disabled ";

//            retval += formattaControlli() + protetto;
            retval += formattaControlli().replaceAll("onchange","onclick");
            retval += " onclick='"+aggiorna+"'/>" + "<span "+stileLett+">"+vd+"</span>";
          }
          retval += "\n"+sErrore+valHidden;
        }
        break;
      
      case 'B':   // CHECKBUTTON
        retval += ancor;
        if (dominio != null) {
          for (i=0; i<dominio.getNumeroValori(); i++) {
            if ((decimali > 0) && ((i % decimali) == 0) && (i > 0)) {
              // In base al numero di colonne che desidero forzo una nuova riga 
              retval = retval + "<br/>";
            }
            cd = dominio.getCodice(i);
            vd = dominio.getValore(i);

            // ------------------------------------------------------------
            // Controllo se il valore di dominio in esame � presente
            // all'interno della stringa che rappresenta i valori del campo 
            // (un campo checkbox pu� assumere pi� valori).
            // ------------------------------------------------------------
            StringTokenizer st = new StringTokenizer(valore, Parametri.SEPARAVALORI);
            boolean trovato = false;
            String nextToken = "";
            while ((st.hasMoreTokens()) && (!trovato)) {
              nextToken = st.nextToken();
              if (nextToken.compareTo(cd) == 0) trovato = true;
            }

            if (tipoAccesso.equalsIgnoreCase("P")) {
              retval += "\n<input class='"+classname+"' type='checkbox' name='" + dato + "_GDM_DISABLED_" + Integer.toString(i) + "' value='" + cd + "' ";
            
              if (trovato) {
                retval += "checked='checked' ";
              }
              retval += "disabled ";
              retval += "/><span "+stileLett+">"+vd+"</span>";

              retval += "\n<span style='display: none'><input class='"+classname+"' type='checkbox' name='" + dato + "_" + Integer.toString(i) + "' value='" + cd + "' ";
            
              if (trovato) {
                retval += "checked='checked' ";
              }
              
              if (readOnly) {
                retval += "disabled ";
              }
              
              retval += formattaControlli().replaceAll("onchange","onclick");
              retval += "onclick='"+aggiorna+"'/></span>";

            } else {
              retval += "\n<input class='"+classname+"' type='checkbox' name='" + dato + "_" + Integer.toString(i) + "' value='" + cd + "' ";
            
              if (trovato) {
                retval += "checked='checked' ";
              }
              
              if (readOnly) {
                retval += "disabled ";
              }
              
              retval += formattaControlli().replaceAll("onchange","onclick");
              retval += "onclick='"+aggiorna+"'/>" + "<span "+stileLett+">"+vd+"</span>";
            }
          }
          retval += "\n"+sErrore;
        }
        break;

      case 'T': // Non pu� pescare da un dominio.
        // Imposto la TEXT AREA, occorre veficare se � possibile 
        if (readOnly) {
          String newVal = valore.replaceAll("\n","<br/>");
          newVal = newVal.replaceAll("\t","&nbsp;&nbsp;&nbsp;&nbsp;");
          newVal = newVal.replaceAll(" ","&nbsp;");
          retval = "<span "+stileLett+">"+newVal+"</span>";

          retval += "\n<input type='hidden' name='" + dato + "' value=\"" + valore.replaceAll("\"","&quot;") + "\"/>\n"; 
        } else {
          retval = ancor+"\n<textarea class='"+classname+"' rows='" + decimali + "' " +
                           "name='" + dato + "' "+ 
                           "cols='" + lunghezza + "' " + stile + " " + formattaControlli();
          if (tipoAccesso.equalsIgnoreCase("P")) {
             retval += " READONLY ";
          } 
          retval += " onblur='lunghezza(this,"+lunghezzaStandard+"); "+aggiorna+"' >" + valore + "</textarea>\n"+sErrore;
          if (!tipoAccesso.equalsIgnoreCase("P")) {
            if ((pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) && !tipoAccesso.equalsIgnoreCase("P")) {
              retval += "<a id=\"ImageLink"+dato+"\" onclick=\"showLOV('../common/AmvEdit.do?TITLE="+label+"','', 'submitForm', '"+dato+"')\" href=\"#\" name=\"ImageLink"+dato+"\">"+
                        "<img src=\"../common/images/AMV/edit.gif\" border=\"0\" /></a>";
            }
          }
        }
        
        //retval = retval + formattaControlli();   // impostare dei controlli anche in questo caso!!
        break;

      default: 
        // Quando il tipo di campo non � stato riconosciuto, lo segnalo con un segnaposto
        // in rosso.
        //retval += "<img border='0' src='"+urlLocale+"/badtype.gif' width='145' height='22'>";      
        retval += "\n<img style='border: none' src='"+this.completeContextURL_images+"/badtype.gif' width='145' height='22' alt='Tipo di campo sconosciuto!' />\n";      
        break;
    }
    retval = "<span id='"+idAjax+"'>"+opRic+retval+"</span>";
    //Debug Tempo
    stampaTempo("CampoHTMLIn::getValue - Fine",area,modello,dato,ptime);
    //Debug Tempo
    return retval;
  }*/
  /**
   * 
   */
/*  private void loggerError(String sMsg, Exception e) {
      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
        logger.error(sMsg);
      }
      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
        logger.error(sMsg,e);
      }
  }*/

}