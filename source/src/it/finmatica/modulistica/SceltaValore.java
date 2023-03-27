package it.finmatica.modulistica;
import it.finmatica.dmServer.Environment;
import it.finmatica.dmServer.util.JNDIParameter;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;

import it.finmatica.dmServer.management.AccediDocumento;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.modulistica.personalizzazioni.Personalizzazioni;

import java.sql.*;

import it.finmatica.modutils.multirecord.Multirecord;
import it.finmatica.modulistica.modulisticapack.Dominio;
import it.finmatica.modulistica.modulisticapack.ListaDomini;
import it.finmatica.modulistica.modulisticapack.ConnessioneParser;
import it.finmatica.modulistica.modulisticapack.Connessione;
import it.finmatica.modulistica.modulisticapack.SceltaValoreParser;

import org.apache.log4j.Logger;
//import org.apache.log4j.PropertyConfigurator;
import org.dom4j.*;
   
public class SceltaValore {
  private String      inifile = null;
  private String      corpoHtml = "";
  private String      bloccoHtml = "";
  private String      queryBody = "";
  private String      driver = "";
  private String      connessione = "";
  private String      utente = "";
  private String      passwd = "";
  private String      ritorno = "";
  private int         record;
  private String      ricerca = "";
  private String      filtri_g = "";
  private String      separatore = "";
  private String      tipo = "";
  private String      autoload = "";
  private String      aggiorna = "";
  private String      filtri_esterni = "N";
  private String      controllo_js = "";
  private String      chiudi_popup = "S";
  private List        colonna;
  private List        alias;
//  private static Properties confLogger = null;
  private static Logger     logger = Logger.getLogger(SceltaValore.class);
  private Document    dDoc;
  private Document    dDoc_Par;
  private Environment vu;
  private Personalizzazioni pers = null;
  
  public SceltaValore(String sPath) {
    init(sPath);
  }

