package it.finmatica.modulistica.modulisticapack;

import it.finmatica.jfc.dbUtil.IDbOperationSQL;
import javax.servlet.http.HttpServletRequest;

public class FineVisualizzaHTML implements IElementoModello {
  private String sHtml = "</span>";
  public FineVisualizzaHTML() throws Exception {
  }

  public String getPRNComValue() {
    return sHtml;
  }
  public String getPRNComValue(IDbOperationSQL dbOpEsterna) {
    return sHtml;
  }

  public String getPRNValue(IDbOperationSQL dbOpEsterna) {
    return sHtml;
  }

  public String getPRNValue() {
    return sHtml;
  }

  public String getValue(IDbOperationSQL dbOpEsterna) {
    return sHtml;
  }

  public String getValue() {
    return sHtml;
  }

  public String getZValue() {
    return sHtml;
  }

  public void release() {
  }

  public void settaListFields(String l_fields) {
  }

  public void settaProtetto(boolean b_protetto) {
    // TODO Auto-generated method stub
    
  }

}
