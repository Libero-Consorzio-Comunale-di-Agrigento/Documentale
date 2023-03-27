package it.finmatica.modutils.attributicampo;

import it.finmatica.modutils.informazionicampo.InformazioniCampo;

public class AttributiCampo {
  private String  area_dato = "";
  private String  dato = "";
  private String  blocco = "";
  private String  tipoCampo = "";
  private String  tipoAccesso = "";
  private String  tipoCalcolato = "N";
  private String  lunghezza = "";
  private String  decimali = "";
//  private String  carattere = "";
  private String  style = "";
  private String  className = "AFCInput";
  private String  href = "";
  private String  funzione = "";
//  private String  dimensione = "";
//  private String  colore = "";
  private String  metaData = "";
  private String  nomeDato = "";
  private String  adsFormat = "";
  private String  elementiAjax = "";

  public AttributiCampo(String pCampoHtml) throws Exception {
    String metaInfo = "";
    String newCampo = "";
    String tagDato = "";
    String tagStyle = "";
    String tagClass = "AFCInput";
    int i,j;
    i = pCampoHtml.indexOf("name=\"")+6;
    j = pCampoHtml.indexOf("\"",i);
    metaInfo = pCampoHtml.substring(i,j);
    i = pCampoHtml.indexOf("style=\"");
    if (i > -1) {
      i = i + 7;
      j = pCampoHtml.indexOf("\"",i);
      tagStyle = pCampoHtml.substring(i,j);
    } 

    i = pCampoHtml.indexOf("class=\"");
    if (i > -1) {
      i = i + 7;
      j = pCampoHtml.indexOf("\"",i);
      tagClass = pCampoHtml.substring(i,j);
    } 

    if (pCampoHtml.indexOf("<input") > -1) {
      i = pCampoHtml.indexOf("value=\"");
      if (i > -1) {
        i = i + 7;
        j = pCampoHtml.indexOf("\"",i);
        tagDato = pCampoHtml.substring(i,j);
      }
      if (tagDato.equalsIgnoreCase("on")) {
        tagDato = "";
      }
    }

    if (pCampoHtml.indexOf("<textarea") > -1) {
      i = pCampoHtml.indexOf(">");
      if (i > -1) {
        i = i + 1;
        j = pCampoHtml.indexOf("</textarea",i);
        tagDato = pCampoHtml.substring(i,j);
      }
    }

    newCampo = "&lt;<!-- ADSPROPERTY "+metaInfo+" ADSFORMAT -->"+
               tagDato+"&gt;<!-- ADSTFE -->";
//    if (!tagStyle.equalsIgnoreCase("")) {
      newCampo += "<!-- style=\""+tagStyle+"\" class=\""+tagClass+"\"-->";
//    }
    adsFormat = "&lt;<!-- ADSTFB -->"+newCampo+"&gt;";
    
    InformazioniCampo infoC = new InformazioniCampo(newCampo,"ADSFORMAT -->");
    area_dato = infoC.getAreaDato();
    dato =  infoC.getDato();
    if (tagDato.equalsIgnoreCase("")) {
      tagDato = dato;
    }
    blocco = infoC.getBlocco();
    tipoCampo = infoC.getTipoCampo();
    tipoAccesso = infoC.getTipoAccesso();
    tipoCalcolato = infoC.getTipoCampoCalcolato();
    lunghezza = infoC.getLunghezza();
    decimali = infoC.getDecimali();
    className = infoC.getClassName();
    style = infoC.getStile();
    href = infoC.getHref();
    funzione = infoC.getFunc();
//    dimensione = infoC.getDimensione();
//    colore = infoC.getColore();
    metaData = infoC.getMetaData();
    nomeDato = infoC.getNomeDato();
    elementiAjax = infoC.getElementiAjax();
  }

  /**
   * 
   */
  public String getAdsFormat() {
    return adsFormat;
  }
  
  /**
   * 
   */
  public String getDato() {
    return dato;
  }
  
  /**
   * 
   */
  public String getTipoCampoCalcolato() {
    return tipoCalcolato;
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
  public String getMetaData() {
    String retval = "";
    retval = "_"+metaData+"_"+getDato();
    if (!getBlocco().equalsIgnoreCase("")) {
      retval += "!"+getBlocco();
    }
    if (!getAreaDato().equalsIgnoreCase("")) {
      retval += "*"+getAreaDato();
    }
    return retval;
  }
  
  /**
   * 
   */
  public String getNomeDato() {
    return nomeDato;
  }
  
  /**
   * 
   */
  public String getLunghezza() {
    return lunghezza;
  }
  
  /**
   * 
   */
  public String getDecimali() {
    return decimali;
  }
  
  /**
   * 
   */
  public String getDescTipo() {
    if (tipoCampo.equalsIgnoreCase("S")) {
      return "Standard";
    }
    if (tipoCampo.equalsIgnoreCase("T")) {
      return "TextArea";
    }
    if (tipoCampo.equalsIgnoreCase("Z")) {
      return "RichTextArea";
    }
    if (tipoCampo.equalsIgnoreCase("C")) {
      return "ComboBox";
    }
    if (tipoCampo.equalsIgnoreCase("B")) {
      return "CheckButton";
    }
    if (tipoCampo.equalsIgnoreCase("R")) {
      return "RadioButton";
    }
    if (tipoCampo.equalsIgnoreCase("H")) {
      return "Nascosto";
    }
    return "Standard";
  }
  
  /**
   * 
   */
  public String getElementiAjax() {
    return elementiAjax;
  }
  
  /**
   * 
   */
  public String getAreaDato() {
    return area_dato;
  }
  
  /**
   * 
   */
  public String getBlocco() {
    return blocco;
  }
  
//  /**
//   * 
//   */
//  public String getCarattere() {
//    return carattere;
//  }
//  
  /**
   * 
   */
  public String getHref() {
    return href;
  }
  
  /**
   * 
   */
  public String getFunc() {
    return funzione;
  }
  
  /**
   * 
   */
  public String getStile() {
    return style;
  }
  
//  /**
//   * 
//   */
//  public String getDimensione() {
//    return dimensione;
//  }
//  
//  /**
//   * 
//   */
//  public String getColore() {
//    return colore;
//  }
  
  /**
   * 
   */
  public String getClassName() {
    return className;
  }
}