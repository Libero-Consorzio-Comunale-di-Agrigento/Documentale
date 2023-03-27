package it.finmatica.modulistica.modulisticapack;
  
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
//import it.finmatica.jfc.dbUtil.IDbOperationSQL;
//import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.modutils.informazionicampo.InformazioniCampo;
//import it.finmatica.dmServer.management.*;
import org.apache.log4j.Logger;


/**     
 * Campo HtmlIn
 * 
 * @author  Nicola Samoggia
 * @author  Antonio Plastini   
 * @author  Sergio Spadaro
 * @version 1.0
 */
public class CampoHTMLIn extends Campo {

  protected ListaControlli controlli;
  protected Dominio   dominioVisu;
  protected HttpServletRequest sRequest;
  protected String 		sNomeServlet = "";
  protected String    lettura = "";
  protected boolean   between = false;
  protected boolean   settoreProtetto = false;
  protected String  	sTestoHtml = "";
  protected String		str_campi_obb	= "";

//  protected Parametri param;
 private  static Logger logger = Logger.getLogger(CampoHTMLIn.class);

  /**
   * 
   */
  public CampoHTMLIn(HttpServletRequest pRequest, 
                     String pArea, 
                     String pModello, 
                     String pTestoHtml, 
//                     Parametri pParametri, 
                     java.sql.Timestamp scadenza,
                    IDbOperationSQL dbOpEsterna) throws Exception {
    // Il dato lo posso identificare solo dopo la costruzione dell'oggetto, per cui lo passo ""
    super(pRequest, pArea, pModello, "", scadenza);

    // Recupero dal testo i dati necessari per identificare il campo poi lo carico da database
    // e infine ne carico l'eventuale valore dalla sessione (da usare come default)
    sNomeServlet = (String)pRequest.getSession().getAttribute("p_nomeservlet");
    sTestoHtml = pTestoHtml;
//    param  = pParametri;
    identificaCampo(pTestoHtml);//, pParametri);
    caricaCampo(pRequest,dbOpEsterna);
    caricaValore(pRequest);

    sRequest = pRequest;
    // Carico i controlli legati al campo
    controlli =  new ListaControlli(pRequest, area_dato, dato, "D", dbOpEsterna);
    if (domVisu == null) {
      dominioVisu = null;
    } else {
      dominioVisu = new DominioVisualizza(area_dato, domVisu, "-", tipo, null, dato, pRequest,dbOpEsterna);
    }
    lettura = pRequest.getParameter("rw");
    if (lettura == null) {
      lettura = "";
    }
//    controlli.caricaControlloStandard(pRequest, tipo);
  }

  /** 
   * 
   */
//  public void identificaCampo(String pTestoHtml, Parametri pParametri) throws Exception {
  public void identificaCampo(String pTestoHtml) throws Exception {
    //Debug Tempo
    long ptime = stampaTempo("CampoHTMLIn::identificaCampo - Fine",area,modello,dato,0);
    //Debug Tempo
    int startChar = pTestoHtml.indexOf(Parametri.getNameFieldBegin()), 
        endChar   = pTestoHtml.indexOf(Parametri.getNameFieldEnd());
    
    if ((startChar == -1) || (endChar == -1)) {
      throw new Exception("Attenzione! Non � possibile identificare il campo.");
    } else {
      // Sono in grado di stabilire il nome del campo.

    InformazioniCampo infoCampo = new InformazioniCampo(pTestoHtml,Parametri.getNameFieldEnd());
      dato = infoCampo.getDato();
      stile = " style=\""+infoCampo.getStile()+"\" ";
      stileLett = stile.replaceAll("WIDTH","L");
      stileLett = stileLett.replaceAll("HEIGHT","A");
      classname = infoCampo.getClassName();
      elementiAjax = infoCampo.getElementiAjax();      
    }
    //Debug Tempo
    stampaTempo("CampoHTMLIn::identificaCampo - Fine",area,modello,dato,ptime);
    //Debug Tempo
  }

  /**
   * 
   */
  public int getNumeroControlli() {
    return controlli.getNumeroControlli();
  }

  /**
   * 
   */
  public Controllo getControllo(int pos) {
    return controlli.getControllo(pos);
  }

