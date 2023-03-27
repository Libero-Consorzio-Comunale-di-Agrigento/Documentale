package it.finmatica.modulistica.modulisticapack;
import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import javax.servlet.http.HttpServletRequest;
//import java.util.*;
//import java.sql.*;
//import java.io.*;
import it.finmatica.modulistica.parametri.Parametri;
import it.finmatica.modutils.informazioniSettore.InformazioniSettore;
//import org.apache.log4j.Logger;
 
public class VisualizzaHTML implements IElementoModello {
//  private Parametri pPm = null;
  private String sHtml = "";
  private String sTagSettore = "";
  private String sSettore = "";
  private String esito = "";
  private boolean protetto = false;
//  private static Logger logger = Logger.getLogger(VisualizzaHTML.class);

//  public VisualizzaHTML(HttpServletRequest pRequest, String pArea, String codiceModello, String pHtml, Parametri pm) throws Exception {
  public VisualizzaHTML(HttpServletRequest pRequest, String pArea, String codiceModello, String pHtml, IDbOperationSQL dbOpEsterna) throws Exception {
    String domVisu = "";
//    pPm = pm;
    sTagSettore = pHtml;
    InformazioniSettore is = new InformazioniSettore(pHtml,Parametri.getNameVisualBegin(),Parametri.getNameVisualEnd(),Parametri.getTagVisualEnd());
    domVisu = is.getDominio();
    sSettore = is.getSettore();
    protetto = false;
    Dominio dp = null;
    ListaProtetti lp = (ListaProtetti)pRequest.getSession().getAttribute("listaProtetti");
    String myVal = null;
    if (lp != null) {
      int numDom = lp.domini.size();
      int i = 0;
      while (i < numDom && myVal == null) { 
        dp = (Dominio)lp.domini.get(i);
          myVal = dp.getValore("$S$"+sSettore);
        i++;
      }
    }
    if (myVal != null) {
      if (myVal.equalsIgnoreCase("S")) {
        protetto = true;
      }
    }

    DominioVisualizza dominioVisu = new DominioVisualizza(pArea, domVisu, "-", "", null, "blocco", pRequest,dbOpEsterna);
    esito = dominioVisu.getValore("blocco");
    if (esito == null) {
      esito = "";
    }
    if (esito.length() == 0) {
      sHtml = "<span id='"+sSettore+"' style='display: none' >\n";
    } else {
      sHtml = "<span id='"+sSettore+"' >\n";
    }
  }

  public  void release() {
  }

  public void settaListFields(String l_fields){
  }

//  public Parametri getParametri() {
//    return pPm;
//  }
  
  public String getSettore() {
    return sSettore;
  }
  
  public String getTagSettore() {
    return sTagSettore;
  }
  
  public boolean getProtetto() {
    return protetto;
  }
  
  public String getAjaxValue() {
    if (esito.length() == 0) {
      return "<span id='gdmIdAjax_"+sSettore+"'>none</span>";
    } else {
      return "<span id='gdmIdAjax_"+sSettore+"'></span>";
    }
  }

  public String getValue() {
    return sHtml;
  }

  public String getValue(IDbOperationSQL dbOpEsterna) {
    return sHtml;
  }
  public String getZValue() {
    return null;
  }

  public String getPRNValue() {
    return getPRNValue(null);
  }

  public String getPRNValue(IDbOperationSQL dbOpEsterna) {
    return  sHtml;
  }
  public String getPRNComValue() {
    return  sHtml;
  }
  public String getPRNComValue(IDbOperationSQL dbOpEsterna) {
    return  sHtml;
  }

  public void settaProtetto(boolean b_protetto) {
  }

}