package it.finmatica.modutils.attributitab;
import it.finmatica.modutils.informazioniTab.InformazioniTab;

public class AttributiTab {
  private String  labelTab = "";
  private String  dominioVis = "";
  private String  adsFormat = "";

  public AttributiTab(String pHtml) throws Exception {
    String myID = "";
    String myTitle = "";
    
    int i,j;
    i = pHtml.indexOf("id=\"")+4;
    j = pHtml.indexOf("\"",i);
    myID = pHtml.substring(i,j);
    i = pHtml.indexOf("title=\"")+7;
    j = pHtml.indexOf("\"",i);
    myTitle = pHtml.substring(i,j);

    adsFormat = "&lt;<!-- ADSTTB -->&lt;<!-- TABPROPERTY "+myID+
                " TABFORMAT -->"+myTitle+"&gt;<!-- ADSTTE -->&gt;";

    InformazioniTab infoT = new InformazioniTab(adsFormat,"&lt;<!-- TABPROPERTY","TABFORMAT -->");
    labelTab = infoT.getLabel();
    dominioVis = infoT.getDominio();  

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
  public String getLabel() {
    return labelTab;
  }

  /**
   * 
   */
  public String getDominio() {
    return dominioVis;
  }
  
}