  public String getPRNValue(IDbOperationSQL dbOpEsterna) {
    return "<b>CAMPO HTMLIN</b><br/>";
  }

  /**
   * NUOVO
   */
  public String getPRNValue() {
    return "<b>CAMPO HTMLIN</b><br/>";
  }

  public String getPRNComValue(IDbOperationSQL dbOpEsterna) {
    return "<b>CAMPO HTMLIN</b><br/>";
  }


  /**
   * NUOVO
   */
  public String getPRNComValue() {
    return "<b>CAMPO HTMLIN</b><br/>";
  }

  /**
   * 
   */
  public String getZValue() {
    String    retval = ""; 
    boolean   readWrite = ((tipoAccesso.compareTo("P") == 0) || (tipoAccesso.compareTo("S") == 0) || (tipoAccesso.compareTo("O") == 0)  || (tipoAccesso.compareTo("A") == 0)  || (tipoAccesso.compareTo("R") == 0));
    boolean   readOnly = (tipoAccesso.compareTo("L") == 0);
    boolean   tipoAccessoValido = (readWrite || readOnly);
    if (!tipoAccessoValido) {
      return null;
    }

    switch (tipoCampo.charAt(0)) {
      case 'Z': // RICHTextArea
        retval = dato;
        break;

      default:
        retval = null;
        break;
    }
    return retval;
  }

  public String getValue() {
    return getValue(null);
  }

