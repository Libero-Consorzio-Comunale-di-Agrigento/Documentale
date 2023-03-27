package it.finmatica.modulistica.modulisticapack;
import java.net.URLEncoder;
import java.util.*;
import javax.servlet.http.*;
import it.finmatica.modulistica.parametri.Parametri;
//import it.finmatica.jfc.bcUtil.*;
import it.finmatica.textparser.AbstractParser;
import it.finmatica.dmServer.management.*;
import org.apache.log4j.Logger;
   
public class ModulisticaParser extends AbstractParser {
  HttpServletRequest pRequest = null;
  private static Logger logger = Logger.getLogger(ModulisticaParser.class);
  
  public ModulisticaParser(HttpServletRequest request) {
    pRequest = request;
  }

  protected String findParamValue(String nomePar, Properties extraKeys) {
    String  cr;
    String  valore = null;
//    String id_session = null;

//    ar = pRequest.getParameter("area");
    cr = pRequest.getParameter("cr");
    if (cr == null) {
//      id_session = ar+'-'+pRequest.getSession().getId();
//      cr = id_session;
      cr = (String )pRequest.getSession().getAttribute("key");
    } /*else {
//      id_session = ar+'-'+cr+'-'+pRequest.getSession().getId();
    }*/
    
    // Caso particolare: binding per la variabile "cr"
    if (nomePar.toLowerCase().equals("cr")) {
      // Lo "username" è un parametro di default
      valore = cr;
      return valore; 
    }

    // Caso particolare: binding per la variabile "username"
    if (nomePar.toLowerCase().equals("username")) {
      // Lo "username" è un parametro di default
      valore = pRequest.getRemoteUser();
			return newValore(valore);
    }

    // Il parametro potrebbe essere stato passato via URL
    if (pRequest == null) {
      logger.error("Dominio::findParamValue() - Attenzione! Si è verificato un errore GRAVE: la request è nulla!");
    }
    HttpSession   httpSess = pRequest.getSession();
//    valore = (String)httpSess.getAttribute("url_"+nomePar); 
    if (valore == null) {
      valore = "";
    }

    Dati dati = (Dati)httpSess.getAttribute("gdm_nuovi_valori_doc");
    if (dati != null) {
      valore = dati.getValore(nomePar);

      if (valore != null) {
      	return newValore(valore);
      }
    }

    if (valore == null) {
      valore = "";
    }

    // Se ancora non è stato fatto il binding, provo a vedere se c'è il valore
    // associato ad una variabile di sessione.
    if (valore.length() == 0) {
      valore = (String)httpSess.getAttribute(nomePar); // Parametro associato alla sessione
      if (valore == null) {
        valore = "";
      }
      if (valore.length() == 0) {
        valore = pRequest.getParameter(nomePar);
        if (valore == null) {
          // Vedo se è un campo legato ad un CAMPO di tipo CHECKBOX
        	String check = pRequest.getParameter("gdm_campi_check");
        	if (check != null && check.indexOf("@"+nomePar+"@") > -1) {
	        	valore = "";
	          StringBuffer sb = new StringBuffer(valore);
	          for (int j=0; j<1000; j++) {  // 1000 valori al massimo.
	            String s = pRequest.getParameter(nomePar+"_"+Integer.toString(j));
	            if (s != null){
	              // Attacco sempre un separatore in fondo, in tal modo quando dovrò
	              // recuperare un valore lo richiamerò sempre utilizzando il separatore
	              //valore = valore + s + Parametri.SEPARAVALORI;
	            	sb.append(s);
	            	sb.append(Parametri.SEPARAVALORI);
	            }
	          }
	          valore = sb.toString();
	          if (valore.length() == 0) {
	            valore = null;
	          }
        	}
        }

        // Se non è nemmeno nella sessione tento il binding sul database
        if (valore == null) {
          AccediDocumento ad = (AccediDocumento)httpSess.getAttribute("valori_doc");
          if (ad != null) {
            try {
              valore = ad.leggiValoreCampo(nomePar);
            } catch (Exception eval) {
              loggerError("ModulisticaParser::findParamValue() - Attenzione! Si è verificato un errore. Nel campo "+nomePar+" !",eval);
              valore = null;
            }
          }

          if (valore == null) {
            // Ricerco nei doumenti precedenti/successivi
            LinkedList lValPrec = null;
            Object attributo = pRequest.getSession().getAttribute("valori_modello_precedente");
            if (attributo != null) {
              lValPrec = (LinkedList)attributo;
              int i = 0;
              int j = lValPrec.size();
              while (( i < j) && (valore == null)) {
                ad = (AccediDocumento)lValPrec.get(i);
                try {
                  valore = ad.leggiValoreCampo(nomePar);
                } catch (Exception e) {
                  valore = null;
                }
                i++;
              }
            }
            
            if (valore == null) {

              Dominio dp = null;
              ListaDomini ld = (ListaDomini)pRequest.getSession().getAttribute("listaDomini");
              if (ld != null) {
                int numDom = ld.domini.size();
                int i = 0;
                while (i < numDom && valore == null) { 
                  dp = (Dominio)ld.domini.get(i);
                  if (dp.isDominioDelModello() || dp.isDominioDiArea()) {
                    valore = dp.getValore(nomePar);
                  }
                  i++;
                }
              }
              if (valore != null) {
                return newValore(valore);
              }
            }
          }
        }
      }
    }
    return newValore(valore);
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
//  private String newValore(String pValore, String pPar ) {
//    String sRitorno = "";
//    try {
//      sRitorno = extraKeys.getProperty("GDM_RITORNA_PARAMETRO");
//    } catch (Exception e) {
//      sRitorno = "N";
//    }
//    if (pValore == null) {
//      if (sRitorno.equalsIgnoreCase("S")) {
//        return pPar;
//      } else {
//        return pValore;
//      }
//    }
//    String sTipo = "";
//    try {
//      sTipo = extraKeys.getProperty("TIPO");
//    } catch (Exception e) {
//      sTipo = "";
//    }
//    if (sTipo == null) {
//      return pValore;
//    }
//    if (sTipo.equalsIgnoreCase("S")) {
//      return pValore.replaceAll("'","''");
//    }
//    if (sTipo.equalsIgnoreCase("J")) {
//      return pValore.replaceAll("\"","\"\"");
//    }
//    return pValore;
//  }
  private String newValore(String pValore) {
    if (pValore == null) {
      return pValore;
    }
    String sTipo = "";
    try {
      sTipo = extraKeys.getProperty("TIPO");
    } catch (Exception e) {
      sTipo = "";
    }
    if (sTipo == null) {
      return pValore;
    }
    if (sTipo.equalsIgnoreCase("S")) {
      return pValore.replaceAll("'","''");
    }
    if (sTipo.equalsIgnoreCase("J")) {
      return pValore.replaceAll("\"","\"\"");
    }
    
    try {
      if (sTipo.equalsIgnoreCase("BLK")) {
      	return URLEncoder.encode(pValore, "UTF-8");
      }
    } catch (Exception e) {}
    
    return pValore;
  }
}