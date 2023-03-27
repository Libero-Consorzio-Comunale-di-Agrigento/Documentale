package it.finmatica.modulistica.modulisticapack;

import java.net.URLDecoder;
import java.sql.ResultSet;
import java.util.*;
import javax.servlet.http.*;
import it.finmatica.dmServer.management.AccediDocumento;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import it.finmatica.jfc.dbUtil.SessioneDb;
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.textparser.AbstractParser;
import org.apache.log4j.Logger;

public class ControlliParser extends AbstractParser {
  HttpServletRequest pRequest = null;
  String iddoc, cr, xml, stato_doc; 
  boolean bDocumetale;
  boolean isAjax;
  private static Logger logger = Logger.getLogger(ControlliParser.class);

  public ControlliParser(HttpServletRequest request, String piddoc, String pcr, String pxml, boolean pbDocumetale, String pstato_doc, boolean pisAjax) {
    pRequest = request;
     iddoc = piddoc;
     cr = pcr;
     xml = pxml;
     bDocumetale = pbDocumetale;
     stato_doc = pstato_doc;
     isAjax = pisAjax;
  }

  protected String findParamValue(String nomePar, Properties extraKeys) {
    String val = null, dato;              
    // Sostituisco il valore del parametro
    dato = nomePar;
    try {
      if (dato.equalsIgnoreCase("xml")) {
        val = xml;
      } else {
        if (dato.equalsIgnoreCase("cr")) {
          val = cr;
        } else {
          if (dato.equalsIgnoreCase("iddoc")) {
            val = iddoc;
          } else {
            // Caso particolare: binding per la variabile "username"
            if (dato.toLowerCase().equals("username")) {
              // Lo "username" è un parametro di default
              val = pRequest.getRemoteUser();
            } else {
              if (dato.equalsIgnoreCase("$STATO")) {
                val = stato_doc;
              } else {
                if (dato.equalsIgnoreCase("area")) {
                  val = pRequest.getParameter("area");
                } else {
                  if (dato.equalsIgnoreCase("cm")) {
                    val = pRequest.getParameter("cm");
                  } else {
                    HttpSession   httpSess = pRequest.getSession();
                    val = (String)httpSess.getAttribute(dato);
                    if (val == null) {
                      Dati dati = (Dati)pRequest.getSession().getAttribute("gdm_nuovi_valori_doc");
                      if (dati != null) {
                        val = dati.getValore(dato);
                      }
                      if (val == null) {
                        val = (String)pRequest.getParameter(dato);
                        if (val == null) {
                          val = leggiValore(pRequest,dato);
                        } else {
                        	if (isAjax) {
                        		val = URLDecoder.decode(val,"UTF-8");
                        	}
                        }
                        //Qua inserisco leggo da documentale per Formula Modello
                        if (val.length() == 0 && bDocumetale) {
                          AccediDocumento ad = (AccediDocumento)pRequest.getSession().getAttribute("valori_doc");
                          if (ad != null) {
                            val = ad.leggiValoreCampo(dato);
                          }
                        }
                        if (val != null) {
                          val = val.replaceAll("'","''");
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
      
    } catch (Exception eval) {
      loggerError("ControlliParser::findParamValue() - Attenzione! Si è verificato un errore. Nel parametro "+nomePar+" !",eval);
      val = null;
    }
    if (val == null) {
      val = "";
    }
    return val;
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

    try {
      dbOpC = SessioneDb.getInstance().createIDbOperationSQL(Parametri.JINDIDBNAME,0);
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
    }
    finally {
      free(dbOpC);
    }
    if (tipoDato == null) {
      return "";
    }

    if (tipoDato.charAt(0) != 'B') {            // new!  
      // E' un campo legato ad un dominio, ma non è di tipo CHECKBOX
      result = request.getParameter(dato);
    } else {
      // E' un campo legato ad un CAMPO di tipo CHECKBOX
      result = "";
      for (int j=0; j<1000; j++) {  // 1000 valori al massimo.
        String s = request.getParameter(dato+"_"+Integer.toString(j));
        if (s != null) {
          // Attacco sempre un separatore in fondo, in tal modo quando dovrò
          // recuperare un valore lo richiamerò sempre utilizzando il separatore
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
 private void free(IDbOperationSQL dbOp) {
   try {
     dbOp.close();
   } catch (Exception e) { }
 }

}
