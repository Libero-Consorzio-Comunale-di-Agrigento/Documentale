package it.finmatica.modutils.informazioniPopup;

public class InformazioniPopup {
  private String  area_blocco = "";
  private String  nomePopup = "";
  private String  nomeBlocco = "";
  private String  campiRitorno = "";

  public InformazioniPopup(String pHtml, 
                             String pNameTabBegin, 
                             String pNameTabEnd) throws Exception {
    int inizio, fine;
    inizio = pHtml.indexOf(pNameTabBegin) + pNameTabBegin.length() + 1; 
    fine = pHtml.indexOf(pNameTabEnd) - 1;
    String newHtml = pHtml.substring(inizio,fine);
    int i = newHtml.indexOf("*");
    int j = newHtml.indexOf("!");
    if (i > -1) {
      nomePopup = newHtml.substring(0,i);
    } else {
      if (j > -1) {
        nomePopup = newHtml.substring(0,j);
      } else {
        nomePopup = newHtml;
      }
    }
    if ((i > -1) && (j > -1)) {
      nomeBlocco = newHtml.substring(i+1,j);
    } else {
      if (i > -1) {
        nomeBlocco = newHtml.substring(i+1);
      } else {
        nomeBlocco = nomePopup;
      }
    }
    int k = nomeBlocco.indexOf("$");
    if (k > -1) {
      area_blocco = nomeBlocco.substring(0,k);
      nomeBlocco = nomeBlocco.substring(k+1);
    } 
    if (j > -1) {
      campiRitorno = newHtml.substring(j+1);
    } 
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
  public String getAreaBlocco() {
    return area_blocco;
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
  public String getCampi() {
    return campiRitorno;
  }
  
}