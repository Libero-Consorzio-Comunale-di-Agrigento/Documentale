/***********************************************************************
 * Module:  Testo.java
 * Author:  adelmo
 * Created: marted� 19 marzo 2002 12.18.01
 * Purpose: Defines the Class Testo
 ***********************************************************************/
 
package it.finmatica.modulistica.modulisticapack;

//import java.util.*;

import it.finmatica.jfc.dbUtil.IDbOperationSQL;

/**
 * 
 */
public class Testo implements IElementoModello {
  String testo;

  /**
   * 
   */
  public Testo(String pTestoHtml) {
    testo = pTestoHtml;
  }

  public String getValue(IDbOperationSQL dbOpEsterna) {
    return testo;
  }

  /**
   * 
   */
  public String getValue() {
    return testo;
  }

  /**
   * 
   */
  public String getZValue() {
    return null;
  }

  public String getPRNValue() {
    return getPRNValue(null);
  }

  /**
   * NUOVO
   */
  public String getPRNValue(IDbOperationSQL dbOpEsterna) {
    return testo;
  }  
  
  /**
   * NUOVO
   */
  public String getPRNComValue() {
    return testo;
  }

  public String getPRNComValue(IDbOperationSQL dbOpEsterna) {
    return testo;
  }
  
  /**
  * Quando chiamata questa funzione non fa nulla
  */
  public void release(){
  }

  public void settaListFields(String l_fields){
  }

  public void settaProtetto(boolean b_protetto) {
    // TODO Auto-generated method stub
    
  }
}

