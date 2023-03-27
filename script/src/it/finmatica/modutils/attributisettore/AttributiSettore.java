package it.finmatica.modutils.attributisettore;
import it.finmatica.modutils.informazioniSettore.InformazioniSettore;

public class AttributiSettore {
  private String  settoreVis = "";
  private String  dominioVis = "";
  private String  adsFormat = "";
  
  public AttributiSettore(String pHtml) throws Exception {

    String myID = "";
    String myTitle = "";
    
    int i,j;
    i = pHtml.indexOf("id=\"")+4;
    j = pHtml.indexOf("\"",i);
    myID = pHtml.substring(i,j);
    i = pHtml.indexOf("title=\"")+7;
    j = pHtml.indexOf("\"",i);
    myTitle = pHtml.substring(i,j);
   
    adsFormat = "&lt;<!-- ADSTVB -->&lt;<!-- VISUALPROPERTY "+myID+
                " VISUALFORMAT -->"+myTitle+"&gt;<!-- ADSTVE -->&gt;";

    InformazioniSettore infoV = new InformazioniSettore(adsFormat,"&lt;<!-- VISUALPROPERTY","VISUALFORMAT -->","<!-- ADSTVE -->");

    settoreVis = infoV.getSettore();   
    dominioVis = infoV.getDominio();      
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
  public String getSettore() {
    return settoreVis;
  }

  /**
   * 
   */
  public String getDominio() {
    return dominioVis;
  }
  
}