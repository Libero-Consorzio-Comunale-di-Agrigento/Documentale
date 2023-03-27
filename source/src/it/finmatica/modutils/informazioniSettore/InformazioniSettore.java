package it.finmatica.modutils.informazioniSettore;

public class InformazioniSettore {
  private String  settore = "";
  private String  dominio = "";

  public InformazioniSettore(String pHtml, 
                             String pNameVisualBegin, 
                             String pNameVisualEnd,
                             String pTagVisualEnd) throws Exception {
    int inizio, fine, inizioNome, fineNome;

    inizio = pHtml.indexOf(pNameVisualBegin) + pNameVisualBegin.length() + 1; 
    fine = pHtml.indexOf(pNameVisualEnd) - 1;
    dominio = pHtml.substring(inizio,fine);
    inizio = fine + pNameVisualEnd.length() + 1; 
    fine = pHtml.indexOf(pTagVisualEnd) - 4;
    settore = pHtml.substring(inizio,fine);    
    inizioNome = settore.indexOf("(") + 1; 
    fineNome = settore.indexOf(")");
    settore = settore.substring(inizioNome ,fineNome);   
  }

  /**
   * 
   */
  public String getDominio() {
    return dominio;
  }

  /**
   * 
   */
  public String getSettore() {
    return settore;
  }
}