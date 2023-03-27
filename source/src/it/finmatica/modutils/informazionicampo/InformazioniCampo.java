package it.finmatica.modutils.informazionicampo;

import java.util.StringTokenizer;

public class InformazioniCampo {
  private String  area_dato = "";
  private String  area_cm_dato = "";
  private String  cm_dato = "";
  private String  categoria_dato = "";
  private String  dato = "";
  private String  blocco = "";
  private String  tipoCampo = "";
  private String  tipoAccesso = "";
  private String  tipoCalcolato = "N";
  private String  lunghezza = "";
  private String  decimali = "";
  private String  style = "";
  private String  classname = "AFCInput";
  private String  href = "";
  private String  funzione = "";
  private String  metaData = "";
  private String  nomeDato = "";
  private String  elementiAjax = "";

  public InformazioniCampo(String pCampoHtml, String pFieldNameEnd) throws Exception {
    String metaInfo = "";
    String stile = "";
    String link = "";
    String func = "";
    int inizioMeta, fineMeta, fineDato, inizioStile, inizioHref, inizioFunc;
    int inizioNome, fineNome;

    inizioMeta = pCampoHtml.indexOf("_");
    fineMeta = pCampoHtml.indexOf("_",inizioMeta+1);
    fineDato = pCampoHtml.indexOf(pFieldNameEnd);
    inizioNome = fineDato + pFieldNameEnd.length();
    fineNome = pCampoHtml.indexOf("&gt;");
    if (fineNome > -1) {
      nomeDato = pCampoHtml.substring(inizioNome,fineNome);
    } else {
      nomeDato = "";
    }

    if (fineMeta == -1 || inizioMeta == -1 || fineDato == -1) {
      throw new Exception ("Campo non identificabile all'interno di ["+pCampoHtml+"]");
    }

    dato = pCampoHtml.substring(fineMeta+1,fineDato-1);
    int ain = dato.indexOf("{");
    int afi = dato.indexOf("}");
    if (ain > -1 && afi > -1) {
      elementiAjax = dato.substring(ain+1,afi);
      dato = dato.substring(0,ain);
    }
    int j = dato.indexOf("*");
    if (j > -1) {
      area_dato = dato.substring(0,j);
      dato = dato.substring(j+1);
    } 
    int i = dato.indexOf("!");
    if (i > -1) {
      blocco = dato.substring(i+1);
      dato = dato.substring(0,i);
    }
    int x = dato.indexOf("<!--");
    if (x > -1) {
      dato = dato.substring(0,x);
    }
    int k = 0;
    String  v1 = "",
            v2 = "",
            v3 = "";
    StringTokenizer st = new StringTokenizer(dato,"#");
    while (st.hasMoreTokens()) {
      k = k + 1;
      if (k == 1) {
        v1 = st.nextToken();
      }
      if (k == 2) {
        v2 = st.nextToken();
      }
      if (k == 3) {
        v3 = st.nextToken();
      }
    }
//    area_cm_dato = "";
//    cm_dato = "";
//    categoria_dato = "";
//    if (k == 2) {
//      nomeDato = v1;
//      categoria_dato = v2;
//    }
//    if (k == 3) {
//      nomeDato = v1;
//      area_cm_dato = v2;
//      cm_dato = v3;
//    }
    try {
      inizioStile = pCampoHtml.indexOf("<!-- style=");
      if (inizioStile > -1) {
        stile = pCampoHtml.substring(inizioStile);
        settaStile(stile);
      }
    } catch (Exception e) {
      throw new Exception ("Errore nell'interpretazione dello stile ["+stile+"]: "+e.toString());
    }

    inizioHref = pCampoHtml.indexOf("<!-- link=");
    if (inizioHref > -1) {
      link = pCampoHtml.substring(inizioHref);
      settaHref(link);
    }

    inizioFunc = pCampoHtml.indexOf("<!-- func=");
    if (inizioFunc > -1) {
      func = pCampoHtml.substring(inizioFunc);
      settaFunc(func);
    }

    metaInfo = pCampoHtml.substring(inizioMeta+1,fineMeta);
    metaData = metaInfo;
    if (metaInfo.length() == 1) {
      nomeDato = dato;
      if (k == 2) {
        nomeDato = v1;
        categoria_dato = v2;
      }
      if (k == 3) {
        nomeDato = v1;
        area_cm_dato = v2;
        cm_dato = v3;
      }
      settaInfo1(metaInfo);
      return;
    }
    if (metaInfo.length() == 4) {
        nomeDato = dato;
        settaInfo4(metaInfo);
        return;
      }
    if (metaInfo.length() == 6) {
      settaInfo6(metaInfo);
      return;
    }
    if (metaInfo.length() == 8) {
      settaInfo8(metaInfo);
      return;
    }
    if (metaInfo.length() == 9) {
      settaInfo9(metaInfo);
      return;
    }

    throw new Exception ("Campo non identificabile all'interno di ["+pCampoHtml+"]");
  }

