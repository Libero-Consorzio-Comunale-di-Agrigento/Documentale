package it.finmatica.modutils.attributiblocco;
import it.finmatica.modutils.informazioniblocco.InformazioniBlocco;

public class AttributiBlocco {
  private String  area_blocco = "";
  private String  blocco = "";
  private String  legame = "";
  private String  numeroRecord = "";
  private String  aggiungi = "";
  private String  ordinamento = "";
  private String  adsFormat = "";

  public AttributiBlocco(String pBloccoHtml) throws Exception {
    String myID = "";
    String myTitle = "";
    int i,j;
    i = pBloccoHtml.indexOf("id=\"")+4;
    j = pBloccoHtml.indexOf("\"",i);
    myID = pBloccoHtml.substring(i,j);
    i = pBloccoHtml.indexOf("title=\"")+7;
    j = pBloccoHtml.indexOf("\"",i);
    myTitle = pBloccoHtml.substring(i,j);
    adsFormat = "&lt;<!-- ADSTBB -->&lt;<!-- BLOCCOPROPERTY "+myID+
       " BLOCCOFORMAT -->"+myTitle+"&gt;<!-- ADSTBE -->&gt;";
    InformazioniBlocco infoB = new InformazioniBlocco(pBloccoHtml);

    area_blocco = infoB.getAreaBlocco();
    blocco = infoB.getBlocco();
    
    legame = infoB.getLegame();
    numeroRecord = infoB.getNumeroRecord();
    aggiungi = infoB.getAggiungi();
    ordinamento = infoB.getOrdinamento(); 
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
  public String getAggiungi() {
    return aggiungi;
  }
  /**
   * 
   */
  public String getBlocco() {
    return blocco;
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
  public String getLegame() {
    return legame;
  }
  /**
   * 
   */
  public String getNumeroRecord() {
    return numeroRecord;
  }
  /**
   * 
   */
  public String getOrdinamento() {
    return ordinamento;
  }
}