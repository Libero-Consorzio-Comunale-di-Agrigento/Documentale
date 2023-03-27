package it.finmatica.modutils.attributicampoblocco;

import it.finmatica.modutils.informazionicampo.InformazioniCampo;

public class AttributiCampoBlocco 
{
  private String  area_cm_dato = "";
  private String  cm_dato = "";
  private String  categoria_dato = "";
  private String  dato = "";
  private String  tipoCampo = "";
  private String  href = "";
  private String  func = "";
  private String  adsFormat = "";
  private String  metaData = "";
  private String  nomeDato = "";
   
  public AttributiCampoBlocco(String pCampoHtml)throws Exception {
    String metaStringa = "";
    String metaInfo = "";
    String newCampo = "";
    String metahref = "";
    String metaFunc = "";
    String tagDato = "";
   
    int i,j,k,h,h1,h2;
    i = pCampoHtml.indexOf("name=\"")+6;
    j = pCampoHtml.indexOf("\"",i);
    metaStringa = pCampoHtml.substring(i,j);
    k = metaStringa.indexOf("!");
    if (k > -1){
      metaInfo = metaStringa.substring(0,k);
      h = metaStringa.indexOf("@",k);
      if (h > -1){
        metaFunc = metaStringa.substring(h+1);
        metahref = metaStringa.substring(k+1,h);
      }else
      metahref = metaStringa.substring(k+1);      
    }else{
      h1 = metaStringa.indexOf("_",1);
      h2 = metaStringa.indexOf("@",h1+1);
      if (h2 > -1){
        metaInfo = metaStringa.substring(0,h2);
        metaFunc = metaStringa.substring(h2+1);
      }else{
        metaInfo = metaStringa;
      }
    }

    if (pCampoHtml.indexOf("<input") > -1) {
      i = pCampoHtml.indexOf("value=\"");
      if (i > -1) {
        i = i + 7;
        j = pCampoHtml.indexOf("\"",i);
        tagDato = pCampoHtml.substring(i,j);
      }      
    }

    newCampo = "&lt;<!-- ADSPROPERTY "+metaInfo+" ADSFORMAT -->"+tagDato;
    newCampo += "<!-- link=\""+metahref+"\" -->";
    
    //adsFormat = "&lt;<!-- ADSTFB -->"+newCampo+"&gt;";
    if (metaFunc.equals("")){
      newCampo += "&gt;<!-- ADSTFE -->";
      adsFormat = "<a href=\"#taglayout\">"+newCampo+"</a>";
    }else{
      newCampo += "<!-- func=\""+metaFunc+"\" -->&gt;<!-- ADSTFE -->";
      adsFormat = "<a href=\"#tagfunc\">"+newCampo+"</a>";
    }
    
    InformazioniCampo infoC = new InformazioniCampo(newCampo,"ADSFORMAT -->");
    dato =  infoC.getDato();
    if (tagDato.equalsIgnoreCase("")) {
      tagDato = dato;
    }
/*    if (!infoC.getCategoriaDato().equalsIgnoreCase("")) {
      dato += "#"+infoC.getCategoriaDato();
    }
    if (!infoC.getAreaModelloDato().equalsIgnoreCase("")) {
      dato += "#"+infoC.getAreaModelloDato()+"#"+infoC.getModelloDato();
    }*/
    tipoCampo = infoC.getTipoCampo();
    href = infoC.getHref();
    metaData = infoC.getMetaData();
    nomeDato = infoC.getNomeDato();
    func = infoC.getFunc();
    area_cm_dato = infoC.getAreaModelloDato();
    cm_dato = infoC.getModelloDato();
    categoria_dato = infoC.getCategoriaDato();
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
  public String getTipoCampo() {
    return tipoCampo;
  }
  
  /**
   * 
   */
  public String getFunc() {
    return func;
  }
  
  /**
   * 
   */
  public String getMetaData() {
    String retval = "";
    retval = "_"+metaData+"_"+getDato();
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
  public String getDescTipo() {
    if (tipoCampo.equalsIgnoreCase("L")) {
      return "Link";
    }
    if (tipoCampo.equalsIgnoreCase("T")) {
      return "Testo";
    }
    if (tipoCampo.equalsIgnoreCase("K")) {
      return "Campo chiave";
    }
    if (tipoCampo.equalsIgnoreCase("D")) {
      return "Link al documento";
    }
     return "Testo";
  }

  /**
   * 
   */
  public String getHref() {
    return href;
  }
  
}