  private void init(String sPath)  {
    try {
      String separa = File.separator;
      inifile = sPath + "config" + separa + "gd4dm.properties";
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
      Parametri.leggiParametriStandard(inifile);

      SessioneDb.getInstance().addAlias(Parametri.ALIAS, Parametri.SPORTELLO_DRIVER);
    } catch(Exception e) {
      logger.error("SceltaValore::init() - Attenzione! si è verificato un errore: "+e.toString());
    }
//    isVerbose = Parametri.ISVERBOSE;
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
  protected void freeConn() {
//    try {
//      SessioneDb.getInstance().closeFreeConnection();
//    } catch (Exception e) {
//      logger.error("SceltaValore::freeConn() - Attenzione! Errore in fase di rilascio connnessioni: "+e.toString());
//      corpoHtml += "<H2>Attenzione! Errore in fase di rilascio connnessioni.</h2>";
//      if (Parametri.DEBUG.equalsIgnoreCase("1")) {
//        corpoHtml += e.toString();
//      }
//      if (Parametri.DEBUG.equalsIgnoreCase("2")) {
//        corpoHtml += e.getStackTrace().toString();
//      }
//    }
  }

  /**
   * 
   */
  public String getValue() {
    return corpoHtml;
  }

  /**
   * 
   */
  private String creaFiltri(HttpServletRequest pRequest, String area, String cm, IDbOperationSQL dbOpEsterna) {
    String retval = "";
    String campo = "";
    String valore = "";
    String label = "";
    String tipo = "";
    String persDom = "";

    Element root, elp, elf;
    Element root_p, elp_p, elf_p;
    
    root = DocumentHelper.createElement("FUNCTION_INPUT");
    dDoc = DocumentHelper.createDocument();
    dDoc.setRootElement(root);
    elf = DocumentHelper.createElement("FILTRI");
    root.add(elf);

    if (filtri_esterni.equalsIgnoreCase("Y")) {
      retval += "      <p class='AFCFieldCaptionTD' style='text-align: center; width: 100%'>Campi di ricerca</p>\n"; 
      root_p = dDoc_Par.getRootElement();
      int num_par = 0;
      num_par = Integer.parseInt(leggiValoreXML(dDoc_Par, "RECORD"));
      retval += "<table>\n";
      for (int k=1; k <= num_par; k++) {
        elp_p = leggiElementoXML(root_p, "PAR_"+k);
        campo = leggiValoreXML(elp_p, "PARAMETRO");
        tipo = leggiValoreXML(elp_p, "TIPO");
        valore = pRequest.getParameter(campo);
        if (valore == null) {
          valore = "";
        }
        if (valore.length() != 0) {
          elp = DocumentHelper.createElement(campo);
          elp.setText(valore);
          elf.add(elp);
        }
        if (tipo.equalsIgnoreCase("F")) {
          retval += "      <input class='AFCInput' type ='hidden' name='"+campo+"' value=\""+valore.replaceAll("\"", "&quot;")+"\" />\n";
        } else {
          label = leggiValoreXML(elp_p, "LABEL");
          if (label.length() == 0) {
            label = leggiLabel(area, cm, campo,dbOpEsterna);
          }
          String area_dom = leggiValoreXML(elp_p, "AREA_DOMINIO");
          String nome_dom = leggiValoreXML(elp_p, "DOMINIO");
          if  (area_dom.length() != 0 && nome_dom.length() != 0) {
            String cd = "", vd = "";
            persDom = pers.getPersonalizzazione(Personalizzazioni.DOMINI, area_dom+"#"+nome_dom+"#-");
            int j = persDom.indexOf("#");
            int x = persDom.lastIndexOf("#");
            nome_dom = persDom.substring(j+1,x);
            Dominio dominio = null;
            try {
              ListaDomini ld = new ListaDomini();
              dominio = ld.caricaDominio(area_dom, nome_dom, "-", pRequest, dbOpEsterna);
              retval += "      <tr><td class='AFCFieldCaptionTD' style='width: 20%' >\n"+label+":</td>\n"+
              					"      <td class='AFCDataTD'style='width: 79%' >\n"+
              					"         <select class='AFCInput' size='1' name='" + campo + "'>";
/*              retval += "      <div><span class='AFCFieldCaptionTD' style='width: 20%' >\n"+label+":</span>\n"+
                        "      <span class='AFCDataTD'style='width: 79%' >\n"+
                        "         <select class='AFCInput' size='1' name='" + campo + "'>";*/
              for (int i=0; i<dominio.getNumeroValori(); i++) {
                cd = dominio.getCodice(i);
                vd = dominio.getValore(i);
              // NB: il valore del campo in realtà e' il suo codice, sebbene l'utente 
              // scelga in base alla descrizione
  
                retval += "\n<option ";
                if (cd.equals(valore))   // Preseleziona quello che rappresenta il valore attuale del campo
                  retval += "selected='selected' ";
                retval += "value='" + cd + "'>"+ vd +"</option>";
              }
/*              retval += " </select></span></div>\n"; */
              retval += " </select></td></tr>\n"; 
            } catch (Exception e) {
/*              retval += "      <div><span class='AFCFieldCaptionTD' style='width: 20%' >\n"+label+":</span>\n"+
                        "      <span class='AFCDataTD'style='width: 79%' >\n"+
                        "        <input class='AFCInput' type ='text' name='"+campo+"' size='50' value=\""+valore.replaceAll("\"", "&quot;")+"\" />"+
                        "      </span></div>\n";*/
              retval += "      <tr><td class='AFCFieldCaptionTD' style='width: 20%' >\n"+label+":</td>\n"+
              					"      <td class='AFCDataTD'style='width: 79%' >\n"+
              					"        <input class='AFCInput' type ='text' name='"+campo+"' size='50' value=\""+valore.replaceAll("\"", "&quot;")+"\" />"+
              					"      </td></tr>\n";
            }
          } else {
/*            retval += "      <div><span class='AFCFieldCaptionTD' style='width: 20%' >\n"+label+":</span>\n"+
                      "      <span class='AFCDataTD'style='width: 79%' >\n"+
                      "        <input class='AFCInput' type ='text' name='"+campo+"' size='50' value=\""+valore.replaceAll("\"", "&quot;")+"\" />"+
                      "      </span></div>\n";*/
            retval += "      <tr><td class='AFCFieldCaptionTD' style='width: 20%' >\n"+label+":</td>\n"+
            					"      <td class='AFCDataTD'style='width: 79%' >\n"+
            					"        <input class='AFCInput' type ='text' name='"+campo+"' size='50' value=\""+valore.replaceAll("\"", "&quot;")+"\" />"+
            					"      </td></tr>\n";
          }
        }
      }
      retval += "</table>";
      retval += "      <div class='AFCFieldCaptionTD' style='text-align: center; width: 100%'><input class='AFCButton' type='button' value='Cerca' name='B1' onclick='bloccoBLKFirst(this);' /></div>\n";
      retval += "      <p class='AFCFieldCaptionTD' style='text-align: center; width: 100%'>&nbsp;</p>\n"; 
      retval += "      <input class='AFCInput' type ='hidden' name='gdm_reload' value='1' />\n"; 
    } else {
      if (filtri_g.length() != 0) {
        StringTokenizer st = new StringTokenizer(filtri_g,"@");
        while (st.hasMoreElements()) {
          campo = st.nextToken();
          int i = campo.indexOf("(");
          if (i > -1) {
            campo = campo.substring(0,i);
          }
          valore = pRequest.getParameter(campo);
          if (valore == null) {
            valore = "";
          }
          if (valore.length() != 0) {
            elp = DocumentHelper.createElement(campo);
            elp.setText(valore);
            elf.add(elp);
          }
          retval += "      <input class='AFCInput' type ='hidden' name='"+campo+"' value=\""+valore.replaceAll("\"", "&quot;")+"\" />\n"; 
        }
      }
  
      if (ricerca.length() != 0) {
        retval += "      <p class='AFCFieldCaptionTD' style='text-align: center; width: 100%'>Campi di ricerca</p>\n"; 
        retval += "<table>";
        StringTokenizer st = new StringTokenizer(ricerca,"@");
        while (st.hasMoreElements()) {
          String tk = st.nextToken();
          campo = tk;
          int i = campo.indexOf("(");
          int j = campo.indexOf("[");
          if (i > -1 ) {
            if (j == -1 || j > i) {
              campo = campo.substring(0,i);
            } else {
              if (j > -1) {
                campo = campo.substring(0,j);
              }
            }
          } else {
            if (j > -1) {
              campo = campo.substring(0,j);
            }
          }
          valore = pRequest.getParameter(campo);
          if (valore == null) {
            valore = "";
          }
          if (valore.length() != 0) {
            elp = DocumentHelper.createElement(campo);
            elp.setText(valore);
            elf.add(elp);
          }
          label = leggiLabel(area, cm, campo,dbOpEsterna);
          if (j > -1 ) {
            i = tk.indexOf("]");
            String dom = tk.substring(j,i);
            int k = dom.indexOf("#");
            String area_dom = dom.substring(1, k);
            String nome_dom = dom.substring(k+1);
            persDom = pers.getPersonalizzazione(Personalizzazioni.DOMINI, area_dom+"#"+nome_dom+"#-");
            int y = persDom.indexOf("#");
            int x = persDom.lastIndexOf("#");
            nome_dom = persDom.substring(y+1,x);
            String cd = "", vd = "";
            Dominio dominio = null;
            try {
              ListaDomini ld = new ListaDomini();
              dominio = ld.caricaDominio(area_dom, nome_dom, "-", pRequest,dbOpEsterna);
              retval += "      <tr><td class='AFCFieldCaptionTD' style='width: 20%' >\n"+label+":</td>\n"+
              					"      <td class='AFCDataTD'style='width: 79%' >\n"+
              					"         <select class='AFCInput' size='1' name='" + campo + "'>";
/*              retval += "      <div><span class='AFCFieldCaptionTD' style='width: 20%' >\n"+label+":</span>\n"+
                        "      <span class='AFCDataTD'style='width: 79%' >\n"+
                        "         <select class='AFCInput' size='1' name='" + campo + "'>";*/
              for (i=0; i<dominio.getNumeroValori(); i++) {
                cd = dominio.getCodice(i);
                vd = dominio.getValore(i);
              // NB: il valore del campo in realtà e' il suo codice, sebbene l'utente 
              // scelga in base alla descrizione
  
                retval += "\n<option ";
                if (cd.equals(valore))   // Preseleziona quello che rappresenta il valore attuale del campo
                  retval += "selected='selected' ";
                retval += "value='" + cd + "'>"+ vd +"</option>";
              }
//              retval += " </select></span></div>\n"; 
              retval += " </select></td></tr>\n"; 
            } catch (Exception e) {
              retval += "      <tr><td class='AFCFieldCaptionTD' style='width: 20%' >\n"+label+":</td>\n"+
                        "      <td class='AFCDataTD'style='width: 79%' >\n"+
                        "        <input class='AFCInput' type ='text' name='"+campo+"' size='50' value=\""+valore.replaceAll("\"", "&quot;")+"\" />"+
                        "      </td></tr>\n";
/*              retval += "      <div><span class='AFCFieldCaptionTD' style='width: 20%' >\n"+label+":</span>\n"+
              					"      <span class='AFCDataTD'style='width: 79%' >\n"+
              					"        <input class='AFCInput' type ='text' name='"+campo+"' size='50' value=\""+valore.replaceAll("\"", "&quot;")+"\" />"+
              					"      </span></div>\n";*/
            }
          } else {
/*            retval += "      <div><span class='AFCFieldCaptionTD' style='width: 20%' >\n"+label+":</span>\n"+
                      "      <span class='AFCDataTD'style='width: 79%' >\n"+
                      "        <input class='AFCInput' type ='text' name='"+campo+"' size='50' value=\""+valore.replaceAll("\"", "&quot;")+"\" />"+
                      "      </span></div>\n";*/
            retval += "      <tr><td class='AFCFieldCaptionTD' style='width: 20%' >\n"+label+":</td>\n"+
            					"      <td class='AFCDataTD'style='width: 79%' >\n"+
            					"        <input class='AFCInput' type ='text' name='"+campo+"' size='50' value=\""+valore.replaceAll("\"", "&quot;")+"\" />"+
            					"      </td></tr>\n";
          }
        }
        retval += "</table>";
        retval += "      <div class='AFCFieldCaptionTD' style='text-align: center; width: 100%'><input class='AFCButton' type='button' value='Cerca' name='B1' onclick='bloccoBLKFirst(this);' /></div>\n";
        retval += "      <p class='AFCFieldCaptionTD' style='text-align: center; width: 100%'>&nbsp;</p>\n"; 
        retval += "      <input class='AFCInput' type ='hidden' name='gdm_reload' value='1' />\n"; 
      }
    }
    return retval;
  }

  private void initVu(String p_user) throws Exception {

    try {
      vu = new Environment(p_user, null, "MODULISTICA", "ADS", null, (new JNDIParameter("jdbc/gdm")));
      vu.setRuolo("GDM");
    } catch (Exception e) {
      throw e;
    }
  }

  public void genera(HttpServletRequest request, String pdo) {
    String us = request.getParameter("us");
    if (us == null) {
      us = "";
    }

    try {
      initVu(us);
      vu.connect();
      vu.setDbOpRestaConnessa(true); //Questo evita che ogni volta che passo la vu alle classi del dmserver, es. AggiornaDocumento
                                     //la classe rifaccia connect e/o disconnect.... a quello ci penso io
    } catch (Exception e) {
      logger.error("SceltaValore::caricaInformazioniBlocco - Errore in creazione dbOp: "+e.toString());
      return;
    }

    try {
      generaInterna(request,pdo,us);
    }
    finally {
      try {
        vu.setDbOpRestaConnessa(false);
        vu.disconnectClose();
      }
      catch (Exception e) {
      }
    }

  }

  /**
   * 
   */
  public void generaInterna(HttpServletRequest request, String pdo, String us) {
    String          area      = request.getParameter("area"),
                    cm        = request.getParameter("cm"),
                    dato      = request.getParameter("dato"),
                    campi     = request.getParameter("campi"),
                    area_blk  = request.getParameter("area_blocco"),
                    blocco    = request.getParameter("blocco");

    if (dato == null) {
      dato = "";
    }
    if (campi == null) {
      campi = "";
    }


    String ruolo = request.getParameter("ru");
    if (ruolo == null) {
      ruolo = "";
    }

    String modulo = request.getParameter("mo");
    if (modulo == null) {
      modulo = "";
    }

    String istanza = request.getParameter("is");
    if (istanza == null) {
      istanza = "";
    }

    String nominativo = request.getParameter("ul");
    if (nominativo == null) {
      nominativo = "";
    }

    pers = (Personalizzazioni)request.getSession().getAttribute("_personalizzazioni_gdm");
    if (pers == null) {
      try {
        pers = new Personalizzazioni("", us, vu);
      } catch (Exception e) {
        
      }
      request.getSession().setAttribute("_personalizzazioni_gdm",pers);
    }
    caricaInformazioniBlocco(area_blk, blocco,vu.getDbOp());
    String filtri = creaFiltri(request,area,cm,vu.getDbOp());
    String bloccoCalc = creaBlocco(request,vu.getDbOp());

    String newAction = "ServletPopup?area_blocco="+area_blk+"&amp;blocco="+blocco+"&amp;dato="+dato+"&amp;campi="+campi+"&amp;area="+area+
      "&amp;cm="+cm+"&amp;us="+us+"&amp;ru="+ruolo+"&amp;mo="+modulo+"&amp;is="+istanza+"&amp;ul="+nominativo;
    String caricaValori = "";
    String calcolaValori = "";
    String scriviValori = "";
    String varValori = "";
    String aggValori = "";
    String singleRadio = "";
    String primoValore = "";
    String chiudiWindow = "";
    boolean esci = false;
    StringTokenizer st = new StringTokenizer(ritorno, "@");
    String nextToken = "";
    StringTokenizer st2 = null;
    String nxT = "";
    if (campi.length() != 0) {
      st2 = new StringTokenizer(campi, ",");
    }
    while (st.hasMoreTokens() && !esci) {
      nextToken = st.nextToken();
      if (primoValore.length() == 0) {
        primoValore = nextToken;
      }
      if (separatore.length() == 0) {
        if (primoValore.equalsIgnoreCase(nextToken)) {
          aggValori += "          valore_"+nextToken+" = theForm."+nextToken+"[xx].value;\n";
          singleRadio += "          valore_"+nextToken+" = theForm."+nextToken+".value;\n";
        } else {
          aggValori += "          valore_"+nextToken+" = theForm."+nextToken+"_xx.value;\n";
          singleRadio += "          valore_"+nextToken+" = theForm."+nextToken+"_0.value;\n";
        }
      } else {
        aggValori += "          valore_"+nextToken+" = valore_"+nextToken+" + separatore + theForm."+nextToken+"_xx.value;\n";
      }
      varValori += "      var valore_"+nextToken+" = String(\"\");\n";
      if (dato.length() != 0) {
        if (st2 != null && st2.hasMoreTokens()) {
          nxT = st2.nextToken();
          caricaValori += "      valore_"+nextToken+" = window.parent.document.getElementById('submitForm')."+nxT+".value;\n";

          scriviValori += "      el = window.parent.document.getElementById('submitForm')."+nxT+";\n";
          scriviValori += "      if(el == null) {\n";
          scriviValori += "        i=0;\n";
          scriviValori += "        el = window.parent.document.getElementsByName('"+nxT+"'+String(i));\n";
          scriviValori += "        while (i <= 100 && el[0] != null) {\n";
          scriviValori += "          if (el[0].value == valore_"+nextToken+") {\n";
          scriviValori += "            el[0].checked = true;\n";
//          scriviValori += "            if (el[0].onclick != null) {\n";
//          scriviValori += "              el[0].onclick();\n";
//          scriviValori += "            }\n";
          scriviValori += "            i = 100;\n";
          scriviValori += "          } else {\n";
          scriviValori += "            el[0].checked = false;\n";
          scriviValori += "          }\n";
          scriviValori += "          i++;\n";
          scriviValori += "          el = window.parent.document.getElementsByName('"+nxT+"_'+String(i));\n";
          scriviValori += "        }\n";
          scriviValori += "      } else {\n";
          scriviValori += "        if (el[0] != null && el[0].type == 'radio') {\n";
          scriviValori += "          i = 0;\n";
          scriviValori += "          while (i <= 100 && el[i] != null) {\n";
          scriviValori += "            if (el[i].value == valore_"+nextToken+") {\n";
          scriviValori += "              el[i].checked = true;\n";
//          scriviValori += "              if (el[i].onclick != null) {\n";
//          scriviValori += "                el[i].onclick();\n";
//          scriviValori += "              }\n";
          scriviValori += "              i = 100;\n";
          scriviValori += "            } else {\n";
          scriviValori += "              el[i].checked = false;\n";
          scriviValori += "            }\n";
          scriviValori += "            i++;\n";
          scriviValori += "          }\n";
          scriviValori += "        } else {\n";
          scriviValori += "          el.value = valore_"+nextToken+";\n";
//          scriviValori += "          if (el.onchange != null) {\n";
//          scriviValori += "             el.onchange();\n";
//          scriviValori += "          }\n";
          scriviValori += "        }\n";
          scriviValori += "      }\n";
        } else {
          nxT = nextToken;
        }

//        scriviValori += "      window.parent.document.getElementById('submitForm')."+nxT+".value = valore_"+nextToken+";\n";
      } else {
        esci = true;
        scriviValori += "      window.parent.myInsertHTML(valore_"+nextToken+");\n";
      }
    }
    
    if (separatore.length() == 0) {
      calcolaValori += "      if (theForm."+primoValore+" != null) {\n"+
                       "      if (theForm."+primoValore+".checked) {\n"+
                       singleRadio + 
                       "         var separatore = String(\""+separatore+"\");\n"+
                       "      }}\n";
    }
    for (int i=0; i<record; i++) {
      if (separatore.length() == 0) {
        int j = i;
        if (isFiltro(primoValore)) {
          j = i+1;
        }
        calcolaValori += "      if (theForm."+primoValore+"["+j+"] != null) {\n"+
                         "      if (theForm."+primoValore+"["+j+"].checked) {\n";
        String newAgg = aggValori.replaceAll("_xx","_"+i);
        newAgg =  newAgg.replaceAll("xx",""+j);
        calcolaValori += newAgg +
                         "         var separatore = String(\""+separatore+"\");\n"+
                         "      }}\n";
      } else {
        calcolaValori += "      if (theForm."+primoValore+"_"+i+" != null) {\n"+
                         "      if (theForm."+primoValore+"_"+i+".checked) {\n"+
                         aggValori.replaceAll("_xx","_"+i) + 
                         "         var separatore = String(\""+separatore+"\");\n"+
                         "      }}\n";
      }
    }
//    corpoHtml = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n"+
    corpoHtml = "<html>\n"+
                "  <head>\n"+
                "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=windows-1252\" />\n"+
                "    <title>SceltaValori</title>\n"+
                "  </head>\n"+
                "  <script type='text/javascript'>\n";
    if (dato.length() != 0 && separatore.length() != 0) {
       corpoHtml +=          
                "    function aggiungiValore() {\n"+
                "      var separatore = String(\""+separatore+"\");\n"+
                "      var theForm = document.getElementById('mainForm');\n"+
                varValori+
                caricaValori+
                calcolaValori+
                scriviValori;
       if (controllo_js.length() != 0) {
         if (chiudi_popup.equalsIgnoreCase("S")) {
           corpoHtml += "      window.parent.execGDMfunc('"+controllo_js+"()');\n";
         } else {
           corpoHtml += "      window.parent.execGDMfunc('"+controllo_js+"()',window);\n";
         }
       } else {
         if (aggiorna.equalsIgnoreCase("S")) {
           corpoHtml += "      window.parent.addHiddenInput(parent.document.getElementById(\"submitForm\"));\n";
         }
       }
      corpoHtml += "    }\n";
    }
    if (separatore.length() == 0) {
      chiudiWindow = "\nchiudi();\n";
    }
    corpoHtml +=          
                "    function inserisciValore() {\n"+
                varValori+
                "      var separatore = String(\"\");\n"+
                "      var theForm = document.getElementById('mainForm');\n"+
                "      var coll = theForm.elements;\n"+
                calcolaValori+
                scriviValori;
    if (controllo_js.length() != 0) {
      if (chiudi_popup.equalsIgnoreCase("S")) {
        corpoHtml += "      window.parent.execGDMfunc('"+controllo_js+"()');\n";
      } else {
        corpoHtml += "      window.parent.execGDMfunc('"+controllo_js+"()',window);\n";
      }
    } else {
      if (aggiorna.equalsIgnoreCase("S")) {
        corpoHtml += "      window.parent.addHiddenInput(parent.document.getElementById(\"submitForm\"));\n";
      }
    }
    if (chiudi_popup.equalsIgnoreCase("S")) {
      corpoHtml += chiudiWindow;
    }
    corpoHtml +="    }\n"+
    			" function chiudi() {\n"+
    			"    parent.$(\"#dialog\").dialog(\"close\");\n"+
    			"    parent.$(\"#dialog\").html(\"\")\n"+
                "  }\n"+
                "  </script>\n"+
                "  <link href='Themes/AFC/Style.css' type='text/css' rel='stylesheet' />\n"+
                "  <body class='AFCPageBODY' >\n"+
                "    <form id ='mainForm' method='post' action='"+newAction+"'>\n"+
                filtri+
                "   \n"+bloccoCalc+"\n"+
                "    <div class='AFCFooterTD' style='text-align: right'>\n"+
//                "      <input class='AFCButton' type='submit' value='Aggiorna' name='B1' />\n"+
                "      <input class='AFCButton' type='button' value='Inserisci' id='B3' disabled onclick='inserisciValore();'/>\n";
    if (dato.length() != 0 && separatore.length() != 0) {
       corpoHtml +=          
                "      <input class='AFCButton' type='button' value='Aggiungi' id='B2' disabled onclick='aggiungiValore();'/>\n";
    } else {
      corpoHtml +=          
               "      <input class='AFCButton' type='button' value='Aggiungi' id='B2' style='display:none' disabled onclick='aggiungiValore();'/>\n";
   }
    corpoHtml +=          
                "      <input class='AFCButton' type='button' value='Chiudi' id='B4' onclick='chiudi();'/>\n"+
                "    </div>\n"+
                "    </form>\n"+
                "  </body>\n"+
                "</html>";

  }

  private void caricaInformazioniBlocco(String area, String blocco, IDbOperationSQL dbOpEsterna) {
    IDbOperationSQL dbOp = null;
    String query ="";
    String sDsn = "";
    ResultSet rst = null;
    BufferedInputStream	bis;
    StringBuffer sb = null;
    Element root, elp, elf;
    
    query = "SELECT CORPO, ISTRUZIONE, DRIVER, CONNESSIONE, "+
            "UTENTE, PASSWD, VALORE_RITORNO, RECORD_DA_VISUALIZZARE, "+
            "CAMPI_DI_RICERCA, SEPARATORE, TIPO, AUTOLOAD, FILTRI, "+
            "AGGIORNA_PADRE, DSN, FILTRI_ESTERNI, CONTROLLO_JS, CHIUDI_POPUP "+
            "FROM BLOCCHI_POPUP "+
            "WHERE BLOCCO = :BLOCCO " +
            "AND AREA = :AREA";

    try {
      dbOp = dbOpEsterna;
      dbOp.setStatement(query);
      dbOp.setParameter(":AREA", area);
      dbOp.setParameter(":BLOCCO", blocco);
      dbOp.execute();
      rst = dbOp.getRstSet();
      if (rst.next()) {
        boolean caricaConnEsterna=false;
        sDsn = rst.getString(15);
        if (sDsn == null || sDsn.length() == 0) {
          driver      = rst.getString(3);
          if (driver == null) {
            driver = "";
          }
          connessione = rst.getString(4);
          if (connessione == null) {
            connessione = "";
          }
          utente      = rst.getString(5);
          if (utente == null) {
            utente = "";
          }
          passwd      = rst.getString(6);
          if (passwd == null) {
            passwd = "";
          }
        } else {
          //lo faccio alla fine per poter riusare la stessa dbOp
          caricaConnEsterna=true;
        }
        ritorno     = rst.getString(7);
        record      = rst.getInt(8);
        ricerca     = rst.getString(9);
        if (ricerca == null) {
          ricerca = "";
        }
        filtri_g    = rst.getString(13);
        if (filtri_g == null) {
          filtri_g = "";
        }
        separatore  = rst.getString(10);
        if (separatore == null) {
          separatore = "";
        }
        tipo  = rst.getString(11);
        if (tipo == null) {
          tipo = "O";
        }
        autoload  = rst.getString(12);
        if (autoload == null) {
          autoload = "N";
        }
        aggiorna  = rst.getString(14);
        if (aggiorna == null) {
          aggiorna = "N";
        }
        filtri_esterni  = rst.getString(16);
        if (filtri_esterni == null) {
          filtri_esterni = "N";
        }
        controllo_js  = rst.getString(17);
        if (controllo_js == null) {
          controllo_js = "";
        }

        String persContr = pers.getPersonalizzazione(Personalizzazioni.LIBRERIA_CONTROLLI,area+"#"+controllo_js);
        int j = persContr.indexOf("#");
        controllo_js = persContr.substring(j+1);
        
        chiudi_popup  = rst.getString(18);
        if (chiudi_popup == null) {
          chiudi_popup = "S";
        }
        bis = dbOp.readClob(1);
        sb = new StringBuffer();
        int ic;
        while ((ic =  bis.read()) != -1) {
          sb.append((char)ic);
        }
        bloccoHtml = sb.toString();
        bis.close();
        bis = dbOp.readClob(2);
        sb = new StringBuffer();
        while ((ic =  bis.read()) != -1) {
          sb.append((char)ic);
        }
        queryBody = sb.toString();
        bis.close();

        if (caricaConnEsterna) {
          Connessione cn = new Connessione(dbOp,sDsn);
          driver      = cn.getDriver();
          connessione = cn.getConnessione();
          utente      = cn.getUtente();
          passwd      = cn.getPassword();
        }
      }
      if (filtri_esterni.equalsIgnoreCase("Y")) {
        query = "SELECT PARAMETRO, TIPO, VALORE_CAMPO, VALORE_DEFAULT, "+
                "LABEL, AREA_DOMINIO, DOMINIO, CONDIZIONE "+
                "FROM PARAMETRI_POPUP "+
                "WHERE BLOCCO = :BLOCCO " +
                "AND AREA = :AREA "+
                "ORDER BY SEQUENZA ASC";
        dbOp.setStatement(query);
        dbOp.setParameter(":AREA", area);
        dbOp.setParameter(":BLOCCO", blocco);
        dbOp.execute();
        rst = dbOp.getRstSet();

        root = DocumentHelper.createElement("PARAMETRI_ESTERNI");
        dDoc_Par = DocumentHelper.createDocument();
        dDoc_Par.setRootElement(root);
        
        int j = 0;
        while (rst.next()) {
          j++;
          elp = DocumentHelper.createElement("PAR_"+j);
          elp = aggFiglio(elp,"PARAMETRO",rst.getString("PARAMETRO"));
          elp = aggFiglio(elp,"TIPO",rst.getString("TIPO"));
          elp = aggFiglio(elp,"VALORE_CAMPO",rst.getString("VALORE_CAMPO"));
          elp = aggFiglio(elp,"VALORE_DEFAULT",rst.getString("VALORE_DEFAULT"));
          elp = aggFiglio(elp,"LABEL",rst.getString("LABEL"));
          elp = aggFiglio(elp,"AREA_DOMINIO",rst.getString("AREA_DOMINIO"));
          elp = aggFiglio(elp,"DOMINIO",rst.getString("DOMINIO"));
          elp = aggFiglio(elp,"CONDIZIONE",rst.getString("CONDIZIONE"));
          root.add(elp);
        }
        elp = DocumentHelper.createElement("RECORD");
        elp.setText(""+j);
        root.add(elp);
      }


      if (driver.length() != 0) {
        String compConn = completaConnessione(connessione);
        ConnessioneParser cp = new ConnessioneParser();
        String newConnessione = cp.bindingDeiParametri(compConn);
        if (newConnessione != null){
          connessione = newConnessione;
        }
      } else {
        driver      = Parametri.ALIAS;
        connessione = Parametri.SPORTELLO_DSN;
        utente      = Parametri.USER;
        passwd      = Parametri.PASSWD;
      }

    } catch(Exception e) {
      logger.error("SceltaValore::caricaInformazioniBlocco - Errore: "+e.toString());
    }
  }

  /**
   * 
   */
  private String creaBlocco(HttpServletRequest pRequest, IDbOperationSQL dbOpEsterna) {
    String retval = "";
    String myUrl = "";
    String contextPath = "";
    IDbOperationSQL dbOp = null;


    boolean nonCaricareDati = (pRequest.getParameter("gdm_reload") == null) && (!autoload.equalsIgnoreCase("S"));

    String us = (String)pRequest.getParameter("us");
    if (us == null) {
      us = "";
    }

    String ruolo = (String)pRequest.getParameter("ru");
    if (ruolo == null) {
      ruolo = "";
    }

    String modulo = (String)pRequest.getParameter("mo");
    if (modulo == null) {
      modulo = "";
    }

    String istanza = (String)pRequest.getParameter("is");
    if (istanza == null) {
      istanza = "";
    }

    String nominativo = (String)pRequest.getParameter("ul");
    if (nominativo == null) {
      nominativo = "";
    }

    if (Parametri.PROTOCOLLO.length() == 0) {
      myUrl = pRequest.getScheme();
    } else {
      myUrl = Parametri.PROTOCOLLO;
    }
    if (Parametri.SERVERNAME.length() == 0) {
      myUrl += "://"+pRequest.getServerName();
    } else {
      myUrl += "://"+Parametri.SERVERNAME;
    }
    if (Parametri.SERVERPORT.length() == 0) {
      myUrl += ":"+pRequest.getServerPort();
    } else {
      myUrl += ":"+Parametri.SERVERPORT;
    }

    contextPath = pRequest.getContextPath();

    if (tipo.equalsIgnoreCase("O")) {
      String campi = "", 
             tab = "", 
             leg = "", 
             ord = "",
             myAnd = "",
             campo = "",
             condizione = "",
             valore = "";
      String newFiltri = "";

      queryBody = queryBody.replaceAll("select","SELECT");
      queryBody = queryBody.replaceAll("distinct","DISTINCT");
      queryBody = queryBody.replaceAll("from","FROM");
      queryBody = queryBody.replaceAll("where","WHERE");
      queryBody = queryBody.replaceAll("order by","ORDER BY");
      queryBody = queryBody.replaceAll("\n", " ");
      queryBody = queryBody.replaceAll("\r", " ");
      int posSelect = queryBody.indexOf("SELECT");
      int posDistinct = queryBody.indexOf("DISTINCT");
      int posFrom = queryBody.indexOf("FROM");
      int posWhere = queryBody.indexOf("WHERE");
      int posOrder = queryBody.indexOf("ORDER BY");

      if (posDistinct == -1) {
        campi = queryBody.substring(posSelect+7,posFrom);
      } else {
        campi = queryBody.substring(posDistinct+8,posFrom);
      }
      if (posWhere > -1) {
        tab = queryBody.substring(posFrom+5,posWhere);
        if (posOrder > -1) {
          leg = queryBody.substring(posWhere+6,posOrder);
        } else {
          leg = queryBody.substring(posWhere+6);
        }
      } else {
        if (posOrder > -1 ) {
          tab = queryBody.substring(posFrom+5,posOrder);
        } else {
          tab = queryBody.substring(posFrom+5);
        }
      }
      if (posOrder > -1) {
        ord = queryBody.substring(posOrder+9);
      }

      if (filtri_esterni.equalsIgnoreCase("Y")) {
        Element root_p, elp_p;
        root_p = dDoc_Par.getRootElement();
        int num_par = 0;
        num_par =  Integer.parseInt(leggiValoreXML(dDoc_Par, "RECORD"));
        for (int i=1; i <= num_par; i++) {
          elp_p = leggiElementoXML(root_p, "PAR_"+i);
          campo = leggiValoreXML(elp_p, "PARAMETRO");
          condizione = leggiValoreXML(elp_p, "CONDIZIONE");
          valore = pRequest.getParameter(campo);
          if (valore == null) {
            valore = "";
          }
          if (leg.length() == 0) {
            myAnd = "";
          } else {
            myAnd = " AND ";
          }
          if (valore.length() != 0) {
            SceltaValoreParser ap = new SceltaValoreParser(pRequest, campo, valore);
            leg += myAnd + ap.bindingDeiParametri(condizione);
            myAnd = " AND ";
          }
        }
      } else {
        if (ricerca.length() != 0 && filtri_g.length() != 0) {
          newFiltri = ricerca+'@'+filtri_g;
        } else {
          if (ricerca.length() != 0) {
            newFiltri = ricerca;
          } else {
            newFiltri = filtri_g;
          }
        }
        if (newFiltri.length() != 0) {
          if (leg.length() == 0) {
            myAnd = "";
          } else {
            myAnd = " AND ";
          }
  
          int posChar = 0;
          String col  = "", 
                 nome = "",
                 temp = "";
          String ch;
          int parentesi = 0;
          colonna = new ArrayList();
          alias = new ArrayList();
          while (posChar < campi.length()) {
            ch = campi.substring(posChar,posChar+1);
            if (ch.equalsIgnoreCase("(")) {
              parentesi++;
            }
            if (ch.equalsIgnoreCase(")")) {
              parentesi--;
            }
            if (parentesi == 0) {
              if (ch.equalsIgnoreCase(" ")) {
                col = temp;
                temp = "";
                ch = "";
              }
              if (ch.equalsIgnoreCase(",")) {
                if (temp.replaceAll(" ","").length() == 0) {
                  nome = col;
                } else {
                  nome = temp;
                }
                if (col.length() == 0) {
                  col = nome;
                }
                temp = "";
                ch = "";
                colonna.add(col);
                alias.add(nome);
              }
            }
            temp = temp + ch;
            posChar++;
          }
          if (temp.replaceAll(" ","").length() == 0) {
            nome = col;
          } else {
            nome = temp;
          }
          if (col.length() == 0) {
            col = nome;
          }
          colonna.add(col);
          alias.add(nome);
  
          StringTokenizer st = new StringTokenizer(newFiltri,"@");
          while (st.hasMoreElements()) {
            campo = st.nextToken();
            int i = campo.indexOf("(");
            int j = campo.indexOf("[");
            if (i > -1 ) {
              if (j == -1 || j > i) {
                campo = campo.substring(0,i);
              } else {
                if (j > -1) {
                  campo = campo.substring(0,j);
                }
              }
            } else {
              if (j > -1) {
                campo = campo.substring(0,j);
              }
            }
            String newCampo = getColonna(campo);
  
            valore = pRequest.getParameter(campo);
            if (valore == null) {
              valore = "";
            }
            if (valore.length() != 0) {
              if (j == -1) {
                valore = "%'||trim('"+valore.replaceAll("'","''")+"')||'%";
                leg += myAnd + "UPPER(NVL(" + newCampo + ",DECODE('"+valore+"','%','%',''))) LIKE '"+valore.toUpperCase()+"' ";
                myAnd = " AND ";
              } else {
                valore = valore.replaceAll("'","''");
                leg += myAnd + "UPPER(NVL(" + newCampo + ",'')) = trim('"+valore.toUpperCase()+"') ";
                myAnd = " AND ";
              }
            }
          }
        }
      }

      if (posDistinct > -1) {
        campi = "DISTINCT " + campi;
      }

      try {
        dbOp = SessioneDb.getInstance().createIDbOperationSQL(driver, connessione, utente, passwd); 
        Multirecord mr = new Multirecord(pRequest, "BLK", bloccoHtml, campi, tab, leg, ord, record);
        if (separatore.length() == 0) {
          mr.setMono(true);
        }
        mr.setWait(true,"");
        mr.setRicerca(true,ritorno);
        mr.setNonCaricareDati(nonCaricareDati);
        retval = mr.creaHtml(dbOp,pRequest,true);
      } catch (Exception e){
        retval = "Errore: "+e.toString();
      }
      finally {
        free(dbOp);
      }
    } 
    if (tipo.equalsIgnoreCase("J")) {
      try {
        Element elp, elf, root;

        root = dDoc.getRootElement();

        elp = DocumentHelper.createElement("CONNESSIONE_DB");
        root.add(elp);
        elf = DocumentHelper.createElement("USER");
        elf.setText(utente);
        elp.add(elf);
        elf = DocumentHelper.createElement("PASSWORD");
        elf.setText(passwd);
        elp.add(elf);
        elf = DocumentHelper.createElement("HOST_STRING");
        elf.setText(connessione);
        elp.add(elf);
        elf = DocumentHelper.createElement("DRIVER");
        elf.setText(driver);
        elp.add(elf);

        elp = DocumentHelper.createElement("CONNESSIONE_TOMCAT");
        root.add(elp);
        elf = DocumentHelper.createElement("UTENTE");
        elf.setText(us);
        elp.add(elf);
        elf = DocumentHelper.createElement("NOMINATIVO");
        elf.setText(nominativo);
        elp.add(elf);
        elf = DocumentHelper.createElement("RUOLO");
        elf.setText(ruolo);
        elp.add(elf);
        elf = DocumentHelper.createElement("MODULO");
        elf.setText(modulo);
        elp.add(elf);
        elf = DocumentHelper.createElement("ISTANZA");
        elf.setText(istanza);
        elp.add(elf);
        elf = DocumentHelper.createElement("PROPERTIES");
        elf.setText(inifile.replaceAll("\\\\","/"));
        elp.add(elf);
        elf = DocumentHelper.createElement("URL_SERVER");
        elf.setText(myUrl);
        elp.add(elf);
        elf = DocumentHelper.createElement("CONTEXT_PATH");
        elf.setText(contextPath);
        elp.add(elf);
       
        String sFinput = dDoc.asXML();
        Multirecord mr = new Multirecord(pRequest, "BLK", bloccoHtml, tipo, queryBody, sFinput, record);
        if (separatore.length() == 0) {
          mr.setMono(true);
        }
        mr.setWait(true,"");
        mr.setRicerca(true,ritorno);
        mr.setNonCaricareDati(nonCaricareDati);
        retval = mr.creaHtmlXML(dbOpEsterna,pRequest,null,true);
      } catch (Exception e){
        retval = "Errore: "+e.toString();
      }
    }
    if (tipo.equalsIgnoreCase("P")) {
      try {
        dbOp = SessioneDb.getInstance().createIDbOperationSQL(driver, connessione, utente, passwd); 
        Element elp, elf, root;

        root = dDoc.getRootElement();

        elp = DocumentHelper.createElement("CONNESSIONE_TOMCAT");
        root.add(elp);
        elf = DocumentHelper.createElement("UTENTE");
        elf.setText(us);
        elp.add(elf);
        elf = DocumentHelper.createElement("NOMINATIVO");
        elf.setText(nominativo);
        elp.add(elf);
        elf = DocumentHelper.createElement("RUOLO");
        elf.setText(ruolo);
        elp.add(elf);
        elf = DocumentHelper.createElement("MODULO");
        elf.setText(modulo);
        elp.add(elf);
        elf = DocumentHelper.createElement("ISTANZA");
        elf.setText(istanza);
        elp.add(elf);
        elf = DocumentHelper.createElement("PROPERTIES");
        elf.setText(inifile.replaceAll("\\\\","/"));
        elp.add(elf);
        elf = DocumentHelper.createElement("URL_SERVER");
        elf.setText(myUrl);
        elp.add(elf);
        elf = DocumentHelper.createElement("CONTEXT_PATH");
        elf.setText(contextPath);
        elp.add(elf);
       
        String sFinput = dDoc.asXML();
        Multirecord mr = new Multirecord(pRequest, "BLK", bloccoHtml, tipo, queryBody, sFinput, record);
        if (separatore.length() == 0) {
          mr.setMono(true);
        }
        mr.setWait(true,"");
        mr.setRicerca(true,ritorno);
        mr.setNonCaricareDati(nonCaricareDati);
        retval = mr.creaHtmlXML(dbOp,pRequest,null,true);
      } catch (Exception e){
        retval = "Errore: "+e.toString();
      }
      finally {
        free(dbOp);
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
  public String getColonna(String pCodice) {
    String  retval = "";
//    String  codice = "";
    boolean trovato = false;
    int     i = 0;

    while ((i<alias.size()) && (!trovato)) {
      if (alias.get(i).equals(pCodice)) {
        retval = (String) colonna.get(i);
        trovato = true;
      }
      i++;
    }
    if (trovato) {
      return retval;
    } else {
      return pCodice;
    }
  }

  private String leggiLabel(String area, String cm, String campo, IDbOperationSQL dbOpEsterna) {
    IDbOperationSQL dbOp = null;
    String query ="";
    ResultSet rst = null;
    String retval = campo;
    query = "SELECT NVL(D.LABEL,D.DATO) "+
            "  FROM DATI D, DATI_MODELLO DM "+
            " WHERE D.AREA = DM.AREA_DATO "+
            " AND D.DATO = DM.DATO "+
            "  AND DM.AREA = :AREA "+
            "  AND DM.CODICE_MODELLO = :CM "+
            "  AND DM.DATO = :CAMPO ";
    try {
      dbOp = dbOpEsterna;
      dbOp.setStatement(query);
      dbOp.setParameter(":AREA", area);
      dbOp.setParameter(":CM", cm);
      dbOp.setParameter(":CAMPO", campo);
      dbOp.execute();
      rst = dbOp.getRstSet();
      if (rst.next()) {
        retval = rst.getString(1);
      }
    } catch(Exception e) {
      logger.error("SceltaValore::leggiLabel - Errore: "+e.toString());
    }
    return retval;
  }

  /**
   * 
   */
  private boolean isFiltro(String sPrimoVal) {
    String campo = "";
    boolean retval = false;

    if (filtri_g.length() != 0) {
      StringTokenizer st = new StringTokenizer(filtri_g,"@");
      while (st.hasMoreElements()) {
        campo = st.nextToken();
        int i = campo.indexOf("(");
        if (i > -1) {
          campo = campo.substring(0,i);
        }
        if (campo.equalsIgnoreCase(sPrimoVal)) {
          retval = true;
        }
      }
    }
    if (ricerca.length() != 0) {
      StringTokenizer st = new StringTokenizer(ricerca,"@");
      while (st.hasMoreElements()) {
        campo = st.nextToken();
        int i = campo.indexOf("(");
        if (i > -1) {
          campo = campo.substring(0,i);
        }
        if (campo.equalsIgnoreCase(sPrimoVal)) {
          retval = true;
        }
      }
    }
    return retval;
  }
    
  private static String leggiValoreXML(Document xmlDocument, String tagName) {
    String valore = null;
    if(xmlDocument == null) {
      System.out.println("xml document null");
    }
    Element root = xmlDocument.getRootElement();
    for(Iterator iterator = root.elementIterator(); iterator != null && iterator.hasNext() && valore == null;) {
      Element elemento = (Element)iterator.next();
      if(elemento != null && elemento.getName().equals(tagName)) {
        valore = elemento.getText();
      } else {
        valore = leggiValoreXML(elemento, tagName);
      }
    }
    return valore;
  }

  private static String leggiValoreXML(Element e, String tagName) {
    String valore = null;
    for(Iterator iterator = e.elementIterator(); iterator != null && iterator.hasNext() && valore == null;) {
      Element elemento = (Element)iterator.next();
      if(elemento != null && elemento.getName().equals(tagName)) {
        valore = elemento.getText();
      } else {
        valore = leggiValoreXML(elemento, tagName);
      }
    }
    return valore;
  }

  private static Element leggiElementoXML(Element e, String tagName) {
    Element elemento = null, eFound = null;;
    for(Iterator iterator = e.elementIterator(); iterator != null && iterator.hasNext() && eFound == null;) {
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
    if (valore == null) {
      valore = "";
    }
    elf.setText(valore);
    elp.add(elf);
    return elp;
  }
  
}