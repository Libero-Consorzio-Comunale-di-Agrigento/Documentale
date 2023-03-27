package it.finmatica.modutils.attributipopup;
import it.finmatica.modutils.informazioniPopup.InformazioniPopup;

public class AttributiPopup {
  private String  nomePopup = "";
  private String  area_blocco = "";
  private String  nomeBlocco = "";
  private String  campi = "";
  private String  adsFormat = "";

  public AttributiPopup(String pHtml) throws Exception {
    String myID = "";
    String myName = "";
    String myTitle = "";
    String myVal = "";
    
    int i,j;
    i = pHtml.indexOf("id=\"")+4;
    j = pHtml.indexOf("\"",i);
    myID = pHtml.substring(i,j);
    i = pHtml.indexOf("name=\"")+6;
    j = pHtml.indexOf("\"",i);
    myName = pHtml.substring(i,j);
    i = pHtml.indexOf("title=\"")+7;
    j = pHtml.indexOf("\"",i);
    myTitle = pHtml.substring(i,j);
    if (!myName.equalsIgnoreCase("")) {
      myID = myName+"*"+myID;
    }
    if (myTitle.equalsIgnoreCase("")) {
      myVal = myID;
    } else {
      myVal = myID+"!"+myTitle;
    }

    adsFormat = "&lt;<!-- ADSTCB -->&lt;<!-- POPPROPERTY "+myVal+
                " POPFORMAT -->&gt;<!-- ADSTCE -->&gt;";

    InformazioniPopup infoP = new InformazioniPopup(adsFormat,"&lt;<!-- POPPROPERTY","POPFORMAT -->");
    nomePopup = infoP.getNome();
    area_blocco = infoP.getAreaBlocco();
    nomeBlocco = infoP.getBlocco();
    campi = infoP.getCampi();  

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
  public String getNome() {
    return nomePopup;
  }

  /**
   * 
   */
  public String getBlocco() {
    return nomeBlocco;
  }
  /**
   * 
   */
  public String getAreaBlocco() {
    return area_blocco;
  }
  /**
   * 
   */
  public String getCampi() {
    return campi;
  }
  
}