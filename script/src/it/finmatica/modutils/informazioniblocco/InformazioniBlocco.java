package it.finmatica.modutils.informazioniblocco;

public class InformazioniBlocco {
  private String  area_blocco = "";
  private String  blocco = "";
  private String  legame = "";
  private String  numeroRecord = "";
  private String  aggiungi = "";
  private String  ordinamento = "";
    
  public InformazioniBlocco(String pBloccoHtml) throws Exception {
//    String metaInfo = "";
    int inizioMeta, fineMeta, inizioInfo, fineInfo;
    
    inizioMeta = pBloccoHtml.indexOf("@");
    fineMeta = pBloccoHtml.lastIndexOf("@",inizioMeta+1);
    
    if (fineMeta == -1 || inizioMeta == -1 ) {
      throw new Exception ("Blocco non identificabile all'interno di ["+pBloccoHtml+"]");
    }
    fineInfo = pBloccoHtml.indexOf("@",inizioMeta+1);
    blocco = pBloccoHtml.substring(inizioMeta+1,fineInfo);

    int j = blocco.indexOf("*");
    if (j > -1) {
      area_blocco = blocco.substring(0,j);
      blocco = blocco.substring(j+1);
    } 
    
    inizioInfo = fineInfo;
    fineInfo = pBloccoHtml.indexOf("@",inizioInfo+1);
    numeroRecord = pBloccoHtml.substring(inizioInfo+1,fineInfo);
    inizioInfo = fineInfo;
    fineInfo = pBloccoHtml.indexOf("@",inizioInfo+1);
    legame = pBloccoHtml.substring(inizioInfo+1,fineInfo);
    inizioInfo = fineInfo;
    fineInfo = pBloccoHtml.indexOf("@",inizioInfo+1);
    ordinamento = pBloccoHtml.substring(inizioInfo+1,fineInfo);
    inizioInfo = fineInfo;
    fineInfo = pBloccoHtml.indexOf("@",inizioInfo+1);
    aggiungi = pBloccoHtml.substring(inizioInfo+1,fineInfo); 
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