  /**
   * 
   */
  public String getValue(IDbOperationSQL dbOpEsterna) {
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
    String    text_upper = "";
//    String    protetto = "";
    int       i;
    int       npos;
    int       posCampo;
    int       posSeverity;
    int       endSeverity;
    if (settoreProtetto && ((tipoAccesso.compareTo("S") == 0) || (tipoAccesso.compareTo("O") == 0)  || (tipoAccesso.compareTo("A") == 0)  || (tipoAccesso.compareTo("R") == 0))) {
      tipoAccesso = "P";
    }
    boolean   readWrite = ((tipoAccesso.compareTo("P") == 0) || (tipoAccesso.compareTo("S") == 0) || (tipoAccesso.compareTo("O") == 0)  || (tipoAccesso.compareTo("A") == 0)  || (tipoAccesso.compareTo("R") == 0));
    boolean   readOnly = (tipoAccesso.compareTo("L") == 0);
    boolean   tipoAccessoValido = (readWrite || readOnly);

    
    
    if (maiuscolo.equalsIgnoreCase("S")) {
      text_upper = "style=\"text-transform:uppercase;\" onblur=\"javascript:this.value=this.value.toUpperCase();\" onfocus=\"javascript:this.value=this.value.toUpperCase();\"";
    }
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
                		"id='_C_" + dato + "' "+
                        "name='" + dato + "' "+
                        "size='" + lunghezza + "' "+
                        "value=\"" + valore.replaceAll("\"","&quot;") + "\" ";
        retval += formattaControlli("","")+"/>\n";
        retval =  "<span id='"+idAjax+"'>"+retval+"</span>";
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
        				"id='_C_" + dato + "' "+
                        "name='" + dato + "' "+
                        "size='" + lunghezza + "' "+
                        "value=\"" + valore.replaceAll("\"","&quot;") + "\" ";
        retval += formattaControlli("","")+"/>\n";
        break;
      case 'F':  // Filtro
        retval = ancor+"\n<input type='text' "+
                        "class='"+classname+"' " +
                		"id='_C_" + dato + "' "+
                        "name='" + dato + "' "+
                        "size='" + lunghezza + "' "+
                        "maxlength='" + lunghezzaStandard + "' "+
                        "value=\"" + valore.replaceAll("\"","&quot;") + "\" " +
                        stile +text_upper+" />\n";
        if (formattaControlli("","").length() == 0) {
          if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
            retval += "<img style='border: none; cursor: hand;' src='../common/images/gdm/funnel.gif' alt='Filtro valori' onclick='addHiddenInput(document.getElementById(\"submitForm\"));' />&nbsp;\n";
          } else {
            retval += "<img style='border: none; cursor: hand;' src='images/gdm/funnel.gif' alt='Filtro valori' onclick='addHiddenInput(document.getElementById(\"submitForm\"));' />&nbsp;\n";
          }
        } else {
          if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
            retval += "<img style='border: none; cursor: hand;' src='../common/images/gdm/funnel.gif' alt='Filtro valori' "+formattaControlli("","").replaceFirst("onchange","onclick")+" />&nbsp;\n";
          } else {
            retval += "<img style='border: none; cursor: hand;' src='images/gdm/funnel.gif' alt='Filtro valori' "+formattaControlli("","").replaceFirst("onchange","onclick")+" />&nbsp;\n";
          }
        }
        break;
      case 'S':  // TEXTBOX (standard) (non pu� pescare da un dominio)
        if (readOnly) {
          retval = "<span "+stileLett+">"+valore+"</span>";
          retval += "\n<input type='hidden' "+
                        "name='" + dato + "' "+
                  		"id='_C_" + dato + "' "+
                        "size='" + lunghezza + "' "+
                         "value=\"" + valore.replaceAll("\"","&quot;") + "\" ";
//          retval = "<pre>"+valore+"</pre>\n" ;
        } else {
          if (tipoAccesso.equalsIgnoreCase("P")) {
          	classname = "AFCInputReadOnly";
          }
          retval = ancor+"\n<input type='text' "+
                          "class='"+classname+"' " +
                  		  "id='_C_" + dato + "' "+
                          "name='" + dato + "' "+
                          "size='" + lunghezza + "' "+
                          "maxlength='" + lunghezzaStandard + "' "+
                          "value=\"" + valore.replaceAll("\"","&quot;") + "\" " +
                          stile+ text_upper+" ";
          if (tipoAccesso.equalsIgnoreCase("P")) {
             retval += " READONLY ";
          }
          sControlli = formattaControlli("","");
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
        	String newFormato = formato.replaceAll("yy", "y").replaceAll("hh", "00").replaceAll("mi", "00").replaceAll("ss", "00");
          /*retval += "<script type='text/javascript' >var submitForm_DatePicker_"+dato+" = new Object(); ";
          retval += "submitForm_DatePicker_"+dato+".format = '"+formato.replaceAll("mi", "nn")+"';";
          retval += "submitForm_DatePicker_"+dato+".style = 'Themes/AFC/Style.css'; ";
          if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
            retval += "submitForm_DatePicker_"+dato+".relativePathPart = '../'; </script>";
          } else {
            retval += "submitForm_DatePicker_"+dato+".relativePathPart = './'; </script>";
          }

          retval += "&nbsp;<a class='AFCDataLink' href='javascript:showDatePicker(\"submitForm_DatePicker_"+dato+"\",\"submitForm\",\""+dato+"\");' >";*/
          if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
        	  retval += "<script>$(function() { $( \"#_C_"+dato+"\" ).datepicker({showOn: 'button', buttonImageOnly: true, buttonImage: '../Themes/DatePicker/DatePicker1.gif', dateFormat: '"+newFormato+"' }); });</script>";
           // retval += "<img style='border: none' src='../Themes/DatePicker/DatePicker1.gif' alt='DatePicker' /></a>&nbsp;\n";
          } else {
        	  retval += "<script>$(function() { $( \"#_C_"+dato+"\" ).datepicker({showOn: 'button', buttonImageOnly: true, buttonImage: 'Themes/DatePicker/DatePicker1.gif', dateFormat: '"+newFormato+"' }); });</script>";
            //retval += "<img style='border: none' src='Themes/DatePicker/DatePicker1.gif' alt='DatePicker' /></a>&nbsp;\n";
          }
        	
        }
        if (tipoAccesso.compareTo("O") == 0 || (tipoAccesso.compareTo("R") == 0)) {
        	retval += str_campi_obb;
        }
        retval += sErrore;
        if (lettura.equalsIgnoreCase("Q") && between) {
        	String newFormato = formato.replaceAll("yy", "y").replaceAll("hh", "00").replaceAll("mi", "00").replaceAll("ss", "00");
          retval += "<span id='_2_"+dato+"' > al <input type='text' "+
            "class='"+classname+"'  " +
            "id='_C_2_" + dato + "' "+
            "name='_2_" + dato + "' "+
            "size='" + lunghezza + "' "+
            "maxlength='" + lunghezzaStandard + "' "+
            "value=\"" + valore_2.replaceAll("\"","&quot;") + "\" " +
            stile+ text_upper +" ";
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
            /*retval += "<script type='text/javascript' >var submitForm_DatePicker__2_"+dato+" = new Object(); ";
            retval += "submitForm_DatePicker__2_"+dato+".format = '"+formato.replaceAll("mi", "nn")+"';";
            retval += "submitForm_DatePicker__2_"+dato+".style = 'Themes/AFC/Style.css'; ";
            if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
              retval += "submitForm_DatePicker__2_"+dato+".relativePathPart = '../'; </script>";
            } else {
              retval += "submitForm_DatePicker__2_"+dato+".relativePathPart = './'; </script>";
            }

            retval += "&nbsp;<a class='AFCDataLink' href='javascript:showDatePicker(\"submitForm_DatePicker__2_"+dato+"\",\"submitForm\",\"_2_"+dato+"\");' >";*/
            if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
            	retval += "<script>$(function() { $( \"#_C_2_"+dato+"\" ).datepicker({showOn: 'button', buttonImageOnly: true, buttonImage: '../Themes/DatePicker/DatePicker1.gif', dateFormat: '"+newFormato+"' }); });</script>";
              //retval += "<img style='border: none' src='../Themes/DatePicker/DatePicker1.gif' alt='DatePicker' /></a>&nbsp;\n";
            } else {
            	retval += "<script>$(function() { $( \"#_C_2_"+dato+"\" ).datepicker({showOn: 'button', buttonImageOnly: true, buttonImage: '../Themes/DatePicker/DatePicker1.gif', dateFormat: '"+newFormato+"' }); });</script>";
              //retval += "<img style='border: none' src='Themes/DatePicker/DatePicker1.gif' alt='DatePicker' /></a>&nbsp;\n";
            }

          }
          retval += "</span>\n";
        }
        break;
      
      case 'Z': // RICHTextArea
        if (readOnly || tipoAccesso.equalsIgnoreCase("P")) {
          retval = "<span "+stileLett+">"+valore+"</span>";

          retval += "\n<input type='hidden' id='_C_" + dato + "' name='" + dato + "' value=\"" + valore.replaceAll("\"","&quot;") + "\"/>\n"; 
        } else {
          retval = ancor+"\n<textarea rows='" + decimali + "' " +
                           " id='_CRT_" + dato + "' name='_C_" + dato + "' "+ 
                           "cols='" + lunghezza + "' " + stile + " " + formattaControlli("","") + " onblur='lunghezza(this,"+lunghezzaStandard+"); "+aggiorna+"' ></textarea>\n";
          retval += "\n<input type='hidden' id='_C_" + dato + "' name='" + dato + "' value=\"" + valore.replaceAll("\"","&quot;") +  "\" "+ stile +"/>\n"; 
          retval +=
            "\n<script>"+
//            "\n   var menuFF;"+
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
            "\n   oEdit1.REPLACE('_CRT_" + dato + "');"+
            "\n   oEdit1.putHTML(document.getElementById(\"submitForm\")."+dato+".value);"+
            "\n   if(navigator.appName.indexOf('Microsoft')!=-1 || navigator.appVersion.indexOf('Trident/') > 0) {"+
            "\n   	var oEditor=eval(\"idContent\"+oEdit1.oName);"+
            "\n   	oEditor.document.body.onblur=salvaRTA;"+
            "\n   } else {"+
            "\n   	var oEditor=document.getElementById(\"idContent\"+oEdit1.oName).contentWindow;"+
            "\n   	oEditor.document.addEventListener('blur',salvaRTA,false);"+
            "\n		}"+
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
            "\n	     testoNew = testoNew + sbte;"+
            "\n      var x = document.getElementById(\"submitForm\")."+dato+";"+
            "\n      x.setAttribute('value',testo);"+
            "\n      document.body.style.cursor='default';"+
            "\n   }"+
            "\n</script>\n";
          if (tipoAccesso.compareTo("O") == 0 || (tipoAccesso.compareTo("R") == 0)) {
          	retval += str_campi_obb;
          }
          retval += sErrore;
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
              if (s.length() != 0) {
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
          retval += domErrore+"\n<input type='hidden' id='_C_" + dato + "' name='" + dato + "' value=\"" + valore.replaceAll("\"","&quot;") + "\"/>\n"; 
          
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
                if (s.length() != 0) {
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
            retval = "\n<input type='hidden' id='_C_" + dato + "' name='" + dato + "' value=\"" + valore.replaceAll("\"","&quot;") + "\"/>\n"; 
            classname = "AFCInputReadOnly";
            retval += ancor+"\n<input type='text' "+
                            "class='"+classname+"' " +
                            "id='_C_" + dato + "' name='" + dato + "_GDM_DISABLED' "+
                            "maxlength='" + lunghezzaStandard + "' "+
                            "value=\"" + myvalore.replaceAll("\"","&quot;") + "\" " +
                            stile+ " READONLY />\n"+domErrore;
          } else {
            retval = ancor+"\n<select class='"+classname+"' size='" + decimali + "' id='_C_" + dato + "' name='" + dato + "' " + stile +" ";
            retval = retval + formattaControlli(aggiorna,"onchange");
            retval += ">";
//            retval += "onchange='"+aggiorna+"'>";

            if (dominio != null) {
              for (i=0; i<dominio.getNumeroValori(); i++) {
                cd = dominio.getCodice(i);
                vd = dominio.getValore(i);
              // NB: il valore del campo in realt� e' il suo codice, sebbene l'utente 
              // scelga in base alla descrizione

                retval += "\n<option ";
                if (cd.equals(valore))   // Preseleziona quello che rappresenta il valore attuale del campo
                  retval += "selected='selected' ";
                retval += "value=\"" + cd.replaceAll("\"","&quot;") + "\">"+ vd +"</option>";
              }
            }
            retval = retval + " </select>\n"; 
            if (tipoAccesso.compareTo("O") == 0 || (tipoAccesso.compareTo("R") == 0)) {
            	retval += str_campi_obb;
            }
            retval = retval + sErrore;
          }
          if (lettura.equalsIgnoreCase("Q") && between) {
//            retval += "\n<span id='_2_"+dato+"' style='display:none'><select class='"+classname+"' size='" + decimali + "' name='_2_" + dato + "' " + stile +" ";
            retval += "\n<span id='_2_"+dato+"' > al <select class='"+classname+"' size='" + decimali + "' id='_C_2_" + dato + "' name='_2_" + dato + "' " + stile +" ";
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

            retval += "\n<input class='"+classname+"' type='radio' id='_C_" + dato + "' name='" + dato + "' value='" + cd + "' ";
            
            if (cd.equals(valore)) {
              retval += "checked='checked' ";
              if (readOnly || tipoAccesso.equalsIgnoreCase("P")) {
                valHidden = "<input type='hidden' "+
                            "id='_C_" + dato + "' name='" + dato + "' "+
                            "value=\"" + cd.replaceAll("\"","&quot;") + "\"/>\n";
              }
            }
            if (readOnly || tipoAccesso.equalsIgnoreCase("P")) 
              retval += "disabled ";

//            retval += formattaControlli() + protetto;
            retval += formattaControlli(aggiorna,"onclick").replaceAll("onchange","onclick");
            retval += " />" + "<span "+stileLett+">"+vd+"</span>";
//            retval += " onclick='"+aggiorna+"'/>" + "<span "+stileLett+">"+vd+"</span>";
          }
          if (tipoAccesso.compareTo("O") == 0 || (tipoAccesso.compareTo("R") == 0)) {
          	retval += str_campi_obb;
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
              retval += "\n<input class='"+classname+"' type='checkbox' id='_C_" + dato + "_GDM_DISABLED_" + Integer.toString(i) + "' name='" + dato + "_GDM_DISABLED_" + Integer.toString(i) + "' value='" + cd + "' ";
            
              if (trovato) {
                retval += "checked='checked' ";
              }
              retval += "disabled ";
              retval += "/><span "+stileLett+">"+vd+"</span>";

              retval += "\n<span style='display: none'><input class='"+classname+"' type='checkbox' id='_C_" + dato + "_" + Integer.toString(i) + "' name='" + dato + "_" + Integer.toString(i) + "' value='" + cd + "' ";
            
              if (trovato) {
                retval += "checked='checked' ";
              }
              
              if (readOnly) {
                retval += "disabled ";
              }
              
              retval += formattaControlli(aggiorna,"onclick").replaceAll("onchange","onclick");
              retval += "/></span>";
//              retval += "onclick='"+aggiorna+"'/></span>";

            } else {
              retval += "\n<input class='"+classname+"' type='checkbox' id='_C_" + dato + "_" + Integer.toString(i) + "' name='" + dato + "_" + Integer.toString(i) + "' value='" + cd + "' ";
            
              if (trovato) {
                retval += "checked='checked' ";
              }
              
              if (readOnly) {
                retval += "disabled ";
              }
              
              retval += formattaControlli(aggiorna,"onclick").replaceAll("onchange","onclick");
              retval += "/>" + "<span "+stileLett+">"+vd+"</span>";
//              retval += "onclick='"+aggiorna+"'/>" + "<span "+stileLett+">"+vd+"</span>";
              if (tipoAccesso.compareTo("O") == 0 || (tipoAccesso.compareTo("R") == 0)) {
              	retval += str_campi_obb;
              }
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

          retval += "\n<input type='hidden' id='_C_" + dato + "' name='" + dato + "' value=\"" + valore.replaceAll("\"","&quot;") + "\"/>\n"; 
        } else {
          if (tipoAccesso.equalsIgnoreCase("P")) {
          	classname = "AFCTextareaReadOnly";
          }
          retval = ancor+"\n<textarea class='"+classname+"' rows='" + decimali + "' " +
                           "id='_C_" + dato + "' name='" + dato + "' "+ 
                           "cols='" + lunghezza + "' " + stile + text_upper + " " + formattaControlli("","");
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
          if (tipoAccesso.compareTo("O") == 0 || (tipoAccesso.compareTo("R") == 0)) {
          	retval += str_campi_obb;
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
  }
  
  /**
   * 
   */
  protected String formattaControlli(String aggiorna, String pEvento) {
    String    retval ="";
    String    sAjax = "onchange='";
    String    contr = "";
    String    evento = "";
    String    evento_agg = "onchange='";
    String    controllo = "";
    String    elemento = "";
    String    tipoEle = "";
    String    sServlet = "";
    Controllo c;
    int       npos;

    //Debug Tempo
    long ptime = stampaTempo("CampoHTMLIn::formattaControlli - Inizio",area,modello,dato,0);
    //Debug Tempo
    
    if (pEvento.length() != 0) {
      evento_agg = pEvento+"='";
      sAjax = evento_agg;
    }
    //Implemento le chiamate alle perazioni di tipo Ajax
    if (pdo.equalsIgnoreCase("CC") || pdo.equalsIgnoreCase("HR")) {
      sServlet = "GdmAjax.do";
    } else {
      sServlet = "ServletModulistica";
    }
    if (aggiorna.length() == 0) {
      StringTokenizer st = new StringTokenizer(elementiAjax,";");
      while (st.hasMoreTokens()) {
        elemento = st.nextToken();
        tipoEle = elemento.substring(0,1);
        elemento = elemento.substring(1);
        
        if (tipoEle.equalsIgnoreCase("C")) {
          sAjax += "f_AjaxCampo(\""+sServlet+"\",\""+elemento+"\");";
        }
        if (tipoEle.equalsIgnoreCase("S")) {
          sAjax += "f_AjaxSettore(\""+sServlet+"\",\""+elemento+"\");";
        }
        if (tipoEle.equalsIgnoreCase("B")) {
          sAjax += "f_AjaxBlocco(\""+sServlet+"\",\""+elemento+"\",null,null,null);";
        }
      }
    } else {
      sAjax += aggiorna;
    }
    if (!sAjax.equalsIgnoreCase(evento_agg)) {
      sAjax += "'"; 
    } else {
      sAjax = "";
    }
    retval = sAjax;
    
    // Implemento i controlli del campo che impongo vengano attivati al momento del SUBMIT
    if (controlli.getNumeroControlli() > 0) {
      contr = retval;
      for (int i = 0; i < controlli.getNumeroControlli(); i++) {
        c = controlli.getControllo(i);
        evento = c.getEvento();
        controllo = c.getControllo();
        npos = contr.indexOf(evento);
        if (npos >= 0) {
          retval = contr.substring(0,npos+(evento.length()+2));
//          retval += controllo+"(window.document.forms[0]."+dato+".value); ";
          retval += controllo+"(this); ";
          retval += contr.substring(npos+(evento.length()+2));
        } else {
//          retval += evento + "='" + controllo + "(window.document.forms[0]."+dato+".value)' ";
          retval += evento + "='" + controllo + "(this)' ";
        }
        contr = retval;
      }
//    } else {
//      retval = "";
    }
    //Debug Tempo
    stampaTempo("CampoHTMLIn::formattaControlli - Fine",area,modello,dato,ptime);
    //Debug Tempo
    return retval;
  }

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
  
  /**
   * 
   */
  private String opzioniRicerca(String campo) {
    String retval = "";
    if (lettura.equalsIgnoreCase("Q")) {
      retval =  " <select class='AFCInput' name='_OPER_"+campo+"' size=1 "+
                "onchange='"+
                "if (document.getElementById(\"_2_"+dato+"\") != null) {"+
                " if (value==\"Between\") {"+
                " document.getElementById(\"_2_"+dato+"\").style.display=\"inline\";"+
                " } else { "+
                " document.getElementById(\"_2_"+dato+"\").style.display=\"none\"; "+
                "} }'>\n"+
                "    <option value='=' selected>=</option>\n"+
                "    <option value='&gt;'>&gt;</option>\n"+
                "    <option value='#'>&lt;</option>\n"+
                "    <option value='#&gt;'>&lt;&gt;</option>\n"+
                "    <option value='Between'>Compreso</option>\n"+
                "    <option value='is null'>E' nullo</option>\n"+
                "    <option value='is not null'>Non � nullo</option>\n"+
                "</select>\n "+
                " <select class='AFCInput' name='_ORD_"+campo+"' size=1>\n"+
                "    <option value='NO' selected>--</option>\n"+
                "    <option value='ASC'>ASC</option>\n"+
                "    <option value='DESC'>DESC</option>\n"+
                "</select>\n ";
      retval = infoCampiQuery(dato);
    }
    return retval;
  }

  private String infoCampiQuery(String dato) {
    String retval = "";
    String area_d = null;
    String cm_d = null;
    String cate_d = null;
//    String ord_d = null;
    String oper_d = null;
    int i = 0;
    ListaDomini ld = null;
    if (sRequest.getSession() == null) {
//    	try {
//        ld = new ListaDomini();
//        ld.caricaDominiiDiArea(area, sRequest, true);
//        ld.caricaDominiiDelModello(area, modello, sRequest, true);
//    	} catch (Exception e) {
//    		ld = null;
//    	}
      ld = null;
      logger.error("CampoHTMLIn::infoCampiQuery - Sessione o Request nulla! Area: "+area+" - Modello: "+modello+" - Dato: "+dato);
    } else {
    	ld = (ListaDomini)sRequest.getSession().getAttribute("listaDomini");
    }

    Dominio dp = null;
    if (ld != null) {
      int numDom = ld.domini.size();
      i = 0;
      while (i < numDom && area_d == null) { 
        dp = (Dominio)ld.domini.get(i);
        if (dp.isDominioDelModello()) {
          area_d = dp.getValore("AREA_CAMPO_"+dato);
        }
        i++;
      }
      if (area_d == null) {
        area_d = "";
      }
      retval = "<input type='hidden' name='_AREA_"+dato+"' value='"+area_d+"' />";
      i = 0;
      while (i < numDom && cm_d == null) { 
        dp = (Dominio)ld.domini.get(i);
        if (dp.isDominioDelModello()) {
          cm_d = dp.getValore("CM_CAMPO_"+dato);
        }
        i++;
      }
      if (cm_d == null) {
        cm_d = "";
      }
      retval += "<input type='hidden' name='_MODELLO_"+dato+"' value='"+cm_d+"' />";
      i = 0;
      while (i < numDom && cate_d == null) { 
        dp = (Dominio)ld.domini.get(i);
        if (dp.isDominioDelModello()) {
          cate_d = dp.getValore("CATEGORIA_CAMPO_"+dato);
        }
        i++;
      }
      if (cate_d == null) {
        cate_d = "";
      }
      retval += "<input type='hidden' name='_CATEGORIA_"+dato+"' value='"+cate_d+"' />";
      if (cm_d == null) {
        cm_d = "";
      }
//      retval += "<input type='hidden' name='_MODELLO_"+dato+"' value='"+cm_d+"' />\n";
/*      i = 0;
      while (i < numDom && ord_d == null) { 
        dp = (Dominio)ld.domini.get(i);
        if (dp.isDominioDelModello()) {
          ord_d = dp.getValore("ORDINAMENTO_CAMPO_"+dato);
        }
        i++;
      }
      if (ord_d == null) {
        ord_d = "NO";
      }
      retval += "<input type='hidden' name='_ORD_"+dato+"' value='"+ord_d+"' />\n";*/
      i = 0;
      while (i < numDom && oper_d == null) { 
        dp = (Dominio)ld.domini.get(i);
        if (dp.isDominioDelModello()) {
          oper_d = dp.getValore("OPERATORE_CAMPO_"+dato);
        }
        i++;
      }
      between = false;
      if (oper_d == null) {
        oper_d = "";
      } else {
        if (oper_d.equalsIgnoreCase("Between")) {
          between = true;
        }
      }

      if (oper_d.equalsIgnoreCase("=")) {
//        retval += "&nbsp;=&nbsp;";
      }
      if (oper_d.equalsIgnoreCase(">")) {
//        retval += "&nbsp;&gt;&nbsp;";
        oper_d = "&gt;";
      }
      if (oper_d.equalsIgnoreCase("<")) {
//        retval += "&nbsp;&lt;&nbsp;";
        oper_d = "#";
      }
      if (oper_d.equalsIgnoreCase("<>")) {
//        retval += "&nbsp;&lt;&gt;&nbsp;";
        oper_d = "#&gt;";
      }
      if (oper_d.equalsIgnoreCase("Between")) {
        retval += "dal&nbsp;";
        oper_d = "Between";
      }
      if (oper_d.equalsIgnoreCase("is null")) {
        retval += "&nbsp;E' nullo&nbsp;";
        oper_d = "is null";
      }
      if (oper_d.equalsIgnoreCase("is not null")) {
        retval += "&nbsp;Non � nullo&nbsp;";
        oper_d = "is not null";
      }
      if (oper_d.equalsIgnoreCase("like")) {
//        retval += "&nbsp;Inizia per&nbsp;";
        oper_d = "LIKE";
      }
      if (oper_d.equalsIgnoreCase("CONTAINS")) {
//        retval += "&nbsp;E' contenuto&nbsp;";
        oper_d = "CONTAINS";
      }
      if (oper_d.equalsIgnoreCase("ESATTA")) {
//        retval += "&nbsp;Esatta&nbsp;";
        oper_d = "ESATTA";
      }
      retval += "<input type='hidden' name='_OPER_"+dato+"' value='"+oper_d+"' />";
    }
    return retval;
  }
  
  public String getTestoHTML() {
    return sTestoHtml;
  }
  
  public void setValore(Dati dati) {
    if (dati != null) {
      valore = dati.getValore(dato);
      valore_2 = dati.getValore("_2_"+dato);
      if (valore == null) {
        valore = "";
      }
      if (valore_2 == null) {
        valore_2 = "";
      }
    }
  }
//  public Parametri getParametri() {
//    return param;
//  }

  public void settaProtetto(boolean b_protetto) {
    settoreProtetto = b_protetto;
  }
  
  public void setNewRequest(HttpServletRequest request) {
    sRequest = request;
  }
  
  public void settaStrCampiObb(String  strCampObb) {
    str_campi_obb = strCampObb;
  }
  

}