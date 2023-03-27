package it.finmatica.modulistica.modulisticapack;


public class Etichetta {
  protected String  area;
  protected String  cm;
  protected String  etichetta;
  protected String  valore;
  protected String  icona;
  protected String  nome;
  protected String  tooltip;
  protected String  separatore;
  protected String  controllo;
  protected String  controllo_js;
  protected long    data;

  public Etichetta(String pArea, 
                   String pCm, 
                   String pEtichetta, 
                   String pValore, 
                   String pIcona, 
                   String pNome, 
                   String pTooltip, 
                   long   pData,
                   String pSepa,
                   String pContr,
                   String pContr_js) {

    area      = pArea;
    cm        = pCm;
    etichetta = pEtichetta;
    if (pValore == null) {
      valore = "";
    } else {
      valore    = pValore;
    }
    if (pIcona == null) {
      icona = "";
    } else {
      icona    = pIcona;
    }
    if (pNome == null) {
      nome = "";
    } else {
      nome    = pNome;
    }
    if (pTooltip == null) {
      tooltip = "";
    } else {
      tooltip    = pTooltip;
    }
    if (pSepa == null) {
      separatore = "";
    } else {
      separatore    = pSepa;
    }
    if (pContr == null) {
      controllo = "";
    } else {
      controllo    = pContr;
    }
    if (pContr_js == null) {
      controllo_js = "";
    } else {
      controllo_js    = pContr_js;
    }
    data      = pData;

  }
  
  public String getArea() {
    return area;
  }
  public String getCm() {
    return cm;
  }
  public long getData() {
    return data;
  }
  public String getEtichetta() {
    return etichetta;
  }
  public String getIcona() {
    return icona;
  }
  public String getNome() {
    return nome;
  }
  public String getTooltip() {
    return tooltip;
  }
  public String getValore() {
    return valore;
  }

  public String getControllo() {
    return controllo;
  }

  public String getControllo_js() {
    return controllo_js;
  }

  public String getSeparatore() {
    return separatore;
  }


}
