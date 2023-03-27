package it.finmatica.modutils.informazioniTab;

public class InformazioniTab {
  private String  labelTab = "";
  private String  dominioVis = "";

  public InformazioniTab(String pHtml, 
                             String pNameTabBegin, 
                             String pNameTabEnd) throws Exception {
    int inizio, fine;
    inizio = pHtml.indexOf(pNameTabBegin) + pNameTabBegin.length() + 1; 
    fine = pHtml.indexOf(pNameTabEnd) - 1;
    String newHtml = pHtml.substring(inizio,fine);
    int i = newHtml.indexOf("@");
    if (i > -1) {
      labelTab = newHtml.substring(0,i);
      dominioVis = newHtml.substring(i+1);
    } else {
      labelTab = newHtml;
    }
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