  /**
   * 
   */
  private void settaInfo1(String pInfo) {
    tipoCampo = pInfo.substring(0,1);
    tipoAccesso = "";
    lunghezza = "";
    decimali = "";
  }

  private void settaInfo4(String pInfo) {
	    tipoCampo = pInfo.substring(0,1);
	    tipoAccesso = "";
	    lunghezza = pInfo.substring(1,4);
	    decimali = "";
	  }

 /**
   * 
   */
  private void settaInfo6(String pInfo) {
    tipoCampo = pInfo.substring(0,1);
    tipoAccesso = pInfo.substring(1,2);
    lunghezza = pInfo.substring(2,4);
    decimali = pInfo.substring(4,6);
    tipoCalcolato = "S";
  }

  /**
   * 
   */
  private void settaInfo8(String pInfo) {
    tipoCampo = pInfo.substring(0,1);
    tipoAccesso = pInfo.substring(1,2);
    lunghezza = pInfo.substring(2,5);
    decimali = pInfo.substring(5,8);
    tipoCalcolato = "S";
  }

  /**
   * 
   */
  private void settaInfo9(String pInfo) {
    tipoCampo = pInfo.substring(0,1);
    tipoAccesso = pInfo.substring(1,2);
    tipoCalcolato = pInfo.substring(2,3);
    lunghezza = pInfo.substring(3,6);
    decimali = pInfo.substring(6,9);
  }

  /**
   * 
   */
  private void settaHref(String pHref) {
    
    int j = -1;
    int i = pHref.indexOf("link=");
    if (i > -1) {
      i = i + 6;
      j = pHref.indexOf("\"", i);
      href = pHref.substring(i,j);
    }

  }

  /**
   * 
   */
  private void settaFunc(String pFunc) {
    int j = -1;
    int i = pFunc.indexOf("func=");
    if (i > -1) {
      i = i + 6;
      j = pFunc.indexOf("\"", i);
      funzione = pFunc.substring(i,j);
    }

  }

  /**
   * 
   */
  private void settaStile(String pStile) {

    int j = -1;
    int k = -1;
    int i = -1; 
    i = pStile.indexOf("style=");
    if (i > -1) {
      j = pStile.indexOf("\"", i+7);
      k = pStile.indexOf("'", i+7);
      if (k == -1) {
        k = j;
      }
      if (j == -1) {
        j = k;
      }
      if (j < k) {
        style = pStile.substring(i+7,j);
      } else {
        style = pStile.substring(i+7,k);
      }
    }

    i = pStile.indexOf("class=");
    if (i > -1) {
      j = pStile.indexOf("\"", i+7);
      k = pStile.indexOf("'", i+7);
      if (k == -1) {
        k = j;
      }
      if (j == -1) {
        j = k;
      }
      if (j < k) {
        classname = pStile.substring(i+7,j);
      } else {
        classname = pStile.substring(i+7,k);
      }
    }

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
  public String getAreaModelloDato() {
    return area_cm_dato ;
  }
  
  /**
   * 
   */
  public String getModelloDato() {
    return cm_dato;
  }
  
  /**
   * 
   */
  public String getCategoriaDato() {
    return categoria_dato;
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
    retval = "_"+metaData+"_";
    if (!getAreaDato().equalsIgnoreCase("")) {
      retval += getAreaDato()+"*";
    }
    retval += getDato();
    if (!getBlocco().equalsIgnoreCase("")) {
      retval += "!"+getBlocco();
    }else{
      if (!getFunc().equalsIgnoreCase("")) {
        retval += "@"+getFunc();
      }
    }
    if (!getElementiAjax().equalsIgnoreCase("")) {
      retval += "{"+getElementiAjax()+"}";
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
//  

  /**
   * 
   */
  public String getClassName() {
    return classname;
  }
  /**
   * Main
   * @param args: usati per specificare alias, db, password, area, directory
   */
  public static void main(String[] args) {

    String pDato = "<a href=\"#taglayout\">&lt;<!-- ADSPROPERTY _T020_COGNOME ADSFORMAT -->COGNOME<!-- link=\"\" -->&gt;<!-- ADSTFE --></a>";

    try {
      InformazioniCampo ic = new InformazioniCampo(pDato,"ADSFORMAT -->");
      System.out.println("Nome: "+ic.getNomeDato());
      System.out.println("Area: "+ic.getAreaModelloDato());
      System.out.println("Modello: "+ic.getModelloDato());
    } catch (Exception e) {
      System.out.println(e.toString());
    }
  }

}